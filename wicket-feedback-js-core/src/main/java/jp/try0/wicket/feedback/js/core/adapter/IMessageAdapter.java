package jp.try0.wicket.feedback.js.core.adapter;

import java.util.Optional;

import org.apache.wicket.feedback.FeedbackMessage;

import jp.try0.wicket.feedback.js.core.js.Js;
import jp.try0.wicket.feedback.js.core.message.Message;

/**
 * Adapter that uses {@link Message}. Only {@link #toScript(Object)} needs to be implemented.
 * <p>
 * Use {@link Js} to write values into the script.
 * </p>
 *
 * <pre>
 * IMessageAdapter adapter = message -&gt; "alert(" + Js.string(message.getText()) + ");";
 * </pre>
 *
 * @author try0
 *
 */
@FunctionalInterface
public interface IMessageAdapter extends IFeedbackMessageAdapter<Message> {

	@Override
	default Optional<Message> toMessage(FeedbackMessage feedbackMessage) {
		return Message.from(feedbackMessage);
	}

}
