package eu.webtoolkit.jwt;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Locale;

public class WDateTest {
	@Test
	public void testToStringEnglish() {
		Locale.setDefault(Locale.ENGLISH);

		WDate date = new WDate(2020, 1, 3, 11, 32, 0, 0);
		assertEquals("Fri Jan 3 11:32:00 2020", date.toString());
	}

	@Test
	public void testToStringLocalized() {
		Locale.setDefault(Locale.FRENCH);

		WDate date = new WDate(2020, 1, 3, 11, 32, 0, 0);
		assertEquals("ven. janv. 3 11:32:00 2020", date.toString());
	}

	@Test
	public void testToStringNotLocalized() {
		Locale.setDefault(Locale.FRENCH);

		WDate date = new WDate(2020, 1, 3, 11, 32, 0, 0);
		assertEquals("Fri Jan 3 11:32:00 2020", date.toString(WDate.getDefaultFormat(), false));
	}

	@Test
	public void testToStringCookieFmt() {
		Locale.setDefault(Locale.FRENCH);

		WDate date = new WDate(2020, 1, 3, 11, 32, 0, 0);
		assertEquals("Fri, 03-Jan-2020 11:32:00 GMT", date.toString("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'", false));
	}

	@Test
	public void testGetDaysTo() {
		Locale.setDefault(Locale.FRENCH);

		WDate date1 = new WDate(2020, 1, 3, 23, 59, 0, 0);
		WDate date2 = new WDate(2020, 1, 4, 0, 1, 0, 0);
		assertEquals(1, date1.getDaysTo(date2));
	}

	@Test
	public void testFromStringNonLocalized() {
		Locale.setDefault(Locale.ENGLISH);

		WDate date = new WDate(2022, 1, 2, 10, 8, 9, 0);
		assertEquals(date, WDate.fromString("10:08:09 02/01/2022", "HH:mm:ss dd/MM/yyyy"));
		assertEquals(date, WDate.fromString("10:08:09 Sun 02 Jan 2022", "HH:mm:ss EEE dd MMM yyyy"));
		assertEquals(date, WDate.fromString("10:08:09 Sunday 02 January 2022", "HH:mm:ss EEEE dd MMMM yyyy"));
		assertNull(WDate.fromString("10:08:09 dim. 02 janv. 2022", "HH:mm:ss EEE dd MMM yyyy"));
		assertNull(WDate.fromString("10:08:09 dimanche 02 janvier 2022", "HH:mm:ss EEEE dd MMMM yyyy"));
	}

	@Test
	public void testFromStringLocalized() {
		Locale.setDefault(Locale.FRENCH);

		WDate date = new WDate(2022, 1, 2, 10, 8, 9, 0);
		assertEquals(date, WDate.fromString("10:08:09 02/01/2022", "HH:mm:ss dd/MM/yyyy"));
		assertEquals(date, WDate.fromString("10:08:09 dim. 02 janv. 2022", "HH:mm:ss EEE dd MMM yyyy"));
		assertEquals(date, WDate.fromString("10:08:09 dimanche 02 janvier 2022", "HH:mm:ss EEEE dd MMMM yyyy"));
		assertNull(WDate.fromString("10:08:09 Sun 02 Jan 2022", "HH:mm:ss EEE dd MMM yyyy"));
		assertNull(WDate.fromString("10:08:09 Sunday 02 January 2022", "HH:mm:ss EEEE dd MMMM yyyy"));
	}

	@Test
	public void testFormatDateTimeToRegExpTimeDateShort() {
		WDate.RegExpInfo info = WDate.formatDateTimeToRegExp("H:m:s.S d-M-yy");
		assertEquals("(0|[1-9]|1[0-9]|2[0-3]):(0|[1-5]?[0-9]):(0|[1-5]?[0-9])\\.(0|[1-9][0-9]{0,2}) (\\d{1,2})-(\\d{1,2})-(\\d{2})", info.regexp);
		assertEquals("var y = parseInt(results[7], 10); return y > 38 ? 1900 + y : 2000 + y;", info.yearGetJS);
		assertEquals("return parseInt(results[6], 10);", info.monthGetJS);
		assertEquals("return parseInt(results[5], 10);", info.dayGetJS);
		assertEquals("return parseInt(results[1], 10);", info.hourGetJS);
		assertEquals("return parseInt(results[2], 10);", info.minuteGetJS);
		assertEquals("return parseInt(results[3], 10);", info.secGetJS);
		assertEquals("return parseInt(results[4], 10);", info.msecGetJS);
	}

