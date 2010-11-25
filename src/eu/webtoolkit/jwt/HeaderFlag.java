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
 * Flags that indicate table header options.
 * <p>
 * 
 * @see WAbstractItemModel#getHeaderFlags(int section, Orientation orientation)
 */
public enum HeaderFlag {
	/**
	 * Flag that indicates that the column can be expanded.
	 * <p>
	 * 
	 * @see WAbstractItemModel#expandColumn(int column)
	 */
	ColumnIsCollapsed,
	/**
	 * Flag that indicates that the column was expanded to the left.
	 * <p>
	 * 
	 * @see WAbstractItemModel#collapseColumn(int column)
	 */
	ColumnIsExpandedLeft,
	/**
	 * Flag that indicates that the column was expanded to the right.
	 * <p>
	 * 
	 * @see WAbstractItemModel#collapseColumn(int column)
	 */
	ColumnIsExpandedRight;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
