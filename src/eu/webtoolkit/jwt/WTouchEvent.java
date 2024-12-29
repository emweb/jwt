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
 * A class providing details for a touch event.
 *
 * <p>
 *
 * @see WInteractWidget#touchStarted()
 * @see WInteractWidget#touchMoved()
 * @see WInteractWidget#touchEnded()
 */
public class WTouchEvent implements WAbstractEvent {
  private static Logger logger = LoggerFactory.getLogger(WTouchEvent.class);

  /** Default constructor. */
  public WTouchEvent() {
    super();
    this.jsEvent_ = new JavaScriptEvent();
  }
  /** Returns a list of objects for every finger currently touching the screen. */
  public List<Touch> getTouches() {
    return this.jsEvent_.touches;
  }
  /** Returns a list of objects for finger touches that started out within the same widget. */
  public List<Touch> getTargetTouches() {
    return this.jsEvent_.targetTouches;
  }
  /** Returns a list of objects for every finger involved in the event. */
  public List<Touch> getChangedTouches() {
    return this.jsEvent_.changedTouches;
  }

  public WAbstractEvent createFromJSEvent(final JavaScriptEvent jsEvent) {
    return new WTouchEvent(jsEvent);
  }

  static WTouchEvent templateEvent = new WTouchEvent();

  WTouchEvent(final JavaScriptEvent jsEvent) {
    super();
    this.jsEvent_ = jsEvent;
  }

  JavaScriptEvent jsEvent_;

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
