/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
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

class Resizable {
  private static Logger logger = LoggerFactory.getLogger(Resizable.class);

  public static void loadJavaScript(WApplication app) {
    app.loadJavaScript("js/Resizable.js", wtjs1());
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "Resizable",
        "(function(t,e){var i,n,o,s,d,h,u,p,x=null,a=null;function l(u){var p=t.pageCoordinates(u),l=p.x-a.x,c=p.y-a.y,r=t.px(e,\"width\"),m=t.px(e,\"height\"),f=Math.max(i+l,d+(i-o));e.style.width=f+\"px\";var g=Math.max(n+c,h+(n-s));e.style.height=g+\"px\";\"auto\"===e.style.left&&(e.style.right=t.px(e,\"right\")-(f-r)+\"px\");\"auto\"===e.style.top&&(e.style.bottom=t.px(e,\"bottom\")-(g-m)+\"px\");x&&x(f,g)}function c(i){$(window.document).unbind(\"mousemove\",l);$(window.document).unbind(\"mouseup\",c);x&&x(t.pxself(e,\"width\"),t.pxself(e,\"height\"),!0)}$(e).mousedown((function(x){var r=t.widgetCoordinates(e,x);if(e.offsetWidth-r.x<16&&e.offsetHeight-r.y<16){if(!u){u=t.css(e,\"minWidth\"),p=t.css(e,\"minHeight\");if(t.isIE6){function m(t,e){var i=new RegExp(e+\":\\\\s*(\\\\d+(?:\\\\.\\\\d+)?)\\\\s*px\",\"i\").exec(t.style.cssText);return i&&2==i.length?i[1]+\"px\":\"\"}u=m(e,\"min-width\");p=m(e,\"min-height\")}d=\"0px\"==u?e.clientWidth:t.parsePx(u);h=\"0px\"==p?e.clientHeight:t.parsePx(p)}a=t.pageCoordinates(x);i=t.innerWidth(e);n=t.innerHeight(e);o=e.clientWidth;s=e.clientHeight;t.capture(null);$(window.document).bind(\"mousemove\",l);$(window.document).bind(\"mouseup\",c)}}));this.onresize=function(t){x=t}})");
  }
}
