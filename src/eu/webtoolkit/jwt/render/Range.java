/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

import eu.webtoolkit.jwt.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Range {
  private static Logger logger = LoggerFactory.getLogger(Range.class);

  public Range(double start, double end) {
    this.start = start;
    this.end = end;
  }

  public double start;
  public double end;
  private static final double MARGINX = -1;
  private static final double EPSILON = 1e-4;

  static boolean isEpsilonMore(double x, double limit) {
    return x - EPSILON > limit;
  }

  static boolean isEpsilonLess(double x, double limit) {
    return x + EPSILON < limit;
  }
}
