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
 * An OpenId Connect authentication process.
 *
 * <p>The process implements the state machine that is needed to complete an OpenID Connect
 * authentication cycle.
 *
 * <p>A process is created for a particular scope, which represents what kind of information one
 * wants to access, and which is used to inform the user of the kind of operations he needs to
 * authorize for your application to make with his protected data.
 *
 * <p>
 *
 * @see OidcService#createProcess(String scope)
 * @see OAuthProcess
 */
public class OidcProcess extends OAuthProcess {
  private static Logger logger = LoggerFactory.getLogger(OidcProcess.class);

  public OidcProcess(final OidcService service, final String scope) {
    super(service, scope);
    this.httpClient_ = null;
  }
  /**
   * Starts an authorization and authentication process.
   *
   * <p>This is {@link OAuthProcess#startAuthorize()} followed by {@link
   * OidcProcess#getIdentity(OAuthAccessToken token) getIdentity()}.
   *
   * <p>The authentication process ends with the {@link OAuthProcess#authenticated()} signal which
   * signals the obtained identity.
   *
   * <p>
   *
   * <p><i><b>Note: </b>To be able to use a popup (instead of a page redirect), you should connect
   * this method directly to an, since popup windows are blocked in most web browsers unless they
   * are the direct consequence of an event. </i>
   */
  public void startAuthenticate() {
    super.startAuthenticate();
  }
  /**
   * Obtains an authenticated identity.
   *
   * <p>The authentication process will either use the ID token included with the access token or,
   * when this is not available, request the identity at the user info endpoint using claims.
   *
   * <p>The authentication process ends with the {@link OAuthProcess#authenticated()} signal which
   * signals the obtained identity.
   */
  public void getIdentity(final OAuthAccessToken token) {
    if (token.getIdToken().length() != 0) {
      Identity id = this.parseIdToken(token.getIdToken());
      if (id.isValid()) {
        this.authenticated().trigger(this.parseIdToken(token.getIdToken()));
        return;
      }
    }
    this.httpClient_ = new HttpClient();
    this.httpClient_.setTimeout(Duration.ofSeconds(15));
    this.httpClient_.setMaximumResponseSize(10 * 1024);
    this.httpClient_
        .done()
        .addListener(
            this,
            (Exception event1, HttpMessage event2) -> {
              OidcProcess.this.handleResponse(event1, event2);
            });
    List<org.apache.http.Header> headers = new ArrayList<org.apache.http.Header>();
    headers.add(
        new org.apache.http.message.BasicHeader("Authorization", "Bearer " + token.getValue()));
    this.httpClient_.get(this.getService().getUserInfoEndpoint(), headers);
  }

  private void handleResponse(Exception err, final HttpMessage response) {
    if (err == null && response.getStatus() == 200) {
      logger.info(new StringWriter().append("user info: ").append(response.getBody()).toString());
      com.google.gson.JsonObject userInfo = new com.google.gson.JsonObject();
      try {
        userInfo =
            (com.google.gson.JsonObject) new com.google.gson.JsonParser().parse(response.getBody());
      } catch (com.google.gson.JsonParseException pe) {
      }
      boolean ok = userInfo != null;
      if (!ok) {
        logger.error(
            new StringWriter()
                .append("could not parse Json: '")
                .append(response.getBody())
                .append("'")
                .toString());
        this.setError(WString.tr("Wt.Auth.OidcService.badjson"));
        this.authenticated().trigger(Identity.Invalid);
      } else {
        this.authenticated().trigger(this.parseClaims(userInfo));
      }
    } else {
      logger.error(
          new StringWriter().append(WString.tr("Wt.Auth.OidcService.badresponse")).toString());
      this.setError(WString.tr("Wt.Auth.OidcService.badresponse"));
      if (err == null) {
        logger.error(
            new StringWriter()
                .append("user info request returned: ")
                .append(String.valueOf(response.getStatus()))
                .toString());
        logger.error(new StringWriter().append("with: ").append(response.getBody()).toString());
      }
      this.authenticated().trigger(Identity.Invalid);
    }
    WApplication.getInstance().triggerUpdate();
    WApplication.getInstance().enableUpdates(false);
  }

  private Identity parseIdToken(final String idToken) {
    List<String> parts = new ArrayList<String>();
    StringUtils.split(parts, idToken, ".", false);
    if (parts.size() != 3) {
      logger.error(
          new StringWriter()
              .append("malformed id_token: '")
              .append(idToken)
              .append("'")
              .toString());
      return Identity.Invalid;
    }
    com.google.gson.JsonObject payloadJson = new com.google.gson.JsonObject();
    try {
      payloadJson =
          (com.google.gson.JsonObject)
              new com.google.gson.JsonParser().parse(Utils.base64DecodeS(parts.get(1)));
    } catch (com.google.gson.JsonParseException pe) {
    }
    boolean ok = payloadJson != null;
    if (!ok) {
      logger.error(
          new StringWriter()
              .append("could not parse Json: '")
              .append(parts.get(1))
              .append("'")
              .toString());
      return Identity.Invalid;
    }
    return this.parseClaims(payloadJson);
  }

  private Identity parseClaims(final com.google.gson.JsonObject claims) {
    String id = JsonUtils.orIfNullString(claims.get("sub"), "");
    String name = JsonUtils.orIfNullString(claims.get("name"), "");
    String email = JsonUtils.orIfNullString(claims.get("email"), "");
    boolean emailVerified = JsonUtils.orIfNullBoolean(claims.get("email_verified"), false);
    String providerName = this.getService().getName();
    return new Identity(providerName, id, name, email, emailVerified);
  }

  private HttpClient httpClient_;
}
