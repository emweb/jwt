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
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExtremesIterator extends SeriesIterator {
  private static Logger logger = LoggerFactory.getLogger(ExtremesIterator.class);

  public ExtremesIterator(Axis axis, int yAxis, AxisScale scale) {
    super();
    this.axis_ = axis;
    this.yAxis_ = yAxis;
    this.scale_ = scale;
    this.minimum_ = Double.MAX_VALUE;
    this.maximum_ = -Double.MAX_VALUE;
  }

  public boolean startSeries(
      final WDataSeries series, double groupWidth, int numBarGroups, int currentBarGroup) {
    return this.axis_ == Axis.XAxis || series.getYAxis() == this.yAxis_;
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
    double v = this.axis_ == Axis.XAxis ? x : y;
    if (!Double.isNaN(v) && (this.scale_ != AxisScale.LogScale || v > 0.0)) {
      this.maximum_ = Math.max(v, this.maximum_);
      this.minimum_ = Math.min(v, this.minimum_);
    }
  }

  public double getMinimum() {
    return this.minimum_;
  }

  public double getMaximum() {
    return this.maximum_;
  }

  private Axis axis_;
  private int yAxis_;
  private AxisScale scale_;
  private double minimum_;
  private double maximum_;
}
