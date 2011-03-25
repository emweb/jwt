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
 * Abstract base class for iterating over series data in
 * {@link WChart2DRenderer}.
 * <p>
 * 
 * This class is specialized for rendering series data.
 * <p>
 * 
 * @see WChart2DRenderer#iterateSeries(SeriesIterator iterator, boolean
 *      reverseStacked)
 */
public class SeriesIterator {
	/**
	 * Start handling a new segment.
	 * <p>
	 * Because of a &apos;break&apos; specified in an axis, axes may be divided
	 * in one or two segments (in fact only the API limits this now to two). The
	 * iterator will iterate all segments seperately, but each time with a
	 * different clipping region specified in the painter, corresponding to that
	 * segment.
	 * <p>
	 * The <i>currentSegmentArea</i> specifies the clipping area.
	 */
	public void startSegment(int currentXSegment, int currentYSegment,
			WRectF currentSegmentArea) {
		this.currentXSegment_ = currentXSegment;
		this.currentYSegment_ = currentYSegment;
	}

	/**
	 * End handling a particular segment.
	 * <p>
	 * 
	 * @see SeriesIterator#startSegment(int currentXSegment, int
	 *      currentYSegment, WRectF currentSegmentArea)
	 */
	public void endSegment() {
	}

	/**
	 * Start iterating a particular series.
	 * <p>
	 * Returns whether the series values should be iterated. The
	 * <i>groupWidth</i> is the width (in pixels) of a single bar group. The
	 * chart contains <i>numBarGroups</i>, and the current series is in the
	 * <i>currentBarGroup</i>&apos;th group.
	 */
	public boolean startSeries(WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		return true;
	}

	/**
	 * End iterating a particular series.
	 */
	public void endSeries() {
	}

	/**
	 * Process a value.
	 * <p>
	 * Processes a value with model coordinates (<i>x</i>, <i>y</i>). The y
	 * value may differ from the model&apos;s y value, because of stacked
	 * series. The y value here corresponds to the location on the chart, after
	 * stacking.
	 * <p>
	 * The <i>stackY</i> argument is the y value from the previous series (also
	 * after stacking). It will be 0, unless this series is stacked.
	 */
	public void newValue(WDataSeries series, double x, double y, double stackY,
			WModelIndex xIndex, WModelIndex yIndex) {
	}

	/**
	 * Returns the current X segment.
	 */
	public int getCurrentXSegment() {
		return this.currentXSegment_;
	}

	/**
	 * Returns the current Y segment.
	 */
	public int getCurrentYSegment() {
		return this.currentYSegment_;
	}

	public static void setPenColor(WPen pen, WModelIndex xIndex,
			WModelIndex yIndex, int colorRole) {
		Object color = new Object();
		if ((yIndex != null)) {
			color = yIndex.getData(colorRole);
		}
		if ((color == null) && (xIndex != null)) {
			color = xIndex.getData(colorRole);
		}
		if (!(color == null)) {
			pen.setColor((WColor) color);
		}
	}

	/**
	 * Helper class for rendering a cartesian chart.
	 * <p>
	 * 
	 * This class is used by {@link eu.webtoolkit.jwt.chart.WCartesianChart}
	 * during rendering, and normally, you will not need to use this class
	 * directly. You may want to specialize this class if you want to override
	 * particular aspects of how the chart is renderered. In that case, you will
	 * want to instantiate the specialized class in
	 * {@link WCartesianChart#createRenderer(WPainter painter, WRectF rectangle)
	 * WCartesianChart#createRenderer()}.
	 * <p>
	 * To simplify the simulatenous handling of Horizontal and Vertical charts,
	 * the renderer makes abstraction of the orientation of the chart:
	 * regardless of the chart orientation, the
	 * {@link WChart2DRenderer#getWidth() WChart2DRenderer#getWidth()}
	 * corresponds to the length along the X axis, and
	 * {@link WChart2DRenderer#getHeight() WChart2DRenderer#getHeight()}
	 * corresponds to the length along the Y axis. Similarly,
	 * {@link WChart2DRenderer#calcChartArea() WChart2DRenderer#calcChartArea()}
	 * and {@link WChart2DRenderer#getChartArea()
	 * WChart2DRenderer#getChartArea()} return a rectangle where the bottom side
	 * corresponds to the lowest displayed Y values, and the left side
	 * corresponds to the lowest displayed X values. To map these &quot;chart
	 * coordinates&quot; to painter coordinates, use one of the
	 * {@link WChart2DRenderer#hv(double x, double y) WChart2DRenderer#hv()}
	 * methods.
	 * <p>
	 * <i>Note, this class is part of the internal charting API, and may be
	 * subject of changes and refactorings.</i>
	 */
	public static void setBrushColor(WBrush brush, WModelIndex xIndex,
			WModelIndex yIndex, int colorRole) {
		Object color = new Object();
		if ((yIndex != null)) {
			color = yIndex.getData(colorRole);
		}
		if ((color == null) && (xIndex != null)) {
			color = xIndex.getData(colorRole);
		}
		if (!(color == null)) {
			brush.setColor((WColor) color);
		}
	}

