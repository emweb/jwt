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

class SamplePdfResource extends WPdfImage {
  private static Logger logger = LoggerFactory.getLogger(SamplePdfResource.class);

  public SamplePdfResource() {
    super(new WLength(400), new WLength(300));
    this.suggestFileName("line.pdf");
    this.paint();
  }

  private void paint() {
    WPainter painter = new WPainter(this);
    WPen thickPen = new WPen();
    thickPen.setWidth(new WLength(5));
    painter.setPen(thickPen);
    painter.drawLine(50, 250, 150, 50);
    painter.drawLine(150, 50, 250, 50);
    painter.drawText(
        0, 0, 400, 300, EnumSet.of(AlignmentFlag.Center, AlignmentFlag.Top), "Hello, PDF");
  }
}
