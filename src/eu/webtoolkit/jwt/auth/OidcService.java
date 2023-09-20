/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.*;
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
 * An OpenId Connect authentication service provider.
 *
 * <p>This class implements an OpenID Connect client (<a
 * href="http://openid.net/specs/openid-connect-core-1_0.html">core specification</a>), which can be
 * used to allow the user to be safely authenticated with your web application without needing to
 * store or even handle his authorization credentials (such as a password).
 *
 * <p>OpenID Connect is a simple identity layer on top of the OAuth 2.0 protocol. It enables Clients
 * to verify the identity of the End-User based on the authentication performed by an Authorization
 * Server, as well as to obtain basic profile information about the End-User in an interoperable and
 * REST-like manner.
 *
 * <p>This implementation only supports authentication using the Authorization Code Flow.
 *
 * <p>The configuration of this service is done by using the setters the service class exposes.
 * Before the authentication process can be started these settings must be configured first and may
 * not be changed afterwards.
 *
 * <p>The OpenID Connect protocol, including the subsequent use for authentication, consists of a
 * number of consecutive steps, some of which require user interaction, and some which require the
 * use of remote web services. The state machine for this process is implemented in an {@link
 * OidcProcess}. To use OpenID Connect, you need to create such a process and listen for state
 * changes.
 */
public class OidcService extends OAuthService {
  private static Logger logger = LoggerFactory.getLogger(OidcService.class);

