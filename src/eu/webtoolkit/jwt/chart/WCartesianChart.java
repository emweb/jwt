/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WPaintDevice;
import eu.webtoolkit.jwt.WPaintedWidget;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPen;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WRectF;
import eu.webtoolkit.jwt.WtException;
import eu.webtoolkit.jwt.utils.StringUtils;

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
 * data series as a CategoryChart, but does not support stacking.
 * <p>
 * <div align="center"> <img src="doc-files//ChartWCartesianChart-2.png"
 * alt="A time series scatter plot with line series">
 * <p>
 * <strong>A time series scatter plot with line series</strong>
 * </p>
 * </div> The cartesian chart has support for dual Y axes. Each data series may
 * be bound to one of the two Y axes. By default, only the first Y axis is
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
 * 
 * @see WDataSeries
 * @see WAxis
 * @see WPieChart
 */
public class WCartesianChart extends WAbstractChart {
	/**
	 * Create a new cartesian chart.
	 * <p>
	 * Creates a cartesian chart of type CategoryChart.
	 */
	public WCartesianChart(WContainerWidget parent) {
		super(parent);
		this.orientation_ = Orientation.Vertical;
		this.XSeriesColumn_ = -1;
		this.type_ = ChartType.CategoryChart;
		this.series_ = new ArrayList<WDataSeries>();
		this.barMargin_ = 0;
		this.legend_ = false;
		this.init();
	}

	/**
	 * Create a new cartesian chart.
	 * <p>
	 * Calls {@link #WCartesianChart(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WCartesianChart() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a new cartesian chart.
	 * <p>
	 * Creates a cartesian chart of the indicated <i>type</i>.
	 */
	public WCartesianChart(ChartType type, WContainerWidget parent) {
		super(parent);
		this.orientation_ = Orientation.Vertical;
		this.XSeriesColumn_ = -1;
		this.type_ = type;
		this.series_ = new ArrayList<WDataSeries>();
		this.barMargin_ = 0;
		this.legend_ = false;
		this.init();
	}

	/**
	 * Create a new cartesian chart.
	 * <p>
	 * Calls {@link #WCartesianChart(ChartType type, WContainerWidget parent)
	 * this(type, (WContainerWidget)null)}
	 */
	public WCartesianChart(ChartType type) {
		this(type, (WContainerWidget) null);
	}

