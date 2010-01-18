/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * An abstract base class for item views
 */
public abstract class WAbstractItemView extends WCompositeWidget {
	/**
	 * Sets the model.
	 * <p>
	 * The view will render the data in the given <code>model</code>. Changes to
	 * the model are reflected in the view.
	 * <p>
	 * The initial model is <code>null</code>.
	 * <p>
	 * Ownership of the model is not transferred (and thus the previously set
	 * model is not deleted).
	 * <p>
	 */
	public void setModel(WAbstractItemModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
		WItemSelectionModel oldSelectionModel = this.selectionModel_;
		this.selectionModel_ = new WItemSelectionModel(model, this);
		this.selectionModel_.setSelectionBehavior(oldSelectionModel
				.getSelectionBehavior());
		for (int i = this.columns_.size(); i < this.model_.getColumnCount(); ++i) {
			this.columnInfo(i);
		}
		this.configureModelDragDrop();
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerender);
		this.setHeaderHeight(this.headerLineHeight_, this.multiLineHeader_);
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
	 * Sets the content alignment for a column.
	 * <p>
	 * The default value is {@link AlignmentFlag#AlignLeft AlignLeft}.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>For column 0, {@link AlignmentFlag#AlignCenter
	 * AlignCenter} is currently not supported.</i>
	 * </p>
	 * 
	 * @see WAbstractItemView#setHeaderAlignment(int column, AlignmentFlag
	 *      alignment)
	 */
	public abstract void setColumnAlignment(int column, AlignmentFlag alignment);

	/**
	 * Sets the header alignment for a column.
	 * <p>
	 * The default value is {@link AlignmentFlag#AlignLeft AlignLeft}.
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnAlignment(int column, AlignmentFlag
	 *      alignment)
	 */
	public abstract void setHeaderAlignment(int column, AlignmentFlag alignment);

	/**
	 * Returns the content alignment for a column.
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnAlignment(int column, AlignmentFlag
	 *      alignment)
	 */
	public abstract AlignmentFlag getColumnAlignment(int column);

	/**
	 * Returns the header alignment for a column.
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderAlignment(int column, AlignmentFlag
	 *      alignment)
	 */
	public abstract AlignmentFlag getHeaderAlignment(int column);

	/**
	 * Sets if alternating row colors are to be used.
	 */
	public abstract void setAlternatingRowColors(boolean enable);

	/**
	 * Returns whether alternating row colors are used.
	 * <p>
	 * 
	 * @see WAbstractItemView#setAlternatingRowColors(boolean enable)
	 */
	public abstract boolean hasAlternatingRowColors();

	/**
	 * Sort the data according to a column.
	 * <p>
	 * Sorts the data according to data in column <code>column</code> and sort
	 * order <code>order</code>.
	 * <p>
	 * 
	 * @see WAbstractItemModel#sort(int column, SortOrder order)
	 */
	public void sortByColumn(int column, SortOrder order) {
		if (this.currentSortColumn_ != -1) {
			this.headerSortIconWidget(this.currentSortColumn_).setStyleClass(
					"Wt-tv-sh Wt-tv-sh-none");
		}
		this.currentSortColumn_ = column;
		this.columnInfo(column).sortOrder = order;
		if (this.renderState_ != WAbstractItemView.RenderState.NeedRerender) {
			this.headerSortIconWidget(this.currentSortColumn_).setStyleClass(
					order == SortOrder.AscendingOrder ? "Wt-tv-sh Wt-tv-sh-up"
							: "Wt-tv-sh Wt-tv-sh-down");
		}
		this.model_.sort(column, order);
	}

	/**
	 * Enable sorting.
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
		for (int i = 0; i < this.getColumnCount(); ++i) {
			this.columnInfo(i).sorting = enabled;
		}
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
	}

	/**
	 * Enable sorting.
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
	 * Enable interactive column resizing.
	 * <p>
	 * Enable or disable column resize handles for interactive resizing of the
	 * columns.
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
	 * Change the selection behaviour.
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
	 * Select a single item.
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
	 * Select a single item.
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
	 * This is a convenience method for: <blockquote>
	 * 
	 * <pre>
	 * selectionModel().isSelected(index)
	 * </pre>
	 * 
	 * </blockquote>
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
	 * This is a convenience method for: <blockquote>
	 * 
	 * <pre>
	 * selectionModel().selectedIndexes()
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * 
	 * @see WAbstractItemView#setSelectedIndexes(SortedSet indexes)
	 */
	public SortedSet<WModelIndex> getSelectedIndexes() {
		return this.selectionModel_.selection_;
	}

	/**
	 * Enable the selection to be dragged (drag &amp; drop).
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
	 * Enable drop operations (drag &amp; drop).
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
	 * Signal emitted when an item is clicked.
	 * <p>
	 * 
	 * @see WAbstractItemView#doubleClicked()
	 */
	public Signal2<WModelIndex, WMouseEvent> clicked() {
		return this.clicked_;
	}

	/**
	 * Signal emitted when an item is double clicked.
	 * <p>
	 * 
	 * @see WAbstractItemView#doubleClicked()
	 */
	public Signal2<WModelIndex, WMouseEvent> doubleClicked() {
		return this.doubleClicked_;
	}

	/**
	 * Signal emitted when an item is double clicked.
	 * <p>
	 * 
	 * @see WAbstractItemView#doubleClicked()
	 */
	public Signal2<WModelIndex, WMouseEvent> mouseWentDown() {
		return this.mouseWentDown_;
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
	 * Sets the default item delegate.
	 * <p>
	 * The previous delegate is removed but not deleted.
	 * <p>
	 * The default item delegate is a {@link WItemDelegate}.
	 */
	public void setItemDelegate(WAbstractItemDelegate delegate) {
		this.itemDelegate_ = delegate;
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
	 * The previous delegate is removed but not deleted.
	 * <p>
	 * 
	 * @see WAbstractItemView#setItemDelegate(WAbstractItemDelegate delegate)
	 */
	public void setItemDelegateForColumn(int column,
			WAbstractItemDelegate delegate) {
		this.columnInfo(column).itemDelegate_ = delegate;
	}

	/**
	 * Returns the delegate for a column.
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
		WAbstractItemDelegate result = this.getItemDelegateForColumn(index
				.getColumn());
		return result != null ? result : this.itemDelegate_;
	}

	/**
	 * Sets the row height.
	 * <p>
	 * The view assumes that all rows are of the same height. Use this method to
	 * set the height.
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
	public abstract void setRowHeight(WLength rowHeight);

	/**
	 * Returns the row height.
	 */
	public WLength getRowHeight() {
		return this.rowHeight_;
	}

	/**
	 * Sets the column width.
	 */
	public abstract void setColumnWidth(int column, WLength width);

	/**
	 * Returns the column width.
	 * <p>
	 * 
	 * @see WAbstractItemView#setColumnWidth(int column, WLength width)
	 */
	public abstract WLength getColumnWidth(int column);

	/**
	 * Sets the column border color.
	 * <p>
	 * The default border color is white.
	 */
	public abstract void setColumnBorder(WColor color);

	/**
	 * Creates a new item view.
	 */
	protected WAbstractItemView(WContainerWidget parent) {
		super(parent);
		this.model_ = null;
		this.itemDelegate_ = new WItemDelegate(this);
		this.selectionModel_ = new WItemSelectionModel(
				(WAbstractItemModel) null, this);
		this.rowHeight_ = new WLength(20);
		this.headerLineHeight_ = new WLength(20);
		this.selectionMode_ = SelectionMode.NoSelection;
		this.sorting_ = true;
		this.columnResize_ = true;
		this.multiLineHeader_ = false;
		this.columns_ = new ArrayList<WAbstractItemView.ColumnInfo>();
		this.nextColumnId_ = 1;
		this.currentSortColumn_ = -1;
		this.dragEnabled_ = false;
		this.dropsEnabled_ = false;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.resizeHandleMDownJS_ = new JSlot(this);
		this.resizeHandleMMovedJS_ = new JSlot(this);
		this.resizeHandleMUpJS_ = new JSlot(this);
		this.tieContentsHeaderScrollJS_ = new JSlot(this);
		this.tieRowsScrollJS_ = new JSlot(this);
		this.itemClickedJS_ = new JSlot(this);
		this.itemDoubleClickedJS_ = new JSlot(this);
		this.itemMouseDownJS_ = new JSlot(this);
		this.clicked_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.doubleClicked_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.mouseWentDown_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.selectionChanged_ = new Signal(this);
		this.clickedForSortMapper_ = new WSignalMapper1<Integer>(this);
		this.clickedForSortMapper_.mapped().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WAbstractItemView.this.toggleSortColumn(e1);
					}
				});
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
	 * Handle a drop event (drag &amp; drop).
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
	 * {@link WAbstractItemView#setHeaderHeight(WLength height, boolean multiLine)
	 * setHeaderHeight()}.
	 * <p>
	 * The widget is created only once, but this method may be called repeatedly
	 * for a column for which prior calls returned <code>null</code> (i.e. each
	 * time the header is rerendered).
	 * <p>
	 * The default implementation returns <code>null</code>.
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderHeight(WLength height, boolean multiLine)
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
	 * Sets the header height.
	 * <p>
	 * Use this method to change the header height. You may also enable the use
	 * of multi-line headers. By default, the header text is a single line, that
	 * is centered vertically.
	 * <p>
	 * The default value is 20 pixels.
	 */
	protected abstract void setHeaderHeight(WLength height, boolean multiLine);

	/**
	 * Sets the header height.
	 * <p>
	 * Calls {@link #setHeaderHeight(WLength height, boolean multiLine)
	 * setHeaderHeight(height, false)}
	 */
	protected final void setHeaderHeight(WLength height) {
		setHeaderHeight(height, false);
	}

	/**
	 * Returns the header height.
	 * <p>
	 * 
	 * @see WAbstractItemView#setHeaderHeight(WLength height, boolean multiLine)
	 */
	protected WLength getHeaderHeight() {
		return this.headerLineHeight_;
	}

	static class ColumnInfo {
		public WCssTemplateRule styleRule;
		public int id;
		public SortOrder sortOrder;
		public AlignmentFlag alignment;
		public AlignmentFlag headerAlignment;
		public WLength width;
		public WWidget extraHeaderWidget;
		public boolean sorting;
		public WAbstractItemDelegate itemDelegate_;

		public String getStyleClass() {
			return "Wt-tv-c" + String.valueOf(this.id);
		}

		public ColumnInfo(WAbstractItemView view, int anId, int column) {
			this.id = anId;
			this.sortOrder = SortOrder.AscendingOrder;
			this.alignment = AlignmentFlag.AlignLeft;
			this.headerAlignment = AlignmentFlag.AlignLeft;
			this.width = new WLength();
			this.extraHeaderWidget = null;
			this.sorting = view.sorting_;
			this.itemDelegate_ = null;
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

	WAbstractItemModel model_;
	WAbstractItemDelegate itemDelegate_;
	WItemSelectionModel selectionModel_;
	WLength rowHeight_;
	WLength headerLineHeight_;
	SelectionMode selectionMode_;
	boolean sorting_;
	boolean columnResize_;
	boolean multiLineHeader_;
	List<WAbstractItemView.ColumnInfo> columns_;
	int nextColumnId_;
	int currentSortColumn_;
	boolean dragEnabled_;
	boolean dropsEnabled_;
	WWidget dragWidget_;
	WAbstractItemView.RenderState renderState_;
	boolean needDefineJS_;
	WSignalMapper1<Integer> clickedForSortMapper_;
	WSignalMapper1<WModelIndex> clickedMapper_;
	WSignalMapper1<Integer> clickedForExpandMapper_;
	WSignalMapper1<Integer> clickedForCollapseMapper_;
	List<AbstractSignal.Connection> modelConnections_;
	JSlot resizeHandleMDownJS_;
	JSlot resizeHandleMMovedJS_;
	JSlot resizeHandleMUpJS_;
	JSlot tieContentsHeaderScrollJS_;
	JSlot tieRowsScrollJS_;
	JSlot itemClickedJS_;
	JSlot itemDoubleClickedJS_;
	JSlot itemMouseDownJS_;

	WWidget createHeaderWidget(WApplication app, int column) {
		int headerLevel = this.model_ != null ? (int) StringUtils
				.asNumber(this.model_.getHeaderData(column,
						Orientation.Horizontal, ItemDataRole.LevelRole)) : 0;
		int rightBorderLevel = headerLevel;
		if (this.model_ != null && column + 1 < this.getColumnCount()) {
			EnumSet<HeaderFlag> flagsLeft = this.model_.getHeaderFlags(column);
			EnumSet<HeaderFlag> flagsRight = this.model_
					.getHeaderFlags(column + 1);
			int rightHeaderLevel = (int) StringUtils.asNumber(this.model_
					.getHeaderData(column + 1, Orientation.Horizontal,
							ItemDataRole.LevelRole));
			if (!EnumUtils.mask(flagsLeft, HeaderFlag.ColumnIsExpandedRight)
					.isEmpty()) {
				rightBorderLevel = headerLevel + 1;
			} else {
				if (!EnumUtils
						.mask(flagsRight, HeaderFlag.ColumnIsExpandedLeft)
						.isEmpty()) {
					rightBorderLevel = rightHeaderLevel + 1;
				} else {
					rightBorderLevel = Math.min(headerLevel, rightHeaderLevel);
				}
			}
		}
		WAbstractItemView.ColumnInfo info = this.columnInfo(column);
		WContainerWidget w = new WContainerWidget();
		w.setObjectName("contents");
		if (info.sorting) {
			WText sortIcon = new WText(w);
			sortIcon.setObjectName("sort");
			sortIcon.setInline(false);
			if (!this.columnResize_) {
				sortIcon.setMargin(new WLength(4), EnumSet.of(Side.Right));
			}
			sortIcon.setStyleClass("Wt-tv-sh Wt-tv-sh-none");
			this.clickedForSortMapper_.mapConnect(sortIcon.clicked(), info.id);
		}
		if (!EnumUtils.mask(
				this.model_.getHeaderFlags(column),
				EnumSet.of(HeaderFlag.ColumnIsExpandedLeft,
						HeaderFlag.ColumnIsExpandedRight)).isEmpty()) {
			WImage collapseIcon = new WImage(w);
			collapseIcon.setFloatSide(Side.Left);
			collapseIcon.setImageRef(WApplication.getResourcesUrl()
					+ "minus.gif");
			this.clickedForCollapseMapper_.mapConnect(collapseIcon.clicked(),
					info.id);
		} else {
			if (!EnumUtils.mask(this.model_.getHeaderFlags(column),
					HeaderFlag.ColumnIsCollapsed).isEmpty()) {
				WImage expandIcon = new WImage(w);
				expandIcon.setFloatSide(Side.Left);
				expandIcon.setImageRef(WApplication.getResourcesUrl()
						+ "plus.gif");
				this.clickedForExpandMapper_.mapConnect(expandIcon.clicked(),
						info.id);
			}
		}
		WText t = new WText("&nbsp;", w);
		t.setObjectName("text");
		t.setStyleClass("Wt-label");
		t.setInline(false);
		if (this.multiLineHeader_ || app.getEnvironment().agentIsIE()) {
			t.setWordWrap(true);
		} else {
			t.setWordWrap(false);
		}
		WContainerWidget result = new WContainerWidget();
		if (headerLevel != 0) {
			WContainerWidget spacer = new WContainerWidget(result);
			t = new WText(spacer);
			t.setInline(false);
			if (rightBorderLevel < headerLevel) {
				if (rightBorderLevel != 0) {
					t.setText(repeat(OneLine, rightBorderLevel));
					spacer = new WContainerWidget(result);
					t = new WText(spacer);
					t.setInline(false);
				}
				t.setText(repeat(OneLine, headerLevel - rightBorderLevel));
				spacer.setStyleClass("Wt-tv-br");
			} else {
				t.setText(repeat(OneLine, headerLevel));
			}
		}
		w.setStyleClass(w.getStyleClass()
				+ (rightBorderLevel <= headerLevel ? " Wt-tv-br" : ""));
		result.addWidget(w);
		result.setStyleClass("Wt-tv-c headerrh " + info.getStyleClass());
		result.setContentAlignment(info.headerAlignment);
		WWidget extraW = this.columnInfo(column).extraHeaderWidget;
		if (extraW != null) {
			result.addWidget(extraW);
			extraW.setStyleClass(extraW.getStyleClass() + " Wt-tv-br");
		}
		if (this.columnResize_) {
			WContainerWidget resizeHandle = new WContainerWidget();
			resizeHandle.setStyleClass("Wt-tv-rh headerrh");
			resizeHandle.mouseWentDown().addListener(this.resizeHandleMDownJS_);
			resizeHandle.mouseWentUp().addListener(this.resizeHandleMUpJS_);
			resizeHandle.mouseMoved().addListener(this.resizeHandleMMovedJS_);
			boolean ie = WApplication.getInstance().getEnvironment()
					.agentIsIE();
			WContainerWidget parent = ie ? w
					: ((result.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (result
							.getWidget(0))
							: null);
			parent.insertWidget(0, resizeHandle);
			if (ie) {
				parent.setAttributeValue("style", "zoom: 1");
				parent.resize(WLength.Auto, this.headerLineHeight_);
			}
		}
		WText spacer = new WText();
		spacer.setInline(false);
		spacer.setStyleClass("Wt-tv-br headerrh");
		result.addWidget(spacer);
		return result;
	}

	abstract WWidget headerWidget(int column, boolean contentsOnly);

	final WWidget headerWidget(int column) {
		return headerWidget(column, true);
	}

	WText headerTextWidget(int column) {
		return ((this.headerWidget(column).find("text")) instanceof WText ? (WText) (this
				.headerWidget(column).find("text"))
				: null);
	}

	WText headerSortIconWidget(int column) {
		if (!this.columnInfo(column).sorting) {
			return null;
		}
		return ((this.headerWidget(column).find("sort")) instanceof WText ? (WText) (this
				.headerWidget(column).find("sort"))
				: null);
	}

	Signal2<WModelIndex, WMouseEvent> clicked_;
	Signal2<WModelIndex, WMouseEvent> doubleClicked_;
	Signal2<WModelIndex, WMouseEvent> mouseWentDown_;
	Signal selectionChanged_;

	void clearSelection() {
		SortedSet<WModelIndex> nodes = this.selectionModel_.selection_;
		while (!nodes.isEmpty()) {
			WModelIndex i = nodes.iterator().next();
			this.internalSelect(i, SelectionFlag.Deselect);
		}
	}

	abstract boolean internalSelect(WModelIndex index, SelectionFlag option);

	void checkDragSelection() {
		String dragMimeType = this.model_.getMimeType();
		if (dragMimeType.length() != 0) {
			SortedSet<WModelIndex> selection = this.selectionModel_
					.getSelectedIndexes();
			boolean dragOk = !selection.isEmpty();
			for (Iterator<WModelIndex> i_it = selection.iterator(); i_it
					.hasNext();) {
				WModelIndex i = i_it.next();
				if (!!EnumUtils.mask(i.getFlags(), ItemFlag.ItemIsDragEnabled)
						.isEmpty()) {
					dragOk = false;
					break;
				}
			}
			if (dragOk) {
				this.setAttributeValue("drag", "true");
			} else {
				this.setAttributeValue("drag", "false");
			}
		}
	}

	void configureModelDragDrop() {
		if (!(this.model_ != null)) {
			return;
		}
		if (this.dragEnabled_) {
			this.setAttributeValue("dmt", this.model_.getMimeType());
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

	abstract WAbstractItemView.ColumnInfo columnInfo(int column);

	// protected WAbstractItemView.ColumnInfo insertColumn(int position) ;
	int columnById(int columnid) {
		for (int i = 0; i < this.getColumnCount(); ++i) {
			if (this.columnInfo(i).id == columnid) {
				return i;
			}
		}
		return 0;
	}

	int getColumnCount() {
		return this.columns_.size();
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

	abstract WContainerWidget getHeaderContainer();

	void scheduleRerender(WAbstractItemView.RenderState what) {
		if (!this.isRendered()) {
			return;
		}
		if (what == WAbstractItemView.RenderState.NeedRerenderHeader
				&& this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData
				|| what == WAbstractItemView.RenderState.NeedRerenderData
				&& this.renderState_ == WAbstractItemView.RenderState.NeedRerenderHeader) {
			this.renderState_ = WAbstractItemView.RenderState.NeedRerender;
		} else {
			this.renderState_ = EnumUtils.max(what, this.renderState_);
		}
		this.askRerender();
	}

	abstract void modelColumnsInserted(WModelIndex parent, int start, int end);

	abstract void modelColumnsAboutToBeRemoved(WModelIndex parent, int start,
			int end);

	abstract void modelColumnsRemoved(WModelIndex parent, int start, int end);

	abstract void modelRowsInserted(WModelIndex parent, int start, int end);

	abstract void modelRowsAboutToBeRemoved(WModelIndex parent, int start,
			int end);

	abstract void modelRowsRemoved(WModelIndex parent, int start, int end);

	abstract void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight);

	abstract void modelHeaderDataChanged(Orientation orientation, int start,
			int end);

	abstract void modelLayoutAboutToBeChanged();

	abstract void modelLayoutChanged();

	void modelReset() {
		this.setModel(this.model_);
	}

	int getHeaderLevelCount() {
		int result = 0;
		if (this.model_ != null) {
			for (int i = 0; i < this.columns_.size(); ++i) {
				int l = (int) StringUtils.asNumber(this.model_.getHeaderData(i,
						Orientation.Horizontal, ItemDataRole.LevelRole));
				result = Math.max(result, l);
			}
		}
		return result + 1;
	}

	static String OneLine = "<div>&nbsp;</div>";

	static String repeat(String s, int times) {
		String result = "";
		for (int i = 0; i < times; ++i) {
			result += s;
		}
		return result;
	}
}
