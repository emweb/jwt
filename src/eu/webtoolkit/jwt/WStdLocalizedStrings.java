package eu.webtoolkit.jwt;

import java.util.Locale;
import java.util.ResourceBundle;

public class WStdLocalizedStrings extends WLocalizedStrings {
	private String bundleName_;
	private ResourceBundle bundle_, defaultBundle_;

	public WStdLocalizedStrings() {
		refresh();
	}

	public void use(String bundleName) {
		bundleName_ = bundleName;
		refresh();
	}

	public void refresh() {
		if (bundleName_ != null) {
			bundle_ = ResourceBundle.getBundle(bundleName_, new Locale(WApplication.getInstance().getLocale()));
			defaultBundle_ = ResourceBundle.getBundle(bundleName_, new Locale(""));
		}
	}

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
