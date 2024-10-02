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
 * OAuth service for Google as third-party authenticator.
 *
 * <p>The configuration of the service is done using properties, whose values need to match the
 * values configured at Google.
 *
 * <p>
 *
 * <ul>
 *   <li><code>google-oauth2-redirect-endpoint</code>: the URL of the local redirect endpoint, to
 *       which the google OAuth service redirects the user after authentication. See also {@link
 *       OidcService#getRedirectEndpoint()}
 *   <li><code>google-oauth2-redirect-endpoint-path</code>: optionally, the deployment path that
 *       corresponds to the redirect endpoint. See also {@link
 *       GoogleService#getRedirectEndpointPath() getRedirectEndpointPath()}
 *   <li><code>google-oauth2-client-id</code>: The client ID
 *   <li><code>google-oauth2-client-secret</code>: The client secret.
 * </ul>
 *
 * <p>For example:
 *
 * <pre>{@code
 * <properties>
 * <property name="google-oauth2-redirect-endpoint">
 * http://localhost:8080/oauth2callback
 * </property>
 * <property name="google-oauth2-client-id">
 * 123456789012.apps.googleusercontent.com
 * </property>
 * <property name="google-oauth2-client-secret">
 * abcdefghijk-12312312312
 * </property>
 * </properties>
 *
 * }</pre>
 *
 * <p>Like all <b>service classes</b>, this class holds only configuration state. Thus, once
 * configured, it can be safely shared between multiple sessions since its state (the configuration)
 * is read-only.
 *
 * <p>See also: <a
 * href="https://developers.google.com/identity/protocols/OAuth2">https://developers.google.com/identity/protocols/OAuth2</a>
 * <a
 * href="https://developers.google.com/identity/protocols/OpenIDConnect">https://developers.google.com/identity/protocols/OpenIDConnect</a>
 */
public class GoogleService extends OidcService {
  private static Logger logger = LoggerFactory.getLogger(GoogleService.class);

  /** Constructor. */
  public GoogleService(final AuthService baseAuth) {
    super(baseAuth);
    this.setRedirectEndpoint(configurationProperty(RedirectEndpointProperty));
    this.setClientId(configurationProperty(ClientIdProperty));
    this.setClientSecret(configurationProperty(ClientSecretProperty));
    this.setAuthEndpoint("https://accounts.google.com/o/oauth2/v2/auth");
    this.setTokenEndpoint("https://www.googleapis.com/oauth2/v4/token");
    this.setUserInfoEndpoint("https://www.googleapis.com/oauth2/v3/userinfo");
    this.setAuthenticationScope("openid email profile");
    this.setName("google");
    this.setDescription("Google Account");
    this.setPopupWidth(550);
  }
  /**
   * Checks whether a GoogleAuth service is properly configured.
   *
   * <p>This returns <code>true</code> if a value is found for the three configuration properties.
   */
  public static boolean configured() {
    try {
      configurationProperty(RedirectEndpointProperty);
      configurationProperty(ClientIdProperty);
      configurationProperty(ClientSecretProperty);
      return true;
    } catch (final RuntimeException e) {
      logger.info(new StringWriter().append("not configured: ").append(e.toString()).toString());
      return false;
    }
  }

  public String getRedirectEndpointPath() {
    try {
      return configurationProperty(RedirectEndpointPathProperty);
    } catch (final RuntimeException e) {
      return super.getRedirectEndpointPath();
    }
  }

  private static String RedirectEndpointProperty = "google-oauth2-redirect-endpoint";
  private static String RedirectEndpointPathProperty = "google-oauth2-redirect-endpoint-path";
  private static String ClientIdProperty = "google-oauth2-client-id";
  private static String ClientSecretProperty = "google-oauth2-client-secret";
}
