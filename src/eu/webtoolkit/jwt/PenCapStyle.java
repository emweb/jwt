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

/** Enumeration that indicates how line end points are rendered. */
public enum PenCapStyle {
  /** Flat ends. */
  Flat,
  /** Square ends (prolongs line with half width) */
  Square,
  /** Rounded ends (terminates with a half circle) */
  Round;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
