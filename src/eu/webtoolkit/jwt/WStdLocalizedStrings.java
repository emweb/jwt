/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

/**
 * A localized strings implementation that uses {@link java.util.ResourceBundle} resource bundles.
 * <p>
 * This localized strings class will translate localized {@link WString} strings in the {@link WApplication#getLocale()}
 * based on their {@link WString#getKey()}.
 * <p>
 * When a key cannot be found (not in the locale specific or default bundle), the translated string will be "??<i>key</i>??".
 * 
 * @see WApplication#setLocalizedStrings(WLocalizedStrings)
 */
public class WStdLocalizedStrings extends WLocalizedStrings {
	private List<String> bundleNames = new ArrayList<String>();
	private List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();
	private List<ResourceBundle> defaultBundles = new ArrayList<ResourceBundle>();
	
	private boolean plainText = false;

	/**
	 * Constructor.
	 */
	public WStdLocalizedStrings() {
	}

	/**
	 * Set the bundle name.
	 * 
	 * The argument will be passed to {@link java.util.ResourceBundle#getBundle(String)}.
	 * 
	 * @param bundleName
	 */
	public void use(String bundleName) {
		bundleNames.add(bundleName);
		bundles.add(ResourceBundle.getBundle(bundleName, WApplication.getInstance().getLocale()));
		defaultBundles.add(ResourceBundle.getBundle(bundleName, new Locale("")));
	}

	@Override
	public void refresh() {
		bundles.clear();
		defaultBundles.clear();
		
		for (String bundleName : bundleNames) {
			bundles.add(ResourceBundle.getBundle(bundleName, WApplication.getInstance().getLocale()));
			defaultBundles.add(ResourceBundle.getBundle(bundleName, new Locale("")));
		}
	}

	@Override
	public String resolveKey(String key) {
		for (ResourceBundle bundle : bundles) {
			try {
				return checkForValidXml(bundle.getString(key));
			} catch (java.util.MissingResourceException mre) {
				
			}
		}
		
		for (ResourceBundle defaultBundle : defaultBundles) {
			try {
				return checkForValidXml(defaultBundle.getString(key));
			} catch (java.util.MissingResourceException mre) {
				
			}
		}
		
		return "??" + key + "??";
	}
	
	private String checkForValidXml(String s) {
		if (!plainText && WApplication.getInstance().getSession().getController().getConfiguration().isDebug()) {
			try {
				IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
				IXMLReader reader = StdXMLReader.stringReader("<span>" + s + "</span>");
				parser.setReader(reader);
				parser.parse();
				return s;
			} catch (Exception e) {
				throw new RuntimeException("WStdLocalizedStrings: no valid xml: \"" + s + "\"");
			}
		} else 
			return s;
	}

	/**
	 * Returns whether this resource bundle is plain text or can contain XML.
	 * When the bundle is able to contain XML, and debug is on, 
	 * the resources will be checked for valid XML when they are resolved.W
	 */
	public boolean isPlainText() {
		return plainText;
	}

	/** Sets whether this resource bundle is plain text or can contain XML.
	 * 
	 * @param plainText
	 * 
	 * @see #isPlainText()
	 */
	public void setPlainText(boolean plainText) {
		this.plainText = plainText;
	}
}
