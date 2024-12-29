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

/** Enumeration for HTML content type. */
public enum HtmlContentType {
  /** XHTML1.x. */
  XHTML1,
  /** HTML4. */
  HTML4,
  /** HTML5. */
  HTML5;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
