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

class WChart3DImplementation implements WAbstractChartImplementation {
	private static Logger logger = LoggerFactory
			.getLogger(WChart3DImplementation.class);

	public WChart3DImplementation(WCartesian3DChart chart) {
		super();
		this.chart_ = chart;
	}

	public ChartType getChartType() {
		return this.chart_.getType();
	}

	public Orientation getOrientation() {
		return Orientation.Vertical;
	}

	public int getAxisPadding() {
		return 0;
	}

	public int numberOfCategories(Axis axis) {
		if (this.chart_.getDataSeries().size() == 0) {
			return 10;
		}
		WAbstractGridData first;
		if (axis == Axis.XAxis_3D) {
			first = ((this.chart_.getDataSeries().get(0)) instanceof WAbstractGridData ? (WAbstractGridData) (this.chart_
					.getDataSeries().get(0)) : null);
			if (first == null) {
				throw new WException(
						"WChart3DImplementation: can only count the categories in WAbstractGridData");
			} else {
				return first.getNbXPoints();
			}
		} else {
			if (axis == Axis.YAxis_3D) {
				first = ((this.chart_.getDataSeries().get(0)) instanceof WAbstractGridData ? (WAbstractGridData) (this.chart_
						.getDataSeries().get(0)) : null);
				if (first == null) {
					throw new WException(
							"WChart3DImplementation: can only count the categories in WAbstractGridData");
				} else {
					return first.getNbYPoints();
				}
			} else {
				throw new WException(
						"WChart3DImplementation: don't know this type of axis");
			}
		}
	}

	public final int numberOfCategories() {
		return numberOfCategories(Axis.XAxis);
	}

	public WString categoryLabel(int u, Axis axis) {
		if (this.chart_.getDataSeries().size() == 0) {
			return new WString(String.valueOf(u));
		}
		WAbstractGridData first = ((this.chart_.getDataSeries().get(0)) instanceof WAbstractGridData ? (WAbstractGridData) (this.chart_
				.getDataSeries().get(0)) : null);
		if (!(first != null)) {
			throw new WException(
					"WChart3DImplementation: can only count the categories in WAbstractGridData");
		}
		return first.axisLabel(u, axis);
	}

	public WAbstractChartImplementation.RenderRange computeRenderRange(
			Axis axis, int yAxis, AxisScale scale) {
		WAbstractChartImplementation.RenderRange range = new WAbstractChartImplementation.RenderRange();
		final List<WAbstractDataSeries3D> series = this.chart_.getDataSeries();
		if (series.size() == 0) {
			range.minimum = 0;
			range.maximum = 100;
			return range;
		}
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		int xDim = 0;
		int yDim = 0;
		double stackedBarsHeight = 0.0;
		WAbstractGridData griddata;
		switch (this.chart_.getType()) {
		case ScatterPlot:
			for (int i = 0; i < series.size(); i++) {
				double seriesMin = series.get(i).minimum(axis);
				double seriesMax = series.get(i).maximum(axis);
				if (seriesMin < min) {
					min = seriesMin;
				}
				if (seriesMax > max) {
					max = seriesMax;
				}
			}
			range.minimum = min;
			range.maximum = max;
			return range;
		case CategoryChart:
			for (int k = 0; k < series.size(); k++) {
				griddata = ((series.get(k)) instanceof WAbstractGridData ? (WAbstractGridData) (series
						.get(k)) : null);
				if (griddata == null
						|| griddata.getType() != Series3DType.BarSeries3D) {
					throw new WException(
							"WChart3DImplementation: not all data is categorical");
				}
			}
			xDim = (((series.get(0)) instanceof WAbstractGridData ? (WAbstractGridData) (series
					.get(0)) : null)).getNbXPoints();
			yDim = (((series.get(0)) instanceof WAbstractGridData ? (WAbstractGridData) (series
					.get(0)) : null)).getNbYPoints();
			min = 0.0;
			for (int i = 0; i < xDim; i++) {
				for (int j = 0; j < yDim; j++) {
					for (int k = 0; k < series.size(); k++) {
						if (series.get(k).isHidden()) {
							continue;
						}
						griddata = ((series.get(k)) instanceof WAbstractGridData ? (WAbstractGridData) (series
								.get(k)) : null);
						stackedBarsHeight += StringUtils.asNumber(griddata
								.data(i, j));
					}
					if (stackedBarsHeight > max) {
						max = stackedBarsHeight;
					}
					stackedBarsHeight = 0;
				}
			}
			if (max == Double.MIN_VALUE) {
				max = 100.0;
			}
			range.minimum = min;
			range.maximum = max;
			return range;
		default:
			throw new WException(
					"WChart3DImplementation: don't know this axis-type");
		}
	}

	public final WAbstractChartImplementation.RenderRange computeRenderRange(
			Axis axis, int yAxis) {
		return computeRenderRange(axis, yAxis, AxisScale.LinearScale);
	}

	public void update() {
		this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext,
				ChartUpdates.GLTextures));
	}

	private WCartesian3DChart chart_;
}
