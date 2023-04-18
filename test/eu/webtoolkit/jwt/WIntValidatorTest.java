package eu.webtoolkit.jwt;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class WIntValidatorTest {
	@Before
	public void createApplication() {
		WTestEnvironment env = new WTestEnvironment(new Configuration());
		new WApplication(env);
	}

	@Test
	public void testInvalidTooSmallText_StandardText() {
		WIntValidator validator = new WIntValidator();
		validator.setBottom(12);

		WString tooSmall1 = validator.getInvalidTooSmallText();

		validator.setBottom(15);

		WString tooSmall2 = validator.getInvalidTooSmallText();

		assertNotSame(tooSmall1, tooSmall2);

		assertEquals(1, tooSmall1.getArgs().size());
		assertEquals(1, tooSmall2.getArgs().size());

		assertEquals(tooSmall1.toString(), "The number must be larger than 12");
		assertEquals(tooSmall2.toString(), "The number must be larger than 15");
	}

	@Test
	public void testInvalidTooSmallText_CustomText() {
		WIntValidator validator = new WIntValidator();
		validator.setBottom(12);
		validator.setTop(20);

		final var tooSmall = new WString("Too small {1} {2}");
		validator.setInvalidTooSmallText(tooSmall);

		WString tooSmall1 = validator.getInvalidTooSmallText();

		validator.setBottom(15);

		WString tooSmall2 = validator.getInvalidTooSmallText();

		assertNotSame(tooSmall, tooSmall1);
		assertNotSame(tooSmall1, tooSmall2);
		assertNotSame(tooSmall, tooSmall2);

		assertEquals(0, tooSmall.getArgs().size());
		assertEquals(2, tooSmall1.getArgs().size());
		assertEquals(2, tooSmall2.getArgs().size());

		assertEquals(tooSmall1.toString(), "Too small 12 20");
		assertEquals(tooSmall2.toString(), "Too small 15 20");
	}

	@Test
	public void testInvalidTooLargeText_StandardText() {
		WIntValidator validator = new WIntValidator();
		validator.setTop(12);

		WString tooLarge1 = validator.getInvalidTooLargeText();

		validator.setTop(15);

		WString tooLarge2 = validator.getInvalidTooLargeText();

		assertNotSame(tooLarge1, tooLarge2);

		assertEquals(1, tooLarge1.getArgs().size());
		assertEquals(1, tooLarge2.getArgs().size());

		assertEquals(tooLarge1.toString(), "The number must be smaller than 12");
		assertEquals(tooLarge2.toString(), "The number must be smaller than 15");
	}

	@Test
	public void testInvalidTooLargeText_CustomText() {
		WIntValidator validator = new WIntValidator();
		validator.setBottom(12);
		validator.setTop(20);

		final var tooLarge = new WString("Too large {1} {2}");
		validator.setInvalidTooLargeText(tooLarge);

		WString tooLarge1 = validator.getInvalidTooLargeText();

		validator.setTop(25);

		WString tooLarge2 = validator.getInvalidTooLargeText();

		assertNotSame(tooLarge, tooLarge1);
		assertNotSame(tooLarge1, tooLarge2);
		assertNotSame(tooLarge, tooLarge2);

		assertEquals(0, tooLarge.getArgs().size());
		assertEquals(2, tooLarge1.getArgs().size());
		assertEquals(2, tooLarge2.getArgs().size());

		assertEquals(tooLarge1.toString(), "Too large 12 20");
		assertEquals(tooLarge2.toString(), "Too large 12 25");
	}
}