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
 * {@link #setDate(int year, int month, int day)} method). When attempting to
 * specify an invalid date (with an impossible combination of year/month/date)
 * an {@link IllegalArgumentException} will be thrown.
 * 
 * The class provides a flexible way for converting between strings and dates.
 * Use toString() to convert to strings, and fromString() for parsing strings.
 * Both methods take a format string, and the same format syntax is supported by
 * both methods.
 * 
 * Simple operations are supported to compare dates, or to calculate with dates.
 */
public class WDate implements Comparable<WDate> {
	static class RegExpInfo {
		public String regexp;
		public String yearGetJS;
		public String dayGetJS;
		public String monthGetJS;
	}

	enum Day {
		Sunday(Calendar.SUNDAY), Monday(Calendar.MONDAY), Tuesday(
				Calendar.TUESDAY), Wednesday(Calendar.WEDNESDAY), Thursday(
				Calendar.THURSDAY), Friday(Calendar.FRIDAY), Saturday(
				Calendar.SATURDAY);

		private int calendarCode;

		private Day(int calendarCode) {
			this.calendarCode = calendarCode;
		}
		
		// calendarCode: 0 (Sunday) to 6 (Saturday)
		static Day fromInt(int day) {
			return values()[day];
		}
	}

	private Date d;

	/**
	 * Set a date by year, month (1-12), day (1-31), hour (0-23), minute (0-59),
	 * second (0 - 59), millisecond (0 - 999)
	 * 
	 * When the date is invalid, an IllegalArgumentException is thrown.
	 * 
	 * @see #setDate(int year, int month, int day, int hour, int minute, int
	 *      second, int millisecond)
	 */
	public WDate(int year, int month, int day, int hour, int minute,
			int second, int millisecond) {
		d = new Date();
		setDate(year, month, day, hour, minute, second, millisecond);
	}

	/**
	 * Set a date by year, month (1-12), day (1-31), hour (0-23), minute (0-59),
	 * second (0 - 59)
	 * 
	 * When the date is invalid, an IllegalArgumentException is thrown.
	 * 
	 * @see #setDate(int year, int month, int day, int hour, int minute, int
	 *      second)
	 */
	public WDate(int year, int month, int day, int hour, int minute, int second) {
		this(year, month, day, hour, minute, second, 0);
	}

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
		this(year, month, day, 0, 0, 0);
	}

	/**
	 * Specify a date by a Date object.
	 */
	public WDate(Date date) {
		d = date;
	}

	/**
	 * Set a date by year, month (1-12), day (1-31), hour (0-23), minute (0-59),
	 * second (0 - 59), millisecond (0 - 999)
	 * 
	 * When the new date is invalid, an IllegalArgumentException is thrown.
	 * 
	 * @see #WDate(int year, int month, int day, int hour, int minute, int
	 *      second, int millisecond)
	 * @see #getYear()
	 * @see #getMonth()
	 * @see #getDay()
	 * @see #getHour()
	 * @see #getMinute()
	 * @see #getSecond()
	 * @see #getMillisecond()
	 */
	public void setDate(int year, int month, int day, int hour, int minute,
			int second, int millisecond) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(year, month - 1, day, hour, minute, second);
		c.set(Calendar.MILLISECOND, millisecond);

		if (c.get(Calendar.YEAR) != year || c.get(Calendar.MONTH) + 1 != month
				|| c.get(Calendar.DATE) != day
				|| c.get(Calendar.HOUR_OF_DAY) != hour
				|| c.get(Calendar.MINUTE) != minute
				|| c.get(Calendar.SECOND) != second
				|| c.get(Calendar.MILLISECOND) != millisecond) {
			throw new IllegalArgumentException("Illegal WDate");
		}

		d = c.getTime();
	}

	/**
	 * Set a date by year, month (1-12), day (1-31)
	 * 
	 * When the new date is invalid, an IllegalArgumentException is thrown.
	 * 
	 * @see #setDate(int, int, int, int, int, int)
	 */
	public void setDate(int year, int month, int day) {
		this.setDate(year, month, day, 0, 0, 0);
	}

	/**
	 * Set a date by year, month (1-12), and day (1-31), hour (0-23), minute
	 * (0-59), second (0 - 59).
	 * 
	 * When the new date is invalid, an IllegalArgumentException is thrown.
	 * 
	 * @see #setDate(int, int, int, int, int, int, int)
	 */
	public void setDate(int year, int month, int day, int hour, int minute,
			int second) {
		this.setDate(year, month, day, hour, minute, second, 0);
	}
	
	/**
	 * Set this date's time by hour (0-23), minute (0-59), second (0 - 59) and millisecond (0 - 999).
	 * 
	 * When the new date is invalid, an IllegalArgumentException is thrown.
	 */
	public void setTime(int hour, int minute, int second, int millisecond) {
		this.setDate(getYear(), getMonth(), getDay(), hour, minute, second, millisecond);
	}
	
	/**
	 * Set this date's time by hour (0-23), minute (0-59) and second (0 - 59).
	 * 
	 * When the new date is invalid, an IllegalArgumentException is thrown.
	 */
	public void setTime(int hour, int minute, int second) {
		this.setTime(hour, minute, second, 0);
	}
	
	/**
	 * Set this date's time by hour (0-23) and minute (0-59).
	 * 
	 * When the new date is invalid, an IllegalArgumentException is thrown.
	 */
	public void setTime(int hour, int minute) {
		this.setTime(hour, minute, 0, 0);
	}

	/**
	 * Adds seconds.
	 * 
	 * Returns a time that is <i> nSeconds </i> seconds later than this time.
	 * Negative values for <i> nSeconds </i> will result in a time that is as
	 * many seconds earlier.
	 */
	public WDate addSeconds(int nSeconds) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.SECOND, nSeconds);
		return new WDate(c.getTime());
	}

	/**
	 * Adds milliseconds.
	 * 
	 * Returns a time that is <i> nMilliseconds </i> milliseconds later than
	 * this time. Negative values for <i> nMilliseconds </i> will result in a
	 * time that is as many seconds earlier.
	 */
	public WDate addMilliseconds(int nMilliseconds) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MILLISECOND, nMilliseconds);
		return new WDate(c.getTime());
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
		return new WDate(c.getTime());
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
		return new WDate(c.getTime());
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
		return new WDate(c.getTime());
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
	 * Hour (0-24)
	 */
	public int getHour() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * Minute (0-59)
	 */
	public int getMinute() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.MINUTE);
	}

	/**
	 * Second (0-59)
	 */
	public int getSecond() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.SECOND);
	}

	/**
	 * Millisecond (0-999)
	 */
	public int getMillisecond() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.MILLISECOND);
	}

	/**
	 * Returns the difference between to time values (in seconds).
	 * 
	 * This returns a value between -86400 s and 86400 s.
	 */
	public int getSecondsTo(WDate d) {
		return (int) (getMillisecondsTo(d) / 1000);
	}

	/**
	 * Returns the difference between to time values (in milliseconds).
	 * 
	 * This returns a value between -86400000 ms and 86400000 ms.
	 */
	public int getMillisecondsTo(WDate d) {
		return (int) (d.d.getTime() - this.d.getTime());
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
	public int getDaysTo(WDate date) {
		return (int) ((date.d.getTime() - d.getTime()) / 1000 / 3600 / 24);
	}

	/**
	 * Returns the difference between two WDate values (as text).
	 * 
	 * This returns a textual representation of the approximate difference
	 * between this time and <i> other </i>. The textual representation returns
	 * the difference as a number of seconds, minutes, hours, days, weeks,
	 * months, or years, using the coarsest unit that is more than \p minValue.
	 * 
	 * @see #getDaysTo(WDate)
	 * @see #getSecondsTo(WDate)
	 */
	public String getTimeTo(WDate other, int minValue) {
		int secs = getSecondsTo(other);

		if (Math.abs(secs) < 1)
			return "less than a second";
		else if (Math.abs(secs) < 60 * minValue)
			return String.valueOf(secs) + " second" + multiple(secs, "s");
		else {
			int minutes = secs / 60;
			if (Math.abs(minutes) < 60 * minValue)
				return String.valueOf(minutes) + " minute"
						+ multiple(minutes, "s");
			else {
				int hours = minutes / 60;
				if (Math.abs(hours) < 24 * minValue)
					return String.valueOf(hours) + " hour"
							+ multiple(hours, "s");
				else {
					int days = hours / 24;
					if (Math.abs(days) < 7 * minValue)
						return String.valueOf(days) + " day"
								+ multiple(days, "s");
					else {
						if (Math.abs(days) < 31 * minValue) {
							int weeks = days / 7;
							return String.valueOf(weeks) + " week"
									+ multiple(weeks, "s");
						} else {
							if (Math.abs(days) < 365 * minValue) {
								int months = days / 30;
								return String.valueOf(months) + " month"
										+ multiple(months, "s");
							} else {
								int years = days / 365;
								return String.valueOf(years) + " year"
										+ multiple(years, "s");
							}
						}
					}
				}
			}
		}
	}

	private String multiple(int secs, String s) {
		if (Math.abs(secs) == 1)
			return "";
		else
			return s;
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
		return "ddd MMM d HH:mm:ss yyyy";
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
	 * Results (for given <i>weekDay</i>) are (default English):<br>
	 * "Mon" (1),<br>
	 * "Tue" (2),<br>
	 * "Wed" (3),<br>
	 * "Thu" (4),<br>
	 * "Fri" (5),<br>
	 * "Sat" (6),<br>
	 * "Sun" (7).
	 *
	 * The result is affected by localization using the "Wt.WDate.Mon" to
	 * "Wt.WDate.Sun" keys.
	 * 
	 * @see #getLongDayName(int)
	 */
	public static String getShortDayName(int weekday) {
		final String[] shortDayNames = { "Mon", "Tue", "Wed", "Thu",
			"Fri", "Sat", "Sun" };

		if (WApplication.getInstance() != null)
			return WString.tr("Wt.WDate.3." + shortDayNames[weekday - 1]).getValue();
		else
			return shortDayNames[weekday - 1];
	}

	/**
	 * Returns the short month name.
	 * 
	 * Results (for given <i>month</i>) are (default English):<br>
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
	 * "Dec" (12).
	 * 
	 * The result is affected by localization using the "Wt.WDate.Jan" to
	 * "Wt.WDate.Dec" keys.
	 * 
	 * @see #getLongMonthName(int)
	 */
	public static String getShortMonthName(int month) {
		final String[] shortMonthNames = { "Jan", "Feb", "Mar",
			"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		
		if (WApplication.getInstance() != null)
			return WString.tr("Wt.WDate." + shortMonthNames[month - 1]).getValue();
		else
			return shortMonthNames[month - 1];
	}

	/**
	 * Returns the long day name.
	 * 
	 * Results (for given <i>weekDay</i>) are (default English):<br>
	 * "Monday" (1),<br>
	 * "Tuesday" (2),<br>
	 * "Wednesday" (3),<br>
	 * "Thursday" (4),<br>
	 * "Friday" (5),<br>
	 * "Saturday" (6),<br>
	 * "Sunday" (7).
	 * 
	 * The result is affected by localization using the "Wt.WDate.Monday" to
	 * "Wt.WDate.Sunday" keys.
	 * 
	 * @see #getShortDayName(int)
	 */
	public static String getLongDayName(int weekday) {
		final String[] longDayNames = { "Monday", "Tuesday",
			"Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };

		if (WApplication.getInstance() != null)
			return WString.tr("Wt.WDate." + longDayNames[weekday - 1]).getValue();
		else
			return longDayNames[weekday - 1];
	}

	/**
	 * Returns the long month name.
	 * 
	 * Results (for given <i>month</i>) are (default English):<br>
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
	 * The result is affected by localization using the "Wt.WDate.January" to
	 * "Wt.WDate.December" keys.
	 * 
	 * @see #getShortDayName(int)
	 */
	public static String getLongMonthName(int month) {
		final String[] longMonthNames = { "January", "February",
			"March", "April", "May", "June", "July", "August", "September",
			"October", "November", "December" };

		if (WApplication.getInstance() != null)
			return WString.tr("Wt.WDate." + longMonthNames[month - 1]).getValue();
		else
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
	 * When the date could not be parsed or is not valid, null is returned.
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
	 * When the date could not be parsed or is not valid, null is returned.
	 * 
	 * @see #toString(String format)
	 */
	public static WDate fromString(String text, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			formatter.setLenient(false);
			Date d = formatter.parse(text);
			if (d != null && formatter.format(d).equals(text))
				return new WDate(d);
		} catch (ParseException e) {
		}
		
		return null;
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
		return toString(getDefaultFormat());
	}

	/**
	 * Format this date to a WString using a specified format.
	 * 
	 * The <i>format</i> is a string interpreted by {@link SimpleDateFormat}.
	 */
	public String toString(String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(this.d);
	}
	
	public String toString(String format, boolean localized) {
		return toString(format);
	}

	static WDate getPreviousWeekday(WDate d, Day gw) {
		Calendar c = Calendar.getInstance();
		c.setTime(d.d);
		
		// FIXME we shouldn't need a loop here!
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
				result.dayGetJS = "return parseInt(results["
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
				result.monthGetJS = "return parseInt(results["
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
				result.regexp += "(\\d{2})";
				result.yearGetJS = "var y=parseInt(results["
						+ String.valueOf(currentGroup++)
						+ "], 10);return y>38?1900+y:2000+y;";
				break;
			case 4:
				result.regexp += "(\\d{4})";
				result.yearGetJS = "return parseInt(results["
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
	 * Returns a hash code value for the object.
	 */
	public int hashCode() {
		return d.hashCode();
	}

	/**
	 * Compares this WDate object with the specified WDate object for order.
	 */
	public int compareTo(WDate o) {
		if (o == null)
			return 1;

		return d.compareTo(o.d);
	}
}