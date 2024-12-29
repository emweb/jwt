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
 * A proxy model for JWt&apos;s item models that provides batch editing.
 *
 * <p>This proxy model presents data from a source model, and caches any editing operation without
 * affecting the underlying source model, until {@link WBatchEditProxyModel#commitAll() commitAll()}
 * or {@link WBatchEditProxyModel#revertAll() revertAll()} is called. In this way, you can commit
 * all the editing in batch to the underlying source model, only when the user confirms the changes.
 *
 * <p>All editing operations are supported:
 *
 * <ul>
 *   <li>changing data (setData())
 *   <li>inserting and removing rows ({@link WBatchEditProxyModel#insertRows(int row, int count,
 *       WModelIndex parent) insertRows()} and {@link WBatchEditProxyModel#removeRows(int row, int
 *       count, WModelIndex parent) removeRows()})
 *   <li>inserting and removing columns ({@link WBatchEditProxyModel#insertColumns(int column, int
 *       count, WModelIndex parent) insertColumns()} and {@link
 *       WBatchEditProxyModel#removeColumns(int column, int count, WModelIndex parent)
 *       removeColumns()})
 * </ul>
 *
 * <p>The model supports both simple tabular models, as well as hierarchical (tree-like /
 * treetable-like) models, with children under items in the first column.
 *
 * <p>Default values for a newly inserted row can be set using {@link
 * WBatchEditProxyModel#setNewRowData(int column, Object data, ItemDataRole role) setNewRowData()}
 * and flags for its items using {@link WBatchEditProxyModel#setNewRowFlags(int column, EnumSet
 * flags) setNewRowFlags()}.
 */
public class WBatchEditProxyModel extends WAbstractProxyModel {
  private static Logger logger = LoggerFactory.getLogger(WBatchEditProxyModel.class);

