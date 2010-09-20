/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.File;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

/**
 * WXmlLocalizedStrings is a {@link WLocalizedStrings} implementation which uses an XML file as input resource.
 */
public class WXmlLocalizedStrings extends WLocalizedStrings {
	private List<String> bundleNames = new ArrayList<String>();
	private Map<String, String> keyValues = new HashMap<String, String>();

	/**
	 * Constructor.
	 */
	public WXmlLocalizedStrings() {
		
	}

	/**
	 * Use an XML file as input resource.
	 * 
	 * @param bundleName
	 */
	public void use(String bundleName) {
		this.bundleNames.add(bundleName);
		readXmlResource(bundleName);
	}

	public void refresh() {
		keyValues.clear();
		
		for (String bundleName : bundleNames) 
			readXmlResource(bundleName);
	}
	
	private void readXmlResource(String bundleName) {
		URL url = getClass().getResource(bundleName + "_" + WApplication.getInstance().getLocale() + ".xml");
		if(url == null)
			url = getClass().getResource(bundleName + ".xml");
		
		//support external URLs
		if(url == null) {
			try {
				url = new URL(bundleName);
			} catch (MalformedURLException murle) {
				murle.printStackTrace();
			}
		}

		File xmlFile = new File(url.getFile());
		
		TransformerFactory tf = TransformerFactory.newInstance();        
        Transformer t = null;
		try {
			t = tf.newTransformer();
		} catch (TransformerConfigurationException e1) {
			throw new RuntimeException(e1);
		}

		if (xmlFile != null) {
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
		if (keyValues != null && keyValues.get(key) != null)
			return keyValues.get(key);
		else
			return null;
	}
}