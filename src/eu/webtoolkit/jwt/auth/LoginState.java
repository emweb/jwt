/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.*;
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
 * Enumeration for a login state.
 *
 * <p>
 *
 * @see Login#getState()
 */
public enum LoginState {
  /** No user is currently identified. */
  LoggedOut,
  /**
   * The identified user was refused to login.
   *
   * <p>This is caused by for example {@link User#getStatus()} returning {@link
   * AccountStatus#Disabled Disabled}, or if email verification is required but the email
   * hasn&apos;t been verified yet.
   */
  Disabled,
  /**
   * A user is weakly authenticated.
   *
   * <p>The authentication method was weak, typically this means that a secondary authentication
   * system was used (e.g. an authentication cookie) instead of a primary mechanism (like a
   * password).
   *
   * <p>You may want to allow certain operations, but request to authenticate fully before more
   * sensitive operations.
   */
  Weak,
  /** A user is strongly authenticated. */
  Strong;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
