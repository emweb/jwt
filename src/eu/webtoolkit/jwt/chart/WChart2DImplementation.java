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

class WChart2DImplementation implements WAbstractChartImplementation {
	private static Logger logger = LoggerFactory
			.getLogger(WChart2DImplementation.class);

	public WChart2DImplementation(WCartesianChart chart) {
		super();
		this.chart_ = chart;
	}

	public ChartType getChartType() {
		return this.chart_.getType();
	}

	public Orientation getOrientation() {
		return this.chart_.getOrientation();
	}

	public int getAxisPadding() {
		return this.chart_.getAxisPadding();
	}

	public int numberOfCategories(Axis axis) {
		if (this.chart_.getModel() != null) {
			return this.chart_.getModel().getRowCount();
		} else {
			return 0;
		}
	}

	public final int numberOfCategories() {
		return numberOfCategories(Axis.XAxis);
	}

	public WString categoryLabel(int u, Axis axis) {
		if (this.chart_.XSeriesColumn() != -1) {
			if (u < this.chart_.getModel().getRowCount()) {
				return this.chart_.getModel().getDisplayData(u,
						this.chart_.XSeriesColumn());
			} else {
				return new WString();
			}
		} else {
			return new WString();
		}
	}

	public final WString categoryLabel(int u) {
		return categoryLabel(u, Axis.XAxis);
	}

	public WAbstractChartImplementation.RenderRange computeRenderRange(
			Axis axis, int yAxis, AxisScale scale) {
		ExtremesIterator iterator = new ExtremesIterator(axis, yAxis, scale);
		this.chart_.iterateSeries(iterator, (WPainter) null);
		WAbstractChartImplementation.RenderRange range = new WAbstractChartImplementation.RenderRange();
		range.minimum = iterator.getMinimum();
		range.maximum = iterator.getMaximum();
		return range;
	}

	public void update() {
		this.chart_.update();
	}

	private WCartesianChart chart_;
}
