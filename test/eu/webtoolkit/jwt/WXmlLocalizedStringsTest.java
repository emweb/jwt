package eu.webtoolkit.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

public class WXmlLocalizedStringsTest {
	@Test
	public void wtXmlTest() {
		final WXmlLocalizedStrings strings = new WXmlLocalizedStrings();
		strings.use("/eu/webtoolkit/jwt/wt");
		
		final Locale locale = new Locale("");
		LocalizedString result;
		
		result = strings.resolveKey(locale, "Wt.WDate.Monday");
		assertTrue(result.success);
		assertEquals(TextFormat.XHTML, result.format);
		assertEquals("Monday", result.value);
		
		result = strings.resolvePluralKey(locale, "Wt.WDateTime.seconds", 0);
		assertTrue(result.success);
		assertEquals(TextFormat.XHTML, result.format);
		assertEquals("{1} seconds", result.value);
		
		result = strings.resolvePluralKey(locale, "Wt.WDateTime.seconds", 1);
		assertTrue(result.success);
		assertEquals(TextFormat.XHTML, result.format);
		assertEquals("one second", result.value);
		
		result = strings.resolvePluralKey(locale, "Wt.WDateTime.seconds", 2);
		assertTrue(result.success);
		assertEquals(TextFormat.XHTML, result.format);
		assertEquals("{1} seconds", result.value);
	}
}
