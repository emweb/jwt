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

class ResizeSensor {
  private static Logger logger = LoggerFactory.getLogger(ResizeSensor.class);

  public static void applyIfNeeded(WWidget w) {
    if (w.getJavaScriptMember(WWidget.WT_RESIZE_JS).length() != 0) {
      WApplication app = WApplication.getInstance();
      loadJavaScript(app);
      w.setJavaScriptMember(" ResizeSensor", "");
      w.setJavaScriptMember(
          " ResizeSensor", "new Wt4_8_1.ResizeSensor(Wt4_8_1," + w.getJsRef() + ")");
    }
  }

  public static void loadJavaScript(WApplication app) {
    app.loadJavaScript("js/ResizeSensor.js", wtjs1());
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "ResizeSensor",
        "(function(e,i){var t=window.requestAnimationFrame||window.mozRequestAnimationFrame||window.webkitRequestAnimationFrame||function(e){return window.setTimeout(e,20)};i.resizeSensor=document.createElement(\"div\");i.resizeSensor.className=\"resize-sensor\";var s=\"position: absolute; left: 0; top: 0; right: 0; bottom: 0; overflow: hidden; z-index: -1; visibility: hidden;\",o=\"position: absolute; left: 0; top: 0; transition: 0s;\";i.resizeSensor.style.cssText=s;i.resizeSensor.innerHTML='<div class=\"resize-sensor-expand\" style=\"'+s+'\"><div style=\"'+o+'\"></div></div><div class=\"resize-sensor-shrink\" style=\"'+s+'\"><div style=\"'+o+' width: 200%; height: 200%\"></div></div>';i.appendChild(i.resizeSensor);\"static\"==e.css(i,\"position\")&&(i.style.position=\"relative\");var n=i.resizeSensor.childNodes[0],r=n.childNodes[0],d=i.resizeSensor.childNodes[1],a=!0,l=0,c=function(){if(a){0===i.offsetWidth&&0===i.offsetHeight?l||(l=t((function(){l=0;c()}))):a=!1}r.style.width=\"100000px\";r.style.height=\"100000px\";n.scrollLeft=1e5;n.scrollTop=1e5;d.scrollLeft=1e5;d.scrollTop=1e5};i.resizeSensor.trigger=function(){var t=f,s=p;if(!e.boxSizing(i)){s-=e.px(i,\"borderTopWidth\");s-=e.px(i,\"borderBottomWidth\");s-=e.px(i,\"paddingTop\");s-=e.px(i,\"paddingBottom\");t-=e.px(i,\"borderLeftWidth\");t-=e.px(i,\"borderRightWidth\");t-=e.px(i,\"paddingLeft\");t-=e.px(i,\"paddingRight\")}i.wtResize&&i.wtResize(i,t,s,!1)};c();var f,p,h,v,u=!1,z=function(){if(u){i.resizeSensor.trigger();u=!1}t(z)};t(z);var g=function(){if((h=i.offsetWidth)!=f||(v=i.offsetHeight)!=p){u=!0;f=h;p=v}c()},m=function(e,i,t){e.attachEvent?e.attachEvent(\"on\"+i,t):e.addEventListener(i,t)};m(n,\"scroll\",g);m(d,\"scroll\",g);l=t((function(){l=0;c()}))})");
  }
}
