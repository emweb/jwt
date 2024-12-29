/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
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
 * An environment for testing purposes.
 *
 * <p>This environment is useful for use in automated (integration/unit) tests: you may configure
 * its properties and pass it to the constructor of an application.
 *
 * <p>This is useful for automated test-cases:
 *
 * <pre>{@code
 * void testX() {
 * WTestEnvironment environment(new Configuration());
 * MyApplication app(environment);
 * ...
 * }
 *
 * }</pre>
 *
 * <p>
 *
 * @see WEnvironment
 * @see WApplication#WApplication(WEnvironment env)
 */
public class WTestEnvironment extends WEnvironment {
  private static Logger logger = LoggerFactory.getLogger(WTestEnvironment.class);

  /**
   * Default constructor.
   *
   * <p>Constructs a test environment that resembles FireFox 3.0 with default settings.
   *
   * <p>After construction, but before passing it to the constructor of a {@link WApplication}, you
   * can change any of the environment properties using the setter methods.
   */
  public WTestEnvironment(Configuration configuration, EntryPointType type) {
    super();
    this.theSession_ = null;
    this.dialogExecuted_ = new Signal1<WDialog>();
    this.popupExecuted_ = new Signal1<WPopupMenu>();
    List<String> dummy = new ArrayList<String>();
    this.controller_ = new TestController(configuration);
    this.init(type);
  }
  /**
   * Default constructor.
   *
   * <p>Calls {@link #WTestEnvironment(Configuration configuration, EntryPointType type)
   * this(configuration, EntryPointType.Application)}
   */
  public WTestEnvironment(Configuration configuration) {
    this(configuration, EntryPointType.Application);
  }
  /**
   * Closes the test environment.
   *
   * <p>Destroys the test environment. This will allow the environment and the application under
   * test to be garbage collected.
   */
  public void close() {
    WebSession.Handler.getInstance().release();
  }
  /**
   * Sets parameters to the application.
   *
   * <p>The default value is an empty map.
   *
   * <p>
   *
   * @see WEnvironment#getParameterMap()
   */
  public void setParameterMap(final Map<String, String[]> parameters) {
    this.parameters_ = parameters;
  }
  /**
   * Sets HTTP cookies.
   *
   * <p>The default value is an empty map.
   *
   * <p>
   *
   * @see WEnvironment#getCookies()
   */
  public void setCookies(final Map<String, String> cookies) {
    this.cookies_ = cookies;
  }
  /**
   * Sets a HTTP header value.
   *
   * <p>The default value is no headers.
   *
   * <p>
   *
   * @see WEnvironment#getHeaderValue(String name)
   */
  public void setHeaderValue(final String value) {}
  /**
   * Sets whether cookies are supported.
   *
   * <p>The default value is <i>true</i>.
   *
   * <p>
   *
   * @see WEnvironment#supportsCookies()
   */
  public void setSupportsCookies(boolean enabled) {
    this.doesCookies_ = enabled;
  }
  /**
   * Sets whether AJAX is supported.
   *
   * <p>The default value is <i>true</i>.
   *
   * <p>
   *
   * @see WEnvironment#hasAjax()
   */
  public void setAjax(boolean enabled) {
    this.doesAjax_ = enabled;
  }
  /**
   * Sets the display&apos;s DPI scale.
   *
   * <p>The default value is 1.
   *
   * <p>
   *
   * @see WEnvironment#getDpiScale()
   */
  public void setDpiScale(double dpiScale) {
    this.dpiScale_ = dpiScale;
  }
  /**
   * Sets the locale.
   *
   * <p>The default value is the English locale (&quot;en&quot;).
   *
   * <p>
   *
   * @see WEnvironment#getLocale()
   */
  public void setLocale(final Locale locale) {
    this.locale_ = locale;
  }
  /**
   * Sets the host name.
   *
   * <p>The default value is &quot;localhost&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getHostName()
   */
  public void setHostName(final String hostName) {
    this.host_ = hostName;
  }
  /**
   * Sets the URL scheme.
   *
   * <p>The default value is &quot;http&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getUrlScheme()
   */
  public void setUrlScheme(final String scheme) {
    this.urlScheme_ = scheme;
  }
  /**
   * Sets the user agent.
   *
   * <p>The default value is no &quot;Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.11)
   * Gecko/2009060309 Ubuntu/9.04 (jaunty) Firefox/3.0.11&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getUserAgent()
   */
  void setUserAgent(final String userAgent) {
    super.setUserAgent(userAgent);
  }
  /**
   * Sets the referer.
   *
   * <p>The default value is &quot;&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getReferer()
   */
  public void setReferer(final String referer) {
    this.referer_ = referer;
  }
  /**
   * Sets the accept header.
   *
   * <p>The default value is
   * &quot;text/html,application/xhtml+xml,application/xml;q=0.9,*&lt;span&gt;/&lt;/span&gt;*;q=0.8&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getAccept()
   */
  public void setAccept(final String accept) {
    this.accept_ = accept;
  }
  /**
   * Sets the server signature.
   *
   * <p>The default value is &quot;None (WTestEnvironment)&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getServerSignature()
   */
  public void setServerSignature(final String signature) {
    this.serverSignature_ = signature;
  }
  /**
   * Sets the server software.
   *
   * <p>The default value is &quot;None (WTestEnvironment)&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getServerSoftware()
   */
  public void setServerSoftware(final String software) {
    this.serverSignature_ = software;
  }
  /**
   * Sets the server admin.
   *
   * <p>The default value is &quot;your@onyourown.here&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getServerAdmin()
   */
  public void setServerAdmin(final String serverAdmin) {
    this.serverAdmin_ = serverAdmin;
  }
  /**
   * Sets the client address.
   *
   * <p>The default value is &quot;127.0.0.1&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getClientAddress()
   */
  public void setClientAddress(final String clientAddress) {
    this.clientAddress_ = clientAddress;
  }
  /**
   * Sets the initial internal path.
   *
   * <p>The default value is &quot;&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getInternalPath()
   */
  public void setInternalPath(final String internalPath) {
    super.setInternalPath(internalPath);
  }
  /**
   * Sets the deployment path.
   *
   * <p>The default value is &quot;&quot;.
   *
   * <p>
   *
   * @see WEnvironment#getDeploymentPath()
   */
  public void setDeploymentPath(final String deployPath) {
    this.publicDeploymentPath_ = deployPath;
  }
  /**
   * Signal used to test a dialog/messagebox reentrant event loop.
   *
   * <p>This signal is emitted when a dialog or message box is being executed using {@link
   * WDialog#exec(WAnimation animation) WDialog#exec()} or {@link WDialog#exec(WAnimation animation)
   * WDialog#exec()}, and allows you to interact with the dialog contents.
   *
   * <p>In the end, the dialog should be closed while executing this signal, (calling done()
   * directly or indirectly) so that the main event loop can continue.
   */
  Signal1<WDialog> dialogExecuted() {
    return this.dialogExecuted_;
  }
  /**
   * Signal used to test a popup menu reentrant event loop.
   *
   * <p>This signal is emitted when a popup menu is being executed using WPopupMenu::exec(), and
   * allows you to interact with the popup menu (i.e. to select an option).
   *
   * <p>
   *
   * @see WTestEnvironment#dialogExecuted()
   */
  Signal1<WPopupMenu> popupExecuted() {
    return this.popupExecuted_;
  }
  /**
   * Simulates the end of a request by the main event loop.
   *
   * <p>The environemnt (and application is) started from within the main event loop. To simulate
   * the delivery of events posted to the application-under-test, by WServer::post(), you need to
   * simulate the release of the session lock.
   *
   * <p>
   *
   * @see WTestEnvironment#startRequest()
   */
  public void endRequest() {}

