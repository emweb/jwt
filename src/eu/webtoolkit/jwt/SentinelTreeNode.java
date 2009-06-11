package eu.webtoolkit.jwt;


class SentinelTreeNode extends WTreeNode {
	public SentinelTreeNode(WTree tree) {
		super("");
		this.tree_ = tree;
		this.setNodeVisible(false);
		this.expand();
	}

	protected void descendantRemoved(WTreeNode node) {
		this.tree_.nodeRemoved(node);
	}

	protected void descendantAdded(WTreeNode node) {
		this.tree_.nodeAdded(node);
	}

	private WTree tree_;
}
