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
 * An OAuth authorization (and authentication) service provider.
 *
 * <p>This class implements an OAuth client (<a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22">2.0 draft</a>), which can be used to
 * allow the user to authorize access to information provided by a third-party OAuth service
 * provider. This allows, among other things, for a user to safely authenticate with your web
 * application without needing to store or even handle his authorization credentials (such as a
 * password), a pattern called &quot;OAuth2 connect&quot;.
 *
 * <p>The OAuth protocol provides a standard for a user to authorize access to protected resources
 * made available by a third party service. The process starts with the user authenticating on an
 * &quot;authorization page&quot; and authorizing access. This results eventually in an access token
 * for the web application. The actual use of this token, to obtain certain information (such as an
 * authenticated identity) from the third party is however not standardized, and there are many
 * other uses of OAuth besides authentication.
 *
 * <p>Because the focus of the Wt::Auth library is authentication, the OAuth class also contains the
 * implementation for using the access token for authentication ({@link
 * OAuthProcess#getIdentity(OAuthAccessToken token) OAuthProcess#getIdentity()}).
 *
 * <p>Like all <b>service classes</b>, this class holds only configuration state. Thus, once
 * configured, it can be safely shared between multiple sessions since its state (the configuration)
 * is read-only.
 *
 * <p>The OAuth authorization protocol, including the subsequent use for authentication, consists of
 * a number of consecutive steps, some of which require user interaction, and some which require the
 * use of remote web services. The state machine for this process is implemented in an {@link
 * OAuthProcess}. To use OAuth, you need to create such a process and listen for state changes.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * OAuthService oauth = ...;
 *
 * // Creates an icon which prompts for authentication using this OAuth service.
 * WImage icon = new WImage("css/oauth-" + auth.getName() + ".png", icons);
 * icon.setToolTip(auth.getDescription());
 *
 * // Creates a new process for authentication, which is started by a click on the icon
 * process = oauth.createProcess(oauth.getAuthenticationScope());
 * process.connectStartAuthenticate(icon.clicked());
 *
 * // And capture the result in a method.
 * process.authenticated().addListener(this, new Signal1.Listener<Identity>() {
 * public void trigger(Identity id) {
 * MyWidget.this.oAuthDone(id);
 * }
 * });
 *
 * }</pre>
 */
public abstract class OAuthService {
  private static Logger logger = LoggerFactory.getLogger(OAuthService.class);

  /**
   * Constructor.
   *
   * <p>Creates a new OAuth service.
   */
  public OAuthService(final AuthService auth) {
    this.baseAuth_ = auth;
    this.impl_ = new Impl();
    this.usePopup_ = false;
  }
  /** Returns the basic authentication service. */
  public AuthService getBaseAuth() {
    return this.baseAuth_;
  }
  /**
   * Creates a new authorization process.
   *
   * <p>This creates a new authorization process for the indicated scope. Valid names for the scope
   * are service provider dependent.
   *
   * <p>
   *
   * @see OAuthService#getAuthenticationScope()
   */
  public abstract OAuthProcess createProcess(final String scope);
  /**
   * Configure if a popup should be used for the login.
   *
   * <p>When set to false, the session will be suspended when the login starts, allowing the state
   * to be restored when it completes. A timeout can be configured in wt_config.xml (see <code>
   * oauth2-redirect-timeout</code>).
   *
   * <p>The default value is false. When JavaScript is not available, a redirect without popup is
   * always used.
   *
   * <p>
   *
   * <p><i><b>Note: </b>In JWt 4.8.0 the default value changed from true to false </i>
   */
  public void setPopupEnabled(boolean enable) {
    this.usePopup_ = enable;
  }
  /** Returns if a popup is used for the login. */
  public boolean isPopupEnabled() {
    return this.usePopup_;
  }
  /**
   * Returns the provider name.
   *
   * <p>This is a short identifier.
   *
   * <p>
   *
   * @see OAuthService#getDescription()
   */
  public abstract String getName();
  /**
   * Returns the provider description.
   *
   * <p>This returns a description useful for e.g. tool tips on a login icon.
   *
   * <p>
   *
   * @see OAuthService#getName()
   */
  public abstract WString getDescription();
  /**
   * Returns the desired width for the popup window.
   *
   * <p>
   *
   * @see OAuthService#getPopupHeight()
   */
  public abstract int getPopupWidth();
  /**
   * Returns the desired height for the popup window.
   *
   * <p>
   *
   * @see OAuthService#getPopupWidth()
   */
  public abstract int getPopupHeight();
  /**
   * Returns the scope needed for authentication.
   *
   * <p>This returns the scope that is needed (and sufficient) for obtaining identity information,
   * and thus to authenticate the user.
   *
   * <p>
   *
   * @see OAuthProcess#getIdentity(OAuthAccessToken token)
   * @see OAuthProcess#startAuthenticate()
   */
  public abstract String getAuthenticationScope();
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
   */
  public abstract String getRedirectEndpoint();
  /**
   * Returns the deployment path of the redirection endpoint.
   *
   * <p>This returns the path at which the static resource is deployed that corresponds to the
   * {@link OAuthService#getRedirectEndpoint() getRedirectEndpoint()}.
   *
   * <p>The default implementation will derive this path from the {@link
   * OAuthService#getRedirectEndpoint() getRedirectEndpoint()} URL.
   */
  public String getRedirectEndpointPath() {
    URL parsedUrl = new URL();
    HttpClient.parseUrl(this.getRedirectEndpoint(), parsedUrl);
    String path = parsedUrl.path;
    return path;
  }
  /**
   * Returns the authorization endpoint URL.
   *
   * <p>This is a remote URL which hosts the OAuth authorization user interface. This URL is loaded
   * in the popup window at the start of an authorization process.
   */
  public abstract String getAuthorizationEndpoint();
  /**
   * Returns the token endpoint URL.
   *
   * <p>This is a remote URL which hosts a web-service that generates access tokens.
   */
  public abstract String getTokenEndpoint();

  public String getUserInfoEndpoint() {
    throw new WException("OAuth::Process::userInfoEndpoint(): not specialized");
  }
  /**
   * Returns the client ID.
   *
   * <p>This is the identification for this web application with the OAuth authorization server.
   */
  public abstract String getClientId();
  /**
   * Returns the client secret.
   *
   * <p>This is the secret credentials for this web application with the OAuth authorization server.
   */
  public abstract String getClientSecret();
  /**
   * Derives a state value from the session ID.
   *
   * <p>The state value protects the authorization protocol against misuse, and is used to connect
   * an authorization code response with a particular request.
   *
   * <p>In the default implementation the state is the <code>sessionId</code>, crytpographically
   * signed. This signature is verified in {@link OAuthService#decodeState(String state)
   * decodeState()}.
   */
  public String encodeState(final String url) {
    return AuthUtils.encodeState(this.impl_.secret_, url);
  }
  /**
   * Validates and decodes a state parameter.
   *
   * <p>This function returns the sessionId that is encoded in the state, if the signature validates
   * that it is an authentic state generated by {@link OAuthService#encodeState(String url)
   * encodeState()}.
   */
  public String decodeState(final String state) {
    return AuthUtils.decodeState(this.impl_.secret_, state);
  }
  /**
   * Returns the HTTP method used for the token request.
   *
   * <p>While the current OAuth 2.0 draft mandates the use of POST, some implementations (like
   * Facebook) use URL-encoding and a GET request.
   *
   * <p>The default implementation returns Http::Method::Post (corresponding to the current draft).
   */
  public Method getTokenRequestMethod() {
    return Method.Post;
  }
  /**
   * Returns the method to transfer the client secret.
   *
   * <p>Some implementations (like Facebook) encode the secret in the GET request parameters, while
   * this is explicitly not allowed in the OAuth 2.0 specification.
   *
   * <p>The default implementation returns HttpAuthorizationBasic (the recommended method).
   */
  public abstract ClientSecretMethod getClientSecretMethod();

  public String getGenerateRedirectEndpoint() {
    this.configureRedirectEndpoint();
    String result = this.getRedirectEndpoint();
    return result;
  }

  public String getRedirectInternalPath() {
    return "/auth/oauth/" + this.getName() + "/redirect";
  }
  /**
   * Configures the static resource implementing the redirect endpoint.
   *
   * <p>By default, this endpoint is configured whenever it&apos;s necessary, but one may also
   * configure it in advance, for example in a multi-process deployment (FastCGI).
   */
  public void configureRedirectEndpoint() {
    if (!(this.impl_.redirectResource_ != null)) {
      if (!(this.impl_.redirectResource_ != null)) {
        OAuthService.Impl.RedirectEndpoint r = new OAuthService.Impl.RedirectEndpoint(this);
        String path = this.getRedirectEndpointPath();
        logger.info(new StringWriter().append("deploying endpoint at ").append(path).toString());
        WApplication app = WApplication.getInstance();
        WtServlet server;
        if (app != null) {
          server = app.getEnvironment().getServer();
        } else {
          server = WtServlet.getInstance();
        }
        server.addResource(r, path);
        this.impl_.redirectResource_ = r;
      }
    }
  }

  protected static String configurationProperty(final String property) {
    return AuthUtils.configurationProperty("OAuth", property);
  }
  // private  OAuthService(final OAuthService anon1) ;
  private final AuthService baseAuth_;
  private OAuthService.Impl impl_;
  private boolean usePopup_;

  static class Impl {
    private static Logger logger = LoggerFactory.getLogger(Impl.class);

    Impl() {
      this.redirectResource_ = (OAuthService.Impl.RedirectEndpoint) null;
      this.secret_ = "";
      try {
        this.secret_ = OAuthService.configurationProperty("oauth2-secret");
      } catch (final RuntimeException e) {
        this.secret_ = MathUtils.randomId(32);
      }
    }

    static final class RedirectEndpoint extends WResource {
      private static Logger logger = LoggerFactory.getLogger(RedirectEndpoint.class);

      RedirectEndpoint(final OAuthService service) {
        super();
        this.service_ = service;
      }

      protected void handleRequest(final WebRequest request, final WebResponse response) {
        try {
          String stateE = request.getParameter("state");
          if (stateE != null) {
            String redirectUrl = this.service_.decodeState(stateE);
            if (redirectUrl.length() != 0) {
              boolean hasQuery = redirectUrl.indexOf('?') != -1;
              redirectUrl += hasQuery ? '&' : '?';
              redirectUrl += "state=" + Utils.urlEncode(stateE);
              String errorE = request.getParameter("error");
              if (errorE != null) {
                redirectUrl += "&error=" + Utils.urlEncode(errorE);
              }
              String codeE = request.getParameter("code");
              if (codeE != null) {
                redirectUrl += "&code=" + Utils.urlEncode(codeE);
              }
              response.setStatus(302);
              response.addHeader("Location", redirectUrl);
              return;
            } else {
              logger.error(
                  new StringWriter()
                      .append("RedirectEndpoint: could not decode state ")
                      .append(stateE)
                      .toString());
            }
          } else {
            logger.error(new StringWriter().append("RedirectEndpoint: missing state").toString());
          }
          response.setStatus(400);
          response.setContentType("text/html");
          response
              .out()
              .append("<html><body>")
              .append("<h1>OAuth Authentication error</h1>")
              .append("</body></html>");
        } catch (IOException ioe) {
          logger.info("Ignoring exception {}", ioe.getMessage(), ioe);
        }
      }

      private final OAuthService service_;
    }

    public OAuthService.Impl.RedirectEndpoint redirectResource_;
    public String secret_;
  }
}
