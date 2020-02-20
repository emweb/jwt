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

/** Lists the three orthogonal planes in 3D. */
public enum Plane {
  /** X/Y Plane. */
  XY_Plane,
  /** X/Z Plane. */
  XZ_Plane,
  /** Y/Z Plane. */
  YZ_Plane;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
