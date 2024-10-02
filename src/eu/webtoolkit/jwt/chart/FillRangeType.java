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
 * Enumeration that specifies how an area should be filled.
 *
 * <p>Data series of type {@link SeriesType#Line} or CurveSerie may be filled under or above the
 * line or curve. This enumeration specifies the other limit of this fill. Data series of type
 * {@link SeriesType#Bar} can use this setting to configure the bottom of the chart.
 *
 * <p>
 *
 * @see WDataSeries#setFillRange(FillRangeType fillRange)
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
 */
public enum FillRangeType {
  /** Do not fill under the curve. */
  None,
  /** Fill from the curve to the chart bottom (min) */
  MinimumValue,
  /** Fill from the curve to the chart top. */
  MaximumValue,
  /** Fill from the curve to the zero Y value. */
  ZeroValue;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
