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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HeadMatter {
  private static Logger logger = LoggerFactory.getLogger(HeadMatter.class);

  public HeadMatter(String contents, String userAgent) {
    this.contents_ = contents;
    this.userAgent_ = userAgent;
  }

  public String getContents() {
    return this.contents_;
  }

  public String getUserAgent() {
    return this.userAgent_;
  }

  private String contents_;
  private String userAgent_;
}
