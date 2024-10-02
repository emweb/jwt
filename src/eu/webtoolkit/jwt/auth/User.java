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
 * A user.
 *
 * <p>This class represents a user. It is a value class that stores only the user id and a reference
 * to an {@link AbstractUserDatabase} to access its properties.
 *
 * <p>An object can point to a valid user, or be invalid. Invalid users are typically used as return
 * value for database queries which did not match with an existing user.
 *
 * <p>Not all methods are valid or applicable to your authentication system. See {@link
 * AbstractUserDatabase} for a discussion.
 *
 * <p>
 *
 * @see AbstractUserDatabase
 */
public class User {
  private static Logger logger = LoggerFactory.getLogger(User.class);

  /**
   * Default constructor.
   *
   * <p>Creates an invalid user.
   *
   * <p>
   *
   * @see User#isValid()
   */
  public User() {
    this.id_ = "";
    this.db_ = null;
  }
  /**
   * Constructor.
   *
   * <p>Creates a user with id <code>id</code>, and whose information is stored in the <code>
   * database</code>.
   */
  public User(final String id, final AbstractUserDatabase userDatabase) {
    this.id_ = id;
    this.db_ = userDatabase;
  }
  /**
   * Returns the user database.
   *
   * <p>This returns the user database passed in the constructor, or 0 if the user is invalid, and
   * was constructed using the default constructor.
   */
  public AbstractUserDatabase getDatabase() {
    return this.db_;
  }
  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * <p>Two users are equal if they have the same identity and the same database.
   */
  public boolean equals(final User other) {
    return this.id_.equals(other.id_) && this.db_ == other.db_;
  }
  /**
   * Returns whether the user is valid.
   *
   * <p>A invalid user is a sentinel value returned by methods that query the database but could not
   * identify a matching user.
   */
  public boolean isValid() {
    return this.db_ != null;
  }
  /**
   * Returns the user id.
   *
   * <p>This returns the id that uniquely identifies the user, and acts as a &quot;primary key&quot;
   * to obtain other information for the user in the database.
   *
   * <p>
   *
   * @see AbstractUserDatabase
   */
  public String getId() {
    return this.id_;
  }
  /** Returns an identity. */
  public String getIdentity(final String provider) {
    this.checkValid();
    return this.db_.getIdentity(this, provider);
  }
  /**
   * Adds an identity.
   *
   * <p>Depending on whether the database supports multiple identities per provider, this may change
   * (like {@link User#setIdentity(String provider, String identity) setIdentity()}), or add another
   * identity to the user. For some identity providers (e.g. a 3rd party identity provider), it may
   * be sensible to have more than one identity of the same provider for a single user (e.g.
   * multiple email accounts managed by the same provider, that in fact identify the same user).
   */
  public void addIdentity(final String provider, final String identity) {
    this.checkValid();
    this.db_.addIdentity(this, provider, identity);
  }
  /**
   * Sets an identity.
   *
   * <p>Unlike {@link User#addIdentity(String provider, String identity) addIdentity()} this
   * overrides any other identity of the given provider, in case the underlying database supports
   * multiple identities per user.
   */
  public void setIdentity(final String provider, final String identity) {
    this.checkValid();
    this.db_.setIdentity(this, provider, identity);
  }
  /**
   * Removes an identity.
   *
   * <p>
   *
   * @see User#addIdentity(String provider, String identity)
   */
  public void removeIdentity(final String provider) {
    this.checkValid();
    this.db_.removeIdentity(this, provider);
  }
  /**
   * Sets a password.
   *
   * <p>This also clears the email token.
   *
   * <p>
   *
   * @see AbstractUserDatabase#setPassword(User user, PasswordHash password)
   * @see User#clearEmailToken()
   */
  public void setPassword(final PasswordHash password) {
    this.checkValid();
    this.db_.setPassword(this, password);
    this.clearEmailToken();
  }
  /**
   * Returns the password.
   *
   * <p>
   *
   * @see AbstractUserDatabase#getPassword(User user)
   */
  public PasswordHash getPassword() {
    this.checkValid();
    return this.db_.getPassword(this);
  }
  /**
   * Sets the email address.
   *
   * <p>
   *
   * @see AbstractUserDatabase#setEmail(User user, String address)
   */
  public void setEmail(final String address) {
    this.checkValid();
    this.db_.setEmail(this, address);
  }
  /**
   * Returns the email address.
   *
   * <p>
   *
   * @see AbstractUserDatabase#getEmail(User user)
   */
  public String getEmail() {
    return this.db_.getEmail(this);
  }
  /**
   * Sets the unverified email address.
   *
   * <p>
   *
   * @see AbstractUserDatabase#setUnverifiedEmail(User user, String address)
   */
  public void setUnverifiedEmail(final String address) {
    this.checkValid();
    this.db_.setUnverifiedEmail(this, address);
  }
  /**
   * Returns the unverified email address.
   *
   * <p>
   *
   * @see AbstractUserDatabase#getUnverifiedEmail(User user)
   */
  public String getUnverifiedEmail() {
    this.checkValid();
    return this.db_.getUnverifiedEmail(this);
  }
  /**
   * Returns the account status.
   *
   * <p>
   *
   * @see AbstractUserDatabase#getStatus(User user)
   */
  public AccountStatus getStatus() {
    this.checkValid();
    return this.db_.getStatus(this);
  }
  /**
   * Sets the account status.
   *
   * <p>
   *
   * @see AbstractUserDatabase#setStatus(User user, AccountStatus status)
   */
  public void setStatus(AccountStatus status) {
    this.checkValid();
    this.db_.setStatus(this, status);
  }
  /**
   * Returns the email token.
   *
   * <p>
   *
   * @see AbstractUserDatabase#getEmailToken(User user)
   */
  public Token getEmailToken() {
    return this.db_.getEmailToken(this);
  }
  /**
   * Returns the email token role.
   *
   * <p>
   *
   * @see AbstractUserDatabase#getEmailTokenRole(User user)
   */
  public EmailTokenRole getEmailTokenRole() {
    return this.db_.getEmailTokenRole(this);
  }
  /**
   * Sets an email token.
   *
   * <p>
   *
   * @see AbstractUserDatabase#setEmailToken(User user, Token token, EmailTokenRole role)
   */
  public void setEmailToken(final Token token, EmailTokenRole role) {
    this.checkValid();
    this.db_.setEmailToken(this, token, role);
  }
  /**
   * Clears the email token.
   *
   * <p>
   *
   * @see User#setEmailToken(Token token, EmailTokenRole role)
   */
  public void clearEmailToken() {
    this.checkValid();
    this.db_.setEmailToken(this, new Token(), EmailTokenRole.LostPassword);
  }
  /**
   * Adds an authentication token.
   *
   * <p>
   *
   * @see AbstractUserDatabase#addAuthToken(User user, Token token)
   */
  public void addAuthToken(final Token token) {
    this.checkValid();
    this.db_.addAuthToken(this, token);
  }
  /**
   * Removes an authentication token.
   *
   * <p>
   *
   * @see AbstractUserDatabase#removeAuthToken(User user, String hash)
   */
  public void removeAuthToken(final String token) {
    this.checkValid();
    this.db_.removeAuthToken(this, token);
  }
  /**
   * Updates an authentication token.
   *
   * <p>
   *
   * @see AbstractUserDatabase#updateAuthToken(User user, String hash, String newHash)
   */
  public int updateAuthToken(final String hash, final String newHash) {
    this.checkValid();
    return this.db_.updateAuthToken(this, hash, newHash);
  }
  /**
   * Logs the result of an authentication attempt.
   *
   * <p>This changes the number of failed login attempts, and stores the current date as the last
   * login attempt time.
   *
   * <p>
   *
   * @see User#getFailedLoginAttempts()
   * @see User#getLastLoginAttempt()
   */
  public void setAuthenticated(boolean success) {
    this.checkValid();
    if (success) {
      this.db_.setFailedLoginAttempts(this, 0);
    } else {
      this.db_.setFailedLoginAttempts(this, this.db_.getFailedLoginAttempts(this) + 1);
    }
    this.db_.setLastLoginAttempt(this, WDate.getCurrentServerDate());
  }
  /**
   * Returns the number of consecutive unsuccessful login attempts.
   *
   * <p>
   *
   * @see User#setAuthenticated(boolean success)
   */
  public int getFailedLoginAttempts() {
    return this.db_.getFailedLoginAttempts(this);
  }
  /**
   * Returns the last login attempt.
   *
   * <p>
   *
   * @see User#setAuthenticated(boolean success)
   */
  public WDate getLastLoginAttempt() {
    return this.db_.getLastLoginAttempt(this);
  }

  private String id_;
  private AbstractUserDatabase db_;

  private void checkValid() {
    if (!(this.db_ != null)) {
      throw new WException("Method called on invalid Auth::User");
    }
  }
}
