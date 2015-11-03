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

/**
 * A table row.
 * <p>
 * 
 * A WTableRow is returned by {@link WTable#getRowAt(int row) WTable#getRowAt()}
 * and managing various properties of a single row in a table (it is however not
 * a widget).
 * <p>
 * A table row corresponds to the HTML <code>&lt;tr&gt;</code> tag.
 * <p>
 * 
 * @see WTable
 * @see WTableColumn
 */
public class WTableRow extends WObject {
	private static Logger logger = LoggerFactory.getLogger(WTableRow.class);

	/**
	 * Creates a new table row.
	 * <p>
	 * Table rows must be added to a table using
	 * {@link WTable#insertRow(int row, WTableRow tableRow) WTable#insertRow()}
	 * before you can access contents in it using
	 * {@link WTableRow#elementAt(int column) elementAt()}.
	 */
	public WTableRow() {
		super();
		this.cells_ = new ArrayList<WTableRow.TableData>();
		this.height_ = null;
		this.id_ = null;
		this.styleClass_ = "";
		this.hidden_ = false;
		this.hiddenChanged_ = false;
		// this.implementStateless(WTableRow.hide,WTableRow.undoHide);
		// this.implementStateless(WTableRow.show,WTableRow.undoHide);
	}

	/**
	 * Returns the table to which this row belongs.
	 * <p>
	 * 
	 * @see WTable#getRowAt(int row)
	 */
	public WTable getTable() {
		return this.table_;
	}

	/**
	 * Access the row element at the given column.
	 * <p>
	 * Like {@link WTable#getElementAt(int row, int column)
	 * WTable#getElementAt()}, if the column is beyond the current table
	 * dimensions, then the table is expanded automatically.
	 * <p>
	 * The row must be inserted within a table first.
	 */
	public WTableCell elementAt(int column) {
		return this.table_.getElementAt(this.getRowNum(), column);
	}

	/**
	 * Returns the row number of this row in the table.
	 * <p>
	 * Returns -1 if the row is not yet part of a table.
	 * <p>
	 * 
	 * @see WTable#getRowAt(int row)
	 */
	public int getRowNum() {
		return this.table_.rows_.indexOf(this);
	}

	/**
	 * Sets the row height.
	 * <p>
	 * The default row height is {@link WLength#Auto}.
	 * <p>
	 * 
	 * @see WTableRow#getHeight()
	 * @see WWidget#resize(WLength width, WLength height)
	 */
	public void setHeight(final WLength height) {
		this.height_ = height;
		this.table_.repaintRow(this);
	}

	/**
	 * Returns the row height.
	 * <p>
	 * 
	 * @see WTableRow#setHeight(WLength height)
	 */
	public WLength getHeight() {
		return this.height_ != null ? this.height_ : WLength.Auto;
	}

	/**
	 * Sets the CSS style class for this row.
	 * <p>
	 * The style is inherited by all table cells in this row.
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
		this.table_.repaintRow(this);
	}

	/**
	 * Returns the CSS style class for this row.
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
			this.table_.repaintRow(this);
		}
	}

	public void removeStyleClass(final String style) {
		String currentClass = this.styleClass_;
		Set<String> classes = new HashSet<String>();
		StringUtils.split(classes, currentClass, " ", true);
		if (classes.contains(style) != false) {
			this.styleClass_ = StringUtils.eraseWord(this.styleClass_, style);
			this.table_.repaintRow(this);
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
	 * <p>
	 * Hide or show the row.
	 * <p>
	 * The default value is <code>false</code> (row is not hidden).
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
	 * Returns whether the rows is hidden.
	 * <p>
	 * 
	 * @see WTableRow#setHidden(boolean how)
	 */
	public boolean isHidden() {
		return this.hidden_;
	}

	/**
	 * Hides the row.
	 * <p>
	 * 
	 * @see WTableRow#setHidden(boolean how)
	 */
	public void hide() {
		this.setHidden(true);
	}

	/**
	 * Shows the row.
	 * <p>
	 * 
	 * @see WTableRow#setHidden(boolean how)
	 */
	public void show() {
		this.setHidden(false);
	}

	/**
	 * Sets the CSS Id.
	 * <p>
	 * Sets a custom Id. Note that the Id must be unique across the whole widget
	 * tree, can only be set right after construction and cannot be changed.
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

	WTableCell createCell(int column) {
		return this.table_.createCell(this.getRowNum(), column);
	}

	void expand(int numCells) {
		int cursize = this.cells_.size();
		for (int col = cursize; col < numCells; ++col) {
			this.cells_.add(new WTableRow.TableData());
			WTableCell cell = this.createCell(col);
			cell.row_ = this;
			cell.column_ = col;
			cell.setParentWidget(this.table_);
			this.cells_.get(this.cells_.size() - 1).cell = cell;
		}
	}

	static class TableData {
		private static Logger logger = LoggerFactory.getLogger(TableData.class);

		public WTableCell cell;
		public boolean overSpanned;

		public TableData() {
			this.cell = null;
			this.overSpanned = false;
		}
	}

	WTable table_;
	List<WTableRow.TableData> cells_;
	private WLength height_;
	private String id_;
	private String styleClass_;
	private boolean hidden_;
	private boolean hiddenChanged_;
	private boolean wasHidden_;

	void updateDom(final DomElement element, boolean all) {
		if (this.height_ != null) {
			element.setProperty(Property.PropertyStyleHeight,
					this.height_.getCssText());
		}
		if (!all || this.styleClass_.length() != 0) {
			element.setProperty(Property.PropertyClass, this.styleClass_);
		}
		if (all && this.hidden_ || !all && this.hiddenChanged_) {
			element.setProperty(Property.PropertyStyleDisplay,
					this.hidden_ ? "none" : "");
			this.hiddenChanged_ = false;
		}
	}

	void insertColumn(int column) {
		this.cells_.add(0 + column, new WTableRow.TableData());
		WTableCell cell = this.createCell(column);
		cell.row_ = this;
		cell.column_ = column;
		cell.setParentWidget(this.table_);
		this.cells_.get(column).cell = cell;
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
