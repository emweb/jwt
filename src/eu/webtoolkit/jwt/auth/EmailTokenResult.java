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
 * The result of processing an email-sent token.
 *
 * <p>An email token can be used for two purposes:
 *
 * <p>
 *
 * <ul>
 *   <li>the user needs to verify his email address by returning a token sent to his supplied email
 *       address.
 *   <li>the user indicates that he lost his email and wants to prove his identity by acknowledging
 *       an email to a previously verified email account.
 * </ul>
 *
 * <p>
 *
 * @see AuthService#processEmailToken(String token, AbstractUserDatabase users)
 * @see AuthService#verifyEmailAddress(User user, String address)
 * @see AuthService#lostPassword(String emailAddress, AbstractUserDatabase users)
 */
public class EmailTokenResult {
  private static Logger logger = LoggerFactory.getLogger(EmailTokenResult.class);

  /**
   * Constructor.
   *
   * <p>Creates an email token result.
   */
  public EmailTokenResult(EmailTokenState state, final User user) {
    this.state_ = state;
    this.user_ = user;
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #EmailTokenResult(EmailTokenState state, User user) this(state, new User())}
   */
  public EmailTokenResult(EmailTokenState state) {
    this(state, new User());
  }
  /** Returns the result. */
  public EmailTokenState getState() {
    return this.state_;
  }
  /**
   * Returns the user, if any.
   *
   * <p>The identified user is only valid when the token state is UpdatePassword or EmailConfirmed.
   * In that case, you may login the user as strongly authenticated since he presented a random
   * token that was sent to his own email address.
   */
  public User getUser() {
    if (this.user_.isValid()) {
      return this.user_;
    } else {
      throw new WException("EmailTokenResult::user() invalid");
    }
  }

  private EmailTokenState state_;
  private User user_;
}
