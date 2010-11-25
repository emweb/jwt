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

abstract class SeriesRenderer {
	public abstract void addValue(double x, double y, double stacky,
			WModelIndex xIndex, WModelIndex yIndex);

	public abstract void paint();

	protected WChart2DRenderer renderer_;
	protected WDataSeries series_;

	protected SeriesRenderer(WChart2DRenderer renderer, WDataSeries series,
			SeriesRenderIterator it) {
		this.renderer_ = renderer;
		this.series_ = series;
		this.it_ = it;
	}

	protected static double crisp(double u) {
		return Math.floor(u) + 0.5;
	}

	protected WPointF hv(WPointF p) {
		return this.renderer_.hv(p);
	}

	protected WPointF hv(double x, double y) {
		return this.renderer_.hv(x, y);
	}

	protected SeriesRenderIterator it_;
}
