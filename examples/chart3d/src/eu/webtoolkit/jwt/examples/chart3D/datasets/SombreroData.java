/*
 * Copyright (C) 2014 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D.datasets;

import eu.webtoolkit.jwt.*;

public class SombreroData extends EquidistantGrid {
	public SombreroData(int nbXPts, int nbYPts, double xStart, double xEnd,
			double yStart, double yEnd, WObject parent) {
		super(parent);
		this.nbXPts_ = nbXPts;
		this.nbYPts_ = nbYPts;
		this.xStart_ = xStart;
		this.xEnd_ = xEnd;
		this.yStart_ = yStart;
		this.yEnd_ = yEnd;
	}

	public SombreroData(int nbXPts, int nbYPts, double xStart, double xEnd,
			double yStart, double yEnd) {
		this(nbXPts, nbYPts, xStart, xEnd, yStart, yEnd, (WObject) null);
	}

	public int getRowCount(final WModelIndex parent) {
		return this.nbXPts_ + 1;
	}

	public int getColumnCount(final WModelIndex parent) {
		return this.nbYPts_ + 1;
	}

	public Object getData(int row, int column, int role,
			final WModelIndex parent) {
		return this.getData(this.createIndex(row, column, null), role);
	}

	public Object getData(final WModelIndex index, int role) {
		if (role != ItemDataRole.DisplayRole) {
			return null;
		}
		double delta_y = (this.yEnd_ - this.yStart_) / (this.nbYPts_ - 1);
		if (index.getRow() == 0) {
			if (index.getColumn() == 0) {
				return 0.0;
			}
			return this.yStart_ + (index.getColumn() - 1) * delta_y;
		}
		double delta_x = (this.xEnd_ - this.xStart_) / (this.nbXPts_ - 1);
		if (index.getColumn() == 0) {
			if (index.getRow() == 0) {
				return 0.0;
			}
			return this.xStart_ + (index.getRow() - 1) * delta_x;
		}
		double x;
		double y;
		y = this.yStart_ + (index.getColumn() - 1) * delta_y;
		x = this.xStart_ + (index.getRow() - 1) * delta_x;
		return 4 * Math.sin(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)))
				/ Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	public Object getHeaderData(int section, Orientation orientation, int role) {
		return 0.0;
	}

	public void update(double xStart, double xEnd, double yStart, double yEnd,
			int nbXPts, int nbYPts) {
		this.nbXPts_ = nbXPts;
		this.nbYPts_ = nbYPts;
		this.xStart_ = xStart;
		this.xEnd_ = xEnd;
		this.yStart_ = yStart;
		this.yEnd_ = yEnd;
		this.reset();
	}

	public double getXMin() {
		return this.xStart_;
	}

	public double getXMax() {
		return this.xEnd_;
	}

	public double getYMin() {
		return this.yStart_;
	}

	public double getYMax() {
		return this.yEnd_;
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
	private double xEnd_;
	private double yStart_;
	private double yEnd_;
}
