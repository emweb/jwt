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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The result of processing an authentication token.
 *
 * <p>An authentication token is usually taken from a browser cookie, and used to identify (and
 * possibly authenticate) a user across sessions.
 *
 * <p>
 *
 * @see AuthService#processAuthToken(String token, AbstractUserDatabase users)
 * @see AuthService#createAuthToken(User user, int authTokenValidity)
 */
public class AuthTokenResult {
  private static Logger logger = LoggerFactory.getLogger(AuthTokenResult.class);

  /**
   * Constructor.
   *
   * <p>Creates an authentication token result.
   */
  public AuthTokenResult(
      AuthTokenState state, final User user, final String newToken, int newTokenValidity) {
    this.state_ = state;
    this.user_ = user;
    this.newToken_ = newToken;
    this.newTokenValidity_ = newTokenValidity;
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #AuthTokenResult(AuthTokenState state, User user, String newToken, int
   * newTokenValidity) this(state, new User(), "", - 1)}
   */
  public AuthTokenResult(AuthTokenState state) {
    this(state, new User(), "", -1);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #AuthTokenResult(AuthTokenState state, User user, String newToken, int
   * newTokenValidity) this(state, user, "", - 1)}
   */
  public AuthTokenResult(AuthTokenState state, final User user) {
    this(state, user, "", -1);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #AuthTokenResult(AuthTokenState state, User user, String newToken, int
   * newTokenValidity) this(state, user, newToken, - 1)}
   */
  public AuthTokenResult(AuthTokenState state, final User user, final String newToken) {
    this(state, user, newToken, -1);
  }
  /** Returns the result. */
  public AuthTokenState getState() {
    return this.state_;
  }
  /**
   * Returns the identified user.
   *
   * <p>The user is valid only if the the {@link AuthTokenResult#getState() getState()} == {@link
   * AuthTokenState#Valid}.
   */
  public User getUser() {
    if (this.user_.isValid()) {
      return this.user_;
    } else {
      throw new WException("AuthTokenResult::user() invalid");
    }
  }
  /**
   * Returns a new token for this user.
   *
   * <p>Returns the empty string if there is no new token. See {@link
   * AuthService#isAuthTokenUpdateEnabled()}.
   *
   * <p>The returned token is valid only if the {@link AuthTokenResult#getState() getState()} ==
   * {@link AuthTokenState#Valid}.
   */
  public String getNewToken() {
    if (this.user_.isValid()) {
      return this.newToken_;
    } else {
      throw new WException("AuthTokenResult::newToken() invalid");
    }
  }
  /**
   * Returns the validity of the new token.
   *
   * <p>This returns the token validity in seconds.
   *
   * <p>Returns -1 if there is no new token, or result() != Valid.
   *
   * <p>
   *
   * @see AuthTokenResult#getNewToken()
   */
  public int getNewTokenValidity() {
    if (this.user_.isValid()) {
      return this.newTokenValidity_;
    } else {
      throw new WException("AuthTokenResult::newTokenValidity() invalid");
    }
  }

  private AuthTokenState state_;
  private User user_;
  private String newToken_;
  private int newTokenValidity_;
}
