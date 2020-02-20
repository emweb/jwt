/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A validator that checks user input against a regular expression.
 *
 * <p>This validator checks whether user input matches the given (perl-like) regular expression. It
 * checks the complete input; prefix ^ and suffix $ are not needed.
 *
 * <p>The following perl features are not supported (since client-side validation cannot handle
 * them):
 *
 * <ul>
 *   <li>No Lookbehind support, i.e. the constructs (?&lt;=text) and (?&lt;!text).
 *   <li>No atomic grouping, i.e. the construct (?&gt;group).
 *   <li>No conditional expressions, i.e. the consturct (?ifthen|else).
 * </ul>
 *
 * <p>See <a
 * href="http://www.boost.org/doc/libs/release/libs/regex/doc/html/boost_regex/syntax/perl_syntax.html">http://www.boost.org/doc/libs/release/libs/regex/doc/html/boost_regex/syntax/perl_syntax.html</a>
 * for a full overview of the supported regular expression syntax. However, if you want client-side
 * validation to work correctly, you will have to limit your regular expressions to those features
 * supported by JavaScript (ECMAScript style regular expressions). See <a
 * href="http://en.cppreference.com/w/cpp/regex/ecmascript">http://en.cppreference.com/w/cpp/regex/ecmascript</a>
 * for an overview of the ECMAScript regular expression syntax.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * WLineEdit lineEdit = new WLineEdit(this);
 * // an email address validator
 * WRegExpValidator validator = new WRegExpValidator("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}");
 * lineEdit.setValidator(validator);
 * lineEdit.setText("pieter@emweb.be");
 *
 * }</pre>
 *
 * <p>
 *
 * <p><i><b>Note: </b>This validator does not fully support unicode: it matches on the UTF8-encoded
 * representation of the string.</i>
 *
 * <h3>i18n</h3>
 *
 * <p>The strings used in this class can be translated by overriding the default values for the
 * following localization keys:
 *
 * <ul>
 *   <li>{@link WValidator.State#Invalid Wt.WRegExpValidator.Invalid}: Invalid input
 * </ul>
 */
public class WRegExpValidator extends WValidator {
  private static Logger logger = LoggerFactory.getLogger(WRegExpValidator.class);

  /** Sets a new regular expression validator. */
  public WRegExpValidator(WObject parent) {
    super(parent);
    this.regexp_ = null;
    this.noMatchText_ = new WString();
  }
  /**
   * Sets a new regular expression validator.
   *
   * <p>Calls {@link #WRegExpValidator(WObject parent) this((WObject)null)}
   */
  public WRegExpValidator() {
    this((WObject) null);
  }
  /**
   * Sets a new regular expression validator that accepts input that matches the given regular
   * expression.
   *
   * <p>This constructs a validator that matches the perl regular expression <code>expr</code>.
   */
  public WRegExpValidator(final String pattern, WObject parent) {
    super(parent);
    this.regexp_ = Pattern.compile(pattern);
    this.noMatchText_ = new WString();
  }
  /**
   * Sets a new regular expression validator that accepts input that matches the given regular
   * expression.
   *
   * <p>Calls {@link #WRegExpValidator(String pattern, WObject parent) this(pattern, (WObject)null)}
   */
  public WRegExpValidator(final String pattern) {
    this(pattern, (WObject) null);
  }
  /**
   * Sets the regular expression for valid input.
   *
   * <p>Sets the perl regular expression <code>expr</code>.
   */
  public void setRegExp(final String pattern) {
    if (!(this.regexp_ != null)) {
      this.regexp_ = Pattern.compile(pattern);
    } else {
      this.regexp_ = Pattern.compile(pattern, this.regexp_.flags());
    }
    this.repaint();
  }
  /**
   * Returns the regular expression for valid input.
   *
   * <p>Returns the perl regular expression.
   */
  public String getRegExp() {
    return this.regexp_ != null ? this.regexp_.pattern() : "";
  }
  /** Sets regular expression matching flags. */
  public void setFlags(int flags) {
    if (!(this.regexp_ != null)) {
      this.regexp_ = Pattern.compile(".*");
    }
    this.regexp_ = Pattern.compile(this.regexp_.pattern(), flags);
  }
  /** Returns regular expression matching flags. */
  public int getFlags() {
    if (this.regexp_ != null) {
      return this.regexp_.flags();
    } else {
      return (int) 0;
    }
  }
  /**
   * Validates the given input.
   *
   * <p>The input is considered valid only when it is blank for a non-mandatory field, or matches
   * the regular expression.
   */
  public WValidator.Result validate(final String input) {
    if (input.length() == 0) {
      return super.validate(input);
    }
    if (!(this.regexp_ != null) || this.regexp_.matcher(input).matches()) {
      return new WValidator.Result(WValidator.State.Valid);
    } else {
      return new WValidator.Result(WValidator.State.Invalid, this.getInvalidNoMatchText());
    }
  }
  // public void createExtConfig(final Writer config) throws IOException;
  /**
   * Sets the text to be shown if no match can be found.
   *
   * <p>This calls {@link WRegExpValidator#setInvalidNoMatchText(CharSequence text)
   * setInvalidNoMatchText()}
   *
   * <p>
   *
   * @deprecated Use {@link WRegExpValidator#setInvalidNoMatchText(CharSequence text)
   *     setInvalidNoMatchText()} instead
   */
  public void setNoMatchText(final CharSequence text) {
    this.setInvalidNoMatchText(text);
  }
  /**
   * Sets the message to display when the input does not match.
   *
   * <p>The default value is &quot;Invalid input&quot;.
   */
  public void setInvalidNoMatchText(final CharSequence text) {
    this.noMatchText_ = WString.toWString(text);
    this.repaint();
  }
  /**
   * Returns the message displayed when the input does not match.
   *
   * <p>
   *
   * @see WRegExpValidator#setInvalidNoMatchText(CharSequence text)
   */
  public WString getInvalidNoMatchText() {
    if (!(this.noMatchText_.length() == 0)) {
      return this.noMatchText_;
    } else {
      return WString.tr("Wt.WRegExpValidator.Invalid");
    }
  }

  public String getJavaScriptValidate() {
    loadJavaScript(WApplication.getInstance());
    StringBuilder js = new StringBuilder();
    js.append("new Wt3_5_1.WRegExpValidator(").append(this.isMandatory()).append(',');
    if (this.regexp_ != null) {
      js.append(WWebWidget.jsStringLiteral(this.regexp_.pattern())).append(",'");
      int flags = this.regexp_.flags();
      if ((flags & Pattern.CASE_INSENSITIVE) != 0) {
        js.append('i');
      }
      js.append('\'');
    } else {
      js.append("null, null");
    }
    js.append(',')
        .append(WWebWidget.jsStringLiteral(this.getInvalidBlankText()))
        .append(',')
        .append(WWebWidget.jsStringLiteral(this.getInvalidNoMatchText()))
        .append(");");
    return js.toString();
  }

  private Pattern regexp_;
  private WString noMatchText_;

  private static void loadJavaScript(WApplication app) {
    app.loadJavaScript("js/WRegExpValidator.js", wtjs1());
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WRegExpValidator",
        "function(e,b,f,g,h){var c=b?new RegExp(b,f):null;this.validate=function(a){if(a.length==0)return e?{valid:false,message:g}:{valid:true};if(c){var d=c.exec(a);return d!==null&&d[0].length===a.length?{valid:true}:{valid:false,message:h}}else return{valid:true}}}");
  }
}
