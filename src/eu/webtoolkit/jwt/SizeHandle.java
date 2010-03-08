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
		return "Wt3_1_1.SizeHandle = function(d,h,f,i,l,m,n,o,e,j){function k(b){b=d.pageCoordinates(b);return Math.min(Math.max(h==\"h\"?b.x-g.x-c.x:b.y-g.y-c.y,l),m)}var a=document.createElement(\"div\");a.style.position=\"absolute\";a.style.zIndex=\"100\";if(h==\"v\"){a.style.width=i+\"px\";a.style.height=f+\"px\"}else{a.style.height=i+\"px\";a.style.width=f+\"px\"}var g=d.widgetCoordinates(e,j),c=d.widgetPageCoordinates(e);f=d.px(e,\"marginLeft\");e=d.px(e,\"marginTop\");c.x-=f;c.y-=e;g.x+=f;g.y+=e;a.style.left=c.x+ \"px\";a.style.top=c.y+\"px\";a.className=n;document.body.appendChild(a);d.capture(a);d.cancelEvent(j);a.onmousemove=function(b){b=k(b);if(h==\"h\")a.style.left=c.x+b+\"px\";else a.style.top=c.y+b+\"px\"};a.onmouseup=function(b){if(a.parentNode!=null){a.parentNode.removeChild(a);o(k(b))}}};";
	}
}
