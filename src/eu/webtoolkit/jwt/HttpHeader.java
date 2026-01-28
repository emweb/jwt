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

class HttpHeader {
  private static Logger logger = LoggerFactory.getLogger(HttpHeader.class);

  public HttpHeader(String name, String contents) {
    this.name_ = name;
    this.contents_ = contents;
  }

  public String getContents() {
    return this.contents_;
  }

  public String getName() {
    return this.name_;
  }

  public boolean equals(final HttpHeader other) {
    return this.contents_.equals(other.getContents()) && this.name_.equals(other.getName());
  }

  private String name_;
  private String contents_;
}
