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
 * Enumeration that indicates a relative location.
 *
 * <p>Values of {@link Side#CenterX CenterX}, {@link Side#CenterY CenterY}, and CenterXY are only
 * valid for {@link WCssDecorationStyle#setBackgroundImage(WLink image, EnumSet repeat, EnumSet
 * sides) WCssDecorationStyle#setBackgroundImage()}
 *
 * <p>
 *
 * @see WWidget#setMargin(WLength margin, EnumSet sides)
 * @see WWidget#setOffsets(WLength offset, EnumSet sides)
 * @see WWidget#setFloatSide(Side s)
 * @see WWidget#setClearSides(EnumSet sides)
 * @see WContainerWidget#setPadding(WLength length, EnumSet sides)
 * @see WCssDecorationStyle#setBackgroundImage(WLink image, EnumSet repeat, EnumSet sides)
 */
public enum Side {
  /** Top side. */
  Top,
  /** Bottom side. */
  Bottom,
  /** Left side. */
  Left,
  /** Right side. */
  Right,
  /** Center horiziontally. */
  CenterX,
  /** Center vertically. */
  CenterY;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }

  public static final EnumSet<Side> AllSides =
      EnumSet.of(Side.Left, Side.Right, Side.Top, Side.Bottom);
}
