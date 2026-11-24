package jp.try0.wicket.feedback.js.core.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

/**
 * Page for tests.
 *
 * @author try0
 *
 */
public class FeedbackJsTestPage extends WebPage {
	private static final long serialVersionUID = 1L;

	private final WebMarkupContainer container;
	private final AjaxLink<Void> ajaxLink;

	public FeedbackJsTestPage() {
		container = newContainer("container");
		container.setOutputMarkupId(true);
		add(container);

		ajaxLink = new AjaxLink<Void>("ajaxLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				onClickAjaxLink(target);
			}
		};
		add(ajaxLink);
	}

	protected WebMarkupContainer newContainer(String id) {
		return new WebMarkupContainer(id);
	}

	protected void onClickAjaxLink(AjaxRequestTarget target) {
		// noop
	}

	public WebMarkupContainer getContainer() {
		return container;
	}

	public AjaxLink<Void> getAjaxLink() {
		return ajaxLink;
	}

}
