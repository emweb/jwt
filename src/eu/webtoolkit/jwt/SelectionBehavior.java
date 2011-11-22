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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration that indicates what is being selected.
 * <p>
 * 
 * @see WAbstractItemView#setSelectionBehavior(SelectionBehavior behavior)
 */
public enum SelectionBehavior {
	/**
	 * Select single items.
	 */
	SelectItems(0),
	/**
	 * Select only rows.
	 */
	SelectRows(1);

	private int value;

	SelectionBehavior(int value) {
		this.value = value;
	}

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return value;
	}
}
