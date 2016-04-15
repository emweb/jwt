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
 * A curve label.
 * <p>
 * 
 * Curve labels can be added with
 * {@link WCartesianChart#addCurveLabel(CurveLabel label)
 * WCartesianChart#addCurveLabel()}. They are associated with a particular
 * series, and are drawn at the given point in model coordinates. When the chart
 * is transformed (zoom or pan) or the associated series is manipulated, the
 * curve label&apos;s position will change, but not its size.
 * <p>
 * <div align="center"> <img src="doc-files//CurveLabel.png"
 * alt="A curve label">
 * <p>
 * <strong>A curve label</strong>
 * </p>
 * </div>
 */
public class CurveLabel {
	private static Logger logger = LoggerFactory.getLogger(CurveLabel.class);

	/**
	 * Create a new curve label.
	 * <p>
	 * Create a new curve label for given series, at the given point with the
	 * given text.
	 */
	public CurveLabel(final WDataSeries series, final WPointF point,
			final String label) {
		this.series_ = series;
		this.point_ = point;
		this.label_ = label;
		this.offset_ = new WPointF(60, -20);
		this.width_ = 0;
		this.linePen_ = new WPen(new WColor(0, 0, 0));
		this.textPen_ = new WPen(new WColor(0, 0, 0));
		this.boxBrush_ = new WBrush(new WColor(255, 255, 255));
		this.markerBrush_ = new WBrush(new WColor(0, 0, 0));
	}

	/**
	 * Set the series this curve label is associated with.
	 */
	public void setSeries(final WDataSeries series) {
		this.series_ = series;
	}

	/**
	 * Get the series this curve label is associated with.
	 * <p>
	 * 
	 * @see CurveLabel#setSeries(WDataSeries series)
	 */
	public WDataSeries getSeries() {
		return this.series_;
	}

	/**
	 * Set the point in model coordinates this label is associated with.
	 */
	public void setPoint(final WPointF point) {
		this.point_ = point;
	}

	/**
	 * Get the point in model coordinates this label is associated with.
	 * <p>
	 * 
	 * @see CurveLabel#setPoint(WPointF point)
	 */
	public WPointF getPoint() {
		return this.point_;
	}

	/**
	 * Set the label that should be drawn in the box.
	 */
	public void setLabel(final String label) {
		this.label_ = label;
	}

	/**
	 * Get the label that should be drawn in the box.
	 * <p>
	 * 
	 * @see CurveLabel#setLabel(String label)
	 */
	public String getLabel() {
		return this.label_;
	}

	/**
	 * Set the offset the text should be placed at.
	 * <p>
	 * The offset is defined in pixels, with x values going from left to right,
	 * and y values from top to bottom.
	 * <p>
	 * The default offset is (60, -20), which means the middle of the
	 * {@link CurveLabel#getLabel() getLabel()} is drawn 60 pixels to the right,
	 * and 20 pixels above the point.
	 */
	public void setOffset(final WPointF offset) {
		this.offset_ = offset;
	}

	/**
	 * Get the offset the text should be placed at.
	 * <p>
	 * 
	 * @see CurveLabel#setOffset(WPointF offset)
	 */
	public WPointF getOffset() {
		return this.offset_;
	}

	/**
	 * Set the width of the box in pixels.
	 * <p>
	 * If the width is 0 (the default), server side font metrics will be used to
	 * determine the size of the box.
	 */
	public void setWidth(int width) {
		this.width_ = width;
	}

	/**
	 * Get the width of the box in pixels.
	 * <p>
	 * 
	 * @see CurveLabel#setWidth(int width)
	 */
	public int getWidth() {
		return this.width_;
	}

	/**
	 * Set the pen to use for the connecting line.
	 * <p>
	 * This sets the pen to use for the line connecting the
	 * {@link CurveLabel#getPoint() point} to the box with the
	 * {@link CurveLabel#getLabel() label} at {@link CurveLabel#getOffset()
	 * offset} pixels from the point.
	 */
	public void setLinePen(final WPen pen) {
		this.linePen_ = pen;
	}

	/**
	 * Get the pen to use for the connecting line.
	 * <p>
	 * 
	 * @see CurveLabel#setLinePen(WPen pen)
	 */
	public WPen getLinePen() {
		return this.linePen_;
	}

	/**
	 * Set the pen for the text in the box.
	 */
	public void setTextPen(final WPen pen) {
		this.textPen_ = pen;
	}

	/**
	 * Get the pen for the text in the box.
	 * <p>
	 * 
	 * @see CurveLabel#setTextPen(WPen pen)
	 */
	public WPen getTextPen() {
		return this.textPen_;
	}

	/**
	 * Set the brush to use for the box around the text.
	 * <p>
	 * This sets the brush used to fill the box with the text defined in
	 * {@link CurveLabel#getLabel() getLabel()}.
	 */
	public void setBoxBrush(final WBrush brush) {
		this.boxBrush_ = brush;
	}

	/**
	 * Get the brush to use for the box around the text.
	 * <p>
	 * 
	 * @see CurveLabel#setBoxBrush(WBrush brush)
	 */
	public WBrush getBoxBrush() {
		return this.boxBrush_;
	}

	/**
	 * Set the brush used to fill the circle at {@link CurveLabel#getPoint()
	 * getPoint()}.
	 */
	public void setMarkerBrush(final WBrush brush) {
		this.markerBrush_ = brush;
	}

	/**
	 * Get the brush used to fill the circle at {@link CurveLabel#getPoint()
	 * getPoint()}.
	 * <p>
	 * 
	 * @see CurveLabel#setMarkerBrush(WBrush brush)
	 */
	public WBrush getMarkerBrush() {
		return this.markerBrush_;
	}

	public void render(final WPainter painter) {
		WRectF rect = null;
		{
			double rectWidth = DEFAULT_CURVE_LABEL_WIDTH;
			if (this.getWidth() != 0) {
				rectWidth = this.getWidth();
			} else {
				if (!EnumUtils.mask(painter.getDevice().getFeatures(),
						WPaintDevice.FeatureFlag.HasFontMetrics).isEmpty()) {
					WMeasurePaintDevice device = new WMeasurePaintDevice(
							painter.getDevice());
					WPainter measPainter = new WPainter(device);
					measPainter.drawText(new WRectF(0, 0, 100, 100), EnumSet
							.of(AlignmentFlag.AlignMiddle,
									AlignmentFlag.AlignCenter),
							TextFlag.TextSingleLine, this.getLabel(),
							(WPointF) null);
					rectWidth = device.getBoundingRect().getWidth()
							+ CURVE_LABEL_PADDING / 2;
				}
			}
			rect = new WRectF(this.getOffset().getX() - rectWidth / 2, this
					.getOffset().getY() - 10, rectWidth, 20).getNormalized();
		}
		WPointF closestAnchor = new WPointF();
		{
			List<WPointF> anchorPoints = new ArrayList<WPointF>();
			anchorPoints.add(new WPointF(rect.getLeft(), rect.getCenter()
					.getY()));
			anchorPoints.add(new WPointF(rect.getRight(), rect.getCenter()
					.getY()));
			anchorPoints
					.add(new WPointF(rect.getCenter().getX(), rect.getTop()));
			anchorPoints.add(new WPointF(rect.getCenter().getX(), rect
					.getBottom()));
			double minSquareDist = Double.POSITIVE_INFINITY;
			for (int k = 0; k < anchorPoints.size(); ++k) {
				final WPointF anchorPoint = anchorPoints.get(k);
				double d = anchorPoint.getX() * anchorPoint.getX()
						+ anchorPoint.getY() * anchorPoint.getY();
				if (d < minSquareDist
						&& (k == 0 || !checkIntersectVertical(new WPointF(),
								anchorPoint, rect.getTop(), rect.getBottom(),
								rect.getLeft()))
						&& (k == 1 || !checkIntersectVertical(new WPointF(),
								anchorPoint, rect.getTop(), rect.getBottom(),
								rect.getRight()))
						&& (k == 2 || !checkIntersectHorizontal(new WPointF(),
								anchorPoint, rect.getLeft(), rect.getRight(),
								rect.getTop()))
						&& (k == 3 || !checkIntersectHorizontal(new WPointF(),
								anchorPoint, rect.getLeft(), rect.getRight(),
								rect.getBottom()))) {
					closestAnchor = anchorPoint;
					minSquareDist = d;
				}
			}
		}
		WTransform translation = painter.getWorldTransform();
		painter.setWorldTransform(new WTransform());
		WPainterPath connectorLine = new WPainterPath();
		connectorLine.moveTo(0, 0);
		connectorLine.lineTo(closestAnchor);
		painter.strokePath(translation.map(connectorLine).getCrisp(),
				this.getLinePen());
		WPainterPath circle = new WPainterPath();
		circle.addEllipse(-2.5, -2.5, 5, 5);
		painter.fillPath(translation.map(circle), this.getMarkerBrush());
		WPainterPath rectPath = new WPainterPath();
		rectPath.addRect(rect);
		painter.fillPath(translation.map(rectPath), this.getBoxBrush());
		painter.strokePath(translation.map(rectPath).getCrisp(),
				this.getLinePen());
		painter.setPen(this.getTextPen());
		painter.drawText(translation.map(rect), EnumSet.of(
				AlignmentFlag.AlignMiddle, AlignmentFlag.AlignCenter),
				TextFlag.TextSingleLine, this.getLabel(), (WPointF) null);
	}

	private WDataSeries series_;
	private WPointF point_;
	private String label_;
	private WPointF offset_;
	private int width_;
	private WPen linePen_;
	private WPen textPen_;
	private WBrush boxBrush_;
	private WBrush markerBrush_;

	private static boolean checkIntersectHorizontal(final WPointF p1,
			final WPointF p2, double minX, double maxX, double y) {
		if (p1.getY() == p2.getY()) {
			return p1.getY() == y;
		}
		double t = (y - p1.getY()) / (p2.getY() - p1.getY());
		if (t <= 0 || t >= 1) {
			return false;
		}
		double x = p1.getX() * (1 - t) + p2.getX() * t;
		return x > minX && x < maxX;
	}

	private static boolean checkIntersectVertical(final WPointF p1,
			final WPointF p2, double minY, double maxY, double x) {
		return checkIntersectHorizontal(new WPointF(p1.getY(), p1.getX()),
				new WPointF(p2.getY(), p2.getX()), minY, maxY, x);
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"ChartCommon",
				"function(v){function z(a,b,e,c){function d(f){return e?b[f]:b[l-1-f]}function j(f){for(;d(f)[2]===t||d(f)[2]===w;)f--;return f}var q=h;if(c)q=i;var l=b.length;c=Math.floor(l/2);c=j(c);var x=0,m=l,k=false;if(d(0)[q]>a)return e?-1:l;if(d(l-1)[q]<a)return e?l:-1;for(;!k;){var g=c+1;if(g<l&&(d(g)[2]===t||d(g)[2]===w))g+=2;if(d(c)[q]>a){m=c;c=Math.floor((m+x)/2);c=j(c)}else if(d(c)[q]===a)k=true;else if(g<l&&d(g)[q]>a)k=true;else if(g<l&&d(g)[q]=== a){c=g;k=true}else{x=c;c=Math.floor((m+x)/2);c=j(c)}}return e?c:l-1-c}function C(a,b){return b[0][a]<b[b.length-1][a]}var t=2,w=3,h=0,i=1,A=this;v=v.WT.gfxUtils;var D=v.rect_top,E=v.rect_bottom,B=v.rect_left,F=v.rect_right,G=v.transform_mult;this.findClosestPoint=function(a,b,e){var c=h;if(e)c=i;var d=C(c,b);e=z(a,b,d,e);if(e<0)e=0;if(e>=b.length)return[b[b.length-1][h],b[b.length-1][i]];if(e>=b.length)e=b.length-2;if(b[e][c]===a)return[b[e][h],b[e][i]];var j=d?e+1:e-1;if(d&&b[j][2]==t)j+=2;if(!d&& j<0)return[b[e][h],b[e][i]];if(!d&&j>0&&b[j][2]==w)j-=2;d=Math.abs(a-b[e][c]);a=Math.abs(b[j][c]-a);return d<a?[b[e][h],b[e][i]]:[b[j][h],b[j][i]]};this.minMaxY=function(a,b){b=b?h:i;for(var e=a[0][b],c=a[0][b],d=1;d<a.length;++d)if(a[d][2]!==t&&a[d][2]!==w&&a[d][2]!==5){if(a[d][b]>c)c=a[d][b];if(a[d][b]<e)e=a[d][b]}return[e,c]};this.projection=function(a,b){var e=Math.cos(a);a=Math.sin(a);var c=e*a,d=-b[0]*e-b[1]*a;return[e*e,c,c,a*a,e*d+b[0],a*d+b[1]]};this.distanceSquared=function(a,b){a=[b[h]- a[h],b[i]-a[i]];return a[h]*a[h]+a[i]*a[i]};this.distanceLessThanRadius=function(a,b,e){return e*e>=A.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var e;if(a.x!==undefined){e=a.x;a=a.y}else{e=a[0];a=a[1]}return e>=B(b)&&e<=F(b)&&a>=D(b)&&a<=E(b)};this.toDisplayCoord=function(a,b,e,c,d){if(e){a=[(a[h]-d[0])/d[2],(a[i]-d[1])/d[3]];c=[c[0]+a[i]*c[2],c[1]+a[h]*c[3]]}else{a=[(a[h]-d[0])/d[2],1-(a[i]-d[1])/d[3]];c=[c[0]+ a[h]*c[2],c[1]+a[i]*c[3]]}return G(b,c)};this.findYRange=function(a,b,e,c,d,j,q){if(a.length!==0){var l=A.toDisplayCoord([b,0],[1,0,0,1,0,0],c,d,j),x=A.toDisplayCoord([e,0],[1,0,0,1,0,0],c,d,j),m=c?i:h,k=c?h:i,g=C(m,a),f=z(l[m],a,g,c),n=z(x[m],a,g,c),o,p,r=Infinity,s=-Infinity,y=f===n&&f===a.length||f===-1&&n===-1;if(!y){if(g)if(f<0)f=0;else{f++;if(a[f]&&a[f][2]===t)f+=2}else if(f>=a.length-1)f=a.length-2;if(!g&&n<0)n=0;for(o=Math.min(f,n);o<=Math.max(f,n)&&o<a.length;++o)if(a[o][2]!==t&&a[o][2]!== w){if(a[o][k]<r)r=a[o][k];if(a[o][k]>s)s=a[o][k]}if(g&&f>0||!g&&f<a.length-1){if(g){p=f-1;if(a[p]&&a[p][2]===w)p-=2}else{p=f+1;if(a[p]&&a[p][2]===t)p+=2}o=(l[m]-a[p][m])/(a[f][m]-a[p][m]);f=a[p][k]+o*(a[f][k]-a[p][k]);if(f<r)r=f;if(f>s)s=f}if(g&&n<a.length-1||!g&&n>0){if(g){g=n+1;if(a[g][2]===t)g+=2}else{g=n-1;if(a[g][2]===w)g-=2}o=(x[m]-a[n][m])/(a[g][m]-a[n][m]);f=a[n][k]+o*(a[g][k]-a[n][k]);if(f<r)r=f;if(f>s)s=f}}var u;a=j[2]/(e-b);b=c?2:3;if(!y){u=d[b]/(s-r);u=d[b]/(d[b]/u+20);if(u>q[k])u=q[k]}c= c?[l[i]-D(d),!y?(r+s)/2-d[2]/u/2-B(d):0]:[l[h]-B(d),!y?-((r+s)/2+d[3]/u/2-E(d)):0];return{xZoom:a,yZoom:u,panPoint:c}}}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(la,D,u,k){function J(a){return a===undefined}function m(){return k.modelArea}function Ea(){return k.followCurve}function ma(){return k.crosshair||Ea()!==-1}function y(){return k.isHorizontal}function e(a){if(a===g)return k.xTransform;if(a===f)return k.yTransform}function i(){return k.area}function l(){return k.insideArea}function na(a){return J(a)?k.series:k.series[a]}function S(a){return na(a).transform}function $a(a){return y()? z([0,1,1,0,0,0],z(S(a),[0,1,1,0,0,0])):S(a)}function Fa(a){return na(a).curve}function ab(){return k.seriesSelection}function bb(){return k.sliders}function cb(){return k.hasToolTips}function db(){return k.coordinateOverlayPadding}function ua(){return k.curveManipulation}function G(){return k.maxZoom}function F(){return k.pens}function Ga(){return k.selectedCurve}function oa(a){a.preventDefault&&a.preventDefault()}function T(a,b){D.addEventListener(a,b)}function N(a,b){D.removeEventListener(a,b)} function A(a){return a.length}function Pa(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Ha(){if(q){if(q.tooltipTimeout){clearTimeout(q.tooltipTimeout);q.tooltipTimeout=null}if(q.tooltipOuterDiv){document.body.removeChild(q.tooltipOuterDiv);q.tooltipEl=null;q.tooltipOuterDiv=null}}}function va(){if(k.notifyTransform.x||k.notifyTransform.y){if(Ia){window.clearTimeout(Ia);Ia=null}Ia=setTimeout(function(){if(k.notifyTransform.x&&!eb(Qa,e(g))){la.emit(u.widget, \"xTransformChanged\");Y(Qa,e(g))}if(k.notifyTransform.y&&!eb(Ra,e(f))){la.emit(u.widget,\"yTransformChanged\");Y(Ra,e(f))}},nb)}}function Z(){var a,b;if(y()){a=n(i());b=t(i());return z([0,1,1,0,a,b],z(e(g),z(e(f),[0,1,1,0,-b,-a])))}else{a=n(i());b=v(i());return z([1,0,0,-1,a,b],z(e(g),z(e(f),[1,0,0,-1,-a,b])))}}function K(){return z(Z(),l())}function aa(a,b){if(J(b))b=false;a=b?a:z(wa(Z()),a);a=y()?[(a[f]-i()[1])/i()[3],(a[g]-i()[0])/i()[2]]:[(a[g]-i()[0])/i()[2],1-(a[f]-i()[1])/i()[3]];return[m()[0]+ a[g]*m()[2],m()[1]+a[f]*m()[3]]}function xa(a,b){if(J(b))b=false;return ba.toDisplayCoord(a,b?[1,0,0,1,0,0]:Z(),y(),i(),m())}function ya(){var a,b;if(y()){a=(aa([0,t(i())])[0]-m()[0])/m()[2];b=(aa([0,v(i())])[0]-m()[0])/m()[2]}else{a=(aa([n(i()),0])[0]-m()[0])/m()[2];b=(aa([p(i()),0])[0]-m()[0])/m()[2]}var c;for(c=0;c<A(bb());++c){var h=$(\"#\"+bb()[c]);if(h)(h=h.data(\"sobj\"))&&h.changeRange(a,b)}}function O(){Ha();if(cb()&&q.tooltipPosition)q.tooltipTimeout=setTimeout(function(){fb()},gb);ca&&hb(function(){u.repaint(); ma()&&Sa()})}function Sa(){if(ca){var a=E.getContext(\"2d\");a.clearRect(0,0,E.width,E.height);a.save();a.beginPath();a.moveTo(n(i()),t(i()));a.lineTo(p(i()),t(i()));a.lineTo(p(i()),v(i()));a.lineTo(n(i()),v(i()));a.closePath();a.clip();var b=z(wa(Z()),w),c=w[g],h=w[f];if(Ea()!==-1){b=ob(y()?b[f]:b[g],Fa(Ea()),y());h=z(Z(),z($a(Ea()),b));c=h[g];h=h[f];w[g]=c;w[f]=h}b=y()?[(b[f]-i()[1])/i()[3],(b[g]-i()[0])/i()[2]]:[(b[g]-i()[0])/i()[2],1-(b[f]-i()[1])/i()[3]];b=[m()[0]+b[g]*m()[2],m()[1]+b[f]*m()[3]]; a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var d=b[0].toFixed(2);b=b[1].toFixed(2);if(d===\"-0.00\")d=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+d+\",\"+b+\")\",p(i())-db()[0],t(i())+db()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(t(i()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(v(i()))+0.5);a.moveTo(Math.floor(n(i()))+0.5,Math.floor(h)+0.5);a.lineTo(Math.floor(p(i()))+0.5,Math.floor(h)+0.5);a.stroke();a.restore()}}function pb(a){return t(a)<= t(i())+Ja&&v(a)>=v(i())-Ja&&n(a)<=n(i())+Ja&&p(a)>=p(i())-Ja}function U(a){var b=K();if(y())if(a===pa)a=qa;else if(a===qa)a=pa;if(J(a)||a===pa)if(e(g)[0]<1){e(g)[0]=1;b=K()}if(J(a)||a===qa)if(e(f)[3]<1){e(f)[3]=1;b=K()}if(J(a)||a===pa){if(n(b)>n(l())){b=n(l())-n(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}if(p(b)<p(l())){b=p(l())-p(b);if(y())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=K()}}if(J(a)||a===qa){if(t(b)>t(l())){b=t(l())-t(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;b= K()}if(v(b)<v(l())){b=v(l())-v(b);if(y())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;K()}}va()}function fb(){la.emit(u.widget,\"loadTooltip\",q.tooltipPosition[g],q.tooltipPosition[f])}function qb(){if(ma()&&(J(E)||u.canvas.width!==E.width||u.canvas.height!==E.height)){if(E){E.parentNode.removeChild(E);jQuery.removeData(D,\"oobj\");E=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",u.canvas.width);a.setAttribute(\"height\",u.canvas.height);a.style.position=\"absolute\";a.style.display= \"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}u.canvas.parentNode.appendChild(a);E=a;jQuery.data(D,\"oobj\",E)}else if(!J(E)&&!ma()){E.parentNode.removeChild(E);jQuery.removeData(D,\"oobj\");E=undefined}w||(w=xa([(n(m())+p(m()))/2,(t(m())+v(m()))/2]))}function Ta(a,b){if(ra){var c=Date.now();if(J(b))b=c-V;var h={x:0,y:0},d=K(),o=rb;if(b>2*za){ca=false;var r=Math.floor(b/za-1),s;for(s=0;s<r;++s){Ta(a,za); if(!ra){ca=true;O();return}}b-=r*za;ca=true}if(j.x===Infinity||j.x===-Infinity)j.x=j.x>0?da:-da;if(isFinite(j.x)){j.x/=1+ib*b;d[0]+=j.x*b;if(n(d)>n(l())){j.x+=-o*(n(d)-n(l()))*b;j.x*=0.7}else if(p(d)<p(l())){j.x+=-o*(p(d)-p(l()))*b;j.x*=0.7}if(Math.abs(j.x)<Ua)if(n(d)>n(l()))j.x=Ua;else if(p(d)<p(l()))j.x=-Ua;if(Math.abs(j.x)>da)j.x=(j.x>0?1:-1)*da;h.x=j.x*b}if(j.y===Infinity||j.y===-Infinity)j.y=j.y>0?da:-da;if(isFinite(j.y)){j.y/=1+ib*b;d[1]+=j.y*b;if(t(d)>t(l())){j.y+=-o*(t(d)-t(l()))*b;j.y*=0.7}else if(v(d)< v(l())){j.y+=-o*(v(d)-v(l()))*b;j.y*=0.7}if(Math.abs(j.y)<0.001)if(t(d)>t(l()))j.y=0.001;else if(v(d)<v(l()))j.y=-0.001;if(Math.abs(j.y)>da)j.y=(j.y>0?1:-1)*da;h.y=j.y*b}d=K();P(h,sa);a=K();if(n(d)>n(l())&&n(a)<=n(l())){j.x=0;P({x:-h.x,y:0},sa);U(pa)}if(p(d)<p(l())&&p(a)>=p(l())){j.x=0;P({x:-h.x,y:0},sa);U(pa)}if(t(d)>t(l())&&t(a)<=t(l())){j.y=0;P({x:0,y:-h.y},sa);U(qa)}if(v(d)<v(l())&&v(a)>=v(l())){j.y=0;P({x:0,y:-h.y},sa);U(qa)}if(Math.abs(j.x)<jb&&Math.abs(j.y)<jb&&pb(a)){U();ra=false;B=null;j.x= 0;j.y=0;V=null;x=[]}else{V=c;ca&&Ka(Ta)}}}function La(){var a,b,c=kb(e(g)[0])-1;if(c>=A(F().x))c=A(F().x)-1;for(a=0;a<A(F().x);++a)if(c===a)for(b=0;b<A(F().x[a]);++b)F().x[a][b].color[3]=k.penAlpha.x[b];else for(b=0;b<A(F().x[a]);++b)F().x[a][b].color[3]=0;c=kb(e(f)[3])-1;if(c>=A(F().y))c=A(F().y)-1;for(a=0;a<A(F().y);++a)if(c===a)for(b=0;b<A(F().y[a]);++b)F().y[a][b].color[3]=k.penAlpha.y[b];else for(b=0;b<A(F().y[a]);++b)F().y[a][b].color[3]=0}function P(a,b){if(J(b))b=0;var c=aa(w);if(y())a={x:a.y, y:-a.x};if(b&sa){e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;va()}else if(b&lb){b=K();if(n(b)>n(l())){if(a.x>0)a.x/=1+(n(b)-n(l()))*Ma}else if(p(b)<p(l()))if(a.x<0)a.x/=1+(p(l())-p(b))*Ma;if(t(b)>t(l())){if(a.y>0)a.y/=1+(t(b)-t(l()))*Ma}else if(v(b)<v(l()))if(a.y<0)a.y/=1+(v(l())-v(b))*Ma;e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;w[g]+=a.x;w[f]+=a.y;va()}else{e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;w[g]+=a.x;w[f]+=a.y;U()}a=xa(c);w[g]=a[g];w[f]=a[f];O();ya()}function Aa(a,b,c){var h=aa(w),d;d=y()?[a.y- t(i()),a.x-n(i())]:z(wa([1,0,0,-1,n(i()),v(i())]),[a.x,a.y]);a=d[0];d=d[1];var o=Math.pow(1.2,y()?c:b);b=Math.pow(1.2,y()?b:c);if(e(g)[0]*o>G()[g])o=G()[g]/e(g)[0];if(o<1||e(g)[0]!==G()[g])Na(e(g),z([o,0,0,1,a-o*a,0],e(g)));if(e(f)[3]*b>G()[f])b=G()[f]/e(f)[3];if(b<1||e(f)[3]!==G()[f])Na(e(f),z([1,0,0,b,0,d-b*d],e(f)));U();h=xa(h);w[g]=h[g];w[f]=h[f];La();O();ya()}jQuery.data(D,\"cobj\",this);var ea=this,C=la.WT;ea.config=k;var H=C.gfxUtils,z=H.transform_mult,wa=H.transform_inverted,Y=H.transform_assign, eb=H.transform_equal,sb=H.transform_apply,t=H.rect_top,v=H.rect_bottom,n=H.rect_left,p=H.rect_right,ba=C.chartCommon,tb=ba.minMaxY,ob=ba.findClosestPoint,ub=ba.projection,mb=ba.distanceLessThanRadius,kb=ba.toZoomLevel,ta=ba.isPointInRect,vb=ba.findYRange,za=17,Ka=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,za)}}(),Va=false,hb=function(a){if(!Va){Va=true;Ka(function(){a();Va=false})}};if(window.MSPointerEvent|| window.PointerEvent){D.style.touchAction=\"none\";u.canvas.style.msTouchAction=\"none\";u.canvas.style.touchAction=\"none\"}var sa=1,lb=2,pa=1,qa=2,g=0,f=1,nb=250,gb=500,ib=0.003,rb=2.0E-4,Ma=0.07,Ja=3,Ua=0.001,da=1.5,jb=0.02,ga=jQuery.data(D,\"eobj2\");if(!ga){ga={};ga.contextmenuListener=function(a){oa(a);N(\"contextmenu\",ga.contextmenuListener)}}jQuery.data(D,\"eobj2\",ga);var Q={},ha=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ha=A(d)>0}function b(r){if(Pa(r)){oa(r);d.push(r); a();Q.start(D,{touches:d.slice(0)})}}function c(r){if(ha)if(Pa(r)){oa(r);var s;for(s=0;s<A(d);++s)if(d[s].pointerId===r.pointerId){d.splice(s,1);break}a();Q.end(D,{touches:d.slice(0),changedTouches:[]})}}function h(r){if(Pa(r)){oa(r);var s;for(s=0;s<A(d);++s)if(d[s].pointerId===r.pointerId){d[s]=r;break}a();Q.moved(D,{touches:d.slice(0)})}}var d=[],o=jQuery.data(D,\"eobj\");if(o)if(window.PointerEvent){N(\"pointerdown\",o.pointerDown);N(\"pointerup\",o.pointerUp);N(\"pointerout\",o.pointerUp);N(\"pointermove\", o.pointerMove)}else{N(\"MSPointerDown\",o.pointerDown);N(\"MSPointerUp\",o.pointerUp);N(\"MSPointerOut\",o.pointerUp);N(\"MSPointerMove\",o.pointerMove)}jQuery.data(D,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:h});if(window.PointerEvent){T(\"pointerdown\",b);T(\"pointerup\",c);T(\"pointerout\",c);T(\"pointermove\",h)}else{T(\"MSPointerDown\",b);T(\"MSPointerUp\",c);T(\"MSPointerOut\",c);T(\"MSPointerMove\",h)}})();var E=jQuery.data(D,\"oobj\"),w=null,ca=true,B=null,x=[],W=false,fa=false,M=null,Wa=null,Xa=null,j={x:0,y:0}, ia=null,V=null,q=jQuery.data(D,\"tobj\");if(!q){q={};jQuery.data(D,\"tobj\",q)}var Ba=null,ra=false,Ia=null,Qa=[0,0,0,0,0,0];Y(Qa,e(g));var Ra=[0,0,0,0,0,0];Y(Ra,e(f));var Na=function(a,b){Y(a,b);va()};u.combinedTransform=Z;this.updateTooltip=function(a){Ha();if(a)if(q.tooltipPosition){q.toolTipEl=document.createElement(\"div\");q.toolTipEl.className=k.ToolTipInnerStyle;q.toolTipEl.innerHTML=a;q.tooltipOuterDiv=document.createElement(\"div\");q.tooltipOuterDiv.className=k.ToolTipOuterStyle;document.body.appendChild(q.tooltipOuterDiv); q.tooltipOuterDiv.appendChild(q.toolTipEl);var b=C.widgetPageCoordinates(u.canvas);a=q.tooltipPosition[g]+b.x;b=q.tooltipPosition[f]+b.y;C.fitToWindow(q.tooltipOuterDiv,a+10,b+10,a-10,b-10)}};this.mouseMove=function(a,b){setTimeout(function(){Ha();if(!ha){var c=C.widgetCoordinates(u.canvas,b);if(ta(c,i())){if(cb()){q.tooltipPosition=[c.x,c.y];q.tooltipTimeout=setTimeout(function(){fb()},gb)}if(ma()&&ca){w=[c.x,c.y];hb(Sa)}}}},0)};this.mouseOut=function(){Ha()};this.mouseDown=function(a,b){if(!ha){a= C.widgetCoordinates(u.canvas,b);if(ta(a,i()))B=a}};this.mouseUp=function(){ha||(B=null)};this.mouseDrag=function(a,b){if(!ha)if(B!==null){a=C.widgetCoordinates(u.canvas,b);if(ta(a,i())){if(C.buttons===1)if(ua()){b=Ga();if(na(b)){var c;c=y()?a.x-B.x:a.y-B.y;Y(S(b),z([1,0,0,1,0,c/e(f)[3]],S(b)));O()}}else k.pan&&P({x:a.x-B.x,y:a.y-B.y});B=a}}};this.clicked=function(a,b){if(!ha)if(B===null)if(ab()){a=C.widgetCoordinates(u.canvas,b);la.emit(u.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a, b){a=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;var c=k.wheelActions[a];if(!J(c)){var h=C.widgetCoordinates(u.canvas,b);if(ta(h,i())){var d=C.normalizeWheel(b);if(a===0&&ua()){c=Ga();a=-d.spinY;if(na(c)){d=$a(c);d=sb(d,Fa(c));d=tb(d,y());d=(d[0]+d[1])/2;C.cancelEvent(b);b=Math.pow(1.2,a);Y(S(c),z([1,0,0,b,0,d-b*d],S(c)));O()}}else if((c===4||c===5||c===6)&&k.pan){a=e(g)[4];h=e(f)[5];if(c===6)P({x:-d.pixelX,y:-d.pixelY});else if(c===5)P({x:0,y:-d.pixelX-d.pixelY});else c===4&&P({x:-d.pixelX- d.pixelY,y:0});if(a!==e(g)[4]||h!==e(f)[5])C.cancelEvent(b)}else if(k.zoom){C.cancelEvent(b);a=-d.spinY;if(a===0)a=-d.spinX;if(c===1)Aa(h,0,a);else if(c===0)Aa(h,a,0);else if(c===2)Aa(h,a,a);else if(c===3)d.pixelX!==0?Aa(h,a,0):Aa(h,0,a)}}}};var wb=function(){ab()&&la.emit(u.widget,\"seriesSelected\",B.x,B.y)};Q.start=function(a,b,c){W=A(b.touches)===1;fa=A(b.touches)===2;if(W){ra=false;a=C.widgetCoordinates(u.canvas,b.touches[0]);if(!ta(a,i()))return;Ba=ma()&&mb(w,[a.x,a.y],30)?1:0;V=Date.now();B= a;if(Ba!==1){c||(ia=window.setTimeout(wb,200));T(\"contextmenu\",ga.contextmenuListener)}C.capture(null);C.capture(u.canvas)}else if(fa&&(k.zoom||ua())){ra=false;x=[C.widgetCoordinates(u.canvas,b.touches[0]),C.widgetCoordinates(u.canvas,b.touches[1])].map(function(h){return[h.x,h.y]});if(!x.every(function(h){return ta(h,i())})){fa=null;return}C.capture(null);C.capture(u.canvas);M=Math.atan2(x[1][1]-x[0][1],x[1][0]-x[0][0]);Wa=[(x[0][0]+x[1][0])/2,(x[0][1]+x[1][1])/2];c=Math.abs(Math.sin(M));a=Math.abs(Math.cos(M)); M=c<Math.sin(0.125*Math.PI)?0:a<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(M)>0?Math.PI/4:-Math.PI/4;Xa=ub(M,Wa)}else return;oa(b)};Q.end=function(a,b){if(ia){window.clearTimeout(ia);ia=null}window.setTimeout(function(){N(\"contextmenu\",ga.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),h=A(c)===0;h||function(){var d;for(d=0;d<A(b.changedTouches);++d)(function(){for(var o=b.changedTouches[d].identifier,r=0;r<A(c);++r)if(c[r].identifier===o){c.splice(r,1);return}})()}();h=A(c)=== 0;W=A(c)===1;fa=A(c)===2;if(h){Oa=null;if(Ba===0&&(isFinite(j.x)||isFinite(j.y))&&k.rubberBand){V=Date.now();ra=true;Ka(Ta)}else{ea.mouseUp(null,null);c=[];Xa=Wa=M=null;if(V!=null){Date.now();V=null}}Ba=null}else if(W||fa)Q.start(a,b,true)};var Oa=null,ja=null,Ya=null;Q.moved=function(a,b){if(W||fa)if(!(W&&B==null)){oa(b);ja=C.widgetCoordinates(u.canvas,b.touches[0]);if(A(b.touches)>1)Ya=C.widgetCoordinates(u.canvas,b.touches[1]);if(W&&ia&&!mb([ja.x,ja.y],[B.x,B.y],3)){window.clearTimeout(ia);ia= null}Oa||(Oa=setTimeout(function(){if(W&&ua()){var c=Ga();if(na(c)){var h=ja,d;d=y()?(h.x-B.x)/e(f)[3]:(h.y-B.y)/e(f)[3];S(c)[5]+=d;B=h;O()}}else if(W){h=ja;d=Date.now();c={x:h.x-B.x,y:h.y-B.y};var o=d-V;V=d;if(Ba===1){w[g]+=c.x;w[f]+=c.y;ma()&&ca&&Ka(Sa)}else if(k.pan){j.x=c.x/o;j.y=c.y/o;P(c,k.rubberBand?lb:0)}B=h}else if(fa&&ua()){var r=y()?g:f;d=[ja,Ya].map(function(I){return y()?[I.x,ka]:[Ca,I.y]});c=Math.abs(x[1][r]-x[0][r]);o=Math.abs(d[1][r]-d[0][r]);var s=c>0?o/c:1;if(o===c)s=1;var ka=z(wa(Z()), [0,(x[0][r]+x[1][r])/2])[1],Da=z(wa(Z()),[0,(d[0][r]+d[1][r])/2])[1];c=Ga();if(na(c)){Y(S(c),z([1,0,0,s,0,-s*ka+Da],S(c)));B=h;O();x=d}}else if(fa&&k.zoom){h=aa(w);var Ca=(x[0][0]+x[1][0])/2;ka=(x[0][1]+x[1][1])/2;d=[ja,Ya].map(function(I){return M===0?[I.x,ka]:M===Math.PI/2?[Ca,I.y]:z(Xa,[I.x,I.y])});c=Math.abs(x[1][0]-x[0][0]);o=Math.abs(d[1][0]-d[0][0]);var X=c>0?o/c:1;if(o===c||M===Math.PI/2)X=1;var Za=(d[0][0]+d[1][0])/2;c=Math.abs(x[1][1]-x[0][1]);o=Math.abs(d[1][1]-d[0][1]);s=c>0?o/c:1;if(o=== c||M===0)s=1;Da=(d[0][1]+d[1][1])/2;y()&&function(){var I=X;X=s;s=I;I=Za;Za=Da;Da=I;I=Ca;Ca=ka;ka=I}();if(e(g)[0]*X>G()[g])X=G()[g]/e(g)[0];if(e(f)[3]*s>G()[f])s=G()[f]/e(f)[3];if(X!==1&&(X<1||e(g)[0]!==G()[g]))Na(e(g),z([X,0,0,1,-X*Ca+Za,0],e(g)));if(s!==1&&(s<1||e(f)[3]!==G()[f]))Na(e(f),z([1,0,0,s,0,-s*ka+Da],e(f)));U();h=xa(h);w[g]=h[g];w[f]=h[f];x=d;La();O();ya()}Oa=null},1))}};this.setXRange=function(a,b,c){b=m()[0]+m()[2]*b;c=m()[0]+m()[2]*c;if(n(m())>p(m())){if(b>n(m()))b=n(m());if(c<p(m()))c= p(m())}else{if(b<n(m()))b=n(m());if(c>p(m()))c=p(m())}a=Fa(a);a=vb(a,b,c,y(),i(),m(),G());b=a.xZoom;c=a.yZoom;a=a.panPoint;var h=aa(w);e(g)[0]=b;if(c)e(f)[3]=c;e(g)[4]=-a[g]*b;if(c)e(f)[5]=-a[f]*c;va();b=xa(h);w[g]=b[g];w[f]=b[f];U();La();O();ya()};this.getSeries=function(a){return Fa(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))k[b]=a[b];qb();La();O();ya()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ea.touchStart= Q.start;ea.touchEnd=Q.end;ea.touchMoved=Q.moved}else{H=function(){};ea.touchStart=H;ea.touchEnd=H;ea.touchMoved=H}}");
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
