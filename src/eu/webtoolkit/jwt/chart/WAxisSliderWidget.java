/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget for selecting an X axis range to display on an associated
 * {@link eu.webtoolkit.jwt.chart.WCartesianChart}.
 * <p>
 * 
 * <p>
 * <i><b>Note: </b>This widget currently only works with the HtmlCanvas
 * rendering method. </i>
 * </p>
 */
public class WAxisSliderWidget extends WPaintedWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WAxisSliderWidget.class);

	/**
	 * Creates an axis slider widget.
	 * <p>
	 * Creates an axis slider widget that is not associated with a chart. Before
	 * it is used, a chart should be assigned with
	 * {@link WAxisSliderWidget#setChart(WCartesianChart chart) setChart()}, and
	 * a series column chosen with
	 * {@link WAxisSliderWidget#setSeriesColumn(int seriesColumn)
	 * setSeriesColumn()}.
	 */
	public WAxisSliderWidget(WContainerWidget parent) {
		super(parent);
		this.chart_ = null;
		this.seriesColumn_ = -1;
		this.seriesPen_ = new WPen();
		this.selectedSeriesPen_ = this.seriesPen_;
		this.handleBrush_ = new WBrush(new WColor(0, 0, 200));
		this.background_ = new WBrush(new WColor(230, 230, 230));
		this.selectedAreaBrush_ = new WBrush(new WColor(255, 255, 255));
		this.autoPadding_ = false;
		this.labelsEnabled_ = true;
		this.transform_ = null;
		this.init();
	}

	/**
	 * Creates an axis slider widget.
	 * <p>
	 * Calls {@link #WAxisSliderWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WAxisSliderWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates an axis slider widget.
	 * <p>
	 * Creates an axis slider widget associated with the given data series of
	 * the given chart.
	 */
	public WAxisSliderWidget(WCartesianChart chart, int seriesColumn,
			WContainerWidget parent) {
		super(parent);
		this.chart_ = chart;
		this.seriesColumn_ = seriesColumn;
		this.seriesPen_ = new WPen();
		this.selectedSeriesPen_ = this.seriesPen_;
		this.handleBrush_ = new WBrush(new WColor(0, 0, 200));
		this.background_ = new WBrush(new WColor(230, 230, 230));
		this.selectedAreaBrush_ = new WBrush(new WColor(255, 255, 255));
		this.autoPadding_ = false;
		this.labelsEnabled_ = true;
		this.transform_ = null;
		this.init();
	}

	/**
	 * Creates an axis slider widget.
	 * <p>
	 * Calls
	 * {@link #WAxisSliderWidget(WCartesianChart chart, int seriesColumn, WContainerWidget parent)
	 * this(chart, seriesColumn, (WContainerWidget)null)}
	 */
	public WAxisSliderWidget(WCartesianChart chart, int seriesColumn) {
		this(chart, seriesColumn, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		if (this.chart_ != null) {
			this.chart_.removeAxisSliderWidget(this);
		}
		if (this.selectedSeriesPen_ != this.seriesPen_) {
			;
		}
		super.remove();
	}

	/**
	 * Set the associated chart.
	 */
	public void setChart(WCartesianChart chart) {
		if (chart != this.chart_) {
			if (this.chart_ != null) {
				this.chart_.removeAxisSliderWidget(this);
			}
			this.chart_ = chart;
			if (this.chart_ != null) {
				this.chart_.addAxisSliderWidget(this);
			}
			this.update();
		}
	}

	/**
	 * Set the associated data series column.
	 * <p>
	 * Only LineSeries and CurveSeries are supported.
	 */
	public void setSeriesColumn(int seriesColumn) {
		if (seriesColumn != this.seriesColumn_) {
			this.seriesColumn_ = seriesColumn;
			this.update();
		}
	}

	/**
	 * Returns the associated data series column.
	 */
	public int getSeriesColumn() {
		return this.seriesColumn_;
	}

	/**
	 * Set the pen to draw the data series with.
	 */
	public void setSeriesPen(final WPen pen) {
		if (!pen.equals(this.seriesPen_)) {
			this.seriesPen_ = pen;
			this.update();
		}
	}

	/**
	 * Returns the pen to draw the data series with.
	 */
	public WPen getSeriesPen() {
		return this.seriesPen_;
	}

	/**
	 * Set the pen to draw the selected part of the data series with.
	 * <p>
	 * If not set, this defaults to {@link WAxisSliderWidget#getSeriesPen()
	 * getSeriesPen()}.
	 */
	public void setSelectedSeriesPen(final WPen pen) {
		if (this.selectedSeriesPen_ != this.seriesPen_) {
			;
		}
		this.selectedSeriesPen_ = pen.clone();
		this.update();
	}

	/**
	 * Returns the pen to draw the selected part of the data series with.
	 */
	public WPen getSelectedSeriesPen() {
		return this.selectedSeriesPen_;
	}

	/**
	 * Set the brush to draw the handles left and right of the selected area
	 * with.
	 */
	public void setHandleBrush(final WBrush brush) {
		if (!brush.equals(this.handleBrush_)) {
			this.handleBrush_ = brush;
			this.update();
		}
	}

	/**
	 * Returns the brush to draw the handles left and right of the selected area
	 * with.
	 */
	public WBrush getHandleBrush() {
		return this.handleBrush_;
	}

	/**
	 * Set the background brush.
	 */
	public void setBackground(final WBrush brush) {
		if (!brush.equals(this.background_)) {
			this.background_ = brush;
			this.update();
		}
	}

	/**
	 * Returns the background brush.
	 */
	public WBrush getBackground() {
		return this.background_;
	}

	/**
	 * Set the brush for the selected area.
	 */
	public void setSelectedAreaBrush(final WBrush brush) {
		if (!brush.equals(this.selectedAreaBrush_)) {
			this.selectedAreaBrush_ = brush;
			this.update();
		}
	}

	/**
	 * Returns the brush for the selected area.
	 */
	public WBrush getSelectedAreaBrush() {
		return this.selectedAreaBrush_;
	}

	/**
	 * Sets an internal margin for the selection area.
	 * <p>
	 * This configures the area (in pixels) around the selection area that is
	 * available for the axes and labels, and the handles.
	 * <p>
	 * Alternatively, you can configure the chart layout to be computed
	 * automatically using
	 * {@link WAxisSliderWidget#setAutoLayoutEnabled(boolean enabled)
	 * setAutoLayoutEnabled()}.
	 * <p>
	 * 
	 * @see WAxisSliderWidget#setAutoLayoutEnabled(boolean enabled)
	 */
	public void setSelectionAreaPadding(int padding, EnumSet<Side> sides) {
		if (!EnumUtils.mask(sides, Side.Top).isEmpty()) {
			this.padding_[0] = padding;
		}
		if (!EnumUtils.mask(sides, Side.Right).isEmpty()) {
			this.padding_[1] = padding;
		}
		if (!EnumUtils.mask(sides, Side.Bottom).isEmpty()) {
			this.padding_[2] = padding;
		}
		if (!EnumUtils.mask(sides, Side.Left).isEmpty()) {
			this.padding_[3] = padding;
		}
	}

	/**
	 * Sets an internal margin for the selection area.
	 * <p>
	 * Calls {@link #setSelectionAreaPadding(int padding, EnumSet sides)
	 * setSelectionAreaPadding(padding, EnumSet.of(side, sides))}
	 */
	public final void setSelectionAreaPadding(int padding, Side side,
			Side... sides) {
		setSelectionAreaPadding(padding, EnumSet.of(side, sides));
	}

	/**
	 * Sets an internal margin for the selection area.
	 * <p>
	 * Calls {@link #setSelectionAreaPadding(int padding, EnumSet sides)
	 * setSelectionAreaPadding(padding, Side.All)}
	 */
	public final void setSelectionAreaPadding(int padding) {
		setSelectionAreaPadding(padding, Side.All);
	}

	/**
	 * Returns the internal margin for the selection area.
	 * <p>
	 * This is either the padding set through
	 * {@link WAxisSliderWidget#setSelectionAreaPadding(int padding, EnumSet sides)
	 * setSelectionAreaPadding()} or computed using
	 * {@link WAxisSliderWidget#setAutoLayoutEnabled(boolean enabled)
	 * setAutoLayoutEnabled()}.
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
			logger.error(new StringWriter().append(
					"selectionAreaPadding(): improper side.").toString());
			return 0;
		}
	}

	/**
	 * Configures the axis slider layout to be automatic.
	 * <p>
	 * This configures the selection area so that the space around it is suited
	 * for the text that is rendered.
	 */
	public void setAutoLayoutEnabled(boolean enabled) {
		this.autoPadding_ = enabled;
	}

	/**
	 * Configures the axis slider layout to be automatic.
	 * <p>
	 * Calls {@link #setAutoLayoutEnabled(boolean enabled)
	 * setAutoLayoutEnabled(true)}
	 */
	public final void setAutoLayoutEnabled() {
		setAutoLayoutEnabled(true);
	}

	/**
	 * Returns whether chart layout is computed automatically.
	 * <p>
	 * 
	 * @see WAxisSliderWidget#setAutoLayoutEnabled(boolean enabled)
	 */
	public boolean isAutoLayoutEnabled() {
		return this.autoPadding_;
	}

	/**
	 * Set whether to draw the X axis tick labels on the slider widget.
	 * <p>
	 * Labels are enabled by default.
	 */
	public void setLabelsEnabled(boolean enabled) {
		if (enabled != this.labelsEnabled_) {
			this.labelsEnabled_ = enabled;
			this.update();
		}
	}

	/**
	 * Set whether to draw the X axis tick labels on the slider widget.
	 * <p>
	 * Calls {@link #setLabelsEnabled(boolean enabled) setLabelsEnabled(true)}
	 */
	public final void setLabelsEnabled() {
		setLabelsEnabled(true);
	}

	/**
	 * Returns whether the X axis tick labels are drawn.
	 * <p>
	 * 
	 * @see WAxisSliderWidget#setLabelsEnabled(boolean enabled)
	 */
	public boolean isLabelsEnabled() {
		return this.labelsEnabled_;
	}

	protected void render(EnumSet<RenderFlag> flags) {
		super.render(flags);
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WAxisSliderWidget.js", wtjs1());
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		if (!(this.chart_ != null) || !this.chart_.cObjCreated_) {
			return;
		}
		if (this.chart_.getSeries(this.seriesColumn_).getType() != SeriesType.LineSeries
				&& this.chart_.getSeries(this.seriesColumn_).getType() != SeriesType.CurveSeries) {
			if (this.getMethod() == WPaintedWidget.Method.HtmlCanvas) {
				StringBuilder ss = new StringBuilder();
				ss.append("jQuery.removeData(").append(this.getJsRef())
						.append(",'sobj');");
				ss.append("\nif (").append(this.getObjJsRef()).append(") {")
						.append(this.getObjJsRef())
						.append(".canvas.style.cursor = 'auto';")
						.append("setTimeout(").append(this.getObjJsRef())
						.append(".repaint,0);}\n");
				this.doJavaScript(ss.toString());
			}
			logger.error(new StringWriter()
					.append("WAxisSliderWidget is not associated with a line or curve series.")
					.toString());
			return;
		}
		WPainter painter = new WPainter(paintDevice);
		boolean horizontal = this.chart_.getOrientation() == Orientation.Vertical;
		double w = horizontal ? this.getWidth().getValue() : this.getHeight()
				.getValue();
		double h = horizontal ? this.getHeight().getValue() : this.getWidth()
				.getValue();
		boolean autoPadding = this.autoPadding_;
		if (autoPadding
				&& EnumUtils.mask(paintDevice.getFeatures(),
						WPaintDevice.FeatureFlag.HasFontMetrics).equals(0)
				&& this.labelsEnabled_) {
			logger.error(new StringWriter()
					.append("setAutoLayout(): device does not have font metrics (not even server-side font metrics).")
					.toString());
			autoPadding = false;
		}
		if (autoPadding) {
			if (horizontal) {
				if (this.labelsEnabled_) {
					this.setSelectionAreaPadding(0, EnumSet.of(Side.Top));
					this.setSelectionAreaPadding(
							(int) (this.chart_.getAxis(Axis.XAxis)
									.calcMaxTickLabelSize(paintDevice,
											Orientation.Vertical) + 10),
							EnumSet.of(Side.Bottom));
					this.setSelectionAreaPadding((int) Math.max(
							this.chart_.getAxis(Axis.XAxis)
									.calcMaxTickLabelSize(paintDevice,
											Orientation.Horizontal) / 2, 10.0),
							EnumSet.of(Side.Left, Side.Right));
				} else {
					this.setSelectionAreaPadding(0, EnumSet.of(Side.Top));
					this.setSelectionAreaPadding(5,
							EnumSet.of(Side.Left, Side.Right, Side.Bottom));
				}
			} else {
				if (this.labelsEnabled_) {
					this.setSelectionAreaPadding(0, EnumSet.of(Side.Right));
					this.setSelectionAreaPadding((int) Math.max(
							this.chart_.getAxis(Axis.XAxis)
									.calcMaxTickLabelSize(paintDevice,
											Orientation.Vertical) / 2, 10.0),
							EnumSet.of(Side.Top, Side.Bottom));
					this.setSelectionAreaPadding(
							(int) (this.chart_.getAxis(Axis.XAxis)
									.calcMaxTickLabelSize(paintDevice,
											Orientation.Horizontal) + 10),
							EnumSet.of(Side.Left));
				} else {
					this.setSelectionAreaPadding(0, EnumSet.of(Side.Right));
					this.setSelectionAreaPadding(5,
							EnumSet.of(Side.Top, Side.Bottom, Side.Left));
				}
			}
		}
		double left = horizontal ? this.getSelectionAreaPadding(Side.Left)
				: this.getSelectionAreaPadding(Side.Top);
		double right = horizontal ? this.getSelectionAreaPadding(Side.Right)
				: this.getSelectionAreaPadding(Side.Bottom);
		double top = horizontal ? this.getSelectionAreaPadding(Side.Top) : this
				.getSelectionAreaPadding(Side.Right);
		double bottom = horizontal ? this.getSelectionAreaPadding(Side.Bottom)
				: this.getSelectionAreaPadding(Side.Left);
		double maxW = w - left - right;
		WRectF drawArea = new WRectF(left, 0, maxW, h);
		List<WAxis.Segment> segmentsBak = new ArrayList<WAxis.Segment>(
				this.chart_.getAxis(Axis.XAxis).segments_);
		double renderIntervalBak = this.chart_.getAxis(Axis.XAxis).renderInterval_;
		this.chart_.getAxis(Axis.XAxis).prepareRender(
				horizontal ? Orientation.Horizontal : Orientation.Vertical,
				drawArea.getWidth());
		final WRectF chartArea = this.chart_.chartArea_;
		WRectF selectionRect = null;
		{
			double u = -this.chart_.xTransformHandle_.getValue().getDx()
					/ (chartArea.getWidth() * this.chart_.xTransformHandle_
							.getValue().getM11());
			selectionRect = new WRectF(0, top, maxW, h - (top + bottom));
			this.transform_.setValue(new WTransform(
					1 / this.chart_.xTransformHandle_.getValue().getM11(), 0,
					0, 1, u * maxW, 0));
		}
		WRectF seriesArea = new WRectF(left, top + 5, maxW, h
				- (top + bottom + 5));
		WTransform selectionTransform = this.hv(new WTransform(1, 0, 0, 1,
				left, 0).multiply(this.transform_.getValue()));
		WRectF rect = selectionTransform.map(this.hv(selectionRect));
		painter.fillRect(
				this.hv(new WRectF(left, top, maxW, h - top - bottom)),
				this.background_);
		painter.fillRect(rect, this.selectedAreaBrush_);
		final double TICK_LENGTH = 5;
		final double ANGLE1 = 15;
		final double ANGLE2 = 80;
		double tickStart = 0.0;
		double tickEnd = 0.0;
		double labelPos = 0.0;
		AlignmentFlag labelHFlag = AlignmentFlag.AlignCenter;
		AlignmentFlag labelVFlag = AlignmentFlag.AlignMiddle;
		final WAxis axis = this.chart_.getAxis(Axis.XAxis);
		if (horizontal) {
			tickStart = 0;
			tickEnd = TICK_LENGTH;
			labelPos = TICK_LENGTH;
			labelVFlag = AlignmentFlag.AlignTop;
		} else {
			tickStart = -TICK_LENGTH;
			tickEnd = 0;
			labelPos = -TICK_LENGTH;
			labelHFlag = AlignmentFlag.AlignRight;
		}
		if (horizontal) {
			if (axis.getLabelAngle() > ANGLE1) {
				labelHFlag = AlignmentFlag.AlignRight;
				if (axis.getLabelAngle() > ANGLE2) {
					labelVFlag = AlignmentFlag.AlignMiddle;
				}
			} else {
				if (axis.getLabelAngle() < -ANGLE1) {
					labelHFlag = AlignmentFlag.AlignLeft;
					if (axis.getLabelAngle() < -ANGLE2) {
						labelVFlag = AlignmentFlag.AlignMiddle;
					}
				}
			}
		} else {
			if (axis.getLabelAngle() > ANGLE1) {
				labelVFlag = AlignmentFlag.AlignBottom;
				if (axis.getLabelAngle() > ANGLE2) {
					labelHFlag = AlignmentFlag.AlignCenter;
				}
			} else {
				if (axis.getLabelAngle() < -ANGLE1) {
					labelVFlag = AlignmentFlag.AlignTop;
					if (axis.getLabelAngle() < -ANGLE2) {
						labelHFlag = AlignmentFlag.AlignCenter;
					}
				}
			}
		}
		EnumSet<AxisProperty> axisProperties = EnumSet.of(AxisProperty.Line);
		if (this.labelsEnabled_) {
			axisProperties.add(AxisProperty.Labels);
		}
		if (horizontal) {
			axis.render(painter, axisProperties, new WPointF(
					drawArea.getLeft(), h - bottom),
					new WPointF(drawArea.getRight(), h - bottom), tickStart,
					tickEnd, labelPos, EnumSet.of(labelHFlag, labelVFlag));
			WPainterPath line = new WPainterPath();
			line.moveTo(drawArea.getLeft() + 0.5, h - (bottom - 0.5));
			line.lineTo(drawArea.getRight(), h - (bottom - 0.5));
			painter.strokePath(line, this.chart_.getAxis(Axis.XAxis).getPen());
		} else {
			axis.render(painter, axisProperties,
					new WPointF(this.getSelectionAreaPadding(Side.Left) - 1,
							drawArea.getLeft()),
					new WPointF(this.getSelectionAreaPadding(Side.Left) - 1,
							drawArea.getRight()), tickStart, tickEnd, labelPos,
					EnumSet.of(labelHFlag, labelVFlag));
			WPainterPath line = new WPainterPath();
			line.moveTo(this.getSelectionAreaPadding(Side.Left) - 0.5,
					drawArea.getLeft() + 0.5);
			line.lineTo(this.getSelectionAreaPadding(Side.Left) - 0.5,
					drawArea.getRight());
			painter.strokePath(line, this.chart_.getAxis(Axis.XAxis).getPen());
		}
		WPainterPath curve = new WPainterPath();
		{
			WTransform t = new WTransform(1, 0, 0, 1, seriesArea.getLeft(),
					seriesArea.getTop()).multiply(
					new WTransform(
							seriesArea.getWidth() / chartArea.getWidth(), 0, 0,
							seriesArea.getHeight() / chartArea.getHeight(), 0,
							0)).multiply(
					new WTransform(1, 0, 0, 1, -chartArea.getLeft(), -chartArea
							.getTop()));
			if (!horizontal) {
				t.assign(new WTransform(0, 1, 1, 0, this
						.getSelectionAreaPadding(Side.Left)
						- this.getSelectionAreaPadding(Side.Right) - 5, 0)
						.multiply(t).multiply(new WTransform(0, 1, 1, 0, 0, 0)));
			}
			curve.assign(t.map(this.chart_.pathForSeries(this.seriesColumn_)));
		}
		{
			WRectF leftHandle = this
					.hv(new WRectF(-5, top, 5, h - top - bottom));
			WTransform t = new WTransform(1, 0, 0, 1, left, -top)
					.multiply(new WTransform().translate(this.transform_
							.getValue().map(selectionRect.getTopLeft())));
			painter.fillRect(this.hv(t).map(leftHandle), this.handleBrush_);
		}
		{
			WRectF rightHandle = this
					.hv(new WRectF(0, top, 5, h - top - bottom));
			WTransform t = new WTransform(1, 0, 0, 1, left, -top)
					.multiply(new WTransform().translate(this.transform_
							.getValue().map(selectionRect.getTopRight())));
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
			leftClipPath.addRect(this.hv(new WTransform(1, 0, 0, 1,
					-selectionRect.getWidth(), 0).map(selectionRect)));
			painter.setClipPath(this.hv(
					new WTransform(1, 0, 0, 1, left, -top)
							.multiply(new WTransform()
									.translate(this.transform_.getValue().map(
											selectionRect.getTopLeft())))).map(
					leftClipPath));
			painter.setPen(this.getSeriesPen());
			painter.drawPath(curve);
			WPainterPath rightClipPath = new WPainterPath();
			rightClipPath.addRect(this.hv(new WTransform(1, 0, 0, 1,
					selectionRect.getWidth(), 0).map(selectionRect)));
			painter.setClipPath(this.hv(
					new WTransform(1, 0, 0, 1, left - selectionRect.getRight(),
							-top).multiply(new WTransform()
							.translate(this.transform_.getValue().map(
									selectionRect.getTopRight())))).map(
					rightClipPath));
			painter.drawPath(curve);
			painter.setClipping(false);
		} else {
			painter.setPen(this.getSeriesPen());
			painter.drawPath(curve);
		}
		if (this.getMethod() == WPaintedWidget.Method.HtmlCanvas) {
			WApplication app = WApplication.getInstance();
			StringBuilder ss = new StringBuilder();
			ss.append("new Wt3_3_4.WAxisSliderWidget(")
					.append(app.getJavaScriptClass()).append(",")
					.append(this.getJsRef()).append(",")
					.append(this.getObjJsRef()).append(",").append("{chart:")
					.append(this.chart_.getCObjJsRef()).append(",transform:")
					.append(this.transform_.getJsRef())
					.append(",rect:function(){return ").append(rect.getJsRef())
					.append("},drawArea:").append(drawArea.getJsRef())
					.append(",series:").append(this.seriesColumn_)
					.append("});");
			this.doJavaScript(ss.toString());
		}
		Utils.copyList(segmentsBak, this.chart_.getAxis(Axis.XAxis).segments_);
		this.chart_.getAxis(Axis.XAxis).renderInterval_ = renderIntervalBak;
	}

	private void init() {
		this.transform_ = this.createJSTransform();
		this.mouseWentDown().addListener(
				"function(o, e){var o=" + this.getSObjJsRef()
						+ ";if(o){o.mouseDown(o, e);}}");
		this.mouseWentUp().addListener(
				"function(o, e){var o=" + this.getSObjJsRef()
						+ ";if(o){o.mouseUp(o, e);}}");
		this.mouseDragged().addListener(
				"function(o, e){var o=" + this.getSObjJsRef()
						+ ";if(o){o.mouseDrag(o, e);}}");
		this.mouseMoved().addListener(
				"function(o, e){var o=" + this.getSObjJsRef()
						+ ";if(o){o.mouseMoved(o, e);}}");
		this.touchStarted().addListener(
				"function(o, e){var o=" + this.getSObjJsRef()
						+ ";if(o){o.touchStarted(o, e);}}");
		this.touchEnded().addListener(
				"function(o, e){var o=" + this.getSObjJsRef()
						+ ";if(o){o.touchEnded(o, e);}}");
		this.touchMoved().addListener(
				"function(o, e){var o=" + this.getSObjJsRef()
						+ ";if(o){o.touchMoved(o, e);}}");
		this.setSelectionAreaPadding(0, EnumSet.of(Side.Top));
		this.setSelectionAreaPadding(20, EnumSet.of(Side.Left, Side.Right));
		this.setSelectionAreaPadding(30, EnumSet.of(Side.Bottom));
		if (this.chart_ != null) {
			this.chart_.addAxisSliderWidget(this);
		}
	}

	private String getSObjJsRef() {
		return "jQuery.data(" + this.getJsRef() + ",'sobj')";
	}

	private WRectF hv(final WRectF rect) {
		boolean horizontal = this.chart_.getOrientation() == Orientation.Vertical;
		if (horizontal) {
			return rect;
		} else {
			return new WRectF(this.getWidth().getValue() - rect.getY()
					- rect.getHeight(), rect.getX(), rect.getHeight(),
					rect.getWidth());
		}
	}

	private WTransform hv(final WTransform t) {
		boolean horizontal = this.chart_.getOrientation() == Orientation.Vertical;
		if (horizontal) {
			return t;
		} else {
			return new WTransform(0, 1, 1, 0, 0, 0).multiply(t).multiply(
					new WTransform(0, 1, 1, 0, 0, 0));
		}
	}

	private WCartesianChart chart_;
	private int seriesColumn_;
	private WPen seriesPen_;
	private WPen selectedSeriesPen_;
	private WBrush handleBrush_;
	private WBrush background_;
	private WBrush selectedAreaBrush_;
	private boolean autoPadding_;
	private boolean labelsEnabled_;
	private int[] padding_ = new int[4];
	private WJavaScriptHandle<WTransform> transform_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WAxisSliderWidget",
				"function(y,d,m,h){function C(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function I(){K(m.repaint)}function s(){I();var a=h.transform,b=a[4]/h.drawArea[2];h.chart.setXRange(h.series,b,a[0]+b)}function q(){return!h.chart.config.isHorizontal}function D(a,b,c){return q()?a.y>=t(b)&&a.y<=u(b)&&a.x>v(b)-c/2&&a.x<v(b)+c/2:a.x>=v(b)&&a.x<=w(b)&&a.y>t(b)-c/2&&a.y<t(b)+c/2}function E(a,b,c){return q()? a.y>=t(b)&&a.y<=u(b)&&a.x>w(b)-c/2&&a.x<w(b)+c/2:a.x>=v(b)&&a.x<=w(b)&&a.y>u(b)-c/2&&a.y<u(b)+c/2}function z(a,b){return q()?a.y>=t(b)&&a.y<=u(b)&&a.x>v(b)&&a.x<w(b):a.x>=v(b)&&a.x<=w(b)&&a.y>t(b)&&a.y<u(b)}function F(a){var b=h.transform,c=h.drawArea,e=b[4]/c[2];b=b[0]+e;a=(e*c[2]+a)/c[2];if(!(1/(b-a)>h.chart.config.maxZoom[0])){if(a<0)a=0;if(a>1)a=1;n.changeRange(a,b);s()}}function G(a){var b=h.transform,c=h.drawArea,e=b[4]/c[2];a=((b[0]+e)*c[2]+a)/c[2];if(!(1/(a-e)>h.chart.config.maxZoom[0])){if(a< 0)a=0;if(a>1)a=1;n.changeRange(e,a);s()}}function J(a){var b=h.transform,c=h.drawArea,e=b[4]/c[2];b=b[0]+e;e=e*c[2];var f=e+a;if(f<0){a=-e;f=0}f=f/c[2];b=b*c[2];a=b+a;if(a>c[2]){a=c[2]-b;f=e+a;f=f/c[2];a=c[2]}n.changeRange(f,a/c[2]);s()}var M=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,0)}}(),H=false,K=function(a){if(!H){H=true;M(function(){a();H=false})}},o={};jQuery.data(d,\"sobj\",this);var n= this,g=y.WT;m.canvas.style.msTouchAction=\"none\";var p=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){if(pointers.length>0&&!p)p=true;else if(pointers.length<=0&&p)p=false}function b(k){if(C(k)){k.preventDefault();pointers.push(k);a();o.start(d,{touches:pointers.slice(0)})}}function c(k){if(p)if(C(k)){k.preventDefault();var l;for(l=0;l<pointers.length;++l)if(pointers[l].pointerId===k.pointerId){pointers.splice(l,1);break}a();o.end(d,{touches:pointers.slice(0),changedTouches:[]})}} function e(k){if(C(k)){k.preventDefault();var l;for(l=0;l<pointers.length;++l)if(pointers[l].pointerId===k.pointerId){pointers[l]=k;break}a();o.moved(d,{touches:pointers.slice(0)})}}pointers=[];var f=jQuery.data(d,\"eobj\");if(f)if(window.PointerEvent){d.removeEventListener(\"pointerdown\",f.pointerDown);d.removeEventListener(\"pointerup\",f.pointerUp);d.removeEventListener(\"pointerout\",f.pointerUp);d.removeEventListener(\"pointermove\",f.pointerMove)}else{d.removeEventListener(\"MSPointerDown\",f.pointerDown); d.removeEventListener(\"MSPointerUp\",f.pointerUp);d.removeEventListener(\"MSPointerOut\",f.pointerUp);d.removeEventListener(\"MSPointerMove\",f.pointerMove)}jQuery.data(d,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:e});if(window.PointerEvent){d.addEventListener(\"pointerdown\",b);d.addEventListener(\"pointerup\",c);d.addEventListener(\"pointerout\",c);d.addEventListener(\"pointermove\",e)}else{d.addEventListener(\"MSPointerDown\",b);d.addEventListener(\"MSPointerUp\",c);d.addEventListener(\"MSPointerOut\",c);d.addEventListener(\"MSPointerMove\", e)}})();var v=g.gfxUtils.rect_left,w=g.gfxUtils.rect_right,t=g.gfxUtils.rect_top,u=g.gfxUtils.rect_bottom,i=null,j=null;this.changeRange=function(a,b){if(a<0)a=0;if(b>1)b=1;var c=h.drawArea;h.transform[0]=b-a;h.transform[4]=a*c[2];I()};this.mouseDown=function(a,b){if(!p){i=g.widgetCoordinates(d,b);a=h.rect();if(D(i,a,10))j=1;else if(E(i,a,10))j=3;else if(z(i,a))j=2;else{j=null;return}g.cancelEvent(b)}};this.mouseUp=function(a,b){if(!p){i=null;if(j!==null){j=null;g.cancelEvent(b)}}};this.mouseDrag= function(a,b){if(!p)if(j){g.cancelEvent(b);a=g.widgetCoordinates(d,b);if(i===null)i=a;else{b=q()?a.x-i.x:a.y-i.y;switch(j){case 1:F(b);break;case 2:J(b);break;case 3:G(b);break}i=a;s()}}};this.mouseMoved=function(a,b){setTimeout(function(){if(!p)if(!j){var c=g.widgetCoordinates(d,b),e=h.rect();m.canvas.style.cursor=D(c,e,10)||E(c,e,10)?q()?\"col-resize\":\"row-resize\":z(c,e)?\"move\":\"auto\"}},0)};var x=false,r=false,A=null;o.start=function(a,b){x=b.touches.length===1;r=b.touches.length===2;if(x){i=g.widgetCoordinates(m.canvas, b.touches[0]);a=h.rect();if(D(i,a,20))j=1;else if(E(i,a,20))j=3;else if(z(i,a))j=2;else{j=null;return}g.capture(null);g.capture(m.canvas);b.preventDefault&&b.preventDefault()}else if(r){j=null;var c=[g.widgetCoordinates(m.canvas,b.touches[0]),g.widgetCoordinates(m.canvas,b.touches[1])];a=h.rect();if(z(c[0],a)&&z(c[1],a)){A=q()?Math.abs(c[0].x-c[1].x):Math.abs(c[0].y-c[1].y);g.capture(null);g.capture(m.canvas);b.preventDefault&&b.preventDefault()}}};o.end=function(a,b){var c=Array.prototype.slice.call(b.touches); a=x;var e=r,f=c.length===0;x=c.length===1;r=c.length===2;f||function(){var k;for(k=0;k<b.changedTouches.length;++k)(function(){for(var l=b.changedTouches[k].identifier,B=0;B<c.length;++B)if(c[B].identifier===l){c.splice(B,1);return}})()}();f=c.length===0;x=c.length===1;r=c.length===2;if(f&&a){i=null;if(j===null)return;j=null;g.cancelEvent(b)}if(x&&e){r=false;A=null;g.cancelEvent(b);o.start(d,b)}if(f&&e){r=false;A=null;g.cancelEvent(b)}};o.moved=function(a,b){if(j){b.preventDefault&&b.preventDefault(); a=g.widgetCoordinates(d,b);if(i===null)i=a;else{b=q()?a.x-i.x:a.y-i.y;switch(j){case 1:F(b);break;case 2:J(b);break;case 3:G(b);break}i=a;s()}}else if(r){b.preventDefault&&b.preventDefault();touches=[g.widgetCoordinates(m.canvas,b.touches[0]),g.widgetCoordinates(m.canvas,b.touches[1])];h.rect();a=q()?Math.abs(touches[0].x-touches[1].x):Math.abs(touches[0].y-touches[1].y);b=a-A;F(-b/2);G(b/2);A=a;s()}};this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))h[b]=a[b];s()};n.updateConfig({}); if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){n.touchStarted=o.start;n.touchEnded=o.end;n.touchMoved=o.moved}else{y=function(){};n.touchStarted=y;n.touchEnded=y;n.touchMoved=y}}");
	}
}
