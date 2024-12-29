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

/**
 * Enumeration for keyboard modifiers.
 *
 * <p>
 *
 * @see WMouseEvent#getModifiers()
 * @see WKeyEvent#getModifiers()
 */
public enum KeyboardModifier {
  /** No modifiers. */
  None,
  /** Shift key pressed. */
  Shift,
  /** Control key pressed. */
  Control,
  /** Alt key pressed. */
  Alt,
  /** Meta key pressed (&quot;Windows&quot; or &quot;Command&quot; (Mac) key) */
  Meta;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
