/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
	 * 
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
	public void setHint(final String name, final String value);
}
