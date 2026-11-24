package jp.try0.wicket.feedback.js.samples;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

import jp.try0.wicket.feedback.js.core.config.FeedbackJsSetting;
import jp.try0.wicket.feedback.js.core.feedback.MessageLevelFilter;
import jp.try0.wicket.feedback.js.core.message.MessageLevel;
import jp.try0.wicket.feedback.js.samples.adapter.NotyfAdapter;
import jp.try0.wicket.feedback.js.samples.adapter.SweetAlert2Adapter;

/**
 * Displays errors with SweetAlert2, and other messages with Notyf.
 *
 * @author try0
 *
 */
public class WicketApplication extends WebApplication {

	@Override
	public Class<? extends WebPage> getHomePage() {
		return HomePage.class;
	}

	@Override
	protected void init() {
		super.init();

		// SweetAlert2 and Notyf are loaded from a CDN
		getCspSettings().blocking().disabled();

		FeedbackJsSetting.createInitializer(this, new NotyfAdapter())
				.setAutoAppendBehavior(true)
				.setMessageFilter(MessageLevelFilter.lessThan(MessageLevel.ERROR))
				.initialize();

		FeedbackJsSetting.createInitializer(this, new SweetAlert2Adapter())
				.setAutoAppendBehavior(true)
				.setMessageFilter(MessageLevelFilter.greaterThanOrEqual(MessageLevel.ERROR))
				.initialize();
	}

}
