/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a drop action.
 * <p>
 * 
 * @see WAbstractItemModel#dropEvent(WDropEvent e, DropAction action, int row,
 *      int column, WModelIndex parent)
 */
public enum DropAction {
	/**
	 * Copy the selection.
	 */
	CopyAction,
	/**
	 * Move the selection (deleting originals).
	 */
	MoveAction;

	public int getValue() {
		return ordinal();
	}
}
