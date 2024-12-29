/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
 * A widget that represents a navigatable tree.
 *
 * <p>WTree provides a tree widget, and coordinates selection functionality.
 *
 * <p>Unlike the MVC-based {@link WTreeView}, the tree renders a widget hierarchy, rather than a
 * hierarhical standard model. This provides extra flexibility (as any widget can be used as
 * contents), at the cost of server-side, client-side and bandwidth resources (especially for large
 * tree tables).
 *
 * <p>The tree is implemented as a hierarchy of {@link WTreeNode} widgets.
 *
 * <p>Selection is rendered by calling {@link WTreeNode#renderSelected(boolean isSelected)
 * renderSelected()}. Only tree nodes that are {@link WTreeNode#setSelectable(boolean selectable)
 * selectable} may participate in the selection.
 *
 * <p>A screenshot of the tree:
 *
 * <table border="1" cellspacing="3" cellpadding="3">
 * <tr><td><div align="center">
 * <img src="doc-files/WTree-default-1.png">
 * <p>
 * <strong>An example WTree (default)</strong></p>
 * </div>
 *
 *
 * </td><td><div align="center">
 * <img src="doc-files/WTree-polished-1.png">
 * <p>
 * <strong>An example WTree (polished)</strong></p>
 * </div>
 *
 *
 * </td></tr>
 * </table>
 *
 * <p>
 *
 * @see WTreeNode
 * @see WTreeTable
 * @see WTreeView
 */
