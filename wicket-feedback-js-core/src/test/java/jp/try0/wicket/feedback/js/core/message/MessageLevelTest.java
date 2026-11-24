package jp.try0.wicket.feedback.js.core.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.wicket.feedback.FeedbackMessage;
import org.junit.jupiter.api.Test;

/**
 * {@link MessageLevel} tests.
 *
 * @author try0
 *
 */
public class MessageLevelTest {

	@Test
	public void fromFeedbackMessageLevel() {
		assertEquals(MessageLevel.INFO, MessageLevel.fromFeedbackMessageLevel(FeedbackMessage.INFO));
		assertEquals(MessageLevel.SUCCESS, MessageLevel.fromFeedbackMessageLevel(FeedbackMessage.SUCCESS));
		assertEquals(MessageLevel.WARNING, MessageLevel.fromFeedbackMessageLevel(FeedbackMessage.WARNING));
		assertEquals(MessageLevel.ERROR, MessageLevel.fromFeedbackMessageLevel(FeedbackMessage.ERROR));
		assertEquals(MessageLevel.UNDEFINED, MessageLevel.fromFeedbackMessageLevel(FeedbackMessage.UNDEFINED));
		assertEquals(MessageLevel.UNDEFINED, MessageLevel.fromFeedbackMessageLevel(12345));
	}

	/**
	 * Same mapping as wicket-iziToast and wicket-toastr.
	 */
	@Test
	public void fromFeedbackMessageLevel_debugAndFatalAreError() {
		assertEquals(MessageLevel.ERROR, MessageLevel.fromFeedbackMessageLevel(FeedbackMessage.DEBUG));
		assertEquals(MessageLevel.ERROR, MessageLevel.fromFeedbackMessageLevel(FeedbackMessage.FATAL));
	}

	@Test
	public void getLevelString_isNameUsedByLibraries() {
		assertEquals("info", MessageLevel.INFO.getLevelString());
		assertEquals("success", MessageLevel.SUCCESS.getLevelString());
		assertEquals("warning", MessageLevel.WARNING.getLevelString());
		assertEquals("error", MessageLevel.ERROR.getLevelString());
	}

	@Test
	public void isSupported_exceptUndefined() {
		for (MessageLevel level : MessageLevel.values()) {
			assertEquals(level != MessageLevel.UNDEFINED, level.isSupported());
		}
	}

	@Test
	public void compare_followsFeedbackMessageLevel() {
		assertTrue(MessageLevel.INFO.lessThan(MessageLevel.SUCCESS));
		assertTrue(MessageLevel.SUCCESS.lessThan(MessageLevel.WARNING));
		assertTrue(MessageLevel.ERROR.greaterThan(MessageLevel.WARNING));
		assertFalse(MessageLevel.INFO.lessThan(MessageLevel.INFO));
		assertFalse(MessageLevel.INFO.greaterThan(MessageLevel.INFO));
	}

}
