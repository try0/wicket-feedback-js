package jp.try0.wicket.feedback.js.core.js;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.wicket.util.lang.Args;

/**
 * Options of a JavaScript library.<br>
 * Holds any keys, so that it can be used without defining a class for each library.
 *
 * @author try0
 *
 */
public class JsOptions implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates empty options.
	 *
	 * @return new options
	 */
	public static JsOptions create() {
		return new JsOptions();
	}

	private final Map<String, Serializable> options = new LinkedHashMap<>();

	/**
	 * Sets an option value. Converted by {@link Js#literal(Object)}.
	 *
	 * @param key the option key
	 * @param value the option value. If null, the key is removed.
	 * @return this
	 */
	public JsOptions set(String key, Serializable value) {
		Args.notNull(key, "key");
		if (value == null) {
			options.remove(key);
		} else {
			options.put(key, value);
		}
		return this;
	}

	/**
	 * Sets plain text to an option that the library inserts as HTML. Converted by {@link Js#html(CharSequence)}.
	 *
	 * @param key the option key
	 * @param text the plain text. If null, the key is removed.
	 * @return this
	 */
	public JsOptions setHtml(String key, CharSequence text) {
		return set(key, text == null ? null : Js.expression(Js.html(text)));
	}

	/**
	 * Gets an option value.
	 *
	 * @param key the option key
	 * @return the option value
	 */
	public Optional<Serializable> get(String key) {
		return Optional.ofNullable(options.get(key));
	}

	/**
	 * Whether there are no options.
	 *
	 * @return true if there are no options
	 */
	public boolean isEmpty() {
		return options.isEmpty();
	}

	/**
	 * Creates merged options. Neither this nor the argument is changed.
	 *
	 * @param priority the options that take priority over this
	 * @return new options
	 */
	public JsOptions merge(JsOptions priority) {
		final JsOptions merged = new JsOptions();
		merged.options.putAll(options);
		merged.options.putAll(priority.options);
		return merged;
	}

	/**
	 * Gets the JavaScript object literal.
	 */
	@Override
	public String toString() {
		return Js.literal(options);
	}

}
