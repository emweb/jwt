/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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

/** An enumeration type for specific user agent. */
public enum UserAgent {
  /** Unknown user agent. */
  Unknown(0),
  /** Internet Explorer Mobile, 5 or older. */
  IEMobile(1000),
  /** Internet Explorer 6. */
  IE6(1001),
  /** Internet Explorer 7. */
  IE7(1002),
  /** Internet Explorer 8. */
  IE8(1003),
  /** Internet Explorer 9. */
  IE9(1004),
  /** Internet Explorer 10. */
  IE10(1005),
  /** Internet Explorer 11. */
  IE11(1006),
  /** Edge. */
  Edge(1100),
  /** Opera. */
  Opera(3000),
  /** Opera 10 or later. */
  Opera10(3010),
  /** WebKit. */
  WebKit(4000),
  /** Safari 2 or older. */
  Safari(4100),
  /** Safari 3. */
  Safari3(4103),
  /** Safari 4 or later. */
  Safari4(4104),
  /** Chrome 0. */
  Chrome0(4200),
  /** Chrome 1. */
  Chrome1(4201),
  /** Chrome 2. */
  Chrome2(4202),
  /** Chrome 3. */
  Chrome3(4203),
  /** Chrome 4. */
  Chrome4(4204),
  /** Chrome 5 or later. */
  Chrome5(4205),
  /** Arora. */
  Arora(4300),
  /** Mobile WebKit. */
  MobileWebKit(4400),
  /** Mobile WebKit iPhone/iPad. */
  MobileWebKitiPhone(4450),
  /** Mobile WebKit Android. */
  MobileWebKitAndroid(4500),
  /** Konqueror. */
  Konqueror(5000),
  /** Gecko. */
  Gecko(6000),
  /** Firefox 2 or older. */
  Firefox(6100),
  /** Firefox 3.0. */
  Firefox3_0(6101),
  /** Firefox 3.1. */
  Firefox3_1(6102),
  /** Firefox 3.1b. */
  Firefox3_1b(6103),
  /** Firefox 3.5. */
  Firefox3_5(6104),
  /** Firefox 3.6. */
  Firefox3_6(6105),
  /** Firefox 4.0. */
  Firefox4_0(6106),
  /** Firefox 5.0 or later. */
  Firefox5_0(6107),
  /** Bot user agent. */
  BotAgent(10000);

  private int value;

  UserAgent(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
