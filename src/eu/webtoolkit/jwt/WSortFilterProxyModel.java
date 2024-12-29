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
 * A proxy model for JWt&apos;s item models that provides filtering and/or sorting.
 *
 * <p>This proxy model does not store data itself, but presents data from a source model, after
 * filtering rows. It also allows sorting of the source model data, without actually altering the
 * source model. This may be convenient when the source model does not support sorting (i.e. does
 * not reimplement {@link WAbstractItemModel#sort(int column, SortOrder order)
 * WAbstractItemModel#sort()}), or you do not want to reorder the underlying model since that
 * affects all views on the model.
 *
 * <p>To use the proxy model to filter data, you use the methods {@link
 * WSortFilterProxyModel#setFilterKeyColumn(int column) setFilterKeyColumn()}, {@link
 * WSortFilterProxyModel#setFilterRegExp(Pattern pattern) setFilterRegExp()} and {@link
 * WSortFilterProxyModel#setFilterRole(ItemDataRole role) setFilterRole()} to specify a filtering
 * operation based on the values of a single column. If this filtering mechanism is too limiting,
 * you can provide specialized filtering by reimplementing the {@link
 * WSortFilterProxyModel#filterAcceptRow(int sourceRow, WModelIndex sourceParent) filterAcceptRow()}
 * method.
 *
 * <p>Sorting is provided by reimplementing the standard {@link WAbstractItemModel#sort(int column,
 * SortOrder order) WAbstractItemModel#sort()} method. In this way, a view class such as {@link
 * WTreeView} may resort the model as indicated by the user. Use {@link
 * WSortFilterProxyModel#setSortRole(ItemDataRole role) setSortRole()} to indicate on what data role
 * sorting should be done, or reimplement the lessThan() method to provide a specialized sorting
 * method.
 *
 * <p>By default, the proxy does not automatically refilter and resort when the original model
 * changes. Data changes or row additions to the source model are not automatically reflected in the
 * proxy model, but to maintain integrity, row removals in the source model are always reflected in
 * the proxy model. You can enable the model to always refilter and resort when the underlying model
 * changes using {@link WSortFilterProxyModel#setDynamicSortFilter(boolean enable)
 * setDynamicSortFilter()}.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * // model is the source model
 * WAbstractItemModel model = ...
 *
 * // we setup a proxy to filter the source model
 * WSortFilterProxyModel proxy = new WSortFilterProxyModel(this);
 * proxy.setSourceModel(model);
 * proxy.setDynamicSortFilter(true);
 * proxy.setFilterKeyColumn(0);
 * proxy.setFilterRole(ItemDataRole.User);
 * proxy.setFilterRegExp(".*");
 *
 * // configure a view to use the proxy model instead of the source model
 * WTreeView view = new WTreeView(this);
 * view.setModel(proxy);
 * ...
 *
 * }</pre>
 */
public class WSortFilterProxyModel extends WAbstractProxyModel {
  private static Logger logger = LoggerFactory.getLogger(WSortFilterProxyModel.class);

  /** Constructor. */
  public WSortFilterProxyModel() {
    super();
    this.regex_ = null;
    this.filterKeyColumn_ = 0;
    this.filterRole_ = ItemDataRole.Display;
    this.sortKeyColumn_ = -1;
    this.sortRole_ = ItemDataRole.Display;
    this.sortOrder_ = SortOrder.Ascending;
    this.dynamic_ = false;
    this.inserting_ = false;
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
    this.mappedIndexes_ = new TreeMap<WModelIndex, WAbstractProxyModel.BaseItem>();
    this.mappedRootItem_ = null;
  }

  public WModelIndex mapFromSource(final WModelIndex sourceIndex) {
    if ((sourceIndex != null)) {
      WModelIndex sourceParent = sourceIndex.getParent();
      WSortFilterProxyModel.Item item = this.itemFromSourceIndex(sourceParent);
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

  public WModelIndex mapToSource(final WModelIndex proxyIndex) {
    if ((proxyIndex != null)) {
      WSortFilterProxyModel.Item parentItem = this.parentItemFromIndex(proxyIndex);
      return this.getSourceModel()
          .getIndex(
              parentItem.proxyRowMap_.get(proxyIndex.getRow()),
              proxyIndex.getColumn(),
              parentItem.sourceIndex_);
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
   * <p>All signals of the source model are forwarded to the proxy model.
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
                  WSortFilterProxyModel.this.sourceColumnsAboutToBeInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSortFilterProxyModel.this.sourceColumnsInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsAboutToBeRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSortFilterProxyModel.this.sourceColumnsAboutToBeRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSortFilterProxyModel.this.sourceColumnsRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsAboutToBeInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSortFilterProxyModel.this.sourceRowsAboutToBeInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSortFilterProxyModel.this.sourceRowsInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsAboutToBeRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSortFilterProxyModel.this.sourceRowsAboutToBeRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WSortFilterProxyModel.this.sourceRowsRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .dataChanged()
            .addListener(
                this,
                (WModelIndex e1, WModelIndex e2) -> {
                  WSortFilterProxyModel.this.sourceDataChanged(e1, e2);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .headerDataChanged()
            .addListener(
                this,
                (Orientation e1, Integer e2, Integer e3) -> {
                  WSortFilterProxyModel.this.sourceHeaderDataChanged(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .layoutAboutToBeChanged()
            .addListener(
                this,
                () -> {
                  WSortFilterProxyModel.this.sourceLayoutAboutToBeChanged();
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .layoutChanged()
            .addListener(
                this,
                () -> {
                  WSortFilterProxyModel.this.sourceLayoutChanged();
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .modelReset()
            .addListener(
                this,
                () -> {
                  WSortFilterProxyModel.this.sourceModelReset();
                }));
    this.resetMappings();
  }
  /**
   * Specify the column on which the filtering is applied.
   *
   * <p>This configures the column on which the {@link WSortFilterProxyModel#getFilterRegExp()
   * getFilterRegExp()} is applied.
   *
   * <p>The default value is 0.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#setFilterRegExp(Pattern pattern)
   * @see WSortFilterProxyModel#setFilterRole(ItemDataRole role)
   */
  public void setFilterKeyColumn(int column) {
    this.filterKeyColumn_ = column;
  }
  /**
   * Return the column on which the filtering is applied.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#setFilterKeyColumn(int column)
   */
  public int getFilterKeyColumn() {
    return this.filterKeyColumn_;
  }
  /**
   * Specify a regular expression for filtering.
   *
   * <p>This configures the regular expression used for filtering on {@link
   * WSortFilterProxyModel#getFilterKeyColumn() getFilterKeyColumn()}.
   *
   * <p>The default value is an empty expression, which disables filtering.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#setFilterKeyColumn(int column)
   * @see WSortFilterProxyModel#setFilterRole(ItemDataRole role)
   */
  public void setFilterRegExp(Pattern pattern) {
    this.regex_ = pattern;
    this.invalidate();
  }
  /**
   * Return the regular expression used for filtering.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#setFilterRegExp(Pattern pattern)
   */
  public Pattern getFilterRegExp() {
    return this.regex_;
  }
  /**
   * Specify the data role used for filtering.
   *
   * <p>This configures the data role used for filtering on {@link
   * WSortFilterProxyModel#getFilterKeyColumn() getFilterKeyColumn()}.
   *
   * <p>The default value is {@link ItemDataRole#Display}.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#setFilterKeyColumn(int column)
   * @see WSortFilterProxyModel#setFilterRegExp(Pattern pattern)
   */
  public void setFilterRole(ItemDataRole role) {
    this.filterRole_ = role;
  }
  /**
   * Return the data role used for filtering.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#setFilterRole(ItemDataRole role)
   */
  public ItemDataRole getFilterRole() {
    return this.filterRole_;
  }
  /**
   * Specify the data role used used for sorting.
   *
   * <p>This configures the data role used for sorting.
   *
   * <p>The default value is {@link ItemDataRole#Display}.
   *
   * <p>
   */
  public void setSortRole(ItemDataRole role) {
    this.sortRole_ = role;
  }
  /**
   * Return the data role used for sorting.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#setSortRole(ItemDataRole role)
   */
  public ItemDataRole getSortRole() {
    return this.sortRole_;
  }
  /**
   * Returns the current sort column.
   *
   * <p>When {@link WSortFilterProxyModel#sort(int column, SortOrder order) sort()} has not been
   * called, the model is unsorted, and this method returns -1.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#sort(int column, SortOrder order)
   */
  public int getSortColumn() {
    return this.sortKeyColumn_;
  }
  /**
   * Returns the current sort order.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#sort(int column, SortOrder order)
   */
  public SortOrder getSortOrder() {
    return this.sortOrder_;
  }
  /**
   * Configure the proxy to dynamically track changes in the source model.
   *
   * <p>When <code>enable</code> is <code>true</code>, the proxy will re-filter and re-sort the
   * model when changes happen to the source model.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This may be ackward when editing through the proxy model, since changing
   * some data may rearrange the model and thus invalidate model indexes. Therefore it is usually
   * less complicated to manipulate directly the source model instead. </i>
   */
  public void setDynamicSortFilter(boolean enable) {
    this.dynamic_ = enable;
  }
  /**
   * Returns whether this proxy dynmically filters and sorts.
   *
   * <p>
   *
   * @see WSortFilterProxyModel#setDynamicSortFilter(boolean enable)
   */
  public boolean isDynamicSortFilter() {
    return this.dynamic_;
  }
  /**
   * Invalidates the current filter.
   *
   * <p>This refilters and resorts the model, and is useful only if you have reimplemented {@link
   * WSortFilterProxyModel#filterAcceptRow(int sourceRow, WModelIndex sourceParent)
   * filterAcceptRow()} and/or lessThan()
   */
  public void invalidate() {
    if (this.getSourceModel() != null) {
      this.layoutAboutToBeChanged().trigger();
      this.resetMappings();
      this.layoutChanged().trigger();
    }
  }

  public int getColumnCount(final WModelIndex parent) {
    return this.getSourceModel().getColumnCount(this.mapToSource(parent));
  }

  public int getRowCount(final WModelIndex parent) {
    WSortFilterProxyModel.Item item = this.itemFromIndex(parent);
    return item.proxyRowMap_.size();
  }

  public WModelIndex getParent(final WModelIndex index) {
    if ((index != null)) {
      WSortFilterProxyModel.Item parentItem = this.parentItemFromIndex(index);
      return this.mapFromSource(parentItem.sourceIndex_);
    } else {
      return null;
    }
  }

  public WModelIndex getIndex(int row, int column, final WModelIndex parent) {
    WSortFilterProxyModel.Item item = this.itemFromIndex(parent);
    return this.createIndex(row, column, item);
  }

  public boolean setHeaderData(
      int section, Orientation orientation, final Object value, ItemDataRole role) {
    if (orientation == Orientation.Vertical) {
      section = this.mapToSource(this.getIndex(section, 0)).getRow();
    }
    return this.getSourceModel().setHeaderData(section, orientation, value, role);
  }

  public Object getHeaderData(int section, Orientation orientation, ItemDataRole role) {
    if (orientation == Orientation.Vertical) {
      section = this.mapToSource(this.getIndex(section, 0)).getRow();
    }
    return this.getSourceModel().getHeaderData(section, orientation, role);
  }

  public EnumSet<HeaderFlag> getHeaderFlags(int section, Orientation orientation) {
    if (orientation == Orientation.Vertical) {
      section = this.mapToSource(this.getIndex(section, 0)).getRow();
    }
    return this.getSourceModel().getHeaderFlags(section, orientation);
  }
  /**
   * Inserts a number rows.
   *
   * <p>The rows are inserted in the source model, and if successful, also in the proxy model
   * regardless of whether they are matched by the current filter. They are inserted at the
   * indicated row, regardless of whether this is the correct place according to the defined
   * sorting.
   *
   * <p>As soon as you set data for the column on which the filtering is active, or which affects
   * the sorting, the row may be filtered out or change position when dynamic sorting/filtering is
   * enabled. Therefore, it is usually a good idea to temporarily disable the dynamic sort/filtering
   * behaviour while inserting new row(s) of data.
   */
  public boolean insertRows(int row, int count, final WModelIndex parent) {
    int sourceRow;
    int currentCount = this.getRowCount(parent);
    if (row < currentCount) {
      sourceRow = this.mapToSource(this.getIndex(row, 0, parent)).getRow();
    } else {
      sourceRow = this.getSourceModel().getRowCount(this.mapToSource(parent));
    }
    this.inserting_ = true;
    boolean result = this.getSourceModel().insertRows(sourceRow, count, this.mapToSource(parent));
    this.inserting_ = false;
    if (!result) {
      return false;
    }
    WSortFilterProxyModel.Item item = this.itemFromIndex(parent);
    this.beginInsertRows(parent, row, row);
    item.proxyRowMap_.add(sourceRow);
    item.sourceRowMap_.add(0 + sourceRow, row);
    this.endInsertRows();
    return true;
  }
  /**
   * Removes a number rows.
   *
   * <p>The rows are removed from the source model.
   */
  public boolean removeRows(int row, int count, final WModelIndex parent) {
    for (int i = 0; i < count; ++i) {
      int sourceRow = this.mapToSource(this.getIndex(row, 0, parent)).getRow();
      if (!this.getSourceModel().removeRows(sourceRow, 1, this.mapToSource(parent))) {
        return false;
      }
    }
    return true;
  }

  public void sort(int column, SortOrder order) {
    this.sortKeyColumn_ = column;
    this.sortOrder_ = order;
    this.invalidate();
  }
  /**
   * Returns whether a source row is accepted by the filter.
   *
   * <p>The default implementation uses {@link WSortFilterProxyModel#getFilterKeyColumn()
   * getFilterKeyColumn()}, {@link WSortFilterProxyModel#getFilterRole() getFilterRole()} and {@link
   * WSortFilterProxyModel#getFilterRegExp() getFilterRegExp()}.
   *
   * <p>You may want to reimplement this method to provide specialized filtering.
   */
  protected boolean filterAcceptRow(int sourceRow, final WModelIndex sourceParent) {
    if (this.regex_ != null) {
      WString s =
          StringUtils.asString(
              this.getSourceModel()
                  .getIndex(sourceRow, this.filterKeyColumn_, sourceParent)
                  .getData(this.filterRole_));
      boolean result = this.regex_.matcher(s.toString()).matches();
      return result;
    } else {
      return true;
    }
  }
  /**
   * Compares two indexes.
   *
   * <p>The default implementation uses {@link WSortFilterProxyModel#getSortRole() getSortRole()}
   * and an ordering using the operator&lt; when the data is of the same type or compares
   * lexicographically otherwise.
   *
   * <p>You may want to reimplement this method to provide specialized sorting.
   */
  protected int compare(final WModelIndex lhs, final WModelIndex rhs) {
    return ObjectUtils.compare(lhs.getData(this.sortRole_), rhs.getData(this.sortRole_));
  }

  static class Item extends WAbstractProxyModel.BaseItem {
    private static Logger logger = LoggerFactory.getLogger(Item.class);

    public List<Integer> sourceRowMap_;
    public List<Integer> proxyRowMap_;

    public Item(final WModelIndex sourceIndex) {
      super(sourceIndex);
      this.sourceRowMap_ = new ArrayList<Integer>();
      this.proxyRowMap_ = new ArrayList<Integer>();
    }
  }

  static class Compare implements Comparator<Integer> {
    private static Logger logger = LoggerFactory.getLogger(Compare.class);

    public Compare(WSortFilterProxyModel aModel, WSortFilterProxyModel.Item anItem) {
      super();
      this.model = aModel;
      this.item = anItem;
    }

    public int compare(Integer sourceRow1, Integer sourceRow2) {
      int factor = this.model.sortOrder_ == SortOrder.Ascending ? 1 : -1;
      if (this.model.sortKeyColumn_ == -1) {
        return factor * (sourceRow1 - sourceRow2);
      }
      WModelIndex lhs =
          this.model
              .getSourceModel()
              .getIndex(sourceRow1, this.model.sortKeyColumn_, this.item.sourceIndex_);
      WModelIndex rhs =
          this.model
              .getSourceModel()
              .getIndex(sourceRow2, this.model.sortKeyColumn_, this.item.sourceIndex_);
      return factor * this.model.compare(lhs, rhs);
    }

    public WSortFilterProxyModel model;
    public WSortFilterProxyModel.Item item;
  }

  private Pattern regex_;
  private int filterKeyColumn_;
  private ItemDataRole filterRole_;
  private int sortKeyColumn_;
  private ItemDataRole sortRole_;
  private SortOrder sortOrder_;
  private boolean dynamic_;
  private boolean inserting_;
  private List<AbstractSignal.Connection> modelConnections_;
  private SortedMap<WModelIndex, WAbstractProxyModel.BaseItem> mappedIndexes_;
  private WSortFilterProxyModel.Item mappedRootItem_;

  private void sourceColumnsAboutToBeInserted(final WModelIndex parent, int start, int end) {
    this.beginInsertColumns(this.mapFromSource(parent), start, end);
  }

  private void sourceColumnsInserted(final WModelIndex parent, int start, int end) {
    this.endInsertColumns();
  }

  private void sourceColumnsAboutToBeRemoved(final WModelIndex parent, int start, int end) {
    this.beginRemoveColumns(this.mapFromSource(parent), start, end);
  }

  private void sourceColumnsRemoved(final WModelIndex parent, int start, int end) {
    this.endRemoveColumns();
  }

  private void sourceRowsAboutToBeInserted(final WModelIndex parent, int start, int end) {
    if (this.inserting_) {
      return;
    }
    WModelIndex pparent = this.mapFromSource(parent);
    if ((parent != null) && !(pparent != null)) {
      return;
    }
    this.itemFromIndex(pparent);
  }

  private void sourceRowsInserted(final WModelIndex parent, int start, int end) {
    this.startShiftModelIndexes(parent, end + 1, end - start + 1, this.mappedIndexes_);
    if (this.inserting_) {
      return;
    }
    int count = end - start + 1;
    WModelIndex pparent = this.mapFromSource(parent);
    if ((parent != null) && !(pparent != null)) {
      return;
    }
    WSortFilterProxyModel.Item item = this.itemFromIndex(pparent);
    for (int i = 0; i < item.proxyRowMap_.size(); ++i) {
      if (item.proxyRowMap_.get(i) >= start) {
        item.proxyRowMap_.set(i, item.proxyRowMap_.get(i) + count);
      }
    }
    {
      int insertPos = 0 + start;
      for (int ii = 0; ii < (count); ++ii) item.sourceRowMap_.add(insertPos + ii, -1);
    }
    ;
    if (!this.dynamic_) {
      return;
    }
    for (int row = start; row <= end; ++row) {
      int newMappedRow = this.mappedInsertionPoint(row, item);
      if (newMappedRow != -1) {
        this.beginInsertRows(pparent, newMappedRow, newMappedRow);
        item.proxyRowMap_.add(0 + newMappedRow, row);
        this.rebuildSourceRowMap(item);
        this.endInsertRows();
      } else {
        item.sourceRowMap_.set(row, -1);
      }
    }
  }

  private void sourceRowsAboutToBeRemoved(final WModelIndex parent, int start, int end) {
    WModelIndex pparent = this.mapFromSource(parent);
    if ((parent != null) && !(pparent != null)) {
      return;
    }
    WSortFilterProxyModel.Item item = this.itemFromIndex(pparent);
    for (int row = start; row <= end; ++row) {
      int mappedRow = item.sourceRowMap_.get(row);
      if (mappedRow != -1) {
        this.beginRemoveRows(pparent, mappedRow, mappedRow);
        item.proxyRowMap_.remove(0 + mappedRow);
        this.rebuildSourceRowMap(item);
        this.endRemoveRows();
      }
    }
    int count = end - start + 1;
    this.startShiftModelIndexes(parent, start, -count, this.mappedIndexes_);
  }

  private void sourceRowsRemoved(final WModelIndex parent, int start, int end) {
    int count = end - start + 1;
    this.endShiftModelIndexes(parent, start, -count, this.mappedIndexes_);
    WModelIndex pparent = this.mapFromSource(parent);
    if ((parent != null) && !(pparent != null)) {
      return;
    }
    WSortFilterProxyModel.Item item = this.itemFromIndex(pparent);
    for (int i = 0; i < item.proxyRowMap_.size(); ++i) {
      if (item.proxyRowMap_.get(i) >= start) {
        item.proxyRowMap_.set(i, item.proxyRowMap_.get(i) - count);
      }
    }
    for (int ii = 0; ii < (0 + start + count) - (0 + start); ++ii)
      item.sourceRowMap_.remove(0 + start);
    ;
  }

  private void sourceDataChanged(final WModelIndex topLeft, final WModelIndex bottomRight) {
    if (!(topLeft != null) || !(bottomRight != null)) {
      return;
    }
    boolean refilter =
        this.dynamic_
            && (this.filterKeyColumn_ >= topLeft.getColumn()
                && this.filterKeyColumn_ <= bottomRight.getColumn());
    boolean resort =
        this.dynamic_
            && (this.sortKeyColumn_ >= topLeft.getColumn()
                && this.sortKeyColumn_ <= bottomRight.getColumn());
    WModelIndex parent = this.mapFromSource(topLeft.getParent());
    if ((topLeft.getParent() != null) && !(parent != null)) {
      return;
    }
    WSortFilterProxyModel.Item item = this.itemFromIndex(parent);
    for (int row = topLeft.getRow(); row <= bottomRight.getRow(); ++row) {
      int oldMappedRow = item.sourceRowMap_.get(row);
      boolean propagateDataChange = oldMappedRow != -1;
      if (refilter || resort) {
        if (oldMappedRow != -1) {
          item.proxyRowMap_.remove(0 + oldMappedRow);
        }
        int newMappedRow = this.mappedInsertionPoint(row, item);
        if (oldMappedRow != -1) {
          item.proxyRowMap_.add(0 + oldMappedRow, row);
        }
        if (newMappedRow != oldMappedRow) {
          if (oldMappedRow != -1) {
            this.beginRemoveRows(parent, oldMappedRow, oldMappedRow);
            item.proxyRowMap_.remove(0 + oldMappedRow);
            this.rebuildSourceRowMap(item);
            this.endRemoveRows();
          }
          if (newMappedRow != -1) {
            this.beginInsertRows(parent, newMappedRow, newMappedRow);
            item.proxyRowMap_.add(0 + newMappedRow, row);
            this.rebuildSourceRowMap(item);
            this.endInsertRows();
          }
          propagateDataChange = false;
        }
      }
      if (propagateDataChange) {
        WModelIndex l =
            this.getSourceModel().getIndex(row, topLeft.getColumn(), topLeft.getParent());
        WModelIndex r =
            this.getSourceModel().getIndex(row, bottomRight.getColumn(), topLeft.getParent());
        this.dataChanged().trigger(this.mapFromSource(l), this.mapFromSource(r));
      }
    }
  }

  private void sourceHeaderDataChanged(Orientation orientation, int start, int end) {
    if (orientation == Orientation.Vertical) {
      WSortFilterProxyModel.Item item = this.itemFromIndex(null);
      for (int row = start; row <= end; ++row) {
        int mappedRow = item.sourceRowMap_.get(row);
        if (mappedRow != -1) {
          this.headerDataChanged().trigger(orientation, mappedRow, mappedRow);
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

  private WSortFilterProxyModel.Item itemFromSourceIndex(final WModelIndex sourceParent) {
    if (!(sourceParent != null)) {
      if (!(this.mappedRootItem_ != null)) {
        WSortFilterProxyModel.Item result = new WSortFilterProxyModel.Item(sourceParent);
        this.mappedRootItem_ = result;
        this.updateItem(result);
      }
      return this.mappedRootItem_;
    }
    WAbstractProxyModel.BaseItem i = this.mappedIndexes_.get(sourceParent);
    if (i == null) {
      WSortFilterProxyModel.Item result = new WSortFilterProxyModel.Item(sourceParent);
      this.mappedIndexes_.put(sourceParent, result);
      this.updateItem(result);
      return result;
    } else {
      return ObjectUtils.cast(i, WSortFilterProxyModel.Item.class);
    }
  }

  private WSortFilterProxyModel.Item parentItemFromIndex(final WModelIndex index) {
    return (WSortFilterProxyModel.Item) index.getInternalPointer();
  }

  private WSortFilterProxyModel.Item itemFromIndex(final WModelIndex index) {
    if ((index != null)) {
      WSortFilterProxyModel.Item parentItem = this.parentItemFromIndex(index);
      WModelIndex sourceIndex =
          this.getSourceModel()
              .getIndex(
                  parentItem.proxyRowMap_.get(index.getRow()),
                  index.getColumn(),
                  parentItem.sourceIndex_);
      return this.itemFromSourceIndex(sourceIndex);
    } else {
      return this.itemFromSourceIndex(null);
    }
  }

  private void resetMappings() {
    for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> i_it =
            this.mappedIndexes_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> i = i_it.next();
    }
    this.mappedIndexes_.clear();

    this.mappedRootItem_ = null;
  }

  private void updateItem(WSortFilterProxyModel.Item item) {
    int sourceRowCount = this.getSourceModel().getRowCount(item.sourceIndex_);
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
      Collections.sort(item.proxyRowMap_, new WSortFilterProxyModel.Compare(this, item));
      this.rebuildSourceRowMap(item);
    }
  }

  private void rebuildSourceRowMap(WSortFilterProxyModel.Item item) {
    for (int i = 0; i < item.sourceRowMap_.size(); ++i) {
      item.sourceRowMap_.set(i, -1);
    }
    for (int i = 0; i < item.proxyRowMap_.size(); ++i) {
      item.sourceRowMap_.set(item.proxyRowMap_.get(i), i);
    }
  }

  private int mappedInsertionPoint(int sourceRow, WSortFilterProxyModel.Item item) {
    boolean acceptRow = this.filterAcceptRow(sourceRow, item.sourceIndex_);
    if (!acceptRow) {
      return -1;
    } else {
      return CollectionUtils.insertionPoint(
          item.proxyRowMap_, sourceRow, new WSortFilterProxyModel.Compare(this, item));
    }
  }
}
