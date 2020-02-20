/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration that indicates a meta header type.
 *
 * <p>
 *
 * @see WApplication#addMetaHeader(String name, CharSequence content, String lang)
 */
public enum MetaHeaderType {
  /** Of the form &lt;meta name=... content=... &gt;. */
  MetaName,
  /** Of the form &lt;meta property=... content=... &gt;. */
  MetaProperty,
  /** Of the form &lt;meta http-equiv=... content=... &gt;. */
  MetaHttpHeader;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
