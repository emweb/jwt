/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
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

	private boolean containsXML = false;

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
		bundles.add(loadResourceBundle(bundleName, WApplication.getInstance().getLocale()));
		defaultBundles.add(loadResourceBundle(bundleName, new Locale("")));
	}

	public void useBuiltin(String bundleName) {
		use(bundleName);
	}

	@Override
	public void refresh() {
		bundles.clear();
		defaultBundles.clear();
		
		for (String bundleName : bundleNames) {
			bundles.add(loadResourceBundle(bundleName, WApplication.getInstance().getLocale()));
			defaultBundles.add(loadResourceBundle(bundleName, new Locale("")));
		}
	}
	
	private ResourceBundle loadResourceBundle(String bundleName, Locale l) {
		ResourceBundle rb = null;
		try {
			rb = ResourceBundle.getBundle(bundleName, l);
		} catch(Exception e) {
		}
		
		if (rb != null)
			return rb;
		
		File f;
		for (String path : StringUtils.expandLocales(bundleName, WApplication.getInstance().getLocale().toString())) {
			f = new File(path + ".properties");
			try {
				if (f.exists())
					return new PropertyResourceBundle(new FileInputStream(f));
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
		
		throw new RuntimeException("JWt exception: Could not find resource \"" + bundleName + "\" for locale " + l.toString());
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
		
		return null;
	}
	
	private String checkForValidXml(String s) {
		/* FIXME, we should do this only once for every key ... */
		if (containsXML) {
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
	 * Returns whether this contains only valid XML.
	 * 
	 * Values that are being returned will be checked to be valid XML (text).
	 */
	public boolean containsXML() {
		return containsXML;
	}

	/** Sets whether this resource bundle contains only valid XML.
	 *
	 * @param containsXML
	 *
	 * @see #containsXML()
	 */
	public void setContainsXML(boolean containsXML) {
		this.containsXML = containsXML;
	}
}
