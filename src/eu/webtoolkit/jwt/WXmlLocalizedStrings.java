/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLException;
import net.n3.nanoxml.XMLParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.webtoolkit.jwt.PluralExpression.evalPluralCase;

/**
 * WXmlLocalizedStrings is a {@link WLocalizedStrings} implementation which uses an XML file as input resource.
 */
public class WXmlLocalizedStrings extends WLocalizedStrings {
	private static Logger logger = LoggerFactory.getLogger(WXmlLocalizedStrings.class);

	static class Resource {
		public Map<String, List<String>> map_ = new HashMap<>();
		public String pluralExpression_ = "";
		int pluralCount_ = 0;
	}
	
	static class Bundle {
		public Bundle(String bundleName) {
			this.bundleName = bundleName;
		}
		
		public LocalizedString resolveKey(final Locale locale, final String key) {
			if (!resources.containsKey(locale.toString()))
				load(locale.toString());
			
			Resource res = resources.get(locale.toString());
			if (res.map_.get(key) == null) {
				if (resources.get("") == null)
					load("");
				
				if (resources.get("") != null)
					res = resources.get("");
			}

			List<String> result = res.map_.get(key);

			if (result != null && result.size() == 1) {
				return new LocalizedString(result.get(0), TextFormat.XHTML);
			} else
				return new LocalizedString();
		}
		
		public LocalizedString resolvePluralKey(final Locale locale, final String key, long amount) {
			if (!resources.containsKey(locale.toString()))
				load(locale.toString());
			
			Resource res = resources.get(locale.toString());
			if (res.map_.get(key) == null) {
				if (resources.get("") == null)
					load("");
				
				if (resources.get("") != null)
					res = resources.get("");
			}

			List<String> result = res.map_.get(key);

			if (result != null) {
				int c = evalPluralCase(res.pluralExpression_, amount);
				if (c < 0 || c > res.pluralCount_ || c > result.size()) {
					throw new WException("Expression '" + res.pluralExpression_ + "' evaluates to '"
							+ c + "' for n=" + amount + ", which is greater than the list of cases (size="
							+ result.size() + ").");
				} else {
					return new LocalizedString(result.get(c), TextFormat.XHTML);
				}
			} else
				return new LocalizedString();
		}
		
		private void load(final String locale) {
			if (!resources.containsKey(locale))
				resources.put(locale, new Resource());
			final Resource target = resources.get(locale);
			target.map_.clear();
			
			InputStream stream = null;
			String bundlePath = null;
			for (String path : StringUtils.expandLocales(bundleName, locale.toString())) {
				try {
					bundlePath = path + ".xml";
					stream = FileUtils.getResourceAsStream(bundlePath);
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
				target.map_.putAll(xmlParser.getKeyValues());
				target.pluralCount_ = xmlParser.getPluralCount();
				target.pluralExpression_ = xmlParser.getPluralExpression();
			} catch (ClassNotFoundException |
					 InstantiationException |
					 IllegalAccessException |
					 IOException |
					 XMLException e) {
				logger.error("Failed to load bundle: {}", bundlePath, e);
			}
		}
		
		public String bundleName;
		public Map<String, Resource> resources = new HashMap<>();
	}

	private Map<String, Bundle> bundles = new HashMap<>();

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
		if (!this.bundles.containsKey(bundleName))
			this.bundles.put(bundleName, new Bundle(bundleName));
	}

	public LocalizedString resolveKey(final Locale locale, final String key) {
		for (String bundleName : bundles.keySet()) {
			LocalizedString result = bundles.get(bundleName).resolveKey(locale, key);
			if (result.success)
				return result;
		}
		return new LocalizedString();
	}
	
	public LocalizedString resolvePluralKey(final Locale locale, final String key, long amount) {
		for (String bundleName : bundles.keySet()) {
			LocalizedString result = bundles.get(bundleName).resolvePluralKey(locale, key, amount);
			if (result.success)
				return result;
		}
		return new LocalizedString();
	}

	public void useBuiltin(String bundle) {
		use(bundle);
	}
}
