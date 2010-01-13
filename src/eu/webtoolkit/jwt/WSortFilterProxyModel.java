/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import eu.webtoolkit.jwt.utils.CollectionUtils;
import eu.webtoolkit.jwt.utils.ObjectUtils;

/**
 * A proxy model for Wt&apos;s item models that provides filtering and/or
 * sorting.
 * <p>
 * 
 * This proxy model does not store data itself, but presents data from a source
 * model, after filtering rows. It also allows sorting of the source model data,
 * without actually altering the source model. This may be convenient when the
 * source model does not support sorting (i.e. does not reimplement
 * {@link WAbstractItemModel#sort(int column, SortOrder order)
 * WAbstractItemModel#sort()}), or you do not want to reorder the underlying
 * model since that affects all views on the model.
 * <p>
 * To use the proxy model to filter data, you use the methods
 * {@link WSortFilterProxyModel#setFilterKeyColumn(int column)
 * setFilterKeyColumn()},
 * {@link WSortFilterProxyModel#setFilterRegExp(String pattern)
 * setFilterRegExp()} and {@link WSortFilterProxyModel#setFilterRole(int role)
 * setFilterRole()} to specify a filtering operation based on the values of a
 * single column. If this filtering mechanism is too limiting, you can provide
 * specialized filtering by reimplementing the
 * {@link WSortFilterProxyModel#filterAcceptRow(int sourceRow, WModelIndex sourceParent)
 * filterAcceptRow()} method.
 * <p>
 * Sorting is provided by reimplementing the standard
 * {@link WAbstractItemModel#sort(int column, SortOrder order)
 * WAbstractItemModel#sort()} method. In this way, a view class such as
 * {@link WTreeView} may resort the model as indicated by the user. Use
 * {@link WSortFilterProxyModel#setSortRole(int role) setSortRole()} to indicate
 * on what data role sorting should be done, or reimplement the
 * {@link WSortFilterProxyModel#lessThan(WModelIndex lhs, WModelIndex rhs)
 * lessThan()} method to provide a specialized sorting method.
 * <p>
 * By default, the proxy does not automatically re-filter and re-sort when the
 * original model changes. You can enable this behaviour using
 * {@link WSortFilterProxyModel#setDynamicSortFilter(boolean enable)
 * setDynamicSortFilter()}.
 * <p>
 * Usage example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * // model is the source model
 *   WAbstractItemModel model = ...
 *  
 *  // we setup a proxy to filter the source model
 *  WSortFilterProxyModel proxy = new WSortFilterProxyModel(this);
 *  proxy.setSourceModel(model);
 *  proxy.setDynamicSortFilter(true);
 *  proxy.setFilterKeyColumn(0);
 *  proxy.setFilterRole(ItemDataRole.UserRole);
 *  proxy.setFilterRegExp(".*");
 * 		 
 *  // configure a view to use the proxy model instead of the source model
 *  WTreeView view = new WTreeView(this);
 *  view.setModel(proxy);
 *  ...
 * </pre>
 * 
 * </blockquote>
 * <p>
 * <p>
 * <i><b>Note: </b>The implementation is not yet complete: the proxy model does
 * not yet react properly to row insertions and row removals in the source
 * model. </i>
 * </p>
 */
public class WSortFilterProxyModel extends WAbstractProxyModel {
	/**
	 * Constructor.
	 */
	public WSortFilterProxyModel(WObject parent) {
		super(parent);
		this.regex_ = null;
		this.filterKeyColumn_ = 0;
		this.filterRole_ = ItemDataRole.DisplayRole;
		this.sortKeyColumn_ = -1;
		this.sortRole_ = ItemDataRole.DisplayRole;
		this.sortOrder_ = SortOrder.AscendingOrder;
		this.dynamic_ = 0 != 0;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.mappedIndexes_ = new HashMap<WModelIndex, WSortFilterProxyModel.Item>();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WSortFilterProxyModel(WObject parent) this((WObject)null)}
	 */
	public WSortFilterProxyModel() {
		this((WObject) null);
	}

