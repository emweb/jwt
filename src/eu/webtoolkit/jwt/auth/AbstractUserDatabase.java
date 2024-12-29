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
 * Abstract interface for an authentication user database.
 *
 * <p>This class defines the interface for managing user data related to authentication. You need to
 * implement this interface to allow the authentication service classes ({@link AuthService}, {@link
 * PasswordService}, {@link OAuthService}, and {@link OidcService}) to locate and update user
 * credentials. Except for functions which do work on a single user, it is more convenient to use
 * the {@link User} API. Obviously, you may have more data associated with a user, including roles
 * for access control, other personal information, address information. This information cannot be
 * accessed through the {@link User} class, but you should make it available through your own {@link
 * User} class, which is then als the basis of this user database implementation.
 *
 * <p>The only assumption made by the authentication system is that an id uniquely defines the user.
 * This is usually an internal identifier, for example an auto-incrementing primary key.
 *
 * <p>With a user, one or more other identities may be associated. These could be a login name (for
 * password-based authentication), or id&apos;s used by third party providers (such as OAuth or
 * LDAP).
 *
 * <p>The database implements a simple data store and does not contain any logic. The database can
 * store data for different aspects of authentication, but most data fields are only relevant for
 * optional functionality, and thus themeselves optional. The default implementation of these
 * methods will log errors.
 *
 * <p>The authentication views and model classes assume a private instance of the database for each
 * different session, and will try to wrap database access within a transaction. {@link Transaction}
 * support can thus be optionally provided by a database implementation.
 *
 * <p>This class is also used by OAuthAuthorizationEndpoint, {@link OAuthTokenEndpoint}, and {@link
 * OidcUserInfoEndpoint} when implementing an OAuth/OpenID Connect provider to retrieve information
 * not only about the {@link User}, but also the {@link OAuthClient}, and an {@link IssuedToken}.
 *
 * <p>
 *
 * @see User
 */
public abstract class AbstractUserDatabase {
  private static Logger logger = LoggerFactory.getLogger(AbstractUserDatabase.class);

