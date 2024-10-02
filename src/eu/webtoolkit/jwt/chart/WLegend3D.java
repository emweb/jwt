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

class WLegend3D extends WLegend {
  private static Logger logger = LoggerFactory.getLogger(WLegend3D.class);

  public void renderLegend(WPainter painter, final List<WAbstractDataSeries3D> dataseries) {
    if (!this.legendEnabled_) {
      return;
    }
    painter.save();
    int nbItems = 0;
    for (int i = 0; i < dataseries.size(); i++) {
      WAbstractDataSeries3D series = dataseries.get(i);
      if (series.isLegendEnabled() && !series.isHidden()) {
        nbItems++;
      }
    }
    double textHeight = this.legendFont_.getSizeLength().toPixels();
    double labelWidth = textHeight * 0.618;
    double lineHeight = textHeight * 1.5;
    double offset = (lineHeight - textHeight) / 2;
    painter.setPen(this.legendBorder_);
    painter.setFont(this.legendFont_);
    painter.setBrush(this.legendBackground_);
    int nbRows = nbItems / this.legendColumns_;
    if (nbItems % this.legendColumns_ != 0) {
      nbRows++;
    }
    painter.drawRect(
        0,
        0,
        this.legendColumns_ * this.legendColumnWidth_.getValue() + 2 * boxPadding,
        nbRows * lineHeight + 2 * boxPadding);
    painter.translate(boxPadding, boxPadding);
    int count = 0;
    for (int i = 0; i < dataseries.size(); i++) {
      WAbstractDataSeries3D series = dataseries.get(i);
      if (!series.isLegendEnabled() || series.isHidden()) {
        continue;
      }
      count++;
      if (series.getColorMap() == null) {
        WColor seriesColor = series.getChartpaletteColor();
        painter.fillRect(0, offset, labelWidth, textHeight, new WBrush(seriesColor));
      } else {
        series.getColorMap().createStrip(painter, new WRectF(0, offset, labelWidth, textHeight));
      }
      painter.drawText(
          labelWidth + 10,
          0,
          100,
          lineHeight,
          EnumUtils.or(EnumSet.of(AlignmentFlag.Left), AlignmentFlag.Middle),
          series.getTitle());
      if (count == this.legendColumns_) {
        painter.translate(
            -(this.legendColumns_ - 1) * this.legendColumnWidth_.getValue(), lineHeight);
        count = 0;
      } else {
        painter.translate(this.legendColumnWidth_.getValue(), 0);
      }
    }
    painter.restore();
  }

  public int getWidth() {
    return (int) (this.legendColumns_ * this.legendColumnWidth_.getValue() + 2 * boxPadding);
  }

  public int height(final List<WAbstractDataSeries3D> dataseries) {
    int nbItems = 0;
    for (int i = 0; i < dataseries.size(); i++) {
      WAbstractDataSeries3D series = dataseries.get(i);
      if (series.isLegendEnabled()) {
        nbItems++;
      }
    }
    double lineHeight = this.legendFont_.getSizeLength().getValue() * 1.5;
    int nbRows = nbItems / this.legendColumns_;
    if (nbItems % this.legendColumns_ != 0) {
      nbRows++;
    }
    return (int) (nbRows * lineHeight + 2 * boxPadding);
  }

  private static int boxPadding = 5;
}
