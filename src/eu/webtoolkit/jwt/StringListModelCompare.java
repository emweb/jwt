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

class StringListModelCompare implements Comparator<Integer> {
	private static Logger logger = LoggerFactory
			.getLogger(StringListModelCompare.class);

	public WStringListModel model_;
	public SortOrder order_;

	public StringListModelCompare(WStringListModel model, SortOrder order) {
		super();
		this.model_ = model;
		this.order_ = order;
	}

	public int compare(Integer r1, Integer r2) {
		int result = this.model_.getStringList().get(r1).compareTo(
				this.model_.getStringList().get(r2));
		if (this.order_ == SortOrder.DescendingOrder) {
			result = -result;
		}
		return result;
	}
}
