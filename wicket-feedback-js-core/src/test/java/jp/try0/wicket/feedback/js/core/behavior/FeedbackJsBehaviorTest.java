package jp.try0.wicket.feedback.js.core.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.feedback.ExactLevelFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessagesModel;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jp.try0.wicket.feedback.js.core.adapter.IFeedbackMessageAdapter;
import jp.try0.wicket.feedback.js.core.adapter.IMessageAdapter;
import jp.try0.wicket.feedback.js.core.combine.MessageCombiner;
import jp.try0.wicket.feedback.js.core.js.Js;
import jp.try0.wicket.feedback.js.core.message.Message;
import jp.try0.wicket.feedback.js.core.test.AbstractFeedbackJsTest;
import jp.try0.wicket.feedback.js.core.test.FeedbackJsTestPage;

/**
 * {@link FeedbackJsBehavior} tests.
 *
 * @author try0
 *
 */
public class FeedbackJsBehaviorTest extends AbstractFeedbackJsTest {

	private static final IMessageAdapter ADAPTER = message -> "lib." + message.getLevel().getLevelString()
			+ "(" + Js.string(message.getText()) + ");";

	private static int count(String text, String part) {
		return (text.length() - text.replace(part, "").length()) / part.length();
	}

	private String start(FeedbackJsTestPage page) {
		getWicketTester().startPage(page);
		return getWicketTester().getLastResponseAsString();
	}

	private String clickAjaxLink(FeedbackJsTestPage page) {
		getWicketTester().clickLink(page.getAjaxLink());
		return getWicketTester().getLastResponseAsString();
	}

	@Test
	public void displaysMessageOnPageRendering() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		page.error("message");

