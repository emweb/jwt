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

class WTreeViewNode extends WTemplate {
	private static Logger logger = LoggerFactory.getLogger(WTreeViewNode.class);

	public WTreeViewNode(WTreeView view, final WModelIndex index,
			int childrenHeight, boolean isLast, WTreeViewNode parent) {
		super(tr("Wt.WTreeViewNode.template"));
		this.view_ = view;
		this.index_ = index;
		this.childrenHeight_ = childrenHeight;
		this.parentNode_ = parent;
		this.childrenLoaded_ = false;
		this.bindEmpty("cols-row");
		this.bindEmpty("selected");
		this.bindEmpty("expand");
		this.bindEmpty("no-expand");
		this.bindEmpty("col0");
		this.bindEmpty("children");
		int selfHeight = 0;
		boolean needLoad = this.view_.isExpanded(this.index_);
		if (!(this.index_ == this.view_.getRootIndex() || (this.index_ != null && this.index_
				.equals(this.view_.getRootIndex()))) && !needLoad) {
			this.getChildContainer().hide();
		}
		if (needLoad) {
			this.childrenLoaded_ = true;
			if (this.childrenHeight_ == -1) {
				this.childrenHeight_ = this.view_.subTreeHeight(this.index_)
						- selfHeight;
			}
			if (this.childrenHeight_ > 0) {
				this.setTopSpacerHeight(this.childrenHeight_);
			}
		} else {
			this.childrenHeight_ = 0;
		}
		if (!(this.index_ == this.view_.getRootIndex() || (this.index_ != null && this.index_
				.equals(this.view_.getRootIndex())))) {
			this.updateGraphics(isLast,
					!this.view_.getModel().hasChildren(this.index_));
			this.insertColumns(0, this.view_.getColumnCount());
			selfHeight = 1;
			if (this.view_.getSelectionBehavior() == SelectionBehavior.SelectRows
					&& this.view_.isSelected(this.index_)) {
				this.renderSelected(true, 0);
			}
		}
		this.view_.addRenderedNode(this);
	}

	public void remove() {
		this.view_.removeRenderedNode(this);
		if (this.view_.isEditing()) {
			WModelIndex parent = this.index_.getParent();
			int thisNodeCount = this.view_.getModel().getColumnCount(parent);
			for (int i = 0; i < thisNodeCount; ++i) {
				WModelIndex child = this.childIndex(i);
				this.view_.persistEditor(child);
			}
		}
		super.remove();
	}

	public void update(int firstColumn, int lastColumn) {
		WModelIndex parent = this.index_.getParent();
		int thisNodeCount = this.view_.getModel().getColumnCount(parent);
		for (int i = firstColumn; i <= lastColumn; ++i) {
			WModelIndex child = i < thisNodeCount ? this.childIndex(i) : null;
			WWidget w = this.getCellWidget(i);
			EnumSet<ViewItemRenderFlag> renderFlags = EnumSet
					.noneOf(ViewItemRenderFlag.class);
			if (this.view_.getSelectionBehavior() == SelectionBehavior.SelectItems
					&& this.view_.isSelected(child)) {
				renderFlags.add(ViewItemRenderFlag.RenderSelected);
			}
			if (this.view_.isEditing(child)) {
				renderFlags.add(ViewItemRenderFlag.RenderEditing);
				if (this.view_.hasEditFocus(child)) {
					renderFlags.add(ViewItemRenderFlag.RenderFocused);
				}
			}
			if (!this.view_.isValid(child)) {
				renderFlags.add(ViewItemRenderFlag.RenderInvalid);
			}
			w = this.view_.getItemDelegate(i).update(w, child, renderFlags);
			if (!EnumUtils.mask(renderFlags, ViewItemRenderFlag.RenderEditing)
					.isEmpty()) {
				this.view_.setEditorWidget(child, w);
			}
			if (!(w.getParent() != null)) {
				this.setCellWidget(i, w);
				if (!EnumUtils.mask(renderFlags,
						ViewItemRenderFlag.RenderEditing).isEmpty()) {
					Object state = this.view_.getEditState(child);
					if (!(state == null)) {
						this.view_.getItemDelegate(i).setEditState(w, state);
					}
				}
			} else {
				this.addColumnStyleClass(i, w);
			}
		}
	}

