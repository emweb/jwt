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

/** Enumeration that specifies where the target of an anchor should be displayed. */
public enum LinkTarget {
  /** Show Instead of the application. */
  Self,
  /** Show in the top level frame of the application window. */
  ThisWindow,
  /** Show in a separate new tab or window. */
  NewWindow,
  /** Useful only for a downloadable resource. */
  Download;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
