/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
 * Enumeration that specifies a type of point marker.
 *
 * <p>
 *
 * @see WDataSeries#setMarker(MarkerType marker)
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
 */
public enum MarkerType {
  /** Do not draw point markers. */
  None,
  /** Mark points using a square. */
  Square,
  /** Mark points using a circle. */
  Circle,
  /** Mark points using a cross (+). */
  Cross,
  /** Mark points using a cross (x). */
  XCross,
  /** Mark points using a triangle. */
  Triangle,
  /** Mark points using a custom marker. */
  Custom,
  /** Mark points using a star. */
  Star,
  /** Mark points using an inverted (upside-down) triangle. */
  InvertedTriangle,
  /** Mark points using an asterisk (*). */
  Asterisk,
  /** Mark points using a diamond. */
  Diamond;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
