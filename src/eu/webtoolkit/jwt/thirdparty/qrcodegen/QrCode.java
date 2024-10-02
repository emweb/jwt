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
 * A QR Code symbol, which is a type of two-dimension barcode. Invented by Denso Wave and described
 * in the ISO/IEC 18004 standard. Instances of this class represent an immutable square grid of dark
 * and light cells. The class provides static factory functions to create a QR Code from text or
 * binary data. The class covers the QR Code Model 2 specification, supporting all versions (sizes)
 * from 1 to 40, all 4 error correction levels, and 4 character encoding modes.
 *
 * <p>Ways to create a QR Code object:
 *
 * <ul>
 *   <li>High level: Take the payload data and call QrCode::encodeText() or QrCode::encodeBinary().
 *   <li>Mid level: Custom-make the list of segments and call QrCode::encodeSegments().
 *   <li>Low level: Custom-make the array of data codeword bytes (including segment headers and
 *       final padding, excluding error correction codewords), supply the appropriate version
 *       number, and call the QrCode() constructor. (Note that all ways require supplying the
 *       desired error correction level.)
 * </ul>
 */
public final class QrCode {
  private static Logger logger = LoggerFactory.getLogger(QrCode.class);

  private static int getFormatBits(Ecc ecl) {
    switch (ecl) {
      case LOW:
        return 1;
      case MEDIUM:
        return 0;
      case QUARTILE:
        return 3;
      case HIGH:
        return 2;
      default:
        throw new UnsupportedOperationException("Unreachable");
    }
  }

  public static QrCode encodeText(String text, Ecc ecl) {
    List<QrSegment> segs = QrSegment.makeSegments(text);
    return encodeSegments(segs, ecl);
  }

  public static QrCode encodeBinary(final List<Integer> data, Ecc ecl) {
    List<QrSegment> segs = new ArrayList<QrSegment>();
    segs.add(QrSegment.makeBytes(data));
    return encodeSegments(segs, ecl);
  }

  public static QrCode encodeSegments(
      final List<QrSegment> segs,
      Ecc ecl,
      int minVersion,
      int maxVersion,
      int mask,
      boolean boostEcl) {
    if (!(MIN_VERSION <= minVersion && minVersion <= maxVersion && maxVersion <= MAX_VERSION)
        || mask < -1
        || mask > 7) {
      throw new IllegalArgumentException("Invalid value");
    }
    int version;
    int dataUsedBits;
    for (version = minVersion; ; version++) {
      int dataCapacityBits = getNumDataCodewords(version, ecl) * 8;
      dataUsedBits = QrSegment.getTotalBits(segs, version);
      if (dataUsedBits != -1 && dataUsedBits <= dataCapacityBits) {
        break;
      }
      if (version >= maxVersion) {
        StringWriter sb = new StringWriter();
        if (dataUsedBits == -1) {
          sb.append("Segment too long");
        } else {
          sb.append("Data length = ").append(String.valueOf(dataUsedBits)).append(" bits, ");
          sb.append("Max capacity = ").append(String.valueOf(dataCapacityBits)).append(" bits");
        }
        throw new data_too_long(sb.toString());
      }
    }
    assert dataUsedBits != -1;
    List<Ecc> codes = new ArrayList<Ecc>();
    codes.add(Ecc.MEDIUM);
    codes.add(Ecc.QUARTILE);
    codes.add(Ecc.HIGH);
    for (Ecc newEcl : codes) {
      if (boostEcl && dataUsedBits <= getNumDataCodewords(version, newEcl) * 8) {
        ecl = newEcl;
      }
    }
    BitBuffer bb = new BitBuffer();
    for (final QrSegment seg : segs) {
      bb.appendBits((int) seg.getMode().getModeBits(), 4);
      bb.appendBits((int) seg.getNumChars(), seg.getMode().numCharCountBits(version));
      for (int j = 0; j != seg.getData().size(); ++j) {
        bb.add(bb.size(), seg.getData().get(j));
      }
    }
    assert bb.size() == (int) dataUsedBits;
    int dataCapacityBits = (int) getNumDataCodewords(version, ecl) * 8;
    assert bb.size() <= dataCapacityBits;
    bb.appendBits(0, Math.min(4, (int) (dataCapacityBits - bb.size())));
    bb.appendBits(0, (8 - (int) (bb.size() % 8)) % 8);
    assert bb.size() % 8 == 0;
    for (int padByte = 0xEC; bb.size() < dataCapacityBits; padByte ^= 0xEC ^ 0x11) {
      bb.appendBits(padByte, 8);
    }
    List<Integer> dataCodewords = new ArrayList<Integer>();
    int defaultValue = 0;
    for (int add = 0; add < (bb.size() / 8); ++add) {
      dataCodewords.add(defaultValue);
    }
    ;
    for (int i = 0; i < bb.size(); i++) {
      dataCodewords.set(i >> 3, dataCodewords.get(i >> 3) | (bb.get(i) ? 1 : 0) << 7 - (i & 7));
    }
    return new QrCode(version, ecl, dataCodewords, mask);
  }

