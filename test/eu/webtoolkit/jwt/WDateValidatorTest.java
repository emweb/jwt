package eu.webtoolkit.jwt;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class WDateValidatorTest {
	@Before
	public void createApplication() {
		WTestEnvironment env = new WTestEnvironment(new Configuration());
		new WApplication(env);
	}

	@Test
	public void testInvalidNotADateText_StandardText() {
		WDateValidator validator = new WDateValidator();
		validator.setFormat("yyyy-MM-dd");

		WString notADate1 = validator.getInvalidNotADateText();

		validator.setFormat("EEE, MMM d, yyyy");

		WString notADate2 = validator.getInvalidNotADateText();

		assertNotSame(notADate1, notADate2);

		assertEquals(1, notADate1.getArgs().size());
		assertEquals(1, notADate1.getArgs().size());

		assertEquals(notADate1.toString(),
				"Must be a date in the format 'yyyy-MM-dd'");
		assertEquals(notADate2.toString(),
				"Must be a date in the format 'EEE, MMM d, yyyy'");
	}

	@Test
	public void testInvalidNotADateText_CustomText() {
		WDateValidator validator = new WDateValidator();
		validator.setFormat("yyyy-MM-dd");

		final var notADate = new WString("Invalid {1}");
		validator.setInvalidNotADateText(notADate);

		WString notADate1 = validator.getInvalidNotADateText();

		validator.setFormat("EEE, MMM d, yyyy");

		WString notADate2 = validator.getInvalidNotADateText();

		assertNotSame(notADate, notADate1);
		assertNotSame(notADate1, notADate2);
		assertNotSame(notADate, notADate2);

		assertEquals(0, notADate.getArgs().size());
		assertEquals(1, notADate1.getArgs().size());
		assertEquals(1, notADate2.getArgs().size());

		assertEquals(notADate1.toString(), "Invalid yyyy-MM-dd");
		assertEquals(notADate2.toString(), "Invalid EEE, MMM d, yyyy");
	}

	@Test
	public void testInvalidTooEarlyText_StandardText() {
		WDateValidator validator = new WDateValidator();
		validator.setBottom(new WDate(2000, 1, 1));

		WString tooEarly1 = validator.getInvalidTooEarlyText();

		validator.setBottom(new WDate(2001, 1, 1));

		WString tooEarly2 = validator.getInvalidTooEarlyText();

		assertNotSame(tooEarly1, tooEarly2);

		assertEquals(1, tooEarly1.getArgs().size());
		assertEquals(1, tooEarly2.getArgs().size());

		assertEquals(tooEarly1.toString(), "The date must be after 2000-01-01");
		assertEquals(tooEarly2.toString(), "The date must be after 2001-01-01");
	}

	@Test
	public void testInvalidTooEarlyText_CustomText() {
		WDateValidator validator = new WDateValidator();
		validator.setBottom(new WDate(2000, 1, 1));
		validator.setTop(new WDate(2000, 2, 1));

		WString tooEarly = new WString("Too early {1} {2}");
		validator.setInvalidTooEarlyText(tooEarly);

		WString tooEarly1 = validator.getInvalidTooEarlyText();

		validator.setBottom(new WDate(2001, 1, 1));
		validator.setTop(new WDate(2001, 2, 1));

		WString tooEarly2 = validator.getInvalidTooEarlyText();

		assertNotSame(tooEarly, tooEarly1);
		assertNotSame(tooEarly1, tooEarly2);
		assertNotSame(tooEarly, tooEarly2);

		assertEquals(0, tooEarly.getArgs().size());
		assertEquals(2, tooEarly1.getArgs().size());
		assertEquals(2, tooEarly2.getArgs().size());

		assertEquals(tooEarly.toString(), "Too early {1} {2}");
		assertEquals(tooEarly1.toString(), "Too early 2000-01-01 2000-02-01");
		assertEquals(tooEarly2.toString(), "Too early 2001-01-01 2001-02-01");
	}

	@Test
	public void testInvalidTooLateText_StandardText() {
		WDateValidator validator = new WDateValidator();
		validator.setTop(new WDate(2000, 2, 1));

		WString tooLate1 = validator.getInvalidTooLateText();

		validator.setTop(new WDate(2001, 2, 1));

		WString tooLate2 = validator.getInvalidTooLateText();

		assertNotSame(tooLate1, tooLate2);

		assertEquals(1, tooLate1.getArgs().size());
		assertEquals(1, tooLate2.getArgs().size());

		assertEquals(tooLate1.toString(), "The date must be before 2000-02-01");
		assertEquals(tooLate2.toString(), "The date must be before 2001-02-01");
	}

	@Test
	public void testInvalidTooLateText_CustomText() {
		WDateValidator validator = new WDateValidator();
		validator.setBottom(new WDate(2000, 1, 1));
		validator.setTop(new WDate(2000, 2, 1));

		WString tooLate = new WString("Too late {1} {2}");
		validator.setInvalidTooLateText(tooLate);

		WString tooLate1 = validator.getInvalidTooLateText();

		validator.setBottom(new WDate(2001, 1, 1));
		validator.setTop(new WDate(2001, 2, 1));

		WString tooLate2 = validator.getInvalidTooLateText();

		assertNotSame(tooLate, tooLate1);
		assertNotSame(tooLate1, tooLate2);
		assertNotSame(tooLate, tooLate2);

		assertEquals(0, tooLate.getArgs().size());
		assertEquals(2, tooLate1.getArgs().size());
		assertEquals(2, tooLate2.getArgs().size());

		assertEquals(tooLate.toString(), "Too late {1} {2}");
		assertEquals(tooLate1.toString(), "Too late 2000-01-01 2000-02-01");
		assertEquals(tooLate2.toString(), "Too late 2001-01-01 2001-02-01");
	}
}