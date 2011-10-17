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

/**
 * A view class that displays a model as a tree or tree table.
 * <p>
 * 
 * The view displays data from a {@link WAbstractItemModel} in a tree or tree
 * table. It provides incremental rendering, allowing the display of data models
 * of any size efficiently, without excessive use of client- or serverside
 * resources.
 * <p>
 * The rendering (and editing) of items is handled by a
 * {@link WAbstractItemDelegate}, by default it uses {@link WItemDelegate} which
 * renders data of all predefined roles (see also {@link ItemDataRole}),
 * including text, icons, checkboxes, and tooltips.
 * <p>
 * The view may support editing of items, if the model indicates support (see
 * the {@link ItemFlag#ItemIsEditable} flag). You can define triggers that
 * initiate editing of an item using
 * {@link WAbstractItemView#setEditTriggers(EnumSet editTriggers)
 * WAbstractItemView#setEditTriggers()}. The actual editing is provided by the
 * item delegate (you can set an appropriate delegate for one column using
 * {@link WAbstractItemView#setItemDelegateForColumn(int column, WAbstractItemDelegate delegate)
 * WAbstractItemView#setItemDelegateForColumn()}). Using
 * {@link WAbstractItemView#setEditOptions(EnumSet editOptions)
 * WAbstractItemView#setEditOptions()} you can customize if and how the view
 * deals with multiple editors.
 * <p>
 * By default, all but the first columns are given a width of 150px, and the
 * first column takes the remaining size. <b>Note that this may have as
 * consequence that the first column&apos;s size is reduced to 0.</b> Column
 * widths of all columns, including the first column, can be set through the API
 * method {@link WTreeView#setColumnWidth(int column, WLength width)
 * setColumnWidth()}, and also by the user using handles provided in the header.
 * <p>
 * Optionally, the treeview may be configured so that the first column is always
 * visible while scrolling through the other columns, which may be convenient if
 * you wish to display a model with many columns. Use
 * {@link WAbstractItemView#setColumn1Fixed(boolean enable)
 * WAbstractItemView#setColumn1Fixed()} to enable this behaviour.
 * <p>
 * If the model supports sorting (
 * {@link WAbstractItemModel#sort(int column, SortOrder order)
 * WAbstractItemModel#sort()}), such as the {@link WStandardItemModel}, then you
 * can enable sorting buttons in the header, using
 * {@link WAbstractItemView#setSortingEnabled(boolean enabled)
 * WAbstractItemView#setSortingEnabled()}.
 * <p>
 * You can allow selection on row or item level (using
 * {@link WAbstractItemView#setSelectionBehavior(SelectionBehavior behavior)
 * WAbstractItemView#setSelectionBehavior()}), and selection of single or
 * multiple items (using
 * {@link WAbstractItemView#setSelectionMode(SelectionMode mode)
 * WAbstractItemView#setSelectionMode()}), and listen for changes in the
 * selection using the {@link WAbstractItemView#selectionChanged()
 * WAbstractItemView#selectionChanged()} signal.
 * <p>
 * You may enable drag &amp; drop support for this view, whith awareness of the
 * items in the model. When enabling dragging (see
 * {@link WAbstractItemView#setDragEnabled(boolean enable)
 * WAbstractItemView#setDragEnabled()}), the current selection may be dragged,
 * but only when all items in the selection indicate support for dragging
 * (controlled by the {@link ItemFlag#ItemIsDragEnabled ItemIsDragEnabled}
 * flag), and if the model indicates a mime-type (controlled by
 * {@link WAbstractItemModel#getMimeType() WAbstractItemModel#getMimeType()}).
 * Likewise, by enabling support for dropping (see
 * {@link WAbstractItemView#setDropsEnabled(boolean enable)
 * WAbstractItemView#setDropsEnabled()}), the treeview may receive a drop event
 * on a particular item, at least if the item indicates support for drops
 * (controlled by the {@link ItemFlag#ItemIsDropEnabled ItemIsDropEnabled}
 * flag).
 * <p>
 * You may also react to mouse click events on any item, by connecting to one of
 * the {@link WAbstractItemView#clicked() WAbstractItemView#clicked()} or
 * {@link WAbstractItemView#doubleClicked() WAbstractItemView#doubleClicked()}
 * signals.
 * <p>
 * <h3>Graceful degradation</h3>
 * <p>
 * The view provides a virtual scrolling behavior which relies on Ajax
 * availability. When Ajax is not available, a page navigation bar is used
 * instead, see {@link WAbstractItemView#getCreatePageNavigationBar()
 * WAbstractItemView#getCreatePageNavigationBar()}. In that case, the widget
 * needs to be given an explicit height using
 * {@link WTreeView#resize(WLength width, WLength height) resize()} which
 * determines the number of rows that are displayed at a time.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The treeview is styled by the current CSS theme. The look can be overridden
 * using the <code>Wt-treeview</code> CSS class and the following selectors.
 * <p>
 * Selectors that apply to the body <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-treeview .Wt-pagingbar        : paging bar
 * </pre>
 * 
 * </div> Selectors that apply to the header: <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-treeview .Wt-headerdiv         : header (background)
 * .Wt-treeview .Wt-header .Wt-label  : header label
 * .Wt-treeview .Wt-tv-rh             : column resize handle
 * .Wt-treeview .Wt-tv-sh-none        : column sort handle, no sorting
 * .Wt-treeview .Wt-tv-sh-up          : column sort handle, sort up
 * .Wt-treeview .Wt-tv-sh-down        : column sort handle, sort down
 * .Wt-treeview .Wt-tv-br             : header border
 * </pre>
 * 
 * </div> Selectors that apply to the tree decoration: <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-treeview .Wt-trunk          : vertical line, trunk
 * .Wt-treeview .Wt-end            : vertical line, last item
 * .Wt-treeview .Wt-collapse       : collapse icon
 * .Wt-treeview .Wt-expand         : expand icon
 * .Wt-treeview .Wt-noexpand       : leaf icon
 * </pre>
 * 
 * </div> Selectors that apply to the table contents: <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-treeview .Wt-spacer         : spacer for non-loaded content
 * .Wt-treeview .Wt-selected       : selected item
 * .Wt-treeview .Wt-drop-site      : possible drop site
 * </pre>
 * 
 * </div> Selectors that apply to the table borders (which must be 1 pixel wide)
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-treeview .Wt-tv-row .Wt-tv-c      : border-right property of cells
 * .Wt-treeview .Wt-header .Wt-tv-row,
 *   .Wt-treeview .Wt-tv-node .Wt-tv-row : border-left property of cells
 * </pre>
 * 
 * </div>
 * <p>
 * A snapshot of the {@link WTreeView}: <div align="center"> <img
 * src="doc-files//WTreeView-default-1.png" alt="WTreeView example (default)">
 * <p>
 * <strong>WTreeView example (default)</strong>
 * </p>
 * </div> <div align="center"> <img src="doc-files//WTreeView-polished-1.png"
 * alt="WTreeView example (polished)">
 * <p>
 * <strong>WTreeView example (polished)</strong>
 * </p>
 * </div>
 */
