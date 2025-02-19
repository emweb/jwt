/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth.mfa;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility class containing functions for TOTP functionality. */
public class Totp {
  private static Logger logger = LoggerFactory.getLogger(Totp.class);

  private Totp() {}
  /**
   * Generate a secret key, for Multi-Factor Authentication.
   *
   * <p>This will generate a base32-encoded string, of <code>length</code>. This will only contain
   * characters from [A-Z2-7]. The generated string is created securely, and sufficiently random for
   * cryptographic purposes.
   *
   * <p>This string returned by this function can be used for a user as their shared secret to
   * generate and verify <a href="https://datatracker.ietf.org/doc/html/rfc6238">TOTP</a> codes.
   *
   * <p>Secret keys with length between 16 and 256 are allowed. By default the length will be 32.
   *
   * <p>
   *
   * @exception {@link WException} if the <code>length</code> specified isn&apos;t within the range
   *     [16, 256].
   */
  public static String generateSecretKey(int length) {
    if (length < MINIMUM_SECRET_LENGTH || length > MAXIMUM_SECRET_LENGTH) {
      throw new WException(
          "Wt::Auth::Mfa::generateSecretKey: the key length should be between "
              + String.valueOf(MINIMUM_SECRET_LENGTH)
              + " and "
              + String.valueOf(MAXIMUM_SECRET_LENGTH));
    }
    String generatedId = MathUtils.randomId(length);
    String encoded = Utils.base32Encode(generatedId);
    return encoded.substring(0, 0 + length);
  }
  /**
   * Generate a secret key, for Multi-Factor Authentication.
   *
   * <p>Returns {@link #generateSecretKey(int length) generateSecretKey(32)}
   */
  public static final String generateSecretKey() {
    return generateSecretKey(32);
  }
  /**
   * Generates a TOTP (Time-Based One-Time Password) code.
   *
   * <p>This code is generated from a secret <code>key</code>, at the specified <code>time</code>.
   * The code will be of length <code>codeDigits</code>.
   *
   * <p>The <code>key</code> should be a base32-encoded string, with a length between 16 and 256.
   * The <code>codeDigits</code> parameter should be at least 6 characters, and at most be 16
   * characters long. Supplying a <code>codeDigits</code> outside of this boundary will result in an
   * exception being thrown.
   *
   * <p>The specified time will be the time the code is generated. This ensures that the TOTP
   * algorithm generates a different code for each time window, where the width of a window is 30
   * seconds.
   *
   * <p>The <code>startTime</code> is optional and is used to define an offset. This offset will be
   * subtracted from the actual <code>time</code>. It can be used to define a starting point.
   *
   * <p>
   *
   * @exception {@link WException} if the <code>codeDigits</code> specified isn&apos;t within the
   *     range [6, 16].
   */
  public static String generateCode(
      final String key, int codeDigits, Duration time, Duration startTime) {
    if (codeDigits > MAXIMUM_CODE_LENGTH) {
      throw new WException(
          "Wt::Auth::Mfa::generateCode: codeDigits cannot be greater than "
              + String.valueOf(MAXIMUM_CODE_LENGTH));
    }
    if (codeDigits < MINIMUM_CODE_LENGTH) {
      throw new WException(
          "Wt::Auth::Mfa::generateCode: codeDigits cannot be lesser than "
              + String.valueOf(MINIMUM_CODE_LENGTH));
    }
    long timeSteps = (time.getSeconds() - startTime.getSeconds()) / PERIOD_IN_SECONDS;
    String hash = "";
    try {
      hash = Utils.hmac_sha1WithEncoding(timeSteps, Utils.base32Decode(key));
    } catch (IOException e) {
    }
    int offset = hash.charAt(hash.length() - 1) & 0xf;
    int binary =
        (hash.charAt(offset) & 0x7f) << 24
            | (hash.charAt(offset + 1) & 0xff) << 16
            | (hash.charAt(offset + 2) & 0xff) << 8
            | hash.charAt(offset + 3) & 0xff;
    int otp = binary % (int) Math.pow(10, codeDigits);
    String result = String.valueOf(otp);
    while (result.length() < (int) codeDigits) {
      result = "0" + result;
    }
    return result;
  }
  /**
   * Generates a TOTP (Time-Based One-Time Password) code.
   *
   * <p>Returns {@link #generateCode(String key, int codeDigits, Duration time, Duration startTime)
   * generateCode(key, codeDigits, time, Duration.ofSeconds(0))}
   */
  public static final String generateCode(final String key, int codeDigits, Duration time) {
    return generateCode(key, codeDigits, time, Duration.ofSeconds(0));
  }
  /**
   * Validate the given <code>code</code> with the given time frame.
   *
   * <p>Here the <code>key</code> is the secret key attached to the {@link User}, the <code>code
   * </code> is the TOTP code the user has entered, which is expected to be of length <code>
   * codeDigits</code>. This length is configured in {@link AuthService#setMfaCodeLength(int length)
   * AuthService#setMfaCodeLength()}.
   *
   * <p>The <code>time</code> specifies the time window for which the code is valid. When this
   * function executes, the code will be generated for the time frame the passed <code>time</code>
   * falls in, and in the previous window. Each window has a width of 30 seconds. Meaning that at
   * most a user has 1 minute to enter the code (if they submit it immediately at the start of the
   * first time frame). Or at least 30 seconds (if they submit it at the end of the first time
   * frame).
   *
   * <p>Time frames start either immediately on the minute, or halfway. This means that for the
   * times:
   *
   * <ul>
   *   <li>12:52:12, the start time frame will be 12:52:00
   *   <li>12:52:48, the start time frame will be 12:52:30
   * </ul>
   *
   * <p>The <code>startTime</code> is optional and is used to define an offset. This offset will be
   * subtracted from the actual <code>time</code>. It can be used to define a starting point.
   */
  public static boolean validateCode(
      final String key, final String code, int codeDigits, Duration time, Duration startTime) {
    if (code.length() != (int) codeDigits) {
      return false;
    }
    return generateCode(key, codeDigits, time, startTime).equals(code)
        || generateCode(
                key, codeDigits, time.minus(Duration.ofSeconds(PERIOD_IN_SECONDS)), startTime)
            .equals(code);
  }
  /**
   * Validate the given <code>code</code> with the given time frame.
   *
   * <p>Returns {@link #validateCode(String key, String code, int codeDigits, Duration time,
   * Duration startTime) validateCode(key, code, codeDigits, time, Duration.ofSeconds(0))}
   */
  public static final boolean validateCode(
      final String key, final String code, int codeDigits, Duration time) {
    return validateCode(key, code, codeDigits, time, Duration.ofSeconds(0));
  }

  private static final int MINIMUM_SECRET_LENGTH = 16;
  private static final int MAXIMUM_SECRET_LENGTH = 256;
  private static final int PERIOD_IN_SECONDS = 30;
  private static final int MINIMUM_CODE_LENGTH = 6;
  private static final int MAXIMUM_CODE_LENGTH = 16;

  static String toBigEndianHexString(long value) {
    return "";
  }
}
