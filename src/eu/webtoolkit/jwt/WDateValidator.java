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
 * A validator for date input.
 * <p>
 * 
 * This validator accepts input in the given date format, and optionally checks
 * if the date is within a given range.
 * <p>
 * The format string used for validating user input are the same as those used
 * by {@link WDate#fromString(String s) WDate#fromString()}.
 * <p>
 * <h3>i18n</h3>
 * <p>
 * The strings used in the {@link WDateValidator} can be translated by
 * overriding the default values for the following localization keys:
 * <ul>
 * <li>Wt.WDateValidator.DateTooEarly: The date must be after {1}</li>
 * <li>Wt.WDateValidator.DateTooLate: The date must be before {1}</li>
 * <li>Wt.WDateValidator.WrongDateRange: The date must be between {1} and {2}</li>
 * <li>Wt.WDateValidator.WrongFormat: Must be a date in the format
 * &apos;{1}&apos;</li>
 * </ul>
 */
public class WDateValidator extends WValidator {
	/**
	 * Creates a date validator.
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
	 * Creates a date validator.
	 * <p>
	 * Calls {@link #WDateValidator(WObject parent) this((WObject)null)}
	 */
	public WDateValidator() {
		this((WObject) null);
	}

	/**
	 * Creates a date validator.
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
	 * Creates a date validator.
	 * <p>
	 * Calls {@link #WDateValidator(WDate bottom, WDate top, WObject parent)
	 * this(bottom, top, (WObject)null)}
	 */
	public WDateValidator(WDate bottom, WDate top) {
		this(bottom, top, (WObject) null);
	}

	/**
	 * Creates a date validator.
	 * <p>
	 * The validator will accept dates in the date format <code>format</code>.
	 * <p>
	 * The syntax for <code>format</code> is as in
	 * {@link WDate#fromString(String s) WDate#fromString()}
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
	 * Creates a date validator.
	 * <p>
	 * Calls {@link #WDateValidator(String format, WObject parent) this(format,
	 * (WObject)null)}
	 */
	public WDateValidator(String format) {
		this(format, (WObject) null);
	}

	/**
	 * Creates a date validator.
	 * <p>
	 * The validator will accept only dates within the indicated range
	 * <i>bottom</i> to <i>top</i>, in the date format <code>format</code>.
	 * <p>
	 * The syntax for <code>format</code> is as in
	 * {@link WDate#fromString(String s) WDate#fromString()}
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
	 * Creates a date validator.
	 * <p>
	 * Calls
	 * {@link #WDateValidator(String format, WDate bottom, WDate top, WObject parent)
	 * this(format, bottom, top, (WObject)null)}
	 */
	public WDateValidator(String format, WDate bottom, WDate top) {
		this(format, bottom, top, (WObject) null);
	}

	/**
	 * Sets the bottom of the valid date range.
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
	 * Returns the bottom date of the valid range.
	 */
	public WDate getBottom() {
		return this.bottom_;
	}

	/**
	 * Sets the top of the valid date range.
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
	 * Returns the top date of the valid range.
	 */
	public WDate getTop() {
		return this.top_;
	}

	/**
	 * Sets the date format used to parse date strings.
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
	 * Returns the format string used to parse date strings.
	 * <p>
	 * 
	 * @see WDateValidator#setFormat(String format)
	 */
	public String getFormat() {
		return this.formats_.get(0);
	}

	/**
	 * Sets the date formats used to parse date strings.
	 */
	public void setFormats(List<String> formats) {
		this.formats_ = formats;
		this.repaint();
	}

	/**
	 * Returns the date formats used to parse date strings.
	 */
	public List<String> getFormats() {
		return this.formats_;
	}

	/**
	 * Validates the given input.
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
	 * Sets the message to display when the input is not a date.
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
			return WString.tr("Wt.WDateValidator.WrongFormat").arg(
					this.formats_.get(0));
		}
	}

	/**
	 * Sets the message to display when the date is earlier than bottom.
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
					return WString.tr("Wt.WDateValidator.DateTooEarly").arg(
							this.bottom_.toString(this.formats_.get(0)));
				} else {
					return WString.tr("Wt.WDateValidator.WrongDateRange").arg(
							this.bottom_.toString(this.formats_.get(0))).arg(
							this.top_.toString(this.formats_.get(0)));
				}
			}
		}
	}

	/**
	 * Sets the message to display when the date is later than top.
	 * <p>
	 * Depending on whether {@link WDateValidator#getBottom() getBottom()} and
	 * {@link WDateValidator#getTop() getTop()} are defined, the default message
	 * is &quot;The date must be between {1} and {2}&quot; or &quot;The date
	 * must be before {2}&quot;.
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
					return WString.tr("Wt.WDateValidator.DateTooLate").arg(
							this.top_.toString(this.formats_.get(0)));
				} else {
					return WString.tr("Wt.WDateValidator.WrongDateRange").arg(
							this.bottom_.toString(this.formats_.get(0))).arg(
							this.top_.toString(this.formats_.get(0)));
				}
			}
		}
	}

	public String getJavaScriptValidate() {
		loadJavaScript(WApplication.getInstance());
		StringBuilder js = new StringBuilder();
		js.append("new Wt3_1_10.WDateValidator(").append(
				this.isMandatory() ? "true" : "false").append(",[");
		for (int i = 0; i < this.formats_.size(); ++i) {
			WDate.RegExpInfo r = WDate.formatToRegExp(this.formats_.get(i));
			if (i != 0) {
				js.append(',');
			}
			js.append("{").append("regexp:").append(
					WWebWidget.jsStringLiteral(r.regexp)).append(',').append(
					"getMonth:function(results){").append(r.monthGetJS).append(
					";},").append("getDay:function(results){").append(
					r.dayGetJS).append(";},").append(
					"getYear:function(results){").append(r.yearGetJS).append(
					";}").append("}");
		}
		js.append("],");
		if (!(this.bottom_ == null)) {
			js.append("new Date(").append(this.bottom_.getYear()).append(',')
					.append(this.bottom_.getMonth() - 1).append(',').append(
							this.bottom_.getDay()).append(")");
		} else {
			js.append("null");
		}
		js.append(',');
		if (!(this.top_ == null)) {
			js.append("new Date(").append(this.top_.getYear()).append(',')
					.append(this.top_.getMonth() - 1).append(',').append(
							this.top_.getDay()).append(")");
		} else {
			js.append("null");
		}
		js.append(',').append(
				WString.toWString(this.getInvalidBlankText())
						.getJsStringLiteral()).append(',').append(
				WString.toWString(this.getInvalidNotADateText())
						.getJsStringLiteral()).append(',').append(
				WString.toWString(this.getInvalidTooEarlyText())
						.getJsStringLiteral()).append(',').append(
				WString.toWString(this.getInvalidTooLateText())
						.getJsStringLiteral()).append(");");
		return js.toString();
	}

	private List<String> formats_;
	private WDate bottom_;
	private WDate top_;
	private WString tooEarlyText_;
	private WString tooLateText_;
	private WString notADateText_;

	private static void loadJavaScript(WApplication app) {
		app.loadJavaScript("js/WDateValidator.js", wtjs1());
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WDateValidator",
				"function(l,i,j,k,m,f,n,o){this.validate=function(a){if(a.length==0)return l?{valid:false,message:m}:{valid:true};for(var b,c=-1,d=-1,g=-1,h=0,p=i.length;h<p;++h){var e=i[h];b=(new RegExp(\"^\"+e.regexp+\"$\")).exec(a);if(b!=null){c=e.getMonth(b);d=e.getDay(b);g=e.getYear(b);break}}if(b==null)return{valid:false,message:f};if(d<=0||d>31||c<=0||c>12)return{valid:false,message:f};a=new Date(g,c-1,d);if(a.getDate()!=d||a.getMonth()!=c-1||a.getFullYear()!= g)return{valid:false,massage:f};if(j)if(a.getTime()<j.getTime())return{valid:false,message:n};if(k)if(a.getTime()>k.getTime())return{valid:false,message:o};return{valid:true}}}");
	}
}
