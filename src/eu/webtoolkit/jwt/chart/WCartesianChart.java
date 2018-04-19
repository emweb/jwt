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
 * A cartesian chart.
 * <p>
 * 
 * A cartesian chart is a chart that uses X and Y axes. It can display one or
 * multiple data series, which each may be rendered using bars, lines, areas, or
 * points.
 * <p>
 * To use a cartesian chart, the minimum you need to do is set a model using
 * {@link WAbstractChart#setModel(WAbstractItemModel model)
 * WAbstractChart#setModel()}, set the model column that holds the X data using
 * {@link WCartesianChart#setXSeriesColumn(int modelColumn) setXSeriesColumn()},
 * and add one or more series using
 * {@link WCartesianChart#addSeries(WDataSeries series) addSeries()}. Each
 * series corresponds to one data column that holds Y data.
 * <p>
 * A cartesian chart is either a {@link ChartType#CategoryChart CategoryChart}
 * or a {@link ChartType#ScatterPlot ScatterPlot}.
 * <p>
 * In a <b>CategoryChart</b>, the X series represent different categories, which
 * are listed consecutively in model row order. The X axis scale is set to
 * {@link AxisScale#CategoryScale CategoryScale}.
 * <p>
 * <div align="center"> <img src="doc-files//ChartWCartesianChart-1.png"
 * alt="A category chart with bar series">
 * <p>
 * <strong>A category chart with bar series</strong>
 * </p>
 * </div> Each series may be rendered differently, and this is configured in the
 * data series (see {@link WDataSeries} for more information).
 * <p>
 * In a <b>ScatterPlot</b>, the X series data are interpreted as numbers on a
 * numerical scale. The scale for the X axis defaults to a
 * {@link AxisScale#LinearScale LinearScale}, but this may be changed to a
 * {@link AxisScale#DateScale DateScale} when the X series contains dates (of
 * type {@link eu.webtoolkit.jwt.WDate}) to create a time series chart, or to a
 * {@link AxisScale#LogScale LogScale}. A ScatterPlot supports the same types of
 * data series as a CategoryChart, but does not support stacking. In a scatter
 * plot, the X series do not need to be ordered in increasing values, and may be
 * set differently for each dataseries using
 * {@link WDataSeries#setXSeriesColumn(int modelColumn)
 * WDataSeries#setXSeriesColumn()}.
 * <p>
 * <div align="center"> <img src="doc-files//ChartWCartesianChart-2.png"
 * alt="A time series scatter plot with line series">
 * <p>
 * <strong>A time series scatter plot with line series</strong>
 * </p>
 * </div> Missing data in a model series Y values is interpreted as a
 * <i>break</i>. For curve-like series, this breaks the curve (or line).
 * <p>
 * The cartesian chart has support for dual Y axes. Each data series may be
 * bound to one of the two Y axes. By default, only the first Y axis is
 * displayed. To show the second Y axis you will need to call:
 * <p>
 * By default a chart has a horizontal X axis and a vertical Y axis, which
 * corresponds to a {@link Orientation#Vertical Vertical} orientation. The
 * orientation may be changed to {@link Orientation#Horizontal Horizontal} using
 * {@link WCartesianChart#setOrientation(Orientation orientation)
 * setOrientation()}.
 * <p>
 * The styling of the series data are dictated by a palette which may be set
 * using {@link WAbstractChart#setPalette(WChartPalette palette)
 * WAbstractChart#setPalette()}, but may be overridden by settings in each data
 * series.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * Styling through CSS is not applicable.
 * <p>
 * <h3>Client-side interaction</h3>
 * <p>
 * WCartesianChart has several features that allow interaction with the chart
 * without server roundtrips. These features include zoom, pan, crosshair and
 * follow curve functionality.
 * <p>
 * <p>
 * <i><b>Note: </b>Client side interaction is only available if the chart is
 * drawn on an HTML canvas. This is the default rendering method on modern
 * browsers, see
 * {@link WPaintedWidget#setPreferredMethod(WPaintedWidget.Method method)
 * WPaintedWidget#setPreferredMethod()}
 * <p>
 * Some features are currently not supported in interactive mode:
 * <ul>
 * <li>Axes set at ZeroValue position will not always be drawn correctly. They
 * may be clipped off outside of the chart area, and when zooming, the axis
 * ticks will change size.</li>
 * <li>{@link WAxis#setBreak(double minimum, double maximum) WAxis#setBreak()}
 * is incompatible with interactive mode</li>
 * </ul>
 * </i>
 * </p>
 * 
 * @see WDataSeries
 * @see WAxis
 * @see WPieChart
 */
public class WCartesianChart extends WAbstractChart {
	private static Logger logger = LoggerFactory
			.getLogger(WCartesianChart.class);

	/**
	 * Creates a new cartesian chart.
	 * <p>
	 * Creates a cartesian chart of type {@link ChartType#CategoryChart
	 * CategoryChart}.
	 */
	public WCartesianChart(WContainerWidget parent) {
		super(parent);
		this.interface_ = new WChart2DImplementation(this);
		this.orientation_ = Orientation.Vertical;
		this.XSeriesColumn_ = -1;
		this.type_ = ChartType.CategoryChart;
		this.series_ = new ArrayList<WDataSeries>();
		this.yAxes_ = new ArrayList<WAxis>();
		this.barMargin_ = 0;
		this.legend_ = new WLegend();
		this.axisPadding_ = 5;
		this.borderPen_ = new WPen(PenStyle.NoPen);
		this.textPen_ = new WPen();
		this.chartArea_ = null;
		this.hasDeferredToolTips_ = false;
		this.yAxisLocations_ = new ArrayList<WCartesianChart.AxisLocation>();
		this.jsDefined_ = false;
		this.zoomEnabled_ = false;
		this.panEnabled_ = false;
		this.rubberBandEnabled_ = true;
		this.crosshairEnabled_ = false;
		this.crosshairColor_ = WColor.black;
		this.crosshairYAxis_ = 0;
		this.seriesSelectionEnabled_ = false;
		this.selectedSeries_ = null;
		this.followCurve_ = null;
		this.curveManipulationEnabled_ = false;
		this.cObjCreated_ = false;
		this.xTransformChanged_ = new JSignal(this, "xTransformChanged");
		this.yTransformChanged_ = new JSignal(this, "yTransformChanged");
		this.seriesSelected_ = new Signal2<WDataSeries, WPointF>();
		this.jsSeriesSelected_ = new JSignal2<Double, Double>(this,
				"seriesSelected") {
		};
		this.curvePaths_ = new HashMap<WDataSeries, WJavaScriptHandle<WPainterPath>>();
		this.freePainterPaths_ = new ArrayList<WJavaScriptHandle<WPainterPath>>();
		this.curveTransforms_ = new HashMap<WDataSeries, WJavaScriptHandle<WTransform>>();
		this.freeTransforms_ = new ArrayList<WJavaScriptHandle<WTransform>>();
		this.xTransform_ = new WTransform();
		this.yTransform_ = new WTransform();
		this.xTransformHandle_ = null;
		this.yTransformHandle_ = null;
		this.pens_ = new HashMap<Axis, List<WCartesianChart.PenAssignment>>();
		this.freePens_ = new ArrayList<WJavaScriptHandle<WPen>>();
		this.curveLabels_ = new ArrayList<CurveLabel>();
		this.axisSliderWidgets_ = new ArrayList<WAxisSliderWidget>();
		this.wheelActions_ = new HashMap<EnumSet<KeyboardModifier>, InteractiveAction>();
		this.loadTooltip_ = new JSignal2<Double, Double>(this, "loadTooltip") {
		};
		this.barTooltips_ = new ArrayList<WCartesianChart.BarTooltip>();
		this.init();
	}

	/**
	 * Creates a new cartesian chart.
	 * <p>
	 * Calls {@link #WCartesianChart(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WCartesianChart() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a new cartesian chart.
	 * <p>
	 * Creates a cartesian chart of the indicated <code>type</code>.
	 */
	public WCartesianChart(ChartType type, WContainerWidget parent) {
		super(parent);
		this.interface_ = new WChart2DImplementation(this);
		this.orientation_ = Orientation.Vertical;
		this.XSeriesColumn_ = -1;
		this.type_ = type;
		this.series_ = new ArrayList<WDataSeries>();
		this.yAxes_ = new ArrayList<WAxis>();
		this.barMargin_ = 0;
		this.legend_ = new WLegend();
		this.axisPadding_ = 5;
		this.borderPen_ = new WPen(PenStyle.NoPen);
		this.textPen_ = new WPen();
		this.chartArea_ = null;
		this.hasDeferredToolTips_ = false;
		this.yAxisLocations_ = new ArrayList<WCartesianChart.AxisLocation>();
		this.jsDefined_ = false;
		this.zoomEnabled_ = false;
		this.panEnabled_ = false;
		this.rubberBandEnabled_ = true;
		this.crosshairEnabled_ = false;
		this.crosshairColor_ = WColor.black;
		this.crosshairYAxis_ = 0;
		this.seriesSelectionEnabled_ = false;
		this.selectedSeries_ = null;
		this.followCurve_ = null;
		this.curveManipulationEnabled_ = false;
		this.cObjCreated_ = false;
		this.xTransformChanged_ = new JSignal(this, "xTransformChanged");
		this.yTransformChanged_ = new JSignal(this, "yTransformChanged");
		this.seriesSelected_ = new Signal2<WDataSeries, WPointF>();
		this.jsSeriesSelected_ = new JSignal2<Double, Double>(this,
				"seriesSelected") {
		};
		this.curvePaths_ = new HashMap<WDataSeries, WJavaScriptHandle<WPainterPath>>();
		this.freePainterPaths_ = new ArrayList<WJavaScriptHandle<WPainterPath>>();
		this.curveTransforms_ = new HashMap<WDataSeries, WJavaScriptHandle<WTransform>>();
		this.freeTransforms_ = new ArrayList<WJavaScriptHandle<WTransform>>();
		this.xTransform_ = new WTransform();
		this.yTransform_ = new WTransform();
		this.xTransformHandle_ = null;
		this.yTransformHandle_ = null;
		this.pens_ = new HashMap<Axis, List<WCartesianChart.PenAssignment>>();
		this.freePens_ = new ArrayList<WJavaScriptHandle<WPen>>();
		this.curveLabels_ = new ArrayList<CurveLabel>();
		this.axisSliderWidgets_ = new ArrayList<WAxisSliderWidget>();
		this.wheelActions_ = new HashMap<EnumSet<KeyboardModifier>, InteractiveAction>();
		this.loadTooltip_ = new JSignal2<Double, Double>(this, "loadTooltip") {
		};
		this.barTooltips_ = new ArrayList<WCartesianChart.BarTooltip>();
		this.init();
	}

	/**
	 * Creates a new cartesian chart.
	 * <p>
	 * Calls {@link #WCartesianChart(ChartType type, WContainerWidget parent)
	 * this(type, (WContainerWidget)null)}
	 */
	public WCartesianChart(ChartType type) {
		this(type, (WContainerWidget) null);
	}

	public void remove() {
		while (!this.yAxes_.isEmpty()) {
			;
			this.yAxes_.remove(this.yAxes_.size() - 1);
		}
		;
		;
		List<WAxisSliderWidget> copy = new ArrayList<WAxisSliderWidget>(
				this.axisSliderWidgets_);
		this.axisSliderWidgets_.clear();
		for (int i = 0; i < copy.size(); ++i) {
			copy.get(i).setSeries((WDataSeries) null);
		}
		for (int i = 0; i < this.series_.size(); ++i) {
			;
		}
		super.remove();
	}

	/**
	 * Sets the chart type.
	 * <p>
	 * The chart type determines how (x,y) data are interpreted. In a
	 * {@link ChartType#CategoryChart CategoryChart}, the X values are
	 * categories, and these are plotted consecutively, evenly spaced, and in
	 * row order. In a {@link ChartType#ScatterPlot ScatterPlot}, the X values
	 * are interpreted numerically (as for Y values).
	 * <p>
	 * The default chart type is a {@link ChartType#CategoryChart CategoryChart}.
	 * <p>
	 * 
	 * @see WCartesianChart#getType()
	 * @see WAxis#setScale(AxisScale scale)
	 * @see WCartesianChart#getAxis(Axis axis)
	 */
	public void setType(ChartType type) {
		if (this.type_ != type) {
			this.type_ = type;
			this.xAxis_.init(this.interface_, Axis.XAxis);
			this.update();
		}
	}

	/**
	 * Returns the chart type.
	 * <p>
	 * 
	 * @see WCartesianChart#setType(ChartType type)
	 */
	public ChartType getType() {
		return this.type_;
	}

	/**
	 * Sets the chart orientation.
	 * <p>
	 * Sets the chart orientation, which corresponds to the orientation of the Y
	 * axis: a {@link Orientation#Vertical} orientation corresponds to the
	 * conventional way of a horizontal X axis and vertical Y axis. A
	 * {@link Orientation#Horizontal} orientation is the other way around.
	 * <p>
	 * The default orientation is {@link Orientation#Vertical}.
	 * <p>
	 * 
	 * @see WCartesianChart#getOrientation()
	 */
	public void setOrientation(Orientation orientation) {
		if (this.orientation_ != orientation) {
			this.orientation_ = orientation;
			this.update();
		}
	}

	/**
	 * Returns the chart orientation.
	 * <p>
	 * 
	 * @see WCartesianChart#setOrientation(Orientation orientation)
	 */
	public Orientation getOrientation() {
		return this.orientation_;
	}

	/**
	 * Sets the the model column for the X series.
	 * <p>
	 * Use this method to specify the default data for the X series. For a
	 * {@link ChartType#ScatterPlot ScatterPlot} this is mandatory if an X
	 * series is not specified for every WDataSeries. For a
	 * {@link ChartType#CategoryChart CategoryChart}, if not specified, an
	 * increasing series of integer numbers will be used (1, 2, ...).
	 * <p>
	 * Scatterplot dataseries may each individually be given its own X series
	 * data using {@link WDataSeries#setXSeriesColumn(int modelColumn)
	 * WDataSeries#setXSeriesColumn()}
	 * <p>
	 * The default value is -1 (not specified).
	 * <p>
	 * The series column is reset to -1 when the model is set (or changed). Thus
	 * you need to set a model before configuring the series.
	 * <p>
	 * 
	 * @see WCartesianChart#XSeriesColumn()
	 */
	public void setXSeriesColumn(int modelColumn) {
		if (this.XSeriesColumn_ != modelColumn) {
			this.XSeriesColumn_ = modelColumn;
			this.update();
		}
	}

	/**
	 * set the pen used to render the labels
	 * <p>
	 * This method overwrites the pen for all axes
	 * <p>
	 * 
	 * @see WAxis#setTextPen(WPen pen)
	 */
	public void setTextPen(final WPen pen) {
		if (pen.equals(this.textPen_)) {
			return;
		}
		this.textPen_ = pen;
		this.xAxis_.setTextPen(pen);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).setTextPen(pen);
		}
	}

	/**
	 * Returns the model column for the X series.
	 * <p>
	 * 
	 * @see WCartesianChart#setXSeriesColumn(int modelColumn)
	 */
	public int XSeriesColumn() {
		return this.XSeriesColumn_;
	}

	/**
	 * Adds a data series.
	 * <p>
	 * A single chart may display one or more data series. Each data series
	 * displays data from a single model column in the chart. Series are plotted
	 * in the order that they have been added to the chart.
	 * <p>
	 * The series column is reset to -1 when the model is set (or changed). Thus
	 * you need to set a model before configuring the series.
	 * <p>
	 * 
	 * @see WCartesianChart#removeSeries(int modelColumn)
	 * @see WCartesianChart#setSeries(List series)
	 */
	public void addSeries(WDataSeries series) {
		this.series_.add(series);
		this.series_.get(this.series_.size() - 1).setChart(this);
		if (series.getType() == SeriesType.LineSeries
				|| series.getType() == SeriesType.CurveSeries) {
			this.assignJSPathsForSeries(series);
			this.assignJSTransformsForSeries(series);
		}
		this.update();
	}

	/**
	 * Removes a data series.
	 * <p>
	 * This removes the first data series which plots the given
	 * <code>modelColumn</code>.
	 * <p>
	 * 
	 * @see WCartesianChart#addSeries(WDataSeries series)
	 * @see WCartesianChart#setSeries(List series)
	 * @deprecated Use {@link WCartesianChart#removeSeries(WDataSeries series)
	 *             removeSeries()} instead
	 */
	public void removeSeries(int modelColumn) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() == modelColumn) {
				this.removeSeries(this.series_.get(i));
				return;
			}
		}
	}

	/**
	 * Removes a data series.
	 * <p>
	 * This will disassociate the given series from any WAxisSliderWidgets.
	 * <p>
	 * 
	 * @see WCartesianChart#addSeries(WDataSeries series)
	 * @see WCartesianChart#setSeries(List series)
	 */
	public void removeSeries(WDataSeries series) {
		int index = this.getSeriesIndexOf(series);
		if (index != -1) {
			for (int i = 0; i < this.axisSliderWidgets_.size(); ++i) {
				if (this.axisSliderWidgets_.get(i).getSeries() == series) {
					this.axisSliderWidgets_.get(i)
							.setSeries((WDataSeries) null);
				}
			}
			if (series.getType() == SeriesType.LineSeries
					|| series.getType() == SeriesType.CurveSeries) {
				this.freeJSPathsForSeries(series);
				this.freeJSTransformsForSeries(series);
			}
			;
			this.series_.remove(0 + index);
			this.update();
		}
	}

	/**
	 * Sets all data series.
	 * <p>
	 * Replaces the current list of series with the new list.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>All series currently associated with the chart will be
	 * deleted. Hence, these series should be distinct from the currently
	 * associated series of this chart!</i>
	 * </p>
	 * 
	 * @see WCartesianChart#getSeries(int modelColumn)
	 * @see WCartesianChart#addSeries(WDataSeries series)
	 * @see WCartesianChart#removeSeries(WDataSeries series)
	 */
	public void setSeries(final List<WDataSeries> series) {
		for (int i = 0; i < this.series_.size(); ++i) {
			;
		}
		Utils.copyList(series, this.series_);
		this.freeJSHandlesForAllSeries();
		this.assignJSHandlesForAllSeries();
		for (int i = 0; i < this.series_.size(); ++i) {
			this.series_.get(i).setChart(this);
		}
		this.update();
	}

	/**
	 * Returns a data series corresponding to a data column.
	 * <p>
	 * Returns a reference to the first data series that plots data from
	 * <code>modelColumn</code>.
	 */
	public WDataSeries getSeries(int modelColumn) {
		int index = this.getSeriesIndexOf(modelColumn);
		if (index != -1) {
			return this.series_.get(index);
		}
		throw new WException("Column " + String.valueOf(modelColumn)
				+ " not in plot");
	}

	/**
	 * Returns a list with the current data series.
	 * <p>
	 * Returns the complete list of current data series.
	 * <p>
	 */
	public List<WDataSeries> getSeries() {
		return this.series_;
	}

	/**
	 * Returns a chart axis.
	 * <p>
	 * Returns a reference to the specified <code>axis</code>.
	 */
	public WAxis getAxis(Axis axis) {
		if (axis == Axis.XAxis) {
			return this.xAxis_;
		} else {
			return this.yAxis(axis == Axis.Y1Axis ? 0 : 1);
		}
	}

	/**
	 * Sets an axis.
	 * <p>
	 * 
	 * @see WCartesianChart#getAxis(Axis axis)
	 */
	public void setAxis(WAxis waxis, Axis axis) {
		if (this.getAxis(axis) == waxis) {
			return;
		}
		if (axis == Axis.XAxis) {
			;
			this.xAxis_ = waxis;
			this.xAxis_.init(this.interface_, axis);
		} else {
			int yIndex = axis == Axis.Y1Axis ? 0 : 1;
			;
			this.yAxes_.set(yIndex, waxis);
			this.yAxes_.get(yIndex).init(this.interface_, axis);
		}
	}

	/**
	 * Returns a vector of all Y axes associated with this chart.
	 * <p>
	 * This defaults to a vector of two axes: the Y1 and Y2 axes. Y1 will be at
	 * index 0, and Y2 will be at index 1.
	 */
	public List<WAxis> getYAxes() {
		return this.yAxes_;
	}

	/**
	 * Returns the number of Y axes associated with this chart.
	 */
	public int getYAxisCount() {
		return (int) this.yAxes_.size();
	}

	/**
	 * Retrieves the Y axis at index i.
	 * <p>
	 * The following expressions are always true:
	 * <p>
	 * 
	 * <pre>
	 *   {@code
	 *    axis(Y1) == yAxis(0)
	 *    axis(Y2) == yAxis(1)
	 *   }
	 * </pre>
	 */
	public WAxis yAxis(int i) {
		return this.yAxes_.get(i);
	}

	/**
	 * Adds a Y axis to this chart.
	 * <p>
	 * The first extra axis will have index 2, the next index 3,...
	 * <p>
	 * Returns the index of the added axis.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This transfers ownership of the given {@link WAxis} to
	 * this chart. </i>
	 * </p>
	 */
	public int addYAxis(WAxis waxis) {
		int idx = (int) this.yAxes_.size();
		this.yAxes_.add(waxis);
		this.yAxisLocations_.add(new WCartesianChart.AxisLocation());
		this.yAxes_.get(idx).initYAxis(this.interface_, idx);
		this.yAxes_.get(idx).setPadding(this.getAxisPadding());
		this.yAxes_.get(idx).setSoftLabelClipping(true);
		return idx;
	}

	/**
	 * Sets the margin between bars of different series.
	 * <p>
	 * Use this method to change the margin that is set between bars of
	 * different series. The margin is specified as a fraction of the width. For
	 * example, a value of 0.1 adds a 10% margin between bars of each series.
	 * Negative values are also allowed. For example, use a margin of -1 to plot
	 * the bars of different series on top of each other.
	 * <p>
	 * The default value is 0.
	 */
	public void setBarMargin(double margin) {
		if (this.barMargin_ != margin) {
			this.barMargin_ = margin;
			this.update();
		}
	}

	/**
	 * Returns the margin between bars of different series.
	 * <p>
	 * 
	 * @see WCartesianChart#setBarMargin(double margin)
	 */
	public double getBarMargin() {
		return this.barMargin_;
	}

	/**
	 * Enables the legend.
	 * <p>
	 * The location of the legend can be configured using
	 * {@link WCartesianChart#setLegendLocation(LegendLocation location, Side side, AlignmentFlag alignment)
	 * setLegendLocation()}. Only series for which the legend is enabled are
	 * included in this legend.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @see WDataSeries#isLegendEnabled()
	 * @see WCartesianChart#setLegendLocation(LegendLocation location, Side
	 *      side, AlignmentFlag alignment)
	 */
	public void setLegendEnabled(boolean enabled) {
		this.legend_.setLegendEnabled(enabled);
		this.update();
	}

	/**
	 * Returns whether the legend is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendEnabled(boolean enabled)
	 */
	public boolean isLegendEnabled() {
		return this.legend_.isLegendEnabled();
	}

	/**
	 * Configures the legend location.
	 * <p>
	 * The legend can be renderd either inside or outside of the chart area.
	 * When <code>location</code> is {@link LegendLocation#LegendInside
	 * Chart::LegendInside}, the legend will be rendered inside the chart. When
	 * <code>location</code> is {@link LegendLocation#LegendOutside
	 * Chart::Legendoutside}, the legend is rendered outside the chart, in the
	 * chart padding area.
	 * <p>
	 * The provided <code>side</code> can either be {@link Side#Left},
	 * {@link Side#Right}, {@link Side#Top}, {@link Side#Bottom} and configures
	 * the side of the chart at which the legend is displayed.
	 * <p>
	 * The <code>alignment</code> specifies how the legend is aligned. This can
	 * be a horizontal alignment flag ({@link AlignmentFlag#AlignLeft},
	 * {@link AlignmentFlag#AlignCenter}, or {@link AlignmentFlag#AlignRight}),
	 * when the <code>side</code> is Bottom or Top, or a vertical alignment flag
	 * ({@link AlignmentFlag#AlignTop}, {@link AlignmentFlag#AlignMiddle}, or
	 * {@link AlignmentFlag#AlignBottom}) when the <code>side</code> is Left or
	 * Right.
	 * <p>
	 * The default location is {@link LegendLocation#LegendOutside
	 * Chart::LegendOutside}, {@link Side#Right} and
	 * {@link AlignmentFlag#AlignMiddle}.
	 * <p>
	 * To have more control over the legend, you could reimplement the
	 * {@link WCartesianChart#renderLegendItem(WPainter painter, WPointF pos, WDataSeries series)
	 * renderLegendItem()} method to customize how one item in the legend is
	 * rendered, or, alternatively you can disable the legend generated by the
	 * chart itself, and reimplement the
	 * {@link WCartesianChart#paint(WPainter painter, WRectF rectangle) paint()}
	 * method in which you use the
	 * {@link WCartesianChart#renderLegendItem(WPainter painter, WPointF pos, WDataSeries series)
	 * renderLegendItem()} method repeatedly to render a customized legend.
	 * <p>
	 * 
	 * @see WDataSeries#setLegendEnabled(boolean enabled)
	 */
	public void setLegendLocation(LegendLocation location, Side side,
			AlignmentFlag alignment) {
		this.legend_.setLegendLocation(location, side, alignment);
		this.update();
	}

	/**
	 * Configures the legend decoration.
	 * <p>
	 * This configures the font, border and background for the legend.
	 * <p>
	 * The default font is a 10pt sans serif font (the same as the default axis
	 * label font), the default <code>border</code> is {@link PenStyle#NoPen
	 * NoPen} and the default <code>background</code> is
	 * {@link BrushStyle#NoBrush NoBrush}.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendEnabled(boolean enabled)
	 */
	public void setLegendStyle(final WFont font, final WPen border,
			final WBrush background) {
		this.legend_.setLegendStyle(font, border, background);
		this.update();
	}

	/**
	 * Returns the legend location.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendLocation(LegendLocation location, Side
	 *      side, AlignmentFlag alignment)
	 */
	public LegendLocation getLegendLocation() {
		return this.legend_.getLegendLocation();
	}

	/**
	 * Returns the legend side.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendLocation(LegendLocation location, Side
	 *      side, AlignmentFlag alignment)
	 */
	public Side getLegendSide() {
		return this.legend_.getLegendSide();
	}

	/**
	 * Returns the legend alignment.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendLocation(LegendLocation location, Side
	 *      side, AlignmentFlag alignment)
	 */
	public AlignmentFlag getLegendAlignment() {
		return this.legend_.getLegendAlignment();
	}

	/**
	 * Returns the legend columns.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendColumns(int columns, WLength columnWidth)
	 */
	public int getLegendColumns() {
		return this.legend_.getLegendColumns();
	}

	/**
	 * Returns the legend column width.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendColumns(int columns, WLength columnWidth)
	 */
	public WLength getLegendColumnWidth() {
		return this.legend_.getLegendColumnWidth();
	}

	/**
	 * Returns the legend font.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendStyle(WFont font, WPen border, WBrush
	 *      background)
	 */
	public WFont getLegendFont() {
		return this.legend_.getLegendFont();
	}

	/**
	 * Returns the legend border pen.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendStyle(WFont font, WPen border, WBrush
	 *      background)
	 */
	public WPen getLegendBorder() {
		return this.legend_.getLegendBorder();
	}

	/**
	 * Returns the legend background brush.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendStyle(WFont font, WPen border, WBrush
	 *      background)
	 */
	public WBrush getLegendBackground() {
		return this.legend_.getLegendBackground();
	}

	/**
	 * Configures multiple legend columns.
	 * <p>
	 * Multiple columns are typically useful when placing the legend at the top
	 * or at the bottom of the chart.
	 * <p>
	 * The default value is a single column, 100 pixels wide.
	 * <p>
	 * When automatic chart layout is enabled, then the legend column width is
	 * computed automatically, and this setting is ignored.
	 * <p>
	 * 
	 * @see WAbstractChart#setAutoLayoutEnabled(boolean enabled)
	 */
	public void setLegendColumns(int columns, final WLength columnWidth) {
		this.legend_.setLegendColumns(columns);
		this.legend_.setLegendColumnWidth(columnWidth);
		this.update();
	}

	public void paint(final WPainter painter, final WRectF rectangle) {
		while (!this.getAreas().isEmpty()) {
			if ((this).getAreas().get(0) != null)
				(this).getAreas().get(0).remove();
		}
		if (!painter.isActive()) {
			throw new WException(
					"WCartesianChart::paint(): painter is not active.");
		}
		WRectF rect = rectangle;
		if ((rect == null) || rect.isEmpty()) {
			rect = painter.getWindow();
		}
		this.render(painter, rect);
	}

	/**
	 * Draws the marker for a given data series.
	 * <p>
	 * Draws the marker for the indicated <code>series</code> in the
	 * <code>result</code>. This method is called while painting the chart, and
	 * you may want to reimplement this method if you wish to provide a custom
	 * marker for a particular data series.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendEnabled(boolean enabled)
	 */
	public void drawMarker(final WDataSeries series, final WPainterPath result) {
		final double size = 6.0;
		final double hsize = size / 2;
		switch (series.getMarker()) {
		case CircleMarker:
			result.addEllipse(-hsize, -hsize, size, size);
			break;
		case SquareMarker:
			result.addRect(new WRectF(-hsize, -hsize, size, size));
			break;
		case CrossMarker:
			result.moveTo(-1.3 * hsize, 0);
			result.lineTo(1.3 * hsize, 0);
			result.moveTo(0, -1.3 * hsize);
			result.lineTo(0, 1.3 * hsize);
			break;
		case XCrossMarker:
			result.moveTo(-hsize, -hsize);
			result.lineTo(hsize, hsize);
			result.moveTo(-hsize, hsize);
			result.lineTo(hsize, -hsize);
			break;
		case TriangleMarker:
			result.moveTo(0, 0.6 * hsize);
			result.lineTo(-hsize, 0.6 * hsize);
			result.lineTo(0, -hsize);
			result.lineTo(hsize, 0.6 * hsize);
			result.closeSubPath();
			break;
		case StarMarker: {
			double angle = 3.14159265358979323846 / 2.0;
			for (int i = 0; i < 5; ++i) {
				double x = Math.cos(angle) * hsize;
				double y = -Math.sin(angle) * hsize;
				result.moveTo(0, 0);
				result.lineTo(x, y);
				angle += 3.14159265358979323846 * 2.0 / 5.0;
			}
		}
			break;
		case InvertedTriangleMarker:
			result.moveTo(0, -0.6 * hsize);
			result.lineTo(-hsize, -0.6 * hsize);
			result.lineTo(0, hsize);
			result.lineTo(hsize, -0.6 * hsize);
			result.closeSubPath();
			break;
		case DiamondMarker: {
			double s = Math.sqrt(2.0) * hsize;
			result.moveTo(0, s);
			result.lineTo(s, 0);
			result.lineTo(0, -s);
			result.lineTo(-s, 0);
			result.closeSubPath();
		}
			break;
		case AsteriskMarker: {
			double angle = 3.14159265358979323846 / 2.0;
			for (int i = 0; i < 6; ++i) {
				double x = Math.cos(angle) * hsize;
				double y = -Math.sin(angle) * hsize;
				result.moveTo(0, 0);
				result.lineTo(x, y);
				angle += 3.14159265358979323846 / 3.0;
			}
		}
			break;
		case CustomMarker:
			result.assign(series.getCustomMarker());
			break;
		default:
			;
		}
	}

	/**
	 * Renders the legend icon for a given data series.
	 * <p>
	 * Renders the legend icon for the indicated <code>series</code> in the
	 * <code>painter</code> at position <code>pos</code>.
	 * <p>
	 * This method is called while rendering a legend item, and you may want to
	 * reimplement this method if you wish to provide a custom legend icon for a
	 * particular data series.
	 * <p>
	 * 
	 * @see WCartesianChart#renderLegendItem(WPainter painter, WPointF pos,
	 *      WDataSeries series)
	 */
	public void renderLegendIcon(final WPainter painter, final WPointF pos,
			final WDataSeries series) {
		WShadow shadow = painter.getShadow();
		switch (series.getType()) {
		case BarSeries: {
			WPainterPath path = new WPainterPath();
			path.moveTo(-6, 8);
			path.lineTo(-6, -8);
			path.lineTo(6, -8);
			path.lineTo(6, 8);
			painter.translate(pos.getX() + 7.5, pos.getY());
			painter.setShadow(series.getShadow());
			painter.fillPath(path, series.getBrush());
			painter.setShadow(shadow);
			painter.strokePath(path, series.getPen());
			painter.translate(-(pos.getX() + 7.5), -pos.getY());
			break;
		}
		case LineSeries:
		case CurveSeries: {
			painter.setPen(series.getPen().clone());
			double offset = series.getPen().getWidth().equals(new WLength(0)) ? 0.5
					: 0;
			painter.setShadow(series.getShadow());
			painter.drawLine(pos.getX(), pos.getY() + offset, pos.getX() + 16,
					pos.getY() + offset);
			painter.setShadow(shadow);
		}
		case PointSeries: {
			WPainterPath path = new WPainterPath();
			this.drawMarker(series, path);
			if (!path.isEmpty()) {
				painter.translate(pos.getX() + 8, pos.getY());
				painter.setShadow(series.getShadow());
				painter.fillPath(path, series.getMarkerBrush());
				painter.setShadow(shadow);
				painter.strokePath(path, series.getMarkerPen());
				painter.translate(-(pos.getX() + 8), -pos.getY());
			}
			break;
		}
		}
	}

	/**
	 * Renders the legend item for a given data series.
	 * <p>
	 * Renders the legend item for the indicated <code>series</code> in the
	 * <code>painter</code> at position <code>pos</code>. The default
	 * implementation draws the marker, and the series description to the right.
	 * The series description is taken from the model&apos;s header data for
	 * that series&apos; data column.
	 * <p>
	 * This method is called while painting the chart, and you may want to
	 * reimplement this method if you wish to provide a custom marker for a
	 * particular data series.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendEnabled(boolean enabled)
	 */
	public void renderLegendItem(final WPainter painter, final WPointF pos,
			final WDataSeries series) {
		WPen fontPen = painter.getPen();
		this.renderLegendIcon(painter, pos, series);
		painter.setPen(fontPen.clone());
		int width = (int) this.getLegendColumnWidth().toPixels();
		if (width < 100) {
			width = 100;
		}
		painter.drawText(pos.getX() + 23, pos.getY() - 9, width, 20,
				EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle),
				series.getModel().getHeaderData(series.getModelColumn()));
	}

	/**
	 * Maps from device coordinates to model coordinates.
	 * <p>
	 * Maps a position in the chart back to model coordinates.
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish that the
	 * mapping already reflects model changes since the last rendering, you
	 * should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * If the chart is interactive, mapFromDevice will correctly take the
	 * current zoom range into account.
	 * <p>
	 * 
	 * @see WCartesianChart#mapToDevice(Object xValue, Object yValue, Axis axis,
	 *      int xSegment, int ySegment)
	 */
	public WPointF mapFromDevice(final WPointF point, Axis ordinateAxis) {
		return this.mapFromDevice(point, ordinateAxis == Axis.Y1Axis ? 0 : 1);
	}

	/**
	 * Maps from device coordinates to model coordinates.
	 * <p>
	 * Returns {@link #mapFromDevice(WPointF point, Axis ordinateAxis)
	 * mapFromDevice(point, Axis.OrdinateAxis)}
	 */
	public final WPointF mapFromDevice(final WPointF point) {
		return mapFromDevice(point, Axis.OrdinateAxis);
	}

	/**
	 * Maps from device coordinates to model coordinates.
	 * <p>
	 * Maps a position in the chart back to model coordinates.
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish that the
	 * mapping already reflects model changes since the last rendering, you
	 * should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * If the chart is interactive, mapFromDevice will correctly take the
	 * current zoom range into account.
	 * <p>
	 * 
	 * @see WCartesianChart#mapToDevice(Object xValue, Object yValue, Axis axis,
	 *      int xSegment, int ySegment)
	 */
	public WPointF mapFromDevice(final WPointF point, int ordinateAxis) {
		if (this.isInteractive()) {
			return this.mapFromDeviceWithoutTransform(
					this.zoomRangeTransform(this.xTransformHandle_.getValue(),
							this.yTransformHandle_.getValue()).getInverted()
							.map(point), ordinateAxis);
		} else {
			return this.mapFromDeviceWithoutTransform(point, ordinateAxis);
		}
	}

	/**
	 * Maps from device coordinates to model coordinates, ignoring the current
	 * zoom range.
	 * <p>
	 * Maps a position in the chart back to model coordinates, as if the chart
	 * was not zoomed in (nor panned).
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish that the
	 * mapping already reflects model changes since the last rendering, you
	 * should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * This function will not take the current zoom range into account. The
	 * mapping will be performed as if
	 * {@link WCartesianChart#getZoomRangeTransform() getZoomRangeTransform()}
	 * is the identity transform.
	 * <p>
	 * 
	 * @see WCartesianChart#mapToDeviceWithoutTransform(Object xValue, Object
	 *      yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 */
	public WPointF mapFromDeviceWithoutTransform(final WPointF point,
			Axis ordinateAxis) {
		return this.mapFromDeviceWithoutTransform(point,
				ordinateAxis == Axis.Y1Axis ? 0 : 1);
	}

	/**
	 * Maps from device coordinates to model coordinates, ignoring the current
	 * zoom range.
	 * <p>
	 * Returns
	 * {@link #mapFromDeviceWithoutTransform(WPointF point, Axis ordinateAxis)
	 * mapFromDeviceWithoutTransform(point, Axis.OrdinateAxis)}
	 */
	public final WPointF mapFromDeviceWithoutTransform(final WPointF point) {
		return mapFromDeviceWithoutTransform(point, Axis.OrdinateAxis);
	}

	/**
	 * Maps from device coordinates to model coordinates, ignoring the current
	 * zoom range.
	 * <p>
	 * Maps a position in the chart back to model coordinates, as if the chart
	 * was not zoomed in (nor panned).
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish that the
	 * mapping already reflects model changes since the last rendering, you
	 * should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * This function will not take the current zoom range into account. The
	 * mapping will be performed as if
	 * {@link WCartesianChart#getZoomRangeTransform() getZoomRangeTransform()}
	 * is the identity transform.
	 * <p>
	 * 
	 * @see WCartesianChart#mapToDeviceWithoutTransform(Object xValue, Object
	 *      yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 */
	public WPointF mapFromDeviceWithoutTransform(final WPointF point,
			int ordinateAxis) {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis yAxis = this.yAxis(ordinateAxis);
		WPointF p = this.inverseHv(point.getX(), point.getY(), this.getWidth()
				.toPixels());
		return new WPointF(xAxis.mapFromDevice(p.getX()
				- this.chartArea_.getLeft()),
				yAxis.mapFromDevice(this.chartArea_.getBottom() - p.getY()));
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * This returns the chart device coordinates for a (x,y) pair of model
	 * values.
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish that the
	 * mapping already reflects model changes since the last rendering, you
	 * should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * The <code>xSegment</code> and <code>ySegment</code> arguments are
	 * relevant only when the corresponding axis is broken using
	 * {@link WAxis#setBreak(double minimum, double maximum) WAxis#setBreak()}.
	 * Then, its possible values may be 0 (below the break) or 1 (above the
	 * break).
	 * <p>
	 * If the chart is interactive, mapToDevice will correctly take the current
	 * zoom range into account.
	 * <p>
	 * 
	 * @see WCartesianChart#mapFromDevice(WPointF point, Axis ordinateAxis)
	 */
	public WPointF mapToDevice(final Object xValue, final Object yValue,
			Axis axis, int xSegment, int ySegment) {
		return this.mapToDevice(xValue, yValue, axis == Axis.Y1Axis ? 0 : 1,
				xSegment, ySegment);
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * Returns
	 * {@link #mapToDevice(Object xValue, Object yValue, Axis axis, int xSegment, int ySegment)
	 * mapToDevice(xValue, yValue, Axis.OrdinateAxis, 0, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue) {
		return mapToDevice(xValue, yValue, Axis.OrdinateAxis, 0, 0);
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * Returns
	 * {@link #mapToDevice(Object xValue, Object yValue, Axis axis, int xSegment, int ySegment)
	 * mapToDevice(xValue, yValue, axis, 0, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue,
			Axis axis) {
		return mapToDevice(xValue, yValue, axis, 0, 0);
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * Returns
	 * {@link #mapToDevice(Object xValue, Object yValue, Axis axis, int xSegment, int ySegment)
	 * mapToDevice(xValue, yValue, axis, xSegment, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue,
			Axis axis, int xSegment) {
		return mapToDevice(xValue, yValue, axis, xSegment, 0);
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * This returns the chart device coordinates for a (x,y) pair of model
	 * values.
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish that the
	 * mapping already reflects model changes since the last rendering, you
	 * should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * The <code>xSegment</code> and <code>ySegment</code> arguments are
	 * relevant only when the corresponding axis is broken using
	 * {@link WAxis#setBreak(double minimum, double maximum) WAxis#setBreak()}.
	 * Then, its possible values may be 0 (below the break) or 1 (above the
	 * break).
	 * <p>
	 * If the chart is interactive, mapToDevice will correctly take the current
	 * zoom range into account.
	 * <p>
	 * 
	 * @see WCartesianChart#mapFromDevice(WPointF point, Axis ordinateAxis)
	 */
	public WPointF mapToDevice(final Object xValue, final Object yValue,
			int axis, int xSegment, int ySegment) {
		if (this.isInteractive()) {
			return this.zoomRangeTransform(this.xTransformHandle_.getValue(),
					this.yTransformHandle_.getValue()).map(
					this.mapToDeviceWithoutTransform(xValue, yValue, axis,
							xSegment, ySegment));
		} else {
			return this.mapToDeviceWithoutTransform(xValue, yValue, axis,
					xSegment, ySegment);
		}
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * Returns
	 * {@link #mapToDevice(Object xValue, Object yValue, int axis, int xSegment, int ySegment)
	 * mapToDevice(xValue, yValue, axis, 0, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue,
			int axis) {
		return mapToDevice(xValue, yValue, axis, 0, 0);
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * Returns
	 * {@link #mapToDevice(Object xValue, Object yValue, int axis, int xSegment, int ySegment)
	 * mapToDevice(xValue, yValue, axis, xSegment, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue,
			int axis, int xSegment) {
		return mapToDevice(xValue, yValue, axis, xSegment, 0);
	}

	/**
	 * Maps model values onto chart coordinates, ignoring the current zoom
	 * range.
	 * <p>
	 * This returns the chart device coordinates for a (x,y) pair of model
	 * values.
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish that the
	 * mapping already reflects model changes since the last rendering, you
	 * should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * The <code>xSegment</code> and <code>ySegment</code> arguments are
	 * relevant only when the corresponding axis is broken using
	 * {@link WAxis#setBreak(double minimum, double maximum) WAxis#setBreak()}.
	 * Then, its possible values may be 0 (below the break) or 1 (above the
	 * break).
	 * <p>
	 * This function will not take the current zoom range into account.The
	 * mapping will be performed as if
	 * {@link WCartesianChart#getZoomRangeTransform() getZoomRangeTransform()}
	 * is the identity transform.
	 * <p>
	 * 
	 * @see WCartesianChart#mapFromDeviceWithoutTransform(WPointF point, Axis
	 *      ordinateAxis)
	 */
	public WPointF mapToDeviceWithoutTransform(final Object xValue,
			final Object yValue, Axis ordinateAxis, int xSegment, int ySegment) {
		return this.mapToDeviceWithoutTransform(xValue, yValue,
				ordinateAxis == Axis.Y1Axis ? 0 : 1, xSegment, ySegment);
	}

	/**
	 * Maps model values onto chart coordinates, ignoring the current zoom
	 * range.
	 * <p>
	 * Returns
	 * {@link #mapToDeviceWithoutTransform(Object xValue, Object yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 * mapToDeviceWithoutTransform(xValue, yValue, Axis.OrdinateAxis, 0, 0)}
	 */
	public final WPointF mapToDeviceWithoutTransform(final Object xValue,
			final Object yValue) {
		return mapToDeviceWithoutTransform(xValue, yValue, Axis.OrdinateAxis,
				0, 0);
	}

	/**
	 * Maps model values onto chart coordinates, ignoring the current zoom
	 * range.
	 * <p>
	 * Returns
	 * {@link #mapToDeviceWithoutTransform(Object xValue, Object yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 * mapToDeviceWithoutTransform(xValue, yValue, ordinateAxis, 0, 0)}
	 */
	public final WPointF mapToDeviceWithoutTransform(final Object xValue,
			final Object yValue, Axis ordinateAxis) {
		return mapToDeviceWithoutTransform(xValue, yValue, ordinateAxis, 0, 0);
	}

	/**
	 * Maps model values onto chart coordinates, ignoring the current zoom
	 * range.
	 * <p>
	 * Returns
	 * {@link #mapToDeviceWithoutTransform(Object xValue, Object yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 * mapToDeviceWithoutTransform(xValue, yValue, ordinateAxis, xSegment, 0)}
	 */
	public final WPointF mapToDeviceWithoutTransform(final Object xValue,
			final Object yValue, Axis ordinateAxis, int xSegment) {
		return mapToDeviceWithoutTransform(xValue, yValue, ordinateAxis,
				xSegment, 0);
	}

	/**
	 * Maps model values onto chart coordinates, ignoring the current zoom
	 * range.
	 * <p>
	 * This returns the chart device coordinates for a (x,y) pair of model
	 * values.
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish that the
	 * mapping already reflects model changes since the last rendering, you
	 * should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * The <code>xSegment</code> and <code>ySegment</code> arguments are
	 * relevant only when the corresponding axis is broken using
	 * {@link WAxis#setBreak(double minimum, double maximum) WAxis#setBreak()}.
	 * Then, its possible values may be 0 (below the break) or 1 (above the
	 * break).
	 * <p>
	 * This function will not take the current zoom range into account.The
	 * mapping will be performed as if
	 * {@link WCartesianChart#getZoomRangeTransform() getZoomRangeTransform()}
	 * is the identity transform.
	 * <p>
	 * 
	 * @see WCartesianChart#mapFromDeviceWithoutTransform(WPointF point, Axis
	 *      ordinateAxis)
	 */
	public WPointF mapToDeviceWithoutTransform(final Object xValue,
			final Object yValue, int ordinateAxis, int xSegment, int ySegment) {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis yAxis = this.yAxis(ordinateAxis);
		double x = this.chartArea_.getLeft()
				+ xAxis.mapToDevice(xValue, xSegment);
		double y = this.chartArea_.getBottom()
				- yAxis.mapToDevice(yValue, ySegment);
		return this.hv(x, y, this.getWidth().toPixels());
	}

	/**
	 * Maps model values onto chart coordinates, ignoring the current zoom
	 * range.
	 * <p>
	 * Returns
	 * {@link #mapToDeviceWithoutTransform(Object xValue, Object yValue, int ordinateAxis, int xSegment, int ySegment)
	 * mapToDeviceWithoutTransform(xValue, yValue, ordinateAxis, 0, 0)}
	 */
	public final WPointF mapToDeviceWithoutTransform(final Object xValue,
			final Object yValue, int ordinateAxis) {
		return mapToDeviceWithoutTransform(xValue, yValue, ordinateAxis, 0, 0);
	}

	/**
	 * Maps model values onto chart coordinates, ignoring the current zoom
	 * range.
	 * <p>
	 * Returns
	 * {@link #mapToDeviceWithoutTransform(Object xValue, Object yValue, int ordinateAxis, int xSegment, int ySegment)
	 * mapToDeviceWithoutTransform(xValue, yValue, ordinateAxis, xSegment, 0)}
	 */
	public final WPointF mapToDeviceWithoutTransform(final Object xValue,
			final Object yValue, int ordinateAxis, int xSegment) {
		return mapToDeviceWithoutTransform(xValue, yValue, ordinateAxis,
				xSegment, 0);
	}

	/**
	 * Initializes the chart layout.
	 * <p>
	 * The mapping between model and device coordinates is only established
	 * after a rendering phase, or after calling initLayout manually.
	 * <p>
	 * You need a layout in order to use the
	 * {@link WCartesianChart#mapFromDevice(WPointF point, Axis ordinateAxis)
	 * mapFromDevice()} and
	 * {@link WCartesianChart#mapToDevice(Object xValue, Object yValue, Axis axis, int xSegment, int ySegment)
	 * mapToDevice()} methods.
	 * <p>
	 * Unless a specific chart rectangle is specified, the entire widget area is
	 * assumed.
	 */
	public boolean initLayout(final WRectF rectangle, WPaintDevice device) {
		WRectF rect = rectangle;
		if ((rect == null) || rect.isEmpty()) {
			rect = new WRectF(0.0, 0.0, this.getWidth().toPixels(), this
					.getHeight().toPixels());
		}
		if (this.getOrientation() == Orientation.Vertical) {
			this.width_ = (int) rect.getWidth();
			this.height_ = (int) rect.getHeight();
		} else {
			this.width_ = (int) rect.getHeight();
			this.height_ = (int) rect.getWidth();
		}
		this.xAxisLocation_ = AxisValue.MinimumValue;
		for (int i = 0; i < this.yAxisLocations_.size(); ++i) {
			this.yAxisLocations_.get(i).loc = AxisValue.MinimumValue;
		}
		WPaintDevice d = device;
		if (!(d != null)) {
			d = this.getCreatePaintDevice();
		}
		boolean autoLayout = this.isAutoLayoutEnabled();
		if (autoLayout
				&& EnumUtils.mask(d.getFeatures(),
						WPaintDevice.FeatureFlag.HasFontMetrics).equals(0)) {
			logger.error(new StringWriter()
					.append("setAutoLayout(): device does not have font metrics (not even server-side font metrics).")
					.toString());
			autoLayout = false;
		}
		if (autoLayout) {
			WCartesianChart self = this;
			self.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
			self.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
			this.calcChartArea();
			this.xTransform_.assign(new WTransform());
			this.yTransform_.assign(new WTransform());
			if (this.chartArea_.getWidth() <= 5
					|| this.chartArea_.getHeight() <= 5
					|| !this.isPrepareAxes()) {
				if (this.isInteractive()) {
					this.xTransform_.assign(this.xTransformHandle_.getValue());
					this.yTransform_.assign(this.yTransformHandle_.getValue());
				}
				return false;
			}
			{
				WMeasurePaintDevice md = new WMeasurePaintDevice(d);
				WPainter painter = new WPainter(md);
				this.renderAxes(painter,
						EnumSet.of(AxisProperty.Line, AxisProperty.Labels));
				this.renderLegend(painter);
				WRectF bounds = md.getBoundingRect();
				final int MARGIN = 5;
				int corrLeft = (int) Math.max(0.0,
						rect.getLeft() - bounds.getLeft() + MARGIN);
				int corrRight = (int) Math.max(0.0,
						bounds.getRight() - rect.getRight() + MARGIN);
				int corrTop = (int) Math.max(0.0,
						rect.getTop() - bounds.getTop() + MARGIN);
				int corrBottom = (int) Math.max(0.0,
						bounds.getBottom() - rect.getBottom() + MARGIN);
				self.setPlotAreaPadding(this.getPlotAreaPadding(Side.Left)
						+ corrLeft, EnumSet.of(Side.Left));
				self.setPlotAreaPadding(this.getPlotAreaPadding(Side.Right)
						+ corrRight, EnumSet.of(Side.Right));
				self.setPlotAreaPadding(this.getPlotAreaPadding(Side.Top)
						+ corrTop, EnumSet.of(Side.Top));
				self.setPlotAreaPadding(this.getPlotAreaPadding(Side.Bottom)
						+ corrBottom, EnumSet.of(Side.Bottom));
			}
		}
		if (!(device != null)) {
			;
		}
		this.calcChartArea();
		boolean result = this.chartArea_.getWidth() > 5
				&& this.chartArea_.getHeight() > 5 && this.isPrepareAxes();
		if (this.isInteractive()) {
			this.xTransform_.assign(this.xTransformHandle_.getValue());
			this.yTransform_.assign(this.yTransformHandle_.getValue());
		} else {
			this.xTransform_.assign(new WTransform());
			this.yTransform_.assign(new WTransform());
		}
		if (this.isInteractive()) {
			WCartesianChart self = this;
			self.clearPens();
			self.createPensForAxis(Axis.XAxis);
			self.createPensForAxis(Axis.YAxis);
			if (this.curvePaths_.isEmpty()) {
				self.assignJSHandlesForAllSeries();
			}
		}
		return result;
	}

	/**
	 * Initializes the chart layout.
	 * <p>
	 * Returns {@link #initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout(null, (WPaintDevice)null)}
	 */
	public final boolean initLayout() {
		return initLayout(null, (WPaintDevice) null);
	}

	/**
	 * Initializes the chart layout.
	 * <p>
	 * Returns {@link #initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout(rectangle, (WPaintDevice)null)}
	 */
	public final boolean initLayout(final WRectF rectangle) {
		return initLayout(rectangle, (WPaintDevice) null);
	}

	/**
	 * Creates a widget which renders the a legend item.
	 * <p>
	 * The legend item widget will contain a text and a {@link WPaintedWidget}
	 * which draws the series&apos; symbol.
	 */
	public WWidget createLegendItemWidget(int index) {
		WContainerWidget legendItem = new WContainerWidget();
		legendItem.addWidget(new WCartesianChart.IconWidget(this, index));
		WText label = new WText(this.getSeries(index).getModel()
				.getHeaderData(index));
		label.setVerticalAlignment(AlignmentFlag.AlignTop);
		legendItem.addWidget(label);
		return legendItem;
	}

	/**
	 * Adds a data point area (used for displaying e.g. tooltips).
	 * <p>
	 * You may want to specialize this is if you wish to modify (or delete) the
	 * area.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Currently, an area is only created if the
	 * {@link ItemDataRole#ToolTipRole} data at the data point is not empty.
	 * </i>
	 * </p>
	 */
	public void addDataPointArea(final WDataSeries series, int xRow,
			int xColumn, WAbstractArea area) {
		if (this.getAreas().isEmpty()) {
			this.addAreaMask();
		}
		this.addArea(area);
	}

	/**
	 * Sets the padding between the chart area and the axes.
	 * <p>
	 * This calls WAxes::setPadding() on all axes.
	 * <p>
	 * 
	 * @see WCartesianChart#getAxisPadding()
	 */
	public void setAxisPadding(int padding) {
		this.axisPadding_ = padding;
		this.xAxis_.setPadding(padding);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).setPadding(padding);
		}
	}

	/**
	 * Returns the padding between the chart area and the axes.
	 * <p>
	 * This number may not reflect the actual padding of the individual axes, if
	 * another padding has been applied on the individual axes.
	 * <p>
	 * 
	 * @see WCartesianChart#setAxisPadding(int padding)
	 */
	public int getAxisPadding() {
		return this.axisPadding_;
	}

	/**
	 * Sets the pen of the border to be drawn around the chart area.
	 * <p>
	 * 
	 * @see WCartesianChart#getBorderPen()
	 */
	public void setBorderPen(final WPen pen) {
		if (!this.borderPen_.equals(pen)) {
			this.borderPen_ = pen;
			this.update();
		}
	}

	/**
	 * Returns the pen used to draw the border around the chart area.
	 * <p>
	 * Defaults to NoPen.
	 * <p>
	 * 
	 * @see WCartesianChart#setBorderPen(WPen pen)
	 */
	public WPen getBorderPen() {
		return this.borderPen_;
	}

	/**
	 * Add a curve label.
	 * <p>
	 * 
	 * @see CurveLabel#CurveLabel(WDataSeries series, WPointF point, String
	 *      label)
	 */
	public void addCurveLabel(final CurveLabel label) {
		this.curveLabels_.add(label);
		this.update();
	}

	/**
	 * Configure all curve labels at once.
	 * <p>
	 * 
	 * @see WCartesianChart#addCurveLabel(CurveLabel label)
	 */
	public void setCurveLabels(final List<CurveLabel> labels) {
		Utils.copyList(labels, this.curveLabels_);
		this.update();
	}

	/**
	 * Clear all curve labels.
	 * <p>
	 * 
	 * @see WCartesianChart#addCurveLabel(CurveLabel label)
	 */
	public void clearCurveLabels() {
		this.curveLabels_.clear();
		this.update();
	}

	/**
	 * Get all of the registered curve labels.
	 * <p>
	 * 
	 * @see WCartesianChart#setCurveLabels(List labels)
	 */
	public List<CurveLabel> getCurveLabels() {
		return this.curveLabels_;
	}

	/**
	 * Returns whether this chart is interactive.
	 * <p>
	 * Return true iff one of the interactive features is enabled, and the chart
	 * is being rendered on an HTML canvas.
	 */
	boolean isInteractive() {
		return (this.zoomEnabled_ || this.panEnabled_ || this.crosshairEnabled_
				|| this.followCurve_ != null
				|| this.axisSliderWidgets_.size() > 0
				|| this.seriesSelectionEnabled_ || this.curveManipulationEnabled_)
				&& this.getMethod() == WPaintedWidget.Method.HtmlCanvas;
	}

	/**
	 * Enables zoom functionality.
	 * <p>
	 * When using the mouse, press the ctrl key while scrolling to zoom in/out a
	 * specific point on the chart. If you press shift+ctrl, it will only zoom
	 * vertically. If you press alt+ctrl, it will only zoom horizontally. To
	 * change these default mappings, use
	 * {@link WCartesianChart#setWheelActions(Map wheelActions)
	 * setWheelActions()}.
	 * <p>
	 * When using touch, you can use a pinch gesture to zoom in/out. If the
	 * pinch gesture is vertical/horizontal, it will zoom only
	 * vertically/horizontally, otherwise it will zoom both axes equally.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @see WCartesianChart#isZoomEnabled()
	 * @see WCartesianChart#setWheelActions(Map wheelActions)
	 */
	public void setZoomEnabled(boolean zoomEnabled) {
		if (this.zoomEnabled_ != zoomEnabled) {
			this.zoomEnabled_ = zoomEnabled;
			this.updateJSConfig("zoom", this.zoomEnabled_);
		}
	}

	/**
	 * Enables zoom functionality.
	 * <p>
	 * Calls {@link #setZoomEnabled(boolean zoomEnabled) setZoomEnabled(true)}
	 */
	public final void setZoomEnabled() {
		setZoomEnabled(true);
	}

	/**
	 * Returns whether zoom is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setZoomEnabled(boolean zoomEnabled)
	 */
	public boolean isZoomEnabled() {
		return this.zoomEnabled_;
	}

	/**
	 * Enables pan functionality.
	 * <p>
	 * When using the mouse, you can click and drag to pan the chart (if zoomed
	 * in), or use the scrollwheel.
	 * <p>
	 * When using touch, you can drag to pan the chart. If the rubberband effect
	 * is enabled, this is intertial (it will keep scrolling after you let go)
	 * and there is an overscroll and bounce back effect on the sides.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @see WCartesianChart#isPanEnabled()
	 */
	public void setPanEnabled(boolean panEnabled) {
		if (this.panEnabled_ != panEnabled) {
			this.panEnabled_ = panEnabled;
			this.updateJSConfig("pan", this.panEnabled_);
		}
	}

	/**
	 * Enables pan functionality.
	 * <p>
	 * Calls {@link #setPanEnabled(boolean panEnabled) setPanEnabled(true)}
	 */
	public final void setPanEnabled() {
		setPanEnabled(true);
	}

	/**
	 * Returns whether pan is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setPanEnabled(boolean panEnabled)
	 */
	public boolean isPanEnabled() {
		return this.panEnabled_;
	}

	/**
	 * Enables the crosshair functionality.
	 * <p>
	 * When enabled, the crosshair will follow mouse movement, and show in the
	 * top right corner the coordinate (according to X axis and the first Y
	 * axis) corresponding to this position.
	 * <p>
	 * When using touch, the crosshair can be moved with a drag. If both panning
	 * and the crosshair are enabled, the crosshair will be moved when dragging
	 * close to the crosshair. Otherwise, the chart will pan.
	 */
	public void setCrosshairEnabled(boolean crosshair) {
		if (this.crosshairEnabled_ != crosshair) {
			this.crosshairEnabled_ = crosshair;
			this.updateJSConfig("crosshair", this.crosshairEnabled_);
		}
	}

	/**
	 * Enables the crosshair functionality.
	 * <p>
	 * Calls {@link #setCrosshairEnabled(boolean crosshair)
	 * setCrosshairEnabled(true)}
	 */
	public final void setCrosshairEnabled() {
		setCrosshairEnabled(true);
	}

	/**
	 * Returns whether the crosshair is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setCrosshairEnabled(boolean crosshair)
	 */
	public boolean isCrosshairEnabled() {
		return this.crosshairEnabled_;
	}

	/**
	 * Sets the crosshair color.
	 * <p>
	 * The crosshair color is black by default.
	 * <p>
	 * 
	 * @see WCartesianChart#setCrosshairEnabled(boolean crosshair)
	 */
	public void setCrosshairColor(final WColor color) {
		if (!this.crosshairColor_.equals(color)) {
			this.crosshairColor_ = color;
			this.updateJSConfig("crosshairColor",
					jsStringLiteral(color.getCssText(true)));
		}
	}

	/**
	 * Returns the crosshair color.
	 * <p>
	 * 
	 * @see WCartesianChart#setCrosshairEnabled(boolean crosshair)
	 * @see WCartesianChart#setCrosshairColor(WColor color)
	 */
	public WColor getCrosshairColor() {
		return this.crosshairColor_;
	}

	/**
	 * Sets the Y axis to use for the crosshair.
	 * <p>
	 * Defaults to 0 (first Y axis)
	 */
	public void setCrosshairYAxis(int yAxis) {
		if (this.crosshairYAxis_ != yAxis) {
			this.crosshairYAxis_ = yAxis;
		}
	}

	/**
	 * Returns the Y axis to use for the crosshair.
	 */
	public int getCrosshairYAxis() {
		return this.crosshairYAxis_;
	}

	/**
	 * Enables the follow curve functionality for a data series.
	 * <p>
	 * This enables follow curve functionality for the data series corresponding
	 * to the given column.
	 * <p>
	 * If the data series is of type LineSeries or CurveSeries, the crosshair
	 * can only be moved in the x direction. The y position of the crosshair
	 * will be determined by the value of the data series. The crosshair will
	 * snap to the nearest point that is defined in the data series.
	 * <p>
	 * When using the mouse, the x position will change on mouseover. When using
	 * touch, the x position can be moved with a drag. The follow curve
	 * functionality has priority over the crosshair functionality.
	 * <p>
	 * Use column index -1 or {@link WCartesianChart#disableFollowCurve()
	 * disableFollowCurve()} to disable the follow curve feature.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The follow curve functionality requires that the X axis
	 * values of the data series are monotonically increasing or decreasing.</i>
	 * </p>
	 * 
	 * @deprecated Use {@link WCartesianChart#setFollowCurve(WDataSeries series)
	 *             setFollowCurve()} instead
	 */
	public void setFollowCurve(int followCurve) {
		if (followCurve == -1) {
			this.setFollowCurve((WDataSeries) null);
		} else {
			for (int i = 0; i < this.series_.size(); ++i) {
				if (this.series_.get(i).getModelColumn() == followCurve) {
					this.setFollowCurve(this.series_.get(i));
				}
			}
		}
	}

	/**
	 * Enabled the follow curve funtionality for a data series.
	 * <p>
	 * This enables follow curve functionality for the data series corresponding
	 * to the given column.
	 * <p>
	 * If the data series is of type LineSeries or CurveSeries, the crosshair
	 * can only be moved in the x direction. The y position of the crosshair
	 * will be determined by the value of the data series. The crosshair will
	 * snap to the nearest point that is defined in the data series.
	 * <p>
	 * When using the mouse, the x position will change on mouseover. When using
	 * touch, the x position can be moved with a drag. The follow curve
	 * functionality has priority over the crosshair functionality.
	 * <p>
	 * Set to null to disable the follow curve feature.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The follow curve functionality requires that the X axis
	 * values of the data series are monotonically increasing or decreasing.
	 * </i>
	 * </p>
	 */
	public void setFollowCurve(WDataSeries series) {
		if (this.followCurve_ != series) {
			this.followCurve_ = series;
			this.updateJSConfig("followCurve",
					series != null ? this.getSeriesIndexOf(series) : -1);
		}
	}

	/**
	 * Disable the follow curve functionality.
	 * <p>
	 * 
	 * @see WCartesianChart#setFollowCurve(int followCurve)
	 */
	public void disableFollowCurve() {
		this.setFollowCurve((WDataSeries) null);
	}

	/**
	 * Returns the curve that is to be followed.
	 * <p>
	 * If follow curve functionality is not enabled, returns -1.
	 * <p>
	 * 
	 * @see WCartesianChart#setFollowCurve(int followCurve)
	 */
	public WDataSeries getFollowCurve() {
		return this.followCurve_;
	}

	/**
	 * Enables/disables the inertial scrolling and rubberband effect.
	 * <p>
	 * 
	 * @see WCartesianChart#setPanEnabled(boolean panEnabled)
	 */
	public void setRubberBandEffectEnabled(boolean rubberBandEnabled) {
		if (this.rubberBandEnabled_ != rubberBandEnabled) {
			this.rubberBandEnabled_ = rubberBandEnabled;
			this.updateJSConfig("rubberBand", this.rubberBandEnabled_);
		}
	}

	/**
	 * Enables/disables the inertial scrolling and rubberband effect.
	 * <p>
	 * Calls {@link #setRubberBandEffectEnabled(boolean rubberBandEnabled)
	 * setRubberBandEffectEnabled(true)}
	 */
	public final void setRubberBandEffectEnabled() {
		setRubberBandEffectEnabled(true);
	}

	/**
	 * Checks whether the rubberband effect is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setRubberBandEffectEnabled(boolean
	 *      rubberBandEnabled)
	 */
	public boolean isRubberBandEffectEnabled() {
		return this.rubberBandEnabled_;
	}

	/**
	 * Sets the mapping of mouse wheel actions for interactive charts.
	 * <p>
	 * 
	 * @see WCartesianChart#getWheelActions()
	 */
	public void setWheelActions(
			Map<EnumSet<KeyboardModifier>, InteractiveAction> wheelActions) {
		this.wheelActions_ = wheelActions;
		this.updateJSConfig("wheelActions",
				wheelActionsToJson(this.wheelActions_));
	}

	/**
	 * Returns the current mouse wheel actions for interactive charts.
	 * <p>
	 * 
	 * @see WCartesianChart#setWheelActions(Map wheelActions)
	 */
	public Map<EnumSet<KeyboardModifier>, InteractiveAction> getWheelActions() {
		return this.wheelActions_;
	}

	/**
	 * Enables or disables soft label clipping on all axes.
	 * <p>
	 * 
	 * @see WAxis#setSoftLabelClipping(boolean enabled)
	 */
	public void setSoftLabelClipping(boolean enabled) {
		this.xAxis_.setSoftLabelClipping(enabled);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).setSoftLabelClipping(enabled);
		}
	}

	/**
	 * Sets whether series selection is enabled.
	 * <p>
	 * If series selection is enabled, series can be selected with a mouse click
	 * or long press. If the selected series is a LineSeries or CurveSeries, it
	 * can be manipulated if
	 * {@link WCartesianChart#setCurveManipulationEnabled(boolean enabled) curve
	 * manipulation} is enabled. The series that are not selected, will be shown
	 * in a lighter color.
	 */
	public void setSeriesSelectionEnabled(boolean enabled) {
		if (this.seriesSelectionEnabled_ != enabled) {
			this.seriesSelectionEnabled_ = enabled;
			this.updateJSConfig("seriesSelection", this.seriesSelectionEnabled_);
		}
	}

	/**
	 * Sets whether series selection is enabled.
	 * <p>
	 * Calls {@link #setSeriesSelectionEnabled(boolean enabled)
	 * setSeriesSelectionEnabled(true)}
	 */
	public final void setSeriesSelectionEnabled() {
		setSeriesSelectionEnabled(true);
	}

	/**
	 * Returns whether series selection is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setSeriesSelectionEnabled(boolean enabled)
	 */
	public boolean isSeriesSelectionEnabled() {
		return this.seriesSelectionEnabled_;
	}

	/**
	 * A signal that notifies the selection of a new curve.
	 * <p>
	 * This signal is emitted if a series is selected using a mouse click or
	 * long press. The first argument is the selected series. The second
	 * argument is the point that was selected, in model coordinates.
	 * <p>
	 * 
	 * @see WCartesianChart#setSeriesSelectionEnabled(boolean enabled)
	 */
	public Signal2<WDataSeries, WPointF> seriesSelected() {
		return this.seriesSelected_;
	}

	/**
	 * Sets the series that is currently selected.
	 * <p>
	 * The series with the given model column will be selected. The other series
	 * will be shown in a lighter color. The series that is currently selected
	 * is the one that can be manipulated if
	 * {@link WCartesianChart#setCurveManipulationEnabled(boolean enabled) curve
	 * manipulation} is enabled, and it is a LineSeries or CurveSeries.
	 * <p>
	 * The selected series can be changed using a long touch or mouse click.
	 * <p>
	 * If the argument provided is null or
	 * {@link WCartesianChart#setSeriesSelectionEnabled(boolean enabled) series
	 * selection} is not enabled, no series will be selected.
	 * <p>
	 * 
	 * @see WCartesianChart#setCurveManipulationEnabled(boolean enabled)
	 * @see WCartesianChart#setSeriesSelectionEnabled(boolean enabled)
	 */
	public void setSelectedSeries(WDataSeries series) {
		if (this.selectedSeries_ != series) {
			this.selectedSeries_ = series;
			this.update();
		}
	}

	/**
	 * Get the currently selected curve.
	 * <p>
	 * -1 means that no curve is currently selected.
	 * <p>
	 * 
	 * @see WCartesianChart#setSelectedSeries(WDataSeries series)
	 */
	public WDataSeries getSelectedSeries() {
		return this.selectedSeries_;
	}

	/**
	 * Enable curve manipulation.
	 * <p>
	 * If curve manipulation is enabled, the
	 * {@link WDataSeries#setScale(double scale) scale} and
	 * {@link WDataSeries#setOffset(double offset) offset} of the
	 * {@link WCartesianChart#getSelectedSeries() selected curve} can be
	 * manipulated interactively using drag, scroll, and pinch.
	 * <p>
	 * 
	 * @see WDataSeries#setOffset(double offset)
	 * @see WDataSeries#setScale(double scale)
	 */
	public void setCurveManipulationEnabled(boolean enabled) {
		if (this.curveManipulationEnabled_ != enabled) {
			this.curveManipulationEnabled_ = enabled;
			this.updateJSConfig("curveManipulation",
					this.curveManipulationEnabled_);
		}
	}

	/**
	 * Enable curve manipulation.
	 * <p>
	 * Calls {@link #setCurveManipulationEnabled(boolean enabled)
	 * setCurveManipulationEnabled(true)}
	 */
	public final void setCurveManipulationEnabled() {
		setCurveManipulationEnabled(true);
	}

	/**
	 * Returns whether curve manipulation is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setCurveManipulationEnabled(boolean enabled)
	 */
	public boolean isCurveManipulationEnabled() {
		return this.curveManipulationEnabled_;
	}

	public void iterateSeries(SeriesIterator iterator, WPainter painter,
			boolean reverseStacked) {
		double groupWidth = 0.0;
		int numBarGroups;
		int currentBarGroup;
		int rowCount = this.getModel() != null ? this.getModel().getRowCount()
				: 0;
		List<Double> posStackedValuesInit = new ArrayList<Double>();
		List<Double> minStackedValuesInit = new ArrayList<Double>();
		{
			int insertPos = 0;
			for (int ii = 0; ii < (rowCount); ++ii)
				posStackedValuesInit.add(insertPos + ii, 0.0);
		}
		;
		{
			int insertPos = 0;
			for (int ii = 0; ii < (rowCount); ++ii)
				minStackedValuesInit.add(insertPos + ii, 0.0);
		}
		;
		final boolean scatterPlot = this.type_ == ChartType.ScatterPlot;
		if (scatterPlot) {
			numBarGroups = 1;
			currentBarGroup = 0;
		} else {
			numBarGroups = this.getCalcNumBarGroups();
			currentBarGroup = 0;
		}
		boolean containsBars = false;
		for (int g = 0; g < this.series_.size(); ++g) {
			if (this.series_.get(g).isHidden()
					&& !(this.axisSliderWidgetForSeries(this.series_.get(g)) && (((iterator) instanceof SeriesRenderIterator ? (SeriesRenderIterator) (iterator)
							: null) != null || ((iterator) instanceof ExtremesIterator ? (ExtremesIterator) (iterator)
							: null) != null))) {
				continue;
			}
			groupWidth = this.series_.get(g).getBarWidth()
					* (this.map(2, 0).getX() - this.map(1, 0).getX());
			if (containsBars) {
				++currentBarGroup;
			}
			containsBars = false;
			int startSeries;
			int endSeries;
			if (scatterPlot) {
				startSeries = endSeries = g;
			} else {
				if (this.series_.get(g).getModel() == this.getModel()) {
					for (int i = 0; i < rowCount; ++i) {
						posStackedValuesInit.set(i,
								minStackedValuesInit.set(i, 0.0));
					}
					if (reverseStacked) {
						endSeries = g;
						int a = this.series_.get(g).getYAxis();
						for (;;) {
							if (g < this.series_.size()
									&& ((int) g == endSeries || this.series_
											.get(g).isStacked())
									&& this.series_.get(g).getYAxis() == a) {
								if (this.series_.get(g).getType() == SeriesType.BarSeries) {
									containsBars = true;
								}
								for (int row = 0; row < rowCount; ++row) {
									double y = StringUtils.asNumber(this
											.getModel().getData(
													row,
													this.series_.get(g)
															.getModelColumn()));
									if (!Double.isNaN(y)) {
										if (y > 0) {
											posStackedValuesInit.set(
													row,
													posStackedValuesInit
															.get(row) + y);
										} else {
											minStackedValuesInit.set(
													row,
													minStackedValuesInit
															.get(row) + y);
										}
									}
								}
								++g;
							} else {
								break;
							}
						}
						--g;
						startSeries = g;
					} else {
						startSeries = g;
						int a = this.series_.get(g).getYAxis();
						if (this.series_.get(g).getType() == SeriesType.BarSeries) {
							containsBars = true;
						}
						++g;
						for (;;) {
							if (g < this.series_.size()
									&& this.series_.get(g).isStacked()
									&& this.series_.get(g).getYAxis() == a) {
								if (this.series_.get(g).getType() == SeriesType.BarSeries) {
									containsBars = true;
								}
								++g;
							} else {
								break;
							}
						}
						--g;
						endSeries = g;
					}
				} else {
					throw new WException(
							"Configuring different models for WDataSeries are unsupported for category charts!");
				}
			}
			int i = startSeries;
			for (;;) {
				boolean doSeries = iterator.startSeries(this.series_.get(i),
						groupWidth, numBarGroups, currentBarGroup);
				List<Double> posStackedValues = new ArrayList<Double>();
				List<Double> minStackedValues = new ArrayList<Double>();
				if (doSeries || !scatterPlot && i != endSeries) {
					for (int currentXSegment = 0; currentXSegment < this
							.getAxis(Axis.XAxis).getSegmentCount(); ++currentXSegment) {
						for (int currentYSegment = 0; currentYSegment < this
								.yAxis(this.series_.get(i).getYAxis())
								.getSegmentCount(); ++currentYSegment) {
							posStackedValues.clear();
							posStackedValues.addAll(posStackedValuesInit);
							minStackedValues.clear();
							minStackedValues.addAll(minStackedValuesInit);
							if (painter != null) {
								WRectF csa = this.chartSegmentArea(this
										.yAxis(this.series_.get(i).getYAxis()),
										currentXSegment, currentYSegment);
								iterator.startSegment(currentXSegment,
										currentYSegment, csa);
								painter.save();
								if (!this.isInteractive()) {
									WPainterPath clipPath = new WPainterPath();
									clipPath.addRect(this.hv(csa));
									painter.setClipPath(clipPath);
									painter.setClipping(true);
								}
							} else {
								iterator.startSegment(currentXSegment,
										currentYSegment, null);
							}
							for (int row = 0; row < (this.series_.get(i)
									.getModel() != null ? this.series_.get(i)
									.getModel().getRowCount() : 0); ++row) {
								int[] xIndex = { -1, -1 };
								int[] yIndex = { -1, -1 };
								double x;
								if (scatterPlot) {
									int c = this.series_.get(i).XSeriesColumn();
									if (c == -1) {
										c = this.XSeriesColumn();
									}
									if (c != -1) {
										xIndex[0] = row;
										xIndex[1] = c;
										x = this.series_.get(i).getModel()
												.getData(xIndex[0], xIndex[1]);
									} else {
										x = row;
									}
								} else {
									x = row;
								}
								yIndex[0] = row;
								yIndex[1] = this.series_.get(i)
										.getModelColumn();
								double y = this.series_.get(i).getModel()
										.getData(yIndex[0], yIndex[1]);
								if (scatterPlot) {
									iterator.newValue(this.series_.get(i), x,
											y, 0, xIndex[0], xIndex[1],
											yIndex[0], yIndex[1]);
								} else {
									double prevStack = 0;
									double nextStack = 0;
									boolean hasValue = !Double.isNaN(y);
									if (hasValue) {
										if (y > 0) {
											prevStack = nextStack = posStackedValues
													.get(row);
										} else {
											prevStack = nextStack = minStackedValues
													.get(row);
										}
										if (reverseStacked) {
											nextStack -= y;
										} else {
											nextStack += y;
										}
										if (y > 0) {
											posStackedValues
													.set(row, nextStack);
										} else {
											minStackedValues
													.set(row, nextStack);
										}
									}
									if (doSeries) {
										if (reverseStacked) {
											iterator.newValue(
													this.series_.get(i), x,
													hasValue ? prevStack : y,
													nextStack, xIndex[0],
													xIndex[1], yIndex[0],
													yIndex[1]);
										} else {
											iterator.newValue(
													this.series_.get(i), x,
													hasValue ? nextStack : y,
													prevStack, xIndex[0],
													xIndex[1], yIndex[0],
													yIndex[1]);
										}
									}
								}
							}
							iterator.endSegment();
							if (painter != null) {
								painter.restore();
							}
						}
					}
					posStackedValuesInit.clear();
					posStackedValuesInit.addAll(posStackedValues);
					minStackedValuesInit.clear();
					minStackedValuesInit.addAll(minStackedValues);
				}
				if (doSeries) {
					iterator.endSeries();
				}
				if (i == endSeries) {
					break;
				} else {
					if (endSeries < startSeries) {
						--i;
					} else {
						++i;
					}
				}
			}
		}
	}

	public final void iterateSeries(SeriesIterator iterator, WPainter painter) {
		iterateSeries(iterator, painter, false);
	}

	void addAxisSliderWidget(WAxisSliderWidget slider) {
		this.axisSliderWidgets_.add(slider);
		StringBuilder ss = new StringBuilder();
		ss.append('[');
		for (int i = 0; i < this.axisSliderWidgets_.size(); ++i) {
			if (i != 0) {
				ss.append(',');
			}
			ss.append('"').append(this.axisSliderWidgets_.get(i).getId())
					.append('"');
		}
		ss.append(']');
		this.updateJSConfig("sliders", ss.toString());
	}

	void removeAxisSliderWidget(WAxisSliderWidget slider) {
		for (int i = 0; i < this.axisSliderWidgets_.size(); ++i) {
			if (slider == this.axisSliderWidgets_.get(i)) {
				this.axisSliderWidgets_.remove(0 + i);
				StringBuilder ss = new StringBuilder();
				ss.append('[');
				for (int j = 0; j < this.axisSliderWidgets_.size(); ++j) {
					if (j != 0) {
						ss.append(',');
					}
					ss.append('"')
							.append(this.axisSliderWidgets_.get(j).getId())
							.append('"');
				}
				ss.append(']');
				this.updateJSConfig("sliders", ss.toString());
				return;
			}
		}
	}

	private WChart2DImplementation interface_;
	private Orientation orientation_;
	private int XSeriesColumn_;
	private ChartType type_;
	private List<WDataSeries> series_;
	private WAxis xAxis_;
	private List<WAxis> yAxes_;
	private double barMargin_;
	private WLegend legend_;
	private int axisPadding_;
	private WPen borderPen_;
	WPen textPen_;
	private int width_;
	private int height_;
	WRectF chartArea_;
	private AxisValue xAxisLocation_;
	boolean hasDeferredToolTips_;

	static class AxisLocation {
		private static Logger logger = LoggerFactory
				.getLogger(AxisLocation.class);

		public AxisLocation() {
			this.minOffset = 0;
			this.maxOffset = 0;
			this.loc = AxisValue.MinimumValue;
		}

		public int minOffset;
		public int maxOffset;
		public AxisValue loc;
	}

	private List<WCartesianChart.AxisLocation> yAxisLocations_;
	private boolean jsDefined_;
	private boolean zoomEnabled_;
	private boolean panEnabled_;
	private boolean rubberBandEnabled_;
	private boolean crosshairEnabled_;
	private WColor crosshairColor_;
	private int crosshairYAxis_;
	private boolean seriesSelectionEnabled_;
	private WDataSeries selectedSeries_;
	private WDataSeries followCurve_;
	private boolean curveManipulationEnabled_;
	boolean cObjCreated_;
	private JSignal xTransformChanged_;
	private JSignal yTransformChanged_;
	private Signal2<WDataSeries, WPointF> seriesSelected_;
	private JSignal2<Double, Double> jsSeriesSelected_;
	Map<WDataSeries, WJavaScriptHandle<WPainterPath>> curvePaths_;
	private List<WJavaScriptHandle<WPainterPath>> freePainterPaths_;
	Map<WDataSeries, WJavaScriptHandle<WTransform>> curveTransforms_;
	private List<WJavaScriptHandle<WTransform>> freeTransforms_;
	WTransform xTransform_;
	WTransform yTransform_;
	WJavaScriptHandle<WTransform> xTransformHandle_;
	WJavaScriptHandle<WTransform> yTransformHandle_;

	static class PenAssignment {
		private static Logger logger = LoggerFactory
				.getLogger(PenAssignment.class);

		public WJavaScriptHandle<WPen> pen;
		public WJavaScriptHandle<WPen> textPen;
		public WJavaScriptHandle<WPen> gridPen;

		public PenAssignment(final WJavaScriptHandle<WPen> pen,
				final WJavaScriptHandle<WPen> textPen,
				final WJavaScriptHandle<WPen> gridPen) {
			this.pen = pen;
			this.textPen = textPen;
			this.gridPen = gridPen;
		}
	}

	private Map<Axis, List<WCartesianChart.PenAssignment>> pens_;
	private List<WJavaScriptHandle<WPen>> freePens_;
	private List<CurveLabel> curveLabels_;
	private List<WAxisSliderWidget> axisSliderWidgets_;
	private Map<EnumSet<KeyboardModifier>, InteractiveAction> wheelActions_;
	private JSignal2<Double, Double> loadTooltip_;

	static class BarTooltip {
		private static Logger logger = LoggerFactory
				.getLogger(BarTooltip.class);

		public BarTooltip(final WDataSeries series, int xRow, int xColumn,
				int yRow, int yColumn) {
			this.series = series;
			this.xRow = xRow;
			this.xColumn = xColumn;
			this.yRow = yRow;
			this.yColumn = yColumn;
		}

		public double[] xs = new double[4];
		public double[] ys = new double[4];
		public WDataSeries series;
		public int xRow;
		public int xColumn;
		public int yRow;
		public int yColumn;
	}

	List<WCartesianChart.BarTooltip> barTooltips_;

	private void init() {
		this.setPalette(new WStandardPalette(WStandardPalette.Flavour.Muted));
		this.xAxis_ = new WAxis();
		this.xAxisLocation_ = AxisValue.MinimumValue;
		this.yAxes_.add(new WAxis());
		this.yAxisLocations_.add(new WCartesianChart.AxisLocation());
		WAxis y2axis = new WAxis();
		y2axis.setLocation(AxisValue.MaximumValue);
		this.yAxes_.add(y2axis);
		this.yAxisLocations_.add(new WCartesianChart.AxisLocation());
		this.getAxis(Axis.XAxis).init(this.interface_, Axis.XAxis);
		this.getAxis(Axis.YAxis).init(this.interface_, Axis.YAxis);
		this.getAxis(Axis.Y2Axis).init(this.interface_, Axis.Y2Axis);
		this.getAxis(Axis.XAxis).setPadding(this.axisPadding_);
		this.getAxis(Axis.YAxis).setPadding(this.axisPadding_);
		this.getAxis(Axis.Y2Axis).setPadding(this.axisPadding_);
		this.getAxis(Axis.XAxis).setSoftLabelClipping(true);
		this.getAxis(Axis.YAxis).setSoftLabelClipping(true);
		this.getAxis(Axis.Y2Axis).setSoftLabelClipping(true);
		this.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
		this.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
		this.xTransformHandle_ = this.createJSTransform();
		this.yTransformHandle_ = this.createJSTransform();
		this.xTransform_.assign(new WTransform());
		this.yTransform_.assign(new WTransform());
		if (WApplication.getInstance() != null
				&& WApplication.getInstance().getEnvironment().hasAjax()) {
			this.mouseWentDown().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.mouseDown(o, e);}}");
			this.mouseWentUp().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.mouseUp(o, e);}}");
			this.mouseDragged().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.mouseDrag(o, e);}}");
			this.mouseMoved().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.mouseMove(o, e);}}");
			this.mouseWheel().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.mouseWheel(o, e);}}");
			this.mouseWentOut().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.mouseOut(o, e);}}");
			this.touchStarted().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.touchStart(o, e);}}");
			this.touchEnded().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.touchEnd(o, e);}}");
			this.touchMoved().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.touchMoved(o, e);}}");
			this.clicked().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.clicked(o, e);}}");
			this.jsSeriesSelected_.addListener(this,
					new Signal2.Listener<Double, Double>() {
						public void trigger(Double e1, Double e2) {
							WCartesianChart.this.jsSeriesSelected(e1, e2);
						}
					});
			this.loadTooltip_.addListener(this,
					new Signal2.Listener<Double, Double>() {
						public void trigger(Double e1, Double e2) {
							WCartesianChart.this.loadTooltip(e1, e2);
						}
					});
		}
		this.wheelActions_.put(EnumSet.of(KeyboardModifier.NoModifier),
				InteractiveAction.PanMatching);
		this.wheelActions_.put(EnumSet.of(KeyboardModifier.AltModifier,
				KeyboardModifier.ControlModifier), InteractiveAction.ZoomX);
		this.wheelActions_.put(EnumSet.of(KeyboardModifier.ControlModifier,
				KeyboardModifier.ShiftModifier), InteractiveAction.ZoomY);
		this.wheelActions_.put(EnumSet.of(KeyboardModifier.ControlModifier),
				InteractiveAction.ZoomXY);
		this.wheelActions_.put(EnumSet.of(KeyboardModifier.AltModifier,
				KeyboardModifier.ControlModifier,
				KeyboardModifier.ShiftModifier), InteractiveAction.ZoomXY);
	}

	private static String wheelActionsToJson(
			Map<EnumSet<KeyboardModifier>, InteractiveAction> wheelActions) {
		StringBuilder ss = new StringBuilder();
		ss.append('{');
		boolean first = true;
		for (Iterator<Map.Entry<EnumSet<KeyboardModifier>, InteractiveAction>> it_it = wheelActions
				.entrySet().iterator(); it_it.hasNext();) {
			Map.Entry<EnumSet<KeyboardModifier>, InteractiveAction> it = it_it
					.next();
			if (first) {
				first = false;
			} else {
				ss.append(',');
			}
			ss.append(EnumUtils.valueOf(it.getKey())).append(':')
					.append((int) it.getValue().getValue());
		}
		ss.append('}');
		return ss.toString();
	}

	static WColor lightenColor(final WColor in) {
		double[] hsl = new double[3];
		in.toHSL(hsl);
		double h = hsl[0];
		double s = hsl[1];
		double l = hsl[2];
		return WColor.fromHSL(h, Math.max(s - 0.2, 0.0),
				Math.min(l + 0.2, 1.0), in.getAlpha());
	}

	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		super.getDomChanges(result, app);
		for (int i = 0; i < this.axisSliderWidgets_.size(); ++i) {
			this.axisSliderWidgets_.get(i).update();
		}
	}

	protected void modelChanged() {
		this.XSeriesColumn_ = -1;
		while (this.axisSliderWidgets_.size() > 0) {
			this.axisSliderWidgets_.get(this.axisSliderWidgets_.size() - 1)
					.setSeries((WDataSeries) null);
		}
		this.freeAllJSPaths();
		this.freeAllJSTransforms();
		for (int i = 0; i < this.series_.size(); ++i) {
			;
		}
		this.series_.clear();
		this.update();
	}

	protected void modelReset() {
		this.update();
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls
	 * {@link WCartesianChart#render(WPainter painter, WRectF rectangle)
	 * render()} to paint on the paint device.
	 */
	protected void paintEvent(WPaintDevice paintDevice) {
		this.hasDeferredToolTips_ = false;
		WPainter painter = new WPainter(paintDevice);
		painter.setRenderHint(WPainter.RenderHint.Antialiasing);
		this.paint(painter);
		if (this.hasDeferredToolTips_ && !this.jsDefined_) {
			this.defineJavaScript();
		}
		if (this.isInteractive() || this.hasDeferredToolTips_) {
			this.setZoomAndPan();
			double modelBottom = this.getAxis(Axis.Y1Axis).mapFromDevice(0);
			double modelTop = this.getAxis(Axis.Y1Axis).mapFromDevice(
					this.chartArea_.getHeight());
			double modelLeft = this.getAxis(Axis.XAxis).mapFromDevice(0);
			double modelRight = this.getAxis(Axis.XAxis).mapFromDevice(
					this.chartArea_.getWidth());
			WRectF modelArea = new WRectF(modelLeft, modelBottom, modelRight
					- modelLeft, modelTop - modelBottom);
			WRectF insideArea = this.getInsideChartArea();
			int coordPaddingX = 5;
			int coordPaddingY = 5;
			if (this.getOrientation() == Orientation.Vertical) {
				if (this.getAxis(Axis.XAxis).isVisible()
						&& this.getAxis(Axis.XAxis).getTickDirection() == TickDirection.Inwards
						&& (this.getAxis(Axis.XAxis).getLocation() == AxisValue.MaximumValue || this
								.getAxis(Axis.XAxis).getLocation() == AxisValue.BothSides)) {
					coordPaddingY = 25;
				}
				if (this.getAxis(Axis.Y1Axis).isVisible()
						&& this.getAxis(Axis.Y1Axis).getTickDirection() == TickDirection.Inwards
						&& (this.getAxis(Axis.Y1Axis).getLocation() == AxisValue.MaximumValue || this
								.getAxis(Axis.Y1Axis).getLocation() == AxisValue.BothSides)
						|| this.getAxis(Axis.Y2Axis).isVisible()
						&& this.getAxis(Axis.Y2Axis).getTickDirection() == TickDirection.Inwards) {
					coordPaddingX = 40;
				}
			} else {
				if (this.getAxis(Axis.XAxis).isVisible()
						&& this.getAxis(Axis.XAxis).getTickDirection() == TickDirection.Inwards
						&& (this.getAxis(Axis.XAxis).getLocation() == AxisValue.MaximumValue || this
								.getAxis(Axis.XAxis).getLocation() == AxisValue.BothSides)) {
					coordPaddingX = 40;
				}
				if (this.getAxis(Axis.Y1Axis).isVisible()
						&& this.getAxis(Axis.Y1Axis).getTickDirection() == TickDirection.Inwards
						&& (this.getAxis(Axis.Y1Axis).getLocation() == AxisValue.MinimumValue || this
								.getAxis(Axis.Y1Axis).getLocation() == AxisValue.BothSides)) {
					coordPaddingY = 25;
				}
			}
			if (this.getAxis(Axis.XAxis).zoomRangeChanged().isConnected()
					&& !this.xTransformChanged_.isConnected()) {
				this.xTransformChanged_.addListener(this,
						new Signal.Listener() {
							public void trigger() {
								WCartesianChart.this.xTransformChanged();
							}
						});
			}
			if (this.getAxis(Axis.YAxis).zoomRangeChanged().isConnected()
					&& !this.yTransformChanged_.isConnected()) {
				this.yTransformChanged_.addListener(this,
						new Signal.Listener() {
							public void trigger() {
								WCartesianChart.this.yTransformChanged();
							}
						});
			}
			char[] buf = new char[30];
			WApplication app = WApplication.getInstance();
			StringBuilder ss = new StringBuilder();
			int selectedCurve = this.selectedSeries_ != null ? this
					.getSeriesIndexOf(this.selectedSeries_) : -1;
			int followCurve = this.followCurve_ != null ? this
					.getSeriesIndexOf(this.followCurve_) : -1;
			ss.append("new Wt3_3_10.WCartesianChart(")
					.append(app.getJavaScriptClass())
					.append(",")
					.append(this.getJsRef())
					.append(",")
					.append(this.getObjJsRef())
					.append(",{curveManipulation:")
					.append(StringUtils
							.asString(this.curveManipulationEnabled_)
							.toString())
					.append(",seriesSelection:")
					.append(StringUtils.asString(this.seriesSelectionEnabled_)
							.toString())
					.append(",selectedCurve:")
					.append(selectedCurve)
					.append(",isHorizontal:")
					.append(StringUtils.asString(
							this.getOrientation() == Orientation.Horizontal)
							.toString())
					.append(",zoom:")
					.append(StringUtils.asString(this.zoomEnabled_).toString())
					.append(",pan:")
					.append(StringUtils.asString(this.panEnabled_).toString())
					.append(",crosshair:")
					.append(StringUtils.asString(this.crosshairEnabled_)
							.toString())
					.append(",crosshairColor:")
					.append(jsStringLiteral(this.crosshairColor_
							.getCssText(true)))
					.append(",followCurve:")
					.append(followCurve)
					.append(",xTransform:")
					.append(this.xTransformHandle_.getJsRef())
					.append(",yTransform:")
					.append(this.yTransformHandle_.getJsRef())
					.append(",area:")
					.append(this.hv(this.chartArea_).getJsRef())
					.append(",insideArea:")
					.append(this.hv(insideArea).getJsRef())
					.append(",modelArea:")
					.append(modelArea.getJsRef())
					.append(",hasToolTips:")
					.append(StringUtils.asString(this.hasDeferredToolTips_)
							.toString())
					.append(",notifyTransform:{x:")
					.append(StringUtils.asString(
							this.getAxis(Axis.XAxis).zoomRangeChanged()
									.isConnected()).toString())
					.append(",y:")
					.append(StringUtils.asString(
							this.getAxis(Axis.YAxis).zoomRangeChanged()
									.isConnected()).toString())
					.append("},ToolTipInnerStyle:")
					.append(jsStringLiteral(app.getTheme().utilityCssClass(
							UtilityCssClassRole.ToolTipInner)))
					.append(",ToolTipOuterStyle:")
					.append(jsStringLiteral(app.getTheme().utilityCssClass(
							UtilityCssClassRole.ToolTipOuter))).append(",");
			this.updateJSPens(ss);
			ss.append("series:{");
			{
				boolean firstCurvePath = true;
				for (int i = 0; i < this.series_.size(); ++i) {
					if (this.curvePaths_.get(this.series_.get(i)) != null) {
						if (firstCurvePath) {
							firstCurvePath = false;
						} else {
							ss.append(",");
						}
						ss.append((int) i).append(":{");
						ss.append("curve:")
								.append(this.curvePaths_.get(
										this.series_.get(i)).getJsRef())
								.append(",");
						ss.append("transform:").append(
								this.curveTransforms_.get(this.series_.get(i))
										.getJsRef());
						ss.append("}");
					}
				}
			}
			ss.append("},");
			ss.append("maxZoom:[")
					.append(MathUtils.roundJs(this.getAxis(Axis.XAxis)
							.getMaxZoom(), 3)).append(",");
			ss.append(
					MathUtils
							.roundJs(this.getAxis(Axis.Y1Axis).getMaxZoom(), 3))
					.append("],");
			ss.append("rubberBand:").append(this.rubberBandEnabled_)
					.append(',');
			ss.append("sliders:[");
			for (int i = 0; i < this.axisSliderWidgets_.size(); ++i) {
				if (i != 0) {
					ss.append(',');
				}
				ss.append('"').append(this.axisSliderWidgets_.get(i).getId())
						.append('"');
			}
			ss.append("],");
			ss.append("wheelActions:")
					.append(wheelActionsToJson(this.wheelActions_)).append(",");
			ss.append("coordinateOverlayPadding:[").append(coordPaddingX)
					.append(",");
			ss.append(coordPaddingY).append("]");
			ss.append("});");
			this.doJavaScript(ss.toString());
			this.cObjCreated_ = true;
		}
	}

	/**
	 * Renders the chart.
	 * <p>
	 * Renders the chart within the given rectangle. To accomodate both
	 * rendering of horizontal and vertically oriented charts, all rendering
	 * logic assumes horizontal. This &quot;chart coordinates&quot; space is
	 * transformed to painter coordinates using
	 * {@link WCartesianChart#hv(double x, double y) hv()}.
	 */
	protected void render(final WPainter painter, final WRectF rectangle) {
		painter.save();
		painter.translate(rectangle.getTopLeft());
		if (this.initLayout(rectangle, painter.getDevice())) {
			this.renderBackground(painter);
			this.renderGrid(painter, this.getAxis(Axis.XAxis));
			for (int i = 0; i < this.yAxes_.size(); ++i) {
				this.renderGrid(painter, this.yAxis(i));
			}
			this.renderAxes(painter, EnumSet.of(AxisProperty.Line));
			this.renderSeries(painter);
			this.renderAxes(painter, EnumSet.of(AxisProperty.Labels));
			this.renderBorder(painter);
			this.renderCurveLabels(painter);
			this.renderLegend(painter);
			this.renderOther(painter);
		}
		painter.restore();
	}

	/**
	 * Map (x, y) value pair to chart coordinates coordinates.
	 * <p>
	 * The result needs further transformation using
	 * {@link WCartesianChart#hv(double x, double y) hv()} to painter
	 * coordinates.
	 */
	protected WPointF map(double xValue, double yValue, Axis yAxis,
			int currentXSegment, int currentYSegment) {
		return this.map(xValue, yValue, yAxis == Axis.Y1Axis ? 0 : 1,
				currentXSegment, currentYSegment);
	}

	/**
	 * Map (x, y) value pair to chart coordinates coordinates.
	 * <p>
	 * Returns
	 * {@link #map(double xValue, double yValue, Axis yAxis, int currentXSegment, int currentYSegment)
	 * map(xValue, yValue, Axis.OrdinateAxis, 0, 0)}
	 */
	protected final WPointF map(double xValue, double yValue) {
		return map(xValue, yValue, Axis.OrdinateAxis, 0, 0);
	}

	/**
	 * Map (x, y) value pair to chart coordinates coordinates.
	 * <p>
	 * Returns
	 * {@link #map(double xValue, double yValue, Axis yAxis, int currentXSegment, int currentYSegment)
	 * map(xValue, yValue, yAxis, 0, 0)}
	 */
	protected final WPointF map(double xValue, double yValue, Axis yAxis) {
		return map(xValue, yValue, yAxis, 0, 0);
	}

	/**
	 * Map (x, y) value pair to chart coordinates coordinates.
	 * <p>
	 * Returns
	 * {@link #map(double xValue, double yValue, Axis yAxis, int currentXSegment, int currentYSegment)
	 * map(xValue, yValue, yAxis, currentXSegment, 0)}
	 */
	protected final WPointF map(double xValue, double yValue, Axis yAxis,
			int currentXSegment) {
		return map(xValue, yValue, yAxis, currentXSegment, 0);
	}

	/**
	 * Map (x, y) value pair to chart coordinates coordinates.
	 * <p>
	 * The result needs further transformation using
	 * {@link WCartesianChart#hv(double x, double y) hv()} to painter
	 * coordinates.
	 */
	protected WPointF map(double xValue, double yValue, int yAxis,
			int currentXSegment, int currentYSegment) {
		final WAxis xAx = this.getAxis(Axis.XAxis);
		final WAxis yAx = this.yAxis(yAxis);
		double x = this.chartArea_.getLeft()
				+ xAx.mapToDevice(xValue, currentXSegment);
		double y = this.chartArea_.getBottom()
				- yAx.mapToDevice(yValue, currentYSegment);
		return new WPointF(x, y);
	}

	/**
	 * Map (x, y) value pair to chart coordinates coordinates.
	 * <p>
	 * Returns
	 * {@link #map(double xValue, double yValue, int yAxis, int currentXSegment, int currentYSegment)
	 * map(xValue, yValue, yAxis, 0, 0)}
	 */
	protected final WPointF map(double xValue, double yValue, int yAxis) {
		return map(xValue, yValue, yAxis, 0, 0);
	}

	/**
	 * Map (x, y) value pair to chart coordinates coordinates.
	 * <p>
	 * Returns
	 * {@link #map(double xValue, double yValue, int yAxis, int currentXSegment, int currentYSegment)
	 * map(xValue, yValue, yAxis, currentXSegment, 0)}
	 */
	protected final WPointF map(double xValue, double yValue, int yAxis,
			int currentXSegment) {
		return map(xValue, yValue, yAxis, currentXSegment, 0);
	}

	/**
	 * Utility function for rendering text.
	 * <p>
	 * This method renders text on the chart position <i>pos</i>, with a
	 * particular alignment <i>flags</i>. These are both specified in chart
	 * coordinates. The position is converted to painter coordinates using
	 * {@link WCartesianChart#hv(double x, double y) hv()}, and the alignment
	 * flags are changed accordingly. The rotation, indicated by <i>angle</i> is
	 * specified in painter coordinates and thus an angle of 0 always indicates
	 * horizontal text, regardless of the chart orientation.
	 */
	protected void renderLabel(final WPainter painter, final CharSequence text,
			final WPointF p, EnumSet<AlignmentFlag> flags, double angle,
			int margin) {
		AlignmentFlag horizontalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignHorizontalMask));
		AlignmentFlag verticalAlign = EnumUtils.enumFromSet(EnumUtils.mask(
				flags, AlignmentFlag.AlignVerticalMask));
		AlignmentFlag rHorizontalAlign = horizontalAlign;
		AlignmentFlag rVerticalAlign = verticalAlign;
		double width = 1000;
		double height = 20;
		WPointF pos = this.hv(p);
		if (this.getOrientation() == Orientation.Horizontal) {
			switch (horizontalAlign) {
			case AlignLeft:
				rVerticalAlign = AlignmentFlag.AlignTop;
				break;
			case AlignCenter:
				rVerticalAlign = AlignmentFlag.AlignMiddle;
				break;
			case AlignRight:
				rVerticalAlign = AlignmentFlag.AlignBottom;
				break;
			default:
				break;
			}
			switch (verticalAlign) {
			case AlignTop:
				rHorizontalAlign = AlignmentFlag.AlignRight;
				break;
			case AlignMiddle:
				rHorizontalAlign = AlignmentFlag.AlignCenter;
				break;
			case AlignBottom:
				rHorizontalAlign = AlignmentFlag.AlignLeft;
				break;
			default:
				break;
			}
		}
		double left = 0;
		double top = 0;
		switch (rHorizontalAlign) {
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
		switch (rVerticalAlign) {
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
		WPen oldPen = painter.getPen();
		painter.setPen(this.textPen_.clone());
		WTransform oldTransform = painter.getWorldTransform().clone();
		painter.translate(pos);
		if (angle == 0) {
			painter.drawText(new WRectF(left, top, width, height),
					EnumSet.of(rHorizontalAlign, rVerticalAlign), text);
		} else {
			painter.rotate(-angle);
			painter.drawText(new WRectF(left, top, width, height),
					EnumSet.of(rHorizontalAlign, rVerticalAlign), text);
		}
		painter.setWorldTransform(oldTransform, false);
		painter.setPen(oldPen);
	}

	/**
	 * Conversion between chart and painter coordinates.
	 * <p>
	 * Converts from chart coordinates to painter coordinates, taking into
	 * account the chart orientation.
	 */
	protected WPointF hv(double x, double y) {
		return this.hv(x, y, this.height_);
	}

	/**
	 * Conversion between chart and painter coordinates.
	 * <p>
	 * Converts from chart coordinates to painter coordinates, taking into
	 * account the chart orientation.
	 */
	protected WPointF hv(final WPointF p) {
		if (p.isJavaScriptBound()) {
			if (this.getOrientation() == Orientation.Vertical) {
				return p;
			} else {
				return p.swapHV(this.height_);
			}
		}
		return this.hv(p.getX(), p.getY());
	}

	/**
	 * Conversion between chart and painter coordinates.
	 * <p>
	 * Converts from chart coordinates to painter coordinates, taking into
	 * account the chart orientation.
	 */
	protected WRectF hv(final WRectF r) {
		if (this.getOrientation() == Orientation.Vertical) {
			return r;
		} else {
			WPointF tl = this.hv(r.getBottomLeft());
			return new WRectF(tl.getX(), tl.getY(), r.getHeight(), r.getWidth());
		}
	}

	/**
	 * Returns the segment area for a combination of X and Y segments.
	 * <p>
	 * This segment area is used for clipping when rendering in a particular
	 * segment.
	 */
	protected WRectF chartSegmentArea(final WAxis yAxis, int xSegment,
			int ySegment) {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis.Segment xs = xAxis.segments_.get(xSegment);
		final WAxis.Segment ys = yAxis.segments_.get(ySegment);
		double xRenderStart = xAxis.isInverted() ? xAxis.mapToDevice(
				xs.renderMaximum, xSegment) : xs.renderStart;
		double xRenderEnd = xAxis.isInverted() ? xAxis.mapToDevice(
				xs.renderMinimum, xSegment) : xs.renderStart + xs.renderLength;
		double yRenderStart = yAxis.isInverted() ? yAxis.mapToDevice(
				ys.renderMaximum, ySegment) : ys.renderStart;
		double yRenderEnd = yAxis.isInverted() ? yAxis.mapToDevice(
				ys.renderMinimum, ySegment) : ys.renderStart + ys.renderLength;
		double x1 = this.chartArea_.getLeft()
				+ xRenderStart
				+ (xSegment == 0 ? xs.renderMinimum == 0 ? 0 : -this
						.getAxisPadding() : -xAxis.getSegmentMargin() / 2);
		double x2 = this.chartArea_.getLeft()
				+ xRenderEnd
				+ (xSegment == xAxis.getSegmentCount() - 1 ? xs.renderMaximum == 0 ? 0
						: this.getAxisPadding()
						: xAxis.getSegmentMargin() / 2);
		double y1 = this.chartArea_.getBottom()
				- yRenderEnd
				- (ySegment == yAxis.getSegmentCount() - 1 ? ys.renderMaximum == 0 ? 0
						: this.getAxisPadding()
						: yAxis.getSegmentMargin() / 2);
		double y2 = this.chartArea_.getBottom()
				- yRenderStart
				+ (ySegment == 0 ? ys.renderMinimum == 0 ? 0 : this
						.getAxisPadding() : yAxis.getSegmentMargin() / 2);
		return new WRectF(Math.floor(x1 + 0.5), Math.floor(y1 + 0.5),
				Math.floor(x2 - x1), Math.floor(y2 - y1));
	}

	/**
	 * Calculates the chart area.
	 * <p>
	 * This calculates the chartArea(), which is the rectangle (in chart
	 * coordinates) that bounds the actual chart (thus excluding axes, labels,
	 * titles, legend, etc...).
	 * <p>
	 * 
	 * @see WAbstractChart#getPlotAreaPadding(Side side)
	 */
	protected void calcChartArea() {
		if (this.orientation_ == Orientation.Vertical) {
			this.chartArea_ = new WRectF(this.getPlotAreaPadding(Side.Left),
					this.getPlotAreaPadding(Side.Top), Math.max(10,
							this.width_ - this.getPlotAreaPadding(Side.Left)
									- this.getPlotAreaPadding(Side.Right)),
					Math.max(10,
							this.height_ - this.getPlotAreaPadding(Side.Top)
									- this.getPlotAreaPadding(Side.Bottom)));
		} else {
			this.chartArea_ = new WRectF(this.getPlotAreaPadding(Side.Top),
					this.getPlotAreaPadding(Side.Right), Math.max(10,
							this.width_ - this.getPlotAreaPadding(Side.Top)
									- this.getPlotAreaPadding(Side.Bottom)),
					Math.max(10,
							this.height_ - this.getPlotAreaPadding(Side.Right)
									- this.getPlotAreaPadding(Side.Left)));
		}
	}

	/**
	 * Prepares the axes for rendering.
	 * <p>
	 * Computes axis properties such as the range (if not manually specified),
	 * label interval (if not manually specified) and axis locations. These
	 * properties are stored within the axes.
	 * <p>
	 * 
	 * @see WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 */
	protected boolean isPrepareAxes() {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		Orientation yDir = this.orientation_;
		Orientation xDir = this.orientation_ == Orientation.Vertical ? Orientation.Horizontal
				: Orientation.Vertical;
		if (!xAxis.prepareRender(xDir, this.chartArea_.getWidth())) {
			return false;
		}
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			if (!this.yAxes_.get(i).prepareRender(yDir,
					this.chartArea_.getHeight())) {
				return false;
			}
		}
		if (xAxis.getScale() == AxisScale.CategoryScale) {
			switch (xAxis.getLocation()) {
			case MinimumValue:
			case ZeroValue:
				this.xAxisLocation_ = AxisValue.MinimumValue;
				break;
			case MaximumValue:
				this.xAxisLocation_ = AxisValue.MaximumValue;
				break;
			case BothSides:
				this.xAxisLocation_ = AxisValue.BothSides;
			}
		}
		AxisValue xLocation = this.xAxis_.getLocation();
		if (xLocation == AxisValue.ZeroValue) {
			if (this.yAxes_.get(0).segments_.get(this.yAxes_.get(0).segments_
					.size() - 1).renderMaximum < 0) {
				xLocation = AxisValue.MaximumValue;
			} else {
				if (this.yAxes_.get(0).segments_
						.get(this.yAxes_.get(0).segments_.size() - 1).renderMinimum > 0) {
					xLocation = AxisValue.MinimumValue;
				} else {
					if (!this.yAxes_.get(0).isOnAxis(0.0)) {
						xLocation = AxisValue.MinimumValue;
					}
				}
			}
		} else {
			if (xLocation == AxisValue.MinimumValue) {
				if (this.yAxes_.get(0).segments_.get(0).renderMinimum == 0
						&& this.yAxes_.get(0).getTickDirection() == TickDirection.Outwards) {
					xLocation = AxisValue.ZeroValue;
				}
			} else {
				if (xLocation == AxisValue.MaximumValue) {
					if (this.yAxes_.get(0).segments_
							.get(this.yAxes_.get(0).segments_.size() - 1).renderMaximum == 0) {
						xLocation = AxisValue.ZeroValue;
					}
				}
			}
		}
		this.xAxisLocation_ = xLocation;
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxisLocations_.get(i).loc = this.yAxes_.get(i).getLocation();
		}
		List<WAxis> minimumYaxes = this
				.collectYAxesAtLocation(AxisValue.MinimumValue);
		int offset = 0;
		for (int i = 0; i < minimumYaxes.size(); ++i) {
			final WAxis axis = minimumYaxes.get(i);
			if (axis.getLocation() != AxisValue.BothSides) {
				this.yAxisLocations_.get(axis.getYAxisId()).loc = AxisValue.MinimumValue;
			}
			this.yAxisLocations_.get(axis.getYAxisId()).minOffset = offset;
			if (i == 0 && axis.getTickDirection() == TickDirection.Inwards) {
				offset += 10;
			} else {
				offset += this.calcYAxisSize(axis);
			}
		}
		List<WAxis> maximumYaxes = this
				.collectYAxesAtLocation(AxisValue.MaximumValue);
		offset = 0;
		for (int i = 0; i < maximumYaxes.size(); ++i) {
			final WAxis axis = maximumYaxes.get(i);
			if (axis.getLocation() != AxisValue.BothSides) {
				this.yAxisLocations_.get(axis.getYAxisId()).loc = AxisValue.MaximumValue;
			}
			this.yAxisLocations_.get(axis.getYAxisId()).maxOffset = offset;
			if (i == 0 && axis.getTickDirection() == TickDirection.Inwards) {
				offset += 10;
			} else {
				offset += this.calcYAxisSize(axis);
			}
		}
		if (!minimumYaxes.isEmpty()
				&& minimumYaxes.get(0).getLocation() == AxisValue.MinimumValue
				&& this.xAxis_.segments_.get(0).renderMinimum == 0
				&& minimumYaxes.get(0).getTickDirection() == TickDirection.Outwards) {
			this.yAxisLocations_.get(minimumYaxes.get(0).getYAxisId()).loc = AxisValue.ZeroValue;
		}
		if (!maximumYaxes.isEmpty()
				&& maximumYaxes.get(0).getLocation() == AxisValue.MaximumValue
				&& this.xAxis_.segments_.get(this.xAxis_.segments_.size() - 1).renderMaximum == 0) {
			this.yAxisLocations_.get(maximumYaxes.get(0).getYAxisId()).loc = AxisValue.ZeroValue;
		}
		return true;
	}

	/**
	 * Renders the background.
	 * <p>
	 * 
	 * @see WCartesianChart#render(WPainter painter, WRectF rectangle)
	 */
	protected void renderBackground(final WPainter painter) {
		if (this.getBackground().getStyle() != BrushStyle.NoBrush) {
			painter.fillRect(this.hv(this.chartArea_), this.getBackground());
		}
	}

	/**
	 * Renders one or more properties of the axes.
	 * <p>
	 * This calls
	 * {@link WCartesianChart#renderAxis(WPainter painter, WAxis axis, EnumSet properties)
	 * renderAxis()} for each axis.
	 * <p>
	 * 
	 * @see WCartesianChart#render(WPainter painter, WRectF rectangle)
	 */
	protected void renderAxes(final WPainter painter,
			EnumSet<AxisProperty> properties) {
		this.renderAxis(painter, this.xAxis_, properties);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.renderAxis(painter, this.yAxes_.get(i), properties);
		}
	}

	/**
	 * Renders one or more properties of the axes.
	 * <p>
	 * Calls {@link #renderAxes(WPainter painter, EnumSet properties)
	 * renderAxes(painter, EnumSet.of(propertie, properties))}
	 */
	protected final void renderAxes(final WPainter painter,
			AxisProperty propertie, AxisProperty... properties) {
		renderAxes(painter, EnumSet.of(propertie, properties));
	}

	/**
	 * Renders the border of the chart area.
	 * <p>
	 * 
	 * @see WCartesianChart#render(WPainter painter, WRectF rectangle)
	 * @see WCartesianChart#setBorderPen(WPen pen)
	 */
	protected void renderBorder(final WPainter painter) {
		WPainterPath area = new WPainterPath();
		int horizontalShift = 0;
		int verticalShift = 0;
		if (this.hasInwardsYAxisOnMaximumSide()) {
			horizontalShift = -1;
		}
		if (this.getAxis(Axis.XAxis).isVisible()
				&& this.getAxis(Axis.XAxis).getTickDirection() == TickDirection.Inwards) {
			verticalShift = -1;
		}
		area.addRect(this.hv(new WRectF(this.chartArea_.getLeft(),
				this.chartArea_.getTop(), this.chartArea_.getWidth()
						+ horizontalShift, this.chartArea_.getHeight()
						+ verticalShift)));
		painter.strokePath(area.getCrisp(), this.borderPen_);
	}

	/**
	 * Renders the curve labels.
	 * <p>
	 * 
	 * @see WCartesianChart#render(WPainter painter, WRectF rectangle)
	 * @see WCartesianChart#addCurveLabel(CurveLabel label)
	 */
	protected void renderCurveLabels(final WPainter painter) {
		if (this.isInteractive()) {
			painter.save();
			WPainterPath clipPath = new WPainterPath();
			clipPath.addRect(this.hv(this.chartArea_));
			painter.setClipPath(clipPath);
			painter.setClipping(true);
		}
		for (int i = 0; i < this.curveLabels_.size(); ++i) {
			final CurveLabel label = this.curveLabels_.get(i);
			for (int j = 0; j < this.series_.size(); ++j) {
				final WDataSeries series = this.series_.get(j);
				if (series.isHidden()) {
					continue;
				}
				if (series == label.getSeries()) {
					WTransform t = this.getZoomRangeTransform();
					if (series.getType() == SeriesType.LineSeries
							|| series.getType() == SeriesType.CurveSeries) {
						t.assign(t.multiply(this.curveTransform(series)));
					}
					int xSegment = 0;
					if (!this.isInteractive()) {
						while (xSegment < this.getAxis(Axis.XAxis)
								.getSegmentCount()
								&& (this.getAxis(Axis.XAxis).segments_
										.get(xSegment).renderMinimum > label
										.getPoint().getX() || this
										.getAxis(Axis.XAxis).segments_
										.get(xSegment).renderMaximum < label
										.getPoint().getX())) {
							++xSegment;
						}
					}
					int ySegment = 0;
					if (!this.isInteractive()) {
						while (ySegment < this.getAxis(series.getAxis())
								.getSegmentCount()
								&& (this.getAxis(series.getAxis()).segments_
										.get(ySegment).renderMinimum > label
										.getPoint().getY() || this
										.getAxis(series.getAxis()).segments_
										.get(ySegment).renderMaximum < label
										.getPoint().getY())) {
							++ySegment;
						}
					}
					if (xSegment < this.getAxis(Axis.XAxis).getSegmentCount()
							&& ySegment < this.getAxis(series.getAxis())
									.getSegmentCount()) {
						WPointF devicePoint = this.mapToDeviceWithoutTransform(
								label.getPoint().getX(), label.getPoint()
										.getY(), series.getAxis(), xSegment,
								ySegment);
						WTransform translation = new WTransform().translate(t
								.map(devicePoint));
						painter.save();
						painter.setWorldTransform(translation);
						label.render(painter);
						painter.restore();
					}
				}
			}
		}
		if (this.isInteractive()) {
			painter.restore();
		}
	}

	/**
	 * Renders all series data, including value labels.
	 * <p>
	 * 
	 * @see WCartesianChart#render(WPainter painter, WRectF rectangle)
	 */
	protected void renderSeries(final WPainter painter) {
		if (this.isInteractive()) {
			painter.save();
			WPainterPath clipPath = new WPainterPath();
			clipPath.addRect(this.hv(this.chartArea_));
			painter.setClipPath(clipPath);
			painter.setClipping(true);
		}
		this.barTooltips_.clear();
		{
			SeriesRenderIterator iterator = new SeriesRenderIterator(this,
					painter);
			this.iterateSeries(iterator, painter, true);
		}
		{
			LabelRenderIterator iterator = new LabelRenderIterator(this,
					painter);
			this.iterateSeries(iterator, painter);
		}
		{
			MarkerRenderIterator iterator = new MarkerRenderIterator(this,
					painter);
			this.iterateSeries(iterator, painter);
		}
		if (this.isInteractive()) {
			painter.restore();
		}
	}

	/**
	 * Renders the (default) legend and chart titles.
	 * <p>
	 * 
	 * @see WCartesianChart#render(WPainter painter, WRectF rectangle)
	 */
	protected void renderLegend(final WPainter painter) {
		boolean vertical = this.getOrientation() == Orientation.Vertical;
		int w = vertical ? this.width_ : this.height_;
		int h = vertical ? this.height_ : this.width_;
		int margin;
		if (this.isLegendEnabled()) {
			painter.save();
			WPaintDevice device = painter.getDevice();
			WAxis caxis = null;
			Orientation titleOrientation = Orientation.Horizontal;
			if (this.getLegendSide() == Side.Right) {
				if (this.yAxes_.get(1).isVisible()) {
					caxis = this.yAxes_.get(1);
				} else {
					if (this.yAxes_.get(0).isVisible()
							&& (this.yAxes_.get(0).getLocation() == AxisValue.BothSides || this.yAxes_
									.get(0).getLocation() == AxisValue.MaximumValue)) {
						caxis = this.yAxes_.get(0);
					}
				}
				if (caxis != null
						&& caxis.getTitleOrientation() == Orientation.Vertical) {
					titleOrientation = Orientation.Vertical;
				}
			} else {
				if (this.getLegendSide() == Side.Left) {
					caxis = this.yAxes_.get(0);
					if (caxis.getTitleOrientation() == Orientation.Vertical) {
						titleOrientation = Orientation.Vertical;
					}
				}
			}
			boolean fontMetrics = !EnumUtils.mask(device.getFeatures(),
					WPaintDevice.FeatureFlag.HasFontMetrics).isEmpty();
			if (titleOrientation == Orientation.Vertical && caxis != null) {
				if (fontMetrics) {
					margin = (int) (caxis.calcTitleSize(device,
							Orientation.Vertical) + this.yAxes_.get(1)
							.calcMaxTickLabelSize(device,
									Orientation.Horizontal));
				} else {
					margin = 30;
				}
			} else {
				margin = 20;
			}
			if (caxis != null && titleOrientation == Orientation.Horizontal) {
				if (fontMetrics) {
					margin += caxis.calcMaxTickLabelSize(device,
							Orientation.Horizontal);
				} else {
					margin += 20;
				}
			}
			int numSeriesWithLegend = 0;
			for (int i = 0; i < this.getSeries().size(); ++i) {
				if (this.getSeries().get(i).isLegendEnabled()) {
					++numSeriesWithLegend;
				}
			}
			painter.setFont(this.getLegendFont());
			WFont f = painter.getFont();
			if (this.isAutoLayoutEnabled()
					&& !EnumUtils.mask(painter.getDevice().getFeatures(),
							WPaintDevice.FeatureFlag.HasFontMetrics).isEmpty()) {
				int columnWidth = 0;
				for (int i = 0; i < this.getSeries().size(); ++i) {
					if (this.getSeries().get(i).isLegendEnabled()) {
						WString s = this
								.getSeries()
								.get(i)
								.getModel()
								.getHeaderData(
										this.getSeries().get(i)
												.getModelColumn());
						WTextItem t = painter.getDevice().measureText(s);
						columnWidth = Math.max(columnWidth, (int) t.getWidth());
					}
				}
				columnWidth += 25;
				WCartesianChart self = this;
				self.legend_.setLegendColumnWidth(new WLength(columnWidth));
				if (this.getLegendSide() == Side.Top
						|| this.getLegendSide() == Side.Bottom) {
					self.legend_.setLegendColumns(Math.max(1, w / columnWidth
							- 1));
				}
			}
			int numLegendRows = (numSeriesWithLegend - 1)
					/ this.getLegendColumns() + 1;
			double lineHeight = f.getSizeLength().toPixels() * 1.5;
			int legendWidth = (int) this.getLegendColumnWidth().toPixels()
					* Math.min(this.getLegendColumns(), numSeriesWithLegend);
			int legendHeight = (int) (numLegendRows * lineHeight);
			int x = 0;
			int y = 0;
			switch (this.getLegendSide()) {
			case Left:
				if (this.getLegendLocation() == LegendLocation.LegendInside) {
					x = this.getPlotAreaPadding(Side.Left) + margin;
				} else {
					x = this.getPlotAreaPadding(Side.Left) - margin
							- legendWidth;
				}
				break;
			case Right:
				x = w - this.getPlotAreaPadding(Side.Right);
				if (this.getLegendLocation() == LegendLocation.LegendInside) {
					x -= margin + legendWidth;
				} else {
					x += margin;
				}
				break;
			case Top:
				if (this.getLegendLocation() == LegendLocation.LegendInside) {
					y = this.getPlotAreaPadding(Side.Top) + margin;
				} else {
					y = this.getPlotAreaPadding(Side.Top) - margin
							- legendHeight;
				}
				break;
			case Bottom:
				y = h - this.getPlotAreaPadding(Side.Bottom);
				if (this.getLegendLocation() == LegendLocation.LegendInside) {
					y -= margin + legendHeight;
				} else {
					y += margin;
				}
			default:
				break;
			}
			switch (this.getLegendAlignment()) {
			case AlignTop:
				y = this.getPlotAreaPadding(Side.Top) + margin;
				break;
			case AlignMiddle: {
				double middle = this.getPlotAreaPadding(Side.Top)
						+ (h - this.getPlotAreaPadding(Side.Top) - this
								.getPlotAreaPadding(Side.Bottom)) / 2;
				y = (int) (middle - legendHeight / 2);
			}
				break;
			case AlignBottom:
				y = h - this.getPlotAreaPadding(Side.Bottom) - margin
						- legendHeight;
				break;
			case AlignLeft:
				x = this.getPlotAreaPadding(Side.Left) + margin;
				break;
			case AlignCenter: {
				double center = this.getPlotAreaPadding(Side.Left)
						+ (w - this.getPlotAreaPadding(Side.Left) - this
								.getPlotAreaPadding(Side.Right)) / 2;
				x = (int) (center - legendWidth / 2);
			}
				break;
			case AlignRight:
				x = w - this.getPlotAreaPadding(Side.Right) - margin
						- legendWidth;
				break;
			default:
				break;
			}
			if (this.getLegendLocation() == LegendLocation.LegendOutside) {
				if (this.getLegendSide() == Side.Top && !vertical
						&& this.getAxis(Axis.Y1Axis).isVisible()) {
					y -= 16;
				}
				if (this.getLegendSide() == Side.Right
						&& vertical
						&& (this.getAxis(Axis.Y2Axis).isVisible() || this
								.getAxis(Axis.Y1Axis).isVisible()
								&& (this.getAxis(Axis.Y1Axis).getLocation() == AxisValue.BothSides || this
										.getAxis(Axis.Y1Axis).getLocation() == AxisValue.MaximumValue))) {
					x += 40;
				}
				if (this.getLegendSide() == Side.Right
						&& !vertical
						&& this.getAxis(Axis.XAxis).isVisible()
						&& (this.getAxis(Axis.XAxis).getLocation() == AxisValue.MaximumValue || this
								.getAxis(Axis.XAxis).getLocation() == AxisValue.BothSides)) {
					x += 40;
				}
				if (this.getLegendSide() == Side.Bottom
						&& (vertical && this.getAxis(Axis.XAxis).isVisible() || !vertical
								&& (this.getAxis(Axis.Y2Axis).isVisible() || this
										.getAxis(Axis.Y1Axis).isVisible()
										&& (this.getAxis(Axis.Y1Axis)
												.getLocation() == AxisValue.BothSides || this
												.getAxis(Axis.Y1Axis)
												.getLocation() == AxisValue.MaximumValue)))) {
					y += 16;
				}
				if (this.getLegendSide() == Side.Left
						&& (vertical && this.getAxis(Axis.Y1Axis).isVisible() || !vertical
								&& this.getAxis(Axis.XAxis).isVisible())) {
					x -= 40;
				}
			}
			painter.setPen(this.getLegendBorder().clone());
			painter.setBrush(this.getLegendBackground());
			painter.drawRect(x - margin / 2, y - margin / 2, legendWidth
					+ margin, legendHeight + margin);
			painter.setPen(new WPen());
			painter.setFont(this.getLegendFont());
			int item = 0;
			for (int i = 0; i < this.getSeries().size(); ++i) {
				if (this.getSeries().get(i).isLegendEnabled()) {
					int col = item % this.getLegendColumns();
					int row = item / this.getLegendColumns();
					double itemX = x + col
							* this.getLegendColumnWidth().toPixels();
					double itemY = y + row * lineHeight;
					this.renderLegendItem(painter, new WPointF(itemX, itemY
							+ lineHeight / 2), this.getSeries().get(i));
					++item;
				}
			}
			painter.restore();
		}
		if (!(this.getTitle().length() == 0)) {
			int x = this.getPlotAreaPadding(Side.Left)
					+ (w - this.getPlotAreaPadding(Side.Left) - this
							.getPlotAreaPadding(Side.Right)) / 2;
			painter.save();
			painter.setFont(this.getTitleFont());
			double titleHeight = this.getTitleFont().getSizeLength().toPixels();
			final int TITLE_PADDING = 10;
			painter.drawText(x - 500, this.getPlotAreaPadding(Side.Top)
					- titleHeight - TITLE_PADDING, 1000, titleHeight, EnumSet
					.of(AlignmentFlag.AlignCenter, AlignmentFlag.AlignTop),
					this.getTitle());
			painter.restore();
		}
	}

	/**
	 * Renders properties of one axis.
	 * <p>
	 * 
	 * @see WCartesianChart#renderAxes(WPainter painter, EnumSet properties)
	 */
	protected void renderAxis(final WPainter painter, final WAxis axis,
			EnumSet<AxisProperty> properties) {
		if (!axis.isVisible()) {
			return;
		}
		boolean isYAxis = axis.getId() != Axis.XAxis;
		final AxisValue location = axis.getId() == Axis.XAxis ? this.xAxisLocation_
				: this.yAxisLocations_.get(axis.getYAxisId()).loc;
		if (this.isInteractive()
				&& ((painter.getDevice()) instanceof WCanvasPaintDevice ? (WCanvasPaintDevice) (painter
						.getDevice()) : null) != null) {
			WRectF clipRect = null;
			WRectF area = this.hv(this.chartArea_);
			if (axis.getLocation() == AxisValue.ZeroValue
					&& location == AxisValue.ZeroValue) {
				clipRect = area;
			} else {
				if (isYAxis != (this.getOrientation() == Orientation.Horizontal)) {
					double h = area.getHeight();
					if (this.xAxisLocation_ == AxisValue.ZeroValue
							&& this.getOrientation() == Orientation.Vertical) {
						h += 1;
					}
					clipRect = new WRectF(0.0, area.getTop(),
							isYAxis ? this.width_ : this.height_, h);
				} else {
					clipRect = new WRectF(area.getLeft(), 0.0, area.getWidth(),
							isYAxis ? this.height_ : this.width_);
				}
			}
			if (properties.equals(AxisProperty.Labels)) {
				clipRect = new WRectF(clipRect.getLeft() - 1,
						clipRect.getTop() - 1, clipRect.getWidth() + 2,
						clipRect.getHeight() + 2);
			}
			WPainterPath clipPath = new WPainterPath();
			clipPath.addRect(clipRect);
			painter.save();
			painter.setClipPath(clipPath);
			painter.setClipping(true);
		}
		List<AxisValue> locations = new ArrayList<AxisValue>();
		if (location == AxisValue.BothSides) {
			locations.add(AxisValue.MinimumValue);
			locations.add(AxisValue.MaximumValue);
		} else {
			locations.add(location);
		}
		for (int l = 0; l < locations.size(); ++l) {
			WPointF axisStart = new WPointF();
			WPointF axisEnd = new WPointF();
			double tickStart = 0.0;
			double tickEnd = 0.0;
			double labelPos = 0.0;
			AlignmentFlag labelHFlag = AlignmentFlag.AlignCenter;
			AlignmentFlag labelVFlag = AlignmentFlag.AlignMiddle;
			if (isYAxis) {
				labelVFlag = AlignmentFlag.AlignMiddle;
				axisStart.setY(this.chartArea_.getBottom());
				axisEnd.setY(this.chartArea_.getTop());
			} else {
				labelHFlag = AlignmentFlag.AlignCenter;
				axisStart.setX(this.chartArea_.getLeft());
				axisEnd.setX(this.chartArea_.getRight());
			}
			switch (locations.get(l)) {
			case MinimumValue:
				if (isYAxis) {
					double x = this.chartArea_.getLeft()
							- this.yAxisLocations_.get(axis.getYAxisId()).minOffset;
					if (axis.getTickDirection() == TickDirection.Inwards) {
						tickStart = 0;
						tickEnd = TICK_LENGTH;
						labelPos = TICK_LENGTH;
						labelHFlag = AlignmentFlag.AlignLeft;
						axisStart.setX(x);
						axisEnd.setX(x);
					} else {
						tickStart = -TICK_LENGTH;
						tickEnd = 0;
						labelPos = -TICK_LENGTH;
						labelHFlag = AlignmentFlag.AlignRight;
						x -= axis.getMargin();
						axisStart.setX(x);
						axisEnd.setX(x);
					}
				} else {
					if (axis.getTickDirection() == TickDirection.Inwards) {
						tickStart = -TICK_LENGTH;
						tickEnd = 0;
						labelPos = -TICK_LENGTH;
						labelVFlag = AlignmentFlag.AlignBottom;
						double y = this.chartArea_.getBottom() - 1;
						axisStart.setY(y);
						axisEnd.setY(y);
					} else {
						tickStart = 0;
						tickEnd = TICK_LENGTH;
						labelPos = TICK_LENGTH;
						labelVFlag = AlignmentFlag.AlignTop;
						double y = this.chartArea_.getBottom()
								+ axis.getMargin();
						axisStart.setY(y);
						axisEnd.setY(y);
					}
				}
				break;
			case MaximumValue:
				if (isYAxis) {
					double x = this.chartArea_.getRight()
							+ this.yAxisLocations_.get(axis.getYAxisId()).maxOffset;
					if (axis.getTickDirection() == TickDirection.Inwards) {
						tickStart = -TICK_LENGTH;
						tickEnd = 0;
						labelPos = -TICK_LENGTH;
						labelHFlag = AlignmentFlag.AlignRight;
						x -= 1;
						axisStart.setX(x);
						axisEnd.setX(x);
					} else {
						tickStart = 0;
						tickEnd = TICK_LENGTH;
						labelPos = TICK_LENGTH;
						labelHFlag = AlignmentFlag.AlignLeft;
						x += axis.getMargin();
						axisStart.setX(x);
						axisEnd.setX(x);
					}
				} else {
					if (axis.getTickDirection() == TickDirection.Inwards) {
						tickStart = 0;
						tickEnd = TICK_LENGTH;
						labelPos = TICK_LENGTH;
						labelVFlag = AlignmentFlag.AlignTop;
						double y = this.chartArea_.getTop();
						axisStart.setY(y);
						axisEnd.setY(y);
					} else {
						tickStart = -TICK_LENGTH;
						tickEnd = 0;
						labelPos = -TICK_LENGTH;
						labelVFlag = AlignmentFlag.AlignBottom;
						double y = this.chartArea_.getTop() - axis.getMargin();
						axisStart.setY(y);
						axisEnd.setY(y);
					}
				}
				break;
			case ZeroValue:
				tickStart = -TICK_LENGTH;
				tickEnd = TICK_LENGTH;
				if (isYAxis) {
					double x = this.chartArea_.getLeft()
							+ this.getAxis(Axis.XAxis).mapToDevice(0.0);
					axisStart.setX(x);
					axisEnd.setX(x);
					labelHFlag = AlignmentFlag.AlignRight;
					if (this.getType() == ChartType.CategoryChart) {
						labelPos = this.chartArea_.getLeft() - axisStart.getX()
								- TICK_LENGTH;
					} else {
						labelPos = -TICK_LENGTH;
					}
				} else {
					double y = this.chartArea_.getBottom()
							- this.getAxis(Axis.YAxis).mapToDevice(0.0);
					axisStart.setY(y);
					axisEnd.setY(y);
					labelVFlag = AlignmentFlag.AlignTop;
					if (this.getType() == ChartType.CategoryChart) {
						labelPos = this.chartArea_.getBottom()
								- axisStart.getY() + TICK_LENGTH;
					} else {
						labelPos = TICK_LENGTH;
					}
				}
				break;
			case BothSides:
				assert false;
				break;
			}
			if (!EnumUtils.mask(properties, AxisProperty.Labels).isEmpty()
					&& !(axis.getTitle().length() == 0)) {
				if (this.isInteractive()) {
					painter.setClipping(false);
				}
				WFont oldFont2 = painter.getFont();
				WFont titleFont = axis.getTitleFont();
				painter.setFont(titleFont);
				boolean chartVertical = this.getOrientation() == Orientation.Vertical;
				if (isYAxis) {
					double u = axisStart.getX();
					if (chartVertical) {
						if (axis.getTitleOrientation() == Orientation.Horizontal) {
							this.renderLabel(
									painter,
									axis.getTitle(),
									new WPointF(
											u
													+ (labelHFlag == AlignmentFlag.AlignRight ? 15
															: -15),
											this.chartArea_.getTop() - 8),
									EnumSet.of(labelHFlag,
											AlignmentFlag.AlignBottom), 0, 10);
						} else {
							WPaintDevice device = painter.getDevice();
							double size = 0;
							double titleSizeW = 0;
							if (!EnumUtils.mask(device.getFeatures(),
									WPaintDevice.FeatureFlag.HasFontMetrics)
									.isEmpty()) {
								if (axis.getTickDirection() == TickDirection.Outwards) {
									size = axis.calcMaxTickLabelSize(device,
											Orientation.Horizontal);
								}
								titleSizeW = axis.calcTitleSize(device,
										Orientation.Vertical);
								if (axis.getTickDirection() == TickDirection.Inwards) {
									titleSizeW = -titleSizeW;
								}
							} else {
								size = 35;
								if (axis.getTickDirection() == TickDirection.Inwards) {
									size = -20;
								}
							}
							this.renderLabel(
									painter,
									axis.getTitle(),
									new WPointF(
											u
													+ (labelHFlag == AlignmentFlag.AlignRight ? -(size
															+ titleSizeW + 5)
															: +(size
																	+ titleSizeW + 5)),
											this.chartArea_.getCenter().getY()),
									EnumSet.of(AlignmentFlag.AlignCenter,
											AlignmentFlag.AlignMiddle),
									locations.get(l) == AxisValue.MaximumValue ? -90
											: 90, 10);
						}
					} else {
						double extraMargin = 0;
						WPaintDevice device = painter.getDevice();
						if (axis.getTickDirection() == TickDirection.Outwards) {
							extraMargin = axis.calcMaxTickLabelSize(device,
									Orientation.Vertical);
						}
						if (locations.get(l) != AxisValue.MaximumValue) {
							extraMargin = -extraMargin;
						}
						EnumSet<AlignmentFlag> alignment = EnumSet
								.of(locations.get(l) == AxisValue.MaximumValue ? AlignmentFlag.AlignLeft
										: AlignmentFlag.AlignRight,
										AlignmentFlag.AlignMiddle);
						this.renderLabel(painter, axis.getTitle(), new WPointF(
								u + extraMargin, this.chartArea_.getCenter()
										.getY()), alignment, 0, 10);
					}
				} else {
					double u = axisStart.getY();
					if (chartVertical) {
						double extraMargin = 0;
						WPaintDevice device = painter.getDevice();
						if (!EnumUtils.mask(device.getFeatures(),
								WPaintDevice.FeatureFlag.HasFontMetrics)
								.isEmpty()) {
							if (axis.getTickDirection() == TickDirection.Outwards) {
								extraMargin = axis.calcMaxTickLabelSize(device,
										Orientation.Vertical);
							}
						} else {
							if (axis.getTickDirection() == TickDirection.Outwards) {
								extraMargin = 15;
							}
						}
						if (locations.get(l) == AxisValue.MaximumValue) {
							extraMargin = -extraMargin;
						}
						EnumSet<AlignmentFlag> alignment = EnumSet
								.of(locations.get(l) == AxisValue.MaximumValue ? AlignmentFlag.AlignBottom
										: AlignmentFlag.AlignTop,
										AlignmentFlag.AlignCenter);
						this.renderLabel(painter, axis.getTitle(), new WPointF(
								this.chartArea_.getCenter().getX(), u
										+ extraMargin), alignment, 0, 10);
					} else {
						if (axis.getTitleOrientation() == Orientation.Vertical) {
							WPaintDevice device = painter.getDevice();
							double extraMargin = 0;
							if (!EnumUtils.mask(device.getFeatures(),
									WPaintDevice.FeatureFlag.HasFontMetrics)
									.isEmpty()) {
								if (axis.getTickDirection() == TickDirection.Outwards) {
									extraMargin = axis.calcMaxTickLabelSize(
											device, Orientation.Horizontal);
								}
								extraMargin += axis.calcTitleSize(device,
										Orientation.Vertical);
							} else {
								extraMargin = 40;
							}
							if (locations.get(l) == AxisValue.MaximumValue) {
								extraMargin = -extraMargin;
							}
							this.renderLabel(
									painter,
									axis.getTitle(),
									new WPointF(this.chartArea_.getCenter()
											.getX(), u + extraMargin),
									EnumSet.of(AlignmentFlag.AlignMiddle,
											AlignmentFlag.AlignCenter),
									locations.get(l) == AxisValue.MaximumValue ? -90
											: 90, 10);
						} else {
							EnumSet<AlignmentFlag> alignment = EnumSet
									.of(locations.get(l) == AxisValue.MaximumValue ? AlignmentFlag.AlignBottom
											: AlignmentFlag.AlignTop,
											AlignmentFlag.AlignLeft);
							this.renderLabel(painter, axis.getTitle(),
									new WPointF(this.chartArea_.getRight(), u),
									alignment, 0, 8);
						}
					}
				}
				painter.setFont(oldFont2);
				if (this.isInteractive()) {
					painter.setClipping(true);
				}
			}
			final double ANGLE1 = 15;
			final double ANGLE2 = 80;
			if (isYAxis) {
				if (axis.getLabelAngle() > ANGLE1) {
					labelVFlag = labelPos < 0 ? AlignmentFlag.AlignBottom
							: AlignmentFlag.AlignTop;
					if (axis.getLabelAngle() > ANGLE2) {
						labelHFlag = AlignmentFlag.AlignCenter;
					}
				} else {
					if (axis.getLabelAngle() < -ANGLE1) {
						labelVFlag = labelPos < 0 ? AlignmentFlag.AlignTop
								: AlignmentFlag.AlignBottom;
						if (axis.getLabelAngle() < -ANGLE2) {
							labelHFlag = AlignmentFlag.AlignCenter;
						}
					}
				}
			} else {
				if (axis.getLabelAngle() > ANGLE1) {
					labelHFlag = labelPos > 0 ? AlignmentFlag.AlignRight
							: AlignmentFlag.AlignLeft;
					if (axis.getLabelAngle() > ANGLE2) {
						labelVFlag = AlignmentFlag.AlignMiddle;
					}
				} else {
					if (axis.getLabelAngle() < -ANGLE1) {
						labelHFlag = labelPos > 0 ? AlignmentFlag.AlignLeft
								: AlignmentFlag.AlignRight;
						if (axis.getLabelAngle() < -ANGLE2) {
							labelVFlag = AlignmentFlag.AlignMiddle;
						}
					}
				}
			}
			if (this.getOrientation() == Orientation.Horizontal) {
				axisStart = this.hv(axisStart);
				axisEnd = this.hv(axisEnd);
				AlignmentFlag rHFlag = AlignmentFlag.AlignCenter;
				AlignmentFlag rVFlag = AlignmentFlag.AlignMiddle;
				switch (labelHFlag) {
				case AlignLeft:
					rVFlag = AlignmentFlag.AlignTop;
					break;
				case AlignCenter:
					rVFlag = AlignmentFlag.AlignMiddle;
					break;
				case AlignRight:
					rVFlag = AlignmentFlag.AlignBottom;
					break;
				default:
					break;
				}
				switch (labelVFlag) {
				case AlignTop:
					rHFlag = AlignmentFlag.AlignRight;
					break;
				case AlignMiddle:
					rHFlag = AlignmentFlag.AlignCenter;
					break;
				case AlignBottom:
					rHFlag = AlignmentFlag.AlignLeft;
					break;
				default:
					break;
				}
				labelHFlag = rHFlag;
				labelVFlag = rVFlag;
				boolean invertTicks = !isYAxis;
				if (invertTicks) {
					tickStart = -tickStart;
					tickEnd = -tickEnd;
					labelPos = -labelPos;
				}
			}
			List<WPen> pens = new ArrayList<WPen>();
			List<WPen> textPens = new ArrayList<WPen>();
			if (this.isInteractive()
					&& (axis.getId() == Axis.XAxis || axis.getId() == Axis.YAxis)) {
				List<WCartesianChart.PenAssignment> it = this.pens_.get(axis
						.getId());
				if (it != null) {
					final List<WCartesianChart.PenAssignment> ass = it;
					for (int i = 0; i < ass.size(); ++i) {
						pens.add(ass.get(i).pen.getValue());
						textPens.add(ass.get(i).textPen.getValue());
					}
				}
			}
			WTransform transform = new WTransform();
			WRectF area = this.hv(this.chartArea_);
			if (axis.getLocation() == AxisValue.ZeroValue) {
				transform.assign(new WTransform(1, 0, 0, -1, area.getLeft(),
						area.getBottom())
						.multiply(this.xTransform_)
						.multiply(this.yTransform_)
						.multiply(
								new WTransform(1, 0, 0, -1, -area.getLeft(),
										area.getBottom())));
			} else {
				if (isYAxis && this.getOrientation() == Orientation.Vertical) {
					transform.assign(new WTransform(1, 0, 0, -1, 0, area
							.getBottom()).multiply(this.yTransform_).multiply(
							new WTransform(1, 0, 0, -1, 0, area.getBottom())));
				} else {
					if (isYAxis
							&& this.getOrientation() == Orientation.Horizontal) {
						transform.assign(new WTransform(0, 1, 1, 0, area
								.getLeft(), 0).multiply(this.yTransform_)
								.multiply(
										new WTransform(0, 1, 1, 0, 0, -area
												.getLeft())));
					} else {
						if (this.getOrientation() == Orientation.Horizontal) {
							transform.assign(new WTransform(0, 1, 1, 0, 0, area
									.getTop()).multiply(this.xTransform_)
									.multiply(
											new WTransform(0, 1, 1, 0, -area
													.getTop(), 0)));
						} else {
							transform.assign(new WTransform(1, 0, 0, 1, area
									.getLeft(), 0).multiply(this.xTransform_)
									.multiply(
											new WTransform(1, 0, 0, 1, -area
													.getLeft(), 0)));
						}
					}
				}
			}
			AxisValue side = location == AxisValue.BothSides ? locations.get(l)
					: axis.getLocation();
			axis.render(painter, properties, axisStart, axisEnd, tickStart,
					tickEnd, labelPos, EnumSet.of(labelHFlag, labelVFlag),
					transform, side, pens, textPens);
		}
		if (this.isInteractive()) {
			painter.restore();
		}
	}

	/**
	 * Renders properties of one axis.
	 * <p>
	 * Calls {@link #renderAxis(WPainter painter, WAxis axis, EnumSet properties)
	 * renderAxis(painter, axis, EnumSet.of(propertie, properties))}
	 */
	protected final void renderAxis(final WPainter painter, final WAxis axis,
			AxisProperty propertie, AxisProperty... properties) {
		renderAxis(painter, axis, EnumSet.of(propertie, properties));
	}

	/**
	 * Renders grid lines along the ticks of the given axis.
	 * <p>
	 * 
	 * @see WCartesianChart#render(WPainter painter, WRectF rectangle)
	 */
	protected void renderGrid(final WPainter painter, final WAxis ax) {
		if (!ax.isGridLinesEnabled()) {
			return;
		}
		boolean vertical = ax.getId() != Axis.XAxis;
		final WAxis other = vertical ? this.getAxis(Axis.XAxis) : this
				.getAxis(Axis.Y1Axis);
		final WAxis.Segment s0 = other.segments_.get(0);
		final WAxis.Segment sn = other.segments_
				.get(other.segments_.size() - 1);
		double ou0 = s0.renderStart;
		double oun = sn.renderStart + sn.renderLength;
		if (!vertical && this.getAxis(Axis.Y2Axis).isGridLinesEnabled()) {
			final WAxis other2 = this.getAxis(Axis.Y2Axis);
			final WAxis.Segment s0_2 = other2.segments_.get(0);
			final WAxis.Segment sn_2 = other2.segments_.get(other2.segments_
					.size() - 1);
			if (!this.getAxis(Axis.YAxis).isGridLinesEnabled()
					|| s0_2.renderStart < ou0) {
				ou0 = s0_2.renderStart;
			}
			if (!this.getAxis(Axis.YAxis).isGridLinesEnabled()
					|| sn_2.renderStart + sn_2.renderLength > oun) {
				oun = sn_2.renderStart + sn_2.renderLength;
			}
		}
		boolean otherVertical = !vertical;
		if (otherVertical) {
			ou0 = this.chartArea_.getBottom() - ou0;
			oun = this.chartArea_.getBottom() - oun;
		} else {
			ou0 = this.chartArea_.getLeft() + ou0;
			oun = this.chartArea_.getLeft() + oun;
		}
		if (this.isInteractive()) {
			painter.save();
			WPainterPath clipPath = new WPainterPath();
			clipPath.addRect(this.hv(this.chartArea_));
			painter.setClipPath(clipPath);
			painter.setClipping(true);
		}
		List<WPen> pens = new ArrayList<WPen>();
		if (this.pens_.get(ax.getId()) == null) {
			pens.add(ax.getGridLinesPen());
		} else {
			final List<WCartesianChart.PenAssignment> assignments = this.pens_
					.get(ax.getId());
			for (int i = 0; i < assignments.size(); ++i) {
				pens.add(assignments.get(i).gridPen.getValue());
			}
		}
		AxisConfig axisConfig = new AxisConfig();
		if (ax.getLocation() == AxisValue.BothSides) {
			axisConfig.side = AxisValue.MinimumValue;
		} else {
			axisConfig.side = ax.getLocation();
		}
		for (int level = 1; level <= pens.size(); ++level) {
			WPainterPath gridPath = new WPainterPath();
			axisConfig.zoomLevel = level;
			List<Double> gridPos = ax.gridLinePositions(axisConfig);
			for (int i = 0; i < gridPos.size(); ++i) {
				double u = gridPos.get(i);
				if (vertical) {
					u = this.chartArea_.getBottom() - u;
					gridPath.moveTo(this.hv(ou0, u));
					gridPath.lineTo(this.hv(oun, u));
				} else {
					u = this.chartArea_.getLeft() + u;
					gridPath.moveTo(this.hv(u, ou0));
					gridPath.lineTo(this.hv(u, oun));
				}
			}
			painter.strokePath(this.getZoomRangeTransform().map(gridPath)
					.getCrisp(), pens.get(level - 1));
		}
		if (this.isInteractive()) {
			painter.restore();
		}
	}

	/**
	 * Renders other, user-defined things.
	 * <p>
	 * The default implementation sets the painter&apos;s
	 * {@link WPainter#setClipPath(WPainterPath clipPath) clip path} to the
	 * chart area, but does not enable clipping.
	 * <p>
	 * This method can be overridden to draw extra content onto the chart.
	 * <p>
	 * {@link } coordinates can be mapped to device coordinates with
	 * {@link WCartesianChart#mapToDeviceWithoutTransform(Object xValue, Object yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 * mapToDeviceWithoutTransform()}. If these need to move and scale along
	 * with the zoom range, those points can be transformed with
	 * {@link WCartesianChart#getZoomRangeTransform() getZoomRangeTransform()}.
	 * <p>
	 * This method is called last by default. If you want to render other things
	 * at some other moment, you can override render(WPainter&amp;, const
	 * WRectF&amp;).
	 */
	protected void renderOther(final WPainter painter) {
		WPainterPath clipPath = new WPainterPath();
		clipPath.addRect(this.chartArea_);
		painter.setClipPath(clipPath);
	}

	/**
	 * Calculates the total number of bar groups.
	 */
	protected int getCalcNumBarGroups() {
		int numBarGroups = 0;
		boolean newGroup = true;
		for (int i = 0; i < this.getSeries().size(); ++i) {
			if (this.getSeries().get(i).getType() == SeriesType.BarSeries) {
				if (newGroup || !this.getSeries().get(i).isStacked()) {
					++numBarGroups;
				}
				newGroup = false;
			} else {
				newGroup = true;
			}
		}
		return numBarGroups;
	}

	protected void render(EnumSet<RenderFlag> flags) {
		super.render(flags);
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()
				|| !this.jsDefined_) {
			this.defineJavaScript();
		}
	}

	protected void setFormData(final WObject.FormData formData) {
		super.setFormData(formData);
		final WTransform xTransform = this.xTransformHandle_.getValue();
		final WTransform yTransform = this.yTransformHandle_.getValue();
		WPointF devicePan = new WPointF(xTransform.getDx()
				/ xTransform.getM11(), yTransform.getDy() / yTransform.getM22());
		WPointF modelPan = new WPointF(this.getAxis(Axis.XAxis).mapFromDevice(
				-devicePan.getX()), this.getAxis(Axis.YAxis).mapFromDevice(
				-devicePan.getY()));
		if (!this.getAxis(Axis.XAxis).zoomRangeDirty_) {
			if (xTransform.isIdentity()) {
				this.getAxis(Axis.XAxis).setZoomRangeFromClient(
						WAxis.AUTO_MINIMUM, WAxis.AUTO_MAXIMUM);
			} else {
				double z = xTransform.getM11();
				double x = modelPan.getX();
				double min = this.getAxis(Axis.XAxis).mapFromDevice(0.0);
				double max = this.getAxis(Axis.XAxis).mapFromDevice(
						this.getAxis(Axis.XAxis).fullRenderLength_);
				double x2 = x + (max - min) / z;
				this.getAxis(Axis.XAxis).setZoomRangeFromClient(x, x2);
			}
		}
		if (!this.getAxis(Axis.YAxis).zoomRangeDirty_) {
			if (yTransform.isIdentity()) {
				this.getAxis(Axis.YAxis).setZoomRangeFromClient(
						WAxis.AUTO_MINIMUM, WAxis.AUTO_MAXIMUM);
			} else {
				double z = yTransform.getM22();
				double y = modelPan.getY();
				double min = this.getAxis(Axis.YAxis).mapFromDevice(0.0);
				double max = this.getAxis(Axis.YAxis).mapFromDevice(
						this.getAxis(Axis.YAxis).fullRenderLength_);
				double y2 = y + (max - min) / z;
				this.getAxis(Axis.YAxis).setZoomRangeFromClient(y, y2);
			}
		}
		if (this.curveTransforms_.size() != 0) {
			for (int i = 0; i < this.series_.size(); ++i) {
				final WDataSeries s = this.series_.get(i);
				if ((s.getType() == SeriesType.LineSeries || s.getType() == SeriesType.CurveSeries)
						&& !s.isHidden()) {
					if (!s.scaleDirty_) {
						s.scale_ = this.curveTransforms_.get(s).getValue()
								.getM22();
					}
					if (!s.offsetDirty_) {
						int yAxis = s.getYAxis();
						double origin;
						if (this.getOrientation() == Orientation.Horizontal) {
							origin = this.mapToDeviceWithoutTransform(0.0, 0.0,
									yAxis).getX();
						} else {
							origin = this.mapToDeviceWithoutTransform(0.0, 0.0,
									yAxis).getY();
						}
						double dy = this.curveTransforms_.get(s).getValue()
								.getDy();
						double scale = this.curveTransforms_.get(s).getValue()
								.getM22();
						double offset = -dy + origin * (1 - scale)
								+ this.yAxis(yAxis).mapToDevice(0.0, 0);
						if (this.getOrientation() == Orientation.Horizontal) {
							s.offset_ = -this.yAxis(yAxis)
									.mapFromDevice(offset);
						} else {
							s.offset_ = this.yAxis(yAxis).mapFromDevice(offset);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the current zoom range transform.
	 * <p>
	 * This transform maps device coordinates from the fully zoomed out position
	 * to the current zoom range.
	 * <p>
	 * This transform is a
	 * {@link WJavaScriptExposableObject#isJavaScriptBound() JavaScript bound}
	 * transform if this chart is interactive. Otherwise, this transform is just
	 * the identity transform.
	 * <p>
	 * 
	 * @see WCartesianChart#setZoomEnabled(boolean zoomEnabled)
	 * @see WCartesianChart#setPanEnabled(boolean panEnabled)
	 * @see WAxis#setZoomRange(double minimum, double maximum)
	 */
	protected WTransform getZoomRangeTransform() {
		return this.zoomRangeTransform(this.xTransform_, this.yTransform_);
	}

	private List<WAxis> collectYAxesAtLocation(AxisValue side) {
		List<WAxis> result = new ArrayList<WAxis>();
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			final WAxis axis = this.yAxes_.get(i);
			if (!axis.isVisible()) {
				continue;
			}
			if (axis.getLocation() == AxisValue.ZeroValue) {
				if (side == AxisValue.MinimumValue) {
					if (this.xAxis_.segments_.get(0).renderMinimum >= 0
							|| !this.xAxis_.isOnAxis(0.0)
							&& this.xAxis_.segments_.get(this.xAxis_.segments_
									.size() - 1).renderMaximum > 0) {
						result.add(axis);
					}
				} else {
					if (side == AxisValue.MaximumValue) {
						if (this.xAxis_.segments_.get(this.xAxis_.segments_
								.size() - 1).renderMaximum <= 0) {
							result.add(axis);
						}
					}
				}
			}
		}
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			final WAxis axis = this.yAxes_.get(i);
			if (!axis.isVisible()) {
				continue;
			}
			if (axis.getLocation() == side
					|| axis.getLocation() == AxisValue.BothSides) {
				result.add(axis);
			}
		}
		return result;
	}

	private int getSeriesIndexOf(int modelColumn) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() == modelColumn) {
				return i;
			}
		}
		return -1;
	}

	int getSeriesIndexOf(final WDataSeries series) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i) == series) {
				return i;
			}
		}
		return -1;
	}

	private boolean hasInwardsYAxisOnMaximumSide() {
		List<WAxis> maximumYaxes = this
				.collectYAxesAtLocation(AxisValue.MaximumValue);
		return !maximumYaxes.isEmpty()
				&& maximumYaxes.get(0).getTickDirection() == TickDirection.Inwards;
	}

	private void clearPens() {
		for (Iterator<Map.Entry<Axis, List<WCartesianChart.PenAssignment>>> it_it = this.pens_
				.entrySet().iterator(); it_it.hasNext();) {
			Map.Entry<Axis, List<WCartesianChart.PenAssignment>> it = it_it
					.next();
			final List<WCartesianChart.PenAssignment> assignments = it
					.getValue();
			for (int i = 0; i < assignments.size(); ++i) {
				final WCartesianChart.PenAssignment assignment = assignments
						.get(i);
				this.freePens_.add(assignment.pen);
				this.freePens_.add(assignment.textPen);
				this.freePens_.add(assignment.gridPen);
			}
		}
		this.pens_.clear();
	}

	private void createPensForAxis(Axis ax) {
		if (!this.getAxis(ax).isVisible()
				|| this.getAxis(ax).getScale() == AxisScale.LogScale) {
			return;
		}
		double zoom = this.getAxis(ax).getZoom();
		if (zoom > this.getAxis(ax).getMaxZoom()) {
			zoom = this.getAxis(ax).getMaxZoom();
		}
		int level = toZoomLevel(zoom);
		List<WCartesianChart.PenAssignment> assignments = new ArrayList<WCartesianChart.PenAssignment>();
		for (int i = 1;; ++i) {
			double z = Math.pow(2.0, i - 1);
			if (z > this.getAxis(ax).getMaxZoom()) {
				break;
			}
			WJavaScriptHandle<WPen> pen = null;
			if (this.freePens_.size() > 0) {
				pen = this.freePens_.get(this.freePens_.size() - 1);
				this.freePens_.remove(this.freePens_.size() - 1);
			} else {
				pen = this.createJSPen();
			}
			WPen p = this.getAxis(ax).getPen().clone();
			p.setColor(new WColor(p.getColor().getRed(), p.getColor()
					.getGreen(), p.getColor().getBlue(), i == level ? p
					.getColor().getAlpha() : 0));
			pen.setValue(p);
			WJavaScriptHandle<WPen> textPen = null;
			if (this.freePens_.size() > 0) {
				textPen = this.freePens_.get(this.freePens_.size() - 1);
				this.freePens_.remove(this.freePens_.size() - 1);
			} else {
				textPen = this.createJSPen();
			}
			p = this.getAxis(ax).getTextPen().clone();
			p.setColor(new WColor(p.getColor().getRed(), p.getColor()
					.getGreen(), p.getColor().getBlue(), i == level ? p
					.getColor().getAlpha() : 0));
			textPen.setValue(p);
			WJavaScriptHandle<WPen> gridPen = null;
			if (this.freePens_.size() > 0) {
				gridPen = this.freePens_.get(this.freePens_.size() - 1);
				this.freePens_.remove(this.freePens_.size() - 1);
			} else {
				gridPen = this.createJSPen();
			}
			p = this.getAxis(ax).getGridLinesPen().clone();
			p.setColor(new WColor(p.getColor().getRed(), p.getColor()
					.getGreen(), p.getColor().getBlue(), i == level ? p
					.getColor().getAlpha() : 0));
			gridPen.setValue(p);
			assignments.add(new WCartesianChart.PenAssignment(pen, textPen,
					gridPen));
		}
		this.pens_.put(ax, assignments);
	}

	String getCObjJsRef() {
		return "jQuery.data(" + this.getJsRef() + ",'cobj')";
	}

	private void assignJSHandlesForAllSeries() {
		if (!this.isInteractive()) {
			return;
		}
		for (int i = 0; i < this.series_.size(); ++i) {
			final WDataSeries s = this.series_.get(i);
			if (s.getType() == SeriesType.LineSeries
					|| s.getType() == SeriesType.CurveSeries) {
				this.assignJSPathsForSeries(s);
				this.assignJSTransformsForSeries(s);
			}
		}
	}

	private void freeJSHandlesForAllSeries() {
		this.freeAllJSPaths();
		this.freeAllJSTransforms();
	}

	private void assignJSPathsForSeries(final WDataSeries series) {
		if (!this.isInteractive()) {
			return;
		}
		WJavaScriptHandle<WPainterPath> handle = null;
		if (this.freePainterPaths_.size() > 0) {
			handle = this.freePainterPaths_
					.get(this.freePainterPaths_.size() - 1);
			this.freePainterPaths_.remove(this.freePainterPaths_.size() - 1);
		} else {
			handle = this.createJSPainterPath();
		}
		this.curvePaths_.put(series, handle);
	}

	private void freeJSPathsForSeries(final WDataSeries series) {
		this.freePainterPaths_.add(this.curvePaths_.get(series));
		this.curvePaths_.remove(series);
	}

	private void freeAllJSPaths() {
		for (Iterator<Map.Entry<WDataSeries, WJavaScriptHandle<WPainterPath>>> it_it = this.curvePaths_
				.entrySet().iterator(); it_it.hasNext();) {
			Map.Entry<WDataSeries, WJavaScriptHandle<WPainterPath>> it = it_it
					.next();
			this.freePainterPaths_.add(it.getValue());
		}
		this.curvePaths_.clear();
	}

	private void assignJSTransformsForSeries(final WDataSeries series) {
		if (!this.isInteractive()) {
			return;
		}
		WJavaScriptHandle<WTransform> handle = null;
		if (this.freeTransforms_.size() > 0) {
			handle = this.freeTransforms_.get(this.freeTransforms_.size() - 1);
			this.freeTransforms_.remove(this.freeTransforms_.size() - 1);
		} else {
			handle = this.createJSTransform();
		}
		this.curveTransforms_.put(series, handle);
	}

	private void freeJSTransformsForSeries(final WDataSeries series) {
		this.freeTransforms_.add(this.curveTransforms_.get(series));
		this.curveTransforms_.remove(series);
	}

	private void freeAllJSTransforms() {
		for (Iterator<Map.Entry<WDataSeries, WJavaScriptHandle<WTransform>>> it_it = this.curveTransforms_
				.entrySet().iterator(); it_it.hasNext();) {
			Map.Entry<WDataSeries, WJavaScriptHandle<WTransform>> it = it_it
					.next();
			this.freeTransforms_.add(it.getValue());
		}
		this.curveTransforms_.clear();
	}

	private void updateJSPens(final StringBuilder js) {
		js.append("pens:{x:");
		this.updateJSPensForAxis(js, Axis.XAxis);
		js.append(",y:");
		this.updateJSPensForAxis(js, Axis.YAxis);
		js.append("},");
		js.append("penAlpha:{x:[");
		js.append(this.getAxis(Axis.XAxis).getPen().getColor().getAlpha())
				.append(',');
		js.append(this.getAxis(Axis.XAxis).getTextPen().getColor().getAlpha())
				.append(',');
		js.append(this.getAxis(Axis.XAxis).getGridLinesPen().getColor()
				.getAlpha());
		js.append("],y:[");
		js.append(this.getAxis(Axis.YAxis).getPen().getColor().getAlpha())
				.append(',');
		js.append(this.getAxis(Axis.YAxis).getTextPen().getColor().getAlpha())
				.append(',');
		js.append(
				this.getAxis(Axis.YAxis).getGridLinesPen().getColor()
						.getAlpha()).append("]},");
	}

	private void updateJSPensForAxis(final StringBuilder js, Axis axis) {
		final Map<Axis, List<WCartesianChart.PenAssignment>> pens = this.pens_;
		js.append("[");
		for (int i = 0; i < pens.get(axis).size(); ++i) {
			if (i != 0) {
				js.append(",");
			}
			final WCartesianChart.PenAssignment assignment = pens.get(axis)
					.get(i);
			js.append("[");
			js.append(assignment.pen.getJsRef());
			js.append(",");
			js.append(assignment.textPen.getJsRef());
			js.append(",");
			js.append(assignment.gridPen.getJsRef());
			js.append("]");
		}
		js.append("]");
	}

	private void updateJSConfig(final String key, Object value) {
		if (this.getMethod() == WPaintedWidget.Method.HtmlCanvas) {
			if (!this.cObjCreated_) {
				this.update();
			} else {
				this.doJavaScript(this.getCObjJsRef() + ".updateConfig({" + key
						+ ":" + StringUtils.asString(value).toString() + "});");
			}
		}
	}

	WPainterPath pathForSeries(final WDataSeries series) {
		WJavaScriptHandle<WPainterPath> it = this.curvePaths_.get(series);
		if (it == null) {
			return new WPainterPath();
		} else {
			return it.getValue();
		}
	}

	private WTransform zoomRangeTransform(WTransform xTransform,
			WTransform yTransform) {
		if (this.getOrientation() == Orientation.Vertical) {
			return new WTransform(1, 0, 0, -1, this.chartArea_.getLeft(),
					this.chartArea_.getBottom())
					.multiply(xTransform)
					.multiply(yTransform)
					.multiply(
							new WTransform(1, 0, 0, -1, -this.chartArea_
									.getLeft(), this.chartArea_.getBottom()));
		} else {
			WRectF area = this.hv(this.chartArea_);
			return new WTransform(0, 1, 1, 0, area.getLeft(), area.getTop())
					.multiply(xTransform)
					.multiply(yTransform)
					.multiply(
							new WTransform(0, 1, 1, 0, -area.getTop(), -area
									.getLeft()));
		}
	}

	WTransform calculateCurveTransform(final WDataSeries series) {
		int yAxis = series.getYAxis();
		double origin;
		if (this.getOrientation() == Orientation.Horizontal) {
			origin = this.mapToDeviceWithoutTransform(0.0, 0.0, yAxis).getX();
		} else {
			origin = this.mapToDeviceWithoutTransform(0.0, 0.0, yAxis).getY();
		}
		double offset = this.yAxis(yAxis).mapToDevice(0.0, 0)
				- this.yAxis(yAxis).mapToDevice(series.getOffset(), 0);
		if (this.getOrientation() == Orientation.Horizontal) {
			offset = -offset;
		}
		return new WTransform(1, 0, 0, series.getScale(), 0, origin
				* (1 - series.getScale()) + offset);
	}

	WTransform curveTransform(final WDataSeries series) {
		WJavaScriptHandle<WTransform> it = this.curveTransforms_.get(series);
		WTransform t = new WTransform();
		if (it == null) {
			t.assign(this.calculateCurveTransform(series));
		} else {
			t.assign(it.getValue());
		}
		if (this.getOrientation() == Orientation.Vertical) {
			return t;
		} else {
			return new WTransform(0, 1, 1, 0, 0, 0).multiply(t).multiply(
					new WTransform(0, 1, 1, 0, 0, 0));
		}
	}

	private void setZoomAndPan() {
		WTransform xTransform = new WTransform();
		if (this.getAxis(Axis.XAxis).zoomMin_ != WAxis.AUTO_MINIMUM
				|| this.getAxis(Axis.XAxis).zoomMax_ != WAxis.AUTO_MAXIMUM) {
			double xPan = -this.getAxis(Axis.XAxis).mapToDevice(
					this.getAxis(Axis.XAxis).getPan(), 0);
			double xZoom = this.getAxis(Axis.XAxis).getZoom();
			if (xZoom > this.getAxis(Axis.XAxis).getMaxZoom()) {
				xZoom = this.getAxis(Axis.XAxis).getMaxZoom();
			}
			xTransform.assign(new WTransform(xZoom, 0, 0, 1, xZoom * xPan, 0));
		}
		WTransform yTransform = new WTransform();
		if (this.getAxis(Axis.YAxis).zoomMin_ != WAxis.AUTO_MINIMUM
				|| this.getAxis(Axis.YAxis).zoomMax_ != WAxis.AUTO_MAXIMUM) {
			double yPan = -this.getAxis(Axis.YAxis).mapToDevice(
					this.getAxis(Axis.YAxis).getPan(), 0);
			double yZoom = this.getAxis(Axis.YAxis).getZoom();
			if (yZoom > this.getAxis(Axis.YAxis).getMaxZoom()) {
				yZoom = this.getAxis(Axis.YAxis).getMaxZoom();
			}
			yTransform.assign(new WTransform(1, 0, 0, yZoom, 0, yZoom * yPan));
		}
		WRectF chartArea = this.hv(this.getInsideChartArea());
		WRectF transformedArea = this
				.zoomRangeTransform(xTransform, yTransform).map(chartArea);
		if (transformedArea.getLeft() > chartArea.getLeft()) {
			double diff = chartArea.getLeft() - transformedArea.getLeft();
			if (this.getOrientation() == Orientation.Vertical) {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			} else {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, diff)
						.multiply(yTransform));
			}
			transformedArea = this.zoomRangeTransform(xTransform, yTransform)
					.map(chartArea);
		}
		if (transformedArea.getRight() < chartArea.getRight()) {
			double diff = chartArea.getRight() - transformedArea.getRight();
			if (this.getOrientation() == Orientation.Vertical) {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			} else {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, diff)
						.multiply(yTransform));
			}
			transformedArea = this.zoomRangeTransform(xTransform, yTransform)
					.map(chartArea);
		}
		if (transformedArea.getTop() > chartArea.getTop()) {
			double diff = chartArea.getTop() - transformedArea.getTop();
			if (this.getOrientation() == Orientation.Vertical) {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, -diff)
						.multiply(yTransform));
			} else {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			}
			transformedArea = this.zoomRangeTransform(xTransform, yTransform)
					.map(chartArea);
		}
		if (transformedArea.getBottom() < chartArea.getBottom()) {
			double diff = chartArea.getBottom() - transformedArea.getBottom();
			if (this.getOrientation() == Orientation.Vertical) {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, -diff)
						.multiply(yTransform));
			} else {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			}
			transformedArea = this.zoomRangeTransform(xTransform, yTransform)
					.map(chartArea);
		}
		this.xTransformHandle_.setValue(xTransform);
		this.yTransformHandle_.setValue(yTransform);
		this.getAxis(Axis.XAxis).zoomRangeDirty_ = false;
		this.getAxis(Axis.Y1Axis).zoomRangeDirty_ = false;
	}

	private void addAreaMask() {
		WRectF all = this.hv(new WRectF(0, 0, this.width_, this.height_));
		WRectF chart = this.hv(this.chartArea_);
		List<WRectF> rects = new ArrayList<WRectF>();
		rects.add(new WRectF(all.getTopLeft(), new WPointF(all.getRight(),
				chart.getTop())));
		rects.add(new WRectF(new WPointF(all.getLeft(), chart.getBottom()), all
				.getBottomRight()));
		rects.add(new WRectF(new WPointF(all.getLeft(), chart.getTop()), chart
				.getBottomLeft()));
		rects.add(new WRectF(chart.getTopRight(), new WPointF(all.getRight(),
				chart.getBottom())));
		for (int i = 0; i < rects.size(); ++i) {
			if (rects.get(i).getHeight() > 0 && rects.get(i).getWidth() > 0) {
				WRectArea rect = new WRectArea(rects.get(i));
				rect.setHole(true);
				rect.setTransformable(false);
				this.addArea(rect);
			}
		}
	}

	private void xTransformChanged() {
		this.getAxis(Axis.XAxis)
				.zoomRangeChanged()
				.trigger(this.getAxis(Axis.XAxis).getZoomMinimum(),
						this.getAxis(Axis.XAxis).getZoomMaximum());
	}

	private void yTransformChanged() {
		this.getAxis(Axis.YAxis)
				.zoomRangeChanged()
				.trigger(this.getAxis(Axis.YAxis).getZoomMinimum(),
						this.getAxis(Axis.YAxis).getZoomMaximum());
	}

	private void jsSeriesSelected(double x, double y) {
		if (!this.isSeriesSelectionEnabled()) {
			return;
		}
		WTransform transform = this.zoomRangeTransform(
				this.xTransformHandle_.getValue(),
				this.yTransformHandle_.getValue());
		WPointF p = transform.getInverted().map(new WPointF(x, y));
		double smallestSqDistance = Double.POSITIVE_INFINITY;
		WDataSeries closestSeries = null;
		WPointF closestPointPx = new WPointF();
		WPointF closestPointBeforeSeriesTransform = new WPointF();
		for (int i = 0; i < this.series_.size(); ++i) {
			final WDataSeries series = this.series_.get(i);
			if (!series.isHidden()
					&& (series.getType() == SeriesType.LineSeries || series
							.getType() == SeriesType.CurveSeries)) {
				WPainterPath path = this.pathForSeries(series);
				WTransform t = this.curveTransform(series);
				for (int j = 0; j < path.getSegments().size(); ++j) {
					final WPainterPath.Segment seg = path.getSegments().get(j);
					if (seg.getType() != WPainterPath.Segment.Type.CubicC1
							&& seg.getType() != WPainterPath.Segment.Type.CubicC2
							&& seg.getType() != WPainterPath.Segment.Type.QuadC) {
						WPointF segP = t
								.map(new WPointF(seg.getX(), seg.getY()));
						double dx = p.getX() - segP.getX();
						double dy = p.getY() - segP.getY();
						double d2 = dx * dx + dy * dy;
						if (d2 < smallestSqDistance) {
							smallestSqDistance = d2;
							closestSeries = series;
							closestPointPx = segP;
							closestPointBeforeSeriesTransform = new WPointF(
									seg.getX(), seg.getY());
						}
					}
				}
			}
		}
		{
			WPointF closestDisplayPoint = transform.map(closestPointPx);
			double dx = closestDisplayPoint.getX() - x;
			double dy = closestDisplayPoint.getY() - y;
			double d2 = dx * dx + dy * dy;
			if (d2 > CURVE_SELECTION_DISTANCE_SQUARED) {
				return;
			}
		}
		this.setSelectedSeries(closestSeries);
		if (closestSeries != null) {
			this.seriesSelected_.trigger(closestSeries, this
					.mapFromDeviceWithoutTransform(
							closestPointBeforeSeriesTransform,
							closestSeries.getAxis()));
		} else {
			this.seriesSelected_.trigger((WDataSeries) null, this
					.mapFromDeviceWithoutTransform(
							closestPointBeforeSeriesTransform, Axis.YAxis));
		}
	}

	private void loadTooltip(double x, double y) {
		WPointF p = this
				.zoomRangeTransform(this.xTransformHandle_.getValue(),
						this.yTransformHandle_.getValue()).getInverted()
				.map(new WPointF(x, y));
		MarkerMatchIterator iterator = new MarkerMatchIterator(this, p.getX(),
				p.getY(), MarkerMatchIterator.MATCH_RADIUS
						/ this.xTransformHandle_.getValue().getM11(),
				MarkerMatchIterator.MATCH_RADIUS
						/ this.yTransformHandle_.getValue().getM22());
		this.iterateSeries(iterator, (WPainter) null);
		if (iterator.getMatchedSeries() != null) {
			final WDataSeries series = iterator.getMatchedSeries();
			WString tooltip = series.getModel().getToolTip(iterator.getYRow(),
					iterator.getYColumn());
			boolean isDeferred = !EnumUtils.mask(
					series.getModel().flags(iterator.getYRow(),
							iterator.getYColumn()),
					ItemFlag.ItemHasDeferredTooltip).isEmpty();
			boolean isXHTML = !EnumUtils.mask(
					series.getModel().flags(iterator.getYRow(),
							iterator.getYColumn()), ItemFlag.ItemIsXHTMLText)
					.isEmpty();
			if (!(tooltip.length() == 0) && isDeferred | isXHTML) {
				if (isXHTML) {
					boolean res = removeScript(tooltip);
					if (!res) {
						tooltip = escapeText(tooltip);
					}
				} else {
					tooltip = escapeText(tooltip);
				}
				this.doJavaScript(this.getCObjJsRef() + ".updateTooltip("
						+ WString.toWString(tooltip).getJsStringLiteral()
						+ ");");
			}
		} else {
			for (int btt = 0; btt < this.barTooltips_.size(); ++btt) {
				double[] xs = this.barTooltips_.get(btt).xs;
				double[] ys = this.barTooltips_.get(btt).ys;
				int j = 0;
				int k = 3;
				boolean c = false;
				for (; j < 4; k = j++) {
					if ((ys[j] <= p.getY() && p.getY() < ys[k] || ys[k] <= p
							.getY() && p.getY() < ys[j])
							&& p.getX() < (xs[k] - xs[j]) * (p.getY() - ys[j])
									/ (ys[k] - ys[j]) + xs[j]) {
						c = !c;
					}
				}
				if (c) {
					WString tooltip = this.barTooltips_.get(btt).series
							.getModel().getToolTip(
									this.barTooltips_.get(btt).yRow,
									this.barTooltips_.get(btt).yColumn);
					if (!(tooltip.length() == 0)) {
						this.doJavaScript(this.getCObjJsRef()
								+ ".updateTooltip("
								+ WString.toWString(escapeText(tooltip, false))
										.getJsStringLiteral() + ");");
					}
					return;
				}
			}
		}
	}

	WPointF hv(double x, double y, double width) {
		if (this.orientation_ == Orientation.Vertical) {
			return new WPointF(x, y);
		} else {
			return new WPointF(width - y, x);
		}
	}

	private WPointF inverseHv(double x, double y, double width) {
		if (this.orientation_ == Orientation.Vertical) {
			return new WPointF(x, y);
		} else {
			return new WPointF(y, width - x);
		}
	}

	WPointF inverseHv(final WPointF p) {
		if (p.isJavaScriptBound()) {
			if (this.getOrientation() == Orientation.Vertical) {
				return p;
			} else {
				return p.inverseSwapHV(this.height_);
			}
		}
		return this.inverseHv(p.getX(), p.getY(), this.height_);
	}

	private WRectF getInsideChartArea() {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis yAxis = this.getAxis(Axis.YAxis);
		final WAxis.Segment xs = xAxis.segments_.get(0);
		final WAxis.Segment ys = yAxis.segments_.get(0);
		double xRenderStart = xAxis.isInverted() ? xAxis.mapToDevice(
				xs.renderMaximum, 0) : xs.renderStart;
		double xRenderEnd = xAxis.isInverted() ? xAxis.mapToDevice(
				xs.renderMinimum, 0) : xs.renderStart + xs.renderLength;
		double yRenderStart = yAxis.isInverted() ? yAxis.mapToDevice(
				ys.renderMaximum, 0) : ys.renderStart;
		double yRenderEnd = yAxis.isInverted() ? yAxis.mapToDevice(
				ys.renderMinimum, 0) : ys.renderStart + ys.renderLength;
		double x1 = this.chartArea_.getLeft() + xRenderStart;
		double x2 = this.chartArea_.getLeft() + xRenderEnd;
		double y1 = this.chartArea_.getBottom() - yRenderEnd;
		double y2 = this.chartArea_.getBottom() - yRenderStart;
		return new WRectF(x1, y1, x2 - x1, y2 - y1);
	}

	private int calcYAxisSize(final WAxis axis) {
		return 50;
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		if (app != null && (this.isInteractive() || this.hasDeferredToolTips_)) {
			app.loadJavaScript("js/ChartCommon.js", wtjs2());
			app.doJavaScript(
					"if (!Wt3_3_10.chartCommon) {Wt3_3_10.chartCommon = new "
							+ "Wt3_3_10.ChartCommon("
							+ app.getJavaScriptClass() + "); }", false);
			app.loadJavaScript("js/WCartesianChart.js", wtjs1());
			this.jsDefined_ = true;
		} else {
			this.jsDefined_ = false;
		}
	}

	private boolean axisSliderWidgetForSeries(WDataSeries series) {
		for (int i = 0; i < this.axisSliderWidgets_.size(); ++i) {
			if (this.axisSliderWidgets_.get(i).getSeries() == series) {
				return true;
			}
		}
		return false;
	}

	static class IconWidget extends WPaintedWidget {
		private static Logger logger = LoggerFactory
				.getLogger(IconWidget.class);

		public IconWidget(WCartesianChart chart, int index,
				WContainerWidget parent) {
			super(parent);
			this.chart_ = chart;
			this.index_ = index;
			this.setInline(true);
			this.resize(new WLength(20), new WLength(20));
		}

		public IconWidget(WCartesianChart chart, int index) {
			this(chart, index, (WContainerWidget) null);
		}

		protected void paintEvent(WPaintDevice paintDevice) {
			WPainter painter = new WPainter(paintDevice);
			this.chart_.renderLegendIcon(painter, new WPointF(2.5, 10.0),
					this.chart_.getSeries(this.index_));
		}

		private WCartesianChart chart_;
		private int index_;
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"ChartCommon",
				"function(v){function z(a,b,e,c){function d(f){return e?b[f]:b[l-1-f]}function j(f){for(;d(f)[2]===t||d(f)[2]===w;)f--;return f}var q=h;if(c)q=i;var l=b.length;c=Math.floor(l/2);c=j(c);var x=0,m=l,k=false;if(d(0)[q]>a)return e?-1:l;if(d(l-1)[q]<a)return e?l:-1;for(;!k;){var g=c+1;if(g<l&&(d(g)[2]===t||d(g)[2]===w))g+=2;if(d(c)[q]>a){m=c;c=Math.floor((m+x)/2);c=j(c)}else if(d(c)[q]===a)k=true;else if(g<l&&d(g)[q]>a)k=true;else if(g<l&&d(g)[q]=== a){c=g;k=true}else{x=c;c=Math.floor((m+x)/2);c=j(c)}}return e?c:l-1-c}function C(a,b){return b[0][a]<b[b.length-1][a]}var t=2,w=3,h=0,i=1,A=this;v=v.WT.gfxUtils;var D=v.rect_top,E=v.rect_bottom,B=v.rect_left,F=v.rect_right,G=v.transform_mult;this.findClosestPoint=function(a,b,e){var c=h;if(e)c=i;var d=C(c,b);e=z(a,b,d,e);if(e<0)e=0;if(e>=b.length)return[b[b.length-1][h],b[b.length-1][i]];if(e>=b.length)e=b.length-2;if(b[e][c]===a)return[b[e][h],b[e][i]];var j=d?e+1:e-1;if(d&&b[j][2]==t)j+=2;if(!d&& j<0)return[b[e][h],b[e][i]];if(!d&&j>0&&b[j][2]==w)j-=2;d=Math.abs(a-b[e][c]);a=Math.abs(b[j][c]-a);return d<a?[b[e][h],b[e][i]]:[b[j][h],b[j][i]]};this.minMaxY=function(a,b){b=b?h:i;for(var e=a[0][b],c=a[0][b],d=1;d<a.length;++d)if(a[d][2]!==t&&a[d][2]!==w&&a[d][2]!==5){if(a[d][b]>c)c=a[d][b];if(a[d][b]<e)e=a[d][b]}return[e,c]};this.projection=function(a,b){var e=Math.cos(a);a=Math.sin(a);var c=e*a,d=-b[0]*e-b[1]*a;return[e*e,c,c,a*a,e*d+b[0],a*d+b[1]]};this.distanceSquared=function(a,b){a=[b[h]- a[h],b[i]-a[i]];return a[h]*a[h]+a[i]*a[i]};this.distanceLessThanRadius=function(a,b,e){return e*e>=A.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var e;if(a.x!==undefined){e=a.x;a=a.y}else{e=a[0];a=a[1]}return e>=B(b)&&e<=F(b)&&a>=D(b)&&a<=E(b)};this.toDisplayCoord=function(a,b,e,c,d){if(e){a=[(a[h]-d[0])/d[2],(a[i]-d[1])/d[3]];c=[c[0]+a[i]*c[2],c[1]+a[h]*c[3]]}else{a=[(a[h]-d[0])/d[2],1-(a[i]-d[1])/d[3]];c=[c[0]+ a[h]*c[2],c[1]+a[i]*c[3]]}return G(b,c)};this.findYRange=function(a,b,e,c,d,j,q){if(a.length!==0){var l=A.toDisplayCoord([b,0],[1,0,0,1,0,0],c,d,j),x=A.toDisplayCoord([e,0],[1,0,0,1,0,0],c,d,j),m=c?i:h,k=c?h:i,g=C(m,a),f=z(l[m],a,g,c),n=z(x[m],a,g,c),o,p,r=Infinity,s=-Infinity,y=f===n&&f===a.length||f===-1&&n===-1;if(!y){if(g)if(f<0)f=0;else{f++;if(a[f]&&a[f][2]===t)f+=2}else if(f>=a.length-1)f=a.length-2;if(!g&&n<0)n=0;for(o=Math.min(f,n);o<=Math.max(f,n)&&o<a.length;++o)if(a[o][2]!==t&&a[o][2]!== w){if(a[o][k]<r)r=a[o][k];if(a[o][k]>s)s=a[o][k]}if(g&&f>0||!g&&f<a.length-1){if(g){p=f-1;if(a[p]&&a[p][2]===w)p-=2}else{p=f+1;if(a[p]&&a[p][2]===t)p+=2}o=(l[m]-a[p][m])/(a[f][m]-a[p][m]);f=a[p][k]+o*(a[f][k]-a[p][k]);if(f<r)r=f;if(f>s)s=f}if(g&&n<a.length-1||!g&&n>0){if(g){g=n+1;if(a[g][2]===t)g+=2}else{g=n-1;if(a[g][2]===w)g-=2}o=(x[m]-a[n][m])/(a[g][m]-a[n][m]);f=a[n][k]+o*(a[g][k]-a[n][k]);if(f<r)r=f;if(f>s)s=f}}var u;a=j[2]/(e-b);b=c?2:3;if(!y){u=d[b]/(s-r);u=d[b]/(d[b]/u+20);if(u>q[k])u=q[k]}c= c?[l[i]-D(d),!y?(r+s)/2-d[2]/u/2-B(d):0]:[l[h]-B(d),!y?-((r+s)/2+d[3]/u/2-E(d)):0];return{xZoom:a,yZoom:u,panPoint:c}}}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(na,E,v,m){function J(a){return a===undefined}function p(){return m.modelArea}function Ea(){return m.followCurve}function oa(){return m.crosshair||Ea()!==-1}function y(){return m.isHorizontal}function e(a){if(a===g)return m.xTransform;if(a===f)return m.yTransform}function i(){return m.area}function l(){return m.insideArea}function Y(a){return J(a)?m.series:m.series[a]}function T(a){return Y(a).transform}function $a(a){return y()? z([0,1,1,0,0,0],z(T(a),[0,1,1,0,0,0])):T(a)}function Fa(a){return Y(a).curve}function ab(){return m.seriesSelection}function bb(){return m.sliders}function cb(){return m.hasToolTips}function db(){return m.coordinateOverlayPadding}function wa(){return m.curveManipulation}function G(){return m.maxZoom}function F(){return m.pens}function ia(){return m.selectedCurve}function pa(a){a.preventDefault&&a.preventDefault()}function U(a,b){E.addEventListener(a,b)}function N(a,b){E.removeEventListener(a,b)}function B(a){return a.length} function Pa(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Ga(){if(n){if(n.tooltipTimeout){clearTimeout(n.tooltipTimeout);n.tooltipTimeout=null}if(!n.overTooltip)if(n.tooltipOuterDiv){document.body.removeChild(n.tooltipOuterDiv);n.tooltipEl=null;n.tooltipOuterDiv=null}}}function xa(){if(m.notifyTransform.x||m.notifyTransform.y){if(Ha){window.clearTimeout(Ha);Ha=null}Ha=setTimeout(function(){if(m.notifyTransform.x&&!eb(Qa,e(g))){na.emit(v.widget, \"xTransformChanged\");Z(Qa,e(g))}if(m.notifyTransform.y&&!eb(Ra,e(f))){na.emit(v.widget,\"yTransformChanged\");Z(Ra,e(f))}},ob)}}function aa(){var a,b;if(y()){a=o(i());b=t(i());return z([0,1,1,0,a,b],z(e(g),z(e(f),[0,1,1,0,-b,-a])))}else{a=o(i());b=u(i());return z([1,0,0,-1,a,b],z(e(g),z(e(f),[1,0,0,-1,-a,b])))}}function K(){return z(aa(),l())}function ba(a,b){if(J(b))b=false;a=b?a:z(ya(aa()),a);a=y()?[(a[f]-i()[1])/i()[3],(a[g]-i()[0])/i()[2]]:[(a[g]-i()[0])/i()[2],1-(a[f]-i()[1])/i()[3]];return[p()[0]+ a[g]*p()[2],p()[1]+a[f]*p()[3]]}function Ia(a,b){if(J(b))b=false;return ca.toDisplayCoord(a,b?[1,0,0,1,0,0]:aa(),y(),i(),p())}function za(){var a,b;if(y()){a=(ba([0,t(i())])[0]-p()[0])/p()[2];b=(ba([0,u(i())])[0]-p()[0])/p()[2]}else{a=(ba([o(i()),0])[0]-p()[0])/p()[2];b=(ba([q(i()),0])[0]-p()[0])/p()[2]}var c;for(c=0;c<B(bb());++c){var h=$(\"#\"+bb()[c]);if(h)(h=h.data(\"sobj\"))&&h.changeRange(a,b)}}function O(){Ga();if(cb()&&n.tooltipPosition)n.tooltipTimeout=setTimeout(function(){fb()},gb);da&&hb(function(){v.repaint(); oa()&&Sa()})}function Sa(){if(da){var a=D.getContext(\"2d\");a.clearRect(0,0,D.width,D.height);a.save();a.beginPath();a.moveTo(o(i()),t(i()));a.lineTo(q(i()),t(i()));a.lineTo(q(i()),u(i()));a.lineTo(o(i()),u(i()));a.closePath();a.clip();var b=z(ya(aa()),w),c=w[g],h=w[f];if(Ea()!==-1){b=pb(y()?b[f]:b[g],Fa(Ea()),y());h=z(aa(),z($a(Ea()),b));c=h[g];h=h[f];w[g]=c;w[f]=h}b=y()?[(b[f]-i()[1])/i()[3],(b[g]-i()[0])/i()[2]]:[(b[g]-i()[0])/i()[2],1-(b[f]-i()[1])/i()[3]];b=[p()[0]+b[g]*p()[2],p()[1]+b[f]*p()[3]]; a.fillStyle=a.strokeStyle=m.crosshairColor;a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var d=b[0].toFixed(2);b=b[1].toFixed(2);if(d===\"-0.00\")d=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+d+\",\"+b+\")\",q(i())-db()[0],t(i())+db()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(t(i()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(u(i()))+0.5);a.moveTo(Math.floor(o(i()))+0.5,Math.floor(h)+0.5);a.lineTo(Math.floor(q(i()))+0.5,Math.floor(h)+ 0.5);a.stroke();a.restore()}}function qb(a){return t(a)<=t(l())+Ja&&u(a)>=u(l())-Ja&&o(a)<=o(l())+Ja&&q(a)>=q(l())-Ja}function V(a){var b=K();if(y())if(a===qa)a=ra;else if(a===ra)a=qa;if(J(a)||a===qa)if(e(g)[0]<1){e(g)[0]=1;b=K()}if(J(a)||a===ra)if(e(f)[3]<1){e(f)[3]=1;b=K()}if(J(a)||a===qa){if(o(b)>o(l())){b=o(l())-o(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}if(q(b)<q(l())){b=q(l())-q(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}}if(J(a)||a===ra){if(t(b)>t(l())){b=t(l())- t(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;b=K()}if(u(b)<u(l())){b=u(l())-u(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;K()}}xa()}function fb(){na.emit(v.widget,\"loadTooltip\",n.tooltipPosition[g],n.tooltipPosition[f])}function rb(){if(oa()&&(J(D)||v.canvas.width!==D.width||v.canvas.height!==D.height)){if(D){D.parentNode.removeChild(D);jQuery.removeData(E,\"oobj\");D=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",v.canvas.width);a.setAttribute(\"height\",v.canvas.height); a.style.position=\"absolute\";a.style.display=\"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}v.canvas.parentNode.appendChild(a);D=a;jQuery.data(E,\"oobj\",D)}else if(!J(D)&&!oa()){D.parentNode.removeChild(D);jQuery.removeData(E,\"oobj\");D=undefined}w=[(o(i())+q(i()))/2,(t(i())+u(i()))/2]}function ib(){return D?D:v.canvas}function Ta(a,b){if(sa){var c=Date.now();if(J(b))b=c-ea;var h={x:0,y:0},d=K(),k=sb;if(b> 2*Aa){da=false;var r=Math.floor(b/Aa-1),s;for(s=0;s<r;++s){Ta(a,Aa);if(!sa){da=true;O();return}}b-=r*Aa;da=true}if(j.x===Infinity||j.x===-Infinity)j.x=j.x>0?fa:-fa;if(isFinite(j.x)){j.x/=1+jb*b;d[0]+=j.x*b;if(o(d)>o(l())){j.x+=-k*(o(d)-o(l()))*b;j.x*=0.7}else if(q(d)<q(l())){j.x+=-k*(q(d)-q(l()))*b;j.x*=0.7}if(Math.abs(j.x)<Ua)if(o(d)>o(l()))j.x=Ua;else if(q(d)<q(l()))j.x=-Ua;if(Math.abs(j.x)>fa)j.x=(j.x>0?1:-1)*fa;h.x=j.x*b}if(j.y===Infinity||j.y===-Infinity)j.y=j.y>0?fa:-fa;if(isFinite(j.y)){j.y/= 1+jb*b;d[1]+=j.y*b;if(t(d)>t(l())){j.y+=-k*(t(d)-t(l()))*b;j.y*=0.7}else if(u(d)<u(l())){j.y+=-k*(u(d)-u(l()))*b;j.y*=0.7}if(Math.abs(j.y)<0.001)if(t(d)>t(l()))j.y=0.001;else if(u(d)<u(l()))j.y=-0.001;if(Math.abs(j.y)>fa)j.y=(j.y>0?1:-1)*fa;h.y=j.y*b}d=K();P(h,ta);a=K();if(o(d)>o(l())&&o(a)<=o(l())){j.x=0;P({x:-h.x,y:0},ta);V(qa)}if(q(d)<q(l())&&q(a)>=q(l())){j.x=0;P({x:-h.x,y:0},ta);V(qa)}if(t(d)>t(l())&&t(a)<=t(l())){j.y=0;P({x:0,y:-h.y},ta);V(ra)}if(u(d)<u(l())&&u(a)>=u(l())){j.y=0;P({x:0,y:-h.y}, ta);V(ra)}if(Math.abs(j.x)<kb&&Math.abs(j.y)<kb&&qb(a)){V();sa=false;A=null;j.x=0;j.y=0;ea=null;x=[]}else{ea=c;da&&Ka(Ta)}}}function La(){var a,b,c=lb(e(g)[0])-1;if(c>=B(F().x))c=B(F().x)-1;for(a=0;a<B(F().x);++a)if(c===a)for(b=0;b<B(F().x[a]);++b)F().x[a][b].color[3]=m.penAlpha.x[b];else for(b=0;b<B(F().x[a]);++b)F().x[a][b].color[3]=0;c=lb(e(f)[3])-1;if(c>=B(F().y))c=B(F().y)-1;for(a=0;a<B(F().y);++a)if(c===a)for(b=0;b<B(F().y[a]);++b)F().y[a][b].color[3]=m.penAlpha.y[b];else for(b=0;b<B(F().y[a]);++b)F().y[a][b].color[3]= 0}function P(a,b){if(J(b))b=0;var c=ba(w);if(y())a={x:a.y,y:-a.x};if(b&ta){e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;xa()}else if(b&mb){b=K();if(o(b)>o(l())){if(a.x>0)a.x/=1+(o(b)-o(l()))*Ma}else if(q(b)<q(l()))if(a.x<0)a.x/=1+(q(l())-q(b))*Ma;if(t(b)>t(l())){if(a.y>0)a.y/=1+(t(b)-t(l()))*Ma}else if(u(b)<u(l()))if(a.y<0)a.y/=1+(u(l())-u(b))*Ma;e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;w[g]+=a.x;w[f]+=a.y;xa()}else{e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;w[g]+=a.x;w[f]+=a.y;V()}a=Ia(c);w[g]=a[g];w[f]= a[f];O();za()}function Ba(a,b,c){var h=ba(w),d;d=y()?[a.y-t(i()),a.x-o(i())]:z(ya([1,0,0,-1,o(i()),u(i())]),[a.x,a.y]);a=d[0];d=d[1];var k=Math.pow(1.2,y()?c:b);b=Math.pow(1.2,y()?b:c);if(e(g)[0]*k>G()[g])k=G()[g]/e(g)[0];if(k<1||e(g)[0]!==G()[g])Na(e(g),z([k,0,0,1,a-k*a,0],e(g)));if(e(f)[3]*b>G()[f])b=G()[f]/e(f)[3];if(b<1||e(f)[3]!==G()[f])Na(e(f),z([1,0,0,b,0,d-b*d],e(f)));V();h=Ia(h);w[g]=h[g];w[f]=h[f];La();O();za()}jQuery.data(E,\"cobj\",this);var ga=this,C=na.WT;ga.config=m;var H=C.gfxUtils, z=H.transform_mult,ya=H.transform_inverted,Z=H.transform_assign,eb=H.transform_equal,tb=H.transform_apply,t=H.rect_top,u=H.rect_bottom,o=H.rect_left,q=H.rect_right,ca=C.chartCommon,ub=ca.minMaxY,pb=ca.findClosestPoint,vb=ca.projection,nb=ca.distanceLessThanRadius,lb=ca.toZoomLevel,ua=ca.isPointInRect,wb=ca.findYRange,Aa=17,Ka=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,Aa)}}(),Va=false,hb=function(a){if(!Va){Va= true;Ka(function(){a();Va=false})}};if(window.MSPointerEvent||window.PointerEvent){E.style.touchAction=\"none\";v.canvas.style.msTouchAction=\"none\";v.canvas.style.touchAction=\"none\"}var ta=1,mb=2,qa=1,ra=2,g=0,f=1,ob=250,gb=500,jb=0.003,sb=2.0E-4,Ma=0.07,Ja=3,Ua=0.001,fa=1.5,kb=0.02,ja=jQuery.data(E,\"eobj2\");if(!ja){ja={};ja.contextmenuListener=function(a){pa(a);N(\"contextmenu\",ja.contextmenuListener)}}jQuery.data(E,\"eobj2\",ja);var Q={},ka=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ka= B(d)>0}function b(r){if(Pa(r)){pa(r);d.push(r);a();Q.start(E,{touches:d.slice(0)})}}function c(r){if(ka)if(Pa(r)){pa(r);var s;for(s=0;s<B(d);++s)if(d[s].pointerId===r.pointerId){d.splice(s,1);break}a();Q.end(E,{touches:d.slice(0),changedTouches:[]})}}function h(r){if(Pa(r)){pa(r);var s;for(s=0;s<B(d);++s)if(d[s].pointerId===r.pointerId){d[s]=r;break}a();Q.moved(E,{touches:d.slice(0)})}}var d=[],k=jQuery.data(E,\"eobj\");if(k)if(window.PointerEvent){N(\"pointerdown\",k.pointerDown);N(\"pointerup\",k.pointerUp); N(\"pointerout\",k.pointerUp);N(\"pointermove\",k.pointerMove)}else{N(\"MSPointerDown\",k.pointerDown);N(\"MSPointerUp\",k.pointerUp);N(\"MSPointerOut\",k.pointerUp);N(\"MSPointerMove\",k.pointerMove)}jQuery.data(E,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:h});if(window.PointerEvent){U(\"pointerdown\",b);U(\"pointerup\",c);U(\"pointerout\",c);U(\"pointermove\",h)}else{U(\"MSPointerDown\",b);U(\"MSPointerUp\",c);U(\"MSPointerOut\",c);U(\"MSPointerMove\",h)}})();var D=jQuery.data(E,\"oobj\"),w=null,da=true,A=null,x=[],W=false, ha=false,M=null,Wa=null,Xa=null,j={x:0,y:0},S=null,ea=null,n=jQuery.data(E,\"tobj\");if(!n){n={overTooltip:false};jQuery.data(E,\"tobj\",n)}var va=null,sa=false,Ha=null,Qa=[0,0,0,0,0,0];Z(Qa,e(g));var Ra=[0,0,0,0,0,0];Z(Ra,e(f));var Na=function(a,b){Z(a,b);xa()};v.combinedTransform=aa;this.updateTooltip=function(a){Ga();if(a)if(n.tooltipPosition){n.toolTipEl=document.createElement(\"div\");n.toolTipEl.className=m.ToolTipInnerStyle;n.toolTipEl.innerHTML=a;n.tooltipOuterDiv=document.createElement(\"div\"); n.tooltipOuterDiv.className=m.ToolTipOuterStyle;document.body.appendChild(n.tooltipOuterDiv);n.tooltipOuterDiv.appendChild(n.toolTipEl);var b=C.widgetPageCoordinates(v.canvas);a=n.tooltipPosition[g]+b.x;b=n.tooltipPosition[f]+b.y;C.fitToWindow(n.tooltipOuterDiv,a+10,b+10,a-10,b-10);$(n.toolTipEl).mouseenter(function(){n.overTooltip=true});$(n.toolTipEl).mouseleave(function(){n.overTooltip=false})}};this.mouseMove=function(a,b){setTimeout(function(){setTimeout(Ga,200);if(!ka){var c=C.widgetCoordinates(v.canvas, b);if(ua(c,i())){if(!n.tooltipEl&&cb()){n.tooltipPosition=[c.x,c.y];n.tooltipTimeout=setTimeout(function(){fb()},gb)}if(A===null&&oa()&&da){w=[c.x,c.y];hb(Sa)}}}},0)};this.mouseOut=function(){setTimeout(Ga,200)};this.mouseDown=function(a,b){if(!ka){a=C.widgetCoordinates(v.canvas,b);if(ua(a,i()))A=a}};this.mouseUp=function(){ka||(A=null)};this.mouseDrag=function(a,b){if(!ka)if(A!==null){a=C.widgetCoordinates(v.canvas,b);if(ua(a,i())){if(C.buttons===1)if(wa()&&Y(ia())){b=ia();var c;c=y()?a.x-A.x:a.y- A.y;Z(T(b),z([1,0,0,1,0,c/e(f)[3]],T(b)));O()}else m.pan&&P({x:a.x-A.x,y:a.y-A.y});A=a}}};this.clicked=function(a,b){if(!ka)if(A===null)if(ab()){a=C.widgetCoordinates(v.canvas,b);na.emit(v.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){a=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;var c=m.wheelActions[a];if(!J(c)){var h=C.widgetCoordinates(v.canvas,b);if(ua(h,i())){var d=C.normalizeWheel(b);if(a===0&&wa()){a=ia();var k=-d.spinY;if(Y(a)){c=$a(a);c=tb(c,Fa(a));c=ub(c,y()); c=(c[0]+c[1])/2;C.cancelEvent(b);b=Math.pow(1.2,k);Z(T(a),z([1,0,0,b,0,c-b*c],T(a)));O();return}}if((c===4||c===5||c===6)&&m.pan){a=e(g)[4];k=e(f)[5];if(c===6)P({x:-d.pixelX,y:-d.pixelY});else if(c===5)P({x:0,y:-d.pixelX-d.pixelY});else c===4&&P({x:-d.pixelX-d.pixelY,y:0});if(a!==e(g)[4]||k!==e(f)[5])C.cancelEvent(b)}else if(m.zoom){C.cancelEvent(b);k=-d.spinY;if(k===0)k=-d.spinX;if(c===1)Ba(h,0,k);else if(c===0)Ba(h,k,0);else if(c===2)Ba(h,k,k);else if(c===3)d.pixelX!==0?Ba(h,k,0):Ba(h,0,k)}}}}; var xb=function(){ab()&&na.emit(v.widget,\"seriesSelected\",A.x,A.y)};Q.start=function(a,b,c){W=B(b.touches)===1;ha=B(b.touches)===2;if(W){sa=false;a=C.widgetCoordinates(v.canvas,b.touches[0]);if(!ua(a,i()))return;va=oa()&&nb(w,[a.x,a.y],30)?1:0;ea=Date.now();A=a;if(va!==1){c||(S=window.setTimeout(xb,200));U(\"contextmenu\",ja.contextmenuListener)}C.capture(null);C.capture(ib())}else if(ha&&(m.zoom||wa())){if(S){window.clearTimeout(S);S=null}sa=false;x=[C.widgetCoordinates(v.canvas,b.touches[0]),C.widgetCoordinates(v.canvas, b.touches[1])].map(function(h){return[h.x,h.y]});if(!x.every(function(h){return ua(h,i())})){ha=null;return}C.capture(null);C.capture(ib());M=Math.atan2(x[1][1]-x[0][1],x[1][0]-x[0][0]);Wa=[(x[0][0]+x[1][0])/2,(x[0][1]+x[1][1])/2];c=Math.abs(Math.sin(M));a=Math.abs(Math.cos(M));M=c<Math.sin(0.125*Math.PI)?0:a<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(M)>0?Math.PI/4:-Math.PI/4;Xa=vb(M,Wa)}else return;pa(b)};Q.end=function(a,b){if(S){window.clearTimeout(S);S=null}window.setTimeout(function(){N(\"contextmenu\", ja.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),h=B(c)===0;h||function(){var d;for(d=0;d<B(b.changedTouches);++d)(function(){for(var k=b.changedTouches[d].identifier,r=0;r<B(c);++r)if(c[r].identifier===k){c.splice(r,1);return}})()}();h=B(c)===0;W=B(c)===1;ha=B(c)===2;if(h){Oa=null;if(va===0&&(isFinite(j.x)||isFinite(j.y))&&m.rubberBand){ea=Date.now();sa=true;Ka(Ta)}else{va===1&&ga.mouseUp(null,null);c=[];ea=Xa=Wa=M=null}va=null}else if(W||ha)Q.start(a,b,true)};var Oa=null, la=null,Ya=null;Q.moved=function(a,b){if(W||ha)if(!(W&&A==null)){pa(b);la=C.widgetCoordinates(v.canvas,b.touches[0]);if(B(b.touches)>1)Ya=C.widgetCoordinates(v.canvas,b.touches[1]);if(W&&S&&!nb([la.x,la.y],[A.x,A.y],3)){window.clearTimeout(S);S=null}Oa||(Oa=setTimeout(function(){if(W&&wa()&&Y(ia())){var c=ia();if(Y(c)){var h=la,d;d=y()?(h.x-A.x)/e(f)[3]:(h.y-A.y)/e(f)[3];T(c)[5]+=d;A=h;O()}}else if(W){h=la;d=Date.now();c={x:h.x-A.x,y:h.y-A.y};var k=d-ea;ea=d;if(va===1){w[g]+=c.x;w[f]+=c.y;oa()&&da&& Ka(Sa)}else if(m.pan){j.x=c.x/k;j.y=c.y/k;P(c,m.rubberBand?mb:0)}A=h}else if(ha&&wa()&&Y(ia())){var r=y()?g:f;d=[la,Ya].map(function(I){return y()?[I.x,ma]:[Ca,I.y]});c=Math.abs(x[1][r]-x[0][r]);k=Math.abs(d[1][r]-d[0][r]);var s=c>0?k/c:1;if(k===c)s=1;var ma=z(ya(aa()),[0,(x[0][r]+x[1][r])/2])[1],Da=z(ya(aa()),[0,(d[0][r]+d[1][r])/2])[1];c=ia();if(Y(c)){Z(T(c),z([1,0,0,s,0,-s*ma+Da],T(c)));A=h;O();x=d}}else if(ha&&m.zoom){h=ba(w);var Ca=(x[0][0]+x[1][0])/2;ma=(x[0][1]+x[1][1])/2;d=[la,Ya].map(function(I){return M=== 0?[I.x,ma]:M===Math.PI/2?[Ca,I.y]:z(Xa,[I.x,I.y])});c=Math.abs(x[1][0]-x[0][0]);k=Math.abs(d[1][0]-d[0][0]);var X=c>0?k/c:1;if(k===c||M===Math.PI/2)X=1;var Za=(d[0][0]+d[1][0])/2;c=Math.abs(x[1][1]-x[0][1]);k=Math.abs(d[1][1]-d[0][1]);s=c>0?k/c:1;if(k===c||M===0)s=1;Da=(d[0][1]+d[1][1])/2;y()&&function(){var I=X;X=s;s=I;I=Za;Za=Da;Da=I;I=Ca;Ca=ma;ma=I}();if(e(g)[0]*X>G()[g])X=G()[g]/e(g)[0];if(e(f)[3]*s>G()[f])s=G()[f]/e(f)[3];if(X!==1&&(X<1||e(g)[0]!==G()[g]))Na(e(g),z([X,0,0,1,-X*Ca+Za,0],e(g))); if(s!==1&&(s<1||e(f)[3]!==G()[f]))Na(e(f),z([1,0,0,s,0,-s*ma+Da],e(f)));V();h=Ia(h);w[g]=h[g];w[f]=h[f];x=d;La();O();za()}Oa=null},1))}};this.setXRange=function(a,b,c,h){b=p()[0]+p()[2]*b;c=p()[0]+p()[2]*c;if(o(p())>q(p())){if(b>o(p()))b=o(p());if(c<q(p()))c=q(p())}else{if(b<o(p()))b=o(p());if(c>q(p()))c=q(p())}a=Fa(a);a=wb(a,b,c,y(),i(),p(),G());b=a.xZoom;c=a.yZoom;a=a.panPoint;var d=ba(w);e(g)[0]=b;if(c&&h)e(f)[3]=c;e(g)[4]=-a[g]*b;if(c&&h)e(f)[5]=-a[f]*c;xa();h=Ia(d);w[g]=h[g];w[f]=h[f];V();La(); O();za()};this.getSeries=function(a){return Fa(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))m[b]=a[b];rb();La();O();za()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ga.touchStart=Q.start;ga.touchEnd=Q.end;ga.touchMoved=Q.moved}else{H=function(){};ga.touchStart=H;ga.touchEnd=H;ga.touchMoved=H}}");
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;
	private static final int CURVE_SELECTION_DISTANCE_SQUARED = 400;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
