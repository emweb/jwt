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

class StdWidgetItemImpl extends StdLayoutItemImpl {
	private static Logger logger = LoggerFactory
			.getLogger(StdWidgetItemImpl.class);

	public StdWidgetItemImpl(WWidgetItem item) {
		super();
		this.item_ = item;
	}

	public static String getChildrenResizeJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs10());
		return "Wt3_2_3.ChildrenResize";
	}

	public static String getChildrenGetPSJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs11());
		return "Wt3_2_3.ChildrenGetPS";
	}

	public static String getSecondResizeJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs12());
		return "Wt3_2_3.LastResize";
	}

	public static String getSecondGetPSJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs13());
		return "Wt3_2_3.LastGetPS";
	}

	public String getId() {
		return this.item_.getWidget().getId();
	}

	public int getMinimumHeight() {
		if (this.item_.getWidget().isHidden()) {
			return 0;
		} else {
			return (int) this.item_.getWidget().getMinimumHeight().toPixels();
		}
	}

	public int getMinimumWidth() {
		if (this.item_.getWidget().isHidden()) {
			return 0;
		} else {
			return (int) this.item_.getWidget().getMinimumWidth().toPixels();
		}
	}

	public void updateAddItem(WLayoutItem anon1) {
		assert false;
	}

	public void updateRemoveItem(WLayoutItem anon1) {
		assert false;
	}

	public void update(WLayoutItem anon1) {
		assert false;
	}

	public WLayoutItem getLayoutItem() {
		return this.item_;
	}

	void containerAddWidgets(WContainerWidget container) {
		if (container != null) {
			container.addWidget(this.item_.getWidget());
		} else {
			WContainerWidget wc = ((this.item_.getWidget().getParent()) instanceof WContainerWidget ? (WContainerWidget) (this.item_
					.getWidget().getParent())
					: null);
			if (wc != null) {
				wc.removeFromLayout(this.item_.getWidget());
			}
		}
	}

	public DomElement createDomElement(boolean fitWidth, boolean fitHeight,
			WApplication app) {
		WWidget w = this.item_.getWidget();
		w.setInline(false);
		DomElement d = w.createSDomElement(app);
		DomElement result = d;
		if (app.getEnvironment().agentIsIElt(9)
				&& (d.getType() == DomElementType.DomElement_TEXTAREA
						|| d.getType() == DomElementType.DomElement_SELECT
						|| d.getType() == DomElementType.DomElement_INPUT || d
						.getType() == DomElementType.DomElement_BUTTON)) {
			d.removeProperty(Property.PropertyStyleDisplay);
		}
		if (!app.getEnvironment().agentIsIE()
				&& w.getJavaScriptMember(WWidget.WT_RESIZE_JS).length() == 0) {
			d.setProperty(Property.PropertyStyleBoxSizing, "border-box");
		}
		return result;
	}

	public void setHint(String name, String value) {
		logger.error(new StringWriter().append("unrecognized hint '").append(
				name).append("'").toString());
	}

	private WWidgetItem item_;

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(a,f,c,d){function i(j){var k=b.px(j,\"marginTop\");k+=b.px(j,\"marginBottom\");if(!b.boxSizing(j)){k+=b.px(j,\"borderTopWidth\");k+=b.px(j,\"borderBottomWidth\");k+=b.px(j,\"paddingTop\");k+=b.px(j,\"paddingBottom\")}return k}var b=this,h=c>=0;a.lh=h&&d;a.style.height=h?c+\"px\":\"\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\"); f-=b.px(a,\"marginLeft\");f-=b.px(a,\"marginRight\");f-=b.px(a,\"borderLeftWidth\");f-=b.px(a,\"borderRightWidth\");f-=b.px(a,\"paddingLeft\");f-=b.px(a,\"paddingRight\")}var g,m,e;g=0;for(m=a.childNodes.length;g<m;++g){e=a.childNodes[g];if(e.nodeType==1)if(h){var l=c-i(e);if(l>0)if(e.wtResize)e.wtResize(e,f,l,d);else{l=l+\"px\";if(e.style.height!=l){e.style.height=l;e.lh=d}}}else if(e.wtResize)e.wtResize(e,f,-1);else{e.style.height=\"\";e.lh=false}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,f,c,d){return d}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,f,c,d){var i=this,b=c>=0;a.lh=b&&d;a.style.height=b?c+\"px\":\"\";a=a.lastChild;var h=a.previousSibling;if(b){c-=h.offsetHeight+i.px(h,\"marginTop\")+i.px(h,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,f,c,d);else{a.style.height=c+\"px\";a.lh=d}}else if(a.wtResize)a.wtResize(a,-1,-1);else{a.style.height=\"\";a.lh=false}}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,f,c,d){var i=this,b,h;b=0;for(h=a.childNodes.length;b<h;++b){var g=a.childNodes[b];if(g!=f){var m=i.css(g,\"position\");if(m!=\"absolute\"&&m!=\"fixed\")if(c===0)d=Math.max(d,g.offsetWidth);else d+=g.offsetHeight+i.px(g,\"marginTop\")+i.px(g,\"marginBottom\")}}return d}");
	}
}
