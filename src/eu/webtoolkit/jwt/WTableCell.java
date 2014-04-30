/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WTableCell extends WContainerWidget {
	private static Logger logger = LoggerFactory.getLogger(WTableCell.class);

	/**
	 * Create a table cell.
	 */
	public WTableCell() {
		super((WContainerWidget) null);
		this.row_ = null;
		this.column_ = 0;
		this.rowSpan_ = 1;
		this.columnSpan_ = 1;
		this.spanChanged_ = false;
		this.contentAlignment_ = EnumSet.copyOf(EnumSet.of(
				AlignmentFlag.AlignLeft, AlignmentFlag.AlignTop));
	}

	/**
	 * Sets the row span.
	 * <p>
	 * The row span indicates how many table rows this {@link WTableCell}
	 * overspans. By default, a {@link WTableCell} has a row span of 1, only
	 * occupying its own grid cell. A row span greater than 1 indicates that
	 * table cells below this one are overspanned.
	 */
	public void setRowSpan(int rowSpan) {
		if (this.rowSpan_ != rowSpan) {
			this.rowSpan_ = rowSpan;
			this.row_.getTable().expand(this.getRow(), this.column_,
					this.rowSpan_, this.columnSpan_);
			this.spanChanged_ = true;
			this.getTable().flags_.set(WTable.BIT_GRID_CHANGED);
			this.getTable()
					.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		}
	}

	/**
	 * Returns the row span.
	 * <p>
	 */
	public int getRowSpan() {
		return this.rowSpan_;
	}

	/**
	 * Sets the column span.
	 * <p>
	 * The column span indicates how many table columns this {@link WTableCell}
	 * overspans. By default, a {@link WTableCell} has a column span of 1, only
	 * occupying its own grid cell. A column span greater than 1 indicates that
	 * table cells to the right of this one are overspanned.
	 */
	public void setColumnSpan(int colSpan) {
		if (this.columnSpan_ != colSpan) {
			this.columnSpan_ = colSpan;
			this.row_.getTable().expand(this.getRow(), this.column_,
					this.rowSpan_, this.columnSpan_);
			this.spanChanged_ = true;
			this.getTable().flags_.set(WTable.BIT_GRID_CHANGED);
			this.getTable()
					.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		}
	}

	/**
	 * Returns the column span.
	 * <p>
	 */
	public int getColumnSpan() {
		return this.columnSpan_;
	}

	/**
	 * Returns the row index of this cell.
	 */
	public int getRow() {
		return this.row_.getRowNum();
	}

	/**
	 * Returns the column index of this cell.
	 */
	public int getColumn() {
		return this.column_;
	}

	/**
	 * Returns the table containing this cell.
	 */
	public WTable getTable() {
		return this.row_.getTable();
	}

	/**
	 * Returns the table row containing this cell.
	 */
	public WTableRow getTableRow() {
		return this.row_;
	}

	/**
	 * Returns the table column containing this cell.
	 */
	public WTableColumn getTableColumn() {
		return this.getTable().getColumnAt(this.getColumn());
	}

	public boolean isVisible() {
		if (this.row_ != null) {
			if (this.row_.isHidden()) {
				return false;
			}
		}
		return super.isVisible();
	}

	WTableCell(WTableRow row, int column) {
		super((WContainerWidget) null);
		this.row_ = row;
		this.column_ = column;
		this.rowSpan_ = 1;
		this.columnSpan_ = 1;
		this.spanChanged_ = false;
		this.contentAlignment_ = EnumSet.copyOf(EnumSet.of(
				AlignmentFlag.AlignLeft, AlignmentFlag.AlignTop));
		this.setParentWidget(row.getTable());
	}

	WTableRow row_;
	int column_;
	private int rowSpan_;
	private int columnSpan_;
	private boolean spanChanged_;

	void updateDom(final DomElement element, boolean all) {
		if (all && this.rowSpan_ != 1 || this.spanChanged_) {
			element.setProperty(Property.PropertyRowSpan, String
					.valueOf(this.rowSpan_));
		}
		if (all && this.columnSpan_ != 1 || this.spanChanged_) {
			element.setProperty(Property.PropertyColSpan, String
					.valueOf(this.columnSpan_));
		}
		if (this.getRow() < this.getTable().getHeaderCount(
				Orientation.Horizontal)) {
			element.setAttribute("scope", "col");
		} else {
			if (this.column_ < this.getTable().getHeaderCount(
					Orientation.Vertical)) {
				element.setAttribute("scope", "row");
			}
		}
		this.spanChanged_ = false;
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		if (this.column_ < this.getTable().getHeaderCount(Orientation.Vertical)
				|| this.getRow() < this.getTable().getHeaderCount(
						Orientation.Horizontal)) {
			return DomElementType.DomElement_TH;
		} else {
			return DomElementType.DomElement_TD;
		}
	}

	void propagateRenderOk(boolean deep) {
		this.spanChanged_ = false;
		super.propagateRenderOk(deep);
	}
}
