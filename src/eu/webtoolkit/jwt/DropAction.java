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
 * Enumeration that indicates a drop action.
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
