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

/**
 * A segment of character/binary/control data in a QR Code symbol. Instances of this class are
 * immutable. The mid-level way to create a segment is to take the payload data and call a static
 * factory function such as QrSegment::makeNumeric(). The low-level way to create a segment is to
 * custom-make the bit buffer and call the QrSegment() constructor with appropriate values. This
 * segment class imposes no length restrictions, but QR Codes have restrictions. Even in the most
 * favorable conditions, a QR Code can only hold 7089 characters of data. Any segment longer than
 * this is meaningless for the purpose of generating QR Codes.
 */
public final class QrSegment {
  private static Logger logger = LoggerFactory.getLogger(QrSegment.class);

  static final class Mode {
    private static Logger logger = LoggerFactory.getLogger(Mode.class);

    public static final QrSegment.Mode NUMERIC = new QrSegment.Mode(0x1, 10, 12, 14);
    public static final QrSegment.Mode ALPHANUMERIC = new QrSegment.Mode(0x2, 9, 11, 13);
    public static final QrSegment.Mode BYTE = new QrSegment.Mode(0x4, 8, 16, 16);
    public static final QrSegment.Mode KANJI = new QrSegment.Mode(0x8, 8, 10, 12);
    public static final QrSegment.Mode ECI = new QrSegment.Mode(0x7, 0, 0, 0);
    private int modeBits;
    private int[] numBitsCharCount = new int[3];

    private Mode(int mode, int cc0, int cc1, int cc2) {
      this.modeBits = mode;
      this.numBitsCharCount[0] = cc0;
      this.numBitsCharCount[1] = cc1;
      this.numBitsCharCount[2] = cc2;
    }

    public int getModeBits() {
      return this.modeBits;
    }

    public int numCharCountBits(int ver) {
      return this.numBitsCharCount[(ver + 7) / 17];
    }
  }

  public static QrSegment makeBytes(final List<Integer> data) {
    if (data.size() > (int) Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Data too long");
    }
    BitBuffer bb = new BitBuffer();
    for (int b : data) {
      bb.appendBits(b, 8);
    }
    return new QrSegment(Mode.BYTE, (int) data.size(), bb);
  }

  public static QrSegment makeNumeric(String digits) {
    BitBuffer bb = new BitBuffer();
    int accumData = 0;
    int accumCount = 0;
    int charCount = 0;
    for (int digit = 0; digit != digits.length(); ++digit) {
      char c = digits.charAt(digit);
      if (c < '0' || c > '9') {
        throw new IllegalArgumentException("String contains non-numeric characters");
      }
      accumData = accumData * 10 + (c - '0');
      ++accumCount;
      if (accumCount == 3) {
        bb.appendBits((int) accumData, 10);
        accumData = 0;
        accumCount = 0;
      }
      ++charCount;
    }
    if (accumCount > 0) {
      bb.appendBits((int) accumData, accumCount * 3 + 1);
    }
    return new QrSegment(Mode.NUMERIC, charCount, bb);
  }

  public static QrSegment makeAlphanumeric(String text) {
    BitBuffer bb = new BitBuffer();
    int accumData = 0;
    int accumCount = 0;
    int charCount = 0;
    for (int t = 0; t != text.length(); ++t) {
      int temp = ALPHANUMERIC_CHARSET.indexOf(text.charAt(t));
      if (temp == -1) {
        throw new IllegalArgumentException(
            "String contains unencodable characters in alphanumeric mode");
      }
      accumData = accumData * 45 + (int) temp;
      ++accumCount;
      if (accumCount == 2) {
        bb.appendBits((int) accumData, 11);
        accumData = 0;
        accumCount = 0;
      }
      ++charCount;
    }
    if (accumCount > 0) {
      bb.appendBits((int) accumData, 6);
    }
    return new QrSegment(Mode.ALPHANUMERIC, charCount, bb);
  }

  public static List<QrSegment> makeSegments(String text) {
    List<QrSegment> result = new ArrayList<QrSegment>();
    if (text.length() == 0) {;
    } else {
      if (isNumeric(text)) {
        result.add(makeNumeric(text));
      } else {
        if (isAlphanumeric(text)) {
          result.add(makeAlphanumeric(text));
        } else {
          List<Integer> bytes = new ArrayList<Integer>();
          for (int t = 0; t != text.length(); ++t) {
            bytes.add((int) text.charAt(t));
          }
          result.add(makeBytes(bytes));
        }
      }
    }
    return result;
  }

  public static QrSegment makeEci(long assignVal) {
    BitBuffer bb = new BitBuffer();
    if (assignVal < 0) {
      throw new IllegalArgumentException("ECI assignment value out of range");
    } else {
      if (assignVal < 1 << 7) {
        bb.appendBits((int) assignVal, 8);
      } else {
        if (assignVal < 1 << 14) {
          bb.appendBits(2, 2);
          bb.appendBits((int) assignVal, 14);
        } else {
          if (assignVal < 1000000L) {
            bb.appendBits(6, 3);
            bb.appendBits((int) assignVal, 21);
          } else {
            throw new IllegalArgumentException("ECI assignment value out of range");
          }
        }
      }
    }
    return new QrSegment(Mode.ECI, 0, bb);
  }

  public static boolean isNumeric(String text) {
    for (int t = 0; t != text.length(); ++t) {
      char c = text.charAt(t);
      if (c < '0' || c > '9') {
        return false;
      }
    }
    return true;
  }

  public static boolean isAlphanumeric(String text) {
    for (int t = 0; t != text.length(); ++t) {
      if (ALPHANUMERIC_CHARSET.indexOf(text.charAt(t)) == -1) {
        return false;
      }
    }
    return true;
  }

  private QrSegment.Mode mode;
  private int numChars;
  private List<Boolean> data;

  public QrSegment(final QrSegment.Mode md, int numCh, final List<Boolean> dt) {
    this.mode = md;
    this.numChars = numCh;
    this.data = dt;
    if (numCh < 0) {
      throw new IllegalArgumentException("Invalid value");
    }
  }

  public QrSegment.Mode getMode() {
    return this.mode;
  }

  public int getNumChars() {
    return this.numChars;
  }

  public List<Boolean> getData() {
    return this.data;
  }

  public static int getTotalBits(final List<QrSegment> segs, int version) {
    int result = 0;
    for (final QrSegment seg : segs) {
      int ccbits = seg.mode.numCharCountBits(version);
      if (seg.numChars >= 1L << ccbits) {
        return -1;
      }
      if (4 + ccbits > Integer.MAX_VALUE - result) {
        return -1;
      }
      result += 4 + ccbits;
      if (seg.data.size() > (int) (Integer.MAX_VALUE - result)) {
        return -1;
      }
      result += (int) seg.data.size();
    }
    return result;
  }

  private static String ALPHANUMERIC_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";
}
