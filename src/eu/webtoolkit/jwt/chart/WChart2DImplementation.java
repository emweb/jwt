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

class WChart2DImplementation implements WAbstractChartImplementation {
  private static Logger logger = LoggerFactory.getLogger(WChart2DImplementation.class);

  public WChart2DImplementation(WCartesianChart chart) {
    super();
    this.chart_ = chart;
  }

  public ChartType getChartType() {
    return this.chart_.getType();
  }

  public Orientation getOrientation() {
    return this.chart_.getOrientation();
  }

  public int getAxisPadding() {
    return this.chart_.getAxisPadding();
  }

  public int numberOfCategories(Axis axis) {
    if (this.chart_.getModel() != null) {
      return this.chart_.getModel().getRowCount();
    } else {
      return 0;
    }
  }

  public WString categoryLabel(int u, Axis axis) {
    if (this.chart_.XSeriesColumn() != -1) {
      if (u < this.chart_.getModel().getRowCount()) {
        return this.chart_.getModel().getDisplayData(u, this.chart_.XSeriesColumn());
      } else {
        return new WString();
      }
    } else {
      return new WString();
    }
  }

  public WAbstractChartImplementation.RenderRange computeRenderRange(
      Axis axis, int xAxis, int yAxis, AxisScale scale) {
    ExtremesIterator iterator = new ExtremesIterator(axis, xAxis, yAxis, scale);
    this.chart_.iterateSeries(iterator, (WPainter) null, false, axis == Axis.X);
    WAbstractChartImplementation.RenderRange range = new WAbstractChartImplementation.RenderRange();
    range.minimum = iterator.getMinimum();
    range.maximum = iterator.getMaximum();
    return range;
  }

  public boolean isOnDemandLoadingEnabled() {
    return this.chart_.isOnDemandLoadingEnabled();
  }

  public void update() {
    this.chart_.update();
  }

  private WCartesianChart chart_;
}
