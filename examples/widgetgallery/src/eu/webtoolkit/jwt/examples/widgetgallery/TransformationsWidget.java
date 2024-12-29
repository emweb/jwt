/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
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

class TransformationsWidget extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(TransformationsWidget.class);

  public TransformationsWidget(WContainerWidget parentContainer) {
    super();
    this.resize(new WLength(300), new WLength(500));
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public TransformationsWidget() {
    this((WContainerWidget) null);
  }

  protected void paintEvent(WPaintDevice paintDevice) {
    WPainter painter = new WPainter(paintDevice);
    painter.setPen(new WPen(new WColor(StandardColor.Red)));
    painter.setBrush(new WBrush(new WColor(StandardColor.Black)));
    painter.save();
    painter.setPen(new WPen(new WColor(StandardColor.White)));
    painter.drawRect(0, 0, 100, 100);
    painter.save();
    painter.setBrush(new WBrush(new WColor(StandardColor.Yellow)));
    painter.drawRect(10, 10, 80, 80);
    painter.save();
    painter.setBrush(new WBrush(new WColor(StandardColor.Red)));
    painter.drawRect(20, 20, 60, 60);
    painter.restore();
    painter.drawRect(30, 30, 40, 40);
    painter.restore();
    painter.drawRect(40, 40, 20, 20);
    painter.restore();
    for (int i = 0; i < 2; i++) {
      painter.save();
      painter.translate(i * 100, 130);
      this.drawFilledPolygon(painter, new WColor(0, 255, 0, 255 - i * 200));
      painter.restore();
    }
    painter.translate(0, 300);
    painter.save();
    painter.translate(90, 0);
    for (int ring = 1; ring < 6; ring++) {
      painter.save();
      painter.setBrush(new WBrush(new WColor(51 * ring, 255 - 51 * ring, 255)));
      for (int j = 0; j < ring * 6; j++) {
        painter.rotate(360 / (ring * 6));
        painter.drawEllipse(0, ring * 12.5, 10, 10);
      }
      painter.restore();
    }
    painter.restore();
    painter.save();
    painter.translate(0, 100);
    this.drawFilledPolygon(painter, new WColor(0, 255, 0, 255));
    painter.translate(100, 0);
    painter.scale(1.2, 1);
    this.drawFilledPolygon(painter, new WColor(0, 255, 0, 55));
    painter.restore();
  }

  private void drawFilledPolygon(final WPainter painter, final WColor color) {
    painter.setBrush(new WBrush(color));
    WPointF[] points = {
      new WPointF(20, 0),
      new WPointF(60, 0),
      new WPointF(80, 34.6),
      new WPointF(60, 69.2),
      new WPointF(20, 69.2),
      new WPointF(0, 34.6),
      new WPointF(20, 0)
    };
    painter.drawPolygon(points, 6);
  }
}
