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
 * browsers.
 * <p>
 * Some features are currently not supported in interactive mode:
 * <ul>
 * <li>Axes set at ZeroValue position will not always be drawn correctly. They
 * may be clipped off outside of the chart area, and when zooming, the axis
 * ticks will change size.</li>
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
		this.barMargin_ = 0;
		this.legend_ = new WLegend();
		this.axisPadding_ = 5;
		this.borderPen_ = new WPen(PenStyle.NoPen);
		this.textPen_ = new WPen();
		this.chartArea_ = null;
		this.zoomEnabled_ = false;
		this.panEnabled_ = false;
		this.rubberBandEnabled_ = true;
		this.crosshairEnabled_ = false;
		this.followCurve_ = -1;
		this.cObjCreated_ = false;
		this.curvePaths_ = new HashMap<Integer, WJavaScriptHandle<WPainterPath>>();
		this.freePainterPaths_ = new ArrayList<WJavaScriptHandle<WPainterPath>>();
		this.xTransform_ = new WTransform();
		this.yTransform_ = new WTransform();
		this.xTransformHandle_ = null;
		this.yTransformHandle_ = null;
		this.pens_ = new HashMap<Axis, List<WCartesianChart.PenAssignment>>();
		this.freePens_ = new ArrayList<WJavaScriptHandle<WPen>>();
		this.axisSliderWidgets_ = new ArrayList<WAxisSliderWidget>();
		this.wheelActions_ = new HashMap<EnumSet<KeyboardModifier>, InteractiveAction>();
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
		this.barMargin_ = 0;
		this.legend_ = new WLegend();
		this.axisPadding_ = 5;
		this.borderPen_ = new WPen(PenStyle.NoPen);
		this.textPen_ = new WPen();
		this.chartArea_ = null;
		this.zoomEnabled_ = false;
		this.panEnabled_ = false;
		this.rubberBandEnabled_ = true;
		this.crosshairEnabled_ = false;
		this.followCurve_ = -1;
		this.cObjCreated_ = false;
		this.curvePaths_ = new HashMap<Integer, WJavaScriptHandle<WPainterPath>>();
		this.freePainterPaths_ = new ArrayList<WJavaScriptHandle<WPainterPath>>();
		this.xTransform_ = new WTransform();
		this.yTransform_ = new WTransform();
		this.xTransformHandle_ = null;
		this.yTransformHandle_ = null;
		this.pens_ = new HashMap<Axis, List<WCartesianChart.PenAssignment>>();
		this.freePens_ = new ArrayList<WJavaScriptHandle<WPen>>();
		this.axisSliderWidgets_ = new ArrayList<WAxisSliderWidget>();
		this.wheelActions_ = new HashMap<EnumSet<KeyboardModifier>, InteractiveAction>();
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
		for (int i = 2; i > -1; i--) {
			;
		}
		;
		List<WAxisSliderWidget> copy = new ArrayList<WAxisSliderWidget>(
				this.axisSliderWidgets_);
		this.axisSliderWidgets_.clear();
		for (int i = 0; i < copy.size(); ++i) {
			copy.get(i).setChart((WCartesianChart) null);
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
			this.axes_[Axis.XAxis.getValue()].init(this.interface_, Axis.XAxis);
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
		for (int i = 0; i < 3; ++i) {
			this.axes_[i].setTextPen(pen);
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
	public void addSeries(final WDataSeries series) {
		this.series_.add(series);
		this.series_.get(this.series_.size() - 1).setChart(this);
		if (series.getType() == SeriesType.LineSeries
				|| series.getType() == SeriesType.CurveSeries) {
			this.assignJSPathsForSeries(series);
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
	 */
	public void removeSeries(int modelColumn) {
		int index = this.getSeriesIndexOf(modelColumn);
		if (index != -1) {
			if (this.series_.get(index).getType() == SeriesType.LineSeries
					|| this.series_.get(index).getType() == SeriesType.CurveSeries) {
				this.freeJSPathsForSeries(modelColumn);
			}
			this.series_.remove(0 + index);
			this.update();
		}
	}

	/**
	 * Sets all data series.
	 * <p>
	 * Replaces the current list of series with the new list.
	 * <p>
	 * 
	 * @see WCartesianChart#getSeries(int modelColumn)
	 * @see WCartesianChart#addSeries(WDataSeries series)
	 * @see WCartesianChart#removeSeries(int modelColumn)
	 */
	public void setSeries(final List<WDataSeries> series) {
		Utils.copyList(series, this.series_);
		this.freeAllJSPaths();
		for (int i = 0; i < this.series_.size(); ++i) {
			final WDataSeries s = this.series_.get(i);
			if (s.getType() == SeriesType.LineSeries
					|| s.getType() == SeriesType.CurveSeries) {
				this.assignJSPathsForSeries(s);
			}
		}
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
	 * 
	 * @see WCartesianChart#setSeries(List series)
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
		return this.axes_[axis.getValue()];
	}

	/**
	 * Sets an axis.
	 * <p>
	 * 
	 * @see WCartesianChart#getAxis(Axis axis)
	 */
	public void setAxis(WAxis waxis, Axis axis) {
		this.axes_[axis.getValue()] = waxis;
		this.axes_[axis.getValue()].init(this.interface_, axis);
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
		painter.drawText(
				pos.getX() + 23,
				pos.getY() - 9,
				100,
				20,
				EnumSet.of(AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle),
				StringUtils.asString(this.getModel().getHeaderData(
						series.getModelColumn())));
	}

	/**
	 * Maps from device coordinates to model coordinates.
	 * <p>
	 * Maps a position in the chart back to model coordinates.
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish to already the
	 * mapping reflect model changes since the last rendering, you should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * 
	 * @see WCartesianChart#mapToDevice(Object xValue, Object yValue, Axis
	 *      ordinateAxis, int xSegment, int ySegment)
	 */
	public WPointF mapFromDevice(final WPointF point, Axis ordinateAxis) {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis yAxis = this.getAxis(ordinateAxis);
		WPointF p = this.inverseHv(point.getX(), point.getY(), this.getWidth()
				.toPixels());
		return new WPointF(xAxis.mapFromDevice(p.getX()
				- this.chartArea_.getLeft()),
				yAxis.mapFromDevice(this.chartArea_.getBottom() - p.getY()));
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
	 * Maps model values onto chart coordinates.
	 * <p>
	 * This returns the chart device coordinates for a (x,y) pair of model
	 * values.
	 * <p>
	 * This uses the axis dimensions that are based on the latest chart
	 * rendering. If you have not yet rendered the chart, or wish to already the
	 * mapping reflect model changes since the last rendering, you should call
	 * {@link WCartesianChart#initLayout(WRectF rectangle, WPaintDevice device)
	 * initLayout()} first.
	 * <p>
	 * The <code>xSegment</code> and <code>ySegment</code> arguments are
	 * relevant only when the corresponding axis is broken using
	 * {@link WAxis#setBreak(double minimum, double maximum) WAxis#setBreak()}.
	 * Then, its possible values may be 0 (below the break) or 1 (above the
	 * break).
	 * <p>
	 * 
	 * @see WCartesianChart#mapFromDevice(WPointF point, Axis ordinateAxis)
	 */
	public WPointF mapToDevice(final Object xValue, final Object yValue,
			Axis ordinateAxis, int xSegment, int ySegment) {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis yAxis = this.getAxis(ordinateAxis);
		double x = this.chartArea_.getLeft()
				+ xAxis.mapToDevice(xValue, xSegment);
		double y = this.chartArea_.getBottom()
				- yAxis.mapToDevice(yValue, ySegment);
		return this.hv(x, y, this.getWidth().toPixels());
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * Returns
	 * {@link #mapToDevice(Object xValue, Object yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 * mapToDevice(xValue, yValue, Axis.OrdinateAxis, 0, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue) {
		return mapToDevice(xValue, yValue, Axis.OrdinateAxis, 0, 0);
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * Returns
	 * {@link #mapToDevice(Object xValue, Object yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 * mapToDevice(xValue, yValue, ordinateAxis, 0, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue,
			Axis ordinateAxis) {
		return mapToDevice(xValue, yValue, ordinateAxis, 0, 0);
	}

	/**
	 * Maps model values onto chart coordinates.
	 * <p>
	 * Returns
	 * {@link #mapToDevice(Object xValue, Object yValue, Axis ordinateAxis, int xSegment, int ySegment)
	 * mapToDevice(xValue, yValue, ordinateAxis, xSegment, 0)}
	 */
	public final WPointF mapToDevice(final Object xValue, final Object yValue,
			Axis ordinateAxis, int xSegment) {
		return mapToDevice(xValue, yValue, ordinateAxis, xSegment, 0);
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
	 * {@link WCartesianChart#mapToDevice(Object xValue, Object yValue, Axis ordinateAxis, int xSegment, int ySegment)
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
		for (int i = 0; i < 3; ++i) {
			this.location_[i] = AxisValue.MinimumValue;
		}
		boolean autoLayout = this.isAutoLayoutEnabled();
		if (autoLayout
				&& EnumUtils.mask(device.getFeatures(),
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
			WPaintDevice d = device;
			if (!(d != null)) {
				d = this.getCreatePaintDevice();
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
			if (!(device != null)) {
				;
			}
		}
		this.calcChartArea();
		boolean result = this.chartArea_.getWidth() > 5
				&& this.chartArea_.getHeight() > 5 && this.isPrepareAxes();
		if (this.isInteractive()) {
			this.xTransform_.assign(this.xTransformHandle_.getValue());
			this.yTransform_.assign(this.yTransformHandle_.getValue());
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
		WText label = new WText(StringUtils.asString(this.getModel()
				.getHeaderData(index)));
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
	public void addDataPointArea(final WDataSeries series,
			final WModelIndex xIndex, WAbstractArea area) {
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
		for (int i = 0; i < 2; ++i) {
			this.axes_[i].setPadding(padding);
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
	 * values of the data series are monotonically increasing or decreasing.
	 * </i>
	 * </p>
	 */
	public void setFollowCurve(int followCurve) {
		if (this.followCurve_ != followCurve) {
			this.followCurve_ = followCurve;
			this.updateJSConfig("followCurve", this.followCurve_);
		}
	}

	/**
	 * Disable the follow curve functionality.
	 * <p>
	 * 
	 * @see WCartesianChart#setFollowCurve(int followCurve)
	 */
	public void disableFollowCurve() {
		this.setFollowCurve(-1);
	}

	/**
	 * Returns the curve that is to be followed.
	 * <p>
	 * If follow curve functionality is not enabled, returns -1.
	 * <p>
	 * 
	 * @see WCartesianChart#setFollowCurve(int followCurve)
	 */
	public int getFollowCurve() {
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
		for (int i = 0; i < 3; ++i) {
			this.axes_[i].setSoftLabelClipping(enabled);
		}
	}

	public void iterateSeries(SeriesIterator iterator, WPainter painter,
			boolean reverseStacked) {
		WAbstractItemModel chart_model = this.getModel();
		int rows = chart_model != null ? chart_model.getRowCount() : 0;
		double groupWidth = 0.0;
		int numBarGroups;
		int currentBarGroup;
		List<Double> stackedValuesInit = new ArrayList<Double>();
		{
			int insertPos = 0;
			for (int ii = 0; ii < rows; ++ii)
				stackedValuesInit.add(insertPos + ii, 0.0);
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
			if (this.series_.get(g).isHidden()) {
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
				for (int i = 0; i < rows; ++i) {
					stackedValuesInit.set(i, 0.0);
				}
				if (reverseStacked) {
					endSeries = g;
					Axis a = this.series_.get(g).getAxis();
					for (;;) {
						if (g < this.series_.size()
								&& ((int) g == endSeries || this.series_.get(g)
										.isStacked())
								&& this.series_.get(g).getAxis() == a) {
							if (this.series_.get(g).getType() == SeriesType.BarSeries) {
								containsBars = true;
							}
							for (int row = 0; row < rows; ++row) {
								double y = StringUtils.asNumber(chart_model
										.getData(row, this.series_.get(g)
												.getModelColumn()));
								if (!Double.isNaN(y)) {
									stackedValuesInit.set(row,
											stackedValuesInit.get(row) + y);
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
					Axis a = this.series_.get(g).getAxis();
					if (this.series_.get(g).getType() == SeriesType.BarSeries) {
						containsBars = true;
					}
					++g;
					for (;;) {
						if (g < this.series_.size()
								&& this.series_.get(g).isStacked()
								&& this.series_.get(g).getAxis() == a) {
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
			}
			int i = startSeries;
			for (;;) {
				boolean doSeries = iterator.startSeries(this.series_.get(i),
						groupWidth, numBarGroups, currentBarGroup);
				List<Double> stackedValues = new ArrayList<Double>();
				if (doSeries || !scatterPlot && i != endSeries) {
					for (int currentXSegment = 0; currentXSegment < this
							.getAxis(Axis.XAxis).getSegmentCount(); ++currentXSegment) {
						for (int currentYSegment = 0; currentYSegment < this
								.getAxis(this.series_.get(i).getAxis())
								.getSegmentCount(); ++currentYSegment) {
							stackedValues.clear();
							stackedValues.addAll(stackedValuesInit);
							if (painter != null) {
								WRectF csa = this.chartSegmentArea(
										this.getAxis(this.series_.get(i)
												.getAxis()), currentXSegment,
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
							for (int row = 0; row < rows; ++row) {
								WModelIndex xIndex = null;
								WModelIndex yIndex = null;
								double x;
								if (scatterPlot) {
									int c = this.series_.get(i).XSeriesColumn();
									if (c == -1) {
										c = this.XSeriesColumn();
									}
									if (c != -1) {
										xIndex = chart_model.getIndex(row, c);
										x = StringUtils.asNumber(chart_model
												.getData(xIndex));
									} else {
										x = row;
									}
								} else {
									x = row;
								}
								yIndex = chart_model.getIndex(row, this.series_
										.get(i).getModelColumn());
								double y = StringUtils.asNumber(chart_model
										.getData(yIndex));
								double prevStack;
								if (scatterPlot) {
									iterator.newValue(this.series_.get(i), x,
											y, 0, xIndex, yIndex);
								} else {
									prevStack = stackedValues.get(row);
									double nextStack = stackedValues.get(row);
									boolean hasValue = !Double.isNaN(y);
									if (hasValue) {
										if (reverseStacked) {
											nextStack -= y;
										} else {
											nextStack += y;
										}
									}
									stackedValues.set(row, nextStack);
									if (doSeries) {
										if (reverseStacked) {
											iterator.newValue(
													this.series_.get(i), x,
													hasValue ? prevStack : y,
													nextStack, xIndex, yIndex);
										} else {
											iterator.newValue(
													this.series_.get(i), x,
													hasValue ? nextStack : y,
													prevStack, xIndex, yIndex);
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
					stackedValuesInit.clear();
					stackedValuesInit.addAll(stackedValues);
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
	private WAxis[] axes_ = new WAxis[3];
	private double barMargin_;
	private WLegend legend_;
	private int axisPadding_;
	private WPen borderPen_;
	WPen textPen_;
	private int width_;
	private int height_;
	WRectF chartArea_;
	private AxisValue[] location_ = new AxisValue[3];
	private boolean zoomEnabled_;
	private boolean panEnabled_;
	private boolean rubberBandEnabled_;
	private boolean crosshairEnabled_;
	private int followCurve_;
	boolean cObjCreated_;
	Map<Integer, WJavaScriptHandle<WPainterPath>> curvePaths_;
	private List<WJavaScriptHandle<WPainterPath>> freePainterPaths_;
	WTransform xTransform_;
	WTransform yTransform_;
	WJavaScriptHandle<WTransform> xTransformHandle_;
	WJavaScriptHandle<WTransform> yTransformHandle_;

	static class PenAssignment {
		private static Logger logger = LoggerFactory
				.getLogger(PenAssignment.class);

		public WJavaScriptHandle<WPen> pen;
		public WJavaScriptHandle<WPen> textPen;

		public PenAssignment(final WJavaScriptHandle<WPen> pen,
				final WJavaScriptHandle<WPen> textPen) {
			this.pen = pen;
			this.textPen = textPen;
		}
	}

	private Map<Axis, List<WCartesianChart.PenAssignment>> pens_;
	private List<WJavaScriptHandle<WPen>> freePens_;
	private List<WAxisSliderWidget> axisSliderWidgets_;
	private Map<EnumSet<KeyboardModifier>, InteractiveAction> wheelActions_;

	private void init() {
		this.setPalette(new WStandardPalette(WStandardPalette.Flavour.Muted));
		for (int i = 0; i < 3; ++i) {
			this.axes_[i] = new WAxis();
		}
		this.axes_[Axis.XAxis.getValue()].init(this.interface_, Axis.XAxis);
		this.axes_[Axis.YAxis.getValue()].init(this.interface_, Axis.YAxis);
		this.axes_[Axis.Y2Axis.getValue()].init(this.interface_, Axis.Y2Axis);
		this.axes_[Axis.XAxis.getValue()].setPadding(this.axisPadding_);
		this.axes_[Axis.YAxis.getValue()].setPadding(this.axisPadding_);
		this.axes_[Axis.Y2Axis.getValue()].setPadding(this.axisPadding_);
		this.axes_[Axis.XAxis.getValue()].setSoftLabelClipping(true);
		this.axes_[Axis.YAxis.getValue()].setSoftLabelClipping(true);
		this.axes_[Axis.Y2Axis.getValue()].setSoftLabelClipping(true);
		this.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
		this.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
		this.xTransformHandle_ = this.createJSTransform();
		this.yTransformHandle_ = this.createJSTransform();
		this.xTransform_.assign(new WTransform());
		this.yTransform_.assign(new WTransform());
		if (WApplication.getInstance() != null) {
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
			this.touchStarted().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.touchStart(o, e);}}");
			this.touchEnded().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.touchEnd(o, e);}}");
			this.touchMoved().addListener(
					"function(o, e){var o=" + this.getCObjJsRef()
							+ ";if(o){o.touchMoved(o, e);}}");
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

	protected void modelColumnsInserted(final WModelIndex parent, int start,
			int end) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() >= start) {
				this.series_.get(i).modelColumn_ += end - start + 1;
			}
		}
	}

	protected void modelColumnsRemoved(final WModelIndex parent, int start,
			int end) {
		boolean needUpdate = false;
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() >= start) {
				if (this.series_.get(i).getModelColumn() <= end) {
					this.series_.remove(0 + i);
					needUpdate = true;
					--i;
				} else {
					this.series_.get(i).modelColumn_ -= end - start + 1;
				}
			}
		}
		if (needUpdate) {
			this.update();
		}
	}

	protected void modelRowsInserted(final WModelIndex parent, int start,
			int end) {
		this.update();
	}

	protected void modelRowsRemoved(final WModelIndex parent, int start, int end) {
		this.update();
	}

	protected void modelDataChanged(final WModelIndex topLeft,
			final WModelIndex bottomRight) {
		if (this.XSeriesColumn_ >= topLeft.getColumn()
				&& this.XSeriesColumn_ <= bottomRight.getColumn()) {
			this.update();
			return;
		}
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() >= topLeft.getColumn()
					&& this.series_.get(i).getModelColumn() <= bottomRight
							.getColumn()
					|| this.series_.get(i).XSeriesColumn() >= topLeft
							.getColumn()
					&& this.series_.get(i).XSeriesColumn() <= bottomRight
							.getColumn()) {
				this.update();
				break;
			}
		}
	}

	protected void modelHeaderDataChanged(Orientation orientation, int start,
			int end) {
		if (orientation == Orientation.Horizontal) {
			if (this.XSeriesColumn_ >= start && this.XSeriesColumn_ <= end) {
				this.update();
				return;
			}
			for (int i = 0; i < this.series_.size(); ++i) {
				if (this.series_.get(i).getModelColumn() >= start
						&& this.series_.get(i).getModelColumn() <= end
						|| this.series_.get(i).XSeriesColumn() >= start
						&& this.series_.get(i).XSeriesColumn() <= end) {
					this.update();
					break;
				}
			}
		}
	}

	protected void modelChanged() {
		this.XSeriesColumn_ = -1;
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
		WPainter painter = new WPainter(paintDevice);
		painter.setRenderHint(WPainter.RenderHint.Antialiasing);
		this.paint(painter);
		if (this.isInteractive()) {
			this.setZoomAndPan();
			double modelBottom = this.getAxis(Axis.Y1Axis).mapFromDevice(0);
			double modelTop = this.getAxis(Axis.Y1Axis).mapFromDevice(
					this.chartArea_.getHeight());
			double modelLeft = this.getAxis(Axis.XAxis).mapFromDevice(0);
			double modelRight = this.getAxis(Axis.XAxis).mapFromDevice(
					this.chartArea_.getWidth());
			WRectF modelArea = new WRectF(modelLeft, modelBottom, modelRight
					- modelLeft, modelTop - modelBottom);
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
			char[] buf = new char[30];
			WApplication app = WApplication.getInstance();
			StringBuilder ss = new StringBuilder();
			ss.append("new Wt3_3_4.WCartesianChart(")
					.append(app.getJavaScriptClass())
					.append(",")
					.append(this.getJsRef())
					.append(",")
					.append(this.getObjJsRef())
					.append(",{isHorizontal:")
					.append(StringUtils.asString(
							this.getOrientation() == Orientation.Horizontal)
							.toString())
					.append(",zoom:")
					.append(StringUtils.asString(this.zoomEnabled_).toString())
					.append(",pan:")
					.append(StringUtils.asString(this.panEnabled_).toString())
					.append(",crosshair:")
					.append(StringUtils.asString(this.crosshairEnabled_)
							.toString()).append(",followCurve:")
					.append(this.followCurve_).append(",xTransform:")
					.append(this.xTransformHandle_.getJsRef())
					.append(",yTransform:")
					.append(this.yTransformHandle_.getJsRef()).append(",area:")
					.append(this.hv(this.chartArea_).getJsRef())
					.append(",modelArea:").append(modelArea.getJsRef())
					.append(",");
			this.updateJSPens(ss);
			ss.append("series:{");
			for (int i = 0; i < this.series_.size(); ++i) {
				if (this.series_.get(i).getType() == SeriesType.LineSeries
						|| this.series_.get(i).getType() == SeriesType.CurveSeries) {
					ss.append(this.series_.get(i).getModelColumn())
							.append(":")
							.append(this.curvePaths_.get(
									this.series_.get(i).getModelColumn())
									.getJsRef()).append(",");
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
			for (int i = 0; i < this.axisSliderWidgets_.size(); ++i) {
				this.axisSliderWidgets_.get(i).update();
			}
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
		if (this.isInteractive()) {
			this.xTransform_.assign(this.xTransformHandle_.getValue());
			this.yTransform_.assign(this.yTransformHandle_.getValue());
		}
		if (this.initLayout(rectangle, painter.getDevice())) {
			this.renderBackground(painter);
			this.renderGrid(painter, this.getAxis(Axis.XAxis));
			this.renderGrid(painter, this.getAxis(Axis.Y1Axis));
			this.renderGrid(painter, this.getAxis(Axis.Y2Axis));
			this.renderSeries(painter);
			this.renderAxes(painter, EnumSet.of(AxisProperty.Line));
			this.renderAxes(painter, EnumSet.of(AxisProperty.Labels));
			this.renderBorder(painter);
			this.renderLegend(painter);
		}
		if (this.isInteractive()) {
			this.xTransform_.assign(new WTransform());
			this.yTransform_.assign(new WTransform());
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
		final WAxis xAx = this.getAxis(Axis.XAxis);
		final WAxis yAx = this.getAxis(yAxis);
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
	protected WRectF chartSegmentArea(WAxis yAxis, int xSegment, int ySegment) {
		final WAxis xAxis = this.getAxis(Axis.XAxis);
		final WAxis.Segment xs = xAxis.segments_.get(xSegment);
		final WAxis.Segment ys = yAxis.segments_.get(ySegment);
		double x1 = this.chartArea_.getLeft()
				+ xs.renderStart
				+ (xSegment == 0 ? xs.renderMinimum == 0 ? 0 : -this
						.getAxisPadding() : -xAxis.getSegmentMargin() / 2);
		double x2 = this.chartArea_.getLeft()
				+ xs.renderStart
				+ xs.renderLength
				+ (xSegment == xAxis.getSegmentCount() - 1 ? xs.renderMaximum == 0 ? 0
						: this.getAxisPadding()
						: xAxis.getSegmentMargin() / 2);
		double y1 = this.chartArea_.getBottom()
				- ys.renderStart
				- ys.renderLength
				- (ySegment == yAxis.getSegmentCount() - 1 ? ys.renderMaximum == 0 ? 0
						: this.getAxisPadding()
						: yAxis.getSegmentMargin() / 2);
		double y2 = this.chartArea_.getBottom()
				- ys.renderStart
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
		final WAxis yAxis = this.getAxis(Axis.YAxis);
		final WAxis y2Axis = this.getAxis(Axis.Y2Axis);
		Orientation yDir = this.orientation_;
		Orientation xDir = this.orientation_ == Orientation.Vertical ? Orientation.Horizontal
				: Orientation.Vertical;
		if (!xAxis.prepareRender(xDir, this.chartArea_.getWidth())) {
			return false;
		}
		if (!yAxis.prepareRender(yDir, this.chartArea_.getHeight())) {
			return false;
		}
		if (!y2Axis.prepareRender(yDir, this.chartArea_.getHeight())) {
			return false;
		}
		if (xAxis.getScale() == AxisScale.CategoryScale) {
			switch (xAxis.getLocation()) {
			case MinimumValue:
			case ZeroValue:
				this.location_[Axis.XAxis.getValue()] = AxisValue.MinimumValue;
				break;
			case MaximumValue:
				this.location_[Axis.XAxis.getValue()] = AxisValue.MaximumValue;
				break;
			case BothSides:
				this.location_[Axis.XAxis.getValue()] = AxisValue.BothSides;
			}
		}
		for (int i = 0; i < 2; ++i) {
			WAxis axis = i == 0 ? xAxis : yAxis;
			WAxis other = i == 0 ? yAxis : xAxis;
			AxisValue location = axis.getLocation();
			if (location == AxisValue.ZeroValue) {
				if (other.segments_.get(0).renderMaximum < 0) {
					location = AxisValue.MaximumValue;
				} else {
					if (other.segments_.get(0).renderMinimum > 0) {
						location = AxisValue.MinimumValue;
					}
				}
			} else {
				if (location == AxisValue.MinimumValue) {
					if (other.segments_.get(0).renderMinimum == 0
							&& axis.getTickDirection() == TickDirection.Outwards) {
						location = AxisValue.ZeroValue;
					}
				} else {
					if (location != AxisValue.BothSides) {
						if (other.segments_.get(0).renderMaximum == 0) {
							location = AxisValue.MaximumValue;
						}
					}
				}
			}
			this.location_[axis.getId().getValue()] = location;
		}
		if (y2Axis.isVisible()) {
			if (this.location_[Axis.Y1Axis.getValue()] == AxisValue.BothSides
					&& xAxis.segments_.get(0).renderMinimum == 0) {
				this.location_[Axis.Y1Axis.getValue()] = AxisValue.ZeroValue;
			}
			if (this.location_[Axis.Y1Axis.getValue()] == AxisValue.BothSides
					|| !(this.location_[Axis.Y1Axis.getValue()] == AxisValue.ZeroValue && xAxis.segments_
							.get(0).renderMinimum == 0)) {
				this.location_[Axis.Y1Axis.getValue()] = AxisValue.MinimumValue;
			}
			this.location_[Axis.Y2Axis.getValue()] = AxisValue.MaximumValue;
		} else {
			this.location_[Axis.Y2Axis.getValue()] = AxisValue.MaximumValue;
		}
		xAxis.setOtherAxisLocation(this.location_[Axis.YAxis.getValue()]);
		yAxis.setOtherAxisLocation(this.location_[Axis.XAxis.getValue()]);
		y2Axis.setOtherAxisLocation(this.location_[Axis.XAxis.getValue()]);
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
		this.renderAxis(painter, this.getAxis(Axis.XAxis), properties);
		this.renderAxis(painter, this.getAxis(Axis.Y1Axis), properties);
		this.renderAxis(painter, this.getAxis(Axis.Y2Axis), properties);
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
		if (this.getAxis(Axis.Y1Axis).isVisible()
				&& this.getAxis(Axis.Y1Axis).getTickDirection() == TickDirection.Inwards
				&& (this.location_[Axis.Y1Axis.getValue()] == AxisValue.BothSides || this.location_[Axis.Y1Axis
						.getValue()] == AxisValue.MaximumValue)
				|| this.getAxis(Axis.Y2Axis).isVisible()
				&& this.getAxis(Axis.Y2Axis).getTickDirection() == TickDirection.Inwards) {
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
				if (this.axes_[Axis.Y2Axis.getValue()].isVisible()) {
					caxis = this.axes_[Axis.Y2Axis.getValue()];
				} else {
					if (this.axes_[Axis.Y1Axis.getValue()].isVisible()
							&& (this.axes_[Axis.Y1Axis.getValue()]
									.getLocation() == AxisValue.BothSides || this.axes_[Axis.Y1Axis
									.getValue()].getLocation() == AxisValue.MaximumValue)) {
						caxis = this.axes_[Axis.Y1Axis.getValue()];
					}
				}
				if (caxis != null
						&& caxis.getTitleOrientation() == Orientation.Vertical) {
					titleOrientation = Orientation.Vertical;
				}
			} else {
				if (this.getLegendSide() == Side.Left) {
					caxis = this.axes_[Axis.YAxis.getValue()];
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
							Orientation.Vertical) + this.axes_[Axis.Y2Axis
							.getValue()].calcMaxTickLabelSize(device,
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
							WPaintDevice.FeatureFlag.HasFontMetrics).equals(0)) {
				int columnWidth = 0;
				for (int i = 0; i < this.getSeries().size(); ++i) {
					if (this.getSeries().get(i).isLegendEnabled()) {
						WString s = StringUtils.asString(this.getModel()
								.getHeaderData(
										this.getSeries().get(i)
												.getModelColumn()));
						WTextItem t = painter.getDevice().measureText(s);
						columnWidth = Math.max(columnWidth, (int) t.getWidth());
					}
				}
				WCartesianChart self = this;
				self.legend_
						.setLegendColumnWidth(new WLength(columnWidth + 25));
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
		boolean vertical = axis.getId() != Axis.XAxis;
		if (this.isInteractive()) {
			WRectF clipRect = null;
			WRectF area = this.hv(this.chartArea_);
			if (axis.getLocation() == AxisValue.ZeroValue
					&& this.location_[axis.getId().getValue()] == AxisValue.ZeroValue) {
				clipRect = area;
			} else {
				if (vertical != (this.getOrientation() == Orientation.Horizontal)) {
					double h = area.getHeight();
					if (this.location_[Axis.XAxis.getValue()] == AxisValue.ZeroValue
							&& this.getOrientation() == Orientation.Vertical) {
						h += 1;
					}
					clipRect = new WRectF(0.0, area.getTop(),
							vertical ? this.width_ : this.height_, h);
				} else {
					clipRect = new WRectF(area.getLeft(), 0.0, area.getWidth(),
							vertical ? this.height_ : this.width_);
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
		if (this.location_[axis.getId().getValue()] == AxisValue.BothSides) {
			locations.add(AxisValue.MinimumValue);
			locations.add(AxisValue.MaximumValue);
		} else {
			locations.add(this.location_[axis.getId().getValue()]);
		}
		for (int l = 0; l < locations.size(); ++l) {
			WPointF axisStart = new WPointF();
			WPointF axisEnd = new WPointF();
			double tickStart = 0.0;
			double tickEnd = 0.0;
			double labelPos = 0.0;
			AlignmentFlag labelHFlag = AlignmentFlag.AlignCenter;
			AlignmentFlag labelVFlag = AlignmentFlag.AlignMiddle;
			if (vertical) {
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
				if (vertical) {
					if (axis.getTickDirection() == TickDirection.Inwards) {
						tickStart = 0;
						tickEnd = TICK_LENGTH;
						labelPos = TICK_LENGTH;
						labelHFlag = AlignmentFlag.AlignLeft;
						double x = this.chartArea_.getLeft();
						axisStart.setX(x);
						axisEnd.setX(x);
					} else {
						tickStart = -TICK_LENGTH;
						tickEnd = 0;
						labelPos = -TICK_LENGTH;
						labelHFlag = AlignmentFlag.AlignRight;
						double x = this.chartArea_.getLeft() - axis.getMargin();
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
				if (vertical) {
					if (axis.getTickDirection() == TickDirection.Inwards) {
						tickStart = -TICK_LENGTH;
						tickEnd = 0;
						labelPos = -TICK_LENGTH;
						labelHFlag = AlignmentFlag.AlignRight;
						double x = this.chartArea_.getRight() - 1;
						axisStart.setX(x);
						axisEnd.setX(x);
					} else {
						tickStart = 0;
						tickEnd = TICK_LENGTH;
						labelPos = TICK_LENGTH;
						labelHFlag = AlignmentFlag.AlignLeft;
						double x = this.chartArea_.getRight()
								+ axis.getMargin();
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
				if (vertical) {
					double x = this.map(0, 0, Axis.YAxis).getX();
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
					double y = this.map(0, 0, Axis.YAxis).getY();
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
				if (vertical) {
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
			if (vertical) {
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
				boolean invertTicks = !vertical;
				if (invertTicks) {
					tickStart = -tickStart;
					tickEnd = -tickEnd;
					labelPos = -labelPos;
				}
			}
			List<WPen> pens = new ArrayList<WPen>();
			List<WPen> textPens = new ArrayList<WPen>();
			final Map<Axis, List<WCartesianChart.PenAssignment>> penMap = this.pens_;
			if (this.isInteractive()
					&& (axis.getId() == Axis.XAxis || axis.getId() == Axis.YAxis)) {
				for (int i = 0; i < penMap.get(axis.getId()).size(); ++i) {
					pens.add(penMap.get(axis.getId()).get(i).pen.getValue());
					textPens.add(penMap.get(axis.getId()).get(i).textPen
							.getValue());
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
				if (vertical && this.getOrientation() == Orientation.Vertical) {
					transform.assign(new WTransform(1, 0, 0, -1, 0, area
							.getBottom()).multiply(this.yTransform_).multiply(
							new WTransform(1, 0, 0, -1, 0, area.getBottom())));
				} else {
					if (vertical
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
			axis.render(painter, properties, axisStart, axisEnd, tickStart,
					tickEnd, labelPos, EnumSet.of(labelHFlag, labelVFlag),
					transform, pens, textPens);
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
		WPainterPath gridPath = new WPainterPath();
		List<Double> gridPos = ax.getGridLinePositions();
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
		if (this.isInteractive()) {
			painter.save();
			WPainterPath clipPath = new WPainterPath();
			clipPath.addRect(this.hv(this.chartArea_));
			painter.setClipPath(clipPath);
			painter.setClipping(true);
		}
		painter.strokePath(
				this.getCombinedTransform().map(gridPath).getCrisp(),
				ax.getGridLinesPen());
		if (this.isInteractive()) {
			painter.restore();
		}
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

	protected DomElement createDomElement(WApplication app) {
		if (this.isInteractive()) {
			this.createPensForAxis(Axis.XAxis);
			this.createPensForAxis(Axis.YAxis);
		}
		DomElement res = super.createDomElement(app);
		return res;
	}

	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		if (this.isInteractive()) {
			this.clearPens();
			this.createPensForAxis(Axis.XAxis);
			this.createPensForAxis(Axis.YAxis);
		}
		super.getDomChanges(result, app);
	}

	protected void render(EnumSet<RenderFlag> flags) {
		super.render(flags);
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WCartesianChart.js", wtjs1());
	}

	protected void setFormData(final WObject.FormData formData) {
		super.setFormData(formData);
		if (!this.getAxis(Axis.XAxis).zoomDirty_) {
			double z = this.xTransformHandle_.getValue().getM11();
			if (z != this.getAxis(Axis.XAxis).getZoom()) {
				this.getAxis(Axis.XAxis).setZoom(z);
			}
		}
		if (!this.getAxis(Axis.Y1Axis).zoomDirty_) {
			double z = this.yTransformHandle_.getValue().getM22();
			if (z != this.getAxis(Axis.Y1Axis).getZoom()) {
				this.getAxis(Axis.Y1Axis).setZoom(z);
			}
		}
		WPointF devicePan = new WPointF(this.xTransformHandle_.getValue()
				.getDx() / this.xTransformHandle_.getValue().getM11(),
				this.yTransformHandle_.getValue().getDy()
						/ this.yTransformHandle_.getValue().getM22());
		WPointF modelPan = new WPointF(this.getAxis(Axis.XAxis).mapFromDevice(
				-devicePan.getX()), this.getAxis(Axis.Y1Axis).mapFromDevice(
				-devicePan.getY()));
		if (!this.getAxis(Axis.XAxis).panDirty_) {
			double x = modelPan.getX();
			if (x != this.getAxis(Axis.XAxis).getPan()) {
				this.getAxis(Axis.XAxis).setPan(x);
			}
		}
		if (!this.getAxis(Axis.Y1Axis).panDirty_) {
			double y = modelPan.getY();
			if (y != this.getAxis(Axis.Y1Axis).getPan()) {
				this.getAxis(Axis.Y1Axis).setPan(y);
			}
		}
	}

	int getSeriesIndexOf(int modelColumn) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() == modelColumn) {
				return i;
			}
		}
		return -1;
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
			}
		}
		this.pens_.clear();
	}

	private void createPensForAxis(Axis ax) {
		if (!this.getAxis(ax).isVisible()) {
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
			assignments.add(new WCartesianChart.PenAssignment(pen, textPen));
		}
		this.pens_.put(ax, assignments);
	}

	String getCObjJsRef() {
		return "jQuery.data(" + this.getJsRef() + ",'cobj')";
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
		this.curvePaths_.put(series.getModelColumn(), handle);
	}

	private void freeJSPathsForSeries(int modelColumn) {
		this.freePainterPaths_.add(this.curvePaths_.get(modelColumn));
		this.curvePaths_.remove(modelColumn);
	}

	private void freeAllJSPaths() {
		for (Iterator<Map.Entry<Integer, WJavaScriptHandle<WPainterPath>>> it_it = this.curvePaths_
				.entrySet().iterator(); it_it.hasNext();) {
			Map.Entry<Integer, WJavaScriptHandle<WPainterPath>> it = it_it
					.next();
			this.freePainterPaths_.add(it.getValue());
		}
		this.curvePaths_.clear();
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
		js.append(this.getAxis(Axis.XAxis).getTextPen().getColor().getAlpha());
		js.append("],y:[");
		js.append(this.getAxis(Axis.YAxis).getPen().getColor().getAlpha())
				.append(',');
		js.append(this.getAxis(Axis.YAxis).getTextPen().getColor().getAlpha())
				.append("]},");
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

	private boolean isInteractive() {
		return (this.zoomEnabled_ || this.panEnabled_ || this.crosshairEnabled_
				|| this.followCurve_ >= 0 || this.axisSliderWidgets_.size() > 0)
				&& this.getMethod() == WPaintedWidget.Method.HtmlCanvas;
	}

	WPainterPath pathForSeries(int modelColumn) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getType() == SeriesType.LineSeries
					|| this.series_.get(i).getType() == SeriesType.CurveSeries) {
				if (this.series_.get(i).getModelColumn() == modelColumn) {
					return this.curvePaths_.get(
							this.series_.get(i).getModelColumn()).getValue();
				}
			}
		}
		return new WPainterPath();
	}

	WTransform getCombinedTransform() {
		return this.combinedTransform(this.xTransform_, this.yTransform_);
	}

	private WTransform combinedTransform(WTransform xTransform,
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

	private void setZoomAndPan() {
		double xPan = -this.getAxis(Axis.XAxis).mapToDevice(
				this.getAxis(Axis.XAxis).getPan(), 0);
		double yPan = -this.getAxis(Axis.YAxis).mapToDevice(
				this.getAxis(Axis.YAxis).getPan(), 0);
		double xZoom = this.getAxis(Axis.XAxis).getZoom();
		if (xZoom > this.getAxis(Axis.XAxis).getMaxZoom()) {
			xZoom = this.getAxis(Axis.XAxis).getMaxZoom();
		}
		double yZoom = this.getAxis(Axis.YAxis).getZoom();
		if (yZoom > this.getAxis(Axis.YAxis).getMaxZoom()) {
			yZoom = this.getAxis(Axis.YAxis).getMaxZoom();
		}
		WTransform xTransform = new WTransform(xZoom, 0, 0, 1, xZoom * xPan, 0);
		WTransform yTransform = new WTransform(1, 0, 0, yZoom, 0, yZoom * yPan);
		WRectF chartArea = this.hv(this.chartArea_);
		WRectF transformedArea = this.combinedTransform(xTransform, yTransform)
				.map(chartArea);
		if (transformedArea.getLeft() > chartArea.getLeft()) {
			double diff = chartArea.getLeft() - transformedArea.getLeft();
			if (this.getOrientation() == Orientation.Vertical) {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			} else {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, diff)
						.multiply(yTransform));
			}
			transformedArea = this.combinedTransform(xTransform, yTransform)
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
			transformedArea = this.combinedTransform(xTransform, yTransform)
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
			transformedArea = this.combinedTransform(xTransform, yTransform)
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
			transformedArea = this.combinedTransform(xTransform, yTransform)
					.map(chartArea);
		}
		this.xTransformHandle_.setValue(xTransform);
		this.yTransformHandle_.setValue(yTransform);
		this.getAxis(Axis.XAxis).zoomDirty_ = false;
		this.getAxis(Axis.XAxis).panDirty_ = false;
		this.getAxis(Axis.Y1Axis).zoomDirty_ = false;
		this.getAxis(Axis.Y1Axis).panDirty_ = false;
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

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(E,v,y,d){function Y(){return d.crosshair||d.followCurve!==-1}function sa(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function j(a){if(a===h)return d.xTransform;if(a===i)return d.yTransform}function Z(){if(d.isHorizontal){var a=p(d.area),b=q(d.area);return C([0,1,1,0,a,b],C(j(h),C(j(i),[0,1,1,0,-b,-a])))}else{a=p(d.area);b=r(d.area);return C([1,0,0,-1,a,b],C(j(h),C(j(i),[1,0,0, -1,-a,b])))}}function G(){return C(Z(),d.area)}function P(a,b){if(b===undefined)b=false;a=b?a:C(ta(Z()),a);a=d.isHorizontal?[(a[i]-d.area[1])/d.area[3],(a[h]-d.area[0])/d.area[2]]:[(a[h]-d.area[0])/d.area[2],1-(a[i]-d.area[1])/d.area[3]];return[d.modelArea[0]+a[h]*d.modelArea[2],d.modelArea[1]+a[i]*d.modelArea[3]]}function W(a,b){if(b===undefined)b=false;if(d.isHorizontal){a=[(a[h]-d.modelArea[0])/d.modelArea[2],(a[i]-d.modelArea[1])/d.modelArea[3]];a=[d.area[0]+a[i]*d.area[2],d.area[1]+a[h]*d.area[3]]}else{a= [(a[h]-d.modelArea[0])/d.modelArea[2],1-(a[i]-d.modelArea[1])/d.modelArea[3]];a=[d.area[0]+a[h]*d.area[2],d.area[1]+a[i]*d.area[3]]}return b?a:C(Z(),a)}function Ba(a,b){return b[0][a]<b[b.length-1][a]}function Ia(a,b){var f=h;if(d.isHorizontal)f=i;var e=Ba(f,b),g=ka(a,b,e);if(g<0)g=0;if(g>=b.length)return[b[b.length-1][h],b[b.length-1][i]];if(g>=b.length)g=b.length-2;if(b[g][f]===a)return[b[g][h],b[g][i]];var k=e?g+1:g-1;if(e&&b[k][2]==X)k+=2;if(!e&&k<0)return[b[g][h],b[g][i]];if(!e&&k>0&&b[k][2]== aa)k-=2;e=Math.abs(a-b[g][f]);a=Math.abs(b[k][f]-a);return e<a?[b[g][h],b[g][i]]:[b[k][h],b[k][i]]}function ka(a,b,f){function e(u){return f?b[u]:b[m-1-u]}function g(u){for(;e(u)[2]===X||e(u)[2]===aa;)u--;return u}var k=h;if(d.isHorizontal)k=i;var m=b.length,n=Math.floor(m/2);n=g(n);var o=0,z=m,s=false;if(e(0)[k]>a)return f?-1:m;if(e(m-1)[k]<a)return f?m:-1;for(;!s;){var x=n+1;if(e(x)[2]===X||e(x)[2]===aa)x+=2;if(e(n)[k]>a){z=n;n=Math.floor((z+o)/2);n=g(n)}else if(e(n)[k]===a)s=true;else if(e(x)[k]> a)s=true;else if(e(x)[k]===a){n=x;s=true}else{o=n;n=Math.floor((z+o)/2);n=g(n)}}return f?n:m-1-n}function ha(){var a,b;if(d.isHorizontal){a=(P([0,q(d.area)])[0]-d.modelArea[0])/d.modelArea[2];b=(P([0,r(d.area)])[0]-d.modelArea[0])/d.modelArea[2]}else{a=(P([p(d.area),0])[0]-d.modelArea[0])/d.modelArea[2];b=(P([t(d.area),0])[0]-d.modelArea[0])/d.modelArea[2]}var f;for(f=0;f<d.sliders.length;++f){var e=$(\"#\"+d.sliders[f]);if(e)(e=e.data(\"sobj\"))&&e.changeRange(a,b)}}function ba(){Q&&Ca(function(){y.repaint(); Y()&&ua()})}function ua(){if(Q){var a=D.getContext(\"2d\");a.clearRect(0,0,D.width,D.height);a.save();a.beginPath();a.moveTo(p(d.area),q(d.area));a.lineTo(t(d.area),q(d.area));a.lineTo(t(d.area),r(d.area));a.lineTo(p(d.area),r(d.area));a.closePath();a.clip();var b=C(ta(Z()),w),f=w[h],e=w[i];if(d.followCurve!==-1){b=Ia(d.isHorizontal?b[i]:b[h],d.series[d.followCurve]);e=C(Z(),b);f=e[h];e=e[i];w[h]=f;w[i]=e}b=d.isHorizontal?[(b[i]-d.area[1])/d.area[3],(b[h]-d.area[0])/d.area[2]]:[(b[h]-d.area[0])/d.area[2], 1-(b[i]-d.area[1])/d.area[3]];b=[d.modelArea[0]+b[h]*d.modelArea[2],d.modelArea[1]+b[i]*d.modelArea[3]];a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var g=b[0].toFixed(2);b=b[1].toFixed(2);if(g==\"-0.00\")g=\"0.00\";if(b==\"-0.00\")b=\"0.00\";a.fillText(\"(\"+g+\",\"+b+\")\",t(d.area)-d.coordinateOverlayPadding[0],q(d.area)+d.coordinateOverlayPadding[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(f)+0.5,Math.floor(q(d.area))+0.5);a.lineTo(Math.floor(f)+0.5,Math.floor(r(d.area))+ 0.5);a.moveTo(Math.floor(p(d.area))+0.5,Math.floor(e)+0.5);a.lineTo(Math.floor(t(d.area))+0.5,Math.floor(e)+0.5);a.stroke();a.restore()}}function ca(a,b){var f;if(a.x!==undefined){f=a.x;a=a.y}else{f=a[0];a=a[1]}return f>=p(b)&&f<=t(b)&&a>=q(b)&&a<=r(b)}function Ja(a){return q(a)<=q(d.area)+la&&r(a)>=r(d.area)-la&&p(a)<=p(d.area)+la&&t(a)>=t(d.area)-la}function K(a){var b=G();if(d.isHorizontal)if(a===da)a=ea;else if(a===ea)a=da;if(a===undefined||a===da)if(j(h)[0]<1){j(h)[0]=1;b=G()}if(a===undefined|| a===ea)if(j(i)[3]<1){j(i)[3]=1;b=G()}if(a===undefined||a===da){if(p(b)>p(d.area)){b=p(d.area)-p(b);if(d.isHorizontal)j(i)[5]=j(i)[5]+b;else j(h)[4]=j(h)[4]+b;b=G()}if(t(b)<t(d.area)){b=t(d.area)-t(b);if(d.isHorizontal)j(i)[5]=j(i)[5]+b;else j(h)[4]=j(h)[4]+b;b=G()}}if(a===undefined||a===ea){if(q(b)>q(d.area)){b=q(d.area)-q(b);if(d.isHorizontal)j(h)[4]=j(h)[4]+b;else j(i)[5]=j(i)[5]-b;b=G()}if(r(b)<r(d.area)){b=r(d.area)-r(b);if(d.isHorizontal)j(h)[4]=j(h)[4]+b;else j(i)[5]=j(i)[5]-b;G()}}}function Ka(){if(Y&& (D===undefined||y.canvas.width!==D.width||y.canvas.height!==D.height)){if(D){D.parentNode.removeChild(D);jQuery.removeData(v,\"oobj\");D=undefined}c=document.createElement(\"canvas\");c.setAttribute(\"width\",y.canvas.width);c.setAttribute(\"height\",y.canvas.height);c.style.position=\"absolute\";c.style.display=\"block\";c.style.left=\"0\";c.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){c.style.msTouchAction=\"none\";c.style.touchAction=\"none\"}y.canvas.parentNode.appendChild(c);D=c;jQuery.data(v, \"oobj\",D)}else if(D!==undefined&&!Y()){D.parentNode.removeChild(D);jQuery.removeData(v,\"oobj\");D=undefined}if(w===null)w=W([(p(d.modelArea)+t(d.modelArea))/2,(q(d.modelArea)+r(d.modelArea))/2])}function La(a,b){var f=Math.cos(a);a=Math.sin(a);var e=f*a,g=-b[0]*f-b[1]*a;return[f*f,e,e,a*a,f*g+b[0],a*g+b[1]]}function Ma(a,b,f){a=[b[h]-a[h],b[i]-a[i]];return f*f>=a[h]*a[h]+a[i]*a[i]}function va(a,b){if(fa){var f=Date.now();if(b===undefined)b=f-M;var e={x:0,y:0},g=G(),k=Na;if(b>2*ia){Q=false;var m=Math.floor(b/ ia-1),n;for(n=0;n<m;++n){va(a,ia);if(!fa){Q=true;ba();return}}b-=m*ia;Q=true}if(l.x===Infinity||l.x===-Infinity)l.x=l.x>0?S:-S;if(isFinite(l.x)){l.x/=1+Da*b;g[0]+=l.x*b;if(p(g)>p(d.area)){l.x+=-k*(p(g)-p(d.area))*b;l.x*=0.7}else if(t(g)<t(d.area)){l.x+=-k*(t(g)-t(d.area))*b;l.x*=0.7}if(Math.abs(l.x)<wa)if(p(g)>p(d.area))l.x=wa;else if(t(g)<t(d.area))l.x=-wa;if(Math.abs(l.x)>S)l.x=(l.x>0?1:-1)*S;e.x=l.x*b}if(l.y===Infinity||l.y===-Infinity)l.y=l.y>0?S:-S;if(isFinite(l.y)){l.y/=1+Da*b;g[1]+=l.y*b;if(q(g)> q(d.area)){l.y+=-k*(q(g)-q(d.area))*b;l.y*=0.7}else if(r(g)<r(d.area)){l.y+=-k*(r(g)-r(d.area))*b;l.y*=0.7}if(Math.abs(l.y)<0.001)if(q(g)>q(d.area))l.y=0.001;else if(r(g)<r(d.area))l.y=-0.001;if(Math.abs(l.y)>S)l.y=(l.y>0?1:-1)*S;e.y=l.y*b}g=G();I(e,ga);a=G();if(p(g)>p(d.area)&&p(a)<=p(d.area)){l.x=0;I({x:-e.x,y:0},ga);K(da)}if(t(g)<t(d.area)&&t(a)>=t(d.area)){l.x=0;I({x:-e.x,y:0},ga);K(da)}if(q(g)>q(d.area)&&q(a)<=q(d.area)){l.y=0;I({x:0,y:-e.y},ga);K(ea)}if(r(g)<r(d.area)&&r(a)>=r(d.area)){l.y= 0;I({x:0,y:-e.y},ga);K(ea)}if(Math.abs(l.x)<Ea&&Math.abs(l.y)<Ea&&Ja(a)){K();fa=false;F=null;l.x=0;l.y=0;M=null;A=[]}else{M=f;Q&&ma(va)}}}function Fa(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1}function na(){var a,b,f=Fa(j(h)[0])-1;if(f>=d.pens.x.length)f=d.pens.x.length-1;for(a=0;a<d.pens.x.length;++a)if(f===a)for(b=0;b<d.pens.x[a].length;++b)d.pens.x[a][b].color[3]=d.penAlpha.x[b];else for(b=0;b<d.pens.x[a].length;++b)d.pens.x[a][b].color[3]=0;f=Fa(j(i)[3])-1;if(f>=d.pens.y.length)f=d.pens.y.length- 1;for(a=0;a<d.pens.y.length;++a)if(f===a)for(b=0;b<d.pens.y[a].length;++b)d.pens.y[a][b].color[3]=d.penAlpha.y[b];else for(b=0;b<d.pens.y[a].length;++b)d.pens.y[a][b].color[3]=0}function I(a,b){var f=P(w);if(d.isHorizontal)a={x:a.y,y:-a.x};if(b&ga){j(h)[4]=j(h)[4]+a.x;j(i)[5]=j(i)[5]-a.y}else if(b&Ga){b=G();if(p(b)>p(d.area)){if(a.x>0)a.x/=1+(p(b)-p(d.area))*oa}else if(t(b)<t(d.area))if(a.x<0)a.x/=1+(t(d.area)-t(b))*oa;if(q(b)>q(d.area)){if(a.y>0)a.y/=1+(q(b)-q(d.area))*oa}else if(r(b)<r(d.area))if(a.y< 0)a.y/=1+(r(d.area)-r(b))*oa;j(h)[4]=j(h)[4]+a.x;j(i)[5]=j(i)[5]-a.y;w[h]+=a.x;w[i]+=a.y}else{j(h)[4]=j(h)[4]+a.x;j(i)[5]=j(i)[5]-a.y;w[h]+=a.x;w[i]+=a.y;K()}a=W(f);w[h]=a[h];w[i]=a[i];ba();ha()}function ja(a,b,f){var e=P(w),g;g=d.isHorizontal?[a.y-q(d.area),a.x-p(d.area)]:C(ta([1,0,0,-1,p(d.area),r(d.area)]),[a.x,a.y]);a=g[0];g=g[1];var k=Math.pow(1.2,d.isHorizontal?f:b);b=Math.pow(1.2,d.isHorizontal?b:f);if(j(h)[0]*k>d.maxZoom[h])k=d.maxZoom[h]/j(h)[0];if(k<1||j(h)[0]!==d.maxZoom[h])pa(j(h),C([k, 0,0,1,a-k*a,0],j(h)));if(j(i)[3]*b>d.maxZoom[i])b=d.maxZoom[i]/j(i)[3];if(b<1||j(i)[3]!==d.maxZoom[i])pa(j(i),C([1,0,0,b,0,g-b*g],j(i)));K();e=W(e);w[h]=e[h];w[i]=e[i];na();ba();ha()}var ia=17,ma=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,ia)}}(),xa=false,Ca=function(a){if(!xa){xa=true;ma(function(){a();xa=false})}};if(window.MSPointerEvent||window.PointerEvent){v.style.touchAction=\"none\";y.canvas.style.msTouchAction= \"none\";y.canvas.style.touchAction=\"none\"}var X=2,aa=3,ga=1,Ga=2,da=1,ea=2,h=0,i=1,J={},N=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){if(pointers.length>0&&!N)N=true;else if(pointers.length<=0&&N)N=false}function b(k){if(sa(k)){k.preventDefault();pointers.push(k);a();J.start(v,{touches:pointers.slice(0)})}}function f(k){if(N)if(sa(k)){k.preventDefault();var m;for(m=0;m<pointers.length;++m)if(pointers[m].pointerId===k.pointerId){pointers.splice(m,1);break}a();J.end(v, {touches:pointers.slice(0),changedTouches:[]})}}function e(k){if(sa(k)){k.preventDefault();var m;for(m=0;m<pointers.length;++m)if(pointers[m].pointerId===k.pointerId){pointers[m]=k;break}a();J.moved(v,{touches:pointers.slice(0)})}}pointers=[];var g=jQuery.data(v,\"eobj\");if(g)if(window.PointerEvent){v.removeEventListener(\"pointerdown\",g.pointerDown);v.removeEventListener(\"pointerup\",g.pointerUp);v.removeEventListener(\"pointerout\",g.pointerUp);v.removeEventListener(\"pointermove\",g.pointerMove)}else{v.removeEventListener(\"MSPointerDown\", g.pointerDown);v.removeEventListener(\"MSPointerUp\",g.pointerUp);v.removeEventListener(\"MSPointerOut\",g.pointerUp);v.removeEventListener(\"MSPointerMove\",g.pointerMove)}jQuery.data(v,\"eobj\",{pointerDown:b,pointerUp:f,pointerMove:e});if(window.PointerEvent){v.addEventListener(\"pointerdown\",b);v.addEventListener(\"pointerup\",f);v.addEventListener(\"pointerout\",f);v.addEventListener(\"pointermove\",e)}else{v.addEventListener(\"MSPointerDown\",b);v.addEventListener(\"MSPointerUp\",f);v.addEventListener(\"MSPointerOut\", f);v.addEventListener(\"MSPointerMove\",e)}})();var Da=0.003,Na=2.0E-4,oa=0.07,la=3,wa=0.001,S=1.5,Ea=0.02;jQuery.data(v,\"cobj\",this);var T=this,B=E.WT;T.config=d;var D=jQuery.data(v,\"oobj\"),w=null,Q=true,F=null,A=[],U=false,V=false,H=null,ya=null,za=null,l={x:0,y:0},M=null,qa=null;E=B.gfxUtils;var C=E.transform_mult,ta=E.transform_inverted,pa=E.transform_assign,q=E.rect_top,r=E.rect_bottom,p=E.rect_left,t=E.rect_right,fa=false;y.combinedTransform=Z;this.bSearch=ka;this.mouseMove=function(a,b){setTimeout(function(){if(!N){var f= B.widgetCoordinates(y.canvas,b);if(ca(f,d.area))if(Y()&&Q){w=[f.x,f.y];Ca(ua)}}},0)};this.mouseDown=function(a,b){if(!N){a=B.widgetCoordinates(y.canvas,b);if(ca(a,d.area))F=a}};this.mouseUp=function(){N||(F=null)};this.mouseDrag=function(a,b){if(!N)if(F!==null){a=B.widgetCoordinates(y.canvas,b);if(ca(a,d.area)){B.buttons===1&&d.pan&&I({x:a.x-F.x,y:a.y-F.y});F=a}}};this.mouseWheel=function(a,b){a=d.wheelActions[(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey];if(a!==undefined){var f=B.widgetCoordinates(y.canvas, b);if(ca(f,d.area)){var e=B.normalizeWheel(b);if((a===4||a===5||a===6)&&d.pan){f=j(h)[4];var g=j(i)[5];if(a===6)I({x:-e.pixelX,y:-e.pixelY});else if(a===5)I({x:0,y:-e.pixelX-e.pixelY});else a===4&&I({x:-e.pixelX-e.pixelY,y:0});if(f!==j(h)[4]||g!==j(i)[5])B.cancelEvent(b)}else if(d.zoom){B.cancelEvent(b);b=-e.spinY;if(b===0)b=-e.spinX;if(a===1)ja(f,0,b);else if(a===0)ja(f,b,0);else if(a===2)ja(f,b,b);else if(a===3)e.pixelX!==0?ja(f,b,0):ja(f,0,b)}}}};J.start=function(a,b){U=b.touches.length===1;V= b.touches.length===2;if(U){fa=false;a=B.widgetCoordinates(y.canvas,b.touches[0]);if(!ca(a,d.area))return;qa=Y()&&Ma(w,[a.x,a.y],30)?1:0;M=Date.now();F=a;B.capture(null);B.capture(y.canvas)}else if(V&&d.zoom){fa=false;A=[B.widgetCoordinates(y.canvas,b.touches[0]),B.widgetCoordinates(y.canvas,b.touches[1])].map(function(e){return[e.x,e.y]});if(!A.every(function(e){return ca(e,d.area)})){V=null;return}B.capture(null);B.capture(y.canvas);H=Math.atan2(A[1][1]-A[0][1],A[1][0]-A[0][0]);ya=[(A[0][0]+A[1][0])/ 2,(A[0][1]+A[1][1])/2];a=Math.abs(Math.sin(H));var f=Math.abs(Math.cos(H));H=a<Math.sin(0.125*Math.PI)?0:f<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(H)>0?Math.PI/4:-Math.PI/4;za=La(H,ya)}else return;b.preventDefault&&b.preventDefault()};J.end=function(a,b){var f=Array.prototype.slice.call(b.touches),e=f.length===0;U=f.length===1;V=f.length===2;e||function(){var g;for(g=0;g<b.changedTouches.length;++g)(function(){for(var k=b.changedTouches[g].identifier,m=0;m<f.length;++m)if(f[m].identifier===k){f.splice(m, 1);return}})()}();e=f.length===0;U=f.length===1;V=f.length===2;if(e){ra=null;if(qa===0&&(isFinite(l.x)||isFinite(l.y))&&d.rubberBand){M=Date.now();fa=true;ma(va)}else{T.mouseUp(null,null);f=[];za=ya=H=null;if(M!=null){Date.now();M=null}}qa=null}else if(U||V)J.start(a,b)};var ra=null,Aa=null,Ha=null;J.moved=function(a,b){if(U||V)if(!(U&&F==null)){b.preventDefault&&b.preventDefault();Aa=B.widgetCoordinates(y.canvas,b.touches[0]);if(b.touches.length>1)Ha=B.widgetCoordinates(y.canvas,b.touches[1]);ra|| (ra=setTimeout(function(){if(U){var f=Aa,e=Date.now(),g={x:f.x-F.x,y:f.y-F.y},k=e-M;M=e;if(qa===1){w[h]+=g.x;w[i]+=g.y;Y()&&Q&&ma(ua)}else if(d.pan){l.x=g.x/k;l.y=g.y/k;I(g,d.rubberBand?Ga:0)}F=f}else if(V&&d.zoom){e=P(w);var m=(A[0][0]+A[1][0])/2,n=(A[0][1]+A[1][1])/2;f=[Aa,Ha].map(function(u){return H===0?[u.x,n]:H===Math.PI/2?[m,u.y]:C(za,[u.x,u.y])});g=Math.abs(A[1][0]-A[0][0]);k=Math.abs(f[1][0]-f[0][0]);var o=g>0?k/g:1;if(k===g||H===Math.PI/2)o=1;var z=(f[0][0]+f[1][0])/2;g=Math.abs(A[1][1]- A[0][1]);k=Math.abs(f[1][1]-f[0][1]);var s=g?k/g:1;if(k===g||H===0)s=1;var x=(f[0][1]+f[1][1])/2;d.isHorizontal&&function(){var u=o;o=s;s=u;u=z;z=x;x=u;u=m;m=n;n=u}();if(j(h)[0]*o>d.maxZoom[h])o=d.maxZoom[h]/j(h)[0];if(j(i)[3]*s>d.maxZoom[i])s=d.maxZoom[i]/j(i)[3];if(o!==1&&(o<1||j(h)[0]!==d.maxZoom[h]))pa(j(h),C([o,0,0,1,-o*m+z,0],j(h)));if(s!==1&&(s<1||j(i)[3]!==d.maxZoom[i]))pa(j(i),C([1,0,0,s,0,-s*n+x],j(i)));K();e=W(e);w[h]=e[h];w[i]=e[i];A=f;na();ba();ha()}ra=null},1))}};this.setXRange=function(a, b,f){b=d.modelArea[0]+d.modelArea[2]*b;f=d.modelArea[0]+d.modelArea[2]*f;if(b<p(d.modelArea))b=p(d.modelArea);if(f>t(d.modelArea))f=t(d.modelArea);var e=d.series[a];if(e.length!==0){a=W([b,0],true);var g=W([f,0],true),k=d.isHorizontal?i:h,m=d.isHorizontal?h:i,n=Ba(k,e),o=ka(a[k],e,n);if(n)if(o<0)o=0;else{o++;if(e[o][2]===X)o+=2}else if(o>=e.length-1)o=e.length-2;var z=ka(g[k],e,n);if(!n&&z<0)z=0;var s,x,u=Infinity,O=-Infinity;for(s=Math.min(o,z);s<=Math.max(o,z)&&s<e.length;++s)if(e[s][2]!==X&&e[s][2]!== aa){if(e[s][m]<u)u=e[s][m];if(e[s][m]>O)O=e[s][m]}if(n&&o>0||!n&&o<e.length-1){if(n){x=o-1;if(e[x][2]===aa)x-=2}else{x=o+1;if(e[x][2]===X)x+=2}s=(a[k]-e[x][k])/(e[o][k]-e[x][k]);o=e[x][m]+s*(e[o][m]-e[x][m]);if(o<u)u=o;if(o>O)O=o}if(n&&z<e.length-1||!n&&z>0){if(n){n=z+1;if(e[n][2]===X)n+=2}else{n=z-1;if(e[n][2]===aa)n-=2}s=(g[k]-e[z][k])/(e[n][k]-e[z][k]);o=e[z][m]+s*(e[n][m]-e[z][m]);if(o<u)u=o;if(o>O)O=o}b=d.modelArea[2]/(f-b);e=d.isHorizontal?2:3;f=d.area[e]/(O-u);f=d.area[e]/(d.area[e]/f+20); if(f>d.maxZoom[m])f=d.maxZoom[m];a=d.isHorizontal?[a[i]-q(d.area),(u+O)/2-d.area[2]/f/2-p(d.area)]:[a[h]-p(d.area),-((u+O)/2+d.area[3]/f/2-r(d.area))];m=P(w);j(h)[0]=b;j(i)[3]=f;j(h)[4]=-a[h]*b;j(i)[5]=-a[i]*f;a=W(m);w[h]=a[h];w[i]=a[i];K();na();ba();ha()}};this.getSeries=function(a){return d.series[a]};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))d[b]=a[b];Ka();na();ba();ha()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&& !window.PointerEvent){T.touchStart=J.start;T.touchEnd=J.end;T.touchMoved=J.moved}else{E=function(){};T.touchStart=E;T.touchEnd=E;T.touchMoved=E}}");
	}

	private static final int TICK_LENGTH = 5;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
