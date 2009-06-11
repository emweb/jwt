package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A container widget which provides layout of children in a table grid
 * 
 * 
 * A WTable arranges its children in a table.
 * <p>
 * To insert or access contents, use
 * {@link WTable#getElementAt(int row, int column)} to access the cell at a
 * particular location in the table. The WTable expands automatically to create
 * the indexed (row, column) as necessary.
 * <p>
 * It is possible to insert and delete entire rows or columns from the table
 * using the {@link WTable#insertColumn(int column)},
 * {@link WTable#insertRow(int row)}, {@link WTable#deleteColumn(int column)},
 * or {@link WTable#deleteRow(int row)} methods.
 * <p>
 * You may indicate a number of rows and columns that act as headers using
 * {@link WTable#setHeaderCount(int count, Orientation orientation)}. Header
 * cells are rendered as <code>&lt;th&gt;</code> instead of
 * <code>&lt;td&gt;</code> elements. By default, no rows or columns are
 * configured as headers.
 * <p>
 * The widget corresponds to the HTML <code>&lt;table&gt;</code> tag.
 * <p>
 * WComboBox is a {@link WWidget#setInline(boolean inlined) stacked} widget.
 * <p>
 * 
 * @see WTableCell
 * @see WTableRow
 * @see WTableColumn
 */
public class WTable extends WInteractWidget {
	/**
	 * Construct an empty table.
	 */
	public WTable(WContainerWidget parent) {
		super(parent);
		this.flags_ = new BitSet();
		this.rows_ = new ArrayList<WTableRow>();
		this.columns_ = null;
		this.rowsChanged_ = null;
		this.rowsAdded_ = 0;
		this.headerRowCount_ = 0;
		this.headerColumnCount_ = 0;
		this.setInline(false);
		this.setIgnoreChildRemoves(true);
	}

	public WTable() {
		this((WContainerWidget) null);
	}

	/**
	 * Delete the table and its entire contents.
	 */
	public void remove() {
		for (int i = 0; i < this.rows_.size(); ++i) {
			/* delete this.rows_.get(i) */;
		}
		if (this.columns_ != null) {
			for (int i = 0; i < this.columns_.size(); ++i) {
				/* delete this.columns_.get(i) */;
			}
			/* delete this.columns_ */;
		}
		/* delete this.rowsChanged_ */;
		this.rowsChanged_ = null;
		super.remove();
	}

	/**
	 * Access the table element at the given row and column.
	 * 
	 * If the row/column is beyond the current table dimensions, then the table
	 * is expanded automatically.
	 */
	public WTableCell getElementAt(int row, int column) {
		this.expand(row, column, 1, 1);
		WTableRow.TableData d = this.itemAt(row, column);
		return d.cell;
	}

	/**
	 * Returns the row object for the given row.
	 * 
	 * Like with {@link WTable#getElementAt(int row, int column)}, the table
	 * expands automatically when the row is beyond the current table
	 * dimensions.
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
	 * Like with {@link WTable#getElementAt(int row, int column)}, the table
	 * expands automatically when the column is beyond the current table
	 * dimensions.
	 * <p>
	 * 
	 * @see WTable#getElementAt(int row, int column)
	 * @see WTable#getRowAt(int row)
	 */
	public WTableColumn getColumnAt(int column) {
		this.expand(0, column, 0, 1);
		if (!(this.columns_ != null)) {
			this.columns_ = new ArrayList<WTableColumn>();
		}
		if (this.columns_.size() <= (int) column) {
			for (int col = this.columns_.size(); col <= (int) column; ++col) {
				this.columns_.add(new WTableColumn(this));
			}
		}
		return this.columns_.get(column);
	}

	/**
	 * Delete the given table cell and its contents.
	 * 
	 * The table cell at that position is recreated.
	 */
	public void removeCell(WTableCell item) {
		this.removeCell(item.getRow(), item.getColumn());
	}

	/**
	 * Delete the table cell at the given position.
	 * 
	 * @see WTable#removeCell(WTableCell item)
	 */
	public void removeCell(int row, int column) {
		WTableRow.TableData d = this.itemAt(row, column);
		if (d.cell != null)
			d.cell.remove();
		d.cell = new WTableCell(this.rows_.get(row), column);
	}

	/**
	 * Insert an empty row.
	 */
	public void insertRow(int row) {
		this.rows_.add(0 + row, new WTableRow(this, this.getColumnCount()));
		this.flags_.set(BIT_GRID_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Delete the given row and all its contents.
	 * 
	 * Rows below the given row are shifted up.
	 */
	public void deleteRow(int row) {
		if (this.rowsChanged_ != null) {
			this.rowsChanged_.remove(this.rows_.get(row));
			if (this.rowsChanged_.isEmpty()) {
				/* delete this.rowsChanged_ */;
				this.rowsChanged_ = null;
			}
		}
		for (int i = 0; i < this.getColumnCount(); ++i) {
			WTableCell cell = this.rows_.get(row).cells_.get(i).cell;
			if (cell != null)
				cell.remove();
		}
		/* delete this.rows_.get(row) */;
		this.rows_.remove(0 + row);
		this.flags_.set(BIT_GRID_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Insert an empty column.
	 */
	public void insertColumn(int column) {
		for (int i = 0; i < this.rows_.size(); ++i) {
			this.rows_.get(i).insertColumn(column);
		}
		if (this.columns_ != null && (int) column <= this.columns_.size()) {
			this.columns_.add(0 + column, new WTableColumn(this));
		}
		this.flags_.set(BIT_GRID_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Delete the given column and all its contents.
	 */
	public void deleteColumn(int column) {
		for (int i = 0; i < this.getRowCount(); ++i) {
			this.rows_.get(i).deleteColumn(column);
		}
		if (this.columns_ != null && (int) column <= this.columns_.size()) {
			/* delete this.columns_.get(column) */;
			this.columns_.remove(0 + column);
		}
		this.flags_.set(BIT_GRID_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Clears the entire table.
	 * 
	 * This method clears the entire table: all cells and their contents are
	 * deleted.
	 */
	public void clear() {
		while (this.getRowCount() > 0) {
			this.deleteRow(this.getRowCount() - 1);
		}
	}

	/**
	 * Returns the number of rows in the table.
	 */
	public int getRowCount() {
		return this.rows_.size();
	}

	/**
	 * Returns the number of columns in the table.
	 */
	public int getColumnCount() {
		return this.rows_.size() > 0 ? this.rows_.get(0).cells_.size() : 0;
	}

	/**
	 * Set the number of header rows or columns.
	 * 
	 * The default values are 0.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>This must be set before the initial rendering and cannot
	 * be changed later. </i>
	 * </p>
	 */
	public void setHeaderCount(int count, Orientation orientation) {
		if (orientation == Orientation.Horizontal) {
			this.headerRowCount_ = count;
		} else {
			this.headerColumnCount_ = count;
		}
	}

	public final void setHeaderCount(int count) {
		setHeaderCount(count, Orientation.Horizontal);
	}

	/**
	 * Returns the number of header rows or columns.
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

	public final int getHeaderCount() {
		return getHeaderCount(Orientation.Horizontal);
	}

	private static final int BIT_GRID_CHANGED = 0;
	private static final int BIT_COLUMNS_CHANGED = 1;
	private BitSet flags_;
	List<WTableRow> rows_;
	List<WTableColumn> columns_;
	private Set<WTableRow> rowsChanged_;
	private int rowsAdded_;
	private int headerRowCount_;
	private int headerColumnCount_;

	void expand(int row, int column, int rowSpan, int columnSpan) {
		int newNumRows = row + rowSpan;
		int curNumColumns = this.getColumnCount();
		int newNumColumns = Math.max(curNumColumns, column + columnSpan);
		if (newNumRows > this.getRowCount() || newNumColumns > curNumColumns) {
			if (newNumColumns == curNumColumns
					&& this.getRowCount() > this.headerRowCount_) {
				this.rowsAdded_ += newNumRows - this.getRowCount();
			} else {
				this.flags_.set(BIT_GRID_CHANGED);
			}
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
			for (int r = this.getRowCount(); r < newNumRows; ++r) {
				this.rows_.add(new WTableRow(this, newNumColumns));
			}
			if (newNumColumns > curNumColumns) {
				for (int r = 0; r < this.getRowCount(); ++r) {
					WTableRow tr = this.rows_.get(r);
					tr.expand(newNumColumns);
				}
			}
		}
	}

	private WTableRow.TableData itemAt(int row, int column) {
		return this.rows_.get(row).cells_.get(column);
	}

	void repaintRow(WTableRow row) {
		if (!(this.rowsChanged_ != null)) {
			this.rowsChanged_ = new HashSet<WTableRow>();
		}
		this.rowsChanged_.add(row);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	void repaintColumn(WTableColumn column) {
		this.flags_.set(BIT_COLUMNS_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	protected void updateDom(DomElement element, boolean all) {
		super.updateDom(element, all);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_TABLE;
	}

	protected DomElement createDomElement(WApplication app) {
		boolean withIds = !app.getEnvironment().agentIsSpiderBot();
		DomElement table = DomElement.createNew(this.getDomElementType());
		this.setId(table, app);
		DomElement thead = null;
		if (this.headerRowCount_ != 0) {
			thead = DomElement.createNew(DomElementType.DomElement_THEAD);
			if (withIds) {
				thead.setId(this.getFormName() + "th");
			}
		}
		DomElement tbody = DomElement
				.createNew(DomElementType.DomElement_TBODY);
		if (withIds) {
			tbody.setId(this.getFormName() + "tb");
		}
		if (this.columns_ != null) {
			for (int col = 0; col < this.columns_.size(); ++col) {
				DomElement c = DomElement
						.createNew(DomElementType.DomElement_COL);
				if (withIds) {
					c.setId(this.columns_.get(col));
				}
				this.columns_.get(col).updateDom(c, true);
				table.addChild(c);
			}
			this.flags_.clear(BIT_COLUMNS_CHANGED);
		}
		for (int row = 0; row < (int) this.getRowCount(); ++row) {
			for (int col = 0; col < (int) this.getColumnCount(); ++col) {
				this.itemAt(row, col).overSpanned = false;
			}
		}
		for (int row = 0; row < (int) this.getRowCount(); ++row) {
			DomElement tr = this.createRow(row, withIds, app);
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
		/* delete this.rowsChanged_ */;
		this.rowsChanged_ = null;
		return table;
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		DomElement e = DomElement.getForUpdate(this, this.getDomElementType());
		if (!this.isStubbed() && this.flags_.get(BIT_GRID_CHANGED)) {
			DomElement newE = this.createDomElement(app);
			e.replaceWith(newE, true);
		} else {
			if (this.rowsChanged_ != null) {
				for (Iterator<WTableRow> i_it = this.rowsChanged_.iterator(); i_it
						.hasNext();) {
					WTableRow i = i_it.next();
					DomElement e2 = DomElement.getForUpdate(i,
							DomElementType.DomElement_TR);
					i.updateDom(e2, false);
					result.add(e2);
				}
				/* delete this.rowsChanged_ */;
				this.rowsChanged_ = null;
			}
			if (this.rowsAdded_ != 0) {
				DomElement etb = DomElement.getForUpdate(this.getFormName()
						+ "tb", DomElementType.DomElement_TBODY);
				for (int i = 0; i < (int) this.rowsAdded_; ++i) {
					DomElement tr = this.createRow(this.getRowCount()
							- this.rowsAdded_ + i, true, app);
					etb.addChild(tr);
				}
				result.add(etb);
				this.rowsAdded_ = 0;
			}
			if (this.flags_.get(BIT_COLUMNS_CHANGED)) {
				if (this.columns_ != null) {
					for (int i = 0; i < this.columns_.size(); ++i) {
						DomElement e2 = DomElement.getForUpdate(this.columns_
								.get(i), DomElementType.DomElement_COL);
						this.columns_.get(i).updateDom(e2, false);
						result.add(e2);
					}
				}
				this.flags_.clear(BIT_COLUMNS_CHANGED);
			}
			this.updateDom(e, false);
		}
		result.add(e);
	}

	protected void propagateRenderOk(boolean deep) {
		this.flags_.clear();
		if (this.rowsChanged_ != null) {
			/* delete this.rowsChanged_ */;
			this.rowsChanged_ = null;
		}
		this.rowsAdded_ = 0;
		super.propagateRenderOk(deep);
	}

	private DomElement createRow(int row, boolean withIds, WApplication app) {
		DomElement tr = DomElement.createNew(DomElementType.DomElement_TR);
		if (withIds) {
			tr.setId(this.rows_.get(row));
		}
		this.rows_.get(row).updateDom(tr, true);
		tr.setWasEmpty(false);
		for (int col = 0; col < this.getColumnCount(); ++col) {
			WTableRow.TableData d = this.itemAt(row, col);
			if (!d.overSpanned) {
				DomElement td = d.cell.createSDomElement(app);
				if (col < this.headerColumnCount_ || row < this.headerRowCount_) {
					tr.addChild(td);
				} else {
					tr.insertChildAt(td, col);
				}
				for (int i = 0; i < d.cell.getRowSpan(); ++i) {
					for (int j = 0; j < d.cell.getColumnSpan(); ++j) {
						if (i + j > 0) {
							this.itemAt(row + i, col + j).overSpanned = true;
						}
					}
				}
			}
		}
		return tr;
	}
}
