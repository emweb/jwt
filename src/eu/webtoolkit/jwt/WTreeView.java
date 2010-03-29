/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A view class that displays a model as a tree or tree table.
 * <p>
 * 
 * The view displays data from a {@link WAbstractItemModel} in a tree or tree
 * table. It provides incremental rendering, allowing the display of data models
 * of any size efficiently, without excessive use of client- or serverside
 * resources. Data of all predefined roles is displayed (see also ItemDataRole),
 * including text, icons, checkboxes, and tooltips .
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
 * {@link WTreeView#setColumn1Fixed(boolean fixed) setColumn1Fixed()} to enable
 * this behaviour.
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
 * <h3>CSS</h3>
 * <p>
 * The treeview is styled by the current CSS theme. The look can be overridden
 * using the <code>Wt-treeview</code> CSS class and the following selectors.
 * <p>
 * Selectors that apply to the header: <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-treeview .Wt-headerdiv         : header (background)
 * .Wt-treeview .Wt-header .Wt-label  : header label
 * .Wt-treeview .Wt-tv-rh             : column resize handle
 * .Wt-treeview .Wt-tv-sh-none        : column sort handle, no sorting
 * .Wt-treeview .Wt-tv-sh-up          : column sort handle, sort up
 * .Wt-treeview .Wt-tv-sh-down        : column sort handle, sort down
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
		this.rootIndex_ = null;
		this.expandedSet_ = new TreeSet<WModelIndex>();
		this.renderedNodes_ = new HashMap<WModelIndex, WTreeViewNode>();
		this.rootNode_ = null;
		this.borderColorRule_ = null;
		this.rootIsDecorated_ = true;
		this.column1Fixed_ = false;
		this.alternatingRowColors_ = false;
		this.expandedRaw_ = new ArrayList<Object>();
		this.selectionRaw_ = new ArrayList<Object>();
		this.collapsed_ = new Signal1<WModelIndex>(this);
		this.expanded_ = new Signal1<WModelIndex>(this);
		this.viewportTop_ = 0;
		this.viewportHeight_ = 30;
		this.nodeLoad_ = 0;
		this.impl_ = new WContainerWidget();
		this.contentsContainer_ = null;
		this.scrollBarC_ = null;
		this.itemEvent_ = new JSignal6<String, Integer, String, String, String, WMouseEvent>(
				this.impl_, "itemEvent") {
		};
		this.setImplementation(this.impl_);
		this.renderState_ = WAbstractItemView.RenderState.NeedRerender;
		WApplication app = WApplication.getInstance();
		this.clickedForCollapseMapper_ = new WSignalMapper1<Integer>(this);
		this.clickedForCollapseMapper_.mapped().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WTreeView.this.collapseColumn(e1);
					}
				});
		this.clickedForExpandMapper_ = new WSignalMapper1<Integer>(this);
		this.clickedForExpandMapper_.mapped().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WTreeView.this.expandColumn(e1);
					}
				});
		if (!app.getEnvironment().hasAjax()) {
			this.clickedMapper_ = new WSignalMapper1<WModelIndex>(this);
			this.clickedMapper_.mapped().addListener(this,
					new Signal1.Listener<WModelIndex>() {
						public void trigger(WModelIndex e1) {
							WTreeView.this.handleClick(e1);
						}
					});
		}
		this.itemEvent_
				.addListener(
						this,
						new Signal6.Listener<String, Integer, String, String, String, WMouseEvent>() {
							public void trigger(String e1, Integer e2,
									String e3, String e4, String e5,
									WMouseEvent e6) {
								WTreeView.this.onItemEvent(e1, e2, e3, e4, e5,
										e6);
							}
						});
		this.setStyleClass("Wt-treeview");
		this.setSelectable(false);
		String CSS_RULES_NAME = "Wt::WTreeView";
		this.expandConfig_ = new ToggleButtonConfig(this);
		this.expandConfig_.addState("Wt-expand");
		this.expandConfig_.addState("Wt-collapse");
		this.expandConfig_.generate();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .Wt-headerdiv",
							"-moz-user-select: none;-khtml-user-select: none;user-select: none;overflow: hidden;width: 100%;",
							CSS_RULES_NAME);
			if (app.getEnvironment().agentIsIE()) {
				app.getStyleSheet().addRule(
						".Wt-treeview .Wt-header .Wt-label", "zoom: 1;");
			}
			app.getStyleSheet().addRule(".Wt-treeview table", "width: 100%");
			app.getStyleSheet().addRule(".Wt-treeview .c1",
					"width: 100%; overflow: hidden;");
			app.getStyleSheet().addRule(".Wt-treeview .c0",
					"width: 19px; vertical-align: middle");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-row",
					"float: right; overflow: hidden;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .Wt-tv-row .Wt-tv-c",
							"display: block; float: left;padding: 0px 3px;text-overflow: ellipsis;overflow: hidden;white-space: nowrap;");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-c",
					"padding: 0px 3px;");
			app.getStyleSheet().addRule(
					".Wt-treeview img.icon, .Wt-treeview input.icon",
					"margin: 0px 3px 2px 0px; vertical-align: middle");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-node img.w0",
					"width: 0px; margin: 0px;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .Wt-tv-node .c0 img, .Wt-treeview .Wt-tv-node .c0 input",
							"margin-right: 0px; margin: -4px 0px;");
			app
					.getStyleSheet()
					.addRule(".Wt-treeview div.Wt-tv-rh",
							"float: right; width: 4px; cursor: col-resize;padding-left: 0px;");
			if (app.getEnvironment().agentIsIE()) {
				app.getStyleSheet().addRule(".Wt-treeview .Wt-header .Wt-tv-c",
						"padding: 0px;padding-left: 7px;");
			} else {
				app.getStyleSheet().addRule(".Wt-treeview .Wt-header .Wt-tv-c",
						"padding: 0px;margin-left: 7px;");
			}
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-rh:hover",
					"background-color: #DDDDDD;");
			app.getStyleSheet().addRule(".Wt-treeview div.Wt-tv-rhc0",
					"float: left; width: 4px;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .Wt-tv-br, .Wt-treeview .Wt-tv-node .Wt-tv-row .Wt-tv-c",
							"border-right: 1px solid white;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .Wt-tv-sh",
							""
									+ "float: right; width: 16px; height: 10px; padding-bottom: 6px;cursor: pointer; cursor:hand;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .Wt-tv-sh-nrh",
							""
									+ "float: right; width: 16px; height: 10px;cursor: pointer; cursor:hand;");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-shc0",
					"float: left;");
			if (app.getEnvironment().agentIsWebKit()
					|| app.getEnvironment().agentIsOpera()) {
				app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-rowc",
						"position: relative;");
			}
			if (app.getEnvironment().agentIsIE()) {
				app.getStyleSheet().addRule(".Wt-treeview .Wt-scroll",
						"position: absolute; overflow-x: auto;height: 19px;");
			} else {
				app.getStyleSheet().addRule(".Wt-treeview .Wt-scroll",
						"overflow: auto;height: 19px;");
			}
			app.getStyleSheet().addRule(".Wt-treeview .Wt-scroll div",
					"height: 1px;");
		}
		this.setColumnBorder(WColor.white);
		app.getStyleSheet().addRule("#" + this.getId() + " .cwidth",
				"height: 1px;");
		app.getStyleSheet()
				.addRule(
						"#" + this.getId() + "dw",
						"width: 32px; height: 32px;background: url("
								+ WApplication.getResourcesUrl()
								+ "items-not-ok.gif);");
		app.getStyleSheet().addRule(
				"#" + this.getId() + "dw.Wt-valid-drop",
				"width: 32px; height: 32px;background: url("
						+ WApplication.getResourcesUrl() + "items-ok.gif);");
		this.rowHeightRule_ = new WCssTemplateRule("#" + this.getId() + " .rh");
		app.getStyleSheet().addRule(this.rowHeightRule_);
		this.rowWidthRule_ = new WCssTemplateRule("#" + this.getId()
				+ " .Wt-tv-row");
		app.getStyleSheet().addRule(this.rowWidthRule_);
		this.rowContentsWidthRule_ = new WCssTemplateRule("#" + this.getId()
				+ " .Wt-tv-rowc");
		app.getStyleSheet().addRule(this.rowContentsWidthRule_);
		this.setRowHeight(this.rowHeight_);
		if (app.getEnvironment().hasJavaScript()) {
			this.impl_.setPositionScheme(PositionScheme.Relative);
		}
		WVBoxLayout layout = new WVBoxLayout();
		layout.setSpacing(0);
		layout.setContentsMargins(0, 0, 0, 0);
		layout.addWidget(this.headerContainer_ = new WContainerWidget());
		this.headerContainer_
				.setOverflow(WContainerWidget.Overflow.OverflowHidden);
		this.headerContainer_.setStyleClass("Wt-header headerrh cwidth");
		this.headers_ = new WContainerWidget(this.headerContainer_);
		this.headers_.setStyleClass("Wt-headerdiv headerrh");
		this.headerHeightRule_ = new WCssTemplateRule("#" + this.getId()
				+ " .headerrh");
		app.getStyleSheet().addRule(this.headerHeightRule_);
		this.setHeaderHeight(this.headerLineHeight_);
		this.contentsContainer_ = new WContainerWidget();
		this.contentsContainer_.setStyleClass("cwidth");
		this.contentsContainer_
				.setOverflow(WContainerWidget.Overflow.OverflowAuto);
		this.contentsContainer_
				.addWidget(this.contents_ = new WContainerWidget());
		this.contentsContainer_.scrolled().addListener(this,
				new Signal1.Listener<WScrollEvent>() {
					public void trigger(WScrollEvent e1) {
						WTreeView.this.onViewportChange(e1);
					}
				});
		this.contentsContainer_.scrolled().addListener(
				this.tieContentsHeaderScrollJS_);
		this.contents_.addWidget(new WContainerWidget());
		if (app.getEnvironment().agentIsIE()) {
			this.contents_.setAttributeValue("style", "zoom: 1");
		}
		layout.addWidget(this.contentsContainer_, 1);
		this.impl_.setLayout(layout);
		this.selectionChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTreeView.this.checkDragSelection();
			}
		});
		this.bindObjJS(this.itemClickedJS_, "click");
		this.bindObjJS(this.itemDoubleClickedJS_, "dblClick");
		this.bindObjJS(this.itemMouseDownJS_, "mouseDown");
		this.bindObjJS(this.itemMouseUpJS_, "mouseUp");
		this.bindObjJS(this.resizeHandleMDownJS_, "resizeHandleMDown");
		this.bindObjJS(this.resizeHandleMMovedJS_, "resizeHandleMMoved");
		this.bindObjJS(this.resizeHandleMUpJS_, "resizeHandleMUp");
		this.tieContentsHeaderScrollJS_
				.setJavaScript("function(obj, event) {"
						+ this.headerContainer_.getJsRef()
						+ ".scrollLeft=obj.scrollLeft;var t = "
						+ this.contents_.getJsRef()
						+ ".firstChild;var h = "
						+ this.headers_.getJsRef()
						+ ";h.style.width = (t.offsetWidth - 1) + 'px';h.style.width = t.offsetWidth + 'px';}");
		if (app.getEnvironment().agentIsWebKit()
				|| app.getEnvironment().agentIsOpera()) {
			this.tieRowsScrollJS_
					.setJavaScript("function(obj, event) {Wt3_1_2.getCssRule('#"
							+ this.getId()
							+ " .Wt-tv-rowc').style.left= -obj.scrollLeft + 'px';}");
		} else {
			this.tieRowsScrollJS_
					.setJavaScript("function(obj, event) {obj.parentNode.style.width = Wt3_1_2.getCssRule('#"
							+ this.getId()
							+ " .cwidth').style.width;$('#"
							+ this.getId()
							+ " .Wt-tv-rowc').parent().scrollLeft(obj.scrollLeft);}");
		}
		app.addAutoJavaScript("{var obj = $('#" + this.getId()
				+ "').data('obj');if (obj) obj.autoJavaScript();}");
		if (parent != null) {
			parent.addWidget(this);
		}
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

	/**
	 * Destructor.
	 */
	public void remove() {
		;
		this.impl_.clear();
		if (this.rowHeightRule_ != null)
			this.rowHeightRule_.remove();
		for (int i = 0; i < this.columns_.size(); ++i) {
			if (this.columns_.get(i).styleRule != null)
				this.columns_.get(i).styleRule.remove();
		}
		super.remove();
	}

	/**
	 * Sets the root index.
	 * <p>
	 * The root index is the model index that is considered the root node. This
	 * node itself is not rendered, but all its children are the top level
	 * nodes.
	 * <p>
	 * The default value is WModelIndex(), corresponding to the invisible root.
	 * <p>
	 * 
	 * @see WTreeView#setModel(WAbstractItemModel model)
	 */
	public void setRootIndex(WModelIndex rootIndex) {
		if (!(rootIndex == this.rootIndex_ || (rootIndex != null && rootIndex
				.equals(this.rootIndex_)))) {
			this.rootIndex_ = rootIndex;
			if (this.model_ != null) {
				this
						.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
			}
		}
	}

	/**
	 * Returns the root index.
	 * <p>
	 * 
	 * @see WTreeView#setRootIndex(WModelIndex rootIndex)
	 */
	public WModelIndex getRootIndex() {
		return this.rootIndex_;
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
		return (index == this.rootIndex_ || (index != null && index
				.equals(this.rootIndex_)))
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
			this.expandChildrenToDepth(this.rootIndex_, depth);
		}
	}

	/**
	 * Sets if alternating row colors are to be used. Configure whether rows get
	 * an alternating background color. These are implemented by using a
	 * background image on the root node, like:.
	 * <p>
	 * <div align="center"> <img src="doc-files//stripe-30px.gif"
	 * alt="Sample image use for alternating row colors">
	 * <p>
	 * <strong>Sample image use for alternating row colors</strong>
	 * </p>
	 * </div> The image that is used is
	 * <i>resourcesURL</i>&quot;/stripes/stripe-
	 * &lt;i&gt;n&lt;/i&gt;px.gif&quot;, where <code>n</code> is the row height.
	 * In the resource folder are images pregenerated for one color and row
	 * sizes from 10 to 30px.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 */
	public void setAlternatingRowColors(boolean enable) {
		if (this.alternatingRowColors_ != enable) {
			this.alternatingRowColors_ = enable;
			this.setRootNodeStyle();
		}
	}

	public boolean hasAlternatingRowColors() {
		return this.alternatingRowColors_;
	}

	/**
	 * Sets whether toplevel items are decorated.
	 * <p>
	 * By default, top level nodes have expand/collapse and other lines to
	 * display their linkage and offspring, like any node.
	 * <p>
	 * By setting <code>show</code> to <code>false</code>, you can hide these
	 * decorations for root nodes, and in this way mimic a plain list.
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

	/**
	 * Configures whether horizontal scrolling includes the first column.
	 * <p>
	 * To display a model with many columns, this option allows you to keep the
	 * first column fixed while scrolling through the other columns of the
	 * model.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Currently, you must set this option before any other
	 * manipulation of a newly created treeview, and only <code>enable</code> =
	 * <code>true</code> is supported. </i>
	 * </p>
	 */
	public void setColumn1Fixed(boolean fixed) {
		if (fixed && !this.column1Fixed_) {
			this.column1Fixed_ = fixed;
			this.setStyleClass("Wt-treeview column1");
			WContainerWidget rootWrap = ((this.contents_.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (this.contents_
					.getWidget(0))
					: null);
			rootWrap.resize(new WLength(100, WLength.Unit.Percentage),
					WLength.Auto);
			rootWrap.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			this.rowWidthRule_.getTemplateWidget().resize(new WLength(0),
					WLength.Auto);
			WContainerWidget scrollBarContainer = new WContainerWidget();
			scrollBarContainer.setStyleClass("cwidth");
			scrollBarContainer.resize(WLength.Auto, new WLength(19));
			this.scrollBarC_ = new WContainerWidget(scrollBarContainer);
			this.scrollBarC_.setStyleClass("Wt-tv-row Wt-scroll");
			this.scrollBarC_.scrolled().addListener(this.tieRowsScrollJS_);
			WApplication app = WApplication.getInstance();
			if (app.getEnvironment().agentIsIE()) {
				scrollBarContainer.setPositionScheme(PositionScheme.Relative);
				this.scrollBarC_.setAttributeValue("style", "right: 0px");
			}
			WContainerWidget scrollBar = new WContainerWidget(this.scrollBarC_);
			scrollBar.setStyleClass("Wt-tv-rowc");
			if (app.getEnvironment().agentIsWebKit()
					|| app.getEnvironment().agentIsOpera()) {
				scrollBar.setAttributeValue("style", "left: 0px;");
			}
			this.impl_.getLayout().addWidget(scrollBarContainer);
			app.addAutoJavaScript("{var s=" + this.scrollBarC_.getJsRef()
					+ ";if (s) {" + this.tieRowsScrollJS_.execJs("s") + "}}");
		}
	}

	/**
	 * Returns whether horizontal scrolling includes the first column.
	 * <p>
	 * 
	 * @see WTreeView#setColumn1Fixed(boolean fixed)
	 */
	public boolean isColumn1Fixed() {
		return this.column1Fixed_;
	}

	public void resize(WLength width, WLength height) {
		if (!height.isAuto()) {
			this.viewportHeight_ = (int) Math.ceil(height.toPixels()
					/ this.rowHeight_.toPixels());
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
		}
		WLength w = WApplication.getInstance().getEnvironment().hasAjax() ? WLength.Auto
				: width;
		this.contentsContainer_.resize(w, WLength.Auto);
		this.headerContainer_.resize(w, WLength.Auto);
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

	// public void openPersistentEditor(WModelIndex index) ;
	// public void closePersistentEditor(WModelIndex index) ;
	public void setModel(WAbstractItemModel model) {
		super.setModel(model);
		this.rootIndex_ = null;
		this.modelConnections_.add(this.model_.columnsInserted().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelColumnsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.columnsAboutToBeRemoved()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WTreeView.this.modelColumnsAboutToBeRemoved(e1,
										e2, e3);
							}
						}));
		this.modelConnections_.add(this.model_.columnsRemoved().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelColumnsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.rowsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelRowsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.rowsAboutToBeRemoved()
				.addListener(this,
						new Signal3.Listener<WModelIndex, Integer, Integer>() {
							public void trigger(WModelIndex e1, Integer e2,
									Integer e3) {
								WTreeView.this.modelRowsAboutToBeRemoved(e1,
										e2, e3);
							}
						}));
		this.modelConnections_.add(this.model_.rowsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTreeView.this.modelRowsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.dataChanged().addListener(this,
				new Signal2.Listener<WModelIndex, WModelIndex>() {
					public void trigger(WModelIndex e1, WModelIndex e2) {
						WTreeView.this.modelDataChanged(e1, e2);
					}
				}));
		this.modelConnections_.add(this.model_.headerDataChanged().addListener(
				this, new Signal3.Listener<Orientation, Integer, Integer>() {
					public void trigger(Orientation e1, Integer e2, Integer e3) {
						WTreeView.this.modelHeaderDataChanged(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.layoutAboutToBeChanged()
				.addListener(this, new Signal.Listener() {
					public void trigger() {
						WTreeView.this.modelLayoutAboutToBeChanged();
					}
				}));
		this.modelConnections_.add(this.model_.layoutChanged().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						WTreeView.this.modelLayoutChanged();
					}
				}));
		this.modelConnections_.add(this.model_.modelReset().addListener(this,
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
		if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
				.getValue()) {
			app.doJavaScript("$('#" + this.getId()
					+ "').data('obj').adjustColumns();");
		}
	}

	public WLength getColumnWidth(int column) {
		return this.columnInfo(column).width;
	}

	public void setRowHeight(WLength rowHeight) {
		this.rowHeight_ = rowHeight;
		this.rowHeightRule_.getTemplateWidget().resize(WLength.Auto,
				this.rowHeight_);
		this.rowHeightRule_.getTemplateWidget().setLineHeight(this.rowHeight_);
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

	public void setHeaderHeight(WLength height, boolean multiLine) {
		this.headerLineHeight_ = height;
		this.multiLineHeader_ = multiLine;
		int lineCount = this.getHeaderLevelCount();
		WLength headerHeight = WLength.multiply(this.headerLineHeight_,
				lineCount);
		this.headerHeightRule_.getTemplateWidget().resize(WLength.Auto,
				headerHeight);
		if (!this.multiLineHeader_) {
			this.headerHeightRule_.getTemplateWidget().setLineHeight(
					this.headerLineHeight_);
		} else {
			this.headerHeightRule_.getTemplateWidget().setLineHeight(
					WLength.Auto);
		}
		this.headers_.resize(this.headers_.getWidth(), headerHeight);
		this.headerContainer_.resize(this.headerContainer_.getWidth(),
				headerHeight);
		if (this.renderState_.getValue() >= WAbstractItemView.RenderState.NeedRerenderHeader
				.getValue()) {
			return;
		}
		if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
			for (int i = 1; i < this.getColumnCount(); ++i) {
				this.headerTextWidget(i).setWordWrap(multiLine);
			}
		}
	}

	public void setColumnAlignment(int column, AlignmentFlag alignment) {
		this.columnInfo(column).alignment = alignment;
		WWidget w = this.columnInfo(column).styleRule.getTemplateWidget();
		if (column != 0) {
			String align = null;
			switch (alignment) {
			case AlignLeft:
				align = "left";
				break;
			case AlignCenter:
				align = "center";
				break;
			case AlignRight:
				align = "right";
				break;
			case AlignJustify:
				align = "justify";
				break;
			default:
				break;
			}
			if (align != null) {
				w.setAttributeValue("style", "text-align: " + align);
			}
		} else {
			if (alignment == AlignmentFlag.AlignRight) {
				w.setFloatSide(Side.Right);
			}
		}
	}

	public AlignmentFlag getColumnAlignment(int column) {
		return this.columnInfo(column).alignment;
	}

	public void setHeaderAlignment(int column, AlignmentFlag alignment) {
		this.columnInfo(column).headerAlignment = alignment;
		if (this.renderState_.getValue() >= WAbstractItemView.RenderState.NeedRerenderHeader
				.getValue()) {
			return;
		}
		WContainerWidget wc = ((this.headerWidget(column)) instanceof WContainerWidget ? (WContainerWidget) (this
				.headerWidget(column))
				: null);
		wc.setContentAlignment(EnumSet.of(alignment));
	}

	public AlignmentFlag getHeaderAlignment(int column) {
		return this.columnInfo(column).headerAlignment;
	}

	public void setColumnBorder(WColor color) {
		if (this.borderColorRule_ != null)
			this.borderColorRule_.remove();
		this.borderColorRule_ = new WCssTextRule(
				".Wt-treeview .Wt-tv-br, .Wt-treeview .header .Wt-tv-row, .Wt-treeview .Wt-tv-node .Wt-tv-row .Wt-tv-c, .Wt-treeview .Wt-tv-node .Wt-tv-row",
				"border-color: " + color.getCssText());
		WApplication.getInstance().getStyleSheet().addRule(
				this.borderColorRule_);
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
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
		super.render(flags);
	}

	protected void enableAjax() {
		this.rootNode_.clicked().addListener(this.itemClickedJS_);
		this.rootNode_.doubleClicked().addListener(this.itemDoubleClickedJS_);
		if (this.mouseWentDown_.isConnected() || this.dragEnabled_) {
			this.rootNode_.mouseWentDown().addListener(this.itemMouseDownJS_);
		}
		if (this.mouseWentUp_.isConnected()) {
			this.rootNode_.mouseWentUp().addListener(this.itemMouseUpJS_);
		}
		super.enableAjax();
	}

	private WModelIndex rootIndex_;
	SortedSet<WModelIndex> expandedSet_;
	private HashMap<WModelIndex, WTreeViewNode> renderedNodes_;
	private WTreeViewNode rootNode_;
	private WCssTemplateRule rowHeightRule_;
	private WCssTemplateRule headerHeightRule_;
	private WCssTemplateRule rowWidthRule_;
	private WCssTemplateRule rowContentsWidthRule_;
	private WCssRule borderColorRule_;
	private boolean rootIsDecorated_;
	boolean column1Fixed_;
	private boolean alternatingRowColors_;
	private List<Object> expandedRaw_;
	private List<Object> selectionRaw_;
	private Object rawRootIndex_;
	Signal1<WModelIndex> collapsed_;
	Signal1<WModelIndex> expanded_;
	private int viewportTop_;
	private int viewportHeight_;
	private int firstRenderedRow_;
	private int validRowCount_;
	private int nodeLoad_;
	private WContainerWidget impl_;
	private WContainerWidget headers_;
	private WContainerWidget headerContainer_;
	private WContainerWidget contents_;
	private WContainerWidget contentsContainer_;
	private WContainerWidget scrollBarC_;
	private int firstRemovedRow_;
	private int removedHeight_;
	private JSignal6<String, Integer, String, String, String, WMouseEvent> itemEvent_;
	ToggleButtonConfig expandConfig_;

	WAbstractItemView.ColumnInfo columnInfo(int column) {
		while (column >= (int) this.columns_.size()) {
			WAbstractItemView.ColumnInfo ci = this.createColumnInfo(column);
			this.columns_.add(ci);
		}
		return this.columns_.get(column);
	}

	private WAbstractItemView.ColumnInfo createColumnInfo(int column) {
		WAbstractItemView.ColumnInfo ci = new WAbstractItemView.ColumnInfo(
				this, ++this.nextColumnId_, column);
		ci.styleRule = new WCssTemplateRule("#" + this.getId() + " ."
				+ ci.getStyleClass());
		WApplication app = WApplication.getInstance();
		if (column != 0) {
			ci.width = new WLength(150);
			ci.styleRule.getTemplateWidget().resize(ci.width, WLength.Auto);
		} else {
			app.getStyleSheet().addRule(
					"#" + this.getId() + " .Wt-tv-node ." + ci.getStyleClass(),
					"width: auto;text-overflow: ellipsis;overflow: hidden");
		}
		app.getStyleSheet().addRule(ci.styleRule);
		return ci;
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		String THIS_JS = "js/WTreeView.js";
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
		app.doJavaScript("new Wt3_1_2.WTreeView(" + app.getJavaScriptClass()
				+ "," + this.getJsRef() + ","
				+ this.contentsContainer_.getJsRef() + ","
				+ this.headerContainer_.getJsRef() + ","
				+ (this.column1Fixed_ ? "true" : "false") + ");");
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
		if (this.column1Fixed_) {
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
		if (this.currentSortColumn_ != -1) {
			SortOrder order = this.columnInfo(this.currentSortColumn_).sortOrder;
			this.headerSortIconWidget(this.currentSortColumn_).setStyleClass(
					order == SortOrder.AscendingOrder ? "Wt-tv-sh Wt-tv-sh-up"
							: "Wt-tv-sh Wt-tv-sh-down");
		}
		app.doJavaScript("$('#" + this.getId()
				+ "').data('obj').adjustColumns();");
		if (this.model_ != null) {
			this.modelHeaderDataChanged(Orientation.Horizontal, 0, this
					.getColumnCount() - 1);
		}
	}

	private void rerenderTree() {
		WContainerWidget wrapRoot = ((this.contents_.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (this.contents_
				.getWidget(0))
				: null);
		wrapRoot.clear();
		this.firstRenderedRow_ = this.getCalcOptimalFirstRenderedRow();
		this.validRowCount_ = 0;
		this.rootNode_ = new WTreeViewNode(this, this.rootIndex_, -1, true,
				(WTreeViewNode) null);
		this.rootNode_.resize(new WLength(100, WLength.Unit.Percentage),
				new WLength(1));
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			this.rootNode_.clicked().addListener(this.itemClickedJS_);
			this.rootNode_.doubleClicked().addListener(
					this.itemDoubleClickedJS_);
			if (this.mouseWentDown_.isConnected() || this.dragEnabled_) {
				this.rootNode_.mouseWentDown().addListener(
						this.itemMouseDownJS_);
			}
			if (this.mouseWentUp_.isConnected()) {
				this.rootNode_.mouseWentUp().addListener(this.itemMouseUpJS_);
			}
		}
		this.setRootNodeStyle();
		wrapRoot.addWidget(this.rootNode_);
		this.adjustToViewport();
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

	void modelColumnsInserted(WModelIndex parent, int start, int end) {
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
					app.doJavaScript("$('#" + this.getId()
							+ "').data('obj').adjustColumns();");
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
			WWidget parentWidget = this.widgetForIndex(parent);
			if (parentWidget != null) {
				WTreeViewNode node = ((parentWidget) instanceof WTreeViewNode ? (WTreeViewNode) (parentWidget)
						: null);
				if (node != null) {
					for (WTreeViewNode c = node
							.nextChildNode((WTreeViewNode) null); c != null; c = node
							.nextChildNode(c)) {
						c.insertColumns(start, count);
					}
				}
			}
		}
	}

	void modelColumnsAboutToBeRemoved(WModelIndex parent, int start, int end) {
		int count = end - start + 1;
		if (!(parent != null)) {
			if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
					.getValue()) {
				WApplication app = WApplication.getInstance();
				app.doJavaScript("$('#" + this.getId()
						+ "').data('obj').adjustColumns();");
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

	void modelColumnsRemoved(WModelIndex parent, int start, int end) {
		if (this.renderState_ == WAbstractItemView.RenderState.NeedRerender
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData) {
			return;
		}
		int count = end - start + 1;
		if (start != 0) {
			WWidget parentWidget = this.widgetForIndex(parent);
			if (parentWidget != null) {
				WTreeViewNode node = ((parentWidget) instanceof WTreeViewNode ? (WTreeViewNode) (parentWidget)
						: null);
				if (node != null) {
					for (WTreeViewNode c = node
							.nextChildNode((WTreeViewNode) null); c != null; c = node
							.nextChildNode(c)) {
						c.removeColumns(start, count);
					}
				}
			}
		}
		if (start <= this.currentSortColumn_ && this.currentSortColumn_ <= end) {
			this.currentSortColumn_ = -1;
		}
	}

	void modelRowsInserted(WModelIndex parent, int start, int end) {
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
					if (end < this.model_.getRowCount(parent) - 1) {
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
						this.renderedRowsChanged(this.renderedRow(this.model_
								.getIndex(start, 0, parent), parentNode
								.topSpacer(), this.getRenderLowerBound(), this
								.getRenderUpperBound()), count);
					} else {
						if (startWidget != null
								&& startWidget == parentNode.bottomSpacer()) {
							parentNode.addBottomSpacerHeight(count);
							this.renderedRowsChanged(this.renderedRow(
									this.model_.getIndex(start, 0, parent),
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
							int parentRowCount = this.model_
									.getRowCount(parent);
							int nodesToAdd = Math.min(count, maxRenderHeight);
							WTreeViewNode first = null;
							for (int i = 0; i < nodesToAdd; ++i) {
								WTreeViewNode n = new WTreeViewNode(this,
										this.model_.getIndex(start + i, 0,
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
							if (end == this.model_.getRowCount(parent) - 1
									&& start >= 1) {
								WTreeViewNode n = ((parentNode
										.widgetForModelRow(start - 1)) instanceof WTreeViewNode ? (WTreeViewNode) (parentNode
										.widgetForModelRow(start - 1))
										: null);
								if (n != null) {
									n.updateGraphics(false,
											this.model_.getRowCount(n
													.getModelIndex()) == 0);
								}
							}
						}
					}
				}
				if (this.model_.getRowCount(parent) == count) {
					parentNode.updateGraphics(parentNode.isLast(), false);
				}
			} else {
				RowSpacer s = ((parentWidget) instanceof RowSpacer ? (RowSpacer) (parentWidget)
						: null);
				s.setRows(s.getRows() + count);
				s.getNode().adjustChildrenHeight(count);
				this.renderedRowsChanged(this.renderedRow(this.model_.getIndex(
						start, 0, parent), s, this.getRenderLowerBound(), this
						.getRenderUpperBound()), count);
			}
		}
	}

	void modelRowsAboutToBeRemoved(WModelIndex parent, int start, int end) {
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
								WModelIndex childIndex = this.model_.getIndex(
										i, 0, parent);
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
								if (w != null)
									w.remove();
							}
						}
						parentNode.normalizeSpacers();
						parentNode.adjustChildrenHeight(-this.removedHeight_);
						parentNode.shiftModelIndexes(start, -count);
						if (end == this.model_.getRowCount(parent) - 1
								&& start >= 1) {
							WTreeViewNode n = ((parentNode
									.widgetForModelRow(start - 1)) instanceof WTreeViewNode ? (WTreeViewNode) (parentNode
									.widgetForModelRow(start - 1))
									: null);
							if (n != null) {
								n.updateGraphics(true, this.model_
										.getRowCount(n.getModelIndex()) == 0);
							}
						}
					}
					if (this.model_.getRowCount(parent) == count) {
						parentNode.updateGraphics(parentNode.isLast(), true);
					}
				} else {
					RowSpacer s = ((parentWidget) instanceof RowSpacer ? (RowSpacer) (parentWidget)
							: null);
					for (int i = start; i <= end; ++i) {
						WModelIndex childIndex = this.model_.getIndex(i, 0,
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

	void modelRowsRemoved(WModelIndex parent, int start, int end) {
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
						WModelIndex index = this.model_.getIndex(r, 0, parent);
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

	void modelHeaderDataChanged(Orientation orientation, int start, int end) {
		if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
				.getValue()) {
			if (orientation == Orientation.Horizontal) {
				for (int i = start; i <= end; ++i) {
					WString label = StringUtils.asString(this.model_
							.getHeaderData(i));
					this.headerTextWidget(i).setText(label);
				}
			}
		}
	}

	void modelLayoutAboutToBeChanged() {
		this.convertToRaw(this.expandedSet_, this.expandedRaw_);
		this.convertToRaw(this.selectionModel_.selection_, this.selectionRaw_);
		this.rawRootIndex_ = this.model_.toRawIndex(this.rootIndex_);
	}

	void modelLayoutChanged() {
		if (this.rawRootIndex_ != null) {
			this.rootIndex_ = this.model_.fromRawIndex(this.rawRootIndex_);
		} else {
			this.rootIndex_ = null;
		}
		for (int i = 0; i < this.expandedRaw_.size(); ++i) {
			this.expandedSet_.add(this.model_.fromRawIndex(this.expandedRaw_
					.get(i)));
		}
		this.expandedRaw_.clear();
		for (int i = 0; i < this.selectionRaw_.size(); ++i) {
			WModelIndex index = this.model_.fromRawIndex(this.selectionRaw_
					.get(i));
			if ((index != null)) {
				this.selectionModel_.selection_.add(index);
			}
		}
		this.selectionRaw_.clear();
		this.renderedNodes_.clear();
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
	}

	private void onViewportChange(WScrollEvent e) {
		this.viewportTop_ = (int) Math.floor(e.getScrollY()
				/ this.rowHeight_.toPixels());
		this.viewportHeight_ = (int) Math.ceil(e.getViewportHeight()
				/ this.rowHeight_.toPixels());
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
		WModelIndex index = this.model_.getIndex(c0index.getRow(), column,
				c0index.getParent());
		if (type.equals("clicked")) {
			this.selectionHandleClick(index, event.getModifiers());
			this.clicked_.trigger(index, event);
		} else {
			if (type.equals("dblclicked")) {
				this.doubleClicked_.trigger(index, event);
			} else {
				if (type.equals("mousedown")) {
					this.mouseWentDown_.trigger(index, event);
				} else {
					if (type.equals("mouseup")) {
						this.mouseWentUp_.trigger(index, event);
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
		if (this.alternatingRowColors_) {
			this.rootNode_.getDecorationStyle().setBackgroundImage(
					WApplication.getResourcesUrl() + "themes/"
							+ WApplication.getInstance().getCssTheme()
							+ "/stripes/stripe-"
							+ String.valueOf((int) this.rowHeight_.toPixels())
							+ "px.gif");
		} else {
			this.rootNode_.getDecorationStyle().setBackgroundImage("");
		}
	}

	void setCollapsed(WModelIndex index) {
		this.expandedSet_.remove(index);
		boolean selectionHasChanged = false;
		SortedSet<WModelIndex> selection = this.selectionModel_.selection_;
		for (Iterator<WModelIndex> it_it = selection.tailSet(index).iterator(); it_it
				.hasNext();) {
			WModelIndex it = it_it.next();
			WModelIndex i = it;
			if ((i == index || (i != null && i.equals(index)))) {
			} else {
				if (isAncestor(i, index)) {
					if (this.internalSelect(i, SelectionFlag.Deselect)) {
						selectionHasChanged = true;
					}
				} else {
					break;
				}
			}
		}
		if (selectionHasChanged) {
			this.selectionChanged_.trigger();
		}
	}

	private void expandColumn(int columnid) {
		this.model_.expandColumn(this.columnById(columnid));
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
		this.setHeaderHeight(this.headerLineHeight_, this.multiLineHeader_);
	}

	private void collapseColumn(int columnid) {
		this.model_.collapseColumn(this.columnById(columnid));
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
		this.setHeaderHeight(this.headerLineHeight_, this.multiLineHeader_);
	}

	private int getCalcOptimalFirstRenderedRow() {
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			return Math.max(0, this.viewportTop_ - this.viewportHeight_
					- this.viewportHeight_ / 2);
		} else {
			return 0;
		}
	}

	private int getCalcOptimalRenderedRowCount() {
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			return 4 * this.viewportHeight_;
		} else {
			return this.rootNode_.getRenderedHeight();
		}
	}

	private void shiftModelIndexes(WModelIndex parent, int start, int count) {
		shiftModelIndexes(parent, start, count, this.model_, this.expandedSet_);
		int removed = shiftModelIndexes(parent, start, count, this.model_,
				this.selectionModel_.selection_);
		if (removed != 0) {
			this.selectionChanged_.trigger();
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
					&& !isAncestor(p, parent)) {
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
			if (this.nodeLoad_ + newRows > 9 * this.viewportHeight_) {
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
		if (pruneFirst || this.nodeLoad_ > 5 * this.viewportHeight_) {
			this.firstRenderedRow_ = this.getCalcOptimalFirstRenderedRow();
			this.validRowCount_ = this.getCalcOptimalRenderedRowCount();
			if (WApplication.getInstance().getEnvironment().hasAjax()) {
				this.pruneNodes(this.rootNode_, 0);
			}
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
		if (!(index == this.rootIndex_ || (index != null && index
				.equals(this.rootIndex_)))) {
			++theNodeRow;
		}
		if (!this.isExpanded(index) && !node.isChildrenLoaded()) {
			return theNodeRow;
		}
		int nodeRow = theNodeRow;
		if (node.isAllSpacer()) {
			if (nodeRow + node.getChildrenHeight() > this.firstRenderedRow_
					&& nodeRow < this.firstRenderedRow_ + this.validRowCount_) {
				int childCount = this.model_.getRowCount(index);
				boolean firstNode = true;
				int rowStubs = 0;
				for (int i = 0; i < childCount; ++i) {
					WModelIndex childIndex = this.model_.getIndex(i, 0, index);
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
			int childCount = this.model_.getRowCount(index);
			while (topSpacerHeight != 0
					&& nodeRow + topSpacerHeight > this.firstRenderedRow_) {
				WTreeViewNode n = ((node.getChildContainer().getWidget(1)) instanceof WTreeViewNode ? (WTreeViewNode) (node
						.getChildContainer().getWidget(1))
						: null);
				assert n != null;
				WModelIndex childIndex = this.model_.getIndex(n.getModelIndex()
						.getRow() - 1, 0, index);
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
				WModelIndex childIndex = this.model_.getIndex(n.getModelIndex()
						.getRow() + 1, 0, index);
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

	int subTreeHeight(WModelIndex index, int lowerBound, int upperBound) {
		int result = 0;
		if (!(index == this.rootIndex_ || (index != null && index
				.equals(this.rootIndex_)))) {
			++result;
		}
		if (result >= upperBound) {
			return result;
		}
		if (this.isExpanded(index)) {
			int childCount = this.model_.getRowCount(index);
			for (int i = 0; i < childCount; ++i) {
				WModelIndex childIndex = this.model_.getIndex(i, 0, index);
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
		if ((child == ancestor || (child != null && child.equals(ancestor)))) {
			return 0;
		} else {
			WModelIndex parent = child.getParent();
			int result = 0;
			for (int r = 0; r < child.getRow(); ++r) {
				result += this.subTreeHeight(
						this.model_.getIndex(r, 0, parent), 0, upperBound
								- result);
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
		if (this.column1Fixed_) {
			row = ((row.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (row
					.getWidget(0))
					: null);
		}
		return row;
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

	private void handleClick(WModelIndex index) {
		this
				.selectionHandleClick(index, EnumSet
						.noneOf(KeyboardModifier.class));
		this.clicked_.trigger(index, new WMouseEvent());
	}

	boolean internalSelect(WModelIndex index, SelectionFlag option) {
		if (this.getSelectionBehavior() == SelectionBehavior.SelectRows
				&& index.getColumn() != 0) {
			return this.internalSelect(this.model_.getIndex(index.getRow(), 0,
					index.getParent()), option);
		}
		if (!!EnumUtils.mask(index.getFlags(), ItemFlag.ItemIsSelectable)
				.isEmpty()
				|| this.selectionMode_ == SelectionMode.NoSelection) {
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
				if (this.selectionMode_ == SelectionMode.SingleSelection
						&& option == SelectionFlag.Select) {
					this.clearSelection();
				}
			}
		}
		if (option == SelectionFlag.Select) {
			this.selectionModel_.selection_.add(index);
		} else {
			if (!this.selectionModel_.selection_.remove(index)) {
				return false;
			}
		}
		WModelIndex column0Index = this.model_.getIndex(index.getRow(), 0,
				index.getParent());
		WWidget w = this.widgetForIndex(column0Index);
		WTreeViewNode node = ((w) instanceof WTreeViewNode ? (WTreeViewNode) (w)
				: null);
		if (node != null) {
			node.renderSelected(option == SelectionFlag.Select, index
					.getColumn());
		}
		return true;
	}

	private void selectRange(WModelIndex first, WModelIndex last) {
		this.clearSelection();
		WModelIndex index = first;
		for (;;) {
			for (int c = first.getColumn(); c <= last.getColumn(); ++c) {
				WModelIndex ic = this.model_.getIndex(index.getRow(), c, index
						.getParent());
				this.internalSelect(ic, SelectionFlag.Select);
				if ((ic == last || (ic != null && ic.equals(last)))) {
					return;
				}
			}
			WModelIndex indexc0 = index.getColumn() == 0 ? index : this.model_
					.getIndex(index.getRow(), 0, index.getParent());
			if (this.isExpanded(indexc0)
					&& this.model_.getRowCount(indexc0) > 0) {
				index = this.model_.getIndex(0, first.getColumn(), indexc0);
			} else {
				for (;;) {
					WModelIndex parent = index.getParent();
					if (index.getRow() + 1 < this.model_.getRowCount(parent)) {
						index = this.model_.getIndex(index.getRow() + 1, first
								.getColumn(), parent);
						break;
					} else {
						index = index.getParent();
					}
				}
			}
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
				this.selectRange(top, index);
			} else {
				WModelIndex bottom = this.selectionModel_.selection_.last();
				this.selectRange(index, bottom);
			}
		}
		this.selectionChanged_.trigger();
	}

	private void expandChildrenToDepth(WModelIndex index, int depth) {
		for (int i = 0; i < this.model_.getRowCount(index); ++i) {
			WModelIndex c = this.model_.getIndex(i, 0, index);
			this.expand(c);
			if (depth > 1) {
				this.expandChildrenToDepth(c, depth - 1);
			}
		}
	}

	private void bindObjJS(JSlot slot, String jsMethod) {
		slot.setJavaScript("function(obj, event) {jQuery.data("
				+ this.getJsRef() + ", 'obj')." + jsMethod + "(obj, event);}");
	}

	WContainerWidget getHeaderContainer() {
		return this.headerContainer_;
	}

	WWidget headerWidget(int column, boolean contentsOnly) {
		WWidget result = null;
		if (column == 0) {
			result = this.headers_.getWidget(this.headers_.getCount() - 1);
		} else {
			result = this.getHeaderRow().getWidget(column - 1);
		}
		if (contentsOnly) {
			return result.find("contents");
		} else {
			return result;
		}
	}

	WText headerSortIconWidget(int column) {
		if (!this.columnInfo(column).sorting) {
			return null;
		}
		return ((this.headerWidget(column).find("sort")) instanceof WText ? (WText) (this
				.headerWidget(column).find("sort"))
				: null);
	}

	static String wtjs1(WApplication app) {
		return "Wt3_1_2.WTreeView = function(m,d,i,p,q){function n(c){var a=-1,b=null,e=false,f=false,j=null;for(c=c.target||c.srcElement;c;){if(c.className.indexOf(\"c1 rh\")==0){if(a==-1)a=0}else if(c.className.indexOf(\"Wt-tv-c\")==0){if(c.className.indexOf(\"Wt-tv-c\")==0)a=c.className.split(\" \")[0].substring(7)*1;else if(a==-1)a=0;if(c.getAttribute(\"drop\")===\"true\")f=true;j=c}else if(c.className==\"Wt-tv-node\"){b=c.id;break}if(c.className===\"Wt-selected\")e=true;c=c.parentNode;if(g.hasTag(c,\"BODY\"))break}return{columnId:a, nodeId:b,selected:e,drop:f,el:j}}jQuery.data(d,\"obj\",this);var o=i.firstChild,k=p.firstChild,g=m.WT;this.click=function(c,a){var b=n(a);b.columnId!=-1&&m.emit(d,{name:\"itemEvent\",eventObject:c,event:a},b.nodeId,b.columnId,\"clicked\",\"\",\"\")};this.dblClick=function(c,a){var b=n(a);b.columnId!=-1&&m.emit(d,{name:\"itemEvent\",eventObject:c,event:a},b.nodeId,b.columnId,\"dblclicked\",\"\",\"\")};this.mouseDown=function(c,a){g.capture(null);var b=n(a);if(b.columnId!=-1){m.emit(d,{name:\"itemEvent\",eventObject:c, event:a},b.nodeId,b.columnId,\"mousedown\",\"\",\"\");d.getAttribute(\"drag\")===\"true\"&&b.selected&&m._p_.dragStart(d,a)}};this.mouseUp=function(c,a){var b=n(a);b.columnId!=-1&&m.emit(d,{name:\"itemEvent\",eventObject:c,event:a},b.nodeId,b.columnId,\"mouseup\",\"\",\"\")};this.resizeHandleMDown=function(c,a){a=g.pageCoordinates(a);c.setAttribute(\"dsx\",a.x)};this.resizeHandleMMoved=function(c,a){var b=c.getAttribute(\"dsx\"),e=k.lastChild.className.split(\" \")[0];g.getCssRule(\"#\"+d.id+\" .\"+e);if(b!=null&&b!=\"\"){e=g.pageCoordinates(a); var f=c.parentNode.parentNode;b=Math.max(e.x-b,-f.offsetWidth);if(f=f.className.split(\" \")[0]){f=g.getCssRule(\"#\"+d.id+\" .\"+f);var j=g.pxself(f,\"width\");f.style.width=Math.max(0,j+b)+\"px\"}this.adjustColumns();c.setAttribute(\"dsx\",e.x);g.cancelEvent(a)}};this.resizeHandleMUp=function(c,a){c.removeAttribute(\"dsx\");g.cancelEvent(a)};this.adjustColumns=function(){var c=o.firstChild,a=k.firstChild,b=0,e=0;e=k.lastChild.className.split(\" \")[0];e=g.getCssRule(\"#\"+d.id+\" .\"+e);if(q)a=a.firstChild;if(!g.isHidden(d)){for(var f= 0,j=a.childNodes.length;f<j;++f)if(a.childNodes[f].className){var h=a.childNodes[f].className.split(\" \")[0];h=g.getCssRule(\"#\"+d.id+\" .\"+h);b+=g.pxself(h,\"width\")+7}if(!e.style.width)e.style.width=k.offsetWidth-a.offsetWidth-8+\"px\";e=b+g.pxself(e,\"width\")+(g.isIE6?10:8);if(q){h=g.getCssRule(\"#\"+d.id+\" .Wt-tv-rowc\");h.style.width=b+\"px\";$(d).find(\" .Wt-tv-rowc\").css(\"width\",b+\"px\").css(\"width\",\"\");d.changed=true;this.autoJavaScript()}else{k.style.width=c.style.width=e+\"px\";a.style.width=b+\"px\"}}}; var l=null;d.handleDragDrop=function(c,a,b,e,f){if(l){l.className=l.classNameOrig;l=null}if(c!=\"end\"){b=n(b);if(!b.selected&&b.drop&&b.columnId!=-1)if(c==\"drop\")m.emit(d.id,\"itemEvent\",b.nodeId,b.columnId,\"drop\",e,f);else{a.className=\"Wt-valid-drop\";l=b.el;l.classNameOrig=l.className;l.className+=\" Wt-drop-site\"}else a.className=\"\"}};this.autoJavaScript=function(){if(d.parentNode==null){d=i=p=o=k=null;this.autoJavaScript=function(){}}else if(!g.isHidden(d)){var c=$(d),a=c.innerWidth(),b=i.scrollHeight> i.offsetHeight,e,f=null;if(c.hasClass(\"column1\")){e=c.find(\".Wt-headerdiv\").get(0).lastChild.className.split(\" \")[0];e=g.getCssRule(\"#\"+d.id+\" .\"+e);f=g.pxself(e,\"width\")}if((!g.isIE||a>100)&&(a!=i.tw||b!=i.vscroll||f!=i.c0w||d.changed)){i.tw=a;i.vscroll=b;i.c0w=f;e=c.find(\".Wt-headerdiv\").get(0).lastChild.className.split(\" \")[0];e=g.getCssRule(\"#\"+d.id+\" .\"+e);var j=o.firstChild,h=g.getCssRule(\"#\"+d.id+\" .cwidth\"),r=h.style.width==k.style.width,s=k.firstChild;h.style.width=a-(b?19:0)+\"px\";i.style.width= a+\"px\";k.style.width=j.offsetWidth+\"px\";if(f!=null){f=a-f-(g.isIE6?10:8)-(b?19:0);if(f>0){h=Math.min(f,g.pxself(g.getCssRule(\"#\"+d.id+\" .Wt-tv-rowc\"),\"width\"));a-=f-h;g.getCssRule(\"#\"+d.id+\" .Wt-tv-row\").style.width=h+\"px\";c.find(\" .Wt-tv-row\").css(\"width\",h+\"px\").css(\"width\",\"\");a-=b?19:0;k.style.width=a+\"px\";j.style.width=a+\"px\"}}else if(r){k.style.width=h.style.width;j.style.width=h.style.width}e.style.width=j.offsetWidth-s.offsetWidth-8+\"px\";d.changed=false}}};this.adjustColumns()};";
	}

	static boolean isAncestor(WModelIndex i1, WModelIndex i2) {
		if (!(i1 != null)) {
			return false;
		}
		for (WModelIndex p = i1.getParent(); (p != null); p = p.getParent()) {
			if ((p == i2 || (p != null && p.equals(i2)))) {
				return true;
			}
		}
		return !(i2 != null);
	}
}
