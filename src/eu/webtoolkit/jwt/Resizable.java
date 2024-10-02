/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
        "(function(t,e){let i,n,s,o,h,d,c,u,p=null,x=null;function l(c){const u=t.pageCoordinates(c),l=u.x-x.x,r=u.y-x.y,m=t.px(e,\"width\"),a=t.px(e,\"height\"),f=Math.max(i+l,h+(i-s));e.style.width=f+\"px\";const g=Math.max(n+r,d+(n-o));e.style.height=g+\"px\";\"auto\"===e.style.left&&(e.style.right=t.px(e,\"right\")-(f-m)+\"px\");\"auto\"===e.style.top&&(e.style.bottom=t.px(e,\"bottom\")-(g-a)+\"px\");p&&p(f,g)}function r(i){document.removeEventListener(\"mousemove\",l);document.removeEventListener(\"mouseup\",r);p&&p(t.pxself(e,\"width\"),t.pxself(e,\"height\"),!0)}e.addEventListener(\"mousedown\",(function(p){const m=t.widgetCoordinates(e,p);if(e.offsetWidth-m.x<16&&e.offsetHeight-m.y<16){if(!c){c=t.css(e,\"minWidth\");u=t.css(e,\"minHeight\");if(t.isIE6){function a(t,e){const i=new RegExp(e+\":\\\\s*(\\\\d+(?:\\\\.\\\\d+)?)\\\\s*px\",\"i\").exec(t.style.cssText);return i&&2===i.length?i[1]+\"px\":\"\"}c=a(e,\"min-width\");u=a(e,\"min-height\")}h=\"0px\"===c?e.clientWidth:t.parsePx(c);d=\"0px\"===u?e.clientHeight:t.parsePx(u)}x=t.pageCoordinates(p);i=t.innerWidth(e);n=t.innerHeight(e);s=e.clientWidth;o=e.clientHeight;t.capture(null);document.addEventListener(\"mousemove\",l);document.addEventListener(\"mouseup\",r)}}));this.onresize=function(t){p=t}})");
  }
}
