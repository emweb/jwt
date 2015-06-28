package eu.webtoolkit.jwt;

import java.io.StringWriter;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

public class WTime {
	private static Logger logger = LoggerFactory.getLogger(WTimeEdit.class);

	enum CharState {
		CharUnhandled, CharHandled, CharInvalid
	};

	public static class RegExpInfo {
		String regexp;
	}

	private static class ParseState {
		int h, m, s, z, a;
		int hour, minute, sec, msec;
		boolean pm, parseAMPM, haveAMPM;
		int index;
	}

	private static class BuildState {
		int i;
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
		this();
		setHMS(h, m, s, ms);
	}

	/**
	 * Create time from unix timestamp
	 * 
	 * @param ms
	 */
	public WTime(long ms) {
		this();
		this.valid_ = true;
		this.null_ = false;
		this.time_ = ms % (1000 * 60 * 60 * 24);
	}

	/**
	 * Construct a Null time.
	 * 
	 * <p>
	 * A time for which isNull() returns true. A Null time is also invalid.
	 */
	public WTime() {
		this.time_ = 0;
		this.null_ = false;
		this.valid_ = false;
	}

	/**
	 * Sets the time.
	 * 
	 * m and s have range 0-59, and ms has range 0-999. When the time is
	 * invalid, isValid() is set to false.
	 */
	public boolean setHMS(int h, int m, int s, int ms) {
		this.null_ = false;

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
	 * Return the minute
	 */
	public int getMinute() {
		return Math.abs((int) time_ / (1000 * 60)) % 60;
	}

	/**
	 * Return the second
	 */
	public int getSecond() {
		return Math.abs((int) time_ / (1000)) % 60;
	}

	/**
	 * Return the millisecond
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

	/**
	 * Return the default format of the time HH:mm:ss
	 */
	public static String getDefaultFormat() {
		return "HH:mm:ss";
	}

	/**
	 * Parses a string to a time using a default format.
	 * 
	 * The default format is "hh:mm:ss". For example, a time specified as:
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
	public static WTime fromString(String v, String format) {
		String f = format;

		boolean inQuote = false;
		boolean gotQuoteInQuote = false;

		WTime result = new WTime();

		ParseState timeParse = new ParseState();

		for (int fi = 0; fi <= f.length(); ++fi) {
			boolean finished = fi == f.length();
			char c = !finished ? f.charAt(fi) : 0;

			if (finished && inQuote)
				return result;

			if (inQuote) {
				if (c != '\'') {
					if (gotQuoteInQuote) {
						gotQuoteInQuote = false;
						inQuote = false;
					} else {
						if (timeParse.index >= v.length()
								|| (v.charAt(timeParse.index++) != c))
							return result;
					}
				} else {
					if (gotQuoteInQuote) {
						gotQuoteInQuote = false;
						if (timeParse.index >= v.length()
								|| (v.charAt(timeParse.index++) != c))
							return result;
					} else
						gotQuoteInQuote = true;
				}
			}

			if (!inQuote) {
				CharState state = CharState.CharUnhandled;
				CharState timeState = handleSpecial(c, v, timeParse, format);
				if (timeState == CharState.CharInvalid)
					return result;
				else if (timeState == CharState.CharHandled)
					state = CharState.CharHandled;

				if (!finished && state == CharState.CharUnhandled) {
					if (c == '\'') {
						inQuote = true;
						gotQuoteInQuote = false;
					} else if (timeParse.index >= v.length()
							|| (v.charAt(timeParse.index++) != c))
						return result;
				}
			}
		}

		if (timeParse.parseAMPM && timeParse.haveAMPM) {
			if (timeParse.pm)
				timeParse.hour = (timeParse.hour % 12) + 12;
			else
				timeParse.hour = timeParse.hour % 12;
		}

		result = new WTime(timeParse.hour, timeParse.minute, timeParse.sec,
				timeParse.msec);
		return result;
	}

	/**
	 * Formats this time to a string using a specified format.
	 * 
	 * @see SimpleDateFormat
	 * 
	 */
	public String toString(String format) {
		StringBuilder result = new StringBuilder();
		String f = format + "000";

		boolean inQuote = false;
		boolean gotQuoteInQuote = false;

		/**
		 * We need to scan the format first to determine whether it contains
		 * 'A(P)' or 'a(p)'
		 */
		boolean useAMPM = false;
		BuildState state = new BuildState();

		if (isValid()) {
			for (int i = 0; i < f.length() - 3; ++i) {
				if (inQuote) {
					if (f.charAt(i) != '\'') {
						if (gotQuoteInQuote) {
							gotQuoteInQuote = false;
							inQuote = false;
						}
					} else {
						if (gotQuoteInQuote)
							gotQuoteInQuote = false;
						else
							gotQuoteInQuote = true;
					}
				}

				if (!inQuote) {
					if (f.charAt(i) == 'a' || f.charAt(i) == 'A') {
						useAMPM = true;
						break;
					} else if (f.charAt(i) == '\'') {
						inQuote = true;
						gotQuoteInQuote = false;
					}
				}
			}

			for (state.i = 0; state.i < f.length() - 3; ++state.i) {
				if (inQuote) {
					if (f.charAt(state.i) != '\'') {
						if (gotQuoteInQuote) {
							gotQuoteInQuote = false;
							inQuote = false;
						} else
							result.append(f.charAt(state.i));
					} else {
						if (gotQuoteInQuote) {
							gotQuoteInQuote = false;
							result.append(f.charAt(state.i));
						} else
							gotQuoteInQuote = true;
					}
				}

				if (!inQuote) {
					boolean handled = false;
					if (!handled)
						handled = writeSpecial(f, state, result, useAMPM, 0);

					if (!handled) {
						if (f.charAt(state.i) == '\'') {
							inQuote = true;
							gotQuoteInQuote = false;
						} else
							result.append(f.charAt(state.i));
					}
				}
			}

			return result.toString();
		}
		return "";
	}

	/**
	 * Reports the current time (UTC clock).
	 * 
	 * This method returns the time as indicated by the system clock of the
	 * server, in UTC.
	 */
	public static WTime getCurrentServerTime() {
		return new WTime(System.currentTimeMillis());
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

	/**
	 * Returns true if time is not null
	 */
	public boolean isNull() {
		return null_;
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

		result.regexp = "^";
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
				i = formatHourToRegExp(result, f, i);
				break;
			case 'm':
				i = formatMinuteToRegExp(result, f, i);
				break;
			case 's':
				i = formatSecondToRegExp(result, f, i);
				break;
			case 'S':
				i = formatMSecondToRegExp(result, f, i);
				break;
			case 'Z':
				result.regexp += "(\\+[0-9]{4})";
				break;
			case 'A':
			case 'a':
				i = formatAPToRegExp(result, f, i);
				break;
			case '+':
				if (i < f.length() - 1 && f.charAt(i + 1) == 'h'
						|| f.charAt(i + 1) == 'H')
					result.regexp += "\\+";
				break;
			default:
				i = processChar(result, f, i);
				break;
			}

		}

		result.regexp += "$";
		System.out.println(result.regexp);
		return result;
	}

