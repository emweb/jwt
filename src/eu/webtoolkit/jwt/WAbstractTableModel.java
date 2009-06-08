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
 * An abstract table model for use with Wt&apos;s view classes.
 * 
 * 
 * An abstract table model specializes {@link WAbstractItemModel} for
 * two-dimensional tables (but no hierarchical models).
 * <p>
 * It cannot be used directly but must be subclassed. Subclassed models must at
 * least reimplement
 * {@link WAbstractItemModel#getColumnCount(WModelIndex parent)},
 * {@link WAbstractItemModel#getRowCount(WModelIndex parent)} and
 * {@link WAbstractItemModel#getData(WModelIndex index, int role)}.
 */
public abstract class WAbstractTableModel extends WAbstractItemModel {
	/**
	 * Create a new abstract list model.
	 */
	public WAbstractTableModel(WObject parent) {
		super(parent);
	}

	public WAbstractTableModel() {
		this((WObject) null);
	}

	/**
	 * Destructor.
	 */
	public void destroy() {
	}

	public WModelIndex getParent(WModelIndex index) {
		return null;
	}

	public WModelIndex getIndex(int row, int column, WModelIndex parent) {
		return this.createIndex(row, column, null);
	}
}
