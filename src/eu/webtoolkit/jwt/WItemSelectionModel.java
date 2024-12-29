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
 * A class that represents a selection for a {@link WAbstractItemView}.
 *
 * <p>This model is currently only used by {@link WTreeView}, and plays only a role in drag &amp;
 * drop of an item selection.
 *
 * <p>When an item selection is dragged from a view widget, the generated drop events will have as
 * source object (see {@link WDropEvent#getSource()}) this selection model.
 *
 * <p>Although this class does not (yet) allow you to modify the selection, note that manipulations
 * to the model may modify the selection (row insertions and removals may shift the selection, and
 * row deletions may shrink the selection).
 *
 * <p>
 *
 * <p><i><b>Note: </b>Currently this class cannot be shared between multiple views. </i>
 *
 * @see WTreeView
 * @see WTableView
 * @see WAbstractItemModel
 */
public class WItemSelectionModel extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WItemSelectionModel.class);

  /** Returns the {@link WAbstractItemModel}. */
  public WAbstractItemModel getModel() {
    return this.model_;
  }
  /**
   * Returns the set of selected items.
   *
   * <p>The model indexes are returned as a set, topologically ordered (in the order they appear in
   * the view).
   *
   * <p>When selection operates on rows ({@link SelectionBehavior#Rows}), this method only returns
   * the model index of first column&apos;s element of the selected rows.
   */
  public SortedSet<WModelIndex> getSelectedIndexes() {
    return this.selection_;
  }
  /**
   * Returns wheter an item is selected.
   *
   * <p>When selection operates on rows ({@link SelectionBehavior#Rows}), this method returns true
   * for each element in a selected row.
   *
   * <p>
   *
   * @see WItemSelectionModel#getSelectedIndexes()
   */
  public boolean isSelected(final WModelIndex index) {
    if (this.selectionBehavior_ == SelectionBehavior.Rows) {
      for (Iterator<WModelIndex> it_it = this.selection_.iterator(); it_it.hasNext(); ) {
        WModelIndex it = it_it.next();
        WModelIndex mi = it;
        if (mi.getRow() == index.getRow()
            && (mi.getParent() == index.getParent()
                || (mi.getParent() != null && mi.getParent().equals(index.getParent())))) {
          return true;
        }
      }
      return false;
    } else {
      return this.selection_.contains(index) != false;
    }
  }
  /**
   * Sets the selection behaviour.
   *
   * <p>By default, the selection contains rows ({@link SelectionBehavior#Rows}), in which case
   * model indexes will always be have column 0, but represent the whole row.
   *
   * <p>Alternatively, you can allow selection for individual items ({@link
   * SelectionBehavior#Items}).
   */
  public void setSelectionBehavior(SelectionBehavior behavior) {
    this.selectionBehavior_ = behavior;
  }
  /**
   * Returns the selection behaviour.
   *
   * <p>
   *
   * @see WItemSelectionModel#setSelectionBehavior(SelectionBehavior behavior)
   */
  public SelectionBehavior getSelectionBehavior() {
    return this.selectionBehavior_;
  }
  /**
   * Returns the selection mime type.
   *
   * <p>This should return the mime type for the current selection, or an emtpy string if the
   * selection cannot be dragged.
   *
   * <p>The default implementation returns the mime type based on {@link ItemDataRole#MimeType} data
   * if all selected items indicate the same mime type, or the model {@link
   * WItemSelectionModel#getMimeType() getMimeType()} otherwise.
   *
   * <p>If one or more items indicate that they cannot be dragged, then an empty string is returned.
   */
  public String getMimeType() {
    String retval = "";
    for (Iterator<WModelIndex> i_it = this.selection_.iterator(); i_it.hasNext(); ) {
      WModelIndex i = i_it.next();
      WModelIndex mi = i;
      if (!!EnumUtils.mask(mi.getFlags(), ItemFlag.DragEnabled).isEmpty()) {
        return "";
      }
      Object mimeTypeData = mi.getData(ItemDataRole.MimeType);
      if ((mimeTypeData != null)) {
        String currentMimeType = StringUtils.asString(mimeTypeData).toString();
        if (currentMimeType.length() != 0) {
          if (retval.length() == 0) {
            retval = currentMimeType;
          } else {
            if (!currentMimeType.equals(retval)) {
              return this.model_.getMimeType();
            }
          }
        }
      }
    }
    if (retval.length() == 0) {
      return this.selection_.isEmpty() ? "" : this.model_.getMimeType();
    } else {
      return retval;
    }
  }

  SortedSet<WModelIndex> selection_;
  private WAbstractItemModel model_;
  private SelectionBehavior selectionBehavior_;

  WItemSelectionModel() {
    super();
    this.selection_ = new TreeSet<WModelIndex>();
    this.model_ = null;
    this.selectionBehavior_ = SelectionBehavior.Rows;
  }

  WItemSelectionModel(final WAbstractItemModel model) {
    super();
    this.selection_ = new TreeSet<WModelIndex>();
    this.model_ = model;
    this.selectionBehavior_ = SelectionBehavior.Rows;
  }

  void modelLayoutAboutToBeChanged() {
    WModelIndex.encodeAsRawIndexes(this.selection_);
  }

  void modelLayoutChanged() {
    this.selection_ = WModelIndex.decodeFromRawIndexes(this.selection_);
  }
}
