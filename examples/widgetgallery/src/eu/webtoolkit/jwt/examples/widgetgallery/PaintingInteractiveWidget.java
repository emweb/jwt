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

class PaintingInteractiveWidget extends WPaintedWidget {
	private static Logger logger = LoggerFactory
			.getLogger(PaintingInteractiveWidget.class);

	public PaintingInteractiveWidget(WContainerWidget parent) {
		super(parent);
		this.rotateSlot = new JSlot(1, this);
		this.transform = null;
		this.resize(new WLength(300), new WLength(300));
		this.transform = this.createJSTransform();
		this.rotateSlot
				.setJavaScript("function(o,e,deg) {if ("
						+ this.getObjJsRef()
						+ ") {var rad = deg / 180 * Math.PI;var c = Math.cos(rad);var s = Math.sin(rad);"
						+ this.transform.getJsRef() + " = [c,-s,s,c,0,0];"
						+ this.getRepaintSlot().execJs() + ";}}");
	}

	public PaintingInteractiveWidget() {
		this((WContainerWidget) null);
	}

	public void rotate(int degrees) {
		double radians = degrees / 180.0 * 3.14159265358979323846;
		double c = Math.cos(radians);
		double s = Math.sin(radians);
		this.transform.setValue(new WTransform(c, -s, s, c, 0, 0));
		this.update();
	}

	public JSlot rotateSlot;

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		painter.translate(150, 150);
		WPen pen = new WPen();
		pen.setWidth(new WLength(5));
		painter.setPen(pen);
		WPainterPath path = new WPainterPath();
		path.moveTo(-50, 100);
		path.lineTo(50, 100);
		path.lineTo(50, 20);
		path.lineTo(100, 20);
		path.lineTo(0, -100);
		path.lineTo(-100, 20);
		path.lineTo(-50, 20);
		path.lineTo(-50, 100);
		path.lineTo(50, 100);
		WPainterPath transformedPath = this.transform.getValue().map(path);
		painter.drawPath(transformedPath);
	}

	private WJavaScriptHandle<WTransform> transform;
}
