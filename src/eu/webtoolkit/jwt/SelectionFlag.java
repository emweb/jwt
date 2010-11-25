/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

/**
 * Enumeration that indicates how to change a selection.
 * <p>
 * 
 * @see WAbstractItemView#select(WModelIndex index, SelectionFlag option)
 */
public enum SelectionFlag {
	/**
	 * Add to selection.
	 */
	Select(1),
	/**
	 * Remove from selection.
	 */
	Deselect(2),
	/**
	 * Toggle in selection.
	 */
	ToggleSelect(3),
	/**
	 * Clear selection and add single item.
	 */
	ClearAndSelect(4);

	private int value;

	SelectionFlag(int value) {
		this.value = value;
	}

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return value;
	}
}
