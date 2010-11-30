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
 * A horizontal or vertical slider control.
 * <p>
 * 
 * A slider allows the user to specify an integer value within a particular
 * range using a visual slider.
 * <p>
 * The slider must be sized explicitly using
 * {@link WWidget#resize(WLength width, WLength height) WWidget#resize()}. The
 * default size is 150 x 50 pixels for a horizontal slider, and 50 x 150 pixels
 * for a vertical slider.
 * <p>
 * <div align="center"> <img src="doc-files//WSlider-1.png"
 * alt="Horizontal slider with ticks on both sides.">
 * <p>
 * <strong>Horizontal slider with ticks on both sides.</strong>
 * </p>
 * </div> <h3>CSS</h3>
 * <p>
 * The slider is styled by the current CSS theme. The look can be overridden
 * using the <code>Wt-slider</code> CSS class and the following selectors:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-slider .handle-v : The vertical handle
 * .Wt-slider .handle-h : The horizontal handle
 * </pre>
 * 
 * </div>
 */
public class WSlider extends WCompositeWidget {
	/**
	 * Enumeration that specifies the location of ticks.
	 */
	public enum TickPosition {
		/**
		 * Render ticks above (horizontal slider).
		 */
		TicksAbove,
		/**
		 * Render ticks below (horizontal slider).
		 */
		TicksBelow;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a default horizontal slider.
	 * <p>
	 * The slider shows no ticks, has a range from 0 to 99, and has tickInterval
	 * of 0 (defaulting to three ticks over the whole range).
	 * <p>
	 * The initial value is 0.
	 */
	public WSlider(WContainerWidget parent) {
		super(parent);
		this.orientation_ = Orientation.Horizontal;
		this.tickInterval_ = 0;
		this.tickPosition_ = EnumSet.noneOf(WSlider.TickPosition.class);
		this.minimum_ = 0;
		this.maximum_ = 99;
		this.value_ = 0;
		this.valueChanged_ = new Signal1<Integer>(this);
		this.sliderMoved_ = new JSignal1<Integer>(this, "moved") {
		};
		this.sliderReleased_ = new JSignal1<Integer>(this, "released") {
		};
		this.mouseDownJS_ = new JSlot();
		this.mouseMovedJS_ = new JSlot();
		this.mouseUpJS_ = new JSlot();
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.create();
	}

	/**
	 * Creates a default horizontal slider.
	 * <p>
	 * Calls {@link #WSlider(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WSlider() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a default slider of the given orientation.
	 * <p>
	 * The slider shows no ticks, has a range from 0 to 99, and has tickInterval
	 * of 0 (defaulting to three ticks over the whole range).
	 * <p>
	 * The initial value is 0.
	 */
	public WSlider(Orientation orientation, WContainerWidget parent) {
		super(parent);
		this.orientation_ = orientation;
		this.tickInterval_ = 0;
		this.tickPosition_ = EnumSet.noneOf(WSlider.TickPosition.class);
		this.minimum_ = 0;
		this.maximum_ = 99;
		this.value_ = 0;
		this.valueChanged_ = new Signal1<Integer>(this);
		this.sliderMoved_ = new JSignal1<Integer>(this, "moved") {
		};
		this.sliderReleased_ = new JSignal1<Integer>(this, "released") {
		};
		this.mouseDownJS_ = new JSlot();
		this.mouseMovedJS_ = new JSlot();
		this.mouseUpJS_ = new JSlot();
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.create();
	}

	/**
	 * Creates a default slider of the given orientation.
	 * <p>
	 * Calls {@link #WSlider(Orientation orientation, WContainerWidget parent)
	 * this(orientation, (WContainerWidget)null)}
	 */
	public WSlider(Orientation orientation) {
		this(orientation, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		super.remove();
	}

	/**
	 * Sets the slider orientation.
	 * <p>
	 * 
	 * @see WSlider#getOrientation()
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation_ = orientation;
		this.update();
	}

	/**
	 * Returns the slider orientation.
	 * <p>
	 * 
	 * @see WSlider#setOrientation(Orientation orientation)
	 */
	public Orientation getOrientation() {
		return this.orientation_;
	}

	/**
	 * Sets the tick interval.
	 * <p>
	 * The tick interval specifies the interval for placing ticks along the
	 * slider. The interval is specified in value units (not pixel units). A
	 * value of 0 specifies an automatic tick interval, which defaults to 3
	 * ticks spanning the whole range.
	 * <p>
	 * 
	 * @see WSlider#getTickInterval()
	 * @see WSlider#setTickPosition(EnumSet tickPosition)
	 */
	public void setTickInterval(int tickInterval) {
		this.tickInterval_ = tickInterval;
		this.background_.update();
	}

	/**
	 * Returns the tick interval.
	 * <p>
	 * 
	 * @see WSlider#setTickInterval(int tickInterval)
	 */
	public int getTickInterval() {
		return this.tickInterval_;
	}

	/**
	 * Sets the tick position.
	 * <p>
	 * The tick position indicates if and where ticks are placed around the
	 * slider groove.
	 * <p>
	 * 
	 * @see WSlider#getTickPosition()
	 * @see WSlider#setTickInterval(int tickInterval)
	 */
	public void setTickPosition(EnumSet<WSlider.TickPosition> tickPosition) {
		this.tickPosition_ = EnumSet.copyOf(tickPosition);
		this.background_.update();
	}

	/**
	 * Sets the tick position.
	 * <p>
	 * Calls {@link #setTickPosition(EnumSet tickPosition)
	 * setTickPosition(EnumSet.of(tickPositio, tickPosition))}
	 */
	public final void setTickPosition(WSlider.TickPosition tickPositio,
			WSlider.TickPosition... tickPosition) {
		setTickPosition(EnumSet.of(tickPositio, tickPosition));
	}

	/**
	 * Returns the tick position.
	 * <p>
	 * 
	 * @see WSlider#setTickPosition(EnumSet tickPosition)
	 * @see WSlider#setTickInterval(int tickInterval)
	 */
	public EnumSet<WSlider.TickPosition> getTickPosition() {
		return this.tickPosition_;
	}

	/**
	 * Sets the slider value.
	 * <p>
	 * The value is automatically trimmed to the valid range (
	 * {@link WSlider#getMinimum() getMinimum()} to {@link WSlider#getMaximum()
	 * getMaximum()}).
	 * <p>
	 * 
	 * @see WSlider#getValue()
	 */
	public void setValue(int value) {
		this.value_ = Math.min(this.maximum_, Math.max(this.minimum_, value));
		this.updateSliderPosition();
	}

	/**
	 * Returns the current slider value.
	 * <p>
	 * 
	 * @see WSlider#setValue(int value)
	 */
	public int getValue() {
		return this.value_;
	}

	/**
	 * Sets the maximum value.
	 * <p>
	 * The maximum value defines the upper limit of the valid range. The lower
	 * limit and current value are automatically adjusted to remain valid.
	 * <p>
	 * 
	 * @see WSlider#getMaximum()
	 * @see WSlider#setMinimum(int minimum)
	 * @see WSlider#setRange(int minimum, int maximum)
	 */
	public void setMaximum(int maximum) {
		this.maximum_ = maximum;
		this.value_ = Math.min(this.maximum_, this.value_);
		this.minimum_ = Math.min(this.maximum_ - 1, this.minimum_);
		this.update();
	}

	/**
	 * Returns the maximum value.
	 * <p>
	 * 
	 * @see WSlider#setMaximum(int maximum)
	 */
	public int getMaximum() {
		return this.maximum_;
	}

	/**
	 * Sets the minimum value.
	 * <p>
	 * The minimum value defines the lower limit of the valid range. The upper
	 * limit and current value are automatically adjusted to remain valid.
	 * <p>
	 * 
	 * @see WSlider#getMinimum()
	 * @see WSlider#setMaximum(int maximum)
	 * @see WSlider#setRange(int minimum, int maximum)
	 */
	public void setMinimum(int minimum) {
		this.minimum_ = minimum;
		this.value_ = Math.max(this.minimum_, this.value_);
		this.maximum_ = Math.max(this.minimum_ + 1, this.maximum_);
		this.update();
	}

	/**
	 * Returns the minimum value.
	 * <p>
	 * 
	 * @see WSlider#setMinimum(int minimum)
	 */
	public int getMinimum() {
		return this.minimum_;
	}

	/**
	 * Sets the value range.
	 * <p>
	 * 
	 * @see WSlider#setMinimum(int minimum)
	 * @see WSlider#setMaximum(int maximum)
	 */
	public void setRange(int minimum, int maximum) {
		this.minimum_ = minimum;
		this.maximum_ = maximum;
		this.value_ = Math.min(this.maximum_, Math.max(this.minimum_,
				this.value_));
		this.update();
	}

	/**
	 * Signal emitted when the user has changed the value of the slider.
	 * <p>
	 * The new value is passed as the argument.
	 * <p>
	 * 
	 * @see WSlider#sliderMoved()
	 */
	public Signal1<Integer> valueChanged() {
		return this.valueChanged_;
	}

	/**
	 * Signal emitted while the user drags the slider.
	 * <p>
	 * The current dragged position is passed as the argument. Note that the
	 * slider value is not changed while dragging the slider, but only after the
	 * slider has been released.
	 * <p>
	 * 
	 * @see WSlider#valueChanged()
	 */
	public JSignal1<Integer> sliderMoved() {
		return this.sliderMoved_;
	}

	public void resize(WLength width, WLength height) {
		super.resize(width, height);
		this.background_.resize(width, height);
		this.update();
	}

	void signalConnectionsChanged() {
		super.signalConnectionsChanged();
		this.update();
	}

	protected void layoutSizeChanged(int width, int height) {
		super.resize(WLength.Auto, WLength.Auto);
		this.background_.resize(new WLength(width), new WLength(height));
		this.update();
	}

	private Orientation orientation_;
	private int tickInterval_;
	private EnumSet<WSlider.TickPosition> tickPosition_;
	private int minimum_;
	private int maximum_;
	private int value_;
	private Signal1<Integer> valueChanged_;
	private JSignal1<Integer> sliderMoved_;
	private JSignal1<Integer> sliderReleased_;
	private WContainerWidget impl_;
	private WSliderBackground background_;
	private WContainerWidget handle_;
	private JSlot mouseDownJS_;
	private JSlot mouseMovedJS_;
	private JSlot mouseUpJS_;

	private int getRange() {
		return this.maximum_ - this.minimum_;
	}

	private void create() {
		this.impl_.setStyleClass("Wt-slider");
		this.setPositionScheme(PositionScheme.Relative);
		this.impl_.addWidget(this.background_ = new WSliderBackground(this));
		this.impl_.addWidget(this.handle_ = new WContainerWidget());
		this.handle_.setPopup(true);
		this.handle_.setPositionScheme(PositionScheme.Absolute);
		if (this.orientation_ == Orientation.Horizontal) {
			this.resize(new WLength(150), new WLength(50));
		} else {
			this.resize(new WLength(50), new WLength(150));
		}
		this.handle_.mouseWentDown().addListener(this.mouseDownJS_);
		this.handle_.mouseMoved().addListener(this.mouseMovedJS_);
		this.handle_.mouseWentUp().addListener(this.mouseUpJS_);
		this.background_.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WSlider.this.onSliderClick(e1);
					}
				});
		this.sliderReleased_.addListener(this, new Signal1.Listener<Integer>() {
			public void trigger(Integer e1) {
				WSlider.this.onSliderReleased(e1);
			}
		});
		this.setLayoutSizeAware(true);
		this.update();
	}

	private void update() {
		String resourcesURL = WApplication.getResourcesUrl();
		this.background_.update();
		this.handle_.setStyleClass("handle-"
				+ (this.orientation_ == Orientation.Horizontal ? 'h' : 'v'));
		if (this.orientation_ == Orientation.Horizontal) {
			this.handle_.resize(new WLength(HANDLE_WIDTH), new WLength(
					HANDLE_HEIGHT));
			this.handle_.setOffsets(
					new WLength(this.getH().toPixels() / 2 + 2), EnumSet
							.of(Side.Top));
		} else {
			this.handle_.resize(new WLength(HANDLE_HEIGHT), new WLength(
					HANDLE_WIDTH));
			this.handle_.setOffsets(new WLength(this.getW().toPixels() / 2
					- HANDLE_HEIGHT - 2), EnumSet.of(Side.Left));
		}
		double l = (this.orientation_ == Orientation.Horizontal ? this.getW()
				: this.getH()).toPixels();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		String dir = this.orientation_ == Orientation.Horizontal ? "left"
				: "top";
		String u = this.orientation_ == Orientation.Horizontal ? "x" : "y";
		String U = this.orientation_ == Orientation.Horizontal ? "X" : "Y";
		String maxS = String.valueOf(l - HANDLE_WIDTH);
		String ppU = String.valueOf(pixelsPerUnit);
		String minimumS = String.valueOf(this.minimum_);
		String maximumS = String.valueOf(this.maximum_);
		this.mouseDownJS_
				.setJavaScript("function(obj, event) {obj.setAttribute('down', Wt3_1_7a.widgetCoordinates(obj, event)."
						+ u + "); Wt3_1_7a.cancelEvent(event);}");
		String computeD = "var objh = " + this.handle_.getJsRef() + ",objb = "
				+ this.background_.getJsRef()
				+ ",u = WT.pageCoordinates(event)." + u
				+ " - down,w = WT.widgetPageCoordinates(objb)." + u
				+ ",d = u-w;";
		this.mouseMovedJS_
				.setJavaScript("function(obj, event) {var down = obj.getAttribute('down');var WT = Wt3_1_7a;if (down != null && down != '') {"
						+ computeD
						+ "d = Math.max(0, Math.min(d, "
						+ maxS
						+ "));var v = Math.round(d/"
						+ ppU
						+ ");var intd = v*"
						+ ppU
						+ ";if (Math.abs(WT.pxself(objh, '"
						+ dir
						+ "') - intd) > 1) {objh.style."
						+ dir
						+ " = intd + 'px';"
						+ this.sliderMoved_
								.createCall(this.orientation_ == Orientation.Horizontal ? "v + "
										+ minimumS
										: maximumS + " - v") + "}}}");
		this.mouseUpJS_
				.setJavaScript("function(obj, event) {var down = obj.getAttribute('down');var WT = Wt3_1_7a;if (down != null && down != '') {"
						+ computeD
						+ "d += "
						+ String.valueOf(HANDLE_WIDTH / 2)
						+ ";"
						+ this.sliderReleased_.createCall("d")
						+ "obj.removeAttribute('down');}}");
		this.updateSliderPosition();
	}

	private void updateSliderPosition() {
		double l = (this.orientation_ == Orientation.Horizontal ? this.getW()
				: this.getH()).toPixels();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		double u = ((double) this.value_ - this.minimum_) * pixelsPerUnit;
		if (this.orientation_ == Orientation.Horizontal) {
			this.handle_.setOffsets(new WLength(u), EnumSet.of(Side.Left));
		} else {
			this.handle_.setOffsets(new WLength(this.getH().toPixels()
					- HANDLE_WIDTH - u), EnumSet.of(Side.Top));
		}
	}

	private void onSliderClick(WMouseEvent event) {
		this
				.onSliderReleased(this.orientation_ == Orientation.Horizontal ? event
						.getWidget().x
						: event.getWidget().y);
	}

	private void onSliderReleased(int u) {
		if (this.orientation_ == Orientation.Horizontal) {
			u -= HANDLE_WIDTH / 2;
		} else {
			u = (int) this.getH().toPixels() - (u + HANDLE_WIDTH / 2);
		}
		double l = (this.orientation_ == Orientation.Horizontal ? this.getW()
				: this.getH()).toPixels();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		double v = Math.max(this.minimum_, Math.min(this.maximum_,
				this.minimum_ + (int) ((double) u / pixelsPerUnit + 0.5)));
		this.sliderMoved_.trigger((int) v);
		this.setValue((int) v);
		this.valueChanged_.trigger(this.getValue());
	}

	private WLength getW() {
		return this.background_.getWidth();
	}

	private WLength getH() {
		return this.background_.getHeight();
	}

	static final int HANDLE_WIDTH = 17;
	static final int HANDLE_HEIGHT = 21;
	/**
	 * Do not render ticks.
	 */
	public static final EnumSet<WSlider.TickPosition> NoTicks = EnumSet
			.noneOf(WSlider.TickPosition.class);
	/**
	 * Render ticks on both sides.
	 */
	public static final EnumSet<WSlider.TickPosition> TicksBothSides = EnumSet
			.of(WSlider.TickPosition.TicksAbove,
					WSlider.TickPosition.TicksBelow);
}
