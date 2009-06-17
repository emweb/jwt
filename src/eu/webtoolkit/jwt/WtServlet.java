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

public abstract class WtServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String WT_WEBSESSION_ID = "wt-websession";

	final static String Boot_html;
	final static String Plain_html;
	final static String JsNoAjax_html;
	final static String Wt_js;
	final static String CommAjax_js;
	final static String CommScript_js;

	private String resourcePath;

	static {
		Boot_html = readFile("/eu/webtoolkit/jwt/skeletons/Boot.html");
		Plain_html = readFile("/eu/webtoolkit/jwt/skeletons/Plain.html");
		JsNoAjax_html = readFile("/eu/webtoolkit/jwt/skeletons/JsNoAjax.html");
		Wt_js = readFile("/eu/webtoolkit/jwt/skeletons/Wt.js");
		CommAjax_js = readFile("/eu/webtoolkit/jwt/skeletons/CommAjax.js");
		CommScript_js = readFile("/eu/webtoolkit/jwt/skeletons/CommScript.js");
	}

	private InputStream getResourceStream(final String fileName) throws FileNotFoundException {
		return this.getClass().getResourceAsStream("/eu/webtoolkit/jwt/" + fileName);
	}

	private static String readFile(final String fileName) {
		return JarUtils.getInstance().readTextFromJar(fileName);
	}

	public WtServlet() {
		this(new Configuration());
	}
	
	public WtServlet(Configuration configuration) {
		this.configuration = configuration;
		this.resourcePath = configuration.getProperties().get("ResourcesURL");
		if (resourcePath == null)
			resourcePath = "/wt-resources/";
	}

	void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		if (request.getPathInfo() != null && request.getPathInfo().startsWith(resourcePath)) {
			String fileName = "wt-resources/" + request.getPathInfo().substring(resourcePath.length());
			try {
				InputStream s = getResourceStream(fileName);
				StreamUtils.copy(s, response.getOutputStream());
				response.getOutputStream().flush();
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

		if (wsession == null) {
			String applicationTypeS = this.getServletConfig().getInitParameter("ApplicationType");
			
			WebSession.Type applicationType;
			if (applicationTypeS == null || applicationTypeS.equals("") || applicationTypeS.equals("Application")) {
				applicationType = WebSession.Type.Application; 
			} else if (applicationTypeS.equals("WidgetSet")) {
				applicationType = WebSession.Type.WidgetSet; 
			} else {
				throw new WtException("Illegal application type: " + applicationTypeS);
			}
			
			wsession = new WebSession(this, jsession.getId(), applicationType, this.configuration.getFavicon(), new WebRequest(request));
			jsession.setAttribute(WT_WEBSESSION_ID, wsession);
		}

		try {
			if (!wsession.handleRequest(new WebRequest(request), new WebResponse(response, request))) {
				System.err.println("Session exiting:" + jsession.getId());
				wsession.destroy();
				jsession.setAttribute(WT_WEBSESSION_ID, null);
				jsession.invalidate();
			}
			System.gc();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	WApplication doCreateApplication(WebSession session) {
		return createApplication(session.getEnv());
	}

	public abstract WApplication createApplication(WEnvironment env);

	private Configuration configuration;
}