/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
 * A layout item that holds a single widget.
 * <p>
 * 
 * @see WLayout#addWidget(WWidget w)
 */
public class WWidgetItem implements WLayoutItem {
	private static Logger logger = LoggerFactory.getLogger(WWidgetItem.class);

	/**
	 * Creates a new item for the given <i>widget</i>.
	 */
	public WWidgetItem(WWidget widget) {
		super();
		this.widget_ = widget;
		this.parentLayout_ = null;
		this.impl_ = null;
		this.widget_.setHasParent(true);
	}

	public WWidget getWidget() {
		return this.widget_;
	}

	public WLayout getLayout() {
		return null;
	}

	public WLayout getParentLayout() {
		return this.parentLayout_;
	}

	public WWidgetItem findWidgetItem(WWidget widget) {
		if (this.widget_ == widget) {
			return this;
		} else {
			return null;
		}
	}

	public WLayoutItemImpl getImpl() {
		return this.impl_;
	}

	private WWidget widget_;
	private WLayout parentLayout_;
	private WLayoutItemImpl impl_;

	public void setParentWidget(WWidget parent) {
		assert !(this.impl_ != null);
		this.impl_ = parent.createLayoutItemImpl(this);
	}

	public void setParentLayout(WLayout parentLayout) {
		this.parentLayout_ = parentLayout;
	}
}
