package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

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
