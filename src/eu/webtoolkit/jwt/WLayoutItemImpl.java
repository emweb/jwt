package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * An abstract base class for implementing layout managers
 * 
 * @see WLayoutItem
 * @see WLayout
 */
public interface WLayoutItemImpl {
	/**
	 * Add a layout <i>item</i>.
	 * 
	 * The <i>item</i> already has an implementation set.
	 */
	public void updateAddItem(WLayoutItem item);

	/**
	 * Remove a layout <i>item</i>.
	 */
	public void updateRemoveItem(WLayoutItem item);

	/**
	 * Update the layout.
	 */
	public void update(WLayoutItem item);

	/**
	 * Get the widget for which this layout item participates in layout
	 * management.
	 */
	public WWidget getParent();

	/**
	 * Provide a hint that can aid in layout strategy / algorithm.
	 */
	public void setHint(String name, String value);
}
