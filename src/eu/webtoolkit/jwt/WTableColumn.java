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
 * A table column.
 *
 * <p>A WTableColumn is returned by {@link WTable#getColumnAt(int column) WTable#getColumnAt()} and
 * managing various properties of a single column in a table (it is however not a widget).
 *
 * <p>A table column corresponds to the HTML <code>&lt;col&gt;</code> tag.
 *
 * <p>
 *
 * @see WTable
 * @see WTableRow
 */
public class WTableColumn extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WTableColumn.class);

  /**
   * Creates a new table column.
   *
   * <p>Table columns must be added to a table using {@link WTable#insertColumn(int column,
   * WTableColumn tableColumn) WTable#insertColumn()} before you can access contents in it using
   * {@link WTableColumn#elementAt(int row) elementAt()}.
   */
  public WTableColumn() {
    super();
    this.flags_ = new BitSet();
    this.width_ = null;
    this.id_ = "";
    this.styleClass_ = "";
  }
  /**
   * Returns the table to which this column belongs.
   *
   * <p>
   *
   * @see WTable#getRowAt(int row)
   */
  public WTable getTable() {
    return this.table_;
  }
  /**
   * Access the column element at the given row.
   *
   * <p>Like {@link WTable#getElementAt(int row, int column) WTable#getElementAt()}, if the row is
   * beyond the current table dimensions, then the table is expanded automatically.
   *
   * <p>The column must be inserted within a table first.
   */
  public WTableCell elementAt(int row) {
    return this.table_.getElementAt(row, this.getColumnNum());
  }
  /**
   * Returns the column number of this column in the table.
   *
   * <p>Returns -1 if the column is not yet part of a table.
   *
   * <p>
   *
   * @see WTable#getColumnAt(int column)
   */
  public int getColumnNum() {
    for (int i = 0; i < this.table_.columns_.size(); i++) {
      if (this.table_.columns_.get(i) == this) {
        return i;
      }
    }
    return -1;
  }
  /**
   * Sets the column width.
   *
   * <p>The default column width is {@link WLength#Auto}.
   *
   * <p>
   *
   * @see WTableColumn#getWidth()
   * @see WWidget#resize(WLength width, WLength height)
   */
  public void setWidth(final WLength width) {
    this.width_ = width;
    this.table_.repaintColumn(this);
  }
  /**
   * Returns the column width.
   *
   * <p>
   *
   * @see WTableColumn#setWidth(WLength width)
   */
  public WLength getWidth() {
    return this.width_ != null ? this.width_ : WLength.Auto;
  }
  /**
   * Sets the CSS style class for this column.
   *
   * <p>The style is inherited by all table cells in this column.
   *
   * <p>
   *
   * @see WTableColumn#getStyleClass()
   * @see WWidget#setStyleClass(String styleClass)
   */
  public void setStyleClass(final String style) {
    if (WWebWidget.canOptimizeUpdates() && style.equals(this.styleClass_)) {
      return;
    }
    this.styleClass_ = style;
    this.table_.repaintColumn(this);
  }
  /**
   * Returns the CSS style class for this column.
   *
   * <p>
   *
   * @see WTableColumn#getStyleClass()
   * @see WWidget#getStyleClass()
   */
  public String getStyleClass() {
    return this.styleClass_;
  }
  /**
   * Sets the CSS Id.
   *
   * <p>Sets a custom Id. Note that the Id must be unique across the whole widget tree, can only be
   * set right after construction and cannot be changed.
   *
   * <p>
   *
   * @see WObject#getId()
   */
  public void setId(final String id) {
    if (!(this.id_ != null)) {
      this.id_ = "";
    }
    this.id_ = id;
  }

  public String getId() {
    if (this.id_ != null) {
      return this.id_;
    } else {
      return super.getId();
    }
  }

  public void setObjectName(final String name) {
    if (!this.getObjectName().equals(name)) {
      super.setObjectName(name);
      this.flags_.set(BIT_OBJECT_NAME_CHANGED);
      if (this.table_ != null) {
        this.table_.repaintColumn(this);
      }
    }
  }

  private static final int BIT_OBJECT_NAME_CHANGED = 0;
  private BitSet flags_;
  WTable table_;
  private WLength width_;
  private String id_;
  private String styleClass_;

  void setTable(WTable table) {
    this.table_ = table;
  }

  void updateDom(final DomElement element, boolean all) {
    if (this.width_ != null) {
      element.setProperty(Property.StyleWidth, this.width_.getCssText());
    }
    if (!all || this.styleClass_.length() != 0) {
      element.setProperty(Property.Class, this.styleClass_);
    }
    if (all || this.flags_.get(BIT_OBJECT_NAME_CHANGED)) {
      if (this.getObjectName().length() != 0) {
        element.setAttribute("data-object-name", this.getObjectName());
      } else {
        if (!all) {
          element.removeAttribute("data-object-name");
        }
      }
      this.flags_.clear(BIT_OBJECT_NAME_CHANGED);
    }
  }
}