	/**
	 * Helper class for rendering a cartesian chart.
	 * <p>
	 * 
	 * This class is used by {@link eu.webtoolkit.jwt.chart.WCartesianChart}
	 * during rendering, and normally, you will not need to use this class
	 * directly. You may want to specialize this class if you want to override
	 * particular aspects of how the chart is renderered. In that case, you will
	 * want to instantiate the specialized class in
	 * {@link WCartesianChart#createRenderer(WPainter painter, WRectF rectangle)
	 * WCartesianChart#createRenderer()}.
	 * <p>
	 * To simplify the simulatenous handling of Horizontal and Vertical charts,
	 * the renderer makes abstraction of the orientation of the chart:
	 * regardless of the chart orientation, the
	 * {@link WChart2DRenderer#getWidth() WChart2DRenderer#getWidth()}
	 * corresponds to the length along the X axis, and
	 * {@link WChart2DRenderer#getHeight() WChart2DRenderer#getHeight()}
	 * corresponds to the length along the Y axis. Similarly,
	 * {@link WChart2DRenderer#calcChartArea() WChart2DRenderer#calcChartArea()}
	 * and {@link WChart2DRenderer#getChartArea()
	 * WChart2DRenderer#getChartArea()} return a rectangle where the bottom side
	 * corresponds to the lowest displayed Y values, and the left side
	 * corresponds to the lowest displayed X values. To map these &quot;chart
	 * coordinates&quot; to painter coordinates, use one of the
	 * {@link WChart2DRenderer#hv(double x, double y) WChart2DRenderer#hv()}
	 * methods.
	 * <p>
	 * <i>Note, this class is part of the internal charting API, and may be
	 * subject of changes and refactorings.</i>
	 */
	private int currentXSegment_;
	/**
	 * Helper class for rendering a cartesian chart.
	 * <p>
	 * 
	 * This class is used by {@link eu.webtoolkit.jwt.chart.WCartesianChart}
	 * during rendering, and normally, you will not need to use this class
	 * directly. You may want to specialize this class if you want to override
	 * particular aspects of how the chart is renderered. In that case, you will
	 * want to instantiate the specialized class in
	 * {@link WCartesianChart#createRenderer(WPainter painter, WRectF rectangle)
	 * WCartesianChart#createRenderer()}.
	 * <p>
	 * To simplify the simulatenous handling of Horizontal and Vertical charts,
	 * the renderer makes abstraction of the orientation of the chart:
	 * regardless of the chart orientation, the
	 * {@link WChart2DRenderer#getWidth() WChart2DRenderer#getWidth()}
	 * corresponds to the length along the X axis, and
	 * {@link WChart2DRenderer#getHeight() WChart2DRenderer#getHeight()}
	 * corresponds to the length along the Y axis. Similarly,
	 * {@link WChart2DRenderer#calcChartArea() WChart2DRenderer#calcChartArea()}
	 * and {@link WChart2DRenderer#getChartArea()
	 * WChart2DRenderer#getChartArea()} return a rectangle where the bottom side
	 * corresponds to the lowest displayed Y values, and the left side
	 * corresponds to the lowest displayed X values. To map these &quot;chart
	 * coordinates&quot; to painter coordinates, use one of the
	 * {@link WChart2DRenderer#hv(double x, double y) WChart2DRenderer#hv()}
	 * methods.
	 * <p>
	 * <i>Note, this class is part of the internal charting API, and may be
	 * subject of changes and refactorings.</i>
	 */
	private int currentYSegment_;
	static final int TICK_LENGTH = 5;
}
