package jp.try0.wicket.feedback.js.samples.adapter;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

import jp.try0.wicket.feedback.js.core.adapter.IMessageAdapter;
import jp.try0.wicket.feedback.js.core.js.JsOptions;
import jp.try0.wicket.feedback.js.core.message.Message;

/**
 * Adapter for SweetAlert2.<br>
 * An example of a library that displays only one message at a time.
 *
 * @author try0
 *
 */
public class SweetAlert2Adapter implements IMessageAdapter {
	private static final long serialVersionUID = 1L;

	private static final String BASE_URL = "https://cdn.jsdelivr.net/npm/sweetalert2@11.14.5/dist/";

	@Override
	public CharSequence toScript(Message message) {
		return "Swal.fire(" + toOptions(message) + ");";
	}

	/**
	 * Opens each modal after the previous one is closed.
	 */
	@Override
	public CharSequence toBatchScript(List<Message> messages) {
		final StringBuilder script = new StringBuilder("Promise.resolve()");
		for (Message message : messages) {
			script.append(".then(function() { return Swal.fire(").append(toOptions(message)).append("); })");
		}
		return script.append(";");
	}

	private JsOptions toOptions(Message message) {
		return JsOptions.create()
				.set("icon", message.getLevel().getLevelString())
				.set("title", message.getTitle().orElse(null))
				.set("text", message.getText())
				.merge(message.getOptions());
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(
				new UrlResourceReference(Url.parse(BASE_URL + "sweetalert2.min.css"))));
		response.render(JavaScriptHeaderItem.forReference(
				new UrlResourceReference(Url.parse(BASE_URL + "sweetalert2.min.js"))));
	}

}
