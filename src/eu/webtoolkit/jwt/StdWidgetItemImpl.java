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
    return "Wt4_8_1.ChildrenResize";
  }

  public static String getChildrenGetPSJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs11());
    return "Wt4_8_1.ChildrenGetPS";
  }

  public static String getSecondResizeJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs12());
    return "Wt4_8_1.LastResize";
  }

  public static String getSecondGetPSJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs13());
    return "Wt4_8_1.LastGetPS";
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
        "(function(e,i,t,o){var s,r,h,p=this,n=t>=0;if(o)if(n){e.style.height=t+\"px\";e.lh=!0}else{e.style.height=\"\";e.lh=!1}else e.lh=!1;if(p.boxSizing(e)){t-=p.px(e,\"marginTop\");t-=p.px(e,\"marginBottom\");t-=p.px(e,\"borderTopWidth\");t-=p.px(e,\"borderBottomWidth\");t-=p.px(e,\"paddingTop\");t-=p.px(e,\"paddingBottom\");i-=p.px(e,\"marginLeft\");i-=p.px(e,\"marginRight\");i-=p.px(e,\"borderLeftWidth\");i-=p.px(e,\"borderRightWidth\");i-=p.px(e,\"paddingLeft\");i-=p.px(e,\"paddingRight\")}function a(e){var i=p.px(e,\"marginTop\");i+=p.px(e,\"marginBottom\");if(!p.boxSizing(e)){i+=p.px(e,\"borderTopWidth\");i+=p.px(e,\"borderBottomWidth\");i+=p.px(e,\"paddingTop\");i+=p.px(e,\"paddingBottom\")}return i}for(s=0,r=e.childNodes.length;s<r;++s)if(1==(h=e.childNodes[s]).nodeType&&!$(h).hasClass(\"wt-reparented\"))if(n){var l=t-a(h);if(l>0){if(h.offsetTop>0){var d=p.css(h,\"overflow\");\"visible\"!==d&&\"\"!==d||(h.style.overflow=\"auto\")}if(h.wtResize)h.wtResize(h,i,l,!0);else{var f=l+\"px\";if(h.style.height!=f){h.style.height=f;h.lh=!0}}}}else if(h.wtResize)h.wtResize(h,i,-1,!0);else{h.style.height=\"\";h.lh=!1}})");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenGetPS",
        "(function(e,i,t,o){return o})");
  }

  static WJavaScriptPreamble wtjs12() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastResize",
        "(function(e,i,t,o){var s=t>=0;if(o)if(s){e.style.height=t+\"px\";e.lh=!0}else{e.style.height=\"\";e.lh=!1}else e.lh=!1;for(var r=e.lastChild;r&&1==r.nodeType&&($(r).hasClass(\"wt-reparented\")||$(r).hasClass(\"resize-sensor\"));)r=r.previousSibling;if(r){var h=r.previousSibling;if(s){if((t-=h.offsetHeight+this.px(h,\"marginTop\")+this.px(h,\"marginBottom\"))>0)if(r.wtResize)r.wtResize(r,i,t,!0);else{r.style.height=t+\"px\";r.lh=!0}}else if(r.wtResize)r.wtResize(r,-1,-1,!0);else{r.style.height=\"\";r.lh=!1}}})");
  }

  static WJavaScriptPreamble wtjs13() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastGetPS",
        "(function(e,i,t,o){var s,r,h=this;for(s=0,r=e.childNodes.length;s<r;++s){var p=e.childNodes[s];if(p!=i){var n=h.css(p,\"position\");\"absolute\"!=n&&\"fixed\"!=n&&(0===t?o=Math.max(o,p.offsetWidth):o+=p.offsetHeight+h.px(p,\"marginTop\")+h.px(p,\"marginBottom\"))}}return o})");
  }
}
