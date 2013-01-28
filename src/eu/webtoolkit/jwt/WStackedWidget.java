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

/**
 * A container widget that stacks its widgets on top of each other.
 * <p>
 * 
 * This is a container widget which at all times has only one item visible. The
 * widget accomplishes this using setHidden(bool) on the children.
 * <p>
 * Using {@link WStackedWidget#getCurrentIndex() getCurrentIndex()} and
 * {@link WStackedWidget#setCurrentIndex(int index) setCurrentIndex()} you can
 * retrieve or set the visible widget.
 * <p>
 * WStackedWidget, like {@link WContainerWidget}, is by default not inline.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The widget is rendered using an HTML <code>&lt;div&gt;</code> tag and does
 * not provide styling. It can be styled using inline or external CSS as
 * appropriate.
 * <p>
 * 
 * @see WMenu
 */
public class WStackedWidget extends WContainerWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WStackedWidget.class);

	/**
	 * Creates a new stack.
	 */
	public WStackedWidget(WContainerWidget parent) {
		super(parent);
		this.animation_ = new WAnimation();
		this.currentIndex_ = -1;
		this.widgetsAdded_ = false;
		this.javaScriptDefined_ = false;
		this.loadAnimateJS_ = false;
		;
		this.addStyleClass("Wt-stack");
	}

	/**
	 * Creates a new stack.
	 * <p>
	 * Calls {@link #WStackedWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
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

	/**
	 * Returns the index of the widget that is currently shown.
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
	 * <p>
	 * 
	 * @see WStackedWidget#setCurrentWidget(WWidget widget)
	 * @see WStackedWidget#getCurrentIndex()
	 */
	public WWidget getCurrentWidget() {
		if (this.getCurrentIndex() >= 0) {
			return this.getWidget(this.getCurrentIndex());
		} else {
			return null;
		}
	}

	/**
	 * Insert a widget at a given index.
	 */
	public void insertWidget(int index, WWidget widget) {
		super.insertWidget(index, widget);
		if (this.currentIndex_ == -1) {
			this.currentIndex_ = 0;
		}
		this.widgetsAdded_ = true;
	}

	/**
	 * Changes the current widget.
	 * <p>
	 * The widget with index <code>index</code> is made visible, while all other
	 * widgets are hidden.
	 * <p>
	 * The change of current widget is done using the animation settings
	 * specified by
	 * {@link WStackedWidget#setTransitionAnimation(WAnimation animation, boolean autoReverse)
	 * setTransitionAnimation()}.
	 * <p>
	 * The default value for current index is 0 (provided thath
	 * <p>
	 * 
	 * @see WStackedWidget#getCurrentIndex()
	 * @see WStackedWidget#setCurrentWidget(WWidget widget)
	 */
	public void setCurrentIndex(int index) {
		this
				.setCurrentIndex(index, this.animation_,
						this.autoReverseAnimation_);
	}

	/**
	 * Changes the current widget using a custom animation.
	 * <p>
	 * 
	 * @see WStackedWidget#getCurrentIndex()
	 * @see WStackedWidget#setCurrentWidget(WWidget widget)
	 */
	public void setCurrentIndex(int index, WAnimation animation,
			boolean autoReverse) {
		if (!animation.isEmpty()
				&& WApplication.getInstance().getEnvironment()
						.supportsCss3Animations()
				&& (this.isRendered() && this.javaScriptDefined_ || !canOptimizeUpdates())) {
			if (canOptimizeUpdates() && index == this.currentIndex_) {
				return;
			}
			this.loadAnimateJS();
			WWidget previous = this.getCurrentWidget();
			this.doJavaScript("$('#" + this.getId()
					+ "').data('obj').adjustScroll("
					+ this.getWidget(this.currentIndex_).getJsRef() + ");");
			this.setJavaScriptMember("wtAutoReverse", autoReverse ? "true"
					: "false");
			if (previous != null) {
				previous.animateHide(animation);
			}
			this.getWidget(index).animateShow(animation);
			this.currentIndex_ = index;
		} else {
			this.currentIndex_ = index;
			for (int i = 0; i < this.getCount(); ++i) {
				if (this.getWidget(i).isHidden() != (this.currentIndex_ != i)) {
					this.getWidget(i).setHidden(this.currentIndex_ != i);
				}
			}
			if (this.currentIndex_ >= 0 && this.isRendered()
					&& this.javaScriptDefined_) {
				this.doJavaScript("$('#" + this.getId()
						+ "').data('obj').setCurrent("
						+ this.getWidget(this.currentIndex_).getJsRef() + ");");
			}
		}
	}

	/**
	 * Changes the current widget using a custom animation.
	 * <p>
	 * Calls
	 * {@link #setCurrentIndex(int index, WAnimation animation, boolean autoReverse)
	 * setCurrentIndex(index, animation, true)}
	 */
	public final void setCurrentIndex(int index, WAnimation animation) {
		setCurrentIndex(index, animation, true);
	}

	/**
	 * Changes the current widget.
	 * <p>
	 * The widget <code>widget</code>, which must have been added before, is
	 * made visible, while all other widgets are hidden.
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
	 * <p>
	 * The animation is used to hide the previously current widget and show the
	 * next current widget using
	 * {@link WStackedWidget#setCurrentIndex(int index) setCurrentIndex()}.
	 * <p>
	 * The initial value for <code>animation</code> is WAnimation(), specifying
	 * no animation.
	 * <p>
	 * When <code>autoReverse</code> is set to <code>true</code>, then the
	 * reverse animation is chosen when the new index precedes the current
	 * index. This only applies to WAnimation::SlideLeft,
	 * WAnimation::SlideRight, WAnimation::SlideUp or WAnimation::SlideDown
	 * transition effects.
	 * <p>
	 * 
	 * @see WStackedWidget#setCurrentIndex(int index)
	 */
	public void setTransitionAnimation(WAnimation animation, boolean autoReverse) {
		if (WApplication.getInstance().getEnvironment()
				.supportsCss3Animations()) {
			if (!animation.isEmpty()) {
				this.addStyleClass("Wt-animated");
			}
			this.loadAnimateJS();
			this.animation_ = animation;
			this.autoReverseAnimation_ = autoReverse;
		}
	}

	/**
	 * Specifies an animation used during transitions.
	 * <p>
	 * Calls
	 * {@link #setTransitionAnimation(WAnimation animation, boolean autoReverse)
	 * setTransitionAnimation(animation, false)}
	 */
	public final void setTransitionAnimation(WAnimation animation) {
		setTransitionAnimation(animation, false);
	}

	void removeChild(WWidget child) {
		super.removeChild(child);
		if (this.currentIndex_ >= this.getCount()) {
			this.currentIndex_ = -1;
			if (this.getCount() > 0) {
				this.setCurrentIndex(this.getCount() - 1);
			}
		}
	}

	DomElement createDomElement(WApplication app) {
		return super.createDomElement(app);
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		super.getDomChanges(result, app);
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (this.widgetsAdded_) {
			for (int i = 0; i < this.getCount(); ++i) {
				if (this.getWidget(i).isHidden() != (this.currentIndex_ != i)) {
					this.getWidget(i).setHidden(this.currentIndex_ != i);
				}
			}
			this.widgetsAdded_ = false;
		}
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
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
			this.setJavaScriptMember(" WStackedWidget",
					"new Wt3_3_0.WStackedWidget(" + app.getJavaScriptClass()
							+ "," + this.getJsRef() + ");");
			this.setJavaScriptMember(WT_RESIZE_JS, "$('#" + this.getId()
					+ "').data('obj').wtResize");
			this.setJavaScriptMember(WT_GETPS_JS, "$('#" + this.getId()
					+ "').data('obj').wtGetPs");
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
				WApplication.getInstance().loadJavaScript(
						"js/WStackedWidget.js", wtjs2());
				this.setJavaScriptMember("wtAnimateChild", "$('#"
						+ this.getId() + "').data('obj').animateChild");
				this.setJavaScriptMember("wtAutoReverse",
						this.autoReverseAnimation_ ? "true" : "false");
			}
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WStackedWidget",
				"function(e,h){jQuery.data(h,\"obj\",this);var d=e.WT,u=[],t=[];this.wtResize=function(b,c,i,f){function v(g){var a=d.px(g,\"marginTop\");a+=d.px(g,\"marginBottom\");if(!d.boxSizing(g)){a+=d.px(g,\"borderTopWidth\");a+=d.px(g,\"borderBottomWidth\");a+=d.px(g,\"paddingTop\");a+=d.px(g,\"paddingBottom\")}return a}var n=i>=0;b.lh=n&&f;b.style.height=n?i+\"px\":\"\";if(d.boxSizing(b)){i-=d.px(b,\"marginTop\");i-=d.px(b,\"marginBottom\");i-=d.px(b,\"borderTopWidth\"); i-=d.px(b,\"borderBottomWidth\");i-=d.px(b,\"paddingTop\");i-=d.px(b,\"paddingBottom\");c-=d.px(b,\"marginLeft\");c-=d.px(b,\"marginRight\");c-=d.px(b,\"borderLeftWidth\");c-=d.px(b,\"borderRightWidth\");c-=d.px(b,\"paddingLeft\");c-=d.px(b,\"paddingRight\")}var m,k,j;m=0;for(k=b.childNodes.length;m<k;++m){j=b.childNodes[m];if(j.nodeType==1)if(!d.isHidden(j))if(n){var l=i-v(j);if(l>0)if(j.wtResize)j.wtResize(j,c,l,f);else{l=l+\"px\";if(j.style.height!=l){j.style.height=l;j.lh=f}}}else if(j.wtResize)j.wtResize(j,c,-1, f);else{j.style.height=\"\";j.lh=false}}};this.wtGetPs=function(b,c,i,f){return f};this.adjustScroll=function(b){var c,i,f,v=h.scrollLeft,n=h.scrollTop;c=0;for(i=h.childNodes.length;c<i;++c){f=h.childNodes[c];if(f.nodeType==1)if(f!=b){if(f.style.display!=\"none\"){t[c]=v;u[c]=n}}else if(typeof t[c]!==\"undefined\"){h.scrollLeft=t[c];h.scrollTop=u[c]}else{h.scrollLeft=0;h.scrollTop=0}}};this.setCurrent=function(b){var c,i,f;this.adjustScroll(b);c=0;for(i=h.childNodes.length;c<i;++c){f=h.childNodes[c];if(f.nodeType== 1)if(f!=b){if(f.style.display!=\"none\")f.style.display=\"none\"}else{f.style.display=\"\";if(h.lh){h.lh=false;h.style.height=\"\"}}}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"WStackedWidget.prototype.animateChild",
				"function(e,h,d,u,t,b){function c(s){var x=[\"Moz\",\"Webkit\"],y=document.createElement(\"div\"),p,w;p=0;for(w=x.length;p<w;++p)if(x[p]+s in y.style)return x[p];return\"\"}function i(){var s,x=k.childNodes.length,y=-1,p=-1;for(s=0;s<x&&(y==-1||p==-1);++s){var w=k.childNodes[s];if(w==h)p=s;else if(w.style.display!==\"none\"&&!$(w).hasClass(\"out\"))y=s}return{from:y,to:p}}function f(){$(g).removeClass(o+\" out\");g.style.display= \"none\";g.style[m+\"Duration\"]=\"\";g.style[m+\"TimingFunction\"]=\"\";$(a).removeClass(o+\" in\");a.style.left=\"\";a.style.width=\"\";a.style.top=\"\";a.style.height=\"\";a.style.position=\"\";a.style[m+\"Duration\"]=\"\";a.style[m+\"TimingFunction\"]=\"\"}var v=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],n=c(\"AnimationDuration\"),m=n+\"Animation\";n=n==\"Moz\"?\"animationend\":\"webkitAnimationEnd\";if(b.display!==\"none\"){var k=h.parentNode,j=k.wtAutoReverse,l=i();if(!(l.from==-1||l.to==-1||l.from==l.to)){var g=k.childNodes[l.from], a=k.childNodes[l.to],q=k.scrollHeight,r=k.scrollWidth;if($(g).hasClass(\"in\"))$(g).one(n,function(){animateChild(h,d,u,t,b)});else{q-=e.px(k,\"paddingTop\");q-=e.px(k,\"paddingBottom\");q-=e.px(a,\"marginTop\");q-=e.px(a,\"marginBottom\");q-=e.px(a,\"borderTopWidth\");q-=e.px(a,\"borderBottomWidth\");q-=e.px(a,\"paddingTop\");q-=e.px(a,\"paddingBottom\");r-=e.px(k,\"paddingLeft\");r-=e.px(k,\"paddingRight\");r-=e.px(a,\"marginLeft\");r-=e.px(a,\"marginRight\");r-=e.px(a,\"borderLeftWidth\");r-=e.px(a,\"borderRightWidth\");r-= e.px(a,\"paddingLeft\");r-=e.px(a,\"paddingRight\");a.style.left=g.style.left||e.px(k,\"paddingLeft\");a.style.top=g.style.top||e.px(k,\"paddingTop\");a.style.width=r+\"px\";a.style.height=q+\"px\";a.style.position=\"absolute\";a.style.display=b.display;e=j&&l.to<l.from;var o=\"\";switch(d&255){case 1:e=!e;case 2:o=\"slide\";break;case 3:o=\"slideup\";break;case 4:o=\"slidedown\";break;case 5:o=\"pop\";break}if(d&256)o+=\" fade\";if(e)o+=\" reverse\";g.style[m+\"Duration\"]=t+\"ms\";a.style[m+\"Duration\"]=t+\"ms\";g.style[m+\"TimingFunction\"]= v[[0,1,3,2,4,5][u]];a.style[m+\"TimingFunction\"]=v[u];$(g).addClass(o+\" out\");$(a).addClass(o+\" in\");$(a).one(n,f)}}}}");
	}
}
