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
        "(function(e,n,o,r,t){var i=function(n,o){var r=e.windowSize(),t=function(){var e=0,n=0;if(\"number\"==typeof window.screenLeft){e=window.screenLeft;n=window.screenTop}else if(\"number\"==typeof window.screenX){e=window.screenX;n=window.screenY}return{x:e,y:n}}();return{x:t.x+Math.max(0,Math.floor((r.x-n)/2)),y:t.y+Math.max(0,Math.floor((r.y-o)/2))}}(o,r),w=window.open(n,\"\",\"width=\"+o+\",height=\"+r+\",status=yes,location=yes,resizable=yes,scrollbars=yes,left=\"+i.x+\",top=\"+i.y);w.opener=window;if(t)var a=setInterval((function(){if(w.closed){clearInterval(a);t(w)}}),500)})");
  }
}
