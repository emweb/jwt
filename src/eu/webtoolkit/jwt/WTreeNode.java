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
 * A single node in a tree.
 * <p>
 * 
 * A tree list is constructed by combining several tree node objects in a tree
 * hierarchy, by passing the parent tree node as the last argument in the child
 * node constructor, or by using {@link }, to add a child to its parent.
 * <p>
 * Each tree node has a label, and optionally a label icon pair. The icon pair
 * offers the capability to show a different icon depending on the state of the
 * node (expanded or collapsed). When the node has any children, a child count
 * may be displayed next to the label using {@link }.
 * <p>
 * Expanding a tree node it will collapse all its children, so that a user may
 * collapse/expand a node as a short-cut to collapsing all children.
 * <p>
 * The treenode provides several policies to communicate the current contents of
 * the tree to the client (if possible):
 * <ul>
 * <li>
 * {@link }: the entire tree is transmitted to the client, and all tree
 * navigation requires no further communication.</li>
 * <li>
 * {@link }: only the minimum is transmitted to the client. When expanding a node
 * for the first time, only then it is transmitted to the client, and this may
 * thus have some latency.</li>
 * <li>
 * {@link }: all leafs of visible children are transmitted, but not their
 * children. This provides a good trade-off between bandwith use and
 * interactivity, since expanding any tree node will happen instantly, and at
 * the same time trigger some communication in the back-ground to load the next
 * level of invisible nodes.</li>
 * </ul>
 * <p>
 * The default policy is {@link }. Another load policy may be specified using
 * {@link } on the root node and before adding any children. The load policy is
 * inherited by all children in the tree.
 * <p>
 * There are a few scenarios where it makes sense to specialize the WTreeNode
 * class. One scenario is create a tree that is populated dynamically while
 * browsing. For this purpose you should reimplement the {@link } method, whose
 * default implementation does nothing. This method is called when
 * &apos;loading&apos; the node. The exact moment for loading a treenode depends
 * on the LoadPolicy.
 * <p>
 * A second scenario that is if you want to customize the look of the tree label
 * (see {@link }) or if you want to modify or augment the event collapse/expand
 * event handling (see {@link } and {@link }).
 * <p>
 * See {@link WTree} for a usage example.
 * <p>
 * 
 * @see WTree
 * @see WTreeTableNode
 */
