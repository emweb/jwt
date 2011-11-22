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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract proxy model for Wt&apos;s item models.
 * <p>
 * 
 * A proxy model does not store data, but presents data from a source model in
 * another way. It may provide filtering, sorting, or other computed changes to
 * the source model. A proxy model may be a fully functional model, that also
 * allows modification of the underlying model.
 * <p>
 * This abstract proxy model may be used as a starting point for implementing a
 * custom proxy model, when {@link WSortFilterProxyModel} is not adequate. It
 * implements data access and manipulation using the a virtual mapping method (
 * {@link WAbstractProxyModel#mapToSource(WModelIndex proxyIndex) mapToSource()}
 * ) to access and manipulate the underlying
 * {@link WAbstractProxyModel#getSourceModel() getSourceModel()}.
 */
public abstract class WAbstractProxyModel extends WAbstractItemModel {
	private static Logger logger = LoggerFactory
			.getLogger(WAbstractProxyModel.class);

	/**
	 * Constructor.
	 */
	public WAbstractProxyModel(WObject parent) {
		super(parent);
		this.sourceModel_ = null;
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WAbstractProxyModel(WObject parent) this((WObject)null)}
	 */
	public WAbstractProxyModel() {
		this((WObject) null);
	}

	/**
	 * Maps a source model index to the proxy model.
	 * <p>
	 * This method returns a model index in the proxy model that corresponds to
	 * the model index <code>sourceIndex</code> in the source model. This method
	 * must only be implemented for source model indexes that are mapped and
	 * thus are the result of
	 * {@link WAbstractProxyModel#mapToSource(WModelIndex proxyIndex)
	 * mapToSource()}.
	 * <p>
	 * 
	 * @see WAbstractProxyModel#mapToSource(WModelIndex proxyIndex)
	 */
	public abstract WModelIndex mapFromSource(WModelIndex sourceIndex);

	/**
	 * Maps a proxy model index to the source model.
	 * <p>
	 * This method returns a model index in the source model that corresponds to
	 * the proxy model index <code>proxyIndex</code>.
	 * <p>
	 * 
	 * @see WAbstractProxyModel#mapFromSource(WModelIndex sourceIndex)
	 */
	public abstract WModelIndex mapToSource(WModelIndex proxyIndex);

	/**
	 * Sets the source model.
	 * <p>
	 * The source model provides the actual data for the proxy model.
	 * <p>
	 * Ownership of the source model is <i>not</i> transferred.
	 */
	public void setSourceModel(WAbstractItemModel sourceModel) {
		this.sourceModel_ = sourceModel;
	}

	/**
	 * Returns the source model.
	 * <p>
	 * 
	 * @see WAbstractProxyModel#setSourceModel(WAbstractItemModel sourceModel)
	 */
	public WAbstractItemModel getSourceModel() {
		return this.sourceModel_;
	}

	public Object getData(WModelIndex index, int role) {
		return this.sourceModel_.getData(this.mapToSource(index), role);
	}

	public boolean setData(WModelIndex index, Object value, int role) {
		return this.sourceModel_.setData(this.mapToSource(index), value, role);
	}

	public EnumSet<ItemFlag> getFlags(WModelIndex index) {
		return this.sourceModel_.getFlags(this.mapToSource(index));
	}

	public boolean insertColumns(int column, int count, WModelIndex parent) {
		return this.sourceModel_.insertColumns(column, count, parent);
	}

	public boolean removeColumns(int column, int count, WModelIndex parent) {
		return this.sourceModel_.removeColumns(column, count, parent);
	}

	public String getMimeType() {
		return this.sourceModel_.getMimeType();
	}

	public List<String> getAcceptDropMimeTypes() {
		return this.sourceModel_.getAcceptDropMimeTypes();
	}

	public void dropEvent(WDropEvent e, DropAction action, int row, int column,
			WModelIndex parent) {
		WModelIndex sourceParent = this.mapToSource(parent);
		int sourceRow = row;
		int sourceColumn = column;
		if (sourceRow != -1) {
			sourceRow = this.mapToSource(this.getIndex(row, 0, parent))
					.getRow();
		}
		this.sourceModel_.dropEvent(e, action, sourceRow, sourceColumn,
				sourceParent);
	}

	public Object toRawIndex(WModelIndex index) {
		return this.sourceModel_.toRawIndex(this.mapToSource(index));
	}

	public WModelIndex fromRawIndex(Object rawIndex) {
		return this.mapFromSource(this.sourceModel_.fromRawIndex(rawIndex));
	}

	/**
	 * Create a source model index.
	 * <p>
	 * This is a utility function that allows you to create indexes in the
	 * source model. In this way, you can reuse the internal pointers of the
	 * source model in proxy model indexes, and convert a proxy model index back
	 * to the source model index using this method.
	 */
	protected WModelIndex createSourceIndex(int row, int column, Object ptr) {
		return this.sourceModel_.createIndex(row, column, ptr);
	}

	/**
	 * A base class for an item modeling a source index parent.
	 * <p>
	 * 
	 * Many mplementations of a proxy model will need to maintain a data
	 * structure per source model indexes, where they relate source rows or
	 * columns to proxy rows or columns, per hierarchical parent.
	 * <p>
	 * It may be convenient to start from this item class as a base class so
	 * that
	 * {@link WAbstractProxyModel#shiftModelIndexes(WModelIndex sourceParent, int start, int count, SortedMap items)
	 * WAbstractProxyModel#shiftModelIndexes()} can be used to update this data
	 * structure when the source model adds or removes rows.
	 * <p>
	 * You will typically use your derived class of this item as the internal
	 * pointer for proxy model indexes: a proxy model index will have an item as
	 * internal pointer whose sourceIndex_ corresponds to the source equivalent
	 * of the proxy model index parent.
	 * <p>
	 * 
	 * @see WAbstractItemModel#createIndex(int row, int column, Object ptr)
	 */
	protected static class BaseItem {
		private static Logger logger = LoggerFactory.getLogger(BaseItem.class);

		/**
		 * The source model index.
		 * <p>
		 * The source model index for this item.
		 */
		public WModelIndex sourceIndex_;

		/**
		 * Create a {@link BaseItem}.
		 */
		public BaseItem(WModelIndex sourceIndex) {
			this.sourceIndex_ = sourceIndex;
		}
	}

	/**
	 * Utility methods to shift items in an item map.
	 * <p>
	 * You can use this method to adjust an item map after the source model has
	 * inserted or removed rows. When removing rows (count &lt; 0), items may
	 * possibly be removed and deleted.
	 */
	protected void shiftModelIndexes(WModelIndex sourceParent, int start,
			int count,
			SortedMap<WModelIndex, WAbstractProxyModel.BaseItem> items) {
		List<WAbstractProxyModel.BaseItem> shifted = new ArrayList<WAbstractProxyModel.BaseItem>();
		List<WAbstractProxyModel.BaseItem> erased = new ArrayList<WAbstractProxyModel.BaseItem>();
		for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> it_it = items
				.tailMap(this.getSourceModel().getIndex(start, 0, sourceParent))
				.entrySet().iterator(); it_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> it = it_it
					.next();
			WModelIndex i = it.getKey();
			if ((i != null)) {
				WModelIndex p = i.getParent();
				if (!(p == sourceParent || (p != null && p.equals(sourceParent)))
						&& !WModelIndex.isAncestor(p, sourceParent)) {
					break;
				}
				if ((p == sourceParent || (p != null && p.equals(sourceParent)))) {
					shifted.add(it.getValue());
				} else {
					if (count < 0) {
						do {
							if ((p.getParent() == sourceParent || (p
									.getParent() != null && p.getParent()
									.equals(sourceParent)))
									&& p.getRow() >= start
									&& p.getRow() < start - count) {
								erased.add(it.getValue());
								break;
							} else {
								p = p.getParent();
							}
						} while (!(p == sourceParent || (p != null && p
								.equals(sourceParent))));
					}
				}
			}
		}
		for (int i = 0; i < erased.size(); ++i) {
			items.remove(erased.get(i).sourceIndex_);
			;
		}
		for (int i = 0; i < shifted.size(); ++i) {
			WAbstractProxyModel.BaseItem item = shifted.get(i);
			items.remove(item.sourceIndex_);
			if (item.sourceIndex_.getRow() + count >= start) {
				item.sourceIndex_ = this.getSourceModel().getIndex(
						item.sourceIndex_.getRow() + count,
						item.sourceIndex_.getColumn(), sourceParent);
			} else {
				;
				shifted.set(i, null);
			}
		}
		for (int i = 0; i < shifted.size(); ++i) {
			if (shifted.get(i) != null) {
				items.put(shifted.get(i).sourceIndex_, shifted.get(i));
			}
		}
	}

	private WAbstractItemModel sourceModel_;
}