  public static final QrCode encodeSegments(final List<QrSegment> segs, Ecc ecl) {
    return encodeSegments(segs, ecl, 1, 40, -1, true);
  }

  public static final QrCode encodeSegments(final List<QrSegment> segs, Ecc ecl, int minVersion) {
    return encodeSegments(segs, ecl, minVersion, 40, -1, true);
  }

  public static final QrCode encodeSegments(
      final List<QrSegment> segs, Ecc ecl, int minVersion, int maxVersion) {
    return encodeSegments(segs, ecl, minVersion, maxVersion, -1, true);
  }

  public static final QrCode encodeSegments(
      final List<QrSegment> segs, Ecc ecl, int minVersion, int maxVersion, int mask) {
    return encodeSegments(segs, ecl, minVersion, maxVersion, mask, true);
  }

  private int version;
  private int size;
  private Ecc errorCorrectionLevel;
  private int mask;
  private List<List<Boolean>> modules;
  private List<List<Boolean>> isFunction;

  public QrCode(int ver, Ecc ecl, final List<Integer> dataCodewords, int msk) {
    this.version = ver;
    this.errorCorrectionLevel = ecl;
    this.modules = new ArrayList<List<Boolean>>();
    this.isFunction = new ArrayList<List<Boolean>>();
    if (ver < MIN_VERSION || ver > MAX_VERSION) {
      throw new IllegalArgumentException("Version value out of range");
    }
    if (msk < -1 || msk > 7) {
      throw new IllegalArgumentException("Mask value out of range");
    }
    this.size = ver * 4 + 17;
    int sz = (int) this.size;
    Utils.copyList(new ArrayList<List<Boolean>>(), this.modules);
    List<Boolean> defaultVector = new ArrayList<Boolean>();
    boolean defaultValue = false;
    for (int add = 0; add < (sz); ++add) {
      defaultVector.add(defaultValue);
    }
    ;
    for (int add = 0; add < (sz); ++add) {
      this.modules.add((ArrayList) ((ArrayList) defaultVector).clone());
    }
    ;
    for (int add = 0; add < (sz); ++add) {
      this.isFunction.add((ArrayList) ((ArrayList) defaultVector).clone());
    }
    ;
    this.drawFunctionPatterns();
    final List<Integer> allCodewords = this.addEccAndInterleave(dataCodewords);
    this.drawCodewords(allCodewords);
    if (msk == -1) {
      long minPenalty = Long.MAX_VALUE;
      for (int i = 0; i < 8; i++) {
        this.applyMask(i);
        this.drawFormatBits(i);
        long penalty = this.getPenaltyScore();
        if (penalty < minPenalty) {
          msk = i;
          minPenalty = penalty;
        }
        this.applyMask(i);
      }
    }
    assert 0 <= msk && msk <= 7;
    this.mask = msk;
    this.applyMask(msk);
    this.drawFormatBits(msk);
    this.isFunction.clear();
  }

  public int getVersion() {
    return this.version;
  }

  public int getSize() {
    return this.size;
  }

  public Ecc getErrorCorrectionLevel() {
    return this.errorCorrectionLevel;
  }

