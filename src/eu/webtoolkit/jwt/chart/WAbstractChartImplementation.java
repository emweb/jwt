/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

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

interface WAbstractChartImplementation {
	static class RenderRange {
		private static Logger logger = LoggerFactory
				.getLogger(RenderRange.class);

		public double minimum;
		public double maximum;
	}

	public ChartType getChartType();

	public Orientation getOrientation();

	public int getAxisPadding();

	public int numberOfCategories(Axis axis);

	public int numberOfCategories();

	public WString categoryLabel(int u, Axis axis);

	public WAbstractChartImplementation.RenderRange computeRenderRange(
			Axis axis, AxisScale scale);

	public void update();
}
