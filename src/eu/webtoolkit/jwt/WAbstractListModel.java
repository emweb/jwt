/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * An abstract list model for use with Wt&apos;s view classes.
 * <p>
 * 
 * An abstract list model specializes {@link WAbstractItemModel} for
 * one-dimensional lists (i.e. a model with 1 column and no children).
 * <p>
 * It cannot be used directly but must be subclassed. Subclassed models must at
 * least reimplement {@link WAbstractItemModel#getRowCount(WModelIndex parent)
 * getRowCount() } to return the number of rows, and
 * {@link WAbstractItemModel#getData(WModelIndex index, int role) getData() } to
 * return data.
 */
public abstract class WAbstractListModel extends WAbstractItemModel {
	/**
	 * Create a new abstract list model.
	 */
	public WAbstractListModel(WObject parent) {
		super(parent);
	}

	/**
	 * Create a new abstract list model.
	 * <p>
	 * Calls {@link #WAbstractListModel(WObject parent) this((WObject)null)}
	 */
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
