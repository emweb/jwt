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

final class SentinelTreeNode extends WTreeNode {
  private static Logger logger = LoggerFactory.getLogger(SentinelTreeNode.class);

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
