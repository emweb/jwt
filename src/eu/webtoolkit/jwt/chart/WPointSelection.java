/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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

/**
 * Represents a single point selection on a {@link WScatterData}.
 */
public class WPointSelection extends WSelection {
	private static Logger logger = LoggerFactory
			.getLogger(WPointSelection.class);

	/**
	 * The row number of the WAbstractDataModel that the selected point is
	 * defined in.
	 */
	public int rowNumber;

	public WPointSelection() {
		super();
		this.rowNumber = -1;
	}

	public WPointSelection(double distance, int rowNumber) {
		super(distance);
		this.rowNumber = rowNumber;
	}
}
