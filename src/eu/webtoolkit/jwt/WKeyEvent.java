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
 * A class providing details for a keyboard event.
 *
 * <p>A key event is associated with the {@link WInteractWidget#keyWentDown()}, {@link
 * WInteractWidget#keyWentUp()} and {@link WInteractWidget#keyPressed()} signals.
 */
public class WKeyEvent implements WAbstractEvent {
  private static Logger logger = LoggerFactory.getLogger(WKeyEvent.class);

  /** Default constructor. */
  public WKeyEvent() {
    super();
    this.jsEvent_ = new JavaScriptEvent();
  }
  /**
   * Returns the key code key that was pressed or released.
   *
   * <p>The key code corresponds to the actual key on the keyboard, rather than the generated
   * character.
   *
   * <p>All three types of key events provide this information.
   *
   * <p>
   *
   * @see WKeyEvent#getModifiers()
   * @see WKeyEvent#getCharCode()
   */
  public Key getKey() {
    int key = this.jsEvent_.keyCode;
    if (key == 0) {
      key = this.jsEvent_.charCode;
    }
    return EnumUtils.keyFromValue(key);
  }
  /**
   * Returns keyboard modifiers.
   *
   * <p>The result is a logical OR of {@link KeyboardModifier} flags.
   *
   * <p>All three types of key events provide this information.
   *
   * <p>
   *
   * @see WKeyEvent#getKey()
   * @see WKeyEvent#getCharCode()
   */
  public EnumSet<KeyboardModifier> getModifiers() {
    return this.jsEvent_.modifiers;
  }
  /**
   * Returns the unicode character code.
   *
   * <p>This is only defined for a {@link WInteractWidget#keyPressed()} event, and returns the
   * unicode character code point of a character that is entered.
   *
   * <p>For the {@link WInteractWidget#keyWentDown()} and {@link WInteractWidget#keyWentUp()}
   * events, &apos;0&apos; is returned.
   *
   * <p>The {@link WKeyEvent#getCharCode() getCharCode()} may be different from {@link
   * WKeyEvent#getKey() getKey()}. For example, a {@link Key#M} key may correspond to &apos;m&apos;
   * or &apos;M&apos; character, depending on whether the shift key is pressed simultaneously.
   *
   * <p>
   *
   * @see WKeyEvent#getKey()
   * @see WKeyEvent#getText()
   */
  public int getCharCode() {
    return this.jsEvent_.charCode;
  }
  /**
   * The (unicode) text that this key generated.
   *
   * <p>This is only defined for a {@link WInteractWidget#keyPressed()} event, and returns a string
   * that holds exactly one unicode character, which corresponds to {@link WKeyEvent#getCharCode()
   * getCharCode()}.
   *
   * <p>For the {@link WInteractWidget#keyWentDown()} and {@link WInteractWidget#keyWentUp()}
   * events, an empty string is returned.
   *
   * <p>
   *
   * @see WKeyEvent#getCharCode()
   */
  public String getText() {
    return "" + (char) this.getCharCode();
  }

  public WAbstractEvent createFromJSEvent(final JavaScriptEvent jsEvent) {
    return new WKeyEvent(jsEvent);
  }

  static WKeyEvent templateEvent = new WKeyEvent();

  WKeyEvent(final JavaScriptEvent jsEvent) {
    super();
    this.jsEvent_ = jsEvent;
  }

  private JavaScriptEvent jsEvent_;

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
