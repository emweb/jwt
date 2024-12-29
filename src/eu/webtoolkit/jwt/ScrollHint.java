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

/** Enumeration that specifies a scrolling option. */
public enum ScrollHint {
  /** Scrolls minimally to make it visible. */
  EnsureVisible,
  /** Positions the item at the top of the viewport. */
  PositionAtTop,
  /** Positions the item at the bottom of the viewport. */
  PositionAtBottom,
  /** Positions the item at the center of the viewport. */
  PositionAtCenter;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