	@Test
	public void testFormatDateTimeToRegExpTimeDateLong() {
		WDate.RegExpInfo info = WDate.formatDateTimeToRegExp("HH:mm:ss.SSS dd-MM-yyyy");
		assertEquals("([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])\\.([0-9]{3}) (\\d{2})-(\\d{2})-(\\d{4})", info.regexp);
		assertEquals("return parseInt(results[7], 10);", info.yearGetJS);
		assertEquals("return parseInt(results[6], 10);", info.monthGetJS);
		assertEquals("return parseInt(results[5], 10);", info.dayGetJS);
		assertEquals("return parseInt(results[1], 10);", info.hourGetJS);
		assertEquals("return parseInt(results[2], 10);", info.minuteGetJS);
		assertEquals("return parseInt(results[3], 10);", info.secGetJS);
		assertEquals("return parseInt(results[4], 10);", info.msecGetJS);
	}

	@Test
	public void testFormatDateTimeToRegExpTimeDateBoth() {
		WDate.RegExpInfo info = WDate.formatDateTimeToRegExp("h:mm:s.SSS a d-M-yy");
		assertEquals("([1-9]|1[012]):([0-5][0-9]):(0|[1-5]?[0-9])\\.([0-9]{3}) ([AP]M) (\\d{1,2})-(\\d{1,2})-(\\d{2})", info.regexp);
		assertEquals("var y = parseInt(results[8], 10); return y > 38 ? 1900 + y : 2000 + y;", info.yearGetJS);
		assertEquals("return parseInt(results[7], 10);", info.monthGetJS);
		assertEquals("return parseInt(results[6], 10);", info.dayGetJS);
		assertEquals("return parseInt(results[1], 10);", info.hourGetJS);
		assertEquals("return parseInt(results[2], 10);", info.minuteGetJS);
		assertEquals("return parseInt(results[3], 10);", info.secGetJS);
		assertEquals("return parseInt(results[4], 10);", info.msecGetJS);
	}

	@Test
	public void testFormatDateTimeToRegExpDateTimeShort() {
		WDate.RegExpInfo info = WDate.formatDateTimeToRegExp("d-M-yy H:m:s.S");
		assertEquals("(\\d{1,2})-(\\d{1,2})-(\\d{2}) (0|[1-9]|1[0-9]|2[0-3]):(0|[1-5]?[0-9]):(0|[1-5]?[0-9])\\.(0|[1-9][0-9]{0,2})", info.regexp);
		assertEquals("var y = parseInt(results[3], 10); return y > 38 ? 1900 + y : 2000 + y;", info.yearGetJS);
		assertEquals("return parseInt(results[2], 10);", info.monthGetJS);
		assertEquals("return parseInt(results[1], 10);", info.dayGetJS);
		assertEquals("return parseInt(results[4], 10);", info.hourGetJS);
		assertEquals("return parseInt(results[5], 10);", info.minuteGetJS);
		assertEquals("return parseInt(results[6], 10);", info.secGetJS);
		assertEquals("return parseInt(results[7], 10);", info.msecGetJS);
	}

	@Test
	public void testFormatDateTimeToRegExpDateTimeLong() {
		WDate.RegExpInfo info = WDate.formatDateTimeToRegExp("dd-MM-yyyy HH:mm:ss.SSS");
		assertEquals("(\\d{2})-(\\d{2})-(\\d{4}) ([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])\\.([0-9]{3})", info.regexp);
		assertEquals("return parseInt(results[3], 10);", info.yearGetJS);
		assertEquals("return parseInt(results[2], 10);", info.monthGetJS);
		assertEquals("return parseInt(results[1], 10);", info.dayGetJS);
	}

	@Test
	public void testFormatDateTimeToRegExpWithQuote() {
		WDate.RegExpInfo info = WDate.formatDateTimeToRegExp("dd-MM-yyyy 'SSS' HH:mm:ss");
		assertEquals("(\\d{2})-(\\d{2})-(\\d{4}) SSS ([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])", info.regexp);
		assertEquals("return parseInt(results[3], 10);", info.yearGetJS);
		assertEquals("return parseInt(results[2], 10);", info.monthGetJS);
		assertEquals("return parseInt(results[1], 10);", info.dayGetJS);
		assertEquals("return parseInt(results[4], 10);", info.hourGetJS);
		assertEquals("return parseInt(results[5], 10);", info.minuteGetJS);
		assertEquals("return parseInt(results[6], 10);", info.secGetJS);
		assertEquals("return 0;", info.msecGetJS);
	}

