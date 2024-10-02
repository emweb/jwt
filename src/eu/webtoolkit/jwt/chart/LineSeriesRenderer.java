/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LineSeriesRenderer extends SeriesRenderer {
  private static Logger logger = LoggerFactory.getLogger(LineSeriesRenderer.class);

  public LineSeriesRenderer(
      final WCartesianChart chart,
      final WPainter painter,
      final WDataSeries series,
      final SeriesRenderIterator it) {
    super(chart, painter, series, it);
    this.curveLength_ = 0;
    this.curveFragmentLength_ = 0;
    this.curve_ = new WPainterPath();
    this.fill_ = new WPainterPath();
    this.p_1 = new WPointF();
    this.p0 = new WPointF();
    this.c_ = new WPointF();
    this.curve_.setOpenSubPathsEnabled(true);
  }

  public void addValue(
      double x, double y, double stackY, int xRow, int xColumn, int yRow, int yColumn) {
    WPointF p =
        this.chart_.map(
            x,
            y,
            this.chart_.getXAxis(this.series_.getXAxis()),
            this.chart_.getYAxis(this.series_.getYAxis()),
            this.it_.getCurrentXSegment(),
            this.it_.getCurrentYSegment());
    if (this.curveFragmentLength_ == 0) {
      this.curve_.moveTo(this.hv(p));
      if (this.series_.getFillRange() != FillRangeType.None
          && !this.series_.getBrush().equals(new WBrush(BrushStyle.None))) {
        this.fill_.moveTo(this.hv(this.fillOtherPoint(x)));
        this.fill_.lineTo(this.hv(p));
      }
    } else {
      if (this.series_.getType() == SeriesType.Line) {
        this.curve_.lineTo(this.hv(p));
        this.fill_.lineTo(this.hv(p));
      } else {
        if (this.curveFragmentLength_ == 1) {
          computeC(this.p0, p, this.c_);
        } else {
          WPointF c1 = new WPointF();
          WPointF c2 = new WPointF();
          computeC(this.p_1, this.p0, p, c1, c2);
          this.curve_.cubicTo(this.hv(this.c_), this.hv(c1), this.hv(this.p0));
          this.fill_.cubicTo(this.hv(this.c_), this.hv(c1), this.hv(this.p0));
          this.c_ = c2;
        }
      }
    }
    this.p_1 = this.p0;
    this.p0 = p;
    this.lastX_ = x;
    ++this.curveLength_;
    ++this.curveFragmentLength_;
  }

  public void addBreak() {
    if (this.curveFragmentLength_ > 1) {
      if (this.series_.getType() == SeriesType.Curve) {
        WPointF c1 = new WPointF();
        computeC(this.p0, this.p_1, c1);
        this.curve_.cubicTo(this.hv(this.c_), this.hv(c1), this.hv(this.p0));
        this.fill_.cubicTo(this.hv(this.c_), this.hv(c1), this.hv(this.p0));
      }
      if (this.series_.getFillRange() != FillRangeType.None
          && !this.series_.getBrush().equals(new WBrush(BrushStyle.None))) {
        this.fill_.lineTo(this.hv(this.fillOtherPoint(this.lastX_)));
        this.fill_.closeSubPath();
      }
    }
    this.curveFragmentLength_ = 0;
  }

  public void paint() {
    WJavaScriptHandle<WPainterPath> curveHandle = this.chart_.curvePaths_.get(this.series_);
    WJavaScriptHandle<WTransform> transformHandle = this.chart_.curveTransforms_.get(this.series_);
    WTransform transform =
        this.chart_.zoomRangeTransform(
            this.chart_.getXAxis(this.series_.getXAxis()),
            this.chart_.getYAxis(this.series_.getYAxis()));
    if (this.curveLength_ > 1) {
      if (this.series_.getType() == SeriesType.Curve) {
        WPointF c1 = new WPointF();
        computeC(this.p0, this.p_1, c1);
        this.curve_.cubicTo(this.hv(this.c_), this.hv(c1), this.hv(this.p0));
        this.fill_.cubicTo(this.hv(this.c_), this.hv(c1), this.hv(this.p0));
      }
      if (this.series_.getFillRange() != FillRangeType.None
          && !this.series_.getBrush().equals(new WBrush(BrushStyle.None))
          && !this.series_.isHidden()) {
        this.fill_.lineTo(this.hv(this.fillOtherPoint(this.lastX_)));
        this.fill_.closeSubPath();
        this.painter_.setShadow(this.series_.getShadow());
        WBrush brush = this.series_.getBrush();
        if (this.chart_.isSeriesSelectionEnabled()
            && this.chart_.getSelectedSeries() != null
            && this.chart_.getSelectedSeries() != this.series_) {
          brush.setColor(WCartesianChart.lightenColor(brush.getColor()));
        }
        this.painter_.fillPath(transform.map(this.fill_), brush);
      }
      if (this.series_.getFillRange() == FillRangeType.None) {
        this.painter_.setShadow(this.series_.getShadow());
      } else {
        this.painter_.setShadow(new WShadow());
      }
      WTransform ct = new WTransform();
      WTransform t = this.chart_.calculateCurveTransform(this.series_);
      if (transformHandle != null) {
        transformHandle.setValue(t);
        ct.assign(this.chart_.curveTransform(this.series_));
      } else {
        ct.assign(t);
        if (this.chart_.getOrientation() == Orientation.Horizontal) {
          ct.assign(
              new WTransform(0, 1, 1, 0, 0, 0)
                  .multiply(ct)
                  .multiply(new WTransform(0, 1, 1, 0, 0, 0)));
        }
      }
      this.series_.scaleDirty_ = false;
      this.series_.offsetDirty_ = false;
      WPainterPath curve = null;
      if (curveHandle != null) {
        curveHandle.setValue(this.curve_);
        curve = curveHandle.getValue();
      } else {
        curve = this.curve_;
      }
      if (!this.series_.isHidden()) {
        WPen pen = this.series_.getPen();
        if (this.chart_.isSeriesSelectionEnabled()
            && this.chart_.getSelectedSeries() != null
            && this.chart_.getSelectedSeries() != this.series_) {
          pen.setColor(WCartesianChart.lightenColor(pen.getColor()));
        }
        this.painter_.strokePath(transform.multiply(ct).map(curve), pen);
      }
    }
    this.curveLength_ = 0;
    this.curveFragmentLength_ = 0;
    this.curve_.assign(new WPainterPath());
    this.fill_.assign(new WPainterPath());
  }

  private int curveLength_;
  private int curveFragmentLength_;
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

  private static void computeC(final WPointF p, final WPointF p1, final WPointF c) {
    c.setX(p.getX() + 0.3 * (p1.getX() - p.getX()));
    c.setY(p.getY() + 0.3 * (p1.getY() - p.getY()));
  }

  private static void computeC(
      final WPointF p_1, final WPointF p0, final WPointF p1, final WPointF c1, final WPointF c2) {
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
      case MinimumValue:
        return new WPointF(
            this.chart_
                .map(
                    x,
                    0,
                    this.chart_.getXAxis(this.series_.getXAxis()),
                    this.chart_.getYAxis(this.series_.getYAxis()),
                    this.it_.getCurrentXSegment(),
                    this.it_.getCurrentYSegment())
                .getX(),
            this.chart_.chartArea_.getBottom());
      case MaximumValue:
        return new WPointF(
            this.chart_
                .map(
                    x,
                    0,
                    this.chart_.getXAxis(this.series_.getXAxis()),
                    this.chart_.getYAxis(this.series_.getYAxis()),
                    this.it_.getCurrentXSegment(),
                    this.it_.getCurrentYSegment())
                .getX(),
            this.chart_.chartArea_.getTop());
      case ZeroValue:
        return new WPointF(
            this.chart_.map(
                x,
                0,
                this.chart_.getXAxis(this.series_.getXAxis()),
                this.chart_.getYAxis(this.series_.getYAxis()),
                this.it_.getCurrentXSegment(),
                this.it_.getCurrentYSegment()));
      default:
        return new WPointF();
    }
  }
}
