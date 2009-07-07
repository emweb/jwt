/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import eu.webtoolkit.jwt.servlet.WebRequest;

/**
 * A class that captures information on the application environment
 * <p>
 * 
 * The environment provides information on the client, and gives access to
 * startup arguments.
 * <p>
 * Usage example:
 * <p>
 * <code>
 WEnvironment env = WApplication.instance().environment(); <br> 
	  <br> 
 // read an application startup argument  <br> 
 // (passed as argument in the URL or POST&apos;ed to the application). <br> 
 if (!env.getParameterValues(&quot;login&quot;).isEmpty()) { <br> 
 String login = env.getParameterValues(&quot;login&quot;).get(0); <br> 
 //... <br> 
 } <br> 
	  <br> 
 // Check for JavaScript/AJAX availability before using JavaScript-only <br> 
 // widgets <br> 
  WTextArea textEdit; <br> 
 if (!env.isAjax()) <br> 
   textEdit = new WTextEdit(); // provide an HTML text editor <br> 
 else <br> 
   textEdit = new WTextArea(); // fall-back to a plain old text area.
</code>
 */
public class WEnvironment {
	public enum UserAgent {
		Unknown(0), IE(1000), IE6(1001), IE7(1002), IE8(1003), IEMobile(1100), Opera(
				3000), WebKit(4000), Safari(4100), Chrome(4200), Konqueror(5000), Gecko(
				6000), Firefox3(6101), Firefox3_1(6102), Firefox3_2(6103), BotAgent(
				10000);

		private int value;

