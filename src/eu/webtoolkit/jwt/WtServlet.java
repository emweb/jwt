/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebRequest.ProgressListener;
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

	static final String WT_WEBSESSION_ID = "wt-websession";

	static final String Boot_html;
	static final String Plain_html;
	static final String Wt_js;
	static final String Boot_js;
	static final String Hybrid_html;
	static final String JQuery_js;
	static final String WtMessages_xml = "eu.webtoolkit.jwt.wt";

	private static ServletApi servletApi;

	static {
		Boot_html = readFile("/eu/webtoolkit/jwt/skeletons/Boot.html");
		Plain_html = readFile("/eu/webtoolkit/jwt/skeletons/Plain.html");
		Hybrid_html = readFile("/eu/webtoolkit/jwt/skeletons/Hybrid.html");
		Wt_js = readFile("/eu/webtoolkit/jwt/skeletons/Wt.min.js");
		Boot_js = readFile("/eu/webtoolkit/jwt/skeletons/Boot.min.js");
		JQuery_js = readFile("/eu/webtoolkit/jwt/skeletons/jquery.min.js");
		
		servletApi = null;
	}

	private InputStream getResourceStream(final String fileName) throws FileNotFoundException {
		return this.getClass().getResourceAsStream("/eu/webtoolkit/jwt/" + fileName);
	}

	private static String readFile(final String fileName) {
		return JarUtils.getInstance().readTextFromJar(fileName);
	}
	
	/**
	 * This function is only to be used by JWt internals.
	 * 
	 * @return the servlet API interface
	 */
	public static ServletApi getServletApi() {
		return servletApi;
	}

	/**
	 * Constructor.
	 * <p>
	 * Instantiates the servlet using the default configuration.
	 * 
	 * @see #getConfiguration()
	 */
	public WtServlet() {
		this.progressListener = new ProgressListener() {
			public void update(WebRequest request, long pBytesRead, long pContentLength) {
				requestDataReceived(request, pBytesRead, pContentLength);
			}
		};
		
		this.configuration = new Configuration();
	}
	
	/**
	 * Initiate the internal servlet api.
	 * 
	 * If you want to override this function, make sure to call the super function,
	 * to ensure the initialization of the servlet api.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		String configFile = this.getInitParameter("jwt-config-file");
		if (configFile != null)
			this.configuration = new Configuration(new File(configFile));
		
		initServletApi(config.getServletContext());
	}

	private void initServletApi(ServletContext context) {
		if (servletApi == null) {
			try {
				if (context.getMajorVersion() == 3) {
					System.err.println("Using servlet API 3");
					servletApi = (ServletApi)Class.forName("eu.webtoolkit.jwt.ServletApi3").newInstance();
				} else {
					System.err.println("Using servlet API 2.5");
					servletApi = (ServletApi)Class.forName("eu.webtoolkit.jwt.ServletApi25").newInstance();
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	void handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
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

		servletApi.doHandleRequest(this, request, response);
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
	 * The Configuration is only definitively constructed after WtServlet#init() is invoked.
	 * You should only modify the configuration from this method.
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

	/*
	 * Actual request handling, may be within an async call depending on the servlet API.
	 */
	void doHandleRequest(HttpServletRequest request, HttpServletResponse response) {		
		HttpSession jsession = request.getSession();
		WebSession wsession = (WebSession) jsession.getAttribute(WtServlet.WT_WEBSESSION_ID);
		getConfiguration().setSessionTimeout(jsession.getMaxInactiveInterval());
	
		if (wsession == null) {
			String applicationTypeS = getServletConfig().getInitParameter("ApplicationType");
			
			EntryPointType applicationType;
			if (applicationTypeS == null || applicationTypeS.equals("") || applicationTypeS.equals("Application")) {
				applicationType = EntryPointType.Application; 
			} else if (applicationTypeS.equals("WidgetSet")) {
				applicationType = EntryPointType.WidgetSet; 
			} else {
				throw new WtException("Illegal application type: " + applicationTypeS);
			}
			
			wsession = new WebSession(this, jsession.getId(), applicationType, getConfiguration().getFavicon(), new WebRequest(request, progressListener));
			jsession.setAttribute(WtServlet.WT_WEBSESSION_ID, wsession);
		}
	
		try {
			WebRequest webRequest = new WebRequest(request, progressListener);
			WebResponse webResponse = new WebResponse(response, webRequest);

			WebSession.Handler handler = new WebSession.Handler(wsession, webRequest, webResponse);
			wsession.handleRequest(handler);
			handler.release();
			if (handler.getSession().isDead()) {
				System.err.println("Session exiting:" + jsession.getId());
				jsession.setAttribute(WtServlet.WT_WEBSESSION_ID, null);
				jsession.invalidate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void addUploadProgressUrl(String url) {
		synchronized (uploadProgressUrls_) {
			uploadProgressUrls_.add(url.substring(url.indexOf('?') + 1));
		}
	}

	void removeUploadProgressUrl(String url) {
		synchronized (uploadProgressUrls_) {
			uploadProgressUrls_.remove(url.substring(url.indexOf('?') + 1));
		}
	}
	
	boolean requestDataReceived(WebRequest request, long current, long total) {
		boolean found = false;

		synchronized (uploadProgressUrls_) {
			for (String url : uploadProgressUrls_) {
				if (url.equals(request.getQueryString())) {
					found = true;
					break;
				}
			}
		}

		if (found) {
			HttpSession jsession = request.getSession();
			WebSession session = (WebSession) jsession.getAttribute(WtServlet.WT_WEBSESSION_ID);

			if (session != null) {
				WebSession.Handler handler = new WebSession.Handler(session,request, null);

				if (!session.isDead() && session.getApp() != null) {
					String requestE = request.getParameter("request");

					WResource resource = null;
					if (requestE == null && request.getPathInfo().length() != 0)
						resource = session.getApp().
							decodeExposedResource("/path/" + request.getPathInfo());

					if (resource == null) {
						String resourceE = request.getParameter("resource");
						resource = session.getApp().
							decodeExposedResource(resourceE);
					}

					if (resource != null) {
						// FIXME, we should do this within app()->notify()
						session.getApp().modifiedWithoutEvent_ = true;
						resource.dataReceived().trigger(current, total);
						session.getApp().modifiedWithoutEvent_ = false;
					}
				}
				
				handler.release();
			}
		}

		return true;
	}

	private Configuration configuration;
	private ProgressListener progressListener;
	private Set<String> uploadProgressUrls_ = new HashSet<String>();
	
	/**
	 * Returns whether asynchronous processing is supported,
	 * which is only the case when the servlet container implements the Servlet 3.0 API,
	 * and when this application is configured to support asynchronous processing.
	 * Asynchronous processing is required for both server push and the recursive event loop.
	 * 
	 * @return whether asynchronous processing is supported
	 */
	public static boolean isAsyncSupported() {
		WebSession.Handler handler = WebSession.Handler.getInstance();
		return servletApi.isAsyncSupported(handler != null ? handler.getRequest() : null);
	}
}