package jp.try0.wicket.feedback.js.core.combine;

import java.util.List;

import org.apache.wicket.util.io.IClusterable;

/**
 * Combines messages before they are converted to JavaScript.
 *
 * @author try0
 *
 * @param <M> type of the messages
 */
@FunctionalInterface
public interface IMessageCombiner<M> extends IClusterable {

	/**
	 * Gets the combiner that does not combine messages.
	 *
	 * @param <M> type of the messages
	 * @return the combiner
	 */
	static <M> IMessageCombiner<M> none() {
		return messages -> messages;
	}

	/**
	 * Combines messages.
	 *
	 * @param messages the messages
	 * @return combined messages
	 */
	List<M> combine(List<M> messages);

}
