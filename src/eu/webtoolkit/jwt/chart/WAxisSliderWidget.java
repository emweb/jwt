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
 * A widget for selecting an X axis range to display on an associated {@link
 * eu.webtoolkit.jwt.chart.WCartesianChart}.
 *
 * <p>
 *
 * <p><i><b>Note: </b>This widget currently only works with the HtmlCanvas rendering method. </i>
 */
public class WAxisSliderWidget extends WPaintedWidget {
  private static Logger logger = LoggerFactory.getLogger(WAxisSliderWidget.class);

  /**
   * Creates an axis slider widget.
   *
   * <p>Creates an axis slider widget that is not associated with a chart. Before it is used, a
   * chart should be assigned with setChart(), and a series column chosen with setSeriesColumn().
   */
  public WAxisSliderWidget(WContainerWidget parentContainer) {
    super();
    this.series_ = null;
    this.seriesPen_ = new WPen();
    this.selectedSeriesPen_ = this.seriesPen_;
    this.handleBrush_ = new WBrush(new WColor(0, 0, 200));
    this.background_ = new WBrush(new WColor(230, 230, 230));
    this.selectedAreaBrush_ = new WBrush(new WColor(255, 255, 255));
    this.autoPadding_ = false;
    this.labelsEnabled_ = true;
    this.yAxisZoomEnabled_ = true;
    this.transform_ = null;
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an axis slider widget.
   *
   * <p>Calls {@link #WAxisSliderWidget(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  public WAxisSliderWidget() {
    this((WContainerWidget) null);
  }
  /**
   * Creates an axis slider widget.
   *
   * <p>Creates an axis slider widget associated with the given data series of the given chart.
   */
  public WAxisSliderWidget(WDataSeries series, WContainerWidget parentContainer) {
    super();
    this.series_ = series;
    this.seriesPen_ = new WPen();
    this.selectedSeriesPen_ = this.seriesPen_;
    this.handleBrush_ = new WBrush(new WColor(0, 0, 200));
    this.background_ = new WBrush(new WColor(230, 230, 230));
    this.selectedAreaBrush_ = new WBrush(new WColor(255, 255, 255));
    this.autoPadding_ = false;
    this.labelsEnabled_ = true;
    this.yAxisZoomEnabled_ = true;
    this.transform_ = null;
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an axis slider widget.
   *
   * <p>Calls {@link #WAxisSliderWidget(WDataSeries series, WContainerWidget parentContainer)
   * this(series, (WContainerWidget)null)}
   */
  public WAxisSliderWidget(WDataSeries series) {
    this(series, (WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    if (this.getChart() != null) {
      this.getChart().removeAxisSliderWidget(this);
    }
    if (this.selectedSeriesPen_ != this.seriesPen_) {}

    super.remove();
  }

  public void setSeries(WDataSeries series) {
    if (this.series_ != series) {
      if (this.series_ != null) {
        this.getChart().removeAxisSliderWidget(this);
      }
      this.series_ = series;
      if (this.series_ != null) {
        this.getChart().addAxisSliderWidget(this);
      }
      this.update();
    }
  }
  /** Set the pen to draw the data series with. */
  public void setSeriesPen(final WPen pen) {
    if (!pen.equals(this.seriesPen_)) {
      this.seriesPen_ = pen;
      this.update();
    }
  }
  /** Returns the pen to draw the data series with. */
  public WPen getSeriesPen() {
    return this.seriesPen_;
  }
  /**
   * Set the pen to draw the selected part of the data series with.
   *
   * <p>If not set, this defaults to {@link WAxisSliderWidget#getSeriesPen() getSeriesPen()}.
   */
  public void setSelectedSeriesPen(final WPen pen) {
    if (this.selectedSeriesPen_ != this.seriesPen_) {}

    this.selectedSeriesPen_ = pen.clone();
    this.update();
  }
  /** Returns the pen to draw the selected part of the data series with. */
  public WPen getSelectedSeriesPen() {
    return this.selectedSeriesPen_;
  }
  /** Set the brush to draw the handles left and right of the selected area with. */
  public void setHandleBrush(final WBrush brush) {
    if (!brush.equals(this.handleBrush_)) {
      this.handleBrush_ = brush;
      this.update();
    }
  }
  /** Returns the brush to draw the handles left and right of the selected area with. */
  public WBrush getHandleBrush() {
    return this.handleBrush_;
  }
  /** Set the background brush. */
  public void setBackground(final WBrush brush) {
    if (!brush.equals(this.background_)) {
      this.background_ = brush;
      this.update();
    }
  }
  /** Returns the background brush. */
  public WBrush getBackground() {
    return this.background_;
  }
  /** Set the brush for the selected area. */
  public void setSelectedAreaBrush(final WBrush brush) {
    if (!brush.equals(this.selectedAreaBrush_)) {
      this.selectedAreaBrush_ = brush;
      this.update();
    }
  }
  /** Returns the brush for the selected area. */
  public WBrush getSelectedAreaBrush() {
    return this.selectedAreaBrush_;
  }
  /**
   * Sets an internal margin for the selection area.
   *
   * <p>This configures the area (in pixels) around the selection area that is available for the
   * axes and labels, and the handles.
   *
   * <p>Alternatively, you can configure the chart layout to be computed automatically using {@link
   * WAxisSliderWidget#setAutoLayoutEnabled(boolean enabled) setAutoLayoutEnabled()}.
   *
   * <p>
   *
   * @see WAxisSliderWidget#setAutoLayoutEnabled(boolean enabled)
   */
  public void setSelectionAreaPadding(int padding, EnumSet<Side> sides) {
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
   * Sets an internal margin for the selection area.
   *
   * <p>Calls {@link #setSelectionAreaPadding(int padding, EnumSet sides)
   * setSelectionAreaPadding(padding, EnumSet.of(side, sides))}
   */
  public final void setSelectionAreaPadding(int padding, Side side, Side... sides) {
    setSelectionAreaPadding(padding, EnumSet.of(side, sides));
  }
  /**
   * Sets an internal margin for the selection area.
   *
   * <p>Calls {@link #setSelectionAreaPadding(int padding, EnumSet sides)
   * setSelectionAreaPadding(padding, Side.AllSides)}
   */
  public final void setSelectionAreaPadding(int padding) {
    setSelectionAreaPadding(padding, Side.AllSides);
  }
  /**
   * Returns the internal margin for the selection area.
   *
   * <p>This is either the padding set through {@link WAxisSliderWidget#setSelectionAreaPadding(int
   * padding, EnumSet sides) setSelectionAreaPadding()} or computed using {@link
   * WAxisSliderWidget#setAutoLayoutEnabled(boolean enabled) setAutoLayoutEnabled()}.
   *
   * <p>
   */
  public int getSelectionAreaPadding(Side side) {
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
        logger.error(
            new StringWriter().append("selectionAreaPadding(): improper side.").toString());
        return 0;
    }
  }
  /**
   * Configures the axis slider layout to be automatic.
   *
   * <p>This configures the selection area so that the space around it is suited for the text that
   * is rendered.
   */
  public void setAutoLayoutEnabled(boolean enabled) {
    this.autoPadding_ = enabled;
  }
  /**
   * Configures the axis slider layout to be automatic.
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
   * @see WAxisSliderWidget#setAutoLayoutEnabled(boolean enabled)
   */
  public boolean isAutoLayoutEnabled() {
    return this.autoPadding_;
  }
  /**
   * Set whether to draw the X axis tick labels on the slider widget.
   *
   * <p>AxisProperty::Labels are enabled by default.
   */
  public void setLabelsEnabled(boolean enabled) {
    if (enabled != this.labelsEnabled_) {
      this.labelsEnabled_ = enabled;
      this.update();
    }
  }
  /**
   * Set whether to draw the X axis tick labels on the slider widget.
   *
   * <p>Calls {@link #setLabelsEnabled(boolean enabled) setLabelsEnabled(true)}
   */
  public final void setLabelsEnabled() {
    setLabelsEnabled(true);
  }
  /**
   * Returns whether the X axis tick labels are drawn.
   *
   * <p>
   *
   * @see WAxisSliderWidget#setLabelsEnabled(boolean enabled)
   */
  public boolean isLabelsEnabled() {
    return this.labelsEnabled_;
  }
  /**
   * Set whether the Y axis of the associated chart should be updated to fit the series.
   *
   * <p>Y axis zoom is enabled by default.
   */
  public void setYAxisZoomEnabled(boolean enabled) {
    if (enabled != this.yAxisZoomEnabled_) {
      this.yAxisZoomEnabled_ = enabled;
      this.update();
    }
  }
  /**
   * Set whether the Y axis of the associated chart should be updated to fit the series.
   *
   * <p>Calls {@link #setYAxisZoomEnabled(boolean enabled) setYAxisZoomEnabled(true)}
   */
  public final void setYAxisZoomEnabled() {
    setYAxisZoomEnabled(true);
  }
  /**
   * Returns whether the Y axis of the associated chart should be updated to fit the series.
   *
   * <p>
   *
   * @see WAxisSliderWidget#setYAxisZoomEnabled(boolean enabled)
   */
  public boolean isYAxisZoomEnabled() {
    return this.yAxisZoomEnabled_;
  }

  public WDataSeries getSeries() {
    return this.series_;
  }

  protected void render(EnumSet<RenderFlag> flags) {
    super.render(flags);
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WAxisSliderWidget.js", wtjs1());
  }

  protected void paintEvent(WPaintDevice paintDevice) {
    if (!(this.getChart() != null)) {
      logger.error(
          new StringWriter()
              .append("Attempted to draw a slider widget not associated with a chart.")
              .toString());
      return;
    }
    if (!this.getChart().cObjCreated_ || this.getChart().needsRerender()) {
      return;
    }
    if (this.series_.getType() != SeriesType.Line && this.series_.getType() != SeriesType.Curve) {
      if (this.getMethod() == RenderMethod.HtmlCanvas) {
        StringBuilder ss = new StringBuilder();
        ss.append("\ndelete ").append(this.getJsRef()).append(".wtSObj;");
        ss.append("\nif (")
            .append(this.getObjJsRef())
            .append(") {")
            .append(this.getObjJsRef())
            .append(".canvas.style.cursor = 'auto';")
            .append("setTimeout(")
            .append(this.getObjJsRef())
            .append(".repaint,0);}\n");
        this.doJavaScript(ss.toString());
      }
      logger.error(
          new StringWriter()
              .append("WAxisSliderWidget is not associated with a line or curve series.")
              .toString());
      return;
    }
    final WAxis xAxis = this.getChart().getXAxis(this.series_.getXAxis());
    final WCartesianChart.AxisStruct xAxisStruct =
        this.getChart().xAxes_.get(this.series_.getXAxis());
    WPainter painter = new WPainter(paintDevice);
    boolean horizontal = this.getChart().getOrientation() == Orientation.Vertical;
    double w = horizontal ? this.getWidth().getValue() : this.getHeight().getValue();
    double h = horizontal ? this.getHeight().getValue() : this.getWidth().getValue();
    boolean autoPadding = this.autoPadding_;
    if (autoPadding
        && !!EnumUtils.mask(paintDevice.getFeatures(), PaintDeviceFeatureFlag.FontMetrics).isEmpty()
        && this.labelsEnabled_) {
      logger.error(
          new StringWriter()
              .append(
                  "setAutoLayout(): device does not have font metrics (not even server-side font metrics).")
              .toString());
      autoPadding = false;
    }
    if (autoPadding) {
      if (horizontal) {
        if (this.labelsEnabled_) {
          this.setSelectionAreaPadding(0, EnumSet.of(Side.Top));
          this.setSelectionAreaPadding(
              (int) (xAxis.calcMaxTickLabelSize(paintDevice, Orientation.Vertical) + 10),
              EnumSet.of(Side.Bottom));
          this.setSelectionAreaPadding(
              (int)
                  Math.max(
                      xAxis.calcMaxTickLabelSize(paintDevice, Orientation.Horizontal) / 2, 10.0),
              EnumUtils.or(EnumSet.of(Side.Left), Side.Right));
        } else {
          this.setSelectionAreaPadding(0, EnumSet.of(Side.Top));
          this.setSelectionAreaPadding(
              5, EnumUtils.or(EnumUtils.or(EnumSet.of(Side.Left), Side.Right), Side.Bottom));
        }
      } else {
        if (this.labelsEnabled_) {
          this.setSelectionAreaPadding(0, EnumSet.of(Side.Right));
          this.setSelectionAreaPadding(
              (int)
                  Math.max(xAxis.calcMaxTickLabelSize(paintDevice, Orientation.Vertical) / 2, 10.0),
              EnumUtils.or(EnumSet.of(Side.Top), Side.Bottom));
          this.setSelectionAreaPadding(
              (int) (xAxis.calcMaxTickLabelSize(paintDevice, Orientation.Horizontal) + 10),
              EnumSet.of(Side.Left));
        } else {
          this.setSelectionAreaPadding(0, EnumSet.of(Side.Right));
          this.setSelectionAreaPadding(
              5, EnumUtils.or(EnumUtils.or(EnumSet.of(Side.Top), Side.Bottom), Side.Left));
        }
      }
    }
    double left =
        horizontal
            ? this.getSelectionAreaPadding(Side.Left)
            : this.getSelectionAreaPadding(Side.Top);
    double right =
        horizontal
            ? this.getSelectionAreaPadding(Side.Right)
            : this.getSelectionAreaPadding(Side.Bottom);
    double top =
        horizontal
            ? this.getSelectionAreaPadding(Side.Top)
            : this.getSelectionAreaPadding(Side.Right);
    double bottom =
        horizontal
            ? this.getSelectionAreaPadding(Side.Bottom)
            : this.getSelectionAreaPadding(Side.Left);
    double maxW = w - left - right;
    WRectF drawArea = new WRectF(left, 0, maxW, h);
    List<WAxis.Segment> segmentsBak = new ArrayList<WAxis.Segment>();
    for (int i = 0; i < xAxis.segments_.size(); ++i) {
      segmentsBak.add(new WAxis.Segment(xAxis.segments_.get(i)));
    }
    double renderIntervalBak = xAxis.renderInterval_;
    double fullRenderLengthBak = xAxis.fullRenderLength_;
    xAxis.prepareRender(
        horizontal ? Orientation.Horizontal : Orientation.Vertical, drawArea.getWidth());
    final WRectF chartArea = this.getChart().chartArea_;
    WRectF selectionRect = null;
    {
      double u =
          -xAxisStruct.transformHandle.getValue().getDx()
              / (chartArea.getWidth() * xAxisStruct.transformHandle.getValue().getM11());
      selectionRect = new WRectF(0, top, maxW, h - (top + bottom));
      this.transform_.setValue(
          new WTransform(
              1 / xAxisStruct.transformHandle.getValue().getM11(), 0, 0, 1, u * maxW, 0));
    }
    WRectF seriesArea = new WRectF(left, top + 5, maxW, h - (top + bottom + 5));
    WTransform selectionTransform =
        this.hv(new WTransform(1, 0, 0, 1, left, 0).multiply(this.transform_.getValue()));
    WRectF rect = selectionTransform.map(this.hv(selectionRect));
    painter.fillRect(this.hv(new WRectF(left, top, maxW, h - top - bottom)), this.background_);
    painter.fillRect(rect, this.selectedAreaBrush_);
    final double TICK_LENGTH = 5;
    final double ANGLE1 = 15;
    final double ANGLE2 = 80;
    double tickStart = 0.0;
    double tickEnd = 0.0;
    double labelPos = 0.0;
    AlignmentFlag labelHFlag = AlignmentFlag.Center;
    AlignmentFlag labelVFlag = AlignmentFlag.Middle;
    if (horizontal) {
      tickStart = 0;
      tickEnd = TICK_LENGTH;
      labelPos = TICK_LENGTH;
      labelVFlag = AlignmentFlag.Top;
    } else {
      tickStart = -TICK_LENGTH;
      tickEnd = 0;
      labelPos = -TICK_LENGTH;
      labelHFlag = AlignmentFlag.Right;
    }
    if (horizontal) {
      if (xAxis.getLabelAngle() > ANGLE1) {
        labelHFlag = AlignmentFlag.Right;
        if (xAxis.getLabelAngle() > ANGLE2) {
          labelVFlag = AlignmentFlag.Middle;
        }
      } else {
        if (xAxis.getLabelAngle() < -ANGLE1) {
          labelHFlag = AlignmentFlag.Left;
          if (xAxis.getLabelAngle() < -ANGLE2) {
            labelVFlag = AlignmentFlag.Middle;
          }
        }
      }
    } else {
      if (xAxis.getLabelAngle() > ANGLE1) {
        labelVFlag = AlignmentFlag.Bottom;
        if (xAxis.getLabelAngle() > ANGLE2) {
          labelHFlag = AlignmentFlag.Center;
        }
      } else {
        if (xAxis.getLabelAngle() < -ANGLE1) {
          labelVFlag = AlignmentFlag.Top;
          if (xAxis.getLabelAngle() < -ANGLE2) {
            labelHFlag = AlignmentFlag.Center;
          }
        }
      }
    }
    EnumSet<AxisProperty> axisProperties = EnumSet.of(AxisProperty.Line);
    if (this.labelsEnabled_) {
      axisProperties.add(AxisProperty.Labels);
    }
    if (horizontal) {
      xAxis.render(
          painter,
          axisProperties,
          new WPointF(drawArea.getLeft(), h - bottom),
          new WPointF(drawArea.getRight(), h - bottom),
          tickStart,
          tickEnd,
          labelPos,
          EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
      WPainterPath line = new WPainterPath();
      line.moveTo(drawArea.getLeft() + 0.5, h - (bottom - 0.5));
      line.lineTo(drawArea.getRight(), h - (bottom - 0.5));
      painter.strokePath(line, xAxis.getPen());
    } else {
      xAxis.render(
          painter,
          axisProperties,
          new WPointF(this.getSelectionAreaPadding(Side.Left) - 1, drawArea.getLeft()),
          new WPointF(this.getSelectionAreaPadding(Side.Left) - 1, drawArea.getRight()),
          tickStart,
          tickEnd,
          labelPos,
          EnumUtils.or(EnumSet.of(labelHFlag), labelVFlag));
      WPainterPath line = new WPainterPath();
      line.moveTo(this.getSelectionAreaPadding(Side.Left) - 0.5, drawArea.getLeft() + 0.5);
      line.lineTo(this.getSelectionAreaPadding(Side.Left) - 0.5, drawArea.getRight());
      painter.strokePath(line, xAxis.getPen());
    }
    WPainterPath curve = new WPainterPath();
    {
      WTransform t =
          new WTransform(1, 0, 0, 1, seriesArea.getLeft(), seriesArea.getTop())
              .multiply(
                  new WTransform(
                      seriesArea.getWidth() / chartArea.getWidth(),
                      0,
                      0,
                      seriesArea.getHeight() / chartArea.getHeight(),
                      0,
                      0))
              .multiply(new WTransform(1, 0, 0, 1, -chartArea.getLeft(), -chartArea.getTop()));
      if (!horizontal) {
        t.assign(
            new WTransform(
                    0,
                    1,
                    1,
                    0,
                    this.getSelectionAreaPadding(Side.Left)
                        - this.getSelectionAreaPadding(Side.Right)
                        - 5,
                    0)
                .multiply(t)
                .multiply(new WTransform(0, 1, 1, 0, 0, 0)));
      }
      curve.assign(t.map(this.getChart().pathForSeries(this.series_)));
    }
    {
      WRectF leftHandle = this.hv(new WRectF(-5, top, 5, h - top - bottom));
      WTransform t =
          new WTransform(1, 0, 0, 1, left, -top)
              .multiply(
                  new WTransform()
                      .translate(this.transform_.getValue().map(selectionRect.getTopLeft())));
      painter.fillRect(this.hv(t).map(leftHandle), this.handleBrush_);
    }
    {
      WRectF rightHandle = this.hv(new WRectF(0, top, 5, h - top - bottom));
      WTransform t =
          new WTransform(1, 0, 0, 1, left, -top)
              .multiply(
                  new WTransform()
                      .translate(this.transform_.getValue().map(selectionRect.getTopRight())));
      painter.fillRect(this.hv(t).map(rightHandle), this.handleBrush_);
    }
    if (this.selectedSeriesPen_ != this.seriesPen_
        && !this.selectedSeriesPen_.equals(this.seriesPen_)) {
      WPainterPath clipPath = new WPainterPath();
      clipPath.addRect(this.hv(selectionRect));
      painter.setClipPath(selectionTransform.map(clipPath));
      painter.setClipping(true);
      painter.setPen(this.getSelectedSeriesPen());
      painter.drawPath(curve);
      WPainterPath leftClipPath = new WPainterPath();
      leftClipPath.addRect(
          this.hv(new WTransform(1, 0, 0, 1, -selectionRect.getWidth(), 0).map(selectionRect)));
      painter.setClipPath(
          this.hv(
                  new WTransform(1, 0, 0, 1, left, -top)
                      .multiply(
                          new WTransform()
                              .translate(
                                  this.transform_.getValue().map(selectionRect.getTopLeft()))))
              .map(leftClipPath));
      painter.setPen(this.getSeriesPen());
      painter.drawPath(curve);
      WPainterPath rightClipPath = new WPainterPath();
      rightClipPath.addRect(
          this.hv(new WTransform(1, 0, 0, 1, selectionRect.getWidth(), 0).map(selectionRect)));
      painter.setClipPath(
          this.hv(
                  new WTransform(1, 0, 0, 1, left - selectionRect.getRight(), -top)
                      .multiply(
                          new WTransform()
                              .translate(
                                  this.transform_.getValue().map(selectionRect.getTopRight()))))
              .map(rightClipPath));
      painter.drawPath(curve);
      painter.setClipping(false);
    } else {
      WPainterPath clipPath = new WPainterPath();
      clipPath.addRect(this.hv(new WRectF(left, top, maxW, h - top - bottom)));
      painter.setClipPath(clipPath);
      painter.setClipping(true);
      painter.setPen(this.getSeriesPen());
      painter.drawPath(curve);
      painter.setClipping(false);
    }
    if (this.getMethod() == RenderMethod.HtmlCanvas) {
      WApplication app = WApplication.getInstance();
      StringBuilder ss = new StringBuilder();
      ss.append("new Wt4_8_1.WAxisSliderWidget(")
          .append(app.getJavaScriptClass())
          .append(",")
          .append(this.getJsRef())
          .append(",")
          .append(this.getObjJsRef())
          .append(",")
          .append("{chart:")
          .append(this.getChart().getCObjJsRef())
          .append(",transform:")
          .append(this.transform_.getJsRef())
          .append(",rect:function(){return ")
          .append(rect.getJsRef())
          .append("},drawArea:")
          .append(drawArea.getJsRef())
          .append(",series:")
          .append(this.getChart().getSeriesIndexOf(this.series_))
          .append(",updateYAxis:")
          .append(StringUtils.asString(this.yAxisZoomEnabled_).toString())
          .append("});");
      this.doJavaScript(ss.toString());
    }
    xAxis.segments_.clear();
    for (int i = 0; i < segmentsBak.size(); ++i) {
      xAxis.segments_.add(new WAxis.Segment(segmentsBak.get(i)));
    }
    xAxis.renderInterval_ = renderIntervalBak;
    xAxis.fullRenderLength_ = fullRenderLengthBak;
  }

  private void init() {
    this.transform_ = this.createJSTransform();
    this.mouseWentDown()
        .addListener("function(o, e){var o=" + this.getSObjJsRef() + ";if(o){o.mouseDown(o, e);}}");
    this.mouseWentUp()
        .addListener("function(o, e){var o=" + this.getSObjJsRef() + ";if(o){o.mouseUp(o, e);}}");
    this.mouseDragged()
        .addListener("function(o, e){var o=" + this.getSObjJsRef() + ";if(o){o.mouseDrag(o, e);}}");
    this.mouseMoved()
        .addListener(
            "function(o, e){var o=" + this.getSObjJsRef() + ";if(o){o.mouseMoved(o, e);}}");
    this.touchStarted()
        .addListener(
            "function(o, e){var o=" + this.getSObjJsRef() + ";if(o){o.touchStarted(o, e);}}");
    this.touchEnded()
        .addListener(
            "function(o, e){var o=" + this.getSObjJsRef() + ";if(o){o.touchEnded(o, e);}}");
    this.touchMoved()
        .addListener(
            "function(o, e){var o=" + this.getSObjJsRef() + ";if(o){o.touchMoved(o, e);}}");
    this.setSelectionAreaPadding(0, EnumSet.of(Side.Top));
    this.setSelectionAreaPadding(20, EnumUtils.or(EnumSet.of(Side.Left), Side.Right));
    this.setSelectionAreaPadding(30, EnumSet.of(Side.Bottom));
    if (this.getChart() != null) {
      this.getChart().addAxisSliderWidget(this);
    }
  }

  private String getSObjJsRef() {
    return this.getJsRef() + ".wtSObj";
  }

  private WRectF hv(final WRectF rect) {
    boolean horizontal = this.getChart().getOrientation() == Orientation.Vertical;
    if (horizontal) {
      return rect;
    } else {
      return new WRectF(
          this.getWidth().getValue() - rect.getY() - rect.getHeight(),
          rect.getX(),
          rect.getHeight(),
          rect.getWidth());
    }
  }

  private WTransform hv(final WTransform t) {
    boolean horizontal = this.getChart().getOrientation() == Orientation.Vertical;
    if (horizontal) {
      return t;
    } else {
      return new WTransform(0, 1, 1, 0, 0, 0)
          .multiply(t)
          .multiply(new WTransform(0, 1, 1, 0, 0, 0));
    }
  }

  private WCartesianChart getChart() {
    if (this.series_ != null) {
      return this.series_.getChart();
    } else {
      return null;
    }
  }

  private WDataSeries series_;
  private WPen seriesPen_;
  private WPen selectedSeriesPen_;
  private WBrush handleBrush_;
  private WBrush background_;
  private WBrush selectedAreaBrush_;
  private boolean autoPadding_;
  private boolean labelsEnabled_;
  private boolean yAxisZoomEnabled_;
  private int[] padding_ = new int[4];
  private WJavaScriptHandle<WTransform> transform_;

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WAxisSliderWidget",
        "(function(e,t,n,r){var i=window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(e){window.setTimeout(e,0)},o=!1,a={};t.wtSObj=this;var s=this,c=e.WT;n.canvas.style.msTouchAction=\"none\";function u(e){return 2===e.pointerType||3===e.pointerType||\"pen\"===e.pointerType||\"touch\"===e.pointerType}var f=!1;if(window.MSPointerEvent||window.PointerEvent){t.style.touchAction=\"none\";n.canvas.style.msTouchAction=\"none\";n.canvas.style.touchAction=\"none\"}(window.MSPointerEvent||window.PointerEvent)&&function(){pointers=[];function e(){pointers.length>0&&!f?f=!0:pointers.length<=0&&f&&(f=!1)}function n(n){if(u(n)){n.preventDefault();pointers.push(n);e();a.start(t,{touches:pointers.slice(0)})}}function r(n){if(f&&u(n)){n.preventDefault();var r;for(r=0;r<pointers.length;++r)if(pointers[r].pointerId===n.pointerId){pointers.splice(r,1);break}e();a.end(t,{touches:pointers.slice(0),changedTouches:[]})}}function i(n){if(u(n)){n.preventDefault();var r;for(r=0;r<pointers.length;++r)if(pointers[r].pointerId===n.pointerId){pointers[r]=n;break}e();a.moved(t,{touches:pointers.slice(0)})}}var o=t.wtEObj;if(o)if(window.PointerEvent){t.removeEventListener(\"pointerdown\",o.pointerDown);t.removeEventListener(\"pointerup\",o.pointerUp);t.removeEventListener(\"pointerout\",o.pointerUp);t.removeEventListener(\"pointermove\",o.pointerMove)}else{t.removeEventListener(\"MSPointerDown\",o.pointerDown);t.removeEventListener(\"MSPointerUp\",o.pointerUp);t.removeEventListener(\"MSPointerOut\",o.pointerUp);t.removeEventListener(\"MSPointerMove\",o.pointerMove)}t.wtEObj={pointerDown:n,pointerUp:r,pointerMove:i};if(window.PointerEvent){t.addEventListener(\"pointerdown\",n);t.addEventListener(\"pointerup\",r);t.addEventListener(\"pointerout\",r);t.addEventListener(\"pointermove\",i)}else{t.addEventListener(\"MSPointerDown\",n);t.addEventListener(\"MSPointerUp\",r);t.addEventListener(\"MSPointerOut\",r);t.addEventListener(\"MSPointerMove\",i)}}();var v=c.gfxUtils.rect_left,l=c.gfxUtils.rect_right,d=c.gfxUtils.rect_top,p=c.gfxUtils.rect_bottom;c.gfxUtils.rect_normalized,c.gfxUtils.transform_mult,c.gfxUtils.transform_apply;function h(){return r.chart.config.series[r.series].xAxis}this.xAxis=h;function w(){return r.chart.config.minZoom.x[h()]}function g(){return r.chart.config.maxZoom.x[h()]}var m=null,E=null;function x(){!function(e){if(!o){o=!0;i((function(){e();o=!1}))}}(n.repaint)}this.changeRange=function(e,t){e<0&&(e=0);t>1&&(t=1);var n=r.drawArea;r.transform[0]=t-e;r.transform[4]=e*n[2];x()};function y(){var e=r.drawArea,t=r.transform[4]/e[2];return[t,r.transform[0]+t]}function M(e){x();if(e){var t=r.transform,n=r.drawArea,i=t[4]/n[2],o=t[0]+i;r.chart.setXRange(r.series,i,o,r.updateYAxis)}}function b(){return!r.chart.config.isHorizontal}function A(e,t,n){return b()?e.y>=d(t)&&e.y<=p(t)&&e.x>v(t)-n/2&&e.x<v(t)+n/2:e.x>=v(t)&&e.x<=l(t)&&e.y>d(t)-n/2&&e.y<d(t)+n/2}function D(e,t,n){return b()?e.y>=d(t)&&e.y<=p(t)&&e.x>l(t)-n/2&&e.x<l(t)+n/2:e.x>=v(t)&&e.x<=l(t)&&e.y>p(t)-n/2&&e.y<p(t)+n/2}function L(e,t){return b()?e.y>=d(t)&&e.y<=p(t)&&e.x>v(t)&&e.x<l(t):e.x>=v(t)&&e.x<=l(t)&&e.y>d(t)&&e.y<p(t)}this.mouseDown=function(e,n){if(!f){m=c.widgetCoordinates(t,n);var i=r.rect();if(A(m,i,10))E=1;else if(D(m,i,10))E=3;else{if(!L(m,i)){E=null;return}E=2}c.cancelEvent(n)}};this.mouseUp=function(e,t){if(!f){m=null;if(null!==E){E=null;c.cancelEvent(t)}}};function P(e){var t=r.transform,n=r.drawArea,i=t[4]/n[2],o=t[0]+i,a=(i*n[2]+e)/n[2];if(!(o<=a)){var c=1/(o-a);if(!(c>g()||c<w())){a<0&&(a=0);a>1&&(a=1);s.changeRange(a,o);M(!0);var u=y();if(Math.abs(u[1]-o)>Math.abs(u[0]-i)){s.changeRange(i,o);M(!0)}}}}function S(e){var t=r.transform,n=r.drawArea,i=t[4]/n[2],o=t[0]+i,a=(o*n[2]+e)/n[2];if(!(a<=i)){var c=1/(a-i);if(!(c>g()||c<w())){a<0&&(a=0);a>1&&(a=1);s.changeRange(i,a);M(!0);var u=y();if(Math.abs(u[0]-i)>Math.abs(u[1]-o)){s.changeRange(i,o);M(!0)}}}}function T(e){var t=r.transform,n=r.drawArea,i=t[4]/n[2],o=t[0]+i,a=i*n[2],c=a+e;if(c<0){e=-a;c=0}var u=c/n[2],f=o*n[2],v=f+e;if(v>n[2]){u=(c=a+(e=n[2]-f))/n[2];v=n[2]}var l=v/n[2];s.changeRange(u,l);M(!0)}this.mouseDrag=function(e,n){if(!f&&E){c.cancelEvent(n);var r=c.widgetCoordinates(t,n);if(null!==m){var i;i=b()?r.x-m.x:r.y-m.y;switch(E){case 1:P(i);break;case 2:T(i);break;case 3:S(i)}m=r;M(!0)}else m=r}};this.mouseMoved=function(e,i){setTimeout((function(){if(!f&&!E){var e=c.widgetCoordinates(t,i),o=r.rect();A(e,o,10)||D(e,o,10)?b()?n.canvas.style.cursor=\"col-resize\":n.canvas.style.cursor=\"row-resize\":L(e,o)?n.canvas.style.cursor=\"move\":n.canvas.style.cursor=\"auto\"}}),0)};var U=!1,C=!1,R=null;a.start=function(e,t){U=1===t.touches.length;C=2===t.touches.length;if(U){m=c.widgetCoordinates(n.canvas,t.touches[0]);var i=r.rect();if(A(m,i,20))E=1;else if(D(m,i,20))E=3;else{if(!L(m,i)){E=null;return}E=2}c.capture(null);c.capture(n.canvas);t.preventDefault&&t.preventDefault()}else if(C){E=null;var o=[c.widgetCoordinates(n.canvas,t.touches[0]),c.widgetCoordinates(n.canvas,t.touches[1])];i=r.rect();if(!L(o[0],i)||!L(o[1],i))return;R=b()?Math.abs(o[0].x-o[1].x):Math.abs(o[0].y-o[1].y);c.capture(null);c.capture(n.canvas);t.preventDefault&&t.preventDefault()}};a.end=function(e,n){var r=Array.prototype.slice.call(n.touches),i=U,o=C,s=0===r.length;U=1===r.length;C=2===r.length;s||function(){var e;for(e=0;e<n.changedTouches.length;++e)!function(){for(var t=n.changedTouches[e].identifier,i=0;i<r.length;++i)if(r[i].identifier===t){r.splice(i,1);return}}()}();s=0===r.length;U=1===r.length;C=2===r.length;if(s&&i){m=null;if(null===E)return;E=null;c.cancelEvent(n)}if(U&&o){C=!1;R=null;c.cancelEvent(n);a.start(t,n)}if(s&&o){C=!1;R=null;c.cancelEvent(n)}};a.moved=function(e,i){if(E){i.preventDefault&&i.preventDefault();var o,a=c.widgetCoordinates(t,i);if(null===m){m=a;return}o=b()?a.x-m.x:a.y-m.y;switch(E){case 1:P(o);break;case 2:T(o);break;case 3:S(o)}m=a;M(!0)}else if(C){i.preventDefault&&i.preventDefault();touches=[c.widgetCoordinates(n.canvas,i.touches[0]),c.widgetCoordinates(n.canvas,i.touches[1])];r.rect();var s,u=(s=b()?Math.abs(touches[0].x-touches[1].x):Math.abs(touches[0].y-touches[1].y))-R;P(-u/2);S(u/2);R=s;M(!0)}};this.updateConfig=function(e){for(var t in e)e.hasOwnProperty(t)&&(r[t]=e[t]);M(!1)};s.updateConfig({});if(!window.TouchEvent||window.MSPointerEvent||window.PointerEvent){var _=function(){};s.touchStarted=_;s.touchEnded=_;s.touchMoved=_}else{s.touchStarted=a.start;s.touchEnded=a.end;s.touchMoved=a.moved}})");
  }
}
