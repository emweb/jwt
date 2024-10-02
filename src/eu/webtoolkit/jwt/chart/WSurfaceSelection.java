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

/** Represents a selection on a surface plot. */
public class WSurfaceSelection extends WSelection {
  private static Logger logger = LoggerFactory.getLogger(WSurfaceSelection.class);

  /** The x coordinate in the coordinate system of the WAbstractDataModel. */
  public double x;
  /** The y coordinate in the coordinate system of the WAbstractDataModel. */
  public double y;
  /** The z coordinate in the coordinate system of the WAbstractDataModel. */
  public double z;

  public WSurfaceSelection() {
    super();
    this.x = 0.0;
    this.y = 0.0;
    this.z = 0.0;
  }

  public WSurfaceSelection(double distance, double x, double y, double z) {
    super(distance);
    this.x = x;
    this.y = y;
    this.z = z;
  }
}
