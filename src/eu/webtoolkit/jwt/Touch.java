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

/**
 * A single finger touch of a touch event.
 *
 * <p>
 *
 * @see WTouchEvent
 */
public class Touch {
  private static Logger logger = LoggerFactory.getLogger(Touch.class);

  /** Constructor. */
  public Touch(
      long identifier,
      int clientX,
      int clientY,
      int documentX,
      int documentY,
      int screenX,
      int screenY,
      int widgetX,
      int widgetY) {
    this.clientX_ = clientX;
    this.clientY_ = clientY;
    this.documentX_ = documentX;
    this.documentY_ = documentY;
    this.screenX_ = screenX;
    this.screenY_ = screenY;
    this.widgetX_ = widgetX;
    this.widgetY_ = widgetY;
    this.identifier_ = identifier;
  }
  /** Returns the touch position relative to the document. */
  public Coordinates getDocument() {
    return new Coordinates(this.documentX_, this.documentY_);
  }
  /**
   * Returns the touch position relative to the window.
   *
   * <p>This differs from {@link Touch#getDocument() getDocument()} only when scrolling through the
   * document.
   */
  public Coordinates getWindow() {
    return new Coordinates(this.clientX_, this.clientY_);
  }
  /** Returns the touch position relative to the screen. */
  public Coordinates getScreen() {
    return new Coordinates(this.screenX_, this.screenY_);
  }
  /** Returns the touch position relative to the widget. */
  public Coordinates getWidget() {
    return new Coordinates(this.widgetX_, this.widgetY_);
  }
  /** Returns the identifier for this touch. */
  public long getIdentifier() {
    return this.identifier_;
  }

  private int clientX_;
  private int clientY_;
  private int documentX_;
  private int documentY_;
  private int screenX_;
  private int screenY_;
  private int widgetX_;
  private int widgetY_;
  private long identifier_;

  static String concat(final String prefix, int prefixLength, String s2) {
    return prefix + s2;
  }

  static int asInt(final String v) {
    return Integer.parseInt(v);
  }

  static long asLongLong(final String v) {
    return Long.parseLong(v);
  }

  static int parseIntParameter(final WebRequest request, final String name, int ifMissing) {
    String p;
    if ((p = request.getParameter(name)) != null) {
      try {
        return asInt(p);
      } catch (final RuntimeException ee) {
        logger.error(
            new StringWriter()
                .append("Could not cast event property '")
                .append(name)
                .append(": ")
                .append(p)
                .append("' to int")
                .toString());
        return ifMissing;
      }
    } else {
      return ifMissing;
    }
  }

  static String getStringParameter(final WebRequest request, final String name) {
    String p;
    if ((p = request.getParameter(name)) != null) {
      return p;
    } else {
      return "";
    }
  }

  static void decodeTouches(String str, final List<Touch> result) {
    if (str.length() == 0) {
      return;
    }
    List<String> s = new ArrayList<String>();
    StringUtils.split(s, str, ";", false);
    if (s.size() % 9 != 0) {
      logger.error(
          new StringWriter()
              .append("Could not parse touches array '")
              .append(str)
              .append("'")
              .toString());
      return;
    }
    try {
      for (int i = 0; i < s.size(); i += 9) {
        result.add(
            new Touch(
                asLongLong(s.get(i + 0)),
                asInt(s.get(i + 1)),
                asInt(s.get(i + 2)),
                asInt(s.get(i + 3)),
                asInt(s.get(i + 4)),
                asInt(s.get(i + 5)),
                asInt(s.get(i + 6)),
                asInt(s.get(i + 7)),
                asInt(s.get(i + 8))));
      }
    } catch (final RuntimeException ee) {
      logger.error(
          new StringWriter()
              .append("Could not parse touches array '")
              .append(str)
              .append("'")
              .toString());
      return;
    }
  }
}
