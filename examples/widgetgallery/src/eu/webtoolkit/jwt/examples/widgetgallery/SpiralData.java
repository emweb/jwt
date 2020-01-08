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

class SpiralData extends WStandardItemModel {
	private static Logger logger = LoggerFactory.getLogger(SpiralData.class);

	public SpiralData(int nbPts, WObject parent) {
		super(nbPts, 3, parent);
		this.nbPts_ = nbPts;
	}

	public SpiralData(int nbPts) {
		this(nbPts, (WObject) null);
	}

	public Object getData(final WModelIndex index, int role) {
		if (role != ItemDataRole.DisplayRole) {
			return super.getData(index, role);
		}
		final double pi = 3.141592;
		double XYangle = index.getRow() * (8 * pi / this.nbPts_);
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
