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

/** Base class for exceptions thrown by JWt. */
public class WException extends RuntimeException {
  private static Logger logger = LoggerFactory.getLogger(WException.class);

  /** Creates an exception. */
  public WException(final String what) {
    super();
    this.what_ = what;
  }
  /** Creates an exception. */
  public WException(final String what, final RuntimeException wrapped) {
    super();
    this.what_ = what + "\nCaused by: " + wrapped.toString();
  }
  /** Returns the message. */
  public String toString() {
    return this.what_;
  }
  /** Sets the message. */
  public void setMessage(final String message) {
    this.what_ = message;
  }

  private String what_;

  public static String backtrace() {
    return "N/A";
  }
}
