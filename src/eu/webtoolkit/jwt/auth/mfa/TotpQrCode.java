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

/**
 * A QR code generator for TOTP secret keys.
 *
 * <p>This class can be used to generate a QR code from a TOTP secret key. The QR code will then be
 * painted to the screen.
 *
 * <p>This allows for some authenticator apps to more conveniently add the TOTP secret key. This QR
 * code embeds more than just the secret.
 *
 * <p>
 *
 * @see TotpQrCode#formatKey(String key, String serviceName, String userName, int codeDigits)
 */
public class TotpQrCode extends WQrCode {
  private static Logger logger = LoggerFactory.getLogger(TotpQrCode.class);

  /**
   * Constructor.
   *
   * <p>This takes the arguments:
   *
   * <ul>
   *   <li><code>key:</code> the secret TOTP key.
   *   <li><code>seviceName:</code> the name of the application.
   *   <li><code>userName:</code> the identifier of the client used to log in.
   *   <li><code>codeDigits:</code> the length of the expected TOTP code accepted.
   * </ul>
   */
  public TotpQrCode(
      final String key,
      final String serviceName,
      final String userName,
      int codeDigits,
      WContainerWidget parentContainer) {
    super();
    this.setMessage(this.formatKey(key, serviceName, userName, codeDigits));
    this.setErrorCorrectionLevel(WQrCode.ErrorCorrectionLevel.LOW);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #TotpQrCode(String key, String serviceName, String userName, int codeDigits,
   * WContainerWidget parentContainer) this(key, serviceName, userName, codeDigits,
   * (WContainerWidget)null)}
   */
  public TotpQrCode(
      final String key, final String serviceName, final String userName, int codeDigits) {
    this(key, serviceName, userName, codeDigits, (WContainerWidget) null);
  }
  /**
   * Format the key and other information to a correct QR code.
   *
   * <p>To generate a correct QR code, it needs to follow a specific format. The rules of this
   * format can be consulted on the site: <a
   * href="https://github.com/google/google-authenticator/wiki/Key-Uri-Format">https://github.com/google/google-authenticator/wiki/Key-Uri-Format</a>.
   *
   * <p>This stipulates that a valid string must contain a label, followed by some (optional)
   * parameters.
   *
   * <p>The label is formatted such that: <code>serviceName:userName</code>.
   *
   * <p>The used parameters are:
   *
   * <ul>
   *   <li>secret: the generated TOTP secret key
   *   <li>issuer: same as the serviceName
   *   <li>userName: the name of the user for whom the QR code is created
   *   <li>algorithm: always SHA1 (the default)
   *   <li>digits: the number of digits the generated code contains
   *   <li>period: the size of the time frame/window
   * </ul>
   */
  public String formatKey(
      final String key, final String serviceName, final String userName, int codeDigits) {
    String path =
        new WString(
                "otpauth://totp/{1}:{2}?secret={3}&issuer={1}&algorithm=SHA1&digits={4}&period=30")
            .arg(serviceName)
            .arg(userName)
            .arg(key)
            .arg(codeDigits)
            .toString();
    return path;
  }
}
