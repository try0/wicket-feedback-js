package jp.try0.wicket.feedback.js.samples.adapter;

import java.io.Serializable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

import jp.try0.wicket.feedback.js.core.adapter.IMessageAdapter;
import jp.try0.wicket.feedback.js.core.js.JsOptions;
import jp.try0.wicket.feedback.js.core.message.Message;

/**
 * Adapter for Notyf.<br>
 * An example of a library that needs an instance, and inserts messages as HTML.
 *
 * @author try0
 *
 */
public class NotyfAdapter implements IMessageAdapter {
	private static final long serialVersionUID = 1L;

	private static final String BASE_URL = "https://cdn.jsdelivr.net/npm/notyf@3.10.0/";

	@Override
	public CharSequence toScript(Message message) {
		return "window.notyf.open(" + JsOptions.create()
				.set("type", message.getLevel().getLevelString())
				.setHtml("message", message.getText())
				.merge(message.getOptions()) + ");";
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.render(CssHeaderItem.forReference(
				new UrlResourceReference(Url.parse(BASE_URL + "notyf.min.css"))));
		response.render(JavaScriptHeaderItem.forReference(
				new UrlResourceReference(Url.parse(BASE_URL + "notyf.min.js"))));
		// needs document.body
		response.render(OnDomReadyHeaderItem.forScript("window.notyf = window.notyf || new Notyf("
				+ JsOptions.create()
						.set("duration", 4000)
						.set("types", new Serializable[] {
								JsOptions.create().set("type", "info").set("background", "#2f6fdb"),
								JsOptions.create().set("type", "warning").set("background", "#b86e00"),
						})
				+ ");"));
	}

}
