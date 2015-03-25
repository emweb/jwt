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
				"function(b,a){function m(c){c=b.pageCoordinates(c);var d=c.x-j.x,e=c.y-j.y;c=b.px(a,\"width\");var f=b.px(a,\"height\");d=Math.max(k+d,n+(k-o));a.style.width=d+\"px\";e=Math.max(l+e,p+(l-q));a.style.height=e+\"px\";if(a.style.left===\"auto\")a.style.right=b.px(a,\"right\")-(d-c)+\"px\";if(a.style.top===\"auto\")a.style.bottom=b.px(a,\"bottom\")-(e-f)+\"px\";g&&g(d,e)}function r(){$(window.document).unbind(\"mousemove\",m);$(window.document).unbind(\"mouseup\",r);g&& g(b.pxself(a,\"width\"),b.pxself(a,\"height\"),true)}function s(c){var d=b.widgetCoordinates(a,c);if(a.offsetWidth-d.x<16&&a.offsetHeight-d.y<16){if(!h){h=b.css(a,\"minWidth\");i=b.css(a,\"minHeight\");if(b.isIE6){function e(f,t){return(f=(new RegExp(t+\":\\\\s*(\\\\d+(?:\\\\.\\\\d+)?)\\\\s*px\",\"i\")).exec(f.style.cssText))&&f.length==2?f[1]+\"px\":\"\"}h=e(a,\"min-width\");i=e(a,\"min-height\")}n=h==\"0px\"?a.clientWidth:b.parsePx(h);p=i==\"0px\"?a.clientHeight:b.parsePx(i)}j=b.pageCoordinates(c);k=b.innerWidth(a);l=b.innerHeight(a); o=a.clientWidth;q=a.clientHeight;b.capture(null);$(window.document).bind(\"mousemove\",m);$(window.document).bind(\"mouseup\",r)}}var g=null,j=null,k,l,o,q,n,p,h,i;$(a).mousedown(s);this.onresize=function(c){g=c}}");
	}
}
