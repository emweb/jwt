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

/** Enumeration to indicate paint device features. */
public enum PaintDeviceFeatureFlag {
  /** Implements drawText() with {@link TextFlag#WordWrap WordWrap}. */
  WordWrap,
  /** Implements fontMetrics() and measureText() */
  FontMetrics;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
