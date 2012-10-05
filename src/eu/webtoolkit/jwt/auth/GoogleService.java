/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth service for Google as third-party authenticator.
 * <p>
 * 
 * The configuration of the service is done using properties, whose values need
 * to match the values configured at Google.
 * <p>
 * <ul>
 * <li><code>google-oauth2-redirect-endpoint</code>: the URL of the local
 * redirect endpoint, to which the google OAuth service redirects the user after
 * authentication. See also {@link }</li>
 * <li><code>google-oauth2-redirect-endpoint-path</code>: optionally, the
 * deployment path that corresponds to the redirect endpoint. See also {@link }</li>
 * <li><code>google-oauth2-client-id</code>: The client ID</li>
 * <li><code>google-oauth2-client-secret</code>: The client secret.</li>
 * </ul>
 * <p>
 * For example:
 * 
 * <pre>
 * {@code
 *  <properties>
 *    <property name="google-oauth2-redirect-endpoint">
 *      http://localhost:8080/oauth2callback
 *    </property>
 *    <property name="google-oauth2-client-id">
 *      123456789012.apps.googleusercontent.com
 *    </property>
 *    <property name="google-oauth2-client-secret">
 *      abcdefghijk-12312312312
 *    </property>
 *  </properties>
 * }
 * </pre>
 * <p>
 * Like all <b>service classes</b>, this class holds only configuration state.
 * Thus, once configured, it can be safely shared between multiple sessions
 * since its state (the configuration) is read-only.
 * <p>
 * See also: <a
 * href="http://code.google.com/apis/accounts/docs/OAuth2.html">http
 * ://code.google.com/apis/accounts/docs/OAuth2.html</a>
 */
public class GoogleService extends OAuthService {
	private static Logger logger = LoggerFactory.getLogger(GoogleService.class);

	/**
	 * Constructor.
	 */
	public GoogleService(AuthService baseAuth) {
		super(baseAuth);
	}

	/**
	 * Checks whether a GoogleAuth service is properly configured.
	 * <p>
	 * This returns <code>true</code> if a value is found for the three
	 * configuration properties.
	 */
	public static boolean configured() {
		try {
			configurationProperty(RedirectEndpointProperty);
			configurationProperty(ClientIdProperty);
			configurationProperty(ClientSecretProperty);
			return true;
		} catch (RuntimeException e) {
			logger.info(new StringWriter().append("not configured: ").append(
					e.toString()).toString());
			return false;
		}
	}

	public String getName() {
		return "google";
	}

	public WString getDescription() {
		return new WString("Google Account");
	}

	public int getPopupWidth() {
		return 550;
	}

	public int getPopupHeight() {
		return 400;
	}

	public String getAuthenticationScope() {
		return ProfileScope + " " + EmailScope;
	}

	public String getRedirectEndpoint() {
		return configurationProperty(RedirectEndpointProperty);
	}

	public String getRedirectEndpointPath() {
		try {
			return configurationProperty(RedirectEndpointPathProperty);
		} catch (RuntimeException e) {
			return super.getRedirectEndpointPath();
		}
	}

	public String getAuthorizationEndpoint() {
		return AuthUrl;
	}

	public String getTokenEndpoint() {
		return TokenUrl;
	}

	public String getClientId() {
		return configurationProperty(ClientIdProperty);
	}

	public String getClientSecret() {
		return configurationProperty(ClientSecretProperty);
	}

	public OAuthProcess createProcess(String scope) {
		return new GoogleProcess(this, scope);
	}

	static String RedirectEndpointProperty = "google-oauth2-redirect-endpoint";
	static String RedirectEndpointPathProperty = "google-oauth2-redirect-endpoint-path";
	static String ClientIdProperty = "google-oauth2-client-id";
	static String ClientSecretProperty = "google-oauth2-client-secret";
	static String AuthUrl = "https://accounts.google.com/o/oauth2/auth";
	static String TokenUrl = "https://accounts.google.com/o/oauth2/token";
	static String ProfileScope = "https://www.googleapis.com/auth/userinfo.profile";
	static String EmailScope = "https://www.googleapis.com/auth/userinfo.email";
}
