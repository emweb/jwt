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
 * An interactive area in a widget, specified by a polygon.
 *
 * <p>The area may be added to a {@link WImage} or {@link WPaintedWidget} to provide interactivity
 * on a polygon area of the image. The polygon is specified in pixel coordinates, and uses an
 * even-odd winding rule (overlaps create holes).
 *
 * <p>The polygon area corresponds to the HTML <code>&lt;area shape=&quot;poly&quot;&gt;</code> tag.
 *
 * <p>
 *
 * @see WImage#addArea(WAbstractArea area)
 * @see WPaintedWidget#addArea(WAbstractArea area)
 * @see WCircleArea
 * @see WRectArea
 */
public class WPolygonArea extends WAbstractArea {
  private static Logger logger = LoggerFactory.getLogger(WPolygonArea.class);

  /**
   * Creates an empty polygon.
   *
   * <p>Defines an empty polygon.
   */
  public WPolygonArea() {
    super();
    this.points_ = new ArrayList<WPointF>();
    this.pointsIntCompatibility_ = new ArrayList<WPoint>();
  }
  /**
   * Creates a polygon area with given vertices.
   *
   * <p>The polygon is defined with vertices corresponding to <code>points</code>. The polygon is
   * closed by connecting the last point with the first point.
   */
  public WPolygonArea(final List<WPoint> points) {
    super();
    this.points_ = new ArrayList<WPointF>();
    this.pointsIntCompatibility_ = new ArrayList<WPoint>();
    this.setPoints(points);
  }
  /** Adds a point. */
  public void addPoint(int x, int y) {
    this.points_.add(new WPointF(x, y));
    this.repaint();
  }
  /** Adds a point. */
  public void addPoint(double x, double y) {
    this.points_.add(new WPointF(x, y));
    this.repaint();
  }
  /** Adds a point. */
  public void addPoint(final WPoint point) {
    this.points_.add(new WPointF(point.getX(), point.getY()));
    this.repaint();
  }
  /** Adds a point. */
  public void addPoint(final WPointF point) {
    this.points_.add(point);
    this.repaint();
  }
  /**
   * Sets the polygon vertices.
   *
   * <p>The polygon is defined with vertices corresponding to <code>points</code>. The polygon is
   * closed by connecting the last point with the first point.
   */
  public void setPoints(final List<WPoint> points) {
    this.points_.clear();
    for (int i = 0; i < points.size(); ++i) {
      this.addPoint(points.get(i));
    }
    this.repaint();
  }
  /**
   * Returns the polygon vertices.
   *
   * <p>
   *
   * @see WPolygonArea#setPoints(List points)
   * @see WPolygonArea#getPoints()
   */
  public List<WPointF> getPointFs() {
    return this.points_;
  }
  /**
   * Returns the polygon vertices.
   *
   * <p>
   *
   * @see WPolygonArea#setPoints(List points)
   * @see WPolygonArea#getPointFs()
   */
  public List<WPoint> getPoints() {
    this.pointsIntCompatibility_.clear();
    for (int i = 0; i < this.points_.size(); ++i) {
      this.pointsIntCompatibility_.add(
          new WPoint((int) this.points_.get(i).getX(), (int) this.points_.get(i).getY()));
    }
    return this.pointsIntCompatibility_;
  }

  private List<WPointF> points_;
  private List<WPoint> pointsIntCompatibility_;

  protected boolean updateDom(final DomElement element, boolean all) {
    element.setAttribute("shape", "poly");
    StringWriter coords = new StringWriter();
    for (int i = 0; i < this.points_.size(); ++i) {
      if (i != 0) {
        coords.append(',');
      }
      coords
          .append(String.valueOf((int) this.points_.get(i).getX()))
          .append(',')
          .append(String.valueOf((int) this.points_.get(i).getY()));
    }
    element.setAttribute("coords", coords.toString());
    return super.updateDom(element, all);
  }

  protected String getUpdateAreaCoordsJS() {
    StringWriter coords = new StringWriter();
    char[] buf = new char[30];
    coords.append("[").append(this.getJsRef()).append(",[");
    for (int i = 0; i < this.points_.size(); ++i) {
      if (i != 0) {
        coords.append(',');
      }
      coords.append(MathUtils.roundJs(this.points_.get(i).getX(), 2)).append(',');
      coords.append(MathUtils.roundJs(this.points_.get(i).getY(), 2));
    }
    coords.append("]]");
    return coords.toString();
  }
}
