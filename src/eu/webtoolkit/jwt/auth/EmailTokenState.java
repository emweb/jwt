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

/** A token validation state. */
public enum EmailTokenState {
  /** The token was invalid. */
  Invalid,
  /** The token has expired. */
  Expired,
  /**
   * A token was presented which requires the user to enter a new password.
   *
   * <p>The presented token was a token sent by the {@link AuthService#lostPassword(String
   * emailAddress, AbstractUserDatabase users) AuthService#lostPassword()} function. When this is
   * returned as result of {@link AuthService#processEmailToken(String token, AbstractUserDatabase
   * users) AuthService#processEmailToken()}, you should present the user with a dialog where he can
   * enter a new password.
   */
  UpdatePassword,
  /**
   * A The token was presented which verifies the email address.
   *
   * <p>The presented token was a token sent by the {@link AuthService#verifyEmailAddress(User user,
   * String address) AuthService#verifyEmailAddress()} function. When this is returned as result of
   * processEmailToken(), you can indicate to the user that his email address is now confirmed.
   */
  EmailConfirmed;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
