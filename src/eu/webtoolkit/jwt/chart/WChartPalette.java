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

/**
 * Abstract base class for styling rendered data series in charts.
 *
 * <p>This class provides an interface for a palette which sets strokes and fill strokes for data in
 * a {@link WAbstractChart chart}. A palette is an ordered list of styles, which is indexed by the
 * chart to get a suitable style for a particular series (in case of {@link
 * eu.webtoolkit.jwt.chart.WCartesianChart}) or data row (in case of {@link WPieChart}). Each style
 * is defined by a brush, two pen styles (one for borders, and one for plain lines), and a font
 * color that is appropriate for drawing text within the brushed area.
 *
 * <p>To use a custom palette, you should reimplement this class, and then use {@link
 * WAbstractChart#setPalette(WChartPalette palette) WAbstractChart#setPalette()} to use an instance
 * of the palette.
 */
public interface WChartPalette {
  /**
   * Returns a brush from the palette.
   *
   * <p>Returns the brush for the style with given <i>index</i>.
   */
  public WBrush getBrush(int index);
  /**
   * Returns a border pen from the palette.
   *
   * <p>Returns the pen for stroking borders around an area filled using the brush at the same
   * <i>index</i>.
   *
   * <p>
   *
   * @see WChartPalette#getStrokePen(int index)
   * @see WChartPalette#getBrush(int index)
   */
  public WPen getBorderPen(int index);
  /**
   * Returns a stroke pen from the palette.
   *
   * <p>Returns the pen for stroking lines for the style with given <i>index</i>.
   *
   * <p>
   *
   * @see WChartPalette#getStrokePen(int index)
   */
  public WPen getStrokePen(int index);
  /**
   * Returns a font color from the palette.
   *
   * <p>Returns a font color suitable for rendering text in the area filled with the brush at the
   * same <i>index</i>.
   *
   * <p>
   *
   * @see WChartPalette#getBrush(int index)
   */
  public WColor getFontColor(int index);
}
