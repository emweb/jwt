/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

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

class PageState {
	private static Logger logger = LoggerFactory.getLogger(PageState.class);

	public PageState() {
		this.floats = new ArrayList<Block>();
	}

	public double y;
	public double minX;
	public double maxX;
	public List<Block> floats;
	public int page;
	static final double MARGINX = -1;
	static final double EPSILON = 1e-4;
}
