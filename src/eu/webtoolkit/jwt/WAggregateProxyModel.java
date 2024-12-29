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
 * A proxy model for JWt&apos;s item models that provides column aggregation.
 *
 * <p>This proxy model does not store data itself, but presents data from a source model, and
 * presents methods to organize columns using aggregation, through which a user may navigate
 * (usually to obtain more detailed results related to a single aggregate quantity).
 *
 * <p>To use this proxy model, you should provide a source model using {@link
 * WAggregateProxyModel#setSourceModel(WAbstractItemModel model) setSourceModel()}, and define
 * column ranges that can be aggregated using {@link WAggregateProxyModel#addAggregate(int
 * parentColumn, int firstColumn, int lastColumn) addAggregate()}.
 *
 * <p>This example would render like this:
 *
 * <p><div align="center"> <img src="doc-files/WAggregateProxyModel-1.png">
 *
 * <p><strong>A WTreeView using a WAggregateProxyModel</strong> </div>
 *
 * <p>
 *
 * <p><i><b>Note: </b>This model does not support dynamic changes to the column definition of the
 * source model (i.e. insertions or deletions of source model columns). </i>
 */
public class WAggregateProxyModel extends WAbstractProxyModel {
  private static Logger logger = LoggerFactory.getLogger(WAggregateProxyModel.class);

  /**
   * Constructor.
   *
   * <p>Sets up the proxy without aggregation functionality.
   */
  public WAggregateProxyModel() {
    super();
    this.topLevel_ = new WAggregateProxyModel.Aggregate();
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
  }
  /**
   * Adds a new column aggregation definition.
   *
   * <p>The <code>parentColumn</code> is the column index in the source model that acts as an
   * aggregate for columns <code>firstColumn</code> to <code>lastColumn</code>. <code>parentColumn
   * </code> must border the range defined by <code>firstColumn</code> to <code>lastColumn:</code>
   *
   * <pre>{@code
   * parentColumn == firstColumn - 1 || parentColumn == lastColumn + 1
   *
   * }</pre>
   *
   * <p>Note that column parameters reference column indexes in the source model.
   *
   * <p>Aggregation definitions can be nested, but should be strictly hierarchical.
   *
   * <p>The aggregate column will initially be collapsed.
   *
   * <p>Only one aggregate can be defined per <code>parentColumn</code>.
   */
  public void addAggregate(int parentColumn, int firstColumn, int lastColumn) {
    WAggregateProxyModel.Aggregate added =
        this.topLevel_.add(
            new WAggregateProxyModel.Aggregate(parentColumn, firstColumn, lastColumn));
    this.collapse(added);
  }

