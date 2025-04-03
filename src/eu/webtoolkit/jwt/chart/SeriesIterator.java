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
 * Abstract base class for iterating over series data in a chart.
 *
 * <p>This class is specialized for rendering series data.
 *
 * <p>
 */
public class SeriesIterator {
  private static Logger logger = LoggerFactory.getLogger(SeriesIterator.class);

  /**
   * Start handling a new segment.
   *
   * <p>Because of a &apos;break&apos; specified in an axis, axes may be divided in one or two
   * segments (in fact only the API limits this now to two). The iterator will iterate all segments
   * separately, but each time with a different clipping region specified in the painter,
   * corresponding to that segment.
   *
   * <p>The <i>currentSegmentArea</i> specifies the clipping area.
   */
  public void startSegment(
      int currentXSegment, int currentYSegment, final WRectF currentSegmentArea) {
    this.currentXSegment_ = currentXSegment;
    this.currentYSegment_ = currentYSegment;
  }
  /**
   * End handling a particular segment.
   *
   * <p>
   *
   * @see SeriesIterator#startSegment(int currentXSegment, int currentYSegment, WRectF
   *     currentSegmentArea)
   */
  public void endSegment() {}
  /**
   * Start iterating a particular series.
   *
   * <p>Returns whether the series values should be iterated. The <i>groupWidth</i> is the width (in
   * pixels) of a single bar group. The chart contains <i>numBarGroups</i>, and the current series
   * is in the <i>currentBarGroup</i>&apos;th group.
   */
  public boolean startSeries(
      final WDataSeries series, double groupWidth, int numBarGroups, int currentBarGroup) {
    return true;
  }
  /** End iterating a particular series. */
  public void endSeries() {}
  /**
   * Process a value.
   *
   * <p>Processes a value with model coordinates (<i>x</i>, <i>y</i>). The y value may differ from
   * the model&apos;s y value, because of stacked series. The y value here corresponds to the
   * location on the chart, after stacking.
   *
   * <p>The <i>stackY</i> argument is the y value from the previous series (also after stacking). It
   * will be 0, unless this series is stacked.
   */
  public void newValue(
      final WDataSeries series,
      double x,
      double y,
      double stackY,
      int xRow,
      int xColumn,
      int yRow,
      int yColumn) {}
  /** Returns the current X segment. */
  public int getCurrentXSegment() {
    return this.currentXSegment_;
  }
  /** Returns the current Y segment. */
  public int getCurrentYSegment() {
    return this.currentYSegment_;
  }

  public static void setPenColor(
      final WPen pen,
      final WDataSeries series,
      int xRow,
      int xColumn,
      int yRow,
      int yColumn,
      ItemDataRole colorRole) {
    WColor color = null;
    if (yRow >= 0 && yColumn >= 0) {
      if (colorRole.equals(ItemDataRole.MarkerPenColor)) {
        color = series.getModel().getMarkerPenColor(yRow, yColumn);
      } else {
        if (colorRole.equals(ItemDataRole.MarkerBrushColor)) {
          color = series.getModel().getMarkerBrushColor(yRow, yColumn);
        }
      }
    }
    if (!(color != null) && xRow >= 0 && xColumn >= 0) {
      if (colorRole.equals(ItemDataRole.MarkerPenColor)) {
        color = series.getModel().getMarkerPenColor(xRow, xColumn);
      } else {
        if (colorRole.equals(ItemDataRole.MarkerBrushColor)) {
          color = series.getModel().getMarkerBrushColor(xRow, xColumn);
        }
      }
    }
    if (color != null) {
      pen.setColor(color);
    }
  }

  public static void setBrushColor(
      final WBrush brush,
      final WDataSeries series,
      int xRow,
      int xColumn,
      int yRow,
      int yColumn,
      ItemDataRole colorRole) {
    WColor color = null;
    if (yRow >= 0 && yColumn >= 0) {
      if (colorRole.equals(ItemDataRole.MarkerBrushColor)) {
        color = series.getModel().getMarkerBrushColor(yRow, yColumn);
      } else {
        if (colorRole.equals(ItemDataRole.BarBrushColor)) {
          color = series.getModel().getBarBrushColor(yRow, yColumn);
        }
      }
    }
    if (!(color != null) && xRow >= 0 && xColumn >= 0) {
      if (colorRole.equals(ItemDataRole.MarkerBrushColor)) {
        color = series.getModel().getMarkerBrushColor(xRow, xColumn);
      } else {
        if (colorRole.equals(ItemDataRole.BarBrushColor)) {
          color = series.getModel().getBarBrushColor(xRow, xColumn);
        }
      }
    }
    if (color != null) {
      brush.setColor(color);
    }
  }

