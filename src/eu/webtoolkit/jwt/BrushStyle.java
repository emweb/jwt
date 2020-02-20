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

/** Enumeration that indicates a fill style. */
public enum BrushStyle {
  /** Do not fill. */
  NoBrush,
  /** Fill with a solid color. */
  SolidPattern,
  /** Fill with a color gradient. */
  GradientPattern;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
