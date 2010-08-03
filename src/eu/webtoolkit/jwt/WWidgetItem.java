/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A layout item that holds a single widget
 * <p>
 * 
 * @see WLayout#addWidget(WWidget w)
 */
public class WWidgetItem implements WLayoutItem {
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