	public void updateGraphics(boolean isLast, boolean isEmpty) {
		if ((this.index_ == this.view_.getRootIndex() || (this.index_ != null && this.index_
				.equals(this.view_.getRootIndex())))) {
			return;
		}
		if ((this.index_.getParent() == this.view_.getRootIndex() || (this.index_
				.getParent() != null && this.index_.getParent().equals(
				this.view_.getRootIndex())))
				&& !this.view_.isRootDecorated()) {
			this.bindEmpty("expand");
			this.bindEmpty("no-expand");
			this.bindEmpty("trunk-class");
			return;
		}
		if (!isEmpty) {
			ToggleButton expandButton = (ToggleButton) this
					.resolveWidget("expand");
			if (!(expandButton != null)) {
				this.bindEmpty("no-expand");
				expandButton = new ToggleButton(this.view_.expandConfig_);
				this.bindWidget("expand", expandButton);
				if (WApplication.getInstance().getEnvironment().agentIsIE()) {
					expandButton.setWidth(new WLength(19));
				}
				expandButton.signal(0).addListener(this, new Signal.Listener() {
					public void trigger() {
						WTreeViewNode.this.doExpand();
					}
				});
				expandButton.signal(1).addListener(this, new Signal.Listener() {
					public void trigger() {
						WTreeViewNode.this.doCollapse();
					}
				});
				expandButton.setState(this.isExpanded() ? 1 : 0);
			}
		} else {
			WText noExpandIcon = (WText) this.resolveWidget("no-expand");
			if (!(noExpandIcon != null)) {
				this.bindEmpty("expand");
				noExpandIcon = new WText();
				this.bindWidget("no-expand", noExpandIcon);
				noExpandIcon.setInline(false);
				noExpandIcon.setStyleClass("Wt-ctrl rh noexpand");
				if (WApplication.getInstance().getEnvironment().agentIsIE()) {
					noExpandIcon.setWidth(new WLength(19));
				}
			}
		}
		this.toggleStyleClass("Wt-trunk", !isLast);
		this.bindString("trunk-class", isLast ? "Wt-end" : "Wt-trunk");
	}

	public void insertColumns(int column, int count) {
		WContainerWidget row = (WContainerWidget) this
				.resolveWidget("cols-row");
		if (this.view_.getColumnCount() > 1) {
			if (!(row != null)) {
				row = new WContainerWidget();
				if (this.view_.getRowHeaderCount() != 0) {
					row.setStyleClass("Wt-tv-rowc rh");
					WContainerWidget rowWrap = new WContainerWidget();
					rowWrap.addWidget(row);
					row = rowWrap;
				}
				row.setStyleClass("Wt-tv-row rh");
				this.bindWidget("cols-row", row);
			}
		} else {
			if (row != null)
				row.remove();
		}
		this.update(0, this.view_.getColumnCount() - 1);
	}

	public void removeColumns(int column, int count) {
		this.insertColumns(0, 0);
	}

	public boolean isLast() {
		return (this.index_ == this.view_.getRootIndex() || (this.index_ != null && this.index_
				.equals(this.view_.getRootIndex())))
				|| this.index_.getRow() == this.view_.getModel().getRowCount(
						this.index_.getParent()) - 1;
	}

	public void rerenderSpacers() {
		RowSpacer s = this.topSpacer();
		if (s != null) {
			s.setRows(this.getTopSpacerHeight(), true);
		}
		s = this.bottomSpacer();
		if (s != null) {
			s.setRows(this.getBottomSpacerHeight(), true);
		}
	}

	public WModelIndex getModelIndex() {
		return this.index_;
	}

	public int getChildrenHeight() {
		return this.childrenHeight_;
	}

	public int getRenderedHeight() {
		return (this.index_ == this.view_.getRootIndex() || (this.index_ != null && this.index_
				.equals(this.view_.getRootIndex()))) ? this.childrenHeight_
				: 1 + (this.isExpanded() ? this.childrenHeight_ : 0);
	}

	public boolean isChildrenLoaded() {
		return this.childrenLoaded_;
	}

