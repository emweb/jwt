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

/** Google Maps map type control. */
public enum MapTypeControl {
  /** Show no maptype control. */
  None,
  /** Show the default maptype control. */
  Default,
  /** Show the drop-down menu maptype control. */
  Menu,
  /** Show the hierarchical maptype control. */
  Hierarchical,
  /** Show the horizontal bar maptype control. */
  HorizontalBar;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
