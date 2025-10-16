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

class PaintingImagesWidget extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(PaintingImagesWidget.class);

  public PaintingImagesWidget(WContainerWidget parentContainer) {
    super();
    this.resize(new WLength(639), new WLength(1310));
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public PaintingImagesWidget() {
    this((WContainerWidget) null);
  }

  protected void paintEvent(WPaintDevice paintDevice) {
    WPainter painter = new WPainter(paintDevice);
    WPainter.Image image =
        new WPainter.Image(new WWebRootDataInfo("pics/sintel_trailer.jpg"), 639, 354);
    painter.drawImage(0.0, 0.0, image);
    painter.drawImage(0.0, 364.0, image, 110.0, 75.0, 130.0, 110.0);
    WPointF location = new WPointF(0.0, 484.0);
    WRectF sourceRect = new WRectF(110.0, 75.0, 130.0, 110.0);
    painter.drawImage(location, image, sourceRect);
    WRectF destinationRect = new WRectF(0.0, 604.0, 130.0, 110.0);
    painter.drawImage(destinationRect, image);
    sourceRect = new WRectF(60.0, 80.0, 220.0, 180.0);
    destinationRect = new WRectF(0.0, 724.0, 130.0, 110.0);
    painter.drawImage(destinationRect, image, sourceRect);
    sourceRect = new WRectF(294.0, 226.0, 265.0, 41.0);
    destinationRect = new WRectF(0.0, 844.0, 639.0, 110.0);
    painter.drawImage(destinationRect, image, sourceRect);
    painter.translate(0, 964);
    painter.drawImage(0.0, 0.0, image);
    WPainterPath path = new WPainterPath();
    path.addEllipse(369, 91, 116, 116);
    path.addRect(294, 226, 265, 41);
    path.moveTo(92, 330);
    path.lineTo(66, 261);
    path.lineTo(122, 176);
    path.lineTo(143, 33);
    path.lineTo(164, 33);
    path.lineTo(157, 88);
    path.lineTo(210, 90);
    path.lineTo(263, 264);
    path.lineTo(228, 330);
    path.lineTo(92, 330);
    WPen pen = new WPen(new WColor(StandardColor.Red));
    pen.setWidth(new WLength(3));
    painter.strokePath(path, pen);
  }
}
