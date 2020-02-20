/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/** Enumeration with the possible representations of a {@link WAbstractGridData}. */
public enum Series3DType {
  /** Series rendered as points. */
  PointSeries3D,
  /** Series rendered as a surface. */
  SurfaceSeries3D,
  /** Series rendered as bars. */
  BarSeries3D;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
