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
		;
		this.addStyleClass("Wt-stack");
		this.javaScriptDefined_ = false;
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
		if (!animation.isEmpty() && this.isLoadAnimateJS()
				&& (this.isRendered() || !canOptimizeUpdates())) {
			if (canOptimizeUpdates() && index == this.currentIndex_) {
				return;
			}
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
			if (this.isRendered()) {
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
		if (this.isLoadAnimateJS()) {
			if (!animation.isEmpty()) {
				this.addStyleClass("Wt-animated");
			}
			this.animation_ = animation;
			this.autoReverseAnimation_ = autoReverse;
			if (this.isRendered()) {
				this.setJavaScriptMember("wtAnimateChild", "$('#"
						+ this.getId() + "').data('obj').animateChild");
				this.setJavaScriptMember("wtAutoReverse",
						this.autoReverseAnimation_ ? "true" : "false");
			}
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

	private boolean isLoadAnimateJS() {
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().supportsCss3Animations()) {
			app.loadJavaScript("js/WStackedWidget.js", wtjs2());
			return true;
		} else {
			return false;
		}
	}

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
			if (!this.animation_.isEmpty()) {
				this.isLoadAnimateJS();
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
				"function(s,n){jQuery.data(n,\"obj\",this);var a=s.WT,o=false;this.wtResize=function(b,f,h,e){function v(j){var g=a.px(j,\"marginTop\");g+=a.px(j,\"marginBottom\");if(!a.boxSizing(j)){g+=a.px(j,\"borderTopWidth\");g+=a.px(j,\"borderBottomWidth\");g+=a.px(j,\"paddingTop\");g+=a.px(j,\"paddingBottom\")}return g}if(e&&e.set[1]){b.style.height=h+\"px\";o=true}else o=false;if(a.boxSizing(b)){h-=a.px(b,\"marginTop\");h-=a.px(b,\"marginBottom\");h-=a.px(b,\"borderTopWidth\"); h-=a.px(b,\"borderBottomWidth\");h-=a.px(b,\"paddingTop\");h-=a.px(b,\"paddingBottom\");f-=a.px(b,\"marginLeft\");f-=a.px(b,\"marginRight\");f-=a.px(b,\"borderLeftWidth\");f-=a.px(b,\"borderRightWidth\");f-=a.px(b,\"paddingLeft\");f-=a.px(b,\"paddingRight\")}var p,c;e=0;for(p=b.childNodes.length;e<p;++e){c=b.childNodes[e];if(c.nodeType==1)if(!a.isHidden(c)){var i=h-v(c);if(i>0)if(c.wtResize)c.wtResize(c,f,i);else{i=i+\"px\";if(c.style.height!=i)c.style.height=i}}}};this.wtGetPs=function(b,f,h,e){return e};this.setCurrent= function(b){var f,h,e;f=0;for(h=n.childNodes.length;f<h;++f){e=n.childNodes[f];if(e.nodeType==1)if(e!=b)e.style.display=\"none\";else{e.style.display=\"\";if(o){o=false;n.style.height=\"\"}}}}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"WStackedWidget.prototype.animateChild",
				"function(s,n,a,o,b){function f(q){var t=[\"Moz\",\"Webkit\"],u=document.createElement(\"div\"),l,r;l=0;for(r=t.length;l<r;++l)if(t[l]+q in u.style)return t[l];return\"\"}function h(){var q,t=i.childNodes.length,u=-1,l=-1;for(q=0;q<t&&(u==-1||l==-1);++q){var r=i.childNodes[q];if(r==s)l=q;else if(r.style.display!==\"none\"&&!$(r).hasClass(\"out\"))u=q}return{from:u,to:l}}function e(){$(m).removeClass(k+\" out\");m.style.display=\"none\"; m.style[c+\"Duration\"]=\"\";m.style[c+\"TimingFunction\"]=\"\";$(d).removeClass(k+\" in\");d.style.left=\"\";d.style.width=\"\";d.style.top=\"\";d.style.height=\"\";d.style.position=\"\";d.style[c+\"Duration\"]=\"\";d.style[c+\"TimingFunction\"]=\"\"}var v=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],p=f(\"AnimationDuration\"),c=p+\"Animation\";p=p==\"Moz\"?\"animationend\":\"webkitAnimationEnd\";if(b.display!==\"none\"){var i=s.parentNode,j=i.wtAutoReverse,g=h();if(!(g.from==-1||g.to==-1||g.from==g.to)){var m=i.childNodes[g.from], d=i.childNodes[g.to],w=i.offsetHeight,x=i.offsetWidth;if($(m).hasClass(\"in\"))$(m).one(p,function(){animateChild(s,n,a,o,b)});else{d.style.left=\"0px\";d.style.top=\"0px\";d.style.width=x+\"px\";d.style.height=w+\"px\";d.style.position=\"absolute\";d.style.display=b.display;j=j&&g.to<g.from;var k=\"\";switch(n&255){case 1:j=!j;case 2:k=\"slide\";break;case 3:k=\"slideup\";break;case 4:k=\"slidedown\";break;case 5:k=\"pop\";break}if(n&256)k+=\" fade\";if(j)k+=\" reverse\";m.style[c+\"Duration\"]=o+\"ms\";d.style[c+\"Duration\"]= o+\"ms\";m.style[c+\"TimingFunction\"]=v[[0,1,3,2,4,5][a]];d.style[c+\"TimingFunction\"]=v[a];$(m).addClass(k+\" out\");$(d).addClass(k+\" in\");$(d).one(p,e)}}}}");
	}
}
