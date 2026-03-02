package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.WString;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class XSSFilterTest {
	@Test
	public void filterXSSRegularContentTest() {
		WString input = new WString("<p>This is normal content</p>");
		WString expected = new WString("<p>This is normal content</p>");
		assertTrue(XSSFilter.removeScript(input));
		assertEquals(input, expected);
  }

	@Test
	public void filterXSSScriptTestString() {
		String input = "<script>doSomethingMalicious()</script>";
		String expected = "<script>doSomethingMalicious()</script>";
		assertTrue(XSSFilter.removeScript(input));
		assertEquals(input, expected);
  }

	@Test
	public void filterXSSScriptTestWString() {
		WString input = new WString("<script>doSomethingMalicious()</script>");
		WString expected = new WString("");
		assertTrue(XSSFilter.removeScript(input));
		assertEquals(input, expected);
  }

	@Test
	public void filterXSSHTMLBasicEntityEncodeTest() {
		assertTrue(XSSFilter.removeScript("<p>&amp;&gt;&nbsp;</p>"));
  }

	@Test
	public void filterXSSHTMLNumberedEntityEncodeTest() {
		assertTrue(XSSFilter.removeScript("<p>&#128293;</p>"));
  }

	@Test
	public void filterXSSUnicodeTest1() {
		assertTrue(XSSFilter.removeScript("<p>Content: 🔥</p>"));
  }

	@Test
	public void filterXSSUnicodeTest2() {
		assertTrue(XSSFilter.removeScript(new WString("<p>Content: 🔥</p>")));
  }
}