  public int getMask() {
    return this.mask;
  }

  public boolean getModule(int x, int y) {
    return 0 <= x && x < this.size && 0 <= y && y < this.size && this.module(x, y);
  }

  private void drawFunctionPatterns() {
    for (int i = 0; i < this.size; i++) {
      this.setFunctionModule(6, i, i % 2 == 0);
      this.setFunctionModule(i, 6, i % 2 == 0);
    }
    this.drawFinderPattern(3, 3);
    this.drawFinderPattern(this.size - 4, 3);
    this.drawFinderPattern(3, this.size - 4);
    final List<Integer> alignPatPos = this.getAlignmentPatternPositions();
    int numAlign = alignPatPos.size();
    for (int i = 0; i < numAlign; i++) {
      for (int j = 0; j < numAlign; j++) {
        if (!(i == 0 && j == 0 || i == 0 && j == numAlign - 1 || i == numAlign - 1 && j == 0)) {
          this.drawAlignmentPattern(alignPatPos.get(i), alignPatPos.get(j));
        }
      }
    }
    this.drawFormatBits(0);
    this.drawVersion();
  }

  private void drawFormatBits(int msk) {
    int data = getFormatBits(this.errorCorrectionLevel) << 3 | msk;
    int rem = data;
    for (int i = 0; i < 10; i++) {
      rem = rem << 1 ^ (rem >> 9) * 0x537;
    }
    int bits = (data << 10 | rem) ^ 0x5412;
    assert bits >> 15 == 0;
    for (int i = 0; i <= 5; i++) {
      this.setFunctionModule(8, i, getBit(bits, i));
    }
    this.setFunctionModule(8, 7, getBit(bits, 6));
    this.setFunctionModule(8, 8, getBit(bits, 7));
    this.setFunctionModule(7, 8, getBit(bits, 8));
    for (int i = 9; i < 15; i++) {
      this.setFunctionModule(14 - i, 8, getBit(bits, i));
    }
    for (int i = 0; i < 8; i++) {
      this.setFunctionModule(this.size - 1 - i, 8, getBit(bits, i));
    }
    for (int i = 8; i < 15; i++) {
      this.setFunctionModule(8, this.size - 15 + i, getBit(bits, i));
    }
    this.setFunctionModule(8, this.size - 8, true);
  }

  private void drawVersion() {
    if (this.version < 7) {
      return;
    }
    int rem = this.version;
    for (int i = 0; i < 12; i++) {
      rem = rem << 1 ^ (rem >> 11) * 0x1F25;
    }
    long bits = (long) this.version << 12 | rem;
    assert bits >> 18 == 0;
    for (int i = 0; i < 18; i++) {
      boolean bit = getBit(bits, i);
      int a = this.size - 11 + i % 3;
      int b = i / 3;
      this.setFunctionModule(a, b, bit);
      this.setFunctionModule(b, a, bit);
    }
  }

  private void drawFinderPattern(int x, int y) {
    for (int dy = -4; dy <= 4; dy++) {
      for (int dx = -4; dx <= 4; dx++) {
        int dist = Math.max(Math.abs(dx), Math.abs(dy));
        int xx = x + dx;
        int yy = y + dy;
        if (0 <= xx && xx < this.size && 0 <= yy && yy < this.size) {
          this.setFunctionModule(xx, yy, dist != 2 && dist != 4);
        }
      }
    }
  }

  private void drawAlignmentPattern(int x, int y) {
    for (int dy = -2; dy <= 2; dy++) {
      for (int dx = -2; dx <= 2; dx++) {
        this.setFunctionModule(x + dx, y + dy, Math.max(Math.abs(dx), Math.abs(dy)) != 1);
      }
    }
  }

  private void setFunctionModule(int x, int y, boolean isDark) {
    int ux = (int) x;
    int uy = (int) y;
    this.modules.get(uy).set(ux, isDark);
    this.isFunction.get(uy).set(ux, true);
  }

  private boolean module(int x, int y) {
    return this.modules.get((int) y).get((int) x);
  }

