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
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;

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
