/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
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

/**
 * Flags that indicate data item options.
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
	ItemIsXHTMLText,
	/**
	 * Item&apos;s value has been modified.
	 */
	ItemIsDirty;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
