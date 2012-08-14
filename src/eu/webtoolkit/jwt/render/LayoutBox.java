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

class LayoutBox {
	private static Logger logger = LoggerFactory.getLogger(LayoutBox.class);

	public LayoutBox() {
		this.page = -1;
		this.x = 0;
		this.y = 0;
		this.width = 0;
		this.height = 0;
	}

	public int page;
	public double x;
	public double y;
	public double width;
	public double height;
}
