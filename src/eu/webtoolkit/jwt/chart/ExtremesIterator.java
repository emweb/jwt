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

class ExtremesIterator extends SeriesIterator {
	public ExtremesIterator(Axis axis, AxisScale scale) {
		super();
		this.axis_ = axis;
		this.scale_ = scale;
		this.minimum_ = WAxis.AUTO_MINIMUM;
		this.maximum_ = WAxis.AUTO_MAXIMUM;
	}

	public boolean startSeries(WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		return series.getAxis() == this.axis_;
	}

	public void newValue(WDataSeries series, double x, double y, double stackY,
			WModelIndex xIndex, WModelIndex yIndex) {
		if (!Double.isNaN(y) && (this.scale_ != AxisScale.LogScale || y > 0.0)) {
			this.maximum_ = Math.max(y, this.maximum_);
			this.minimum_ = Math.min(y, this.minimum_);
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
