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
 * Enumeration that indicates which way the axis ticks point.
 *
 * <p>
 *
 * @see WAxis#setTickDirection(TickDirection direction)
 */
public enum TickDirection {
  /** Towards of the outside of the chart. */
  Outwards,
  /** Pointing inwards to the chart. */
  Inwards;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
