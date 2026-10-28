/*
 * Copyright (C) 2026 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class WLengthTest {
  private static final LengthUnit[] CONTENT_SIZING_UNITS = {
    LengthUnit.FitContent, LengthUnit.MaxContent, LengthUnit.MinContent
  };

  private static final String[] CONTENT_SIZING_KEYWORDS = {
    "fit-content", "max-content", "min-content"
  };

  @Test
  public void testParseContentSizingKeywords() {
    for (int i = 0; i < CONTENT_SIZING_UNITS.length; ++i) {
      WLength length = new WLength("  " + CONTENT_SIZING_KEYWORDS[i] + "  ");

      assertFalse(length.isAuto());
      assertEquals(CONTENT_SIZING_UNITS[i], length.getUnit());
      assertEquals(-1.0, length.getValue(), 0.0);
      assertEquals(CONTENT_SIZING_KEYWORDS[i], length.getCssText());
      assertEquals(0.0, length.toPixels(), 0.0);
    }
  }

  @Test
  public void testConstructContentSizingKeywordsWithNumericValue() {
    for (int i = 0; i < CONTENT_SIZING_UNITS.length; ++i) {
      WLength length = new WLength(42.0, CONTENT_SIZING_UNITS[i]);

      assertFalse(length.isAuto());
      assertEquals(CONTENT_SIZING_UNITS[i], length.getUnit());
      assertEquals(-1.0, length.getValue(), 0.0);
      assertEquals(CONTENT_SIZING_KEYWORDS[i], length.getCssText());
      assertEquals(0.0, length.toPixels(), 0.0);
    }
  }

  @Test
  public void testConstructContentSizingKeywordsWithoutNumericValue() {
    for (int i = 0; i < CONTENT_SIZING_UNITS.length; ++i) {
      WLength length = new WLength(CONTENT_SIZING_UNITS[i]);

      assertFalse(length.isAuto());
      assertEquals(CONTENT_SIZING_UNITS[i], length.getUnit());
      assertEquals(-1.0, length.getValue(), 0.0);
      assertEquals(CONTENT_SIZING_KEYWORDS[i], length.getCssText());
      assertEquals(0.0, length.toPixels(), 0.0);
    }
  }

  @Test
  public void testKeywordConstructorRejectsNumericUnits() {
    try {
      new WLength(LengthUnit.Pixel);
      fail("Expected WLength(LengthUnit.Pixel) to throw");
    } catch (WException expected) {
    }
  }

  @Test
  public void testAutoAndPixelLengthsAreUnchanged() {
    WLength auto = new WLength();
    assertTrue(auto.isAuto());
    assertEquals("auto", auto.getCssText());
    assertEquals(0.0, auto.toPixels(), 0.0);

    WLength pixels = new WLength(42.0);
    assertFalse(pixels.isAuto());
    assertEquals(LengthUnit.Pixel, pixels.getUnit());
    assertEquals(42.0, pixels.getValue(), 0.0);
    assertEquals("42.0px", pixels.getCssText());
    assertEquals(42.0, pixels.toPixels(), 0.0);
  }

  @Test
  public void testInvalidContentSizingKeywordsAreAuto() {
    assertTrue(new WLength("FIT-CONTENT").isAuto());
    assertTrue(new WLength("fit-content(10px)").isAuto());
  }
}
