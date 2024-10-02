/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.thirdparty.qrcodegen;

import eu.webtoolkit.jwt.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An appendable sequence of bits (0s and 1s). Mainly used by {@link QrSegment}. */
public final class BitBuffer extends ArrayList<Boolean> {
  private static Logger logger = LoggerFactory.getLogger(BitBuffer.class);

  public BitBuffer() {
    super();
  }

  public void appendBits(int val, int len) {
    if (len < 0 || len > 31 || val >> len != 0) {
      throw new IllegalArgumentException("Value out of range");
    }
    for (int i = len - 1; i >= 0; i--) {
      this.add((val >> i & 1) != 0);
    }
  }
}
