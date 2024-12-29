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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Represents a selection on a chart. */
public class WSelection {
  private static Logger logger = LoggerFactory.getLogger(WSelection.class);

  /** The distance from the look point to the selected point. */
  public double distance;

  public WSelection() {
    this.distance = Double.POSITIVE_INFINITY;
  }

  public WSelection(double distance) {
    this.distance = distance;
  }
}
