package jp.try0.wicket.feedback.js.core.test;

import org.apache.wicket.Page;
import org.apache.wicket.feedback.DefaultCleanupFeedbackMessageFilter;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class of tests that need an application.
 *
 * @author try0
 *
 */
public abstract class AbstractFeedbackJsTest {

	private WebApplication application;
	private WicketTester tester;

	@BeforeEach
	public final void setUpApplication() {
		application = new WebApplication() {

			@Override
			public Class<? extends Page> getHomePage() {
				return FeedbackJsTestPage.class;
			}

		};
		tester = new WicketTester(application);
		// WicketTester keeps rendered messages by default. Removes them as a real application does.
		application.getApplicationSettings()
				.setFeedbackMessageCleanupFilter(new DefaultCleanupFeedbackMessageFilter());
	}

	@AfterEach
	public final void destroyApplication() {
		tester.destroy();
	}

	protected WebApplication getWebApplication() {
		return application;
	}

	protected WicketTester getWicketTester() {
		return tester;
	}

}
