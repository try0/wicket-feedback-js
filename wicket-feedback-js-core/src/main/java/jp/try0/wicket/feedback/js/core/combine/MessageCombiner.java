package jp.try0.wicket.feedback.js.core.combine;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.util.lang.Args;

import jp.try0.wicket.feedback.js.core.message.Message;

/**
 * Combines the messages of the same level into one message.<br>
 * The last title wins, and the options of later messages take priority.
 *
 * @author try0
 *
 */
public class MessageCombiner extends AbstractMessageCombiner<Message> {
	private static final long serialVersionUID = 1L;

	private String prefix = "";
	private String separator = "\n";

	/**
	 * Sets the prefix of each text.
	 *
	 * @param prefix the prefix
	 * @return this
	 */
	public MessageCombiner setPrefix(String prefix) {
		this.prefix = Args.notNull(prefix, "prefix");
		return this;
	}

	/**
	 * Sets the separator between the texts. The default is a newline.
	 *
	 * @param separator the separator
	 * @return this
	 */
	public MessageCombiner setSeparator(String separator) {
		this.separator = Args.notNull(separator, "separator");
		return this;
	}

	@Override
	protected Object getGroupKey(Message message) {
		return message.getLevel();
	}

	@Override
	protected Message combineGroup(List<Message> group) {
		if (group.size() == 1) {
			return group.get(0);
		}

		final String text = group.stream()
				.map(message -> prefix + message.getText())
				.collect(Collectors.joining(separator));
		return group.stream()
				.reduce((combined, message) -> message
						.withTitle(message.getTitle().or(combined::getTitle).orElse(null))
						.withOptions(combined.getOptions().merge(message.getOptions())))
				.orElseThrow()
				.withText(text);
	}

}