  private List<Integer> addEccAndInterleave(final List<Integer> data) {
    if (data.size() != (int) getNumDataCodewords(this.version, this.errorCorrectionLevel)) {
      throw new IllegalArgumentException("Invalid argument");
    }
    int numBlocks =
        NUM_ERROR_CORRECTION_BLOCKS[(int) this.errorCorrectionLevel.getValue()][this.version];
    int blockEccLen =
        ECC_CODEWORDS_PER_BLOCK[(int) this.errorCorrectionLevel.getValue()][this.version];
    int rawCodewords = getNumRawDataModules(this.version) / 8;
    int numShortBlocks = numBlocks - rawCodewords % numBlocks;
    int shortBlockLen = rawCodewords / numBlocks;
    List<List<Integer>> blocks = new ArrayList<List<Integer>>();
    final List<Integer> rsDiv = reedSolomonComputeDivisor(blockEccLen);
    int k = 0;
    for (int i = 0; i < numBlocks; i++) {
      int shift = shortBlockLen - blockEccLen + (i < numShortBlocks ? 0 : 1);
      List<Integer> dat = new ArrayList<Integer>();
      for (int j = k + shift; j > k; --j) {
        dat.add(0, data.get(j - 1));
      }
      k += (int) dat.size();
      final List<Integer> ecc = reedSolomonComputeRemainder(dat, rsDiv);
      if (i < numShortBlocks) {
        dat.add(0);
      }
      for (int j = 0; j != ecc.size(); ++j) {
        dat.add(dat.size(), ecc.get(j));
      }
      blocks.add(dat);
    }
    List<Integer> result = new ArrayList<Integer>();
    for (int i = 0; i < blocks.get(0).size(); i++) {
      for (int j = 0; j < blocks.size(); j++) {
        if (i != (int) (shortBlockLen - blockEccLen) || j >= (int) numShortBlocks) {
          result.add(blocks.get(j).get(i));
        }
      }
    }
    assert result.size() == (int) rawCodewords;
    return result;
  }

  private void drawCodewords(final List<Integer> data) {
    if (data.size() != (int) (getNumRawDataModules(this.version) / 8)) {
      throw new IllegalArgumentException("Invalid argument");
    }
    int i = 0;
    for (int right = this.size - 1; right >= 1; right -= 2) {
      if (right == 6) {
        right = 5;
      }
      for (int vert = 0; vert < this.size; vert++) {
        for (int j = 0; j < 2; j++) {
          int x = (int) (right - j);
          boolean upward = (right + 1 & 2) == 0;
          int y_res = upward ? this.size - 1 - vert : vert;
          int y = (int) y_res;
          if (!this.isFunction.get(y).get(x) && i < data.size() * 8) {
            this.modules.get(y).set(x, getBit(data.get(i >> 3), 7 - (int) (i & 7)));
            i++;
          }
        }
      }
    }
    assert i == data.size() * 8;
  }

  private void applyMask(int msk) {
    if (msk < 0 || msk > 7) {
      throw new IllegalArgumentException("Mask value out of range");
    }
    int sz = (int) this.size;
    for (int y = 0; y < sz; y++) {
      for (int x = 0; x < sz; x++) {
        boolean invert;
        switch (msk) {
          case 0:
            invert = (x + y) % 2 == 0;
            break;
          case 1:
            invert = y % 2 == 0;
            break;
          case 2:
            invert = x % 3 == 0;
            break;
          case 3:
            invert = (x + y) % 3 == 0;
            break;
          case 4:
            invert = (x / 3 + y / 2) % 2 == 0;
            break;
          case 5:
            invert = x * y % 2 + x * y % 3 == 0;
            break;
          case 6:
            invert = (x * y % 2 + x * y % 3) % 2 == 0;
            break;
          case 7:
            invert = ((x + y) % 2 + x * y % 3) % 2 == 0;
            break;
          default:
            throw new UnsupportedOperationException("Unreachable");
        }
        this.modules
            .get(y)
            .set(x, this.modules.get(y).get(x) ^ invert & !this.isFunction.get(y).get(x));
      }
    }
  }