	/**
	 * Change the chart type.
	 * <p>
	 * The chart type determines how (x,y) data are interpreted. In a
	 * CategoryChart, the X values are categories, and these are plotted
	 * consecutively, evenly spaced, and in row order. In a ScatterPlot, the X
	 * values are interpreted numerically (as for Y values).
	 * <p>
	 * The default chart type is a CategoryChart.
	 * <p>
	 * 
	 * @see WCartesianChart#getType()
	 * @see WAxis#setScale(AxisScale scale)
	 * @see WCartesianChart#getAxis(Axis axis)
	 */
	public void setType(ChartType type) {
		if (this.type_ != type) {
			this.type_ = type;
			this.axes_[Axis.XAxis.getValue()].init(this, Axis.XAxis);
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
	 * Change the chart orientation.
	 * <p>
	 * Sets the chart orientation, which corresponds to the orientation of the Y
	 * axis: a Vertical orientation corresponds to the conventional way of a
	 * horizontal X axis and vertical Y axis. A Horizontal orientation is the
	 * other way around.
	 * <p>
	 * The default orientation is Vertical.
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
	 * Change the the model column for the X series.
	 * <p>
	 * Use this method to specify the data for the X series. For a ScatterPlot
	 * this is mandatory, while for a CategoryChart, if not specified, an
	 * increasing series of integer numbers will be used (1, 2, ...).
	 * <p>
	 * The default value is -1 (not specified).
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
	 * Add a data series.
	 * <p>
	 * A single chart may display one or more data series. Each data series
	 * displays data from a single model column in the chart. Series are plotted
	 * in the order that they have been added to the chart.
	 * <p>
	 * 
	 * @see WCartesianChart#removeSeries(int modelColumn)
	 * @see WCartesianChart#setSeries(List series)
	 */
	public void addSeries(WDataSeries series) {
		this.series_.add(series);
		this.series_.get(this.series_.size() - 1).setChart(this);
		this.update();
	}

	/**
	 * Remove a data series.
	 * <p>
	 * This removes the first data series which plots the given
	 * <i>modelColumn</i>.
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
	 * Change all data series.
	 * <p>
	 * Replaces the current list of series with the new list.
	 * <p>
	 * 
	 * @see WCartesianChart#getSeries(int modelColumn)
	 * @see WCartesianChart#addSeries(WDataSeries series)
	 * @see WCartesianChart#removeSeries(int modelColumn)
	 */
	public void setSeries(List<WDataSeries> series) {
		this.series_ = series;
		for (int i = 0; i < this.series_.size(); ++i) {
			this.series_.get(i).setChart(this);
		}
		this.update();
	}

	int getSeriesIndexOf(int modelColumn) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() == modelColumn) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns a data series corresponding to a data column.
	 * <p>
	 * Returns a reference to the first data series that plots data from
	 * <i>modelColumn</i>.
	 */
	public WDataSeries getSeries(int modelColumn) {
		int index = this.getSeriesIndexOf(modelColumn);
		if (index != -1) {
			return this.series_.get(index);
		}
		throw new PlotException("Column " + String.valueOf(modelColumn)
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
	 * Access a chart axis.
	 * <p>
	 * Returns a reference to the specified <i>axis</i>.
	 */
	public WAxis getAxis(Axis axis) {
		return this.axes_[axis.getValue()];
	}

	/**
	 * Change the margin between bars of different series.
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
	 * Enable the legend.
	 * <p>
	 * If <i>enabled</i> is true, then a default legend is added to the right of
	 * the chart. You should provide space for the legend using the
	 * setChartPadding() method. Only series for which the legend is enabled or
	 * included in this legend (see {@link WDataSeries#isLegendEnabled()
	 * WDataSeries#isLegendEnabled()}).
	 * <p>
	 * To have more control over the legend, you could reimplement the
	 * {@link WCartesianChart#renderLegendItem(WPainter painter, WPointF pos, WDataSeries series)
	 * renderLegendItem()} method to customize how one item in the legend is
	 * rendered, or, alternatively you could reimplement the paint(WPainter&amp;
	 * painter, const WRectF&amp;) method in which you use the
	 * {@link WCartesianChart#renderLegendItem(WPainter painter, WPointF pos, WDataSeries series)
	 * renderLegendItem()} method repeatedly to render a legend at an arbitrary
	 * position.
	 * <p>
	 * The default value is false.
	 * <p>
	 * 
	 * @see WDataSeries#setLegendEnabled(boolean enabled)
	 */
	public void setLegendEnabled(boolean enabled) {
		if (this.legend_ != enabled) {
			this.legend_ = enabled;
			this.update();
		}
	}

	/**
	 * Returns whether the legend is enabled.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendEnabled(boolean enabled)
	 */
	public boolean isLegendEnabled() {
		return this.legend_;
	}

	public void paint(WPainter painter, WRectF rectangle) {
		if (!painter.isActive()) {
			throw new WtException(
					"WCartesianChart::paint(): painter is not active.");
		}
		WRectF rect = rectangle;
		if (rect.isEmpty()) {
			rect.assign(painter.getWindow());
		}
		WChart2DRenderer renderer = this.createRenderer(painter, rect);
		renderer.render();
		;
	}

	/**
	 * Draws the marker for a given data series.
	 * <p>
	 * Draws the marker for the indicated <i>series</i> in the <i>result</i>.
	 * This method is called while painting the chart, and you may want to
	 * reimplement this method if you wish to provide a custom marker for a
	 * particular data series.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendEnabled(boolean enabled)
	 */
	public void drawMarker(WDataSeries series, WPainterPath result) {
		switch (series.getMarker()) {
		case CircleMarker:
			result.addEllipse(-3, -3, 6, 6);
			break;
		case SquareMarker:
			result.addRect(new WRectF(-3, -3, 6, 6));
			break;
		case CrossMarker:
			result.moveTo(-4, 0);
			result.lineTo(4, 0);
			result.moveTo(0, -4);
			result.lineTo(0, 4);
			break;
		case XCrossMarker:
			result.moveTo(-3, -3);
			result.lineTo(3, 3);
			result.moveTo(-3, 3);
			result.lineTo(3, -3);
			break;
		case TriangleMarker:
			result.moveTo(0, -3);
			result.lineTo(3, 2);
			result.lineTo(-3, 2);
			result.closeSubPath();
			break;
		default:
			;
		}
	}

	/**
	 * Renders the legend item for a given data series.
	 * <p>
	 * Renders the legend item for the indicated <i>series</i> in the
	 * <i>paintert</i> at position <i>pos</i>. The default implementation draws
	 * the marker, and the series description to the right. The series
	 * description is taken from the model&apos;s header data for that
	 * series&apos; data column.
	 * <p>
	 * This method is called while painting the chart, and you may want to
	 * reimplement this method if you wish to provide a custom marker for a
	 * particular data series.
	 * <p>
	 * 
	 * @see WCartesianChart#setLegendEnabled(boolean enabled)
	 */
	public void renderLegendItem(WPainter painter, WPointF pos,
			WDataSeries series) {
		WPen fontPen = painter.getPen();
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
		painter.setPen(fontPen);
		painter.drawText(pos.getX() + 17, pos.getY() - 10, 100, 20, EnumSet.of(
				AlignmentFlag.AlignLeft, AlignmentFlag.AlignMiddle),
				StringUtils.asString(this.getModel().getHeaderData(
						series.getModelColumn())));
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		painter.setRenderHint(WPainter.RenderHint.Antialiasing);
		this.paint(painter);
	}

	/**
	 * Create a renderer which renders the chart.
	 * <p>
	 * The rendering of the chart is delegated to a {@link WChart2DRenderer}
	 * class, which will render the chart within the <i>rectangle</i> of the
	 * <i>painter</i>.
	 * <p>
	 * You may want to reimplement this method if you wish to override one or
	 * more aspects of the rendering, by returning an new instance of a
	 * specialized {@link WChart2DRenderer} class.
	 * <p>
	 * After rendering, the renderer is deleted.
	 * <p>
	 * 
	 * @see WChart2DRenderer#render()
	 */
	protected WChart2DRenderer createRenderer(WPainter painter, WRectF rectangle) {
		return new WChart2DRenderer(this, painter, rectangle);
	}

	private Orientation orientation_;
	private int XSeriesColumn_;
	private ChartType type_;
	private List<WDataSeries> series_;
	private WAxis[] axes_ = new WAxis[3];
	private double barMargin_;
	private boolean legend_;

	private void init() {
		this.setPalette(new WStandardPalette(WStandardPalette.Flavour.Muted));
		this.setPreferredMethod(WPaintedWidget.Method.InlineSvgVml);
		for (int i = 0; i < 3; ++i) {
			this.axes_[i] = new WAxis();
		}
		this.axes_[Axis.XAxis.getValue()].init(this, Axis.XAxis);
		this.axes_[Axis.YAxis.getValue()].init(this, Axis.YAxis);
		this.axes_[Axis.Y2Axis.getValue()].init(this, Axis.Y2Axis);
		this.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
		this.setPlotAreaPadding(30, EnumSet.of(Side.Top, Side.Bottom));
	}

	protected void modelColumnsInserted(WModelIndex parent, int start, int end) {
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() >= start) {
				this.series_.get(i).modelColumn_ += end - start + 1;
			}
		}
	}

	protected void modelColumnsRemoved(WModelIndex parent, int start, int end) {
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

	protected void modelRowsInserted(WModelIndex parent, int start, int end) {
		this.update();
	}

	protected void modelRowsRemoved(WModelIndex parent, int start, int end) {
		this.update();
	}

	protected void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
		if (this.XSeriesColumn_ <= topLeft.getColumn()
				&& this.XSeriesColumn_ >= bottomRight.getColumn()) {
			this.update();
			return;
		}
		for (int i = 0; i < this.series_.size(); ++i) {
			if (this.series_.get(i).getModelColumn() >= topLeft.getColumn()
					&& this.series_.get(i).getModelColumn() <= bottomRight
							.getColumn()) {
				this.update();
				break;
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
}
