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
 * Enumeration that indicates a logical location for an axis.
 *
 * <p>The location is dependent on the values of the other axis.
 *
 * <p>
 *
 * @see WAxis#setLocation(AxisValue location)
 */
public enum AxisValue {
  /** At the minimum value. */
  MinimumValue,
  /** At the maximum value. */
  MaximumValue,
  /** At the zero value (if displayed). */
  ZeroValue,
  /** At both sides (MinimumValue and MaximumValue). */
  BothSides;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
