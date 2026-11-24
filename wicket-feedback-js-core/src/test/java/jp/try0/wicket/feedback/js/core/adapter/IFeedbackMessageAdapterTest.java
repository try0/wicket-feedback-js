package jp.try0.wicket.feedback.js.core.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.junit.jupiter.api.Test;

import jp.try0.wicket.feedback.js.core.message.Message;
import jp.try0.wicket.feedback.js.core.message.MessageLevel;
import jp.try0.wicket.feedback.js.core.test.AbstractFeedbackJsTest;
import jp.try0.wicket.feedback.js.core.test.FeedbackJsTestPage;

/**
 * {@link IFeedbackMessageAdapter} and {@link IMessageAdapter} tests.
 *
 * @author try0
 *
 */
public class IFeedbackMessageAdapterTest extends AbstractFeedbackJsTest {

	@Test
	public void toBatchScript_concatenatesScripts() {
		IMessageAdapter adapter = message -> message.getText() + "();";

		assertEquals("a();b();", adapter.toBatchScript(List.of(Message.info("a"), Message.info("b"))).toString());
	}

	@Test
	public void toBatchScript_terminatesStatements() {
		IMessageAdapter adapter = message -> message.getText() + "() ";

		assertEquals("a();b();", adapter.toBatchScript(List.of(Message.info("a"), Message.info("b"))).toString());
	}

	@Test
	public void toBatchScript_skipsEmptyScripts() {
		IMessageAdapter adapter = message -> message.getLevel() == MessageLevel.INFO ? null : "a();";

		assertEquals("a();", adapter.toBatchScript(List.of(Message.info("a"), Message.error("b"))).toString());
	}

	@Test
	public void toMessage_convertsToNeutralMessage() {
		IMessageAdapter adapter = message -> "";

		assertEquals("text",
				adapter.toMessage(new FeedbackMessage(null, "text", FeedbackMessage.INFO)).orElseThrow().getText());
		assertFalse(
				adapter.toMessage(new FeedbackMessage(null, "text", FeedbackMessage.UNDEFINED)).isPresent());
	}

	@Test
	public void show_onAjaxRequest() {
		IMessageAdapter adapter = message -> "show_" + message.getText() + "();";
		FeedbackJsTestPage page = new FeedbackJsTestPage() {
			@Override
			protected void onClickAjaxLink(AjaxRequestTarget target) {
				adapter.show(Message.info("message"), target);
			}
		};
		getWicketTester().startPage(page);
		getWicketTester().clickLink(page.getAjaxLink());

		assertTrue(getWicketTester().getLastResponseAsString().contains("show_message();"));
	}

	@Test
	public void show_onPageRendering() {
		IMessageAdapter adapter = message -> "show_" + message.getText() + "();";
		getWicketTester().startPage(new FeedbackJsTestPage() {
			@Override
			public void renderHead(IHeaderResponse response) {
				super.renderHead(response);
				adapter.show(Message.info("message"), response);
			}
		});

		assertTrue(getWicketTester().getLastResponseAsString().contains("show_message();"));
	}

}
