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

/** Enumeration that indicates a pen style. */
public enum PenStyle {
  /** Do not stroke. */
  None,
  /** Stroke with a solid line. */
  SolidLine,
  /** Stroked with a dashed line. */
  DashLine,
  /** Stroke with a dotted line. */
  DotLine,
  /** Stroke with a dash dot line. */
  DashDotLine,
  /** Stroke with a dash dot dot line. */
  DashDotDotLine;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