public class WTreeNode extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WTreeNode.class);

	/**
	 * An enumeration for the policy to load children.
	 */
	public enum LoadPolicy {
		/**
		 * Load-on-demand of child nodes.
		 */
		LazyLoading,
		/**
		 * Pre-load all child nodes.
		 */
		PreLoading,
		/**
		 * Pre-load one level of child nodes.
		 */
		NextLevelLoading;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * An enumeration for the policy to display the child count.
	 */
	public enum ChildCountPolicy {
		/**
		 * Do not display a child count.
		 */
		Disabled,
		/**
		 * Always display a child count.
		 */
		Enabled,
		/**
		 * Only display a child count when the node is populated.
		 */
		Lazy;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a tree node with the given label.
	 * <p>
	 * The labelIcon, if specified, will appear just before the label and its
	 * state reflect the expand/collapse state of the node.
	 * <p>
	 * The node is initialized to be collapsed.
	 */
	public WTreeNode(final CharSequence labelText, WIconPair labelIcon,
			WTreeNode parent) {
		super();
		this.childNodes_ = new ArrayList<WTreeNode>();
		this.collapsed_ = true;
		this.selectable_ = true;
		this.visible_ = true;
		this.childrenDecorated_ = true;
		this.parentNode_ = null;
		this.childCountPolicy_ = WTreeNode.ChildCountPolicy.Disabled;
		this.labelIcon_ = labelIcon;
		this.labelText_ = new WText(labelText);
		this.childrenLoaded_ = false;
		this.populated_ = false;
		this.interactive_ = true;
		this.selected_ = new Signal1<Boolean>(this);
		this.clickedConnection_ = new AbstractSignal.Connection();
		this.create();
		if (parent != null) {
			parent.addChildNode(this);
		}
	}

	/**
	 * Creates a tree node with the given label.
	 * <p>
	 * Calls
	 * {@link #WTreeNode(CharSequence labelText, WIconPair labelIcon, WTreeNode parent)
	 * this(labelText, (WIconPair)null, (WTreeNode)null)}
	 */
	public WTreeNode(final CharSequence labelText) {
		this(labelText, (WIconPair) null, (WTreeNode) null);
	}

	/**
	 * Creates a tree node with the given label.
	 * <p>
	 * Calls
	 * {@link #WTreeNode(CharSequence labelText, WIconPair labelIcon, WTreeNode parent)
	 * this(labelText, labelIcon, (WTreeNode)null)}
	 */
	public WTreeNode(final CharSequence labelText, WIconPair labelIcon) {
		this(labelText, labelIcon, (WTreeNode) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		for (int i = 0; i < this.childNodes_.size(); ++i) {
			if (this.childNodes_.get(i) != null)
				this.childNodes_.get(i).remove();
		}
		if (this.noExpandIcon_ != null)
			this.noExpandIcon_.remove();
		if (this.expandIcon_ != null)
			this.expandIcon_.remove();
		super.remove();
	}

	/**
	 * Returns the tree.
	 * <p>
	 * By default if this node has no parent the result will be 0.
	 */
	public WTree getTree() {
		return this.parentNode_ != null ? this.parentNode_.getTree() : null;
	}

	/**
	 * Returns the label.
	 */
	public WText getLabel() {
		return this.labelText_;
	}

	/**
	 * Returns the label icon.
	 */
	public WIconPair getLabelIcon() {
		return this.labelIcon_;
	}

	/**
	 * Sets the label icon.
	 */
	public void setLabelIcon(WIconPair labelIcon) {
		if (this.labelIcon_ != null)
			this.labelIcon_.remove();
		this.labelIcon_ = labelIcon;
		if (this.labelIcon_ != null) {
			if (this.labelText_ != null) {
				this.getLabelArea().insertBefore(this.labelIcon_,
						this.labelText_);
			} else {
				this.getLabelArea().addWidget(this.labelIcon_);
			}
			this.labelIcon_.setState(this.isExpanded() ? 1 : 0);
		}
	}

	/**
	 * Inserts a child node.
	 * <p>
	 * Inserts the node <code>node</code> at index <code>index</code>.
	 */
	public void insertChildNode(int index, WTreeNode node) {
		this.childNodes_.add(0 + index, node);
		node.parentNode_ = this;
		if (this.childrenLoaded_) {
			this.getChildContainer().insertWidget(index, node);
		} else {
			node.setParent((WObject) null);
		}
		this.descendantAdded(node);
		if (this.loadPolicy_ != node.loadPolicy_) {
			node.setLoadPolicy(this.loadPolicy_);
		}
		if (this.childCountPolicy_ != node.childCountPolicy_) {
			node.setChildCountPolicy(this.childCountPolicy_);
		}
		if (index == (int) this.childNodes_.size() - 1
				&& this.childNodes_.size() > 1) {
			this.childNodes_.get(this.childNodes_.size() - 2).update();
		}
		node.update();
		this.update();
		this.resetLearnedSlots();
	}

	/**
	 * Adds a child node.
	 * <p>
	 * Equivalent to:
	 * 
	 * <pre>
	 *   {@code
	 *    insertChildNode(childNodes().size(), node);
	 *   }
	 * </pre>
	 * <p>
	 * 
	 * @see WTreeNode#insertChildNode(int index, WTreeNode node)
	 */
	public void addChildNode(WTreeNode node) {
		this.insertChildNode(this.childNodes_.size(), node);
	}

	/**
	 * Removes a child node.
	 */
	public void removeChildNode(WTreeNode node) {
		this.childNodes_.remove(node);
		node.parentNode_ = null;
		if (this.childrenLoaded_) {
			this.getChildContainer().removeWidget(node);
		}
		this.descendantRemoved(node);
		this.updateChildren();
	}

	/**
	 * Returns the list of children.
	 */
	public List<WTreeNode> getChildNodes() {
		return this.childNodes_;
	}

	/**
	 * Returns the number of children that should be displayed.
	 * <p>
	 * This is used to display the count in the count label. The default
	 * implementation simply returns {@link WTreeNode#getChildNodes()
	 * getChildNodes()}.size().
	 */
	public int getDisplayedChildCount() {
		return this.childNodes_.size();
	}

	/**
	 * Configures how and when the child count should be displayed.
	 * <p>
	 * By default, no child count indication is disabled (this is the behaviour
	 * since 2.1.1). Use this method to enable child count indications.
	 * <p>
	 * The child count policy is inherited by all children in the tree.
	 */
	public void setChildCountPolicy(WTreeNode.ChildCountPolicy policy) {
		if (policy != WTreeNode.ChildCountPolicy.Disabled
				&& !(this.childCountLabel_ != null)) {
			this.childCountLabel_ = new WText();
			this.childCountLabel_.setMargin(new WLength(7),
					EnumSet.of(Side.Left));
			this.childCountLabel_.setStyleClass("Wt-childcount");
			this.getLabelArea().addWidget(this.childCountLabel_);
		}
		this.childCountPolicy_ = policy;
		if (this.childCountPolicy_ == WTreeNode.ChildCountPolicy.Enabled) {
			WTreeNode parent = this.getParentNode();
			if (parent != null && parent.isExpanded()) {
				if (this.isDoPopulate()) {
					this.update();
				}
			}
		}
		if (this.childCountPolicy_ != WTreeNode.ChildCountPolicy.Disabled) {
			for (int i = 0; i < this.childNodes_.size(); ++i) {
				this.childNodes_.get(i).setChildCountPolicy(
						this.childCountPolicy_);
			}
		}
	}

	/**
	 * Returns the child count policy.
	 * <p>
	 * 
	 * @see WTreeNode#setChildCountPolicy(WTreeNode.ChildCountPolicy policy)
	 */
	public WTreeNode.ChildCountPolicy getChildCountPolicy() {
		return this.childCountPolicy_;
	}

	/**
	 * Sets the image pack for this (sub)tree (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated This method does not do anything since JWt 3.1.1, as the tree
	 *             is now styled based on the current CSS theme.
	 */
	public void setImagePack(final String url) {
	}

	/**
	 * Sets the load policy for this tree.
	 * <p>
	 * This may only be set on the root of a tree, and before adding any
	 * children.
	 */
	public void setLoadPolicy(WTreeNode.LoadPolicy loadPolicy) {
		this.loadPolicy_ = loadPolicy;
		switch (loadPolicy) {
		case PreLoading:
			this.loadChildren();
			break;
		case NextLevelLoading:
			if (this.isExpanded()) {
				this.loadChildren();
				this.loadGrandChildren();
			} else {
				WTreeNode parent = this.getParentNode();
				if (parent != null && parent.isExpanded()) {
					this.loadChildren();
				}
				this.expandIcon_.icon1Clicked().addListener(this,
						new Signal1.Listener<WMouseEvent>() {
							public void trigger(WMouseEvent e1) {
								WTreeNode.this.loadGrandChildren();
							}
						});
			}
			break;
		case LazyLoading:
			if (this.isExpanded()) {
				this.loadChildren();
			} else {
				if (this.childCountPolicy_ == WTreeNode.ChildCountPolicy.Enabled) {
					WTreeNode parent = this.getParentNode();
					if (parent != null && parent.isExpanded()) {
						this.isDoPopulate();
					}
				}
				this.expandIcon_.icon1Clicked().addListener(this,
						new Signal1.Listener<WMouseEvent>() {
							public void trigger(WMouseEvent e1) {
								WTreeNode.this.expand();
							}
						});
			}
		}
		if (this.loadPolicy_ != WTreeNode.LoadPolicy.LazyLoading) {
			for (int i = 0; i < this.childNodes_.size(); ++i) {
				this.childNodes_.get(i).setLoadPolicy(this.loadPolicy_);
			}
		}
	}

	/**
	 * Returns whether this node is expanded.
	 */
	public boolean isExpanded() {
		return !this.collapsed_;
	}

	/**
	 * Allows this node to be selected.
	 * <p>
	 * By default, all nodes may be selected.
	 * <p>
	 * 
	 * @see WTree#select(WTreeNode node, boolean selected)
	 */
	public void setSelectable(boolean selectable) {
		this.selectable_ = selectable;
	}

	/**
	 * Returns if this node may be selected.
	 * <p>
	 * 
	 * @see WTreeNode#setSelectable(boolean selectable)
	 */
	public boolean isSelectable() {
		return this.selectable_;
	}

	/**
	 * Returns the parent node.
	 * <p>
	 * 
	 * @see WTreeNode#getChildNodes()
	 */
	public WTreeNode getParentNode() {
		return this.parentNode_;
	}

	/**
	 * Sets the visibility of the node itself.
	 * <p>
	 * If <code>false</code>, then the node itself is not displayed, but only
	 * its children. This is typically used to hide the root node of a tree.
	 */
	public void setNodeVisible(boolean visible) {
		this.visible_ = visible;
		this.updateChildren(false);
	}

	/**
	 * Sets whether this node&apos;s children are decorated.
	 * <p>
	 * By default, node&apos;s children have expand/collapse and other lines to
	 * display their linkage and offspring.
	 * <p>
	 * By setting <code>decorated</code> to <code>false</code>, you can hide the
	 * decorations for the node&apos;s children.
	 */
	public void setChildrenDecorated(boolean decorated) {
		this.childrenDecorated_ = decorated;
		this.updateChildren(false);
	}

	/**
	 * Sets whether this node is interactive.
	 * <p>
	 * Interactive nodes can be clicked upon and will populate a list of
	 * children when clicked. By disabling the interactivity, a node will not
	 * react to a click event.
	 */
	public void setInteractive(boolean interactive) {
		this.interactive_ = interactive;
	}

	/**
	 * Expands this node.
	 * <p>
	 * Besides the actual expansion of the node, this may also trigger the
	 * loading and population of the node children, or of the children&apos;s
	 * children.
	 * <p>
	 */
	public void expand() {
		if (!this.isExpanded()) {
			if (!this.childrenLoaded_) {
				this.loadChildren();
			}
			if (this.getParentNode() != null && this.childNodes_.isEmpty()) {
				this.getParentNode().resetLearnedSlots();
				this.update();
			}
			if (this.loadPolicy_ == WTreeNode.LoadPolicy.NextLevelLoading) {
				this.loadGrandChildren();
			}
			this.doExpand();
			this.updateChildren();
		}
	}

	/**
	 * Collapses this node.
	 * <p>
	 * 
	 * @see WTreeNode#expand()
	 */
	public void collapse() {
		if (this.isExpanded()) {
			this.doCollapse();
		}
	}

	/**
	 * Signal emitted when the node is expanded by the user.
	 * <p>
	 */
	public EventSignal1<WMouseEvent> expanded() {
		return this.expandIcon_.icon1Clicked();
	}

	/**
	 * Signal emitted when the node is collapsed by the user.
	 * <p>
	 * 
	 * @see WTreeNode#expanded()
	 */
	public EventSignal1<WMouseEvent> collapsed() {
		return this.expandIcon_.icon2Clicked();
	}

	/**
	 * Signal that is emitted when the node is added or removed from the
	 * selection
	 * <p>
	 * 
	 * @see WTree#itemSelectionChanged()
	 */
	public Signal1<Boolean> selected() {
		return this.selected_;
	}

	/**
	 * Creates a tree node with empty {@link }.
	 * <p>
	 * This tree node has no label or labelicon, and is therefore ideally suited
	 * to provide a custom look.
	 */
	protected WTreeNode(WTreeNode parent) {
		super();
		this.childNodes_ = new ArrayList<WTreeNode>();
		this.collapsed_ = true;
		this.selectable_ = true;
		this.visible_ = true;
		this.childrenDecorated_ = true;
		this.parentNode_ = null;
		this.childCountPolicy_ = WTreeNode.ChildCountPolicy.Disabled;
		this.labelIcon_ = null;
		this.labelText_ = null;
		this.childrenLoaded_ = false;
		this.populated_ = false;
		this.interactive_ = true;
		this.selected_ = new Signal1<Boolean>(this);
		this.clickedConnection_ = new AbstractSignal.Connection();
		this.create();
		if (parent != null) {
			parent.addChildNode(this);
		}
	}

	/**
	 * Creates a tree node with empty {@link }.
	 * <p>
	 * Calls {@link #WTreeNode(WTreeNode parent) this((WTreeNode)null)}
	 */
	protected WTreeNode() {
		this((WTreeNode) null);
	}

	/**
	 * Accesses the container widget that holds the label area.
	 * <p>
	 * Use this to customize how the label should look like.
	 */
	protected WContainerWidget getLabelArea() {
		return (WContainerWidget) this.layout_.resolveWidget("label-area");
	}

	/**
	 * Populates the node dynamically on loading.
	 * <p>
	 * Reimplement this method if you want to populate the widget dynamically,
	 * as the tree is being browsed and therefore loaded. This is only usefull
	 * with LazyLoading or NextLevelLoading strategies.
	 */
	protected void populate() {
	}

	/**
	 * Returns whether this node has already been populated.
	 * <p>
	 * 
	 * @see WTreeNode#populate()
	 */
	protected boolean isPopulated() {
		return this.populated_;
	}

	/**
	 * Returns whether this node can be expanded.
	 * <p>
	 * The default implementation populates the node if necessary, and then
	 * checks if there are any child nodes.
	 * <p>
	 * You may wish to reimplement this method if you reimplement
	 * {@link WTreeNode#populate() populate()}, and you have a quick default for
	 * determining whether a node may be expanded (which does not require
	 * populating the node).
	 * <p>
	 * 
	 * @see WTreeNode#populate()
	 */
	protected boolean isExpandable() {
		if (this.interactive_) {
			this.isDoPopulate();
			return !this.childNodes_.isEmpty();
		} else {
			return false;
		}
	}

	/**
	 * Renders the node to be selected.
	 * <p>
	 * The default implementation changes the style class of the
	 * {@link WTreeNode#getLabelArea() getLabelArea()} to &quot;selected&quot;.
	 */
	protected void renderSelected(boolean isSelected) {
		this.layout_.bindString("selected", isSelected ? WApplication
				.getInstance().getTheme().getActiveClass() : "");
		this.selected().trigger(isSelected);
	}

	/**
	 * The image pack that is used for this tree node (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated This method returns &quot;&quot; since JWt 3.1.1, as the
	 *             image pack is no longer used in favour of the CSS themes.
	 */
	protected String getImagePack() {
		return "";
	}

	/**
	 * Reacts to the removal of a descendant node.
	 * <p>
	 * Reimplement this method if you wish to react on the removal of a
	 * descendant node. The default implementation simply propagates the event
	 * to the parent.
	 */
	protected void descendantRemoved(WTreeNode node) {
		WTreeNode parent = this.getParentNode();
		if (parent != null) {
			parent.descendantRemoved(node);
		}
	}

	/**
	 * Reacts to the addition of a descendant node.
	 * <p>
	 * Reimplement this method if you wish to react on the addition of a
	 * descendant node. The default implementation simply propagates the event
	 * to the parent.
	 */
	protected void descendantAdded(WTreeNode node) {
		WTreeNode parent = this.getParentNode();
		if (parent != null) {
			parent.descendantAdded(node);
		}
	}

	/**
	 * The actual expand.
	 * <p>
	 * This method, which is implemented as a stateless slot, performs the
	 * actual expansion of the node.
	 * <p>
	 * You may want to reimplement this function (and {@link }) if you wish to do
	 * additional things on node expansion.
	 * <p>
	 * 
	 * @see WTreeNode#expand()
	 */
	protected void doExpand() {
		this.wasCollapsed_ = !this.isExpanded();
		this.collapsed_ = false;
		this.expandIcon_.setState(1);
		this.getChildContainer().show();
		if (this.labelIcon_ != null) {
			this.labelIcon_.setState(1);
		}
		for (int i = 0; i < this.childNodes_.size(); ++i) {
			this.childNodes_.get(i).doCollapse();
		}
	}

	/**
	 * The actual collapse.
	 * <p>
	 * This method, which is implemented as a stateless slot, performs the
	 * actual collapse of the node.
	 * <p>
	 * You may want to reimplement this function (and {@link }) if you wish to do
	 * additional things on node expansion.
	 * <p>
	 * 
	 * @see WTreeNode#doExpand()
	 * @see WTreeNode#collapse()
	 */
	protected void doCollapse() {
		this.wasCollapsed_ = !this.isExpanded();
		this.collapsed_ = true;
		this.expandIcon_.setState(0);
		this.getChildContainer().hide();
		if (this.labelIcon_ != null) {
			this.labelIcon_.setState(0);
		}
	}

	/**
	 * Undo method for {@link WTreeNode#doCollapse() doCollapse()} stateless
	 * implementation.
	 * <p>
	 * 
	 * @see WTreeNode#doCollapse()
	 */
	protected void undoDoExpand() {
		if (this.wasCollapsed_) {
			this.expandIcon_.setState(0);
			this.getChildContainer().hide();
			if (this.labelIcon_ != null) {
				this.labelIcon_.setState(0);
			}
			this.collapsed_ = true;
		}
		for (int i = 0; i < this.childNodes_.size(); ++i) {
			this.childNodes_.get(i).undoDoCollapse();
		}
	}

	/**
	 * Undo method for {@link WTreeNode#doCollapse() doCollapse()} stateless
	 * implementation.
	 * <p>
	 * 
	 * @see WTreeNode#doExpand()
	 */
	protected void undoDoCollapse() {
		if (!this.wasCollapsed_) {
			this.expandIcon_.setState(1);
			this.getChildContainer().show();
			if (this.labelIcon_ != null) {
				this.labelIcon_.setState(1);
			}
			this.collapsed_ = false;
		}
	}

	/**
	 * Accesses the icon pair that allows expansion of the tree node.
	 */
	protected WIconPair getExpandIcon() {
		return this.expandIcon_;
	}

	protected WTemplate getImpl() {
		return this.layout_;
	}

	private List<WTreeNode> childNodes_;
	private boolean collapsed_;
	private boolean selectable_;
	private boolean visible_;
	private boolean childrenDecorated_;
	private WTreeNode parentNode_;
	private WTreeNode.LoadPolicy loadPolicy_;
	private WTreeNode.ChildCountPolicy childCountPolicy_;
	private WTemplate layout_;
	private WIconPair expandIcon_;
	private WText noExpandIcon_;
	private WIconPair labelIcon_;
	private WText labelText_;
	private WText childCountLabel_;
	private boolean childrenLoaded_;
	private boolean populated_;
	private boolean interactive_;
	private Signal1<Boolean> selected_;

	private WContainerWidget getChildContainer() {
		return (WContainerWidget) this.layout_.resolveWidget("children");
	}

	private void loadChildren() {
		if (!this.childrenLoaded_) {
			this.isDoPopulate();
			for (int i = 0; i < this.childNodes_.size(); ++i) {
				this.getChildContainer().addWidget(this.childNodes_.get(i));
			}
			this.expandIcon_.icon1Clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WTreeNode.this.doExpand();
						}
					});
			this.expandIcon_.icon2Clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WTreeNode.this.doCollapse();
						}
					});
			this.resetLearnedSlots();
			this.childrenLoaded_ = true;
		}
	}

	private void loadGrandChildren() {
		for (int i = 0; i < this.childNodes_.size(); ++i) {
			this.childNodes_.get(i).loadChildren();
		}
	}

	private void create() {
		this.setImplementation(this.layout_ = new WTemplate(
				tr("Wt.WTreeNode.template")));
		this.setStyleClass("Wt-tree");
		this.layout_.setSelectable(false);
		this.layout_.bindEmpty("cols-row");
		this.layout_.bindEmpty("trunk-class");
		// this.implementStateless(WTreeNode.doExpand,WTreeNode.undoDoExpand);
		// this.implementStateless(WTreeNode.doCollapse,WTreeNode.undoDoCollapse);
		WApplication app = WApplication.getInstance();
		WContainerWidget children = new WContainerWidget();
		children.setList(true);
		children.hide();
		this.layout_.bindWidget("children", children);
		if (WApplication.getInstance().getLayoutDirection() == LayoutDirection.RightToLeft) {
			this.expandIcon_ = new WIconPair(app.getTheme().getResourcesUrl()
					+ imagePlusRtl_, app.getTheme().getResourcesUrl()
					+ imageMinRtl_);
		} else {
			this.expandIcon_ = new WIconPair(app.getTheme().getResourcesUrl()
					+ imagePlus_, app.getTheme().getResourcesUrl() + imageMin_);
		}
		this.expandIcon_.setStyleClass("Wt-ctrl Wt-expand");
		this.noExpandIcon_ = new WText();
		this.noExpandIcon_.setStyleClass("Wt-ctrl Wt-noexpand");
		this.layout_.bindWidget("expand", this.noExpandIcon_);
		this.addStyleClass("Wt-trunk");
		this.layout_.bindWidget("label-area", new WContainerWidget());
		if (this.labelText_ != null) {
			this.labelText_.setStyleClass("Wt-label");
		}
		this.childCountLabel_ = null;
		if (this.labelIcon_ != null) {
			this.getLabelArea().addWidget(this.labelIcon_);
			this.labelIcon_.setVerticalAlignment(AlignmentFlag.AlignMiddle);
		}
		if (this.labelText_ != null) {
			this.getLabelArea().addWidget(this.labelText_);
		}
		this.childrenLoaded_ = false;
		this.setLoadPolicy(WTreeNode.LoadPolicy.LazyLoading);
	}

	private void update() {
		boolean isLast = this.isLastChildNode();
		if (!this.visible_) {
			this.layout_.bindString("selected", "Wt-root");
			this.getChildContainer().addStyleClass("Wt-root");
		} else {
			this.layout_.bindEmpty("selected");
			this.getChildContainer().removeStyleClass("Wt-root");
		}
		WTreeNode parent = this.getParentNode();
		if (parent != null && !parent.childrenDecorated_) {
		}
		if (this.expandIcon_.getState() != (this.isExpanded() ? 1 : 0)) {
			this.expandIcon_.setState(this.isExpanded() ? 1 : 0);
		}
		if (this.getChildContainer().isHidden() != !this.isExpanded()) {
			this.getChildContainer().setHidden(!this.isExpanded());
		}
		if (this.labelIcon_ != null
				&& this.labelIcon_.getState() != (this.isExpanded() ? 1 : 0)) {
			this.labelIcon_.setState(this.isExpanded() ? 1 : 0);
		}
		this.toggleStyleClass("Wt-trunk", !isLast);
		this.layout_.bindString("trunk-class", isLast ? "Wt-end" : "Wt-trunk");
		if (!(this.getParentNode() != null)
				|| this.getParentNode().isExpanded()) {
			if (this.childCountPolicy_ == WTreeNode.ChildCountPolicy.Enabled
					&& !this.populated_) {
				this.isDoPopulate();
			}
			if (!this.isExpandable()) {
				if (this.noExpandIcon_.getParent() == null) {
					this.layout_.takeWidget("expand");
					this.layout_.bindWidget("expand", this.noExpandIcon_);
				}
			} else {
				if (this.expandIcon_.getParent() == null) {
					this.layout_.takeWidget("expand");
					this.layout_.bindWidget("expand", this.expandIcon_);
				}
			}
		}
		if (this.childCountPolicy_ != WTreeNode.ChildCountPolicy.Disabled
				&& this.populated_ && this.childCountLabel_ != null) {
			int n = this.getDisplayedChildCount();
			if (n != 0) {
				this.childCountLabel_.setText(new WString("("
						+ String.valueOf(n) + ")"));
			} else {
				this.childCountLabel_.setText(new WString());
			}
		}
	}

	private boolean isLastChildNode() {
		WTreeNode parent = this.getParentNode();
		if (parent != null) {
			return parent.childNodes_.get(parent.childNodes_.size() - 1) == this;
		} else {
			return true;
		}
	}

	private void updateChildren(boolean recursive) {
		for (int i = 0; i < this.childNodes_.size(); ++i) {
			if (recursive) {
				this.childNodes_.get(i).updateChildren(recursive);
			} else {
				this.childNodes_.get(i).update();
			}
		}
		this.update();
		this.resetLearnedSlots();
	}

	private final void updateChildren() {
		updateChildren(false);
	}

	private boolean wasCollapsed_;

	private boolean isDoPopulate() {
		if (!this.populated_) {
			this.populated_ = true;
			this.populate();
			return true;
		} else {
			return false;
		}
	}

	private static String imagePlus_ = "nav-plus.gif";
	private static String imageMin_ = "nav-minus.gif";
	private static String imagePlusRtl_ = "nav-plus-rtl.gif";
	private static String imageMinRtl_ = "nav-minus-rtl.gif";
	AbstractSignal.Connection clickedConnection_;
}
