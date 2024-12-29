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
 * A linear or radial gradient.
 *
 * <p>
 *
 * @see WPen#setGradient(WGradient gradient)
 * @see WBrush#setGradient(WGradient gradient)
 */
public class WGradient {
  private static Logger logger = LoggerFactory.getLogger(WGradient.class);

  /**
   * A gradient color stop.
   *
   * <p>A color stop is defined by a color and a (relative) position. The interpretation of the
   * position depends on the gradient style.
   *
   * <p>
   *
   * @see WGradient#addColorStop(double position, WColor color)
   */
  public static class ColorStop {
    private static Logger logger = LoggerFactory.getLogger(ColorStop.class);

    /** Constructor. */
    public ColorStop(double position, final WColor color) {
      this.position_ = position;
      this.color_ = color;
    }
    /** Returns the position. */
    public double getPosition() {
      return this.position_;
    }
    /** Returns the color. */
    public WColor getColor() {
      return this.color_;
    }
    /** Indicates whether some other object is "equal to" this one. */
    public boolean equals(final WGradient.ColorStop other) {
      return this.position_ == other.position_ && this.color_.equals(other.color_);
    }

    private double position_;
    private WColor color_;
  }
  /**
   * Default constructor.
   *
   * <p>Creates an empty linear gradient from (0,0) to (1,1) (without color stops).
   *
   * <p>
   *
   * @see WGradient#isEmpty()
   */
  public WGradient() {
    this.style_ = GradientStyle.Linear;
    this.colorstops_ = new ArrayList<WGradient.ColorStop>();
    this.gradientVector_ = new WLineF(0, 0, 1, 1);
    this.center_ = new WPointF(0, 0);
    this.focal_ = new WPointF(0, 0);
    this.radius_ = 1;
  }
  /**
   * Returns the gradient style.
   *
   * <p>
   *
   * @see WGradient#setLinearGradient(double x0, double y0, double x1, double y1)
   * @see WGradient#setRadialGradient(double cx, double cy, double r, double fx, double fy)
   */
  public GradientStyle getStyle() {
    return this.style_;
  }
  /**
   * Returns whether the gradient is empty.
   *
   * <p>A gradient is empty if no color stops are defined.
   */
  public boolean isEmpty() {
    return this.colorstops_.isEmpty();
  }
  /**
   * Configures a linear gradient.
   *
   * <p>The coordinates describe a line which provides an origin and orientation of the gradient in
   * user-space coordinates.
   */
  public void setLinearGradient(double x0, double y0, double x1, double y1) {
    this.style_ = GradientStyle.Linear;
    this.gradientVector_ = new WLineF(x0, y0, x1, y1);
  }
  /**
   * Configures a radial gradient.
   *
   * <p>A radial gradient is described by a center, a radial and a focus point. All coordinates are
   * user-space coordinates.
   */
  public void setRadialGradient(double cx, double cy, double r, double fx, double fy) {
    this.style_ = GradientStyle.Radial;
    this.center_ = new WPointF(cx, cy);
    this.focal_ = new WPointF(fx, fy);
    this.radius_ = r;
  }
  /**
   * Adds a color stop.
   *
   * <p>For a linear gradient, the position is relative to the position on the line (from 0 to 1
   * corresponding to p0 to p1).
   *
   * <p>For a radial gradient, the position indicates the distance from the center (from 0 to 1
   * corresponding to center to radius).
   */
  public void addColorStop(double position, final WColor color) {
    this.addColorStop(new WGradient.ColorStop(position, color));
  }
  /**
   * Adds a color stop.
   *
   * <p>Adds a color stop.
   */
  public void addColorStop(final WGradient.ColorStop colorstop) {
    for (int i = 0; i < this.colorstops_.size(); ++i) {
      if (colorstop.getPosition() < this.colorstops_.get(i).getPosition()) {
        this.colorstops_.add(0 + i, colorstop);
        return;
      }
    }
    this.colorstops_.add(colorstop);
  }
  /**
   * Removes all color stops.
   *
   * <p>
   *
   * @see WGradient#addColorStop(double position, WColor color)
   */
  public void clearColorStops() {
    this.colorstops_.clear();
  }
  /**
   * Returns the color stops.
   *
   * <p>
   *
   * @see WGradient#addColorStop(double position, WColor color)
   */
  public List<WGradient.ColorStop> getColorstops() {
    return this.colorstops_;
  }
  /**
   * Returns the line positioning the linear gradient.
   *
   * <p>This returns the line set in {@link WGradient#setLinearGradient(double x0, double y0, double
   * x1, double y1) setLinearGradient()}.
   */
  public WLineF getLinearGradientVector() {
    return this.gradientVector_;
  }
  /**
   * Returns the center of a radial gradient.
   *
   * <p>This returns the center point set in {@link WGradient#setRadialGradient(double cx, double
   * cy, double r, double fx, double fy) setRadialGradient()}.
   */
  public WPointF getRadialCenterPoint() {
    return this.center_;
  }
  /**
   * Returns the focal point of a radial gradient.
   *
   * <p>This returns the focal point set in {@link WGradient#setRadialGradient(double cx, double cy,
   * double r, double fx, double fy) setRadialGradient()}.
   */
  public WPointF getRadialFocalPoint() {
    return this.focal_;
  }
  /**
   * Returns the radius of a radial gradient.
   *
   * <p>This returns the radius set in {@link WGradient#setRadialGradient(double cx, double cy,
   * double r, double fx, double fy) setRadialGradient()}.
   */
  public double getRadialRadius() {
    return this.radius_;
  }
  /** Indicates whether some other object is "equal to" this one. */
  public boolean equals(final WGradient other) {
    if (this.style_ != other.style_) {
      return false;
    }
    if (this.colorstops_.size() != other.colorstops_.size()) {
      return false;
    }
    for (int i = 0; i < this.colorstops_.size(); i++) {
      if (!this.colorstops_.get(i).equals(other.colorstops_.get(i))) {
        return false;
      }
    }
    if (this.style_ == GradientStyle.Linear) {
      return this.gradientVector_.equals(other.gradientVector_);
    } else {
      if (this.style_ == GradientStyle.Radial) {
        return this.center_.equals(other.center_)
            && this.focal_.equals(other.focal_)
            && this.radius_ == other.radius_;
      } else {
        return false;
      }
    }
  }

  private GradientStyle style_;
  private List<WGradient.ColorStop> colorstops_;
  private WLineF gradientVector_;
  private WPointF center_;
  private WPointF focal_;
  private double radius_;
}
