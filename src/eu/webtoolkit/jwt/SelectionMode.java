/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates how items may be selected.
 * <p>
 * 
 * @see WTreeView#setSelectionMode(SelectionMode mode)
 */
public enum SelectionMode {
	/**
	 * No selections.
	 */
	NoSelection(0),
	/**
	 * Single selection only.
	 */
	SingleSelection(1),
	/**
	 * Multiple selection.
	 */
	ExtendedSelection(3);

	private int value;

	SelectionMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
