package eu.webtoolkit.jwt;

import java.util.Locale;
import java.util.ResourceBundle;

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
	private String bundleName_;
	private ResourceBundle bundle_, defaultBundle_;

	/**
	 * Constructor.
	 */
	public WStdLocalizedStrings() {
		refresh();
	}

	/**
	 * Set the bundle name.
	 * 
	 * The argument will be passed to {@link java.util.ResourceBundle#getBundle(String)}.
	 * 
	 * @param bundleName
	 */
	public void use(String bundleName) {
		bundleName_ = bundleName;
		refresh();
	}

	@Override
	public void refresh() {
		if (bundleName_ != null) {
			bundle_ = ResourceBundle.getBundle(bundleName_, new Locale(WApplication.getInstance().getLocale()));
			defaultBundle_ = ResourceBundle.getBundle(bundleName_, new Locale(""));
		}
	}

	@Override
	public String resolveKey(String key) {
		if (bundle_ != null)
			try {
				return bundle_.getString(key);
			} catch (java.util.MissingResourceException mre) {
				if (defaultBundle_ != null)
					try {
						return defaultBundle_.getString(key);
					} catch (java.util.MissingResourceException mre2) {
						return "??" + key + "??";
					}
				else
					return "??" + key + "??";
			}
		else
			return "??" + key + "??";
	}
}
