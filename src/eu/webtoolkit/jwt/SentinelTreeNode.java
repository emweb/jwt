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

class SentinelTreeNode extends WTreeNode {
	private static Logger logger = LoggerFactory
			.getLogger(SentinelTreeNode.class);

	public SentinelTreeNode(WTree tree) {
		super("");
		this.tree_ = tree;
		this.addStyleClass("Wt-sentinel");
		this.setNodeVisible(false);
		this.expand();
	}

	public WTree getTree() {
		return this.tree_;
	}

	protected void descendantRemoved(WTreeNode node) {
		this.tree_.nodeRemoved(node);
	}

	protected void descendantAdded(WTreeNode node) {
		this.tree_.nodeAdded(node);
	}

	private WTree tree_;
}
