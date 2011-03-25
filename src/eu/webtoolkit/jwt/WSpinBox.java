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
 * An input control for integer numbers.
 * <p>
 * 
 * The spin box provides a control for entering an integer number. It consists
 * of a line edit, and buttons which allow to increase or decrease the value. If
 * you rather need input of a fractional number, use {@link WDoubleSpinBox}
 * instead.
 * <p>
 * WSpinBox is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * See {@link WAbstractSpinBox}.
 * <p>
 * 
 * @see WDoubleSpinBox
 */
public class WSpinBox extends WAbstractSpinBox {
	/**
	 * Creates a spin-box.
	 * <p>
	 * The range is (0 - 99) and the step size 1.
	 * <p>
	 * The initial value is 0.
	 */
	public WSpinBox(WContainerWidget parent) {
		super(parent);
		this.value_ = -1;
		this.min_ = 0;
		this.max_ = 99;
		this.step_ = 1;
		this.valueChanged_ = new Signal1<Integer>();
		this.setValue(0);
	}

	/**
	 * Creates a spin-box.
	 * <p>
	 * Calls {@link #WSpinBox(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WSpinBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Sets the minimum value.
	 * <p>
	 * The default value is 0.
	 */
	public void setMinimum(int minimum) {
		this.min_ = minimum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the minimum value.
	 * <p>
	 * 
	 * @see WSpinBox#setMinimum(int minimum)
	 */
	public int getMinimum() {
		return this.min_;
	}

	/**
	 * Sets the maximum value.
	 * <p>
	 * The default value is 99.
	 */
	public void setMaximum(int maximum) {
		this.max_ = maximum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the maximum value.
	 * <p>
	 * 
	 * @see WSpinBox#setMaximum(int maximum)
	 */
	public int getMaximum() {
		return this.max_;
	}

	/**
	 * Sets the range.
	 * <p>
	 * 
	 * @see WSpinBox#setMinimum(int minimum)
	 * @see WSpinBox#setMaximum(int maximum)
	 */
	public void setRange(int minimum, int maximum) {
		this.min_ = minimum;
		this.max_ = maximum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Sets the step value.
	 * <p>
	 * The default value is 1.
	 */
	public void setSingleStep(int step) {
		this.step_ = step;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the step value.
	 */
	public int getSingleStep() {
		return this.step_;
	}

	/**
	 * Sets the value.
	 * <p>
	 * <code>value</code> must be a value between {@link WSpinBox#getMinimum()
	 * getMinimum()} and {@link WSpinBox#getMaximum() getMaximum()}.
	 * <p>
	 * The default value is 0
	 */
	public void setValue(int value) {
		if (this.value_ != value) {
			this.value_ = value;
			this.setText(this.getTextFromValue().toString());
		}
	}

	/**
	 * Returns the value.
	 */
	public int getValue() {
		return this.value_;
	}

	/**
	 * A signal that indicates when the value has changed.
	 * <p>
	 * This signal is emitted when {@link WSpinBox#setValue(int value)
	 * setValue()} is called.
	 * <p>
	 * 
	 * @see WSpinBox#setValue(int value)
	 */
	public Signal1<Integer> valueChanged() {
		return this.valueChanged_;
	}

	void updateDom(DomElement element, boolean all) {
		if (all || this.changed_) {
			if (this.isNativeControl()) {
				element.setAttribute("min", String.valueOf(this.min_));
				element.setAttribute("max", String.valueOf(this.max_));
				element.setAttribute("step", String.valueOf(this.step_));
			} else {
				final WIntValidator v = new WIntValidator();
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
					WSpinBox.this.onChange();
				}
			});
		}
	}

	protected String getJsMinMaxStep() {
		return String.valueOf(this.min_) + "," + String.valueOf(this.max_)
				+ "," + String.valueOf(this.step_);
	}

	protected int getDecimals() {
		return 0;
	}

	protected boolean parseNumberValue(String text) {
		try {
			this.value_ = Integer.parseInt(text);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	protected WString getTextFromValue() {
		if (this.isNativeControl()) {
			return new WString(String.valueOf(this.value_));
		} else {
			String text = this.getPrefix().toString()
					+ String.valueOf(this.value_) + this.getSuffix().toString();
			return new WString(text);
		}
	}

	protected WValidator getCreateValidator() {
		WIntValidator validator = new WIntValidator();
		validator.setRange(this.min_, this.max_);
		return validator;
	}

	private int value_;
	private int min_;
	private int max_;
	private int step_;
	private Signal1<Integer> valueChanged_;

	private void onChange() {
		this.valueChanged_.trigger(this.getValue());
	}
}
