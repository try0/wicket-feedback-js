package jp.try0.wicket.feedback.js.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.feedback.ExactLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.junit.jupiter.api.Test;

import jp.try0.wicket.feedback.js.core.adapter.IMessageAdapter;
import jp.try0.wicket.feedback.js.core.behavior.FeedbackJsBehavior;
import jp.try0.wicket.feedback.js.core.combine.MessageCombiner;
import jp.try0.wicket.feedback.js.core.js.Js;
import jp.try0.wicket.feedback.js.core.test.AbstractFeedbackJsTest;
import jp.try0.wicket.feedback.js.core.test.FeedbackJsTestPage;

/**
 * {@link FeedbackJsSetting} tests.
 *
 * @author try0
 *
 */
public class FeedbackJsSettingTest extends AbstractFeedbackJsTest {

	private static final IMessageAdapter ADAPTER = message -> "lib." + message.getLevel().getLevelString()
			+ "(" + Js.string(message.getText()) + ");";

	private String start(FeedbackJsTestPage page) {
		getWicketTester().startPage(page);
		return getWicketTester().getLastResponseAsString();
	}

	@Test
	public void autoAppend_addsBehaviorToAllPages() {
		FeedbackJsSetting.createInitializer(getWebApplication(), ADAPTER)
				.setAutoAppendBehavior(true)
				.initialize();

		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.error("message");

		assertEquals(1, page.getBehaviors(FeedbackJsBehavior.class).size());
		assertTrue(page.getContainer().getBehaviors(FeedbackJsBehavior.class).isEmpty());
		assertTrue(start(page).contains("lib.error(\"message\");"));
	}

	@Test
	public void autoAppend_isDisabledByDefault() {
		FeedbackJsSetting.createInitializer(getWebApplication(), ADAPTER).initialize();

		assertTrue(new FeedbackJsTestPage().getBehaviors(FeedbackJsBehavior.class).isEmpty());
	}

	@Test
	public void newBehavior_usesSetting() {
		FeedbackJsSetting<?> setting = FeedbackJsSetting.createInitializer(getWebApplication(), ADAPTER)
				.setMessageFilter(new ExactLevelFeedbackMessageFilter(FeedbackMessage.ERROR))
				.setMessageCombiner(new MessageCombiner().setSeparator("|"))
				.initialize();

		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(setting.newBehavior());
		page.info("a");
		page.error("b");
		page.error("c");

		String response = start(page);
		assertFalse(response.contains("lib.info("));
		assertTrue(response.contains("lib.error(\"b|c\");"));
	}

	@Test
	public void newBehavior_createsNewInstance() {
		FeedbackJsSetting<?> setting = FeedbackJsSetting.createInitializer(getWebApplication(), ADAPTER)
				.initialize();

		assertNotSame(setting.newBehavior(), setting.newBehavior());
	}

	@Test
	public void behaviorFactory_isUsed() {
		FeedbackJsSetting.createInitializer(getWebApplication(), ADAPTER)
				.setAutoAppendBehavior(true)
				.setBehaviorFactory(adapter -> new FeedbackJsBehavior<>(adapter) {
					@Override
					protected String getScriptForDisplay(java.util.List<FeedbackMessage> feedbackMessages) {
						return "custom();" + super.getScriptForDisplay(feedbackMessages);
					}
				})
				.initialize();

		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.error("message");

		assertTrue(start(page).contains("custom();lib.error(\"message\");"));
	}

	@Test
	public void settingsWithDifferentAdaptersCoexist() {
		IMessageAdapter modal = message -> "modal(" + Js.string(message.getText()) + ");";
		FeedbackJsSetting.createInitializer(getWebApplication(), ADAPTER)
				.setAutoAppendBehavior(true)
				.setMessageFilter(message -> message.getLevel() < FeedbackMessage.ERROR)
				.initialize();
		FeedbackJsSetting.createInitializer(getWebApplication(), modal)
				.setAutoAppendBehavior(true)
				.setMessageFilter(message -> message.getLevel() >= FeedbackMessage.ERROR)
				.initialize();

		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.info("a");
		page.error("b");

		String response = start(page);
		assertTrue(response.contains("lib.info(\"a\");"));
		assertTrue(response.contains("modal(\"b\");"));
		assertFalse(response.contains("lib.error("));
	}

}
