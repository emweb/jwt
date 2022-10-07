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

/**
 * A container widget that stacks its widgets on top of each other.
 *
 * <p>This is a container widget which at all times has only one item visible. The widget
 * accomplishes this using setHidden(bool) on the children.
 *
 * <p>Using {@link WStackedWidget#getCurrentIndex() getCurrentIndex()} and {@link
 * WStackedWidget#setCurrentIndex(int index) setCurrentIndex()} you can retrieve or set the visible
 * widget.
 *
 * <p>WStackedWidget, like {@link WContainerWidget}, is by default not inline.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>The widget is rendered using an HTML <code>&lt;div&gt;</code> tag and does not provide
 * styling. It can be styled using inline or external CSS as appropriate.
 *
 * <p>
 *
 * @see WMenu
 */
public class WStackedWidget extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(WStackedWidget.class);

  /** Creates a new stack. */
  public WStackedWidget(WContainerWidget parentContainer) {
    super();
    this.animation_ = new WAnimation();
    this.autoReverseAnimation_ = false;
    this.currentIndex_ = -1;
    this.widgetsAdded_ = false;
    this.javaScriptDefined_ = false;
    this.loadAnimateJS_ = false;
    this.setOverflow(Overflow.Hidden);
    this.addStyleClass("Wt-stack");
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new stack.
   *
   * <p>Calls {@link #WStackedWidget(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WStackedWidget() {
    this((WContainerWidget) null);
  }

  public void addWidget(WWidget widget) {
    super.addWidget(widget);
    if (this.currentIndex_ == -1) {
      this.currentIndex_ = 0;
    }
    this.widgetsAdded_ = true;
  }
  // public Widget  addWidget(<Woow... some pseudoinstantiation type!> widget) ;
  public WWidget removeWidget(WWidget widget) {
    WWidget result = super.removeWidget(widget);
    if (this.currentIndex_ >= this.getCount()) {
      if (this.getCount() > 0) {
        this.setCurrentIndex(this.getCount() - 1);
      } else {
        this.currentIndex_ = -1;
      }
    }
    return result;
  }
  /**
   * Returns the index of the widget that is currently shown.
   *
   * <p>
   *
   * @see WStackedWidget#setCurrentIndex(int index)
   * @see WStackedWidget#getCurrentWidget()
   */
  public int getCurrentIndex() {
    return this.currentIndex_;
  }
  /**
   * Returns the widget that is currently shown.
   *
   * <p>
   *
   * @see WStackedWidget#setCurrentWidget(WWidget widget)
   * @see WStackedWidget#getCurrentIndex()
   */
  public WWidget getCurrentWidget() {
    if (this.currentIndex_ >= 0 && this.currentIndex_ < this.getCount()) {
      return this.getWidget(this.currentIndex_);
    } else {
      return null;
    }
  }
  /** Insert a widget at a given index. */
  public void insertWidget(int index, WWidget widget) {
    super.insertWidget(index, widget);
    if (this.currentIndex_ == -1) {
      this.currentIndex_ = 0;
    }
    this.widgetsAdded_ = true;
  }
  /**
   * Changes the current widget.
   *
   * <p>The widget with index <code>index</code> is made visible, while all other widgets are
   * hidden.
   *
   * <p>The change of current widget is done using the animation settings specified by {@link
   * WStackedWidget#setTransitionAnimation(WAnimation animation, boolean autoReverse)
   * setTransitionAnimation()}.
   *
   * <p>The default value for current index is 0 (provided thath
   *
   * <p>
   *
   * @see WStackedWidget#getCurrentIndex()
   * @see WStackedWidget#setCurrentWidget(WWidget widget)
   */
  public void setCurrentIndex(int index) {
    this.setCurrentIndex(index, this.animation_, this.autoReverseAnimation_);
  }
  /**
   * Changes the current widget using a custom animation.
   *
   * <p>
   *
   * @see WStackedWidget#getCurrentIndex()
   * @see WStackedWidget#setCurrentWidget(WWidget widget)
   */
  public void setCurrentIndex(int index, final WAnimation animation, boolean autoReverse) {
    if (!animation.isEmpty()
        && WApplication.getInstance().getEnvironment().supportsCss3Animations()
        && (this.isRendered() && this.javaScriptDefined_ || !canOptimizeUpdates())) {
      if (canOptimizeUpdates() && index == this.currentIndex_) {
        return;
      }
      this.loadAnimateJS();
      WWidget previous = this.getCurrentWidget();
      if (previous != null) {
        this.doJavaScript(this.getJsRef() + ".wtObj.adjustScroll(" + previous.getJsRef() + ");");
      }
      this.setJavaScriptMember("wtAutoReverse", autoReverse ? "true" : "false");
      if (previous != null) {
        previous.animateHide(animation);
      }
      this.getWidget(index).animateShow(animation);
      this.currentIndex_ = index;
    } else {
      this.currentIndex_ = index;
      for (int i = 0; i < this.getCount(); ++i) {
        if (!canOptimizeUpdates() || this.getWidget(i).isHidden() != (this.currentIndex_ != i)) {
          this.getWidget(i).setHidden(this.currentIndex_ != i);
        }
      }
      if (this.currentIndex_ >= 0 && this.isRendered() && this.javaScriptDefined_) {
        this.doJavaScript(
            this.getJsRef()
                + ".wtObj.setCurrent("
                + this.getWidget(this.currentIndex_).getJsRef()
                + ");");
      }
    }
  }
  /**
   * Changes the current widget using a custom animation.
   *
   * <p>Calls {@link #setCurrentIndex(int index, WAnimation animation, boolean autoReverse)
   * setCurrentIndex(index, animation, true)}
   */
  public final void setCurrentIndex(int index, final WAnimation animation) {
    setCurrentIndex(index, animation, true);
  }
  /**
   * Changes the current widget.
   *
   * <p>The widget <code>widget</code>, which must have been added before, is made visible, while
   * all other widgets are hidden.
   *
   * <p>
   *
   * @see WStackedWidget#getCurrentWidget()
   * @see WStackedWidget#setCurrentIndex(int index)
   */
  public void setCurrentWidget(WWidget widget) {
    this.setCurrentIndex(this.getIndexOf(widget));
  }
  /**
   * Specifies an animation used during transitions.
   *
   * <p>The animation is used to hide the previously current widget and show the next current widget
   * using {@link WStackedWidget#setCurrentIndex(int index) setCurrentIndex()}.
   *
   * <p>The initial value for <code>animation</code> is WAnimation(), specifying no animation.
   *
   * <p>When <code>autoReverse</code> is set to <code>true</code>, then the reverse animation is
   * chosen when the new index precedes the current index. This only applies to {@link
   * AnimationEffect#SlideInFromLeft}, {@link AnimationEffect#SlideInFromRight}, {@link
   * AnimationEffect#SlideInFromTop} or {@link AnimationEffect#SlideInFromBottom} transition
   * effects.
   *
   * <p>
   *
   * <p><i><b>Note: </b>If you intend to use a transition animation with a {@link WStackedWidget}
   * you should set it before it is first rendered. Otherwise, transition animations caused by
   * {@link WStackedWidget#setCurrentIndex(int index) setCurrentIndex()} may not be correctly
   * performed. If you do want to force this change you can use {@link WApplication#processEvents()}
   * before calling {@link WStackedWidget#setCurrentIndex(int index) setCurrentIndex()}.</i>
   *
   * <p><i><b>Note: </b>It is also not supported to use a {@link AnimationEffect#Pop} animation on a
   * {@link WStackedWidget}.</i>
   *
   * @see WStackedWidget#setCurrentIndex(int index)
   */
  public void setTransitionAnimation(final WAnimation animation, boolean autoReverse) {
    if (WApplication.getInstance().getEnvironment().supportsCss3Animations()) {
      if (!animation.isEmpty()) {
        this.addStyleClass("Wt-animated");
      }
      this.animation_ = animation;
      this.autoReverseAnimation_ = autoReverse;
      this.loadAnimateJS();
    }
  }
  /**
   * Specifies an animation used during transitions.
   *
   * <p>Calls {@link #setTransitionAnimation(WAnimation animation, boolean autoReverse)
   * setTransitionAnimation(animation, false)}
   */
  public final void setTransitionAnimation(final WAnimation animation) {
    setTransitionAnimation(animation, false);
  }

  protected DomElement createDomElement(WApplication app) {
    return super.createDomElement(app);
  }

  protected void getDomChanges(final List<DomElement> result, WApplication app) {
    super.getDomChanges(result, app);
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (this.widgetsAdded_ || flags.contains(RenderFlag.Full)) {
      for (int i = 0; i < this.getCount(); ++i) {
        if (!canOptimizeUpdates() || this.getWidget(i).isHidden() != (this.currentIndex_ != i)) {
          this.getWidget(i).setHidden(this.currentIndex_ != i);
        }
      }
      this.widgetsAdded_ = false;
    }
    if (flags.contains(RenderFlag.Full)) {
      this.defineJavaScript();
      if (this.currentIndex_ >= 0 && this.isRendered() && this.javaScriptDefined_) {
        this.doJavaScript(
            this.getJsRef()
                + ".wtObj.setCurrent("
                + this.getWidget(this.currentIndex_).getJsRef()
                + ");");
      }
    }
    super.render(flags);
  }

  private WAnimation animation_;
  private boolean autoReverseAnimation_;
  private int currentIndex_;
  private boolean widgetsAdded_;
  private boolean javaScriptDefined_;
  private boolean loadAnimateJS_;

  private void defineJavaScript() {
    if (!this.javaScriptDefined_) {
      this.javaScriptDefined_ = true;
      WApplication app = WApplication.getInstance();
      app.loadJavaScript("js/WStackedWidget.js", wtjs1());
      this.setJavaScriptMember(
          " WStackedWidget",
          "new Wt4_8_1.WStackedWidget(" + app.getJavaScriptClass() + "," + this.getJsRef() + ");");
      this.setJavaScriptMember(WT_RESIZE_JS, this.getJsRef() + ".wtObj.wtResize");
      this.setJavaScriptMember(WT_GETPS_JS, this.getJsRef() + ".wtObj.wtGetPs");
      if (this.loadAnimateJS_) {
        this.loadAnimateJS_ = false;
        this.loadAnimateJS();
      }
    }
  }

  private void loadAnimateJS() {
    if (!this.loadAnimateJS_) {
      this.loadAnimateJS_ = true;
      if (this.javaScriptDefined_) {
        WApplication.getInstance().loadJavaScript("js/WStackedWidget.js", wtjs2());
        this.setJavaScriptMember("wtAnimateChild", this.getJsRef() + ".wtObj.animateChild");
        this.setJavaScriptMember("wtAutoReverse", this.autoReverseAnimation_ ? "true" : "false");
      }
    }
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WStackedWidget",
        "(function(t,e){e.wtObj=this;var i=t.WT,o=[],s=[],l=null,n=null;function a(t){return 1==t.nodeType&&!$(t).hasClass(\"wt-reparented\")&&!$(t).hasClass(\"resize-sensor\")}this.reApplySize=function(){n&&this.wtResize(e,l,n,!1)};this.wtResize=function(t,e,o,s){l=e;n=o;var d,r,p,h=o>=0;if(s)if(h){t.style.height=o+\"px\";t.lh=!0}else{t.style.height=\"\";t.lh=!1}else t.lh=!1;if(i.boxSizing(t)){o-=i.px(t,\"marginTop\");o-=i.px(t,\"marginBottom\");o-=i.px(t,\"borderTopWidth\");o-=i.px(t,\"borderBottomWidth\");o-=i.px(t,\"paddingTop\");o-=i.px(t,\"paddingBottom\");e-=i.px(t,\"marginLeft\");e-=i.px(t,\"marginRight\");e-=i.px(t,\"borderLeftWidth\");e-=i.px(t,\"borderRightWidth\");e-=i.px(t,\"paddingLeft\");e-=i.px(t,\"paddingRight\")}function f(t){var e=i.px(t,\"marginTop\");e+=i.px(t,\"marginBottom\");if(!i.boxSizing(t)){e+=i.px(t,\"borderTopWidth\");e+=i.px(t,\"borderBottomWidth\");e+=i.px(t,\"paddingTop\");e+=i.px(t,\"paddingBottom\")}return e}for(d=0,r=t.childNodes.length;d<r;++d)if(a(p=t.childNodes[d])&&!i.isHidden(p)&&!$(p).hasClass(\"out\"))if(h){var y=o-f(p);if(y>0){if(p.offsetTop>0){var c=i.css(p,\"overflow\");\"visible\"!==c&&\"\"!==c||(p.style.overflow=\"auto\")}if(p.wtResize)p.wtResize(p,e,y,!0);else{var g=y+\"px\";if(p.style.height!=g){p.style.height=g;p.lh=!0}}}}else if(p.wtResize)p.wtResize(p,e,-1,!0);else{p.style.height=\"\";p.lh=!1}};this.wtGetPs=function(t,e,i,o){return o};this.adjustScroll=function(t){var i,l,n,d=e.scrollLeft,r=e.scrollTop;for(i=0,l=e.childNodes.length;i<l;++i)if(a(n=e.childNodes[i]))if(n!=t){if(\"none\"!=n.style.display){s[i]=d;o[i]=r}}else if(void 0!==s[i]){e.scrollLeft=s[i];e.scrollTop=o[i]}else{e.scrollLeft=0;e.scrollTop=0}};this.setCurrent=function(t){var i,o,s;this.adjustScroll(t);for(i=0,o=e.childNodes.length;i<o;++i)if(a(s=e.childNodes[i]))if(s!=t)\"none\"!=s.style.display&&(s.style.display=\"none\");else{s.style.flexFlow?s.style.display=\"flex\":s.style.display=\"\";if(e.lh){e.lh=!1;e.style.height=\"\"}}this.reApplySize()}})");
  }

  static WJavaScriptPreamble wtjs2() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptPrototype,
        "WStackedWidget.prototype.animateChild",
        "(function(t,e,i,o,s,l){var n=function(t,e,i,o,s,l){var a=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],d=\"Webkit\"==t.vendorPrefix(t.styleAttribute(\"animation-duration\"))?\"webkitAnimationEnd\":\"animationend\";if(\"none\"!==l.display){var r=e.parentNode,p=r.wtAutoReverse,h=function(){var t,i=r.childNodes.length,o=-1,s=-1;for(t=0;t<i&&(-1==o||-1==s);++t){var l=r.childNodes[t];l==e?s=t:\"none\"===l.style.display||$(l).hasClass(\"out\")||(o=t)}return{from:o,to:s}}();if(-1!=h.from&&-1!=h.to&&h.from!=h.to){var f=r.childNodes[h.from],y=r.childNodes[h.to],c=$(f),g=$(y),u=r.scrollHeight,m=r.scrollWidth;if(c.hasClass(\"in\"))c.one(d,(function(){n(t,e,i,o,1,l)}));else if(g.hasClass(\"out\"))g.one(d,(function(){n(t,e,i,o,1,l)}));else{u-=t.px(r,\"paddingTop\");u-=t.px(r,\"paddingBottom\");u-=t.px(y,\"marginTop\");u-=t.px(y,\"marginBottom\");u-=t.px(y,\"borderTopWidth\");u-=t.px(y,\"borderBottomWidth\");u-=t.px(y,\"paddingTop\");u-=t.px(y,\"paddingBottom\");m-=t.px(r,\"paddingLeft\");m-=t.px(r,\"paddingRight\");m-=t.px(y,\"marginLeft\");m-=t.px(y,\"marginRight\");m-=t.px(y,\"borderLeftWidth\");m-=t.px(y,\"borderRightWidth\");m-=t.px(y,\"paddingLeft\");m-=t.px(y,\"paddingRight\");y.style.left=f.style.left||t.px(r,\"paddingLeft\");y.style.top=f.style.top||t.px(r,\"paddingTop\");y.style.width=m+\"px\";y.style.height=u+\"px\";y.style.position=\"absolute\";t.isGecko&&256&i&&(y.style.opacity=\"0\");y.style.display=l.display;var x=p&&h.to<h.from,b=\"\";switch(255&i){case 1:x=!x;case 2:b=\"slide\";break;case 3:b=\"slideup\";break;case 4:b=\"slidedown\";break;case 5:b=\"pop\"}256&i&&(b+=\" fade\");x&&(b+=\" reverse\");f.style[t.styleAttribute(\"animation-duration\")]=s+\"ms\";y.style[t.styleAttribute(\"animation-duration\")]=s+\"ms\";f.style[t.styleAttribute(\"animation-timing-function\")]=a[[0,1,3,2,4,5][o]];y.style[t.styleAttribute(\"animation-timing-function\")]=a[o];c.addClass(b+\" out\");c.one(d,(function(){c.removeClass(b+\" out\");f.style.display=\"none\";if(r.lh&&y.lh){y.style.height=\"\";y.lh=!1}f.style[t.styleAttribute(\"animation-duration\")]=\"\";f.style[t.styleAttribute(\"animation-timing-function\")]=\"\"}));g.addClass(b+\" in\");g.one(d,(function(){g.removeClass(b+\" in\");y.style.position=\"\";y.style.left=\"\";y.style.width=\"\";y.style.top=\"\";r.lh?y.lh=!0:y.lh||(y.style.height=\"\");t.isGecko&&256&i&&(y.style.opacity=\"1\");y.style[t.styleAttribute(\"animation-duration\")]=\"\";y.style[t.styleAttribute(\"animation-timing-function\")]=\"\"}))}}}};n(t,e,i,o,s,l)})");
  }
}
