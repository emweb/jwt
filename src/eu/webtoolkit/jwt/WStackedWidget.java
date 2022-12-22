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
          "new Wt4_8_3.WStackedWidget(" + app.getJavaScriptClass() + "," + this.getJsRef() + ");");
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
        "function(D,j){function v(b){return b.nodeType==1&&!$(b).hasClass(\"wt-reparented\")&&!$(b).hasClass(\"resize-sensor\")}j.wtObj=this;var e=D.WT,B=[],w=[],s=null,a=null;this.reApplySize=function(){a&&this.wtResize(j,s,a,false)};this.wtResize=function(b,d,g,f){function p(k){var h=e.px(k,\"marginTop\");h+=e.px(k,\"marginBottom\");if(!e.boxSizing(k)){h+=e.px(k,\"borderTopWidth\");h+=e.px(k,\"borderBottomWidth\");h+=e.px(k,\"paddingTop\");h+=e.px(k,\"paddingBottom\")}return h} s=d;a=g;var t=g>=0;if(f)if(t){b.style.height=g+\"px\";b.lh=true}else{b.style.height=\"\";b.lh=false}else b.lh=false;if(e.boxSizing(b)){g-=e.px(b,\"marginTop\");g-=e.px(b,\"marginBottom\");g-=e.px(b,\"borderTopWidth\");g-=e.px(b,\"borderBottomWidth\");g-=e.px(b,\"paddingTop\");g-=e.px(b,\"paddingBottom\");d-=e.px(b,\"marginLeft\");d-=e.px(b,\"marginRight\");d-=e.px(b,\"borderLeftWidth\");d-=e.px(b,\"borderRightWidth\");d-=e.px(b,\"paddingLeft\");d-=e.px(b,\"paddingRight\")}var C,i;f=0;for(C=b.childNodes.length;f<C;++f){i=b.childNodes[f]; if(v(i))if(!e.isHidden(i)&&!$(i).hasClass(\"out\"))if(t){var l=g-p(i);if(l>0){if(i.offsetTop>0){var x=e.css(i,\"overflow\");if(x===\"visible\"||x===\"\")i.style.overflow=\"auto\"}if(i.wtResize)i.wtResize(i,d,l,true);else{l=l+\"px\";if(i.style.height!=l){i.style.height=l;i.lh=true}}}}else if(i.wtResize)i.wtResize(i,d,-1,true);else{i.style.height=\"\";i.lh=false}}};this.wtGetPs=function(b,d,g,f){return f};this.adjustScroll=function(b){var d,g,f,p=j.scrollLeft,t=j.scrollTop;d=0;for(g=j.childNodes.length;d<g;++d){f= j.childNodes[d];if(v(f))if(f!=b){if(f.style.display!=\"none\"){w[d]=p;B[d]=t}}else if(typeof w[d]!==\"undefined\"){j.scrollLeft=w[d];j.scrollTop=B[d]}else{j.scrollLeft=0;j.scrollTop=0}}};this.setCurrent=function(b){var d,g,f;this.adjustScroll(b);d=0;for(g=j.childNodes.length;d<g;++d){f=j.childNodes[d];if(v(f))if(f!=b){if(f.style.display!=\"none\")f.style.display=\"none\"}else{f.style.display=f.style.flexFlow?\"flex\":\"\";if(j.lh){j.lh=false;j.style.height=\"\"}}}this.reApplySize()}}");
  }

  static WJavaScriptPreamble wtjs2() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptPrototype,
        "WStackedWidget.prototype.animateChild",
        "function(D,j,v,e,B,w){var s=function(a,b,d,g,f,p){function t(){var u,H=h.childNodes.length,E=-1,F=-1;for(u=0;u<H&&(E==-1||F==-1);++u){var G=h.childNodes[u];if(G==b)F=u;else if(G.style.display!==\"none\"&&!$(G).hasClass(\"out\"))E=u}return{from:E,to:F}}function C(){y.removeClass(m+\" in\");c.style.position=\"\";c.style.left=\"\";c.style.width=\"\";c.style.top=\"\";if(h.lh)c.lh=true;else if(!c.lh)c.style.height=\"\";if(a.isGecko&&d& l)c.style.opacity=\"1\";c.style[a.styleAttribute(\"animation-duration\")]=\"\";c.style[a.styleAttribute(\"animation-timing-function\")]=\"\"}function i(){z.removeClass(m+\" out\");q.style.display=\"none\";if(h.lh)if(c.lh){c.style.height=\"\";c.lh=false}q.style[a.styleAttribute(\"animation-duration\")]=\"\";q.style[a.styleAttribute(\"animation-timing-function\")]=\"\"}var l=256,x=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],k=a.vendorPrefix(a.styleAttribute(\"animation-duration\"))==\"Webkit\"?\"webkitAnimationEnd\":\"animationend\"; if(p.display!==\"none\"){var h=b.parentNode,A=h.wtAutoReverse,r=t();if(!(r.from==-1||r.to==-1||r.from==r.to)){var q=h.childNodes[r.from],c=h.childNodes[r.to],z=$(q),y=$(c),n=h.scrollHeight,o=h.scrollWidth;if(z.hasClass(\"in\"))z.one(k,function(){s(a,b,d,g,1,p)});else if(y.hasClass(\"out\"))y.one(k,function(){s(a,b,d,g,1,p)});else{n-=a.px(h,\"paddingTop\");n-=a.px(h,\"paddingBottom\");n-=a.px(c,\"marginTop\");n-=a.px(c,\"marginBottom\");n-=a.px(c,\"borderTopWidth\");n-=a.px(c,\"borderBottomWidth\");n-=a.px(c,\"paddingTop\"); n-=a.px(c,\"paddingBottom\");o-=a.px(h,\"paddingLeft\");o-=a.px(h,\"paddingRight\");o-=a.px(c,\"marginLeft\");o-=a.px(c,\"marginRight\");o-=a.px(c,\"borderLeftWidth\");o-=a.px(c,\"borderRightWidth\");o-=a.px(c,\"paddingLeft\");o-=a.px(c,\"paddingRight\");c.style.left=q.style.left||a.px(h,\"paddingLeft\");c.style.top=q.style.top||a.px(h,\"paddingTop\");c.style.width=o+\"px\";c.style.height=n+\"px\";c.style.position=\"absolute\";if(a.isGecko&&d&l)c.style.opacity=\"0\";c.style.display=p.display;A=A&&r.to<r.from;var m=\"\";switch(d& 255){case 1:A=!A;case 2:m=\"slide\";break;case 3:m=\"slideup\";break;case 4:m=\"slidedown\";break;case 5:m=\"pop\";break}if(d&l)m+=\" fade\";if(A)m+=\" reverse\";q.style[a.styleAttribute(\"animation-duration\")]=f+\"ms\";c.style[a.styleAttribute(\"animation-duration\")]=f+\"ms\";q.style[a.styleAttribute(\"animation-timing-function\")]=x[[0,1,3,2,4,5][g]];c.style[a.styleAttribute(\"animation-timing-function\")]=x[g];z.addClass(m+\" out\");z.one(k,i);y.addClass(m+\" in\");y.one(k,C)}}}};s(D,j,v,e,B,w)}");
  }
}
