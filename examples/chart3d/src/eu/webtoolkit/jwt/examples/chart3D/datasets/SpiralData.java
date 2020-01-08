/*
 * Copyright (C) 2014 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D.datasets;

import eu.webtoolkit.jwt.*;

public class SpiralData extends WStandardItemModel {
	public SpiralData(int nbPts) {
		this(nbPts, null);
	}

	public SpiralData(int nbPts, WObject parent) {
		super(nbPts, 3, parent);
		this.nbPts_ = nbPts;
	}

	public Object getData(final WModelIndex index, int role) {
		if (role != ItemDataRole.DisplayRole) {
			return super.getData(index, role);
		}
		double XYangle = index.getRow() * (8 * Math.PI / this.nbPts_);
		double heightRatio = (float) index.getRow() / this.getRowCount();
		double radius = 1.0 + heightRatio * 5.0;
		if (index.getColumn() == 0) {
			return radius * Math.cos(XYangle);
		} else {
			if (index.getColumn() == 1) {
				return radius * Math.sin(XYangle);
			} else {
				if (index.getColumn() == 2) {
					return 5.0 - index.getRow() * (10.0 / this.nbPts_);
				} else {
					return null;
				}
			}
		}
	}

	private int nbPts_;
}