	@Test
	public void testFormatDateTimeToRegExpDateTimeBoth() {
		WDate.RegExpInfo info = WDate.formatDateTimeToRegExp("d-M-yy HH:m:ss.SSS a");
		assertEquals("(\\d{1,2})-(\\d{1,2})-(\\d{2}) ([0-1][0-9]|2[0-3]):(0|[1-5]?[0-9]):([0-5][0-9])\\.([0-9]{3}) ([AP]M)", info.regexp);
		assertEquals("var y = parseInt(results[3], 10); return y > 38 ? 1900 + y : 2000 + y;", info.yearGetJS);
		assertEquals("return parseInt(results[4], 10);", info.hourGetJS);
		assertEquals("return parseInt(results[5], 10);", info.minuteGetJS);
		assertEquals("return parseInt(results[6], 10);", info.secGetJS);
		assertEquals("return parseInt(results[7], 10);", info.msecGetJS);
	}

	@Test
	public void testFormatDateTimeToRegExpInterleaved() {
		WDate.RegExpInfo info = WDate.formatDateTimeToRegExp("H dd yyyy --m--ss.SSS / MM");
		assertEquals("(0|[1-9]|1[0-9]|2[0-3]) (\\d{2}) (\\d{4}) --(0|[1-5]?[0-9])--([0-5][0-9])\\.([0-9]{3}) \\/ (\\d{2})", info.regexp);
		assertEquals("return parseInt(results[3], 10);", info.yearGetJS);
		assertEquals("return parseInt(results[7], 10);", info.monthGetJS);
		assertEquals("return parseInt(results[2], 10);", info.dayGetJS);
		assertEquals("return parseInt(results[1], 10);", info.hourGetJS);
		assertEquals("return parseInt(results[4], 10);", info.minuteGetJS);
		assertEquals("return parseInt(results[5], 10);", info.secGetJS);
		assertEquals("return parseInt(results[6], 10);", info.msecGetJS);
	}

	@Test(expected = eu.webtoolkit.jwt.WException.class)
	public void testFormatDateTimeToRegExpYearNotAllowed() {
		WDate.formatDateTimeToRegExp("HH mm ss a dd MM yyy");
	}

	@Test(expected = eu.webtoolkit.jwt.WException.class)
	public void testFormatToRegExpMonthNotAllowed() {
		WDate.formatDateTimeToRegExp("HH mm ss a dd MMM yyyy");
	}

	@Test(expected = eu.webtoolkit.jwt.WException.class)
	public void testFormatToRegExpDayNotAllowed() {
		WDate.formatDateTimeToRegExp("HH mm ss a dd ddddd MM yyyy");
	}

