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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that holds an authentication result.
 *
 * <p>This class in essence is a record of an authentication attempt. The AuthenticationStatus will
 * indicate whether the event was successful or not, and the optional string provides a way to
 * customize a message. This can be used to display more detailed information to the user, or allow
 * the developer to log some information.
 *
 * <p>By default this signal is used in {@link TotpProcess#processEnvironment()}. There it is fired
 * upon successfully matching the TOTP code and finding a matching environment token (Http::Cookie)
 * against the database respectively.
 */
public class AuthenticationResult {
  private static Logger logger = LoggerFactory.getLogger(AuthenticationResult.class);

  /**
   * Default constructor.
   *
   * <p>Creates an invalid result.
   */
  public AuthenticationResult() {
    this.status_ = AuthenticationStatus.Failure;
    this.message_ = new WString();
  }
  /**
   * Constructor.
   *
   * <p>Creates a result with given <code>status</code> and <code>message</code>.
   */
  public AuthenticationResult(AuthenticationStatus status, final CharSequence message) {
    this.status_ = status;
    this.message_ = WString.toWString(message);
  }
  /**
   * Constructor.
   *
   * <p>Creates a result with given <code>status</code> and an empty message.
   */
  public AuthenticationResult(AuthenticationStatus status) {
    this.status_ = status;
    this.message_ = new WString();
  }
  /** Returns the authentication status. */
  public AuthenticationStatus getStatus() {
    return this.status_;
  }
  /** Returns the authentication message. */
  public WString getMessage() {
    return this.message_;
  }

  private AuthenticationStatus status_;
  private WString message_;
}
