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

class PointsData extends WAbstractTableModel {
	private static Logger logger = LoggerFactory.getLogger(PointsData.class);

	public PointsData(int nbPts, WObject parent) {
		super();
		this.nbPts_ = nbPts;
	}

	public PointsData(int nbPts) {
		this(nbPts, (WObject) null);
	}

	public int getRowCount(final WModelIndex parent) {
		return this.nbPts_;
	}

	public int getColumnCount(final WModelIndex parent) {
		return 3;
	}

	public Object getData(int row, int column, int role,
			final WModelIndex parent) {
		return this.getData(this.createIndex(row, column, null), role);
	}

	public Object getData(final WModelIndex index, int role) {
		if (role == ItemDataRole.MarkerBrushColorRole) {
			return new WColor(0, 255, 0);
		} else {
			if (role != ItemDataRole.DisplayRole) {
				return null;
			}
		}
		final double pi = 3.141592;
		double XYangle = index.getRow() * (8 * pi / this.nbPts_);
		if (index.getColumn() == 0) {
			return Math.cos(XYangle);
		}
		if (index.getColumn() == 1) {
			return Math.sin(XYangle);
		}
		if (index.getColumn() == 2) {
			return -5.0 + index.getRow() * (10.0 / this.nbPts_);
		}
		return null;
	}

	public Object getHeaderData(int section, Orientation orientation, int role) {
		return 0.0;
	}

	private int nbPts_;
}
