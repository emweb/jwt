/*
 * Copyright (C) 2014 Emweb bvba, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D.datasets;

import java.util.ArrayList;

import javax.vecmath.Point3d;

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WObject;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WModelIndex;

public class PointListData extends WStandardItemModel {
	public PointListData(ArrayList<Point3d> points, WObject parent) {
		super(points.size(), 4, parent);

		this.points = points;
	}

	@Override
	public Object getData(WModelIndex index, int role) {
		if (role != ItemDataRole.DisplayRole) {
			return super.getData(index, role);
		}
		if (index.getColumn() == 0) {
			return points.get(index.getRow()).x;
		} else if (index.getColumn() == 1) {
			return points.get(index.getRow()).y;
		} else if (index.getColumn() == 2) {
			return points.get(index.getRow()).z;
		} else if (index.getColumn() == 3) {
			return new WColor(0, 50, 200);
		} else {
			return new Object();
		}
	}

	public ArrayList<Point3d> points;
}
