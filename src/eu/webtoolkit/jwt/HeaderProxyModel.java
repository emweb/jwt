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

class HeaderProxyModel extends WAbstractTableModel {
	private static Logger logger = LoggerFactory
			.getLogger(HeaderProxyModel.class);

	public HeaderProxyModel(WAbstractItemModel model, WObject parent) {
		super(parent);
		this.model_ = model;
	}

	public int getColumnCount(final WModelIndex parent) {
		return this.model_.getColumnCount();
	}

	public int getRowCount(final WModelIndex parent) {
		return 1;
	}

	public Object getData(final WModelIndex index, int role) {
		return this.model_.getHeaderData(index.getColumn(),
				Orientation.Horizontal, role);
	}

	public boolean setData(final WModelIndex index, final Object value, int role) {
		return this.model_.setHeaderData(index.getColumn(),
				Orientation.Horizontal, value, role);
	}

	public EnumSet<ItemFlag> getFlags(final WModelIndex index) {
		EnumSet<HeaderFlag> headerFlags = this.model_.getHeaderFlags(index
				.getColumn(), Orientation.Horizontal);
		EnumSet<ItemFlag> result = EnumSet.noneOf(ItemFlag.class);
		if (!EnumUtils.mask(headerFlags, HeaderFlag.HeaderIsUserCheckable)
				.isEmpty()) {
			result.add(ItemFlag.ItemIsUserCheckable);
		}
		if (!EnumUtils.mask(headerFlags, HeaderFlag.HeaderIsTristate).isEmpty()) {
			result.add(ItemFlag.ItemIsTristate);
		}
		return result;
	}

	private WAbstractItemModel model_;
}
