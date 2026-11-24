package jp.try0.wicket.feedback.js.core.js;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * {@link Js} tests.
 *
 * @author try0
 *
 */
public class JsTest {

	@Test
	public void string_quotesPlainText() {
		assertEquals("\"message\"", Js.string("message"));
	}

	@Test
	public void string_nullBecomesNullLiteral() {
		assertEquals("null", Js.string(null));
	}

	@Test
	public void string_escapesQuoteAndBackslash() {
		assertEquals("\"a\\\"b\\\\c\"", Js.string("a\"b\\c"));
	}

	@Test
	public void string_escapesControlCharacters() {
		assertEquals("\"\\r\\n\\t\\u0000\\u001F\"", Js.string("\r\n\t\0\037"));
	}

	/**
	 * Not allowed in XML. Breaks ajax responses.
	 */
	@Test
	public void string_escapesNonCharacters() {
		assertEquals("\"\\uFFFE\\uFFFF\"", Js.string(String.valueOf((char) 0xFFFE) + (char) 0xFFFF));
	}

	@Test
	public void string_escapesJavaScriptLineTerminators() {
		String lineSeparator = String.valueOf((char) 0x2028);
		String paragraphSeparator = String.valueOf((char) 0x2029);

		assertEquals("\"\\u2028\\u2029\"", Js.string(lineSeparator + paragraphSeparator));
	}

	@ParameterizedTest
	@CsvSource({
			"</script>,\\u003C/script>",
			"</SCRIPT>,\\u003C/SCRIPT>",
			"<script>,\\u003Cscript>",
			"<!--,\\u003C!--",
	})
	public void string_escapesSequencesThatChangeScriptElementParsing(String text, String expected) {
		assertEquals("\"" + expected + "\"", Js.string(text));
	}

	/**
	 * Wicket parses inline scripts of ajax responses as XML, and wraps them in a CDATA section.
	 */
	@Test
	public void string_escapesEndOfCdataSection() {
		assertEquals("\"]]\\u003E\"", Js.string("]]>"));
		assertEquals("\"]]]\\u003E>\"", Js.string("]]]>>"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "<br>", "<b>bold</b>", "1 < 2", "2 > 1", "a[b]>c", "日本語", "it's" })
	public void string_keepsHarmlessText(String text) {
		assertEquals("\"" + text + "\"", Js.string(text));
	}

	@Test
	public void html_escapesMarkup() {
		assertEquals("\"&lt;img src=x onerror=alert(1)&gt;\"", Js.html("<img src=x onerror=alert(1)>"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "a\nb", "a\r\nb", "a\rb" })
	public void html_convertsNewlineToLineBreak(String text) {
		assertEquals("\"a<br>b\"", Js.html(text));
	}

	@Test
	public void html_nullBecomesNullLiteral() {
		assertEquals("null", Js.html(null));
	}

	@Test
	public void literal_convertsScalars() {
		assertEquals("null", Js.literal(null));
		assertEquals("true", Js.literal(true));
		assertEquals("10", Js.literal(10));
		assertEquals("1.5", Js.literal(1.5));
		assertEquals("\"te\\\"xt\"", Js.literal("te\"xt"));
	}

	@Test
	public void literal_keepsExpressionAsIs() {
		assertEquals("function() { go(\"x\"); }", Js.literal(Js.expression("function() { go(\"x\"); }")));
	}

	@Test
	public void literal_convertsCollections() {
		assertEquals("[\"a\",1,null]", Js.literal(Arrays.asList("a", 1, null)));
		assertEquals("[\"a\",\"b\"]", Js.literal(new String[] { "a", "b" }));
		assertEquals("[1,2]", Js.literal(new int[] { 1, 2 }));
	}

	@Test
	public void literal_convertsMapToObject() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("key", "value");
		map.put("ke\"y", List.of(true));

		assertEquals("{\"key\":\"value\",\"ke\\\"y\":[true]}", Js.literal(map));
	}

	@Test
	public void literal_quotesUnknownTypes() {
		Object value = new Object() {
			@Override
			public String toString() {
				return "\");alert(1);(\"";
			}
		};

		assertEquals("\"\\\");alert(1);(\\\"\"", Js.literal(value));
	}

}
