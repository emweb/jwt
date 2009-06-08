/*
 * Copyright (C) 2006 Pieter Libin, Leuven, Belgium.
 *
 * Licensed under the terms of the GNU General Public License,
 * see the LICENSE file for more details.
 */

package eu.webtoolkit.jwt.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;

import eu.webtoolkit.jwt.MatchFlag;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WtException;

public class StringUtils {
	// !! maybe we also need to make a function which only replaces 1 char by a
	// string, making this specific will make it a bit faster
	public static String replaceAll(final String string,
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

	public static String escapeText(final String text, boolean newlinesToo) {
		if (newlinesToo)
			return replaceAll(text, "&<>\n", toReplaceWithNewLines_);
		else
			return replaceAll(text, "&<>", toReplaceWith_);
	}

	public static String readStringFromFile(String fileName) {
		String thisLine;
		StringBuilder builder = new StringBuilder();
		try {
			InputStream is = new FileInputStream(new File(fileName));
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			while ((thisLine = br.readLine()) != null) {
				builder.append(thisLine + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	public static String terminate(String s, char c) {
		if (s.length() > 0 && s.charAt(s.length() - 1) == c)
			return s;
		else
			return s + c;
	}

	public static String put(String s, int pos, char c) {
		StringBuilder sb = new StringBuilder(s);
		sb.setCharAt(pos, c);
		return sb.toString();
	}

	public static void main(String[] args) {
		String a = "adklasjlkdabjsdkfljsdklfjklabkdlsfksdl;fab";
		String[] replaceWith = { "||", "**" };
		String b = replaceAll(a, "ab", replaceWith);
		System.err.println(a);
		System.err.println(b);

		String c = readStringFromFile("Boot.html");
		System.err.println(c);
	}

	public static boolean matchValue(Object v, Object value, EnumSet<MatchFlag> flags) {
		// TODO: take into account flags
		if (v == value)
			return true;
		else
			return asString(v).equals(asString(value));
	}

	public static String replace(String s, char c, String r) {
		return s.replaceAll("\\Q" + c + "\\E", r);
	}

	public static final int strpbrk(String src, int startPos, String matches) {
		for (int i = startPos; i < src.length(); i++) {
			for (int j = 0; j < matches.length(); j++) {
				if (src.charAt(i) == matches.charAt(j)) {
					return i;
				}
			}
		}

		return -1;
	}

	public static CharSequence asJSLiteral(Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	public static WString asString(Object data) {
		if (data instanceof WString)
			return (WString) data;
		else if (data == null)
			return WString.Empty;
		else
			return new WString(data.toString());
	}

	public static WString asString(Object data, String format) {
		if (data instanceof WDate) {
			WDate d = (WDate) data;
			return new WString(d.toString(format));
		} else
			return asString(data);
	}

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

	public static String addWord(String s, String w) {
		if (s.length() == 0)
			return w;
		else
			return s + ' ' + w;
	}

	public static String eraseWord(String s, String w) {
		int i = s.indexOf(w);
		if (i != -1) {
			String result = s.substring(0, Math.max(0, i - 1));
			if (i + w.length() + 1 < s.length())
				result += s.substring(i + w.length() + 1);
			return result;
		} else
			return s;
	}

	public static boolean startsWithIgnoreCase(String name, String string) {
		return name.toLowerCase().startsWith(string.toLowerCase());
	}

	public static boolean containsIgnoreCase(String value, String string) {
		return value.toLowerCase().contains(string.toLowerCase());
	}
	
    public static String trimLeft(String s) {
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
