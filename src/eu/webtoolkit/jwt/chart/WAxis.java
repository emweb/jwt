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

/**
 * Class which represents an axis of a cartesian chart.
 * <p>
 * 
 * A cartesian chart has two or three axes: an X axis ({@link Axis#XAxis XAxis}
 * ), a Y axis ({@link Axis#YAxis YAxis}) and optionally a second Y axis (
 * {@link Axis#Y2Axis Y2Axis}). Each of the up to three axes in a cartesian
 * chart has a unique {@link WAxis#getId() getId()} that identifies which of
 * these three axes it is in the enclosing {@link WAxis#getChart() getChart()}.
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
 * &quot;dd/MM/yyyy&quot; (for {@link AxisScale#DateScale DateScale}). The
 * format may be customized using
 * {@link WAxis#setLabelFormat(CharSequence format) setLabelFormat()}. The angle
 * of the label text may be changed using
 * {@link WAxis#setLabelAngle(double angle) setLabelAngle()}. By default, all
 * labels are printed horizontally.
 * <p>
 * 
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
 */
public class WAxis {
	/**
	 * Returns the axis id.
	 * <p>
	 * 
	 * @see WAxis#getChart()
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
	 * Configures the location of the axis, relative to values on the other
	 * values (i.e. Y values for the X axis, and X values for the Y axis).
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
	 * Specify the minimum value to be displayed on the axis. By default, the
	 * minimum and maximum values are determined automatically so that all the
	 * data can be displayed.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#getMinimum()
	 * @see WAxis#setMaximum(double maximum)
	 */
	public void setMinimum(double minimum) {
		WAxis.Segment s = this.segments_.get(0);
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
	}

	/**
	 * Returns the minimum value displayed on the axis.
	 * <p>
	 * This returned the minimum value that was set using
	 * {@link WAxis#setMinimum(double minimum) setMinimum()}, or otherwise the
	 * automatically calculated minimum.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#getMaximum()
	 * @see WAxis#setMinimum(double minimum)
	 * @see WAxis#setAutoLimits(EnumSet locations)
	 */
	public double getMinimum() {
		return !EnumUtils.mask(this.getAutoLimits(), AxisValue.MinimumValue)
				.isEmpty() ? this.segments_.get(0).renderMinimum
				: this.segments_.get(0).minimum;
	}

	/**
	 * Sets the maximum value for the axis displayed on the axis.
	 * <p>
	 * Specify the maximum value to be displayed on the axis. By default, the
	 * minimum and maximum values are determined automatically so that all the
	 * data can be displayed.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#getMinimum()
	 * @see WAxis#setMinimum(double minimum)
	 */
	public void setMaximum(double maximum) {
		WAxis.Segment s = this.segments_.get(this.segments_.size() - 1);
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
	}

