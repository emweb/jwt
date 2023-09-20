/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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
 * A layout manager which arranges widgets in a grid.
 *
 * <p>This layout manager arranges widgets in a grid.
 *
 * <p>Each grid cell (row, column) may contain one widget or nested layout. {@link
 * Orientation#Horizontal} and vertical space are divided so that each non-stretchable column/row is
 * given its preferred size (if possible) and the remaining space is divided according to stretch
 * factors among the columns/rows. If not all columns/rows can be given their preferred size (there
 * is not enough room), then columns/rows are given a smaller size (down to a minimum size based on
 * widget minimum sizes). If necessary, the container (or parent layout) of this layout is resized
 * to meet minimum size requirements.
 *
 * <p>The preferred width/height of a column/row is based on the natural size of the widgets, where
 * they present their contents without overflowing. {@link WWidget#resize(WLength width, WLength
 * height) WWidget#resize()} or (CSS <code>width</code>, <code>height</code> properties) can be used
 * to adjust the preferred size of a widget.
 *
 * <p>The minimum width/height of a column/row is based on the minimum dimensions of contained
 * widgets or nested layouts. The default minimum height and width for a widget is 0. It can be
 * specified using {@link WWidget#setMinimumSize(WLength width, WLength height)
 * WWidget#setMinimumSize()} or using CSS <code>min-width</code> and <code>min-height</code>
 * properties.
 *
 * <p>You should use {@link WContainerWidget#setOverflow(Overflow value, EnumSet orientation)
 * WContainerWidget#setOverflow()} to automatically show scrollbars for widgets inserted in the
 * layout to cope with a size set by the layout manager that is smaller than the preferred size.
 *
 * <p>When the container of a layout manager has a maximum size set using {@link
 * WWidget#setMaximumSize(WLength width, WLength height) WWidget#setMaximumSize()}, then the size of
 * the container will be based on the preferred size of the contents, up to this maximum size,
 * instead of the default behaviour of constraining the size of the children based on the size of
 * the container.
 *
 * <p>A layout manager may provide resize handles between columns or rows which allow the user to
 * change the automatic layout provided by the layout manager (see {@link
 * WGridLayout#setRowResizable(int row, boolean enabled, WLength initialSize) setRowResizable()} and
 * {@link WGridLayout#setColumnResizable(int column, boolean enabled, WLength initialSize)
 * setColumnResizable()}).
 *
 * <p>Columns and rows are separated using a constant spacing, which defaults to 6 pixels by
 * default, and can be changed using {@link WGridLayout#setHorizontalSpacing(int size)
 * setHorizontalSpacing()} and {@link WGridLayout#setVerticalSpacing(int size)
 * setVerticalSpacing()}. In addition, when this layout is a top-level layout (i.e. is not nested
 * inside another layout), a margin is set around the contents. This margin defaults to 9 pixels,
 * and can be changed using {@link WLayout#setContentsMargins(int left, int top, int right, int
 * bottom) WLayout#setContentsMargins()}.
 *
 * <p>For each column or row, a stretch factor may be defined, which controls how remaining
 * horizontal or vertical space is used. Each column and row is stretched using the stretch factor
 * to fill the remaining space. When the stretch factor is 0, the height of the row and its contents
 * is set to the preferred size (if possible). When the stretch factor is 1 or higher, these widgets
 * will be given the remaining size, limited only by their minimum size (their preferred size is
 * ignored).
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * WContainerWidget w = new WContainerWidget(this);
 * w.resize(WLength.Auto, new WLength(600));
 *
 * WGridLayout layout = new WGridLayout();
 * layout.addWidget(new WText("Item 0 0"), 0, 0);
 * layout.addWidget(new WText("Item 0 1"), 0, 1);
 * layout.addWidget(new WText("Item 1 0"), 1, 0);
 * layout.addWidget(new WText("Item 1 1"), 1, 1);
 *
 * w.setLayout(layout);
 *
 * }</pre>
 *
 * <p>
 *
 * <p><i><b>Note: </b>When JavaScript support is not available, not all functionality of the layout
 * is available. In particular, vertical size management is not available. </i>
 *
 * <p><i><b>Note: </b>When a layout is used on a first page with progressive bootstrap, then the
 * layout will progress only in a limited way to a full JavaScript-based layout. You can thus not
 * rely on it to behave properly for example when dynamically adding or removing widgets. </i>
 */
public class WGridLayout extends WLayout {
  private static Logger logger = LoggerFactory.getLogger(WGridLayout.class);

  /**
   * Create a new grid layout.
   *
   * <p>The grid will grow dynamically as items are added.
   *
   * <p>Use <code>parent</code> = <code>null</code> to create a layout manager that can be nested
   * inside other layout managers or if you use {@link WContainerWidget#setLayout(WLayout layout)
   * WContainerWidget#setLayout()} to add specify the container later.
   */
  public WGridLayout() {
    super();
    this.grid_ = new Grid();
  }

  public void addItem(WLayoutItem item) {
    this.addItem(item, 0, this.getColumnCount());
  }

  public WLayoutItem removeItem(WLayoutItem item) {
    int index = this.indexOf(item);
    WLayoutItem result = null;
    if (index != -1) {
      int row = index / this.getColumnCount();
      int col = index % this.getColumnCount();
      result = this.grid_.items_.get(row).get(col).item_;
      this.itemRemoved(item);
    }
    return result;
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
   *
   * <p>Adds the <i>item</i> at (<i>row</i>, <code>column</code>). If an item was already added to
   * that location, it is replaced.
   *
   * <p>An item may span several more rows or columns, which is controlled by <i>rowSpan</i> and
   * <code>columnSpan</code>.
   *
   * <p>The <code>alignment</code> specifies the vertical and horizontal alignment of the item. The
   * default value 0 indicates that the item is stretched to fill the entire grid cell. The
   * alignment can be specified as a logical combination of a horizontal alignment ({@link
   * AlignmentFlag#Left}, {@link AlignmentFlag#Center}, or {@link AlignmentFlag#Right}) and a
   * vertical alignment ({@link AlignmentFlag#Top}, {@link AlignmentFlag#Middle}, or {@link
   * AlignmentFlag#Bottom}).
   *
   * <p>
   *
   * @see WGridLayout#addLayout(WLayout layout, int row, int column, EnumSet alignment)
   * @see WGridLayout#addWidget(WWidget widget, int row, int column, EnumSet alignment)
   */
  public void addItem(
      WLayoutItem item,
      int row,
      int column,
      int rowSpan,
      int columnSpan,
      EnumSet<AlignmentFlag> alignment) {
    columnSpan = Math.max(1, columnSpan);
    rowSpan = Math.max(1, rowSpan);
    this.expand(row, column, rowSpan, columnSpan);
    final Grid.Item gridItem = this.grid_.items_.get(row).get(column);
    if (gridItem.item_ != null) {
      final WLayoutItem oldItem = gridItem.item_;
      this.itemRemoved(oldItem);
    }
    gridItem.item_ = item;
    gridItem.rowSpan_ = rowSpan;
    gridItem.colSpan_ = columnSpan;
    gridItem.alignment_ = EnumSet.copyOf(alignment);
    this.itemAdded(gridItem.item_);
  }
  /**
   * Adds a layout item to the grid.
   *
   * <p>Calls {@link #addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan,
   * EnumSet alignment) addItem(item, row, column, rowSpan, columnSpan, EnumSet.of(alignmen,
   * alignment))}
   */
  public final void addItem(
      WLayoutItem item,
      int row,
      int column,
      int rowSpan,
      int columnSpan,
      AlignmentFlag alignmen,
      AlignmentFlag... alignment) {
    addItem(item, row, column, rowSpan, columnSpan, EnumSet.of(alignmen, alignment));
  }
  /**
   * Adds a layout item to the grid.
   *
   * <p>Calls {@link #addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan,
   * EnumSet alignment) addItem(item, row, column, 1, 1, EnumSet.noneOf(AlignmentFlag.class))}
   */
  public final void addItem(WLayoutItem item, int row, int column) {
    addItem(item, row, column, 1, 1, EnumSet.noneOf(AlignmentFlag.class));
  }
  /**
   * Adds a layout item to the grid.
   *
   * <p>Calls {@link #addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan,
   * EnumSet alignment) addItem(item, row, column, rowSpan, 1, EnumSet.noneOf(AlignmentFlag.class))}
   */
  public final void addItem(WLayoutItem item, int row, int column, int rowSpan) {
    addItem(item, row, column, rowSpan, 1, EnumSet.noneOf(AlignmentFlag.class));
  }
  /**
   * Adds a layout item to the grid.
   *
   * <p>Calls {@link #addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan,
   * EnumSet alignment) addItem(item, row, column, rowSpan, columnSpan,
   * EnumSet.noneOf(AlignmentFlag.class))}
   */
  public final void addItem(WLayoutItem item, int row, int column, int rowSpan, int columnSpan) {
    addItem(item, row, column, rowSpan, columnSpan, EnumSet.noneOf(AlignmentFlag.class));
  }
  /**
   * Adds a nested layout item to the grid.
   *
   * <p>Adds the <i>layout</i> at (<i>row</i>, <code>column</code>). If an item was already added to
   * that location, it is replaced (but not deleted).
   *
   * <p>The <code>alignment</code> specifies the vertical and horizontal alignment of the item. The
   * default value 0 indicates that the item is stretched to fill the entire grid cell. The
   * alignment can be specified as a logical combination of a horizontal alignment ({@link
   * AlignmentFlag#Left}, {@link AlignmentFlag#Center}, or {@link AlignmentFlag#Right}) and a
   * vertical alignment ({@link AlignmentFlag#Top}, {@link AlignmentFlag#Middle}, or {@link
   * AlignmentFlag#Bottom}).
   *
   * <p>
   */
  public void addLayout(WLayout layout, int row, int column, EnumSet<AlignmentFlag> alignment) {
    this.addItem(layout, row, column, 1, 1, alignment);
  }
  /**
   * Adds a nested layout item to the grid.
   *
   * <p>Calls {@link #addLayout(WLayout layout, int row, int column, EnumSet alignment)
   * addLayout(layout, row, column, EnumSet.of(alignmen, alignment))}
   */
  public final void addLayout(
      WLayout layout, int row, int column, AlignmentFlag alignmen, AlignmentFlag... alignment) {
    addLayout(layout, row, column, EnumSet.of(alignmen, alignment));
  }
  /**
   * Adds a nested layout item to the grid.
   *
   * <p>Calls {@link #addLayout(WLayout layout, int row, int column, EnumSet alignment)
   * addLayout(layout, row, column, EnumSet.noneOf(AlignmentFlag.class))}
   */
  public final void addLayout(WLayout layout, int row, int column) {
    addLayout(layout, row, column, EnumSet.noneOf(AlignmentFlag.class));
  }
  /**
   * Adds a nested layout item to the grid.
   *
   * <p>Adds the <i>layout</i> at (<i>row</i>, <code>column</code>). If an item was already added to
   * that location, it is replaced (but not deleted).
   *
   * <p>An item may span several more rows or columns, which is controlled by <i>rowSpan</i> and
   * <code>columnSpan</code>.
   *
   * <p>The <code>alignment</code> specifies the vertical and horizontal alignment of the item. The
   * default value 0 indicates that the item is stretched to fill the entire grid cell. The
   * alignment can be specified as a logical combination of a horizontal alignment ({@link
   * AlignmentFlag#Left}, {@link AlignmentFlag#Center}, or {@link AlignmentFlag#Right}) and a
   * vertical alignment ({@link AlignmentFlag#Top}, {@link AlignmentFlag#Middle}, or {@link
   * AlignmentFlag#Bottom}).
   *
   * <p>
   */
  public void addLayout(
      WLayout layout,
      int row,
      int column,
      int rowSpan,
      int columnSpan,
      EnumSet<AlignmentFlag> alignment) {
    this.addItem(layout, row, column, rowSpan, columnSpan, alignment);
  }
  /**
   * Adds a nested layout item to the grid.
   *
   * <p>Calls {@link #addLayout(WLayout layout, int row, int column, int rowSpan, int columnSpan,
   * EnumSet alignment) addLayout(layout, row, column, rowSpan, columnSpan, EnumSet.of(alignmen,
   * alignment))}
   */
  public final void addLayout(
      WLayout layout,
      int row,
      int column,
      int rowSpan,
      int columnSpan,
      AlignmentFlag alignmen,
      AlignmentFlag... alignment) {
    addLayout(layout, row, column, rowSpan, columnSpan, EnumSet.of(alignmen, alignment));
  }
  /**
   * Adds a nested layout item to the grid.
   *
   * <p>Calls {@link #addLayout(WLayout layout, int row, int column, int rowSpan, int columnSpan,
   * EnumSet alignment) addLayout(layout, row, column, rowSpan, columnSpan,
   * EnumSet.noneOf(AlignmentFlag.class))}
   */
  public final void addLayout(WLayout layout, int row, int column, int rowSpan, int columnSpan) {
    addLayout(layout, row, column, rowSpan, columnSpan, EnumSet.noneOf(AlignmentFlag.class));
  }
  /**
   * Adds a widget to the grid.
   *
   * <p>Adds the <i>widget</i> at (<i>row</i>, <code>column</code>). If an item was already added to
   * that location, it is replaced (but not deleted).
   *
   * <p>The <code>alignment</code> specifies the vertical and horizontal alignment of the item. The
   * default value 0 indicates that the item is stretched to fill the entire grid cell. The
   * alignment can be specified as a logical combination of a horizontal alignment ({@link
   * AlignmentFlag#Left}, {@link AlignmentFlag#Center}, or {@link AlignmentFlag#Right}) and a
   * vertical alignment ({@link AlignmentFlag#Top}, {@link AlignmentFlag#Middle}, or {@link
   * AlignmentFlag#Bottom}).
   *
   * <p>
   */
  public void addWidget(WWidget widget, int row, int column, EnumSet<AlignmentFlag> alignment) {
    this.addItem(new WWidgetItem(widget), row, column, 1, 1, alignment);
  }
  /**
   * Adds a widget to the grid.
   *
   * <p>Calls {@link #addWidget(WWidget widget, int row, int column, EnumSet alignment)
   * addWidget(widget, row, column, EnumSet.of(alignmen, alignment))}
   */
  public final void addWidget(
      WWidget widget, int row, int column, AlignmentFlag alignmen, AlignmentFlag... alignment) {
    addWidget(widget, row, column, EnumSet.of(alignmen, alignment));
  }
  /**
   * Adds a widget to the grid.
   *
   * <p>Calls {@link #addWidget(WWidget widget, int row, int column, EnumSet alignment)
   * addWidget(widget, row, column, EnumSet.noneOf(AlignmentFlag.class))}
   */
  public final void addWidget(WWidget widget, int row, int column) {
    addWidget(widget, row, column, EnumSet.noneOf(AlignmentFlag.class));
  }
  /**
   * Adds a widget to the grid.
   *
   * <p>Adds the <i>widget</i> at (<i>row</i>, <code>column</code>). If an item was already added to
   * that location, it is replaced (but not deleted).
   *
   * <p>The widget may span several more rows or columns, which is controlled by <i>rowSpan</i> and
   * <code>columnSpan</code>.
   *
   * <p>The <code>alignment</code> specifies the vertical and horizontal alignment of the item. The
   * default value 0 indicates that the item is stretched to fill the entire grid cell. The
   * alignment can be specified as a logical combination of a horizontal alignment ({@link
   * AlignmentFlag#Left}, {@link AlignmentFlag#Center}, or {@link AlignmentFlag#Right}) and a
   * vertical alignment ({@link AlignmentFlag#Top}, {@link AlignmentFlag#Middle}, or {@link
   * AlignmentFlag#Bottom}).
   *
   * <p>
   */
  public void addWidget(
      WWidget widget,
      int row,
      int column,
      int rowSpan,
      int columnSpan,
      EnumSet<AlignmentFlag> alignment) {
    this.addItem(new WWidgetItem(widget), row, column, rowSpan, columnSpan, alignment);
  }
  /**
   * Adds a widget to the grid.
   *
   * <p>Calls {@link #addWidget(WWidget widget, int row, int column, int rowSpan, int columnSpan,
   * EnumSet alignment) addWidget(widget, row, column, rowSpan, columnSpan, EnumSet.of(alignmen,
   * alignment))}
   */
  public final void addWidget(
      WWidget widget,
      int row,
      int column,
      int rowSpan,
      int columnSpan,
      AlignmentFlag alignmen,
      AlignmentFlag... alignment) {
    addWidget(widget, row, column, rowSpan, columnSpan, EnumSet.of(alignmen, alignment));
  }
  /**
   * Adds a widget to the grid.
   *
   * <p>Calls {@link #addWidget(WWidget widget, int row, int column, int rowSpan, int columnSpan,
   * EnumSet alignment) addWidget(widget, row, column, rowSpan, columnSpan,
   * EnumSet.noneOf(AlignmentFlag.class))}
   */
  public final void addWidget(WWidget widget, int row, int column, int rowSpan, int columnSpan) {
    addWidget(widget, row, column, rowSpan, columnSpan, EnumSet.noneOf(AlignmentFlag.class));
  }
  /**
   * Sets the horizontal spacing.
   *
   * <p>The default horizontal spacing is 9 pixels.
   *
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
   *
   * <p>
   *
   * @see WGridLayout#setHorizontalSpacing(int size)
   */
  public int getHorizontalSpacing() {
    return this.grid_.horizontalSpacing_;
  }
  /**
   * Sets the vertical spacing.
   *
   * <p>The default vertical spacing is 9 pixels.
   *
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
   *
   * <p>
   *
   * @see WGridLayout#setVerticalSpacing(int size)
   */
  public int getVerticalSpacing() {
    return this.grid_.verticalSpacing_;
  }
  /**
   * Returns the column count.
   *
   * <p>The grid dimensions change dynamically when adding contents to the grid.
   *
   * <p>
   *
   * @see WGridLayout#getRowCount()
   */
  public int getColumnCount() {
    return this.grid_.columns_.size();
  }
  /**
   * Returns the row count.
   *
   * <p>The grid dimensions change dynamically when adding contents to the grid.
   *
   * <p>
   *
   * @see WGridLayout#getColumnCount()
   */
  public int getRowCount() {
    return this.grid_.rows_.size();
  }
  /**
   * Sets the column stretch.
   *
   * <p>Sets the <i>stretch</i> factor for column <code>column</code>.
   *
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
   *
   * <p>
   *
   * @see WGridLayout#setColumnStretch(int column, int stretch)
   */
  public int getColumnStretch(int column) {
    return this.grid_.columns_.get(column).stretch_;
  }
  /**
   * Sets the row stretch.
   *
   * <p>Sets the <i>stretch</i> factor for row <code>row</code>.
   *
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
   *
   * <p>
   *
   * @see WGridLayout#setRowStretch(int row, int stretch)
   */
  public int getRowStretch(int row) {
    return this.grid_.rows_.get(row).stretch_;
  }
  /**
   * Sets whether the user may drag a particular column border.
   *
   * <p>This method sets whether the border that separates column <i>column</i> from the next column
   * may be resized by the user, depending on the value of <i>enabled</i>.
   *
   * <p>The default value is <i>false</i>.
   *
   * <p>If an <code>initialSize</code> is given (that is not {@link WLength#Auto}), then this size
   * is used for the width of the column, overriding the width it would be given by the layout
   * manager.
   */
  public void setColumnResizable(int column, boolean enabled, final WLength initialSize) {
    this.expand(0, column, 0, 1);
    this.grid_.columns_.get(column).resizable_ = enabled;
    this.grid_.columns_.get(column).initialSize_ = initialSize;
    this.update();
  }
  /**
   * Sets whether the user may drag a particular column border.
   *
   * <p>Calls {@link #setColumnResizable(int column, boolean enabled, WLength initialSize)
   * setColumnResizable(column, true, WLength.Auto)}
   */
  public final void setColumnResizable(int column) {
    setColumnResizable(column, true, WLength.Auto);
  }
  /**
   * Sets whether the user may drag a particular column border.
   *
   * <p>Calls {@link #setColumnResizable(int column, boolean enabled, WLength initialSize)
   * setColumnResizable(column, enabled, WLength.Auto)}
   */
  public final void setColumnResizable(int column, boolean enabled) {
    setColumnResizable(column, enabled, WLength.Auto);
  }
  /**
   * Returns whether the user may drag a particular column border.
   *
   * <p>This method returns whether the border that separates column <i>column</i> from the next
   * column may be resized by the user.
   *
   * <p>
   *
   * @see WGridLayout#setColumnResizable(int column, boolean enabled, WLength initialSize)
   */
  public boolean columnIsResizable(int column) {
    return this.grid_.columns_.get(column).resizable_;
  }
  /**
   * Sets whether the user may drag a particular row border.
   *
   * <p>This method sets whether the border that separates row <i>row</i> from the next row may be
   * resized by the user, depending on the value of <i>enabled</i>.
   *
   * <p>The default value is <i>false</i>.
   *
   * <p>If an <code>initialSize</code> is given (that is not {@link WLength#Auto}), then this size
   * is used for the height of the row, overriding the height it would be given by the layout
   * manager.
   */
  public void setRowResizable(int row, boolean enabled, final WLength initialSize) {
    this.expand(row, 0, 1, 0);
    this.grid_.rows_.get(row).resizable_ = enabled;
    this.grid_.rows_.get(row).initialSize_ = initialSize;
    this.update();
  }
  /**
   * Sets whether the user may drag a particular row border.
   *
   * <p>Calls {@link #setRowResizable(int row, boolean enabled, WLength initialSize)
   * setRowResizable(row, true, WLength.Auto)}
   */
  public final void setRowResizable(int row) {
    setRowResizable(row, true, WLength.Auto);
  }
  /**
   * Sets whether the user may drag a particular row border.
   *
   * <p>Calls {@link #setRowResizable(int row, boolean enabled, WLength initialSize)
   * setRowResizable(row, enabled, WLength.Auto)}
   */
  public final void setRowResizable(int row, boolean enabled) {
    setRowResizable(row, enabled, WLength.Auto);
  }
  /**
   * Returns whether the user may drag a particular row border.
   *
   * <p>This method returns whether the border that separates row <i>row</i> from the next row may
   * be resized by the user.
   *
   * <p>
   *
   * @see WGridLayout#setRowResizable(int row, boolean enabled, WLength initialSize)
   */
  public boolean rowIsResizable(int row) {
    return this.grid_.rows_.get(row).resizable_;
  }

  public void iterateWidgets(final HandleWidgetMethod method) {
    for (int r = 0; r < this.grid_.rows_.size(); ++r) {
      for (int c = 0; c < this.grid_.columns_.size(); ++c) {
        WLayoutItem item = this.grid_.items_.get(r).get(c).item_;
        if (item != null) {
          item.iterateWidgets(method);
        }
      }
    }
  }

  private Grid grid_;

  private void expand(int row, int column, int rowSpan, int columnSpan) {
    int newRowCount = Math.max(this.getRowCount(), row + rowSpan);
    int newColumnCount = Math.max(this.getColumnCount(), column + columnSpan);
    int extraRows = newRowCount - this.getRowCount();
    int extraColumns = newColumnCount - this.getColumnCount();
    if (extraColumns > 0) {
      for (int a_row = 0; a_row < this.getRowCount(); ++a_row) {
        for (int i = 0; i < extraColumns; ++i) {
          this.grid_.items_.get(a_row).add(new Grid.Item());
        }
      }
      {
        int insertPos = this.grid_.columns_.size();
        for (int ii = 0; ii < (extraColumns); ++ii)
          this.grid_.columns_.add(insertPos + ii, new Grid.Section());
      }
      ;
    }
    if (extraRows > 0) {
      {
        int insertPos = this.grid_.items_.size();
        for (int ii = 0; ii < (extraRows); ++ii)
          this.grid_.items_.add(insertPos + ii, new ArrayList<Grid.Item>());
      }
      ;
      for (int i = 0; i < extraRows; ++i) {
        final List<Grid.Item> items =
            this.grid_.items_.get(this.grid_.items_.size() - extraRows + i);
        {
          int insertPos = items.size();
          for (int ii = 0; ii < (newColumnCount); ++ii) items.add(insertPos + ii, new Grid.Item());
        }
        ;
      }
      {
        int insertPos = this.grid_.rows_.size();
        for (int ii = 0; ii < (extraRows); ++ii)
          this.grid_.rows_.add(insertPos + ii, new Grid.Section());
      }
      ;
    }
  }

  public void setParentWidget(WWidget parent) {
    super.setParentWidget(parent);
    if (parent != null) {
      this.setImpl(new StdGridLayoutImpl2(this, this.grid_));
    }
  }
}
