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

/** Enumeration for border style. */
public enum BorderStyle {
  /** No border (width ignored), default. */
  None,
  /** Invisible border (of specified width). */
  Hidden,
  /** Dotted border. */
  Dotted,
  /** Dashed border. */
  Dashed,
  /** Solid border. */
  Solid,
  /** Double lined border. */
  Double,
  /** Relief border grooved into the canvas. */
  Groove,
  /** Relief border coming out of the canvas. */
  Ridge,
  /** Relief border lowering contents into the canvas. */
  Inset,
  /** Relief border letting contents come out of the canvas. */
  Outset;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