public class WTree extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WTree.class);

  /** Creates a new tree. */
  public WTree(WContainerWidget parentContainer) {
    super();
    this.treeRoot_ = null;
    this.selectionMode_ = SelectionMode.None;
    this.selection_ = new HashSet<WTreeNode>();
    this.itemSelectionChanged_ = new Signal();
    this.setImplementation(this.sentinelRoot_ = new SentinelTreeNode(this));
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new tree.
   *
   * <p>Calls {@link #WTree(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WTree() {
    this((WContainerWidget) null);
  }
  /**
   * Sets the tree root node.
   *
   * <p>The initial value is <code>null</code>.
   */
  public void setTreeRoot(WTreeNode node) {
    if (this.treeRoot_ != null) {
      {
        WTreeNode toRemove = this.sentinelRoot_.removeChildNode(this.treeRoot_);
        if (toRemove != null) toRemove.remove();
      }
    }
    this.treeRoot_ = node;
    this.sentinelRoot_.addChildNode(node);
  }
  /**
   * Returns the root node.
   *
   * <p>
   *
   * @see WTree#setTreeRoot(WTreeNode node)
   */
  public WTreeNode getTreeRoot() {
    return this.treeRoot_;
  }
  /**
   * Sets the selection mode.
   *
   * <p>The default selection mode is {@link SelectionMode#None}.
   */
  public void setSelectionMode(SelectionMode mode) {
    if (mode != this.selectionMode_) {
      this.selectionMode_ = mode;
      this.clearSelection();
    }
  }
  /** Returns the selection mode. */
  public SelectionMode getSelectionMode() {
    return this.selectionMode_;
  }
  /** Returns the set of selected tree nodes. */
  public Set<WTreeNode> getSelectedNodes() {
    return this.selection_;
  }
  /** Sets a selection of tree nodes. */
  public void select(final Set<WTreeNode> nodes) {
    this.clearSelection();
    for (Iterator<WTreeNode> i_it = nodes.iterator(); i_it.hasNext(); ) {
      WTreeNode i = i_it.next();
      this.select(i);
    }
    this.itemSelectionChanged_.trigger();
  }
  /** Selects or unselect the given <i>node</i>. */
  public void select(WTreeNode node, boolean selected) {
    if (this.selectionMode_ == SelectionMode.Single
        && selected
        && this.selection_.size() == 1
        && this.selection_.iterator().next() == node) {
      return;
    }
    if (this.selectionMode_ == SelectionMode.Single && selected) {
      this.clearSelection();
    }
    if (!selected || this.selectionMode_ != SelectionMode.None) {
      if (selected) {
        if (node.isSelectable()) {
          this.selection_.add(node);
          node.renderSelected(selected);
        }
      } else {
        if (this.selection_.remove(node)) {
          node.renderSelected(false);
        } else {
          return;
        }
      }
    }
    this.itemSelectionChanged_.trigger();
  }
  /**
   * Selects or unselect the given <i>node</i>.
   *
   * <p>Calls {@link #select(WTreeNode node, boolean selected) select(node, true)}
   */
  public final void select(WTreeNode node) {
    select(node, true);
  }
  /** Returns if the given <i>node</i> is currently selected. */
  public boolean isSelected(WTreeNode node) {
    boolean i = this.selection_.contains(node);
    return i != false;
  }
  /** Clears the current selection. */
  public void clearSelection() {
    while (!this.selection_.isEmpty()) {
      WTreeNode n = this.selection_.iterator().next();
      this.select(n, false);
    }
  }
  /** Signal that is emitted when the selection changes. */
  public Signal itemSelectionChanged() {
    return this.itemSelectionChanged_;
  }

  private WTreeNode treeRoot_;
  private SentinelTreeNode sentinelRoot_;
  private SelectionMode selectionMode_;
  private Set<WTreeNode> selection_;
  private Signal itemSelectionChanged_;

  private void onClick(WTreeNode node, WMouseEvent event) {
    if (this.selectionMode_ == SelectionMode.None) {
      return;
    }
    if (this.selectionMode_ == SelectionMode.Extended) {
      if (event.getModifiers().contains(KeyboardModifier.Shift)) {
        this.extendSelection(node);
      } else {
        if (!!EnumUtils.mask(
                event.getModifiers(), EnumSet.of(KeyboardModifier.Control, KeyboardModifier.Meta))
            .isEmpty()) {
          if (this.isSelected(node)) {
            return;
          } else {
            this.clearSelection();
            this.select(node);
          }
        } else {
          this.select(node, !this.isSelected(node));
        }
      }
    } else {
      this.select(node);
    }
  }

  private void selectRange(WTreeNode from, WTreeNode to) {
    this.clearSelection();
    WTreeNode n = from;
    for (; ; ) {
      this.select(n);
      if (n == to) {
        break;
      } else {
        if (n.isExpanded() && !n.getChildNodes().isEmpty()) {
          n = n.getChildNodes().get(0);
        } else {
          for (; ; ) {
            final List<WTreeNode> cs = n.getParentNode().getChildNodes();
            int i = cs.indexOf(n);
            i++;
            if (i < (int) cs.size()) {
              n = cs.get(i);
              break;
            } else {
              n = n.getParentNode();
            }
          }
        }
      }
    }
  }

  private void extendSelection(WTreeNode node) {
    if (this.selection_.isEmpty()) {
      this.select(node);
    } else {
      WTreeNode top = null;
      WTreeNode bottom = null;
      for (Iterator<WTreeNode> i_it = this.selection_.iterator(); i_it.hasNext(); ) {
        WTreeNode i = i_it.next();
        WTreeNode s = i;
        WTreeNode f1 = firstNode(top, s);
        if (!(f1 != null)) {
          continue;
        }
        top = f1;
        if (!(bottom != null)) {
          bottom = s;
        } else {
          WTreeNode f2 = firstNode(bottom, s);
          if (f2 == bottom) {
            bottom = s;
          }
        }
      }
      if (!(top != null)) {
        this.clearSelection();
        this.select(node);
        return;
      }
      WTreeNode f1 = firstNode(node, top);
      if (f1 == top) {
        this.selectRange(top, node);
      } else {
        this.selectRange(node, bottom);
      }
    }
  }

  void nodeRemoved(WTreeNode node) {
    this.select(node, false);
    node.clickedConnection_.disconnect();
    for (int i = 0; i < node.getChildNodes().size(); ++i) {
      this.nodeRemoved(node.getChildNodes().get(i));
    }
  }

  void nodeAdded(final WTreeNode node) {
    if (node.isSelectable()) {
      WInteractWidget w;
      if (WApplication.getInstance().getEnvironment().hasAjax()) {
        w = node.getImpl();
      } else {
        w = node.getLabel();
      }
      node.clickedConnection_ =
          w.clicked()
              .addListener(
                  this,
                  (WMouseEvent event) -> {
                    WTree.this.onClick(node, event);
                  });
      w.clicked().preventPropagation();
      for (int i = 0; i < node.getChildNodes().size(); ++i) {
        this.nodeAdded(node.getChildNodes().get(i));
      }
    }
    if (!node.getParentNode().isSelectable() && this.isSelected(node.getParentNode())) {
      this.select(node.getParentNode(), false);
    }
  }

  static boolean getAncestors(WTreeNode n, final List<WTreeNode> ancestors) {
    WTreeNode p = n.getParentNode();
    if (p != null) {
      if (!p.isExpanded()) {
        return false;
      } else {
        if (!getAncestors(p, ancestors)) {
          return false;
        }
      }
    }
    ancestors.add(n);
    return true;
  }

  static WTreeNode firstNode(WTreeNode n1, WTreeNode n2) {
    List<WTreeNode> ancestors1 = new ArrayList<WTreeNode>();
    List<WTreeNode> ancestors2 = new ArrayList<WTreeNode>();
    boolean visible1 = n1 != null ? getAncestors(n1, ancestors1) : true;
    boolean visible2 = n2 != null ? getAncestors(n2, ancestors2) : true;
    if (!visible1 || !visible2) {
      return null;
    }
    if (!(n1 != null)) {
      return n2;
    }
    if (!(n2 != null)) {
      return n1;
    }
    for (int i = 1; i < Math.min(ancestors1.size(), ancestors2.size()); ++i) {
      if (ancestors1.get(i) != ancestors2.get(i)) {
        WTreeNode parent = ancestors1.get(i - 1);
        int i1 = parent.getChildNodes().indexOf(ancestors1.get(i));
        int i2 = parent.getChildNodes().indexOf(ancestors2.get(i));
        if (i1 < i2) {
          return n1;
        } else {
          return n2;
        }
      }
    }
    if (ancestors1.size() > ancestors2.size()) {
      return n2;
    } else {
      return n1;
    }
  }
}
