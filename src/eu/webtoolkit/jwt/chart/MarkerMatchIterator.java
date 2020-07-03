/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
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

final class MarkerMatchIterator extends SeriesIterator {
  private static Logger logger = LoggerFactory.getLogger(MarkerMatchIterator.class);

  public static final double MATCH_RADIUS = 5;

  public MarkerMatchIterator(
      final WCartesianChart chart,
      List<Double> xs,
      List<Double> ys,
      List<Double> rxs,
      List<Double> rys) {
    super();
    this.chart_ = chart;
    this.matchXs_ = xs;
    this.rXs_ = rxs;
    this.matchYs_ = ys;
    this.rYs_ = rys;
    this.matchedSeries_ = null;
    this.matchedXRow_ = -1;
    this.matchedXColumn_ = -1;
    this.matchedYRow_ = -1;
    this.matchedYColumn_ = -1;
  }

  public boolean startSeries(
      final WDataSeries series, double groupWidth, int numBarGroups, int currentBarGroup) {
    return this.matchedSeries_ == null
        && (series.getType() == SeriesType.Point
            || series.getType() == SeriesType.Point
            || series.getType() == SeriesType.Curve);
  }

  public void newValue(
      final WDataSeries series,
      double x,
      double y,
      double stackY,
      int xRow,
      int xColumn,
      int yRow,
      int yColumn) {
    if (this.matchedSeries_ != null) {
      return;
    }
    if (!Double.isNaN(x) && !Double.isNaN(y)) {
      Double scaleFactorP = series.getModel().getMarkerScaleFactor(yRow, yColumn);
      double scaleFactor = scaleFactorP != null ? scaleFactorP : 1.0;
      if (scaleFactor < 1.0) {
        scaleFactor = 1.0;
      }
      double scaledRx = scaleFactor * this.rXs_.get(series.getXAxis());
      double scaledRy = scaleFactor * this.rYs_.get(series.getYAxis());
      WPointF p =
          this.chart_.map(
              x,
              y,
              this.chart_.getXAxis(series.getXAxis()),
              this.chart_.getYAxis(series.getYAxis()),
              this.getCurrentXSegment(),
              this.getCurrentYSegment());
      double dx = p.getX() - this.matchYs_.get(series.getXAxis());
      double dy = p.getY() - this.matchYs_.get(series.getYAxis());
      double dx2 = dx * dx;
      double dy2 = dy * dy;
      double rx2 = scaledRx * scaledRx;
      double ry2 = scaledRy * scaledRy;
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
  private List<Double> matchXs_;
  private List<Double> rXs_;
  private List<Double> matchYs_;
  private List<Double> rYs_;
  private WDataSeries matchedSeries_;
  private int matchedXRow_;
  private int matchedXColumn_;
  private int matchedYRow_;
  private int matchedYColumn_;
}
