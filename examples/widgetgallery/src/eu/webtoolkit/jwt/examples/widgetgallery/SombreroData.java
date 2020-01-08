/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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

class SombreroData extends WStandardItemModel {
	private static Logger logger = LoggerFactory.getLogger(SombreroData.class);

	public SombreroData(int nbXpts, int nbYpts, WObject parent) {
		super(nbXpts + 1, nbYpts + 1, parent);
		this.xStart_ = -10.0;
		this.xEnd_ = 10.0;
		this.yStart_ = -10.0;
		this.yEnd_ = 10.0;
	}

	public SombreroData(int nbXpts, int nbYpts) {
		this(nbXpts, nbYpts, (WObject) null);
	}

	public Object getData(final WModelIndex index, int role) {
		if (role != ItemDataRole.DisplayRole) {
			return super.getData(index, role);
		}
		double delta_y = (this.yEnd_ - this.yStart_)
				/ (this.getColumnCount() - 2);
		if (index.getRow() == 0) {
			if (index.getColumn() == 0) {
				return 0.0;
			}
			return this.yStart_ + (index.getColumn() - 1) * delta_y;
		}
		double delta_x = (this.xEnd_ - this.xStart_) / (this.getRowCount() - 2);
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

	private final double xStart_;
	private final double xEnd_;
	private final double yStart_;
	private final double yEnd_;
}
