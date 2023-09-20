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

interface WAbstractChartImplementation {
  static class RenderRange {
    private static Logger logger = LoggerFactory.getLogger(RenderRange.class);

    public double minimum;
    public double maximum;
  }

  public ChartType getChartType();

  public Orientation getOrientation();

  public int getAxisPadding();

  public int numberOfCategories(Axis axis);

  public default int numberOfCategories() {
    return numberOfCategories(Axis.X);
  }

  public WString categoryLabel(int u, Axis axis);

  public default WString categoryLabel(int u) {
    return categoryLabel(u, Axis.X);
  }

  public WAbstractChartImplementation.RenderRange computeRenderRange(
      Axis axis, int xAxis, int yAxis, AxisScale scale);

  public default WAbstractChartImplementation.RenderRange computeRenderRange(
      Axis axis, int xAxis, int yAxis) {
    return computeRenderRange(axis, xAxis, yAxis, AxisScale.Linear);
  }

  public boolean isOnDemandLoadingEnabled();

  public void update();
}
