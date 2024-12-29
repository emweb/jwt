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

/** The font weight. */
public enum FontWeight {
  /** Normal (default) (Value == 400) */
  Normal,
  /** Bold (Value == 700) */
  Bold,
  /** Bolder than the parent widget. */
  Bolder,
  /** Lighter than the parent widget. */
  Lighter,
  /** Specify a value (100 - 900) */
  Value;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
