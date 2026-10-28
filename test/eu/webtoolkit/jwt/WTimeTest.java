package eu.webtoolkit.jwt;

import static org.junit.Assert.*;
import org.junit.Test;

public class WTimeTest {
	@Test
	public void testFormatToRegExpShort() {
		WTime.RegExpInfo timeInfo = WTime.formatToRegExp("H:m:s.S");

		assertEquals("(0|[1-9]|1[0-9]|2[0-3]):(0|[1-5]?[0-9]):(0|[1-5]?[0-9])\\.(0|[1-9][0-9]{0,2})", timeInfo.regexp);
		assertEquals("return parseInt(results[1], 10);", timeInfo.hourGetJS);
		assertEquals("return parseInt(results[2], 10);", timeInfo.minuteGetJS);
		assertEquals("return parseInt(results[3], 10);", timeInfo.secGetJS);
		assertEquals("return parseInt(results[4], 10);", timeInfo.msecGetJS);
	}

	@Test
	public void testFormatToRegExpLong() {
		WTime.RegExpInfo timeInfo = WTime.formatToRegExp("HH:mm:ss.SSS");

		assertEquals("([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])\\.([0-9]{3})", timeInfo.regexp);
		assertEquals("return parseInt(results[1], 10);", timeInfo.hourGetJS);
		assertEquals("return parseInt(results[2], 10);", timeInfo.minuteGetJS);
		assertEquals("return parseInt(results[3], 10);", timeInfo.secGetJS);
		assertEquals("return parseInt(results[4], 10);", timeInfo.msecGetJS);
	}

	@Test
	public void testFormatToRegExpBoth() {
		WTime.RegExpInfo timeInfo = WTime.formatToRegExp("h:mm:s.SSS a");

		assertEquals("([1-9]|1[012]):([0-5][0-9]):(0|[1-5]?[0-9])\\.([0-9]{3}) ([AP]M)", timeInfo.regexp);
		assertEquals("return parseInt(results[1], 10);", timeInfo.hourGetJS);
		assertEquals("return parseInt(results[2], 10);", timeInfo.minuteGetJS);
		assertEquals("return parseInt(results[3], 10);", timeInfo.secGetJS);
		assertEquals("return parseInt(results[4], 10);", timeInfo.msecGetJS);
	}

	@Test
	public void testFormatToRegExpBadFormattingHourAllowed() {
		WTime.RegExpInfo timeInfo = WTime.formatToRegExp("HHHH mm ss a");

		assertEquals("([0-1][0-9]|2[0-3])([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9]) ([AP]M)", timeInfo.regexp);
	}

	@Test
	public void testFormatToRegExpBadFormattingMinuteAllowed() {
		WTime.RegExpInfo timeInfo = WTime.formatToRegExp("HH mmm ss a");

		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9])(0|[1-5]?[0-9]) ([0-5][0-9]) ([AP]M)", timeInfo.regexp);
	}

	@Test
	public void testFormatToRegExpBadFormattingSecondAllowed() {
		WTime.RegExpInfo timeInfo = WTime.formatToRegExp("HH mm sssss a");

		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9])([0-5][0-9])(0|[1-5]?[0-9]) ([AP]M)", timeInfo.regexp);
	}

	@Test
	public void testFormatToRegExpBadFormattingMillisecondAllowed() {
		WTime.RegExpInfo timeInfo = WTime.formatToRegExp("HH mm ss.SSSS a");

		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9])\\.([0-9]{3})(0|[1-9][0-9]{0,2}) ([AP]M)", timeInfo.regexp);

		timeInfo = WTime.formatToRegExp("HH mm ss.SS a");
		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9])\\. ([AP]M)", timeInfo.regexp);

		timeInfo = WTime.formatToRegExp("HH mm ss.SSSSS a");
		assertEquals("([0-1][0-9]|2[0-3]) ([0-5][0-9]) ([0-5][0-9])\\.([0-9]{3}) ([AP]M)", timeInfo.regexp);
	}

}
