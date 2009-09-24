/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Flags that data item options.
 * <p>
 * 
 * @see WModelIndex#getFlags()
 */
public enum ItemFlag {
	/**
	 * Item can be selected.
	 */
	ItemIsSelectable,
	/**
	 * Item can be edited.
	 */
	ItemIsEditable,
	/**
	 * Item can be checked.
	 */
	ItemIsUserCheckable,
	/**
	 * Item can be dragged.
	 */
	ItemIsDragEnabled,
	/**
	 * Item can be a drop target.
	 */
	ItemIsDropEnabled,
	/**
	 * <p>
	 * Item has tree states. When set, {@link ItemDataRole#CheckStateRole} data
	 * is of type {@link CheckState}
	 */
	ItemIsTristate,
	/**
	 * Item&apos;s textual is HTML.
	 */
	ItemIsXHTMLText;

	public int getValue() {
		return ordinal();
	}
}