  /** Constructor. */
  public WBatchEditProxyModel() {
    super();
    this.submitting_ = false;
    this.newRowData_ = new HashMap<Integer, SortedMap<ItemDataRole, Object>>();
    this.newRowFlags_ = new HashMap<Integer, EnumSet<ItemFlag>>();
    this.dirtyIndicationRole_ = ItemDataRole.of(-1);
    this.dirtyIndicationData_ = new Object();
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
    this.mappedIndexes_ = new TreeMap<WModelIndex, WAbstractProxyModel.BaseItem>();
  }
  /**
   * Returns whether changes have not yet been committed.
   *
   * <p>Returns whether have been made to the proxy model, which could be committed using {@link
   * WBatchEditProxyModel#commitAll() commitAll()} or reverted using {@link
   * WBatchEditProxyModel#revertAll() revertAll()}.
   */
  public boolean isDirty() {
    for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it =
            this.mappedIndexes_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it.next();
      WBatchEditProxyModel.Item item =
          ObjectUtils.cast(i.getValue(), WBatchEditProxyModel.Item.class);
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
   *
   * <p>This commits all changes to the source model.
   *
   * <p>
   *
   * @see WBatchEditProxyModel#revertAll()
   */
  public void commitAll() {
    this.submitting_ = true;
    for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it =
            this.mappedIndexes_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it.next();
      WBatchEditProxyModel.Item item =
          ObjectUtils.cast(i.getValue(), WBatchEditProxyModel.Item.class);
      while (!item.removedColumns_.isEmpty()) {
        this.getSourceModel().removeColumn(item.removedColumns_.get(0), item.sourceIndex_);
      }
      while (!item.insertedColumns_.isEmpty()) {
        this.getSourceModel().insertColumn(item.insertedColumns_.get(0), item.sourceIndex_);
      }
      while (!item.removedRows_.isEmpty()) {
        this.getSourceModel().removeRow(item.removedRows_.get(0), item.sourceIndex_);
      }
      while (!item.insertedRows_.isEmpty()) {
        this.getSourceModel().insertRow(item.insertedRows_.get(0), item.sourceIndex_);
      }
      for (Iterator<Map.Entry<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>>> j_it =
              item.editedValues_.entrySet().iterator();
          j_it.hasNext(); ) {
        Map.Entry<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>> j = j_it.next();
        WModelIndex index =
            this.getSourceModel().getIndex(j.getKey().row, j.getKey().column, item.sourceIndex_);
        SortedMap<ItemDataRole, Object> data = j.getValue();
        j_it.remove();
        this.getSourceModel().setItemData(index, data);
      }
    }
    this.submitting_ = false;
  }
  /**
   * Reverts changes.
   *
   * <p>This reverts all changes.
   *
   * <p>
   *
   * @see WBatchEditProxyModel#commitAll()
   */
  public void revertAll() {
    for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it =
            this.mappedIndexes_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it.next();
      WBatchEditProxyModel.Item item =
          ObjectUtils.cast(i.getValue(), WBatchEditProxyModel.Item.class);
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
      for (Iterator<Map.Entry<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>>> j_it =
              item.editedValues_.entrySet().iterator();
          j_it.hasNext(); ) {
        Map.Entry<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>> j = j_it.next();
        WBatchEditProxyModel.Cell c = j.getKey();
        j_it.remove();
        WModelIndex child = this.getIndex(c.row, c.column, proxyIndex);
        this.dataChanged().trigger(child, child);
      }
    }
  }
  /**
   * Sets default data for a newly inserted row.
   *
   * <p>You can use this method to initialize data for a newly inserted row.
   */
  public void setNewRowData(int column, final Object data, ItemDataRole role) {
    this.newRowData_.get(column).put(role, data);
  }
  /**
   * Sets default data for a newly inserted row.
   *
   * <p>Calls {@link #setNewRowData(int column, Object data, ItemDataRole role)
   * setNewRowData(column, data, ItemDataRole.Display)}
   */
  public final void setNewRowData(int column, final Object data) {
    setNewRowData(column, data, ItemDataRole.Display);
  }
  /**
   * Sets the item flags for items in a newly inserted row.
   *
   * <p>By default, {@link WBatchEditProxyModel#getFlags(WModelIndex index) getFlags()} will return
   * {@link ItemFlag#Selectable}.
   */
  public void setNewRowFlags(int column, EnumSet<ItemFlag> flags) {
    this.newRowFlags_.put(column, flags);
  }
  /**
   * Sets the item flags for items in a newly inserted row.
   *
   * <p>Calls {@link #setNewRowFlags(int column, EnumSet flags) setNewRowFlags(column,
   * EnumSet.of(flag, flags))}
   */
  public final void setNewRowFlags(int column, ItemFlag flag, ItemFlag... flags) {
    setNewRowFlags(column, EnumSet.of(flag, flags));
  }
  /**
   * Configures data used to indicate a modified item.
   *
   * <p>This sets <code>data</code> for item data role <code>role</code> to be returned by data()
   * for an item that is dirty (e.g. because it belongs to a newly inserted row/column, or because
   * new data has been set for it.
   *
   * <p>When <code>role</code> is {@link ItemDataRole#StyleClass}, the style class is appended to
   * any style already returned by the source model or set by {@link
   * WBatchEditProxyModel#setNewRowData(int column, Object data, ItemDataRole role)
   * setNewRowData()}.
   *
   * <p>By default there is no dirty indication.
   */
  public void setDirtyIndication(ItemDataRole role, final Object data) {
    this.dirtyIndicationRole_ = role;
    this.dirtyIndicationData_ = data;
  }

  public WModelIndex mapFromSource(final WModelIndex sourceIndex) {
    if ((sourceIndex != null)) {
      if (this.isRemoved(sourceIndex.getParent())) {
        return null;
      }
      WModelIndex sourceParent = sourceIndex.getParent();
      WBatchEditProxyModel.Item parentItem = this.itemFromSourceIndex(sourceParent);
      int row = this.adjustedProxyRow(parentItem, sourceIndex.getRow());
      int column = this.adjustedProxyColumn(parentItem, sourceIndex.getColumn());
      if (row >= 0 && column >= 0) {
        return this.createIndex(row, column, parentItem);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public WModelIndex mapToSource(final WModelIndex proxyIndex) {
    if ((proxyIndex != null)) {
      WBatchEditProxyModel.Item parentItem = this.parentItemFromIndex(proxyIndex);
      int row = this.adjustedSourceRow(parentItem, proxyIndex.getRow());
      int column = this.adjustedSourceColumn(parentItem, proxyIndex.getColumn());
      if (row >= 0 && column >= 0) {
        return this.getSourceModel().getIndex(row, column, parentItem.sourceIndex_);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
  /**
   * Sets the source model.
   *
   * <p>The source model provides the actual data for the proxy model.
   *
   * <p>Ownership of the source model is <i>not</i> transferred.
   *
   * <p>All signals of the source model are propagated to the proxy model.
   */
  public void setSourceModel(final WAbstractItemModel model) {
    for (int i = 0; i < this.modelConnections_.size(); ++i) {
      this.modelConnections_.get(i).disconnect();
    }
    this.modelConnections_.clear();
    super.setSourceModel(model);
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsAboutToBeInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceColumnsAboutToBeInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceColumnsInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsAboutToBeRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceColumnsAboutToBeRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceColumnsRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsAboutToBeInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceRowsAboutToBeInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceRowsInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsAboutToBeRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceRowsAboutToBeRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceRowsRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .dataChanged()
            .addListener(
                this,
                (WModelIndex e1, WModelIndex e2) -> {
                  WBatchEditProxyModel.this.sourceDataChanged(e1, e2);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .headerDataChanged()
            .addListener(
                this,
                (Orientation e1, Integer e2, Integer e3) -> {
                  WBatchEditProxyModel.this.sourceHeaderDataChanged(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .layoutAboutToBeChanged()
            .addListener(
                this,
                () -> {
                  WBatchEditProxyModel.this.sourceLayoutAboutToBeChanged();
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .layoutChanged()
            .addListener(
                this,
                () -> {
                  WBatchEditProxyModel.this.sourceLayoutChanged();
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .modelReset()
            .addListener(
                this,
                () -> {
                  WBatchEditProxyModel.this.sourceModelReset();
                }));
    this.resetMappings();
  }

  public int getColumnCount(final WModelIndex parent) {
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
      return this.getSourceModel().getColumnCount(this.mapToSource(parent));
    }
  }

  public int getRowCount(final WModelIndex parent) {
    WBatchEditProxyModel.Item item = this.itemFromIndex(parent, false);
    if (item != null) {
      if (item.insertedParent_ != null) {
        return item.insertedRows_.size();
      } else {
        return this.getSourceModel().getRowCount(item.sourceIndex_)
            + item.insertedRows_.size()
            - item.removedRows_.size();
      }
    } else {
      return this.getSourceModel().getRowCount(this.mapToSource(parent));
    }
  }

  public WModelIndex getParent(final WModelIndex index) {
    if ((index != null)) {
      WBatchEditProxyModel.Item parentItem = this.parentItemFromIndex(index);
      return this.mapFromSource(parentItem.sourceIndex_);
    } else {
      return null;
    }
  }

  public WModelIndex getIndex(int row, int column, final WModelIndex parent) {
    WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
    return this.createIndex(row, column, item);
  }

  public Object getData(final WModelIndex index, ItemDataRole role) {
    WBatchEditProxyModel.Item item = this.itemFromIndex(index.getParent());
    SortedMap<ItemDataRole, Object> i =
        item.editedValues_.get(new WBatchEditProxyModel.Cell(index.getRow(), index.getColumn()));
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
   *
   * <p>The default implementation will copy {@link ItemDataRole#Edit} data to {@link
   * ItemDataRole#Display}. You may want to specialize the model to provide a more specialized
   * editing behaviour.
   */
  public boolean setData(final WModelIndex index, final Object value, ItemDataRole role) {
    WBatchEditProxyModel.Item item = this.itemFromIndex(index.getParent());
    SortedMap<ItemDataRole, Object> i =
        item.editedValues_.get(new WBatchEditProxyModel.Cell(index.getRow(), index.getColumn()));
    if (i == null) {
      WModelIndex sourceIndex = this.mapToSource(index);
      SortedMap<ItemDataRole, Object> dataMap = new TreeMap<ItemDataRole, Object>();
      if ((sourceIndex != null)) {
        dataMap = this.getSourceModel().getItemData(sourceIndex);
      }
      dataMap.put(role, value);
      if (role.equals(ItemDataRole.Edit)) {
        dataMap.put(ItemDataRole.Display, value);
      }
      item.editedValues_.put(
          new WBatchEditProxyModel.Cell(index.getRow(), index.getColumn()), dataMap);
    } else {
      i.put(role, value);
      if (role.equals(ItemDataRole.Edit)) {
        i.put(ItemDataRole.Display, value);
      }
    }
    this.dataChanged().trigger(index, index);
    return true;
  }

  public EnumSet<ItemFlag> getFlags(final WModelIndex index) {
    WModelIndex sourceIndex = this.mapToSource(index);
    if ((sourceIndex != null)) {
      return this.getSourceModel().getFlags(sourceIndex);
    } else {
      EnumSet<ItemFlag> i = this.newRowFlags_.get(index.getColumn());
      if (i != null) {
        return i;
      } else {
        return super.getFlags(index);
      }
    }
  }

  public Object getHeaderData(int section, Orientation orientation, ItemDataRole role) {
    if (orientation == Orientation.Vertical) {
      return null;
    } else {
      return this.getSourceModel().getHeaderData(section, orientation, role);
    }
  }

  public boolean insertRows(int row, int count, final WModelIndex parent) {
    if (this.getColumnCount(parent) == 0) {
      this.insertColumns(0, 1, parent);
    }
    this.beginInsertRows(parent, row, row + count - 1);
    WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
    this.shiftRows(item, row, count);
    this.insertIndexes(item, item.insertedRows_, item.insertedItems_, row, count);
    for (int i = 0; i < count; ++i) {
      for (int j = 0; j < this.getColumnCount(parent); ++j) {
        SortedMap<ItemDataRole, Object> data = new TreeMap<ItemDataRole, Object>();
        SortedMap<ItemDataRole, Object> nri = this.newRowData_.get(j);
        if (nri != null) {
          data = nri;
        }
        item.editedValues_.put(new WBatchEditProxyModel.Cell(row + i, j), data);
      }
    }
    this.endInsertRows();
    return true;
  }

  public boolean removeRows(int row, int count, final WModelIndex parent) {
    this.beginRemoveRows(parent, row, row + count - 1);
    WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
    this.removeIndexes(
        item, item.insertedRows_, item.removedRows_, item.insertedItems_, row, count);
    this.shiftRows(item.editedValues_, row, -count);
    this.endRemoveRows();
    return true;
  }

  public boolean insertColumns(int column, int count, final WModelIndex parent) {
    this.beginInsertColumns(parent, column, column + count - 1);
    WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
    this.shiftColumns(item, column, count);
    this.insertIndexes(
        item, item.insertedColumns_, (List<WBatchEditProxyModel.Item>) null, column, count);
    this.endInsertColumns();
    return true;
  }

  public boolean removeColumns(int column, int count, final WModelIndex parent) {
    this.beginRemoveColumns(parent, column, column + count - 1);
    WBatchEditProxyModel.Item item = this.itemFromIndex(parent);
    this.removeIndexes(
        item,
        item.insertedColumns_,
        item.removedColumns_,
        (List<WBatchEditProxyModel.Item>) null,
        column,
        count);
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
    public Map<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>> editedValues_;
    public List<Integer> removedRows_;
    public List<Integer> insertedRows_;
    public List<WBatchEditProxyModel.Item> insertedItems_;
    public List<Integer> removedColumns_;
    public List<Integer> insertedColumns_;

    public Item(final WModelIndex sourceIndex) {
      super(sourceIndex);
      this.insertedParent_ = null;
      this.editedValues_ =
          new HashMap<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>>();
      this.removedRows_ = new ArrayList<Integer>();
      this.insertedRows_ = new ArrayList<Integer>();
      this.insertedItems_ = new ArrayList<WBatchEditProxyModel.Item>();
      this.removedColumns_ = new ArrayList<Integer>();
      this.insertedColumns_ = new ArrayList<Integer>();
    }

    public Item(WBatchEditProxyModel.Item insertedParent) {
      super(null);
      this.insertedParent_ = insertedParent;
      this.editedValues_ =
          new HashMap<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>>();
      this.removedRows_ = new ArrayList<Integer>();
      this.insertedRows_ = new ArrayList<Integer>();
      this.insertedItems_ = new ArrayList<WBatchEditProxyModel.Item>();
      this.removedColumns_ = new ArrayList<Integer>();
      this.insertedColumns_ = new ArrayList<Integer>();
    }
  }

  private boolean submitting_;
  private Map<Integer, SortedMap<ItemDataRole, Object>> newRowData_;
  private Map<Integer, EnumSet<ItemFlag>> newRowFlags_;
  private ItemDataRole dirtyIndicationRole_;
  private Object dirtyIndicationData_;
  private List<AbstractSignal.Connection> modelConnections_;
  private SortedMap<WModelIndex, WAbstractProxyModel.BaseItem> mappedIndexes_;

  private void sourceColumnsAboutToBeInserted(final WModelIndex parent, int start, int end) {
    if (this.isRemoved(parent)) {
      return;
    }
    this.beginInsertColumns(this.mapFromSource(parent), start, end);
  }

  private void sourceColumnsInserted(final WModelIndex parent, int start, int end) {
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

  private void sourceColumnsAboutToBeRemoved(final WModelIndex parent, int start, int end) {
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

  private void sourceColumnsRemoved(final WModelIndex parent, int start, int end) {
    if (this.isRemoved(parent)) {
      return;
    }
    this.endRemoveColumns();
  }

  private void sourceRowsAboutToBeInserted(final WModelIndex parent, int start, int end) {}

  private void sourceRowsInserted(final WModelIndex parent, int start, int end) {
    if (this.isRemoved(parent)) {
      return;
    }
    this.startShiftModelIndexes(parent, start, end - start + 1, this.mappedIndexes_);
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
          WBatchEditProxyModel.Item child = item.insertedItems_.get(index);
          if (child != null) {
            child.sourceIndex_ = this.getSourceModel().getIndex(start + i, 0, parent);
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
  }

  private void sourceRowsAboutToBeRemoved(final WModelIndex parent, int start, int end) {
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
    this.startShiftModelIndexes(parent, start, -(end - start + 1), this.mappedIndexes_);
  }

  private void sourceRowsRemoved(final WModelIndex parent, int start, int end) {
    if (this.isRemoved(parent)) {
      return;
    }
    this.endShiftModelIndexes(parent, start, -(end - start + 1), this.mappedIndexes_);
  }

  private void sourceDataChanged(final WModelIndex topLeft, final WModelIndex bottomRight) {
    if (this.isRemoved(topLeft.getParent())) {
      return;
    }
    for (int row = topLeft.getRow(); row <= bottomRight.getRow(); ++row) {
      for (int col = topLeft.getColumn(); col <= bottomRight.getColumn(); ++col) {
        WModelIndex l = this.getSourceModel().getIndex(row, col, topLeft.getParent());
        if (!this.isRemoved(l)) {
          this.dataChanged().trigger(this.mapFromSource(l), this.mapFromSource(l));
        }
      }
    }
  }

  private void sourceHeaderDataChanged(Orientation orientation, int start, int end) {
    if (orientation == Orientation.Vertical) {
      WBatchEditProxyModel.Item item = this.itemFromIndex(null);
      for (int row = start; row <= end; ++row) {
        int proxyRow = this.adjustedProxyRow(item, row);
        if (proxyRow != -1) {
          this.headerDataChanged().trigger(orientation, proxyRow, proxyRow);
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

  private void sourceModelReset() {
    this.resetMappings();
    this.reset();
  }

  private WBatchEditProxyModel.Item itemFromSourceIndex(
      final WModelIndex sourceParent, boolean autoCreate) {
    if (this.isRemoved(sourceParent)) {
      return null;
    }
    WAbstractProxyModel.BaseItem i = this.mappedIndexes_.get(sourceParent);
    if (i == null) {
      if (autoCreate) {
        WBatchEditProxyModel.Item result = new WBatchEditProxyModel.Item(sourceParent);
        this.mappedIndexes_.put(sourceParent, result);
        return result;
      } else {
        return null;
      }
    } else {
      return ObjectUtils.cast(i, WBatchEditProxyModel.Item.class);
    }
  }

  private final WBatchEditProxyModel.Item itemFromSourceIndex(final WModelIndex sourceParent) {
    return itemFromSourceIndex(sourceParent, true);
  }

  private WBatchEditProxyModel.Item itemFromInsertedRow(
      WBatchEditProxyModel.Item parentItem, final WModelIndex index, boolean autoCreate) {
    int i = parentItem.insertedRows_.indexOf(index.getRow());
    if (!(parentItem.insertedItems_.get(i) != null) && autoCreate) {
      WBatchEditProxyModel.Item item = new WBatchEditProxyModel.Item(parentItem);
      parentItem.insertedItems_.set(i, item);
    }
    return parentItem.insertedItems_.get(i);
  }

  private final WBatchEditProxyModel.Item itemFromInsertedRow(
      WBatchEditProxyModel.Item parentItem, final WModelIndex index) {
    return itemFromInsertedRow(parentItem, index, true);
  }

  private WBatchEditProxyModel.Item parentItemFromIndex(final WModelIndex index) {
    return (WBatchEditProxyModel.Item) index.getInternalPointer();
  }

  private WBatchEditProxyModel.Item itemFromIndex(final WModelIndex index, boolean autoCreate) {
    if ((index != null)) {
      WBatchEditProxyModel.Item parentItem = this.parentItemFromIndex(index);
      int row = this.adjustedSourceRow(parentItem, index.getRow());
      int column = this.adjustedSourceColumn(parentItem, index.getColumn());
      if (row >= 0 && column >= 0) {
        WModelIndex sourceIndex =
            this.getSourceModel().getIndex(row, column, parentItem.sourceIndex_);
        return this.itemFromSourceIndex(sourceIndex, autoCreate);
      } else {
        if (index.getColumn() == 0) {
          return this.itemFromInsertedRow(parentItem, index, autoCreate);
        } else {
          if (autoCreate) {
            throw new WException("WBatchEditProxyModel does not support children in column > 0");
          } else {
            return null;
          }
        }
      }
    } else {
      return this.itemFromSourceIndex(null, autoCreate);
    }
  }

  private final WBatchEditProxyModel.Item itemFromIndex(final WModelIndex index) {
    return itemFromIndex(index, true);
  }

  private boolean isRemoved(final WModelIndex sourceIndex) {
    if (!(sourceIndex != null)) {
      return false;
    }
    WModelIndex sourceParent = sourceIndex.getParent();
    if (this.isRemoved(sourceParent)) {
      return true;
    } else {
      WBatchEditProxyModel.Item parentItem = this.itemFromSourceIndex(sourceParent);
      int row = this.adjustedProxyRow(parentItem, sourceIndex.getRow());
      if (row < 0) {
        return true;
      }
      int column = this.adjustedProxyColumn(parentItem, sourceIndex.getColumn());
      return column < 0;
    }
  }

  private int adjustedProxyRow(WBatchEditProxyModel.Item item, int sourceRow) {
    return this.adjustedProxyIndex(sourceRow, item.insertedRows_, item.removedRows_);
  }

  private int adjustedSourceRow(WBatchEditProxyModel.Item item, int proxyRow) {
    return this.adjustedSourceIndex(proxyRow, item.insertedRows_, item.removedRows_);
  }

  private int adjustedProxyColumn(WBatchEditProxyModel.Item item, int sourceColumn) {
    return this.adjustedProxyIndex(sourceColumn, item.insertedColumns_, item.removedColumns_);
  }

  private int adjustedSourceColumn(WBatchEditProxyModel.Item item, int proxyColumn) {
    return this.adjustedSourceIndex(proxyColumn, item.insertedColumns_, item.removedColumns_);
  }

  private int adjustedProxyIndex(
      int sourceIndex, final List<Integer> ins, final List<Integer> rem) {
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

  private int adjustedSourceIndex(
      int proxyIndex, final List<Integer> ins, final List<Integer> rem) {
    int inserted = CollectionUtils.lowerBound(ins, proxyIndex);
    if (inserted < ins.size() && ins.get(inserted) == proxyIndex) {
      return -1;
    }
    int removed = CollectionUtils.upperBound(rem, proxyIndex);
    return proxyIndex + removed - inserted;
  }

  private void insertIndexes(
      WBatchEditProxyModel.Item item,
      final List<Integer> ins,
      List<WBatchEditProxyModel.Item> rowItems,
      int index,
      int count) {
    int insertIndex = CollectionUtils.lowerBound(ins, index);
    for (int i = 0; i < count; ++i) {
      ins.add(0 + insertIndex + i, index + i);
      if (rowItems != null) {
        rowItems.add(0 + insertIndex + i, (WBatchEditProxyModel.Item) null);
      }
    }
  }

  private void removeIndexes(
      WBatchEditProxyModel.Item item,
      final List<Integer> ins,
      final List<Integer> rem,
      List<WBatchEditProxyModel.Item> rowItems,
      int index,
      int count) {
    for (int i = 0; i < count; ++i) {
      int insi = CollectionUtils.lowerBound(ins, index);
      if (insi != ins.size() && ins.get(insi) == index) {
        ins.remove(0 + insi);
        if (rowItems != null) {

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
    WModelIndex sourceIndex = this.getSourceModel().getIndex(row, 0, item.sourceIndex_);
    for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it =
            this.mappedIndexes_.tailMap(sourceIndex).entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it.next();
      if (isAncestor(sourceIndex, i.getKey())) {

        i_it.remove();
      } else {
        break;
      }
    }
  }

  private void shift(final List<Integer> v, int index, int count) {
    int first = CollectionUtils.lowerBound(v, index);
    for (int i = first; i < v.size(); ++i) {
      v.set(i, v.get(i) + count);
    }
  }

  private void shiftRows(
      final Map<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>> v, int row, int count) {
    for (Iterator<Map.Entry<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>>> i_it =
            v.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>> i = i_it.next();
      if (i.getKey().row >= row) {
        final WBatchEditProxyModel.Cell c = i.getKey();
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
      final Map<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>> v,
      int column,
      int count) {
    for (Iterator<Map.Entry<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>>> i_it =
            v.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WBatchEditProxyModel.Cell, SortedMap<ItemDataRole, Object>> i = i_it.next();
      if (i.getKey().column >= column) {
        final WBatchEditProxyModel.Cell c = i.getKey();
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

  private void shiftColumns(WBatchEditProxyModel.Item item, int column, int count) {
    this.shift(item.insertedColumns_, column, count);
    this.shift(item.removedColumns_, column, count);
    this.shiftColumns(item.editedValues_, column, count);
  }

  private void resetMappings() {
    for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it =
            this.mappedIndexes_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it.next();
    }
    this.mappedIndexes_.clear();
  }

  private Object indicateDirty(ItemDataRole role, final Object value) {
    if (role.equals(this.dirtyIndicationRole_)) {
      if (role.equals(ItemDataRole.StyleClass)) {
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

  static boolean isAncestor(final WModelIndex i1, final WModelIndex i2) {
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
