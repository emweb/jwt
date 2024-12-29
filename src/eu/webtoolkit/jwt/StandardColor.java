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

/** Enumeration for predefined colors. */
public enum StandardColor {
  /** Color white. */
  White,
  /** Color black. */
  Black,
  /** Color red. */
  Red,
  /** Color dark red. */
  DarkRed,
  /** Color green. */
  Green,
  /** Color dark green. */
  DarkGreen,
  /** Color blue. */
  Blue,
  /** Color dark blue. */
  DarkBlue,
  /** Color cyan. */
  Cyan,
  /** Color dark cyan. */
  DarkCyan,
  /** Color magenta. */
  Magenta,
  /** Color dark magenta. */
  DarkMagenta,
  /** Color yellow. */
  Yellow,
  /** Color dark yellow. */
  DarkYellow,
  /** Color medium gray. */
  Gray,
  /** Color dark gray. */
  DarkGray,
  /** Color light gray. */
  LightGray,
  /** Color transparent. */
  Transparent;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
