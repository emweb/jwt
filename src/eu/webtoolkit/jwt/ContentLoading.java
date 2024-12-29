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

/** Enumeration that determines when contents should be loaded. */
public enum ContentLoading {
  /** Lazy loading: on first use. */
  Lazy,
  /** Pre-loading: before first use. */
  Eager,
  /** Pre-load also next level (if applicable, e.g. for {@link WTreeNode}) */
  NextLevel;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
