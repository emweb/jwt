/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

/**
 * A row in a {@link WTable}
 * <p>
 * 
 * A WTableRow is returned by {@link WTable#getRowAt(int row) WTable#getRowAt()}
 * and managing various properties of a single row in a table (it is however not
 * a widget).
 * <p>
 * You cannot access table cells through the row. Instead, to access table
 * cells, see {@link WTable#getElementAt(int row, int column)
 * WTable#getElementAt()}.
 * <p>
 * A table row corresponds to the HTML <code>&lt;tr&gt;</code> tag.
 * <p>
 * 
 * @see WTable
 * @see WTableColumn
 */
public class WTableRow extends WObject {
	/**
	 * The table to which this row belongs.
	 * <p>
	 * 
	 * @see WTable#getRowAt(int row)
	 */
	public WTable getTable() {
		return this.table_;
	}

	/**
	 * The row number of this row in the table.
	 * <p>
	 * 
	 * @see WTable#getRowAt(int row)
	 */
	public int getRowNum() {
		return this.table_.rows_.indexOf(this);
	}

	/**
	 * Set the row height.
	 * <p>
	 * The default row height is {@link WLength#Auto}.
	 * <p>
	 * 
	 * @see WTableRow#getHeight()
	 * @see WWidget#resize(WLength width, WLength height)
	 */
	public void setHeight(WLength height) {
		this.height_ = height;
		this.table_.repaintRow(this);
	}

	/**
	 * Get the row height.
	 * <p>
	 * 
	 * @see WTableRow#setHeight(WLength height)
	 */
	public WLength getHeight() {
		return this.height_ != null ? this.height_ : WLength.Auto;
	}

	/**
	 * Set the CSS style class for this row.
	 * <p>
	 * The style is inherited by all table cells in this row.
	 * <p>
	 * 
	 * @see WTableRow#getStyleClass()
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public void setStyleClass(String style) {
		if (WWebWidget.canOptimizeUpdates() && style.equals(this.styleClass_)) {
			return;
		}
		this.styleClass_ = style;
		this.table_.repaintRow(this);
	}

	/**
	 * Get the CSS style class for this row.
	 * <p>
	 * 
	 * @see WTableRow#getStyleClass()
	 * @see WWidget#getStyleClass()
	 */
	public String getStyleClass() {
		return this.styleClass_;
	}

	/**
	 * Set whether the row must be hidden.
	 * <p>
	 * Hide or show the row.
	 * <p>
	 * The default value is false (row is not hidden).
	 * <p>
	 * 
	 * @see WTableRow#hide()
	 * @see WTableRow#show()
	 */
	public void setHidden(boolean how) {
		if (WWebWidget.canOptimizeUpdates() && this.hidden_ == how) {
			return;
		}
		this.wasHidden_ = this.hidden_;
		this.hidden_ = how;
		this.hiddenChanged_ = true;
		this.table_.repaintRow(this);
	}

	/**
	 * Return whether the rows is hidden.
	 * <p>
	 * 
	 * @see WTableRow#setHidden(boolean how)
	 */
	public boolean isHidden() {
		return this.hidden_;
	}

	/**
	 * Hide the row.
	 * <p>
	 * 
	 * @see WTableRow#setHidden(boolean how)
	 */
	public void hide() {
		this.setHidden(true);
	}

	/**
	 * Show the row.
	 * <p>
	 * 
	 * @see WTableRow#setHidden(boolean how)
	 */
	public void show() {
		this.setHidden(false);
	}

	/**
	 * Set the CSS Id.
	 * <p>
	 * Sets a custom Id. Note that the Id must be unique across the whole widget
	 * tree, can only be set right after construction and cannot be changed.
	 * <p>
	 * 
	 * @see WObject#getId()
	 */
	public void setId(String id) {
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

	WTableRow(WTable table, int numCells) {
		super();
		this.table_ = table;
		this.cells_ = new ArrayList<WTableRow.TableData>();
		this.height_ = null;
		this.id_ = null;
		this.styleClass_ = "";
		this.hidden_ = false;
		this.hiddenChanged_ = false;
		this.expand(numCells);
		// this.implementStateless(WTableRow.hide,WTableRow.undoHide);
		// this.implementStateless(WTableRow.show,WTableRow.undoHide);
	}

	void expand(int numCells) {
		int cursize = this.cells_.size();
		for (int col = cursize; col < numCells; ++col) {
			this.cells_.add(new WTableRow.TableData());
			this.cells_.get(this.cells_.size() - 1).cell = new WTableCell(this,
					col);
		}
	}

	static class TableData {
		public WTableCell cell;
		public boolean overSpanned;

		public TableData() {
			this.cell = null;
			this.overSpanned = false;
		}
	}

	private WTable table_;
	List<WTableRow.TableData> cells_;
	private WLength height_;
	private String id_;
	private String styleClass_;
	private boolean hidden_;
	private boolean hiddenChanged_;
	private boolean wasHidden_;

	void updateDom(DomElement element, boolean all) {
		if (this.height_ != null) {
			element.setProperty(Property.PropertyStyleHeight, this.height_
					.getCssText());
		}
		if (!all || this.styleClass_.length() != 0) {
			element.setAttribute("class", this.styleClass_);
		}
		if (all && this.hidden_ || !all && this.hiddenChanged_) {
			element.setProperty(Property.PropertyStyleDisplay,
					this.hidden_ ? "none" : "");
			this.hiddenChanged_ = false;
		}
	}

	void insertColumn(int column) {
		this.cells_.add(0 + column, new WTableRow.TableData());
		this.cells_.get(column).cell = new WTableCell(this, column);
		for (int i = column; i < this.cells_.size(); ++i) {
			this.cells_.get(i).cell.column_ = i;
		}
	}

	void deleteColumn(int column) {
		if (this.cells_.get(column).cell != null)
			this.cells_.get(column).cell.remove();
		this.cells_.remove(0 + column);
		for (int i = column; i < this.cells_.size(); ++i) {
			this.cells_.get(i).cell.column_ = i;
		}
	}

	private void undoHide() {
		this.setHidden(this.wasHidden_);
	}
}
