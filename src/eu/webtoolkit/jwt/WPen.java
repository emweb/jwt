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
 * A value class that defines the style for pen strokes.
 *
 * <p>A pen defines the properties of how lines (that may surround shapes) are rendered.
 *
 * <p>A pen with width 0 is a <i>cosmetic</i> pen, and is always rendered as 1 pixel width,
 * regardless of transformations. Otherwized, the pen width is modified by the {@link
 * WPainter#getWorldTransform() transformation} set on the painter.
 *
 * <p>
 *
 * <h3>JavaScript exposability</h3>
 *
 * <p>A WPen is JavaScript exposable. If a WPen {@link
 * WJavaScriptExposableObject#isJavaScriptBound() is JavaScript bound}, it can be accessed in your
 * custom JavaScript code through {@link WJavaScriptHandle#getJsRef() its handle&apos;s jsRef()}. At
 * the moment, only the {@link WPen#getColor() getColor()} property is exposed, e.g. a pen with the
 * color WColor(10,20,30,255) will be represented in JavaScript as:
 *
 * <pre>{@code
 * {
 * color: [10,20,30,255]
 * }
 *
 * }</pre>
 *
 * <p>
 *
 * <p><i><b>Warning: </b>A WPen that is JavaScript exposed should be modified only through its
 * {@link WJavaScriptHandle handle}. Any attempt at modifying it will cause an exception to be
 * thrown. </i>
 *
 * @see WPainter#setPen(WPen p)
 * @see WBrush
 * @see WPaintedWidget#createJSPen()
 */
public class WPen extends WJavaScriptExposableObject {
  private static Logger logger = LoggerFactory.getLogger(WPen.class);

  /**
   * Creates a black cosmetic pen.
   *
   * <p>Constructs a black solid pen of 0 width (i.e. cosmetic single pixel width), with
   * PenCapStyle::Square line ends and PenJoinStyle::Bevel line join style.
   */
  public WPen() {
    super();
    this.penStyle_ = PenStyle.SolidLine;
    this.penCapStyle_ = PenCapStyle.Square;
    this.penJoinStyle_ = PenJoinStyle.Bevel;
    this.width_ = new WLength(0);
    this.color_ = new WColor(StandardColor.Black);
    this.gradient_ = new WGradient();
  }
  /**
   * Creates a black pen with a particular style.
   *
   * <p>Constructs a black pen of 0 width (i.e. cosmetic single pixel width), with
   * PenCapStyle::Square line ends and PenJoinStyle::Bevel line join style.
   *
   * <p>The line style is set to <code>style</code>.
   */
  public WPen(PenStyle style) {
    super();
    this.penStyle_ = style;
    this.penCapStyle_ = PenCapStyle.Square;
    this.penJoinStyle_ = PenJoinStyle.Bevel;
    this.width_ = new WLength(0);
    this.color_ = new WColor(StandardColor.Black);
    this.gradient_ = new WGradient();
  }
  /**
   * Creates a solid pen of a particular color.
   *
   * <p>Constructs a solid pen of 0 width (i.e. cosmetic single pixel width), with
   * PenCapStyle::Square line ends and PenJoinStyle::Bevel line join style.
   *
   * <p>The pen color is set to <code>color</code>.
   */
  public WPen(final WColor color) {
    super();
    this.penStyle_ = PenStyle.SolidLine;
    this.penCapStyle_ = PenCapStyle.Square;
    this.penJoinStyle_ = PenJoinStyle.Bevel;
    this.width_ = new WLength(0);
    this.color_ = color;
    this.gradient_ = new WGradient();
  }
  /**
   * Creates a solid pen of a standard color.
   *
   * <p>Constructs a solid pen of 0 width (i.e. cosmetic single pixel width), with
   * PenCapStyle::Square line ends and PenJoinStyle::Bevel line join style.
   *
   * <p>The pen color is set to <code>color</code>.
   */
  public WPen(StandardColor color) {
    super();
    this.penStyle_ = PenStyle.SolidLine;
    this.penCapStyle_ = PenCapStyle.Square;
    this.penJoinStyle_ = PenJoinStyle.Bevel;
    this.width_ = new WLength(0);
    this.color_ = new WColor(color);
    this.gradient_ = new WGradient();
  }
  /**
   * Creates a solid pen with a gradient color.
   *
   * <p>Constructs a solid pen of 0 width (i.e. cosmetic single pixel width), with
   * PenCapStyle::Square line ends and PenJoinStyle::Bevel line join style.
   *
   * <p>The pen&apos;s color is defined by the gradient <code>color</code>.
   */
  public WPen(final WGradient gradient) {
    super();
    this.penStyle_ = PenStyle.SolidLine;
    this.penCapStyle_ = PenCapStyle.Square;
    this.penJoinStyle_ = PenJoinStyle.Bevel;
    this.width_ = new WLength(0);
    this.color_ = new WColor(StandardColor.Black);
    this.gradient_ = new WGradient();
    this.gradient_ = gradient;
  }
  /** Copy constructor. */
  public WPen(final WPen other) {
    super(other);
    this.penStyle_ = other.penStyle_;
    this.penCapStyle_ = other.penCapStyle_;
    this.penJoinStyle_ = other.penJoinStyle_;
    this.width_ = other.width_;
    this.color_ = other.color_;
    this.gradient_ = new WGradient();
    if (other.isJavaScriptBound()) {
      this.assignBinding(other);
    }
  }
  // public void  destroy() ;
  /**
   * Clone method.
   *
   * <p>Clones this pen.
   */
  public WPen clone() {
    WPen result = new WPen();
    if (this.isJavaScriptBound()) {
      result.assignBinding(this);
    }
    result.penStyle_ = this.penStyle_;
    result.penCapStyle_ = this.penCapStyle_;
    result.penJoinStyle_ = this.penJoinStyle_;
    result.width_ = this.width_;
    result.color_ =
        new WColor(
            this.color_.getRed(),
            this.color_.getGreen(),
            this.color_.getBlue(),
            this.color_.getAlpha());
    return result;
  }
  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * <p>Returns <code>true</code> if the pens are exactly the same.
   */
  public boolean equals(final WPen other) {
    return this.sameBindingAs(other)
        && this.penStyle_ == other.penStyle_
        && this.penCapStyle_ == other.penCapStyle_
        && this.penJoinStyle_ == other.penJoinStyle_
        && this.width_.equals(other.width_)
        && this.color_.equals(other.color_)
        && this.gradient_.equals(other.gradient_);
  }
  /**
   * Sets the pen style.
   *
   * <p>The pen style determines the pattern with which the pen is rendered.
   *
   * <p>
   *
   * @exception {@link WException} if the pen {@link WJavaScriptExposableObject#isJavaScriptBound()
   *     is JavaScript bound}
   */
  public void setStyle(PenStyle style) {
    this.checkModifiable();
    this.penStyle_ = style;
  }
  /**
   * Returns the pen style.
   *
   * <p>
   *
   * @see WPen#setStyle(PenStyle style)
   */
  public PenStyle getStyle() {
    return this.penStyle_;
  }
  /**
   * Sets the style for rendering line ends.
   *
   * <p>The cap style configures how line ends are rendered.
   *
   * <p>
   *
   * @exception {@link WException} if the pen {@link WJavaScriptExposableObject#isJavaScriptBound()
   *     is JavaScript bound}
   */
  public void setCapStyle(PenCapStyle style) {
    this.checkModifiable();
    this.penCapStyle_ = style;
  }
  /**
   * Returns the style for rendering line ends.
   *
   * <p>
   *
   * @see WPen#setCapStyle(PenCapStyle style)
   */
  public PenCapStyle getCapStyle() {
    return this.penCapStyle_;
  }
  /**
   * Sets the style for rendering line joins.
   *
   * <p>The join style configures how corners are rendered between different segments of a
   * poly-line, rectange or painter path.
   *
   * <p>
   *
   * @exception {@link WException} if the pen {@link WJavaScriptExposableObject#isJavaScriptBound()
   *     is JavaScript bound}
   */
  public void setJoinStyle(PenJoinStyle style) {
    this.checkModifiable();
    this.penJoinStyle_ = style;
  }
  /**
   * Returns the style for rendering line joins.
   *
   * <p>
   *
   * @see WPen#setJoinStyle(PenJoinStyle style)
   */
  public PenJoinStyle getJoinStyle() {
    return this.penJoinStyle_;
  }
  /**
   * Sets the pen width.
   *
   * <p>A pen width <code>must</code> be specified using {@link LengthUnit#Pixel} units.
   *
   * <p>
   *
   * @exception {@link WException} if the pen {@link WJavaScriptExposableObject#isJavaScriptBound()
   *     is JavaScript bound}
   */
  public void setWidth(final WLength width) {
    this.checkModifiable();
    this.width_ = width;
  }
  /**
   * Returns the pen width.
   *
   * <p>
   *
   * @see WPen#setWidth(WLength width)
   */
  public WLength getWidth() {
    return this.width_;
  }
  /**
   * Sets the pen color.
   *
   * <p>
   *
   * @exception {@link WException} if the pen {@link WJavaScriptExposableObject#isJavaScriptBound()
   *     is JavaScript bound}
   *     <p><i>{@link WPen#setGradient(WGradient gradient) setGradient()}</i>
   */
  public void setColor(final WColor color) {
    this.checkModifiable();
    this.color_ = color;
    this.gradient_ = new WGradient();
  }
  /**
   * Returns the pen color.
   *
   * <p>
   *
   * @see WPen#setColor(WColor color)
   */
  public WColor getColor() {
    return this.color_;
  }
  /**
   * Sets the pen color as a gradient.
   *
   * <p>
   *
   * @exception {@link WException} if the pen {@link WJavaScriptExposableObject#isJavaScriptBound()
   *     is JavaScript bound}
   * @see WPen#setColor(WColor color)
   */
  public void setGradient(final WGradient gradient) {
    this.checkModifiable();
    this.gradient_ = gradient;
  }
  /**
   * Returns the pen color gradient.
   *
   * <p>
   *
   * @see WPen#setGradient(WGradient gradient)
   */
  public WGradient getGradient() {
    return this.gradient_;
  }

  public String getJsValue() {
    StringBuilder ss = new StringBuilder();
    ss.append("{\"color\":[")
        .append(this.color_.getRed())
        .append(",")
        .append(this.color_.getGreen())
        .append(",")
        .append(this.color_.getBlue())
        .append(",")
        .append(this.color_.getAlpha())
        .append("]}");
    return ss.toString();
  }

  protected void assignFromJSON(final com.google.gson.JsonElement value) {
    try {
      final com.google.gson.JsonObject o = (com.google.gson.JsonObject) value;
      final com.google.gson.JsonElement color = o.get("color");
      final com.google.gson.JsonArray col = (com.google.gson.JsonArray) color;
      if (col.size() == 4
          && !JsonUtils.isNull(JsonUtils.toNumber(col.get(0)))
          && !JsonUtils.isNull(JsonUtils.toNumber(col.get(1)))
          && !JsonUtils.isNull(JsonUtils.toNumber(col.get(2)))
          && !JsonUtils.isNull(JsonUtils.toNumber(col.get(3)))) {
        this.color_ =
            new WColor(
                JsonUtils.orIfNullInt(JsonUtils.toNumber(col.get(0)), 0),
                JsonUtils.orIfNullInt(JsonUtils.toNumber(col.get(1)), 0),
                JsonUtils.orIfNullInt(JsonUtils.toNumber(col.get(2)), 0),
                JsonUtils.orIfNullInt(JsonUtils.toNumber(col.get(3)), 255));
      } else {
        logger.error(new StringWriter().append("Couldn't convert JSON to WPen").toString());
      }
    } catch (final RuntimeException e) {
      logger.error(
          new StringWriter().append("Couldn't convert JSON to WPen: " + e.toString()).toString());
    }
  }

  private PenStyle penStyle_;
  private PenCapStyle penCapStyle_;
  private PenJoinStyle penJoinStyle_;
  private WLength width_;
  private WColor color_;
  private WGradient gradient_;
}
