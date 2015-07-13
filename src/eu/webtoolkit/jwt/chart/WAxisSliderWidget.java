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
		this.margin_ = 10;
		this.seriesPen_ = new WPen();
		this.selectedSeriesPen_ = this.seriesPen_;
		this.handleBrush_ = new WBrush(new WColor(0, 0, 200));
		this.background_ = new WBrush(new WColor(230, 230, 230));
		this.selectedAreaBrush_ = new WBrush(new WColor(255, 255, 255));
		this.axisMargin_ = -1;
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
		this.margin_ = 10;
		this.seriesPen_ = new WPen();
		this.selectedSeriesPen_ = this.seriesPen_;
		this.handleBrush_ = new WBrush(new WColor(0, 0, 200));
		this.background_ = new WBrush(new WColor(230, 230, 230));
		this.selectedAreaBrush_ = new WBrush(new WColor(255, 255, 255));
		this.axisMargin_ = -1;
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
			if (!this.selectedSeriesPen_.equals(pen)) {
				this.selectedSeriesPen_ = pen.clone();
			}
		} else {
			this.selectedSeriesPen_ = pen.clone();
		}
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
	 * Set the margin for the axis to be drawn in.
	 * <p>
	 * If set to -1, the margin will be calculated automatically, if possible.
	 * If this is not possible (no font metrics support), the margin will be set
	 * to 25.
	 */
	public void setAxisMargin(double margin) {
		if (margin != this.axisMargin_) {
			this.axisMargin_ = margin;
			this.update();
		}
	}

	/**
	 * Return the margin for the axis to be drawn in.
	 * <p>
	 * 
	 * @see WAxisSliderWidget#setAxisMargin(double margin)
	 */
	public double getAxisMargin() {
		return this.axisMargin_;
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
		WPainter painter = new WPainter(paintDevice);
		boolean horizontal = this.chart_.getOrientation() == Orientation.Vertical;
		double w = horizontal ? this.getWidth().getValue() : this.getHeight()
				.getValue();
		double h = horizontal ? this.getHeight().getValue() : this.getWidth()
				.getValue();
		WRectF drawArea = new WRectF(this.margin_, 0, w - 2 * this.margin_, h);
		this.chart_.getAxis(Axis.XAxis).prepareRender(
				horizontal ? Orientation.Horizontal : Orientation.Vertical,
				drawArea.getWidth());
		double axisMargin = this.axisMargin_;
		if (axisMargin == -1
				&& !EnumUtils.mask(paintDevice.getFeatures(),
						WPaintDevice.FeatureFlag.HasFontMetrics).isEmpty()) {
			axisMargin = this.chart_.getAxis(Axis.XAxis).calcMaxTickLabelSize(
					paintDevice,
					horizontal ? Orientation.Vertical : Orientation.Horizontal) + 10;
		} else {
			axisMargin = 25;
		}
		final WRectF chartArea = this.chart_.chartArea_;
		WRectF selectionRect = null;
		{
			double maxW = w - 2 * this.margin_;
			double u = -this.chart_.xTransform_.getValue().getDx()
					/ (chartArea.getWidth() * this.chart_.xTransform_
							.getValue().getM11());
			selectionRect = new WRectF(0, 0, maxW, h - axisMargin);
			this.transform_.setValue(new WTransform(1 / this.chart_.xTransform_
					.getValue().getM11(), 0, 0, 1, u * maxW, 0));
		}
		WRectF seriesArea = new WRectF(this.margin_, 5, w - 2 * this.margin_, h
				- (axisMargin + 5));
		WTransform selectionTransform = this.hv(new WTransform(1, 0, 0, 1,
				this.margin_, 0).multiply(this.transform_.getValue()));
		WRectF rect = selectionTransform.map(this.hv(selectionRect));
		painter.fillRect(this.hv(new WRectF(this.margin_, 0, w - 2
				* this.margin_, h - axisMargin)), this.background_);
		painter.fillRect(rect, this.selectedAreaBrush_);
		if (horizontal) {
			this.chart_.getAxis(Axis.XAxis).render(
					painter,
					EnumSet.of(AxisProperty.Labels, AxisProperty.Line),
					new WPointF(drawArea.getLeft(), h - axisMargin),
					new WPointF(drawArea.getRight(), h - axisMargin),
					0,
					5,
					5,
					EnumSet.of(AlignmentFlag.AlignCenter,
							AlignmentFlag.AlignTop));
			WPainterPath line = new WPainterPath();
			line.moveTo(drawArea.getLeft() + 0.5, h - (axisMargin - 0.5));
			line.lineTo(drawArea.getRight(), h - (axisMargin - 0.5));
			painter.strokePath(line, this.chart_.getAxis(Axis.XAxis).getPen());
		} else {
			this.chart_.getAxis(Axis.XAxis).render(
					painter,
					EnumSet.of(AxisProperty.Labels, AxisProperty.Line),
					new WPointF(axisMargin - 1, drawArea.getLeft()),
					new WPointF(axisMargin - 1, drawArea.getRight()),
					-5,
					0,
					-5,
					EnumSet.of(AlignmentFlag.AlignRight,
							AlignmentFlag.AlignMiddle));
			WPainterPath line = new WPainterPath();
			line.moveTo(axisMargin - 0.5, drawArea.getLeft() + 0.5);
			line.lineTo(axisMargin - 0.5, drawArea.getRight());
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
				t.assign(new WTransform(0, 1, 1, 0, axisMargin, 0).multiply(t)
						.multiply(new WTransform(0, 1, 1, 0, 0, 0)));
			}
			curve.assign(t.map(this.chart_.pathForSeries(this.seriesColumn_)));
		}
		{
			WRectF leftHandle = this.hv(new WRectF(-5, 0, 5, h - axisMargin));
			WTransform t = new WTransform(1, 0, 0, 1, this.margin_, 0)
					.multiply(new WTransform().translate(this.transform_
							.getValue().map(selectionRect.getTopLeft())));
			painter.fillRect(this.hv(t).map(leftHandle), this.handleBrush_);
		}
		{
			WRectF rightHandle = this.hv(new WRectF(0, 0, 5, h - axisMargin));
			WTransform t = new WTransform(1, 0, 0, 1, this.margin_, 0)
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
					new WTransform(1, 0, 0, 1, this.margin_, 0)
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
					new WTransform(1, 0, 0, 1, this.margin_
							- selectionRect.getRight(), 0)
							.multiply(new WTransform()
									.translate(this.transform_.getValue().map(
											selectionRect.getTopRight()))))
					.map(rightClipPath));
			painter.drawPath(curve);
			painter.setClipping(false);
		} else {
			painter.setPen(this.getSeriesPen());
			painter.drawPath(curve);
		}
		if (this.getMethod() == WPaintedWidget.Method.HtmlCanvas) {
			WApplication app = WApplication.getInstance();
			StringBuilder ss = new StringBuilder();
			ss.append("new Wt3_3_4.WAxisSliderWidget(").append(
					app.getJavaScriptClass()).append(",").append(
					this.getJsRef()).append(",").append(this.getObjJsRef())
					.append(",").append("{chart:").append(
							this.chart_.getCObjJsRef()).append(",transform:")
					.append(this.transform_.getJsRef()).append(
							",rect:function(){return ").append(rect.getJsRef())
					.append("},drawArea:").append(drawArea.getJsRef()).append(
							",series:").append(this.seriesColumn_)
					.append("});");
			this.doJavaScript(ss.toString());
		}
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
					- rect.getHeight(), rect.getX(), rect.getHeight(), rect
					.getWidth());
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
	private int margin_;
	private WPen seriesPen_;
	private WPen selectedSeriesPen_;
	private WBrush handleBrush_;
	private WBrush background_;
	private WBrush selectedAreaBrush_;
	private double axisMargin_;
	private WJavaScriptHandle<WTransform> transform_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WAxisSliderWidget",
				"function(G,j,l,g){function z(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function E(){H(l.repaint)}function q(){E();var a=g.transform,b=a[4]/g.drawArea[2];g.chart.setXRange(g.series,b,a[0]+b)}function n(){return!g.chart.config.isHorizontal}function A(a,b,c){return n()?a.y>=r(b)&&a.y<=s(b)&&a.x>t(b)-c/2&&a.x<t(b)+c/2:a.x>=t(b)&&a.x<=u(b)&&a.y>r(b)-c/2&&a.y<r(b)+c/2}function B(a,b,c){return n()? a.y>=r(b)&&a.y<=s(b)&&a.x>u(b)-c/2&&a.x<u(b)+c/2:a.x>=t(b)&&a.x<=u(b)&&a.y>s(b)-c/2&&a.y<s(b)+c/2}function w(a,b){return n()?a.y>=r(b)&&a.y<=s(b)&&a.x>t(b)&&a.x<u(b):a.x>=t(b)&&a.x<=u(b)&&a.y>r(b)&&a.y<s(b)}function C(a){var b=g.transform,c=g.drawArea,f=b[4]/c[2];b=b[0]+f;a=(f*c[2]+a)/c[2];if(!(1/(b-a)>g.chart.config.maxZoom[0])){if(a<0)a=0;if(a>1)a=1;o.changeRange(a,b);q()}}function D(a){var b=g.transform,c=g.drawArea,f=b[4]/c[2];a=((b[0]+f)*c[2]+a)/c[2];if(!(1/(a-f)>g.chart.config.maxZoom[0])){if(a< 0)a=0;if(a>1)a=1;o.changeRange(f,a);q()}}function F(a){var b=g.transform,c=g.drawArea,f=b[4]/c[2];b=b[0]+f;f=f*c[2];var d=f+a;if(d<0){a=-f;d=0}d=d/c[2];b=b*c[2];a=b+a;if(a>c[2]){a=c[2]-b;d=f+a;d=d/c[2];a=c[2]}o.changeRange(d,a/c[2]);q()}var H=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,0)}}();jQuery.data(j,\"sobj\",this);var o=this,e=G.WT;l.canvas.style.msTouchAction=\"none\";var m=false;if(!window.TouchEvent&& (window.MSPointerEvent||window.PointerEvent))(function(){function a(){if(pointers.length>0&&!m)m=true;else if(pointers.length<=0&&m)m=false}function b(d){if(z(d)){d.preventDefault();pointers.push(d);a();o.touchStarted(j,{touches:pointers.slice(0)})}}function c(d){if(m)if(z(d)){d.preventDefault();var k;for(k=0;k<pointers.length;++k)if(pointers[k].pointerId===d.pointerId){pointers.splice(k,1);break}a();o.touchEnded(j,{touches:pointers.slice(0),changedTouches:[]})}}function f(d){if(z(d)){d.preventDefault(); var k;for(k=0;k<pointers.length;++k)if(pointers[k].pointerId===d.pointerId){pointers[k]=d;break}a();o.touchMoved(j,{touches:pointers.slice(0)})}}pointers=[];if(window.PointerEvent){j.addEventListener(\"pointerdown\",b);j.addEventListener(\"pointerup\",c);j.addEventListener(\"pointerout\",c);j.addEventListener(\"pointermove\",f)}else{j.addEventListener(\"MSPointerDown\",b);j.addEventListener(\"MSPointerUp\",c);j.addEventListener(\"MSPointerOut\",c);j.addEventListener(\"MSPointerMove\",f)}})();var t=e.gfxUtils.rect_left, u=e.gfxUtils.rect_right,r=e.gfxUtils.rect_top,s=e.gfxUtils.rect_bottom,h=null,i=null;this.changeRange=function(a,b){if(a<0)a=0;if(b>1)b=1;var c=g.drawArea;g.transform[0]=b-a;g.transform[4]=a*c[2];E()};this.mouseDown=function(a,b){if(!m){h=e.widgetCoordinates(j,b);a=g.rect();if(A(h,a,10))i=1;else if(B(h,a,10))i=3;else if(w(h,a))i=2;else{i=null;return}e.cancelEvent(b)}};this.mouseUp=function(a,b){if(!m){h=null;if(i!==null){i=null;e.cancelEvent(b)}}};this.mouseDrag=function(a,b){if(!m)if(i){e.cancelEvent(b); a=e.widgetCoordinates(j,b);if(h===null)h=a;else{b=n()?a.x-h.x:a.y-h.y;switch(i){case 1:C(b);break;case 2:F(b);break;case 3:D(b);break}h=a;q()}}};this.mouseMoved=function(a,b){setTimeout(function(){if(!m)if(!i){var c=e.widgetCoordinates(j,b),f=g.rect();l.canvas.style.cursor=A(c,f,10)||B(c,f,10)?n()?\"col-resize\":\"row-resize\":w(c,f)?\"move\":\"auto\"}},0)};var v=false,p=false,x=null;this.touchStarted=function(a,b){v=b.touches.length===1;p=b.touches.length===2;if(v){h=e.widgetCoordinates(l.canvas,b.touches[0]); a=g.rect();if(A(h,a,20))i=1;else if(B(h,a,20))i=3;else if(w(h,a))i=2;else{i=null;return}e.capture(null);e.capture(l.canvas);b.preventDefault&&b.preventDefault()}else if(p){i=null;var c=[e.widgetCoordinates(l.canvas,b.touches[0]),e.widgetCoordinates(l.canvas,b.touches[1])];a=g.rect();if(w(c[0],a)&&w(c[1],a)){x=n()?Math.abs(c[0].x-c[1].x):Math.abs(c[0].y-c[1].y);e.capture(null);e.capture(l.canvas);b.preventDefault&&b.preventDefault()}}};this.touchEnded=function(a,b){var c=Array.prototype.slice.call(b.touches); a=v;var f=p,d=c.length===0;v=c.length===1;p=c.length===2;d||function(){var k;for(k=0;k<b.changedTouches.length;++k)(function(){for(var I=b.changedTouches[k].identifier,y=0;y<c.length;++y)if(c[y].identifier===I){c.splice(y,1);return}})()}();d=c.length===0;v=c.length===1;p=c.length===2;if(d&&a){h=null;if(i===null)return;i=null;e.cancelEvent(b)}if(v&&f){p=false;x=null;e.cancelEvent(b);o.touchStarted(j,b)}if(d&&f){p=false;x=null;e.cancelEvent(b)}};this.touchMoved=function(a,b){if(i){e.cancelEvent(b); a=e.widgetCoordinates(j,b);if(h===null)h=a;else{b=n()?a.x-h.x:a.y-h.y;switch(i){case 1:C(b);break;case 2:F(b);break;case 3:D(b);break}h=a;q()}}else if(p){touches=[e.widgetCoordinates(l.canvas,b.touches[0]),e.widgetCoordinates(l.canvas,b.touches[1])];g.rect();a=n()?Math.abs(touches[0].x-touches[1].x):Math.abs(touches[0].y-touches[1].y);b=a-x;C(-b/2);D(b/2);x=a}};this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))g[b]=a[b];q()};o.updateConfig({})}");
	}
}
