/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * String utility class.
 */
public class StringUtils {
	static final Pattern FLOAT_PATTERN;

	static {
		// This code is copied from the javadoc for java.lang.Double. Why they
		// didn't make this a field of Double is beyond me...
		String digits = "(\\p{Digit}+)";
		String hexDigits = "(\\p{XDigit}+)";
		// an exponent is 'e' or 'E' followed by an optionally
		// signed decimal integer.
		String exp = "[eE][+-]?" + digits;
		String fpRegex = ("[\\x00-\\x20]*" + // Optional leading "whitespace"
				"[+-]?(" + // Optional sign character
				"NaN|" + // "NaN" string
				"Infinity|" + // "Infinity" string

				// A decimal floating-point string representing a finite
				// positive
				// number without a leading sign has at most five basic pieces:
				// Digits . Digits ExponentPart FloatTypeSuffix
				//
				// Since this method allows integer-only strings as input
				// in addition to strings of floating-point literals, the
				// two sub-patterns below are simplifications of the grammar
				// productions from the Java Language Specification, 2nd
				// edition, section 3.10.2.

				// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
				"(((" + digits + "(\\.)?(" + digits + "?)(" + exp + ")?)|" +

				// . Digits ExponentPart_opt FloatTypeSuffix_opt
				"(\\.(" + digits + ")(" + exp + ")?)|" +

				// Hexadecimal strings
				"((" +
				// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
				"(0[xX]" + hexDigits + "(\\.)?)|" +

				// 0[xX] HexDigits_opt . HexDigits BinaryExponent
				// FloatTypeSuffix_opt
				"(0[xX]" + hexDigits + "?(\\.)" + hexDigits + ")" +

				")[pP][+-]?" + digits + "))" + "[fFdD]?))" + "[\\x00-\\x20]*");// Optional
																				// trailing
		FLOAT_PATTERN = Pattern.compile("^\\s*" + fpRegex);
	}
	
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
		if (format == null || format.length() == 0)
			return asString(data);
		else if (data instanceof WDate) {
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
	
	public static String replaceAll(String str, String pattern, String replace) {
		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();
		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str.substring(s, e));
			result.append(replace);
			s = e + pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
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

	static String replace(String s, String c, String r) {
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
    
	public static int findFirstOf(String text, String chars, int pos) {
		char[] textA = text.toCharArray();
		char[] charsA = chars.toCharArray();
		
		return strpbrk(textA, pos, charsA);
	}

	static boolean isValidUnicode(char c) {
		return ((c == 0x9) || (c == 0xA) || (c == 0xD)
				|| ((c >= 0x20) && (c <= 0xD7FF))
				|| ((c >= 0xE000) && (c <= 0xFFFD)) || ((c >= 0x10000) && (c <= 0x10FFFF)));
	}
	
	static void sanitizeUnicode(EscapeOStream sout, String text) {
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
		      if (isValidUnicode(c))
		    	  sout.append(c);
		      else 
		    	  sout.append(0xFFFD);
		}
	}
	
	static void split(Set<String> tokens, String in, String sep, boolean compress_adjacent_tokens) {
		for (String token : in.split(sep)) {
			tokens.add(token);
		}
	}
	
	static List<String> expandLocales(String bundleName, String locale) {
		List<String> expanded = new ArrayList<String>();

		StringBuffer localeBuffer = new StringBuffer(locale);
		
		while (localeBuffer.length() != 0) {
			expanded.add(bundleName + "_" + localeBuffer.toString());
			
			int index = localeBuffer.lastIndexOf("_");
			if (index == -1)
				index = 0;
			
			localeBuffer.delete(index, localeBuffer.length());
		}
		expanded.add(bundleName);
		
		return expanded;
	}

	static String append(String s, char c) {
		if (s.length() == 0 || s.charAt(s.length() - 1) != c)
		    return s + c;
		  else
		    return s;
	}
	
	static String prepend(String s, char c) {
		if (s.length() == 0 || s.charAt(0) != c)
		    return c + s;
		  else
		    return s;
	}

	static WString formatFloat(WString format, double value) {
		return new WString(String.format(format.toString(), value));
	}
	
	static String asJSLiteral(Object v, TextFormat textFormat) {
		if (v.getClass().equals(WString.class)) {
			WString s = (WString) v;
			boolean plainText = false;
			if (textFormat == TextFormat.XHTMLText) {
				if (s.isLiteral()) {
					plainText = !WWebWidget.removeScript(s);
				}
			} else {
				plainText = true;
			}
			if (plainText && textFormat != TextFormat.XHTMLUnsafeText) {
				s = WWebWidget.escapeText(s);
			}
			return WString.toWString(s).getJsStringLiteral();
		} else if (v.getClass().equals(String.class)) {
			WString s = new WString((String) v);
			boolean plainText;
			if (textFormat == TextFormat.XHTMLText) {
				plainText = !WWebWidget.removeScript(s);
			} else {
				plainText = true;
			}
			if (plainText && textFormat != TextFormat.XHTMLUnsafeText) {
				s = WWebWidget.escapeText(s);
			}
			return WString.toWString(s).getJsStringLiteral();
		} else if (v.getClass().equals(Boolean.class)) {
			boolean b = (Boolean) v;
			return b ? "true" : "false";
		} else if (v.getClass().equals(WDate.class)) {
			WDate d = (WDate) v;
			return "new Date(" + String.valueOf(d.getYear()) + ','
					+ String.valueOf(d.getMonth() - 1) + ','
					+ String.valueOf(d.getDay()) + ','
					+ String.valueOf(d.getHour()) + ','
					+ String.valueOf(d.getMinute()) + ','
					+ String.valueOf(d.getSecond()) + ','
					+ String.valueOf(d.getMillisecond()) + ')';
		} else if (v instanceof Number) {
			return v.toString();
		} else {
			return "'" + v.toString() + "'";
		}
	}
	
	static String urlEncode(String url) {
		return DomElement.urlEncodeS(url);
	}

	static String urlEncode(String url, String allowed) {
		return DomElement.urlEncodeS(url, allowed);
	}

	static String EncodeHttpHeaderField(String fieldname, String fieldValue) {
		// This implements RFC 5987
		return fieldname + "*=UTF-8''" + StringUtils.urlEncode(fieldValue);
	}
	
	private StringUtils() { }
}