	public WModelIndex mapFromSource(WModelIndex sourceIndex) {
		if ((sourceIndex != null)) {
			WModelIndex sourceParent = sourceIndex.getParent();
			WSortFilterProxyModel.Item item = this
					.itemFromSourceIndex(sourceParent);
			int row = item.sourceRowMap_.get(sourceIndex.getRow());
			if (row != -1) {
				return this.createIndex(row, sourceIndex.getColumn(), item);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public WModelIndex mapToSource(WModelIndex proxyIndex) {
		if ((proxyIndex != null)) {
			WSortFilterProxyModel.Item parentItem = this
					.parentItemFromIndex(proxyIndex);
			return this.getSourceModel().getIndex(
					parentItem.proxyRowMap_.get(proxyIndex.getRow()),
					proxyIndex.getColumn(), parentItem.sourceIndex_);
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
								WSortFilterProxyModel.this
										.sourceColumnsAboutToBeInserted(e1, e2,
												e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().columnsInserted()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WSortFilterProxyModel.this
										.sourceColumnsInserted(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel()
				.columnsAboutToBeRemoved().addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WSortFilterProxyModel.this
										.sourceColumnsAboutToBeRemoved(e1, e2,
												e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().columnsRemoved()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WSortFilterProxyModel.this
										.sourceColumnsRemoved(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel()
				.rowsAboutToBeInserted().addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WSortFilterProxyModel.this
										.sourceRowsAboutToBeInserted(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().rowsInserted()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WSortFilterProxyModel.this.sourceRowsInserted(
										e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().rowsAboutToBeRemoved()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WSortFilterProxyModel.this
										.sourceRowsAboutToBeRemoved(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().rowsRemoved()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WSortFilterProxyModel.this.sourceRowsRemoved(
										e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().dataChanged()
				.addListener(this,
						new Signal2.Listener<WModelIndex, WModelIndex>() {
							public void trigger(WModelIndex e1, WModelIndex e2) {
								WSortFilterProxyModel.this.sourceDataChanged(
										e1, e2);
							}
						}));
		this.modelConnections_.add(this.getSourceModel().headerDataChanged()
				.addListener(this,
						new Signal3.Listener<Orientation, Integer, Integer>() {
							public void trigger(Orientation e1, Integer e2,
									Integer e3) {
								WSortFilterProxyModel.this
										.sourceHeaderDataChanged(e1, e2, e3);
							}
						}));
		this.modelConnections_.add(this.getSourceModel()
				.layoutAboutToBeChanged().addListener(this,
						new Signal.Listener() {
							public void trigger() {
								WSortFilterProxyModel.this
										.sourceLayoutAboutToBeChanged();
							}
						}));
		this.modelConnections_.add(this.getSourceModel().layoutChanged()
				.addListener(this, new Signal.Listener() {
					public void trigger() {
						WSortFilterProxyModel.this.sourceLayoutChanged();
					}
				}));
		this.resetMappings();
	}

	/**
	 * Specify the column on which the filtering is applied.
	 * <p>
	 * This configures the column on which the
	 * {@link WSortFilterProxyModel#getFilterRegExp() getFilterRegExp()} is
	 * applied.
	 * <p>
	 * The default value is 0.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#setFilterRegExp(String pattern)
	 * @see WSortFilterProxyModel#setFilterRole(int role)
	 */
	public void setFilterKeyColumn(int column) {
		this.filterKeyColumn_ = column;
	}

	/**
	 * Return the column on which the filtering is applied.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#setFilterKeyColumn(int column)
	 */
	public int getFilterKeyColumn() {
		return this.filterKeyColumn_;
	}

	/**
	 * Specify a regular expression for filtering.
	 * <p>
	 * This configures the regular expression used for filtering on
	 * {@link WSortFilterProxyModel#getFilterKeyColumn() getFilterKeyColumn()}.
	 * <p>
	 * The default value is an empty expression, which disables filtering.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#setFilterKeyColumn(int column)
	 * @see WSortFilterProxyModel#setFilterRole(int role)
	 */
	public void setFilterRegExp(String pattern) {
		if (!(this.regex_ != null)) {
			this.regex_ = Pattern.compile(pattern);
		} else {
			this.regex_ = Pattern.compile(pattern);
		}
		if (this.getSourceModel() != null && this.dynamic_) {
			this.layoutAboutToBeChanged().trigger();
			this.resetMappings();
			this.layoutChanged().trigger();
		}
	}

	/**
	 * Return the regular expression used for filtering.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#setFilterRegExp(String pattern)
	 */
	public String getFilterRegExp() {
		return this.regex_ != null ? this.regex_.pattern() : "";
	}

	/**
	 * Specify the data role used for filtering.
	 * <p>
	 * This configures the data role used for filtering on
	 * {@link WSortFilterProxyModel#getFilterKeyColumn() getFilterKeyColumn()}.
	 * <p>
	 * The default value is {@link ItemDataRole#DisplayRole DisplayRole}.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#setFilterKeyColumn(int column)
	 * @see WSortFilterProxyModel#setFilterRegExp(String pattern)
	 */
	public void setFilterRole(int role) {
		this.filterRole_ = role;
	}

	/**
	 * Return the data role used for filtering.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#setFilterRole(int role)
	 */
	public int getFilterRole() {
		return this.filterRole_;
	}

	/**
	 * Specify the data role used used for sorting.
	 * <p>
	 * This configures the data role used for sorting.
	 * <p>
	 * The default value is {@link ItemDataRole#DisplayRole DisplayRole}.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#lessThan(WModelIndex lhs, WModelIndex rhs)
	 */
	public void setSortRole(int role) {
		this.sortRole_ = role;
	}

	/**
	 * Return the data role used for sorting.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#setSortRole(int role)
	 */
	public int getSortRole() {
		return this.sortRole_;
	}

	/**
	 * Configure the proxy to dynamically track changes in the source model.
	 * <p>
	 * When <code>enable</code> is <code>true</code>, the proxy will re-filter
	 * and re-sort the model when changes happen to the source model.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>This may be ackward when editing through the proxy model,
	 * since changing some data may rearrange the model and thus invalidate
	 * model indexes. Therefore it is usually less complicated to manipulate
	 * directly the source model instead.</i>
	 * </p>
	 * 
	 * @see WSortFilterProxyModel#lessThan(WModelIndex lhs, WModelIndex rhs)
	 */
	public void setDynamicSortFilter(boolean enable) {
		this.dynamic_ = enable;
	}

	/**
	 * Returns whether this proxy dynmically filters and sorts.
	 * <p>
	 * 
	 * @see WSortFilterProxyModel#setDynamicSortFilter(boolean enable)
	 */
	public boolean isDynamicSortFilter() {
		return this.dynamic_;
	}

	public int getColumnCount(WModelIndex parent) {
		return this.getSourceModel().getColumnCount(this.mapToSource(parent));
	}

	public int getRowCount(WModelIndex parent) {
		WSortFilterProxyModel.Item item = this.itemFromIndex(parent);
		return item.proxyRowMap_.size();
	}

	public WModelIndex getParent(WModelIndex index) {
		if ((index != null)) {
			WSortFilterProxyModel.Item parentItem = this
					.parentItemFromIndex(index);
			return this.mapFromSource(parentItem.sourceIndex_);
		} else {
			return null;
		}
	}

	public WModelIndex getIndex(int row, int column, WModelIndex parent) {
		WSortFilterProxyModel.Item item = this.itemFromIndex(parent);
		return this.createIndex(row, column, item);
	}

	public boolean setHeaderData(int section, Orientation orientation,
			Object value, int role) {
		if (orientation == Orientation.Vertical) {
			section = this.mapToSource(this.getIndex(section, 0)).getRow();
		}
		return this.getSourceModel().setHeaderData(section, orientation, value,
				role);
	}

	public Object getHeaderData(int section, Orientation orientation, int role) {
		if (orientation == Orientation.Vertical) {
			section = this.mapToSource(this.getIndex(section, 0)).getRow();
		}
		return this.getSourceModel().getHeaderData(section, orientation, role);
	}

	public void sort(int column, SortOrder order) {
		this.sortKeyColumn_ = column;
		this.sortOrder_ = order;
		if (this.getSourceModel() != null) {
			this.layoutAboutToBeChanged().trigger();
			this.resetMappings();
			this.layoutChanged().trigger();
		}
	}

	/**
	 * Returns whether a source row is accepted by the filter.
	 * <p>
	 * The default implementation uses
	 * {@link WSortFilterProxyModel#getFilterKeyColumn() getFilterKeyColumn()},
	 * {@link WSortFilterProxyModel#getFilterRole() getFilterRole()} and
	 * {@link WSortFilterProxyModel#getFilterRegExp() getFilterRegExp()}.
	 * <p>
	 * You may want to reimplement this method to provide specialized filtering.
	 */
	protected boolean filterAcceptRow(int sourceRow, WModelIndex sourceParent) {
		if (this.regex_ != null) {
			WString s = StringUtils.asString(this.getSourceModel().getIndex(
					sourceRow, this.filterKeyColumn_, sourceParent).getData(
					this.filterRole_));
			boolean result = this.regex_.matcher(s.toString()).matches();
			return result;
		} else {
			return true;
		}
	}

	/**
	 * Compares two indexes.
	 * <p>
	 * The default implementation uses
	 * {@link WSortFilterProxyModel#getSortRole() getSortRole()} and an ordering
	 * using the operator&lt; when the data is of the same type or compares
	 * lexicographically otherwise.
	 * <p>
	 * You may want to reimplement this method to provide specialized sorting.
	 */
	protected boolean lessThan(WModelIndex lhs, WModelIndex rhs) {
		return this.compare(lhs, rhs) < 0;
	}

	static class Item {
		public List<Integer> sourceRowMap_;
		public List<Integer> proxyRowMap_;
		public WModelIndex sourceIndex_;

		public Item(WModelIndex sourceIndex) {
			this.sourceRowMap_ = new ArrayList<Integer>();
			this.proxyRowMap_ = new ArrayList<Integer>();
			this.sourceIndex_ = sourceIndex;
		}
	}

	static class Compare implements Comparator<Integer> {
		public Compare(WSortFilterProxyModel aModel,
				WSortFilterProxyModel.Item anItem) {
			super();
			this.model = aModel;
			this.item = anItem;
		}

		public int compare(Integer sourceRow1, Integer sourceRow2) {
			int factor = this.model.sortOrder_ == SortOrder.AscendingOrder ? 1
					: -1;
			if (this.model.sortKeyColumn_ == -1) {
				return factor * (sourceRow1 - sourceRow2);
			}
			WModelIndex lhs = this.model.getSourceModel().getIndex(sourceRow1,
					this.model.sortKeyColumn_, this.item.sourceIndex_);
			WModelIndex rhs = this.model.getSourceModel().getIndex(sourceRow2,
					this.model.sortKeyColumn_, this.item.sourceIndex_);
			return factor * this.model.compare(lhs, rhs);
		}

		public WSortFilterProxyModel model;
		public WSortFilterProxyModel.Item item;
	}

	private Pattern regex_;
	private int filterKeyColumn_;
	private int filterRole_;
	private int sortKeyColumn_;
	private int sortRole_;
	private SortOrder sortOrder_;
	private boolean dynamic_;
	private List<AbstractSignal.Connection> modelConnections_;
	private Map<WModelIndex, WSortFilterProxyModel.Item> mappedIndexes_;

	private void sourceColumnsAboutToBeInserted(WModelIndex parent, int start,
			int end) {
		this.beginInsertColumns(this.mapFromSource(parent), start, end);
	}

	private void sourceColumnsInserted(WModelIndex parent, int start, int end) {
		this.endInsertColumns();
	}

	private void sourceColumnsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		this.beginRemoveColumns(this.mapFromSource(parent), start, end);
	}

	private void sourceColumnsRemoved(WModelIndex parent, int start, int end) {
		this.endRemoveColumns();
	}

	private void sourceRowsAboutToBeInserted(WModelIndex parent, int start,
			int end) {
	}

	private void sourceRowsInserted(WModelIndex parent, int start, int end) {
	}

	private void sourceRowsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
	}

	private void sourceRowsRemoved(WModelIndex parent, int start, int end) {
	}

	private void sourceDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
		boolean refilter = this.dynamic_
				&& (this.filterKeyColumn_ >= topLeft.getColumn() && this.filterKeyColumn_ <= bottomRight
						.getColumn());
		boolean resort = this.dynamic_
				&& (this.sortKeyColumn_ >= topLeft.getColumn() && this.sortKeyColumn_ <= bottomRight
						.getColumn());
		WModelIndex parent = this.mapFromSource(topLeft.getParent());
		WSortFilterProxyModel.Item item = this.itemFromIndex(parent);
		for (int row = topLeft.getRow(); row <= bottomRight.getRow(); ++row) {
			int oldMappedRow = item.sourceRowMap_.get(row);
			boolean propagateDataChange = oldMappedRow != -1;
			if (refilter || resort) {
				item.proxyRowMap_.remove(0 + oldMappedRow);
				int newMappedRow = this.changedMappedRow(row, oldMappedRow,
						item);
				item.proxyRowMap_.add(0 + oldMappedRow, row);
				if (newMappedRow != oldMappedRow) {
					if (oldMappedRow != -1) {
						this
								.beginRemoveRows(parent, oldMappedRow,
										oldMappedRow);
						item.proxyRowMap_.remove(0 + oldMappedRow);
						this.rebuildSourceRowMap(item);
						this.endRemoveRows();
					}
					if (newMappedRow != -1) {
						this
								.beginInsertRows(parent, newMappedRow,
										newMappedRow);
						item.proxyRowMap_.add(0 + newMappedRow, row);
						this.rebuildSourceRowMap(item);
						this.endInsertRows();
					}
					propagateDataChange = false;
				}
			}
			if (propagateDataChange) {
				WModelIndex l = this.getSourceModel().getIndex(row,
						topLeft.getColumn(), topLeft.getParent());
				WModelIndex r = this.getSourceModel().getIndex(row,
						bottomRight.getColumn(), topLeft.getParent());
				this.dataChanged().trigger(this.mapFromSource(l),
						this.mapFromSource(r));
			}
		}
	}

	private void sourceHeaderDataChanged(Orientation orientation, int start,
			int end) {
		if (orientation == Orientation.Vertical) {
			WSortFilterProxyModel.Item item = this.itemFromIndex(null);
			for (int row = start; row <= end; ++row) {
				int mappedRow = item.sourceRowMap_.get(row);
				if (mappedRow != -1) {
					this.headerDataChanged().trigger(orientation, mappedRow,
							mappedRow);
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

	private WSortFilterProxyModel.Item itemFromSourceIndex(
			WModelIndex sourceParent) {
		WSortFilterProxyModel.Item i = this.mappedIndexes_.get(sourceParent);
		if (i == null) {
			WSortFilterProxyModel.Item result = new WSortFilterProxyModel.Item(
					sourceParent);
			this.mappedIndexes_.put(sourceParent, result);
			this.updateItem(result);
			return result;
		} else {
			return i;
		}
	}

	private WSortFilterProxyModel.Item parentItemFromIndex(WModelIndex index) {
		return (WSortFilterProxyModel.Item) index.getInternalPointer();
	}

	private WSortFilterProxyModel.Item itemFromIndex(WModelIndex index) {
		if ((index != null)) {
			WSortFilterProxyModel.Item parentItem = this
					.parentItemFromIndex(index);
			WModelIndex sourceIndex = this.getSourceModel().getIndex(
					parentItem.proxyRowMap_.get(index.getRow()),
					index.getColumn(), parentItem.sourceIndex_);
			return this.itemFromSourceIndex(sourceIndex);
		} else {
			return this.itemFromSourceIndex(null);
		}
	}

	private void resetMappings() {
		for (Iterator<Map.Entry<WModelIndex, WSortFilterProxyModel.Item>> i_it = this.mappedIndexes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WSortFilterProxyModel.Item> i = i_it.next();
			;
		}
		this.mappedIndexes_.clear();
	}

	private void updateItem(WSortFilterProxyModel.Item item) {
		int sourceRowCount = this.getSourceModel().getRowCount(
				item.sourceIndex_);
		CollectionUtils.resize(item.sourceRowMap_, sourceRowCount);
		item.proxyRowMap_.clear();
		for (int i = 0; i < sourceRowCount; ++i) {
			if (this.filterAcceptRow(i, item.sourceIndex_)) {
				item.sourceRowMap_.set(i, item.proxyRowMap_.size());
				item.proxyRowMap_.add(i);
			} else {
				item.sourceRowMap_.set(i, -1);
			}
		}
		if (this.sortKeyColumn_ != -1) {
			Collections.sort(item.proxyRowMap_,
					new WSortFilterProxyModel.Compare(this, item));
			this.rebuildSourceRowMap(item);
		}
	}

	private void rebuildSourceRowMap(WSortFilterProxyModel.Item item) {
		for (int i = 0; i < item.proxyRowMap_.size(); ++i) {
			item.sourceRowMap_.set(item.proxyRowMap_.get(i), i);
		}
	}

	private int changedMappedRow(int sourceRow, int currentMappedRow,
			WSortFilterProxyModel.Item item) {
		boolean acceptRow = this.filterAcceptRow(sourceRow, item.sourceIndex_);
		if (!acceptRow) {
			return -1;
		} else {
			return CollectionUtils.insertionPoint(item.proxyRowMap_, sourceRow,
					new WSortFilterProxyModel.Compare(this, item));
		}
	}

	private int compare(WModelIndex lhs, WModelIndex rhs) {
		return ObjectUtils.compare(lhs.getData(this.sortRole_), rhs
				.getData(this.sortRole_));
	}
}
