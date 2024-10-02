/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
 * Standard colorMap.
 *
 * <p>The WStandardColorMap is defined by a list of value-to-color pairs. The WStandardColorMap has
 * two modes: a continuous mode, in which the colors are linearly interpolated in between the pair
 * values, and a non-continuous mode, where the values are not interpolated, so that the colormap
 * has a banded effect. In non-continuous mode, the color of a given point P is the color of the
 * pair with the largest value smaller than P.
 *
 * <p>Numerical values above the maximum value in the list map to the maximum value&apos;s color,
 * all values below the minimum value in the list map to the minimum value&apos;s color. The range
 * indicated by the minimum and maximum passed to the constructor determines which part of the
 * colormap is drawn by {@link WStandardColorMap#createStrip(WPainter painter, WRectF area)
 * createStrip()} or {@link WStandardColorMap#paintLegend(WPainter painter, WRectF area)
 * paintLegend()}.
 *
 * <p>The figure below illustrates the possible colormaps that can be constructed from the list
 * {&quot;0.0 - StandardColor::DarkRed&quot;, &quot;1.0 - StandardColor::Red&quot;, &quot;2.0 -
 * StandardColor::Gray&quot;}. The discrete map (on the left) has the range [0, 3], the continuous
 * map (on the right) has the range [0, 2]. The utility method {@link
 * WStandardColorMap#discretise(int numberOfBands) discretise()} is also applied to the continuous
 * colormap to obtain a colormap with 5 bands in the same range.
 *
 * <p><div align="center"> <img src="doc-files/standardcolormaps.png">
 *
 * <p><strong>Different uses of WStandardColorMap</strong> </div>
 */
public class WStandardColorMap extends WAbstractColorMap {
  private static Logger logger = LoggerFactory.getLogger(WStandardColorMap.class);

  /** Contains a pair of a numerical value and a {@link WColor}. */
  public static class Pair {
    private static Logger logger = LoggerFactory.getLogger(Pair.class);

    public Pair() {
      this.color_ = new WColor();
    }
    /** Constructs a WStandardColorMap::Pair from a double and a {@link WColor}. */
    public Pair(double val, WColor col) {
      this.value_ = val;
      this.color_ = col;
    }

    public void setValue(double value) {
      this.value_ = value;
    }

    public double getValue() {
      return this.value_;
    }

    public void setColor(final WColor color) {
      this.color_ = color;
    }

    public WColor getColor() {
      return this.color_;
    }

    private double value_;
    private WColor color_;
  }
  /**
   * Construct a default colormap.
   *
   * <p>The default colormap is a transition from yellow to red. The color-scheme was taken from <a
   * href="http://www.personal.psu.edu/faculty/c/a/cab38/ColorBrewer/ColorBrewer.html">ColorBrewer</a>,
   * which contains lots of useful info and example schemes you might want to use for your custom
   * colormaps.
   */
  public WStandardColorMap(double min, double max, boolean continuous) {
    super(min, max);
    this.continuous_ = continuous;
    this.colors_ = new ArrayList<WStandardColorMap.Pair>();
    double interval;
    if (this.continuous_) {
      interval = (this.max_ - this.min_) / 4;
    } else {
      interval = (this.max_ - this.min_) / 5;
    }
    this.colors_.add(new WStandardColorMap.Pair(this.min_, new WColor(255, 255, 178)));
    this.colors_.add(
        new WStandardColorMap.Pair(this.min_ + 1 * interval, new WColor(254, 204, 92)));
    this.colors_.add(
        new WStandardColorMap.Pair(this.min_ + 2 * interval, new WColor(253, 141, 60)));
    this.colors_.add(new WStandardColorMap.Pair(this.min_ + 3 * interval, new WColor(240, 59, 32)));
    this.colors_.add(new WStandardColorMap.Pair(this.min_ + 4 * interval, new WColor(189, 0, 38)));
  }
  /**
   * Construct a custom colormap.
   *
   * <p>This constructor allows you to pass a list of value-to-color pairs that define a colormap as
   * described in the class description.
   */
  public WStandardColorMap(
      double min, double max, final List<WStandardColorMap.Pair> colors, boolean continuous) {
    super(min, max);
    this.continuous_ = continuous;
    this.colors_ = new ArrayList<WStandardColorMap.Pair>();
    double prev = -Double.MAX_VALUE;
    for (int i = 0; i < colors.size(); i++) {
      double val = colors.get(i).getValue();
      if (val < prev) {
        throw new WException("WStandardColorMap: the provided vector is not sorted");
      }
      prev = val;
    }
    Utils.copyList(colors, this.colors_);
  }
  /**
   * Utility method to discretise a continuous colormap in a number of equally sized bands.
   *
   * <p>This method makes a new list of value-to-color pairs by discretising the linear
   * interpolation of the previous one into numberOfBands equally sized colorbands. This method only
   * has effect if the colormap is continuous.
   */
  public void discretise(int numberOfBands) {
    if (!this.continuous_ || this.colors_.size() <= 1) {
      return;
    }
    double val0 = this.colors_.get(0).getValue();
    double interval =
        (this.colors_.get(this.colors_.size() - 1).getValue() - this.colors_.get(0).getValue())
            / numberOfBands;
    List<WStandardColorMap.Pair> newColors = new ArrayList<WStandardColorMap.Pair>();
    for (int i = 0; i < numberOfBands; i++) {
      WStandardColorMap.Pair newCol =
          new WStandardColorMap.Pair(
              val0 + i * interval, this.toColor(val0 + i * interval + interval / 2));
      newColors.add(newCol);
    }
    Utils.copyList(newColors, this.colors_);
    this.continuous_ = false;
  }

  public boolean isContinuous() {
    return this.continuous_;
  }

  public List<WStandardColorMap.Pair> getColorValuePairs() {
    return this.colors_;
  }

  public WColor toColor(double value) {
    if (this.colors_.size() == 0) {
      return new WColor();
    }
    if (value < this.colors_.get(0).getValue()) {
      return this.colors_.get(0).getColor();
    } else {
      if (value >= this.colors_.get(this.colors_.size() - 1).getValue()) {
        return this.colors_.get(this.colors_.size() - 1).getColor();
      }
    }
    int i = 0;
    for (; i < this.colors_.size(); i++) {
      if (value < this.colors_.get(i).getValue()) {
        break;
      }
    }
    if (this.continuous_) {
      WStandardColorMap.Pair mapVal1 = this.colors_.get(i - 1);
      WStandardColorMap.Pair mapVal2 = this.colors_.get(i);
      double factor = (value - mapVal1.getValue()) / (mapVal2.getValue() - mapVal1.getValue());
      return this.interpolate(mapVal1.getColor(), mapVal2.getColor(), factor);
    } else {
      return this.colors_.get(i - 1).getColor();
    }
  }

  public void createStrip(WPainter painter, final WRectF area) {
    painter.save();
    painter.setRenderHint(RenderHint.Antialiasing, false);
    int width;
    int height;
    if ((area == null)) {
      width = (int) painter.getDevice().getWidth().getValue();
      height = (int) painter.getDevice().getHeight().getValue();
    } else {
      painter.translate((int) area.getX(), (int) area.getY());
      width = (int) area.getWidth();
      height = (int) area.getHeight();
    }
    double valueInterval = (this.max_ - this.min_) / height;
    double offset = valueInterval / 2;
    for (int i = 0; i < height; i++) {
      WColor color = this.toColor(this.min_ + offset + i * valueInterval);
      painter.setBrush(new WBrush(color));
      WPen linePen = new WPen(color);
      linePen.setWidth(new WLength(1));
      painter.setPen(linePen);
      painter.drawLine(0, height - (0.5 + i), width, height - (0.5 + i));
    }
    painter.restore();
  }

  public void paintLegend(WPainter painter, final WRectF area) {
    painter.save();
    WPainterPath clipPath = new WPainterPath();
    painter.setRenderHint(RenderHint.Antialiasing, false);
    painter.setFont(this.labelFont_);
    int height;
    if ((area == null)) {
      height = (int) painter.getDevice().getHeight().getValue();
    } else {
      clipPath.addRect(area);
      painter.setClipPath(clipPath);
      painter.setClipping(true);
      painter.translate(area.getX(), area.getY());
      height = (int) area.getHeight();
    }
    int textHeight = (int) painter.getFont().getSizeLength().toPixels();
    int stripWidth = 50;
    this.createStrip(
        painter,
        new WRectF(0, (int) (textHeight / 2 + 0.5), (int) stripWidth, (int) (height - textHeight)));
    painter.setPen(new WPen());
    painter.setBrush(new WBrush());
    painter.drawRect(
        new WRectF(0.5, (int) (textHeight / 2) + 0.5, stripWidth, height - textHeight));
    painter.translate(stripWidth, textHeight / 2);
    if (this.continuous_) {
      int lineHeights = (int) (height / textHeight);
      int lhPerTick = 1 + this.tickSpacing_;
      int nbTicks =
          lineHeights % lhPerTick == 0 ? lineHeights / lhPerTick : lineHeights / lhPerTick + 1;
      int interval = (height - textHeight) / (nbTicks - 1);
      int rest = (height - textHeight) % (nbTicks - 1);
      int adjustedInterval = interval;
      double value = this.max_;
      double valDiff = (this.max_ - this.min_) / (nbTicks - 1);
      for (int i = 0; i < nbTicks; i++) {
        painter.drawLine(0, 0.5, 4, 0.5);
        painter.drawText(
            10,
            -textHeight / 2,
            40,
            textHeight,
            EnumUtils.or(EnumSet.of(AlignmentFlag.Left), AlignmentFlag.Middle),
            StringUtils.asString(value, this.format_.toString()));
        value -= valDiff;
        if (rest > 0) {
          adjustedInterval = interval + 1;
          rest--;
        } else {
          adjustedInterval = interval;
        }
        painter.translate(0, adjustedInterval);
      }
    } else {
      painter.drawLine(0, 0.5, 4, 0.5);
      painter.drawText(
          10,
          -textHeight / 2,
          100,
          textHeight,
          EnumUtils.or(EnumSet.of(AlignmentFlag.Left), AlignmentFlag.Middle),
          StringUtils.asString(this.max_, this.format_.toString()));
      int nbTicks = this.colors_.size();
      int prevDiff = 0;
      for (int i = nbTicks - 1; i >= 0; i--) {
        double relPos = -(this.colors_.get(i).getValue() - this.max_) / (this.max_ - this.min_);
        double diff = relPos * (height - textHeight);
        int roundedDiff = (int) (diff + 0.5);
        painter.translate(0, roundedDiff - prevDiff);
        painter.drawLine(0, 0.5, 4, 0.5);
        painter.drawText(
            10,
            -textHeight / 2,
            40,
            textHeight,
            EnumUtils.or(EnumSet.of(AlignmentFlag.Left), AlignmentFlag.Middle),
            StringUtils.asString(this.colors_.get(i).getValue(), this.format_.toString()));
        prevDiff = roundedDiff;
      }
    }
    painter.restore();
  }

  private WColor interpolate(final WColor color1, final WColor color2, double factor) {
    return new WColor(
        (int) ((1 - factor) * color1.getRed() + factor * color2.getRed()),
        (int) ((1 - factor) * color1.getGreen() + factor * color2.getGreen()),
        (int) ((1 - factor) * color1.getBlue() + factor * color2.getBlue()));
  }

  private boolean continuous_;
  private List<WStandardColorMap.Pair> colors_;
}
