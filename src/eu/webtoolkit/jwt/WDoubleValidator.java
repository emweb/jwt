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
 * A validator for validating floating point user input.
 * <p>
 * 
 * This validator checks whether user input is a double in the pre-defined
 * range.
 * <p>
 * <h3>i18n</h3>
 * <p>
 * The strings used in this class can be translated by overriding the default
 * values for the following localization keys: Wt.WDoubleValidator.NotANumber:
 * Must be a number Wt.WDoubleValidator.TooSmall: The number must be larger than
 * {1} Wt.WDoubleValidator.BadRange: The number must be in the range {1} to {2}
 * Wt.WDoubleValidator.TooLarge: The number must be smaller than {1}
 */
public class WDoubleValidator extends WValidator {
	private static Logger logger = LoggerFactory
			.getLogger(WDoubleValidator.class);

	/**
	 * Creates a new double validator that accepts any double.
	 */
	public WDoubleValidator(WObject parent) {
		super(parent);
		this.bottom_ = -Double.MAX_VALUE;
		this.top_ = Double.MAX_VALUE;
		this.tooSmallText_ = new WString();
		this.tooLargeText_ = new WString();
		this.nanText_ = new WString();
	}

	/**
	 * Creates a new double validator that accepts any double.
	 * <p>
	 * Calls {@link #WDoubleValidator(WObject parent) this((WObject)null)}
	 */
	public WDoubleValidator() {
		this((WObject) null);
	}

	/**
	 * Creates a new double validator that accepts double within the given
	 * range.
	 */
	public WDoubleValidator(double bottom, double top, WObject parent) {
		super(parent);
		this.bottom_ = bottom;
		this.top_ = top;
		this.tooSmallText_ = new WString();
		this.tooLargeText_ = new WString();
		this.nanText_ = new WString();
	}

	/**
	 * Creates a new double validator that accepts double within the given
	 * range.
	 * <p>
	 * Calls {@link #WDoubleValidator(double bottom, double top, WObject parent)
	 * this(bottom, top, (WObject)null)}
	 */
	public WDoubleValidator(double bottom, double top) {
		this(bottom, top, (WObject) null);
	}

	/**
	 * Returns the bottom of the valid double range.
	 */
	public double getBottom() {
		return this.bottom_;
	}

	/**
	 * Sets the bottom of the valid double range.
	 * <p>
	 * The default value is the minimum double value.
	 */
	public void setBottom(double bottom) {
		if (bottom != this.bottom_) {
			this.bottom_ = bottom;
			this.repaint();
		}
	}

	/**
	 * Returns the top of the valid double range.
	 */
	public double getTop() {
		return this.top_;
	}

	/**
	 * Sets the top of the valid double range.
	 * <p>
	 * The default value is the maximum double value.
	 */
	public void setTop(double top) {
		if (top != this.top_) {
			this.top_ = top;
			this.repaint();
		}
	}

	/**
	 * Sets the range of valid doubles.
	 */
	public void setRange(double bottom, double top) {
		this.setBottom(bottom);
		this.setTop(top);
	}

	/**
	 * Validates the given input.
	 * <p>
	 * The input is considered valid only when it is blank for a non-mandatory
	 * field, or represents a double within the valid range.
	 */
	public WValidator.Result validate(String input) {
		if (input.length() == 0) {
			return super.validate(input);
		}
		String text = input;
		try {
			double i = Double.parseDouble(text);
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
	 * The default value is &quot;Must be a number.&quot;
	 */
	public void setInvalidNotANumberText(CharSequence text) {
		this.nanText_ = WString.toWString(text);
		this.repaint();
	}

	/**
	 * Returns the message displayed when the input is not a number.
	 * <p>
	 * 
	 * @see WDoubleValidator#setInvalidNotANumberText(CharSequence text)
	 */
	public WString getInvalidNotANumberText() {
		if (!(this.nanText_.length() == 0)) {
			return this.nanText_;
		} else {
			return WString.tr("Wt.WDoubleValidator.NotANumber");
		}
	}

	/**
	 * Sets the message to display when the number is too small.
	 * <p>
	 * Depending on whether {@link WDoubleValidator#getBottom() getBottom()} and
	 * {@link WDoubleValidator#getTop() getTop()} are real bounds, the default
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
	 * @see WDoubleValidator#setInvalidTooSmallText(CharSequence text)
	 */
	public WString getInvalidTooSmallText() {
		if (!(this.tooSmallText_.length() == 0)) {
			WString s = this.tooSmallText_;
			s.arg(this.bottom_).arg(this.top_);
			return s;
		} else {
			if (this.bottom_ == -Double.MAX_VALUE) {
				return new WString();
			} else {
				if (this.top_ == Double.MAX_VALUE) {
					return WString.tr("Wt.WDoubleValidator.TooSmall").arg(
							this.bottom_);
				} else {
					return WString.tr("Wt.WDoubleValidator.BadRange").arg(
							this.bottom_).arg(this.top_);
				}
			}
		}
	}

	/**
	 * Sets the message to display when the number is too large.
	 * <p>
	 * Depending on whether {@link WDoubleValidator#getBottom() getBottom()} and
	 * {@link WDoubleValidator#getTop() getTop()} are real bounds, the default
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
	 * @see WDoubleValidator#setInvalidTooLargeText(CharSequence text)
	 */
	public WString getInvalidTooLargeText() {
		if (!(this.tooLargeText_.length() == 0)) {
			WString s = this.tooLargeText_;
			s.arg(this.bottom_).arg(this.top_);
			return s;
		} else {
			if (this.top_ == Double.MAX_VALUE) {
				return new WString();
			} else {
				if (this.bottom_ == -Integer.MAX_VALUE) {
					return WString.tr("Wt.WDoubleValidator.TooLarge").arg(
							this.top_);
				} else {
					return WString.tr("Wt.WDoubleValidator.BadRange").arg(
							this.bottom_).arg(this.top_);
				}
			}
		}
	}

	public String getJavaScriptValidate() {
		loadJavaScript(WApplication.getInstance());
		StringBuilder js = new StringBuilder();
		js.append("new Wt3_2_2.WDoubleValidator(").append(
				this.isMandatory() ? "true" : "false").append(",");
		if (this.bottom_ != -Double.MAX_VALUE) {
			js.append(String.valueOf(this.bottom_));
		} else {
			js.append("null");
		}
		js.append(',');
		if (this.top_ != Double.MAX_VALUE) {
			js.append(String.valueOf(this.top_));
		} else {
			js.append("null");
		}
		js.append(',').append(
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

	private double bottom_;
	private double top_;
	private WString tooSmallText_;
	private WString tooLargeText_;
	private WString nanText_;

	private static void loadJavaScript(WApplication app) {
		app.loadJavaScript("js/WDoubleValidator.js", wtjs1());
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WDoubleValidator",
				"function(d,b,c,e,f,g,h){this.validate=function(a){if(a.length==0)return d?{valid:false,message:e}:{valid:true};a=Number(a);if(isNaN(a))return{valid:false,message:f};if(b!==null)if(a<b)return{valid:false,message:g};if(c!==null)if(a>c)return{valid:false,message:h};return{valid:true}}}");
	}
}
