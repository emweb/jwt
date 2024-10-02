package eu.webtoolkit.jwt.thirdparty.qrcodegen;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.List;

public class QrSegmentTest {
	@Test
	public void testMakeNumericEmpty() {
    String value = "";
    QrSegment segment = QrSegment.makeNumeric(value);

    QrSegment.Mode m = segment.getMode();
    assertEquals(m.getModeBits(), 0x1);
    assertEquals(segment.getNumChars(), 0);
    assertEquals(segment.getData().size(), 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMakeNumericNonNumber() {
    String value = "01234A56789";
    QrSegment.makeNumeric(value);
	}

	@Test
	public void testMakeNumeric() {
    String value = "0123456789";
    QrSegment segment = QrSegment.makeNumeric(value);

    QrSegment.Mode m = segment.getMode();
    assertEquals(m.getModeBits(), 0x1);
    assertEquals(segment.getNumChars(), 10);
    assertEquals(segment.getData().size(), 34);
    Boolean[] expected = {
      false, false, false, false, false, false, true, true, false, false,
      false, true, false, true, false, true, true, false, false, true,
      true, false, true, false, true, false, false, true, true, false,
      true,false, false, true };
    assertEquals(expected, segment.getData().toArray());
	}

	@Test
	public void testMakeAlphanumericEmpty() {
    String value = "";
    QrSegment segment = QrSegment.makeAlphanumeric(value);

    QrSegment.Mode m = segment.getMode();
    assertEquals(m.getModeBits(), 0x2);
    assertEquals(segment.getNumChars(), 0);
    assertEquals(segment.getData().size(), 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMakeAlphanumericNonAlpahnumeric() {
    // Charset: 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:
    String value = "0123456789 ABCD$%+-./\\";
    QrSegment.makeNumeric(value);
	}

	@Test
	public void testMakeAlphanumeric() {
    String value = "0123456789 ABCD$%+-./";
    QrSegment segment = QrSegment.makeAlphanumeric(value);

    QrSegment.Mode m = segment.getMode();
    assertEquals(m.getModeBits(), 0x2);
    assertEquals(segment.getNumChars(), 21);
    assertEquals(segment.getData().size(), 116);
    Boolean[] expected = {
      false, false, false, false, false, false, false, false, false, false,
      true, false, false, false, false, true, false, true, true, true,
      false, true, false, false, false, true, false, true, true, true,
      false, false, true, false, false, true, false, false, false, true,
      false, true, false, true, false, false, true, false, true, true,
      true, false, false, false, true, true, true, false, false, true,
      false, true, true, true, true, false, false, false, true, true,
      true, true, true, true, false, true, true, false, true, false,
      false, true, true, false, true, true, true, false, true, true,
      false, true, true, false, true, false, true, true, false, true,
      true, true, false, true, false, true, true, true, true, true,
      true, false, true, false, true, true };
    assertEquals(expected, segment.getData().toArray());
	}

  @Test
  public void testMakeSegmentsNumeric() {
    String value = "0123456789";
    List<QrSegment> segments = QrSegment.makeSegments(value);
    QrSegment segment = segments.get(0);

    QrSegment.Mode m = segment.getMode();
    assertEquals(m.getModeBits(), 0x1);
    assertEquals(segment.getNumChars(), 10);
    assertEquals(segment.getData().size(), 34);
    Boolean[] expected = {
      false, false, false, false, false, false, true, true, false, false,
      false, true, false, true, false, true, true, false, false, true,
      true, false, true, false, true, false, false, true, true, false,
      true, false, false, true };
    assertEquals(expected, segment.getData().toArray());
  }

  @Test
  public void testMakeSegmentsAlphanumeric() {
    String value = "0123456789 ABCD$%+-./";
    List<QrSegment> segments = QrSegment.makeSegments(value);
    QrSegment segment = segments.get(0);

    QrSegment.Mode m = segment.getMode();
    assertEquals(m.getModeBits(), 0x2);
    assertEquals(segment.getNumChars(), 21);
    assertEquals(segment.getData().size(), 116);
    Boolean[] expected = {
      false, false, false, false, false, false, false, false, false, false,
      true, false, false, false, false, true, false, true, true, true,
      false, true, false, false, false, true, false, true, true, true,
      false, false, true, false, false, true, false, false, false, true,
      false, true, false, true, false, false, true, false, true, true,
      true, false, false, false, true, true, true, false, false, true,
      false, true, true, true, true, false, false, false, true, true,
      true, true, true, true, false, true, true, false, true, false,
      false, true, true, false, true, true, true, false, true, true,
      false, true, true, false, true, false, true, true, false, true,
      true, true, false, true, false, true, true, true, true, true,
      true, false, true, false, true, true };
    assertEquals(expected, segment.getData().toArray());
  }
}
