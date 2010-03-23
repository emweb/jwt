/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;
import eu.webtoolkit.jwt.utils.JarUtils;
import eu.webtoolkit.jwt.utils.StreamUtils;

/**
 * The abstract Wt servlet class.
 * <p>
 * This servlet processes all requests for a JWt application. You will need to specialize this class to provide an entry
 * point to your web application.
 * <p>
 * For each new session {@link #createApplication(WEnvironment)} is called to create a new {@link WApplication} object for that session.
 * The web controller that is implemented by this servlet validates each incoming request, and takes the appropriate action by either
 * notifying the application of an event, or serving a {@link WResource}.
 */
public abstract class WtServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String WT_WEBSESSION_ID = "wt-websession";

	static final String Boot_html;
	static final String Plain_html;
	static final String Wt_js;
	static final String CommAjax_js;
	static final String CommScript_js;
	static final String Hybrid_html;
	static final String JQuery_js;

	static {
		Boot_html = readFile("/eu/webtoolkit/jwt/skeletons/Boot.html");
		Plain_html = readFile("/eu/webtoolkit/jwt/skeletons/Plain.html");
		Hybrid_html = readFile("/eu/webtoolkit/jwt/skeletons/Hybrid.html");
		Wt_js = readFile("/eu/webtoolkit/jwt/skeletons/Wt.min.js");
		CommAjax_js = readFile("/eu/webtoolkit/jwt/skeletons/CommAjax.js");
		CommScript_js = readFile("/eu/webtoolkit/jwt/skeletons/CommScript.js");
		JQuery_js = readFile("/eu/webtoolkit/jwt/skeletons/jquery.min.js");
	}

	private InputStream getResourceStream(final String fileName) throws FileNotFoundException {
		return this.getClass().getResourceAsStream("/eu/webtoolkit/jwt/" + fileName);
	}

	private static String readFile(final String fileName) {
		return JarUtils.getInstance().readTextFromJar(fileName);
	}

	/**
	 * Constructor.
	 * <p>
	 * Instantiates the servlet using the default configuration.
	 * 
	 * @see #getConfiguration()
	 */
	public WtServlet() {
		this.configuration = new Configuration();
	}

	void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		String pathInfo = request.getPathInfo();
		String resourcePath = configuration.getProperty(WApplication.RESOURCES_URL);
		if (pathInfo != null && pathInfo.startsWith(resourcePath)) {
			String fileName = "wt-resources/";
			pathInfo = pathInfo.substring(resourcePath.length());
			while (pathInfo.charAt(0) == '/')
				pathInfo = pathInfo.substring(1);
			fileName += pathInfo;
			try {
				InputStream s = getResourceStream(fileName);
				if (s != null) {
					StreamUtils.copy(s, response.getOutputStream());
					response.getOutputStream().flush();
				} else {
					response.setStatus(404);
				}
			} catch (FileNotFoundException e) {
				response.setStatus(404);
				e.printStackTrace();
			} catch (IOException e) {
				response.setStatus(500);
				e.printStackTrace();
			}
			return;
		}

		HttpSession jsession = request.getSession();
		WebSession wsession = (WebSession) jsession.getAttribute(WT_WEBSESSION_ID);
		getConfiguration().setSessionTimeout(jsession.getMaxInactiveInterval());

		if (wsession == null) {
			String applicationTypeS = this.getServletConfig().getInitParameter("ApplicationType");
			
			EntryPointType applicationType;
			if (applicationTypeS == null || applicationTypeS.equals("") || applicationTypeS.equals("Application")) {
				applicationType = EntryPointType.Application; 
			} else if (applicationTypeS.equals("WidgetSet")) {
				applicationType = EntryPointType.WidgetSet; 
			} else {
				throw new WtException("Illegal application type: " + applicationTypeS);
			}
			
			wsession = new WebSession(this, jsession.getId(), applicationType, this.configuration.getFavicon(), new WebRequest(request));
			jsession.setAttribute(WT_WEBSESSION_ID, wsession);
		}

		try {
			WebRequest webRequest = new WebRequest(request);
			WebResponse webResponse = new WebResponse(response, webRequest);
			if (!wsession.handleRequest(webRequest, webResponse)) {
				System.err.println("Session exiting:" + jsession.getId());
				jsession.setAttribute(WT_WEBSESSION_ID, null);
				jsession.invalidate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Implement the GET request.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	/**
	 * Implement the POST request.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	/**
	 * Returns the JWt configuration.
	 * <p>
	 * You should only modify the configuration from the servlet constructor.
	 * 
	 * @return the configuration.
	 */
	public Configuration getConfiguration() {
		return configuration;
	}

	/**
	 * Sets the JWt configuration.
	 * <p>
	 * You should only set the configuration from the servlet constructor.
	 * 
	 * @param configuration
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	WApplication doCreateApplication(WebSession session) {
		return createApplication(session.getEnv());
	}

	void removeSession(String sessionId) {
		// in JWt, this is a NOOP: instead we interpret the return value of handleRequest()
	}

	/**
	 * Creates a new application for a new session.
	 * 
	 * @param env the environment that describes the new user (agent) and initial parameters
	 * @return a new application object.
	 */
	public abstract WApplication createApplication(WEnvironment env);

	private Configuration configuration;

}