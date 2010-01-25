/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


class SentinelTreeNode extends WTreeNode {
	public SentinelTreeNode(WTree tree) {
		super("");
		this.tree_ = tree;
		this.setStyleClass("Wt-tree Wt-sentinel");
		this.setNodeVisible(false);
		this.expand();
		if (WApplication.getInstance().getEnvironment().agentIsIE()) {
			this.getImpl().getElementAt(1, 0).resize(new WLength(1),
					WLength.Auto);
		}
	}

	protected void descendantRemoved(WTreeNode node) {
		this.tree_.nodeRemoved(node);
	}

	protected void descendantAdded(WTreeNode node) {
		this.tree_.nodeAdded(node);
	}

	private WTree tree_;
}
