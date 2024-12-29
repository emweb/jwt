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

/** A value class that defines the CSS border style of a widget. */
public class WBorder {
  private static Logger logger = LoggerFactory.getLogger(WBorder.class);

  /** Creates a border indicating <i>no border</i>. */
  public WBorder() {
    this.width_ = BorderWidth.Medium;
    this.explicitWidth_ = new WLength();
    this.color_ = new WColor();
    this.style_ = BorderStyle.None;
  }
  /** Creates a border with given style, thickness and color. */
  public WBorder(BorderStyle style, BorderWidth width, WColor color) {
    this.width_ = width;
    this.explicitWidth_ = new WLength();
    this.color_ = color;
    this.style_ = style;
  }
  /**
   * Creates a border with given style, thickness and color.
   *
   * <p>Calls {@link #WBorder(BorderStyle style, BorderWidth width, WColor color) this(style,
   * BorderWidth.Medium, new WColor())}
   */
  public WBorder(BorderStyle style) {
    this(style, BorderWidth.Medium, new WColor());
  }
  /**
   * Creates a border with given style, thickness and color.
   *
   * <p>Calls {@link #WBorder(BorderStyle style, BorderWidth width, WColor color) this(style, width,
   * new WColor())}
   */
  public WBorder(BorderStyle style, BorderWidth width) {
    this(style, width, new WColor());
  }
  /** Creates a border with an absolute width. */
  public WBorder(BorderStyle style, final WLength width, WColor color) {
    this.width_ = BorderWidth.Explicit;
    this.explicitWidth_ = width;
    this.color_ = color;
    this.style_ = style;
  }
  /**
   * Creates a border with an absolute width.
   *
   * <p>Calls {@link #WBorder(BorderStyle style, WLength width, WColor color) this(style, width, new
   * WColor())}
   */
  public WBorder(BorderStyle style, final WLength width) {
    this(style, width, new WColor());
  }
  /** Indicates whether some other object is "equal to" this one. */
  public boolean equals(final WBorder other) {
    return this.width_ == other.width_
        && this.color_.equals(other.color_)
        && this.style_ == other.style_;
  }
  /**
   * Sets the border width.
   *
   * <p>If width == Explicit, then the width specified in <code>explicitWidth</code> is used.
   */
  public void setWidth(BorderWidth width, final WLength explicitWidth) {
    this.width_ = width;
    this.explicitWidth_ = explicitWidth;
  }
  /**
   * Sets the border width.
   *
   * <p>Calls {@link #setWidth(BorderWidth width, WLength explicitWidth) setWidth(width,
   * WLength.Auto)}
   */
  public final void setWidth(BorderWidth width) {
    setWidth(width, WLength.Auto);
  }
  /** Sets the border color. */
  public void setColor(WColor color) {
    this.color_ = color;
  }
  /** Sets the border style. */
  public void setStyle(BorderStyle style) {
    this.style_ = style;
  }
  /**
   * Returns the border width.
   *
   * <p>
   *
   * @see WBorder#setWidth(BorderWidth width, WLength explicitWidth)
   */
  public BorderWidth getWidth() {
    return this.width_;
  }
  /**
   * Returns the border width when set explicitly.
   *
   * <p>
   *
   * @see WBorder#setWidth(BorderWidth width, WLength explicitWidth)
   */
  public WLength getExplicitWidth() {
    return this.explicitWidth_;
  }
  /**
   * Returns the border color.
   *
   * <p>
   *
   * @see WBorder#setColor(WColor color)
   */
  public WColor getColor() {
    return this.color_;
  }
  /**
   * Returns the border style.
   *
   * <p>
   *
   * @see WBorder#setStyle(BorderStyle style)
   */
  public BorderStyle getStyle() {
    return this.style_;
  }
  /** Returns the CSS text for this border style. */
  public String getCssText() {
    String style = "";
    switch (this.style_) {
      case None:
        return "none";
      case Hidden:
        style = "hidden";
        break;
      case Dotted:
        style = "dotted";
        break;
      case Dashed:
        style = "dashed";
        break;
      case Solid:
        style = "solid";
        break;
      case Double:
        style = "double";
        break;
      case Groove:
        style = "groove";
        break;
      case Ridge:
        style = "ridge";
        break;
      case Inset:
        style = "inset";
        break;
      case Outset:
        style = "outset";
        break;
    }
    String width = "";
    switch (this.width_) {
      case Thin:
        width = "thin";
        break;
      case Medium:
        width = "medium";
        break;
      case Thick:
        width = "thick";
        break;
      case Explicit:
        width = this.explicitWidth_.getCssText();
    }
    return width + " " + style + " " + this.color_.getCssText();
  }
  /**
   * Clone method.
   *
   * <p>Clones this border.
   */
  public WBorder clone() {
    WBorder b = new WBorder();
    b.width_ = this.width_;
    b.explicitWidth_ = this.explicitWidth_;
    b.color_ = this.color_;
    b.style_ = this.style_;
    return b;
  }

  private BorderWidth width_;
  private WLength explicitWidth_;
  private WColor color_;
  private BorderStyle style_;
}
