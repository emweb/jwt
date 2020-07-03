/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * JWt application configuration class.
 * <p>
 * This class holds the configuration for JWt, controlling general features of the interaction between
 * the browser and the web application.
 * 
 * @see WtServlet#getConfiguration()
 */
public class Configuration {
	private static Logger logger = LoggerFactory.getLogger(AbstractJSignal.class);

	public enum SessionTracking {
		CookiesURL, Auto, Combined
	}

	/**
	 * An enumeration for the level of error reporting in case of client-side (JavaScript) errors.
	 */
	public enum ErrorReporting {
		/**
		 * The application silently dies, and this allows the use of standard debugging
		 * capabilities of the browser to diagnose the problem (convenient during development).
		 */
		NoErrors,
		
		/**
		 * The application dies with a message to the user indicating an internal error.
		 * This is the default behaviour.
		 */
		ErrorMessage,

		/**
		 * The application dies with a message and if possible a stack trace of the problem.
		 */
		ErrorMessageWithStack
	}

	private HashMap<String, String> properties_ = new HashMap<String, String>();
	private String redirectMessage_ = "Plain HTML version";
	private boolean sendXHTMLMimeType = false;
	private boolean inlineCss_ = true;
	private boolean webGLDetect_ = true;
	private ArrayList<String> botList = new ArrayList<String>();
	private ArrayList<String> ajaxAgentList = new ArrayList<String>();
	private boolean ajaxAgentWhiteList = false;
	private ErrorReporting errorReporting = ErrorReporting.ErrorMessage;

	private String favicon = "/favicon.ico";
	private boolean progressiveBootstrap = false;
	
	private int sessionTimeout = 600;
	private int idleTimeout = -1;
	private int indicatorTimeout = 500;
	private int doubleClickTimeout = 200;
	private int bootstrapTimeout = 10;
	private String uaCompatible = "";
	private List<MetaHeader> metaHeaders = new ArrayList<MetaHeader>();
	private List<HeadMatter> headMatter = new ArrayList<HeadMatter>();
	private int internalDeploymentSize = 0;
	private long maxRequestSize = 1024*1024; // 1 Megabyte
	private boolean behindReverseProxy = false;
	private boolean webSocketsEnabled = false;
	private long asyncContextTimeout = 90000;

	private Collection<String> allowedOrigins_ = Collections.<String>emptySet();

	/**
	 * Creates a default configuration.
	 */
	public Configuration() {
		properties_.put(WApplication.RESOURCES_URL, "/wt-resources/");
		
		botList.add(".*Googlebot.*");
		botList.add(".*msnbot.*");
		botList.add(".*Slurp.*");
		botList.add(".*Crawler.*");
		botList.add(".*Bot.*");
		botList.add(".ia_archiver.*");
		botList.add(".*Googlebot.*");
		botList.add(".*Twiceler.*");
	}

