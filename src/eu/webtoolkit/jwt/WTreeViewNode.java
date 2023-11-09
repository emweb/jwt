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

class WTreeViewNode extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(WTreeViewNode.class);

  public WTreeViewNode(
      WTreeView view,
      final WModelIndex index,
      int childrenHeight,
      boolean isLast,
      WTreeViewNode parent) {
    super();
    this.view_ = view;
    this.nodeWidget_ = null;
    this.childContainer_ = null;
    this.index_ = index;
    this.childrenHeight_ = childrenHeight;
    this.parentNode_ = parent;
    this.childrenLoaded_ = false;
    this.nodeWidget_ = new WTemplate(tr("Wt.WTreeViewNode.template"));
    this.addWidget(this.nodeWidget_);
    this.nodeWidget_.setStyleClass("Wt-item");
    this.nodeWidget_.bindEmpty("cols-row");
    this.nodeWidget_.bindEmpty("expand");
    this.nodeWidget_.bindEmpty("no-expand");
    this.nodeWidget_.bindEmpty("col0");
    final int selfHeight =
        (this.index_ == this.view_.getRootIndex()
                || (this.index_ != null && this.index_.equals(this.view_.getRootIndex())))
            ? 0
            : 1;
    boolean needLoad = this.view_.isExpanded(this.index_);
    if (!(this.index_ == this.view_.getRootIndex()
            || (this.index_ != null && this.index_.equals(this.view_.getRootIndex())))
        && !needLoad) {
      this.getChildContainer().hide();
    }
    if (needLoad) {
      this.childrenLoaded_ = true;
      if (this.childrenHeight_ == -1) {
        this.childrenHeight_ = this.view_.subTreeHeight(this.index_) - selfHeight;
      }
      if (this.childrenHeight_ > 0) {
        this.setTopSpacerHeight(this.childrenHeight_);
      }
    } else {
      this.childrenHeight_ = 0;
    }
    if (!(this.index_ == this.view_.getRootIndex()
        || (this.index_ != null && this.index_.equals(this.view_.getRootIndex())))) {
      this.updateGraphics(isLast, !this.view_.getModel().hasChildren(this.index_));
      this.insertColumns(0, this.view_.getColumnCount());
      if (this.view_.getSelectionBehavior() == SelectionBehavior.Rows
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
      EnumSet<ViewItemRenderFlag> renderFlags = EnumSet.noneOf(ViewItemRenderFlag.class);
      if (this.view_.getSelectionBehavior() == SelectionBehavior.Items
          && this.view_.isSelected(child)) {
        renderFlags.add(ViewItemRenderFlag.Selected);
      }
      if (this.view_.isEditing(child)) {
        renderFlags.add(ViewItemRenderFlag.Editing);
        if (this.view_.hasEditFocus(child)) {
          renderFlags.add(ViewItemRenderFlag.Focused);
        }
      }
      if (!this.view_.isValid(child)) {
        renderFlags.add(ViewItemRenderFlag.Invalid);
      }
      WWidget wAfter = this.view_.getItemDelegate(i).update(w, child, renderFlags);
      if (wAfter != null) {
        w = wAfter;
      }
      if (renderFlags.contains(ViewItemRenderFlag.Editing)) {
        this.view_.setEditorWidget(child, w);
      }
      if (wAfter != null) {
        this.setCellWidget(i, wAfter);
        if (renderFlags.contains(ViewItemRenderFlag.Editing)) {
          Object state = this.view_.getEditState(child);
          if ((state != null)) {
            this.view_.getItemDelegate(i).setEditState(w, child, state);
          }
        }
      } else {
        this.addColumnStyleClass(i, w);
      }
    }
  }

  public void updateGraphics(boolean isLast, boolean isEmpty) {
    if ((this.index_ == this.view_.getRootIndex()
        || (this.index_ != null && this.index_.equals(this.view_.getRootIndex())))) {
      return;
    }
    if ((this.index_.getParent() == this.view_.getRootIndex()
            || (this.index_.getParent() != null
                && this.index_.getParent().equals(this.view_.getRootIndex())))
        && !this.view_.isRootDecorated()) {
      this.nodeWidget_.bindEmpty("expand");
      this.nodeWidget_.bindEmpty("no-expand");
      return;
    }
    if (!isEmpty) {
      ToggleButton expandButton = (ToggleButton) this.nodeWidget_.resolveWidget("expand");
      if (!(expandButton != null)) {
        this.nodeWidget_.bindEmpty("no-expand");
        expandButton = new ToggleButton(this.view_.expandConfig_);
        this.nodeWidget_.bindWidget("expand", expandButton);
        if (WApplication.getInstance().getEnvironment().agentIsIE()) {
          expandButton.setWidth(new WLength(19));
        }
        expandButton
            .signal(0)
            .addListener(
                this,
                () -> {
                  WTreeViewNode.this.doExpand();
                });
        expandButton
            .signal(1)
            .addListener(
                this,
                () -> {
                  WTreeViewNode.this.doCollapse();
                });
        expandButton.setState(this.isExpanded() ? 1 : 0);
      }
    } else {
      WText noExpandIcon = (WText) this.nodeWidget_.resolveWidget("no-expand");
      if (!(noExpandIcon != null)) {
        this.nodeWidget_.bindEmpty("expand");
        noExpandIcon = new WText();
        this.nodeWidget_.bindWidget("no-expand", noExpandIcon);
        noExpandIcon.setInline(false);
        noExpandIcon.setStyleClass("Wt-ctrl rh noexpand");
        if (WApplication.getInstance().getEnvironment().agentIsIE()) {
          noExpandIcon.setWidth(new WLength(19));
        }
      }
    }
    this.toggleStyleClass("Wt-trunk", !isLast);
    this.nodeWidget_.toggleStyleClass("Wt-end", isLast);
    this.nodeWidget_.toggleStyleClass("Wt-trunk", !isLast);
  }

  public void insertColumns(int column, int count) {
    WContainerWidget row = (WContainerWidget) this.nodeWidget_.resolveWidget("cols-row");
    if (this.view_.getColumnCount() > 1) {
      if (!(row != null)) {
        WContainerWidget newRow = new WContainerWidget();
        if (this.view_.getRowHeaderCount() != 0) {
          newRow.setStyleClass("Wt-tv-rowc rh");
          WContainerWidget rowWrap = new WContainerWidget();
          rowWrap.addWidget(newRow);
          newRow = rowWrap;
        }
        newRow.setStyleClass("Wt-tv-row rh");
        this.nodeWidget_.bindWidget("cols-row", newRow);
      }
    } else {
      if (row != null) {
        {
          WWidget toRemove = row.removeFromParent();
          if (toRemove != null) toRemove.remove();
        }
      }
    }
    this.update(0, this.view_.getColumnCount() - 1);
  }

  public void removeColumns(int column, int count) {
    this.insertColumns(0, 0);
  }

  public boolean isLast() {
    return (this.index_ == this.view_.getRootIndex()
            || (this.index_ != null && this.index_.equals(this.view_.getRootIndex())))
        || this.index_.getRow() == this.view_.getModel().getRowCount(this.index_.getParent()) - 1;
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
    return (this.index_ == this.view_.getRootIndex()
            || (this.index_ != null && this.index_.equals(this.view_.getRootIndex())))
        ? this.childrenHeight_
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
      WTreeViewNode n = ObjectUtils.cast(c.getWidget(first), WTreeViewNode.class);
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
    int nextI = prev != null ? c.getIndexOf(prev) + 1 : this.topSpacer() != null ? 1 : 0;
    if (nextI < c.getCount()) {
      return ObjectUtils.cast(c.getWidget(nextI), WTreeViewNode.class);
    } else {
      return null;
    }
  }

  public boolean isAllSpacer() {
    return this.childrenLoaded_
        && this.topSpacer() != null
        && this.topSpacer() == this.bottomSpacer();
  }

  public void setTopSpacerHeight(int rows) {
    if (rows == 0) {
      {
        WWidget toRemove = WidgetUtils.remove(this.getChildContainer(), this.topSpacer());
        if (toRemove != null) toRemove.remove();
      }

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
      RowSpacer bottom = this.bottomSpacer();
      if (bottom != null) {
        {
          WWidget toRemove = bottom.removeFromParent();
          if (toRemove != null) toRemove.remove();
        }
      }
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
        || !((result = ObjectUtils.cast(c.getWidget(0), RowSpacer.class)) != null)) {
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
        || !((result = ObjectUtils.cast(c.getWidget(c.getCount() - 1), RowSpacer.class)) != null)) {
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
    if (!(this.childContainer_ != null)) {
      this.childContainer_ = new WContainerWidget();
      this.addWidget(this.childContainer_);
      this.childContainer_.setList(true);
      if ((this.index_ == this.view_.getRootIndex()
          || (this.index_ != null && this.index_.equals(this.view_.getRootIndex())))) {
        this.childContainer_.addStyleClass("Wt-tv-root");
      }
    }
    return this.childContainer_;
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
      WTreeViewNode n = ObjectUtils.cast(c.getWidget(i), WTreeViewNode.class);
      if (n != null && n.getModelIndex().getRow() >= start) {
        this.view_.removeRenderedNode(n);
        n.index_ =
            this.view_
                .getModel()
                .getIndex(
                    n.getModelIndex().getRow() + offset,
                    n.getModelIndex().getColumn(),
                    this.index_);
        int lastColumn = this.view_.getColumnCount() - 1;
        int thisNodeCount = this.view_.getModel().getColumnCount(this.index_);
        for (int j = 0; j <= lastColumn; ++j) {
          WModelIndex child = j < thisNodeCount ? n.childIndex(j) : null;
          this.view_.getItemDelegate(j).updateModelIndex(n.getCellWidget(j), child);
        }
        this.view_.addRenderedNode(n);
      }
    }
  }

  public WTreeViewNode getParentNode() {
    return this.parentNode_;
  }

  public boolean isExpanded() {
    return (this.index_ == this.view_.getRootIndex()
            || (this.index_ != null && this.index_.equals(this.view_.getRootIndex())))
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
        if (bottom != null) {
          {
            WWidget toRemove = bottom.removeFromParent();
            if (toRemove != null) toRemove.remove();
          }
        }
      }
    }
  }

  public void selfCheck() {
    assert this.getRenderedHeight() == this.view_.subTreeHeight(this.index_);
    int childNodesHeight = 0;
    for (WTreeViewNode c = this.nextChildNode((WTreeViewNode) null);
        c != null;
        c = this.nextChildNode(c)) {
      c.selfCheck();
      childNodesHeight += c.getRenderedHeight();
    }
    if (childNodesHeight == 0) {
      assert this.topSpacer() == this.bottomSpacer();
      assert this.getTopSpacerHeight() == this.getChildrenHeight();
    } else {
      assert this.getTopSpacerHeight() + childNodesHeight + this.getBottomSpacerHeight()
          == this.getChildrenHeight();
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
      return result + this.parentNode_.renderedRow(this, lowerBound - result, upperBound - result);
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
    for (WTreeViewNode c = this.nextChildNode((WTreeViewNode) null);
        c != null;
        c = this.nextChildNode(c)) {
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
    if (this.view_.getSelectionBehavior() == SelectionBehavior.Rows) {
      this.nodeWidget_.toggleStyleClass(cl, selected);
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
    ToggleButton expandButton = (ToggleButton) this.nodeWidget_.resolveWidget("expand");
    if (expandButton != null) {
      expandButton.setState(1);
    }
    this.view_.expandedSet_.add(this.index_);
    this.getChildContainer().show();
    if (this.getParentNode() != null) {
      this.getParentNode().adjustChildrenHeight(this.childrenHeight_);
    }
    this.view_.adjustRenderedNode(this, this.renderedRow());
    this.view_.scheduleRerender(WAbstractItemView.RenderState.NeedAdjustViewPort);
    this.view_.expanded_.trigger(this.index_);
  }

  public void doCollapse() {
    if (!this.isExpanded()) {
      return;
    }
    ToggleButton expandButton = (ToggleButton) this.nodeWidget_.resolveWidget("expand");
    if (expandButton != null) {
      expandButton.setState(0);
    }
    this.view_.setCollapsed(this.index_);
    this.getChildContainer().hide();
    if (this.getParentNode() != null) {
      this.getParentNode().adjustChildrenHeight(-this.childrenHeight_);
    }
    this.view_.renderedRowsChanged(this.renderedRow(), -this.childrenHeight_);
    this.view_.collapsed_.trigger(this.index_);
  }

  public WWidget getCellWidget(int column) {
    if (column == 0) {
      return this.nodeWidget_.resolveWidget("col0");
    } else {
      WContainerWidget row = (WContainerWidget) this.nodeWidget_.resolveWidget("cols-row");
      if (this.view_.getRowHeaderCount() != 0) {
        row = ObjectUtils.cast(row.getWidget(0), WContainerWidget.class);
      }
      return row.getCount() >= column ? row.getWidget(column - 1) : null;
    }
  }

  void updateDom(final DomElement element, boolean all) {
    if (this.view_.isDisabled()) {
      this.addStyleClass("Wt-disabled");
      this.nodeWidget_.addStyleClass("Wt-disabled");
      WWidget widget = this.nodeWidget_.resolveWidget("cols-row");
      if (widget != null) {
        widget.addStyleClass("Wt-disabled");
      }
      widget = this.nodeWidget_.resolveWidget("expand");
      if (widget != null) {
        widget.addStyleClass("Wt-disabled");
      }
      widget = this.nodeWidget_.resolveWidget("no-expand");
      if (widget != null) {
        widget.addStyleClass("Wt-disabled");
      }
      widget = this.nodeWidget_.resolveWidget("col0");
      if (widget != null) {
        widget.addStyleClass("Wt-disabled");
      }
    }
    super.updateDom(element, all);
  }

  private WTreeView view_;
  private WTemplate nodeWidget_;
  private WContainerWidget childContainer_;
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
    return this.view_.getModel().getIndex(this.index_.getRow(), column, this.index_.getParent());
  }

  private void setCellWidget(int column, WWidget newW) {
    WWidget current = this.getCellWidget(column);
    this.addColumnStyleClass(column, newW);
    if (current != null) {
      current.setStyleClass(WString.Empty.toString());
    }
    if (!WApplication.getInstance().getEnvironment().hasAjax()) {
      WInteractWidget wi = ObjectUtils.cast(newW, WInteractWidget.class);
      final WModelIndex ci = this.childIndex(column);
      if (wi != null) {
        wi.clicked()
            .addListener(
                this.view_,
                (WMouseEvent event) -> {
                  WTreeViewNode.this.view_.handleClick(ci, event);
                });
      }
    }
    if (column == 0) {
      newW.setInline(false);
      this.nodeWidget_.bindWidget("col0", newW);
    } else {
      WContainerWidget row = (WContainerWidget) this.nodeWidget_.resolveWidget("cols-row");
      if (this.view_.getRowHeaderCount() != 0) {
        row = ObjectUtils.cast(row.getWidget(0), WContainerWidget.class);
      }
      if (current != null) {
        {
          WWidget toRemove = current.removeFromParent();
          if (toRemove != null) toRemove.remove();
        }
      }
      row.insertWidget(column - 1, newW);
    }
  }

  private void addColumnStyleClass(int column, WWidget w) {
    StringBuilder s = new StringBuilder();
    s.append(this.view_.getColumnStyleClass(column))
        .append(" Wt-tv-c rh ")
        .append(w.getStyleClass());
    w.setStyleClass(new WString(s.toString()).toString());
  }
}
