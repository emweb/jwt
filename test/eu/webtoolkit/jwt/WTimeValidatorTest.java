package eu.webtoolkit.jwt;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class WTimeValidatorTest {
	@Before
	public void createApplication() {
		WTestEnvironment env = new WTestEnvironment(new Configuration());
		new WApplication(env);
	}

	@Test
	public void testInvalidNotATimeText_StandardText() {
		WTimeValidator validator = new WTimeValidator();
		validator.setFormat("HH:mm:ss");

		WString notATime1 = validator.getInvalidNotATimeText();

		validator.setFormat("HH:mm");

		WString notATime2 = validator.getInvalidNotATimeText();

		assertNotSame(notATime1, notATime2);

		assertEquals(1, notATime1.getArgs().size());
		assertEquals(1, notATime2.getArgs().size());

		assertEquals(notATime1.toString(),
				"Must be a time in the format 'HH:mm:ss'");
		assertEquals(notATime2.toString(),
				"Must be a time in the format 'HH:mm'");
	}

	@Test
	public void testInvalidNotATimeText_CustomText() {
		WTimeValidator validator = new WTimeValidator();
		validator.setFormat("HH:mm:ss");

		final var notATime = new WString("Invalid {1}");
		validator.setInvalidNotATimeText(notATime);

		WString notATime1 = validator.getInvalidNotATimeText();

		validator.setFormat("HH:mm");

		WString notATime2 = validator.getInvalidNotATimeText();

		assertNotSame(notATime, notATime1);
		assertNotSame(notATime1, notATime2);
		assertNotSame(notATime, notATime2);

		assertEquals(0, notATime.getArgs().size());
		assertEquals(1, notATime1.getArgs().size());
		assertEquals(1, notATime2.getArgs().size());

		assertEquals(notATime1.toString(), "Invalid HH:mm:ss");
		assertEquals(notATime2.toString(), "Invalid HH:mm");
	}

	@Test
	public void testInvalidTooEarlyText_StandardText() {
		WTimeValidator validator = new WTimeValidator();
		validator.setBottom(new WTime(1, 10, 10));

		WString tooEarly1 = validator.getInvalidTooEarlyText();

		validator.setBottom(new WTime(3, 10, 10));

		WString tooEarly2 = validator.getInvalidTooEarlyText();

		assertNotSame(tooEarly1, tooEarly2);

		assertEquals(1, tooEarly1.getArgs().size());
		assertEquals(1, tooEarly2.getArgs().size());

		assertEquals(tooEarly1.toString(), "The time must be after 01:10:10");
		assertEquals(tooEarly2.toString(), "The time must be after 03:10:10");
	}

	@Test
	public void testInvalidTooEarlyText_CustomText() {
		WTimeValidator validator = new WTimeValidator();
		validator.setBottom(new WTime(1, 10, 10));
		validator.setTop(new WTime(2, 10, 10));

		WString tooEarly = new WString("Too early {1} {2}");
		validator.setInvalidTooEarlyText(tooEarly);

		WString tooEarly1 = validator.getInvalidTooEarlyText();

		validator.setBottom(new WTime(3, 10, 10));
		validator.setTop(new WTime(4, 10, 10));

		WString tooEarly2 = validator.getInvalidTooEarlyText();

		assertNotSame(tooEarly, tooEarly1);
		assertNotSame(tooEarly1, tooEarly2);
		assertNotSame(tooEarly, tooEarly2);

		assertEquals(0, tooEarly.getArgs().size());
		assertEquals(2, tooEarly1.getArgs().size());
		assertEquals(2, tooEarly2.getArgs().size());

		assertEquals(tooEarly.toString(), "Too early {1} {2}");
		assertEquals(tooEarly1.toString(), "Too early 01:10:10 02:10:10");
		assertEquals(tooEarly2.toString(), "Too early 03:10:10 04:10:10");
	}

	@Test
	public void testInvalidTooLateText_StandardText() {
		WTimeValidator validator = new WTimeValidator();
		validator.setTop(new WTime(2, 10, 10));

		WString tooLate1 = validator.getInvalidTooLateText();

		validator.setTop(new WTime(4, 10, 10));

		WString tooLate2 = validator.getInvalidTooLateText();

		assertNotSame(tooLate1, tooLate2);

		assertEquals(1, tooLate1.getArgs().size());
		assertEquals(1, tooLate2.getArgs().size());

		assertEquals(tooLate1.toString(), "The time must be before 02:10:10");
		assertEquals(tooLate2.toString(), "The time must be before 04:10:10");
	}

	@Test
	public void testInvalidTooLateText_CustomText() {
		WTimeValidator validator = new WTimeValidator();
		validator.setBottom(new WTime(1, 10, 10));
		validator.setTop(new WTime(2, 10, 10));

		WString tooLate = new WString("Too late {1} {2}");
		validator.setInvalidTooLateText(tooLate);

		WString tooLate1 = validator.getInvalidTooLateText();

		validator.setBottom(new WTime(3, 10, 10));
		validator.setTop(new WTime(4, 10, 10));

		WString tooLate2 = validator.getInvalidTooLateText();

		assertNotSame(tooLate, tooLate1);
		assertNotSame(tooLate1, tooLate2);
		assertNotSame(tooLate, tooLate2);

		assertEquals(0, tooLate.getArgs().size());
		assertEquals(2, tooLate1.getArgs().size());
		assertEquals(2, tooLate2.getArgs().size());

		assertEquals(tooLate.toString(), "Too late {1} {2}");
		assertEquals(tooLate1.toString(), "Too late 01:10:10 02:10:10");
		assertEquals(tooLate2.toString(), "Too late 03:10:10 04:10:10");
	}
}
