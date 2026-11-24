package jp.try0.wicket.feedback.js.core.js;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Converts Java values to JavaScript literals.<br>
 * Every value an adapter writes into a script should go through this class.
 *
 * @author try0
 *
 */
public final class Js {

	private static final String[] SCRIPT_ELEMENT_SEQUENCES = { "<!--", "<script", "</script" };

	private Js() {
	}

	/**
	 * Wraps JavaScript code that must not be quoted, such as a function.
	 *
	 * @param code the JavaScript code. Never build it from user input.
	 * @return the expression
	 */
	public static JsExpression expression(CharSequence code) {
		return new JsExpression(code == null ? null : code.toString());
	}

	/**
	 * Converts text to a JavaScript string literal including the quotes.<br>
	 * Use this if the library treats the value as plain text, or if the value is trusted markup.
	 *
	 * @param text the text
	 * @return the literal. If text is null, "null".
	 */
	public static String string(CharSequence text) {
		if (text == null) {
			return "null";
		}

		final String value = text.toString();
		final StringBuilder literal = new StringBuilder(value.length() + 16).append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"' -> literal.append("\\\"");
			case '\\' -> literal.append("\\\\");
			case '\n' -> literal.append("\\n");
			case '\r' -> literal.append("\\r");
			case '\t' -> literal.append("\\t");
			default -> {
				if (needsUnicodeEscape(value, i)) {
					literal.append(String.format("\\u%04X", (int) c));
				} else {
					literal.append(c);
				}
			}
			}
		}
		return literal.append('"').toString();
	}

	private static boolean needsUnicodeEscape(String value, int index) {
		final char c = value.charAt(index);
		if (c == '<') {
			// keeps the inline script element from being closed or switched to the escaped state
			for (String sequence : SCRIPT_ELEMENT_SEQUENCES) {
				if (value.regionMatches(true, index, sequence, 0, sequence.length())) {
					return true;
				}
			}
			return false;
		}
		if (c == '>') {
			// keeps the CDATA section from being closed, because wicket parses scripts of ajax responses as XML
			return value.startsWith("]]>", index - 2);
		}
		// U+2028 and U+2029 are line terminators in JavaScript. U+FFFE and U+FFFF are not allowed in XML.
		return c < 0x20 || c == 0x2028 || c == 0x2029 || c >= 0xFFFE;
	}

	/**
	 * Escapes markup, converts newlines to &lt;br&gt; and returns a JavaScript string literal including the quotes.<br>
	 * Use this if the library inserts the value as HTML.
	 *
	 * @param text the plain text
	 * @return the literal. If text is null, "null".
	 */
	public static String html(CharSequence text) {
		if (text == null) {
			return "null";
		}

		return string(text.toString()
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;")
				.replace("\r\n", "<br>")
				.replace("\r", "<br>")
				.replace("\n", "<br>"));
	}

	/**
	 * Converts a value to a JavaScript literal.
	 * <ul>
	 * <li>{@link JsExpression} and {@link JsOptions} are written as is.</li>
	 * <li>null, {@link Boolean} and {@link Number} become the corresponding literal.</li>
	 * <li>{@link Map} becomes an object, {@link Iterable} and arrays become an array.</li>
	 * <li>Everything else becomes the string literal of its {@link Object#toString()}.</li>
	 * </ul>
	 *
	 * @param value the value
	 * @return the literal
	 */
	public static String literal(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof JsExpression || value instanceof JsOptions
				|| value instanceof Boolean || value instanceof Number) {
			return value.toString();
		}
		if (value instanceof Map<?, ?> map) {
			final StringJoiner object = new StringJoiner(",", "{", "}");
			map.forEach((key, val) -> object.add(string(String.valueOf(key)) + ":" + literal(val)));
			return object.toString();
		}
		if (value instanceof Iterable<?> iterable) {
			final StringJoiner array = new StringJoiner(",", "[", "]");
			iterable.forEach(element -> array.add(literal(element)));
			return array.toString();
		}
		if (value.getClass().isArray()) {
			final StringJoiner array = new StringJoiner(",", "[", "]");
			for (int i = 0; i < Array.getLength(value); i++) {
				array.add(literal(Array.get(value, i)));
			}
			return array.toString();
		}
		return string(value.toString());
	}

}
