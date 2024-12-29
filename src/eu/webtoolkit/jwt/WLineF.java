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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class that defines a single line. */
public class WLineF {
  private static Logger logger = LoggerFactory.getLogger(WLineF.class);

  /**
   * Default constructor.
   *
   * <p>Constructs a line from (<i>x1=0</i>,<i>y1=0</i>) to (<i>x2=0</i>,<code>y2=0</code>).
   */
  public WLineF() {
    this.x1_ = 0;
    this.y1_ = 0;
    this.x2_ = 0;
    this.y2_ = 0;
  }
  /**
   * Construct a line connecting two points.
   *
   * <p>Constructs a line from <i>p1</i> to <code>p2</code>.
   */
  public WLineF(final WPointF p1, final WPointF p2) {
    this.x1_ = p1.getX();
    this.y1_ = p1.getY();
    this.x2_ = p2.getX();
    this.y2_ = p2.getY();
  }
  /**
   * Construct a line connecting two points.
   *
   * <p>Constructs a line from (<i>x1</i>,<i>y1</i>) to (<i>x2</i>,<code>y2</code>).
   */
  public WLineF(double x1, double y1, double x2, double y2) {
    this.x1_ = x1;
    this.y1_ = y1;
    this.x2_ = x2;
    this.y2_ = y2;
  }
  /**
   * Returns the X coordinate of the first point.
   *
   * <p>
   *
   * @see WLineF#getY1()
   * @see WLineF#getP1()
   */
  public double getX1() {
    return this.x1_;
  }
  /**
   * Returns the Y coordinate of the first point.
   *
   * <p>
   *
   * @see WLineF#getX1()
   * @see WLineF#getP1()
   */
  public double getY1() {
    return this.y1_;
  }
  /**
   * Returns the X coordinate of the second point.
   *
   * <p>
   *
   * @see WLineF#getY2()
   * @see WLineF#getP2()
   */
  public double getX2() {
    return this.x2_;
  }
  /**
   * Returns the Y coordinate of the second point.
   *
   * <p>
   *
   * @see WLineF#getX2()
   * @see WLineF#getP2()
   */
  public double getY2() {
    return this.y2_;
  }
  /**
   * Returns the first point.
   *
   * <p>
   *
   * @see WLineF#getX1()
   * @see WLineF#getY1()
   */
  public WPointF getP1() {
    return new WPointF(this.x1_, this.y1_);
  }
  /**
   * Returns the second point.
   *
   * <p>
   *
   * @see WLineF#getX2()
   * @see WLineF#getY2()
   */
  public WPointF getP2() {
    return new WPointF(this.x2_, this.y2_);
  }

  public boolean equals(final WLineF other) {
    return this.x1_ == other.x1_
        && this.y1_ == other.y1_
        && this.x2_ == other.x2_
        && this.y2_ == other.y2_;
  }

  private double x1_;
  private double y1_;
  private double x2_;
  private double y2_;
}
