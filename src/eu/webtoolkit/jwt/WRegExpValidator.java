/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.regex.Pattern;
import eu.webtoolkit.jwt.utils.StringUtils;

/**
 * A validator that checks user input against a regular expression
 * <p>
 * 
 * This validator checks whether user input is matched by the given (perl-like)
 * regular expression.
 * <p>
 * The following perl features are not supported (since client-side validation
 * cannot handle them):
 * <ul>
 * <li>
 * No Lookbehind support, i.e. the constructs (?&lt;=text) and (?&lt;!text).</li>
 * <li>
 * No atomic grouping, i.e. the construct (?&gt;group).</li>
 * <li>
 * No conditional expressions, i.e. the consturct (?ifthen|else).</li>
 * </ul>
 * <p>
 * Usage example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * WLineEdit lineEdit = new WLineEdit(this);
 * // an email address validator
 * WRegExpValidator validator = new WRegExpValidator(
 * 		&quot;[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}&quot;);
 * lineEdit.setValidator(validator);
 * lineEdit.setText(&quot;pieter@emweb.be&quot;);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * <p>
 * <i><b>Note:</b>This validator does not fully support unicode: it matches on
 * the UTF8-encoded representation of the string. </i>
 * </p>
 */
public class WRegExpValidator extends WValidator {
	/**
	 * Create a new regular expression validator.
	 */
	public WRegExpValidator(WObject parent) {
		super(parent);
		this.regexp_ = null;
		this.noMatchText_ = new WString();
	}

	/**
	 * Create a new regular expression validator.
	 * <p>
	 * Calls {@link #WRegExpValidator(WObject parent) this((WObject)null)}
	 */
	public WRegExpValidator() {
		this((WObject) null);
	}

	/**
	 * Create a new regular expression validator that accepts input that matches
	 * the given regular expression.
	 * <p>
	 * This constructs a validator that matches the perl regular expression
	 * <i>expr</i>.
	 */
	public WRegExpValidator(String pattern, WObject parent) {
		super(parent);
		this.regexp_ = Pattern.compile(pattern);
		this.noMatchText_ = new WString();
	}

	/**
	 * Create a new regular expression validator that accepts input that matches
	 * the given regular expression.
	 * <p>
	 * Calls {@link #WRegExpValidator(String pattern, WObject parent)
	 * this(pattern, (WObject)null)}
	 */
	public WRegExpValidator(String pattern) {
		this(pattern, (WObject) null);
	}

	/**
	 * Delete the regexp validator.
	 */
	public void destroy() {
		/* delete this.regexp_ */;
	}

	/**
	 * Set the regular expression for valid input.
	 * <p>
	 * Sets the perl regular expression <i>expr</i>.
	 */
	public void setRegExp(String pattern) {
		if (!(this.regexp_ != null)) {
			this.regexp_ = Pattern.compile(pattern);
		} else {
			this.regexp_ = Pattern.compile(pattern);
		}
		this.repaint();
	}

	public String getRegExp() {
		return this.regexp_ != null ? this.regexp_.pattern() : "";
	}

	/**
	 * Validate the given input.
	 * <p>
	 * The input is considered valid only when it is blank for a non-mandatory
	 * field, or matches the regular expression.
	 */
	public WValidator.State validate(String input) {
		if (this.isMandatory()) {
			if (input.length() == 0) {
				return WValidator.State.InvalidEmpty;
			}
		} else {
			if (input.length() == 0) {
				return WValidator.State.Valid;
			}
		}
		if (!(this.regexp_ != null) || this.regexp_.matcher(input).matches()) {
			return WValidator.State.Valid;
		} else {
			return WValidator.State.Invalid;
		}
	}

	// public void createExtConfig(Writer config) throws IOException;
	public void setNoMatchText(CharSequence text) {
		this.setInvalidNoMatchText(text);
	}

	/**
	 * Set the message to display when the input does not match.
	 * <p>
	 * The default value is &quot;Invalid input&quot;.
	 */
	public void setInvalidNoMatchText(CharSequence text) {
		this.noMatchText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when the input does not match.
	 * <p>
	 * 
	 * @see WRegExpValidator#setInvalidNoMatchText(CharSequence text)
	 */
	public WString getInvalidNoMatchText() {
		if (!(this.noMatchText_.length() == 0)) {
			return this.noMatchText_;
		} else {
			return new WString("Invalid input");
		}
	}

	protected String javaScriptValidate(String jsRef) {
		String js = "function(e,te,tn){if(e.value.length==0)";
		if (this.isMandatory()) {
			js += "return {valid:false,message:te};";
		} else {
			js += "return {valid:true};";
		}
		if (this.regexp_ != null) {
			String s = this.regexp_.pattern();
			StringUtils.replace(s, '/', "\\/");
			js += "var r=/^" + s
					+ "$/; return {valid:r.test(e.value),message:tn};";
		} else {
			js += "return {valid:true};";
		}
		js += "}(" + jsRef + ','
				+ this.getInvalidBlankText().getJsStringLiteral() + ','
				+ this.getInvalidNoMatchText().getJsStringLiteral() + ')';
		return js;
	}

	private Pattern regexp_;
	private WString noMatchText_;
}
