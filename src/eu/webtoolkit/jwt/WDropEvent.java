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
 * A class providing details for a drop event.
 *
 * <p>
 *
 * @see WWidget#dropEvent(WDropEvent event)
 */
public class WDropEvent {
  private static Logger logger = LoggerFactory.getLogger(WDropEvent.class);

  /** The type of the original event. */
  public enum OriginalEventType {
    /** The original event was a {@link WMouseEvent}. */
    Mouse,
    /** The original event was a {@link WTouchEvent}. */
    Touch;

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return ordinal();
    }
  }
  /** Constructor. */
  public WDropEvent(WObject source, final String mimeType, final WMouseEvent mouseEvent) {
    this.dropSource_ = source;
    this.dropMimeType_ = mimeType;
    this.mouseEvent_ = mouseEvent;
    this.touchEvent_ = (WTouchEvent) null;
  }
  /** Constructor. */
  public WDropEvent(WObject source, final String mimeType, final WTouchEvent touchEvent) {
    this.dropSource_ = source;
    this.dropMimeType_ = mimeType;
    this.mouseEvent_ = (WMouseEvent) null;
    this.touchEvent_ = touchEvent;
  }
  /**
   * Returns the source of the drag&amp;drop operation.
   *
   * <p>The source is the widget that was set draggable using {@link
   * WInteractWidget#setDraggable(String mimeType, WWidget dragWidget, boolean isDragWidgetOnly,
   * WObject sourceObject) WInteractWidget#setDraggable()}.
   */
  public WObject getSource() {
    return this.dropSource_;
  }
  /** Returns the mime type of this drop event. */
  public String getMimeType() {
    return this.dropMimeType_;
  }
  /**
   * Returns the original mouse event.
   *
   * <p>If eventType() == {@link WDropEvent.OriginalEventType#Mouse Mouse}, this returns the
   * original mouse event, otherwise this returns null.
   */
  public WMouseEvent getMouseEvent() {
    return this.mouseEvent_;
  }
  /**
   * Returns the original touch event.
   *
   * <p>If eventType() == {@link WDropEvent.OriginalEventType#Touch Touch}, this returns the
   * original touch event, otherwise this returns null.
   */
  public WTouchEvent getTouchEvent() {
    return this.touchEvent_;
  }
  /** Returns the type of the original event. */
  public WDropEvent.OriginalEventType getOriginalEventType() {
    return this.mouseEvent_ != null
        ? WDropEvent.OriginalEventType.Mouse
        : WDropEvent.OriginalEventType.Touch;
  }

  private WObject dropSource_;
  private String dropMimeType_;
  private final WMouseEvent mouseEvent_;
  private final WTouchEvent touchEvent_;

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
