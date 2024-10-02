package eu.webtoolkit.jwt.auth.mfa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.Duration;
import org.junit.Test;

import eu.webtoolkit.jwt.WException;
import eu.webtoolkit.jwt.Utils;

public class TotpTest {
  @Test(expected = WException.class)
  public void testKeyTooShort() {
    Totp.generateSecretKey(15);
  }

  @Test(expected = WException.class)
  public void testKeyTooLong() {
    Totp.generateSecretKey(257);
  }

  @Test
  public void testKeyBetweenBounds() {
    assertEquals(Totp.generateSecretKey(167).length(), 167);
  }

  @Test(expected = WException.class)
  public void testCodeTooShort() {
    Totp.generateCode("ABC", 5, Duration.parse("PT0s"));
  }

  @Test(expected = WException.class)
  public void testCodeTooLong() {
    Totp.generateCode("ABC", 17, Duration.parse("PT0s"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCodeEmptyKey() {
    Totp.generateCode("", 6, Duration.parse("PT0s"));
  }

  @Test
  public void testCodeReturn6Idempotent() {
    assertEquals("328482", Totp.generateCode("ABC", 6, Duration.parse("PT0s")));
    assertEquals("328482", Totp.generateCode("ABC", 6, Duration.parse("PT0s")));
  }

  @Test
  public void testCodeReturn6DifferentPeriod() {
    String code = Totp.generateCode("ABC", 6, Duration.parse("PT0s"));
    assertEquals("328482", code);

    String code2 = Totp.generateCode("ABC", 6, Duration.parse("PT31s"));
    assertNotEquals(code, code2);
  }

  @Test
  public void testCodeKey16return6() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IY======";
    // Time is 2022-01-01 10:10:10
    assertEquals("934872", Totp.generateCode(key, 6, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testCodeKey32Return6() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3A====";
    // Time is 2022-01-01 10:10:10
    assertEquals("769426", Totp.generateCode(key, 6, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testCodeKey64Return6() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3EMNJUM5QWIOCBKVCDERRZGZATGNRQGRSDMYJZKFYTQUDNJVGTCYQ=";
    // Time is 2022-01-01 10:10:10
    assertEquals("388951", Totp.generateCode(key, 6, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testCodeKey16return8() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IY======";
    // Time is 2022-01-01 10:10:10
    assertEquals("12934872", Totp.generateCode(key, 8, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testCodeKey32Return8() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3A====";
    // Time is 2022-01-01 10:10:10
    assertEquals("88769426", Totp.generateCode(key, 8, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testCodeKey64Return8() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3EMNJUM5QWIOCBKVCDERRZGZATGNRQGRSDMYJZKFYTQUDNJVGTCYQ=";
    // Time is 2022-01-01 10:10:10
    assertEquals("13388951", Totp.generateCode(key, 8, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testCodeKey16return16() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IY======";
    // Time is 2022-01-01 10:10:10
    assertEquals("0000000012934872", Totp.generateCode(key, 16, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testCodeKey32Return16() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3A====";
    // Time is 2022-01-01 10:10:10
    assertEquals("0000000188769426", Totp.generateCode(key, 16, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testCodeKey64Return16() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3EMNJUM5QWIOCBKVCDERRZGZATGNRQGRSDMYJZKFYTQUDNJVGTCYQ=";
    // Time is 2022-01-01 10:10:10
    assertEquals("0000000113388951", Totp.generateCode(key, 16, Duration.parse("PT1641031810s")));
  }

  @Test
  public void testValidateCodeKey16Code6() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IY======";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 6, time);

    assertEquals(true, Totp.validateCode(key, code, 6, time));
  }

  @Test
  public void testValidateCodeKey32Code6() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3A====";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 6, time);

    assertEquals(true, Totp.validateCode(key, code, 6, time));
  }

  @Test
  public void testValidateCodeKey64Code6() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3EMNJUM5QWIOCBKVCDERRZGZATGNRQGRSDMYJZKFYTQUDNJVGTCYQ=";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 6, time);

    assertEquals(true, Totp.validateCode(key, code, 6, time));
  }

  @Test
  public void testValidateCodeKey16Code8() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IY======";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 8, time);

    assertEquals(true, Totp.validateCode(key, code, 8, time));
  }

  @Test
  public void testValidateCodeKey32Code8() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3A====";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 8, time);

    assertEquals(true, Totp.validateCode(key, code, 8, time));
  }

  @Test
  public void testValidateCodeKey64Code8() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3EMNJUM5QWIOCBKVCDERRZGZATGNRQGRSDMYJZKFYTQUDNJVGTCYQ=";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 8, time);

    assertEquals(true, Totp.validateCode(key, code, 8, time));
  }

  @Test
  public void testValidateCodeKey16Code16() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IY======";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 16, time);

    assertEquals(true, Totp.validateCode(key, code, 16, time));
  }

  @Test
  public void testValidateCodeKey32Code16() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3A====";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 16, time);

    assertEquals(true, Totp.validateCode(key, code, 16, time));
  }

  @Test
  public void testValidateCodeKey64Code16() {
    String key = "GE2UINL2IQ4GMZZWGEZDGYL2IZTHMRRVI5UGQNSHOAYVAMJSOM3EMNJUM5QWIOCBKVCDERRZGZATGNRQGRSDMYJZKFYTQUDNJVGTCYQ=";
    // Time is 2022-01-01 10:10:10
    Duration time = Duration.parse("PT1641031810s");
    String code = Totp.generateCode(key, 16, time);

    assertEquals(true, Totp.validateCode(key, code, 16, time));
  }

  @Test
  public void testRFC6238() {
    String key = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
    Duration time = Duration.parse("PT59s");
    String code = Totp.generateCode(key, 8, time);

    assertEquals("94287082", code);

    time = Duration.parse("PT1111111109s");
    code = Totp.generateCode(key, 8, time);

    assertEquals("07081804", code);

    time = Duration.parse("PT1111111111s");
    code = Totp.generateCode(key, 8, time);

    assertEquals("14050471", code);

    time = Duration.parse("PT1234567890s");
    code = Totp.generateCode(key, 8, time);

    assertEquals("89005924", code);

    time = Duration.parse("PT2000000000s");
    code = Totp.generateCode(key, 8, time);

    assertEquals("69279037", code);

    time = Duration.parse("PT20000000000s");
    code = Totp.generateCode(key, 8, time);

    assertEquals("65353130", code);
  }
}

