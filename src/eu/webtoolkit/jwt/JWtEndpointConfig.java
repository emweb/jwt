package eu.webtoolkit.jwt;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

public class JWtEndpointConfig extends Configurator {
	private Map<Integer, WtServlet> servlets = new HashMap<Integer, WtServlet>();
	private int lastServletId = 0;
	
	public int addServlet(WtServlet servlet) {
		if (!servlets.containsValue(servlet)) {
			servlets.put(++lastServletId, servlet);
			return lastServletId;
		}
		return -1;
	}
	
	public WtServlet getServlet(int id) {
		return this.servlets.get(id);
	}

	@Override
	public void modifyHandshake(ServerEndpointConfig sec,
			HandshakeRequest request, HandshakeResponse response) {
		super.modifyHandshake(sec, request, response);
		sec.getUserProperties().put("Headers", request.getHeaders());
	}
}
