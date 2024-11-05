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
 * A horizontal or vertical slider control.
 *
 * <p>A slider allows the user to specify an integer value within a particular range using a visual
 * slider.
 *
 * <p>The slider must be sized explicitly using {@link WSlider#resize(WLength width, WLength height)
 * resize()} or by a layout manager. The default size is 150 x 50 pixels for a horizontal slider,
 * and 50 x 150 pixels for a vertical slider.
 *
 * <p><div align="center"> <img src="doc-files/WSlider-1.png">
 *
 * <p><strong>Horizontal slider with ticks on both sides.</strong> </div>
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>The non-native slider (HTML4, see {@link WSlider#setNativeControl(boolean nativeControl)
 * setNativeControl()}) is styled by the current CSS theme.
 */
public class WSlider extends WFormWidget {
  private static Logger logger = LoggerFactory.getLogger(WSlider.class);

  /** Enumeration that specifies the location of ticks. */
  public enum TickPosition {
    /** Render ticks above (horizontal slider) */
    TicksAbove,
    /** Render ticks below (horizontal slider) */
    TicksBelow;

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return ordinal();
    }
  }

  public static final EnumSet<WSlider.TickPosition> NoTicks =
      EnumSet.noneOf(WSlider.TickPosition.class);
  public static final EnumSet<WSlider.TickPosition> TicksBothSides =
      EnumSet.of(WSlider.TickPosition.TicksAbove, WSlider.TickPosition.TicksBelow);
  /**
   * Creates a default horizontal slider.
   *
   * <p>The slider shows no ticks, has a range from 0 to 99, and has tickInterval of 0 (defaulting
   * to three ticks over the whole range).
   *
   * <p>The initial value is 0.
   */
  public WSlider(WContainerWidget parentContainer) {
    super();
    this.orientation_ = Orientation.Horizontal;
    this.tickInterval_ = 0;
    this.tickPosition_ = EnumSet.noneOf(WSlider.TickPosition.class);
    this.tickLength_ = new WLength();
    this.preferNative_ = false;
    this.changed_ = false;
    this.changedConnected_ = false;
    this.inputConnected_ = false;
    this.handleWidth_ = 20;
    this.minimum_ = 0;
    this.maximum_ = 99;
    this.value_ = 0;
    this.step_ = 1;
    this.valueChanged_ = new Signal1<Integer>();
    this.sliderMoved_ = new JSignal1<Integer>(this, "moved", true) {};
    this.paintedSlider_ = null;
    this.tickList_ = null;
    this.resize(new WLength(150), new WLength(50));
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a default horizontal slider.
   *
   * <p>Calls {@link #WSlider(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WSlider() {
    this((WContainerWidget) null);
  }
  /**
   * Creates a default slider of the given orientation.
   *
   * <p>The slider shows no ticks, has a range from 0 to 99, and has tickInterval of 0 (defaulting
   * to three ticks over the whole range).
   *
   * <p>The initial value is 0.
   */
  public WSlider(Orientation orientation, WContainerWidget parentContainer) {
    super();
    this.orientation_ = orientation;
    this.tickInterval_ = 0;
    this.tickPosition_ = EnumSet.noneOf(WSlider.TickPosition.class);
    this.tickLength_ = new WLength();
    this.preferNative_ = false;
    this.changed_ = false;
    this.changedConnected_ = false;
    this.inputConnected_ = false;
    this.handleWidth_ = 20;
    this.minimum_ = 0;
    this.maximum_ = 99;
    this.value_ = 0;
    this.step_ = 1;
    this.valueChanged_ = new Signal1<Integer>();
    this.sliderMoved_ = new JSignal1<Integer>(this, "moved", true) {};
    this.paintedSlider_ = null;
    this.tickList_ = null;
    if (orientation == Orientation.Horizontal) {
      this.resize(new WLength(150), new WLength(50));
    } else {
      this.resize(new WLength(50), new WLength(150));
    }
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a default slider of the given orientation.
   *
   * <p>Calls {@link #WSlider(Orientation orientation, WContainerWidget parentContainer)
   * this(orientation, (WContainerWidget)null)}
   */
  public WSlider(Orientation orientation) {
    this(orientation, (WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    {
      WWidget oldWidget = this.paintedSlider_;
      this.paintedSlider_ = null;
      {
        WWidget toRemove = this.manageWidget(oldWidget, this.paintedSlider_);
        if (toRemove != null) toRemove.remove();
      }
    }
    {
      WWidget oldWidget = this.tickList_;
      this.tickList_ = null;
      {
        WWidget toRemove = this.manageWidget(oldWidget, this.tickList_);
        if (toRemove != null) toRemove.remove();
      }
    }
    super.remove();
  }
  /**
   * Configures whether a native HTML5 control should be used.
   *
   * <p>When <code>native</code>, the new &quot;range&quot; input element, specified by HTML5 and
   * when implemented by the browser, is used rather than the built-in element. A native control is
   * styled by the browser (usually in sync with the OS) rather than through the theme chosen.
   * Settings like tick interval and tick position are ignored.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Vertically oriented sliders are in theory supported by the HTML5 input
   * element, but in practice are usually not rendered correctly by the browser. </i>
   */
  public void setNativeControl(boolean nativeControl) {
    this.preferNative_ = nativeControl;
  }
  /**
   * Returns whether a native HTML5 control is used.
   *
   * <p>Taking into account the preference for a native control, configured using {@link
   * WSlider#setNativeControl(boolean nativeControl) setNativeControl()}, this method returns
   * whether a native control is actually being used.
   */
  public boolean isNativeControl() {
    return this.preferNative_;
  }
  /**
   * Sets the slider orientation.
   *
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
   *
   * <p>
   *
   * @see WSlider#setOrientation(Orientation orientation)
   */
  public Orientation getOrientation() {
    return this.orientation_;
  }
  /**
   * Sets the tick interval.
   *
   * <p>The tick interval specifies the interval for placing ticks along the slider. The interval is
   * specified in value units (not pixel units). A value of 0 specifies an automatic tick interval,
   * which defaults to 3 ticks spanning the whole range.
   *
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
   *
   * <p>
   *
   * @see WSlider#setTickInterval(int tickInterval)
   */
  public int getTickInterval() {
    return this.tickInterval_;
  }
  /**
   * Sets the tick position.
   *
   * <p>The tick position indicates if and where ticks are placed around the slider groove.
   *
   * <p>This function has no effect if the native widget is used.
   *
   * <p>
   *
   * @see WSlider#getTickPosition()
   * @see WSlider#setTickInterval(int tickInterval)
   */
  public void setTickPosition(EnumSet<WSlider.TickPosition> tickPosition) {
    if (this.isNativeControl()) {
      logger.warn(
          new StringWriter()
              .append("setTickLength(): Cannot set the tick length of a native widget.")
              .toString());
      return;
    }
    this.tickPosition_ = EnumSet.copyOf(tickPosition);
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.updateState();
    }
  }
  /**
   * Sets the tick position.
   *
   * <p>Calls {@link #setTickPosition(EnumSet tickPosition) setTickPosition(EnumSet.of(tickPositio,
   * tickPosition))}
   */
  public final void setTickPosition(
      WSlider.TickPosition tickPositio, WSlider.TickPosition... tickPosition) {
    setTickPosition(EnumSet.of(tickPositio, tickPosition));
  }
  /**
   * Returns the tick position.
   *
   * <p>
   *
   * @see WSlider#setTickPosition(EnumSet tickPosition)
   * @see WSlider#setTickInterval(int tickInterval)
   */
  public EnumSet<WSlider.TickPosition> getTickPosition() {
    return this.tickPosition_;
  }
  /**
   * Sets the length of the ticks to be drawn.
   *
   * <p>This length will be either the width or height when the slider is oriented vertically or
   * horizontally respectively.
   *
   * <p>This function has no effect if the native widget is used.
   *
   * <p>
   *
   * @see WSlider#getTickLength()
   */
  public void setTickLength(final WLength length) {
    if (this.isNativeControl()) {
      logger.warn(
          new StringWriter()
              .append("setTickLength(): Cannot set the tick length of a native widget.")
              .toString());
      return;
    }
    this.tickLength_ = length;
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.updateState();
    }
  }
  /**
   * Returns the tick length.
   *
   * <p>
   *
   * @see WSlider#setTickLength(WLength length)
   */
  public WLength getTickLength() {
    return this.tickLength_;
  }
  /**
   * Sets the slider value.
   *
   * <p>The value is automatically trimmed to the valid range ({@link WSlider#getMinimum()
   * getMinimum()} to {@link WSlider#getMaximum() getMaximum()}).
   *
   * <p>
   *
   * @see WSlider#getValue()
   */
  public void setValue(int value) {
    this.value_ = Math.min(this.maximum_, Math.max(this.minimum_, value));
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.updateSliderPosition();
    } else {
      this.update();
      this.onChange();
    }
  }
  /**
   * Returns the current slider value.
   *
   * <p>
   *
   * @see WSlider#setValue(int value)
   */
  public int getValue() {
    return this.value_;
  }
  /**
   * Sets the maximum value.
   *
   * <p>The maximum value defines the upper limit of the valid range. The lower limit and current
   * value are automatically adjusted to remain valid.
   *
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
   * <p>
   *
   * @see WSlider#setMaximum(int maximum)
   */
  public int getMaximum() {
    return this.maximum_;
  }
  /**
   * Sets the minimum value.
   *
   * <p>The minimum value defines the lower limit of the valid range. The upper limit and current
   * value are automatically adjusted to remain valid.
   *
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
   * <p>
   *
   * @see WSlider#setMinimum(int minimum)
   */
  public int getMinimum() {
    return this.minimum_;
  }
  /**
   * Sets the value range.
   *
   * <p>
   *
   * @see WSlider#setMinimum(int minimum)
   * @see WSlider#setMaximum(int maximum)
   */
  public void setRange(int minimum, int maximum) {
    this.minimum_ = minimum;
    this.maximum_ = maximum;
    this.value_ = Math.min(this.maximum_, Math.max(this.minimum_, this.value_));
    this.update();
  }
  /**
   * Return the step value.
   *
   * <p>The default value of the step is <code>1</code>.
   *
   * <p>
   *
   * @see WSlider#setStep(int step)
   */
  public int getStep() {
    return this.step_;
  }
  /**
   * Sets the step value.
   *
   * <p>This is a positive integer value that indicates by which step the slider moves between the
   * minimum and maximum.
   *
   * <p>It is not necessary that the slider&apos;s range can be neatly divided by the step value.
   * Meaning a range of 50 (0 - 50), with a step of 7, is possible, but will never reach the maximum
   * value.
   *
   * <p>
   *
   * @see WSlider#getStep()
   */
  public void setStep(int step) {
    if (step <= 0) {
      logger.warn(
          new StringWriter()
              .append("setStep() is called with a bad step value. This must be greater than 0.")
              .toString());
      return;
    }
    this.step_ = step;
    this.value_ = this.getClosestNumberByStep(this.getValue(), step);
    this.update();
    this.onChange();
  }
  /**
   * Signal emitted when the user has changed the value of the slider.
   *
   * <p>The new value is passed as the argument.
   *
   * <p>
   *
   * @see WSlider#sliderMoved()
   */
  public Signal1<Integer> valueChanged() {
    return this.valueChanged_;
  }
  /**
   * Signal emitted while the user drags the slider.
   *
   * <p>The current dragged position is passed as the argument. Note that the slider value is not
   * changed while dragging the slider, but only after the slider has been released.
   *
   * <p>
   *
   * @see WSlider#valueChanged()
   */
  public JSignal1<Integer> sliderMoved() {
    return this.sliderMoved_;
  }
  /**
   * Sets the slider handle width.
   *
   * <p>This sets the width for the handle, which is needed to accurately position the handle.
   *
   * <p>The default value is 20 pixels.
   */
  public void setHandleWidth(int handleWidth) {
    this.handleWidth_ = handleWidth;
  }
  /**
   * Returns the handle width.
   *
   * <p>
   *
   * @see WSlider#setHandleWidth(int handleWidth)
   */
  public int getHandleWidth() {
    return this.handleWidth_;
  }
  /**
   * Signal emitted when input was captured.
   *
   * <p>The signal is only emitted when keyboard input (arrow keys) are captured.
   */
  public EventSignal input() {
    return this.voidEventSignal(INPUT_SIGNAL, true);
  }

  public void setDisabled(boolean disabled) {
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.setDisabled(disabled);
    }
    super.setDisabled(disabled);
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.updateState();
    }
  }

  public void resize(final WLength width, final WLength height) {
    super.resize(width, height);
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.sliderResized(width, height);
    }
  }

  public String getValueText() {
    return String.valueOf(this.value_);
  }

  public void setValueText(final String value) {
    try {
      this.value_ = Integer.parseInt(value);
    } catch (final RuntimeException e) {
    }
  }

  public void enableAjax() {
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.connectSlots();
    } else {
      super.enableAjax();
    }
  }

  protected static String INPUT_SIGNAL = "input";
  /**
   * Paints a slider ticks (for a non-native widget)
   *
   * <p>The default implementation draws ticks taking into account the the tickPosition.
   *
   * <p>The mid point for the tick should be at position (x, y). The <code>value</code> that
   * corresponds to the tick is also passed.
   */
  protected void paintTick(final WPainter painter, int value, int x, int y) {
    if (!this.tickPosition_.isEmpty()) {
      int h = 0;
      if (this.orientation_ == Orientation.Horizontal) {
        h = (int) painter.getDevice().getHeight().toPixels();
      } else {
        h = (int) painter.getDevice().getWidth().toPixels();
      }
      WPen pen = new WPen();
      pen.setColor(new WColor(0xd7, 0xd7, 0xd7));
      pen.setCapStyle(PenCapStyle.Flat);
      pen.setWidth(new WLength(1));
      painter.setPen(pen);
      int y1 = h / 4;
      int y2 = h / 2 - 4;
      int y3 = h / 2 + 4;
      int y4 = h - h / 4;
      if (!this.getTickLength().isAuto()) {
        y1 = y2 - (int) this.getTickLength().toPixels();
        y4 = y3 + (int) this.getTickLength().toPixels();
      }
      switch (this.orientation_) {
        case Horizontal:
          if (this.tickPosition_.contains(WSlider.TickPosition.TicksAbove)) {
            painter.drawLine(x + 0.5, y1, x + 0.5, y2);
          }
          if (this.tickPosition_.contains(WSlider.TickPosition.TicksBelow)) {
            painter.drawLine(x + 0.5, y3, x + 0.5, y4);
          }
          break;
        case Vertical:
          if (this.tickPosition_.contains(WSlider.TickPosition.TicksAbove)) {
            painter.drawLine(y1, y + 0.5, y2, y + 0.5);
          }
          if (this.tickPosition_.contains(WSlider.TickPosition.TicksBelow)) {
            painter.drawLine(y3, y + 0.5, y4, y + 0.5);
          }
      }
    }
  }
  /**
   * Creates the handle (for a non-native widget)
   *
   * <p>The default implementation creates a container widget. You may want to specialize this
   * function if you want to have more control on the handle appearance or if you want to associate
   * with the handle a tooltip or other information (e.g. a popup balloon).
   */
  protected WInteractWidget getCreateHandle() {
    return new WContainerWidget();
  }

  void signalConnectionsChanged() {
    super.signalConnectionsChanged();
    this.update();
  }

  protected void layoutSizeChanged(int width, int height) {
    super.resize(WLength.Auto, WLength.Auto);
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.sliderResized(new WLength(width), new WLength(height));
    }
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full)) {
      boolean useNative = this.isNativeControl();
      if (!useNative) {
        if (!(this.paintedSlider_ != null)) {
          PaintedSlider paintedSlider = new PaintedSlider(this);
          {
            WWidget oldWidget = this.paintedSlider_;
            this.paintedSlider_ = paintedSlider;
            {
              WWidget toRemove = this.manageWidget(oldWidget, this.paintedSlider_);
              if (toRemove != null) toRemove.remove();
            }
          }
          this.paintedSlider_.sliderResized(this.getWidth(), this.getHeight());
        }
      } else {
        TickList tickList = new TickList(this);
        {
          WWidget oldWidget = this.tickList_;
          this.tickList_ = tickList;
          {
            WWidget toRemove = this.manageWidget(oldWidget, this.tickList_);
            if (toRemove != null) toRemove.remove();
          }
        }
      }
      this.setLayoutSizeAware(!useNative);
      this.setFormObject(useNative);
    }
    super.render(flags);
  }

  void updateDom(final DomElement element, boolean all) {
    if (this.preferNative_) {
      if (this.getOrientation() == Orientation.Horizontal) {
        element.removeProperty(Property.Orient);
        element.removeProperty(Property.StyleWebkitAppearance);
      } else {
        element.setProperty(Property.Orient, "vertical");
        element.setProperty(Property.StyleWebkitAppearance, "slider-vertical");
      }
      if (this.tickList_ != null) {
        this.tickList_.doUpdateDom(element, all);
        element.setAttribute("list", this.getId() + "dl");
      }
      element.setAttribute("step", String.valueOf(this.getStep()));
      element.setAttribute("value", String.valueOf(this.getValue()));
    }
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.doUpdateDom(element, all);
    } else {
      if (all || this.changed_) {
        element.setAttribute("type", "range");
        element.setProperty(Property.Value, String.valueOf(this.value_));
        element.setAttribute("min", String.valueOf(this.minimum_));
        element.setAttribute("max", String.valueOf(this.maximum_));
        if (!this.changedConnected_
            && (this.valueChanged_.isConnected() || this.sliderMoved_.isConnected())) {
          this.changedConnected_ = true;
          this.changed()
              .addListener(
                  this,
                  () -> {
                    WSlider.this.onChange();
                  });
        } else {
          if (!this.inputConnected_
              && (this.valueChanged_.isConnected() || this.sliderMoved_.isConnected())) {
            this.inputConnected_ = true;
            this.input()
                .addListener(
                    this,
                    () -> {
                      WSlider.this.onChange();
                    });
          }
        }
        this.changed_ = false;
      }
    }
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    return this.paintedSlider_ != null ? DomElementType.DIV : DomElementType.INPUT;
  }

  protected void setFormData(final WObject.FormData formData) {
    if (this.changed_ || this.isReadOnly()) {
      return;
    }
    if (!(formData.values.length == 0)) {
      final String value = formData.values[0];
      try {
        this.value_ = Integer.parseInt(value);
      } catch (final RuntimeException e) {
      }
    }
  }

  private Orientation orientation_;
  private int tickInterval_;
  private EnumSet<WSlider.TickPosition> tickPosition_;
  private WLength tickLength_;
  private boolean preferNative_;
  private boolean changed_;
  private boolean changedConnected_;
  private boolean inputConnected_;
  private int handleWidth_;
  private int minimum_;
  private int maximum_;
  private int value_;
  private int step_;
  private Signal1<Integer> valueChanged_;
  private JSignal1<Integer> sliderMoved_;
  private PaintedSlider paintedSlider_;
  private TickList tickList_;

  private void update() {
    if (this.paintedSlider_ != null) {
      this.paintedSlider_.updateState();
    } else {
      this.changed_ = true;
      this.repaint();
    }
  }

  private void onChange() {
    this.updateSliderProperties();
    this.valueChanged_.trigger(this.value_);
    this.sliderMoved_.trigger(this.value_);
  }

  private void updateSliderProperties() {
    if (this.preferNative_) {
      this.scheduleRender();
    }
  }

  private int getClosestNumberByStep(int value, int step) {
    int absValue = Math.abs(value);
    int sign = value < 0 ? -1 : 1;
    int lowDelta = absValue - absValue % step;
    int highDelta = lowDelta + step;
    if (absValue - lowDelta < highDelta - absValue) {
      return lowDelta * sign;
    } else {
      return highDelta * sign;
    }
  }
}
