package jp.try0.wicket.feedback.js.core.message;

import org.apache.wicket.feedback.FeedbackMessage;

/**
 * Level of a message. The levels that most of the JavaScript libraries have in common.
 *
 * @author try0
 *
 */
public enum MessageLevel {

	/**
	 * Not displayed.
	 */
	UNDEFINED("undefined", FeedbackMessage.UNDEFINED),
	/**
	 * Info
	 */
	INFO("info", FeedbackMessage.INFO),
	/**
	 * Success
	 */
	SUCCESS("success", FeedbackMessage.SUCCESS),
	/**
	 * Warning
	 */
	WARNING("warning", FeedbackMessage.WARNING),
	/**
	 * Error
	 */
	ERROR("error", FeedbackMessage.ERROR),;

	/**
	 * Gets the level corresponding to the level of a feedback message.<br>
	 * {@link FeedbackMessage#DEBUG} and {@link FeedbackMessage#FATAL} are {@link #ERROR}.
	 *
	 * @param feedbackMessageLevel the level of a feedback message
	 * @return the level. If there is no corresponding level, {@link #UNDEFINED}.
	 */
	public static MessageLevel fromFeedbackMessageLevel(int feedbackMessageLevel) {
		return switch (feedbackMessageLevel) {
		case FeedbackMessage.INFO -> INFO;
		case FeedbackMessage.SUCCESS -> SUCCESS;
		case FeedbackMessage.WARNING -> WARNING;
		case FeedbackMessage.DEBUG, FeedbackMessage.ERROR, FeedbackMessage.FATAL -> ERROR;
		default -> UNDEFINED;
		};
	}

	private final String levelString;
	private final int feedbackMessageLevel;

	MessageLevel(String levelString, int feedbackMessageLevel) {
		this.levelString = levelString;
		this.feedbackMessageLevel = feedbackMessageLevel;
	}

	/**
	 * Gets the lower case name, such as "success". Many libraries use it as a method name or a type.
	 *
	 * @return the name of the level
	 */
	public String getLevelString() {
		return levelString;
	}

	/**
	 * Whether messages of this level are displayed.
	 *
	 * @return true if displayed
	 */
	public boolean isSupported() {
		return this != UNDEFINED;
	}

	/**
	 * Whether this level is less than the argument.
	 *
	 * @param level the level to compare
	 * @return true if this level is less than the argument
	 */
	public boolean lessThan(MessageLevel level) {
		return feedbackMessageLevel < level.feedbackMessageLevel;
	}

	/**
	 * Whether this level is greater than the argument.
	 *
	 * @param level the level to compare
	 * @return true if this level is greater than the argument
	 */
	public boolean greaterThan(MessageLevel level) {
		return feedbackMessageLevel > level.feedbackMessageLevel;
	}

}
