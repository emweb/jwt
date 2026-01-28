package eu.webtoolkit.jwt;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.*;

public class ServletApi6 extends ServletApi {
	private static Logger logger = LoggerFactory.getLogger(ServletApi6.class);

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	public void configureRequestEncoding(ServletContext context, boolean contextIsInitializing) {
		if (contextIsInitializing) {
			context.setRequestCharacterEncoding("UTF-8");
			logger.info("Set default request character encoding to UTF-8");
		} else {
			if (Objects.equals(context.getRequestCharacterEncoding(), "UTF-8")) {
				logger.info("Request character encoding is UTF-8: excellent!");
			} else {
				logger.error("Request character encoding is not set to UTF-8 (is {}), this will not work well:\n" +
						" - either configure request character encoding directly in your web.xml:\n" +
						"\n" +
						"  <request-character-encoding>UTF-8</request-character-encoding>\n" +
						"\n" +
						" - OR, configure eu.webtoolkit.jwt.ServletInit as a listener in your web.xml:\n" +
						"\n" +
						"  <listener>\n" +
						"    <listener-class>eu.webtoolkit.jwt.ServletInit</listener-class>\n" +
						"  </listener>\n", context.getRequestCharacterEncoding());
			}
		}
	}

	@Override
	public HttpServletRequest getMockupHttpServletRequest() {
		return new HttpServletRequest() {
			@Override public boolean authenticate(HttpServletResponse arg0) { return false; }
			@Override public String getAuthType() { return null; }
			@Override public String getContextPath() { return null; }
			@Override public Cookie[] getCookies() { return null; }
			@Override public long getDateHeader(String arg0) { return 0; }
			@Override public String getHeader(String arg0) { return null; }
			@Override public Enumeration<String> getHeaderNames() { return null; }
			@Override public Enumeration<String> getHeaders(String arg0) { return null; }
			@Override public int getIntHeader(String arg0) { return 0; }
			@Override public String getMethod() { return null; }
			@Override public Part getPart(String arg0) { return null; }
			@Override public Collection<Part> getParts() { return null; }
			@Override public String getPathInfo() { return null; }
			@Override public String getPathTranslated() { return null; }
			@Override public String getQueryString() { return null; }
			@Override public String getRemoteUser() { return null; }
			@Override public String getRequestURI() { return null; }
			@Override public StringBuffer getRequestURL() { return null; }
			@Override public String getRequestedSessionId() { return null; }
			@Override public String getServletPath() { return null; }
			@Override public HttpSession getSession() { return null; }
			@Override public HttpSession getSession(boolean arg0) { return null; }
			@Override public Principal getUserPrincipal() { return null; }
			@Override public boolean isRequestedSessionIdFromCookie() { return false; }
			@Override public boolean isRequestedSessionIdFromURL() { return false; }
			@Override public boolean isRequestedSessionIdValid() { return false; }
			@Override public boolean isUserInRole(String arg0) { return false; }
			@Override public void login(String arg0, String arg1) {}
			@Override public void logout() {}
			@Override public AsyncContext getAsyncContext() { return null; }
			@Override public Object getAttribute(String arg0) { return null; }
			@Override public Enumeration<String> getAttributeNames() { return null; }
			@Override public String getCharacterEncoding() { return null; }
			@Override public int getContentLength() { return 0; }
			@Override public String getContentType() { return null; }
			@Override public DispatcherType getDispatcherType() { return null; }
			@Override public String getRequestId() { return null; }
			@Override public String getProtocolRequestId() { return null; }
			@Override public ServletConnection getServletConnection() { return null; }
			@Override public ServletInputStream getInputStream() { return null; }
			@Override public String getLocalAddr() { return null; }
			@Override public String getLocalName() { return null; }
			@Override public int getLocalPort() { return 0; }
			@Override public Locale getLocale() { return null; }
			@Override public Enumeration<Locale> getLocales() { return null; }
			@Override public String getParameter(String arg0) { return null; }
			@Override public Map<String, String[]> getParameterMap() { return null; }
			@Override public Enumeration<String> getParameterNames() { return null; }
			@Override public String[] getParameterValues(String arg0) { return null; }
			@Override public String getProtocol() { return null; }
			@Override public BufferedReader getReader() { return null; }
			@Override public String getRemoteAddr() { return null; }
			@Override public String getRemoteHost() { return null; }
			@Override public int getRemotePort() { return 0; }
			@Override public RequestDispatcher getRequestDispatcher(String arg0) { return null; }
			@Override public String getScheme() { return null; }
			@Override public String getServerName() { return null; }
			@Override public int getServerPort() { return 0; }
			@Override public ServletContext getServletContext() { return null; }
			@Override public boolean isAsyncStarted() { return false; }
			@Override public boolean isAsyncSupported() { return false; }
			@Override public boolean isSecure() { return false; }
			@Override public void removeAttribute(String arg0) {}
			@Override public void setAttribute(String arg0, Object arg1) {}
			@Override public void setCharacterEncoding(String arg0) {}
			@Override public AsyncContext startAsync() throws IllegalStateException { return null; }
			@Override public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1)
					throws IllegalStateException { return null; }
			@Override public long getContentLengthLong() { return 0; }
			@Override public String changeSessionId() { return null; }
			@Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> arg0) { return null; }
		};
	}

	@Override
	public HttpServletResponse getMockupHttpServletResponse() {
		return new HttpServletResponse() {
			@Override public void addCookie(Cookie arg0) {}
			@Override public void addDateHeader(String arg0, long arg1) {}
			@Override public void addHeader(String arg0, String arg1) {}
			@Override public void addIntHeader(String arg0, int arg1) {}
			@Override public boolean containsHeader(String arg0) { return false; }
			@Override public String encodeRedirectURL(String arg0) { return null; }
			@Override public String encodeURL(String arg0) { return null; }
			@Override public String getHeader(String arg0) { return null; }
			@Override public Collection<String> getHeaderNames() { return null; }
			@Override public Collection<String> getHeaders(String arg0) { return null; }
			@Override public int getStatus() { return 0; }
			@Override public void sendError(int arg0) {}
			@Override public void sendError(int arg0, String arg1) {}
			@Override public void sendRedirect(String arg0) {}
			@Override public void sendRedirect(String s, int i, boolean b) {}
			@Override public void setDateHeader(String arg0, long arg1) {}
			@Override public void setHeader(String arg0, String arg1) {}
			@Override public void setIntHeader(String arg0, int arg1) {}
			@Override public void setStatus(int arg0) {}
			@Override public void flushBuffer() {}
			@Override public int getBufferSize() { return 0; }
			@Override public String getCharacterEncoding() { return null; }
			@Override public String getContentType() { return null; }
			@Override public Locale getLocale() { return null; }
			@Override public ServletOutputStream getOutputStream() { return null; }
			@Override public PrintWriter getWriter() { return null; }
			@Override public boolean isCommitted() { return false; }
			@Override public void reset() {}
			@Override public void resetBuffer() {}
			@Override public void setBufferSize(int arg0) {}
			@Override public void setCharacterEncoding(String arg0) {}
			@Override public void setContentLength(int arg0) {}
			@Override public void setContentType(String arg0) {}
			@Override public void setLocale(Locale arg0) {}
			@Override public void setContentLengthLong(long arg0) {}
		};
	}

}
