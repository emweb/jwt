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

class SinModel extends WAbstractChartModel {
	private static Logger logger = LoggerFactory.getLogger(SinModel.class);

	public SinModel(double minimum, double maximum, WObject parent) {
		super(parent);
		this.minimum_ = minimum;
		this.maximum_ = maximum;
	}

	public SinModel(double minimum, double maximum) {
		this(minimum, maximum, (WObject) null);
	}

	public double getData(int row, int column) {
		double x = this.minimum_ + row * (this.maximum_ - this.minimum_)
				/ (this.getRowCount() - 1);
		if (column == 0) {
			return x;
		} else {
			return Math.sin(x) + Math.sin(x * 100.0) / 40.0;
		}
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return 100;
	}

	public double getMinimum() {
		return this.minimum_;
	}

	public double getMaximum() {
		return this.maximum_;
	}

	private double minimum_;
	private double maximum_;
}
