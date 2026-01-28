/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
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
    return "Wt4_12_2.ChildrenResize";
  }

  public static String getChildrenGetPSJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs11());
    return "Wt4_12_2.ChildrenGetPS";
  }

  public static String getSecondResizeJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs12());
    return "Wt4_12_2.LastResize";
  }

  public static String getSecondGetPSJS() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WtResize.js", wtjs13());
    return "Wt4_12_2.LastGetPS";
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

  public int getMaximumHeight() {
    if (this.item_.getWidget().isHidden()) {
      return 0;
    } else {
      return (int) this.item_.getWidget().getMaximumHeight().toPixels();
    }
  }

  public int getMaximumWidth() {
    if (this.item_.getWidget().isHidden()) {
      return 0;
    } else {
      return (int) this.item_.getWidget().getMaximumWidth().toPixels();
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
        "(function(t,e,i,o){const s=this,n=i>=0;if(o)if(n){t.style.height=i+\"px\";t.lh=!0}else{t.style.height=\"\";t.lh=!1}else t.lh=!1;if(s.boxSizing(t)){i-=s.px(t,\"marginTop\");i-=s.px(t,\"marginBottom\");i-=s.pxComputedStyle(t,\"borderTopWidth\");i-=s.pxComputedStyle(t,\"borderBottomWidth\");i-=s.px(t,\"paddingTop\");i-=s.px(t,\"paddingBottom\");e-=s.px(t,\"marginLeft\");e-=s.px(t,\"marginRight\");e-=s.pxComputedStyle(t,\"borderLeftWidth\");e-=s.pxComputedStyle(t,\"borderRightWidth\");e-=s.px(t,\"paddingLeft\");e-=s.px(t,\"paddingRight\")}function p(t){let e=s.px(t,\"marginTop\");e+=s.px(t,\"marginBottom\");if(!s.boxSizing(t)){e+=s.pxComputedStyle(t,\"borderTopWidth\");e+=s.pxComputedStyle(t,\"borderBottomWidth\");e+=s.px(t,\"paddingTop\");e+=s.px(t,\"paddingBottom\")}return e}for(const o of t.childNodes)if(1===o.nodeType&&!o.classList.contains(\"wt-reparented\"))if(n){const t=i-p(o);if(t>0){if(o.offsetTop>0){const t=s.css(o,\"overflow\");\"visible\"!==t&&\"\"!==t||(o.style.overflow=\"auto\")}if(o.wtResize)o.wtResize(o,e,t,!0);else{const e=t+\"px\";if(o.style.height!==e){o.style.height=e;o.lh=!0}}}}else if(o.wtResize)o.wtResize(o,e,-1,!0);else{o.style.height=\"\";o.lh=!1}})");
  }

  static WJavaScriptPreamble wtjs11() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "ChildrenGetPS",
        "(function(t,e,i,o){return o})");
  }

  static WJavaScriptPreamble wtjs12() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastResize",
        "(function(t,e,i,o){const s=this,n=i>=0;if(o)if(n){t.style.height=i+\"px\";t.lh=!0}else{t.style.height=\"\";t.lh=!1}else t.lh=!1;let p=t.lastChild;for(;p&&1===p.nodeType&&(p.classList.contains(\"wt-reparented\")||p.classList.contains(\"resize-sensor\"));)p=p.previousSibling;if(!p)return;const l=p.previousSibling;if(n){if((i-=l.offsetHeight+s.px(l,\"marginTop\")+s.px(l,\"marginBottom\"))>0)if(p.wtResize)p.wtResize(p,e,i,!0);else{p.style.height=i+\"px\";p.lh=!0}}else if(p.wtResize)p.wtResize(p,-1,-1,!0);else{p.style.height=\"\";p.lh=!1}})");
  }

  static WJavaScriptPreamble wtjs13() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptFunction,
        "LastGetPS",
        "(function(t,e,i,o){const s=this;for(const n of t.childNodes)if(n!==e){const t=s.css(n,\"position\");\"absolute\"!==t&&\"fixed\"!==t&&(0===i?o=Math.max(o,n.offsetWidth):o+=n.offsetHeight+s.px(n,\"marginTop\")+s.px(n,\"marginBottom\"))}return o})");
  }
}
