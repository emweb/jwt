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

/**
 * A value class that describes a CSS length.
 *
 * <p>The class combines a value with a unit. There is a special value <i>auto</i> which has a
 * different meaning depending on the context.
 */
public class WLength {
  private static Logger logger = LoggerFactory.getLogger(WLength.class);

  /**
   * An &apos;auto&apos; length.
   *
   * <p>
   *
   * @see WLength#WLength()
   */
  public static WLength Auto = new WLength();
  /**
   * Creates an &apos;auto&apos; length.
   *
   * <p>Specifies an &apos;auto&apos; length.
   *
   * <p>
   *
   * @see WLength#Auto
   */
  public WLength() {
    this.auto_ = true;
    this.unit_ = LengthUnit.Pixel;
    this.value_ = -1;
  }
  /**
   * Creates a length by parsing the argument as a css length string.
   *
   * <p>This supports all CSS length formats that have an API counterpart.
   */
  public WLength(final String str) {
    this.parseCssString(str);
  }
  /**
   * Creates a length with value and unit.
   *
   * <p>This constructor is also used for the implicit conversion of a double to a {@link WLength},
   * assuming a pixel unit.
   */
  public WLength(double value, LengthUnit unit) {
    this.auto_ = false;
    this.value_ = value;
    this.setUnit(unit);
  }
  /**
   * Creates a length with value and unit.
   *
   * <p>Calls {@link #WLength(double value, LengthUnit unit) this(value, LengthUnit.Pixel)}
   */
  public WLength(double value) {
    this(value, LengthUnit.Pixel);
  }
  /**
   * Returns whether the length is &apos;auto&apos;.
   *
   * <p>
   *
   * @see WLength#WLength()
   * @see WLength#Auto
   */
  public boolean isAuto() {
    return this.auto_;
  }
  /**
   * Returns the value.
   *
   * <p>
   *
   * @see WLength#getUnit()
   */
  public double getValue() {
    return this.value_;
  }
  /**
   * Returns the unit.
   *
   * <p>
   *
   * @see WLength#getValue()
   */
  public LengthUnit getUnit() {
    return this.unit_;
  }
  /** Returns the CSS text. */
  public String getCssText() {
    if (this.auto_) {
      return "auto";
    } else {
      return String.valueOf(this.value_) + unitText[(int) this.unit_.getValue()];
    }
  }
  /** Indicates whether some other object is "equal to" this one. */
  public boolean equals(final WLength other) {
    return this.auto_ == other.auto_ && this.unit_ == other.unit_ && this.value_ == other.value_;
  }
  /**
   * Returns the (approximate) length in pixels.
   *
   * <p>When the length {@link WLength#isAuto() isAuto()}, 0 is returned, otherwise the approximate
   * length in pixels.
   *
   * <p>
   *
   * <p><i><b>Note: </b>For percentages ({@link LengthUnit#Percentage}), and units relative to
   * viewport size ({@link LengthUnit#ViewportWidth}, {@link LengthUnit#ViewportHeight}, {@link
   * LengthUnit#ViewportMin}, {@link LengthUnit#ViewportMax}), a percentage of the font size is
   * used. </i>
   */
  public double toPixels(double fontSize) {
    if (this.auto_) {
      return 0;
    } else {
      if (this.unit_ == LengthUnit.FontEm) {
        return this.value_ * fontSize;
      } else {
        if (this.unit_ == LengthUnit.FontEx) {
          return this.value_ * fontSize / 2.0;
        } else {
          if (this.unit_ == LengthUnit.Percentage
              || this.unit_ == LengthUnit.ViewportWidth
              || this.unit_ == LengthUnit.ViewportHeight
              || this.unit_ == LengthUnit.ViewportMin
              || this.unit_ == LengthUnit.ViewportMax) {
            return this.value_ * fontSize / 100.0;
          } else {
            return this.value_ * unitFactor[(int) this.unit_.getValue() - 2];
          }
        }
      }
    }
  }
  /**
   * Returns the (approximate) length in pixels.
   *
   * <p>Returns {@link #toPixels(double fontSize) toPixels(16.0)}
   */
  public final double toPixels() {
    return toPixels(16.0);
  }

  private boolean auto_;
  private LengthUnit unit_;
  private double value_;

  private void setUnit(LengthUnit unit) {
    this.unit_ = unit;
  }

  private void parseCssString(String s) {
    this.auto_ = false;
    this.unit_ = LengthUnit.Pixel;
    this.value_ = -1;
    if ("auto".equals(s)) {
      this.auto_ = true;
      return;
    }
    String end = null;
    {
      Matcher matcher = StringUtils.FLOAT_PATTERN.matcher(s);
      this.value_ = 0.0;
      if (matcher.find()) {
        end = s.substring(matcher.end());
        this.value_ = Double.parseDouble(matcher.group().trim());
      } else {
        end = s;
      }
    }
    ;

    if (s == end) {
      logger.error(
          new StringWriter().append("cannot parse CSS length: '").append(s).append("'").toString());
      this.auto_ = true;
      return;
    }
    String unit = end;
    unit = unit.trim();
    if (unit.equals("em")) {
      this.unit_ = LengthUnit.FontEm;
    } else {
      if (unit.equals("ex")) {
        this.unit_ = LengthUnit.FontEx;
      } else {
        if (unit.length() == 0 || unit.equals("px")) {
          this.unit_ = LengthUnit.Pixel;
        } else {
          if (unit.equals("in")) {
            this.unit_ = LengthUnit.Inch;
          } else {
            if (unit.equals("cm")) {
              this.unit_ = LengthUnit.Centimeter;
            } else {
              if (unit.equals("mm")) {
                this.unit_ = LengthUnit.Millimeter;
              } else {
                if (unit.equals("pt")) {
                  this.unit_ = LengthUnit.Point;
                } else {
                  if (unit.equals("pc")) {
                    this.unit_ = LengthUnit.Pica;
                  } else {
                    if (unit.equals("%")) {
                      this.unit_ = LengthUnit.Percentage;
                    } else {
                      if (unit.equals("vw")) {
                        this.unit_ = LengthUnit.ViewportWidth;
                      } else {
                        if (unit.equals("vh")) {
                          this.unit_ = LengthUnit.ViewportHeight;
                        } else {
                          if (unit.equals("vmin")) {
                            this.unit_ = LengthUnit.ViewportMin;
                          } else {
                            if (unit.equals("vmax")) {
                              this.unit_ = LengthUnit.ViewportMax;
                            } else {
                              logger.error(
                                  new StringWriter()
                                      .append("unrecognized unit in '")
                                      .append(s)
                                      .append("'")
                                      .toString());
                              this.auto_ = true;
                              this.value_ = -1;
                              this.unit_ = LengthUnit.Pixel;
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private static String[] unitText = {
    "em", "ex", "px", "in", "cm", "mm", "pt", "pc", "%", "vw", "vh", "vmin", "vmax"
  };
  private static final double pxPerPt = 4.0 / 3.0;
  private static double[] unitFactor = {
    1, 72 * pxPerPt, 72 / 2.54 * pxPerPt, 72 / 25.4 * pxPerPt, pxPerPt, 12 * pxPerPt
  };

  static WLength multiply(final WLength l, double s) {
    return new WLength(l.getValue() * s, l.getUnit());
  }

  static WLength multiply(double s, final WLength l) {
    return WLength.multiply(l, s);
  }

  static WLength divide(final WLength l, double s) {
    return WLength.multiply(l, 1 / s);
  }
}