	public WWidget widgetForModelRow(int modelRow) {
		if (!this.childrenLoaded_) {
			return null;
		}
		WContainerWidget c = this.getChildContainer();
		int first = this.topSpacer() != null ? 1 : 0;
		if (first < c.getCount()) {
			WTreeViewNode n = ((c.getWidget(first)) instanceof WTreeViewNode ? (WTreeViewNode) (c
					.getWidget(first)) : null);
			if (n != null) {
				int row = this.getTopSpacerHeight();
				int index = first + (modelRow - row);
				if (index < first) {
					return this.topSpacer();
				} else {
					if (index < c.getCount()) {
						return c.getWidget(index);
					} else {
						return this.bottomSpacer();
					}
				}
			} else {
				return this.bottomSpacer();
			}
		} else {
			return this.topSpacer();
		}
	}

	public WTreeViewNode nextChildNode(WTreeViewNode prev) {
		if (!this.childrenLoaded_) {
			return null;
		}
		WContainerWidget c = this.getChildContainer();
		int nextI = prev != null ? c.getIndexOf(prev) + 1
				: this.topSpacer() != null ? 1 : 0;
		if (nextI < c.getCount()) {
			return ((c.getWidget(nextI)) instanceof WTreeViewNode ? (WTreeViewNode) (c
					.getWidget(nextI)) : null);
		} else {
			return null;
		}
	}

	public boolean isAllSpacer() {
		return this.childrenLoaded_ && this.topSpacer() != null
				&& this.topSpacer() == this.bottomSpacer();
	}

	public void setTopSpacerHeight(int rows) {
		if (rows == 0) {
			if (this.topSpacer() != null)
				this.topSpacer().remove();
		} else {
			this.topSpacer(true).setRows(rows);
		}
	}

	public void addTopSpacerHeight(int rows) {
		this.setTopSpacerHeight(this.getTopSpacerHeight() + rows);
	}

	public int getTopSpacerHeight() {
		RowSpacer s = this.topSpacer();
		return s != null ? s.getRows() : 0;
	}

	public void setBottomSpacerHeight(int rows) {
		if (!(rows != 0)) {
			if (this.bottomSpacer() != null)
				this.bottomSpacer().remove();
		} else {
			this.bottomSpacer(true).setRows(rows);
		}
	}

	public void addBottomSpacerHeight(int rows) {
		this.setBottomSpacerHeight(this.getBottomSpacerHeight() + rows);
	}

	public int getBottomSpacerHeight() {
		RowSpacer s = this.bottomSpacer();
		return s != null ? s.getRows() : 0;
	}

	public RowSpacer topSpacer(boolean create) {
		WContainerWidget c = this.getChildContainer();
		RowSpacer result = null;
		if (c.getCount() == 0
				|| !((result = ((c.getWidget(0)) instanceof RowSpacer ? (RowSpacer) (c
						.getWidget(0)) : null)) != null)) {
			if (!create) {
				return null;
			} else {
				result = new RowSpacer(this, 0);
				c.insertWidget(0, result);
			}
		}
		return result;
	}

	public final RowSpacer topSpacer() {
		return topSpacer(false);
	}

	public RowSpacer bottomSpacer(boolean create) {
		WContainerWidget c = this.getChildContainer();
		RowSpacer result = null;
		if (c.getCount() == 0
				|| !((result = ((c.getWidget(c.getCount() - 1)) instanceof RowSpacer ? (RowSpacer) (c
						.getWidget(c.getCount() - 1)) : null)) != null)) {
			if (!create) {
				return null;
			} else {
				result = new RowSpacer(this, 0);
				c.addWidget(result);
			}
		}
		return result;
	}

	public final RowSpacer bottomSpacer() {
		return bottomSpacer(false);
	}

	public WContainerWidget getChildContainer() {
		WContainerWidget result = (WContainerWidget) this
				.resolveWidget("children");
		if (!(result != null)) {
			result = new WContainerWidget();
			this.bindWidget("children", result);
			result.setList(true);
			if ((this.index_ == this.view_.getRootIndex() || (this.index_ != null && this.index_
					.equals(this.view_.getRootIndex())))) {
				result.addStyleClass("Wt-tv-root");
			}
		}
		return result;
	}

