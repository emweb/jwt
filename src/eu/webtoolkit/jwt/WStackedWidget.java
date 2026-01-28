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
 * <p>When calling the above <code>
 * {@link WStackedWidget#setCurrentIndex(int index) setCurrentIndex()}</code>, this will fire the
 * {@link WStackedWidget#currentWidgetChanged() currentWidgetChanged()} signal. This allows
 * developers to know when the current visible widget has changed and what the new visible widget
 * is.
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
    this.loadPolicies_ = new ArrayList<ContentLoading>();
    this.currentWidgetChanged_ = new Signal1<WWidget>();
    this.hasEmittedChanged_ = false;
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
    int index = this.getCount() - 1;
    this.loadPolicies_.add(0 + index, ContentLoading.Eager);
    if (this.currentIndex_ == -1) {
      this.currentIndex_ = 0;
    }
    this.widgetsAdded_ = true;
  }
  // public Widget  addWidget(<Woow... some pseudoinstantiation type!> widget) ;
  public WWidget removeWidget(WWidget widget) {
    int index = this.getIndexOf(widget);
    WWidget result = super.removeWidget(widget);
    this.loadPolicies_.remove(0 + index);
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
    this.loadPolicies_.add(0 + index, ContentLoading.Eager);
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
   * <p>The default value for current index is 0 if there are child widgets, if no child widgets
   * were added this returns -1.
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
    boolean hasChanged = this.currentIndex_ != index;
    if (hasChanged) {
      this.hasEmittedChanged_ = false;
    }
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
            "setTimeout(function() { "
                + this.getJsRef()
                + ".wtObj.setCurrent("
                + this.getWidget(this.currentIndex_).getJsRef()
                + ");}, 0);");
      }
    }
    if ((hasChanged || !this.hasEmittedChanged_) && this.currentIndex_ >= 0) {
      if (this.loadPolicies_.get(this.currentIndex_) == ContentLoading.Lazy) {
        WContainerWidget container =
            ObjectUtils.cast(this.getCurrentWidget(), WContainerWidget.class);
        if (container.getCount() > 0) {
          this.currentWidgetChanged().trigger(container.getWidget(0));
          this.hasEmittedChanged_ = true;
        }
      } else {
        this.currentWidgetChanged().trigger(this.getCurrentWidget());
        this.hasEmittedChanged_ = true;
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
   * before calling {@link WStackedWidget#setCurrentIndex(int index) setCurrentIndex()}. </i>
   *
   * <p><i><b>Note: </b>It is also not supported to use a {@link AnimationEffect#Pop} animation on a
   * {@link WStackedWidget}. </i>
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
  /**
   * Signal which indicates that the current widget was changed.
   *
   * <p>This signal is emitted when the current widget was changed. It holds a pointer to the new
   * current widget. It is emitted every time the {@link WStackedWidget#setCurrentIndex(int index)
   * setCurrentIndex()} or {@link WStackedWidget#setCurrentWidget(WWidget widget)
   * setCurrentWidget()} is called.
   */
  public Signal1<WWidget> currentWidgetChanged() {
    return this.currentWidgetChanged_;
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
  int currentIndex_;
  private boolean widgetsAdded_;
  private boolean javaScriptDefined_;
  private boolean loadAnimateJS_;
  List<ContentLoading> loadPolicies_;
  private Signal1<WWidget> currentWidgetChanged_;
  private boolean hasEmittedChanged_;

  private void defineJavaScript() {
    if (!this.javaScriptDefined_) {
      this.javaScriptDefined_ = true;
      WApplication app = WApplication.getInstance();
      app.loadJavaScript("js/WStackedWidget.js", wtjs1());
      this.setJavaScriptMember(
          " WStackedWidget",
          "new Wt4_12_2.WStackedWidget(" + app.getJavaScriptClass() + "," + this.getJsRef() + ");");
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

  void setLoadPolicy(int index, ContentLoading loadPolicy) {
    this.loadPolicies_.set(index, loadPolicy);
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WStackedWidget",
        "(function(t,e){e.wtObj=this;const i=t.WT,o=[],s=[];let n=null,l=null;function d(t){return 1===t.nodeType&&!t.classList.contains(\"wt-reparented\")&&!t.classList.contains(\"resize-sensor\")}this.reApplySize=function(){l&&this.wtResize(e,n,l,!1)};this.wtResize=function(t,e,o,s){n=e;l=o;const p=o>=0;if(s)if(p){t.style.height=o+\"px\";t.lh=!0}else{t.style.height=\"\";t.lh=!1}else t.lh=!1;if(i.boxSizing(t)){o-=i.px(t,\"marginTop\");o-=i.px(t,\"marginBottom\");o-=i.pxComputedStyle(t,\"borderTopWidth\");o-=i.pxComputedStyle(t,\"borderBottomWidth\");o-=i.px(t,\"paddingTop\");o-=i.px(t,\"paddingBottom\");e-=i.px(t,\"marginLeft\");e-=i.px(t,\"marginRight\");e-=i.pxComputedStyle(t,\"borderLeftWidth\");e-=i.pxComputedStyle(t,\"borderRightWidth\");e-=i.px(t,\"paddingLeft\");e-=i.px(t,\"paddingRight\")}function a(t){let e=i.px(t,\"marginTop\");e+=i.px(t,\"marginBottom\");if(!i.boxSizing(t)){e+=i.pxComputedStyle(t,\"borderTopWidth\");e+=i.pxComputedStyle(t,\"borderBottomWidth\");e+=i.px(t,\"paddingTop\");e+=i.px(t,\"paddingBottom\")}return e}for(const s of t.childNodes)if(d(s)&&!i.isHidden(s)&&!s.classList.contains(\"out\"))if(p){const t=o-a(s);if(t>0){if(s.offsetTop>0){const t=i.css(s,\"overflow\");\"visible\"!==t&&\"\"!==t||(s.style.overflow=\"auto\")}if(s.wtResize)s.wtResize(s,e,t,!0);else{const e=t+\"px\";if(s.style.height!==e){s.style.height=e;s.lh=!0}}}}else if(s.wtResize)s.wtResize(s,e,-1,!0);else{s.style.height=\"\";s.lh=!1}};this.wtGetPs=function(t,e,i,o){return o};this.adjustScroll=function(t){const i=e.scrollLeft,n=e.scrollTop;for(let l=0,p=e.childNodes.length;l<p;++l){const p=e.childNodes[l];if(d(p))if(p!==t){if(\"none\"!==p.style.display){s[l]=i;o[l]=n}}else if(void 0!==s[l]){e.scrollLeft=s[l];e.scrollTop=o[l]}else{e.scrollLeft=0;e.scrollTop=0}}};this.setCurrent=function(t){this.adjustScroll(t);for(let i=0,o=e.childNodes.length;i<o;++i){const o=e.childNodes[i];if(d(o))if(o!==t)\"none\"!==o.style.display&&(o.style.display=\"none\");else{o.style.flexFlow?o.style.display=\"flex\":o.style.display=\"\";if(e.lh){e.lh=!1;e.style.height=\"\"}}}this.reApplySize()}})");
  }

  static WJavaScriptPreamble wtjs2() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptPrototype,
        "WStackedWidget.prototype.animateChild",
        "(function(t,e,i,o,s,n){const l=function(t,e,i,o,s,n){const d=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],p=\"Webkit\"===t.vendorPrefix(t.styleAttribute(\"animation-duration\"))?\"webkitAnimationEnd\":\"animationend\";if(\"none\"===n.display)return;const a=e.parentNode,r=a.wtAutoReverse;const c=function(){let t,i=-1,o=-1;const s=a.childNodes.length;for(t=0;t<s&&(-1===i||-1===o);++t){const s=a.childNodes[t];s===e?o=t:\"none\"===s.style.display||s.classList.contains(\"out\")||(i=t)}return{from:i,to:o}}();if(-1===c.from||-1===c.to||c.from===c.to)return;const h=a.childNodes[c.from],y=a.childNodes[c.to];let f=a.scrollHeight,u=a.scrollWidth;if(h.classList.contains(\"in\")){h.addEventListener(p,(function(){l(t,e,i,o,1,n)}),{once:!0});return}if(y.classList.contains(\"out\")){h.addEventListener(p,(function(){l(t,e,i,o,1,n)}),{once:!0});return}f-=t.px(a,\"paddingTop\");f-=t.px(a,\"paddingBottom\");f-=t.px(y,\"marginTop\");f-=t.px(y,\"marginBottom\");f-=t.pxComputedStyle(y,\"borderTopWidth\");f-=t.pxComputedStyle(y,\"borderBottomWidth\");f-=t.px(y,\"paddingTop\");f-=t.px(y,\"paddingBottom\");u-=t.px(a,\"paddingLeft\");u-=t.px(a,\"paddingRight\");u-=t.px(y,\"marginLeft\");u-=t.px(y,\"marginRight\");u-=t.pxComputedStyle(y,\"borderLeftWidth\");u-=t.pxComputedStyle(y,\"borderRightWidth\");u-=t.px(y,\"paddingLeft\");u-=t.px(y,\"paddingRight\");y.style.left=h.style.left||t.px(a,\"paddingLeft\");y.style.top=h.style.top||t.px(a,\"paddingTop\");y.style.width=u+\"px\";y.style.height=f+\"px\";y.style.position=\"absolute\";t.isGecko&&256&i&&(y.style.opacity=\"0\");y.style.display=n.display;let m=r&&c.to<c.from,g=[];switch(255&i){case 1:m=!m;case 2:g=[\"slide\"];break;case 3:g=[\"slideup\"];break;case 4:g=[\"slidedown\"];break;case 5:g=[\"pop\"]}256&i&&g.push(\"fade\");m&&g.push(\"reverse\");h.style[t.styleAttribute(\"animation-duration\")]=s+\"ms\";y.style[t.styleAttribute(\"animation-duration\")]=s+\"ms\";h.style[t.styleAttribute(\"animation-timing-function\")]=d[[0,1,3,2,4,5][o]];y.style[t.styleAttribute(\"animation-timing-function\")]=d[o];h.classList.add(...g,\"out\");h.addEventListener(p,(function(){h.classList.remove(...g,\"out\");h.style.display=\"none\";if(a.lh&&y.lh){y.style.height=\"\";y.lh=!1}h.style[t.styleAttribute(\"animation-duration\")]=\"\";h.style[t.styleAttribute(\"animation-timing-function\")]=\"\"}),{once:!0});y.classList.add(...g,\"in\");y.addEventListener(p,(function(){y.classList.remove(...g,\"in\");y.style.position=\"\";y.style.left=\"\";y.style.width=\"\";y.style.top=\"\";a.lh?y.lh=!0:y.lh||(y.style.height=\"\");t.isGecko&&256&i&&(y.style.opacity=\"1\");y.style[t.styleAttribute(\"animation-duration\")]=\"\";y.style[t.styleAttribute(\"animation-timing-function\")]=\"\"}),{once:!0})};l(t,e,i,o,s,n)})");
  }
}