	@Test
	public void testFormatDateTimeToRegExpRepeatedFields() {
		assertEquals("([0-1][0-9]|2[0-3])([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9]) ([AP]M) (\\d{2}) (\\d{2}) (\\d{4})",
				WDate.formatDateTimeToRegExp("HHHH mm ss a dd MM yyyy").regexp);
		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9])(0|[1-5]?[0-9]) ([0-5][0-9]) ([AP]M) (\\d{2}) (\\d{2}) (\\d{4})",
				WDate.formatDateTimeToRegExp("HH mmm ss a dd MM yyyy").regexp);
		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9])([0-5][0-9])(0|[1-5]?[0-9]) ([AP]M) (\\d{2}) (\\d{2}) (\\d{4})",
				WDate.formatDateTimeToRegExp("HH mm sssss a dd MM yyyy").regexp);
	}

	@Test
	public void testFormatDateTimeToRegExpRepeatedMilliseconds() {
		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9])\\.([0-9]{3})(0|[1-9][0-9]{0,2}) ([AP]M) (\\d{2}) (\\d{2}) (\\d{4})",
				WDate.formatDateTimeToRegExp("HH mm ss.SSSS a dd MM yyyy").regexp);
		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9])\\. ([AP]M) (\\d{2}) (\\d{2}) (\\d{4})",
				WDate.formatDateTimeToRegExp("HH mm ss.SS a dd MM yyyy").regexp);
		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9])\\.([0-9]{3}) ([AP]M) (\\d{2}) (\\d{2}) (\\d{4})",
				WDate.formatDateTimeToRegExp("HH mm ss.SSSSS a dd MM yyyy").regexp);
	}

	@Test
	public void testSplitFormat() {
		assertEquals("yyyy-MM-dd", WDate.splitFormat("yyyy-MM-dd").dateFormat);
		assertEquals("", WDate.splitFormat("yyyy-MM-dd").timeFormat);
		assertEquals("", WDate.splitFormat("HH:mm:ss.zzzZ").dateFormat);
		assertEquals("HH:mm:ss.zzz", WDate.splitFormat("HH:mm:ss.zzzZ").timeFormat);
		assertEquals("yyyy-MM-dd", WDate.splitFormat("yyyy-MM-dd HH:mm:ss.zzzZ").dateFormat);
		assertEquals("HH:mm:ss.zzz", WDate.splitFormat("yyyy-MM-dd HH:mm:ss.zzzZ").timeFormat);
		assertEquals("yyyy-MM-dd", WDate.splitFormat("HH:mm:ss.zzzZ yyyy-MM-dd").dateFormat);
		assertEquals("HH:mm:ss.zzz", WDate.splitFormat("HH:mm:ss.zzzZ yyyy-MM-dd").timeFormat);
		assertEquals("dd/MM yyyy", WDate.splitFormat("dd/MM HH:mm:ss yyyy").dateFormat);
		assertEquals("HH:mm:ss", WDate.splitFormat("dd/MM HH:mm:ss yyyy").timeFormat);
	}

	@Test
	public void testFormatToRegExpShort() {
		WDate.RegExpInfo dateInfo = WDate.formatToRegExp("d-M-yy");

		assertEquals("(\\d{1,2})-(\\d{1,2})-(\\d{2})", dateInfo.regexp);
		assertEquals("var y = parseInt(results[3], 10); return y > 38 ? 1900 + y : 2000 + y;", dateInfo.yearGetJS);
		assertEquals("return parseInt(results[2], 10);", dateInfo.monthGetJS);
		assertEquals("return parseInt(results[1], 10);", dateInfo.dayGetJS);
	}

	@Test
	public void testFormatToRegExpLong() {
		WDate.RegExpInfo dateInfo = WDate.formatToRegExp("dd-MM-yyyy");

		assertEquals("(\\d{2})-(\\d{2})-(\\d{4})", dateInfo.regexp);
		assertEquals("return parseInt(results[3], 10);", dateInfo.yearGetJS);
		assertEquals("return parseInt(results[2], 10);", dateInfo.monthGetJS);
		assertEquals("return parseInt(results[1], 10);", dateInfo.dayGetJS);
	}

	@Test
	public void testFormatToRegExpBoth() {
		WDate.RegExpInfo dateInfo = WDate.formatToRegExp("dd-M-yy");

		assertEquals("(\\d{2})-(\\d{1,2})-(\\d{2})", dateInfo.regexp);
		assertEquals("var y = parseInt(results[3], 10); return y > 38 ? 1900 + y : 2000 + y;", dateInfo.yearGetJS);
		assertEquals("return parseInt(results[2], 10);", dateInfo.monthGetJS);
		assertEquals("return parseInt(results[1], 10);", dateInfo.dayGetJS);
	}

	@Test(expected = eu.webtoolkit.jwt.WException.class)
	public void testFormatToRegExpBadFormatting3YNotAllowed() {
		WDate.formatToRegExp("dd MM yyy");
	}

	@Test(expected = eu.webtoolkit.jwt.WException.class)
	public void testFormatToRegExpBadFormattingTooMuchY() {
		WDate.formatToRegExp("dd MM yyyyy");
	}

	@Test(expected = eu.webtoolkit.jwt.WException.class)
	public void testFormatToRegExpBadFormattingMonthAllowed() {
		WDate.formatToRegExp("dd MMM yyyy");
	}

	@Test(expected = eu.webtoolkit.jwt.WException.class)
	public void testFormatToRegExpBadFormattingDayAllowed() {
		WDate.formatToRegExp("ddddd MM yyyy");
	}
}
