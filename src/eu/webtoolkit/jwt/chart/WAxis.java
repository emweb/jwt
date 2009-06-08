package eu.webtoolkit.jwt.chart;

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
 * Class which represents an axis of a cartesian chart.
 * 
 * 
 * A cartesian chart has two or three axes: an X axis ({@link Axis#XAxis XAxis}
 * ), a Y axis ({@link Axis#YAxis YAxis}) and optionally a second Y axis (
 * {@link Axis#Y2Axis Y2Axis}). Each of the up to three axes in a cartesian
 * chart has a unique {@link WAxis#getId()} that identifies which of these three
 * axes it is in the enclosing {@link WAxis#getChart()}.
 * <p>
 * Use {@link WAxis#setVisible(boolean visible)} to change the visibility of an
 * axis, {@link WAxis#setGridLinesEnabled(boolean enabled)} to show grid lines
 * for an axis. The pen styles for rendering the axis or grid lines may be
 * changed using {@link WAxis#setPen(WPen pen)} and
 * {@link WAxis#setGridLinesPen(WPen pen)}. A margin between the axis and the
 * main plot area may be configured using {@link WAxis#setMargin(int pixels)}.
 * <p>
 * By default, the axis will automatically adjust its range so that all data
 * will be visible. You may manually specify a range using
 * {@link WAxis#setMinimum(double minimum)}, setMaximum or
 * {@link WAxis#setRange(double minimum, double maximum)}. The interval between
 * labels is by default automatically adjusted depending on the axis length and
 * the range, but may be manually specified using
 * {@link WAxis#setLabelInterval(double labelInterval)}.
 * <p>
 * The axis has support for being &quot;broken&quot;, to support displaying data
 * with a few outliers which would otherwise swamp the chart. This is not done
 * automatically, but instead you need to use
 * {@link WAxis#setBreak(double minimum, double maximum)} to specify the value
 * range that needs to be omitted from the axis. The omission is rendered in the
 * axis and in bars that cross the break.
 * <p>
 * The labels are shown using a &quot;%.4g&quot; format string for numbers, and
 * &quot;dd/MM/yyyy&quot; (for {@link AxisScale#DateScale DateScale}). The
 * format may be customized using
 * {@link WAxis#setLabelFormat(CharSequence format)}. The angle of the label
 * text may be changed using {@link WAxis#setLabelAngle(double angle)}. By
 * default, all labels are printed horizontally.
 * <p>
 * 
 * @see WCartesianChart
 */
public class WAxis {
	/**
	 * Returns the axis id.
	 * 
	 * @see WAxis#getChart()
	 * @see WCartesianChart#axis(Axis axis)
	 */
	public Axis getId() {
		return this.axis_;
	}

	/**
	 * Set whether this axis is visible.
	 * 
	 * Changes whether the axis is displayed, including ticks and labels. The
	 * rendering of the grid lines is controlled seperately by
	 * {@link WAxis#setGridLinesEnabled(boolean enabled)}.
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
	 * 
	 * @see WAxis#setVisible(boolean visible)
	 */
	public boolean isVisible() {
		return this.visible_;
	}

	/**
	 * Set the axis location.
	 * 
	 * Configures the location of the axis, relative to values on the other
	 * values (i.e. Y values for the X axis, and X values for the Y axis).
	 * <p>
	 * The default value is {@link AxisLocation#MinimumValue}.
	 * <p>
	 * 
	 * @see WAxis#getLocation()
	 */
	public void setLocation(AxisLocation location) {
		if (!ChartUtils.equals(this.location_, location)) {
			this.location_ = location;
			update();
		}
		;
	}

	/**
	 * Returns the axis location.
	 * 
	 * @see WAxis#setLocation(AxisLocation location)
	 */
	public AxisLocation getLocation() {
		return this.location_;
	}

	/**
	 * Set the scale of the axis.
	 * 
	 * For the X scale in a {@link ChartType#CategoryChart CategoryChart}, the
	 * scale should be left unchanged to {@link AxisScale#CategoryScale
	 * CategoryScale}.
	 * <p>
	 * For all other axes, the default value is {@link AxisScale#LinearScale
	 * LinearScale}, but this may be changed to {@link AxisScale#LogScale
	 * LogScale} or {@link AxisScale#DateScale DateScale}.
	 * {@link AxisScale#DateScale DateScale} is only useful for the X axis in a
	 * ScatterPlot which contains {@link WDate} values.
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
	 * 
	 * @see WAxis#setScale(AxisScale scale)
	 */
	public AxisScale getScale() {
		return this.scale_;
	}

	/**
	 * Set the minimum value displayed on the axis.
	 * 
	 * Specify the minimum value to be displayed on the axis. By default, the
	 * minimum and maximum values are determined automatically so that all the
	 * data can be displayed.
	 * <p>
	 * The default value is {@link WAxis#AUTO_MINIMUM AUTO_MINIMUM}, which
	 * indicates that the value must be determined automatically.
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
	 * 
	 * @see WAxis#getMaximum()
	 * @see WAxis#setMinimum(double minimum)
	 */
	public double getMinimum() {
		return this.segments_.get(0).minimum;
	}

	/**
	 * Set the maximum value for the axis displayed on the axis.
	 * 
	 * Specify the maximum value to be displayed on the axis. By default, the
	 * minimum and maximum values are determined automatically so that all the
	 * data can be displayed.
	 * <p>
	 * The default value is {@link WAxis#AUTO_MAXIMUM AUTO_MAXIMUM}, which
	 * indicates that the value must be determined automatically.
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
	 * 
	 * @see WAxis#getMinimum()
	 * @see WAxis#setMaximum(double maximum)
	 */
	public double getMaximum() {
		return this.segments_.get(this.segments_.size() - 1).maximum;
	}

	/**
	 * Set the axis range (minimum and maximum values).
	 * 
	 * Specify both minimum and maximum value for the axis.
	 * <p>
	 * 
	 * @see WAxis#setMinimum(double minimum)
	 * @see WAxis#setMaximum(double maximum)
	 */
	public void setRange(double minimum, double maximum) {
		this.segments_.get(0).minimum = minimum;
		this.segments_.get(this.segments_.size() - 1).maximum = maximum;
		this.update();
	}

	/**
	 * Specify a range that needs to be omitted from the axis.
	 * 
	 * This is useful to display data with a few outliers which would otherwise
	 * swamp the chart. This is not done automatically, but instead you need to
	 * use {@link WAxis#setBreak(double minimum, double maximum)} to specify the
	 * value range that needs to be omitted from the axis. The omission is
	 * rendered in the axis and in BarSeries that cross the break.
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
	 * Set the label interval.
	 * 
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
	 * 
	 * @see WAxis#setLabelInterval(double labelInterval)
	 */
	public double getLabelInterval() {
		return this.labelInterval_;
	}

	/**
	 * Set the label format.
	 * 
	 * Set a format string which is used to format values, both for the axis
	 * labels as well as data series values (see
	 * {@link WDataSeries#setLabelsEnabled(Axis axis, boolean enabled)}).
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
		if (!ChartUtils.equals(this.labelFormat_, new WString(format))) {
			this.labelFormat_ = new WString(format);
			update();
		}
		;
	}

	/**
	 * Returns the label format string.
	 * 
	 * @see WAxis#setLabelFormat(CharSequence format)
	 */
	public WString getLabelFormat() {
		return this.labelFormat_;
	}

	/**
	 * Sets the label angle.
	 * 
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
	 * 
	 * @see WAxis#setLabelAngle(double angle)
	 */
	public double getLabelAngle() {
		return this.labelAngle_;
	}

	/**
	 * Sets whether gridlines are displayed for this axis.
	 * 
	 * When <i>enabled</i>, gird lines are drawn for each tick on this axis,
	 * using the {@link WAxis#getGridLinesPen()}.
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
	 * 
	 * @see WAxis#setGridLinesEnabled(boolean enabled)
	 */
	public boolean isGridLinesEnabled() {
		return this.gridLines_;
	}

	/**
	 * Changes the pen used for rendering the axis and ticks.
	 * 
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
	 * 
	 * @see WAxis#setPen(WPen pen)
	 */
	public WPen getPen() {
		return this.pen_;
	}

	/**
	 * Changes the pen used for rendering the grid lines.
	 * 
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
	 * 
	 * @see WAxis#setGridLinesPen(WPen pen)
	 */
	public WPen getGridLinesPen() {
		return this.gridLinesPen_;
	}

	/**
	 * Sets the margin between the axis and the plot area.
	 * 
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
	 * 
	 * @see WAxis#setMargin(int pixels)
	 */
	public int getMargin() {
		return this.margin_;
	}

	/**
	 * Set the axis title.
	 * 
	 * The default title is empty.
	 * <p>
	 * 
	 * @see WAxis#getTitle()
	 */
	public void setTitle(CharSequence title) {
		if (!ChartUtils.equals(this.title_, new WString(title))) {
			this.title_ = new WString(title);
			update();
		}
		;
	}

	/**
	 * Returns the axis title.
	 * 
	 * @see WAxis#setTitle(CharSequence title)
	 */
	public WString getTitle() {
		return this.title_;
	}

	/**
	 * Set the axis title font.
	 * 
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
	 * 
	 * @see WAxis#setTitle(CharSequence title)
	 */
	public WFont getTitleFont() {
		return this.titleFont_;
	}

	public WString label(double u) {
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
	 * 
	 * @see WCartesianChart#axis(Axis axis)
	 */
	public WCartesianChart getChart() {
		return this.chart_;
	}

	public int getSegmentCount() {
		return this.segments_.size();
	}

	private WCartesianChart chart_;
	private Axis axis_;
	private boolean visible_;
	private AxisLocation location_;
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
		}
	}

	List<WAxis.Segment> segments_;
	private double renderInterval_;

	WAxis() {
		this.chart_ = null;
		this.axis_ = Axis.XAxis;
		this.visible_ = true;
		this.location_ = AxisLocation.MinimumValue;
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
		this.segments_ = new ArrayList<WAxis.Segment>();
		this.titleFont_.setFamily(WFont.GenericFamily.SansSerif);
		this.titleFont_.setSize(WFont.Size.FixedSize, new WLength(12,
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
		double totalRenderLength = vertical ? renderer.getChartArea()
				.getHeight() : renderer.getChartArea().getWidth();
		double totalRenderStart = vertical ? renderer.getChartArea()
				.getBottom() : renderer.getChartArea().getLeft();
		final double SEGMENT_MARGIN = 40;
		totalRenderLength -= SEGMENT_MARGIN * (this.segments_.size() - 1);
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
							this.renderInterval_ = Math.max(1.0, Math
									.floor(this.chart_.getModel().getRowCount()
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
		if (this.scale_ == AxisScale.CategoryScale) {
			segment.renderMinimum = -0.5;
			segment.renderMaximum = this.chart_.getModel().getRowCount() - 0.5;
		} else {
			segment.renderMinimum = segment.minimum;
			segment.renderMaximum = segment.maximum;
			boolean findMinimum = segment.renderMinimum == AUTO_MINIMUM;
			boolean findMaximum = segment.renderMaximum == AUTO_MAXIMUM;
			if (findMinimum || findMaximum) {
				double minimum = Double.MAX_VALUE;
				double maximum = Double.MIN_VALUE;
				if (this.axis_ == Axis.XAxis) {
					int dataColumn = this.chart_.XSeriesColumn();
					if (dataColumn != -1) {
						WAbstractItemModel model = this.chart_.getModel();
						for (int i = 0; i < model.getRowCount(); ++i) {
							double v;
							if (this.scale_ != AxisScale.DateScale) {
								v = StringUtils.asNumber(model.getData(i,
										dataColumn));
							} else {
								v = getDateValue(model.getData(i, dataColumn));
							}
							if (myisnan(v)) {
								continue;
							}
							if (findMaximum) {
								maximum = Math.max(v, maximum);
							}
							if (findMaximum) {
								minimum = Math.min(v, minimum);
							}
						}
					}
				} else {
					ExtremesIterator iterator = new ExtremesIterator(this.axis_);
					renderer.iterateSeries(iterator);
					minimum = iterator.getMinimum();
					maximum = iterator.getMaximum();
				}
				if (minimum == Double.MAX_VALUE) {
					if (this.scale_ == AxisScale.LogScale) {
						minimum = 1;
					} else {
						minimum = 0;
					}
				}
				if (maximum == -Double.MAX_VALUE) {
					if (this.scale_ == AxisScale.LogScale) {
						maximum = 10;
					} else {
						maximum = 100;
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
								.log10(segment.renderMinimum - 1E-4)));
					}
					if (findMaximum) {
						segment.renderMaximum = Math.pow(10, Math.ceil(Math
								.log10(segment.renderMaximum + 1E-4)));
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

	static class TickLabel {
		public enum TickLength {
			Zero, Short, Long;

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
			this.label = new WString(l);
		}

		public TickLabel(double v, WAxis.TickLabel.TickLength length) {
			this(v, length, new WString());
		}
	}

	void getLabelTicks(WChart2DRenderer renderer, List<WAxis.TickLabel> ticks,
			int segment) {
		WAxis.Segment s = this.segments_.get(segment);
		switch (this.scale_) {
		case CategoryScale: {
			int renderInterval = Math.max(1, (int) this.renderInterval_);
			if (renderInterval == 1) {
				ticks.add(new WAxis.TickLabel(-0.5,
						WAxis.TickLabel.TickLength.Long));
				for (int i = 0; i < this.chart_.getModel().getRowCount(); ++i) {
					ticks.add(new WAxis.TickLabel(i + 0.5,
							WAxis.TickLabel.TickLength.Long));
					ticks.add(new WAxis.TickLabel(i,
							WAxis.TickLabel.TickLength.Zero, this
									.label((double) i)));
				}
			} else {
				for (int i = 0; i < this.chart_.getModel().getRowCount(); i += renderInterval) {
					ticks.add(new WAxis.TickLabel(i,
							WAxis.TickLabel.TickLength.Long, this
									.label((double) i)));
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
					t = this.label(v);
				}
				ticks.add(new WAxis.TickLabel(v,
						i % 2 == 0 ? WAxis.TickLabel.TickLength.Long
								: WAxis.TickLabel.TickLength.Short, t));
			}
			break;
		}
		case LogScale: {
			double v = s.renderMinimum;
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
							WAxis.TickLabel.TickLength.Long, this.label(v)));
				} else {
					ticks.add(new WAxis.TickLabel(v,
							WAxis.TickLabel.TickLength.Short));
				}
				v += p;
			}
			break;
		}
		case DateScale: {
			long daysRange = (long) (s.renderMaximum - s.renderMinimum);
			double numLabels = this.calcAutoNumLabels(s);
			double days = daysRange / numLabels;
			final int Days = 0;
			final int Months = 1;
			final int Years = 2;
			int unit;
			int interval;
			WDate d = WDate.fromJulianDay((int) s.renderMinimum);
			if (days > 200) {
				unit = Years;
				interval = Math.max(1, (int) round125(days / 365));
				if (d.getDay() != 1 && d.getMonth() != 1) {
					d.setDate(d.getYear(), 1, 1);
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
					if (d.getDay() != 1) {
						d.setDate(d.getYear(), d.getMonth(), 1);
					}
					if ((d.getMonth() - 1) % interval != 0) {
						int m = (d.getMonth() - 1) / interval * interval + 1;
						d.setDate(d.getYear(), m, 1);
					}
				} else {
					unit = Days;
					if (days < 1.3) {
						interval = 1;
					} else {
						interval = 7 * Math.max(1, (int) ((days + 5) / 7));
					}
				}
			}
			boolean atTick = interval > 1 || unit == Days;
			for (;;) {
				long dl = d.toJulianDay();
				if (dl > s.renderMaximum) {
					break;
				}
				WDate next = null;
				switch (unit) {
				case Years:
					next = d.addYears(interval);
					break;
				case Months:
					next = d.addMonths(interval);
					break;
				case Days:
					next = d.addDays(interval);
				}
				WString text = new WString();
				if (!(this.labelFormat_.length() == 0)) {
					text = new WString(d.toString(this.labelFormat_.toString()));
				} else {
					if (atTick) {
						text = new WString(d.toString("dd/MM/yy"));
					} else {
						switch (unit) {
						case Months:
							text = new WString(d.toString("MMM yy"));
							break;
						case Years:
							text = new WString(d.toString("yyyy"));
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
					double tl = (next.toJulianDay() + dl) / 2;
					if (tl >= s.renderMinimum && tl <= s.renderMaximum) {
						ticks.add(new WAxis.TickLabel((double) tl,
								WAxis.TickLabel.TickLength.Zero, text));
					}
				}
				d = next;
			}
			break;
		}
		}
	}

	double map(double u, AxisLocation otherLocation, int segment) {
		if (myisnan(u)) {
			return u;
		}
		WAxis.Segment s = this.segments_.get(segment);
		double d;
		if (this.scale_ != AxisScale.LogScale) {
			int borderMin;
			int borderMax;
			if (this.scale_ == AxisScale.CategoryScale) {
				borderMin = borderMax = 5;
			} else {
				borderMin = s.renderMinimum == 0
						&& otherLocation == AxisLocation.ZeroValue ? 0 : 5;
				borderMax = s.renderMinimum == 0
						&& otherLocation == AxisLocation.ZeroValue ? 0 : 5;
			}
			int remainLength = (int) s.renderLength - borderMin - borderMax;
			d = borderMin + (u - s.renderMinimum)
					/ (s.renderMaximum - s.renderMinimum) * remainLength;
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

	private double map(Object value, AxisLocation otherLocation, int segment) {
		assert this.scale_ != AxisScale.CategoryScale;
		if (this.scale_ == AxisScale.LinearScale
				|| this.scale_ == AxisScale.LogScale) {
			return this
					.map(StringUtils.asNumber(value), otherLocation, segment);
		} else {
			return this.map(getDateValue(value), otherLocation, segment);
		}
	}

	private double map(int rowIndex, int columnIndex,
			AxisLocation otherLocation, int segment) {
		if (this.scale_ == AxisScale.CategoryScale) {
			return this.map((double) rowIndex, otherLocation, segment);
		} else {
			return this.map(this.chart_.getModel().getData(rowIndex,
					columnIndex), otherLocation, segment);
		}
	}

	private static double getDateValue(Object v) {
		if (!v.getClass().equals(WDate.class)) {
			return Double.NaN;
		} else {
			WDate d = (WDate) v;
			return (double) d.toJulianDay();
		}
	}

	private double calcAutoNumLabels(WAxis.Segment s) {
		boolean vertical = this.axis_ != Axis.XAxis == (this.chart_
				.getOrientation() == Orientation.Vertical);
		return s.renderLength
				/ (vertical ? AUTO_V_LABEL_PIXELS : AUTO_H_LABEL_PIXELS);
	}

	private static double EPSILON = 1E-3;
	static final int AXIS_MARGIN = 4;
	static final int AUTO_V_LABEL_PIXELS = 25;
	static final int AUTO_H_LABEL_PIXELS = 60;

	static boolean myisnan(double d) {
		return !(d == d);
	}

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
	 * 
	 * @see WAxis#setMinimum(double minimum)
	 */
	public static final double AUTO_MINIMUM = Double.MAX_VALUE;
	/**
	 * Constant which indicates automatic maximum calculation.
	 * 
	 * @see WAxis#setMaximum(double maximum)
	 */
	public static final double AUTO_MAXIMUM = -Double.MAX_VALUE;
}
