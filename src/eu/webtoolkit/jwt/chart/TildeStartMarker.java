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

class TildeStartMarker extends WPainterPath {
	private static Logger logger = LoggerFactory
			.getLogger(TildeStartMarker.class);

	public TildeStartMarker(int segmentMargin) {
		super();
		this.moveTo(0, 0);
		this.lineTo(0, segmentMargin - 25);
		this.moveTo(-15, segmentMargin - 10);
		this.lineTo(15, segmentMargin - 20);
	}
}
