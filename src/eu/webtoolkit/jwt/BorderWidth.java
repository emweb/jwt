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

/** Enumeration for border width. */
public enum BorderWidth {
  /** Browser-dependent &apos;thin&apos; border. */
  Thin,
  /** Browser-dependent &apos;medium&apos; border, default. */
  Medium,
  /** Browser-dependent &apos;thick&apos; border. */
  Thick,
  /** Explicit width. See also explicitWidth() */
  Explicit;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
