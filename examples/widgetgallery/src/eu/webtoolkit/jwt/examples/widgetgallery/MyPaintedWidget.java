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

class MyPaintedWidget extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(MyPaintedWidget.class);

  public MyPaintedWidget(WContainerWidget parentContainer) {
    super();
    this.end_ = 100;
    this.resize(new WLength(200), new WLength(60));
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public MyPaintedWidget() {
    this((WContainerWidget) null);
  }

  public void setEnd(int end) {
    this.end_ = end;
    this.update();
  }

  protected void paintEvent(WPaintDevice paintDevice) {
    WPainter painter = new WPainter(paintDevice);
    painter.setBrush(new WBrush(new WColor(StandardColor.Blue)));
    painter.drawRect(0, 0, this.end_, 50);
  }

  private int end_;
}
