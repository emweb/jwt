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
 * An abstract base class for item Views.
 *
 * <p>See {@link WTableView} or {@link WTreeView} for a description.
 *
 * <p>
 *
 * <h3>i18n</h3>
 *
 * <p>The strings used in this class can be translated by overriding the default values for the
 * following localization keys:
 *
 * <ul>
 *   <li>Wt.WAbstractItemView.PageIOfN: <b>{1}</b> of <b>{2}</b>
 *   <li>Wt.WAbstractItemView.PageBar.First: &amp;#xc2ab; First
 *   <li>Wt.WAbstractItemView.PageBar.Previous: &amp;#xe280b9; Previous
 *   <li>Wt.WAbstractItemView.PageBar.Next: Next &amp;#xe280ba;
 *   <li>Wt.WAbstractItemView.PageBar.Last: Last &amp;#xc2bb;
 * </ul>
 */
public abstract class WAbstractItemView extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WAbstractItemView.class);

  public void remove() {
    WApplication app = WApplication.getInstance();
    app.getStyleSheet().removeRule(this.headerHeightRule_);
    for (int i = 0; i < this.columns_.size(); ++i) {
      app.getStyleSheet().removeRule(this.columns_.get(i).styleRule);
    }
    super.remove();
  }
  /**
   * Sets the model.
   *
   * <p>The View will display data of the given <code>model</code> and changes in the model are
   * reflected by the View.
   *
   * <p>The initial model is <code>null</code>.
   *
   * <p>
   *
   * @see WAbstractItemView#setRootIndex(WModelIndex rootIndex)
   */
  public void setModel(final WAbstractItemModel model) {
    if (!this.columnWidthChanged_.isConnected()) {
      this.columnWidthChanged_.addListener(
          this,
          (Integer e1, Integer e2) -> {
            WAbstractItemView.this.updateColumnWidth(e1, e2);
          });
    }
    boolean isReset = this.model_ != null;
    for (int i = 0; i < this.modelConnections_.size(); ++i) {
      this.modelConnections_.get(i).disconnect();
    }
    this.modelConnections_.clear();
    this.model_ = model;
    this.headerModel_ = new HeaderProxyModel(this.model_);
    final WItemSelectionModel oldSelectionModel = this.selectionModel_;
    this.selectionModel_ = new WItemSelectionModel(model);
    this.selectionModel_.setSelectionBehavior(oldSelectionModel.getSelectionBehavior());
    this.delayedClearAndSelectIndex_ = null;
    this.editedItems_.clear();
    if (!isReset) {
      this.initDragDrop();
    }
    this.configureModelDragDrop();
    this.setRootIndex(null);
    this.setHeaderHeight(this.headerLineHeight_);
  }
  /**
   * Returns the model.
   *
   * <p>
   *
   * @see WAbstractItemView#setModel(WAbstractItemModel model)
   */
  public WAbstractItemModel getModel() {
    return this.model_;
  }
  /**
   * Sets the root index.
   *
   * <p>The root index is the model index that is considered the root node. This node itself is not
   * rendered, but its children are.
   *
   * <p>The default value is <code>null</code>, corresponding to the model&apos;s root node.
   *
   * <p>
   *
   * @see WAbstractItemView#setModel(WAbstractItemModel model)
   */
  public void setRootIndex(final WModelIndex rootIndex) {
    this.rootIndex_ = rootIndex;
    this.scheduleRerender(WAbstractItemView.RenderState.NeedRerender);
    int modelColumnCount = this.model_.getColumnCount(this.rootIndex_);
    WApplication app = WApplication.getInstance();
    while (this.columns_.size() > modelColumnCount) {
      int i = this.columns_.size() - 1;
      app.getStyleSheet().removeRule(this.columns_.get(i).styleRule);
      this.columns_.remove(0 + i);
    }
    while (this.columns_.size() < modelColumnCount) {
      this.columns_.add(this.createColumnInfo(this.columns_.size()));
    }
  }
  /**
   * Returns the root index.
   *
   * <p>
   *
   * @see WAbstractItemView#setRootIndex(WModelIndex rootIndex)
   */
  public WModelIndex getRootIndex() {
    return this.rootIndex_;
  }
  /**
   * Sets the default item delegate.
   *
   * <p>The previous delegate is not deleted. This item delegate is for all columns for which no
   * specific item delegate is set.
   *
   * <p>The default item delegate is a {@link WItemDelegate}.
   *
   * <p>
   *
   * @see WAbstractItemView#setItemDelegateForColumn(int column, WAbstractItemDelegate delegate)
   */
  public void setItemDelegate(final WAbstractItemDelegate delegate) {
    this.itemDelegate_ = delegate;
    this.itemDelegate_
        .closeEditor()
        .addListener(
            this,
            (WWidget e1, Boolean e2) -> {
              WAbstractItemView.this.closeEditorWidget(e1, e2);
            });
  }
  /**
   * Returns the default item delegate.
   *
   * <p>
   *
   * @see WAbstractItemView#setItemDelegate(WAbstractItemDelegate delegate)
   */
  public WAbstractItemDelegate getItemDelegate() {
    return this.itemDelegate_;
  }
  /**
   * Sets the delegate for a column.
   *
   * <p>A delegate previously set (if any) is not deleted.
   *
   * <p>
   *
   * @see WAbstractItemView#setItemDelegate(WAbstractItemDelegate delegate)
   */
  public void setItemDelegateForColumn(int column, final WAbstractItemDelegate delegate) {
    this.columnInfo(column).itemDelegate_ = delegate;
    delegate
        .closeEditor()
        .addListener(
            this,
            (WWidget e1, Boolean e2) -> {
              WAbstractItemView.this.closeEditorWidget(e1, e2);
            });
  }
  /**
   * Returns the delegate that was set for a column.
   *
   * <p>Returns <code>null</code> if no delegate was set for the column.
   *
   * <p>
   *
   * @see WAbstractItemView#setItemDelegateForColumn(int column, WAbstractItemDelegate delegate)
   */
  public WAbstractItemDelegate getItemDelegateForColumn(int column) {
    return this.columnInfo(column).itemDelegate_;
  }
  /**
   * Returns the delegate for rendering an item.
   *
   * <p>
   *
   * @see WAbstractItemView#setItemDelegateForColumn(int column, WAbstractItemDelegate delegate)
   * @see WAbstractItemView#setItemDelegate(WAbstractItemDelegate delegate)
   */
  public WAbstractItemDelegate getItemDelegate(final WModelIndex index) {
    return this.getItemDelegate(index.getColumn());
  }
  /**
   * Returns the delegate for a column.
   *
   * <p>Returns either the delegate that was set for the column, or the default delegate.
   */
  public WAbstractItemDelegate getItemDelegate(int column) {
    WAbstractItemDelegate result = this.getItemDelegateForColumn(column);
    return result != null ? result : this.itemDelegate_;
  }
  /**
   * Returns the widget that renders an item.
   *
   * <p>This returns the widget that renders the given item. This may return 0 if the item is
   * currently not rendered.
   *
   * <p>This widget has been created by an item delegate, and usually an item delegate is involved
   * when updating it.
   */
  public abstract WWidget itemWidget(final WModelIndex index);
  /**
   * Sets the header item delegate.
   *
   * <p>This item delegate is used for rendering items in the header.
   *
   * <p>The previous delegate is not deleted. This item delegate is for all columns for which no
   * specific item delegate is set.
   *
   * <p>The default item delegate is a {@link WItemDelegate}.
   */
  public void setHeaderItemDelegate(final WAbstractItemDelegate delegate) {
    this.headerItemDelegate_ = delegate;
  }
  /**
   * Returns the header item delegate.
   *
   * <p>
   *
   * @see WAbstractItemView#setHeaderItemDelegate(WAbstractItemDelegate delegate)
   */
  public WAbstractItemDelegate getHeaderItemDelegate() {
    return this.headerItemDelegate_;
  }
  /**
   * Sets the content alignment for a column.
   *
   * <p>The default value is {@link AlignmentFlag#Left}.
   *
   * <p>
   *
   * @see WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
   */
  public void setColumnAlignment(int column, AlignmentFlag alignment) {
    this.columnInfo(column).alignment = alignment;
    WApplication app = WApplication.getInstance();
    String align = null;
    switch (alignment) {
      case Left:
        align = app.getLayoutDirection() == LayoutDirection.LeftToRight ? "left" : "right";
        break;
      case Center:
        align = "center";
        break;
      case Right:
        align = app.getLayoutDirection() == LayoutDirection.LeftToRight ? "right" : "left";
        break;
      case Justify:
        align = "justify";
        break;
      default:
        break;
    }
    if (align != null) {
      WWidget w = this.columnInfo(column).styleRule.getTemplateWidget();
      w.setAttributeValue("style", "text-align: " + align);
    }
  }
  /**
   * Returns the content alignment for a column.
   *
   * <p>
   *
   * @see WAbstractItemView#setColumnAlignment(int column, AlignmentFlag alignment)
   */
  public AlignmentFlag getColumnAlignment(int column) {
    return this.columnInfo(column).alignment;
  }
  /**
   * Sets the header alignment for a column.
   *
   * <p>The default alignemnt is horizontally left, and vertically centered. ({@link
   * AlignmentFlag#Left} | {@link AlignmentFlag#Middle}).
   *
   * <p>Valid options for horizontal alignment are {@link AlignmentFlag#Left}, {@link
   * AlignmentFlag#Center} or {@link AlignmentFlag#Right}.
   *
   * <p>Valid options for vertical alignment are {@link AlignmentFlag#Middle} or {@link
   * AlignmentFlag#Top}. In the latter case, other contents may be added below the label in {@link
   * WAbstractItemView#createExtraHeaderWidget(int column) createExtraHeaderWidget()}.
   *
   * <p>
   *
   * @see WAbstractItemView#setColumnAlignment(int column, AlignmentFlag alignment)
   */
  public void setHeaderAlignment(int column, EnumSet<AlignmentFlag> alignment) {
    this.columnInfo(column).headerHAlignment =
        EnumUtils.enumFromSet(EnumUtils.mask(alignment, AlignmentFlag.AlignHorizontalMask));
    if (!EnumUtils.mask(alignment, AlignmentFlag.AlignVerticalMask).isEmpty()) {
      this.columnInfo(column).headerVAlignment =
          EnumUtils.enumFromSet(EnumUtils.mask(alignment, AlignmentFlag.AlignVerticalMask));
    }
    if (this.columnInfo(column).hidden
        || (int) this.renderState_.getValue()
            >= (int) WAbstractItemView.RenderState.NeedRerenderHeader.getValue()) {
      return;
    }
    WContainerWidget wc = ObjectUtils.cast(this.headerWidget(column), WContainerWidget.class);
    wc.setContentAlignment(alignment);
    if (this.columnInfo(column).headerVAlignment == AlignmentFlag.Middle) {
      wc.setLineHeight(this.headerLineHeight_);
    } else {
      wc.setLineHeight(WLength.Auto);
    }
  }
  /**
   * Sets the header alignment for a column.
   *
   * <p>Calls {@link #setHeaderAlignment(int column, EnumSet alignment) setHeaderAlignment(column,
   * EnumSet.of(alignmen, alignment))}
   */
  public final void setHeaderAlignment(
      int column, AlignmentFlag alignmen, AlignmentFlag... alignment) {
    setHeaderAlignment(column, EnumSet.of(alignmen, alignment));
  }
  /**
   * Returns the horizontal header alignment for a column.
   *
   * <p>
   *
   * @see WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
   */
  public AlignmentFlag horizontalHeaderAlignment(int column) {
    return this.columnInfo(column).headerHAlignment;
  }
  /**
   * Returns the vertical header alignment for a column.
   *
   * <p>
   *
   * @see WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
   */
  public AlignmentFlag verticalHeaderAlignment(int column) {
    return this.columnInfo(column).headerVAlignment;
  }
  /**
   * Configures header text wrapping.
   *
   * <p>This setting only affects a multiline header, and the default value is <code>true</code>.
   * When set to <code>false</code>, the header itself will not wrap (as with a vertically centered
   * header), and thus extra widgets will not shift down when there is a long header label.
   */
  public void setHeaderWordWrap(int column, boolean enabled) {
    this.columnInfo(column).headerWordWrap = enabled;
    if (this.columnInfo(column).hidden
        || (int) this.renderState_.getValue()
            >= (int) WAbstractItemView.RenderState.NeedRerenderHeader.getValue()) {
      return;
    }
    if (this.columnInfo(column).headerVAlignment == AlignmentFlag.Top) {
      WContainerWidget wc = ObjectUtils.cast(this.headerWidget(column), WContainerWidget.class);
      wc.toggleStyleClass("Wt-wwrap", enabled);
    }
  }
  // public boolean isHeaderWordWrap(int column) ;
  /**
   * Sets if alternating row colors are to be used.
   *
   * <p>Configure whether rows get alternating background colors, defined by the current CSS theme.
   *
   * <p>The default value is <code>false</code>.
   */
  public void setAlternatingRowColors(boolean enable) {
    this.alternatingRowColors_ = enable;
  }
  /**
   * Returns whether alternating row colors are used.
   *
   * <p>When enabled, rows are displayed in alternating row colors, according to the current
   * theme&apos;s definition.
   *
   * <p>
   *
   * @see WAbstractItemView#setAlternatingRowColors(boolean enable)
   */
  public boolean hasAlternatingRowColors() {
    return this.alternatingRowColors_;
  }
  /**
   * Sorts the data according to a column.
   *
   * <p>Sorts the data according to data in column <code>column</code> and sort order <code>order
   * </code>.
   *
   * <p>The default sorting column is -1: the model is unsorted.
   *
   * <p>
   *
   * @see WAbstractItemModel#sort(int column, SortOrder order)
   */
  public void sortByColumn(int column, SortOrder order) {
    if (this.currentSortColumn_ != -1) {
      WText t = this.headerSortIconWidget(this.currentSortColumn_);
      if (t != null) {
        t.setStyleClass("Wt-tv-sh Wt-tv-sh-none");
      }
    }
    this.currentSortColumn_ = column;
    this.columnInfo(column).sortOrder = order;
    if (this.renderState_ != WAbstractItemView.RenderState.NeedRerender) {
      WText t = this.headerSortIconWidget(this.currentSortColumn_);
      if (t != null) {
        t.setStyleClass(
            order == SortOrder.Ascending ? "Wt-tv-sh Wt-tv-sh-up" : "Wt-tv-sh Wt-tv-sh-down");
      }
    }
    this.model_.sort(column, order);
  }
  /**
   * Returns the current sorting columm.
   *
   * <p>
   *
   * @see WAbstractItemView#sortByColumn(int column, SortOrder order)
   * @see WAbstractItemView#getSortOrder()
   */
  public int getSortColumn() {
    return this.currentSortColumn_;
  }
  /**
   * Returns the current sorting order.
   *
   * <p>
   *
   * @see WAbstractItemView#sortByColumn(int column, SortOrder order)
   * @see WAbstractItemView#getSortColumn()
   */
  public SortOrder getSortOrder() {
    if (this.currentSortColumn_ >= 0 && this.currentSortColumn_ < (int) this.columns_.size()) {
      return this.columns_.get(this.currentSortColumn_).sortOrder;
    } else {
      return SortOrder.Ascending;
    }
  }
  /**
   * Enables or disables sorting for all columns.
   *
   * <p>Enable or disable sorting by the user on all columns.
   *
   * <p>Sorting is enabled by default.
   *
   * <p>
   *
   * @see WAbstractItemModel#sort(int column, SortOrder order)
   */
  public void setSortingEnabled(boolean enabled) {
    this.sorting_ = enabled;
    for (int i = 0; i < this.columns_.size(); ++i) {
      this.columnInfo(i).sorting = enabled;
    }
    this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
  }
  /**
   * Enables or disables sorting for a single column.
   *
   * <p>Enable or disable sorting by the user for a specific column.
   *
   * <p>Sorting is enabled by default.
   *
   * <p>
   *
   * @see WAbstractItemModel#sort(int column, SortOrder order)
   */
  public void setSortingEnabled(int column, boolean enabled) {
    this.columnInfo(column).sorting = enabled;
    this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
  }
  /**
   * Returns whether sorting is enabled.
   *
   * <p>
   *
   * @see WAbstractItemView#setSortingEnabled(boolean enabled)
   */
  public boolean isSortingEnabled() {
    return this.sorting_;
  }
  /**
   * Returns whether sorting is enabled for a single column.
   *
   * <p>
   *
   * @see WAbstractItemView#setSortingEnabled(boolean enabled)
   */
  public boolean isSortingEnabled(int column) {
    return this.columnInfo(column).sorting;
  }
  /**
   * Enables interactive column resizing.
   *
   * <p>Enable or disable column resize handles for interactive resizing of the columns.
   *
   * <p>Column resizing is enabled by default when JavaScript is available.
   *
   * <p>
   *
   * @see WAbstractItemView#setColumnResizeEnabled(boolean enabled)
   */
  public void setColumnResizeEnabled(boolean enabled) {
    if (enabled != this.columnResize_) {
      this.columnResize_ = enabled;
      this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
    }
  }
  /**
   * Returns whether column resizing is enabled.
   *
   * <p>
   *
   * @see WAbstractItemView#setColumnResizeEnabled(boolean enabled)
   */
  public boolean isColumnResizeEnabled() {
    return this.columnResize_;
  }
  /**
   * Changes the selection behaviour.
   *
   * <p>The selection behavior indicates whether whole rows or individual items can be selected. It
   * is a property of the {@link WAbstractItemView#getSelectionModel() getSelectionModel()}.
   *
   * <p>By default, selection operates on rows ({@link SelectionBehavior#Rows}), in which case model
   * indexes will always be in the first column (column <code>null</code>).
   *
   * <p>Alternatively, you can allow selection for individual items ({@link
   * SelectionBehavior#Items})
   *
   * <p>
   *
   * @see WItemSelectionModel#setSelectionBehavior(SelectionBehavior behavior)
   * @see WAbstractItemView#setSelectionMode(SelectionMode mode)
   */
  public void setSelectionBehavior(SelectionBehavior behavior) {
    if (behavior != this.getSelectionBehavior()) {
      this.clearSelection();
      this.selectionModel_.setSelectionBehavior(behavior);
    }
  }
  /**
   * Returns the selection behaviour.
   *
   * <p>
   *
   * @see WAbstractItemView#setSelectionBehavior(SelectionBehavior behavior)
   */
  public SelectionBehavior getSelectionBehavior() {
    return this.selectionModel_.getSelectionBehavior();
  }
  /**
   * Sets the selection mode.
   *
   * <p>By default selection is disabled ({@link SelectionMode#None}).
   *
   * <p>
   *
   * @see WAbstractItemView#setSelectionBehavior(SelectionBehavior behavior)
   */
  public void setSelectionMode(SelectionMode mode) {
    if (mode != this.selectionMode_) {
      this.clearSelection();
      this.selectionMode_ = mode;
    }
  }
  /**
   * Returns the selection mode.
   *
   * <p>
   *
   * @see WAbstractItemView#setSelectionMode(SelectionMode mode)
   */
  public SelectionMode getSelectionMode() {
    return this.selectionMode_;
  }
  /**
   * Returns the selection model.
   *
   * <p>The selection model keeps track of the currently selected items.
   */
  public WItemSelectionModel getSelectionModel() {
    return this.selectionModel_;
  }
  /**
   * Sets the selected items.
   *
   * <p>Replaces the current selection with <code>indexes</code>.
   *
   * <p>When selection operates on rows ({@link SelectionBehavior#Rows}), it is sufficient to pass
   * the first element in a row (column <code>null</code> ) to select the entire row.
   *
   * <p>
   *
   * @see WAbstractItemView#select(WModelIndex index, SelectionFlag option)
   * @see WAbstractItemView#getSelectionModel()
   */
  public void setSelectedIndexes(final SortedSet<WModelIndex> indexes) {
    if (indexes.isEmpty() && this.selectionModel_.selection_.isEmpty()) {
      return;
    }
    this.clearSelection();
    for (Iterator<WModelIndex> i_it = indexes.iterator(); i_it.hasNext(); ) {
      WModelIndex i = i_it.next();
      this.internalSelect(i, SelectionFlag.Select);
    }
    this.selectionChanged_.trigger();
  }
  /**
   * Clears the selection.
   *
   * <p>
   *
   * @see WAbstractItemView#setSelectedIndexes(SortedSet indexes)
   */
  public void clearSelection() {
    final SortedSet<WModelIndex> nodes = this.selectionModel_.selection_;
    while (!nodes.isEmpty()) {
      WModelIndex i = nodes.iterator().next();
      this.internalSelect(i, SelectionFlag.Deselect);
    }
  }
  /**
   * Selects a single item.
   *
   * <p>
   *
   * @see WAbstractItemView#setSelectedIndexes(SortedSet indexes)
   * @see WAbstractItemView#getSelectionModel()
   */
  public void select(final WModelIndex index, SelectionFlag option) {
    if (this.internalSelect(index, option)) {
      this.selectionChanged_.trigger();
    }
  }
  /**
   * Selects a single item.
   *
   * <p>Calls {@link #select(WModelIndex index, SelectionFlag option) select(index,
   * SelectionFlag.Select)}
   */
  public final void select(final WModelIndex index) {
    select(index, SelectionFlag.Select);
  }
  /**
   * Returns wheter an item is selected.
   *
   * <p>When selection operates on rows ({@link SelectionBehavior#Rows}), this method returns true
   * for each element in a selected row.
   *
   * <p>This is a convenience method for:
   *
   * <pre>{@code
   * selectionModel().isSelected(index)
   *
   * }</pre>
   *
   * <p>
   *
   * @see WAbstractItemView#getSelectedIndexes()
   * @see WAbstractItemView#select(WModelIndex index, SelectionFlag option)
   * @see WAbstractItemView#getSelectionModel()
   */
  public boolean isSelected(final WModelIndex index) {
    return this.selectionModel_.isSelected(index);
  }
  /**
   * Returns the set of selected items.
   *
   * <p>The model indexes are returned as a set, topologically ordered (in the order they appear in
   * the view).
   *
   * <p>When selection operates on rows ({@link SelectionBehavior#Rows}), this method only returns
   * the model index of first column&apos;s element of the selected rows.
   *
   * <p>This is a convenience method for:
   *
   * <pre>{@code
   * selectionModel().selectedIndexes()
   *
   * }</pre>
   *
   * <p>
   *
   * @see WAbstractItemView#setSelectedIndexes(SortedSet indexes)
   */
  public SortedSet<WModelIndex> getSelectedIndexes() {
    return this.selectionModel_.selection_;
  }
  /**
   * Enables the selection to be dragged (drag &amp; drop).
   *
   * <p>To enable dragging of the selection, you first need to enable selection using {@link
   * WAbstractItemView#setSelectionMode(SelectionMode mode) setSelectionMode()}.
   *
   * <p>Whether an individual item may be dragged is controlled by the item&apos;s {@link
   * ItemFlag#DragEnabled} flag. The selection can be dragged only if all items currently selected
   * can be dragged.
   *
   * <p>
   *
   * @see WAbstractItemView#setDropsEnabled(boolean enable)
   */
  public void setDragEnabled(boolean enable) {
    if (this.dragEnabled_ != enable) {
      this.dragEnabled_ = enable;
      if (enable) {
        this.uDragWidget_ = new WText();
        this.dragWidget_ = this.uDragWidget_;
        this.dragWidget_.setId(this.getId() + "dw");
        this.dragWidget_.setInline(false);
        this.dragWidget_.hide();
        this.setAttributeValue("dwid", this.dragWidget_.getId());
        if (this.getHeaderContainer() != null) {
          this.getHeaderContainer().addWidget(this.uDragWidget_);
        }
        this.configureModelDragDrop();
      }
    }
  }
  /**
   * Enables drop operations (drag &amp; drop).
   *
   * <p>When drop is enabled, the tree view will indicate that something may be dropped when the
   * mime-type of the dragged object is compatible with one of the model&apos;s accepted drop
   * mime-types (see {@link WAbstractItemModel#getAcceptDropMimeTypes()}) or this widget&apos;s
   * accepted drop mime-types (see {@link WWidget#acceptDrops(String mimeType, String
   * hoverStyleClass) WWidget#acceptDrops()}), and the target item has drop enabled (which is
   * controlled by the item&apos;s {@link ItemFlag#DropEnabled} flag).
   *
   * <p>Drop events must be handled in dropEvent().
   *
   * <p>
   *
   * <p>
   *
   * @see WAbstractItemView#setDragEnabled(boolean enable)
   * @deprecated Use {@link WAbstractItemView#setEnabledDropLocations(EnumSet dropLocations)
   *     setEnabledDropLocations()} instead. This method now enables {@link DropLocation#OnItem}.
   */
  public void setDropsEnabled(boolean enable) {
    if (enable) {
      this.setEnabledDropLocations(EnumSet.of(DropLocation.OnItem));
    } else {
      this.setEnabledDropLocations(EnumSet.noneOf(DropLocation.class));
    }
  }
  /**
   * Enables drop operations (drag &amp; drop).
   *
   * <p>When drop is enabled, the tree view will indicate that something may be dropped when the
   * mime-type of the dragged object is compatible with one of the model&apos;s accepted drop
   * mime-types (see {@link WAbstractItemModel#getAcceptDropMimeTypes()}) or this widget&apos;s
   * accepted drop mime-types (see {@link WWidget#acceptDrops(String mimeType, String
   * hoverStyleClass) WWidget#acceptDrops()}).
   *
   * <p>When {@link DropLocation#OnItem} is enabled, the view will allow drops on items that have
   * the {@link ItemFlag#DropEnabled} flag set. When {@link DropLocation#BetweenRows} is enabled,
   * the view will indicate that something may be dropped between any two rows. When {@link
   * DropLocation#OnItem} and {@link DropLocation#BetweenRows} are both enabled, the drop indication
   * differs depending on whether {@link ItemFlag#DropEnabled} is set on the item.
   *
   * <p>Drop events must be handled in dropEvent().
   */
  public void setEnabledDropLocations(EnumSet<DropLocation> dropLocations) {
    if (this.enabledDropLocations_.equals(dropLocations)) {
      return;
    }
    this.enabledDropLocations_ = EnumSet.copyOf(dropLocations);
    this.configureModelDragDrop();
    this.scheduleRender();
  }
  /**
   * Enables drop operations (drag &amp; drop).
   *
   * <p>Calls {@link #setEnabledDropLocations(EnumSet dropLocations)
   * setEnabledDropLocations(EnumSet.of(dropLocation, dropLocations))}
   */
  public final void setEnabledDropLocations(
      DropLocation dropLocation, DropLocation... dropLocations) {
    setEnabledDropLocations(EnumSet.of(dropLocation, dropLocations));
  }
  /** Returns the enabled drop locations. */
  public EnumSet<DropLocation> getEnabledDropLocations() {
    return this.enabledDropLocations_;
  }
  /**
   * Sets the row height.
   *
   * <p>The view renders all rows with a same height. This method configures this row height.
   *
   * <p>The default value is 20 pixels.
   *
   * <p>
   *
   * <p><i><b>Note: </b>The height must be specified in {@link LengthUnit#Pixel} units. </i>
   *
   * @see WAbstractItemView#setColumnWidth(int column, WLength width)
   */
  public void setRowHeight(final WLength rowHeight) {
    this.rowHeight_ = rowHeight;
  }
  /** Returns the row height. */
  public WLength getRowHeight() {
    return this.rowHeight_;
  }
  /**
   * Sets the column width.
   *
   * <p>The default column width is 150 pixels.
   *
   * <p>
   *
   * <p><i><b>Note: </b>The width must be specified in {@link LengthUnit#Pixel} units. </i>
   *
   * <p><i><b>Note: </b>The actual space occupied by each column is the column width augmented by 7
   * pixels for internal padding and a border. </i>
   */
  public abstract void setColumnWidth(int column, final WLength width);
  /**
   * Returns the column width.
   *
   * <p>
   *
   * @see WAbstractItemView#setColumnWidth(int column, WLength width)
   */
  public WLength getColumnWidth(int column) {
    return this.columnInfo(column).width;
  }
  /**
   * Changes the visibility of a column.
   *
   * <p>
   *
   * @see WAbstractItemView#isColumnHidden(int column)
   */
  public void setColumnHidden(int column, boolean hidden) {
    this.columnInfo(column).hidden = hidden;
  }
  /**
   * Returns if a column is hidden.
   *
   * <p>
   *
   * @see WAbstractItemView#setColumnHidden(int column, boolean hidden)
   */
  public boolean isColumnHidden(int column) {
    return this.columnInfo(column).hidden;
  }
  /**
   * Hides a column.
   *
   * <p>
   *
   * @see WAbstractItemView#showColumn(int column)
   * @see WAbstractItemView#setColumnHidden(int column, boolean hidden)
   */
  public void hideColumn(int column) {
    this.setColumnHidden(column, true);
  }
  /**
   * Shows a column.
   *
   * <p>
   *
   * @see WAbstractItemView#hideColumn(int column)
   * @see WAbstractItemView#setColumnHidden(int column, boolean hidden)
   */
  public void showColumn(int column) {
    this.setColumnHidden(column, false);
  }
  /**
   * Sets the header height.
   *
   * <p>The default value is 20 pixels.
   *
   * <p>
   *
   * <p><i><b>Note: </b>The height must be specified in {@link LengthUnit#Pixel} units. </i>
   */
  public void setHeaderHeight(final WLength height) {
    this.headerLineHeight_ = height;
    int lineCount = this.getHeaderLevelCount();
    WLength headerHeight = WLength.multiply(this.headerLineHeight_, lineCount);
    if (this.columns_.size() > 0) {
      WWidget w = this.headerWidget(0, false);
      if (w != null) {
        w.scheduleRender(EnumSet.of(RepaintFlag.SizeAffected));
      }
    }
    this.headerHeightRule_.getTemplateWidget().resize(WLength.Auto, headerHeight);
  }
  /**
   * Returns the header height.
   *
   * <p>
   *
   * @see WAbstractItemView#setHeaderHeight(WLength height)
   */
  public WLength getHeaderHeight() {
    return this.headerLineHeight_;
  }
  /**
   * Returns the number of pages.
   *
   * <p>When Ajax/JavaScript is not available, the view will use a paging navigation bar to allow
   * scrolling through the data. This returns the number of pages currently shown.
   *
   * <p>
   *
   * @see WAbstractItemView#getCreatePageNavigationBar()
   * @see WAbstractItemView#pageChanged()
   */
  public abstract int getPageCount();
  /**
   * Returns the page size.
   *
   * <p>When Ajax/JavaScript is not available, the view will use a paging navigation bar to allow
   * scrolling through the data. This returns the number of items per page.
   *
   * <p>
   *
   * @see WAbstractItemView#getCreatePageNavigationBar()
   * @see WAbstractItemView#pageChanged()
   */
  public abstract int getPageSize();
  /**
   * Returns the current page.
   *
   * <p>When Ajax/JavaScript is not available, the view will use a paging navigation bar to allow
   * scrolling through the data. This returns the current page (between 0 and {@link
   * WAbstractItemView#getPageCount() getPageCount()} - 1).
   *
   * <p>
   *
   * @see WAbstractItemView#getCreatePageNavigationBar()
   * @see WAbstractItemView#pageChanged()
   */
  public abstract int getCurrentPage();
  /**
   * Sets the current page.
   *
   * <p>When Ajax/JavaScript is not available, the view will use a paging navigation bar to allow
   * scrolling through the data. This method can be used to change the current page.
   *
   * <p>
   *
   * @see WAbstractItemView#getCreatePageNavigationBar()
   * @see WAbstractItemView#pageChanged()
   */
  public abstract void setCurrentPage(int page);
  /**
   * Scrolls the view to an item.
   *
   * <p>Scrolls the view to ensure that the item which represents the provided <code>index</code> is
   * visible. A <code>hint</code> may indicate how the item should appear in the viewport (if
   * possible).
   *
   * <p>
   *
   * <p><i><b>Note: </b>Currently only implemented to scroll to the correct row, not taking into
   * account the column. </i>
   */
  public abstract void scrollTo(final WModelIndex index, ScrollHint hint);
  /**
   * Scrolls the view to an item.
   *
   * <p>Calls {@link #scrollTo(WModelIndex index, ScrollHint hint) scrollTo(index,
   * ScrollHint.EnsureVisible)}
   */
  public final void scrollTo(final WModelIndex index) {
    scrollTo(index, ScrollHint.EnsureVisible);
  }
  /**
   * Configures what actions should trigger editing.
   *
   * <p>The default value is DoubleClicked.
   *
   * <p>
   *
   * @see WAbstractItemView#edit(WModelIndex index)
   */
  public void setEditTriggers(EnumSet<EditTrigger> editTriggers) {
    this.editTriggers_ = EnumSet.copyOf(editTriggers);
  }
  /**
   * Configures what actions should trigger editing.
   *
   * <p>Calls {@link #setEditTriggers(EnumSet editTriggers) setEditTriggers(EnumSet.of(editTrigger,
   * editTriggers))}
   */
  public final void setEditTriggers(EditTrigger editTrigger, EditTrigger... editTriggers) {
    setEditTriggers(EnumSet.of(editTrigger, editTriggers));
  }
  /**
   * Returns the editing triggers.
   *
   * <p>
   *
   * @see WAbstractItemView#setEditTriggers(EnumSet editTriggers)
   */
  public EnumSet<EditTrigger> getEditTriggers() {
    return this.editTriggers_;
  }
  /**
   * Configures editing options.
   *
   * <p>The default value is SingleEditor;
   */
  public void setEditOptions(EnumSet<EditOption> editOptions) {
    this.editOptions_ = EnumSet.copyOf(editOptions);
  }
  /**
   * Configures editing options.
   *
   * <p>Calls {@link #setEditOptions(EnumSet editOptions) setEditOptions(EnumSet.of(editOption,
   * editOptions))}
   */
  public final void setEditOptions(EditOption editOption, EditOption... editOptions) {
    setEditOptions(EnumSet.of(editOption, editOptions));
  }
  /**
   * Returns the editing options.
   *
   * <p>
   *
   * @see WAbstractItemView#setEditOptions(EnumSet editOptions)
   */
  public EnumSet<EditOption> getEditOptions() {
    return this.editOptions_;
  }
  /**
   * Opens an editor for the given index.
   *
   * <p>Unless multiple editors are enabled, any other open editor is closed first.
   *
   * <p>
   *
   * @see WAbstractItemView#setEditTriggers(EnumSet editTriggers)
   * @see WAbstractItemView#setEditOptions(EnumSet editOptions)
   * @see WAbstractItemView#closeEditor(WModelIndex index, boolean saveData)
   */
  public void edit(final WModelIndex index) {
    if (index.getFlags().contains(ItemFlag.Editable) && !this.isEditing(index)) {
      if (this.editOptions_.contains(EditOption.SingleEditor)) {
        while (!this.editedItems_.isEmpty()) {
          this.closeEditor(this.editedItems_.entrySet().iterator().next().getKey(), false);
        }
      }
      this.editedItems_.put(index, new WAbstractItemView.Editor());
      this.editedItems_.get(index).widget = (WWidget) null;
      this.editedItems_.get(index).stateSaved = false;
      this.modelDataChanged(index, index);
    }
  }
  /**
   * Closes the editor for the given index.
   *
   * <p>If <code>saveData</code> is true, then the currently edited value is saved first to the
   * model.
   *
   * <p>
   *
   * @see WAbstractItemView#edit(WModelIndex index)
   */
  public void closeEditor(final WModelIndex index, boolean saveData) {
    WAbstractItemView.Editor i = this.editedItems_.get(index);
    if (i != null) {
      WAbstractItemView.Editor editor = i;
      WModelIndex closed = index;
      this.editedItems_.remove(index);
      if (saveData || this.editOptions_.contains(EditOption.SaveWhenClosed)) {
        this.saveEditedValue(closed, editor);
      }
      this.modelDataChanged(closed, closed);
    }
  }
  /**
   * Closes the editor for the given index.
   *
   * <p>Calls {@link #closeEditor(WModelIndex index, boolean saveData) closeEditor(index, true)}
   */
  public final void closeEditor(final WModelIndex index) {
    closeEditor(index, true);
  }
  /**
   * Closes all open editors.
   *
   * <p>If <code>saveData</code> is true, then the currently edited values are saved to the model
   * before closing the editor.
   *
   * <p>
   *
   * @see WAbstractItemView#closeEditor(WModelIndex index, boolean saveData)
   */
  public void closeEditors(boolean saveData) {
    while (!this.editedItems_.isEmpty()) {
      this.closeEditor(this.editedItems_.entrySet().iterator().next().getKey(), saveData);
    }
  }
  /**
   * Closes all open editors.
   *
   * <p>Calls {@link #closeEditors(boolean saveData) closeEditors(true)}
   */
  public final void closeEditors() {
    closeEditors(true);
  }
  /**
   * Validates the editor for the given index.
   *
   * <p>Validation is done by invoking {@link WAbstractItemDelegate#validate(WModelIndex index,
   * Object editState) WAbstractItemDelegate#validate()}.
   */
  public ValidationState validateEditor(final WModelIndex index) {
    WAbstractItemView.Editor i = this.editedItems_.get(index);
    if (i != null) {
      WAbstractItemDelegate delegate = this.getItemDelegate(index);
      Object editState = new Object();
      final WAbstractItemView.Editor editor = i;
      if (editor.widget != null) {
        editState = delegate.getEditState(editor.widget, index);
      } else {
        editState = editor.editState;
      }
      ValidationState state = delegate.validate(index, editState);
      editor.valid = state == ValidationState.Valid;
      return state;
    }
    return ValidationState.Invalid;
  }
  /**
   * Validates all editors.
   *
   * <p>
   *
   * @see WAbstractItemView#validateEditor(WModelIndex index)
   */
  public ValidationState validateEditors() {
    ValidationState state = ValidationState.Valid;
    for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it =
            this.editedItems_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
      ValidationState s = this.validateEditor(i.getKey());
      if (s.getValue() < state.getValue()) {
        state = s;
      }
    }
    return state;
  }
  /**
   * Returns whether an editor is open for a given index.
   *
   * <p>
   *
   * @see WAbstractItemView#edit(WModelIndex index)
   */
  public boolean isEditing(final WModelIndex index) {
    return this.editedItems_.get(index) != null;
  }
  /** Returns whether an editor&apos;s state is valid. */
  public boolean isValid(final WModelIndex index) {
    WAbstractItemView.Editor i = this.editedItems_.get(index);
    if (i != null) {
      final WAbstractItemView.Editor editor = i;
      return editor.valid;
    } else {
      return false;
    }
  }

  boolean isEditing() {
    return !this.editedItems_.isEmpty();
  }
  /**
   * Signal emitted when clicked.
   *
   * <p>When the event happened over an item, the first argument indicates the item that was clicked
   * on.
   *
   * <p>
   *
   * @see WAbstractItemView#doubleClicked()
   */
  public Signal2<WModelIndex, WMouseEvent> clicked() {
    return this.clicked_;
  }
  /**
   * Signal emitted when double clicked.
   *
   * <p>When the event happened over an item, the first argument indicates the item that was double
   * clicked on.
   *
   * <p>
   *
   * @see WAbstractItemView#clicked()
   */
  public Signal2<WModelIndex, WMouseEvent> doubleClicked() {
    return this.doubleClicked_;
  }
  /**
   * Signal emitted when a mouse button is pressed down.
   *
   * <p>This signal is emitted only when &apos;over&apos; an item (the model index is passed as
   * first argument is never invalid).
   *
   * <p>
   *
   * @see WAbstractItemView#mouseWentUp()
   */
  public Signal2<WModelIndex, WMouseEvent> mouseWentDown() {
    return this.mouseWentDown_;
  }
  /**
   * Signal emitted when the mouse button is released.
   *
   * <p>When the event happened over an item, the first argument indicates the item where the mouse
   * went up.
   *
   * <p>
   *
   * @see WAbstractItemView#mouseWentDown()
   */
  public Signal2<WModelIndex, WMouseEvent> mouseWentUp() {
    return this.mouseWentUp_;
  }
  /**
   * Signal emitted when a finger is placed on the screen.
   *
   * <p>When the event happened over an item, the first argument indicates the item that was
   * touched.
   *
   * <p>
   *
   * @deprecated Use {@link WAbstractItemView#touchStarted() touchStarted()} instead.
   */
  public Signal2<WModelIndex, WTouchEvent> touchStart() {
    return this.touchStart_;
  }
  /**
   * Signal emitted when one or more fingers are placed on the screen.
   *
   * <p>When the event happened over an item, the first argument indicates the items that were
   * touched. The indices in the model index vector match the indices in the {@link
   * WTouchEvent#getChangedTouches()}.
   */
  public Signal2<List<WModelIndex>, WTouchEvent> touchStarted() {
    return this.touchStarted_;
  }
  /**
   * Signal emitted when one or more fingers are moved on the screen.
   *
   * <p>When the event happened over an item, the first argument indicates the items that were
   * touched. The indices in the model index vector match the indices in the {@link
   * WTouchEvent#getChangedTouches()}.
   */
  public Signal2<List<WModelIndex>, WTouchEvent> touchMoved() {
    return this.touchMoved_;
  }
  /**
   * Signal emitted when one or more fingers are removed from the screen.
   *
   * <p>When the event happened over an item, the first argument indicates the items where the touch
   * ended. The indices in the model index vector match the indices in the {@link
   * WTouchEvent#getChangedTouches()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>When JavaScript is disabled, the signal will never fire. </i>
   */
  public Signal2<List<WModelIndex>, WTouchEvent> touchEnded() {
    return this.touchEnded_;
  }
  /**
   * Signal emitted when the selection is changed.
   *
   * <p>
   *
   * @see WAbstractItemView#select(WModelIndex index, SelectionFlag option)
   * @see WAbstractItemView#setSelectionMode(SelectionMode mode)
   * @see WAbstractItemView#setSelectionBehavior(SelectionBehavior behavior)
   */
  public Signal selectionChanged() {
    return this.selectionChanged_;
  }
  /**
   * Signal emitted when page information was updated.
   *
   * <p>When Ajax/JavaScript is not available, the view will use a paging navigation bar to allow
   * scrolling through the data. This signal is emitted when page-related information changed (e.g.
   * the current page was changed, or the number of rows was changed).
   *
   * <p>
   *
   * @see WAbstractItemView#getCreatePageNavigationBar()
   */
  public Signal pageChanged() {
    return this.pageChanged_;
  }
  /**
   * Returns the signal emitted when a column is resized by the user.
   *
   * <p>The arguments of the signal are: the column index and the new width of the column.
   */
  public Signal2<Integer, WLength> columnResized() {
    return this.columnResized_;
  }
  /**
   * Returns whether the view is sortable.
   *
   * <p>When enabeld the view can be sorted by clicking on the header.
   */
  public boolean isSortEnabled() {
    return this.sortEnabled_;
  }
  /**
   * Alow to sort.
   *
   * <p>When enabeld the view can be sorted by clicking on the header.
   */
  public void setHeaderClickSortEnabled(boolean enabled) {
    this.sortEnabled_ = enabled;
  }
  /**
   * Signal emitted when a header item is clicked.
   *
   * <p>The argument that is passed is the column number.
   *
   * <p>
   *
   * @see WAbstractItemView#clicked()
   */
  public Signal2<Integer, WMouseEvent> headerClicked() {
    return this.headerClicked_;
  }
  /**
   * Signal emitted when a header item is double clicked.
   *
   * <p>The argument that is passed is the column number.
   *
   * <p>
   *
   * @see WAbstractItemView#doubleClicked()
   * @see WAbstractItemView#headerClicked()
   */
  public Signal2<Integer, WMouseEvent> headerDoubleClicked() {
    return this.headerDblClicked_;
  }
  /**
   * Signal emitted when a mouse button is pressed on a header item
   *
   * <p>The argument that is passed is the column number.
   *
   * <p>
   */
  public Signal2<Integer, WMouseEvent> headerMouseWentDown() {
    return this.headerMouseWentDown_;
  }
  /**
   * Signal emitted when a mouse button is released on a header item
   *
   * <p>The argument that is passed is the column number.
   *
   * <p>
   *
   * @see WAbstractItemView#headerMouseWentDown()
   */
  public Signal2<Integer, WMouseEvent> headerMouseWentUp() {
    return this.headerMouseWentUp_;
  }
  /**
   * Signal emitted when scrolling.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Works only if ajax is available. </i>
   */
  public abstract EventSignal1<WScrollEvent> scrolled();
  /**
   * Configures the number of columns that are used as row headers.
   *
   * <p>An item view does not use the vertical header data from the model in any way, but instead
   * you can configure data in the first column(s) to be used as a row headers.
   *
   * <p>These columns will not scroll horizontally together with the rest of the model.
   *
   * <p>The default value is 0.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Currently, this property must be set before any other settings of the view
   * and only a value of 0 or 1 is supported. </i>
   */
  public void setRowHeaderCount(int count) {
    this.rowHeaderCount_ = count;
  }
  /**
   * Returns the number of columns that are used as row headers.
   *
   * <p>
   *
   * @see WAbstractItemView#setRowHeaderCount(int count)
   */
  public int getRowHeaderCount() {
    return this.rowHeaderCount_;
  }
  /**
   * Event signal emitted when a keyboard key is pushed down.
   *
   * <p>The keyWentDown signal is the first signal emitted when a key is pressed (before the
   * keyPressed signal). Unlike {@link WAbstractItemView#keyPressed() keyPressed()} however it is
   * also emitted for modifier keys (such as &quot;shift&quot;, &quot;control&quot;, ...) or
   * keyboard navigation keys that do not have a corresponding character.
   *
   * <p>
   *
   * @see WAbstractItemView#keyPressed()
   * @see WAbstractItemView#keyWentUp()
   */
  public EventSignal1<WKeyEvent> keyWentDown() {
    this.impl_.setCanReceiveFocus(true);
    return this.impl_.keyWentDown();
  }
  /**
   * Event signal emitted when a &quot;character&quot; was entered.
   *
   * <p>The keyPressed signal is emitted when a key is pressed, and a character is entered. Unlike
   * {@link WAbstractItemView#keyWentDown() keyWentDown()}, it is emitted only for key presses that
   * result in a character being entered, and thus not for modifier keys or keyboard navigation
   * keys.
   *
   * <p>
   *
   * @see WAbstractItemView#keyWentDown()
   */
  public EventSignal1<WKeyEvent> keyPressed() {
    this.impl_.setCanReceiveFocus(true);
    return this.impl_.keyPressed();
  }
  /**
   * Event signal emitted when a keyboard key is released.
   *
   * <p>This is the counter-part of the {@link WAbstractItemView#keyWentDown() keyWentDown()} event.
   * Every key-down has its corresponding key-up.
   *
   * <p>
   *
   * @see WAbstractItemView#keyWentDown()
   */
  public EventSignal1<WKeyEvent> keyWentUp() {
    this.impl_.setCanReceiveFocus(true);
    return this.impl_.keyWentUp();
  }
  /** Creates a new item view. */
  protected WAbstractItemView(WContainerWidget parentContainer) {
    super(new WContainerWidget(), (WContainerWidget) null);
    this.renderState_ = WAbstractItemView.RenderState.NeedRerender;
    this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
    this.columns_ = new ArrayList<WAbstractItemView.ColumnInfo>();
    this.currentSortColumn_ = -1;
    this.dragEnabled_ = false;
    this.enabledDropLocations_ = EnumSet.noneOf(DropLocation.class);
    this.uDragWidget_ = null;
    this.dragWidget_ = null;
    this.model_ = null;
    this.headerModel_ = null;
    this.rootIndex_ = null;
    this.itemDelegate_ = null;
    this.headerItemDelegate_ = null;
    this.selectionModel_ = new WItemSelectionModel();
    this.rowHeight_ = new WLength(20);
    this.headerLineHeight_ = new WLength(20);
    this.selectionMode_ = SelectionMode.None;
    this.sorting_ = true;
    this.columnResize_ = true;
    this.defaultHeaderVAlignment_ = AlignmentFlag.Middle;
    this.defaultHeaderWordWrap_ = true;
    this.rowHeaderCount_ = 0;
    this.computedDragMimeType_ = new WString();
    this.sortEnabled_ = true;
    this.delayedClearAndSelectIndex_ = null;
    this.columnWidthChanged_ =
        new JSignal2<Integer, Integer>(this.getImplementation(), "columnResized") {};
    this.columnResized_ = new Signal2<Integer, WLength>();
    this.headerHeightRule_ = null;
    this.nextColumnId_ = 1;
    this.alternatingRowColors_ = false;
    this.resizeHandleMDownJS_ = new JSlot();
    this.editedItems_ = new HashMap<WModelIndex, WAbstractItemView.Editor>();
    this.headerClicked_ = new Signal2<Integer, WMouseEvent>();
    this.headerDblClicked_ = new Signal2<Integer, WMouseEvent>();
    this.headerMouseWentDown_ = new Signal2<Integer, WMouseEvent>();
    this.headerMouseWentUp_ = new Signal2<Integer, WMouseEvent>();
    this.clicked_ = new Signal2<WModelIndex, WMouseEvent>();
    this.doubleClicked_ = new Signal2<WModelIndex, WMouseEvent>();
    this.mouseWentDown_ = new Signal2<WModelIndex, WMouseEvent>();
    this.mouseWentUp_ = new Signal2<WModelIndex, WMouseEvent>();
    this.touchStart_ = new Signal2<WModelIndex, WTouchEvent>();
    this.touchStarted_ = new Signal2<List<WModelIndex>, WTouchEvent>();
    this.touchMoved_ = new Signal2<List<WModelIndex>, WTouchEvent>();
    this.touchEnded_ = new Signal2<List<WModelIndex>, WTouchEvent>();
    this.selectionChanged_ = new Signal();
    this.pageChanged_ = new Signal();
    this.editTriggers_ = EnumSet.of(EditTrigger.DoubleClicked);
    this.editOptions_ = EnumSet.of(EditOption.SingleEditor);
    this.touchRegistered_ = false;
    this.impl_ = ObjectUtils.cast(this.getImplementation(), WContainerWidget.class);
    WItemDelegate d = new WItemDelegate();
    this.setItemDelegate(d);
    this.setHeaderItemDelegate(d);
    WApplication app = WApplication.getInstance();
    SizeHandle.loadJavaScript(app);
    if (!app.getEnvironment().hasAjax()) {
      this.columnResize_ = false;
    }
    this.bindObjJS(this.resizeHandleMDownJS_, "resizeHandleMDown");
    this.headerHeightRule_ = new WCssTemplateRule("#" + this.getId() + " .headerrh");
    app.getStyleSheet().addRule(this.headerHeightRule_);
    this.setHeaderHeight(this.headerLineHeight_);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new item view.
   *
   * <p>Calls {@link #WAbstractItemView(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  protected WAbstractItemView() {
    this((WContainerWidget) null);
  }
  /**
   * Handles a drop event (drag &amp; drop).
   *
   * <p>The <code>event</code> object contains details about the drop operation, identifying the
   * source (which provides the data) and the mime-type of the data. The drop was received on the
   * <code>target</code> item.
   *
   * <p>The drop event can be handled either by the view itself, or by the model. The default
   * implementation checks if the mime-type is accepted by the model, and if so passes the drop
   * event to the model. If the source is the view&apos;s own selection model, then the drop event
   * will be handled as a {@link DropAction#Move}, otherwise the drop event will be handled as a
   * {@link DropAction#Copy}.
   *
   * <p>
   *
   * @see WAbstractItemModel#dropEvent(WDropEvent e, DropAction action, int row, int column,
   *     WModelIndex parent)
   */
  protected void dropEvent(final WDropEvent e, final WModelIndex index) {
    if (this.enabledDropLocations_.contains(DropLocation.OnItem)) {
      List<String> acceptMimeTypes = this.model_.getAcceptDropMimeTypes();
      for (int i = 0; i < acceptMimeTypes.size(); ++i) {
        if (acceptMimeTypes.get(i).equals(e.getMimeType())) {
          boolean internal = e.getSource() == this.selectionModel_;
          DropAction action = internal ? DropAction.Move : DropAction.Copy;
          this.model_.dropEvent(e, action, index.getRow(), index.getColumn(), index.getParent());
          this.setSelectedIndexes(new TreeSet<WModelIndex>());
          return;
        }
      }
    }
    super.dropEvent(e);
  }
  /**
   * Handles a drop event (drag &amp; drop).
   *
   * <p>The <code>event</code> object contains details about the drop operation, identifying the
   * source (which provides the data) and the mime-type of the data. The drop was received relative
   * to the <code>index</code> item and the <code>side</code> parameter will only be Wt::Top or
   * Wt::Bottom.
   *
   * <p>A drop below the lowest item or on an empty view will result in a call to this method with
   * an invalid index and side Wt::Bottom.
   *
   * <p>The drop event can be handled either by the view itself, or by the model. The default
   * implementation checks if the mime-type is accepted by the model, and if so passes the drop
   * event to the model as a {@link DropAction#Move}.
   *
   * <p>
   *
   * @see WAbstractItemModel#dropEvent(WDropEvent e, DropAction action, int row, int column,
   *     WModelIndex parent)
   */
  protected void dropEvent(final WDropEvent e, final WModelIndex index, Side side) {
    if (this.enabledDropLocations_.contains(DropLocation.BetweenRows)) {
      List<String> acceptMimeTypes = this.model_.getAcceptDropMimeTypes();
      for (int i = 0; i < acceptMimeTypes.size(); ++i) {
        if (acceptMimeTypes.get(i).equals(e.getMimeType())) {
          this.model_.dropEvent(e, DropAction.Move, index, side);
          this.setSelectedIndexes(new TreeSet<WModelIndex>());
          return;
        }
      }
    }
    super.dropEvent(e);
  }
  /**
   * Create an extra widget in the header.
   *
   * <p>You may reimplement this method to provide an extra widget to be placed below the header
   * label. The extra widget will be visible only if a multi-line header is configured using {@link
   * WAbstractItemView#setHeaderHeight(WLength height) setHeaderHeight()}.
   *
   * <p>The widget is created only once, but this method may be called repeatedly for a column for
   * which prior calls returned <code>null</code> (i.e. each time the header is rerendered).
   *
   * <p>The default implementation returns <code>null</code>.
   *
   * <p>
   *
   * @see WAbstractItemView#setHeaderHeight(WLength height)
   * @see WAbstractItemView#extraHeaderWidget(int column)
   */
  protected WWidget createExtraHeaderWidget(int column) {
    return null;
  }
  /**
   * Returns the extra header widget.
   *
   * <p>Returns the widget previously created using {@link
   * WAbstractItemView#createExtraHeaderWidget(int column) createExtraHeaderWidget()}
   *
   * <p>
   *
   * @see WAbstractItemView#createExtraHeaderWidget(int column)
   */
  protected WWidget extraHeaderWidget(int column) {
    return this.columnInfo(column).extraHeaderWidget;
  }
  /**
   * Returns a page navigation widget.
   *
   * <p>When Ajax/JavaScript is not available, the view will use a paging navigation bar to allow
   * scrolling through the data, created by this method. The default implementation displays a
   * simple navigation bar with (First, Prevous, Next, Last) buttons and a page counter.
   *
   * <p>You may want to reimplement this method to provide a custom page navigation bar. You can use
   * the {@link WAbstractItemView#getCurrentPage() getCurrentPage()}, {@link
   * WAbstractItemView#getPageCount() getPageCount()}, and {@link
   * WAbstractItemView#setCurrentPage(int page) setCurrentPage()} methods to set or get the page
   * information, and listen to the {@link WAbstractItemView#pageChanged() pageChanged()} signal to
   * react to changes.
   */
  protected WWidget getCreatePageNavigationBar() {
    return new DefaultPagingBar(this);
  }

  static class ColumnInfo {
    private static Logger logger = LoggerFactory.getLogger(ColumnInfo.class);

    public WCssTemplateRule styleRule;
    public int id;
    public SortOrder sortOrder;
    public AlignmentFlag alignment;
    public AlignmentFlag headerHAlignment;
    public AlignmentFlag headerVAlignment;
    public boolean headerWordWrap;
    public WLength width;
    public WWidget extraHeaderWidget;
    public boolean sorting;
    public boolean hidden;
    public WAbstractItemDelegate itemDelegate_;

    public String getStyleClass() {
      return "Wt-tv-c" + String.valueOf(this.id);
    }

    public ColumnInfo(WAbstractItemView view, int anId) {
      this.styleRule = null;
      this.id = anId;
      this.sortOrder = SortOrder.Ascending;
      this.alignment = AlignmentFlag.Left;
      this.headerHAlignment = AlignmentFlag.Left;
      this.headerVAlignment = view.defaultHeaderVAlignment_;
      this.headerWordWrap = view.defaultHeaderWordWrap_;
      this.width = new WLength();
      this.extraHeaderWidget = (WWidget) null;
      this.sorting = view.sorting_;
      this.hidden = false;
      this.itemDelegate_ = (WAbstractItemDelegate) null;
      this.width = new WLength(150);
      WCssTemplateRule r = new WCssTemplateRule("#" + view.getId() + " ." + this.getStyleClass());
      r.getTemplateWidget().resize(new WLength(this.width.toPixels()), WLength.Auto);
      this.styleRule = r;
      CssUtils.add(WApplication.getInstance().getStyleSheet(), r);
    }
  }

  enum RenderState {
    RenderOk(0),
    NeedAdjustViewPort(1),
    NeedUpdateModelIndexes(2),
    NeedRerenderData(3),
    NeedRerenderHeader(4),
    NeedRerender(5);

    private int value;

    RenderState(int value) {
      this.value = value;
    }

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return value;
    }
  }

  WContainerWidget impl_;
  WAbstractItemView.RenderState renderState_;
  List<AbstractSignal.Connection> modelConnections_;
  List<WAbstractItemView.ColumnInfo> columns_;
  int currentSortColumn_;
  boolean dragEnabled_;
  protected EnumSet<DropLocation> enabledDropLocations_;
  protected WWidget uDragWidget_;
  WWidget dragWidget_;

  void scheduleRerender(WAbstractItemView.RenderState what) {
    if (what == WAbstractItemView.RenderState.NeedRerenderHeader
            && this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData
        || what == WAbstractItemView.RenderState.NeedRerenderData
            && this.renderState_ == WAbstractItemView.RenderState.NeedRerenderHeader) {
      this.renderState_ = WAbstractItemView.RenderState.NeedRerender;
    } else {
      this.renderState_ = EnumUtils.max(what, this.renderState_);
    }
    if (!this.isRendered()) {
      return;
    }
    this.scheduleRender();
  }

  abstract void modelDataChanged(final WModelIndex topLeft, final WModelIndex bottomRight);

  void modelLayoutAboutToBeChanged() {
    if ((this.rootIndex_ != null)) {
      this.rootIndex_.encodeAsRawIndex();
    }
    for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it =
            this.editedItems_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
      this.persistEditor(i.getKey(), i.getValue());
      i.getKey().encodeAsRawIndex();
    }
    this.selectionModel_.modelLayoutAboutToBeChanged();
  }

  void modelLayoutChanged() {
    if ((this.rootIndex_ != null)) {
      this.rootIndex_ = this.rootIndex_.decodeFromRawIndex();
    }
    Map<WModelIndex, WAbstractItemView.Editor> newEditorMap =
        new HashMap<WModelIndex, WAbstractItemView.Editor>();
    for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it =
            this.editedItems_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
      WModelIndex m = i.getKey().decodeFromRawIndex();
      if ((m != null)) {
        newEditorMap.put(m, i.getValue());
      }
    }
    this.editedItems_ = newEditorMap;
    this.selectionModel_.modelLayoutChanged();
    this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
    this.selectionChanged().trigger();
  }

  protected void modelHeaderDataChanged(Orientation orientation, int start, int end) {
    if ((int) this.renderState_.getValue()
        < (int) WAbstractItemView.RenderState.NeedRerenderHeader.getValue()) {
      if (orientation == Orientation.Horizontal) {
        for (int i = start; i <= end; ++i) {
          WContainerWidget hw =
              ObjectUtils.cast(this.headerWidget(i, true), WContainerWidget.class);
          WWidget tw = hw.getWidget(hw.getCount() - 1);
          {
            WWidget toRemove =
                this.headerItemDelegate_.update(
                    tw, this.headerModel_.getIndex(0, i), EnumSet.noneOf(ViewItemRenderFlag.class));
            if (toRemove != null) toRemove.remove();
          }

          tw.setInline(false);
          tw.addStyleClass("Wt-label");
          WWidget h = this.headerWidget(i, false);
          final WAbstractItemView.ColumnInfo info = this.columnInfo(i);
          h.setStyleClass(info.getStyleClass() + " Wt-tv-c headerrh");
          String sc =
              StringUtils.asString(
                      this.headerModel_.getIndex(0, i).getData(ItemDataRole.StyleClass))
                  .toString();
          if (sc.length() != 0) {
            h.addStyleClass(sc);
          }
        }
      }
    }
  }

  void modelReset() {
    this.setModel(this.model_);
  }

  WAbstractItemView.ColumnInfo columnInfo(int column) {
    while (column >= (int) this.columns_.size()) {
      this.columns_.add(this.createColumnInfo(this.columns_.size()));
    }
    return this.columns_.get(column);
  }

  int columnById(int columnid) {
    for (int i = 0; i < this.columns_.size(); ++i) {
      if (this.columnInfo(i).id == columnid) {
        return i;
      }
    }
    return 0;
  }

  int getColumnCount() {
    return this.columns_.size();
  }

  int getVisibleColumnCount() {
    int result = 0;
    for (int i = 0; i < this.columns_.size(); ++i) {
      if (!this.columns_.get(i).hidden) {
        ++result;
      }
    }
    return result;
  }

  int visibleColumnIndex(int modelColumn) {
    if (this.columns_.get(modelColumn).hidden) {
      return -1;
    }
    int j = 0;
    for (int i = 0; i < modelColumn; ++i) {
      if (!this.columns_.get(i).hidden) {
        ++j;
      }
    }
    return j;
  }

  int modelColumnIndex(int visibleColumn) {
    int j = -1;
    for (int i = 0; i <= visibleColumn; ++i) {
      ++j;
      while ((int) j < this.columns_.size() && this.columns_.get(j).hidden) {
        ++j;
      }
      if ((int) j >= this.columns_.size()) {
        return -1;
      }
    }
    return j;
  }

  WAbstractItemView.ColumnInfo createColumnInfo(int column) {
    return new WAbstractItemView.ColumnInfo(this, this.nextColumnId_++);
  }

  void saveExtraHeaderWidgets() {
    for (int i = 0; i < this.getColumnCount(); ++i) {
      WWidget w = this.columnInfo(i).extraHeaderWidget;
      if (w != null && w.getParent() != null) {
        w.getParent().removeWidget(w);
      }
    }
  }

  protected WWidget createHeaderWidget(int column) {
    final WAbstractItemView.ColumnInfo info = this.columnInfo(column);
    WContainerWidget contents = new WContainerWidget();
    contents.setObjectName("contents");
    if (info.sorting) {
      WText sortIcon = new WText();
      sortIcon.setObjectName("sort");
      sortIcon.setInline(false);
      sortIcon.setStyleClass("Wt-tv-sh Wt-tv-sh-none");
      if (this.currentSortColumn_ == column) {
        sortIcon.setStyleClass(
            info.sortOrder == SortOrder.Ascending
                ? "Wt-tv-sh Wt-tv-sh-up"
                : "Wt-tv-sh Wt-tv-sh-down");
      }
      contents.addWidget(sortIcon);
    }
    if (!EnumUtils.mask(
            this.model_.getHeaderFlags(column),
            EnumSet.of(HeaderFlag.ColumnIsExpandedLeft, HeaderFlag.ColumnIsExpandedRight))
        .isEmpty()) {
      WImage collapseIcon = new WImage();
      collapseIcon.setFloatSide(Side.Left);
      collapseIcon.setImageLink(new WLink(WApplication.getRelativeResourcesUrl() + "minus.gif"));
      collapseIcon
          .clicked()
          .addListener(
              this,
              () -> {
                WAbstractItemView.this.collapseColumn(info.id);
              });
      contents.addWidget(collapseIcon);
    } else {
      if (this.model_.getHeaderFlags(column).contains(HeaderFlag.ColumnIsCollapsed)) {
        WImage expandIcon = new WImage();
        expandIcon.setFloatSide(Side.Left);
        expandIcon.setImageLink(new WLink(WApplication.getRelativeResourcesUrl() + "plus.gif"));
        expandIcon
            .clicked()
            .addListener(
                this,
                () -> {
                  WAbstractItemView.this.expandColumn(info.id);
                });
        contents.addWidget(expandIcon);
      }
    }
    WModelIndex index = this.headerModel_.getIndex(0, column);
    WWidget i =
        this.headerItemDelegate_.update(
            (WWidget) null, index, EnumSet.noneOf(ViewItemRenderFlag.class));
    i.setInline(false);
    i.addStyleClass("Wt-label");
    contents.addWidget(i);
    if (this.isDisabled()) {
      contents.addStyleClass("Wt-disabled");
      for (WWidget child : contents.getChildren()) {
        child.addStyleClass("Wt-disabled");
      }
    }
    int headerLevel = this.model_ != null ? this.headerLevel(column) : 0;
    contents.setMargin(
        new WLength(headerLevel * this.headerLineHeight_.toPixels()), EnumSet.of(Side.Top));
    int rightBorderLevel = headerLevel;
    if (this.model_ != null) {
      int rightColumn = this.modelColumnIndex(this.visibleColumnIndex(column) + 1);
      if (rightColumn != -1) {
        EnumSet<HeaderFlag> flagsLeft = this.model_.getHeaderFlags(column);
        EnumSet<HeaderFlag> flagsRight = this.model_.getHeaderFlags(rightColumn);
        int rightHeaderLevel = this.headerLevel(rightColumn);
        if (flagsLeft.contains(HeaderFlag.ColumnIsExpandedRight)) {
          rightBorderLevel = headerLevel + 1;
        } else {
          if (flagsRight.contains(HeaderFlag.ColumnIsExpandedLeft)) {
            rightBorderLevel = rightHeaderLevel + 1;
          } else {
            rightBorderLevel = Math.min(headerLevel, rightHeaderLevel);
          }
        }
      }
    }
    boolean activeRH = this.columnResize_;
    WContainerWidget resizeHandle = new WContainerWidget();
    resizeHandle.setStyleClass(
        "Wt-tv-rh" + (activeRH ? "" : " Wt-tv-no-rh") + " Wt-tv-br headerrh");
    if (activeRH) {
      resizeHandle.mouseWentDown().addListener(this.resizeHandleMDownJS_);
    }
    resizeHandle.setMargin(
        new WLength(rightBorderLevel * this.headerLineHeight_.toPixels()), EnumSet.of(Side.Top));
    if (!(this.columnInfo(column).extraHeaderWidget != null)) {
      this.columnInfo(column).extraHeaderWidget = this.createExtraHeaderWidget(column);
    }
    WWidget extraW = this.columnInfo(column).extraHeaderWidget;
    WContainerWidget result = new WContainerWidget();
    result.setStyleClass(info.getStyleClass() + " Wt-tv-c headerrh");
    result.addWidget(resizeHandle);
    WContainerWidget main = new WContainerWidget();
    main.setOverflow(Overflow.Hidden);
    main.setContentAlignment(info.headerHAlignment);
    main.addWidget(contents);
    if (info.headerVAlignment == AlignmentFlag.Middle) {
      main.setLineHeight(this.headerLineHeight_);
    } else {
      main.setLineHeight(WLength.Auto);
      if (info.headerWordWrap) {
        main.addStyleClass("Wt-wwrap");
      }
    }
    if (extraW != null) {
      main.addWidget(extraW);
    }
    main.clicked()
        .addListener(
            this,
            (WMouseEvent event) -> {
              WAbstractItemView.this.handleHeaderClicked(info.id, event);
            });
    main.mouseWentDown()
        .addListener(
            this,
            (WMouseEvent event) -> {
              WAbstractItemView.this.handleHeaderMouseDown(info.id, event);
            });
    main.mouseWentUp()
        .addListener(
            this,
            (WMouseEvent event) -> {
              WAbstractItemView.this.handleHeaderMouseUp(info.id, event);
            });
    main.doubleClicked()
        .addListener(
            this,
            (WMouseEvent event) -> {
              WAbstractItemView.this.handleHeaderDblClicked(info.id, event);
            });
    result.addWidget(main);
    if (this.isDisabled()) {
      result.addStyleClass("Wt-disabled");
      for (WWidget child : result.getChildren()) {
        child.addStyleClass("Wt-disabled");
      }
    }
    String sc = StringUtils.asString(index.getData(ItemDataRole.StyleClass)).toString();
    if (sc.length() != 0) {
      result.addStyleClass(sc);
    }
    return result;
  }
  //  WText  headerTextWidget(int column) ;
  /**
   * Handles a click event.
   *
   * <p>This processes the event for internal purposes (such as selection or editing) and emits the
   * {@link WAbstractItemView#clicked() clicked()} signal.
   *
   * <p>You may want to override this signal to override the built-in selection or editing
   * behaviour.
   */
  void handleClick(final WModelIndex index, final WMouseEvent event) {
    if (this.dragEnabled_ && (this.delayedClearAndSelectIndex_ != null)) {
      Coordinates delta = event.getDragDelta();
      if ((delta.x < 0 ? -delta.x : delta.x) < 4 && (delta.y < 0 ? -delta.y : delta.y) < 4) {
        this.select(this.delayedClearAndSelectIndex_, SelectionFlag.ClearAndSelect);
      }
    }
    boolean doEdit = (index != null) && this.getEditTriggers().contains(EditTrigger.SingleClicked);
    if (doEdit) {
      this.edit(index);
    }
    this.clicked_.trigger(index, event);
  }
  /**
   * Handles a double click event.
   *
   * <p>This processes the event for internal purposes (such as editing) and emits the {@link
   * WAbstractItemView#doubleClicked() doubleClicked()} signal.
   *
   * <p>You may want to override this signal to override the built-in editing behaviour.
   */
  void handleDoubleClick(final WModelIndex index, final WMouseEvent event) {
    boolean doEdit = (index != null) && this.getEditTriggers().contains(EditTrigger.DoubleClicked);
    if (doEdit) {
      this.edit(index);
    }
    this.doubleClicked_.trigger(index, event);
  }
  /**
   * Handles a mouse down event.
   *
   * <p>This emits the {@link WAbstractItemView#mouseWentDown() mouseWentDown()} signal.
   */
  void handleMouseDown(final WModelIndex index, final WMouseEvent event) {
    if (this.touchRegistered_) {
      return;
    }
    boolean doEdit =
        (index != null)
            && this.getEditTriggers().contains(EditTrigger.SelectedClicked)
            && this.isSelected(index);
    this.delayedClearAndSelectIndex_ = null;
    if ((index != null) && event.getButton() == MouseButton.Left) {
      this.selectionHandleClick(index, event.getModifiers());
    }
    if (doEdit) {
      this.edit(index);
    }
    this.mouseWentDown_.trigger(index, event);
    this.touchRegistered_ = false;
  }
  /**
   * Handles a mouse up event.
   *
   * <p>This emits the {@link WAbstractItemView#mouseWentUp() mouseWentUp()} signal.
   */
  void handleMouseUp(final WModelIndex index, final WMouseEvent event) {
    this.mouseWentUp_.trigger(index, event);
  }
  /** Handles a touch select event. */
  protected void handleTouchSelect(final List<WModelIndex> indices, final WTouchEvent event) {
    if (indices.isEmpty()) {
      return;
    }
    final WModelIndex index = indices.get(0);
    this.touchRegistered_ = true;
    this.delayedClearAndSelectIndex_ = null;
    if (indices.size() == 1) {
      boolean doEdit =
          (index != null)
              && this.getEditTriggers().contains(EditTrigger.SelectedClicked)
              && this.isSelected(index);
      if (doEdit) {
        this.edit(index);
      }
    }
    if ((indices.get(0) != null) && (indices.get(indices.size() - 1) != null)) {
      this.selectionHandleTouch(indices, event);
    }
    this.touchStart_.trigger(index, event);
  }
  /** Handles a touch started event. */
  protected void handleTouchStart(final List<WModelIndex> indices, final WTouchEvent event) {
    this.touchStarted_.trigger(indices, event);
  }
  /** Handles a touch moved event. */
  protected void handleTouchMove(final List<WModelIndex> indices, final WTouchEvent event) {
    this.touchMoved_.trigger(indices, event);
  }
  /** Handles a touch ended event. */
  protected void handleTouchEnd(final List<WModelIndex> indices, final WTouchEvent event) {
    this.touchEnded_.trigger(indices, event);
  }

  boolean internalSelect(final WModelIndex index, SelectionFlag option) {
    if (!!EnumUtils.mask(index.getFlags(), ItemFlag.Selectable).isEmpty()
        || this.getSelectionMode() == SelectionMode.None) {
      return false;
    }
    if (option == SelectionFlag.ToggleSelect) {
      option = this.isSelected(index) ? SelectionFlag.Deselect : SelectionFlag.Select;
    }
    if (this.getSelectionMode() == SelectionMode.Single && option == SelectionFlag.Select) {
      option = SelectionFlag.ClearAndSelect;
    }
    if ((option == SelectionFlag.ClearAndSelect || option == SelectionFlag.Select)
        && this.getSelectionModel().selection_.size() == 1
        && this.isSelected(index)) {
      return false;
    } else {
      if (option == SelectionFlag.Deselect && !this.isSelected(index)) {
        return false;
      }
    }
    if (option == SelectionFlag.ClearAndSelect) {
      this.clearSelection();
      option = SelectionFlag.Select;
    }
    if (option == SelectionFlag.Select) {
      this.getSelectionModel().selection_.add(index);
    } else {
      this.getSelectionModel().selection_.remove(index);
    }
    return true;
  }

  protected void enableAjax() {
    super.enableAjax();
    if (this.uDragWidget_ != null) {
      this.getHeaderContainer().addWidget(this.uDragWidget_);
      this.configureModelDragDrop();
    }
  }

  void setEditState(final WModelIndex index, final Object editState) {
    this.editedItems_.get(index).editState = editState;
  }

  Object getEditState(final WModelIndex index) {
    WAbstractItemView.Editor i = this.editedItems_.get(index);
    if (i != null) {
      return i.editState;
    } else {
      return null;
    }
  }

  boolean hasEditFocus(final WModelIndex index) {
    WAbstractItemView.Editor i = this.editedItems_.get(index);
    if (i != null) {
      final WAbstractItemView.Editor editor = i;
      return !(editor.widget != null) && !editor.stateSaved;
    } else {
      return false;
    }
  }

  void setEditorWidget(final WModelIndex index, WWidget editor) {
    this.editedItems_.get(index).widget = editor;
    this.editedItems_.get(index).stateSaved = !(editor != null);
  }

  void bindObjJS(final JSlot slot, final String jsMethod) {
    slot.setJavaScript(
        "function(obj, event) {" + this.getJsRef() + ".wtObj." + jsMethod + "(obj, event);}");
  }

  void connectObjJS(final AbstractEventSignal s, final String jsMethod) {
    s.addListener(
        "function(obj, event) {" + this.getJsRef() + ".wtObj." + jsMethod + "(obj, event);}");
  }

  protected boolean shiftEditorRows(
      final WModelIndex parent, int start, int count, boolean persistWhenShifted) {
    boolean result = false;
    if (!this.editedItems_.isEmpty()) {
      List<WModelIndex> toClose = new ArrayList<WModelIndex>();
      Map<WModelIndex, WAbstractItemView.Editor> newMap =
          new HashMap<WModelIndex, WAbstractItemView.Editor>();
      for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it =
              this.editedItems_.entrySet().iterator();
          i_it.hasNext(); ) {
        Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
        WModelIndex c = i.getKey();
        WModelIndex p = c.getParent();
        if (!(p == parent || (p != null && p.equals(parent)))
            && !WModelIndex.isAncestor(p, parent)) {
          newMap.put(c, i.getValue());
        } else {
          if ((p == parent || (p != null && p.equals(parent)))) {
            if (c.getRow() >= start) {
              if (c.getRow() < start - count) {
                toClose.add(c);
              } else {
                WModelIndex shifted = this.model_.getIndex(c.getRow() + count, c.getColumn(), p);
                newMap.put(shifted, i.getValue());
                if (i.getValue().widget != null) {
                  if (persistWhenShifted) {
                    this.persistEditor(shifted, i.getValue());
                  }
                  result = true;
                }
              }
            } else {
              newMap.put(c, i.getValue());
            }
          } else {
            if (count < 0) {
              do {
                if ((p.getParent() == parent
                        || (p.getParent() != null && p.getParent().equals(parent)))
                    && p.getRow() >= start
                    && p.getRow() < start - count) {
                  toClose.add(c);
                  break;
                } else {
                  p = p.getParent();
                }
              } while (!(p == parent || (p != null && p.equals(parent))));
            }
          }
        }
      }
      for (int i = 0; i < toClose.size(); ++i) {
        this.closeEditor(toClose.get(i));
      }
      this.editedItems_ = newMap;
    }
    return result;
  }

  protected boolean shiftEditorColumns(
      final WModelIndex parent, int start, int count, boolean persistWhenShifted) {
    boolean result = false;
    if (!this.editedItems_.isEmpty()) {
      List<WModelIndex> toClose = new ArrayList<WModelIndex>();
      Map<WModelIndex, WAbstractItemView.Editor> newMap =
          new HashMap<WModelIndex, WAbstractItemView.Editor>();
      for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it =
              this.editedItems_.entrySet().iterator();
          i_it.hasNext(); ) {
        Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
        WModelIndex c = i.getKey();
        WModelIndex p = c.getParent();
        if (!(p == parent || (p != null && p.equals(parent)))
            && !WModelIndex.isAncestor(p, parent)) {
          newMap.put(c, i.getValue());
        } else {
          if ((p == parent || (p != null && p.equals(parent)))) {
            if (c.getColumn() >= start) {
              if (c.getColumn() < start - count) {
                toClose.add(c);
              } else {
                WModelIndex shifted = this.model_.getIndex(c.getRow(), c.getColumn() + count, p);
                newMap.put(shifted, i.getValue());
                if (i.getValue().widget != null) {
                  if (persistWhenShifted) {
                    this.persistEditor(shifted, i.getValue());
                  }
                  result = true;
                }
              }
            } else {
              newMap.put(c, i.getValue());
            }
          } else {
            if (count < 0) {
              do {
                if ((p.getParent() == parent
                        || (p.getParent() != null && p.getParent().equals(parent)))
                    && p.getColumn() >= start
                    && p.getColumn() < start - count) {
                  toClose.add(c);
                  break;
                } else {
                  p = p.getParent();
                }
              } while (!(p == parent || (p != null && p.equals(parent))));
            }
          }
        }
      }
      for (int i = 0; i < toClose.size(); ++i) {
        this.closeEditor(toClose.get(i));
      }
      this.editedItems_ = newMap;
    }
    return result;
  }

  void persistEditor(final WModelIndex index) {
    WAbstractItemView.Editor i = this.editedItems_.get(index);
    if (i != null) {
      this.persistEditor(index, i);
    }
  }

  static class Editor {
    private static Logger logger = LoggerFactory.getLogger(Editor.class);

    public Editor() {
      this.widget = (WWidget) null;
      this.editState = new Object();
      this.stateSaved = false;
      this.valid = false;
      this.editState = null;
    }

    public WWidget widget;
    public Object editState;
    public boolean stateSaved;
    public boolean valid;
  }

  private WAbstractItemModel model_;
  private WAbstractItemModel headerModel_;
  private WModelIndex rootIndex_;
  private WAbstractItemDelegate itemDelegate_;
  private WAbstractItemDelegate headerItemDelegate_;
  private WItemSelectionModel selectionModel_;
  private WLength rowHeight_;
  private WLength headerLineHeight_;
  private SelectionMode selectionMode_;
  private boolean sorting_;
  private boolean columnResize_;
  private AlignmentFlag defaultHeaderVAlignment_;
  private boolean defaultHeaderWordWrap_;
  private int rowHeaderCount_;
  private WString computedDragMimeType_;
  private boolean sortEnabled_;
  private WModelIndex delayedClearAndSelectIndex_;
  private JSignal2<Integer, Integer> columnWidthChanged_;
  private Signal2<Integer, WLength> columnResized_;
  private WCssTemplateRule headerHeightRule_;
  private int nextColumnId_;
  private boolean alternatingRowColors_;
  private JSlot resizeHandleMDownJS_;
  private Map<WModelIndex, WAbstractItemView.Editor> editedItems_;
  private Signal2<Integer, WMouseEvent> headerClicked_;
  private Signal2<Integer, WMouseEvent> headerDblClicked_;
  private Signal2<Integer, WMouseEvent> headerMouseWentDown_;
  private Signal2<Integer, WMouseEvent> headerMouseWentUp_;
  private Signal2<WModelIndex, WMouseEvent> clicked_;
  private Signal2<WModelIndex, WMouseEvent> doubleClicked_;
  private Signal2<WModelIndex, WMouseEvent> mouseWentDown_;
  private Signal2<WModelIndex, WMouseEvent> mouseWentUp_;
  private Signal2<WModelIndex, WTouchEvent> touchStart_;
  private Signal2<List<WModelIndex>, WTouchEvent> touchStarted_;
  private Signal2<List<WModelIndex>, WTouchEvent> touchMoved_;
  private Signal2<List<WModelIndex>, WTouchEvent> touchEnded_;
  private Signal selectionChanged_;
  private Signal pageChanged_;
  private EnumSet<EditTrigger> editTriggers_;
  private EnumSet<EditOption> editOptions_;
  private boolean touchRegistered_;

  private void closeEditorWidget(WWidget editor, boolean saveData) {
    for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it =
            this.editedItems_.entrySet().iterator();
        i_it.hasNext(); ) {
      Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
      if (i.getValue().widget == editor) {
        if (this.editOptions_.contains(EditOption.LeaveEditorsOpen)) {
          if (saveData) {
            this.saveEditedValue(i.getKey(), i.getValue());
          }
        } else {
          this.closeEditor(i.getKey(), saveData);
        }
        return;
      }
    }
  }

  private void saveEditedValue(final WModelIndex index, final WAbstractItemView.Editor editor) {
    Object editState = new Object();
    WAbstractItemDelegate delegate = this.getItemDelegate(index);
    if (editor.widget != null) {
      editState = delegate.getEditState(editor.widget, index);
    } else {
      editState = editor.editState;
    }
    delegate.setModelData(editState, this.model_, index);
  }

  private void persistEditor(final WModelIndex index, final WAbstractItemView.Editor editor) {
    if (editor.widget != null) {
      editor.editState = this.getItemDelegate(index).getEditState(editor.widget, index);
      editor.stateSaved = true;
      editor.widget = (WWidget) null;
    }
  }

  abstract WWidget headerWidget(int column, boolean contentsOnly);

  final WWidget headerWidget(int column) {
    return headerWidget(column, true);
  }

  private WText headerSortIconWidget(int column) {
    if (!this.columnInfo(column).sorting) {
      return null;
    }
    WWidget hw = this.headerWidget(column);
    if (hw != null) {
      return ObjectUtils.cast(hw.find("sort"), WText.class);
    } else {
      return null;
    }
  }

  private void selectionHandleClick(final WModelIndex index, EnumSet<KeyboardModifier> modifiers) {
    if (this.selectionMode_ == SelectionMode.None) {
      return;
    }
    if (this.selectionMode_ == SelectionMode.Extended) {
      if (modifiers.contains(KeyboardModifier.Shift)) {
        this.extendSelection(index);
      } else {
        if (!!EnumUtils.mask(modifiers, EnumSet.of(KeyboardModifier.Control, KeyboardModifier.Meta))
            .isEmpty()) {
          if (!this.dragEnabled_) {
            this.select(index, SelectionFlag.ClearAndSelect);
          } else {
            if (!this.isSelected(index)) {
              this.select(index, SelectionFlag.ClearAndSelect);
            } else {
              this.delayedClearAndSelectIndex_ = index;
            }
          }
        } else {
          this.select(index, SelectionFlag.ToggleSelect);
        }
      }
    } else {
      if (!EnumUtils.mask(modifiers, EnumSet.of(KeyboardModifier.Control, KeyboardModifier.Meta))
              .isEmpty()
          && this.isSelected(index)) {
        this.clearSelection();
        this.selectionChanged_.trigger();
      } else {
        this.select(index, SelectionFlag.Select);
      }
    }
  }

  private final void selectionHandleClick(
      final WModelIndex index, KeyboardModifier modifier, KeyboardModifier... modifiers) {
    selectionHandleClick(index, EnumSet.of(modifier, modifiers));
  }

  private void selectionHandleTouch(final List<WModelIndex> indices, final WTouchEvent event) {
    if (this.selectionMode_ == SelectionMode.None) {
      return;
    }
    final WModelIndex index = indices.get(0);
    if (this.selectionMode_ == SelectionMode.Extended) {
      if (event.getTouches().size() > 1) {
        this.extendSelection(indices);
      } else {
        this.select(index, SelectionFlag.ToggleSelect);
      }
    } else {
      if (this.isSelected(index)) {
        this.clearSelection();
        this.selectionChanged_.trigger();
      } else {
        this.select(index, SelectionFlag.ClearAndSelect);
      }
    }
  }

  private void extendSelection(final WModelIndex index) {
    if (this.selectionModel_.selection_.isEmpty()) {
      this.internalSelect(index, SelectionFlag.Select);
    } else {
      if (this.getSelectionBehavior() == SelectionBehavior.Rows && index.getColumn() != 0) {
        this.extendSelection(this.model_.getIndex(index.getRow(), 0, index.getParent()));
        return;
      }
    }
    WModelIndex top = this.selectionModel_.selection_.iterator().next();
    if (top.compareTo(index) < 0) {
      this.clearSelection();
      this.selectRange(top, index);
    } else {
      WModelIndex bottom = this.selectionModel_.selection_.last();
      this.clearSelection();
      this.selectRange(index, bottom);
    }
    this.selectionChanged_.trigger();
  }

  private void extendSelection(final List<WModelIndex> indices) {
    final WModelIndex firstIndex = indices.get(0);
    final WModelIndex secondIndex = indices.get(indices.size() - 1);
    if (indices.size() > 1) {
      if (firstIndex.getRow() > secondIndex.getRow()) {
        this.selectRange(secondIndex, firstIndex);
      } else {
        this.selectRange(firstIndex, secondIndex);
      }
    }
    this.selectionChanged_.trigger();
  }

  abstract void selectRange(final WModelIndex first, final WModelIndex last);

  private void checkDragSelection() {
    this.computedDragMimeType_ = new WString(this.selectionModel_.getMimeType());
    this.setAttributeValue("dmt", this.computedDragMimeType_.toString());
    if (!(this.computedDragMimeType_.length() == 0)) {
      this.setAttributeValue("drag", "true");
    } else {
      this.setAttributeValue("drag", "false");
    }
  }

  void configureModelDragDrop() {
    if (!(this.model_ != null)) {
      return;
    }
    if (this.dragEnabled_) {
      this.setAttributeValue("dsid", WApplication.getInstance().encodeObject(this.selectionModel_));
      this.checkDragSelection();
    }
    List<String> acceptMimeTypes = this.model_.getAcceptDropMimeTypes();
    for (int i = 0; i < acceptMimeTypes.size(); ++i) {
      if (!this.enabledDropLocations_.isEmpty()) {
        this.acceptDrops(acceptMimeTypes.get(i), "Wt-drop-site");
      } else {
        this.stopAcceptDrops(acceptMimeTypes.get(i));
      }
    }
  }

  private void handleHeaderMouseDown(int columnid, WMouseEvent event) {
    this.headerMouseWentDown_.trigger(this.columnById(columnid), event);
  }

  private void handleHeaderMouseUp(int columnid, WMouseEvent event) {
    this.headerMouseWentUp_.trigger(this.columnById(columnid), event);
  }

  private void handleHeaderClicked(int columnid, WMouseEvent event) {
    int column = this.columnById(columnid);
    final WAbstractItemView.ColumnInfo info = this.columnInfo(column);
    if (this.sortEnabled_ && info.sorting) {
      this.toggleSortColumn(columnid);
    }
    this.headerClicked_.trigger(column, event);
  }

  private void handleHeaderDblClicked(int columnid, WMouseEvent event) {
    this.headerDblClicked_.trigger(this.columnById(columnid), event);
  }

  void toggleSortColumn(int columnid) {
    int column = this.columnById(columnid);
    if (column != this.currentSortColumn_) {
      this.sortByColumn(column, this.columnInfo(column).sortOrder);
    } else {
      this.sortByColumn(
          column,
          this.columnInfo(column).sortOrder == SortOrder.Ascending
              ? SortOrder.Descending
              : SortOrder.Ascending);
    }
  }

  private void updateColumnWidth(int columnId, int width) {
    int column = this.columnById(columnId);
    if (column >= 0) {
      this.columnInfo(column).width = new WLength(width);
      this.columnResized_.trigger(column, this.columnInfo(column).width);
      WWidget w = this.headerWidget(column, 0 != 0);
      if (w != null) {
        w.scheduleRender(EnumSet.of(RepaintFlag.SizeAffected));
      }
    }
  }

  abstract WContainerWidget getHeaderContainer();

  private int headerLevel(int column) {
    Object d = this.model_.getHeaderData(column, Orientation.Horizontal, ItemDataRole.Level);
    if ((d != null)) {
      return (int) StringUtils.asNumber(d);
    } else {
      return 0;
    }
  }

  private int getHeaderLevelCount() {
    int result = 0;
    if (this.model_ != null) {
      for (int i = 0; i < this.columns_.size(); ++i) {
        if (!this.columns_.get(i).hidden) {
          result = Math.max(result, this.headerLevel(i));
        }
      }
    }
    return result + 1;
  }

  private void expandColumn(int columnid) {
    this.model_.expandColumn(this.columnById(columnid));
    this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
    this.setHeaderHeight(this.headerLineHeight_);
  }

  private void collapseColumn(int columnid) {
    this.model_.collapseColumn(this.columnById(columnid));
    this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
    this.setHeaderHeight(this.headerLineHeight_);
  }

  private void initDragDrop() {
    this.addCssRule(
        "#" + this.getId() + "dw",
        "width: 32px; height: 32px;background: url("
            + WApplication.getResourcesUrl()
            + "items-not-ok.gif);");
    this.addCssRule(
        "#" + this.getId() + "dw.Wt-valid-drop",
        "width: 32px; height: 32px;background: url("
            + WApplication.getResourcesUrl()
            + "items-ok.gif);");
    this.selectionChanged_.addListener(
        this,
        () -> {
          WAbstractItemView.this.checkDragSelection();
        });
  }

  static String repeat(final String s, int times) {
    String result = "";
    for (int i = 0; i < times; ++i) {
      result += s;
    }
    return result;
  }
}
