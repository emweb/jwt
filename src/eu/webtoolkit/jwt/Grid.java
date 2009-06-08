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

class Grid {
	public int horizontalSpacing_;
	public int verticalSpacing_;

	public static class Row {
		public int stretch_;

		public Row(int stretch) {
			this.stretch_ = stretch;
		}

		public Row() {
			this(0);
		}
	}

	public static class Column {
		public int stretch_;

		public Column(int stretch) {
			this.stretch_ = stretch;
		}

		public Column() {
			this(0);
		}
	}

	public static class Item {
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

	public void destroy() {
		for (int i = 0; i < this.items_.size(); ++i) {
			for (int j = 0; j < this.items_.get(i).size(); ++j) {
				/* delete this.items_.get(i).get(j).item_ */;
			}
		}
	}
}
