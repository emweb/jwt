/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A validator for date input
 * <p>
 * 
 * This validator accepts input in the given date format, and optionally checks
 * if the date is within a given range.
 * <p>
 * The format string used for validating user input are the same as those used
 * by {@link WDate#fromString(String s) fromString() }.
 */
public class WDateValidator extends WValidator {
	/**
	 * Construct a date validator.
	 * <p>
	 * The validator will accept any date of the format &apos;yyyy-MM-dd&apos;.
	 */
	public WDateValidator(WObject parent) {
		super(parent);
		this.formats_ = new ArrayList<String>();
		this.bottom_ = null;
		this.top_ = null;
		this.tooEarlyText_ = new WString();
		this.tooLateText_ = new WString();
		this.notADateText_ = new WString();
		this.setFormat("yyyy-MM-dd");
	}

	/**
	 * Construct a date validator.
	 * <p>
	 * Calls {@link #WDateValidator(WObject parent) this((WObject)null)}
	 */
	public WDateValidator() {
		this((WObject) null);
	}

	/**
	 * Construct a date validator.
	 * <p>
	 * The validator will accept dates in the indicated range in the format
	 * &apos;yyyy-MM-dd&apos;.
	 */
	public WDateValidator(WDate bottom, WDate top, WObject parent) {
		super(parent);
		this.formats_ = new ArrayList<String>();
		this.bottom_ = bottom;
		this.top_ = top;
		this.tooEarlyText_ = new WString();
		this.tooLateText_ = new WString();
		this.notADateText_ = new WString();
		this.setFormat("yyyy-MM-dd");
	}

	/**
	 * Construct a date validator.
	 * <p>
	 * Calls {@link #WDateValidator(WDate bottom, WDate top, WObject parent)
	 * this(bottom, top, (WObject)null)}
	 */
	public WDateValidator(WDate bottom, WDate top) {
		this(bottom, top, (WObject) null);
	}

	/**
	 * Construct a date validator.
	 * <p>
	 * The validator will accept dates in the date format <i>format</i>.
	 * <p>
	 * The syntax for <i>format</i> is as in {@link WDate#fromString(String s)
	 * WDate#fromString() }
	 */
	public WDateValidator(String format, WObject parent) {
		super(parent);
		this.formats_ = new ArrayList<String>();
		this.bottom_ = null;
		this.top_ = null;
		this.tooEarlyText_ = new WString();
		this.tooLateText_ = new WString();
		this.notADateText_ = new WString();
		this.setFormat(format);
	}

	/**
	 * Construct a date validator.
	 * <p>
	 * Calls {@link #WDateValidator(String format, WObject parent) this(format,
	 * (WObject)null)}
	 */
	public WDateValidator(String format) {
		this(format, (WObject) null);
	}

	/**
	 * Construct a date validator.
	 * <p>
	 * The validator will accept only dates within the indicated range
	 * <i>bottom</i> to <i>top</i>, in the date format <i>format</i>.
	 * <p>
	 * The syntax for <i>format</i> is as in {@link WDate#fromString(String s)
	 * WDate#fromString() }
	 */
	public WDateValidator(String format, WDate bottom, WDate top, WObject parent) {
		super(parent);
		this.formats_ = new ArrayList<String>();
		this.bottom_ = bottom;
		this.top_ = top;
		this.tooEarlyText_ = new WString();
		this.tooLateText_ = new WString();
		this.notADateText_ = new WString();
		this.setFormat(format);
	}

	/**
	 * Construct a date validator.
	 * <p>
	 * Calls
	 * {@link #WDateValidator(String format, WDate bottom, WDate top, WObject parent)
	 * this(format, bottom, top, (WObject)null)}
	 */
	public WDateValidator(String format, WDate bottom, WDate top) {
		this(format, bottom, top, (WObject) null);
	}

	/**
	 * Set the bottom of the valid date range.
	 * <p>
	 * The default is a null date constructed using WDate().
	 */
	public void setBottom(WDate bottom) {
		if (!(this.bottom_ == bottom || (this.bottom_ != null && this.bottom_
				.equals(bottom)))) {
			this.bottom_ = bottom;
			this.repaint();
		}
	}

	/**
	 * Get the bottom date of the valid range.
	 */
	public WDate getBottom() {
		return this.bottom_;
	}

	/**
	 * Set the top of the valid date range.
	 * <p>
	 * The default is a null date constructed using WDate().
	 */
	public void setTop(WDate top) {
		if (!(this.top_ == top || (this.top_ != null && this.top_.equals(top)))) {
			this.top_ = top;
			this.repaint();
		}
	}

	/**
	 * Get the top date of the valid range.
	 */
	public WDate getTop() {
		return this.top_;
	}

	/**
	 * Set the date format used to parse date strings.
	 * <p>
	 * 
	 * @see WDate#fromString(String s)
	 */
	public void setFormat(String format) {
		this.formats_.clear();
		this.formats_.add(format);
		this.repaint();
	}

	/**
	 * Get the format string used to parse date strings.
	 * <p>
	 * 
	 * @see WDateValidator#setFormat(String format)
	 */
	public String getFormat() {
		return this.formats_.get(0);
	}

	public void setFormats(List<String> formats) {
		this.formats_ = formats;
		this.repaint();
	}

	public List<String> getFormats() {
		return this.formats_;
	}

	/**
	 * Validate the given input.
	 * <p>
	 * The input is considered valid only when it is blank for a non-mandatory
	 * field, or represents a date in the given format, and within the valid
	 * range.
	 */
	public WValidator.State validate(String input) {
		if (input.length() == 0) {
			return this.isMandatory() ? WValidator.State.InvalidEmpty
					: WValidator.State.Valid;
		}
		for (int i = 0; i < this.formats_.size(); ++i) {
			try {
				WDate d = WDate.fromString(input, this.formats_.get(i));
				if ((d != null)) {
					if (!(this.bottom_ == null)) {
						if (d.before(this.bottom_)) {
							return WValidator.State.Invalid;
						}
					}
					if (!(this.top_ == null)) {
						if (d.after(this.top_)) {
							return WValidator.State.Invalid;
						}
					}
					return WValidator.State.Valid;
				}
			} catch (RuntimeException e) {
				WApplication.getInstance().log("warn").append(
						"WDateValidator::validate(): ").append(e.toString());
			}
		}
		return WValidator.State.Invalid;
	}

	// public void createExtConfig(Writer config) throws IOException;
	/**
	 * Set the message to display when the input is not a date.
	 * <p>
	 * The default message is &quot;The date must be of the format {1}&quot;,
	 * with as first argument the format string.
	 */
	public void setInvalidNotADateText(CharSequence text) {
		this.notADateText_ = WString.toWString(text);
	}

	/**
	 * Returns the message displayed when the input is not a date.
	 * <p>
	 * 
	 * @see WDateValidator#setInvalidNotADateText(CharSequence text)
	 */
	public WString getInvalidNotADateText() {
		if (!(this.notADateText_.length() == 0)) {
			WString s = this.notADateText_;
			s.arg(this.formats_.get(0));
			return s;
		} else {
			return new WString("Must be a date in the format '").append(
					this.formats_.get(0)).append("'");
		}
	}

	/**
	 * Set message to display when the date is earlier than bottom
	 * <p>
	 * The default message is &quot;The date must be between {1} and {2}&quot;
	 * or &quot;The date must be after {1}&quot;.
	 */
	public void setInvalidTooEarlyText(CharSequence text) {
		this.tooEarlyText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when date is too early.
	 * <p>
	 * 
	 * @see WDateValidator#setInvalidTooEarlyText(CharSequence text)
	 */
	public WString getInvalidTooEarlyText() {
		if (!(this.tooEarlyText_.length() == 0)) {
			WString s = this.tooEarlyText_;
			s.arg(this.bottom_.toString(this.formats_.get(0))).arg(
					this.top_.toString(this.formats_.get(0)));
			return s;
		} else {
			if ((this.bottom_ == null)) {
				return new WString();
			} else {
				if ((this.top_ == null)) {
					return new WString("The date must be after ")
							.append(this.bottom_.toString(this.formats_.get(0)));
				} else {
					return new WString("The date must be between ").append(
							this.bottom_.toString(this.formats_.get(0)))
							.append(" and ").append(
									this.top_.toString(this.formats_.get(0)));
				}
			}
		}
	}

	/**
	 * Set message to display when the date is later than top.
	 * <p>
	 * Depending on whether {@link WDateValidator#getBottom() getBottom() } and
	 * {@link WDateValidator#getTop() getTop() } are defined, the default
	 * message is &quot;The date must be between {1} and {2}&quot; or &quot;The
	 * date must be before {2}&quot;.
	 */
	public void setInvalidTooLateText(CharSequence text) {
		this.tooLateText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when the date is too late.
	 * <p>
	 * 
	 * @see WDateValidator#setInvalidTooLateText(CharSequence text)
	 */
	public WString getInvalidTooLateText() {
		if (!(this.tooLateText_.length() == 0)) {
			WString s = this.tooLateText_;
			s.arg(this.bottom_.toString(this.formats_.get(0))).arg(
					this.top_.toString(this.formats_.get(0)));
			return s;
		} else {
			if ((this.top_ == null)) {
				return new WString();
			} else {
				if ((this.bottom_ == null)) {
					return new WString("The date must be before ")
							.append(this.top_.toString(this.formats_.get(0)));
				} else {
					return new WString("The date must be between ").append(
							this.bottom_.toString(this.formats_.get(0)))
							.append(" and ").append(
									this.top_.toString(this.formats_.get(0)));
				}
			}
		}
	}

	protected String javaScriptValidate(String jsRef) {
		StringWriter js = new StringWriter();
		js.append("function(e,te,tn,ts,tb){if(e.value.length==0)");
		if (this.isMandatory()) {
			js.append("return {valid:false,message:te};");
		} else {
			js.append("return {valid:true};");
		}
		js.append("var r,res,m=-1,d=-1,y=-1;");
		for (int i = 0; i < this.formats_.size(); ++i) {
			WDate.RegExpInfo r = WDate.formatToRegExp(this.formats_.get(i));
			js.append("r=/^").append(r.regexp).append(
					"$/;results=r.exec(e.value);if (results!=null) {m=")
					.append(r.monthGetJS).append(";d=").append(r.dayGetJS)
					.append(";y=").append(r.yearGetJS).append(";} else {");
		}
		js.append("return {valid:false,message:tn};");
		for (int i = 0; i < this.formats_.size(); ++i) {
			js.append("}");
		}
		js
				.append("if ((d<=0)||(d>31)||(m<=0)||(m>12))return {valid:false,message:tn};var dt=new Date(y,m-1,d);if (dt.getDate() != d || dt.getMonth() != m-1 || dt.getFullYear() != y) return {valid:false,massage:tn};");
		if (!(this.bottom_ == null)) {
			js.append("if(dt.getTime()<new Date(").append(
					String.valueOf(this.bottom_.getYear())).append(',').append(
					String.valueOf(this.bottom_.getMonth() - 1)).append(',')
					.append(String.valueOf(this.bottom_.getDay())).append(
							").getTime())return {valid:false,message:ts};");
		}
		if (!(this.top_ == null)) {
			js.append("if(dt.getTime()>new Date(").append(
					String.valueOf(this.top_.getYear())).append(',').append(
					String.valueOf(this.top_.getMonth() - 1)).append(',')
					.append(String.valueOf(this.top_.getDay())).append(
							").getTime())return {valid:false,message:tb};");
		}
		js.append("return {valid:true};}(").append(jsRef).append(',').append(
				this.getInvalidBlankText().getJsStringLiteral()).append(',')
				.append(this.getInvalidNotADateText().getJsStringLiteral())
				.append(',').append(
						this.getInvalidTooEarlyText().getJsStringLiteral())
				.append(',').append(
						this.getInvalidTooLateText().getJsStringLiteral())
				.append(')');
		return js.toString();
	}

	private List<String> formats_;
	private WDate bottom_;
	private WDate top_;
	private WString tooEarlyText_;
	private WString tooLateText_;
	private WString notADateText_;
}
