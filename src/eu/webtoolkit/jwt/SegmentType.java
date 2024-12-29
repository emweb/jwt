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

/** The segment type */
public enum SegmentType {
  /** moveTo segment */
  MoveTo(0),
  /** lineTo segment */
  LineTo(1),
  /**
   * first control point of cubic bezier curve.
   *
   * <p>Always followed by a CubicC2 and CubicEnd segment
   */
  CubicC1(2),
  /**
   * second control point of cubic bezier curve
   *
   * <p>Always followed by a CubicEnd segment
   */
  CubicC2(3),
  /** end point of cubic bezier curve */
  CubicEnd(4),
  /** control point of quadratic bezier curve */
  QuadC(5),
  /** end point of quadratic bezier curve */
  QuadEnd(6),
  /**
   * center of an arc
   *
   * <p>Always followed by an ArcR and ArcAngleSweep segment
   */
  ArcC(7),
  /** radius of an arc Always followed by an ArcAngleSweep segment */
  ArcR(8),
  /**
   * the sweep of an arc
   *
   * <p>x = startAngle, y = spanAngle
   */
  ArcAngleSweep(9);

  private int value;

  SegmentType(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
