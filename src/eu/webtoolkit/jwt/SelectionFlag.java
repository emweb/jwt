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
 * Enumeration that indicates how to change a selection.
 * 
 * @see WTreeView#select(WModelIndex index, SelectionFlag option)
 */
public enum SelectionFlag {
	/**
	 * Add to selection.
	 */
	Select(1),
	/**
	 * Remove from selection.
	 */
	Deselect(2),
	/**
	 * Toggle in selection.
	 */
	ToggleSelect(3),
	/**
	 * Clear selection and add single item.
	 */
	ClearAndSelect(4);

	private int value;

	SelectionFlag(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
