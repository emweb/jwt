package eu.webtoolkit.jwt;


/**
 * A validator that validates integer user input
 * <p>
 * 
 * This validator checks whether user input is an integer number in a
 * pre-defined range.
 */
public class WIntValidator extends WValidator {
	/**
	 * Create a new integer validator that accepts any integer.
	 */
	public WIntValidator(WObject parent) {
		super(parent);
		this.bottom_ = Integer.MIN_VALUE;
		this.top_ = Integer.MAX_VALUE;
		this.tooSmallText_ = new WString();
		this.tooLargeText_ = new WString();
		this.nanText_ = new WString();
	}

	/**
	 * Create a new integer validator that accepts any integer.
	 * <p>
	 * Calls {@link #WIntValidator(WObject parent) this((WObject)null)}
	 */
	public WIntValidator() {
		this((WObject) null);
	}

	/**
	 * Create a new integer validator that accepts integer input within the
	 * given range.
	 */
	public WIntValidator(int bottom, int top, WObject parent) {
		super(parent);
		this.bottom_ = bottom;
		this.top_ = top;
		this.tooSmallText_ = new WString();
		this.tooLargeText_ = new WString();
		this.nanText_ = new WString();
	}

	/**
	 * Create a new integer validator that accepts integer input within the
	 * given range.
	 * <p>
	 * Calls {@link #WIntValidator(int bottom, int top, WObject parent)
	 * this(bottom, top, (WObject)null)}
	 */
	public WIntValidator(int bottom, int top) {
		this(bottom, top, (WObject) null);
	}

	/**
	 * Return the bottom of the valid integer range.
	 */
	public int getBottom() {
		return this.bottom_;
	}

	/**
	 * Set the bottom of the valid integer range.
	 * <p>
	 * The default value is std::numeric_limits&lt;int&gt;::min().
	 */
	public void setBottom(int bottom) {
		if (bottom != this.bottom_) {
			this.bottom_ = bottom;
			this.repaint();
		}
	}

	/**
	 * Return the top of the valid integer range.
	 */
	public int getTop() {
		return this.top_;
	}

	/**
	 * Set the top of the valid integer range.
	 * <p>
	 * The default value is std::numeric_limits&lt;int&gt;::max().
	 */
	public void setTop(int top) {
		if (top != this.top_) {
			this.top_ = top;
			this.repaint();
		}
	}

	/**
	 * Set the range of valid integers.
	 */
	public void setRange(int bottom, int top) {
		this.setBottom(bottom);
		this.setTop(top);
	}

	/**
	 * Validate the given input.
	 * <p>
	 * The input is considered valid only when it is blank for a non-mandatory
	 * field, or represents an integer within the valid range.
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
		try {
			int i = Integer.parseInt(text);
			if (i >= this.bottom_ && i <= this.top_) {
				return WValidator.State.Valid;
			} else {
				return WValidator.State.Invalid;
			}
		} catch (NumberFormatException e) {
			return WValidator.State.Invalid;
		}
	}

	// public void createExtConfig(Writer config) throws IOException;
	/**
	 * Set the message to display when the input is not a number.
	 * <p>
	 * The default value is &quot;Must be an integer number.&quot;
	 */
	public void setInvalidNotANumberText(CharSequence text) {
		this.nanText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when the input is not a number.
	 * <p>
	 * 
	 * @see WIntValidator#setInvalidNotANumberText(CharSequence text)
	 */
	public WString getInvalidNotANumberText() {
		if (!(this.nanText_.length() == 0)) {
			return this.nanText_;
		} else {
			return new WString("Must be an integer number.");
		}
	}

	/**
	 * Set message to display when the number is too small.
	 * <p>
	 * Depending on whether {@link WIntValidator#getBottom()} and
	 * {@link WIntValidator#getTop()} are real bounds, the default message is
	 * &quot;The number must be between {1} and {2}&quot; or &quot;The number
	 * must be larger than {1}&quot;.
	 */
	public void setInvalidTooSmallText(CharSequence text) {
		this.tooSmallText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when the number is too small.
	 * <p>
	 * 
	 * @see WIntValidator#setInvalidTooSmallText(CharSequence text)
	 */
	public WString getInvalidTooSmallText() {
		if (!(this.tooSmallText_.length() == 0)) {
			WString s = this.tooSmallText_;
			s.arg(this.bottom_).arg(this.top_);
			return s;
		} else {
			if (this.bottom_ == Integer.MIN_VALUE) {
				return new WString();
			} else {
				if (this.top_ == Integer.MAX_VALUE) {
					return new WString("The number must be larger than "
							+ String.valueOf(this.bottom_));
				} else {
					return new WString("The number must be in the range "
							+ String.valueOf(this.bottom_) + " to "
							+ String.valueOf(this.top_));
				}
			}
		}
	}

	/**
	 * Set message to display when the number is too large.
	 * <p>
	 * Depending on whether {@link WIntValidator#getBottom()} and
	 * {@link WIntValidator#getTop()} are real bounds, the default message is
	 * &quot;The number must be between {1} and {2}&quot; or &quot;The number
	 * must be smaller than {2}&quot;.
	 */
	public void setInvalidTooLargeText(CharSequence text) {
		this.tooLargeText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when the number is too large.
	 * <p>
	 * 
	 * @see WIntValidator#setInvalidTooLargeText(CharSequence text)
	 */
	public WString getInvalidTooLargeText() {
		if (!(this.tooLargeText_.length() == 0)) {
			WString s = this.tooLargeText_;
			s.arg(this.bottom_).arg(this.top_);
			return s;
		} else {
			if (this.top_ == Integer.MAX_VALUE) {
				return new WString();
			} else {
				if (this.bottom_ == Integer.MIN_VALUE) {
					return new WString("The number must be smaller than "
							+ String.valueOf(this.top_));
				} else {
					return new WString("The number must be in the range "
							+ String.valueOf(this.bottom_) + " to "
							+ String.valueOf(this.top_));
				}
			}
		}
	}

	protected String javaScriptValidate(String jsRef) {
		String js = "function(e,te,tn,ts,tb){if(e.value.length==0)";
		if (this.isMandatory()) {
			js += "return {valid:false,message:te};";
		} else {
			js += "return {valid:true};";
		}
		js += "var n=Number(e.value);if (isNaN(n)||(Math.round(n) != n)) return {valid:false,message:tn};";
		if (this.bottom_ != Integer.MIN_VALUE) {
			js += "if(n<" + String.valueOf(this.bottom_)
					+ ") return {valid:false,message:ts};";
		}
		if (this.top_ != Integer.MAX_VALUE) {
			js += "if(n>" + String.valueOf(this.top_)
					+ ") return {valid:false,message:tb};";
		}
		js += "return {valid:true};}(" + jsRef + ','
				+ this.getInvalidBlankText().getJsStringLiteral() + ','
				+ this.getInvalidNotANumberText().getJsStringLiteral() + ','
				+ this.getInvalidTooSmallText().getJsStringLiteral() + ','
				+ this.getInvalidTooLargeText().getJsStringLiteral() + ')';
		return js;
	}

	protected String getInputFilter() {
		return "[-+0-9]";
	}

	private int bottom_;
	private int top_;
	private WString tooSmallText_;
	private WString tooLargeText_;
	private WString nanText_;
}
