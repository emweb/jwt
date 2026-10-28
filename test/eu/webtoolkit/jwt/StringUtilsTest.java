
package eu.webtoolkit.jwt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class StringUtilsTest {
	private static class TestCase {
		TestCase(String input, String separators, List<String> uncompressed, List<String> compressed) {
			this.input = input;
			this.separators = separators;
			this.uncompressed = uncompressed;
			this.compressed = compressed;
		}

		String input;
		String separators;
		List<String> uncompressed;
		List<String> compressed;
	}

	private static List<TestCase> testCases = List.of(
		new TestCase("", ",", List.of(""), List.of("")),
		new TestCase(",", ",", List.of("", ""), List.of("", "")),
		new TestCase(",,", ",", List.of("", "", ""), List.of("", "")),
		new TestCase("a,,b,c", ",", List.of("a", "", "b", "c"), List.of("a", "b", "c")),
		new TestCase(",,b,c", ",", List.of("", "", "b", "c"), List.of("", "b", "c")),
		new TestCase("a,b,c,,", ",", List.of("a", "b", "c", "", ""), List.of("a", "b", "c", "")),
		new TestCase("foo,,,,bar,baz,,", ",", List.of("foo", "", "", "", "bar", "baz", "", ""), List.of("foo", "bar", "baz", "")),
		new TestCase("multi:ple,:separators", ":,", List.of("multi", "ple", "", "separators"), List.of("multi", "ple", "separators"))
	);

	@Test
	public void testSplit() {
		for (final var testCase : testCases) {
			final var list = new ArrayList<String>();
			StringUtils.split(list, testCase.input, testCase.separators, false);
			assertEquals(testCase.uncompressed, list);
			list.clear();
			StringUtils.split(list, testCase.input, testCase.separators, true);
			assertEquals(testCase.compressed, list);
		}
	}

	@Test
	public void testErase() {
		assertEquals("", StringUtils.erase("0123456789", 0));
		assertEquals("012", StringUtils.erase("0123456789", 3));
		assertEquals("012345678", StringUtils.erase("0123456789", 9));
		assertEquals("0123456789", StringUtils.erase("0123456789", 10));

		assertEquals("0123456789", StringUtils.erase("0123456789", 0, 0));
		assertEquals("3456789", StringUtils.erase("0123456789", 0, 3));
		assertEquals("9", StringUtils.erase("0123456789", 0, 9));
		assertEquals("", StringUtils.erase("0123456789", 0, 10));
		assertEquals("", StringUtils.erase("0123456789", 0, 11));
		assertEquals("0156789", StringUtils.erase("0123456789", 2, 3));
		assertEquals("01", StringUtils.erase("0123456789", 2, 8));
		assertEquals("01", StringUtils.erase("0123456789", 2, 9));
		assertEquals("012345678", StringUtils.erase("0123456789", 9, 5));
		assertEquals("0123456789", StringUtils.erase("0123456789", 10, 5));
	}

	@Test
	public void testFindLastOf() {
		assertEquals(-1, StringUtils.findLastOf("abcdedcba", "."));
		assertEquals(8, StringUtils.findLastOf("abcdedcba", "a"));
		assertEquals(7, StringUtils.findLastOf("abcdedcba", "b"));
		assertEquals(4, StringUtils.findLastOf("abcdedcba", "e"));
		assertEquals(6, StringUtils.findLastOf("abcdedcba", "ce"));
		assertEquals(6, StringUtils.findLastOf("abcdedcba", "ec"));
		assertEquals(7, StringUtils.findLastOf("abcdedcba", ".-/&b=*"));
		assertEquals(-1, StringUtils.findLastOf("abcdedcba", ".-/&=*"));

		assertEquals(-1, StringUtils.findLastOf("abcdedcba", ".", 5));
		assertEquals(8, StringUtils.findLastOf("abcdedcba", "a", 8));
		assertEquals(0, StringUtils.findLastOf("abcdedcba", "a", 7));
		assertEquals(0, StringUtils.findLastOf("abcdedcba", "a", 0));

		assertEquals(7, StringUtils.findLastOf("abcdedcba", "b", 7));
		assertEquals(1, StringUtils.findLastOf("abcdedcba", "b", 6));
		assertEquals(1, StringUtils.findLastOf("abcdedcba", "b", 1));
		assertEquals(-1, StringUtils.findLastOf("abcdedcba", "b", 0));

		assertEquals(4, StringUtils.findLastOf("abcdedcba", "e", 10));
		assertEquals(6, StringUtils.findLastOf("abcdedcba", "ec", 10));
		assertEquals(6, StringUtils.findLastOf("abcdedcba", "ce", 10));
		assertEquals(4, StringUtils.findLastOf("abcdedcba", "e", 6));
		assertEquals(6, StringUtils.findLastOf("abcdedcba", "ec", 6));
		assertEquals(6, StringUtils.findLastOf("abcdedcba", "ce", 6));
		assertEquals(4, StringUtils.findLastOf("abcdedcba", "e", 5));
		assertEquals(4, StringUtils.findLastOf("abcdedcba", "ce", 5));
		assertEquals(4, StringUtils.findLastOf("abcdedcba", "ec", 5));
		assertEquals(4, StringUtils.findLastOf("abcdedcba", "e", 4));
		assertEquals(4, StringUtils.findLastOf("abcdedcba", "ce", 4));
		assertEquals(4, StringUtils.findLastOf("abcdedcba", "ec", 4));
		assertEquals(-1, StringUtils.findLastOf("abcdedcba", "e", 3));
		assertEquals(2, StringUtils.findLastOf("abcdedcba", "ce", 3));
		assertEquals(2, StringUtils.findLastOf("abcdedcba", "ec", 3));
		assertEquals(2, StringUtils.findLastOf("abcdedcba", "ce", 2));
		assertEquals(2, StringUtils.findLastOf("abcdedcba", "ec", 2));
		assertEquals(-1, StringUtils.findLastOf("abcdedcba", "ce", 1));
		assertEquals(-1, StringUtils.findLastOf("abcdedcba", "ec", 1));

		assertEquals(-1, StringUtils.findLastOf("abcdedcba", ".-/&=*", 5));
	}

	@Test
	public void testFindFirstOf() {
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "."));
		assertEquals(0, StringUtils.findFirstOf("abcdedcba", "a"));
		assertEquals(1, StringUtils.findFirstOf("abcdedcba", "b"));
		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "e"));
		assertEquals(2, StringUtils.findFirstOf("abcdedcba", "ce"));
		assertEquals(2, StringUtils.findFirstOf("abcdedcba", "ec"));
		assertEquals(1, StringUtils.findFirstOf("abcdedcba", ".-/&b=*"));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", ".-/&=*"));

		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", ".", 5));
		assertEquals(0, StringUtils.findFirstOf("abcdedcba", "a", 0));
		assertEquals(8, StringUtils.findFirstOf("abcdedcba", "a", 1));
		assertEquals(8, StringUtils.findFirstOf("abcdedcba", "a", 8));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "a", 9));

		assertEquals(1, StringUtils.findFirstOf("abcdedcba", "b", 0));
		assertEquals(1, StringUtils.findFirstOf("abcdedcba", "b", 1));
		assertEquals(7, StringUtils.findFirstOf("abcdedcba", "b", 2));
		assertEquals(7, StringUtils.findFirstOf("abcdedcba", "b", 7));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "b", 8));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "b", 10));

		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "e", 0));
		assertEquals(2, StringUtils.findFirstOf("abcdedcba", "ce", 0));
		assertEquals(2, StringUtils.findFirstOf("abcdedcba", "ec", 0));
		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "e", 2));
		assertEquals(2, StringUtils.findFirstOf("abcdedcba", "ce", 2));
		assertEquals(2, StringUtils.findFirstOf("abcdedcba", "ec", 2));
		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "e", 3));
		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "ce", 3));
		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "ec", 3));
		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "e", 4));
		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "ce", 4));
		assertEquals(4, StringUtils.findFirstOf("abcdedcba", "ec", 4));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "e", 5));
		assertEquals(6, StringUtils.findFirstOf("abcdedcba", "ce", 5));
		assertEquals(6, StringUtils.findFirstOf("abcdedcba", "ec", 5));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "e", 6));
		assertEquals(6, StringUtils.findFirstOf("abcdedcba", "ce", 6));
		assertEquals(6, StringUtils.findFirstOf("abcdedcba", "ec", 6));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "e", 7));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "ce", 7));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", "ec", 7));
		assertEquals(-1, StringUtils.findFirstOf("abcdedcba", ".-/&=*", 5));
	}
}
