/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

class InternalPathEncoder extends XHtmlFilter {

	static void EncodeInternalPathRefs(CharSequence text) {
		try {
			WString wText = WString.toWString(text);
			
			InternalPathEncoder encoder = new InternalPathEncoder();
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			parser.setBuilder(encoder);
			parser.setResolver(encoder);
			IXMLReader reader
				= StdXMLReader.stringReader("<span>" + wText.getValue() + "</span>");
			parser.setReader(reader);
			parser.parse();

			StringBuilder filtered = encoder.result();

			// 6 and 7 correct for respectively <span> and </span>
			wText.set(filtered.substring(6, filtered.length() - 7));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (XMLException e) {
			System.err.println("Error reading XHTML string: " + e.getMessage());
		}
	}

	private String currentTag, aClass;

	@Override
	public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
		if (currentTag.equals("a")) {
			if (key.equals("class")) {
				aClass = StringUtils.addWord(aClass, value);
			} else if (key.equals("href")) {
				String path = value;

				if (path.startsWith("#/")) {
					path = path.substring(1);

					String url;

					WApplication app = WApplication.getInstance();

					if (app.getEnvironment().hasAjax()) {
						url = app.getBookmarkUrl(path);

						super.addAttribute("onclick", nsPrefix, nsURI, WEnvironment.getJavaScriptWtScope()
							+ ".navigateInternalPath(event, " + WWebWidget.jsStringLiteral(path) + ");", type);

						aClass = StringUtils.addWord(aClass, "Wt-rr");
					} else {
						if (app.getEnvironment().agentIsSpiderBot())
							url = app.getBookmarkUrl(path);
						else
							url = app.getSession().getMostRelativeUrl(path);

						aClass = StringUtils.addWord(aClass, "Wt-ip");
					}

					super.addAttribute(key, nsPrefix, nsURI, app.resolveRelativeUrl(url), type);
				}
			}
		}

		super.addAttribute(key, nsPrefix, nsURI, value, type);
	}

	@Override
	public void startElement(String name, String nsPrefix, String nsURI, String systemID, int lineNr) throws Exception {
		currentTag = name;
		aClass = "";
	
		super.startElement(name, nsPrefix, nsURI, systemID, lineNr);
	}

	@Override
	public void elementAttributesProcessed(String name, String nsPrefix, String nsURI) throws Exception {
		if (aClass.length() > 0)
			super.addAttribute("class", nsPrefix, nsURI, aClass, "");

		super.elementAttributesProcessed(name, nsPrefix, nsURI);
	}
}