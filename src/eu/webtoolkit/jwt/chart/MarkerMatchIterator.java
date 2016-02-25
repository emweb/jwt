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

class MarkerMatchIterator extends SeriesIterator {
	private static Logger logger = LoggerFactory
			.getLogger(MarkerMatchIterator.class);

	public static final double MATCH_RADIUS = 5;

	public MarkerMatchIterator(final WCartesianChart chart, double x, double y,
			double rx, double ry) {
		super();
		this.chart_ = chart;
		this.matchX_ = x;
		this.matchY_ = y;
		this.rX_ = rx;
		this.rY_ = ry;
		this.matchedSeries_ = null;
		this.matchedXRow_ = -1;
		this.matchedXColumn_ = -1;
		this.matchedYRow_ = -1;
		this.matchedYColumn_ = -1;
	}

	public boolean startSeries(final WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		return this.matchedSeries_ == null
				&& series.getType() == SeriesType.PointSeries;
	}

	public void newValue(final WDataSeries series, double x, double y,
			double stackY, int xRow, int xColumn, int yRow, int yColumn) {
		if (this.matchedSeries_ != null) {
			return;
		}
		if (!Double.isNaN(x) && !Double.isNaN(y)) {
			WPointF p = this.chart_.map(x, y, series.getAxis(),
					this.getCurrentXSegment(), this.getCurrentYSegment());
			double dx = p.getX() - this.matchX_;
			double dy = p.getY() - this.matchY_;
			double dx2 = dx * dx;
			double dy2 = dy * dy;
			double rx2 = this.rX_ * this.rX_;
			double ry2 = this.rY_ * this.rY_;
			if (dx2 / rx2 + dy2 / ry2 <= 1) {
				this.matchedXRow_ = xRow;
				this.matchedXColumn_ = xColumn;
				this.matchedYRow_ = yRow;
				this.matchedYColumn_ = yColumn;
				this.matchedSeries_ = series;
			}
		}
	}

	public WDataSeries getMatchedSeries() {
		return this.matchedSeries_;
	}

	public int getXRow() {
		return this.matchedXRow_;
	}

	public int getXColumn() {
		return this.matchedXColumn_;
	}

	public int getYRow() {
		return this.matchedYRow_;
	}

	public int getYColumn() {
		return this.matchedYColumn_;
	}

	private final WCartesianChart chart_;
	private double matchX_;
	private double matchY_;
	private double rX_;
	private double rY_;
	private WDataSeries matchedSeries_;
	private int matchedXRow_;
	private int matchedXColumn_;
	private int matchedYRow_;
	private int matchedYColumn_;
}
