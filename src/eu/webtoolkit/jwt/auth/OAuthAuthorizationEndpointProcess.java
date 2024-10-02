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
 * Allows clients to authorize users according to the OAuth 2.0 protocol.
 *
 * <p>This class will process the environment and perform the authorization of the user if this is
 * possible. If this is successful, an authorization code will be sent to the client.
 *
 * <p>The following URL parameters are expected: &quot;client_id&quot;, which obviously has to
 * contain a valid client ID. &quot;redirect_uri&quot;, which has to be a valid redirect URI where
 * the user will be redirected to when the authorization has been succesful. &quot;scope&quot;,
 * which has to be set to the scope of the requested information. &quot;response_type&quot;, which
 * has to be set to &quot;code&quot;. If the &quot;state&quot; parameter has been included, it will
 * be passed on as a paremeter to the redirect URI.
 *
 * <p>When the client ID and the redirect URI is valid but something else went wrong, an
 * &quot;error=invalid_request&quot; will be sent to the client. If the user failed to log in
 * correctly &quot;error=login_required&quot; will be sent. If everything went OK, the
 * &quot;code&quot; parameter is included which can be used to obtain a token from a token endpoint.
 *
 * <p>See <a
 * href="https://tools.ietf.org/rfc/rfc6749.txt">https://tools.ietf.org/rfc/rfc6749.txt</a> for more
 * information.
 *
 * <p>This class relies on a correct implementation of several function in the {@link
 * AbstractUserDatabase}. Namely {@link AbstractUserDatabase#idpClientFindWithId(String clientId)
 * AbstractUserDatabase#idpClientFindWithId()}, {@link AbstractUserDatabase#idpTokenAdd(String
 * value, WDate expirationTime, String purpose, String scope, String redirectUri, User user,
 * OAuthClient authClient) AbstractUserDatabase#idpTokenAdd()}, and {@link
 * AbstractUserDatabase#idpClientRedirectUris(OAuthClient client)
 * AbstractUserDatabase#idpClientRedirectUris()}.
 *
 * <p>Must be deployed with TLS.
 *
 * <p>Example:
 *
 * <pre>{@code
 * process = std::make_unique<OAuthAuthorizationEndpointProcess>(
 * login,
 * database);
 * process.authorized().connect(
 * process.get(),
 * &OAuthAuthorizationEndpointProcess::authorizeScope);
 * process.processEnvironment();
 * if (process.validRequest()) {
 * root().addWidget(std::move(authWidget));
 * } else
 * root().addWidget(std::make_unique<Wt::WText>(Wt::utf8("The request was invalid."));
 *
 * }</pre>
 *
 * <p>
 *
 * @see OAuthTokenEndpoint
 * @see AbstractUserDatabase
 */
public class OAuthAuthorizationEndpointProcess extends WObject {
  private static Logger logger = LoggerFactory.getLogger(OAuthAuthorizationEndpointProcess.class);

  /** Constructor. */
  public OAuthAuthorizationEndpointProcess(final Login login, final AbstractUserDatabase db) {
    super();
    this.db_ = db;
    this.authCodeExpSecs_ = 600;
    this.redirectUri_ = "";
    this.state_ = "";
    this.scope_ = "";
    this.client_ = new OAuthClient();
    this.validRequest_ = false;
    this.login_ = login;
    this.authorized_ = new Signal1<String>();
  }
  /**
   * Processes the environment and authorizes the user when already logged in.
   *
   * <p>The {@link OAuthAuthorizationEndpointProcess#authorized() authorized()} signal should be
   * connected before calling this function.
   */
  public void processEnvironment() {
    final WEnvironment env = WApplication.getInstance().getEnvironment();
    String redirectUri = env.getParameter("redirect_uri");
    if (!(redirectUri != null)) {
      logger.error(
          new StringWriter()
              .append("The client application did not pass a redirection URI.")
              .toString());
      return;
    }
    this.redirectUri_ = redirectUri;
    String clientId = env.getParameter("client_id");
    if (!(clientId != null)) {
      logger.error(new StringWriter().append("Missing client_id parameter.").toString());
      return;
    }
    this.client_ = this.db_.idpClientFindWithId(clientId);
    if (!this.client_.isCheckValid()) {
      logger.error(
          new StringWriter().append("Unknown or invalid client_id ").append(clientId).toString());
      return;
    }
    Set<String> redirectUris = this.client_.getRedirectUris();
    if (redirectUris.contains(this.redirectUri_) == false) {
      logger.error(
          new StringWriter()
              .append("The client application passed the  unregistered redirection URI ")
              .append(this.redirectUri_)
              .toString());
      return;
    }
    String scope = env.getParameter("scope");
    String responseType = env.getParameter("response_type");
    String state = env.getParameter("state");
    if (!(scope != null) || !(responseType != null) || !responseType.equals("code")) {
      this.sendResponse("error=invalid_request");
      logger.info(
          new StringWriter()
              .append("error=invalid_request: ")
              .append(" scope: ")
              .append(scope != null ? scope : "NULL")
              .append(" response_type: ")
              .append(responseType != null ? responseType : "NULL")
              .toString());
      return;
    }
    this.validRequest_ = true;
    this.scope_ = scope;
    if (state != null) {
      this.state_ = state;
    }
    this.login_
        .changed()
        .addListener(
            this,
            () -> {
              OAuthAuthorizationEndpointProcess.this.authEvent();
            });
    String prompt = WApplication.getInstance().getEnvironment().getParameter("prompt");
    if (this.login_.isLoggedIn()) {
      this.authorized_.trigger(this.scope_);
      return;
    } else {
      if (prompt != null && prompt.equals("none")) {
        this.sendResponse("error=login_required");
        logger.info(
            new StringWriter().append("error=login_required but prompt == none").toString());
        return;
      }
    }
  }
  /** Returns true if the request was a valid OAuth request with the correct parameters. */
  public boolean isValidRequest() {
    return this.validRequest_;
  }
  /**
   * This signal is emitted when the user has successfully logged in.
   *
   * <p>When the user has successfully logged in and the request is valid, this signal will be
   * emitted and the user can be redirected to the redirect URI using authorizeScope.
   *
   * <p>This signal supplies the scope as argument.
   */
  public Signal1<String> authorized() {
    return this.authorized_;
  }
  /**
   * Authorize the given scope and redirect the user.
   *
   * <p>If the user has successfully logged in this function will redirect the user to the redirect
   * URI with a valid &quot;code&quot; parameter which is only valid for the given scope.
   */
  public void authorizeScope(final String scope) {
    if (this.validRequest_ && this.login_.isLoggedIn()) {
      String authCodeValue = MathUtils.randomId();
      WDate expirationTime = WDate.getCurrentServerDate().addSeconds(this.authCodeExpSecs_);
      this.db_.idpTokenAdd(
          authCodeValue,
          expirationTime,
          "authorization_code",
          scope,
          this.redirectUri_,
          this.login_.getUser(),
          this.client_);
      this.sendResponse("code=" + authCodeValue);
      logger.info(
          new StringWriter()
              .append("authorization_code created for ")
              .append(this.login_.getUser().getId())
              .append("(")
              .append(this.login_.getUser().getEmail())
              .append(")")
              .append(", code = " + authCodeValue)
              .toString());
    } else {
      throw new WException(
          "Wt::Auth::OAuthAuthorizationEndpointProcess::authorizeScope: request isn't valid");
    }
  }
  /**
   * Sets the amount of seconds after which generated authorization codes expire.
   *
   * <p>This defaults to 600 seconds.
   */
  public void setAuthCodeExpSecs(int seconds) {
    this.authCodeExpSecs_ = seconds;
  }

  protected AbstractUserDatabase db_;

  protected void authEvent() {
    if (this.login_.isLoggedIn()) {
      this.authorized_.trigger(this.scope_);
    } else {
      this.sendResponse("error=login_required");
    }
  }

  private void sendResponse(final String param) {
    String redirectParam = this.redirectUri_.indexOf("?") != -1 ? "&" : "?";
    redirectParam += param;
    if (this.state_.length() != 0) {
      redirectParam += "&state=" + Utils.urlEncode(this.state_);
    }
    WApplication.getInstance().redirect(this.redirectUri_ + redirectParam);
    WApplication.getInstance().quit();
  }

  private int authCodeExpSecs_;
  private String redirectUri_;
  private String state_;
  private String scope_;
  private OAuthClient client_;
  private boolean validRequest_;
  private final Login login_;
  private Signal1<String> authorized_;
}
