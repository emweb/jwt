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
 * A value class that describes font metrics for a font.
 *
 * <p>This class provides font metrics for a given font. It is returned by an implementation of
 * {@link WPaintDevice#getFontMetrics()}, and may differ between devices.
 *
 * <p>All methods return pixel dimensions.
 *
 * <p>
 *
 * @see WPaintDevice
 */
public class WFontMetrics {
  private static Logger logger = LoggerFactory.getLogger(WFontMetrics.class);

  /** Creates a font metrics information object. */
  public WFontMetrics(final WFont font, double leading, double ascent, double descent) {
    this.font_ = font;
    this.leading_ = leading;
    this.ascent_ = ascent;
    this.descent_ = descent;
  }
  /** Returns the font for which these font metrics were computed. */
  public WFont getFont() {
    return this.font_;
  }
  /**
   * Returns the font size.
   *
   * <p>This is the same as:
   *
   * <pre>{@code
   * font().size().sizeLength()
   *
   * }</pre>
   *
   * <p>e.g.~for a font with size set to 16px, this returns 16.
   */
  public double getSize() {
    return this.ascent_ + this.descent_;
  }
  /**
   * Returns the font height.
   *
   * <p>The font height is the total height of a text line. It is usually a bit bigger than the font
   * size to have natural line spacing.
   */
  public double getHeight() {
    return this.leading_ + this.ascent_ + this.descent_;
  }
  /**
   * Returns the font leading length.
   *
   * <p>This is vertical space provided on top of the ascent (empty space which serves as natural
   * line spacing).
   */
  public double getLeading() {
    return this.leading_;
  }
  /**
   * Returns the font ascent length.
   *
   * <p>This is vertical space which corresponds to the maximum height of a character over the
   * baseline (although many fonts violate this for some glyphs).
   */
  public double getAscent() {
    return this.ascent_;
  }
  /**
   * Returns the font descent length.
   *
   * <p>This is vertical space which corresponds to the maximum height of a character under the
   * baseline (although many fonts violate this for some glyphs).
   */
  public double getDescent() {
    return this.descent_;
  }

  private WFont font_;
  private double leading_;
  private double ascent_;
  private double descent_;
}
