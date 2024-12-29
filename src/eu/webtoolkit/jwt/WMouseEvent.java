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
 * A class providing details for a mouse event.
 *
 * <p>
 *
 * @see WInteractWidget#clicked()
 * @see WInteractWidget#doubleClicked()
 * @see WInteractWidget#mouseWentDown()
 * @see WInteractWidget#mouseWentUp()
 * @see WInteractWidget#mouseWentOver()
 * @see WInteractWidget#mouseMoved()
 */
public class WMouseEvent implements WAbstractEvent {
  private static Logger logger = LoggerFactory.getLogger(WMouseEvent.class);

  /** Default constructor. */
  public WMouseEvent() {
    super();
    this.jsEvent_ = new JavaScriptEvent();
  }
  /**
   * Returns the button.
   *
   * <p>If multiple buttons are currently pressed for a mouse moved or mouse dragged event, then the
   * one with the smallest numerical value is returned.
   */
  public MouseButton getButton() {
    switch (this.jsEvent_.button) {
      case 1:
        return MouseButton.Left;
      case 2:
        return MouseButton.Middle;
      case 4:
        return MouseButton.Right;
      default:
        return MouseButton.None;
    }
  }
  /**
   * Returns keyboard modifiers.
   *
   * <p>The result is a logical OR of {@link KeyboardModifier} flags.
   */
  public EnumSet<KeyboardModifier> getModifiers() {
    return this.jsEvent_.modifiers;
  }
  /** Returns the mouse position relative to the document. */
  public Coordinates getDocument() {
    return new Coordinates(this.jsEvent_.documentX, this.jsEvent_.documentY);
  }
  /**
   * Returns the mouse position relative to the window.
   *
   * <p>This differs from documentX() only through scrolling through the document.
   */
  public Coordinates getWindow() {
    return new Coordinates(this.jsEvent_.clientX, this.jsEvent_.clientY);
  }
  /** Returns the mouse position relative to the screen. */
  public Coordinates getScreen() {
    return new Coordinates(this.jsEvent_.screenX, this.jsEvent_.screenY);
  }
  /** Returns the mouse position relative to the widget. */
  public Coordinates getWidget() {
    return new Coordinates(this.jsEvent_.widgetX, this.jsEvent_.widgetY);
  }
  /**
   * Returns the distance over which the mouse has been dragged.
   *
   * <p>This is only defined for a {@link WInteractWidget#mouseWentUp()} event.
   */
  public Coordinates getDragDelta() {
    return new Coordinates(this.jsEvent_.dragDX, this.jsEvent_.dragDY);
  }
  /**
   * Returns the scroll wheel delta.
   *
   * <p>This is 1 when wheel was scrolled up or -1 when wheel was scrolled down.
   *
   * <p>This is only defined for a {@link WInteractWidget#mouseWheel()} event.
   */
  public int getWheelDelta() {
    return this.jsEvent_.wheelDelta;
  }

  public WAbstractEvent createFromJSEvent(final JavaScriptEvent jsEvent) {
    return new WMouseEvent(jsEvent);
  }

  static WMouseEvent templateEvent = new WMouseEvent();

  WMouseEvent(final JavaScriptEvent jsEvent) {
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
