/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration that specifies the type of a chart series.
 *
 * <p>
 *
 * @see WDataSeries#setType(SeriesType type)
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
 */
public enum SeriesType {
  /** Series rendered solely as point markers. */
  PointSeries,
  /** Series rendered as points connected by straight lines. */
  LineSeries,
  /** Series rendered as points connected by curves. */
  CurveSeries,
  /** Series rendered as bars. */
  BarSeries;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