public class WTreeView extends WAbstractItemView {
	/**
	 * Creates a new tree view.
	 */
	public WTreeView(WContainerWidget parent) {
		super(parent);
		this.expandedSet_ = new TreeSet<WModelIndex>();
		this.renderedNodes_ = new HashMap<WModelIndex, WTreeViewNode>();
		this.renderedNodesAdded_ = false;
		this.rootNode_ = null;
		this.borderColorRule_ = null;
		this.rootIsDecorated_ = true;
		this.collapsed_ = new Signal1<WModelIndex>(this);
		this.expanded_ = new Signal1<WModelIndex>(this);
		this.viewportTop_ = 0;
		this.viewportHeight_ = 30;
		this.nodeLoad_ = 0;
		this.headerContainer_ = null;
		this.contentsContainer_ = null;
		this.scrollBarC_ = null;
		this.itemEvent_ = new JSignal6<String, Integer, String, String, String, WMouseEvent>(
				this.impl_, "itemEvent") {
		};
		this.tieRowsScrollJS_ = new JSlot();
		this.setSelectable(false);
		this.expandConfig_ = new ToggleButtonConfig(this);
		this.expandConfig_.addState("Wt-expand");
		this.expandConfig_.addState("Wt-collapse");
		this.expandConfig_.generate();
		this.setStyleClass("Wt-itemview Wt-treeview");
		String CSS_RULES_NAME = "Wt::WTreeView";
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().agentIsWebKit()
				|| app.getEnvironment().agentIsOpera()) {
			if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
				this.addCssRule(".Wt-treeview .Wt-tv-rowc",
						"position: relative;", CSS_RULES_NAME);
			}
		}
		this.setColumnBorder(WColor.white);
		this.addCssRule("#" + this.getId() + " .cwidth", "");
		this.rowHeightRule_ = new WCssTemplateRule("#" + this.getId() + " .rh",
				this);
		app.getStyleSheet().addRule(this.rowHeightRule_);
		this.rowWidthRule_ = new WCssTemplateRule("#" + this.getId()
				+ " .Wt-tv-row", this);
		app.getStyleSheet().addRule(this.rowWidthRule_);
		this.rowContentsWidthRule_ = new WCssTemplateRule("#" + this.getId()
				+ " .Wt-tv-rowc", this);
		app.getStyleSheet().addRule(this.rowContentsWidthRule_);
		app.addAutoJavaScript("{var obj = $('#" + this.getId()
				+ "').data('obj');if (obj) obj.autoJavaScript();}");
		if (parent != null) {
			parent.addWidget(this);
		}
		this.setup();
	}

	/**
	 * Creates a new tree view.
	 * <p>
	 * Calls {@link #WTreeView(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTreeView() {
		this((WContainerWidget) null);
	}

	public void remove() {
		;
		if (this.rowHeightRule_ != null)
			this.rowHeightRule_.remove();
		this.impl_.clear();
		super.remove();
	}

	/**
	 * Expands or collapses a node.
	 * <p>
	 * 
	 * @see WTreeView#expand(WModelIndex index)
	 * @see WTreeView#collapse(WModelIndex index)
	 */
	public void setExpanded(WModelIndex index, boolean expanded) {
		if (this.isExpanded(index) != expanded) {
			WWidget w = this.widgetForIndex(index);
			WTreeViewNode node = w != null ? ((w) instanceof WTreeViewNode ? (WTreeViewNode) (w)
					: null)
					: null;
			if (node != null) {
				if (expanded) {
					node.doExpand();
				} else {
					node.doCollapse();
				}
			} else {
				if (expanded) {
					this.expandedSet_.add(index);
				} else {
					this.setCollapsed(index);
				}
				if (w != null) {
					RowSpacer spacer = ((w) instanceof RowSpacer ? (RowSpacer) (w)
							: null);
					int height = this.subTreeHeight(index);
					int diff = this.subTreeHeight(index) - height;
					spacer.setRows(spacer.getRows() + diff);
					spacer.getNode().adjustChildrenHeight(diff);
					this.renderedRowsChanged(this.renderedRow(index, spacer,
							this.getRenderLowerBound(), this
									.getRenderUpperBound()), diff);
				}
			}
		}
	}

	/**
	 * Returns whether a node is expanded.
	 * <p>
	 * 
	 * @see WTreeView#setExpanded(WModelIndex index, boolean expanded)
	 */
	public boolean isExpanded(WModelIndex index) {
		return (index == this.getRootIndex() || (index != null && index
				.equals(this.getRootIndex())))
				|| this.expandedSet_.contains(index) != false;
	}

	/**
	 * Collapses a node.
	 * <p>
	 * 
	 * @see WTreeView#setExpanded(WModelIndex index, boolean expanded)
	 * @see WTreeView#expand(WModelIndex index)
	 */
	public void collapse(WModelIndex index) {
		this.setExpanded(index, false);
	}

	/**
	 * Expands a node.
	 * <p>
	 * 
	 * @see WTreeView#setExpanded(WModelIndex index, boolean expanded)
	 * @see WTreeView#collapse(WModelIndex index)
	 */
	public void expand(WModelIndex index) {
		this.setExpanded(index, true);
	}

	/**
	 * Expands all nodes to a depth.
	 * <p>
	 * Expands all nodes to the given <code>depth</code>. A depth of 1
	 * corresponds to the top level nodes.
	 * <p>
	 * 
	 * @see WTreeView#expand(WModelIndex index)
	 */
	public void expandToDepth(int depth) {
		if (depth > 0) {
			this.expandChildrenToDepth(this.getRootIndex(), depth);
		}
	}

	/**
	 * Sets whether toplevel items are decorated.
	 * <p>
	 * By default, top level nodes have expand/collapse and other lines to
	 * display their linkage and offspring, like any node.
	 * <p>
	 * By setting <code>show</code> to <code>false</code>, you can hide these
	 * decorations for root nodes, and in this way mimic a plain list. You could
	 * also consider using a {@link WTableView} instead.
	 */
	public void setRootIsDecorated(boolean show) {
		this.rootIsDecorated_ = show;
	}

	/**
	 * Returns whether toplevel items are decorated.
	 * <p>
	 * 
	 * @see WTreeView#setRootIsDecorated(boolean show)
	 */
	public boolean isRootDecorated() {
		return this.rootIsDecorated_;
	}

	public void resize(WLength width, WLength height) {
		WApplication app = WApplication.getInstance();
		WLength w = app.getEnvironment().hasAjax() ? WLength.Auto : width;
		this.contentsContainer_.resize(w, WLength.Auto);
		if (this.headerContainer_ != null) {
			this.headerContainer_.resize(w, WLength.Auto);
		}
		if (!height.isAuto()) {
			if (!app.getEnvironment().hasAjax()) {
				if (this.impl_.getCount() < 3) {
					this.impl_.addWidget(this.getCreatePageNavigationBar());
				}
				double navigationBarHeight = 25;
				double headerHeight = this.getHeaderHeight().toPixels();
				this.contentsContainer_.resize(width, new WLength(height
						.toPixels()
						- navigationBarHeight - headerHeight));
				this.viewportHeight_ = (int) (this.contentsContainer_
						.getHeight().toPixels() / this.getRowHeight()
						.toPixels());
			} else {
				this.viewportHeight_ = (int) Math.ceil(height.toPixels()
						/ this.getRowHeight().toPixels());
			}
		} else {
			if (app.getEnvironment().hasAjax()) {
				this.viewportHeight_ = 30;
			}
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
		}
		super.resize(width, height);
	}

	/**
	 * Signal emitted when a node is collapsed.
	 * <p>
	 * 
	 * @see WTreeView#setExpanded(WModelIndex index, boolean expanded)
	 * @see WTreeView#expanded()
	 */
	public Signal1<WModelIndex> collapsed() {
		return this.collapsed_;
	}

	/**
	 * Signal emitted when a node is expanded.
	 * <p>
	 * 
	 * @see WTreeView#setExpanded(WModelIndex index, boolean expanded)
	 * @see WTreeView#collapsed()
	 */
	public Signal1<WModelIndex> expanded() {
		return this.expanded_;
	}

	public void setModel(WAbstractItemModel model) {
		super.setModel(model);
		this.modelConnections_.add(model.columnsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelColumnsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.columnsAboutToBeRemoved().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelColumnsAboutToBeRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.columnsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelColumnsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.rowsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelRowsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.rowsAboutToBeRemoved().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelRowsAboutToBeRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.rowsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelRowsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.dataChanged().addListener(this,
				new Signal2.Listener<WModelIndex, WModelIndex>() {
					public void trigger(WModelIndex e1, WModelIndex e2) {
						WTreeView.this.modelDataChanged(e1, e2);
					}
				}));
		this.modelConnections_.add(model.headerDataChanged().addListener(this,
				new Signal3.Listener<Orientation, Integer, Integer>() {
					public void trigger(Orientation e1, Integer e2, Integer e3) {
						WTreeView.this.modelHeaderDataChanged(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.layoutAboutToBeChanged().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						WTreeView.this.modelLayoutAboutToBeChanged();
					}
				}));
		this.modelConnections_.add(model.layoutChanged().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WTreeView.this.modelLayoutChanged();
					}
				}));
		this.modelConnections_.add(model.modelReset().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WTreeView.this.modelReset();
					}
				}));
		this.expandedSet_.clear();
		while ((int) this.columns_.size() > model.getColumnCount()) {
			if (this.columns_.get(this.columns_.size() - 1).styleRule != null)
				this.columns_.get(this.columns_.size() - 1).styleRule.remove();
			this.columns_.remove(0 + this.columns_.size() - 1);
		}
		this.pageChanged().trigger();
	}

	/**
	 * Sets the column width.
	 * <p>
	 * For a model with
	 * {@link WAbstractItemModel#getColumnCount(WModelIndex parent)
	 * columnCount()} == <code>N</code>, the initial width of columns 1..
	 * <code>N</code> is set to 150 pixels, and column 0 will take all remaining
	 * space.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The actual space occupied by each column is the column
	 * width augmented by 7 pixels for internal padding and a border.</i>
	 * </p>
	 * 
	 * @see WTreeView#setRowHeight(WLength rowHeight)
	 */
	public void setColumnWidth(int column, WLength width) {
		this.columnInfo(column).width = width;
		WWidget toResize = this.columnInfo(column).styleRule
				.getTemplateWidget();
		toResize.resize(new WLength(0), WLength.Auto);
		toResize.resize(new WLength(width.toPixels()), WLength.Auto);
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().hasAjax()
				&& this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
						.getValue()) {
			app.doJavaScript("$('#" + this.getId()
					+ "').data('obj').adjustColumns();");
		}
		if (!app.getEnvironment().hasAjax() && column == 0 && !width.isAuto()) {
			double total = 0;
			for (int i = 0; i < this.getColumnCount(); ++i) {
				if (!this.columnInfo(i).hidden) {
					total += this.getColumnWidth(i).toPixels();
				}
			}
			this.resize(new WLength(total), this.getHeight());
		}
	}

	public void setAlternatingRowColors(boolean enable) {
		super.setAlternatingRowColors(enable);
		this.setRootNodeStyle();
	}

	public void setRowHeight(WLength rowHeight) {
		super.setRowHeight(rowHeight);
		this.rowHeightRule_.getTemplateWidget().resize(WLength.Auto, rowHeight);
		this.rowHeightRule_.getTemplateWidget().setLineHeight(rowHeight);
		if (!WApplication.getInstance().getEnvironment().hasAjax()
				&& !this.getHeight().isAuto()) {
			this.viewportHeight_ = (int) (this.contentsContainer_.getHeight()
					.toPixels() / rowHeight.toPixels());
		}
		this.setRootNodeStyle();
		for (Iterator<Map.Entry<WModelIndex, WTreeViewNode>> i_it = this.renderedNodes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WTreeViewNode> i = i_it.next();
			i.getValue().rerenderSpacers();
		}
		if (this.rootNode_ != null) {
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
		}
	}

	public void setHeaderHeight(WLength height) {
		super.setHeaderHeight(height);
	}

	public void setColumnBorder(WColor color) {
		if (this.borderColorRule_ != null)
			this.borderColorRule_.remove();
		this.borderColorRule_ = new WCssTextRule(
				".Wt-treeview .Wt-tv-br, .Wt-treeview .header .Wt-tv-row, .Wt-treeview .Wt-tv-node .Wt-tv-row .Wt-tv-c, .Wt-treeview .Wt-tv-node .Wt-tv-row",
				"border-color: " + color.getCssText(), this);
		WApplication.getInstance().getStyleSheet().addRule(
				this.borderColorRule_);
	}

	public void setColumnHidden(int column, boolean hidden) {
		if (this.columnInfo(column).hidden != hidden) {
			super.setColumnHidden(column, hidden);
			WWidget toHide = this.columnInfo(column).styleRule
					.getTemplateWidget();
			toHide.setHidden(hidden);
			this.setColumnWidth(column, this.getColumnWidth(column));
		}
	}

	public void setRowHeaderCount(int count) {
		WApplication app = WApplication.getInstance();
		if (!app.getEnvironment().hasAjax()) {
			return;
		}
		int oldCount = this.getRowHeaderCount();
		if (count != 0 && count != 1) {
			throw new UnsupportedOperationException(
					"WTreeView::setRowHeaderCount: count must be 0 or 1");
		}
		super.setRowHeaderCount(count);
		if (count != 0 && !(oldCount != 0)) {
			this.addStyleClass("column1");
			WContainerWidget rootWrap = ((this.contents_.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (this.contents_
					.getWidget(0))
					: null);
			rootWrap.resize(new WLength(100, WLength.Unit.Percentage),
					WLength.Auto);
			rootWrap.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			this.contents_.setPositionScheme(PositionScheme.Relative);
			rootWrap.setPositionScheme(PositionScheme.Absolute);
			boolean useStyleLeft = app.getEnvironment().agentIsWebKit()
					|| app.getEnvironment().agentIsOpera();
			if (useStyleLeft) {
				boolean rtl = app.getLayoutDirection() == LayoutDirection.RightToLeft;
				this.tieRowsScrollJS_
						.setJavaScript("function(obj, event) {Wt3_1_11.getCssRule('#"
								+ this.getId()
								+ " .Wt-tv-rowc').style.left= -obj.scrollLeft "
								+ (rtl ? "+ (obj.firstChild.offsetWidth - obj.offsetWidth)"
										: "") + "+ 'px';}");
			} else {
				this.tieRowsScrollJS_
						.setJavaScript("function(obj, event) {$('#"
								+ this.getId()
								+ " .Wt-tv-rowc').parent().scrollLeft(obj.scrollLeft);}");
			}
			WContainerWidget scrollBarContainer = new WContainerWidget();
			scrollBarContainer.setStyleClass("cwidth");
			scrollBarContainer.resize(WLength.Auto, new WLength(22));
			this.scrollBarC_ = new WContainerWidget(scrollBarContainer);
			this.scrollBarC_.setStyleClass("Wt-tv-row Wt-scroll");
			this.scrollBarC_.scrolled().addListener(this.tieRowsScrollJS_);
			if (app.getEnvironment().agentIsIE()) {
				scrollBarContainer.setPositionScheme(PositionScheme.Relative);
				boolean rtl = app.getLayoutDirection() == LayoutDirection.RightToLeft;
				this.scrollBarC_.setAttributeValue("style", rtl ? "left:"
						: "right:" + "0px");
			}
			WContainerWidget scrollBar = new WContainerWidget(this.scrollBarC_);
			scrollBar.setStyleClass("Wt-tv-rowc");
			if (useStyleLeft) {
				scrollBar.setAttributeValue("style", "left: 0px;");
			}
			this.impl_.getLayout().addWidget(scrollBarContainer);
		}
	}

	public int getPageCount() {
		if (this.rootNode_ != null) {
			return (this.rootNode_.getRenderedHeight() - 1)
					/ this.viewportHeight_ + 1;
		} else {
			return 1;
		}
	}

	public int getPageSize() {
		return this.viewportHeight_;
	}

	public int getCurrentPage() {
		return this.viewportTop_ / this.viewportHeight_;
	}

	public void setCurrentPage(int page) {
		this.viewportTop_ = page * this.viewportHeight_;
		this.contents_.setOffsets(new WLength(-this.viewportTop_
				* this.getRowHeight().toPixels()), EnumSet.of(Side.Top));
		this.pageChanged().trigger();
		this.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
	}

	public void scrollTo(WModelIndex index, WAbstractItemView.ScrollHint hint) {
		int row = this.getIndexRow(index, this.getRootIndex(), 0,
				Integer.MAX_VALUE) + 1;
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().hasAjax()) {
			if (this.viewportHeight_ != 30) {
				if (hint == WAbstractItemView.ScrollHint.EnsureVisible) {
					if (this.viewportTop_ + this.viewportHeight_ < row) {
						hint = WAbstractItemView.ScrollHint.PositionAtTop;
					} else {
						if (row < this.viewportTop_) {
							hint = WAbstractItemView.ScrollHint.PositionAtBottom;
						}
					}
				}
				switch (hint) {
				case PositionAtTop:
					this.viewportTop_ = row;
					break;
				case PositionAtBottom:
					this.viewportTop_ = row - this.viewportHeight_ + 1;
					break;
				case PositionAtCenter:
					this.viewportTop_ = row - this.viewportHeight_ / 2 + 1;
					break;
				default:
					break;
				}
				if (hint != WAbstractItemView.ScrollHint.EnsureVisible) {
					this
							.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
				}
			}
			StringBuilder s = new StringBuilder();
			s.append("jQuery.data(").append(this.getJsRef()).append(
					", 'obj').scrollTo(-1, ").append(row).append(",").append(
					(int) this.getRowHeight().toPixels()).append(",").append(
					hint.getValue()).append(");");
			this.doJavaScript(s.toString());
		} else {
			this.setCurrentPage(row / this.getPageSize());
		}
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			if (!this.itemEvent_.isConnected()) {
				this.itemEvent_
						.addListener(
								this,
								new Signal6.Listener<String, Integer, String, String, String, WMouseEvent>() {
									public void trigger(String e1, Integer e2,
											String e3, String e4, String e5,
											WMouseEvent e6) {
										WTreeView.this.onItemEvent(e1, e2, e3,
												e4, e5, e6);
									}
								});
			}
			this.defineJavaScript();
		}
		while (this.renderState_ != WAbstractItemView.RenderState.RenderOk) {
			WAbstractItemView.RenderState s = this.renderState_;
			this.renderState_ = WAbstractItemView.RenderState.RenderOk;
			switch (s) {
			case NeedRerender:
				this.rerenderHeader();
				this.rerenderTree();
				break;
			case NeedRerenderHeader:
				this.rerenderHeader();
				break;
			case NeedRerenderData:
				this.rerenderTree();
				break;
			case NeedAdjustViewPort:
				this.adjustToViewport();
				break;
			default:
				break;
			}
		}
		if (this.getRowHeaderCount() != 0 && this.renderedNodesAdded_) {
			WApplication.getInstance().doJavaScript(
					"{var s=" + this.scrollBarC_.getJsRef() + ";if (s) {"
							+ this.tieRowsScrollJS_.execJs("s") + "}}");
			this.renderedNodesAdded_ = false;
		}
		super.render(flags);
	}

	protected void enableAjax() {
		this.setup();
		this.defineJavaScript();
		this.rerenderHeader();
		this.rerenderTree();
		super.enableAjax();
	}

	SortedSet<WModelIndex> expandedSet_;
	private HashMap<WModelIndex, WTreeViewNode> renderedNodes_;
	private boolean renderedNodesAdded_;
	private WTreeViewNode rootNode_;
	private WCssTemplateRule rowHeightRule_;
	private WCssTemplateRule rowWidthRule_;
	private WCssTemplateRule rowContentsWidthRule_;
	private WCssTemplateRule c0WidthRule_;
	private WCssRule borderColorRule_;
	private boolean rootIsDecorated_;
	boolean column1Fixed_;
	Signal1<WModelIndex> collapsed_;
	Signal1<WModelIndex> expanded_;
	private int viewportTop_;
	private int viewportHeight_;
	private int firstRenderedRow_;
	private int validRowCount_;
	private int nodeLoad_;
	private WContainerWidget headers_;
	private WContainerWidget headerContainer_;
	private WContainerWidget contents_;
	private WContainerWidget contentsContainer_;
	private WContainerWidget scrollBarC_;
	private int firstRemovedRow_;
	private int removedHeight_;
	private JSignal6<String, Integer, String, String, String, WMouseEvent> itemEvent_;
	ToggleButtonConfig expandConfig_;
	private JSlot tieRowsScrollJS_;

	WAbstractItemView.ColumnInfo createColumnInfo(int column) {
		WAbstractItemView.ColumnInfo ci = super.createColumnInfo(column);
		if (column == 0) {
			ci.width = WLength.Auto;
			ci.styleRule.getTemplateWidget().resize(WLength.Auto, WLength.Auto);
			(this).addCssRule("#" + this.getId() + " .Wt-tv-node ."
					+ ci.getStyleClass(),
					"width: auto;text-overflow: ellipsis;overflow: hidden");
		}
		return ci;
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		if (!app.getEnvironment().hasAjax()) {
			return;
		}
		app.loadJavaScript("js/WTreeView.js", wtjs1());
		app.doJavaScript("new Wt3_1_11.WTreeView(" + app.getJavaScriptClass()
				+ "," + this.getJsRef() + ","
				+ this.contentsContainer_.getJsRef() + ","
				+ this.headerContainer_.getJsRef() + ","
				+ String.valueOf(this.getRowHeaderCount()) + ");");
	}

	private void rerenderHeader() {
		WApplication app = WApplication.getInstance();
		for (int i = 0; i < this.getColumnCount(); ++i) {
			WWidget w = this.columnInfo(i).extraHeaderWidget;
			if (!(w != null)) {
				this.columnInfo(i).extraHeaderWidget = this
						.createExtraHeaderWidget(i);
			} else {
				(((w.getParent()) instanceof WContainerWidget ? (WContainerWidget) (w
						.getParent())
						: null)).removeWidget(w);
			}
		}
		this.headers_.clear();
		WContainerWidget row = new WContainerWidget(this.headers_);
		row.setFloatSide(Side.Right);
		if (this.getRowHeaderCount() != 0) {
			row.setStyleClass("Wt-tv-row headerrh background");
			row = new WContainerWidget(row);
			row.setStyleClass("Wt-tv-rowc headerrh");
		} else {
			row.setStyleClass("Wt-tv-row");
		}
		for (int i = 0; i < this.getColumnCount(); ++i) {
			WWidget w = this.createHeaderWidget(app, i);
			if (i != 0) {
				w.setFloatSide(Side.Left);
				row.addWidget(w);
			} else {
				this.headers_.addWidget(w);
			}
		}
		if (app.getEnvironment().hasAjax()) {
			app.doJavaScript("$('#" + this.getId()
					+ "').data('obj').adjustColumns();");
		}
	}

	private void rerenderTree() {
		WContainerWidget wrapRoot = ((this.contents_.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (this.contents_
				.getWidget(0))
				: null);
		wrapRoot.clear();
		this.firstRenderedRow_ = this.getCalcOptimalFirstRenderedRow();
		this.validRowCount_ = 0;
		this.rootNode_ = new WTreeViewNode(this, this.getRootIndex(), -1, true,
				(WTreeViewNode) null);
		this.rootNode_.resize(new WLength(100, WLength.Unit.Percentage),
				new WLength(1));
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			this.connectObjJS(this.rootNode_.clicked(), "click");
			this.connectObjJS(this.rootNode_.doubleClicked(), "dblClick");
			if (this.mouseWentDown().isConnected() || this.dragEnabled_) {
				this.connectObjJS(this.rootNode_.mouseWentDown(), "mouseDown");
			}
			if (this.mouseWentUp().isConnected()) {
				this.connectObjJS(this.rootNode_.mouseWentUp(), "mouseUp");
			}
		}
		this.setRootNodeStyle();
		wrapRoot.addWidget(this.rootNode_);
		this.pageChanged().trigger();
		this.adjustToViewport();
	}

	private void setup() {
		WApplication app = WApplication.getInstance();
		this.impl_.clear();
		this.rootNode_ = null;
		this.headers_ = new WContainerWidget();
		this.headers_.setStyleClass("Wt-headerdiv headerrh");
		this.contents_ = new WContainerWidget();
		WContainerWidget wrapRoot = new WContainerWidget();
		this.contents_.addWidget(wrapRoot);
		if (app.getEnvironment().agentIsIE()) {
			wrapRoot.setAttributeValue("style", "zoom: 1");
			this.contents_.setAttributeValue("style", "zoom: 1");
		}
		if (app.getEnvironment().hasAjax()) {
			this.impl_.setPositionScheme(PositionScheme.Relative);
			WVBoxLayout layout = new WVBoxLayout();
			layout.setSpacing(0);
			layout.setContentsMargins(0, 0, 0, 0);
			this.headerContainer_ = new WContainerWidget();
			this.headerContainer_
					.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			this.headerContainer_.setStyleClass("Wt-header headerrh cwidth");
			this.headerContainer_.addWidget(this.headers_);
			this.contentsContainer_ = new WContainerWidget();
			this.contentsContainer_.setStyleClass("cwidth");
			this.contentsContainer_
					.setOverflow(WContainerWidget.Overflow.OverflowAuto);
			this.contentsContainer_.scrolled().addListener(this,
					new Signal1.Listener<WScrollEvent>() {
						public void trigger(WScrollEvent e1) {
							WTreeView.this.onViewportChange(e1);
						}
					});
			this.contentsContainer_
					.scrolled()
					.addListener(
							"function(obj, event) {if (obj.sb) return;obj.sb = true;"
									+ this.headerContainer_.getJsRef()
									+ ".scrollLeft=obj.scrollLeft;var t = "
									+ this.contents_.getJsRef()
									+ ".firstChild;var h = "
									+ this.headers_.getJsRef()
									+ ";h.style.width = (t.offsetWidth - 1) + 'px';h.style.width = t.offsetWidth + 'px';obj.sb = false;}");
			this.contentsContainer_.addWidget(this.contents_);
			layout.addWidget(this.headerContainer_);
			layout.addWidget(this.contentsContainer_, 1);
			this.impl_.setLayout(layout);
		} else {
			this.contentsContainer_ = new WContainerWidget();
			this.contentsContainer_.addWidget(this.contents_);
			this.contentsContainer_
					.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			this.impl_.setPositionScheme(PositionScheme.Relative);
			this.contentsContainer_.setPositionScheme(PositionScheme.Relative);
			this.contents_.setPositionScheme(PositionScheme.Relative);
			this.impl_.addWidget(this.headers_);
			this.impl_.addWidget(this.contentsContainer_);
			this.viewportHeight_ = 1000;
			this.resize(this.getWidth(), this.getHeight());
		}
		this.setRowHeight(this.getRowHeight());
	}

	void scheduleRerender(WAbstractItemView.RenderState what) {
		if (what == WAbstractItemView.RenderState.NeedRerender
				|| what == WAbstractItemView.RenderState.NeedRerenderData) {
			if (this.rootNode_ != null)
				this.rootNode_.remove();
			this.rootNode_ = null;
		}
		super.scheduleRerender(what);
	}

	private void modelColumnsInserted(WModelIndex parent, int start, int end) {
		int count = end - start + 1;
		if (!(parent != null)) {
			WApplication app = WApplication.getInstance();
			for (int i = start; i < start + count; ++i) {
				this.columns_.add(0 + i, this.createColumnInfo(i));
			}
			if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
					.getValue()) {
				if (start == 0) {
					this
							.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
				} else {
					if (app.getEnvironment().hasAjax()) {
						app.doJavaScript("$('#" + this.getId()
								+ "').data('obj').adjustColumns();");
					}
					WContainerWidget row = this.getHeaderRow();
					for (int i = start; i < start + count; ++i) {
						WWidget w = this.createHeaderWidget(app, i);
						w.setFloatSide(Side.Left);
						row.insertWidget(i - 1, w);
					}
				}
			}
		}
		if (this.renderState_ == WAbstractItemView.RenderState.NeedRerender
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData) {
			return;
		}
		if (start == 0) {
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
		} else {
			WTreeViewNode node = this.nodeForIndex(parent);
			if (node != null) {
				for (WTreeViewNode c = node.nextChildNode((WTreeViewNode) null); c != null; c = node
						.nextChildNode(c)) {
					c.insertColumns(start, count);
				}
			}
		}
	}

	private void modelColumnsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		int count = end - start + 1;
		if (!(parent != null)) {
			if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
					.getValue()) {
				WApplication app = WApplication.getInstance();
				if (app.getEnvironment().hasAjax()) {
					app.doJavaScript("$('#" + this.getId()
							+ "').data('obj').adjustColumns();");
				}
			}
			for (int ii = 0; ii < (0 + start + count) - (0 + start); ++ii)
				this.columns_.remove(0 + start);
			;
			if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
					.getValue()) {
				if (start == 0) {
					this
							.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
				} else {
					for (int i = start; i < start + count; ++i) {
						if (this.headerWidget(start, false) != null)
							this.headerWidget(start, false).remove();
					}
				}
			}
		}
		if (start == 0) {
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
		}
	}

	private void modelColumnsRemoved(WModelIndex parent, int start, int end) {
		if (this.renderState_ == WAbstractItemView.RenderState.NeedRerender
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData) {
			return;
		}
		int count = end - start + 1;
		if (start != 0) {
			WTreeViewNode node = this.nodeForIndex(parent);
			if (node != null) {
				for (WTreeViewNode c = node.nextChildNode((WTreeViewNode) null); c != null; c = node
						.nextChildNode(c)) {
					c.removeColumns(start, count);
				}
			}
		}
		if (start <= this.currentSortColumn_ && this.currentSortColumn_ <= end) {
			this.currentSortColumn_ = -1;
		}
	}

	private void modelRowsInserted(WModelIndex parent, int start, int end) {
		int count = end - start + 1;
		this.shiftModelIndexes(parent, start, count);
		if (this.renderState_ == WAbstractItemView.RenderState.NeedRerender
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData) {
			return;
		}
		WWidget parentWidget = this.widgetForIndex(parent);
		if (parentWidget != null) {
			WTreeViewNode parentNode = ((parentWidget) instanceof WTreeViewNode ? (WTreeViewNode) (parentWidget)
					: null);
			if (parentNode != null) {
				if (parentNode.isChildrenLoaded()) {
					WWidget startWidget = null;
					if (end < this.getModel().getRowCount(parent) - 1) {
						startWidget = parentNode.widgetForModelRow(start);
					} else {
						if (parentNode.getBottomSpacerHeight() != 0) {
							startWidget = parentNode.bottomSpacer();
						}
					}
					parentNode.adjustChildrenHeight(count);
					parentNode.shiftModelIndexes(start, count);
					if (startWidget != null
							&& startWidget == parentNode.topSpacer()) {
						parentNode.addTopSpacerHeight(count);
						this.renderedRowsChanged(this.renderedRow(this
								.getModel().getIndex(start, 0, parent),
								parentNode.topSpacer(), this
										.getRenderLowerBound(), this
										.getRenderUpperBound()), count);
					} else {
						if (startWidget != null
								&& startWidget == parentNode.bottomSpacer()) {
							parentNode.addBottomSpacerHeight(count);
							this.renderedRowsChanged(this.renderedRow(this
									.getModel().getIndex(start, 0, parent),
									parentNode.bottomSpacer(), this
											.getRenderLowerBound(), this
											.getRenderUpperBound()), count);
						} else {
							int maxRenderHeight = this.firstRenderedRow_
									+ Math.max(this.validRowCount_,
											this.viewportHeight_)
									- parentNode.renderedRow()
									- parentNode.getTopSpacerHeight();
							int containerIndex = startWidget != null ? parentNode
									.getChildContainer()
									.getIndexOf(startWidget)
									: parentNode.getChildContainer().getCount();
							int parentRowCount = this.getModel().getRowCount(
									parent);
							int nodesToAdd = Math.min(count, maxRenderHeight);
							WTreeViewNode first = null;
							for (int i = 0; i < nodesToAdd; ++i) {
								WTreeViewNode n = new WTreeViewNode(this, this
										.getModel().getIndex(start + i, 0,
												parent), -1,
										start + i == parentRowCount - 1,
										parentNode);
								parentNode.getChildContainer().insertWidget(
										containerIndex + i, n);
								++this.validRowCount_;
								if (!(first != null)) {
									first = n;
								}
							}
							if (nodesToAdd < count) {
								parentNode.addBottomSpacerHeight(count
										- nodesToAdd);
								int targetSize = containerIndex + nodesToAdd
										+ 1;
								int extraBottomSpacer = 0;
								while (parentNode.getChildContainer()
										.getCount() > targetSize) {
									WTreeViewNode n = ((parentNode
											.getChildContainer()
											.getWidget(targetSize - 1)) instanceof WTreeViewNode ? (WTreeViewNode) (parentNode
											.getChildContainer()
											.getWidget(targetSize - 1))
											: null);
									assert n != null;
									extraBottomSpacer += n.getRenderedHeight();
									this.validRowCount_ -= n
											.getRenderedHeight();
									if (n != null)
										n.remove();
								}
								if (extraBottomSpacer != 0) {
									parentNode
											.addBottomSpacerHeight(extraBottomSpacer);
								}
								parentNode.normalizeSpacers();
							}
							if (first != null) {
								this.renderedRowsChanged(first.renderedRow(this
										.getRenderLowerBound(), this
										.getRenderUpperBound()), nodesToAdd);
							}
							if (end == this.getModel().getRowCount(parent) - 1
									&& start >= 1) {
								WTreeViewNode n = ((parentNode
										.widgetForModelRow(start - 1)) instanceof WTreeViewNode ? (WTreeViewNode) (parentNode
										.widgetForModelRow(start - 1))
										: null);
								if (n != null) {
									n.updateGraphics(false,
											this.getModel().getRowCount(
													n.getModelIndex()) == 0);
								}
							}
						}
					}
				}
				if (this.getModel().getRowCount(parent) == count) {
					parentNode.updateGraphics(parentNode.isLast(), false);
				}
			} else {
				RowSpacer s = ((parentWidget) instanceof RowSpacer ? (RowSpacer) (parentWidget)
						: null);
				s.setRows(s.getRows() + count);
				s.getNode().adjustChildrenHeight(count);
				this.renderedRowsChanged(this.renderedRow(this.getModel()
						.getIndex(start, 0, parent), s, this
						.getRenderLowerBound(), this.getRenderUpperBound()),
						count);
			}
		}
	}

	private void modelRowsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		int count = end - start + 1;
		if (this.renderState_ != WAbstractItemView.RenderState.NeedRerender
				|| this.renderState_ != WAbstractItemView.RenderState.NeedRerenderData) {
			this.firstRemovedRow_ = -1;
			this.removedHeight_ = 0;
			WWidget parentWidget = this.widgetForIndex(parent);
			if (parentWidget != null) {
				WTreeViewNode parentNode = ((parentWidget) instanceof WTreeViewNode ? (WTreeViewNode) (parentWidget)
						: null);
				if (parentNode != null) {
					if (parentNode.isChildrenLoaded()) {
						for (int i = end; i >= start; --i) {
							WWidget w = parentNode.widgetForModelRow(i);
							assert w != null;
							RowSpacer s = ((w) instanceof RowSpacer ? (RowSpacer) (w)
									: null);
							if (s != null) {
								WModelIndex childIndex = this.getModel()
										.getIndex(i, 0, parent);
								if (i == start) {
									this.firstRemovedRow_ = this.renderedRow(
											childIndex, w);
								}
								int childHeight = this
										.subTreeHeight(childIndex);
								this.removedHeight_ += childHeight;
								s.setRows(s.getRows() - childHeight);
							} else {
								WTreeViewNode node = ((w) instanceof WTreeViewNode ? (WTreeViewNode) (w)
										: null);
								if (i == start) {
									this.firstRemovedRow_ = node.renderedRow();
								}
								this.removedHeight_ += node.getRenderedHeight();
								if (node != null)
									node.remove();
							}
						}
						parentNode.normalizeSpacers();
						parentNode.adjustChildrenHeight(-this.removedHeight_);
						parentNode.shiftModelIndexes(start, -count);
						if (end == this.getModel().getRowCount(parent) - 1
								&& start >= 1) {
							WTreeViewNode n = ((parentNode
									.widgetForModelRow(start - 1)) instanceof WTreeViewNode ? (WTreeViewNode) (parentNode
									.widgetForModelRow(start - 1))
									: null);
							if (n != null) {
								n.updateGraphics(true, this.getModel()
										.getRowCount(n.getModelIndex()) == 0);
							}
						}
					}
					if (this.getModel().getRowCount(parent) == count) {
						parentNode.updateGraphics(parentNode.isLast(), true);
					}
				} else {
					RowSpacer s = ((parentWidget) instanceof RowSpacer ? (RowSpacer) (parentWidget)
							: null);
					for (int i = start; i <= end; ++i) {
						WModelIndex childIndex = this.getModel().getIndex(i, 0,
								parent);
						int childHeight = this.subTreeHeight(childIndex);
						this.removedHeight_ += childHeight;
						if (i == start) {
							this.firstRemovedRow_ = this.renderedRow(
									childIndex, s);
						}
					}
					WTreeViewNode node = s.getNode();
					s.setRows(s.getRows() - this.removedHeight_);
					node.adjustChildrenHeight(-this.removedHeight_);
				}
			}
		}
		this.shiftModelIndexes(parent, start, -count);
	}

	private void modelRowsRemoved(WModelIndex parent, int start, int end) {
		this.renderedRowsChanged(this.firstRemovedRow_, -this.removedHeight_);
	}

	void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
		if (this.renderState_ == WAbstractItemView.RenderState.NeedRerender
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData) {
			return;
		}
		WModelIndex parent = topLeft.getParent();
		WWidget parentWidget = this.widgetForIndex(parent);
		if (parentWidget != null) {
			WTreeViewNode parentNode = ((parentWidget) instanceof WTreeViewNode ? (WTreeViewNode) (parentWidget)
					: null);
			if (parentNode != null) {
				if (parentNode.isChildrenLoaded()) {
					for (int r = topLeft.getRow(); r <= bottomRight.getRow(); ++r) {
						WModelIndex index = this.getModel().getIndex(r, 0,
								parent);
						WTreeViewNode n = ((this.widgetForIndex(index)) instanceof WTreeViewNode ? (WTreeViewNode) (this
								.widgetForIndex(index))
								: null);
						if (n != null) {
							n.update(topLeft.getColumn(), bottomRight
									.getColumn());
						}
					}
				}
			}
		}
	}

	void modelLayoutAboutToBeChanged() {
		WModelIndex.encodeAsRawIndexes(this.expandedSet_);
		super.modelLayoutAboutToBeChanged();
	}

	void modelLayoutChanged() {
		super.modelLayoutChanged();
		this.expandedSet_ = WModelIndex.decodeFromRawIndexes(this.expandedSet_);
		this.renderedNodes_.clear();
		this.pageChanged().trigger();
	}

	private void onViewportChange(WScrollEvent e) {
		this.viewportTop_ = (int) Math.floor(e.getScrollY()
				/ this.getRowHeight().toPixels());
		this.viewportHeight_ = (int) Math.ceil(e.getViewportHeight()
				/ this.getRowHeight().toPixels());
		this.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
	}

	private void onItemEvent(String nodeId, int columnId, String type,
			String extra1, String extra2, WMouseEvent event) {
		int column = columnId == 0 ? 0 : -1;
		for (int i = 0; i < this.columns_.size(); ++i) {
			if (this.columns_.get(i).id == columnId) {
				column = i;
				break;
			}
		}
		if (column == -1) {
			return;
		}
		WModelIndex c0index = null;
		for (Iterator<Map.Entry<WModelIndex, WTreeViewNode>> i_it = this.renderedNodes_
				.entrySet().iterator(); i_it.hasNext();) {
			Map.Entry<WModelIndex, WTreeViewNode> i = i_it.next();
			if (i.getValue().getId().equals(nodeId)) {
				c0index = i.getValue().getModelIndex();
				break;
			}
		}
		if (!(c0index != null)) {
			System.err.append(
					"Warning (error?): illegal id in WTreeView::onItemEvent()")
					.append('\n');
			return;
		}
		WModelIndex index = this.getModel().getIndex(c0index.getRow(), column,
				c0index.getParent());
		if (!(index != null)) {
			return;
		}
		if (type.equals("clicked")) {
			this.handleClick(index, event);
		} else {
			if (type.equals("dblclicked")) {
				this.handleDoubleClick(index, event);
			} else {
				if (type.equals("mousedown")) {
					this.mouseWentDown().trigger(index, event);
				} else {
					if (type.equals("mouseup")) {
						this.mouseWentUp().trigger(index, event);
					} else {
						if (type.equals("drop")) {
							WDropEvent e = new WDropEvent(WApplication
									.getInstance().decodeObject(extra1),
									extra2, event);
							this.dropEvent(e, index);
						}
					}
				}
			}
		}
	}

	private void setRootNodeStyle() {
		if (!(this.rootNode_ != null)) {
			return;
		}
		if (this.hasAlternatingRowColors()) {
			this.rootNode_.getDecorationStyle().setBackgroundImage(
					new WLink(WApplication.getResourcesUrl()
							+ "themes/"
							+ WApplication.getInstance().getCssTheme()
							+ "/stripes/stripe-"
							+ String.valueOf((int) this.getRowHeight()
									.toPixels()) + "px.gif"));
		} else {
			this.rootNode_.getDecorationStyle().setBackgroundImage(
					new WLink(""));
		}
	}

	void setCollapsed(WModelIndex index) {
		this.expandedSet_.remove(index);
		boolean selectionHasChanged = false;
		SortedSet<WModelIndex> selection = this.getSelectionModel().selection_;
		for (Iterator<WModelIndex> it_it = selection.tailSet(index).iterator(); it_it
				.hasNext();) {
			WModelIndex it = it_it.next();
			WModelIndex i = it;
			if ((i == index || (i != null && i.equals(index)))) {
			} else {
				if (WModelIndex.isAncestor(i, index)) {
					if (this.internalSelect(i, SelectionFlag.Deselect)) {
						selectionHasChanged = true;
					}
				} else {
					break;
				}
			}
		}
		if (selectionHasChanged) {
			this.selectionChanged().trigger();
		}
	}

	private int getCalcOptimalFirstRenderedRow() {
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			return Math.max(0, this.viewportTop_ - this.viewportHeight_
					- this.viewportHeight_ / 2);
		} else {
			return this.viewportTop_;
		}
	}

	private int getCalcOptimalRenderedRowCount() {
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			return 4 * this.viewportHeight_;
		} else {
			return this.viewportHeight_ + 5;
		}
	}

	private void shiftModelIndexes(WModelIndex parent, int start, int count) {
		shiftModelIndexes(parent, start, count, this.getModel(),
				this.expandedSet_);
		int removed = shiftModelIndexes(parent, start, count, this.getModel(),
				this.getSelectionModel().selection_);
		this.shiftEditors(parent, start, count, false);
		if (removed != 0) {
			this.selectionChanged().trigger();
		}
	}

	private static int shiftModelIndexes(WModelIndex parent, int start,
			int count, WAbstractItemModel model, SortedSet<WModelIndex> set) {
		List<WModelIndex> toShift = new ArrayList<WModelIndex>();
		List<WModelIndex> toErase = new ArrayList<WModelIndex>();
		for (Iterator<WModelIndex> it_it = set.tailSet(
				model.getIndex(start, 0, parent)).iterator(); it_it.hasNext();) {
			WModelIndex it = it_it.next();
			WModelIndex i = it;
			WModelIndex p = i.getParent();
			if (!(p == parent || (p != null && p.equals(parent)))
					&& !WModelIndex.isAncestor(p, parent)) {
				break;
			}
			if ((p == parent || (p != null && p.equals(parent)))) {
				toShift.add(i);
				toErase.add(i);
			} else {
				if (count < 0) {
					do {
						if ((p.getParent() == parent || (p.getParent() != null && p
								.getParent().equals(parent)))
								&& p.getRow() >= start
								&& p.getRow() < start - count) {
							toErase.add(i);
							break;
						} else {
							p = p.getParent();
						}
					} while (!(p == parent || (p != null && p.equals(parent))));
				}
			}
		}
		for (int i = 0; i < toErase.size(); ++i) {
			set.remove(toErase.get(i));
		}
		int removed = 0;
		for (int i = 0; i < toShift.size(); ++i) {
			if (toShift.get(i).getRow() + count >= start) {
				WModelIndex newIndex = model.getIndex(toShift.get(i).getRow()
						+ count, toShift.get(i).getColumn(), parent);
				set.add(newIndex);
			} else {
				++removed;
			}
		}
		return removed;
	}

	void addRenderedNode(WTreeViewNode node) {
		this.renderedNodes_.put(node.getModelIndex(), node);
		++this.nodeLoad_;
		this.renderedNodesAdded_ = true;
	}

	void removeRenderedNode(WTreeViewNode node) {
		this.renderedNodes_.remove(node.getModelIndex());
		--this.nodeLoad_;
	}

	private void adjustToViewport(WTreeViewNode changed) {
		this.firstRenderedRow_ = Math.max(0, this.firstRenderedRow_);
		this.validRowCount_ = Math.max(0, Math.min(this.validRowCount_,
				this.rootNode_.getRenderedHeight() - this.firstRenderedRow_));
		int viewportBottom = Math.min(this.rootNode_.getRenderedHeight(),
				this.viewportTop_ + this.viewportHeight_);
		int lastValidRow = this.firstRenderedRow_ + this.validRowCount_;
		boolean renderMore = Math.max(0, this.viewportTop_
				- this.viewportHeight_) < this.firstRenderedRow_
				|| Math.min(this.rootNode_.getRenderedHeight(), viewportBottom
						+ this.viewportHeight_) > lastValidRow;
		boolean pruneFirst = false;
		if (renderMore) {
			int newFirstRenderedRow = Math.min(this.firstRenderedRow_, this
					.getCalcOptimalFirstRenderedRow());
			int newLastValidRow = Math.max(lastValidRow, Math.min(
					this.rootNode_.getRenderedHeight(), this
							.getCalcOptimalFirstRenderedRow()
							+ this.getCalcOptimalRenderedRowCount()));
			int newValidRowCount = newLastValidRow - newFirstRenderedRow;
			int newRows = Math.max(0, this.firstRenderedRow_
					- newFirstRenderedRow)
					+ Math.max(0, newLastValidRow - lastValidRow);
			final int pruneFactor = WApplication.getInstance().getEnvironment()
					.hasAjax() ? 9 : 1;
			if (this.nodeLoad_ + newRows > pruneFactor * this.viewportHeight_) {
				pruneFirst = true;
			} else {
				if (newFirstRenderedRow < this.firstRenderedRow_
						|| newLastValidRow > lastValidRow) {
					this.firstRenderedRow_ = newFirstRenderedRow;
					this.validRowCount_ = newValidRowCount;
					this.adjustRenderedNode(this.rootNode_, 0);
				}
			}
		}
		final int pruneFactor = WApplication.getInstance().getEnvironment()
				.hasAjax() ? 5 : 1;
		if (pruneFirst || this.nodeLoad_ > pruneFactor * this.viewportHeight_) {
			this.firstRenderedRow_ = this.getCalcOptimalFirstRenderedRow();
			this.validRowCount_ = this.getCalcOptimalRenderedRowCount();
			this.pruneNodes(this.rootNode_, 0);
			if (pruneFirst
					&& this.nodeLoad_ < this.getCalcOptimalRenderedRowCount()) {
				this.adjustRenderedNode(this.rootNode_, 0);
			}
		}
	}

	private final void adjustToViewport() {
		adjustToViewport((WTreeViewNode) null);
	}

	private int pruneNodes(WTreeViewNode node, int nodeRow) {
		WModelIndex index = node.getModelIndex();
		++nodeRow;
		if (this.isExpanded(index)) {
			nodeRow += node.getTopSpacerHeight();
			boolean done = false;
			WTreeViewNode c = null;
			for (; nodeRow < this.firstRenderedRow_;) {
				c = node.nextChildNode((WTreeViewNode) null);
				if (!(c != null)) {
					done = true;
					break;
				}
				if (nodeRow + c.getRenderedHeight() < this.firstRenderedRow_) {
					node.addTopSpacerHeight(c.getRenderedHeight());
					nodeRow += c.getRenderedHeight();
					if (c != null)
						c.remove();
					c = null;
				} else {
					nodeRow = this.pruneNodes(c, nodeRow);
					break;
				}
			}
			if (!done) {
				for (; nodeRow <= this.firstRenderedRow_ + this.validRowCount_;) {
					c = node.nextChildNode(c);
					if (!(c != null)) {
						done = true;
						break;
					}
					nodeRow = this.pruneNodes(c, nodeRow);
				}
			}
			if (!done) {
				c = node.nextChildNode(c);
				if (c != null) {
					int i = node.getChildContainer().getIndexOf(c);
					int prunedHeight = 0;
					while (c != null && i < node.getChildContainer().getCount()) {
						c = ((node.getChildContainer().getWidget(i)) instanceof WTreeViewNode ? (WTreeViewNode) (node
								.getChildContainer().getWidget(i))
								: null);
						if (c != null) {
							prunedHeight += c.getRenderedHeight();
							if (c != null)
								c.remove();
						}
					}
					node.addBottomSpacerHeight(prunedHeight);
				}
			}
			nodeRow += node.getBottomSpacerHeight();
			node.normalizeSpacers();
		} else {
			if (node.isChildrenLoaded()) {
				int prunedHeight = 0;
				for (;;) {
					WTreeViewNode c = node.nextChildNode((WTreeViewNode) null);
					if (!(c != null)) {
						break;
					}
					prunedHeight += c.getRenderedHeight();
					if (c != null)
						c.remove();
				}
				node.addBottomSpacerHeight(prunedHeight);
				node.normalizeSpacers();
			}
		}
		return nodeRow;
	}

	int adjustRenderedNode(WTreeViewNode node, int theNodeRow) {
		WModelIndex index = node.getModelIndex();
		if (!(index == this.getRootIndex() || (index != null && index
				.equals(this.getRootIndex())))) {
			++theNodeRow;
		}
		if (!this.isExpanded(index) && !node.isChildrenLoaded()) {
			return theNodeRow;
		}
		int nodeRow = theNodeRow;
		if (node.isAllSpacer()) {
			if (nodeRow + node.getChildrenHeight() > this.firstRenderedRow_
					&& nodeRow < this.firstRenderedRow_ + this.validRowCount_) {
				int childCount = this.getModel().getRowCount(index);
				boolean firstNode = true;
				int rowStubs = 0;
				for (int i = 0; i < childCount; ++i) {
					WModelIndex childIndex = this.getModel().getIndex(i, 0,
							index);
					int childHeight = this.subTreeHeight(childIndex);
					if (nodeRow <= this.firstRenderedRow_ + this.validRowCount_
							&& nodeRow + childHeight > this.firstRenderedRow_) {
						if (firstNode) {
							firstNode = false;
							node.setTopSpacerHeight(rowStubs);
							rowStubs = 0;
						}
						WTreeViewNode n = new WTreeViewNode(this, childIndex,
								childHeight - 1, i == childCount - 1, node);
						node.getChildContainer().addWidget(n);
						int nestedNodeRow = nodeRow;
						nestedNodeRow = this.adjustRenderedNode(n,
								nestedNodeRow);
						assert nestedNodeRow == nodeRow + childHeight;
					} else {
						rowStubs += childHeight;
					}
					nodeRow += childHeight;
				}
				node.setBottomSpacerHeight(rowStubs);
			} else {
				nodeRow += node.getChildrenHeight();
			}
		} else {
			int topSpacerHeight = node.getTopSpacerHeight();
			int nestedNodeRow = nodeRow + topSpacerHeight;
			WTreeViewNode child = node.nextChildNode((WTreeViewNode) null);
			int childCount = this.getModel().getRowCount(index);
			while (topSpacerHeight != 0
					&& nodeRow + topSpacerHeight > this.firstRenderedRow_) {
				WTreeViewNode n = ((node.getChildContainer().getWidget(1)) instanceof WTreeViewNode ? (WTreeViewNode) (node
						.getChildContainer().getWidget(1))
						: null);
				assert n != null;
				WModelIndex childIndex = this.getModel().getIndex(
						n.getModelIndex().getRow() - 1, 0, index);
				int childHeight = this.subTreeHeight(childIndex);
				n = new WTreeViewNode(this, childIndex, childHeight - 1,
						childIndex.getRow() == childCount - 1, node);
				node.getChildContainer().insertWidget(1, n);
				nestedNodeRow = nodeRow + topSpacerHeight - childHeight;
				nestedNodeRow = this.adjustRenderedNode(n, nestedNodeRow);
				assert nestedNodeRow == nodeRow + topSpacerHeight;
				topSpacerHeight -= childHeight;
				node.addTopSpacerHeight(-childHeight);
			}
			for (; child != null; child = node.nextChildNode(child)) {
				nestedNodeRow = this.adjustRenderedNode(child, nestedNodeRow);
			}
			int nch = node.getChildrenHeight();
			int bottomSpacerStart = nch - node.getBottomSpacerHeight();
			while (node.getBottomSpacerHeight() != 0
					&& nodeRow + bottomSpacerStart <= this.firstRenderedRow_
							+ this.validRowCount_) {
				int lastNodeIndex = node.getChildContainer().getCount() - 2;
				WTreeViewNode n = ((node.getChildContainer()
						.getWidget(lastNodeIndex)) instanceof WTreeViewNode ? (WTreeViewNode) (node
						.getChildContainer().getWidget(lastNodeIndex))
						: null);
				assert n != null;
				WModelIndex childIndex = this.getModel().getIndex(
						n.getModelIndex().getRow() + 1, 0, index);
				int childHeight = this.subTreeHeight(childIndex);
				n = new WTreeViewNode(this, childIndex, childHeight - 1,
						childIndex.getRow() == childCount - 1, node);
				node.getChildContainer().insertWidget(lastNodeIndex + 1, n);
				nestedNodeRow = nodeRow + bottomSpacerStart;
				nestedNodeRow = this.adjustRenderedNode(n, nestedNodeRow);
				assert nestedNodeRow == nodeRow + bottomSpacerStart
						+ childHeight;
				node.addBottomSpacerHeight(-childHeight);
				bottomSpacerStart += childHeight;
			}
			nodeRow += nch;
		}
		return this.isExpanded(index) ? nodeRow : theNodeRow;
	}

	private WWidget widgetForIndex(WModelIndex index) {
		if (!(index != null)) {
			return this.rootNode_;
		}
		if (index.getColumn() != 0) {
			return null;
		}
		WTreeViewNode i = this.renderedNodes_.get(index);
		if (i != null) {
			return i;
		} else {
			if (!this.isExpanded(index.getParent())) {
				return null;
			}
			WWidget parent = this.widgetForIndex(index.getParent());
			WTreeViewNode parentNode = ((parent) instanceof WTreeViewNode ? (WTreeViewNode) (parent)
					: null);
			if (parentNode != null) {
				return parentNode.widgetForModelRow(index.getRow());
			} else {
				return parent;
			}
		}
	}

	private WTreeViewNode nodeForIndex(WModelIndex index) {
		if ((index == this.getRootIndex() || (index != null && index
				.equals(this.getRootIndex())))) {
			return this.rootNode_;
		} else {
			WModelIndex column0Index = this.getModel().getIndex(index.getRow(),
					0, index.getParent());
			WTreeViewNode i = this.renderedNodes_.get(column0Index);
			return i != null ? i : null;
		}
	}

	int subTreeHeight(WModelIndex index, int lowerBound, int upperBound) {
		int result = 0;
		if (!(index == this.getRootIndex() || (index != null && index
				.equals(this.getRootIndex())))) {
			++result;
		}
		if (result >= upperBound) {
			return result;
		}
		if (this.getModel() != null && this.isExpanded(index)) {
			int childCount = this.getModel().getRowCount(index);
			for (int i = 0; i < childCount; ++i) {
				WModelIndex childIndex = this.getModel().getIndex(i, 0, index);
				result += this.subTreeHeight(childIndex, upperBound - result);
				if (result >= upperBound) {
					return result;
				}
			}
		}
		return result;
	}

	final int subTreeHeight(WModelIndex index) {
		return subTreeHeight(index, 0, Integer.MAX_VALUE);
	}

	final int subTreeHeight(WModelIndex index, int lowerBound) {
		return subTreeHeight(index, lowerBound, Integer.MAX_VALUE);
	}

	private int renderedRow(WModelIndex index, WWidget w, int lowerBound,
			int upperBound) {
		WTreeViewNode node = ((w) instanceof WTreeViewNode ? (WTreeViewNode) (w)
				: null);
		if (node != null) {
			return node.renderedRow(lowerBound, upperBound);
		} else {
			RowSpacer s = ((w) instanceof RowSpacer ? (RowSpacer) (w) : null);
			int result = s.renderedRow(0, upperBound);
			if (result > upperBound) {
				return result;
			} else {
				if (result + s.getNode().getRenderedHeight() < lowerBound) {
					return result;
				} else {
					return result
							+ this.getIndexRow(index, s.getNode()
									.getModelIndex(), lowerBound - result,
									upperBound - result);
				}
			}
		}
	}

	private final int renderedRow(WModelIndex index, WWidget w) {
		return renderedRow(index, w, 0, Integer.MAX_VALUE);
	}

	private final int renderedRow(WModelIndex index, WWidget w, int lowerBound) {
		return renderedRow(index, w, lowerBound, Integer.MAX_VALUE);
	}

	private int getIndexRow(WModelIndex child, WModelIndex ancestor,
			int lowerBound, int upperBound) {
		if (!(child != null)
				|| (child == ancestor || (child != null && child
						.equals(ancestor)))) {
			return 0;
		} else {
			WModelIndex parent = child.getParent();
			int result = 0;
			for (int r = 0; r < child.getRow(); ++r) {
				result += this.subTreeHeight(this.getModel().getIndex(r, 0,
						parent), 0, upperBound - result);
				if (result >= upperBound) {
					return result;
				}
			}
			return result
					+ this.getIndexRow(parent, ancestor, lowerBound - result,
							upperBound - result);
		}
	}

	private final int getIndexRow(WModelIndex child, WModelIndex ancestor) {
		return getIndexRow(child, ancestor, 0, Integer.MAX_VALUE);
	}

	private final int getIndexRow(WModelIndex child, WModelIndex ancestor,
			int lowerBound) {
		return getIndexRow(child, ancestor, lowerBound, Integer.MAX_VALUE);
	}

	String getColumnStyleClass(int column) {
		return this.columnInfo(column).getStyleClass();
	}

	private int getRenderLowerBound() {
		return this.firstRenderedRow_;
	}

	private int getRenderUpperBound() {
		return this.firstRenderedRow_ + this.validRowCount_;
	}

	void renderedRowsChanged(int row, int count) {
		if (count < 0 && row - count >= this.firstRenderedRow_
				&& row < this.firstRenderedRow_ + this.validRowCount_) {
			this.validRowCount_ += Math.max(this.firstRenderedRow_ - row
					+ count, count);
		}
		if (row < this.firstRenderedRow_) {
			this.firstRenderedRow_ += count;
		}
		this.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
	}

	private WContainerWidget getHeaderRow() {
		WContainerWidget row = ((this.headers_.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (this.headers_
				.getWidget(0))
				: null);
		if (this.getRowHeaderCount() != 0) {
			row = ((row.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (row
					.getWidget(0))
					: null);
		}
		return row;
	}

	boolean internalSelect(WModelIndex index, SelectionFlag option) {
		if (this.getSelectionBehavior() == SelectionBehavior.SelectRows
				&& index.getColumn() != 0) {
			return this.internalSelect(this.getModel().getIndex(index.getRow(),
					0, index.getParent()), option);
		}
		if (super.internalSelect(index, option)) {
			WTreeViewNode node = this.nodeForIndex(index);
			if (node != null) {
				node.renderSelected(this.isSelected(index), index.getColumn());
			}
			return true;
		} else {
			return false;
		}
	}

	void selectRange(WModelIndex first, WModelIndex last) {
		WModelIndex index = first;
		for (;;) {
			for (int c = first.getColumn(); c <= last.getColumn(); ++c) {
				WModelIndex ic = this.getModel().getIndex(index.getRow(), c,
						index.getParent());
				this.internalSelect(ic, SelectionFlag.Select);
				if ((ic == last || (ic != null && ic.equals(last)))) {
					return;
				}
			}
			WModelIndex indexc0 = index.getColumn() == 0 ? index : this
					.getModel().getIndex(index.getRow(), 0, index.getParent());
			if (this.isExpanded(indexc0)
					&& this.getModel().getRowCount(indexc0) > 0) {
				index = this.getModel().getIndex(0, first.getColumn(), indexc0);
			} else {
				for (;;) {
					WModelIndex parent = index.getParent();
					if (index.getRow() + 1 < this.getModel()
							.getRowCount(parent)) {
						index = this.getModel().getIndex(index.getRow() + 1,
								first.getColumn(), parent);
						break;
					} else {
						index = index.getParent();
					}
				}
			}
		}
	}

	private void expandChildrenToDepth(WModelIndex index, int depth) {
		for (int i = 0; i < this.getModel().getRowCount(index); ++i) {
			WModelIndex c = this.getModel().getIndex(i, 0, index);
			this.expand(c);
			if (depth > 1) {
				this.expandChildrenToDepth(c, depth - 1);
			}
		}
	}

	// private void updateColumnWidth(int columnId, int width) ;
	WContainerWidget getHeaderContainer() {
		return this.headerContainer_;
	}

	WWidget headerWidget(int column, boolean contentsOnly) {
		WWidget result = null;
		if (this.headers_ != null && this.headers_.getCount() > 0) {
			if (column == 0) {
				result = this.headers_.getWidget(this.headers_.getCount() - 1);
			} else {
				result = this.getHeaderRow().getWidget(column - 1);
			}
		}
		if (result != null && contentsOnly) {
			return result.find("contents");
		} else {
			return result;
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WTreeView",
				"function(m,d,i,t,r){function p(c){var a=-1,b=null,e=false,h=false,g=null;for(c=f.target(c);c;){if(c.className.indexOf(\"c1 rh\")==0){if(a==-1)a=0}else if(c.className.indexOf(\"Wt-tv-c\")==0){if(c.className.indexOf(\"Wt-tv-c\")==0)a=c.className.split(\" \")[0].substring(7)*1;else if(a==-1)a=0;if(c.getAttribute(\"drop\")===\"true\")h=true;g=c}else if(c.className==\"Wt-tv-node\"){b=c.id;break}if(c.className===\"Wt-selected\")e=true;c=c.parentNode;if(f.hasTag(c, \"BODY\"))break}return{columnId:a,nodeId:b,selected:e,drop:h,el:g}}function x(){if(s){s=false;var c=u.firstChild,a=l.firstChild,b=0,e=0;e=l.lastChild.className.split(\" \")[0];var h=f.getCssRule(\"#\"+d.id+\" .\"+e);if(r)a=a.firstChild;if(!(f.isHidden(d)||l.offsetWidth-a.offsetWidth<8)){for(var g=0,k=a.childNodes.length;g<k;++g)if(a.childNodes[g].className){var j=a.childNodes[g].className.split(\" \")[0];j=f.getCssRule(\"#\"+d.id+\" .\"+j);if(j.style.display!=\"none\")b+=f.pxself(j,\"width\")+7}if(!r){if(f.isIE&&$(document.body).hasClass(\"Wt-rtl\"))if(g= f.getCssRule(\"#\"+d.id+\" .Wt-tv-row\"))g.style.width=b+\"px\";if(h.style.width)$(d).find(\".Wt-headerdiv .\"+e).css(\"width\",h.style.width);else h.style.width=l.offsetWidth-a.offsetWidth-8+\"px\"}e=b+f.pxself(h,\"width\")+(f.isIE6?10:7);if(r){j=f.getCssRule(\"#\"+d.id+\" .Wt-tv-rowc\");j.style.width=b+\"px\";f.isIE&&setTimeout(function(){$(d).find(\".Wt-tv-rowc\").css(\"width\",b+\"px\").css(\"width\",\"\")},0);d.changed=true}else{l.style.width=c.style.width=e+\"px\";a.style.width=b+\"px\"}}}}jQuery.data(d,\"obj\",this);var u=i.firstChild, l=t.firstChild,v=this,f=m.WT;this.click=function(c,a){var b=p(a);b.columnId!=-1&&m.emit(d,{name:\"itemEvent\",eventObject:c,event:a},b.nodeId,b.columnId,\"clicked\",\"\",\"\")};this.dblClick=function(c,a){var b=p(a);b.columnId!=-1&&m.emit(d,{name:\"itemEvent\",eventObject:c,event:a},b.nodeId,b.columnId,\"dblclicked\",\"\",\"\")};this.mouseDown=function(c,a){f.capture(null);var b=p(a);if(b.columnId!=-1){m.emit(d,{name:\"itemEvent\",eventObject:c,event:a},b.nodeId,b.columnId,\"mousedown\",\"\",\"\");d.getAttribute(\"drag\")=== \"true\"&&b.selected&&m._p_.dragStart(d,a)}};this.mouseUp=function(c,a){var b=p(a);b.columnId!=-1&&m.emit(d,{name:\"itemEvent\",eventObject:c,event:a},b.nodeId,b.columnId,\"mouseup\",\"\",\"\")};this.resizeHandleMDown=function(c,a){var b=c.parentNode.parentNode.className.split(\" \")[0];if(b){var e=f.getCssRule(\"#\"+d.id+\" .\"+b),h=f.pxself(e,\"width\"),g=-h,k=1E4,j=$(document.body).hasClass(\"Wt-rtl\");if(j){var w=g;g=-k;k=-w}new f.SizeHandle(f,\"h\",c.offsetWidth,d.firstChild.offsetHeight,g,k,\"Wt-hsh\",function(n){n= h+(j?-n:n);var q=b.substring(7)*1;e.style.width=n+\"px\";v.adjustColumns();m.emit(d,\"columnResized\",q,parseInt(n))},c,d,a,-2,-1)}};var s=false;this.adjustColumns=function(){if(!s){s=true;setTimeout(x,0)}};var o=null;d.handleDragDrop=function(c,a,b,e,h){if(o){o.className=o.classNameOrig;o=null}if(c!=\"end\"){var g=p(b);if(!g.selected&&g.drop&&g.columnId!=-1)if(c==\"drop\")m.emit(d,{name:\"itemEvent\",eventObject:a,event:b},g.nodeId,g.columnId,\"drop\",e,h);else{a.className=\"Wt-valid-drop\";o=g.el;o.classNameOrig= o.className;o.className+=\" Wt-drop-site\"}else a.className=\"\"}};this.autoJavaScript=function(){if(d.parentNode==null){d=i=t=u=l=null;this.autoJavaScript=function(){}}else if(!f.isHidden(d)){x();var c=$(d),a,b=null,e=f.pxself(d,\"width\");if(e==0)e=d.clientWidth;else if(f.boxSizing(d)){e-=f.px(d,\"borderLeftWidth\");e-=f.px(d,\"borderRightWidth\")}var h=i.offsetWidth-i.clientWidth;if(i.clientWidth>0)e-=h;if(c.hasClass(\"column1\")){a=c.find(\".Wt-headerdiv\").get(0).lastChild.className.split(\" \")[0];a=f.getCssRule(\"#\"+ d.id+\" .\"+a);b=f.pxself(a,\"width\")}if((!f.isIE||e>100)&&(e!=i.tw||b!=i.c0w||d.changed)){var g=!d.changed;i.tw=e;i.c0w=b;a=c.find(\".Wt-headerdiv\").get(0).lastChild.className.split(\" \")[0];a=f.getCssRule(\"#\"+d.id+\" .\"+a);var k=u.firstChild,j=f.getCssRule(\"#\"+d.id+\" .cwidth\"),w=j.style.width==k.offsetWidth+1+\"px\",n=l.firstChild;j.style.width=e+\"px\";i.style.width=e+h+\"px\";if(!f.isIE)t.style.marginRight=h+\"px\";if(b!=null){b=e-b-(f.isIE6?10:7);if(b>0){var q=Math.min(b,f.pxself(f.getCssRule(\"#\"+d.id+\" .Wt-tv-rowc\"), \"width\"));e-=b-q;l.style.width=e+\"px\";k.style.width=e+\"px\";f.getCssRule(\"#\"+d.id+\" .Wt-tv-row\").style.width=q+\"px\";f.isIE&&setTimeout(function(){c.find(\" .Wt-tv-row\").css(\"width\",q+\"px\").css(\"width\",\"\")},0)}}else if(w){l.style.width=j.style.width;k.style.width=j.style.width}else l.style.width=k.offsetWidth+\"px\";if(!r&&k.offsetWidth-n.offsetWidth>=7)a.style.width=k.offsetWidth-n.offsetWidth-7+\"px\";d.changed=false;g&&v.adjustColumns()}}};this.scrollTo=function(c,a,b,e){if(a!=-1){a*=b;c=i.scrollTop; var h=i.clientHeight;if(e==0)if(c+h<a)e=1;else if(a<c)e=2;switch(e){case 1:i.scrollTop=a;break;case 2:i.scrollTop=a-(h-b);break;case 3:i.scrollTop=a-(h-b)/2;break}window.fakeEvent={object:i};i.onscroll(window.fakeEvent);window.fakeEvent=null}};v.adjustColumns()}");
	}
}
