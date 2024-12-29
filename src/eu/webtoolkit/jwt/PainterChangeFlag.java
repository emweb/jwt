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

/** Enumeration to communicate painter state changes. */
public enum PainterChangeFlag {
  /** Properties of the pen have changed. */
  Pen,
  /** Properties of the brush have changed. */
  Brush,
  /** Properties of the font have changed. */
  Font,
  /** Some render hints have changed. */
  Hints,
  /** The transformation has changed. */
  Transform,
  /** The clipping has changed. */
  Clipping,
  /** Properties of the shadow have changed. */
  Shadow;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