  private long getPenaltyScore() {
    long result = 0;
    for (int y = 0; y < this.size; y++) {
      boolean runColor = false;
      int runX = 0;
      Integer[] runHistory = new Integer[ /*int*/7];
      Arrays.fill(runHistory, 0);
      ;
      for (int x = 0; x < this.size; x++) {
        if (this.module(x, y) == runColor) {
          runX++;
          if (runX == 5) {
            result += PENALTY_N1;
          } else {
            if (runX > 5) {
              result++;
            }
          }
        } else {
          this.finderPenaltyAddHistory(runX, runHistory);
          if (!runColor) {
            result += this.finderPenaltyCountPatterns(runHistory) * PENALTY_N3;
          }
          runColor = this.module(x, y);
          runX = 1;
        }
      }
      result += this.finderPenaltyTerminateAndCount(runColor, runX, runHistory) * PENALTY_N3;
    }
    for (int x = 0; x < this.size; x++) {
      boolean runColor = false;
      int runY = 0;
      Integer[] runHistory = new Integer[ /*int*/7];
      Arrays.fill(runHistory, 0);
      ;
      for (int y = 0; y < this.size; y++) {
        if (this.module(x, y) == runColor) {
          runY++;
          if (runY == 5) {
            result += PENALTY_N1;
          } else {
            if (runY > 5) {
              result++;
            }
          }
        } else {
          this.finderPenaltyAddHistory(runY, runHistory);
          if (!runColor) {
            result += this.finderPenaltyCountPatterns(runHistory) * PENALTY_N3;
          }
          runColor = this.module(x, y);
          runY = 1;
        }
      }
      result += this.finderPenaltyTerminateAndCount(runColor, runY, runHistory) * PENALTY_N3;
    }
    for (int y = 0; y < this.size - 1; y++) {
      for (int x = 0; x < this.size - 1; x++) {
        boolean color = this.module(x, y);
        if (color == this.module(x + 1, y)
            && color == this.module(x, y + 1)
            && color == this.module(x + 1, y + 1)) {
          result += PENALTY_N2;
        }
      }
    }
    int dark = 0;
    for (final List<Boolean> row : this.modules) {
      for (boolean color : row) {
        if (color) {
          dark++;
        }
      }
    }
    int total = this.size * this.size;
    int k = (int) ((Math.abs(dark * 20L - total * 10L) + total - 1) / total) - 1;
    assert 0 <= k && k <= 9;
    result += k * PENALTY_N4;
    assert 0 <= result && result <= 2568888L;
    return result;
  }

  private List<Integer> getAlignmentPatternPositions() {
    if (this.version == 1) {
      return new ArrayList<Integer>();
    } else {
      int numAlign = this.version / 7 + 2;
      int step =
          this.version == 32 ? 26 : (this.version * 4 + numAlign * 2 + 1) / (numAlign * 2 - 2) * 2;
      List<Integer> result = new ArrayList<Integer>();
      int pos = this.size - 7;
      for (int i = 0; i < numAlign - 1; i++) {
        result.add(0, pos);
        pos -= step;
      }
      result.add(0, 6);
      return result;
    }
  }

  private static int getNumRawDataModules(int ver) {
    if (ver < MIN_VERSION || ver > MAX_VERSION) {
      throw new IllegalArgumentException("Version number out of range");
    }
    int result = (16 * ver + 128) * ver + 64;
    if (ver >= 2) {
      int numAlign = ver / 7 + 2;
      result -= (25 * numAlign - 10) * numAlign - 55;
      if (ver >= 7) {
        result -= 36;
      }
    }
    assert 208 <= result && result <= 29648;
    return result;
  }

  private static int getNumDataCodewords(int ver, Ecc ecl) {
    return getNumRawDataModules(ver) / 8
        - ECC_CODEWORDS_PER_BLOCK[(int) ecl.getValue()][ver]
            * NUM_ERROR_CORRECTION_BLOCKS[(int) ecl.getValue()][ver];
  }

