package jp.try0.wicket.feedback.js.core.js;

import java.io.Serializable;

import org.apache.wicket.util.lang.Args;

/**
 * A raw JavaScript expression, such as a function or a reference to a variable.<br>
 * The code is written to the page as is. Never build it from user input.
 *
 * @author try0
 *
 * @param code the JavaScript code
 */
public record JsExpression(String code) implements Serializable {

	public JsExpression {
		Args.notNull(code, "code");
	}

	/**
	 * Gets the JavaScript code.
	 */
	@Override
	public String toString() {
		return code;
	}

}
