/*
 * Copyright (C) 2014 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D.datasets;

import eu.webtoolkit.jwt.*;

import java.util.Objects;

public class PlaneData extends WStandardItemModel {
	public PlaneData(int nbXpts, int nbYpts) {
		this(nbXpts, nbYpts, false);
	}

	public PlaneData(int nbXpts, int nbYpts, boolean reversedy) {
		this(nbXpts, nbYpts, false, reversedy);
	}

	public PlaneData(int nbXpts, int nbYpts, boolean reversedx,
			boolean reversedy) {
		super(nbXpts + 1, nbYpts + 1);
		this.xStart_ = -10.0;
		this.xEnd_ = 10.0;
		this.yStart_ = -10.0;
		this.yEnd_ = 10.0;
		this.reversedx_ = reversedx;
		this.reversedy_ = reversedy;
	}

	public Object getData(final WModelIndex index, ItemDataRole role) {
		if (!Objects.equals(role, ItemDataRole.Display)) {
			return super.getData(index, role);
		}
		double delta_x = (this.xEnd_ - this.xStart_) / (this.getRowCount() - 2);
		double delta_y = (this.yEnd_ - this.yStart_)
				/ (this.getColumnCount() - 2);
		double x = this.xStart_ + index.getRow() * delta_x;
		if (reversedx_)
			x = this.xEnd_ - x;
		double y = this.yStart_ + index.getColumn() * delta_y;
		if (reversedy_)
			y = this.yEnd_ - y;
		return 0.2 * x - 0.2 * y;
	}

	private final double xStart_;
	private final double xEnd_;
	private final double yStart_;
	private final double yEnd_;
	private final boolean reversedx_;
	private final boolean reversedy_;
}
