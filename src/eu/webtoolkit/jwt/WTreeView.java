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
import eu.webtoolkit.jwt.utils.StringUtils;

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
 * method {@link WTreeView#setColumnWidth(int column, WLength width)}, and also
 * by the user using handles provided in the header.
 * <p>
 * Optionally, the treeview may be configured so that the first column is always
 * visible while scrolling through the other columns, which may be convenient if
 * you wish to display a model with many columns. Use
 * {@link WTreeView#setColumn1Fixed(boolean fixed)} to enable this behaviour.
 * <p>
 * If the model supports sorting (
 * {@link WAbstractItemModel#sort(int column, SortOrder order)}), such as the
 * {@link WStandardItemModel}, then you can enable sorting buttons in the
 * header, using {@link WTreeView#setSortingEnabled(boolean enabled)}.
 * <p>
 * You can allow selection on row or item level (using
 * {@link WTreeView#setSelectionBehavior(SelectionBehavior behavior)}), and
 * selection of single or multiple items (using
 * {@link WTreeView#setSelectionMode(SelectionMode mode)}), and listen for
 * changes in the selection using the {@link WTreeView#selectionChanged()}
 * signal.
 * <p>
 * You may enable drag &amp; drop support for this view, whith awareness of the
 * items in the model. When enabling dragging (see
 * {@link WTreeView#setDragEnabled(boolean enable)}), the current selection may
 * be dragged, but only when all items in the selection indicate support for
 * dragging (controlled by the {@link ItemFlag#ItemIsDragEnabled
 * ItemIsDragEnabled} flag), and if the model indicates a mime-type (controlled
 * by {@link WAbstractItemModel#getMimeType()}). Likewise, by enabling support
 * for dropping (see {@link WTreeView#setDropsEnabled(boolean enable)}), the
 * treeview may receive a drop event on a particular item, at least if the item
 * indicates support for drops (controlled by the
 * {@link ItemFlag#ItemIsDropEnabled ItemIsDropEnabled} flag).
 * <p>
 * You may also react to mouse click events on any item, by connecting to one of
 * the {@link WTreeView#clicked()} or {@link WTreeView#doubleClicked()} signals.
 */
public class WTreeView extends WCompositeWidget {
	/**
	 * Create a new tree view.
	 */
	public WTreeView(WContainerWidget parent) {
		super((WContainerWidget) null);
		this.model_ = null;
		this.itemDelegate_ = new WItemDelegate(this);
		this.selectionModel_ = new WItemSelectionModel(
				(WAbstractItemModel) null, this);
		this.rootIndex_ = null;
		this.rowHeight_ = new WLength(20);
		this.headerHeight_ = new WLength(20);
		this.expandedSet_ = new TreeSet<WModelIndex>();
		this.renderedNodes_ = new HashMap<WModelIndex, WTreeViewNode>();
		this.rootNode_ = null;
		this.imagePack_ = "";
		this.borderColorRule_ = null;
		this.alternatingRowColors_ = false;
		this.rootIsDecorated_ = true;
		this.selectionMode_ = SelectionMode.NoSelection;
		this.sorting_ = true;
		this.columnResize_ = true;
		this.multiLineHeader_ = false;
		this.column1Fixed_ = false;
		this.expandedRaw_ = new ArrayList<Object>();
		this.selectionRaw_ = new ArrayList<Object>();
		this.columns_ = new ArrayList<WTreeView.ColumnInfo>();
		this.nextColumnId_ = 1;
		this.collapsed_ = new Signal1<WModelIndex>(this);
		this.expanded_ = new Signal1<WModelIndex>(this);
		this.clicked_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.doubleClicked_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.mouseWentDown_ = new Signal2<WModelIndex, WMouseEvent>(this);
		this.selectionChanged_ = new Signal(this);
		this.viewportTop_ = 0;
		this.viewportHeight_ = 30;
		this.nodeLoad_ = 0;
		this.currentSortColumn_ = -1;
		this.contentsContainer_ = null;
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.resizeHandleMDownJS_ = new JSlot(this);
		this.resizeHandleMMovedJS_ = new JSlot(this);
		this.resizeHandleMUpJS_ = new JSlot(this);
		this.tieContentsHeaderScrollJS_ = new JSlot(this);
		this.tieRowsScrollJS_ = new JSlot(this);
		this.itemClickedJS_ = new JSlot(this);
		this.itemDoubleClickedJS_ = new JSlot(this);
		this.itemMouseDownJS_ = new JSlot(this);
		this.itemEvent_ = new JSignal6<String, Integer, String, String, String, WMouseEvent>(
				this, "itemEvent") {
		};
		this.dragEnabled_ = false;
		this.dropsEnabled_ = false;
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.renderState_ = WTreeView.RenderState.NeedRerender;
		WApplication app = WApplication.getInstance();
		this.clickedForSortMapper_ = new WSignalMapper1<Integer>(this);
		this.clickedForSortMapper_.mapped().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WTreeView.this.toggleSortColumn(e1);
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
		this.imagePack_ = WApplication.getResourcesUrl();
		String CSS_RULES_NAME = "Wt::WTreeView";
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview",
							"font-family: verdana,helvetica,tahoma,sans-serif;font-size: 10pt;cursor: default;",
							CSS_RULES_NAME);
			app.getStyleSheet().addRule(".Wt-treeview .spacer",
					"background: url(" + this.imagePack_ + "loading.png);");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .header-div",
							"-moz-user-select: none;-khtml-user-select: none;user-select: none;background-color: #EEEEEE;overflow: hidden;width: 100%;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .header .Wt-label",
							""
									+ "white-space: normal;font-weight: bold;text-overflow: ellipsis;"
									+ (app.getEnvironment().agentIsIE() ? "zoom: 1;"
											: "") + "overflow: hidden;");
			app.getStyleSheet().addRule(
					".Wt-treeview .Wt-trunk",
					"background: url(" + this.imagePack_
							+ "line-trunk.gif) repeat-y;");
			app.getStyleSheet().addRule(
					".Wt-treeview .Wt-end",
					"background: url(" + this.imagePack_
							+ "tv-line-last.gif) no-repeat 0 center;");
			app.getStyleSheet().addRule(".Wt-treeview table", "width: 100%");
			app.getStyleSheet().addRule(".Wt-treeview td.c1", "width: 100%");
			app.getStyleSheet().addRule(".Wt-treeview td.c0",
					"width: 1px; vertical-align: middle");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-row",
					"float: right; overflow: hidden;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .Wt-tv-c",
							"display: block; float: left;padding: 0px 3px;text-overflow: ellipsis;overflow: hidden;white-space: nowrap;");
			app.getStyleSheet().addRule(".Wt-treeview img.icon",
					"margin-right: 3px; vertical-align: middle");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-node img.w0",
					"width: 0px");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-node .c0 img",
					"margin-right: 0px;");
			app
					.getStyleSheet()
					.addRule(".Wt-treeview div.Wt-tv-rh",
							"float: right; width: 4px; cursor: col-resize;padding-left: 0px;");
			if (app.getEnvironment().agentIsIE()) {
				app.getStyleSheet().addRule(".Wt-treeview .header .Wt-tv-c",
						"padding: 0px;padding-left: 6px;");
			} else {
				app.getStyleSheet().addRule(".Wt-treeview .header .Wt-tv-c",
						"padding: 0px;margin-left: 6px;");
			}
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-rh:hover",
					"background-color: #DDDDDD;");
			app.getStyleSheet().addRule(".Wt-treeview div.Wt-tv-rhc0",
					"float: left; width: 4px;");
			app
					.getStyleSheet()
					.addRule(".Wt-treeview .Wt-tv-sh",
							"float: right; width: 16px; margin-top: 6px;cursor: pointer; cursor:hand;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .Wt-tv-sh-nrh",
							"float: right; width: 16px; margin-top: 6px; margin-right: 4pxcursor: pointer; cursor:hand;");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-shc0",
					"float: left;");
			app.getStyleSheet().addRule(".Wt-treeview .selected",
					"background-color: #FFFFAA;");
			app.getStyleSheet().addRule(".Wt-treeview .drop-site",
					"background-color: #EEEEEE;outline: 1px dotted black;");
			app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-row .Wt-tv-c",
					"border-right: 1px solid;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-treeview .header .Wt-tv-row, .Wt-treeview .Wt-tv-node .Wt-tv-row",
							"border-left: 1px solid;");
			this.setColumnBorder(WColor.white);
			if (app.getEnvironment().agentIsWebKit()
					|| app.getEnvironment().agentIsOpera()) {
				app.getStyleSheet().addRule(".Wt-treeview .Wt-tv-rowc",
						"position: relative;");
			}
			if (app.getEnvironment().agentIsIE()) {
				app.getStyleSheet().addRule(".Wt-treeview .Wt-scroll",
						"overflow-x: scroll; height: 16px;");
			} else {
				app.getStyleSheet().addRule(".Wt-treeview .Wt-scroll",
						"overflow: scroll; height: 16px;");
			}
			app.getStyleSheet().addRule(".Wt-treeview .Wt-scroll div",
					"height: 1px;");
		}
		app.getStyleSheet().addRule("#" + this.getFormName() + " .cwidth",
				"height: 1px;");
		app.getStyleSheet().addRule(
				"#" + this.getFormName() + "dw",
				"width: 32px; height: 32px;background: url(" + this.imagePack_
						+ "items-not-ok.gif);");
		app.getStyleSheet().addRule(
				"#" + this.getFormName() + "dw.valid-drop",
				"width: 32px; height: 32px;background: url(" + this.imagePack_
						+ "items-ok.gif);");
		this.rowHeightRule_ = new WCssTemplateRule("#" + this.getFormName()
				+ " .rh");
		app.getStyleSheet().addRule(this.rowHeightRule_);
		this.rowWidthRule_ = new WCssTemplateRule("#" + this.getFormName()
				+ " .Wt-tv-row");
		app.getStyleSheet().addRule(this.rowWidthRule_);
		this.rowContentsWidthRule_ = new WCssTemplateRule("#"
				+ this.getFormName() + " .Wt-tv-rowc");
		app.getStyleSheet().addRule(this.rowContentsWidthRule_);
		this.c0WidthRule_ = new WCssTemplateRule("#" + this.getFormName()
				+ " .c0w");
		this.c0WidthRule_.getTemplateWidget().resize(new WLength(150),
				WLength.Auto);
		app.getStyleSheet().addRule(this.c0WidthRule_);
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
		this.headerContainer_.setStyleClass("header headerrh cwidth");
		this.headers_ = new WContainerWidget(this.headerContainer_);
		this.headers_.setStyleClass("header-div headerrh");
		this.headers_.setSelectable(false);
		this.headerHeightRule_ = new WCssTemplateRule("#" + this.getFormName()
				+ " .headerrh");
		app.getStyleSheet().addRule(this.headerHeightRule_);
		this.setHeaderHeight(this.headerHeight_);
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
		app
				.declareJavaScriptFunction(
						"getItem",
						"function(event) {var columnId = -1, nodeId = null, selected = false, drop = false, el = null;var t = event.target || event.srcElement;while (t) {if (t.className.indexOf('c1 rh') == 0) {if (columnId == -1)columnId = 0;} else if (t.className.indexOf('Wt-tv-c') == 0) {if (t.className.indexOf('Wt-tv-c rh Wt-tv-c') == 0)columnId = t.className.split(' ')[2].substring(7) * 1;else if (columnId == -1)columnId = 0;if (t.getAttribute('drop') === 'true')drop = true;el = t;} else if (t.className == 'Wt-tv-node') {nodeId = t.id;break;}if (t.className === 'selected')selected = true;t = t.parentNode;if (Wt2_99_2.hasTag(t, 'BODY'))break;}return { columnId: columnId, nodeId: nodeId, selected: selected, drop: drop, el: el };}");
		this.itemClickedJS_.setJavaScript("function(obj, event) {var item="
				+ app.getJavaScriptClass()
				+ ".getItem(event);if (item.columnId != -1) {"
				+ this.itemEvent_
						.createEventCall("item.el", "event", "item.nodeId",
								"item.columnId", "'clicked'", "''", "''")
				+ ";}}");
		this.itemDoubleClickedJS_
				.setJavaScript("function(obj, event) {var item="
						+ app.getJavaScriptClass()
						+ ".getItem(event);if (item.columnId != -1)"
						+ this.itemEvent_.createEventCall("item.el", "event",
								"item.nodeId", "item.columnId", "'dblclicked'",
								"''", "''") + ";}");
		this.itemMouseDownJS_
				.setJavaScript("function(obj, event) {var APP="
						+ app.getJavaScriptClass()
						+ ", tv="
						+ this.getJsRef()
						+ ";APP._p_.capture(null);var item=APP.getItem(event);if (item.columnId != -1) {"
						+ this.itemEvent_.createEventCall("item.el", "event",
								"item.nodeId", "item.columnId", "'mousedown'",
								"''", "''")
						+ ";if (tv.getAttribute('drag') === 'true' && item.selected)APP._p_.dragStart(tv, event);}}");
		this.resizeHandleMDownJS_
				.setJavaScript("function(obj, event) {var pc = Wt2_99_2.pageCoordinates(event);obj.setAttribute('dsx', pc.x);}");
		this.resizeHandleMMovedJS_
				.setJavaScript("function(obj, event) {var WT = Wt2_99_2,lastx = obj.getAttribute('dsx'),t = "
						+ this.contents_.getJsRef()
						+ ".firstChild,h="
						+ this.headers_.getJsRef()
						+ ",hh=h.firstChild;if (lastx != null && lastx != '') {nowxy = WT.pageCoordinates(event);var parent = obj.parentNode,diffx = Math.max(nowxy.x - lastx, -parent.offsetWidth),c = parent.className.split(' ')[2];if (c) {var r = WT.getCssRule('#"
						+ this.getFormName()
						+ " .' + c),tw = WT.pxself(r, 'width');if (tw == 0) tw = parent.offsetWidth;r.style.width = (tw + diffx) + 'px';}"
						+ this.getJsRef()
						+ ".adjustHeaderWidth(c, diffx);obj.setAttribute('dsx', nowxy.x);WT.cancelEvent(event);  }}");
		this.resizeHandleMUpJS_
				.setJavaScript("function(obj, event) {obj.removeAttribute('dsx');Wt2_99_2.cancelEvent(event);}");
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
					.setJavaScript("function(obj, event) {Wt2_99_2.getCssRule('#"
							+ this.getFormName()
							+ " .Wt-tv-rowc').style.left= -obj.scrollLeft + 'px';}");
		} else {
			this.tieRowsScrollJS_
					.setJavaScript("function(obj, event) {var c =Wt2_99_2.getElementsByClassName('Wt-tv-rowc', "
							+ this.getJsRef()
							+ ");for (var i = 0, length = c.length; i < length; ++i) {var cc=c[i];if (cc.parentNode.scrollLeft != obj.scrollLeft)cc.parentNode.scrollLeft=obj.scrollLeft;}}");
		}
		app
				.addAutoJavaScript("{var e="
						+ this.contentsContainer_.getJsRef()
						+ ";var s="
						+ this.getJsRef()
						+ ";var WT=Wt2_99_2;if (e) {var tw=s.offsetWidth-WT.px(s, 'borderLeftWidth')-WT.px(s, 'borderRightWidth');if (tw > 200) {var h= "
						+ this.headers_.getJsRef()
						+ ",hh=h.firstChild,t="
						+ this.contents_.getJsRef()
						+ ".firstChild,r= WT.getCssRule('#"
						+ this.getFormName()
						+ " .cwidth'),vscroll=e.scrollHeight > e.offsetHeight,contentstoo=(r.style.width == h.style.width);if (vscroll) {r.style.width=(tw-17) + 'px';} else {r.style.width=tw + 'px';}e.style.width=tw + 'px';h.style.width=t.offsetWidth + 'px';if (s.className.indexOf('column1') != -1) {var r=WT.getCssRule('#"
						+ this.getFormName()
						+ " .c0w'),hh=h.firstChild,w=tw - WT.pxself(r, 'width') - (vscroll ? 17 : 0);WT.getCssRule('#"
						+ this.getFormName()
						+ " .Wt-tv-row').style.width= w + 'px';var extra = hh.childNodes.length > 1? (WT.hasTag(hh.childNodes[1], 'IMG') ? 21 : 6) : 0;hh.style.width= (w + extra) + 'px';} else if (contentstoo) {h.style.width=r.style.width;t.style.width=r.style.width;}if (s.adjustHeaderWidth)s.adjustHeaderWidth(1, 0);}}}");
		if (parent != null) {
			parent.addWidget(this);
		}
	}

	/**
	 * Create a new tree view.
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
	 * Sets the model.
	 * <p>
	 * The view will render the data in the given <i>model</i>. Changes to the
	 * model are reflected in the view.
	 * <p>
	 * When resetting a model, all nodes are initially collapsed, the selection
	 * is cleared, and the root index corresponds to the model&apos;s top level
	 * node (see {@link WTreeView#setRootIndex(WModelIndex rootIndex)}).
	 * <p>
	 * The initial model is 0.
	 * <p>
	 * Ownership of the model is not transferred (and thus the previously set
	 * model is not deleted).
	 * <p>
	 * 
	 * @see WTreeView#setRootIndex(WModelIndex rootIndex)
	 */
	public void setModel(WAbstractItemModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
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
		WItemSelectionModel oldSelectionModel = this.selectionModel_;
		this.selectionModel_ = new WItemSelectionModel(model, this);
		this.selectionModel_.setSelectionBehavior(oldSelectionModel
				.getSelectionBehavior());
		this.expandedSet_.clear();
		for (int i = this.columns_.size(); i < this.model_.getColumnCount(); ++i) {
			this.columnInfo(i);
		}
		while ((int) this.columns_.size() > model.getColumnCount()) {
			if (this.columns_.get(this.columns_.size() - 1).styleRule != null)
				this.columns_.get(this.columns_.size() - 1).styleRule.remove();
			this.columns_.remove(0 + this.columns_.size() - 1);
		}
		this.configureModelDragDrop();
		this.scheduleRerender(WTreeView.RenderState.NeedRerender);
	}

	/**
	 * Returns the model.
	 * <p>
	 * 
	 * @see WTreeView#setModel(WAbstractItemModel model)
	 */
	public WAbstractItemModel getModel() {
		return this.model_;
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
				this.scheduleRerender(WTreeView.RenderState.NeedRerenderTree);
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
	 * Sets the header height.
	 * <p>
	 * Use this method to change the header height. You may also enable the use
	 * of multi-line headers. By default, the header text is a single line, that
	 * is centered vertically.
	 * <p>
	 * The default value is 20 pixels.
	 */
	public void setHeaderHeight(WLength height, boolean multiLine) {
		this.headerHeight_ = height;
		this.multiLineHeader_ = multiLine;
		this.headerHeightRule_.getTemplateWidget().resize(WLength.Auto,
				this.headerHeight_);
		if (!this.multiLineHeader_) {
			this.headerHeightRule_.getTemplateWidget().setLineHeight(
					this.headerHeight_);
		} else {
			this.headerHeightRule_.getTemplateWidget().setLineHeight(
					WLength.Auto);
		}
		this.headers_.resize(this.headers_.getWidth(), this.headerHeight_);
		this.headerContainer_.resize(WLength.Auto, this.headerHeight_);
		if (this.renderState_.getValue() >= WTreeView.RenderState.NeedRerenderHeader
				.getValue()) {
			return;
		}
		if (!WApplication.getInstance().getEnvironment().agentIsIE()) {
			for (int i = 1; i < this.getColumnCount(); ++i) {
				this.headerTextWidget(i).setWordWrap(multiLine);
			}
		}
	}

	/**
	 * Sets the header height.
	 * <p>
	 * Calls {@link #setHeaderHeight(WLength height, boolean multiLine)
	 * setHeaderHeight(height, false)}
	 */
	public final void setHeaderHeight(WLength height) {
		setHeaderHeight(height, false);
	}

	/**
	 * Returns the header height.
	 * <p>
	 * 
	 * @see WTreeView#setHeaderHeight(WLength height, boolean multiLine)
	 */
	public WLength getHeaderHeight() {
		return this.headerHeight_;
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
	 * <i><b>Note:</b>The height must be specified in {@link WLength.Unit#Pixel}
	 * units.</i>
	 * </p>
	 * 
	 * @see WTreeView#setColumnWidth(int column, WLength width)
	 */
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
			this.scheduleRerender(WTreeView.RenderState.NeedAdjustViewPort);
		}
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
	 * For a model with
	 * {@link WAbstractItemModel#getColumnCount(WModelIndex parent)
	 * columnCount()} == <i>N</i>, the initial width of columns 1..<i>N</i> is
	 * set to 150 pixels, and column 0 will take all remaining space.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>The actual space occupied by each column is the column
	 * width augmented by 7 pixels for internal padding and a border.</i>
	 * </p>
	 * 
	 * @see WTreeView#setRowHeight(WLength rowHeight)
	 */
	public void setColumnWidth(int column, WLength width) {
		this.columnInfo(column).width = width;
		if (column != 0) {
			this.columnInfo(column).styleRule.getTemplateWidget().resize(width,
					WLength.Auto);
		} else {
			if (this.column1Fixed_) {
				this.c0WidthRule_.getTemplateWidget().resize(
						new WLength(width.toPixels()), WLength.Auto);
			}
		}
		if (!this.column1Fixed_ && !this.columnInfo(0).width.isAuto()) {
			double total = 0;
			for (int i = 0; i < this.getColumnCount(); ++i) {
				total += this.columnInfo(i).width.toPixels() + 7;
			}
			this.headers_.resize(new WLength(total), this.headers_.getHeight());
			WContainerWidget wrapRoot = ((this.contents_.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (this.contents_
					.getWidget(0))
					: null);
			wrapRoot.resize(new WLength(total), wrapRoot.getHeight());
		}
	}

	/**
	 * Returns the column width.
	 * <p>
	 * 
	 * @see WTreeView#setColumnWidth(int column, WLength width)
	 */
	public WLength getColumnWidth(int column) {
		return this.columnInfo(column).width;
	}

	/**
	 * Sets the content alignment for a column.
	 * <p>
	 * The default value is {@link AlignmentFlag#AlignLeft AlignLeft}.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>For column 0, {@link AlignmentFlag#AlignCenter
	 * AlignCenter} is currently not supported.</i>
	 * </p>
	 * 
	 * @see WTreeView#setHeaderAlignment(int column, AlignmentFlag alignment)
	 */
	public void setColumnAlignment(int column, AlignmentFlag alignment) {
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

	/**
	 * Sets the header alignment for a column.
	 * <p>
	 * The default value is {@link AlignmentFlag#AlignLeft AlignLeft}.
	 * <p>
	 * 
	 * @see WTreeView#setColumnAlignment(int column, AlignmentFlag alignment)
	 */
	public void setHeaderAlignment(int column, AlignmentFlag alignment) {
		this.columnInfo(column).headerAlignment = alignment;
		if (this.renderState_.getValue() >= WTreeView.RenderState.NeedRerenderHeader
				.getValue()) {
			return;
		}
		WContainerWidget wc;
		if (column != 0) {
			wc = ((this.headerWidget(column)) instanceof WContainerWidget ? (WContainerWidget) (this
					.headerWidget(column))
					: null);
		} else {
			wc = this.headers_;
		}
		wc.setContentAlignment(EnumSet.of(alignment));
	}

	/**
	 * Returns the content alignment for a column.
	 * <p>
	 * 
	 * @see WTreeView#setColumnAlignment(int column, AlignmentFlag alignment)
	 */
	public AlignmentFlag getColumnAlignment(int column) {
		return this.columnInfo(column).alignment;
	}

	/**
	 * Returns the header alignment for a column.
	 * <p>
	 * 
	 * @see WTreeView#setHeaderAlignment(int column, AlignmentFlag alignment)
	 */
	public AlignmentFlag getHeaderAlignment(int column) {
		return this.columnInfo(column).headerAlignment;
	}

	/**
	 * Setss the column border color.
	 * <p>
	 * The default border color is WColor.white.
	 */
	public void setColumnBorder(WColor color) {
		if (this.borderColorRule_ != null)
			this.borderColorRule_.remove();
		this.borderColorRule_ = new WCssTextRule(
				".Wt-treeview .Wt-tv-row .Wt-tv-c, .Wt-treeview .header .Wt-tv-row, .Wt-treeview .Wt-tv-node .Wt-tv-row",
				"border-color: " + color.getCssText());
		WApplication.getInstance().getStyleSheet().addRule(
				this.borderColorRule_);
	}

	/**
	 * Setss the base urls for icons.
	 * <p>
	 * This widget relies on several icons that are distributed together with Wt
	 * for drawing icons, lines, and backgrounds.
	 * <p>
	 * The default location for the image pack is <i>resourcesURL</i>.
	 * <p>
	 * The default value for <i>resourcesURL</i> is &quot;resources/&quot;. This
	 * value may be overridden with a URL that points to a folder where these
	 * files are located, by configuring the <i>resourcesURL</i> property in
	 * your Wt configuration file.
	 */
	public void setImagePack(String uri) {
	}

	/**
	 * Returns the base url for icons.
	 * <p>
	 * 
	 * @see WTreeView#setImagePack(String uri)
	 */
	public String getImagePack() {
		return this.imagePack_;
	}

	/**
	 * Expand or collapse a node.
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
	 * Collapse a node.
	 * <p>
	 * 
	 * @see WTreeView#setExpanded(WModelIndex index, boolean expanded)
	 * @see WTreeView#expand(WModelIndex index)
	 */
	public void collapse(WModelIndex index) {
		this.setExpanded(index, false);
	}

	/**
	 * Expand a node.
	 * <p>
	 * 
	 * @see WTreeView#setExpanded(WModelIndex index, boolean expanded)
	 * @see WTreeView#collapse(WModelIndex index)
	 */
	public void expand(WModelIndex index) {
		this.setExpanded(index, true);
	}

	/**
	 * Expand all nodes to a depth.
	 * <p>
	 * Expands all nodes to the given <i>depth</i>. A depth of 1 corresponds to
	 * the top level nodes.
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
	 * Sets if alternating row colors are to be used.
	 * <p>
	 * Configure whether rows get an alternating background color. These are
	 * implemented by using a background image on the root node, like: <div
	 * align="center"> <img src="doc-files//stripe-30px.gif"
	 * alt="Sample image use for alternating row colors">
	 * <p>
	 * <strong>Sample image use for alternating row colors</strong>
	 * </p>
	 * </div> The image that is used is {@link WTreeView#getImagePack()} +
	 * &quot;/stripes/stripe-&lt;i&gt;n&lt;/i&gt;px.gif&quot;, where <i>n</i> is
	 * the row height. In the resource folder are images pregenerated for one
	 * color and row sizes from 10 to 30px.
	 * <p>
	 * The default value is false.
	 * <p>
	 * 
	 * @see WTreeView#setImagePack(String uri)
	 */
	public void setAlternatingRowColors(boolean enable) {
		if (this.alternatingRowColors_ != enable) {
			this.alternatingRowColors_ = enable;
			this.setRootNodeStyle();
		}
	}

	/**
	 * Returns whether alternating row colors are used.
	 * <p>
	 * 
	 * @see WTreeView#setAlternatingRowColors(boolean enable)
	 */
	public boolean hasAlternatingRowColors() {
		return this.alternatingRowColors_;
	}

	/**
	 * Sets whether toplevel items are decorated.
	 * <p>
	 * By default, top level nodes have expand/collapse and other lines to
	 * display their linkage and offspring, like any node.
	 * <p>
	 * By setting <i>show</i> to false, you can hide these decorations for root
	 * nodes, and in this way mimic a plain list.
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
	 * Sort the data according to a column.
	 * <p>
	 * Sorts the data according to data in column <i>column</i> and sort order
	 * <i>order</i>.
	 * <p>
	 * 
	 * @see WAbstractItemModel#sort(int column, SortOrder order)
	 */
	public void sortByColumn(int column, SortOrder order) {
		if (this.currentSortColumn_ != -1) {
			this.headerSortIconWidget(this.currentSortColumn_).setImageRef(
					this.imagePack_ + "sort-arrow-none.gif");
		}
		this.currentSortColumn_ = column;
		this.columnInfo(column).sortOrder = order;
		if (this.renderState_ != WTreeView.RenderState.NeedRerender) {
			this
					.headerSortIconWidget(this.currentSortColumn_)
					.setImageRef(
							this.imagePack_
									+ (order == SortOrder.AscendingOrder ? "sort-arrow-up.gif"
											: "sort-arrow-down.gif"));
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
		this.scheduleRerender(WTreeView.RenderState.NeedRerenderHeader);
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
		this.scheduleRerender(WTreeView.RenderState.NeedRerenderHeader);
	}

	/**
	 * Returns whether sorting is enabled.
	 * <p>
	 * 
	 * @see WTreeView#setSortingEnabled(boolean enabled)
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
	 * @see WTreeView#setColumnResizeEnabled(boolean enabled)
	 */
	public void setColumnResizeEnabled(boolean enabled) {
		if (enabled != this.columnResize_) {
			this.columnResize_ = enabled;
			this.scheduleRerender(WTreeView.RenderState.NeedRerenderHeader);
		}
	}

	/**
	 * Returns whether column resizing is enabled.
	 * <p>
	 * 
	 * @see WTreeView#setColumnResizeEnabled(boolean enabled)
	 */
	public boolean isColumnResizeEnabled() {
		return this.columnResize_;
	}

	/**
	 * Change the selection behaviour.
	 * <p>
	 * The selection behavior indicates whether whole rows or individual items
	 * can be selected. It is a property of the
	 * {@link WTreeView#getSelectionModel()}.
	 * <p>
	 * By default, selection operates on rows (
	 * {@link SelectionBehavior#SelectRows SelectRows}), in which case model
	 * indexes will always be in the first column (column 0).
	 * <p>
	 * Alternatively, you can allow selection for individual items (
	 * {@link SelectionBehavior#SelectItems SelectItems}).
	 * <p>
	 * 
	 * @see WItemSelectionModel#setSelectionBehavior(SelectionBehavior behavior)
	 * @see WTreeView#setSelectionMode(SelectionMode mode)
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
	 * @see WTreeView#setSelectionBehavior(SelectionBehavior behavior)
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
	 * @see WTreeView#setSelectionBehavior(SelectionBehavior behavior)
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
	 * @see WTreeView#setSelectionMode(SelectionMode mode)
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
	 * Replaces the current selection with <i>indexes</i>.
	 * <p>
	 * 
	 * @see WTreeView#select(WModelIndex index, SelectionFlag option)
	 * @see WTreeView#getSelectionModel()
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
	 * @see WTreeView#setSelectedIndexes(SortedSet indexes)
	 * @see WTreeView#getSelectionModel()
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
	 * This is a convenience method for: <code>
   selectionModel()-&gt;isSelected(index)
  </code>
	 * <p>
	 * 
	 * @see WTreeView#getSelectedIndexes()
	 * @see WTreeView#select(WModelIndex index, SelectionFlag option)
	 * @see WTreeView#getSelectionModel()
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
	 * This is a convenience method for: <code>
   selectionModel()-&gt;selectedIndexes()
  </code>
	 * <p>
	 * 
	 * @see WTreeView#setSelectedIndexes(SortedSet indexes)
	 */
	public SortedSet<WModelIndex> getSelectedIndexes() {
		return this.selectionModel_.selection_;
	}

	/**
	 * Enable the selection to be dragged (drag &amp; drop).
	 * <p>
	 * To enable dragging of the selection, you first need to enable selection
	 * using {@link WTreeView#setSelectionMode(SelectionMode mode)}.
	 * <p>
	 * Whether an individual item may be dragged is controlled by the
	 * item&apos;s {@link ItemFlag#ItemIsDragEnabled ItemIsDragEnabled} flag.
	 * The selection can be dragged only if all items currently selected can be
	 * dragged.
	 * <p>
	 * 
	 * @see WTreeView#setDropsEnabled(boolean enable)
	 */
	public void setDragEnabled(boolean enable) {
		if (this.dragEnabled_ != enable) {
			this.dragEnabled_ = enable;
			if (enable) {
				this.dragWidget_ = new WText(this.headerContainer_);
				this.dragWidget_.setId(this.getFormName() + "dw");
				this.dragWidget_.setInline(false);
				this.dragWidget_.hide();
				this.setAttributeValue("dwid", this.dragWidget_.getFormName());
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
	 * {@link WAbstractItemModel#getAcceptDropMimeTypes()}) or this
	 * widget&apos;s accepted drop mime-types (see
	 * {@link WWidget#acceptDrops(String mimeType, String hoverStyleClass)}),
	 * and the target item has drop enabled (which is controlled by the
	 * item&apos;s {@link ItemFlag#ItemIsDropEnabled ItemIsDropEnabled} flag).
	 * <p>
	 * Drop events must be handled in
	 * {@link WTreeView#dropEvent(WDropEvent e, WModelIndex index)}.
	 * <p>
	 * 
	 * @see WTreeView#setDragEnabled(boolean enable)
	 * @see WTreeView#dropEvent(WDropEvent e, WModelIndex index)
	 */
	public void setDropsEnabled(boolean enable) {
		if (this.dropsEnabled_ != enable) {
			this.dropsEnabled_ = enable;
			this.configureModelDragDrop();
		}
	}

	/**
	 * Configure whether horizontal scrolling includes the first column.
	 * <p>
	 * To display a model with many columns, this option allows you to keep the
	 * first column fixed while scrolling through the other columns of the
	 * model.
	 * <p>
	 * The default value is false.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>Currently, you must set this option before any other
	 * manipulation of a newly created treeview, and only <i>enable</i> =
	 * <i>true</i> is supported. </i>
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
			scrollBarContainer.resize(WLength.Auto, new WLength(16));
			WContainerWidget scrollBarC = new WContainerWidget(
					scrollBarContainer);
			scrollBarC.setStyleClass("Wt-tv-row Wt-scroll");
			scrollBarC.scrolled().addListener(this.tieRowsScrollJS_);
			WContainerWidget scrollBar = new WContainerWidget(scrollBarC);
			scrollBar.setStyleClass("Wt-tv-rowc");
			WApplication app = WApplication.getInstance();
			if (app.getEnvironment().agentIsWebKit()
					|| app.getEnvironment().agentIsOpera()) {
				scrollBar.setAttributeValue("style", "left: 0px;");
			}
			this.impl_.getLayout().addWidget(scrollBarContainer);
		}
	}

	public void resize(WLength width, WLength height) {
		if (!height.isAuto()) {
			this.viewportHeight_ = (int) Math.ceil(height.toPixels()
					/ this.rowHeight_.toPixels());
			this.scheduleRerender(WTreeView.RenderState.NeedAdjustViewPort);
		}
		WLength w = WApplication.getInstance().getEnvironment().hasAjax() ? WLength.Auto
				: width;
		this.contentsContainer_.resize(w, WLength.Auto);
		this.headerContainer_.resize(w, WLength.Auto);
		super.resize(width, height);
	}

	public void load() {
		super.load();
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

	/**
	 * Signal emitted when an item is clicked.
	 * <p>
	 * 
	 * @see WTreeView#doubleClicked()
	 */
	public Signal2<WModelIndex, WMouseEvent> clicked() {
		return this.clicked_;
	}

	/**
	 * Signal emitted when an item is double clicked.
	 * <p>
	 * 
	 * @see WTreeView#doubleClicked()
	 */
	public Signal2<WModelIndex, WMouseEvent> doubleClicked() {
		return this.doubleClicked_;
	}

	/**
	 * Signal emitted when an item is double clicked.
	 * <p>
	 * 
	 * @see WTreeView#doubleClicked()
	 */
	public Signal2<WModelIndex, WMouseEvent> mouseWentDown() {
		return this.mouseWentDown_;
	}

	/**
	 * Signal emitted when the selection is changed.
	 * <p>
	 * 
	 * @see WTreeView#select(WModelIndex index, SelectionFlag option)
	 * @see WTreeView#setSelectionMode(SelectionMode mode)
	 * @see WTreeView#setSelectionBehavior(SelectionBehavior behavior)
	 */
	public Signal selectionChanged() {
		return this.selectionChanged_;
	}

	// public void openPersistentEditor(WModelIndex index) ;
	// public void closePersistentEditor(WModelIndex index) ;
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
	 * @see WTreeView#setItemDelegate(WAbstractItemDelegate delegate)
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
	 * @see WTreeView#setItemDelegate(WAbstractItemDelegate delegate)
	 */
	public void setItemDelegateForColumn(int column,
			WAbstractItemDelegate delegate) {
		this.columnInfo(column).itemDelegate_ = delegate;
	}

	/**
	 * Returns the delegate for a column.
	 * <p>
	 * 
	 * @see WTreeView#setItemDelegateForColumn(int column, WAbstractItemDelegate
	 *      delegate)
	 */
	public WAbstractItemDelegate getItemDelegateForColumn(int column) {
		return this.columnInfo(column).itemDelegate_;
	}

	/**
	 * Returns the delegate for rendering an item.
	 * <p>
	 * 
	 * @see WTreeView#setItemDelegateForColumn(int column, WAbstractItemDelegate
	 *      delegate)
	 * @see WTreeView#setItemDelegate(WAbstractItemDelegate delegate)
	 */
	public WAbstractItemDelegate getItemDelegate(WModelIndex index) {
		WAbstractItemDelegate result = this.getItemDelegateForColumn(index
				.getColumn());
		return result != null ? result : this.itemDelegate_;
	}

	public void refresh() {
		WApplication app = WApplication.getInstance();
		String columnsWidth = ""
				+ "var WT=Wt2_99_2,t="
				+ this.contents_.getJsRef()
				+ ".firstChild,h="
				+ this.headers_.getJsRef()
				+ ",hh=h.firstChild,hc=hh.firstChild"
				+ (this.column1Fixed_ ? ".firstChild" : "")
				+ ",totalw=0,extra="
				+ (this.column1Fixed_ ? "1" : "4")
				+ "+ (hh.childNodes.length > 1? (WT.hasTag(hh.childNodes[1], 'IMG') ? 17 : 6): 0);if("
				+ this.getJsRef()
				+ ".offsetWidth == 0) return;for (var i=0, length=hc.childNodes.length; i < length; ++i) {var cl = hc.childNodes[i].className.split(' ')[2],r = WT.getCssRule('#"
				+ this.getFormName()
				+ " .' + cl);totalw += WT.pxself(r, 'width') + 7;}var cw = WT.pxself(hh, 'width'),hdiff = c ? (cw == 0 ? 0 : (totalw - (cw - extra))) : diffx;";
		if (!this.column1Fixed_) {
			columnsWidth += "t.style.width = (t.offsetWidth + hdiff) + 'px';h.style.width = t.offsetWidth + 'px';hh.style.width = (totalw + extra) + 'px';";
		} else {
			columnsWidth += "var r = WT.getCssRule('#"
					+ this.getFormName()
					+ " '+ (c ? '.Wt-tv-rowc' : '.c0w'));totalw += 'px';if (c) {r.style.width = totalw;"
					+ (app.getEnvironment().agentIsIE() ? "var c =Wt2_99_2.getElementsByClassName('Wt-tv-rowc', "
							+ this.getJsRef()
							+ ");for (var i = 0, length = c.length; i < length; ++i) {var cc=c[i];cc.style.width = totalw;}"
							: "")
					+ "} else {r.style.width = (WT.pxself(r, 'width') + diffx) + 'px';"
					+ app.getJavaScriptClass() + "._p_.autoJavaScript();}";
		}
		app.doJavaScript(this.getJsRef()
				+ ".adjustHeaderWidth=function(c, diffx) {if ("
				+ this.contentsContainer_.getJsRef() + ") {" + columnsWidth
				+ "}};");
		app
				.doJavaScript(this.getJsRef()
						+ ".handleDragDrop=function(action, object, event, sourceId, mimeType) {var self="
						+ this.getJsRef()
						+ ";if (self.dropEl) {self.dropEl.className = self.dropEl.classNameOrig;self.dropEl = null;}if (action=='end')return;var item="
						+ app.getJavaScriptClass()
						+ ".getItem(event);if (!item.selected && item.drop && item.columnId != -1) {if (action=='drop') {"
						+ this.itemEvent_.createCall("item.nodeId",
								"item.columnId", "'drop'", "sourceId",
								"mimeType")
						+ ";} else {object.className = 'valid-drop';self.dropEl = item.el;self.dropEl.classNameOrig = self.dropEl.className;self.dropEl.className = self.dropEl.className + ' drop-site';}} else {object.className = '';}};");
	}

	/**
	 * Handle a drop event (drag &amp; drop).
	 * <p>
	 * The <i>event</i> object contains details about the drop operation,
	 * identifying the source (which provides the data) and the mime-type of the
	 * data. The drop was received on the <i>target</i> item.
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
	 * {@link WTreeView#setHeaderHeight(WLength height, boolean multiLine)}.
	 * <p>
	 * The widget is created only once, but this method may be called repeatedly
	 * for a column for which prior calls returned 0 (i.e. each time the header
	 * is rerendered).
	 * <p>
	 * The default implementation returns 0.
	 * <p>
	 * 
	 * @see WTreeView#setHeaderHeight(WLength height, boolean multiLine)
	 * @see WTreeView#extraHeaderWidget(int column)
	 */
	protected WWidget createExtraHeaderWidget(int column) {
		return null;
	}

	protected void render() {
		while (this.renderState_ != WTreeView.RenderState.RenderOk) {
			WTreeView.RenderState s = this.renderState_;
			this.renderState_ = WTreeView.RenderState.RenderOk;
			switch (s) {
			case NeedRerender:
				this.initLayoutJavaScript();
				this.rerenderHeader();
				this.rerenderTree();
				break;
			case NeedRerenderHeader:
				this.rerenderHeader();
				break;
			case NeedRerenderTree:
				this.rerenderTree();
				break;
			case NeedAdjustViewPort:
				this.adjustToViewport();
				break;
			default:
				break;
			}
		}
		super.render();
	}

	/**
	 * Returns the extra header widget.
	 * <p>
	 * Returns the widget previously created using
	 * {@link WTreeView#createExtraHeaderWidget(int column)}
	 * <p>
	 * 
	 * @see WTreeView#createExtraHeaderWidget(int column)
	 */
	protected WWidget extraHeaderWidget(int column) {
		return this.columnInfo(column).extraHeaderWidget;
	}

	private static class ColumnInfo {
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

		public ColumnInfo(WTreeView view, WApplication app, int anId, int column) {
			this.id = anId;
			this.sortOrder = SortOrder.AscendingOrder;
			this.alignment = AlignmentFlag.AlignLeft;
			this.headerAlignment = AlignmentFlag.AlignLeft;
			this.width = new WLength();
			this.extraHeaderWidget = null;
			this.sorting = view.sorting_;
			this.itemDelegate_ = null;
			this.styleRule = new WCssTemplateRule("#" + view.getFormName()
					+ " ." + this.getStyleClass());
			if (column != 0) {
				this.width = new WLength(150);
				this.styleRule.getTemplateWidget().resize(this.width,
						WLength.Auto);
			}
			app.getStyleSheet().addRule(this.styleRule);
		}
	}

	private Map<WModelIndex, WTreeViewNode> NodeMap;
	private WAbstractItemModel model_;
	private WAbstractItemDelegate itemDelegate_;
	private WItemSelectionModel selectionModel_;
	private WModelIndex rootIndex_;
	private WLength rowHeight_;
	private WLength headerHeight_;
	SortedSet<WModelIndex> expandedSet_;
	private Map<WModelIndex, WTreeViewNode> renderedNodes_;
	private WTreeViewNode rootNode_;
	private String imagePack_;
	private WCssTemplateRule rowHeightRule_;
	private WCssTemplateRule headerHeightRule_;
	private WCssTemplateRule rowWidthRule_;
	private WCssTemplateRule rowContentsWidthRule_;
	private WCssTemplateRule c0WidthRule_;
	private WCssRule borderColorRule_;
	private boolean alternatingRowColors_;
	private boolean rootIsDecorated_;
	private SelectionMode selectionMode_;
	private boolean sorting_;
	private boolean columnResize_;
	private boolean multiLineHeader_;
	boolean column1Fixed_;
	private List<Object> expandedRaw_;
	private List<Object> selectionRaw_;
	private Object rawRootIndex_;
	private List<WTreeView.ColumnInfo> columns_;
	private int nextColumnId_;
	Signal1<WModelIndex> collapsed_;
	Signal1<WModelIndex> expanded_;
	private Signal2<WModelIndex, WMouseEvent> clicked_;
	private Signal2<WModelIndex, WMouseEvent> doubleClicked_;
	private Signal2<WModelIndex, WMouseEvent> mouseWentDown_;
	private Signal selectionChanged_;
	private WSignalMapper1<Integer> clickedForSortMapper_;
	WSignalMapper1<WModelIndex> clickedMapper_;

	enum RenderState {
		RenderOk(0), NeedAdjustViewPort(1), NeedRerenderTree(2), NeedRerenderHeader(
				3), NeedRerender(4);

		private int value;

		RenderState(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	private WTreeView.RenderState renderState_;
	private int viewportTop_;
	private int viewportHeight_;
	private int firstRenderedRow_;
	private int validRowCount_;
	private int nodeLoad_;
	private int currentSortColumn_;
	private WContainerWidget impl_;
	private WContainerWidget headers_;
	private WContainerWidget headerContainer_;
	private WContainerWidget contents_;
	private WContainerWidget contentsContainer_;
	private int firstRemovedRow_;
	private int removedHeight_;
	private List<AbstractSignal.Connection> modelConnections_;
	private JSlot resizeHandleMDownJS_;
	private JSlot resizeHandleMMovedJS_;
	private JSlot resizeHandleMUpJS_;
	private JSlot tieContentsHeaderScrollJS_;
	private JSlot tieRowsScrollJS_;
	private JSlot itemClickedJS_;
	private JSlot itemDoubleClickedJS_;
	private JSlot itemMouseDownJS_;
	private JSignal6<String, Integer, String, String, String, WMouseEvent> itemEvent_;
	private boolean dragEnabled_;
	private boolean dropsEnabled_;
	private WWidget dragWidget_;

	private void initLayoutJavaScript() {
		this.refresh();
	}

	private void rerenderHeader() {
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
		WContainerWidget rowc = new WContainerWidget(this.headers_);
		rowc.setFloatSide(Side.Right);
		WContainerWidget row = new WContainerWidget(rowc);
		row.setStyleClass("Wt-tv-row headerrh");
		if (this.column1Fixed_) {
			row = new WContainerWidget(row);
			row.setStyleClass("Wt-tv-rowc");
		}
		if (this.columnInfo(0).sorting) {
			WImage sortIcon = new WImage(rowc);
			sortIcon.setStyleClass(this.columnResize_ ? "Wt-tv-sh Wt-tv-shc0"
					: "Wt-tv-sh-nrh Wt-tv-shc0");
			sortIcon.setImageRef(this.imagePack_ + "sort-arrow-none.gif");
			this.clickedForSortMapper_.mapConnect(sortIcon.clicked(), 0);
		}
		if (this.columnResize_) {
			WContainerWidget resizeHandle = new WContainerWidget(rowc);
			resizeHandle.setStyleClass("Wt-tv-rh headerrh Wt-tv-rhc0");
			resizeHandle.mouseWentDown().addListener(this.resizeHandleMDownJS_);
			resizeHandle.mouseWentUp().addListener(this.resizeHandleMUpJS_);
			resizeHandle.mouseMoved().addListener(this.resizeHandleMMovedJS_);
		}
		WApplication app = WApplication.getInstance();
		for (int i = 1; i < this.getColumnCount(); ++i) {
			WWidget w = this.createHeaderWidget(app, i);
			row.addWidget(w);
		}
		WText t = new WText("&nbsp;");
		if (this.getColumnCount() > 0) {
			if (!this.multiLineHeader_) {
				t.setStyleClass(this.getColumnStyleClass(0)
						+ " headerrh Wt-label");
			} else {
				t.setStyleClass(this.getColumnStyleClass(0) + " Wt-label");
			}
		}
		t.setInline(false);
		t.setAttributeValue("style",
				"float: none; margin: 0px auto;padding-left: 6px;");
		if (this.columnInfo(0).extraHeaderWidget != null) {
			WContainerWidget c = new WContainerWidget(this.headers_);
			c.setInline(true);
			c.addWidget(t);
			c.addWidget(this.columnInfo(0).extraHeaderWidget);
		} else {
			this.headers_.addWidget(t);
		}
		if (this.currentSortColumn_ != -1) {
			SortOrder order = this.columnInfo(this.currentSortColumn_).sortOrder;
			this
					.headerSortIconWidget(this.currentSortColumn_)
					.setImageRef(
							this.imagePack_
									+ (order == SortOrder.AscendingOrder ? "sort-arrow-up.gif"
											: "sort-arrow-down.gif"));
		}
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
		this.rootNode_.setSelectable(false);
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			this.rootNode_.clicked().addListener(this.itemClickedJS_);
			this.rootNode_.doubleClicked().addListener(
					this.itemDoubleClickedJS_);
			if (this.mouseWentDown_.isConnected() || this.dragEnabled_) {
				this.rootNode_.mouseWentDown().addListener(
						this.itemMouseDownJS_);
			}
		}
		this.setRootNodeStyle();
		wrapRoot.addWidget(this.rootNode_);
		this.adjustToViewport();
	}

	void scheduleRerender(WTreeView.RenderState what) {
		if (what == WTreeView.RenderState.NeedRerenderHeader
				&& this.renderState_ == WTreeView.RenderState.NeedRerenderTree
				|| what == WTreeView.RenderState.NeedRerenderTree
				&& this.renderState_ == WTreeView.RenderState.NeedRerenderHeader) {
			this.renderState_ = WTreeView.RenderState.NeedRerender;
		} else {
			this.renderState_ = EnumUtils.max(what, this.renderState_);
		}
		this.askRerender();
	}

	private void modelColumnsInserted(WModelIndex parent, int start, int end) {
		int count = end - start + 1;
		if (!(parent != null)) {
			WApplication app = WApplication.getInstance();
			for (int i = start; i < start + count; ++i) {
				this.columns_.add(0 + i, new WTreeView.ColumnInfo(this, app,
						++this.nextColumnId_, i));
			}
			if (start == 0) {
				this.scheduleRerender(WTreeView.RenderState.NeedRerenderHeader);
			} else {
				WContainerWidget row = this.getHeaderRow();
				for (int i = start; i < start + count; ++i) {
					row.insertWidget(i - 1, this.createHeaderWidget(app, i));
				}
			}
		}
		if (start == 0) {
			this.scheduleRerender(WTreeView.RenderState.NeedRerenderTree);
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

	private void modelColumnsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		int count = end - start + 1;
		if (!(parent != null)) {
			for (int ii = 0; ii < 0 + start + count; ++ii)
				this.columns_.remove(0 + start);
			;
			if (start == 0) {
				this.scheduleRerender(WTreeView.RenderState.NeedRerenderHeader);
			} else {
				for (int i = start; i < start + count; ++i) {
					if (this.headerWidget(start) != null)
						this.headerWidget(start).remove();
				}
			}
		}
		if (start == 0) {
			this.scheduleRerender(WTreeView.RenderState.NeedRerenderTree);
		}
	}

	private void modelColumnsRemoved(WModelIndex parent, int start, int end) {
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
	}

	private void modelRowsInserted(WModelIndex parent, int start, int end) {
		int count = end - start + 1;
		this.shiftModelIndexes(parent, start, count);
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
									+ this.validRowCount_
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

	private void modelRowsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		int count = end - start + 1;
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
							WModelIndex childIndex = this.model_.getIndex(i, 0,
									parent);
							if (i == start) {
								this.firstRemovedRow_ = this.renderedRow(
										childIndex, w);
							}
							int childHeight = this.subTreeHeight(childIndex);
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
							n.updateGraphics(true, this.model_.getRowCount(n
									.getModelIndex()) == 0);
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
					WModelIndex childIndex = this.model_.getIndex(i, 0, parent);
					int childHeight = this.subTreeHeight(childIndex);
					this.removedHeight_ += childHeight;
					if (i == start) {
						this.firstRemovedRow_ = this.renderedRow(childIndex, s);
					}
				}
				s.setRows(s.getRows() - this.removedHeight_);
				s.getNode().adjustChildrenHeight(-this.removedHeight_);
			}
		}
		this.shiftModelIndexes(parent, start, -count);
	}

	private void modelRowsRemoved(WModelIndex parent, int start, int end) {
		this.renderedRowsChanged(this.firstRemovedRow_, -this.removedHeight_);
	}

	private void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
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

	private void modelHeaderDataChanged(Orientation orientation, int start,
			int end) {
		if (orientation == Orientation.Horizontal) {
			for (int i = start; i <= end; ++i) {
				WString label = StringUtils.asString(this.model_
						.getHeaderData(i));
				this.headerTextWidget(i).setText(label);
			}
		}
	}

	private void modelLayoutAboutToBeChanged() {
		this.convertToRaw(this.expandedSet_, this.expandedRaw_);
		this.convertToRaw(this.selectionModel_.selection_, this.selectionRaw_);
		this.rawRootIndex_ = this.model_.toRawIndex(this.rootIndex_);
	}

	private void modelLayoutChanged() {
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
		this.scheduleRerender(WTreeView.RenderState.NeedRerenderTree);
	}

	private void modelReset() {
		this.setModel(this.model_);
	}

	private void onViewportChange(WScrollEvent e) {
		this.viewportTop_ = (int) Math.floor(e.getScrollY()
				/ this.rowHeight_.toPixels());
		this.viewportHeight_ = (int) Math.ceil(e.getViewportHeight()
				/ this.rowHeight_.toPixels());
		this.scheduleRerender(WTreeView.RenderState.NeedAdjustViewPort);
	}

	private void toggleSortColumn(int columnid) {
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
			if (i.getValue().getFormName().equals(nodeId)) {
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
					if (type.equals("drop")) {
						WDropEvent e = new WDropEvent(WApplication
								.getInstance().decodeObject(extra1), extra2,
								event);
						this.dropEvent(e, index);
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
					this.imagePack_ + "stripes/stripe-"
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
		int viewportBottom = this.viewportTop_ + this.viewportHeight_;
		int lastValidRow = this.firstRenderedRow_ + this.validRowCount_;
		boolean renderMore = this.viewportTop_ > this.firstRenderedRow_
				- this.viewportHeight_
				|| viewportBottom < lastValidRow + this.viewportHeight_;
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

	int getColumnCount() {
		return this.columns_.size();
	}

	private int columnById(int columnid) {
		for (int i = 0; i < this.getColumnCount(); ++i) {
			if (this.columnInfo(i).id == columnid) {
				return i;
			}
		}
		return 0;
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
		this.scheduleRerender(WTreeView.RenderState.NeedAdjustViewPort);
	}

	private void convertToRaw(SortedSet<WModelIndex> set, List<Object> result) {
		for (Iterator<WModelIndex> i_it = set.iterator(); i_it.hasNext();) {
			WModelIndex i = i_it.next();
			Object rawIndex = this.model_.toRawIndex(i);
			if (rawIndex != null) {
				result.add(rawIndex);
			}
		}
		set.clear();
	}

	private WContainerWidget getHeaderRow() {
		WContainerWidget row = ((this.headers_.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (this.headers_
				.getWidget(0))
				: null);
		row = ((row.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (row
				.getWidget(0))
				: null);
		if (this.column1Fixed_) {
			row = ((row.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (row
					.getWidget(0))
					: null);
		}
		return row;
	}

	private WWidget createHeaderWidget(WApplication app, int column) {
		WTreeView.ColumnInfo info = this.columnInfo(column);
		WContainerWidget w = new WContainerWidget();
		w.setStyleClass("Wt-tv-c headerrh " + info.getStyleClass());
		w.setContentAlignment(info.headerAlignment);
		if (this.columnResize_) {
			WContainerWidget resizeHandle = new WContainerWidget(w);
			resizeHandle.setStyleClass("Wt-tv-rh headerrh");
			resizeHandle.mouseWentDown().addListener(this.resizeHandleMDownJS_);
			resizeHandle.mouseWentUp().addListener(this.resizeHandleMUpJS_);
			resizeHandle.mouseMoved().addListener(this.resizeHandleMMovedJS_);
		}
		if (info.sorting) {
			WImage sortIcon = new WImage(w);
			sortIcon.setStyleClass(this.columnResize_ ? "Wt-tv-sh"
					: "Wt-tv-sh-nrh");
			sortIcon.setImageRef(this.imagePack_ + "sort-arrow-none.gif");
			this.clickedForSortMapper_.mapConnect(sortIcon.clicked(), info.id);
		}
		WText t = new WText("&nbsp;", w);
		t.setStyleClass("Wt-label");
		t.setInline(false);
		if (this.multiLineHeader_ || app.getEnvironment().agentIsIE()) {
			t.setWordWrap(true);
		} else {
			t.setWordWrap(false);
		}
		if (this.columnInfo(column).extraHeaderWidget != null) {
			w.addWidget(this.columnInfo(column).extraHeaderWidget);
		}
		return w;
	}

	private WWidget headerWidget(int column) {
		if (column == 0) {
			return this.headers_.getWidget(this.headers_.getCount() - 1);
		} else {
			return this.getHeaderRow().getWidget(column - 1);
		}
	}

	private WText headerTextWidget(int column) {
		WWidget w = this.headerWidget(column);
		WText result = ((w) instanceof WText ? (WText) (w) : null);
		if (result != null) {
			return result;
		} else {
			WContainerWidget wc = ((w) instanceof WContainerWidget ? (WContainerWidget) (w)
					: null);
			int fromLast = this.columnInfo(column).extraHeaderWidget != null ? 1
					: 0;
			return ((wc.getWidget(wc.getCount() - 1 - fromLast)) instanceof WText ? (WText) (wc
					.getWidget(wc.getCount() - 1 - fromLast))
					: null);
		}
	}

	private WImage headerSortIconWidget(int column) {
		if (!this.columnInfo(column).sorting) {
			return null;
		}
		if (column == 0) {
			WContainerWidget row = ((this.headers_.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (this.headers_
					.getWidget(0))
					: null);
			return ((row.getWidget(1)) instanceof WImage ? (WImage) (row
					.getWidget(1)) : null);
		} else {
			WContainerWidget w = ((this.headerWidget(column)) instanceof WContainerWidget ? (WContainerWidget) (this
					.headerWidget(column))
					: null);
			return ((w.getWidget(this.columnResize_ ? 1 : 0)) instanceof WImage ? (WImage) (w
					.getWidget(this.columnResize_ ? 1 : 0))
					: null);
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

	private void handleClick(WModelIndex index) {
		this
				.selectionHandleClick(index, EnumSet
						.noneOf(KeyboardModifier.class));
		this.clicked_.trigger(index, new WMouseEvent());
	}

	private boolean internalSelect(WModelIndex index, SelectionFlag option) {
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
			WModelIndex top = this.selectionModel_.selection_.first();
			if (top.compareTo(index) < 0) {
				this.selectRange(top, index);
			} else {
				WModelIndex bottom = this.selectionModel_.selection_.last();
				this.selectRange(index, bottom);
			}
		}
		this.selectionChanged_.trigger();
	}

	private void clearSelection() {
		SortedSet<WModelIndex> nodes = this.selectionModel_.selection_;
		while (!nodes.isEmpty()) {
			WModelIndex i = nodes.iterator().next();
			this.internalSelect(i, SelectionFlag.Deselect);
		}
	}

	private void checkDragSelection() {
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

	private void configureModelDragDrop() {
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
				this.acceptDrops(acceptMimeTypes.get(i), "drop-site");
			} else {
				this.stopAcceptDrops(acceptMimeTypes.get(i));
			}
		}
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

	private WTreeView.ColumnInfo columnInfo(int column) {
		while (column >= (int) this.columns_.size()) {
			this.columns_.add(new WTreeView.ColumnInfo(this, WApplication
					.getInstance(), ++this.nextColumnId_, column));
		}
		return this.columns_.get(column);
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
