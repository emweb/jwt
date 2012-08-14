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
		return "Wt3_2_2.ChildrenResize";
	}

	public static String getChildrenGetPSJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs11());
		return "Wt3_2_2.ChildrenGetPS";
	}

	public static String getSecondResizeJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs12());
		return "Wt3_2_2.LastResize";
	}

	public static String getSecondGetPSJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs13());
		return "Wt3_2_2.LastGetPS";
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
				"function(a,d,c){function e(h){var j=b.px(h,\"marginTop\");j+=b.px(h,\"marginBottom\");if(!b.boxSizing(h)){j+=b.px(h,\"borderTopWidth\");j+=b.px(h,\"borderBottomWidth\");j+=b.px(h,\"paddingTop\");j+=b.px(h,\"paddingBottom\")}return j}var f,i,g,b=this;a.style.height=c+\"px\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\");d-= b.px(a,\"marginLeft\");d-=b.px(a,\"marginRight\");d-=b.px(a,\"borderLeftWidth\");d-=b.px(a,\"borderRightWidth\");d-=b.px(a,\"paddingLeft\");d-=b.px(a,\"paddingRight\")}f=0;for(i=a.childNodes.length;f<i;++f){g=a.childNodes[f];if(g.nodeType==1){var k=c-e(g);if(k>0)if(g.wtResize)g.wtResize(g,d,k);else{k=k+\"px\";if(g.style.height!=k)g.style.height=k}}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,d,c,e){return e}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,d,c){var e=this;a.style.height=c+\"px\";a=a.lastChild;var f=a.previousSibling;c-=f.offsetHeight+e.px(f,\"marginTop\")+e.px(f,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,d,c);else a.style.height=c+\"px\"}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,d,c,e){var f=this,i,g;i=0;for(g=a.childNodes.length;i<g;++i){var b=a.childNodes[i];if(b!=d)if(c===0)e=Math.max(e,b.offsetWidth);else e+=b.offsetHeight+f.px(b,\"marginTop\")+f.px(b,\"marginBottom\")}return e}");
	}
}
