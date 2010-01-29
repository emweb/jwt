/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A layout manager which arranges widgets in a grid
 * <p>
 * 
 * This is a layout class that arranges widgets in a grid, to span the entire
 * area of the parent container. Each grid location (row, column) may contain
 * one widget or nested layout. Horizontal and vertical space are divided so
 * that each column/row is given its minimum size and the remaining space is
 * dived according to stretch factors among the columns/rows. The minimum width
 * of a column/row is based on the minimum dimensions of contained widgets or
 * nested layouts. The default minimum height and width may be overridden using
 * {@link WWidget#setMinimumSize(WLength width, WLength height)
 * WWidget#setMinimumSize()}.
 * <p>
 * You can use
 * {@link WContainerWidget#setOverflow(WContainerWidget.Overflow value, EnumSet orientation)
 * WContainerWidget#setOverflow()} to automatically show scrollbars on a
 * container widget which you have set for a grid cell. In some cases, you will
 * want to wrap another widget (like a {@link WTable}) into
 * {@link WContainerWidget} to have the scrollbars, since not all widgets allow
 * for scrollbars.
 * <p>
 * Columns and rows are separated using a constant spacing, which defaults to 6
 * pixels by default, and can be changed using
 * {@link WGridLayout#setHorizontalSpacing(int size) setHorizontalSpacing()} and
 * {@link WGridLayout#setVerticalSpacing(int size) setVerticalSpacing()}. In
 * addition, when this layout is a top-level layout (i.e. is not nested inside
 * another layout), a margin is set around the contents, which thus replaces
 * padding defined for the container. It is not allowed to define padding for
 * the container widget using its CSS &apos;padding&apos; property or the
 * {@link WContainerWidget#setPadding(WLength length, EnumSet sides)
 * WContainerWidget#setPadding()}. This margin also defaults to 9 pixels, and
 * can be changed using
 * {@link WLayout#setContentsMargins(int left, int top, int right, int bottom)
 * WLayout#setContentsMargins()}.
 * <p>
 * For each column or row, a stretch factor may be defined, which controls how
 * remaining horizontal or vertical space is used. Each column and row is
 * stretched using the stretch factor to fill the remaining space. When the
 * stretch factor is 0, the height of the row and its contents is not actively
 * managed. As a consequence, the contents of each cell will not fill the cell.
 * You may use a special stretch factor of -1 to indicate that the height of the
 * row should not stretch but the contents height should be actively managed.
 * This has as draw-back that the height of the row will no longer reduce in
 * size when any of the cell contents reduces in size.
 * <p>
 * Usage example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * WContainerWidget w = new WContainerWidget(this);
 * w.resize(WLength.Auto, new WLength(600));
 * 
 * WGridLayout layout = new WGridLayout();
 * layout.addWidget(new WText(&quot;Item 0 0&quot;), 0, 0);
 * layout.addWidget(new WText(&quot;Item 0 1&quot;), 0, 1);
 * layout.addWidget(new WText(&quot;Item 1 0&quot;), 1, 0);
 * layout.addWidget(new WText(&quot;Item 1 1&quot;), 1, 1);
 * 
 * w.setLayout(layout);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * <p>
 * <i><b>Note: </b>When JavaScript support is not available, only Safari and
 * Firefox properly implement this layout. For other browsers, only the
 * horizontal layout is properly implemented, while vertically all widgets use
 * their minimum size.
 * <p>
 * When set on a {@link WContainerWidget}, this layout manager accepts the
 * following hints (see {@link WLayout#setLayoutHint(String name, String value)
 * WLayout#setLayoutHint()}):
 * <ul>
 * <li>
 * &quot;table-layout&quot; with possible values &quot;auto&quot; (default) or
 * &quot;fixed&quot;.<br>
 * Use &quot;fixed&quot; to prevent nested tables from overflowing the layout.
 * In that case, you will need to specify a width (in CSS or otherwise) for at
 * least one item in every column that has no stretch factor.</li>
 * </ul>
 * </i>
 * </p>
 * <p>
 * <i><b>Warning:</b>You should specify AlignTop in the alignment flags of
 * {@link WContainerWidget#setLayout(WLayout layout)
 * WContainerWidget#setLayout()} if the container does not have a height that is
 * constrained somehow. Otherwise the behavior is undefined (the parent
 * container will continue to increase in size) </i>
 * </p>
 */
public class WGridLayout extends WLayout {
	/**
	 * Create a new grid layout.
	 * <p>
	 * The grid will grow dynamically as items are added.
	 * <p>
	 * Use <code>parent</code> = <code>null</code> to create a layout manager
	 * that can be nested inside other layout managers or to specify a specific
	 * alignment when setting the layout to a {@link WContainerWidget}.
	 */
	public WGridLayout(WWidget parent) {
		super();
		this.grid_ = new Grid();
		if (parent != null) {
			this.setLayoutInParent(parent);
		}
	}

	/**
	 * Create a new grid layout.
	 * <p>
	 * Calls {@link #WGridLayout(WWidget parent) this((WWidget)null)}
	 */
	public WGridLayout() {
		this((WWidget) null);
	}

	public void addItem(WLayoutItem item) {
		this.addItem(item, 0, this.getColumnCount());
	}

	public void removeItem(WLayoutItem item) {
		int index = this.indexOf(item);
		if (index != -1) {
			int row = index / this.getColumnCount();
			int col = index % this.getColumnCount();
			this.grid_.items_.get(row).get(col).item_ = null;
			this.updateRemoveItem(item);
		}
	}

	public WLayoutItem getItemAt(int index) {
		int row = index / this.getColumnCount();
		int col = index % this.getColumnCount();
		return this.grid_.items_.get(row).get(col).item_;
	}

	public int getCount() {
		return this.grid_.rows_.size() * this.grid_.columns_.size();
	}

	/**
	 * Adds a layout item to the grid.
	 * <p>
	 * Adds the <i>item</i> at (<i>row</i>, <code>column</code>). If an item was
	 * already added to that location, it is replaced (but not deleted).
	 * <p>
	 * An item may span several more rows or columns, which is controlled by
	 * <i>rowSpan</i> and <code>columnSpan</code>.
	 * <p>
	 * The <code>alignment</code> specifies the vertical and horizontal
	 * alignment of the item. The default value 0 indicates that the item is
	 * stretched to fill the entire grid cell. The alignment can be specified as
	 * a logical combination of a horizontal alignment (
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter}, or
	 * {@link AlignmentFlag#AlignRight}) and a vertical alignment (
	 * {@link AlignmentFlag#AlignTop}, {@link AlignmentFlag#AlignMiddle}, or
	 * {@link AlignmentFlag#AlignBottom}).
	 * <p>
	 * 
	 * @see WGridLayout#addLayout(WLayout layout, int row, int column, EnumSet
	 *      alignment)
	 * @see WGridLayout#addWidget(WWidget widget, int row, int column, EnumSet
	 *      alignment)
	 */
	public void addItem(WLayoutItem item, int row, int column, int rowSpan,
			int columnSpan, EnumSet<AlignmentFlag> alignment) {
		columnSpan = Math.max(1, columnSpan);
		rowSpan = Math.max(1, rowSpan);
		this.expand(row, column, rowSpan, columnSpan);
		Grid.Item gridItem = this.grid_.items_.get(row).get(column);
		if (gridItem.item_ != null) {
			WLayoutItem oldItem = gridItem.item_;
			gridItem.item_ = null;
			this.updateRemoveItem(oldItem);
		}
		gridItem.item_ = item;
		gridItem.rowSpan_ = rowSpan;
		gridItem.colSpan_ = columnSpan;
		gridItem.alignment_ = EnumSet.copyOf(alignment);
		this.updateAddItem(item);
	}

	/**
	 * Adds a layout item to the grid.
	 * <p>
	 * Calls
	 * {@link #addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan, EnumSet alignment)
	 * addItem(item, row, column, rowSpan, columnSpan, EnumSet.of(alignmen,
	 * alignment))}
	 */
	public final void addItem(WLayoutItem item, int row, int column,
			int rowSpan, int columnSpan, AlignmentFlag alignmen,
			AlignmentFlag... alignment) {
		addItem(item, row, column, rowSpan, columnSpan, EnumSet.of(alignmen,
				alignment));
	}

	/**
	 * Adds a layout item to the grid.
	 * <p>
	 * Calls
	 * {@link #addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan, EnumSet alignment)
	 * addItem(item, row, column, 1, 1, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addItem(WLayoutItem item, int row, int column) {
		addItem(item, row, column, 1, 1, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a layout item to the grid.
	 * <p>
	 * Calls
	 * {@link #addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan, EnumSet alignment)
	 * addItem(item, row, column, rowSpan, 1,
	 * EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addItem(WLayoutItem item, int row, int column, int rowSpan) {
		addItem(item, row, column, rowSpan, 1, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a layout item to the grid.
	 * <p>
	 * Calls
	 * {@link #addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan, EnumSet alignment)
	 * addItem(item, row, column, rowSpan, columnSpan,
	 * EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addItem(WLayoutItem item, int row, int column,
			int rowSpan, int columnSpan) {
		addItem(item, row, column, rowSpan, columnSpan, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a nested layout item to the grid.
	 * <p>
	 * Adds the <i>layout</i> at (<i>row</i>, <code>column</code>). If an item
	 * was already added to that location, it is replaced (but not deleted).
	 * <p>
	 * The <code>alignment</code> specifies the vertical and horizontal
	 * alignment of the item. The default value 0 indicates that the item is
	 * stretched to fill the entire grid cell. The alignment can be specified as
	 * a logical combination of a horizontal alignment (
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter}, or
	 * {@link AlignmentFlag#AlignRight}) and a vertical alignment (
	 * {@link AlignmentFlag#AlignTop}, {@link AlignmentFlag#AlignMiddle}, or
	 * {@link AlignmentFlag#AlignBottom}).
	 * <p>
	 * 
	 * @see WGridLayout#addLayout(WLayout layout, int row, int column, int
	 *      rowSpan, int columnSpan, EnumSet alignment)
	 */
	public void addLayout(WLayout layout, int row, int column,
			EnumSet<AlignmentFlag> alignment) {
		this.addItem(layout, row, column, 1, 1, alignment);
	}

	/**
	 * Adds a nested layout item to the grid.
	 * <p>
	 * Calls
	 * {@link #addLayout(WLayout layout, int row, int column, EnumSet alignment)
	 * addLayout(layout, row, column, EnumSet.of(alignmen, alignment))}
	 */
	public final void addLayout(WLayout layout, int row, int column,
			AlignmentFlag alignmen, AlignmentFlag... alignment) {
		addLayout(layout, row, column, EnumSet.of(alignmen, alignment));
	}

	/**
	 * Adds a nested layout item to the grid.
	 * <p>
	 * Calls
	 * {@link #addLayout(WLayout layout, int row, int column, EnumSet alignment)
	 * addLayout(layout, row, column, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addLayout(WLayout layout, int row, int column) {
		addLayout(layout, row, column, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a nested layout item to the grid.
	 * <p>
	 * Adds the <i>layout</i> at (<i>row</i>, <code>column</code>). If an item
	 * was already added to that location, it is replaced (but not deleted).
	 * <p>
	 * An item may span several more rows or columns, which is controlled by
	 * <i>rowSpan</i> and <code>columnSpan</code>.
	 * <p>
	 * The <code>alignment</code> specifies the vertical and horizontal
	 * alignment of the item. The default value 0 indicates that the item is
	 * stretched to fill the entire grid cell. The alignment can be specified as
	 * a logical combination of a horizontal alignment (
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter}, or
	 * {@link AlignmentFlag#AlignRight}) and a vertical alignment (
	 * {@link AlignmentFlag#AlignTop}, {@link AlignmentFlag#AlignMiddle}, or
	 * {@link AlignmentFlag#AlignBottom}).
	 * <p>
	 * 
	 * @see WGridLayout#addLayout(WLayout layout, int row, int column, EnumSet
	 *      alignment)
	 */
	public void addLayout(WLayout layout, int row, int column, int rowSpan,
			int columnSpan, EnumSet<AlignmentFlag> alignment) {
		this.addItem(layout, row, column, rowSpan, columnSpan, alignment);
	}

	/**
	 * Adds a nested layout item to the grid.
	 * <p>
	 * Calls
	 * {@link #addLayout(WLayout layout, int row, int column, int rowSpan, int columnSpan, EnumSet alignment)
	 * addLayout(layout, row, column, rowSpan, columnSpan, EnumSet.of(alignmen,
	 * alignment))}
	 */
	public final void addLayout(WLayout layout, int row, int column,
			int rowSpan, int columnSpan, AlignmentFlag alignmen,
			AlignmentFlag... alignment) {
		addLayout(layout, row, column, rowSpan, columnSpan, EnumSet.of(
				alignmen, alignment));
	}

	/**
	 * Adds a nested layout item to the grid.
	 * <p>
	 * Calls
	 * {@link #addLayout(WLayout layout, int row, int column, int rowSpan, int columnSpan, EnumSet alignment)
	 * addLayout(layout, row, column, rowSpan, columnSpan,
	 * EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addLayout(WLayout layout, int row, int column,
			int rowSpan, int columnSpan) {
		addLayout(layout, row, column, rowSpan, columnSpan, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a widget to the grid.
	 * <p>
	 * Adds the <i>widget</i> at (<i>row</i>, <code>column</code>). If an item
	 * was already added to that location, it is replaced (but not deleted).
	 * <p>
	 * The <code>alignment</code> specifies the vertical and horizontal
	 * alignment of the item. The default value 0 indicates that the item is
	 * stretched to fill the entire grid cell. The alignment can be specified as
	 * a logical combination of a horizontal alignment (
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter}, or
	 * {@link AlignmentFlag#AlignRight}) and a vertical alignment (
	 * {@link AlignmentFlag#AlignTop}, {@link AlignmentFlag#AlignMiddle}, or
	 * {@link AlignmentFlag#AlignBottom}).
	 * <p>
	 * 
	 * @see WGridLayout#addWidget(WWidget widget, int row, int column, int
	 *      rowSpan, int columnSpan, EnumSet alignment)
	 */
	public void addWidget(WWidget widget, int row, int column,
			EnumSet<AlignmentFlag> alignment) {
		this.addItem(new WWidgetItem(widget), row, column, 1, 1, alignment);
	}

	/**
	 * Adds a widget to the grid.
	 * <p>
	 * Calls
	 * {@link #addWidget(WWidget widget, int row, int column, EnumSet alignment)
	 * addWidget(widget, row, column, EnumSet.of(alignmen, alignment))}
	 */
	public final void addWidget(WWidget widget, int row, int column,
			AlignmentFlag alignmen, AlignmentFlag... alignment) {
		addWidget(widget, row, column, EnumSet.of(alignmen, alignment));
	}

	/**
	 * Adds a widget to the grid.
	 * <p>
	 * Calls
	 * {@link #addWidget(WWidget widget, int row, int column, EnumSet alignment)
	 * addWidget(widget, row, column, EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addWidget(WWidget widget, int row, int column) {
		addWidget(widget, row, column, EnumSet.noneOf(AlignmentFlag.class));
	}

	/**
	 * Adds a widget to the grid.
	 * <p>
	 * Adds the <i>widget</i> at (<i>row</i>, <code>column</code>). If an item
	 * was already added to that location, it is replaced (but not deleted).
	 * <p>
	 * The widget may span several more rows or columns, which is controlled by
	 * <i>rowSpan</i> and <code>columnSpan</code>.
	 * <p>
	 * The <code>alignment</code> specifies the vertical and horizontal
	 * alignment of the item. The default value 0 indicates that the item is
	 * stretched to fill the entire grid cell. The alignment can be specified as
	 * a logical combination of a horizontal alignment (
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter}, or
	 * {@link AlignmentFlag#AlignRight}) and a vertical alignment (
	 * {@link AlignmentFlag#AlignTop}, {@link AlignmentFlag#AlignMiddle}, or
	 * {@link AlignmentFlag#AlignBottom}).
	 * <p>
	 * 
	 * @see WGridLayout#addWidget(WWidget widget, int row, int column, EnumSet
	 *      alignment)
	 */
	public void addWidget(WWidget widget, int row, int column, int rowSpan,
			int columnSpan, EnumSet<AlignmentFlag> alignment) {
		this.addItem(new WWidgetItem(widget), row, column, rowSpan, columnSpan,
				alignment);
	}

	/**
	 * Adds a widget to the grid.
	 * <p>
	 * Calls
	 * {@link #addWidget(WWidget widget, int row, int column, int rowSpan, int columnSpan, EnumSet alignment)
	 * addWidget(widget, row, column, rowSpan, columnSpan, EnumSet.of(alignmen,
	 * alignment))}
	 */
	public final void addWidget(WWidget widget, int row, int column,
			int rowSpan, int columnSpan, AlignmentFlag alignmen,
			AlignmentFlag... alignment) {
		addWidget(widget, row, column, rowSpan, columnSpan, EnumSet.of(
				alignmen, alignment));
	}

	/**
	 * Adds a widget to the grid.
	 * <p>
	 * Calls
	 * {@link #addWidget(WWidget widget, int row, int column, int rowSpan, int columnSpan, EnumSet alignment)
	 * addWidget(widget, row, column, rowSpan, columnSpan,
	 * EnumSet.noneOf(AlignmentFlag.class))}
	 */
	public final void addWidget(WWidget widget, int row, int column,
			int rowSpan, int columnSpan) {
		addWidget(widget, row, column, rowSpan, columnSpan, EnumSet
				.noneOf(AlignmentFlag.class));
	}

	/**
	 * Sets the horizontal spacing.
	 * <p>
	 * The default horizontal spacing is 9 pixels.
	 * <p>
	 * 
	 * @see WGridLayout#setVerticalSpacing(int size)
	 */
	public void setHorizontalSpacing(int size) {
		this.grid_.horizontalSpacing_ = size;
		this.update();
	}

	/**
	 * Returns the horizontal spacing.
	 * <p>
	 * 
	 * @see WGridLayout#setHorizontalSpacing(int size)
	 */
	public int getHorizontalSpacing() {
		return this.grid_.horizontalSpacing_;
	}

	/**
	 * Sets the vertical spacing.
	 * <p>
	 * The default vertical spacing is 9 pixels.
	 * <p>
	 * 
	 * @see WGridLayout#setHorizontalSpacing(int size)
	 */
	public void setVerticalSpacing(int size) {
		this.grid_.verticalSpacing_ = size;
		this.update();
	}

	/**
	 * Returns the vertical spacing.
	 * <p>
	 * 
	 * @see WGridLayout#setVerticalSpacing(int size)
	 */
	public int getVerticalSpacing() {
		return this.grid_.verticalSpacing_;
	}

	/**
	 * Returns the column count.
	 * <p>
	 * The grid dimensions change dynamically when adding contents to the grid.
	 * <p>
	 * 
	 * @see WGridLayout#getRowCount()
	 */
	public int getColumnCount() {
		return this.grid_.columns_.size();
	}

	/**
	 * Returns the row count.
	 * <p>
	 * The grid dimensions change dynamically when adding contents to the grid.
	 * <p>
	 * 
	 * @see WGridLayout#getColumnCount()
	 */
	public int getRowCount() {
		return this.grid_.rows_.size();
	}

	/**
	 * Sets the column stretch.
	 * <p>
	 * Sets the <i>stretch</i> factor for column <code>column</code>.
	 * <p>
	 * 
	 * @see WGridLayout#getColumnStretch(int column)
	 */
	public void setColumnStretch(int column, int stretch) {
		this.expand(0, column, 0, 1);
		this.grid_.columns_.get(column).stretch_ = stretch;
		this.update();
	}

	/**
	 * Returns the column stretch.
	 * <p>
	 * 
	 * @see WGridLayout#setColumnStretch(int column, int stretch)
	 */
	public int getColumnStretch(int column) {
		return this.grid_.columns_.get(column).stretch_;
	}

	/**
	 * Sets the row stretch.
	 * <p>
	 * Sets the <i>stretch</i> factor for row <code>row</code>. See the
	 * description for the special value of -1.
	 * <p>
	 * 
	 * @see WGridLayout#getRowStretch(int row)
	 */
	public void setRowStretch(int row, int stretch) {
		this.expand(row, 0, 1, 0);
		this.grid_.rows_.get(row).stretch_ = stretch;
		this.update();
	}

	/**
	 * Returns the column stretch.
	 * <p>
	 * 
	 * @see WGridLayout#setRowStretch(int row, int stretch)
	 */
	public int getRowStretch(int row) {
		return this.grid_.rows_.get(row).stretch_;
	}

	Grid getGrid() {
		return this.grid_;
	}

	private Grid grid_;

	private void expand(int row, int column, int rowSpan, int columnSpan) {
		int newRowCount = Math.max(this.getRowCount(), row + rowSpan);
		int newColumnCount = Math.max(this.getColumnCount(), column
				+ columnSpan);
		int extraRows = newRowCount - this.getRowCount();
		int extraColumns = newColumnCount - this.getColumnCount();
		if (extraColumns > 0) {
			for (int a_row = 0; a_row < this.getRowCount(); ++a_row) {
				{
					int insertPos = this.grid_.items_.get(a_row).size();
					for (int ii = 0; ii < extraColumns; ++ii)
						this.grid_.items_.get(a_row).add(insertPos + ii,
								new Grid.Item());
				}
				;
			}
			{
				int insertPos = this.grid_.columns_.size();
				for (int ii = 0; ii < extraColumns; ++ii)
					this.grid_.columns_.add(insertPos + ii, new Grid.Column());
			}
			;
		}
		if (extraRows > 0) {
			{
				int insertPos = this.grid_.items_.size();
				for (int ii = 0; ii < extraRows; ++ii)
					this.grid_.items_.add(insertPos + ii,
							new ArrayList<Grid.Item>());
			}
			;
			for (int i = 0; i < extraRows; ++i) {
				List<Grid.Item> items = this.grid_.items_.get(this.grid_.items_
						.size()
						- extraRows + i);
				{
					int insertPos = items.size();
					for (int ii = 0; ii < newColumnCount; ++ii)
						items.add(insertPos + ii, new Grid.Item());
				}
				;
			}
			{
				int insertPos = this.grid_.rows_.size();
				for (int ii = 0; ii < extraRows; ++ii)
					this.grid_.rows_.add(insertPos + ii, new Grid.Row());
			}
			;
		}
	}
}
