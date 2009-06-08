package eu.webtoolkit.jwt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class WDate implements Comparable<WDate> {
	static class RegExpInfo {
		public String regexp;
		public String yearGetJS;
		public String dayGetJS;
		public String monthGetJS;
	}

	public enum Days {
		Sunday(Calendar.SUNDAY), Monday(Calendar.MONDAY), Tuesday(Calendar.TUESDAY), Wednesday(Calendar.WEDNESDAY), Thursday(Calendar.THURSDAY), Friday(
				Calendar.FRIDAY), Saturday(Calendar.SATURDAY);

		private int calendarCode;

		private Days(int calendarCode) {
			this.calendarCode = calendarCode;
		}
	}

	final static String[] longMonthNames = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November",
			"December" };
	final static String[] shortMonthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	final static String[] longDayNames = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	final static String[] shortDayNames = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

	public Date d;

	public WDate(int year, int month, int day) {
		d = new Date();
		setDate(year, month, day);
	}

	public WDate(Date date) {
		d = date;
	}

	public void setDate(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(year, month - 1, day, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		if (c.get(Calendar.YEAR) != year || c.get(Calendar.MONTH) + 1 != month || c.get(Calendar.DATE) != day) {
			throw new IllegalArgumentException("Illegal WDate");
		}
			
		d = c.getTime();
	}

	public WDate addDays(int ndays) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DATE, ndays);
		d = c.getTime();
		return this;
	}

	public WDate addMonths(int nmonths) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, nmonths);
		d = c.getTime();
		return this;
	}

	public WDate addYears(int nyears) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.YEAR, nyears);
		d = c.getTime();
		return this;
	}

	public int getYear() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.YEAR);
	}

	public int getMonth() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.MONTH) + 1;
	}

	public int getDay() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.DATE);
	}

	public int getDayOfWeek() {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c.get(Calendar.DAY_OF_WEEK);
	}

	public int daysTo(WDate date) {
		return (int) ((date.d.getTime() - d.getTime()) / 1000 / 3600 / 24);
	}

	public boolean before(WDate other) {
		return d.before(other.d);
	}

	public boolean beforeOrEquals(WDate other) {
		return this.before(other) || this.equals(other);
	}

	public boolean after(WDate other) {
		return d.after(other.d);
	}

	public boolean afterOrEquals(WDate other) {
		return this.after(other) || this.equals(other);
	}

	public boolean equals(Object other) {
		if (other instanceof WDate) {
			WDate d2 = (WDate) other;
			return this.d.equals(d2.d);
		} else
			return false;
	}

	public boolean notEquals(WDate other) {
		return !this.equals(other);
	}

	public static String getDefaultFormat() {
		return "ddd MMM d yyyy";
	}

	public static WDate getCurrentDate() {
		return new WDate(new Date());
	}

	public static WDate getCurrentServerDate() {
		return getCurrentDate();
	}

	public static boolean isLeapYear(int year) {
		GregorianCalendar c = new GregorianCalendar();
		return c.isLeapYear(year);
	}

	public static String getShortDayName(int weekday, Locale locale) {
		return shortDayNames[weekday - 1];
	}

	public static String getShortDayName(int weekday) {
		return getShortDayName(weekday, Locale.ENGLISH);
	}

	public static String getShortMonthName(int month, Locale locale) {
		return shortMonthNames[month - 1];
	}

	public static String getShortMonthName(int month) {
		return getShortMonthName(month, Locale.ENGLISH);
	}

	public static String getLongDayName(int weekday, Locale locale) {
		return longDayNames[weekday - 1];
	}

	public static String getLongDayName(int weekday) {
		return getLongDayName(weekday, Locale.ENGLISH);
	}

	public static String getLongMonthName(int month, Locale locale) {
		return longMonthNames[month - 1];
	}

	public static String getLongMonthName(int month) {
		return getLongMonthName(month, Locale.ENGLISH);
	}

	public static WDate fromString(CharSequence text) {
		return fromString(text, getDefaultFormat());
	}
	
	public static WDate fromString(CharSequence text, String format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			return new WDate(formatter.parse(text.toString()));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Illegal WDate");
		}
	}

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
	
	@Override
	public String toString() {
		return toString(getDefaultFormat()).toString();
	}
	
	public String toString(CharSequence format) {
		SimpleDateFormat formatter = new SimpleDateFormat(format.toString());
		return formatter.format(this.d);
	}

	public static WDate getPreviousWeekday(WDate d, Days gw) {
		Calendar c = Calendar.getInstance();
		c.setTime(d.d);
		while (true) {
			c.add(Calendar.DATE, -1);
			if (c.get(Calendar.DAY_OF_WEEK) == gw.calendarCode)
				break;
		}
		return new WDate(c.getTime());
	}

	public static WDate.RegExpInfo formatToRegExp(String f) {
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
						currentGroup = writeRegExpLast(result, d, M, y, f, currentGroup);
						d = M = y = 0;
					}
					++d;
					break;
				case 'M':
					if (M == 0) {
						currentGroup = writeRegExpLast(result, d, M, y, f, currentGroup);
						d = M = y = 0;
					}
					++M;
					break;
				case 'y':
					if (y == 0) {
						currentGroup = writeRegExpLast(result, d, M, y, f, currentGroup);
						d = M = y = 0;
					}
					++y;
					break;
				default:
					currentGroup = writeRegExpLast(result, d, M, y, f, currentGroup);
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

	private static int writeRegExpLast(RegExpInfo result, int d, int M, int y, String format, int currentGroup) {
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
				result.yearGetJS = "function() { var y=parseInt(results[" + String.valueOf(currentGroup++) + "], 10);return y>38?1900+y:2000+y;}()";
				break;
			case 4:
				result.regexp += "(\\d{4})";
				result.yearGetJS = "parseInt(results[" + String.valueOf(currentGroup++) + "], 10)";
				break;
			default:
				fatalFormatRegExpError(format, y, "y's");
			}
		}

		return currentGroup;
	}
	
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
		s.append("WDate to regexp: (for \"").append(format).append("\": cannot handle ").append(c).append(" consecutive ").append(cs);
		throw new RuntimeException(s.toString());
	}

	public Date getDate() {
		return d;
	}

	public int hashCode() {
		return d.hashCode();
	}

	public int compareTo(WDate o) {
		if (o == null)
			return 1;

		return d.compareTo(o.d);
	}
}