		UserAgent(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * Enumeration for HTML content type.
	 */
	public enum ContentType {
		/**
		 * XHTML1.x.
		 */
		XHTML1,
		/**
		 * HTML4.
		 */
		HTML4;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Cookie map.
	 * <p>
	 * A std::map which associates a cookie name with a cookie value.
	 * <p>
	 * 
	 * @see WEnvironment#getCookies()
	 */
	public Map<String, String> CookieMap;

	/**
	 * Parameters passed to the application.
	 * <p>
	 * Arguments passed to the application, either in the URL for a http GET, or
	 * in both the URL and data submitted in a http POST.
	 * <p>
	 * 
	 * @see WEnvironment#getParameterValues(String name)
	 */
	public Map<String, List<String>> getParameterMap() {
		return this.parameters_;
	}

	/**
	 * Returns values for a query parameter.
	 * <p>
	 * Returns an empty list if the parameter was not defined.
	 * <p>
	 * One or more values may be associated with a single argument.
	 * <p>
	 * For example a Wt application <code>foo.wt</code> started as
	 * 
	 * <code><a href="http://.../foo.wt?hello=Hello&hello=World">http://.../foo.wt?hello=Hello&amp;hello=World</a></code>
	 * will result in both values <code>&quot;Hello&quot;</code> and
	 * <code>&quot;World&quot;</code> to be associated with the argument
	 * <code>&quot;hello&quot;</code>.
	 * <p>
	 * 
	 * @see WEnvironment#getParameterMap()
	 */
	public List<String> getParameterValues(String name) {
		List<String> i = this.parameters_.get(name);
		if (i != null) {
			return i;
		} else {
			return WebRequest.emptyValues_;
		}
	}

	/**
	 * Returns a single value for a query parameter.
	 * <p>
	 * Returns the first value for a parameter, or 0 if the parameter is not
	 * found.
	 * <p>
	 * 
	 * @see WEnvironment#getParameterValues(String name)
	 */
	public String getParameter(String name) {
		List<String> values = this.getParameterValues(name);
		if (!values.isEmpty()) {
			return values.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Cookies set in the initial call to the application.
	 * <p>
	 * Note that cookies set with
	 * {@link WApplication#setCookie(String name, String value, int maxAge, String domain, String path)}
	 * are not made available in the environment.
	 * <p>
	 * Not all clients may support cookies or have cookies enabled.
	 * <p>
	 * 
	 * @see WEnvironment#supportsCookies()
	 * @see WEnvironment#getCookie(String cookieNname)
	 */
	public Map<String, String> getCookies() {
		return this.cookies_;
	}

	/**
	 * Checks for existence and returns specified argument.
	 * <p>
	 * Throws a <code>std::runtime_error(&quot;Missing cookie: ...&quot;)</code>
	 * when the cookie is missing, or returns cookie value otherwise.
	 */
	public String getCookie(String cookieNname) {
		String i = this.cookies_.get(cookieNname);
		if (i == null) {
			throw new RuntimeException("Missing cookie: " + cookieNname);
		} else {
			return i;
		}
	}

	/**
	 * Returns a cgi environment header value.
	 */
	public String getHeaderValue(String name) {
		return this.session_.getCgiHeader(name);
	}

	/**
	 * Returns whether the browser has enabled support for cookies.
	 * <p>
	 * When the user disables cookies during the visit of the page, this value
	 * is not updated.
	 * <p>
	 * 
	 * @see WEnvironment#getCookies()
	 * @see WEnvironment#getCookie(String cookieNname)
	 */
	public boolean supportsCookies() {
		return this.doesCookies_;
	}

	/**
	 * Returns whether the browser has enabled support for JavaScript.
	 * <p>
	 * Without support for JavaScript, Wt will still be able to serve the
	 * application, but with one considerable limitation: only the
	 * {@link WTimer#timeout()}, {@link WInteractWidget#clicked()},
	 * {@link WApplication#internalPathChanged()}, and
	 * {@link WAbstractArea#clicked()} signals (and any derived signals) will
	 * generate events.
	 * <p>
	 * Every event will cause the complete page to be rerendered.
	 * <p>
	 * 
	 * @see WEnvironment#hasAjax()
	 */
	public boolean hasJavaScript() {
		return this.doesJavaScript_;
	}

	/**
	 * Returns whether the browser has enabled support for AJAX.
	 * <p>
	 * Without support for AJAX, Wt will still be able to serve the application,
	 * but every event will cause the application to retransmit the whole page,
	 * rendering many events inpractical.
	 * <p>
	 * 
	 * @see WEnvironment#hasJavaScript()
	 */
	public boolean hasAjax() {
		return this.doesAjax_;
	}

	/**
	 * Returns the browser-side DPI scaling factor.
	 * <p>
	 * Internet Explorer scales all graphics, fonts and other elements on
	 * high-density screens to make them readable. This is controlled by the DPI
	 * setting of the display. If all goes well, you do not have to worry about
	 * this scaling factor. Unfortunately, not all elements are scaled
	 * appropriately. The scaling factor is supposed to be used only internally
	 * in Wt and is in this interface for informational purposes.
	 * <p>
	 * 
	 * @see WVmlImage
	 */
	public double getDpiScale() {
		return this.dpiScale_;
	}

	/**
	 * Returns the preferred language indicated in the request header.
	 * <p>
	 * The language is parsed from the HTTP <code>Accept-Language</code> field,
	 * if present. If not, the locale is empty.
	 * <p>
	 * If multiple languages are present, the one with the highest
	 * &quot;q&quot;uality is assumed, and if a tie is present, the first one is
	 * taken.
	 * <p>
	 * 
	 * @see WApplication#setLocale(Locale locale)
	 */
	public Locale getLocale() {
		return this.locale_;
	}

	/**
	 * Returns the server host name that is used by the client.
	 * <p>
	 * The hostname is the unresolved host name with optional port number, which
	 * the browser used to connect to the application.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li><code>www.mydomain.com</code></li>
	 * <li><code>localhost:8080</code></li>
	 * </ul>
	 * <p>
	 * For HTTP 1.1 requests, this information is fetched from the HTTP
	 * <code>Host</code> header. If Wt is configured behind a reverse proxy,
	 * then the last entry in the HTTP <code>X-Forwarded-Host</code> header
	 * field is used instead (to infer the name of the reverse proxy instead).
	 * <p>
	 * For HTTP 1.0 requests, the HTTP <code>Host</code> header is not required.
	 * When not present, the server host name is inferred from the configured
	 * server name, which defaults to the DNS name.
	 */
	public String getHostName() {
		return this.host_;
	}

	/**
	 * Returns the URL scheme used for the current request (
	 * <code>&quot;http&quot;</code> or <code>&quot;https&quot;</code>).
	 */
	public String getUrlScheme() {
		return this.urlScheme_;
	}

	/**
	 * Returns the user agent.
	 * <p>
	 * The user agent, as reported in the HTTP <code>User-Agent</code> field.
	 */
	public String getUserAgent() {
		return this.userAgent_;
	}

	/**
	 * Returns the referer.
	 * <p>
	 * The referer, as reported in the HTTP <code>Referer</code> field.
	 */
	public String getReferer() {
		return this.referer_;
	}

	/**
	 * Returns the accept header.
	 * <p>
	 * The accept header, as reported in the HTTP <code>Accept</code> field.
	 */
	public String getAccept() {
		return this.accept_;
	}

	/**
	 * Returns if the user agent is a (known) indexing spider bot.
	 * <p>
	 * Note: currently the list of know bots is quite small. This method is used
	 * internally to render the web application for optimal indexing by bots:
	 * <ul>
	 * <li>there is no detection for JavaScript, instead the application is
	 * directly served assuming no JavaScript support.</li>
	 * <li>session information is omitted from the Urls.</li>
	 * <li>no sessions are created (they are immediately stopped after the
	 * request has been handled).</li>
	 * <li>auto-generated <code>id</code> and <code>name</code> attributes are
	 * omitted from DOM nodes. In this way, the generated page is always exactly
	 * the same.</li>
	 * </ul>
	 */
	public boolean agentIsSpiderBot() {
		return this.agent_ == WEnvironment.UserAgent.BotAgent;
	}

	/**
	 * Returns the web server signature.
	 * <p>
	 * The value of the CGI variable <code>SERVER_SIGNATURE</code>.
	 * <p>
	 * Example:
	 * <code>&lt;address&gt;Apache Server at localhost Port 80&lt;/address&gt;</code>.
	 */
	public String getServerSignature() {
		return this.serverSignature_;
	}

	/**
	 * Returns the web server software.
	 * <p>
	 * The value of the CGI variable <code>SERVER_SOFTWARE</code>.
	 * <p>
	 * Example: <code>&quot;Apache&quot;</code>
	 */
	public String getServerSoftware() {
		return this.serverSoftware_;
	}

	/**
	 * Returns the email address of the server admin.
	 * <p>
	 * The value of the CGI variable <code>SERVER_ADMIN</code>.
	 * <p>
	 * Example: <code>&quot;root@localhost&quot;</code>
	 */
	public String getServerAdmin() {
		return this.serverAdmin_;
	}

	/**
	 * Returns the IP address of the client.
	 * <p>
	 * The (most likely) IP address of the client that is connected to this
	 * session.
	 * <p>
	 * This is taken to be the first public address that is given in the
	 * Client-IP header, or in the X-Forwarded-For header (in case the client is
	 * behind a proxy). If none of these headers is present, the remote socket
	 * IP address is used.
	 */
	public String getClientAddress() {
		return this.clientAddress_;
	}

	/**
	 * Returns the initial internal path.
	 * <p>
	 * This is the internal path with which the application was started.
	 * <p>
	 * For an application deployed at <code>&quot;/stuff/app.wt&quot;</code>,
	 * the following three URLs are considered equivalent, and indicate an
	 * internal path <code>&quot;/this/there&quot;</code>: <code>
   http://www.mydomain.com/stuff/app.wt/this/there <br> 
   http://www.mydomain.com/stuff/app.wt#/this/there <br> 
   http://www.mydomain.com/stuff/app.wt?_=/this/there
  </code>
	 * <p>
	 * The last form is generated by Wt when the application ends with a
	 * &apos;/&apos;, as an alternative to the first form, which is then
	 * impossible.
	 * <p>
	 * 
	 * @see WApplication#setInternalPath(String path, boolean emitChange)
	 * @see WEnvironment#getDeploymentPath()
	 */
	public String getInternalPath() {
		return this.internalPath_;
	}

	/**
	 * Returns the deployment path.
	 * <p>
	 * This is the path at which the application is deployed.
	 * <p>
	 * 
	 * @see WEnvironment#getInternalPath()
	 */
	public String getDeploymentPath() {
		return this.session_.getDeploymentPath();
	}

	/**
	 * Returns the version of the Wt library.
	 * <p>
	 * Example: <code>&quot;1.99.2&quot;</code>
	 */
	public static String getLibraryVersion() {
		return "2.99.2";
	}

	// public void libraryVersion(bad java simple ref int series, bad java
	// simple ref int major, bad java simple ref int minor) ;
	/**
	 * Returns the Wt session id.
	 * <p>
	 * Retrieves the session id for this session. This is an auto-generated
	 * random alpha-numerical id, whose length is determined by settings in the
	 * configuration file.
	 */
	public String getSessionId() {
		return this.session_.getSessionId();
	}

	/**
	 * Returns a raw CGI environment variable.
	 * <p>
	 * Retrieves the value for the given CGI environment variable (like
	 * <code>&quot;SSL_CLIENT_S_DN_CN&quot;</code>), if it is defined, otherwise
	 * an empty string.
	 * <p>
	 * 
	 * @see WEnvironment#getServerSignature()
	 * @see WEnvironment#getServerSoftware()
	 * @see WEnvironment#getServerAdmin()
	 */
	public String getCgiValue(String varName) {
		return this.session_.getCgiValue(varName);
	}

	/**
	 * The type of the content provided to the browser.
	 * <p>
	 * This is determined by listening to the capabilities of the browser.
	 * Xhtml1 is chosen only if the browser reports support for it, and it is
	 * allowed in the configuration file (wt_config.xml).
	 * <p>
	 * Note that Wt makes also use of common non-standard techniques implemented
	 * in every major browser.
	 */
	public WEnvironment.ContentType getContentType() {
		return this.contentType_;
	}

	public WEnvironment.UserAgent getAgent() {
		return this.agent_;
	}

	public boolean agentIsIE() {
		return this.agent_.getValue() >= WEnvironment.UserAgent.IE.getValue()
				&& this.agent_.getValue() < WEnvironment.UserAgent.Opera
						.getValue();
	}

	public boolean agentIsIEMobile() {
		return this.agent_.getValue() >= WEnvironment.UserAgent.IEMobile
				.getValue()
				&& this.agent_.getValue() < WEnvironment.UserAgent.Opera
						.getValue();
	}

	public boolean agentIsOpera() {
		return this.agent_.getValue() >= WEnvironment.UserAgent.Opera
				.getValue()
				&& this.agent_.getValue() < WEnvironment.UserAgent.Safari
						.getValue();
	}

	public boolean agentIsWebKit() {
		return this.agent_.getValue() >= WEnvironment.UserAgent.WebKit
				.getValue()
				&& this.agent_.getValue() < WEnvironment.UserAgent.Konqueror
						.getValue();
	}

	public boolean agentIsSafari() {
		return this.agent_.getValue() >= WEnvironment.UserAgent.Safari
				.getValue()
				&& this.agent_.getValue() < WEnvironment.UserAgent.Chrome
						.getValue();
	}

	public boolean agentIsChrome() {
		return this.agent_.getValue() >= WEnvironment.UserAgent.Chrome
				.getValue()
				&& this.agent_.getValue() < WEnvironment.UserAgent.Konqueror
						.getValue();
	}

	public boolean agentIsGecko() {
		return this.agent_.getValue() >= WEnvironment.UserAgent.Gecko
				.getValue()
				&& this.agent_.getValue() < WEnvironment.UserAgent.BotAgent
						.getValue();
	}

	WebSession session_;
	boolean doesJavaScript_;
	boolean doesAjax_;
	boolean doesCookies_;
	private WEnvironment.UserAgent agent_;
	double dpiScale_;
	private WEnvironment.ContentType contentType_;
	private Map<String, List<String>> parameters_;
	private Map<String, String> cookies_;
	private Locale locale_;
	private String host_;
	private String userAgent_;
	String urlScheme_;
	private String referer_;
	private String accept_;
	private String serverSignature_;
	private String serverSoftware_;
	private String serverAdmin_;
	private String clientAddress_;
	private String pathInfo_;
	private String internalPath_;

	void init(WebRequest request) {
		Configuration conf = this.session_.getController().getConfiguration();
		this.parameters_ = request.getParameterMap();
		this.urlScheme_ = request.getUrlScheme();
		this.userAgent_ = request.getHeaderValue("User-Agent");
		this.referer_ = request.getHeaderValue("Referer");
		this.accept_ = request.getHeaderValue("Accept");
		this.serverSignature_ = request.getEnvValue("SERVER_SIGNATURE");
		this.serverSoftware_ = request.getEnvValue("SERVER_SOFTWARE");
		this.serverAdmin_ = request.getEnvValue("SERVER_ADMIN");
		this.pathInfo_ = request.getPathInfo();
		System.err.append(this.userAgent_).append('\n');
		this.agent_ = WEnvironment.UserAgent.Unknown;
		if (this.userAgent_.indexOf("MSIE 4") != -1
				|| this.userAgent_.indexOf("MSIE 5") != -1
				|| this.userAgent_.indexOf("IEMobile") != -1) {
			this.agent_ = WEnvironment.UserAgent.IEMobile;
		} else {
			if (this.userAgent_.indexOf("MSIE 6") != -1) {
				this.agent_ = WEnvironment.UserAgent.IE6;
			} else {
				if (this.userAgent_.indexOf("MSIE 7") != -1) {
					this.agent_ = WEnvironment.UserAgent.IE7;
				} else {
					if (this.userAgent_.indexOf("MSIE 8") != -1) {
						this.agent_ = WEnvironment.UserAgent.IE8;
					} else {
						if (this.userAgent_.indexOf("MSIE") != -1) {
							this.agent_ = WEnvironment.UserAgent.IE;
						}
					}
				}
			}
		}
		if (this.userAgent_.indexOf("Opera") != -1) {
			this.agent_ = WEnvironment.UserAgent.Opera;
		}
		if (this.userAgent_.indexOf("Chrome") != -1) {
			this.agent_ = WEnvironment.UserAgent.Chrome;
		} else {
			if (this.userAgent_.indexOf("Safari") != -1) {
				this.agent_ = WEnvironment.UserAgent.Safari;
			} else {
				if (this.userAgent_.indexOf("WebKit") != -1) {
					this.agent_ = WEnvironment.UserAgent.WebKit;
				} else {
					if (this.userAgent_.indexOf("Konqueror") != -1) {
						this.agent_ = WEnvironment.UserAgent.Konqueror;
					} else {
						if (this.userAgent_.indexOf("Gecko") != -1) {
							this.agent_ = WEnvironment.UserAgent.Gecko;
						}
					}
				}
			}
		}
		if (this.userAgent_.indexOf("Firefox/3.2.") != -1) {
			this.agent_ = WEnvironment.UserAgent.Firefox3_2;
		} else {
			if (this.userAgent_.indexOf("Firefox/3.1.") != -1) {
				this.agent_ = WEnvironment.UserAgent.Firefox3_1;
			} else {
				if (this.userAgent_.indexOf("Firefox/3.") != -1) {
					this.agent_ = WEnvironment.UserAgent.Firefox3;
				}
			}
		}
		if (regexMatchAny(this.userAgent_, conf.getBotList())) {
			this.agent_ = WEnvironment.UserAgent.BotAgent;
		}
		if (conf.isBehindReverseProxy()) {
			String forwardedHost = request.getHeaderValue("X-Forwarded-Host");
			if (forwardedHost.length() != 0) {
				int i = forwardedHost.lastIndexOf(',');
				if (i == -1) {
					this.host_ = forwardedHost;
				} else {
					this.host_ = forwardedHost.substring(i + 1);
				}
			} else {
				this.host_ = request.getHeaderValue("Host");
			}
		} else {
			this.host_ = request.getHeaderValue("Host");
		}
		if (this.host_.length() == 0) {
			this.host_ = request.getServerName();
			if ((request.getServerPort() + "").length() != 0) {
				this.host_ += ":" + (request.getServerPort() + "");
			}
		}
		String ips = "";
		ips = request.getHeaderValue("Client-IP") + ","
				+ request.getHeaderValue("X-Forwarded-For");
		for (int pos = 0; pos != -1;) {
			int komma_pos = ips.indexOf(',', pos);
			this.clientAddress_ = ips.substring(pos, pos + komma_pos);
			this.clientAddress_.trim();
			if (!this.clientAddress_.startsWith("10.")
					&& !this.clientAddress_.startsWith("172.16.")
					&& !this.clientAddress_.startsWith("192.168.")) {
				break;
			}
			if (komma_pos != -1) {
				pos = komma_pos + 1;
			}
		}
		if (this.clientAddress_.length() == 0) {
			this.clientAddress_ = request.getEnvValue("REMOTE_ADDR");
		}
		String cookie = request.getHeaderValue("Cookie");
		this.doesCookies_ = cookie.length() != 0;
		if (this.doesCookies_) {
			this.parseCookies(cookie);
		}
		this.locale_ = request.getLocale();
		if (this.session_.getController().getConfiguration()
				.isSendXHTMLMimeType()
				&& this.accept_.indexOf("application/xhtml+xml") != -1) {
			this.contentType_ = WEnvironment.ContentType.XHTML1;
		}
	}

	void setInternalPath(String path) {
		this.internalPath_ = path.length() == 0 ? "/" : path;
	}

	WEnvironment(WebSession session) {
		this.session_ = session;
		this.doesJavaScript_ = false;
		this.doesAjax_ = false;
		this.doesCookies_ = false;
		this.dpiScale_ = 1;
		this.contentType_ = WEnvironment.ContentType.HTML4;
		this.parameters_ = new HashMap<String, List<String>>();
		this.cookies_ = new HashMap<String, String>();
		this.locale_ = new Locale("");
		this.host_ = "";
		this.userAgent_ = "";
		this.urlScheme_ = "";
		this.referer_ = "";
		this.accept_ = "";
		this.serverSignature_ = "";
		this.serverSoftware_ = "";
		this.serverAdmin_ = "";
		this.clientAddress_ = "";
		this.pathInfo_ = "";
		this.internalPath_ = "";
	}

	// private String parsePreferredAcceptValue(String value) ;
	private void parseCookies(String str) {
		List<String> cookies = new ArrayList<String>();
		cookies = new ArrayList<String>(Arrays.asList(str.split(";")));
		for (int i = 0; i < cookies.size(); ++i) {
			int e = cookies.get(i).indexOf('=');
			String cookieName = cookies.get(i).substring(0, 0 + e);
			String cookieValue = e != -1 && cookies.get(i).length() > e + 1 ? cookies
					.get(i).substring(e + 1)
					: "";
			cookieName.trim();
			cookieValue.trim();
			try {
				cookieName = java.net.URLDecoder.decode(cookieName, "UTF-8");
			} catch (UnsupportedEncodingException eee) {
				// I don't think so
			}
			;
			try {
				cookieValue = java.net.URLDecoder.decode(cookieValue, "UTF-8");
			} catch (UnsupportedEncodingException eee) {
				// I don't think so
			}
			;
			if (!cookieName.equals("")) {
				this.cookies_.put(cookieName, cookieValue);
			}
		}
	}

	boolean agentSupportsAjax() {
		Configuration conf = this.session_.getController().getConfiguration();
		boolean matches = regexMatchAny(this.userAgent_, conf
				.getAjaxAgentList());
		if (conf.isAjaxAgentWhiteList()) {
			return matches;
		} else {
			return !matches;
		}
	}

	static boolean regexMatchAny(String agent, List<String> regexList) {
		String s = agent;
		for (int i = 0; i < regexList.size(); ++i) {
			Pattern expr = Pattern.compile(regexList.get(i));
			if (expr.matcher(s).matches()) {
				return true;
			}
		}
		return false;
	}

	public static String wt_class = "Wt2_99_2";
}
