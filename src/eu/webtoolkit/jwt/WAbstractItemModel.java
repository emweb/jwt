/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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
 * An abstract model for use with JWt&apos;s view classes.
 *
 * <p>This abstract model is used by several JWt view widgets as data models.
 *
 * <p>It may model data for both tree-like and table-like view widgets. Data is therefore organized
 * in a hierarchical structure of tables, where every item stores data and items in column 0 can be
 * the parent of a nested table of data. Every data item is uniquely identified by their row, column
 * and parent index, and items may be referenced using the helper class {@link WModelIndex}.
 *
 * <p>Each item may provide data for one or more {@link ItemDataRole roles}, and indicate options
 * using {@link ItemFlag flags}. The different roles can be used to model different aspects of an
 * item (its text value, an icon, style class), or to hold auxiliary custom information. The flags
 * provide information to the View on possible interactivity.
 *
 * <p>{@link Side#Top} level data have a <code>null</code> parent {@link WModelIndex}.
 *
 * <p>The data itself is of type Object, which can either be <code>null</code>, or be any type of
 * data. Depending on the role however, view classes may expect certain types of data (e.g.
 * numerical types for charts) or will convert the data to a string (e.g. for {@link
 * ItemDataRole#Display}).
 *
 * <p>To implement a custom model, you need to reimplement the following methods:
 *
 * <ul>
 *   <li>{@link WAbstractItemModel#getIndex(int row, int column, WModelIndex parent) getIndex()} and
 *       {@link WAbstractItemModel#getParent(WModelIndex index) getParent()} methods that allow one
 *       to navigate the model
 *   <li>{@link WAbstractItemModel#getColumnCount(WModelIndex parent) getColumnCount()} and {@link
 *       WAbstractItemModel#getRowCount(WModelIndex parent) getRowCount()} to specify the top level
 *       geometry and the nested geometry at every item
 *   <li>{@link WAbstractItemModel#getData(WModelIndex index, ItemDataRole role) getData()} to
 *       return the data for an item
 *   <li>optionally, {@link WAbstractItemModel#getHeaderData(int section, Orientation orientation,
 *       ItemDataRole role) getHeaderData()} to return row and column header data
 *   <li>optionally, {@link WAbstractItemModel#getFlags(WModelIndex index) getFlags()} to indicate
 *       data options
 * </ul>
 *
 * <p>A crucial point in implementing a hierarchical model is to decide how to reference an index in
 * terms of an internal pointer ({@link WModelIndex#getInternalPointer()}). Other than the top-level
 * index, which is special since it is referenced using an invalid index, every index with children
 * must be identifiable using this object. For example, in the {@link WStandardItemModel}, the
 * internal pointer points to the parent {@link WStandardItem}. For table models, the internal
 * pointer plays no role, since only the toplevel index has children.
 *
 * <p>If you want to support editing of the model, then you need to indicate this support using a
 * {@link ItemFlag#Editable} flag, and reimplement {@link WAbstractItemModel#setData(WModelIndex
 * index, Object value, ItemDataRole role) setData()}. View classes will use the {@link
 * ItemDataRole#Edit} to read and update the data for the editor.
 *
 * <p>When the model&apos;s data has been changed, the model must emit the {@link
 * WAbstractItemModel#dataChanged() dataChanged()} signal.
 *
 * <p>Finally, there is a generic interface for insertion of new data or removal of data (changing
 * the geometry), although this interface is not yet used by any View class:
 *
 * <p>
 *
 * <ul>
 *   <li>{@link WAbstractItemModel#insertRows(int row, int count, WModelIndex parent) insertRows()}
 *   <li>{@link WAbstractItemModel#insertColumns(int column, int count, WModelIndex parent)
 *       insertColumns()}
 *   <li>{@link WAbstractItemModel#removeRows(int row, int count, WModelIndex parent) removeRows()}
 *   <li>{@link WAbstractItemModel#removeColumns(int column, int count, WModelIndex parent)
 *       removeColumns()}
 * </ul>
 *
 * <p>Alternatively, you can provide your own API for changing the model. In either case it is
 * important that you call the corresponding protected member functions which will emit the relevant
 * signals so that views can adapt themselves to the new geometry.
 */
public abstract class WAbstractItemModel extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WAbstractItemModel.class);

  /** Creates a new data model. */
  public WAbstractItemModel() {
    super();
    this.parent_ = null;
    this.columnsAboutToBeInserted_ = new Signal3<WModelIndex, Integer, Integer>();
    this.columnsAboutToBeRemoved_ = new Signal3<WModelIndex, Integer, Integer>();
    this.columnsInserted_ = new Signal3<WModelIndex, Integer, Integer>();
    this.columnsRemoved_ = new Signal3<WModelIndex, Integer, Integer>();
    this.rowsAboutToBeInserted_ = new Signal3<WModelIndex, Integer, Integer>();
    this.rowsAboutToBeRemoved_ = new Signal3<WModelIndex, Integer, Integer>();
    this.rowsInserted_ = new Signal3<WModelIndex, Integer, Integer>();
    this.rowsRemoved_ = new Signal3<WModelIndex, Integer, Integer>();
    this.dataChanged_ = new Signal2<WModelIndex, WModelIndex>();
    this.headerDataChanged_ = new Signal3<Orientation, Integer, Integer>();
    this.layoutAboutToBeChanged_ = new Signal();
    this.layoutChanged_ = new Signal();
    this.modelReset_ = new Signal();
  }
  /**
   * Returns the number of columns.
   *
   * <p>This returns the number of columns at index <code>parent</code>.
   *
   * <p>
   *
   * @see WAbstractItemModel#getRowCount(WModelIndex parent)
   */
  public abstract int getColumnCount(final WModelIndex parent);
  /**
   * Returns the number of columns.
   *
   * <p>Returns {@link #getColumnCount(WModelIndex parent) getColumnCount(null)}
   */
  public final int getColumnCount() {
    return getColumnCount(null);
  }
  /**
   * Returns the number of rows.
   *
   * <p>This returns the number of rows at index <code>parent</code>.
   *
   * <p>
   *
   * @see WAbstractItemModel#getColumnCount(WModelIndex parent)
   */
  public abstract int getRowCount(final WModelIndex parent);
  /**
   * Returns the number of rows.
   *
   * <p>Returns {@link #getRowCount(WModelIndex parent) getRowCount(null)}
   */
  public final int getRowCount() {
    return getRowCount(null);
  }

  boolean canFetchMore(final WModelIndex parent) {
    return false;
  }

  void fetchMore(final WModelIndex parent) {}
  /**
   * Returns the flags for an item.
   *
   * <p>The default implementation returns {@link ItemFlag#Selectable}.
   *
   * <p>
   *
   * @see ItemFlag
   */
  public EnumSet<ItemFlag> getFlags(final WModelIndex index) {
    return EnumSet.of(ItemFlag.Selectable);
  }
  /**
   * Returns the flags for a header.
   *
   * <p>The default implementation returns no flags set.
   *
   * <p>
   *
   * @see HeaderFlag
   */
  public EnumSet<HeaderFlag> getHeaderFlags(int section, Orientation orientation) {
    return EnumSet.noneOf(HeaderFlag.class);
  }
  /**
   * Returns the flags for a header.
   *
   * <p>Returns {@link #getHeaderFlags(int section, Orientation orientation) getHeaderFlags(section,
   * Orientation.Horizontal)}
   */
  public final EnumSet<HeaderFlag> getHeaderFlags(int section) {
    return getHeaderFlags(section, Orientation.Horizontal);
  }
  /**
   * Returns if there are children at an index.
   *
   * <p>Returns <code>true</code> when rowCount(index) &gt; 0 and columnCount(index) &gt; 0.
   *
   * <p>
   *
   * @see WAbstractItemModel#getRowCount(WModelIndex parent)
   * @see WAbstractItemModel#getColumnCount(WModelIndex parent)
   */
  public boolean hasChildren(final WModelIndex index) {
    return this.getRowCount(index) > 0 && this.getColumnCount(index) > 0;
  }
  /**
   * Returns the parent for a model index.
   *
   * <p>An implementation should use {@link WAbstractItemModel#createIndex(int row, int column,
   * Object ptr) createIndex()} to create a model index that corresponds to the parent of a given
   * index.
   *
   * <p>Note that the index itself may be stale (referencing a row/column within the parent that is
   * outside the model geometry), but its parent (identified by the {@link
   * WModelIndex#getInternalPointer()}) is referencing an existing parent. A stale index can only be
   * used while the model geometry is being updated, i.e. during the emission of the corresponding
   * [rows/columns](Being)[Removed/Inserted]() signals.
   *
   * <p>
   *
   * @see WAbstractItemModel#getIndex(int row, int column, WModelIndex parent)
   */
  public abstract WModelIndex getParent(final WModelIndex index);
  /**
   * Returns data at a specified model index for the given role.
   *
   * <p>You should check the <code>role</code> to decide what data to return. Usually a View class
   * will ask for data for several roles which affect not only the contents ({@link
   * ItemDataRole#Display}) but also icons ({@link ItemDataRole#Decoration}), URLs ({@link
   * ItemDataRole#Link}), and other visual aspects. If your item does not specify data for a
   * particular role, it should simply return a Wt::cpp17::any().
   *
   * <p>
   *
   * @see WAbstractItemModel#getFlags(WModelIndex index)
   * @see WAbstractItemModel#getHeaderData(int section, Orientation orientation, ItemDataRole role)
   * @see WAbstractItemModel#setData(WModelIndex index, Object value, ItemDataRole role)
   */
  public abstract Object getData(final WModelIndex index, ItemDataRole role);
  /**
   * Returns data at a specified model index for the given role.
   *
   * <p>Returns {@link #getData(WModelIndex index, ItemDataRole role) getData(index,
   * ItemDataRole.Display)}
   */
  public final Object getData(final WModelIndex index) {
    return getData(index, ItemDataRole.Display);
  }
  /**
   * Returns all data at a specific index.
   *
   * <p>This is a convenience function that returns a map with data corresponding to all standard
   * roles.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public SortedMap<ItemDataRole, Object> getItemData(final WModelIndex index) {
    SortedMap<ItemDataRole, Object> result = new TreeMap<ItemDataRole, Object>();
    if ((index != null)) {
      for (int i = 0; i <= ItemDataRole.BarBrushColor.getValue(); ++i) {
        result.put(ItemDataRole.of(i), this.getData(index, ItemDataRole.of(i)));
      }
      result.put(ItemDataRole.User, this.getData(index, ItemDataRole.User));
    }
    return result;
  }
  /**
   * Returns the row or column header data.
   *
   * <p>When <code>orientation</code> is {@link Orientation#Horizontal}, <code>section</code> is a
   * column number, when <code>orientation</code> is {@link Orientation#Vertical}, <code>section
   * </code> is a row number.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   * @see WAbstractItemModel#setHeaderData(int section, Orientation orientation, Object value,
   *     ItemDataRole role)
   */
  public Object getHeaderData(int section, Orientation orientation, ItemDataRole role) {
    if (role.equals(ItemDataRole.Level)) {
      return (int) 0;
    } else {
      return null;
    }
  }
  /**
   * Returns the row or column header data.
   *
   * <p>Returns {@link #getHeaderData(int section, Orientation orientation, ItemDataRole role)
   * getHeaderData(section, Orientation.Horizontal, ItemDataRole.Display)}
   */
  public final Object getHeaderData(int section) {
    return getHeaderData(section, Orientation.Horizontal, ItemDataRole.Display);
  }
  /**
   * Returns the row or column header data.
   *
   * <p>Returns {@link #getHeaderData(int section, Orientation orientation, ItemDataRole role)
   * getHeaderData(section, orientation, ItemDataRole.Display)}
   */
  public final Object getHeaderData(int section, Orientation orientation) {
    return getHeaderData(section, orientation, ItemDataRole.Display);
  }
  /**
   * Returns the child index for the given row and column.
   *
   * <p>When implementing this method, you can use {@link WAbstractItemModel#createIndex(int row,
   * int column, Object ptr) createIndex()} to create an index that corresponds to the item at
   * <code>row</code> and <code>column</code> within <code>parent</code>.
   *
   * <p>If the location is invalid (out of bounds at the parent), then an invalid index must be
   * returned.
   *
   * <p>
   *
   * @see WAbstractItemModel#getParent(WModelIndex index)
   */
  public abstract WModelIndex getIndex(int row, int column, final WModelIndex parent);
  /**
   * Returns the child index for the given row and column.
   *
   * <p>Returns {@link #getIndex(int row, int column, WModelIndex parent) getIndex(row, column,
   * null)}
   */
  public final WModelIndex getIndex(int row, int column) {
    return getIndex(row, column, null);
  }
  /**
   * Returns an index list for data items that match.
   *
   * <p>Returns an index list of data items that match, starting at start, and searching further in
   * that column. If flags specifies {@link MatchOptions.MatchFlag#Wrap MatchFlag#Wrap} then the
   * search wraps around from the start. If hits is not -1, then at most that number of hits are
   * returned.
   */
  public List<WModelIndex> match(
      final WModelIndex start,
      ItemDataRole role,
      final Object value,
      int hits,
      MatchOptions flags) {
    List<WModelIndex> result = new ArrayList<WModelIndex>();
    final int rc = this.getRowCount(start.getParent());
    for (int i = 0; i < rc; ++i) {
      int row = start.getRow() + i;
      if (row >= rc) {
        if (!!EnumUtils.mask(flags.getFlags(), MatchOptions.MatchFlag.Wrap).isEmpty()) {
          break;
        } else {
          row -= rc;
        }
      }
      WModelIndex idx = this.getIndex(row, start.getColumn(), start.getParent());
      Object v = this.getData(idx, role);
      if (StringUtils.matchValue(v, value, flags)) {
        result.add(idx);
        if (hits != -1 && (int) result.size() == hits) {
          break;
        }
      }
    }
    return result;
  }
  /**
   * Returns the data item at the given column and row.
   *
   * <p>This is a convenience method, and is equivalent to:
   *
   * <pre>{@code
   * index(row, column, parent).data(role)
   *
   * }</pre>
   *
   * <p>
   *
   * @see WAbstractItemModel#getIndex(int row, int column, WModelIndex parent)
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public Object getData(int row, int column, ItemDataRole role, final WModelIndex parent) {
    return this.getData(this.getIndex(row, column, parent), role);
  }
  /**
   * Returns the data item at the given column and row.
   *
   * <p>Returns {@link #getData(int row, int column, ItemDataRole role, WModelIndex parent)
   * getData(row, column, ItemDataRole.Display, null)}
   */
  public final Object getData(int row, int column) {
    return getData(row, column, ItemDataRole.Display, null);
  }
  /**
   * Returns the data item at the given column and row.
   *
   * <p>Returns {@link #getData(int row, int column, ItemDataRole role, WModelIndex parent)
   * getData(row, column, role, null)}
   */
  public final Object getData(int row, int column, ItemDataRole role) {
    return getData(row, column, role, null);
  }
  /**
   * Returns if an index at the given position is valid (i.e. falls within the column-row bounds).
   *
   * <p>Equivalent to:
   *
   * <pre>{@code
   * return row >= 0 && column >= 0
   * && row < rowCount(parent) && column < columnCount(parent);
   *
   * }</pre>
   *
   * <p>
   *
   * @see WAbstractItemModel#getRowCount(WModelIndex parent)
   * @see WAbstractItemModel#getColumnCount(WModelIndex parent)
   */
  public boolean hasIndex(int row, int column, final WModelIndex parent) {
    return row >= 0
        && column >= 0
        && row < this.getRowCount(parent)
        && column < this.getColumnCount(parent);
  }
  /**
   * Returns if an index at the given position is valid (i.e. falls within the column-row bounds).
   *
   * <p>Returns {@link #hasIndex(int row, int column, WModelIndex parent) hasIndex(row, column,
   * null)}
   */
  public final boolean hasIndex(int row, int column) {
    return hasIndex(row, column, null);
  }
  /**
   * Inserts one or more columns.
   *
   * <p>In models that support column insertion, this inserts <code>count</code> columns, starting
   * at <code>column</code>, and returns <code>true</code> if the operation was successful. The new
   * columns are inserted under <code>parent</code>.
   *
   * <p>The default implementation returns <code>false</code>.
   *
   * <p>The model implementation must call {@link WAbstractItemModel#beginInsertColumns(WModelIndex
   * parent, int first, int last) beginInsertColumns()} and {@link
   * WAbstractItemModel#endInsertColumns() endInsertColumns()} before and after the operation
   * whenever its geometry is changed by inserting columns. This emits signals for views to properly
   * react to these changes.
   *
   * <p>
   *
   * @see WAbstractItemModel#insertRows(int row, int count, WModelIndex parent)
   * @see WAbstractItemModel#removeColumns(int column, int count, WModelIndex parent)
   * @see WAbstractItemModel#beginInsertColumns(WModelIndex parent, int first, int last)
   * @see WAbstractItemModel#endInsertColumns()
   */
  public boolean insertColumns(int column, int count, final WModelIndex parent) {
    return false;
  }
  /**
   * Inserts one or more columns.
   *
   * <p>Returns {@link #insertColumns(int column, int count, WModelIndex parent)
   * insertColumns(column, count, null)}
   */
  public final boolean insertColumns(int column, int count) {
    return insertColumns(column, count, null);
  }
  /**
   * Inserts one or more rows.
   *
   * <p>In models that support row insertion, this inserts <code>count</code> rows, starting at
   * <code>row</code>, and returns <code>true</code> if the operation was successful. The new rows
   * are inserted under <code>parent</code>.
   *
   * <p>If parent had no children, then a single column is added with <code>count</code> rows.
   *
   * <p>The default implementation returns <code>false</code>.
   *
   * <p>The model implementation must call {@link WAbstractItemModel#beginInsertRows(WModelIndex
   * parent, int first, int last) beginInsertRows()} and {@link WAbstractItemModel#endInsertRows()
   * endInsertRows()} before and after the operation whenever its geometry is changed by inserting
   * rows. This emits signals for views to properly react to these changes.
   *
   * <p>
   *
   * @see WAbstractItemModel#insertColumns(int column, int count, WModelIndex parent)
   * @see WAbstractItemModel#removeRows(int row, int count, WModelIndex parent)
   * @see WAbstractItemModel#beginInsertRows(WModelIndex parent, int first, int last)
   * @see WAbstractItemModel#endInsertRows()
   */
  public boolean insertRows(int row, int count, final WModelIndex parent) {
    return false;
  }
  /**
   * Inserts one or more rows.
   *
   * <p>Returns {@link #insertRows(int row, int count, WModelIndex parent) insertRows(row, count,
   * null)}
   */
  public final boolean insertRows(int row, int count) {
    return insertRows(row, count, null);
  }
  /**
   * Removes columns.
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>The default implementation returns <code>false</code>.
   *
   * <p>The model implementation must call {@link WAbstractItemModel#beginRemoveColumns(WModelIndex
   * parent, int first, int last) beginRemoveColumns()} and {@link
   * WAbstractItemModel#endRemoveColumns() endRemoveColumns()} before and after the operation
   * whenever its geometry is changed by removing columns. This emits signals for views to properly
   * react to these changes.
   *
   * <p>
   *
   * @see WAbstractItemModel#removeRows(int row, int count, WModelIndex parent)
   * @see WAbstractItemModel#insertColumns(int column, int count, WModelIndex parent)
   * @see WAbstractItemModel#beginRemoveColumns(WModelIndex parent, int first, int last)
   * @see WAbstractItemModel#endRemoveColumns()
   */
  public boolean removeColumns(int column, int count, final WModelIndex parent) {
    return false;
  }
  /**
   * Removes columns.
   *
   * <p>Returns {@link #removeColumns(int column, int count, WModelIndex parent)
   * removeColumns(column, count, null)}
   */
  public final boolean removeColumns(int column, int count) {
    return removeColumns(column, count, null);
  }
  /**
   * Removes rows.
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>The default implementation returns <code>false</code>.
   *
   * <p>The model implementation must call {@link WAbstractItemModel#beginRemoveRows(WModelIndex
   * parent, int first, int last) beginRemoveRows()} and {@link WAbstractItemModel#endRemoveRows()
   * endRemoveRows()} before and after the operation whenever its geometry is changed by removing
   * rows. This emits signals for views to properly react to these changes.
   *
   * <p>
   *
   * @see WAbstractItemModel#removeColumns(int column, int count, WModelIndex parent)
   * @see WAbstractItemModel#insertRows(int row, int count, WModelIndex parent)
   * @see WAbstractItemModel#beginRemoveRows(WModelIndex parent, int first, int last)
   * @see WAbstractItemModel#endRemoveRows()
   */
  public boolean removeRows(int row, int count, final WModelIndex parent) {
    return false;
  }
  /**
   * Removes rows.
   *
   * <p>Returns {@link #removeRows(int row, int count, WModelIndex parent) removeRows(row, count,
   * null)}
   */
  public final boolean removeRows(int row, int count) {
    return removeRows(row, count, null);
  }
  /**
   * Sets data at the given model index.
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>The default implementation returns <code>false</code>.
   *
   * <p>The model implementation must emit the {@link WAbstractItemModel#dataChanged()
   * dataChanged()} signal after data was changed.
   *
   * <p>
   *
   * @see WAbstractItemModel#getData(WModelIndex index, ItemDataRole role)
   */
  public boolean setData(final WModelIndex index, final Object value, ItemDataRole role) {
    return false;
  }
  /**
   * Sets data at the given model index.
   *
   * <p>Returns {@link #setData(WModelIndex index, Object value, ItemDataRole role) setData(index,
   * value, ItemDataRole.Edit)}
   */
  public final boolean setData(final WModelIndex index, final Object value) {
    return setData(index, value, ItemDataRole.Edit);
  }
  /**
   * Sets data at the given model index.
   *
   * <p>This is a convenience function that sets data for all roles at once.
   *
   * <p>
   *
   * @see WAbstractItemModel#setData(WModelIndex index, Object value, ItemDataRole role)
   */
  public boolean setItemData(
      final WModelIndex index, final SortedMap<ItemDataRole, Object> values) {
    boolean result = true;
    for (Iterator<Map.Entry<ItemDataRole, Object>> i_it = values.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<ItemDataRole, Object> i = i_it.next();
      if (!this.setData(index, i.getValue(), i.getKey())) {
        result = false;
      }
    }
    this.dataChanged().trigger(index, index);
    return result;
  }
  /**
   * Sets header data for a column or row.
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>
   *
   * @see WAbstractItemModel#getHeaderData(int section, Orientation orientation, ItemDataRole role)
   */
  public boolean setHeaderData(
      int section, Orientation orientation, final Object value, ItemDataRole role) {
    return false;
  }
  /**
   * Sets header data for a column or row.
   *
   * <p>Returns {@link #setHeaderData(int section, Orientation orientation, Object value,
   * ItemDataRole role) setHeaderData(section, orientation, value, ItemDataRole.Edit)}
   */
  public final boolean setHeaderData(int section, Orientation orientation, final Object value) {
    return setHeaderData(section, orientation, value, ItemDataRole.Edit);
  }
  /**
   * Sets column header data.
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>
   */
  public boolean setHeaderData(int section, final Object value) {
    return this.setHeaderData(section, Orientation.Horizontal, value);
  }
  /**
   * Sorts the model according to a particular column.
   *
   * <p>If the model supports sorting, then it should emit the {@link
   * WAbstractItemModel#layoutAboutToBeChanged() layoutAboutToBeChanged()} signal, rearrange its
   * items, and afterwards emit the {@link WAbstractItemModel#layoutChanged() layoutChanged()}
   * signal.
   *
   * <p>
   *
   * @see WAbstractItemModel#layoutAboutToBeChanged()
   * @see WAbstractItemModel#layoutChanged()
   */
  public void sort(int column, SortOrder order) {}
  /**
   * Sorts the model according to a particular column.
   *
   * <p>Calls {@link #sort(int column, SortOrder order) sort(column, SortOrder.Ascending)}
   */
  public final void sort(int column) {
    sort(column, SortOrder.Ascending);
  }
  /**
   * Expands a column.
   *
   * <p>Expands a column. This may only be called by a view when the {@link
   * HeaderFlag#ColumnIsCollapsed} flag is set.
   *
   * <p>The default implementation does nothing.
   *
   * <p>
   *
   * @see WAggregateProxyModel
   */
  public void expandColumn(int column) {}
  /**
   * Collapses a column.
   *
   * <p>Collapses a column. This may only be called by a view when the {@link
   * HeaderFlag#ColumnIsExpandedLeft} or {@link HeaderFlag#ColumnIsExpandedRight} flag is set.
   *
   * <p>The default implementation does nothing.
   *
   * <p>
   *
   * @see WAggregateProxyModel
   */
  public void collapseColumn(int column) {}
  /**
   * Converts a model index to a raw pointer that remains valid while the model&apos;s layout is
   * changed.
   *
   * <p>Use this method to temporarily save model indexes while the model&apos;s layout is changed
   * by for example a sorting operation.
   *
   * <p>The default implementation returns <code>null</code>, which indicates that the index cannot
   * be converted to a raw pointer. If you reimplement this method, you also need to reimplemnt
   * {@link WAbstractItemModel#fromRawIndex(Object rawIndex) fromRawIndex()}.
   *
   * <p>
   *
   * @see WAbstractItemModel#layoutAboutToBeChanged()
   * @see WAbstractItemModel#sort(int column, SortOrder order)
   * @see WAbstractItemModel#fromRawIndex(Object rawIndex)
   */
  public Object toRawIndex(final WModelIndex index) {
    return null;
  }
  /**
   * Converts a raw pointer to a model index.
   *
   * <p>Use this method to create model index from temporary raw pointers. It is the reciproce
   * method of {@link WAbstractItemModel#toRawIndex(WModelIndex index) toRawIndex()}.
   *
   * <p>You can return an invalid modelindex if the rawIndex no longer points to a valid item
   * because of the layout change.
   *
   * <p>
   *
   * @see WAbstractItemModel#toRawIndex(WModelIndex index)
   */
  public WModelIndex fromRawIndex(Object rawIndex) {
    return null;
  }
  /**
   * Returns a mime-type for dragging a set of indexes.
   *
   * <p>This method returns a mime-type that describes dragging of a selection of items.
   *
   * <p>The drop event will indicate a {@link WItemSelectionModel selection model} for this abstract
   * item model as {@link WDropEvent#getSource()}.
   *
   * <p>The default implementation returns a mime-type for generic drag&amp;drop support between
   * abstract item models.
   *
   * <p>
   *
   * @see WAbstractItemModel#getAcceptDropMimeTypes()
   */
  public String getMimeType() {
    return DRAG_DROP_MIME_TYPE;
  }
  /**
   * Returns a list of mime-types that could be accepted for a drop event.
   *
   * <p>The default implementation only accepts drag&amp;drop support between abstract item models.
   *
   * <p>
   *
   * @see WAbstractItemModel#getMimeType()
   */
  public List<String> getAcceptDropMimeTypes() {
    List<String> result = new ArrayList<String>();
    result.add(DRAG_DROP_MIME_TYPE);
    return result;
  }
  /**
   * Handles a drop event.
   *
   * <p>The default implementation only handles generic drag&amp;drop between abstract item models.
   * Source item data is copied (but not the source item&apos;s flags).
   *
   * <p>This method is overloaded for handling drop events on top of items or drop events between
   * items (see {@link DropLocation}). This overload handles drops on top of items, but note that
   * due to historical reasons it will also insert the items in between when called with {@link
   * DropAction#Move}.
   *
   * <p>The location in the model is indicated by the <code>row</code> and <code>column</code>
   * within the <code>parent</code> index. If <code>row</code> is -1, then the item is appended to
   * the <code>parent</code>. Otherwise, the item is inserted at or copied over the indicated item
   * (and subsequent rows). When <code>action</code> is a {@link DropAction#Move}, the original
   * items are deleted from the source model.
   *
   * <p>You may want to reimplement this method if you want to handle other mime-type data, or if
   * you want to refine how the drop event of an item selection must be interpreted.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Currently, only row selections are handled by the default implementation.
   * </i>
   *
   * @see WAbstractItemModel#getMimeType()
   * @see WItemSelectionModel
   */
  public void dropEvent(
      final WDropEvent e, DropAction action, int row, int column, final WModelIndex parent) {
    WItemSelectionModel selectionModel = ObjectUtils.cast(e.getSource(), WItemSelectionModel.class);
    if (selectionModel != null) {
      WAbstractItemModel sourceModel = selectionModel.getModel();
      if (action == DropAction.Move || row == -1) {
        if (row == -1) {
          row = this.getRowCount(parent);
        }
        if (!this.insertRows(row, selectionModel.getSelectedIndexes().size(), parent)) {
          logger.error(new StringWriter().append("dropEvent(): could not insertRows()").toString());
          return;
        }
      }
      SortedSet<WModelIndex> selection = selectionModel.getSelectedIndexes();
      int r = row;
      for (Iterator<WModelIndex> i_it = selection.iterator(); i_it.hasNext(); ) {
        WModelIndex i = i_it.next();
        WModelIndex sourceIndex = i;
        if (selectionModel.getSelectionBehavior() == SelectionBehavior.Rows) {
          WModelIndex sourceParent = sourceIndex.getParent();
          for (int col = 0; col < sourceModel.getColumnCount(sourceParent); ++col) {
            WModelIndex s = sourceModel.getIndex(sourceIndex.getRow(), col, sourceParent);
            WModelIndex d = this.getIndex(r, col, parent);
            this.copyData(s, d);
          }
          ++r;
        }
      }
      if (action == DropAction.Move) {
        while (!selectionModel.getSelectedIndexes().isEmpty()) {
          WModelIndex i = selectionModel.getSelectedIndexes().last();
          if (!sourceModel.removeRow(i.getRow(), i.getParent())) {
            logger.error(
                new StringWriter().append("dropEvent(): could not removeRows()").toString());
            return;
          }
        }
      }
    }
  }
  /**
   * Handles a drop event.
   *
   * <p>The default implementation only handles generic drag&amp;drop between abstract item models.
   * Source item data is copied (but not the source item&apos;s flags).
   *
   * <p>This method is overloaded for handling drop events on top of items or drop events between
   * items. This overload handles drops between items. The drop was received relative to the <code>
   * index</code> item and the <code>side</code> parameter will only be Wt::Top or Wt::Bottom.
   *
   * <p>You may want to reimplement this method if you want to handle other mime-type data, or if
   * you want to refine how the drop event of an item selection must be interpreted.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Currently, only row selections are handled by the default implementation.
   * </i>
   *
   * @see WAbstractItemModel#getMimeType()
   * @see WItemSelectionModel
   */
  public void dropEvent(
      final WDropEvent e, DropAction action, final WModelIndex pindex, Side side) {
    WItemSelectionModel selectionModel = ObjectUtils.cast(e.getSource(), WItemSelectionModel.class);
    if (selectionModel != null) {
      WAbstractItemModel sourceModel = selectionModel.getModel();
      final WModelIndex parent = pindex.getParent();
      int row =
          !(pindex != null)
              ? this.getRowCount()
              : side == Side.Bottom ? pindex.getRow() + 1 : pindex.getRow();
      if (!this.insertRows(row, selectionModel.getSelectedIndexes().size(), parent)) {
        logger.error(new StringWriter().append("dropEvent(): could not insertRows()").toString());
        return;
      }
      SortedSet<WModelIndex> selection = selectionModel.getSelectedIndexes();
      int r = row;
      for (Iterator<WModelIndex> i_it = selection.iterator(); i_it.hasNext(); ) {
        WModelIndex i = i_it.next();
        WModelIndex sourceIndex = i;
        if (selectionModel.getSelectionBehavior() == SelectionBehavior.Rows) {
          WModelIndex sourceParent = sourceIndex.getParent();
          for (int col = 0; col < sourceModel.getColumnCount(sourceParent); ++col) {
            WModelIndex s = sourceModel.getIndex(sourceIndex.getRow(), col, sourceParent);
            WModelIndex d = this.getIndex(r, col, parent);
            this.copyData(s, d);
          }
          ++r;
        }
      }
      if (action == DropAction.Move) {
        while (!selectionModel.getSelectedIndexes().isEmpty()) {
          WModelIndex i = selectionModel.getSelectedIndexes().last();
          if (!sourceModel.removeRow(i.getRow(), i.getParent())) {
            logger.error(
                new StringWriter().append("dropEvent(): could not removeRows()").toString());
            return;
          }
        }
      }
    }
  }
  /**
   * Inserts one column.
   *
   * <p>This is a convenience method that adds a single column, and is equivalent to:
   *
   * <pre>{@code
   * insertColumns(column, 1, parent);
   *
   * }</pre>
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>
   *
   * @see WAbstractItemModel#insertColumns(int column, int count, WModelIndex parent)
   */
  public boolean insertColumn(int column, final WModelIndex parent) {
    return this.insertColumns(column, 1, parent);
  }
  /**
   * Inserts one column.
   *
   * <p>Returns {@link #insertColumn(int column, WModelIndex parent) insertColumn(column, null)}
   */
  public final boolean insertColumn(int column) {
    return insertColumn(column, null);
  }
  /**
   * Inserts one row.
   *
   * <p>This is a convenience method that adds a single row, and is equivalent to:
   *
   * <pre>{@code
   * insertRows(row, 1, parent);
   *
   * }</pre>
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>
   *
   * @see WAbstractItemModel#insertRows(int row, int count, WModelIndex parent)
   */
  public boolean insertRow(int row, final WModelIndex parent) {
    return this.insertRows(row, 1, parent);
  }
  /**
   * Inserts one row.
   *
   * <p>Returns {@link #insertRow(int row, WModelIndex parent) insertRow(row, null)}
   */
  public final boolean insertRow(int row) {
    return insertRow(row, null);
  }
  /**
   * Removes one column.
   *
   * <p>This is a convenience method that removes a single column, and is equivalent to:
   *
   * <pre>{@code
   * removeColumns(column, 1, parent);
   *
   * }</pre>
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>
   *
   * @see WAbstractItemModel#removeColumns(int column, int count, WModelIndex parent)
   */
  public boolean removeColumn(int column, final WModelIndex parent) {
    return this.removeColumns(column, 1, parent);
  }
  /**
   * Removes one column.
   *
   * <p>Returns {@link #removeColumn(int column, WModelIndex parent) removeColumn(column, null)}
   */
  public final boolean removeColumn(int column) {
    return removeColumn(column, null);
  }
  /**
   * Removes one row.
   *
   * <p>This is a convenience method that removes a single row, and is equivalent to:
   *
   * <pre>{@code
   * removeRows(row, 1, parent);
   *
   * }</pre>
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>
   *
   * @see WAbstractItemModel#removeRows(int row, int count, WModelIndex parent)
   */
  public boolean removeRow(int row, final WModelIndex parent) {
    return this.removeRows(row, 1, parent);
  }
  /**
   * Removes one row.
   *
   * <p>Returns {@link #removeRow(int row, WModelIndex parent) removeRow(row, null)}
   */
  public final boolean removeRow(int row) {
    return removeRow(row, null);
  }
  /**
   * Sets data at the given row and column.
   *
   * <p>This is a convience method, and is equivalent to:
   *
   * <pre>{@code
   * setData(index(row, column, parent), value, role);
   *
   * }</pre>
   *
   * <p>Returns <code>true</code> if the operation was successful.
   *
   * <p>
   *
   * @see WAbstractItemModel#setData(WModelIndex index, Object value, ItemDataRole role)
   * @see WAbstractItemModel#getIndex(int row, int column, WModelIndex parent)
   */
  public boolean setData(
      int row, int column, final Object value, ItemDataRole role, final WModelIndex parent) {
    WModelIndex i = this.getIndex(row, column, parent);
    if ((i != null)) {
      return this.setData(i, value, role);
    } else {
      return false;
    }
  }
  /**
   * Sets data at the given row and column.
   *
   * <p>Returns {@link #setData(int row, int column, Object value, ItemDataRole role, WModelIndex
   * parent) setData(row, column, value, ItemDataRole.Edit, null)}
   */
  public final boolean setData(int row, int column, final Object value) {
    return setData(row, column, value, ItemDataRole.Edit, null);
  }
  /**
   * Sets data at the given row and column.
   *
   * <p>Returns {@link #setData(int row, int column, Object value, ItemDataRole role, WModelIndex
   * parent) setData(row, column, value, role, null)}
   */
  public final boolean setData(int row, int column, final Object value, ItemDataRole role) {
    return setData(row, column, value, role, null);
  }
  /**
   * Signal emitted before a number of columns will be inserted.
   *
   * <p>The first argument is the parent index. The two integer arguments are the column numbers
   * that the first and last column will have when inserted.
   *
   * <p>
   *
   * @see WAbstractItemModel#columnsInserted()
   * @see WAbstractItemModel#beginInsertColumns(WModelIndex parent, int first, int last)
   */
  public Signal3<WModelIndex, Integer, Integer> columnsAboutToBeInserted() {
    return this.columnsAboutToBeInserted_;
  }
  /**
   * Signal emitted before a number of columns will be removed.
   *
   * <p>The first argument is the parent index. The two integer arguments are the column numbers of
   * the first and last column that will be removed.
   *
   * <p>
   *
   * @see WAbstractItemModel#columnsRemoved()
   * @see WAbstractItemModel#beginRemoveColumns(WModelIndex parent, int first, int last)
   */
  public Signal3<WModelIndex, Integer, Integer> columnsAboutToBeRemoved() {
    return this.columnsAboutToBeRemoved_;
  }
  /**
   * Signal emitted after a number of columns were inserted.
   *
   * <p>The first argument is the parent index. The two integer arguments are the column numbers of
   * the first and last column that were inserted.
   *
   * <p>
   *
   * @see WAbstractItemModel#columnsAboutToBeInserted()
   * @see WAbstractItemModel#endInsertColumns()
   */
  public Signal3<WModelIndex, Integer, Integer> columnsInserted() {
    return this.columnsInserted_;
  }
  /**
   * Signal emitted after a number of columns were removed.
   *
   * <p>The first argument is the parent index. The two integer arguments are the column numbers of
   * the first and last column that were removed.
   *
   * <p>
   *
   * @see WAbstractItemModel#columnsAboutToBeRemoved()
   * @see WAbstractItemModel#endRemoveColumns()
   */
  public Signal3<WModelIndex, Integer, Integer> columnsRemoved() {
    return this.columnsRemoved_;
  }
  /**
   * Signal emitted before a number of rows will be inserted.
   *
   * <p>The first argument is the parent index. The two integer arguments are the row numbers that
   * the first and last row will have when inserted.
   *
   * <p>
   *
   * @see WAbstractItemModel#rowsInserted()
   * @see WAbstractItemModel#beginInsertRows(WModelIndex parent, int first, int last)
   */
  public Signal3<WModelIndex, Integer, Integer> rowsAboutToBeInserted() {
    return this.rowsAboutToBeInserted_;
  }
  /**
   * Signal emitted before a number of rows will be removed.
   *
   * <p>The first argument is the parent index. The two integer arguments are the row numbers of the
   * first and last row that will be removed.
   *
   * <p>
   *
   * @see WAbstractItemModel#rowsRemoved()
   * @see WAbstractItemModel#beginRemoveRows(WModelIndex parent, int first, int last)
   */
  public Signal3<WModelIndex, Integer, Integer> rowsAboutToBeRemoved() {
    return this.rowsAboutToBeRemoved_;
  }
  /**
   * Signal emitted after a number of rows were inserted.
   *
   * <p>The first argument is the parent index. The two integer arguments are the row numbers of the
   * first and last row that were inserted.
   *
   * <p>
   *
   * @see WAbstractItemModel#rowsAboutToBeInserted()
   * @see WAbstractItemModel#endInsertRows()
   */
  public Signal3<WModelIndex, Integer, Integer> rowsInserted() {
    return this.rowsInserted_;
  }
  /**
   * Signal emitted after a number of rows were removed.
   *
   * <p>The first argument is the parent index. The two integer arguments are the row numbers of the
   * first and last row that were removed.
   *
   * <p>
   *
   * @see WAbstractItemModel#rowsAboutToBeRemoved()
   * @see WAbstractItemModel#endRemoveRows()
   */
  public Signal3<WModelIndex, Integer, Integer> rowsRemoved() {
    return this.rowsRemoved_;
  }
  /**
   * Signal emitted when some data was changed.
   *
   * <p>The two arguments are the model indexes of the top-left and bottom-right data items that
   * span the rectangle of changed data items.
   *
   * <p>
   *
   * @see WAbstractItemModel#setData(WModelIndex index, Object value, ItemDataRole role)
   */
  public Signal2<WModelIndex, WModelIndex> dataChanged() {
    return this.dataChanged_;
  }
  /**
   * Signal emitted when some header data was changed.
   *
   * <p>The first argument indicates the orientation of the header, and the two integer arguments
   * are the row or column numbers of the first and last header item of which the value was changed.
   *
   * <p>
   *
   * @see WAbstractItemModel#setHeaderData(int section, Orientation orientation, Object value,
   *     ItemDataRole role)
   */
  public Signal3<Orientation, Integer, Integer> headerDataChanged() {
    return this.headerDataChanged_;
  }
  /**
   * Signal emitted when the layout is about to be changed.
   *
   * <p>A layout change may reorder or add/remove rows in the model, but columns are preserved.
   * Model indexes are invalidated by a layout change, but indexes may be ported across a layout
   * change by using the {@link WAbstractItemModel#toRawIndex(WModelIndex index) toRawIndex()} and
   * {@link WAbstractItemModel#fromRawIndex(Object rawIndex) fromRawIndex()} methods.
   *
   * <p>
   *
   * @see WAbstractItemModel#layoutChanged()
   * @see WAbstractItemModel#toRawIndex(WModelIndex index)
   * @see WAbstractItemModel#fromRawIndex(Object rawIndex)
   */
  public Signal layoutAboutToBeChanged() {
    return this.layoutAboutToBeChanged_;
  }
  /**
   * Signal emitted when the layout is changed.
   *
   * <p>
   *
   * @see WAbstractItemModel#layoutAboutToBeChanged()
   */
  public Signal layoutChanged() {
    return this.layoutChanged_;
  }
  /**
   * Signal emitted when the model was reset.
   *
   * <p>A model reset invalidates all existing data, and the model may change its entire geometry
   * (column count, row count).
   *
   * <p>
   *
   * @see WAbstractItemModel#reset()
   */
  public Signal modelReset() {
    return this.modelReset_;
  }
  /**
   * Resets the model and invalidate any data.
   *
   * <p>Informs any attached view that all data in the model was invalidated, and the model&apos;s
   * data should be reread.
   *
   * <p>This causes the {@link WAbstractItemModel#modelReset() modelReset()} signal to be emitted.
   */
  protected void reset() {
    this.modelReset_.trigger();
  }
  /**
   * Creates a model index for the given row and column.
   *
   * <p>Use this method to create a model index. <code>ptr</code> is an internal pointer that may be
   * used to identify the <b>parent</b> of the corresponding item. For a flat table model, <code>ptr
   * </code> can thus always be 0.
   *
   * <p>
   *
   * @see WModelIndex#getInternalPointer()
   */
  protected WModelIndex createIndex(int row, int column, Object ptr) {
    return new WModelIndex(row, column, this, ptr);
  }
  /**
   * Creates a model index for the given row and column.
   *
   * <p>Use this method to create a model index. <code>id</code> is an internal id that may be used
   * to identify the <b>parent</b> of the corresponding item. For a flat table model, <code>ptr
   * </code> can thus always be 0.
   *
   * <p>
   *
   * @see WModelIndex#getInternalId()
   */
  protected WModelIndex createIndex(int row, int column, long id) {
    return new WModelIndex(row, column, this, id);
  }
  /**
   * Method to be called before inserting columns.
   *
   * <p>If your model supports insertion of columns, then you should call this method before
   * inserting one or more columns, and {@link WAbstractItemModel#endInsertColumns()
   * endInsertColumns()} afterwards. These methods emit the necessary signals to allow view classes
   * to update themselves.
   *
   * <p>
   *
   * @see WAbstractItemModel#endInsertColumns()
   * @see WAbstractItemModel#insertColumns(int column, int count, WModelIndex parent)
   * @see WAbstractItemModel#columnsAboutToBeInserted()
   */
  protected void beginInsertColumns(final WModelIndex parent, int first, int last) {
    this.first_ = first;
    this.last_ = last;
    this.parent_ = parent;
    this.columnsAboutToBeInserted().trigger(this.parent_, first, last);
  }
  /**
   * Method to be called before inserting rows.
   *
   * <p>If your model supports insertion of rows, then you should call this method before inserting
   * one or more rows, and {@link WAbstractItemModel#endInsertRows() endInsertRows()} afterwards.
   * These methods emit the necessary signals to allow view classes to update themselves.
   *
   * <p>
   *
   * @see WAbstractItemModel#endInsertRows()
   * @see WAbstractItemModel#insertRows(int row, int count, WModelIndex parent)
   * @see WAbstractItemModel#rowsAboutToBeInserted()
   */
  protected void beginInsertRows(final WModelIndex parent, int first, int last) {
    this.first_ = first;
    this.last_ = last;
    this.parent_ = parent;
    this.rowsAboutToBeInserted().trigger(parent, first, last);
  }
  /**
   * Method to be called before removing columns.
   *
   * <p>If your model supports removal of columns, then you should call this method before removing
   * one or more columns, and {@link WAbstractItemModel#endRemoveColumns() endRemoveColumns()}
   * afterwards. These methods emit the necessary signals to allow view classes to update
   * themselves.
   *
   * <p>
   *
   * @see WAbstractItemModel#endRemoveColumns()
   * @see WAbstractItemModel#removeColumns(int column, int count, WModelIndex parent)
   * @see WAbstractItemModel#columnsAboutToBeRemoved()
   */
  protected void beginRemoveColumns(final WModelIndex parent, int first, int last) {
    this.first_ = first;
    this.last_ = last;
    this.parent_ = parent;
    this.columnsAboutToBeRemoved().trigger(parent, first, last);
  }
  /**
   * Method to be called before removing rows.
   *
   * <p>If your model supports removal of rows, then you should call this method before removing one
   * or more rows, and {@link WAbstractItemModel#endRemoveRows() endRemoveRows()} afterwards. These
   * methods emit the necessary signals to allow view classes to update themselves.
   *
   * <p>
   *
   * @see WAbstractItemModel#endRemoveRows()
   * @see WAbstractItemModel#removeRows(int row, int count, WModelIndex parent)
   * @see WAbstractItemModel#rowsAboutToBeRemoved()
   */
  protected void beginRemoveRows(final WModelIndex parent, int first, int last) {
    this.first_ = first;
    this.last_ = last;
    this.parent_ = parent;
    this.rowsAboutToBeRemoved().trigger(parent, first, last);
  }
  /**
   * Method to be called after inserting columns.
   *
   * <p>
   *
   * @see WAbstractItemModel#beginInsertColumns(WModelIndex parent, int first, int last)
   */
  protected void endInsertColumns() {
    this.columnsInserted().trigger(this.parent_, this.first_, this.last_);
  }
  /**
   * Method to be called after inserting rows.
   *
   * <p>
   *
   * @see WAbstractItemModel#beginInsertRows(WModelIndex parent, int first, int last)
   */
  protected void endInsertRows() {
    this.rowsInserted().trigger(this.parent_, this.first_, this.last_);
  }
  /**
   * Method to be called after removing columns.
   *
   * <p>
   *
   * @see WAbstractItemModel#beginRemoveColumns(WModelIndex parent, int first, int last)
   */
  protected void endRemoveColumns() {
    this.columnsRemoved().trigger(this.parent_, this.first_, this.last_);
  }
  /**
   * Method to be called after removing rows.
   *
   * <p>
   *
   * @see WAbstractItemModel#beginRemoveRows(WModelIndex parent, int first, int last)
   */
  protected void endRemoveRows() {
    this.rowsRemoved().trigger(this.parent_, this.first_, this.last_);
  }
  /**
   * Copy data to an index in this model.
   *
   * <p>The source index can be any valid index. The destination index must be part of this model.
   */
  protected void copyData(final WModelIndex sIndex, final WModelIndex dIndex) {
    if (dIndex.getModel() != this) {
      throw new WException("WAbstractItemModel::copyData(): dIndex must be an index of this model");
    }
    SortedMap<ItemDataRole, Object> values = this.getItemData(dIndex);
    for (Iterator<Map.Entry<ItemDataRole, Object>> i_it = values.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<ItemDataRole, Object> i = i_it.next();
      this.setData(dIndex, null, i.getKey());
    }
    WAbstractItemModel source = sIndex.getModel();
    this.setItemData(dIndex, source.getItemData(sIndex));
  }

  private int first_;
  private int last_;
  private WModelIndex parent_;
  private Signal3<WModelIndex, Integer, Integer> columnsAboutToBeInserted_;
  private Signal3<WModelIndex, Integer, Integer> columnsAboutToBeRemoved_;
  private Signal3<WModelIndex, Integer, Integer> columnsInserted_;
  private Signal3<WModelIndex, Integer, Integer> columnsRemoved_;
  private Signal3<WModelIndex, Integer, Integer> rowsAboutToBeInserted_;
  private Signal3<WModelIndex, Integer, Integer> rowsAboutToBeRemoved_;
  private Signal3<WModelIndex, Integer, Integer> rowsInserted_;
  private Signal3<WModelIndex, Integer, Integer> rowsRemoved_;
  private Signal2<WModelIndex, WModelIndex> dataChanged_;
  private Signal3<Orientation, Integer, Integer> headerDataChanged_;
  private Signal layoutAboutToBeChanged_;
  private Signal layoutChanged_;
  private Signal modelReset_;
  private static String DRAG_DROP_MIME_TYPE = "application/x-wabstractitemmodelselection";
}