  public WModelIndex mapFromSource(final WModelIndex sourceIndex) {
    if ((sourceIndex != null)) {
      int column = this.topLevel_.mapFromSource(sourceIndex.getColumn());
      if (column >= 0) {
        int row = sourceIndex.getRow();
        return this.createIndex(row, column, sourceIndex.getInternalPointer());
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public WModelIndex mapToSource(final WModelIndex proxyIndex) {
    if ((proxyIndex != null)) {
      int column = this.topLevel_.mapToSource(proxyIndex.getColumn());
      int row = proxyIndex.getRow();
      return this.createSourceIndex(row, column, proxyIndex.getInternalPointer());
    } else {
      return null;
    }
  }

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
                  WAggregateProxyModel.this.sourceColumnsAboutToBeInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WAggregateProxyModel.this.sourceColumnsInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsAboutToBeRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WAggregateProxyModel.this.sourceColumnsAboutToBeRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .columnsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WAggregateProxyModel.this.sourceColumnsRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsAboutToBeInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WAggregateProxyModel.this.sourceRowsAboutToBeInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsInserted()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WAggregateProxyModel.this.sourceRowsInserted(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsAboutToBeRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WAggregateProxyModel.this.sourceRowsAboutToBeRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .rowsRemoved()
            .addListener(
                this,
                (WModelIndex e1, Integer e2, Integer e3) -> {
                  WAggregateProxyModel.this.sourceRowsRemoved(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .dataChanged()
            .addListener(
                this,
                (WModelIndex e1, WModelIndex e2) -> {
                  WAggregateProxyModel.this.sourceDataChanged(e1, e2);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .headerDataChanged()
            .addListener(
                this,
                (Orientation e1, Integer e2, Integer e3) -> {
                  WAggregateProxyModel.this.sourceHeaderDataChanged(e1, e2, e3);
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .layoutAboutToBeChanged()
            .addListener(
                this,
                () -> {
                  WAggregateProxyModel.this.sourceLayoutAboutToBeChanged();
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .layoutChanged()
            .addListener(
                this,
                () -> {
                  WAggregateProxyModel.this.sourceLayoutChanged();
                }));
    this.modelConnections_.add(
        this.getSourceModel()
            .modelReset()
            .addListener(
                this,
                () -> {
                  WAggregateProxyModel.this.sourceModelReset();
                }));
    this.topLevel_ = new WAggregateProxyModel.Aggregate();
  }

  public void expandColumn(int column) {
    int sourceColumn = this.topLevel_.mapToSource(column);
    WAggregateProxyModel.Aggregate ag = this.topLevel_.findAggregate(sourceColumn);
    if (ag != null) {
      this.expand(ag);
    }
  }

  public void collapseColumn(int column) {
    int sourceColumn = this.topLevel_.mapToSource(column);
    WAggregateProxyModel.Aggregate ag = this.topLevel_.findAggregate(sourceColumn);
    if (ag != null) {
      this.collapse(ag);
    }
  }

  public int getColumnCount(final WModelIndex parent) {
    int c = this.getSourceModel().getColumnCount(this.mapToSource(parent));
    if (c > 0) {
      c = this.lastVisibleSourceNotAfter(c - 1);
      return this.topLevel_.mapFromSource(c) + 1;
    } else {
      return 0;
    }
  }

  public int getRowCount(final WModelIndex parent) {
    return this.getSourceModel().getRowCount(this.mapToSource(parent));
  }

  public EnumSet<HeaderFlag> getHeaderFlags(int section, Orientation orientation) {
    if (orientation == Orientation.Horizontal) {
      int srcColumn = this.topLevel_.mapToSource(section);
      EnumSet<HeaderFlag> result = this.getSourceModel().getHeaderFlags(srcColumn, orientation);
      WAggregateProxyModel.Aggregate agg = this.topLevel_.findAggregate(srcColumn);
      if (agg != null) {
        if (agg.collapsed_) {
          return EnumUtils.or(result, HeaderFlag.ColumnIsCollapsed);
        } else {
          if (agg.parentSrc_ == agg.lastChildSrc_ + 1) {
            return EnumUtils.or(result, HeaderFlag.ColumnIsExpandedLeft);
          } else {
            return EnumUtils.or(result, HeaderFlag.ColumnIsExpandedRight);
          }
        }
      } else {
        return result;
      }
    } else {
      return this.getSourceModel().getHeaderFlags(section, orientation);
    }
  }

  public boolean setHeaderData(
      int section, Orientation orientation, final Object value, ItemDataRole role) {
    if (orientation == Orientation.Horizontal) {
      section = this.topLevel_.mapToSource(section);
    }
    return this.getSourceModel().setHeaderData(section, orientation, value, role);
  }

  public Object getHeaderData(int section, Orientation orientation, ItemDataRole role) {
    if (orientation == Orientation.Horizontal) {
      section = this.topLevel_.mapToSource(section);
      if (role.equals(ItemDataRole.Level)) {
        WAggregateProxyModel.Aggregate agg = this.topLevel_.findEnclosingAggregate(section);
        return agg.level_;
      } else {
        return this.getSourceModel().getHeaderData(section, orientation, role);
      }
    } else {
      return this.getSourceModel().getHeaderData(section, orientation, role);
    }
  }

  public WModelIndex getParent(final WModelIndex index) {
    if ((index != null)) {
      return this.mapFromSource(this.mapToSource(index).getParent());
    } else {
      return null;
    }
  }

  public WModelIndex getIndex(int row, int column, final WModelIndex parent) {
    WModelIndex sourceParent = this.mapToSource(parent);
    int sourceRow = row;
    int sourceColumn = this.topLevel_.mapToSource(column);
    WModelIndex sourceIndex = this.getSourceModel().getIndex(sourceRow, sourceColumn, sourceParent);
    return this.createIndex(
        row, column, (sourceIndex != null) ? sourceIndex.getInternalPointer() : null);
  }

  public void sort(int column, SortOrder order) {
    this.getSourceModel().sort(this.topLevel_.mapToSource(column), order);
  }

  static class Aggregate {
    private static Logger logger = LoggerFactory.getLogger(Aggregate.class);

    public int parentSrc_;
    public int firstChildSrc_;
    public int lastChildSrc_;
    public int level_;
    public boolean collapsed_;
    public List<WAggregateProxyModel.Aggregate> nestedAggregates_;

    public Aggregate() {
      this.parentSrc_ = -1;
      this.firstChildSrc_ = -1;
      this.lastChildSrc_ = -1;
      this.level_ = 0;
      this.collapsed_ = false;
      this.nestedAggregates_ = new ArrayList<WAggregateProxyModel.Aggregate>();
    }

    public Aggregate(int parentColumn, int firstColumn, int lastColumn) {
      this.parentSrc_ = parentColumn;
      this.firstChildSrc_ = firstColumn;
      this.lastChildSrc_ = lastColumn;
      this.level_ = 0;
      this.collapsed_ = false;
      this.nestedAggregates_ = new ArrayList<WAggregateProxyModel.Aggregate>();
      if (this.parentSrc_ != this.firstChildSrc_ - 1 && this.parentSrc_ != this.lastChildSrc_ + 1) {
        throw new WException(
            "WAggregateProxyModel::addAggregate: parent column must border children range");
      }
    }

    public boolean contains(final WAggregateProxyModel.Aggregate other) {
      int pa = this.parentSrc_;
      int a1 = this.firstChildSrc_;
      int a2 = this.lastChildSrc_;
      int pb = other.parentSrc_;
      int b1 = other.firstChildSrc_;
      int b2 = other.lastChildSrc_;
      if (pb >= a1 && pb <= a2) {
        if (!contains2(a1, a2, b1, b2)) {
          throw new WException(nestingError(pa, a1, a2, pb, b1, b2));
        }
        return true;
      } else {
        if (overlaps(a1, a2, b1, b2)) {
          throw new WException(nestingError(pa, a1, a2, pb, b1, b2));
        }
        return false;
      }
    }

    public boolean contains(int sourceColumn) {
      return this.firstChildSrc_ <= sourceColumn && sourceColumn <= this.lastChildSrc_;
    }

    public WAggregateProxyModel.Aggregate add(final WAggregateProxyModel.Aggregate toAdd) {
      for (int i = 0; i < this.nestedAggregates_.size(); ++i) {
        final WAggregateProxyModel.Aggregate a = this.nestedAggregates_.get(i);
        if (a.contains(toAdd)) {
          return a.add(toAdd);
        }
        if (toAdd.before(a)) {
          this.nestedAggregates_.add(0 + i, toAdd);
          this.nestedAggregates_.get(i).level_ = this.level_ + 1;
          return this.nestedAggregates_.get(i);
        }
      }
      this.nestedAggregates_.add(toAdd);
      this.nestedAggregates_.get(this.nestedAggregates_.size() - 1).level_ = this.level_ + 1;
      return this.nestedAggregates_.get(this.nestedAggregates_.size() - 1);
    }

    public int mapFromSource(int sourceColumn) {
      int collapsedCount = 0;
      for (int i = 0; i < this.nestedAggregates_.size(); ++i) {
        final WAggregateProxyModel.Aggregate a = this.nestedAggregates_.get(i);
        if (a.after(sourceColumn)) {
          return sourceColumn - collapsedCount;
        } else {
          if (a.contains(sourceColumn)) {
            if (a.collapsed_) {
              return -1;
            } else {
              return a.mapFromSource(sourceColumn) - collapsedCount;
            }
          } else {
            collapsedCount += a.getCollapsedCount();
          }
        }
      }
      return sourceColumn - collapsedCount;
    }

    public int mapToSource(int column) {
      int sourceColumn = column;
      for (int i = 0; i < this.nestedAggregates_.size(); ++i) {
        final WAggregateProxyModel.Aggregate a = this.nestedAggregates_.get(i);
        if (a.after(sourceColumn)) {
          return sourceColumn;
        } else {
          if (!a.collapsed_ && a.contains(sourceColumn)) {
            return a.mapToSource(sourceColumn);
          } else {
            sourceColumn += a.getCollapsedCount();
          }
        }
      }
      return sourceColumn;
    }

    public boolean before(final WAggregateProxyModel.Aggregate other) {
      return this.lastChildSrc_ < other.firstChildSrc_;
    }

    public boolean after(int column) {
      return this.firstChildSrc_ > column;
    }

    public boolean before(int column) {
      return this.lastChildSrc_ < column;
    }

    public int getCollapsedCount() {
      if (this.collapsed_) {
        return this.lastChildSrc_ - this.firstChildSrc_ + 1;
      } else {
        int result = 0;
        for (int i = 0; i < this.nestedAggregates_.size(); ++i) {
          final WAggregateProxyModel.Aggregate a = this.nestedAggregates_.get(i);
          result += a.getCollapsedCount();
        }
        return result;
      }
    }

    public WAggregateProxyModel.Aggregate findAggregate(int parentColumn) {
      if (this.parentSrc_ == parentColumn) {
        return this;
      } else {
        if (this.parentSrc_ != -1 && parentColumn > this.lastChildSrc_) {
          return null;
        } else {
          for (int i = 0; i < this.nestedAggregates_.size(); ++i) {
            final WAggregateProxyModel.Aggregate a = this.nestedAggregates_.get(i);
            WAggregateProxyModel.Aggregate result = a.findAggregate(parentColumn);
            if (result != null) {
              return result;
            }
          }
        }
      }
      return null;
    }

    public WAggregateProxyModel.Aggregate findEnclosingAggregate(int column) {
      for (int i = 0; i < this.nestedAggregates_.size(); ++i) {
        final WAggregateProxyModel.Aggregate a = this.nestedAggregates_.get(i);
        if (a.after(column)) {
          return this;
        }
        if (a.contains(column)) {
          return a.findEnclosingAggregate(column);
        }
      }
      return this;
    }

    public int firstVisibleNotBefore(int column) {
      if (this.collapsed_) {
        return this.lastChildSrc_ + 1;
      } else {
        for (int i = 0; i < this.nestedAggregates_.size(); ++i) {
          final WAggregateProxyModel.Aggregate a = this.nestedAggregates_.get(i);
          if (a.after(column)) {
            return column;
          } else {
            if (a.before(column)) {
              continue;
            } else {
              column = a.firstVisibleNotBefore(column);
            }
          }
        }
        return column;
      }
    }

    public int lastVisibleNotAfter(int column) {
      if (this.collapsed_) {
        return this.firstChildSrc_ - 1;
      } else {
        for (int i = this.nestedAggregates_.size() - 1; i >= 0; --i) {
          final WAggregateProxyModel.Aggregate a = this.nestedAggregates_.get(i);
          if (a.before(column)) {
            return column;
          } else {
            if (a.after(column)) {
              continue;
            } else {
              column = a.lastVisibleNotAfter(column);
            }
          }
        }
        return column;
      }
    }
  }

  private WAggregateProxyModel.Aggregate topLevel_;
  private List<AbstractSignal.Connection> modelConnections_;

  private void expand(final WAggregateProxyModel.Aggregate aggregate) {
    int c = this.topLevel_.mapFromSource(aggregate.parentSrc_);
    if (c >= 0) {
      aggregate.collapsed_ = false;
      int c1 =
          this.topLevel_.mapFromSource(this.firstVisibleSourceNotBefore(aggregate.firstChildSrc_));
      int c2 =
          this.topLevel_.mapFromSource(this.lastVisibleSourceNotAfter(aggregate.lastChildSrc_));
      aggregate.collapsed_ = true;
      this.propagateBeginInsert(null, c1, c2);
      aggregate.collapsed_ = false;
      this.propagateEndInsert(null, c1, c2);
    } else {
      aggregate.collapsed_ = false;
    }
  }

  private void collapse(final WAggregateProxyModel.Aggregate aggregate) {
    int c = this.topLevel_.mapFromSource(aggregate.parentSrc_);
    if (c >= 0) {
      int c1 =
          this.topLevel_.mapFromSource(this.firstVisibleSourceNotBefore(aggregate.firstChildSrc_));
      int c2 =
          this.topLevel_.mapFromSource(this.lastVisibleSourceNotAfter(aggregate.lastChildSrc_));
      this.propagateBeginRemove(null, c1, c2);
      aggregate.collapsed_ = true;
      this.propagateEndRemove(null, c1, c2);
    } else {
      aggregate.collapsed_ = true;
    }
  }

  private void propagateBeginRemove(final WModelIndex proxyIndex, int start, int end) {
    this.columnsAboutToBeRemoved().trigger(proxyIndex, start, end);
    int rc = this.getRowCount(proxyIndex);
    for (int i = 0; i < rc; ++i) {
      this.propagateBeginRemove(this.getIndex(i, 0, proxyIndex), start, end);
    }
  }

  private void propagateEndRemove(final WModelIndex proxyIndex, int start, int end) {
    this.columnsRemoved().trigger(proxyIndex, start, end);
    int rc = this.getRowCount(proxyIndex);
    for (int i = 0; i < rc; ++i) {
      this.propagateEndRemove(this.getIndex(i, 0, proxyIndex), start, end);
    }
  }

  private void propagateBeginInsert(final WModelIndex proxyIndex, int start, int end) {
    this.columnsAboutToBeInserted().trigger(proxyIndex, start, end);
    int rc = this.getRowCount(proxyIndex);
    for (int i = 0; i < rc; ++i) {
      this.propagateBeginInsert(this.getIndex(i, 0, proxyIndex), start, end);
    }
  }

  private void propagateEndInsert(final WModelIndex proxyIndex, int start, int end) {
    this.columnsInserted().trigger(proxyIndex, start, end);
    int rc = this.getRowCount(proxyIndex);
    for (int i = 0; i < rc; ++i) {
      this.propagateEndInsert(this.getIndex(i, 0, proxyIndex), start, end);
    }
  }

  private int lastVisibleSourceNotAfter(int column) {
    return this.topLevel_.lastVisibleNotAfter(column);
  }

  private int firstVisibleSourceNotBefore(int column) {
    return this.topLevel_.firstVisibleNotBefore(column);
  }

  private void sourceColumnsAboutToBeInserted(final WModelIndex parent, int start, int end) {
    throw new WException("WAggregateProxyModel does not support source model column insertion");
  }

  private void sourceColumnsInserted(final WModelIndex parent, int start, int end) {
    throw new WException("WAggregateProxyModel does not support source model column insertion");
  }

  private void sourceColumnsAboutToBeRemoved(final WModelIndex parent, int start, int end) {
    throw new WException("WAggregateProxyModel does not support source model column removal");
  }

  private void sourceColumnsRemoved(final WModelIndex parent, int start, int end) {
    throw new WException("WAggregateProxyModel does not support source model column removal");
  }

  private void sourceRowsAboutToBeInserted(final WModelIndex parent, int start, int end) {
    WModelIndex proxyParent = this.mapFromSource(parent);
    if ((proxyParent != null) || !(parent != null)) {
      this.beginInsertRows(proxyParent, start, end);
    }
  }

  private void sourceRowsInserted(final WModelIndex parent, int start, int end) {
    WModelIndex proxyParent = this.mapFromSource(parent);
    if ((proxyParent != null) || !(parent != null)) {
      this.endInsertRows();
    }
  }

  private void sourceRowsAboutToBeRemoved(final WModelIndex parent, int start, int end) {
    WModelIndex proxyParent = this.mapFromSource(parent);
    if ((proxyParent != null) || !(parent != null)) {
      this.beginRemoveRows(proxyParent, start, end);
    }
  }

  private void sourceRowsRemoved(final WModelIndex parent, int start, int end) {
    WModelIndex proxyParent = this.mapFromSource(parent);
    if ((proxyParent != null) || !(parent != null)) {
      this.endRemoveRows();
    }
  }

  private void sourceDataChanged(final WModelIndex topLeft, final WModelIndex bottomRight) {
    int l = this.firstVisibleSourceNotBefore(topLeft.getColumn());
    int r = this.lastVisibleSourceNotAfter(bottomRight.getColumn());
    if (r >= l) {
      WModelIndex tl =
          this.mapFromSource(
              this.getSourceModel().getIndex(topLeft.getRow(), l, topLeft.getParent()));
      WModelIndex br =
          this.mapFromSource(
              this.getSourceModel().getIndex(bottomRight.getRow(), r, bottomRight.getParent()));
      this.dataChanged().trigger(tl, br);
    }
  }

  private void sourceHeaderDataChanged(Orientation orientation, int start, int end) {
    if (orientation == Orientation.Vertical) {
      this.headerDataChanged().trigger(orientation, start, end);
    } else {
      int l = this.firstVisibleSourceNotBefore(start);
      int r = this.lastVisibleSourceNotAfter(end);
      if (r >= l) {
        l = this.topLevel_.mapFromSource(l);
        r = this.topLevel_.mapFromSource(r);
        this.headerDataChanged().trigger(orientation, l, r);
      }
    }
  }

  private void sourceLayoutAboutToBeChanged() {
    this.layoutAboutToBeChanged().trigger();
  }

  private void sourceLayoutChanged() {
    this.layoutChanged().trigger();
  }

  private void sourceModelReset() {
    this.topLevel_ = new WAggregateProxyModel.Aggregate();
    this.reset();
  }

  static boolean contains2(int a1, int a2, int b1, int b2) {
    return b1 >= a1 && b1 <= a2 && b2 >= a1 && b2 <= a2;
  }

  static boolean overlaps(int a1, int a2, int b1, int b2) {
    return !(a2 < b1 || a1 > b2);
  }

  static String nestingError(int pa, int a1, int a2, int pb, int b1, int b2) {
    StringWriter msg = new StringWriter();
    msg.append("WAggregateProxyModel: aggregates must strictly nest: [")
        .append(String.valueOf(pa))
        .append(": ")
        .append(String.valueOf(a1))
        .append(" - ")
        .append(String.valueOf(a2))
        .append("] overlaps partially with [")
        .append(String.valueOf(pb))
        .append(": ")
        .append(String.valueOf(b1))
        .append(" - ")
        .append(String.valueOf(b2))
        .append("]");
    return msg.toString();
  }
}
