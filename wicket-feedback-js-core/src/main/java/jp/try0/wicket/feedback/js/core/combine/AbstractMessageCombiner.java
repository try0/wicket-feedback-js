package jp.try0.wicket.feedback.js.core.combine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Combiner that combines the messages of each group, such as the level.<br>
 * Messages that are not combined come first, followed by the groups in order of first appearance.
 *
 * @author try0
 *
 * @param <M> type of the messages
 */
public abstract class AbstractMessageCombiner<M> implements IMessageCombiner<M> {
	private static final long serialVersionUID = 1L;

	@Override
	public List<M> combine(List<M> messages) {
		final List<M> results = new ArrayList<>();
		final Map<Object, List<M>> groups = new LinkedHashMap<>();

		for (M message : messages) {
			if (isCombinable(message)) {
				groups.computeIfAbsent(getGroupKey(message), key -> new ArrayList<>()).add(message);
			} else {
				results.add(message);
			}
		}

		groups.values().forEach(group -> results.add(combineGroup(group)));
		return results;
	}

	/**
	 * Whether the message can be combined with other messages.
	 *
	 * @param message the message
	 * @return true if the message can be combined
	 */
	protected boolean isCombinable(M message) {
		return true;
	}

	/**
	 * Gets the key of the group. Messages with the same key are combined.
	 *
	 * @param message the message
	 * @return the key
	 */
	protected abstract Object getGroupKey(M message);

	/**
	 * Combines the messages of a group.
	 *
	 * @param group the messages. Contains at least one message.
	 * @return combined message
	 */
	protected abstract M combineGroup(List<M> group);

}
