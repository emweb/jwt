package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A horizontal or vertical slider control
 * 
 * 
 * A slider allows the user to specify an integer value within a particular
 * range using a visual slider.
 * <p>
 * The slider must be sized explicitly using
 * {@link WWidget#resize(WLength width, WLength height)}. The default size is
 * 150 x 50 pixels for a horizontal slider, and 50 x 150 pixels for a vertical
 * slider.
 * <p>
 * Usage example:
 * <p>
 * <div align="center"> <img src="/WSlider-1.png"
 * alt="Horizontal slider with ticks on both sides.">
 * <p>
 * <strong>Horizontal slider with ticks on both sides.</strong>
 * </p>
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

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a default horizontal slider.
	 * 
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
		this.mouseDownJS_ = new JSlot();
		this.mouseMovedJS_ = new JSlot();
		this.mouseUpJS_ = new JSlot();
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.create();
	}

	public WSlider() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a default slider of the given orientation.
	 * 
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
		this.mouseDownJS_ = new JSlot();
		this.mouseMovedJS_ = new JSlot();
		this.mouseUpJS_ = new JSlot();
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.create();
	}

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
	 * Change the slider orientation.
	 * 
	 * @see WSlider#getOrientation()
	 */
	public void setOrientation(Orientation orientation) {
		this.orientation_ = orientation;
		this.update();
	}

	/**
	 * Returns the slider orientation.
	 * 
	 * @see WSlider#setOrientation(Orientation orientation)
	 */
	public Orientation getOrientation() {
		return this.orientation_;
	}

	/**
	 * Change the tick interval.
	 * 
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
	 * 
	 * @see WSlider#setTickInterval(int tickInterval)
	 */
	public int getTickInterval() {
		return this.tickInterval_;
	}

	/**
	 * Set the tick position.
	 * 
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

	public final void setTickPosition(WSlider.TickPosition tickPositio,
			WSlider.TickPosition... tickPosition) {
		setTickPosition(EnumSet.of(tickPositio, tickPosition));
	}

	/**
	 * Return the tick position.
	 * 
	 * @see WSlider#setTickPosition(EnumSet tickPosition)
	 * @see WSlider#setTickInterval(int tickInterval)
	 */
	public EnumSet<WSlider.TickPosition> getTickPosition() {
		return this.tickPosition_;
	}

	/**
	 * Change the slider value.
	 * 
	 * The value is automatically trimmed to the valid range (
	 * {@link WSlider#getMinimum()} to {@link WSlider#getMaximum()}).
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
	 * 
	 * @see WSlider#setValue(int value)
	 */
	public int getValue() {
		return this.value_;
	}

	/**
	 * Set the maximum value.
	 * 
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
	 * 
	 * @see WSlider#setMaximum(int maximum)
	 */
	public int getMaximum() {
		return this.maximum_;
	}

	/**
	 * Set the minimum value.
	 * 
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
	 * 
	 * @see WSlider#setMinimum(int minimum)
	 */
	public int getMinimum() {
		return this.minimum_;
	}

	/**
	 * Set the value range.
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
	 * 
	 * The new value is passed as the argument.
	 */
	public Signal1<Integer> valueChanged() {
		return this.valueChanged_;
	}

	public void resize(WLength width, WLength height) {
		super.resize(width, height);
		this.background_.resize(width, height);
		this.update();
	}

	private Orientation orientation_;
	private int tickInterval_;
	private EnumSet<WSlider.TickPosition> tickPosition_;
	private int minimum_;
	private int maximum_;
	private int value_;
	private Signal1<Integer> valueChanged_;
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
		this.update();
	}

	private void update() {
		String resourcesURL = WApplication.resourcesUrl();
		this.background_.update();
		this.handle_.getDecorationStyle().setBackgroundImage(
				resourcesURL
						+ "slider-thumb-"
						+ (this.orientation_ == Orientation.Horizontal ? 'h'
								: 'v') + ".gif");
		if (this.orientation_ == Orientation.Horizontal) {
			this.handle_.resize(new WLength(HANDLE_WIDTH), new WLength(
					HANDLE_HEIGHT));
			this.handle_.setOffsets(new WLength(
					this.getHeight().getValue() / 2 + 2), EnumSet.of(Side.Top));
		} else {
			this.handle_.resize(new WLength(HANDLE_HEIGHT), new WLength(
					HANDLE_WIDTH));
			this.handle_.setOffsets(new WLength(this.getWidth().getValue() / 2
					- HANDLE_HEIGHT - 2), EnumSet.of(Side.Left));
		}
		double l = this.orientation_ == Orientation.Horizontal ? this
				.getWidth().getValue() : this.getHeight().getValue();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		String dir = this.orientation_ == Orientation.Horizontal ? "left"
				: "top";
		String u = this.orientation_ == Orientation.Horizontal ? "x" : "y";
		String maxS = String.valueOf(l - HANDLE_WIDTH);
		String ppU = String.valueOf(pixelsPerUnit);
		this.mouseDownJS_
				.setJavaScript("function(obj, event) {  obj.setAttribute('down', Wt2_99_2.widgetCoordinates(obj, event)."
						+ u + "); Wt2_99_2.cancelEvent(event);}");
		this.mouseMovedJS_
				.setJavaScript("function(obj, event) {  var down = obj.getAttribute('down');  if (down != null && down != '') {    var objh = "
						+ this.handle_.getJsRef()
						+ ";    var objb = "
						+ this.background_.getJsRef()
						+ ";    var u = Wt2_99_2.pageCoordinates(event)."
						+ u
						+ " - down;    var w = Wt2_99_2.widgetPageCoordinates(objb)."
						+ u
						+ ";    var d = u-w;    d = (d<0?0:(d>"
						+ maxS
						+ "?"
						+ maxS
						+ ":d));    d = Math.round(d/"
						+ ppU
						+ ")*"
						+ ppU
						+ ";    objh.style."
						+ dir
						+ " = d + 'px';  }}");
		this.mouseUpJS_
				.setJavaScript("function(obj, event) {  var down = obj.getAttribute('down');  if (down != null && down != '') {    obj.removeAttribute('down');    var objb = "
						+ this.background_.getJsRef()
						+ ";    objb.onclick(event);  }}");
		this.updateSliderPosition();
	}

	private void updateSliderPosition() {
		double l = this.orientation_ == Orientation.Horizontal ? this
				.getWidth().getValue() : this.getHeight().getValue();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		double u = ((double) this.value_ - this.minimum_) * pixelsPerUnit;
		this.handle_.setOffsets(new WLength(u),
				this.orientation_ == Orientation.Horizontal ? Side.Left
						: Side.Top);
	}

	private void onSliderClick(WMouseEvent event) {
		double l = this.orientation_ == Orientation.Horizontal ? this
				.getWidth().getValue() : this.getHeight().getValue();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		double u = this.orientation_ == Orientation.Horizontal ? event
				.getWidget().x : event.getWidget().y;
		u -= HANDLE_WIDTH / 2;
		this.setValue(this.minimum_ + (int) (u / pixelsPerUnit + 0.5));
		this.valueChanged_.trigger(this.getValue());
	}

	static final int HANDLE_WIDTH = 17;
	static final int HANDLE_HEIGHT = 21;
	/**
	 * Render ticks on both sides.
	 */
	public static final EnumSet<WSlider.TickPosition> TicksBothSides = EnumSet
			.of(WSlider.TickPosition.TicksAbove,
					WSlider.TickPosition.TicksBelow);
}
