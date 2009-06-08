package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A container widget that represents a cell in a table
 * 
 * 
 * A {@link WTable} provides a table of WTableCell container widgets. A
 * WtableCell may overspan more than one grid location in the table, by
 * specifying a {@link WTableCell#setRowSpan(int rowSpan) rowSpan} and
 * {@link WTableCell#setColumnSpan(int colSpan) columnSpan}. Table cells at
 * overspanned positions are hidden. You cannot directly create a WTableCell,
 * instead, they are created automatically by a table.
 * <p>
 * A WTableCell acts as any other {@link WContainerWidget}, except that both the
 * vertical and horizontal alignment of contents may be specified by
 * {@link WContainerWidget#setContentAlignment(EnumSet alignment)}.
 * <p>
 * The widget corresponds to the HTML <code>&lt;td&gt;</code> or
 * <code>&lt;th&gt;</code> tag, depending on whether the cell is a plain cell or
 * a header cell.
 * <p>
 * 
 * @see WTable
 */
public class WTableCell extends WContainerWidget {
	/**
	 * Set the row span.
	 * 
	 * The row span indicates how many table rows this {@link WTableCell}
	 * overspans. By default, a {@link WTableCell} has a row span of 1, only
	 * occupying its own grid cell. A row span greater than 1 indicates that
	 * table cells to the right of this one are overspanned.
	 */
	public void setRowSpan(int rowSpan) {
		if (this.rowSpan_ != rowSpan) {
			this.rowSpan_ = rowSpan;
			this.row_.getTable().expand(this.getRow(), this.column_,
					this.rowSpan_, this.columnSpan_);
			this.spanChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Get the row span.
	 * 
	 * @see WTableCell#setRowSpan(int rowSpan)
	 */
	public int getRowSpan() {
		return this.rowSpan_;
	}

	/**
	 * Set the column span.
	 * 
	 * The column span indicates how many table columns this {@link WTableCell}
	 * overspans. By default, a {@link WTableCell} has a column span of 1, only
	 * occupying its own grid cell. A column span greater than 1 indicates that
	 * table cells below this one are overspanned.
	 */
	public void setColumnSpan(int colSpan) {
		if (this.columnSpan_ != colSpan) {
			this.columnSpan_ = colSpan;
			this.row_.getTable().expand(this.getRow(), this.column_,
					this.rowSpan_, this.columnSpan_);
			this.spanChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Returns the column span.
	 * 
	 * @see WTableCell#setColumnSpan(int colSpan)
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
	 * Returns the table to which this cell belongs.
	 */
	public WTable getTable() {
		return this.row_.getTable();
	}

	WTableCell(WTableRow row, int column) {
		super((WContainerWidget) null);
		this.row_ = row;
		this.column_ = column;
		this.rowSpan_ = 1;
		this.columnSpan_ = 1;
		this.spanChanged_ = false;
		this.setParent(row.getTable());
	}

	private WTableRow row_;
	int column_;
	private int rowSpan_;
	private int columnSpan_;
	private boolean spanChanged_;

	protected void updateDom(DomElement element, boolean all) {
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

	protected DomElementType getDomElementType() {
		if (this.column_ < this.getTable().getHeaderCount(Orientation.Vertical)
				|| this.getRow() < this.getTable().getHeaderCount(
						Orientation.Horizontal)) {
			return DomElementType.DomElement_TH;
		} else {
			return DomElementType.DomElement_TD;
		}
	}

	protected void propagateRenderOk(boolean deep) {
		this.spanChanged_ = false;
		super.propagateRenderOk(deep);
	}
}