  /**
   * An abstract transaction.
   *
   * <p>An abstract transaction interface.
   *
   * <p>
   *
   * @see AbstractUserDatabase#startTransaction()
   */
  public static interface Transaction extends AutoCloseable {
    /**
     * Commits the transaction.
     *
     * <p>
     *
     * @see AbstractUserDatabase.Transaction#rollback()
     */
    public void commit();
    /**
     * Rolls back the transaction.
     *
     * <p>
     *
     * @see AbstractUserDatabase.Transaction#commit()
     */
    public void rollback();
  }
  /**
   * Creates a new database transaction.
   *
   * <p>If the underlying database does not support transactions, you can return <code>null</code>.
   *
   * <p>Ownership of the transaction is transferred, and the transaction must be deleted after it
   * has been committed or rolled back.
   *
   * <p>The default implementation returns <code>null</code> (no transaction support).
   */
  public AbstractUserDatabase.Transaction startTransaction() {
    return null;
  }
  /**
   * Finds a user with a given id.
   *
   * <p>The id uniquely identifies a user.
   *
   * <p>This should find the user with the given <code>id</code>, or return an invalid user if no
   * user with that id exists.
   */
  public abstract User findWithId(final String id);
  /**
   * Finds a user with a given identity.
   *
   * <p>The <code>identity</code> uniquely identifies the user by the <code>provider</code>.
   *
   * <p>This should find the user with the given <code>identity</code>, or return an invalid user if
   * no user with that identity exists.
   */
  public abstract User findWithIdentity(final String provider, final String identity);
  /**
   * Adds an identify for the user.
   *
   * <p>This adds an identity to the user.
   *
   * <p>You are free to support only one identity per user, e.g. if you only use password-based
   * authentication. But you may also want to support more than one if you allow the user to login
   * using multiple methods (e.g. name/password, OAuth from one or more providers, LDAP, ...).
   */
  public abstract void addIdentity(final User user, final String provider, final String id);
  /**
   * Changes an identity for a user.
   *
   * <p>The base implementation calls {@link AbstractUserDatabase#removeIdentity(User user, String
   * provider) removeIdentity()} followed by {@link AbstractUserDatabase#addIdentity(User user,
   * String provider, String id) addIdentity()}.
   */
  public void setIdentity(final User user, final String provider, final String id) {
    this.removeIdentity(user, provider);
    this.addIdentity(user, provider, id);
  }
  /**
   * Returns a user identity.
   *
   * <p>Returns a user identity for the given provider, or an empty string if the user has no
   * identitfy set for this provider.
   *
   * <p>
   *
   * @see AbstractUserDatabase#addIdentity(User user, String provider, String id)
   */
  public abstract String getIdentity(final User user, final String provider);
  /**
   * Removes a user identity.
   *
   * <p>This removes all identities of a <code>provider</code> from the <code>user</code>.
   *
   * <p>
   *
   * @see AbstractUserDatabase#addIdentity(User user, String provider, String id)
   */
  public abstract void removeIdentity(final User user, final String provider);
  /**
   * Registers a new user.
   *
   * <p>This adds a new user.
   *
   * <p>This method is only used by view classes involved with registration ({@link
   * RegistrationWidget}).
   */
  public User registerNew() {
    logger.error(
        new StringWriter()
            .append(new Require("registerNew()", REGISTRATION).toString())
            .toString());
    return new User();
  }
  /**
   * Delete a user.
   *
   * <p>This deletes a user from the database.
   */
  public void deleteUser(final User user) {
    logger.error(
        new StringWriter().append(new Require("deleteUser()", REGISTRATION).toString()).toString());
  }
  /**
   * Returns the status for a user.
   *
   * <p>If there is support for suspending accounts, then this method may be implemented to return
   * whether a user account is disabled.
   *
   * <p>The default implementation always returns {@link AccountStatus#Normal}.
   *
   * <p>
   */
  public AccountStatus getStatus(final User user) {
    return AccountStatus.Normal;
  }
  /**
   * Sets the user status.
   *
   * <p>This sets the status for a user (if supported).
   */
  public void setStatus(final User user, AccountStatus status) {
    logger.error(new StringWriter().append(new Require("setStatus()").toString()).toString());
  }
  /**
   * Sets a new user password.
   *
   * <p>This updates the password for a user.
   *
   * <p>This is used only by {@link PasswordService}.
   */
  public void setPassword(final User user, final PasswordHash password) {
    logger.error(
        new StringWriter().append(new Require("setPassword()", PASSWORDS).toString()).toString());
  }
  /**
   * Returns a user password.
   *
   * <p>This returns the stored password for a user, or a default constructed password hash if the
   * user does not yet have password credentials.
   *
   * <p>This is used only by {@link PasswordService}.
   */
  public PasswordHash getPassword(final User user) {
    logger.error(
        new StringWriter().append(new Require("password()", PASSWORDS).toString()).toString());
    return new PasswordHash();
  }
  /**
   * Sets a user&apos;s email address.
   *
   * <p>This is used only when email verification is enabled, or as a result of a 3rd party {@link
   * Identity} Provider based registration process, if the provider also provides email address
   * information with the identiy.
   *
   * <p>Returns whether the user&apos;s email address could be set. This may fail when there is
   * already a user registered that email address.
   *
   * <p>
   *
   * @see AbstractUserDatabase#findWithEmail(String address)
   */
  public boolean setEmail(final User user, final String address) {
    logger.error(
        new StringWriter()
            .append(new Require("setEmail()", EMAIL_VERIFICATION).toString())
            .toString());
    return false;
  }
  /**
   * Returns a user&apos;s email address.
   *
   * <p>This may be an unverified or verified email address, depending on whether email address
   * verification is enabled in the model classes.
   *
   * <p>This is an optional method, and currently not used by any of the included models or views.
   */
  public String getEmail(final User user) {
    logger.error(
        new StringWriter()
            .append(new Require("email()", EMAIL_VERIFICATION).toString())
            .toString());
    return "";
  }
  /**
   * Sets a user&apos;s unverified email address.
   *
   * <p>This is only used when email verification is enabled. It holds the currently unverified
   * email address, while a mail is being sent for the user to confirm this email address.
   */
  public void setUnverifiedEmail(final User user, final String address) {
    logger.error(
        new StringWriter()
            .append(new Require("setUnverifiedEmail()", EMAIL_VERIFICATION).toString())
            .toString());
  }
  /**
   * Returns a user&apos;s unverified email address.
   *
   * <p>This is an optional method, and currently not used by any of the included models or views.
   */
  public String getUnverifiedEmail(final User user) {
    logger.error(
        new StringWriter()
            .append(new Require("unverifiedEmail()", EMAIL_VERIFICATION).toString())
            .toString());
    return "";
  }
  /**
   * Finds a user with a given email address.
   *
   * <p>This is used to verify that a email addresses are unique, and to implement lost password
   * functionality.
   */
  public User findWithEmail(final String address) {
    logger.error(
        new StringWriter()
            .append(new Require("findWithEmail()", EMAIL_VERIFICATION).toString())
            .toString());
    return new User();
  }
  /**
   * Sets a new email token for a user.
   *
   * <p>This is only used when email verification is enabled or for lost password functionality.
   */
  public void setEmailToken(final User user, final Token token, EmailTokenRole role) {
    logger.error(
        new StringWriter()
            .append(new Require("setEmailToken()", EMAIL_VERIFICATION).toString())
            .toString());
  }
  /**
   * Returns an email token.
   *
   * <p>This is only used when email verification is enabled and for lost password functionality. It
   * should return the email token previously set with {@link
   * AbstractUserDatabase#setEmailToken(User user, Token token, EmailTokenRole role)
   * setEmailToken()}
   */
  public Token getEmailToken(final User user) {
    logger.error(
        new StringWriter()
            .append(new Require("emailToken()", EMAIL_VERIFICATION).toString())
            .toString());
    return new Token();
  }
  /**
   * Returns the role of the current email token.
   *
   * <p>This is only used when email verification is enabled or for lost password functionality. It
   * should return the role previously set with setEailToken().
   */
  public EmailTokenRole getEmailTokenRole(final User user) {
    logger.error(
        new StringWriter()
            .append(new Require("emailTokenRole()", EMAIL_VERIFICATION).toString())
            .toString());
    return EmailTokenRole.VerifyEmail;
  }
  /**
   * Finds a user with a given email token.
   *
   * <p>This is only used when email verification is enabled or for lost password functionality.
   */
  public User findWithEmailToken(final String hash) {
    logger.error(
        new StringWriter()
            .append(new Require("findWithEmailToken()", EMAIL_VERIFICATION).toString())
            .toString());
    return new User();
  }
  /**
   * Adds an authentication token to a user.
   *
   * <p>An authentication token enables a user to not always type out their full username/password
   * (see {@link AuthWidget}) or enter their MFA code (see TotpProcess). An authentication token
   * will remember the user by placing a cookie in their browser and tracking the user&apos;s token
   * in a local table in the database (by default called &quot;auth_token&quot;).
   *
   * <p>The token itself is not aware of which use-case it serves. That being either for the regular
   * username/password login, or for MFA. However, in the browser this cookie is given a name, based
   * on the name set by {@link AuthService#setAuthTokensEnabled(boolean enabled, String cookieName,
   * String cookieDomain) AuthService#setAuthTokensEnabled()} or {@link
   * AuthService#setMfaTokenCookieName(String name) AuthService#setMfaTokenCookieName()}. This name
   * can be used to match to the correct type.
   *
   * <p>Enabling either of these (by calling {@link AuthService#setAuthTokensEnabled(boolean
   * enabled, String cookieName, String cookieDomain) AuthService#setAuthTokensEnabled()}) will also
   * enable the other, but their name and validity can be set separately.
   *
   * <p>
   *
   * @see AbstractUserDatabase#findWithAuthToken(String hash)
   *     <p><i><b>Note: </b>Unless you want a user to only have remember-me support from a single
   *     browser at a time, you should support multiple authentication tokens per user. </i>
   */
  public void addAuthToken(final User user, final Token token) {
    logger.error(
        new StringWriter().append(new Require("addAuthToken()", AUTH_TOKEN).toString()).toString());
  }
  /**
   * Deletes an authentication token.
   *
   * <p>Deletes an authentication token previously added with {@link
   * AbstractUserDatabase#addAuthToken(User user, Token token) addAuthToken()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This deletes the local entry in the database (in table
   * &quot;auth_token&quot;). It will not remove the cookie from the {@link User}&apos;s browser.
   * </i>
   */
  public void removeAuthToken(final User user, final String hash) {
    logger.error(
        new StringWriter()
            .append(new Require("removeAuthToken()", AUTH_TOKEN).toString())
            .toString());
  }
  /**
   * Finds a user with an authentication token.
   *
   * <p>Returns a user with an authentication token.
   *
   * <p>This should find the user associated with a particular token hash, or return an invalid user
   * if no user with that token hash exists.
   *
   * <p>The authentication token can be used for two means:
   *
   * <ul>
   *   <li>used for the normal authentication, denoting a regular username/password login. If the
   *       &quot;remember-me&quot; functionality is enabled for it, and selected, a token will be
   *       produced, named according to {@link AuthService#getAuthTokenCookieName()}, and valid for
   *       {@link AuthService#getAuthTokenValidity()} (in minutes). Both can be set by enabling
   *       authentication tokens with {@link AuthService#setAuthTokensEnabled(boolean enabled,
   *       String cookieName, String cookieDomain) AuthService#setAuthTokensEnabled()}. By default
   *       the cookie will be called &quot;wtauth&quot; and will be valid for two weeks.
   *   <li>used for the multi-factor verification, currently this is to be implemented by the
   *       developer if they want anything other than JWt&apos;s default of TOTP (see {@link
   *       TotpProcess}). This functions identical to the other authentication token, and is enabled
   *       the same way. The name can be changed by {@link AuthService#setMfaTokenCookieName(String
   *       name) AuthService#setMfaTokenCookieName()}, and its duration by {@link
   *       AuthService#setMfaTokenValidity(int validity) AuthService#setMfaTokenValidity()} (in
   *       minutes). By default the cookie will be called &quot;wtauth-mfa&quot; and it will be
   *       valid indefinitely.
   * </ul>
   */
  public User findWithAuthToken(final String hash) {
    logger.error(
        new StringWriter()
            .append(new Require("findWithAuthToken()", AUTH_TOKEN).toString())
            .toString());
    return new User();
  }
  /**
   * Updates the authentication token with a new hash.
   *
   * <p>If successful, returns the validity of the updated token in seconds.
   *
   * <p>Returns 0 if the token could not be updated because it wasn&apos;t found or is expired.
   *
   * <p>Returns -1 if not implemented.
   */
  public int updateAuthToken(final User user, final String hash, final String newHash) {
    logger.warn(
        new StringWriter()
            .append(new Require("updateAuthToken()", AUTH_TOKEN).toString())
            .toString());
    return -1;
  }
  /**
   * Sets the number of consecutive authentication failures.
   *
   * <p>This sets the number of consecutive authentication failures since the last valid login.
   *
   * <p>This is used by the throttling logic to determine how much time a user needs to wait before
   * he can do a new login attempt.
   */
  public void setFailedLoginAttempts(final User user, int count) {
    logger.error(
        new StringWriter()
            .append(new Require("setFailedLoginAttempts()", THROTTLING).toString())
            .toString());
  }
  /**
   * Returns the number of consecutive authentication failures.
   *
   * <p><i>{@link AbstractUserDatabase#setFailedLoginAttempts(User user, int count)
   * setFailedLoginAttempts()}</i>
   */
  public int getFailedLoginAttempts(final User user) {
    logger.error(
        new StringWriter()
            .append(new Require("failedLoginAttempts()", THROTTLING).toString())
            .toString());
    return 0;
  }
  /**
   * Sets the time of the last login attempt.
   *
   * <p>This sets the time at which the user attempted to login.
   */
  public void setLastLoginAttempt(final User user, final WDate timestamp) {
    logger.error(
        new StringWriter()
            .append(new Require("setLastLoginAttempt()", THROTTLING).toString())
            .toString());
  }
  /**
   * Returns the time of the last login.
   *
   * <p>
   *
   * @see AbstractUserDatabase#setLastLoginAttempt(User user, WDate timestamp)
   */
  public WDate getLastLoginAttempt(final User user) {
    logger.error(
        new StringWriter()
            .append(new Require("lastLoginAttempt()", THROTTLING).toString())
            .toString());
    return new WDate(1970, 1, 1);
  }
  /**
   * Returns the value of a claim for a user.
   *
   * <p>Should return a null Json value when the claim is unavailable.
   */
  public com.google.gson.JsonElement idpJsonClaim(final User user, final String claim) {
    logger.error(
        new StringWriter().append(new Require("idpClaim()", IDP_SUPPORT).toString()).toString());
    return com.google.gson.JsonNull.INSTANCE;
  }
  /** Adds a new {@link IssuedToken} to the database and returns it. S. */
  public IssuedToken idpTokenAdd(
      final String value,
      final WDate expirationTime,
      final String purpose,
      final String scope,
      final String redirectUri,
      final User user,
      final OAuthClient authClient) {
    logger.error(
        new StringWriter().append(new Require("idpTokenAdd()", IDP_SUPPORT).toString()).toString());
    return new IssuedToken();
  }
  /** Removes an issued token from the database. */
  public void idpTokenRemove(final IssuedToken token) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenRemove()", IDP_SUPPORT).toString())
            .toString());
  }
  /** Finds a token in the database with a given value. */
  public IssuedToken idpTokenFindWithValue(final String purpose, final String scope) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenFindWithValue()", IDP_SUPPORT).toString())
            .toString());
    return new IssuedToken();
  }
  /** Gets the expiration time for a token. */
  public WDate idpTokenExpirationTime(final IssuedToken token) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenExpirationTime)", IDP_SUPPORT).toString())
            .toString());
    return new WDate(1970, 1, 1);
  }
  /** Gets the value for a token. */
  public String idpTokenValue(final IssuedToken token) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenValue()", IDP_SUPPORT).toString())
            .toString());
    return "";
  }
  /** Gets the token purpose (authorization_code, access_token, id_token, refresh_token). */
  public String idpTokenPurpose(final IssuedToken token) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenPurpose()", IDP_SUPPORT).toString())
            .toString());
    return "";
  }
  /** Gets the scope associated with the token. */
  public String idpTokenScope(final IssuedToken token) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenScope()", IDP_SUPPORT).toString())
            .toString());
    return "";
  }
  /** Returns the redirect URI that was used with the token request. */
  public String idpTokenRedirectUri(final IssuedToken token) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenRedirectUri()", IDP_SUPPORT).toString())
            .toString());
    return "";
  }
  /** Returns the user associated with the token. */
  public User idpTokenUser(final IssuedToken token) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenUser()", IDP_SUPPORT).toString())
            .toString());
    return new User();
  }
  /** Returns the authorization client (relying party) that is associated with the token. */
  public OAuthClient idpTokenOAuthClient(final IssuedToken token) {
    logger.error(
        new StringWriter()
            .append(new Require("idpTokenOAuthClient()", IDP_SUPPORT).toString())
            .toString());
    return new OAuthClient();
  }
  /** Finds the authorization client (relying party) with this identifier. */
  public OAuthClient idpClientFindWithId(final String clientId) {
    logger.error(
        new StringWriter()
            .append(new Require("idpClientFindWithId()", IDP_SUPPORT).toString())
            .toString());
    return new OAuthClient();
  }
  /** Returns the secret for this client. */
  public String idpClientSecret(final OAuthClient client) {
    logger.error(
        new StringWriter()
            .append(new Require("idpClientSecret()", IDP_SUPPORT).toString())
            .toString());
    return "";
  }
  /** Returns true if the given secret is correct for the given client. */
  public boolean idpVerifySecret(final OAuthClient client, final String secret) {
    logger.error(
        new StringWriter()
            .append(new Require("idpVerifySecret()", IDP_SUPPORT).toString())
            .toString());
    return false;
  }
  /** Returns the redirect URI for this client. */
  public Set<String> idpClientRedirectUris(final OAuthClient client) {
    logger.error(
        new StringWriter()
            .append(new Require("idpClientRedirectUris()", IDP_SUPPORT).toString())
            .toString());
    return new HashSet<String>();
  }
  /** Returns the identifier for this client. */
  public String idpClientId(final OAuthClient client) {
    logger.error(
        new StringWriter().append(new Require("idpClientId()", IDP_SUPPORT).toString()).toString());
    return "";
  }
  /** Returns whether the client is confidential or public. */
  public boolean idpClientConfidential(final OAuthClient client) {
    logger.error(
        new StringWriter()
            .append(new Require("idpClientConfidential()", IDP_SUPPORT).toString())
            .toString());
    return false;
  }
  /** Returns the client authentication method (see OIDC Core chapter 9) */
  public ClientSecretMethod idpClientAuthMethod(final OAuthClient client) {
    logger.error(
        new StringWriter()
            .append(new Require("idpClientAuthMethod()", IDP_SUPPORT).toString())
            .toString());
    return ClientSecretMethod.HttpAuthorizationBasic;
  }
  /** Add a new client to the database and returns it. */
  public OAuthClient idpClientAdd(
      final String clientId,
      boolean confidential,
      final Set<String> redirectUris,
      ClientSecretMethod authMethod,
      final String secret) {
    logger.error(
        new StringWriter().append(new Require("idpTokenAdd()", IDP_SUPPORT).toString()).toString());
    return new OAuthClient();
  }

  protected AbstractUserDatabase() {}
  // private  AbstractUserDatabase(final AbstractUserDatabase anon1) ;
  private static String EMAIL_VERIFICATION = "email verification";
  private static String AUTH_TOKEN = "authentication tokens";
  private static String PASSWORDS = "password handling";
  private static String THROTTLING = "password attempt throttling";
  private static String REGISTRATION = "user registration";
  private static String IDP_SUPPORT = "identity provider support";
}
