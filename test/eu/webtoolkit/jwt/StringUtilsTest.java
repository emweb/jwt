
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
}
