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

class ShapesWidget extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(ShapesWidget.class);

  public ShapesWidget(WContainerWidget parentContainer) {
    super();
    this.resize(new WLength(310), new WLength(400));
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public ShapesWidget() {
    this((WContainerWidget) null);
  }

  protected void paintEvent(WPaintDevice paintDevice) {
    WPainter painter = new WPainter(paintDevice);
    painter.setPen(new WPen(new WColor(StandardColor.Red)));
    painter.drawLine(0, 0, 200, 0);
    painter.drawLine(200, 0, 200, 30);
    painter.drawRect(0, 25, 80, 50);
    painter.setBrush(new WBrush(new WColor(StandardColor.Green)));
    painter.drawRect(100, 25, 80, 50);
    painter.fillRect(220, 25, 80, 50, new WBrush(new WColor(0, 255, 0, 64)));
    painter.drawEllipse(0, 100, 80, 50);
    painter.drawChord(100, 100, 80, 50, 0, 180 * 16);
    painter.drawArc(220, 100, 50, 50, 90 * 16, 90 * 16);
    painter.drawArc(240, 100, 50, 50, 0, 90 * 16);
    painter.drawLine(265, 100, 265, 125);
    painter.drawLine(265, 125, 290, 125);
    WPointF[] points = {
      new WPointF(120, 170),
      new WPointF(160, 170),
      new WPointF(180, 204.6),
      new WPointF(160, 239.2),
      new WPointF(120, 239.2),
      new WPointF(100, 204.6)
    };
    painter.drawPolygon(points, 6);
    WPainterPath filledEllipsePath = new WPainterPath();
    filledEllipsePath.addEllipse(0, 180, 80, 50);
    filledEllipsePath.closeSubPath();
    painter.drawPath(filledEllipsePath);
    WPainterPath filledTrianglePath = new WPainterPath();
    filledTrianglePath.moveTo(0, 270);
    filledTrianglePath.lineTo(80, 270);
    filledTrianglePath.lineTo(0, 350);
    filledTrianglePath.closeSubPath();
    painter.drawPath(filledTrianglePath);
    WPainterPath strokedTrianglePath = new WPainterPath();
    strokedTrianglePath.moveTo(100, 270);
    strokedTrianglePath.lineTo(100, 350);
    strokedTrianglePath.lineTo(20, 350);
    strokedTrianglePath.closeSubPath();
    WPen pen = new WPen();
    pen.setWidth(new WLength(3));
    painter.strokePath(strokedTrianglePath, pen);
    WPainterPath quadraticCurvePath = new WPainterPath();
    quadraticCurvePath.moveTo(250, 150);
    quadraticCurvePath.quadTo(200, 150, 200, 187.5);
    quadraticCurvePath.quadTo(200, 225, 225, 225);
    quadraticCurvePath.quadTo(225, 245, 205, 250);
    quadraticCurvePath.quadTo(235, 245, 240, 225);
    quadraticCurvePath.quadTo(300, 225, 300, 187.5);
    quadraticCurvePath.quadTo(300, 150, 250, 150);
    painter.strokePath(quadraticCurvePath, pen);
    WPainterPath bezierCurvePath = new WPainterPath();
    bezierCurvePath.moveTo(255, 285);
    bezierCurvePath.cubicTo(255, 282, 250, 270, 230, 270);
    bezierCurvePath.cubicTo(200, 270, 200, 307.5, 200, 307.5);
    bezierCurvePath.cubicTo(200, 325, 220, 357, 255, 365);
    bezierCurvePath.cubicTo(290, 347, 310, 325, 310, 307.5);
    bezierCurvePath.cubicTo(310, 307.5, 310, 270, 290, 270);
    bezierCurvePath.cubicTo(265, 270, 255, 282, 255, 285);
    painter.setBrush(new WBrush(new WColor(StandardColor.Red)));
    painter.drawPath(bezierCurvePath);
  }
}
