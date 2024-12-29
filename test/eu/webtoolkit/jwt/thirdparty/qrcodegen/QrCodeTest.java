package eu.webtoolkit.jwt.thirdparty.qrcodegen;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.List;

public class QrCodeTest {
  @Test
  public void testEncodeSegmentsLowEmpty() {
    String value = "";
    List<QrSegment> segments = QrSegment.makeSegments(value);
    QrCode code = QrCode.encodeSegments(segments, Ecc.LOW);

    assertEquals(code.getSize(), 21);
    Boolean[] expected = {
      true, true, true, true, true, true, true, false, false, true, true, false, true, false, true, true, true, true, true, true, true,
      true, false, false, false, false, false, true, false, false, true, true, true, false, false, true, false, false, false, false, false, true,
      true, false, true, true, true, false, true, false, false, false, false, false, true, false, true, false, true, true, true, false, true,
      true, false, true, true, true, false, true, false, true, false, false, true, true, false, true, false, true, true, true, false, true,
      true, false, true, true, true, false, true, false, true, false, false, true, false, false, true, false, true, true, true, false, true,
      true, false, false, false, false, false, true, false, false, true, true, false, false, false, true, false, false, false, false, false, true,
      true, true, true, true, true, true, true, false, true, false, true, false, true, false, true, true, true, true, true, true, true,
      false, false, false, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false,
      false, false, true, true, false, false, true, false, false, true, true, true, true, true, true, false, true, true, false, false, false,
      true, false, true, true, true, true, false, false, false, false, false, true, false, true, true, true, true, true, true, false, false,
      false, true, true, true, true, false, true, false, false, false, false, true, false, false, true, true, false, false, false, false, true,
      false, true, true, false, true, true, false, false, false, true, false, true, false, false, false, true, true, false, true, false, true,
      false, false, true, true, false, true, true, false, true, true, true, false, false, false, false, false, false, true, true, false, true,
      false, false, false, false, false, false, false, false, false, false, true, false, false, true, true, true, false, false, false, true, false,
      true, true, true, true, true, true, true, false, false, false, true, false, true, false, true, true, true, false, false, false, true,
      true, false, false, false, false, false, true, false, false, true, true, true, true, false, false, false, false, true, true, true, true,
      true, false, true, true, true, false, true, false, false, true, false, true, true, false, false, false, true, true, true, false, true,
      true, false, true, true, true, false, true, false, true, true, true, false, true, false, true, false, false, false, true, true, false,
      true, false, true, true, true, false, true, false, true, false, false, false, true, true, true, true, true, false, false, false, true,
      true, false, false, false, false, false, true, false, false, true, false, true, true, true, false, false, true, false, true, true, true,
      true, true, true, true, true, true, true, false, false, true, true, false, true, true, true, false, false, false, true, true, false };

    for (int x = 0; x < code.getSize(); ++x) {
      for (int y = 0; y < code.getSize(); ++y) {
        System.out.println("Expected: " + expected[y + (x * code.getSize())] + ", but got: " + code.getModule(x, y) + " for (" + x + ", " + y + ")");
        assertEquals(code.getModule(x, y), expected[y + (x * code.getSize())]);
      }
    }
  }

  @Test
  public void testEncodeSegmentsLowNumeric() {
    String value = "0123456789";
    List<QrSegment> segments = QrSegment.makeSegments(value);
    QrCode code = QrCode.encodeSegments(segments, Ecc.LOW);

    assertEquals(code.getSize(), 21);
    Boolean[] expected = {
      true, true, true, true, true, true, true, false, false, false, false, false, false, false, true, true, true, true, true, true, true,
      true, false, false, false, false, false, true, false, false, false, false, true, true, false, true, false, false, false, false, false, true,
      true, false, true, true, true, false, true, false, false, true, true, false, false, false, true, false, true, true, true, false, true,
      true, false, true, true, true, false, true, false, true, false, false, true, true, false, true, false, true, true, true, false, true,
      true, false, true, true, true, false, true, false, true, true, true, true, false, false, true, false, true, true, true, false, true,
      true, false, false, false, false, false, true, false, false, true, false, true, true, false, true, false, false, false, false, false, true,
      true, true, true, true, true, true, true, false, true, false, true, false, true, false, true, true, true, true, true, true, true,
      false, false, false, false, false, false, false, false, true, false, true, true, true, false, false, false, false, false, false, false, false,
      false, false, true, true, false, false, true, false, false, false, true, false, false, true, true, false, true, true, false, false, false,
      true, false, false, true, true, false, false, false, true, true, false, false, false, true, true, true, false, true, false, true, false,
      false, true, true, false, true, true, true, true, false, true, false, true, true, true, true, false, false, false, true, true, true,
      false, true, false, false, true, true, false, false, false, false, false, true, true, false, false, false, true, false, true, true, false,
      false, true, true, true, false, false, true, true, true, false, true, false, true, true, true, false, false, false, false, false, false,
      false, false, false, false, false, false, false, false, false, true, false, true, false, true, false, true, true, false, true, true, true,
      true, true, true, true, true, true, true, false, false, false, false, false, true, true, true, false, true, false, true, false, true,
      true, false, false, false, false, false, true, false, false, true, true, true, true, false, false, false, false, false, true, false, true,
      true, false, true, true, true, false, true, false, false, true, false, true, true, false, false, false, true, true, true, false, true,
      true, false, true, true, true, false, true, false, true, true, false, false, true, true, false, false, true, false, true, false, true,
      true, false, true, true, true, false, true, false, true, true, true, true, false, true, false, false, false, true, false, false, true,
      true, false, false, false, false, false, true, false, false, false, true, false, true, true, true, false, false, false, true, false, true,
      true, true, true, true, true, true, true, false, false, true, false, false, true, true, true, false, true, false, true, true, false };

    for (int x = 0; x < code.getSize(); ++x) {
      for (int y = 0; y < code.getSize(); ++y) {
        assertEquals(code.getModule(x, y), expected[y + (x * code.getSize())]);
      }
    }
  }
}
