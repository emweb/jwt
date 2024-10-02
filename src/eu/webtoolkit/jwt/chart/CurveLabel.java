/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
   * they may be safer to use. </i>
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
        "(function(t){const n=t.WT,e=this,r=n.gfxUtils,i=r.rect_top,o=r.rect_bottom,f=r.rect_left,u=r.rect_right,s=r.transform_mult;function l(t,n,e,r){let i=0;r&&(i=1);const o=n.length;function f(t){return e?n[t]:n[o-1-t]}function u(t){for(;2===f(t)[2]||3===f(t)[2];)t--;return t}let s=Math.floor(o/2);s=u(s);let l=0,h=o,c=!1;if(f(0)[i]>t)return e?-1:o;if(f(o-1)[i]<t)return e?o:-1;for(;!c;){let n=s+1;n<o&&(2===f(n)[2]||3===f(n)[2])&&(n+=2);if(f(s)[i]>t){h=s;s=Math.floor((h+l)/2);s=u(s)}else if(f(s)[i]===t)c=!0;else if(n<o&&f(n)[i]>t)c=!0;else if(n<o&&f(n)[i]===t){s=n;c=!0}else{l=s;s=Math.floor((h+l)/2);s=u(s)}}return e?s:o-1-s}function h(t,n){return n[0][t]<n[n.length-1][t]}this.findClosestPoint=function(t,n,e){let r=0;e&&(r=1);const i=h(r,n);let o=l(t,n,i,e);o<0&&(o=0);if(o>=n.length)return[n[n.length-1][0],n[n.length-1][1]];o>=n.length&&(o=n.length-2);if(n[o][r]===t)return[n[o][0],n[o][1]];let f=i?o+1:o-1;i&&2===n[f][2]&&(f+=2);if(!i&&f<0)return[n[o][0],n[o][1]];!i&&f>0&&3===n[f][2]&&(f-=2);return Math.abs(t-n[o][r])<Math.abs(n[f][r]-t)?[n[o][0],n[o][1]]:[n[f][0],n[f][1]]};this.minMaxY=function(t,n){const e=n?0:1;let r=t[0][e],i=t[0][e];for(let n=1;n<t.length;++n)if(2!==t[n][2]&&3!==t[n][2]&&5!==t[n][2]){t[n][e]>i&&(i=t[n][e]);t[n][e]<r&&(r=t[n][e])}return[r,i]};this.projection=function(t,n){const e=Math.cos(t),r=Math.sin(t),i=e*e,o=r*r,f=e*r,u=-n[0]*e-n[1]*r;return[i,f,f,o,e*u+n[0],r*u+n[1]]};this.distanceSquared=function(t,n){const e=[n[0]-t[0],n[1]-t[1]];return e[0]*e[0]+e[1]*e[1]};this.distanceLessThanRadius=function(t,n,r){return r*r>=e.distanceSquared(t,n)};this.toZoomLevel=function(t){return Math.floor(Math.log(t)/Math.LN2+.5)+1};this.isPointInRect=function(t,n){const e=t.x??t[0],r=t.y??t[1];return e>=f(n)&&e<=u(n)&&r>=i(n)&&r<=o(n)};this.toDisplayCoord=function(t,n,e,r,i){let o,f;if(e){o=[(t[0]-i[0])/i[2],(t[1]-i[1])/i[3]];f=[r[0]+o[1]*r[2],r[1]+o[0]*r[3]]}else{o=[(t[0]-i[0])/i[2],1-(t[1]-i[1])/i[3]];f=[r[0]+o[0]*r[2],r[1]+o[1]*r[3]]}return s(n,f)};this.findYRange=function(t,n,r,u,s,c,a,m,g){if(0===t.length)return null;const d=e.toDisplayCoord([r,0],[1,0,0,1,0,0],s,c,a),M=e.toDisplayCoord([u,0],[1,0,0,1,0,0],s,c,a),x=s?1:0,b=s?0:1,y=h(x,t);let C,_,p,R,D,E=l(d[x],t,y,s),L=l(M[x],t,y,s),O=1/0,T=-1/0;const A=E===L&&E===t.length||-1===E&&-1===L;if(!A){if(y)if(E<0)E=0;else{E++;t[E]&&2===t[E][2]&&(E+=2)}else E>=t.length-1&&(E=t.length-2);!y&&L<0&&(L=0);for(C=Math.min(E,L);C<=Math.max(E,L)&&C<t.length;++C)if(2!==t[C][2]&&3!==t[C][2]){t[C][b]<O&&(O=t[C][b]);t[C][b]>T&&(T=t[C][b])}if(y&&E>0||!y&&E<t.length-1){if(y){R=E-1;t[R]&&3===t[R][2]&&(R-=2)}else{R=E+1;t[R]&&2===t[R][2]&&(R+=2)}_=(d[x]-t[R][x])/(t[E][x]-t[R][x]);p=t[R][b]+_*(t[E][b]-t[R][b]);p<O&&(O=p);p>T&&(T=p)}if(y&&L<t.length-1||!y&&L>0){if(y){D=L+1;2===t[D][2]&&(D+=2)}else{D=L-1;3===t[D][2]&&(D-=2)}_=(M[x]-t[L][x])/(t[D][x]-t[L][x]);p=t[L][b]+_*(t[D][b]-t[L][b]);p<O&&(O=p);p>T&&(T=p)}}let P,S;const W=a[2]/(u-r),Y=s?2:3;if(!A){P=c[Y]/(T-O);S=10;P=c[Y]/(c[Y]/P+20);P>g.y[n]&&(P=g.y[n]);P<m.y[n]&&(P=m.y[n])}let Z;Z=s?[d[1]-i(c),A?0:(O+T)/2-c[2]/P/2-f(c)]:[d[0]-f(c),A?0:-((O+T)/2+c[3]/P/2-o(c))];return{xZoom:W,yZoom:P,panPoint:Z}};this.matchXAxis=function(t,n,e,r,s){function l(t){return r[t].side}function h(t){return r[t].width}function c(t){return r[t].minOffset}function a(t){return r[t].maxOffset}if(s){if(n<i(e)||n>o(e))return-1}else if(t<f(e)||t>u(e))return-1;for(let m=0;m<r.length;++m)if(s){if((\"min\"===l(m)||\"both\"===l(m))&&t>=f(e)-c(m)-h(m)&&t<=f(e)-c(m))return m;if((\"max\"===l(m)||\"both\"===l(m))&&t>=u(e)+a(m)&&t<=u(e)+a(m)+h(m))return m}else{if((\"min\"===l(m)||\"both\"===l(m))&&n<=o(e)+c(m)+h(m)&&n>=o(e)+c(m))return m;if((\"max\"===l(m)||\"both\"===l(m))&&n<=i(e)-a(m)&&n>=i(e)-a(m)-h(m))return m}return-1};this.matchYAxis=function(t,n,e,r,s){function l(t){return r[t].side}function h(t){return r[t].width}function c(t){return r[t].minOffset}function a(t){return r[t].maxOffset}if(s){if(t<f(e)||t>u(e))return-1}else if(n<i(e)||n>o(e))return-1;for(let m=0;m<r.length;++m)if(s){if((\"min\"===l(m)||\"both\"===l(m))&&n>=i(e)-c(m)-h(m)&&n<=i(e)-c(m))return m;if((\"max\"===l(m)||\"both\"===l(m))&&n>=o(e)+a(m)&&n<=o(e)+a(m)+h(m))return m}else{if((\"min\"===l(m)||\"both\"===l(m))&&t>=f(e)-c(m)-h(m)&&t<=f(e)-c(m))return m;if((\"max\"===l(m)||\"both\"===l(m))&&t>=u(e)+a(m)&&t<=u(e)+a(m)+h(m))return m}return-1}})");
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WCartesianChart",
        "(function(t,e,n,o){e.wtCObj=this;const i=this,r=t.WT;i.config=o;const l=r.gfxUtils,s=l.transform_mult,u=l.transform_inverted,f=l.transform_assign,c=l.transform_equal,a=l.transform_apply,x=l.rect_top,p=l.rect_bottom,y=l.rect_left,h=l.rect_right,d=l.rect_intersection,m=r.chartCommon,v=m.minMaxY,w=m.findClosestPoint,T=m.projection,M=m.distanceLessThanRadius,g=m.toZoomLevel,b=m.isPointInRect,E=m.findYRange,P=function(t,e){return m.matchXAxis(t,e,L(),o.xAxes,S())},C=function(t,e){return m.matchYAxis(t,e,L(),o.yAxes,S())};function A(t,e){const n=(i=t,o.xModelAreas[i]);var i;const r=function(t){return o.yModelAreas[t]}(e);return S()?[r[0],n[1],r[2],n[3]]:[n[0],r[1],n[2],r[3]]}function O(){return o.followCurve}function D(){return o.crosshair||-1!==O()}function S(){return o.isHorizontal}function j(t){return o.xTransforms[t]}function I(t){return o.yTransforms[t]}function L(){return o.area}function _(){return o.insideArea}function F(t=null){return null===t?o.series:o.series[t]}function U(t){return F(t).transform}function Y(t){return S()?s([0,1,1,0,0,0],s(U(t),[0,1,1,0,0,0])):U(t)}function Z(t){return F(t).curve}function R(t){return F(t).xAxis}function X(t){return F(t).yAxis}function k(){return o.seriesSelection}function W(){return o.sliders}function q(){return o.hasToolTips}function z(){return o.coordinateOverlayPadding}function B(){return o.curveManipulation}function N(t){return o.minZoom.x[t]}function K(t){return o.minZoom.y[t]}function H(t){return o.maxZoom.x[t]}function G(t){return o.maxZoom.y[t]}function J(){return o.pens}function $(){return o.penAlpha}function Q(){return o.selectedCurve}function V(t){t.preventDefault&&t.preventDefault()}function tt(t,n){e.addEventListener(t,n)}function et(t,n){e.removeEventListener(t,n)}function nt(t){return t.length}function ot(){return nt(o.xAxes)}function it(){return nt(o.yAxes)}function rt(){return o.crosshairXAxis}function lt(){return o.crosshairYAxis}let st=!1;const ut=function(t){if(!st){st=!0;requestAnimationFrame((function(){t();st=!1}))}};if(window.MSPointerEvent||window.PointerEvent){e.style.touchAction=\"none\";n.canvas.style.msTouchAction=\"none\";n.canvas.style.touchAction=\"none\"}const ft=.07,ct=.001,at=1.5;let xt=e.wtEObj2;if(!xt){xt={};xt.contextmenuListener=function(t){V(t);et(\"contextmenu\",xt.contextmenuListener)}}e.wtEObj2=xt;const pt={};function yt(t){return 2===t.pointerType||3===t.pointerType||\"pen\"===t.pointerType||\"touch\"===t.pointerType}let ht=!1;(window.MSPointerEvent||window.PointerEvent)&&function(){const t=[];function n(){ht=nt(t)>0}function o(o){if(yt(o)){V(o);t.push(o);n();pt.start(e,{touches:t.slice(0)})}}function i(o){if(ht&&yt(o)){V(o);for(let e=0;e<nt(t);++e)if(t[e].pointerId===o.pointerId){t.splice(e,1);break}n();pt.end(e,{touches:t.slice(0),changedTouches:[]})}}function r(o){if(yt(o)){V(o);for(let e=0;e<nt(t);++e)if(t[e].pointerId===o.pointerId){t[e]=o;break}n();pt.moved(e,{touches:t.slice(0)})}}const l=e.wtEObj;if(l)if(window.PointerEvent){et(\"pointerdown\",l.pointerDown);et(\"pointerup\",l.pointerUp);et(\"pointerout\",l.pointerUp);et(\"pointermove\",l.pointerMove)}else{et(\"MSPointerDown\",l.pointerDown);et(\"MSPointerUp\",l.pointerUp);et(\"MSPointerOut\",l.pointerUp);et(\"MSPointerMove\",l.pointerMove)}e.wtEObj={pointerDown:o,pointerUp:i,pointerMove:r};if(window.PointerEvent){tt(\"pointerdown\",o);tt(\"pointerup\",i);tt(\"pointerout\",i);tt(\"pointermove\",r)}else{tt(\"MSPointerDown\",o);tt(\"MSPointerUp\",i);tt(\"MSPointerOut\",i);tt(\"MSPointerMove\",r)}}();let dt=e.wtOObj??null,mt=null,vt=!0,wt=null,Tt=-1,Mt=-1,gt=[],bt=!1,Et=!1,Pt=null,Ct=null,At=null;const Ot={x:0,y:0};let Dt=null,St=null,jt=e.wtTObj;if(!jt){jt={overTooltip:!1};e.wtTObj=jt}function It(){if(jt){if(jt.tooltipTimeout){clearTimeout(jt.tooltipTimeout);jt.tooltipTimeout=null}if(!jt.overTooltip&&jt.tooltipOuterDiv){document.body.removeChild(jt.tooltipOuterDiv);jt.toolTipEl=null;jt.tooltipOuterDiv=null}}}let Lt=null,_t=!1,Ft=null;const Ut=[];for(let t=0;t<ot();++t){Ut.push([0,0,0,0,0,0]);f(Ut[t],j(t))}const Yt=[];for(let t=0;t<it();++t){Yt.push([0,0,0,0,0,0]);f(Yt[t],I(t))}function Zt(){if(function(){for(let t=0;t<ot();++t)if(o.notifyTransform.x[t])return!0;for(let t=0;t<it();++t)if(o.notifyTransform.y[t])return!0;return!1}()){if(Ft){window.clearTimeout(Ft);Ft=null}Ft=setTimeout((function(){for(let e=0;e<ot();++e)if(o.notifyTransform.x[e]&&!c(Ut[e],j(e))){t.emit(n.widget,\"xTransformChanged\"+e);f(Ut[e],j(e))}for(let e=0;e<it();++e)if(o.notifyTransform.y[e]&&!c(Yt[e],I(e))){t.emit(n.widget,\"yTransformChanged\"+e);f(Yt[e],I(e))}}),250)}}const Rt=function(t,e){f(t,e);Zt()};function Xt(t=0,e=0){if(S()){const n=y(L()),o=x(L());return s([0,1,1,0,n,o],s(j(t),s(I(e),[0,1,1,0,-o,-n])))}{const n=y(L()),o=p(L());return s([1,0,0,-1,n,o],s(j(t),s(I(e),[1,0,0,-1,-n,o])))}}n.combinedTransform=Xt;function kt(t,e){return s(Xt(t,e),_())}function Wt(t,e,n,o=!1){let i,r;i=o?t:s(u(Xt(e,n)),t);r=S()?[(i[1]-L()[1])/L()[3],(i[0]-L()[0])/L()[2]]:[(i[0]-L()[0])/L()[2],1-(i[1]-L()[1])/L()[3]];return[A(e,n)[0]+r[0]*A(e,n)[2],A(e,n)[1]+r[1]*A(e,n)[3]]}function qt(t,e,n,o=!1){return m.toDisplayCoord(t,o?[1,0,0,1,0,0]:Xt(e,n),S(),L(),A(e,n))}function zt(){for(let t=0;t<ot();++t){let e,n;const o=A(t,0);if(S()){e=(Wt([0,x(L())],t,0)[0]-o[0])/o[2];n=(Wt([0,p(L())],t,0)[0]-o[0])/o[2]}else{e=(Wt([y(L()),0],t,0)[0]-o[0])/o[2];n=(Wt([h(L()),0],t,0)[0]-o[0])/o[2]}for(let o=0;o<nt(W());++o){const i=r.$(W()[o]);if(i){const o=i.wtSObj;o&&o.xAxis()===t&&o.changeRange(e,n)}}}}function Bt(){It();q()&&jt.tooltipPosition&&(jt.tooltipTimeout=setTimeout((function(){Ht()}),500));vt&&ut((function(){n.repaint();D()&&Nt()}))}function Nt(){if(!vt)return;const t=dt.getContext(\"2d\");t.clearRect(0,0,dt.width,dt.height);t.save();t.beginPath();t.moveTo(y(L()),x(L()));t.lineTo(h(L()),x(L()));t.lineTo(h(L()),p(L()));t.lineTo(y(L()),p(L()));t.closePath();t.clip();let e,n=s(u(Xt(rt(),lt())),mt),i=mt[0],r=mt[1];if(-1!==O()){n=w(S()?n[1]:n[0],Z(O()),S());const t=s(Xt(R(O()),X(O())),s(Y(O()),n));i=t[0];r=t[1];mt[0]=i;mt[1]=r}e=S()?[(n[1]-L()[1])/L()[3],(n[0]-L()[0])/L()[2]]:[(n[0]-L()[0])/L()[2],1-(n[1]-L()[1])/L()[3]];if(-1!==O()){const t=A(R(O()),X(O()));n=[t[0]+e[0]*t[2],t[1]+e[1]*t[3]]}else{const t=A(rt(),lt());n=[t[0]+e[0]*t[2],t[1]+e[1]*t[3]]}t.fillStyle=t.strokeStyle=o.crosshairColor;t.font=\"16px sans-serif\";t.textAlign=\"right\";t.textBaseline=\"top\";let l=n[0].toFixed(2),f=n[1].toFixed(2);\"-0.00\"===l&&(l=\"0.00\");\"-0.00\"===f&&(f=\"0.00\");t.fillText(\"(\"+l+\",\"+f+\")\",h(L())-z()[0],x(L())+z()[1]);t.setLineDash&&t.setLineDash([1,2]);t.beginPath();t.moveTo(Math.floor(i)+.5,Math.floor(x(L()))+.5);t.lineTo(Math.floor(i)+.5,Math.floor(p(L()))+.5);t.moveTo(Math.floor(y(L()))+.5,Math.floor(r)+.5);t.lineTo(Math.floor(h(L()))+.5,Math.floor(r)+.5);t.stroke();t.restore()}function Kt(t=null){let e;S()&&(1===t?t=2:2===t&&(t=1));for(let n=0;n<ot();++n){let o=kt(n,0);if(S()){if(null===t||2===t){if(j(n)[0]<1){j(n)[0]=1;o=kt(n,0)}if(x(o)>x(_())){e=x(_())-x(o);j(n)[4]=j(n)[4]+e}else if(p(o)<p(_())){e=p(_())-p(o);j(n)[4]=j(n)[4]+e}}}else if(null===t||1===t){if(j(n)[0]<1){j(n)[0]=1;o=kt(n,0)}if(y(o)>y(_())){e=y(_())-y(o);j(n)[4]=j(n)[4]+e}else if(h(o)<h(_())){e=h(_())-h(o);j(n)[4]=j(n)[4]+e}}}for(let n=0;n<it();++n){let o=kt(0,n);if(S()){if(null===t||1===t){if(I(n)[3]<1){I(n)[3]=1;o=kt(0,n)}if(y(o)>y(_())){e=y(_())-y(o);I(n)[5]=I(n)[5]+e}else if(h(o)<h(_())){e=h(_())-h(o);I(n)[5]=I(n)[5]+e}}}else if(null===t||2===t){if(I(n)[3]<1){I(n)[3]=1;o=kt(0,n)}if(x(o)>x(_())){e=x(_())-x(o);I(n)[5]=I(n)[5]-e}else if(p(o)<p(_())){e=p(_())-p(o);I(n)[5]=I(n)[5]-e}}}Zt()}function Ht(){jt.toolTipEl||t.emit(n.widget,\"loadTooltip\",jt.tooltipPosition[0],jt.tooltipPosition[1])}this.updateTooltip=function(t){It();if(t){if(!jt.tooltipPosition)return;jt.toolTipEl=document.createElement(\"div\");jt.toolTipEl.className=o.ToolTipInnerStyle;jt.toolTipEl.innerHTML=t;jt.tooltipOuterDiv=document.createElement(\"div\");jt.tooltipOuterDiv.className=o.ToolTipOuterStyle;document.body.appendChild(jt.tooltipOuterDiv);jt.tooltipOuterDiv.appendChild(jt.toolTipEl);const e=r.widgetPageCoordinates(n.canvas),i=jt.tooltipPosition[0]+e.x,l=jt.tooltipPosition[1]+e.y;r.fitToWindow(jt.tooltipOuterDiv,i+10,l+10,i-10,l-10);jt.toolTipEl.addEventListener(\"mouseenter\",(function(){jt.overTooltip=!0}));jt.toolTipEl.addEventListener(\"mouseleave\",(function(){jt.overTooltip=!1}))}};this.mouseMove=function(t,e){setTimeout((function(){setTimeout(It,200);if(ht)return;const t=r.widgetCoordinates(n.canvas,e);if(b(t,L())){if(q()){jt.tooltipPosition=[t.x,t.y];jt.tooltipTimeout=setTimeout((function(){Ht()}),500)}if(null===wt&&D()&&vt){mt=[t.x,t.y];ut(Nt)}}}),0)};this.mouseOut=function(t,e){setTimeout(It,200)};this.mouseDown=function(t,e){if(ht)return;const o=r.widgetCoordinates(n.canvas,e),i=C(o.x,o.y),l=b(o,L()),s=P(o.x,o.y);if(-1!==i||-1!==s||l){wt=o;Tt=s;Mt=i}};this.mouseUp=function(t,e){if(!ht){wt=null;Tt=-1;Mt=-1}};this.mouseDrag=function(t,e){if(ht)return;if(null===wt){i.mouseDown(t,e);return}const l=r.widgetCoordinates(n.canvas,e);if(1===r.buttons)if(-1===Mt&&-1===Tt&&B()&&F(Q())){const t=Q();let e;e=S()?l.x-wt.x:l.y-wt.y;f(U(t),s([1,0,0,1,0,e/I(X(Q()))[3]],U(t)));Bt()}else o.pan&&ne({x:l.x-wt.x,y:l.y-wt.y},0,Tt,Mt);wt=l};this.clicked=function(e,o){if(ht)return;if(null!==wt)return;if(!k())return;const i=r.widgetCoordinates(n.canvas,o);t.emit(n.widget,\"seriesSelected\",i.x,i.y)};this.mouseWheel=function(t,e){const i=(e.metaKey<<3)+(e.altKey<<2)+(e.ctrlKey<<1)+e.shiftKey,l=o.wheelActions[i];if(void 0===l)return;const u=r.widgetCoordinates(n.canvas,e),c=P(u.x,u.y),x=C(u.x,u.y),p=b(u,L());if(-1===c&&-1===x&&!p)return;const y=r.normalizeWheel(e);if(p&&0===i&&B()){const t=Q(),n=-y.spinY;if(F(t)){const o=Y(t),i=a(o,Z(t)),l=v(i,S()),u=(l[0]+l[1])/2;r.cancelEvent(e);const c=Math.pow(1.2,n);f(U(t),s([1,0,0,c,0,u-c*u],U(t)));Bt();return}}if(4!==l&&5!==l&&6!==l||!o.pan){if(o.zoom){r.cancelEvent(e);let t=-y.spinY;0===t&&(t=-y.spinX);1===l?oe(u,0,t,c,x):0===l?oe(u,t,0,c,x):2===l?oe(u,t,t,c,x):3===l&&(0!==y.pixelX?oe(u,t,0,c,x):oe(u,0,t,c,x))}}else{const t=[];for(let e=0;e<ot();++e)t.push(j(e)[4]);const n=[];for(let t=0;t<it();++t)n.push(I(t)[5]);6===l?ne({x:-y.pixelX,y:-y.pixelY},0,c,x):5===l?ne({x:0,y:-y.pixelX-y.pixelY},0,c,x):4===l&&ne({x:-y.pixelX-y.pixelY,y:0},0,c,x);for(let n=0;n<ot();++n)t[n]!==j(n)[4]&&r.cancelEvent(e);for(let t=0;t<it();++t)n[t]!==I(t)[5]&&r.cancelEvent(e)}};const Gt=function(){k()&&t.emit(n.widget,\"seriesSelected\",wt.x,wt.y)};function Jt(){return dt||n.canvas}pt.start=function(t,e,i){bt=1===nt(e.touches);Et=2===nt(e.touches);if(bt){_t=!1;const t=r.widgetCoordinates(n.canvas,e.touches[0]),o=C(t.x,t.y),l=b(t,L()),s=P(t.x,t.y);if(-1===o&&-1===s&&!l)return;Lt=-1===o&&-1===s&&D()&&M(mt,[t.x,t.y],30)?1:0;St=Date.now();wt=t;Mt=o;Tt=s;if(1!==Lt){!i&&l&&(Dt=window.setTimeout(Gt,200));tt(\"contextmenu\",xt.contextmenuListener)}r.capture(null);r.capture(Jt())}else{if(!Et||!o.zoom&&!B())return;{if(Dt){window.clearTimeout(Dt);Dt=null}_t=!1;gt=[r.widgetCoordinates(n.canvas,e.touches[0]),r.widgetCoordinates(n.canvas,e.touches[1])].map((function(t){return[t.x,t.y]}));let t=-1,o=-1;if(!gt.every((function(t){return b(t,L())}))){t=P(gt[0][0],gt[0][1]);if(-1!==t){if(t!==P(gt[1][0],gt[1][1])){Et=null;return}}else{o=C(gt[0][0],gt[0][1]);if(1===o){Et=null;return}if(o!==C(gt[1][0],gt[1][1])){Et=null;return}}}r.capture(null);r.capture(Jt());Pt=Math.atan2(gt[1][1]-gt[0][1],gt[1][0]-gt[0][0]);Ct=[(gt[0][0]+gt[1][0])/2,(gt[0][1]+gt[1][1])/2];const i=Math.abs(Math.sin(Pt)),l=Math.abs(Math.cos(Pt));Pt=i<Math.sin(.125*Math.PI)?0:l<Math.cos(.375*Math.PI)?Math.PI/2:Math.tan(Pt)>0?Math.PI/4:-Math.PI/4;At=T(Pt,Ct);Tt=t;Mt=o}}V(e)};function $t(t,e=null){if(!_t)return;const n=Date.now();e=e??n-St;const o={x:0,y:0};let i;if(-1!==Tt)i=kt(Tt,0);else if(-1===Mt){i=kt(0,0);for(let t=1;t<ot();++t)i=d(i,kt(t,0));for(let t=1;t<it();++t)i=d(i,kt(0,t))}else i=kt(0,Mt);const r=2e-4;if(e>34){vt=!1;const n=Math.floor(e/17-1);for(let e=0;e<n;++e){$t(t,17);if(!_t){vt=!0;Bt();return}}e-=17*n;vt=!0}Ot.x!==1/0&&Ot.x!==-1/0||(Ot.x>0?Ot.x=at:Ot.x=-1.5);if(isFinite(Ot.x)){Ot.x=Ot.x/(1+.003*e);i[0]+=Ot.x*e;if(y(i)>y(_())){Ot.x=Ot.x+-r*(y(i)-y(_()))*e;Ot.x*=.7}else if(h(i)<h(_())){Ot.x=Ot.x+-r*(h(i)-h(_()))*e;Ot.x*=.7}Math.abs(Ot.x)<ct&&(y(i)>y(_())?Ot.x=ct:h(i)<h(_())&&(Ot.x=-.001));Math.abs(Ot.x)>at&&(Ot.x=(Ot.x>0?1:-1)*at);o.x=Ot.x*e}Ot.y!==1/0&&Ot.y!==-1/0||(Ot.y>0?Ot.y=at:Ot.y=-1.5);if(isFinite(Ot.y)){Ot.y=Ot.y/(1+.003*e);i[1]+=Ot.y*e;if(x(i)>x(_())){Ot.y=Ot.y+-r*(x(i)-x(_()))*e;Ot.y*=.7}else if(p(i)<p(_())){Ot.y=Ot.y+-r*(p(i)-p(_()))*e;Ot.y*=.7}Math.abs(Ot.y)<.001&&(x(i)>x(_())?Ot.y=.001:p(i)<p(_())&&(Ot.y=-.001));Math.abs(Ot.y)>at&&(Ot.y=(Ot.y>0?1:-1)*at);o.y=Ot.y*e}if(-1!==Tt)i=kt(Tt,0);else if(-1===Mt){i=kt(0,0);for(let t=1;t<ot();++t)i=d(i,kt(t,0));for(let t=1;t<it();++t)i=d(i,kt(0,t))}else i=kt(0,Mt);ne(o,1,Tt,Mt);let l;if(-1!==Tt)l=kt(Tt,0);else if(-1===Mt){l=kt(0,0);for(let t=1;t<ot();++t)l=d(l,kt(t,0));for(let t=1;t<it();++t)l=d(l,kt(0,t))}else l=kt(0,Mt);if(y(i)>y(_())&&y(l)<=y(_())){Ot.x=0;ne({x:-o.x,y:0},1,Tt,Mt);Kt(1)}if(h(i)<h(_())&&h(l)>=h(_())){Ot.x=0;ne({x:-o.x,y:0},1,Tt,Mt);Kt(1)}if(x(i)>x(_())&&x(l)<=x(_())){Ot.y=0;ne({x:0,y:-o.y},1,Tt,Mt);Kt(2)}if(p(i)<p(_())&&p(l)>=p(_())){Ot.y=0;ne({x:0,y:-o.y},1,Tt,Mt);Kt(2)}if(Math.abs(Ot.x)<.02&&Math.abs(Ot.y)<.02&&function(t){return x(t)<=x(_())+3&&p(t)>=p(_())-3&&y(t)<=y(_())+3&&h(t)>=h(_())-3}(l)){Kt();_t=!1;wt=null;Ot.x=0;Ot.y=0;St=null;gt=[]}else{St=n;vt&&requestAnimationFrame($t)}}pt.end=function(t,e){if(Dt){window.clearTimeout(Dt);Dt=null}window.setTimeout((function(){et(\"contextmenu\",xt.contextmenuListener)}),0);let n=Array.prototype.slice.call(e.touches),r=0===nt(n);if(!r)for(let t=0;t<nt(e.changedTouches);++t){const o=e.changedTouches[t].identifier;for(let t=0;t<nt(n);++t)if(n[t].identifier===o){n.splice(t,1);return}}r=0===nt(n);bt=1===nt(n);Et=2===nt(n);if(r){Qt=null;if(0===Lt&&(isFinite(Ot.x)||isFinite(Ot.y))&&o.rubberBand){St=Date.now();_t=!0;requestAnimationFrame($t)}else{1===Lt&&i.mouseUp(null,null);n=[];Pt=null;Ct=null;At=null;St=null}Lt=null}else(bt||Et)&&pt.start(t,e,!0)};let Qt=null,Vt=null,te=null;pt.moved=function(t,e){if((bt||Et)&&(!bt||null!==wt)){V(e);Vt=r.widgetCoordinates(n.canvas,e.touches[0]);nt(e.touches)>1&&(te=r.widgetCoordinates(n.canvas,e.touches[1]));if(-1===Tt&&-1===Mt&&bt&&Dt&&!M([Vt.x,Vt.y],[wt.x,wt.y],3)){window.clearTimeout(Dt);Dt=null}Qt||(Qt=setTimeout((function(){if(-1===Tt&&-1===Mt&&bt&&B()&&F(Q())){const t=Q();if(F(t)){const e=Vt;let n;n=S()?(e.x-wt.x)/I(X(Q()))[3]:(e.y-wt.y)/I(X(Q()))[3];U(t)[5]+=n;wt=e;Bt()}}else if(bt){const t=Vt,e=Date.now(),n={x:t.x-wt.x,y:t.y-wt.y},i=e-St;St=e;if(1===Lt){mt[0]+=n.x;mt[1]+=n.y;D()&&vt&&requestAnimationFrame(Nt)}else if(o.pan){Ot.x=n.x/i;Ot.y=n.y/i;ne(n,o.rubberBand?2:0,Tt,Mt)}wt=t}else if(-1===Tt&&-1===Mt&&Et&&B()&&F(Q())){const t=Q();if(F(t)){const e=S()?0:1,n=[Vt,te].map((function(t){return[t.x,t.y]})),o=Math.abs(gt[1][e]-gt[0][e]),i=Math.abs(n[1][e]-n[0][e]);let r=o>0?i/o:1;i===o&&(r=1);const l=s(u(Xt(R(t),X(t))),[0,(gt[0][e]+gt[1][e])/2])[1],c=s(u(Xt(R(t),X(t))),[0,(n[0][e]+n[1][e])/2])[1];f(U(t),s([1,0,0,r,0,-r*l+c],U(t)));Bt();wt=null;gt=n}}else if(Et&&o.zoom){const t=Wt(mt,rt(),lt());let e=(gt[0][0]+gt[1][0])/2,n=(gt[0][1]+gt[1][1])/2;const o=[Vt,te].map((function(t){return 0===Pt?[t.x,n]:Pt===Math.PI/2?[e,t.y]:s(At,[t.x,t.y])})),i=Math.abs(gt[1][0]-gt[0][0]),r=Math.abs(o[1][0]-o[0][0]);let l=i>0?r/i:1;r!==i&&Pt!==Math.PI/2||(l=1);let u=(o[0][0]+o[1][0])/2;const f=Math.abs(gt[1][1]-gt[0][1]),c=Math.abs(o[1][1]-o[0][1]);let a=f>0?c/f:1;c!==f&&0!==Pt||(a=1);let x=(o[0][1]+o[1][1])/2;if(S()){[l,a]=[a,l];[e,n]=[n,e];[u,x]=[x,u]}const p=[];for(let t=0;t<ot();++t)p.push(l);for(let t=0;t<ot();++t){j(t)[0]*p[t]>H(t)&&(p[t]=H(t)/j(t)[0]);j(t)[0]*p[t]<N(t)&&(p[t]=N(t)/j(t)[0])}const y=[];for(let t=0;t<it();++t)y.push(a);for(let t=0;t<it();++t){I(t)[3]*y[t]>G(t)&&(y[t]=G(t)/I(t)[3]);I(t)[3]*y[t]<K(t)&&(y[t]=K(t)/I(t)[3])}if(-1!==Tt)1!==p[Tt]&&(p[Tt]<1||j(Tt)[0]!==H(Tt))&&Rt(j(Tt),s([p[Tt],0,0,1,-p[Tt]*e+u,0],j(Tt)));else if(-1===Mt){for(let t=0;t<ot();++t)1!==p[t]&&(p[t]<1||j(t)[0]!==H(t))&&Rt(j(t),s([p[t],0,0,1,-p[t]*e+u,0],j(t)));for(let t=0;t<it();++t)1!==y[t]&&(y[t]<1||I(t)[3]!==G(t))&&Rt(I(t),s([1,0,0,y[t],0,-y[t]*n+x],I(t)))}else 1!==y[Mt]&&(y[Mt]<1||I(Mt)[3]!==G(Mt))&&Rt(I(Mt),s([1,0,0,y[Mt],0,-y[Mt]*n+x],I(Mt)));Kt();const h=qt(t,rt(),lt());mt[0]=h[0];mt[1]=h[1];gt=o;ee();Bt();zt()}Qt=null}),1))}};function ee(){for(let t=0;t<nt(J().x);++t){let e=g(j(t)[0])-1;j(t)[0]===H(t)&&(e=nt(J().x[t])-1);e>=nt(J().x[t])&&(e=nt(J().x[t])-1);for(let n=0;n<nt(J().x[t]);++n)if(e===n)for(let e=0;e<nt(J().x[t][n]);++e)J().x[t][n][e].color[3]=$().x[t][e];else for(let e=0;e<nt(J().x[t][n]);++e)J().x[t][n][e].color[3]=0}for(let t=0;t<nt(J().y);++t){let e=g(I(t)[3])-1;I(t)[3]===G(t)&&(e=nt(J().y[t])-1);e>=nt(J().y[t])&&(e=nt(J().y[t])-1);for(let n=0;n<nt(J().y[t]);++n)if(e===n)for(let e=0;e<nt(J().y[t][n]);++e)J().y[t][n][e].color[3]=$().y[t][e];else for(let e=0;e<nt(J().y[t][n]);++e)J().y[t][n][e].color[3]=0}}function ne(t,e=0,n=-1,o=-1){const i=Wt(mt,rt(),lt());S()&&(t={x:t.y,y:-t.x});if(1&e){if(-1!==n)j(n)[4]=j(n)[4]+t.x;else if(-1===o){for(let e=0;e<ot();++e)j(e)[4]=j(e)[4]+t.x;for(let e=0;e<it();++e)I(e)[5]=I(e)[5]-t.y}else I(o)[5]=I(o)[5]-t.y;Zt()}else if(2&e){let e;if(-1!==n)e=kt(n,0);else if(-1===o){e=kt(0,0);for(let t=1;t<ot();++t)e=d(e,kt(t,0));for(let t=1;t<it();++t)e=d(e,kt(0,t))}else e=kt(0,o);y(e)>y(_())?t.x>0&&(t.x=t.x/(1+(y(e)-y(_()))*ft)):h(e)<h(_())&&t.x<0&&(t.x=t.x/(1+(h(_())-h(e))*ft));x(e)>x(_())?t.y>0&&(t.y=t.y/(1+(x(e)-x(_()))*ft)):p(e)<p(_())&&t.y<0&&(t.y=t.y/(1+(p(_())-p(e))*ft));if(-1!==n)j(n)[4]=j(n)[4]+t.x;else if(-1===o){for(let e=0;e<ot();++e)j(e)[4]=j(e)[4]+t.x;for(let e=0;e<it();++e)I(e)[5]=I(e)[5]-t.y}else I(o)[5]=I(o)[5]-t.y;-1===o&&(mt[0]=mt[0]+t.x);-1===n&&(mt[1]=mt[1]+t.y);Zt()}else{if(-1!==n)j(n)[4]=j(n)[4]+t.x;else if(-1===o){for(let e=0;e<ot();++e)j(e)[4]=j(e)[4]+t.x;for(let e=0;e<it();++e)I(e)[5]=I(e)[5]-t.y}else I(o)[5]=I(o)[5]-t.y;-1===o&&(mt[0]=mt[0]+t.x);-1===n&&(mt[1]=mt[1]+t.y);Kt()}const r=qt(i,rt(),lt());mt[0]=r[0];mt[1]=r[1];Bt();zt()}function oe(t,e,n,o=-1,i=-1){const r=Wt(mt,rt(),lt());let l;l=S()?[t.y-x(L()),t.x-y(L())]:s(u([1,0,0,-1,y(L()),p(L())]),[t.x,t.y]);const f=l[0],c=l[1];let a=Math.pow(1.2,S()?n:e),h=Math.pow(1.2,S()?e:n);if(-1!==o){j(o)[0]*a>H(o)&&(a=H(o)/j(o)[0]);(a<1||j(o)[0]!==H(o))&&Rt(j(o),s([a,0,0,1,f-a*f,0],j(o)))}else if(-1===i){for(let t=0;t<ot();++t){let e=a;j(t)[0]*a>H(t)&&(e=H(t)/j(t)[0]);(e<1||j(t)[0]!==H(t))&&Rt(j(t),s([e,0,0,1,f-e*f,0],j(t)))}for(let t=0;t<it();++t){let e=h;I(t)[3]*h>G(t)&&(e=G(t)/I(t)[3]);(e<1||I(t)[3]!==G(t))&&Rt(I(t),s([1,0,0,e,0,c-e*c],I(t)))}}else{I(i)[3]*h>G(i)&&(h=G(i)/I(i)[3]);(h<1||I(i)[3]!==G(i))&&Rt(I(i),s([1,0,0,h,0,c-h*c],I(i)))}Kt();const d=qt(r,rt(),lt());mt[0]=d[0];mt[1]=d[1];ee();Bt();zt()}this.setXRange=function(t,e,n,i){const r=A(R(t),0);e=r[0]+r[2]*e;n=r[0]+r[2]*n;if(y(r)>h(r)){e>y(r)&&(e=y(r));n<h(r)&&(n=h(r))}else{e<y(r)&&(e=y(r));n>h(r)&&(n=h(r))}const l=Z(t),s=E(l,X(t),e,n,S(),L(),A(R(t),X(t)),o.minZoom,o.maxZoom),u=s.xZoom,f=s.yZoom,c=s.panPoint,a=Wt(mt,rt(),lt());j(R(t))[0]=u;f&&i&&(I(X(t))[3]=f);j(R(t))[4]=-c[0]*u;f&&i&&(I(X(t))[5]=-c[1]*f);Zt();const x=qt(a,rt(),lt());mt[0]=x[0];mt[1]=x[1];Kt();ee();Bt();zt()};this.getSeries=function(t){return Z(t)};this.rangeChangedCallbacks=[];this.updateConfig=function(t){for(const[e,n]of Object.entries(t))o[e]=n;!function(){if(!D()||null!==dt&&n.canvas.width===dt.width&&n.canvas.height===dt.height){if(null!==dt&&!D()){dt.parentNode.removeChild(dt);delete e.wtOObj;dt=null}}else{if(dt){dt.parentNode.removeChild(dt);delete e.wtOObj;dt=null}const t=document.createElement(\"canvas\");t.setAttribute(\"width\",n.canvas.width);t.setAttribute(\"height\",n.canvas.height);t.style.position=\"absolute\";t.style.display=\"block\";t.style.left=\"0\";t.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){t.style.msTouchAction=\"none\";t.style.touchAction=\"none\"}n.canvas.parentNode.appendChild(t);dt=t;e.wtOObj=dt}mt=[(y(L())+h(L()))/2,(x(L())+p(L()))/2]}();ee();Bt();zt()};this.updateConfig({});if(!window.TouchEvent||window.MSPointerEvent||window.PointerEvent){const t=function(){};i.touchStart=t;i.touchEnd=t;i.touchMoved=t}else{i.touchStart=pt.start;i.touchEnd=pt.end;i.touchMoved=pt.moved}})");
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
