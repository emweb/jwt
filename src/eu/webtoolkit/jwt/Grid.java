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

class Grid {
	private static Logger logger = LoggerFactory.getLogger(Grid.class);

	public int horizontalSpacing_;
	public int verticalSpacing_;

	static class Row {
		private static Logger logger = LoggerFactory.getLogger(Row.class);

		public int stretch_;
		public boolean resizable_;

		public Row(int stretch) {
			this.stretch_ = stretch;
			this.resizable_ = false;
		}

		public Row() {
			this(0);
		}
	}

	static class Column {
		private static Logger logger = LoggerFactory.getLogger(Column.class);

		public int stretch_;
		public boolean resizable_;

		public Column(int stretch) {
			this.stretch_ = stretch;
			this.resizable_ = false;
		}

		public Column() {
			this(0);
		}
	}

	static class Item {
		private static Logger logger = LoggerFactory.getLogger(Item.class);

		public WLayoutItem item_;
		public int rowSpan_;
		public int colSpan_;
		public EnumSet<AlignmentFlag> alignment_;

		public Item(WLayoutItem item, EnumSet<AlignmentFlag> alignment) {
			this.item_ = item;
			this.rowSpan_ = 1;
			this.colSpan_ = 1;
			this.alignment_ = alignment;
		}

		public Item(WLayoutItem item, AlignmentFlag alignmen,
				AlignmentFlag... alignment) {
			this(item, EnumSet.of(alignmen, alignment));
		}

		public Item() {
			this((WLayoutItem) null, EnumSet.noneOf(AlignmentFlag.class));
		}

		public Item(WLayoutItem item) {
			this(item, EnumSet.noneOf(AlignmentFlag.class));
		}
	}

	public List<Grid.Row> rows_;
	public List<Grid.Column> columns_;
	public List<List<Grid.Item>> items_;

	public Grid() {
		this.horizontalSpacing_ = 6;
		this.verticalSpacing_ = 6;
		this.rows_ = new ArrayList<Grid.Row>();
		this.columns_ = new ArrayList<Grid.Column>();
		this.items_ = new ArrayList<List<Grid.Item>>();
	}

	public void clear() {
		this.rows_.clear();
		this.columns_.clear();
		this.items_.clear();
	}
}
