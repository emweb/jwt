/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Enumeration that specifies an option for rendering a view item.
 * <p>
 * 
 * @see WAbstractItemDelegate#update(WWidget widget, WModelIndex index, EnumSet
 *      flags)
 */
public enum ViewItemRenderFlag {
	/**
	 * Render as selected.
	 */
	RenderSelected,
	/**
	 * Render in editing mode.
	 */
	RenderEditing,
	/**
	 * Render (the editor) focused.
	 */
	RenderFocused;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
