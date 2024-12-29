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
 * An input control for integer numbers.
 *
 * <p>The spin box provides a control for entering an integer number. It consists of a line edit,
 * and buttons which allow to increase or decrease the value. If you rather need input of a
 * fractional number, use {@link WDoubleSpinBox} instead.
 *
 * <p>WSpinBox is an {@link WWidget#setInline(boolean inlined) inline } widget.
 *
 * <p>
 *
 * @see WDoubleSpinBox
 *     <p><i><b>Note: </b>A spinbox configures a validator for validating the input. Therefore you
 *     cannot set a validator yourself. </i>
 */
public class WSpinBox extends WAbstractSpinBox {
  private static Logger logger = LoggerFactory.getLogger(WSpinBox.class);

  /**
   * Creates a spin-box.
   *
   * <p>The range is (0 - 99) and the step size 1.
   *
   * <p>The initial value is 0.
   */
  public WSpinBox(WContainerWidget parentContainer) {
    super();
    this.value_ = -1;
    this.min_ = 0;
    this.max_ = 99;
    this.step_ = 1;
    this.wrapAroundEnabled_ = false;
    this.valueChanged_ = new Signal1<Integer>();
    this.setValidator(this.createValidator());
    this.setValue(0);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a spin-box.
   *
   * <p>Calls {@link #WSpinBox(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WSpinBox() {
    this((WContainerWidget) null);
  }
  /**
   * Sets the minimum value.
   *
   * <p>The default value is 0.
   */
  public void setMinimum(int minimum) {
    this.min_ = minimum;
    WIntValidator v = ObjectUtils.cast(this.getValidator(), WIntValidator.class);
    if (v != null) {
      v.setBottom(this.min_);
    }
    this.changed_ = true;
    this.repaint();
  }
  /**
   * Returns the minimum value.
   *
   * <p>
   *
   * @see WSpinBox#setMinimum(int minimum)
   */
  public int getMinimum() {
    return this.min_;
  }
  /**
   * Sets the maximum value.
   *
   * <p>The default value is 99.
   */
  public void setMaximum(int maximum) {
    this.max_ = maximum;
    WIntValidator v = ObjectUtils.cast(this.getValidator(), WIntValidator.class);
    if (v != null) {
      v.setTop(this.max_);
    }
    this.changed_ = true;
    this.repaint();
  }
  /**
   * Returns the maximum value.
   *
   * <p>
   *
   * @see WSpinBox#setMaximum(int maximum)
   */
  public int getMaximum() {
    return this.max_;
  }
  /**
   * Sets the range.
   *
   * <p>
   *
   * @see WSpinBox#setMinimum(int minimum)
   * @see WSpinBox#setMaximum(int maximum)
   */
  public void setRange(int minimum, int maximum) {
    this.setMinimum(minimum);
    this.setMaximum(maximum);
  }
  /**
   * Sets the step value.
   *
   * <p>The default value is 1.
   */
  public void setSingleStep(int step) {
    this.step_ = step;
    this.changed_ = true;
    this.repaint();
  }
  /** Returns the step value. */
  public int getSingleStep() {
    return this.step_;
  }
  /**
   * Sets the value.
   *
   * <p><code>value</code> must be a value between {@link WSpinBox#getMinimum() getMinimum()} and
   * {@link WSpinBox#getMaximum() getMaximum()}.
   *
   * <p>The default value is 0
   */
  public void setValue(int value) {
    if (this.value_ != value || !this.getText().equals(this.getTextFromValue())) {
      this.value_ = value;
      this.setText(this.getTextFromValue());
    }
  }
  /**
   * Returns the value.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This value may not correctly reflect the {@link WLineEdit#getValueText()} of
   * the spin box if {@link WLineEdit#getValueText()} is empty or if the contents are not in a
   * {@link WFormWidget#validate() valid state}. </i>
   */
  public int getValue() {
    return this.value_;
  }
  /**
   * Sets if this spinbox wraps around to stay in the valid range.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Not supported by the native controls. </i>
   */
  public void setWrapAroundEnabled(boolean enabled) {
    if (this.wrapAroundEnabled_ != enabled) {
      this.wrapAroundEnabled_ = enabled;
      this.changed_ = true;
      this.repaint();
    }
  }
  /**
   * Returns if the spinbox wraps around.
   *
   * <p>
   *
   * @see WSpinBox#setWrapAroundEnabled(boolean enabled)
   */
  public boolean isWrapAroundEnabled() {
    return this.wrapAroundEnabled_;
  }
  /**
   * A signal that indicates when the value has changed.
   *
   * <p>This signal is emitted when {@link WFormWidget#changed()} is emitted, but supplies the new
   * value as an argument. The {@link WFormWidget#changed()} signal is emitted when the user changes
   * the value of the spinbox by pressing the up/down arrow, or entering a different value and
   * pressing enter or moving focus.
   *
   * <p>
   *
   * @see WFormWidget#changed()
   */
  public Signal1<Integer> valueChanged() {
    return this.valueChanged_;
  }

  void updateDom(final DomElement element, boolean all) {
    if (all || this.changed_) {
      if (this.isNativeControl()) {
        element.setAttribute("min", String.valueOf(this.min_));
        element.setAttribute("max", String.valueOf(this.max_));
        element.setAttribute("step", String.valueOf(this.step_));
      } else {
        WIntValidator v = new WIntValidator();
        v.getJavaScriptValidate();
        this.doJavaScript(
            this.getJsRef()
                + ".wtObj.setWrapAroundEnabled("
                + (this.isWrapAroundEnabled() ? "true" : "false")
                + ");");
      }
    }
    super.updateDom(element, all);
  }

  void signalConnectionsChanged() {
    if (this.valueChanged_.isConnected() && !this.valueChangedConnection_) {
      this.valueChangedConnection_ = true;
      this.changed()
          .addListener(
              this,
              () -> {
                WSpinBox.this.onChange();
              });
    }
    super.signalConnectionsChanged();
  }

  String getJsMinMaxStep() {
    return String.valueOf(this.min_)
        + ","
        + String.valueOf(this.max_)
        + ","
        + String.valueOf(this.step_);
  }

  int getDecimals() {
    return 0;
  }

  boolean parseNumberValue(final String text) {
    try {
      this.value_ = LocaleUtils.toInt(LocaleUtils.getCurrentLocale(), text);
      return true;
    } catch (final RuntimeException e) {
      return false;
    }
  }

  protected String getTextFromValue() {
    if (this.isNativeControl()) {
      return LocaleUtils.toString(LocaleUtils.getCurrentLocale(), this.value_);
    } else {
      String text =
          this.getPrefix().toString()
              + LocaleUtils.toString(LocaleUtils.getCurrentLocale(), this.value_)
              + this.getSuffix().toString();
      return text;
    }
  }

  WValidator createValidator() {
    WIntValidator validator = new WIntValidator();
    validator.setMandatory(true);
    validator.setRange(this.min_, this.max_);
    return validator;
  }

  protected WValidator.Result getValidateRange() {
    WIntValidator validator = new WIntValidator();
    validator.setRange(this.min_, this.max_);
    String badRangeText = WString.tr("Wt.WIntValidator.BadRange").toString();
    StringUtils.replace(badRangeText, "{1}", "{1}" + this.getSuffix().toString());
    StringUtils.replace(badRangeText, "{2}", "{2}" + this.getSuffix().toString());
    validator.setInvalidTooLargeText(new WString(badRangeText));
    validator.setInvalidTooSmallText(new WString(badRangeText));
    return validator.validate(new WString("{1}").arg(this.value_).toString());
  }

  private int value_;
  private int min_;
  private int max_;
  private int step_;
  private boolean wrapAroundEnabled_;
  private Signal1<Integer> valueChanged_;

  private void onChange() {
    this.valueChanged_.trigger(this.getValue());
  }
}
