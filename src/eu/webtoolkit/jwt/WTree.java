package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A widget that represents a navigatable tree
 * 
 * 
 * WTree provides a tree widget, and coordinates selection functionality.
 * <p>
 * Unlike the MVC-based {@link WTreeView}, the tree renders a widget hierarchy,
 * rather than a hierarhical standard model. This provides extra flexibility (as
 * any widget can be used as contents), at the cost of server-side, client-side
 * and bandwidth resources (especially for large tree tables).
 * <p>
 * The tree is implemented as a hierarchy of {@link WTreeNode} widgets.
 * <p>
 * Selection is rendered by calling
 * {@link WTreeNode#renderSelected(boolean isSelected)}. Only tree nodes that
 * are {@link WTreeNode#setSelectable(boolean selectable) selectable} may
 * participate in the selection.
 * <p>
 * 
 * @see WTreeNode
 * @see WTreeTable
 * @see WTreeView
 */
public class WTree extends WCompositeWidget {
	/**
	 * Create a new tree.
	 */
	public WTree(WContainerWidget parent) {
		super(parent);
		this.treeRoot_ = null;
		this.selectionMode_ = SelectionMode.NoSelection;
		this.selection_ = new HashSet<WTreeNode>();
		this.onClickMapper_ = new WSignalMapper2<WTreeNode, WMouseEvent>();
		this.itemSelectionChanged_ = new Signal(this);
		this.setImplementation(this.sentinelRoot_ = new SentinelTreeNode(this));
		this.onClickMapper_.mapped().addListener(this,
				new Signal2.Listener<WTreeNode, WMouseEvent>() {
					public void trigger(WTreeNode e1, WMouseEvent e2) {
						WTree.this.onClick(e1, e2);
					}
				});
	}

	public WTree() {
		this((WContainerWidget) null);
	}

	/**
	 * Set the tree root node.
	 * 
	 * @see WTree#getTreeRoot()
	 */
	public void setTreeRoot(WTreeNode node) {
		if (this.treeRoot_ != null) {
			this.sentinelRoot_.removeChildNode(this.treeRoot_);
			if (this.treeRoot_ != null)
				this.treeRoot_.remove();
		}
		this.treeRoot_ = node;
		this.sentinelRoot_.addChildNode(node);
	}

	/**
	 * Get the root node.
	 * 
	 * @see WTree#setTreeRoot(WTreeNode node)
	 */
	public WTreeNode getTreeRoot() {
		return this.treeRoot_;
	}

	/**
	 * Set the selection mode.
	 * 
	 * The default selection mode is {@link SelectionMode#NoSelection}.
	 */
	public void setSelectionMode(SelectionMode mode) {
		if (mode != this.selectionMode_) {
			this.selectionMode_ = mode;
			this.clearSelection();
			if (this.selectionMode_ != SelectionMode.NoSelection) {
				this.setAttributeValue("onselectstart", "return false;");
				this.setStyleClass("unselectable");
			}
		}
	}

	/**
	 * Get the selection mode.
	 */
	public SelectionMode getSelectionMode() {
		return this.selectionMode_;
	}

	/**
	 * Typedef for a set of WTreeNode&apos;s.
	 */
	public Set<WTreeNode> WTreeNodeSet;

	/**
	 * Get the set of selected tree nodes.
	 */
	public Set<WTreeNode> getSelectedNodes() {
		return this.selection_;
	}

	/**
	 * Set a selection of tree nodes.
	 */
	public void select(Set<WTreeNode> nodes) {
		this.clearSelection();
		for (Iterator<WTreeNode> i_it = nodes.iterator(); i_it.hasNext();) {
			WTreeNode i = i_it.next();
			this.select(i);
		}
		this.itemSelectionChanged_.trigger();
	}

	/**
	 * Select or unselect the given <i>node</i>.
	 */
	public void select(WTreeNode node, boolean selected) {
		if (this.selectionMode_ == SelectionMode.SingleSelection && selected) {
			this.clearSelection();
		}
		if (!selected || this.selectionMode_ != SelectionMode.NoSelection) {
			if (selected) {
				if (node.isSelectable()) {
					this.selection_.add(node);
					node.renderSelected(selected);
				}
			} else {
				if (this.selection_.remove(node)) {
					node.renderSelected(false);
				}
			}
		}
		this.itemSelectionChanged_.trigger();
	}

	public final void select(WTreeNode node) {
		select(node, true);
	}

	/**
	 * Return if the given <i>node</i> is currently selected.
	 */
	public boolean isSelected(WTreeNode node) {
		boolean i = this.selection_.contains(node);
		return i != false;
	}

	/**
	 * Clear the current selection.
	 */
	public void clearSelection() {
		while (!this.selection_.isEmpty()) {
			WTreeNode n = this.selection_.iterator().next();
			this.select(n, false);
		}
	}

	/**
	 * Signal that is emitted when the selection changes.
	 */
	public Signal itemSelectionChanged() {
		return this.itemSelectionChanged_;
	}

	private WTreeNode treeRoot_;
	private SentinelTreeNode sentinelRoot_;
	private SelectionMode selectionMode_;
	private Set<WTreeNode> selection_;
	private WSignalMapper2<WTreeNode, WMouseEvent> onClickMapper_;
	private Signal itemSelectionChanged_;

	private void onClick(WTreeNode node, WMouseEvent event) {
		if (this.selectionMode_ == SelectionMode.NoSelection) {
			return;
		}
		if (this.selectionMode_ == SelectionMode.ExtendedSelection) {
			if (!EnumUtils.mask(event.getModifiers(),
					KeyboardModifier.ShiftModifier).isEmpty()) {
				this.extendSelection(node);
			} else {
				if (!!EnumUtils.mask(
						event.getModifiers(),
						EnumSet.of(KeyboardModifier.ControlModifier,
								KeyboardModifier.MetaModifier)).isEmpty()) {
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
		for (;;) {
			this.select(n);
			if (n == to) {
				break;
			} else {
				if (n.isExpanded() && !n.getChildNodes().isEmpty()) {
					n = n.getChildNodes().get(0);
				} else {
					for (;;) {
						List<WTreeNode> cs = n.getParentNode().getChildNodes();
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
			for (Iterator<WTreeNode> i_it = this.selection_.iterator(); i_it
					.hasNext();) {
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

	protected void nodeRemoved(WTreeNode node) {
		this.select(node, false);
		for (int i = 0; i < node.getChildNodes().size(); ++i) {
			this.nodeRemoved(node.getChildNodes().get(i));
		}
	}

	protected void nodeAdded(WTreeNode node) {
		this.select(node, false);
		if (node.isSelectable()) {
			WInteractWidget w = node.getLabel();
			if (!(w != null)) {
				w = node.getLabelArea();
			}
			this.onClickMapper_.mapConnect1(w.clicked(), node);
			for (int i = 0; i < node.getChildNodes().size(); ++i) {
				this.nodeAdded(node.getChildNodes().get(i));
			}
		}
		if (!node.getParentNode().isSelectable()
				&& this.isSelected(node.getParentNode())) {
			this.select(node.getParentNode(), false);
		}
	}

	static boolean getAncestors(WTreeNode n, List<WTreeNode> ancestors) {
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
