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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A validator that validates integer user input.
 * <p>
 * 
 * This validator checks whether user input is an integer number in a
 * pre-defined range.
 * <p>
 * <h3>i18n</h3>
 * <p>
 * The strings used in this class can be translated by overriding the default
 * values for the following localization keys:
 * <ul>
 * <li>Wt.WIntValidator.NotAnInteger: Must be an integer number</li>
 * <li>Wt.WIntValidator.TooSmall: The number must be larger than {1}</li>
 * <li>Wt.WIntValidator.BadRange: The number must be in the range {1} to {2}</li>
 * <li>Wt.WIntValidator.TooLarge: The number must be smaller than {1}</li>
 * </ul>
 */
public class WIntValidator extends WValidator {
	private static Logger logger = LoggerFactory.getLogger(WIntValidator.class);

	/**
	 * Creates a new integer validator that accepts any integer.
	 * <p>
	 * The validator will accept numbers using the current locale&apos;s format.
	 * <p>
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
	 * Creates a new integer validator that accepts any integer.
	 * <p>
	 * Calls {@link #WIntValidator(WObject parent) this((WObject)null)}
	 */
	public WIntValidator() {
		this((WObject) null);
	}

	/**
	 * Creates a new integer validator that accepts integer input within the
	 * given range.
	 * <p>
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
	 * Creates a new integer validator that accepts integer input within the
	 * given range.
	 * <p>
	 * Calls {@link #WIntValidator(int bottom, int top, WObject parent)
	 * this(bottom, top, (WObject)null)}
	 */
	public WIntValidator(int bottom, int top) {
		this(bottom, top, (WObject) null);
	}

	/**
	 * Returns the bottom of the valid integer range.
	 */
	public int getBottom() {
		return this.bottom_;
	}

	/**
	 * Sets the bottom of the valid integer range.
	 * <p>
	 * The default value is the minimum integer value.
	 */
	public void setBottom(int bottom) {
		if (bottom != this.bottom_) {
			this.bottom_ = bottom;
			this.repaint();
		}
	}

	/**
	 * Returns the top of the valid integer range.
	 */
	public int getTop() {
		return this.top_;
	}

	/**
	 * Sets the top of the valid integer range.
	 * <p>
	 * The default value is the maximum integer value.
	 */
	public void setTop(int top) {
		if (top != this.top_) {
			this.top_ = top;
			this.repaint();
		}
	}

	/**
	 * Sets the range of valid integers.
	 */
	public void setRange(int bottom, int top) {
		this.setBottom(bottom);
		this.setTop(top);
	}

	/**
	 * Validates the given input.
	 * <p>
	 * The input is considered valid only when it is blank for a non-mandatory
	 * field, or represents an integer within the valid range.
	 */
	public WValidator.Result validate(String input) {
		if (input.length() == 0) {
			return super.validate(input);
		}
		String text = input;
		try {
			int i = LocaleUtils.toInt(LocaleUtils.getCurrentLocale(), text);
			if (i < this.bottom_) {
				return new WValidator.Result(WValidator.State.Invalid, this
						.getInvalidTooSmallText());
			} else {
				if (i > this.top_) {
					return new WValidator.Result(WValidator.State.Invalid, this
							.getInvalidTooLargeText());
				} else {
					return new WValidator.Result(WValidator.State.Valid);
				}
			}
		} catch (NumberFormatException e) {
			return new WValidator.Result(WValidator.State.Invalid, this
					.getInvalidNotANumberText());
		}
	}

	// public void createExtConfig(Writer config) throws IOException;
	/**
	 * Sets the message to display when the input is not a number.
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
			return WString.tr("Wt.WIntValidator.NotAnInteger");
		}
	}

	/**
	 * Sets the message to display when the number is too small.
	 * <p>
	 * Depending on whether {@link WIntValidator#getBottom() getBottom()} and
	 * {@link WIntValidator#getTop() getTop()} are real bounds, the default
	 * message is &quot;The number must be between {1} and {2}&quot; or
	 * &quot;The number must be larger than {1}&quot;.
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
					return WString.tr("Wt.WIntValidator.TooSmall").arg(
							this.bottom_);
				} else {
					return WString.tr("Wt.WIntValidator.BadRange").arg(
							this.bottom_).arg(this.top_);
				}
			}
		}
	}

	/**
	 * Sets the message to display when the number is too large.
	 * <p>
	 * Depending on whether {@link WIntValidator#getBottom() getBottom()} and
	 * {@link WIntValidator#getTop() getTop()} are real bounds, the default
	 * message is &quot;The number must be between {1} and {2}&quot; or
	 * &quot;The number must be smaller than {2}&quot;.
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
					return WString.tr("Wt.WIntValidator.TooLarge").arg(
							this.top_);
				} else {
					return WString.tr("Wt.WIntValidator.BadRange").arg(
							this.bottom_).arg(this.top_);
				}
			}
		}
	}

	public String getJavaScriptValidate() {
		loadJavaScript(WApplication.getInstance());
		StringBuilder js = new StringBuilder();
		js.append("new Wt3_3_1.WIntValidator(").append(this.isMandatory())
				.append(',');
		if (this.bottom_ != Integer.MIN_VALUE) {
			js.append(this.bottom_);
		} else {
			js.append("null");
		}
		js.append(',');
		if (this.top_ != Integer.MAX_VALUE) {
			js.append(this.top_);
		} else {
			js.append("null");
		}
		js.append(",").append(
				WWebWidget.jsStringLiteral(LocaleUtils
						.getGroupSeparator(LocaleUtils.getCurrentLocale())))
				.append(',').append(
						WString.toWString(this.getInvalidBlankText())
								.getJsStringLiteral()).append(',').append(
						WString.toWString(this.getInvalidNotANumberText())
								.getJsStringLiteral()).append(',').append(
						WString.toWString(this.getInvalidTooSmallText())
								.getJsStringLiteral()).append(',').append(
						WString.toWString(this.getInvalidTooLargeText())
								.getJsStringLiteral()).append(");");
		return js.toString();
	}

	public String getInputFilter() {
		return "[-+0-9]";
	}

	private int bottom_;
	private int top_;
	private WString tooSmallText_;
	private WString tooLargeText_;
	private WString nanText_;

	private static void loadJavaScript(WApplication app) {
		app.loadJavaScript("js/WIntValidator.js", wtjs1());
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WIntValidator",
				"function(e,b,c,d,f,g,h,i){this.validate=function(a){a=String(a);if(a.length==0)return e?{valid:false,message:f}:{valid:true};if(d!=\"\")a=a.replace(d,\"\");a=Number(a);if(isNaN(a)||Math.round(a)!=a)return{valid:false,message:g};if(b!==null)if(a<b)return{valid:false,message:h};if(c!==null)if(a>c)return{valid:false,message:i};return{valid:true}}}");
	}
}
