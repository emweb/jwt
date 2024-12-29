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
 * A interactive area in a widget, specified by a rectangle.
 *
 * <p>The area may be added to a {@link WImage} or {@link WPaintedWidget} to provide interactivity
 * on a rectangular area of the image. The rectangle is specified in pixel coordinates.
 *
 * <p>
 *
 * @see WImage#addArea(WAbstractArea area)
 * @see WPaintedWidget#addArea(WAbstractArea area)
 * @see WCircleArea
 * @see WPolygonArea
 */
public class WRectArea extends WAbstractArea {
  private static Logger logger = LoggerFactory.getLogger(WRectArea.class);

  /**
   * Default constructor.
   *
   * <p>The default constructor creates a rectangular area spans the whole widget.
   */
  public WRectArea() {
    super();
    this.x_ = 0;
    this.y_ = 0;
    this.width_ = 0;
    this.height_ = 0;
  }
  /**
   * Creates a rectangular area with given geometry.
   *
   * <p>The arguments are in pixel units.
   */
  public WRectArea(int x, int y, int width, int height) {
    super();
    this.x_ = x;
    this.y_ = y;
    this.width_ = width;
    this.height_ = height;
  }
  /**
   * Creates a rectangular area with given geometry.
   *
   * <p>The arguments are in pixel units.
   */
  public WRectArea(double x, double y, double width, double height) {
    super();
    this.x_ = x;
    this.y_ = y;
    this.width_ = width;
    this.height_ = height;
  }
  /**
   * Creates a rectangular area with given geometry.
   *
   * <p>The <code>rect</code> argument is in pixel units.
   */
  public WRectArea(final WRectF rect) {
    super();
    this.x_ = rect.getX();
    this.y_ = rect.getY();
    this.width_ = rect.getWidth();
    this.height_ = rect.getHeight();
  }
  /** Sets the top-left X coordinate. */
  public void setX(int x) {
    this.x_ = x;
    this.repaint();
  }
  /** Returns the top-left X coordinate. */
  public int getX() {
    return (int) this.x_;
  }
  /** Sets the top-left Y coordinate. */
  public void setY(int y) {
    this.y_ = y;
    this.repaint();
  }
  /** Returns the top-left Y coordinate. */
  public int getY() {
    return (int) this.y_;
  }
  /** Sets the width. */
  public void setWidth(int width) {
    this.width_ = width;
    this.repaint();
  }
  /** Returns the width. */
  public int getWidth() {
    return (int) this.width_;
  }
  /** Sets the height. */
  public void setHeight(int height) {
    this.height_ = height;
    this.repaint();
  }
  /** Returns the height. */
  public int getHeight() {
    return (int) this.height_;
  }

  private double x_;
  private double y_;
  private double width_;
  private double height_;

  protected boolean updateDom(final DomElement element, boolean all) {
    element.setAttribute("shape", "rect");
    StringWriter coords = new StringWriter();
    int x = (int) this.x_;
    int y = (int) this.y_;
    int width = (int) this.width_;
    int height = (int) this.height_;
    if (x == 0 && y == 0 && width == 0 && height == 0) {
      coords.append("0%,0%,100%,100%");
    } else {
      coords
          .append(String.valueOf(x))
          .append(',')
          .append(String.valueOf(y))
          .append(',')
          .append(String.valueOf(x + width))
          .append(',')
          .append(String.valueOf(y + height));
    }
    element.setAttribute("coords", coords.toString());
    return super.updateDom(element, all);
  }

  protected String getUpdateAreaCoordsJS() {
    StringWriter coords = new StringWriter();
    char[] buf = new char[30];
    coords.append("[").append(this.getJsRef()).append(",[");
    coords.append(MathUtils.roundJs(this.x_, 2)).append(',');
    coords.append(MathUtils.roundJs(this.y_, 2)).append(',');
    coords.append(MathUtils.roundJs(this.x_ + this.width_, 2)).append(',');
    coords.append(MathUtils.roundJs(this.y_ + this.height_, 2)).append("]]");
    return coords.toString();
  }
}
