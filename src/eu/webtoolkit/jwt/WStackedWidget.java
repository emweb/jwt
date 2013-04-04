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
				"function(E,h){function x(a){return a.nodeType==1}jQuery.data(h,\"obj\",this);var d=E.WT,C=[],y=[];this.wtResize=function(a,c,f,e){function u(j){var i=d.px(j,\"marginTop\");i+=d.px(j,\"marginBottom\");if(!d.boxSizing(j)){i+=d.px(j,\"borderTopWidth\");i+=d.px(j,\"borderBottomWidth\");i+=d.px(j,\"paddingTop\");i+=d.px(j,\"paddingBottom\")}return i}var n=f>=0;a.lh=n&&e;a.style.height=n?f+\"px\":\"\";if(d.boxSizing(a)){f-=d.px(a,\"marginTop\");f-=d.px(a,\"marginBottom\"); f-=d.px(a,\"borderTopWidth\");f-=d.px(a,\"borderBottomWidth\");f-=d.px(a,\"paddingTop\");f-=d.px(a,\"paddingBottom\");c-=d.px(a,\"marginLeft\");c-=d.px(a,\"marginRight\");c-=d.px(a,\"borderLeftWidth\");c-=d.px(a,\"borderRightWidth\");c-=d.px(a,\"paddingLeft\");c-=d.px(a,\"paddingRight\")}var v,D,g;v=0;for(D=a.childNodes.length;v<D;++v){g=a.childNodes[v];if(x(g))if(!d.isHidden(g))if(n){var o=f-u(g);if(o>0)if(g.wtResize)g.wtResize(g,c,o,e);else{o=o+\"px\";if(g.style.height!=o){g.style.height=o;g.lh=e}}}else if(g.wtResize)g.wtResize(g, c,-1,e);else{g.style.height=\"\";g.lh=false}}};this.wtGetPs=function(a,c,f,e){return e};this.adjustScroll=function(a){var c,f,e,u=h.scrollLeft,n=h.scrollTop;c=0;for(f=h.childNodes.length;c<f;++c){e=h.childNodes[c];if(x(e))if(e!=a){if(e.style.display!=\"none\"){y[c]=u;C[c]=n}}else if(typeof y[c]!==\"undefined\"){h.scrollLeft=y[c];h.scrollTop=C[c]}else{h.scrollLeft=0;h.scrollTop=0}}};this.setCurrent=function(a){var c,f,e;this.adjustScroll(a);c=0;for(f=h.childNodes.length;c<f;++c){e=h.childNodes[c];if(x(e))if(e!= a){if(e.style.display!=\"none\")e.style.display=\"none\"}else{e.style.display=\"\";if(h.lh){h.lh=false;h.style.height=\"\"}}}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"WStackedWidget.prototype.animateChild",
				"function(E,h,x,d,C,y){doAnimateChild=function(a,c,f,e,u,n){function v(s){var z=[\"Moz\",\"Webkit\"],A=document.createElement(\"div\"),p,w;p=0;for(w=z.length;p<w;++p)if(z[p]+s in A.style)return z[p];return\"\"}function D(){var s,z=k.childNodes.length,A=-1,p=-1;for(s=0;s<z&&(A==-1||p==-1);++s){var w=k.childNodes[s];if(w==c)p=s;else if(w.style.display!==\"none\"&&!$(w).hasClass(\"out\"))A=s}return{from:A,to:p}}function g(){$(l).removeClass(m+ \" out\");l.style.display=\"none\";l.style[i+\"Duration\"]=\"\";l.style[i+\"TimingFunction\"]=\"\";$(b).removeClass(m+\" in\");b.style.left=\"\";b.style.width=\"\";b.style.top=\"\";b.style.height=\"\";b.style.position=\"\";b.style[i+\"Duration\"]=\"\";b.style[i+\"TimingFunction\"]=\"\"}var o=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],j=v(\"AnimationDuration\"),i=j+\"Animation\";j=j==\"Moz\"?\"animationend\":\"webkitAnimationEnd\";if(n.display!==\"none\"){var k=c.parentNode,B=k.wtAutoReverse,t=D();if(!(t.from==-1||t.to==-1||t.from== t.to)){var l=k.childNodes[t.from],b=k.childNodes[t.to],q=k.scrollHeight,r=k.scrollWidth;if($(l).hasClass(\"in\"))$(l).one(j,function(){doAnimateChild(a,c,f,e,u,n)});else{q-=a.px(k,\"paddingTop\");q-=a.px(k,\"paddingBottom\");q-=a.px(b,\"marginTop\");q-=a.px(b,\"marginBottom\");q-=a.px(b,\"borderTopWidth\");q-=a.px(b,\"borderBottomWidth\");q-=a.px(b,\"paddingTop\");q-=a.px(b,\"paddingBottom\");r-=a.px(k,\"paddingLeft\");r-=a.px(k,\"paddingRight\");r-=a.px(b,\"marginLeft\");r-=a.px(b,\"marginRight\");r-=a.px(b,\"borderLeftWidth\"); r-=a.px(b,\"borderRightWidth\");r-=a.px(b,\"paddingLeft\");r-=a.px(b,\"paddingRight\");b.style.left=l.style.left||a.px(k,\"paddingLeft\");b.style.top=l.style.top||a.px(k,\"paddingTop\");b.style.width=r+\"px\";b.style.height=q+\"px\";b.style.position=\"absolute\";b.style.display=n.display;B=B&&t.to<t.from;var m=\"\";switch(f&255){case 1:B=!B;case 2:m=\"slide\";break;case 3:m=\"slideup\";break;case 4:m=\"slidedown\";break;case 5:m=\"pop\";break}if(f&256)m+=\" fade\";if(B)m+=\" reverse\";l.style[i+\"Duration\"]=u+\"ms\";b.style[i+\"Duration\"]= u+\"ms\";l.style[i+\"TimingFunction\"]=o[[0,1,3,2,4,5][e]];b.style[i+\"TimingFunction\"]=o[e];$(l).addClass(m+\" out\");$(b).addClass(m+\" in\");$(b).one(j,g)}}}};doAnimateChild(E,h,x,d,C,y)}");
	}
}
