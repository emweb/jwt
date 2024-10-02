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
 * {@link Token} or authorization code that was issued to a relying party.
 *
 * <p>This class represents an access token. It is a value class that stores only the id and a
 * reference to an {@link AbstractUserDatabase} to access its properties.
 *
 * <p>An object can point to a valid token, or be invalid. Invalid tokens are typically used as
 * return value for database queries which did not match with an existing client.
 *
 * <p>
 *
 * @see AbstractUserDatabase
 */
public class IssuedToken {
  private static Logger logger = LoggerFactory.getLogger(IssuedToken.class);

  /**
   * Default constructor.
   *
   * <p>Creates an invalid token.
   *
   * <p>
   *
   * @see IssuedToken#isCheckValid()
   */
  public IssuedToken() {
    this.id_ = "";
    this.db_ = null;
  }
  /**
   * Constructor.
   *
   * <p>Creates a user with id <code>id</code>, and whose information is stored in the <code>
   * database</code>.
   */
  public IssuedToken(final String id, final AbstractUserDatabase userDatabase) {
    this.id_ = id;
    this.db_ = userDatabase;
  }
  /**
   * Returns whether the token is valid.
   *
   * <p>A invalid token is a sentinel value returned by methods that query the database but could
   * not identify a matching user.
   */
  public boolean isCheckValid() {
    return this.db_ != null;
  }
  /**
   * Returns the user id.
   *
   * <p>This returns the id that uniquely identifies the token, and acts as a &quot;primary
   * key&quot; to obtain other information for the token in the database.
   *
   * <p>
   *
   * @see AbstractUserDatabase
   */
  public String getId() {
    return this.id_;
  }
  /** Retrieves the string value that represents this token, usually random characters. */
  public String getValue() {
    if (this.db_ != null) {
      return this.db_.idpTokenValue(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /** Retrieves the time when the token expires. */
  public WDate getExpirationTime() {
    if (this.db_ != null) {
      return this.db_.idpTokenExpirationTime(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /** Retrieves the purpose of this token: authenication code, access token or refresh token. */
  public String getPurpose() {
    if (this.db_ != null) {
      return this.db_.idpTokenPurpose(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /** Retrieves the scope of this token as a space-separated string. */
  public String getScope() {
    if (this.db_ != null) {
      return this.db_.idpTokenScope(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /** Retrieves the valid redirect uri of this token. */
  public String getRedirectUri() {
    if (this.db_ != null) {
      return this.db_.idpTokenRedirectUri(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /** Retrieves the user that is associated with this token. */
  public User getUser() {
    if (this.db_ != null) {
      return this.db_.idpTokenUser(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /** Retrieves the client for which this token was issued. */
  public OAuthClient getAuthClient() {
    if (this.db_ != null) {
      return this.db_.idpTokenOAuthClient(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }

  private String id_;
  private AbstractUserDatabase db_;
  private static String INVALID_ERROR = "Wt::Auth::IssuedToken invalid";
}
