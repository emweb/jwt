/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import java.util.EnumSet;
import eu.webtoolkit.jwt.WBrush;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WPen;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A single data series in a cartesian chart.
 * <p>
 * 
 * This class configures all aspects for rendering a single data series in a
 * cartesian chart. A data series renders Y data from a single model column
 * against the X series configured for the chart.
 * <p>
 * The data column should contain data that can be converted to a number, but
 * should not necessarily be of a number type, see also
 * {@link eu.webtoolkit.jwt.utils.StringUtils#asNumber(Object)}.
 * <p>
 * Multiple series of different types may be combined on a single chart.
 * <p>
 * <div align="center"> <img src="doc-files//ChartWDataSeries-1.png"
 * alt="Different styles of data series">
 * <p>
 * <strong>Different styles of data series</strong>
 * </p>
 * </div> For a category chart, series may be stacked on top of each other. This
 * is controlled by {@link WDataSeries#setStacked(boolean stacked) setStacked()}
 * for a series, which if enabled, will stack that series on top of the
 * preceding data series. This works regardless of whether they are of the same
 * type, but obviously works visually best if these series are of the same type.
 * When not stacked, bar series are rendered next to each other. The margin
 * between bars of different data series is controlled using
 * {@link WCartesianChart#setBarMargin(double margin)
 * WCartesianChart#setBarMargin()}.
 * <p>
 * The line and color type are by default based on the
 * {@link WAbstractChart#getPalette() chart palette}, but may be overridden for
 * a series using {@link WDataSeries#setPen(WPen pen) setPen()},
 * {@link WDataSeries#setBrush(WBrush brush) setBrush()}, etc...
 * <p>
 * 
 * @see WCartesianChart#addSeries(WDataSeries series)
 */
public class WDataSeries {
	/**
	 * Enumeration that indicates an aspect of the look.
	 * <p>
	 * These flags are used to keep track of which aspects of the look that are
	 * overridden from the values provided by the chart palette, using one of
	 * the methods in this class.
	 * <p>
	 * 
	 * @see WDataSeries#setPen(WPen pen)
	 * @see WDataSeries#setBrush(WBrush brush)
	 * @see WDataSeries#setMarkerPen(WPen pen)
	 * @see WDataSeries#setMarkerBrush(WBrush brush)
	 * @see WDataSeries#setLabelColor(WColor color)
	 */
	public enum CustomFlag {
		/**
		 * A custom pen is set.
		 */
		CustomPen,
		/**
		 * A custom brush is set.
		 */
		CustomBrush,
		/**
		 * A custom marker pen is set.
		 */
		CustomMarkerPen,
		/**
		 * A custom marker brush is set.
		 */
		CustomMarkerBrush,
		/**
		 * A custom label color is set.
		 */
		CustomLabelColor;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Construct a new data series.
	 * <p>
	 * Creates a new data series which plots the Y values from the model column
	 * <i>modelColumn</i>, with the indicated <i>seriesType</i>. The Y values
	 * are mapped to the indicated <i>axis</i>, which should correspond to one
	 * of the two Y axes.
	 * <p>
	 * 
	 * @see WCartesianChart#addSeries(WDataSeries series)
	 */
	public WDataSeries(int modelColumn, SeriesType type, Axis axis) {
		this.chart_ = null;
		this.modelColumn_ = modelColumn;
		this.stacked_ = false;
		this.type_ = type;
		this.axis_ = axis;
		this.customFlags_ = EnumSet.noneOf(WDataSeries.CustomFlag.class);
		this.pen_ = new WPen();
		this.markerPen_ = new WPen();
		this.brush_ = new WBrush();
		this.markerBrush_ = new WBrush();
		this.labelColor_ = new WColor();
		this.fillRange_ = FillRangeType.NoFill;
		this.marker_ = type == SeriesType.PointSeries ? MarkerType.CircleMarker
				: MarkerType.NoMarker;
		this.legend_ = true;
		this.xLabel_ = false;
		this.yLabel_ = false;
	}

	/**
	 * Construct a new data series.
	 * <p>
	 * Calls {@link #WDataSeries(int modelColumn, SeriesType type, Axis axis)
	 * this(modelColumn, SeriesType.PointSeries, Axis.Y1Axis)}
	 */
	public WDataSeries(int modelColumn) {
		this(modelColumn, SeriesType.PointSeries, Axis.Y1Axis);
	}

	/**
	 * Construct a new data series.
	 * <p>
	 * Calls {@link #WDataSeries(int modelColumn, SeriesType type, Axis axis)
	 * this(modelColumn, type, Axis.Y1Axis)}
	 */
	public WDataSeries(int modelColumn, SeriesType type) {
		this(modelColumn, type, Axis.Y1Axis);
	}

	/**
	 * Change the series type.
	 * <p>
	 * The series type specifies how the data is plotted, i.e. using mere point
	 * markers, lines, curves, or bars.
	 */
	public void setType(SeriesType type) {
		if (!ChartUtils.equals(this.type_, type)) {
			this.type_ = type;
			update();
		}
		;
	}

	/**
	 * Returns the series type.
	 * <p>
	 * 
	 * @see WDataSeries#setType(SeriesType type)
	 */
	public SeriesType getType() {
		return this.type_;
	}

	/**
	 * Change the model column.
	 * <p>
	 * This specifies the model column from which the Y data is retrieved that
	 * is plotted by this series.
	 * <p>
	 * The data column should contain data that can be converted to a number
	 * (but should not necessarily be of a number type).
	 * <p>
	 * See also {@link eu.webtoolkit.jwt.utils.StringUtils#asNumber(Object)}.
	 */
	public void setModelColumn(int modelColumn) {
		if (!ChartUtils.equals(this.modelColumn_, modelColumn)) {
			this.modelColumn_ = modelColumn;
			update();
		}
		;
	}

	/**
	 * Returns the model column.
	 * <p>
	 * 
	 * @see WDataSeries#setModelColumn(int modelColumn)
	 */
	public int getModelColumn() {
		return this.modelColumn_;
	}

	/**
	 * Sets whether this series is stacked on top of the preceding series.
	 * <p>
	 * For category charts, data from different series may be rendered stacked
	 * on top of each other. The rendered value is the sum of the value of this
	 * series plus the rendered value of the preceding series. For line series,
	 * you probably will want to add filling under the curve. A stacked bar
	 * series is rendered by a bar on top of the preceding bar series.
	 * <p>
	 * The default value is false.
	 */
	public void setStacked(boolean stacked) {
		if (!ChartUtils.equals(this.stacked_, stacked)) {
			this.stacked_ = stacked;
			update();
		}
		;
	}

	/**
	 * Returns whether this series is stacked on top of the preceding series.
	 * <p>
	 * 
	 * @see WDataSeries#setStacked(boolean stacked)
	 */
	public boolean isStacked() {
		return this.stacked_;
	}

	/**
	 * Bind this series to a chart axis.
	 * <p>
	 * A data series may be bound to either the first or second Y axis. Note
	 * that the second Y axis is by default not displayed.
	 * <p>
	 * The default value is the first Y axis.
	 * <p>
	 * 
	 * @see WAxis#setVisible(boolean visible)
	 */
	public void bindToAxis(Axis axis) {
		if (!ChartUtils.equals(this.axis_, axis)) {
			this.axis_ = axis;
			update();
		}
		;
	}

	/**
	 * Returns the chart axis used for this series.
	 * <p>
	 * 
	 * @see WDataSeries#bindToAxis(Axis axis)
	 */
	public Axis getAxis() {
		return this.axis_;
	}

	/**
	 * Set which aspects of the look are overriden.
	 * <p>
	 * Set which aspects of the look, that are by default based on the chart
	 * palette, or overridden by custom settings.
	 * <p>
	 * The default value is 0 (nothing overridden).
	 */
	public void setCustomFlags(EnumSet<WDataSeries.CustomFlag> flags) {
		if (!ChartUtils.equals(this.customFlags_, flags)) {
			this.customFlags_ = flags;
			update();
		}
		;
	}

	/**
	 * Set which aspects of the look are overriden.
	 * <p>
	 * Calls {@link #setCustomFlags(EnumSet flags)
	 * setCustomFlags(EnumSet.of(flag, flags))}
	 */
	public final void setCustomFlags(WDataSeries.CustomFlag flag,
			WDataSeries.CustomFlag... flags) {
		setCustomFlags(EnumSet.of(flag, flags));
	}

	/**
	 * Returns which aspects of the look are overriden.
	 * <p>
	 * 
	 * @see WDataSeries#setCustomFlags(EnumSet flags)
	 */
	public EnumSet<WDataSeries.CustomFlag> getCurstomFlags() {
		return this.customFlags_;
	}

	/**
	 * Override the pen used for drawing lines for this series.
	 * <p>
	 * Overrides the pen that is used to draw this series. Calling this method
	 * automatically adds CustomPen to the custom flags.
	 * <p>
	 * The default value is a default WPen().
	 * <p>
	 * 
	 * @see WChartPalette#getStrokePen(int index)
	 * @see WChartPalette#getBorderPen(int index)
	 */
	public void setPen(WPen pen) {
		if (!ChartUtils.equals(this.pen_, pen)) {
			this.pen_ = pen;
			update();
		}
		;
		this.customFlags_.add(WDataSeries.CustomFlag.CustomPen);
	}

	/**
	 * Returns the pen used for drawing lines for this series.
	 * <p>
	 * 
	 * @see WDataSeries#setPen(WPen pen)
	 */
	public WPen getPen() {
		if (!EnumUtils
				.mask(this.customFlags_, WDataSeries.CustomFlag.CustomPen)
				.isEmpty()) {
			return this.pen_;
		} else {
			if (this.chart_ != null) {
				if (this.type_ == SeriesType.BarSeries) {
					return this.chart_.getPalette().getBorderPen(
							this.chart_.getSeriesIndexOf(this.modelColumn_));
				} else {
					return this.chart_.getPalette().getStrokePen(
							this.chart_.getSeriesIndexOf(this.modelColumn_));
				}
			} else {
				return new WPen();
			}
		}
	}

	/**
	 * Override the brush used for filling areas for this series.
	 * <p>
	 * Overrides the brush that is used to draw this series. For a bar plot,
	 * this is the brush used to fill the bars. For a line chart, this is the
	 * brush used to fill the area under (or above) the line. Calling this
	 * method automatically adds CustomBrush to the custom flags.
	 * <p>
	 * 
	 * @see WChartPalette#getBrush(int index)
	 */
	public void setBrush(WBrush brush) {
		if (!ChartUtils.equals(this.brush_, brush)) {
			this.brush_ = brush;
			update();
		}
		;
		this.customFlags_.add(WDataSeries.CustomFlag.CustomBrush);
	}

	/**
	 * Returns the brush used for filling areas for this series.
	 * <p>
	 * 
	 * @see WDataSeries#setBrush(WBrush brush)
	 */
	public WBrush getBrush() {
		if (!EnumUtils.mask(this.customFlags_,
				WDataSeries.CustomFlag.CustomBrush).isEmpty()) {
			return this.brush_;
		} else {
			if (this.chart_ != null) {
				return this.chart_.getPalette().getBrush(
						this.chart_.getSeriesIndexOf(this.modelColumn_));
			} else {
				return new WBrush();
			}
		}
	}

	/**
	 * Sets the fill range for line or curve series.
	 * <p>
	 * Line or curve series may be filled under or above the curve, using the
	 * {@link WDataSeries#getBrush() getBrush()}. This setting specifies the
	 * range that is filled.
	 */
	public void setFillRange(FillRangeType fillRange) {
		if (!ChartUtils.equals(this.fillRange_, fillRange)) {
			this.fillRange_ = fillRange;
			update();
		}
		;
	}

	/**
	 * Returns the fill range for line or curve series.
	 * <p>
	 * 
	 * @see WDataSeries#setFillRange(FillRangeType fillRange)
	 */
	public FillRangeType getFillRange() {
		return this.fillRange_;
	}

	/**
	 * Sets the data point marker.
	 * <p>
	 * Specifies a marker that is displayed at the (X,Y) coordinate for each
	 * series data point.
	 * <p>
	 * The default value is a CircleMarker for a PointSeries, or NoMarker
	 * otherwise.
	 * <p>
	 * 
	 * @see WDataSeries#setMarkerPen(WPen pen)
	 * @see WDataSeries#setMarkerBrush(WBrush brush)
	 */
	public void setMarker(MarkerType marker) {
		if (!ChartUtils.equals(this.marker_, marker)) {
			this.marker_ = marker;
			update();
		}
		;
	}

	/**
	 * Returns the data point marker.
	 * <p>
	 * 
	 * @see WDataSeries#setMarker(MarkerType marker)
	 */
	public MarkerType getMarker() {
		return this.marker_;
	}

	/**
	 * Sets the marker pen.
	 * <p>
	 * Overrides the pen used for stroking the marker. By default the marker pen
	 * is the same as {@link WDataSeries#getPen() getPen()}. Calling this method
	 * automatically adds CustomMarkerPen to the custom flags.
	 * <p>
	 * 
	 * @see WDataSeries#setPen(WPen pen)
	 * @see WDataSeries#setMarkerBrush(WBrush brush)
	 */
	public void setMarkerPen(WPen pen) {
		if (!ChartUtils.equals(this.markerPen_, pen)) {
			this.markerPen_ = pen;
			update();
		}
		;
		this.customFlags_.add(WDataSeries.CustomFlag.CustomMarkerPen);
	}

	/**
	 * Returns the marker pen.
	 * <p>
	 * 
	 * @see WDataSeries#setMarkerPen(WPen pen)
	 */
	public WPen getMarkerPen() {
		if (!EnumUtils.mask(this.customFlags_,
				WDataSeries.CustomFlag.CustomMarkerPen).isEmpty()) {
			return this.markerPen_;
		} else {
			return this.getPen();
		}
	}

	/**
	 * Sets the marker brush.
	 * <p>
	 * Overrides the brush used for filling the marker. By default the marker
	 * brush is the same as {@link WDataSeries#getBrush() getBrush()}. Calling
	 * this method automatically adds CustomMarkerBrush to the custom flags.
	 * <p>
	 * 
	 * @see WDataSeries#setBrush(WBrush brush)
	 * @see WDataSeries#setMarkerPen(WPen pen)
	 */
	public void setMarkerBrush(WBrush brush) {
		if (!ChartUtils.equals(this.markerBrush_, brush)) {
			this.markerBrush_ = brush;
			update();
		}
		;
		this.customFlags_.add(WDataSeries.CustomFlag.CustomMarkerBrush);
	}

	/**
	 * Returns the marker brush.
	 * <p>
	 * 
	 * @see WDataSeries#setMarkerBrush(WBrush brush)
	 */
	public WBrush getMarkerBrush() {
		if (!EnumUtils.mask(this.customFlags_,
				WDataSeries.CustomFlag.CustomMarkerBrush).isEmpty()) {
			return this.markerBrush_;
		} else {
			return this.getBrush();
		}
	}

	/**
	 * Enable the entry for this series in the legend.
	 * <p>
	 * When <i>enabled</i>, this series is added to the chart legend.
	 * <p>
	 * The default value is true.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendEnabled(boolean enabled)
	 */
	public void setLegendEnabled(boolean enabled) {
		if (!ChartUtils.equals(this.legend_, enabled)) {
			this.legend_ = enabled;
			update();
		}
		;
	}

	/**
	 * Returns whether this series has an entry in the legend.
	 * <p>
	 * 
	 * @see WDataSeries#setLegendEnabled(boolean enabled)
	 */
	public boolean isLegendEnabled() {
		return this.legend_;
	}

	/**
	 * Enable a label that is shown at the series data points.
	 * <p>
	 * You may enable labels for the XAxis, YAxis or both axes. The label that
	 * is displayed is the corresponding value on that axis. If both labels are
	 * enabled then they are combined in a single text using the format:
	 * &quot;&lt;x-value&gt;: &lt;y-value&gt;&quot;.
	 * <p>
	 * The default values are false for both axes (no labels).
	 * <p>
	 * 
	 * @see WDataSeries#isLabelsEnabled(Axis axis)
	 */
	public void setLabelsEnabled(Axis axis, boolean enabled) {
		if (axis == Axis.XAxis) {
			this.xLabel_ = enabled;
		} else {
			this.yLabel_ = enabled;
		}
		this.update();
	}

	/**
	 * Enable a label that is shown at the series data points.
	 * <p>
	 * Calls {@link #setLabelsEnabled(Axis axis, boolean enabled)
	 * setLabelsEnabled(axis, true)}
	 */
	public final void setLabelsEnabled(Axis axis) {
		setLabelsEnabled(axis, true);
	}

	/**
	 * Returns whether labels are enabled for the given axis.
	 * <p>
	 * 
	 * @see WDataSeries#setLabelsEnabled(Axis axis, boolean enabled)
	 */
	public boolean isLabelsEnabled(Axis axis) {
		return axis == Axis.XAxis ? this.xLabel_ : this.yLabel_;
	}

	/**
	 * Set the label color.
	 * <p>
	 * Specify the color used for the rendering labels at the data points.
	 * <p>
	 * 
	 * @see WDataSeries#setLabelsEnabled(Axis axis, boolean enabled)
	 */
	public void setLabelColor(WColor color) {
		if (!ChartUtils.equals(this.labelColor_, color)) {
			this.labelColor_ = color;
			update();
		}
		;
	}

	/**
	 * Returns the label color.
	 * <p>
	 * 
	 * @see WDataSeries#setLabelColor(WColor color)
	 */
	public WColor getLabelColor() {
		if (!EnumUtils.mask(this.customFlags_,
				WDataSeries.CustomFlag.CustomLabelColor).isEmpty()) {
			return this.labelColor_;
		} else {
			if (this.chart_ != null) {
				return this.chart_.getPalette().getFontColor(
						this.chart_.getSeriesIndexOf(this.modelColumn_));
			} else {
				return WColor.black;
			}
		}
	}

	private WCartesianChart chart_;
	int modelColumn_;
	private boolean stacked_;
	private SeriesType type_;
	private Axis axis_;
	private EnumSet<WDataSeries.CustomFlag> customFlags_;
	private WPen pen_;
	private WPen markerPen_;
	private WBrush brush_;
	private WBrush markerBrush_;
	private WColor labelColor_;
	private FillRangeType fillRange_;
	private MarkerType marker_;
	private boolean legend_;
	private boolean xLabel_;
	private boolean yLabel_;

	// private boolean (T m, T v) ;
	void setChart(WCartesianChart chart) {
		this.chart_ = chart;
	}

	private void update() {
		if (this.chart_ != null) {
			this.chart_.update();
		}
	}
}
