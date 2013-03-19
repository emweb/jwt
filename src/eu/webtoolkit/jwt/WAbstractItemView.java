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
 * An abstract base class for item Views.
 * <p>
 * 
 * See {@link WTableView} or {@link WTreeView} for a description.
 * <p>
 * <h3>i18n</h3>
 * <p>
 * The strings used in this class can be translated by overriding the default
 * values for the following localization keys:
 * <ul>
 * <li>Wt.WAbstractItemView.PageIOfN: <b>{1}</b> of <b>{2}</b></li>
 * <li>Wt.WAbstractItemView.PageBar.First: &amp;#xc2ab; First</li>
 * <li>Wt.WAbstractItemView.PageBar.Previous: &amp;#xe280b9; Previous</li>
 * <li>Wt.WAbstractItemView.PageBar.Next: Next &amp;#xe280ba;</li>
 * <li>Wt.WAbstractItemView.PageBar.Last: Last &amp;#xc2bb;</li>
 * </ul>
 */
public abstract class WAbstractItemView extends WCompositeWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WAbstractItemView.class);

	/**
	 * Enumeration that specifies the user action that triggers editing.
	 * <p>
	 * 
	 * @see WAbstractItemView#setEditTriggers(EnumSet editTriggers)
	 * @see WAbstractItemView#edit(WModelIndex index)
	 */
	public enum EditTrigger {
		/**
		 * Do not allow user to initiate editing.
		 */
		NoEditTrigger,
		/**
		 * Edit an item when clicked.
		 */
		SingleClicked,
		/**
		 * Edit an item when double clicked.
		 */
		DoubleClicked,
		/**
		 * Edit a selected item that is clicked again.
		 */
		SelectedClicked;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Enumeration that specifies editing options.
	 * <p>
	 * 
	 * @see WAbstractItemView#setEditOptions(EnumSet editOptions)
	 */
	public enum EditOption {
		/**
		 * Never show more than one active editor.
		 */
		SingleEditor,
		/**
		 * Allow multiple editors at the same time.
		 */
		MultipleEditors,
		/**
		 * Always save the current edit value when closing.
		 */
		SaveWhenClosed,
		/**
		 * Editors can only be closed using
		 * {@link WAbstractItemView#closeEditor(WModelIndex index, boolean saveData)
		 * closeEditor()}.
		 */
		LeaveEditorsOpen;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Enumeration that specifies a scrolling option.
	 * <p>
	 * 
	 * @see WAbstractItemView#scrollTo(WModelIndex index,
	 *      WAbstractItemView.ScrollHint hint)
	 */
	public enum ScrollHint {
		/**
		 * Scrolls minimally to make it visible.
		 */
		EnsureVisible,
		/**
		 * Positions the item at the top of the viewport.
		 */
		PositionAtTop,
		/**
		 * Positions the item at the bottom of the viewport.
		 */
		PositionAtBottom,
		/**
		 * Positions the item at the center of the viewport.
		 */
		PositionAtCenter;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	public void remove() {
		if (this.headerHeightRule_ != null)
			this.headerHeightRule_.remove();
		for (int i = 0; i < this.columns_.size(); ++i) {
			if (this.columns_.get(i).styleRule != null)
				this.columns_.get(i).styleRule.remove();
		}
		super.remove();
	}

	/**
	 * Sets the model.
	 * <p>
	 * The View will display data of the given <code>model</code> and changes in
	 * the model are reflected by the View.
	 * <p>
	 * The initial model is <code>null</code>.
	 * <p>
	 * 
	 * @see WAbstractItemView#setRootIndex(WModelIndex rootIndex)
	 */
	public void setModel(WAbstractItemModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
		;
		this.headerModel_ = new HeaderProxyModel(this.model_, this);
		WItemSelectionModel oldSelectionModel = this.selectionModel_;
		this.selectionModel_ = new WItemSelectionModel(model, this);
		this.selectionModel_.setSelectionBehavior(oldSelectionModel
				.getSelectionBehavior());
		;
		this.editedItems_.clear();
		this.configureModelDragDrop();
		this.setRootIndex(null);
		this.setHeaderHeight(this.headerLineHeight_);
	}

	/**
	 * Returns the model.
	 * <p>
	 * 
	 * @see WAbstractItemView#setModel(WAbstractItemModel model)
	 */
	public WAbstractItemModel getModel() {
		return this.model_;
	}

	/**
	 * Sets the root index.
	 * <p>
	 * The root index is the model index that is considered the root node. This
	 * node itself is not rendered, but its children are.
	 * <p>
	 * The default value is <code>null</code>, corresponding to the model&apos;s
	 * root node.
	 * <p>
	 * 
	 * @see WAbstractItemView#setModel(WAbstractItemModel model)
	 */
	public void setRootIndex(WModelIndex rootIndex) {
		this.rootIndex_ = rootIndex;
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerender);
		int modelColumnCount = this.model_.getColumnCount(this.rootIndex_);
		while (this.columns_.size() > modelColumnCount) {
			int i = this.columns_.size() - 1;
			if (this.columns_.get(i).styleRule != null)
				this.columns_.get(i).styleRule.remove();
			this.columns_.remove(0 + i);
		}
		while (this.columns_.size() < modelColumnCount) {
			this.columns_.add(this.createColumnInfo(this.columns_.size()));
		}
	}

	/**
	 * Returns the root index.
	 * <p>
	 * 
	 * @see WAbstractItemView#setRootIndex(WModelIndex rootIndex)
	 */
	public WModelIndex getRootIndex() {
		return this.rootIndex_;
	}

	/**
	 * Sets the default item delegate.
	 * <p>
	 * The previous delegate is not deleted. This item delegate is for all
	 * columns for which no specific item delegate is set.
	 * <p>
	 * The default item delegate is a {@link WItemDelegate}.
	 * <p>
	 * 
	 * @see WAbstractItemView#setItemDelegateForColumn(int column,
	 *      WAbstractItemDelegate delegate)
	 */
	public void setItemDelegate(WAbstractItemDelegate delegate) {
		this.itemDelegate_ = delegate;
		this.itemDelegate_.closeEditor().addListener(this,
				new Signal2.Listener<WWidget, Boolean>() {
					public void trigger(WWidget e1, Boolean e2) {
						WAbstractItemView.this.closeEditorWidget(e1, e2);
					}
				});
	}

	/**
	 * Returns the default item delegate.
	 * <p>
	 * 
	 * @see WAbstractItemView#setItemDelegate(WAbstractItemDelegate delegate)
	 */
	public WAbstractItemDelegate getItemDelegate() {
		return this.itemDelegate_;
	}

	/**
	 * Sets the delegate for a column.
	 * <p>
	 * A delegate previously set (if any) is not deleted.
	 * <p>
	 * 
	 * @see WAbstractItemView#setItemDelegate(WAbstractItemDelegate delegate)
	 */
	public void setItemDelegateForColumn(int column,
			WAbstractItemDelegate delegate) {
		this.columnInfo(column).itemDelegate_ = delegate;
		delegate.closeEditor().addListener(this,
				new Signal2.Listener<WWidget, Boolean>() {
					public void trigger(WWidget e1, Boolean e2) {
						WAbstractItemView.this.closeEditorWidget(e1, e2);
					}
				});
	}

	/**
	 * Returns the delegate that was set for a column.
	 * <p>
	 * Returns <code>null</code> if no delegate was set for the column.
	 * <p>
	 * 
	 * @see WAbstractItemView#setItemDelegateForColumn(int column,
	 *      WAbstractItemDelegate delegate)
	 */
	public WAbstractItemDelegate getItemDelegateForColumn(int column) {
		return this.columnInfo(column).itemDelegate_;
	}

	/**
	 * Returns the delegate for rendering an item.
	 * <p>
	 * 
	 * @see WAbstractItemView#setItemDelegateForColumn(int column,
	 *      WAbstractItemDelegate delegate)
	 * @see WAbstractItemView#setItemDelegate(WAbstractItemDelegate delegate)
	 */
	public WAbstractItemDelegate getItemDelegate(WModelIndex index) {
		return this.getItemDelegate(index.getColumn());
	}

	/**
	 * Returns the delegate for a column.
	 * <p>
	 * Returns either the delegate that was set for the column, or the default
	 * delegate.
	 */
	public WAbstractItemDelegate getItemDelegate(int column) {
		WAbstractItemDelegate result = this.getItemDelegateForColumn(column);
		return result != null ? result : this.itemDelegate_;
	}

	/**
	 * Returns the widget that renders an item.
	 * <p>
	 * This returns the widget that renders the given item. This may return 0 if
	 * the item is currently not rendered.
	 * <p>
	 * This widget has been created by an item delegate, and usually an item
	 * delegate is involved when updating it.
	 */
	public abstract WWidget itemWidget(WModelIndex index);

	/**
	 * Sets the header item delegate.
	 * <p>
	 * This item delegate is used for rendering items in the header.
	 * <p>
	 * The previous delegate is not deleted. This item delegate is for all
	 * columns for which no specific item delegate is set.
	 * <p>
	 * The default item delegate is a {@link WItemDelegate}.
	 */
	public void setHeaderItemDelegate(WAbstractItemDelegate delegate) {
		this.headerItemDelegate_ = delegate;
	}

	/**
	 * Returns the header item delegate.
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderItemDelegate(WAbstractItemDelegate
	 *      delegate)
	 */
	public WAbstractItemDelegate getHeaderItemDelegate() {
		return this.headerItemDelegate_;
	}

	/**
	 * Sets the content alignment for a column.
	 * <p>
	 * The default value is {@link AlignmentFlag#AlignLeft}.
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
	 */
	public void setColumnAlignment(int column, AlignmentFlag alignment) {
		this.columnInfo(column).alignment = alignment;
		WApplication app = WApplication.getInstance();
		String align = null;
		switch (alignment) {
		case AlignLeft:
			align = app.getLayoutDirection() == LayoutDirection.LeftToRight ? "left"
					: "right";
			break;
		case AlignCenter:
			align = "center";
			break;
		case AlignRight:
			align = app.getLayoutDirection() == LayoutDirection.LeftToRight ? "right"
					: "left";
			break;
		case AlignJustify:
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
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnAlignment(int column, AlignmentFlag
	 *      alignment)
	 */
	public AlignmentFlag getColumnAlignment(int column) {
		return this.columnInfo(column).alignment;
	}

	/**
	 * Sets the header alignment for a column.
	 * <p>
	 * The default alignemnt is horizontally left, and vertically centered. (
	 * {@link AlignmentFlag#AlignLeft} | {@link AlignmentFlag#AlignMiddle}).
	 * <p>
	 * Valid options for horizontal alignment are
	 * {@link AlignmentFlag#AlignLeft}, {@link AlignmentFlag#AlignCenter} or
	 * {@link AlignmentFlag#AlignRight}.
	 * <p>
	 * Valid options for vertical alignment are
	 * {@link AlignmentFlag#AlignMiddle} or {@link AlignmentFlag#AlignTop}. In
	 * the latter case, other contents may be added below the label in
	 * {@link WAbstractItemView#createExtraHeaderWidget(int column)
	 * createExtraHeaderWidget()}.
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnAlignment(int column, AlignmentFlag
	 *      alignment)
	 */
	public void setHeaderAlignment(int column, EnumSet<AlignmentFlag> alignment) {
		this.columnInfo(column).headerHAlignment = EnumUtils
				.enumFromSet(EnumUtils.mask(alignment,
						AlignmentFlag.AlignHorizontalMask));
		if (!EnumUtils.mask(alignment, AlignmentFlag.AlignVerticalMask)
				.isEmpty()) {
			this.columnInfo(column).headerVAlignment = EnumUtils
					.enumFromSet(EnumUtils.mask(alignment,
							AlignmentFlag.AlignVerticalMask));
		}
		if (this.columnInfo(column).hidden
				|| this.renderState_.getValue() >= WAbstractItemView.RenderState.NeedRerenderHeader
						.getValue()) {
			return;
		}
		WContainerWidget wc = ((this.headerWidget(column)) instanceof WContainerWidget ? (WContainerWidget) (this
				.headerWidget(column))
				: null);
		wc.setContentAlignment(alignment);
		if (this.columnInfo(column).headerVAlignment == AlignmentFlag.AlignMiddle) {
			wc.setLineHeight(this.headerLineHeight_);
		} else {
			wc.setLineHeight(WLength.Auto);
		}
	}

	/**
	 * Sets the header alignment for a column.
	 * <p>
	 * Calls {@link #setHeaderAlignment(int column, EnumSet alignment)
	 * setHeaderAlignment(column, EnumSet.of(alignmen, alignment))}
	 */
	public final void setHeaderAlignment(int column, AlignmentFlag alignmen,
			AlignmentFlag... alignment) {
		setHeaderAlignment(column, EnumSet.of(alignmen, alignment));
	}

	/**
	 * Returns the horizontal header alignment for a column (<b>deprecated</b>).
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
	 * @deprecated use
	 *             {@link WAbstractItemView#horizontalHeaderAlignment(int column)
	 *             horizontalHeaderAlignment()} instead.
	 */
	public AlignmentFlag getHeaderAlignment(int column) {
		return this.horizontalHeaderAlignment(column);
	}

	/**
	 * Returns the horizontal header alignment for a column.
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
	 */
	public AlignmentFlag horizontalHeaderAlignment(int column) {
		return this.columnInfo(column).headerHAlignment;
	}

	/**
	 * Returns the vertical header alignment for a column.
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
	 */
	public AlignmentFlag verticalHeaderAlignment(int column) {
		return this.columnInfo(column).headerVAlignment;
	}

	/**
	 * Configures header text wrapping.
	 * <p>
	 * This setting only affects a multiline header, and the default value is
	 * <code>true</code>. When set to <code>false</code>, the header itself will
	 * not wrap (as with a vertically centered header), and thus extra widgets
	 * will not shift down when there is a long header label.
	 */
	public void setHeaderWordWrap(int column, boolean enabled) {
		this.columnInfo(column).headerWordWrap = enabled;
		if (this.columnInfo(column).hidden
				|| this.renderState_.getValue() >= WAbstractItemView.RenderState.NeedRerenderHeader
						.getValue()) {
			return;
		}
		if (this.columnInfo(column).headerVAlignment == AlignmentFlag.AlignTop) {
			WContainerWidget wc = ((this.headerWidget(column)) instanceof WContainerWidget ? (WContainerWidget) (this
					.headerWidget(column))
					: null);
			wc.toggleStyleClass("Wt-wwrap", enabled);
		}
	}

	// public boolean isHeaderWordWrap(int column) ;
	/**
	 * Sets if alternating row colors are to be used.
	 * <p>
	 * Configure whether rows get alternating background colors, defined by the
	 * current CSS theme.
	 * <p>
	 * The default value is <code>false</code>.
	 */
	public void setAlternatingRowColors(boolean enable) {
		this.alternatingRowColors_ = enable;
	}

	/**
	 * Returns whether alternating row colors are used.
	 * <p>
	 * When enabled, rows are displayed in alternating row colors, according to
	 * the current theme&apos;s definition.
	 * <p>
	 * 
	 * @see WAbstractItemView#setAlternatingRowColors(boolean enable)
	 */
	public boolean hasAlternatingRowColors() {
		return this.alternatingRowColors_;
	}

	/**
	 * Sorts the data according to a column.
	 * <p>
	 * Sorts the data according to data in column <code>column</code> and sort
	 * order <code>order</code>.
	 * <p>
	 * The default sorting column is -1: the model is unsorted.
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
				t
						.setStyleClass(order == SortOrder.AscendingOrder ? "Wt-tv-sh Wt-tv-sh-up"
								: "Wt-tv-sh Wt-tv-sh-down");
			}
		}
		this.model_.sort(column, order);
	}

	/**
	 * Returns the current sorting columm.
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
	 * <p>
	 * 
	 * @see WAbstractItemView#sortByColumn(int column, SortOrder order)
	 * @see WAbstractItemView#getSortColumn()
	 */
	public SortOrder getSortOrder() {
		if (this.currentSortColumn_ >= 0
				&& this.currentSortColumn_ < (int) this.columns_.size()) {
			return this.columns_.get(this.currentSortColumn_).sortOrder;
		} else {
			return SortOrder.AscendingOrder;
		}
	}

	/**
	 * Enables or disables sorting for all columns.
	 * <p>
	 * Enable or disable sorting by the user on all columns.
	 * <p>
	 * Sorting is enabled by default.
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
	 * <p>
	 * Enable or disable sorting by the user for a specific column.
	 * <p>
	 * Sorting is enabled by default.
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
	 * <p>
	 * 
	 * @see WAbstractItemView#setSortingEnabled(boolean enabled)
	 */
	public boolean isSortingEnabled() {
		return this.sorting_;
	}

	/**
	 * Returns whether sorting is enabled for a single column.
	 * <p>
	 * 
	 * @see WAbstractItemView#setSortingEnabled(boolean enabled)
	 */
	public boolean isSortingEnabled(int column) {
		return this.columnInfo(column).sorting;
	}

	/**
	 * Enables interactive column resizing.
	 * <p>
	 * Enable or disable column resize handles for interactive resizing of the
	 * columns.
	 * <p>
	 * Column resizing is enabled by default when JavaScript is available.
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnResizeEnabled(boolean enabled)
	 */
	public void setColumnResizeEnabled(boolean enabled) {
		if (enabled != this.columnResize_) {
			this.columnResize_ = enabled;
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
		}
	}

	/**
	 * Returns whether column resizing is enabled.
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnResizeEnabled(boolean enabled)
	 */
	public boolean isColumnResizeEnabled() {
		return this.columnResize_;
	}

	/**
	 * Changes the selection behaviour.
	 * <p>
	 * The selection behavior indicates whether whole rows or individual items
	 * can be selected. It is a property of the
	 * {@link WAbstractItemView#getSelectionModel() getSelectionModel()}.
	 * <p>
	 * By default, selection operates on rows (
	 * {@link SelectionBehavior#SelectRows SelectRows}), in which case model
	 * indexes will always be in the first column (column <code>null</code>).
	 * <p>
	 * Alternatively, you can allow selection for individual items (
	 * {@link SelectionBehavior#SelectItems SelectItems}).
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
	 * <p>
	 * 
	 * @see WAbstractItemView#setSelectionBehavior(SelectionBehavior behavior)
	 */
	public SelectionBehavior getSelectionBehavior() {
		return this.selectionModel_.getSelectionBehavior();
	}

	/**
	 * Sets the selection mode.
	 * <p>
	 * By default selection is disabled ({@link SelectionMode#NoSelection
	 * NoSelection}).
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
	 * <p>
	 * 
	 * @see WAbstractItemView#setSelectionMode(SelectionMode mode)
	 */
	public SelectionMode getSelectionMode() {
		return this.selectionMode_;
	}

	/**
	 * Returns the selection model.
	 * <p>
	 * The selection model keeps track of the currently selected items.
	 */
	public WItemSelectionModel getSelectionModel() {
		return this.selectionModel_;
	}

	/**
	 * Sets the selected items.
	 * <p>
	 * Replaces the current selection with <code>indexes</code>.
	 * <p>
	 * When selection operates on rows ({@link SelectionBehavior#SelectRows
	 * SelectRows}), it is sufficient to pass the first element in a row (column
	 * <code>null</code> ) to select the entire row.
	 * <p>
	 * 
	 * @see WAbstractItemView#select(WModelIndex index, SelectionFlag option)
	 * @see WAbstractItemView#getSelectionModel()
	 */
	public void setSelectedIndexes(SortedSet<WModelIndex> indexes) {
		if (indexes.isEmpty() && this.selectionModel_.selection_.isEmpty()) {
			return;
		}
		this.clearSelection();
		for (Iterator<WModelIndex> i_it = indexes.iterator(); i_it.hasNext();) {
			WModelIndex i = i_it.next();
			this.internalSelect(i, SelectionFlag.Select);
		}
		this.selectionChanged_.trigger();
	}

	/**
	 * Selects a single item.
	 * <p>
	 * 
	 * @see WAbstractItemView#setSelectedIndexes(SortedSet indexes)
	 * @see WAbstractItemView#getSelectionModel()
	 */
	public void select(WModelIndex index, SelectionFlag option) {
		if (this.internalSelect(index, option)) {
			this.selectionChanged_.trigger();
		}
	}

	/**
	 * Selects a single item.
	 * <p>
	 * Calls {@link #select(WModelIndex index, SelectionFlag option)
	 * select(index, SelectionFlag.Select)}
	 */
	public final void select(WModelIndex index) {
		select(index, SelectionFlag.Select);
	}

	/**
	 * Returns wheter an item is selected.
	 * <p>
	 * When selection operates on rows ({@link SelectionBehavior#SelectRows
	 * SelectRows}), this method returns true for each element in a selected
	 * row.
	 * <p>
	 * This is a convenience method for:
	 * 
	 * <pre>
	 * {@code
	 *    selectionModel().isSelected(index)
	 *   }
	 * </pre>
	 * <p>
	 * 
	 * @see WAbstractItemView#getSelectedIndexes()
	 * @see WAbstractItemView#select(WModelIndex index, SelectionFlag option)
	 * @see WAbstractItemView#getSelectionModel()
	 */
	public boolean isSelected(WModelIndex index) {
		return this.selectionModel_.isSelected(index);
	}

	/**
	 * Returns the set of selected items.
	 * <p>
	 * The model indexes are returned as a set, topologically ordered (in the
	 * order they appear in the view).
	 * <p>
	 * When selection operates on rows ({@link SelectionBehavior#SelectRows
	 * SelectRows}), this method only returns the model index of first
	 * column&apos;s element of the selected rows.
	 * <p>
	 * This is a convenience method for:
	 * 
	 * <pre>
	 * {@code
	 *    selectionModel().selectedIndexes()
	 *   }
	 * </pre>
	 * <p>
	 * 
	 * @see WAbstractItemView#setSelectedIndexes(SortedSet indexes)
	 */
	public SortedSet<WModelIndex> getSelectedIndexes() {
		return this.selectionModel_.selection_;
	}

	/**
	 * Enables the selection to be dragged (drag &amp; drop).
	 * <p>
	 * To enable dragging of the selection, you first need to enable selection
	 * using {@link WAbstractItemView#setSelectionMode(SelectionMode mode)
	 * setSelectionMode()}.
	 * <p>
	 * Whether an individual item may be dragged is controlled by the
	 * item&apos;s {@link ItemFlag#ItemIsDragEnabled ItemIsDragEnabled} flag.
	 * The selection can be dragged only if all items currently selected can be
	 * dragged.
	 * <p>
	 * 
	 * @see WAbstractItemView#setDropsEnabled(boolean enable)
	 */
	public void setDragEnabled(boolean enable) {
		if (this.dragEnabled_ != enable) {
			this.dragEnabled_ = enable;
			if (enable) {
				this.dragWidget_ = new WText(this.getHeaderContainer());
				this.dragWidget_.setId(this.getId() + "dw");
				this.dragWidget_.setInline(false);
				this.dragWidget_.hide();
				this.setAttributeValue("dwid", this.dragWidget_.getId());
				this.configureModelDragDrop();
			}
		}
	}

	/**
	 * Enables drop operations (drag &amp; drop).
	 * <p>
	 * When drop is enabled, the tree view will indicate that something may be
	 * dropped when the mime-type of the dragged object is compatible with one
	 * of the model&apos;s accepted drop mime-types (see
	 * {@link WAbstractItemModel#getAcceptDropMimeTypes()
	 * WAbstractItemModel#getAcceptDropMimeTypes()}) or this widget&apos;s
	 * accepted drop mime-types (see
	 * {@link WWidget#acceptDrops(String mimeType, String hoverStyleClass)
	 * WWidget#acceptDrops()}), and the target item has drop enabled (which is
	 * controlled by the item&apos;s {@link ItemFlag#ItemIsDropEnabled
	 * ItemIsDropEnabled} flag).
	 * <p>
	 * Drop events must be handled in
	 * {@link WAbstractItemView#dropEvent(WDropEvent e, WModelIndex index)
	 * dropEvent()}.
	 * <p>
	 * 
	 * @see WAbstractItemView#setDragEnabled(boolean enable)
	 * @see WAbstractItemView#dropEvent(WDropEvent e, WModelIndex index)
	 */
	public void setDropsEnabled(boolean enable) {
		if (this.dropsEnabled_ != enable) {
			this.dropsEnabled_ = enable;
			this.configureModelDragDrop();
		}
	}

	/**
	 * Sets the row height.
	 * <p>
	 * The view renders all rows with a same height. This method configures this
	 * row height.
	 * <p>
	 * The default value is 20 pixels.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The height must be specified in
	 * {@link WLength.Unit#Pixel} units.</i>
	 * </p>
	 * 
	 * @see WAbstractItemView#setColumnWidth(int column, WLength width)
	 */
	public void setRowHeight(WLength rowHeight) {
		this.rowHeight_ = rowHeight;
	}

	/**
	 * Returns the row height.
	 */
	public WLength getRowHeight() {
		return this.rowHeight_;
	}

	/**
	 * Sets the column width.
	 * <p>
	 * The default column width is 150 pixels.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The actual space occupied by each column is the column
	 * width augmented by 7 pixels for internal padding and a border. </i>
	 * </p>
	 */
	public abstract void setColumnWidth(int column, WLength width);

	/**
	 * Returns the column width.
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnWidth(int column, WLength width)
	 */
	public WLength getColumnWidth(int column) {
		return this.columnInfo(column).width;
	}

	/**
	 * Changes the visibility of a column.
	 * <p>
	 * 
	 * @see WAbstractItemView#isColumnHidden(int column)
	 */
	public void setColumnHidden(int column, boolean hidden) {
		this.columnInfo(column).hidden = hidden;
	}

	/**
	 * Returns if a column is hidden.
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnHidden(int column, boolean hidden)
	 */
	public boolean isColumnHidden(int column) {
		return this.columnInfo(column).hidden;
	}

	/**
	 * Hides a column.
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
	 * <p>
	 * 
	 * @see WAbstractItemView#hideColumn(int column)
	 * @see WAbstractItemView#setColumnHidden(int column, boolean hidden)
	 */
	public void showColumn(int column) {
		this.setColumnHidden(column, false);
	}

	/**
	 * Sets the column border color.
	 * <p>
	 * The default border color is defined by the CSS theme.
	 */
	public abstract void setColumnBorder(WColor color);

	/**
	 * Sets the header height.
	 * <p>
	 * The default value is 20 pixels.
	 */
	public void setHeaderHeight(WLength height) {
		this.headerLineHeight_ = height;
		int lineCount = this.getHeaderLevelCount();
		WLength headerHeight = WLength.multiply(this.headerLineHeight_,
				lineCount);
		if (this.columns_.size() > 0) {
			WWidget w = this.headerWidget(0);
			if (w != null) {
				w.scheduleRender();
			}
		}
		this.headerHeightRule_.getTemplateWidget().resize(WLength.Auto,
				headerHeight);
	}

	/**
	 * Sets the header height, and vertical alignment (<b>deprecated</b>). By
	 * default, the header text is a single line that is centered vertically.
	 * <p>
	 * Along with setting the header height, this also configures vertical
	 * aligment and header label word wrapping. This has been deprecated in
	 * favour of the more fine grained control using
	 * {@link WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
	 * setHeaderAlignment()} and
	 * {@link WAbstractItemView#setHeaderWordWrap(int column, boolean enabled)
	 * setHeaderWordWrap()}.
	 * <p>
	 * When <code>multiLine</code> is <code>false</code>, the header alignment
	 * is set to {@link AlignmentFlag#AlignMiddle}. When <code>multiLine</code>
	 * is <code>true</code>, the header alignment is set to
	 * {@link AlignmentFlag#AlignTop} and header word wrap is set to
	 * <code>true</code>.
	 * <p>
	 * 
	 * @deprecated use {@link WAbstractItemView#setHeaderHeight(WLength height)
	 *             setHeaderHeight()},
	 *             {@link WAbstractItemView#setHeaderAlignment(int column, EnumSet alignment)
	 *             setHeaderAlignment()} and
	 *             {@link WAbstractItemView#setHeaderWordWrap(int column, boolean enabled)
	 *             setHeaderWordWrap()} instead.
	 */
	public void setHeaderHeight(WLength height, boolean multiLine) {
		this.setHeaderHeight(height);
		if (multiLine) {
			this.defaultHeaderVAlignment_ = AlignmentFlag.AlignTop;
			this.defaultHeaderWordWrap_ = true;
		} else {
			this.defaultHeaderVAlignment_ = AlignmentFlag.AlignMiddle;
			this.defaultHeaderWordWrap_ = false;
		}
		for (int i = 0; i < this.columns_.size(); ++i) {
			this.setHeaderAlignment(i, EnumSet.of(
					this.columns_.get(i).headerHAlignment,
					this.defaultHeaderVAlignment_));
			this.setHeaderWordWrap(i, this.defaultHeaderWordWrap_);
		}
	}

	/**
	 * Returns the header height.
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderHeight(WLength height)
	 */
	public WLength getHeaderHeight() {
		return this.headerLineHeight_;
	}

	/**
	 * Returns the number of pages.
	 * <p>
	 * When Ajax/JavaScript is not available, the view will use a paging
	 * navigation bar to allow scrolling through the data. This returns the
	 * number of pages currently shown.
	 * <p>
	 * 
	 * @see WAbstractItemView#getCreatePageNavigationBar()
	 * @see WAbstractItemView#pageChanged()
	 */
	public abstract int getPageCount();

	/**
	 * Returns the page size.
	 * <p>
	 * When Ajax/JavaScript is not available, the view will use a paging
	 * navigation bar to allow scrolling through the data. This returns the
	 * number of items per page.
	 * <p>
	 * 
	 * @see WAbstractItemView#getCreatePageNavigationBar()
	 * @see WAbstractItemView#pageChanged()
	 */
	public abstract int getPageSize();

	/**
	 * Returns the current page.
	 * <p>
	 * When Ajax/JavaScript is not available, the view will use a paging
	 * navigation bar to allow scrolling through the data. This returns the
	 * current page (between 0 and {@link WAbstractItemView#getPageCount()
	 * getPageCount()} - 1).
	 * <p>
	 * 
	 * @see WAbstractItemView#getCreatePageNavigationBar()
	 * @see WAbstractItemView#pageChanged()
	 */
	public abstract int getCurrentPage();

	/**
	 * Sets the current page.
	 * <p>
	 * When Ajax/JavaScript is not available, the view will use a paging
	 * navigation bar to allow scrolling through the data. This method can be
	 * used to change the current page.
	 * <p>
	 * 
	 * @see WAbstractItemView#getCreatePageNavigationBar()
	 * @see WAbstractItemView#pageChanged()
	 */
	public abstract void setCurrentPage(int page);

	/**
	 * Scrolls the view to an item.
	 * <p>
	 * Scrolls the view to ensure that the item which represents the provided
	 * <code>index</code> is visible. A <code>hint</code> may indicate how the
	 * item should appear in the viewport (if possible).
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Currently only implemented to scroll to the correct row,
	 * not taking into account the column. </i>
	 * </p>
	 */
	public abstract void scrollTo(WModelIndex index,
			WAbstractItemView.ScrollHint hint);

	/**
	 * Scrolls the view to an item.
	 * <p>
	 * Calls
	 * {@link #scrollTo(WModelIndex index, WAbstractItemView.ScrollHint hint)
	 * scrollTo(index, WAbstractItemView.ScrollHint.EnsureVisible)}
	 */
	public final void scrollTo(WModelIndex index) {
		scrollTo(index, WAbstractItemView.ScrollHint.EnsureVisible);
	}

	/**
	 * Configures what actions should trigger editing.
	 * <p>
	 * The default value is DoubleClicked.
	 * <p>
	 * 
	 * @see WAbstractItemView#edit(WModelIndex index)
	 */
	public void setEditTriggers(
			EnumSet<WAbstractItemView.EditTrigger> editTriggers) {
		this.editTriggers_ = EnumSet.copyOf(editTriggers);
	}

	/**
	 * Configures what actions should trigger editing.
	 * <p>
	 * Calls {@link #setEditTriggers(EnumSet editTriggers)
	 * setEditTriggers(EnumSet.of(editTrigger, editTriggers))}
	 */
	public final void setEditTriggers(
			WAbstractItemView.EditTrigger editTrigger,
			WAbstractItemView.EditTrigger... editTriggers) {
		setEditTriggers(EnumSet.of(editTrigger, editTriggers));
	}

	/**
	 * Returns the editing triggers.
	 * <p>
	 * 
	 * @see WAbstractItemView#setEditTriggers(EnumSet editTriggers)
	 */
	public EnumSet<WAbstractItemView.EditTrigger> getEditTriggers() {
		return this.editTriggers_;
	}

	/**
	 * Configures editing options.
	 * <p>
	 * The default value is SingleEditor;
	 */
	public void setEditOptions(EnumSet<WAbstractItemView.EditOption> editOptions) {
		this.editOptions_ = EnumSet.copyOf(editOptions);
	}

	/**
	 * Configures editing options.
	 * <p>
	 * Calls {@link #setEditOptions(EnumSet editOptions)
	 * setEditOptions(EnumSet.of(editOption, editOptions))}
	 */
	public final void setEditOptions(WAbstractItemView.EditOption editOption,
			WAbstractItemView.EditOption... editOptions) {
		setEditOptions(EnumSet.of(editOption, editOptions));
	}

	/**
	 * Returns the editing options.
	 * <p>
	 * 
	 * @see WAbstractItemView#setEditOptions(EnumSet editOptions)
	 */
	public EnumSet<WAbstractItemView.EditOption> getEditOptions() {
		return this.editOptions_;
	}

	/**
	 * Opens an editor for the given index.
	 * <p>
	 * Unless multiple editors are enabled, any other open editor is closed
	 * first.
	 * <p>
	 * 
	 * @see WAbstractItemView#setEditTriggers(EnumSet editTriggers)
	 * @see WAbstractItemView#setEditOptions(EnumSet editOptions)
	 * @see WAbstractItemView#closeEditor(WModelIndex index, boolean saveData)
	 */
	public void edit(WModelIndex index) {
		if (!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsEditable)
				.isEmpty()
				&& !this.isEditing(index)) {
			if (!EnumUtils.mask(this.editOptions_,
					WAbstractItemView.EditOption.SingleEditor).isEmpty()) {
				while (!this.editedItems_.isEmpty()) {
					this.closeEditor(this.editedItems_.entrySet().iterator()
							.next().getKey(), false);
				}
			}
			this.editedItems_.put(index, new WAbstractItemView.Editor());
			this.editedItems_.get(index).widget = null;
			this.editedItems_.get(index).stateSaved = false;
			this.modelDataChanged(index, index);
		}
	}

	/**
	 * Closes the editor for the given index.
	 * <p>
	 * If <code>saveData</code> is true, then the currently edited value is
	 * saved first to the model.
	 * <p>
	 * 
	 * @see WAbstractItemView#edit(WModelIndex index)
	 */
	public void closeEditor(WModelIndex index, boolean saveData) {
		WAbstractItemView.Editor i = this.editedItems_.get(index);
		if (i != null) {
			WAbstractItemView.Editor editor = i;
			WModelIndex closed = index;
			this.editedItems_.remove(index);
			if (saveData
					|| !EnumUtils.mask(this.editOptions_,
							WAbstractItemView.EditOption.SaveWhenClosed)
							.isEmpty()) {
				this.saveEditedValue(closed, editor);
			}
			this.modelDataChanged(closed, closed);
		}
	}

	/**
	 * Closes the editor for the given index.
	 * <p>
	 * Calls {@link #closeEditor(WModelIndex index, boolean saveData)
	 * closeEditor(index, true)}
	 */
	public final void closeEditor(WModelIndex index) {
		closeEditor(index, true);
	}

	/**
	 * Closes all open editors.
	 * <p>
	 * If <code>saveData</code> is true, then the currently edited values are
	 * saved to the model before closing the editor.
	 * <p>
	 * 
	 * @see WAbstractItemView#closeEditor(WModelIndex index, boolean saveData)
	 */
	public void closeEditors(boolean saveData) {
		while (!this.editedItems_.isEmpty()) {
			this.closeEditor(this.editedItems_.entrySet().iterator().next()
					.getKey(), saveData);
		}
	}

	/**
	 * Closes all open editors.
	 * <p>
	 * Calls {@link #closeEditors(boolean saveData) closeEditors(true)}
	 */
	public final void closeEditors() {
		closeEditors(true);
	}

	/**
	 * Validates the editor for the given index.
	 * <p>
	 * Validation is done by invoking
	 * {@link WAbstractItemDelegate#validate(WModelIndex index, Object editState)
	 * WAbstractItemDelegate#validate()}.
	 */
	public WValidator.State validateEditor(WModelIndex index) {
		WAbstractItemView.Editor i = this.editedItems_.get(index);
		if (i != null) {
			WAbstractItemDelegate delegate = this.getItemDelegate(index);
			Object editState = new Object();
			WAbstractItemView.Editor editor = i;
			if (editor.widget != null) {
				editState = delegate.getEditState(editor.widget);
			} else {
				editState = editor.editState;
			}
			WValidator.State state = delegate.validate(index, editState);
			editor.valid = state == WValidator.State.Valid;
			return state;
		}
		return WValidator.State.Invalid;
	}

	/**
	 * Validates all editors.
	 * <p>
	 * 
	 * @see WAbstractItemView#validateEditor(WModelIndex index)
	 */
	public WValidator.State validateEditors() {
		WValidator.State state = WValidator.State.Valid;
		for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it = this.editedItems_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
			WValidator.State s = this.validateEditor(i.getKey());
			if (s.getValue() < state.getValue()) {
				state = s;
			}
		}
		return state;
	}

	/**
	 * Returns whether an editor is open for a given index.
	 * <p>
	 * 
	 * @see WAbstractItemView#edit(WModelIndex index)
	 */
	public boolean isEditing(WModelIndex index) {
		return this.editedItems_.get(index) != null;
	}

	/**
	 * Returns whether an editor&apos;s state is valid.
	 */
	public boolean isValid(WModelIndex index) {
		WAbstractItemView.Editor i = this.editedItems_.get(index);
		if (i != null) {
			WAbstractItemView.Editor editor = i;
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
	 * <p>
	 * When the event happened over an item, the first argument indicates the
	 * item that was clicked on.
	 * <p>
	 * 
	 * @see WAbstractItemView#doubleClicked()
	 */
	public Signal2<WModelIndex, WMouseEvent> clicked() {
		return this.clicked_;
	}

	/**
	 * Signal emitted when double clicked.
	 * <p>
	 * When the event happened over an item, the first argument indicates the
	 * item that was double clicked on.
	 * <p>
	 * 
	 * @see WAbstractItemView#clicked()
	 */
	public Signal2<WModelIndex, WMouseEvent> doubleClicked() {
		return this.doubleClicked_;
	}

	/**
	 * Signal emitted when a mouse button is pressed down.
	 * <p>
	 * When the event happened over an item, the first argument indicates the
	 * item where the mouse went down.
	 * <p>
	 * 
	 * @see WAbstractItemView#mouseWentUp()
	 */
	public Signal2<WModelIndex, WMouseEvent> mouseWentDown() {
		return this.mouseWentDown_;
	}

	/**
	 * Signal emitted when the mouse button is released.
	 * <p>
	 * When the event happened over an item, the first argument indicates the
	 * item where the mouse went up.
	 * <p>
	 * 
	 * @see WAbstractItemView#mouseWentDown()
	 */
	public Signal2<WModelIndex, WMouseEvent> mouseWentUp() {
		return this.mouseWentUp_;
	}

	/**
	 * Signal emitted when the selection is changed.
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
	 * <p>
	 * When Ajax/JavaScript is not available, the view will use a paging
	 * navigation bar to allow scrolling through the data. This signal is
	 * emitted when page-related information changed (e.g. the current page was
	 * changed, or the number of rows was changed).
	 * <p>
	 * 
	 * @see WAbstractItemView#getCreatePageNavigationBar()
	 */
	public Signal pageChanged() {
		return this.pageChanged_;
	}

	/**
	 * Returns the signal emitted when a column is resized by the user.
	 * <p>
	 * The arguments of the signal are: the column index and the new width of
	 * the column.
	 */
	public Signal2<Integer, WLength> columnResized() {
		return this.columnResized_;
	}

	/**
	 * Configures the number of columns that are used as row headers.
	 * <p>
	 * An item view does not use the vertical header data from the model in any
	 * way, but instead you can configure data in the first column(s) to be used
	 * as a row headers.
	 * <p>
	 * These columns will not scroll horizontally together with the rest of the
	 * model.
	 * <p>
	 * The default value is 0.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Currently, this property must be set before any other
	 * settings of the view and only a value of 0 or 1 is supported. </i>
	 * </p>
	 */
	public void setRowHeaderCount(int count) {
		this.rowHeaderCount_ = count;
	}

	/**
	 * Returns the number of columns that are used as row headers.
	 * <p>
	 * 
	 * @see WAbstractItemView#setRowHeaderCount(int count)
	 */
	public int getRowHeaderCount() {
		return this.rowHeaderCount_;
	}

	/**
	 * Configures whether horizontal scrolling includes the first column
	 * (<b>deprecated</b>).
	 * <p>
	 * To display a model with many columns, this option allows you to keep the
	 * first column fixed while scrolling through the other columns of the
	 * model.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @deprecated Use {@link WAbstractItemView#setRowHeaderCount(int count)
	 *             setRowHeaderCount()} instead.
	 */
	public void setColumn1Fixed(boolean enable) {
		this.setRowHeaderCount(enable ? 1 : 0);
	}

	/**
	 * Returns whether horizontal scrolling includes the first column.
	 * (<b>deprecated</b>).
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumn1Fixed(boolean enable)
	 * @deprecated Use {@link WAbstractItemView#getRowHeaderCount()
	 *             getRowHeaderCount()} instead.
	 */
	public boolean isColumn1Fixed() {
		return this.rowHeaderCount_ == 1;
	}

	/**
	 * Creates a new item view.
	 */
	protected WAbstractItemView(WContainerWidget parent) {
		super(parent);
		this.impl_ = new WContainerWidget();
		this.renderState_ = WAbstractItemView.RenderState.NeedRerender;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.columns_ = new ArrayList<WAbstractItemView.ColumnInfo>();
		this.currentSortColumn_ = -1;
		this.dragEnabled_ = false;
		this.dropsEnabled_ = false;
		this.model_ = null;
		this.headerModel_ = null;
		this.rootIndex_ = null;
		this.itemDelegate_ = null;
		this.headerItemDelegate_ = null;
		this.selectionModel_ = new WItemSelectionModel(
				(WAbstractItemModel) null, this);
		this.rowHeight_ = new WLength(20);
		this.headerLineHeight_ = new WLength(20);
		this.selectionMode_ = SelectionMode.NoSelection;
		this.sorting_ = true;
		this.columnResize_ = true;
		this.defaultHeaderVAlignment_ = AlignmentFlag.AlignMiddle;
		this.defaultHeaderWordWrap_ = true;
		this.rowHeaderCount_ = 0;
		this.computedDragMimeType_ = new WString();
		this.columnWidthChanged_ = new JSignal2<Integer, Integer>(this.impl_,
				"columnResized") {
		};
		this.columnResized_ = new Signal2<Integer, WLength>(this);
		this.nextColumnId_ = 1;
		this.alternatingRowColors_ = false;
		this.resizeHandleMDownJS_ = new JSlot();
		this.editedItems_ = new HashMap<WModelIndex, WAbstractItemView.Editor>();
		this.clicked_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.doubleClicked_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.mouseWentDown_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.mouseWentUp_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.selectionChanged_ = new Signal(this);
		this.pageChanged_ = new Signal(this);
		this.editTriggers_ = EnumSet
				.of(WAbstractItemView.EditTrigger.DoubleClicked);
		this.editOptions_ = EnumSet
				.of(WAbstractItemView.EditOption.SingleEditor);
		this.setImplementation(this.impl_);
		this.setItemDelegate(new WItemDelegate(this));
		this.setHeaderItemDelegate(new WItemDelegate(this));
		WApplication app = WApplication.getInstance();
		this.clickedForSortMapper_ = new WSignalMapper1<Integer>(this);
		this.clickedForSortMapper_.mapped().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WAbstractItemView.this.toggleSortColumn(e1);
					}
				});
		this.clickedForCollapseMapper_ = new WSignalMapper1<Integer>(this);
		this.clickedForCollapseMapper_.mapped().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WAbstractItemView.this.collapseColumn(e1);
					}
				});
		this.clickedForExpandMapper_ = new WSignalMapper1<Integer>(this);
		this.clickedForExpandMapper_.mapped().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WAbstractItemView.this.expandColumn(e1);
					}
				});
		SizeHandle.loadJavaScript(app);
		if (!app.getEnvironment().hasAjax()) {
			this.clickedMapper_ = new WSignalMapper2<WModelIndex, WMouseEvent>(
					this);
			this.clickedMapper_.mapped().addListener(this,
					new Signal2.Listener<WModelIndex, WMouseEvent>() {
						public void trigger(WModelIndex e1, WMouseEvent e2) {
							WAbstractItemView.this.handleClick(e1, e2);
						}
					});
			this.columnResize_ = false;
		}
		this.bindObjJS(this.resizeHandleMDownJS_, "resizeHandleMDown");
		this.columnWidthChanged_.addListener(this,
				new Signal2.Listener<Integer, Integer>() {
					public void trigger(Integer e1, Integer e2) {
						WAbstractItemView.this.updateColumnWidth(e1, e2);
					}
				});
		this.headerHeightRule_ = new WCssTemplateRule("#" + this.getId()
				+ " .headerrh", this);
		app.getStyleSheet().addRule(this.headerHeightRule_);
		this.setHeaderHeight(this.headerLineHeight_);
	}

	/**
	 * Creates a new item view.
	 * <p>
	 * Calls {@link #WAbstractItemView(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	protected WAbstractItemView() {
		this((WContainerWidget) null);
	}

	/**
	 * Handles a drop event (drag &amp; drop).
	 * <p>
	 * The <code>event</code> object contains details about the drop operation,
	 * identifying the source (which provides the data) and the mime-type of the
	 * data. The drop was received on the <code>target</code> item.
	 * <p>
	 * The drop event can be handled either by the view itself, or by the model.
	 * The default implementation checks if the mime-type is accepted by the
	 * model, and if so passes the drop event to the model. If the source is the
	 * view&apos;s own selection model, then the drop event will be handled as a
	 * {@link DropAction#MoveAction MoveAction}, otherwise the drop event will
	 * be handled as a {@link DropAction#CopyAction CopyAction}.
	 * <p>
	 * 
	 * @see WAbstractItemModel#dropEvent(WDropEvent e, DropAction action, int
	 *      row, int column, WModelIndex parent)
	 */
	protected void dropEvent(WDropEvent e, WModelIndex index) {
		if (this.dropsEnabled_) {
			List<String> acceptMimeTypes = this.model_.getAcceptDropMimeTypes();
			for (int i = 0; i < acceptMimeTypes.size(); ++i) {
				if (acceptMimeTypes.get(i).equals(e.getMimeType())) {
					boolean internal = e.getSource() == this.selectionModel_;
					DropAction action = internal ? DropAction.MoveAction
							: DropAction.CopyAction;
					this.model_.dropEvent(e, action, index.getRow(), index
							.getColumn(), index.getParent());
					this.setSelectedIndexes(new TreeSet<WModelIndex>());
					return;
				}
			}
		}
		super.dropEvent(e);
	}

	/**
	 * Create an extra widget in the header.
	 * <p>
	 * You may reimplement this method to provide an extra widget to be placed
	 * below the header label. The extra widget will be visible only if a
	 * multi-line header is configured using
	 * {@link WAbstractItemView#setHeaderHeight(WLength height)
	 * setHeaderHeight()}.
	 * <p>
	 * The widget is created only once, but this method may be called repeatedly
	 * for a column for which prior calls returned <code>null</code> (i.e. each
	 * time the header is rerendered).
	 * <p>
	 * The default implementation returns <code>null</code>.
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
	 * <p>
	 * Returns the widget previously created using
	 * {@link WAbstractItemView#createExtraHeaderWidget(int column)
	 * createExtraHeaderWidget()}
	 * <p>
	 * 
	 * @see WAbstractItemView#createExtraHeaderWidget(int column)
	 */
	protected WWidget extraHeaderWidget(int column) {
		return this.columnInfo(column).extraHeaderWidget;
	}

	/**
	 * Returns a page navigation widget.
	 * <p>
	 * When Ajax/JavaScript is not available, the view will use a paging
	 * navigation bar to allow scrolling through the data, created by this
	 * method. The default implementation displays a simple navigation bar with
	 * (First, Prevous, Next, Last) buttons and a page counter.
	 * <p>
	 * You may want to reimplement this method to provide a custom page
	 * navigation bar. You can use the
	 * {@link WAbstractItemView#getCurrentPage() getCurrentPage()},
	 * {@link WAbstractItemView#getPageCount() getPageCount()}, and
	 * {@link WAbstractItemView#setCurrentPage(int page) setCurrentPage()}
	 * methods to set or get the page information, and listen to the
	 * {@link WAbstractItemView#pageChanged() pageChanged()} signal to react to
	 * changes.
	 */
	protected WWidget getCreatePageNavigationBar() {
		return new DefaultPagingBar(this);
	}

	static class ColumnInfo {
		private static Logger logger = LoggerFactory
				.getLogger(ColumnInfo.class);

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
			this.id = anId;
			this.sortOrder = SortOrder.AscendingOrder;
			this.alignment = AlignmentFlag.AlignLeft;
			this.headerHAlignment = AlignmentFlag.AlignLeft;
			this.headerVAlignment = view.defaultHeaderVAlignment_;
			this.headerWordWrap = view.defaultHeaderWordWrap_;
			this.width = new WLength();
			this.extraHeaderWidget = null;
			this.sorting = view.sorting_;
			this.hidden = false;
			this.itemDelegate_ = null;
			this.width = new WLength(150);
			this.styleRule = new WCssTemplateRule("#" + view.getId() + " ."
					+ this.getStyleClass());
			this.styleRule.getTemplateWidget().resize(
					new WLength(this.width.toPixels()), WLength.Auto);
			WApplication.getInstance().getStyleSheet().addRule(this.styleRule);
		}
	}

	enum RenderState {
		RenderOk(0), NeedAdjustViewPort(1), NeedRerenderData(2), NeedRerenderHeader(
				3), NeedRerender(4);

		private int value;

		RenderState(int value) {
			this.value = value;
		}

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return value;
		}
	}

	WContainerWidget impl_;
	WAbstractItemView.RenderState renderState_;
	List<AbstractSignal.Connection> modelConnections_;
	WSignalMapper2<WModelIndex, WMouseEvent> clickedMapper_;
	List<WAbstractItemView.ColumnInfo> columns_;
	int currentSortColumn_;
	boolean dragEnabled_;
	boolean dropsEnabled_;
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

	abstract void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight);

	void modelLayoutAboutToBeChanged() {
		if ((this.rootIndex_ != null)) {
			this.rootIndex_.encodeAsRawIndex();
		}
		for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it = this.editedItems_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
			this.persistEditor(i.getKey(), i.getValue());
			i.getKey().encodeAsRawIndex();
		}
	}

	void modelLayoutChanged() {
		if ((this.rootIndex_ != null)) {
			this.rootIndex_ = this.rootIndex_.decodeFromRawIndex();
		}
		Map<WModelIndex, WAbstractItemView.Editor> newEditorMap = new HashMap<WModelIndex, WAbstractItemView.Editor>();
		for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it = this.editedItems_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
			WModelIndex m = i.getKey().decodeFromRawIndex();
			if ((m != null)) {
				newEditorMap.put(m, i.getValue());
			}
		}
		this.editedItems_ = newEditorMap;
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
	}

	protected void modelHeaderDataChanged(Orientation orientation, int start,
			int end) {
		if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
				.getValue()) {
			if (orientation == Orientation.Horizontal) {
				for (int i = start; i <= end; ++i) {
					WContainerWidget hw = ((this.headerWidget(i, true)) instanceof WContainerWidget ? (WContainerWidget) (this
							.headerWidget(i, true))
							: null);
					WWidget tw = hw.getWidget(hw.getCount() - 1);
					this.headerItemDelegate_.update(tw, this.headerModel_
							.getIndex(0, i), EnumSet
							.noneOf(ViewItemRenderFlag.class));
					tw.setInline(false);
					tw.addStyleClass("Wt-label");
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
			while ((int) j < this.columns_.size()
					&& this.columns_.get(j).hidden) {
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
				(((w.getParent()) instanceof WContainerWidget ? (WContainerWidget) (w
						.getParent())
						: null)).removeWidget(w);
			}
		}
	}

	WWidget createHeaderWidget(WApplication app, int column) {
		WAbstractItemView.ColumnInfo info = this.columnInfo(column);
		WContainerWidget contents = new WContainerWidget();
		contents.setObjectName("contents");
		if (info.sorting) {
			WText sortIcon = new WText(contents);
			sortIcon.setObjectName("sort");
			sortIcon.setInline(false);
			sortIcon.setStyleClass("Wt-tv-sh Wt-tv-sh-none");
			this.clickedForSortMapper_.mapConnect(sortIcon.clicked(), info.id);
			if (this.currentSortColumn_ == column) {
				sortIcon
						.setStyleClass(info.sortOrder == SortOrder.AscendingOrder ? "Wt-tv-sh Wt-tv-sh-up"
								: "Wt-tv-sh Wt-tv-sh-down");
			}
		}
		if (!EnumUtils.mask(
				this.model_.getHeaderFlags(column),
				EnumSet.of(HeaderFlag.ColumnIsExpandedLeft,
						HeaderFlag.ColumnIsExpandedRight)).isEmpty()) {
			WImage collapseIcon = new WImage(contents);
			collapseIcon.setFloatSide(Side.Left);
			collapseIcon.setImageLink(new WLink(WApplication
					.getRelativeResourcesUrl()
					+ "minus.gif"));
			this.clickedForCollapseMapper_.mapConnect(collapseIcon.clicked(),
					info.id);
		} else {
			if (!EnumUtils.mask(this.model_.getHeaderFlags(column),
					HeaderFlag.ColumnIsCollapsed).isEmpty()) {
				WImage expandIcon = new WImage(contents);
				expandIcon.setFloatSide(Side.Left);
				expandIcon.setImageLink(new WLink(WApplication
						.getRelativeResourcesUrl()
						+ "plus.gif"));
				this.clickedForExpandMapper_.mapConnect(expandIcon.clicked(),
						info.id);
			}
		}
		WModelIndex index = this.headerModel_.getIndex(0, column);
		WWidget i = this.headerItemDelegate_.update((WWidget) null, index,
				EnumSet.noneOf(ViewItemRenderFlag.class));
		i.setInline(false);
		i.addStyleClass("Wt-label");
		contents.addWidget(i);
		if (info.sorting) {
			WInteractWidget ww = ((i) instanceof WInteractWidget ? (WInteractWidget) (i)
					: null);
			if (ww != null) {
				this.clickedForSortMapper_.mapConnect(ww.clicked(), info.id);
			}
		}
		int headerLevel = this.model_ != null ? this.headerLevel(column) : 0;
		contents.setMargin(new WLength(headerLevel
				* this.headerLineHeight_.toPixels()), EnumSet.of(Side.Top));
		int rightBorderLevel = headerLevel;
		if (this.model_ != null) {
			int rightColumn = this.modelColumnIndex(this
					.visibleColumnIndex(column) + 1);
			if (rightColumn != -1) {
				EnumSet<HeaderFlag> flagsLeft = this.model_
						.getHeaderFlags(column);
				EnumSet<HeaderFlag> flagsRight = this.model_
						.getHeaderFlags(rightColumn);
				int rightHeaderLevel = this.headerLevel(rightColumn);
				if (!EnumUtils
						.mask(flagsLeft, HeaderFlag.ColumnIsExpandedRight)
						.isEmpty()) {
					rightBorderLevel = headerLevel + 1;
				} else {
					if (!EnumUtils.mask(flagsRight,
							HeaderFlag.ColumnIsExpandedLeft).isEmpty()) {
						rightBorderLevel = rightHeaderLevel + 1;
					} else {
						rightBorderLevel = Math.min(headerLevel,
								rightHeaderLevel);
					}
				}
			}
		}
		boolean activeRH = this.columnResize_;
		WContainerWidget resizeHandle = new WContainerWidget();
		resizeHandle.setStyleClass("Wt-tv-rh"
				+ (activeRH ? "" : " Wt-tv-no-rh") + " Wt-tv-br headerrh");
		if (activeRH) {
			resizeHandle.mouseWentDown().addListener(this.resizeHandleMDownJS_);
		}
		resizeHandle.setMargin(new WLength(rightBorderLevel
				* this.headerLineHeight_.toPixels()), EnumSet.of(Side.Top));
		if (!(this.columnInfo(column).extraHeaderWidget != null)) {
			this.columnInfo(column).extraHeaderWidget = this
					.createExtraHeaderWidget(column);
		}
		WWidget extraW = this.columnInfo(column).extraHeaderWidget;
		WContainerWidget result = new WContainerWidget();
		result.setStyleClass(info.getStyleClass() + " Wt-tv-c headerrh");
		result.addWidget(resizeHandle);
		WContainerWidget main = new WContainerWidget();
		main.setOverflow(WContainerWidget.Overflow.OverflowHidden);
		main.setContentAlignment(info.headerHAlignment);
		result.addWidget(main);
		main.addWidget(contents);
		if (info.headerVAlignment == AlignmentFlag.AlignMiddle) {
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
		String sc = StringUtils.asString(
				index.getData(ItemDataRole.StyleClassRole)).toString();
		if (sc.length() != 0) {
			result.addStyleClass(sc);
		}
		return result;
	}

	// WText headerTextWidget(int column) ;
	/**
	 * Handles a click event.
	 * <p>
	 * This processes the event for internal purposes (such as selection or
	 * editing) and emits the {@link WAbstractItemView#clicked() clicked()}
	 * signal.
	 * <p>
	 * You may want to override this signal to override the built-in selection
	 * or editing behaviour.
	 */
	void handleClick(WModelIndex index, WMouseEvent event) {
		boolean doEdit = (index != null)
				&& (!EnumUtils.mask(this.getEditTriggers(),
						WAbstractItemView.EditTrigger.SelectedClicked)
						.isEmpty()
						&& this.isSelected(index) || !EnumUtils.mask(
						this.getEditTriggers(),
						WAbstractItemView.EditTrigger.SingleClicked).isEmpty());
		if ((index != null)) {
			this.selectionHandleClick(index, event.getModifiers());
		}
		if (doEdit) {
			this.edit(index);
		}
		this.clicked_.trigger(index, event);
	}

	/**
	 * Handles a double click event.
	 * <p>
	 * This processes the event for internal purposes (such as editing) and
	 * emits the {@link WAbstractItemView#doubleClicked() doubleClicked()}
	 * signal.
	 * <p>
	 * You may want to override this signal to override the built-in editing
	 * behaviour.
	 */
	void handleDoubleClick(WModelIndex index, WMouseEvent event) {
		boolean doEdit = (index != null)
				&& !EnumUtils.mask(this.getEditTriggers(),
						WAbstractItemView.EditTrigger.DoubleClicked).isEmpty();
		if (doEdit) {
			this.edit(index);
		}
		this.doubleClicked_.trigger(index, event);
	}

	/**
	 * Handles a mouse down event.
	 * <p>
	 * This emits the {@link WAbstractItemView#mouseWentDown() mouseWentDown()}
	 * signal.
	 */
	void handleMouseDown(WModelIndex index, WMouseEvent event) {
		this.mouseWentDown_.trigger(index, event);
	}

	/**
	 * Handles a mouse up event.
	 * <p>
	 * This emits the {@link WAbstractItemView#mouseWentUp() mouseWentUp()}
	 * signal.
	 */
	void handleMouseUp(WModelIndex index, WMouseEvent event) {
		this.mouseWentUp_.trigger(index, event);
	}

	boolean internalSelect(WModelIndex index, SelectionFlag option) {
		if (!!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsSelectable)
				.isEmpty()
				|| this.getSelectionMode() == SelectionMode.NoSelection) {
			return false;
		}
		if (option == SelectionFlag.ToggleSelect) {
			option = this.isSelected(index) ? SelectionFlag.Deselect
					: SelectionFlag.Select;
		} else {
			if (option == SelectionFlag.ClearAndSelect) {
				this.clearSelection();
				option = SelectionFlag.Select;
			} else {
				if (this.getSelectionMode() == SelectionMode.SingleSelection
						&& option == SelectionFlag.Select) {
					this.clearSelection();
				}
			}
		}
		if (option == SelectionFlag.Select) {
			this.getSelectionModel().selection_.add(index);
		} else {
			if (!this.getSelectionModel().selection_.remove(index)) {
				return false;
			}
		}
		return true;
	}

	void setEditState(WModelIndex index, Object editState) {
		this.editedItems_.get(index).editState = editState;
	}

	Object getEditState(WModelIndex index) {
		WAbstractItemView.Editor i = this.editedItems_.get(index);
		if (i != null) {
			return i.editState;
		} else {
			return null;
		}
	}

	boolean hasEditFocus(WModelIndex index) {
		WAbstractItemView.Editor i = this.editedItems_.get(index);
		if (i != null) {
			WAbstractItemView.Editor editor = i;
			return !(editor.widget != null) && !editor.stateSaved;
		} else {
			return false;
		}
	}

	void setEditorWidget(WModelIndex index, WWidget editor) {
		this.editedItems_.get(index).widget = editor;
		this.editedItems_.get(index).stateSaved = !(editor != null);
	}

	void bindObjJS(JSlot slot, String jsMethod) {
		slot.setJavaScript("function(obj, event) {jQuery.data("
				+ this.getJsRef() + ", 'obj')." + jsMethod + "(obj, event);}");
	}

	void connectObjJS(AbstractEventSignal s, String jsMethod) {
		s.addListener("function(obj, event) {jQuery.data(" + this.getJsRef()
				+ ", 'obj')." + jsMethod + "(obj, event);}");
	}

	protected boolean shiftEditorRows(WModelIndex parent, int start, int count,
			boolean persistWhenShifted) {
		boolean result = false;
		if (!this.editedItems_.isEmpty()) {
			List<WModelIndex> toClose = new ArrayList<WModelIndex>();
			Map<WModelIndex, WAbstractItemView.Editor> newMap = new HashMap<WModelIndex, WAbstractItemView.Editor>();
			for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it = this.editedItems_
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it
						.next();
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
								WModelIndex shifted = this.model_.getIndex(c
										.getRow()
										+ count, c.getColumn(), p);
								newMap.put(shifted, i.getValue());
								if (i.getValue().widget != null) {
									if (persistWhenShifted) {
										this.persistEditor(shifted, i
												.getValue());
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
								if ((p.getParent() == parent || (p.getParent() != null && p
										.getParent().equals(parent)))
										&& p.getRow() >= start
										&& p.getRow() < start - count) {
									toClose.add(c);
									break;
								} else {
									p = p.getParent();
								}
							} while (!(p == parent || (p != null && p
									.equals(parent))));
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

	protected boolean shiftEditorColumns(WModelIndex parent, int start,
			int count, boolean persistWhenShifted) {
		boolean result = false;
		if (!this.editedItems_.isEmpty()) {
			List<WModelIndex> toClose = new ArrayList<WModelIndex>();
			Map<WModelIndex, WAbstractItemView.Editor> newMap = new HashMap<WModelIndex, WAbstractItemView.Editor>();
			for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it = this.editedItems_
					.entrySet().iterator(); i_it.hasNext();) {
				Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it
						.next();
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
								WModelIndex shifted = this.model_.getIndex(c
										.getRow(), c.getColumn() + count, p);
								newMap.put(shifted, i.getValue());
								if (i.getValue().widget != null) {
									if (persistWhenShifted) {
										this.persistEditor(shifted, i
												.getValue());
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
								if ((p.getParent() == parent || (p.getParent() != null && p
										.getParent().equals(parent)))
										&& p.getColumn() >= start
										&& p.getColumn() < start - count) {
									toClose.add(c);
									break;
								} else {
									p = p.getParent();
								}
							} while (!(p == parent || (p != null && p
									.equals(parent))));
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

	void persistEditor(WModelIndex index) {
		WAbstractItemView.Editor i = this.editedItems_.get(index);
		if (i != null) {
			this.persistEditor(index, i);
		}
	}

	static class Editor {
		private static Logger logger = LoggerFactory.getLogger(Editor.class);

		public Editor() {
			this.widget = null;
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
	private JSignal2<Integer, Integer> columnWidthChanged_;
	private Signal2<Integer, WLength> columnResized_;
	private WCssTemplateRule headerHeightRule_;
	private int nextColumnId_;
	private WSignalMapper1<Integer> clickedForSortMapper_;
	private WSignalMapper1<Integer> clickedForExpandMapper_;
	private WSignalMapper1<Integer> clickedForCollapseMapper_;
	private boolean alternatingRowColors_;
	private JSlot resizeHandleMDownJS_;
	private Map<WModelIndex, WAbstractItemView.Editor> editedItems_;
	private Signal2<WModelIndex, WMouseEvent> clicked_;
	private Signal2<WModelIndex, WMouseEvent> doubleClicked_;
	private Signal2<WModelIndex, WMouseEvent> mouseWentDown_;
	private Signal2<WModelIndex, WMouseEvent> mouseWentUp_;
	private Signal selectionChanged_;
	private Signal pageChanged_;
	private EnumSet<WAbstractItemView.EditTrigger> editTriggers_;
	private EnumSet<WAbstractItemView.EditOption> editOptions_;

	private void closeEditorWidget(WWidget editor, boolean saveData) {
		for (Iterator<Map.Entry<WModelIndex, WAbstractItemView.Editor>> i_it = this.editedItems_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WAbstractItemView.Editor> i = i_it.next();
			if (i.getValue().widget == editor) {
				if (!EnumUtils.mask(this.editOptions_,
						WAbstractItemView.EditOption.LeaveEditorsOpen)
						.isEmpty()) {
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

	private void saveEditedValue(WModelIndex index,
			WAbstractItemView.Editor editor) {
		Object editState = new Object();
		WAbstractItemDelegate delegate = this.getItemDelegate(index);
		if (editor.widget != null) {
			editState = delegate.getEditState(editor.widget);
		} else {
			editState = editor.editState;
		}
		delegate.setModelData(editState, this.getModel(), index);
	}

	private void persistEditor(WModelIndex index,
			WAbstractItemView.Editor editor) {
		if (editor.widget != null) {
			editor.editState = this.getItemDelegate(index).getEditState(
					editor.widget);
			editor.stateSaved = true;
			editor.widget = null;
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
			return ((hw.find("sort")) instanceof WText ? (WText) (hw
					.find("sort")) : null);
		} else {
			return null;
		}
	}

	private void selectionHandleClick(WModelIndex index,
			EnumSet<KeyboardModifier> modifiers) {
		if (this.selectionMode_ == SelectionMode.NoSelection) {
			return;
		}
		if (this.selectionMode_ == SelectionMode.ExtendedSelection) {
			if (!EnumUtils.mask(modifiers, KeyboardModifier.ShiftModifier)
					.isEmpty()) {
				this.extendSelection(index);
			} else {
				if (!!EnumUtils.mask(
						modifiers,
						EnumSet.of(KeyboardModifier.ControlModifier,
								KeyboardModifier.MetaModifier)).isEmpty()) {
					this.select(index, SelectionFlag.ClearAndSelect);
				} else {
					this.select(index, SelectionFlag.ToggleSelect);
				}
			}
		} else {
			this.select(index, SelectionFlag.Select);
		}
	}

	private final void selectionHandleClick(WModelIndex index,
			KeyboardModifier modifier, KeyboardModifier... modifiers) {
		selectionHandleClick(index, EnumSet.of(modifier, modifiers));
	}

	private void clearSelection() {
		SortedSet<WModelIndex> nodes = this.selectionModel_.selection_;
		while (!nodes.isEmpty()) {
			WModelIndex i = nodes.iterator().next();
			this.internalSelect(i, SelectionFlag.Deselect);
		}
	}

	private void extendSelection(WModelIndex index) {
		if (this.selectionModel_.selection_.isEmpty()) {
			this.internalSelect(index, SelectionFlag.Select);
		} else {
			if (this.getSelectionBehavior() == SelectionBehavior.SelectRows
					&& index.getColumn() != 0) {
				this.extendSelection(this.model_.getIndex(index.getRow(), 0,
						index.getParent()));
				return;
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
		}
		this.selectionChanged_.trigger();
	}

	abstract void selectRange(WModelIndex first, WModelIndex last);

	private void checkDragSelection() {
		this.computedDragMimeType_ = new WString(this.selectionModel_
				.getMimeType());
		this.setAttributeValue("dmt", this.computedDragMimeType_.toString());
		if (!(this.computedDragMimeType_.length() == 0)) {
			this.setAttributeValue("drag", "true");
		} else {
			this.setAttributeValue("drag", "false");
		}
	}

	void configureModelDragDrop() {
		this.initDragDrop();
		if (!(this.model_ != null)) {
			return;
		}
		if (this.dragEnabled_) {
			this.setAttributeValue("dsid", WApplication.getInstance()
					.encodeObject(this.selectionModel_));
			this.checkDragSelection();
		}
		List<String> acceptMimeTypes = this.model_.getAcceptDropMimeTypes();
		for (int i = 0; i < acceptMimeTypes.size(); ++i) {
			if (this.dropsEnabled_) {
				this.acceptDrops(acceptMimeTypes.get(i), "Wt-drop-site");
			} else {
				this.stopAcceptDrops(acceptMimeTypes.get(i));
			}
		}
	}

	void toggleSortColumn(int columnid) {
		int column = this.columnById(columnid);
		if (column != this.currentSortColumn_) {
			this.sortByColumn(column, this.columnInfo(column).sortOrder);
		} else {
			this
					.sortByColumn(
							column,
							this.columnInfo(column).sortOrder == SortOrder.AscendingOrder ? SortOrder.DescendingOrder
									: SortOrder.AscendingOrder);
		}
	}

	private void updateColumnWidth(int columnId, int width) {
		int column = this.columnById(columnId);
		if (column >= 0) {
			this.columnInfo(column).width = new WLength(width);
			this.columnResized_.trigger(column, this.columnInfo(column).width);
		}
	}

	abstract WContainerWidget getHeaderContainer();

	private int headerLevel(int column) {
		Object d = this.model_.getHeaderData(column, Orientation.Horizontal,
				ItemDataRole.LevelRole);
		if (!(d == null)) {
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
		this
				.addCssRule("#" + this.getId() + "dw",
						"width: 32px; height: 32px;background: url("
								+ WApplication.getResourcesUrl()
								+ "items-not-ok.gif);");
		this.addCssRule("#" + this.getId() + "dw.Wt-valid-drop",
				"width: 32px; height: 32px;background: url("
						+ WApplication.getResourcesUrl() + "items-ok.gif);");
		this.selectionChanged_.addListener(this, new Signal.Listener() {
			public void trigger() {
				WAbstractItemView.this.checkDragSelection();
			}
		});
	}

	static String repeat(String s, int times) {
		String result = "";
		for (int i = 0; i < times; ++i) {
			result += s;
		}
		return result;
	}
}
