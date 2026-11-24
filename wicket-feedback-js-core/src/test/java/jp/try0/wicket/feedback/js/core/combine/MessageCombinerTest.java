package jp.try0.wicket.feedback.js.core.combine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;

import jp.try0.wicket.feedback.js.core.message.Message;
import jp.try0.wicket.feedback.js.core.message.MessageLevel;

/**
 * {@link MessageCombiner} tests.
 *
 * @author try0
 *
 */
public class MessageCombinerTest {

	@Test
	public void none_returnsMessagesAsIs() {
		List<Message> messages = List.of(Message.info("a"), Message.info("b"));

		assertSame(messages, IMessageCombiner.<Message> none().combine(messages));
	}

	@Test
	public void combine_joinsMessagesOfSameLevel() {
		List<Message> combined = new MessageCombiner().combine(List.of(
				Message.error("a"), Message.error("b"), Message.error("c")));

		assertEquals(1, combined.size());
		assertEquals(MessageLevel.ERROR, combined.get(0).getLevel());
		assertEquals("a\nb\nc", combined.get(0).getText());
	}

	@Test
	public void combine_keepsOrderOfFirstAppearance() {
		List<Message> combined = new MessageCombiner().combine(List.of(
				Message.warn("w1"), Message.info("i1"), Message.error("e1"), Message.warn("w2"), Message.success("s1")));

		assertEquals(List.of(MessageLevel.WARNING, MessageLevel.INFO, MessageLevel.ERROR, MessageLevel.SUCCESS),
				combined.stream().map(Message::getLevel).toList());
	}

	@Test
	public void combine_keepsSingleMessageAsIs() {
		Message message = Message.info("a").withTitle("title");

		assertSame(message, new MessageCombiner().combine(List.of(message)).get(0));
	}

	@Test
	public void combine_usesPrefixAndSeparator() {
		MessageCombiner combiner = new MessageCombiner().setPrefix("- ").setSeparator(" / ");

		assertEquals("- a / - b", combiner.combine(List.of(Message.info("a"), Message.info("b"))).get(0).getText());
	}

	@Test
	public void combine_lastTitleWins() {
		List<Message> combined = new MessageCombiner().combine(List.of(
				Message.info("a").withTitle("1"), Message.info("b").withTitle("2"), Message.info("c")));

		assertEquals("2", combined.get(0).getTitle().orElseThrow());
	}

	@Test
	public void combine_withoutTitle() {
		List<Message> combined = new MessageCombiner().combine(List.of(Message.info("a"), Message.info("b")));

		assertFalse(combined.get(0).getTitle().isPresent());
	}

	@Test
	public void combine_mergesOptionsWithPriorityToLast() {
		List<Message> combined = new MessageCombiner().combine(List.of(
				Message.info("a").withOption("timer", 1).withOption("icon", "x"),
				Message.info("b").withOption("timer", 2)));

		assertEquals("{\"timer\":2,\"icon\":\"x\"}", combined.get(0).getOptions().toString());
	}

	@Test
	public void combine_putsNonCombinableMessagesFirst() {
		MessageCombiner combiner = new MessageCombiner() {
			@Override
			protected boolean isCombinable(Message message) {
				return !message.getText().startsWith("!");
			}
		};

		List<Message> combined = combiner.combine(List.of(
				Message.info("a"), Message.info("!b"), Message.info("c"), Message.info("!d")));

		assertEquals(List.of("!b", "!d", "a\nc"), combined.stream().map(Message::getText).toList());
	}

}
