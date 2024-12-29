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

abstract class SeriesRenderer {
  private static Logger logger = LoggerFactory.getLogger(SeriesRenderer.class);

  public abstract void addBreak();

  public abstract void addValue(
      double x, double y, double stacky, int xRow, int xColumn, int yRow, int yColumn);

  public abstract void paint();

  protected final WCartesianChart chart_;
  protected final WPainter painter_;
  protected final WDataSeries series_;
  protected final SeriesRenderIterator it_;

  protected SeriesRenderer(
      final WCartesianChart chart,
      final WPainter painter,
      final WDataSeries series,
      final SeriesRenderIterator it) {
    this.chart_ = chart;
    this.painter_ = painter;
    this.series_ = series;
    this.it_ = it;
  }

  protected static double crisp(double u) {
    return Math.floor(u) + 0.5;
  }

  protected WPointF hv(final WPointF p) {
    return this.chart_.hv(p);
  }

  protected WPointF hv(double x, double y) {
    return this.chart_.hv(x, y);
  }
}
