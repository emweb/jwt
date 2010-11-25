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

/**
 * An abstract table model for use with Wt&apos;s view classes.
 * <p>
 * 
 * An abstract table model specializes {@link WAbstractItemModel} for
 * two-dimensional tables (but no hierarchical models).
 * <p>
 * It cannot be used directly but must be subclassed. Subclassed models must at
 * least reimplement
 * {@link WAbstractItemModel#getColumnCount(WModelIndex parent)
 * WAbstractItemModel#getColumnCount()},
 * {@link WAbstractItemModel#getRowCount(WModelIndex parent)
 * WAbstractItemModel#getRowCount()} and
 * {@link WAbstractItemModel#getData(WModelIndex index, int role)
 * WAbstractItemModel#getData()}.
 */
public abstract class WAbstractTableModel extends WAbstractItemModel {
	/**
	 * Creates a new abstract list model.
	 */
	public WAbstractTableModel(WObject parent) {
		super(parent);
	}

	/**
	 * Creates a new abstract list model.
	 * <p>
	 * Calls {@link #WAbstractTableModel(WObject parent) this((WObject)null)}
	 */
	public WAbstractTableModel() {
		this((WObject) null);
	}

	public WModelIndex getParent(WModelIndex index) {
		return null;
	}

	public WModelIndex getIndex(int row, int column, WModelIndex parent) {
		return this.createIndex(row, column, null);
	}
}