	private static CharState handleSpecial(char c, String v, ParseState parse,
			String format) {

		switch (c) {
		case 'H':
		case 'h':
			parse.parseAMPM = c == 'h';

			if (parse.h == 0)
				if (!parseLast(v, parse, format))
					return CharState.CharInvalid;

			++parse.h;
			return CharState.CharHandled;

		case 'm':
			if (parse.m == 0)
				if (!parseLast(v, parse, format))
					return CharState.CharInvalid;

			++parse.m;
			return CharState.CharHandled;

		case 's':
			if (parse.s == 0)
				if (!parseLast(v, parse, format))
					return CharState.CharInvalid;

			++parse.s;
			return CharState.CharHandled;

		case 'S':
			if (parse.z == 0)
				if (!parseLast(v, parse, format))
					return CharState.CharInvalid;

			++parse.z;
			return CharState.CharHandled;

		case 'A':
		case 'a':
			if (!parseLast(v, parse, format))
				return CharState.CharInvalid;

			parse.a = 1;
			return CharState.CharHandled;

		case 'P':
		case 'p':
			if (parse.a == 1) {
				if (!parseLast(v, parse, format))
					return CharState.CharInvalid;

				return CharState.CharHandled;
			}

			/* fall through */

		default:
			if (!parseLast(v, parse, format))
				return CharState.CharInvalid;

			return CharState.CharUnhandled;
		}

	}

