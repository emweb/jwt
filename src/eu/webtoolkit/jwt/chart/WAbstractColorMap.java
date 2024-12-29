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
 * Maps numerical values to colors.
 *
 * <p>The colormap has functionality to convert a numerical value to a {@link WColor}. For details
 * on how, see the documentation of the implementations.
 *
 * <p>A colormap has a certain numerical range. When a colormap is painted as a colored strip or as
 * a legend, this range is what will be presented, even if the colormap has the ability to convert
 * values outside this range.
 */
public abstract class WAbstractColorMap extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WAbstractColorMap.class);

  /**
   * Constructor.
   *
   * <p>Constructor taking a minimum and maximum value, determining the active range of the
   * colormap. The default value for tickSpacing is 2 and the default label format is 2 decimal
   * points.
   */
  public WAbstractColorMap(double min, double max) {
    super();
    this.min_ = min;
    this.max_ = max;
    this.tickSpacing_ = 2;
    this.format_ = new WString("0.00");
    this.labelFont_ = new WFont();
  }
  /** Converts a numerical value to a {@link WColor}. */
  public abstract WColor toColor(double value);
  /**
   * Paints the colormap as a colored strip.
   *
   * <p>This paints the colormap from the minimum to the maximum value in the provided area (default
   * = fill the entire paintdevice). This is no legend with ticks and labels, only the colors are
   * painted.
   *
   * <p>
   *
   * @see WAbstractColorMap#paintLegend(WPainter painter, WRectF area)
   */
  public abstract void createStrip(WPainter painter, final WRectF area);
  /**
   * Paints the colormap as a colored strip.
   *
   * <p>Calls {@link #createStrip(WPainter painter, WRectF area) createStrip(painter, null)}
   */
  public final void createStrip(WPainter painter) {
    createStrip(painter, null);
  }
  /**
   * Paints the colormap as a legend.
   *
   * <p>The colormap is painted as a legend with ticks and value-labels. The parameter area can be
   * used to specify a part of the paintdevice where the legend should be drawn. When drawing the
   * legend, the tickspacing, labelformat and labelfont are taken into account.
   *
   * <p>
   *
   * @see WAbstractColorMap#setTickSpacing(int spacing)
   * @see WAbstractColorMap#setFormatString(CharSequence format)
   * @see WAbstractColorMap#setLabelFont(WFont font)
   */
  public abstract void paintLegend(WPainter painter, final WRectF area);
  /**
   * Paints the colormap as a legend.
   *
   * <p>Calls {@link #paintLegend(WPainter painter, WRectF area) paintLegend(painter, null)}
   */
  public final void paintLegend(WPainter painter) {
    paintLegend(painter, null);
  }
  /** Returns the minimum of the colormap range. */
  public double getMinimum() {
    return this.min_;
  }
  /** Returns the maximum of the colormap range. */
  public double getMaximum() {
    return this.max_;
  }
  /**
   * Sets the tickspacing for the legend as a number of line-heights.
   *
   * <p>The tickspacing must be specified as an integer number of line-heigths. For example, the
   * default value of 2 will leave two line-heights between the labels of the ticks.
   *
   * <p>
   *
   * @see WAbstractColorMap#paintLegend(WPainter painter, WRectF area)
   */
  public void setTickSpacing(int spacing) {
    this.tickSpacing_ = spacing;
  }
  /**
   * Returns the tickspacing for the legend.
   *
   * <p>
   *
   * @see WAbstractColorMap#setTickSpacing(int spacing)
   */
  public int getTickSpacing() {
    return this.tickSpacing_;
  }
  /**
   * Sets the format for the labels on the colormap-legend.
   *
   * <p>The format string is interpreted by snprintf(). The default is a float with two decimal
   * places.
   *
   * <p>
   *
   * @see WAbstractColorMap#paintLegend(WPainter painter, WRectF area)
   */
  public void setFormatString(final CharSequence format) {
    this.format_ = WString.toWString(format);
  }
  /**
   * Returns the format string.
   *
   * <p>
   *
   * @see WAbstractColorMap#setFormatString(CharSequence format)
   */
  public WString getFormatString() {
    return this.format_;
  }
  /**
   * Sets the font to be used when drawing the labels in the legend.
   *
   * <p>The default is a default constructed {@link WFont}.
   *
   * <p>
   *
   * @see WAbstractColorMap#paintLegend(WPainter painter, WRectF area)
   */
  public void setLabelFont(final WFont font) {
    this.labelFont_ = font;
  }
  /**
   * Returns the font to be used when drawing the labels in the legend.
   *
   * <p>
   *
   * @see WAbstractColorMap#setLabelFont(WFont font)
   */
  public WFont getLabelFont() {
    return this.labelFont_;
  }

  protected double min_;
  protected double max_;
  protected int tickSpacing_;
  protected WString format_;
  protected WFont labelFont_;
}