  private int currentXSegment_;
  private int currentYSegment_;

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
        "(function(t,e,n,o){e.wtCObj=this;const i=3,r=4,l=2440587.5,s=86400,u=this,f=t.WT;u.config=o;const c=f.gfxUtils,a=c.transform_mult,x=c.transform_inverted,p=c.transform_assign,y=c.transform_equal,h=c.transform_apply,d=c.rect_top,m=c.rect_bottom,v=c.rect_left,w=c.rect_right,T=c.rect_intersection,M=f.chartCommon,g=M.minMaxY,b=M.findClosestPoint,E=M.projection,P=M.distanceLessThanRadius,C=M.toZoomLevel,A=M.isPointInRect,O=M.findYRange,D=function(t,e){return M.matchXAxis(t,e,Y(),o.xAxes,_())},S=function(t,e){return M.matchYAxis(t,e,Y(),o.yAxes,_())};function j(t,e){const n=(i=t,o.xModelAreas[i]);var i;const r=function(t){return o.yModelAreas[t]}(e);return _()?[r[0],n[1],r[2],n[3]]:[n[0],r[1],n[2],r[3]]}function I(){return o.followCurve}function L(){return o.crosshair||-1!==I()}function _(){return o.isHorizontal}function U(t){return o.xTransforms[t]}function F(t){return o.yTransforms[t]}function Y(){return o.area}function Z(){return o.insideArea}function R(t=null){return null===t?o.series:o.series[t]}function X(t){return R(t).transform}function k(t){return _()?a([0,1,1,0,0,0],a(X(t),[0,1,1,0,0,0])):X(t)}function W(t){return R(t).curve}function q(t){return R(t).xAxis}function z(t){return R(t).yAxis}function B(){return o.seriesSelection}function N(){return o.sliders}function K(){return o.hasToolTips}function H(){return o.coordinateOverlayPadding}function G(){return o.curveManipulation}function J(t){return o.minZoom.x[t]}function $(t){return o.minZoom.y[t]}function Q(t){return o.maxZoom.x[t]}function V(t){return o.maxZoom.y[t]}function tt(){return o.pens}function et(){return o.penAlpha}function nt(){return o.selectedCurve}function ot(t){t.preventDefault&&t.preventDefault()}function it(t,n){e.addEventListener(t,n)}function rt(t,n){e.removeEventListener(t,n)}function lt(t){return t.length}function st(){return lt(o.xAxes)}function ut(){return lt(o.yAxes)}function ft(){return o.crosshairXAxis}function ct(){return o.crosshairYAxis}let at=!1;const xt=function(t){if(!at){at=!0;requestAnimationFrame((function(){t();at=!1}))}};if(window.MSPointerEvent||window.PointerEvent){e.style.touchAction=\"none\";n.canvas.style.msTouchAction=\"none\";n.canvas.style.touchAction=\"none\"}const pt=1,yt=2,ht=1,dt=2,mt=0,vt=1,wt=250,Tt=500,Mt=.07,gt=3,bt=.001,Et=1.5;let Pt=e.wtEObj2;if(!Pt){Pt={};Pt.contextmenuListener=function(t){ot(t);rt(\"contextmenu\",Pt.contextmenuListener)}}e.wtEObj2=Pt;const Ct={};function At(t){return 2===t.pointerType||3===t.pointerType||\"pen\"===t.pointerType||\"touch\"===t.pointerType}let Ot=!1;(window.MSPointerEvent||window.PointerEvent)&&function(){const t=[];function n(){Ot=lt(t)>0}function o(o){if(At(o)){ot(o);t.push(o);n();Ct.start(e,{touches:t.slice(0)})}}function i(o){if(Ot&&At(o)){ot(o);for(let e=0;e<lt(t);++e)if(t[e].pointerId===o.pointerId){t.splice(e,1);break}n();Ct.end(e,{touches:t.slice(0),changedTouches:[]})}}function r(o){if(At(o)){ot(o);for(let e=0;e<lt(t);++e)if(t[e].pointerId===o.pointerId){t[e]=o;break}n();Ct.moved(e,{touches:t.slice(0)})}}const l=e.wtEObj;if(l)if(window.PointerEvent){rt(\"pointerdown\",l.pointerDown);rt(\"pointerup\",l.pointerUp);rt(\"pointerout\",l.pointerUp);rt(\"pointermove\",l.pointerMove)}else{rt(\"MSPointerDown\",l.pointerDown);rt(\"MSPointerUp\",l.pointerUp);rt(\"MSPointerOut\",l.pointerUp);rt(\"MSPointerMove\",l.pointerMove)}e.wtEObj={pointerDown:o,pointerUp:i,pointerMove:r};if(window.PointerEvent){it(\"pointerdown\",o);it(\"pointerup\",i);it(\"pointerout\",i);it(\"pointermove\",r)}else{it(\"MSPointerDown\",o);it(\"MSPointerUp\",i);it(\"MSPointerOut\",i);it(\"MSPointerMove\",r)}}();let Dt=e.wtOObj??null,St=null,jt=!0,It=null,Lt=-1,_t=-1,Ut=[],Ft=!1,Yt=!1,Zt=null,Rt=null,Xt=null;const kt={x:0,y:0};let Wt=null,qt=null,zt=e.wtTObj;if(!zt){zt={overTooltip:!1};e.wtTObj=zt}function Bt(){if(zt){if(zt.tooltipTimeout){clearTimeout(zt.tooltipTimeout);zt.tooltipTimeout=null}if(!zt.overTooltip&&zt.tooltipOuterDiv){document.body.removeChild(zt.tooltipOuterDiv);zt.toolTipEl=null;zt.tooltipOuterDiv=null}}}let Nt=null,Kt=!1,Ht=null;const Gt=[];for(let t=0;t<st();++t){Gt.push([0,0,0,0,0,0]);p(Gt[t],U(t))}const Jt=[];for(let t=0;t<ut();++t){Jt.push([0,0,0,0,0,0]);p(Jt[t],F(t))}function $t(){if(function(){for(let t=0;t<st();++t)if(o.notifyTransform.x[t])return!0;for(let t=0;t<ut();++t)if(o.notifyTransform.y[t])return!0;return!1}()){if(Ht){window.clearTimeout(Ht);Ht=null}Ht=setTimeout((function(){for(let e=0;e<st();++e)if(o.notifyTransform.x[e]&&!y(Gt[e],U(e))){t.emit(n.widget,\"xTransformChanged\"+e);p(Gt[e],U(e))}for(let e=0;e<ut();++e)if(o.notifyTransform.y[e]&&!y(Jt[e],F(e))){t.emit(n.widget,\"yTransformChanged\"+e);p(Jt[e],F(e))}}),wt)}}const Qt=function(t,e){p(t,e);$t()};function Vt(t=0,e=0){if(_()){const n=v(Y()),o=d(Y());return a([0,1,1,0,n,o],a(U(t),a(F(e),[0,1,1,0,-o,-n])))}{const n=v(Y()),o=m(Y());return a([1,0,0,-1,n,o],a(U(t),a(F(e),[1,0,0,-1,-n,o])))}}n.combinedTransform=Vt;function te(t,e){return a(Vt(t,e),Z())}function ee(t,e,n,o=!1){let i,r;i=o?t:a(x(Vt(e,n)),t);r=_()?[(i[vt]-Y()[1])/Y()[3],(i[mt]-Y()[0])/Y()[2]]:[(i[mt]-Y()[0])/Y()[2],1-(i[vt]-Y()[1])/Y()[3]];return[j(e,n)[0]+r[mt]*j(e,n)[2],j(e,n)[1]+r[vt]*j(e,n)[3]]}function ne(t,e,n,o=!1){return M.toDisplayCoord(t,o?[1,0,0,1,0,0]:Vt(e,n),_(),Y(),j(e,n))}function oe(){for(let t=0;t<st();++t){let e,n;const o=j(t,0);if(_()){e=(ee([0,d(Y())],t,0)[0]-o[0])/o[2];n=(ee([0,m(Y())],t,0)[0]-o[0])/o[2]}else{e=(ee([v(Y()),0],t,0)[0]-o[0])/o[2];n=(ee([w(Y()),0],t,0)[0]-o[0])/o[2]}for(let o=0;o<lt(N());++o){const i=f.$(N()[o]);if(i){const o=i.wtSObj;o&&o.xAxis()===t&&o.changeRange(e,n)}}}}function ie(){Bt();K()&&zt.tooltipPosition&&(zt.tooltipTimeout=setTimeout((function(){ue()}),Tt));jt&&xt((function(){n.repaint();L()&&re()}))}function re(){if(!jt)return;const t=Dt.getContext(\"2d\");t.clearRect(0,0,Dt.width,Dt.height);t.save();t.beginPath();t.moveTo(v(Y()),d(Y()));t.lineTo(w(Y()),d(Y()));t.lineTo(w(Y()),m(Y()));t.lineTo(v(Y()),m(Y()));t.closePath();t.clip();let e,n,i,r=a(x(Vt(ft(),ct())),St),l=St[mt],s=St[vt];if(-1!==I()){r=b(_()?r[vt]:r[mt],W(I()),_());const t=a(Vt(q(I()),z(I())),a(k(I()),r));l=t[mt];s=t[vt];St[mt]=l;St[vt]=s}e=_()?[(r[vt]-Y()[1])/Y()[3],(r[mt]-Y()[0])/Y()[2]]:[(r[mt]-Y()[0])/Y()[2],1-(r[vt]-Y()[1])/Y()[3]];if(-1!==I()){n=q(I());i=z(I())}else{n=ft();i=ct()}const u=j(n,i);r=[u[0]+e[mt]*u[2],u[1]+e[vt]*u[3]];t.fillStyle=t.strokeStyle=o.crosshairColor;t.font=\"16px sans-serif\";t.textAlign=\"right\";t.textBaseline=\"top\";let f=le(r[0],o.xAxes[n].scale),c=le(r[1],o.yAxes[i].scale);\"-0.00\"===f&&(f=\"0.00\");\"-0.00\"===c&&(c=\"0.00\");t.fillText(\"(\"+f+\",\"+c+\")\",w(Y())-H()[0],d(Y())+H()[1]);t.setLineDash&&t.setLineDash([1,2]);t.beginPath();t.moveTo(Math.floor(l)+.5,Math.floor(d(Y()))+.5);t.lineTo(Math.floor(l)+.5,Math.floor(m(Y()))+.5);t.moveTo(Math.floor(v(Y()))+.5,Math.floor(s)+.5);t.lineTo(Math.floor(w(Y()))+.5,Math.floor(s)+.5);t.stroke();t.restore()}function le(t,e){let n=t.toFixed(2);e===i?n=new Date((t-l)*s*1e3).toDateString():e===r&&(n=new Date(1e3*t).toString());return n}function se(t=null){let e;_()&&(t===ht?t=dt:t===dt&&(t=ht));for(let n=0;n<st();++n){let o=te(n,0);if(_()){if(null===t||t===dt){if(U(n)[0]<1){U(n)[0]=1;o=te(n,0)}if(d(o)>d(Z())){e=d(Z())-d(o);U(n)[4]=U(n)[4]+e}else if(m(o)<m(Z())){e=m(Z())-m(o);U(n)[4]=U(n)[4]+e}}}else if(null===t||t===ht){if(U(n)[0]<1){U(n)[0]=1;o=te(n,0)}if(v(o)>v(Z())){e=v(Z())-v(o);U(n)[4]=U(n)[4]+e}else if(w(o)<w(Z())){e=w(Z())-w(o);U(n)[4]=U(n)[4]+e}}}for(let n=0;n<ut();++n){let o=te(0,n);if(_()){if(null===t||t===ht){if(F(n)[3]<1){F(n)[3]=1;o=te(0,n)}if(v(o)>v(Z())){e=v(Z())-v(o);F(n)[5]=F(n)[5]+e}else if(w(o)<w(Z())){e=w(Z())-w(o);F(n)[5]=F(n)[5]+e}}}else if(null===t||t===dt){if(F(n)[3]<1){F(n)[3]=1;o=te(0,n)}if(d(o)>d(Z())){e=d(Z())-d(o);F(n)[5]=F(n)[5]-e}else if(m(o)<m(Z())){e=m(Z())-m(o);F(n)[5]=F(n)[5]-e}}}$t()}function ue(){zt.toolTipEl||t.emit(n.widget,\"loadTooltip\",zt.tooltipPosition[mt],zt.tooltipPosition[vt])}this.updateTooltip=function(t){Bt();if(t){if(!zt.tooltipPosition)return;zt.toolTipEl=document.createElement(\"div\");zt.toolTipEl.className=o.ToolTipInnerStyle;zt.toolTipEl.innerHTML=t;zt.tooltipOuterDiv=document.createElement(\"div\");zt.tooltipOuterDiv.className=o.ToolTipOuterStyle;document.body.appendChild(zt.tooltipOuterDiv);zt.tooltipOuterDiv.appendChild(zt.toolTipEl);const e=f.widgetPageCoordinates(n.canvas),i=zt.tooltipPosition[mt]+e.x,r=zt.tooltipPosition[vt]+e.y;f.fitToWindow(zt.tooltipOuterDiv,i+10,r+10,i-10,r-10);zt.toolTipEl.addEventListener(\"mouseenter\",(function(){zt.overTooltip=!0}));zt.toolTipEl.addEventListener(\"mouseleave\",(function(){zt.overTooltip=!1}))}};this.mouseMove=function(t,e){setTimeout((function(){setTimeout(Bt,200);if(Ot)return;const t=f.widgetCoordinates(n.canvas,e);if(A(t,Y())){if(K()){zt.tooltipPosition=[t.x,t.y];zt.tooltipTimeout=setTimeout((function(){ue()}),Tt)}if(null===It&&L()&&jt){St=[t.x,t.y];xt(re)}}}),0)};this.mouseOut=function(t,e){setTimeout(Bt,200)};this.mouseDown=function(t,e){if(Ot)return;const o=f.widgetCoordinates(n.canvas,e),i=S(o.x,o.y),r=A(o,Y()),l=D(o.x,o.y);if(-1!==i||-1!==l||r){It=o;Lt=l;_t=i}};this.mouseUp=function(t,e){if(!Ot){It=null;Lt=-1;_t=-1}};this.mouseDrag=function(t,e){if(Ot)return;if(null===It){u.mouseDown(t,e);return}const i=f.widgetCoordinates(n.canvas,e);if(1===f.buttons)if(-1===_t&&-1===Lt&&G()&&R(nt())){const t=nt();let e;e=_()?i.x-It.x:i.y-It.y;p(X(t),a([1,0,0,1,0,e/F(z(nt()))[3]],X(t)));ie()}else o.pan&&de({x:i.x-It.x,y:i.y-It.y},0,Lt,_t);It=i};this.clicked=function(e,o){if(Ot)return;if(null!==It)return;if(!B())return;const i=f.widgetCoordinates(n.canvas,o);t.emit(n.widget,\"seriesSelected\",i.x,i.y)};this.mouseWheel=function(t,e){const i=(e.metaKey<<3)+(e.altKey<<2)+(e.ctrlKey<<1)+e.shiftKey,r=o.wheelActions[i];if(void 0===r)return;const l=f.widgetCoordinates(n.canvas,e),s=D(l.x,l.y),u=S(l.x,l.y),c=A(l,Y());if(-1===s&&-1===u&&!c)return;const x=f.normalizeWheel(e);if(c&&0===i&&G()){const t=nt(),n=-x.spinY;if(R(t)){const o=k(t),i=h(o,W(t)),r=g(i,_()),l=(r[0]+r[1])/2;f.cancelEvent(e);const s=Math.pow(1.2,n);p(X(t),a([1,0,0,s,0,l-s*l],X(t)));ie();return}}if(4!==r&&5!==r&&6!==r||!o.pan){if(o.zoom){f.cancelEvent(e);let t=-x.spinY;0===t&&(t=-x.spinX);1===r?me(l,0,t,s,u):0===r?me(l,t,0,s,u):2===r?me(l,t,t,s,u):3===r&&(0!==x.pixelX?me(l,t,0,s,u):me(l,0,t,s,u))}}else{const t=[];for(let e=0;e<st();++e)t.push(U(e)[4]);const n=[];for(let t=0;t<ut();++t)n.push(F(t)[5]);6===r?de({x:-x.pixelX,y:-x.pixelY},0,s,u):5===r?de({x:0,y:-x.pixelX-x.pixelY},0,s,u):4===r&&de({x:-x.pixelX-x.pixelY,y:0},0,s,u);for(let n=0;n<st();++n)t[n]!==U(n)[4]&&f.cancelEvent(e);for(let t=0;t<ut();++t)n[t]!==F(t)[5]&&f.cancelEvent(e)}};const fe=function(){B()&&t.emit(n.widget,\"seriesSelected\",It.x,It.y)};function ce(){return Dt||n.canvas}Ct.start=function(t,e,i){Ft=1===lt(e.touches);Yt=2===lt(e.touches);if(Ft){Kt=!1;const t=f.widgetCoordinates(n.canvas,e.touches[0]),o=S(t.x,t.y),r=A(t,Y()),l=D(t.x,t.y);if(-1===o&&-1===l&&!r)return;Nt=-1===o&&-1===l&&L()&&P(St,[t.x,t.y],30)?1:0;qt=Date.now();It=t;_t=o;Lt=l;if(1!==Nt){!i&&r&&(Wt=window.setTimeout(fe,200));it(\"contextmenu\",Pt.contextmenuListener)}f.capture(null);f.capture(ce())}else{if(!Yt||!o.zoom&&!G())return;{if(Wt){window.clearTimeout(Wt);Wt=null}Kt=!1;Ut=[f.widgetCoordinates(n.canvas,e.touches[0]),f.widgetCoordinates(n.canvas,e.touches[1])].map((function(t){return[t.x,t.y]}));let t=-1,o=-1;if(!Ut.every((function(t){return A(t,Y())}))){t=D(Ut[0][mt],Ut[0][vt]);if(-1!==t){if(t!==D(Ut[1][mt],Ut[1][vt])){Yt=null;return}}else{o=S(Ut[0][mt],Ut[0][vt]);if(1===o){Yt=null;return}if(o!==S(Ut[1][mt],Ut[1][vt])){Yt=null;return}}}f.capture(null);f.capture(ce());Zt=Math.atan2(Ut[1][1]-Ut[0][1],Ut[1][0]-Ut[0][0]);Rt=[(Ut[0][0]+Ut[1][0])/2,(Ut[0][1]+Ut[1][1])/2];const i=Math.abs(Math.sin(Zt)),r=Math.abs(Math.cos(Zt));Zt=i<Math.sin(.125*Math.PI)?0:r<Math.cos(.375*Math.PI)?Math.PI/2:Math.tan(Zt)>0?Math.PI/4:-Math.PI/4;Xt=E(Zt,Rt);Lt=t;_t=o}}ot(e)};function ae(t,e=null){if(!Kt)return;const n=Date.now();e=e??n-qt;const o={x:0,y:0};let i;if(-1!==Lt)i=te(Lt,0);else if(-1===_t){i=te(0,0);for(let t=1;t<st();++t)i=T(i,te(t,0));for(let t=1;t<ut();++t)i=T(i,te(0,t))}else i=te(0,_t);const r=2e-4;if(e>34){jt=!1;const n=Math.floor(e/17-1);for(let e=0;e<n;++e){ae(t,17);if(!Kt){jt=!0;ie();return}}e-=17*n;jt=!0}kt.x!==1/0&&kt.x!==-1/0||(kt.x>0?kt.x=Et:kt.x=-1.5);if(isFinite(kt.x)){kt.x=kt.x/(1+.003*e);i[0]+=kt.x*e;if(v(i)>v(Z())){kt.x=kt.x+-r*(v(i)-v(Z()))*e;kt.x*=.7}else if(w(i)<w(Z())){kt.x=kt.x+-r*(w(i)-w(Z()))*e;kt.x*=.7}Math.abs(kt.x)<bt&&(v(i)>v(Z())?kt.x=bt:w(i)<w(Z())&&(kt.x=-.001));Math.abs(kt.x)>Et&&(kt.x=(kt.x>0?1:-1)*Et);o.x=kt.x*e}kt.y!==1/0&&kt.y!==-1/0||(kt.y>0?kt.y=Et:kt.y=-1.5);if(isFinite(kt.y)){kt.y=kt.y/(1+.003*e);i[1]+=kt.y*e;if(d(i)>d(Z())){kt.y=kt.y+-r*(d(i)-d(Z()))*e;kt.y*=.7}else if(m(i)<m(Z())){kt.y=kt.y+-r*(m(i)-m(Z()))*e;kt.y*=.7}Math.abs(kt.y)<.001&&(d(i)>d(Z())?kt.y=.001:m(i)<m(Z())&&(kt.y=-.001));Math.abs(kt.y)>Et&&(kt.y=(kt.y>0?1:-1)*Et);o.y=kt.y*e}if(-1!==Lt)i=te(Lt,0);else if(-1===_t){i=te(0,0);for(let t=1;t<st();++t)i=T(i,te(t,0));for(let t=1;t<ut();++t)i=T(i,te(0,t))}else i=te(0,_t);de(o,pt,Lt,_t);let l;if(-1!==Lt)l=te(Lt,0);else if(-1===_t){l=te(0,0);for(let t=1;t<st();++t)l=T(l,te(t,0));for(let t=1;t<ut();++t)l=T(l,te(0,t))}else l=te(0,_t);if(v(i)>v(Z())&&v(l)<=v(Z())){kt.x=0;de({x:-o.x,y:0},pt,Lt,_t);se(ht)}if(w(i)<w(Z())&&w(l)>=w(Z())){kt.x=0;de({x:-o.x,y:0},pt,Lt,_t);se(ht)}if(d(i)>d(Z())&&d(l)<=d(Z())){kt.y=0;de({x:0,y:-o.y},pt,Lt,_t);se(dt)}if(m(i)<m(Z())&&m(l)>=m(Z())){kt.y=0;de({x:0,y:-o.y},pt,Lt,_t);se(dt)}if(Math.abs(kt.x)<.02&&Math.abs(kt.y)<.02&&function(t){return d(t)<=d(Z())+gt&&m(t)>=m(Z())-gt&&v(t)<=v(Z())+gt&&w(t)>=w(Z())-gt}(l)){se();Kt=!1;It=null;kt.x=0;kt.y=0;qt=null;Ut=[]}else{qt=n;jt&&requestAnimationFrame(ae)}}Ct.end=function(t,e){if(Wt){window.clearTimeout(Wt);Wt=null}window.setTimeout((function(){rt(\"contextmenu\",Pt.contextmenuListener)}),0);let n=Array.prototype.slice.call(e.touches),i=0===lt(n);if(!i)for(let t=0;t<lt(e.changedTouches);++t){const o=e.changedTouches[t].identifier;for(let t=0;t<lt(n);++t)if(n[t].identifier===o){n.splice(t,1);return}}i=0===lt(n);Ft=1===lt(n);Yt=2===lt(n);if(i){xe=null;if(0===Nt&&(isFinite(kt.x)||isFinite(kt.y))&&o.rubberBand){qt=Date.now();Kt=!0;requestAnimationFrame(ae)}else{1===Nt&&u.mouseUp(null,null);n=[];Zt=null;Rt=null;Xt=null;qt=null}Nt=null}else(Ft||Yt)&&Ct.start(t,e,!0)};let xe=null,pe=null,ye=null;Ct.moved=function(t,e){if((Ft||Yt)&&(!Ft||null!==It)){ot(e);pe=f.widgetCoordinates(n.canvas,e.touches[0]);lt(e.touches)>1&&(ye=f.widgetCoordinates(n.canvas,e.touches[1]));if(-1===Lt&&-1===_t&&Ft&&Wt&&!P([pe.x,pe.y],[It.x,It.y],3)){window.clearTimeout(Wt);Wt=null}xe||(xe=setTimeout((function(){if(-1===Lt&&-1===_t&&Ft&&G()&&R(nt())){const t=nt();if(R(t)){const e=pe;let n;n=_()?(e.x-It.x)/F(z(nt()))[3]:(e.y-It.y)/F(z(nt()))[3];X(t)[5]+=n;It=e;ie()}}else if(Ft){const t=pe,e=Date.now(),n={x:t.x-It.x,y:t.y-It.y},i=e-qt;qt=e;if(1===Nt){St[mt]+=n.x;St[vt]+=n.y;L()&&jt&&requestAnimationFrame(re)}else if(o.pan){kt.x=n.x/i;kt.y=n.y/i;de(n,o.rubberBand?yt:0,Lt,_t)}It=t}else if(-1===Lt&&-1===_t&&Yt&&G()&&R(nt())){const t=nt();if(R(t)){const e=_()?mt:vt,n=[pe,ye].map((function(t){return[t.x,t.y]})),o=Math.abs(Ut[1][e]-Ut[0][e]),i=Math.abs(n[1][e]-n[0][e]);let r=o>0?i/o:1;i===o&&(r=1);const l=a(x(Vt(q(t),z(t))),[0,(Ut[0][e]+Ut[1][e])/2])[1],s=a(x(Vt(q(t),z(t))),[0,(n[0][e]+n[1][e])/2])[1];p(X(t),a([1,0,0,r,0,-r*l+s],X(t)));ie();It=null;Ut=n}}else if(Yt&&o.zoom){const t=ee(St,ft(),ct());let e=(Ut[0][0]+Ut[1][0])/2,n=(Ut[0][1]+Ut[1][1])/2;const o=[pe,ye].map((function(t){return 0===Zt?[t.x,n]:Zt===Math.PI/2?[e,t.y]:a(Xt,[t.x,t.y])})),i=Math.abs(Ut[1][0]-Ut[0][0]),r=Math.abs(o[1][0]-o[0][0]);let l=i>0?r/i:1;r!==i&&Zt!==Math.PI/2||(l=1);let s=(o[0][0]+o[1][0])/2;const u=Math.abs(Ut[1][1]-Ut[0][1]),f=Math.abs(o[1][1]-o[0][1]);let c=u>0?f/u:1;f!==u&&0!==Zt||(c=1);let x=(o[0][1]+o[1][1])/2;if(_()){[l,c]=[c,l];[e,n]=[n,e];[s,x]=[x,s]}const p=[];for(let t=0;t<st();++t)p.push(l);for(let t=0;t<st();++t){U(t)[0]*p[t]>Q(t)&&(p[t]=Q(t)/U(t)[0]);U(t)[0]*p[t]<J(t)&&(p[t]=J(t)/U(t)[0])}const y=[];for(let t=0;t<ut();++t)y.push(c);for(let t=0;t<ut();++t){F(t)[3]*y[t]>V(t)&&(y[t]=V(t)/F(t)[3]);F(t)[3]*y[t]<$(t)&&(y[t]=$(t)/F(t)[3])}if(-1!==Lt)1!==p[Lt]&&(p[Lt]<1||U(Lt)[0]!==Q(Lt))&&Qt(U(Lt),a([p[Lt],0,0,1,-p[Lt]*e+s,0],U(Lt)));else if(-1===_t){for(let t=0;t<st();++t)1!==p[t]&&(p[t]<1||U(t)[0]!==Q(t))&&Qt(U(t),a([p[t],0,0,1,-p[t]*e+s,0],U(t)));for(let t=0;t<ut();++t)1!==y[t]&&(y[t]<1||F(t)[3]!==V(t))&&Qt(F(t),a([1,0,0,y[t],0,-y[t]*n+x],F(t)))}else 1!==y[_t]&&(y[_t]<1||F(_t)[3]!==V(_t))&&Qt(F(_t),a([1,0,0,y[_t],0,-y[_t]*n+x],F(_t)));se();const h=ne(t,ft(),ct());St[mt]=h[mt];St[vt]=h[vt];Ut=o;he();ie();oe()}xe=null}),1))}};function he(){for(let t=0;t<lt(tt().x);++t){let e=C(U(t)[0])-1;U(t)[0]===Q(t)&&(e=lt(tt().x[t])-1);e>=lt(tt().x[t])&&(e=lt(tt().x[t])-1);for(let n=0;n<lt(tt().x[t]);++n)if(e===n)for(let e=0;e<lt(tt().x[t][n]);++e)tt().x[t][n][e].color[3]=et().x[t][e];else for(let e=0;e<lt(tt().x[t][n]);++e)tt().x[t][n][e].color[3]=0}for(let t=0;t<lt(tt().y);++t){let e=C(F(t)[3])-1;F(t)[3]===V(t)&&(e=lt(tt().y[t])-1);e>=lt(tt().y[t])&&(e=lt(tt().y[t])-1);for(let n=0;n<lt(tt().y[t]);++n)if(e===n)for(let e=0;e<lt(tt().y[t][n]);++e)tt().y[t][n][e].color[3]=et().y[t][e];else for(let e=0;e<lt(tt().y[t][n]);++e)tt().y[t][n][e].color[3]=0}}function de(t,e=0,n=-1,o=-1){const i=ee(St,ft(),ct());_()&&(t={x:t.y,y:-t.x});if(e&pt){if(-1!==n)U(n)[4]=U(n)[4]+t.x;else if(-1===o){for(let e=0;e<st();++e)U(e)[4]=U(e)[4]+t.x;for(let e=0;e<ut();++e)F(e)[5]=F(e)[5]-t.y}else F(o)[5]=F(o)[5]-t.y;$t()}else if(e&yt){let e;if(-1!==n)e=te(n,0);else if(-1===o){e=te(0,0);for(let t=1;t<st();++t)e=T(e,te(t,0));for(let t=1;t<ut();++t)e=T(e,te(0,t))}else e=te(0,o);v(e)>v(Z())?t.x>0&&(t.x=t.x/(1+(v(e)-v(Z()))*Mt)):w(e)<w(Z())&&t.x<0&&(t.x=t.x/(1+(w(Z())-w(e))*Mt));d(e)>d(Z())?t.y>0&&(t.y=t.y/(1+(d(e)-d(Z()))*Mt)):m(e)<m(Z())&&t.y<0&&(t.y=t.y/(1+(m(Z())-m(e))*Mt));if(-1!==n)U(n)[4]=U(n)[4]+t.x;else if(-1===o){for(let e=0;e<st();++e)U(e)[4]=U(e)[4]+t.x;for(let e=0;e<ut();++e)F(e)[5]=F(e)[5]-t.y}else F(o)[5]=F(o)[5]-t.y;-1===o&&(St[mt]=St[mt]+t.x);-1===n&&(St[vt]=St[vt]+t.y);$t()}else{if(-1!==n)U(n)[4]=U(n)[4]+t.x;else if(-1===o){for(let e=0;e<st();++e)U(e)[4]=U(e)[4]+t.x;for(let e=0;e<ut();++e)F(e)[5]=F(e)[5]-t.y}else F(o)[5]=F(o)[5]-t.y;-1===o&&(St[mt]=St[mt]+t.x);-1===n&&(St[vt]=St[vt]+t.y);se()}const r=ne(i,ft(),ct());St[mt]=r[mt];St[vt]=r[vt];ie();oe()}function me(t,e,n,o=-1,i=-1){const r=ee(St,ft(),ct());let l;l=_()?[t.y-d(Y()),t.x-v(Y())]:a(x([1,0,0,-1,v(Y()),m(Y())]),[t.x,t.y]);const s=l[0],u=l[1];let f=Math.pow(1.2,_()?n:e),c=Math.pow(1.2,_()?e:n);if(-1!==o){U(o)[0]*f>Q(o)&&(f=Q(o)/U(o)[0]);(f<1||U(o)[0]!==Q(o))&&Qt(U(o),a([f,0,0,1,s-f*s,0],U(o)))}else if(-1===i){for(let t=0;t<st();++t){let e=f;U(t)[0]*f>Q(t)&&(e=Q(t)/U(t)[0]);(e<1||U(t)[0]!==Q(t))&&Qt(U(t),a([e,0,0,1,s-e*s,0],U(t)))}for(let t=0;t<ut();++t){let e=c;F(t)[3]*c>V(t)&&(e=V(t)/F(t)[3]);(e<1||F(t)[3]!==V(t))&&Qt(F(t),a([1,0,0,e,0,u-e*u],F(t)))}}else{F(i)[3]*c>V(i)&&(c=V(i)/F(i)[3]);(c<1||F(i)[3]!==V(i))&&Qt(F(i),a([1,0,0,c,0,u-c*u],F(i)))}se();const p=ne(r,ft(),ct());St[mt]=p[mt];St[vt]=p[vt];he();ie();oe()}this.setXRange=function(t,e,n,i){const r=j(q(t),0);e=r[0]+r[2]*e;n=r[0]+r[2]*n;if(v(r)>w(r)){e>v(r)&&(e=v(r));n<w(r)&&(n=w(r))}else{e<v(r)&&(e=v(r));n>w(r)&&(n=w(r))}const l=W(t),s=O(l,z(t),e,n,_(),Y(),j(q(t),z(t)),o.minZoom,o.maxZoom),u=s.xZoom,f=s.yZoom,c=s.panPoint,a=ee(St,ft(),ct());U(q(t))[0]=u;f&&i&&(F(z(t))[3]=f);U(q(t))[4]=-c[mt]*u;f&&i&&(F(z(t))[5]=-c[vt]*f);$t();const x=ne(a,ft(),ct());St[mt]=x[mt];St[vt]=x[vt];se();he();ie();oe()};this.getSeries=function(t){return W(t)};this.rangeChangedCallbacks=[];this.updateConfig=function(t){for(const[e,n]of Object.entries(t))o[e]=n;!function(){if(!L()||null!==Dt&&n.canvas.width===Dt.width&&n.canvas.height===Dt.height){if(null!==Dt&&!L()){Dt.parentNode.removeChild(Dt);delete e.wtOObj;Dt=null}}else{if(Dt){Dt.parentNode.removeChild(Dt);delete e.wtOObj;Dt=null}const t=document.createElement(\"canvas\");t.setAttribute(\"width\",n.canvas.width);t.setAttribute(\"height\",n.canvas.height);t.style.position=\"absolute\";t.style.display=\"block\";t.style.left=\"0\";t.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){t.style.msTouchAction=\"none\";t.style.touchAction=\"none\"}n.canvas.parentNode.appendChild(t);Dt=t;e.wtOObj=Dt}St=[(v(Y())+w(Y()))/2,(d(Y())+m(Y()))/2]}();he();ie();oe()};this.updateConfig({});if(!window.TouchEvent||window.MSPointerEvent||window.PointerEvent){const t=function(){};u.touchStart=t;u.touchEnd=t;u.touchMoved=t}else{u.touchStart=Ct.start;u.touchEnd=Ct.end;u.touchMoved=Ct.moved}})");
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
