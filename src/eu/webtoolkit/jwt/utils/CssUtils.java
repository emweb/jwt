/*
 * Copyright (C) 2018 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.utils;

import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WCssRule;
import eu.webtoolkit.jwt.WCssStyleSheet;

import java.util.Locale;

public class CssUtils {
	public static <R extends WCssRule> R add(WCssStyleSheet s, R r) {
		return (R) s.addRule(r);
	}

    public static String colorToHex(WColor color) {
        return String.format((Locale)null, "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
