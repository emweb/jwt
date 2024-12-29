package eu.webtoolkit.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import eu.webtoolkit.jwt.servlet.UploadedFile;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.servlet.WebResponse;

/**
 * An incoming web socket message
 * @author raf
 */
class WebSocketMessage extends WebRequest {
	private String query;
	private WebSocketConnection socketConnection;

	/**
	 * Parse the web socket message, adding some extra parameters
	 * @param query the message contents, constructed like a URL query
	 * @param socketConnection the associated connection
	 * @throws IOException when parameters could not be passed to bytes
	 */
	public WebSocketMessage(String query, WebSocketConnection socketConnection) throws IOException {
		super(new HashMap<String, String[]>(), new HashMap<String, List<UploadedFile>>());
		this.query = "wtd=" + socketConnection.getParameter("wtd") + "&request=jsupdate" + query;
		this.socketConnection = socketConnection;
	    readParameters(this.query.getBytes("UTF-8"));
	}
	
	@Override
	public boolean isWebSocketMessage() {
		return true;
	}
	
	/**
	 * A web socket request is used for an upgrade on the HTTP session. 
	 * This is handled by the JSR-356 implementation and this is always false here.
	 * @see eu.webtoolkit.jwt.servlet.WebRequest#isWebSocketRequest()
	 */
	@Override
	public boolean isWebSocketRequest() {
		return false;
	}
	
	@Override
	public int getContentLength() {
		return this.query.length();
	}
	
	/**
	 * The request method is always "POST" for a web socket connection
	 * @see eu.webtoolkit.jwt.servlet.WebResponse#getRequestMethod()
	 */
	@Override
	public String getRequestMethod() {
		return "POST";
	}
	
	@Override
	public String getHeader(String name) {
		return this.socketConnection.getRequestHeader(name);
	}
	
	@Override
	public String getHeaderValue(String header) {
		return getHeader(header);
	}
	
	@Override
	public String getPathInfo() {
		return this.socketConnection.getPathInfo();
	}
	
	/**
	 * The content type is always "application/x-www-form-urlencoded" for a web socket connection
	 * @see javax.servlet.ServletResponseWrapper#getContentType()
	 */
	@Override
	public String getContentType() {
		return "application/x-www-form-urlencoded";
	}

	@Override
	public String getQueryString() {
		return query;
	}
	@Override
	public String getScheme() {
		return "http";
	}
}