  private static List<Integer> reedSolomonComputeDivisor(int degree) {
    if (degree < 1 || degree > 255) {
      throw new IllegalArgumentException("Degree out of range");
    }
    List<Integer> result = new ArrayList<Integer>();
    int defaultValue = 0;
    for (int add = 0; add < ((int) degree); ++add) {
      result.add(defaultValue);
    }
    ;
    result.set(result.size() - 1, 1);
    int root = 1;
    for (int i = 0; i < degree; i++) {
      for (int j = 0; j < result.size(); j++) {
        result.set(j, reedSolomonMultiply(result.get(j), root));
        if (j + 1 < result.size()) {
          result.set(j, result.get(j) ^ result.get(j + 1));
        }
      }
      root = reedSolomonMultiply(root, 0x02);
    }
    return result;
  }

  private static List<Integer> reedSolomonComputeRemainder(
      final List<Integer> data, final List<Integer> divisor) {
    List<Integer> result = new ArrayList<Integer>();
    int defaultValue = 0;
    for (int add = 0; add < (divisor.size()); ++add) {
      result.add(defaultValue);
    }
    ;
    for (int b : data) {
      int factor = b ^ result.get(0);
      result.remove(0);
      result.add(0);
      for (int i = 0; i < result.size(); i++) {
        result.set(i, result.get(i) ^ reedSolomonMultiply(divisor.get(i), factor));
      }
    }
    return result;
  }

  private static int reedSolomonMultiply(int x, int y) {
    int z = 0;
    for (int i = 7; i >= 0; i--) {
      z = z << 1 ^ (z >> 7) * 0x11D;
      z ^= (y >> i & 1) * x;
    }
    assert z >> 8 == 0;
    return (int) z;
  }

  private int finderPenaltyCountPatterns(final Integer[] runHistory) {
    int n = runHistory[1];
    assert n <= this.size * 3;
    boolean core =
        n > 0
            && runHistory[2] == n
            && runHistory[3] == n * 3
            && runHistory[4] == n
            && runHistory[5] == n;
    return (core && runHistory[0] >= n * 4 && runHistory[6] >= n ? 1 : 0)
        + (core && runHistory[6] >= n * 4 && runHistory[0] >= n ? 1 : 0);
  }

  private int finderPenaltyTerminateAndCount(
      boolean currentRunColor, int currentRunLength, final Integer[] runHistory) {
    if (currentRunColor) {
      this.finderPenaltyAddHistory(currentRunLength, runHistory);
      currentRunLength = 0;
    }
    currentRunLength += this.size;
    this.finderPenaltyAddHistory(currentRunLength, runHistory);
    return this.finderPenaltyCountPatterns(runHistory);
  }

  private void finderPenaltyAddHistory(int currentRunLength, final Integer[] runHistory) {
    if (runHistory[0] == 0) {
      currentRunLength += this.size;
    }
    for (int i = runHistory.length - 2; i >= 0; i--) {
      runHistory[i + 1] = runHistory[i];
    }
    ;
    runHistory[0] = currentRunLength;
  }

  private static boolean getBit(long x, int i) {
    return (x >> i & 1) != 0;
  }

  public static final int MIN_VERSION = 1;
  public static final int MAX_VERSION = 40;
  private static final int PENALTY_N1 = 3;
  private static final int PENALTY_N2 = 3;
  private static final int PENALTY_N3 = 40;
  private static final int PENALTY_N4 = 10;
  private static int[][] ECC_CODEWORDS_PER_BLOCK = {
    {
      -1, 7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 28, 30,
      30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30
    },
    {
      -1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26, 26, 28,
      28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28
    },
    {
      -1, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 30, 28, 30,
      30, 30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30
    },
    {
      -1, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 24, 28, 22, 24, 24, 30, 28, 28, 26, 28, 30, 24,
      30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30
    }
  };
  private static int[][] NUM_ERROR_CORRECTION_BLOCKS = {
    {
      -1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 4, 4, 4, 4, 4, 6, 6, 6, 6, 7, 8, 8, 9, 9, 10, 12, 12, 12, 13,
      14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25
    },
    {
      -1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5, 5, 8, 9, 9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23,
      25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49
    },
    {
      -1, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8, 8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25, 27, 29,
      34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68
    },
    {
      -1, 1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30, 32, 35,
      37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81
    }
  };
}
