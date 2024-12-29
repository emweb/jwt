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
 * An input control for fixed point numbers.
 *
 * <p>The spin box provides a control for entering a fixed point number. It consists of a line edit,
 * and buttons which allow to increase or decrease the value. If you rather need input of an integer
 * number number, use {@link WSpinBox} instead.
 *
 * <p>WDoubleSpinBox is an {@link WWidget#setInline(boolean inlined) inline } widget.
 *
 * <p>
 *
 * @see WSpinBox
 *     <p><i><b>Note: </b>A spinbox configures a validator for validating the input. Therefore you
 *     cannot set a validator yourself. </i>
 */
public class WDoubleSpinBox extends WAbstractSpinBox {
  private static Logger logger = LoggerFactory.getLogger(WDoubleSpinBox.class);

  /**
   * Creates a spin-box.
   *
   * <p>The range is (0.0 - 99.99), the step size 1.0, and the spin box has a precision of 2
   * decimals.
   *
   * <p>The initial value is 0.0.
   */
  public WDoubleSpinBox(WContainerWidget parentContainer) {
    super();
    this.setup_ = false;
    this.value_ = -1;
    this.min_ = 0.0;
    this.max_ = 99.99;
    this.step_ = 1.0;
    this.precision_ = 2;
    this.valueChanged_ = new Signal1<Double>();
    this.setValidator(this.createValidator());
    this.setValue(0.0);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a spin-box.
   *
   * <p>Calls {@link #WDoubleSpinBox(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WDoubleSpinBox() {
    this((WContainerWidget) null);
  }
  /**
   * Sets the minimum value.
   *
   * <p>The default value is 0.0.
   */
  public void setMinimum(double minimum) {
    this.min_ = minimum;
    WDoubleValidator v = ObjectUtils.cast(this.getValidator(), WDoubleValidator.class);
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
   * @see WDoubleSpinBox#setMinimum(double minimum)
   */
  public double getMinimum() {
    return this.min_;
  }
  /**
   * Sets the maximum value.
   *
   * <p>The default value is 99.99.
   */
  public void setMaximum(double maximum) {
    this.max_ = maximum;
    WDoubleValidator v = ObjectUtils.cast(this.getValidator(), WDoubleValidator.class);
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
   * @see WDoubleSpinBox#setMaximum(double maximum)
   */
  public double getMaximum() {
    return this.max_;
  }
  /**
   * Sets the range.
   *
   * <p>
   *
   * @see WDoubleSpinBox#setMinimum(double minimum)
   * @see WDoubleSpinBox#setMaximum(double maximum)
   */
  public void setRange(double minimum, double maximum) {
    this.setMinimum(minimum);
    this.setMaximum(maximum);
  }
  /**
   * Sets the step value.
   *
   * <p>The default value is 1.0.
   */
  public void setSingleStep(double step) {
    this.step_ = step;
    this.changed_ = true;
    this.repaint();
  }
  /**
   * Returns the step value.
   *
   * <p>
   *
   * @see WDoubleSpinBox#setSingleStep(double step)
   */
  public double getSingleStep() {
    return this.step_;
  }
  /**
   * Sets the precision.
   *
   * <p>This sets the number of digits after the decimal point shown
   *
   * <p>The default precision is 2.
   */
  public void setDecimals(int decimals) {
    this.precision_ = decimals;
    this.setText(this.getTextFromValue());
  }
  /**
   * Returns the precision.
   *
   * <p>
   *
   * @see WDoubleSpinBox#setDecimals(int decimals)
   */
  int getDecimals() {
    return this.precision_;
  }
  /**
   * Sets the value.
   *
   * <p><code>value</code> must be a value between {@link WDoubleSpinBox#getMinimum() getMinimum()}
   * and {@link WDoubleSpinBox#getMaximum() getMaximum()}.
   *
   * <p>The default value is 0
   */
  public void setValue(double value) {
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
  public double getValue() {
    return this.value_;
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
  public Signal1<Double> valueChanged() {
    return this.valueChanged_;
  }

  public void refresh() {
    this.setText(this.getTextFromValue());
    super.refresh();
  }

  void updateDom(final DomElement element, boolean all) {
    if (all || this.changed_) {
      if (this.isNativeControl()) {
        element.setAttribute("min", String.valueOf(this.min_));
        element.setAttribute("max", String.valueOf(this.max_));
        element.setAttribute("step", String.valueOf(this.step_));
      } else {
        WDoubleValidator v = new WDoubleValidator();
        v.getJavaScriptValidate();
      }
    }
    super.updateDom(element, all);
  }

  protected void render(EnumSet<RenderFlag> flags) {
    super.render(flags);
    if (!this.setup_ && flags.contains(RenderFlag.Full)) {
      this.setup();
    }
  }

  void signalConnectionsChanged() {
    if (this.valueChanged_.isConnected() && !this.valueChangedConnection_) {
      this.valueChangedConnection_ = true;
      this.changed()
          .addListener(
              this,
              () -> {
                WDoubleSpinBox.this.onChange();
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

  boolean parseNumberValue(final String text) {
    try {
      if (!this.getTextFromValue().equals(text)) {
        this.value_ = LocaleUtils.toDouble(LocaleUtils.getCurrentLocale(), text);
      }
      return true;
    } catch (final RuntimeException e) {
      return false;
    }
  }

  protected String getTextFromValue() {
    String result =
        LocaleUtils.toFixedString(LocaleUtils.getCurrentLocale(), this.value_, this.precision_);
    if (!this.isNativeControl()) {
      result = this.getPrefix().toString() + result + this.getSuffix().toString();
    }
    return result;
  }

  WValidator createValidator() {
    WDoubleValidator validator = new WDoubleValidator();
    validator.setMandatory(true);
    validator.setRange(this.min_, this.max_);
    return validator;
  }

  protected WValidator.Result getValidateRange() {
    WDoubleValidator validator = new WDoubleValidator();
    validator.setRange(this.min_, this.max_);
    String badRangeText = WString.tr("Wt.WDoubleValidator.BadRange").toString();
    StringUtils.replace(badRangeText, "{1}", "{1}" + this.getSuffix().toString());
    StringUtils.replace(badRangeText, "{2}", "{2}" + this.getSuffix().toString());
    validator.setInvalidTooLargeText(new WString(badRangeText));
    validator.setInvalidTooSmallText(new WString(badRangeText));
    return validator.validate(new WString("{1}").arg(this.value_).toString());
  }

  private boolean setup_;
  private double value_;
  private double min_;
  private double max_;
  private double step_;
  private int precision_;
  private Signal1<Double> valueChanged_;

  private void setup() {
    this.setup_ = true;
    this.doJavaScript(this.getJsRef() + ".wtObj.setIsDoubleSpinBox(true);");
  }

  private void onChange() {
    this.valueChanged_.trigger(this.getValue());
  }
}
