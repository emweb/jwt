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
		} else {
			widget.hide();
		}
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
		} else {
			widget.hide();
		}
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
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	private WAnimation animation_;
	private boolean autoReverseAnimation_;
	private int currentIndex_;
	private boolean javaScriptDefined_;
	private boolean loadAnimateJS_;

	private void defineJavaScript() {
		if (!this.javaScriptDefined_) {
			this.javaScriptDefined_ = true;
			WApplication app = WApplication.getInstance();
			app.loadJavaScript("js/WStackedWidget.js", wtjs1());
			this.setJavaScriptMember(" WStackedWidget",
					"new Wt3_2_3.WStackedWidget(" + app.getJavaScriptClass()
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
				"function(s,l){jQuery.data(l,\"obj\",this);var a=s.WT;this.wtResize=function(b,d,h,i){function v(f){var g=a.px(f,\"marginTop\");g+=a.px(f,\"marginBottom\");if(!a.boxSizing(f)){g+=a.px(f,\"borderTopWidth\");g+=a.px(f,\"borderBottomWidth\");g+=a.px(f,\"paddingTop\");g+=a.px(f,\"paddingBottom\")}return g}var q=h>=0;b.lh=q&&i;b.style.height=q?h+\"px\":\"\";if(a.boxSizing(b)){h-=a.px(b,\"marginTop\");h-=a.px(b,\"marginBottom\");h-=a.px(b,\"borderTopWidth\");h-=a.px(b, \"borderBottomWidth\");h-=a.px(b,\"paddingTop\");h-=a.px(b,\"paddingBottom\");d-=a.px(b,\"marginLeft\");d-=a.px(b,\"marginRight\");d-=a.px(b,\"borderLeftWidth\");d-=a.px(b,\"borderRightWidth\");d-=a.px(b,\"paddingLeft\");d-=a.px(b,\"paddingRight\")}var m,k,c;m=0;for(k=b.childNodes.length;m<k;++m){c=b.childNodes[m];if(c.nodeType==1)if(!a.isHidden(c))if(q){var j=h-v(c);if(j>0)if(c.wtResize)c.wtResize(c,d,j,i);else{j=j+\"px\";if(c.style.height!=j){c.style.height=j;c.lh=i}}}else if(c.wtResize)c.wtResize(c,d,-1,i);else{c.style.height= \"\";c.lh=false}}};this.wtGetPs=function(b,d,h,i){return i};this.setCurrent=function(b){var d,h,i;d=0;for(h=l.childNodes.length;d<h;++d){i=l.childNodes[d];if(i.nodeType==1)if(i!=b)i.style.display=\"none\";else{i.style.display=\"\";if(l.lh){l.lh=false;l.style.height=\"\"}}}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"WStackedWidget.prototype.animateChild",
				"function(s,l,a,b,d){function h(p){var t=[\"Moz\",\"Webkit\"],u=document.createElement(\"div\"),o,r;o=0;for(r=t.length;o<r;++o)if(t[o]+p in u.style)return t[o];return\"\"}function i(){var p,t=c.childNodes.length,u=-1,o=-1;for(p=0;p<t&&(u==-1||o==-1);++p){var r=c.childNodes[p];if(r==s)o=p;else if(r.style.display!==\"none\"&&!$(r).hasClass(\"out\"))u=p}return{from:u,to:o}}function v(){$(g).removeClass(n+\" out\");g.style.display=\"none\"; g.style[k+\"Duration\"]=\"\";g.style[k+\"TimingFunction\"]=\"\";$(e).removeClass(n+\" in\");e.style.left=\"\";e.style.width=\"\";e.style.top=\"\";e.style.height=\"\";e.style.position=\"\";e.style[k+\"Duration\"]=\"\";e.style[k+\"TimingFunction\"]=\"\"}var q=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],m=h(\"AnimationDuration\"),k=m+\"Animation\";m=m==\"Moz\"?\"animationend\":\"webkitAnimationEnd\";if(d.display!==\"none\"){var c=s.parentNode,j=c.wtAutoReverse,f=i();if(!(f.from==-1||f.to==-1||f.from==f.to)){var g=c.childNodes[f.from], e=c.childNodes[f.to],w=c.offsetHeight,x=c.offsetWidth;if($(g).hasClass(\"in\"))$(g).one(m,function(){animateChild(s,l,a,b,d)});else{e.style.left=\"0px\";e.style.top=\"0px\";e.style.width=x+\"px\";e.style.height=w+\"px\";e.style.position=\"absolute\";e.style.display=d.display;j=j&&f.to<f.from;var n=\"\";switch(l&255){case 1:j=!j;case 2:n=\"slide\";break;case 3:n=\"slideup\";break;case 4:n=\"slidedown\";break;case 5:n=\"pop\";break}if(l&256)n+=\" fade\";if(j)n+=\" reverse\";g.style[k+\"Duration\"]=b+\"ms\";e.style[k+\"Duration\"]= b+\"ms\";g.style[k+\"TimingFunction\"]=q[[0,1,3,2,4,5][a]];e.style[k+\"TimingFunction\"]=q[a];$(g).addClass(n+\" out\");$(e).addClass(n+\" in\");$(e).one(m,v)}}}}");
	}
}
