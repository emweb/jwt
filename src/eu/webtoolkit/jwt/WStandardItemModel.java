/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A standard data model, which stores its data in memory.
 *
 * <p>The standard item model supports all features of {@link WAbstractItemModel}, and can thus be
 * used to represent tables, trees and tree tables.
 *
 * <p>The data itself are organized in {@link WStandardItem} objects. There is one invisible root
 * object ({@link WStandardItemModel#getInvisibleRootItem() getInvisibleRootItem()}) that holds the
 * toplevel data. Most methods in this class that access or manipulate data internally operate on
 * this root item.
 *
 * <p>If you want to use the model as a table, then you can use {@link
 * WStandardItemModel#WStandardItemModel(int rows, int columns) WStandardItemModel()} to set the
 * initial table size, and use the {@link WStandardItemModel#getItem(int row, int column) getItem()}
 * and {@link WStandardItemModel#setItem(int row, int column, WStandardItem item) setItem()} methods
 * to set data. You can change the geometry by inserting rows (insertRow()) or columns
 * (insertColumn()) or removing rows ({@link WAbstractItemModel#removeRow(int row, WModelIndex
 * parent) WAbstractItemModel#removeRow()}) or columns ({@link WAbstractItemModel#removeColumn(int
 * column, WModelIndex parent) WAbstractItemModel#removeColumn()}).
 *
 * <p>If you want to use the model as a tree (or tree table), then you can use the default
 * constructor to start with an empty tree, and use the {@link WStandardItem} API on {@link
 * WStandardItemModel#getInvisibleRootItem() getInvisibleRootItem()} to manipulate the tree root.
 * When you are building a tree, the column count at each node is 1. When you are building a tree
 * table, you can add additional columns of data for each internal node. Only the items in the first
 * column have children that result in a hierarchical tree structure.
 *
 * <p>When using the model with a view class, you can use the {@link
 * WStandardItemModel#getItemFromIndex(WModelIndex index) getItemFromIndex()} and {@link
 * WStandardItemModel#indexFromItem(WStandardItem item) indexFromItem()} models to translate between
 * model indexes (that are used by the view class) and standard items.
 */
public class WStandardItemModel extends WAbstractItemModel {
  private static Logger logger = LoggerFactory.getLogger(WStandardItemModel.class);

  /** Creates a new standard item model. */
  public WStandardItemModel() {
    super();
    this.sortRole_ = ItemDataRole.Display;
    this.columnHeaderData_ = new ArrayList<Map<ItemDataRole, Object>>();
    this.rowHeaderData_ = new ArrayList<Map<ItemDataRole, Object>>();
    this.columnHeaderFlags_ = new ArrayList<EnumSet<HeaderFlag>>();
    this.rowHeaderFlags_ = new ArrayList<EnumSet<HeaderFlag>>();
    this.invisibleRootItem_ = null;
    this.itemPrototype_ = null;
    this.itemChanged_ = new Signal1<WStandardItem>();
    this.init();
  }
  /**
   * Creates a new standard item model with an initial geometry.
   *
   * <p>Creates a standard item model with a geometry of <i>rows</i> x <code>columns</code>. All
   * items are set to <code>null</code>.
   */
  public WStandardItemModel(int rows, int columns) {
    super();
    this.sortRole_ = ItemDataRole.Display;
    this.columnHeaderData_ = new ArrayList<Map<ItemDataRole, Object>>();
    this.rowHeaderData_ = new ArrayList<Map<ItemDataRole, Object>>();
    this.columnHeaderFlags_ = new ArrayList<EnumSet<HeaderFlag>>();
    this.rowHeaderFlags_ = new ArrayList<EnumSet<HeaderFlag>>();
    this.invisibleRootItem_ = null;
    this.itemPrototype_ = null;
    this.itemChanged_ = new Signal1<WStandardItem>();
    this.init();
    this.invisibleRootItem_.setColumnCount(columns);
    this.invisibleRootItem_.setRowCount(rows);
  }
  /**
   * Erases all data in the model.
   *
   * <p>After clearing the model, {@link WAbstractItemModel#getRowCount(WModelIndex parent)
   * WAbstractItemModel#getRowCount()} and {@link WAbstractItemModel#getColumnCount(WModelIndex
   * parent) WAbstractItemModel#getColumnCount()} are 0.
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
   *
   * <p>The invisible root item is a special item that is not rendered itself, but holds the top
   * level data.
   */
  public WStandardItem getInvisibleRootItem() {
    return this.invisibleRootItem_;
  }
  /**
   * Returns the model index for a particular item.
   *
   * <p>If the <code>item</code> is the {@link WStandardItemModel#getInvisibleRootItem()
   * getInvisibleRootItem()}, then an invalid index is returned.
   *
   * <p>
   *
   * @see WStandardItemModel#getItemFromIndex(WModelIndex index)
   */
  public WModelIndex indexFromItem(WStandardItem item) {
    if (item == this.invisibleRootItem_) {
      return null;
    } else {
      return this.createIndex(item.getRow(), item.getColumn(), item.getParent());
    }
  }
  /**
   * Returns the standard item that corresponds to a model index.
   *
   * <p>If the index is an invalid index, then the {@link WStandardItemModel#getInvisibleRootItem()
   * getInvisibleRootItem()} is returned.
   *
   * <p>
   *
   * @see WStandardItemModel#indexFromItem(WStandardItem item)
   */
  public WStandardItem getItemFromIndex(final WModelIndex index) {
    return this.getItemFromIndex(index, true);
  }
  /**
   * Adds a single column of top level items.
   *
   * <p>Appends a single column of top level <code>items</code>. If necessary, the row count is
   * increased.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * insertColumn(columnCount(), std::move(items));
   *
   * }</pre>
   *
   * <p>
   *
   * @see WStandardItemModel#appendRow(List items)
   */
  public void appendColumn(List<WStandardItem> items) {
    this.insertColumn(this.getColumnCount(), items);
  }
  /**
   * Inserts a single column of top level items.
   *
   * <p>Inserts a single column of top level <code>items</code> at column <code>column</code>. If
   * necessary, the row count is increased.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * invisibleRootItem().insertColumn(column, std::move(items));
   *
   * }</pre>
   *
   * <p>
   *
   * @see WStandardItem#insertColumn(int column, List items)
   */
  public void insertColumn(int column, List<WStandardItem> items) {
    this.invisibleRootItem_.insertColumn(column, items);
  }
  /**
   * Adds a single row of top level items.
   *
   * <p>Appends a single row of top level <code>items</code>. If necessary, the column count is
   * increased.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * insertRow(rowCount(), std::move(items));
   *
   * }</pre>
   *
   * <p>
   *
   * @see WStandardItemModel#appendColumn(List items)
   */
  public void appendRow(List<WStandardItem> items) {
    this.insertRow(this.getRowCount(), items);
  }
  /**
   * Inserts a single row of top level items.
   *
   * <p>Inserts a single row of top level <code>items</code> at row <code>row</code>. If necessary,
   * the column count is increased.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * invisibleRootItem().insertRow(row, std::move(items));
   *
   * }</pre>
   *
   * <p>
   *
   * @see WStandardItem#insertRow(int row, List items)
   */
  public void insertRow(int row, List<WStandardItem> items) {
    this.invisibleRootItem_.insertRow(row, items);
  }
  /**
   * Appends a single row containing a single item.
   *
   * <p>Appends a single toplevel row, with a single item.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * insertRow(rowCount(), std::move(item));
   *
   * }</pre>
   *
   * <p>
   *
   * @see WStandardItem#insertRow(int row, WStandardItem item)
   */
  public void appendRow(WStandardItem item) {
    this.insertRow(this.getRowCount(), item);
  }
  /**
   * Inserts a single row containing a single item.
   *
   * <p>Inserts a single toplevel row, with a single item.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * invisibleRootItem().insertRow(row, std::move(item));
   *
   * }</pre>
   *
   * <p>
   *
   * @see WStandardItem#insertRow(int row, WStandardItem item)
   */
  public void insertRow(int row, WStandardItem item) {
    this.invisibleRootItem_.insertRow(row, item);
  }
  /**
   * Returns a toplevel item.
   *
   * <p>Returns the top level at at (<i>row</i>, <code>column</code>). This may be 0 if no item was
   * set previously at that position, or if the indicated position is out of bounds.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * invisibleRootItem().child(row, column);
   *
   * }</pre>
   *
   * <p>
   *
   * @see WStandardItem#getChild(int row, int column)
   */
  public WStandardItem getItem(int row, int column) {
    return this.invisibleRootItem_.getChild(row, column);
  }
  /**
   * Returns a toplevel item.
   *
   * <p>Returns {@link #getItem(int row, int column) getItem(row, 0)}
   */
  public final WStandardItem getItem(int row) {
    return getItem(row, 0);
  }
  /**
   * Sets a toplevel item.
   *
   * <p>Sets the top level at at (<i>row</i>, <code>column</code>). If necessary, the number of rows
   * or columns is increased.
   *
   * <p>If an item was previously set for that position, it is deleted first.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * invisibleRootItem().setChild(row, column, std::move(item));
   *
   * }</pre>
   *
   * <p>
   *
   * @see WStandardItem#setChild(int row, int column, WStandardItem item)
   */
  public void setItem(int row, int column, WStandardItem item) {
    this.invisibleRootItem_.setChild(row, column, item);
  }
  /**
   * Returns the item prototype.
   *
   * <p>
   *
   * @see WStandardItemModel#setItemPrototype(WStandardItem item)
   */
  public WStandardItem getItemPrototype() {
    return this.itemPrototype_;
  }
  /**
   * Sets the item prototype.
   *
   * <p>Set the item that is cloned when an item needs to be created because the model is
   * manipulated through its {@link WAbstractItemModel} API. For example, this may be needed when a
   * view sets data at a position for which no item was previously set and thus created.
   *
   * <p>The new item is created based on this prototype by using {@link WStandardItem#clone()}.
   *
   * <p>The default prototype is WStandardItem().
   *
   * <p>
   *
   * @see WStandardItemModel#setItemPrototype(WStandardItem item)
   */
  public void setItemPrototype(WStandardItem item) {
    this.itemPrototype_ = item;
  }
  /**
   * Takes a column out of the model.
   *
   * <p>Removes a column from the model, and returns the items that it contained.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * invisibleRootItem().takeColumn(column);
   *
   * }</pre>
   *
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
   *
   * <p>Removes a row from the model, and returns the items that it contained.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * invisibleRootItem().takeRow(row);
   *
   * }</pre>
   *
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
   *
   * <p>Removes an item from the model, and returns it.
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * invisibleRootItem().takeItem(row, column);
   *
   * }</pre>
   *
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
   *
   * <p>Returns {@link #takeItem(int row, int column) takeItem(row, 0)}
   */
  public final WStandardItem takeItem(int row) {
    return takeItem(row, 0);
  }
  /**
   * Sets header flags.
   *
   * <p>By default, no flags are set.
   */
  public void setHeaderFlags(int section, Orientation orientation, EnumSet<HeaderFlag> flags) {
    final List<EnumSet<HeaderFlag>> fl =
        orientation == Orientation.Horizontal ? this.columnHeaderFlags_ : this.rowHeaderFlags_;
    fl.set(section, flags);
  }
  /**
   * Sets header flags.
   *
   * <p>Calls {@link #setHeaderFlags(int section, Orientation orientation, EnumSet flags)
   * setHeaderFlags(section, orientation, EnumSet.of(flag, flags))}
   */
  public final void setHeaderFlags(
      int section, Orientation orientation, HeaderFlag flag, HeaderFlag... flags) {
    setHeaderFlags(section, orientation, EnumSet.of(flag, flags));
  }

  public EnumSet<HeaderFlag> getHeaderFlags(int section, Orientation orientation) {
    final List<EnumSet<HeaderFlag>> fl =
        orientation == Orientation.Horizontal ? this.columnHeaderFlags_ : this.rowHeaderFlags_;
    if (section >= (int) fl.size()) {
      return EnumSet.noneOf(HeaderFlag.class);
    } else {
      return fl.get(section);
    }
  }

  public EnumSet<ItemFlag> getFlags(final WModelIndex index) {
    WStandardItem item = this.getItemFromIndex(index, false);
    return item != null ? item.getFlags() : EnumSet.noneOf(ItemFlag.class);
  }

  public WModelIndex getParent(final WModelIndex index) {
    if (!(index != null)) {
      return index;
    }
    WStandardItem parent = (WStandardItem) index.getInternalPointer();
    return this.indexFromItem(parent);
  }

  public Object getData(final WModelIndex index, ItemDataRole role) {
    WStandardItem item = this.getItemFromIndex(index, false);
    return item != null ? item.getData(role) : null;
  }

  public Object getHeaderData(int section, Orientation orientation, ItemDataRole role) {
    if (role.equals(ItemDataRole.Level)) {
      return 0;
    }
    final List<Map<ItemDataRole, Object>> headerData =
        orientation == Orientation.Horizontal ? this.columnHeaderData_ : this.rowHeaderData_;
    if (section >= (int) headerData.size()) {
      return null;
    }
    final Map<ItemDataRole, Object> d = headerData.get(section);
    Object i = d.get(role);
    if (i != null) {
      return i;
    } else {
      return null;
    }
  }

  public WModelIndex getIndex(int row, int column, final WModelIndex parent) {
    WStandardItem parentItem = this.getItemFromIndex(parent, false);
    if (parentItem != null
        && row >= 0
        && column >= 0
        && row < parentItem.getRowCount()
        && column < parentItem.getColumnCount()) {
      return this.createIndex(row, column, parentItem);
    }
    return null;
  }

  public int getColumnCount(final WModelIndex parent) {
    WStandardItem parentItem = this.getItemFromIndex(parent, false);
    return parentItem != null ? parentItem.getColumnCount() : 0;
  }

  public int getRowCount(final WModelIndex parent) {
    WStandardItem parentItem = this.getItemFromIndex(parent, false);
    return parentItem != null ? parentItem.getRowCount() : 0;
  }

  public boolean insertColumns(int column, int count, final WModelIndex parent) {
    WStandardItem parentItem = this.getItemFromIndex(parent);
    if (parentItem != null) {
      parentItem.insertColumns(column, count);
    }
    return parentItem != null;
  }

  public boolean insertRows(int row, int count, final WModelIndex parent) {
    WStandardItem parentItem = this.getItemFromIndex(parent);
    if (parentItem != null) {
      parentItem.insertRows(row, count);
    }
    return parentItem != null;
  }

  public boolean removeColumns(int column, int count, final WModelIndex parent) {
    WStandardItem parentItem = this.getItemFromIndex(parent, false);
    if (parentItem != null) {
      parentItem.removeColumns(column, count);
    }
    return parentItem != null;
  }

  public boolean removeRows(int row, int count, final WModelIndex parent) {
    WStandardItem parentItem = this.getItemFromIndex(parent, false);
    if (parentItem != null) {
      parentItem.removeRows(row, count);
    }
    return parentItem != null;
  }

  public boolean setData(final WModelIndex index, final Object value, ItemDataRole role) {
    WStandardItem item = this.getItemFromIndex(index);
    if (item != null) {
      item.setData(value, role);
    }
    return item != null;
  }

  public boolean setHeaderData(
      int section, Orientation orientation, final Object value, ItemDataRole role) {
    final List<Map<ItemDataRole, Object>> header =
        orientation == Orientation.Horizontal ? this.columnHeaderData_ : this.rowHeaderData_;
    final Map<ItemDataRole, Object> d = header.get(section);
    if (role.equals(ItemDataRole.Edit)) {
      role = ItemDataRole.Display;
    }
    d.put(role, value);
    this.headerDataChanged().trigger(orientation, section, section);
    return true;
  }

  public Object toRawIndex(final WModelIndex index) {
    return this.getItemFromIndex(index);
  }

  public WModelIndex fromRawIndex(Object rawIndex) {
    return this.indexFromItem((WStandardItem) rawIndex);
  }

  public void dropEvent(
      final WDropEvent e, DropAction action, int row, int column, final WModelIndex parent) {
    WItemSelectionModel selectionModel = ObjectUtils.cast(e.getSource(), WItemSelectionModel.class);
    if (selectionModel != null
        && selectionModel.getModel() == this
        && selectionModel.getSelectionBehavior() == SelectionBehavior.Rows
        && action == DropAction.Move) {
      SortedSet<WModelIndex> selection = selectionModel.getSelectedIndexes();
      int r = row;
      if (r < 0) {
        r = this.getRowCount(parent);
      }
      WStandardItem targetParentItem = this.getItemFromIndex(parent);
      List<List<WStandardItem>> rows = new ArrayList<List<WStandardItem>>();
      for (Iterator<WModelIndex> i_it = selection.iterator(); i_it.hasNext(); ) {
        WModelIndex i = i_it.next();
        WModelIndex sourceIndex = i;
        if ((sourceIndex.getParent() == parent
                || (sourceIndex.getParent() != null && sourceIndex.getParent().equals(parent)))
            && sourceIndex.getRow() < r) {
          r--;
        }
        WStandardItem parentItem = this.getItemFromIndex(sourceIndex.getParent());
        rows.add(parentItem.takeRow(sourceIndex.getRow()));
      }
      for (int i = 0; i < rows.size(); i++) {
        targetParentItem.insertRow(r + i, rows.get(i));
      }
    } else {
      super.dropEvent(e, action, row, column, parent);
    }
  }
  /**
   * Set the role used to sort the model.
   *
   * <p>The default role is {@link ItemDataRole#Display}.
   *
   * <p>
   *
   * @see WStandardItemModel#sort(int column, SortOrder order)
   */
  public void setSortRole(ItemDataRole role) {
    this.sortRole_ = role;
  }
  /**
   * Returns the role used to sort the model.
   *
   * <p>
   *
   * @see WStandardItemModel#setSortRole(ItemDataRole role)
   */
  public ItemDataRole getSortRole() {
    return this.sortRole_;
  }

  public void sort(int column, SortOrder order) {
    this.invisibleRootItem_.sortChildren(column, order);
  }
  /**
   * Signal emitted when an item is changed.
   *
   * <p>This signal is emitted whenever data for an item has changed. The item that has changed is
   * passed as the first parameter.
   *
   * <p>
   *
   * @see WStandardItem#setData(Object d, ItemDataRole role)
   */
  public Signal1<WStandardItem> itemChanged() {
    return this.itemChanged_;
  }

  protected void beginInsertColumns(final WModelIndex parent, int first, int last) {
    super.beginInsertColumns(parent, first, last);
    this.insertHeaderData(
        this.columnHeaderData_,
        this.columnHeaderFlags_,
        this.getItemFromIndex(parent),
        first,
        last - first + 1);
  }

  protected void beginInsertRows(final WModelIndex parent, int first, int last) {
    super.beginInsertRows(parent, first, last);
    this.insertHeaderData(
        this.rowHeaderData_,
        this.rowHeaderFlags_,
        this.getItemFromIndex(parent),
        first,
        last - first + 1);
  }

  protected void beginRemoveColumns(final WModelIndex parent, int first, int last) {
    super.beginRemoveColumns(parent, first, last);
    this.removeHeaderData(
        this.columnHeaderData_,
        this.columnHeaderFlags_,
        this.getItemFromIndex(parent),
        first,
        last - first + 1);
  }

  protected void beginRemoveRows(final WModelIndex parent, int first, int last) {
    super.beginRemoveRows(parent, first, last);
    this.removeHeaderData(
        this.rowHeaderData_,
        this.rowHeaderFlags_,
        this.getItemFromIndex(parent),
        first,
        last - first + 1);
  }

  protected void copyData(final WModelIndex sIndex, final WModelIndex dIndex) {
    if (dIndex.getModel() != this) {
      throw new WException("WStandardItemModel::copyData(): dIndex must be an index of this model");
    }
    WStandardItemModel source = ObjectUtils.cast(sIndex.getModel(), WStandardItemModel.class);
    if (source != null) {
      WStandardItem sItem = source.getItemFromIndex(sIndex);
      WStandardItem dItem = this.getItemFromIndex(dIndex);
      dItem.setFlags(sItem.getFlags());
    }
    super.copyData(sIndex, dIndex);
  }

  private ItemDataRole sortRole_;
  private List<Map<ItemDataRole, Object>> columnHeaderData_;
  private List<Map<ItemDataRole, Object>> rowHeaderData_;
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

  private WStandardItem getItemFromIndex(final WModelIndex index, boolean lazyCreate) {
    if (!(index != null)) {
      return this.invisibleRootItem_;
    } else {
      if (index.getModel() != this) {
        return null;
      } else {
        WStandardItem parent = (WStandardItem) index.getInternalPointer();
        WStandardItem c = parent.getChild(index.getRow(), index.getColumn());
        if (lazyCreate && !(c != null)) {
          WStandardItem item = this.getItemPrototype().clone();
          c = item;
          parent.setChild(index.getRow(), index.getColumn(), item);
        }
        return c;
      }
    }
  }

  private void insertHeaderData(
      final List<Map<ItemDataRole, Object>> headerData,
      final List<EnumSet<HeaderFlag>> fl,
      WStandardItem item,
      int index,
      int count) {
    if (item == this.invisibleRootItem_) {
      {
        int insertPos = 0 + index;
        for (int ii = 0; ii < (count); ++ii)
          headerData.add(insertPos + ii, new HashMap<ItemDataRole, Object>());
      }
      ;
      {
        int insertPos = 0 + index;
        for (int ii = 0; ii < (count); ++ii)
          fl.add(insertPos + ii, EnumSet.noneOf(HeaderFlag.class));
      }
      ;
    }
  }

  private void removeHeaderData(
      final List<Map<ItemDataRole, Object>> headerData,
      final List<EnumSet<HeaderFlag>> fl,
      WStandardItem item,
      int index,
      int count) {
    if (item == this.invisibleRootItem_) {
      for (int ii = 0; ii < (0 + index + count) - (0 + index); ++ii) headerData.remove(0 + index);
      ;
      for (int ii = 0; ii < (0 + index + count) - (0 + index); ++ii) fl.remove(0 + index);
      ;
    }
  }
}
