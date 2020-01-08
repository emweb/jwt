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

class ClippingWidget extends WPaintedWidget {
	private static Logger logger = LoggerFactory
			.getLogger(ClippingWidget.class);

	public ClippingWidget(WContainerWidget parent) {
		super(parent);
		this.resize(new WLength(310), new WLength(150));
	}

	public ClippingWidget() {
		this((WContainerWidget) null);
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		for (int i = 0; i < 2; i++) {
			painter.translate(i * 160, 0);
			painter.fillRect(0, 0, 150, 150, new WBrush(WColor.black));
			WPainterPath path = new WPainterPath();
			path.addEllipse(15, 15, 120, 120);
			painter.fillPath(path, new WBrush(WColor.blue));
			painter.setClipPath(path);
			painter.setClipping(i != 0);
			this.drawStars(painter);
		}
	}

	private void drawStar(final WPainter painter, double radius) {
		painter.save();
		WPainterPath circlePath = new WPainterPath();
		circlePath.addEllipse(0, 0, radius, radius);
		circlePath.closeSubPath();
		painter.fillPath(circlePath, new WBrush(WColor.white));
		painter.restore();
	}

	private void drawStars(final WPainter painter) {
		Random random = new Random();
		random.setSeed(WDate.getCurrentServerDate().getDate().getTime());
		painter.save();
		painter.translate(75, 75);
		for (int star = 1; star < 50; star++) {
			painter.save();
			painter.translate(75 - random.nextInt() % 150 + 1,
					75 - random.nextInt() % 150 + 1);
			this.drawStar(painter, Math.max(0, random.nextInt() % 4) + 2);
			painter.restore();
		}
		painter.restore();
	}
}
