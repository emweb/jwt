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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PdfUtils {
  private static Logger logger = LoggerFactory.getLogger(PdfUtils.class);

  private PdfUtils() {}

  static String toBase14Font(final WFont font) {
    String base = null;
    String italic = null;
    String bold = null;
    switch (font.getGenericFamily()) {
      case Default:
      case Serif:
      case Fantasy:
      case Cursive:
        base = "Times";
        italic = "Italic";
        bold = "Bold";
        break;
      case SansSerif:
        base = "Helvetica";
        italic = "Oblique";
        bold = "Bold";
        break;
      case Monospace:
        base = "Courier";
        italic = "Oblique";
        bold = "Bold";
        break;
    }
    if ((font.getSpecificFamilies().toString().equals("Symbol".toString()))) {
      base = "Symbol";
    } else {
      if ((font.getSpecificFamilies().toString().equals("ZapfDingbats".toString()))) {
        base = "ZapfDingbats";
      }
    }
    if (italic != null) {
      switch (font.getStyle()) {
        case Normal:
          italic = null;
          break;
        default:
          break;
      }
    }
    if (font.getWeightValue() <= 400) {
      bold = null;
    }
    String name = base;
    if (bold != null) {
      name += "-" + bold;
      if (italic != null) {
        name += italic;
      }
    } else {
      if (italic != null) {
        name += "-" + italic;
      }
    }
    if (name.equals("Times")) {
      name = "Times-Roman";
    }
    return name;
  }
}
