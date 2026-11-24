# wicket-feedback-js

English | [日本語](README.ja.md)

Displays [Apache Wicket](https://wicket.apache.org/) feedback messages with any JavaScript library for toasts, alerts or messages.  
You only write the part that converts a message to JavaScript. Collecting, filtering, combining and rendering feedback messages on page renderings and Ajax requests is up to this library.

| Version | Wicket | Java |
| ---- | ---- | ---- |
| 2.0.0-SNAPSHOT | 11.x | 21 |
| 1.0.0-SNAPSHOT | 10.x | 17 |

Ready-made adapters:

* [wicket-iziToast](https://github.com/try0/wicket-iziToast) (4.1.0 or later)
* [wicket-toastr](https://github.com/try0/wicket-toastr) (4.1.0 or later)


# Usage

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

Implement the conversion, and set it up in `Application#init`.

```java
IMessageAdapter adapter = message -> "alert(" + Js.string(message.getText()) + ");";

FeedbackJsSetting.createInitializer(this, adapter)
        .setAutoAppendBehavior(true) // adds the behavior to all pages
        .initialize();
```

Feedback messages are now displayed with `alert`.

```java
info("info");
error("error");
getSession().success("success");
```

Without `setAutoAppendBehavior(true)`, add the behavior to a page or any component of it.

```java
add(new FeedbackJsBehavior<>(adapter));
```


## Adapters

`IMessageAdapter` requires `toScript` only. Override the other methods as the library needs.

```java
public class SweetAlert2Adapter implements IMessageAdapter {

    // a message -> JavaScript
    @Override
    public CharSequence toScript(Message message) {
        return "Swal.fire(" + toOptions(message) + ");";
    }

    // all messages of a request -> JavaScript
    // SweetAlert2 replaces the previous modal, so open them one by one
    @Override
    public CharSequence toBatchScript(List<Message> messages) {
        StringBuilder script = new StringBuilder("Promise.resolve()");
        for (Message message : messages) {
            script.append(".then(function() { return Swal.fire(").append(toOptions(message)).append("); })");
        }
        return script.append(";");
    }

    // resources, and scripts to set up the library
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

An adapter is shared by pages and serialized with them, so it should not hold any state of a request.

If the library has its own message type, implement `IFeedbackMessageAdapter<M>` instead. wicket-iziToast and wicket-toastr do so.


## Escaping

`Message#getText` and `Message#getTitle` are not escaped. Always write values through `Js` or `JsOptions`, and choose by how the library treats the value.

| The library treats the value as | Literal | Option |
| ---- | ---- | ---- |
| text | `Js.string(text)` | `JsOptions#set(key, text)` |
| HTML | `Js.html(text)` | `JsOptions#setHtml(key, text)` |
| code, such as a function | `Js.expression(code)` | `JsOptions#set(key, Js.expression(code))` |

`Js.html` escapes markup and converts newlines to `<br>`.  
Feedback messages may contain user input (for example, validation messages that use `${input}`), so do not use `Js.string` for libraries that insert HTML, unless the message is trusted.


## Title and options

Pass a `Message` to add a title, or options of the library.

```java
Message.error("message")
        .withTitle("title")
        .withOption("timer", 3000)
        .withOption("didOpen", Js.expression("function() { console.log('opened'); }"))
        .show(component);
```


## Filters

```java
// a toast for information, and a modal for errors
FeedbackJsSetting.createInitializer(this, toastAdapter)
        .setMessageFilter(MessageLevelFilter.lessThan(MessageLevel.ERROR))
        .setAutoAppendBehavior(true)
        .initialize();

FeedbackJsSetting.createInitializer(this, modalAdapter)
        .setMessageFilter(MessageLevelFilter.greaterThanOrEqual(MessageLevel.ERROR))
        .setAutoAppendBehavior(true)
        .initialize();
```

`MessageIgnoreFilter` keeps a `FeedbackPanel` from displaying `Message`s.


## Combining

Combines the messages of the same level.

```java
initializer.setMessageCombiner(new MessageCombiner().setPrefix("- "));
```


# Samples

[wicket-feedback-js-samples](wicket-feedback-js-samples) contains the adapters for SweetAlert2 and Notyf. Run `jp.try0.wicket.feedback.js.samples.Start`.


# License

[Apache License 2.0](LICENSE)
