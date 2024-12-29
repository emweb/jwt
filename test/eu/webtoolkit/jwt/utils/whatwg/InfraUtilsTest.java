package eu.webtoolkit.jwt.utils.whatwg;

import static eu.webtoolkit.jwt.utils.whatwg.InfraUtils.*;

import static org.junit.Assert.*;

import org.junit.Test;

public class InfraUtilsTest {
	@Test
	public void testStripNewLines() {
		assertEquals(stripNewlines("one\ntwo\nthree"), "onetwothree");
		assertEquals(
			stripNewlines("one   \ntwo \r\r three\n"),
			"one   two  three");
		assertEquals(stripNewlines("one\r\ntwo\r\n"), "onetwo");
		assertEquals(stripNewlines("\n\r\n\r\n"), "");
		assertEquals(stripNewlines(""), "");
	}

	@Test
	public void testTrim() {
		assertEquals(trim("    \t\r\f\n  \t\r  "), "");
		assertEquals(trim("   this is a string    "), "this is a string");
		assertEquals(trim(""), "");
		assertEquals(trim("    \fone two"), "one two");
		assertEquals(trim("one two  \f\r\n "), "one two");
	}
}
