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
 * A read-only wrapper for a source model.
 * <p>
 * 
 * This is a simple proxy model which provides a read-only view on a source
 * model. This is convenient for situations where you want to share a common
 * read-only source model between different sessions.
 */
public class WReadOnlyProxyModel extends WAbstractProxyModel {
	private static Logger logger = LoggerFactory
			.getLogger(WReadOnlyProxyModel.class);

	/**
	 * Constructor.
	 */
	public WReadOnlyProxyModel(WObject parent) {
		super(parent);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WReadOnlyProxyModel(WObject parent) this((WObject)null)}
	 */
	public WReadOnlyProxyModel() {
		this((WObject) null);
	}

	public WModelIndex mapFromSource(WModelIndex sourceIndex) {
		return sourceIndex;
	}

	public WModelIndex mapToSource(WModelIndex proxyIndex) {
		return proxyIndex;
	}

	public int getColumnCount(WModelIndex parent) {
		return this.getSourceModel().getColumnCount(parent);
	}

	public int getRowCount(WModelIndex parent) {
		return this.getSourceModel().getRowCount(parent);
	}

	public WModelIndex getParent(WModelIndex index) {
		return this.getSourceModel().getParent(index);
	}

	public WModelIndex getIndex(int row, int column, WModelIndex parent) {
		return this.getSourceModel().getIndex(row, column, parent);
	}
}
