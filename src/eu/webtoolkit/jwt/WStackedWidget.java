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
		this.setJavaScriptMember(WT_RESIZE_JS, StdWidgetItemImpl
				.getChildrenResizeJS());
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
		if (this.isLoadAnimateJS() && !animation.isEmpty()
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
				this.getWidget(i).setHidden(this.currentIndex_ != i);
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
			this.setJavaScriptMember("wtAnimateChild",
					"Wt3_2_1.WStackedWidget.animateChild");
			this.setJavaScriptMember("wtAutoReverse",
					this.autoReverseAnimation_ ? "true" : "false");
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

	private WAnimation animation_;
	private boolean autoReverseAnimation_;
	private int currentIndex_;

	private boolean isLoadAnimateJS() {
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().supportsCss3Animations()) {
			app.loadJavaScript("js/WStackedWidget.js", wtjs1());
			return true;
		} else {
			return false;
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptObject,
				"WStackedWidget",
				"function(){function s(n,o,p,q,r){function u(e){var j=[\"Moz\",\"Webkit\"],k=document.createElement(\"div\"),c,i;c=0;for(i=j.length;c<i;++c)if(j[c]+e in k.style)return j[c]}function v(){var e,j=h.childNodes.length,k=-1,c=-1;for(e=0;e<j&&(k==-1||c==-1);++e){var i=h.childNodes[e];if(i==n)c=e;else if(i.style.display!==\"none\"&&!$(i).hasClass(\"out\"))k=e}return{from:k,to:c}}function w(){$(d).removeClass(b+\" out\");d.style.display=\"none\";d.style[f+\"Duration\"]= \"\";d.style[f+\"TimingFunction\"]=\"\";$(a).removeClass(b+\" in\");a.style.left=\"\";a.style.width=\"\";a.style.top=\"\";a.style.height=\"\";a.style.position=\"\";a.style[f+\"Duration\"]=\"\";a.style[f+\"TimingFunction\"]=\"\"}var t=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"],l=u(\"AnimationDuration\"),f=l+\"Animation\";l=l==\"Moz\"?\"animationend\":\"webkitAnimationEnd\";if(r.display!==\"none\"){var h=n.parentNode,m=h.wtAutoReverse,g=v();if(!(g.from==-1||g.to==-1||g.from==g.to)){var d=h.childNodes[g.from],a=h.childNodes[g.to], x=h.offsetHeight,y=h.offsetWidth;if($(d).hasClass(\"in\"))$(d).one(l,function(){s(n,o,p,q,r)});else{a.style.left=\"0px\";a.style.top=\"0px\";a.style.width=y+\"px\";a.style.height=x+\"px\";a.style.position=\"absolute\";a.style.display=r.display;m=m&&g.to<g.from;var b=\"\";switch(o&255){case 1:m=!m;case 2:b=\"slide\";break;case 3:b=\"slideup\";break;case 4:b=\"slidedown\";break;case 5:b=\"pop\";break}if(o&256)b+=\" fade\";if(m)b+=\" reverse\";d.style[f+\"Duration\"]=q+\"ms\";a.style[f+\"Duration\"]=q+\"ms\";d.style[f+\"TimingFunction\"]= t[[0,1,3,2,4,5][p]];a.style[f+\"TimingFunction\"]=t[p];$(d).addClass(b+\" out\");$(a).addClass(b+\" in\");$(a).one(l,w)}}}}return{animateChild:s}}()");
	}
}
