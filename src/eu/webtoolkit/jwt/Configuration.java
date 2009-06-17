package eu.webtoolkit.jwt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Wt configuration class.
 */
public class Configuration {
	public enum SessionTracking {
		CookiesURL, Auto
	}

	public enum ServerType {
		WtHttpdServer
	}

	private HashMap<String, String> properties_ = new HashMap<String, String>();
	private WLogger logger = new WLogger(System.err);
	private String redirectMessage_ = "Plain HTML version";
	private boolean sendXHTMLMimeType = true;
	private boolean inlineCss_= true;
	private ArrayList<String> botList = new ArrayList<String>();
	private ArrayList<String> ajaxAgentList = new ArrayList<String>();
	private boolean ajaxAgentWhiteList = false;
	private boolean debug = false;
	private String favicon = "";

	public Configuration() {
		botList.add(".*Googlebot.*");
		botList.add(".*msnbot.*");
		botList.add(".*Slurp.*");
		botList.add(".*Crawler.*");
		botList.add(".*Bot.*");
		botList.add(".ia_archiver.*");
		botList.add(".*Googlebot.*");
		botList.add(".*Twiceler.*");
	}
	
	public Configuration(File configurationFile) {
        final String errorMessage = "Error parsing configuration file: ";

		if (configurationFile != null) {
			DocumentBuilder docBuilder = null;
			Document doc = null;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			docBuilderFactory.setIgnoringElementContentWhitespace(true);
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(errorMessage + e.getMessage());
			}
			try {
				doc = docBuilder.parse(configurationFile);
			} catch (Exception e) {
				throw new RuntimeException(errorMessage + e.getMessage());
			}

			NodeList nl = doc.getElementsByTagName("wt-app");
			
			if(nl.getLength()>0) {
				NodeList elements = nl.item(0).getChildNodes();
				Node node;
				for (int i = 0; i < elements.getLength(); i++) {
					node = elements.item(i);
					if (node.getNodeName().equalsIgnoreCase("debug")) {
						setDebug(parseBoolean(errorMessage, node));
					} else if (node.getNodeName().equalsIgnoreCase("log-file")) {
						try {
							setLogger(new WLogger(new FileOutputStream(node.getTextContent().trim())));
						} catch (FileNotFoundException e) {
							throw new RuntimeException(errorMessage + "log-file file not found (" + node.getTextContent().trim() + ")");
						}
					} else if (node.getNodeName().equalsIgnoreCase("send-xhtml-mime-type")) {
						setSendXHTMLMimeType(parseBoolean(errorMessage, node));
					} else if (node.getNodeName().equalsIgnoreCase("redirect-message")) {
						setRedirectMessage(node.getTextContent().trim());
					} else if (node.getNodeName().equalsIgnoreCase("inline-css")) {
						setInlineCss(parseBoolean(errorMessage, node));
					} else if (node.getNodeName().equalsIgnoreCase("favicon")) {
						setFavicon(node.getTextContent().trim());
					} else if (node.getNodeName().equalsIgnoreCase("user-agents")) {
						if (node.getAttributes().getNamedItem("type") == null) {
							throw new RuntimeException(errorMessage + "user-agent elements require  a type specification");
						} else if (node.getAttributes().getNamedItem("type").getTextContent().trim().equals("ajax")) {
							String mode = node.getAttributes().getNamedItem("mode").getTextContent().trim();

							if (mode.equals("black-list"))
								ajaxAgentWhiteList = false;
							else if (mode.equals("white-list"))
								ajaxAgentWhiteList = true;
							else
								throw new RuntimeException(errorMessage + "unsupported user-agent mode: " + mode);
							
							parseUserAgents(errorMessage, node, ajaxAgentList);
						} else if (node.getAttributes().getNamedItem("type").getTextContent().trim().equals("bot")) {
							parseUserAgents(errorMessage, node, botList);
						}
					}
				}
			}
		}
	}
	
	private void parseUserAgents(String errorMessage, Node node, List<String> list) {
		NodeList userAgents = node.getChildNodes();
		Node n;
		for (int i = 0; i < userAgents.getLength(); i++) {
			n = userAgents.item(i);
			if(n.getNodeName().equals("user-agent")) {
				list.add(node.getTextContent().trim());
			}
		}
	}
	
	private boolean parseBoolean(String errorMessage, Node n) {
		try {
			return Boolean.parseBoolean(n.getTextContent().trim());
		} catch(Exception e) {
			throw new RuntimeException(errorMessage + "Cannot parse boolean value from element " + n.getNodeName());
		}
	}

	public HashMap<String, String> getProperties() {
		return properties_;
	}
	
	public String getProperty(String name) {
		return properties_.get(name);
	}

	public String getRedirectMessage() {
		return redirectMessage_;
	}

	int getMaxRequestSize() {
		return 0;
	}

	public WLogger getLogger() {
		return logger;
	}

	SessionTracking getSessionTracking() {
		return SessionTracking.Auto;
	}

	int getSessionTimeout() {
		return 600;
	}

	ServerType getServerType() {
		return ServerType.WtHttpdServer;
	}

	boolean isReloadIsNewSession() {
		return true;
	}

	boolean isBehindReverseProxy() {
		return false;
	}

	public boolean isSendXHTMLMimeType() {
		return sendXHTMLMimeType ;
	}

	boolean isSerializedEvents() {
		return false;
	}

	public boolean isDebug() {
		return debug;
	}
	
	public void setDebug(boolean how) {
		debug = how;
	}

	int getServerPushTimeout() {
		return 50;
	}

	public boolean isInlineCss() {
		return inlineCss_;
	}

	public void setProperties(HashMap<String, String> properties) {
		this.properties_ = properties;
	}

	public void setLogger(WLogger logger) {
		this.logger = logger;
	}

	public void setRedirectMessage(String redirectMessage) {
		this.redirectMessage_ = redirectMessage;
	}

	public void setSendXHTMLMimeType(boolean sendXHTMLMimeType) {
		this.sendXHTMLMimeType = sendXHTMLMimeType;
	}

	public void setInlineCss(boolean inlineCss) {
		this.inlineCss_ = inlineCss;
	}

	public ArrayList<String> getBotList() {
		return botList;
	}

	public ArrayList<String> getAjaxAgentList() {
		return ajaxAgentList;
	}

	public boolean isAjaxAgentWhiteList() {
		return ajaxAgentWhiteList;
	}

	public void setAjaxAgentList(ArrayList<String> ajaxAgentList, boolean isWhiteList) {
		this.ajaxAgentList = ajaxAgentList;
		this.ajaxAgentWhiteList = isWhiteList;
	}
	
	public String getFavicon() {
		return this.favicon;
	}
	
	public void setFavicon(String favicon) {
		this.favicon = favicon;
	}
}
