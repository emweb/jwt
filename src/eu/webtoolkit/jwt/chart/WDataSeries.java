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
 * A single data series in a cartesian chart.
 * <p>
 * 
 * This class configures all aspects for rendering a single data series in a
 * cartesian chart. A data series renders Y data from a single model column
 * against the X series configured for the chart.
 * <p>
 * The data column should contain data that can be converted to a number, but
 * should not necessarily be of a number type, see also
 * {@link eu.webtoolkit.jwt.StringUtils#asNumber(Object)}.
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
public class WDataSeries extends WObject {
	private static Logger logger = LoggerFactory.getLogger(WDataSeries.class);

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

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Constructs a new data series.
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
		super();
		this.chart_ = null;
		this.model_ = null;
		this.modelColumn_ = modelColumn;
		this.XSeriesColumn_ = -1;
		this.stacked_ = false;
		this.type_ = type;
		this.yAxis_ = axis == Axis.Y1Axis ? 0 : 1;
		this.customFlags_ = EnumSet.noneOf(WDataSeries.CustomFlag.class);
		this.pen_ = new WPen();
		this.markerPen_ = new WPen();
		this.brush_ = new WBrush();
		this.markerBrush_ = new WBrush();
		this.labelColor_ = new WColor();
		this.shadow_ = new WShadow();
		this.fillRange_ = FillRangeType.NoFill;
		this.marker_ = type == SeriesType.PointSeries ? MarkerType.CircleMarker
				: MarkerType.NoMarker;
		this.markerSize_ = 6;
		this.legend_ = true;
		this.xLabel_ = false;
		this.yLabel_ = false;
		this.barWidth_ = 0.8;
		this.hidden_ = false;
		this.customMarker_ = new WPainterPath();
		this.offset_ = 0.0;
		this.scale_ = 1.0;
		this.offsetDirty_ = true;
		this.scaleDirty_ = true;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
	}

	/**
	 * Constructs a new data series.
	 * <p>
	 * Calls {@link #WDataSeries(int modelColumn, SeriesType type, Axis axis)
	 * this(modelColumn, SeriesType.PointSeries, Axis.Y1Axis)}
	 */
	public WDataSeries(int modelColumn) {
		this(modelColumn, SeriesType.PointSeries, Axis.Y1Axis);
	}

	/**
	 * Constructs a new data series.
	 * <p>
	 * Calls {@link #WDataSeries(int modelColumn, SeriesType type, Axis axis)
	 * this(modelColumn, type, Axis.Y1Axis)}
	 */
	public WDataSeries(int modelColumn, SeriesType type) {
		this(modelColumn, type, Axis.Y1Axis);
	}

	/**
	 * Constructs a new data series.
	 * <p>
	 * Creates a new data series which plots the Y values from the model column
	 * <i>modelColumn</i>, with the indicated <i>seriesType</i>. The Y values
	 * are mapped to the indicated <i>yAxis</i>, which should correspond to one
	 * of the two Y axes.
	 * <p>
	 * 
	 * @see WCartesianChart#addSeries(WDataSeries series)
	 */
	public WDataSeries(int modelColumn, SeriesType type, int yAxis) {
		super();
		this.chart_ = null;
		this.model_ = null;
		this.modelColumn_ = modelColumn;
		this.XSeriesColumn_ = -1;
		this.stacked_ = false;
		this.type_ = type;
		this.yAxis_ = yAxis;
		this.customFlags_ = EnumSet.noneOf(WDataSeries.CustomFlag.class);
		this.pen_ = new WPen();
		this.markerPen_ = new WPen();
		this.brush_ = new WBrush();
		this.markerBrush_ = new WBrush();
		this.labelColor_ = new WColor();
		this.shadow_ = new WShadow();
		this.fillRange_ = FillRangeType.NoFill;
		this.marker_ = type == SeriesType.PointSeries ? MarkerType.CircleMarker
				: MarkerType.NoMarker;
		this.markerSize_ = 6;
		this.legend_ = true;
		this.xLabel_ = false;
		this.yLabel_ = false;
		this.barWidth_ = 0.8;
		this.hidden_ = false;
		this.customMarker_ = new WPainterPath();
		this.offset_ = 0.0;
		this.scale_ = 1.0;
		this.offsetDirty_ = true;
		this.scaleDirty_ = true;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
	}

	/**
	 * Copy constructor.
	 * <p>
	 * This does not copy over the associated chart.
	 * <p>
	 * 
	 * @deprecated
	 */
	public WDataSeries(final WDataSeries other) {
		super();
		this.chart_ = null;
		this.model_ = other.model_;
		this.modelColumn_ = other.modelColumn_;
		this.XSeriesColumn_ = other.XSeriesColumn_;
		this.stacked_ = other.stacked_;
		this.type_ = other.type_;
		this.yAxis_ = other.yAxis_;
		this.customFlags_ = other.customFlags_;
		this.pen_ = other.pen_;
		this.markerPen_ = other.markerPen_;
		this.brush_ = other.brush_;
		this.markerBrush_ = other.markerBrush_;
		this.labelColor_ = other.labelColor_;
		this.shadow_ = other.shadow_;
		this.fillRange_ = other.fillRange_;
		this.marker_ = other.marker_;
		this.markerSize_ = other.markerSize_;
		this.legend_ = other.legend_;
		this.xLabel_ = other.xLabel_;
		this.yLabel_ = other.yLabel_;
		this.barWidth_ = other.barWidth_;
		this.hidden_ = other.hidden_;
		this.customMarker_ = other.customMarker_;
		this.offset_ = other.offset_;
		this.scale_ = other.scale_;
		this.offsetDirty_ = true;
		this.scaleDirty_ = true;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		if (this.model_ != null) {
			this.modelConnections_.add(this.model_.changed().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WDataSeries.this.modelReset();
						}
					}));
		}
	}

	/**
	 * Sets the bar width.
	 * <p>
	 * The bar width specifies the bar width (in axis dimensions). For category
	 * plots, which may have several bars for different series next to each
	 * other, you will want to specify the same bar width for each series.
	 * <p>
	 * For scatter plots, you may want to set the bar width to a natural size.
	 * E.g. if you are plotting weekly measurements, you could set the width to
	 * correspond to a week (=7).
	 * <p>
	 * The default value is 0.8 (which leaves a 20% margin between bars for
	 * different categories in a category chart.
	 * <p>
	 * 
	 * @see WCartesianChart#setBarMargin(double margin)
	 */
	public void setBarWidth(final double width) {
		this.barWidth_ = width;
	}

	/**
	 * Returns the bar width.
	 * <p>
	 * 
	 * @see WDataSeries#setBarWidth(double width)
	 */
	public double getBarWidth() {
		return this.barWidth_;
	}

	/**
	 * Sets the series type.
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
	 * Sets the model column.
	 * <p>
	 * This specifies the model column from which the Y data is retrieved that
	 * is plotted by this series.
	 * <p>
	 * The data column should contain data that can be converted to a number
	 * (but should not necessarily be of a number type).
	 * <p>
	 * See also {@link eu.webtoolkit.jwt.StringUtils#asNumber(Object)}.
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
	 * Sets the X series column.
	 * <p>
	 * By default, the data series uses the X series column configured for the
	 * chart. For a scatter plot, each series can have its own matching X data,
	 * which is configured here. For other plots, this setting is ignored.
	 * <p>
	 * The default value is -1, which indicates that
	 * {@link WCartesianChart#XSeriesColumn() WCartesianChart#XSeriesColumn()}
	 * is to be used.
	 * <p>
	 * 
	 * @see WCartesianChart#setXSeriesColumn(int modelColumn)
	 */
	public void setXSeriesColumn(int modelColumn) {
		this.XSeriesColumn_ = modelColumn;
	}

	/**
	 * Returns the X series column.
	 * <p>
	 * 
	 * @see WDataSeries#setXSeriesColumn(int modelColumn)
	 */
	public int XSeriesColumn() {
		return this.XSeriesColumn_;
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
	 * Binds this series to a chart axis.
	 * <p>
	 * A data series can only be bound to a Y axis. Note that the second Y axis
	 * will not be displayed by default.
	 * <p>
	 * The default value is the first Y axis.
	 * <p>
	 * 
	 * @see WAxis#setVisible(boolean visible)
	 */
	public void bindToAxis(Axis axis) {
		if (!ChartUtils.equals(this.yAxis_, axis == Axis.Y1Axis ? 0 : 1)) {
			this.yAxis_ = axis == Axis.Y1Axis ? 0 : 1;
			update();
		}
		;
	}

	/**
	 * Binds this series to a chart axis.
	 * <p>
	 * A data series can only be bound to a Y axis. Note that the second Y axis
	 * will not be displayed by default.
	 * <p>
	 * The default value is the first Y axis.
	 * <p>
	 * 
	 * @see WAxis#setVisible(boolean visible)
	 */
	public void bindToYAxis(int yAxis) {
		if (!ChartUtils.equals(this.yAxis_, yAxis)) {
			this.yAxis_ = yAxis;
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
		return this.yAxis_ == 1 ? Axis.Y2Axis : Axis.Y1Axis;
	}

	/**
	 * Returns the Y axis used for this series.
	 * <p>
	 * 
	 * @see WDataSeries#bindToYAxis(int yAxis)
	 */
	public int getYAxis() {
		return this.yAxis_;
	}

	/**
	 * Sets which aspects of the look are overriden.
	 * <p>
	 * Set which aspects of the look, that are by default based on the chart
	 * palette, are overridden by custom settings.
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
	 * Sets which aspects of the look are overriden.
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
	public EnumSet<WDataSeries.CustomFlag> getCustomFlags() {
		return this.customFlags_;
	}

	/**
	 * Overrides the pen used for drawing lines for this series.
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
	public void setPen(final WPen pen) {
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
							this.chart_.getSeriesIndexOf(this));
				} else {
					return this.chart_.getPalette().getStrokePen(
							this.chart_.getSeriesIndexOf(this));
				}
			} else {
				WPen defaultPen = new WPen();
				defaultPen.setCapStyle(PenCapStyle.RoundCap);
				defaultPen.setJoinStyle(PenJoinStyle.RoundJoin);
				return defaultPen;
			}
		}
	}

	/**
	 * Overrides the brush used for filling areas for this series.
	 * <p>
	 * Overrides the brush that is used to draw this series which is otherwise
	 * provided by the chart palette. For a bar plot, this is the brush used to
	 * fill the bars. For a line chart, this is the brush used to fill the area
	 * under (or above) the line. Calling this method automatically adds
	 * CustomBrush to the custom flags.
	 * <p>
	 * 
	 * @see WChartPalette#getBrush(int index)
	 */
	public void setBrush(final WBrush brush) {
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
						this.chart_.getSeriesIndexOf(this));
			} else {
				return new WBrush();
			}
		}
	}

	/**
	 * Sets a shadow used for stroking lines for this series.
	 */
	public void setShadow(final WShadow shadow) {
		if (!ChartUtils.equals(this.shadow_, shadow)) {
			this.shadow_ = shadow;
			update();
		}
		;
	}

	/**
	 * Returns the shadow used for stroking lines for this series.
	 * <p>
	 * 
	 * @see WDataSeries#setShadow(WShadow shadow)
	 */
	public WShadow getShadow() {
		return this.shadow_;
	}

	/**
	 * Sets the fill range for line or curve series.
	 * <p>
	 * Line or curve series may be filled under or above the curve, using the
	 * {@link WDataSeries#getBrush() getBrush()}. This setting specifies the
	 * range that is filled. The default value for all but BarSeries is NoFill.
	 * <p>
	 * Bar series may use MinimumValueFill to configure the chart to render its
	 * bars from the data point to the bottom of the chart or MaximumValueFill
	 * to render the bars from the data point to the top of the chart. The
	 * default value for BarSeries is ZeroValueFill, which render bars from zero
	 * to the data value.
	 */
	public void setFillRange(FillRangeType fillRange) {
		if (!ChartUtils.equals(this.fillRange_, fillRange)) {
			this.fillRange_ = fillRange;
			update();
		}
		;
	}

	/**
	 * Returns the fill range (for line, curve and bar series).
	 * <p>
	 * 
	 * @see WDataSeries#setFillRange(FillRangeType fillRange)
	 */
	public FillRangeType getFillRange() {
		if (this.type_ == SeriesType.BarSeries
				&& this.fillRange_ == FillRangeType.NoFill) {
			return FillRangeType.ZeroValueFill;
		} else {
			return this.fillRange_;
		}
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
	 * @see WDataSeries#setCustomMarker(WPainterPath path)
	 */
	public void setMarker(MarkerType marker) {
		if (!ChartUtils.equals(this.marker_, marker)) {
			this.marker_ = marker;
			update();
		}
		;
	}

	/**
	 * Sets the custom marker.
	 * <p>
	 * This will also changes the marker type to CustomMarker.
	 * <p>
	 * 
	 * @see WDataSeries#setMarker(MarkerType marker)
	 */
	public void setCustomMarker(final WPainterPath path) {
		if (!ChartUtils.equals(this.marker_, MarkerType.CustomMarker)) {
			this.marker_ = MarkerType.CustomMarker;
			update();
		}
		;
		this.customMarker_.assign(path);
	}

	/**
	 * Returns the custom marker.
	 * <p>
	 * 
	 * @see WDataSeries#setCustomMarker(WPainterPath path)
	 */
	public WPainterPath getCustomMarker() {
		return this.customMarker_;
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
	 * Sets the marker size.
	 * <p>
	 * The default marker size is 6 pixels.
	 */
	public void setMarkerSize(double size) {
		if (!ChartUtils.equals(this.markerSize_, size)) {
			this.markerSize_ = size;
			update();
		}
		;
	}

	/**
	 * Returns the marker size.
	 * <p>
	 * 
	 * @see WDataSeries#setMarkerSize(double size)
	 */
	public double getMarkerSize() {
		return this.markerSize_;
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
	public void setMarkerPen(final WPen pen) {
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
	public void setMarkerBrush(final WBrush brush) {
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
	 * Enables the entry for this series in the legend.
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
		if (!this.isHidden()) {
			return this.legend_;
		} else {
			return false;
		}
	}

	/**
	 * Enables a label that is shown at the series data points.
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
	 * Enables a label that is shown at the series data points.
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
	 * Sets the label color.
	 * <p>
	 * Specify the color used for the rendering labels at the data points.
	 * <p>
	 * 
	 * @see WDataSeries#setLabelsEnabled(Axis axis, boolean enabled)
	 */
	public void setLabelColor(final WColor color) {
		if (!ChartUtils.equals(this.labelColor_, color)) {
			this.labelColor_ = color;
			update();
		}
		;
		this.customFlags_.add(WDataSeries.CustomFlag.CustomLabelColor);
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
						this.chart_.getSeriesIndexOf(this));
			} else {
				return WColor.black;
			}
		}
	}

	/**
	 * Hide/unhide this series.
	 * <p>
	 * A hidden series will not be show in the chart and legend.
	 */
	public void setHidden(boolean hidden) {
		this.hidden_ = hidden;
	}

	/**
	 * Return whether the series is hidden.
	 * <p>
	 * 
	 * @see WDataSeries#setHidden(boolean hidden)
	 */
	public boolean isHidden() {
		return this.hidden_;
	}

	/**
	 * Maps from device coordinates to model coordinates.
	 * <p>
	 * Maps a position in the chart back to model coordinates, for data in this
	 * data series.
	 * <p>
	 * This uses WChart::mapFromDevice() passing the
	 * {@link WDataSeries#getAxis() getAxis()} to which this series is bound.
	 * <p>
	 * This method uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish to already the
	 * mapping reflect model changes since the last rendering, you should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * WCartesianChart#initLayout()} first.
	 * <p>
	 * 
	 * @see WDataSeries#mapToDevice(Object xValue, Object yValue, int segment)
	 */
	public WPointF mapFromDevice(final WPointF deviceCoordinates) {
		if (this.chart_ != null) {
			return this.chart_.mapFromDevice(deviceCoordinates, this.yAxis_);
		} else {
			return new WPointF();
		}
	}

	/**
	 * Maps from model values to device coordinates.
	 * <p>
	 * Maps model values to device coordinates, for data in this data series.
	 * <p>
	 * This uses WChart::mapToDevice() passing the {@link WDataSeries#getAxis()
	 * getAxis()} to which this series is bound.
	 * <p>
	 * This method uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish to already the
	 * mapping reflect model changes since the last rendering, you should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * WCartesianChart#initLayout()} first.
	 * <p>
	 * 
	 * @see WDataSeries#mapFromDevice(WPointF deviceCoordinates)
	 */
	public WPointF mapToDevice(final Object xValue, final Object yValue,
			int segment) {
		if (this.chart_ != null) {
			return this.chart_
					.mapToDevice(xValue, yValue, this.yAxis_, segment);
		} else {
			return new WPointF();
		}
	}

	/**
	 * Maps from model values to device coordinates.
	 * <p>
	 * Returns {@link #mapToDevice(Object xValue, Object yValue, int segment)
	 * mapToDevice(xValue, yValue, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue) {
		return mapToDevice(xValue, yValue, 0);
	}

	/**
	 * Set an offset to draw the data series at.
	 * <p>
	 * The Y position of the data series will be drawn at an offset, expressed
	 * in model coordinates. The axis labels won&apos;t follow the same offset.
	 * <p>
	 * The offset can be manipulated client side using a mouse or touch drag if
	 * {@link WCartesianChart#isCurveManipulationEnabled()
	 * WCartesianChart#isCurveManipulationEnabled()} is enabled.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This is only supported for axes with linear scale.</i>
	 * </p>
	 * 
	 * @see WDataSeries#setScale(double scale)
	 * @see WCartesianChart#setCurveManipulationEnabled(boolean enabled)
	 */
	public void setOffset(double offset) {
		if (this.offset_ != offset) {
			this.offset_ = offset;
			this.update();
		}
		this.offsetDirty_ = true;
	}

	/**
	 * Get the offset for this data series.
	 * <p>
	 * 
	 * @see WDataSeries#setOffset(double offset)
	 */
	public double getOffset() {
		return this.offset_;
	}

	/**
	 * Set the scale to draw the data series at.
	 * <p>
	 * The Y position of the data series will be scaled around the zero
	 * position, and offset by {@link WDataSeries#getOffset() getOffset()}.
	 * <p>
	 * The scale can be manipulated client side using the scroll wheel or a
	 * pinch motion if {@link WCartesianChart#isCurveManipulationEnabled()
	 * WCartesianChart#isCurveManipulationEnabled()} is enabled.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This is only supported for axes with linear scale.</i>
	 * </p>
	 * 
	 * @see WDataSeries#setOffset(double offset)
	 * @see WCartesianChart#setCurveManipulationEnabled(boolean enabled)
	 */
	public void setScale(double scale) {
		if (this.scale_ != scale) {
			this.scale_ = scale;
			this.update();
		}
		this.scaleDirty_ = true;
	}

	/**
	 * Get the scale for this data series.
	 * <p>
	 * 
	 * @see WDataSeries#setScale(double scale)
	 */
	public double getScale() {
		return this.scale_;
	}

	/**
	 * Set a model for this data series.
	 * <p>
	 * If no model is set for this data series, the model of the chart will be
	 * used.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Individual models per data series are only supported for
	 * ScatterPlot type charts.</i>
	 * </p>
	 * 
	 * @see WAbstractChart#setModel(WAbstractItemModel model)
	 */
	public void setModel(WAbstractChartModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
		if (this.model_ != null) {
			this.modelConnections_.add(this.model_.changed().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WDataSeries.this.modelReset();
						}
					}));
		}
		if (this.chart_ != null) {
			this.chart_.update();
		}
	}

	/**
	 * Get the model for this data series.
	 * <p>
	 * This will return the model set for this data series, if it is set.
	 * <p>
	 * If no model is set for this data series, and the series is associated
	 * with a chart, the model of the chart is returned.
	 * <p>
	 * If no model is set for this data series, and the series is not associated
	 * with any data series, this will return null.
	 * <p>
	 * 
	 * @see WDataSeries#setModel(WAbstractChartModel model)
	 * @see WAbstractChart#setModel(WAbstractItemModel model)
	 */
	public WAbstractChartModel getModel() {
		if (this.model_ != null) {
			return this.model_;
		}
		if (this.chart_ != null) {
			return this.chart_.getModel();
		}
		return null;
	}

	public WCartesianChart getChart() {
		return this.chart_;
	}

	private WCartesianChart chart_;
	private WAbstractChartModel model_;
	int modelColumn_;
	private int XSeriesColumn_;
	private boolean stacked_;
	private SeriesType type_;
	private int yAxis_;
	private EnumSet<WDataSeries.CustomFlag> customFlags_;
	private WPen pen_;
	private WPen markerPen_;
	private WBrush brush_;
	private WBrush markerBrush_;
	private WColor labelColor_;
	private WShadow shadow_;
	private FillRangeType fillRange_;
	private MarkerType marker_;
	private double markerSize_;
	private boolean legend_;
	private boolean xLabel_;
	private boolean yLabel_;
	private double barWidth_;
	private boolean hidden_;
	private WPainterPath customMarker_;
	double offset_;
	double scale_;
	boolean offsetDirty_;
	boolean scaleDirty_;
	private List<AbstractSignal.Connection> modelConnections_;

	private void modelReset() {
		if (this.chart_ != null) {
			this.chart_.modelReset();
		}
	}

	// private boolean (final T m, final T v) ;
	void setChart(WCartesianChart chart) {
		this.chart_ = chart;
	}

	private void update() {
		if (this.chart_ != null) {
			this.chart_.update();
		}
	}
}
