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
				"function(x){function E(a,b,c,d){function e(j){return c?b[j]:b[n-1-j]}function i(j){for(;e(j)[2]===v||e(j)[2]===y;)j--;return j}var k=l;if(d)k=m;var n=b.length;d=Math.floor(n/2);d=i(d);var q=0,s=n,f=false;if(e(0)[k]>a)return c?-1:n;if(e(n-1)[k]<a)return c?n:-1;for(;!f;){var h=d+1;if(h<n&&(e(h)[2]===v||e(h)[2]===y))h+=2;if(e(d)[k]>a){s=d;d=Math.floor((s+q)/2);d=i(d)}else if(e(d)[k]===a)f=true;else if(h<n&&e(h)[k]>a)f=true;else if(h<n&&e(h)[k]=== a){d=h;f=true}else{q=d;d=Math.floor((s+q)/2);d=i(d)}}return c?d:n-1-d}function G(a,b){return b[0][a]<b[b.length-1][a]}var v=2,y=3,l=0,m=1,F=this;x=x.WT.gfxUtils;var A=x.rect_top,B=x.rect_bottom,z=x.rect_left,C=x.rect_right,H=x.transform_mult;this.findClosestPoint=function(a,b,c){var d=l;if(c)d=m;var e=G(d,b);c=E(a,b,e,c);if(c<0)c=0;if(c>=b.length)return[b[b.length-1][l],b[b.length-1][m]];if(c>=b.length)c=b.length-2;if(b[c][d]===a)return[b[c][l],b[c][m]];var i=e?c+1:c-1;if(e&&b[i][2]==v)i+=2;if(!e&& i<0)return[b[c][l],b[c][m]];if(!e&&i>0&&b[i][2]==y)i-=2;e=Math.abs(a-b[c][d]);a=Math.abs(b[i][d]-a);return e<a?[b[c][l],b[c][m]]:[b[i][l],b[i][m]]};this.minMaxY=function(a,b){b=b?l:m;for(var c=a[0][b],d=a[0][b],e=1;e<a.length;++e)if(a[e][2]!==v&&a[e][2]!==y&&a[e][2]!==5){if(a[e][b]>d)d=a[e][b];if(a[e][b]<c)c=a[e][b]}return[c,d]};this.projection=function(a,b){var c=Math.cos(a);a=Math.sin(a);var d=c*a,e=-b[0]*c-b[1]*a;return[c*c,d,d,a*a,c*e+b[0],a*e+b[1]]};this.distanceSquared=function(a,b){a=[b[l]- a[l],b[m]-a[m]];return a[l]*a[l]+a[m]*a[m]};this.distanceLessThanRadius=function(a,b,c){return c*c>=F.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var c;if(a.x!==undefined){c=a.x;a=a.y}else{c=a[0];a=a[1]}return c>=z(b)&&c<=C(b)&&a>=A(b)&&a<=B(b)};this.toDisplayCoord=function(a,b,c,d,e){if(c){a=[(a[l]-e[0])/e[2],(a[m]-e[1])/e[3]];d=[d[0]+a[m]*d[2],d[1]+a[l]*d[3]]}else{a=[(a[l]-e[0])/e[2],1-(a[m]-e[1])/e[3]];d=[d[0]+ a[l]*d[2],d[1]+a[m]*d[3]]}return H(b,d)};this.findYRange=function(a,b,c,d,e,i,k,n){if(a.length!==0){var q=F.toDisplayCoord([c,0],[1,0,0,1,0,0],e,i,k),s=F.toDisplayCoord([d,0],[1,0,0,1,0,0],e,i,k),f=e?m:l,h=e?l:m,j=G(f,a),g=E(q[f],a,j,e),o=E(s[f],a,j,e),p,r,t=Infinity,u=-Infinity,D=g===o&&g===a.length||g===-1&&o===-1;if(!D){if(j)if(g<0)g=0;else{g++;if(a[g]&&a[g][2]===v)g+=2}else if(g>=a.length-1)g=a.length-2;if(!j&&o<0)o=0;for(p=Math.min(g,o);p<=Math.max(g,o)&&p<a.length;++p)if(a[p][2]!==v&&a[p][2]!== y){if(a[p][h]<t)t=a[p][h];if(a[p][h]>u)u=a[p][h]}if(j&&g>0||!j&&g<a.length-1){if(j){r=g-1;if(a[r]&&a[r][2]===y)r-=2}else{r=g+1;if(a[r]&&a[r][2]===v)r+=2}p=(q[f]-a[r][f])/(a[g][f]-a[r][f]);g=a[r][h]+p*(a[g][h]-a[r][h]);if(g<t)t=g;if(g>u)u=g}if(j&&o<a.length-1||!j&&o>0){if(j){j=o+1;if(a[j][2]===v)j+=2}else{j=o-1;if(a[j][2]===y)j-=2}p=(s[f]-a[o][f])/(a[j][f]-a[o][f]);g=a[o][h]+p*(a[j][h]-a[o][h]);if(g<t)t=g;if(g>u)u=g}}var w;a=k[2]/(d-c);c=e?2:3;if(!D){w=i[c]/(u-t);w=i[c]/(i[c]/w+20);if(w>n.y[b])w=n.y[b]}b= e?[q[m]-A(i),!D?(t+u)/2-i[2]/w/2-z(i):0]:[q[l]-z(i),!D?-((t+u)/2+i[3]/w/2-B(i)):0];return{xZoom:a,yZoom:w,panPoint:b}}};this.matchAxis=function(a,b,c,d,e){function i(){return d.length}function k(h){return d[h].side}function n(h){return d[h].width}function q(h){return d[h].minOffset}function s(h){return d[h].maxOffset}if(e){if(a<z(c)||a>C(c))return-1}else if(b<A(c)||b>B(c))return-1;for(var f=0;f<i();++f)if(e)if((k(f)===\"min\"||k(f)===\"both\")&&b>=A(c)-q(f)-n(f)&&b<=A(c)-q(f))return f;else{if((k(f)=== \"max\"||k(f)===\"both\")&&b>=B(c)+s(f)&&b<=B(c)+s(f)+n(f))return f}else if((k(f)===\"min\"||k(f)===\"both\")&&a>=z(c)-q(f)-n(f)&&a<=z(c)-q(f))return f;else if((k(f)===\"max\"||k(f)===\"both\")&&a>=C(c)+s(f)&&a<=C(c)+s(f)+n(f))return f;return-1}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(ta,I,x,k){function M(a){return a===undefined}function m(a){return k.modelAreas[a]}function S(){return k.followCurve}function ua(){return k.crosshair||S()!==-1}function A(){return k.isHorizontal}function n(){return k.xTransform}function h(a){return k.yTransforms[a]}function g(){return k.area}function l(){return k.insideArea}function Z(a){return M(a)?k.series:k.series[a]}function aa(a){return Z(a).transform}function hb(a){return A()? y([0,1,1,0,0,0],y(aa(a),[0,1,1,0,0,0])):aa(a)}function Na(a){return Z(a).curve}function N(a){return Z(a).axis}function ib(){return k.seriesSelection}function jb(){return k.sliders}function kb(){return k.hasToolTips}function lb(){return k.coordinateOverlayPadding}function Ea(){return k.curveManipulation}function va(){return k.maxZoom.x}function T(a){return k.maxZoom.y[a]}function K(){return k.pens}function mb(){return k.penAlpha}function ba(){return k.selectedCurve}function wa(a){a.preventDefault&& a.preventDefault()}function ca(a,b){I.addEventListener(a,b)}function U(a,b){I.removeEventListener(a,b)}function B(a){return a.length}function J(){return B(k.yTransforms)}function xb(){if(k.notifyTransform.x)return true;for(var a=0;a<J();++a)if(k.notifyTransform.y[a])return true;return false}function O(){return k.crosshairAxis}function Ya(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Oa(){if(o){if(o.tooltipTimeout){clearTimeout(o.tooltipTimeout); o.tooltipTimeout=null}if(!o.overTooltip)if(o.tooltipOuterDiv){document.body.removeChild(o.tooltipOuterDiv);o.tooltipEl=null;o.tooltipOuterDiv=null}}}function Fa(){if(xb()){if(Pa){window.clearTimeout(Pa);Pa=null}Pa=setTimeout(function(){if(k.notifyTransform.x&&!nb(Za,n())){ta.emit(x.widget,\"xTransformChanged\");ha(Za,n())}for(var a=0;a<J();++a)if(k.notifyTransform.y[a]&&!nb(Qa[a],h(a))){ta.emit(x.widget,\"yTransformChanged\"+a);ha(Qa[a],h(a))}},yb)}}function ia(a){var b,c;if(A()){b=p(g());c=t(g());return y([0, 1,1,0,b,c],y(n(),y(h(a),[0,1,1,0,-c,-b])))}else{b=p(g());c=w(g());return y([1,0,0,-1,b,c],y(n(),y(h(a),[1,0,0,-1,-b,c])))}}function F(a){return y(ia(a),l())}function ja(a,b,c){if(M(c))c=false;a=c?a:y(Ga(ia(b)),a);a=A()?[(a[u]-g()[1])/g()[3],(a[v]-g()[0])/g()[2]]:[(a[v]-g()[0])/g()[2],1-(a[u]-g()[1])/g()[3]];return[m(b)[0]+a[v]*m(b)[2],m(b)[1]+a[u]*m(b)[3]]}function Ra(a,b,c){if(M(c))c=false;return da.toDisplayCoord(a,c?[1,0,0,1,0,0]:ia(b),A(),g(),m(b))}function Ha(){var a,b;if(A()){a=(ja([0,t(g())], 0)[0]-m(0)[0])/m(0)[2];b=(ja([0,w(g())],0)[0]-m(0)[0])/m(0)[2]}else{a=(ja([p(g()),0],0)[0]-m(0)[0])/m(0)[2];b=(ja([q(g()),0],0)[0]-m(0)[0])/m(0)[2]}var c;for(c=0;c<B(jb());++c){var e=$(\"#\"+jb()[c]);if(e)(e=e.data(\"sobj\"))&&e.changeRange(a,b)}}function V(){Oa();if(kb()&&o.tooltipPosition)o.tooltipTimeout=setTimeout(function(){ob()},pb);ka&&qb(function(){x.repaint();ua()&&$a()})}function $a(){if(ka){var a=H.getContext(\"2d\");a.clearRect(0,0,H.width,H.height);a.save();a.beginPath();a.moveTo(p(g()),t(g())); a.lineTo(q(g()),t(g()));a.lineTo(q(g()),w(g()));a.lineTo(p(g()),w(g()));a.closePath();a.clip();var b=y(Ga(ia(O())),z),c=z[v],e=z[u];if(S()!==-1){b=zb(A()?b[u]:b[v],Na(S()),A());e=y(ia(N(S())),y(hb(S()),b));c=e[v];e=e[u];z[v]=c;z[u]=e}b=A()?[(b[u]-g()[1])/g()[3],(b[v]-g()[0])/g()[2]]:[(b[v]-g()[0])/g()[2],1-(b[u]-g()[1])/g()[3]];b=S()!==-1?[m(N(S()))[0]+b[v]*m(N(S()))[2],m(N(S()))[1]+b[u]*m(N(S()))[3]]:[m(O())[0]+b[v]*m(O())[2],m(O())[1]+b[u]*m(O())[3]];a.fillStyle=a.strokeStyle=k.crosshairColor;a.font= \"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var d=b[0].toFixed(2);b=b[1].toFixed(2);if(d===\"-0.00\")d=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+d+\",\"+b+\")\",q(g())-lb()[0],t(g())+lb()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(t(g()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(w(g()))+0.5);a.moveTo(Math.floor(p(g()))+0.5,Math.floor(e)+0.5);a.lineTo(Math.floor(q(g()))+0.5,Math.floor(e)+0.5);a.stroke();a.restore()}}function Ab(a){return t(a)<= t(l())+Sa&&w(a)>=w(l())-Sa&&p(a)<=p(l())+Sa&&q(a)>=q(l())-Sa}function ea(a){for(var b=0;b<J();++b){var c=F(b);if(A())if(a===xa)a=ya;else if(a===ya)a=xa;if(M(a)||a===xa)if(n()[0]<1){n()[0]=1;c=F(b)}if(M(a)||a===ya)if(h(b)[3]<1){h(b)[3]=1;c=F(b)}if(M(a)||a===xa){if(p(c)>p(l())){c=p(l())-p(c);if(A())h(b)[5]=h(b)[5]+c;else n()[4]=n()[4]+c;c=F(b)}if(q(c)<q(l())){c=q(l())-q(c);if(A())h(b)[5]=h(b)[5]+c;else n()[4]=n()[4]+c;c=F(b)}}if(M(a)||a===ya){if(t(c)>t(l())){c=t(l())-t(c);if(A())n()[4]=n()[4]+c;else h(b)[5]= h(b)[5]-c;c=F(b)}if(w(c)<w(l())){c=w(l())-w(c);if(A())n()[4]=n()[4]+c;else h(b)[5]=h(b)[5]-c;F(b)}}}Fa()}function ob(){ta.emit(x.widget,\"loadTooltip\",o.tooltipPosition[v],o.tooltipPosition[u])}function Bb(){if(ua()&&(M(H)||x.canvas.width!==H.width||x.canvas.height!==H.height)){if(H){H.parentNode.removeChild(H);jQuery.removeData(I,\"oobj\");H=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",x.canvas.width);a.setAttribute(\"height\",x.canvas.height);a.style.position=\"absolute\";a.style.display= \"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}x.canvas.parentNode.appendChild(a);H=a;jQuery.data(I,\"oobj\",H)}else if(!M(H)&&!ua()){H.parentNode.removeChild(H);jQuery.removeData(I,\"oobj\");H=undefined}z=[(p(g())+q(g()))/2,(t(g())+w(g()))/2]}function rb(){return H?H:x.canvas}function ab(a,b){if(za){var c=Date.now();if(M(b))b=c-la;var e={x:0,y:0},d;if(r===-1){d=F(0);for(var f=1;f<J();++f)d=Ta(d,F(f))}else d= F(r);f=Cb;if(b>2*Ia){ka=false;var j=Math.floor(b/Ia-1),C;for(C=0;C<j;++C){ab(a,Ia);if(!za){ka=true;V();return}}b-=j*Ia;ka=true}if(i.x===Infinity||i.x===-Infinity)i.x=i.x>0?ma:-ma;if(isFinite(i.x)){i.x/=1+sb*b;d[0]+=i.x*b;if(p(d)>p(l())){i.x+=-f*(p(d)-p(l()))*b;i.x*=0.7}else if(q(d)<q(l())){i.x+=-f*(q(d)-q(l()))*b;i.x*=0.7}if(Math.abs(i.x)<bb)if(p(d)>p(l()))i.x=bb;else if(q(d)<q(l()))i.x=-bb;if(Math.abs(i.x)>ma)i.x=(i.x>0?1:-1)*ma;e.x=i.x*b}if(i.y===Infinity||i.y===-Infinity)i.y=i.y>0?ma:-ma;if(isFinite(i.y)){i.y/= 1+sb*b;d[1]+=i.y*b;if(t(d)>t(l())){i.y+=-f*(t(d)-t(l()))*b;i.y*=0.7}else if(w(d)<w(l())){i.y+=-f*(w(d)-w(l()))*b;i.y*=0.7}if(Math.abs(i.y)<0.001)if(t(d)>t(l()))i.y=0.001;else if(w(d)<w(l()))i.y=-0.001;if(Math.abs(i.y)>ma)i.y=(i.y>0?1:-1)*ma;e.y=i.y*b}if(r===-1){d=F(0);for(f=1;f<J();++f)d=Ta(d,F(f))}else d=F(r);W(e,Aa,r);if(r===-1){a=F(0);for(f=1;f<J();++f)a=Ta(a,F(f))}else a=F(r);if(p(d)>p(l())&&p(a)<=p(l())){i.x=0;W({x:-e.x,y:0},Aa,r);ea(xa)}if(q(d)<q(l())&&q(a)>=q(l())){i.x=0;W({x:-e.x,y:0},Aa, r);ea(xa)}if(t(d)>t(l())&&t(a)<=t(l())){i.y=0;W({x:0,y:-e.y},Aa,r);ea(ya)}if(w(d)<w(l())&&w(a)>=w(l())){i.y=0;W({x:0,y:-e.y},Aa,r);ea(ya)}if(Math.abs(i.x)<tb&&Math.abs(i.y)<tb&&Ab(a)){ea();za=false;D=null;i.x=0;i.y=0;la=null;s=[]}else{la=c;ka&&Ua(ab)}}}function Va(){var a,b,c=ub(n()[0])-1;if(c>=B(K().x))c=B(K().x)-1;for(a=0;a<B(K().x);++a)if(c===a)for(b=0;b<B(K().x[a]);++b)K().x[a][b].color[3]=mb().x[b];else for(b=0;b<B(K().x[a]);++b)K().x[a][b].color[3]=0;for(c=0;c<B(K().y);++c){var e=ub(h(c)[3])- 1;if(e>=B(K().y[c]))e=B(K().y[c])-1;for(a=0;a<B(K().y[c]);++a)if(e===a)for(b=0;b<B(K().y[c][a]);++b)K().y[c][a][b].color[3]=mb().y[c][b];else for(b=0;b<B(K().y[c][a]);++b)K().y[c][a][b].color[3]=0}}function W(a,b,c){if(M(b))b=0;if(M(c))c=-1;var e=ja(z,O());if(A())a={x:a.y,y:-a.x};if(b&Aa){if(c===-1){n()[4]=n()[4]+a.x;for(b=0;b<J();++b)h(b)[5]=h(b)[5]-a.y}else h(c)[5]=h(c)[5]-a.y;Fa()}else if(b&vb){var d;if(c===-1){d=F(0);for(b=1;b<J();++b)d=Ta(d,F(b))}else d=F(c);if(p(d)>p(l())){if(a.x>0)a.x/=1+(p(d)- p(l()))*Wa}else if(q(d)<q(l()))if(a.x<0)a.x/=1+(q(l())-q(d))*Wa;if(t(d)>t(l())){if(a.y>0)a.y/=1+(t(d)-t(l()))*Wa}else if(w(d)<w(l()))if(a.y<0)a.y/=1+(w(l())-w(d))*Wa;if(c===-1){n()[4]=n()[4]+a.x;for(b=0;b<J();++b)h(b)[5]=h(b)[5]-a.y}else h(c)[5]=h(c)[5]-a.y;if(c===-1)z[v]+=a.x;z[u]+=a.y;Fa()}else{if(c===-1){n()[4]=n()[4]+a.x;for(b=0;b<J();++b)h(b)[5]=h(b)[5]-a.y}else h(c)[5]=h(c)[5]-a.y;if(c===-1)z[v]+=a.x;z[u]+=a.y;ea()}a=Ra(e,O());z[v]=a[v];z[u]=a[u];V();Ha()}function Ja(a,b,c,e){if(M(e))e=-1;var d= ja(z,O());a=A()?[a.y-t(g()),a.x-p(g())]:y(Ga([1,0,0,-1,p(g()),w(g())]),[a.x,a.y]);var f=a[0];a=a[1];var j=Math.pow(1.2,A()?c:b);b=Math.pow(1.2,A()?b:c);if(n()[0]*j>va())j=va()/n()[0];if(e===-1){if(j<1||n()[0]!==va())Ba(n(),y([j,0,0,1,f-j*f,0],n()));for(e=0;e<J();++e){c=b;if(h(e)[3]*b>T(e))c=T(e)/h(e)[3];if(c<1||h(e)[3]!==T(e))Ba(h(e),y([1,0,0,c,0,a-c*a],h(e)))}}else{if(h(e)[3]*b>T(e))b=T(e)/h(e)[3];if(b<1||h(e)[3]!=T(e))Ba(h(e),y([1,0,0,b,0,a-b*a],h(e)))}ea();d=Ra(d,O());z[v]=d[v];z[u]=d[u];Va(); V();Ha()}jQuery.data(I,\"cobj\",this);var na=this,E=ta.WT;na.config=k;var G=E.gfxUtils,y=G.transform_mult,Ga=G.transform_inverted,ha=G.transform_assign,nb=G.transform_equal,Db=G.transform_apply,t=G.rect_top,w=G.rect_bottom,p=G.rect_left,q=G.rect_right,Ta=G.rect_intersection,da=E.chartCommon,Eb=da.minMaxY,zb=da.findClosestPoint,Fb=da.projection,wb=da.distanceLessThanRadius,ub=da.toZoomLevel,Ka=da.isPointInRect,Gb=da.findYRange,La=function(a,b){return da.matchAxis(a,b,g(),k.yAxes,A())},Ia=17,Ua=function(){return window.requestAnimationFrame|| window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,Ia)}}(),cb=false,qb=function(a){if(!cb){cb=true;Ua(function(){a();cb=false})}};if(window.MSPointerEvent||window.PointerEvent){I.style.touchAction=\"none\";x.canvas.style.msTouchAction=\"none\";x.canvas.style.touchAction=\"none\"}var Aa=1,vb=2,xa=1,ya=2,v=0,u=1,yb=250,pb=500,sb=0.003,Cb=2.0E-4,Wa=0.07,Sa=3,bb=0.001,ma=1.5,tb=0.02,qa=jQuery.data(I,\"eobj2\");if(!qa){qa={};qa.contextmenuListener=function(a){wa(a); U(\"contextmenu\",qa.contextmenuListener)}}jQuery.data(I,\"eobj2\",qa);var X={},ra=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ra=B(d)>0}function b(j){if(Ya(j)){wa(j);d.push(j);a();X.start(I,{touches:d.slice(0)})}}function c(j){if(ra)if(Ya(j)){wa(j);var C;for(C=0;C<B(d);++C)if(d[C].pointerId===j.pointerId){d.splice(C,1);break}a();X.end(I,{touches:d.slice(0),changedTouches:[]})}}function e(j){if(Ya(j)){wa(j);var C;for(C=0;C<B(d);++C)if(d[C].pointerId===j.pointerId){d[C]= j;break}a();X.moved(I,{touches:d.slice(0)})}}var d=[],f=jQuery.data(I,\"eobj\");if(f)if(window.PointerEvent){U(\"pointerdown\",f.pointerDown);U(\"pointerup\",f.pointerUp);U(\"pointerout\",f.pointerUp);U(\"pointermove\",f.pointerMove)}else{U(\"MSPointerDown\",f.pointerDown);U(\"MSPointerUp\",f.pointerUp);U(\"MSPointerOut\",f.pointerUp);U(\"MSPointerMove\",f.pointerMove)}jQuery.data(I,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:e});if(window.PointerEvent){ca(\"pointerdown\",b);ca(\"pointerup\",c);ca(\"pointerout\",c);ca(\"pointermove\", e)}else{ca(\"MSPointerDown\",b);ca(\"MSPointerUp\",c);ca(\"MSPointerOut\",c);ca(\"MSPointerMove\",e)}})();var H=jQuery.data(I,\"oobj\"),z=null,ka=true,D=null,r=-1,s=[],fa=false,oa=false,Q=null,db=null,eb=null,i={x:0,y:0},Y=null,la=null,o=jQuery.data(I,\"tobj\");if(!o){o={overTooltip:false};jQuery.data(I,\"tobj\",o)}var Ca=null,za=false,Pa=null,Za=[0,0,0,0,0,0];ha(Za,n());var Qa=[];for(G=0;G<J();++G){Qa.push([0,0,0,0,0,0]);ha(Qa[G],h(G))}var Ba=function(a,b){ha(a,b);Fa()};x.combinedTransform=ia;this.updateTooltip= function(a){Oa();if(a)if(o.tooltipPosition){o.toolTipEl=document.createElement(\"div\");o.toolTipEl.className=k.ToolTipInnerStyle;o.toolTipEl.innerHTML=a;o.tooltipOuterDiv=document.createElement(\"div\");o.tooltipOuterDiv.className=k.ToolTipOuterStyle;document.body.appendChild(o.tooltipOuterDiv);o.tooltipOuterDiv.appendChild(o.toolTipEl);var b=E.widgetPageCoordinates(x.canvas);a=o.tooltipPosition[v]+b.x;b=o.tooltipPosition[u]+b.y;E.fitToWindow(o.tooltipOuterDiv,a+10,b+10,a-10,b-10);$(o.toolTipEl).mouseenter(function(){o.overTooltip= true});$(o.toolTipEl).mouseleave(function(){o.overTooltip=false})}};this.mouseMove=function(a,b){setTimeout(function(){setTimeout(Oa,200);if(!ra){var c=E.widgetCoordinates(x.canvas,b);if(Ka(c,g())){if(!o.tooltipEl&&kb()){o.tooltipPosition=[c.x,c.y];o.tooltipTimeout=setTimeout(function(){ob()},pb)}if(D===null&&ua()&&ka){z=[c.x,c.y];qb($a)}}}},0)};this.mouseOut=function(){setTimeout(Oa,200)};this.mouseDown=function(a,b){if(!ra){a=E.widgetCoordinates(x.canvas,b);b=La(a.x,a.y);var c=Ka(a,g());if(!(b=== -1&&!c)){D=a;r=b}}};this.mouseUp=function(){if(!ra){D=null;r=-1}};this.mouseDrag=function(a,b){if(!ra)if(D!==null){a=E.widgetCoordinates(x.canvas,b);if(E.buttons===1)if(r===-1&&Ea()&&Z(ba())){b=ba();var c;c=A()?a.x-D.x:a.y-D.y;ha(aa(b),y([1,0,0,1,0,c/h(N(seriesNb))[3]],aa(b)));V()}else k.pan&&W({x:a.x-D.x,y:a.y-D.y},0,r);D=a}};this.clicked=function(a,b){if(!ra)if(D===null)if(ib()){a=E.widgetCoordinates(x.canvas,b);ta.emit(x.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){var c=(b.metaKey<< 3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;a=k.wheelActions[c];if(!M(a)){var e=E.widgetCoordinates(x.canvas,b),d=La(e.x,e.y),f=Ka(e,g());if(!(d===-1&&!f)){var j=E.normalizeWheel(b);if(f&&c===0&&Ea()){c=ba();f=-j.spinY;if(Z(c)){a=hb(c);a=Db(a,Na(c));a=Eb(a,A());a=(a[0]+a[1])/2;E.cancelEvent(b);b=Math.pow(1.2,f);ha(aa(c),y([1,0,0,b,0,a-b*a],aa(c)));V();return}}if((a===4||a===5||a===6)&&k.pan){c=n()[4];f=[];for(e=0;e<J();++e)f.push(h(e)[5]);if(a===6)W({x:-j.pixelX,y:-j.pixelY},0,d);else if(a===5)W({x:0, y:-j.pixelX-j.pixelY},0,d);else a===4&&W({x:-j.pixelX-j.pixelY,y:0},0,d);if(c!==n()[4])for(e=0;e<J();++e)f[e]!==h(e)[5]&&E.cancelEvent(b)}else if(k.zoom){E.cancelEvent(b);f=-j.spinY;if(f===0)f=-j.spinX;if(a===1)Ja(e,0,f,d);else if(a===0)Ja(e,f,0,d);else if(a===2)Ja(e,f,f,d);else if(a===3)j.pixelX!==0?Ja(e,f,0,d):Ja(e,0,f,d)}}}};var Hb=function(){ib()&&ta.emit(x.widget,\"seriesSelected\",D.x,D.y)};X.start=function(a,b,c){fa=B(b.touches)===1;oa=B(b.touches)===2;if(fa){za=false;var e=E.widgetCoordinates(x.canvas, b.touches[0]);a=La(e.x,e.y);var d=Ka(e,g());if(a===-1&&!d)return;Ca=a===-1&&ua()&&wb(z,[e.x,e.y],30)?1:0;la=Date.now();D=e;r=a;if(Ca!==1){if(!c&&d)Y=window.setTimeout(Hb,200);ca(\"contextmenu\",qa.contextmenuListener)}E.capture(null);E.capture(rb())}else if(oa&&(k.zoom||Ea())){if(Y){window.clearTimeout(Y);Y=null}za=false;s=[E.widgetCoordinates(x.canvas,b.touches[0]),E.widgetCoordinates(x.canvas,b.touches[1])].map(function(f){return[f.x,f.y]});a=-1;if(!s.every(function(f){return Ka(f,g())})){a=La(s[0][v], s[0][u]);if(a===-1||La(s[1][v],s[1][u])!==a){oa=null;return}r=a}E.capture(null);E.capture(rb());Q=Math.atan2(s[1][1]-s[0][1],s[1][0]-s[0][0]);db=[(s[0][0]+s[1][0])/2,(s[0][1]+s[1][1])/2];c=Math.abs(Math.sin(Q));e=Math.abs(Math.cos(Q));Q=c<Math.sin(0.125*Math.PI)?0:e<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(Q)>0?Math.PI/4:-Math.PI/4;eb=Fb(Q,db);r=a}else return;wa(b)};X.end=function(a,b){if(Y){window.clearTimeout(Y);Y=null}window.setTimeout(function(){U(\"contextmenu\",qa.contextmenuListener)},0);var c= Array.prototype.slice.call(b.touches),e=B(c)===0;e||function(){var d;for(d=0;d<B(b.changedTouches);++d)(function(){for(var f=b.changedTouches[d].identifier,j=0;j<B(c);++j)if(c[j].identifier===f){c.splice(j,1);return}})()}();e=B(c)===0;fa=B(c)===1;oa=B(c)===2;if(e){Xa=null;if(Ca===0&&(isFinite(i.x)||isFinite(i.y))&&k.rubberBand){la=Date.now();za=true;Ua(ab)}else{Ca===1&&na.mouseUp(null,null);c=[];la=eb=db=Q=null}Ca=null}else if(fa||oa)X.start(a,b,true)};var Xa=null,sa=null,fb=null;X.moved=function(a, b){if(fa||oa)if(!(fa&&D==null)){wa(b);sa=E.widgetCoordinates(x.canvas,b.touches[0]);if(B(b.touches)>1)fb=E.widgetCoordinates(x.canvas,b.touches[1]);if(r===-1&&fa&&Y&&!wb([sa.x,sa.y],[D.x,D.y],3)){window.clearTimeout(Y);Y=null}Xa||(Xa=setTimeout(function(){if(r===-1&&fa&&Ea()&&Z(ba())){var c=ba();if(Z(c)){var e=sa,d;d=A()?(e.x-D.x)/h(N(ba()))[3]:(e.y-D.y)/h(N(ba()))[3];aa(c)[5]+=d;D=e;V()}}else if(fa){e=sa;d=Date.now();var f={x:e.x-D.x,y:e.y-D.y};c=d-la;la=d;if(Ca===1){z[v]+=f.x;z[u]+=f.y;ua()&&ka&& Ua($a)}else if(k.pan){i.x=f.x/c;i.y=f.y/c;W(f,k.rubberBand?vb:0,r)}D=e}else if(r===-1&&oa&&Ea()&&Z(ba())){f=A()?v:u;d=[sa,fb].map(function(P){return A()?[P.x,pa]:[Ma,P.y]});c=Math.abs(s[1][f]-s[0][f]);var j=Math.abs(d[1][f]-d[0][f]),C=c>0?j/c:1;if(j===c)C=1;c=ba();if(Z(c)){var pa=y(Ga(ia(N(c))),[0,(s[0][f]+s[1][f])/2])[1],Da=y(Ga(ia(N(c))),[0,(d[0][f]+d[1][f])/2])[1];ha(aa(c),y([1,0,0,C,0,-C*pa+Da],aa(c)));D=e;V();s=d}}else if(oa&&k.zoom){e=ja(z,O());var Ma=(s[0][0]+s[1][0])/2;pa=(s[0][1]+s[1][1])/ 2;d=[sa,fb].map(function(P){return Q===0?[P.x,pa]:Q===Math.PI/2?[Ma,P.y]:y(eb,[P.x,P.y])});f=Math.abs(s[1][0]-s[0][0]);c=Math.abs(d[1][0]-d[0][0]);var ga=f>0?c/f:1;if(c===f||Q===Math.PI/2)ga=1;var gb=(d[0][0]+d[1][0])/2;c=Math.abs(s[1][1]-s[0][1]);j=Math.abs(d[1][1]-d[0][1]);C=c>0?j/c:1;if(j===c||Q===0)C=1;Da=(d[0][1]+d[1][1])/2;A()&&function(){var P=ga;ga=C;C=P;P=gb;gb=Da;Da=P;P=Ma;Ma=pa;pa=P}();if(n()[0]*ga>va())ga=va()/n()[0];f=[];for(c=0;c<J();++c)f.push(C);for(c=0;c<J();++c)if(h(c)[3]*f[c]>T(c))f[c]= T(c)/h(c)[3];if(r===-1){if(ga!==1&&(ga<1||n()[0]!==va()))Ba(n(),y([ga,0,0,1,-ga*Ma+gb,0],n()));for(c=0;c<J();++c)if(f[c]!==1&&(f[c]<1||h(c)[3]!==T(c)))Ba(h(c),y([1,0,0,f[c],0,-f[c]*pa+Da],h(c)))}else if(f[r]!==1&&(f[r]<1||h(r)[3]!==T(r)))Ba(h(r),y([1,0,0,f[r],0,-f[r]*pa+Da],h(r)));ea();e=Ra(e,O());z[v]=e[v];z[u]=e[u];s=d;Va();V();Ha()}Xa=null},1))}};this.setXRange=function(a,b,c,e){b=m(0)[0]+m(0)[2]*b;c=m(0)[0]+m(0)[2]*c;if(p(m(0))>q(m(0))){if(b>p(m(0)))b=p(m(0));if(c<q(m(0)))c=q(m(0))}else{if(b< p(m(0)))b=p(m(0));if(c>q(m(0)))c=q(m(0))}var d=Na(a);d=Gb(d,N(a),b,c,A(),g(),m(N(a)),k.maxZoom);b=d.xZoom;c=d.yZoom;d=d.panPoint;var f=ja(z,O());n()[0]=b;if(c&&e)h(N(a))[3]=c;n()[4]=-d[v]*b;if(c&&e)h(N(a))[5]=-d[u]*c;Fa();a=Ra(f,O());z[v]=a[v];z[u]=a[u];ea();Va();V();Ha()};this.getSeries=function(a){return Na(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))k[b]=a[b];Bb();Va();V();Ha()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&& !window.PointerEvent){na.touchStart=X.start;na.touchEnd=X.end;na.touchMoved=X.moved}else{G=function(){};na.touchStart=G;na.touchEnd=G;na.touchMoved=G}}");
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;
	private static final int CURVE_SELECTION_DISTANCE_SQUARED = 400;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