	private static boolean parseLast(String v, ParseState parse, String format) {

		for (int i = 0; i < 4; ++i) {
			int count = 0;
			int value = 0;
			int maxCount = 2;

			switch (i) {
			case 0:
				count = parse.h;
				value = parse.hour;
				break;

			case 1:
				count = parse.m;
				value = parse.minute;
				break;

			case 2:
				count = parse.s;
				value = parse.sec;
				break;

			case 3:
				count = parse.z;
				value = parse.msec;
				maxCount = 3;
			}

			if (count != 0) {
				if (count == 1) {
					String str = "";

					if (parse.index >= v.length())
						return false;

					if ((i == 0)
							&& (v.charAt(parse.index) == '-' || v
									.charAt(parse.index) == '+')) {
						str += v.charAt(parse.index++);

						if (parse.index >= v.length())
							return false;
					}
					str += v.charAt(parse.index++);

					for (int j = 0; j < maxCount - 1; ++j)
						if (parse.index < v.length())
							if ('0' <= v.charAt(parse.index)
									&& v.charAt(parse.index) <= '9')
								str += v.charAt(parse.index++);
					try {
						value = Integer.parseInt(str);
					} catch (NumberFormatException e) {
						return false;
					}

				} else if (count == maxCount) {
					if (parse.index + (maxCount - 1) >= v.length())
						return false;

					String str = v.substring(parse.index, parse.index
							+ maxCount);
					parse.index += maxCount;

					try {
						value = Integer.parseInt(str);
					} catch (NumberFormatException e) {
						return false;
					}
				} else {
					return false;
				}
			}

			count = 0;
			switch (i) {
			case 0:
				parse.h = count;
				parse.hour = value;
				break;

			case 1:
				parse.m = count;
				parse.minute = value;
				break;

			case 2:
				parse.s = count;
				parse.sec = value;
				break;

			case 3:
				parse.z = count;
				parse.msec = value;
				maxCount = 3;
			}
		}

		if (parse.a != 0) {
			if (parse.index + 1 >= v.length())
				return false;

			String str = v.substring(parse.index, parse.index + 2);
			parse.index += 2;
			parse.haveAMPM = true;

			if (str == "am" || str == "AM")
				parse.pm = false;
			else if (str == "pm" || str == "PM")
				parse.pm = true;
			else
				return false;

			parse.a = 0;
		}

		return true;
	}

