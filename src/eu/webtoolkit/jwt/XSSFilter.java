/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


import java.io.Reader;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class XSSFilter extends XHtmlFilter {
    private static Logger logger = LoggerFactory.getLogger(XSSFilter.class);

	protected int discarding = 0;

	static boolean removeScript(CharSequence text) {
		try {
			WString wText = WString.toWString(text);
			
			XSSFilter filter = new XSSFilter();
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			parser.setBuilder(filter);
			parser.setResolver(filter);
			IXMLReader reader = StdXMLReader.stringReader("<span>" + wText.getValue() + "</span>");
			parser.setReader(reader);
			parser.parse();

			String filtered = filter.result();

			// 6 and 7 correct for respectively <span> and </span>
			wText.set(filtered.substring(6, filtered.length() - 7));

			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLException e) {
			logger.error("Error reading XHTML string: " + e.getMessage());
		}

		return false;
	}

	@Override
	public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
		if (discarding != 0)
			return;
	
		if (XSSUtils.isBadAttribute(key) || XSSUtils.isBadAttributeValue(key, value)) {
		    logger.warn("(XSS) discarding invalid attribute: " + key + ": " + value);
			return;
		}

		super.addAttribute(key, nsPrefix, nsURI, value, type);
	}

	@Override
	public void addPCData(Reader reader, String systemID, int lineNr) throws Exception {
		if (discarding != 0)
			return;
	
		super.addPCData(reader, systemID, lineNr);
	}

	@Override
	public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
		if (discarding != 0)
			return;
		
		super.elementAttributesProcessed(name, nsPrefix, nsURI);
	}

	@Override
	public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
		if (discarding == 0 && XSSUtils.isBadTag(name)) {
			discarding = 1;
			logger.warn("(XSS) discarding invalid tag: " + name);
			return;
		}
	
		if (discarding != 0) {
			++discarding;
			return;
		}
	
		super.startElement(name, nsPrefix, nsURI, systemID, lineNr);
	}

	@Override
	public void endElement(String name, String nsPrefix, String nsURI) throws Exception {
		if (discarding != 0) {
			--discarding;
			return;
		}

		super.endElement(name, nsPrefix, nsURI);
	}
}