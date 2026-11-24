package jp.try0.wicket.feedback.js.core.config;

import java.util.function.Function;

import org.apache.wicket.Application;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.util.lang.Args;

import jp.try0.wicket.feedback.js.core.adapter.IFeedbackMessageAdapter;
import jp.try0.wicket.feedback.js.core.behavior.FeedbackJsBehavior;
import jp.try0.wicket.feedback.js.core.combine.IMessageCombiner;

/**
 * Setting of an adapter for an application.<br>
 * An application can have multiple settings, such as toasts for information and modals for errors.
 *
 * @author try0
 *
 * @param <M> type of the messages of the adapter
 */
public final class FeedbackJsSetting<M> {

	/**
	 * Initializer of {@link FeedbackJsSetting}.
	 *
	 * @author try0
	 *
	 * @param <M> type of the messages of the adapter
	 */
	public static final class Initializer<M> {

		private final Application application;
		private final IFeedbackMessageAdapter<M> adapter;
		private IFeedbackMessageFilter messageFilter = IFeedbackMessageFilter.ALL;
		private IMessageCombiner<M> messageCombiner = IMessageCombiner.none();
		private boolean needAutoAppendBehavior = false;
		private Function<IFeedbackMessageAdapter<M>, FeedbackJsBehavior<M>> behaviorFactory = FeedbackJsBehavior::new;

		private Initializer(Application application, IFeedbackMessageAdapter<M> adapter) {
			this.application = Args.notNull(application, "application");
			this.adapter = Args.notNull(adapter, "adapter");
		}

		/**
		 * Sets the filter of the feedback messages.
		 *
		 * @param messageFilter the filter
		 * @return this
		 */
		public Initializer<M> setMessageFilter(IFeedbackMessageFilter messageFilter) {
			this.messageFilter = Args.notNull(messageFilter, "messageFilter");
			return this;
		}

		/**
		 * Sets the combiner.
		 *
		 * @param messageCombiner the combiner
		 * @return this
		 */
		public Initializer<M> setMessageCombiner(IMessageCombiner<M> messageCombiner) {
			this.messageCombiner = Args.notNull(messageCombiner, "messageCombiner");
			return this;
		}

		/**
		 * Sets whether to add the behavior to all pages.
		 *
		 * @param needAutoAppendBehavior if true, the behavior is added to all pages
		 * @return this
		 */
		public Initializer<M> setAutoAppendBehavior(boolean needAutoAppendBehavior) {
			this.needAutoAppendBehavior = needAutoAppendBehavior;
			return this;
		}

		/**
		 * Sets the factory of the behavior.
		 *
		 * @param behaviorFactory the factory
		 * @return this
		 */
		public Initializer<M> setBehaviorFactory(
				Function<IFeedbackMessageAdapter<M>, FeedbackJsBehavior<M>> behaviorFactory) {
			this.behaviorFactory = Args.notNull(behaviorFactory, "behaviorFactory");
			return this;
		}

		/**
		 * Applies the setting to the application.
		 *
		 * @return the setting
		 */
		public FeedbackJsSetting<M> initialize() {
			final FeedbackJsSetting<M> setting = new FeedbackJsSetting<>(this);
			if (needAutoAppendBehavior) {
				application.getComponentInstantiationListeners()
						.add(new PageBehaviorAutoAppender(setting::newBehavior));
			}
			return setting;
		}

	}

	/**
	 * Creates an initializer.
	 *
	 * @param <M> type of the messages of the adapter
	 * @param application the application
	 * @param adapter the adapter
	 * @return new initializer
	 */
	public static <M> Initializer<M> createInitializer(Application application, IFeedbackMessageAdapter<M> adapter) {
		return new Initializer<>(application, adapter);
	}

	private final IFeedbackMessageAdapter<M> adapter;
	private final IFeedbackMessageFilter messageFilter;
	private final IMessageCombiner<M> messageCombiner;
	private final Function<IFeedbackMessageAdapter<M>, FeedbackJsBehavior<M>> behaviorFactory;

	private FeedbackJsSetting(Initializer<M> initializer) {
		this.adapter = initializer.adapter;
		this.messageFilter = initializer.messageFilter;
		this.messageCombiner = initializer.messageCombiner;
		this.behaviorFactory = initializer.behaviorFactory;
	}

	/**
	 * Gets the adapter.
	 *
	 * @return the adapter
	 */
	public IFeedbackMessageAdapter<M> getAdapter() {
		return adapter;
	}

	/**
	 * Creates a behavior based on this setting.
	 *
	 * @return new behavior
	 */
	public FeedbackJsBehavior<M> newBehavior() {
		return behaviorFactory.apply(adapter)
				.setMessageFilter(messageFilter)
				.setMessageCombiner(messageCombiner);
	}

}
