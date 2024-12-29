/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
 * Enumeration that indicates an aspect of the look.
 *
 * <p>These flags are used to keep track of which aspects of the look that are overridden from the
 * values provided by the chart palette, using one of the methods in this class.
 *
 * <p>
 */
public enum CustomFlag {
  /** A custom pen is set. */
  Pen,
  /** A custom brush is set. */
  Brush,
  /** A custom marker pen is set. */
  MarkerPen,
  /** A custom marker brush is set. */
  MarkerBrush,
  /** A custom label color is set. */
  LabelColor;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