  /** Constructor. */
  public OidcService(final AuthService baseAuth) {
    super(baseAuth);
    this.redirectEndpoint_ = "";
    this.authorizationEndpoint_ = "";
    this.tokenEndpoint_ = "";
    this.userInfoEndpoint_ = "";
    this.clientId_ = "";
    this.clientSecret_ = "";
    this.name_ = "";
    this.description_ = "";
    this.scope_ = "openid";
    this.popupWidth_ = 670;
    this.popupHeight_ = 400;
    this.method_ = ClientSecretMethod.HttpAuthorizationBasic;
    this.configured_ = false;
  }
  /**
   * Returns the provider name.
   *
   * <p>This is a short identifier.
   *
   * <p>
   *
   * @see OidcService#getDescription()
   * @see OidcService#setName(String name)
   */
  public String getName() {
    return this.name_;
  }
  /**
   * Returns the provider description.
   *
   * <p>This returns a description useful for e.g. tool tips on a login icon.
   *
   * <p>
   *
   * @see OidcService#getName()
   * @see OidcService#setDescription(String description)
   */
  public WString getDescription() {
    return new WString(this.description_);
  }
  /**
   * Returns the desired width for the popup window.
   *
   * <p>Defaults to 670 pixels.
   *
   * <p>
   *
   * @see OidcService#setPopupWidth(int width)
   */
  public int getPopupWidth() {
    return this.popupWidth_;
  }
  /**
   * Returns the desired height of the popup window.
   *
   * <p>Defaults to 400 pixels.
   *
   * <p>
   *
   * @see OidcService#setPopupHeight(int height)
   */
  public int getPopupHeight() {
    return this.popupHeight_;
  }
  /**
   * Returns the scope needed for authentication.
   *
   * <p>This returns the scope that is needed (and sufficient) for obtaining identity information,
   * and thus to authenticate the user.
   *
   * <p>This defaults to &quot;openid&quot;.
   *
   * <p>
   *
   * @see OidcProcess#startAuthenticate()
   * @see OidcService#createProcess(String scope)
   * @see OidcService#setAuthenticationScope(String scope)
   */
  public String getAuthenticationScope() {
    return this.scope_;
  }
  /**
   * Returns the redirection endpoint URL.
   *
   * <p>This is the local URL to which the browser is redirect from the service provider, after the
   * authorization process. You need to configure this URL with the third party authentication
   * service.
   *
   * <p>A static resource will be deployed at this URL.
   *
   * <p>
   *
   * @see OidcService#setRedirectEndpoint(String url)
   */
  public String getRedirectEndpoint() {
    return this.redirectEndpoint_;
  }
  /**
   * Returns the authorization endpoint URL.
   *
   * <p>This is a remote URL which hosts the OpenID Connect authorization user interface. This URL
   * is loaded in the popup window at the start of an authorization process.
   *
   * <p>
   *
   * @see OidcService#setAuthEndpoint(String url)
   */
  public String getAuthorizationEndpoint() {
    return this.authorizationEndpoint_;
  }
  /**
   * Returns the token endpoint URL.
   *
   * <p>This is a remote URL which hosts a web-service that generates access and id tokens.
   *
   * <p>
   *
   * @see OidcService#setTokenEndpoint(String url)
   */
  public String getTokenEndpoint() {
    return this.tokenEndpoint_;
  }
  /**
   * Returns the user info endpoint URL.
   *
   * <p>This is a remote URL which hosts a web-service that provides the claims that are associated
   * with the requested scope.
   *
   * <p>
   *
   * @see OidcService#setTokenEndpoint(String url)
   */
  public String getUserInfoEndpoint() {
    return this.userInfoEndpoint_;
  }
  /**
   * Returns the client ID.
   *
   * <p>This is the identification for this web application with the OpenID Connect provider.
   *
   * <p>
   *
   * @see OidcService#setClientId(String id)
   */
  public String getClientId() {
    return this.clientId_;
  }
  /**
   * Returns the client secret.
   *
   * <p>This is the secret credentials for this web application with the OpenID Connect provider.
   *
   * <p>
   *
   * @see OidcService#setClientSecret(String secret)
   */
  public String getClientSecret() {
    return this.clientSecret_;
  }
  /**
   * Returns the method to transfer the client secret.
   *
   * <p>The default implementation returns HttpAuthorizationBasic (the recommended method).
   */
  public ClientSecretMethod getClientSecretMethod() {
    return this.method_;
  }
  /**
   * Creates a new authentication process.
   *
   * <p>This creates a new authentication process for the indicated scope. Valid names for the scope
   * are service provider dependent.
   *
   * <p>The service needs to be correctly configured before being able to call this function.
   * configure() needs to be called first to check if the configuration is valid.
   *
   * <p>
   *
   * <p><i><b>Note: </b>The returned process will be an instance of {@link OidcService} </i>
   *
   * @see OidcService#getAuthenticationScope()
   */
  public OAuthProcess createProcess(final String scope) {
    if (this.configured_) {
      return new OidcProcess(this, scope);
    } else {
      throw new WException("OidcService not configured correctly");
    }
  }
  /** Sets the redirection endpoint URL. */
  public void setRedirectEndpoint(final String url) {
    this.redirectEndpoint_ = url;
    this.isConfigure();
  }
  /**
   * Sets the client ID.
   *
   * <p>This setting is required.
   *
   * <p>
   *
   * @see OidcService#getClientId()
   */
  public void setClientId(final String id) {
    this.clientId_ = id;
    this.isConfigure();
  }
  /**
   * Sets the client secret.
   *
   * <p>This setting is required.
   *
   * <p>
   *
   * @see OidcService#setClientSecret(String secret)
   */
  public void setClientSecret(final String secret) {
    this.clientSecret_ = secret;
    this.isConfigure();
  }
  /**
   * Sets the authorization endpoint URL.
   *
   * <p>This setting is required.
   *
   * <p>
   *
   * @see OidcService#getAuthorizationEndpoint()
   */
  public void setAuthEndpoint(final String url) {
    this.authorizationEndpoint_ = url;
    this.isConfigure();
  }
  /**
   * Sets the token endpoint URL.
   *
   * <p>This setting is required.
   *
   * <p>
   *
   * @see OidcService#getTokenEndpoint()
   */
  public void setTokenEndpoint(final String url) {
    this.tokenEndpoint_ = url;
    this.isConfigure();
  }
  /**
   * Sets the user info endpoint URL.
   *
   * <p>This setting is required.
   *
   * <p>
   *
   * @see OidcService#getUserInfoEndpoint()
   */
  public void setUserInfoEndpoint(final String url) {
    this.userInfoEndpoint_ = url;
    this.isConfigure();
  }
  /**
   * Sets the scope needed for authentication.
   *
   * <p>This setting is required.
   *
   * <p>
   *
   * @see OidcService#getAuthenticationScope()
   */
  public void setAuthenticationScope(final String scope) {
    this.scope_ = scope;
    this.isConfigure();
  }
  /**
   * Sets the provider name.
   *
   * <p>
   *
   * @see OidcService#getName()
   */
  public void setName(final String name) {
    this.name_ = name;
    this.isConfigure();
  }
  /**
   * Sets the provider description.
   *
   * <p>This setting is required.
   *
   * <p>
   *
   * @see OidcService#getName()
   */
  public void setDescription(final String description) {
    this.description_ = description;
  }
  /**
   * Sets the method to transfer the client secret.
   *
   * <p>
   *
   * @see OidcService#getName()
   */
  public void setClientSecretMethod(ClientSecretMethod method) {
    this.method_ = method;
  }
  /**
   * Sets the desired width for the popup window.
   *
   * <p>
   *
   * @see OidcService#getPopupWidth()
   */
  public void setPopupWidth(int width) {
    this.popupWidth_ = width;
  }
  /**
   * Sets the desired height for the popup window.
   *
   * <p>
   *
   * @see OidcService#getPopupHeight()
   */
  public void setPopupHeight(int height) {
    this.popupHeight_ = height;
  }

  private boolean isConfigure() {
    this.configured_ =
        this.redirectEndpoint_.length() != 0
            && this.authorizationEndpoint_.length() != 0
            && this.tokenEndpoint_.length() != 0
            && this.userInfoEndpoint_.length() != 0
            && this.clientId_.length() != 0
            && this.clientSecret_.length() != 0
            && this.name_.length() != 0
            && this.scope_.length() != 0;
    return this.configured_;
  }

  private String redirectEndpoint_;
  private String authorizationEndpoint_;
  private String tokenEndpoint_;
  private String userInfoEndpoint_;
  private String clientId_;
  private String clientSecret_;
  private String name_;
  private String description_;
  private String scope_;
  private int popupWidth_;
  private int popupHeight_;
  private ClientSecretMethod method_;
  private boolean configured_;
}
