/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A validator is used to validate user input according to pre-defined rules.
 * <p>
 * 
 * A validator may be associated with a form widget using
 * {@link WFormWidget#setValidator(WValidator validator)
 * WFormWidget#setValidator()}.
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
 * provide a suitable implementation to {@link WValidator#validate(String input)
 * validate()} which does the server-side validation. If you want to provide
 * client-side validation for your own validator, you may also reimplement
 * {@link WValidator#getJavaScriptValidate() getJavaScriptValidate()}.
 * <p>
 * <h3>i18n</h3>
 * <p>
 * The strings used in this class can be translated by overriding the default
 * values for the following localization keys:
 * <ul>
 * <li>{@link WValidator.State#Invalid}: This field cannot be empty</li>
 * </ul>
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

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a new validator.
	 */
	public WValidator(WObject parent) {
		super(parent);
		this.mandatory_ = false;
		this.mandatoryText_ = new WString();
		this.formWidgets_ = new ArrayList<WFormWidget>();
	}

	/**
	 * Creates a new validator.
	 * <p>
	 * Calls {@link #WValidator(WObject parent) this((WObject)null)}
	 */
	public WValidator() {
		this((WObject) null);
	}

	/**
	 * Creates a new validator.
	 * <p>
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

	/**
	 * Creates a new validator.
	 * <p>
	 * Calls {@link #WValidator(boolean mandatory, WObject parent)
	 * this(mandatory, (WObject)null)}
	 */
	public WValidator(boolean mandatory) {
		this(mandatory, (WObject) null);
	}

	/**
	 * Sets if input is mandatory.
	 * <p>
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
	 * Sets the message to display when a mandatory field is left blank.
	 * <p>
	 * The default value is &quot;This field cannot be empty&quot;.
	 */
	public void setInvalidBlankText(CharSequence text) {
		this.mandatoryText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when a mandatory field is left blank.
	 * <p>
	 * 
	 * @see WValidator#setInvalidBlankText(CharSequence text)
	 */
	public WString getInvalidBlankText() {
		if (!(this.mandatoryText_.length() == 0)) {
			return this.mandatoryText_;
		} else {
			return WString.tr("Wt.WValidator.Invalid");
		}
	}

	/**
	 * This function attempts to change input to be valid according to the
	 * validator&apos;s rules.
	 * <p>
	 * In general the function needs not to change the input into a valid input.
	 * The default implementation does nothing. But it may help the user in
	 * getting its input right.
	 */
	public void fixup(CharSequence input) {
	}

	/**
	 * Validates the given input.
	 * <p>
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
	 * Creates a Javascript object that validates the input.
	 * <p>
	 * The JavaScript expression should evaluate to an object which contains a
	 * <code>validate(text)</code> function, which returns an object that
	 * contains the following two fields:
	 * <ul>
	 * <li>fields: a boolean <i>valid</i>,</li>
	 * <li>a <code>message</code> that indicates the problem if not valid.</li>
	 * </ul>
	 * <p>
	 * Return an empty string if you are not provide the client-side validation.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The signature and contract changed changed in JWt
	 * 3.1.9.</i>
	 * </p>
	 * 
	 * @see WValidator#getInputFilter()
	 */
	public String getJavaScriptValidate() {
		if (this.mandatory_) {
			return "new (function() {this.validate = function(text) {return { valid: text.length != 0, message: "
					+ WString.toWString(this.getInvalidBlankText())
							.getJsStringLiteral() + "}};})();";
		} else {
			return "new (function() {this.validate = function(text) {return { valid: true }};})();";
		}
	}

	/**
	 * Returns a regular expression that filters input.
	 * <p>
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
	 * @see WValidator#getJavaScriptValidate()
	 */
	public String getInputFilter() {
		return "";
	}

	void repaint() {
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
