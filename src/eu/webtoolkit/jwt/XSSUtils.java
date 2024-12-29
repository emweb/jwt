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

class XSSUtils {
  private static Logger logger = LoggerFactory.getLogger(XSSUtils.class);

  private XSSUtils() {}

  static boolean isBadTag(final String name) {
    return name.equalsIgnoreCase("script")
        || name.equalsIgnoreCase("applet")
        || name.equalsIgnoreCase("object")
        || name.equalsIgnoreCase("iframe")
        || name.equalsIgnoreCase("frame")
        || name.equalsIgnoreCase("layer")
        || name.equalsIgnoreCase("ilayer")
        || name.equalsIgnoreCase("frameset")
        || name.equalsIgnoreCase("link")
        || name.equalsIgnoreCase("meta")
        || name.equalsIgnoreCase("title")
        || name.equalsIgnoreCase("base")
        || name.equalsIgnoreCase("basefont")
        || name.equalsIgnoreCase("bgsound")
        || name.equalsIgnoreCase("head")
        || name.equalsIgnoreCase("body")
        || name.equalsIgnoreCase("embed")
        || name.equalsIgnoreCase("style")
        || name.equalsIgnoreCase("comment")
        || name.equalsIgnoreCase("blink");
  }

  static boolean isBadAttribute(final String name) {
    return StringUtils.startsWithIgnoreCase(name, "on")
        || StringUtils.startsWithIgnoreCase(name, "data")
        || name.equalsIgnoreCase("dynsrc")
        || name.equalsIgnoreCase("id")
        || name.equalsIgnoreCase("autofocus")
        || name.equalsIgnoreCase("name")
        || name.equalsIgnoreCase("repeat-start")
        || name.equalsIgnoreCase("repeat-end")
        || name.equalsIgnoreCase("repeat")
        || name.equalsIgnoreCase("pattern");
  }

  static boolean isBadAttributeValue(final String name, final String value) {
    if (name.equalsIgnoreCase("action")
        || name.equalsIgnoreCase("background")
        || name.equalsIgnoreCase("codebase")
        || name.equalsIgnoreCase("dynsrc")
        || name.equalsIgnoreCase("href")
        || name.equalsIgnoreCase("formaction")
        || name.equalsIgnoreCase("poster")
        || name.equalsIgnoreCase("src")) {
      String v = value.trim();
      return StringUtils.startsWithIgnoreCase(v, "javascript:")
          || StringUtils.startsWithIgnoreCase(v, "vbscript:")
          || StringUtils.startsWithIgnoreCase(v, "about:")
          || StringUtils.startsWithIgnoreCase(v, "chrome:")
          || StringUtils.startsWithIgnoreCase(v, "data:")
          || StringUtils.startsWithIgnoreCase(v, "disk:")
          || StringUtils.startsWithIgnoreCase(v, "hcp:")
          || StringUtils.startsWithIgnoreCase(v, "help:")
          || StringUtils.startsWithIgnoreCase(v, "livescript")
          || StringUtils.startsWithIgnoreCase(v, "lynxcgi:")
          || StringUtils.startsWithIgnoreCase(v, "lynxexec:")
          || StringUtils.startsWithIgnoreCase(v, "ms-help:")
          || StringUtils.startsWithIgnoreCase(v, "ms-its:")
          || StringUtils.startsWithIgnoreCase(v, "mhtml:")
          || StringUtils.startsWithIgnoreCase(v, "mocha:")
          || StringUtils.startsWithIgnoreCase(v, "opera:")
          || StringUtils.startsWithIgnoreCase(v, "res:")
          || StringUtils.startsWithIgnoreCase(v, "resource:")
          || StringUtils.startsWithIgnoreCase(v, "shell:")
          || StringUtils.startsWithIgnoreCase(v, "view-source:")
          || StringUtils.startsWithIgnoreCase(v, "vnd.ms.radio:")
          || StringUtils.startsWithIgnoreCase(v, "wysiwyg:");
    } else {
      if (name.equalsIgnoreCase("style")) {
        return StringUtils.containsIgnoreCase(value, "absolute")
            || StringUtils.containsIgnoreCase(value, "behaviour")
            || StringUtils.containsIgnoreCase(value, "behavior")
            || StringUtils.containsIgnoreCase(value, "content")
            || StringUtils.containsIgnoreCase(value, "expression")
            || StringUtils.containsIgnoreCase(value, "fixed")
            || StringUtils.containsIgnoreCase(value, "include-source")
            || StringUtils.containsIgnoreCase(value, "moz-binding")
            || StringUtils.containsIgnoreCase(value, "javascript");
      } else {
        return false;
      }
    }
  }
}
