package jp.try0.wicket.feedback.js.core.config;

import java.util.function.Supplier;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.util.lang.Args;

/**
 * Adds a behavior to all pages.
 *
 * @author try0
 *
 */
public class PageBehaviorAutoAppender implements IComponentInstantiationListener {

	private final Supplier<? extends Behavior> behaviorFactory;

	/**
	 * Constructor
	 *
	 * @param behaviorFactory the factory of the behavior. Called for each page.
	 */
	public PageBehaviorAutoAppender(Supplier<? extends Behavior> behaviorFactory) {
		this.behaviorFactory = Args.notNull(behaviorFactory, "behaviorFactory");
	}

	@Override
	public void onInstantiation(Component component) {
		if (component instanceof Page) {
			component.add(behaviorFactory.get());
		}
	}

}
