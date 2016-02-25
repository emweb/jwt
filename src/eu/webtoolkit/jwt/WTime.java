package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WTime {
	private static Logger logger = LoggerFactory.getLogger(WTimeEdit.class);

	static class RegExpInfo {
		String regexp;
		String hourGetJS;
		String minuteGetJS;
		String secGetJS;
		String msecGetJS;
	}

	/**
	 * Construct a time given hour, minutes.
	 * 
	 * @param minutes a range 0-59 A duration can be positive or negative depending on the
	 * sign of @param hours.
	 * 
	 * When the time is invalid, isValid() is set to false.
	 */
	public WTime(int hours, int minutes) {
		this(hours, minutes, 0, 0);
	}

	/**
	 * Construct a time given hour, minutes, seconds.
	 * 
	 * minutes and seconds have range 0-59 A duration can be positive or negative depending
	 * on the sign of hour.
	 * 
	 * When the time is invalid, isValid() is set to false.
	 */
	public WTime(int h, int m, int s) {
		this(h, m, s, 0);
	}

	/**
	 * Construct a time given hour, minutes, seconds, and milliseconds.
	 * 
	 * m and s have range 0-59, and ms has range 0-999. A duration can be
	 * positive or negative depending on the sign of h.
	 * 
	 * When the time is invalid, isValid() is set to false.
	 */
	public WTime(int h, int m, int s, int ms) {
		setHMS(h, m, s, ms);
	}

	/**
	 * Create time from a number of milliseconds since the EPOCH.
	 * 
	 * @param ms
	 */
	public WTime(long ms) {
		this.valid_ = true;
		this.time_ = ms % (1000 * 60 * 60 * 24);
	}

	/**
	 * Sets the time.
	 * 
	 * m and s have range 0-59, and ms has range 0-999. When the time is
	 * invalid, isValid() is set to false.
	 */
	public boolean setHMS(int h, int m, int s, int ms) {
		if (m >= 0 && m <= 59 && s >= 0 && s <= 59 && ms >= 0 && ms <= 999) {
			valid_ = true;
			boolean negative = h < 0;

			if (negative)
				h = -h;

			time_ = ((h * 60 + m) * 60 + s) * 1000 + ms;

			if (negative)
				time_ = -time_;
		} else {
			logger.warn(new StringWriter().append("Invalid time : ")
					.append(h + ":" + m + ":" + s).toString());
			valid_ = false;
		}

		return valid_;
	}

	/**
	 * Returns the hour
	 */
	public int getHour() {
		return (int) time_ / (1000 * 60 * 60);
	}

	/**
	 * Returns the hour in AM/PM format
	 */
	public int getPmHour() {
		int result = getHour() % 12;
		return result != 0 ? result : 12;
	}

	/**
	 * Returns the minutes
	 */
	public int getMinute() {
		return Math.abs((int) time_ / (1000 * 60)) % 60;
	}

	/**
	 * Returns the seconds
	 */
	public int getSecond() {
		return Math.abs((int) time_ / (1000)) % 60;
	}

	/**
	 * Returns the milliseconds
	 */
	public int getMsec() {
		return Math.abs((int) time_) % 1000;
	}

	/**
	 * Returns the number of seconds until t
	 */
	public long secsTo(WTime t) {
		return msecsTo(t) / 1000;
	}

	/**
	 * Returns the number of milliseconds until t
	 */
	public long msecsTo(WTime t) {
		if (isValid() && t.isValid())
			return t.time_ - time_;
		return 0;
	}

	public WTime addSecs(int seconds) {
		if (isValid())
			return new WTime(time_ + seconds * 1000);
		else
			return this;
	}

	/**
	 * Returns the default format of the time HH:mm:ss
	 */
	public static String getDefaultFormat() {
		return "HH:mm:ss";
	}

	/**
	 * Parses a string to a time using a default format.
	 * 
	 * The default format is "HH:mm:ss". For example, a time specified as:
	 * "22:55:15" will be parsed as a time that equals a time constructed as:
	 * WTime d(22,55,15); When the time could not be parsed or is not valid, an
	 * invalid time is returned (for which isValid() returns false).
	 */
	public static WTime fromString(String text) {
		return fromString(text, getDefaultFormat());
	}

	/**
	 * Parses a string to a time using a specified format.
	 * 
	 * The format follows the same syntax as used by toString(const WString&
	 * format). When the time could not be parsed or is not valid, an invalid
	 * time is returned (for which isValid() returns false).
	 * 
	 * @see #toString()
	 */
	public static WTime fromString(String text, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			formatter.setLenient(false);
			formatter.setCalendar(Calendar.getInstance());
			Date d = formatter.parse(text);
			if (d != null && formatter.format(d).equals(text))
				return new WDate(d).getTime();
		} catch (ParseException e) {
		}
		
		return null;
	}

	/**
	 * Formats this time to a string using a specified format.
	 * 
	 * @see java.text.SimpleDateFormat
	 */
	public String toString(String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new WDate(1980,1,1,getHour(),getMinute(),getSecond(),getMsec()).getDate());
	}

	/**
	 * Reports the current local server time.
	 * 
	 * This method returns the time as indicated by the server clock.
	 */
	public static WTime getCurrentServerTime() {
		return WDate.getCurrentServerDate().getTime();
	}

	/**
	 * Reports the current local client time.
	 * 
	 * This method uses browser information to retrieve the date that is
	 * configured in the client.
	 */
	public static WTime getCurrentTime() {
		return WDate.getCurrentDate().getTime();
	}
	
	/**
	 * Returns the string format of the time using the default format
	 */
	public String toString() {
		return toString(getDefaultFormat());
	}

	/**
	 * Returns true if time is valid time
	 */
	public boolean isValid() {
		return valid_;
	}

	static boolean usesAmPm(String format) {
		return format.indexOf('a') != -1;
	}
	
	/**
	 * Converts the format to a regular expression
	 * 
	 * The Format can contains characters with special meaning (@see
	 * #toString(String)) Any other text is kept literally. String content
	 * between single quotes (') are not interpreted as special codes. Inside a
	 * string, a literal quote may be specified using two single quotes ('').
	 * 
	 * @see #toString(String);
	 */
	public static RegExpInfo formatToRegExp(String format) {

		RegExpInfo result = new RegExpInfo();
		String f = format;
		int currentGroup = 1;

		result.hourGetJS = "return 1";
		result.minuteGetJS = "return 1";
		result.secGetJS = "return 1";
		result.msecGetJS = "return 1";
		
		result.regexp = "";
		
		boolean inQuote = false;

		for (int i = 0; i < f.length(); ++i) {
			if (inQuote && f.charAt(i) != '\'') {
				i = processChar(result, f, i);
				continue;
			}

			switch (f.charAt(i)) {
			case '\'':
				if (i < f.length() - 2 && f.charAt(i + 1) == f.charAt(i + 2)
						&& f.charAt(i + 1) == '\'')
					result.regexp += f.charAt(i);
				else
					inQuote = !inQuote;
			case 'h':
			case 'H':
				i = formatHourToRegExp(result, f, i, currentGroup++);
				break;
			case 'm':
				i = formatMinuteToRegExp(result, f, i, currentGroup++);
				break;
			case 's':
				i = formatSecondToRegExp(result, f, i, currentGroup++);
				break;
			case 'S':
				i = formatMSecondToRegExp(result, f, i, currentGroup++);
				break;
			case 'Z':
				result.regexp += "([+-][0-9]{4})";
				break;
			case 'a':
				i = formatAPToRegExp(result, f, i);
				break;
			default:
				i = processChar(result, f, i);
				break;
			}
		}
		
		return result;
	}

	private static int processChar(RegExpInfo result, String format, int i) {
		switch (format.charAt(i)) {
		case '.':
		case '+':
		case '$':
		case '^':
		case '*':
		case '[':
		case ']':
		case '{':
		case '}':
		case '(':
		case ')':
		case '?':
		case '!':
			result.regexp += "\\";
			break;
		}
		result.regexp += format.charAt(i);
		return i;
	}

	private static int formatMSecondToRegExp(RegExpInfo result, String format,
			int i, int currentGroup) {
		Character next = null;
		String sf = "";
		sf += format.charAt(i);
		for (int k = 0; k < 2; ++k) {
			if (i < format.length() - 1)
				next = format.charAt(i + 1);

			if (next != null && next == 'S') {
				sf += "S";
				next = null;
				i++;
			} else {
				next = null;
				break;
			}
		}

		if (sf.equals("S")) /* The Ms without trailing 0 */
			result.regexp += "(0|[1-9][0-9]{0,2})";
		else if (sf.equals("SSS"))
			result.regexp += "([0-9]{3})";
		
		result.msecGetJS = "return parseInt(results[" + Integer.toString(currentGroup) + "], 10);";

		return i;
	}

	private static int formatSecondToRegExp(RegExpInfo result, String format,
			int i, int currentGroup) {
		Character next = null;
		String sf = null;
		if (i < format.length() - 1)
			next = format.charAt(i + 1);

		if (next != null && next == 's') {
			sf = "ss";
			i++;
		} else {
			sf = "s";
			next = null;
		}

		if (sf == "s") /* Seconds without leading 0 */
			result.regexp += "(0|[1-5]?[0-9])";
		else
			/* Seconds with leading 0 */
			result.regexp += "([0-5][0-9])";
		
		result.secGetJS = "return parseInt(results[" + Integer.toString(currentGroup) + "], 10);";
		return i;
	}

	private static int formatMinuteToRegExp(RegExpInfo result, String format,
			int i, int currentGroup) {
		Character next = null;
		String sf = null;
		if (i < format.length() - 1)
			next = format.charAt(i + 1);

		if (next != null && next == 'm') {
			sf = "mm";
			i++;
		} else {
			sf = "m";
			next = null;
		}

		if (sf == "m") /* Minutes without leading 0 */
			result.regexp += "(0|[1-5]?[0-9])";
		else
			/* Minutes with leading 0 */
			result.regexp += "([0-5][0-9])";
		
		result.minuteGetJS = "return parseInt(results[" + Integer.toString(currentGroup) + "], 10);";
		return i;
	}

	private static int formatHourToRegExp(RegExpInfo result, String format,
			int i, int currentGroup) {
		/* Possible values */
		/* h, hh, H, HH */
		Character next = null;
		String sf = "";
		sf += format.charAt(i);

		if (i < format.length() - 1)
			next = format.charAt(i + 1);

		if (next != null && next == 'h' || next == 'H') {
			sf += next;
			i++;
		}

		if (sf.equals("HH")) { // Hour with leading 0 0-23
			result.regexp += "(([0-1][0-9])|([2][0-3]))";
		} else if (sf.equals("hh")) { // Hour with leading 0 01-12
			result.regexp += "(0[1-9]|[1][0-2])";
		} else if (sf.equals("H")) { // Hour without leading 0 0-23
			result.regexp += "([0-9]|[1][0-9]|2[0-3])";
		} else if (sf.equals("h")) { // Hour without leading 0 1-12
			result.regexp += "([1-9]|1[012])";
		}
		result.hourGetJS = "return parseInt(results[" + Integer.toString(currentGroup) + "], 10);";
		return i;
	}

	private static int formatAPToRegExp(RegExpInfo result, String format, int i) {
		result.regexp += "([AP]M)";
		return i;
	}
	
	/**
	 * Compares current time to given time
	 * @return true when current time is earlier than given time
	 */
	public boolean before(WTime t) {
		return this.time_ < t.time_;
	}
	
	/**
	 * Compares current time to given time
	 * @return true when current time is later than given time
	 */
	public boolean after(WTime t) {
		return this.time_ > t.time_;
	}
	
	/**
	 * Compares current time to given time
	 * @return true when times are equal
	 */
	public boolean equals(WTime t) {
		return this.time_ == t.time_;
	}
	
	/**
	 * Compares current time to given time
	 * @return true when current time is earlier than or equal to given time
	 */
	public boolean beforeOrEquals(WTime t) {
		return (this.equals(t) || this.before(t));
	}
	
	private boolean valid_;
	private long time_;
}
