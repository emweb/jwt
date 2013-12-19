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

abstract class EquidistantGrid extends WAbstractTableModel {
	private static Logger logger = LoggerFactory
			.getLogger(EquidistantGrid.class);

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
