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

/**
 * A container widget that stacks its widgets on top of each other.
 * <p>
 * 
 * This is a container widgets which at all times has only one item visible. The
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
	/**
	 * Creates a new stack.
	 */
	public WStackedWidget(WContainerWidget parent) {
		super(parent);
		this.animation_ = new WAnimation();
		this.currentIndex_ = -1;
		;
		this.setJavaScriptMember(WT_RESIZE_JS, StdGridLayoutImpl
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
					"Wt3_1_10.WStackedWidget.animateChild");
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
		if (app.getEnvironment().agentIsWebKit()) {
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
				"function(){function p(h,i,j,k,l){function r(){var f,s=e.childNodes.length,m=-1,n=-1;for(f=0;f<s&&(m==-1||n==-1);++f){var o=e.childNodes[f];if(o==h)n=f;else if(o.style.display!==\"none\"&&!$(o).hasClass(\"out\"))m=f}return{from:m,to:n}}function t(){debugger;$(c).removeClass(b+\" out\");c.style.display=\"none\";c.style[\"-webkit-animation-duration\"]=\"\";c.style[\"-webkit-animation-timing-function\"]=\"\";$(a).removeClass(b+\" in\");a.style.left=\"\";a.style.width= \"\";a.style.top=\"\";a.style.height=\"\";a.style.position=\"\";a.style[\"-webkit-animation-duration\"]=\"\";a.style[\"-webkit-animation-timing-function\"]=\"\"}var q=[\"ease\",\"linear\",\"ease-in\",\"ease-out\",\"ease-in-out\"];if(l.display!==\"none\"){var e=h.parentNode,g=e.wtAutoReverse,d=r();if(!(d.from==-1||d.to==-1||d.from==d.to)){var c=e.childNodes[d.from],a=e.childNodes[d.to],u=e.offsetHeight,v=e.offsetWidth;if($(c).hasClass(\"in\"))$(c).one(\"webkitAnimationEnd\",function(){p(h,i,j,k,l)});else{a.style.left=\"0px\";a.style.top= \"0px\";a.style.width=v+\"px\";a.style.height=u+\"px\";a.style.position=\"absolute\";a.style.display=l.display;g=g&&d.to<d.from;var b=\"\";switch(i&255){case 1:g=!g;case 2:b=\"slide\";break;case 3:b=\"slideup\";break;case 4:b=\"slidedown\";break;case 5:b=\"pop\";break}if(i&256)b+=\" fade\";if(g)b+=\" reverse\";c.style[\"-webkit-animation-duration\"]=k+\"ms\";a.style[\"-webkit-animation-duration\"]=k+\"ms\";c.style[\"-webkit-animation-timing-function\"]=q[[0,1,3,2,4,5][j]];a.style[\"-webkit-animation-timing-function\"]=q[j];$(c).addClass(b+ \" out\");$(a).addClass(b+\" in\");$(a).one(\"webkitAnimationEnd\",t)}}}}return{animateChild:p}}()");
	}
}
