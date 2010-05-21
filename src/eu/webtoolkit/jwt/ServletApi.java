package eu.webtoolkit.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

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
	
	public abstract void startAsync(HttpServletRequest request);
	
	public abstract void doHandleRequest(WtServlet servlet, HttpServletRequest request, HttpServletResponse response);
	
	protected void handleRequest(WtServlet servlet, HttpServletRequest request, HttpServletResponse response) {
		HttpSession jsession = request.getSession();
		WebSession wsession = (WebSession) jsession.getAttribute(WtServlet.WT_WEBSESSION_ID);
		servlet.getConfiguration().setSessionTimeout(jsession.getMaxInactiveInterval());

		if (wsession == null) {
			String applicationTypeS = servlet.getServletConfig().getInitParameter("ApplicationType");
			
			EntryPointType applicationType;
			if (applicationTypeS == null || applicationTypeS.equals("") || applicationTypeS.equals("Application")) {
				applicationType = EntryPointType.Application; 
			} else if (applicationTypeS.equals("WidgetSet")) {
				applicationType = EntryPointType.WidgetSet; 
			} else {
				throw new WtException("Illegal application type: " + applicationTypeS);
			}
			
			wsession = new WebSession(servlet, jsession.getId(), applicationType, servlet.getConfiguration().getFavicon(), new WebRequest(request));
			jsession.setAttribute(WtServlet.WT_WEBSESSION_ID, wsession);
		}

		try {
			WebRequest webRequest = new WebRequest(request);
			WebResponse webResponse = new WebResponse(response, webRequest);
			if (!wsession.handleRequest(webRequest, webResponse)) {
				System.err.println("Session exiting:" + jsession.getId());
				jsession.setAttribute(WtServlet.WT_WEBSESSION_ID, null);
				jsession.invalidate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
