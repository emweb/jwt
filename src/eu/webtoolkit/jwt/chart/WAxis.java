/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

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
 * Class which represents an axis of a cartesian chart.
 * <p>
 * 
 * A cartesian chart has two or three axes: an X axis ({@link Axis#XAxis XAxis}
 * ), a Y axis ({@link Axis#YAxis YAxis}) and optionally a second Y axis (
 * {@link Axis#Y2Axis Y2Axis}). Each of the up to three axes in a cartesian
 * chart has a unique {@link WAxis#getId() getId()} that identifies which of
 * these three axes it is in the enclosing chart().
 * <p>
 * Use {@link WAxis#setVisible(boolean visible) setVisible()} to change the
 * visibility of an axis, {@link WAxis#setGridLinesEnabled(boolean enabled)
 * setGridLinesEnabled()} to show grid lines for an axis. The pen styles for
 * rendering the axis or grid lines may be changed using
 * {@link WAxis#setPen(WPen pen) setPen()} and
 * {@link WAxis#setGridLinesPen(WPen pen) setGridLinesPen()}. A margin between
 * the axis and the main plot area may be configured using
 * {@link WAxis#setMargin(int pixels) setMargin()}.
 * <p>
 * By default, the axis will automatically adjust its range so that all data
 * will be visible. You may manually specify a range using
 * {@link WAxis#setMinimum(double minimum) setMinimum()}, setMaximum or
 * {@link WAxis#setRange(double minimum, double maximum) setRange()}. The
 * interval between labels is by default automatically adjusted depending on the
 * axis length and the range, but may be manually specified using
 * {@link WAxis#setLabelInterval(double labelInterval) setLabelInterval()}.
 * <p>
 * The axis has support for being &quot;broken&quot;, to support displaying data
 * with a few outliers which would otherwise swamp the chart. This is not done
 * automatically, but instead you need to use
 * {@link WAxis#setBreak(double minimum, double maximum) setBreak()} to specify
 * the value range that needs to be omitted from the axis. The omission is
 * rendered in the axis and in bars that cross the break.
 * <p>
 * The labels are shown using a &quot;%.4g&quot; format string for numbers, and
 * a suitable format for {@link AxisScale#DateScale DateScale} or
 * {@link AxisScale#DateTimeScale DateTimeScale} scales, based on heuristics.
 * The format may be customized using
 * {@link WAxis#setLabelFormat(CharSequence format) setLabelFormat()}. The angle
 * of the label text may be changed using
 * {@link WAxis#setLabelAngle(double angle) setLabelAngle()}. By default, all
 * labels are printed horizontally.
 * <p>
 * 
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
 */
public class WAxis {
	private static Logger logger = LoggerFactory.getLogger(WAxis.class);

	/**
	 * Constant which indicates automatic minimum calculation.
	 * <p>
	 * 
	 * @see WAxis#setMinimum(double minimum)
	 */
	public static final double AUTO_MINIMUM = -Double.MAX_VALUE;
	/**
	 * Constant which indicates automatic maximum calculation.
	 * <p>
	 * 
	 * @see WAxis#setMaximum(double maximum)
	 */
	public static final double AUTO_MAXIMUM = Double.MAX_VALUE;

	/**
	 * Returns the axis id.
	 * <p>
	 * 
	 * @see WCartesianChart#getAxis(Axis axis)
	 */
	public Axis getId() {
		return this.axis_;
	}

	/**
	 * Sets whether this axis is visible.
	 * <p>
	 * Changes whether the axis is displayed, including ticks and labels. The
	 * rendering of the grid lines is controlled seperately by
	 * {@link WAxis#setGridLinesEnabled(boolean enabled) setGridLinesEnabled()}.
	 * <p>
	 * The default value is true for the X axis and first Y axis, but false for
	 * the second Y axis.
	 * <p>
	 * 
	 * @see WAxis#setGridLinesEnabled(boolean enabled)
	 */
	public void setVisible(boolean visible) {
		if (!ChartUtils.equals(this.visible_, visible)) {
			this.visible_ = visible;
			update();
		}
		;
	}

	/**
	 * Returns whether this axis is visible.
	 * <p>
	 * 
	 * @see WAxis#setVisible(boolean visible)
	 */
	public boolean isVisible() {
		return this.visible_;
	}

	/**
	 * Sets the axis location.
	 * <p>
	 * Configures the location of the axis, relative to values on the other axis
	 * (i.e. Y values for the X axis, and X values for the Y axis).
	 * <p>
	 * The default value is {@link AxisValue#MinimumValue}.
	 * <p>
	 * 
	 * @see WAxis#getLocation()
	 */
	public void setLocation(AxisValue location) {
		if (!ChartUtils.equals(this.location_, location)) {
			this.location_ = location;
			update();
		}
		;
	}

	/**
	 * Returns the axis location.
	 * <p>
	 * 
	 * @see WAxis#setLocation(AxisValue location)
	 */
	public AxisValue getLocation() {
		return this.location_;
	}

	/**
	 * Sets the scale of the axis.
	 * <p>
	 * For the X scale in a {@link ChartType#CategoryChart CategoryChart}, the
	 * scale should be left unchanged to {@link AxisScale#CategoryScale
	 * CategoryScale}.
	 * <p>
	 * For all other axes, the default value is {@link AxisScale#LinearScale
	 * LinearScale}, but this may be changed to {@link AxisScale#LogScale
	 * LogScale} or {@link AxisScale#DateScale DateScale}.
	 * {@link AxisScale#DateScale DateScale} is only useful for the X axis in a
	 * ScatterPlot which contains {@link eu.webtoolkit.jwt.WDate} values.
	 * <p>
	 * 
	 * @see WAxis#getScale()
	 */
	public void setScale(AxisScale scale) {
		if (!ChartUtils.equals(this.scale_, scale)) {
			this.scale_ = scale;
			update();
		}
		;
	}

	/**
	 * Returns the scale of the axis.
	 * <p>
	 * 
	 * @see WAxis#setScale(AxisScale scale)
	 */
	public AxisScale getScale() {
		return this.scale_;
	}

	/**
	 * Sets the minimum value displayed on the axis.
	 * <p>
	 * By default, the minimum and maximum values are determined automatically
	 * so that all the data can be displayed.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#setMaximum(double maximum)
	 * @see WAxis#setAutoLimits(EnumSet locations)
	 */
	public void setMinimum(double minimum) {
		final WAxis.Segment s = this.segments_.get(0);
		if (!ChartUtils.equals(s.minimum, minimum)) {
			s.minimum = minimum;
			update();
		}
		;
		if (!ChartUtils.equals(s.maximum, Math.max(s.minimum, s.maximum))) {
			s.maximum = Math.max(s.minimum, s.maximum);
			update();
		}
		;
		this.roundLimits_.remove(AxisValue.MinimumValue);
		this.update();
	}

	/**
	 * Returns the minimum value displayed on the axis.
	 * <p>
	 * This returned the minimum value that was set using
	 * {@link WAxis#setMinimum(double minimum) setMinimum()}, or otherwise the
	 * automatically calculated (and rounded) minimum.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#setMinimum(double minimum)
	 * @see WAxis#setAutoLimits(EnumSet locations)
	 * @see WAxis#setRoundLimits(EnumSet locations)
	 */
	public double getMinimum() {
		return !EnumUtils.mask(this.getAutoLimits(), AxisValue.MinimumValue)
				.isEmpty() ? this.segments_.get(0).renderMinimum
				: this.segments_.get(0).minimum;
	}

	/**
	 * Sets the maximum value for the axis displayed on the axis.
	 * <p>
	 * By default, the minimum and maximum values are determined automatically
	 * so that all the data can be displayed.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#setMinimum(double minimum)
	 * @see WAxis#setAutoLimits(EnumSet locations)
	 */
	public void setMaximum(double maximum) {
		final WAxis.Segment s = this.segments_.get(this.segments_.size() - 1);
		if (!ChartUtils.equals(s.maximum, maximum)) {
			s.maximum = maximum;
			update();
		}
		;
		if (!ChartUtils.equals(s.minimum, Math.min(s.minimum, s.maximum))) {
			s.minimum = Math.min(s.minimum, s.maximum);
			update();
		}
		;
		this.roundLimits_.remove(AxisValue.MaximumValue);
		this.update();
	}

	/**
	 * Returns the maximum value displayed on the axis.
	 * <p>
	 * This returned the maximum value that was set using
	 * {@link WAxis#setMaximum(double maximum) setMaximum()}, or otherwise the
	 * automatically calculated (and rounded) maximum.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#setMaximum(double maximum)
	 * @see WAxis#setAutoLimits(EnumSet locations)
	 * @see WAxis#setRoundLimits(EnumSet locations)
	 */
	public double getMaximum() {
		final WAxis.Segment s = this.segments_.get(this.segments_.size() - 1);
		return !EnumUtils.mask(this.getAutoLimits(), AxisValue.MaximumValue)
				.isEmpty() ? s.renderMaximum : s.maximum;
	}

	/**
	 * Sets the axis range (minimum and maximum values) manually.
	 * <p>
	 * Specifies both minimum and maximum value for the axis. This automatically
	 * disables automatic range calculation.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#setMinimum(double minimum)
	 * @see WAxis#setMaximum(double maximum)
	 */
	public void setRange(double minimum, double maximum) {
		if (maximum > minimum) {
			this.segments_.get(0).minimum = minimum;
			this.segments_.get(this.segments_.size() - 1).maximum = maximum;
			this.roundLimits_.clear();
			this.update();
		}
	}

	/**
	 * Sets the axis resolution.
	 * <p>
	 * Specifies the axis resolution, in case maximum-minimum &lt; resolution
	 * minimum and maximum are modified so the maximum - minimum = resolution
	 * <p>
	 * The default resolution is 0, which uses a built-in epsilon.
	 * <p>
	 * 
	 * @see WAxis#getResolution()
	 */
	public void setResolution(final double resolution) {
		this.resolution_ = resolution;
		this.update();
	}

	/**
	 * Returns the axis resolution.
	 * <p>
	 * 
	 * @see WAxis#setResolution(double resolution)
	 */
	public double getResolution() {
		return this.resolution_;
	}

	/**
	 * Let the minimum and/or maximum be calculated from the data.
	 * <p>
	 * Using this method, you can indicate that you want to have automatic
	 * limits, rather than limits set manually using
	 * {@link WAxis#setMinimum(double minimum) setMinimum()} or
	 * {@link WAxis#setMaximum(double maximum) setMaximum()}.
	 * <p>
	 * <code>locations</code> can be {@link AxisValue#MinimumValue} and/or
	 * {@link AxisValue#MaximumValue}.
	 * <p>
	 * The default value is {@link AxisValue#MinimumValue} |
	 * {@link AxisValue#MaximumValue}.
	 */
	public void setAutoLimits(EnumSet<AxisValue> locations) {
		if (!EnumUtils.mask(locations, AxisValue.MinimumValue).isEmpty()) {
			if (!ChartUtils.equals(this.segments_.get(0).minimum, AUTO_MINIMUM)) {
				this.segments_.get(0).minimum = AUTO_MINIMUM;
				update();
			}
			;
			this.roundLimits_.add(AxisValue.MinimumValue);
		}
		if (!EnumUtils.mask(locations, AxisValue.MaximumValue).isEmpty()) {
			if (!ChartUtils.equals(this.segments_
					.get(this.segments_.size() - 1).maximum, AUTO_MAXIMUM)) {
				this.segments_.get(this.segments_.size() - 1).maximum = AUTO_MAXIMUM;
				update();
			}
			;
			this.roundLimits_.add(AxisValue.MaximumValue);
		}
	}

	/**
	 * Let the minimum and/or maximum be calculated from the data.
	 * <p>
	 * Calls {@link #setAutoLimits(EnumSet locations)
	 * setAutoLimits(EnumSet.of(location, locations))}
	 */
	public final void setAutoLimits(AxisValue location, AxisValue... locations) {
		setAutoLimits(EnumSet.of(location, locations));
	}

	/**
	 * Returns the limits that are calculated automatically.
	 * <p>
	 * This returns the limits ({@link AxisValue#MinimumValue} and/or
	 * {@link AxisValue#MaximumValue}) that are calculated automatically from
	 * the data, rather than being specified manually using
	 * {@link WAxis#setMinimum(double minimum) setMinimum()} and/or
	 * {@link WAxis#setMaximum(double maximum) setMaximum()}.
	 * <p>
	 * 
	 * @see WAxis#setAutoLimits(EnumSet locations)
	 */
	public EnumSet<AxisValue> getAutoLimits() {
		EnumSet<AxisValue> result = EnumSet.noneOf(AxisValue.class);
		if (this.segments_.get(0).minimum == AUTO_MINIMUM) {
			result.add(AxisValue.MinimumValue);
		}
		if (this.segments_.get(this.segments_.size() - 1).maximum == AUTO_MAXIMUM) {
			result.add(AxisValue.MaximumValue);
		}
		return result;
	}

	/**
	 * Specifies whether limits should be rounded.
	 * <p>
	 * When enabling rounding, this has the effect of rounding down the minimum
	 * value, or rounding up the maximum value, to the nearest label interval.
	 * <p>
	 * By default, rounding is enabled for an auto-calculated limited, and
	 * disabled for a manually specifed limit.
	 * <p>
	 * 
	 * @see WAxis#setAutoLimits(EnumSet locations)
	 */
	public void setRoundLimits(EnumSet<AxisValue> locations) {
		this.roundLimits_ = EnumSet.copyOf(locations);
	}

	/**
	 * Specifies whether limits should be rounded.
	 * <p>
	 * Calls {@link #setRoundLimits(EnumSet locations)
	 * setRoundLimits(EnumSet.of(location, locations))}
	 */
	public final void setRoundLimits(AxisValue location, AxisValue... locations) {
		setRoundLimits(EnumSet.of(location, locations));
	}

	/**
	 * Returns whether limits should be rounded.
	 * <p>
	 * 
	 * @see WAxis#setRoundLimits(EnumSet locations)
	 */
	public EnumSet<AxisValue> getRoundLimits() {
		return this.roundLimits_;
	}

	/**
	 * Specifies a range that needs to be omitted from the axis.
	 * <p>
	 * This is useful to display data with a few outliers which would otherwise
	 * swamp the chart. This is not done automatically, but instead you need to
	 * use {@link WAxis#setBreak(double minimum, double maximum) setBreak()} to
	 * specify the value range that needs to be omitted from the axis. The
	 * omission is rendered in the axis and in BarSeries that cross the break.
	 */
	public void setBreak(double minimum, double maximum) {
		if (this.segments_.size() != 2) {
			this.segments_.add(new WAxis.Segment());
			this.segments_.get(1).maximum = this.segments_.get(0).maximum;
		}
		this.segments_.get(0).maximum = minimum;
		this.segments_.get(1).minimum = maximum;
		this.update();
	}

	/**
	 * Sets the label interval.
	 * <p>
	 * Specifies the interval for displaying labels (and ticks) on the axis. The
	 * default value is 0.0, and indicates that the interval should be computed
	 * automatically.
	 * <p>
	 * The unit for the label interval is in logical units (i.e. the same as
	 * minimum or maximum).
	 * <p>
	 * 
	 * @see WAxis#setLabelFormat(CharSequence format)
	 */
	public void setLabelInterval(double labelInterval) {
		if (!ChartUtils.equals(this.labelInterval_, labelInterval)) {
			this.labelInterval_ = labelInterval;
			update();
		}
		;
	}

	/**
	 * Returns the label interval.
	 * <p>
	 * 
	 * @see WAxis#setLabelInterval(double labelInterval)
	 */
	public double getLabelInterval() {
		return this.labelInterval_;
	}

	/**
	 * Sets the label format.
	 * <p>
	 * Sets a format string which is used to format values, both for the axis
	 * labels as well as data series values (see
	 * {@link WDataSeries#setLabelsEnabled(Axis axis, boolean enabled)
	 * WDataSeries#setLabelsEnabled()}).
	 * <p>
	 * For an axis with a {@link AxisScale#LinearScale LinearScale} or
	 * {@link AxisScale#LogScale LogScale} scale, the format string must be a
	 * format string that is accepted by snprintf() and which formats one
	 * double. If the format string is an empty string, then {@link } is used.
	 * <p>
	 * For an axis with a {@link AxisScale#DateScale DateScale} scale, the
	 * format string must be a format string accepted by
	 * {@link WDate#toString() WDate#toString()}, to format a date. If the
	 * format string is an empty string, a suitable format is chosen based on
	 * heuristics.
	 * <p>
	 * For an axis with a {@link AxisScale#DateTimeScale DateTimeScale} scale,
	 * the format string must be a format string accepted by
	 * {@link WDateTime#toString() WDateTime#toString()}, to format a date. If
	 * the format string is an empty string, a suitable format is chosen based
	 * on heuristics.
	 * <p>
	 * The default value is &quot;%.4g&quot; for a numeric axis, and a suitable
	 * format for date(time) scales based on a heuristic taking into account the
	 * current axis range.
	 * <p>
	 * 
	 * @see WAxis#getLabelFormat()
	 */
	public void setLabelFormat(final CharSequence format) {
		if (!ChartUtils.equals(this.labelFormat_, WString.toWString(format))) {
			this.labelFormat_ = WString.toWString(format);
			update();
		}
		;
		this.defaultLabelFormat_ = false;
	}

	/**
	 * Returns the label format string.
	 * <p>
	 * 
	 * @see WAxis#setLabelFormat(CharSequence format)
	 */
	public WString getLabelFormat() {
		switch (this.scale_) {
		case CategoryScale:
			return new WString();
		case DateScale:
		case DateTimeScale:
			if (this.defaultLabelFormat_) {
				if (!this.segments_.isEmpty()) {
					final WAxis.Segment s = this.segments_.get(0);
					return this.defaultDateTimeFormat(s);
				} else {
					return this.labelFormat_;
				}
			} else {
				return this.labelFormat_;
			}
		default:
			return this.defaultLabelFormat_ ? new WString("%.4g")
					: this.labelFormat_;
		}
	}

	/**
	 * Sets the label angle.
	 * <p>
	 * Sets the angle used for displaying the labels (in degrees). A 0 angle
	 * corresponds to horizontal text. Note that this option is only supported
	 * by the InlineSvgVml renderers, but not by HtmlCanvas.
	 * <p>
	 * The default value is 0.0.
	 * <p>
	 * 
	 * @see WAxis#getLabelAngle()
	 */
	public void setLabelAngle(double angle) {
		if (this.renderingMirror_) {
			this.labelAngle_ = angle;
		} else {
			if (!ChartUtils.equals(this.labelAngle_, angle)) {
				this.labelAngle_ = angle;
				update();
			}
			;
		}
	}

	/**
	 * Returns the label angle.
	 * <p>
	 * 
	 * @see WAxis#setLabelAngle(double angle)
	 */
	public double getLabelAngle() {
		return this.labelAngle_;
	}

	/**
	 * Sets whether gridlines are displayed for this axis.
	 * <p>
	 * When <i>enabled</i>, gird lines are drawn for each tick on this axis,
	 * using the {@link WAxis#getGridLinesPen() getGridLinesPen()}.
	 * <p>
	 * Unlike all other visual aspects of an axis, rendering of the gridlines is
	 * not controlled by setDisplayEnabled().
	 * <p>
	 * 
	 * @see WAxis#setGridLinesPen(WPen pen)
	 * @see WAxis#isGridLinesEnabled()
	 */
	public void setGridLinesEnabled(boolean enabled) {
		if (!ChartUtils.equals(this.gridLines_, enabled)) {
			this.gridLines_ = enabled;
			update();
		}
		;
	}

	/**
	 * Returns whether gridlines are displayed for this axis.
	 * <p>
	 * 
	 * @see WAxis#setGridLinesEnabled(boolean enabled)
	 */
	public boolean isGridLinesEnabled() {
		return this.gridLines_;
	}

	/**
	 * Changes the pen used for rendering the axis and ticks.
	 * <p>
	 * The default value is a black pen of 0 width.
	 * <p>
	 * 
	 * @see WAxis#setGridLinesPen(WPen pen)
	 */
	public void setPen(final WPen pen) {
		if (!ChartUtils.equals(this.pen_, pen)) {
			this.pen_ = pen;
			update();
		}
		;
	}

	/**
	 * Returns the pen used for rendering the axis and ticks.
	 * <p>
	 * 
	 * @see WAxis#setPen(WPen pen)
	 */
	public WPen getPen() {
		return this.pen_;
	}

	/**
	 * Changes the pen used for rendering the grid lines.
	 * <p>
	 * The default value is a gray pen of 0 width.
	 * <p>
	 * 
	 * @see WAxis#setPen(WPen pen)
	 * @see WAxis#getGridLinesPen()
	 */
	public void setGridLinesPen(final WPen pen) {
		if (!ChartUtils.equals(this.gridLinesPen_, pen)) {
			this.gridLinesPen_ = pen;
			update();
		}
		;
	}

	/**
	 * Returns the pen used for rendering the grid lines.
	 * <p>
	 * 
	 * @see WAxis#setGridLinesPen(WPen pen)
	 */
	public WPen getGridLinesPen() {
		return this.gridLinesPen_;
	}

	/**
	 * Sets the margin between the axis and the plot area.
	 * <p>
	 * The margin is defined in pixels.
	 * <p>
	 * The default value is 0.
	 * <p>
	 * 
	 * @see WAxis#getMargin()
	 */
	public void setMargin(int pixels) {
		if (!ChartUtils.equals(this.margin_, pixels)) {
			this.margin_ = pixels;
			update();
		}
		;
	}

	/**
	 * Returns the margin between the axis and the plot area.
	 * <p>
	 * 
	 * @see WAxis#setMargin(int pixels)
	 */
	public int getMargin() {
		return this.margin_;
	}

	/**
	 * Sets the axis title.
	 * <p>
	 * The default title is empty.
	 * <p>
	 * 
	 * @see WAxis#getTitle()
	 */
	public void setTitle(final CharSequence title) {
		if (!ChartUtils.equals(this.title_, WString.toWString(title))) {
			this.title_ = WString.toWString(title);
			update();
		}
		;
	}

	/**
	 * Returns the axis title.
	 * <p>
	 * 
	 * @see WAxis#setTitle(CharSequence title)
	 */
	public WString getTitle() {
		return this.title_;
	}

	/**
	 * Sets the axis title font.
	 * <p>
	 * The default title font is a 12 point Sans Serif font.
	 * <p>
	 * 
	 * @see WAxis#getTitleFont()
	 */
	public void setTitleFont(final WFont titleFont) {
		if (!ChartUtils.equals(this.titleFont_, titleFont)) {
			this.titleFont_ = titleFont;
			update();
		}
		;
	}

	/**
	 * Returns the axis title font.
	 * <p>
	 * 
	 * @see WAxis#setTitleFont(WFont titleFont)
	 */
	public WFont getTitleFont() {
		return this.titleFont_;
	}

	public void setTitleOffset(double offset) {
		this.titleOffset_ = offset;
	}

	public double getTitleOffset() {
		return this.titleOffset_;
	}

	/**
	 * Sets the axis label font.
	 * <p>
	 * The default label font is a 10 point Sans Serif font.
	 * <p>
	 * 
	 * @see WAxis#getLabelFont()
	 */
	public void setLabelFont(final WFont labelFont) {
		if (!ChartUtils.equals(this.labelFont_, labelFont)) {
			this.labelFont_ = labelFont;
			update();
		}
		;
	}

	/**
	 * Returns the axis label font.
	 * <p>
	 * 
	 * @see WAxis#setLabelFont(WFont labelFont)
	 */
	public WFont getLabelFont() {
		return this.labelFont_;
	}

	public WString getLabel(double u) {
		String buf = null;
		WString text = new WString();
		if (this.scale_ == AxisScale.CategoryScale) {
			text = this.chart_.categoryLabel((int) u, this.axis_);
			if ((text.length() == 0)) {
				text = new WString(LocaleUtils.toString(LocaleUtils
						.getCurrentLocale(), u));
			}
		} else {
			if (this.scale_ == AxisScale.DateScale) {
				WDate d = WDate.fromJulianDay((int) u);
				WString format = this.getLabelFormat();
				return new WString(d.toString(format.toString()));
			} else {
				String format = this.getLabelFormat().toString();
				if (format.length() == 0) {
					text = new WString(LocaleUtils.toString(LocaleUtils
							.getCurrentLocale(), u));
				} else {
					buf = String.format(format, u);
					text = new WString(buf);
				}
			}
		}
		return text;
	}

	/**
	 * Returns the chart to which this axis belongs.
	 * <p>
	 * 
	 * @see WCartesianChart#getAxis(Axis axis)
	 */
	public int getSegmentCount() {
		return (int) this.segments_.size();
	}

	public double getSegmentMargin() {
		return this.segmentMargin_;
	}

	public boolean prepareRender(Orientation orientation, double length) {
		double totalRenderRange = 0;
		for (int i = 0; i < this.segments_.size(); ++i) {
			final WAxis.Segment s = this.segments_.get(i);
			this.computeRange(s);
			totalRenderRange += s.renderMaximum - s.renderMinimum;
		}
		double clipMin = this.segments_.get(0).renderMinimum == 0 ? 0
				: this.chart_.getAxisPadding();
		double clipMax = this.segments_.get(this.segments_.size() - 1).renderMaximum == 0 ? 0
				: this.chart_.getAxisPadding();
		double totalRenderLength = length;
		double totalRenderStart = clipMin;
		final double SEGMENT_MARGIN = 40;
		totalRenderLength -= SEGMENT_MARGIN * (this.segments_.size() - 1)
				+ clipMin + clipMax;
		if (totalRenderLength <= 0) {
			this.renderInterval_ = 1.0;
			return false;
		}
		for (int it = 0; it < 2; ++it) {
			double rs = totalRenderStart;
			double TRR = totalRenderRange;
			totalRenderRange = 0;
			for (int i = 0; i < this.segments_.size(); ++i) {
				final WAxis.Segment s = this.segments_.get(i);
				double diff = s.renderMaximum - s.renderMinimum;
				s.renderStart = rs;
				s.renderLength = diff / TRR * totalRenderLength;
				if (i == 0) {
					this.renderInterval_ = this.labelInterval_;
					if (this.renderInterval_ == 0) {
						if (this.scale_ == AxisScale.CategoryScale) {
							double numLabels = this.calcAutoNumLabels(
									orientation, s) / 1.5;
							int rc = this.chart_.numberOfCategories(this.axis_);
							this.renderInterval_ = Math.max(1.0, Math.floor(rc
									/ numLabels));
						} else {
							if (this.scale_ == AxisScale.LogScale) {
								this.renderInterval_ = 1;
							} else {
								double numLabels = this.calcAutoNumLabels(
										orientation, s);
								this.renderInterval_ = round125(diff
										/ numLabels);
							}
						}
					}
				}
				if (this.renderInterval_ == 0) {
					this.renderInterval_ = 1;
					return false;
				}
				if (this.scale_ == AxisScale.LinearScale) {
					if (it == 0) {
						if (!EnumUtils.mask(this.roundLimits_,
								AxisValue.MinimumValue).isEmpty()) {
							s.renderMinimum = roundDown125(s.renderMinimum,
									this.renderInterval_);
						}
						if (!EnumUtils.mask(this.roundLimits_,
								AxisValue.MaximumValue).isEmpty()) {
							s.renderMaximum = roundUp125(s.renderMaximum,
									this.renderInterval_);
						}
					}
				} else {
					if (this.scale_ == AxisScale.DateScale
							|| this.scale_ == AxisScale.DateTimeScale) {
						double daysInterval = 0.0;
						WDate min = null;
						WDate max = null;
						int interval;
						if (this.scale_ == AxisScale.DateScale) {
							daysInterval = this.renderInterval_;
							min = WDate.fromJulianDay((int) s.renderMinimum);
							max = WDate.fromJulianDay((int) s.renderMaximum);
						} else {
							if (this.scale_ == AxisScale.DateTimeScale) {
								daysInterval = this.renderInterval_
										/ (60.0 * 60.0 * 24);
								min = new WDate(new Date(
										(long) (long) s.renderMinimum));
								max = new WDate(new Date(
										(long) (long) s.renderMaximum));
							}
						}
						logger.debug(new StringWriter().append("Range: ")
								.append(min.toString()).append(", ").append(
										max.toString()).toString());
						if (daysInterval > 200) {
							s.dateTimeRenderUnit = WAxis.DateTimeUnit.Years;
							interval = Math.max(1,
									(int) round125(daysInterval / 365));
							if (!EnumUtils.mask(this.roundLimits_,
									AxisValue.MinimumValue).isEmpty()) {
								if (min.getDay() != 1 && min.getMonth() != 1) {
									min = new WDate(min.getYear(), 1, 1);
								}
							}
							if (!EnumUtils.mask(this.roundLimits_,
									AxisValue.MaximumValue).isEmpty()) {
								if (max.getDay() != 1 && max.getDay() != 1) {
									max = new WDate(max.getYear() + 1, 1, 1);
								}
							}
						} else {
							if (daysInterval > 20) {
								s.dateTimeRenderUnit = WAxis.DateTimeUnit.Months;
								double d = daysInterval / 30;
								if (d < 1.3) {
									interval = 1;
								} else {
									if (d < 2.3) {
										interval = 2;
									} else {
										if (d < 3.3) {
											interval = 3;
										} else {
											if (d < 4.3) {
												interval = 4;
											} else {
												interval = 6;
											}
										}
									}
								}
								if (!EnumUtils.mask(this.roundLimits_,
										AxisValue.MinimumValue).isEmpty()) {
									if ((min.getMonth() - 1) % interval != 0) {
										int m = roundDown(min.getMonth() - 1,
												interval) + 1;
										min = new WDate(min.getYear(), m, 1);
									} else {
										if (min.getDay() != 1) {
											min = new WDate(min.getYear(), min
													.getMonth(), 1);
										}
									}
								}
								if (!EnumUtils.mask(this.roundLimits_,
										AxisValue.MaximumValue).isEmpty()) {
									if (max.getDay() != 1) {
										max = new WDate(max.getYear(), max
												.getMonth(), 1).addMonths(1);
									}
									if ((max.getMonth() - 1) % interval != 0) {
										int m = roundDown(max.getMonth() - 1,
												interval) + 1;
										max = new WDate(max.getYear(), m, 1)
												.addMonths(interval);
									}
								}
							} else {
								if (daysInterval > 0.6) {
									s.dateTimeRenderUnit = WAxis.DateTimeUnit.Days;
									if (daysInterval < 1.3) {
										interval = 1;
									} else {
										interval = 7 * Math.max(1,
												(int) ((daysInterval + 5) / 7));
									}
								} else {
									double minutes = daysInterval * 24 * 60;
									if (minutes > 40) {
										s.dateTimeRenderUnit = WAxis.DateTimeUnit.Hours;
										double d = minutes / 60;
										if (d < 1.3) {
											interval = 1;
										} else {
											if (d < 2.3) {
												interval = 2;
											} else {
												if (d < 3.3) {
													interval = 3;
												} else {
													if (d < 4.3) {
														interval = 4;
													} else {
														if (d < 6.3) {
															interval = 6;
														} else {
															interval = 12;
														}
													}
												}
											}
										}
										if (!EnumUtils.mask(this.roundLimits_,
												AxisValue.MinimumValue)
												.isEmpty()) {
											if (min.getHour() % interval != 0) {
												int h = roundDown(
														min.getHour(), interval);
												min.setTime(h, 0);
											} else {
												if (min.getMinute() != 0) {
													min.setTime(min.getHour(),
															0);
												}
											}
										}
										if (!EnumUtils.mask(this.roundLimits_,
												AxisValue.MaximumValue)
												.isEmpty()) {
											if (max.getMinute() != 0) {
												max.setTime(max.getHour(), 0);
												max = max.addSeconds(60 * 60);
											}
											if (max.getHour() % interval != 0) {
												int h = roundDown(
														max.getHour(), interval);
												max.setTime(h, 0);
												max = max
														.addSeconds(interval * 60 * 60);
											}
										}
									} else {
										if (minutes > 0.8) {
											s.dateTimeRenderUnit = WAxis.DateTimeUnit.Minutes;
											if (minutes < 1.3) {
												interval = 1;
											} else {
												if (minutes < 2.3) {
													interval = 2;
												} else {
													if (minutes < 5.3) {
														interval = 5;
													} else {
														if (minutes < 10.3) {
															interval = 10;
														} else {
															if (minutes < 15.3) {
																interval = 15;
															} else {
																if (minutes < 20.3) {
																	interval = 20;
																} else {
																	interval = 30;
																}
															}
														}
													}
												}
											}
											if (!EnumUtils.mask(
													this.roundLimits_,
													AxisValue.MinimumValue)
													.isEmpty()) {
												if (min.getMinute() % interval != 0) {
													int m = roundDown(min
															.getMinute(),
															interval);
													min.setTime(min.getHour(),
															m);
												} else {
													if (min.getSecond() != 0) {
														min.setTime(min
																.getHour(), min
																.getMinute());
													}
												}
											}
											if (!EnumUtils.mask(
													this.roundLimits_,
													AxisValue.MaximumValue)
													.isEmpty()) {
												if (max.getSecond() != 0) {
													max.setTime(max.getHour(),
															max.getMinute());
													max = max.addSeconds(60);
												}
												if (max.getMinute() % interval != 0) {
													int m = roundDown(max
															.getMinute(),
															interval);
													max.setTime(max.getHour(),
															m);
													max = max
															.addSeconds(interval * 60);
												}
											}
										} else {
											s.dateTimeRenderUnit = WAxis.DateTimeUnit.Seconds;
											double seconds = minutes * 60;
											if (seconds < 1.3) {
												interval = 1;
											} else {
												if (seconds < 2.3) {
													interval = 2;
												} else {
													if (seconds < 5.3) {
														interval = 5;
													} else {
														if (seconds < 10.3) {
															interval = 10;
														} else {
															if (seconds < 15.3) {
																interval = 15;
															} else {
																if (seconds < 20.3) {
																	interval = 20;
																} else {
																	interval = 30;
																}
															}
														}
													}
												}
											}
											if (!EnumUtils.mask(
													this.roundLimits_,
													AxisValue.MinimumValue)
													.isEmpty()) {
												if (min.getSecond() % interval != 0) {
													int sec = roundDown(min
															.getSecond(),
															interval);
													min.setTime(min.getHour(),
															min.getMinute(),
															sec);
												} else {
													if (min.getMillisecond() != 0) {
														min
																.setTime(
																		min
																				.getHour(),
																		min
																				.getMinute(),
																		min
																				.getSecond());
													}
												}
											}
											if (!EnumUtils.mask(
													this.roundLimits_,
													AxisValue.MaximumValue)
													.isEmpty()) {
												if (max.getMillisecond() != 0) {
													max.setTime(max.getHour(),
															max.getMinute(),
															max.getSecond());
													max = max.addSeconds(1);
												}
												if (max.getSecond() % interval != 0) {
													int sec = roundDown(max
															.getSecond(),
															interval);
													max.setTime(max.getHour(),
															max.getMinute(),
															sec);
													max = max
															.addSeconds(interval);
												}
											}
										}
									}
								}
							}
						}
						s.dateTimeRenderInterval = interval;
						if (this.scale_ == AxisScale.DateScale) {
							s.renderMinimum = min.toJulianDay();
							s.renderMaximum = max.toJulianDay();
						} else {
							if (this.scale_ == AxisScale.DateTimeScale) {
								s.renderMinimum = min.getDate().getTime();
								s.renderMaximum = max.getDate().getTime();
							}
						}
					}
				}
				totalRenderRange += s.renderMaximum - s.renderMinimum;
				rs += s.renderLength + SEGMENT_MARGIN;
			}
		}
		return true;
	}

	public void render(final WPainter painter,
			EnumSet<AxisProperty> properties, final WPointF axisStart,
			final WPointF axisEnd, double tickStart, double tickEnd,
			double labelPos, EnumSet<AlignmentFlag> labelFlags) {
		WFont oldFont1 = painter.getFont();
		painter.setFont(this.labelFont_);
		boolean vertical = axisStart.getX() == axisEnd.getX();
		for (int segment = 0; segment < this.getSegmentCount(); ++segment) {
			final WAxis.Segment s = this.segments_.get(segment);
			if (!EnumUtils.mask(properties, AxisProperty.Line).isEmpty()) {
				painter.setPen(this.getPen());
				WPointF begin = interpolate(axisStart, axisEnd, s.renderStart);
				WPointF end = interpolate(axisStart, axisEnd, s.renderStart
						+ s.renderLength);
				painter.drawLine(begin, end);
				boolean rotate = vertical;
				if (segment != 0) {
					painter.save();
					painter.translate(begin);
					if (rotate) {
						painter.rotate(90);
					}
					painter.drawPath(new TildeStartMarker(
							(int) this.segmentMargin_));
					painter.restore();
				}
				if (segment != this.getSegmentCount() - 1) {
					painter.save();
					painter.translate(end);
					if (rotate) {
						painter.rotate(90);
					}
					painter.drawPath(new TildeEndMarker(
							(int) this.segmentMargin_));
					painter.restore();
				}
			}
			WPainterPath ticksPath = new WPainterPath();
			List<WAxis.TickLabel> ticks = new ArrayList<WAxis.TickLabel>();
			this.getLabelTicks(ticks, segment);
			for (int i = 0; i < ticks.size(); ++i) {
				double u = this.mapToDevice(ticks.get(i).u, segment);
				WPointF p = interpolate(axisStart, axisEnd, Math.floor(u));
				if (!EnumUtils.mask(properties, AxisProperty.Line).isEmpty()
						&& ticks.get(i).tickLength != WAxis.TickLabel.TickLength.Zero) {
					double ts = tickStart;
					double te = tickEnd;
					if (ticks.get(i).tickLength == WAxis.TickLabel.TickLength.Short) {
						ts = tickStart / 2;
						te = tickEnd / 2;
					}
					if (vertical) {
						ticksPath.moveTo(new WPointF(p.getX() + ts, p.getY()));
						ticksPath.lineTo(new WPointF(p.getX() + te, p.getY()));
					} else {
						ticksPath.moveTo(new WPointF(p.getX(), p.getY() + ts));
						ticksPath.lineTo(new WPointF(p.getX(), p.getY() + te));
					}
				}
				if (!EnumUtils.mask(properties, AxisProperty.Labels).isEmpty()
						&& !(ticks.get(i).label.length() == 0)) {
					WPointF labelP = new WPointF();
					if (vertical) {
						labelP = new WPointF(p.getX() + labelPos, p.getY());
					} else {
						labelP = new WPointF(p.getX(), p.getY() + labelPos);
					}
					this.renderLabel(painter, ticks.get(i).label, labelP,
							WColor.black, labelFlags, this.getLabelAngle(), 3);
				}
			}
			if (!ticksPath.isEmpty()) {
				painter.strokePath(ticksPath, this.getPen());
			}
		}
		painter.setFont(oldFont1);
	}

	public final void render(final WPainter painter,
			EnumSet<AxisProperty> properties, final WPointF axisStart,
			final WPointF axisEnd, double tickStart, double tickEnd,
			double labelPos, AlignmentFlag labelFlag,
			AlignmentFlag... labelFlags) {
		render(painter, properties, axisStart, axisEnd, tickStart, tickEnd,
				labelPos, EnumSet.of(labelFlag, labelFlags));
	}

	public List<Double> getGridLinePositions() {
		List<Double> pos = new ArrayList<Double>();
		for (int segment = 0; segment < this.segments_.size(); ++segment) {
			List<WAxis.TickLabel> ticks = new ArrayList<WAxis.TickLabel>();
			this.getLabelTicks(ticks, segment);
			for (int i = 0; i < ticks.size(); ++i) {
				if (ticks.get(i).tickLength == WAxis.TickLabel.TickLength.Long) {
					pos.add(this.mapToDevice(ticks.get(i).u, segment));
				}
			}
		}
		return pos;
	}

	public void renderLabel(final WPainter painter, final CharSequence text,
			final WPointF p, final WColor color, EnumSet<AlignmentFlag> flags,
			double angle, int margin) {
		AlignmentFlag horizontalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignHorizontalMask));
		AlignmentFlag verticalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignVerticalMask));
		double width = 1000;
		double height = 20;
		WPointF pos = p;
		double left = pos.getX();
		double top = pos.getY();
		switch (horizontalAlign) {
		case AlignLeft:
			left += margin;
			break;
		case AlignCenter:
			left -= width / 2;
			break;
		case AlignRight:
			left -= width + margin;
		default:
			break;
		}
		switch (verticalAlign) {
		case AlignTop:
			top += margin;
			break;
		case AlignMiddle:
			top -= height / 2;
			break;
		case AlignBottom:
			top -= height + margin;
			break;
		default:
			break;
		}
		WPen pen = new WPen(color);
		WPen oldPen = painter.getPen();
		painter.setPen(pen);
		if (angle == 0) {
			painter.drawText(new WRectF(left, top, width, height), EnumSet.of(
					horizontalAlign, verticalAlign), text);
		} else {
			painter.save();
			painter.translate(pos);
			painter.rotate(-angle);
			painter.drawText(new WRectF(left - pos.getX(), top - pos.getY(),
					width, height), EnumSet.of(horizontalAlign, verticalAlign),
					text);
			painter.restore();
		}
		painter.setPen(oldPen);
	}

	public void setRenderMirror(boolean enable) {
		this.renderingMirror_ = enable;
	}

	static class TickLabel {
		private static Logger logger = LoggerFactory.getLogger(TickLabel.class);

		enum TickLength {
			Zero, Short, Long;

			/**
			 * Returns the numerical representation of this enum.
			 */
			public int getValue() {
				return ordinal();
			}
		}

		public double u;
		public WAxis.TickLabel.TickLength tickLength;
		public WString label;

		public TickLabel(double v, WAxis.TickLabel.TickLength length,
				final CharSequence l) {
			this.u = v;
			this.tickLength = length;
			this.label = WString.toWString(l);
		}

		public TickLabel(double v, WAxis.TickLabel.TickLength length) {
			this(v, length, new WString());
		}
	}

	protected void getLabelTicks(final List<WAxis.TickLabel> ticks, int segment) {
		final WAxis.Segment s = this.segments_.get(segment);
		int rc;
		switch (this.scale_) {
		case CategoryScale: {
			rc = this.chart_.numberOfCategories(this.axis_);
			int renderInterval = Math.max(1, (int) this.renderInterval_);
			if (renderInterval == 1) {
				ticks.add(new WAxis.TickLabel(-0.5,
						WAxis.TickLabel.TickLength.Long));
				for (int i = 0; i < rc; ++i) {
					ticks.add(new WAxis.TickLabel(i + 0.5,
							WAxis.TickLabel.TickLength.Long));
					ticks.add(new WAxis.TickLabel(i,
							WAxis.TickLabel.TickLength.Zero, this
									.getLabel((double) i)));
				}
			} else {
				for (int i = 0; i < rc; i += renderInterval) {
					ticks.add(new WAxis.TickLabel(i,
							WAxis.TickLabel.TickLength.Long, this
									.getLabel((double) i)));
				}
			}
			break;
		}
		case LinearScale: {
			for (int i = 0;; ++i) {
				double v = s.renderMinimum + this.renderInterval_ * i;
				if (v - s.renderMaximum > EPSILON * this.renderInterval_) {
					break;
				}
				WString t = new WString();
				if (i % 2 == 0) {
					t = this.getLabel(v);
				}
				ticks.add(new WAxis.TickLabel(v,
						i % 2 == 0 ? WAxis.TickLabel.TickLength.Long
								: WAxis.TickLabel.TickLength.Short, t));
			}
			break;
		}
		case LogScale: {
			double v = s.renderMinimum > 0 ? s.renderMinimum : 0.0001;
			double p = v;
			int i = 0;
			for (;; ++i) {
				if (v - s.renderMaximum > EPSILON * s.renderMaximum) {
					break;
				}
				if (i == 9) {
					v = p = 10 * p;
					i = 0;
				}
				if (i == 0) {
					ticks.add(new WAxis.TickLabel(v,
							WAxis.TickLabel.TickLength.Long, this.getLabel(v)));
				} else {
					ticks.add(new WAxis.TickLabel(v,
							WAxis.TickLabel.TickLength.Short));
				}
				v += p;
			}
			break;
		}
		case DateTimeScale:
		case DateScale: {
			WString format = this.getLabelFormat();
			WDate dt = null;
			if (this.scale_ == AxisScale.DateScale) {
				dt = WDate.fromJulianDay((int) s.renderMinimum);
				if (!(dt != null)) {
					String exception = "Invalid julian day: "
							+ String.valueOf(s.renderMinimum);
					throw new WException(exception);
				}
			} else {
				dt = new WDate(new Date((long) (long) s.renderMinimum));
			}
			int interval = s.dateTimeRenderInterval;
			WAxis.DateTimeUnit unit = s.dateTimeRenderUnit;
			boolean atTick = interval > 1
					|| unit.getValue() <= WAxis.DateTimeUnit.Days.getValue()
					|| !!EnumUtils.mask(this.roundLimits_,
							AxisValue.MinimumValue).isEmpty();
			for (;;) {
				long dl = this.getDateNumber(dt);
				if (dl > s.renderMaximum) {
					break;
				}
				WDate next = null;
				switch (unit) {
				case Years:
					next = dt.addYears(interval);
					break;
				case Months:
					next = dt.addMonths(interval);
					break;
				case Days:
					next = dt.addDays(interval);
					break;
				case Hours:
					next = dt.addSeconds(interval * 60 * 60);
					break;
				case Minutes:
					next = dt.addSeconds(interval * 60);
					break;
				case Seconds:
					next = dt.addSeconds(interval);
					break;
				}
				WString text = new WString(dt.toString(format.toString()));
				if (dl >= s.renderMinimum) {
					ticks.add(new WAxis.TickLabel((double) dl,
							WAxis.TickLabel.TickLength.Long, atTick ? text
									: new WString()));
				}
				if (!atTick) {
					double tl = (this.getDateNumber(next) + dl) / 2;
					if (tl >= s.renderMinimum && tl <= s.renderMaximum) {
						ticks.add(new WAxis.TickLabel((double) tl,
								WAxis.TickLabel.TickLength.Zero, text));
					}
				}
				dt = next;
			}
			break;
		}
		}
	}

	enum DateTimeUnit {
		Seconds, Minutes, Hours, Days, Months, Years;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	private WAbstractChartImplementation chart_;
	private Axis axis_;
	private boolean visible_;
	private AxisValue location_;
	private AxisScale scale_;
	private double resolution_;
	private double labelInterval_;
	private WString labelFormat_;
	private boolean defaultLabelFormat_;
	private boolean gridLines_;
	private WPen pen_;
	private WPen gridLinesPen_;
	private int margin_;
	private double labelAngle_;
	private WString title_;
	private WFont titleFont_;
	private WFont labelFont_;
	private EnumSet<AxisValue> roundLimits_;
	private double segmentMargin_;
	private double titleOffset_;
	private boolean renderingMirror_;

	static class Segment {
		private static Logger logger = LoggerFactory.getLogger(Segment.class);

		public double minimum;
		public double maximum;
		public double renderMinimum;
		public double renderMaximum;
		public double renderLength;
		public double renderStart;
		public WAxis.DateTimeUnit dateTimeRenderUnit;
		public int dateTimeRenderInterval;

		public Segment() {
			this.minimum = AUTO_MINIMUM;
			this.maximum = AUTO_MAXIMUM;
			this.renderMinimum = AUTO_MINIMUM;
			this.renderMaximum = AUTO_MAXIMUM;
			this.renderLength = AUTO_MAXIMUM;
			this.renderStart = AUTO_MAXIMUM;
		}
	}

	List<WAxis.Segment> segments_;
	private double renderInterval_;

	WAxis() {
		this.chart_ = null;
		this.axis_ = Axis.XAxis;
		this.visible_ = true;
		this.location_ = AxisValue.MinimumValue;
		this.scale_ = AxisScale.LinearScale;
		this.resolution_ = 0.0;
		this.labelInterval_ = 0;
		this.labelFormat_ = new WString();
		this.defaultLabelFormat_ = true;
		this.gridLines_ = false;
		this.pen_ = new WPen();
		this.gridLinesPen_ = new WPen(WColor.gray);
		this.margin_ = 0;
		this.labelAngle_ = 0;
		this.title_ = new WString();
		this.titleFont_ = new WFont();
		this.labelFont_ = new WFont();
		this.roundLimits_ = EnumSet.of(AxisValue.MinimumValue,
				AxisValue.MaximumValue);
		this.segmentMargin_ = 40;
		this.titleOffset_ = 0;
		this.segments_ = new ArrayList<WAxis.Segment>();
		this.titleFont_.setFamily(WFont.GenericFamily.SansSerif);
		this.titleFont_.setSize(WFont.Size.FixedSize, new WLength(12,
				WLength.Unit.Point));
		this.labelFont_.setFamily(WFont.GenericFamily.SansSerif);
		this.labelFont_.setSize(WFont.Size.FixedSize, new WLength(10,
				WLength.Unit.Point));
		this.segments_.add(new WAxis.Segment());
	}

	void init(WAbstractChartImplementation chart, Axis axis) {
		this.chart_ = chart;
		this.axis_ = axis;
		if (axis == Axis.XAxis || this.axis_ == Axis.XAxis_3D
				|| this.axis_ == Axis.YAxis_3D) {
			if (this.chart_.getChartType() == ChartType.CategoryChart) {
				this.scale_ = AxisScale.CategoryScale;
			} else {
				if (this.scale_ != AxisScale.DateScale) {
					this.scale_ = AxisScale.LinearScale;
				}
			}
		}
		if (axis == Axis.Y2Axis) {
			this.visible_ = false;
		}
	}

	private void update() {
		if (this.chart_ != null) {
			this.chart_.update();
		}
	}

	// private boolean (final T m, final T v) ;
	private void computeRange(final WAxis.Segment segment) {
		if (this.scale_ == AxisScale.CategoryScale) {
			int rc = this.chart_.numberOfCategories(this.axis_);
			rc = Math.max(1, rc);
			segment.renderMinimum = -0.5;
			segment.renderMaximum = rc - 0.5;
		} else {
			segment.renderMinimum = segment.minimum;
			segment.renderMaximum = segment.maximum;
			final boolean findMinimum = segment.renderMinimum == AUTO_MINIMUM;
			final boolean findMaximum = segment.renderMaximum == AUTO_MAXIMUM;
			if (findMinimum || findMaximum) {
				double minimum = Double.MAX_VALUE;
				double maximum = -Double.MAX_VALUE;
				WAbstractChartImplementation.RenderRange rr = this.chart_
						.computeRenderRange(this.axis_, this.scale_);
				minimum = rr.minimum;
				maximum = rr.maximum;
				if (minimum == Double.MAX_VALUE) {
					if (this.scale_ == AxisScale.LogScale) {
						minimum = 1;
					} else {
						if (this.scale_ == AxisScale.DateScale) {
							minimum = WDate.getCurrentDate().toJulianDay() - 10;
						} else {
							minimum = 0;
						}
					}
				}
				if (maximum == -Double.MAX_VALUE) {
					if (this.scale_ == AxisScale.LogScale) {
						maximum = 10;
					} else {
						if (this.scale_ == AxisScale.DateScale) {
							maximum = WDate.getCurrentDate().toJulianDay();
						} else {
							maximum = 100;
						}
					}
				}
				if (findMinimum) {
					segment.renderMinimum = Math.min(minimum,
							findMaximum ? maximum : segment.maximum);
				}
				if (findMaximum) {
					segment.renderMaximum = Math.max(maximum,
							findMinimum ? minimum : segment.minimum);
				}
			}
			double diff = segment.renderMaximum - segment.renderMinimum;
			if (this.scale_ == AxisScale.LogScale) {
				double minLog10 = Math.log10(segment.renderMinimum);
				double maxLog10 = Math.log10(segment.renderMaximum);
				if (findMinimum && findMaximum) {
					segment.renderMinimum = Math.pow(10, Math.floor(minLog10));
					segment.renderMaximum = Math.pow(10, Math.ceil(maxLog10));
					if (segment.renderMinimum == segment.renderMaximum) {
						segment.renderMaximum = Math.pow(10, Math
								.ceil(maxLog10) + 1);
					}
				} else {
					if (findMinimum) {
						segment.renderMinimum = Math.pow(10, Math
								.floor(minLog10));
						if (segment.renderMinimum == segment.renderMaximum) {
							segment.renderMinimum = Math.pow(10, Math
									.floor(minLog10) - 1);
						}
					} else {
						if (findMaximum) {
							segment.renderMaximum = Math.pow(10, Math
									.ceil(maxLog10));
							if (segment.renderMinimum == segment.renderMaximum) {
								segment.renderMaximum = Math.pow(10, Math
										.ceil(maxLog10) + 1);
							}
						}
					}
				}
			} else {
				double resolution = this.resolution_;
				if (resolution == 0) {
					if (this.scale_ == AxisScale.LinearScale) {
						resolution = Math.max(1E-3, Math
								.abs(1E-3 * segment.renderMinimum));
					} else {
						if (this.scale_ == AxisScale.DateScale) {
							resolution = 1;
						} else {
							if (this.scale_ == AxisScale.DateTimeScale) {
								resolution = 120;
							}
						}
					}
				}
				if (Math.abs(diff) < resolution) {
					double average = (segment.renderMaximum + segment.renderMinimum) / 2.0;
					double d = resolution;
					if (findMinimum && findMaximum) {
						segment.renderMaximum = average + d / 2.0;
						segment.renderMinimum = average - d / 2.0;
					} else {
						if (findMinimum) {
							segment.renderMinimum = segment.renderMaximum - d;
						} else {
							if (findMaximum) {
								segment.renderMaximum = segment.renderMinimum
										+ d;
							}
						}
					}
					diff = segment.renderMaximum - segment.renderMinimum;
				}
				if (findMinimum && segment.renderMinimum >= 0
						&& segment.renderMinimum - 0.50 * diff <= 0) {
					segment.renderMinimum = 0;
				}
				if (findMaximum && segment.renderMaximum <= 0
						&& segment.renderMaximum + 0.50 * diff >= 0) {
					segment.renderMaximum = 0;
				}
			}
		}
		assert segment.renderMinimum < segment.renderMaximum;
	}

	void setOtherAxisLocation(AxisValue otherLocation) {
		if (this.scale_ != AxisScale.LogScale) {
			for (int i = 0; i < this.segments_.size(); ++i) {
				final WAxis.Segment s = this.segments_.get(i);
				int borderMin;
				int borderMax;
				if (this.scale_ == AxisScale.CategoryScale) {
					borderMax = borderMin = this.chart_.getAxisPadding();
				} else {
					borderMin = s.renderMinimum == 0
							&& otherLocation == AxisValue.ZeroValue ? 0
							: this.chart_.getAxisPadding();
					borderMax = s.renderMinimum == 0
							&& otherLocation == AxisValue.ZeroValue ? 0
							: this.chart_.getAxisPadding();
				}
				s.renderLength -= borderMin + borderMax;
				s.renderStart += borderMin;
			}
		}
	}

	private double getValue(final Object v) {
		switch (this.scale_) {
		case LinearScale:
		case LogScale:
			return StringUtils.asNumber(v);
		case DateScale:
			if (v.getClass().equals(WDate.class)) {
				WDate d = (WDate) v;
				return (double) d.toJulianDay();
			} else {
				return Double.NaN;
			}
		case DateTimeScale:
			if (v.getClass().equals(WDate.class)) {
				WDate d = (WDate) v;
				WDate dt = null;
				dt = d;
				return (double) dt.getDate().getTime();
			} else {
				return Double.NaN;
			}
		default:
			return -1.0;
		}
	}

	private double calcAutoNumLabels(Orientation orientation,
			final WAxis.Segment s) {
		return s.renderLength
				/ (orientation == Orientation.Vertical ? AUTO_V_LABEL_PIXELS
						: AUTO_H_LABEL_PIXELS);
	}

	private WString defaultDateTimeFormat(final WAxis.Segment s) {
		WDate dt = null;
		if (this.scale_ == AxisScale.DateScale) {
			dt = WDate.fromJulianDay((int) s.renderMinimum);
			if (!(dt != null)) {
				String exception = "Invalid julian day: "
						+ String.valueOf(s.renderMinimum);
				throw new WException(exception);
			}
		} else {
			dt = new WDate(new Date((long) (long) s.renderMinimum));
		}
		int interval = s.dateTimeRenderInterval;
		WAxis.DateTimeUnit unit = s.dateTimeRenderUnit;
		boolean atTick = interval > 1
				|| unit.getValue() <= WAxis.DateTimeUnit.Days.getValue()
				|| !!EnumUtils.mask(this.roundLimits_, AxisValue.MinimumValue)
						.isEmpty();
		if (atTick) {
			switch (unit) {
			case Months:
			case Years:
			case Days:
				if (dt.getSecond() != 0) {
					return new WString("dd/MM/yy hh:mm:ss");
				} else {
					if (dt.getHour() != 0) {
						return new WString("dd/MM/yy hh:mm");
					} else {
						return new WString("dd/MM/yy");
					}
				}
			case Hours:
				if (dt.getSecond() != 0) {
					return new WString("dd/MM hh:mm:ss");
				} else {
					if (dt.getMinute() != 0) {
						return new WString("dd/MM hh:mm");
					} else {
						return new WString("h'h' dd/MM");
					}
				}
			case Minutes:
				if (dt.getSecond() != 0) {
					return new WString("hh:mm:ss");
				} else {
					return new WString("hh:mm");
				}
			case Seconds:
				return new WString("hh:mm:ss");
			}
		} else {
			switch (unit) {
			case Years:
				return new WString("yyyy");
			case Months:
				return new WString("MMM yy");
			case Days:
				return new WString("dd/MM/yy");
			case Hours:
				return new WString("h'h' dd/MM");
			case Minutes:
				return new WString("hh:mm");
			case Seconds:
				return new WString("hh:mm:ss");
			default:
				break;
			}
		}
		return WString.Empty;
	}

	double mapFromDevice(double d) {
		for (int i = 0; i < this.segments_.size(); ++i) {
			final WAxis.Segment s = this.segments_.get(i);
			boolean lastSegment = i == this.segments_.size() - 1;
			if (lastSegment || d < this.mapToDevice(s.renderMaximum, i)) {
				d = d - s.renderStart;
				if (this.scale_ != AxisScale.LogScale) {
					return s.renderMinimum + d
							* (s.renderMaximum - s.renderMinimum)
							/ s.renderLength;
				} else {
					return Math.exp(Math.log(s.renderMinimum)
							+ d
							* (Math.log(s.renderMaximum) - Math
									.log(s.renderMinimum)) / s.renderLength);
				}
			}
		}
		return 0;
	}

	double mapToDevice(final Object value, int segment) {
		return this.mapToDevice(this.getValue(value), segment);
	}

	final double mapToDevice(final Object value) {
		return mapToDevice(value, 0);
	}

	double mapToDevice(double u, int segment) {
		if (Double.isNaN(u)) {
			return u;
		}
		final WAxis.Segment s = this.segments_.get(segment);
		double d;
		if (this.scale_ != AxisScale.LogScale) {
			d = (u - s.renderMinimum) / (s.renderMaximum - s.renderMinimum)
					* s.renderLength;
		} else {
			u = Math.max(s.renderMinimum, u);
			d = (Math.log(u) - Math.log(s.renderMinimum))
					/ (Math.log(s.renderMaximum) - Math.log(s.renderMinimum))
					* s.renderLength;
		}
		return s.renderStart + d;
	}

	final double mapToDevice(double u) {
		return mapToDevice(u, 0);
	}

	private long getDateNumber(WDate dt) {
		switch (this.scale_) {
		case DateScale:
			return (long) dt.toJulianDay();
		case DateTimeScale:
			return (long) dt.getDate().getTime();
		default:
			return 1;
		}
	}

	private static double EPSILON = 1E-3;
	private static final int TICK_LENGTH = 5;
	private static final int AXIS_MARGIN = 4;
	private static final int AUTO_V_LABEL_PIXELS = 25;
	private static final int AUTO_H_LABEL_PIXELS = 80;

	static double round125(double v) {
		double n = Math.pow(10, Math.floor(Math.log10(v)));
		double msd = v / n;
		if (msd < 1.5) {
			return n;
		} else {
			if (msd < 3.3) {
				return 2 * n;
			} else {
				if (msd < 7) {
					return 5 * n;
				} else {
					return 10 * n;
				}
			}
		}
	}

	static double roundUp125(double v, double t) {
		return t * Math.ceil((v - 1E-10) / t);
	}

	static double roundDown125(double v, double t) {
		return t * Math.floor((v + 1E-10) / t);
	}

	static int roundDown(int v, int factor) {
		return v / factor * factor;
	}

	static WPointF interpolate(final WPointF p1, final WPointF p2, double u) {
		double x = p1.getX();
		if (p2.getX() - p1.getX() > 0) {
			x += u;
		} else {
			if (p2.getX() - p1.getX() < 0) {
				x -= u;
			}
		}
		double y = p1.getY();
		if (p2.getY() - p1.getY() > 0) {
			y += u;
		} else {
			if (p2.getY() - p1.getY() < 0) {
				y -= u;
			}
		}
		return new WPointF(x, y);
	}
}
