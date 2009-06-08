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
 * An abstract list model for use with Wt&apos;s view classes.
 * 
 * 
 * An abstract list model specializes {@link WAbstractItemModel} for
 * one-dimensional lists (i.e. a model with 1 column and no children).
 * <p>
 * It cannot be used directly but must be subclassed. Subclassed models must at
 * least reimplement {@link WAbstractItemModel#getRowCount(WModelIndex parent)}
 * to return the number of rows, and
 * {@link WAbstractItemModel#getData(WModelIndex index, int role)} to return
 * data.
 */
public abstract class WAbstractListModel extends WAbstractItemModel {
	/**
	 * Create a new abstract list model.
	 */
	public WAbstractListModel(WObject parent) {
		super(parent);
	}

	public WAbstractListModel() {
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

	public int getColumnCount(WModelIndex parent) {
		return (parent != null) ? 0 : 1;
	}
}