	public void shiftModelIndexes(int start, int offset) {
		if (!this.childrenLoaded_) {
			return;
		}
		WContainerWidget c = this.getChildContainer();
		int first;
		int end;
		int inc;
		if (offset > 0) {
			first = c.getCount() - 1;
			end = -1;
			inc = -1;
		} else {
			first = 0;
			end = c.getCount();
			inc = 1;
		}
		for (int i = first; i != end; i += inc) {
			WTreeViewNode n = ((c.getWidget(i)) instanceof WTreeViewNode ? (WTreeViewNode) (c
					.getWidget(i)) : null);
			if (n != null && n.getModelIndex().getRow() >= start) {
				this.view_.removeRenderedNode(n);
				n.index_ = this.view_.getModel().getIndex(
						n.getModelIndex().getRow() + offset,
						n.getModelIndex().getColumn(), this.index_);
				int lastColumn = this.view_.getColumnCount() - 1;
				int thisNodeCount = this.view_.getModel().getColumnCount(
						this.index_);
				for (int j = 0; j <= lastColumn; ++j) {
					WModelIndex child = j < thisNodeCount ? n.childIndex(j)
							: null;
					this.view_.getItemDelegate(j).updateModelIndex(
							n.getCellWidget(j), child);
				}
				this.view_.addRenderedNode(n);
			}
		}
	}

	public WTreeViewNode getParentNode() {
		return this.parentNode_;
	}

	public boolean isExpanded() {
		return (this.index_ == this.view_.getRootIndex() || (this.index_ != null && this.index_
				.equals(this.view_.getRootIndex())))
				|| !this.getChildContainer().isHidden();
	}

	public void adjustChildrenHeight(int diff) {
		this.childrenHeight_ += diff;
		if (this.isExpanded()) {
			WTreeViewNode parent = this.getParentNode();
			if (parent != null) {
				parent.adjustChildrenHeight(diff);
			} else {
				this.view_.pageChanged().trigger();
			}
		}
	}

	public void normalizeSpacers() {
		if (this.childrenLoaded_ && this.getChildContainer().getCount() == 2) {
			RowSpacer top = this.topSpacer();
			RowSpacer bottom = this.bottomSpacer();
			if (top != null && bottom != null && top != bottom) {
				top.setRows(top.getRows() + bottom.getRows());
				if (bottom != null)
					bottom.remove();
			}
		}
	}

	public void selfCheck() {
		assert this.getRenderedHeight() == this.view_
				.subTreeHeight(this.index_);
		int childNodesHeight = 0;
		for (WTreeViewNode c = this.nextChildNode((WTreeViewNode) null); c != null; c = this
				.nextChildNode(c)) {
			c.selfCheck();
			childNodesHeight += c.getRenderedHeight();
		}
		if (childNodesHeight == 0) {
			assert this.topSpacer() == this.bottomSpacer();
			assert this.getTopSpacerHeight() == this.getChildrenHeight();
		} else {
			assert this.getTopSpacerHeight() + childNodesHeight
					+ this.getBottomSpacerHeight() == this.getChildrenHeight();
		}
	}

	public WTreeView getView() {
		return this.view_;
	}

	public int renderedRow(int lowerBound, int upperBound) {
		if (!(this.parentNode_ != null)) {
			return 0;
		} else {
			int result = this.parentNode_.renderedRow(0, upperBound);
			if (result > upperBound) {
				return result;
			}
			return result
					+ this.parentNode_.renderedRow(this, lowerBound - result,
							upperBound - result);
		}
	}

	public final int renderedRow() {
		return renderedRow(0, Integer.MAX_VALUE);
	}

	public final int renderedRow(int lowerBound) {
		return renderedRow(lowerBound, Integer.MAX_VALUE);
	}

	public int renderedRow(WTreeViewNode node, int lowerBound, int upperBound) {
		if (this.getRenderedHeight() < lowerBound) {
			return this.getRenderedHeight();
		}
		int result = this.getTopSpacerHeight();
		if (result > upperBound) {
			return result;
		}
		for (WTreeViewNode c = this.nextChildNode((WTreeViewNode) null); c != null; c = this
				.nextChildNode(c)) {
			if (c == node) {
				return result;
			} else {
				result += c.getRenderedHeight();
				if (result > upperBound) {
					return result;
				}
			}
		}
		assert false;
		return 0;
	}

	public final int renderedRow(WTreeViewNode node) {
		return renderedRow(node, 0, Integer.MAX_VALUE);
	}

