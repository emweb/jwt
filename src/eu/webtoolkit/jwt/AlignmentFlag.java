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
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration that specifies a horizontal or a vertical alignment.
 *
 * <p>The vertical alignment flags are AlignBaseline, AlignSub, AlignSuper, AlignTop, AlignTextTop,
 * AlignMiddle, AlignBottom and AlignTextBottom. The horizontal alignment flags are AlignLeft,
 * AlignRight, AlignCenter and AlignJustify.
 *
 * <p>When used with setVerticalAlignment(), this applies only to inline widgets and determines how
 * to position itself on the current line, with respect to sibling inline widgets.
 *
 * <p>When used with {@link WContainerWidget#setContentAlignment(EnumSet alignment)
 * WContainerWidget#setContentAlignment()}, this determines the vertical alignment of contents
 * within the table cell.
 *
 * <p>When used with {@link WPainter#drawText(WRectF rectangle, EnumSet alignmentFlags, TextFlag
 * textFlag, CharSequence text, WPointF clipPoint) WPainter#drawText()}, this determines the
 * horizontal and vertical alignment of the text with respect to the bounding rectangle.
 *
 * <p>When used with {@link WContainerWidget#setContentAlignment(EnumSet alignment)
 * WContainerWidget#setContentAlignment()}, this specifies how contents should be aligned
 * horizontally within the container.
 *
 * <p>Not all values are applicable in all situations. The most commonly used values are AlignLeft,
 * AlignCenter, AlignRight, AlignBottom, AlignMiddle and AlignTop.
 */
public enum AlignmentFlag {
  /** Align to the left. */
  AlignLeft,
  /** Align to the right. */
  AlignRight,
  /** Align horizontally in the center. */
  AlignCenter,
  /** Justify left and right. */
  AlignJustify,
  /** Align at baseline (default alignment). */
  AlignBaseline,
  /** Align below the baseline (as if subscript). */
  AlignSub,
  /** Align above the baseline (as if superscript). */
  AlignSuper,
  /** Align top of widget with top of tallest sibling widget. */
  AlignTop,
  /** Align top of widget with the top of the parent widget&apos;s font. */
  AlignTextTop,
  /** Align vertically the middle to the middle of the parent widget. */
  AlignMiddle,
  /** Align bottom of widget to the bottom of the lowest sigling widget. */
  AlignBottom,
  /** Align bottom of widget to the bottom of parent widget&apos;s font. */
  AlignTextBottom;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
  /** Combination of all horizontal alignment flags. */
  public static final EnumSet<AlignmentFlag> AlignHorizontalMask =
      EnumSet.of(
          AlignmentFlag.AlignLeft,
          AlignmentFlag.AlignRight,
          AlignmentFlag.AlignCenter,
          AlignmentFlag.AlignJustify);
  /** Combination of all vertical alignment flags. */
  public static final EnumSet<AlignmentFlag> AlignVerticalMask =
      EnumSet.of(
          AlignmentFlag.AlignBaseline,
          AlignmentFlag.AlignSub,
          AlignmentFlag.AlignSuper,
          AlignmentFlag.AlignTop,
          AlignmentFlag.AlignTextTop,
          AlignmentFlag.AlignMiddle,
          AlignmentFlag.AlignBottom,
          AlignmentFlag.AlignTextBottom);
}
