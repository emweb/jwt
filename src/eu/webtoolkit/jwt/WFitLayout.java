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
 * A layout manager which spans a single widget to all available space.
 * <p>
 * 
 * This layout manager may manage only a single child widget, and sizes that
 * widget so that it uses all space available in the parent.
 */
public class WFitLayout extends WLayout {
	private static Logger logger = LoggerFactory.getLogger(WFitLayout.class);

	/**
	 * Creates a new fit layout.
	 */
	public WFitLayout(WWidget parent) {
		super();
		this.grid_ = new Grid();
		{
			int insertPos = 0;
			for (int ii = 0; ii < 1; ++ii)
				this.grid_.columns_.add(insertPos + ii, new Grid.Section(0));
		}
		;
		{
			int insertPos = 0;
			for (int ii = 0; ii < 1; ++ii)
				this.grid_.rows_.add(insertPos + ii, new Grid.Section(0));
		}
		;
		{
			int insertPos = 0;
			for (int ii = 0; ii < 1; ++ii)
				this.grid_.items_.add(insertPos + ii,
						new ArrayList<Grid.Item>());
		}
		;
		for (int i = 0; i < 1; ++i) {
			final List<Grid.Item> items = this.grid_.items_.get(i);
			{
				int insertPos = 0;
				for (int ii = 0; ii < 1; ++ii)
					items.add(insertPos + ii, new Grid.Item());
			}
			;
		}
		if (parent != null) {
			this.setLayoutInParent(parent);
		}
	}

	/**
	 * Creates a new fit layout.
	 * <p>
	 * Calls {@link #WFitLayout(WWidget parent) this((WWidget)null)}
	 */
	public WFitLayout() {
		this((WWidget) null);
	}

	public void addItem(WLayoutItem item) {
		if (this.grid_.items_.get(0).get(0).item_ != null) {
			logger.error(new StringWriter().append(
					"addItem(): already have a widget").toString());
			return;
		}
		this.grid_.items_.get(0).get(0).item_ = item;
		this.updateAddItem(item);
	}

	public void removeItem(WLayoutItem item) {
		if (item == this.grid_.items_.get(0).get(0).item_) {
			this.grid_.items_.get(0).get(0).item_ = null;
			this.updateRemoveItem(item);
		}
	}

	public WLayoutItem getItemAt(int index) {
		return this.grid_.items_.get(0).get(0).item_;
	}

	public int indexOf(WLayoutItem item) {
		if (this.grid_.items_.get(0).get(0).item_ == item) {
			return 0;
		} else {
			return -1;
		}
	}

	public int getCount() {
		return this.grid_.items_.get(0).get(0).item_ != null ? 1 : 0;
	}

	public void clear() {
		this.clearLayoutItem(this.grid_.items_.get(0).get(0).item_);
		this.grid_.items_.get(0).get(0).item_ = null;
	}

	public Grid getGrid() {
		return this.grid_;
	}

	private Grid grid_;
}
