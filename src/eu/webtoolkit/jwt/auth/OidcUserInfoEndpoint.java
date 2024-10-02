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
 * Endpoint at which user info can be requested.
 *
 * <p>The UserInfo Endpoint is an OAuth 2.0 Protected Resource that returns Claims about the
 * authenticated End-User. To obtain the requested Claims about the End-User, the Client makes a
 * request to the UserInfo Endpoint using an Access {@link Token} obtained through OpenID Connect
 * Authentication. These Claims are normally represented by a JSON object that contains a collection
 * of name and value pairs for the Claims.
 *
 * <p>One can use setScopeToken to map claims to a scopeToken. The value of these claims will be
 * retrieved using the {@link AbstractUserDatabase#idpJsonClaim(User user, String claim)
 * AbstractUserDatabase#idpJsonClaim()} function.
 *
 * <p>You can look at <a
 * href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfo">http://openid.net/specs/openid-connect-core-1_0.html#UserInfo</a>
 * for more information.
 *
 * <p>This endpoint is implemented as a {@link WResource}, so it&apos;s usually deployed using
 * {@link WServer#addResource(WResource resource, String path) WServer#addResource()}.
 *
 * <p>This class relies on the implementation of several functions in the {@link
 * AbstractUserDatabase}. Namely {@link AbstractUserDatabase#idpJsonClaim(User user, String claim)
 * AbstractUserDatabase#idpJsonClaim()}, {@link AbstractUserDatabase#idpTokenFindWithValue(String
 * purpose, String scope) AbstractUserDatabase#idpTokenFindWithValue()}, {@link
 * AbstractUserDatabase#idpTokenUser(IssuedToken token) AbstractUserDatabase#idpTokenUser()}, and
 * {@link AbstractUserDatabase#idpTokenScope(IssuedToken token)
 * AbstractUserDatabase#idpTokenScope()}.
 *
 * <p>Must be deployed using TLS.
 *
 * <p>
 *
 * @see OidcUserInfoEndpoint#setScopeToken(String scopeToken, Set claims)
 * @see AbstractUserDatabase
 */
public class OidcUserInfoEndpoint extends WResource {
  private static Logger logger = LoggerFactory.getLogger(OidcUserInfoEndpoint.class);

  /** Constructor. */
  public OidcUserInfoEndpoint(final AbstractUserDatabase db) {
    super();
    this.db_ = db;
    this.claimMap_ = new HashMap<String, Set<String>>();
    Set<String> s1 = new HashSet<String>();
    s1.add("name");
    this.setScopeToken("profile", s1);
    Set<String> s2 = new HashSet<String>();
    s2.add("email");
    s2.add("email_verified");
    this.setScopeToken("email", s2);
  }

  public void handleRequest(final WebRequest request, final WebResponse response) {
    String authHeader = request.getHeaderValue("Authorization");
    if (!authHeader.startsWith(AUTH_TYPE)) {
      response.setStatus(400);
      response.addHeader("WWW-Authenticate", "error=\"invalid_request\"");
      logger.info(
          new StringWriter()
              .append("error=\"invalid_request\": Authorization header missing")
              .toString());
      return;
    }
    String tokenValue = authHeader.substring(AUTH_TYPE.length());
    IssuedToken accessToken = this.db_.idpTokenFindWithValue("access_token", tokenValue);
    if (!accessToken.isCheckValid()
        || WDate.getCurrentServerDate().after(accessToken.getExpirationTime())) {
      response.setStatus(401);
      response.addHeader("WWW-Authenticate", "error=\"invalid_token\"");
      logger.info(
          new StringWriter().append("error=\"invalid_token\" ").append(authHeader).toString());
      return;
    }
    response.setContentType("application/json");
    response.setStatus(200);
    User user = accessToken.getUser();
    String scope = accessToken.getScope();
    Set<String> scopeSet = new HashSet<String>();
    StringUtils.split(scopeSet, scope, " ", false);
    try {
      response.out().append(this.generateUserInfo(user, scopeSet).toString()).append('\n');
      logger.info(
          new StringWriter()
              .append("Response sent for ")
              .append(user.getId())
              .append("(")
              .append(this.db_.getEmail(user))
              .append(")")
              .toString());
    } catch (IOException ioe) {
      logger.error(new StringWriter().append(ioe.getMessage()).toString());
    }
  }
  /**
   * Maps the given scope token to the given set of claims.
   *
   * <p>The value of these claims will be retrieved from the {@link AbstractUserDatabase} using the
   * {@link AbstractUserDatabase#idpJsonClaim(User user, String claim)
   * AbstractUserDatabase#idpJsonClaim()} function.
   *
   * <p>At construction, the following default scopes are automatically populated: profile . {name}
   * and email . {email, email_verified}
   *
   * <p>A scope can be erased by setting it to an empty set of claims.
   *
   * <p>
   *
   * @see AbstractUserDatabase#idpJsonClaim(User user, String claim)
   */
  public void setScopeToken(final String scopeToken, final Set<String> claims) {
    this.claimMap_.put(scopeToken, claims);
  }
  /** Retrieves the set of claims that has been mapped to the given scope token. */
  public Map<String, Set<String>> getScopeTokens() {
    return this.claimMap_;
  }
  /**
   * Generates the JSON containing the claims for the given scope.
   *
   * <p>Can be overridden, but by default it uses the configured mapping set by setScopeToken, and
   * {@link AbstractUserDatabase#idpJsonClaim(User user, String claim)
   * AbstractUserDatabase#idpJsonClaim()}.
   *
   * <p>
   *
   * @see AbstractUserDatabase#idpJsonClaim(User user, String claim)
   */
  protected com.google.gson.JsonObject generateUserInfo(final User user, final Set<String> scope) {
    com.google.gson.JsonObject root = new com.google.gson.JsonObject();
    root.add("sub", (new com.google.gson.JsonPrimitive(user.getId())));
    Set<String> claims = new HashSet<String>();
    for (Iterator<String> s_it = scope.iterator(); s_it.hasNext(); ) {
      String s = s_it.next();
      Set<String> it = this.claimMap_.get(s);
      if (it == null) {
        continue;
      }
      final Set<String> c = it;
      for (Iterator<String> s2_it = c.iterator(); s2_it.hasNext(); ) {
        String s2 = s2_it.next();
        claims.add(s2);
      }
    }
    for (Iterator<String> claim_it = claims.iterator(); claim_it.hasNext(); ) {
      String claim = claim_it.next();
      com.google.gson.JsonElement claimValue = this.db_.idpJsonClaim(user, claim);
      if (!JsonUtils.isNull(claimValue)) {
        root.add(claim, claimValue);
      }
    }
    return root;
  }

  private AbstractUserDatabase db_;
  private Map<String, Set<String>> claimMap_;
  private static final String AUTH_TYPE = "Bearer ";
}
