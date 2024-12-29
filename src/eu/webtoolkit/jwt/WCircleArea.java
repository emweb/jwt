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

/**
 * A interactive area in a widget, specified by a circle.
 *
 * <p>The area may be added to a {@link WImage} or {@link WPaintedWidget} to provide interactivity
 * on a circular area of the image. The circle is specified in pixel coordinates.
 *
 * <p>
 *
 * @see WImage#addArea(WAbstractArea area)
 * @see WPaintedWidget#addArea(WAbstractArea area)
 * @see WRectArea
 * @see WPolygonArea
 */
public class WCircleArea extends WAbstractArea {
  private static Logger logger = LoggerFactory.getLogger(WCircleArea.class);

  /**
   * Default constructor.
   *
   * <p>Specifies a circular area with center (0, 0) and radius 0.
   */
  public WCircleArea() {
    super();
    this.x_ = 0;
    this.y_ = 0;
    this.r_ = 0;
  }
  /**
   * Creates a circular area with given geometry.
   *
   * <p>The arguments are in pixel units.
   */
  public WCircleArea(int x, int y, int radius) {
    super();
    this.x_ = x;
    this.y_ = y;
    this.r_ = radius;
  }
  /** Sets the center. */
  public void setCenter(final WPoint point) {
    this.setCenter(point.getX(), point.getY());
  }
  /** Sets the center. */
  public void setCenter(final WPointF point) {
    this.x_ = point.getX();
    this.y_ = point.getY();
    this.repaint();
  }
  /** Sets the center. */
  public void setCenter(int x, int y) {
    this.x_ = x;
    this.y_ = y;
    this.repaint();
  }
  /** Returns the center X coordinate. */
  public int getCenterX() {
    return (int) this.x_;
  }
  /** Returns the center Y coordinate. */
  public int getCenterY() {
    return (int) this.y_;
  }
  /** Sets the radius. */
  public void setRadius(int radius) {
    this.r_ = radius;
    this.repaint();
  }
  /** Returns the radius. */
  public int getRadius() {
    return (int) this.r_;
  }

  private double x_;
  private double y_;
  private double r_;

  protected boolean updateDom(final DomElement element, boolean all) {
    element.setAttribute("shape", "circle");
    StringWriter coords = new StringWriter();
    coords
        .append(String.valueOf((int) this.x_))
        .append(',')
        .append(String.valueOf((int) this.y_))
        .append(',')
        .append(String.valueOf((int) this.r_));
    element.setAttribute("coords", coords.toString());
    return super.updateDom(element, all);
  }

  protected String getUpdateAreaCoordsJS() {
    StringWriter coords = new StringWriter();
    char[] buf = new char[30];
    coords.append("[").append(this.getJsRef()).append(",[");
    coords.append(MathUtils.roundJs(this.x_, 2)).append(',');
    coords.append(MathUtils.roundJs(this.y_, 2)).append(',');
    coords.append(MathUtils.roundJs(this.r_, 2)).append("]]");
    return coords.toString();
  }
}
