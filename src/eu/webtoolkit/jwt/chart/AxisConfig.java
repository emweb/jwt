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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Axis configuration.
 *
 * <p>This describes the configuration of an axis determining how it should be drawn. To be passed
 * into getLabelTicks().
 */
public class AxisConfig {
  private static Logger logger = LoggerFactory.getLogger(AxisConfig.class);

  public AxisConfig() {
    this.side = AxisValue.Minimum;
    this.zoomLevel = 1;
  }
  /** The side the axis is drawn on, should be Minimum, Maximum, or Zero. */
  public AxisValue side;
  /**
   * The zoom level.
   *
   * <p>Different axis labels can be drawn depending on the zoom level. These are requested by
   * {@link eu.webtoolkit.jwt.chart.WCartesianChart} in powers of 2 (1, 2, 4, 8,...)
   */
  public int zoomLevel;

  private static final int AUTO_V_LABEL_PIXELS = 25;
  private static final int AUTO_H_LABEL_PIXELS = 80;

  static boolean isfin(double d) {
    return -Double.POSITIVE_INFINITY < d && d < Double.POSITIVE_INFINITY;
  }

  static double round125(double v) {
    double n = Math.pow(10, Math.floor(Math.log10(v)));
    double msd = v / n;
    if (msd < 1.5) {
      return n;
    } else {
      if (msd < 3.3) {
        return 2 * n;
      } else {
        if (msd < 7) {
          return 5 * n;
        } else {
          return 10 * n;
        }
      }
    }
  }

  static double roundUp125(double v, double t) {
    return t * Math.ceil((v - 1E-10) / t);
  }

  static double roundDown125(double v, double t) {
    return t * Math.floor((v + 1E-10) / t);
  }

  static int roundDown(int v, int factor) {
    return v / factor * factor;
  }

  static int roundUp(int v, int factor) {
    return ((v - 1) / factor + 1) * factor;
  }

  static WPointF interpolate(final WPointF p1, final WPointF p2, double u) {
    double x = p1.getX();
    if (p2.getX() - p1.getX() > 0) {
      x += u;
    } else {
      if (p2.getX() - p1.getX() < 0) {
        x -= u;
      }
    }
    double y = p1.getY();
    if (p2.getY() - p1.getY() > 0) {
      y += u;
    } else {
      if (p2.getY() - p1.getY() < 0) {
        y -= u;
      }
    }
    return new WPointF(x, y);
  }
}