	/**
	 * Returns the maximum value displayed on the axis.
	 * <p>
	 * This returned the maximum value that was set using
	 * {@link WAxis#setMaximum(double maximum) setMaximum()}, or otherwise the
	 * automatically calculated maximum.
	 * <p>
	 * The numerical value corresponding to a data point is defined by it&apos;s
	 * AxisScale type.
	 * <p>
	 * 
	 * @see WAxis#getMinimum()
	 * @see WAxis#setMaximum(double maximum)
	 */
	public double getMaximum() {
		WAxis.Segment s = this.segments_.get(this.segments_.size() - 1);
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
			this.update();
		}
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
		}
		if (!EnumUtils.mask(locations, AxisValue.MaximumValue).isEmpty()) {
			if (!ChartUtils.equals(this.segments_
					.get(this.segments_.size() - 1).maximum, AUTO_MAXIMUM)) {
				this.segments_.get(this.segments_.size() - 1).maximum = AUTO_MAXIMUM;
				update();
			}
			;
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
	 * Specify a range that needs to be omitted from the axis.
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
	 * double. If the format string is an empty string, &quot;%.4g&quot; is
	 * used.
	 * <p>
	 * For an axis with a {@link AxisScale#DateScale DateScale} scale, the
	 * format string must be a format string accepted by WDate::toString(const
	 * WString&amp;), to format a date. If the format string is an empty string,
	 * &quot;dd/MM/yyyy&quot;, &quot;MMM yy&quot; or &quot;yyyy&quot; is used
	 * depending on the situation.
	 * <p>
	 * The default value is an empty string (&quot;&quot;).
	 * <p>
	 * 
	 * @see WAxis#getLabelFormat()
	 */
	public void setLabelFormat(CharSequence format) {
		if (!ChartUtils.equals(this.labelFormat_, WString.toWString(format))) {
			this.labelFormat_ = WString.toWString(format);
			update();
		}
		;
	}

	/**
	 * Returns the label format string.
	 * <p>
	 * 
	 * @see WAxis#setLabelFormat(CharSequence format)
	 */
	public WString getLabelFormat() {
		return this.labelFormat_;
	}

	/**
	 * Sets the label angle.
	 * <p>
	 * Sets the angle used for displaying the labels (in degrees). A 0 angle
	 * corresponds to horizontal text. Note that this option is only supported
	 * by the InlineSvgVml renderers, but not by HtmlCanvas.
	 * <p>
	 * The default value is 0.0 (&quot;horizontal text&quot;).
	 * <p>
	 * 
	 * @see WAxis#getLabelAngle()
	 */
	public void setLabelAngle(double angle) {
		if (!ChartUtils.equals(this.labelAngle_, angle)) {
			this.labelAngle_ = angle;
			update();
		}
		;
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
	 * not controlled by setDisplayEnabled(bool).
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
	public void setPen(WPen pen) {
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
	public void setGridLinesPen(WPen pen) {
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
	public void setTitle(CharSequence title) {
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
	public void setTitleFont(WFont titleFont) {
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

	/**
	 * Sets the axis label font.
	 * <p>
	 * The default label font is a 10 point Sans Serif font.
	 * <p>
	 * 
	 * @see WAxis#getLabelFont()
	 */
	public void setLabelFont(WFont labelFont) {
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

	WString getLabel(double u) {
		String buf = null;
		WString text = new WString();
		if (this.scale_ == AxisScale.CategoryScale) {
			if (this.chart_.XSeriesColumn() != -1) {
				text = StringUtils.asString(this.chart_.getModel().getData(
						(int) u, this.chart_.XSeriesColumn()));
			} else {
				buf = String.format("%.4g", u + 1);
				text = new WString(buf);
			}
		} else {
			if (this.scale_ == AxisScale.DateScale) {
				WDate d = WDate.fromJulianDay((int) u);
				WString format = this.labelFormat_;
				if ((format.length() == 0)) {
					return new WString(d.toString("dd/MM/yyyy"));
				} else {
					return new WString(d.toString(format.toString()));
				}
			} else {
				String format = this.labelFormat_.toString();
				if (format.length() == 0) {
					format = "%.4g";
				}
				buf = String.format(format, u);
				text = new WString(buf);
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
	public WCartesianChart getChart() {
		return this.chart_;
	}

	int getSegmentCount() {
		return (int) this.segments_.size();
	}

	private WCartesianChart chart_;
	private Axis axis_;
	private boolean visible_;
	private AxisValue location_;
	private AxisScale scale_;
	private double labelInterval_;
	private WString labelFormat_;
	private boolean gridLines_;
	private WPen pen_;
	private WPen gridLinesPen_;
	private int margin_;
	private double labelAngle_;
	private WString title_;
	private WFont titleFont_;
	private WFont labelFont_;

	static class Segment {
		public double minimum;
		public double maximum;
		public double renderMinimum;
		public double renderMaximum;
		public double renderLength;
		public double renderStart;

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
		this.labelInterval_ = 0;
		this.labelFormat_ = new WString();
		this.gridLines_ = false;
		this.pen_ = new WPen();
		this.gridLinesPen_ = new WPen(WColor.gray);
		this.margin_ = 0;
		this.labelAngle_ = 0;
		this.title_ = new WString();
		this.titleFont_ = new WFont();
		this.labelFont_ = new WFont();
		this.segments_ = new ArrayList<WAxis.Segment>();
		this.titleFont_.setFamily(WFont.GenericFamily.SansSerif);
		this.titleFont_.setSize(WFont.Size.FixedSize, new WLength(12,
				WLength.Unit.Point));
		this.labelFont_.setFamily(WFont.GenericFamily.SansSerif);
		this.labelFont_.setSize(WFont.Size.FixedSize, new WLength(10,
				WLength.Unit.Point));
		this.segments_.add(new WAxis.Segment());
	}

	void init(WCartesianChart chart, Axis axis) {
		this.chart_ = chart;
		this.axis_ = axis;
		if (axis == Axis.XAxis) {
			if (chart.getType() == ChartType.CategoryChart) {
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

	// private boolean (T m, T v) ;
	void prepareRender(WChart2DRenderer renderer) {
		double totalRenderRange = 0;
		for (int i = 0; i < this.segments_.size(); ++i) {
			WAxis.Segment s = this.segments_.get(i);
			this.computeRange(renderer, s);
			totalRenderRange += s.renderMaximum - s.renderMinimum;
		}
		boolean vertical = this.axis_ != Axis.XAxis;
		double clipMin = this.segments_.get(0).renderMinimum == 0 ? 0
				: CLIP_MARGIN;
		double clipMax = this.segments_.get(this.segments_.size() - 1).renderMaximum == 0 ? 0
				: CLIP_MARGIN;
		double totalRenderLength = vertical ? renderer.getChartArea()
				.getHeight() : renderer.getChartArea().getWidth();
		double totalRenderStart = vertical ? renderer.getChartArea()
				.getBottom()
				- clipMin : renderer.getChartArea().getLeft() + clipMin;
		final double SEGMENT_MARGIN = 40;
		totalRenderLength -= SEGMENT_MARGIN * (this.segments_.size() - 1)
				+ clipMin + clipMax;
		int rc = 0;
		if (this.chart_.getModel() != null) {
			rc = this.chart_.getModel().getRowCount();
		}
		for (int it = 0; it < 2; ++it) {
			double rs = totalRenderStart;
			double TRR = totalRenderRange;
			totalRenderRange = 0;
			for (int i = 0; i < this.segments_.size(); ++i) {
				WAxis.Segment s = this.segments_.get(i);
				double diff = s.renderMaximum - s.renderMinimum;
				s.renderStart = rs;
				s.renderLength = diff / TRR * totalRenderLength;
				if (i == 0) {
					this.renderInterval_ = this.labelInterval_;
					if (this.renderInterval_ == 0) {
						if (this.scale_ == AxisScale.CategoryScale) {
							double numLabels = this.calcAutoNumLabels(s) / 1.5;
							this.renderInterval_ = Math.max(1.0, Math.floor(rc
									/ numLabels));
						} else {
							if (this.scale_ == AxisScale.LinearScale) {
								double numLabels = this.calcAutoNumLabels(s);
								this.renderInterval_ = round125(diff
										/ numLabels);
							}
						}
					}
				}
				if (this.scale_ == AxisScale.LinearScale) {
					if (it == 0) {
						if (s.minimum == AUTO_MINIMUM) {
							s.renderMinimum = roundDown125(s.renderMinimum,
									this.renderInterval_);
						}
						if (s.maximum == AUTO_MAXIMUM) {
							s.renderMaximum = roundUp125(s.renderMaximum,
									this.renderInterval_);
						}
					}
				}
				totalRenderRange += s.renderMaximum - s.renderMinimum;
				if (this.axis_ == Axis.XAxis) {
					rs += s.renderLength + SEGMENT_MARGIN;
				} else {
					rs -= s.renderLength + SEGMENT_MARGIN;
				}
			}
		}
	}

	private void computeRange(WChart2DRenderer renderer, WAxis.Segment segment) {
		int rc = 0;
		if (this.chart_.getModel() != null) {
			rc = this.chart_.getModel().getRowCount();
		}
		if (this.scale_ == AxisScale.CategoryScale) {
			segment.renderMinimum = -0.5;
			segment.renderMaximum = rc - 0.5;
		} else {
			segment.renderMinimum = segment.minimum;
			segment.renderMaximum = segment.maximum;
			boolean findMinimum = segment.renderMinimum == AUTO_MINIMUM;
			boolean findMaximum = segment.renderMaximum == AUTO_MAXIMUM;
			if (findMinimum || findMaximum) {
				double minimum = Double.MAX_VALUE;
				double maximum = -Double.MAX_VALUE;
				ExtremesIterator iterator = new ExtremesIterator(this.axis_,
						this.scale_);
				renderer.iterateSeries(iterator);
				minimum = iterator.getMinimum();
				maximum = iterator.getMaximum();
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
			if (Math.abs(diff) < 1E-10) {
				if (this.scale_ == AxisScale.LogScale) {
					if (findMinimum) {
						segment.renderMinimum = Math.pow(10, Math.floor(Math
								.log10(segment.renderMinimum - 0.1)));
					}
					if (findMaximum) {
						segment.renderMaximum = Math.pow(10, Math.ceil(Math
								.log10(segment.renderMaximum + 0.1)));
					}
				} else {
					if (findMinimum) {
						segment.renderMinimum = Math
								.floor(segment.renderMinimum - 1E-4);
					}
					if (findMaximum) {
						segment.renderMaximum = Math
								.ceil(segment.renderMaximum + 1E-4);
					}
				}
				diff = segment.renderMaximum - segment.renderMinimum;
			}
			if (this.scale_ == AxisScale.LinearScale) {
				if (findMinimum && segment.renderMinimum >= 0
						&& segment.renderMinimum - 0.50 * diff <= 0) {
					segment.renderMinimum = 0;
				}
				if (findMaximum && segment.renderMaximum <= 0
						&& segment.renderMaximum + 0.50 * diff >= 0) {
					segment.renderMaximum = 0;
				}
			} else {
				if (this.scale_ == AxisScale.LogScale) {
					double minLog10 = Math.floor(Math
							.log10(segment.renderMinimum));
					double maxLog10 = Math.ceil(Math
							.log10(segment.renderMaximum));
					if (findMinimum) {
						segment.renderMinimum = Math.pow(10, minLog10);
					}
					if (findMinimum) {
						segment.renderMaximum = Math.pow(10, maxLog10);
					}
				}
			}
		}
	}

	void setOtherAxisLocation(AxisValue otherLocation) {
		if (this.scale_ != AxisScale.LogScale) {
			for (int i = 0; i < this.segments_.size(); ++i) {
				WAxis.Segment s = this.segments_.get(i);
				int borderMin;
				int borderMax;
				if (this.scale_ == AxisScale.CategoryScale) {
					borderMin = borderMax = 5;
				} else {
					borderMin = s.renderMinimum == 0
							&& otherLocation == AxisValue.ZeroValue ? 0 : 5;
					borderMax = s.renderMinimum == 0
							&& otherLocation == AxisValue.ZeroValue ? 0 : 5;
				}
				s.renderLength -= borderMin + borderMax;
				if (this.axis_ == Axis.XAxis) {
					s.renderStart += borderMin;
				} else {
					s.renderStart -= borderMin;
				}
			}
		}
	}

	static class TickLabel {
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
				CharSequence l) {
			this.u = v;
			this.tickLength = length;
			this.label = WString.toWString(l);
		}

		public TickLabel(double v, WAxis.TickLabel.TickLength length) {
			this(v, length, new WString());
		}
	}

	void getLabelTicks(WChart2DRenderer renderer, List<WAxis.TickLabel> ticks,
			int segment) {
		WAxis.Segment s = this.segments_.get(segment);
		int rc = 0;
		if (this.chart_.getModel() != null) {
			rc = this.chart_.getModel().getRowCount();
		}
		switch (this.scale_) {
		case CategoryScale: {
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
				if (v - s.renderMaximum > MathUtils.EPSILON
						* this.renderInterval_) {
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
				if (v - s.renderMaximum > MathUtils.EPSILON * s.renderMaximum) {
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
			double daysRange = 0.0;
			WDate dt = null;
			switch (this.scale_) {
			case DateScale:
				daysRange = (double) (s.renderMaximum - s.renderMinimum);
				dt = WDate.fromJulianDay((int) s.renderMinimum);
				if (!(dt != null)) {
					String exception = "Invalid julian day: ";
					exception += String.valueOf(s.renderMinimum);
					throw new WtException(exception);
				}
				break;
			case DateTimeScale:
				daysRange = (double) ((s.renderMaximum - s.renderMinimum) / (60.0 * 60.0 * 24));
				dt = new WDate(new Date((long) (long) s.renderMinimum));
				break;
			default:
				assert false;
			}
			double numLabels = this.calcAutoNumLabels(s);
			double days = daysRange / numLabels;
			final int Days = 0;
			final int Months = 1;
			final int Years = 2;
			final int Hours = 3;
			final int Minutes = 4;
			int unit;
			int interval;
			if (days > 200) {
				unit = Years;
				interval = Math.max(1, (int) round125(days / 365));
				if (dt.getDay() != 1 && dt.getMonth() != 1) {
					dt.setDate(dt.getYear(), 1, 1);
				}
			} else {
				if (days > 20) {
					unit = Months;
					double i = days / 30;
					if (i < 1.3) {
						interval = 1;
					} else {
						if (i < 2.3) {
							interval = 2;
						} else {
							if (i < 3.3) {
								interval = 3;
							} else {
								if (i < 4.3) {
									interval = 4;
								} else {
									interval = 6;
								}
							}
						}
					}
					if (dt.getDay() != 1) {
						dt.setDate(dt.getYear(), dt.getMonth(), 1);
					}
					if ((dt.getMonth() - 1) % interval != 0) {
						int m = (dt.getMonth() - 1) / interval * interval + 1;
						dt.setDate(dt.getYear(), m, 1);
					}
				} else {
					if (days > 0.6) {
						unit = Days;
						if (days < 1.3) {
							interval = 1;
						} else {
							interval = 7 * Math.max(1, (int) ((days + 5) / 7));
						}
					} else {
						double minutes = days * 24 * 60;
						if (minutes > 40) {
							unit = Hours;
							double i = minutes / 60;
							if (i < 1.3) {
								interval = 1;
							} else {
								if (i < 2.3) {
									interval = 2;
								} else {
									if (i < 3.3) {
										interval = 3;
									} else {
										if (i < 4.3) {
											interval = 4;
										} else {
											if (i < 6.3) {
												interval = 6;
											} else {
												interval = 12;
											}
										}
									}
								}
							}
						} else {
							unit = Minutes;
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
						}
					}
				}
			}
			boolean atTick = interval > 1 || unit <= Days;
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
				}
				WString text = new WString();
				if (!(this.labelFormat_.length() == 0)) {
					text = new WString(dt
							.toString(this.labelFormat_.toString()));
				} else {
					if (atTick) {
						switch (unit) {
						case Months:
						case Years:
						case Days:
							text = new WString(dt.toString("dd/MM/yy"));
							break;
						case Hours:
							text = new WString(dt.toString("h'h' dd/MM"));
							break;
						case Minutes:
							text = new WString(dt.toString("hh:mm"));
							break;
						default:
							break;
						}
					} else {
						switch (unit) {
						case Months:
							text = new WString(dt.toString("MMM yy"));
							break;
						case Years:
							text = new WString(dt.toString("yyyy"));
							break;
						case Hours:
							text = new WString(dt.toString("h'h' dd/MM"));
							break;
						case Minutes:
							text = new WString(dt.toString("hh:mm"));
							break;
						default:
							break;
						}
					}
				}
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

	private double getValue(Object v) {
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

	private double calcAutoNumLabels(WAxis.Segment s) {
		boolean vertical = this.axis_ != Axis.XAxis == (this.chart_
				.getOrientation() == Orientation.Vertical);
		return s.renderLength
				/ (vertical ? AUTO_V_LABEL_PIXELS : AUTO_H_LABEL_PIXELS);
	}

	double mapFromDevice(double d) {
		for (int i = 0; i < this.segments_.size(); ++i) {
			WAxis.Segment s = this.segments_.get(i);
			boolean lastSegment = i == this.segments_.size() - 1;
			if (lastSegment || d < this.mapToDevice(s.renderMaximum, i)) {
				if (this.axis_ == Axis.XAxis) {
					d = d - s.renderStart;
				} else {
					d = s.renderStart - d;
				}
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

	double mapToDevice(Object value, int segment) {
		assert this.scale_ != AxisScale.CategoryScale;
		return this.mapToDevice(this.getValue(value), segment);
	}

	final double mapToDevice(Object value) {
		return mapToDevice(value, 0);
	}

	double mapToDevice(double u, int segment) {
		if (Double.isNaN(u)) {
			return u;
		}
		WAxis.Segment s = this.segments_.get(segment);
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
		if (this.axis_ == Axis.XAxis) {
			return s.renderStart + d;
		} else {
			return s.renderStart - d;
		}
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

	private static final int CLIP_MARGIN = 5;
	private static double EPSILON = 1E-3;
	static final int AXIS_MARGIN = 4;
	static final int AUTO_V_LABEL_PIXELS = 25;
	static final int AUTO_H_LABEL_PIXELS = 60;

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

	/**
	 * Constant which indicates automatic minimum calculation.
	 * <p>
	 * 
	 * @see WAxis#setMinimum(double minimum)
	 */
	public static final double AUTO_MINIMUM = Double.MAX_VALUE;
	/**
	 * Constant which indicates automatic maximum calculation.
	 * <p>
	 * 
	 * @see WAxis#setMaximum(double maximum)
	 */
	public static final double AUTO_MAXIMUM = -Double.MAX_VALUE;
}
