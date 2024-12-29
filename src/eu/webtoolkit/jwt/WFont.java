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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A value class that describes a font. */
public class WFont {
  private static Logger logger = LoggerFactory.getLogger(WFont.class);

  /** A default font (dependent on the user agent). */
  public WFont() {
    this.widget_ = null;
    this.genericFamily_ = FontFamily.Default;
    this.specificFamilies_ = new WString();
    this.style_ = FontStyle.Normal;
    this.variant_ = FontVariant.Normal;
    this.weight_ = FontWeight.Normal;
    this.weightValue_ = 400;
    this.size_ = FontSize.Medium;
    this.sizeLength_ = new WLength();
    this.familyChanged_ = false;
    this.styleChanged_ = false;
    this.variantChanged_ = false;
    this.weightChanged_ = false;
    this.sizeChanged_ = false;
  }
  /**
   * A font of a given family.
   *
   * <p>Creates a Medium font of the given family.
   */
  public WFont(FontFamily family) {
    this.widget_ = null;
    this.genericFamily_ = family;
    this.specificFamilies_ = new WString();
    this.style_ = FontStyle.Normal;
    this.variant_ = FontVariant.Normal;
    this.weight_ = FontWeight.Normal;
    this.weightValue_ = 400;
    this.size_ = FontSize.Medium;
    this.sizeLength_ = new WLength();
    this.familyChanged_ = false;
    this.styleChanged_ = false;
    this.variantChanged_ = false;
    this.weightChanged_ = false;
    this.sizeChanged_ = false;
  }
  /** Indicates whether some other object is "equal to" this one. */
  public boolean equals(final WFont other) {
    return this.genericFamily_ == other.genericFamily_
        && (this.specificFamilies_.toString().equals(other.specificFamilies_.toString()))
        && this.style_ == other.style_
        && this.variant_ == other.variant_
        && this.weight_ == other.weight_
        && this.weightValue_ == other.weightValue_
        && this.size_ == other.size_
        && this.sizeLength_.equals(other.sizeLength_);
  }
  /**
   * Sets the font family.
   *
   * <p>The font family is specified using a generic family name, in addition to a comma-separated
   * list of specific font choices.
   *
   * <p>The first specific font that can be matched will be used, otherwise a generic font will be
   * used.
   *
   * <p>Careful, for a font family name that contains a space, you need to add quotes, to {@link
   * WFont#setFamily(FontFamily genericFamily, CharSequence specificFamilies) setFamily()}, e.g.
   *
   * <p>
   *
   * <pre>{@code
   * WFont mono;
   * mono.setFamily(FontFamily::Monospace, "'Courier New'");
   * mono.setSize(18);
   *
   * }</pre>
   */
  public void setFamily(FontFamily genericFamily, final CharSequence specificFamilies) {
    this.genericFamily_ = genericFamily;
    this.specificFamilies_ = WString.toWString(specificFamilies);
    this.familyChanged_ = true;
    if (this.widget_ != null) {
      this.widget_.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /**
   * Sets the font family.
   *
   * <p>Calls {@link #setFamily(FontFamily genericFamily, CharSequence specificFamilies)
   * setFamily(genericFamily, new WString())}
   */
  public final void setFamily(FontFamily genericFamily) {
    setFamily(genericFamily, new WString());
  }
  /** Returns the font generic family. */
  public FontFamily getGenericFamily() {
    return this.genericFamily_;
  }
  /** Returns the font specific family names. */
  public WString getSpecificFamilies() {
    return this.specificFamilies_;
  }
  /** Sets the font style. */
  public void setStyle(FontStyle style) {
    this.style_ = style;
    this.styleChanged_ = true;
    if (this.widget_ != null) {
      this.widget_.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /** Returns the font style. */
  public FontStyle getStyle() {
    return this.style_;
  }
  /** Sets the font variant. */
  public void setVariant(FontVariant variant) {
    this.variant_ = variant;
    this.variantChanged_ = true;
    if (this.widget_ != null) {
      this.widget_.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /** Returns the font variant. */
  public FontVariant getVariant() {
    return this.variant_;
  }
  /**
   * Sets the font weight.
   *
   * <p>When setting weight == Value, you may specify a value.
   *
   * <p>Valid values are between 100 and 900, and are rounded to multiples of 100.
   */
  public void setWeight(FontWeight weight, int value) {
    this.weight_ = weight;
    this.weightValue_ = value;
    this.weightChanged_ = true;
    if (this.widget_ != null) {
      this.widget_.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /**
   * Sets the font weight.
   *
   * <p>Calls {@link #setWeight(FontWeight weight, int value) setWeight(weight, 400)}
   */
  public final void setWeight(FontWeight weight) {
    setWeight(weight, 400);
  }
  /** Returns the font weight. */
  public FontWeight getWeight() {
    if (this.weight_ != FontWeight.Value) {
      return this.weight_;
    } else {
      return this.weightValue_ >= 700 ? FontWeight.Bold : FontWeight.Normal;
    }
  }
  /** Returns the font weight value. */
  public int getWeightValue() {
    switch (this.weight_) {
      case Normal:
      case Lighter:
        return 400;
      case Bold:
      case Bolder:
        return 700;
      case Value:
        return this.weightValue_;
    }
    assert false;
    return -1;
  }
  /**
   * Sets the font size.
   *
   * <p>Sets the font size using a predefined CSS size.
   */
  public void setSize(FontSize size) {
    this.size_ = size;
    this.sizeLength_ = WLength.Auto;
    this.sizeChanged_ = true;
    if (this.widget_ != null) {
      this.widget_.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /**
   * Sets the font size.
   *
   * <p>Sets the font size.
   */
  public void setSize(final WLength size) {
    this.size_ = FontSize.FixedSize;
    this.sizeLength_ = size;
    this.sizeChanged_ = true;
    if (this.widget_ != null) {
      this.widget_.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
  }
  /** Returns the font size. */
  public FontSize getSize(double mediumSize) {
    if (this.size_ != FontSize.FixedSize) {
      return this.size_;
    } else {
      double pixels = this.sizeLength_.toPixels();
      if (pixels == mediumSize) {
        return FontSize.Medium;
      } else {
        if (pixels > mediumSize) {
          if (pixels < 1.2 * 1.19 * mediumSize) {
            return FontSize.Large;
          } else {
            if (pixels < 1.2 * 1.2 * 1.19 * mediumSize) {
              return FontSize.XLarge;
            } else {
              return FontSize.XXLarge;
            }
          }
        } else {
          if (pixels > mediumSize / 1.2 / 1.19) {
            return FontSize.Small;
          } else {
            if (pixels > mediumSize / 1.2 / 1.2 / 1.19) {
              return FontSize.XSmall;
            } else {
              return FontSize.XXSmall;
            }
          }
        }
      }
    }
  }
  /**
   * Returns the font size.
   *
   * <p>Returns {@link #getSize(double mediumSize) getSize(16)}
   */
  public final FontSize getSize() {
    return getSize(16);
  }
  /**
   * Returns the font size as a numerical value.
   *
   * <p>{@link PositionScheme#Absolute} size enumerations are converted to a length assuming a
   * Medium font size of 16 px.
   */
  public WLength getSizeLength(double mediumSize) {
    switch (this.size_) {
      case FixedSize:
        return this.sizeLength_;
      case XXSmall:
        return new WLength(mediumSize / 1.2 / 1.2 / 1.2);
      case XSmall:
        return new WLength(mediumSize / 1.2 / 1.2);
      case Small:
        return new WLength(mediumSize / 1.2);
      case Medium:
        return new WLength(mediumSize);
      case Large:
        return new WLength(mediumSize * 1.2);
      case XLarge:
        return new WLength(mediumSize * 1.2 * 1.2);
      case XXLarge:
        return new WLength(mediumSize * 1.2 * 1.2 * 1.2);
      case Smaller:
        return new WLength(1 / 1.2, LengthUnit.FontEm);
      case Larger:
        return new WLength(1.2, LengthUnit.FontEm);
    }
    assert false;
    return new WLength();
  }
  /**
   * Returns the font size as a numerical value.
   *
   * <p>Returns {@link #getSizeLength(double mediumSize) getSizeLength(16)}
   */
  public final WLength getSizeLength() {
    return getSizeLength(16);
  }

  String getCssText(boolean combined) {
    StringBuilder result = new StringBuilder();
    if (combined) {
      String s = "";
      s = this.cssStyle(false);
      if (s.length() != 0) {
        result.append(s).append(' ');
      }
      s = this.cssVariant(false);
      if (s.length() != 0) {
        result.append(s).append(' ');
      }
      s = this.cssWeight(false);
      if (s.length() != 0) {
        result.append(s).append(' ');
      }
      result.append(this.cssSize(true)).append(' ');
      s = this.cssFamily(true);
      if (s.length() != 0) {
        result.append(s).append(' ');
      } else {
        result.append(s).append(" inherit");
      }
    } else {
      String s = "";
      s = this.cssSize(false);
      if (s.length() != 0) {
        result.append("font-size: ").append(s).append(";");
      }
      s = this.cssStyle(false);
      if (s.length() != 0) {
        result.append("font-style: ").append(s).append(";");
      }
      s = this.cssVariant(false);
      if (s.length() != 0) {
        result.append("font-variant: ").append(s).append(";");
      }
      s = this.cssWeight(false);
      if (s.length() != 0) {
        result.append("font-weight: ").append(s).append(";");
      }
      s = this.cssFamily(false);
      if (s.length() != 0) {
        result.append("font-family: ").append(s).append(";");
      }
    }
    return result.toString();
  }

  final String getCssText() {
    return getCssText(true);
  }

  public void updateDomElement(final DomElement element, boolean fontall, boolean all) {
    if (this.familyChanged_ || fontall || all) {
      String family = this.cssFamily(fontall);
      if (family.length() != 0) {
        element.setProperty(Property.StyleFontFamily, family);
      }
      this.familyChanged_ = false;
    }
    if (this.styleChanged_ || fontall || all) {
      String style = this.cssStyle(fontall);
      if (style.length() != 0) {
        element.setProperty(Property.StyleFontStyle, style);
      }
      this.styleChanged_ = false;
    }
    if (this.variantChanged_ || fontall || all) {
      String variant = this.cssVariant(fontall);
      if (variant.length() != 0) {
        element.setProperty(Property.StyleFontVariant, variant);
      }
      this.variantChanged_ = false;
    }
    if (this.weightChanged_ || fontall || all) {
      String weight = this.cssWeight(fontall);
      if (weight.length() != 0) {
        element.setProperty(Property.StyleFontWeight, weight);
      }
      this.weightChanged_ = false;
    }
    if (this.sizeChanged_ || fontall || all) {
      String size = this.cssSize(fontall);
      if (size.length() != 0) {
        element.setProperty(Property.StyleFontSize, size);
      }
      this.sizeChanged_ = false;
    }
  }

  private WWebWidget widget_;
  private FontFamily genericFamily_;
  private WString specificFamilies_;
  private FontStyle style_;
  private FontVariant variant_;
  private FontWeight weight_;
  private int weightValue_;
  private FontSize size_;
  private WLength sizeLength_;
  private boolean familyChanged_;
  private boolean styleChanged_;
  private boolean variantChanged_;
  private boolean weightChanged_;
  private boolean sizeChanged_;

  private String cssStyle(boolean all) {
    switch (this.style_) {
      case Normal:
        if (this.styleChanged_ || all) {
          return "normal";
        }
        break;
      case Italic:
        return "italic";
      case Oblique:
        return "oblique";
    }
    return "";
  }

  private String cssVariant(boolean all) {
    switch (this.variant_) {
      case Normal:
        if (this.variantChanged_ || all) {
          return "normal";
        }
        break;
      case SmallCaps:
        return "small-caps";
    }
    return "";
  }

  private String cssWeight(boolean all) {
    switch (this.weight_) {
      case Normal:
        if (this.weightChanged_ || all) {
          return "normal";
        }
        break;
      case Bold:
        return "bold";
      case Bolder:
        return "bolder";
      case Lighter:
        return "lighter";
      case Value:
        {
          int v = Math.min(900, Math.max(100, this.weightValue_ / 100 * 100));
          return String.valueOf(v);
        }
    }
    return "";
  }

  private String cssFamily(boolean anon1) {
    String family = this.specificFamilies_.toString();
    if (family.length() != 0 && this.genericFamily_ != FontFamily.Default) {
      family += ',';
    }
    switch (this.genericFamily_) {
      case Default:
        break;
      case Serif:
        family += "serif";
        break;
      case SansSerif:
        family += "sans-serif";
        break;
      case Cursive:
        family += "cursive";
        break;
      case Fantasy:
        family += "fantasy";
        break;
      case Monospace:
        family += "monospace";
        break;
    }
    return family;
  }

  private String cssSize(boolean all) {
    switch (this.size_) {
      case Medium:
        if (this.sizeChanged_ || all) {
          return "medium";
        }
        break;
      case XXSmall:
        return "xx-small";
      case XSmall:
        return "x-small";
      case Small:
        return "small";
      case Large:
        return "large";
      case XLarge:
        return "x-large";
      case XXLarge:
        return "xx-large";
      case Smaller:
        return "smaller";
      case Larger:
        return "larger";
      case FixedSize:
        return this.sizeLength_.getCssText();
    }
    return "";
  }

  void setWebWidget(WWebWidget w) {
    this.widget_ = w;
  }
}
