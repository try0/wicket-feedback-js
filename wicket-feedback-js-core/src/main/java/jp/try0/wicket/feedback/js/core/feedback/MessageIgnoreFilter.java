package jp.try0.wicket.feedback.js.core.feedback;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.util.lang.Args;

import jp.try0.wicket.feedback.js.core.message.Message;

/**
 * Filter that ignores feedback messages holding a specific type.<br>
 * Set it to a {@link FeedbackPanel} to keep it from displaying the messages meant for the adapter.
 *
 * @author try0
 *
 */
public class MessageIgnoreFilter implements IFeedbackMessageFilter {
	private static final long serialVersionUID = 1L;

	private final Class<?> ignoreType;

	/**
	 * Constructor. Ignores {@link Message}.
	 */
	public MessageIgnoreFilter() {
		this(Message.class);
	}

	/**
	 * Constructor
	 *
	 * @param ignoreType the type to be ignored
	 */
	public MessageIgnoreFilter(Class<?> ignoreType) {
		this.ignoreType = Args.notNull(ignoreType, "ignoreType");
	}

	@Override
	public boolean accept(FeedbackMessage message) {
		return !ignoreType.isInstance(message.getMessage());
	}

}
