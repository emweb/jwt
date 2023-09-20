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
 * A single data series in a cartesian chart.
 *
 * <p>This class configures all aspects for rendering a single data series in a cartesian chart. A
 * data series renders Y data from a single model column against the X series configured for the
 * chart.
 *
 * <p>The data column should contain data that can be converted to a number, but should not
 * necessarily be of a number type, see also {@link eu.webtoolkit.jwt.StringUtils#asNumber(Object)}.
 *
 * <p>Multiple series of different types may be combined on a single chart.
 *
 * <p><div align="center"> <img src="doc-files/ChartWDataSeries-1.png">
 *
 * <p><strong>Different styles of data series</strong> </div>
 *
 * <p>For a category chart, series may be stacked on top of each other. This is controlled by {@link
 * WDataSeries#setStacked(boolean stacked) setStacked()} for a series, which if enabled, will stack
 * that series on top of the preceding data series. This works regardless of whether they are of the
 * same type, but obviously works visually best if these series are of the same type. When not
 * stacked, bar series are rendered next to each other. The margin between bars of different data
 * series is controlled using {@link WCartesianChart#setBarMargin(double margin)
 * WCartesianChart#setBarMargin()}.
 *
 * <p>The line and color type are by default based on the {@link WAbstractChart#getPalette() chart
 * palette}, but may be overridden for a series using {@link WDataSeries#setPen(WPen pen) setPen()},
 * {@link WDataSeries#setBrush(WBrush brush) setBrush()}, etc...
 *
 * <p>
 *
 * @see WCartesianChart#addSeries(WDataSeries series)
 */
public class WDataSeries extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WDataSeries.class);

  /**
   * Constructs a new data series.
   *
   * <p>Creates a new data series which plots the Y values from the model column <i>modelColumn</i>,
   * with the indicated <i>seriesType</i>. The Y values are mapped to the indicated <i>axis</i>,
   * which should correspond to one of the two Y axes.
   *
   * <p>
   *
   * @see WCartesianChart#addSeries(WDataSeries series)
   */
  public WDataSeries(int modelColumn, SeriesType type, Axis axis) {
    super();
    this.chart_ = null;
    this.model_ = (WAbstractChartModel) null;
    this.modelColumn_ = modelColumn;
    this.XSeriesColumn_ = -1;
    this.stacked_ = false;
    this.type_ = type;
    this.xAxis_ = 0;
    this.yAxis_ = axis == Axis.Y1 ? 0 : 1;
    this.customFlags_ = EnumSet.noneOf(CustomFlag.class);
    this.pen_ = new WPen();
    this.markerPen_ = new WPen();
    this.brush_ = new WBrush();
    this.markerBrush_ = new WBrush();
    this.labelColor_ = new WColor();
    this.shadow_ = new WShadow();
    this.fillRange_ = FillRangeType.None;
    this.marker_ = type == SeriesType.Point ? MarkerType.Circle : MarkerType.None;
    this.markerSize_ = 6;
    this.legend_ = true;
    this.xLabel_ = false;
    this.yLabel_ = false;
    this.barWidth_ = 0.8;
    this.hidden_ = false;
    this.customMarker_ = new WPainterPath();
    this.offset_ = 0.0;
    this.scale_ = 1.0;
    this.offsetDirty_ = true;
    this.scaleDirty_ = true;
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
  }
  /**
   * Constructs a new data series.
   *
   * <p>Calls {@link #WDataSeries(int modelColumn, SeriesType type, Axis axis) this(modelColumn,
   * SeriesType.Point, Axis.Y1)}
   */
  public WDataSeries(int modelColumn) {
    this(modelColumn, SeriesType.Point, Axis.Y1);
  }
  /**
   * Constructs a new data series.
   *
   * <p>Calls {@link #WDataSeries(int modelColumn, SeriesType type, Axis axis) this(modelColumn,
   * type, Axis.Y1)}
   */
  public WDataSeries(int modelColumn, SeriesType type) {
    this(modelColumn, type, Axis.Y1);
  }
  /**
   * Constructs a new data series.
   *
   * <p>Creates a new data series which plots the Y values from the model column <i>modelColumn</i>,
   * with the indicated <i>seriesType</i>. The Y values are mapped to the indicated <i>yAxis</i>,
   * which should correspond to one of the two Y axes.
   *
   * <p>
   *
   * @see WCartesianChart#addSeries(WDataSeries series)
   */
  public WDataSeries(int modelColumn, SeriesType type, int axis) {
    super();
    this.chart_ = null;
    this.model_ = (WAbstractChartModel) null;
    this.modelColumn_ = modelColumn;
    this.XSeriesColumn_ = -1;
    this.stacked_ = false;
    this.type_ = type;
    this.xAxis_ = 0;
    this.yAxis_ = axis;
    this.customFlags_ = EnumSet.noneOf(CustomFlag.class);
    this.pen_ = new WPen();
    this.markerPen_ = new WPen();
    this.brush_ = new WBrush();
    this.markerBrush_ = new WBrush();
    this.labelColor_ = new WColor();
    this.shadow_ = new WShadow();
    this.fillRange_ = FillRangeType.None;
    this.marker_ = type == SeriesType.Point ? MarkerType.Circle : MarkerType.None;
    this.markerSize_ = 6;
    this.legend_ = true;
    this.xLabel_ = false;
    this.yLabel_ = false;
    this.barWidth_ = 0.8;
    this.hidden_ = false;
    this.customMarker_ = new WPainterPath();
    this.offset_ = 0.0;
    this.scale_ = 1.0;
    this.offsetDirty_ = true;
    this.scaleDirty_ = true;
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
  }
  /**
   * Sets the bar width.
   *
   * <p>The bar width specifies the bar width (in axis dimensions). For category plots, which may
   * have several bars for different series next to each other, you will want to specify the same
   * bar width for each series.
   *
   * <p>For scatter plots, you may want to set the bar width to a natural size. E.g. if you are
   * plotting weekly measurements, you could set the width to correspond to a week (=7).
   *
   * <p>The default value is 0.8 (which leaves a 20% margin between bars for different categories in
   * a category chart.
   *
   * <p>
   *
   * @see WCartesianChart#setBarMargin(double margin)
   */
  public void setBarWidth(final double width) {
    this.barWidth_ = width;
  }
  /**
   * Returns the bar width.
   *
   * <p>
   *
   * @see WDataSeries#setBarWidth(double width)
   */
  public double getBarWidth() {
    return this.barWidth_;
  }
  /**
   * Sets the series type.
   *
   * <p>The series type specifies how the data is plotted, i.e. using mere point markers, lines,
   * curves, or bars.
   */
  public void setType(SeriesType type) {
    if (!ChartUtils.equals(this.type_, type)) {
      this.type_ = type;
      update();
    }
    ;
  }
  /**
   * Returns the series type.
   *
   * <p>
   *
   * @see WDataSeries#setType(SeriesType type)
   */
  public SeriesType getType() {
    return this.type_;
  }
  /**
   * Sets the model column.
   *
   * <p>This specifies the model column from which the Y data is retrieved that is plotted by this
   * series.
   *
   * <p>The data column should contain data that can be converted to a number (but should not
   * necessarily be of a number type). See also {@link
   * eu.webtoolkit.jwt.StringUtils#asNumber(Object)}.
   */
  public void setModelColumn(int modelColumn) {
    if (!ChartUtils.equals(this.modelColumn_, modelColumn)) {
      this.modelColumn_ = modelColumn;
      update();
    }
    ;
  }
  /**
   * Returns the model column.
   *
   * <p>
   *
   * @see WDataSeries#setModelColumn(int modelColumn)
   */
  public int getModelColumn() {
    return this.modelColumn_;
  }
  /**
   * Sets the X series column.
   *
   * <p>By default, the data series uses the X series column configured for the chart. For a scatter
   * plot, each series can have its own matching X data, which is configured here. For other plots,
   * this setting is ignored.
   *
   * <p>The default value is -1, which indicates that {@link WCartesianChart#XSeriesColumn()} is to
   * be used.
   *
   * <p>
   *
   * @see WCartesianChart#setXSeriesColumn(int modelColumn)
   */
  public void setXSeriesColumn(int modelColumn) {
    this.XSeriesColumn_ = modelColumn;
  }
  /**
   * Returns the X series column.
   *
   * <p>
   *
   * @see WDataSeries#setXSeriesColumn(int modelColumn)
   */
  public int XSeriesColumn() {
    return this.XSeriesColumn_;
  }
  /**
   * Sets whether this series is stacked on top of the preceding series.
   *
   * <p>For category charts, data from different series may be rendered stacked on top of each
   * other. The rendered value is the sum of the value of this series plus the rendered value of the
   * preceding series. For line series, you probably will want to add filling under the curve. A
   * stacked bar series is rendered by a bar on top of the preceding bar series.
   *
   * <p>The default value is false.
   */
  public void setStacked(boolean stacked) {
    if (!ChartUtils.equals(this.stacked_, stacked)) {
      this.stacked_ = stacked;
      update();
    }
    ;
  }
  /**
   * Returns whether this series is stacked on top of the preceding series.
   *
   * <p>
   *
   * @see WDataSeries#setStacked(boolean stacked)
   */
  public boolean isStacked() {
    return this.stacked_;
  }
  /**
   * Binds this series to a chart axis.
   *
   * <p>A data series may be bound to either the first or second Y axis. Note that the second Y axis
   * is by default not displayed.
   *
   * <p>The default value is the first Y axis.
   *
   * <p>
   *
   * @see WAxis#setVisible(boolean visible)
   */
  public void bindToAxis(Axis axis) {
    if (!ChartUtils.equals(this.yAxis_, axis == Axis.Y1 ? 0 : 1)) {
      this.yAxis_ = axis == Axis.Y1 ? 0 : 1;
      update();
    }
    ;
  }
  /**
   * Binds this series to a chart&apos;s X axis.
   *
   * <p>Note that the second Y axis will not be displayed by default.
   *
   * <p>The default value is the first X axis.
   *
   * <p>
   *
   * @see WAxis#setVisible(boolean visible)
   */
  public void bindToXAxis(int xAxis) {
    if (!ChartUtils.equals(this.xAxis_, xAxis)) {
      this.xAxis_ = xAxis;
      update();
    }
    ;
  }
  /**
   * Binds this series to a chart&apos;s Y axis.
   *
   * <p>Note that the second Y axis will not be displayed by default.
   *
   * <p>The default value is the first Y axis.
   *
   * <p>
   *
   * @see WAxis#setVisible(boolean visible)
   */
  public void bindToYAxis(int yAxis) {
    if (!ChartUtils.equals(this.yAxis_, yAxis)) {
      this.yAxis_ = yAxis;
      update();
    }
    ;
  }
  /**
   * Returns the Y axis used for this series.
   *
   * <p>
   *
   * @see WDataSeries#bindToAxis(Axis axis)
   */
  public Axis getAxis() {
    return this.yAxis_ == 1 ? Axis.Y2 : Axis.Y1;
  }
  /**
   * Returns the Y axis used for this series.
   *
   * <p>
   *
   * @see WDataSeries#bindToXAxis(int xAxis)
   */
  public int getXAxis() {
    return this.xAxis_;
  }
  /**
   * Returns the Y axis used for this series.
   *
   * <p>
   *
   * @see WDataSeries#bindToYAxis(int yAxis)
   */
  public int getYAxis() {
    return this.yAxis_;
  }
  /**
   * Sets which aspects of the look are overriden.
   *
   * <p>Set which aspects of the look, that are by default based on the chart palette, are
   * overridden by custom settings.
   *
   * <p>The default value is 0 (nothing overridden).
   */
  public void setCustomFlags(EnumSet<CustomFlag> flags) {
    if (!ChartUtils.equals(this.customFlags_, flags)) {
      this.customFlags_ = flags;
      update();
    }
    ;
  }
  /**
   * Sets which aspects of the look are overriden.
   *
   * <p>Calls {@link #setCustomFlags(EnumSet flags) setCustomFlags(EnumSet.of(flag, flags))}
   */
  public final void setCustomFlags(CustomFlag flag, CustomFlag... flags) {
    setCustomFlags(EnumSet.of(flag, flags));
  }
  /**
   * Returns which aspects of the look are overriden.
   *
   * <p>
   *
   * @see WDataSeries#setCustomFlags(EnumSet flags)
   */
  public EnumSet<CustomFlag> getCustomFlags() {
    return this.customFlags_;
  }
  /**
   * Overrides the pen used for drawing lines for this series.
   *
   * <p>Overrides the pen that is used to draw this series. Calling this method automatically adds
   * CustomPen to the custom flags.
   *
   * <p>The default value is a default WPen().
   *
   * <p>
   *
   * @see WChartPalette#getStrokePen(int index)
   * @see WChartPalette#getBorderPen(int index)
   */
  public void setPen(final WPen pen) {
    if (!ChartUtils.equals(this.pen_, pen)) {
      this.pen_ = pen;
      update();
    }
    ;
    this.customFlags_.add(CustomFlag.Pen);
  }
  /**
   * Returns the pen used for drawing lines for this series.
   *
   * <p>
   *
   * @see WDataSeries#setPen(WPen pen)
   */
  public WPen getPen() {
    if (this.customFlags_.contains(CustomFlag.Pen)) {
      return this.pen_;
    } else {
      if (this.chart_ != null) {
        if (this.type_ == SeriesType.Bar) {
          return this.chart_.getPalette().getBorderPen(this.chart_.getSeriesIndexOf(this));
        } else {
          return this.chart_.getPalette().getStrokePen(this.chart_.getSeriesIndexOf(this));
        }
      } else {
        WPen defaultPen = new WPen();
        defaultPen.setCapStyle(PenCapStyle.Round);
        defaultPen.setJoinStyle(PenJoinStyle.Round);
        return defaultPen;
      }
    }
  }
  /**
   * Overrides the brush used for filling areas for this series.
   *
   * <p>Overrides the brush that is used to draw this series which is otherwise provided by the
   * chart palette. For a bar plot, this is the brush used to fill the bars. For a line chart, this
   * is the brush used to fill the area under (or above) the line. Calling this method automatically
   * adds CustomBrush to the custom flags.
   *
   * <p>
   *
   * @see WChartPalette#getBrush(int index)
   */
  public void setBrush(final WBrush brush) {
    if (!ChartUtils.equals(this.brush_, brush)) {
      this.brush_ = brush;
      update();
    }
    ;
    this.customFlags_.add(CustomFlag.Brush);
  }
  /**
   * Returns the brush used for filling areas for this series.
   *
   * <p>
   *
   * @see WDataSeries#setBrush(WBrush brush)
   */
  public WBrush getBrush() {
    if (this.customFlags_.contains(CustomFlag.Brush)) {
      return this.brush_;
    } else {
      if (this.chart_ != null) {
        return this.chart_.getPalette().getBrush(this.chart_.getSeriesIndexOf(this));
      } else {
        return new WBrush();
      }
    }
  }
  /** Sets a shadow used for stroking lines for this series. */
  public void setShadow(final WShadow shadow) {
    if (!ChartUtils.equals(this.shadow_, shadow)) {
      this.shadow_ = shadow;
      update();
    }
    ;
  }
  /**
   * Returns the shadow used for stroking lines for this series.
   *
   * <p>
   *
   * @see WDataSeries#setShadow(WShadow shadow)
   */
  public WShadow getShadow() {
    return this.shadow_;
  }
  /**
   * Sets the fill range for line or curve series.
   *
   * <p>Line or curve series may be filled under or above the curve, using the {@link
   * WDataSeries#getBrush() getBrush()}. This setting specifies the range that is filled. The
   * default value for all but {@link SeriesType#Bar} is {@link FillRangeType#None}.
   *
   * <p>Bar series may use {@link FillRangeType#MinimumValue} to configure the chart to render its
   * bars from the data point to the bottom of the chart or {@link FillRangeType#MaximumValue} to
   * render the bars from the data point to the top of the chart. The default value for {@link
   * SeriesType#Bar} is {@link FillRangeType#ZeroValue}, which render bars from zero to the data
   * value.
   */
  public void setFillRange(FillRangeType fillRange) {
    if (!ChartUtils.equals(this.fillRange_, fillRange)) {
      this.fillRange_ = fillRange;
      update();
    }
    ;
  }
  /**
   * Returns the fill range (for line, curve and bar series).
   *
   * <p>
   *
   * @see WDataSeries#setFillRange(FillRangeType fillRange)
   */
  public FillRangeType getFillRange() {
    if (this.type_ == SeriesType.Bar && this.fillRange_ == FillRangeType.None) {
      return FillRangeType.ZeroValue;
    } else {
      return this.fillRange_;
    }
  }
  /**
   * Sets the data point marker.
   *
   * <p>Specifies a marker that is displayed at the (X,Y) coordinate for each series data point.
   *
   * <p>The default value is a {@link MarkerType#Circle} for a {@link SeriesType#Point}, or {@link
   * MarkerType#None} otherwise.
   *
   * <p>
   *
   * @see WDataSeries#setMarkerPen(WPen pen)
   * @see WDataSeries#setMarkerBrush(WBrush brush)
   * @see WDataSeries#setCustomMarker(WPainterPath path)
   */
  public void setMarker(MarkerType marker) {
    if (!ChartUtils.equals(this.marker_, marker)) {
      this.marker_ = marker;
      update();
    }
    ;
  }
  /**
   * Sets the custom marker.
   *
   * <p>This will also changes the marker type to {@link MarkerType#Custom}.
   *
   * <p>
   *
   * @see WDataSeries#setMarker(MarkerType marker)
   */
  public void setCustomMarker(final WPainterPath path) {
    if (!ChartUtils.equals(this.marker_, MarkerType.Custom)) {
      this.marker_ = MarkerType.Custom;
      update();
    }
    ;
    this.customMarker_.assign(path);
  }
  /**
   * Returns the custom marker.
   *
   * <p>
   *
   * @see WDataSeries#setCustomMarker(WPainterPath path)
   */
  public WPainterPath getCustomMarker() {
    return this.customMarker_;
  }
  /**
   * Returns the data point marker.
   *
   * <p>
   *
   * @see WDataSeries#setMarker(MarkerType marker)
   */
  public MarkerType getMarker() {
    return this.marker_;
  }
  /**
   * Sets the marker size.
   *
   * <p>The default marker size is 6 pixels.
   */
  public void setMarkerSize(double size) {
    if (!ChartUtils.equals(this.markerSize_, size)) {
      this.markerSize_ = size;
      update();
    }
    ;
  }
  /**
   * Returns the marker size.
   *
   * <p>
   *
   * @see WDataSeries#setMarkerSize(double size)
   */
  public double getMarkerSize() {
    return this.markerSize_;
  }
  /**
   * Sets the marker pen.
   *
   * <p>Overrides the pen used for stroking the marker. By default the marker pen is the same as
   * {@link WDataSeries#getPen() getPen()}. Calling this method automatically adds CustomMarkerPen
   * to the custom flags.
   *
   * <p>
   *
   * @see WDataSeries#setPen(WPen pen)
   * @see WDataSeries#setMarkerBrush(WBrush brush)
   */
  public void setMarkerPen(final WPen pen) {
    if (!ChartUtils.equals(this.markerPen_, pen)) {
      this.markerPen_ = pen;
      update();
    }
    ;
    this.customFlags_.add(CustomFlag.MarkerPen);
  }
  /**
   * Returns the marker pen.
   *
   * <p>
   *
   * @see WDataSeries#setMarkerPen(WPen pen)
   */
  public WPen getMarkerPen() {
    if (this.customFlags_.contains(CustomFlag.MarkerPen)) {
      return this.markerPen_;
    } else {
      return this.getPen();
    }
  }
  /**
   * Sets the marker brush.
   *
   * <p>Overrides the brush used for filling the marker. By default the marker brush is the same as
   * {@link WDataSeries#getBrush() getBrush()}. Calling this method automatically adds
   * CustomMarkerBrush to the custom flags.
   *
   * <p>
   *
   * @see WDataSeries#setBrush(WBrush brush)
   * @see WDataSeries#setMarkerPen(WPen pen)
   */
  public void setMarkerBrush(final WBrush brush) {
    if (!ChartUtils.equals(this.markerBrush_, brush)) {
      this.markerBrush_ = brush;
      update();
    }
    ;
    this.customFlags_.add(CustomFlag.MarkerBrush);
  }
  /**
   * Returns the marker brush.
   *
   * <p>
   *
   * @see WDataSeries#setMarkerBrush(WBrush brush)
   */
  public WBrush getMarkerBrush() {
    if (this.customFlags_.contains(CustomFlag.MarkerBrush)) {
      return this.markerBrush_;
    } else {
      return this.getBrush();
    }
  }
  /**
   * Enables the entry for this series in the legend.
   *
   * <p>When <i>enabled</i>, this series is added to the chart legend.
   *
   * <p>The default value is true.
   *
   * <p>
   *
   * @see WCartesianChart#setLegendEnabled(boolean enabled)
   */
  public void setLegendEnabled(boolean enabled) {
    if (!ChartUtils.equals(this.legend_, enabled)) {
      this.legend_ = enabled;
      update();
    }
    ;
  }
  /**
   * Returns whether this series has an entry in the legend.
   *
   * <p>
   *
   * @see WDataSeries#setLegendEnabled(boolean enabled)
   */
  public boolean isLegendEnabled() {
    if (!this.isHidden()) {
      return this.legend_;
    } else {
      return false;
    }
  }
  /**
   * Enables a label that is shown at the series data points.
   *
   * <p>You may enable labels for the {@link Axis#X}, {@link Axis#Y} or both axes. The label that is
   * displayed is the corresponding value on that axis. If both labels are enabled then they are
   * combined in a single text using the format: &quot;&lt;x-value&gt;: &lt;y-value&gt;&quot;.
   *
   * <p>The default values are false for both axes (no labels).
   *
   * <p>
   *
   * @see WDataSeries#isLabelsEnabled(Axis axis)
   */
  public void setLabelsEnabled(Axis axis, boolean enabled) {
    if (axis == Axis.X) {
      this.xLabel_ = enabled;
    } else {
      this.yLabel_ = enabled;
    }
    this.update();
  }
  /**
   * Enables a label that is shown at the series data points.
   *
   * <p>Calls {@link #setLabelsEnabled(Axis axis, boolean enabled) setLabelsEnabled(axis, true)}
   */
  public final void setLabelsEnabled(Axis axis) {
    setLabelsEnabled(axis, true);
  }
  /**
   * Returns whether labels are enabled for the given axis.
   *
   * <p>
   *
   * @see WDataSeries#setLabelsEnabled(Axis axis, boolean enabled)
   */
  public boolean isLabelsEnabled(Axis axis) {
    return axis == Axis.X ? this.xLabel_ : this.yLabel_;
  }
  /**
   * Sets the label color.
   *
   * <p>Specify the color used for the rendering labels at the data points.
   *
   * <p>
   *
   * @see WDataSeries#setLabelsEnabled(Axis axis, boolean enabled)
   */
  public void setLabelColor(final WColor color) {
    if (!ChartUtils.equals(this.labelColor_, color)) {
      this.labelColor_ = color;
      update();
    }
    ;
    this.customFlags_.add(CustomFlag.LabelColor);
  }
  /**
   * Returns the label color.
   *
   * <p>
   *
   * @see WDataSeries#setLabelColor(WColor color)
   */
  public WColor getLabelColor() {
    if (this.customFlags_.contains(CustomFlag.LabelColor)) {
      return this.labelColor_;
    } else {
      if (this.chart_ != null) {
        return this.chart_.getPalette().getFontColor(this.chart_.getSeriesIndexOf(this));
      } else {
        return new WColor(StandardColor.Black);
      }
    }
  }
  /**
   * Hide/unhide this series.
   *
   * <p>A hidden series will not be show in the chart and legend.
   */
  public void setHidden(boolean hidden) {
    this.hidden_ = hidden;
  }
  /**
   * Return whether the series is hidden.
   *
   * <p>
   *
   * @see WDataSeries#setHidden(boolean hidden)
   */
  public boolean isHidden() {
    return this.hidden_;
  }
  /**
   * Maps from device coordinates to model coordinates.
   *
   * <p>Maps a position in the chart back to model coordinates, for data in this data series.
   *
   * <p>This uses WChart::mapFromDevice() passing the {@link WDataSeries#getAxis() getAxis()} to
   * which this series is bound.
   *
   * <p>This method uses the axis dimensions that are based on the latest chart rendering. If you
   * have not yet rendered the chart, or wish to already the mapping reflect model changes since the
   * last rendering, you should call {@link WCartesianChart#initLayout(WRectF rectangle,
   * WPaintDevice device) WCartesianChart#initLayout()} first.
   *
   * <p>
   *
   * @see WDataSeries#mapToDevice(Object xValue, Object yValue, int segment)
   */
  public WPointF mapFromDevice(final WPointF deviceCoordinates) {
    if (this.chart_ != null) {
      return this.chart_.mapFromDevice(
          deviceCoordinates, this.chart_.getXAxis(this.xAxis_), this.chart_.getYAxis(this.yAxis_));
    } else {
      return new WPointF();
    }
  }
  /**
   * Maps from model values to device coordinates.
   *
   * <p>Maps model values to device coordinates, for data in this data series.
   *
   * <p>This uses WChart::mapToDevice() passing the {@link WDataSeries#getAxis() getAxis()} to which
   * this series is bound.
   *
   * <p>This method uses the axis dimensions that are based on the latest chart rendering. If you
   * have not yet rendered the chart, or wish to already the mapping reflect model changes since the
   * last rendering, you should call {@link WCartesianChart#initLayout(WRectF rectangle,
   * WPaintDevice device) WCartesianChart#initLayout()} first.
   *
   * <p>
   *
   * @see WDataSeries#mapFromDevice(WPointF deviceCoordinates)
   */
  public WPointF mapToDevice(final Object xValue, final Object yValue, int segment) {
    if (this.chart_ != null) {
      return this.chart_.mapToDevice(
          xValue,
          yValue,
          this.chart_.getXAxis(this.xAxis_),
          this.chart_.getYAxis(this.yAxis_),
          segment);
    } else {
      return new WPointF();
    }
  }
  /**
   * Maps from model values to device coordinates.
   *
   * <p>Returns {@link #mapToDevice(Object xValue, Object yValue, int segment) mapToDevice(xValue,
   * yValue, 0)}
   */
  public final WPointF mapToDevice(final Object xValue, final Object yValue) {
    return mapToDevice(xValue, yValue, 0);
  }
  /**
   * Set an offset to draw the data series at.
   *
   * <p>The Y position of the data series will be drawn at an offset, expressed in model
   * coordinates. The axis labels won&apos;t follow the same offset.
   *
   * <p>The offset can be manipulated client side using a mouse or touch drag if {@link
   * WCartesianChart#isCurveManipulationEnabled()} is enabled.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This is only supported for axes with linear scale. </i>
   *
   * @see WDataSeries#setScale(double scale)
   * @see WCartesianChart#setCurveManipulationEnabled(boolean enabled)
   */
  public void setOffset(double offset) {
    if (this.offset_ != offset) {
      this.offset_ = offset;
      this.update();
    }
    this.offsetDirty_ = true;
  }
  /**
   * Get the offset for this data series.
   *
   * <p>
   *
   * @see WDataSeries#setOffset(double offset)
   */
  public double getOffset() {
    return this.offset_;
  }
  /**
   * Set the scale to draw the data series at.
   *
   * <p>The Y position of the data series will be scaled around the zero position, and offset by
   * {@link WDataSeries#getOffset() getOffset()}.
   *
   * <p>The scale can be manipulated client side using the scroll wheel or a pinch motion if {@link
   * WCartesianChart#isCurveManipulationEnabled()} is enabled.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This is only supported for axes with linear scale. </i>
   *
   * @see WDataSeries#setOffset(double offset)
   * @see WCartesianChart#setCurveManipulationEnabled(boolean enabled)
   */
  public void setScale(double scale) {
    if (this.scale_ != scale) {
      this.scale_ = scale;
      this.update();
    }
    this.scaleDirty_ = true;
  }
  /**
   * Get the scale for this data series.
   *
   * <p>
   *
   * @see WDataSeries#setScale(double scale)
   */
  public double getScale() {
    return this.scale_;
  }
  /**
   * Set a model for this data series.
   *
   * <p>If no model is set for this data series, the model of the chart will be used.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Individual models per data series are only supported for {@link
   * ChartType#Scatter} type charts. </i>
   *
   * @see WAbstractChart#setModel(WAbstractItemModel model)
   */
  public void setModel(final WAbstractChartModel model) {
    if (this.model_ != null) {
      for (int i = 0; i < this.modelConnections_.size(); ++i) {
        this.modelConnections_.get(i).disconnect();
      }
      this.modelConnections_.clear();
    }
    this.model_ = model;
    if (this.model_ != null) {
      this.modelConnections_.add(
          this.model_
              .changed()
              .addListener(
                  this,
                  () -> {
                    WDataSeries.this.modelReset();
                  }));
    }
    if (this.chart_ != null) {
      this.chart_.update();
    }
  }
  /**
   * Get the model for this data series.
   *
   * <p>This will return the model set for this data series, if it is set.
   *
   * <p>If no model is set for this data series, and the series is associated with a chart, the
   * model of the chart is returned.
   *
   * <p>If no model is set for this data series, and the series is not associated with any data
   * series, this will return null.
   *
   * <p>
   *
   * @see WDataSeries#setModel(WAbstractChartModel model)
   * @see WAbstractChart#setModel(WAbstractItemModel model)
   */
  public WAbstractChartModel getModel() {
    if (this.model_ != null) {
      return this.model_;
    }
    if (this.chart_ != null) {
      return this.chart_.getModel();
    }
    return null;
  }

  public WCartesianChart getChart() {
    return this.chart_;
  }

  private WCartesianChart chart_;
  private WAbstractChartModel model_;
  int modelColumn_;
  private int XSeriesColumn_;
  private boolean stacked_;
  private SeriesType type_;
  private int xAxis_;
  private int yAxis_;
  private EnumSet<CustomFlag> customFlags_;
  private WPen pen_;
  private WPen markerPen_;
  private WBrush brush_;
  private WBrush markerBrush_;
  private WColor labelColor_;
  private WShadow shadow_;
  private FillRangeType fillRange_;
  private MarkerType marker_;
  private double markerSize_;
  private boolean legend_;
  private boolean xLabel_;
  private boolean yLabel_;
  private double barWidth_;
  private boolean hidden_;
  private WPainterPath customMarker_;
  double offset_;
  double scale_;
  boolean offsetDirty_;
  boolean scaleDirty_;
  private List<AbstractSignal.Connection> modelConnections_;

  private void modelReset() {
    if (this.chart_ != null) {
      this.chart_.modelReset();
    }
  }
  // private boolean (final T m, final T v) ;
  void setChart(WCartesianChart chart) {
    this.chart_ = chart;
  }

  private void update() {
    if (this.chart_ != null) {
      this.chart_.update();
    }
  }
}
