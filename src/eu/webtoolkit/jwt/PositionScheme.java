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
 * Enumeration that specifies a layout mechanism for a widget.
 *
 * <p>The layout mechanism determines how the widget positions itself relative to the parent or
 * sibling widgets.
 *
 * <p>
 *
 * @see WWidget#setPositionScheme(PositionScheme scheme)
 */
public enum PositionScheme {
  /**
   * Static position scheme.
   *
   * <p>The widget is layed-out with other {@link PositionScheme#Static Static} and {@link
   * PositionScheme#Relative Relative} sibling widgets, one after another.
   *
   * <p>Inline widgets are layed out in horizontal lines (like text), wrapping around at the end of
   * the line to continue on the next line. Block widgets are stacked vertically.
   *
   * <p>Static widgets may also float to the left or right border, using setFloatSide().
   */
  Static,
  /**
   * Relative position scheme.
   *
   * <p>The widget is first layed out according to Static layout rules, but after layout, the widget
   * may be offset relative to where it would be in a static layout, using setOffsets().
   *
   * <p>Another common use of a Relative position scheme (even with no specified offsets) is to
   * provide a new reference coordinate system for Absolutely positioned widgets.
   */
  Relative,
  /**
   * Absolute position scheme.
   *
   * <p>The widget is positioned at an absolute position with respect to the nearest ancestor widget
   * that is either:
   *
   * <ul>
   *   <li>a {@link WTableCell}
   *   <li>or has a position scheme that is {@link PositionScheme#Relative Relative} or {@link
   *       PositionScheme#Absolute Absolute}.
   * </ul>
   */
  Absolute,
  /**
   * Fixed position scheme.
   *
   * <p>The widget is positioned at fixed position with respect to the browser&apos;s view-pane.
   */
  Fixed;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
