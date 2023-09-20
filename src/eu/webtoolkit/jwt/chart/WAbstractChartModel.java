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
 * An abstract model for use with JWt&apos;s charts.
 *
 * <p>This abstract model is used by {@link WAbstractChart} as data model.
 */
public abstract class WAbstractChartModel extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WAbstractChartModel.class);

  /** Creates a new chart model. */
  public WAbstractChartModel() {
    super();
    this.changed_ = new Signal();
  }
  /**
   * Returns data at a given row and column.
   *
   * <p>This value determines the position of a data point on the chart.
   */
  public abstract double getData(int row, int column);
  /**
   * Returns display data at a given row and column.
   *
   * <p>This value should be a textual representation of the value returned by {@link
   * WAbstractChartModel#getData(int row, int column) getData()}. This defaults to the string
   * representation of the double returned by {@link WAbstractChartModel#getData(int row, int
   * column) getData()}.
   */
  public WString getDisplayData(int row, int column) {
    return new WString(String.valueOf(this.getData(row, column)));
  }
  /**
   * Returns the given column&apos;s header data.
   *
   * <p>This is used as the name in the legend for a data series.
   *
   * <p>Defaults to an empty string.
   */
  public WString getHeaderData(int column) {
    return new WString();
  }
  /**
   * Returns the tooltip text to use on a given row and column.
   *
   * <p>Defaults to an empty string, signifying that no tooltip should be shown.
   */
  public WString getToolTip(int row, int column) {
    return new WString();
  }
  /**
   * Returns the item flags for the given row and column.
   *
   * <p>Only the ItemIsXHTMLText and ItemHasDeferredTooltip flags are supported for charts.
   *
   * <p>ItemIsXHTMLText determines whether the tooltip text should be rendered as XHTML or as plain
   * text, and ItemHasDeferredTooltip makes it so that tooltips are only loaded on demand.
   *
   * <p>
   *
   * <p><i><b>Note: </b>An XHTML text tooltip will be forced to be deferred. Non-deferred XHTML
   * tooltips are not supported. </i>
   *
   * <p><i><b>Note: </b>When not using deferred tooltips, the HTML &lt;area&gt; tag will be used. If
   * there are many tooltips and the chart is interactive this may cause client-side performance
   * issues. If deferred tooltips are used, this will cause some load on the server, as it
   * calculates server-side what marker or bar the user is hovering over. </i>
   *
   * <p><i><b>Note: </b>If the chart is interactive, and tooltips are not deferred, they will be
   * scaled according to the first Y axis, and thus multiple Y axes will not be supported in
   * combination with plain tooltips. </i>
   */
  public EnumSet<ItemFlag> flags(int row, int column) {
    return EnumSet.noneOf(ItemFlag.class);
  }
  /**
   * Returns the link for a given row and column.
   *
   * <p>Defaults to an empty link, signifying that no link should be shown.
   */
  public WLink link(int row, int column) {
    return null;
  }
  /**
   * Returns the marker pen color to use for a given row and column.
   *
   * <p>This is used as the color of the outline of markers when drawing a {@link SeriesType#Point}.
   * The default is null, indicating that the default color, as determined by {@link
   * WDataSeries#getMarkerPen()}, should be used.
   *
   * <p>
   *
   * @see WDataSeries#setMarkerPen(WPen pen)
   */
  public WColor getMarkerPenColor(int row, int column) {
    return null;
  }
  /**
   * Returns the marker brush color to use for a given row and column.
   *
   * <p>This is used as the color of the brush used when drawing a {@link SeriesType#Point}. The
   * default is null, indicating that the default color, as determined by {@link
   * WDataSeries#getMarkerBrush()}, should be used.
   *
   * <p>
   *
   * @see WDataSeries#setMarkerBrush(WBrush brush)
   */
  public WColor getMarkerBrushColor(int row, int column) {
    return null;
  }
  /**
   * Returns the marker type to use for a given row and column.
   *
   * <p>This is used as the shape of the marker used when drawing a PointSeries. The default is
   * null, indicating that the default marker, as determined by {@link WDataSeries#getMarker()},
   * should be used.
   *
   * <p>
   *
   * @see WDataSeries#setMarker(MarkerType marker)
   */
  public MarkerType markerType(int row, int column) {
    return null;
  }
  /**
   * Returns the bar pen color to use for a given row and column.
   *
   * <p>This is used as the color of the outline of bars when drawing a {@link SeriesType#Bar}. The
   * default is null, indicating that the default color, as determined by {@link
   * WDataSeries#getPen()}, should be used.
   *
   * <p>
   *
   * @see WDataSeries#setPen(WPen pen)
   */
  public WColor getBarPenColor(int row, int column) {
    return null;
  }
  /**
   * Returns the bar brush color to use for a given row and column.
   *
   * <p>This is used as the color of the brush used when drawing a {@link SeriesType#Bar}. The
   * default is null, indicating that the default color, as determined by {@link
   * WDataSeries#getBrush()}, should be used.
   *
   * <p>
   *
   * @see WDataSeries#setBrush(WBrush brush)
   */
  public WColor getBarBrushColor(int row, int column) {
    return null;
  }
  /**
   * Returns the marker scale factor to use for a given row and column.
   *
   * <p>This is used to scale the size of the marker when drawing a {@link SeriesType#Point}. The
   * default is null, indicating that the default scale should be used.
   */
  public Double getMarkerScaleFactor(int row, int column) {
    return null;
  }
  /**
   * Returns the number of columns.
   *
   * <p>
   *
   * @see WAbstractChartModel#getRowCount()
   */
  public abstract int getColumnCount();
  /**
   * Returns the number of rows.
   *
   * <p>
   *
   * @see WAbstractChartModel#getColumnCount()
   */
  public abstract int getRowCount();
  /**
   * A signal that notifies of any change to the model.
   *
   * <p>Implementations should trigger this signal in order to update the chart.
   */
  public Signal changed() {
    return this.changed_;
  }

  private Signal changed_;
}
