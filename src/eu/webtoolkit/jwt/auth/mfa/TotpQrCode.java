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
import eu.webtoolkit.jwt.thirdparty.qrcodegen.*;
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
public class TotpQrCode extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(TotpQrCode.class);

  public static final Ecc ErrorLevelCorrection = Ecc.LOW;
  public static final double SQUARE_SIZE = 5;
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
    this.code_ =
        QrCode.encodeText(
            this.formatKey(key, serviceName, userName, codeDigits), ErrorLevelCorrection);
    final double size = this.getCode().getSize() * SQUARE_SIZE;
    this.resize(new WLength(size), new WLength(size));
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

  protected void paintEvent(WPaintDevice paintDevice) {
    WPainter painter = new WPainter(paintDevice);
    WBrush brush = new WBrush(StandardColor.Black);
    for (int line = 0; line < this.getCode().getSize(); ++line) {
      for (int column = 0; column < this.getCode().getSize(); ++column) {
        if (this.getCode().getModule(column, line)) {
          painter.fillRect(
              line * SQUARE_SIZE, column * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, brush);
        }
      }
    }
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
  protected String formatKey(
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

  private QrCode code_;

  private QrCode getCode() {
    return this.code_;
  }
}
