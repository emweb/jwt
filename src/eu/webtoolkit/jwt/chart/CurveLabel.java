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
				"function(B){function F(a,b,c,d){function e(m){return c?b[m]:b[n-1-m]}function i(m){for(;e(m)[2]===y||e(m)[2]===C;)m--;return m}var j=k;if(d)j=l;var n=b.length;d=Math.floor(n/2);d=i(d);var t=0,r=n,f=false;if(e(0)[j]>a)return c?-1:n;if(e(n-1)[j]<a)return c?n:-1;for(;!f;){var g=d+1;if(g<n&&(e(g)[2]===y||e(g)[2]===C))g+=2;if(e(d)[j]>a){r=d;d=Math.floor((r+t)/2);d=i(d)}else if(e(d)[j]===a)f=true;else if(g<n&&e(g)[j]>a)f=true;else if(g<n&&e(g)[j]=== a){d=g;f=true}else{t=d;d=Math.floor((r+t)/2);d=i(d)}}return c?d:n-1-d}function H(a,b){return b[0][a]<b[b.length-1][a]}var y=2,C=3,k=0,l=1,G=this;B=B.WT.gfxUtils;var z=B.rect_top,A=B.rect_bottom,v=B.rect_left,D=B.rect_right,I=B.transform_mult;this.findClosestPoint=function(a,b,c){var d=k;if(c)d=l;var e=H(d,b);c=F(a,b,e,c);if(c<0)c=0;if(c>=b.length)return[b[b.length-1][k],b[b.length-1][l]];if(c>=b.length)c=b.length-2;if(b[c][d]===a)return[b[c][k],b[c][l]];var i=e?c+1:c-1;if(e&&b[i][2]==y)i+=2;if(!e&& i<0)return[b[c][k],b[c][l]];if(!e&&i>0&&b[i][2]==C)i-=2;e=Math.abs(a-b[c][d]);a=Math.abs(b[i][d]-a);return e<a?[b[c][k],b[c][l]]:[b[i][k],b[i][l]]};this.minMaxY=function(a,b){b=b?k:l;for(var c=a[0][b],d=a[0][b],e=1;e<a.length;++e)if(a[e][2]!==y&&a[e][2]!==C&&a[e][2]!==5){if(a[e][b]>d)d=a[e][b];if(a[e][b]<c)c=a[e][b]}return[c,d]};this.projection=function(a,b){var c=Math.cos(a);a=Math.sin(a);var d=c*a,e=-b[0]*c-b[1]*a;return[c*c,d,d,a*a,c*e+b[0],a*e+b[1]]};this.distanceSquared=function(a,b){a=[b[k]- a[k],b[l]-a[l]];return a[k]*a[k]+a[l]*a[l]};this.distanceLessThanRadius=function(a,b,c){return c*c>=G.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var c;if(a.x!==undefined){c=a.x;a=a.y}else{c=a[0];a=a[1]}return c>=v(b)&&c<=D(b)&&a>=z(b)&&a<=A(b)};this.toDisplayCoord=function(a,b,c,d,e){if(c){a=[(a[k]-e[0])/e[2],(a[l]-e[1])/e[3]];d=[d[0]+a[l]*d[2],d[1]+a[k]*d[3]]}else{a=[(a[k]-e[0])/e[2],1-(a[l]-e[1])/e[3]];d=[d[0]+ a[k]*d[2],d[1]+a[l]*d[3]]}return I(b,d)};this.findYRange=function(a,b,c,d,e,i,j,n,t){if(a.length!==0){var r=G.toDisplayCoord([c,0],[1,0,0,1,0,0],e,i,j),f=G.toDisplayCoord([d,0],[1,0,0,1,0,0],e,i,j),g=e?l:k,m=e?k:l,o=H(g,a),h=F(r[g],a,o,e),p=F(f[g],a,o,e),q,s,w=Infinity,x=-Infinity,E=h===p&&h===a.length||h===-1&&p===-1;if(!E){if(o)if(h<0)h=0;else{h++;if(a[h]&&a[h][2]===y)h+=2}else if(h>=a.length-1)h=a.length-2;if(!o&&p<0)p=0;for(q=Math.min(h,p);q<=Math.max(h,p)&&q<a.length;++q)if(a[q][2]!==y&&a[q][2]!== C){if(a[q][m]<w)w=a[q][m];if(a[q][m]>x)x=a[q][m]}if(o&&h>0||!o&&h<a.length-1){if(o){s=h-1;if(a[s]&&a[s][2]===C)s-=2}else{s=h+1;if(a[s]&&a[s][2]===y)s+=2}q=(r[g]-a[s][g])/(a[h][g]-a[s][g]);h=a[s][m]+q*(a[h][m]-a[s][m]);if(h<w)w=h;if(h>x)x=h}if(o&&p<a.length-1||!o&&p>0){if(o){o=p+1;if(a[o][2]===y)o+=2}else{o=p-1;if(a[o][2]===C)o-=2}q=(f[g]-a[p][g])/(a[o][g]-a[p][g]);h=a[p][m]+q*(a[o][m]-a[p][m]);if(h<w)w=h;if(h>x)x=h}}var u;a=j[2]/(d-c);c=e?2:3;if(!E){u=i[c]/(x-w);u=i[c]/(i[c]/u+20);if(u>t.y[b])u=t.y[b]; if(u<n.y[b])u=n.y[b]}b=e?[r[l]-z(i),!E?(w+x)/2-i[2]/u/2-v(i):0]:[r[k]-v(i),!E?-((w+x)/2+i[3]/u/2-A(i)):0];return{xZoom:a,yZoom:u,panPoint:b}}};this.matchesXAxis=function(a,b,c,d,e){if(e){if(b<z(c)||b>A(c))return false;if((d.side===\"min\"||d.side===\"both\")&&a>=v(c)-d.width&&a<=v(c))return true;if((d.side===\"max\"||d.side===\"both\")&&a<=D(c)+d.width&&a>=D(c))return true}else{if(a<v(c)||a>D(c))return false;if((d.side===\"min\"||d.side===\"both\")&&b<=A(c)+d.width&&b>=A(c))return true;if((d.side===\"max\"||d.side=== \"both\")&&b>=z(c)-d.width&&b<=z(c))return true}return false};this.matchYAxis=function(a,b,c,d,e){function i(){return d.length}function j(g){return d[g].side}function n(g){return d[g].width}function t(g){return d[g].minOffset}function r(g){return d[g].maxOffset}if(e){if(a<v(c)||a>D(c))return-1}else if(b<z(c)||b>A(c))return-1;for(var f=0;f<i();++f)if(e)if((j(f)===\"min\"||j(f)===\"both\")&&b>=z(c)-t(f)-n(f)&&b<=z(c)-t(f))return f;else{if((j(f)===\"max\"||j(f)===\"both\")&&b>=A(c)+r(f)&&b<=A(c)+r(f)+n(f))return f}else if((j(f)=== \"min\"||j(f)===\"both\")&&a>=v(c)-t(f)-n(f)&&a<=v(c)-t(f))return f;else if((j(f)===\"max\"||j(f)===\"both\")&&a>=D(c)+r(f)&&a<=D(c)+r(f)+n(f))return f;return-1}}");
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WCartesianChart",
				"function(wa,J,A,l){function N(a){return a===undefined}function o(a){return l.modelAreas[a]}function V(){return l.followCurve}function xa(){return l.crosshair||V()!==-1}function B(){return l.isHorizontal}function i(){return l.xTransform}function g(a){return l.yTransforms[a]}function h(){return l.area}function n(){return l.insideArea}function ca(a){return N(a)?l.series:l.series[a]}function da(a){return ca(a).transform}function kb(a){return B()? w([0,1,1,0,0,0],w(da(a),[0,1,1,0,0,0])):da(a)}function Pa(a){return ca(a).curve}function P(a){return ca(a).axis}function lb(){return l.seriesSelection}function mb(){return l.sliders}function nb(){return l.hasToolTips}function ob(){return l.coordinateOverlayPadding}function Ga(){return l.curveManipulation}function Qa(){return l.minZoom.x}function pb(a){return l.minZoom.y[a]}function ea(){return l.maxZoom.x}function T(a){return l.maxZoom.y[a]}function K(){return l.pens}function qb(){return l.penAlpha} function fa(){return l.selectedCurve}function ya(a){a.preventDefault&&a.preventDefault()}function ga(a,b){J.addEventListener(a,b)}function W(a,b){J.removeEventListener(a,b)}function y(a){return a.length}function M(){return y(l.yTransforms)}function Bb(){if(l.notifyTransform.x)return true;for(var a=0;a<M();++a)if(l.notifyTransform.y[a])return true;return false}function Q(){return l.crosshairAxis}function bb(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"} function Ra(){if(p){if(p.tooltipTimeout){clearTimeout(p.tooltipTimeout);p.tooltipTimeout=null}if(!p.overTooltip)if(p.tooltipOuterDiv){document.body.removeChild(p.tooltipOuterDiv);p.tooltipEl=null;p.tooltipOuterDiv=null}}}function Ha(){if(Bb()){if(Sa){window.clearTimeout(Sa);Sa=null}Sa=setTimeout(function(){if(l.notifyTransform.x&&!rb(cb,i())){wa.emit(A.widget,\"xTransformChanged\");ka(cb,i())}for(var a=0;a<M();++a)if(l.notifyTransform.y[a]&&!rb(Ta[a],g(a))){wa.emit(A.widget,\"yTransformChanged\"+a);ka(Ta[a], g(a))}},Cb)}}function la(a){var b,c;if(B()){b=q(h());c=x(h());return w([0,1,1,0,b,c],w(i(),w(g(a),[0,1,1,0,-c,-b])))}else{b=q(h());c=z(h());return w([1,0,0,-1,b,c],w(i(),w(g(a),[1,0,0,-1,-b,c])))}}function F(a){return w(la(a),n())}function ma(a,b,c){if(N(c))c=false;a=c?a:w(Ia(la(b)),a);a=B()?[(a[u]-h()[1])/h()[3],(a[v]-h()[0])/h()[2]]:[(a[v]-h()[0])/h()[2],1-(a[u]-h()[1])/h()[3]];return[o(b)[0]+a[v]*o(b)[2],o(b)[1]+a[u]*o(b)[3]]}function Ua(a,b,c){if(N(c))c=false;return X.toDisplayCoord(a,c?[1,0, 0,1,0,0]:la(b),B(),h(),o(b))}function Ja(){var a,b;if(B()){a=(ma([0,x(h())],0)[0]-o(0)[0])/o(0)[2];b=(ma([0,z(h())],0)[0]-o(0)[0])/o(0)[2]}else{a=(ma([q(h()),0],0)[0]-o(0)[0])/o(0)[2];b=(ma([s(h()),0],0)[0]-o(0)[0])/o(0)[2]}var c;for(c=0;c<y(mb());++c){var d=$(\"#\"+mb()[c]);if(d)(d=d.data(\"sobj\"))&&d.changeRange(a,b)}}function Y(){Ra();if(nb()&&p.tooltipPosition)p.tooltipTimeout=setTimeout(function(){sb()},tb);na&&ub(function(){A.repaint();xa()&&db()})}function db(){if(na){var a=I.getContext(\"2d\"); a.clearRect(0,0,I.width,I.height);a.save();a.beginPath();a.moveTo(q(h()),x(h()));a.lineTo(s(h()),x(h()));a.lineTo(s(h()),z(h()));a.lineTo(q(h()),z(h()));a.closePath();a.clip();var b=w(Ia(la(Q())),C),c=C[v],d=C[u];if(V()!==-1){b=Db(B()?b[u]:b[v],Pa(V()),B());d=w(la(P(V())),w(kb(V()),b));c=d[v];d=d[u];C[v]=c;C[u]=d}b=B()?[(b[u]-h()[1])/h()[3],(b[v]-h()[0])/h()[2]]:[(b[v]-h()[0])/h()[2],1-(b[u]-h()[1])/h()[3]];b=V()!==-1?[o(P(V()))[0]+b[v]*o(P(V()))[2],o(P(V()))[1]+b[u]*o(P(V()))[3]]:[o(Q())[0]+b[v]* o(Q())[2],o(Q())[1]+b[u]*o(Q())[3]];a.fillStyle=a.strokeStyle=l.crosshairColor;a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";var e=b[0].toFixed(2);b=b[1].toFixed(2);if(e===\"-0.00\")e=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+e+\",\"+b+\")\",s(h())-ob()[0],x(h())+ob()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(x(h()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(z(h()))+0.5);a.moveTo(Math.floor(q(h()))+0.5,Math.floor(d)+0.5);a.lineTo(Math.floor(s(h()))+ 0.5,Math.floor(d)+0.5);a.stroke();a.restore()}}function Eb(a){return x(a)<=x(n())+Va&&z(a)>=z(n())-Va&&q(a)<=q(n())+Va&&s(a)>=s(n())-Va}function ha(a){for(var b=0;b<M();++b){var c=F(b);if(B())if(a===za)a=Aa;else if(a===Aa)a=za;if(N(a)||a===za)if(i()[0]<1){i()[0]=1;c=F(b)}if(N(a)||a===Aa)if(g(b)[3]<1){g(b)[3]=1;c=F(b)}if(N(a)||a===za){if(q(c)>q(n())){c=q(n())-q(c);if(B())g(b)[5]=g(b)[5]+c;else i()[4]=i()[4]+c;c=F(b)}if(s(c)<s(n())){c=s(n())-s(c);if(B())g(b)[5]=g(b)[5]+c;else i()[4]=i()[4]+c;c=F(b)}}if(N(a)|| a===Aa){if(x(c)>x(n())){c=x(n())-x(c);if(B())i()[4]=i()[4]+c;else g(b)[5]=g(b)[5]-c;c=F(b)}if(z(c)<z(n())){c=z(n())-z(c);if(B())i()[4]=i()[4]+c;else g(b)[5]=g(b)[5]-c;F(b)}}}Ha()}function sb(){wa.emit(A.widget,\"loadTooltip\",p.tooltipPosition[v],p.tooltipPosition[u])}function Fb(){if(xa()&&(N(I)||A.canvas.width!==I.width||A.canvas.height!==I.height)){if(I){I.parentNode.removeChild(I);jQuery.removeData(J,\"oobj\");I=undefined}var a=document.createElement(\"canvas\");a.setAttribute(\"width\",A.canvas.width); a.setAttribute(\"height\",A.canvas.height);a.style.position=\"absolute\";a.style.display=\"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}A.canvas.parentNode.appendChild(a);I=a;jQuery.data(J,\"oobj\",I)}else if(!N(I)&&!xa()){I.parentNode.removeChild(I);jQuery.removeData(J,\"oobj\");I=undefined}C=[(q(h())+s(h()))/2,(x(h())+z(h()))/2]}function vb(){return I?I:A.canvas}function eb(a,b){if(Ba){var c=Date.now();if(N(b))b= c-oa;var d={x:0,y:0},e;if(G)e=F(0);else if(t===-1){e=F(0);for(var f=1;f<M();++f)e=Wa(e,F(f))}else e=F(t);f=Gb;if(b>2*Ka){na=false;var j=Math.floor(b/Ka-1),m;for(m=0;m<j;++m){eb(a,Ka);if(!Ba){na=true;Y();return}}b-=j*Ka;na=true}if(k.x===Infinity||k.x===-Infinity)k.x=k.x>0?pa:-pa;if(isFinite(k.x)){k.x/=1+wb*b;e[0]+=k.x*b;if(q(e)>q(n())){k.x+=-f*(q(e)-q(n()))*b;k.x*=0.7}else if(s(e)<s(n())){k.x+=-f*(s(e)-s(n()))*b;k.x*=0.7}if(Math.abs(k.x)<fb)if(q(e)>q(n()))k.x=fb;else if(s(e)<s(n()))k.x=-fb;if(Math.abs(k.x)> pa)k.x=(k.x>0?1:-1)*pa;d.x=k.x*b}if(k.y===Infinity||k.y===-Infinity)k.y=k.y>0?pa:-pa;if(isFinite(k.y)){k.y/=1+wb*b;e[1]+=k.y*b;if(x(e)>x(n())){k.y+=-f*(x(e)-x(n()))*b;k.y*=0.7}else if(z(e)<z(n())){k.y+=-f*(z(e)-z(n()))*b;k.y*=0.7}if(Math.abs(k.y)<0.001)if(x(e)>x(n()))k.y=0.001;else if(z(e)<z(n()))k.y=-0.001;if(Math.abs(k.y)>pa)k.y=(k.y>0?1:-1)*pa;d.y=k.y*b}if(G)e=F(0);else if(t===-1){e=F(0);for(f=1;f<M();++f)e=Wa(e,F(f))}else e=F(t);Z(d,Ca,t,G);if(G)a=F(0);else if(t===-1){a=F(0);for(f=1;f<M();++f)a= Wa(a,F(f))}else a=F(t);if(q(e)>q(n())&&q(a)<=q(n())){k.x=0;Z({x:-d.x,y:0},Ca,t,G);ha(za)}if(s(e)<s(n())&&s(a)>=s(n())){k.x=0;Z({x:-d.x,y:0},Ca,t,G);ha(za)}if(x(e)>x(n())&&x(a)<=x(n())){k.y=0;Z({x:0,y:-d.y},Ca,t,G);ha(Aa)}if(z(e)<z(n())&&z(a)>=z(n())){k.y=0;Z({x:0,y:-d.y},Ca,t,G);ha(Aa)}if(Math.abs(k.x)<xb&&Math.abs(k.y)<xb&&Eb(a)){ha();Ba=false;D=null;k.x=0;k.y=0;oa=null;r=[]}else{oa=c;na&&Xa(eb)}}}function Ya(){var a,b,c=yb(i()[0])-1;if(i()[0]==ea())c=y(K().x)-1;if(c>=y(K().x))c=y(K().x)-1;for(a= 0;a<y(K().x);++a)if(c===a)for(b=0;b<y(K().x[a]);++b)K().x[a][b].color[3]=qb().x[b];else for(b=0;b<y(K().x[a]);++b)K().x[a][b].color[3]=0;for(c=0;c<y(K().y);++c){var d=yb(g(c)[3])-1;if(g(c)[3]==T(c))d=y(K().y[c])-1;if(d>=y(K().y[c]))d=y(K().y[c])-1;for(a=0;a<y(K().y[c]);++a)if(d===a)for(b=0;b<y(K().y[c][a]);++b)K().y[c][a][b].color[3]=qb().y[c][b];else for(b=0;b<y(K().y[c][a]);++b)K().y[c][a][b].color[3]=0}}function Z(a,b,c,d){if(N(b))b=0;if(N(c))c=-1;if(N(d))d=false;var e=ma(C,Q());if(B())a={x:a.y, y:-a.x};if(b&Ca){if(d)i()[4]=i()[4]+a.x;else if(c===-1){i()[4]=i()[4]+a.x;for(b=0;b<M();++b)g(b)[5]=g(b)[5]-a.y}else g(c)[5]=g(c)[5]-a.y;Ha()}else if(b&zb){var f;if(d)f=F(0);else if(c===-1){f=F(0);for(b=1;b<M();++b)f=Wa(f,F(b))}else f=F(c);if(q(f)>q(n())){if(a.x>0)a.x/=1+(q(f)-q(n()))*Za}else if(s(f)<s(n()))if(a.x<0)a.x/=1+(s(n())-s(f))*Za;if(x(f)>x(n())){if(a.y>0)a.y/=1+(x(f)-x(n()))*Za}else if(z(f)<z(n()))if(a.y<0)a.y/=1+(z(n())-z(f))*Za;if(d)i()[4]=i()[4]+a.x;else if(c===-1){i()[4]=i()[4]+a.x; for(b=0;b<M();++b)g(b)[5]=g(b)[5]-a.y}else g(c)[5]=g(c)[5]-a.y;if(c===-1)C[v]+=a.x;d||(C[u]+=a.y);Ha()}else{if(d)i()[4]=i()[4]+a.x;else if(c===-1){i()[4]=i()[4]+a.x;for(b=0;b<M();++b)g(b)[5]=g(b)[5]-a.y}else g(c)[5]=g(c)[5]-a.y;if(c===-1)C[v]+=a.x;d||(C[u]+=a.y);ha()}a=Ua(e,Q());C[v]=a[v];C[u]=a[u];Y();Ja()}function La(a,b,c,d,e){if(N(d))d=-1;if(N(e))e=false;var f=ma(C,Q());a=B()?[a.y-x(h()),a.x-q(h())]:w(Ia([1,0,0,-1,q(h()),z(h())]),[a.x,a.y]);var j=a[0];a=a[1];var m=Math.pow(1.2,B()?c:b);b=Math.pow(1.2, B()?b:c);if(i()[0]*m>ea())m=ea()/i()[0];if(i()[0]*m<Qa())m=Qa()/i()[0];if(e){if(m<1||i()[0]!==ea())qa(i(),w([m,0,0,1,j-m*j,0],i()))}else if(d===-1){if(m<1||i()[0]!==ea())qa(i(),w([m,0,0,1,j-m*j,0],i()));for(d=0;d<M();++d){e=b;if(g(d)[3]*b>T(d))e=T(d)/g(d)[3];if(e<1||g(d)[3]!==T(d))qa(g(d),w([1,0,0,e,0,a-e*a],g(d)))}}else{if(g(d)[3]*b>T(d))b=T(d)/g(d)[3];if(b<1||g(d)[3]!=T(d))qa(g(d),w([1,0,0,b,0,a-b*a],g(d)))}ha();f=Ua(f,Q());C[v]=f[v];C[u]=f[u];Ya();Y();Ja()}jQuery.data(J,\"cobj\",this);var ia=this, E=wa.WT;ia.config=l;var H=E.gfxUtils,w=H.transform_mult,Ia=H.transform_inverted,ka=H.transform_assign,rb=H.transform_equal,Hb=H.transform_apply,x=H.rect_top,z=H.rect_bottom,q=H.rect_left,s=H.rect_right,Wa=H.rect_intersection,X=E.chartCommon,Ib=X.minMaxY,Db=X.findClosestPoint,Jb=X.projection,Ab=X.distanceLessThanRadius,yb=X.toZoomLevel,Ma=X.isPointInRect,Kb=X.findYRange,Na=function(a,b){return X.matchesXAxis(a,b,h(),l.xAxis,B())},Oa=function(a,b){return X.matchYAxis(a,b,h(),l.yAxes,B())},Ka=17,Xa= function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(a){window.setTimeout(a,Ka)}}(),gb=false,ub=function(a){if(!gb){gb=true;Xa(function(){a();gb=false})}};if(window.MSPointerEvent||window.PointerEvent){J.style.touchAction=\"none\";A.canvas.style.msTouchAction=\"none\";A.canvas.style.touchAction=\"none\"}var Ca=1,zb=2,za=1,Aa=2,v=0,u=1,Cb=250,tb=500,wb=0.003,Gb=2.0E-4,Za=0.07,Va=3,fb=0.001,pa=1.5,xb=0.02,ta=jQuery.data(J,\"eobj2\");if(!ta){ta= {};ta.contextmenuListener=function(a){ya(a);W(\"contextmenu\",ta.contextmenuListener)}}jQuery.data(J,\"eobj2\",ta);var aa={},ua=false;if(window.MSPointerEvent||window.PointerEvent)(function(){function a(){ua=y(e)>0}function b(j){if(bb(j)){ya(j);e.push(j);a();aa.start(J,{touches:e.slice(0)})}}function c(j){if(ua)if(bb(j)){ya(j);var m;for(m=0;m<y(e);++m)if(e[m].pointerId===j.pointerId){e.splice(m,1);break}a();aa.end(J,{touches:e.slice(0),changedTouches:[]})}}function d(j){if(bb(j)){ya(j);var m;for(m=0;m< y(e);++m)if(e[m].pointerId===j.pointerId){e[m]=j;break}a();aa.moved(J,{touches:e.slice(0)})}}var e=[],f=jQuery.data(J,\"eobj\");if(f)if(window.PointerEvent){W(\"pointerdown\",f.pointerDown);W(\"pointerup\",f.pointerUp);W(\"pointerout\",f.pointerUp);W(\"pointermove\",f.pointerMove)}else{W(\"MSPointerDown\",f.pointerDown);W(\"MSPointerUp\",f.pointerUp);W(\"MSPointerOut\",f.pointerUp);W(\"MSPointerMove\",f.pointerMove)}jQuery.data(J,\"eobj\",{pointerDown:b,pointerUp:c,pointerMove:d});if(window.PointerEvent){ga(\"pointerdown\", b);ga(\"pointerup\",c);ga(\"pointerout\",c);ga(\"pointermove\",d)}else{ga(\"MSPointerDown\",b);ga(\"MSPointerUp\",c);ga(\"MSPointerOut\",c);ga(\"MSPointerMove\",d)}})();var I=jQuery.data(J,\"oobj\"),C=null,na=true,D=null,G=false,t=-1,r=[],ja=false,ra=false,U=null,hb=null,ib=null,k={x:0,y:0},ba=null,oa=null,p=jQuery.data(J,\"tobj\");if(!p){p={overTooltip:false};jQuery.data(J,\"tobj\",p)}var Da=null,Ba=false,Sa=null,cb=[0,0,0,0,0,0];ka(cb,i());var Ta=[];for(H=0;H<M();++H){Ta.push([0,0,0,0,0,0]);ka(Ta[H],g(H))}var qa=function(a, b){ka(a,b);Ha()};A.combinedTransform=la;this.updateTooltip=function(a){Ra();if(a)if(p.tooltipPosition){p.toolTipEl=document.createElement(\"div\");p.toolTipEl.className=l.ToolTipInnerStyle;p.toolTipEl.innerHTML=a;p.tooltipOuterDiv=document.createElement(\"div\");p.tooltipOuterDiv.className=l.ToolTipOuterStyle;document.body.appendChild(p.tooltipOuterDiv);p.tooltipOuterDiv.appendChild(p.toolTipEl);var b=E.widgetPageCoordinates(A.canvas);a=p.tooltipPosition[v]+b.x;b=p.tooltipPosition[u]+b.y;E.fitToWindow(p.tooltipOuterDiv, a+10,b+10,a-10,b-10);$(p.toolTipEl).mouseenter(function(){p.overTooltip=true});$(p.toolTipEl).mouseleave(function(){p.overTooltip=false})}};this.mouseMove=function(a,b){setTimeout(function(){setTimeout(Ra,200);if(!ua){var c=E.widgetCoordinates(A.canvas,b);if(Ma(c,h())){if(!p.tooltipEl&&nb()){p.tooltipPosition=[c.x,c.y];p.tooltipTimeout=setTimeout(function(){sb()},tb)}if(D===null&&xa()&&na){C=[c.x,c.y];ub(db)}}}},0)};this.mouseOut=function(){setTimeout(Ra,200)};this.mouseDown=function(a,b){if(!ua){a= E.widgetCoordinates(A.canvas,b);b=Oa(a.x,a.y);var c=Ma(a,h()),d=Na(a.x,a.y);if(!(b===-1&&!d&&!c)){D=a;G=d;t=b}}};this.mouseUp=function(){if(!ua){D=null;G=false;t=-1}};this.mouseDrag=function(a,b){if(!ua)if(D===null)ia.mouseDown(a,b);else{a=E.widgetCoordinates(A.canvas,b);if(E.buttons===1)if(t===-1&&!G&&Ga()&&ca(fa())){b=fa();var c;c=B()?a.x-D.x:a.y-D.y;ka(da(b),w([1,0,0,1,0,c/g(P(seriesNb))[3]],da(b)));Y()}else l.pan&&Z({x:a.x-D.x,y:a.y-D.y},0,t,G);D=a}};this.clicked=function(a,b){if(!ua)if(D===null)if(lb()){a= E.widgetCoordinates(A.canvas,b);wa.emit(A.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){var c=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;a=l.wheelActions[c];if(!N(a)){var d=E.widgetCoordinates(A.canvas,b),e=Na(d.x,d.y),f=Oa(d.x,d.y),j=Ma(d,h());if(!(!e&&f===-1&&!j)){var m=E.normalizeWheel(b);if(j&&c===0&&Ga()){c=fa();j=-m.spinY;if(ca(c)){a=kb(c);a=Hb(a,Pa(c));a=Ib(a,B());a=(a[0]+a[1])/2;E.cancelEvent(b);b=Math.pow(1.2,j);ka(da(c),w([1,0,0,b,0,a-b*a],da(c)));Y();return}}if((a=== 4||a===5||a===6)&&l.pan){c=i()[4];j=[];for(d=0;d<M();++d)j.push(g(d)[5]);if(a===6)Z({x:-m.pixelX,y:-m.pixelY},0,f,e);else if(a===5)Z({x:0,y:-m.pixelX-m.pixelY},0,f,e);else a===4&&Z({x:-m.pixelX-m.pixelY,y:0},0,f,e);c!==i()[4]&&E.cancelEvent(b);for(d=0;d<M();++d)j[d]!==g(d)[5]&&E.cancelEvent(b)}else if(l.zoom){E.cancelEvent(b);j=-m.spinY;if(j===0)j=-m.spinX;if(a===1)La(d,0,j,f,e);else if(a===0)La(d,j,0,f,e);else if(a===2)La(d,j,j,f,e);else if(a===3)m.pixelX!==0?La(d,j,0,f,e):La(d,0,j,f,e)}}}};var Lb= function(){lb()&&wa.emit(A.widget,\"seriesSelected\",D.x,D.y)};aa.start=function(a,b,c){ja=y(b.touches)===1;ra=y(b.touches)===2;if(ja){Ba=false;var d=E.widgetCoordinates(A.canvas,b.touches[0]);a=Oa(d.x,d.y);var e=Ma(d,h()),f=Na(d.x,d.y);if(a===-1&&!f&&!e)return;Da=a===-1&&!f&&xa()&&Ab(C,[d.x,d.y],30)?1:0;oa=Date.now();D=d;t=a;G=f;if(Da!==1){if(!c&&e)ba=window.setTimeout(Lb,200);ga(\"contextmenu\",ta.contextmenuListener)}E.capture(null);E.capture(vb())}else if(ra&&(l.zoom||Ga())){if(ba){window.clearTimeout(ba); ba=null}Ba=false;r=[E.widgetCoordinates(A.canvas,b.touches[0]),E.widgetCoordinates(A.canvas,b.touches[1])].map(function(j){return[j.x,j.y]});f=false;a=-1;if(!r.every(function(j){return Ma(j,h())})){(f=Na(r[0][v],r[0][u])&&Na(r[1][v],r[1][u]))||(a=Oa(r[0][v],r[0][u]));if(!f&&(a===-1||Oa(r[1][v],r[1][u])!==a)){ra=null;return}G=f;t=a}E.capture(null);E.capture(vb());U=Math.atan2(r[1][1]-r[0][1],r[1][0]-r[0][0]);hb=[(r[0][0]+r[1][0])/2,(r[0][1]+r[1][1])/2];c=Math.abs(Math.sin(U));d=Math.abs(Math.cos(U)); U=c<Math.sin(0.125*Math.PI)?0:d<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(U)>0?Math.PI/4:-Math.PI/4;ib=Jb(U,hb);G=f;t=a}else return;ya(b)};aa.end=function(a,b){if(ba){window.clearTimeout(ba);ba=null}window.setTimeout(function(){W(\"contextmenu\",ta.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),d=y(c)===0;d||function(){var e;for(e=0;e<y(b.changedTouches);++e)(function(){for(var f=b.changedTouches[e].identifier,j=0;j<y(c);++j)if(c[j].identifier===f){c.splice(j,1);return}})()}(); d=y(c)===0;ja=y(c)===1;ra=y(c)===2;if(d){$a=null;if(Da===0&&(isFinite(k.x)||isFinite(k.y))&&l.rubberBand){oa=Date.now();Ba=true;Xa(eb)}else{Da===1&&ia.mouseUp(null,null);c=[];oa=ib=hb=U=null}Da=null}else if(ja||ra)aa.start(a,b,true)};var $a=null,va=null,jb=null;aa.moved=function(a,b){if(ja||ra)if(!(ja&&D==null)){ya(b);va=E.widgetCoordinates(A.canvas,b.touches[0]);if(y(b.touches)>1)jb=E.widgetCoordinates(A.canvas,b.touches[1]);if(!G&&t===-1&&ja&&ba&&!Ab([va.x,va.y],[D.x,D.y],3)){window.clearTimeout(ba); ba=null}$a||($a=setTimeout(function(){if(!G&&t===-1&&ja&&Ga()&&ca(fa())){var c=fa();if(ca(c)){var d=va,e;e=B()?(d.x-D.x)/g(P(fa()))[3]:(d.y-D.y)/g(P(fa()))[3];da(c)[5]+=e;D=d;Y()}}else if(ja){d=va;e=Date.now();var f={x:d.x-D.x,y:d.y-D.y};c=e-oa;oa=e;if(Da===1){C[v]+=f.x;C[u]+=f.y;xa()&&na&&Xa(db)}else if(l.pan){k.x=f.x/c;k.y=f.y/c;Z(f,l.rubberBand?zb:0,t,G)}D=d}else if(!G&&t===-1&&ra&&Ga()&&ca(fa())){f=B()?v:u;e=[va,jb].map(function(S){return B()?[S.x,sa]:[Ea,S.y]});c=Math.abs(r[1][f]-r[0][f]);var j= Math.abs(e[1][f]-e[0][f]),m=c>0?j/c:1;if(j===c)m=1;c=fa();if(ca(c)){var sa=w(Ia(la(P(c))),[0,(r[0][f]+r[1][f])/2])[1],Fa=w(Ia(la(P(c))),[0,(e[0][f]+e[1][f])/2])[1];ka(da(c),w([1,0,0,m,0,-m*sa+Fa],da(c)));D=d;Y();r=e}}else if(ra&&l.zoom){d=ma(C,Q());var Ea=(r[0][0]+r[1][0])/2;sa=(r[0][1]+r[1][1])/2;e=[va,jb].map(function(S){return U===0?[S.x,sa]:U===Math.PI/2?[Ea,S.y]:w(ib,[S.x,S.y])});f=Math.abs(r[1][0]-r[0][0]);c=Math.abs(e[1][0]-e[0][0]);var O=f>0?c/f:1;if(c===f||U===Math.PI/2)O=1;var ab=(e[0][0]+ e[1][0])/2;c=Math.abs(r[1][1]-r[0][1]);j=Math.abs(e[1][1]-e[0][1]);m=c>0?j/c:1;if(j===c||U===0)m=1;Fa=(e[0][1]+e[1][1])/2;B()&&function(){var S=O;O=m;m=S;S=ab;ab=Fa;Fa=S;S=Ea;Ea=sa;sa=S}();if(i()[0]*O>ea())O=ea()/i()[0];if(i()[0]*O<Qa())O=Qa()/i()[0];f=[];for(c=0;c<M();++c)f.push(m);for(c=0;c<M();++c){if(g(c)[3]*f[c]>T(c))f[c]=T(c)/g(c)[3];if(g(c)[3]*f[c]<pb(c))f[c]=pb(c)/g(c)[3]}if(G){if(O!==1&&(O<1||i()[0]!==ea()))qa(i(),w([O,0,0,1,-O*Ea+ab,0],i()))}else if(t===-1){if(O!==1&&(O<1||i()[0]!==ea()))qa(i(), w([O,0,0,1,-O*Ea+ab,0],i()));for(c=0;c<M();++c)if(f[c]!==1&&(f[c]<1||g(c)[3]!==T(c)))qa(g(c),w([1,0,0,f[c],0,-f[c]*sa+Fa],g(c)))}else if(f[t]!==1&&(f[t]<1||g(t)[3]!==T(t)))qa(g(t),w([1,0,0,f[t],0,-f[t]*sa+Fa],g(t)));ha();d=Ua(d,Q());C[v]=d[v];C[u]=d[u];r=e;Ya();Y();Ja()}$a=null},1))}};this.setXRange=function(a,b,c,d){b=o(0)[0]+o(0)[2]*b;c=o(0)[0]+o(0)[2]*c;if(q(o(0))>s(o(0))){if(b>q(o(0)))b=q(o(0));if(c<s(o(0)))c=s(o(0))}else{if(b<q(o(0)))b=q(o(0));if(c>s(o(0)))c=s(o(0))}var e=Pa(a);e=Kb(e,P(a),b, c,B(),h(),o(P(a)),l.minZoom,l.maxZoom);b=e.xZoom;c=e.yZoom;e=e.panPoint;var f=ma(C,Q());i()[0]=b;if(c&&d)g(P(a))[3]=c;i()[4]=-e[v]*b;if(c&&d)g(P(a))[5]=-e[u]*c;Ha();a=Ua(f,Q());C[v]=a[v];C[u]=a[u];ha();Ya();Y();Ja()};this.getSeries=function(a){return Pa(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))l[b]=a[b];Fb();Ya();Y();Ja()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ia.touchStart=aa.start;ia.touchEnd= aa.end;ia.touchMoved=aa.moved}else{H=function(){};ia.touchStart=H;ia.touchEnd=H;ia.touchMoved=H}}");
	}

	static String locToJsString(AxisValue loc) {
		switch (loc) {
		case MinimumValue:
			return "min";
		case MaximumValue:
			return "max";
		case ZeroValue:
			return "zero";
		case BothSides:
			return "both";
		}
		assert false;
		return "";
	}

	static int binarySearchRow(final WAbstractChartModel model, int xColumn,
			double d, int minRow, int maxRow) {
		if (minRow == maxRow) {
			return minRow;
		}
		double min = model.getData(minRow, xColumn);
		double max = model.getData(maxRow, xColumn);
		if (d <= min) {
			return minRow;
		}
		if (d >= max) {
			return maxRow;
		}
		double start = minRow + (d - min) / (max - min) * (maxRow - minRow);
		double data = model.getData((int) start, xColumn);
		if (data < d) {
			return binarySearchRow(model, xColumn, d, (int) start + 1, maxRow);
		} else {
			if (data > d) {
				return binarySearchRow(model, xColumn, d, minRow,
						(int) start - 1);
			} else {
				return (int) start;
			}
		}
	}

	private static final int TICK_LENGTH = 5;
	private static final int CURVE_LABEL_PADDING = 10;
	private static final int DEFAULT_CURVE_LABEL_WIDTH = 100;
	private static final int CURVE_SELECTION_DISTANCE_SQUARED = 400;

	static int toZoomLevel(double zoomFactor) {
		return (int) Math.floor(Math.log(zoomFactor) / Math.log(2.0) + 0.5) + 1;
	}
}
