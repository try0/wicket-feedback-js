# wicket-feedback-js

[English](README.md) | 日本語

[Apache Wicket](https://wicket.apache.org/) のフィードバックメッセージを、好きな JavaScript ライブラリー（トースト、アラート、メッセージ表示など）で表示するためのアダプターです。  
実装するのは、メッセージを JavaScript に変換する部分だけです。フィードバックメッセージの収集・フィルター・結合と、ページ表示時や Ajax リクエスト時の出力は、このライブラリーが行います。

| バージョン | Wicket | Java |
| ---- | ---- | ---- |
| 2.0.0-SNAPSHOT | 11.x | 21 |
| 1.0.0-SNAPSHOT | 10.x | 17 |


# 使い方

Wicket 11.x
```xml
<dependency>
    <groupId>jp.try0.wicket</groupId>
    <artifactId>wicket-feedback-js-core</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

Wicket 10.x
```xml
<dependency>
    <groupId>jp.try0.wicket</groupId>
    <artifactId>wicket-feedback-js-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

変換処理を実装し、`Application#init` で設定します。

```java
IMessageAdapter adapter = message -> "alert(" + Js.string(message.getText()) + ");";

FeedbackJsSetting.createInitializer(this, adapter)
        .setAutoAppendBehavior(true) // すべてのページに Behavior を追加
        .initialize();
```

これで、フィードバックメッセージが `alert` で表示されます。

```java
info("info");
error("error");
getSession().success("success");
```

`setAutoAppendBehavior(true)` を使わない場合は、ページまたはページ内の任意のコンポーネントに Behavior を追加します。

```java
add(new FeedbackJsBehavior<>(adapter));
```


## アダプター

`IMessageAdapter` で実装が必須なのは `toScript` だけです。ほかのメソッドは、ライブラリーに応じてオーバーライドします。

```java
public class SweetAlert2Adapter implements IMessageAdapter {

    // メッセージ1件 -> JavaScript
    @Override
    public CharSequence toScript(Message message) {
        return "Swal.fire(" + toOptions(message) + ");";
    }

    // 1リクエスト分のメッセージ -> JavaScript
    // SweetAlert2 は前のモーダルを置き換えるため、1件ずつ順に開く
    @Override
    public CharSequence toBatchScript(List<Message> messages) {
        StringBuilder script = new StringBuilder("Promise.resolve()");
        for (Message message : messages) {
            script.append(".then(function() { return Swal.fire(").append(toOptions(message)).append("); })");
        }
        return script.append(";");
    }

    // リソースと、ライブラリーの初期化スクリプト
    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(...));
        response.render(JavaScriptHeaderItem.forReference(...));
    }

    private JsOptions toOptions(Message message) {
        return JsOptions.create()
                .set("icon", message.getLevel().getLevelString())
                .set("title", message.getTitle().orElse(null))
                .set("text", message.getText())
                .merge(message.getOptions());
    }
}
```

アダプターのインスタンスはページ間で共有され、ページと一緒にシリアライズされます。リクエストごとの状態を持たせないでください。

独自のメッセージ型を使う場合は、代わりに `IFeedbackMessageAdapter<M>` を実装します。wicket-iziToast と wicket-toastr はこの方法です。


## エスケープ

`Message#getText` と `Message#getTitle` はエスケープされていません。スクリプトに値を埋め込むときは必ず `Js` または `JsOptions` を使い、ライブラリーが値をどう扱うかに応じて使い分けてください。

| ライブラリーでの扱い | リテラル | オプション |
| ---- | ---- | ---- |
| テキスト | `Js.string(text)` | `JsOptions#set(key, text)` |
| HTML | `Js.html(text)` | `JsOptions#setHtml(key, text)` |
| 関数などのコード | `Js.expression(code)` | `JsOptions#set(key, Js.expression(code))` |

`Js.html` はマークアップをエスケープし、改行を `<br>` に変換します。  
フィードバックメッセージにはユーザーの入力値が含まれることがあります（`${input}` を使った検証メッセージなど）。HTML として挿入するライブラリーでは、信頼できるメッセージを除いて `Js.string` を使わないでください。


## タイトルとオプション

`Message` を使うと、タイトルやライブラリーのオプションを指定できます。

```java
Message.error("message")
        .withTitle("title")
        .withOption("timer", 3000)
        .withOption("didOpen", Js.expression("function() { console.log('opened'); }"))
        .show(component);
```


## フィルター

```java
// 通常のメッセージはトースト、エラーはモーダルで表示
FeedbackJsSetting.createInitializer(this, toastAdapter)
        .setMessageFilter(MessageLevelFilter.lessThan(MessageLevel.ERROR))
        .setAutoAppendBehavior(true)
        .initialize();

FeedbackJsSetting.createInitializer(this, modalAdapter)
        .setMessageFilter(MessageLevelFilter.greaterThanOrEqual(MessageLevel.ERROR))
        .setAutoAppendBehavior(true)
        .initialize();
```

`MessageIgnoreFilter` を使うと、`FeedbackPanel` に `Message` を表示させないようにできます。


## 結合

同じレベルのメッセージを1つに結合します。

```java
initializer.setMessageCombiner(new MessageCombiner().setPrefix("・"));
```


# サンプル

[wicket-feedback-js-samples](wicket-feedback-js-samples) に、SweetAlert2 と Notyf のアダプターがあります。`jp.try0.wicket.feedback.js.samples.Start` を実行してください。


# ライセンス

[Apache License 2.0](LICENSE)
