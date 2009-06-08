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
 * Flags that data item options.
 * 
 * @see WModelIndex#flags()
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
	 * Item has tree states. When set, {@link ItemDataRole#CheckStateRole} data
	 * is of type {@link CheckState#CheckState}
	 */
	ItemIsTristate,
	/**
	 * Item&apos;s textual is HTML.
	 */
	ItemIsXHTMLText;

	public int getValue() {
		return ordinal();
	}
}
