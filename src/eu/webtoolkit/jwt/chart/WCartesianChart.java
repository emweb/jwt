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
		this.chartArea_ = null;
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
		this.chartArea_ = null;
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
	 */
	public void setLegendColumns(int columns, final WLength columnWidth) {
		this.legend_.setLegendColumns(columns, columnWidth);
		this.update();
	}

	public void paint(final WPainter painter, final WRectF rectangle) {
		if (!painter.isActive()) {
			throw new WException(
					"WCartesianChart::paint(): painter is not active.");
		}
		WRectF rect = rectangle;
		if (rect.isEmpty()) {
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
		switch (series.getType()) {
		case BarSeries: {
			WPainterPath path = new WPainterPath();
			path.moveTo(-6, 8);
			path.lineTo(-6, -8);
			path.lineTo(6, -8);
			path.lineTo(6, 8);
			painter.setPen(series.getPen());
			painter.setBrush(series.getBrush());
			painter.translate(pos.getX() + 7.5, pos.getY());
			painter.drawPath(path);
			painter.translate(-(pos.getX() + 7.5), -pos.getY());
			break;
		}
		case LineSeries:
		case CurveSeries: {
			painter.setPen(series.getPen());
			double offset = series.getPen().getWidth().equals(new WLength(0)) ? 0.5
					: 0;
			painter.drawLine(pos.getX(), pos.getY() + offset, pos.getX() + 16,
					pos.getY() + offset);
		}
		case PointSeries: {
			WPainterPath path = new WPainterPath();
			this.drawMarker(series, path);
			if (!path.isEmpty()) {
				painter.translate(pos.getX() + 8, pos.getY());
				painter.setPen(series.getMarkerPen());
				painter.setBrush(series.getMarkerBrush());
				painter.drawPath(path);
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
		painter.setPen(fontPen);
		painter.drawText(pos.getX() + 17, pos.getY() - 10, 100, 20, EnumSet.of(
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
	 * {@link WCartesianChart#initLayout(WRectF rectangle) initLayout()} first.
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
	 * {@link WCartesianChart#initLayout(WRectF rectangle) initLayout()} first.
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
	public boolean initLayout(final WRectF rectangle) {
		WRectF rect = rectangle;
		if (rect.isEmpty()) {
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
		this.calcChartArea();
		return this.chartArea_.getWidth() > 5
				&& this.chartArea_.getHeight() > 5 && this.isPrepareAxes();
	}

	/**
	 * Initializes the chart layout.
	 * <p>
	 * Returns {@link #initLayout(WRectF rectangle) initLayout(null)}
	 */
	public final boolean initLayout() {
		return initLayout(null);
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
	 * 
	 * @see WCartesianChart#getAxisPadding()
	 */
	public void setAxisPadding(int padding) {
		this.axisPadding_ = padding;
	}

	/**
	 * Returns the padding between the chart area and the axes.
	 * <p>
	 * 
	 * @see WCartesianChart#setAxisPadding(int padding)
	 */
	public int getAxisPadding() {
		return this.axisPadding_;
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
								WPainterPath clipPath = new WPainterPath();
								clipPath.addRect(this.hv(csa));
								painter.setClipPath(clipPath);
								painter.setClipping(true);
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

	private WChart2DImplementation interface_;
	private Orientation orientation_;
	private int XSeriesColumn_;
	private ChartType type_;
	private List<WDataSeries> series_;
	private WAxis[] axes_ = new WAxis[3];
	private double barMargin_;
	private WLegend legend_;
	private int axisPadding_;
	private int width_;
	private int height_;
	WRectF chartArea_;
	private AxisValue[] location_ = new AxisValue[3];

	private void init() {
		this.setPalette(new WStandardPalette(WStandardPalette.Flavour.Muted));
		for (int i = 0; i < 3; ++i) {
			this.axes_[i] = new WAxis();
		}
		this.axes_[Axis.XAxis.getValue()].init(this.interface_, Axis.XAxis);
		this.axes_[Axis.YAxis.getValue()].init(this.interface_, Axis.YAxis);
		this.axes_[Axis.Y2Axis.getValue()].init(this.interface_, Axis.Y2Axis);
		this.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
		this.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
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
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected void paintEvent(WPaintDevice paintDevice) {
		while (!this.getAreas().isEmpty()) {
			if (this.getAreas().get(0) != null)
				this.getAreas().get(0).remove();
		}
		WPainter painter = new WPainter(paintDevice);
		painter.setRenderHint(WPainter.RenderHint.Antialiasing);
		this.paint(painter);
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected void render(final WPainter painter, final WRectF rectangle) {
		painter.save();
		painter.translate(rectangle.getTopLeft());
		if (this.initLayout(rectangle)) {
			this.renderBackground(painter);
			this.renderGrid(painter, this.getAxis(Axis.XAxis));
			this.renderGrid(painter, this.getAxis(Axis.Y1Axis));
			this.renderGrid(painter, this.getAxis(Axis.Y2Axis));
			this.renderSeries(painter);
			this.renderAxes(painter, EnumSet.of(AxisProperty.Line,
					AxisProperty.Labels));
			this.renderLegend(painter);
		}
		painter.restore();
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
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
	 * Paints the widget.
	 * <p>
	 * Returns
	 * {@link #map(double xValue, double yValue, Axis yAxis, int currentXSegment, int currentYSegment)
	 * map(xValue, yValue, Axis.OrdinateAxis, 0, 0)}
	 */
	protected final WPointF map(double xValue, double yValue) {
		return map(xValue, yValue, Axis.OrdinateAxis, 0, 0);
	}

	/**
	 * Paints the widget.
	 * <p>
	 * Returns
	 * {@link #map(double xValue, double yValue, Axis yAxis, int currentXSegment, int currentYSegment)
	 * map(xValue, yValue, yAxis, 0, 0)}
	 */
	protected final WPointF map(double xValue, double yValue, Axis yAxis) {
		return map(xValue, yValue, yAxis, 0, 0);
	}

	/**
	 * Paints the widget.
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
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected void renderLabel(final WPainter painter, final CharSequence text,
			final WPointF p, final WColor color, EnumSet<AlignmentFlag> flags,
			double angle, int margin) {
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
		double left = pos.getX();
		double top = pos.getY();
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
		WPen pen = new WPen(color);
		WPen oldPen = painter.getPen();
		painter.setPen(pen);
		if (angle == 0) {
			painter.drawText(new WRectF(left, top, width, height), EnumSet.of(
					rHorizontalAlign, rVerticalAlign), text);
		} else {
			painter.save();
			painter.translate(pos);
			painter.rotate(-angle);
			painter.drawText(new WRectF(left - pos.getX(), top - pos.getY(),
					width, height), EnumSet
					.of(rHorizontalAlign, rVerticalAlign), text);
			painter.restore();
		}
		painter.setPen(oldPen);
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected WPointF hv(double x, double y) {
		return this.hv(x, y, this.height_);
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected WPointF hv(final WPointF p) {
		return this.hv(p.getX(), p.getY());
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
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
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
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
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
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
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
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
					if (other.segments_.get(0).renderMinimum == 0) {
						location = AxisValue.ZeroValue;
					}
				} else {
					if (other.segments_.get(0).renderMaximum == 0) {
						location = AxisValue.MaximumValue;
					}
				}
			}
			this.location_[axis.getId().getValue()] = location;
		}
		if (y2Axis.isVisible()) {
			if (!(this.location_[Axis.Y1Axis.getValue()] == AxisValue.ZeroValue && xAxis.segments_
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
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected void renderBackground(final WPainter painter) {
		if (this.getBackground().getStyle() != BrushStyle.NoBrush) {
			painter.fillRect(this.hv(this.chartArea_), this.getBackground());
		}
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected void renderAxes(final WPainter painter,
			EnumSet<AxisProperty> properties) {
		this.renderAxis(painter, this.getAxis(Axis.XAxis), properties);
		this.renderAxis(painter, this.getAxis(Axis.Y1Axis), properties);
		this.renderAxis(painter, this.getAxis(Axis.Y2Axis), properties);
	}

	/**
	 * Paints the widget.
	 * <p>
	 * Calls {@link #renderAxes(WPainter painter, EnumSet properties)
	 * renderAxes(painter, EnumSet.of(propertie, properties))}
	 */
	protected final void renderAxes(final WPainter painter,
			AxisProperty propertie, AxisProperty... properties) {
		renderAxes(painter, EnumSet.of(propertie, properties));
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected void renderSeries(final WPainter painter) {
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
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected void renderLegend(final WPainter painter) {
		boolean vertical = this.getOrientation() == Orientation.Vertical;
		int w = vertical ? this.width_ : this.height_;
		int h = vertical ? this.height_ : this.width_;
		final int margin = 10;
		if (this.isLegendEnabled()) {
			int numSeriesWithLegend = 0;
			for (int i = 0; i < this.getSeries().size(); ++i) {
				if (this.getSeries().get(i).isLegendEnabled()) {
					++numSeriesWithLegend;
				}
			}
			WFont f = painter.getFont();
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
				if (this.getLegendSide() == Side.Right && vertical
						&& this.getAxis(Axis.Y2Axis).isVisible()) {
					x += 40;
				}
				if (this.getLegendSide() == Side.Bottom
						&& (vertical && this.getAxis(Axis.XAxis).isVisible() || !vertical
								&& this.getAxis(Axis.Y2Axis).isVisible())) {
					y += 16;
				}
				if (this.getLegendSide() == Side.Left
						&& (vertical && this.getAxis(Axis.Y1Axis).isVisible() || !vertical
								&& this.getAxis(Axis.XAxis).isVisible())) {
					x -= 40;
				}
			}
			painter.setPen(this.getLegendBorder());
			painter.setBrush(this.getLegendBackground());
			painter.drawRect(x - margin / 2, y - margin / 2, legendWidth
					+ margin, legendHeight + margin);
			painter.setPen(new WPen());
			painter.save();
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
			int x = w / 2;
			painter.save();
			painter.setFont(this.getTitleFont());
			painter.drawText(x - 500, 5, 1000, 50, EnumSet.of(
					AlignmentFlag.AlignCenter, AlignmentFlag.AlignTop), this
					.getTitle());
			painter.restore();
		}
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	protected void renderAxis(final WPainter painter, final WAxis axis,
			EnumSet<AxisProperty> properties) {
		if (!axis.isVisible()) {
			return;
		}
		boolean vertical = axis.getId() != Axis.XAxis;
		WPointF axisStart = new WPointF();
		WPointF axisEnd = new WPointF();
		double tickStart = 0.0;
		double tickEnd = 0.0;
		double labelPos = 0.0;
		AlignmentFlag labelHFlag = AlignmentFlag.AlignCenter;
		AlignmentFlag labelVFlag = AlignmentFlag.AlignMiddle;
		if (vertical) {
			labelVFlag = AlignmentFlag.AlignMiddle;
			axisStart.setY(this.chartArea_.getBottom() + 0.5);
			axisEnd.setY(this.chartArea_.getTop() + 0.5);
		} else {
			labelHFlag = AlignmentFlag.AlignCenter;
			axisStart.setX(this.chartArea_.getLeft() + 0.5);
			axisEnd.setX(this.chartArea_.getRight() + 0.5);
		}
		switch (this.location_[axis.getId().getValue()]) {
		case MinimumValue:
			if (vertical) {
				tickStart = -TICK_LENGTH;
				tickEnd = 0;
				labelPos = -TICK_LENGTH;
				labelHFlag = AlignmentFlag.AlignRight;
				double x = this.chartArea_.getLeft() - axis.getMargin() + 0.5;
				axisStart.setX(x);
				axisEnd.setX(x);
			} else {
				tickStart = 0;
				tickEnd = TICK_LENGTH;
				labelPos = TICK_LENGTH;
				labelVFlag = AlignmentFlag.AlignTop;
				double y = this.chartArea_.getBottom() + axis.getMargin() + 0.5;
				axisStart.setY(y);
				axisEnd.setY(y);
			}
			break;
		case MaximumValue:
			if (vertical) {
				tickStart = 0;
				tickEnd = TICK_LENGTH;
				labelPos = TICK_LENGTH;
				labelHFlag = AlignmentFlag.AlignLeft;
				double x = this.chartArea_.getRight() + axis.getMargin() + 0.5;
				axisStart.setX(x);
				axisEnd.setX(x);
			} else {
				tickStart = -TICK_LENGTH;
				tickEnd = 0;
				labelPos = -TICK_LENGTH;
				labelVFlag = AlignmentFlag.AlignBottom;
				double y = this.chartArea_.getTop() - axis.getMargin() + 0.5;
				axisStart.setY(y);
				axisEnd.setY(y);
			}
			break;
		case ZeroValue:
			tickStart = -TICK_LENGTH;
			tickEnd = TICK_LENGTH;
			if (vertical) {
				double x = Math.floor(this.map(0, 0, Axis.YAxis).getX()) + 0.5;
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
				double y = Math.floor(this.map(0, 0, Axis.YAxis).getY()) + 0.5;
				axisStart.setY(y);
				axisEnd.setY(y);
				labelVFlag = AlignmentFlag.AlignTop;
				if (this.getType() == ChartType.CategoryChart) {
					labelPos = this.chartArea_.getBottom() - axisStart.getY()
							+ TICK_LENGTH;
				} else {
					labelPos = TICK_LENGTH;
				}
			}
			break;
		}
		if (!EnumUtils.mask(properties, AxisProperty.Labels).isEmpty()
				&& !(axis.getTitle().length() == 0)) {
			WFont oldFont2 = painter.getFont();
			WFont titleFont = axis.getTitleFont();
			painter.setFont(titleFont);
			boolean chartVertical = this.getOrientation() == Orientation.Vertical;
			if (vertical) {
				double u = axisStart.getX();
				if (chartVertical) {
					this.renderLabel(painter, axis.getTitle(), new WPointF(u
							+ (labelHFlag == AlignmentFlag.AlignRight ? 15
									: -15), this.chartArea_.getTop() - 8),
							WColor.black, EnumSet.of(labelHFlag,
									AlignmentFlag.AlignBottom), 0, 0);
				} else {
					this
							.renderLabel(
									painter,
									axis.getTitle(),
									new WPointF(
											u
													+ (labelHFlag == AlignmentFlag.AlignRight ? -40
															: +40),
											this.chartArea_.getCenter().getY()),
									WColor.black,
									EnumSet
											.of(
													labelHFlag == AlignmentFlag.AlignRight ? AlignmentFlag.AlignLeft
															: AlignmentFlag.AlignRight,
													AlignmentFlag.AlignMiddle),
									0, 0);
				}
			} else {
				double u = axisStart.getY();
				if (chartVertical) {
					this.renderLabel(painter, axis.getTitle(), new WPointF(
							this.chartArea_.getCenter().getX(), u + 22),
							WColor.black, EnumSet.of(AlignmentFlag.AlignTop,
									AlignmentFlag.AlignCenter), 0, 0);
				} else {
					this.renderLabel(painter, axis.getTitle(), new WPointF(
							this.chartArea_.getRight(), u), WColor.black,
							EnumSet.of(AlignmentFlag.AlignTop,
									AlignmentFlag.AlignLeft), 0, 8);
				}
			}
			painter.setFont(oldFont2);
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
		axis.render(painter, properties, axisStart, axisEnd, tickStart,
				tickEnd, labelPos, EnumSet.of(labelHFlag, labelVFlag));
	}

	/**
	 * Paints the widget.
	 * <p>
	 * Calls {@link #renderAxis(WPainter painter, WAxis axis, EnumSet properties)
	 * renderAxis(painter, axis, EnumSet.of(propertie, properties))}
	 */
	protected final void renderAxis(final WPainter painter, final WAxis axis,
			AxisProperty propertie, AxisProperty... properties) {
		renderAxis(painter, axis, EnumSet.of(propertie, properties));
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
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
		boolean otherVertical = !vertical;
		if (otherVertical) {
			ou0 = this.chartArea_.getBottom() - ou0 + 0.5;
			oun = this.chartArea_.getBottom() - oun + 0.5;
		} else {
			ou0 = this.chartArea_.getLeft() + ou0 + 0.5;
			oun = this.chartArea_.getLeft() + oun + 0.5;
		}
		WPainterPath gridPath = new WPainterPath();
		List<Double> gridPos = ax.getGridLinePositions();
		for (int i = 0; i < gridPos.size(); ++i) {
			double u = gridPos.get(i);
			if (vertical) {
				u = Math.floor(this.chartArea_.getBottom() - u) + 0.5;
				gridPath.moveTo(this.hv(ou0, u));
				gridPath.lineTo(this.hv(oun, u));
			} else {
				u = Math.floor(this.chartArea_.getLeft() + u) + 0.5;
				gridPath.moveTo(this.hv(u, ou0));
				gridPath.lineTo(this.hv(u, oun));
			}
		}
		painter.strokePath(gridPath, ax.getGridLinesPen());
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
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

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	int getSeriesIndexOf(int modelColumn) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() == modelColumn) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	WPointF hv(double x, double y, double width) {
		if (this.orientation_ == Orientation.Vertical) {
			return new WPointF(x, y);
		} else {
			return new WPointF(width - y, x);
		}
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * paintEvent()} to paint on the paint device.
	 */
	private WPointF inverseHv(double x, double y, double width) {
		if (this.orientation_ == Orientation.Vertical) {
			return new WPointF(x, y);
		} else {
			return new WPointF(y, width - x);
		}
	}

	/**
	 * Paints the widget.
	 * <p>
	 * This calls {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
	 * WCartesianChart#paintEvent()} to paint on the paint device.
	 */
	private static class IconWidget extends WPaintedWidget {
		private static Logger logger = LoggerFactory
				.getLogger(IconWidget.class);

		/**
		 * Paints the widget.
		 * <p>
		 * This calls
		 * {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
		 * WCartesianChart#paintEvent()} to paint on the paint device.
		 */
		public IconWidget(WCartesianChart chart, int index,
				WContainerWidget parent) {
			super(parent);
			this.chart_ = chart;
			this.index_ = index;
			this.setInline(true);
			this.resize(new WLength(20), new WLength(20));
		}

		/**
		 * Paints the widget.
		 * <p>
		 * Calls
		 * {@link #IconWidget(WCartesianChart chart, int index, WContainerWidget parent)
		 * this(chart, index, (WContainerWidget)null)}
		 */
		public IconWidget(WCartesianChart chart, int index) {
			this(chart, index, (WContainerWidget) null);
		}

		/**
		 * Paints the widget.
		 * <p>
		 * This calls
		 * {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
		 * WCartesianChart#paintEvent()} to paint on the paint device.
		 */
		protected void paintEvent(WPaintDevice paintDevice) {
			WPainter painter = new WPainter(paintDevice);
			this.chart_.renderLegendIcon(painter, new WPointF(2.5, 10.0),
					this.chart_.getSeries(this.index_));
		}

		/**
		 * Paints the widget.
		 * <p>
		 * This calls
		 * {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
		 * WCartesianChart#paintEvent()} to paint on the paint device.
		 */
		private WCartesianChart chart_;
		/**
		 * Paints the widget.
		 * <p>
		 * This calls
		 * {@link WCartesianChart#paintEvent(WPaintDevice paintDevice)
		 * WCartesianChart#paintEvent()} to paint on the paint device.
		 */
		private int index_;
	}

	private static final int TICK_LENGTH = 5;
}
