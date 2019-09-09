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
		this.xAxis_ = new WCartesianChart.AxisStruct();
		this.yAxes_ = new ArrayList<WCartesianChart.AxisStruct>();
		this.barMargin_ = 0;
		this.legend_ = new WLegend();
		this.axisPadding_ = 5;
		this.borderPen_ = new WPen(PenStyle.NoPen);
		this.textPen_ = new WPen();
		this.chartArea_ = null;
		this.hasDeferredToolTips_ = false;
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
		this.onDemandLoadingEnabled_ = false;
		this.loadingBackground_ = new WBrush(WColor.lightGray);
		this.cObjCreated_ = false;
		this.seriesSelected_ = new Signal2<WDataSeries, WPointF>();
		this.jsSeriesSelected_ = new JSignal2<Double, Double>(this,
				"seriesSelected") {
		};
		this.curvePaths_ = new HashMap<WDataSeries, WJavaScriptHandle<WPainterPath>>();
		this.freePainterPaths_ = new ArrayList<WJavaScriptHandle<WPainterPath>>();
		this.curveTransforms_ = new HashMap<WDataSeries, WJavaScriptHandle<WTransform>>();
		this.freeTransforms_ = new ArrayList<WJavaScriptHandle<WTransform>>();
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
		this.xAxis_ = new WCartesianChart.AxisStruct();
		this.yAxes_ = new ArrayList<WCartesianChart.AxisStruct>();
		this.barMargin_ = 0;
		this.legend_ = new WLegend();
		this.axisPadding_ = 5;
		this.borderPen_ = new WPen(PenStyle.NoPen);
		this.textPen_ = new WPen();
		this.chartArea_ = null;
		this.hasDeferredToolTips_ = false;
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
		this.onDemandLoadingEnabled_ = false;
		this.loadingBackground_ = new WBrush(WColor.lightGray);
		this.cObjCreated_ = false;
		this.seriesSelected_ = new Signal2<WDataSeries, WPointF>();
		this.jsSeriesSelected_ = new JSignal2<Double, Double>(this,
				"seriesSelected") {
		};
		this.curvePaths_ = new HashMap<WDataSeries, WJavaScriptHandle<WPainterPath>>();
		this.freePainterPaths_ = new ArrayList<WJavaScriptHandle<WPainterPath>>();
		this.curveTransforms_ = new HashMap<WDataSeries, WJavaScriptHandle<WTransform>>();
		this.freeTransforms_ = new ArrayList<WJavaScriptHandle<WTransform>>();
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
			this.xAxis_.axis.init(this.interface_, Axis.XAxis);
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
		this.xAxis_.axis.setTextPen(pen);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).axis.setTextPen(pen);
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
			return this.xAxis_.axis;
		} else {
			return this.getYAxis(axis == Axis.Y1Axis ? 0 : 1);
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
			this.xAxis_.axis = waxis;
			this.xAxis_.axis.init(this.interface_, axis);
		} else {
			int yIndex = axis == Axis.Y1Axis ? 0 : 1;
			;
			this.yAxes_.get(yIndex).axis = waxis;
			this.yAxes_.get(yIndex).axis.init(this.interface_, axis);
		}
	}

	/**
	 * Returns a vector of all Y axes associated with this chart.
	 * <p>
	 * This defaults to a vector of two axes: the Y1 and Y2 axes. Y1 will be at
	 * index 0, and Y2 will be at index 1.
	 */
	public List<WAxis> getYAxes() {
		List<WAxis> result = new ArrayList<WAxis>();
		;
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			result.add(this.yAxes_.get(i).axis);
		}
		return result;
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
	 *    getAxis(Axis.YAxis) == getYAxis(0)
	 *    getAxis(Axis.Y2Axis) == getYAxis(1)
	 *   }
	 * </pre>
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Precondition: 0 &lt;= i &lt;
	 * {@link WCartesianChart#getYAxisCount() getYAxisCount()} </i>
	 * </p>
	 */
	public WAxis getYAxis(int i) {
		return this.yAxes_.get(i).axis;
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
	 * this chart.
	 * <p>
	 * Precondition: waxis is not null </i>
	 * </p>
	 */
	public int addYAxis(WAxis waxis) {
		int idx = (int) this.yAxes_.size();
		this.yAxes_.add(new WCartesianChart.AxisStruct(waxis));
		this.yAxes_.get(idx).axis.initYAxis(this.interface_, idx);
		this.yAxes_.get(idx).axis.setPadding(this.getAxisPadding());
		this.yAxes_.get(idx).axis.setSoftLabelClipping(true);
		this.yAxes_.get(idx).transformHandle = this.createJSTransform();
		this.yAxes_.get(idx).transformChanged = new JSignal(this,
				"yTransformChanged" + String.valueOf(idx));
		this.update();
		return idx;
	}

	/**
	 * Removes the Y axis with the given id.
	 * <p>
	 * The indices of the axes with an id higher than yAxisId will be
	 * decremented.
	 * <p>
	 * Any {@link WDataSeries} associated with the removed axis are also
	 * removed.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Precondition: 0 &lt;= yAxisId &lt;
	 * {@link WCartesianChart#getYAxisCount() getYAxisCount()} </i>
	 * </p>
	 */
	public void removeYAxis(int yAxisId) {
		{
			int i = 0;
			while (i < this.series_.size()) {
				if (this.series_.get(i).getYAxis() == yAxisId) {
					this.removeSeries(this.series_.get(i));
				} else {
					if (this.series_.get(i).getYAxis() > yAxisId) {
						this.series_.get(i).bindToYAxis(
								this.series_.get(i).getYAxis() - 1);
					}
					++i;
				}
			}
		}
		if (this.getCrosshairYAxis() > yAxisId) {
			this.setCrosshairYAxis(this.getCrosshairYAxis() - 1);
		}
		this.clearPensForAxis(Axis.YAxis, yAxisId);
		;
		this.yAxes_.remove(0 + yAxisId);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).axis.yAxis_ = (int) i;
			this.yAxes_.get(i).axis.axis_ = i == 1 ? Axis.Y2Axis : Axis.Y1Axis;
		}
		this.update();
	}

	/**
	 * Clears all Y axes.
	 * <p>
	 * The effect is the same as repeatedly using
	 * {@link WCartesianChart#removeYAxis(int yAxisId) removeYAxis()} until are
	 * axes are removed, i.e. any {@link WDataSeries} will also be removed.
	 */
	public void clearYAxes() {
		while (!this.series_.isEmpty()) {
			this.removeSeries(this.series_.get(this.series_.size() - 1));
		}
		this.clearPens();
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			;
		}
		this.yAxes_.clear();
		this.update();
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
		this.drawMarker(series, series.getMarker(), result);
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
					this.zoomRangeTransform(
							this.xAxis_.transformHandle.getValue(),
							this.yAxes_.get(ordinateAxis).transformHandle
									.getValue()).getInverted().map(point),
					ordinateAxis);
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
	 * {@link WCartesianChart#zoomRangeTransform(int yAxis)
	 * zoomRangeTransform()} is the identity transform.
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
	 * {@link WCartesianChart#zoomRangeTransform(int yAxis)
	 * zoomRangeTransform()} is the identity transform.
	 * <p>
	 * 
	 * @see WCartesianChart#mapToDeviceWithoutTransform(Object xValue, Object
	 *      yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 */
	public WPointF mapFromDeviceWithoutTransform(final WPointF point,
			int ordinateAxis) {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis yAxis = this.getYAxis(ordinateAxis);
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
			return this.zoomRangeTransform(
					this.xAxis_.transformHandle.getValue(),
					this.yAxes_.get(axis).transformHandle.getValue()).map(
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
	 * {@link WCartesianChart#zoomRangeTransform(int yAxis)
	 * zoomRangeTransform()} is the identity transform.
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
	 * {@link WCartesianChart#zoomRangeTransform(int yAxis)
	 * zoomRangeTransform()} is the identity transform.
	 * <p>
	 * 
	 * @see WCartesianChart#mapFromDeviceWithoutTransform(WPointF point, Axis
	 *      ordinateAxis)
	 */
	public WPointF mapToDeviceWithoutTransform(final Object xValue,
			final Object yValue, int ordinateAxis, int xSegment, int ySegment) {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis yAxis = this.getYAxis(ordinateAxis);
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
		this.xAxis_.location.initLoc = AxisValue.MinimumValue;
		this.xAxis_.location.finLoc = AxisValue.MinimumValue;
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).location.initLoc = AxisValue.MinimumValue;
			this.yAxes_.get(i).location.finLoc = AxisValue.MinimumValue;
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
		WCartesianChart self = this;
		self.clearPens();
		if (this.isInteractive()) {
			self.createPensForAxis(Axis.XAxis, -1);
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				self.createPensForAxis(Axis.YAxis, i);
			}
		}
		if (autoLayout) {
			self.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
			self.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
			this.calcChartArea();
			this.xAxis_.transform.assign(new WTransform());
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				this.yAxes_.get(i).transform.assign(new WTransform());
			}
			if (this.chartArea_.getWidth() <= 5
					|| this.chartArea_.getHeight() <= 5
					|| !this.prepareAxes(device)) {
				if (this.isInteractive()) {
					this.xAxis_.transform.assign(this.xAxis_.transformHandle
							.getValue());
					for (int i = 0; i < this.getYAxisCount(); ++i) {
						this.yAxes_.get(i).transform
								.assign(this.yAxes_.get(i).transformHandle
										.getValue());
					}
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
				&& this.chartArea_.getHeight() > 5 && this.prepareAxes(device);
		if (this.isInteractive()) {
			this.xAxis_.transform
					.assign(this.xAxis_.transformHandle.getValue());
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				this.yAxes_.get(i).transform
						.assign(this.yAxes_.get(i).transformHandle.getValue());
			}
		} else {
			this.xAxis_.transform.assign(new WTransform());
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				this.yAxes_.get(i).transform.assign(new WTransform());
			}
		}
		if (this.isInteractive()) {
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
		this.xAxis_.axis.setPadding(padding);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).axis.setPadding(padding);
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
		return !this.yAxes_.isEmpty()
				&& (this.zoomEnabled_ || this.panEnabled_
						|| this.crosshairEnabled_ || this.followCurve_ != null
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
			this.updateJSConfig("crosshairAxis", yAxis);
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
		this.xAxis_.axis.setSoftLabelClipping(enabled);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).axis.setSoftLabelClipping(enabled);
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

	/**
	 * Enable on-demand loading.
	 * <p>
	 * By default, when on-demand loading is not enabled, the entire chart area
	 * is loaded, regardless of whether it is within the current zoom range of
	 * the X axis.
	 * <p>
	 * When on-demand loading is enabled only the currently visible area + some
	 * margin is loaded. As the visible area changes, different data is loaded.
	 * This improves performance for charts with a lot of data if not all of the
	 * data needs to be visible at the same time.
	 * <p>
	 * This feature is especially useful in combination with
	 * {@link WAxis#setMaximumZoomRange(double size)
	 * WAxis#setMaximumZoomRange()} or {@link WAxis#setMinZoom(double minZoom)
	 * WAxis#setMinZoom()}, which makes it impossible for the user to view all
	 * of the data at the same time, because that would incur too much overhead.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>On-demand loading requires that the X axis data for all
	 * data series is sorted in ascending order. This feature is optimized for
	 * equidistant X axis data, but that&apos;s not a requirement.
	 * <p>
	 * If no minimum or maximum are set on the Y axis (or axes), then the chart
	 * will still have to scan all data of its data series to automatically
	 * determine the minimum and maximum Y axis values. If this performance hit
	 * is undesirable and the Y axis range is known or guaranteed to be within a
	 * certain range, make sure to
	 * {@link WAxis#setRange(double minimum, double maximum) set a range} on the
	 * Y axis (or axes).</i>
	 * </p>
	 * 
	 * @see WCartesianChart#isOnDemandLoadingEnabled()
	 */
	public void setOnDemandLoadingEnabled(boolean enabled) {
		if (this.onDemandLoadingEnabled_ != enabled) {
			this.onDemandLoadingEnabled_ = enabled;
			this.update();
		}
	}

	/**
	 * Returns whether on-demand loading is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setOnDemandLoadingEnabled(boolean enabled)
	 */
	public boolean isOnDemandLoadingEnabled() {
		return this.onDemandLoadingEnabled_;
	}

	/**
	 * Set the background brush for the unloaded area.
	 * <p>
	 * 
	 * @see WCartesianChart#setOnDemandLoadingEnabled(boolean enabled)
	 * @see WCartesianChart#getLoadingBackground()
	 */
	public void setLoadingBackground(final WBrush brush) {
		if (!this.loadingBackground_.equals(brush)) {
			this.loadingBackground_ = brush;
			if (this.isOnDemandLoadingEnabled()) {
				this.update();
			}
		}
	}

	/**
	 * Returns the background brush for the unloaded area.
	 * <p>
	 * 
	 * @see WCartesianChart#setOnDemandLoadingEnabled(boolean enabled)
	 * @see WCartesianChart#setLoadingBackground(WBrush brush)
	 */
	public WBrush getLoadingBackground() {
		return this.loadingBackground_;
	}

	public void iterateSeries(SeriesIterator iterator, WPainter painter,
			boolean reverseStacked, boolean extremesOnly) {
		double groupWidth = 0.0;
		int numBarGroups;
		int currentBarGroup;
		int rowCount = this.getModel() != null ? this.getModel().getRowCount()
				: 0;
		List<Double> posStackedValuesInit = new ArrayList<Double>();
		List<Double> minStackedValuesInit = new ArrayList<Double>();
		final boolean scatterPlot = this.type_ == ChartType.ScatterPlot;
		if (scatterPlot) {
			numBarGroups = 1;
			currentBarGroup = 0;
		} else {
			numBarGroups = this.getCalcNumBarGroups();
			currentBarGroup = 0;
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
					* (this.getAxis(Axis.XAxis).mapToDevice(2) - this.getAxis(
							Axis.XAxis).mapToDevice(1));
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
				boolean doSeries = this.series_.get(i).getYAxis() >= 0
						&& this.series_.get(i).getYAxis() < this
								.getYAxisCount()
						&& iterator.startSeries(this.series_.get(i),
								groupWidth, numBarGroups, currentBarGroup);
				List<Double> posStackedValues = new ArrayList<Double>();
				List<Double> minStackedValues = new ArrayList<Double>();
				if (doSeries || !scatterPlot && i != endSeries) {
					for (int currentXSegment = 0; currentXSegment < this
							.getAxis(Axis.XAxis).getSegmentCount(); ++currentXSegment) {
						for (int currentYSegment = 0; currentYSegment < this
								.getYAxis(this.series_.get(i).getYAxis())
								.getSegmentCount(); ++currentYSegment) {
							posStackedValues.clear();
							posStackedValues.addAll(posStackedValuesInit);
							minStackedValues.clear();
							minStackedValues.addAll(minStackedValuesInit);
							if (painter != null) {
								WRectF csa = this.chartSegmentArea(this
										.getYAxis(this.series_.get(i)
												.getYAxis()), currentXSegment,
										currentYSegment);
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
							int startRow = 0;
							int endRow = this.series_.get(i).getModel() != null ? this.series_
									.get(i).getModel().getRowCount()
									: 0;
							if (this.isInteractive()
									&& !extremesOnly
									&& this.isOnDemandLoadingEnabled()
									&& this.series_.get(i).getModel() != null
									&& !this.axisSliderWidgetForSeries(this.series_
											.get(i))) {
								int xColumn = this.series_.get(i)
										.XSeriesColumn() == -1 ? this
										.XSeriesColumn() : this.series_.get(i)
										.XSeriesColumn();
								double zoomMin = this.getAxis(Axis.XAxis)
										.getZoomMinimum();
								double zoomMax = this.getAxis(Axis.XAxis)
										.getZoomMaximum();
								double zoomRange = zoomMax - zoomMin;
								if (xColumn == -1) {
									startRow = Math.max(0,
											(int) (zoomMin - zoomRange));
									endRow = Math.min(endRow, (int) Math
											.ceil(zoomMax + zoomRange) + 1);
								} else {
									startRow = Math
											.max(binarySearchRow(this.series_
													.get(i).getModel(),
													xColumn, zoomMin
															- zoomRange, 0,
													this.series_.get(i)
															.getModel()
															.getRowCount() - 1) - 1,
													startRow);
									endRow = Math
											.min(binarySearchRow(this.series_
													.get(i).getModel(),
													xColumn, zoomMax
															+ zoomRange, 0,
													this.series_.get(i)
															.getModel()
															.getRowCount() - 1) + 1,
													endRow);
								}
							}
							for (int row = startRow; row < endRow; ++row) {
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
								if (extremesOnly
										&& this.isOnDemandLoadingEnabled()) {
									row = Math.max(endRow - 2, row);
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
		iterateSeries(iterator, painter, false, false);
	}

	public final void iterateSeries(SeriesIterator iterator, WPainter painter,
			boolean reverseStacked) {
		iterateSeries(iterator, painter, reverseStacked, false);
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

	static class AxisLocation {
		private static Logger logger = LoggerFactory
				.getLogger(AxisLocation.class);

		public AxisLocation() {
			this.minOffset = 0;
			this.maxOffset = 0;
			this.initLoc = AxisValue.MinimumValue;
			this.finLoc = AxisValue.MinimumValue;
		}

		public int minOffset;
		public int maxOffset;
		public AxisValue initLoc;
		public AxisValue finLoc;
	}

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

	static class AxisStruct {
		private static Logger logger = LoggerFactory
				.getLogger(AxisStruct.class);

		public AxisStruct() {
			this.axis = new WAxis();
			this.calculatedWidth = 0;
			this.location = new WCartesianChart.AxisLocation();
			this.transform = new WTransform();
			this.transformHandle = null;
			this.transformChanged = null;
			this.pens = new ArrayList<WCartesianChart.PenAssignment>();
		}

		public AxisStruct(WAxis ax) {
			this.axis = ax;
			this.calculatedWidth = 0;
			this.location = new WCartesianChart.AxisLocation();
			this.transform = new WTransform();
			this.transformHandle = null;
			this.transformChanged = null;
			this.pens = new ArrayList<WCartesianChart.PenAssignment>();
		}

		public WAxis axis;
		public int calculatedWidth;
		public WCartesianChart.AxisLocation location;
		public WTransform transform;
		public WJavaScriptHandle<WTransform> transformHandle;
		public JSignal transformChanged;
		public List<WCartesianChart.PenAssignment> pens;
	}

	private WChart2DImplementation interface_;
	private Orientation orientation_;
	private int XSeriesColumn_;
	private ChartType type_;
	private List<WDataSeries> series_;
	WCartesianChart.AxisStruct xAxis_;
	private List<WCartesianChart.AxisStruct> yAxes_;
	private double barMargin_;
	private WLegend legend_;
	private int axisPadding_;
	private WPen borderPen_;
	WPen textPen_;
	private int width_;
	private int height_;
	WRectF chartArea_;
	boolean hasDeferredToolTips_;
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
	private boolean onDemandLoadingEnabled_;
	private WBrush loadingBackground_;
	boolean cObjCreated_;
	private Signal2<WDataSeries, WPointF> seriesSelected_;
	private JSignal2<Double, Double> jsSeriesSelected_;
	Map<WDataSeries, WJavaScriptHandle<WPainterPath>> curvePaths_;
	private List<WJavaScriptHandle<WPainterPath>> freePainterPaths_;
	Map<WDataSeries, WJavaScriptHandle<WTransform>> curveTransforms_;
	private List<WJavaScriptHandle<WTransform>> freeTransforms_;
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
		this.yAxes_.add(new WCartesianChart.AxisStruct());
		this.yAxes_.add(new WCartesianChart.AxisStruct());
		this.yAxes_.get(this.yAxes_.size() - 1).axis
				.setLocation(AxisValue.MaximumValue);
		this.getAxis(Axis.XAxis).init(this.interface_, Axis.XAxis);
		this.getAxis(Axis.YAxis).init(this.interface_, Axis.YAxis);
		this.getAxis(Axis.Y2Axis).init(this.interface_, Axis.Y2Axis);
		this.getAxis(Axis.Y2Axis).setVisible(false);
		this.getAxis(Axis.XAxis).setPadding(this.axisPadding_);
		this.getAxis(Axis.YAxis).setPadding(this.axisPadding_);
		this.getAxis(Axis.Y2Axis).setPadding(this.axisPadding_);
		this.getAxis(Axis.XAxis).setSoftLabelClipping(true);
		this.getAxis(Axis.YAxis).setSoftLabelClipping(true);
		this.getAxis(Axis.Y2Axis).setSoftLabelClipping(true);
		this.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
		this.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
		this.xAxis_.transformHandle = this.createJSTransform();
		this.xAxis_.transformChanged = new JSignal(this, "xTransformChanged");
		for (int i = 0; i < 2; ++i) {
			this.yAxes_.get(i).transformHandle = this.createJSTransform();
			this.yAxes_.get(i).transformChanged = new JSignal(this,
					"yTransformChanged" + String.valueOf(i));
		}
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
			List<WRectF> modelAreas = new ArrayList<WRectF>();
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				double modelBottom = this.getYAxis(i).mapFromDevice(0);
				double modelTop = this.getYAxis(i).mapFromDevice(
						this.chartArea_.getHeight());
				double modelLeft = this.getAxis(Axis.XAxis).mapFromDevice(0);
				double modelRight = this.getAxis(Axis.XAxis).mapFromDevice(
						this.chartArea_.getWidth());
				WRectF modelArea = new WRectF(modelLeft, modelBottom,
						modelRight - modelLeft, modelTop - modelBottom);
				modelAreas.add(modelArea);
			}
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
				for (int i = 0; i < this.getYAxisCount(); ++i) {
					if (this.getYAxis(i).isVisible()
							&& (this.yAxes_.get(i).location.initLoc == AxisValue.MaximumValue || this.yAxes_
									.get(i).location.initLoc == AxisValue.BothSides)) {
						if (this.getYAxis(i).getTickDirection() == TickDirection.Inwards) {
							coordPaddingX = 40;
						}
						break;
					}
				}
			} else {
				if (this.getAxis(Axis.XAxis).isVisible()
						&& this.getAxis(Axis.XAxis).getTickDirection() == TickDirection.Inwards
						&& (this.getAxis(Axis.XAxis).getLocation() == AxisValue.MaximumValue || this
								.getAxis(Axis.XAxis).getLocation() == AxisValue.BothSides)) {
					coordPaddingX = 40;
				}
				for (int i = 0; i < this.getYAxisCount(); ++i) {
					if (this.getYAxis(i).isVisible()
							&& (this.yAxes_.get(i).location.initLoc == AxisValue.MinimumValue || this.yAxes_
									.get(i).location.initLoc == AxisValue.BothSides)) {
						if (this.getYAxis(i).getTickDirection() == TickDirection.Inwards) {
							coordPaddingY = 25;
						}
						break;
					}
				}
			}
			if ((this.getAxis(Axis.XAxis).zoomRangeChanged().isConnected() || this
					.isOnDemandLoadingEnabled())
					&& !this.xAxis_.transformChanged.isConnected()) {
				this.xAxis_.transformChanged.addListener(this,
						new Signal.Listener() {
							public void trigger() {
								WCartesianChart.this.xTransformChanged();
							}
						});
			}
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				if ((this.getYAxis(i).zoomRangeChanged().isConnected() || this
						.isOnDemandLoadingEnabled())
						&& !this.yAxes_.get(i).transformChanged.isConnected()) {
					final int axis = i;
					this.yAxes_.get(i).transformChanged.addListener(this,
							new Signal.Listener() {
								public void trigger() {
									WCartesianChart.this
											.yTransformChanged(axis);
								}
							});
				}
			}
			char[] buf = new char[30];
			WApplication app = WApplication.getInstance();
			StringBuilder ss = new StringBuilder();
			int selectedCurve = this.selectedSeries_ != null ? this
					.getSeriesIndexOf(this.selectedSeries_) : -1;
			int followCurve = this.followCurve_ != null ? this
					.getSeriesIndexOf(this.followCurve_) : -1;
			ss.append("new Wt3_4_1.WCartesianChart(")
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
					.append(",crosshairAxis:")
					.append(this.crosshairYAxis_)
					.append(",crosshairColor:")
					.append(jsStringLiteral(this.crosshairColor_
							.getCssText(true))).append(",followCurve:")
					.append(followCurve).append(",xTransform:")
					.append(this.xAxis_.transformHandle.getJsRef())
					.append(",yTransforms:[");
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				if (i != 0) {
					ss.append(',');
				}
				ss.append(this.yAxes_.get(i).transformHandle.getJsRef());
			}
			ss.append("],area:").append(this.hv(this.chartArea_).getJsRef())
					.append(",insideArea:")
					.append(this.hv(insideArea).getJsRef())
					.append(",modelAreas:[");
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				if (i != 0) {
					ss.append(',');
				}
				ss.append(modelAreas.get(i).getJsRef());
			}
			ss.append("],");
			ss.append("hasToolTips:")
					.append(StringUtils.asString(this.hasDeferredToolTips_)
							.toString())
					.append(",notifyTransform:{x:")
					.append(StringUtils.asString(
							this.getAxis(Axis.XAxis).zoomRangeChanged()
									.isConnected()
									|| this.isOnDemandLoadingEnabled())
							.toString()).append(",y:[");
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				if (i != 0) {
					ss.append(',');
				}
				ss.append(StringUtils.asString(
						this.getYAxis(i).zoomRangeChanged().isConnected()
								|| this.isOnDemandLoadingEnabled()).toString());
			}
			ss.append("]},ToolTipInnerStyle:")
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
						ss.append("transform:")
								.append(this.curveTransforms_.get(
										this.series_.get(i)).getJsRef())
								.append(",");
						ss.append("axis:").append(
								this.series_.get(i).getYAxis());
						ss.append("}");
					}
				}
			}
			ss.append("},");
			ss.append("minZoom:{x:")
					.append(MathUtils.roundJs(this.getAxis(Axis.XAxis)
							.getMinZoom(), 16)).append(",y:[");
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				if (i != 0) {
					ss.append(',');
				}
				ss.append(MathUtils.roundJs(this.getYAxis(i).getMinZoom(), 16));
			}
			ss.append("]},");
			ss.append("maxZoom:{x:")
					.append(MathUtils.roundJs(this.getAxis(Axis.XAxis)
							.getMaxZoom(), 16)).append(",y:[");
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				if (i != 0) {
					ss.append(',');
				}
				ss.append(MathUtils.roundJs(this.getYAxis(i).getMaxZoom(), 16));
			}
			ss.append("]},");
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
			ss.append(coordPaddingY).append("],");
			ss.append("xAxis:{");
			ss.append("width:")
					.append(MathUtils.roundJs(this.xAxis_.calculatedWidth, 16))
					.append(',');
			ss.append("side:'")
					.append(locToJsString(this.xAxis_.location.initLoc))
					.append('\'');
			ss.append("},");
			ss.append("yAxes:[");
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				if (i != 0) {
					ss.append(',');
				}
				ss.append('{');
				ss.append("width:")
						.append(MathUtils.roundJs(
								this.yAxes_.get(i).calculatedWidth, 16))
						.append(',');
				ss.append("side:'")
						.append(locToJsString(this.yAxes_.get(i).location.initLoc))
						.append("',");
				ss.append("minOffset:")
						.append(this.yAxes_.get(i).location.minOffset)
						.append(',');
				ss.append("maxOffset:").append(
						this.yAxes_.get(i).location.maxOffset);
				ss.append('}');
			}
			ss.append("]});");
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
			for (int i = 0; i < this.getYAxisCount(); ++i) {
				this.renderGrid(painter, this.getYAxis(i));
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
		final WAxis yAx = this.getYAxis(yAxis);
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
	protected boolean prepareAxes(WPaintDevice device) {
		if (this.yAxes_.isEmpty()) {
			return true;
		}
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		Orientation yDir = this.orientation_;
		Orientation xDir = this.orientation_ == Orientation.Vertical ? Orientation.Horizontal
				: Orientation.Vertical;
		if (!xAxis.prepareRender(xDir, this.chartArea_.getWidth())) {
			return false;
		}
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			if (!this.yAxes_.get(i).axis.prepareRender(yDir,
					this.chartArea_.getHeight())) {
				return false;
			}
		}
		if (xAxis.getScale() == AxisScale.CategoryScale) {
			switch (xAxis.getLocation()) {
			case MinimumValue:
			case ZeroValue:
				this.xAxis_.location.initLoc = AxisValue.MinimumValue;
				this.xAxis_.location.finLoc = AxisValue.MinimumValue;
				break;
			case MaximumValue:
				this.xAxis_.location.initLoc = AxisValue.MaximumValue;
				this.xAxis_.location.finLoc = AxisValue.MaximumValue;
				break;
			case BothSides:
				this.xAxis_.location.initLoc = AxisValue.BothSides;
				this.xAxis_.location.finLoc = AxisValue.BothSides;
			}
		} else {
			AxisValue xLocation = this.xAxis_.axis.getLocation();
			this.xAxis_.location.initLoc = xLocation;
			if (xLocation == AxisValue.ZeroValue) {
				if (this.yAxes_.get(0).axis.segments_
						.get(this.yAxes_.get(0).axis.segments_.size() - 1).renderMaximum < 0) {
					xLocation = AxisValue.MaximumValue;
				} else {
					if (this.yAxes_.get(0).axis.segments_.get(this.yAxes_
							.get(0).axis.segments_.size() - 1).renderMinimum > 0) {
						xLocation = AxisValue.MinimumValue;
					} else {
						if (!this.yAxes_.get(0).axis.isOnAxis(0.0)) {
							xLocation = AxisValue.MinimumValue;
						}
					}
				}
				this.xAxis_.location.initLoc = xLocation;
			} else {
				if (xLocation == AxisValue.MinimumValue) {
					if (this.yAxes_.get(0).axis.segments_.get(0).renderMinimum == 0
							&& this.yAxes_.get(0).axis.getTickDirection() == TickDirection.Outwards) {
						xLocation = AxisValue.ZeroValue;
					}
				} else {
					if (xLocation == AxisValue.MaximumValue) {
						if (this.yAxes_.get(0).axis.segments_.get(this.yAxes_
								.get(0).axis.segments_.size() - 1).renderMaximum == 0) {
							xLocation = AxisValue.ZeroValue;
						}
					}
				}
			}
			this.xAxis_.location.finLoc = xLocation;
		}
		this.xAxis_.calculatedWidth = this.calcAxisSize(xAxis, device) + 10;
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.yAxes_.get(i).location.initLoc = this.yAxes_.get(i).axis
					.getLocation();
		}
		List<WAxis> minimumYaxes = this
				.collectYAxesAtLocation(AxisValue.MinimumValue);
		int offset = 0;
		for (int i = 0; i < minimumYaxes.size(); ++i) {
			final WAxis axis = minimumYaxes.get(i);
			if (axis.getLocation() != AxisValue.BothSides) {
				this.yAxes_.get(axis.getYAxisId()).location.initLoc = AxisValue.MinimumValue;
			}
			this.yAxes_.get(axis.getYAxisId()).location.minOffset = offset;
			this.yAxes_.get(axis.getYAxisId()).calculatedWidth = this
					.calcAxisSize(axis, device) + 10;
			if (i == 0 && axis.getTickDirection() == TickDirection.Inwards) {
				offset += 10;
			} else {
				offset += this.yAxes_.get(axis.getYAxisId()).calculatedWidth;
			}
		}
		List<WAxis> maximumYaxes = this
				.collectYAxesAtLocation(AxisValue.MaximumValue);
		offset = 0;
		for (int i = 0; i < maximumYaxes.size(); ++i) {
			final WAxis axis = maximumYaxes.get(i);
			if (axis.getLocation() != AxisValue.BothSides) {
				this.yAxes_.get(axis.getYAxisId()).location.initLoc = AxisValue.MaximumValue;
			}
			this.yAxes_.get(axis.getYAxisId()).location.maxOffset = offset;
			this.yAxes_.get(axis.getYAxisId()).calculatedWidth = this
					.calcAxisSize(axis, device) + 10;
			if (i == 0 && axis.getTickDirection() == TickDirection.Inwards) {
				offset += 10;
			} else {
				offset += this.yAxes_.get(axis.getYAxisId()).calculatedWidth;
			}
		}
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			this.yAxes_.get(i).location.finLoc = this.yAxes_.get(i).location.initLoc;
		}
		if (!minimumYaxes.isEmpty()
				&& minimumYaxes.get(0).getLocation() == AxisValue.MinimumValue
				&& (this.getAxis(Axis.XAxis).isInverted() ? this.xAxis_.axis.segments_
						.get(this.xAxis_.axis.segments_.size() - 1).renderMaximum == 0
						: this.xAxis_.axis.segments_.get(0).renderMinimum == 0)
				&& minimumYaxes.get(0).getTickDirection() == TickDirection.Outwards) {
			this.yAxes_.get(minimumYaxes.get(0).getYAxisId()).location.finLoc = AxisValue.ZeroValue;
		}
		if (!maximumYaxes.isEmpty()
				&& maximumYaxes.get(0).getLocation() == AxisValue.MaximumValue
				&& (this.getAxis(Axis.XAxis).isInverted() ? this.xAxis_.axis.segments_
						.get(0).renderMinimum == 0
						: this.xAxis_.axis.segments_
								.get(this.xAxis_.axis.segments_.size() - 1).renderMaximum == 0)) {
			this.yAxes_.get(maximumYaxes.get(0).getYAxisId()).location.finLoc = AxisValue.ZeroValue;
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
		if (this.isOnDemandLoadingEnabled()) {
			painter.save();
			WPainterPath clipPath = new WPainterPath();
			clipPath.addRect(this.hv(this.chartArea_));
			painter.setClipPath(clipPath);
			painter.setClipping(true);
			double zoomRange = this.getAxis(Axis.XAxis).getZoomMaximum()
					- this.getAxis(Axis.XAxis).getZoomMinimum();
			double zoomStart = this.getAxis(Axis.XAxis).getZoomMinimum()
					- zoomRange;
			double zoomEnd = this.getAxis(Axis.XAxis).getZoomMaximum()
					+ zoomRange;
			double minX = Math.max(
					this.chartArea_.getLeft()
							+ this.getAxis(Axis.XAxis).mapToDevice(zoomStart),
					this.chartArea_.getLeft());
			double maxX = Math.min(
					this.chartArea_.getLeft()
							+ this.getAxis(Axis.XAxis).mapToDevice(zoomEnd),
					this.chartArea_.getRight());
			painter.fillRect(
					this.zoomRangeTransform(0).map(
							this.hv(new WRectF(this.chartArea_.getLeft(),
									this.chartArea_.getTop(), minX
											- this.chartArea_.getLeft(),
									this.chartArea_.getHeight()))), this
							.getLoadingBackground());
			painter.fillRect(
					this.zoomRangeTransform(0).map(
							this.hv(new WRectF(maxX, this.chartArea_.getTop(),
									this.chartArea_.getRight() - maxX,
									this.chartArea_.getHeight()))), this
							.getLoadingBackground());
			painter.restore();
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
		this.renderAxis(painter, this.xAxis_.axis, properties);
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			this.renderAxis(painter, this.yAxes_.get(i).axis, properties);
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
					WTransform t = this.zoomRangeTransform(series.getYAxis());
					if (series.getType() == SeriesType.LineSeries
							|| series.getType() == SeriesType.CurveSeries) {
						t.assign(t.multiply(this.curveTransform(series)));
					}
					int xSegment = 0;
					double x = this.getAxis(Axis.XAxis).getValue(label.getX());
					if (!this.isInteractive()) {
						while (xSegment < this.getAxis(Axis.XAxis)
								.getSegmentCount()
								&& (this.getAxis(Axis.XAxis).segments_
										.get(xSegment).renderMinimum > x || this
										.getAxis(Axis.XAxis).segments_
										.get(xSegment).renderMaximum < x)) {
							++xSegment;
						}
					}
					int ySegment = 0;
					double y = this.getYAxis(series.getYAxis()).getValue(
							label.getY());
					if (!this.isInteractive()) {
						while (ySegment < this.getYAxis(series.getYAxis())
								.getSegmentCount()
								&& (this.getYAxis(series.getYAxis()).segments_
										.get(ySegment).renderMinimum > y || this
										.getYAxis(series.getYAxis()).segments_
										.get(ySegment).renderMaximum < y)) {
							++ySegment;
						}
					}
					if (xSegment < this.getAxis(Axis.XAxis).getSegmentCount()
							&& ySegment < this.getYAxis(series.getYAxis())
									.getSegmentCount()) {
						WPointF devicePoint = this.mapToDeviceWithoutTransform(
								label.getX(), label.getY(), series.getYAxis(),
								xSegment, ySegment);
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
		final int legendPadding = 10;
		int legendWidth = 0;
		int legendHeight = 0;
		if (this.isLegendEnabled()) {
			painter.save();
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
			legendWidth = (int) this.getLegendColumnWidth().toPixels()
					* Math.min(this.getLegendColumns(), numSeriesWithLegend);
			legendHeight = (int) (numLegendRows * lineHeight);
			int x = 0;
			int y = 0;
			switch (this.getLegendSide()) {
			case Left:
				if (this.getLegendLocation() == LegendLocation.LegendInside) {
					x = this.getPlotAreaPadding(Side.Left) + legendPadding;
				} else {
					x = this.getPlotAreaPadding(Side.Left) - legendPadding
							- legendWidth;
				}
				break;
			case Right:
				x = w - this.getPlotAreaPadding(Side.Right);
				if (this.getLegendLocation() == LegendLocation.LegendInside) {
					x -= legendPadding + legendWidth;
				} else {
					x += legendPadding;
				}
				break;
			case Top:
				if (this.getLegendLocation() == LegendLocation.LegendInside) {
					y = this.getPlotAreaPadding(Side.Top) + legendPadding;
				} else {
					y = this.getPlotAreaPadding(Side.Top) - legendPadding
							- legendHeight;
				}
				break;
			case Bottom:
				y = h - this.getPlotAreaPadding(Side.Bottom);
				if (this.getLegendLocation() == LegendLocation.LegendInside) {
					y -= legendPadding + legendHeight;
				} else {
					y += legendPadding;
				}
			default:
				break;
			}
			switch (this.getLegendAlignment()) {
			case AlignTop:
				y = this.getPlotAreaPadding(Side.Top) + legendPadding;
				break;
			case AlignMiddle: {
				double middle = this.getPlotAreaPadding(Side.Top)
						+ (h - this.getPlotAreaPadding(Side.Top) - this
								.getPlotAreaPadding(Side.Bottom)) / 2;
				y = (int) (middle - legendHeight / 2);
			}
				break;
			case AlignBottom:
				y = h - this.getPlotAreaPadding(Side.Bottom) - legendPadding
						- legendHeight;
				break;
			case AlignLeft:
				x = this.getPlotAreaPadding(Side.Left) + legendPadding;
				break;
			case AlignCenter: {
				double center = this.getPlotAreaPadding(Side.Left)
						+ (w - this.getPlotAreaPadding(Side.Left) - this
								.getPlotAreaPadding(Side.Right)) / 2;
				x = (int) (center - legendWidth / 2);
			}
				break;
			case AlignRight:
				x = w - this.getPlotAreaPadding(Side.Right) - legendPadding
						- legendWidth;
				break;
			default:
				break;
			}
			int xOffset = 0;
			int yOffset = 0;
			if (this.getLegendLocation() == LegendLocation.LegendOutside) {
				switch (this.getLegendSide()) {
				case Top: {
					if (this.getOrientation() == Orientation.Horizontal) {
						for (int i = this.getYAxisCount() - 1; i >= 0; --i) {
							if (this.getYAxis(i).isVisible()
									&& (this.yAxes_.get(i).location.initLoc == AxisValue.MinimumValue || this.yAxes_
											.get(i).location.initLoc == AxisValue.BothSides)) {
								yOffset = -(this.yAxes_.get(i).location.minOffset + this.yAxes_
										.get(i).calculatedWidth);
								break;
							}
						}
					} else {
						if (this.getAxis(Axis.XAxis).isVisible()
								&& (this.xAxis_.location.initLoc == AxisValue.MaximumValue || this.xAxis_.location.initLoc == AxisValue.BothSides)) {
							yOffset = -this.xAxis_.calculatedWidth;
						}
					}
					yOffset -= 5;
				}
					break;
				case Bottom: {
					if (this.getOrientation() == Orientation.Horizontal) {
						for (int i = this.getYAxisCount() - 1; i >= 0; --i) {
							if (this.getYAxis(i).isVisible()
									&& (this.yAxes_.get(i).location.initLoc == AxisValue.MaximumValue || this.yAxes_
											.get(i).location.initLoc == AxisValue.BothSides)) {
								yOffset = this.yAxes_.get(i).location.maxOffset
										+ this.yAxes_.get(i).calculatedWidth;
								break;
							}
						}
					} else {
						if (this.getAxis(Axis.XAxis).isVisible()
								&& (this.xAxis_.location.initLoc == AxisValue.MinimumValue || this.xAxis_.location.initLoc == AxisValue.BothSides)) {
							yOffset = this.xAxis_.calculatedWidth;
						}
					}
					yOffset += 5;
				}
					break;
				case Left: {
					if (this.getOrientation() == Orientation.Horizontal) {
						if (this.getAxis(Axis.XAxis).isVisible()
								&& (this.xAxis_.location.initLoc == AxisValue.MinimumValue || this.xAxis_.location.initLoc == AxisValue.BothSides)) {
							xOffset = -this.xAxis_.calculatedWidth;
						}
					} else {
						for (int i = this.getYAxisCount() - 1; i >= 0; --i) {
							if (this.getYAxis(i).isVisible()
									&& (this.yAxes_.get(i).location.initLoc == AxisValue.MinimumValue || this.yAxes_
											.get(i).location.initLoc == AxisValue.BothSides)) {
								xOffset = -(this.yAxes_.get(i).location.minOffset + this.yAxes_
										.get(i).calculatedWidth);
								break;
							}
						}
					}
					xOffset -= 5;
				}
					break;
				case Right: {
					if (this.getOrientation() == Orientation.Horizontal) {
						if (this.getAxis(Axis.XAxis).isVisible()
								&& (this.xAxis_.location.initLoc == AxisValue.MaximumValue || this.xAxis_.location.initLoc == AxisValue.BothSides)) {
							xOffset = this.xAxis_.calculatedWidth;
						}
					} else {
						for (int i = this.getYAxisCount() - 1; i >= 0; --i) {
							if (this.getYAxis(i).isVisible()
									&& (this.yAxes_.get(i).location.initLoc == AxisValue.MaximumValue || this.yAxes_
											.get(i).location.initLoc == AxisValue.BothSides)) {
								xOffset = this.yAxes_.get(i).location.maxOffset
										+ this.yAxes_.get(i).calculatedWidth;
								break;
							}
						}
					}
					xOffset += 5;
				}
					break;
				}
			} else {
				switch (this.getLegendSide()) {
				case Top:
					yOffset = 5;
					break;
				case Bottom:
					yOffset = -5;
					break;
				case Left:
					xOffset = 5;
					break;
				case Right:
					xOffset = -5;
					break;
				}
			}
			painter.setPen(this.getLegendBorder().clone());
			painter.setBrush(this.getLegendBackground());
			painter.drawRect(x + xOffset - legendPadding / 2, y + yOffset
					- legendPadding / 2, legendWidth + legendPadding,
					legendHeight + legendPadding);
			painter.setPen(new WPen());
			painter.setFont(this.getLegendFont());
			int item = 0;
			for (int i = 0; i < this.getSeries().size(); ++i) {
				if (this.getSeries().get(i).isLegendEnabled()) {
					int col = item % this.getLegendColumns();
					int row = item / this.getLegendColumns();
					double itemX = x + xOffset + col
							* this.getLegendColumnWidth().toPixels();
					double itemY = y + yOffset + row * lineHeight;
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
			final int TITLE_WIDTH = 1000;
			int titleOffset = 0;
			if (this.getOrientation() == Orientation.Horizontal) {
				for (int i = this.getYAxisCount() - 1; i >= 0; --i) {
					final WAxis yAx = this.getYAxis(i);
					if (this.yAxes_.get(i).location.initLoc == AxisValue.MinimumValue
							|| this.yAxes_.get(i).location.initLoc == AxisValue.BothSides) {
						titleOffset = this.yAxes_.get(i).location.minOffset
								+ this.yAxes_.get(i).calculatedWidth;
						break;
					}
				}
			} else {
				if (this.xAxis_.location.initLoc == AxisValue.MaximumValue
						|| this.xAxis_.location.initLoc == AxisValue.BothSides) {
					titleOffset = this.xAxis_.calculatedWidth;
				}
			}
			if (this.getLegendSide() == Side.Top
					&& this.getLegendLocation() == LegendLocation.LegendOutside) {
				titleOffset += legendHeight + legendPadding + 5;
			}
			painter.drawText(x - TITLE_WIDTH / 2,
					this.getPlotAreaPadding(Side.Top) - titleHeight
							- TITLE_PADDING - titleOffset, TITLE_WIDTH,
					titleHeight, EnumSet.of(AlignmentFlag.AlignCenter,
							AlignmentFlag.AlignTop), this.getTitle());
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
		final AxisValue location = axis.getId() == Axis.XAxis ? this.xAxis_.location.finLoc
				: this.yAxes_.get(axis.getYAxisId()).location.finLoc;
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
					if (this.xAxis_.location.finLoc == AxisValue.ZeroValue
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
							- this.yAxes_.get(axis.getYAxisId()).location.minOffset;
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
							+ this.yAxes_.get(axis.getYAxisId()).location.maxOffset;
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
							if (!EnumUtils.mask(device.getFeatures(),
									WPaintDevice.FeatureFlag.HasFontMetrics)
									.isEmpty()) {
								extraMargin = axis.calcMaxTickLabelSize(device,
										Orientation.Vertical);
							} else {
								extraMargin = 15;
							}
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
			if (this.isInteractive()) {
				final List<WCartesianChart.PenAssignment> assignment = axis
						.getId() == Axis.XAxis ? this.xAxis_.pens : this.yAxes_
						.get(axis.getYAxisId()).pens;
				if (!assignment.isEmpty()) {
					for (int i = 0; i < assignment.size(); ++i) {
						pens.add(assignment.get(i).pen.getValue());
						textPens.add(assignment.get(i).textPen.getValue());
					}
				}
			}
			WTransform transform = new WTransform();
			WRectF area = this.hv(this.chartArea_);
			if (axis.getLocation() == AxisValue.ZeroValue) {
				transform.assign(new WTransform(1, 0, 0, -1, area.getLeft(),
						area.getBottom())
						.multiply(this.xAxis_.transform)
						.multiply(this.yAxes_.get(axis.getYAxisId()).transform)
						.multiply(
								new WTransform(1, 0, 0, -1, -area.getLeft(),
										area.getBottom())));
			} else {
				if (isYAxis && this.getOrientation() == Orientation.Vertical) {
					transform.assign(new WTransform(1, 0, 0, -1, 0, area
							.getBottom()).multiply(
							this.yAxes_.get(axis.getYAxisId()).transform)
							.multiply(
									new WTransform(1, 0, 0, -1, 0, area
											.getBottom())));
				} else {
					if (isYAxis
							&& this.getOrientation() == Orientation.Horizontal) {
						transform.assign(new WTransform(0, 1, 1, 0, area
								.getLeft(), 0).multiply(
								this.yAxes_.get(axis.getYAxisId()).transform)
								.multiply(
										new WTransform(0, 1, 1, 0, 0, -area
												.getLeft())));
					} else {
						if (this.getOrientation() == Orientation.Horizontal) {
							transform.assign(new WTransform(0, 1, 1, 0, 0, area
									.getTop()).multiply(this.xAxis_.transform)
									.multiply(
											new WTransform(0, 1, 1, 0, -area
													.getTop(), 0)));
						} else {
							transform.assign(new WTransform(1, 0, 0, 1, area
									.getLeft(), 0).multiply(
									this.xAxis_.transform).multiply(
									new WTransform(1, 0, 0, 1, -area.getLeft(),
											0)));
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
		boolean isYAxis = ax.getId() != Axis.XAxis;
		if (!isYAxis && this.yAxes_.isEmpty()) {
			return;
		}
		final WAxis other = isYAxis ? this.getAxis(Axis.XAxis) : this
				.getAxis(Axis.Y1Axis);
		final WAxis.Segment s0 = other.segments_.get(0);
		final WAxis.Segment sn = other.segments_
				.get(other.segments_.size() - 1);
		double ou0 = s0.renderStart;
		double oun = sn.renderStart + sn.renderLength;
		if (!isYAxis) {
			boolean gridLines = this.getAxis(Axis.YAxis).isGridLinesEnabled();
			for (int i = 1; i < this.getYAxisCount(); ++i) {
				if (!(this.getYAxis(i).isVisible() && this.getYAxis(i)
						.isGridLinesEnabled())) {
					continue;
				}
				final WAxis.Segment s0_2 = this.getYAxis(i).segments_.get(0);
				final WAxis.Segment sn_2 = this.getYAxis(i).segments_.get(0);
				if (!gridLines || s0_2.renderStart < ou0) {
					ou0 = s0_2.renderStart;
				}
				if (!gridLines || sn_2.renderStart + sn_2.renderLength > oun) {
					oun = sn_2.renderStart + sn_2.renderLength;
				}
				gridLines = true;
			}
		}
		if (!isYAxis) {
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
		final List<WCartesianChart.PenAssignment> assignments = ax.getId() == Axis.XAxis ? this.xAxis_.pens
				: this.yAxes_.get(ax.getYAxisId()).pens;
		if (assignments.isEmpty()) {
			pens.add(ax.getGridLinesPen());
		} else {
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
				if (isYAxis) {
					u = this.chartArea_.getBottom() - u;
					gridPath.moveTo(this.hv(ou0, u));
					gridPath.lineTo(this.hv(oun, u));
				} else {
					u = this.chartArea_.getLeft() + u;
					gridPath.moveTo(this.hv(u, ou0));
					gridPath.lineTo(this.hv(u, oun));
				}
			}
			painter.strokePath(
					this.zoomRangeTransform(isYAxis ? ax.getYAxisId() : 0)
							.map(gridPath).getCrisp(), pens.get(level - 1));
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
	 * {@link WCartesianChart#zoomRangeTransform(int yAxis)
	 * zoomRangeTransform()}.
	 * <p>
	 * This method is called last by default. If you want to render other things
	 * at some other moment, you can override render(WPainter&amp;, const
	 * WRectF&amp;).
	 */
	protected void renderOther(final WPainter painter) {
		WPainterPath clipPath = new WPainterPath();
		clipPath.addRect(this.hv(this.chartArea_));
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
		final WTransform xTransform = this.xAxis_.transformHandle.getValue();
		List<WTransform> yTransforms = new ArrayList<WTransform>();
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			yTransforms.add(this.yAxes_.get(i).transformHandle.getValue());
		}
		if (!this.getAxis(Axis.XAxis).zoomRangeDirty_) {
			WPointF devicePan = new WPointF(xTransform.getDx()
					/ xTransform.getM11(), 0.0);
			WPointF modelPan = new WPointF(this.getAxis(Axis.XAxis)
					.mapFromDevice(-devicePan.getX()), 0.0);
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
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			if (!this.getYAxis(i).zoomRangeDirty_) {
				WPointF devicePan = new WPointF(0.0, yTransforms.get(i).getDy()
						/ yTransforms.get(i).getM22());
				WPointF modelPan = new WPointF(0.0, this.getYAxis(i)
						.mapFromDevice(-devicePan.getY()));
				if (yTransforms.get(i).isIdentity()) {
					this.getYAxis(i).setZoomRangeFromClient(WAxis.AUTO_MINIMUM,
							WAxis.AUTO_MAXIMUM);
				} else {
					double z = yTransforms.get(i).getM22();
					double y = modelPan.getY();
					double min = this.getYAxis(i).mapFromDevice(0.0);
					double max = this.getYAxis(i).mapFromDevice(
							this.getYAxis(i).fullRenderLength_);
					double y2 = y + (max - min) / z;
					this.getYAxis(i).setZoomRangeFromClient(y, y2);
				}
			}
		}
		if (this.curveTransforms_.size() != 0) {
			for (int i = 0; i < this.series_.size(); ++i) {
				final WDataSeries s = this.series_.get(i);
				int yAxis = s.getYAxis();
				if (yAxis >= 0 && yAxis < this.getYAxisCount()) {
					if ((s.getType() == SeriesType.LineSeries || s.getType() == SeriesType.CurveSeries)
							&& !s.isHidden()) {
						if (!s.scaleDirty_) {
							s.scale_ = this.curveTransforms_.get(s).getValue()
									.getM22();
						}
						if (!s.offsetDirty_) {
							double origin;
							if (this.getOrientation() == Orientation.Horizontal) {
								origin = this.mapToDeviceWithoutTransform(0.0,
										0.0, yAxis).getX();
							} else {
								origin = this.mapToDeviceWithoutTransform(0.0,
										0.0, yAxis).getY();
							}
							double dy = this.curveTransforms_.get(s).getValue()
									.getDy();
							double scale = this.curveTransforms_.get(s)
									.getValue().getM22();
							double offset = -dy + origin * (1 - scale)
									+ this.getYAxis(yAxis).mapToDevice(0.0, 0);
							if (this.getOrientation() == Orientation.Horizontal) {
								s.offset_ = -this.getYAxis(yAxis)
										.mapFromDevice(offset);
							} else {
								s.offset_ = this.getYAxis(yAxis).mapFromDevice(
										offset);
							}
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
	protected WTransform zoomRangeTransform(int yAxis) {
		return this.zoomRangeTransform(this.xAxis_.transform,
				this.yAxes_.get(yAxis).transform);
	}

	/**
	 * Returns the current zoom range transform.
	 * <p>
	 * Returns {@link #zoomRangeTransform(int yAxis) zoomRangeTransform(0)}
	 */
	protected final WTransform zoomRangeTransform() {
		return zoomRangeTransform(0);
	}

	private List<WAxis> collectYAxesAtLocation(AxisValue side) {
		List<WAxis> result = new ArrayList<WAxis>();
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			final WAxis axis = this.yAxes_.get(i).axis;
			if (!axis.isVisible()) {
				continue;
			}
			if (axis.getLocation() == AxisValue.ZeroValue) {
				if (side == AxisValue.MinimumValue) {
					if (this.xAxis_.axis.segments_.get(0).renderMinimum >= 0
							|| !this.xAxis_.axis.isOnAxis(0.0)
							&& this.xAxis_.axis.segments_
									.get(this.xAxis_.axis.segments_.size() - 1).renderMaximum > 0) {
						result.add(axis);
					}
				} else {
					if (side == AxisValue.MaximumValue) {
						if (this.xAxis_.axis.segments_
								.get(this.xAxis_.axis.segments_.size() - 1).renderMaximum <= 0) {
							result.add(axis);
						}
					}
				}
			}
		}
		for (int i = 0; i < this.yAxes_.size(); ++i) {
			final WAxis axis = this.yAxes_.get(i).axis;
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
		this.clearPensForAxis(Axis.XAxis, -1);
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			this.clearPensForAxis(Axis.YAxis, i);
		}
	}

	private void clearPensForAxis(Axis ax, int yAxis) {
		final List<WCartesianChart.PenAssignment> assignments = ax == Axis.XAxis ? this.xAxis_.pens
				: this.yAxes_.get(yAxis).pens;
		for (int i = 0; i < assignments.size(); ++i) {
			final WCartesianChart.PenAssignment assignment = assignments.get(i);
			this.freePens_.add(assignment.pen);
			this.freePens_.add(assignment.textPen);
			this.freePens_.add(assignment.gridPen);
		}
		assignments.clear();
	}

	private void createPensForAxis(Axis ax, int yAxis) {
		final WAxis axis = ax == Axis.XAxis ? this.getAxis(Axis.XAxis) : this
				.getYAxis(yAxis);
		if (!axis.isVisible() || axis.getScale() == AxisScale.LogScale) {
			return;
		}
		final WCartesianChart.AxisStruct axisStruct = ax == Axis.XAxis ? this.xAxis_
				: this.yAxes_.get(yAxis);
		double zoom = axis.getZoom();
		if (zoom > axis.getMaxZoom()) {
			zoom = axis.getMaxZoom();
		}
		int level = toZoomLevel(zoom);
		List<WCartesianChart.PenAssignment> assignments = new ArrayList<WCartesianChart.PenAssignment>();
		boolean stop = false;
		for (int i = 1; !stop; ++i) {
			if (this.isOnDemandLoadingEnabled() && i > level + 1) {
				break;
			}
			double z = Math.pow(2.0, i - 1);
			stop = z >= axis.getMaxZoom();
			WJavaScriptHandle<WPen> pen = null;
			if (this.freePens_.size() > 0) {
				pen = this.freePens_.get(this.freePens_.size() - 1);
				this.freePens_.remove(this.freePens_.size() - 1);
			} else {
				pen = this.createJSPen();
			}
			WPen p = axis.getPen().clone();
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
			p = axis.getTextPen().clone();
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
			p = axis.getGridLinesPen().clone();
			p.setColor(new WColor(p.getColor().getRed(), p.getColor()
					.getGreen(), p.getColor().getBlue(), i == level ? p
					.getColor().getAlpha() : 0));
			gridPen.setValue(p);
			assignments.add(new WCartesianChart.PenAssignment(pen, textPen,
					gridPen));
		}
		Utils.copyList(assignments, axisStruct.pens);
	}

	String getCObjJsRef() {
		return this.getJsRef() + ".wtCObj";
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
		this.updateJSPensForAxis(js, Axis.XAxis, -1);
		js.append(",y:[");
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			if (i != 0) {
				js.append(',');
			}
			this.updateJSPensForAxis(js, Axis.YAxis, i);
		}
		js.append("]},");
		js.append("penAlpha:{x:[");
		js.append(this.getAxis(Axis.XAxis).getPen().getColor().getAlpha())
				.append(',');
		js.append(this.getAxis(Axis.XAxis).getTextPen().getColor().getAlpha())
				.append(',');
		js.append(this.getAxis(Axis.XAxis).getGridLinesPen().getColor()
				.getAlpha());
		js.append("],y:[");
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			if (i != 0) {
				js.append(',');
			}
			js.append('[')
					.append(this.getYAxis(i).getPen().getColor().getAlpha())
					.append(',');
			js.append(this.getYAxis(i).getTextPen().getColor().getAlpha())
					.append(',');
			js.append(this.getYAxis(i).getGridLinesPen().getColor().getAlpha())
					.append(']');
		}
		js.append("]},");
	}

	private void updateJSPensForAxis(final StringBuilder js, Axis axis,
			int yAxis) {
		final List<WCartesianChart.PenAssignment> pens = axis == Axis.XAxis ? this.xAxis_.pens
				: this.yAxes_.get(yAxis).pens;
		js.append("[");
		for (int i = 0; i < pens.size(); ++i) {
			if (i != 0) {
				js.append(",");
			}
			final WCartesianChart.PenAssignment assignment = pens.get(i);
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

	private WTransform zoomRangeTransform(final WTransform xTransform,
			final WTransform yTransform) {
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
		double offset = this.getYAxis(yAxis).mapToDevice(0.0, 0)
				- this.getYAxis(yAxis).mapToDevice(series.getOffset(), 0);
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
			if (xZoom < this.getAxis(Axis.XAxis).getMinZoom()) {
				xZoom = this.getAxis(Axis.XAxis).getMinZoom();
			}
			xTransform.assign(new WTransform(xZoom, 0, 0, 1, xZoom * xPan, 0));
		} else {
			double xZoom = this.getAxis(Axis.XAxis).getMinZoom();
			xTransform.assign(new WTransform(xZoom, 0, 0, 1, 0, 0));
		}
		List<WTransform> yTransforms = new ArrayList<WTransform>();
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			if (this.getYAxis(i).zoomMin_ != WAxis.AUTO_MINIMUM
					|| this.getYAxis(i).zoomMax_ != WAxis.AUTO_MAXIMUM) {
				double yPan = -this.getYAxis(i).mapToDevice(
						this.getYAxis(i).getPan(), 0);
				double yZoom = this.getYAxis(i).getZoom();
				if (yZoom > this.getYAxis(i).getMaxZoom()) {
					yZoom = this.getYAxis(i).getMaxZoom();
				}
				if (yZoom < this.getYAxis(i).getMinZoom()) {
					yZoom = this.getYAxis(i).getMinZoom();
				}
				yTransforms
						.add(new WTransform(1, 0, 0, yZoom, 0, yZoom * yPan));
			} else {
				double yZoom = this.getYAxis(i).getMinZoom();
				yTransforms.add(new WTransform(1, 0, 0, yZoom, 0, 0));
			}
		}
		WRectF chartArea = this.hv(this.getInsideChartArea());
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			WRectF transformedArea = this.zoomRangeTransform(xTransform,
					yTransforms.get(i)).map(chartArea);
			if (transformedArea.getLeft() > chartArea.getLeft()) {
				double diff = chartArea.getLeft() - transformedArea.getLeft();
				if (this.getOrientation() == Orientation.Vertical) {
					xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
							.multiply(xTransform));
				} else {
					yTransforms.set(i, new WTransform(1, 0, 0, 1, 0, diff)
							.multiply(yTransforms.get(i)));
				}
				transformedArea = this.zoomRangeTransform(xTransform,
						yTransforms.get(i)).map(chartArea);
			}
			if (transformedArea.getRight() < chartArea.getRight()) {
				double diff = chartArea.getRight() - transformedArea.getRight();
				if (this.getOrientation() == Orientation.Vertical) {
					xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
							.multiply(xTransform));
				} else {
					yTransforms.set(i, new WTransform(1, 0, 0, 1, 0, diff)
							.multiply(yTransforms.get(i)));
				}
				transformedArea = this.zoomRangeTransform(xTransform,
						yTransforms.get(i)).map(chartArea);
			}
			if (transformedArea.getTop() > chartArea.getTop()) {
				double diff = chartArea.getTop() - transformedArea.getTop();
				if (this.getOrientation() == Orientation.Vertical) {
					yTransforms.set(i, new WTransform(1, 0, 0, 1, 0, -diff)
							.multiply(yTransforms.get(i)));
				} else {
					xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
							.multiply(xTransform));
				}
				transformedArea = this.zoomRangeTransform(xTransform,
						yTransforms.get(i)).map(chartArea);
			}
			if (transformedArea.getBottom() < chartArea.getBottom()) {
				double diff = chartArea.getBottom()
						- transformedArea.getBottom();
				if (this.getOrientation() == Orientation.Vertical) {
					yTransforms.set(i, new WTransform(1, 0, 0, 1, 0, -diff)
							.multiply(yTransforms.get(i)));
				} else {
					xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
							.multiply(xTransform));
				}
				transformedArea = this.zoomRangeTransform(xTransform,
						yTransforms.get(i)).map(chartArea);
			}
		}
		this.xAxis_.transformHandle.setValue(xTransform);
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			this.yAxes_.get(i).transformHandle.setValue(yTransforms.get(i));
		}
		this.getAxis(Axis.XAxis).zoomRangeDirty_ = false;
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			this.getYAxis(i).zoomRangeDirty_ = false;
		}
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
		if (this.isOnDemandLoadingEnabled()) {
			this.update();
		}
		this.getAxis(Axis.XAxis)
				.zoomRangeChanged()
				.trigger(this.getAxis(Axis.XAxis).getZoomMinimum(),
						this.getAxis(Axis.XAxis).getZoomMaximum());
	}

	private void yTransformChanged(int yAxis) {
		if (this.isOnDemandLoadingEnabled()) {
			this.update();
		}
		this.getYAxis(yAxis)
				.zoomRangeChanged()
				.trigger(this.getYAxis(yAxis).getZoomMinimum(),
						this.getYAxis(yAxis).getZoomMaximum());
	}

	private void jsSeriesSelected(double x, double y) {
		if (!this.isSeriesSelectionEnabled()) {
			return;
		}
		double smallestSqDistance = Double.POSITIVE_INFINITY;
		WDataSeries closestSeries = null;
		WPointF closestPointPx = new WPointF();
		WPointF closestPointBeforeSeriesTransform = new WPointF();
		for (int i = 0; i < this.series_.size(); ++i) {
			final WDataSeries series = this.series_.get(i);
			if (!series.isHidden()
					&& (series.getType() == SeriesType.LineSeries || series
							.getType() == SeriesType.CurveSeries)) {
				WTransform transform = this.zoomRangeTransform(
						this.xAxis_.transformHandle.getValue(), this.yAxes_
								.get(series.getYAxis()).transformHandle
								.getValue());
				WPointF p = transform.getInverted().map(new WPointF(x, y));
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
			WTransform transform = this
					.zoomRangeTransform(this.xAxis_.transformHandle.getValue(),
							this.yAxes_
									.get(closestSeries != null ? closestSeries
											.getYAxis() : 0).transformHandle
									.getValue());
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
		double px = this
				.zoomRangeTransform(this.xAxis_.transformHandle.getValue(),
						new WTransform()).getInverted()
				.map(new WPointF(x, 0.0)).getX();
		double rx = MarkerMatchIterator.MATCH_RADIUS
				/ this.xAxis_.transformHandle.getValue().getM11();
		List<Double> pys = new ArrayList<Double>();
		List<Double> rys = new ArrayList<Double>();
		for (int i = 0; i < this.getYAxisCount(); ++i) {
			WPointF p = this
					.zoomRangeTransform(new WTransform(),
							this.yAxes_.get(i).transformHandle.getValue())
					.getInverted().map(new WPointF(0.0, y));
			pys.add(p.getY());
			rys.add(MarkerMatchIterator.MATCH_RADIUS
					/ this.yAxes_.get(i).transformHandle.getValue().getM22());
		}
		MarkerMatchIterator iterator = new MarkerMatchIterator(this, px, pys,
				rx, rys);
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
				WPointF p = this
						.zoomRangeTransform(
								this.xAxis_.transformHandle.getValue(),
								this.yAxes_.get(this.barTooltips_.get(btt).series
										.getYAxis()).transformHandle.getValue())
						.getInverted().map(new WPointF(x, y));
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
		final WAxis.Segment xs = xAxis.segments_.get(0);
		double xRenderStart = xAxis.isInverted() ? xAxis.mapToDevice(
				xs.renderMaximum, 0) : xs.renderStart;
		double xRenderEnd = xAxis.isInverted() ? xAxis.mapToDevice(
				xs.renderMinimum, 0) : xs.renderStart + xs.renderLength;
		double yRenderStart = 0;
		double yRenderEnd = 0;
		if (this.getYAxisCount() >= 1) {
			final WAxis yAxis = this.getAxis(Axis.YAxis);
			final WAxis.Segment ys = yAxis.segments_.get(0);
			yRenderStart = yAxis.isInverted() ? yAxis.mapToDevice(
					ys.renderMaximum, 0) : ys.renderStart;
			yRenderEnd = yAxis.isInverted() ? yAxis.mapToDevice(
					ys.renderMinimum, 0) : ys.renderStart + ys.renderLength;
		}
		double x1 = this.chartArea_.getLeft() + xRenderStart;
		double x2 = this.chartArea_.getLeft() + xRenderEnd;
		double y1 = this.chartArea_.getBottom() - yRenderEnd;
		double y2 = this.chartArea_.getBottom() - yRenderStart;
		return new WRectF(x1, y1, x2 - x1, y2 - y1);
	}

	private int calcAxisSize(final WAxis axis, WPaintDevice device) {
		if (!EnumUtils.mask(device.getFeatures(),
				WPaintDevice.FeatureFlag.HasFontMetrics).isEmpty()) {
			if (this.getOrientation() == Orientation.Horizontal != (axis
					.getId() == Axis.XAxis)) {
				WMeasurePaintDevice md = new WMeasurePaintDevice(device);
				double h = TICK_LENGTH;
				h += axis.calcMaxTickLabelSize(md, Orientation.Vertical);
				if (!(axis.getTitle().length() == 0)) {
					h += axis.calcTitleSize(md, Orientation.Vertical);
				}
				return (int) Math.ceil(h);
			} else {
				WMeasurePaintDevice md = new WMeasurePaintDevice(device);
				double w = TICK_LENGTH;
				w += axis.calcMaxTickLabelSize(md, Orientation.Horizontal);
				if (!(axis.getTitle().length() == 0)
						&& axis.getTitleOrientation() == Orientation.Vertical) {
					w += axis.calcTitleSize(md, Orientation.Vertical) + 10;
				}
				return (int) Math.ceil(w);
			}
		} else {
			if (this.getOrientation() == Orientation.Horizontal != (axis
					.getId() == Axis.XAxis)) {
				return TICK_LENGTH + 20
						+ ((axis.getTitle().length() == 0) ? 0 : 20);
			} else {
				return TICK_LENGTH + 30
						+ ((axis.getTitle().length() == 0) ? 0 : 15);
			}
		}
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		if (app != null && (this.isInteractive() || this.hasDeferredToolTips_)) {
			app.loadJavaScript("js/ChartCommon.js", wtjs2());
			app.doJavaScript(
					"if (!Wt3_4_1.chartCommon) {Wt3_4_1.chartCommon = new "
							+ "Wt3_4_1.ChartCommon(" + app.getJavaScriptClass()
							+ "); }", false);
			app.loadJavaScript("js/WCartesianChart.js", wtjs1());
			this.jsDefined_ = true;
		} else {
			this.jsDefined_ = false;
		}
	}

	void drawMarker(final WDataSeries series, MarkerType marker,
			final WPainterPath result) {
		final double size = 6.0;
		final double hsize = size / 2;
		switch (marker) {
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
				"function(B){function F(a,b,c,d){function e(m){return c?b[m]:b[n-1-m]}function i(m){for(;e(m)[2]===y||e(m)[2]===C;)m--;return m}var j=k;if(d)j=l;var n=b.length;d=Math.floor(n/2);d=i(d);var t=0,r=n,f=false;if(e(0)[j]>a)return c?-1:n;if(e(n-1)[j]<a)return c?n:-1;for(;!f;){var g=d+1;if(g<n&&(e(g)[2]===y||e(g)[2]===C))g+=2;if(e(d)[j]>a){r=d;d=Math.floor((r+t)/2);d=i(d)}else if(e(d)[j]===a)f=true;else if(g<n&&e(g)[j]>a)f=true;else if(g<n&&e(g)[j]=== a){d=g;f=true}else{t=d;d=Math.floor((r+t)/2);d=i(d)}}return c?d:n-1-d}function H(a,b){return b[0][a]<b[b.length-1][a]}var y=2,C=3,k=0,l=1,G=this;B=B.WT.gfxUtils;var z=B.rect_top,A=B.rect_bottom,v=B.rect_left,D=B.rect_right,I=B.transform_mult;this.findClosestPoint=function(a,b,c){var d=k;if(c)d=l;var e=H(d,b);c=F(a,b,e,c);if(c<0)c=0;if(c>=b.length)return[b[b.length-1][k],b[b.length-1][l]];if(c>=b.length)c=b.length-2;if(b[c][d]===a)return[b[c][k],b[c][l]];var i=e?c+1:c-1;if(e&&b[i][2]==y)i+=2;if(!e&& i<0)return[b[c][k],b[c][l]];if(!e&&i>0&&b[i][2]==C)i-=2;e=Math.abs(a-b[c][d]);a=Math.abs(b[i][d]-a);return e<a?[b[c][k],b[c][l]]:[b[i][k],b[i][l]]};this.minMaxY=function(a,b){b=b?k:l;for(var c=a[0][b],d=a[0][b],e=1;e<a.length;++e)if(a[e][2]!==y&&a[e][2]!==C&&a[e][2]!==5){if(a[e][b]>d)d=a[e][b];if(a[e][b]<c)c=a[e][b]}return[c,d]};this.projection=function(a,b){var c=Math.cos(a);a=Math.sin(a);var d=c*a,e=-b[0]*c-b[1]*a;return[c*c,d,d,a*a,c*e+b[0],a*e+b[1]]};this.distanceSquared=function(a,b){a=[b[k]- a[k],b[l]-a[l]];return a[k]*a[k]+a[l]*a[l]};this.distanceLessThanRadius=function(a,b,c){return c*c>=G.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var c;if(a.x!==undefined){c=a.x;a=a.y}else{c=a[0];a=a[1]}return c>=v(b)&&c<=D(b)&&a>=z(b)&&a<=A(b)};this.toDisplayCoord=function(a,b,c,d,e){if(c){a=[(a[k]-e[0])/e[2],(a[l]-e[1])/e[3]];d=[d[0]+a[l]*d[2],d[1]+a[k]*d[3]]}else{a=[(a[k]-e[0])/e[2],1-(a[l]-e[1])/e[3]];d=[d[0]+ a[k]*d[2],d[1]+a[l]*d[3]]}return I(b,d)};this.findYRange=function(a,b,c,d,e,i,j,n,t){if(a.length!==0){var r=G.toDisplayCoord([c,0],[1,0,0,1,0,0],e,i,j),f=G.toDisplayCoord([d,0],[1,0,0,1,0,0],e,i,j),g=e?l:k,m=e?k:l,o=H(g,a),h=F(r[g],a,o,e),p=F(f[g],a,o,e),q,s,w=Infinity,x=-Infinity,E=h===p&&h===a.length||h===-1&&p===-1;if(!E){if(o)if(h<0)h=0;else{h++;if(a[h]&&a[h][2]===y)h+=2}else if(h>=a.length-1)h=a.length-2;if(!o&&p<0)p=0;for(q=Math.min(h,p);q<=Math.max(h,p)&&q<a.length;++q)if(a[q][2]!==y&&a[q][2]!== C){if(a[q][m]<w)w=a[q][m];if(a[q][m]>x)x=a[q][m]}if(o&&h>0||!o&&h<a.length-1){if(o){s=h-1;if(a[s]&&a[s][2]===C)s-=2}else{s=h+1;if(a[s]&&a[s][2]===y)s+=2}q=(r[g]-a[s][g])/(a[h][g]-a[s][g]);h=a[s][m]+q*(a[h][m]-a[s][m]);if(h<w)w=h;if(h>x)x=h}if(o&&p<a.length-1||!o&&p>0){if(o){o=p+1;if(a[o][2]===y)o+=2}else{o=p-1;if(a[o][2]===C)o-=2}q=(f[g]-a[p][g])/(a[o][g]-a[p][g]);h=a[p][m]+q*(a[o][m]-a[p][m]);if(h<w)w=h;if(h>x)x=h}}var u;a=j[2]/(d-c);c=e?2:3;if(!E){u=i[c]/(x-w);u=i[c]/(i[c]/u+20);if(u>t.y[b])u=t.y[b]; if(u<n.y[b])u=n.y[b]}b=e?[r[l]-z(i),!E?(w+x)/2-i[2]/u/2-v(i):0]:[r[k]-v(i),!E?-((w+x)/2+i[3]/u/2-A(i)):0];return{xZoom:a,yZoom:u,panPoint:b}}};this.matchesXAxis=function(a,b,c,d,e){if(e){if(b<z(c)||b>A(c))return false;if((d.side===\"min\"||d.side===\"both\")&&a>=v(c)-d.width&&a<=v(c))return true;if((d.side===\"max\"||d.side===\"both\")&&a<=D(c)+d.width&&a>=D(c))return true}else{if(a<v(c)||a>D(c))return false;if((d.side===\"min\"||d.side===\"both\")&&b<=A(c)+d.width&&b>=A(c))return true;if((d.side===\"max\"||d.side=== \"both\")&&b>=z(c)-d.width&&b<=z(c))return true}return false};this.matchYAxis=function(a,b,c,d,e){function i(){return d.length}function j(g){return d[g].side}function n(g){return d[g].width}function t(g){return d[g].minOffset}function r(g){return d[g].maxOffset}if(e){if(a<v(c)||a>D(c))return-1}else if(b<z(c)||b>A(c))return-1;for(var f=0;f<i();++f)if(e)if((j(f)===\"min\"||j(f)===\"both\")&&b>=z(c)-t(f)-n(f)&&b<=z(c)-t(f))return f;else{if((j(f)===\"max\"||j(f)===\"both\")&&b>=A(c)+r(f)&&b<=A(c)+r(f)+n(f))return f}else if((j(f)=== \"min\"||j(f)===\"both\")&&a>=v(c)-t(f)-n(f)&&a<=v(c)-t(f))return f;else if((j(f)===\"max\"||j(f)===\"both\")&&a>=D(c)+r(f)&&a<=D(c)+r(f)+n(f))return f;return-1}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(wa,J,A,l){function N(a){return a===undefined}function o(a){return l.modelAreas[a]}function V(){return l.followCurve}function xa(){return l.crosshair||V()!==-1}function B(){return l.isHorizontal}function i(){return l.xTransform}function g(a){return l.yTransforms[a]}function h(){return l.area}function n(){return l.insideArea}function da(a){return N(a)?l.series:l.series[a]}function ea(a){return da(a).transform}function kb(a){return B()? w([0,1,1,0,0,0],w(ea(a),[0,1,1,0,0,0])):ea(a)}function Pa(a){return da(a).curve}function P(a){return da(a).axis}function lb(){return l.seriesSelection}function mb(){return l.sliders}function nb(){return l.hasToolTips}function ob(){return l.coordinateOverlayPadding}function Ga(){return l.curveManipulation}function Qa(){return l.minZoom.x}function pb(a){return l.minZoom.y[a]}function fa(){return l.maxZoom.x}function T(a){return l.maxZoom.y[a]}function K(){return l.pens}function qb(){return l.penAlpha} function W(){return l.selectedCurve}function ya(a){a.preventDefault&&a.preventDefault()}function ga(a,b){J.addEventListener(a,b)}function X(a,b){J.removeEventListener(a,b)}function y(a){return a.length}function M(){return y(l.yTransforms)}function Bb(){if(l.notifyTransform.x)return true;for(var a=0;a<M();++a)if(l.notifyTransform.y[a])return true;return false}function Q(){return l.crosshairAxis}function bb(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"} function Ra(){if(p){if(p.tooltipTimeout){clearTimeout(p.tooltipTimeout);p.tooltipTimeout=null}if(!p.overTooltip)if(p.tooltipOuterDiv){document.body.removeChild(p.tooltipOuterDiv);p.toolTipEl=null;p.tooltipOuterDiv=null}}}function Ha(){if(Bb()){if(Sa){window.clearTimeout(Sa);Sa=null}Sa=setTimeout(function(){if(l.notifyTransform.x&&!rb(cb,i())){wa.emit(A.widget,\"xTransformChanged\");ka(cb,i())}for(var a=0;a<M();++a)if(l.notifyTransform.y[a]&&!rb(Ta[a],g(a))){wa.emit(A.widget,\"yTransformChanged\"+a);ka(Ta[a], g(a))}},Cb)}}function la(a){if(a===undefined)a=0;var b,c;if(B()){b=q(h());c=x(h());return w([0,1,1,0,b,c],w(i(),w(g(a),[0,1,1,0,-c,-b])))}else{b=q(h());c=z(h());return w([1,0,0,-1,b,c],w(i(),w(g(a),[1,0,0,-1,-b,c])))}}function F(a){return w(la(a),n())}function ma(a,b,c){if(N(c))c=false;a=c?a:w(Ia(la(b)),a);a=B()?[(a[u]-h()[1])/h()[3],(a[v]-h()[0])/h()[2]]:[(a[v]-h()[0])/h()[2],1-(a[u]-h()[1])/h()[3]];return[o(b)[0]+a[v]*o(b)[2],o(b)[1]+a[u]*o(b)[3]]}function Ua(a,b,c){if(N(c))c=false;return Y.toDisplayCoord(a, c?[1,0,0,1,0,0]:la(b),B(),h(),o(b))}function Ja(){var a,b;if(B()){a=(ma([0,x(h())],0)[0]-o(0)[0])/o(0)[2];b=(ma([0,z(h())],0)[0]-o(0)[0])/o(0)[2]}else{a=(ma([q(h()),0],0)[0]-o(0)[0])/o(0)[2];b=(ma([s(h()),0],0)[0]-o(0)[0])/o(0)[2]}var c;for(c=0;c<y(mb());++c){var d=$(\"#\"+mb()[c]);if(d)(d=d.data(\"sobj\"))&&d.changeRange(a,b)}}function Z(){Ra();if(nb()&&p.tooltipPosition)p.tooltipTimeout=setTimeout(function(){sb()},tb);na&&ub(function(){A.repaint();xa()&&db()})}function db(){if(na){var a=I.getContext(\"2d\"); a.clearRect(0,0,I.width,I.height);a.save();a.beginPath();a.moveTo(q(h()),x(h()));a.lineTo(s(h()),x(h()));a.lineTo(s(h()),z(h()));a.lineTo(q(h()),z(h()));a.closePath();a.clip();var b=w(Ia(la(Q())),C),c=C[v],d=C[u];if(V()!==-1){b=Db(B()?b[u]:b[v],Pa(V()),B());d=w(la(P(V())),w(kb(V()),b));c=d[v];d=d[u];C[v]=c;C[u]=d}b=B()?[(b[u]-h()[1])/h()[3],(b[v]-h()[0])/h()[2]]:[(b[v]-h()[0])/h()[2],1-(b[u]-h()[1])/h()[3]];b=V()!==-1?[o(P(V()))[0]+b[v]*o(P(V()))[2],o(P(V()))[1]+b[u]*o(P(V()))[3]]:[o(Q())[0]+b[v]* o(Q())[2],o(Q())[1]+b[u]*o(Q())[3]];a.fillStyle=a.strokeStyle=l.crosshairColor;a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var e=b[0].toFixed(2);b=b[1].toFixed(2);if(e===\"-0.00\")e=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+e+\",\"+b+\")\",s(h())-ob()[0],x(h())+ob()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(x(h()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(z(h()))+0.5);a.moveTo(Math.floor(q(h()))+0.5,Math.floor(d)+0.5);a.lineTo(Math.floor(s(h()))+ 0.5,Math.floor(d)+0.5);a.stroke();a.restore()}}function Eb(a){return x(a)<=x(n())+Va&&z(a)>=z(n())-Va&&q(a)<=q(n())+Va&&s(a)>=s(n())-Va}function ha(a){for(var b=0;b<M();++b){var c=F(b);if(B())if(a===za)a=Aa;else if(a===Aa)a=za;if(N(a)||a===za)if(i()[0]<1){i()[0]=1;c=F(b)}if(N(a)||a===Aa)if(g(b)[3]<1){g(b)[3]=1;c=F(b)}if(N(a)||a===za){if(q(c)>q(n())){c=q(n())-q(c);if(B())g(b)[5]=g(b)[5]+c;else i()[4]=i()[4]+c;c=F(b)}if(s(c)<s(n())){c=s(n())-s(c);if(B())g(b)[5]=g(b)[5]+c;else i()[4]=i()[4]+c;c=F(b)}}if(N(a)|| a===Aa){if(x(c)>x(n())){c=x(n())-x(c);if(B())i()[4]=i()[4]+c;else g(b)[5]=g(b)[5]-c;c=F(b)}if(z(c)<z(n())){c=z(n())-z(c);if(B())i()[4]=i()[4]+c;else g(b)[5]=g(b)[5]-c;F(b)}}}Ha()}function sb(){p.toolTipEl||wa.emit(A.widget,\"loadTooltip\",p.tooltipPosition[v],p.tooltipPosition[u])}function Fb(){if(xa()&&(N(I)||A.canvas.width!==I.width||A.canvas.height!==I.height)){if(I){I.parentNode.removeChild(I);delete J.wtOObj;I=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",A.canvas.width); a.setAttribute(\"height\",A.canvas.height);a.style.position=\"absolute\";a.style.display=\"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}A.canvas.parentNode.appendChild(a);I=a;J.wtOObj=I}else if(!N(I)&&!xa()){I.parentNode.removeChild(I);delete J.wtOObj;I=undefined}C=[(q(h())+s(h()))/2,(x(h())+z(h()))/2]}function vb(){return I?I:A.canvas}function eb(a,b){if(Ba){var c=Date.now();if(N(b))b=c-oa;var d={x:0,y:0}, e;if(G)e=F(0);else if(t===-1){e=F(0);for(var f=1;f<M();++f)e=Wa(e,F(f))}else e=F(t);f=Gb;if(b>2*Ka){na=false;var j=Math.floor(b/Ka-1),m;for(m=0;m<j;++m){eb(a,Ka);if(!Ba){na=true;Z();return}}b-=j*Ka;na=true}if(k.x===Infinity||k.x===-Infinity)k.x=k.x>0?pa:-pa;if(isFinite(k.x)){k.x/=1+wb*b;e[0]+=k.x*b;if(q(e)>q(n())){k.x+=-f*(q(e)-q(n()))*b;k.x*=0.7}else if(s(e)<s(n())){k.x+=-f*(s(e)-s(n()))*b;k.x*=0.7}if(Math.abs(k.x)<fb)if(q(e)>q(n()))k.x=fb;else if(s(e)<s(n()))k.x=-fb;if(Math.abs(k.x)>pa)k.x=(k.x> 0?1:-1)*pa;d.x=k.x*b}if(k.y===Infinity||k.y===-Infinity)k.y=k.y>0?pa:-pa;if(isFinite(k.y)){k.y/=1+wb*b;e[1]+=k.y*b;if(x(e)>x(n())){k.y+=-f*(x(e)-x(n()))*b;k.y*=0.7}else if(z(e)<z(n())){k.y+=-f*(z(e)-z(n()))*b;k.y*=0.7}if(Math.abs(k.y)<0.001)if(x(e)>x(n()))k.y=0.001;else if(z(e)<z(n()))k.y=-0.001;if(Math.abs(k.y)>pa)k.y=(k.y>0?1:-1)*pa;d.y=k.y*b}if(G)e=F(0);else if(t===-1){e=F(0);for(f=1;f<M();++f)e=Wa(e,F(f))}else e=F(t);aa(d,Ca,t,G);if(G)a=F(0);else if(t===-1){a=F(0);for(f=1;f<M();++f)a=Wa(a,F(f))}else a= F(t);if(q(e)>q(n())&&q(a)<=q(n())){k.x=0;aa({x:-d.x,y:0},Ca,t,G);ha(za)}if(s(e)<s(n())&&s(a)>=s(n())){k.x=0;aa({x:-d.x,y:0},Ca,t,G);ha(za)}if(x(e)>x(n())&&x(a)<=x(n())){k.y=0;aa({x:0,y:-d.y},Ca,t,G);ha(Aa)}if(z(e)<z(n())&&z(a)>=z(n())){k.y=0;aa({x:0,y:-d.y},Ca,t,G);ha(Aa)}if(Math.abs(k.x)<xb&&Math.abs(k.y)<xb&&Eb(a)){ha();Ba=false;D=null;k.x=0;k.y=0;oa=null;r=[]}else{oa=c;na&&Xa(eb)}}}function Ya(){var a,b,c=yb(i()[0])-1;if(i()[0]==fa())c=y(K().x)-1;if(c>=y(K().x))c=y(K().x)-1;for(a=0;a<y(K().x);++a)if(c=== a)for(b=0;b<y(K().x[a]);++b)K().x[a][b].color[3]=qb().x[b];else for(b=0;b<y(K().x[a]);++b)K().x[a][b].color[3]=0;for(c=0;c<y(K().y);++c){var d=yb(g(c)[3])-1;if(g(c)[3]==T(c))d=y(K().y[c])-1;if(d>=y(K().y[c]))d=y(K().y[c])-1;for(a=0;a<y(K().y[c]);++a)if(d===a)for(b=0;b<y(K().y[c][a]);++b)K().y[c][a][b].color[3]=qb().y[c][b];else for(b=0;b<y(K().y[c][a]);++b)K().y[c][a][b].color[3]=0}}function aa(a,b,c,d){if(N(b))b=0;if(N(c))c=-1;if(N(d))d=false;var e=ma(C,Q());if(B())a={x:a.y,y:-a.x};if(b&Ca){if(d)i()[4]= i()[4]+a.x;else if(c===-1){i()[4]=i()[4]+a.x;for(b=0;b<M();++b)g(b)[5]=g(b)[5]-a.y}else g(c)[5]=g(c)[5]-a.y;Ha()}else if(b&zb){var f;if(d)f=F(0);else if(c===-1){f=F(0);for(b=1;b<M();++b)f=Wa(f,F(b))}else f=F(c);if(q(f)>q(n())){if(a.x>0)a.x/=1+(q(f)-q(n()))*Za}else if(s(f)<s(n()))if(a.x<0)a.x/=1+(s(n())-s(f))*Za;if(x(f)>x(n())){if(a.y>0)a.y/=1+(x(f)-x(n()))*Za}else if(z(f)<z(n()))if(a.y<0)a.y/=1+(z(n())-z(f))*Za;if(d)i()[4]=i()[4]+a.x;else if(c===-1){i()[4]=i()[4]+a.x;for(b=0;b<M();++b)g(b)[5]=g(b)[5]- a.y}else g(c)[5]=g(c)[5]-a.y;if(c===-1)C[v]+=a.x;d||(C[u]+=a.y);Ha()}else{if(d)i()[4]=i()[4]+a.x;else if(c===-1){i()[4]=i()[4]+a.x;for(b=0;b<M();++b)g(b)[5]=g(b)[5]-a.y}else g(c)[5]=g(c)[5]-a.y;if(c===-1)C[v]+=a.x;d||(C[u]+=a.y);ha()}a=Ua(e,Q());C[v]=a[v];C[u]=a[u];Z();Ja()}function La(a,b,c,d,e){if(N(d))d=-1;if(N(e))e=false;var f=ma(C,Q());a=B()?[a.y-x(h()),a.x-q(h())]:w(Ia([1,0,0,-1,q(h()),z(h())]),[a.x,a.y]);var j=a[0];a=a[1];var m=Math.pow(1.2,B()?c:b);b=Math.pow(1.2,B()?b:c);if(i()[0]*m>fa())m= fa()/i()[0];if(i()[0]*m<Qa())m=Qa()/i()[0];if(e){if(m<1||i()[0]!==fa())qa(i(),w([m,0,0,1,j-m*j,0],i()))}else if(d===-1){if(m<1||i()[0]!==fa())qa(i(),w([m,0,0,1,j-m*j,0],i()));for(d=0;d<M();++d){e=b;if(g(d)[3]*b>T(d))e=T(d)/g(d)[3];if(e<1||g(d)[3]!==T(d))qa(g(d),w([1,0,0,e,0,a-e*a],g(d)))}}else{if(g(d)[3]*b>T(d))b=T(d)/g(d)[3];if(b<1||g(d)[3]!=T(d))qa(g(d),w([1,0,0,b,0,a-b*a],g(d)))}ha();f=Ua(f,Q());C[v]=f[v];C[u]=f[u];Ya();Z();Ja()}J.wtCObj=this;var ia=this,E=wa.WT;ia.config=l;var H=E.gfxUtils,w= H.transform_mult,Ia=H.transform_inverted,ka=H.transform_assign,rb=H.transform_equal,Hb=H.transform_apply,x=H.rect_top,z=H.rect_bottom,q=H.rect_left,s=H.rect_right,Wa=H.rect_intersection,Y=E.chartCommon,Ib=Y.minMaxY,Db=Y.findClosestPoint,Jb=Y.projection,Ab=Y.distanceLessThanRadius,yb=Y.toZoomLevel,Ma=Y.isPointInRect,Kb=Y.findYRange,Na=function(a,b){return Y.matchesXAxis(a,b,h(),l.xAxis,B())},Oa=function(a,b){return Y.matchYAxis(a,b,h(),l.yAxes,B())},Ka=17,Xa=function(){return window.requestAnimationFrame|| window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,Ka)}}(),gb=false,ub=function(a){if(!gb){gb=true;Xa(function(){a();gb=false})}};if(window.MSPointerEvent||window.PointerEvent){J.style.touchAction=\"none\";A.canvas.style.msTouchAction=\"none\";A.canvas.style.touchAction=\"none\"}var Ca=1,zb=2,za=1,Aa=2,v=0,u=1,Cb=250,tb=500,wb=0.003,Gb=2.0E-4,Za=0.07,Va=3,fb=0.001,pa=1.5,xb=0.02,ta=J.wtEObj2;if(!ta){ta={};ta.contextmenuListener=function(a){ya(a);X(\"contextmenu\", ta.contextmenuListener)}}J.wtEObj2=ta;var ba={},ua=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ua=y(e)>0}function b(j){if(bb(j)){ya(j);e.push(j);a();ba.start(J,{touches:e.slice(0)})}}function c(j){if(ua)if(bb(j)){ya(j);var m;for(m=0;m<y(e);++m)if(e[m].pointerId===j.pointerId){e.splice(m,1);break}a();ba.end(J,{touches:e.slice(0),changedTouches:[]})}}function d(j){if(bb(j)){ya(j);var m;for(m=0;m<y(e);++m)if(e[m].pointerId===j.pointerId){e[m]=j;break}a();ba.moved(J,{touches:e.slice(0)})}} var e=[],f=J.wtEObj;if(f)if(window.PointerEvent){X(\"pointerdown\",f.pointerDown);X(\"pointerup\",f.pointerUp);X(\"pointerout\",f.pointerUp);X(\"pointermove\",f.pointerMove)}else{X(\"MSPointerDown\",f.pointerDown);X(\"MSPointerUp\",f.pointerUp);X(\"MSPointerOut\",f.pointerUp);X(\"MSPointerMove\",f.pointerMove)}J.wtEObj={pointerDown:b,pointerUp:c,pointerMove:d};if(window.PointerEvent){ga(\"pointerdown\",b);ga(\"pointerup\",c);ga(\"pointerout\",c);ga(\"pointermove\",d)}else{ga(\"MSPointerDown\",b);ga(\"MSPointerUp\",c);ga(\"MSPointerOut\", c);ga(\"MSPointerMove\",d)}})();var I=J.wtOObj,C=null,na=true,D=null,G=false,t=-1,r=[],ja=false,ra=false,U=null,hb=null,ib=null,k={x:0,y:0},ca=null,oa=null,p=J.wtTObj;if(!p){p={overTooltip:false};J.wtTObj=p}var Da=null,Ba=false,Sa=null,cb=[0,0,0,0,0,0];ka(cb,i());var Ta=[];for(H=0;H<M();++H){Ta.push([0,0,0,0,0,0]);ka(Ta[H],g(H))}var qa=function(a,b){ka(a,b);Ha()};A.combinedTransform=la;this.updateTooltip=function(a){Ra();if(a)if(p.tooltipPosition){p.toolTipEl=document.createElement(\"div\");p.toolTipEl.className= l.ToolTipInnerStyle;p.toolTipEl.innerHTML=a;p.tooltipOuterDiv=document.createElement(\"div\");p.tooltipOuterDiv.className=l.ToolTipOuterStyle;document.body.appendChild(p.tooltipOuterDiv);p.tooltipOuterDiv.appendChild(p.toolTipEl);var b=E.widgetPageCoordinates(A.canvas);a=p.tooltipPosition[v]+b.x;b=p.tooltipPosition[u]+b.y;E.fitToWindow(p.tooltipOuterDiv,a+10,b+10,a-10,b-10);$(p.toolTipEl).mouseenter(function(){p.overTooltip=true});$(p.toolTipEl).mouseleave(function(){p.overTooltip=false})}};this.mouseMove= function(a,b){setTimeout(function(){setTimeout(Ra,200);if(!ua){var c=E.widgetCoordinates(A.canvas,b);if(Ma(c,h())){if(nb()){p.tooltipPosition=[c.x,c.y];p.tooltipTimeout=setTimeout(function(){sb()},tb)}if(D===null&&xa()&&na){C=[c.x,c.y];ub(db)}}}},0)};this.mouseOut=function(){setTimeout(Ra,200)};this.mouseDown=function(a,b){if(!ua){a=E.widgetCoordinates(A.canvas,b);b=Oa(a.x,a.y);var c=Ma(a,h()),d=Na(a.x,a.y);if(!(b===-1&&!d&&!c)){D=a;G=d;t=b}}};this.mouseUp=function(){if(!ua){D=null;G=false;t=-1}}; this.mouseDrag=function(a,b){if(!ua)if(D===null)ia.mouseDown(a,b);else{a=E.widgetCoordinates(A.canvas,b);if(E.buttons===1)if(t===-1&&!G&&Ga()&&da(W())){b=W();var c;c=B()?a.x-D.x:a.y-D.y;ka(ea(b),w([1,0,0,1,0,c/g(P(W()))[3]],ea(b)));Z()}else l.pan&&aa({x:a.x-D.x,y:a.y-D.y},0,t,G);D=a}};this.clicked=function(a,b){if(!ua)if(D===null)if(lb()){a=E.widgetCoordinates(A.canvas,b);wa.emit(A.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){var c=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey; a=l.wheelActions[c];if(!N(a)){var d=E.widgetCoordinates(A.canvas,b),e=Na(d.x,d.y),f=Oa(d.x,d.y),j=Ma(d,h());if(!(!e&&f===-1&&!j)){var m=E.normalizeWheel(b);if(j&&c===0&&Ga()){c=W();j=-m.spinY;if(da(c)){a=kb(c);a=Hb(a,Pa(c));a=Ib(a,B());a=(a[0]+a[1])/2;E.cancelEvent(b);b=Math.pow(1.2,j);ka(ea(c),w([1,0,0,b,0,a-b*a],ea(c)));Z();return}}if((a===4||a===5||a===6)&&l.pan){c=i()[4];j=[];for(d=0;d<M();++d)j.push(g(d)[5]);if(a===6)aa({x:-m.pixelX,y:-m.pixelY},0,f,e);else if(a===5)aa({x:0,y:-m.pixelX-m.pixelY}, 0,f,e);else a===4&&aa({x:-m.pixelX-m.pixelY,y:0},0,f,e);c!==i()[4]&&E.cancelEvent(b);for(d=0;d<M();++d)j[d]!==g(d)[5]&&E.cancelEvent(b)}else if(l.zoom){E.cancelEvent(b);j=-m.spinY;if(j===0)j=-m.spinX;if(a===1)La(d,0,j,f,e);else if(a===0)La(d,j,0,f,e);else if(a===2)La(d,j,j,f,e);else if(a===3)m.pixelX!==0?La(d,j,0,f,e):La(d,0,j,f,e)}}}};var Lb=function(){lb()&&wa.emit(A.widget,\"seriesSelected\",D.x,D.y)};ba.start=function(a,b,c){ja=y(b.touches)===1;ra=y(b.touches)===2;if(ja){Ba=false;var d=E.widgetCoordinates(A.canvas, b.touches[0]);a=Oa(d.x,d.y);var e=Ma(d,h()),f=Na(d.x,d.y);if(a===-1&&!f&&!e)return;Da=a===-1&&!f&&xa()&&Ab(C,[d.x,d.y],30)?1:0;oa=Date.now();D=d;t=a;G=f;if(Da!==1){if(!c&&e)ca=window.setTimeout(Lb,200);ga(\"contextmenu\",ta.contextmenuListener)}E.capture(null);E.capture(vb())}else if(ra&&(l.zoom||Ga())){if(ca){window.clearTimeout(ca);ca=null}Ba=false;r=[E.widgetCoordinates(A.canvas,b.touches[0]),E.widgetCoordinates(A.canvas,b.touches[1])].map(function(j){return[j.x,j.y]});f=false;a=-1;if(!r.every(function(j){return Ma(j, h())})){(f=Na(r[0][v],r[0][u])&&Na(r[1][v],r[1][u]))||(a=Oa(r[0][v],r[0][u]));if(!f&&(a===-1||Oa(r[1][v],r[1][u])!==a)){ra=null;return}G=f;t=a}E.capture(null);E.capture(vb());U=Math.atan2(r[1][1]-r[0][1],r[1][0]-r[0][0]);hb=[(r[0][0]+r[1][0])/2,(r[0][1]+r[1][1])/2];c=Math.abs(Math.sin(U));d=Math.abs(Math.cos(U));U=c<Math.sin(0.125*Math.PI)?0:d<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(U)>0?Math.PI/4:-Math.PI/4;ib=Jb(U,hb);G=f;t=a}else return;ya(b)};ba.end=function(a,b){if(ca){window.clearTimeout(ca); ca=null}window.setTimeout(function(){X(\"contextmenu\",ta.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),d=y(c)===0;d||function(){var e;for(e=0;e<y(b.changedTouches);++e)(function(){for(var f=b.changedTouches[e].identifier,j=0;j<y(c);++j)if(c[j].identifier===f){c.splice(j,1);return}})()}();d=y(c)===0;ja=y(c)===1;ra=y(c)===2;if(d){$a=null;if(Da===0&&(isFinite(k.x)||isFinite(k.y))&&l.rubberBand){oa=Date.now();Ba=true;Xa(eb)}else{Da===1&&ia.mouseUp(null,null);c=[];oa=ib=hb=U=null}Da= null}else if(ja||ra)ba.start(a,b,true)};var $a=null,va=null,jb=null;ba.moved=function(a,b){if(ja||ra)if(!(ja&&D==null)){ya(b);va=E.widgetCoordinates(A.canvas,b.touches[0]);if(y(b.touches)>1)jb=E.widgetCoordinates(A.canvas,b.touches[1]);if(!G&&t===-1&&ja&&ca&&!Ab([va.x,va.y],[D.x,D.y],3)){window.clearTimeout(ca);ca=null}$a||($a=setTimeout(function(){if(!G&&t===-1&&ja&&Ga()&&da(W())){var c=W();if(da(c)){var d=va,e;e=B()?(d.x-D.x)/g(P(W()))[3]:(d.y-D.y)/g(P(W()))[3];ea(c)[5]+=e;D=d;Z()}}else if(ja){d= va;e=Date.now();var f={x:d.x-D.x,y:d.y-D.y};c=e-oa;oa=e;if(Da===1){C[v]+=f.x;C[u]+=f.y;xa()&&na&&Xa(db)}else if(l.pan){k.x=f.x/c;k.y=f.y/c;aa(f,l.rubberBand?zb:0,t,G)}D=d}else if(!G&&t===-1&&ra&&Ga()&&da(W())){f=B()?v:u;e=[va,jb].map(function(S){return B()?[S.x,sa]:[Ea,S.y]});c=Math.abs(r[1][f]-r[0][f]);var j=Math.abs(e[1][f]-e[0][f]),m=c>0?j/c:1;if(j===c)m=1;c=W();if(da(c)){var sa=w(Ia(la(P(c))),[0,(r[0][f]+r[1][f])/2])[1],Fa=w(Ia(la(P(c))),[0,(e[0][f]+e[1][f])/2])[1];ka(ea(c),w([1,0,0,m,0,-m*sa+ Fa],ea(c)));D=d;Z();r=e}}else if(ra&&l.zoom){d=ma(C,Q());var Ea=(r[0][0]+r[1][0])/2;sa=(r[0][1]+r[1][1])/2;e=[va,jb].map(function(S){return U===0?[S.x,sa]:U===Math.PI/2?[Ea,S.y]:w(ib,[S.x,S.y])});f=Math.abs(r[1][0]-r[0][0]);c=Math.abs(e[1][0]-e[0][0]);var O=f>0?c/f:1;if(c===f||U===Math.PI/2)O=1;var ab=(e[0][0]+e[1][0])/2;c=Math.abs(r[1][1]-r[0][1]);j=Math.abs(e[1][1]-e[0][1]);m=c>0?j/c:1;if(j===c||U===0)m=1;Fa=(e[0][1]+e[1][1])/2;B()&&function(){var S=O;O=m;m=S;S=ab;ab=Fa;Fa=S;S=Ea;Ea=sa;sa=S}(); if(i()[0]*O>fa())O=fa()/i()[0];if(i()[0]*O<Qa())O=Qa()/i()[0];f=[];for(c=0;c<M();++c)f.push(m);for(c=0;c<M();++c){if(g(c)[3]*f[c]>T(c))f[c]=T(c)/g(c)[3];if(g(c)[3]*f[c]<pb(c))f[c]=pb(c)/g(c)[3]}if(G){if(O!==1&&(O<1||i()[0]!==fa()))qa(i(),w([O,0,0,1,-O*Ea+ab,0],i()))}else if(t===-1){if(O!==1&&(O<1||i()[0]!==fa()))qa(i(),w([O,0,0,1,-O*Ea+ab,0],i()));for(c=0;c<M();++c)if(f[c]!==1&&(f[c]<1||g(c)[3]!==T(c)))qa(g(c),w([1,0,0,f[c],0,-f[c]*sa+Fa],g(c)))}else if(f[t]!==1&&(f[t]<1||g(t)[3]!==T(t)))qa(g(t), w([1,0,0,f[t],0,-f[t]*sa+Fa],g(t)));ha();d=Ua(d,Q());C[v]=d[v];C[u]=d[u];r=e;Ya();Z();Ja()}$a=null},1))}};this.setXRange=function(a,b,c,d){b=o(0)[0]+o(0)[2]*b;c=o(0)[0]+o(0)[2]*c;if(q(o(0))>s(o(0))){if(b>q(o(0)))b=q(o(0));if(c<s(o(0)))c=s(o(0))}else{if(b<q(o(0)))b=q(o(0));if(c>s(o(0)))c=s(o(0))}var e=Pa(a);e=Kb(e,P(a),b,c,B(),h(),o(P(a)),l.minZoom,l.maxZoom);b=e.xZoom;c=e.yZoom;e=e.panPoint;var f=ma(C,Q());i()[0]=b;if(c&&d)g(P(a))[3]=c;i()[4]=-e[v]*b;if(c&&d)g(P(a))[5]=-e[u]*c;Ha();a=Ua(f,Q());C[v]= a[v];C[u]=a[u];ha();Ya();Z();Ja()};this.getSeries=function(a){return Pa(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))l[b]=a[b];Fb();Ya();Z();Ja()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ia.touchStart=ba.start;ia.touchEnd=ba.end;ia.touchMoved=ba.moved}else{H=function(){};ia.touchStart=H;ia.touchEnd=H;ia.touchMoved=H}}");
	}

	static String locToJsString(AxisValue loc) {
		switch (loc) {
		case MinimumValue:
			return "min";
		case MaximumValue:
			return "max";
		case ZeroValue:
			return "zero";
		case BothSides:
			return "both";
		}
		assert false;
		return "";
	}

	static int binarySearchRow(final WAbstractChartModel model, int xColumn,
			double d, int minRow, int maxRow) {
		if (minRow == maxRow) {
			return minRow;
		}
		double min = model.getData(minRow, xColumn);
		double max = model.getData(maxRow, xColumn);
		if (d <= min) {
			return minRow;
		}
		if (d >= max) {
			return maxRow;
		}
		double start = minRow + (d - min) / (max - min) * (maxRow - minRow);
		double data = model.getData((int) start, xColumn);
		if (data < d) {
			return binarySearchRow(model, xColumn, d, (int) start + 1, maxRow);
		} else {
			if (data > d) {
				return binarySearchRow(model, xColumn, d, minRow,
						(int) start - 1);
			} else {
				return (int) start;
			}
		}
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;
	private static final int CURVE_SELECTION_DISTANCE_SQUARED = 400;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