	public final int renderedRow(WTreeViewNode node, int lowerBound) {
		return renderedRow(node, lowerBound, Integer.MAX_VALUE);
	}

	public void renderSelected(boolean selected, int column) {
		String cl = WApplication.getInstance().getTheme().getActiveClass();
		if (this.view_.getSelectionBehavior() == SelectionBehavior.SelectRows) {
			this.bindString("selected", selected ? cl : "");
		} else {
			WWidget w = this.getCellWidget(column);
			w.toggleStyleClass(cl, selected);
		}
	}

	public void doExpand() {
		if (this.isExpanded()) {
			return;
		}
		this.loadChildren();
		ToggleButton expandButton = (ToggleButton) this.resolveWidget("expand");
		if (expandButton != null) {
			expandButton.setState(1);
		}
		this.view_.expandedSet_.add(this.index_);
		this.getChildContainer().show();
		if (this.getParentNode() != null) {
			this.getParentNode().adjustChildrenHeight(this.childrenHeight_);
		}
		this.view_.adjustRenderedNode(this, this.renderedRow());
		this.view_
				.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
		this.view_.expanded_.trigger(this.index_);
	}

	public void doCollapse() {
		if (!this.isExpanded()) {
			return;
		}
		ToggleButton expandButton = (ToggleButton) this.resolveWidget("expand");
		if (expandButton != null) {
			expandButton.setState(0);
		}
		this.view_.setCollapsed(this.index_);
		this.getChildContainer().hide();
		if (this.getParentNode() != null) {
			this.getParentNode().adjustChildrenHeight(-this.childrenHeight_);
		}
		this.view_.renderedRowsChanged(this.renderedRow(),
				-this.childrenHeight_);
		this.view_.collapsed_.trigger(this.index_);
	}

	public WWidget getCellWidget(int column) {
		if (column == 0) {
			return this.resolveWidget("col0");
		} else {
			WContainerWidget row = (WContainerWidget) this
					.resolveWidget("cols-row");
			if (this.view_.getRowHeaderCount() != 0) {
				row = ((row.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (row
						.getWidget(0)) : null);
			}
			return row.getCount() >= column ? row.getWidget(column - 1) : null;
		}
	}

	private WTreeView view_;
	private WModelIndex index_;
	private int childrenHeight_;
	private WTreeViewNode parentNode_;
	private boolean childrenLoaded_;

	private void loadChildren() {
		if (!this.childrenLoaded_) {
			this.childrenLoaded_ = true;
			this.view_.expandedSet_.add(this.index_);
			this.childrenHeight_ = this.view_.subTreeHeight(this.index_) - 1;
			this.view_.expandedSet_.remove(this.index_);
			if (this.childrenHeight_ > 0) {
				this.setTopSpacerHeight(this.childrenHeight_);
			}
		}
	}

	private WModelIndex childIndex(int column) {
		return this.view_.getModel().getIndex(this.index_.getRow(), column,
				this.index_.getParent());
	}

	private void setCellWidget(int column, WWidget newW) {
		WWidget current = this.getCellWidget(column);
		this.addColumnStyleClass(column, newW);
		if (current != null) {
			current.setStyleClass(WString.Empty.toString());
		}
		if (column == 0) {
			newW.setInline(false);
			this.bindWidget("col0", newW);
		} else {
			WContainerWidget row = (WContainerWidget) this
					.resolveWidget("cols-row");
			if (this.view_.getRowHeaderCount() != 0) {
				row = ((row.getWidget(0)) instanceof WContainerWidget ? (WContainerWidget) (row
						.getWidget(0)) : null);
			}
			if (current != null)
				current.remove();
			row.insertWidget(column - 1, newW);
		}
		if (!WApplication.getInstance().getEnvironment().hasAjax()) {
			WInteractWidget wi = ((newW) instanceof WInteractWidget ? (WInteractWidget) (newW)
					: null);
			if (wi != null) {
				this.view_.clickedMapper_.mapConnect1(wi.clicked(),
						this.childIndex(column));
			}
		}
	}

	private void addColumnStyleClass(int column, WWidget w) {
		StringBuilder s = new StringBuilder();
		s.append(this.view_.getColumnStyleClass(column)).append(" Wt-tv-c rh ")
				.append(w.getStyleClass());
		w.setStyleClass(new WString(s.toString()).toString());
	}
}
