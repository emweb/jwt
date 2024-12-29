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
 * Enumeration for a DOM element type.
 *
 * <p>For internal use only.
 */
public enum DomElementType {
  A,
  BR,
  BUTTON,
  COL,
  COLGROUP,
  DIV,
  FIELDSET,
  FORM,
  H1,
  H2,
  H3,
  H4,
  H5,
  H6,
  IFRAME,
  IMG,
  INPUT,
  LABEL,
  LEGEND,
  LI,
  OL,
  OPTION,
  UL,
  SCRIPT,
  SELECT,
  SPAN,
  TABLE,
  TBODY,
  THEAD,
  TFOOT,
  TH,
  TD,
  TEXTAREA,
  OPTGROUP,
  TR,
  P,
  CANVAS,
  MAP,
  AREA,
  STYLE,
  OBJECT,
  PARAM,
  AUDIO,
  VIDEO,
  SOURCE,
  B,
  STRONG,
  EM,
  I,
  HR,
  DATALIST,
  UNKNOWN,
  OTHER;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
