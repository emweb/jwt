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
}
