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
				"function(z,i){function u(c){return c.nodeType==1}jQuery.data(i,\"obj\",this);var d=z.WT,x=[],v=[];this.wtResize=function(c,a,f,e){function s(k){var g=d.px(k,\"marginTop\");g+=d.px(k,\"marginBottom\");if(!d.boxSizing(k)){g+=d.px(k,\"borderTopWidth\");g+=d.px(k,\"borderBottomWidth\");g+=d.px(k,\"paddingTop\");g+=d.px(k,\"paddingBottom\")}return g}var m=f>=0;c.lh=m&&e;c.style.height=m?f+\"px\":\"\";if(d.boxSizing(c)){f-=d.px(c,\"marginTop\");f-=d.px(c,\"marginBottom\"); f-=d.px(c,\"borderTopWidth\");f-=d.px(c,\"borderBottomWidth\");f-=d.px(c,\"paddingTop\");f-=d.px(c,\"paddingBottom\");a-=d.px(c,\"marginLeft\");a-=d.px(c,\"marginRight\");a-=d.px(c,\"borderLeftWidth\");a-=d.px(c,\"borderRightWidth\");a-=d.px(c,\"paddingLeft\");a-=d.px(c,\"paddingRight\")}var q,y,h;q=0;for(y=c.childNodes.length;q<y;++q){h=c.childNodes[q];if(u(h))if(!d.isHidden(h))if(m){var n=f-s(h);if(n>0)if(h.wtResize)h.wtResize(h,a,n,e);else{n=n+\"px\";if(h.style.height!=n){h.style.height=n;h.lh=e}}}else if(h.wtResize)h.wtResize(h, a,-1,e);else{h.style.height=\"\";h.lh=false}}};this.wtGetPs=function(c,a,f,e){return e};this.adjustScroll=function(c){var a,f,e,s=i.scrollLeft,m=i.scrollTop;a=0;for(f=i.childNodes.length;a<f;++a){e=i.childNodes[a];if(u(e))if(e!=c){if(e.style.display!=\"none\"){v[a]=s;x[a]=m}}else if(typeof v[a]!==\"undefined\"){i.scrollLeft=v[a];i.scrollTop=x[a]}else{i.scrollLeft=0;i.scrollTop=0}}};this.setCurrent=function(c){var a,f,e;this.adjustScroll(c);a=0;for(f=i.childNodes.length;a<f;++a){e=i.childNodes[a];if(u(e))if(e!= c){if(e.style.display!=\"none\")e.style.display=\"none\"}else{e.style.display=\"\";if(i.lh){i.lh=false;i.style.height=\"\"}}}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"WStackedWidget.prototype.animateChild",
				"function(z,i,u,d,x,v){var c=function(a,f,e,s,m,q){function y(){var t,D=g.childNodes.length,A=-1,B=-1;for(t=0;t<D&&(A==-1||B==-1);++t){var C=g.childNodes[t];if(C==f)B=t;else if(C.style.display!==\"none\"&&!$(C).hasClass(\"out\"))A=t}return{from:A,to:B}}function h(){$(j).removeClass(l+\" out\");j.style.display=\"none\";j.style[a.styleAttribute(\"animation-duration\")]=\"\";j.style[a.styleAttribute(\"animation-timing-function\")]=\"\"; $(b).removeClass(l+\" in\");b.style.left=\"\";b.style.width=\"\";b.style.top=\"\";b.style.height=\"\";b.style.position=\"\";b.style[a.styleAttribute(\"animation-duration\")]=\"\";b.style[a.styleAttribute(\"animation-timing-function\")]=\"\"}var n=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],k=a.vendorPrefix(a.styleAttribute(\"animation-duration\"))==\"Webkit\"?\"webkitAnimationEnd\":\"animationend\";if(q.display!==\"none\"){var g=f.parentNode,w=g.wtAutoReverse,r=y();if(!(r.from==-1||r.to==-1||r.from==r.to)){var j=g.childNodes[r.from], b=g.childNodes[r.to],o=g.scrollHeight,p=g.scrollWidth;if($(j).hasClass(\"in\"))$(j).one(k,function(){c(a,f,e,s,m,q)});else{o-=a.px(g,\"paddingTop\");o-=a.px(g,\"paddingBottom\");o-=a.px(b,\"marginTop\");o-=a.px(b,\"marginBottom\");o-=a.px(b,\"borderTopWidth\");o-=a.px(b,\"borderBottomWidth\");o-=a.px(b,\"paddingTop\");o-=a.px(b,\"paddingBottom\");p-=a.px(g,\"paddingLeft\");p-=a.px(g,\"paddingRight\");p-=a.px(b,\"marginLeft\");p-=a.px(b,\"marginRight\");p-=a.px(b,\"borderLeftWidth\");p-=a.px(b,\"borderRightWidth\");p-=a.px(b,\"paddingLeft\"); p-=a.px(b,\"paddingRight\");b.style.left=j.style.left||a.px(g,\"paddingLeft\");b.style.top=j.style.top||a.px(g,\"paddingTop\");b.style.width=p+\"px\";b.style.height=o+\"px\";b.style.position=\"absolute\";b.style.opacity=\"0\";b.style.display=q.display;w=w&&r.to<r.from;var l=\"\";switch(e&255){case 1:w=!w;case 2:l=\"slide\";break;case 3:l=\"slideup\";break;case 4:l=\"slidedown\";break;case 5:l=\"pop\";break}if(e&256)l+=\" fade\";if(w)l+=\" reverse\";j.style[a.styleAttribute(\"animation-duration\")]=m+\"ms\";b.style[a.styleAttribute(\"animation-duration\")]= m+\"ms\";j.style[a.styleAttribute(\"animation-timing-function\")]=n[[0,1,3,2,4,5][s]];b.style[a.styleAttribute(\"animation-timing-function\")]=n[s];$(j).addClass(l+\" out\");$(b).addClass(l+\" in\");b.style.opacity=\"\";$(b).one(k,h)}}}};c(z,i,u,d,x,v)}");
	}
}
