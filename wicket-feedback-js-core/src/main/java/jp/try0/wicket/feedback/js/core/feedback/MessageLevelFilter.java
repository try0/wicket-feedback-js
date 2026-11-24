package jp.try0.wicket.feedback.js.core.feedback;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Predicate;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;

import jp.try0.wicket.feedback.js.core.message.Message;
import jp.try0.wicket.feedback.js.core.message.MessageLevel;

/**
 * Filter by {@link MessageLevel}.
 *
 * @author try0
 *
 */
public class MessageLevelFilter implements IFeedbackMessageFilter {
	private static final long serialVersionUID = 1L;

	/**
	 * Accepts levels less than the argument.
	 *
	 * @param level the level
	 * @return new filter
	 */
	public static MessageLevelFilter lessThan(MessageLevel level) {
		return where(lv -> lv.lessThan(level));
	}

	/**
	 * Accepts levels less than or equal to the argument.
	 *
	 * @param level the level
	 * @return new filter
	 */
	public static MessageLevelFilter lessThanOrEqual(MessageLevel level) {
		return where(lv -> !lv.greaterThan(level));
	}

	/**
	 * Accepts levels greater than the argument.
	 *
	 * @param level the level
	 * @return new filter
	 */
	public static MessageLevelFilter greaterThan(MessageLevel level) {
		return where(lv -> lv.greaterThan(level));
	}

	/**
	 * Accepts levels greater than or equal to the argument.
	 *
	 * @param level the level
	 * @return new filter
	 */
	public static MessageLevelFilter greaterThanOrEqual(MessageLevel level) {
		return where(lv -> !lv.lessThan(level));
	}

	/**
	 * Accepts the levels.
	 *
	 * @param levels the levels
	 * @return new filter
	 */
	public static MessageLevelFilter accepts(MessageLevel... levels) {
		return new MessageLevelFilter(Arrays.asList(levels));
	}

	/**
	 * Accepts levels other than the arguments.
	 *
	 * @param levels the levels
	 * @return new filter
	 */
	public static MessageLevelFilter ignores(MessageLevel... levels) {
		return where(lv -> !Arrays.asList(levels).contains(lv));
	}

	private static MessageLevelFilter where(Predicate<MessageLevel> condition) {
		return new MessageLevelFilter(Arrays.stream(MessageLevel.values()).filter(condition).toList());
	}

	private final EnumSet<MessageLevel> accepts = EnumSet.noneOf(MessageLevel.class);

	/**
	 * Constructor
	 *
	 * @param accepts the levels to be accepted
	 */
	public MessageLevelFilter(Collection<MessageLevel> accepts) {
		this.accepts.addAll(accepts);
	}

	@Override
	public boolean accept(FeedbackMessage feedbackMessage) {
		if (feedbackMessage.getMessage() instanceof Message message) {
			return accepts.contains(message.getLevel());
		}
		return accepts.contains(MessageLevel.fromFeedbackMessageLevel(feedbackMessage.getLevel()));
	}

}
