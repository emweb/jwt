/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

abstract class WWidgetPainter {
  private static Logger logger = LoggerFactory.getLogger(WWidgetPainter.class);

  enum RenderType {
    InlineVml,
    InlineSvg,
    HtmlCanvas,
    PngImage;

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return ordinal();
    }
  }

  public abstract WPaintDevice createPaintDevice(boolean paintUpdate);

  public abstract WPaintDevice getPaintDevice(boolean paintUpdate);

  public abstract void createContents(DomElement element, WPaintDevice device);

  public abstract void updateContents(final List<DomElement> result, WPaintDevice device);

  public abstract WWidgetPainter.RenderType getRenderType();

  protected WWidgetPainter(WPaintedWidget widget) {
    this.widget_ = widget;
  }

  protected WPaintedWidget widget_;
}
