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

final class SeriesRenderIterator extends SeriesIterator {
  private static Logger logger = LoggerFactory.getLogger(SeriesRenderIterator.class);

  public SeriesRenderIterator(final WCartesianChart chart, final WPainter painter) {
    super();
    this.chart_ = chart;
    this.painter_ = painter;
    this.series_ = null;
  }

  public void startSegment(
      int currentXSegment, int currentYSegment, final WRectF currentSegmentArea) {
    super.startSegment(currentXSegment, currentYSegment, currentSegmentArea);
    final WAxis yAxis = this.chart_.getYAxis(this.series_.getYAxis());
    if (currentYSegment == 0) {
      this.maxY_ = Double.MAX_VALUE;
    } else {
      this.maxY_ = currentSegmentArea.getBottom();
    }
    if (currentYSegment == yAxis.getSegmentCount() - 1) {
      this.minY_ = -Double.MAX_VALUE;
    } else {
      this.minY_ = currentSegmentArea.getTop();
    }
  }

  public void endSegment() {
    super.endSegment();
    this.seriesRenderer_.paint();
  }

  public boolean startSeries(
      final WDataSeries series, double groupWidth, int numBarGroups, int currentBarGroup) {
    this.seriesRenderer_ = null;
    switch (series.getType()) {
      case Line:
      case Curve:
        this.seriesRenderer_ = new LineSeriesRenderer(this.chart_, this.painter_, series, this);
        break;
      case Bar:
        this.seriesRenderer_ =
            new BarSeriesRenderer(
                this.chart_,
                this.painter_,
                series,
                this,
                groupWidth,
                numBarGroups,
                currentBarGroup);
      default:
        break;
    }
    this.series_ = series;
    if (this.seriesRenderer_ != null) {
      this.painter_.save();
    }
    return this.seriesRenderer_ != null;
  }

  public void endSeries() {
    this.seriesRenderer_.paint();
    this.painter_.restore();

    this.series_ = null;
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
    if (Double.isNaN(x) || Double.isNaN(y)) {
      this.seriesRenderer_.addBreak();
    } else {
      this.seriesRenderer_.addValue(x, y, stackY, xRow, xColumn, yRow, yColumn);
    }
  }

  public double breakY(double y) {
    if (y < this.minY_) {
      return this.minY_;
    } else {
      if (y > this.maxY_) {
        return this.maxY_;
      } else {
        return y;
      }
    }
  }

  private final WCartesianChart chart_;
  private final WPainter painter_;
  private WDataSeries series_;
  private SeriesRenderer seriesRenderer_;
  private double minY_;
  private double maxY_;
}
