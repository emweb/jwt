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
				"function(E,h){jQuery.data(h,\"obj\",this);var d=E.WT,C=[],y=[];this.wtResize=function(c,a,f,e){function w(l){var j=d.px(l,\"marginTop\");j+=d.px(l,\"marginBottom\");if(!d.boxSizing(l)){j+=d.px(l,\"borderTopWidth\");j+=d.px(l,\"borderBottomWidth\");j+=d.px(l,\"paddingTop\");j+=d.px(l,\"paddingBottom\")}return j}var n=f>=0;c.lh=n&&e;c.style.height=n?f+\"px\":\"\";if(d.boxSizing(c)){f-=d.px(c,\"marginTop\");f-=d.px(c,\"marginBottom\");f-=d.px(c,\"borderTopWidth\"); f-=d.px(c,\"borderBottomWidth\");f-=d.px(c,\"paddingTop\");f-=d.px(c,\"paddingBottom\");a-=d.px(c,\"marginLeft\");a-=d.px(c,\"marginRight\");a-=d.px(c,\"borderLeftWidth\");a-=d.px(c,\"borderRightWidth\");a-=d.px(c,\"paddingLeft\");a-=d.px(c,\"paddingRight\")}var r,D,g;r=0;for(D=c.childNodes.length;r<D;++r){g=c.childNodes[r];if(g.nodeType==1)if(!d.isHidden(g))if(n){var s=f-w(g);if(s>0)if(g.wtResize)g.wtResize(g,a,s,e);else{s=s+\"px\";if(g.style.height!=s){g.style.height=s;g.lh=e}}}else if(g.wtResize)g.wtResize(g,a,-1, e);else{g.style.height=\"\";g.lh=false}}};this.wtGetPs=function(c,a,f,e){return e};this.adjustScroll=function(c){var a,f,e,w=h.scrollLeft,n=h.scrollTop;a=0;for(f=h.childNodes.length;a<f;++a){e=h.childNodes[a];if(e.nodeType==1)if(e!=c){if(e.style.display!=\"none\"){y[a]=w;C[a]=n}}else if(typeof y[a]!==\"undefined\"){h.scrollLeft=y[a];h.scrollTop=C[a]}else{h.scrollLeft=0;h.scrollTop=0}}};this.setCurrent=function(c){var a,f,e;this.adjustScroll(c);a=0;for(f=h.childNodes.length;a<f;++a){e=h.childNodes[a];if(e.nodeType== 1)if(e!=c){if(e.style.display!=\"none\")e.style.display=\"none\"}else{e.style.display=\"\";if(h.lh){h.lh=false;h.style.height=\"\"}}}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"WStackedWidget.prototype.animateChild",
				"function(E,h,d,C,y,c){doAnimateChild=function(a,f,e,w,n,r){function D(t){var z=[\"Moz\",\"Webkit\"],A=document.createElement(\"div\"),o,x;o=0;for(x=z.length;o<x;++o)if(z[o]+t in A.style)return z[o];return\"\"}function g(){var t,z=i.childNodes.length,A=-1,o=-1;for(t=0;t<z&&(A==-1||o==-1);++t){var x=i.childNodes[t];if(x==f)o=t;else if(x.style.display!==\"none\"&&!$(x).hasClass(\"out\"))A=t}return{from:A,to:o}}function s(){$(k).removeClass(m+ \" out\");k.style.display=\"none\";k.style[u+\"Duration\"]=\"\";k.style[u+\"TimingFunction\"]=\"\";$(b).removeClass(m+\" in\");b.style.left=\"\";b.style.width=\"\";b.style.top=\"\";b.style.height=\"\";b.style.position=\"\";b.style[u+\"Duration\"]=\"\";b.style[u+\"TimingFunction\"]=\"\"}var l=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],j=D(\"AnimationDuration\"),u=j+\"Animation\";j=j==\"Moz\"?\"animationend\":\"webkitAnimationEnd\";if(r.display!==\"none\"){var i=f.parentNode,B=i.wtAutoReverse,v=g();if(!(v.from==-1||v.to==-1||v.from== v.to)){var k=i.childNodes[v.from],b=i.childNodes[v.to],p=i.scrollHeight,q=i.scrollWidth;if($(k).hasClass(\"in\"))$(k).one(j,function(){doAnimateChild(a,f,e,w,n,r)});else{p-=a.px(i,\"paddingTop\");p-=a.px(i,\"paddingBottom\");p-=a.px(b,\"marginTop\");p-=a.px(b,\"marginBottom\");p-=a.px(b,\"borderTopWidth\");p-=a.px(b,\"borderBottomWidth\");p-=a.px(b,\"paddingTop\");p-=a.px(b,\"paddingBottom\");q-=a.px(i,\"paddingLeft\");q-=a.px(i,\"paddingRight\");q-=a.px(b,\"marginLeft\");q-=a.px(b,\"marginRight\");q-=a.px(b,\"borderLeftWidth\"); q-=a.px(b,\"borderRightWidth\");q-=a.px(b,\"paddingLeft\");q-=a.px(b,\"paddingRight\");b.style.left=k.style.left||a.px(i,\"paddingLeft\");b.style.top=k.style.top||a.px(i,\"paddingTop\");b.style.width=q+\"px\";b.style.height=p+\"px\";b.style.position=\"absolute\";b.style.display=r.display;B=B&&v.to<v.from;var m=\"\";switch(e&255){case 1:B=!B;case 2:m=\"slide\";break;case 3:m=\"slideup\";break;case 4:m=\"slidedown\";break;case 5:m=\"pop\";break}if(e&256)m+=\" fade\";if(B)m+=\" reverse\";k.style[u+\"Duration\"]=n+\"ms\";b.style[u+\"Duration\"]= n+\"ms\";k.style[u+\"TimingFunction\"]=l[[0,1,3,2,4,5][w]];b.style[u+\"TimingFunction\"]=l[w];$(k).addClass(m+\" out\");$(b).addClass(m+\" in\");$(b).one(j,s)}}}};doAnimateChild(E,h,d,C,y,c)}");
	}
}
