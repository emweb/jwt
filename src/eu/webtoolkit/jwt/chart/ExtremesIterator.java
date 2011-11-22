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

class ExtremesIterator extends SeriesIterator {
	private static Logger logger = LoggerFactory
			.getLogger(ExtremesIterator.class);

	public ExtremesIterator(Axis axis, AxisScale scale) {
		super();
		this.axis_ = axis;
		this.scale_ = scale;
		this.minimum_ = Double.MAX_VALUE;
		this.maximum_ = -Double.MAX_VALUE;
	}

	public boolean startSeries(WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		return this.axis_ == Axis.XAxis || series.getAxis() == this.axis_;
	}

	public void newValue(WDataSeries series, double x, double y, double stackY,
			WModelIndex xIndex, WModelIndex yIndex) {
		double v = this.axis_ == Axis.XAxis ? x : y;
		if (!Double.isNaN(v) && (this.scale_ != AxisScale.LogScale || v > 0.0)) {
			this.maximum_ = Math.max(v, this.maximum_);
			this.minimum_ = Math.min(v, this.minimum_);
		}
	}

	public double getMinimum() {
		return this.minimum_;
	}

	public double getMaximum() {
		return this.maximum_;
	}

	private Axis axis_;
	private AxisScale scale_;
	private double minimum_;
	private double maximum_;
}
