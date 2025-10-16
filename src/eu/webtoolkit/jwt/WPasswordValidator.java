/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A validator that checks if a password is valid.
 *
 * <p>This validator validate an input like a browser supporting the attribute maxlength, minlength,
 * required and pattern would.
 */
public class WPasswordValidator extends WRegExpValidator {
  private static Logger logger = LoggerFactory.getLogger(WPasswordValidator.class);

  /** Creates a basic password validator. */
  public WPasswordValidator() {
    super();
    this.minLength_ = 0;
    this.maxLength_ = Integer.MAX_VALUE;
    this.pattern_ = new WString();
    this.tooShortText_ = WString.tr("Wt.WPasswordValidator.TooShort");
    this.tooLongText_ = WString.tr("Wt.WPasswordValidator.TooLong");
    this.setMandatory(true);
  }
  /**
   * Specifies the minimum length of the password.
   *
   * <p>The default value is 0.
   */
  public void setMinLength(int chars) {
    this.minLength_ = chars;
    this.repaint();
  }
  /**
   * Returns the minimum length of the password.
   *
   * <p>
   *
   * @see WPasswordValidator#setMinLength(int chars)
   */
  public int getMinLength() {
    return this.minLength_;
  }
  /**
   * Sets the message to display when the password is too small.
   *
   * <p>The default value is &quot;Password too small&quot;.
   */
  public void setInvalidTooShortText(final CharSequence text) {
    this.tooShortText_ = WString.toWString(text);
    this.repaint();
  }
  /**
   * Returns the message displayed when the password is too small.
   *
   * <p>
   *
   * @see WPasswordValidator#setInvalidTooShortText(CharSequence text)
   */
  public WString getInvalidTooShortText() {
    return this.tooShortText_;
  }
  /**
   * Specifies the maximum length of the password.
   *
   * <p>The default value is std::numeric_limits&lt;int&gt;::max().
   */
  public void setMaxLength(int chars) {
    this.maxLength_ = chars;
    this.repaint();
  }
  /**
   * Returns the maximum length of the password.
   *
   * <p>
   *
   * @see WPasswordValidator#setMaxLength(int chars)
   */
  public int getMaxLength() {
    return this.maxLength_;
  }
  /**
   * Sets the message to display when the password is too long.
   *
   * <p>The default value is &quot;Password too long&quot;.
   */
  public void setInvalidTooLongText(final CharSequence text) {
    this.tooLongText_ = WString.toWString(text);
    this.repaint();
  }
  /**
   * Returns the message displayed when the password is too long.
   *
   * <p>
   *
   * @see WPasswordValidator#setInvalidTooLongText(CharSequence text)
   */
  public WString getInvalidTooLongText() {
    return this.tooLongText_;
  }

  public WValidator.Result validate(final String input) {
    if (input.length() == 0) {
      if (this.isMandatory()) {
        return new WValidator.Result(ValidationState.InvalidEmpty, this.getInvalidBlankText());
      } else {
        return new WValidator.Result(ValidationState.Valid);
      }
    }
    String text = input;
    int size = (int) text.length();
    if (size < this.minLength_ && (this.minLength_ <= this.maxLength_ || this.maxLength_ < 0)) {
      return new WValidator.Result(ValidationState.Invalid, this.getInvalidTooShortText());
    }
    if (size > this.maxLength_) {
      return new WValidator.Result(ValidationState.Invalid, this.getInvalidTooLongText());
    }
    if (this.getRegExpPattern().length() == 0) {
      return new WValidator.Result(ValidationState.Valid);
    }
    return super.validate(input);
  }

  public String getJavaScriptValidate() {
    WApplication.getInstance().loadJavaScript("js/WPasswordValidator.js", wtjs1());
    StringBuilder js = new StringBuilder();
    js.append("new Wt4_12_1.WPasswordValidator(")
        .append(this.isMandatory())
        .append(",")
        .append(this.getMinLength())
        .append(",")
        .append(this.getMaxLength())
        .append(",")
        .append(WWebWidget.jsStringLiteral(this.getRegExpPattern()))
        .append(",")
        .append(WWebWidget.jsStringLiteral(this.getInvalidBlankText()))
        .append(",")
        .append(WWebWidget.jsStringLiteral(this.getInvalidTooShortText()))
        .append(",")
        .append(WWebWidget.jsStringLiteral(this.getInvalidTooLongText()))
        .append(",")
        .append(WWebWidget.jsStringLiteral(this.getInvalidNoMatchText()))
        .append(");");
    return js.toString();
  }

  private int minLength_;
  private int maxLength_;
  private WString pattern_;
  private WString tooShortText_;
  private WString tooLongText_;

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WPasswordValidator",
        "(function(e,t,a,i,n,r,s,l){this.validate=function(d){if(0===d.length)return e?{valid:!1,message:n}:{valid:!0};if((t<a||a<0)&&t>d.length)return{valid:!1,message:r};if(a>-1&&a<d.length)return{valid:!1,message:s};if(i.length>0){if(!new RegExp(i,\"v\").test(d))return{valid:!1,message:l}}return{valid:!0}}})");
  }
}
