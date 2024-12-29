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
 * The result of resolving a localized string.
 *
 * <p>This struct contains the result (in UTF-8 encoding) of resolving a localized string,
 * consisting of its value, its format ({@link TextFormat#Plain} or {@link TextFormat#XHTML}), and a
 * success value indicating whether the string was successfully resolved.
 */
public class LocalizedString {
  private static Logger logger = LoggerFactory.getLogger(LocalizedString.class);

  /**
   * Constructor for an unsuccessful localized string result.
   *
   * <p>This constructor sets success to false.
   */
  public LocalizedString() {
    this.value = "";
    this.format = TextFormat.Plain;
    this.success = false;
  }
  /**
   * Constructor for a successful localized string result.
   *
   * <p>Sets the value to the given string, and the format to the given format, and sets success to
   * true.
   */
  public LocalizedString(String v, TextFormat f) {
    this.value = v;
    this.format = f;
    this.success = true;
  }
  /**
   * The value of the resolved localized string.
   *
   * <p>This value is UTF-8 encoded
   */
  public String value;
  /**
   * The format that the resolved localized string is stored in ({@link TextFormat#Plain} or {@link
   * TextFormat#XHTML})
   */
  public TextFormat format;
  /** Indicates whether resolving the string was successful. */
  public boolean success;
}
