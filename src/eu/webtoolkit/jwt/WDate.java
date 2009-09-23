/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class which holds a date on the gregorian calendar, specified as
 * day/month/year.
 * 
 * A valid date may be specified by year, month, and day of month (using the
 * {@link #WDate(int, int, int)} constructor, or the
 * {@link #setDate(int year, int month, int day)} method). When attempting to specify an invalid date
 * (with an impossible combination of year/month/date) an
 * {@link IllegalArgumentException} will be thrown.
 * 
 * The class provides a flexible way for converting between strings and dates.
 * Use toString() to convert to strings, and fromString() for parsing strings.
 * Both methods take a format string, and the same format syntax is supported by
 * both methods.
 * 
 * Simple operations are supported to compare dates, or to calculate with dates.
 * 
 * <i>This class is still missing localization support in its conversion methods
 * from and to string representations.</i>
 */
public class WDate implements Comparable<WDate> {
	static class RegExpInfo {
		public String regexp;
		public String yearGetJS;
		public String dayGetJS;
		public String monthGetJS;
	}

	enum Days {
		Sunday(Calendar.SUNDAY), Monday(Calendar.MONDAY), Tuesday(
				Calendar.TUESDAY), Wednesday(Calendar.WEDNESDAY), Thursday(
				Calendar.THURSDAY), Friday(Calendar.FRIDAY), Saturday(
				Calendar.SATURDAY);

		private int calendarCode;

		private Days(int calendarCode) {
			this.calendarCode = calendarCode;
		}
	}

	private final static String[] longMonthNames = { "January", "February",
			"March", "April", "May", "June", "July", "August", "September",
			"October", "November", "December" };
	private final static String[] shortMonthNames = { "Jan", "Feb", "Mar",
			"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	private final static String[] longDayNames = { "Monday", "Tuesday",
			"Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	private final static String[] shortDayNames = { "Mon", "Tue", "Wed", "Thu",
			"Fri", "Sat", "Sun" };

	private Date d;

	/**
	 * Specify a date by year, month (1-12), and day (1-31)
	 * 
	 * When the date is invalid, an IllegalArgumentException is thrown.
	 * 
	 * @see #setDate(int year, int month, int day)
	 * @see #getYear()
	 * @see #getMonth()
	 * @see #getDay()
	 */
	public WDate(int year, int month, int day) {
		d = new Date();
		setDate(year, month, day);
	}

	/**
	 * Specify a date by a Date object.
	 */
	public WDate(Date date) {
		d = date;
	}

	/**
	 * Set a date by year, month (1-12), and day (1-31)
	 * 
	 * When the new date is invalid, an IllegalArgumentException is thrown.
	 * 
	 * @see #WDate(int year, int month, int day)
	 * @see #getYear()
	 * @see #getMonth()
	 * @see #getDay()
	 */
	public void setDate(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(year, month - 1, day, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);

		if (c.get(Calendar.YEAR) != year || c.get(Calendar.MONTH) + 1 != month
				|| c.get(Calendar.DATE) != day) {
			throw new IllegalArgumentException("Illegal WDate");
		}

		d = c.getTime();
	}

	/**
	 * Add days to a date.
	 * 
	 * Returns a date that is <i>ndays</i> later than this date. Negative values
	 * for <i>ndays</i> will result in a date that is as many days earlier.
	 * 
	 * @see #addMonths(int)
	 * @see #addYears(int)
	 */
	public WDate addDays(int ndays) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DATE, ndays);
		d = c.getTime();
		return this;
	}

	/**
	 * Add months to a date.
	 * 
	 * Returns a date that is the same day of the month, but <i>nmonths</i>
	 * later than this date. Negative values for <i>nmonths</i> will result in a
	 * date that is as many months earlier.
	 * 
	 * @see #addDays(int)
	 * @see #addYears(int)
	 */
	public WDate addMonths(int nmonths) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, nmonths);
		d = c.getTime();
		return this;
	}

	/**
	 * Add years to a date.
	 * 
	 * Returns a date that is <i>nyears</i> later than this date. Negative
	 * values for <i>nyears</i> will result in a date that is as many years
	 * earlier.
	 * 
	 * @see #addDays(int)
	 * @see #addMonths(int)
	 */
	public WDate addYears(int nyears) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.YEAR, nyears);
		d = c.getTime();
		return this;
	}

	/**
	 * Year
	 */
	public int getYear() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.YEAR);
	}

	/**
	 * Month (1-12)
	 */
	public int getMonth() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.MONTH) + 1;
	}

	/**
	 * Day of month (1-31)
	 */
	public int getDay() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.DATE);
	}

	/**
	 * Day of week (1-7)
	 */
	public int getDayOfWeek() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * Returns the number of days from this date to <i>date</i>.
	 */
	public int daysTo(WDate date) {
		return (int) ((date.d.getTime() - d.getTime()) / 1000 / 3600 / 24);
	}

	/**
	 * Tests if this date is after the specified date.
	 * 
	 * @param when
	 *            - a date.
	 * @return true if and only if the instant represented by this Date object
	 *         is strictly earlier than the instant represented by when; false
	 *         otherwise.
	 */
	public boolean before(WDate when) {
		return d.before(when.d);
	}

	/**
	 * Tests if this date is after the specified date.
	 * 
	 * @param when
	 *            - a date.
	 * @return true if and only if the instant represented by this Date object
	 *         is strictly later than the instant represented by when; false
	 *         otherwise.
	 */
	public boolean after(WDate when) {
		return d.after(when.d);
	}

	/**
	 * Compares two dates for equality. The result is true if and only if the
	 * argument is not null and is a Date object that represents the same point
	 * in time, to the millisecond, as this object. Thus, two Date objects are
	 * equal if and only if the getTime method returns the same long value for
	 * both.
	 */
	public boolean equals(Object other) {
		if (other instanceof WDate) {
			WDate d2 = (WDate) other;
			return this.d.equals(d2.d);
		} else
			return false;
	}

	/**
	 * Returns the default date format.
	 */
	public static String getDefaultFormat() {
		return "ddd MMM d yyyy";
	}

	/**
	 * Construct a date for the current client date.
	 * 
	 * This method uses browser information to retrieve the date that is
	 * configured in the client.
	 */
	public static WDate getCurrentDate() {
		// TODO
		return new WDate(new Date());
	}

	/**
	 * Construct a date for the current server date.
	 * 
	 * This method returns the date as indicated by the system clock of the
	 * server.
	 */
	public static WDate getCurrentServerDate() {
		return getCurrentDate();
	}

	static boolean isLeapYear(int year) {
		GregorianCalendar c = new GregorianCalendar();
		return c.isLeapYear(year);
	}

	/**
	 * Returns the short day name.
	 * 
	 * Results (for given <i>weekDay</i>) are:<br>
	 * "Mon" (1),<br>
	 * "Tue" (2),<br>
	 * "Wed" (3),<br>
	 * "Thu" (4),<br>
	 * "Fri" (5),<br>
	 * "Sat" (6),<br>
	 * "Sun" (7).
	 * 
	 * @see #getLongDayName(int)
	 */
	public static String getShortDayName(int weekday) {
		return shortDayNames[weekday - 1];
	}

	/**
	 * Returns the short month name.
	 * 
	 * Results (for given <i>month</i>) are:<br>
	 * "Jan" (1),<br>
	 * "Feb" (2),<br>
	 * "Mar" (3),<br>
	 * "Apr" (4),<br>
	 * "May" (5),<br>
	 * "Jun" (6),<br>
	 * "Jul" (7),<br>
	 * "Aug" (8),<br>
	 * "Sep" (9),<br>
	 * "Oct" (10),<br>
	 * "Nov" (11),<br>
	 * "Dec" (12)<br>
	 * .
	 * 
	 * @see #getLongMonthName(int)
	 */
	public static String getShortMonthName(int month) {
		return shortMonthNames[month - 1];
	}

	/**
	 * Returns the long day name.
	 * 
	 * Results (for given <i>weekDay</i>) are:<br>
	 * "Monday" (1),<br>
	 * "Tuesday" (2),<br>
	 * "Wednesday" (3),<br>
	 * "Thursday" (4),<br>
	 * "Friday" (5),<br>
	 * "Saturday" (6),<br>
	 * "Sunday" (7).
	 * 
	 * @see #getShortDayName(int)
	 */
	public static String getLongDayName(int weekday) {
		return longDayNames[weekday - 1];
	}

	/**
	 * Returns the long month name.
	 * 
	 * Results (for given <i>month</i>) are:<br>
	 * "January" (1),<br>
	 * "February" (2),<br>
	 * "March" (3),<br>
	 * "April" (4),<br>
	 * "May" (5),<br>
	 * "June" (6),<br>
	 * "July" (7),<br>
	 * "August" (8),<br>
	 * "September" (9),<br>
	 * "October" (10),<br>
	 * "November" (11),<br>
	 * "December" (12).
	 * 
	 * @see #getShortDayName(int)
	 */
	public static String getLongMonthName(int month) {
		return longMonthNames[month - 1];
	}

	/**
	 * Parse a WString to a date using a default format.
	 * 
	 * The default <i>format</i> is "ddd MMM d yyyy". For example, a date
	 * specified as: <code>
	   *   "Wed Aug 29 2007"
	   * </code> will be parsed as a date that equals a date
	 * constructed as: <code>
	   *   WDate d = new WDate(2007,8,29);
	   * </code>
	 * 
	 * When the date could not be parsed or is not valid, 
	 * null is returned.
	 * 
	 * @see #fromString(String, String)
	 */
	public static WDate fromString(String text) {
		return fromString(text, getDefaultFormat());
	}

	/**
	 * Parse a String to a date using a specified format.
	 * 
	 * The <i>format</i> follows the same syntax as used by
	 * {@link #toString(String format)}.
	 * 
	 * When the date could not be parsed or is not valid, 
	 * null is returned.
	 * 
	 * @see #toString(String format)
	 */
	public static WDate fromString(String text, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			formatter.setLenient(false);
			return new WDate(formatter.parse(text));
		} catch (ParseException e) {
			return null;
		}
	}

	/**
	 * Converts the date to a Julian day.
	 * 
	 * @see #fromJulianDay(int)
	 */
	public int toJulianDay() {
		int year = getYear();
		int month = getMonth();
		int day = getDay();

		int y = year;

		if (year < 0) {
			y++;
		}

		int m = month;

		if (month > 2) {
			m++;
		} else {
			y--;
			m += 13;
		}

		int julian = (int) (java.lang.Math.floor(365.25 * y)
				+ java.lang.Math.floor(30.6001 * m) + day + 1720995.0);

		int yearZero = 15 + 31 * (10 + 12 * 1582);

		if (day + 31 * (month + 12 * year) >= yearZero) {
			int jadj = (int) (0.01 * y);

			julian += 2 - jadj + (int) (0.25 * jadj);
		}

		return julian;
	}

	/**
	 * Format this date to a String using a default format.
	 * 
	 * The default <i>format</i> is "ddd MMM d yyyy".
	 * 
	 * @see #toString(String format)
	 * @see #fromString(String)
	 */
	@Override
	public String toString() {
		return toString(getDefaultFormat()).toString();
	}

	/**
	 * Format this date to a WString using a specified format.
	 * 
	 * The <i>format</i> is a string in which the following contents has a
	 * special meaning.
	 * 
	 * <table>
	 * <tr>
	 * <td><b>Code</b></td>
	 * <td><b>Meaning</b></td>
	 * <td><b>Example (for Mon Aug 3 2007)</b></td>
	 * </tr>
	 * <tr>
	 * <td>d</td>
	 * <td>The day as a one or two-digit number</td>
	 * <td>3</td>
	 * </tr>
	 * <tr>
	 * <td>dd</td>
	 * <td>The day as a two-digit number (with leading 0)</td>
	 * <td>03</td>
	 * </tr>
	 * <tr>
	 * <td>ddd</td>
	 * <td>The day abbreviated using shortDayName()</td>
	 * <td>Mon</td>
	 * </tr>
	 * <tr>
	 * <td>dddd</td>
	 * <td>The day abbreviated using longDayName()</td>
	 * <td>Monday</td>
	 * </tr>
	 * <tr>
	 * <td>M</td>
	 * <td>The month as a one or two-digit number</td>
	 * <td>8</td>
	 * </tr>
	 * <tr>
	 * <td>MM</td>
	 * <td>The month as a two-digit number (with leading 0)</td>
	 * <td>08</td>
	 * </tr>
	 * <tr>
	 * <td>MMM</td>
	 * <td>The month abbreviated using shortMonthName()</td>
	 * <td>Aug</td>
	 * </tr>
	 * <tr>
	 * <td>MMMM</td>
	 * <td>The month abbreviated using longMonthName()</td>
	 * <td>August</td>
	 * </tr>
	 * <tr>
	 * <td>yy</td>
	 * <td>The year as a two-digit number</td>
	 * <td>07</td>
	 * </tr>
	 * <tr>
	 * <td>yyyy</td>
	 * <td>The year as a four-digit number</td>
	 * <td>2007</td>
	 * </tr>
	 * </table>
	 * 
	 * Any other text is kept literally. String content between single quotes
	 * (') are not interpreted as special codes. Inside a string, a literal
	 * quote may be specifed using a double quote ('').
	 * 
	 * Example of format and result:
	 * <table>
	 * <tr>
	 * <td><b>Format</b></td>
	 * <td><b>Result (for Mon Aug 3 2007)</b></td>
	 * </tr>
	 * <tr>
	 * <td>ddd MMM d yyyy</td>
	 * <td>Mon Aug 3 2007</td>
	 * </tr>
	 * <tr>
	 * <td>dd/MM/yyyy</td>
	 * <td>03/08/2007</td>
	 * </tr>
	 * <tr>
	 * <td>dddd, MMM d, yyyy</td>
	 * <td>Wednesday, Aug 3, 2007</td>
	 * </tr>
	 * <tr>
	 * <td>'MM': MM, 'd': d, 'yyyy': yyyy</td>
	 * <td>MM: 08, d: 3, yyyy: 2007</td>
	 * </tr>
	 * </table>
	 */
	public String toString(String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(this.d);
	}

	static WDate getPreviousWeekday(WDate d, Days gw) {
		Calendar c = Calendar.getInstance();
		c.setTime(d.d);
		while (true) {
			c.add(Calendar.DATE, -1);
			if (c.get(Calendar.DAY_OF_WEEK) == gw.calendarCode)
				break;
		}
		return new WDate(c.getTime());
	}

	static WDate.RegExpInfo formatToRegExp(String f) {
		WDate.RegExpInfo result = new WDate.RegExpInfo();

		int currentGroup = 1;

		result.regexp = "";
		result.dayGetJS = "1";
		result.monthGetJS = "1";
		result.yearGetJS = "2000";

		boolean inQuote = false;
		boolean gotQuoteInQuote = false;

		int d = 0, M = 0, y = 0;

		for (int i = 0; i < f.length(); ++i) {
			if (inQuote)
				if (f.charAt(i) != '\'')
					if (gotQuoteInQuote) {
						gotQuoteInQuote = false;
						inQuote = false;
					} else
						result.regexp += f.charAt(i);
				else if (gotQuoteInQuote) {
					gotQuoteInQuote = false;
					result.regexp += f.charAt(i);
				} else
					gotQuoteInQuote = true;

			if (!inQuote) {
				switch (f.charAt(i)) {
				case 'd':
					if (d == 0) {
						currentGroup = writeRegExpLast(result, d, M, y, f,
								currentGroup);
						d = M = y = 0;
					}
					++d;
					break;
				case 'M':
					if (M == 0) {
						currentGroup = writeRegExpLast(result, d, M, y, f,
								currentGroup);
						d = M = y = 0;
					}
					++M;
					break;
				case 'y':
					if (y == 0) {
						currentGroup = writeRegExpLast(result, d, M, y, f,
								currentGroup);
						d = M = y = 0;
					}
					++y;
					break;
				default:
					currentGroup = writeRegExpLast(result, d, M, y, f,
							currentGroup);
					d = M = y = 0;
					if (f.charAt(i) == '\'') {
						inQuote = true;
						gotQuoteInQuote = false;
					} else if ("/[\\^$.|?*+()".indexOf(f.charAt(i)) != -1)
						result.regexp += "\\" + f.charAt(i);
					else
						result.regexp += f.charAt(i);
				}
			}
		}

		currentGroup = writeRegExpLast(result, d, M, y, f, currentGroup);

		return result;
	}

	private static int writeRegExpLast(RegExpInfo result, int d, int M, int y,
			String format, int currentGroup) {
		if (d != 0) {
			switch (d) {
			case 1:
			case 2:
				if (d == 1)
					result.regexp += "(\\d{1,2})";
				else
					result.regexp += "(\\d{2})";
				result.dayGetJS = "parseInt(results["
						+ String.valueOf(currentGroup++) + "], 10)";
				break;
			default:
				fatalFormatRegExpError(format, d, "d's");
			}
		}

		if (M != 0) {
			switch (M) {
			case 1:
			case 2:
				if (M == 1)
					result.regexp += "(\\d{1,2})";
				else
					result.regexp += "(\\d{2})";
				result.monthGetJS = "parseInt(results["
						+ String.valueOf(currentGroup++) + "], 10)";
				break;
			default:
				fatalFormatRegExpError(format, M, "M's");
			}
			M = 0;
		}

		if (y != 0) {
			switch (y) {
			case 2:
				result.regexp += "(\\d{1,2})";
				result.yearGetJS = "function() { var y=parseInt(results["
						+ String.valueOf(currentGroup++)
						+ "], 10);return y>38?1900+y:2000+y;}()";
				break;
			case 4:
				result.regexp += "(\\d{4})";
				result.yearGetJS = "parseInt(results["
						+ String.valueOf(currentGroup++) + "], 10)";
				break;
			default:
				fatalFormatRegExpError(format, y, "y's");
			}
		}

		return currentGroup;
	}

	/**
	 * Converts a Julian Day <i>jd</i> to a WDate.
	 * 
	 * @see #toJulianDay()
	 */
	public static WDate fromJulianDay(int jd) {
		int julian = jd;
		int day, month, year;

		if (julian < 0) {
			julian = 0;
		}

		int a = julian;

		if (julian >= 2299161) {
			int jadj = (int) (((float) (julian - 1867216) - 0.25) / 36524.25);

			a += 1 + jadj - (int) (0.25 * jadj);
		}

		int b = a + 1524;
		int c = (int) (6680.0 + ((float) (b - 2439870) - 122.1) / 365.25);
		int d = (int) (365 * c + (0.25 * c));
		int e = (int) ((b - d) / 30.6001);

		day = b - d - (int) (30.6001 * e);
		month = e - 1;

		if (month > 12) {
			month -= 12;

		}

		year = c - 4715;

		if (month > 2) {
			--year;
		}

		if (year <= 0) {
			--year;
		}

		return new WDate(year, month, day);
	}

	private static void fatalFormatRegExpError(String format, int c, String cs) {
		StringBuilder s = new StringBuilder();
		s.append("WDate to regexp: (for \"").append(format).append(
				"\": cannot handle ").append(c).append(" consecutive ").append(
				cs);
		throw new RuntimeException(s.toString());
	}

	/**
	 * Returns the internal Date object.
	 */
	public Date getDate() {
		return d;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return d.hashCode();
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(WDate o) {
		if (o == null)
			return 1;

		return d.compareTo(o.d);
	}
}