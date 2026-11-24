package jp.try0.wicket.feedback.js.core.message;

import java.io.Serializable;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.util.lang.Args;

import jp.try0.wicket.feedback.js.core.js.JsOptions;

/**
 * Message that does not depend on any JavaScript library.<br>
 * Immutable. It can be passed to methods such as {@link Component#info(Serializable)} to add a title or options to a
 * feedback message.
 *
 * @author try0
 *
 */
public final class Message implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a message.
	 *
	 * @param level the level
	 * @param text the text
	 * @return new message
	 */
	public static Message create(MessageLevel level, String text) {
		return new Message(level, text, null, JsOptions.create());
	}

	/**
	 * Creates an info message.
	 *
	 * @param text the text
	 * @return new message
	 */
	public static Message info(String text) {
		return create(MessageLevel.INFO, text);
	}

	/**
	 * Creates a success message.
	 *
	 * @param text the text
	 * @return new message
	 */
	public static Message success(String text) {
		return create(MessageLevel.SUCCESS, text);
	}

	/**
	 * Creates a warning message.
	 *
	 * @param text the text
	 * @return new message
	 */
	public static Message warn(String text) {
		return create(MessageLevel.WARNING, text);
	}

	/**
	 * Creates an error message.
	 *
	 * @param text the text
	 * @return new message
	 */
	public static Message error(String text) {
		return create(MessageLevel.ERROR, text);
	}

	/**
	 * Converts a feedback message. If the feedback message holds a {@link Message}, it is used as is.
	 *
	 * @param feedbackMessage the feedback message
	 * @return the message. If the level is not supported, empty.
	 */
	public static Optional<Message> from(FeedbackMessage feedbackMessage) {
		final MessageLevel level = MessageLevel.fromFeedbackMessageLevel(feedbackMessage.getLevel());
		if (!level.isSupported()) {
			return Optional.empty();
		}
		if (feedbackMessage.getMessage() instanceof Message message) {
			return Optional.of(message);
		}
		return Optional.of(create(level, feedbackMessage.getMessage().toString()));
	}

	private final MessageLevel level;
	private final String text;
	private final String title;
	private final JsOptions options;

	private Message(MessageLevel level, String text, String title, JsOptions options) {
		this.level = Args.notNull(level, "level");
		if (!level.isSupported()) {
			throw new IllegalArgumentException("This level is unsupported.");
		}
		this.text = Args.notNull(text, "text");
		this.title = title;
		this.options = options;
	}

	/**
	 * Gets the level.
	 *
	 * @return the level
	 */
	public MessageLevel getLevel() {
		return level;
	}

	/**
	 * Gets the text. It is not escaped.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets the title. It is not escaped.
	 *
	 * @return the title
	 */
	public Optional<String> getTitle() {
		return Optional.ofNullable(title);
	}

	/**
	 * Gets a copy of the options for the library that displays this message.
	 *
	 * @return the options
	 */
	public JsOptions getOptions() {
		return JsOptions.create().merge(options);
	}

	/**
	 * Creates a copy with the text.
	 *
	 * @param text the text
	 * @return new message
	 */
	public Message withText(String text) {
		return new Message(level, text, title, options);
	}

	/**
	 * Creates a copy with the title.
	 *
	 * @param title the title
	 * @return new message
	 */
	public Message withTitle(String title) {
		return new Message(level, text, title, options);
	}

	/**
	 * Creates a copy with the option added.
	 *
	 * @param key the option key
	 * @param value the option value
	 * @return new message
	 */
	public Message withOption(String key, Serializable value) {
		return new Message(level, text, title, getOptions().set(key, value));
	}

	/**
	 * Creates a copy with the options added.
	 *
	 * @param options the options that take priority over the current options
	 * @return new message
	 */
	public Message withOptions(JsOptions options) {
		return new Message(level, text, title, this.options.merge(options));
	}

	/**
	 * Registers this message as a feedback message.
	 *
	 * @param component the component that reports this message
	 */
	public void show(Component component) {
		switch (level) {
		case INFO -> component.info(this);
		case SUCCESS -> component.success(this);
		case WARNING -> component.warn(this);
		case ERROR -> component.error(this);
		default -> throw new IllegalStateException("This level is unsupported.");
		}
	}

	/**
	 * Gets the text.
	 */
	@Override
	public String toString() {
		return text;
	}

}
