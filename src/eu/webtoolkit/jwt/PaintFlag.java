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
 * Enumeration that indicates how to change a selection.
 *
 * <p>
 *
 * @see WPaintedWidget#update(EnumSet flags)
 */
public enum PaintFlag {
  /** The canvas is not cleared, but further painted on. */
  Update;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
