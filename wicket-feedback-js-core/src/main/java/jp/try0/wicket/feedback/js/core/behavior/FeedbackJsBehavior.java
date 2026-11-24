package jp.try0.wicket.feedback.js.core.behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessagesModel;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.lang.Args;

import jp.try0.wicket.feedback.js.core.adapter.IFeedbackMessageAdapter;
import jp.try0.wicket.feedback.js.core.combine.IMessageCombiner;

/**
 * Displays the feedback messages of the page and the session with the JavaScript of an adapter.<br>
 * Can be added to a page or any component of it.
 *
 * @author try0
 *
 * @param <M> type of the messages of the adapter
 */
public class FeedbackJsBehavior<M> extends Behavior {
	private static final long serialVersionUID = 1L;

	private final IFeedbackMessageAdapter<M> adapter;
	private IFeedbackMessageFilter messageFilter;
	private IMessageCombiner<M> messageCombiner = IMessageCombiner.none();

	/**
	 * Messages already displayed in the current request.<br>
	 * {@link FeedbackMessage#isRendered()} can not be used, because it is also set by other components.
	 */
	private transient Set<FeedbackMessage> displayedMessages;

	/**
	 * Constructor
	 *
	 * @param adapter the adapter
	 */
	public FeedbackJsBehavior(IFeedbackMessageAdapter<M> adapter) {
		this(adapter, IFeedbackMessageFilter.ALL);
	}

	/**
	 * Constructor
	 *
	 * @param adapter the adapter
	 * @param messageFilter the filter of the feedback messages
	 */
	public FeedbackJsBehavior(IFeedbackMessageAdapter<M> adapter, IFeedbackMessageFilter messageFilter) {
		super();
		this.adapter = Args.notNull(adapter, "adapter");
		this.messageFilter = Args.notNull(messageFilter, "messageFilter");
	}

	@Override
	public void detach(Component component) {
		super.detach(component);
		displayedMessages = null;
	}

	/**
	 * Displays messages on ajax requests.
	 */
	@Override
	public void onEvent(Component component, IEvent<?> event) {
		super.onEvent(component, event);
		if (event.getPayload() instanceof AjaxRequestTarget target) {
			display(component, target::appendJavaScript);
		}
	}

	/**
	 * Displays messages on page renderings.
	 */
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		adapter.renderHead(response);
		display(component, script -> response.render(OnDomReadyHeaderItem.forScript(script)));
	}

	private void display(Component component, Consumer<String> renderer) {
		final List<FeedbackMessage> feedbackMessages = getFeedbackMessages(component);
		if (feedbackMessages.isEmpty()) {
			return;
		}
		final String script = getScriptForDisplay(feedbackMessages);
		if (!script.isEmpty()) {
			renderer.accept(script);
		}
	}

	/**
	 * Gets the feedback messages that are not displayed yet.
	 *
	 * @param component the component this behavior is bound to
	 * @return the feedback messages
	 */
	protected final List<FeedbackMessage> getFeedbackMessages(Component component) {
		final List<FeedbackMessage> feedbackMessages = newFeedbackMessagesModel(component, messageFilter).getObject();
		if (displayedMessages == null) {
			return feedbackMessages;
		}
		return feedbackMessages.stream()
				.filter(feedbackMessage -> !displayedMessages.contains(feedbackMessage))
				.toList();
	}

	/**
	 * Creates the model of feedback messages.
	 *
	 * @param component the component this behavior is bound to
	 * @param messageFilter the filter of the feedback messages
	 * @return new model
	 */
	protected FeedbackMessagesModel newFeedbackMessagesModel(Component component,
			IFeedbackMessageFilter messageFilter) {
		return new FeedbackMessagesModel(component.getPage(), messageFilter);
	}

	/**
	 * Gets the script that displays the feedback messages.
	 *
	 * @param feedbackMessages the feedback messages. Contains at least one feedback message.
	 * @return the script. If there is nothing to display, an empty string.
	 */
	protected String getScriptForDisplay(List<FeedbackMessage> feedbackMessages) {
		final List<M> messages = new ArrayList<>();
		for (FeedbackMessage feedbackMessage : feedbackMessages) {
			adapter.toMessage(feedbackMessage).ifPresent(message -> {
				markRendered(feedbackMessage);
				addDisplayedMessage(feedbackMessage);
				messages.add(message);
			});
		}
		if (messages.isEmpty()) {
			return "";
		}
		return adapter.toBatchScript(messageCombiner.combine(messages)).toString();
	}

	private void addDisplayedMessage(FeedbackMessage feedbackMessage) {
		if (displayedMessages == null) {
			displayedMessages = Collections.newSetFromMap(new IdentityHashMap<>());
		}
		displayedMessages.add(feedbackMessage);
	}

	/**
	 * Marks the feedback message as rendered.
	 *
	 * @param feedbackMessage the feedback message to be displayed
	 */
	protected void markRendered(FeedbackMessage feedbackMessage) {
		feedbackMessage.markRendered();
	}

	/**
	 * Sets the filter of the feedback messages.
	 *
	 * @param messageFilter the filter
	 * @return this
	 */
	public FeedbackJsBehavior<M> setMessageFilter(IFeedbackMessageFilter messageFilter) {
		this.messageFilter = Args.notNull(messageFilter, "messageFilter");
		return this;
	}

	/**
	 * Sets the combiner.
	 *
	 * @param messageCombiner the combiner
	 * @return this
	 */
	public FeedbackJsBehavior<M> setMessageCombiner(IMessageCombiner<M> messageCombiner) {
		this.messageCombiner = Args.notNull(messageCombiner, "messageCombiner");
		return this;
	}

}
