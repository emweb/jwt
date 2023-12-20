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
          " ResizeSensor", "new Wt4_10_3.ResizeSensor(Wt4_10_3," + w.getJsRef() + ")");
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
        "(function(e,i){i.resizeSensor=document.createElement(\"div\");i.resizeSensor.className=\"resize-sensor\";const t=\"position: absolute; left: 0; top: 0; right: 0; bottom: 0; overflow: hidden; z-index: -1; visibility: hidden;\",s=\"position: absolute; left: 0; top: 0; transition: 0s;\";i.resizeSensor.style.cssText=t;i.resizeSensor.innerHTML='<div class=\"resize-sensor-expand\" style=\"'+t+'\"><div style=\"'+s+'\"></div></div><div class=\"resize-sensor-shrink\" style=\"'+t+'\"><div style=\"'+s+' width: 200%; height: 200%\"></div></div>';i.appendChild(i.resizeSensor);\"static\"===e.css(i,\"position\")&&(i.style.position=\"relative\");const o=i.resizeSensor.childNodes[0],n=o.childNodes[0],r=i.resizeSensor.childNodes[1];let d=!0,l=0;const c=function(){if(d){0===i.offsetWidth&&0===i.offsetHeight?l||(l=requestAnimationFrame((function(){l=0;c()}))):d=!1}n.style.width=\"100000px\";n.style.height=\"100000px\";o.scrollLeft=1e5;o.scrollTop=1e5;r.scrollLeft=1e5;r.scrollTop=1e5};i.resizeSensor.trigger=function(){let t=a,s=p;if(!e.boxSizing(i)){s-=e.px(i,\"borderTopWidth\");s-=e.px(i,\"borderBottomWidth\");s-=e.px(i,\"paddingTop\");s-=e.px(i,\"paddingBottom\");t-=e.px(i,\"borderLeftWidth\");t-=e.px(i,\"borderRightWidth\");t-=e.px(i,\"paddingLeft\");t-=e.px(i,\"paddingRight\")}i.wtResize&&i.wtResize(i,t,s,!1)};c();let a,p,f=!1;const h=function(){if(f){i.resizeSensor.trigger();f=!1}requestAnimationFrame(h)};requestAnimationFrame(h);let z,g;const u=function(){if((z=i.offsetWidth)!==a||(g=i.offsetHeight)!==p){f=!0;a=z;p=g}c()};o.addEventListener(\"scroll\",u);r.addEventListener(\"scroll\",u);l=requestAnimationFrame((function(){l=0;c()}))})");
  }
}
