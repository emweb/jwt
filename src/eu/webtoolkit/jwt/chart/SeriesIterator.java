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
   * seperately, but each time with a different clipping region specified in the painter,
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
      int colorRole) {
    WColor color = null;
    if (yRow >= 0 && yColumn >= 0) {
      if (colorRole == ItemDataRole.MarkerPenColorRole) {
        color = series.getModel().getMarkerPenColor(yRow, yColumn);
      } else {
        if (colorRole == ItemDataRole.MarkerBrushColorRole) {
          color = series.getModel().getMarkerBrushColor(yRow, yColumn);
        }
      }
    }
    if (!(color != null) && xRow >= 0 && xColumn >= 0) {
      if (colorRole == ItemDataRole.MarkerPenColorRole) {
        color = series.getModel().getMarkerPenColor(xRow, xColumn);
      } else {
        if (colorRole == ItemDataRole.MarkerBrushColorRole) {
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
      int colorRole) {
    WColor color = null;
    if (yRow >= 0 && yColumn >= 0) {
      if (colorRole == ItemDataRole.MarkerBrushColorRole) {
        color = series.getModel().getMarkerBrushColor(yRow, yColumn);
      } else {
        if (colorRole == ItemDataRole.BarBrushColorRole) {
          color = series.getModel().getBarBrushColor(yRow, yColumn);
        }
      }
    }
    if (!(color != null) && xRow >= 0 && xColumn >= 0) {
      if (colorRole == ItemDataRole.MarkerBrushColorRole) {
        color = series.getModel().getMarkerBrushColor(xRow, xColumn);
      } else {
        if (colorRole == ItemDataRole.BarBrushColorRole) {
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
        "function(B){function F(a,b,c,e){function f(n){return c?b[n]:b[k-1-n]}function h(n){for(;f(n)[2]===y||f(n)[2]===C;)n--;return n}var i=l;if(e)i=m;var k=b.length;e=Math.floor(k/2);e=h(e);var q=0,o=k,d=false;if(f(0)[i]>a)return c?-1:k;if(f(k-1)[i]<a)return c?k:-1;for(;!d;){var g=e+1;if(g<k&&(f(g)[2]===y||f(g)[2]===C))g+=2;if(f(e)[i]>a){o=e;e=Math.floor((o+q)/2);e=h(e)}else if(f(e)[i]===a)d=true;else if(g<k&&f(g)[i]>a)d=true;else if(g<k&&f(g)[i]=== a){e=g;d=true}else{q=e;e=Math.floor((o+q)/2);e=h(e)}}return c?e:k-1-e}function H(a,b){return b[0][a]<b[b.length-1][a]}var y=2,C=3,l=0,m=1,G=this;B=B.WT.gfxUtils;var z=B.rect_top,A=B.rect_bottom,v=B.rect_left,D=B.rect_right,I=B.transform_mult;this.findClosestPoint=function(a,b,c){var e=l;if(c)e=m;var f=H(e,b);c=F(a,b,f,c);if(c<0)c=0;if(c>=b.length)return[b[b.length-1][l],b[b.length-1][m]];if(c>=b.length)c=b.length-2;if(b[c][e]===a)return[b[c][l],b[c][m]];var h=f?c+1:c-1;if(f&&b[h][2]==y)h+=2;if(!f&& h<0)return[b[c][l],b[c][m]];if(!f&&h>0&&b[h][2]==C)h-=2;f=Math.abs(a-b[c][e]);a=Math.abs(b[h][e]-a);return f<a?[b[c][l],b[c][m]]:[b[h][l],b[h][m]]};this.minMaxY=function(a,b){b=b?l:m;for(var c=a[0][b],e=a[0][b],f=1;f<a.length;++f)if(a[f][2]!==y&&a[f][2]!==C&&a[f][2]!==5){if(a[f][b]>e)e=a[f][b];if(a[f][b]<c)c=a[f][b]}return[c,e]};this.projection=function(a,b){var c=Math.cos(a);a=Math.sin(a);var e=c*a,f=-b[0]*c-b[1]*a;return[c*c,e,e,a*a,c*f+b[0],a*f+b[1]]};this.distanceSquared=function(a,b){a=[b[l]- a[l],b[m]-a[m]];return a[l]*a[l]+a[m]*a[m]};this.distanceLessThanRadius=function(a,b,c){return c*c>=G.distanceSquared(a,b)};this.toZoomLevel=function(a){return Math.floor(Math.log(a)/Math.LN2+0.5)+1};this.isPointInRect=function(a,b){var c;if(a.x!==undefined){c=a.x;a=a.y}else{c=a[0];a=a[1]}return c>=v(b)&&c<=D(b)&&a>=z(b)&&a<=A(b)};this.toDisplayCoord=function(a,b,c,e,f){if(c){a=[(a[l]-f[0])/f[2],(a[m]-f[1])/f[3]];e=[e[0]+a[m]*e[2],e[1]+a[l]*e[3]]}else{a=[(a[l]-f[0])/f[2],1-(a[m]-f[1])/f[3]];e=[e[0]+ a[l]*e[2],e[1]+a[m]*e[3]]}return I(b,e)};this.findYRange=function(a,b,c,e,f,h,i,k,q){if(a.length!==0){var o=G.toDisplayCoord([c,0],[1,0,0,1,0,0],f,h,i),d=G.toDisplayCoord([e,0],[1,0,0,1,0,0],f,h,i),g=f?m:l,n=f?l:m,p=H(g,a),j=F(o[g],a,p,f),r=F(d[g],a,p,f),s,t,w=Infinity,x=-Infinity,E=j===r&&j===a.length||j===-1&&r===-1;if(!E){if(p)if(j<0)j=0;else{j++;if(a[j]&&a[j][2]===y)j+=2}else if(j>=a.length-1)j=a.length-2;if(!p&&r<0)r=0;for(s=Math.min(j,r);s<=Math.max(j,r)&&s<a.length;++s)if(a[s][2]!==y&&a[s][2]!== C){if(a[s][n]<w)w=a[s][n];if(a[s][n]>x)x=a[s][n]}if(p&&j>0||!p&&j<a.length-1){if(p){t=j-1;if(a[t]&&a[t][2]===C)t-=2}else{t=j+1;if(a[t]&&a[t][2]===y)t+=2}s=(o[g]-a[t][g])/(a[j][g]-a[t][g]);j=a[t][n]+s*(a[j][n]-a[t][n]);if(j<w)w=j;if(j>x)x=j}if(p&&r<a.length-1||!p&&r>0){if(p){p=r+1;if(a[p][2]===y)p+=2}else{p=r-1;if(a[p][2]===C)p-=2}s=(d[g]-a[r][g])/(a[p][g]-a[r][g]);j=a[r][n]+s*(a[p][n]-a[r][n]);if(j<w)w=j;if(j>x)x=j}}var u;a=i[2]/(e-c);c=f?2:3;if(!E){u=h[c]/(x-w);u=h[c]/(h[c]/u+20);if(u>q.y[b])u=q.y[b]; if(u<k.y[b])u=k.y[b]}b=f?[o[m]-z(h),!E?(w+x)/2-h[2]/u/2-v(h):0]:[o[l]-v(h),!E?-((w+x)/2+h[3]/u/2-A(h)):0];return{xZoom:a,yZoom:u,panPoint:b}}};this.matchXAxis=function(a,b,c,e,f){function h(){return e.length}function i(g){return e[g].side}function k(g){return e[g].width}function q(g){return e[g].minOffset}function o(g){return e[g].maxOffset}if(f){if(b<z(c)||b>A(c))return-1}else if(a<v(c)||a>D(c))return-1;for(var d=0;d<h();++d)if(f)if((i(d)===\"min\"||i(d)===\"both\")&&a>=v(c)-q(d)-k(d)&&a<=v(c)-q(d))return d; else{if((i(d)===\"max\"||i(d)===\"both\")&&a>=D(c)+o(d)&&a<=D(c)+o(d)+k(d))return d}else if((i(d)===\"min\"||i(d)===\"both\")&&b<=A(c)+q(d)+k(d)&&b>=A(c)+q(d))return d;else if((i(d)===\"max\"||i(d)===\"both\")&&b<=z(c)-o(d)&&b>=z(c)-o(d)-k(d))return d;return-1};this.matchYAxis=function(a,b,c,e,f){function h(){return e.length}function i(g){return e[g].side}function k(g){return e[g].width}function q(g){return e[g].minOffset}function o(g){return e[g].maxOffset}if(f){if(a<v(c)||a>D(c))return-1}else if(b<z(c)||b> A(c))return-1;for(var d=0;d<h();++d)if(f)if((i(d)===\"min\"||i(d)===\"both\")&&b>=z(c)-q(d)-k(d)&&b<=z(c)-q(d))return d;else{if((i(d)===\"max\"||i(d)===\"both\")&&b>=A(c)+o(d)&&b<=A(c)+o(d)+k(d))return d}else if((i(d)===\"min\"||i(d)===\"both\")&&a>=v(c)-q(d)-k(d)&&a<=v(c)-q(d))return d;else if((i(d)===\"max\"||i(d)===\"both\")&&a>=D(c)+o(d)&&a<=D(c)+o(d)+k(d))return d;return-1}}");
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WCartesianChart",
        "function(za,K,A,k){function N(a){return a===undefined}function Eb(a){return k.xModelAreas[a]}function Fb(a){return k.yModelAreas[a]}function U(a,b){a=Eb(a);b=Fb(b);return D()?[b[0],a[1],b[2],a[3]]:[a[0],b[1],a[2],b[3]]}function ga(){return k.followCurve}function Aa(){return k.crosshair||ga()!==-1}function D(){return k.isHorizontal}function i(a){return k.xTransforms[a]}function h(a){return k.yTransforms[a]}function j(){return k.area} function l(){return k.insideArea}function V(a){return N(a)?k.series:k.series[a]}function ha(a){return V(a).transform}function mb(a){return D()?z([0,1,1,0,0,0],z(ha(a),[0,1,1,0,0,0])):ha(a)}function Sa(a){return V(a).curve}function ma(a){return V(a).xAxis}function P(a){return V(a).yAxis}function nb(){return k.seriesSelection}function ob(){return k.sliders}function pb(){return k.hasToolTips}function qb(){return k.coordinateOverlayPadding}function Ja(){return k.curveManipulation}function rb(a){return k.minZoom.x[a]} function sb(a){return k.minZoom.y[a]}function Q(a){return k.maxZoom.x[a]}function S(a){return k.maxZoom.y[a]}function H(){return k.pens}function tb(){return k.penAlpha}function W(){return k.selectedCurve}function Ba(a){a.preventDefault&&a.preventDefault()}function ia(a,b){K.addEventListener(a,b)}function X(a,b){K.removeEventListener(a,b)}function w(a){return a.length}function I(){return w(k.xAxes)}function M(){return w(k.yAxes)}function Gb(){for(var a=0;a<I();++a)if(k.notifyTransform.x[a])return true; for(a=0;a<M();++a)if(k.notifyTransform.y[a])return true;return false}function Y(){return k.crosshairXAxis}function Z(){return k.crosshairYAxis}function eb(a){return a.pointerType===2||a.pointerType===3||a.pointerType===\"pen\"||a.pointerType===\"touch\"}function Ta(){if(p){if(p.tooltipTimeout){clearTimeout(p.tooltipTimeout);p.tooltipTimeout=null}if(!p.overTooltip)if(p.tooltipOuterDiv){document.body.removeChild(p.tooltipOuterDiv);p.toolTipEl=null;p.tooltipOuterDiv=null}}}function Ka(){if(Gb()){if(Ua){window.clearTimeout(Ua); Ua=null}Ua=setTimeout(function(){for(var a=0;a<I();++a)if(k.notifyTransform.x[a]&&!ub(Va[a],i(a))){za.emit(A.widget,\"xTransformChanged\"+a);na(Va[a],i(a))}for(a=0;a<M();++a)if(k.notifyTransform.y[a]&&!ub(Wa[a],h(a))){za.emit(A.widget,\"yTransformChanged\"+a);na(Wa[a],h(a))}},Hb)}}function oa(a,b){if(a===undefined)a=0;if(b===undefined)b=0;var c,d;if(D()){c=o(j());d=s(j());return z([0,1,1,0,c,d],z(i(a),z(h(b),[0,1,1,0,-d,-c])))}else{c=o(j());d=t(j());return z([1,0,0,-1,c,d],z(i(a),z(h(b),[1,0,0,-1,-c, d])))}}function B(a,b){return z(oa(a,b),l())}function pa(a,b,c,d){if(N(d))d=false;a=d?a:z(La(oa(b,c)),a);a=D()?[(a[x]-j()[1])/j()[3],(a[y]-j()[0])/j()[2]]:[(a[y]-j()[0])/j()[2],1-(a[x]-j()[1])/j()[3]];return[U(b,c)[0]+a[y]*U(b,c)[2],U(b,c)[1]+a[x]*U(b,c)[3]]}function Xa(a,b,c,d){if(N(d))d=false;return aa.toDisplayCoord(a,d?[1,0,0,1,0,0]:oa(b,c),D(),j(),U(b,c))}function Ma(){for(var a=0;a<I();++a){var b,c;c=U(a,0);if(D()){b=(pa([0,s(j())],a,0)[0]-c[0])/c[2];c=(pa([0,t(j())],a,0)[0]-c[0])/c[2]}else{b= (pa([o(j()),0],a,0)[0]-c[0])/c[2];c=(pa([q(j()),0],a,0)[0]-c[0])/c[2]}var d;for(d=0;d<w(ob());++d){var e=$(\"#\"+ob()[d]);if(e)(e=e.data(\"sobj\"))&&e.xAxis===a&&e.changeRange(b,c)}}}function ba(){Ta();if(pb()&&p.tooltipPosition)p.tooltipTimeout=setTimeout(function(){vb()},wb);qa&&xb(function(){A.repaint();Aa()&&fb()})}function fb(){if(qa){var a=J.getContext(\"2d\");a.clearRect(0,0,J.width,J.height);a.save();a.beginPath();a.moveTo(o(j()),s(j()));a.lineTo(q(j()),s(j()));a.lineTo(q(j()),t(j()));a.lineTo(o(j()), t(j()));a.closePath();a.clip();var b=z(La(oa(Y(),Z())),C),c=C[y],d=C[x];if(ga()!==-1){b=Ib(D()?b[x]:b[y],Sa(ga()),D());d=z(oa(ma(ga()),P(ga())),z(mb(ga()),b));c=d[y];d=d[x];C[y]=c;C[x]=d}b=D()?[(b[x]-j()[1])/j()[3],(b[y]-j()[0])/j()[2]]:[(b[y]-j()[0])/j()[2],1-(b[x]-j()[1])/j()[3]];var e=ga()!==-1?U(ma(ga()),P(ga())):U(Y(),Z());b=[e[0]+b[y]*e[2],e[1]+b[x]*e[3]];a.fillStyle=a.strokeStyle=k.crosshairColor;a.font=\"16px sans-serif\";a.textAlign=\"right\";a.textBaseline=\"top\";e=b[0].toFixed(2);b=b[1].toFixed(2); if(e===\"-0.00\")e=\"0.00\";if(b===\"-0.00\")b=\"0.00\";a.fillText(\"(\"+e+\",\"+b+\")\",q(j())-qb()[0],s(j())+qb()[1]);a.setLineDash&&a.setLineDash([1,2]);a.beginPath();a.moveTo(Math.floor(c)+0.5,Math.floor(s(j()))+0.5);a.lineTo(Math.floor(c)+0.5,Math.floor(t(j()))+0.5);a.moveTo(Math.floor(o(j()))+0.5,Math.floor(d)+0.5);a.lineTo(Math.floor(q(j()))+0.5,Math.floor(d)+0.5);a.stroke();a.restore()}}function Jb(a){return s(a)<=s(l())+Ya&&t(a)>=t(l())-Ya&&o(a)<=o(l())+Ya&&q(a)>=q(l())-Ya}function ja(a){var b;if(D())if(a=== Ca)a=Da;else if(a===Da)a=Ca;for(var c=0;c<I();++c){b=B(c,0);if(D()){if(N(a)||a===Da){if(i(c)[0]<1){i(c)[0]=1;b=B(c,0)}if(s(b)>s(l())){b=s(l())-s(b);i(c)[4]=i(c)[4]+b}else if(t(b)<t(l())){b=t(l())-t(b);i(c)[4]=i(c)[4]+b}}}else if(N(a)||a===Ca){if(i(c)[0]<1){i(c)[0]=1;b=B(c,0)}if(o(b)>o(l())){b=o(l())-o(b);i(c)[4]=i(c)[4]+b}else if(q(b)<q(l())){b=q(l())-q(b);i(c)[4]=i(c)[4]+b}}}for(c=0;c<M();++c){b=B(0,c);if(D()){if(N(a)||a===Ca){if(h(c)[3]<1){h(c)[3]=1;b=B(0,c)}if(o(b)>o(l())){b=o(l())-o(b);h(c)[5]= h(c)[5]+b}else if(q(b)<q(l())){b=q(l())-q(b);h(c)[5]=h(c)[5]+b}}}else if(N(a)||a===Da){if(h(c)[3]<1){h(c)[3]=1;b=B(0,c)}if(s(b)>s(l())){b=s(l())-s(b);h(c)[5]=h(c)[5]-b}else if(t(b)<t(l())){b=t(l())-t(b);h(c)[5]=h(c)[5]-b}}}Ka()}function vb(){p.toolTipEl||za.emit(A.widget,\"loadTooltip\",p.tooltipPosition[y],p.tooltipPosition[x])}function Kb(){if(Aa()&&(N(J)||A.canvas.width!==J.width||A.canvas.height!==J.height)){if(J){J.parentNode.removeChild(J);delete K.wtOObj;J=undefined}var a=document.createElement(\"canvas\"); a.setAttribute(\"width\",A.canvas.width);a.setAttribute(\"height\",A.canvas.height);a.style.position=\"absolute\";a.style.display=\"block\";a.style.left=\"0\";a.style.top=\"0\";if(window.MSPointerEvent||window.PointerEvent){a.style.msTouchAction=\"none\";a.style.touchAction=\"none\"}A.canvas.parentNode.appendChild(a);J=a;K.wtOObj=J}else if(!N(J)&&!Aa()){J.parentNode.removeChild(J);delete K.wtOObj;J=undefined}C=[(o(j())+q(j()))/2,(s(j())+t(j()))/2]}function yb(){return J?J:A.canvas}function gb(a,b){if(Ea){var c=Date.now(); if(N(b))b=c-ra;var d={x:0,y:0},e;if(u!==-1)e=B(u,0);else if(v===-1){e=B(0,0);for(var f=1;f<I();++f)e=sa(e,B(f,0));for(f=1;f<M();++f)e=sa(e,B(0,f))}else e=B(0,v);f=Lb;if(b>2*Na){qa=false;var g=Math.floor(b/Na-1),n;for(n=0;n<g;++n){gb(a,Na);if(!Ea){qa=true;ba();return}}b-=g*Na;qa=true}if(m.x===Infinity||m.x===-Infinity)m.x=m.x>0?ta:-ta;if(isFinite(m.x)){m.x/=1+zb*b;e[0]+=m.x*b;if(o(e)>o(l())){m.x+=-f*(o(e)-o(l()))*b;m.x*=0.7}else if(q(e)<q(l())){m.x+=-f*(q(e)-q(l()))*b;m.x*=0.7}if(Math.abs(m.x)<hb)if(o(e)> o(l()))m.x=hb;else if(q(e)<q(l()))m.x=-hb;if(Math.abs(m.x)>ta)m.x=(m.x>0?1:-1)*ta;d.x=m.x*b}if(m.y===Infinity||m.y===-Infinity)m.y=m.y>0?ta:-ta;if(isFinite(m.y)){m.y/=1+zb*b;e[1]+=m.y*b;if(s(e)>s(l())){m.y+=-f*(s(e)-s(l()))*b;m.y*=0.7}else if(t(e)<t(l())){m.y+=-f*(t(e)-t(l()))*b;m.y*=0.7}if(Math.abs(m.y)<0.001)if(s(e)>s(l()))m.y=0.001;else if(t(e)<t(l()))m.y=-0.001;if(Math.abs(m.y)>ta)m.y=(m.y>0?1:-1)*ta;d.y=m.y*b}if(u!==-1)e=B(u,0);else if(v===-1){e=B(0,0);for(f=1;f<I();++f)e=sa(e,B(f,0));for(f= 1;f<M();++f)e=sa(e,B(0,f))}else e=B(0,v);ca(d,Fa,u,v);if(u!==-1)a=B(u,0);else if(v===-1){a=B(0,0);for(f=1;f<I();++f)a=sa(a,B(f,0));for(f=1;f<M();++f)a=sa(a,B(0,f))}else a=B(0,v);if(o(e)>o(l())&&o(a)<=o(l())){m.x=0;ca({x:-d.x,y:0},Fa,u,v);ja(Ca)}if(q(e)<q(l())&&q(a)>=q(l())){m.x=0;ca({x:-d.x,y:0},Fa,u,v);ja(Ca)}if(s(e)>s(l())&&s(a)<=s(l())){m.y=0;ca({x:0,y:-d.y},Fa,u,v);ja(Da)}if(t(e)<t(l())&&t(a)>=t(l())){m.y=0;ca({x:0,y:-d.y},Fa,u,v);ja(Da)}if(Math.abs(m.x)<Ab&&Math.abs(m.y)<Ab&&Jb(a)){ja();Ea=false; E=null;m.x=0;m.y=0;ra=null;r=[]}else{ra=c;qa&&Za(gb)}}}function $a(){for(var a,b,c=0;c<w(H().x);++c){var d=Bb(i(c)[0])-1;if(i(c)[0]==Q(c))d=w(H().x[c])-1;if(d>=w(H().x[c]))d=w(H().x[c])-1;for(a=0;a<w(H().x[c]);++a)if(d===a)for(b=0;b<w(H().x[c][a]);++b)H().x[c][a][b].color[3]=tb().x[c][b];else for(b=0;b<w(H().x[c][a]);++b)H().x[c][a][b].color[3]=0}for(c=0;c<w(H().y);++c){d=Bb(h(c)[3])-1;if(h(c)[3]==S(c))d=w(H().y[c])-1;if(d>=w(H().y[c]))d=w(H().y[c])-1;for(a=0;a<w(H().y[c]);++a)if(d===a)for(b=0;b< w(H().y[c][a]);++b)H().y[c][a][b].color[3]=tb().y[c][b];else for(b=0;b<w(H().y[c][a]);++b)H().y[c][a][b].color[3]=0}}function ca(a,b,c,d){if(N(b))b=0;if(N(c))c=-1;if(N(d))d=-1;var e=pa(C,Y(),Z());if(D())a={x:a.y,y:-a.x};if(b&Fa){if(c!==-1)i(c)[4]=i(c)[4]+a.x;else if(d===-1){for(var f=0;f<I();++f)i(f)[4]=i(f)[4]+a.x;for(f=0;f<M();++f)h(f)[5]=h(f)[5]-a.y}else h(d)[5]=h(d)[5]-a.y;Ka()}else if(b&Cb){if(c!==-1)b=B(c,0);else if(d===-1){b=B(0,0);for(f=1;f<I();++f)b=sa(b,B(f,0));for(f=1;f<M();++f)b=sa(b, B(0,f))}else b=B(0,d);if(o(b)>o(l())){if(a.x>0)a.x/=1+(o(b)-o(l()))*ab}else if(q(b)<q(l()))if(a.x<0)a.x/=1+(q(l())-q(b))*ab;if(s(b)>s(l())){if(a.y>0)a.y/=1+(s(b)-s(l()))*ab}else if(t(b)<t(l()))if(a.y<0)a.y/=1+(t(l())-t(b))*ab;if(c!==-1)i(c)[4]=i(c)[4]+a.x;else if(d===-1){for(f=0;f<I();++f)i(f)[4]=i(f)[4]+a.x;for(f=0;f<M();++f)h(f)[5]=h(f)[5]-a.y}else h(d)[5]=h(d)[5]-a.y;if(d===-1)C[y]+=a.x;if(c===-1)C[x]+=a.y;Ka()}else{if(c!==-1)i(c)[4]=i(c)[4]+a.x;else if(d===-1){for(f=0;f<I();++f)i(f)[4]=i(f)[4]+ a.x;for(f=0;f<M();++f)h(f)[5]=h(f)[5]-a.y}else h(d)[5]=h(d)[5]-a.y;if(d===-1)C[y]+=a.x;if(c===-1)C[x]+=a.y;ja()}a=Xa(e,Y(),Z());C[y]=a[y];C[x]=a[x];ba();Ma()}function Oa(a,b,c,d,e){if(N(d))d=-1;if(N(e))e=-1;var f=pa(C,Y(),Z());a=D()?[a.y-s(j()),a.x-o(j())]:z(La([1,0,0,-1,o(j()),t(j())]),[a.x,a.y]);var g=a[0];a=a[1];var n=Math.pow(1.2,D()?c:b);b=Math.pow(1.2,D()?b:c);if(d!==-1){if(i(d)[0]*n>Q(d))n=Q(d)/i(d)[0];if(n<1||i(d)[0]!=Q(d))ua(i(d),z([n,0,0,1,g-n*g,0],i(d)))}else if(e===-1){for(d=0;d<I();++d){e= n;if(i(d)[0]*n>Q(d))e=Q(d)/i(d)[0];if(e<1||i(d)[0]!==Q(d))ua(i(d),z([e,0,0,1,g-e*g,0],i(d)))}for(g=0;g<M();++g){d=b;if(h(g)[3]*b>S(g))d=S(g)/h(g)[3];if(d<1||h(g)[3]!==S(g))ua(h(g),z([1,0,0,d,0,a-d*a],h(g)))}}else{if(h(e)[3]*b>S(e))b=S(e)/h(e)[3];if(b<1||h(e)[3]!=S(e))ua(h(e),z([1,0,0,b,0,a-b*a],h(e)))}ja();f=Xa(f,Y(),Z());C[y]=f[y];C[x]=f[x];$a();ba();Ma()}K.wtCObj=this;var ka=this,F=za.WT;ka.config=k;var G=F.gfxUtils,z=G.transform_mult,La=G.transform_inverted,na=G.transform_assign,ub=G.transform_equal, Mb=G.transform_apply,s=G.rect_top,t=G.rect_bottom,o=G.rect_left,q=G.rect_right,sa=G.rect_intersection,aa=F.chartCommon,Nb=aa.minMaxY,Ib=aa.findClosestPoint,Ob=aa.projection,Db=aa.distanceLessThanRadius,Bb=aa.toZoomLevel,Pa=aa.isPointInRect,Pb=aa.findYRange,Qa=function(a,b){return aa.matchXAxis(a,b,j(),k.xAxes,D())},Ra=function(a,b){return aa.matchYAxis(a,b,j(),k.yAxes,D())},Na=17,Za=function(){return window.requestAnimationFrame||window.webkitRequestAnimationFrame||window.mozRequestAnimationFrame|| function(a){window.setTimeout(a,Na)}}(),ib=false,xb=function(a){if(!ib){ib=true;Za(function(){a();ib=false})}};if(window.MSPointerEvent||window.PointerEvent){K.style.touchAction=\"none\";A.canvas.style.msTouchAction=\"none\";A.canvas.style.touchAction=\"none\"}var Fa=1,Cb=2,Ca=1,Da=2,y=0,x=1,Hb=250,wb=500,zb=0.003,Lb=2.0E-4,ab=0.07,Ya=3,hb=0.001,ta=1.5,Ab=0.02,wa=K.wtEObj2;if(!wa){wa={};wa.contextmenuListener=function(a){Ba(a);X(\"contextmenu\",wa.contextmenuListener)}}K.wtEObj2=wa;var da={},xa=false;if(window.MSPointerEvent|| window.PointerEvent)(function(){function a(){xa=w(e)>0}function b(g){if(eb(g)){Ba(g);e.push(g);a();da.start(K,{touches:e.slice(0)})}}function c(g){if(xa)if(eb(g)){Ba(g);var n;for(n=0;n<w(e);++n)if(e[n].pointerId===g.pointerId){e.splice(n,1);break}a();da.end(K,{touches:e.slice(0),changedTouches:[]})}}function d(g){if(eb(g)){Ba(g);var n;for(n=0;n<w(e);++n)if(e[n].pointerId===g.pointerId){e[n]=g;break}a();da.moved(K,{touches:e.slice(0)})}}var e=[],f=K.wtEObj;if(f)if(window.PointerEvent){X(\"pointerdown\", f.pointerDown);X(\"pointerup\",f.pointerUp);X(\"pointerout\",f.pointerUp);X(\"pointermove\",f.pointerMove)}else{X(\"MSPointerDown\",f.pointerDown);X(\"MSPointerUp\",f.pointerUp);X(\"MSPointerOut\",f.pointerUp);X(\"MSPointerMove\",f.pointerMove)}K.wtEObj={pointerDown:b,pointerUp:c,pointerMove:d};if(window.PointerEvent){ia(\"pointerdown\",b);ia(\"pointerup\",c);ia(\"pointerout\",c);ia(\"pointermove\",d)}else{ia(\"MSPointerDown\",b);ia(\"MSPointerUp\",c);ia(\"MSPointerOut\",c);ia(\"MSPointerMove\",d)}})();var J=K.wtOObj,C=null,qa= true,E=null,u=-1,v=-1,r=[],la=false,ea=false,T=null,jb=null,kb=null,m={x:0,y:0},fa=null,ra=null,p=K.wtTObj;if(!p){p={overTooltip:false};K.wtTObj=p}var Ga=null,Ea=false,Ua=null,Va=[];for(G=0;G<I();++G){Va.push([0,0,0,0,0,0]);na(Va[G],i(G))}var Wa=[];for(G=0;G<M();++G){Wa.push([0,0,0,0,0,0]);na(Wa[G],h(G))}var ua=function(a,b){na(a,b);Ka()};A.combinedTransform=oa;this.updateTooltip=function(a){Ta();if(a)if(p.tooltipPosition){p.toolTipEl=document.createElement(\"div\");p.toolTipEl.className=k.ToolTipInnerStyle; p.toolTipEl.innerHTML=a;p.tooltipOuterDiv=document.createElement(\"div\");p.tooltipOuterDiv.className=k.ToolTipOuterStyle;document.body.appendChild(p.tooltipOuterDiv);p.tooltipOuterDiv.appendChild(p.toolTipEl);var b=F.widgetPageCoordinates(A.canvas);a=p.tooltipPosition[y]+b.x;b=p.tooltipPosition[x]+b.y;F.fitToWindow(p.tooltipOuterDiv,a+10,b+10,a-10,b-10);$(p.toolTipEl).mouseenter(function(){p.overTooltip=true});$(p.toolTipEl).mouseleave(function(){p.overTooltip=false})}};this.mouseMove=function(a,b){setTimeout(function(){setTimeout(Ta, 200);if(!xa){var c=F.widgetCoordinates(A.canvas,b);if(Pa(c,j())){if(pb()){p.tooltipPosition=[c.x,c.y];p.tooltipTimeout=setTimeout(function(){vb()},wb)}if(E===null&&Aa()&&qa){C=[c.x,c.y];xb(fb)}}}},0)};this.mouseOut=function(){setTimeout(Ta,200)};this.mouseDown=function(a,b){if(!xa){a=F.widgetCoordinates(A.canvas,b);b=Ra(a.x,a.y);var c=Pa(a,j()),d=Qa(a.x,a.y);if(!(b===-1&&d===-1&&!c)){E=a;u=d;v=b}}};this.mouseUp=function(){if(!xa){E=null;v=u=-1}};this.mouseDrag=function(a,b){if(!xa)if(E===null)ka.mouseDown(a, b);else{a=F.widgetCoordinates(A.canvas,b);if(F.buttons===1)if(v===-1&&u===-1&&Ja()&&V(W())){b=W();var c;c=D()?a.x-E.x:a.y-E.y;na(ha(b),z([1,0,0,1,0,c/h(P(W()))[3]],ha(b)));ba()}else k.pan&&ca({x:a.x-E.x,y:a.y-E.y},0,u,v);E=a}};this.clicked=function(a,b){if(!xa)if(E===null)if(nb()){a=F.widgetCoordinates(A.canvas,b);za.emit(A.widget,\"seriesSelected\",a.x,a.y)}};this.mouseWheel=function(a,b){var c=(b.metaKey<<3)+(b.altKey<<2)+(b.ctrlKey<<1)+b.shiftKey;a=k.wheelActions[c];if(!N(a)){var d=F.widgetCoordinates(A.canvas, b),e=Qa(d.x,d.y),f=Ra(d.x,d.y),g=Pa(d,j());if(!(e===-1&&f===-1&&!g)){var n=F.normalizeWheel(b);if(g&&c===0&&Ja()){c=W();g=-n.spinY;if(V(c)){a=mb(c);a=Mb(a,Sa(c));a=Nb(a,D());a=(a[0]+a[1])/2;F.cancelEvent(b);b=Math.pow(1.2,g);na(ha(c),z([1,0,0,b,0,a-b*a],ha(c)));ba();return}}if((a===4||a===5||a===6)&&k.pan){c=[];for(d=0;d<I();++d)c.push(i(d)[4]);g=[];for(d=0;d<M();++d)g.push(h(d)[5]);if(a===6)ca({x:-n.pixelX,y:-n.pixelY},0,e,f);else if(a===5)ca({x:0,y:-n.pixelX-n.pixelY},0,e,f);else a===4&&ca({x:-n.pixelX- n.pixelY,y:0},0,e,f);for(d=0;d<I();++d)c[d]!==i(d)[4]&&F.cancelEvent(b);for(d=0;d<M();++d)g[d]!==h(d)[5]&&F.cancelEvent(b)}else if(k.zoom){F.cancelEvent(b);g=-n.spinY;if(g===0)g=-n.spinX;if(a===1)Oa(d,0,g,e,f);else if(a===0)Oa(d,g,0,e,f);else if(a===2)Oa(d,g,g,e,f);else if(a===3)n.pixelX!==0?Oa(d,g,0,e,f):Oa(d,0,g,e,f)}}}};var Qb=function(){nb()&&za.emit(A.widget,\"seriesSelected\",E.x,E.y)};da.start=function(a,b,c){la=w(b.touches)===1;ea=w(b.touches)===2;if(la){Ea=false;var d=F.widgetCoordinates(A.canvas, b.touches[0]);a=Ra(d.x,d.y);var e=Pa(d,j()),f=Qa(d.x,d.y);if(a===-1&&f===-1&&!e)return;Ga=a===-1&&f===-1&&Aa()&&Db(C,[d.x,d.y],30)?1:0;ra=Date.now();E=d;v=a;u=f;if(Ga!==1){if(!c&&e)fa=window.setTimeout(Qb,200);ia(\"contextmenu\",wa.contextmenuListener)}F.capture(null);F.capture(yb())}else if(ea&&(k.zoom||Ja())){if(fa){window.clearTimeout(fa);fa=null}Ea=false;r=[F.widgetCoordinates(A.canvas,b.touches[0]),F.widgetCoordinates(A.canvas,b.touches[1])].map(function(g){return[g.x,g.y]});a=f=-1;if(!r.every(function(g){return Pa(g, j())})){f=Qa(r[0][y],r[0][x]);if(f!==-1){if(f!==Qa(r[1][y],r[1][x])){ea=null;return}}else{a=Ra(r[0][y],r[0][x]);if(a!==1){if(a!==Ra(r[1][y],r[1][x])){ea=null;return}}else{ea=null;return}}}F.capture(null);F.capture(yb());T=Math.atan2(r[1][1]-r[0][1],r[1][0]-r[0][0]);jb=[(r[0][0]+r[1][0])/2,(r[0][1]+r[1][1])/2];c=Math.abs(Math.sin(T));d=Math.abs(Math.cos(T));T=c<Math.sin(0.125*Math.PI)?0:d<Math.cos(0.375*Math.PI)?Math.PI/2:Math.tan(T)>0?Math.PI/4:-Math.PI/4;kb=Ob(T,jb);u=f;v=a}else return;Ba(b)};da.end= function(a,b){if(fa){window.clearTimeout(fa);fa=null}window.setTimeout(function(){X(\"contextmenu\",wa.contextmenuListener)},0);var c=Array.prototype.slice.call(b.touches),d=w(c)===0;d||function(){var e;for(e=0;e<w(b.changedTouches);++e)(function(){for(var f=b.changedTouches[e].identifier,g=0;g<w(c);++g)if(c[g].identifier===f){c.splice(g,1);return}})()}();d=w(c)===0;la=w(c)===1;ea=w(c)===2;if(d){bb=null;if(Ga===0&&(isFinite(m.x)||isFinite(m.y))&&k.rubberBand){ra=Date.now();Ea=true;Za(gb)}else{Ga=== 1&&ka.mouseUp(null,null);c=[];ra=kb=jb=T=null}Ga=null}else if(la||ea)da.start(a,b,true)};var bb=null,ya=null,lb=null;da.moved=function(a,b){if(la||ea)if(!(la&&E==null)){Ba(b);ya=F.widgetCoordinates(A.canvas,b.touches[0]);if(w(b.touches)>1)lb=F.widgetCoordinates(A.canvas,b.touches[1]);if(u===-1&&v===-1&&la&&fa&&!Db([ya.x,ya.y],[E.x,E.y],3)){window.clearTimeout(fa);fa=null}bb||(bb=setTimeout(function(){if(u===-1&&v===-1&&la&&Ja()&&V(W())){var c=W();if(V(c)){var d=ya,e;e=D()?(d.x-E.x)/h(P(W()))[3]:(d.y- E.y)/h(P(W()))[3];ha(c)[5]+=e;E=d;ba()}}else if(la){d=ya;e=Date.now();var f={x:d.x-E.x,y:d.y-E.y};c=e-ra;ra=e;if(Ga===1){C[y]+=f.x;C[x]+=f.y;Aa()&&qa&&Za(fb)}else if(k.pan){m.x=f.x/c;m.y=f.y/c;ca(f,k.rubberBand?Cb:0,u,v)}E=d}else if(u===-1&&v===-1&&ea&&Ja()&&V(W())){f=D()?y:x;e=[ya,lb].map(function(O){return D()?[O.x,va]:[Ha,O.y]});c=Math.abs(r[1][f]-r[0][f]);var g=Math.abs(e[1][f]-e[0][f]),n=c>0?g/c:1;if(g===c)n=1;c=W();if(V(c)){var va=z(La(oa(ma(c),P(c))),[0,(r[0][f]+r[1][f])/2])[1],Ia=z(La(oa(ma(c), P(c))),[0,(e[0][f]+e[1][f])/2])[1];na(ha(c),z([1,0,0,n,0,-n*va+Ia],ha(c)));E=d;ba();r=e}}else if(ea&&k.zoom){d=pa(C,Y(),Z());var Ha=(r[0][0]+r[1][0])/2;va=(r[0][1]+r[1][1])/2;e=[ya,lb].map(function(O){return T===0?[O.x,va]:T===Math.PI/2?[Ha,O.y]:z(kb,[O.x,O.y])});f=Math.abs(r[1][0]-r[0][0]);c=Math.abs(e[1][0]-e[0][0]);var cb=f>0?c/f:1;if(c===f||T===Math.PI/2)cb=1;var db=(e[0][0]+e[1][0])/2;c=Math.abs(r[1][1]-r[0][1]);g=Math.abs(e[1][1]-e[0][1]);n=c>0?g/c:1;if(g===c||T===0)n=1;Ia=(e[0][1]+e[1][1])/ 2;D()&&function(){var O=cb;cb=n;n=O;O=db;db=Ia;Ia=O;O=Ha;Ha=va;va=O}();f=[];for(g=0;g<I();++g)f.push(cb);for(g=0;g<I();++g){if(i(g)[0]*f[g]>Q(g))f[g]=Q(g)/i(g)[0];if(i(g)[0]*f[g]<rb(g))f[g]=rb(g)/i(g)[0]}c=[];for(g=0;g<M();++g)c.push(n);for(g=0;g<M();++g){if(h(g)[3]*c[g]>S(g))c[g]=S(g)/h(g)[3];if(h(g)[3]*c[g]<sb(g))c[g]=sb(g)/h(g)[3]}if(u!==-1){if(f[u]!==1&&(f[u]<1||i(u)[0]!==Q(u)))ua(i(u),z([f[u],0,0,1,-f[u]*Ha+db,0],i(u)))}else if(v===-1){for(g=0;g<I();++g)if(f[g]!==1&&(f[g]<1||i(g)[0]!==Q(g)))ua(i(g), z([f[g],0,0,1,-f[g]*Ha+db,0],i(g)));for(g=0;g<M();++g)if(c[g]!==1&&(c[g]<1||h(g)[3]!==S(g)))ua(h(g),z([1,0,0,c[g],0,-c[g]*va+Ia],h(g)))}else if(c[v]!==1&&(c[v]<1||h(v)[3]!==S(v)))ua(h(v),z([1,0,0,c[v],0,-c[v]*va+Ia],h(v)));ja();d=Xa(d,Y(),Z());C[y]=d[y];C[x]=d[x];r=e;$a();ba();Ma()}bb=null},1))}};this.setXRange=function(a,b,c,d){var e=ma(a);e=U(e,0);b=e[0]+e[2]*b;c=e[0]+e[2]*c;if(o(e)>q(e)){if(b>o(e))b=o(e);if(c<q(e))c=q(e)}else{if(b<o(e))b=o(e);if(c>q(e))c=q(e)}e=Sa(a);e=Pb(e,P(a),b,c,D(),j(),U(ma(a), P(a)),k.minZoom,k.maxZoom);b=e.xZoom;c=e.yZoom;e=e.panPoint;var f=pa(C,Y(),Z());i(ma(a))[0]=b;if(c&&d)h(P(a))[3]=c;i(ma(a))[4]=-e[y]*b;if(c&&d)h(P(a))[5]=-e[x]*c;Ka();a=Xa(f,Y(),Z());C[y]=a[y];C[x]=a[x];ja();$a();ba();Ma()};this.getSeries=function(a){return Sa(a)};this.rangeChangedCallbacks=[];this.updateConfig=function(a){for(var b in a)if(a.hasOwnProperty(b))k[b]=a[b];Kb();$a();ba();Ma()};this.updateConfig({});if(window.TouchEvent&&!window.MSPointerEvent&&!window.PointerEvent){ka.touchStart=da.start; ka.touchEnd=da.end;ka.touchMoved=da.moved}else{G=function(){};ka.touchStart=G;ka.touchEnd=G;ka.touchMoved=G}}");
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
