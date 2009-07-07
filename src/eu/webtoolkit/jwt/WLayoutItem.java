/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * An abstract base class for items that can participate in a layout
 * <p>
 * 
 * @see WLayout
 */
public interface WLayoutItem {
	/**
	 * Find the widget item corresponding to the given <i>widget</i>.
	 * <p>
	 * The widget is searched for recursively inside nested layouts.
	 */
	public WWidgetItem findWidgetItem(WWidget widget);

	/**
	 * Get the layout that implements this {@link WLayoutItem}.
	 * <p>
	 * This implements a type-safe upcasting mechanism to a {@link WLayout}.
	 */
	public WLayout getLayout();

	/**
	 * Get the widget that is held by this {@link WLayoutItem}.
	 * <p>
	 * This implements a type-safe upcasting mechanism to a {@link WWidgetItem}.
	 */
	public WWidget getWidget();

	/**
	 * Get the layout in which this item is contained.
	 */
	public WLayout getParentLayout();

	/**
	 * Get the implementation for this layout item.
	 * <p>
	 * The implementation of a layout item depends on the kind of container for
	 * which the layout does layout management.
	 */
	public WLayoutItemImpl getImpl();

	void setParent(WWidget parent);

	void setParentLayout(WLayout parentLayout);
}
