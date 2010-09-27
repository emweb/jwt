/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


class SizeHandle {
	public static void loadJavaScript(WApplication app) {
		String THIS_JS = "js/SizeHandle.js";
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
	}

	static String wtjs1(WApplication app) {
		return "Wt3_1_6.SizeHandle = function(c,h,e,k,o,p,q,r,f,l,m,i,j){function n(b){b=c.pageCoordinates(b);return Math.min(Math.max(h==\"h\"?b.x-g.x-d.x:b.y-g.y-d.y,o),p)}var a=document.createElement(\"div\");a.style.position=\"absolute\";a.style.zIndex=\"100\";if(h==\"v\"){a.style.width=k+\"px\";a.style.height=e+\"px\"}else{a.style.height=k+\"px\";a.style.width=e+\"px\"}var g=c.widgetCoordinates(f,m),d=c.widgetPageCoordinates(f);e=c.widgetPageCoordinates(l);i-=c.px(f,\"marginLeft\");j-=c.px(f,\"marginTop\");d.x+=i- e.x;d.y+=j-e.y;g.x-=i-e.x;g.y-=j-e.y;a.style.left=d.x+\"px\";a.style.top=d.y+\"px\";a.className=q;l.appendChild(a);c.capture(null);c.capture(a);c.cancelEvent(m);a.onmousemove=function(b){b=n(b);if(h==\"h\")a.style.left=d.x+b+\"px\";else a.style.top=d.y+b+\"px\"};a.onmouseup=function(b){if(a.parentNode!=null){a.parentNode.removeChild(a);r(n(b))}}};";
	}
}
