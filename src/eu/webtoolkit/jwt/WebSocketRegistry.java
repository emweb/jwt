package eu.webtoolkit.jwt;

import java.util.HashMap;
import java.util.Map;

/**
 * Web socket registry holding global information, such as all servlets
 * @author raf
 */
class WebSocketRegistry {
	private static WebSocketRegistry instance = null;
	
	private Map<Integer, WtServlet> servlets = new HashMap<Integer, WtServlet>();
	private int lastId = 0;
	
	private WebSocketRegistry() {};
	
	private synchronized static void createInstance() {
		if (instance == null) {
			instance = new WebSocketRegistry();
		}
	}
	
	public static WebSocketRegistry getInstance() {
		if (instance == null) {
			createInstance();
		}
		return instance;
	}
	
	/**
	 * Add a servlet to the registry, generate a unique identification and return this
	 * @param servlet the servlet reference to register
	 * @return a new unique identification, or -1 if it already exists
	 */
	public int addServlet(WtServlet servlet) {
		if (!this.servlets.containsValue(servlet)) {
			this.servlets.put(++lastId, servlet);
			return lastId;
		}
		return -1;
	}
	
	/**
	 * Get a WtServlet object reference based on its unique identification in the registry
	 * @param id the unique idenification (retrieved earlier via addServlet())
	 * @return the WtServlet reference, or null if the id is unknown
	 */
	public WtServlet getServlet(int id) {
		return this.servlets.get(id);
	}
}
