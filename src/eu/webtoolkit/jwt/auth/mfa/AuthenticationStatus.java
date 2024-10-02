/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth.mfa;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
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

/**
 * Enumeration that holds the result of the authentication event.
 *
 * <p>The authentication event as a second step in the authentication flow, will have a certain
 * result after the user submits their input.
 */
public enum AuthenticationStatus {
  /** Indicates a successful authentication event. */
  Success,
  /** Indicates a failure to authenticate. */
  Failure,
  /** An error was encountered when trying to authenticate. */
  Error;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
