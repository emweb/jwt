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
 * The result of a font metrics computation.
 *
 * <p>
 *
 * @see WPaintDevice#measureText(CharSequence text, double maxWidth, boolean wordWrap)
 */
public class WTextItem {
  private static Logger logger = LoggerFactory.getLogger(WTextItem.class);

  /** Constructor. */
  public WTextItem(final CharSequence text, double width, double nextWidth) {
    this.text_ = WString.toWString(text);
    this.width_ = width;
    this.nextWidth_ = nextWidth;
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WTextItem(CharSequence text, double width, double nextWidth) this(text, width,
   * - 1)}
   */
  public WTextItem(final CharSequence text, double width) {
    this(text, width, -1);
  }
  /**
   * Returns the measured text.
   *
   * <p>If the item was measured with word breaking enabled, then the text may contain trailing
   * whitespace that is not included in the {@link WTextItem#getWidth() getWidth()}.
   */
  public WString getText() {
    return this.text_;
  }
  /**
   * Returns the measured width.
   *
   * <p>Returns the text width, in device local coordinates (pixels).
   */
  public double getWidth() {
    return this.width_;
  }
  /**
   * Returns the width for a next line-break boundary.
   *
   * <p>Returns the width until the next line-break boundary, or -1 if the underlying word boundary
   * analysis does not support this.
   */
  public double getNextWidth() {
    return this.nextWidth_;
  }

  private WString text_;
  private double width_;
  private double nextWidth_;
}
