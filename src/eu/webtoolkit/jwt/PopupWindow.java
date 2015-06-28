/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal class that provides a JavaScript popup window managing function.
 */
public class PopupWindow {
	private static Logger logger = LoggerFactory.getLogger(PopupWindow.class);

	/**
	 * Loads the {@link PopupWindow} JavaScript support function.
	 */
	public static void loadJavaScript(WApplication app) {
		app.loadJavaScript("js/PopupWindow.js", wtjs1());
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"PopupWindow",
				"function(j,k,d,e,f){function l(){var a=0,b=0;if(typeof window.screenLeft===\"number\"){a=window.screenLeft;b=window.screenTop}else if(typeof window.screenX===\"number\"){a=window.screenX;b=window.screenY}return{x:a,y:b}}function m(a,b){var g=j.windowSize(),h=l();a=h.x+Math.max(0,Math.floor((g.x-a)/2));b=h.y+Math.max(0,Math.floor((g.y-b)/2));return{x:a,y:b}}var i=m(d,e),c=window.open(k,\"\",\"width=\"+d+\",height=\"+e+\",status=yes,location=yes,resizable=yes,scrollbars=yes,left=\"+ i.x+\",top=\"+i.y);c.opener=window;if(f)var n=setInterval(function(){if(c.closed){clearInterval(n);f(c)}},500)}");
	}
}
