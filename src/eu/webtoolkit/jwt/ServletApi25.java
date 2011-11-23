package eu.webtoolkit.jwt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

class ServletApi25 extends ServletApi {
	public void completeAsyncContext(HttpServletRequest request) {
	}

	public void doHandleRequest(WtServlet servlet, WebRequest request, WebResponse response) {
		handleRequest(servlet, request, response);
	}

	public HttpServletRequest getMockupHttpServletRequest() {
		return new HttpServletRequest() {

			
			public String getAuthType() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getContextPath() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Cookie[] getCookies() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public long getDateHeader(String arg0) {
				// TODO Auto-generated method stub
				return 0;
			}

			
			public String getHeader(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Enumeration getHeaderNames() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Enumeration getHeaders(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public int getIntHeader(String arg0) {
				// TODO Auto-generated method stub
				return 0;
			}

			
			public String getMethod() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getPathInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getPathTranslated() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getQueryString() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getRemoteUser() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getRequestURI() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public StringBuffer getRequestURL() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getRequestedSessionId() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getServletPath() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public HttpSession getSession() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public HttpSession getSession(boolean arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Principal getUserPrincipal() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public boolean isRequestedSessionIdFromCookie() {
				// TODO Auto-generated method stub
				return false;
			}

			
			public boolean isRequestedSessionIdFromURL() {
				// TODO Auto-generated method stub
				return false;
			}

			
			public boolean isRequestedSessionIdFromUrl() {
				// TODO Auto-generated method stub
				return false;
			}

			
			public boolean isRequestedSessionIdValid() {
				// TODO Auto-generated method stub
				return false;
			}

			
			public boolean isUserInRole(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			
			public Object getAttribute(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Enumeration getAttributeNames() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getCharacterEncoding() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public int getContentLength() {
				// TODO Auto-generated method stub
				return 0;
			}

			
			public String getContentType() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public ServletInputStream getInputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getLocalAddr() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getLocalName() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public int getLocalPort() {
				// TODO Auto-generated method stub
				return 0;
			}

			
			public Locale getLocale() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Enumeration getLocales() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getParameter(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Map getParameterMap() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Enumeration getParameterNames() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String[] getParameterValues(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getProtocol() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public BufferedReader getReader() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getRealPath(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getRemoteAddr() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getRemoteHost() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public int getRemotePort() {
				// TODO Auto-generated method stub
				return 0;
			}

			
			public RequestDispatcher getRequestDispatcher(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getScheme() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getServerName() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public int getServerPort() {
				// TODO Auto-generated method stub
				return 0;
			}

			
			public boolean isSecure() {
				// TODO Auto-generated method stub
				return false;
			}

			
			public void removeAttribute(String arg0) {
				// TODO Auto-generated method stub
				
			}

			
			public void setAttribute(String arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}

			
			public void setCharacterEncoding(String arg0)
					throws UnsupportedEncodingException {
				// TODO Auto-generated method stub
				
			}
		};
	}

	public HttpServletResponse getMockupHttpServletResponse() {
		return new HttpServletResponse() {

			
			public void addCookie(Cookie arg0) {
				// TODO Auto-generated method stub
				
			}

			
			public void addDateHeader(String arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}

			
			public void addHeader(String arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}

			
			public void addIntHeader(String arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			
			public boolean containsHeader(String arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			
			public String encodeRedirectURL(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String encodeRedirectUrl(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String encodeURL(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String encodeUrl(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			
			public void sendError(int arg0) throws IOException {
				// TODO Auto-generated method stub
				
			}

			
			public void sendError(int arg0, String arg1) throws IOException {
				// TODO Auto-generated method stub
				
			}

			
			public void sendRedirect(String arg0) throws IOException {
				// TODO Auto-generated method stub
				
			}

			
			public void setDateHeader(String arg0, long arg1) {
				// TODO Auto-generated method stub
				
			}

			
			public void setHeader(String arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}

			
			public void setIntHeader(String arg0, int arg1) {
				// TODO Auto-generated method stub
				
			}

			
			public void setStatus(int arg0) {
				// TODO Auto-generated method stub
				
			}

			
			public void setStatus(int arg0, String arg1) {
				// TODO Auto-generated method stub
				
			}

			
			public void flushBuffer() throws IOException {
				// TODO Auto-generated method stub
				
			}

			
			public int getBufferSize() {
				// TODO Auto-generated method stub
				return 0;
			}

			
			public String getCharacterEncoding() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public String getContentType() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public Locale getLocale() {
				// TODO Auto-generated method stub
				return null;
			}

			
			public ServletOutputStream getOutputStream() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			
			public PrintWriter getWriter() throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			
			public boolean isCommitted() {
				// TODO Auto-generated method stub
				return false;
			}

			
			public void reset() {
				// TODO Auto-generated method stub
				
			}

			
			public void resetBuffer() {
				// TODO Auto-generated method stub
				
			}

			
			public void setBufferSize(int arg0) {
				// TODO Auto-generated method stub
				
			}

			
			public void setCharacterEncoding(String arg0) {
				// TODO Auto-generated method stub
				
			}

			
			public void setContentLength(int arg0) {
				// TODO Auto-generated method stub
				
			}

			
			public void setContentType(String arg0) {
				// TODO Auto-generated method stub
				
			}

			
			public void setLocale(Locale arg0) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	public boolean isAsyncSupported(HttpServletRequest request) {
		return false;
	}

	@Override
	public void init(ServletContext context, boolean contextIsInitializing) {
	}
}