	/**
	 * Reads a configuration from an XML file.
	 * <p>
	 * An example configuration file can be found in the JWt source distribution.
	 * 
	 * @param configurationFile
	 */
	public Configuration(File configurationFile) {
		logger.info("Reading configuration file: " + configurationFile.getAbsolutePath());
		
		properties_.put(WApplication.RESOURCES_URL, "/wt-resources/");
		
		final String errorMessage = "Error parsing configuration file: ";

		if (configurationFile != null) {
			DocumentBuilder docBuilder = null;
			Document doc = null;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setIgnoringElementContentWhitespace(true);
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(errorMessage + e.getMessage());
			}
			try {
				doc = docBuilder.parse(configurationFile);
			} catch (Exception e) {
				throw new RuntimeException(errorMessage + e.getMessage());
			}

			NodeList nl = doc.getElementsByTagName("wt-app");

			if (nl.getLength() > 0) {
				NodeList elements = nl.item(0).getChildNodes();
				Node node;
				for (int i = 0; i < elements.getLength(); i++) {
					node = elements.item(i);
					if (node.getNodeName().equalsIgnoreCase("debug")) {
						setDebug(parseBoolean(errorMessage, node));
					} else if (node.getNodeName().equalsIgnoreCase("properties")) {
						NodeList properties = node.getChildNodes();
						for (int j = 0; j < properties.getLength(); j++) { 
							Node n = properties.item(j);
							if (n.getNodeName().equals("property")) {
								Node propertyName = n.getAttributes().getNamedItem("name");
								if (propertyName != null)
									properties_.put(propertyName.getTextContent().trim(), n.getTextContent().trim());
							}
						}
					} else if (node.getNodeName().equalsIgnoreCase("progressive-bootstrap")) {
						setProgressiveBootstrap(parseBoolean(errorMessage, node));
					} else if (node.getNodeName().equalsIgnoreCase("ua-compatible")) {
						setUaCompatible(node.getTextContent().trim());
					} else if (node.getNodeName().equalsIgnoreCase("send-xhtml-mime-type")) {
						setSendXHTMLMimeType(parseBoolean(errorMessage, node));
					} else if (node.getNodeName().equalsIgnoreCase("redirect-message")) {
						setRedirectMessage(node.getTextContent().trim());
					} else if (node.getNodeName().equalsIgnoreCase("inline-css")) {
						setInlineCss(parseBoolean(errorMessage, node));
					} else if (node.getNodeName().equalsIgnoreCase("favicon")) {
						setFavicon(node.getTextContent().trim());
					} else if (node.getNodeName().equalsIgnoreCase("user-agents")) {
						if (node.getAttributes().getNamedItem("type") == null) {
							throw new RuntimeException(errorMessage + "user-agent elements require  a type specification");
						} else if (node.getAttributes().getNamedItem("type").getTextContent().trim().equals("ajax")) {
							String mode = node.getAttributes().getNamedItem("mode").getTextContent().trim();

							if (mode.equals("black-list"))
								ajaxAgentWhiteList = false;
							else if (mode.equals("white-list"))
								ajaxAgentWhiteList = true;
							else
								throw new RuntimeException(errorMessage + "unsupported li mode: " + mode);

							parseUserAgents(errorMessage, node, ajaxAgentList);
						} else if (node.getAttributes().getNamedItem("type").getTextContent().trim().equals("bot")) {
							parseUserAgents(errorMessage, node, botList);
						}
					} else if (node.getNodeName().equalsIgnoreCase("allowed-origins")) {
						String origins = node.getTextContent().trim();
						for (String origin : origins.split(",")) {
							origin = origin.trim();
							if (origin.isEmpty())
								continue;
							if (this.allowedOrigins_.isEmpty())
								this.allowedOrigins_ = new HashSet<String>();
							this.allowedOrigins_.add(origin);
						}
					}
				}
			}
		}
	}

	private void parseUserAgents(String errorMessage, Node node, List<String> list) {
		NodeList userAgents = node.getChildNodes();
		Node n;
		for (int i = 0; i < userAgents.getLength(); i++) {
			n = userAgents.item(i);
			if (n.getNodeName().equals("user-agent")) {
				list.add(node.getTextContent().trim());
			}
		}
	}

	private boolean parseBoolean(String errorMessage, Node n) {
		try {
			return Boolean.parseBoolean(n.getTextContent().trim());
		} catch (Exception e) {
			throw new RuntimeException(errorMessage + "Cannot parse boolean value from element " + n.getNodeName());
		}
	}

	/**
	 * Sets properties.
	 * <br/>
	 * Examples: 
	 * <ul>
	 * 	<li>smtp.host: SMTP host used by JWt to send out emails </li>
	 * 	<li>smtp.port: SMTP port used by JWt to send out emails</li>
	 * </ul>
	 * 
	 * @param properties
	 * 
	 * @see #getProperties()
	 * @see #getProperty(String)
	 */
	public void setProperties(HashMap<String, String> properties) {
		this.properties_ = properties;
	}

	/**
	 * Returns configured properties.
	 * <p>
	 * Properties may be used to adapt applications to their deployment environment.
	 *
	 * @return a map of all properties.
	 */
	public HashMap<String, String> getProperties() {
		return properties_;
	}

	/**
	 * Returns a property value.
	 * <p>
	 * Properties may be used to adapt applications to their deployment environment.
	 * 
	 * @param name
	 * @return the property value, or <code>null</code> if the property has not been defined.
	 */
	public String getProperty(String name) {
		return properties_.get(name);
	}

	/**
	 * Sets the plain-HTML redirect message.
	 * <p>
	 * By default, JWt will use an automatic redirect to start the application when the browser does not support
	 * JavaScript. However, browsers are not required to follow the redirection, and in some situations (when using
	 * XHTML), such automatic redirection is not supported.
	 * <p>
	 * This configures the text that is shown in the anchor which the user may click to be redirected to a basic HTML
	 * version of your application.
	 */
	public void setRedirectMessage(String redirectMessage) {
		this.redirectMessage_ = redirectMessage;
	}

	/**
	 * Returns the plain-HTML redirect message.
	 * 
	 * @return the plain-HTML redirect message.
	 * 
	 * @see #setRedirectMessage(String)
	 */
	public String getRedirectMessage() {
		return redirectMessage_;
	}

	/**
	 * Sets whether XHTML should be used (if supported by the client).
	 * <p>
	 * JWt renders XHTML1 (XML variant of HTML) that is backward-compatible with HTML. Using XHTML, JWt is capable of
	 * supporting XHTML-only features such as embedded SVG or MathML.
	 * <p>
	 * When enabled, JWt sets an XHTML mime-type (<tt>application/xhtml+xml</tt>) when the browser reports support for
	 * it. Most notably, Internet Explorer does not support it. Because XHTML and HTML are slightly different with
	 * respect to default CSS rules, you may want to disable sending the XHTML mime-type all-together, at least if you
	 * are not using SVG (used by the {@link WPaintedWidget}).
	 */
	public void setSendXHTMLMimeType(boolean sendXHTMLMimeType) {
		this.sendXHTMLMimeType = sendXHTMLMimeType;
	}
	/**
	 * Returns whether XHTML should be used (if supported by the client).
	 * 
	 * @return whether XHTML should be used.
	 * 
	 * @see #setSendXHTMLMimeType(boolean)
	 */
	public boolean sendXHTMLMimeType() {
		return sendXHTMLMimeType;
	}

	boolean serializedEvents() {
		return false;
	}

	/**
	 * Configures debugging.
	 * <p>
	 * Currently, the only effect of debugging is that JavaScript exceptions are not caught but allowed to propagate
	 * so that you can inspect the stack trace.
	 * <p>
	 * Debugging is off by default.
	 * 
	 * @deprecated use {@link #setErrorReporting(ErrorReporting)} instead.
	 */
	public void setDebug(boolean how) {
		if (how)
			errorReporting = ErrorReporting.NoErrors;
		else 
			errorReporting = ErrorReporting.ErrorMessage;
	}

	/**
	 * Returns whether debugging is enabled.
	 * 
	 * @return whether debugging is enabled.
	 * 
	 * @see #setDebug(boolean)
	 * @deprecated use {@link #getErrorReporting()} instead.
	 */
	public boolean debug() {
		return errorReporting == ErrorReporting.NoErrors;
	}

	int getServerPushTimeout() {
		return 50;
	}

	/**
	 * Sets whether inline CSS may be generated.
	 * <p>
	 * This option configures whether CSS rules added to the inline stylesheet {@link WApplication#getStyleSheet()} are
	 * rendered.
	 * <p>
	 * Some pedantic accessibility guidelines may forbid inline CSS.
	 * <p>
	 * Note: some widgets, such as {@link WTreeView}, dynamically manipulate rules in this stylesheet, and will no longer work
	 * properly when inline CSS is disabled.
	 * 
	 * @param inlineCss
	 */
	public void setInlineCss(boolean inlineCss) {
		this.inlineCss_ = inlineCss;
	}

	/**
	 * Returns whether inline CSS may be generated.
	 * 
	 * @return whether inline CSS may be generated.
	 * 
	 * @see #setInlineCss(boolean)
	 */
	public boolean isInlineCss() {
		return inlineCss_;
	}

	public boolean isWebglDetect() {
		return webGLDetect_;
	}

	/**
	 * Configures agents that may be served an AJAX version of the application.
	 * <p>
	 * When <i>isWhiteList</i> is <code>true</code>, the given list exhaustively indicates all user agents that
	 * will be served an AJAX version of the application. When <i>isWhiteList</i> is <code>false</code>, the given
	 * list excludes some user agents that may be served an AJAX version of the application.
	 * <p>
	 * Each entry in <i>ajaxAgentList</i> is a regular expression against which the browser-reported user agent
	 * is compared.
	 * <p>
	 * By default, JWt will serve an AJAX version when JavaScript and AJAX support are detected.
	 *
	 * @param ajaxAgentList a list of regular expressions that identify user agents
	 * @param isWhiteList whether the list is a white list or black list.
	 */
	public void setAjaxAgentList(ArrayList<String> ajaxAgentList, boolean isWhiteList) {
		this.ajaxAgentList = ajaxAgentList;
		this.ajaxAgentWhiteList = isWhiteList;
	}

	/**
	 * Returns the list of user agents that are (not) considered for AJAX sessions.
	 * <p>
	 * Depending on the value of {@link #isAjaxAgentWhiteList()}, the list is a white-list or a black-list.
	 *
	 * @return the list of user agents that are (not) considered for AJAX sessions.
	 * 
	 * @see #setAjaxAgentList(ArrayList, boolean)
	 * @see #agentSupportsAjax(String)
	 */
	public ArrayList<String> getAjaxAgentList() {
		return ajaxAgentList;
	}

	/**
	 * Returns whether the {@link #getAjaxAgentList()} is a white list or black list.
	 * 
	 * @return whether the {@link #getAjaxAgentList()} is a white list or black list.
	 * 
	 * @see #setAjaxAgentList(ArrayList, boolean)
	 * @see #agentSupportsAjax(String)
	 */
	public boolean isAjaxAgentWhiteList() {
		return ajaxAgentWhiteList;
	}


	/**
	 * Returns whether the user agent should be considered as one with Ajax support.
	 * 
	 * @return whether the user agent should be considered as one with Ajax support.
	 * 
	 * @see #setAjaxAgentList(ArrayList, boolean)
	 */
	public boolean agentSupportsAjax(String userAgent) {
		boolean inList = false;
		
		for (String regex : ajaxAgentList) {
			if (userAgent.matches(regex)) {
				inList = true;
				break;
			}
		}
		
		if (ajaxAgentWhiteList)
			return inList;
		else
			return !inList;
	}

	/**
	 * Sets the list of bots.
	 * <p>
	 * JWt considers three types of sessions:
	 * <ul>
	 *   <li>AJAX sessions: use AJAX and JavaScript</li>
	 *   <li>plain HTML sessions: use plain old server GETs and POSTs</li>
	 *   <li>bots: have clean internal paths (see {@link WApplication#setInternalPath(String)}) and no persistent sessions</li>
	 * </ul>
	 *
	 * By default, JWt does a browser detection to distinguish between
	 * the first two: if a browser supports JavaScript (and has it
	 * enabled), and has an AJAX DOM API, then AJAX sessions are chosen,
	 * otherwise plain HTML sessions.
	 * <p>
     * Here, you can specify user agents that should be should be
     * treated as bots.
     * <p>
     * The default configuration sets the following list:
     * <ul>
     *   <li>.*Googlebot.*</li>
     *   <li>.*msnbot.*</li>
	 *   <li>.*Slurp.*</li>
	 *   <li>.*Crawler.*</li>
	 *   <li>.*Bot.*</li>
	 *   <li>.*ia_archiver.*</li>
	 *   <li>.*Twiceler.*</li>
	 * </ul>
	 */
	public void setBotList(ArrayList<String> botList) {
		this.botList = botList;
	}

	/**
	 * Returns the list of user agents that are treated as bots.
	 * 
	 * @return the list of user agents that are treated as bots.
	 * 
	 * @see #setBotList(ArrayList)
	 */
	public ArrayList<String> getBotList() {
		return botList;
	}

	/**
	 * Returns whether the user agent is a bot.
	 * 
	 * @see #setBotList(ArrayList)
	 */
	public boolean agentIsBot(String userAgent) {
		for (String regex : botList) {
			if (userAgent.matches(regex))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Configures a path to a favicon.
	 * 
	 * By default, a browser will fetch a favicon from "/favicon.ico". <br>
	 * Using this setting, you may provide a custom path to the favicon.
	 * <p>
	 * The default value is "".
	 */
	public void setFavicon(String favicon) {
		this.favicon = favicon;
	}

	/**
	 * Returns the path for the favicon
	 * 
	 * @return the path for the favicon
	 * 
	 * @see #setFavicon(String)
	 */
	public String getFavicon() {
		return this.favicon;
	}

	/**
	 * Sets whether the progressive bootstrap method is used.
	 * <p>
	 * Since JWt 2.99.4, a new bootstrap method has been added (initially
	 * proposed by Anthony roger Buck). While the default bootstrap already
  	 * honors the principle of graceful degradation, this bootstrap
  	 * implements this using the principle of <a href="http://en.wikipedia.org/wiki/Progressive_enhancement">progressive enhancement</a>
	 * (and quite literally so).
	 * <p>
	 * This bootstrap method will initially assume that the user agent is a
  	 * plain HTML user-agent and immediately create the application (with
  	 * {@link WEnvironment#hasAjax()} always returning <code>false</code>).
  	 * The initial response will contain the initial page suitable for a plain HTML
  	 * user-agent.
	 * <p>
  	 * JavaScript embedded in this page will sense for AJAX support and
  	 * trigger a second request which progresses the application to an AJAX
  	 * application (without repainting the user interface). To that extent,
  	 * it will change {@link WEnvironment#hasAjax()} to return <code>true</code>, and
  	 * invoke {@link WApplication#enableAjax()} which in turn propagates
  	 * {@link WWidget#enableAjax()} through the widget hierarchy. This upgrade
  	 * happens in the back-ground, unnoticed to the user.
	 * <p>
  	 * This mitigates disadvantages associated with the default bootstrap, which implements
  	 * a browser detection first after it starts the application:
  	 * <ul>
  	 *   <li>the redirection without JavaScript support may not be supported
  	 *     by all user agents, leaving these with a link and a {@link #getRedirectMessage()}.
  	 *    </li>
  	 *   <li>there is an additional round-trip before any contents is rendered</li>
  	 *   <li>for an AJAX user interface, all contents will be loaded through
  	 *     JavaScript. This has a draw-back that IE may delay applying external
  	 *     stylesheets after the contents has been rendered, which might cause
  	 *     some confusion, and some 3rd party JavaScript libraries do not support
  	 *     being loaded on-demand (with as most notable example, Google ads).
     *   </li>
     * </ul>
	 */
	public void setProgressiveBootstrap(boolean enable) {
		this.progressiveBootstrap = enable;
	}

	/**
	 * Returns whether the progressive bootstrap method is used.
	 * 
	 * The method may take into account the internalPath to differentiate between
	 * certain deep links which display widgets that do not progress well (such 
	 * as table views or tree views).
	 * 
	 * @param internalPath the initial internal path  
	 * @return whether the progressive bootstrap method is used.
	 * 
	 * @see #setProgressiveBootstrap(boolean).
	 */
	public boolean progressiveBootstrap(String internalPath) {
		return this.progressiveBootstrap ;
	}

	public int getKeepAlive() {
		return getSessionTimeout() / 2;
	}

	public int getMultiSessionCookieTimeout() {
		return getSessionTimeout() * 2;
	}
	
	/**
	 * Returns the session timeout.
	 * 
	 * @return the session timeout.
	 */
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	/**
	 * Returns the idle timeout (in seconds).
	 *
	 * @return the idle timeout.
	 */
	public int getIdleTimeout() {
		return idleTimeout;
	}

	/**
	 * Sets the idle timeout.
	 *
	 * When the user does not interact with the application for the set number of seconds,
	 * WApplication#idleTimeout() is called. By default, this method quits the application
	 * immediately, but it can be overridden if different behaviour is desired.
	 *
	 * This feature can be used to prevent others from taking over a session
	 * when the device that the Wt application is being used from is left behind,
	 * and is most effective in combination with a fairly short session timeout.
	 *
	 * The default is -1 (disabled)
	 */
	public void setIdleTimeout(int timeout) {
		this.idleTimeout = timeout;
	}
	
	/**
	 * Sets the maximum request size (in bytes).
	 * 
	 * The default value is 1MB
	 */
	public void setMaximumRequestSize(long requestSize) {
		this.maxRequestSize = requestSize;
	}
	
	void setSessionTimeout(int sessionTimeout) {
		if (sessionTimeout <= 0)
			this.sessionTimeout = 10 * 60;
		else
			this.sessionTimeout = sessionTimeout;
	}


	/**
	 * Returns the double click timeout.
	 */
	public int getDoubleClickTimeout() {
		return doubleClickTimeout;
	}
	
	/**
	 * Sets the double click timeout.
	 * 
	 * The default value is 200 (ms).
	 */
	public void setDoubleClickTimeout(int doubleClickTimeout) {
		this.doubleClickTimeout = doubleClickTimeout;
	}

	/**
	 * Returns the loading indicator timeout.
	 * 
	 * When a response time for an AJAX call exceeds this time, a loading indicator is shown.
	 * 
	 * @return the loading indicator timeout in milliseconds.
	 */
	public int getIndicatorTimeout() {
		return indicatorTimeout;
	}
	
	/**
	 * Sets the loading indicator timeout.
	 * 
	 * When a response time for an AJAX call exceeds this time, a loading indicator is shown.
	 * 
	 * @param timeout the timeout in milliseconds.
	 */
	public void setIndicatorTimeout(int timeout) {
		this.indicatorTimeout = timeout;
	}

	
	public int getBootstrapTimeout() {
		return bootstrapTimeout;
	}

	/**
	 * Returns the error reporting mode.
	 */
	public ErrorReporting getErrorReporting() { 
		return errorReporting; 
	}
	
	/**
	 * Sets the error reporting mode.
	 */
	public void setErrorReporting(ErrorReporting err) { 
		errorReporting = err;
	}

	/**
	 * Configures different rendering engines for certain browsers.
	 * 
	 * Currently this is only used to select IE7 compatible rendering
	 * engine for IE8, which solves problems of unreliable and slow
	 * rendering performance for VML which Microsoft broke in IE8.
	 *
     * Before 3.3.0, the default value was IE8=IE7, but since 3.3.0
	 * this has been changed to an empty string (i.e. let IE8 use the
	 * standard IE8 rendering engine) to take advantage of IE8's
	 * improved CSS support. 
	 */
	public void setUaCompatible(String uaCompatible) {
		this.uaCompatible = uaCompatible;
	}
	
	/**
	 * Returns UA compatibility selection
	 * 
	 * @see #setUaCompatible(String)
	 */
	public String getUaCompatible() {
		return uaCompatible;
	}
	
	/**
	 * Sets the TinyMCE version to be used.
	 * 
	 * The default version is 3.
	 * 
	 * @param version must be 3 or 4
	 * 
	 * @see WTextEdit
	 */
	public void setTinyMCEVersion(int version) {
		this.properties_.put("tinyMCEVersion", "" + version);
	}

	/**
	 * Enables or disables the use of web sockets
	 */
	public void setWebSocketsEnabled(boolean enabled) {
		this.webSocketsEnabled = enabled;
	}
	
	boolean webSockets() {
		return webSocketsEnabled;
	}

	/*
	 * The following are not yet enabled for JWt
	 */

	boolean splitScript() {
		return false;
	}

	boolean sessionIdCookie() {
		return false;
	}

	boolean ajaxPuzzle() {
		return false;
	}
	
	/** Returns the maximum request size.
	 */
	public long getMaxRequestSize() {
		return maxRequestSize;
	}

	SessionTracking getSessionTracking() {
		return SessionTracking.Auto;
	}

	boolean reloadIsNewSession() {
		return true;
	}

	/**
	 * Configures whether the application is hosted behind a reverse proxy.
	 * 
	 * @see #isBehindReverseProxy()
	 */
	public void setBehindReverseProxy(boolean enabled) {
		this.behindReverseProxy = enabled;
	}
	
	/**
	 * Returns whether we are deployment behind a reverse proxy.
	 * When configured behind a reverse proxy, typical headers set
	 * by the reverse proxy are interpreted correctly:
	 *  - X-Forwarded-Host
	 *  - X-Forwarded-Proto
	 */
	public boolean isBehindReverseProxy() {
		return this.behindReverseProxy;
	}

	public boolean isCookieChecks() {
		return true;
	}

	public int internalDeploymentSize() {
		return internalDeploymentSize;
	}
	
	public void setInternalDeploymentSize(int size) {
		this.internalDeploymentSize = size;
    if (size == 0)
      getProperties().put(WApplication.RESOURCES_URL, "/wt-resources/");
    else
      getProperties().put(WApplication.RESOURCES_URL, "wt-resources/");
	}

	/**
	 * Returns configured meta headers.
	 */
	public List<MetaHeader> getMetaHeaders() {
		return metaHeaders;
	}

	/**
	 * Returns configured head matter.
	 * Like meta headers, but also supports e.g. &lt;link&gt; tags.
	 */
	public List<HeadMatter> getHeadMatter() {
		return headMatter;
	}

	/**
	 * Sets (static) meta headers. This is an alternative to using
	 * {@link WApplication#addMetaHeader(String, CharSequence)}, but having the
	 * benefit that they are added to all sessions.
	 */
	public void setMetaHeaders(List<MetaHeader> headers) {
		this.metaHeaders = headers;
	}

	/**
	 * Sets (static) head matter.
	 * Like meta headers, but also supports e.g. &lt;link&gt; tags.
	 */
	public void setHeadMatter(List<HeadMatter> headMatter) {
		this.headMatter = headMatter;
	}

	public boolean isAllowedOrigin(String origin) {
		if (this.allowedOrigins_.size() == 1 &&
			"*".equals(this.allowedOrigins_.iterator().next()))
			return true;
		else
			return this.allowedOrigins_.contains(origin);
	}

	/**
	 * Sets the list of origins that are allowed for CORS
	 * (only supported for WidgetSet entry points)
	 * 
	 * The default is empty (no origins are allowed).
	 */
	public void setAllowedOrigins(Collection<String> origins) {
		this.allowedOrigins_ = origins;
	}

	/**
	 * Get async context timeout for WtServlet requests
	 */
	public long getAsyncContextTimeout() {
		return asyncContextTimeout;
	}

	/**
	 * Set async context timeout for WtServlet requests
	 */
	public void setAsyncContextTimeout(long asyncContextTimeout) {
		this.asyncContextTimeout = asyncContextTimeout;
	}
}
