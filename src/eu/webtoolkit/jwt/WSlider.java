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
 * {@link WSlider#resize(WLength width, WLength height) resize()} or by a layout
 * manager. The default size is 150 x 50 pixels for a horizontal slider, and 50
 * x 150 pixels for a vertical slider.
 * <p>
 * <div align="center"> <img src="doc-files//WSlider-1.png"
 * alt="Horizontal slider with ticks on both sides.">
 * <p>
 * <strong>Horizontal slider with ticks on both sides.</strong>
 * </p>
 * </div> <h3>CSS</h3>
 * <p>
 * The non-native slider (HTML4, see
 * {@link WSlider#setNativeControl(boolean nativeControl) setNativeControl()})
 * is styled by the current CSS theme. The look can be overridden using the
 * <code>Wt-slider-[hv]</code> CSS class and the following selectors (shown here
 * for a horizontal slider, the vertical slider is equivalent but using
 * Wt-slider-v instead of Wt-slider-h:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-slider-h .Wt-slider-bg : A background sized with 5px left/right margin
 * .Wt-slider-h .Wt-e         : A west background image (5px width)
 * .Wt-slider-h .Wt-w         : An east background image (5px width)
 * .Wt-slider-h .handle       : The handle (20px width)
 * </pre>
 * 
 * </div>
 */
public class WSlider extends WFormWidget {
	/**
	 * Enumeration that specifies the location of ticks.
	 */
	public enum TickPosition {
		/**
		 * Render ticks above (horizontal slider)
		 */
		TicksAbove,
		/**
		 * Render ticks below (horizontal slider)
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
		this.preferNative_ = false;
		this.changed_ = false;
		this.changedConnected_ = false;
		this.minimum_ = 0;
		this.maximum_ = 99;
		this.value_ = 0;
		this.valueChanged_ = new Signal1<Integer>(this);
		this.sliderMoved_ = new JSignal1<Integer>(this, "moved") {
		};
		this.paintedSlider_ = null;
		this.resize(new WLength(150), new WLength(50));
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
		this.preferNative_ = false;
		this.changed_ = false;
		this.changedConnected_ = false;
		this.minimum_ = 0;
		this.maximum_ = 99;
		this.value_ = 0;
		this.valueChanged_ = new Signal1<Integer>(this);
		this.sliderMoved_ = new JSignal1<Integer>(this, "moved") {
		};
		this.paintedSlider_ = null;
		if (orientation == Orientation.Horizontal) {
			this.resize(new WLength(150), new WLength(50));
		} else {
			this.resize(new WLength(50), new WLength(150));
		}
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
	 * Configures whether a native HTML5 control should be used.
	 * <p>
	 * When <code>native</code>, the new &quot;range&quot; input element,
	 * specified by HTML5 and when implemented by the browser, is used rather
	 * than the built-in element. A native control is styled by the browser
	 * (usually in sync with the OS) rather than through the theme chosen.
	 * Settings like tick interval and tick position are ignored.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Vertically oriented sliders are in theory supported by
	 * the HTML5 input element, but in practice are usually not rendered
	 * correctly by the browser. </i>
	 * </p>
	 */
	public void setNativeControl(boolean nativeControl) {
		this.preferNative_ = nativeControl;
	}

	/**
	 * Returns whether a native HTML5 control is used.
	 * <p>
	 * Taking into account the preference for a native control, configured using
	 * {@link WSlider#setNativeControl(boolean nativeControl)
	 * setNativeControl()}, this method returns whether a native control is
	 * actually being used.
	 */
	public boolean isNativeControl() {
		if (this.preferNative_) {
			WEnvironment env = WApplication.getInstance().getEnvironment();
			if (env.agentIsChrome()
					&& env.getAgent().getValue() >= WEnvironment.UserAgent.Chrome5
							.getValue()
					|| env.agentIsSafari()
					&& env.getAgent().getValue() >= WEnvironment.UserAgent.Safari4
							.getValue()
					|| env.agentIsOpera()
					&& env.getAgent().getValue() >= WEnvironment.UserAgent.Opera10
							.getValue()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the slider orientation.
	 * <p>
	 * 
	 * @see WSlider#getOrientation()
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation_ = orientation;
		if (this.paintedSlider_ != null) {
			this.paintedSlider_.updateState();
		}
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
		if (this.paintedSlider_ != null) {
			this.paintedSlider_.updateState();
		}
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
		if (this.paintedSlider_ != null) {
			this.paintedSlider_.updateState();
		}
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
		this.update();
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
		if (this.paintedSlider_ != null) {
			this.paintedSlider_.sliderResized(width, height);
		}
	}

	void signalConnectionsChanged() {
		super.signalConnectionsChanged();
		this.update();
	}

	protected void layoutSizeChanged(int width, int height) {
		super.resize(WLength.Auto, WLength.Auto);
		if (this.paintedSlider_ != null) {
			this.paintedSlider_.sliderResized(new WLength(width), new WLength(
					height));
		}
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			boolean useNative = this.isNativeControl();
			if (!useNative) {
				if (!(this.paintedSlider_ != null)) {
					this
							.addChild(this.paintedSlider_ = new PaintedSlider(
									this));
					this.paintedSlider_.sliderResized(this.getWidth(), this
							.getHeight());
				}
			} else {
				if (this.paintedSlider_ != null)
					this.paintedSlider_.remove();
				this.paintedSlider_ = null;
			}
			this.setLayoutSizeAware(!useNative);
			this.setFormObject(useNative);
		}
		super.render(flags);
	}

	void updateDom(DomElement element, boolean all) {
		if (this.paintedSlider_ != null) {
			this.paintedSlider_.doUpdateDom(element, all);
		} else {
			if (all || this.changed_) {
				element.setAttribute("type", "range");
				element.setProperty(Property.PropertyValue, String
						.valueOf(this.value_));
				element.setAttribute("min", String.valueOf(this.minimum_));
				element.setAttribute("max", String.valueOf(this.maximum_));
				if (!this.changedConnected_
						&& (this.valueChanged_.isConnected() || this.sliderMoved_
								.isConnected())) {
					this.changedConnected_ = true;
					this.changed().addListener(this, new Signal.Listener() {
						public void trigger() {
							WSlider.this.onChange();
						}
					});
				}
				this.changed_ = false;
			}
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return this.paintedSlider_ != null ? DomElementType.DomElement_DIV
				: DomElementType.DomElement_INPUT;
	}

	void setFormData(WObject.FormData formData) {
		if (this.changed_) {
			return;
		}
		if (!(formData.values.length == 0)) {
			String value = formData.values[0];
			try {
				this.value_ = Integer.parseInt(value);
			} catch (NumberFormatException e) {
			}
		}
	}

	private Orientation orientation_;
	private int tickInterval_;
	private EnumSet<WSlider.TickPosition> tickPosition_;
	private boolean preferNative_;
	private boolean changed_;
	private boolean changedConnected_;
	private int minimum_;
	private int maximum_;
	private int value_;
	private Signal1<Integer> valueChanged_;
	private JSignal1<Integer> sliderMoved_;
	private PaintedSlider paintedSlider_;

	private void update() {
		if (this.paintedSlider_ != null) {
			this.paintedSlider_.updateState();
		} else {
			this.changed_ = true;
			this.repaint();
		}
	}

	private void onChange() {
		this.valueChanged_.trigger(this.value_);
		this.sliderMoved_.trigger(this.value_);
	}

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
