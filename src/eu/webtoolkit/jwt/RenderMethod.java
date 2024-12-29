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
 * Enumeration that indicates a client-side rendering method for a painted widget.
 *
 * <p>
 *
 * @see WPaintedWidget
 */
public enum RenderMethod {
  /** SVG (Most browsers) or VML (Internet Explorer &lt; 9) embedded in the page. */
  InlineSvgVml,
  /** The HTML5 canvas element. */
  HtmlCanvas,
  /** Using a PNG image resource. */
  PngImage;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
