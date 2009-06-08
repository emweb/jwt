package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A validator is used to validate user input according to pre-defined rules
 * 
 * 
 * A validator may be associated with a form widget using
 * {@link WFormWidget#setValidator(WValidator validator)}.
 * <p>
 * The validator validates the user input. A validator may have a split
 * implementation to provide both validation at the client-side (which gives
 * instant feed-back to the user while editing), and server-side validation (to
 * be sure that the client was not tampered with). The feed-back given by
 * (client-side and server-side) validation is reflected in the style class of
 * the form field: a style class of <code>Wt-invalid</code> is set for a field
 * that is invalid.
 * <p>
 * This WValidator only checks that mandatory fields are not empty. This class
 * is reimplemented in {@link WDateValidator}, {@link WIntValidator},
 * {@link WDoubleValidator}, {@link WLengthValidator} and
 * {@link WRegExpValidator}. All these validators provibe both client-side and
 * server-side validation.
 * <p>
 * If these validators are not suitable, you can inherit from this class, and
 * provide a suitable implementation to {@link WValidator#validate(String input)}
 * which does the server-side validation. If you want to provide client-side
 * validation for your own validator, you may also reimplement
 * {@link WValidator#javaScriptValidate(String jsRef)}.
 * <p>
 * 
 * @see WFormWidget
 */
public class WValidator extends WObject {
	/**
	 * The state in which validated input can exist.
	 */
	public enum State {
		/**
		 * The input is invalid.
		 */
		Invalid,
		/**
		 * The input is invalid (emtpy and mandatory).
		 */
		InvalidEmpty,
		/**
		 * The input is valid.
		 */
		Valid;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a new validator.
	 */
	public WValidator(WObject parent) {
		super(parent);
		this.mandatory_ = false;
		this.mandatoryText_ = new WString();
		this.formWidgets_ = new ArrayList<WFormWidget>();
	}

	public WValidator() {
		this((WObject) null);
	}

	/**
	 * Create a new validator.
	 * 
	 * Indicate whether input is mandatory.
	 * <p>
	 * 
	 * @see WValidator#setMandatory(boolean mandatory)
	 */
	public WValidator(boolean mandatory, WObject parent) {
		super(parent);
		this.mandatory_ = mandatory;
		this.mandatoryText_ = new WString();
		this.formWidgets_ = new ArrayList<WFormWidget>();
	}

	public WValidator(boolean mandatory) {
		this(mandatory, (WObject) null);
	}

	/**
	 * Destructor.
	 * 
	 * The validator automatically removes itself from all formfields to which
	 * it was associated.
	 */
	public void destroy() {
		for (int i = this.formWidgets_.size() - 1; i >= 0; --i) {
			this.formWidgets_.get(i).setValidator((WValidator) null);
		}
	}

	/**
	 * Set if input is mandatory.
	 * 
	 * When an input is not mandatory, then an empty field is always valid.
	 */
	public void setMandatory(boolean mandatory) {
		if (this.mandatory_ != mandatory) {
			this.mandatory_ = mandatory;
			this.repaint();
		}
	}

	/**
	 * Returns if input is mandatory.
	 */
	public boolean isMandatory() {
		return this.mandatory_;
	}

	/**
	 * Set message to display when a mandatory field is left blank.
	 * 
	 * The default value is &quot;This field cannot be empty&quot;.
	 */
	public void setInvalidBlankText(CharSequence text) {
		this.mandatoryText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when a mandatory field is left blank.
	 * 
	 * @see WValidator#setInvalidBlankText(CharSequence text)
	 */
	public WString getInvalidBlankText() {
		if (!(this.mandatoryText_.length() == 0)) {
			return this.mandatoryText_;
		} else {
			return new WString("This field cannot be empty");
		}
	}

	/**
	 * This function attempts to change input to be valid according to the
	 * validator&apos;s rules.
	 * 
	 * In general the function needs not to change the input into a valid input.
	 * The default implementation does nothing. But it may help the user in
	 * getting its input right.
	 */
	public void fixup(CharSequence input) {
	}

	/**
	 * Validate the given input.
	 * 
	 * This function returns the current validation state of the input. The
	 * default implementation only checks whether a mandatory field is not left
	 * blank.
	 */
	public WValidator.State validate(String input) {
		if (this.isMandatory()) {
			if (input.length() == 0) {
				return WValidator.State.InvalidEmpty;
			}
		}
		return WValidator.State.Valid;
	}

	// public void createExtConfig(Writer config) throws IOException;
	/**
	 * Creates a Javascript expression that validates the input.
	 * 
	 * The JavaScript expression should evaluate to an object with two fields: a
	 * boolean <i>valid</i>, and a <i>message</i> that indicates the problem if
	 * not valid.
	 * <p>
	 * Return an empty string if you are not provide the client-side validation.
	 * <p>
	 * 
	 * @see WValidator#getInputFilter()
	 */
	protected String javaScriptValidate(String jsRef) {
		if (!this.mandatory_) {
			return "{valid:true}";
		} else {
			return "function(e,t){var v=e.value.length!=0;return {valid:v,message:t};}("
					+ jsRef
					+ ","
					+ this.getInvalidBlankText().getJsStringLiteral() + ")";
		}
	}

	/**
	 * Returns a regular expression that filters input.
	 * 
	 * The returned regular expression is used to filter keys presses. The
	 * regular expression should accept valid single characters.
	 * <p>
	 * For details on valid regular expressions, see {@link WRegExpValidator}.
	 * As an example, &quot;[0-9]&quot; would only accept numbers as valid
	 * input.
	 * <p>
	 * The default implementation returns an empty string, which does not filter
	 * any input.
	 * <p>
	 * 
	 * @see WValidator#javaScriptValidate(String jsRef)
	 */
	protected String getInputFilter() {
		return "";
	}

	protected void repaint() {
		for (int i = 0; i < this.formWidgets_.size(); ++i) {
			this.formWidgets_.get(i).validatorChanged();
		}
	}

	private boolean mandatory_;
	private WString mandatoryText_;
	private List<WFormWidget> formWidgets_;

	void addFormWidget(WFormWidget w) {
		this.formWidgets_.add(w);
	}

	void removeFormWidget(WFormWidget w) {
		this.formWidgets_.remove(w);
	}
}
