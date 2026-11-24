# 次期メジャーバージョン（5.0）の整理リスト

wicket-iziToast と wicket-toastr は、4.1.0 で wicket-feedback-js のアダプターになりました。  
4.x では 4.0 の公開 API を維持するため、互換用のコードが残っています。5.0 でまとめて整理します。


## 4.x で守ること

| 対象 | 守る内容 | 確認方法 |
| ---- | ---- | ---- |
| 公開 API | public / protected のシグネチャ、クラスの継承関係 | `IziToastBehaviorExtensionTest`、`ToastrBehaviorExtensionTest` |
| `ToastOption#toJsonString` | 末尾カンマ付きの出力形式（`{"message":"a",}`） | 4.0 からある既存テスト |
| 本文とタイトル | HTML として表示する（JavaScript のエスケープだけを行う） | `keepsMarkupInMessage` |


## 1. コアに置き換えて削除するもの

| 対象 | 4.x で残している理由 | 5.0 |
| ---- | ---- | ---- |
| `IziToastBehavior`、`ToastrBehavior` の委譲（`ExtensionPointAdapter`、`ExtensionPointBehavior`） | 親クラス（`*ResourcesBehavior`）と protected メソッドを維持するため | `FeedbackJsBehavior<M>` を直接使うか継承する。委譲先も Behavior なので、現在は `IBehaviorInstantiationListener` が 1 つの Behavior につき 2 回呼ばれる |
| `IziToastResourcesBehavior`、`ToastrResourcesBehavior` | 公開クラスのため | 廃止を検討する。リソースの出力がアダプターの `renderResources` と重複している |
| `IziToastSetting`、`ToastrSetting` | 公開クラスのため | `FeedbackJsSetting` |
| `IziToastBehaviorAutoAppender`、`ToastrBehaviorAutoAppender` | 公開クラスのため | `FeedbackJsSetting` の `setAutoAppendBehavior` |
| `ToastType`、`ToastLevel`、`FeedbackMessageLevel` | 公開 enum のため | `MessageLevel` |
| `ToastLevelFeedbackMessageFilter` | 上の enum に依存するため | `MessageLevelFilter` |
| `ToastIgnoreFilter` | 公開クラスのため | `MessageIgnoreFilter` |
| `IToast`、`Toast` | `getTitle` と `getToastOption` の型が両者で異なるため（値と `Optional`） | `Message`、または `Message` を基にした型 |
| `IToastOption`、`ToastOption` | 公開 API のため | `JsOptions` を基にした型。リフレクションによる JSON 化と、項目ごとに手書きしたマージが不要になる |
| `ToastMessageCombiner`（toastr は `combine(Stream)`） | 公開 API のため | `MessageCombiner` |


## 2. 先に決めること

挙動や設計が変わるため、置き換える前に方針を決めます。

### HTML エスケープの既定値
4.x は本文を HTML として表示します。メッセージにユーザーの入力値が含まれると、HTML を注入される余地があります。  
5.0 では既定でエスケープし（`Js.html`）、HTML を使いたい場合だけ明示的に指定する形を推奨します。

### レベルごとのオプションと全体のオプション
`EachLevelToastOptions` と `setGlobalOption` に相当する機能は、コアにありません。アダプターに持たせるか、コアに追加するかを決めます。  
レベルごとのオプションは、適用範囲も両者で異なります。

| | 文字列のメッセージ | `IToast` のメッセージ |
| ---- | ---- | ---- |
| iziToast | 適用する | 適用する（マージ） |
| toastr | 適用する | 適用しない |

### `PLAIN` と `QUESTION`
iziToast の `ToastType` にあるこの 2 つは、`MessageLevel` に対応する値がありません。iziToast 専用の型として残すか、オプションで指定するかを決めます。

### 結合の方法
既存の `ToastMessageCombiner` は、各メッセージの後ろに必ず suffix（`<br>`）を付けます。1 件だけのときも付きます。  
コアの `MessageCombiner` は、2 件以上のときだけ separator（改行）で区切ります。

### トーストの表示先（iziToast）
`IToastTargetLinker` は、`IToast` を書き換えて表示先を設定します。`Message` は変更できないため、`toMessage` で設定済みのものを返す形に変えます。

### 設定の持ち方
既存の設定は、アプリケーションに 1 つだけです（`MetaDataKey`）。コアの `FeedbackJsSetting` は複数持てる代わりに、どこからでも取得できる `get()` がありません。

### ヘッダーへの直接出力
`Toast#show(IHeaderResponse)` はスクリプトをその場で実行します。コアの `IFeedbackMessageAdapter#show` は DOM の準備後に実行します。

### `DEBUG` の扱い
コアも既存に合わせて、`FeedbackMessage.DEBUG` を `ERROR` として表示します。変える場合はコアも変更します。


## 3. 一緒に直す小さなもの

| 対象 | 内容 |
| ---- | ---- |
| iziToast `ToastOption#meargeOptions` | つづり（merge） |
| toastr `ToastOption#getCloseDureation`、`setCloseDureation` | つづり（duration） |
| toastr `timeOut`、`extendedTimeOut` | 数値が文字列として出力される（`"timeOut":"0"`） |
| iziToast `Toast#getHideScript`、`ToastTargetContainerCreateBehavior` | id を `Js.string` を通さずに埋め込んでいる |
| iziToast `Toast#replaceNewlineCodeToTag` | 使われていない。toastr と異なり、改行は `<br/>` に変換されない |
| iziToast の Javadoc | 生成時にエラーになる（4.0.0 から。原因は未調査） |


## 4. 各ライブラリーに残すもの

ライブラリー固有の機能なので、コアには移しません。

* iziToast: `ToastAjaxEventBehavior`、表示先のコンテナー（`ToastTargetContainerCreateBehavior` など）、`Toast#destroy`、`Toast#hideAll`、`Toast#settings`
* toastr: FontAwesome の設定、`Toast#clear`、`Toast#remove`
* 共通: CSS と JavaScript の `ResourceReference`


## 進め方

1. 「2. 先に決めること」の方針を決める
2. コアに足りない機能を追加する
3. 両ライブラリーを置き換える。`*BehaviorExtensionTest` は 4.x の互換性を確認するテストなので、あわせて削除するか書き直す
4. 移行方法を CHANGELOG に書く（wicket-toastr には CHANGELOG がないため、新しく作る）
