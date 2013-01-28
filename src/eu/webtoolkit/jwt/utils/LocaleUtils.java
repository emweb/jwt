package eu.webtoolkit.jwt.utils;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
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
		try {
			return NumberFormat.getInstance(locale).parse(value).doubleValue();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static int toInt(Locale locale, String value) {
		try {
			return NumberFormat.getInstance(locale).parse(value).intValue();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public static Locale getCurrentLocale() {
		WApplication app = WApplication.getInstance();
		if (app == null)
			return new Locale("");
		else
			return app.getLocale();
	}

	public static String toString(Locale locale, int value) {
		return NumberFormat.getInstance(locale).format(value);
	}	
}
