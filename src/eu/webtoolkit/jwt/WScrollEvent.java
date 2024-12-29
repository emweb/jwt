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
 * A class providing details for a scroll event.
 *
 * <p>
 *
 * @see WContainerWidget#scrolled()
 */
public class WScrollEvent implements WAbstractEvent {
  private static Logger logger = LoggerFactory.getLogger(WScrollEvent.class);

  /** Default constructor. */
  public WScrollEvent() {
    super();
    this.jsEvent_ = new JavaScriptEvent();
  }
  /**
   * Returns the current horizontal scroll position.
   *
   * <p>
   *
   * @see WScrollEvent#getScrollY()
   * @see WScrollEvent#getViewportWidth()
   */
  public int getScrollX() {
    return this.jsEvent_.scrollX;
  }
  /**
   * Returns the current vertical scroll position.
   *
   * <p>
   *
   * @see WScrollEvent#getScrollX()
   * @see WScrollEvent#getViewportHeight()
   */
  public int getScrollY() {
    return this.jsEvent_.scrollY;
  }
  /**
   * Returns the current horizontal viewport width.
   *
   * <p>Returns the current viewport width.
   *
   * <p>
   *
   * @see WScrollEvent#getViewportHeight()
   * @see WScrollEvent#getScrollX()
   */
  public int getViewportWidth() {
    return this.jsEvent_.viewportWidth;
  }
  /**
   * Returns the current horizontal viewport height.
   *
   * <p>Returns the current viewport height.
   *
   * <p>
   *
   * @see WScrollEvent#getViewportWidth()
   * @see WScrollEvent#getScrollY()
   */
  public int getViewportHeight() {
    return this.jsEvent_.viewportHeight;
  }

  public WAbstractEvent createFromJSEvent(final JavaScriptEvent jsEvent) {
    return new WScrollEvent(jsEvent);
  }

  static WScrollEvent templateEvent = new WScrollEvent();
  private JavaScriptEvent jsEvent_;

  private WScrollEvent(final JavaScriptEvent jsEvent) {
    super();
    this.jsEvent_ = jsEvent;
  }

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
