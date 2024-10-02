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

final class BarSeriesRenderer extends SeriesRenderer {
  private static Logger logger = LoggerFactory.getLogger(BarSeriesRenderer.class);

  public BarSeriesRenderer(
      final WCartesianChart chart,
      final WPainter painter,
      final WDataSeries series,
      final SeriesRenderIterator it,
      double groupWidth,
      int numGroups,
      int group) {
    super(chart, painter, series, it);
    this.groupWidth_ = groupWidth;
    this.numGroups_ = numGroups;
    this.group_ = group;
  }

  public void addValue(
      double x, double y, double stacky, int xRow, int xColumn, int yRow, int yColumn) {
    WPainterPath bar = new WPainterPath();
    final WAxis xAxis = this.chart_.getXAxis(this.series_.getXAxis());
    final WAxis yAxis = this.chart_.getYAxis(this.series_.getYAxis());
    WPointF topMid =
        this.chart_.map(
            x, y, xAxis, yAxis, this.it_.getCurrentXSegment(), this.it_.getCurrentYSegment());
    WPointF bottomMid =
        this.chart_.map(
            x, stacky, xAxis, yAxis, this.it_.getCurrentXSegment(), this.it_.getCurrentYSegment());
    FillRangeType fr = this.series_.getFillRange();
    switch (fr) {
      case MinimumValue:
        bottomMid =
            new WPointF(
                this.chart_
                    .map(
                        x,
                        stacky,
                        xAxis,
                        yAxis,
                        this.it_.getCurrentXSegment(),
                        this.it_.getCurrentYSegment())
                    .getX(),
                this.chart_.chartArea_.getBottom());
        break;
      case MaximumValue:
        bottomMid =
            new WPointF(
                this.chart_
                    .map(
                        x,
                        stacky,
                        xAxis,
                        yAxis,
                        this.it_.getCurrentXSegment(),
                        this.it_.getCurrentYSegment())
                    .getX(),
                this.chart_.chartArea_.getTop());
        break;
      default:
        break;
    }
    double g = this.numGroups_ + (this.numGroups_ - 1) * this.chart_.getBarMargin();
    double width = this.groupWidth_ / g;
    double left =
        topMid.getX()
            - this.groupWidth_ / 2
            + this.group_ * width * (1 + this.chart_.getBarMargin());
    boolean nonZeroWidth = this.chart_.isInteractive() || crisp(left) != crisp(left + width);
    bar.moveTo(this.hv(left, topMid.getY()));
    if (nonZeroWidth) {
      bar.lineTo(this.hv(left + width, topMid.getY()));
      bar.lineTo(this.hv(left + width, bottomMid.getY()));
    }
    bar.lineTo(this.hv(left, bottomMid.getY()));
    if (nonZeroWidth) {
      bar.closeSubPath();
    }
    this.painter_.setShadow(this.series_.getShadow());
    WTransform transform = this.chart_.zoomRangeTransform(xAxis, yAxis);
    if (nonZeroWidth) {
      WBrush brush = this.series_.getBrush().clone();
      SeriesIterator.setBrushColor(
          brush, this.series_, xRow, xColumn, yRow, yColumn, ItemDataRole.BarBrushColor);
      this.painter_.fillPath(transform.map(bar), brush);
    }
    this.painter_.setShadow(new WShadow());
    WPen pen = this.series_.getPen().clone();
    SeriesIterator.setPenColor(
        pen, this.series_, xRow, xColumn, yRow, yColumn, ItemDataRole.BarPenColor);
    this.painter_.strokePath(transform.map(bar).getCrisp(), pen);
    WString toolTip = this.series_.getModel().getToolTip(yRow, yColumn);
    if (!(toolTip.length() == 0) && nonZeroWidth) {
      WTransform t = this.painter_.getWorldTransform();
      WPointF tl = t.map(segmentPoint(bar, 0));
      WPointF tr = t.map(segmentPoint(bar, 1));
      WPointF br = t.map(segmentPoint(bar, 2));
      WPointF bl = t.map(segmentPoint(bar, 3));
      if (this.series_.getModel().flags(yRow, yColumn).contains(ItemFlag.DeferredToolTip)
          || this.series_.getModel().flags(yRow, yColumn).contains(ItemFlag.XHTMLText)) {
        this.chart_.hasDeferredToolTips_ = true;
        WCartesianChart.BarTooltip btt =
            new WCartesianChart.BarTooltip(this.series_, xRow, xColumn, yRow, yColumn);
        btt.xs[0] = tl.getX();
        btt.ys[0] = tl.getY();
        btt.xs[1] = tr.getX();
        btt.ys[1] = tr.getY();
        btt.xs[2] = br.getX();
        btt.ys[2] = br.getY();
        btt.xs[3] = bl.getX();
        btt.ys[3] = bl.getY();
        this.chart_.barTooltips_.add(btt);
      } else {
        double tlx = 0;
        double tly = 0;
        double brx = 0;
        double bry = 0;
        boolean useRect = false;
        if (fequal(tl.getY(), tr.getY())) {
          tlx = Math.min(tl.getX(), tr.getX());
          brx = Math.max(tl.getX(), tr.getX());
          tly = Math.min(tl.getY(), bl.getY());
          bry = Math.max(tl.getY(), br.getY());
          useRect = true;
        } else {
          if (fequal(tl.getX(), tr.getX())) {
            tlx = Math.min(tl.getX(), bl.getX());
            brx = Math.max(tl.getX(), bl.getX());
            tly = Math.min(tl.getY(), tr.getY());
            bry = Math.max(tl.getY(), tr.getY());
            useRect = true;
          }
        }
        WAbstractArea area = null;
        if (useRect) {
          area = new WRectArea(tlx, tly, brx - tlx, bry - tly);
        } else {
          WPolygonArea poly = new WPolygonArea();
          poly.addPoint(tl.getX(), tl.getY());
          poly.addPoint(tr.getX(), tr.getY());
          poly.addPoint(br.getX(), br.getY());
          poly.addPoint(bl.getX(), bl.getY());
          area = poly;
        }
        area.setToolTip(toolTip);
        this.chart_.addDataPointArea(this.series_, xRow, xColumn, area);
      }
    }
    double bTopMidY = this.it_.breakY(topMid.getY());
    double bBottomMidY = this.it_.breakY(bottomMid.getY());
    if (bTopMidY > topMid.getY() && bBottomMidY <= bottomMid.getY()) {
      WPainterPath breakPath = new WPainterPath();
      breakPath.moveTo(this.hv(left - 10, bTopMidY + 10));
      breakPath.lineTo(this.hv(left + width + 10, bTopMidY + 1));
      breakPath.lineTo(this.hv(left + width + 10, bTopMidY - 1));
      breakPath.lineTo(this.hv(left - 10, bTopMidY - 1));
      this.painter_.setPen(new WPen(PenStyle.None));
      this.painter_.setBrush(this.chart_.getBackground());
      this.painter_.drawPath(transform.map(breakPath).getCrisp());
      this.painter_.setPen(new WPen());
      WPainterPath line = new WPainterPath();
      line.moveTo(this.hv(left - 10, bTopMidY + 10));
      line.lineTo(this.hv(left + width + 10, bTopMidY + 1));
      this.painter_.drawPath(transform.map(line).getCrisp());
    }
    if (bBottomMidY < bottomMid.getY() && bTopMidY >= topMid.getY()) {
      WPainterPath breakPath = new WPainterPath();
      breakPath.moveTo(this.hv(left + width + 10, bBottomMidY - 10));
      breakPath.lineTo(this.hv(left - 10, bBottomMidY - 1));
      breakPath.lineTo(this.hv(left - 10, bBottomMidY + 1));
      breakPath.lineTo(this.hv(left + width + 10, bBottomMidY + 1));
      this.painter_.setBrush(this.chart_.getBackground());
      this.painter_.setPen(new WPen(PenStyle.None));
      this.painter_.drawPath(transform.map(breakPath).getCrisp());
      this.painter_.setPen(new WPen());
      WPainterPath line = new WPainterPath();
      line.moveTo(this.hv(left - 10, bBottomMidY - 1));
      line.lineTo(this.hv(left + width + 10, bBottomMidY - 10));
      this.painter_.drawPath(transform.map(line).getCrisp());
    }
  }

  public void addBreak() {}

  public void paint() {}

  private static WPointF segmentPoint(final WPainterPath path, int segment) {
    final WPainterPath.Segment s = path.getSegments().get(segment);
    return new WPointF(s.getX(), s.getY());
  }

  private static boolean fequal(double d1, double d2) {
    return Math.abs(d1 - d2) < 1E-5;
  }

  private double groupWidth_;
  private int numGroups_;
  private int group_;
}
