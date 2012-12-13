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
 * A proxy model for Wt&apos;s item models that provides batch editing.
 * <p>
 * 
 * This proxy model presents data from a source model, and caches any editing
 * operation without affecting the underlying source model, until
 * {@link WBatchEditProxyModel#commitAll() commitAll()} or
 * {@link WBatchEditProxyModel#revertAll() revertAll()} is called. In this way,
 * you can commit all the editing in batch to the underlying source model, only
 * when the user confirms the changes.
 * <p>
 * All editing operations are supported:
 * <ul>
 * <li>changing data (
 * {@link WBatchEditProxyModel#setData(WModelIndex index, Object value, int role)
 * setData()})</li>
 * <li>inserting and removing rows (
 * {@link WBatchEditProxyModel#insertRows(int row, int count, WModelIndex parent)
 * insertRows()} and
 * {@link WBatchEditProxyModel#removeRows(int row, int count, WModelIndex parent)
 * removeRows()})</li>
 * <li>inserting and removing columns (
 * {@link WBatchEditProxyModel#insertColumns(int column, int count, WModelIndex parent)
 * insertColumns()} and
 * {@link WBatchEditProxyModel#removeColumns(int column, int count, WModelIndex parent)
 * removeColumns()})</li>
 * </ul>
 * <p>
 * The model supports both simple tabular models, as well as hierarchical
 * (tree-like / treetable-like) models, with children under items in the first
 * column.
 * <p>
 * Default values for a newly inserted row can be set using
 * {@link WBatchEditProxyModel#setNewRowData(int column, Object data, int role)
 * setNewRowData()} and flags for its items using
 * {@link WBatchEditProxyModel#setNewRowFlags(int column, EnumSet flags)
 * setNewRowFlags()}.
 */
public class WBatchEditProxyModel extends WAbstractProxyModel {
	private static Logger logger = LoggerFactory
			.getLogger(WBatchEditProxyModel.class);

	/**
	 * Constructor.
	 */
	public WBatchEditProxyModel(WObject parent) {
		super(parent);
		this.submitting_ = false;
		this.newRowData_ = new HashMap<Integer, SortedMap<Integer, Object>>();
		this.newRowFlags_ = new HashMap<Integer, EnumSet<ItemFlag>>();
		this.dirtyIndicationRole_ = -1;
		this.dirtyIndicationData_ = new Object();
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.mappedIndexes_ = new TreeMap<WModelIndex, WAbstractProxyModel.BaseItem>();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WBatchEditProxyModel(WObject parent) this((WObject)null)}
	 */
	public WBatchEditProxyModel() {
		this((WObject) null);
	}

	/**
	 * Returns whether changes have not yet been committed.
	 * <p>
	 * Returns whether have been made to the proxy model, which could be
	 * committed using {@link WBatchEditProxyModel#commitAll() commitAll()} or
	 * reverted using {@link WBatchEditProxyModel#revertAll() revertAll()}.
	 */
	public boolean isDirty() {
		for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it = this.mappedIndexes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it
					.next();
			WBatchEditProxyModel.Item item = ((i.getValue()) instanceof WBatchEditProxyModel.Item ? (WBatchEditProxyModel.Item) (i
					.getValue())
					: null);
			if (!item.removedColumns_.isEmpty()
					|| !item.insertedColumns_.isEmpty()
					|| !item.removedRows_.isEmpty()
					|| !item.insertedRows_.isEmpty()
					|| !item.editedValues_.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Commits changes.
	 * <p>
	 * This commits all changes to the source model.
	 * <p>
	 * 
	 * @see WBatchEditProxyModel#revertAll()
	 */
	public void commitAll() {
		this.submitting_ = true;
		for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it = this.mappedIndexes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it
					.next();
			WBatchEditProxyModel.Item item = ((i.getValue()) instanceof WBatchEditProxyModel.Item ? (WBatchEditProxyModel.Item) (i
					.getValue())
					: null);
			while (!item.removedColumns_.isEmpty()) {
				this.getSourceModel().removeColumn(item.removedColumns_.get(0),
						item.sourceIndex_);
			}
			while (!item.insertedColumns_.isEmpty()) {
				this.getSourceModel().insertColumn(
						item.insertedColumns_.get(0), item.sourceIndex_);
			}
			while (!item.removedRows_.isEmpty()) {
				this.getSourceModel().removeRow(item.removedRows_.get(0),
						item.sourceIndex_);
			}
			while (!item.insertedRows_.isEmpty()) {
				this.getSourceModel().insertRow(item.insertedRows_.get(0),
						item.sourceIndex_);
			}
			for (Iterator<Map.Entry<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>>> j_it = item.editedValues_
					.entrySet().iterator(); j_it.hasNext();) {
				Map.Entry<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>> j = j_it
						.next();
				WModelIndex index = this.getSourceModel().getIndex(
						j.getKey().row, j.getKey().column, item.sourceIndex_);
				SortedMap<Integer, Object> data = j.getValue();
				j_it.remove();
				this.getSourceModel().setItemData(index, data);
			}
		}
		this.submitting_ = false;
	}

	/**
	 * Reverts changes.
	 * <p>
	 * This reverts all changes.
	 * <p>
	 * 
	 * @see WBatchEditProxyModel#commitAll()
	 */
	public void revertAll() {
		for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it = this.mappedIndexes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it
					.next();
			WBatchEditProxyModel.Item item = ((i.getValue()) instanceof WBatchEditProxyModel.Item ? (WBatchEditProxyModel.Item) (i
					.getValue())
					: null);
			WModelIndex proxyIndex = this.mapFromSource(item.sourceIndex_);
			while (!item.insertedColumns_.isEmpty()) {
				this.removeColumn(item.insertedColumns_.get(0), proxyIndex);
			}
			while (!item.removedColumns_.isEmpty()) {
				int column = item.removedColumns_.get(0);
				this.beginInsertColumns(proxyIndex, column, 1);
				item.removedColumns_.remove(0);
				this.shiftColumns(item, column, 1);
				this.endInsertColumns();
			}
			while (!item.insertedRows_.isEmpty()) {
				this.removeRow(item.insertedRows_.get(0), proxyIndex);
			}
			while (!item.removedRows_.isEmpty()) {
				int row = item.removedRows_.get(0);
				this.beginInsertRows(proxyIndex, row, 1);
				item.removedRows_.remove(0);
				this.shiftRows(item, row, 1);
				this.endInsertRows();
			}
			for (Iterator<Map.Entry<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>>> j_it = item.editedValues_
					.entrySet().iterator(); j_it.hasNext();) {
				Map.Entry<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>> j = j_it
						.next();
				WBatchEditProxyModel.Cell c = j.getKey();
				j_it.remove();
				WModelIndex child = this.getIndex(c.row, c.column, proxyIndex);
				this.dataChanged().trigger(child, child);
			}
		}
	}

	/**
	 * Sets default data for a newly inserted row.
	 * <p>
	 * You can use this method to initialize data for a newly inserted row.
	 */
	public void setNewRowData(int column, Object data, int role) {
		this.newRowData_.get(column).put(role, data);
	}

	/**
	 * Sets default data for a newly inserted row.
	 * <p>
	 * Calls {@link #setNewRowData(int column, Object data, int role)
	 * setNewRowData(column, data, ItemDataRole.DisplayRole)}
	 */
	public final void setNewRowData(int column, Object data) {
		setNewRowData(column, data, ItemDataRole.DisplayRole);
	}

	/**
	 * Sets the item flags for items in a newly inserted row.
	 * <p>
	 * By default, {@link WBatchEditProxyModel#getFlags(WModelIndex index)
	 * getFlags()} will return ItemIsSelectable.
	 */
	public void setNewRowFlags(int column, EnumSet<ItemFlag> flags) {
		this.newRowFlags_.put(column, flags);
	}

	/**
	 * Sets the item flags for items in a newly inserted row.
	 * <p>
	 * Calls {@link #setNewRowFlags(int column, EnumSet flags)
	 * setNewRowFlags(column, EnumSet.of(flag, flags))}
	 */
	public final void setNewRowFlags(int column, ItemFlag flag,
			ItemFlag... flags) {
		setNewRowFlags(column, EnumSet.of(flag, flags));
	}

	/**
	 * Configures data used to indicate a modified item.
	 * <p>
	 * This sets <code>data</code> for item data role <code>role</code> to be
	 * returned by
	 * {@link WBatchEditProxyModel#getData(WModelIndex index, int role)
	 * getData()} for an item that is dirty (e.g. because it belongs to a newly
	 * inserted row/column, or because new data has been set for it.
	 * <p>
	 * When <code>role</code> is {@link ItemDataRole#StyleClassRole}, the style
	 * class is appended to any style already returned by the source model or
	 * set by
	 * {@link WBatchEditProxyModel#setNewRowData(int column, Object data, int role)
	 * setNewRowData()}.
	 * <p>
	 * By default there is no dirty indication.
	 */
	public void setDirtyIndication(int role, Object data) {
		this.dirtyIndicationRole_ = role;
		this.dirtyIndicationData_ = data;
	}

	public WModelIndex mapFromSource(WModelIndex sourceIndex) {
		if ((sourceIndex != null)) {
			if (this.isRemoved(sourceIndex.getParent())) {
				return null;
			}
			WModelIndex sourceParent = sourceIndex.getParent();
			WBatchEditProxyModel.Item parentItem = this
					.itemFromSourceIndex(sourceParent);
			int row = this.adjustedProxyRow(parentItem, sourceIndex.getRow());
			int column = this.adjustedProxyColumn(parentItem, sourceIndex
					.getColumn());
			if (row >= 0 && column >= 0) {
				return this.createIndex(row, column, parentItem);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public WModelIndex mapToSource(WModelIndex proxyIndex) {
		if ((proxyIndex != null)) {
			WBatchEditProxyModel.Item parentItem = this
					.parentItemFromIndex(proxyIndex);
			int row = this.adjustedSourceRow(parentItem, proxyIndex.getRow());
			int column = this.adjustedSourceColumn(parentItem, proxyIndex
					.getColumn());
			if (row >= 0 && column >= 0) {
				return this.getSourceModel().getIndex(row, column,
						parentItem.sourceIndex_);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public void setSourceModel(WAbstractItemModel model) {
		if (this.getSourceModel() != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		super.setSourceModel(model);
		this.modelConnections_.add(this.getSourceModel()
				.columnsAboutToBeInserted().addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this
										.sourceColumnsAboutToBeInserted(e1, e2,
												e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().columnsInserted()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this
										.sourceColumnsInserted(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel()
				.columnsAboutToBeRemoved().addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this
										.sourceColumnsAboutToBeRemoved(e1, e2,
												e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().columnsRemoved()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this.sourceColumnsRemoved(
										e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel()
				.rowsAboutToBeInserted().addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this
										.sourceRowsAboutToBeInserted(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().rowsInserted()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this.sourceRowsInserted(
										e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().rowsAboutToBeRemoved()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this
										.sourceRowsAboutToBeRemoved(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().rowsRemoved()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this.sourceRowsRemoved(e1,
										e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().dataChanged()
				.addListener(this,
						new Signal2.Listener<WModelIndex, WModelIndex>() {
							public void trigger(WModelIndex e1, WModelIndex e2) {
								WBatchEditProxyModel.this.sourceDataChanged(e1,
										e2);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().headerDataChanged()
				.addListener(this,
						new Signal3.Listener<Orientation, Integer, Integer>() {
							public void trigger(Orientation e1, Integer e2,
									Integer e3) {
								WBatchEditProxyModel.this
										.sourceHeaderDataChanged(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel()
				.layoutAboutToBeChanged().addListener(this,
						new Signal.Listener() {
							public void trigger() {
								WBatchEditProxyModel.this
										.sourceLayoutAboutToBeChanged();
							}
						}));
		this.modelConnections_.add(this.getSourceModel().layoutChanged()
				.addListener(this, new Signal.Listener() {
					public void trigger() {
						WBatchEditProxyModel.this.sourceLayoutChanged();
					}
				}));
		this.resetMappings();
	}

	public int getColumnCount(WModelIndex parent) {
		WBatchEditProxyModel.Item item = this.itemFromIndex(parent, false);
		if (item != null) {
			if (item.insertedParent_ != null) {
				return item.insertedColumns_.size();
			} else {
				return this.getSourceModel().getColumnCount(item.sourceIndex_)
						+ item.insertedColumns_.size()
						- item.removedColumns_.size();
			}
		} else {
			return this.getSourceModel().getColumnCount(
					this.mapToSource(parent));
		}
	}

	public int getRowCount(WModelIndex parent) {
		WBatchEditProxyModel.Item item = this.itemFromIndex(parent, false);
		if (item != null) {
			if (item.insertedParent_ != null) {
				return item.insertedRows_.size();
			} else {
				return this.getSourceModel().getRowCount(item.sourceIndex_)
						+ item.insertedRows_.size() - item.removedRows_.size();
			}
		} else {
			return this.getSourceModel().getRowCount(this.mapToSource(parent));
		}
	}

	public WModelIndex getParent(WModelIndex index) {
		if ((index != null)) {
			WBatchEditProxyModel.Item parentItem = this
					.parentItemFromIndex(index);
			return this.mapFromSource(parentItem.sourceIndex_);
		} else {
			return null;
		}
	}

	public WModelIndex getIndex(int row, int column, WModelIndex parent) {
		WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
		return this.createIndex(row, column, item);
	}

	public Object getData(WModelIndex index, int role) {
		WBatchEditProxyModel.Item item = this.itemFromIndex(index.getParent());
		SortedMap<Integer, Object> i = item.editedValues_
				.get(new WBatchEditProxyModel.Cell(index.getRow(), index
						.getColumn()));
		if (i != null) {
			Object j = i.get(role);
			if (j != null) {
				return this.indicateDirty(role, j);
			} else {
				return this.indicateDirty(role, null);
			}
		}
		WModelIndex sourceIndex = this.mapToSource(index);
		if ((sourceIndex != null)) {
			return this.getSourceModel().getData(sourceIndex, role);
		} else {
			return this.indicateDirty(role, null);
		}
	}

	/**
	 * Sets item data.
	 * <p>
	 * The default implementation will copy {@link ItemDataRole#EditRole} data
	 * to {@link ItemDataRole#DisplayRole}. You may want to specialize the model
	 * to provide a more specialized editing behaviour.
	 */
	public boolean setData(WModelIndex index, Object value, int role) {
		WBatchEditProxyModel.Item item = this.itemFromIndex(index.getParent());
		SortedMap<Integer, Object> i = item.editedValues_
				.get(new WBatchEditProxyModel.Cell(index.getRow(), index
						.getColumn()));
		if (i == null) {
			WModelIndex sourceIndex = this.mapToSource(index);
			SortedMap<Integer, Object> dataMap = new TreeMap<Integer, Object>();
			if ((sourceIndex != null)) {
				dataMap = this.getSourceModel().getItemData(sourceIndex);
			}
			dataMap.put(role, value);
			if (role == ItemDataRole.EditRole) {
				dataMap.put(ItemDataRole.DisplayRole, value);
			}
			item.editedValues_.put(new WBatchEditProxyModel.Cell(
					index.getRow(), index.getColumn()), dataMap);
		} else {
			i.put(role, value);
			if (role == ItemDataRole.EditRole) {
				i.put(ItemDataRole.DisplayRole, value);
			}
		}
		this.dataChanged().trigger(index, index);
		return true;
	}

	public EnumSet<ItemFlag> getFlags(WModelIndex index) {
		WModelIndex sourceIndex = this.mapToSource(index);
		if ((sourceIndex != null)) {
			return this.getSourceModel().getFlags(index);
		} else {
			EnumSet<ItemFlag> i = this.newRowFlags_.get(index.getColumn());
			if (i != null) {
				return i;
			} else {
				return super.getFlags(index);
			}
		}
	}

	public Object getHeaderData(int section, Orientation orientation, int role) {
		if (orientation == Orientation.Vertical) {
			return null;
		} else {
			return this.getSourceModel().getHeaderData(section, orientation,
					role);
		}
	}

	public boolean insertRows(int row, int count, WModelIndex parent) {
		if (this.getColumnCount(parent) == 0) {
			this.insertColumns(0, 1, parent);
		}
		this.beginInsertRows(parent, row, row + count - 1);
		WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
		this.shiftRows(item, row, count);
		this.insertIndexes(item, item.insertedRows_, item.insertedItems_, row,
				count);
		for (int i = 0; i < count; ++i) {
			for (int j = 0; j < this.getColumnCount(parent); ++j) {
				SortedMap<Integer, Object> data = new TreeMap<Integer, Object>();
				SortedMap<Integer, Object> nri = this.newRowData_.get(j);
				if (nri != null) {
					data = nri;
				}
				item.editedValues_.put(
						new WBatchEditProxyModel.Cell(row + i, j), data);
			}
		}
		this.endInsertRows();
		return true;
	}

	public boolean removeRows(int row, int count, WModelIndex parent) {
		this.beginRemoveRows(parent, row, row + count - 1);
		WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
		this.removeIndexes(item, item.insertedRows_, item.removedRows_,
				item.insertedItems_, row, count);
		this.shiftRows(item.editedValues_, row, count);
		this.endRemoveRows();
		return true;
	}

	public boolean insertColumns(int column, int count, WModelIndex parent) {
		this.beginInsertColumns(parent, column, column + count - 1);
		WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
		this.shiftColumns(item, column, count);
		this.insertIndexes(item, item.insertedColumns_,
				(List<WBatchEditProxyModel.Item>) null, column, count);
		this.endInsertColumns();
		return true;
	}

	public boolean removeColumns(int column, int count, WModelIndex parent) {
		this.beginRemoveColumns(parent, column, column + count - 1);
		WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
		this.removeIndexes(item, item.insertedColumns_, item.removedColumns_,
				(List<WBatchEditProxyModel.Item>) null, column, count);
		this.shiftColumns(item.editedValues_, column, count);
		this.endRemoveColumns();
		return true;
	}

	public void sort(int column, SortOrder order) {
		this.getSourceModel().sort(column, order);
	}

	static class Cell {
		private static Logger logger = LoggerFactory.getLogger(Cell.class);

		public int row;
		public int column;

		public Cell(int r, int c) {
			this.row = r;
			this.column = c;
		}
	}

	static class Item extends WAbstractProxyModel.BaseItem {
		private static Logger logger = LoggerFactory.getLogger(Item.class);

		public WBatchEditProxyModel.Item insertedParent_;
		public Map<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>> editedValues_;
		public List<Integer> removedRows_;
		public List<Integer> insertedRows_;
		public List<WBatchEditProxyModel.Item> insertedItems_;
		public List<Integer> removedColumns_;
		public List<Integer> insertedColumns_;

		public Item(WModelIndex sourceIndex) {
			super(sourceIndex);
			this.insertedParent_ = null;
			this.editedValues_ = new HashMap<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>>();
			this.removedRows_ = new ArrayList<Integer>();
			this.insertedRows_ = new ArrayList<Integer>();
			this.insertedItems_ = new ArrayList<WBatchEditProxyModel.Item>();
			this.removedColumns_ = new ArrayList<Integer>();
			this.insertedColumns_ = new ArrayList<Integer>();
		}

		public Item(WBatchEditProxyModel.Item insertedParent) {
			super(null);
			this.insertedParent_ = insertedParent;
			this.editedValues_ = new HashMap<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>>();
			this.removedRows_ = new ArrayList<Integer>();
			this.insertedRows_ = new ArrayList<Integer>();
			this.insertedItems_ = new ArrayList<WBatchEditProxyModel.Item>();
			this.removedColumns_ = new ArrayList<Integer>();
			this.insertedColumns_ = new ArrayList<Integer>();
		}
	}

	private boolean submitting_;
	private Map<Integer, SortedMap<Integer, Object>> newRowData_;
	private Map<Integer, EnumSet<ItemFlag>> newRowFlags_;
	private int dirtyIndicationRole_;
	private Object dirtyIndicationData_;
	private List<AbstractSignal.Connection> modelConnections_;
	private SortedMap<WModelIndex, WAbstractProxyModel.BaseItem> mappedIndexes_;

	private void sourceColumnsAboutToBeInserted(WModelIndex parent, int start,
			int end) {
		if (this.isRemoved(parent)) {
			return;
		}
		this.beginInsertColumns(this.mapFromSource(parent), start, end);
	}

	private void sourceColumnsInserted(WModelIndex parent, int start, int end) {
		if (this.isRemoved(parent)) {
			return;
		}
		WModelIndex pparent = this.mapFromSource(parent);
		WBatchEditProxyModel.Item item = this.itemFromIndex(pparent);
		int count = end - start + 1;
		for (int i = 0; i < count; ++i) {
			int proxyColumn = this.adjustedProxyColumn(item, start + i);
			if (proxyColumn >= 0) {
				if (!this.submitting_) {
					this.beginInsertColumns(pparent, proxyColumn, proxyColumn);
					this.shiftColumns(item, proxyColumn, 1);
					this.endInsertColumns();
				} else {
					int index = item.insertedColumns_.indexOf(proxyColumn);
					assert index != -1;
					item.insertedColumns_.remove(0 + index);
				}
			} else {
				assert !this.submitting_;
				int remi = -proxyColumn - 1;
				proxyColumn = item.removedColumns_.get(remi);
				this.beginInsertColumns(pparent, proxyColumn, proxyColumn);
				this.shiftColumns(item, proxyColumn, 1);
				this.endInsertColumns();
			}
		}
	}

	private void sourceColumnsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		if (this.isRemoved(parent)) {
			return;
		}
		WModelIndex pparent = this.mapFromSource(parent);
		WBatchEditProxyModel.Item item = this.itemFromIndex(pparent);
		int count = end - start + 1;
		for (int i = 0; i < count; ++i) {
			int proxyColumn = this.adjustedProxyColumn(item, start);
			if (proxyColumn >= 0) {
				this.beginRemoveColumns(pparent, proxyColumn, proxyColumn);
				this.shiftColumns(item, proxyColumn, -1);
				this.endRemoveColumns();
			} else {
				int remi = -proxyColumn - 1;
				item.removedColumns_.remove(0 + remi);
			}
		}
	}

	private void sourceColumnsRemoved(WModelIndex parent, int start, int end) {
		if (this.isRemoved(parent)) {
			return;
		}
		this.endRemoveColumns();
	}

	private void sourceRowsAboutToBeInserted(WModelIndex parent, int start,
			int end) {
	}

	private void sourceRowsInserted(WModelIndex parent, int start, int end) {
		if (this.isRemoved(parent)) {
			return;
		}
		WModelIndex pparent = this.mapFromSource(parent);
		WBatchEditProxyModel.Item item = this.itemFromIndex(pparent);
		int count = end - start + 1;
		for (int i = 0; i < count; ++i) {
			int proxyRow = this.adjustedProxyRow(item, start + i);
			if (proxyRow >= 0) {
				if (!this.submitting_) {
					this.beginInsertRows(pparent, proxyRow, proxyRow);
					this.shiftRows(item, proxyRow, 1);
					this.endInsertRows();
				} else {
					int index = item.insertedRows_.indexOf(proxyRow);
					assert index != -1;
					WBatchEditProxyModel.Item child = item.insertedItems_
							.get(index);
					if (child != null) {
						child.sourceIndex_ = this.getSourceModel().getIndex(
								start + i, 0, parent);
						child.insertedParent_ = null;
						this.mappedIndexes_.put(child.sourceIndex_, child);
					}
					item.insertedItems_.remove(0 + index);
					item.insertedRows_.remove(0 + index);
				}
			} else {
				assert !this.submitting_;
				int remi = -proxyRow - 1;
				proxyRow = item.removedRows_.get(remi);
				this.beginInsertRows(pparent, proxyRow, proxyRow);
				this.shiftRows(item, proxyRow, 1);
				this.endInsertRows();
			}
		}
		this.shiftModelIndexes(parent, start, end - start + 1,
				this.mappedIndexes_);
	}

	private void sourceRowsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		if (this.isRemoved(parent)) {
			return;
		}
		WModelIndex pparent = this.mapFromSource(parent);
		WBatchEditProxyModel.Item item = this.itemFromIndex(pparent);
		int count = end - start + 1;
		for (int i = 0; i < count; ++i) {
			int proxyRow = this.adjustedProxyRow(item, start);
			if (proxyRow >= 0) {
				this.beginRemoveRows(pparent, proxyRow, proxyRow);
				this.deleteItemsUnder(item, proxyRow);
				this.shiftRows(item, proxyRow, -1);
				this.endRemoveRows();
			} else {
				int remi = -proxyRow - 1;
				item.removedRows_.remove(0 + remi);
			}
		}
	}

	private void sourceRowsRemoved(WModelIndex parent, int start, int end) {
		if (this.isRemoved(parent)) {
			return;
		}
		this.shiftModelIndexes(parent, start, -(end - start + 1),
				this.mappedIndexes_);
	}

	private void sourceDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
		if (this.isRemoved(topLeft.getParent())) {
			return;
		}
		for (int row = topLeft.getRow(); row <= bottomRight.getRow(); ++row) {
			for (int col = topLeft.getColumn(); col <= bottomRight.getColumn(); ++col) {
				WModelIndex l = this.getSourceModel().getIndex(row, col,
						topLeft.getParent());
				if (!this.isRemoved(l)) {
					this.dataChanged().trigger(this.mapFromSource(l),
							this.mapFromSource(l));
				}
			}
		}
	}

	private void sourceHeaderDataChanged(Orientation orientation, int start,
			int end) {
		if (orientation == Orientation.Vertical) {
			WBatchEditProxyModel.Item item = this.itemFromIndex(null);
			for (int row = start; row <= end; ++row) {
				int proxyRow = this.adjustedProxyRow(item, row);
				if (proxyRow != -1) {
					this.headerDataChanged().trigger(orientation, proxyRow,
							proxyRow);
				}
			}
		} else {
			this.headerDataChanged().trigger(orientation, start, end);
		}
	}

	private void sourceLayoutAboutToBeChanged() {
		this.layoutAboutToBeChanged().trigger();
		this.resetMappings();
	}

	private void sourceLayoutChanged() {
		this.layoutChanged().trigger();
	}

	private WBatchEditProxyModel.Item itemFromSourceIndex(
			WModelIndex sourceParent, boolean autoCreate) {
		if (this.isRemoved(sourceParent)) {
			return null;
		}
		WAbstractProxyModel.BaseItem i = this.mappedIndexes_.get(sourceParent);
		if (i == null) {
			if (autoCreate) {
				WBatchEditProxyModel.Item result = new WBatchEditProxyModel.Item(
						sourceParent);
				this.mappedIndexes_.put(sourceParent, result);
				return result;
			} else {
				return null;
			}
		} else {
			return ((i) instanceof WBatchEditProxyModel.Item ? (WBatchEditProxyModel.Item) (i)
					: null);
		}
	}

	private final WBatchEditProxyModel.Item itemFromSourceIndex(
			WModelIndex sourceParent) {
		return itemFromSourceIndex(sourceParent, true);
	}

	private WBatchEditProxyModel.Item itemFromInsertedRow(
			WBatchEditProxyModel.Item parentItem, WModelIndex index,
			boolean autoCreate) {
		int i = parentItem.insertedRows_.indexOf(index.getRow());
		if (!(parentItem.insertedItems_.get(i) != null) && autoCreate) {
			WBatchEditProxyModel.Item item = new WBatchEditProxyModel.Item(
					parentItem);
			parentItem.insertedItems_.set(i, item);
		}
		return parentItem.insertedItems_.get(i);
	}

	private final WBatchEditProxyModel.Item itemFromInsertedRow(
			WBatchEditProxyModel.Item parentItem, WModelIndex index) {
		return itemFromInsertedRow(parentItem, index, true);
	}

	private WBatchEditProxyModel.Item parentItemFromIndex(WModelIndex index) {
		return (WBatchEditProxyModel.Item) index.getInternalPointer();
	}

	private WBatchEditProxyModel.Item itemFromIndex(WModelIndex index,
			boolean autoCreate) {
		if ((index != null)) {
			WBatchEditProxyModel.Item parentItem = this
					.parentItemFromIndex(index);
			int row = this.adjustedSourceRow(parentItem, index.getRow());
			int column = this.adjustedSourceColumn(parentItem, index
					.getColumn());
			if (row >= 0 && column >= 0) {
				WModelIndex sourceIndex = this.getSourceModel().getIndex(row,
						column, parentItem.sourceIndex_);
				return this.itemFromSourceIndex(sourceIndex, autoCreate);
			} else {
				if (index.getColumn() == 0) {
					return this.itemFromInsertedRow(parentItem, index,
							autoCreate);
				} else {
					if (autoCreate) {
						throw new WException(
								"WBatchEditProxyModel does not support children in column > 0");
					} else {
						return null;
					}
				}
			}
		} else {
			return this.itemFromSourceIndex(null, autoCreate);
		}
	}

	private final WBatchEditProxyModel.Item itemFromIndex(WModelIndex index) {
		return itemFromIndex(index, true);
	}

	private boolean isRemoved(WModelIndex sourceIndex) {
		if (!(sourceIndex != null)) {
			return false;
		}
		WModelIndex sourceParent = sourceIndex.getParent();
		if (this.isRemoved(sourceParent)) {
			return true;
		} else {
			WBatchEditProxyModel.Item parentItem = this
					.itemFromSourceIndex(sourceParent);
			int row = this.adjustedProxyRow(parentItem, sourceIndex.getRow());
			if (row < 0) {
				return true;
			}
			int column = this.adjustedProxyColumn(parentItem, sourceIndex
					.getColumn());
			return column < 0;
		}
	}

	private int adjustedProxyRow(WBatchEditProxyModel.Item item, int sourceRow) {
		return this.adjustedProxyIndex(sourceRow, item.insertedRows_,
				item.removedRows_);
	}

	private int adjustedSourceRow(WBatchEditProxyModel.Item item, int proxyRow) {
		return this.adjustedSourceIndex(proxyRow, item.insertedRows_,
				item.removedRows_);
	}

	private int adjustedProxyColumn(WBatchEditProxyModel.Item item,
			int sourceColumn) {
		return this.adjustedProxyIndex(sourceColumn, item.insertedColumns_,
				item.removedColumns_);
	}

	private int adjustedSourceColumn(WBatchEditProxyModel.Item item,
			int proxyColumn) {
		return this.adjustedSourceIndex(proxyColumn, item.insertedColumns_,
				item.removedColumns_);
	}

	private int adjustedProxyIndex(int sourceIndex, List<Integer> ins,
			List<Integer> rem) {
		if (ins.isEmpty() && rem.isEmpty()) {
			return sourceIndex;
		}
		int insi = 0;
		int remi = 0;
		int proxyIndex = -1;
		for (int si = 0; si <= sourceIndex; ++si) {
			++proxyIndex;
			while (remi < (int) rem.size() && rem.get(remi) == proxyIndex) {
				if (si == sourceIndex) {
					return -1 - remi;
				}
				++remi;
				++si;
			}
			if (this.submitting_ && si == sourceIndex) {
				return proxyIndex;
			}
			while (insi < (int) ins.size() && ins.get(insi) == proxyIndex) {
				++insi;
				++proxyIndex;
			}
		}
		return proxyIndex;
	}

	private int adjustedSourceIndex(int proxyIndex, List<Integer> ins,
			List<Integer> rem) {
		int inserted = CollectionUtils.lowerBound(ins, proxyIndex);
		if (inserted < ins.size() && ins.get(inserted) == proxyIndex) {
			return -1;
		}
		int removed = CollectionUtils.upperBound(rem, proxyIndex);
		return proxyIndex + removed - inserted;
	}

	private void insertIndexes(WBatchEditProxyModel.Item item,
			List<Integer> ins, List<WBatchEditProxyModel.Item> rowItems,
			int index, int count) {
		int insertIndex = CollectionUtils.lowerBound(ins, index);
		for (int i = 0; i < count; ++i) {
			ins.add(0 + insertIndex + i, index + i);
			if (rowItems != null) {
				rowItems.add(0 + insertIndex + i,
						(WBatchEditProxyModel.Item) null);
			}
		}
	}

	private void removeIndexes(WBatchEditProxyModel.Item item,
			List<Integer> ins, List<Integer> rem,
			List<WBatchEditProxyModel.Item> rowItems, int index, int count) {
		for (int i = 0; i < count; ++i) {
			int insi = CollectionUtils.lowerBound(ins, index);
			if (insi != ins.size() && ins.get(insi) == index) {
				ins.remove(0 + insi);
				if (rowItems != null) {
					;
					rowItems.remove(0 + insi);
				}
			} else {
				if (rowItems != null) {
					this.deleteItemsUnder(item, index);
				}
				rem.add(0 + CollectionUtils.lowerBound(rem, index), index);
			}
			this.shift(ins, index, -1);
			this.shift(rem, index + 1, -1);
		}
	}

	private void deleteItemsUnder(WBatchEditProxyModel.Item item, int row) {
		WModelIndex sourceIndex = this.getSourceModel().getIndex(row, 0,
				item.sourceIndex_);
		for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it = this.mappedIndexes_
				.tailMap(sourceIndex).entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it
					.next();
			if (isAncestor(sourceIndex, i.getKey())) {
				;
				i_it.remove();
			} else {
				break;
			}
		}
	}

	private void shift(List<Integer> v, int index, int count) {
		int first = CollectionUtils.lowerBound(v, index);
		for (int i = first; i < v.size(); ++i) {
			v.set(i, v.get(i) + count);
		}
	}

	private void shiftRows(
			Map<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>> v,
			int row, int count) {
		for (Iterator<Map.Entry<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>>> i_it = v
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>> i = i_it
					.next();
			if (i.getKey().row >= row) {
				WBatchEditProxyModel.Cell c = i.getKey();
				if (count < 0) {
					if (c.row >= row - count) {
						c.row += count;
					} else {
						i_it.remove();
					}
				} else {
					c.row += count;
				}
			} else {
				break;
			}
		}
	}

	private void shiftRows(WBatchEditProxyModel.Item item, int row, int count) {
		this.shift(item.insertedRows_, row, count);
		this.shift(item.removedRows_, row, count);
		this.shiftRows(item.editedValues_, row, count);
	}

	private void shiftColumns(
			Map<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>> v,
			int column, int count) {
		for (Iterator<Map.Entry<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>>> i_it = v
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WBatchEditProxyModel.Cell, SortedMap<Integer, Object>> i = i_it
					.next();
			if (i.getKey().column >= column) {
				WBatchEditProxyModel.Cell c = i.getKey();
				if (count < 0) {
					if (c.column >= column - count) {
						c.column += count;
					} else {
						i_it.remove();
					}
				} else {
					c.column += count;
				}
			} else {
			}
		}
	}

	private void shiftColumns(WBatchEditProxyModel.Item item, int column,
			int count) {
		this.shift(item.insertedColumns_, column, count);
		this.shift(item.removedColumns_, column, count);
		this.shiftColumns(item.editedValues_, column, count);
	}

	private void resetMappings() {
		for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it = this.mappedIndexes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it
					.next();
			;
		}
		this.mappedIndexes_.clear();
	}

	private Object indicateDirty(int role, Object value) {
		if (role == this.dirtyIndicationRole_) {
			if (role == ItemDataRole.StyleClassRole) {
				WString s1 = StringUtils.asString(value);
				WString s2 = StringUtils.asString(this.dirtyIndicationData_);
				if (!(s1.length() == 0)) {
					s1.append(" ");
				}
				s1.append(s2);
				return s1;
			} else {
				return this.dirtyIndicationData_;
			}
		} else {
			return value;
		}
	}

	static boolean isAncestor(WModelIndex i1, WModelIndex i2) {
		if (!(i1 != null)) {
			return false;
		}
		for (WModelIndex p = i1; (p != null); p = p.getParent()) {
			if ((p == i2 || (p != null && p.equals(i2)))) {
				return true;
			}
		}
		return !(i2 != null);
	}
}
