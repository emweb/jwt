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

/** An enumeration describing a layout direction. */
public enum LayoutDirection {
  /** Left-to-Right layout. */
  LeftToRight,
  /** Right-to-left layout. */
  RightToLeft,
  /** Top-to-bottom layout. */
  TopToBottom,
  /** Bottom-to-top layout. */
  BottomToTop;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
