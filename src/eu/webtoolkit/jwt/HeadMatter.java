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

class HeadMatter {
  private static Logger logger = LoggerFactory.getLogger(HeadMatter.class);

  // public  HeadMatter(String contents, String userAgent) ;
  public String getContents() {
    return this.contents_;
  }

  public String getUserAgent() {
    return this.userAgent_;
  }

  private String contents_;
  private String userAgent_;

  static boolean isAbsoluteUrl(final String url) {
    return url.indexOf("://") != -1;
  }

  static void appendAttribute(final EscapeOStream eos, final String name, final String value) {
    eos.append(' ').append(name).append("=\"");
    eos.pushEscape(EscapeOStream.RuleSet.HtmlAttribute);
    eos.append(value);
    eos.popEscape();
    eos.append('"');
  }

  static void closeSpecial(final StringBuilder s) {
    s.append(">\n");
  }

  static void closeSpecial(final EscapeOStream s) {
    s.append(">\n");
  }
}
