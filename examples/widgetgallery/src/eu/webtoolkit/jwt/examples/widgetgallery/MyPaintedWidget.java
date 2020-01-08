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

class MyPaintedWidget extends WPaintedWidget {
	private static Logger logger = LoggerFactory
			.getLogger(MyPaintedWidget.class);

	public MyPaintedWidget(WContainerWidget parent) {
		super(parent);
		this.end_ = 100;
		this.resize(new WLength(200), new WLength(60));
	}

	public MyPaintedWidget() {
		this((WContainerWidget) null);
	}

	public void setEnd(int end) {
		this.end_ = end;
		this.update();
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		painter.setBrush(new WBrush(WColor.blue).clone());
		painter.drawRect(0, 0, this.end_, 50);
	}

	private int end_;
}