		assertTrue(start(page).contains("lib.error(\"message\");"));
	}

	@Test
	public void displaysMessageOnDomReady() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		page.error("message");

		String response = start(page);
		int domReady = response.indexOf("Wicket.Event.add(window, \"domready\"");
		assertTrue(domReady >= 0);
		assertTrue(domReady < response.indexOf("lib.error(\"message\");"));
	}

	@Test
	public void displaysMessageOnAjaxRequest() {
		FeedbackJsTestPage page = new FeedbackJsTestPage() {
			@Override
			protected void onClickAjaxLink(AjaxRequestTarget target) {
				info("message");
			}
		};
		page.add(new FeedbackJsBehavior<>(ADAPTER));

		assertFalse(start(page).contains("lib.info("));
		assertEquals(1, count(clickAjaxLink(page), "lib.info(\"message\");"));
	}

	@Test
	public void displaysSessionMessage() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		Session.get().success("message");

		assertTrue(start(page).contains("lib.success(\"message\");"));
	}

	@Test
	public void displaysMessagesOfDescendants() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		page.getContainer().warn("message");

		assertTrue(start(page).contains("lib.warning(\"message\");"));
	}

	@Test
	public void displaysAllMessagesInOrder() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		page.info("a");
		page.error("b");

		assertTrue(start(page).contains("lib.info(\"a\");lib.error(\"b\");"));
	}

	@Test
	public void displaysMessageOnlyOnce() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		page.error("message");

		assertTrue(start(page).contains("lib.error("));
		assertFalse(start(page).contains("lib.error("));
	}

	@Test
	public void rendersNoScriptWithoutMessages() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));

		assertFalse(start(page).contains("lib."));
	}

	@Test
	public void doesNotConsumeUnsupportedMessage() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		FeedbackMessage feedbackMessage = new FeedbackMessage(page, "message", FeedbackMessage.UNDEFINED);
		page.getFeedbackMessages().add(feedbackMessage);

		assertFalse(start(page).contains("lib."));
		assertFalse(feedbackMessage.isRendered());
	}

	@Test
	public void displaysMessageWithOptions() {
		IMessageAdapter adapter = message -> "lib.open(" + Js.string(message.getTitle().orElse(null))
				+ "," + message.getOptions() + ");";
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(adapter));
		Message.info("message").withTitle("title").withOption("timer", 1).show(page);

		assertTrue(start(page).contains("lib.open(\"title\",{\"timer\":1});"));
	}

	@Test
	public void usesFilter() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER,
				new ExactLevelFeedbackMessageFilter(FeedbackMessage.ERROR)));
		page.info("a");
		page.error("b");

		String response = start(page);
		assertFalse(response.contains("lib.info("));
		assertTrue(response.contains("lib.error(\"b\");"));
	}

	@Test
	public void usesFilterSetAfterBinding() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		FeedbackJsBehavior<Message> behavior = new FeedbackJsBehavior<>(ADAPTER);
		page.add(behavior);
		behavior.setMessageFilter(new ExactLevelFeedbackMessageFilter(FeedbackMessage.ERROR));
		page.info("a");

		assertFalse(start(page).contains("lib.info("));
	}

	@Test
	public void usesCombiner() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER).setMessageCombiner(new MessageCombiner().setSeparator("|")));
		page.error("a");
		page.info("b");
		page.error("c");

		assertTrue(start(page).contains("lib.error(\"a|c\");lib.info(\"b\");"));
	}

	@Test
	public void usesBatchScript() {
		IMessageAdapter adapter = new IMessageAdapter() {
			@Override
			public CharSequence toScript(Message message) {
				return "lib.open(" + Js.string(message.getText()) + ")";
			}

			@Override
			public CharSequence toBatchScript(List<Message> messages) {
				return "lib.queue(" + Js.literal(messages.stream().map(Message::getText).toList()) + ");";
			}
		};
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(adapter));
		page.info("a");
		page.info("b");

		assertTrue(start(page).contains("lib.queue([\"a\",\"b\"]);"));
	}

	@Test
	public void rendersHeadOfAdapterBeforeMessages() {
		IMessageAdapter adapter = new IMessageAdapter() {
			@Override
			public CharSequence toScript(Message message) {
				return "lib.open();";
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				response.render(OnDomReadyHeaderItem.forScript("lib.init();"));
			}
		};
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(adapter));
		page.info("message");

		String response = start(page);
		assertTrue(response.contains("lib.init();"));
		assertTrue(response.indexOf("lib.init();") < response.indexOf("lib.open();"));
	}

	@Test
	public void rendersHeadOfAdapterWithoutMessages() {
		IMessageAdapter adapter = new IMessageAdapter() {
			@Override
			public CharSequence toScript(Message message) {
				return "lib.open();";
			}

			@Override
			public void renderHead(IHeaderResponse response) {
				response.render(OnDomReadyHeaderItem.forScript("lib.init();"));
			}
		};
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(adapter));

		assertTrue(start(page).contains("lib.init();"));
	}

	/**
	 * For a library whose message is not {@link Message}.
	 */
	@Test
	public void supportsOwnMessageType() {
		IFeedbackMessageAdapter<Integer> adapter = new IFeedbackMessageAdapter<>() {
			@Override
			public Optional<Integer> toMessage(FeedbackMessage feedbackMessage) {
				return Optional.of(feedbackMessage.getLevel());
			}

			@Override
			public CharSequence toScript(Integer message) {
				return "lib.level(" + message + ");";
			}
		};
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(adapter));
		page.fatal("message");

		assertTrue(start(page).contains("lib.level(500);"));
	}

	@Test
	public void displaysMessageIfAddedBeforeComponentIsAttachedToPage() {
		FeedbackJsTestPage page = new FeedbackJsTestPage() {
			@Override
			protected WebMarkupContainer newContainer(String id) {
				WebMarkupContainer container = super.newContainer(id);
				container.add(new FeedbackJsBehavior<>(ADAPTER));
				return container;
			}
		};
		page.error("message");

		assertTrue(start(page).contains("lib.error(\"message\");"));
	}

	@Test
	public void displaysMessageOnceIfComponentIsRenderedOnAjaxRequest() {
		FeedbackJsTestPage page = new FeedbackJsTestPage() {
			@Override
			protected void onClickAjaxLink(AjaxRequestTarget target) {
				info("message");
				target.add(getContainer());
			}
		};
		page.getContainer().add(new FeedbackJsBehavior<>(ADAPTER));
		start(page);

		assertEquals(1, count(clickAjaxLink(page), "lib.info(\"message\");"));
	}

	@Test
	public void displaysMessageReportedWhileRenderingOnAjaxRequest() {
		FeedbackJsTestPage page = new FeedbackJsTestPage() {
			@Override
			protected void onClickAjaxLink(AjaxRequestTarget target) {
				info("before");
				target.add(getContainer());
			}

			@Override
			protected WebMarkupContainer newContainer(String id) {
				return new WebMarkupContainer(id) {
					@Override
					protected void onBeforeRender() {
						super.onBeforeRender();
						error("rendering");
					}
				};
			}
		};
		page.getContainer().add(new FeedbackJsBehavior<>(ADAPTER));
		start(page);

		String response = clickAjaxLink(page);
		assertEquals(1, count(response, "lib.info(\"before\");"));
		assertEquals(1, count(response, "lib.error(\"rendering\");"));
	}

	/**
	 * A FeedbackPanel marks messages as rendered before the head is rendered.
	 */
	@Test
	public void displaysMessageDisplayedByFeedbackPanel() {
		FeedbackJsTestPage page = new FeedbackJsTestPage() {
			@Override
			protected WebMarkupContainer newContainer(String id) {
				return new FeedbackPanel(id);
			}
		};
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		page.error("message");

		String response = start(page);
		assertTrue(response.contains("lib.error(\"message\");"));
		assertTrue(response.contains("feedbackPanelERROR"));
	}

	@Test
	public void behaviorsWithDifferentAdaptersCoexist() {
		IMessageAdapter modal = message -> "modal(" + Js.string(message.getText()) + ");";
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER, message -> message.getLevel() < FeedbackMessage.ERROR));
		page.add(new FeedbackJsBehavior<>(modal, message -> message.getLevel() >= FeedbackMessage.ERROR));
		page.info("a");
		page.error("b");

		String response = start(page);
		assertTrue(response.contains("lib.info(\"a\");"));
		assertTrue(response.contains("modal(\"b\");"));
		assertFalse(response.contains("lib.error("));
		assertFalse(response.contains("modal(\"a\");"));
	}

	@Test
	public void newFeedbackMessagesModel_isOverridable() {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER) {
			@Override
			protected FeedbackMessagesModel newFeedbackMessagesModel(Component component,
					IFeedbackMessageFilter filter) {
				return super.newFeedbackMessagesModel(component, filter).setIncludeSession(false);
			}
		});
		Session.get().info("session");
		page.info("page");

		String response = start(page);
		assertFalse(response.contains("lib.info(\"session\");"));
		assertTrue(response.contains("lib.info(\"page\");"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "\"); alert(1); (\"", "</script><script>alert(1)</script>", "a\nb", "]]>" })
	public void messageDoesNotBreakScript(String text) {
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(new FeedbackJsBehavior<>(ADAPTER));
		page.error(text);

		assertTrue(start(page).contains("lib.error(" + Js.string(text) + ");"));
	}

	@Test
	public void isSerializable() {
		FeedbackJsBehavior<Message> behavior = new FeedbackJsBehavior<>(ADAPTER,
				new ExactLevelFeedbackMessageFilter(FeedbackMessage.ERROR));
		behavior.setMessageCombiner(new MessageCombiner());

		FeedbackJsTestPage page = new FeedbackJsTestPage();
		page.add(WicketObjects.cloneObject(behavior));
		page.error("message");

		assertTrue(start(page).contains("lib.error(\"message\");"));
	}

	@Test
	public void markRendered_isCalledForDisplayedMessage() {
		WicketTester tester = getWicketTester();
		FeedbackJsTestPage page = new FeedbackJsTestPage();
		List<FeedbackMessage> rendered = new ArrayList<>();
		page.add(new FeedbackJsBehavior<>(ADAPTER) {
			@Override
			protected void markRendered(FeedbackMessage feedbackMessage) {
				super.markRendered(feedbackMessage);
				rendered.add(feedbackMessage);
			}
		});
		page.error("message");
		tester.startPage(page);

		assertEquals(1, rendered.size());
		assertTrue(rendered.get(0).isRendered());
	}

}
