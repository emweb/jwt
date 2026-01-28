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

/** Enumeration that specifies the way text should be printed. */
public enum TextFlag {
  /** Text will be printed on just one line. */
  SingleLine,
  /** Lines will break at word boundaries. */
  WordWrap;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
