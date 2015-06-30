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

class LineSeriesRenderer extends SeriesRenderer {
	private static Logger logger = LoggerFactory
			.getLogger(LineSeriesRenderer.class);

	public LineSeriesRenderer(final WCartesianChart chart,
			final WPainter painter, final WDataSeries series,
			final SeriesRenderIterator it) {
		super(chart, painter, series, it);
		this.curveLength_ = 0;
		this.curve_ = new WPainterPath();
		this.fill_ = new WPainterPath();
		this.p_1 = new WPointF();
		this.p0 = new WPointF();
		this.c_ = new WPointF();
	}

	public void addValue(double x, double y, double stacky,
			final WModelIndex xIndex, final WModelIndex yIndex) {
		WPointF p = this.chart_.map(x, y, this.series_.getAxis(), this.it_
				.getCurrentXSegment(), this.it_.getCurrentYSegment());
		if (this.curveLength_ == 0) {
			this.curve_.moveTo(this.hv(p));
			if (this.series_.getFillRange() != FillRangeType.NoFill
					&& !this.series_.getBrush().equals(
							new WBrush(BrushStyle.NoBrush))) {
				this.fill_.moveTo(this.hv(this.fillOtherPoint(x)));
				this.fill_.lineTo(this.hv(p));
			}
		} else {
			if (this.series_.getType() == SeriesType.LineSeries) {
				this.curve_.lineTo(this.hv(p));
				this.fill_.lineTo(this.hv(p));
			} else {
				if (this.curveLength_ == 1) {
					computeC(this.p0, p, this.c_);
				} else {
					WPointF c1 = new WPointF();
					WPointF c2 = new WPointF();
					computeC(this.p_1, this.p0, p, c1, c2);
					this.curve_.cubicTo(this.hv(this.c_), this.hv(c1), this
							.hv(this.p0));
					this.fill_.cubicTo(this.hv(this.c_), this.hv(c1), this
							.hv(this.p0));
					this.c_ = c2;
				}
			}
		}
		this.p_1 = this.p0;
		this.p0 = p;
		this.lastX_ = x;
		++this.curveLength_;
	}

	public void paint() {
		final WCartesianChart chart = this.chart_;
		final WJavaScriptHandle<WPainterPath> curveHandle = chart.curvePaths_
				.get(this.series_.getModelColumn());
		WTransform transform = chart.getCombinedTransform();
		if (this.curveLength_ > 1) {
			if (this.series_.getType() == SeriesType.CurveSeries) {
				WPointF c1 = new WPointF();
				computeC(this.p0, this.p_1, c1);
				this.curve_.cubicTo(this.hv(this.c_), this.hv(c1), this
						.hv(this.p0));
				this.fill_.cubicTo(this.hv(this.c_), this.hv(c1), this
						.hv(this.p0));
			}
			if (this.series_.getFillRange() != FillRangeType.NoFill
					&& !this.series_.getBrush().equals(
							new WBrush(BrushStyle.NoBrush))) {
				this.fill_.lineTo(this.hv(this.fillOtherPoint(this.lastX_)));
				this.fill_.closeSubPath();
				this.painter_.setShadow(this.series_.getShadow());
				this.painter_.fillPath(transform.map(this.fill_), this.series_
						.getBrush());
			}
			if (this.series_.getFillRange() == FillRangeType.NoFill) {
				this.painter_.setShadow(this.series_.getShadow());
			} else {
				this.painter_.setShadow(new WShadow());
			}
			curveHandle.setValue(this.curve_);
			this.painter_.strokePath(transform.map(curveHandle.getValue()),
					this.series_.getPen());
		}
		this.curveLength_ = 0;
		this.curve_.assign(new WPainterPath());
		this.fill_.assign(new WPainterPath());
	}

	private int curveLength_;
	private WPainterPath curve_;
	private WPainterPath fill_;
	private double lastX_;
	private WPointF p_1;
	private WPointF p0;
	private WPointF c_;

	private static double dist(final WPointF p1, final WPointF p2) {
		double dx = p2.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		return Math.sqrt(dx * dx + dy * dy);
	}

	private static void computeC(final WPointF p, final WPointF p1,
			final WPointF c) {
		c.setX(p.getX() + 0.3 * (p1.getX() - p.getX()));
		c.setY(p.getY() + 0.3 * (p1.getY() - p.getY()));
	}

	private static void computeC(final WPointF p_1, final WPointF p0,
			final WPointF p1, final WPointF c1, final WPointF c2) {
		double m1x = (p_1.getX() + p0.getX()) / 2.0;
		double m1y = (p_1.getY() + p0.getY()) / 2.0;
		double m2x = (p0.getX() + p1.getX()) / 2.0;
		double m2y = (p0.getY() + p1.getY()) / 2.0;
		double L1 = dist(p_1, p0);
		double L2 = dist(p0, p1);
		double r = L1 / (L1 + L2);
		c1.setX(p0.getX() - r * (m2x - m1x));
		c1.setY(p0.getY() - r * (m2y - m1y));
		r = 1 - r;
		c2.setX(p0.getX() - r * (m1x - m2x));
		c2.setY(p0.getY() - r * (m1y - m2y));
	}

	private WPointF fillOtherPoint(double x) {
		FillRangeType fr = this.series_.getFillRange();
		switch (fr) {
		case MinimumValueFill:
			return new WPointF(this.chart_.map(x, 0, this.series_.getAxis(),
					this.it_.getCurrentXSegment(),
					this.it_.getCurrentYSegment()).getX(),
					this.chart_.chartArea_.getBottom());
		case MaximumValueFill:
			return new WPointF(this.chart_.map(x, 0, this.series_.getAxis(),
					this.it_.getCurrentXSegment(),
					this.it_.getCurrentYSegment()).getX(),
					this.chart_.chartArea_.getTop());
		case ZeroValueFill:
			return new WPointF(this.chart_.map(x, 0, this.series_.getAxis(),
					this.it_.getCurrentXSegment(), this.it_
							.getCurrentYSegment()));
		default:
			return new WPointF();
		}
	}
}
