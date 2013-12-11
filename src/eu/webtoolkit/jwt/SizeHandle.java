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
				"function(b,j,e,m,p,q,r,s,g,h,i,k,l){function n(c){c=b.pageCoordinates(c);return Math.min(Math.max(j==\"h\"?c.x-f.x-d.x:c.y-f.y-d.y,p),q)}var a=document.createElement(\"div\");a.style.position=\"absolute\";a.style.zIndex=\"100\";if(j==\"v\"){a.style.width=m+\"px\";a.style.height=e+\"px\"}else{a.style.height=m+\"px\";a.style.width=e+\"px\"}var f,d=b.widgetPageCoordinates(g);e=b.widgetPageCoordinates(h);if(i.touches)f=b.widgetCoordinates(g,i.touches[0]);else{f= b.widgetCoordinates(g,i);b.capture(null);b.capture(a)}k-=b.px(g,\"marginLeft\");l-=b.px(g,\"marginTop\");d.x+=k-e.x;d.y+=l-e.y;f.x-=k-e.x;f.y-=l-e.y;a.style.left=d.x+\"px\";a.style.top=d.y+\"px\";a.className=r;h.appendChild(a);b.cancelEvent(i);a.onmousemove=h.ontouchmove=function(c){var o=n(c);if(j==\"h\")a.style.left=d.x+o+\"px\";else a.style.top=d.y+o+\"px\";b.cancelEvent(c)};a.onmouseup=h.ontouchend=function(c){if(a.parentNode!=null){a.parentNode.removeChild(a);s(n(c));h.ontouchmove=null}}}");
	}
}
