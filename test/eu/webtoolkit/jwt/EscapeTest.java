package eu.webtoolkit.jwt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EscapeTest {
	@Test
    public void test1() {
    	final String toEscape = "Support & training";
    	final String toUnescape = "Support &amp; training";
    	
    	assertEquals(toUnescape, WWebWidget.escapeText(toEscape));
    	assertEquals(toEscape, WWebWidget.unescapeText(toUnescape));
    }
	
	@Test
	public void test2() {
		final String toUnescape = "&#34;";
		
		assertEquals("\"", WWebWidget.unescapeText(toUnescape));
	}
}
