package eu.webtoolkit.jwt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

class ServletApi3 extends ServletApi{
	private static Logger logger = LoggerFactory.getLogger(ServletApi3.class);

	@Override
	public void init(ServletContext context, boolean contextIsInitializing) {
		if (contextIsInitializing) {
			Set<SessionTrackingMode> modes = new HashSet<SessionTrackingMode>();
			modes.add(SessionTrackingMode.URL);
			context.setSessionTrackingModes(modes);
			logger.info("Configured URL tracking");
		} else {
			Set<SessionTrackingMode> modes = context.getDefaultSessionTrackingModes();
			boolean urlTracking = false;
			for (SessionTrackingMode m : modes) {
				if (m == SessionTrackingMode.COOKIE) {
					urlTracking = false;
					break;
				} else if (m == SessionTrackingMode.URL)
					urlTracking = true;
			}
			
			if (urlTracking)
				logger.info("Detected URL tracking: excellent!");
			else
				logger.error("Detected cookies configured for session tracking, this will not work well:\n" +
					" - either configure tracking directly in your web.xml:\n" +
					"\n" +
					"  <session-config>\n" +
					"    <tracking-mode>URL</tracking-mode>\n" +
					"  </session-config>\n" +
					"\n" +
					"  (but this is not supported by e.g. Jetty8)\n" +
					"\n" +
					" - OR, configure eu.webtoolkit.jwt.ServletInit as a listener in your web.xml:\n" +
					"\n" +
					"  <listener>\n" +
					"    <listener-class>eu.webtoolkit.jwt.ServletInit</listener-class>\n" +
					"  </listener>\n");
		}
	}
	
	@Override
	public void completeAsyncContext(HttpServletRequest request) {
		if (request.isAsyncStarted()) {
			try {
				request.getAsyncContext().complete();
			} catch (IllegalStateException e) {
				logger.error("IllegalStateException occurred when completing async context: {}", e.getMessage(), e);
			}
		}
	}

	@Override
	public void doHandleRequest(final WtServlet servlet, final WebRequest request, final WebResponse response) {
		if (request.isAsyncSupported()) {
			request.startAsync();
			final long asyncContextTimeout = WtServlet.getInstance().getConfiguration().getAsyncContextTimeout();
			request.getAsyncContext().setTimeout(asyncContextTimeout);
			request.getAsyncContext().start(new Runnable() {
				@Override
				public void run() {
					handleRequest(servlet, request, response);
				}
			});
			request.getAsyncContext().addListener(new AsyncListener() {
				@Override
				public void onTimeout(AsyncEvent e) throws IOException {
					logger.error("Timeout: waiting more then " + asyncContextTimeout, e.getThrowable());
				}
				
				@Override
				public void onStartAsync(AsyncEvent arg0) throws IOException {					
				}
				
				@Override
				public void onError(AsyncEvent e) throws IOException {
					logger.error("Error during async request ", e.getThrowable());
				}
				
				@Override
				public void onComplete(AsyncEvent arg0) throws IOException {					
				}
			});
		} else
			handleRequest(servlet, request, response);
	}

	@Override
	public HttpServletRequest getMockupHttpServletRequest() {
		return new HttpServletRequest() {

			@Override
			public boolean authenticate(HttpServletResponse arg0)
					throws IOException, ServletException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String getAuthType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getContextPath() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Cookie[] getCookies() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getDateHeader(String arg0) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getHeader(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Enumeration<String> getHeaderNames() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Enumeration<String> getHeaders(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getIntHeader(String arg0) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getMethod() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Part getPart(String arg0) throws IOException,
					ServletException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<Part> getParts() throws IOException,
					ServletException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getPathInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getPathTranslated() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getQueryString() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getRemoteUser() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getRequestURI() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public StringBuffer getRequestURL() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getRequestedSessionId() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getServletPath() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public HttpSession getSession() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public HttpSession getSession(boolean arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Principal getUserPrincipal() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isRequestedSessionIdFromCookie() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isRequestedSessionIdFromURL() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isRequestedSessionIdFromUrl() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isRequestedSessionIdValid() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isUserInRole(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void login(String arg0, String arg1) throws ServletException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void logout() throws ServletException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public AsyncContext getAsyncContext() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getAttribute(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Enumeration<String> getAttributeNames() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getCharacterEncoding() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getContentLength() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getContentType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public DispatcherType getDispatcherType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ServletInputStream getInputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getLocalAddr() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getLocalName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getLocalPort() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public Locale getLocale() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Enumeration<Locale> getLocales() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getParameter(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Map<String, String[]> getParameterMap() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Enumeration<String> getParameterNames() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String[] getParameterValues(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getProtocol() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public BufferedReader getReader() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getRealPath(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getRemoteAddr() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getRemoteHost() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getRemotePort() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public RequestDispatcher getRequestDispatcher(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getScheme() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getServerName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getServerPort() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public ServletContext getServletContext() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isAsyncStarted() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isAsyncSupported() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isSecure() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void removeAttribute(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setAttribute(String arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setCharacterEncoding(String arg0)
					throws UnsupportedEncodingException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public AsyncContext startAsync() throws IllegalStateException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public AsyncContext startAsync(ServletRequest arg0,
					ServletResponse arg1) throws IllegalStateException {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	public HttpServletResponse getMockupHttpServletResponse() {
		return new HttpServletResponse(){

			@Override
			public void addCookie(Cookie arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void addDateHeader(String arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void addHeader(String arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void addIntHeader(String arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean containsHeader(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public String encodeRedirectURL(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String encodeRedirectUrl(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String encodeURL(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String encodeUrl(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getHeader(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<String> getHeaderNames() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<String> getHeaders(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getStatus() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void sendError(int arg0) throws IOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void sendError(int arg0, String arg1) throws IOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void sendRedirect(String arg0) throws IOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDateHeader(String arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setHeader(String arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setIntHeader(String arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setStatus(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setStatus(int arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void flushBuffer() throws IOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public int getBufferSize() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getCharacterEncoding() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getContentType() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Locale getLocale() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ServletOutputStream getOutputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public PrintWriter getWriter() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean isCommitted() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void reset() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void resetBuffer() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setBufferSize(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setCharacterEncoding(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setContentLength(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setContentType(String arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setLocale(Locale arg0) {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

	@Override
	public boolean isAsyncSupported(HttpServletRequest request) {
          if (request != null)
            return request.isAsyncSupported();
          else
            return true;
	}
}
