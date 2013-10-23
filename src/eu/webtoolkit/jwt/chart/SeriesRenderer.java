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

abstract class SeriesRenderer {
	private static Logger logger = LoggerFactory
			.getLogger(SeriesRenderer.class);

	public abstract void addValue(double x, double y, double stacky,
			final WModelIndex xIndex, final WModelIndex yIndex);

	public abstract void paint();

	protected final WChart2DRenderer renderer_;
	protected final WDataSeries series_;

	protected SeriesRenderer(final WChart2DRenderer renderer,
			final WDataSeries series, final SeriesRenderIterator it) {
		this.renderer_ = renderer;
		this.series_ = series;
		this.it_ = it;
	}

	protected static double crisp(double u) {
		return Math.floor(u) + 0.5;
	}

	protected WPointF hv(final WPointF p) {
		return this.renderer_.hv(p);
	}

	protected WPointF hv(double x, double y) {
		return this.renderer_.hv(x, y);
	}

	protected final SeriesRenderIterator it_;
}
