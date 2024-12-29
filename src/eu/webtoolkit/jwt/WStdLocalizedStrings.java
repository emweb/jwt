/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * <p>
 * Unlike WXmlLocalizedStrings, this implementation does not handle plurals (WString#trn())
 * 
 * @see WApplication#setLocalizedStrings(WLocalizedStrings)
 */
public class WStdLocalizedStrings extends WLocalizedStrings {
	private Map<String, Bundle> bundles = new HashMap<>();

	private boolean containsXML = false;

	class Bundle {
		public Bundle(String bundleName) {
			this.bundleName = bundleName;
		}

		public LocalizedString resolveKey(final Locale locale, final String key) {
			if (!resources.containsKey(locale))
				load(locale);

			ResourceBundle res = resources.get(locale);
			String result = null;
			try {
				result = res.getString(key);
			} catch (java.util.MissingResourceException ignored) {
			}

			if (result == null) {
				final Locale defaultLocale = new Locale("");
				if (!resources.containsKey(defaultLocale))
					load(defaultLocale);

				if (resources.containsKey(defaultLocale)) {
					try {
						result = resources.get(defaultLocale).getString(key);
					} catch (java.util.MissingResourceException ignored) {
					}
				}
			}

			if (result != null)
				return new LocalizedString(result, WStdLocalizedStrings.this.containsXML ? TextFormat.XHTML : TextFormat.Plain);
			else
				return new LocalizedString();
		}

	
		private void load(final Locale locale) {
			ResourceBundle rb = null;
			try {
				rb = ResourceBundle.getBundle(bundleName, locale);
			} catch(Exception e) {
			}
			
			if (rb != null) {
				resources.put(locale, rb);
				return;
			}
			
			File f;
			for (String path : StringUtils.expandLocales(bundleName, locale.toString())) {
				f = new File(path + ".properties");
				try {
					if (f.exists()) {
						rb = new PropertyResourceBundle(new FileInputStream(f));
						resources.put(locale, rb);
						return;
					}
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
			}
		}

		public String bundleName;
		public Map<Locale, ResourceBundle> resources = new HashMap<>();
	}

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
		if (!this.bundles.containsKey(bundleName))
			bundles.put(bundleName, new Bundle(bundleName));
	}

	@Override
	public LocalizedString resolveKey(final Locale locale, final String key) {
		for (String bundleName : bundles.keySet()) {
			LocalizedString result = bundles.get(bundleName).resolveKey(locale, key);
			if (result.success) {
				return checkForValidXml(result);
			}
		}
		return new LocalizedString();
	}
	
	private static LocalizedString checkForValidXml(LocalizedString s) {
		/* FIXME, we should do this only once for every key ... */
		if (s.format != TextFormat.Plain) {
			try {
				IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
				IXMLReader reader = StdXMLReader.stringReader("<span>" + s.value + "</span>");
				parser.setReader(reader);
				parser.parse();
				return s;
			} catch (Exception e) {
				throw new RuntimeException("WStdLocalizedStrings: no valid xml: \"" + s.value + "\"");
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
