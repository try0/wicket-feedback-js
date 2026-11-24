package jp.try0.wicket.feedback.js.core.feedback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.junit.jupiter.api.Test;

import jp.try0.wicket.feedback.js.core.message.Message;
import jp.try0.wicket.feedback.js.core.message.MessageLevel;

/**
 * {@link MessageLevelFilter} and {@link MessageIgnoreFilter} tests.
 *
 * @author try0
 *
 */
public class MessageFilterTest {

	private static FeedbackMessage feedbackMessage(MessageLevel level) {
		int feedbackMessageLevel = switch (level) {
		case UNDEFINED -> FeedbackMessage.UNDEFINED;
		case INFO -> FeedbackMessage.INFO;
		case SUCCESS -> FeedbackMessage.SUCCESS;
		case WARNING -> FeedbackMessage.WARNING;
		case ERROR -> FeedbackMessage.ERROR;
		};
		return new FeedbackMessage(null, "text", feedbackMessageLevel);
	}

	private static void assertAccepts(IFeedbackMessageFilter filter, MessageLevel... expected) {
		List<MessageLevel> accepted = Arrays.stream(MessageLevel.values())
				.filter(level -> filter.accept(feedbackMessage(level)))
				.toList();
		assertEquals(List.of(expected), accepted);
	}

	@Test
	public void level_accepts() {
		assertAccepts(MessageLevelFilter.accepts(MessageLevel.INFO, MessageLevel.ERROR),
				MessageLevel.INFO, MessageLevel.ERROR);
		assertAccepts(MessageLevelFilter.accepts());
	}

	@Test
	public void level_ignores() {
		assertAccepts(MessageLevelFilter.ignores(MessageLevel.INFO, MessageLevel.ERROR),
				MessageLevel.UNDEFINED, MessageLevel.SUCCESS, MessageLevel.WARNING);
	}

	@Test
	public void level_lessThan() {
		assertAccepts(MessageLevelFilter.lessThan(MessageLevel.WARNING),
				MessageLevel.UNDEFINED, MessageLevel.INFO, MessageLevel.SUCCESS);
	}

	@Test
	public void level_lessThanOrEqual() {
		assertAccepts(MessageLevelFilter.lessThanOrEqual(MessageLevel.SUCCESS),
				MessageLevel.UNDEFINED, MessageLevel.INFO, MessageLevel.SUCCESS);
	}

	@Test
	public void level_greaterThan() {
		assertAccepts(MessageLevelFilter.greaterThan(MessageLevel.SUCCESS),
				MessageLevel.WARNING, MessageLevel.ERROR);
	}

	@Test
	public void level_greaterThanOrEqual() {
		assertAccepts(MessageLevelFilter.greaterThanOrEqual(MessageLevel.WARNING),
				MessageLevel.WARNING, MessageLevel.ERROR);
	}

	@Test
	public void level_usesLevelOfMessage() {
		FeedbackMessage feedbackMessage = new FeedbackMessage(null, Message.error("text"), FeedbackMessage.INFO);

		assertTrue(MessageLevelFilter.accepts(MessageLevel.ERROR).accept(feedbackMessage));
		assertFalse(MessageLevelFilter.accepts(MessageLevel.INFO).accept(feedbackMessage));
	}

	@Test
	public void ignore_rejectsMessage() {
		MessageIgnoreFilter filter = new MessageIgnoreFilter();

		assertFalse(filter.accept(new FeedbackMessage(null, Message.info("text"), FeedbackMessage.INFO)));
		assertTrue(filter.accept(new FeedbackMessage(null, "text", FeedbackMessage.INFO)));
	}

	@Test
	public void ignore_rejectsSpecifiedType() {
		MessageIgnoreFilter filter = new MessageIgnoreFilter(Number.class);

		assertFalse(filter.accept(new FeedbackMessage(null, 1, FeedbackMessage.INFO)));
		assertTrue(filter.accept(new FeedbackMessage(null, Message.info("text"), FeedbackMessage.INFO)));
	}

}
