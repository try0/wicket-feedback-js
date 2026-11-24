package jp.try0.wicket.feedback.js.core.adapter;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;

/**
 * Converts feedback messages to the JavaScript of a library that displays messages, such as toasts or alerts.<br>
 * Implement this if the library has its own message type. Otherwise, {@link IMessageAdapter} is enough.
 * <p>
 * An instance is shared by pages and serialized with them. Implementations should not hold any state of a request.
 * </p>
 *
 * @author try0
 *
 * @param <M> type of the messages
 */
public interface IFeedbackMessageAdapter<M> extends IHeaderContributor {

	/**
	 * Converts a feedback message to the message type of this adapter.
	 *
	 * @param feedbackMessage the feedback message
	 * @return the message. If the feedback message should not be displayed, empty.
	 */
	Optional<M> toMessage(FeedbackMessage feedbackMessage);

	/**
	 * Converts a message to JavaScript that displays it.
	 *
	 * @param message the message
	 * @return the script
	 */
	CharSequence toScript(M message);

	/**
	 * Converts all messages of a request to JavaScript.<br>
	 * Override this if the library can not be called repeatedly, such as a modal that replaces the previous one.
	 *
	 * @param messages the messages
	 * @return the script
	 */
	default CharSequence toBatchScript(List<M> messages) {
		final StringBuilder batch = new StringBuilder();
		for (M message : messages) {
			final CharSequence script = toScript(message);
			if (script == null) {
				continue;
			}
			final String statement = script.toString().strip();
			batch.append(statement);
			if (!statement.isEmpty() && !statement.endsWith(";")) {
				batch.append(';');
			}
		}
		return batch;
	}

	/**
	 * Contributes the resources of the library, and scripts to set it up.<br>
	 * Called on every page rendering, before the messages are displayed.
	 *
	 * @param response the header response
	 */
	@Override
	default void renderHead(IHeaderResponse response) {
		// noop
	}

	/**
	 * Displays a message without feedback messages.
	 *
	 * @param message the message
	 * @param target the request handler
	 */
	default void show(M message, IPartialPageRequestHandler target) {
		target.appendJavaScript(toScript(message));
	}

	/**
	 * Displays a message without feedback messages.
	 *
	 * @param message the message
	 * @param response the header response
	 */
	default void show(M message, IHeaderResponse response) {
		response.render(OnDomReadyHeaderItem.forScript(toScript(message)));
	}

}
