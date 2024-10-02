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
 * Enumeration that indicates a scale for an axis.
 *
 * <p>The scale determines how values are mapped onto an axis.
 *
 * <p>
 *
 * @see WAxis#setScale(AxisScale scale)
 */
public enum AxisScale {
  /**
   * A discrete scale is set as the scale for the X axis in a {@link ChartType#Category}, and is
   * only applicable there. It lists all values, evenly spaced, and consecutively in the order of
   * the model. The categories are converted to numbers using their ordinal (first category = 0,
   * second = 1, ...).
   */
  Discrete(0),
  /**
   * A linear scale is the default scale for all axes, except for the X scale in a {@link
   * AxisScale#Discrete}. It maps values in a linear fashion on the axis.
   */
  Linear(1),
  /**
   * A logarithmic scale is useful for plotting values with of a large range, but only works for
   * positive values.
   */
  Log(2),
  /**
   * A date scale is a special linear scale, which is useful for the X axis in a {@link
   * ChartType#Scatter}, when the X series contain dates (of type {@link eu.webtoolkit.jwt.WDate}).
   * The dates are converted to numbers, as Julian Days.
   */
  Date(3),
  /**
   * A datetime scale is a special linear scale, which is useful for the X axis in a {@link
   * ChartType#Scatter}, when the X series contain timedates (of type {@link
   * eu.webtoolkit.jwt.WDate}). The dates are converted to numbers, as the number of seconds since
   * the Unix Epoch (midnight Coordinated Universal Time (UTC) of January 1, 1970).
   */
  DateTime(4);

  private int value;

  AxisScale(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
