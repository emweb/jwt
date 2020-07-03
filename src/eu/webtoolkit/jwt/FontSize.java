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
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/** The font size. */
public enum FontSize {
  /** Extra Extra small. */
  XXSmall,
  /** Extra small. */
  XSmall,
  /** Small. */
  Small,
  /** Medium, default. */
  Medium,
  /** Large. */
  Large,
  /** Extra large. */
  XLarge,
  /** Extra Extra large. */
  XXLarge,
  /** Relatively smaller than the parent widget. */
  Smaller,
  /** Relatively larger than the parent widget. */
  Larger,
  /** Explicit size, See also fontFixedSize() */
  FixedSize;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
