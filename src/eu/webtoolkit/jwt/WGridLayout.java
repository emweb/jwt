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
 * A layout manager which arranges widgets in a grid.
 * <p>
 * 
 * This is a layout class that arranges widgets in a grid, to span the entire
 * area of the parent container. Each grid location (row, column) may contain
 * one widget or nested layout. Horizontal and vertical space are divided so
 * that each non-stretchable column/row is given its preferred size (if
 * possible) and the remaining space is divided according to stretch factors
 * among the columns/rows.
 * <p>
 * The preferred width/height of a column/row is based on the size the widgets
 * need in order to not require a scrollbar.
 * <p>
 * The minimum width/height of a column/row is based on the minimum dimensions
 * of contained widgets or nested layouts. The default minimum height and width
 * for a widget is 0. It can be specified using
 * {@link WWidget#setMinimumSize(WLength width, WLength height)
 * WWidget#setMinimumSize()} or using CSS min-width and min-height properties.
 * <p>
 * You should use
 * {@link WContainerWidget#setOverflow(WContainerWidget.Overflow value, EnumSet orientation)
 * WContainerWidget::setOverflow(OverflowAuto)} or use a {@link WScrollArea} to
 * automatically show scrollbars for widgets inserted in the layout to cope with
 * sizes that are smaller than their preferred size.
 * <p>
 * A layout manager may provide resize handles between columns or rows which
 * allow the user to change the automatic layout provided by the layout manager
 * (see {@link WGridLayout#setRowResizable(int row, boolean enabled)
 * setRowResizable()} and
 * {@link WGridLayout#setColumnResizable(int column, boolean enabled)
 * setColumnResizable()}).
 * <p>
 * Columns and rows are separated using a constant spacing, which defaults to 6
 * pixels by default, and can be changed using
 * {@link WGridLayout#setHorizontalSpacing(int size) setHorizontalSpacing()} and
 * {@link WGridLayout#setVerticalSpacing(int size) setVerticalSpacing()}. In
 * addition, when this layout is a top-level layout (i.e. is not nested inside
 * another layout), a margin is set around the contents. This margin defaults to
 * 9 pixels, and can be changed using
 * {@link WLayout#setContentsMargins(int left, int top, int right, int bottom)
 * WLayout#setContentsMargins()}.
 * <p>
 * For each column or row, a stretch factor may be defined, which controls how
 * remaining horizontal or vertical space is used. Each column and row is
 * stretched using the stretch factor to fill the remaining space. When the
 * stretch factor is 0, the height of the row and its contents is set to the
 * preferred size (if possible). When the stretch factor is 1 or higher, these
 * widgets will be given the remaining size, limited only by their minimum size
 * (their preferred size is ignored).
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	WContainerWidget w = new WContainerWidget(this);
 * 	w.resize(WLength.Auto, new WLength(600));
 * 
 * 	WGridLayout layout = new WGridLayout();
 * 	layout.addWidget(new WText(&quot;Item 0 0&quot;), 0, 0);
 * 	layout.addWidget(new WText(&quot;Item 0 1&quot;), 0, 1);
 * 	layout.addWidget(new WText(&quot;Item 1 0&quot;), 1, 0);
 * 	layout.addWidget(new WText(&quot;Item 1 1&quot;), 1, 1);
 * 
 * 	w.setLayout(layout);
 * }
 * </pre>
 * <p>
 * <p>
 * <i><b>Note: </b>When JavaScript support is not available, not all
 * functionality of the layout is available. In particular, vertical size
 * management is not available.
 * <p>
 * When a layout is used on a first page with progressive bootstrap, then the
 * layout will progress only in a limited way to a full JavaScript-based layout.
 * You can thus not rely on it to behave properly for example when dynamically
 * adding or removing widgets. </i>
 * </p>
 */
public class WGridLayout extends WLayout {
	private static Logger logger = LoggerFactory.getLogger(WGridLayout.class);

	/**
	 * Create a new grid layout.
	 * <p>
	 * The grid will grow dynamically as items are added.
	 * <p>
	 * Use <code>parent</code> = <code>null</code> to create a layout manager
	 * that can be nested inside other layout managers or if you use
	 * {@link WContainerWidget#setLayout(WLayout layout)
	 * WContainerWidget#setLayout()} to add specify the container later.
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

	public void clear() {
		int c = this.getCount();
		for (int i = 0; i < c; ++i) {
			WLayoutItem item = this.getItemAt(i);
			this.clearLayoutItem(item);
		}
		this.grid_.clear();
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
	 * Sets the <i>stretch</i> factor for row <code>row</code>.
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

	/**
	 * Sets whether the user may drag a particular column border.
	 * <p>
	 * This method sets whether the border that separates column <i>column</i>
	 * from the next column may be resized by the user, depending on the value
	 * of <i>enabled</i>.
	 * <p>
	 * The default value is <i>false</i>.
	 */
	public void setColumnResizable(int column, boolean enabled) {
		this.expand(0, column, 0, 1);
		this.grid_.columns_.get(column).resizable_ = enabled;
		this.update();
	}

	/**
	 * Sets whether the user may drag a particular column border.
	 * <p>
	 * Calls {@link #setColumnResizable(int column, boolean enabled)
	 * setColumnResizable(column, true)}
	 */
	public final void setColumnResizable(int column) {
		setColumnResizable(column, true);
	}

	/**
	 * Returns whether the user may drag a particular column border.
	 * <p>
	 * This method returns whether the border that separates column
	 * <i>column</i> from the next column may be resized by the user.
	 * <p>
	 * 
	 * @see WGridLayout#setColumnResizable(int column, boolean enabled)
	 */
	public boolean columnIsResizable(int column) {
		return this.grid_.columns_.get(column).resizable_;
	}

	/**
	 * Sets whether the user may drag a particular row border.
	 * <p>
	 * This method sets whether the border that separates row <i>row</i> from
	 * the next row may be resized by the user, depending on the value of
	 * <i>enabled</i>.
	 * <p>
	 * The default value is <i>false</i>.
	 */
	public void setRowResizable(int row, boolean enabled) {
		this.expand(row, 0, 1, 0);
		this.grid_.rows_.get(row).resizable_ = enabled;
		this.update();
	}

	/**
	 * Sets whether the user may drag a particular row border.
	 * <p>
	 * Calls {@link #setRowResizable(int row, boolean enabled)
	 * setRowResizable(row, true)}
	 */
	public final void setRowResizable(int row) {
		setRowResizable(row, true);
	}

	/**
	 * Returns whether the user may drag a particular row border.
	 * <p>
	 * This method returns whether the border that separates row <i>row</i> from
	 * the next row may be resized by the user.
	 * <p>
	 * 
	 * @see WGridLayout#setRowResizable(int row, boolean enabled)
	 */
	public boolean rowIsResizable(int row) {
		return this.grid_.rows_.get(row).resizable_;
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
