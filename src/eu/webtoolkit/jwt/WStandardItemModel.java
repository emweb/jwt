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
 * A standard data model, which stores its data in memory.
 * <p>
 * 
 * The standard item model supports all features of {@link WAbstractItemModel},
 * and can thus be used to represent tables, trees and tree tables.
 * <p>
 * The data itself are organized in {@link WStandardItem} objects. There is one
 * invisible root object ({@link WStandardItemModel#getInvisibleRootItem()
 * getInvisibleRootItem()}) that holds the toplevel data. Most methods in this
 * class that access or manipulate data internally operate on this root item.
 * <p>
 * If you want to use the model as a table, then you can use
 * {@link WStandardItemModel#WStandardItemModel(int rows, int columns, WObject parent)
 * WStandardItemModel()} to set the initial table size, and use the
 * {@link WStandardItemModel#getItem(int row, int column) getItem()} and
 * {@link WStandardItemModel#setItem(int row, int column, WStandardItem item)
 * setItem()} methods to set data. You can change the geometry by inserting rows
 * ({@link WStandardItemModel#insertRow(int row, List items) insertRow()}) or
 * columns ({@link WStandardItemModel#insertColumn(int column, List items)
 * insertColumn()}) or removing rows (
 * {@link WAbstractItemModel#removeRow(int row, WModelIndex parent)
 * WAbstractItemModel#removeRow()}) or columns (
 * {@link WAbstractItemModel#removeColumn(int column, WModelIndex parent)
 * WAbstractItemModel#removeColumn()}).
 * <p>
 * If you want to use the model as a tree (or tree table), then you can use the
 * default constructor to start with an empty tree, and use the
 * {@link WStandardItem} API on
 * {@link WStandardItemModel#getInvisibleRootItem() getInvisibleRootItem()} to
 * manipulate the tree root. When you are building a tree, the column count at
 * each node is 1. When you are building a tree table, you can add additional
 * columns of data for each internal node. Only the items in the first column
 * have children that result in a hierarchical tree structure.
 * <p>
 * When using the model with a view class, you can use the
 * {@link WStandardItemModel#getItemFromIndex(WModelIndex index)
 * getItemFromIndex()} and
 * {@link WStandardItemModel#indexFromItem(WStandardItem item) indexFromItem()}
 * models to translate between model indexes (that are used by the view class)
 * and standard items.
 */
public class WStandardItemModel extends WAbstractItemModel {
	private static Logger logger = LoggerFactory
			.getLogger(WStandardItemModel.class);

	/**
	 * Creates a new standard item model.
	 */
	public WStandardItemModel(WObject parent) {
		super(parent);
		this.sortRole_ = ItemDataRole.DisplayRole;
		this.columnHeaderData_ = new ArrayList<Map<Integer, Object>>();
		this.rowHeaderData_ = new ArrayList<Map<Integer, Object>>();
		this.columnHeaderFlags_ = new ArrayList<EnumSet<HeaderFlag>>();
		this.rowHeaderFlags_ = new ArrayList<EnumSet<HeaderFlag>>();
		this.itemChanged_ = new Signal1<WStandardItem>(this);
		this.init();
	}

	/**
	 * Creates a new standard item model.
	 * <p>
	 * Calls {@link #WStandardItemModel(WObject parent) this((WObject)null)}
	 */
	public WStandardItemModel() {
		this((WObject) null);
	}

	/**
	 * Creates a new standard item model with an initial geometry.
	 * <p>
	 * Creates a standard item model with a geometry of <i>rows</i> x
	 * <code>columns</code>. All items are set to <code>null</code>.
	 */
	public WStandardItemModel(int rows, int columns, WObject parent) {
		super(parent);
		this.sortRole_ = ItemDataRole.DisplayRole;
		this.columnHeaderData_ = new ArrayList<Map<Integer, Object>>();
		this.rowHeaderData_ = new ArrayList<Map<Integer, Object>>();
		this.columnHeaderFlags_ = new ArrayList<EnumSet<HeaderFlag>>();
		this.rowHeaderFlags_ = new ArrayList<EnumSet<HeaderFlag>>();
		this.itemChanged_ = new Signal1<WStandardItem>(this);
		this.init();
		this.invisibleRootItem_.setColumnCount(columns);
		this.invisibleRootItem_.setRowCount(rows);
	}

	/**
	 * Creates a new standard item model with an initial geometry.
	 * <p>
	 * Calls {@link #WStandardItemModel(int rows, int columns, WObject parent)
	 * this(rows, columns, (WObject)null)}
	 */
	public WStandardItemModel(int rows, int columns) {
		this(rows, columns, (WObject) null);
	}

	/**
	 * Erases all data in the model.
	 * <p>
	 * After clearing the model,
	 * {@link WAbstractItemModel#getRowCount(WModelIndex parent)
	 * WAbstractItemModel#getRowCount()} and
	 * {@link WAbstractItemModel#getColumnCount(WModelIndex parent)
	 * WAbstractItemModel#getColumnCount()} are 0.
	 */
	public void clear() {
		this.invisibleRootItem_.setRowCount(0);
		this.invisibleRootItem_.setColumnCount(0);
		this.columnHeaderData_.clear();
		this.rowHeaderData_.clear();
		this.columnHeaderFlags_.clear();
		this.rowHeaderFlags_.clear();
		this.reset();
	}

	/**
	 * Returns the invisible root item.
	 * <p>
	 * The invisible root item is a special item that is not rendered itself,
	 * but holds the top level data.
	 */
	public WStandardItem getInvisibleRootItem() {
		return this.invisibleRootItem_;
	}

	/**
	 * Returns the model index for a particular item.
	 * <p>
	 * If the <code>item</code> is the
	 * {@link WStandardItemModel#getInvisibleRootItem() getInvisibleRootItem()},
	 * then an invalid index is returned.
	 * <p>
	 * 
	 * @see WStandardItemModel#getItemFromIndex(WModelIndex index)
	 */
	public WModelIndex indexFromItem(WStandardItem item) {
		if (item == this.invisibleRootItem_) {
			return null;
		} else {
			return this.createIndex(item.getRow(), item.getColumn(), item
					.getParent());
		}
	}

	/**
	 * Returns the standard item that corresponds to a model index.
	 * <p>
	 * If the index is an invalid index, then the
	 * {@link WStandardItemModel#getInvisibleRootItem() getInvisibleRootItem()}
	 * is returned.
	 * <p>
	 * 
	 * @see WStandardItemModel#indexFromItem(WStandardItem item)
	 */
	public WStandardItem getItemFromIndex(WModelIndex index) {
		return this.getItemFromIndex(index, true);
	}

	/**
	 * Adds a single column of top level items.
	 * <p>
	 * Appends a single column of top level <code>items</code>. If necessary,
	 * the row count is increased.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * insertColumn(columnCount(), items);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItemModel#insertColumn(int column, List items)
	 * @see WStandardItemModel#appendRow(List items)
	 */
	public void appendColumn(List<WStandardItem> items) {
		this.insertColumn(this.getColumnCount(), items);
	}

	/**
	 * Inserts a single column of top level items.
	 * <p>
	 * Inserts a single column of top level <code>items</code> at column
	 * <code>column</code>. If necessary, the row count is increased.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * invisibleRootItem().insertColumn(column, items);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertColumn(int column, List items)
	 */
	public void insertColumn(int column, List<WStandardItem> items) {
		this.invisibleRootItem_.insertColumn(column, items);
	}

	/**
	 * Adds a single row of top level items.
	 * <p>
	 * Appends a single row of top level <code>items</code>. If necessary, the
	 * column count is increased.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * insertRow(rowCount(), items);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItemModel#insertRow(int row, List items)
	 * @see WStandardItemModel#appendColumn(List items)
	 */
	public void appendRow(List<WStandardItem> items) {
		this.insertRow(this.getRowCount(), items);
	}

	/**
	 * Inserts a single row of top level items.
	 * <p>
	 * Inserts a single row of top level <code>items</code> at row
	 * <code>row</code>. If necessary, the column count is increased.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * invisibleRootItem().insertRow(row, items);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertRow(int row, List items)
	 */
	public void insertRow(int row, List<WStandardItem> items) {
		this.invisibleRootItem_.insertRow(row, items);
	}

	/**
	 * Appends a single row containing a single item.
	 * <p>
	 * Appends a single toplevel row, with a single item.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * insertRow(rowCount(), item);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertRow(int row, WStandardItem item)
	 */
	public void appendRow(WStandardItem item) {
		this.insertRow(this.getRowCount(), item);
	}

	/**
	 * Inserts a single row containing a single item.
	 * <p>
	 * Inserts a single toplevel row, with a single item.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * invisibleRootItem().insertRow(row, item);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#insertRow(int row, WStandardItem item)
	 */
	public void insertRow(int row, WStandardItem item) {
		this.invisibleRootItem_.insertRow(row, item);
	}

	/**
	 * Returns a toplevel item.
	 * <p>
	 * Returns the top level at at (<i>row</i>, <code>column</code>). This may
	 * be 0 if no item was set previously at that position, or if the indicated
	 * position is out of bounds.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * invisibleRootItem().child(row, column);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#getChild(int row, int column)
	 */
	public WStandardItem getItem(int row, int column) {
		return this.invisibleRootItem_.getChild(row, column);
	}

	/**
	 * Returns a toplevel item.
	 * <p>
	 * Returns {@link #getItem(int row, int column) getItem(row, 0)}
	 */
	public final WStandardItem getItem(int row) {
		return getItem(row, 0);
	}

	/**
	 * Sets a toplevel item.
	 * <p>
	 * Sets the top level at at (<i>row</i>, <code>column</code>). If necessary,
	 * the number of rows or columns is increased.
	 * <p>
	 * If an item was previously set for that position, it is deleted first.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * invisibleRootItem().setChild(row, column, item);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#setChild(int row, int column, WStandardItem item)
	 */
	public void setItem(int row, int column, WStandardItem item) {
		this.invisibleRootItem_.setChild(row, column, item);
	}

	/**
	 * Returns the item prototype.
	 * <p>
	 * 
	 * @see WStandardItemModel#setItemPrototype(WStandardItem item)
	 */
	public WStandardItem getItemPrototype() {
		return this.itemPrototype_;
	}

	/**
	 * Sets the item prototype.
	 * <p>
	 * Set the item that is cloned when an item needs to be created because the
	 * model is manipulated through its {@link WAbstractItemModel} API. For
	 * example, this may be needed when a view sets data at a position for which
	 * no item was previously set and thus created.
	 * <p>
	 * The new item is created based on this prototype by using
	 * {@link WStandardItem#clone() WStandardItem#clone()}.
	 * <p>
	 * The default prototype is WStandardItem().
	 * <p>
	 * 
	 * @see WStandardItemModel#setItemPrototype(WStandardItem item)
	 */
	public void setItemPrototype(WStandardItem item) {
		;
		this.itemPrototype_ = item;
	}

	/**
	 * Takes a column out of the model.
	 * <p>
	 * Removes a column from the model, and returns the items that it contained.
	 * Ownership of the items is transferred out of the model.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * invisibleRootItem().takeColumn(column);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#takeColumn(int column)
	 * @see WStandardItem#takeRow(int row)
	 */
	public List<WStandardItem> takeColumn(int column) {
		return this.invisibleRootItem_.takeColumn(column);
	}

	/**
	 * Takes a row out of the model.
	 * <p>
	 * Removes a row from the model, and returns the items that it contained.
	 * Ownership of the items is transferred out of the model.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * invisibleRootItem().takeRow(row);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItem#takeRow(int row)
	 * @see WStandardItemModel#takeColumn(int column)
	 */
	public List<WStandardItem> takeRow(int row) {
		return this.invisibleRootItem_.takeRow(row);
	}

	/**
	 * Takes an item out of the model.
	 * <p>
	 * Removes an item from the model, and returns it. Ownership of the item is
	 * transferred out of the model.
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * invisibleRootItem().takeItem(row, column);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WStandardItemModel#takeItem(int row, int column)
	 * @see WStandardItem#takeRow(int row)
	 * @see WStandardItem#takeColumn(int column)
	 */
	public WStandardItem takeItem(int row, int column) {
		return this.invisibleRootItem_.takeChild(row, column);
	}

	/**
	 * Takes an item out of the model.
	 * <p>
	 * Returns {@link #takeItem(int row, int column) takeItem(row, 0)}
	 */
	public final WStandardItem takeItem(int row) {
		return takeItem(row, 0);
	}

	/**
	 * Sets header flags.
	 * <p>
	 * By default, no flags are set.
	 */
	public void setHeaderFlags(int section, Orientation orientation,
			EnumSet<HeaderFlag> flags) {
		List<EnumSet<HeaderFlag>> fl = orientation == Orientation.Horizontal ? this.columnHeaderFlags_
				: this.rowHeaderFlags_;
		fl.set(section, flags);
	}

	/**
	 * Sets header flags.
	 * <p>
	 * Calls
	 * {@link #setHeaderFlags(int section, Orientation orientation, EnumSet flags)
	 * setHeaderFlags(section, orientation, EnumSet.of(flag, flags))}
	 */
	public final void setHeaderFlags(int section, Orientation orientation,
			HeaderFlag flag, HeaderFlag... flags) {
		setHeaderFlags(section, orientation, EnumSet.of(flag, flags));
	}

	public EnumSet<HeaderFlag> getHeaderFlags(int section,
			Orientation orientation) {
		List<EnumSet<HeaderFlag>> fl = orientation == Orientation.Horizontal ? this.columnHeaderFlags_
				: this.rowHeaderFlags_;
		return fl.get(section);
	}

	public EnumSet<ItemFlag> getFlags(WModelIndex index) {
		WStandardItem item = this.getItemFromIndex(index, false);
		return item != null ? item.getFlags() : EnumSet.noneOf(ItemFlag.class);
	}

	public WModelIndex getParent(WModelIndex index) {
		if (!(index != null)) {
			return index;
		}
		WStandardItem parent = (WStandardItem) index.getInternalPointer();
		return this.indexFromItem(parent);
	}

	public Object getData(WModelIndex index, int role) {
		WStandardItem item = this.getItemFromIndex(index, false);
		return item != null ? item.getData(role) : null;
	}

	public Object getHeaderData(int section, Orientation orientation, int role) {
		if (role == ItemDataRole.LevelRole) {
			return 0;
		}
		Map<Integer, Object> d = orientation == Orientation.Horizontal ? this.columnHeaderData_
				.get(section)
				: this.rowHeaderData_.get(section);
		Object i = d.get(role);
		return i != null ? i : null;
	}

	public WModelIndex getIndex(int row, int column, WModelIndex parent) {
		WStandardItem parentItem = this.getItemFromIndex(parent, false);
		if (parentItem != null && row >= 0 && column >= 0
				&& row < parentItem.getRowCount()
				&& column < parentItem.getColumnCount()) {
			return this.createIndex(row, column, parentItem);
		}
		return null;
	}

	public int getColumnCount(WModelIndex parent) {
		WStandardItem parentItem = this.getItemFromIndex(parent, false);
		return parentItem != null ? parentItem.getColumnCount() : 0;
	}

	public int getRowCount(WModelIndex parent) {
		WStandardItem parentItem = this.getItemFromIndex(parent, false);
		return parentItem != null ? parentItem.getRowCount() : 0;
	}

	public boolean insertColumns(int column, int count, WModelIndex parent) {
		WStandardItem parentItem = this.getItemFromIndex(parent);
		if (parentItem != null) {
			parentItem.insertColumns(column, count);
		}
		return parentItem != null;
	}

	public boolean insertRows(int row, int count, WModelIndex parent) {
		WStandardItem parentItem = this.getItemFromIndex(parent);
		if (parentItem != null) {
			parentItem.insertRows(row, count);
		}
		return parentItem != null;
	}

	public boolean removeColumns(int column, int count, WModelIndex parent) {
		WStandardItem parentItem = this.getItemFromIndex(parent, false);
		if (parentItem != null) {
			parentItem.removeColumns(column, count);
		}
		return parentItem != null;
	}

	public boolean removeRows(int row, int count, WModelIndex parent) {
		WStandardItem parentItem = this.getItemFromIndex(parent, false);
		if (parentItem != null) {
			parentItem.removeRows(row, count);
		}
		return parentItem != null;
	}

	public boolean setData(WModelIndex index, Object value, int role) {
		WStandardItem item = this.getItemFromIndex(index);
		if (item != null) {
			item.setData(value, role);
		}
		return item != null;
	}

	public boolean setHeaderData(int section, Orientation orientation,
			Object value, int role) {
		List<Map<Integer, Object>> header = orientation == Orientation.Horizontal ? this.columnHeaderData_
				: this.rowHeaderData_;
		Map<Integer, Object> d = header.get(section);
		if (role == ItemDataRole.EditRole) {
			role = ItemDataRole.DisplayRole;
		}
		d.put(role, value);
		this.headerDataChanged().trigger(orientation, section, section);
		return true;
	}

	public Object toRawIndex(WModelIndex index) {
		return this.getItemFromIndex(index);
	}

	public WModelIndex fromRawIndex(Object rawIndex) {
		return this.indexFromItem((WStandardItem) rawIndex);
	}

	/**
	 * Set the role used to sort the model.
	 * <p>
	 * The default role is {@link ItemDataRole#DisplayRole DisplayRole}.
	 * <p>
	 * 
	 * @see WStandardItemModel#sort(int column, SortOrder order)
	 */
	public void setSortRole(int role) {
		this.sortRole_ = role;
	}

	/**
	 * Returns the role used to sort the model.
	 * <p>
	 * 
	 * @see WStandardItemModel#setSortRole(int role)
	 */
	public int getSortRole() {
		return this.sortRole_;
	}

	public void sort(int column, SortOrder order) {
		this.invisibleRootItem_.sortChildren(column, order);
	}

	/**
	 * Signal emitted when an item is changed.
	 * <p>
	 * This signal is emitted whenever data for an item has changed. The item
	 * that has changed is passed as the first parameter.
	 * <p>
	 * 
	 * @see WStandardItem#setData(Object d, int role)
	 */
	public Signal1<WStandardItem> itemChanged() {
		return this.itemChanged_;
	}

	protected void beginInsertColumns(WModelIndex parent, int first, int last) {
		super.beginInsertColumns(parent, first, last);
		this.insertHeaderData(this.columnHeaderData_, this.columnHeaderFlags_,
				this.getItemFromIndex(parent), first, last - first + 1);
	}

	protected void beginInsertRows(WModelIndex parent, int first, int last) {
		super.beginInsertRows(parent, first, last);
		this.insertHeaderData(this.rowHeaderData_, this.rowHeaderFlags_, this
				.getItemFromIndex(parent), first, last - first + 1);
	}

	protected void beginRemoveColumns(WModelIndex parent, int first, int last) {
		super.beginRemoveColumns(parent, first, last);
		this.removeHeaderData(this.columnHeaderData_, this.columnHeaderFlags_,
				this.getItemFromIndex(parent), first, last - first + 1);
	}

	protected void beginRemoveRows(WModelIndex parent, int first, int last) {
		super.beginRemoveRows(parent, first, last);
		this.removeHeaderData(this.rowHeaderData_, this.rowHeaderFlags_, this
				.getItemFromIndex(parent), first, last - first + 1);
	}

	private int sortRole_;
	private List<Map<Integer, Object>> columnHeaderData_;
	private List<Map<Integer, Object>> rowHeaderData_;
	private List<EnumSet<HeaderFlag>> columnHeaderFlags_;
	private List<EnumSet<HeaderFlag>> rowHeaderFlags_;
	private WStandardItem invisibleRootItem_;
	private WStandardItem itemPrototype_;
	private Signal1<WStandardItem> itemChanged_;

	private void init() {
		this.invisibleRootItem_ = new WStandardItem();
		this.invisibleRootItem_.model_ = this;
		this.itemPrototype_ = new WStandardItem();
	}

	private WStandardItem getItemFromIndex(WModelIndex index, boolean lazyCreate) {
		if (!(index != null)) {
			return this.invisibleRootItem_;
		} else {
			if (index.getModel() != this) {
				return null;
			} else {
				WStandardItem parent = (WStandardItem) index
						.getInternalPointer();
				WStandardItem c = parent.getChild(index.getRow(), index
						.getColumn());
				if (lazyCreate && !(c != null)) {
					c = this.getItemPrototype().clone();
					parent.setChild(index.getRow(), index.getColumn(), c);
				}
				return c;
			}
		}
	}

	private void insertHeaderData(List<Map<Integer, Object>> headerData,
			List<EnumSet<HeaderFlag>> fl, WStandardItem item, int index,
			int count) {
		if (item == this.invisibleRootItem_) {
			{
				int insertPos = 0 + index;
				for (int ii = 0; ii < count; ++ii)
					headerData.add(insertPos + ii,
							new HashMap<Integer, Object>());
			}
			;
			{
				int insertPos = 0 + index;
				for (int ii = 0; ii < count; ++ii)
					fl.add(insertPos + ii, EnumSet.noneOf(HeaderFlag.class));
			}
			;
		}
	}

	private void removeHeaderData(List<Map<Integer, Object>> headerData,
			List<EnumSet<HeaderFlag>> fl, WStandardItem item, int index,
			int count) {
		if (item == this.invisibleRootItem_) {
			for (int ii = 0; ii < (0 + index + count) - (0 + index); ++ii)
				headerData.remove(0 + index);
			;
			for (int ii = 0; ii < (0 + index + count) - (0 + index); ++ii)
				fl.remove(0 + index);
			;
		}
	}
}
