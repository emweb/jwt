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

class WChart3DImplementation implements WAbstractChartImplementation {
  private static Logger logger = LoggerFactory.getLogger(WChart3DImplementation.class);

  public WChart3DImplementation(WCartesian3DChart chart) {
    super();
    this.chart_ = chart;
  }

  public ChartType getChartType() {
    return this.chart_.getType();
  }

  public Orientation getOrientation() {
    return Orientation.Vertical;
  }

  public int getAxisPadding() {
    return 0;
  }

  public int numberOfCategories(Axis axis) {
    if (this.chart_.getDataSeries().size() == 0) {
      return 10;
    }
    WAbstractGridData first;
    if (axis == Axis.X3D) {
      first = ObjectUtils.cast(this.chart_.getDataSeries().get(0), WAbstractGridData.class);
      if (first == null) {
        throw new WException(
            "WChart3DImplementation: can only count the categories in WAbstractGridData");
      } else {
        return first.getNbXPoints();
      }
    } else {
      if (axis == Axis.Y3D) {
        first = ObjectUtils.cast(this.chart_.getDataSeries().get(0), WAbstractGridData.class);
        if (first == null) {
          throw new WException(
              "WChart3DImplementation: can only count the categories in WAbstractGridData");
        } else {
          return first.getNbYPoints();
        }
      } else {
        throw new WException("WChart3DImplementation: don't know this type of axis");
      }
    }
  }

  public WString categoryLabel(int u, Axis axis) {
    if (this.chart_.getDataSeries().size() == 0) {
      return new WString(String.valueOf(u));
    }
    WAbstractGridData first =
        ObjectUtils.cast(this.chart_.getDataSeries().get(0), WAbstractGridData.class);
    if (!(first != null)) {
      throw new WException(
          "WChart3DImplementation: can only count the categories in WAbstractGridData");
    }
    return first.axisLabel(u, axis);
  }

  public WAbstractChartImplementation.RenderRange computeRenderRange(
      Axis axis, int xAxis, int yAxis, AxisScale scale) {
    WAbstractChartImplementation.RenderRange range = new WAbstractChartImplementation.RenderRange();
    final List<WAbstractDataSeries3D> series = this.chart_.getDataSeries();
    if (series.size() == 0) {
      range.minimum = 0;
      range.maximum = 100;
      return range;
    }
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    int xDim = 0;
    int yDim = 0;
    double stackedBarsHeight = 0.0;
    WAbstractGridData griddata;
    switch (this.chart_.getType()) {
      case Scatter:
        for (int i = 0; i < series.size(); i++) {
          double seriesMin = series.get(i).minimum(axis);
          double seriesMax = series.get(i).maximum(axis);
          if (seriesMin < min) {
            min = seriesMin;
          }
          if (seriesMax > max) {
            max = seriesMax;
          }
        }
        range.minimum = min;
        range.maximum = max;
        return range;
      case Category:
        for (int k = 0; k < series.size(); k++) {
          griddata = ObjectUtils.cast(series.get(k), WAbstractGridData.class);
          if (griddata == null || griddata.getType() != Series3DType.Bar) {
            throw new WException("WChart3DImplementation: not all data is categorical");
          }
        }
        xDim = (ObjectUtils.cast(series.get(0), WAbstractGridData.class)).getNbXPoints();
        yDim = (ObjectUtils.cast(series.get(0), WAbstractGridData.class)).getNbYPoints();
        min = 0.0;
        for (int i = 0; i < xDim; i++) {
          for (int j = 0; j < yDim; j++) {
            for (int k = 0; k < series.size(); k++) {
              if (series.get(k).isHidden()) {
                continue;
              }
              griddata = ObjectUtils.cast(series.get(k), WAbstractGridData.class);
              stackedBarsHeight += StringUtils.asNumber(griddata.data(i, j));
            }
            if (stackedBarsHeight > max) {
              max = stackedBarsHeight;
            }
            stackedBarsHeight = 0;
          }
        }
        if (max == Double.MIN_VALUE) {
          max = 100.0;
        }
        range.minimum = min;
        range.maximum = max;
        return range;
      default:
        throw new WException("WChart3DImplementation: don't know this axis-type");
    }
  }

  public boolean isOnDemandLoadingEnabled() {
    return false;
  }

  public void update() {
    this.chart_.updateChart(EnumSet.of(ChartUpdates.GLContext, ChartUpdates.GLTextures));
  }

  private WCartesian3DChart chart_;
}
