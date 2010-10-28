package eu.webtoolkit.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Implementation specific servlet interface,
 * this class is only to be used by JWt internals.
 * 
 * @author pieter
 */
public abstract class ServletApi {
	public abstract HttpServletRequest getMockupHttpServletRequest();
	public abstract HttpServletResponse getMockupHttpServletResponse();
	
	public abstract void completeAsyncContext(HttpServletRequest request);
	
	public abstract void doHandleRequest(WtServlet servlet, HttpServletRequest request, HttpServletResponse response);

	public abstract boolean isAsyncSupported();
	
	protected void handleRequest(WtServlet servlet, HttpServletRequest request, HttpServletResponse response) {
		servlet.doHandleRequest(request, response);
	}
}
