/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.text.DecimalFormat;

import eu.webtoolkit.jwt.MatchOptions;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WtException;

/**
 * String utility class.
 */
public class StringUtils {
	/**
	 * Convert an object to a {@link WString}.
	 */
	public static WString asString(Object data) {
		if (data instanceof WString)
			return (WString) data;
		else if (data == null)
			return WString.Empty;
		else
			return new WString(data.toString());
	}

	/**
	 * Convert an object to a {@link WString}.
	 * If the object is a {@link WDate}, 
	 * it is formatted with the format String.
	 */
	public static WString asString(Object data, String format) {
		if (data instanceof WDate) {
			WDate d = (WDate) data;
			return new WString(d.toString(format));
		} else if (data instanceof Double) {
			Double d = (Double) data;
			return new WString(new DecimalFormat(format).format(d));
		} else
			return asString(data);
	}

	/**
	 * Convert an object to a double.
	 */
	public static double asNumber(Object data) {
		if (data == null)
			return Double.NaN;
		else if (data instanceof WString)
			try {
				return Double.parseDouble(((WString) data).getValue());
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		else if (data instanceof String)
			try {
				return Double.parseDouble((String) data);
			} catch (NumberFormatException e) {
				return Double.NaN;
			}
		else if (data instanceof WDate)
			return (double) ((WDate) data).toJulianDay();
		else if (data instanceof Double)
			return (Double) data;
		else if (data instanceof Short)
			return ((Short) data).doubleValue();
		else if (data instanceof Integer)
			return ((Integer) data).doubleValue();
		else if (data instanceof Long)
			return ((Long) data).doubleValue();
		else if (data instanceof Float)
			return ((Float) data).doubleValue();
		else
			throw new WtException("WAbstractItemModel: unsupported type "
					+ data.getClass().getName());
	}
	
	// !! maybe we also need to make a function which only replaces 1 char by a
	// string, making this specific will make it a bit faster
	static String replaceAll(final String string,
			final String charsToChange, String[] newSubstrings) {
		boolean changed = false;
		StringBuffer sb = new StringBuffer(string.length());
		assert charsToChange.length() == newSubstrings.length;
		for (int i = 0; i < string.length(); i++) {
			for (int j = 0; j < charsToChange.length(); j++) {
				if (string.charAt(i) == charsToChange.charAt(j)) {
					sb.append(newSubstrings[j]);
					changed = true;
					break;
				}
			}
			if (!changed) {
				sb.append(string.charAt(i));
			} else {
				changed = false;
			}
		}

		return sb.toString();
	}

	private final static String[] toReplaceWith_ = { "&amp;", "&lt;", "&gt;" };
	private final static String[] toReplaceWithNewLines_ = { "&amp;", "&lt;",
			"&gt;", "<br />" };

	static String escapeText(final String text, boolean newlinesToo) {
		if (newlinesToo)
			return replaceAll(text, "&<>\n", toReplaceWithNewLines_);
		else
			return replaceAll(text, "&<>", toReplaceWith_);
	}

	static String terminate(String s, char c) {
		if (s.length() > 0 && s.charAt(s.length() - 1) == c)
			return s;
		else
			return s + c;
	}

	static String put(String s, int pos, char c) {
		StringBuilder sb = new StringBuilder(s);
		sb.setCharAt(pos, c);
		return sb.toString();
	}

	static boolean matchValue(Object query, Object value,
			MatchOptions options) {
		if (options.getType() == MatchOptions.MatchType.MatchExactly) {
			return (query.getClass().equals(value.getClass()))
					&& asString(query).equals(asString(value));
		} else {
			String query_str = asString(query).getValue();
			String value_str = asString(value).getValue();

			switch (options.getType()) {
			case MatchStringExactly:
				if (options.getFlags().contains(
						MatchOptions.MatchFlag.MatchCaseSensitive))
					return value_str.equals(query_str);
				else
					return value_str.equalsIgnoreCase(query_str);

			case MatchStartsWith:
				if (options.getFlags().contains(
						MatchOptions.MatchFlag.MatchCaseSensitive))
					return value_str.startsWith(query_str);
				else
					return value_str.toLowerCase().startsWith(
							query_str.toLowerCase());

			case MatchEndsWith:
				if (options.getFlags().contains(
						MatchOptions.MatchFlag.MatchCaseSensitive))
					return value_str.endsWith(query_str);
				else
					return value_str.toLowerCase().endsWith(
							query_str.toLowerCase());

			default:
				throw new WtException(
						"Not yet implemented: WAbstractItemModel.match with "
								+ "MatchOptions = " + options.getType() + " "
								+ options.getFlags().toString());
			}

		}
	}

	static String replace(String s, char c, String r) {
		return s.replaceAll("\\Q" + c + "\\E", r);
	}

	static final int strpbrk(char[] srcArray, int startPos, char[] matchesArray) {
		int matchesL = matchesArray.length;
		int srcL = srcArray.length;
		for (int i = startPos; i < srcL; i++) {
			char c = srcArray[i];
			for (int j = 0; j < matchesL ; j++) {
				if (c == matchesArray[j]) {
					return i;
				}
			}
		}

		return -1;
	}

	static String addWord(String s, String w) {
		if (s.length() == 0)
			return w;
		else
			return s + ' ' + w;
	}

	static String eraseWord(String s, String w) {
		int i = s.indexOf(w);
		if (i != -1) {
			String result = s.substring(0, Math.max(0, i - 1));
			if (i + w.length() + 1 < s.length())
				result += s.substring(i + w.length() + 1);
			return result;
		} else
			return s;
	}

	static boolean startsWithIgnoreCase(String name, String string) {
		return name.toLowerCase().startsWith(string.toLowerCase());
	}

	static boolean containsIgnoreCase(String value, String string) {
		return value.toLowerCase().contains(string.toLowerCase());
	}
	
    static String trimLeft(String s) {
        int start = 0;
        while (start < s.length()) {
            if (!Character.isWhitespace(s.charAt(start))) {
                break;
            }
            start++;
        }
        return s.substring(start);
    }
}
