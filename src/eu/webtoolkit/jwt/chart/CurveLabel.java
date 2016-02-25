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
				"function(la,D,t,k){function I(a){return a===undefined}function m(){return k.modelArea}function Fa(){return k.followCurve}function ma(){return k.crosshair||Fa()!==-1}function x(){return k.isHorizontal}function e(a){if(a===g)return k.xTransform;if(a===f)return k.yTransform}function i(){return k.area}function l(){return k.insideArea}function na(a){return I(a)?k.series:k.series[a]}function Q(a){return na(a).transform}function bb(a){return x()? y([0,1,1,0,0,0],y(Q(a),[0,1,1,0,0,0])):Q(a)}function Ga(a){return na(a).curve}function cb(){return k.seriesSelection}function db(){return k.sliders}function mb(){return k.hasToolTips}function eb(){return k.coordinateOverlayPadding}function ua(){return k.curveManipulation}function F(){return k.maxZoom}function E(){return k.pens}function Ha(){return k.selectedCurve}function oa(a){a.preventDefault&&a.preventDefault()}function S(a,b){D.addEventListener(a,b)}function M(a,b){D.removeEventListener(a,b)} function z(a){return a.length}function Ra(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Ia(){if(Ja){clearTimeout(Ja);Ja=null}if(X){document.body.removeChild(X);X=nb=null}}function va(){if(k.notifyTransform.x||k.notifyTransform.y){if(Ka){window.clearTimeout(Ka);Ka=null}Ka=setTimeout(function(){if(k.notifyTransform.x&&!fb(Sa,e(g))){la.emit(t.widget,\"xTransformChanged\");Y(Sa,e(g))}if(k.notifyTransform.y&&!fb(Ta,e(f))){la.emit(t.widget,\"yTransformChanged\"); Y(Ta,e(f))}},ob)}}function Z(){var a,b;if(x()){a=n(i());b=s(i());return y([0,1,1,0,a,b],y(e(g),y(e(f),[0,1,1,0,-b,-a])))}else{a=n(i());b=u(i());return y([1,0,0,-1,a,b],y(e(g),y(e(f),[1,0,0,-1,-a,b])))}}function J(){return y(Z(),l())}function aa(a,b){if(I(b))b=false;a=b?a:y(wa(Z()),a);a=x()?[(a[f]-i()[1])/i()[3],(a[g]-i()[0])/i()[2]]:[(a[g]-i()[0])/i()[2],1-(a[f]-i()[1])/i()[3]];return[m()[0]+a[g]*m()[2],m()[1]+a[f]*m()[3]]}function xa(a,b){if(I(b))b=false;return ba.toDisplayCoord(a,b?[1,0,0,1,0,0]: Z(),x(),i(),m())}function ya(){var a,b;if(x()){a=(aa([0,s(i())])[0]-m()[0])/m()[2];b=(aa([0,u(i())])[0]-m()[0])/m()[2]}else{a=(aa([n(i()),0])[0]-m()[0])/m()[2];b=(aa([p(i()),0])[0]-m()[0])/m()[2]}var c;for(c=0;c<z(db());++c){var h=$(\"#\"+db()[c]);if(h)(h=h.data(\"sobj\"))&&h.changeRange(a,b)}}function N(){Ia();ca&&gb(function(){t.repaint();ma()&&Ua()})}function Ua(){if(ca){var a=C.getContext(\"2d\");a.clearRect(0,0,C.width,C.height);a.save();a.beginPath();a.moveTo(n(i()),s(i()));a.lineTo(p(i()),s(i())); a.lineTo(p(i()),u(i()));a.lineTo(n(i()),u(i()));a.closePath();a.clip();var b=y(wa(Z()),v),c=v[g],h=v[f];if(Fa()!==-1){b=pb(x()?b[f]:b[g],Ga(Fa()),x());h=y(Z(),y(bb(Fa()),b));c=h[g];h=h[f];v[g]=c;v[f]=h}b=x()?[(b[f]-i()[1])/i()[3],(b[g]-i()[0])/i()[2]]:[(b[g]-i()[0])/i()[2],1-(b[f]-i()[1])/i()[3]];b=[m()[0]+b[g]*m()[2],m()[1]+b[f]*m()[3]];a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var d=b[0].toFixed(2);b=b[1].toFixed(2);if(d===\"-0.00\")d=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+ d+\",\"+b+\")\",p(i())-eb()[0],s(i())+eb()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(s(i()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(u(i()))+0.5);a.moveTo(Math.floor(n(i()))+0.5,Math.floor(h)+0.5);a.lineTo(Math.floor(p(i()))+0.5,Math.floor(h)+0.5);a.stroke();a.restore()}}function qb(a){return s(a)<=s(i())+La&&u(a)>=u(i())-La&&n(a)<=n(i())+La&&p(a)>=p(i())-La}function T(a){var b=J();if(x())if(a===pa)a=qa;else if(a===qa)a=pa;if(I(a)||a===pa)if(e(g)[0]< 1){e(g)[0]=1;b=J()}if(I(a)||a===qa)if(e(f)[3]<1){e(f)[3]=1;b=J()}if(I(a)||a===pa){if(n(b)>n(l())){b=n(l())-n(b);if(x())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=J()}if(p(b)<p(l())){b=p(l())-p(b);if(x())e(f)[5]=e(f)[5]+b;else e(g)[4]=e(g)[4]+b;b=J()}}if(I(a)||a===qa){if(s(b)>s(l())){b=s(l())-s(b);if(x())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;b=J()}if(u(b)<u(l())){b=u(l())-u(b);if(x())e(g)[4]=e(g)[4]+b;else e(f)[5]=e(f)[5]-b;J()}}va()}function rb(){la.emit(t.widget,\"loadTooltip\",za[g],za[f])}function sb(){if(ma()&& (I(C)||t.canvas.width!==C.width||t.canvas.height!==C.height)){if(C){C.parentNode.removeChild(C);jQuery.removeData(D,\"oobj\");C=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",t.canvas.width);a.setAttribute(\"height\",t.canvas.height);a.style.position=\"absolute\";a.style.display=\"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}t.canvas.parentNode.appendChild(a);C=a;jQuery.data(D,\"oobj\", C)}else if(!I(C)&&!ma()){C.parentNode.removeChild(C);jQuery.removeData(D,\"oobj\");C=undefined}v||(v=xa([(n(m())+p(m()))/2,(s(m())+u(m()))/2]))}function Va(a,b){if(ra){var c=Date.now();if(I(b))b=c-U;var h={x:0,y:0},d=J(),o=tb;if(b>2*Aa){ca=false;var q=Math.floor(b/Aa-1),r;for(r=0;r<q;++r){Va(a,Aa);if(!ra){ca=true;N();return}}b-=q*Aa;ca=true}if(j.x===Infinity||j.x===-Infinity)j.x=j.x>0?da:-da;if(isFinite(j.x)){j.x/=1+hb*b;d[0]+=j.x*b;if(n(d)>n(l())){j.x+=-o*(n(d)-n(l()))*b;j.x*=0.7}else if(p(d)<p(l())){j.x+= -o*(p(d)-p(l()))*b;j.x*=0.7}if(Math.abs(j.x)<Wa)if(n(d)>n(l()))j.x=Wa;else if(p(d)<p(l()))j.x=-Wa;if(Math.abs(j.x)>da)j.x=(j.x>0?1:-1)*da;h.x=j.x*b}if(j.y===Infinity||j.y===-Infinity)j.y=j.y>0?da:-da;if(isFinite(j.y)){j.y/=1+hb*b;d[1]+=j.y*b;if(s(d)>s(l())){j.y+=-o*(s(d)-s(l()))*b;j.y*=0.7}else if(u(d)<u(l())){j.y+=-o*(u(d)-u(l()))*b;j.y*=0.7}if(Math.abs(j.y)<0.001)if(s(d)>s(l()))j.y=0.001;else if(u(d)<u(l()))j.y=-0.001;if(Math.abs(j.y)>da)j.y=(j.y>0?1:-1)*da;h.y=j.y*b}d=J();O(h,sa);a=J();if(n(d)> n(l())&&n(a)<=n(l())){j.x=0;O({x:-h.x,y:0},sa);T(pa)}if(p(d)<p(l())&&p(a)>=p(l())){j.x=0;O({x:-h.x,y:0},sa);T(pa)}if(s(d)>s(l())&&s(a)<=s(l())){j.y=0;O({x:0,y:-h.y},sa);T(qa)}if(u(d)<u(l())&&u(a)>=u(l())){j.y=0;O({x:0,y:-h.y},sa);T(qa)}if(Math.abs(j.x)<ib&&Math.abs(j.y)<ib&&qb(a)){T();ra=false;A=null;j.x=0;j.y=0;U=null;w=[]}else{U=c;ca&&Ma(Va)}}}function Na(){var a,b,c=jb(e(g)[0])-1;if(c>=z(E().x))c=z(E().x)-1;for(a=0;a<z(E().x);++a)if(c===a)for(b=0;b<z(E().x[a]);++b)E().x[a][b].color[3]=k.penAlpha.x[b]; else for(b=0;b<z(E().x[a]);++b)E().x[a][b].color[3]=0;c=jb(e(f)[3])-1;if(c>=z(E().y))c=z(E().y)-1;for(a=0;a<z(E().y);++a)if(c===a)for(b=0;b<z(E().y[a]);++b)E().y[a][b].color[3]=k.penAlpha.y[b];else for(b=0;b<z(E().y[a]);++b)E().y[a][b].color[3]=0}function O(a,b){if(I(b))b=0;var c=aa(v);if(x())a={x:a.y,y:-a.x};if(b&sa){e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;va()}else if(b&kb){b=J();if(n(b)>n(l())){if(a.x>0)a.x/=1+(n(b)-n(l()))*Oa}else if(p(b)<p(l()))if(a.x<0)a.x/=1+(p(l())-p(b))*Oa;if(s(b)>s(l())){if(a.y> 0)a.y/=1+(s(b)-s(l()))*Oa}else if(u(b)<u(l()))if(a.y<0)a.y/=1+(u(l())-u(b))*Oa;e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;v[g]+=a.x;v[f]+=a.y;va()}else{e(g)[4]=e(g)[4]+a.x;e(f)[5]=e(f)[5]-a.y;v[g]+=a.x;v[f]+=a.y;T()}a=xa(c);v[g]=a[g];v[f]=a[f];N();ya()}function Ba(a,b,c){var h=aa(v),d;d=x()?[a.y-s(i()),a.x-n(i())]:y(wa([1,0,0,-1,n(i()),u(i())]),[a.x,a.y]);a=d[0];d=d[1];var o=Math.pow(1.2,x()?c:b);b=Math.pow(1.2,x()?b:c);if(e(g)[0]*o>F()[g])o=F()[g]/e(g)[0];if(o<1||e(g)[0]!==F()[g])Pa(e(g),y([o,0,0,1, a-o*a,0],e(g)));if(e(f)[3]*b>F()[f])b=F()[f]/e(f)[3];if(b<1||e(f)[3]!==F()[f])Pa(e(f),y([1,0,0,b,0,d-b*d],e(f)));T();h=xa(h);v[g]=h[g];v[f]=h[f];Na();N();ya()}jQuery.data(D,\"cobj\",this);var ea=this,B=la.WT;ea.config=k;var G=B.gfxUtils,y=G.transform_mult,wa=G.transform_inverted,Y=G.transform_assign,fb=G.transform_equal,ub=G.transform_apply,s=G.rect_top,u=G.rect_bottom,n=G.rect_left,p=G.rect_right,ba=B.chartCommon,vb=ba.minMaxY,pb=ba.findClosestPoint,wb=ba.projection,lb=ba.distanceLessThanRadius,jb= ba.toZoomLevel,ta=ba.isPointInRect,xb=ba.findYRange,Aa=17,Ma=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,Aa)}}(),Xa=false,gb=function(a){if(!Xa){Xa=true;Ma(function(){a();Xa=false})}};if(window.MSPointerEvent||window.PointerEvent){D.style.touchAction=\"none\";t.canvas.style.msTouchAction=\"none\";t.canvas.style.touchAction=\"none\"}var sa=1,kb=2,pa=1,qa=2,g=0,f=1,ob=250,hb=0.003,tb=2.0E-4,Oa=0.07,La= 3,Wa=0.001,da=1.5,ib=0.02,ga=jQuery.data(D,\"eobj2\");if(!ga){ga={};ga.contextmenuListener=function(a){oa(a);M(\"contextmenu\",ga.contextmenuListener)}}jQuery.data(D,\"eobj2\",ga);var P={},ha=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ha=z(d)>0}function b(q){if(Ra(q)){oa(q);d.push(q);a();P.start(D,{touches:d.slice(0)})}}function c(q){if(ha)if(Ra(q)){oa(q);var r;for(r=0;r<z(d);++r)if(d[r].pointerId===q.pointerId){d.splice(r,1);break}a();P.end(D,{touches:d.slice(0),changedTouches:[]})}} function h(q){if(Ra(q)){oa(q);var r;for(r=0;r<z(d);++r)if(d[r].pointerId===q.pointerId){d[r]=q;break}a();P.moved(D,{touches:d.slice(0)})}}var d=[],o=jQuery.data(D,\"eobj\");if(o)if(window.PointerEvent){M(\"pointerdown\",o.pointerDown);M(\"pointerup\",o.pointerUp);M(\"pointerout\",o.pointerUp);M(\"pointermove\",o.pointerMove)}else{M(\"MSPointerDown\",o.pointerDown);M(\"MSPointerUp\",o.pointerUp);M(\"MSPointerOut\",o.pointerUp);M(\"MSPointerMove\",o.pointerMove)}jQuery.data(D,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:h}); if(window.PointerEvent){S(\"pointerdown\",b);S(\"pointerup\",c);S(\"pointerout\",c);S(\"pointermove\",h)}else{S(\"MSPointerDown\",b);S(\"MSPointerUp\",c);S(\"MSPointerOut\",c);S(\"MSPointerMove\",h)}})();var C=jQuery.data(D,\"oobj\"),v=null,ca=true,A=null,w=[],V=false,fa=false,K=null,Ya=null,Za=null,j={x:0,y:0},ia=null,U=null,Ja=null,za=null,X=null,nb=null,Ca=null,ra=false,Ka=null,Sa=[0,0,0,0,0,0];Y(Sa,e(g));var Ta=[0,0,0,0,0,0];Y(Ta,e(f));var Pa=function(a,b){Y(a,b);va()};t.combinedTransform=Z;this.updateTooltip= function(a){Ia();if(a){var b=document.createElement(\"div\");b.className=k.ToolTipInnerStyle;b.innerHTML=a;X=document.createElement(\"div\");X.className=k.ToolTipOuterStyle;document.body.appendChild(X);X.appendChild(b);b=B.widgetPageCoordinates(t.canvas);a=za[g]+b.x;b=za[f]+b.y;B.fitToWindow(X,a+10,b+10,a-10,b-10)}};this.mouseMove=function(a,b){setTimeout(function(){Ia();if(!ha){var c=B.widgetCoordinates(t.canvas,b);if(ta(c,i())){if(mb()){za=[c.x,c.y];Ja=setTimeout(function(){rb()},500)}if(ma()&&ca){v= [c.x,c.y];gb(Ua)}}}},0)};this.mouseOut=function(){Ia()};this.mouseDown=function(a,b){if(!ha){a=B.widgetCoordinates(t.canvas,b);if(ta(a,i()))A=a}};this.mouseUp=function(){ha||(A=null)};this.mouseDrag=function(a,b){if(!ha)if(A!==null){a=B.widgetCoordinates(t.canvas,b);if(ta(a,i())){if(B.buttons===1)if(ua()){b=Ha();if(na(b)){var c;c=x()?a.x-A.x:a.y-A.y;Y(Q(b),y([1,0,0,1,0,c/e(f)[3]],Q(b)));N()}}else k.pan&&O({x:a.x-A.x,y:a.y-A.y});A=a}}};this.clicked=function(a,b){if(!ha)if(A===null)if(cb()){a=B.widgetCoordinates(t.canvas, b);la.emit(t.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){a=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;var c=k.wheelActions[a];if(!I(c)){var h=B.widgetCoordinates(t.canvas,b);if(ta(h,i())){var d=B.normalizeWheel(b);if(a===0&&ua()){c=Ha();a=-d.spinY;if(na(c)){d=bb(c);d=ub(d,Ga(c));d=vb(d,x());d=(d[0]+d[1])/2;B.cancelEvent(b);b=Math.pow(1.2,a);Y(Q(c),y([1,0,0,b,0,d-b*d],Q(c)));N()}}else if((c===4||c===5||c===6)&&k.pan){a=e(g)[4];h=e(f)[5];if(c===6)O({x:-d.pixelX,y:-d.pixelY}); else if(c===5)O({x:0,y:-d.pixelX-d.pixelY});else c===4&&O({x:-d.pixelX-d.pixelY,y:0});if(a!==e(g)[4]||h!==e(f)[5])B.cancelEvent(b)}else if(k.zoom){B.cancelEvent(b);a=-d.spinY;if(a===0)a=-d.spinX;if(c===1)Ba(h,0,a);else if(c===0)Ba(h,a,0);else if(c===2)Ba(h,a,a);else if(c===3)d.pixelX!==0?Ba(h,a,0):Ba(h,0,a)}}}};var yb=function(){cb()&&la.emit(t.widget,\"seriesSelected\",A.x,A.y)};P.start=function(a,b){V=z(b.touches)===1;fa=z(b.touches)===2;if(V){ra=false;a=B.widgetCoordinates(t.canvas,b.touches[0]); if(!ta(a,i()))return;Ca=ma()&&lb(v,[a.x,a.y],30)?1:0;U=Date.now();A=a;if(Ca!==1){ia=window.setTimeout(yb,200);S(\"contextmenu\",ga.contextmenuListener)}B.capture(null);B.capture(t.canvas)}else if(fa&&(k.zoom||ua())){ra=false;w=[B.widgetCoordinates(t.canvas,b.touches[0]),B.widgetCoordinates(t.canvas,b.touches[1])].map(function(h){return[h.x,h.y]});if(!w.every(function(h){return ta(h,i())})){fa=null;return}B.capture(null);B.capture(t.canvas);K=Math.atan2(w[1][1]-w[0][1],w[1][0]-w[0][0]);Ya=[(w[0][0]+ w[1][0])/2,(w[0][1]+w[1][1])/2];a=Math.abs(Math.sin(K));var c=Math.abs(Math.cos(K));K=a<Math.sin(0.125*Math.PI)?0:c<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(K)>0?Math.PI/4:-Math.PI/4;Za=wb(K,Ya)}else return;oa(b)};P.end=function(a,b){if(ia){window.clearTimeout(ia);ia=null}window.setTimeout(function(){M(\"contextmenu\",ga.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),h=z(c)===0;h||function(){var d;for(d=0;d<z(b.changedTouches);++d)(function(){for(var o=b.changedTouches[d].identifier, q=0;q<z(c);++q)if(c[q].identifier===o){c.splice(q,1);return}})()}();h=z(c)===0;V=z(c)===1;fa=z(c)===2;if(h){Qa=null;if(Ca===0&&(isFinite(j.x)||isFinite(j.y))&&k.rubberBand){U=Date.now();ra=true;Ma(Va)}else{ea.mouseUp(null,null);c=[];Za=Ya=K=null;if(U!=null){Date.now();U=null}}Ca=null}else if(V||fa)P.start(a,b)};var Qa=null,ja=null,$a=null;P.moved=function(a,b){if(V||fa)if(!(V&&A==null)){oa(b);ja=B.widgetCoordinates(t.canvas,b.touches[0]);if(z(b.touches)>1)$a=B.widgetCoordinates(t.canvas,b.touches[1]); if(V&&ia&&!lb([ja.x,ja.y],[A.x,A.y],3)){window.clearTimeout(ia);ia=null}Qa||(Qa=setTimeout(function(){if(V&&ua()){var c=Ha();if(na(c)){var h=ja,d;d=x()?(h.x-A.x)/e(f)[3]:(h.y-A.y)/e(f)[3];Q(c)[5]+=d;A=h;N()}}else if(V){h=ja;d=Date.now();c={x:h.x-A.x,y:h.y-A.y};var o=d-U;U=d;if(Ca===1){v[g]+=c.x;v[f]+=c.y;ma()&&ca&&Ma(Ua)}else if(k.pan){j.x=c.x/o;j.y=c.y/o;O(c,k.rubberBand?kb:0)}A=h}else if(fa&&ua()){var q=x()?g:f;d=[ja,$a].map(function(H){return x()?[H.x,ka]:[Da,H.y]});c=Math.abs(w[1][q]-w[0][q]); o=Math.abs(d[1][q]-d[0][q]);var r=c>0?o/c:1;if(o===c)r=1;var ka=y(wa(Z()),[0,(w[0][q]+w[1][q])/2])[1],Ea=y(wa(Z()),[0,(d[0][q]+d[1][q])/2])[1];c=Ha();if(na(c)){Y(Q(c),y([1,0,0,r,0,-r*ka+Ea],Q(c)));A=h;N();w=d}}else if(fa&&k.zoom){h=aa(v);var Da=(w[0][0]+w[1][0])/2;ka=(w[0][1]+w[1][1])/2;d=[ja,$a].map(function(H){return K===0?[H.x,ka]:K===Math.PI/2?[Da,H.y]:y(Za,[H.x,H.y])});c=Math.abs(w[1][0]-w[0][0]);o=Math.abs(d[1][0]-d[0][0]);var W=c>0?o/c:1;if(o===c||K===Math.PI/2)W=1;var ab=(d[0][0]+d[1][0])/ 2;c=Math.abs(w[1][1]-w[0][1]);o=Math.abs(d[1][1]-d[0][1]);r=c>0?o/c:1;if(o===c||K===0)r=1;Ea=(d[0][1]+d[1][1])/2;x()&&function(){var H=W;W=r;r=H;H=ab;ab=Ea;Ea=H;H=Da;Da=ka;ka=H}();if(e(g)[0]*W>F()[g])W=F()[g]/e(g)[0];if(e(f)[3]*r>F()[f])r=F()[f]/e(f)[3];if(W!==1&&(W<1||e(g)[0]!==F()[g]))Pa(e(g),y([W,0,0,1,-W*Da+ab,0],e(g)));if(r!==1&&(r<1||e(f)[3]!==F()[f]))Pa(e(f),y([1,0,0,r,0,-r*ka+Ea],e(f)));T();h=xa(h);v[g]=h[g];v[f]=h[f];w=d;Na();N();ya()}Qa=null},1))}};this.setXRange=function(a,b,c){b=m()[0]+ m()[2]*b;c=m()[0]+m()[2]*c;if(n(m())>p(m())){if(b>n(m()))b=n(m());if(c<p(m()))c=p(m())}else{if(b<n(m()))b=n(m());if(c>p(m()))c=p(m())}a=Ga(a);a=xb(a,b,c,x(),i(),m(),F());b=a.xZoom;c=a.yZoom;a=a.panPoint;var h=aa(v);e(g)[0]=b;if(c)e(f)[3]=c;e(g)[4]=-a[g]*b;if(c)e(f)[5]=-a[f]*c;va();b=xa(h);v[g]=b[g];v[f]=b[f];T();Na();N();ya()};this.getSeries=function(a){return Ga(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))k[b]=a[b];sb();Na();N();ya()};this.updateConfig({}); if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ea.touchStart=P.start;ea.touchEnd=P.end;ea.touchMoved=P.moved}else{G=function(){};ea.touchStart=G;ea.touchEnd=G;ea.touchMoved=G}}");
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
