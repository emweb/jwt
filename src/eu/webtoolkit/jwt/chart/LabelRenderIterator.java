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

class LabelRenderIterator extends SeriesIterator {
  private static Logger logger = LoggerFactory.getLogger(LabelRenderIterator.class);

  public LabelRenderIterator(final WCartesianChart chart, final WPainter painter) {
    super();
    this.chart_ = chart;
    this.painter_ = painter;
  }

  public boolean startSeries(
      final WDataSeries series, double groupWidth, int numBarGroups, int currentBarGroup) {
    if (series.isLabelsEnabled(Axis.X) || series.isLabelsEnabled(Axis.Y)) {
      this.groupWidth_ = groupWidth;
      this.numGroups_ = numBarGroups;
      this.group_ = currentBarGroup;
      return true;
    } else {
      return false;
    }
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
      return;
    }
    WString text = new WString();
    if (series.isLabelsEnabled(Axis.X)) {
      text = this.chart_.getXAxis(series.getXAxis()).getLabel(x);
    }
    if (series.isLabelsEnabled(Axis.Y)) {
      if (!(text.length() == 0)) {
        text.append(": ");
      }
      text.append(this.chart_.getYAxis(series.getYAxis()).getLabel(y - stackY));
    }
    if (!(text.length() == 0)) {
      WPointF point =
          this.chart_.map(
              x, y, series.getAxis(), this.getCurrentXSegment(), this.getCurrentYSegment());
      WPointF p = point;
      if (series.getType() == SeriesType.Bar) {
        double g = this.numGroups_ + (this.numGroups_ - 1) * this.chart_.getBarMargin();
        double width = this.groupWidth_ / g;
        double left =
            p.getX()
                - this.groupWidth_ / 2
                + this.group_ * width * (1 + this.chart_.getBarMargin());
        p = new WPointF(left + width / 2, p.getY());
      }
      EnumSet<AlignmentFlag> alignment = EnumSet.noneOf(AlignmentFlag.class);
      if (series.getType() == SeriesType.Bar) {
        if (y < 0) {
          alignment =
              EnumSet.copyOf(EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Bottom));
        } else {
          alignment =
              EnumSet.copyOf(EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Top));
        }
      } else {
        alignment =
            EnumSet.copyOf(EnumUtils.or(EnumSet.of(AlignmentFlag.Center), AlignmentFlag.Bottom));
        p.setY(p.getY() - 3);
      }
      final WCartesianChart chart = this.chart_;
      WPen oldPen = chart.textPen_.clone();
      chart.textPen_.setColor(series.getLabelColor());
      WTransform t =
          this.chart_.zoomRangeTransform(
              this.chart_.getXAxis(series.getXAxis()), this.chart_.getYAxis(series.getYAxis()));
      WTransform ct = new WTransform();
      WJavaScriptHandle<WTransform> transformHandle = this.chart_.curveTransforms_.get(series);
      if (transformHandle != null) {
        ct.assign(this.chart_.curveTransform(series));
      }
      if (series.getType() == SeriesType.Bar) {
        chart.renderLabel(
            this.painter_,
            text,
            this.chart_.inverseHv(t.multiply(ct).map(this.chart_.hv(p))),
            alignment,
            0,
            3);
      } else {
        double dx = p.getX() - point.getX();
        double dy = p.getY() - point.getY();
        chart.renderLabel(
            this.painter_,
            text,
            this.chart_.inverseHv(
                t.multiply(ct).translate(new WPointF(dx, dy)).map(this.chart_.hv(point))),
            alignment,
            0,
            3);
      }
      chart.textPen_ = oldPen;
    }
  }

  private final WCartesianChart chart_;
  private final WPainter painter_;
  private double groupWidth_;
  private int numGroups_;
  private int group_;
}