  /**
   * Simulates the start of a new request by the main event loop.
   *
   * <p>
   *
   * @see WTestEnvironment#endRequest()
   */
  public void startRequest() {
    new WebSession.Handler(this.theSession_, WebSession.Handler.LockOption.TakeLock);
  }
  /**
   * Simulates the presence of the session ID in the URL.
   *
   * <p>A session ID in the URL should cause a trampoline to be used for references to external
   * servers.
   *
   * <p>The default value is <code>false</code>.
   */
  public void setSessionIdInUrl(boolean sessionIdInUrl) {
    this.theSession_.setSessionIdInUrl(sessionIdInUrl);
  }

  private WebSession theSession_;
  private WtServlet controller_;
  private Signal1<WDialog> dialogExecuted_;
  private Signal1<WPopupMenu> popupExecuted_;

  public boolean isTest() {
    return true;
  }

  private void init(EntryPointType type) {
    this.session_ = new WebSession(this.controller_, "testwtd", type, "", (WebRequest) null, this);
    this.theSession_ = this.session_;
    new WebSession.Handler(this.theSession_, WebSession.Handler.LockOption.TakeLock);
    this.doesAjax_ = true;
    this.doesCookies_ = true;
    this.dpiScale_ = 1;
    this.urlScheme_ = "http";
    this.referer_ = "";
    this.accept_ = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    this.serverSignature_ = "None (WTestEnvironment)";
    this.serverSoftware_ = this.serverSignature_;
    this.serverAdmin_ = "your@onyourown.here";
    this.setUserAgent(
        "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.0.11) Gecko/2009060309 Ubuntu/9.04 (jaunty) Firefox/3.0.11");
    this.host_ = "localhost";
    this.clientAddress_ = "127.0.0.1";
    this.locale_ = new Locale("en");
  }
}
