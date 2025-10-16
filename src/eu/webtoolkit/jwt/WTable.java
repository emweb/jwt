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
 * A container widget which provides layout of children in a table grid.
 *
 * <p>A WTable arranges its children in a table.
 *
 * <p>To insert or access contents, use {@link WTable#getElementAt(int row, int column)
 * getElementAt()} to access the {@link WTableCell cell} at a particular location in the table. The
 * WTable expands automatically to create the indexed (row, column) as necessary.
 *
 * <p>It is possible to insert and delete entire rows or columns from the table using the
 * insertColumn(int column), insertRow(int row), deleteColumn(int column), or deleteRow(int row)
 * methods.
 *
 * <p>You may indicate a number of rows and columns that act as headers using {@link
 * WTable#setHeaderCount(int count, Orientation orientation) setHeaderCount()}. Header cells are
 * rendered as <code>&lt;th&gt;</code> instead of <code>&lt;td&gt;</code> elements. By default, no
 * rows or columns are configured as headers.
 *
 * <p>WTable is displayed as a {@link WWidget#setInline(boolean inlined) block}.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>The widget corresponds to the HTML <code>&lt;table&gt;</code> tag and does not provide
 * styling. It can be styled using inline or external CSS as appropriate.
 *
 * <p>
 *
 * @see WTableCell
 * @see WTableRow
 * @see WTableColumn
 */
public class WTable extends WInteractWidget {
  private static Logger logger = LoggerFactory.getLogger(WTable.class);

  /** Creates an empty table. */
  public WTable(WContainerWidget parentContainer) {
    super();
    this.flags_ = new BitSet();
    this.rows_ = new ArrayList<WTableRow>();
    this.columns_ = new ArrayList<WTableColumn>();
    this.rowsChanged_ = new HashSet<WTableRow>();
    this.rowsAdded_ = 0;
    this.headerRowCount_ = 0;
    this.headerColumnCount_ = 0;
    this.setInline(false);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates an empty table.
   *
   * <p>Calls {@link #WTable(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WTable() {
    this((WContainerWidget) null);
  }
  /** Deletes the table and its entire contents. */
  public void remove() {
    this.beingDeleted();
    this.clear();
    super.remove();
  }
  /**
   * Accesses the table element at the given row and column.
   *
   * <p>If the row/column is beyond the current table dimensions, then the table is expanded
   * automatically.
   */
  public WTableCell getElementAt(int row, int column) {
    this.expand(row, column, 1, 1);
    return this.getItemAt(row, column);
  }
  /**
   * Returns the row object for the given row.
   *
   * <p>Like with {@link WTable#getElementAt(int row, int column) getElementAt()}, the table expands
   * automatically when the row is beyond the current table dimensions.
   *
   * <p>
   *
   * @see WTable#getElementAt(int row, int column)
   * @see WTable#getColumnAt(int column)
   */
  public WTableRow getRowAt(int row) {
    this.expand(row, 0, 1, 0);
    return this.rows_.get(row);
  }
  /**
   * Returns the column object for the given column.
   *
   * <p>Like with {@link WTable#getElementAt(int row, int column) getElementAt()}, the table expands
   * automatically when the column is beyond the current table dimensions.
   *
   * <p>
   *
   * @see WTable#getElementAt(int row, int column)
   * @see WTable#getRowAt(int row)
   */
  public WTableColumn getColumnAt(int column) {
    this.expand(0, column, 0, 1);
    return this.columns_.get(column);
  }
  /**
   * Deletes a table cell and its contents.
   *
   * <p>The table cell at that position is recreated.
   *
   * <p>
   *
   * @see WTable#removeCell(int row, int column)
   */
  public void removeCell(WTableCell item) {
    this.removeCell(item.getRow(), item.getColumn());
  }
  /**
   * Deletes the table cell at the given position.
   *
   * <p>
   *
   * @see WTable#removeCell(WTableCell item)
   */
  public void removeCell(int row, int column) {
    this.setItemAt(row, column, this.rows_.get(row).createCell(column));
  }
  /** Inserts a row. */
  public WTableRow insertRow(int row, WTableRow tableRow) {
    if (row == this.getRowCount() && this.getRowCount() >= this.headerRowCount_) {
      ++this.rowsAdded_;
    } else {
      this.flags_.set(BIT_GRID_CHANGED);
    }
    if (!(tableRow != null)) {
      tableRow = this.createRow(row);
    }
    tableRow.setTable(this);
    for (WTableCell cell : tableRow.cells_) {
      this.widgetAdded(cell);
    }
    this.rows_.add(0 + row, tableRow);
    this.rows_.get(row).expand(this.getColumnCount());
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    return this.rows_.get(row);
  }
  /**
   * Inserts a row.
   *
   * <p>Returns {@link #insertRow(int row, WTableRow tableRow) insertRow(row, null)}
   */
  public final WTableRow insertRow(int row) {
    return insertRow(row, null);
  }
  /**
   * Removes a row.
   *
   * <p>Rows below the given row are shifted up. Returns a {@link WTableRow} that is not associated
   * with a {@link WTable}. Unlinke {@link WTable#removeColumn(int column) removeColumn()}, the
   * cells in the row will not be deleted, because they are owned by the {@link WTableRow}.
   */
  public WTableRow removeRow(int row) {
    this.rowsChanged_.remove(this.rows_.get(row));
    if (row >= (int) (this.getRowCount() - this.rowsAdded_)) {
      --this.rowsAdded_;
    } else {
      this.flags_.set(BIT_GRID_CHANGED);
      this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    }
    WTableRow result = this.rows_.get(row);
    this.rows_.remove(0 + row);
    result.setTable((WTable) null);
    for (WTableCell cell : result.cells_) {
      this.widgetRemoved(cell, false);
    }
    return result;
  }
  /** Inserts an empty column. */
  public WTableColumn insertColumn(int column, WTableColumn tableColumn) {
    for (int i = 0; i < this.rows_.size(); ++i) {
      this.rows_.get(i).insertColumn(column);
    }
    if ((int) column <= this.columns_.size()) {
      if (!(tableColumn != null)) {
        tableColumn = this.createColumn(column);
      }
      tableColumn.setTable(this);
      this.columns_.add(0 + column, tableColumn);
    }
    this.flags_.set(BIT_GRID_CHANGED);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    return this.columns_.get(column);
  }
  /**
   * Inserts an empty column.
   *
   * <p>Returns {@link #insertColumn(int column, WTableColumn tableColumn) insertColumn(column,
   * null)}
   */
  public final WTableColumn insertColumn(int column) {
    return insertColumn(column, null);
  }
  /**
   * Remove a column and all its contents.
   *
   * <p>The contents of the column will be deleted, because a {@link WTableColumn} does not own its
   * cells.
   */
  public WTableColumn removeColumn(int column) {
    for (int i = 0; i < this.getRowCount(); ++i) {
      {
        WTableCell toRemove = this.rows_.get(i).removeColumn(column);
        if (toRemove != null) toRemove.remove();
      }
    }
    WTableColumn result = this.columns_.get(column);
    this.columns_.remove(0 + column);
    result.setTable((WTable) null);
    this.flags_.set(BIT_GRID_CHANGED);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
    return result;
  }
  /**
   * Clears the entire table.
   *
   * <p>This method clears the entire table: all cells and their contents are deleted.
   */
  public void clear() {
    while (this.getRowCount() > 0) {
      this.removeRow(this.getRowCount() - 1);
    }
    while (this.getColumnCount() > 0) {
      this.removeColumn(this.getColumnCount() - 1);
    }
  }
  /** Returns the number of rows in the table. */
  public int getRowCount() {
    return this.rows_.size();
  }
  /** Returns the number of columns in the table. */
  public int getColumnCount() {
    return this.columns_.size();
  }
  /**
   * Sets the number of header rows or columns.
   *
   * <p>The default values are 0.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This must be set before the initial rendering and cannot be changed later.
   * </i>
   */
  public void setHeaderCount(int count, Orientation orientation) {
    if (orientation == Orientation.Horizontal) {
      this.headerRowCount_ = count;
    } else {
      this.headerColumnCount_ = count;
    }
  }
  /**
   * Sets the number of header rows or columns.
   *
   * <p>Calls {@link #setHeaderCount(int count, Orientation orientation) setHeaderCount(count,
   * Orientation.Horizontal)}
   */
  public final void setHeaderCount(int count) {
    setHeaderCount(count, Orientation.Horizontal);
  }
  /**
   * Returns the number of header rows or columns.
   *
   * <p>
   *
   * @see WTable#setHeaderCount(int count, Orientation orientation)
   */
  public int getHeaderCount(Orientation orientation) {
    if (orientation == Orientation.Horizontal) {
      return this.headerRowCount_;
    } else {
      return this.headerColumnCount_;
    }
  }
  /**
   * Returns the number of header rows or columns.
   *
   * <p>Returns {@link #getHeaderCount(Orientation orientation)
   * getHeaderCount(Orientation.Horizontal)}
   */
  public final int getHeaderCount() {
    return getHeaderCount(Orientation.Horizontal);
  }
  /**
   * Move a table row from its original position to a new position.
   *
   * <p>The table expands automatically when the <code>to</code> row is beyond the current table
   * dimensions.
   *
   * <p>
   *
   * @see WTable#moveColumn(int from, int to)
   */
  public void moveRow(int from, int to) {
    if (from < 0 || from >= (int) this.rows_.size()) {
      logger.error(
          new StringWriter()
              .append("moveRow: the from index is not a valid row index.")
              .toString());
      return;
    }
    WTableRow from_tr = CollectionUtils.take(this.rows_, this.getRowAt(from));
    if (to > (int) this.rows_.size()) {
      this.getRowAt(to);
    }
    this.rows_.add(0 + to, from_tr);
    final List<WTableCell> cells = this.rows_.get(to).cells_;
    for (int i = 0; i < cells.size(); ++i) {
      if (cells.get(i).getRowSpan() > 1) {
        this.getRowAt(to + cells.get(i).getRowSpan() - 1);
      }
    }
    this.flags_.set(BIT_GRID_CHANGED);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }
  /**
   * Move a table column from its original position to a new position.
   *
   * <p>The table expands automatically when the <code>to</code> column is beyond the current table
   * dimensions.
   *
   * <p>
   *
   * @see WTable#moveRow(int from, int to)
   */
  public void moveColumn(int from, int to) {
    if (from < 0 || from >= (int) this.columns_.size()) {
      logger.error(
          new StringWriter()
              .append("moveColumn: the from index is not a valid column index.")
              .toString());
      return;
    }
    WTableColumn from_tc = CollectionUtils.take(this.columns_, this.getColumnAt(from));
    if (to > (int) this.columns_.size()) {
      this.getColumnAt(to);
    }
    this.columns_.add(0 + to, from_tc);
    for (int i = 0; i < this.rows_.size(); i++) {
      final List<WTableCell> cells = this.rows_.get(i).cells_;
      final WTableCell cell = cells.get(from);
      cells.remove(0 + from);
      cells.add(0 + to, cell);
      int colSpan = cells.get(to).getColumnSpan();
      if (colSpan > 1) {
        this.getColumnAt(to + colSpan - 1);
      }
      for (int j = Math.min(from, to); j < cells.size(); ++j) {
        cells.get(j).column_ = j;
      }
    }
    this.flags_.set(BIT_GRID_CHANGED);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }

  static final int BIT_GRID_CHANGED = 0;
  private static final int BIT_COLUMNS_CHANGED = 1;
  BitSet flags_;
  List<WTableRow> rows_;
  List<WTableColumn> columns_;
  private Set<WTableRow> rowsChanged_;
  private int rowsAdded_;
  private int headerRowCount_;
  private int headerColumnCount_;

  void expand(int row, int column, int rowSpan, int columnSpan) {
    int curNumRows = this.getRowCount();
    int curNumColumns = this.getColumnCount();
    int newNumRows = row + rowSpan;
    int newNumColumns = Math.max(curNumColumns, column + columnSpan);
    for (int r = curNumRows; r < newNumRows; ++r) {
      this.insertRow(r);
    }
    for (int c = curNumColumns; c < newNumColumns; ++c) {
      this.insertColumn(c);
    }
  }

  private WTableCell getItemAt(int row, int column) {
    return this.rows_.get(row).cells_.get(column);
  }

  private void setItemAt(int row, int column, WTableCell cell) {
    this.rows_.get(row).cells_.set(column, cell);
  }

  void repaintRow(WTableRow row) {
    if (row.getRowNum() >= (int) (this.getRowCount() - this.rowsAdded_)) {
      return;
    }
    this.rowsChanged_.add(row);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }

  void repaintColumn(WTableColumn column) {
    this.flags_.set(BIT_COLUMNS_CHANGED);
    this.repaint(EnumSet.of(RepaintFlag.SizeAffected));
  }
  /**
   * Creates a table cell.
   *
   * <p>You may want to override this method if you want your table to contain specialized cells.
   */
  protected WTableCell createCell(int row, int column) {
    return new WTableCell();
  }
  /**
   * Creates a table row.
   *
   * <p>You may want to override this method if you want your table to contain specialized rows.
   */
  protected WTableRow createRow(int row) {
    return new WTableRow();
  }
  /**
   * Creates a table column.
   *
   * <p>You may want to override this method if you want your table to contain specialized columns.
   */
  protected WTableColumn createColumn(int column) {
    return new WTableColumn();
  }

  void updateDom(final DomElement element, boolean all) {
    super.updateDom(element, all);
  }

  DomElementType getDomElementType() {
    return DomElementType.TABLE;
  }

  protected DomElement createDomElement(WApplication app) {
    boolean withIds = !app.getEnvironment().isTreatLikeBot();
    DomElement table = DomElement.createNew(this.getDomElementType());
    this.setId(table, app);
    DomElement thead = null;
    if (this.headerRowCount_ != 0) {
      thead = DomElement.createNew(DomElementType.THEAD);
      if (withIds) {
        thead.setId(this.getId() + "th");
      }
    }
    DomElement tbody = DomElement.createNew(DomElementType.TBODY);
    if (withIds) {
      tbody.setId(this.getId() + "tb");
    }
    DomElement colgroup = DomElement.createNew(DomElementType.COLGROUP);
    for (int col = 0; col < this.columns_.size(); ++col) {
      DomElement c = DomElement.createNew(DomElementType.COL);
      if (withIds) {
        c.setId(this.columns_.get(col).getId());
      }
      this.columns_.get(col).updateDom(c, true);
      colgroup.addChild(c);
    }
    table.addChild(colgroup);
    this.flags_.clear(BIT_COLUMNS_CHANGED);
    for (int row = 0; row < (int) this.getRowCount(); ++row) {
      for (int col = 0; col < (int) this.getColumnCount(); ++col) {
        this.getItemAt(row, col).overSpanned_ = false;
      }
    }
    for (int row = 0; row < (int) this.getRowCount(); ++row) {
      DomElement tr = this.createRowDomElement(row, withIds, app);
      if (row < (int) this.headerRowCount_) {
        thead.addChild(tr);
      } else {
        tbody.addChild(tr);
      }
    }
    this.rowsAdded_ = 0;
    if (thead != null) {
      table.addChild(thead);
    }
    table.addChild(tbody);
    this.updateDom(table, true);
    this.flags_.clear(BIT_GRID_CHANGED);
    this.rowsChanged_.clear();
    return table;
  }

  protected void getDomChanges(final List<DomElement> result, WApplication app) {
    DomElement e = DomElement.getForUpdate(this, this.getDomElementType());
    if (!this.isStubbed() && this.flags_.get(BIT_GRID_CHANGED)) {
      DomElement newE = this.createDomElement(app);
      e.replaceWith(newE);
    } else {
      for (Iterator<WTableRow> i_it = this.rowsChanged_.iterator(); i_it.hasNext(); ) {
        WTableRow i = i_it.next();
        DomElement e2 = DomElement.getForUpdate(i, DomElementType.TR);
        i.updateDom(e2, false);
        result.add(e2);
      }
      this.rowsChanged_.clear();
      if (this.rowsAdded_ != 0) {
        DomElement etb = DomElement.getForUpdate(this.getId() + "tb", DomElementType.TBODY);
        for (int i = 0; i < (int) this.rowsAdded_; ++i) {
          DomElement tr =
              this.createRowDomElement(this.getRowCount() - this.rowsAdded_ + i, true, app);
          etb.addChild(tr);
        }
        result.add(etb);
        this.rowsAdded_ = 0;
      }
      if (this.flags_.get(BIT_COLUMNS_CHANGED)) {
        for (int i = 0; i < this.columns_.size(); ++i) {
          DomElement e2 = DomElement.getForUpdate(this.columns_.get(i), DomElementType.COL);
          this.columns_.get(i).updateDom(e2, false);
          result.add(e2);
        }
        this.flags_.clear(BIT_COLUMNS_CHANGED);
      }
      this.updateDom(e, false);
    }
    result.add(e);
  }

  void propagateRenderOk(boolean deep) {
    this.flags_.clear();
    this.rowsChanged_.clear();
    this.rowsAdded_ = 0;
    super.propagateRenderOk(deep);
  }

  protected void iterateChildren(final HandleWidgetMethod method) {
    for (WTableRow row : this.rows_) {
      for (WTableCell cell : row.cells_) {
        method.handle(cell);
      }
    }
  }

  private DomElement createRowDomElement(int row, boolean withIds, WApplication app) {
    DomElement tr = DomElement.createNew(DomElementType.TR);
    if (withIds) {
      tr.setId(this.rows_.get(row).getId());
    }
    this.rows_.get(row).updateDom(tr, true);
    tr.setWasEmpty(false);
    int spanCounter = 0;
    for (int col = 0; col < this.getColumnCount(); ++col) {
      WTableCell cell = this.getItemAt(row, col);
      if (!cell.overSpanned_) {
        DomElement td = cell.createSDomElement(app);
        if (col < this.headerColumnCount_ || row < this.headerRowCount_) {
          tr.addChild(td);
        } else {
          tr.insertChildAt(td, col - spanCounter);
        }
        for (int i = 0; i < cell.getRowSpan(); ++i) {
          for (int j = 0; j < cell.getColumnSpan(); ++j) {
            if (i + j > 0) {
              this.getItemAt(row + i, col + j).overSpanned_ = true;
              this.getItemAt(row + i, col + j).setRendered(false);
            }
          }
        }
      } else {
        spanCounter++;
      }
    }
    return tr;
  }
}
