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
 * OAuth 2.0 client implementing OpenID Connect, a.k.a. relying party.
 *
 * <p>This class represents a client. It is a value class that stores only the id and a reference to
 * an {@link AbstractUserDatabase} to access its properties.
 *
 * <p>An object can point to a valid client, or be invalid. Invalid clients are typically used as
 * return value for database queries which did not match with an existing client.
 *
 * <p>
 *
 * @see AbstractUserDatabase
 */
public class OAuthClient {
  private static Logger logger = LoggerFactory.getLogger(OAuthClient.class);

  /**
   * Default constructor that creates an invalid {@link OAuthClient}.
   *
   * <p>
   *
   * @see OAuthClient#isCheckValid()
   */
  public OAuthClient() {
    this.db_ = null;
    this.id_ = "";
  }
  /**
   * Constructor.
   *
   * <p>Creates a client with id <code>id</code>, and whose information is stored in the <code>
   * database</code>.
   */
  public OAuthClient(final String id, final AbstractUserDatabase db) {
    this.db_ = db;
    this.id_ = id;
  }
  /**
   * Returns whether the user is valid.
   *
   * <p>A invalid user is a sentinel value returned by methods that query the database but could not
   * identify a matching user.
   */
  public boolean isCheckValid() {
    return this.db_ != null;
  }
  /**
   * Returns the ID used to identify the client in the database.
   *
   * <p>This returns the id that uniquely identifies the user, and acts as a &quot;primary key&quot;
   * to obtain other information for the user in the database.
   */
  public String getId() {
    return this.id_;
  }
  /**
   * Returns the ID used to identify the client with the OpenID Connect provider and user.
   *
   * <p>This is the id that the client uses to identify itself with the identity provider.
   *
   * <p>
   *
   * @see AbstractUserDatabase#idpClientId(OAuthClient client)
   */
  public String getClientId() {
    if (this.db_ != null) {
      return this.db_.idpClientId(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /** Returns true if the given secret is correct for the given client. */
  public boolean verifySecret(final String secret) {
    if (this.db_ != null) {
      return this.db_.idpVerifySecret(this, secret);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /**
   * Returns the set of redirect URI&apos;s that are valid for this client.
   *
   * <p>
   *
   * @see AbstractUserDatabase#idpClientRedirectUris(OAuthClient client)
   */
  public Set<String> getRedirectUris() {
    if (this.db_ != null) {
      return this.db_.idpClientRedirectUris(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /**
   * Returns whether the client is confidential or public.
   *
   * <p>
   *
   * @see AbstractUserDatabase#idpClientConfidential(OAuthClient client)
   */
  public boolean isConfidential() {
    if (this.db_ != null) {
      return this.db_.idpClientConfidential(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }
  /**
   * Returns the client authentication method (see OIDC Core chapter 9)
   *
   * <p>
   *
   * @see AbstractUserDatabase#idpClientAuthMethod(OAuthClient client)
   */
  public ClientSecretMethod getAuthMethod() {
    if (this.db_ != null) {
      return this.db_.idpClientAuthMethod(this);
    } else {
      throw new WException(INVALID_ERROR);
    }
  }

  private AbstractUserDatabase db_;
  private String id_;
  private static String INVALID_ERROR = "Wt::Auth::OAuthClient invalid";
}