	private boolean writeSpecial(String f, BuildState state,
			StringBuilder result, boolean useAMPM, int zoneOffset) {
		switch (f.charAt(state.i)) {
		case '+':
			if (f.charAt(state.i + 1) == 'h' || f.charAt(state.i + 1) == 'H') {
				result.append((getHour() >= 0) ? '+' : '-');
				return true;
			}

			return false;

		case 'h':
			if (f.charAt(state.i + 1) == 'h') {
				++state.i;
				result.append(String.format("%02d",
						Math.abs(useAMPM ? getPmHour() : getHour())));
			} else
				result.append(Math.abs(useAMPM ? getPmHour() : getHour()));

			return true;

		case 'H':
			if (f.charAt(state.i + 1) == 'H') {
				++state.i;
				result.append(String.format("%02d", Math.abs(getHour())));
			} else
				result.append(Math.abs(getHour()));

			return true;

		case 'm':
			if (f.charAt(state.i + 1) == 'm') {
				++state.i;
				result.append(String.format("%02d", getMinute()));
			} else
				result.append(Math.abs(getMinute()));
			return true;

		case 's':
			if (f.charAt(state.i + 1) == 's') {
				++state.i;
				result.append(String.format("%02d", getSecond()));
			} else
				result.append(getSecond());
			return true;

		case 'Z': {
			boolean negate = zoneOffset < 0;
			if (!negate)
				result.append('+');
			else {
				result.append('-');
				zoneOffset = -zoneOffset;
			}

			int hours = zoneOffset / 60;
			int minutes = zoneOffset % 60;
			result.append(String.format("%02d", hours));
			result.append(String.format("%02d", minutes));
			return true;
		}

		case 'S':
			if (f.substring(state.i + 1, state.i + 3) == "SSS") {
				state.i += 3;
				result.append(String.format("%02d", getMsec()));
			} else
				result.append(getMsec());

			return true;

		case 'a':
		case 'A':
			if (getHour() < 12)
				result.append((f.charAt(state.i) == 'a') ? "am" : "AM");
			else
				result.append((f.charAt(state.i) == 'a') ? "pm" : "PM");

			if (f.charAt(state.i + 1) == 'p' || f.charAt(state.i + 1) == 'P')
				++state.i;

			return true;

		default:
			return false;
		}

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
			int i) {
		Character next = null;
		String sf = "";
		sf += format.charAt(i);
		for (int k = 0; k < 2; ++k) {
			if (i < format.length() - 1)
				next = format.charAt(i + 1);

			if (next != null && next == 'z') {
				sf += "z";
				next = null;
				i++;
			} else {
				next = null;
				break;
			}
		}

		if (sf.equals("z")) /* The Ms without trailing 0 */
			result.regexp += "(0|[1-9][0-9]{0,2})";
		else if (sf.equals("zzz"))
			result.regexp += "([0-9]{3})";

		return i;
	}

	private static int formatSecondToRegExp(RegExpInfo result, String format,
			int i) {
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
		return i;
	}

	private static int formatMinuteToRegExp(RegExpInfo result, String format,
			int i) {
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
		return i;
	}

	private static int formatHourToRegExp(RegExpInfo result, String format,
			int i) {
		/* Possible values */
		/* h, hh, H, HH */
		Character next = null;
		boolean ap = (format.contains("AP")) || (format.contains("ap")); // AM-PM
		String sf = "";
		sf += format.charAt(i);

		if (i < format.length() - 1)
			next = format.charAt(i + 1);
		if (next != null && next == 'h' || next == 'H') {
			sf += next;
			i++;
		} else {
			next = null;
			sf = "";
			sf += format.charAt(i);
		}

		if (sf.equals("HH") || (sf.equals("hh") && !ap)) { // Hour with leading
															// 0 0-23
			result.regexp += "(([0-1][0-9])|([2][0-3]))";
		} else if (sf.equals("hh") && ap) { // Hour with leading 0 01-12
			result.regexp += "(0[1-9]|[1][012])";
		} else if (sf.equals("H") || (sf.equals("h") && !ap)) { // Hour without
																// leading 0
																// 0-23
			result.regexp += "(0|[1-9]|[1][0-9]|2[0-3])";
		} else if (sf.equals("h") && ap) { // Hour without leading 0 0-12
			result.regexp += "([1-9]|1[012])";
		}
		return i;

	}

	private static int formatAPToRegExp(RegExpInfo result, String format, int i) {

		if (i < format.length() - 1) {
			if (format.charAt(i) == 'A' && format.charAt(i + 1) == 'P') {
				result.regexp += "([AP]M)";
				i++;
			} else if (format.charAt(i) == 'a' && format.charAt(i + 1) == 'p') {
				result.regexp += "([ap]m)";
				i++;
			}
		} else
			result.regexp += format.charAt(i);

		return i;
	}

	private boolean valid_;
	private boolean null_;
	private long time_;

}
