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

class PlaneData extends EquidistantGrid {
	private static Logger logger = LoggerFactory.getLogger(PlaneData.class);

	public PlaneData(int nbXPts, int nbYPts, double xStart, double xDelta,
			double yStart, double yDelta, WObject parent) {
		super(parent);
		this.nbXPts_ = nbXPts;
		this.nbYPts_ = nbYPts;
		this.xStart_ = xStart;
		this.xDelta_ = xDelta;
		this.yStart_ = yStart;
		this.yDelta_ = yDelta;
	}

	public PlaneData(int nbXPts, int nbYPts, double xStart, double xDelta,
			double yStart, double yDelta) {
		this(nbXPts, nbYPts, xStart, xDelta, yStart, yDelta, (WObject) null);
	}

	public int getRowCount(final WModelIndex parent) {
		return this.nbXPts_;
	}

	public int getColumnCount(final WModelIndex parent) {
		return this.nbYPts_;
	}

	public Object getData(int row, int column, int role,
			final WModelIndex parent) {
		return this.getData(this.createIndex(row, column, null), role);
	}

	public Object getData(final WModelIndex index, int role) {
		if (role != ItemDataRole.DisplayRole) {
			return null;
		}
		double x;
		double y;
		y = this.yStart_ + index.getColumn() * this.yDelta_;
		x = this.xStart_ + index.getRow() * this.xDelta_;
		return 0.5 * y;
	}

	public Object getHeaderData(int section, Orientation orientation, int role) {
		return 0.0;
	}

	public void update(double xStart, double xDelta, double yStart,
			double yDelta, int nbXPts, int nbYPts) {
		this.nbXPts_ = nbXPts;
		this.nbYPts_ = nbYPts;
		this.xStart_ = xStart;
		this.xDelta_ = xDelta;
		this.yStart_ = yStart;
		this.yDelta_ = yDelta;
		this.reset();
	}

	public double getXMin() {
		return this.xStart_;
	}

	public double getXMax() {
		return this.xStart_ + this.nbXPts_ * this.xDelta_;
	}

	public double getYMin() {
		return this.yStart_;
	}

	public double getYMax() {
		return this.yStart_ + this.nbYPts_ * this.yDelta_;
	}

	public int getNbXPts() {
		return this.nbXPts_;
	}

	public int getNbYPts() {
		return this.nbYPts_;
	}

	private int nbXPts_;
	private int nbYPts_;
	private double xStart_;
	private double xDelta_;
	private double yStart_;
	private double yDelta_;
}
