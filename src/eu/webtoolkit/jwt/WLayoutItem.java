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
 * An abstract base class for items that can participate in a layout.
 * <p>
 * 
 * @see WLayout
 */
public interface WLayoutItem {
	/**
	 * Finds the widget item corresponding to the given <i>widget</i>.
	 * <p>
	 * The widget is searched for recursively inside nested layouts.
	 */
	public WWidgetItem findWidgetItem(WWidget widget);

	/**
	 * Returns the layout that implements this {@link WLayoutItem}.
	 * <p>
	 * This implements a type-safe upcasting mechanism to a {@link WLayout}.
	 */
	public WLayout getLayout();

	/**
	 * Returns the widget that is held by this {@link WLayoutItem}.
	 * <p>
	 * This implements a type-safe upcasting mechanism to a {@link WWidgetItem}.
	 */
	public WWidget getWidget();

	/**
	 * Returns the layout in which this item is contained.
	 */
	public WLayout getParentLayout();

	/**
	 * Returns the implementation for this layout item.
	 * <p>
	 * The implementation of a layout item depends on the kind of container for
	 * which the layout does layout management.
	 */
	public WLayoutItemImpl getImpl();

	/**
	 * Internal method.
	 */
	public void setParentWidget(WWidget parent);

	/**
	 * Internal method.
	 */
	public void setParentLayout(WLayout parentLayout);
}
