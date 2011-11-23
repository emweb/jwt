package eu.webtoolkit.jwt;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;


/**
 * Implementation specific servlet interface,
 * this class is only to be used by JWt internals.
 * 
 * @author pieter
 */
public abstract class ServletApi {
	public abstract void init(ServletContext context, boolean contextIsInitializing);
	public abstract boolean isAsyncSupported(HttpServletRequest request);
	public abstract void completeAsyncContext(HttpServletRequest request);
	public abstract void doHandleRequest(WtServlet servlet, WebRequest request, WebResponse response);
	
	public abstract HttpServletRequest getMockupHttpServletRequest();
	public abstract HttpServletResponse getMockupHttpServletResponse();

	protected void handleRequest(WtServlet servlet, WebRequest request, WebResponse response) {
		servlet.doHandleRequest(request, response);
	}	
}
