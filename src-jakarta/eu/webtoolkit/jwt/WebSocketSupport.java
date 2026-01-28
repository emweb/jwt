package eu.webtoolkit.jwt;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.websocket.CloseReason;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.webtoolkit.jwt.WebSession.Handler;

/**
 * Handle web socket events for all running servlets
 * @author raf
 */
@ServerEndpoint(value = "/ws", configurator=JWtEndpointConfig.class)
public class WebSocketSupport {
	private static Logger logger = LoggerFactory.getLogger(WebSocketSupport.class);
	
	/**
	 * Get the web session via connection parameters
	 * @param connection the web socket connection to query for parameters
	 * @return an associated WebSession reference
	 */
	private WebSession getWebSession(WebSocketConnection connection) {
		String servletId = connection.getParameter("wsid");
		if (servletId == null){
			logger.debug("WebSocket message discarded: no wsid parameter present");
			return null;
		}
		WtServlet servlet = WebSocketRegistry.getInstance().getServlet(Integer.valueOf(servletId));
		if (servlet == null) {
			logger.debug("WebSocket message discarded: invalid wsid parameter");
			return null;
		}
		String wtdParameter = connection.getParameter("wtd");
		if (wtdParameter == null) {
			logger.debug("WebSocket message discarded: no wtd parameter present");
			return null;
		}
		return servlet.getSession(wtdParameter);
	}
	
	/**
	 * Is called when a new web socket connection is opened
	 * @param session holds all state for this web socket session
	 * @param config holds the headers used to set up this socket session
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, List<String>> headers = (Map<String, List<String>>) config.getUserProperties().get("Headers");
			WebSocketConnection connection = new WebSocketConnection(session, headers);
			WebSession webSession = getWebSession(connection); 
			if (webSession == null) {
				logger.info("WebSocket connection discarded: could not retrieve web session");
				return;
			}
			connection.setWebSession(webSession);
			webSession.webSocket_ = connection;
			session.getBasicRemote().sendText("connect");
		} catch (IOException e) {
			logger.info("IOException in onOpen");
		}
	}
	
	/**
	 * Is called when a web socket message arrives
	 * @param session holds all state for this web socket session
	 * @param contents the contents of the message
	 * @throws IOException on error writing to the socket session
	 */
	@OnMessage
	public void handleMessage(Session session, String contents) throws IOException {
		WebSocketConnection connection = WebSocketConnection.getWebSocketConnection(session);
		WebSession webSession = getWebSession(connection); 
		if (webSession == null) {
			logger.info("WebSocket message discarded: could not retrieve web session");
			return;
		}
		WebSocketMessage message = new WebSocketMessage(contents, connection);
		Handler handler = new Handler(webSession, message, connection);
		try {
			webSession.handleWebSocketMessage(handler);
		} finally {
			connection.flushBuffer();
			handler.release();
		}
	}
	
    /**
     * Is called when a web socket is closed
     * @param session holds all state for this web session
     * @param closeReason the reason why the socket is closed
     */
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info(String.format("Web socket session %s closed because of %s", session.getId(), closeReason));
    }
	
	/**
	 * Is called when a socket exception occurs
	 * @param t the generated exception
	 */
	@OnError
	public void onError(Throwable t) {
		logger.error("WebSocket error", t);
	}
}
