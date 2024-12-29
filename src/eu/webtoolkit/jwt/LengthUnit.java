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

/** CSS length unit type. */
public enum LengthUnit {
  /** The relative font size (em) */
  FontEm,
  /** The height of an &apos;x&apos; in the font (ex) */
  FontEx,
  /** Pixel, relative to canvas resolution (px) */
  Pixel,
  /** Inch (in) */
  Inch,
  /** Centimeter (cm) */
  Centimeter,
  /** Millimeter (mm) */
  Millimeter,
  /** Point (1/72 Inch) (pt) */
  Point,
  /** Pica (12 Point) (pc) */
  Pica,
  /** Percentage (meaning context-sensitive) (%) */
  Percentage,
  /**
   * A percentage of the viewport&apos;s width (vw)
   *
   * <p>
   *
   * <p><i><b>Note: </b>Internet Explorer only supports vw since version 9 </i>
   */
  ViewportWidth,
  /**
   * A percentage of the viewport&apos;s height (vh)
   *
   * <p>
   *
   * <p><i><b>Note: </b>Internet Explorer only supports vh since version 9 </i>
   */
  ViewportHeight,
  /**
   * A percentage of the viewport&apos;s smaller dimension (vmin)
   *
   * <p>
   *
   * <p><i><b>Note: </b>Internet Explorer only supports vmin since version 9 </i>
   */
  ViewportMin,
  /**
   * A percentage of the viewport&apos;s larger dimension (vmax)
   *
   * <p>
   *
   * <p><i><b>Note: </b>Not supported on Internet Explorer </i>
   */
  ViewportMax;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
