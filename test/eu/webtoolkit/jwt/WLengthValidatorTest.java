package eu.webtoolkit.jwt;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class WLengthValidatorTest {
	@Before
	public void createApplication() {
		WTestEnvironment env = new WTestEnvironment(new Configuration());
		new WApplication(env);
	}

	@Test
	public void testInvalidTooShortText_StandardText() {
		WLengthValidator validator = new WLengthValidator();
		validator.setMinimumLength(12);

		WString tooShort1 = validator.getInvalidTooShortText();

		validator.setMinimumLength(15);

		WString tooShort2 = validator.getInvalidTooShortText();

		assertNotSame(tooShort1, tooShort2);

		assertEquals(1, tooShort1.getArgs().size());
		assertEquals(1, tooShort2.getArgs().size());

		assertEquals(tooShort1.toString(),
				"The input must be at least 12 characters");
		assertEquals(tooShort2.toString(),
				"The input must be at least 15 characters");
	}

	@Test
	public void testInvalidTooShortText_CustomText() {
		WLengthValidator validator = new WLengthValidator();
		validator.setMinimumLength(12);
		validator.setMaximumLength(20);

		final var tooShort = new WString("Too short {1} {2}");
		validator.setInvalidTooShortText(tooShort);

		WString tooShort1 = validator.getInvalidTooShortText();

		validator.setMinimumLength(15);

		WString tooShort2 = validator.getInvalidTooShortText();

		assertNotSame(tooShort, tooShort1);
		assertNotSame(tooShort1, tooShort2);
		assertNotSame(tooShort, tooShort2);

		assertEquals(0, tooShort.getArgs().size());
		assertEquals(2, tooShort1.getArgs().size());
		assertEquals(2, tooShort2.getArgs().size());

		assertEquals(tooShort1.toString(), "Too short 12 20");
		assertEquals(tooShort2.toString(), "Too short 15 20");
	}

	@Test
	public void testInvalidTooLongText_StandardText() {
		WLengthValidator validator = new WLengthValidator();
		validator.setMaximumLength(12);

		WString tooLong1 = validator.getInvalidTooLongText();

		validator.setMaximumLength(15);

		WString tooLong2 = validator.getInvalidTooLongText();

		assertNotSame(tooLong1, tooLong2);

		assertEquals(1, tooLong1.getArgs().size());
		assertEquals(1, tooLong2.getArgs().size());

		assertEquals(tooLong1.toString(),
				"The input must be no more than 12 characters");
		assertEquals(tooLong2.toString(),
				"The input must be no more than 15 characters");
	}

	@Test
	public void testInvalidTooLongText_CustomText() {
		WLengthValidator validator = new WLengthValidator();
		validator.setMinimumLength(12);
		validator.setMaximumLength(20);

		final var tooLong = new WString("Too long {1} {2}");
		validator.setInvalidTooLongText(tooLong);

		WString tooLong1 = validator.getInvalidTooLongText();

		validator.setMaximumLength(25);

		WString tooLong2 = validator.getInvalidTooLongText();

		assertNotSame(tooLong, tooLong1);
		assertNotSame(tooLong1, tooLong2);
		assertNotSame(tooLong, tooLong2);

		assertEquals(0, tooLong.getArgs().size());
		assertEquals(2, tooLong1.getArgs().size());
		assertEquals(2, tooLong2.getArgs().size());

		assertEquals(tooLong1.toString(), "Too long 12 20");
		assertEquals(tooLong2.toString(), "Too long 12 25");
	}
}