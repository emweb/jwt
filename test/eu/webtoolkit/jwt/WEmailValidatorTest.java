package eu.webtoolkit.jwt;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class WEmailValidatorTest {
	private static final String EMAIL_ADDRESS_PATTERN = ".*@example[.]com";

	private static class TestCase {
		enum Flag {
			Single,
			Multiple,
			Pattern
		}

		TestCase(final String input, final Flag...flags) {
			this.input = input;
			if (flags.length > 0) {
				this.flags = EnumSet.copyOf(Arrays.asList(flags));
			} else {
				this.flags = EnumSet.noneOf(Flag.class);
			}
		}

		String input;
		EnumSet<Flag> flags;
	}

	private List<TestCase> cases = Arrays.asList(new TestCase[] {
		new TestCase("jos@example.com", TestCase.Flag.Single, TestCase.Flag.Multiple, TestCase.Flag.Pattern),
		new TestCase("jos@example.net", TestCase.Flag.Single, TestCase.Flag.Multiple),
		new TestCase("jos@example.com.be", TestCase.Flag.Single, TestCase.Flag.Multiple),
		new TestCase("jos@example.com,jos.bosmans@example.com", TestCase.Flag.Multiple, TestCase.Flag.Pattern),
		new TestCase("jos@example.com,jos.bosmans@example.net", TestCase.Flag.Multiple),
		new TestCase("greg"),
		new TestCase("@foo"),
		new TestCase("foo@"),
		new TestCase("jos.bosmans@example.com", TestCase.Flag.Single, TestCase.Flag.Multiple, TestCase.Flag.Pattern),
		new TestCase("jos@jos@example.com"),
		new TestCase("jos@jos@example.net"),
		new TestCase("jos@example.com,,jos.bosmans@example.com"),
		new TestCase("me@example", TestCase.Flag.Single, TestCase.Flag.Multiple),
		new TestCase("me@example.org", TestCase.Flag.Single, TestCase.Flag.Multiple),
		new TestCase("me@example.org,you@example.org", TestCase.Flag.Multiple),
		new TestCase("me@example.org, you@example.org"),
		new TestCase("me@example.org,you@example.org, us@example.org"),
		new TestCase(","),
		new TestCase("me"),
		new TestCase("me@example.org you@example.org")
	});

	@Before
	public void createApplication() {
		final var env = new WTestEnvironment(new Configuration());
		new WApplication(env);
	}

	private void testEmpty(final WValidator validator) {
		final var wasMandatory = validator.isMandatory();

		validator.setMandatory(false);
		{
			final var result = validator.validate("");
			assertTrue(result.getMessage().isEmpty());
			assertEquals(result.getState(), ValidationState.Valid);
		}

		validator.setMandatory(true);
		{
			final var result = validator.validate("");
			assertEquals(result.getMessage(), "This field cannot be empty");
			assertEquals(result.getState(), ValidationState.InvalidEmpty);
		}

		validator.setMandatory(wasMandatory);
	}

	@Test
	public void testValidateOne() {
		final var validator = new WEmailValidator();

		for (final var testCase : cases) {
			final var result = validator.validate(testCase.input);
			if (testCase.flags.contains(TestCase.Flag.Single)) {
				assertTrue(result.getMessage().isEmpty());
				assertEquals(ValidationState.Valid, result.getState());
			} else {
				assertEquals("Must be a valid email address", result.getMessage().toString());
				assertEquals(ValidationState.Invalid, result.getState());
			}
		}

		testEmpty(validator);
	}

	@Test
	public void testValidateOne_withPattern() {
		final var validator = new WEmailValidator();
		validator.setPattern(EMAIL_ADDRESS_PATTERN);

		for (final var testCase : cases) {
			final var result = validator.validate(testCase.input);
			if (testCase.flags.containsAll(List.of(TestCase.Flag.Single, TestCase.Flag.Pattern))) {
				assertTrue(result.getMessage().isEmpty());
				assertEquals(ValidationState.Valid, result.getState());
			} else {
				assertEquals("Must be an email address matching the pattern '" + EMAIL_ADDRESS_PATTERN + "'",
						result.getMessage().toString());
				assertEquals(ValidationState.Invalid, result.getState());
			}
		}

		testEmpty(validator);
	}

	@Test
	public void testValidateMultiple() {
		final var validator = new WEmailValidator();
		validator.setMultiple(true);

		for (final var testCase : cases) {
			final var result = validator.validate(testCase.input);
			if (testCase.flags.contains(TestCase.Flag.Multiple)) {
				assertTrue(result.getMessage().isEmpty());
				assertEquals(ValidationState.Valid, result.getState());
			} else {
				assertEquals("Must be a comma-separated list of email addresses", result.getMessage().toString());
				assertEquals(ValidationState.Invalid, result.getState());
			}
		}

		testEmpty(validator);
	}

	@Test
	public void testValidateMultiple_withPattern() {
		final var validator = new WEmailValidator();
		validator.setMultiple(true);
		validator.setPattern(EMAIL_ADDRESS_PATTERN);

		for (final var testCase : cases) {
			final var result = validator.validate(testCase.input);
			if (testCase.flags.containsAll(List.of(TestCase.Flag.Multiple, TestCase.Flag.Pattern))) {
				assertTrue(result.getMessage().isEmpty());
				assertEquals(ValidationState.Valid, result.getState());
			} else {
				assertEquals("Must be a comma-separated list of email addresses " +
			                 "matching the pattern '" + EMAIL_ADDRESS_PATTERN + "'",
			                 result.getMessage().toString());
				assertEquals(ValidationState.Invalid, result.getState());
			}
		}

		testEmpty(validator);
	}
}
