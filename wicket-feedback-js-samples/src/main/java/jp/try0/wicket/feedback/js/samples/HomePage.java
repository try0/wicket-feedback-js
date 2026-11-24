package jp.try0.wicket.feedback.js.samples;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.danekja.java.util.function.serializable.SerializableConsumer;

import jp.try0.wicket.feedback.js.core.message.Message;

/**
 * Home page.
 *
 * @author try0
 *
 */
public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HomePage() {
		addAjaxLink("info", target -> info("info"));
		addAjaxLink("success", target -> success("success"));
		addAjaxLink("warn", target -> warn("warn"));
		addAjaxLink("error", target -> error("error"));
		addAjaxLink("errors", target -> {
			error("1st error");
			error("2nd error");
		});
		addAjaxLink("all", target -> {
			info("info");
			success("success");
			error("error");
		});
		addAjaxLink("options", target -> Message.error("Displayed with a title and options")
				.withTitle("Title")
				.withOption("confirmButtonText", "Got it")
				.show(this));
		addAjaxLink("escape", target -> {
			String text = "\"quoted\" \\ <img src=x onerror=\"window.injected=true\"> </script> ]]>\n2nd line";
			info(text);
			error(text);
		});

		Form<Void> form = new Form<>("form");
		form.add(new TextField<>("number", new Model<Integer>(), Integer.class).setRequired(true));
		add(form);
	}

	private void addAjaxLink(String id, SerializableConsumer<AjaxRequestTarget> onClick) {
		add(new AjaxLink<Void>(id) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClick.accept(target);
			}
		});
	}

}
