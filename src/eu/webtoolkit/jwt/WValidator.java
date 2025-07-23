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
 * A validator is used to validate user input according to pre-defined rules.
 *
 * <p>A validator may be associated with a form widget using {@link
 * WFormWidget#setValidator(WValidator validator) WFormWidget#setValidator()}.
 *
 * <p>The validator validates the user input. A validator may have a split implementation to provide
 * both validation at the client-side (which gives instant feed-back to the user while editing), and
 * server-side validation (to be sure that the client was not tampered with). The feed-back given by
 * (client-side and server-side) validation is reflected in the style class of the form field: a
 * style class of <code>Wt-invalid</code> is set for a field that is invalid.
 *
 * <p>This WValidator only checks that mandatory fields are not empty. This class is reimplemented
 * in {@link WDateValidator}, {@link WIntValidator}, {@link WDoubleValidator}, {@link
 * WLengthValidator} and {@link WRegExpValidator}. All these validators provide both client-side and
 * server-side validation.
 *
 * <p>If these validators are not suitable, you can inherit from this class, and provide a suitable
 * implementation to {@link WValidator#validate(String input) validate()} which does the server-side
 * validation. If you want to provide client-side validation for your own validator, you may also
 * reimplement {@link WValidator#getJavaScriptValidate() getJavaScriptValidate()}.
 *
 * <p>
 *
 * <h3>i18n</h3>
 *
 * <p>The strings used in this class can be translated by overriding the default values for the
 * following localization keys:
 *
 * <ul>
 *   <li>Wt.WValidator.Invalid: This field cannot be empty
 * </ul>
 *
 * <p>
 *
 * @see WFormWidget
 */
public class WValidator {
  private static Logger logger = LoggerFactory.getLogger(WValidator.class);

  /**
   * A class that holds a validation result.
   *
   * <p>This structure is returned as the result of validation.
   */
  public static class Result {
    private static Logger logger = LoggerFactory.getLogger(Result.class);

    /**
     * Default constructor.
     *
     * <p>Creates an invalid result.
     */
    public Result() {
      this.state_ = ValidationState.Invalid;
      this.message_ = new WString();
    }
    /**
     * Constructor.
     *
     * <p>Creates a result with given <code>state</code> and <code>message</code>.
     */
    public Result(ValidationState state, final CharSequence message) {
      this.state_ = state;
      this.message_ = WString.toWString(message);
    }
    /**
     * Constructor.
     *
     * <p>Creates a result with given <code>state</code> and initalizes the message field to an
     * empty {@link WString}.
     */
    public Result(ValidationState state) {
      this.state_ = state;
      this.message_ = WString.Empty;
    }
    /** Returns the validation state. */
    public ValidationState getState() {
      return this.state_;
    }
    /** Returns the validation message. */
    public WString getMessage() {
      return this.message_;
    }

    private ValidationState state_;
    private WString message_;
  }
  /**
   * Creates a new validator.
   *
   * <p>Indicate whether input is mandatory.
   *
   * <p>
   *
   * @see WValidator#setMandatory(boolean mandatory)
   */
  public WValidator(boolean mandatory) {
    this.mandatory_ = mandatory;
    this.mandatoryText_ = new WString();
    this.formWidgets_ = new ArrayList<WFormWidget>();
    this.parentValidators_ = new ArrayList<WValidator>();
  }
  /**
   * Creates a new validator.
   *
   * <p>Calls {@link #WValidator(boolean mandatory) this(false)}
   */
  public WValidator() {
    this(false);
  }
  /**
   * Sets if input is mandatory.
   *
   * <p>When an input is not mandatory, then an empty field is always valid.
   */
  public void setMandatory(boolean mandatory) {
    if (this.mandatory_ != mandatory) {
      this.mandatory_ = mandatory;
      this.repaint();
    }
  }
  /** Returns if input is mandatory. */
  public boolean isMandatory() {
    return this.mandatory_;
  }
  /**
   * Sets the message to display when a mandatory field is left blank.
   *
   * <p>The default value is &quot;This field cannot be empty&quot;.
   */
  public void setInvalidBlankText(final CharSequence text) {
    this.mandatoryText_ = WString.toWString(text);
    this.repaint();
  }
  /**
   * Returns the message displayed when a mandatory field is left blank.
   *
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
   * Validates the given input.
   *
   * <p>
   *
   * <p><i><b>Note: </b>The signature for this method changed in JWt 3.2.0. </i>
   */
  public WValidator.Result validate(final String input) {
    if (this.isMandatory()) {
      if (input.length() == 0) {
        return new WValidator.Result(ValidationState.InvalidEmpty, this.getInvalidBlankText());
      }
    }
    return new WValidator.Result(ValidationState.Valid);
  }
  /**
   * Returns the validator format.
   *
   * <p>The default implementation returns an empty string.
   */
  public String getFormat() {
    return "";
  }
  /**
   * Creates a Javascript object that validates the input.
   *
   * <p>The JavaScript expression should evaluate to an object which contains a <code>validate(text)
   * </code> function, which returns an object that contains the following two fields:
   *
   * <ul>
   *   <li>fields: a boolean <i>valid</i>,
   *   <li>a <code>message</code> that indicates the problem if not valid.
   * </ul>
   *
   * <p>Returns an empty string if the validator does not provide a client-side validation
   * implementationq.
   *
   * <p>
   *
   * @see WValidator#getInputFilter()
   */
  public String getJavaScriptValidate() {
    if (this.mandatory_) {
      return "new (function() {this.validate = function(text) {return { valid: text.length != 0, message: "
          + WString.toWString(this.getInvalidBlankText()).getJsStringLiteral()
          + "}};})();";
    } else {
      return "new (function() {this.validate = function(text) {return { valid: true }};})();";
    }
  }
  /**
   * Returns a regular expression that filters input.
   *
   * <p>The returned regular expression is used to filter keys presses. The regular expression
   * should accept valid single characters.
   *
   * <p>For details on valid regular expressions, see {@link WRegExpValidator}. As an example,
   * &quot;[0-9]&quot; would only accept numbers as valid input.
   *
   * <p>The default implementation returns an empty string, which does not filter any input.
   *
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
    for (int i = 0; i < this.parentValidators_.size(); ++i) {
      this.parentValidators_.get(i).repaint();
    }
  }

  private boolean mandatory_;
  private WString mandatoryText_;
  private List<WFormWidget> formWidgets_;
  private List<WValidator> parentValidators_;

  void addFormWidget(WFormWidget w) {
    this.formWidgets_.add(w);
  }

  void removeFormWidget(WFormWidget w) {
    this.formWidgets_.remove(w);
  }

  void addParentValidator(WValidator v) {
    this.parentValidators_.add(v);
  }

  void removeParentValidator(WValidator v) {
    this.parentValidators_.remove(v);
  }
}
