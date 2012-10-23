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
 * An input control for fixed point numbers.
 * <p>
 * 
 * The spin box provides a control for entering a fixed point number. It
 * consists of a line edit, and buttons which allow to increase or decrease the
 * value. If you rather need input of an integer number number, use
 * {@link WSpinBox} instead.
 * <p>
 * WDoubleSpinBox is an {@link WWidget#setInline(boolean inlined) inline}
 * widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * See {@link WAbstractSpinBox}.
 * <p>
 * 
 * @see WSpinBox <p>
 *      <i><b>Note: </b>A spinbox configures a validator for validating the
 *      input. Therefore you cannot set a validator yourself. </i>
 *      </p>
 */
public class WDoubleSpinBox extends WAbstractSpinBox {
	private static Logger logger = LoggerFactory
			.getLogger(WDoubleSpinBox.class);

	/**
	 * Creates a spin-box.
	 * <p>
	 * The range is (0.0 - 99.99), the step size 1.0, and the spin box has a
	 * precision of 2 decimals.
	 * <p>
	 * The initial value is 0.0.
	 */
	public WDoubleSpinBox(WContainerWidget parent) {
		super(parent);
		this.value_ = -1;
		this.min_ = 0.0;
		this.max_ = 99.99;
		this.step_ = 1.0;
		this.precision_ = 2;
		this.valueChanged_ = new Signal1<Double>();
		this.setValue(0.0);
	}

	/**
	 * Creates a spin-box.
	 * <p>
	 * Calls {@link #WDoubleSpinBox(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WDoubleSpinBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Sets the minimum value.
	 * <p>
	 * The default value is 0.0.
	 */
	public void setMinimum(double minimum) {
		this.min_ = minimum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the minimum value.
	 * <p>
	 * 
	 * @see WDoubleSpinBox#setMinimum(double minimum)
	 */
	public double getMinimum() {
		return this.min_;
	}

	/**
	 * Sets the maximum value.
	 * <p>
	 * The default value is 99.99.
	 */
	public void setMaximum(double maximum) {
		this.max_ = maximum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the maximum value.
	 * <p>
	 * 
	 * @see WDoubleSpinBox#setMaximum(double maximum)
	 */
	public double getMaximum() {
		return this.max_;
	}

	/**
	 * Sets the range.
	 * <p>
	 * 
	 * @see WDoubleSpinBox#setMinimum(double minimum)
	 * @see WDoubleSpinBox#setMaximum(double maximum)
	 */
	public void setRange(double minimum, double maximum) {
		this.min_ = minimum;
		this.max_ = maximum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Sets the step value.
	 * <p>
	 * The default value is 1.0.
	 */
	public void setSingleStep(double step) {
		this.step_ = step;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the step value.
	 * <p>
	 * 
	 * @see WDoubleSpinBox#setSingleStep(double step)
	 */
	public double getSingleStep() {
		return this.step_;
	}

	/**
	 * Sets the precision.
	 * <p>
	 * This sets the number of digits after the decimal point shown
	 * <p>
	 * The default precision is 2.
	 */
	public void setDecimals(int decimals) {
		this.precision_ = decimals;
		this.setText(this.getTextFromValue().toString());
	}

	/**
	 * Returns the precision.
	 * <p>
	 * 
	 * @see WDoubleSpinBox#setDecimals(int decimals)
	 */
	int getDecimals() {
		return this.precision_;
	}

	/**
	 * Sets the value.
	 * <p>
	 * <code>value</code> must be a value between
	 * {@link WDoubleSpinBox#getMinimum() getMinimum()} and
	 * {@link WDoubleSpinBox#getMaximum() getMaximum()}.
	 * <p>
	 * The default value is 0
	 */
	public void setValue(double value) {
		if (this.value_ != value) {
			this.value_ = value;
			this.setText(this.getTextFromValue().toString());
		}
	}

	/**
	 * Returns the value.
	 */
	public double getValue() {
		return this.value_;
	}

	/**
	 * A signal that indicates when the value has changed.
	 * <p>
	 * This signal is emitted when {@link WDoubleSpinBox#setValue(double value)
	 * setValue()} is called.
	 * <p>
	 * 
	 * @see WDoubleSpinBox#setValue(double value)
	 */
	public Signal1<Double> valueChanged() {
		return this.valueChanged_;
	}

	void updateDom(DomElement element, boolean all) {
		if (all || this.changed_) {
			if (this.isNativeControl()) {
				element.setAttribute("min", String.valueOf(this.min_));
				element.setAttribute("max", String.valueOf(this.max_));
				element.setAttribute("step", String.valueOf(this.step_));
			} else {
				final WDoubleValidator v = new WDoubleValidator();
				v.getJavaScriptValidate();
			}
		}
		super.updateDom(element, all);
	}

	void signalConnectionsChanged() {
		if (this.valueChanged_.isConnected() && !this.valueChangedConnection_) {
			this.valueChangedConnection_ = true;
			this.changed().addListener(this, new Signal.Listener() {
				public void trigger() {
					WDoubleSpinBox.this.onChange();
				}
			});
		}
	}

	String getJsMinMaxStep() {
		return String.valueOf(this.min_) + "," + String.valueOf(this.max_)
				+ "," + String.valueOf(this.step_);
	}

	boolean parseNumberValue(String text) {
		try {
			char[] buf = new char[30];
			String currentV = MathUtils.round(this.value_, this.precision_);
			if (!currentV.equals(text)) {
				this.value_ = Double.parseDouble(text);
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	WString getTextFromValue() {
		char[] buf = new char[30];
		String result = MathUtils.round(this.value_, this.precision_);
		if (!this.isNativeControl()) {
			result = this.getPrefix().toString() + result
					+ this.getSuffix().toString();
		}
		return new WString(result);
	}

	WValidator createValidator() {
		WDoubleValidator validator = new WDoubleValidator();
		validator.setRange(this.min_, this.max_);
		return validator;
	}

	protected WValidator.Result getValidateRange() {
		final WDoubleValidator validator = new WDoubleValidator();
		validator.setRange(this.min_, this.max_);
		return validator.validate(new WString("{1}").arg(this.value_)
				.toString());
	}

	private double value_;
	private double min_;
	private double max_;
	private double step_;
	private int precision_;
	private Signal1<Double> valueChanged_;

	private void onChange() {
		this.valueChanged_.trigger(this.getValue());
	}
}
