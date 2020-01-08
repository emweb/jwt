/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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

class ContentsContainer extends WContainerWidget {
	private static Logger logger = LoggerFactory
			.getLogger(ContentsContainer.class);

	public ContentsContainer(WTreeView treeView) {
		super();
		this.treeView_ = treeView;
		this.setLayoutSizeAware(true);
	}

	protected void layoutSizeChanged(int width, int height) {
		this.treeView_.contentsSizeChanged(width, height);
	}

	private WTreeView treeView_;
}
