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

/** Internal class that provides a JavaScript popup window managing function. */
public class PopupWindow {
  private static Logger logger = LoggerFactory.getLogger(PopupWindow.class);

  /** Loads the {@link PopupWindow} JavaScript support function. */
  public static void loadJavaScript(WApplication app) {
    app.loadJavaScript("js/PopupWindow.js", wtjs1());
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "PopupWindow",
        "(function(o,n,t,e,i){const s=function(n,t){const e=o.windowSize();return{x:window.screenLeft+Math.max(0,Math.floor((e.x-n)/2)),y:window.screenTop+Math.max(0,Math.floor((e.y-t)/2))}}(t,e),a=window.open(n,\"\",\"width=\"+t+\",height=\"+e+\",status=yes,location=yes,resizable=yes,scrollbars=yes,left=\"+s.x+\",top=\"+s.y);a.opener=window;if(i){const o=setInterval((function(){if(a.closed){clearInterval(o);i(a)}}),500)}})");
  }
}
