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

/**
 * Enumeration that indicates a standard button.
 *
 * <p>Multiple buttons may be specified by logically or&apos;ing these values together, e.g.
 *
 * <pre>{@code
 * StandardButton::Ok | StandardButton::Cancel
 *
 * }</pre>
 *
 * <p>
 *
 * @see WMessageBox
 */
public enum StandardButton {
  /** No button. */
  None,
  /** An OK button. */
  Ok,
  /** A Cancel button. */
  Cancel,
  /** A Yes button. */
  Yes,
  /** A No button. */
  No,
  /** An Abort button. */
  Abort,
  /** A Retry button. */
  Retry,
  /** An Ignore button. */
  Ignore,
  /** A Yes-to-All button. */
  YesAll,
  /** A No-to-All button. */
  NoAll;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
