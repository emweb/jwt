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
 * A curve label.
 *
 * <p>Curve labels can be added with {@link WCartesianChart#addCurveLabel(CurveLabel label)
 * WCartesianChart#addCurveLabel()}. They are associated with a particular series, and are drawn at
 * the given point in model coordinates. When the chart is transformed (zoom or pan) or the
 * associated series is manipulated, the curve label&apos;s position will change, but not its size.
 *
 * <p><div align="center"> <img src="doc-files/CurveLabel.png">
 *
 * <p><strong>A curve label</strong> </div>
 */
public class CurveLabel {
  private static Logger logger = LoggerFactory.getLogger(CurveLabel.class);

  /**
   * Create a new curve label.
   *
   * <p>Create a new curve label for given series, at the given point with the given text.
   */
  public CurveLabel(final WDataSeries series, final WPointF point, final String label) {
    this.series_ = series;
    this.x_ = new Object();
    this.y_ = new Object();
    this.label_ = label;
    this.offset_ = new WPointF(60, -20);
    this.width_ = 0;
    this.linePen_ = new WPen(new WColor(0, 0, 0));
    this.textPen_ = new WPen(new WColor(0, 0, 0));
    this.boxBrush_ = new WBrush(new WColor(255, 255, 255));
    this.markerBrush_ = new WBrush(new WColor(0, 0, 0));
    this.x_ = point.getX();
    this.y_ = point.getY();
  }
  /**
   * Create a new curve label.
   *
   * <p>Create a new curve label for given series, at the given x, y coordinates and the given text.
   */
  public CurveLabel(final WDataSeries series, final Object x, final Object y, final String label) {
    this.series_ = series;
    this.x_ = x;
    this.y_ = y;
    this.label_ = label;
    this.offset_ = new WPointF(60, -20);
    this.width_ = 0;
    this.linePen_ = new WPen(new WColor(0, 0, 0));
    this.textPen_ = new WPen(new WColor(0, 0, 0));
    this.boxBrush_ = new WBrush(new WColor(255, 255, 255));
    this.markerBrush_ = new WBrush(new WColor(0, 0, 0));
  }
  /** Set the series this curve label is associated with. */
  public void setSeries(final WDataSeries series) {
    this.series_ = series;
  }
  /**
   * Get the series this curve label is associated with.
   *
   * <p>
   *
   * @see CurveLabel#setSeries(WDataSeries series)
   */
  public WDataSeries getSeries() {
    return this.series_;
  }
  /** Set the point in model coordinates this label is associated with. */
  public void setPoint(final WPointF point) {
    this.x_ = point.getX();
    this.y_ = point.getY();
  }
  /** Set the point in model coordinates this label is associated with. */
  public void setPoint(final Object x, final Object y) {
    this.x_ = x;
    this.y_ = y;
  }
  /**
   * Get the point in model coordinates this label is associated with.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This uses asNumber(), which may not be the same conversion that {@link
   * WCartesianChart#mapToDevice(Object xValue, Object yValue, Axis axis, int xSegment, int
   * ySegment) WCartesianChart#mapToDevice()} uses, depending on the scale of the axis. {@link
   * CurveLabel#getX() getX()} and {@link CurveLabel#getY() getY()} will perform no conversion, so
   * they may be safer to use.</i>
   *
   * @see CurveLabel#setPoint(WPointF point)
   */
  public WPointF getPoint() {
    return new WPointF(StringUtils.asNumber(this.x_), StringUtils.asNumber(this.y_));
  }
  /**
   * Get the x position for this label.
   *
   * <p>
   *
   * @see CurveLabel#setPoint(WPointF point)
   */
  public Object getX() {
    return this.x_;
  }
  /**
   * Get the y position for this label.
   *
   * <p>
   *
   * @see CurveLabel#setPoint(WPointF point)
   */
  public Object getY() {
    return this.y_;
  }
  /** Set the label that should be drawn in the box. */
  public void setLabel(final String label) {
    this.label_ = label;
  }
  /**
   * Get the label that should be drawn in the box.
   *
   * <p>
   *
   * @see CurveLabel#setLabel(String label)
   */
  public String getLabel() {
    return this.label_;
  }
  /**
   * Set the offset the text should be placed at.
   *
   * <p>The offset is defined in pixels, with x values going from left to right, and y values from
   * top to bottom.
   *
   * <p>The default offset is (60, -20), which means the middle of the {@link CurveLabel#getLabel()
   * getLabel()} is drawn 60 pixels to the right, and 20 pixels above the point.
   */
  public void setOffset(final WPointF offset) {
    this.offset_ = offset;
  }
  /**
   * Get the offset the text should be placed at.
   *
   * <p>
   *
   * @see CurveLabel#setOffset(WPointF offset)
   */
  public WPointF getOffset() {
    return this.offset_;
  }
  /**
   * Set the width of the box in pixels.
   *
   * <p>If the width is 0 (the default), server side font metrics will be used to determine the size
   * of the box.
   */
  public void setWidth(int width) {
    this.width_ = width;
  }
  /**
   * Get the width of the box in pixels.
   *
   * <p>
   *
   * @see CurveLabel#setWidth(int width)
   */
  public int getWidth() {
    return this.width_;
  }
  /**
   * Set the pen to use for the connecting line.
   *
   * <p>This sets the pen to use for the line connecting the {@link CurveLabel#getPoint()
   * getPoint()} to the box with the {@link CurveLabel#getLabel() getLabel()} at {@link
   * CurveLabel#getOffset() getOffset()} pixels from the point.
   */
  public void setLinePen(final WPen pen) {
    this.linePen_ = pen;
  }
  /**
   * Get the pen to use for the connecting line.
   *
   * <p>
   *
   * @see CurveLabel#setLinePen(WPen pen)
   */
  public WPen getLinePen() {
    return this.linePen_;
  }
  /** Set the pen for the text in the box. */
  public void setTextPen(final WPen pen) {
    this.textPen_ = pen;
  }
  /**
   * Get the pen for the text in the box.
   *
   * <p>
   *
   * @see CurveLabel#setTextPen(WPen pen)
   */
  public WPen getTextPen() {
    return this.textPen_;
  }
  /**
   * Set the brush to use for the box around the text.
   *
   * <p>This sets the brush used to fill the box with the text defined in {@link
   * CurveLabel#getLabel() getLabel()}.
   */
  public void setBoxBrush(final WBrush brush) {
    this.boxBrush_ = brush;
  }
  /**
   * Get the brush to use for the box around the text.
   *
   * <p>
   *
   * @see CurveLabel#setBoxBrush(WBrush brush)
   */
  public WBrush getBoxBrush() {
    return this.boxBrush_;
  }
  /** Set the brush used to fill the circle at {@link CurveLabel#getPoint() getPoint()}. */
  public void setMarkerBrush(final WBrush brush) {
    this.markerBrush_ = brush;
  }
  /**
   * Get the brush used to fill the circle at {@link CurveLabel#getPoint() getPoint()}.
   *
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
        if (painter.getDevice().getFeatures().contains(PaintDeviceFeatureFlag.FontMetrics)) {
          WMeasurePaintDevice device = new WMeasurePaintDevice(painter.getDevice());
          WPainter measPainter = new WPainter(device);
          measPainter.drawText(
              new WRectF(0, 0, 100, 100),
              EnumUtils.or(EnumSet.of(AlignmentFlag.Middle), AlignmentFlag.Center),
              TextFlag.SingleLine,
              this.getLabel(),
              (WPointF) null);
          rectWidth = device.getBoundingRect().getWidth() + CURVE_LABEL_PADDING / 2;
        }
      }
      rect =
          new WRectF(
                  this.getOffset().getX() - rectWidth / 2,
                  this.getOffset().getY() - 10,
                  rectWidth,
                  20)
              .getNormalized();
    }
    WPointF closestAnchor = new WPointF();
    {
      List<WPointF> anchorPoints = new ArrayList<WPointF>();
      anchorPoints.add(new WPointF(rect.getLeft(), rect.getCenter().getY()));
      anchorPoints.add(new WPointF(rect.getRight(), rect.getCenter().getY()));
      anchorPoints.add(new WPointF(rect.getCenter().getX(), rect.getTop()));
      anchorPoints.add(new WPointF(rect.getCenter().getX(), rect.getBottom()));
      double minSquareDist = Double.POSITIVE_INFINITY;
      for (int k = 0; k < anchorPoints.size(); ++k) {
        final WPointF anchorPoint = anchorPoints.get(k);
        double d =
            anchorPoint.getX() * anchorPoint.getX() + anchorPoint.getY() * anchorPoint.getY();
        if (d < minSquareDist
            && (k == 0
                || !checkIntersectVertical(
                    new WPointF(), anchorPoint, rect.getTop(), rect.getBottom(), rect.getLeft()))
            && (k == 1
                || !checkIntersectVertical(
                    new WPointF(), anchorPoint, rect.getTop(), rect.getBottom(), rect.getRight()))
            && (k == 2
                || !checkIntersectHorizontal(
                    new WPointF(), anchorPoint, rect.getLeft(), rect.getRight(), rect.getTop()))
            && (k == 3
                || !checkIntersectHorizontal(
                    new WPointF(),
                    anchorPoint,
                    rect.getLeft(),
                    rect.getRight(),
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
    painter.strokePath(translation.map(connectorLine).getCrisp(), this.getLinePen());
    WPainterPath circle = new WPainterPath();
    circle.addEllipse(-2.5, -2.5, 5, 5);
    painter.fillPath(translation.map(circle), this.getMarkerBrush());
    WPainterPath rectPath = new WPainterPath();
    rectPath.addRect(rect);
    painter.fillPath(translation.map(rectPath), this.getBoxBrush());
    painter.strokePath(translation.map(rectPath).getCrisp(), this.getLinePen());
    painter.setPen(this.getTextPen());
    painter.drawText(
        translation.map(rect),
        EnumUtils.or(EnumSet.of(AlignmentFlag.Middle), AlignmentFlag.Center),
        TextFlag.SingleLine,
        this.getLabel(),
        (WPointF) null);
  }

  private WDataSeries series_;
  private Object x_;
  private Object y_;
  private String label_;
  private WPointF offset_;
  private int width_;
  private WPen linePen_;
  private WPen textPen_;
  private WBrush boxBrush_;
  private WBrush markerBrush_;

  private static boolean checkIntersectHorizontal(
      final WPointF p1, final WPointF p2, double minX, double maxX, double y) {
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

  private static boolean checkIntersectVertical(
      final WPointF p1, final WPointF p2, double minY, double maxY, double x) {
    return checkIntersectHorizontal(
        new WPointF(p1.getY(), p1.getX()), new WPointF(p2.getY(), p2.getX()), minY, maxY, x);
  }

  static WJavaScriptPreamble wtjs2() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "ChartCommon",
        "(function(t){var n=t.WT,r=this,i=n.gfxUtils,e=i.rect_top,f=i.rect_bottom,o=i.rect_left,u=i.rect_right,a=i.transform_mult;function h(t,n,r,i){var e=0;i&&(e=1);var f=n.length;function o(t){return r?n[t]:n[f-1-t]}function u(t){for(;2===o(t)[2]||3===o(t)[2];)t--;return t}var a=Math.floor(f/2);a=u(a);var h=0,s=f,l=!1;if(o(0)[e]>t)return r?-1:f;if(o(f-1)[e]<t)return r?f:-1;for(;!l;){var c=a+1;c<f&&(2===o(c)[2]||3===o(c)[2])&&(c+=2);if(o(a)[e]>t){s=a;a=u(a=Math.floor((s+h)/2))}else if(o(a)[e]===t)l=!0;else if(c<f&&o(c)[e]>t)l=!0;else if(c<f&&o(c)[e]===t){a=c;l=!0}else{h=a;a=u(a=Math.floor((s+h)/2))}}return r?a:f-1-a}function s(t,n){return n[0][t]<n[n.length-1][t]}this.findClosestPoint=function(t,n,r){var i=0;r&&(i=1);var e=s(i,n),f=h(t,n,e,r);f<0&&(f=0);if(f>=n.length)return[n[n.length-1][0],n[n.length-1][1]];f>=n.length&&(f=n.length-2);if(n[f][i]===t)return[n[f][0],n[f][1]];var o=e?f+1:f-1;e&&2==n[o][2]&&(o+=2);if(!e&&o<0)return[n[f][0],n[f][1]];!e&&o>0&&3==n[o][2]&&(o-=2);return Math.abs(t-n[f][i])<Math.abs(n[o][i]-t)?[n[f][0],n[f][1]]:[n[o][0],n[o][1]]};this.minMaxY=function(t,n){for(var r=n?0:1,i=t[0][r],e=t[0][r],f=1;f<t.length;++f)if(2!==t[f][2]&&3!==t[f][2]&&5!==t[f][2]){t[f][r]>e&&(e=t[f][r]);t[f][r]<i&&(i=t[f][r])}return[i,e]};this.projection=function(t,n){var r=Math.cos(t),i=Math.sin(t),e=r*r,f=i*i,o=r*i,u=-n[0]*r-n[1]*i;return[e,o,o,f,r*u+n[0],i*u+n[1]]};this.distanceSquared=function(t,n){var r=[n[0]-t[0],n[1]-t[1]];return r[0]*r[0]+r[1]*r[1]};this.distanceLessThanRadius=function(t,n,i){return i*i>=r.distanceSquared(t,n)};this.toZoomLevel=function(t){return Math.floor(Math.log(t)/Math.LN2+.5)+1};this.isPointInRect=function(t,n){var r,i;if(void 0!==t.x){r=t.x;i=t.y}else{r=t[0];i=t[1]}return r>=o(n)&&r<=u(n)&&i>=e(n)&&i<=f(n)};this.toDisplayCoord=function(t,n,r,i,e){var f,o;if(r){f=[(t[0]-e[0])/e[2],(t[1]-e[1])/e[3]];o=[i[0]+f[1]*i[2],i[1]+f[0]*i[3]]}else{f=[(t[0]-e[0])/e[2],1-(t[1]-e[1])/e[3]];o=[i[0]+f[0]*i[2],i[1]+f[1]*i[3]]}return a(n,o)};this.findYRange=function(t,n,i,u,a,l,c,m,g){if(0!==t.length){var v,d,M,x,b,y,C=r.toDisplayCoord([i,0],[1,0,0,1,0,0],a,l,c),_=r.toDisplayCoord([u,0],[1,0,0,1,0,0],a,l,c),p=a?1:0,R=a?0:1,D=s(p,t),E=h(C[p],t,D,a),L=h(_[p],t,D,a),O=1/0,T=-1/0,A=E===L&&E===t.length||-1===E&&-1===L;if(!A){D?E<0?E=0:t[++E]&&2===t[E][2]&&(E+=2):E>=t.length-1&&(E=t.length-2);!D&&L<0&&(L=0);for(v=Math.min(E,L);v<=Math.max(E,L)&&v<t.length;++v)if(2!==t[v][2]&&3!==t[v][2]){t[v][R]<O&&(O=t[v][R]);t[v][R]>T&&(T=t[v][R])}if(D&&E>0||!D&&E<t.length-1){D?t[x=E-1]&&3===t[x][2]&&(x-=2):t[x=E+1]&&2===t[x][2]&&(x+=2);d=(C[p]-t[x][p])/(t[E][p]-t[x][p]);(M=t[x][R]+d*(t[E][R]-t[x][R]))<O&&(O=M);M>T&&(T=M)}if(D&&L<t.length-1||!D&&L>0){D?2===t[b=L+1][2]&&(b+=2):3===t[b=L-1][2]&&(b-=2);d=(_[p]-t[L][p])/(t[b][p]-t[L][p]);(M=t[L][R]+d*(t[b][R]-t[L][R]))<O&&(O=M);M>T&&(T=M)}}var P=c[2]/(u-i),S=a?2:3;if(!A){y=l[S]/(T-O);10;(y=l[S]/(l[S]/y+20))>g.y[n]&&(y=g.y[n]);y<m.y[n]&&(y=m.y[n])}return{xZoom:P,yZoom:y,panPoint:a?[C[1]-e(l),A?0:(O+T)/2-l[2]/y/2-o(l)]:[C[0]-o(l),A?0:-((O+T)/2+l[3]/y/2-f(l))]}}};this.matchXAxis=function(t,n,r,i,a){function h(t){return i[t].side}function s(t){return i[t].width}function l(t){return i[t].minOffset}function c(t){return i[t].maxOffset}if(a){if(n<e(r)||n>f(r))return-1}else if(t<o(r)||t>u(r))return-1;for(var m=0;m<i.length;++m)if(a){if((\"min\"===h(m)||\"both\"===h(m))&&t>=o(r)-l(m)-s(m)&&t<=o(r)-l(m))return m;if((\"max\"===h(m)||\"both\"===h(m))&&t>=u(r)+c(m)&&t<=u(r)+c(m)+s(m))return m}else{if((\"min\"===h(m)||\"both\"===h(m))&&n<=f(r)+l(m)+s(m)&&n>=f(r)+l(m))return m;if((\"max\"===h(m)||\"both\"===h(m))&&n<=e(r)-c(m)&&n>=e(r)-c(m)-s(m))return m}return-1};this.matchYAxis=function(t,n,r,i,a){function h(t){return i[t].side}function s(t){return i[t].width}function l(t){return i[t].minOffset}function c(t){return i[t].maxOffset}if(a){if(t<o(r)||t>u(r))return-1}else if(n<e(r)||n>f(r))return-1;for(var m=0;m<i.length;++m)if(a){if((\"min\"===h(m)||\"both\"===h(m))&&n>=e(r)-l(m)-s(m)&&n<=e(r)-l(m))return m;if((\"max\"===h(m)||\"both\"===h(m))&&n>=f(r)+c(m)&&n<=f(r)+c(m)+s(m))return m}else{if((\"min\"===h(m)||\"both\"===h(m))&&t>=o(r)-l(m)-s(m)&&t<=o(r)-l(m))return m;if((\"max\"===h(m)||\"both\"===h(m))&&t>=u(r)+c(m)&&t<=u(r)+c(m)+s(m))return m}return-1}})");
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WCartesianChart",
        "(function(t,e,n,o){e.wtCObj=this;var i=this,r=t.WT;i.config=o;var a=r.gfxUtils,f=a.transform_mult,u=a.transform_inverted,s=a.transform_assign,l=a.transform_equal,c=a.transform_apply,v=a.rect_top,p=a.rect_bottom,x=a.rect_left,y=a.rect_right,h=a.rect_intersection,d=r.chartCommon,m=d.minMaxY,w=d.findClosestPoint,T=d.projection,M=d.distanceLessThanRadius,g=d.toZoomLevel,b=d.isPointInRect,P=d.findYRange,E=function(t,e){return d.matchXAxis(t,e,L(),o.xAxes,I())},C=function(t,e){return d.matchYAxis(t,e,L(),o.yAxes,I())};function O(t){return void 0===t}function A(t,e){var n,i=(n=t,o.xModelAreas[n]),r=function(t){return o.yModelAreas[t]}(e);return I()?[r[0],i[1],r[2],i[3]]:[i[0],r[1],i[2],r[3]]}function D(){return o.followCurve}function S(){return o.crosshair||-1!==D()}function I(){return o.isHorizontal}function _(t){return o.xTransforms[t]}function j(t){return o.yTransforms[t]}function L(){return o.area}function R(){return o.insideArea}function U(t){return O(t)?o.series:o.series[t]}function F(t){return U(t).transform}function Y(t){return I()?f([0,1,1,0,0,0],f(F(t),[0,1,1,0,0,0])):F(t)}function Z(t){return U(t).curve}function k(t){return U(t).xAxis}function X(t){return U(t).yAxis}function W(){return o.seriesSelection}function z(){return o.sliders}function B(){return o.hasToolTips}function N(){return o.coordinateOverlayPadding}function q(){return o.curveManipulation}function K(t){return o.minZoom.x[t]}function H(t){return o.minZoom.y[t]}function G(t){return o.maxZoom.x[t]}function J(t){return o.maxZoom.y[t]}function Q(){return o.pens}function V(){return o.penAlpha}function tt(){return o.selectedCurve}function et(t){t.preventDefault&&t.preventDefault()}function nt(t,n){e.addEventListener(t,n)}function ot(t,n){e.removeEventListener(t,n)}function it(t){return t.length}function rt(){return it(o.xAxes)}function at(){return it(o.yAxes)}function ft(){return o.crosshairXAxis}function ut(){return o.crosshairYAxis}var st=window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame||function(t){window.setTimeout(t,17)},lt=!1,ct=function(t){if(!lt){lt=!0;st((function(){t();lt=!1}))}};if(window.MSPointerEvent||window.PointerEvent){e.style.touchAction=\"none\";n.canvas.style.msTouchAction=\"none\";n.canvas.style.touchAction=\"none\"}var vt=.07,pt=.001,xt=1.5,yt=e.wtEObj2;yt||((yt={}).contextmenuListener=function(t){et(t);ot(\"contextmenu\",yt.contextmenuListener)});e.wtEObj2=yt;var ht={};function dt(t){return 2===t.pointerType||3===t.pointerType||\"pen\"===t.pointerType||\"touch\"===t.pointerType}var mt=!1;(window.MSPointerEvent||window.PointerEvent)&&function(){var t=[];function n(){mt=it(t)>0}function o(o){if(dt(o)){et(o);t.push(o);n();ht.start(e,{touches:t.slice(0)})}}function i(o){if(mt&&dt(o)){et(o);var i;for(i=0;i<it(t);++i)if(t[i].pointerId===o.pointerId){t.splice(i,1);break}n();ht.end(e,{touches:t.slice(0),changedTouches:[]})}}function r(o){if(dt(o)){et(o);var i;for(i=0;i<it(t);++i)if(t[i].pointerId===o.pointerId){t[i]=o;break}n();ht.moved(e,{touches:t.slice(0)})}}var a=e.wtEObj;if(a)if(window.PointerEvent){ot(\"pointerdown\",a.pointerDown);ot(\"pointerup\",a.pointerUp);ot(\"pointerout\",a.pointerUp);ot(\"pointermove\",a.pointerMove)}else{ot(\"MSPointerDown\",a.pointerDown);ot(\"MSPointerUp\",a.pointerUp);ot(\"MSPointerOut\",a.pointerUp);ot(\"MSPointerMove\",a.pointerMove)}e.wtEObj={pointerDown:o,pointerUp:i,pointerMove:r};if(window.PointerEvent){nt(\"pointerdown\",o);nt(\"pointerup\",i);nt(\"pointerout\",i);nt(\"pointermove\",r)}else{nt(\"MSPointerDown\",o);nt(\"MSPointerUp\",i);nt(\"MSPointerOut\",i);nt(\"MSPointerMove\",r)}}();var wt=e.wtOObj,Tt=null,Mt=!0,gt=null,bt=-1,Pt=-1,Et=[],Ct=!1,Ot=!1,At=null,Dt=null,St=null,It={x:0,y:0},_t=null,jt=null,Lt=e.wtTObj;if(!Lt){Lt={overTooltip:!1};e.wtTObj=Lt}function Rt(){if(Lt){if(Lt.tooltipTimeout){clearTimeout(Lt.tooltipTimeout);Lt.tooltipTimeout=null}if(!Lt.overTooltip&&Lt.tooltipOuterDiv){document.body.removeChild(Lt.tooltipOuterDiv);Lt.toolTipEl=null;Lt.tooltipOuterDiv=null}}}for(var Ut=null,Ft=!1,Yt=null,Zt=[],kt=0;kt<rt();++kt){Zt.push([0,0,0,0,0,0]);s(Zt[kt],_(kt))}var Xt=[];for(kt=0;kt<at();++kt){Xt.push([0,0,0,0,0,0]);s(Xt[kt],j(kt))}function Wt(){if(function(){for(var t=0;t<rt();++t)if(o.notifyTransform.x[t])return!0;for(t=0;t<at();++t)if(o.notifyTransform.y[t])return!0;return!1}()){if(Yt){window.clearTimeout(Yt);Yt=null}Yt=setTimeout((function(){for(var e=0;e<rt();++e)if(o.notifyTransform.x[e]&&!l(Zt[e],_(e))){t.emit(n.widget,\"xTransformChanged\"+e);s(Zt[e],_(e))}for(e=0;e<at();++e)if(o.notifyTransform.y[e]&&!l(Xt[e],j(e))){t.emit(n.widget,\"yTransformChanged\"+e);s(Xt[e],j(e))}}),250)}}var zt=function(t,e){s(t,e);Wt()};function Bt(t,e){void 0===t&&(t=0);void 0===e&&(e=0);var n,o,i;if(I()){n=x(L());i=v(L());return f([0,1,1,0,n,i],f(_(t),f(j(e),[0,1,1,0,-i,-n])))}n=x(L());o=p(L());return f([1,0,0,-1,n,o],f(_(t),f(j(e),[1,0,0,-1,-n,o])))}n.combinedTransform=Bt;function Nt(t,e){return f(Bt(t,e),R())}function qt(t,e,n,o){O(o)&&(o=!1);var i,r;i=o?t:f(u(Bt(e,n)),t);r=I()?[(i[1]-L()[1])/L()[3],(i[0]-L()[0])/L()[2]]:[(i[0]-L()[0])/L()[2],1-(i[1]-L()[1])/L()[3]];return[A(e,n)[0]+r[0]*A(e,n)[2],A(e,n)[1]+r[1]*A(e,n)[3]]}function Kt(t,e,n,o){O(o)&&(o=!1);return d.toDisplayCoord(t,o?[1,0,0,1,0,0]:Bt(e,n),I(),L(),A(e,n))}function Ht(){for(var t=0;t<rt();++t){var e,n,o,i=A(t,0);if(I()){e=(qt([0,v(L())],t,0)[0]-i[0])/i[2];n=(qt([0,p(L())],t,0)[0]-i[0])/i[2]}else{e=(qt([x(L()),0],t,0)[0]-i[0])/i[2];n=(qt([y(L()),0],t,0)[0]-i[0])/i[2]}for(o=0;o<it(z());++o){var r=document.getElementById(z()[o]);if(r){var a=r.wtSObj;a&&a.xAxis()===t&&a.changeRange(e,n)}}}}function $t(){Rt();B()&&Lt.tooltipPosition&&(Lt.tooltipTimeout=setTimeout((function(){Qt()}),500));Mt&&ct((function(){n.repaint();S()&&Gt()}))}function Gt(){if(Mt){var t=wt.getContext(\"2d\");t.clearRect(0,0,wt.width,wt.height);t.save();t.beginPath();t.moveTo(x(L()),v(L()));t.lineTo(y(L()),v(L()));t.lineTo(y(L()),p(L()));t.lineTo(x(L()),p(L()));t.closePath();t.clip();var e,n=f(u(Bt(ft(),ut())),Tt),i=Tt[0],r=Tt[1];if(-1!==D()){n=w(I()?n[1]:n[0],Z(D()),I());var a=f(Bt(k(D()),X(D())),f(Y(D()),n));i=a[0];r=a[1];Tt[0]=i;Tt[1]=r}e=I()?[(n[1]-L()[1])/L()[3],(n[0]-L()[0])/L()[2]]:[(n[0]-L()[0])/L()[2],1-(n[1]-L()[1])/L()[3]];if(-1!==D()){n=[(s=A(k(D()),X(D())))[0]+e[0]*s[2],s[1]+e[1]*s[3]]}else{var s;n=[(s=A(ft(),ut()))[0]+e[0]*s[2],s[1]+e[1]*s[3]]}t.fillStyle=t.strokeStyle=o.crosshairColor;t.font=\"16px sans-serif\";t.textAlign=\"right\";t.textBaseline=\"top\";var l=n[0].toFixed(2),c=n[1].toFixed(2);\"-0.00\"===l&&(l=\"0.00\");\"-0.00\"===c&&(c=\"0.00\");t.fillText(\"(\"+l+\",\"+c+\")\",y(L())-N()[0],v(L())+N()[1]);t.setLineDash&&t.setLineDash([1,2]);t.beginPath();t.moveTo(Math.floor(i)+.5,Math.floor(v(L()))+.5);t.lineTo(Math.floor(i)+.5,Math.floor(p(L()))+.5);t.moveTo(Math.floor(x(L()))+.5,Math.floor(r)+.5);t.lineTo(Math.floor(y(L()))+.5,Math.floor(r)+.5);t.stroke();t.restore()}}function Jt(t){var e;I()&&(1===t?t=2:2===t&&(t=1));for(var n=0;n<rt();++n){var o=Nt(n,0);if(I()){if(O(t)||2===t){if(_(n)[0]<1){_(n)[0]=1;o=Nt(n,0)}if(v(o)>v(R())){e=v(R())-v(o);_(n)[4]=_(n)[4]+e}else if(p(o)<p(R())){e=p(R())-p(o);_(n)[4]=_(n)[4]+e}}}else if(O(t)||1===t){if(_(n)[0]<1){_(n)[0]=1;o=Nt(n,0)}if(x(o)>x(R())){e=x(R())-x(o);_(n)[4]=_(n)[4]+e}else if(y(o)<y(R())){e=y(R())-y(o);_(n)[4]=_(n)[4]+e}}}for(n=0;n<at();++n){o=Nt(0,n);if(I()){if(O(t)||1===t){if(j(n)[3]<1){j(n)[3]=1;o=Nt(0,n)}if(x(o)>x(R())){e=x(R())-x(o);j(n)[5]=j(n)[5]+e}else if(y(o)<y(R())){e=y(R())-y(o);j(n)[5]=j(n)[5]+e}}}else if(O(t)||2===t){if(j(n)[3]<1){j(n)[3]=1;o=Nt(0,n)}if(v(o)>v(R())){e=v(R())-v(o);j(n)[5]=j(n)[5]-e}else if(p(o)<p(R())){e=p(R())-p(o);j(n)[5]=j(n)[5]-e}}}Wt()}function Qt(){Lt.toolTipEl||t.emit(n.widget,\"loadTooltip\",Lt.tooltipPosition[0],Lt.tooltipPosition[1])}this.updateTooltip=function(t){Rt();if(t){if(!Lt.tooltipPosition)return;Lt.toolTipEl=document.createElement(\"div\");Lt.toolTipEl.className=o.ToolTipInnerStyle;Lt.toolTipEl.innerHTML=t;Lt.tooltipOuterDiv=document.createElement(\"div\");Lt.tooltipOuterDiv.className=o.ToolTipOuterStyle;document.body.appendChild(Lt.tooltipOuterDiv);Lt.tooltipOuterDiv.appendChild(Lt.toolTipEl);var e=r.widgetPageCoordinates(n.canvas),i=Lt.tooltipPosition[0]+e.x,a=Lt.tooltipPosition[1]+e.y;r.fitToWindow(Lt.tooltipOuterDiv,i+10,a+10,i-10,a-10);$(Lt.toolTipEl).mouseenter((function(){Lt.overTooltip=!0}));$(Lt.toolTipEl).mouseleave((function(){Lt.overTooltip=!1}))}};this.mouseMove=function(t,e){setTimeout((function(){setTimeout(Rt,200);if(!mt){var t=r.widgetCoordinates(n.canvas,e);if(b(t,L())){if(B()){Lt.tooltipPosition=[t.x,t.y];Lt.tooltipTimeout=setTimeout((function(){Qt()}),500)}if(null===gt&&S()&&Mt){Tt=[t.x,t.y];ct(Gt)}}}}),0)};this.mouseOut=function(t,e){setTimeout(Rt,200)};this.mouseDown=function(t,e){if(!mt){var o=r.widgetCoordinates(n.canvas,e),i=C(o.x,o.y),a=b(o,L()),f=E(o.x,o.y);if(-1!==i||-1!==f||a){gt=o;bt=f;Pt=i}}};this.mouseUp=function(t,e){if(!mt){gt=null;bt=-1;Pt=-1}};this.mouseDrag=function(t,e){if(!mt)if(null!==gt){var a=r.widgetCoordinates(n.canvas,e);if(1===r.buttons)if(-1===Pt&&-1===bt&&q()&&U(tt())){var u,l=tt();u=I()?a.x-gt.x:a.y-gt.y;s(F(l),f([1,0,0,1,0,u/j(X(tt()))[3]],F(l)));$t()}else o.pan&&ae({x:a.x-gt.x,y:a.y-gt.y},0,bt,Pt);gt=a}else i.mouseDown(t,e)};this.clicked=function(e,o){if(!mt&&null===gt&&W()){var i=r.widgetCoordinates(n.canvas,o);t.emit(n.widget,\"seriesSelected\",i.x,i.y)}};this.mouseWheel=function(t,e){var i=(e.metaKey<<3)+(e.altKey<<2)+(e.ctrlKey<<1)+e.shiftKey,a=o.wheelActions[i];if(!O(a)){var u=r.widgetCoordinates(n.canvas,e),l=E(u.x,u.y),v=C(u.x,u.y),p=b(u,L());if(-1!==l||-1!==v||p){var x=r.normalizeWheel(e);if(p&&0===i&&q()){var y=tt(),h=-x.spinY;if(U(y)){var d=Y(y),w=c(d,Z(y)),T=m(w,I()),M=(T[0]+T[1])/2;r.cancelEvent(e);var g=Math.pow(1.2,h);s(F(y),f([1,0,0,g,0,M-g*M],F(y)));$t();return}}if(4!==a&&5!==a&&6!==a||!o.pan){if(o.zoom){r.cancelEvent(e);0===(h=-x.spinY)&&(h=-x.spinX);1===a?fe(u,0,h,l,v):0===a?fe(u,h,0,l,v):2===a?fe(u,h,h,l,v):3===a&&(0!==x.pixelX?fe(u,h,0,l,v):fe(u,0,h,l,v))}}else{for(var P=[],A=0;A<rt();++A)P.push(_(A)[4]);for(var D=[],S=0;S<at();++S)D.push(j(S)[5]);6===a?ae({x:-x.pixelX,y:-x.pixelY},0,l,v):5===a?ae({x:0,y:-x.pixelX-x.pixelY},0,l,v):4===a&&ae({x:-x.pixelX-x.pixelY,y:0},0,l,v);for(A=0;A<rt();++A)P[A]!==_(A)[4]&&r.cancelEvent(e);for(S=0;S<at();++S)D[S]!==j(S)[5]&&r.cancelEvent(e)}}}};var Vt=function(){W()&&t.emit(n.widget,\"seriesSelected\",gt.x,gt.y)};function te(){return wt||n.canvas}ht.start=function(t,e,i){Ct=1===it(e.touches);Ot=2===it(e.touches);if(Ct){Ft=!1;var a=r.widgetCoordinates(n.canvas,e.touches[0]),f=C(a.x,a.y),u=b(a,L()),s=E(a.x,a.y);if(-1===f&&-1===s&&!u)return;Ut=-1===f&&-1===s&&S()&&M(Tt,[a.x,a.y],30)?1:0;jt=Date.now();gt=a;Pt=f;bt=s;if(1!==Ut){!i&&u&&(_t=window.setTimeout(Vt,200));nt(\"contextmenu\",yt.contextmenuListener)}r.capture(null);r.capture(te())}else{if(!Ot||!o.zoom&&!q())return;if(_t){window.clearTimeout(_t);_t=null}Ft=!1;s=-1,f=-1;if(!(Et=[r.widgetCoordinates(n.canvas,e.touches[0]),r.widgetCoordinates(n.canvas,e.touches[1])].map((function(t){return[t.x,t.y]}))).every((function(t){return b(t,L())})))if(-1!==(s=E(Et[0][0],Et[0][1]))){if(s!==E(Et[1][0],Et[1][1])){Ot=null;return}}else{if(1===(f=C(Et[0][0],Et[0][1]))){Ot=null;return}if(f!==C(Et[1][0],Et[1][1])){Ot=null;return}}r.capture(null);r.capture(te());At=Math.atan2(Et[1][1]-Et[0][1],Et[1][0]-Et[0][0]);Dt=[(Et[0][0]+Et[1][0])/2,(Et[0][1]+Et[1][1])/2];var l=Math.abs(Math.sin(At)),c=Math.abs(Math.cos(At));At=l<Math.sin(.125*Math.PI)?0:c<Math.cos(.375*Math.PI)?Math.PI/2:Math.tan(At)>0?Math.PI/4:-Math.PI/4;St=T(At,Dt);bt=s;Pt=f}et(e)};function ee(t,e){if(Ft){var n=Date.now();O(e)&&(e=n-jt);var o,i={x:0,y:0};if(-1!==bt)o=Nt(bt,0);else if(-1===Pt){o=Nt(0,0);for(var r=1;r<rt();++r)o=h(o,Nt(r,0));for(var a=1;a<at();++a)o=h(o,Nt(0,a))}else o=Nt(0,Pt);var f,u=2e-4;if(e>34){Mt=!1;var s,l=Math.floor(e/17-1);for(s=0;s<l;++s){ee(t,17);if(!Ft){Mt=!0;$t();return}}e-=17*l;Mt=!0}It.x!==1/0&&It.x!==-1/0||(It.x>0?It.x=xt:It.x=-1.5);if(isFinite(It.x)){It.x=It.x/(1+.003*e);o[0]+=It.x*e;if(x(o)>x(R())){It.x=It.x+-u*(x(o)-x(R()))*e;It.x*=.7}else if(y(o)<y(R())){It.x=It.x+-u*(y(o)-y(R()))*e;It.x*=.7}Math.abs(It.x)<pt&&(x(o)>x(R())?It.x=pt:y(o)<y(R())&&(It.x=-.001));Math.abs(It.x)>xt&&(It.x=(It.x>0?1:-1)*xt);i.x=It.x*e}It.y!==1/0&&It.y!==-1/0||(It.y>0?It.y=xt:It.y=-1.5);if(isFinite(It.y)){It.y=It.y/(1+.003*e);o[1]+=It.y*e;if(v(o)>v(R())){It.y=It.y+-u*(v(o)-v(R()))*e;It.y*=.7}else if(p(o)<p(R())){It.y=It.y+-u*(p(o)-p(R()))*e;It.y*=.7}Math.abs(It.y)<.001&&(v(o)>v(R())?It.y=.001:p(o)<p(R())&&(It.y=-.001));Math.abs(It.y)>xt&&(It.y=(It.y>0?1:-1)*xt);i.y=It.y*e}if(-1!==bt)o=Nt(bt,0);else if(-1===Pt){o=Nt(0,0);for(r=1;r<rt();++r)o=h(o,Nt(r,0));for(a=1;a<at();++a)o=h(o,Nt(0,a))}else o=Nt(0,Pt);ae(i,1,bt,Pt);if(-1!==bt)f=Nt(bt,0);else if(-1===Pt){f=Nt(0,0);for(r=1;r<rt();++r)f=h(f,Nt(r,0));for(a=1;a<at();++a)f=h(f,Nt(0,a))}else f=Nt(0,Pt);if(x(o)>x(R())&&x(f)<=x(R())){It.x=0;ae({x:-i.x,y:0},1,bt,Pt);Jt(1)}if(y(o)<y(R())&&y(f)>=y(R())){It.x=0;ae({x:-i.x,y:0},1,bt,Pt);Jt(1)}if(v(o)>v(R())&&v(f)<=v(R())){It.y=0;ae({x:0,y:-i.y},1,bt,Pt);Jt(2)}if(p(o)<p(R())&&p(f)>=p(R())){It.y=0;ae({x:0,y:-i.y},1,bt,Pt);Jt(2)}if(Math.abs(It.x)<.02&&Math.abs(It.y)<.02&&function(t){return v(t)<=v(R())+3&&p(t)>=p(R())-3&&x(t)<=x(R())+3&&y(t)>=y(R())-3}(f)){Jt();Ft=!1;gt=null;It.x=0;It.y=0;jt=null;Et=[]}else{jt=n;Mt&&st(ee)}}}ht.end=function(t,e){if(_t){window.clearTimeout(_t);_t=null}window.setTimeout((function(){ot(\"contextmenu\",yt.contextmenuListener)}),0);var n=Array.prototype.slice.call(e.touches),r=0===it(n);r||function(){var t;for(t=0;t<it(e.changedTouches);++t)!function(){for(var o=e.changedTouches[t].identifier,i=0;i<it(n);++i)if(n[i].identifier===o){n.splice(i,1);return}}()}();r=0===it(n);Ct=1===it(n);Ot=2===it(n);if(r){ne=null;if(0===Ut&&(isFinite(It.x)||isFinite(It.y))&&o.rubberBand){jt=Date.now();Ft=!0;st(ee)}else{1===Ut&&i.mouseUp(null,null);n=[];At=null;Dt=null;St=null;jt=null}Ut=null}else(Ct||Ot)&&ht.start(t,e,!0)};var ne=null,oe=null,ie=null;ht.moved=function(t,e){if((Ct||Ot)&&(!Ct||null!=gt)){et(e);oe=r.widgetCoordinates(n.canvas,e.touches[0]);it(e.touches)>1&&(ie=r.widgetCoordinates(n.canvas,e.touches[1]));if(-1===bt&&-1===Pt&&Ct&&_t&&!M([oe.x,oe.y],[gt.x,gt.y],3)){window.clearTimeout(_t);_t=null}ne||(ne=setTimeout((function(){if(-1===bt&&-1===Pt&&Ct&&q()&&U(tt())){if(U(a=tt())){var t,e=oe;t=I()?(e.x-gt.x)/j(X(tt()))[3]:(e.y-gt.y)/j(X(tt()))[3];F(a)[5]+=t;gt=e;$t()}}else if(Ct){e=oe;var n=Date.now(),i={x:e.x-gt.x,y:e.y-gt.y},r=n-jt;jt=n;if(1===Ut){Tt[0]+=i.x;Tt[1]+=i.y;S()&&Mt&&st(Gt)}else if(o.pan){It.x=i.x/r;It.y=i.y/r;ae(i,o.rubberBand?2:0,bt,Pt)}gt=e}else if(-1===bt&&-1===Pt&&Ot&&q()&&U(tt())){var a,l=I()?0:1,c=[oe,ie].map((function(t){return I()?[t.x,y]:[m,t.y]})),v=Math.abs(Et[1][l]-Et[0][l]),p=Math.abs(c[1][l]-c[0][l]),x=v>0?p/v:1;p===v&&(x=1);if(U(a=tt())){var y=f(u(Bt(k(a),X(a))),[0,(Et[0][l]+Et[1][l])/2])[1],h=f(u(Bt(k(a),X(a))),[0,(c[0][l]+c[1][l])/2])[1];s(F(a),f([1,0,0,x,0,-x*y+h],F(a)));gt=e;$t();Et=c}}else if(Ot&&o.zoom){var d=qt(Tt,ft(),ut()),m=(Et[0][0]+Et[1][0])/2,w=(y=(Et[0][1]+Et[1][1])/2,c=[oe,ie].map((function(t){return 0===At?[t.x,y]:At===Math.PI/2?[m,t.y]:f(St,[t.x,t.y])})),Math.abs(Et[1][0]-Et[0][0])),T=Math.abs(c[1][0]-c[0][0]),M=w>0?T/w:1;T!==w&&At!==Math.PI/2||(M=1);var g=(c[0][0]+c[1][0])/2;v=Math.abs(Et[1][1]-Et[0][1]),p=Math.abs(c[1][1]-c[0][1]),x=v>0?p/v:1;p!==v&&0!==At||(x=1);h=(c[0][1]+c[1][1])/2;I()&&function(){var t=M;M=x;x=t;t=g;g=h;h=t;t=m;m=y;y=t}();for(var b=[],P=0;P<rt();++P)b.push(M);for(P=0;P<rt();++P){_(P)[0]*b[P]>G(P)&&(b[P]=G(P)/_(P)[0]);_(P)[0]*b[P]<K(P)&&(b[P]=K(P)/_(P)[0])}for(var E=[],C=0;C<at();++C)E.push(x);for(C=0;C<at();++C){j(C)[3]*E[C]>J(C)&&(E[C]=J(C)/j(C)[3]);j(C)[3]*E[C]<H(C)&&(E[C]=H(C)/j(C)[3])}if(-1!==bt)1!==b[bt]&&(b[bt]<1||_(bt)[0]!==G(bt))&&zt(_(bt),f([b[bt],0,0,1,-b[bt]*m+g,0],_(bt)));else if(-1===Pt){for(P=0;P<rt();++P)1!==b[P]&&(b[P]<1||_(P)[0]!==G(P))&&zt(_(P),f([b[P],0,0,1,-b[P]*m+g,0],_(P)));for(C=0;C<at();++C)1!==E[C]&&(E[C]<1||j(C)[3]!==J(C))&&zt(j(C),f([1,0,0,E[C],0,-E[C]*y+h],j(C)))}else 1!==E[Pt]&&(E[Pt]<1||j(Pt)[3]!==J(Pt))&&zt(j(Pt),f([1,0,0,E[Pt],0,-E[Pt]*y+h],j(Pt)));Jt();var O=Kt(d,ft(),ut());Tt[0]=O[0];Tt[1]=O[1];Et=c;re();$t();Ht()}ne=null}),1))}};function re(){for(var t,e,n=0;n<it(Q().x);++n){var o=g(_(n)[0])-1;_(n)[0]==G(n)&&(o=it(Q().x[n])-1);o>=it(Q().x[n])&&(o=it(Q().x[n])-1);for(t=0;t<it(Q().x[n]);++t)if(o===t)for(e=0;e<it(Q().x[n][t]);++e)Q().x[n][t][e].color[3]=V().x[n][e];else for(e=0;e<it(Q().x[n][t]);++e)Q().x[n][t][e].color[3]=0}for(var i=0;i<it(Q().y);++i){var r=g(j(i)[3])-1;j(i)[3]==J(i)&&(r=it(Q().y[i])-1);r>=it(Q().y[i])&&(r=it(Q().y[i])-1);for(t=0;t<it(Q().y[i]);++t)if(r===t)for(e=0;e<it(Q().y[i][t]);++e)Q().y[i][t][e].color[3]=V().y[i][e];else for(e=0;e<it(Q().y[i][t]);++e)Q().y[i][t][e].color[3]=0}}function ae(t,e,n,o){O(e)&&(e=0);O(n)&&(n=-1);O(o)&&(o=-1);var i=qt(Tt,ft(),ut());I()&&(t={x:t.y,y:-t.x});if(1&e){if(-1!==n)_(n)[4]=_(n)[4]+t.x;else if(-1===o){for(var r=0;r<rt();++r)_(r)[4]=_(r)[4]+t.x;for(var a=0;a<at();++a)j(a)[5]=j(a)[5]-t.y}else j(o)[5]=j(o)[5]-t.y;Wt()}else if(2&e){var f;if(-1!==n)f=Nt(n,0);else if(-1===o){f=Nt(0,0);for(r=1;r<rt();++r)f=h(f,Nt(r,0));for(a=1;a<at();++a)f=h(f,Nt(0,a))}else f=Nt(0,o);x(f)>x(R())?t.x>0&&(t.x=t.x/(1+(x(f)-x(R()))*vt)):y(f)<y(R())&&t.x<0&&(t.x=t.x/(1+(y(R())-y(f))*vt));v(f)>v(R())?t.y>0&&(t.y=t.y/(1+(v(f)-v(R()))*vt)):p(f)<p(R())&&t.y<0&&(t.y=t.y/(1+(p(R())-p(f))*vt));if(-1!==n)_(n)[4]=_(n)[4]+t.x;else if(-1===o){for(r=0;r<rt();++r)_(r)[4]=_(r)[4]+t.x;for(a=0;a<at();++a)j(a)[5]=j(a)[5]-t.y}else j(o)[5]=j(o)[5]-t.y;-1===o&&(Tt[0]=Tt[0]+t.x);-1===n&&(Tt[1]=Tt[1]+t.y);Wt()}else{if(-1!==n)_(n)[4]=_(n)[4]+t.x;else if(-1===o){for(r=0;r<rt();++r)_(r)[4]=_(r)[4]+t.x;for(a=0;a<at();++a)j(a)[5]=j(a)[5]-t.y}else j(o)[5]=j(o)[5]-t.y;-1===o&&(Tt[0]=Tt[0]+t.x);-1===n&&(Tt[1]=Tt[1]+t.y);Jt()}var u=Kt(i,ft(),ut());Tt[0]=u[0];Tt[1]=u[1];$t();Ht()}function fe(t,e,n,o,i){O(o)&&(o=-1);O(i)&&(i=-1);var r,a=qt(Tt,ft(),ut()),s=(r=I()?[t.y-v(L()),t.x-x(L())]:f(u([1,0,0,-1,x(L()),p(L())]),[t.x,t.y]))[0],l=r[1],c=Math.pow(1.2,I()?n:e),y=Math.pow(1.2,I()?e:n);if(-1!==o){_(o)[0]*c>G(o)&&(c=G(o)/_(o)[0]);(c<1||_(o)[0]!=G(o))&&zt(_(o),f([c,0,0,1,s-c*s,0],_(o)))}else if(-1===i){for(var h=0;h<rt();++h){var d=c;_(h)[0]*c>G(h)&&(d=G(h)/_(h)[0]);(d<1||_(h)[0]!==G(h))&&zt(_(h),f([d,0,0,1,s-d*s,0],_(h)))}for(var m=0;m<at();++m){var w=y;j(m)[3]*y>J(m)&&(w=J(m)/j(m)[3]);(w<1||j(m)[3]!==J(m))&&zt(j(m),f([1,0,0,w,0,l-w*l],j(m)))}}else{j(i)[3]*y>J(i)&&(y=J(i)/j(i)[3]);(y<1||j(i)[3]!=J(i))&&zt(j(i),f([1,0,0,y,0,l-y*l],j(i)))}Jt();var T=Kt(a,ft(),ut());Tt[0]=T[0];Tt[1]=T[1];re();$t();Ht()}this.setXRange=function(t,e,n,i){var r=A(k(t),0);e=r[0]+r[2]*e;n=r[0]+r[2]*n;if(x(r)>y(r)){e>x(r)&&(e=x(r));n<y(r)&&(n=y(r))}else{e<x(r)&&(e=x(r));n>y(r)&&(n=y(r))}var a=Z(t),f=P(a,X(t),e,n,I(),L(),A(k(t),X(t)),o.minZoom,o.maxZoom),u=f.xZoom,s=f.yZoom,l=f.panPoint,c=qt(Tt,ft(),ut());_(k(t))[0]=u;s&&i&&(j(X(t))[3]=s);_(k(t))[4]=-l[0]*u;s&&i&&(j(X(t))[5]=-l[1]*s);Wt();var v=Kt(c,ft(),ut());Tt[0]=v[0];Tt[1]=v[1];Jt();re();$t();Ht()};this.getSeries=function(t){return Z(t)};this.rangeChangedCallbacks=[];this.updateConfig=function(t){for(var i in t)t.hasOwnProperty(i)&&(o[i]=t[i]);!function(){if(S()&&(O(wt)||n.canvas.width!==wt.width||n.canvas.height!==wt.height)){if(wt){wt.parentNode.removeChild(wt);delete e.wtOObj;wt=void 0}var t=document.createElement(\"canvas\");t.setAttribute(\"width\",n.canvas.width);t.setAttribute(\"height\",n.canvas.height);t.style.position=\"absolute\";t.style.display=\"block\";t.style.left=\"0\";t.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){t.style.msTouchAction=\"none\";t.style.touchAction=\"none\"}n.canvas.parentNode.appendChild(t);wt=t;e.wtOObj=wt}else if(!O(wt)&&!S()){wt.parentNode.removeChild(wt);delete e.wtOObj;wt=void 0}Tt=[(x(L())+y(L()))/2,(v(L())+p(L()))/2]}();re();$t();Ht()};this.updateConfig({});if(!window.TouchEvent||window.MSPointerEvent||window.PointerEvent){var ue=function(){};i.touchStart=ue;i.touchEnd=ue;i.touchMoved=ue}else{i.touchStart=ht.start;i.touchEnd=ht.end;i.touchMoved=ht.moved}})");
  }

  static String locToJsString(AxisValue loc) {
    switch (loc) {
      case Minimum:
        return "min";
      case Maximum:
        return "max";
      case Zero:
        return "zero";
      case Both:
        return "both";
    }
    assert false;
    return "";
  }

  static int binarySearchRow(
      final WAbstractChartModel model, int xColumn, double d, int minRow, int maxRow) {
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
        return binarySearchRow(model, xColumn, d, minRow, (int) start - 1);
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
