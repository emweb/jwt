/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
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
 * Abstract base class for MVC-based charts.
 *
 * <p>This is an abstract class and should not be used directly.
 *
 * <p>As an abstract base for MVC-based charts, this class manages the model {@link
 * WAbstractChart#setModel(WAbstractItemModel model) setModel()} and provides virtual methods that
 * listen to model changes. In addition, it gives access to generic chart properties such as the
 * title {@link WAbstractChart#setTitle(CharSequence title) setTitle()} and title font {@link
 * WAbstractChart#setTitleFont(WFont titleFont) setTitleFont()}, the chart palette {@link
 * WAbstractChart#setPalette(WChartPalette palette) setPalette()}, plot area padding {@link
 * WAbstractChart#setPlotAreaPadding(int padding, EnumSet sides) setPlotAreaPadding()}, and the
 * background fill color {@link WAbstractChart#setBackground(WBrush background) setBackground()}.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>Styling through CSS is not applicable.
 *
 * <p>
 *
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
 * @see WPieChart
 */
public abstract class WAbstractChart extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(WAbstractChart.class);

  /** Destructor. */
  public void remove() {
    super.remove();
  }
  /**
   * Sets the model.
   *
   * <p>The model is used by the chart to get its data.
   *
   * <p>The default model is <code>null</code>.
   *
   * <p>This creates an internal proxy model that presents the {@link WAbstractItemModel} as a
   * {@link WAbstractChartModel}. Use {@link WAbstractChart#setModel(WAbstractChartModel model)
   * setModel()} directly for highest performance (avoiding the overhead of any for numeric data).
   *
   * <p>
   *
   * <p><i><b>Note: </b>Setting a new model on a {@link eu.webtoolkit.jwt.chart.WCartesianChart}
   * causes the {@link WCartesianChart#XSeriesColumn()} and all series to be cleared </i>
   *
   * @see WAbstractChart#getModel()
   */
  public void setModel(final WAbstractItemModel model) {
    this.setModel(new WStandardChartProxyModel(model));
  }
  /**
   * Sets the model.
   *
   * <p>The model is used by the chart to get its data.
   *
   * <p>The default model is a <code>null</code>.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Setting a new model on a {@link eu.webtoolkit.jwt.chart.WCartesianChart}
   * causes the {@link WCartesianChart#XSeriesColumn()} and all series to be cleared </i>
   *
   * @see WAbstractChart#getModel()
   */
  public void setModel(final WAbstractChartModel model) {
    if (this.model_ != null) {
      for (int i = 0; i < this.modelConnections_.size(); ++i) {
        this.modelConnections_.get(i).disconnect();
      }
      this.modelConnections_.clear();
    }
    this.model_ = model;
    this.modelConnections_.add(
        this.model_
            .changed()
            .addListener(
                this,
                () -> {
                  WAbstractChart.this.modelReset();
                }));
    this.modelChanged();
  }
  /**
   * Returns the model.
   *
   * <p>
   *
   * @see WAbstractChart#setModel(WAbstractChartModel model)
   */
  public WAbstractChartModel getModel() {
    return this.model_;
  }
  /**
   * Returns the model.
   *
   * <p>If a model was set using {@link WAbstractChart#setModel(WAbstractItemModel model)
   * setModel()}, then this model will be returned by this call.
   *
   * <p>
   *
   * @see WAbstractChart#setModel(WAbstractChartModel model)
   */
  public WAbstractItemModel getItemModel() {
    WStandardChartProxyModel proxy = ObjectUtils.cast(this.model_, WStandardChartProxyModel.class);
    if (proxy != null) {
      return proxy.getSourceModel();
    } else {
      return null;
    }
  }
  /**
   * Sets a background for the chart.
   *
   * <p>Set the background color for the main plot area.
   *
   * <p>The default is a completely transparent background.
   *
   * <p>
   *
   * @see WAbstractChart#getBackground()
   */
  public void setBackground(final WBrush background) {
    if (!ChartUtils.equals(this.background_, background)) {
      this.background_ = background;
      update();
    }
    ;
  }
  /**
   * Returns the background of the chart.
   *
   * <p>
   *
   * @see WAbstractChart#setBackground(WBrush background)
   */
  public WBrush getBackground() {
    return this.background_;
  }
  /**
   * Set a palette for the chart.
   *
   * <p>A palette is used to provide the style information to render the chart series.
   *
   * <p>The default palette is dependent on the chart type.
   *
   * <p>
   *
   * @see WAbstractChart#getPalette()
   */
  public void setPalette(final WChartPalette palette) {
    this.palette_ = palette;
    this.update();
  }
  /** Returns the palette for the chart. */
  public WChartPalette getPalette() {
    return this.palette_;
  }
  /**
   * Set an internal margin for the main plot area.
   *
   * <p>This configures the area (in pixels) around the plot area that is available for axes,
   * labels, and titles.
   *
   * <p>The default is dependent on the chart type.
   *
   * <p>Alternatively, you can configure the chart layout to be computed automatically using {@link
   * WAbstractChart#setAutoLayoutEnabled(boolean enabled) setAutoLayoutEnabled()}.
   *
   * <p>
   *
   * @see WAbstractChart#setAutoLayoutEnabled(boolean enabled)
   */
  public void setPlotAreaPadding(int padding, EnumSet<Side> sides) {
    if (sides.contains(Side.Top)) {
      this.padding_[0] = padding;
    }
    if (sides.contains(Side.Right)) {
      this.padding_[1] = padding;
    }
    if (sides.contains(Side.Bottom)) {
      this.padding_[2] = padding;
    }
    if (sides.contains(Side.Left)) {
      this.padding_[3] = padding;
    }
  }
  /**
   * Set an internal margin for the main plot area.
   *
   * <p>Calls {@link #setPlotAreaPadding(int padding, EnumSet sides) setPlotAreaPadding(padding,
   * EnumSet.of(side, sides))}
   */
  public final void setPlotAreaPadding(int padding, Side side, Side... sides) {
    setPlotAreaPadding(padding, EnumSet.of(side, sides));
  }
  /**
   * Set an internal margin for the main plot area.
   *
   * <p>Calls {@link #setPlotAreaPadding(int padding, EnumSet sides) setPlotAreaPadding(padding,
   * Side.AllSides)}
   */
  public final void setPlotAreaPadding(int padding) {
    setPlotAreaPadding(padding, Side.AllSides);
  }
  /**
   * Returns the internal margin for the main plot area.
   *
   * <p>This is either the paddings set through {@link WAbstractChart#setPlotAreaPadding(int
   * padding, EnumSet sides) setPlotAreaPadding()} or computed using {@link
   * WAbstractChart#setAutoLayoutEnabled(boolean enabled) setAutoLayoutEnabled()}
   *
   * <p>
   *
   * @see WAbstractChart#setPlotAreaPadding(int padding, EnumSet sides)
   */
  public int getPlotAreaPadding(Side side) {
    switch (side) {
      case Top:
        return this.padding_[0];
      case Right:
        return this.padding_[1];
      case Bottom:
        return this.padding_[2];
      case Left:
        return this.padding_[3];
      default:
        logger.error(new StringWriter().append("plotAreaPadding(): improper side.").toString());
        return 0;
    }
  }
  /**
   * Configures the chart layout to be automatic.
   *
   * <p>This configures the plot area so that the space around it is suited for the text that is
   * rendered (axis labels and text, the title, and legend).
   *
   * <p>The default value is <code>false</code>, and the chart layout is set manually using values
   * set in {@link WAbstractChart#setPlotAreaPadding(int padding, EnumSet sides)
   * setPlotAreaPadding()}.
   */
  public void setAutoLayoutEnabled(boolean enabled) {
    this.autoPadding_ = enabled;
  }
  /**
   * Configures the chart layout to be automatic.
   *
   * <p>Calls {@link #setAutoLayoutEnabled(boolean enabled) setAutoLayoutEnabled(true)}
   */
  public final void setAutoLayoutEnabled() {
    setAutoLayoutEnabled(true);
  }
  /**
   * Returns whether chart layout is computed automatically.
   *
   * <p>
   *
   * @see WAbstractChart#setAutoLayoutEnabled(boolean enabled)
   */
  public boolean isAutoLayoutEnabled() {
    return this.autoPadding_;
  }
  /**
   * Set a chart title.
   *
   * <p>The title is displayed on top of the chart, using the {@link WAbstractChart#getTitleFont()
   * getTitleFont()}.
   *
   * <p>The default title is an empty title (&quot;&quot;).
   *
   * <p>
   *
   * @see WAbstractChart#getTitle()
   */
  public void setTitle(final CharSequence title) {
    if (!ChartUtils.equals(this.title_, WString.toWString(title))) {
      this.title_ = WString.toWString(title);
      update();
    }
    ;
  }
  /**
   * Return the chart title.
   *
   * <p>
   *
   * @see WAbstractChart#getTitle()
   */
  public WString getTitle() {
    return this.title_;
  }
  /**
   * Set the font for the chart title.
   *
   * <p>Changes the font for the chart title.
   *
   * <p>The default title font is a 15 point Sans Serif font.
   *
   * <p>
   *
   * @see WAbstractChart#getTitleFont()
   * @see WAbstractChart#setTitle(CharSequence title)
   */
  public void setTitleFont(final WFont titleFont) {
    if (!ChartUtils.equals(this.titleFont_, titleFont)) {
      this.titleFont_ = titleFont;
      update();
    }
    ;
  }
  /**
   * Returns the font for the chart title.
   *
   * <p>
   *
   * @see WAbstractChart#setTitleFont(WFont titleFont)
   */
  public WFont getTitleFont() {
    return this.titleFont_;
  }

  void setAxisTitleFont(final WFont titleFont) {
    if (!ChartUtils.equals(this.axisTitleFont_, titleFont)) {
      this.axisTitleFont_ = titleFont;
      update();
    }
    ;
  }

  WFont getAxisTitleFont() {
    return this.titleFont_;
  }
  /**
   * Paint the chart in a rectangle of the given painter.
   *
   * <p>Paints the chart inside the <i>painter</i>, in the area indicated by <i>rectangle</i>. When
   * <i>rectangle</i> is a null rectangle, the entire painter {@link WPainter#getWindow()} is used.
   */
  public abstract void paint(final WPainter painter, final WRectF rectangle);
  /**
   * Paint the chart in a rectangle of the given painter.
   *
   * <p>Calls {@link #paint(WPainter painter, WRectF rectangle) paint(painter, null)}
   */
  public final void paint(final WPainter painter) {
    paint(painter, null);
  }

  protected WAbstractChart(WContainerWidget parentContainer) {
    super();
    this.model_ = null;
    this.palette_ = null;
    this.background_ = new WBrush(StandardColor.White);
    this.autoPadding_ = false;
    this.title_ = new WString();
    this.titleFont_ = new WFont();
    this.axisTitleFont_ = new WFont();
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
    this.titleFont_.setFamily(FontFamily.SansSerif);
    this.titleFont_.setSize(new WLength(15, LengthUnit.Point));
    this.setPlotAreaPadding(5, EnumUtils.or(EnumSet.of(Side.Left), Side.Right));
    this.setPlotAreaPadding(5, EnumUtils.or(EnumSet.of(Side.Top), Side.Bottom));
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  protected WAbstractChart() {
    this((WContainerWidget) null);
  }

  private WAbstractChartModel model_;
  private WChartPalette palette_;
  private WBrush background_;
  private boolean autoPadding_;
  private int[] padding_ = new int[4];
  private WString title_;
  private WFont titleFont_;
  private WFont axisTitleFont_;
  private List<AbstractSignal.Connection> modelConnections_;

  protected void modelChanged() {}

  protected void modelReset() {}
  // private void (final T m, final T v) ;
  // private void modelDataChanged(int row1, int column1, int row2, int column2) ;
}
