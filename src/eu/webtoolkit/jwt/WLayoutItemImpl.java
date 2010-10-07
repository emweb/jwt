/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * An abstract base class for implementing layout managers.
 * <p>
 * 
 * @see WLayoutItem
 * @see WLayout
 */
public interface WLayoutItemImpl {
	/**
	 * Adds a layout <i>item</i>.
	 * <p>
	 * The <code>item</code> already has an implementation set.
	 */
	public void updateAddItem(WLayoutItem item);

	/**
	 * Removes a layout <i>item</i>.
	 */
	public void updateRemoveItem(WLayoutItem item);

	/**
	 * Updates the layout.
	 */
	public void update(WLayoutItem item);

	/**
	 * Returns the widget for which this layout item participates in layout
	 * management.
	 */
	public WWidget getParentWidget();

	/**
	 * Provides a hint that can aid in layout strategy / algorithm.
	 */
	public void setHint(String name, String value);
}
