/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An index to a data item in a data model.
 * <p>
 * Indexes are used to indicate a particular item in a {@link WAbstractItemModel}. An index points to the item by
 * identifying its row and column location within a parent model index. Indexes are immutable.
 * <p>
 * The <code>null</code> index identifies the parent of top level indexes. Thus, a model that specifies only a list or
 * table of data (but no hierarchical data) would have as indexes only indexes that specify the <code>null</code> as
 * parent.
 * <p>
 * Valid indexes are created by the model, within the protected {@link WAbstractItemModel#createIndex(int, int, Object)}
 * method. In this way, models can define an internal pointer or suitable for identifying items in the model.
 * 
 * @see WAbstractItemModel
 */
public class WModelIndex implements Comparable<WModelIndex> {

	private int row;
	private int column;
	private WAbstractItemModel model;
	private Object internalPointer;

	/**
	 * Constructor.
	 * 
	 * @param row
	 * @param column
	 * @param model
	 * @param ptr
	 */
	WModelIndex(int row, int column, WAbstractItemModel model, Object ptr) {
		this.row = row;
		this.column = column;
		this.model = model;
		this.internalPointer = ptr;
	}

	/**
	 * Returns the parent index.
	 * 
	 * @return the parent index, which may be <code>null</code> if the index is a top-level index.
	 */
	public WModelIndex getParent() {
		return model.getParent(this);
	}

	/**
	 * Returns the row.
	 * 
	 * @return the row.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Returns the column.
	 * 
	 * @return the column.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Returns the internal pointer.
	 * <p>
	 * The internal pointer is used by a specific model implementation to uniquely identify (in conjunction with {@link #getRow()} and {@link #getColumn()})
	 * the item to which this index points.
	 * <p>
	 * Usually the internal pointer therefore identifies the parent for hierarchical models, or is simply <code>null</code> for tabular models.
	 * 
	 * @return the internal pointer.
	 */
	public Object getInternalPointer() {
		return internalPointer;
	}

	/**
	 * Returns the model.
	 * 
	 * @return the model that created this index.
	 */
	public WAbstractItemModel getModel() {
		return model;
	}

	/**
	 * Returns the data in the model associated at this index.
	 * 
	 * This is a convenience method for {@link WAbstractItemModel#getData(WModelIndex, int)}.
	 * 
	 * @param role the {@link ItemDataRole} role.
	 * 
	 * @return the data, or <code>null</code> if no data was available for this role.
	 */
	public Object getData(int role) {
		return model.getData(this, role);
	}

	/**
	 * Returns the data in the model associated at this index.
	 * 
	 * Calls {@link #getData(int) getData(ItemDataRole.DisplayRole)}.
	 */
	public Object getData() {
		return getData(ItemDataRole.DisplayRole);
	}

	/**
	 * Returns a model index for a child item.
	 * <p>
	 * This is a convenience method for {@link WAbstractItemModel#getIndex(int, int, WModelIndex)}.
	 */
	public WModelIndex getChild(int row, int column) {
		return model.getIndex(row, column, this);
	}
	
	/**
	 * Indicates whether some other object is "equal to" this one. 
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof WModelIndex))
			return false;

		WModelIndex i1 = this;
		WModelIndex i2 = (WModelIndex) other;

		return i1.getModel() == i2.getModel() && i1.getRow() == i2.getRow() && i1.getColumn() == i2.getColumn()
				&& i1.getInternalPointer() == i2.getInternalPointer();
	}

	/**
	 * Returns a hash code value for the object.
	 */
	@Override
	public int hashCode() {
		return model.hashCode() + (internalPointer == null ? 0 : internalPointer.hashCode()) + row * 1000 + column;
	}

	/**
	 * Implements {@link Comparable#compareTo(Object)}.
	 * <p>
	 * Creates a topological ordering of model indexes.
	 * <p>
	 * The topological order follows the order in which the indexes would be
	 * displayed in a tree table view, from top to bottom followed by
	 * left to right.
	 */
	public int compareTo(WModelIndex i2) {
		WModelIndex i1 = this;

		if (i1.equals(i2))
			return 0;
		else {
			ArrayList<WModelIndex> ancestors1 = i1.getAncestors();
			ArrayList<WModelIndex> ancestors2 = i2.getAncestors();

			int e = Math.min(ancestors1.size(), ancestors2.size());

			for (int i = 0; i < e; ++i) {
				WModelIndex a1 = ancestors1.get(i);
				WModelIndex a2 = ancestors2.get(i);

				if (!a1.equals(a2)) {
					if (a1.getRow() < a2.getRow())
						return -1;
					else if (a1.getRow() > a2.getRow())
						return 1;
					else if (a1.getColumn() < a2.getColumn())
						return -1;
					else
						return 1;
				}
			}

			return ancestors1.size() - ancestors2.size();
		}
	}

	private ArrayList<WModelIndex> getAncestors() {
		ArrayList<WModelIndex> result;

		WModelIndex parent = getParent();
		if (parent != null)
			result = parent.getAncestors();
		else
			result = new ArrayList<WModelIndex>();

		result.add(this);

		return result;
	}

	/**
	 * Returns the item flags for the item at this index.
	 * 
	 * This is a convenience method for {@link WAbstractItemModel#getFlags(WModelIndex)}.
	 */
	public EnumSet<ItemFlag> getFlags() {
		return model.getFlags(this);
	}

	/**
	 * Encodes to raw index (before a layout change).
	 * 
	 * Use this method to encode an index for which you want to recover an index after the layout change to the same
	 * item (which may still be in the model, but at a different location).
	 * 
	 * An index that has been encoded as a raw index cannot be used for anything but #decodeFromRawIndex() at a later
	 * point.
	 * 
	 * @see WAbstractItemModel#toRawIndex(WModelIndex)
	 * @see WAbstractItemModel#layoutAboutToBeChanged()
	 * @see #decodeFromRawIndex()
	 */
	public void encodeAsRawIndex() {
	    if (isRawIndex())
		throw new WtException("WModelIndex::encodeAsRawIndex(): cannot encode a raw index to raw again");

	    internalPointer = model.toRawIndex(this);
	    row = column = -42;
	}

	private boolean isRawIndex() {
		return row == -42 && column == -42;
	}

	/**
	 * Decodes a raw index (after a layout change).
	 * 
	 * A raw index can be decoded, within the context of a model that has been re-layed out.
	 * 
	 * This method returns a new index that points to the same item, or <code>null</code> if the underlying model did not
	 * support encoding to raw indexes, or, if the item to which the index previously pointed, is no longer part of the
	 * model.
	 * 
	 * @see WAbstractItemModel#fromRawIndex(Object)
	 * @see WAbstractItemModel#layoutChanged()
	 * @see #encodeAsRawIndex()
	 */
	public WModelIndex decodeFromRawIndex() {
	    	if (!isRawIndex())
		    	throw new WtException("WModelIndex::decodeFromRawIndex(): can only decode an encoded raw index");

	    	return model.fromRawIndex(getInternalPointer());
	}

	/**
	 * Utility method for converting an entire set of indexes to raw.
	 * 
	 * @param indexes
	 * 
	 * @see #encodeAsRawIndex()
	 */
	public static void encodeAsRawIndexes(SortedSet<WModelIndex> indexes) {
	    	for (WModelIndex i : indexes)
			i.encodeAsRawIndex();
	}
	
	/**
	 * Utility method to decode an entire set of raw indexes.
	 *
	 * @see #decodeFromRawIndex()
	 */
	public static SortedSet<WModelIndex> decodeFromRawIndexes(SortedSet<WModelIndex> encodedIndexes) {
		SortedSet<WModelIndex> result = new TreeSet<WModelIndex>();

		for (WModelIndex i : encodedIndexes) {
			WModelIndex n = i.decodeFromRawIndex();
			if (n != null)
				result.add(n);
		}
		
		return result;
	}
}
