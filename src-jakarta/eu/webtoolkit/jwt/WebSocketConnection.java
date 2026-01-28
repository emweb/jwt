/**
 * 
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.servlet.WebResponse;

/**
 * A connection for a web socket, to generate responses
 * @author raf
 */
class WebSocketConnection extends WebResponse {
	private static final Logger logger = LoggerFactory.getLogger(WebSocketConnection.class);
	
	private Writer outWriter;
	private Session socketSession;
	private Map<String, List<String>> headers;
	private WebSession webSession;
	
	public WebSocketConnection(final Session socketSession, Map<String, List<String>> headers) throws IOException {
		super();
		this.socketSession = socketSession;
		this.headers = headers;
		this.socketSession.getUserProperties().put(WebSocketConnection.class.toString(), this);
	}

	public static WebSocketConnection getWebSocketConnection(Session socketSession) {
		return (WebSocketConnection) socketSession.getUserProperties().get(WebSocketConnection.class.toString());
	}
	
	@Override
	public String getParameter(String string) {
		Map<String, List<String>> requestParameters = socketSession.getRequestParameterMap();
		return requestParameters.get(string).get(0);
	}
	
	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, List<String>> requestParameters = this.socketSession.getRequestParameterMap();
		Map<String, String[]> requestParameterArrays = new HashMap<String, String[]>();
		for (Entry<String, List<String>> parameter : requestParameters.entrySet()) {
			List<String> value = parameter.getValue();
			String[] arrayValue = null;
			if (value != null) {
				arrayValue = (String[]) value.toArray(new String[value.size()]);
			}
			requestParameterArrays.put(parameter.getKey(), arrayValue);
		}

		return requestParameterArrays;
	}
	
	/**
	 * This is called from WebSession when we need to render.
	 * @see eu.webtoolkit.jwt.servlet.WebResponse#flush()
	 */
	@Override
	public void flush() {
		webSession.pushUpdates();
	}
	
	/**
	 * Flush the buffer of the output stream, close it and forget about it
	 * @see jakarta.servlet.ServletResponseWrapper#flushBuffer()
	 */
	@Override
	public void flushBuffer() {
		if (this.outWriter != null) {
			try {
				this.outWriter.close();
				this.outWriter = null;
			} catch (IOException e) {
				logger.info("IOException in flush", e);
			}
		}
	}
	
	@Override
	public boolean isWebSocketMessage() {
		return true;
	}

	public String getRequestHeader(String name) {
		List<String> header = this.headers.get(name);
		if (header != null) {
			return header.get(0);
		}
		return null;
	}
	
	@Override
	public Writer out() {
		if (this.outWriter == null) {
			try {
				this.outWriter = this.socketSession.getBasicRemote().getSendWriter();
			} catch (IOException e) {
				logger.info("IOException retrieving out writer", e);
			}
		}
		return this.outWriter;
	}
	
	@Override
	public String getPathInfo() {
		String pathInfo = "";
		for (Entry<String, String> entry : this.socketSession.getPathParameters().entrySet()) {
			pathInfo += entry.getKey() + "=" + entry.getValue();
		};
		return pathInfo;
	}
	
	/**
	 * The content type can only be "text/javascript; charset=UTF-8" for a web socket connection
	 * @see jakarta.servlet.ServletResponseWrapper#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType(String type) {
		if (!type.equals("text/javascript; charset=UTF-8")) {
			try {
				throw new Exception("setContentType(): text/javascript expected");
			} catch (Exception e) {
				logger.error("input error: type is {}, expected text/javascript", type, e);
			}
		}
		super.setContentType(type);
	}
	
	@Override
	public void addHeader(String name, String value) {
		if (name.equals("Set-Cookie")) {
			try {
				out().write("document.cookie=" + WWebWidget.jsStringLiteral(value) + ";");
			} catch (IOException e) {
				logger.error("IOException adding header {} = {}", name, value);
			}
		}
	}
	
	/**
	 * The content type is always "application/x-www-form-urlencoded" for a web socket connection
	 * @see jakarta.servlet.ServletResponseWrapper#getContentType()
	 */
	@Override
	public String getContentType() {
		return "application/x-www-form-urlencoded";
	}
	
	/**
	 * The request method is always "POST" for a web socket connection
	 * @see eu.webtoolkit.jwt.servlet.WebResponse#getRequestMethod()
	 */
	@Override
	public String getRequestMethod() {
		return "POST";
	}
	
	/**
	 * Set the web session in order to push updates on flush
	 * @param webSession the associated web session
	 */
	public void setWebSession(WebSession webSession) {
		this.webSession = webSession;
	}
}
