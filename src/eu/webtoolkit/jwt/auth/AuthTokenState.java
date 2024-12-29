/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.*;
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

/** Enumeration that describes an auth token validation state. */
public enum AuthTokenState {
  /** The presented auth token was invalid. */
  Invalid,
  /** The presented auth token could be used to identify a user. */
  Valid;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
