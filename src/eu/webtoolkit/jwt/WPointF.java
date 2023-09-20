/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

/**
 * A value class that defines a 2D point.
 *
 * <p>
 *
 * <h3>JavaScript exposability</h3>
 *
 * <p>A WPointF is JavaScript exposable. If a WPointF {@link
 * WJavaScriptExposableObject#isJavaScriptBound() is JavaScript bound}, it can be accessed in your
 * custom JavaScript code through {@link WJavaScriptHandle#getJsRef() its handle&apos;s jsRef()}. A
 * point is represented in JavaScript as an array of two elements, e.g. a point WPointF(10,20) will
 * be represented in JavaScript as:
 *
 * <pre>{@code
 * [10, 20]
 *
 * }</pre>
 *
 * <p>
 *
 * <p><i><b>Warning: </b>A WPointF that is JavaScript exposed should be modified only through its
 * {@link WJavaScriptHandle handle}. Any attempt at modifying it will cause an exception to be
 * thrown. </i>
 *
 * @see WPaintedWidget#createJSPoint()
 */
public class WPointF extends WJavaScriptExposableObject {
  private static Logger logger = LoggerFactory.getLogger(WPointF.class);

  /** Creates point (0, 0). */
  public WPointF() {
    super();
    this.x_ = 0;
    this.y_ = 0;
  }
  /** Creates a point (x, y). */
  public WPointF(double x, double y) {
    super();
    this.x_ = x;
    this.y_ = y;
  }
  /** Copy constructor. */
  public WPointF(final WPointF other) {
    super(other);
    this.x_ = other.getX();
    this.y_ = other.getY();
  }
  /** Creates a point from mouse coordinates. */
  public WPointF(final Coordinates other) {
    super();
    this.x_ = other.x;
    this.y_ = other.y;
  }

  public WPointF clone() {
    return new WPointF(this);
  }
  /**
   * Sets the X coordinate.
   *
   * <p>
   *
   * @exception {@link WException} if the point {@link
   *     WJavaScriptExposableObject#isJavaScriptBound() is JavaScript bound}
   */
  public void setX(double x) {
    this.checkModifiable();
    this.x_ = x;
  }
  /**
   * Sets the Y coordinate.
   *
   * <p>
   *
   * @exception {@link WException} if the point {@link
   *     WJavaScriptExposableObject#isJavaScriptBound() is JavaScript bound}
   */
  public void setY(double y) {
    this.checkModifiable();
    this.y_ = y;
  }
  /** Returns the X coordinate. */
  public double getX() {
    return this.x_;
  }
  /** Returns the Y coordinate. */
  public double getY() {
    return this.y_;
  }
  /** Indicates whether some other object is "equal to" this one. */
  public boolean equals(final WPointF other) {
    if (!this.sameBindingAs(other)) {
      return false;
    }
    return this.x_ == other.x_ && this.y_ == other.y_;
  }

  WPointF add(final WPointF other) {
    this.checkModifiable();
    this.x_ += other.x_;
    this.y_ += other.y_;
    return this;
  }

  public String getJsValue() {
    char[] buf = new char[30];
    StringBuilder ss = new StringBuilder();
    ss.append('[');
    ss.append(MathUtils.roundJs(this.x_, 3)).append(',');
    ss.append(MathUtils.roundJs(this.y_, 3)).append(']');
    return ss.toString();
  }

  public WPointF swapHV(double width) {
    WPointF result = new WPointF(width - this.getY(), this.getX());
    if (this.isJavaScriptBound()) {
      StringBuilder ss = new StringBuilder();
      char[] buf = new char[30];
      ss.append("((function(p){return [");
      ss.append(MathUtils.roundJs(width, 3))
          .append("-p[1],p[0]];})(")
          .append(this.getJsRef() + "))");
      result.assignBinding(this, ss.toString());
    }
    return result;
  }

  public WPointF inverseSwapHV(double width) {
    WPointF result = new WPointF(this.getY(), width - this.getX());
    if (this.isJavaScriptBound()) {
      StringBuilder ss = new StringBuilder();
      char[] buf = new char[30];
      ss.append("((function(p){return [");
      ss.append("p[1],")
          .append(MathUtils.roundJs(width, 3))
          .append("-p[0]];})(")
          .append(this.getJsRef() + "))");
      result.assignBinding(this, ss.toString());
    }
    return result;
  }

  protected void assignFromJSON(final com.google.gson.JsonElement value) {
    try {
      final com.google.gson.JsonArray ar = (com.google.gson.JsonArray) value;
      if (ar.size() == 2
          && !JsonUtils.isNull(JsonUtils.toNumber(ar.get(0)))
          && !JsonUtils.isNull(JsonUtils.toNumber(ar.get(1)))) {
        this.x_ = JsonUtils.orIfNullDouble(JsonUtils.toNumber(ar.get(0)), this.x_);
        this.y_ = JsonUtils.orIfNullDouble(JsonUtils.toNumber(ar.get(1)), this.y_);
      } else {
        logger.error(new StringWriter().append("Couldn't convert JSON to WPointF").toString());
      }
    } catch (final RuntimeException e) {
      logger.error(
          new StringWriter()
              .append("Couldn't convert JSON to WPointF: " + e.toString())
              .toString());
    }
  }

  private double x_;
  private double y_;
}
