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
 * A table row.
 *
 * <p>A WTableRow is returned by {@link WTable#getRowAt(int row) WTable#getRowAt()} and managing
 * various properties of a single row in a table (it is however not a widget).
 *
 * <p>A table row corresponds to the HTML <code>&lt;tr&gt;</code> tag.
 *
 * <p>
 *
 * @see WTable
 * @see WTableColumn
 */
public class WTableRow extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WTableRow.class);

  /**
   * Creates a new table row.
   *
   * <p>Table rows must be added to a table using {@link WTable#insertRow(int row, WTableRow
   * tableRow) WTable#insertRow()} before you can access contents in it using {@link
   * WTableRow#elementAt(int column) elementAt()}.
   */
  public WTableRow() {
    super();
    this.table_ = null;
    this.cells_ = new ArrayList<WTableCell>();
    this.flags_ = new BitSet();
    this.height_ = new WLength();
    this.id_ = "";
    this.styleClass_ = "";
    // this.implementStateless(WTableRow.hide,WTableRow.undoHide);
    // this.implementStateless(WTableRow.show,WTableRow.undoHide);
  }
  /**
   * Returns the table to which this row belongs.
   *
   * <p>
   *
   * @see WTable#getRowAt(int row)
   */
  public WTable getTable() {
    return this.table_;
  }
  /**
   * Access the row element at the given column.
   *
   * <p>Like {@link WTable#getElementAt(int row, int column) WTable#getElementAt()}, if the column
   * is beyond the current table dimensions, then the table is expanded automatically.
   *
   * <p>The row must be inserted within a table first.
   */
  public WTableCell elementAt(int column) {
    if (this.table_ != null) {
      return this.table_.getElementAt(this.getRowNum(), column);
    } else {
      this.expand(column + 1);
      return this.cells_.get(column);
    }
  }
  /**
   * Returns the row number of this row in the table.
   *
   * <p>Returns -1 if the row is not yet part of a table.
   *
   * <p>
   *
   * @see WTable#getRowAt(int row)
   */
  public int getRowNum() {
    if (this.table_ != null) {
      for (int i = 0; i < this.table_.rows_.size(); ++i) {
        if (this.table_.rows_.get(i) == this) {
          return i;
        }
      }
    }
    return -1;
  }
  /**
   * Sets the row height.
   *
   * <p>The default row height is {@link WLength#Auto}.
   *
   * <p>
   *
   * @see WTableRow#getHeight()
   * @see WWidget#resize(WLength width, WLength height)
   */
  public void setHeight(final WLength height) {
    this.height_ = height;
    if (this.table_ != null) {
      this.table_.repaintRow(this);
    }
  }
  /**
   * Returns the row height.
   *
   * <p>
   *
   * @see WTableRow#setHeight(WLength height)
   */
  public WLength getHeight() {
    return this.height_;
  }
  /**
   * Sets the CSS style class for this row.
   *
   * <p>The style is inherited by all table cells in this row.
   *
   * <p>
   *
   * @see WTableRow#getStyleClass()
   * @see WWidget#setStyleClass(String styleClass)
   */
  public void setStyleClass(final String style) {
    if (WWebWidget.canOptimizeUpdates() && style.equals(this.styleClass_)) {
      return;
    }
    this.styleClass_ = style;
    if (this.table_ != null) {
      this.table_.repaintRow(this);
    }
  }
  /**
   * Returns the CSS style class for this row.
   *
   * <p>
   *
   * @see WTableRow#getStyleClass()
   * @see WWidget#getStyleClass()
   */
  public String getStyleClass() {
    return this.styleClass_;
  }

  public void addStyleClass(final String style) {
    String currentClass = this.styleClass_;
    Set<String> classes = new HashSet<String>();
    StringUtils.split(classes, currentClass, " ", true);
    if (classes.contains(style) == false) {
      this.styleClass_ = StringUtils.addWord(this.styleClass_, style);
      if (this.table_ != null) {
        this.table_.repaintRow(this);
      }
    }
  }

  public void removeStyleClass(final String style) {
    String currentClass = this.styleClass_;
    Set<String> classes = new HashSet<String>();
    StringUtils.split(classes, currentClass, " ", true);
    if (classes.contains(style) != false) {
      this.styleClass_ = StringUtils.eraseWord(this.styleClass_, style);
      if (this.table_ != null) {
        this.table_.repaintRow(this);
      }
    }
  }

  public void toggleStyleClass(final String style, boolean add) {
    if (add) {
      this.addStyleClass(style);
    } else {
      this.removeStyleClass(style);
    }
  }
  /**
   * Sets whether the row must be hidden.
   *
   * <p>Hide or show the row.
   *
   * <p>The default value is <code>false</code> (row is not hidden).
   *
   * <p>
   *
   * @see WTableRow#hide()
   * @see WTableRow#show()
   */
  public void setHidden(boolean how) {
    if (WWebWidget.canOptimizeUpdates() && this.flags_.get(BIT_HIDDEN) == how) {
      return;
    }
    this.flags_.set(BIT_WAS_HIDDEN, this.flags_.get(BIT_HIDDEN));
    this.flags_.set(BIT_HIDDEN, how);
    this.flags_.set(BIT_HIDDEN_CHANGED);
    if (this.table_ != null) {
      this.table_.repaintRow(this);
    }
  }
  /**
   * Returns whether the rows is hidden.
   *
   * <p>
   *
   * @see WTableRow#setHidden(boolean how)
   */
  public boolean isHidden() {
    return this.flags_.get(BIT_HIDDEN);
  }
  /**
   * Hides the row.
   *
   * <p>
   *
   * @see WTableRow#setHidden(boolean how)
   */
  public void hide() {
    this.setHidden(true);
  }
  /**
   * Shows the row.
   *
   * <p>
   *
   * @see WTableRow#setHidden(boolean how)
   */
  public void show() {
    this.setHidden(false);
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
    this.id_ = id;
  }

  public String getId() {
    if (this.id_.length() != 0) {
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
        this.table_.repaintRow(this);
      }
    }
  }

  WTableCell createCell(int column) {
    if (this.table_ != null) {
      return this.table_.createCell(this.getRowNum(), column);
    } else {
      return new WTableCell();
    }
  }

  void expand(int numCells) {
    int cursize = this.cells_.size();
    for (int col = cursize; col < numCells; ++col) {
      this.cells_.add(this.createCell(col));
      WTableCell cell = this.cells_.get(this.cells_.size() - 1);
      if (this.table_ != null) {
        this.table_.widgetAdded(cell);
      }
      cell.row_ = this;
      cell.column_ = col;
    }
  }

  WTable table_;
  List<WTableCell> cells_;
  private static final int BIT_HIDDEN = 0;
  private static final int BIT_WAS_HIDDEN = 1;
  private static final int BIT_HIDDEN_CHANGED = 2;
  private static final int BIT_OBJECT_NAME_CHANGED = 3;
  private BitSet flags_;
  private WLength height_;
  private String id_;
  private String styleClass_;

  void updateDom(final DomElement element, boolean all) {
    if (!this.height_.isAuto()) {
      element.setProperty(Property.StyleHeight, this.height_.getCssText());
    }
    if (!all || this.styleClass_.length() != 0) {
      element.setProperty(Property.Class, this.styleClass_);
    }
    if (all && this.flags_.get(BIT_HIDDEN) || !all && this.flags_.get(BIT_HIDDEN_CHANGED)) {
      element.setProperty(Property.StyleDisplay, this.flags_.get(BIT_HIDDEN) ? "none" : "");
      this.flags_.clear(BIT_HIDDEN_CHANGED);
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

  void setTable(WTable table) {
    this.table_ = table;
  }

  void insertColumn(int column) {
    this.cells_.add(0 + column, this.createCell(column));
    WTableCell cell = this.cells_.get(column);
    if (this.table_ != null) {
      this.table_.widgetAdded(cell);
    }
    cell.row_ = this;
    cell.column_ = column;
    for (int i = column; i < this.cells_.size(); ++i) {
      this.cells_.get(i).column_ = i;
    }
  }

  WTableCell removeColumn(int column) {
    final WTableCell result = this.cells_.get(column);
    this.cells_.remove(0 + column);
    for (int i = column; i < this.cells_.size(); ++i) {
      this.cells_.get(i).column_ = i;
    }
    if (this.table_ != null) {
      this.table_.widgetRemoved(result, false);
    }
    return result;
  }

  private void undoHide() {
    this.setHidden(this.flags_.get(BIT_WAS_HIDDEN));
  }
}
