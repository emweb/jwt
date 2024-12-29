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

/**
 * Enumeration that specifies a horizontal or a vertical alignment.
 *
 * <p>The vertical alignment flags are {@link AlignmentFlag#Baseline Baseline}, {@link
 * AlignmentFlag#Sub Sub}, {@link AlignmentFlag#Super Super}, {@link AlignmentFlag#Top Top}, {@link
 * AlignmentFlag#TextTop TextTop}, {@link AlignmentFlag#Middle Middle}, {@link AlignmentFlag#Bottom
 * Bottom} and {@link AlignmentFlag#TextBottom TextBottom}.
 *
 * <p>The horizontal alignment flags are {@link AlignmentFlag#Left Left}, {@link AlignmentFlag#Right
 * Right}, {@link AlignmentFlag#Center Center} and {@link AlignmentFlag#Justify Justify}.
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
 * <p>Not all values are applicable in all situations. The most commonly used values are {@link
 * AlignmentFlag#Left Left}, {@link AlignmentFlag#Center Center}, {@link AlignmentFlag#Right Right},
 * {@link AlignmentFlag#Bottom Bottom}, {@link AlignmentFlag#Middle Middle} and {@link
 * AlignmentFlag#Top Top}.
 */
public enum AlignmentFlag {
  /** Align to the left. */
  Left,
  /** Align to the right. */
  Right,
  /** Align horizontally in the center. */
  Center,
  /** Justify left and right. */
  Justify,
  /** Align at baseline (default alignment). */
  Baseline,
  /** Align below the baseline (as if subscript). */
  Sub,
  /** Align above the baseline (as if superscript). */
  Super,
  /** Align top of widget with top of tallest sibling widget. */
  Top,
  /** Align top of widget with the top of the parent widget&apos;s font. */
  TextTop,
  /** Align vertically the middle to the middle of the parent widget. */
  Middle,
  /** Align bottom of widget to the bottom of the lowest sigling widget. */
  Bottom,
  /** Align bottom of widget to the bottom of parent widget&apos;s font. */
  TextBottom;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
  /** Combination of all horizontal alignment flags. */
  public static final EnumSet<AlignmentFlag> AlignHorizontalMask =
      EnumSet.of(
          AlignmentFlag.Left, AlignmentFlag.Right, AlignmentFlag.Center, AlignmentFlag.Justify);
  /** Combination of all vertical alignment flags. */
  public static final EnumSet<AlignmentFlag> AlignVerticalMask =
      EnumSet.of(
          AlignmentFlag.Baseline,
          AlignmentFlag.Sub,
          AlignmentFlag.Super,
          AlignmentFlag.Top,
          AlignmentFlag.TextTop,
          AlignmentFlag.Middle,
          AlignmentFlag.Bottom,
          AlignmentFlag.TextBottom);
}
