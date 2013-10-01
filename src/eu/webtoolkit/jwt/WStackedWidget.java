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
			this.animation_ = animation;
			this.autoReverseAnimation_ = autoReverse;
			this.loadAnimateJS();
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
		if (this.widgetsAdded_
				|| !EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
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
					"new Wt3_3_1.WStackedWidget(" + app.getJavaScriptClass()
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
				"function(B,h){function v(c){return c.nodeType==1}jQuery.data(h,\"obj\",this);var d=B.WT,z=[],w=[];this.wtResize=function(c,a,f,e){function t(j){var k=d.px(j,\"marginTop\");k+=d.px(j,\"marginBottom\");if(!d.boxSizing(j)){k+=d.px(j,\"borderTopWidth\");k+=d.px(j,\"borderBottomWidth\");k+=d.px(j,\"paddingTop\");k+=d.px(j,\"paddingBottom\")}return k}var m=f>=0;c.lh=m&&e;c.style.height=m?f+\"px\":\"\";if(d.boxSizing(c)){f-=d.px(c,\"marginTop\");f-=d.px(c,\"marginBottom\"); f-=d.px(c,\"borderTopWidth\");f-=d.px(c,\"borderBottomWidth\");f-=d.px(c,\"paddingTop\");f-=d.px(c,\"paddingBottom\");a-=d.px(c,\"marginLeft\");a-=d.px(c,\"marginRight\");a-=d.px(c,\"borderLeftWidth\");a-=d.px(c,\"borderRightWidth\");a-=d.px(c,\"paddingLeft\");a-=d.px(c,\"paddingRight\")}var p,A,g;p=0;for(A=c.childNodes.length;p<A;++p){g=c.childNodes[p];if(v(g))if(!d.isHidden(g))if(m){var q=f-t(g);if(q>0)if(g.wtResize)g.wtResize(g,a,q,e);else{q=q+\"px\";if(g.style.height!=q){g.style.height=q;g.lh=e}}}else if(g.wtResize)g.wtResize(g, a,-1,e);else{g.style.height=\"\";g.lh=false}}};this.wtGetPs=function(c,a,f,e){return e};this.adjustScroll=function(c){var a,f,e,t=h.scrollLeft,m=h.scrollTop;a=0;for(f=h.childNodes.length;a<f;++a){e=h.childNodes[a];if(v(e))if(e!=c){if(e.style.display!=\"none\"){w[a]=t;z[a]=m}}else if(typeof w[a]!==\"undefined\"){h.scrollLeft=w[a];h.scrollTop=z[a]}else{h.scrollLeft=0;h.scrollTop=0}}};this.setCurrent=function(c){var a,f,e;this.adjustScroll(c);a=0;for(f=h.childNodes.length;a<f;++a){e=h.childNodes[a];if(v(e))if(e!= c){if(e.style.display!=\"none\")e.style.display=\"none\"}else{e.style.display=\"\";if(h.lh){h.lh=false;h.style.height=\"\"}}}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"WStackedWidget.prototype.animateChild",
				"function(B,h,v,d,z,w){var c=function(a,f,e,t,m,p){function A(){var u,G=i.childNodes.length,C=-1,D=-1;for(u=0;u<G&&(C==-1||D==-1);++u){var E=i.childNodes[u];if(E==f)D=u;else if(E.style.display!==\"none\"&&!$(E).hasClass(\"out\"))C=u}return{from:C,to:D}}function g(){F.removeClass(l+\" in\");b.style.position=\"\";b.style.left=\"\";b.style.width=\"\";b.style.top=\"\";b.style.height=\"\";b.style[a.styleAttribute(\"animation-duration\")]= \"\";b.style[a.styleAttribute(\"animation-timing-function\")]=\"\"}function q(){x.removeClass(l+\" out\");r.style.display=\"none\";r.style[a.styleAttribute(\"animation-duration\")]=\"\";r.style[a.styleAttribute(\"animation-timing-function\")]=\"\"}var j=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],k=a.vendorPrefix(a.styleAttribute(\"animation-duration\"))==\"Webkit\"?\"webkitAnimationEnd\":\"animationend\";if(p.display!==\"none\"){var i=f.parentNode,y=i.wtAutoReverse,s=A();if(!(s.from==-1||s.to==-1||s.from==s.to)){var r= i.childNodes[s.from],b=i.childNodes[s.to],x=$(r),F=$(b),n=i.scrollHeight,o=i.scrollWidth;if(x.hasClass(\"in\"))x.one(k,function(){c(a,f,e,t,m,p)});else{n-=a.px(i,\"paddingTop\");n-=a.px(i,\"paddingBottom\");n-=a.px(b,\"marginTop\");n-=a.px(b,\"marginBottom\");n-=a.px(b,\"borderTopWidth\");n-=a.px(b,\"borderBottomWidth\");n-=a.px(b,\"paddingTop\");n-=a.px(b,\"paddingBottom\");o-=a.px(i,\"paddingLeft\");o-=a.px(i,\"paddingRight\");o-=a.px(b,\"marginLeft\");o-=a.px(b,\"marginRight\");o-=a.px(b,\"borderLeftWidth\");o-=a.px(b,\"borderRightWidth\"); o-=a.px(b,\"paddingLeft\");o-=a.px(b,\"paddingRight\");b.style.left=r.style.left||a.px(i,\"paddingLeft\");b.style.top=r.style.top||a.px(i,\"paddingTop\");b.style.width=o+\"px\";b.style.height=n+\"px\";b.style.position=\"absolute\";if(a.isGecko)b.style.opacity=\"0\";b.style.display=p.display;y=y&&s.to<s.from;var l=\"\";switch(e&255){case 1:y=!y;case 2:l=\"slide\";break;case 3:l=\"slideup\";break;case 4:l=\"slidedown\";break;case 5:l=\"pop\";break}if(e&256)l+=\" fade\";if(y)l+=\" reverse\";r.style[a.styleAttribute(\"animation-duration\")]= m+\"ms\";b.style[a.styleAttribute(\"animation-duration\")]=m+\"ms\";r.style[a.styleAttribute(\"animation-timing-function\")]=j[[0,1,3,2,4,5][t]];b.style[a.styleAttribute(\"animation-timing-function\")]=j[t];x.addClass(l+\" out\");x.one(k,q);F.addClass(l+\" in\");F.one(k,g);if(a.isGecko)b.style.opacity=\"\"}}}};c(B,h,v,d,z,w)}");
	}
}
