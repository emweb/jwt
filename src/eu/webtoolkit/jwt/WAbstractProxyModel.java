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
 * An abstract proxy model for JWt&apos;s item models.
 *
 * <p>A proxy model does not store data, but presents data from a source model in another way. It
 * may provide filtering, sorting, or other computed changes to the source model. A proxy model may
 * be a fully functional model, that also allows modification of the underlying model.
 *
 * <p>This abstract proxy model may be used as a starting point for implementing a custom proxy
 * model, when {@link WSortFilterProxyModel} is not adequate. It implements data access and
 * manipulation using the a virtual mapping method ({@link
 * WAbstractProxyModel#mapToSource(WModelIndex proxyIndex) mapToSource()}) to access and manipulate
 * the underlying {@link WAbstractProxyModel#getSourceModel() getSourceModel()}.
 */
public abstract class WAbstractProxyModel extends WAbstractItemModel {
  private static Logger logger = LoggerFactory.getLogger(WAbstractProxyModel.class);

  /** Constructor. */
  public WAbstractProxyModel() {
    super();
    this.itemsToShift_ = new ArrayList<WAbstractProxyModel.BaseItem>();
    this.sourceModel_ = (WAbstractItemModel) null;
  }
  /**
   * Maps a source model index to the proxy model.
   *
   * <p>This method returns a model index in the proxy model that corresponds to the model index
   * <code>sourceIndex</code> in the source model. This method must only be implemented for source
   * model indexes that are mapped and thus are the result of {@link
   * WAbstractProxyModel#mapToSource(WModelIndex proxyIndex) mapToSource()}.
   *
   * <p>
   *
   * @see WAbstractProxyModel#mapToSource(WModelIndex proxyIndex)
   */
  public abstract WModelIndex mapFromSource(final WModelIndex sourceIndex);
  /**
   * Maps a proxy model index to the source model.
   *
   * <p>This method returns a model index in the source model that corresponds to the proxy model
   * index <code>proxyIndex</code>.
   *
   * <p>
   *
   * @see WAbstractProxyModel#mapFromSource(WModelIndex sourceIndex)
   */
  public abstract WModelIndex mapToSource(final WModelIndex proxyIndex);
  /**
   * Sets the source model.
   *
   * <p>The source model provides the actual data for the proxy model.
   *
   * <p>Ownership of the source model is <i>not</i> transferred.
   *
   * <p>Note that the source model&apos;s signals are not forwarded to the proxy model by default,
   * but some specializations, like {@link WBatchEditProxyModel} and {@link WSortFilterProxyModel}
   * do. If you want to reimplement data() with no changes to row or column indices, consider the
   * use of {@link WIdentityProxyModel}.
   */
  public void setSourceModel(final WAbstractItemModel sourceModel) {
    this.sourceModel_ = sourceModel;
  }
  /**
   * Returns the source model.
   *
   * <p>
   *
   * @see WAbstractProxyModel#setSourceModel(WAbstractItemModel sourceModel)
   */
  public WAbstractItemModel getSourceModel() {
    return this.sourceModel_;
  }
  /**
   * Returns the data at a specific model index.
   *
   * <p>The default proxy implementation translates the index to the source model, and calls {@link
   * WAbstractProxyModel#getSourceModel() getSourceModel()}.data() with this index.
   */
  public Object getData(final WModelIndex index, ItemDataRole role) {
    return this.sourceModel_.getData(this.mapToSource(index), role);
  }
  /**
   * Returns the row or column header data.
   *
   * <p>The default proxy implementation constructs a dummy {@link WModelIndex} with the row set to
   * 0 and column set to <code>section</code> if the orientation is {@link Orientation#Horizontal},
   * or with the row set to <code>section</code> and the column set to 0 if the orientation is
   * {@link Orientation#Vertical}.
   *
   * <p>The resulting section that is forwarded to {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.{@link WAbstractProxyModel#getHeaderData(int section, Orientation
   * orientation, ItemDataRole role) getHeaderData()} depends on how the {@link WModelIndex} is
   * transformed with {@link WAbstractProxyModel#mapToSource(WModelIndex proxyIndex) mapToSource()}.
   */
  public Object getHeaderData(int section, Orientation orientation, ItemDataRole role) {
    if (orientation == Orientation.Horizontal) {
      section = this.mapToSource(this.getIndex(0, section, null)).getColumn();
    } else {
      section = this.mapToSource(this.getIndex(section, 0, null)).getRow();
    }
    return this.sourceModel_.getHeaderData(section, orientation, role);
  }
  /**
   * Sets the data at the given model index.
   *
   * <p>The default proxy implementation calls {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.setData(mapToSource(index), value, role)
   */
  public boolean setData(final WModelIndex index, final Object value, ItemDataRole role) {
    return this.sourceModel_.setData(this.mapToSource(index), value, role);
  }
  /**
   * Sets the data at the given model index.
   *
   * <p>The default proxy implementation calls {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.setData(mapToSource(index), values)
   */
  public boolean setItemData(
      final WModelIndex index, final SortedMap<ItemDataRole, Object> values) {
    return this.sourceModel_.setItemData(this.mapToSource(index), values);
  }
  /**
   * Returns the flags for an item.
   *
   * <p>The default proxy implementation calls {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.flags(mapToSource(index))
   */
  public EnumSet<ItemFlag> getFlags(final WModelIndex index) {
    return this.sourceModel_.getFlags(this.mapToSource(index));
  }
  /**
   * Returns the flags for a header.
   *
   * <p>The default proxy implementation constructs a dummy {@link WModelIndex} with the row set to
   * 0 and column set to <code>section</code> if the orientation is {@link Orientation#Horizontal},
   * or with the row set to <code>section</code> and the column set to 0 if the orientation is
   * {@link Orientation#Vertical}.
   *
   * <p>The resulting section that is forwarded to {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.{@link WAbstractProxyModel#getHeaderFlags(int section, Orientation
   * orientation) getHeaderFlags()} depends on how the {@link WModelIndex} is transformed with
   * {@link WAbstractProxyModel#mapToSource(WModelIndex proxyIndex) mapToSource()}.
   */
  public EnumSet<HeaderFlag> getHeaderFlags(int section, Orientation orientation) {
    if (orientation == Orientation.Horizontal) {
      section = this.mapToSource(this.getIndex(0, section, null)).getColumn();
    } else {
      section = this.mapToSource(this.getIndex(section, 0, null)).getRow();
    }
    return this.sourceModel_.getHeaderFlags(section, orientation);
  }
  /**
   * Inserts one or more columns.
   *
   * <p>The default proxy implementation calls {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.insertColumns(column, count, parent)
   */
  public boolean insertColumns(int column, int count, final WModelIndex parent) {
    return this.sourceModel_.insertColumns(column, count, parent);
  }
  /**
   * Removes columns.
   *
   * <p>The default proxy implementation calls {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.removeColumns(column, count, parent)
   */
  public boolean removeColumns(int column, int count, final WModelIndex parent) {
    return this.sourceModel_.removeColumns(column, count, parent);
  }
  /**
   * Returns a mime-type for dragging a set of indexes.
   *
   * <p>The default proxy implementation calls {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.{@link WAbstractProxyModel#getMimeType() getMimeType()}
   */
  public String getMimeType() {
    return this.sourceModel_.getMimeType();
  }
  /**
   * Returns a list of mime-types that could be accepted for a drop event.
   *
   * <p>The default proxy implementation calls {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.{@link WAbstractProxyModel#getAcceptDropMimeTypes() getAcceptDropMimeTypes()}
   */
  public List<String> getAcceptDropMimeTypes() {
    return this.sourceModel_.getAcceptDropMimeTypes();
  }
  /**
   * Handles a drop event.
   *
   * <p>The default proxy implementation maps the given row and parent to the row and parent in the
   * source model, and forwards the dropEvent call to the source model.
   */
  public void dropEvent(
      final WDropEvent e, DropAction action, int row, int column, final WModelIndex parent) {
    WModelIndex sourceParent = this.mapToSource(parent);
    int sourceRow = row;
    int sourceColumn = column;
    if (sourceRow != -1) {
      sourceRow = this.mapToSource(this.getIndex(row, 0, parent)).getRow();
    }
    this.sourceModel_.dropEvent(e, action, sourceRow, sourceColumn, sourceParent);
  }
  /**
   * Converts a model index to a raw pointer that remains valid while the model&apos;s layout is
   * changed.
   *
   * <p>The default proxy implementation calls {@link WAbstractProxyModel#getSourceModel()
   * getSourceModel()}.toRawIndex(mapToSource(index))
   */
  public Object toRawIndex(final WModelIndex index) {
    return this.sourceModel_.toRawIndex(this.mapToSource(index));
  }
  /**
   * Converts a raw pointer to a model index.
   *
   * <p>The default proxy implementation calls mapFromSource({@link
   * WAbstractProxyModel#getSourceModel() getSourceModel()}.fromRawIndex(rawIndex))
   */
  public WModelIndex fromRawIndex(Object rawIndex) {
    return this.mapFromSource(this.sourceModel_.fromRawIndex(rawIndex));
  }
  /**
   * Create a source model index.
   *
   * <p>This is a utility function that allows you to create indexes in the source model. In this
   * way, you can reuse the internal pointers of the source model in proxy model indexes, and
   * convert a proxy model index back to the source model index using this method.
   */
  protected WModelIndex createSourceIndex(int row, int column, Object ptr) {
    return this.sourceModel_.createIndex(row, column, ptr);
  }
  /**
   * A base class for an item modeling a source index parent.
   *
   * <p>Many mplementations of a proxy model will need to maintain a data structure per source model
   * indexes, where they relate source rows or columns to proxy rows or columns, per hierarchical
   * parent.
   *
   * <p>It may be convenient to start from this item class as a base class so that
   * shiftModelIndexes() can be used to update this data structure when the source model adds or
   * removes rows.
   *
   * <p>You will typically use your derived class of this item as the internal pointer for proxy
   * model indexes: a proxy model index will have an item as internal pointer whose sourceIndex_
   * corresponds to the source equivalent of the proxy model index parent.
   *
   * <p>
   *
   * @see WAbstractItemModel#createIndex(int row, int column, Object ptr)
   */
  protected static class BaseItem {
    private static Logger logger = LoggerFactory.getLogger(BaseItem.class);

    /**
     * The source model index.
     *
     * <p>The source model index for this item.
     */
    public WModelIndex sourceIndex_;
    /** Create a {@link BaseItem}. */
    public BaseItem(final WModelIndex sourceIndex) {
      this.sourceIndex_ = sourceIndex;
    }
  }
  /**
   * Utility methods to shift items in an item map.
   *
   * <p>You can use this method to adjust an item map after the source model has inserted or removed
   * rows. When removing rows (count &lt; 0), items may possibly be removed and deleted.
   */
  protected void startShiftModelIndexes(
      final WModelIndex sourceParent,
      int start,
      int count,
      final SortedMap<WModelIndex, WAbstractProxyModel.BaseItem> items) {
    List<WAbstractProxyModel.BaseItem> erased = new ArrayList<WAbstractProxyModel.BaseItem>();
    WModelIndex startIndex = null;
    if (this.getSourceModel().getRowCount(sourceParent) == 0) {
      startIndex = sourceParent;
    } else {
      if (start >= this.getSourceModel().getRowCount(sourceParent)) {
        return;
      } else {
        startIndex = this.getSourceModel().getIndex(start, 0, sourceParent);
      }
    }
    if (!(startIndex != null)) {
      return;
    }
    for (Iterator<Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem>> it_it =
            items.tailMap(startIndex).entrySet().iterator();
        it_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractProxyModel.BaseItem> it = it_it.next();
      WModelIndex i = it.getKey();
      if ((i == sourceParent || (i != null && i.equals(sourceParent)))) {
        continue;
      }
      if ((i != null)) {
        WModelIndex p = i.getParent();
        if (!(p == sourceParent || (p != null && p.equals(sourceParent)))
            && !WModelIndex.isAncestor(p, sourceParent)) {
          break;
        }
        if ((p == sourceParent || (p != null && p.equals(sourceParent)))) {
          if (count < 0 && i.getRow() >= start && i.getRow() < start + -count) {
            erased.add(it.getValue());
          } else {
            this.itemsToShift_.add(it.getValue());
          }
        } else {
          if (count < 0) {
            do {
              if ((p.getParent() == sourceParent
                      || (p.getParent() != null && p.getParent().equals(sourceParent)))
                  && p.getRow() >= start
                  && p.getRow() < start + -count) {
                erased.add(it.getValue());
                break;
              } else {
                p = p.getParent();
              }
            } while (!(p == sourceParent || (p != null && p.equals(sourceParent))));
          }
        }
      }
    }
    for (int i = 0; i < this.itemsToShift_.size(); ++i) {
      WAbstractProxyModel.BaseItem item = this.itemsToShift_.get(i);
      items.remove(item.sourceIndex_);
      item.sourceIndex_ =
          this.getSourceModel()
              .getIndex(
                  item.sourceIndex_.getRow() + count, item.sourceIndex_.getColumn(), sourceParent);
    }
    for (int i = 0; i < erased.size(); ++i) {
      items.remove(erased.get(i).sourceIndex_);
    }
    if (count > 0) {
      this.endShiftModelIndexes(sourceParent, start, count, items);
    }
  }

  protected void endShiftModelIndexes(
      final WModelIndex sourceParent,
      int start,
      int count,
      final SortedMap<WModelIndex, WAbstractProxyModel.BaseItem> items) {
    for (int i = 0; i < this.itemsToShift_.size(); ++i) {
      items.put(this.itemsToShift_.get(i).sourceIndex_, this.itemsToShift_.get(i));
    }
    this.itemsToShift_.clear();
  }

  private List<WAbstractProxyModel.BaseItem> itemsToShift_;
  private WAbstractItemModel sourceModel_;
}
