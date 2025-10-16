/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

class RefEncoder extends XHtmlFilter {
	private static Logger logger = LoggerFactory.getLogger(RefEncoder.class);

	private EnumSet<RefEncoderOption> options;

	public RefEncoder(EnumSet<RefEncoderOption> options) {
		super(false);

		this.options = options;
	}

	static WString EncodeRefs(CharSequence text, EnumSet<RefEncoderOption> options) {
		WString wText = WString.toWString(text);
		
		try {
			RefEncoder encoder = new RefEncoder(options);
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			parser.setBuilder(encoder);
			parser.setResolver(encoder);
			IXMLReader reader
				= StdXMLReader.stringReader("<span>" + wText.toXhtml() + "</span>");
			parser.setReader(reader);
			parser.parse();

			String filtered = encoder.result();

			// 6 and 7 correct for respectively <span> and </span>
			return new WString(filtered.substring(6, filtered.length() - 7));
		} catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException", e);
		} catch (InstantiationException e) {
			logger.error("InstantiationException", e);
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException", e);
		} catch (XMLException e) {
			logger.error("Error reading XHTML string: " + e.getMessage());
		}

		return wText;
	}

	private String currentTag, aClass;

	@Override
	public void addAttribute(String key, String nsPrefix, String nsURI, String value, String type) throws Exception {
		if (currentTag.equals("a")) {
			if (key.equals("class")) {
				aClass = StringUtils.addWord(aClass, value);
				return;
			} else if (key.equals("href")) {
				String path = value;

				WApplication app = WApplication.getInstance();

				if (options.contains(RefEncoderOption.EncodeInternalPaths)
				    && path.startsWith("#/")) {
					path = htmlEntityDecode(path.substring(1));

					String addClass, url;

					if (app.getEnvironment().hasAjax()) {
						url = app.getBookmarkUrl(path);

						super.addAttribute("onclick", nsPrefix, nsURI, WEnvironment.getJavaScriptWtScope()
							+ ".navigateInternalPath(event, " + WWebWidget.jsStringLiteral(path) + ");", type, true);

						addClass = "Wt-rr";
					} else {
						if (app.getEnvironment().agentIsSpiderBot())
							url = app.getBookmarkUrl(path);
						else
							url = app.getSession().getMostRelativeUrl(path);

						addClass = "Wt-ip";
					}

					aClass = StringUtils.addWord(aClass, addClass);

					value = htmlAttributeEncode(app.resolveRelativeUrl(url));
				} else if (options.contains(RefEncoderOption.EncodeRedirectTrampoline)) {
					if (path.indexOf("://") != -1) {
						path = "?request=redirect&amp;url=" + StringUtils.urlEncode(path);
						value = path;
					}
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
