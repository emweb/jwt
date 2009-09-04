/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A validator that checks the string length of user input
 * <p>
 * 
 * This validator checks whether user input is within the specified range of
 * accepted string lengths.
 * <p>
 * If you only want to limit the length on a line edit, you may also use
 * {@link WLineEdit#setMaxLength(int chars) setMaxLength()}.
 */
public class WLengthValidator extends WValidator {
	/**
	 * Create a length validator that accepts input of any length.
	 */
	public WLengthValidator(WObject parent) {
		super(parent);
		this.minLength_ = 0;
		this.maxLength_ = Integer.MAX_VALUE;
		this.tooLongText_ = new WString();
		this.tooShortText_ = new WString();
	}

	/**
	 * Create a length validator that accepts input of any length.
	 * <p>
	 * Calls {@link #WLengthValidator(WObject parent) this((WObject)null)}
	 */
	public WLengthValidator() {
		this((WObject) null);
	}

	/**
	 * Create a length validator that accepts input within a length range.
	 */
	public WLengthValidator(int minLength, int maxLength, WObject parent) {
		super(parent);
		this.minLength_ = minLength;
		this.maxLength_ = maxLength;
		this.tooLongText_ = new WString();
		this.tooShortText_ = new WString();
	}

	/**
	 * Create a length validator that accepts input within a length range.
	 * <p>
	 * Calls
	 * {@link #WLengthValidator(int minLength, int maxLength, WObject parent)
	 * this(minLength, maxLength, (WObject)null)}
	 */
	public WLengthValidator(int minLength, int maxLength) {
		this(minLength, maxLength, (WObject) null);
	}

	/**
	 * Set the minimum length.
	 * <p>
	 * The default value is 0.
	 */
	public void setMinimumLength(int minLength) {
		if (this.minLength_ != minLength) {
			this.minLength_ = minLength;
			this.repaint();
		}
	}

	/**
	 * Return the minimum length.
	 * <p>
	 * 
	 * @see WLengthValidator#setMinimumLength(int minLength)
	 */
	public int getMinimumLength() {
		return this.minLength_;
	}

	/**
	 * Set the maximum length.
	 * <p>
	 * The default value is std::numeric_limits&lt;int&gt;::max()
	 */
	public void setMaximumLength(int maxLength) {
		if (this.maxLength_ != maxLength) {
			this.maxLength_ = maxLength;
			this.repaint();
		}
	}

	/**
	 * Returns the maximum length.
	 * <p>
	 * 
	 * @see WLengthValidator#setMaximumLength(int maxLength)
	 */
	public int getMaximumLength() {
		return this.maxLength_;
	}

	/**
	 * Validate the given input.
	 * <p>
	 * The input is considered valid only when it is blank for a non-mandatory
	 * field, or has a length within the valid range.
	 */
	public WValidator.State validate(String input) {
		String text = input;
		if (this.isMandatory()) {
			if (text.length() == 0) {
				return WValidator.State.InvalidEmpty;
			}
		} else {
			if (text.length() == 0) {
				return WValidator.State.Valid;
			}
		}
		if ((int) text.length() >= this.minLength_
				&& (int) text.length() <= this.maxLength_) {
			return WValidator.State.Valid;
		} else {
			return WValidator.State.Invalid;
		}
	}

	// public void createExtConfig(Writer config) throws IOException;
	/**
	 * Set message to display when the input is too short.
	 * <p>
	 * Depending on whether {@link WLengthValidator#getMaximumLength()
	 * getMaximumLength()} is a real bound, the default message is &quot;The
	 * input must have a length between {1} and {2} characters&quot; or &quot;
	 * &quot;The input must be at least {1} characters&quot;.
	 */
	public void setInvalidTooShortText(CharSequence text) {
		this.tooShortText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when the input is too short.
	 * <p>
	 * 
	 * @see WLengthValidator#setInvalidTooShortText(CharSequence text)
	 */
	public WString getInvalidTooShortText() {
		if (!(this.tooShortText_.length() == 0)) {
			WString s = this.tooShortText_;
			s.arg(this.minLength_).arg(this.maxLength_);
			return s;
		} else {
			if (this.minLength_ == 0) {
				return new WString();
			} else {
				if (this.maxLength_ == Integer.MAX_VALUE) {
					return new WString("The input must be at least "
							+ String.valueOf(this.minLength_) + " characters");
				} else {
					return new WString("The input must have a length between "
							+ String.valueOf(this.minLength_) + " and "
							+ String.valueOf(this.maxLength_) + " characters");
				}
			}
		}
	}

	/**
	 * Set message to display when the input is too long.
	 * <p>
	 * Depending on whether {@link WLengthValidator#getMinimumLength()
	 * getMinimumLength()} is different from zero, the default message is
	 * &quot;The input must have a length between {1} and {2} characters&quot;
	 * or &quot; &quot;The input must be no more than {2} characters&quot;.
	 */
	public void setInvalidTooLongText(CharSequence text) {
		this.tooLongText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when the input is too long.
	 * <p>
	 * 
	 * @see WLengthValidator#setInvalidTooLongText(CharSequence text)
	 */
	public WString getInvalidTooLongText() {
		if (!(this.tooLongText_.length() == 0)) {
			WString s = this.tooLongText_;
			s.arg(this.minLength_).arg(this.maxLength_);
			return s;
		} else {
			if (this.maxLength_ == Integer.MAX_VALUE) {
				return new WString();
			} else {
				if (this.minLength_ == 0) {
					return new WString("The input must be no more than "
							+ String.valueOf(this.maxLength_) + " characters");
				} else {
					return new WString("The input must have a length between "
							+ String.valueOf(this.minLength_) + " and "
							+ String.valueOf(this.maxLength_) + " characters");
				}
			}
		}
	}

	protected String javaScriptValidate(String jsRef) {
		String js = "function(e,te,ts,tb){if(e.value.length==0)";
		if (this.isMandatory()) {
			js += "return {valid:false,message:te};";
		} else {
			js += "return {valid:true};";
		}
		if (this.minLength_ != 0) {
			js += "if(e.value.length<" + String.valueOf(this.minLength_)
					+ ") return {valid:false,message:ts};";
		}
		if (this.maxLength_ != Integer.MAX_VALUE) {
			js += "if(e.value.length>" + String.valueOf(this.maxLength_)
					+ ") return {valid:false,message:tb};";
		}
		js += "return {valid:true};}(" + jsRef + ','
				+ this.getInvalidBlankText().getJsStringLiteral() + ','
				+ this.getInvalidTooShortText().getJsStringLiteral() + ','
				+ this.getInvalidTooLongText().getJsStringLiteral() + ')';
		return js;
	}

	private int minLength_;
	private int maxLength_;
	private WString tooLongText_;
	private WString tooShortText_;
}
