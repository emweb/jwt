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

class StdWidgetItemImpl extends StdLayoutItemImpl implements WWidgetItemImpl {
  private static Logger logger = LoggerFactory.getLogger(StdWidgetItemImpl.class);

  public StdWidgetItemImpl(WWidgetItem item) {
    super();
    this.item_ = item;
  }

  public static String getChildrenResizeJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs10());
    return "Wt4_7_2.ChildrenResize";
  }

  public static String getChildrenGetPSJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs11());
    return "Wt4_7_2.ChildrenGetPS";
  }

  public static String getSecondResizeJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs12());
    return "Wt4_7_2.LastResize";
  }

  public static String getSecondGetPSJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs13());
    return "Wt4_7_2.LastGetPS";
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

  public WLayoutItem getLayoutItem() {
    return this.item_;
  }

  public DomElement createDomElement(
      DomElement parent, boolean fitWidth, boolean fitHeight, WApplication app) {
    WWidget w = this.item_.getWidget();
    w.setInline(false);
    DomElement d = w.createSDomElement(app);
    DomElement result = d;
    if (app.getEnvironment().agentIsIElt(9)
        && (d.getType() == DomElementType.TEXTAREA
            || d.getType() == DomElementType.SELECT
            || d.getType() == DomElementType.INPUT
            || d.getType() == DomElementType.BUTTON)) {
      d.removeProperty(Property.StyleDisplay);
    }
    if (!app.getEnvironment().agentIsIElt(9)
        && w.getJavaScriptMember(WWidget.WT_RESIZE_JS).length() == 0
        && d.getType() != DomElementType.TABLE
        && app.getTheme().canBorderBoxElement(d)) {
      d.setProperty(Property.StyleBoxSizing, "border-box");
    }
    return result;
  }

  private WWidgetItem item_;

  static WJavaScriptPreamble wtjs10() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenResize",
        "function(a,f,d,e){function h(i){var j=b.px(i,\"marginTop\");j+=b.px(i,\"marginBottom\");if(!b.boxSizing(i)){j+=b.px(i,\"borderTopWidth\");j+=b.px(i,\"borderBottomWidth\");j+=b.px(i,\"paddingTop\");j+=b.px(i,\"paddingBottom\")}return j}var b=this,l=d>=0;if(e)if(l){a.style.height=d+\"px\";a.lh=true}else{a.style.height=\"\";a.lh=false}else a.lh=false;if(b.boxSizing(a)){d-=b.px(a,\"marginTop\");d-=b.px(a,\"marginBottom\");d-=b.px(a,\"borderTopWidth\");d-=b.px(a,\"borderBottomWidth\"); d-=b.px(a,\"paddingTop\");d-=b.px(a,\"paddingBottom\");f-=b.px(a,\"marginLeft\");f-=b.px(a,\"marginRight\");f-=b.px(a,\"borderLeftWidth\");f-=b.px(a,\"borderRightWidth\");f-=b.px(a,\"paddingLeft\");f-=b.px(a,\"paddingRight\")}var g,c;e=0;for(g=a.childNodes.length;e<g;++e){c=a.childNodes[e];if(c.nodeType==1&&!$(c).hasClass(\"wt-reparented\"))if(l){var k=d-h(c);if(k>0){if(c.offsetTop>0){var m=b.css(c,\"overflow\");if(m===\"visible\"||m===\"\")c.style.overflow=\"auto\"}if(c.wtResize)c.wtResize(c,f,k,true);else{k=k+\"px\";if(c.style.height!= k){c.style.height=k;c.lh=true}}}}else if(c.wtResize)c.wtResize(c,f,-1,true);else{c.style.height=\"\";c.lh=false}}}");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenGetPS",
        "function(a,f,d,e){return e}");
  }

  static WJavaScriptPreamble wtjs12() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastResize",
        "function(a,f,d,e){var h=this,b=d>=0;if(e)if(b){a.style.height=d+\"px\";a.lh=true}else{a.style.height=\"\";a.lh=false}else a.lh=false;for(a=a.lastChild;a&&a.nodeType==1&&($(a).hasClass(\"wt-reparented\")||$(a).hasClass(\"resize-sensor\"));)a=a.previousSibling;if(a){e=a.previousSibling;if(b){d-=e.offsetHeight+h.px(e,\"marginTop\")+h.px(e,\"marginBottom\");if(d>0)if(a.wtResize)a.wtResize(a,f,d,true);else{a.style.height=d+\"px\";a.lh=true}}else if(a.wtResize)a.wtResize(a, -1,-1,true);else{a.style.height=\"\";a.lh=false}}}");
  }

  static WJavaScriptPreamble wtjs13() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastGetPS",
        "function(a,f,d,e){var h=this,b,l;b=0;for(l=a.childNodes.length;b<l;++b){var g=a.childNodes[b];if(g!=f){var c=h.css(g,\"position\");if(c!=\"absolute\"&&c!=\"fixed\")if(d===0)e=Math.max(e,g.offsetWidth);else e+=g.offsetHeight+h.px(g,\"marginTop\")+h.px(g,\"marginBottom\")}}return e}");
  }
}
