/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class VirtualModel extends WAbstractTableModel {
	private static Logger logger = LoggerFactory.getLogger(VirtualModel.class);

	public VirtualModel(int rows, int columns, WObject parent) {
		super(parent);
		this.rows_ = rows;
		this.columns_ = columns;
	}

	public VirtualModel(int rows, int columns) {
		this(rows, columns, (WObject) null);
	}

	public int getRowCount(final WModelIndex parent) {
		if (!(parent != null)) {
			return this.rows_;
		} else {
			return 0;
		}
	}

	public int getColumnCount(final WModelIndex parent) {
		if (!(parent != null)) {
			return this.columns_;
		} else {
			return 0;
		}
	}

	public Object getData(final WModelIndex index, int role) {
		switch (role) {
		case ItemDataRole.DisplayRole:
			if (index.getColumn() == 0) {
				return new WString("Row {1}").arg(index.getRow());
			} else {
				return new WString("Item row {1}, col {2}").arg(index.getRow())
						.arg(index.getColumn());
			}
		default:
			return null;
		}
	}

	public Object getHeaderData(int section, Orientation orientation, int role) {
		if (orientation == Orientation.Horizontal) {
			switch (role) {
			case ItemDataRole.DisplayRole:
				return new WString("Column {1}").arg(section);
			default:
				return null;
			}
		} else {
			return null;
		}
	}

	private int rows_;
	private int columns_;

	static void virtualModel() {
	}
}
