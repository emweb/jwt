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

/** An HTML Meta Header. */
public class MetaHeader {
  private static Logger logger = LoggerFactory.getLogger(MetaHeader.class);

  /**
   * Constructor.
   *
   * <p>Creates a meta header. The lang and user agents are optional, and should be an empty string
   * if not used.
   */
  public MetaHeader(
      MetaHeaderType aType,
      final String aName,
      final CharSequence aContent,
      final String aLang,
      final String aUserAgent) {
    this.type = aType;
    this.name = aName;
    this.lang = aLang;
    this.userAgent = aUserAgent;
    this.content = WString.toWString(aContent);
  }

  public MetaHeaderType type;
  public String name;
  public String lang;
  public String userAgent;
  public WString content;
}
