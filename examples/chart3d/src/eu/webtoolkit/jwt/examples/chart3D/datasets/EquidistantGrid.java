/*
 * Copyright (C) 2014 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D.datasets;

import eu.webtoolkit.jwt.*;

abstract class EquidistantGrid extends WAbstractTableModel {
	public EquidistantGrid(WObject parent) {
		super(parent);
	}

	public EquidistantGrid() {
		this((WObject) null);
	}

	public abstract double getXMin();

	public abstract double getXMax();

	public abstract double getYMin();

	public abstract double getYMax();

	public abstract int getNbXPts();

	public abstract int getNbYPts();

	public abstract int getRowCount(final WModelIndex parent);

	public abstract int getColumnCount(final WModelIndex parent);

	public abstract Object getData(final WModelIndex index, int role);

	public abstract Object getHeaderData(int section, Orientation orientation,
			int role);
}
