package eu.webtoolkit.jwt;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WXmlLocalizedStrings extends WLocalizedStrings {
	private String bundleName;
	private Map<String, String> keyValues = null;

	public WXmlLocalizedStrings() {
		
	}

	public void use(String bundleName) {
		this.bundleName = bundleName;
		
		refresh();
	}

	public void refresh() {
		URL url = getClass().getResource(bundleName + "_" + WApplication.getInstance().getLocale() + ".xml");
		if(url == null)
			url = getClass().getResource(bundleName + ".xml");

		File xmlFile = new File(url.getFile());
		
		TransformerFactory tf = TransformerFactory.newInstance();        
        Transformer t = null;
		try {
			t = tf.newTransformer();
		} catch (TransformerConfigurationException e1) {
			throw new RuntimeException(e1);
		}

		if (xmlFile != null) {
			keyValues = new HashMap<String, String>();

			DocumentBuilder docBuilder = null;
			Document doc = null;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			docBuilderFactory.setIgnoringElementContentWhitespace(true);
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			try {
				doc = docBuilder.parse(xmlFile);
			} catch (Exception e) {
				e.printStackTrace();
			}

			NodeList nl = doc.getElementsByTagName("message");
			if(nl.getLength()>0) {
				Node node;
				for (int i = 0; i < nl.getLength(); i++) {
					node = nl.item(i);
					if (node.getNodeName().equals("message")) {				        
						String id = node.getAttributes().getNamedItem("id").getNodeValue();
						StringWriter writer = new StringWriter();
				        try {
							t.transform(new DOMSource(node), new StreamResult(writer));
						} catch (TransformerException e) {
							throw new RuntimeException(e);
						}

						// Remove leading <?xml version="1.0" encoding="UTF-8"?><message id=""> ...
						String xml = writer.toString().substring(53 + id.length());
						// ... and trailing </message>
						xml = xml.substring(0, xml.length() - 10);

						keyValues.put(id, xml);
					}
				}
			}
		}
	}

	public String resolveKey(String key) {
		if (keyValues != null)
			try {
				return keyValues.get(key);
			} catch (java.util.MissingResourceException mre) {
				return "??" + key + "??";
			}
		else
			return "??" + key + "??";
	}
}