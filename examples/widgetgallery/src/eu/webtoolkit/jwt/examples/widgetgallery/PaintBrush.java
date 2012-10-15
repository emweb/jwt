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

class PaintBrush extends WPaintedWidget {
	private static Logger logger = LoggerFactory.getLogger(PaintBrush.class);

	public PaintBrush(int width, int height, WContainerWidget parent) {
		super(parent);
		this.path_ = new WPainterPath();
		this.color_ = new WColor();
		this.setSelectable(false);
		this.resize(new WLength(width), new WLength(height));
		this.getDecorationStyle().setCursor("icons/pencil.cur",
				Cursor.CrossCursor);
		this.mouseDragged().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						PaintBrush.this.mouseDrag(e1);
					}
				});
		this.mouseWentDown().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						PaintBrush.this.mouseDown(e1);
					}
				});
		this.touchStarted().addListener(this,
				new Signal1.Listener<WTouchEvent>() {
					public void trigger(WTouchEvent e1) {
						PaintBrush.this.touchStart(e1);
					}
				});
		this.touchMoved().addListener(this,
				new Signal1.Listener<WTouchEvent>() {
					public void trigger(WTouchEvent e1) {
						PaintBrush.this.touchMove(e1);
					}
				});
		this.touchMoved().preventDefaultAction();
		this.color_ = WColor.black;
	}

	public PaintBrush(int width, int height) {
		this(width, height, (WContainerWidget) null);
	}

	public void clear() {
		this.update();
	}

	public void setColor(WColor c) {
		this.color_ = c;
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		painter.setRenderHint(WPainter.RenderHint.Antialiasing);
		WPen pen = new WPen();
		pen.setWidth(new WLength(3));
		pen.setColor(this.color_);
		pen.setCapStyle(PenCapStyle.FlatCap);
		pen.setJoinStyle(PenJoinStyle.MiterJoin);
		painter.setPen(pen);
		painter.drawPath(this.path_);
		this.path_.assign(new WPainterPath(this.path_.getCurrentPosition()));
	}

	private WPainterPath path_;
	private WColor color_;

	private void mouseDown(WMouseEvent e) {
		Coordinates c = e.getWidget();
		this.path_.assign(new WPainterPath(new WPointF(c.x, c.y)));
	}

	private void mouseDrag(WMouseEvent e) {
		Coordinates c = e.getWidget();
		this.path_.lineTo(c.x, c.y);
		this.update(EnumSet.of(PaintFlag.PaintUpdate));
	}

	private void touchStart(WTouchEvent e) {
		Coordinates c = e.getTouches().get(0).getWidget();
		this.path_.assign(new WPainterPath(new WPointF(c.x, c.y)));
	}

	private void touchMove(WTouchEvent e) {
		Coordinates c = e.getTouches().get(0).getWidget();
		this.path_.lineTo(c.x, c.y);
		this.update(EnumSet.of(PaintFlag.PaintUpdate));
	}
}
