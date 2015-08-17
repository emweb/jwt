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
 * <li>Tooltips are not zoomed or panned along with the chart</li>
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
		this.initialZoomAndPanApplied_ = false;
		this.rubberBandEnabled_ = true;
		this.crosshairEnabled_ = false;
		this.followCurve_ = -1;
		this.cObjCreated_ = false;
		this.curvePaths_ = new HashMap<Integer, WJavaScriptHandle<WPainterPath>>();
		this.freePainterPaths_ = new ArrayList<WJavaScriptHandle<WPainterPath>>();
		this.xTransform_ = null;
		this.yTransform_ = null;
		this.pens_ = new HashMap<Axis, List<WCartesianChart.PenAssignment>>();
		this.freePens_ = new ArrayList<WJavaScriptHandle<WPen>>();
		this.axisSliderWidgets_ = new ArrayList<WAxisSliderWidget>();
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
		this.initialZoomAndPanApplied_ = false;
		this.rubberBandEnabled_ = true;
		this.crosshairEnabled_ = false;
		this.followCurve_ = -1;
		this.cObjCreated_ = false;
		this.curvePaths_ = new HashMap<Integer, WJavaScriptHandle<WPainterPath>>();
		this.freePainterPaths_ = new ArrayList<WJavaScriptHandle<WPainterPath>>();
		this.xTransform_ = null;
		this.yTransform_ = null;
		this.pens_ = new HashMap<Axis, List<WCartesianChart.PenAssignment>>();
		this.freePens_ = new ArrayList<WJavaScriptHandle<WPen>>();
		this.axisSliderWidgets_ = new ArrayList<WAxisSliderWidget>();
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
		painter.drawText(pos.getX() + 23, pos.getY() - 9, 100, 20, EnumSet.of(
				AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle),
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
				- this.chartArea_.getLeft()), yAxis
				.mapFromDevice(this.chartArea_.getBottom() - p.getY()));
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
			logger
					.error(new StringWriter()
							.append(
									"setAutoLayout(): device does not have font metrics (not even server-side font metrics).")
							.toString());
			autoLayout = false;
		}
		if (autoLayout) {
			WCartesianChart self = this;
			self.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
			self.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
			this.calcChartArea();
			if (this.chartArea_.getWidth() <= 5
					|| this.chartArea_.getHeight() <= 5
					|| !this.isPrepareAxes()) {
				return false;
			}
			WPaintDevice d = device;
			if (!(d != null)) {
				d = this.getCreatePaintDevice();
			}
			{
				WMeasurePaintDevice md = new WMeasurePaintDevice(d);
				WPainter painter = new WPainter(md);
				this.renderAxes(painter, EnumSet.of(AxisProperty.Line,
						AxisProperty.Labels));
				this.renderLegend(painter);
				WRectF bounds = md.getBoundingRect();
				final int MARGIN = 5;
				int corrLeft = (int) Math.max(0.0, rect.getLeft()
						- bounds.getLeft() + MARGIN);
				int corrRight = (int) Math.max(0.0, bounds.getRight()
						- rect.getRight() + MARGIN);
				int corrTop = (int) Math.max(0.0, rect.getTop()
						- bounds.getTop() + MARGIN);
				int corrBottom = (int) Math.max(0.0, bounds.getBottom()
						- rect.getBottom() + MARGIN);
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
		return this.chartArea_.getWidth() > 5
				&& this.chartArea_.getHeight() > 5 && this.isPrepareAxes();
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
	 * Set the pen of the border to be drawn around the chart area.
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
	 * The pen of the border to be drawn around the chart area.
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
	 * vertically. If you press alt+ctrl, it will only zoom horizontally.
	 * <p>
	 * When using touch, you can use a pinch gesture to zoom in/out. If the
	 * pinch gesture is vertical/horizontal, it will zoom only
	 * vertically/horizontally, otherwise it will zoom both axes equally.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @see WCartesianChart#isZoomEnabled()
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
	 * Enable the crosshair functionality.
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
	 * Enable the crosshair functionality.
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
	 * Enable the follow curve functionality for the data series corresponding
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
	 * Returns the curve that is to be followed, if the follow curve
	 * functionality is enabled, or -1 otherwise.
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
	 * Check whether the rubberband effect is enabled.
	 * <p>
	 */
	public boolean isRubberBandEffectEnabled() {
		return this.rubberBandEnabled_;
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
											iterator.newValue(this.series_
													.get(i), x,
													hasValue ? prevStack : y,
													nextStack, xIndex, yIndex);
										} else {
											iterator.newValue(this.series_
													.get(i), x,
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
					ss.append('"').append(
							this.axisSliderWidgets_.get(j).getId()).append('"');
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
	private boolean initialZoomAndPanApplied_;
	private boolean rubberBandEnabled_;
	private boolean crosshairEnabled_;
	private int followCurve_;
	boolean cObjCreated_;
	Map<Integer, WJavaScriptHandle<WPainterPath>> curvePaths_;
	private List<WJavaScriptHandle<WPainterPath>> freePainterPaths_;
	WJavaScriptHandle<WTransform> xTransform_;
	WJavaScriptHandle<WTransform> yTransform_;

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
		this.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
		this.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
		this.xTransform_ = this.createJSTransform();
		this.yTransform_ = this.createJSTransform();
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
			if (!this.initialZoomAndPanApplied_) {
				this.setInitialZoomAndPan();
				this.initialZoomAndPanApplied_ = true;
			}
			double modelBottom = this.getAxis(Axis.Y1Axis).mapFromDevice(0);
			double modelTop = this.getAxis(Axis.Y1Axis).mapFromDevice(
					this.chartArea_.getHeight());
			double modelLeft = this.getAxis(Axis.XAxis).mapFromDevice(0);
			double modelRight = this.getAxis(Axis.XAxis).mapFromDevice(
					this.chartArea_.getWidth());
			WRectF modelArea = new WRectF(modelLeft, modelBottom, modelRight
					- modelLeft, modelTop - modelBottom);
			char[] buf = new char[30];
			WApplication app = WApplication.getInstance();
			StringBuilder ss = new StringBuilder();
			ss
					.append("new Wt3_3_4.WCartesianChart(")
					.append(app.getJavaScriptClass())
					.append(",")
					.append(this.getJsRef())
					.append(",")
					.append(this.getObjJsRef())
					.append(",{isHorizontal:")
					.append(
							StringUtils
									.asString(
											this.getOrientation() == Orientation.Horizontal)
									.toString()).append(",zoom:").append(
							StringUtils.asString(this.zoomEnabled_).toString())
					.append(",pan:").append(
							StringUtils.asString(this.panEnabled_).toString())
					.append(",crosshair:").append(
							StringUtils.asString(this.crosshairEnabled_)
									.toString()).append(",followCurve:")
					.append(this.followCurve_).append(",xTransform:").append(
							this.xTransform_.getJsRef()).append(",yTransform:")
					.append(this.yTransform_.getJsRef()).append(",area:")
					.append(this.hv(this.chartArea_).getJsRef()).append(
							",modelArea:").append(modelArea.getJsRef()).append(
							",");
			this.updateJSPens(ss);
			ss.append("series:{");
			for (int i = 0; i < this.series_.size(); ++i) {
				if (this.series_.get(i).getType() == SeriesType.LineSeries
						|| this.series_.get(i).getType() == SeriesType.CurveSeries) {
					ss.append(this.series_.get(i).getModelColumn()).append(":")
							.append(
									this.curvePaths_.get(
											this.series_.get(i)
													.getModelColumn())
											.getJsRef()).append(",");
				}
			}
			ss.append("},");
			ss.append("maxZoom:[")
					.append(
							MathUtils.roundJs(this.getAxis(Axis.XAxis)
									.getMaxZoom(), 3)).append(",");
			ss
					.append(
							MathUtils.roundJs(this.getAxis(Axis.Y1Axis)
									.getMaxZoom(), 3)).append("],");
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
			ss.append("]");
			ss.append("});");
			this.doJavaScript(ss.toString());
			this.cObjCreated_ = true;
			for (int i = 0; i < this.axisSliderWidgets_.size(); ++i) {
				this.axisSliderWidgets_.get(i).update();
			}
		} else {
			this.initialZoomAndPanApplied_ = false;
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
			this.renderGrid(painter, this.getAxis(Axis.Y1Axis));
			this.renderGrid(painter, this.getAxis(Axis.Y2Axis));
			this.renderSeries(painter);
			this.renderAxes(painter, EnumSet.of(AxisProperty.Line,
					AxisProperty.Labels));
			this.renderBorder(painter);
			this.renderLegend(painter);
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
			painter.drawText(new WRectF(left, top, width, height), EnumSet.of(
					rHorizontalAlign, rVerticalAlign), text);
		} else {
			painter.rotate(-angle);
			painter.drawText(new WRectF(left, top, width, height), EnumSet.of(
					rHorizontalAlign, rVerticalAlign), text);
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
		return new WRectF(Math.floor(x1 + 0.5), Math.floor(y1 + 0.5), Math
				.floor(x2 - x1), Math.floor(y2 - y1));
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
					this.getPlotAreaPadding(Side.Top), Math.max(10, this.width_
							- this.getPlotAreaPadding(Side.Left)
							- this.getPlotAreaPadding(Side.Right)), Math.max(
							10, this.height_
									- this.getPlotAreaPadding(Side.Top)
									- this.getPlotAreaPadding(Side.Bottom)));
		} else {
			this.chartArea_ = new WRectF(this.getPlotAreaPadding(Side.Top),
					this.getPlotAreaPadding(Side.Right), Math.max(10,
							this.width_ - this.getPlotAreaPadding(Side.Top)
									- this.getPlotAreaPadding(Side.Bottom)),
					Math.max(10, this.height_
							- this.getPlotAreaPadding(Side.Right)
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
			final int TITLE_HEIGHT = 50;
			final int TITLE_PADDING = 10;
			painter.drawText(x - 500, this.getPlotAreaPadding(Side.Top)
					- TITLE_HEIGHT - TITLE_PADDING, 1000, TITLE_HEIGHT, EnumSet
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
							this
									.renderLabel(
											painter,
											axis.getTitle(),
											new WPointF(
													u
															+ (labelHFlag == AlignmentFlag.AlignRight ? 15
																	: -15),
													this.chartArea_.getTop() - 8),
											EnumSet.of(labelHFlag,
													AlignmentFlag.AlignBottom),
											0, 10);
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
							this
									.renderLabel(
											painter,
											axis.getTitle(),
											new WPointF(
													u
															+ (labelHFlag == AlignmentFlag.AlignRight ? -(size
																	+ titleSizeW + 5)
																	: +(size
																			+ titleSizeW + 5)),
													this.chartArea_.getCenter()
															.getY()),
											EnumSet.of(
													AlignmentFlag.AlignCenter,
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
								.of(
										locations.get(l) == AxisValue.MaximumValue ? AlignmentFlag.AlignLeft
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
								.of(
										locations.get(l) == AxisValue.MaximumValue ? AlignmentFlag.AlignBottom
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
							this
									.renderLabel(
											painter,
											axis.getTitle(),
											new WPointF(this.chartArea_
													.getCenter().getX(), u
													+ extraMargin),
											EnumSet.of(
													AlignmentFlag.AlignMiddle,
													AlignmentFlag.AlignCenter),
											locations.get(l) == AxisValue.MaximumValue ? -90
													: 90, 10);
						} else {
							EnumSet<AlignmentFlag> alignment = EnumSet
									.of(
											locations.get(l) == AxisValue.MaximumValue ? AlignmentFlag.AlignBottom
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
						area.getBottom()).multiply(this.xTransform_.getValue())
						.multiply(this.yTransform_.getValue()).multiply(
								new WTransform(1, 0, 0, -1, -area.getLeft(),
										area.getBottom())));
			} else {
				if (vertical && this.getOrientation() == Orientation.Vertical) {
					transform.assign(new WTransform(1, 0, 0, -1, 0, area
							.getBottom()).multiply(this.yTransform_.getValue())
							.multiply(
									new WTransform(1, 0, 0, -1, 0, area
											.getBottom())));
				} else {
					if (vertical
							&& this.getOrientation() == Orientation.Horizontal) {
						transform
								.assign(new WTransform(0, 1, 1, 0, area
										.getLeft(), 0).multiply(
										this.yTransform_.getValue()).multiply(
										new WTransform(0, 1, 1, 0, 0, -area
												.getLeft())));
					} else {
						if (this.getOrientation() == Orientation.Horizontal) {
							transform.assign(new WTransform(0, 1, 1, 0, 0, area
									.getTop()).multiply(
									this.xTransform_.getValue()).multiply(
									new WTransform(0, 1, 1, 0, -area.getTop(),
											0)));
						} else {
							transform.assign(new WTransform(1, 0, 0, 1, area
									.getLeft(), 0).multiply(
									this.xTransform_.getValue()).multiply(
									new WTransform(1, 0, 0, 1, -area.getLeft(),
											0)));
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
				this.getCombinedTransform().map(gridPath).getCrisp(), ax
						.getGridLinesPen());
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
		double initialZoom = this.getAxis(ax).getInitialZoom();
		if (initialZoom > this.getAxis(ax).getMaxZoom()) {
			initialZoom = this.getAxis(ax).getMaxZoom();
		}
		int initialLevel = toZoomLevel(initialZoom);
		List<WCartesianChart.PenAssignment> assignments = new ArrayList<WCartesianChart.PenAssignment>();
		for (int i = 1;; ++i) {
			double zoom = Math.pow(2.0, i - 1);
			if (zoom > this.getAxis(ax).getMaxZoom()) {
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
					.getGreen(), p.getColor().getBlue(), i == initialLevel ? p
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
					.getGreen(), p.getColor().getBlue(), i == initialLevel ? p
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
		return this.combinedTransform(this.xTransform_.getValue(),
				this.yTransform_.getValue());
	}

	private WTransform combinedTransform(WTransform xTransform,
			WTransform yTransform) {
		if (this.getOrientation() == Orientation.Vertical) {
			return new WTransform(1, 0, 0, -1, this.chartArea_.getLeft(),
					this.chartArea_.getBottom()).multiply(xTransform).multiply(
					yTransform).multiply(
					new WTransform(1, 0, 0, -1, -this.chartArea_.getLeft(),
							this.chartArea_.getBottom()));
		} else {
			WRectF area = this.hv(this.chartArea_);
			return new WTransform(0, 1, 1, 0, area.getLeft(), area.getTop())
					.multiply(xTransform).multiply(yTransform).multiply(
							new WTransform(0, 1, 1, 0, -area.getTop(), -area
									.getLeft()));
		}
	}

	private void setInitialZoomAndPan() {
		double xPan = -this.getAxis(Axis.XAxis).mapToDevice(
				this.getAxis(Axis.XAxis).getInitialPan(), 0);
		double yPan = -this.getAxis(Axis.YAxis).mapToDevice(
				this.getAxis(Axis.YAxis).getInitialPan(), 0);
		double xZoom = this.getAxis(Axis.XAxis).getInitialZoom();
		if (xZoom > this.getAxis(Axis.XAxis).getMaxZoom()) {
			xZoom = this.getAxis(Axis.XAxis).getMaxZoom();
		}
		double yZoom = this.getAxis(Axis.YAxis).getInitialZoom();
		if (yZoom > this.getAxis(Axis.YAxis).getMaxZoom()) {
			yZoom = this.getAxis(Axis.YAxis).getMaxZoom();
		}
		WTransform xTransform = new WTransform(xZoom, 0, 0, 1, xZoom * xPan, 0);
		WTransform yTransform = new WTransform(1, 0, 0, yZoom, 0, yZoom * yPan);
		WRectF transformedArea = this.combinedTransform(xTransform, yTransform)
				.map(this.chartArea_);
		if (transformedArea.getLeft() > this.chartArea_.getLeft()) {
			double diff = this.chartArea_.getLeft() - transformedArea.getLeft();
			if (this.getOrientation() == Orientation.Vertical) {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			} else {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, diff)
						.multiply(yTransform));
			}
			transformedArea = this.combinedTransform(xTransform, yTransform)
					.map(this.chartArea_);
		}
		if (transformedArea.getRight() < this.chartArea_.getRight()) {
			double diff = this.chartArea_.getRight()
					- transformedArea.getRight();
			if (this.getOrientation() == Orientation.Vertical) {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			} else {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, diff)
						.multiply(yTransform));
			}
			transformedArea = this.combinedTransform(xTransform, yTransform)
					.map(this.chartArea_);
		}
		if (transformedArea.getTop() > this.chartArea_.getTop()) {
			double diff = this.chartArea_.getTop() - transformedArea.getTop();
			if (this.getOrientation() == Orientation.Vertical) {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, -diff)
						.multiply(yTransform));
			} else {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			}
			transformedArea = this.combinedTransform(xTransform, yTransform)
					.map(this.chartArea_);
		}
		if (transformedArea.getBottom() < this.chartArea_.getBottom()) {
			double diff = this.chartArea_.getBottom()
					- transformedArea.getBottom();
			if (this.getOrientation() == Orientation.Vertical) {
				yTransform.assign(new WTransform(1, 0, 0, 1, 0, -diff)
						.multiply(yTransform));
			} else {
				xTransform.assign(new WTransform(1, 0, 0, 1, diff, 0)
						.multiply(xTransform));
			}
			transformedArea = this.combinedTransform(xTransform, yTransform)
					.map(this.chartArea_);
		}
		this.xTransform_.setValue(xTransform);
		this.yTransform_.setValue(yTransform);
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
				"function(G,u,x,b){function U(){return b.crosshair||b.followCurve!==-1}function ma(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function j(a){if(a===g)return b.xTransform;if(a===h)return b.yTransform}function da(){if(b.isHorizontal){var a=o(b.area),d=p(b.area);return z([0,1,1,0,a,d],z(j(g),z(j(h),[0,1,1,0,-d,-a])))}else{a=o(b.area);d=r(b.area);return z([1,0,0,-1,a,d],z(j(g),z(j(h),[1,0, 0,-1,-a,d])))}}function D(){return z(da(),b.area)}function M(a,d){if(d===undefined)d=false;a=d?a:z(na(da()),a);a=b.isHorizontal?[(a[h]-b.area[1])/b.area[3],(a[g]-b.area[0])/b.area[2]]:[(a[g]-b.area[0])/b.area[2],1-(a[h]-b.area[1])/b.area[3]];return[b.modelArea[0]+a[g]*b.modelArea[2],b.modelArea[1]+a[h]*b.modelArea[3]]}function S(a,d){if(d===undefined)d=false;if(b.isHorizontal){a=[(a[g]-b.modelArea[0])/b.modelArea[2],(a[h]-b.modelArea[1])/b.modelArea[3]];a=[b.area[0]+a[h]*b.area[2],b.area[1]+a[g]* b.area[3]]}else{a=[(a[g]-b.modelArea[0])/b.modelArea[2],1-(a[h]-b.modelArea[1])/b.modelArea[3]];a=[b.area[0]+a[g]*b.area[2],b.area[1]+a[h]*b.area[3]]}return d?a:z(da(),a)}function Aa(a,d){var f=g;if(b.isHorizontal)f=h;var e=oa(a,d);if(e<0)e=0;if(e>=d.length)e=d.length-2;if(d[e][f]===a)return[d[e][g],d[e][h]];var i=e+1;if(d[i][2]==V)i+=2;return a-d[e][f]<d[i][f]-a?[d[e][g],d[e][h]]:[d[i][g],d[i][h]]}function oa(a,d){function f(q){d[q][2]===pa&&--q;d[q][2]===V&&--q;return q}var e=g;if(b.isHorizontal)e= h;var i=d.length,k=Math.floor(i/2);k=f(k);var m=0,n=i,A=false;if(d[0][e]>a)return-1;if(d[i-1][e]<a)return i;for(;!A;){i=k+1;if(d[i][2]===V)i+=2;if(d[k][e]>a){n=k;k=Math.floor((n+m)/2);k=f(k)}else if(d[k][e]===a)A=true;else if(d[i][e]>a)A=true;else if(d[i][e]===a){k=i;A=true}else{m=k;k=Math.floor((n+m)/2);k=f(k)}}return k}function ea(){var a,d;if(b.isHorizontal){a=(M([0,p(b.area)])[0]-b.modelArea[0])/b.modelArea[2];d=(M([0,r(b.area)])[0]-b.modelArea[0])/b.modelArea[2]}else{a=(M([o(b.area),0])[0]-b.modelArea[0])/ b.modelArea[2];d=(M([s(b.area),0])[0]-b.modelArea[0])/b.modelArea[2]}var f;for(f=0;f<b.sliders.length;++f){var e=$(\"#\"+b.sliders[f]);if(e)(e=e.data(\"sobj\"))&&e.changeRange(a,d)}}function W(){N&&fa(function(){x.repaint();U()&&qa()})}function qa(){if(N){var a=B.getContext(\"2d\");a.clearRect(0,0,B.width,B.height);a.save();a.beginPath();a.moveTo(o(b.area),p(b.area));a.lineTo(s(b.area),p(b.area));a.lineTo(s(b.area),r(b.area));a.lineTo(o(b.area),r(b.area));a.closePath();a.clip();var d=z(na(da()),t),f=t[g], e=t[h];if(b.followCurve!==-1){d=Aa(b.isHorizontal?d[h]:d[g],b.series[b.followCurve]);e=z(da(),d);f=e[g];e=e[h];t[g]=f;t[h]=e}d=b.isHorizontal?[(d[h]-b.area[1])/b.area[3],(d[g]-b.area[0])/b.area[2]]:[(d[g]-b.area[0])/b.area[2],1-(d[h]-b.area[1])/b.area[3]];d=[b.modelArea[0]+d[g]*b.modelArea[2],b.modelArea[1]+d[h]*b.modelArea[3]];a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var i=d[0].toFixed(2);d=d[1].toFixed(2);if(i==\"-0.00\")i=\"0.00\";if(d==\"-0.00\")d=\"0.00\";a.fillText(\"(\"+i+\",\"+ d+\")\",s(b.area)-5,p(b.area)+5);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(f)+0.5,Math.floor(p(b.area))+0.5);a.lineTo(Math.floor(f)+0.5,Math.floor(r(b.area))+0.5);a.moveTo(Math.floor(o(b.area))+0.5,Math.floor(e)+0.5);a.lineTo(Math.floor(s(b.area))+0.5,Math.floor(e)+0.5);a.stroke();a.restore()}}function X(a,d){var f;if(a.x!==undefined){f=a.x;a=a.y}else{f=a[0];a=a[1]}return f>=o(d)&&f<=s(d)&&a>=p(d)&&a<=r(d)}function Ba(a){return p(a)<=p(b.area)+ha&&r(a)>=r(b.area)-ha&&o(a)<= o(b.area)+ha&&s(a)>=s(b.area)-ha}function H(a){var d=D();if(b.isHorizontal)if(a===Y)a=Z;else if(a===Z)a=Y;if(a===undefined||a===Y)if(j(g)[0]<1){j(g)[0]=1;d=D()}if(a===undefined||a===Z)if(j(h)[3]<1){j(h)[3]=1;d=D()}if(a===undefined||a===Y){if(o(d)>o(b.area)){d=o(b.area)-o(d);if(b.isHorizontal)j(h)[5]=j(h)[5]+d;else j(g)[4]=j(g)[4]+d;d=D()}if(s(d)<s(b.area)){d=s(b.area)-s(d);if(b.isHorizontal)j(h)[5]=j(h)[5]+d;else j(g)[4]=j(g)[4]+d;d=D()}}if(a===undefined||a===Z){if(p(d)>p(b.area)){d=p(b.area)-p(d); if(b.isHorizontal)j(g)[4]=j(g)[4]+d;else j(h)[5]=j(h)[5]-d;d=D()}if(r(d)<r(b.area)){d=r(b.area)-r(d);if(b.isHorizontal)j(g)[4]=j(g)[4]+d;else j(h)[5]=j(h)[5]-d;D()}}}function Ca(){if(U&&(B===undefined||x.canvas.width!==B.width||x.canvas.height!==B.height)){if(B){B.parentNode.removeChild(B);jQuery.removeData(u,\"oobj\");B=undefined}c=document.createElement(\"canvas\");c.setAttribute(\"width\",x.canvas.width);c.setAttribute(\"height\",x.canvas.height);c.style.position=\"absolute\";c.style.display=\"block\";c.style.left= \"0\";c.style.top=\"0\";c.style.msTouchAction=\"none\";x.canvas.parentNode.appendChild(c);B=c;jQuery.data(u,\"oobj\",B)}else if(B!==undefined&&!U()){B.parentNode.removeChild(B);jQuery.removeData(u,\"oobj\");B=undefined}if(t===null)t=S([(o(b.modelArea)+s(b.modelArea))/2,(p(b.modelArea)+r(b.modelArea))/2])}function Da(a,d){var f=Math.cos(a);a=Math.sin(a);var e=f*a,i=-d[0]*f-d[1]*a;return[f*f,e,e,a*a,f*i+d[0],a*i+d[1]]}function Ea(a,d,f){a=[d[g]-a[g],d[h]-a[h]];return f*f>=a[g]*a[g]+a[h]*a[h]}function ra(a,d){if(aa){var f= Date.now();if(d===undefined)d=f-I;var e={x:0,y:0},i=D(),k=Fa;if(d>2*ga){N=false;var m=Math.floor(d/ga-1),n;for(n=0;n<m;++n){ra(a,ga);if(!aa){N=true;W();return}}d-=m*ga;N=true}if(l.x===Infinity||l.x===-Infinity)l.x=l.x>0?O:-O;if(isFinite(l.x)){l.x/=1+wa*d;i[0]+=l.x*d;if(o(i)>o(b.area)){l.x+=-k*(o(i)-o(b.area))*d;l.x*=0.7}else if(s(i)<s(b.area)){l.x+=-k*(s(i)-s(b.area))*d;l.x*=0.7}if(Math.abs(l.x)<sa)if(o(i)>o(b.area))l.x=sa;else if(s(i)<s(b.area))l.x=-sa;if(Math.abs(l.x)>O)l.x=(l.x>0?1:-1)*O;e.x=l.x* d}if(l.y===Infinity||l.y===-Infinity)l.y=l.y>0?O:-O;if(isFinite(l.y)){l.y/=1+wa*d;i[1]+=l.y*d;if(p(i)>p(b.area)){l.y+=-k*(p(i)-p(b.area))*d;l.y*=0.7}else if(r(i)<r(b.area)){l.y+=-k*(r(i)-r(b.area))*d;l.y*=0.7}if(Math.abs(l.y)<0.001)if(p(i)>p(b.area))l.y=0.001;else if(r(i)<r(b.area))l.y=-0.001;if(Math.abs(l.y)>O)l.y=(l.y>0?1:-1)*O;e.y=l.y*d}i=D();P(e,ba);a=D();if(o(i)>o(b.area)&&o(a)<=o(b.area)){l.x=0;P({x:-e.x,y:0},ba);H(Y)}if(s(i)<s(b.area)&&s(a)>=s(b.area)){l.x=0;P({x:-e.x,y:0},ba);H(Y)}if(p(i)> p(b.area)&&p(a)<=p(b.area)){l.y=0;P({x:0,y:-e.y},ba);H(Z)}if(r(i)<r(b.area)&&r(a)>=r(b.area)){l.y=0;P({x:0,y:-e.y},ba);H(Z)}if(Math.abs(l.x)<xa&&Math.abs(l.y)<xa&&Ba(a)){H();aa=false;C=null;l.x=0;l.y=0;I=null;v=[]}else{I=f;N&&fa(ra)}}}function ya(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1}function ia(){var a,d,f=ya(j(g)[0])-1;if(f>=b.pens.x.length)f=b.pens.x.length-1;for(a=0;a<b.pens.x.length;++a)if(f===a)for(d=0;d<b.pens.x[a].length;++d)b.pens.x[a][d].color[3]=b.penAlpha.x[d];else for(d=0;d< b.pens.x[a].length;++d)b.pens.x[a][d].color[3]=0;f=ya(j(h)[3])-1;if(f>=b.pens.y.length)f=b.pens.y.length-1;for(a=0;a<b.pens.y.length;++a)if(f===a)for(d=0;d<b.pens.y[a].length;++d)b.pens.y[a][d].color[3]=b.penAlpha.y[d];else for(d=0;d<b.pens.y[a].length;++d)b.pens.y[a][d].color[3]=0}function P(a,d){var f=M(t);if(b.isHorizontal)a={x:a.y,y:-a.x};if(d&ba){j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y}else if(d&za){d=D();if(o(d)>o(b.area)){if(a.x>0)a.x/=1+(o(d)-o(b.area))*ja}else if(s(d)<s(b.area))if(a.x<0)a.x/= 1+(s(b.area)-s(d))*ja;if(p(d)>p(b.area)){if(a.y>0)a.y/=1+(p(d)-p(b.area))*ja}else if(r(d)<r(b.area))if(a.y<0)a.y/=1+(r(b.area)-r(d))*ja;j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y;t[g]+=a.x;t[h]+=a.y}else{j(g)[4]=j(g)[4]+a.x;j(h)[5]=j(h)[5]-a.y;t[g]+=a.x;t[h]+=a.y;H()}a=S(f);t[g]=a[g];t[h]=a[h];W();ea()}function ta(a,d,f){var e=M(t),i;i=b.isHorizontal?[a.y-p(b.area),a.x-o(b.area)]:z(na([1,0,0,-1,o(b.area),r(b.area)]),[a.x,a.y]);a=i[0];i=i[1];var k=Math.pow(1.2,b.isHorizontal?f:d);d=Math.pow(1.2,b.isHorizontal? d:f);if(j(g)[0]*k>b.maxZoom[g])k=b.maxZoom[g]/j(g)[0];if(k<1||j(g)[0]!==b.maxZoom[g])ka(j(g),z([k,0,0,1,a-k*a,0],j(g)));if(j(h)[3]*d>b.maxZoom[h])d=b.maxZoom[h]/j(h)[3];if(d<1||j(h)[3]!==b.maxZoom[h])ka(j(h),z([1,0,0,d,0,i-d*i],j(h)));H();e=S(e);t[g]=e[g];t[h]=e[h];ia();W();ea()}var ga=17,fa=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,ga)}}();x.canvas.style.msTouchAction=\"none\";var V=2,pa=3,ba= 1,za=2,Y=1,Z=2,g=0,h=1,J=false;if(!window.TouchEvent&&(window.MSPointerEvent||window.PointerEvent))(function(){function a(){if(pointers.length>0&&!J)J=true;else if(pointers.length<=0&&J)J=false}function d(k){if(ma(k)){k.preventDefault();pointers.push(k);a();ca.touchStart(u,{touches:pointers.slice(0)})}}function f(k){if(J)if(ma(k)){k.preventDefault();var m;for(m=0;m<pointers.length;++m)if(pointers[m].pointerId===k.pointerId){pointers.splice(m,1);break}a();ca.touchEnd(u,{touches:pointers.slice(0),changedTouches:[]})}} function e(k){if(ma(k)){k.preventDefault();var m;for(m=0;m<pointers.length;++m)if(pointers[m].pointerId===k.pointerId){pointers[m]=k;break}a();ca.touchMoved(u,{touches:pointers.slice(0)})}}pointers=[];var i=jQuery.data(u,\"eobj\");if(i)if(window.PointerEvent){u.removeEventListener(\"pointerdown\",i.pointerDown);u.removeEventListener(\"pointerup\",i.pointerUp);u.removeEventListener(\"pointerout\",i.pointerUp);u.removeEventListener(\"pointermove\",i.pointerMove)}else{u.removeEventListener(\"MSPointerDown\",i.pointerDown); u.removeEventListener(\"MSPointerUp\",i.pointerUp);u.removeEventListener(\"MSPointerOut\",i.pointerUp);u.removeEventListener(\"MSPointerMove\",i.pointerMove)}jQuery.data(u,\"eobj\",{pointerDown:d,pointerUp:f,pointerMove:e});if(window.PointerEvent){u.addEventListener(\"pointerdown\",d);u.addEventListener(\"pointerup\",f);u.addEventListener(\"pointerout\",f);u.addEventListener(\"pointermove\",e)}else{u.addEventListener(\"MSPointerDown\",d);u.addEventListener(\"MSPointerUp\",f);u.addEventListener(\"MSPointerOut\",f);u.addEventListener(\"MSPointerMove\", e)}})();var wa=0.003,Fa=2.0E-4,ja=0.07,ha=3,sa=0.001,O=1.5,xa=0.02;jQuery.data(u,\"cobj\",this);var ca=this,y=G.WT;ca.config=b;var B=jQuery.data(u,\"oobj\"),t=null,N=true,C=null,v=[],T=false,Q=false,E=null,ua=null,va=null,l={x:0,y:0},I=null,la=null;G=y.gfxUtils;var z=G.transform_mult,na=G.transform_inverted,ka=G.transform_assign,p=G.rect_top,r=G.rect_bottom,o=G.rect_left,s=G.rect_right,aa=false;this.mouseMove=function(a,d){setTimeout(function(){if(!J){var f=y.widgetCoordinates(x.canvas,d);if(X(f,b.area))if(U()&& N){t=[f.x,f.y];fa(qa)}}},0)};this.mouseDown=function(a,d){if(!J){a=y.widgetCoordinates(x.canvas,d);if(X(a,b.area))C=a}};this.mouseUp=function(){J||(C=null)};this.mouseDrag=function(a,d){if(!J)if(C!==null){a=y.widgetCoordinates(x.canvas,d);if(X(a,b.area)){y.buttons===1&&b.pan&&P({x:a.x-C.x,y:a.y-C.y});C=a}}};this.mouseWheel=function(a,d){var f=y.widgetCoordinates(x.canvas,d);if(X(f,b.area)){a=y.normalizeWheel(d);if(!d.ctrlKey&&b.pan){f=j(g)[4];var e=j(h)[5];P({x:-a.pixelX,y:-a.pixelY});if(f!==j(g)[4]|| e!==j(h)[5])y.cancelEvent(d)}else if(d.ctrlKey&&b.zoom){y.cancelEvent(d);e=-a.spinY;if(e===0)e=-a.spinX;if(d.shiftKey&&!d.altKey)ta(f,0,e);else d.altKey&&!d.shiftKey?ta(f,e,0):ta(f,e,e)}}};this.touchStart=function(a,d){T=d.touches.length===1;Q=d.touches.length===2;if(T){aa=false;a=y.widgetCoordinates(x.canvas,d.touches[0]);if(!X(a,b.area))return;la=U()&&Ea(t,[a.x,a.y],30)?1:0;I=Date.now();C=a;y.capture(null);y.capture(x.canvas)}else if(Q&&b.zoom){aa=false;v=[y.widgetCoordinates(x.canvas,d.touches[0]), y.widgetCoordinates(x.canvas,d.touches[1])].map(function(e){return[e.x,e.y]});if(!v.every(function(e){return X(e,b.area)})){Q=null;return}y.capture(null);y.capture(x.canvas);E=Math.atan2(v[1][1]-v[0][1],v[1][0]-v[0][0]);ua=[(v[0][0]+v[1][0])/2,(v[0][1]+v[1][1])/2];a=Math.abs(Math.sin(E));var f=Math.abs(Math.cos(E));E=a<Math.sin(0.125*Math.PI)?0:f<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(E)>0?Math.PI/4:-Math.PI/4;va=Da(E,ua)}else return;d.preventDefault&&d.preventDefault()};this.touchEnd=function(a, d){var f=Array.prototype.slice.call(d.touches),e=f.length===0;T=f.length===1;Q=f.length===2;e||function(){var i;for(i=0;i<d.changedTouches.length;++i)(function(){for(var k=d.changedTouches[i].identifier,m=0;m<f.length;++m)if(f[m].identifier===k){f.splice(m,1);return}})()}();e=f.length===0;T=f.length===1;Q=f.length===2;if(e){if(la===0&&(isFinite(l.x)||isFinite(l.y))&&b.rubberBand){I=Date.now();aa=true;fa(ra)}else{ca.mouseUp(null,null);f=[];va=ua=E=null;if(I!=null){Date.now();I=null}}la=null}else if(T|| Q)ca.touchStart(a,d)};this.touchMoved=function(a,d){if(T||Q)if(T){if(C!==null){a=y.widgetCoordinates(x.canvas,d.touches[0]);var f=Date.now(),e={x:a.x-C.x,y:a.y-C.y},i=f-I;I=f;if(la===1){t[g]+=e.x;t[h]+=e.y;U()&&N&&fa(qa)}else if(b.pan){if(a.x<b.area[0]||a.x>b.area[0]+b.area[2]){l={x:0,y:0};return}if(a.y<b.area[1]||a.y>b.area[1]+b.area[3]){l={x:0,y:0};return}l.x=e.x/i;l.y=e.y/i;P(e,b.rubberBand?za:0)}d.preventDefault&&d.preventDefault();C=a}}else if(Q&&b.zoom){d.preventDefault&&d.preventDefault(); a=M(t);var k=(v[0][0]+v[1][0])/2,m=(v[0][1]+v[1][1])/2;d=[y.widgetCoordinates(x.canvas,d.touches[0]),y.widgetCoordinates(x.canvas,d.touches[1])].map(function(w){return E===0?[w.x,m]:E===Math.PI/2?[k,w.y]:z(va,[w.x,w.y])});f=Math.abs(v[1][0]-v[0][0]);e=Math.abs(d[1][0]-d[0][0]);var n=f>0?e/f:1;if(e===f||E===Math.PI/2)n=1;var A=(d[0][0]+d[1][0])/2;f=Math.abs(v[1][1]-v[0][1]);e=Math.abs(d[1][1]-d[0][1]);var q=f?e/f:1;if(e===f||E===0)q=1;var F=(d[0][1]+d[1][1])/2;b.isHorizontal&&function(){var w=n;n= q;q=w;w=A;A=F;F=w;w=k;k=m;m=w}();if(j(g)[0]*n>b.maxZoom[g])n=b.maxZoom[g]/j(g)[0];if(j(h)[3]*q>b.maxZoom[h])q=b.maxZoom[h]/j(h)[3];if(n!==1&&(n<1||j(g)[0]!==b.maxZoom[g]))ka(j(g),z([n,0,0,1,-n*k+A,0],j(g)));if(q!==1&&(q<1||j(h)[3]!==b.maxZoom[h]))ka(j(h),z([1,0,0,q,0,-q*m+F],j(h)));H();a=S(a);t[g]=a[g];t[h]=a[h];v=d;ia();W();ea()}};this.setXRange=function(a,d,f){d=b.modelArea[0]+b.modelArea[2]*d;f=b.modelArea[0]+b.modelArea[2]*f;if(d<o(b.modelArea))d=o(b.modelArea);if(f>s(b.modelArea))f=s(b.modelArea); var e=b.series[a];if(e.length!==0){a=S([d,0],true);var i=S([f,0],true),k=b.isHorizontal?h:g,m=b.isHorizontal?g:h,n=oa(a[k],e);if(n<0)n=0;else{n++;if(e[n][2]===V)n+=2}var A=oa(i[k],e),q,F,w=Infinity,K=-Infinity;for(q=n;q<=A&&q<e.length;++q)if(e[q][2]!==V&&e[q][2]!==pa){if(e[q][m]<w)w=e[q][m];if(e[q][m]>K)K=e[q][m]}if(n>0){F=n-1;if(e[F][2]===pa)F-=2;q=(a[k]-e[F][k])/(e[n][k]-e[F][k]);n=e[F][m]+q*(e[n][m]-e[F][m]);if(n<w)w=n;if(n>K)K=n}if(A<e.length-1){n=A+1;if(e[n][2]===V)n+=2;q=(i[k]-e[A][k])/(e[n][k]- e[A][k]);n=e[A][m]+q*(e[n][m]-e[A][m]);if(n<w)w=n;if(n>K)K=n}d=b.modelArea[2]/(f-d);e=b.isHorizontal?2:3;f=b.area[e]/(K-w);f=b.area[e]/(b.area[e]/f+20);if(f>b.maxZoom[m])f=b.maxZoom[m];a=b.isHorizontal?[a[h]-p(b.area),(w+K)/2-b.area[2]/f/2-o(b.area)]:[a[g]-o(b.area),-((w+K)/2+b.area[3]/f/2-r(b.area))];m=M(t);j(g)[0]=d;j(h)[3]=f;j(g)[4]=-a[g]*d;j(h)[5]=-a[h]*f;a=S(m);t[g]=a[g];t[h]=a[h];H();ia();W();ea()}};this.getSeries=function(a){return b.series[a]};this.rangeChangedCallbacks=[];this.updateConfig= function(a){for(var d in a)if(a.hasOwnProperty(d))b[d]=a[d];Ca();ia();W();ea()};this.updateConfig({})}");
	}

	private static final int TICK_LENGTH = 5;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
