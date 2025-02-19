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
 * A class that manages the current login state.
 *
 * <p>This is a model class which is typically associated with a single session, for the duration of
 * the session.
 *
 * <p>Widgets that implement authentication (and thus produce authentication changes), will indicate
 * their result in this object using the {@link Login#login(User user, LoginState state) login()} or
 * {@link Login#logout() logout()} methods.
 *
 * <p>Widgets that want to react to login state changes (typically, as user logging in or out)
 * should listen to the {@link Login#changed() changed()} signal of this object.
 *
 * <p>
 *
 * @see AuthWidget
 */
public class Login extends WObject {
  private static Logger logger = LoggerFactory.getLogger(Login.class);

  /**
   * Default constructor.
   *
   * <p>Creates a login object in the {@link LoginState#LoggedOut} state.
   */
  public Login() {
    super();
    this.changed_ = new Signal();
    this.user_ = new User();
    this.state_ = LoginState.LoggedOut;
  }
  /**
   * Logs a user in.
   *
   * <p>A user can be logged in using either a {@link LoginState#Disabled}, {@link LoginState#Weak}
   * or {@link LoginState#Strong} <code>state</code>. The login state is forced to {@link
   * LoginState#Disabled} if {@link User#getStatus()} returns Disabled.
   *
   * <p>
   *
   * @see Login#logout()
   * @see Login#isLoggedIn()
   */
  public void login(final User user, LoginState state) {
    if (state == LoginState.LoggedOut || !user.isValid()) {
      this.logout();
      return;
    } else {
      if (state != LoginState.Disabled && user.getStatus() == AccountStatus.Disabled) {
        state = LoginState.Disabled;
      }
      if (!user.equals(this.user_)) {
        this.user_ = user;
        this.state_ = state;
        this.changed_.trigger();
      } else {
        if (state != this.state_) {
          this.state_ = state;
          this.changed_.trigger();
        }
      }
    }
  }
  /**
   * Logs a user in.
   *
   * <p>Calls {@link #login(User user, LoginState state) login(user, LoginState.Strong)}
   */
  public final void login(final User user) {
    login(user, LoginState.Strong);
  }
  /**
   * Logs the current user out.
   *
   * <p>Sets the state to {@link LoginState#LoggedOut}.
   */
  public void logout() {
    if (this.user_.isValid()) {
      this.user_ = new User();
      this.state_ = LoginState.LoggedOut;
      this.changed_.trigger();
    }
  }
  /**
   * Returns the current login state.
   *
   * <p>
   *
   * @see Login#login(User user, LoginState state)
   * @see Login#logout()
   */
  public LoginState getState() {
    return this.state_;
  }
  /**
   * Returns whether a user has successfully logged in.
   *
   * <p>This returns <code>true</code> only if the user is valid and the state is {@link
   * LoginState#Weak} or {@link LoginState#Strong}.
   *
   * <p>In case the state is {@link LoginState#RequiresMfa}, the user still needs to go through the
   * MFA process before being logged in.
   *
   * <p>
   *
   * @see Login#getState()
   * @see User#isValid()
   */
  public boolean isLoggedIn() {
    return this.user_.isValid()
        && (this.state_ == LoginState.Strong || this.state_ == LoginState.Weak);
  }
  /**
   * Returns the user currently identified.
   *
   * <p>Returns the user currently identified.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This may also be a user whose account is currently disabled. </i>
   */
  public User getUser() {
    return this.user_;
  }
  /**
   * Signal that indicates login changes.
   *
   * <p>This signal is emitted as a result of {@link Login#login(User user, LoginState state)
   * login()} or {@link Login#logout() logout()}. If no user was logged in, then a {@link
   * Login#changed() changed()} signal does not necessarily mean that user is {@link
   * Login#isLoggedIn() isLoggedIn()} as the user may have been identified correctly but have a
   * {@link LoginState#Disabled} {@link Login#getState() getState()} for example.
   */
  public Signal changed() {
    return this.changed_;
  }

  private Signal changed_;
  private User user_;
  private LoginState state_;
}
