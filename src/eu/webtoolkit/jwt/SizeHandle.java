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

class SizeHandle {
  private static Logger logger = LoggerFactory.getLogger(SizeHandle.class);

  public static void loadJavaScript(WApplication app) {
    app.loadJavaScript("js/SizeHandle.js", wtjs1());
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "SizeHandle",
        "(function(e,t,o,n,u,s,c,r,d,i,l,a,m){const p=document.createElement(\"div\");p.style.position=\"absolute\";p.style.zIndex=\"100\";if(\"v\"===t){p.style.width=n+\"px\";p.style.height=o+\"px\"}else{p.style.height=n+\"px\";p.style.width=o+\"px\"}let v;const y=e.widgetPageCoordinates(d),h=e.widgetPageCoordinates(i);if(l.touches)v=e.widgetCoordinates(d,l.touches[0]);else{v=e.widgetCoordinates(d,l);e.capture(null);e.capture(p)}a-=e.px(d,\"marginLeft\");m-=e.px(d,\"marginTop\");y.x+=a-h.x;y.y+=m-h.y;v.x-=a-h.x;v.y-=m-h.y;p.style.left=y.x+\"px\";p.style.top=y.y+\"px\";p.className=c;i.appendChild(p);e.cancelEvent(l);function x(o){let n;const c=e.pageCoordinates(o);n=\"h\"===t?c.x-v.x-y.x:c.y-v.y-y.y;return Math.min(Math.max(n,u),s)}function E(o){const n=x(o);\"h\"===t?p.style.left=y.x+n+\"px\":p.style.top=y.y+n+\"px\";e.cancelEvent(o)}function f(e){if(null!==p.parentNode){p.parentNode.removeChild(p);r(x(e))}}if(document.addEventListener){let L=document.querySelector(\".Wt-domRoot\");L||(L=i);let C=L.style[\"pointer-events\"];C||(C=\"all\");L.style[\"pointer-events\"]=\"none\";let w=document.body.style.cursor;w||(w=\"auto\");document.body.style.cursor=\"h\"===t?\"ew-resize\":\"ns-resize\";function g(e){L.style[\"pointer-events\"]=C;document.body.style.cursor=w;document.removeEventListener(\"mousemove\",E,{capture:!0});document.removeEventListener(\"mouseup\",g,{capture:!0});document.removeEventListener(\"touchmove\",E,{capture:!0});document.removeEventListener(\"touchend\",g,{capture:!0});f(e)}document.addEventListener(\"mousemove\",E,{capture:!0});document.addEventListener(\"mouseup\",g,{capture:!0});document.addEventListener(\"touchmove\",E,{capture:!0});document.addEventListener(\"touchend\",g,{capture:!0})}else{p.onmousemove=i.ontouchmove=E;p.onmouseup=i.ontouchend=function(e){i.ontouchmove=null;i.ontouchend=null;f(e)}}})");
  }
}
