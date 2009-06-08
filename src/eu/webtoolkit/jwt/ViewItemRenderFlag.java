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
 * Enumeration that specifies an option for rendering a view item.
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
	RenderEditing;

	public int getValue() {
		return ordinal();
	}
}
