package eu.webtoolkit.jwt;


/**
 * A column in a {@link WTable}
 * <p>
 * 
 * A WTableColumn is returned by {@link WTable#getColumnAt(int column)} and
 * managing various properties of a single column in a table (it is however not
 * a widget).
 * <p>
 * You cannot access table cells through the column. Instead, to access table
 * cells, see {@link WTable#getElementAt(int row, int column)}.
 * <p>
 * A table column corresponds to the HTML <code>&lt;col&gt;</code> tag.
 * <p>
 * 
 * @see WTable
 * @see WTableRow
 */
public class WTableColumn extends WObject {
	/**
	 * The table to which this column belongs.
	 * <p>
	 * 
	 * @see WTable#getRowAt(int row)
	 */
	public WTable getTable() {
		return this.table_;
	}

	/**
	 * The column number of this column in the table.
	 * <p>
	 * 
	 * @see WTable#getRowAt(int row)
	 */
	public int getColumnNum() {
		return this.table_.columns_.indexOf(this);
	}

	/**
	 * Set the column width.
	 * <p>
	 * The default column width is {@link WLength#Auto}.
	 * <p>
	 * 
	 * @see WTableColumn#getWidth()
	 * @see WWidget#resize(WLength width, WLength height)
	 */
	public void setWidth(WLength width) {
		this.width_ = width;
		this.table_.repaintColumn(this);
	}

	/**
	 * Get the column width.
	 * <p>
	 * 
	 * @see WTableColumn#setWidth(WLength width)
	 */
	public WLength getWidth() {
		return this.width_ != null ? this.width_ : WLength.Auto;
	}

	/**
	 * Set the CSS style class for this column.
	 * <p>
	 * The style is inherited by all table cells in this column.
	 * <p>
	 * 
	 * @see WTableColumn#getStyleClass()
	 * @see WWidget#setStyleClass(String styleClass)
	 */
	public void setStyleClass(String style) {
		this.styleClass_ = style;
		this.table_.repaintColumn(this);
	}

	/**
	 * Get the CSS style class for this column.
	 * <p>
	 * 
	 * @see WTableColumn#getStyleClass()
	 * @see WWidget#getStyleClass()
	 */
	public String getStyleClass() {
		return this.styleClass_;
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

	public String getFormName() {
		if (this.id_ != null) {
			return this.id_;
		} else {
			return super.getFormName();
		}
	}

	WTableColumn(WTable table) {
		super();
		this.table_ = table;
		this.width_ = null;
		this.id_ = null;
		this.styleClass_ = "";
	}

	public void destroy() {
		/* delete this.width_ */;
		/* delete this.id_ */;
	}

	private WTable table_;
	private WLength width_;
	private String id_;
	private String styleClass_;

	void updateDom(DomElement element, boolean all) {
		if (this.width_ != null) {
			element.setProperty(Property.PropertyStyleWidth, this.width_
					.getCssText());
		}
		if (!all || this.styleClass_.length() != 0) {
			element.setAttribute("class", this.styleClass_);
		}
	}
}
