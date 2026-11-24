package jp.try0.wicket.feedback.js.core.js;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@link JsOptions} tests.
 *
 * @author try0
 *
 */
public class JsOptionsTest {

	@Test
	public void toString_returnsObjectLiteralInInsertionOrder() {
		JsOptions options = JsOptions.create()
				.set("title", "a\"b")
				.set("timer", 3000)
				.set("onOpen", Js.expression("function() {}"));

		assertEquals("{\"title\":\"a\\\"b\",\"timer\":3000,\"onOpen\":function() {}}", options.toString());
	}

	@Test
	public void toString_emptyOptions() {
		assertEquals("{}", JsOptions.create().toString());
	}

	@Test
	public void set_nestsOptions() {
		JsOptions options = JsOptions.create().set("position", JsOptions.create().set("x", "right"));

		assertEquals("{\"position\":{\"x\":\"right\"}}", options.toString());
	}

	@Test
	public void setHtml_escapesMarkup() {
		JsOptions options = JsOptions.create().setHtml("message", "<b>a</b>\nb");

		assertEquals("{\"message\":\"&lt;b&gt;a&lt;/b&gt;<br>b\"}", options.toString());
	}

	@Test
	public void setHtml_nullRemovesKey() {
		JsOptions options = JsOptions.create().setHtml("message", "a").setHtml("message", null);

		assertTrue(options.isEmpty());
	}

	@Test
	public void set_nullRemovesKey() {
		JsOptions options = JsOptions.create().set("title", "title").set("title", null);

		assertTrue(options.isEmpty());
	}

	@Test
	public void merge_givesPriorityToArgument() {
		JsOptions base = JsOptions.create().set("timer", 1).set("title", "base");
		JsOptions priority = JsOptions.create().set("timer", 2);

		assertEquals("{\"timer\":2,\"title\":\"base\"}", base.merge(priority).toString());
	}

	@Test
	public void merge_doesNotChangeOperands() {
		JsOptions base = JsOptions.create().set("timer", 1);
		JsOptions priority = JsOptions.create().set("timer", 2);

		base.merge(priority).set("added", true);

		assertEquals("{\"timer\":1}", base.toString());
		assertEquals("{\"timer\":2}", priority.toString());
	}

	@Test
	public void get_returnsValue() {
		JsOptions options = JsOptions.create().set("timer", 1);

		assertEquals(1, options.get("timer").orElseThrow());
		assertFalse(options.get("unknown").isPresent());
	}

}
