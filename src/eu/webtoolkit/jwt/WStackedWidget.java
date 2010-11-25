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
 * widget accomplishes this using {@link WWebWidget#setHidden(boolean hidden)
 * WWebWidget#setHidden()} on the children.
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
	 * Created a new stacked container widget.
	 */
	public WStackedWidget(WContainerWidget parent) {
		super(parent);
		this.currentIndex_ = -1;
		;
		this.setJavaScriptMember(WT_RESIZE_JS, StdGridLayoutImpl
				.getChildrenResizeJS());
	}

	/**
	 * Created a new stacked container widget.
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
		if (this.currentIndex_ >= 0) {
			return this.getWidget(this.currentIndex_);
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
	}

	/**
	 * Shows a particular widget.
	 * <p>
	 * The widget with index <code>index</code> is made visible, while all other
	 * widgets are invisible.
	 * <p>
	 * 
	 * @see WStackedWidget#getCurrentIndex()
	 * @see WStackedWidget#setCurrentWidget(WWidget widget)
	 */
	public void setCurrentIndex(int index) {
		this.currentIndex_ = index;
		for (int i = 0; i < this.getCount(); ++i) {
			this.getWidget(i).setHidden(this.currentIndex_ != i);
		}
	}

	/**
	 * Shows a particular widget.
	 * <p>
	 * The widget <code>widget</code>, which must have been added before, is
	 * made visible, while all other widgets are invisible.
	 * <p>
	 * 
	 * @see WStackedWidget#getCurrentWidget()
	 * @see WStackedWidget#setCurrentIndex(int index)
	 */
	public void setCurrentWidget(WWidget widget) {
		this.setCurrentIndex(this.getIndexOf(widget));
	}

	void removeChild(WWidget child) {
		super.removeChild(child);
		if (this.currentIndex_ >= this.getCount()) {
			this.setCurrentIndex(this.getCount() - 1);
		}
	}

	DomElement createDomElement(WApplication app) {
		this.setCurrentIndex(this.currentIndex_);
		return super.createDomElement(app);
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		this.setCurrentIndex(this.currentIndex_);
		super.getDomChanges(result, app);
	}

	private int currentIndex_;
}
