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

/**
 * Enumeration to indicate which layout implementation to use.
 *
 * <p>
 *
 * @see WLayout#setPreferredImplementation(LayoutImplementation implementation)
 * @see WLayout#setDefaultImplementation(LayoutImplementation implementation)
 */
public enum LayoutImplementation {
  /** Use CSS flex layout (if supported by the browser) */
  Flex,
  /**
   * Uses the classic JavaScript-based method. In some cases flex layout may fail when nesting
   * multiple layouts, so this method can be used instead.
   */
  JavaScript;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
