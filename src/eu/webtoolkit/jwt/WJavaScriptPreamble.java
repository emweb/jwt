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

/**
 * Javascript preamble.
 *
 * <p>This is an internal JWt type.
 */
public class WJavaScriptPreamble {
  private static Logger logger = LoggerFactory.getLogger(WJavaScriptPreamble.class);

  public WJavaScriptPreamble(
      JavaScriptScope scope, JavaScriptObjectType type, String name, String src) {
    this.scope = scope;
    this.type = type;
    this.name = name;
    this.src = src;
  }

  public JavaScriptScope scope;
  public JavaScriptObjectType type;
  public String name;
  public String src;
}
