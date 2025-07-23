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
 * A password edit.
 *
 * <p>A password edit is a line edit where the character are hidden.
 *
 * <p>The widget corresponds to the HTML <code>&lt;input type=&quot;password&quot;&gt;</code> tag.
 */
public class WPasswordEdit extends WLineEdit {
  private static Logger logger = LoggerFactory.getLogger(WPasswordEdit.class);

  /** Creates a password edit with empty content. */
  public WPasswordEdit(WContainerWidget parentContainer) {
    super();
    this.nativeControl_ = false;
    this.pwdValidator_ = null;
    this.otherValidator_ = null;
    this.flags_ = new BitSet();
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a password edit with empty content.
   *
   * <p>Calls {@link #WPasswordEdit(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WPasswordEdit() {
    this((WContainerWidget) null);
  }
  /** Creates a password edit with given content. */
  public WPasswordEdit(final String content, WContainerWidget parentContainer) {
    super(content, (WContainerWidget) null);
    this.nativeControl_ = false;
    this.pwdValidator_ = null;
    this.otherValidator_ = null;
    this.flags_ = new BitSet();
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a password edit with given content.
   *
   * <p>Calls {@link #WPasswordEdit(String content, WContainerWidget parentContainer) this(content,
   * (WContainerWidget)null)}
   */
  public WPasswordEdit(final String content) {
    this(content, (WContainerWidget) null);
  }

  public void setMaxLength(int length) {
    if (length != this.getMaxLength()) {
      this.pwdValidator_.setMaxLength(length);
    }
    super.setMaxLength(length);
  }
  /**
   * Changes whether a native HTML5 control is used.
   *
   * <p>When enabled the browser&apos;s native attribute for password input (&lt;input
   * type=&quot;password&quot;&gt;) will be used instead of a validator.
   *
   * <p>This option is set to false by default. @see WPasswordEdit#isNativeControl()
   */
  public void setNativeControl(boolean nativeControl) {
    if (nativeControl != this.nativeControl_) {
      this.nativeControl_ = nativeControl;
      this.flags_.set(BIT_CONTROL_CHANGED);
      if (!nativeControl) {
        this.getStackedValidator().addValidator(this.pwdValidator_);
      } else {
        this.getStackedValidator().removeValidator(this.pwdValidator_);
      }
    }
  }
  /**
   * Returns whether a native HTML5 control is used.
   *
   * <p>
   *
   * @see WPasswordEdit#setNativeControl(boolean nativeControl)
   */
  public boolean isNativeControl() {
    return this.nativeControl_;
  }
  /**
   * Specifies the minimum length of text that can be entered.
   *
   * <p>The default value is 0.
   */
  public void setMinLength(int length) {
    if (length != this.getMinLength()) {
      this.pwdValidator_.setMinLength(length);
      this.flags_.set(BIT_MIN_LENGTH_CHANGED);
      this.repaint();
    }
  }
  /**
   * Returns the minimum length of text that can be entered.
   *
   * <p>
   *
   * @see WPasswordEdit#setMinLength(int length)
   */
  public int getMinLength() {
    return this.pwdValidator_.getMinLength();
  }
  /**
   * Specifies if the password is required.
   *
   * <p>If true, the password cannot be empty.
   *
   * <p>The default value is true.
   */
  public void setRequired(boolean required) {
    if (required != this.isRequired()) {
      this.pwdValidator_.setMandatory(required);
      this.flags_.set(BIT_REQUIRED_CHANGED);
      this.repaint();
    }
  }
  /**
   * Return if the password is required.
   *
   * <p>
   *
   * @see WPasswordEdit#setRequired(boolean required)
   */
  public boolean isRequired() {
    return this.pwdValidator_.isMandatory();
  }
  /**
   * Specifies the pattern that the password must match.
   *
   * <p>The default value is &quot;&quot;.
   */
  public void setPattern(final String newPattern) {
    this.pwdValidator_.setRegExp(newPattern);
    this.flags_.set(BIT_PATTERN_CHANGED);
    this.repaint();
  }
  /**
   * Return the pattern the password must match.
   *
   * <p>
   *
   * @see WPasswordEdit#setPattern(String newPattern)
   */
  public WString getPattern() {
    return new WString(this.pwdValidator_.getRegExpPattern());
  }
  /**
   * Sets the message to display when the password is too long.
   *
   * <p>The default value is &quot;Password too long&quot;.
   */
  public void setInvalidTooLongText(final CharSequence text) {
    this.pwdValidator_.setInvalidTooLongText(text);
  }
  /**
   * Returns the message displayed when the password is too long.
   *
   * <p>
   *
   * @see WPasswordEdit#setInvalidTooLongText(CharSequence text)
   */
  public WString getInvalidTooLongText() {
    return this.pwdValidator_.getInvalidTooLongText();
  }
  /**
   * Sets the message to display when the password is too small.
   *
   * <p>The default value is &quot;Password too small&quot;.
   */
  public void setInvalidTooShortText(final CharSequence text) {
    this.pwdValidator_.setInvalidTooShortText(text);
  }
  /**
   * Returns the message displayed when the password is too small.
   *
   * <p>
   *
   * @see WPasswordEdit#setInvalidTooShortText(CharSequence text)
   */
  public WString getInvalidTooShortText() {
    return this.pwdValidator_.getInvalidTooShortText();
  }
  /**
   * Sets the message to display when the password does not match the pattern.
   *
   * <p>The default value is &quot;Invalid input&quot;.
   */
  public void setInvalidNoMatchText(final CharSequence text) {
    this.pwdValidator_.setInvalidNoMatchText(text);
  }
  /**
   * Returns the message displayed when the password does not match the pattern.
   *
   * <p>
   *
   * @see WPasswordEdit#setInvalidNoMatchText(CharSequence text)
   */
  public WString getInvalidNoMatchText() {
    return this.pwdValidator_.getInvalidNoMatchText();
  }
  /**
   * Sets the message to display when the password is empty and required.
   *
   * <p>The default value is &quot;This field cannot be empty&quot;.
   */
  public void setInvalidBlankText(final CharSequence text) {
    this.pwdValidator_.setInvalidBlankText(text);
  }
  /**
   * Returns the message displayed when the password is empty and required.
   *
   * <p>
   *
   * @see WPasswordEdit#setInvalidBlankText(CharSequence text)
   */
  public WString getInvalidBlankText() {
    return this.pwdValidator_.getInvalidBlankText();
  }

  public void setValidator(final WValidator validator) {
    if (this.otherValidator_ != validator) {
      if (this.otherValidator_ != null) {
        this.getStackedValidator().removeValidator(this.otherValidator_);
      }
      this.otherValidator_ = validator;
      if (this.otherValidator_ != null) {
        this.getStackedValidator().insertValidator(0, this.otherValidator_);
      }
    }
    super.setValidator(this.getStackedValidator());
  }

  public WValidator getValidator() {
    return this.otherValidator_;
  }

  public ValidationState validate() {
    if (this.nativeControl_) {
      ValidationState state = this.pwdValidator_.validate(this.getText()).getState();
      if (state != ValidationState.Valid) {
        return state;
      }
    }
    return super.validate();
  }

  void updateDom(final DomElement element, boolean all) {
    boolean controlChanged = this.flags_.get(BIT_CONTROL_CHANGED);
    if (this.nativeControl_) {
      if (all || controlChanged || this.flags_.get(BIT_MIN_LENGTH_CHANGED)) {
        if (!all || this.getMinLength() > 0) {
          element.setAttribute("minlength", String.valueOf(this.getMinLength()));
        }
        this.flags_.clear(BIT_MIN_LENGTH_CHANGED);
      }
      if (all || controlChanged || this.flags_.get(BIT_REQUIRED_CHANGED)) {
        if (this.isRequired()) {
          element.setAttribute("required", "");
        } else {
          element.removeAttribute("required");
        }
        this.flags_.clear(BIT_MIN_LENGTH_CHANGED);
      }
      if (all || controlChanged || this.flags_.get(BIT_PATTERN_CHANGED)) {
        if (!(this.getPattern().length() == 0)) {
          element.setAttribute("pattern", this.getPattern().toString());
        } else {
          element.removeAttribute("pattern");
        }
        this.flags_.clear(BIT_PATTERN_CHANGED);
      }
    } else {
      if (controlChanged) {
        element.removeAttribute("minlength");
        element.removeAttribute("required");
        element.removeAttribute("pattern");
      }
    }
    super.updateDom(element, all);
  }

  protected String getType() {
    return "password";
  }

  protected WValidator getRealValidator() {
    return super.getValidator();
  }

  private boolean nativeControl_;
  private WPasswordValidator pwdValidator_;
  private WValidator otherValidator_;
  private static final int BIT_MIN_LENGTH_CHANGED = 0;
  private static final int BIT_REQUIRED_CHANGED = 1;
  private static final int BIT_CONTROL_CHANGED = 2;
  private static final int BIT_PATTERN_CHANGED = 3;
  BitSet flags_;

  private WStackedValidator getStackedValidator() {
    return ObjectUtils.cast(super.getValidator(), WStackedValidator.class);
  }

  private void init() {
    this.pwdValidator_ = new WPasswordValidator();
    super.setValidator(new WStackedValidator());
    this.getStackedValidator().addValidator(this.pwdValidator_);
  }
}
