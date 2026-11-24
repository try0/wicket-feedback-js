package jp.try0.wicket.feedback.js.core.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.wicket.feedback.FeedbackMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import jp.try0.wicket.feedback.js.core.js.JsOptions;
import jp.try0.wicket.feedback.js.core.test.AbstractFeedbackJsTest;
import jp.try0.wicket.feedback.js.core.test.FeedbackJsTestPage;

/**
 * {@link Message} tests.
 *
 * @author try0
 *
 */
public class MessageTest extends AbstractFeedbackJsTest {

	@Test
	public void factories() {
		assertEquals(MessageLevel.INFO, Message.info("text").getLevel());
		assertEquals(MessageLevel.SUCCESS, Message.success("text").getLevel());
		assertEquals(MessageLevel.WARNING, Message.warn("text").getLevel());
		assertEquals(MessageLevel.ERROR, Message.error("text").getLevel());
		assertEquals("text", Message.info("text").getText());
	}

	@Test
	public void create_rejectsUnsupportedLevel() {
		assertThrows(IllegalArgumentException.class, () -> Message.create(MessageLevel.UNDEFINED, "text"));
	}

	@Test
	public void defaults() {
		Message message = Message.info("text");

		assertFalse(message.getTitle().isPresent());
		assertEquals("{}", message.getOptions().toString());
	}

	@Test
	public void with_returnsChangedCopy() {
		Message message = Message.info("text");

		Message changed = message.withTitle("title").withText("changed").withOption("timer", 1);

		assertEquals("title", changed.getTitle().orElseThrow());
		assertEquals("changed", changed.getText());
		assertEquals("{\"timer\":1}", changed.getOptions().toString());
		assertEquals("text", message.getText());
		assertFalse(message.getTitle().isPresent());
		assertEquals("{}", message.getOptions().toString());
	}

	@Test
	public void withOptions_mergesIntoCurrentOptions() {
		Message message = Message.info("text").withOption("timer", 1).withOption("icon", "a");

		Message changed = message.withOptions(JsOptions.create().set("timer", 2));

		assertEquals("{\"timer\":2,\"icon\":\"a\"}", changed.getOptions().toString());
	}

	@Test
	public void withOptions_isIndependentOfArgument() {
		JsOptions options = JsOptions.create().set("timer", 1);
		Message message = Message.info("text").withOptions(options);

		options.set("timer", 2);

		assertEquals("{\"timer\":1}", message.getOptions().toString());
	}

	/**
	 * A FeedbackPanel displays the message properly, too.
	 */
	@Test
	public void toString_isText() {
		assertEquals("text", Message.info("text").withTitle("title").toString());
	}

	@Test
	public void from_convertsPlainMessage() {
		Message message = Message.from(new FeedbackMessage(null, "text", FeedbackMessage.WARNING)).orElseThrow();

		assertEquals(MessageLevel.WARNING, message.getLevel());
		assertEquals("text", message.getText());
	}

	@Test
	public void from_usesMessageItself() {
		Message message = Message.success("text").withTitle("title");

		assertSame(message, Message.from(new FeedbackMessage(null, message, FeedbackMessage.INFO)).orElseThrow());
	}

	@Test
	public void from_ignoresUnsupportedLevel() {
		assertFalse(Message.from(new FeedbackMessage(null, "text", FeedbackMessage.UNDEFINED)).isPresent());
	}

	@ParameterizedTest
	@EnumSource(value = MessageLevel.class, mode = Mode.EXCLUDE, names = "UNDEFINED")
	public void show_registersFeedbackMessageOfSameLevel(MessageLevel level) {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		Message message = Message.create(level, "text");

		message.show(page);

		FeedbackMessage feedbackMessage = page.getFeedbackMessages().first();
		assertSame(message, feedbackMessage.getMessage());
		assertEquals(level, MessageLevel.fromFeedbackMessageLevel(feedbackMessage.getLevel()));
	}

}
