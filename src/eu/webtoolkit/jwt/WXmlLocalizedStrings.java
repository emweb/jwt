/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * WXmlLocalizedStrings is a {@link WLocalizedStrings} implementation which uses an XML file as input resource.
 */
public class WXmlLocalizedStrings extends WLocalizedStrings {
	private static Logger logger = LoggerFactory.getLogger(WXmlLocalizedStrings.class);

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
		WApplication app = WApplication.getInstance();
		
		Locale locale;
		if (app == null)
			locale = Locale.getDefault();
		else
			locale = app.getLocale();

		InputStream stream = null;
		for (String path : StringUtils.expandLocales(bundleName, locale.toString())) {
			try {
				stream = FileUtils.getResourceAsStream(path + ".xml");
			} catch (IOException e) {
			}
			if (stream != null)
				break;
		}
		
		if (stream == null) {
			logger.warn("Could not find resource \"" + bundleName + "\"");
			return;
		}
		
		try {
			XmlMessageParser xmlParser = new XmlMessageParser();
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			parser.setBuilder(xmlParser);
			parser.setResolver(xmlParser);
			IXMLReader reader = new StdXMLReader(stream);
			parser.setReader(reader);
			parser.parse();
			keyValues.putAll(xmlParser.getKeyValues());
		} catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException {}", bundleName, e);
		} catch (InstantiationException e) {
			logger.error("InstantiationException {}", bundleName, e);
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException {}", bundleName, e);
		} catch (IOException e) {
			logger.error("IOException {}", bundleName, e);
		} catch (XMLException e) {
			logger.error("XMLException {}", bundleName, e);
		}
	}

	public String resolveKey(String key) {
		if (keyValues != null && keyValues.get(key) != null)
			return keyValues.get(key);
		else
			return null;
	}

	public void useBuiltin(String bundle) {
		use(bundle);
	}
}