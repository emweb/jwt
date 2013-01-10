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
 * An MVC View widget for tabular data.
 * <p>
 * 
 * The view displays data from a {@link WAbstractItemModel} in a table. It
 * provides incremental rendering, without excessive use of client- or
 * serverside resources.
 * <p>
 * The rendering (and editing) of items is handled by a
 * {@link WAbstractItemDelegate}, by default it uses {@link WItemDelegate} which
 * renders data of all predefined roles (see also {@link ItemDataRole}),
 * including text, icons, checkboxes, and tooltips.
 * <p>
 * The view provides virtual scrolling in both horizontal and vertical
 * directions, and can therefore be used to display large data models (with
 * large number of columns and rows).
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
 * By default, all columns are given a width of 150px. Column widths of all
 * columns can be set through the API method
 * {@link WTableView#setColumnWidth(int column, WLength width) setColumnWidth()}
 * , and also by the user using handles provided in the header.
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
 * WAbstractItemView#setDropsEnabled()}), the view may receive a drop event on a
 * particular item, at least if the item indicates support for drops (controlled
 * by the {@link ItemFlag#ItemIsDropEnabled ItemIsDropEnabled} flag).
 * <p>
 * You may also react to mouse click events on any item, by connecting to one of
 * the {@link WAbstractItemView#clicked() WAbstractItemView#clicked()} or
 * {@link WAbstractItemView#doubleClicked() WAbstractItemView#doubleClicked()}
 * signals.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The tableview is styled by the current CSS theme. The look can be overridden
 * using the <code>Wt-tableview</code> CSS class and the following selectors.
 * <p>
 * Selectors that apply to the body: <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-tableview .Wt-tv-contents : the main div used when javascript is available
 *  .Wt-tableview .Wt-plaintable  : the main table used when no javascript is 
 *                                  available
 *  .Wt-tableview .Wt-pagingbar   : the paging bar
 * </pre>
 * 
 * </div> Selectors that apply to the header: <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-tableview .Wt-header      : header background div
 *  .Wt-tableview .Wt-headertable : div in the .Wt-header div to enable the 
 *                                  header scrolling
 *  .Wt-tableview .Wt-label       : header label
 *  .Wt-tableview .Wt-tv-rh       : column resize handle
 *  .Wt-tableview .Wt-tv-sh-up    : column sort handle, no sorting
 *  .Wt-tableview .Wt-tv-sh-none  : column sort handle, sort up
 *  .Wt-tableview .Wt-tv-sh-down  : column sort handle, sort down
 *  .Wt-tableview .Wt-tv-br             : header border
 * </pre>
 * 
 * </div> Selectors that apply to the table contents: <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-tableview .Wt-spacer      : spacer for non-loaded content
 *  .Wt-tableview .Wt-selected    : selected item
 *  .Wt-tableview .Wt-drop-site   : possible drop site
 *  .Wt-tableview .Wt-tv-c        : cell style
 * </pre>
 * 
 * </div>
 */
public class WTableView extends WAbstractItemView {
	private static Logger logger = LoggerFactory.getLogger(WTableView.class);

	/**
	 * Constructor.
	 */
	public WTableView(WContainerWidget parent) {
		super(parent);
		this.headers_ = null;
		this.canvas_ = null;
		this.table_ = null;
		this.headerContainer_ = null;
		this.contentsContainer_ = null;
		this.headerColumnsCanvas_ = null;
		this.headerColumnsTable_ = null;
		this.headerColumnsHeaderContainer_ = null;
		this.headerColumnsContainer_ = null;
		this.plainTable_ = null;
		this.dropEvent_ = new JSignal5<Integer, Integer, String, String, WMouseEvent>(
				this.impl_, "dropEvent") {
		};
		this.columnWidthChanged_ = new JSignal2<Integer, Integer>(this.impl_,
				"columnResized") {
		};
		this.scrolled_ = new JSignal4<Integer, Integer, Integer, Integer>(
				this.impl_, "scrolled") {
		};
		this.firstColumn_ = -1;
		this.lastColumn_ = -1;
		this.viewportLeft_ = 0;
		this.viewportWidth_ = 1000;
		this.viewportTop_ = 0;
		this.viewportHeight_ = 800;
		this.setSelectable(false);
		this.dropEvent_
				.addListener(
						this,
						new Signal5.Listener<Integer, Integer, String, String, WMouseEvent>() {
							public void trigger(Integer e1, Integer e2,
									String e3, String e4, WMouseEvent e5) {
								WTableView.this.onDropEvent(e1, e2, e3, e4, e5);
							}
						});
		this.setStyleClass("Wt-itemview Wt-tableview");
		WApplication app = WApplication.getInstance();
		if (app.getEnvironment().hasAjax()) {
			this.impl_.setPositionScheme(PositionScheme.Relative);
			this.headers_ = new WContainerWidget();
			this.headers_.setStyleClass("Wt-headerdiv headerrh");
			this.table_ = new WContainerWidget();
			this.table_.setStyleClass("Wt-tv-contents");
			this.table_.setPositionScheme(PositionScheme.Absolute);
			this.table_.setWidth(new WLength(100, WLength.Unit.Percentage));
			WGridLayout layout = new WGridLayout();
			layout.setHorizontalSpacing(0);
			layout.setVerticalSpacing(0);
			layout.setContentsMargins(0, 0, 0, 0);
			this.headerContainer_ = new WContainerWidget();
			this.headerContainer_.setStyleClass("Wt-header headerrh cwidth");
			this.headerContainer_
					.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			this.headerContainer_.addWidget(this.headers_);
			this.canvas_ = new WContainerWidget();
			this.canvas_.setStyleClass("Wt-spacer");
			this.canvas_.setPositionScheme(PositionScheme.Relative);
			this.canvas_.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent event) {
							WTableView.this.handleSingleClick(false, event);
						}
					});
			this.canvas_.mouseWentDown().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent event) {
							WTableView.this.handleMouseWentDown(false, event);
						}
					});
			this.canvas_.mouseWentUp().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent event) {
							WTableView.this.handleMouseWentUp(false, event);
						}
					});
			this.canvas_.addWidget(this.table_);
			this.contentsContainer_ = new WContainerWidget();
			this.contentsContainer_.setStyleClass("cwidth");
			this.contentsContainer_
					.setOverflow(WContainerWidget.Overflow.OverflowAuto);
			this.contentsContainer_.setPositionScheme(PositionScheme.Absolute);
			this.contentsContainer_.addWidget(this.canvas_);
			this.scrolled_.addListener(this,
					new Signal4.Listener<Integer, Integer, Integer, Integer>() {
						public void trigger(Integer e1, Integer e2, Integer e3,
								Integer e4) {
							WTableView.this.onViewportChange(e1, e2, e3, e4);
						}
					});
			this.headerColumnsHeaderContainer_ = new WContainerWidget();
			this.headerColumnsHeaderContainer_
					.setStyleClass("Wt-header headerrh cwidth Wt-headerdiv");
			this.headerColumnsHeaderContainer_.hide();
			this.headerColumnsTable_ = new WContainerWidget();
			this.headerColumnsTable_.setStyleClass("Wt-tv-contents");
			this.headerColumnsTable_.setPositionScheme(PositionScheme.Absolute);
			this.headerColumnsTable_.setWidth(new WLength(100,
					WLength.Unit.Percentage));
			this.headerColumnsCanvas_ = new WContainerWidget();
			this.headerColumnsCanvas_
					.setPositionScheme(PositionScheme.Relative);
			this.headerColumnsCanvas_.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent event) {
							WTableView.this.handleSingleClick(true, event);
						}
					});
			this.headerColumnsCanvas_.mouseWentDown().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent event) {
							WTableView.this.handleMouseWentDown(true, event);
						}
					});
			this.headerColumnsCanvas_.mouseWentUp().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent event) {
							WTableView.this.handleMouseWentUp(true, event);
						}
					});
			this.headerColumnsCanvas_.addWidget(this.headerColumnsTable_);
			this.headerColumnsContainer_ = new WContainerWidget();
			this.headerColumnsContainer_
					.setPositionScheme(PositionScheme.Absolute);
			this.headerColumnsContainer_
					.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			this.headerColumnsContainer_.addWidget(this.headerColumnsCanvas_);
			this.headerColumnsContainer_.hide();
			layout.addWidget(this.headerColumnsHeaderContainer_, 0, 0);
			layout.addWidget(this.headerContainer_, 0, 1);
			layout.addWidget(this.headerColumnsContainer_, 1, 0);
			layout.addWidget(this.contentsContainer_, 1, 1);
			layout.setRowStretch(1, 1);
			layout.setColumnStretch(1, 1);
			this.impl_.setLayout(layout);
			app.addAutoJavaScript("{var obj = $('#" + this.getId()
					+ "').data('obj');if (obj) obj.autoJavaScript();}");
			this.connectObjJS(this.canvas_.mouseWentDown(), "mouseDown");
		} else {
			this.plainTable_ = new WTable();
			this.plainTable_.setStyleClass("Wt-plaintable");
			this.plainTable_.setHeaderCount(1);
			this.impl_.addWidget(this.plainTable_);
			this.resize(this.getWidth(), this.getHeight());
		}
		this.setRowHeight(this.getRowHeight());
		this.updateTableBackground();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WTableView(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTableView() {
		this((WContainerWidget) null);
	}

	public void remove() {
		this.impl_.clear();
		super.remove();
	}

	public WWidget itemWidget(WModelIndex index) {
		if (this.isRowRendered(index.getRow())
				&& this.isColumnRendered(index.getColumn())) {
			int renderedRow = index.getRow() - this.getFirstRow();
			int renderedCol = index.getColumn() - this.getFirstColumn();
			if (this.isAjaxMode()) {
				WTableView.ColumnWidget column = this
						.columnContainer(renderedCol);
				return column.getWidget(renderedRow);
			} else {
				return this.plainTable_.getElementAt(renderedRow + 1,
						renderedCol);
			}
		} else {
			return null;
		}
	}

	public void setModel(WAbstractItemModel model) {
		super.setModel(model);
		this.modelConnections_.add(model.columnsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTableView.this.modelColumnsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.columnsAboutToBeRemoved().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTableView.this
								.modelColumnsAboutToBeRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.rowsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTableView.this.modelRowsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.rowsAboutToBeRemoved().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTableView.this.modelRowsAboutToBeRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.rowsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WTableView.this.modelRowsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.dataChanged().addListener(this,
				new Signal2.Listener<WModelIndex, WModelIndex>() {
					public void trigger(WModelIndex e1, WModelIndex e2) {
						WTableView.this.modelDataChanged(e1, e2);
					}
				}));
		this.modelConnections_.add(model.headerDataChanged().addListener(this,
				new Signal3.Listener<Orientation, Integer, Integer>() {
					public void trigger(Orientation e1, Integer e2, Integer e3) {
						WTableView.this.modelHeaderDataChanged(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(model.layoutAboutToBeChanged().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						WTableView.this.modelLayoutAboutToBeChanged();
					}
				}));
		this.modelConnections_.add(model.layoutChanged().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WTableView.this.modelLayoutChanged();
					}
				}));
		this.modelConnections_.add(model.modelReset().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WTableView.this.modelReset();
					}
				}));
		this.firstColumn_ = this.lastColumn_ = -1;
	}

	public void setColumnWidth(int column, WLength width) {
		int delta = (int) (width.toPixels() - this.columnInfo(column).width
				.toPixels());
		this.columnInfo(column).width = width;
		if (this.columnInfo(column).hidden) {
			delta = 0;
		}
		if (this.isAjaxMode()) {
			this.headers_.setWidth(new WLength(this.headers_.getWidth()
					.toPixels()
					+ delta));
			this.canvas_.setWidth(new WLength(this.canvas_.getWidth()
					.toPixels()
					+ delta));
			if (this.renderState_.getValue() >= WAbstractItemView.RenderState.NeedRerenderHeader
					.getValue()) {
				return;
			}
			if (this.isColumnRendered(column)) {
				this.updateColumnOffsets();
			} else {
				if (column < this.getFirstColumn()) {
					this.setSpannerCount(Side.Left, this
							.getSpannerCount(Side.Left));
				}
			}
		}
		if (this.renderState_.getValue() >= WAbstractItemView.RenderState.NeedRerenderHeader
				.getValue()) {
			return;
		}
		WWidget hc = this.headers_.getWidget(column);
		hc.setWidth(new WLength(width.toPixels() + 1));
		if (!this.isAjaxMode()) {
			hc.getParent().resize(new WLength(width.toPixels() + 1),
					hc.getHeight());
		}
	}

	public void setAlternatingRowColors(boolean enable) {
		super.setAlternatingRowColors(enable);
		this.updateTableBackground();
	}

	public void setRowHeight(WLength rowHeight) {
		int renderedRowCount = this.getModel() != null ? this.getLastRow()
				- this.getFirstRow() + 1 : 0;
		super.setRowHeight(rowHeight);
		String lh = "line-height: " + rowHeight.getCssText();
		if (this.isAjaxMode()) {
			this.canvas_.setAttributeValue("style", lh);
			this.headerColumnsCanvas_.setAttributeValue("style", lh);
			if (this.getModel() != null) {
				double ch = this.getCanvasHeight();
				this.canvas_.resize(this.canvas_.getWidth(), new WLength(ch));
				this.headerColumnsCanvas_.setHeight(new WLength(ch));
				double th = renderedRowCount * rowHeight.toPixels();
				this.setRenderedHeight(th);
			}
		} else {
			this.plainTable_.setAttributeValue("style", lh
					+ ";table-layout: fixed;");
			this.resize(this.getWidth(), this.getHeight());
		}
		this.updateTableBackground();
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
	}

	public void setHeaderHeight(WLength height) {
		super.setHeaderHeight(height);
		if (!this.isAjaxMode()) {
			this.resize(this.getWidth(), this.getHeight());
		}
	}

	public void setColumnBorder(WColor color) {
	}

	public void resize(WLength width, WLength height) {
		if (this.isAjaxMode()) {
			if (height.getUnit() == WLength.Unit.Percentage) {
				logger.error(new StringWriter().append(
						"resize(): height cannot be a Percentage").toString());
				return;
			}
			if (!height.isAuto()) {
				this.viewportHeight_ = (int) Math.ceil(height.toPixels()
						- this.getHeaderHeight().toPixels());
			} else {
				this.viewportHeight_ = 800;
			}
		} else {
			if (!(this.plainTable_ != null)) {
				return;
			}
			this.plainTable_.setWidth(width);
			if (!height.isAuto()) {
				if (this.impl_.getCount() < 2) {
					this.impl_.addWidget(this.getCreatePageNavigationBar());
				}
			}
		}
		this.computeRenderedArea();
		super.resize(width, height);
		this.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
	}

	public void setColumnHidden(int column, boolean hidden) {
		if (this.columnInfo(column).hidden != hidden) {
			super.setColumnHidden(column, hidden);
			int delta = (int) this.columnInfo(column).width.toPixels() + 7;
			if (hidden) {
				delta = -delta;
			}
			if (this.isAjaxMode()) {
				this.headers_.setWidth(new WLength(this.headers_.getWidth()
						.toPixels()
						+ delta));
				this.canvas_.setWidth(new WLength(this.canvas_.getWidth()
						.toPixels()
						+ delta));
				if (this.isColumnRendered(column)) {
					this.updateColumnOffsets();
				} else {
					if (column < this.getFirstColumn()) {
						this.setSpannerCount(Side.Left, this
								.getSpannerCount(Side.Left));
					}
				}
				if (this.renderState_.getValue() >= WAbstractItemView.RenderState.NeedRerenderHeader
						.getValue()) {
					return;
				}
				WWidget hc = this.headerWidget(column, false);
				hc.setHidden(hidden);
			} else {
				if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderData
						.getValue()) {
					for (int i = 0; i < this.plainTable_.getRowCount(); ++i) {
						this.plainTable_.getElementAt(i, column).setHidden(
								hidden);
					}
				}
			}
		}
	}

	public void setRowHeaderCount(int count) {
		super.setRowHeaderCount(count);
		if (this.isAjaxMode()) {
			int total = 0;
			for (int i = 0; i < count; i++) {
				if (!this.columnInfo(i).hidden) {
					total += (int) this.columnInfo(i).width.toPixels() + 7;
				}
			}
			this.headerColumnsContainer_.setWidth(new WLength(total));
			this.headerColumnsContainer_.setHidden(count == 0);
			this.headerColumnsHeaderContainer_.setHidden(count == 0);
		}
	}

	public int getPageCount() {
		if (this.getModel() != null) {
			return (this.getModel().getRowCount(this.getRootIndex()) - 1)
					/ this.getPageSize() + 1;
		} else {
			return 1;
		}
	}

	public int getPageSize() {
		if (this.getHeight().isAuto()) {
			return 10000;
		} else {
			final int navigationBarHeight = 25;
			int pageSize = (int) ((this.getHeight().toPixels()
					- this.getHeaderHeight().toPixels() - navigationBarHeight) / this
					.getRowHeight().toPixels());
			if (pageSize <= 0) {
				pageSize = 1;
			}
			return pageSize;
		}
	}

	public int getCurrentPage() {
		return this.renderedFirstRow_ / this.getPageSize();
	}

	public void setCurrentPage(int page) {
		this.renderedFirstRow_ = page * this.getPageSize();
		if (this.getModel() != null) {
			this.renderedLastRow_ = Math.min(this.renderedFirstRow_
					+ this.getPageSize() - 1, this.getModel().getRowCount(
					this.getRootIndex()) - 1);
		} else {
			this.renderedLastRow_ = this.renderedFirstRow_;
		}
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
	}

	public void scrollTo(WModelIndex index, WAbstractItemView.ScrollHint hint) {
		if ((index.getParent() == this.getRootIndex() || (index.getParent() != null && index
				.getParent().equals(this.getRootIndex())))) {
			if (this.isAjaxMode()) {
				int rh = (int) this.getRowHeight().toPixels();
				int rowY = index.getRow() * rh;
				if (this.viewportHeight_ != 800) {
					if (hint == WAbstractItemView.ScrollHint.EnsureVisible) {
						if (this.viewportTop_ + this.viewportHeight_ < rowY) {
							hint = WAbstractItemView.ScrollHint.PositionAtTop;
						} else {
							if (rowY < this.viewportTop_) {
								hint = WAbstractItemView.ScrollHint.PositionAtBottom;
							}
						}
					}
					switch (hint) {
					case PositionAtTop:
						this.viewportTop_ = rowY;
						break;
					case PositionAtBottom:
						this.viewportTop_ = rowY - this.viewportHeight_ + rh;
						break;
					case PositionAtCenter:
						this.viewportTop_ = rowY - (this.viewportHeight_ - rh)
								/ 2;
						break;
					default:
						break;
					}
					this.viewportTop_ = Math.max(0, this.viewportTop_);
					if (hint != WAbstractItemView.ScrollHint.EnsureVisible) {
						this.computeRenderedArea();
						this
								.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
					}
				}
				if (this.isRendered()) {
					StringBuilder s = new StringBuilder();
					s.append("jQuery.data(").append(this.getJsRef()).append(
							", 'obj').scrollTo(-1, ").append(rowY).append(",")
							.append((int) hint.getValue()).append(");");
					this.doJavaScript(s.toString());
				}
			} else {
				this.setCurrentPage(index.getRow() / this.getPageSize());
			}
		}
	}

	public void setHidden(boolean hidden, WAnimation animation) {
		boolean change = this.isHidden() != hidden;
		super.setHidden(hidden, animation);
		if (change && !hidden) {
			WApplication app = WApplication.getInstance();
			if (app.getEnvironment().hasAjax()
					&& app.getEnvironment().agentIsIE()
					&& !app.getEnvironment().agentIsIElt(9)) {
				StringBuilder s = new StringBuilder();
				s.append("jQuery.data(").append(this.getJsRef()).append(
						", 'obj').resetScroll();");
				this.doJavaScript(s.toString());
			}
		}
	}

	static class ColumnWidget extends WContainerWidget {
		private static Logger logger = LoggerFactory
				.getLogger(ColumnWidget.class);

		public ColumnWidget(WTableView view, int column) {
			super();
			this.column_ = column;
			assert view.isAjaxMode();
			WAbstractItemView.ColumnInfo ci = view.columnInfo(column);
			this.setStyleClass(ci.getStyleClass());
			this.setPositionScheme(PositionScheme.Absolute);
			this.setOffsets(new WLength(0), EnumSet.of(Side.Top, Side.Left));
			this.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			this.setHeight(view.table_.getHeight());
			if (column >= view.getRowHeaderCount()) {
				if (view.table_.getCount() == 0
						|| column > view.columnContainer(-1).getColumn()) {
					view.table_.addWidget(this);
				} else {
					view.table_.insertWidget(0, this);
				}
			} else {
				view.headerColumnsTable_.insertWidget(column, this);
			}
		}

		public int getColumn() {
			return this.column_;
		}

		private int column_;
	}

	private WContainerWidget headers_;
	private WContainerWidget canvas_;
	private WContainerWidget table_;
	private WContainerWidget headerContainer_;
	private WContainerWidget contentsContainer_;
	private WContainerWidget headerColumnsCanvas_;
	private WContainerWidget headerColumnsTable_;
	private WContainerWidget headerColumnsHeaderContainer_;
	private WContainerWidget headerColumnsContainer_;
	private WTable plainTable_;
	private JSignal5<Integer, Integer, String, String, WMouseEvent> dropEvent_;
	private JSignal2<Integer, Integer> columnWidthChanged_;
	private JSignal4<Integer, Integer, Integer, Integer> scrolled_;
	private int firstColumn_;
	private int lastColumn_;
	private int viewportLeft_;
	private int viewportWidth_;
	private int viewportTop_;
	private int viewportHeight_;
	private int renderedFirstRow_;
	private int renderedLastRow_;
	private int renderedFirstColumn_;
	private int renderedLastColumn_;
	private int tabIndex_;

	private void updateTableBackground() {
		String backgroundImage = "";
		if (this.hasAlternatingRowColors()) {
			backgroundImage = "/stripes/stripe-";
		} else {
			backgroundImage = "/no-stripes/no-stripe-";
		}
		backgroundImage = WApplication.getResourcesUrl() + "themes/"
				+ WApplication.getInstance().getCssTheme() + backgroundImage
				+ String.valueOf((int) this.getRowHeight().toPixels())
				+ "px.gif";
		if (this.isAjaxMode()) {
			this.table_.getDecorationStyle().setBackgroundImage(
					new WLink(backgroundImage));
			this.headerColumnsTable_.getDecorationStyle().setBackgroundImage(
					new WLink(backgroundImage));
		} else {
			this.plainTable_.getDecorationStyle().setBackgroundImage(
					new WLink(backgroundImage));
		}
	}

	private WTableView.ColumnWidget columnContainer(int renderedColumn) {
		assert this.isAjaxMode();
		if (renderedColumn < this.getRowHeaderCount() && renderedColumn >= 0) {
			return ((this.headerColumnsTable_.getWidget(renderedColumn)) instanceof WTableView.ColumnWidget ? (WTableView.ColumnWidget) (this.headerColumnsTable_
					.getWidget(renderedColumn))
					: null);
		} else {
			if (this.table_.getCount() > 0) {
				if (renderedColumn < 0) {
					return ((this.table_.getWidget(this.table_.getCount() - 1)) instanceof WTableView.ColumnWidget ? (WTableView.ColumnWidget) (this.table_
							.getWidget(this.table_.getCount() - 1))
							: null);
				} else {
					return ((this.table_.getWidget(renderedColumn
							- this.getRowHeaderCount())) instanceof WTableView.ColumnWidget ? (WTableView.ColumnWidget) (this.table_
							.getWidget(renderedColumn
									- this.getRowHeaderCount()))
							: null);
				}
			} else {
				return null;
			}
		}
	}

	private void modelColumnsInserted(WModelIndex parent, int start, int end) {
		if (!(parent == this.getRootIndex() || (parent != null && parent
				.equals(this.getRootIndex())))) {
			return;
		}
		int count = end - start + 1;
		int width = 0;
		for (int i = start; i < start + count; ++i) {
			this.columns_.add(0 + i, this.createColumnInfo(i));
			width += (int) this.columnInfo(i).width.toPixels() + 7;
		}
		this.shiftModelIndexColumns(start, end - start + 1);
		if (this.isAjaxMode()) {
			this.canvas_.setWidth(new WLength(this.canvas_.getWidth()
					.toPixels()
					+ width));
		}
		if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
				.getValue()) {
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
		}
		if (start > this.getLastColumn()
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerender
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData) {
			return;
		}
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
	}

	private void modelColumnsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		if (!(parent == this.getRootIndex() || (parent != null && parent
				.equals(this.getRootIndex())))) {
			return;
		}
		for (int r = 0; r < this.getModel().getRowCount(); r++) {
			for (int c = start; c <= end; c++) {
				this.closeEditor(this.getModel().getIndex(r, c), false);
			}
		}
		this.shiftModelIndexColumns(start, -(end - start + 1));
		int count = end - start + 1;
		int width = 0;
		for (int i = start; i < start + count; ++i) {
			if (!this.columnInfo(i).hidden) {
				width += (int) this.columnInfo(i).width.toPixels() + 7;
			}
		}
		for (int ii = 0; ii < (0 + start + count) - (0 + start); ++ii)
			this.columns_.remove(0 + start);
		;
		if (this.isAjaxMode()) {
			this.canvas_.setWidth(new WLength(this.canvas_.getWidth()
					.toPixels()
					- width));
		}
		if (start <= this.currentSortColumn_ && this.currentSortColumn_ <= end) {
			this.currentSortColumn_ = -1;
		}
		if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderHeader
				.getValue()) {
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderHeader);
		}
		if (start > this.getLastColumn()
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerender
				|| this.renderState_ == WAbstractItemView.RenderState.NeedRerenderData) {
			return;
		}
		this.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
	}

	private void modelRowsInserted(WModelIndex parent, int start, int end) {
		if (!(parent == this.getRootIndex() || (parent != null && parent
				.equals(this.getRootIndex())))) {
			return;
		}
		this.shiftModelIndexRows(start, end - start + 1);
		if (this.isAjaxMode()) {
			this.canvas_.setHeight(new WLength(this.getCanvasHeight()));
			this.headerColumnsCanvas_.setHeight(new WLength(this
					.getCanvasHeight()));
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
		}
		this.computeRenderedArea();
		if (start <= this.getLastRow()) {
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
		}
	}

	private void modelRowsAboutToBeRemoved(WModelIndex parent, int start,
			int end) {
		if (!(parent == this.getRootIndex() || (parent != null && parent
				.equals(this.getRootIndex())))) {
			return;
		}
		for (int c = 0; c < this.getColumnCount(); c++) {
			for (int r = start; r <= end; r++) {
				this.closeEditor(this.getModel().getIndex(r, c), false);
			}
		}
		this.shiftModelIndexRows(start, -(end - start + 1));
	}

	private void modelRowsRemoved(WModelIndex parent, int start, int end) {
		if (!(parent == this.getRootIndex() || (parent != null && parent
				.equals(this.getRootIndex())))) {
			return;
		}
		if (this.isAjaxMode()) {
			this.canvas_.setHeight(new WLength(this.getCanvasHeight()));
			this.headerColumnsCanvas_.setHeight(new WLength(this
					.getCanvasHeight()));
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
		}
		this.computeRenderedArea();
		if (start <= this.getLastRow()) {
			this
					.scheduleRerender(WAbstractItemView.RenderState.NeedRerenderData);
		}
	}

	void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
		if (!(topLeft.getParent() == this.getRootIndex() || (topLeft
				.getParent() != null && topLeft.getParent().equals(
				this.getRootIndex())))) {
			return;
		}
		if (this.renderState_.getValue() < WAbstractItemView.RenderState.NeedRerenderData
				.getValue()) {
			int row1 = Math.max(topLeft.getRow(), this.getFirstRow());
			int row2 = Math.min(bottomRight.getRow(), this.getLastRow());
			int col1 = Math.max(topLeft.getColumn(), this.getFirstColumn());
			int col2 = Math.min(bottomRight.getColumn(), this.getLastColumn());
			for (int i = row1; i <= row2; ++i) {
				int renderedRow = i - this.getFirstRow();
				int rhc = this.isAjaxMode() ? this.getRowHeaderCount() : 0;
				for (int j = topLeft.getColumn(); j < rhc; ++j) {
					int renderedColumn = j;
					WModelIndex index = this.getModel().getIndex(i, j,
							this.getRootIndex());
					this.updateItem(index, renderedRow, renderedColumn);
				}
				for (int j = col1; j <= col2; ++j) {
					int renderedColumn = rhc + j - this.getFirstColumn();
					WModelIndex index = this.getModel().getIndex(i, j,
							this.getRootIndex());
					this.updateItem(index, renderedRow, renderedColumn);
				}
			}
		}
	}

	void modelLayoutChanged() {
		super.modelLayoutChanged();
		this.resetGeometry();
	}

	private WWidget renderWidget(WWidget widget, WModelIndex index) {
		WAbstractItemDelegate itemDelegate = this.getItemDelegate(index
				.getColumn());
		EnumSet<ViewItemRenderFlag> renderFlags = EnumSet
				.noneOf(ViewItemRenderFlag.class);
		if (this.isAjaxMode()) {
			if (this.isSelected(index)) {
				renderFlags.add(ViewItemRenderFlag.RenderSelected);
			}
		}
		if (this.isEditing(index)) {
			renderFlags.add(ViewItemRenderFlag.RenderEditing);
			if (this.hasEditFocus(index)) {
				renderFlags.add(ViewItemRenderFlag.RenderFocused);
			}
		}
		if (!this.isValid(index)) {
			renderFlags.add(ViewItemRenderFlag.RenderInvalid);
		}
		boolean initial = !(widget != null);
		widget = itemDelegate.update(widget, index, renderFlags);
		widget.setInline(false);
		widget.addStyleClass("Wt-tv-c");
		widget.setHeight(this.getRowHeight());
		if (!EnumUtils.mask(renderFlags, ViewItemRenderFlag.RenderEditing)
				.isEmpty()) {
			widget.setTabIndex(-1);
			this.setEditorWidget(index, widget);
		}
		if (initial) {
			if (!EnumUtils.mask(renderFlags, ViewItemRenderFlag.RenderEditing)
					.isEmpty()) {
				Object state = this.getEditState(index);
				if (!(state == null)) {
					itemDelegate.setEditState(widget, state);
				}
			}
		}
		return widget;
	}

	private int getSpannerCount(final Side side) {
		assert this.isAjaxMode();
		switch (side) {
		case Top: {
			return (int) (this.table_.getOffset(Side.Top).toPixels() / this
					.getRowHeight().toPixels());
		}
		case Bottom: {
			return (int) (this.getModel().getRowCount(this.getRootIndex()) - (this.table_
					.getOffset(Side.Top).toPixels() + this.table_.getHeight()
					.toPixels())
					/ this.getRowHeight().toPixels());
		}
		case Left:
			return this.firstColumn_ - this.getRowHeaderCount();
		case Right:
			return this.getColumnCount() - (this.lastColumn_ + 1);
		default:
			assert false;
			return -1;
		}
	}

	private void setSpannerCount(final Side side, final int count) {
		assert this.isAjaxMode();
		switch (side) {
		case Top: {
			int size = this.getModel().getRowCount(this.getRootIndex()) - count
					- this.getSpannerCount(Side.Bottom);
			double to = count * this.getRowHeight().toPixels();
			this.table_.setOffsets(new WLength(to), EnumSet.of(Side.Top));
			this.headerColumnsTable_.setOffsets(new WLength(to), EnumSet
					.of(Side.Top));
			double th = size * this.getRowHeight().toPixels();
			this.setRenderedHeight(th);
			break;
		}
		case Bottom: {
			int size = this.getModel().getRowCount(this.getRootIndex())
					- this.getSpannerCount(Side.Top) - count;
			double th = size * this.getRowHeight().toPixels();
			this.setRenderedHeight(th);
			break;
		}
		case Left: {
			int total = 0;
			for (int i = this.getRowHeaderCount(); i < count; i++) {
				if (!this.columnInfo(i).hidden) {
					total += (int) this.columnInfo(i).width.toPixels() + 7;
				}
			}
			this.table_.setOffsets(new WLength(total), EnumSet.of(Side.Left));
			this.firstColumn_ = count + this.getRowHeaderCount();
			break;
		}
		case Right:
			this.lastColumn_ = this.getColumnCount() - count - 1;
			break;
		default:
			assert false;
		}
	}

	private void renderTable(final int fr, final int lr, final int fc,
			final int lc) {
		assert this.isAjaxMode();
		if (fr > this.getLastRow() || this.getFirstRow() > lr
				|| fc > this.getLastColumn() || this.getFirstColumn() > lc) {
			this.reset();
		}
		int oldFirstRow = this.getFirstRow();
		int oldLastRow = this.getLastRow();
		int topRowsToAdd = 0;
		int bottomRowsToAdd = 0;
		if (oldLastRow - oldFirstRow < 0) {
			topRowsToAdd = 0;
			this.setSpannerCount(Side.Top, fr);
			this.setSpannerCount(Side.Bottom, this.getModel().getRowCount(
					this.getRootIndex())
					- fr);
			bottomRowsToAdd = lr - fr + 1;
		} else {
			topRowsToAdd = this.getFirstRow() - fr;
			bottomRowsToAdd = lr - this.getLastRow();
		}
		int oldFirstCol = this.getFirstColumn();
		int oldLastCol = this.getLastColumn();
		int leftColsToAdd = 0;
		int rightColsToAdd = 0;
		if (oldLastCol - oldFirstCol < 0) {
			leftColsToAdd = 0;
			this.setSpannerCount(Side.Left, fc - this.getRowHeaderCount());
			this.setSpannerCount(Side.Right, this.getColumnCount() - fc);
			rightColsToAdd = lc - fc + 1;
		} else {
			leftColsToAdd = this.getFirstColumn() - fc;
			rightColsToAdd = lc - this.getLastColumn();
		}
		for (int i = 0; i < -leftColsToAdd; ++i) {
			this.removeSection(Side.Left);
		}
		for (int i = 0; i < -rightColsToAdd; ++i) {
			this.removeSection(Side.Right);
		}
		for (int i = 0; i < -topRowsToAdd; ++i) {
			this.removeSection(Side.Top);
		}
		for (int i = 0; i < -bottomRowsToAdd; ++i) {
			this.removeSection(Side.Bottom);
		}
		for (int i = 0; i < topRowsToAdd; i++) {
			int row = this.getFirstRow() - 1;
			List<WWidget> items = new ArrayList<WWidget>();
			for (int j = 0; j < this.getRowHeaderCount(); ++j) {
				items.add(this.renderWidget((WWidget) null, this.getModel()
						.getIndex(row, j, this.getRootIndex())));
			}
			for (int j = this.getFirstColumn(); j <= this.getLastColumn(); ++j) {
				items.add(this.renderWidget((WWidget) null, this.getModel()
						.getIndex(row, j, this.getRootIndex())));
			}
			this.addSection(Side.Top, items);
		}
		for (int i = 0; i < bottomRowsToAdd; ++i) {
			int row = this.getLastRow() + 1;
			List<WWidget> items = new ArrayList<WWidget>();
			for (int j = 0; j < this.getRowHeaderCount(); ++j) {
				items.add(this.renderWidget((WWidget) null, this.getModel()
						.getIndex(row, j, this.getRootIndex())));
			}
			for (int j = this.getFirstColumn(); j <= this.getLastColumn(); ++j) {
				items.add(this.renderWidget((WWidget) null, this.getModel()
						.getIndex(row, j, this.getRootIndex())));
			}
			this.addSection(Side.Bottom, items);
		}
		for (int i = 0; i < leftColsToAdd; ++i) {
			int col = this.getFirstColumn() - 1;
			List<WWidget> items = new ArrayList<WWidget>();
			int nfr = this.getFirstRow();
			int nlr = this.getLastRow();
			for (int j = nfr; j <= nlr; ++j) {
				items.add(this.renderWidget((WWidget) null, this.getModel()
						.getIndex(j, col, this.getRootIndex())));
			}
			this.addSection(Side.Left, items);
		}
		for (int i = 0; i < rightColsToAdd; ++i) {
			int col = this.getLastColumn() + 1;
			List<WWidget> items = new ArrayList<WWidget>();
			int nfr = this.getFirstRow();
			int nlr = this.getLastRow();
			for (int j = nfr; j <= nlr; ++j) {
				items.add(this.renderWidget((WWidget) null, this.getModel()
						.getIndex(j, col, this.getRootIndex())));
			}
			this.addSection(Side.Right, items);
		}
		this.updateColumnOffsets();
		int scrollX1 = Math
				.max(0, this.viewportLeft_ - this.viewportWidth_ / 2);
		int scrollX2 = this.viewportLeft_ + this.viewportWidth_ / 2;
		int scrollY1 = Math
				.max(0, this.viewportTop_ - this.viewportHeight_ / 2);
		int scrollY2 = this.viewportTop_ + this.viewportHeight_ / 2;
		StringBuilder s = new StringBuilder();
		s.append("jQuery.data(").append(this.getJsRef()).append(
				", 'obj').scrolled(").append(scrollX1).append(", ").append(
				scrollX2).append(", ").append(scrollY1).append(", ").append(
				scrollY2).append(");");
		this.doJavaScript(s.toString());
	}

	private void addSection(final Side side, List<WWidget> items) {
		assert this.isAjaxMode();
		switch (side) {
		case Top:
			for (int i = 0; i < items.size(); ++i) {
				WTableView.ColumnWidget w = this.columnContainer(i);
				w.insertWidget(0, items.get(i));
			}
			this.setSpannerCount(side, this.getSpannerCount(side) - 1);
			break;
		case Bottom:
			for (int i = 0; i < items.size(); ++i) {
				WTableView.ColumnWidget w = this.columnContainer(i);
				w.addWidget(items.get(i));
			}
			this.setSpannerCount(side, this.getSpannerCount(side) - 1);
			break;
		case Left: {
			WTableView.ColumnWidget w = new WTableView.ColumnWidget(this, this
					.getFirstColumn() - 1);
			for (int i = 0; i < items.size(); ++i) {
				w.addWidget(items.get(i));
			}
			if (!this.columnInfo(w.getColumn()).hidden) {
				this.table_.setOffsets(new WLength(this.table_.getOffset(
						Side.Left).toPixels()
						- this.getColumnWidth(w.getColumn()).toPixels() - 7),
						EnumSet.of(Side.Left));
			} else {
				w.hide();
			}
			--this.firstColumn_;
			break;
		}
		case Right: {
			WTableView.ColumnWidget w = new WTableView.ColumnWidget(this, this
					.getLastColumn() + 1);
			for (int i = 0; i < items.size(); ++i) {
				w.addWidget(items.get(i));
			}
			if (this.columnInfo(w.getColumn()).hidden) {
				w.hide();
			}
			++this.lastColumn_;
			break;
		}
		default:
			assert false;
		}
	}

	private void removeSection(final Side side) {
		assert this.isAjaxMode();
		int row = this.getFirstRow();
		int col = this.getFirstColumn();
		switch (side) {
		case Top:
			this.setSpannerCount(side, this.getSpannerCount(side) + 1);
			for (int i = 0; i < this.getRenderedColumnsCount(); ++i) {
				WTableView.ColumnWidget w = this.columnContainer(i);
				this.deleteItem(row, col + i, w.getWidget(0));
			}
			break;
		case Bottom:
			row = this.getLastRow();
			this.setSpannerCount(side, this.getSpannerCount(side) + 1);
			for (int i = 0; i < this.getRenderedColumnsCount(); ++i) {
				WTableView.ColumnWidget w = this.columnContainer(i);
				this.deleteItem(row, col + i, w.getWidget(w.getCount() - 1));
			}
			break;
		case Left: {
			WTableView.ColumnWidget w = this.columnContainer(this
					.getRowHeaderCount());
			if (!this.columnInfo(w.getColumn()).hidden) {
				this.table_.setOffsets(new WLength(this.table_.getOffset(
						Side.Left).toPixels()
						+ this.getColumnWidth(w.getColumn()).toPixels() + 7),
						EnumSet.of(Side.Left));
			}
			++this.firstColumn_;
			for (int i = w.getCount() - 1; i >= 0; --i) {
				this.deleteItem(row + i, col, w.getWidget(i));
			}
			if (w != null)
				w.remove();
			break;
		}
		case Right: {
			WTableView.ColumnWidget w = this.columnContainer(-1);
			col = w.getColumn();
			--this.lastColumn_;
			for (int i = w.getCount() - 1; i >= 0; --i) {
				this.deleteItem(row + i, col, w.getWidget(i));
			}
			if (w != null)
				w.remove();
			break;
		}
		default:
			break;
		}
	}

	private int getFirstRow() {
		if (this.isAjaxMode()) {
			return this.getSpannerCount(Side.Top);
		} else {
			return this.renderedFirstRow_;
		}
	}

	private int getLastRow() {
		if (this.isAjaxMode()) {
			return this.getModel().getRowCount(this.getRootIndex())
					- this.getSpannerCount(Side.Bottom) - 1;
		} else {
			return this.renderedLastRow_;
		}
	}

	private int getFirstColumn() {
		if (this.isAjaxMode()) {
			return this.firstColumn_;
		} else {
			return 0;
		}
	}

	private int getLastColumn() {
		if (this.isAjaxMode()) {
			return this.lastColumn_;
		} else {
			return this.getColumnCount() - 1;
		}
	}

	void render(EnumSet<RenderFlag> flags) {
		if (this.isAjaxMode()) {
			if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
				this.defineJavaScript();
			}
			if (!this.canvas_.doubleClicked().isConnected()
					&& (!EnumUtils.mask(this.getEditTriggers(),
							WAbstractItemView.EditTrigger.DoubleClicked)
							.isEmpty() || this.doubleClicked().isConnected())) {
				this.canvas_.doubleClicked().addListener(this,
						new Signal1.Listener<WMouseEvent>() {
							public void trigger(WMouseEvent event) {
								WTableView.this.handleDoubleClick(false, event);
							}
						});
				this.headerColumnsCanvas_.doubleClicked().addListener(this,
						new Signal1.Listener<WMouseEvent>() {
							public void trigger(WMouseEvent event) {
								WTableView.this.handleDoubleClick(true, event);
							}
						});
			}
		}
		if (this.getModel() != null) {
			while (this.renderState_ != WAbstractItemView.RenderState.RenderOk) {
				WAbstractItemView.RenderState s = this.renderState_;
				this.renderState_ = WAbstractItemView.RenderState.RenderOk;
				switch (s) {
				case NeedRerender:
					this.resetGeometry();
					this.rerenderHeader();
					this.rerenderData();
					break;
				case NeedRerenderHeader:
					this.rerenderHeader();
					break;
				case NeedRerenderData:
					this.rerenderData();
					break;
				case NeedAdjustViewPort:
					this.adjustToViewport();
					break;
				default:
					break;
				}
			}
		}
		super.render(flags);
	}

	private void reset() {
		assert this.isAjaxMode();
		int total = 0;
		for (int i = 0; i < this.getColumnCount(); ++i) {
			if (!this.columnInfo(i).hidden) {
				total += (int) this.columnInfo(i).width.toPixels() + 7;
			}
		}
		this.headers_.setWidth(new WLength(total));
		this.canvas_.resize(new WLength(total), new WLength(this
				.getCanvasHeight()));
		this.headerColumnsCanvas_
				.setHeight(new WLength(this.getCanvasHeight()));
		this.computeRenderedArea();
		int renderedRows = this.getLastRow() - this.getFirstRow() + 1;
		for (int i = 0; i < renderedRows; ++i) {
			this.removeSection(Side.Top);
		}
		this.setSpannerCount(Side.Top, 0);
		this.setSpannerCount(Side.Left, 0);
		this.table_.clear();
		this.setSpannerCount(Side.Bottom, this.getModel().getRowCount(
				this.getRootIndex()));
		this.setSpannerCount(Side.Right, this.getColumnCount()
				- this.getRowHeaderCount());
		this.headerColumnsTable_.clear();
		for (int i = 0; i < this.getRowHeaderCount(); ++i) {
			new WTableView.ColumnWidget(this, i);
		}
	}

	private void rerenderHeader() {
		this.saveExtraHeaderWidgets();
		WApplication app = WApplication.getInstance();
		if (this.isAjaxMode()) {
			this.headers_.clear();
			for (int i = 0; i < this.getColumnCount(); ++i) {
				WWidget w = this.createHeaderWidget(app, i);
				w.setFloatSide(Side.Left);
				if (i < this.getRowHeaderCount()) {
					this.headerColumnsHeaderContainer_.addWidget(w);
				} else {
					this.headers_.addWidget(w);
				}
				w
						.setWidth(new WLength(this.columnInfo(i).width
								.toPixels() + 1));
				if (this.columnInfo(i).hidden) {
					w.hide();
				}
			}
		} else {
			for (int i = 0; i < this.getColumnCount(); ++i) {
				WWidget w = this.createHeaderWidget(app, i);
				WTableCell cell = this.plainTable_.getElementAt(0, i);
				cell.clear();
				cell.setStyleClass("headerrh");
				cell.addWidget(w);
				w
						.setWidth(new WLength(this.columnInfo(i).width
								.toPixels() + 1));
				cell.resize(
						new WLength(this.columnInfo(i).width.toPixels() + 1), w
								.getHeight());
				if (this.columnInfo(i).hidden) {
					cell.hide();
				}
			}
		}
	}

	private void rerenderData() {
		if (this.isAjaxMode()) {
			this.reset();
			this.renderTable(this.renderedFirstRow_, this.renderedLastRow_,
					this.renderedFirstColumn_, this.renderedLastColumn_);
		} else {
			this.pageChanged().trigger();
			while (this.plainTable_.getRowCount() > 1) {
				this.plainTable_.deleteRow(this.plainTable_.getRowCount() - 1);
			}
			for (int i = this.getFirstRow(); i <= this.getLastRow(); ++i) {
				int renderedRow = i - this.getFirstRow();
				if (this.getSelectionBehavior() == SelectionBehavior.SelectRows
						&& this.isSelected(this.getModel().getIndex(i, 0,
								this.getRootIndex()))) {
					WTableRow row = this.plainTable_.getRowAt(renderedRow + 1);
					row.setStyleClass("Wt-selected");
				}
				for (int j = this.getFirstColumn(); j <= this.getLastColumn(); ++j) {
					int renderedCol = j - this.getFirstColumn();
					WModelIndex index = this.getModel().getIndex(i, j,
							this.getRootIndex());
					WWidget w = this.renderWidget((WWidget) null, index);
					WTableCell cell = this.plainTable_.getElementAt(
							renderedRow + 1, renderedCol);
					if (this.columnInfo(j).hidden) {
						cell.hide();
					}
					cell.addWidget(w);
					WInteractWidget wi = ((w) instanceof WInteractWidget ? (WInteractWidget) (w)
							: null);
					if (wi != null && !this.isEditing(index)) {
						this.clickedMapper_.mapConnect1(wi.clicked(), index);
					}
					if (this.getSelectionBehavior() == SelectionBehavior.SelectItems
							&& this.isSelected(index)) {
						cell.setStyleClass("Wt-selected");
					}
				}
			}
		}
	}

	private void adjustToViewport() {
		assert this.isAjaxMode();
		if (this.renderedFirstRow_ != this.getFirstRow()
				|| this.renderedLastRow_ != this.getLastRow()
				|| this.renderedFirstColumn_ != this.getFirstColumn()
				|| this.renderedLastColumn_ != this.getLastColumn()) {
			this.renderTable(this.renderedFirstRow_, this.renderedLastRow_,
					this.renderedFirstColumn_, this.renderedLastColumn_);
		}
	}

	private void computeRenderedArea() {
		if (this.isAjaxMode()) {
			final int borderRows = 5;
			final int borderColumnPixels = 200;
			int top = Math.min(this.viewportTop_, (int) this.canvas_
					.getHeight().toPixels());
			int height = Math.min(this.viewportHeight_, (int) this.canvas_
					.getHeight().toPixels());
			this.renderedFirstRow_ = (int) (top / this.getRowHeight()
					.toPixels());
			int renderedRows = (int) (height / this.getRowHeight().toPixels() + 0.5);
			if (this.getModel() != null) {
				this.renderedLastRow_ = Math.min(this.renderedFirstRow_
						+ renderedRows * 2 + borderRows, this.getModel()
						.getRowCount(this.getRootIndex()) - 1);
			}
			this.renderedFirstRow_ = Math.max(this.renderedFirstRow_
					- renderedRows - borderRows, 0);
			if (this.renderedFirstRow_ % 2 == 1) {
				--this.renderedFirstRow_;
			}
			int left = Math.max(0, this.viewportLeft_ - this.viewportWidth_
					- borderColumnPixels);
			int right = Math.min((int) this.canvas_.getWidth().toPixels(),
					this.viewportLeft_ + 2 * this.viewportWidth_
							+ borderColumnPixels);
			int total = 0;
			this.renderedFirstColumn_ = this.getRowHeaderCount();
			this.renderedLastColumn_ = this.getColumnCount() - 1;
			for (int i = this.getRowHeaderCount(); i < this.getColumnCount(); i++) {
				if (this.columnInfo(i).hidden) {
					continue;
				}
				int w = (int) this.columnInfo(i).width.toPixels();
				if (total <= left && left < total + w) {
					this.renderedFirstColumn_ = i;
				}
				if (total <= right && right < total + w) {
					this.renderedLastColumn_ = i;
					break;
				}
				total += w + 7;
			}
			assert this.renderedLastColumn_ == -1
					|| this.renderedFirstColumn_ <= this.renderedLastColumn_;
		} else {
			this.renderedFirstColumn_ = 0;
			if (this.getModel() != null) {
				this.renderedLastColumn_ = this.getColumnCount() - 1;
				int cp = Math.max(0, Math.min(this.getCurrentPage(), this
						.getPageCount() - 1));
				this.setCurrentPage(cp);
			} else {
				this.renderedFirstRow_ = this.renderedLastRow_ = 0;
			}
		}
	}

	WContainerWidget getHeaderContainer() {
		return this.headerContainer_;
	}

	WWidget headerWidget(int column, boolean contentsOnly) {
		WWidget result = null;
		if (this.isAjaxMode()) {
			if (this.headers_ != null) {
				if (column < this.getRowHeaderCount()) {
					if (column < this.headerColumnsHeaderContainer_.getCount()) {
						result = this.headerColumnsHeaderContainer_
								.getWidget(column);
					}
				} else {
					if (column - this.getRowHeaderCount() < this.headers_
							.getCount()) {
						result = this.headers_.getWidget(column
								- this.getRowHeaderCount());
					}
				}
			}
		} else {
			if (this.plainTable_ != null
					&& column < this.plainTable_.getColumnCount()) {
				result = this.plainTable_.getElementAt(0, column).getWidget(0);
			}
		}
		if (result != null && contentsOnly) {
			return result.find("contents");
		} else {
			return result;
		}
	}

	private void onViewportChange(int left, int top, int width, int height) {
		assert this.isAjaxMode();
		this.viewportLeft_ = left;
		this.viewportWidth_ = width;
		this.viewportTop_ = top;
		this.viewportHeight_ = height;
		this.computeRenderedArea();
		this.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
	}

	private void resetGeometry() {
		if (this.isAjaxMode()) {
			this.reset();
		} else {
			this.renderedLastRow_ = Math.min(this.getModel().getRowCount(
					this.getRootIndex()) - 1, this.renderedFirstRow_
					+ this.getPageSize() - 1);
			this.renderedLastColumn_ = this.getColumnCount() - 1;
		}
	}

	private void handleSingleClick(boolean headerColumns, WMouseEvent event) {
		WModelIndex index = this.translateModelIndex(headerColumns, event);
		if ((index != null)) {
			super.handleClick(index, event);
		}
	}

	private void handleDoubleClick(boolean headerColumns, WMouseEvent event) {
		WModelIndex index = this.translateModelIndex(headerColumns, event);
		if ((index != null)) {
			super.handleDoubleClick(index, event);
		}
	}

	private void handleMouseWentDown(boolean headerColumns, WMouseEvent event) {
		WModelIndex index = this.translateModelIndex(headerColumns, event);
		if ((index != null)) {
			super.handleMouseDown(index, event);
		}
	}

	private void handleMouseWentUp(boolean headerColumns, WMouseEvent event) {
		WModelIndex index = this.translateModelIndex(headerColumns, event);
		if ((index != null)) {
			super.handleMouseUp(index, event);
		}
	}

	private WModelIndex translateModelIndex(boolean headerColumns,
			WMouseEvent event) {
		int row = (int) (event.getWidget().y / this.getRowHeight().toPixels());
		int column = -1;
		int total = 0;
		if (headerColumns) {
			for (int i = 0; i < this.getRowHeaderCount(); ++i) {
				if (!this.columnInfo(i).hidden) {
					total += (int) this.columnInfo(i).width.toPixels() + 7;
				}
				if (event.getWidget().x < total) {
					column = i;
					break;
				}
			}
		} else {
			for (int i = this.getRowHeaderCount(); i < this.getColumnCount(); i++) {
				if (!this.columnInfo(i).hidden) {
					total += (int) this.columnInfo(i).width.toPixels() + 7;
				}
				if (event.getWidget().x < total) {
					column = i;
					break;
				}
			}
		}
		if (column >= 0 && row >= 0
				&& row < this.getModel().getRowCount(this.getRootIndex())) {
			return this.getModel().getIndex(row, column, this.getRootIndex());
		} else {
			return null;
		}
	}

	private void updateItem(WModelIndex index, int renderedRow,
			int renderedColumn) {
		WContainerWidget parentWidget;
		int wIndex;
		if (this.isAjaxMode()) {
			parentWidget = this.columnContainer(renderedColumn);
			wIndex = renderedRow;
		} else {
			parentWidget = this.plainTable_.getElementAt(renderedRow + 1,
					renderedColumn);
			wIndex = 0;
		}
		WWidget current = parentWidget.getWidget(wIndex);
		WWidget w = this.renderWidget(current, index);
		if (!(w.getParent() != null)) {
			if (current != null)
				current.remove();
			parentWidget.insertWidget(wIndex, w);
			if (!this.isAjaxMode() && !this.isEditing(index)) {
				WInteractWidget wi = ((w) instanceof WInteractWidget ? (WInteractWidget) (w)
						: null);
				if (wi != null) {
					this.clickedMapper_.mapConnect1(wi.clicked(), index);
				}
			}
		}
	}

	boolean internalSelect(WModelIndex index, SelectionFlag option) {
		if (this.getSelectionBehavior() == SelectionBehavior.SelectRows
				&& index.getColumn() != 0) {
			return this.internalSelect(this.getModel().getIndex(index.getRow(),
					0, index.getParent()), option);
		}
		if (super.internalSelect(index, option)) {
			this.renderSelected(this.isSelected(index), index);
			return true;
		} else {
			return false;
		}
	}

	void selectRange(WModelIndex first, WModelIndex last) {
		for (int c = first.getColumn(); c <= last.getColumn(); ++c) {
			for (int r = first.getRow(); r <= last.getRow(); ++r) {
				this.internalSelect(this.getModel().getIndex(r, c,
						this.getRootIndex()), SelectionFlag.Select);
			}
		}
	}

	private void shiftModelIndexRows(int start, int count) {
		SortedSet<WModelIndex> set = this.getSelectionModel().selection_;
		List<WModelIndex> toShift = new ArrayList<WModelIndex>();
		List<WModelIndex> toErase = new ArrayList<WModelIndex>();
		for (Iterator<WModelIndex> it_it = set.tailSet(
				this.getModel().getIndex(start, 0, this.getRootIndex()))
				.iterator(); it_it.hasNext();) {
			WModelIndex it = it_it.next();
			if (count < 0) {
				if (it.getRow() < start - count) {
					toErase.add(it);
					continue;
				}
			}
			toShift.add(it);
			toErase.add(it);
		}
		for (int i = 0; i < toErase.size(); ++i) {
			set.remove(toErase.get(i));
		}
		for (int i = 0; i < toShift.size(); ++i) {
			WModelIndex newIndex = this.getModel().getIndex(
					toShift.get(i).getRow() + count,
					toShift.get(i).getColumn(), toShift.get(i).getParent());
			set.add(newIndex);
		}
		this.shiftEditorRows(this.getRootIndex(), start, count, true);
		if (!toErase.isEmpty()) {
			this.selectionChanged().trigger();
		}
	}

	private void shiftModelIndexColumns(int start, int count) {
		SortedSet<WModelIndex> set = this.getSelectionModel().selection_;
		List<WModelIndex> toShift = new ArrayList<WModelIndex>();
		List<WModelIndex> toErase = new ArrayList<WModelIndex>();
		for (Iterator<WModelIndex> it_it = set.iterator(); it_it.hasNext();) {
			WModelIndex it = it_it.next();
			if (count < 0) {
				if (it.getColumn() < start - count) {
					toErase.add(it);
					continue;
				}
			}
			if (it.getColumn() >= start) {
				toShift.add(it);
				toErase.add(it);
			}
		}
		for (int i = 0; i < toErase.size(); ++i) {
			set.remove(toErase.get(i));
		}
		for (int i = 0; i < toShift.size(); ++i) {
			WModelIndex newIndex = this.getModel().getIndex(
					toShift.get(i).getRow(),
					toShift.get(i).getColumn() + count,
					toShift.get(i).getParent());
			set.add(newIndex);
		}
		this.shiftEditorColumns(this.getRootIndex(), start, count, true);
		if (!toErase.isEmpty()) {
			this.selectionChanged().trigger();
		}
	}

	private void renderSelected(boolean selected, WModelIndex index) {
		if (this.getSelectionBehavior() == SelectionBehavior.SelectRows) {
			if (this.isRowRendered(index.getRow())) {
				int renderedRow = index.getRow() - this.getFirstRow();
				if (this.isAjaxMode()) {
					for (int i = 0; i < this.getRenderedColumnsCount(); ++i) {
						WTableView.ColumnWidget column = this
								.columnContainer(i);
						WWidget w = column.getWidget(renderedRow);
						if (selected) {
							w.addStyleClass("Wt-selected");
						} else {
							w.removeStyleClass("Wt-selected");
						}
					}
				} else {
					WTableRow row = this.plainTable_.getRowAt(renderedRow + 1);
					row.setStyleClass(selected ? "Wt-selected" : "");
				}
			}
		} else {
			WWidget w = this.itemWidget(index);
			if (w != null) {
				if (selected) {
					w.addStyleClass("Wt-selected");
				} else {
					w.removeStyleClass("Wt-selected");
				}
			}
		}
	}

	private int getRenderedColumnsCount() {
		assert this.isAjaxMode();
		return this.headerColumnsTable_.getCount() + this.table_.getCount();
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WTableView.js", wtjs1());
		this.setJavaScriptMember(" WTableView", "new Wt3_2_3.WTableView("
				+ app.getJavaScriptClass() + "," + this.getJsRef() + ","
				+ this.contentsContainer_.getJsRef() + ","
				+ this.headerContainer_.getJsRef() + ","
				+ this.headerColumnsContainer_.getJsRef() + ");");
		if (this.viewportTop_ != 0) {
			StringBuilder s = new StringBuilder();
			s.append("function(o, w, h) {").append("if (!o.scrollTopSet) {")
					.append("o.scrollTop = ").append(this.viewportTop_).append(
							";").append("o.onscroll();").append(
							"o.scrollTopSet = true;").append("}").append("}");
			this.contentsContainer_.setJavaScriptMember(WT_RESIZE_JS, s
					.toString());
		}
	}

	private boolean isRowRendered(final int row) {
		return row >= this.getFirstRow() && row <= this.getLastRow();
	}

	private boolean isColumnRendered(final int column) {
		return column >= this.getFirstColumn()
				&& column <= this.getLastColumn();
	}

	private void updateColumnOffsets() {
		assert this.isAjaxMode();
		int totalRendered = 0;
		for (int i = 0; i < this.getRowHeaderCount(); ++i) {
			WAbstractItemView.ColumnInfo ci = this.columnInfo(i);
			WTableView.ColumnWidget w = this.columnContainer(i);
			w.setOffsets(new WLength(totalRendered), EnumSet.of(Side.Left));
			w.setWidth(new WLength(ci.width.toPixels() + 7));
			if (!this.columnInfo(i).hidden) {
				totalRendered += (int) ci.width.toPixels() + 7;
			}
			w.setHidden(ci.hidden);
		}
		this.headerColumnsContainer_.setWidth(new WLength(totalRendered));
		this.headerColumnsCanvas_.setWidth(new WLength(totalRendered));
		this.headerColumnsTable_.setWidth(new WLength(totalRendered));
		this.headerColumnsHeaderContainer_.setWidth(new WLength(totalRendered));
		this.headerColumnsContainer_.setHidden(totalRendered == 0);
		this.headerColumnsHeaderContainer_.setHidden(totalRendered == 0);
		int fc = this.getFirstColumn();
		int lc = this.getLastColumn();
		totalRendered = 0;
		int total = 0;
		for (int i = this.getRowHeaderCount(); i < this.getColumnCount(); ++i) {
			WAbstractItemView.ColumnInfo ci = this.columnInfo(i);
			if (i >= fc && i <= lc) {
				WTableView.ColumnWidget w = this.columnContainer(this
						.getRowHeaderCount()
						+ i - fc);
				w.setOffsets(new WLength(totalRendered), EnumSet.of(Side.Left));
				w.setWidth(new WLength(ci.width.toPixels() + 7));
				if (!this.columnInfo(i).hidden) {
					totalRendered += (int) ci.width.toPixels() + 7;
				}
				w.setHidden(ci.hidden);
			}
			if (!this.columnInfo(i).hidden) {
				total += (int) this.columnInfo(i).width.toPixels() + 7;
			}
		}
		double ch = this.getCanvasHeight();
		this.canvas_.resize(new WLength(total), new WLength(ch));
		this.headerColumnsCanvas_.setHeight(new WLength(ch));
		this.headers_.setWidth(new WLength(total));
		this.table_.setWidth(new WLength(totalRendered));
	}

	private void onDropEvent(int renderedRow, int columnId, String sourceId,
			String mimeType, WMouseEvent event) {
		WDropEvent e = new WDropEvent(WApplication.getInstance().decodeObject(
				sourceId), mimeType, event);
		WModelIndex index = this.getModel().getIndex(
				this.getFirstRow() + renderedRow, this.columnById(columnId),
				this.getRootIndex());
		this.dropEvent(e, index);
	}

	private void deleteItem(int row, int col, WWidget w) {
		this.persistEditor(this.getModel().getIndex(row, col,
				this.getRootIndex()));
		if (w != null)
			w.remove();
	}

	private boolean isAjaxMode() {
		return this.table_ != null;
	}

	private double getCanvasHeight() {
		return Math.max(1.0, this.getModel().getRowCount(this.getRootIndex())
				* this.getRowHeight().toPixels());
	}

	private void setRenderedHeight(double th) {
		this.table_.setHeight(new WLength(th));
		this.headerColumnsTable_.setHeight(new WLength(th));
		for (int i = 0; i < this.getRenderedColumnsCount(); ++i) {
			WTableView.ColumnWidget w = this.columnContainer(i);
			w.setHeight(new WLength(th));
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WTableView",
				"function(q,i,d,n,o){function v(a){var b=-1,c=-1,e=false,k=false,h=null;for(a=f.target(a);a;){var g=$(a);if(g.hasClass(\"Wt-tv-contents\"))break;else if(g.hasClass(\"Wt-tv-c\")){if(a.getAttribute(\"drop\")===\"true\")k=true;if(g.hasClass(\"Wt-selected\"))e=true;h=a;a=a.parentNode;b=a.className.split(\" \")[0].substring(7)*1;c=g.index();break}a=a.parentNode}return{columnId:b,rowIdx:c,selected:e,drop:k,el:h}}function C(){return f.pxself(d.firstChild,\"lineHeight\")} function w(a){var b,c,e=a.parentNode.childNodes;b=0;for(c=e.length;b<c;++b)if(e[b]==a)return b;return-1}function F(a,b){var c=$(document.body).hasClass(\"Wt-rtl\");if(c)b=-b;var e=a.className.split(\" \")[0],k=e.substring(7)*1,h=a.parentNode,g=h.parentNode!==n,j=g?o.firstChild:d.firstChild,l=j.firstChild;e=$(j).find(\".\"+e).get(0);var m=a.nextSibling,r=e.nextSibling,x=f.pxself(a,\"width\")-1+b,s=f.pxself(h,\"width\")+b+\"px\";h.style.width=j.style.width=l.style.width=s;if(g){o.style.width=s;o.firstChild.style.width= s;d.style.left=s;n.style.left=s}a.style.width=x+1+\"px\";for(e.style.width=x+7+\"px\";m;m=m.nextSibling)if(r){if(c)r.style.right=f.pxself(r,\"right\")+b+\"px\";else r.style.left=f.pxself(r,\"left\")+b+\"px\";r=r.nextSibling}q.emit(i,\"columnResized\",k,parseInt(x));G.autoJavaScript()}jQuery.data(i,\"obj\",this);var G=this,f=q.WT,y=0,z=0,A=0,B=0,t=0,u=0,D=0,E=0;d.onscroll=function(){u=n.scrollLeft=d.scrollLeft;t=o.scrollTop=d.scrollTop;if(!(d.scrollTop==0&&f.isAndroid))if(d.clientWidth&&d.clientHeight&&(d.scrollTop< A||d.scrollTop>B||d.scrollLeft<y||d.scrollLeft>z))q.emit(i,\"scrolled\",d.scrollLeft,d.scrollTop,d.clientWidth,d.clientHeight)};d.wtResize=function(a,b,c){if(b-D>(z-y)/2||c-E>(B-A)/2){D=b;E=c;q.emit(i,\"scrolled\",a.scrollLeft,a.scrollTop,a.clientWidth,a.clientHeight)}};this.mouseDown=function(a,b){f.capture(null);a=v(b);i.getAttribute(\"drag\")===\"true\"&&a.selected&&q._p_.dragStart(i,b)};this.resizeHandleMDown=function(a,b){var c=a.parentNode,e=-(f.pxself(c,\"width\")-1),k=1E4;if($(document.body).hasClass(\"Wt-rtl\")){var h= e;e=-k;k=-h}new f.SizeHandle(f,\"h\",a.offsetWidth,i.offsetHeight,e,k,\"Wt-hsh\",function(g){F(c,g)},a,i,b,-2,-1)};this.scrolled=function(a,b,c,e){y=a;z=b;A=c;B=e};this.resetScroll=function(){n.scrollLeft=u;d.scrollLeft=u;d.scrollTop=t;o.scrollTop=t};this.scrollTo=function(a,b,c){if(b!=-1){a=d.scrollTop;var e=d.clientHeight;if(c==0)if(a+e<b)c=1;else if(b<a)c=2;switch(c){case 1:d.scrollTop=b;break;case 2:d.scrollTop=b-(e-C());break;case 3:d.scrollTop=b-(e-C())/2;break}d.onscroll()}};var p=null;i.handleDragDrop= function(a,b,c,e,k){if(p){p.className=p.classNameOrig;p=null}if(a!=\"end\"){var h=v(c);if(!h.selected&&h.drop)if(a==\"drop\")q.emit(i,{name:\"dropEvent\",eventObject:b,event:c},h.rowIdx,h.columnId,e,k);else{b.className=\"Wt-valid-drop\";p=h.el;p.classNameOrig=p.className;p.className+=\" Wt-drop-site\"}else b.className=\"\"}};i.onkeydown=function(a){var b=a||window.event;if(b.keyCode==9){f.cancelEvent(b);var c=v(b);if(c.el){a=c.el.parentNode;c=w(c.el);var e=w(a),k=a.parentNode.childNodes.length,h=a.childNodes.length; b=b.shiftKey;for(var g=false,j=c,l;;){for(;b?j>=0:j<h;j=b?j-1:j+1)for(l=j==c&&!g?b?e-1:e+1:b?k-1:0;b?l>=0:l<k;l=b?l-1:l+1){if(j==c&&l==e)return;a=a.parentNode.childNodes[l];var m=$(a.childNodes[j]).find(\":input\");if(m.size()>0){setTimeout(function(){m.focus()},0);return}}j=b?h-1:0;g=true}}}else if(b.keyCode>=37&&b.keyCode<=40){g=f.target(b);if(g.nodeName!=\"select\"){c=v(b);if(c.el){a=c.el.parentNode;c=w(c.el);e=w(a);k=a.parentNode.childNodes.length;h=a.childNodes.length;switch(b.keyCode){case 39:if(f.hasTag(g, \"INPUT\")&&g.type==\"text\"){j=f.getSelectionRange(g);if(j.start!=g.value.length)return}e++;break;case 38:c--;break;case 37:if(f.hasTag(g,\"INPUT\")&&g.type==\"text\"){j=f.getSelectionRange(g);if(j.start!=0)return}e--;break;case 40:c++;break;default:return}f.cancelEvent(b);if(c>-1&&c<h&&e>-1&&e<k){a=a.parentNode.childNodes[e];m=$(a.childNodes[c]).find(\":input\");m.size()>0&&setTimeout(function(){m.focus()},0)}}}}};this.autoJavaScript=function(){if(i.parentNode==null){i=d=n=null;this.autoJavaScript=function(){}}else if(!f.isHidden(i)){if(!f.isIE&& (t!=d.scrollTop||u!=d.scrollLeft)){n.scrollLeft=d.scrollLeft=u;o.scrollTop=d.scrollTop=t}var a=i.offsetWidth-f.px(i,\"borderLeftWidth\")-f.px(i,\"borderRightWidth\"),b=d.offsetWidth-d.clientWidth;a-=b;a-=o.clientWidth;if(a>200&&a!=d.tw){d.tw=a;d.style.width=a+b+\"px\";n.style.width=a+\"px\";if(!f.isIE)n.style.marginRight=b+\"px\"}a=d.offsetHeight-d.clientHeight;if((b=o.style)&&b.marginBottom!==a+\"px\"){b.marginBottom=a+\"px\";q.layouts2.adjust(i.children[0].id,[[1,0]])}}}}");
	}
}
