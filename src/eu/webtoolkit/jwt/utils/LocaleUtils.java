package eu.webtoolkit.jwt.utils;

import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;

import eu.webtoolkit.jwt.WApplication;

public class LocaleUtils {

	public static String getDecimalPoint(Locale locale) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
		return String.valueOf(symbols.getDecimalSeparator());
	}
	
	public static String getGroupSeparator(Locale locale) {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(locale);
		return String.valueOf(symbols.getGroupingSeparator());
	}
	
	public static String getDateFormat(Locale locale) {
		return "yyyy-MM-dd";
	}
	
	public static double toDouble(Locale locale, String value) {
		value = value.trim();
		ParsePosition pos = new ParsePosition(0);
		Number result = NumberFormat.getInstance(locale).parse(value, pos);
		if (pos.getIndex() != value.length())
			throw new NumberFormatException("Could not parse: " + value);
		return result.doubleValue();
	}

	public static int toInt(Locale locale, String value) {
		value = value.trim();
		ParsePosition pos = new ParsePosition(0);
		NumberFormat numberFormatter = NumberFormat.getInstance(locale);
		numberFormatter.setParseIntegerOnly(true);
		Number result = numberFormatter.parse(value, pos);
		if (pos.getIndex() != value.length())
			throw new NumberFormatException("Could not parse: " + value);
		return result.intValue();
	}

	public static Locale getCurrentLocale() {
		WApplication app = WApplication.getInstance();
		if (app == null)
			return new Locale("");
		else
			return app.getLocale();
	}

	public static String toFixedString(Locale locale, double value, int precision) {
		NumberFormat nf = NumberFormat.getInstance(locale);
		int current = nf.getMaximumFractionDigits();
		nf.setMaximumFractionDigits(precision);
		String ans = nf.format(value);
		nf.setMaximumFractionDigits(current);
		return ans;
	}	

	public static String toString(Locale locale, int value) {
		return NumberFormat.getInstance(locale).format(value);
	}

	public static String toString(Locale locale, double value) {
		return NumberFormat.getInstance(locale).format(value);
	}	
}
