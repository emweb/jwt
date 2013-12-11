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
		return "Wt3_3_1.ChildrenResize";
	}

	public static String getChildrenGetPSJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs11());
		return "Wt3_3_1.ChildrenGetPS";
	}

	public static String getSecondResizeJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs12());
		return "Wt3_3_1.LastResize";
	}

	public static String getSecondGetPSJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs13());
		return "Wt3_3_1.LastGetPS";
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
		if (!app.getEnvironment().agentIsIElt(9)
				&& w.getJavaScriptMember(WWidget.WT_RESIZE_JS).length() == 0
				&& d.getType() != DomElementType.DomElement_TABLE
				&& app.getTheme().canBorderBoxElement(d)) {
			d.setProperty(Property.PropertyStyleBoxSizing, "border-box");
		}
		return result;
	}

	public void setHint(final String name, final String value) {
		logger.error(new StringWriter().append("unrecognized hint '").append(
				name).append("'").toString());
	}

	private WWidgetItem item_;

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(a,e,c,d){function i(j){var k=b.px(j,\"marginTop\");k+=b.px(j,\"marginBottom\");if(!b.boxSizing(j)){k+=b.px(j,\"borderTopWidth\");k+=b.px(j,\"borderBottomWidth\");k+=b.px(j,\"paddingTop\");k+=b.px(j,\"paddingBottom\")}return k}var b=this,h=c>=0;a.lh=h&&d;a.style.height=h?c+\"px\":\"\";if(b.boxSizing(a)){c-=b.px(a,\"marginTop\");c-=b.px(a,\"marginBottom\");c-=b.px(a,\"borderTopWidth\");c-=b.px(a,\"borderBottomWidth\");c-=b.px(a,\"paddingTop\");c-=b.px(a,\"paddingBottom\"); e-=b.px(a,\"marginLeft\");e-=b.px(a,\"marginRight\");e-=b.px(a,\"borderLeftWidth\");e-=b.px(a,\"borderRightWidth\");e-=b.px(a,\"paddingLeft\");e-=b.px(a,\"paddingRight\")}var g,m,f;g=0;for(m=a.childNodes.length;g<m;++g){f=a.childNodes[g];if(f.nodeType==1)if(h){var l=c-i(f);if(l>0)if(f.wtResize)f.wtResize(f,e,l,d);else{l=l+\"px\";if(f.style.height!=l){f.style.height=l;f.lh=d}}}else if(f.wtResize)f.wtResize(f,e,-1);else{f.style.height=\"\";f.lh=false}}}");
	}

	static WJavaScriptPreamble wtjs11() {
		return new WJavaScriptPreamble(JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction, "ChildrenGetPS",
				"function(a,e,c,d){return d}");
	}

	static WJavaScriptPreamble wtjs12() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastResize",
				"function(a,e,c,d){var i=this,b=c>=0;a.lh=b&&d;a.style.height=b?c+\"px\":\"\";a=a.lastChild;var h=a.previousSibling;if(b){c-=h.offsetHeight+i.px(h,\"marginTop\")+i.px(h,\"marginBottom\");if(c>0)if(a.wtResize)a.wtResize(a,e,c,d);else{a.style.height=c+\"px\";a.lh=d}}else if(a.wtResize)a.wtResize(a,-1,-1);else{a.style.height=\"\";a.lh=false}}");
	}

	static WJavaScriptPreamble wtjs13() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"LastGetPS",
				"function(a,e,c,d){var i=this,b,h;if(e.wtGetPS)return d;b=0;for(h=a.childNodes.length;b<h;++b){var g=a.childNodes[b];if(g!=e){var m=i.css(g,\"position\");if(m!=\"absolute\"&&m!=\"fixed\")if(c===0)d=Math.max(d,g.offsetWidth);else d+=g.offsetHeight+i.px(g,\"marginTop\")+i.px(g,\"marginBottom\")}}return d}");
	}
}
