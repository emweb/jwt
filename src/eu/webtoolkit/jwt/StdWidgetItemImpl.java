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
				"function(a,e,c){function f(h){var i=b.px(h,\"marginTop\");i+=b.px(h,\"marginBottom\");if(!b.boxSizing(h)){i+=b.px(h,\"borderTopWidth\");i+=b.px(h,\"borderBottomWidth\");i+=b.px(h,\"paddingTop\");i+=b.px(h,\"paddingBottom\")}return i}var b=this;a.style.height=c+\"px\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\");e-=b.px(a, \"marginLeft\");e-=b.px(a,\"marginRight\");e-=b.px(a,\"borderLeftWidth\");e-=b.px(a,\"borderRightWidth\");e-=b.px(a,\"paddingLeft\");e-=b.px(a,\"paddingRight\")}var g,k,d;g=0;for(k=a.childNodes.length;g<k;++g){d=a.childNodes[g];if(d.nodeType==1){var j=c-f(d);if(j>0)if(d.wtResize)d.wtResize(d,e,j);else{j=j+\"px\";if(d.style.height!=j)d.style.height=j}}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,e,c,f){return f}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,e,c){var f=this;a.style.height=c+\"px\";a=a.lastChild;var b=a.previousSibling;c-=b.offsetHeight+f.px(b,\"marginTop\")+f.px(b,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,e,c);else a.style.height=c+\"px\"}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,e,c,f){var b=this,g,k;g=0;for(k=a.childNodes.length;g<k;++g){var d=a.childNodes[g];if(d!=e)if(c===0)f=Math.max(f,d.offsetWidth);else f+=d.offsetHeight+b.px(d,\"marginTop\")+b.px(d,\"marginBottom\")}return f}");
	}
}
