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
 * Enumeration that specifies options for the labels.
 *
 * <p>
 *
 * @see WPieChart#setDisplayLabels(EnumSet options)
 */
public enum LabelOption {
  /** Do not display labels (default). */
  None,
  /** Display labels inside each segment. */
  Inside,
  /** Display labels outside each segment. */
  Outside,
  /** Display the label text. */
  TextLabel,
  /** Display the value (as percentage) */
  TextPercentage;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
