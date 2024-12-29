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

class StyleWidget extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(StyleWidget.class);

  public StyleWidget(WContainerWidget parentContainer) {
    super();
    this.resize(new WLength(310), new WLength(1140));
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public StyleWidget() {
    this((WContainerWidget) null);
  }

  protected void paintEvent(WPaintDevice paintDevice) {
    WPainter painter = new WPainter(paintDevice);
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 6; col++) {
        WBrush brush = new WBrush(new WColor(255 - 42 * row, 255 - 42 * col, 0));
        painter.fillRect(row * 25, col * 25, 25, 25, brush);
      }
    }
    painter.translate(0, 160);
    WPen pen = new WPen();
    pen.setWidth(new WLength(3));
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 6; col++) {
        WPainterPath path = new WPainterPath();
        path.addEllipse(3 + col * 25, 3 + row * 25, 20, 20);
        pen.setColor(new WColor(0, 255 - 42 * row, 255 - 42 * col));
        painter.strokePath(path, pen);
      }
    }
    painter.translate(0, 160);
    painter.fillRect(0, 0, 150, 37.5, new WBrush(new WColor(StandardColor.Yellow)));
    painter.fillRect(0, 37.5, 150, 37.5, new WBrush(new WColor(StandardColor.Green)));
    painter.fillRect(0, 75, 150, 37.5, new WBrush(new WColor(StandardColor.Blue)));
    painter.fillRect(0, 112.5, 150, 37.5, new WBrush(new WColor(StandardColor.Red)));
    for (int i = 0; i < 10; i++) {
      WBrush brush = new WBrush(new WColor(255, 255, 255, 255 / 10 * i));
      for (int j = 0; j < 4; j++) {
        WPainterPath path = new WPainterPath();
        path.addRect(5 + i * 14, 5 + j * 37.5, 14, 27.5);
        painter.fillPath(path, brush);
      }
    }
    painter.translate(0, 160);
    painter.fillRect(0, 0, 75, 75, new WBrush(new WColor(StandardColor.Yellow)));
    painter.fillRect(75, 0, 75, 75, new WBrush(new WColor(StandardColor.Green)));
    painter.fillRect(0, 75, 75, 75, new WBrush(new WColor(StandardColor.Blue)));
    painter.fillRect(75, 75, 75, 75, new WBrush(new WColor(StandardColor.Red)));
    for (int i = 1; i < 8; i++) {
      WPainterPath path = new WPainterPath();
      path.addEllipse(75 - i * 10, 75 - i * 10, i * 20, i * 20);
      WBrush brush = new WBrush(new WColor(255, 255, 255, 50));
      painter.fillPath(path, brush);
    }
    painter.translate(0, 170);
    painter.setPen(new WPen(PenStyle.None));
    WGradient linGrad = new WGradient();
    linGrad.setLinearGradient(0, 0, 100, 150);
    linGrad.addColorStop(0, new WColor(255, 0, 0, 255));
    linGrad.addColorStop(0.5, new WColor(0, 0, 255, 255));
    linGrad.addColorStop(1, new WColor(0, 255, 0, 255));
    WBrush linearGradientBrush = new WBrush(linGrad);
    painter.setBrush(linearGradientBrush);
    painter.drawRect(0, 0, 100, 150);
    WGradient radGrad = new WGradient();
    radGrad.setRadialGradient(170, 100, 50, 130, 130);
    radGrad.addColorStop(0.2, new WColor(255, 0, 0, 255));
    radGrad.addColorStop(0.9, new WColor(0, 0, 255, 255));
    radGrad.addColorStop(1, new WColor(0, 0, 255, 0));
    WBrush radialGradientBrush = new WBrush(radGrad);
    painter.setBrush(radialGradientBrush);
    painter.drawEllipse(120, 50, 100, 100);
    painter.translate(0, 170);
    for (int i = 0; i < 11; i++) {
      WPainterPath path = new WPainterPath();
      path.moveTo(i * 14, 0);
      path.lineTo(i * 14, 150);
      pen = new WPen();
      pen.setWidth(new WLength(i + 1));
      painter.strokePath(path, pen);
    }
    painter.translate(160, 0);
    for (int i = 0; i < 11; i++) {
      WPainterPath path = new WPainterPath();
      if (i % 2 == 0) {
        path.moveTo(i * 14 - 0.5, 0);
        path.lineTo(i * 14 - 0.5, 150);
      } else {
        path.moveTo(i * 14, 0);
        path.lineTo(i * 14, 150);
      }
      pen = new WPen();
      pen.setCapStyle(PenCapStyle.Flat);
      pen.setWidth(new WLength(i + 1));
      painter.strokePath(path, pen);
    }
    painter.translate(-160, 170);
    WPainterPath guidePath = new WPainterPath();
    guidePath.moveTo(0, 10);
    guidePath.lineTo(150, 10);
    guidePath.moveTo(0, 140);
    guidePath.lineTo(150, 140);
    pen = new WPen(new WColor(StandardColor.Blue));
    painter.strokePath(guidePath, pen);
    List<WPainterPath> paths = new ArrayList<WPainterPath>();
    for (int i = 0; i < 3; i++) {
      WPainterPath path = new WPainterPath();
      path.moveTo(25 + i * 50, 10);
      path.lineTo(25 + i * 50, 140);
      paths.add(path);
    }
    pen = new WPen();
    pen.setWidth(new WLength(20));
    pen.setCapStyle(PenCapStyle.Flat);
    painter.strokePath(paths.get(0), pen);
    pen = new WPen();
    pen.setWidth(new WLength(20));
    pen.setCapStyle(PenCapStyle.Square);
    painter.strokePath(paths.get(1), pen);
    pen = new WPen();
    pen.setWidth(new WLength(20));
    pen.setCapStyle(PenCapStyle.Round);
    painter.strokePath(paths.get(2), pen);
    painter.translate(0, 170);
    paths.clear();
    for (int i = 0; i < 3; i++) {
      WPainterPath path = new WPainterPath();
      path.moveTo(15, 5 + i * 40);
      path.lineTo(45, 45 + i * 40);
      path.lineTo(75, 5 + i * 40);
      path.lineTo(105, 45 + i * 40);
      path.lineTo(135, 5 + i * 40);
      paths.add(path);
    }
    pen = new WPen();
    pen.setWidth(new WLength(20));
    pen.setJoinStyle(PenJoinStyle.Miter);
    painter.strokePath(paths.get(0), pen);
    pen = new WPen();
    pen.setWidth(new WLength(20));
    pen.setJoinStyle(PenJoinStyle.Bevel);
    painter.strokePath(paths.get(1), pen);
    pen = new WPen();
    pen.setWidth(new WLength(20));
    pen.setJoinStyle(PenJoinStyle.Round);
    painter.strokePath(paths.get(2), pen);
  }
}
