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
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

/** Enumeration that indicates a fill style. */
public enum BrushStyle {
  /** Do not fill. */
  None,
  /** Fill with a solid color. */
  Solid,
  /** Fill with a color gradient. */
  Gradient;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
