/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

class PaintedSlider extends WPaintedWidget {
	public PaintedSlider(WSlider slider) {
		super();
		this.slider_ = slider;
		this.sliderReleased_ = new JSignal1<Integer>(this, "released") {
		};
		this.mouseDownJS_ = new JSlot();
		this.mouseMovedJS_ = new JSlot();
		this.mouseUpJS_ = new JSlot();
		this.setStyleClass("Wt-slider-bg");
		this.slider_
				.setStyleClass("Wt-slider-"
						+ (this.slider_.getOrientation() == Orientation.Horizontal ? "h"
								: "v"));
		this.slider_.setPositionScheme(PositionScheme.Relative);
		this.addChild(this.handle_ = new WContainerWidget());
		this.handle_.setPopup(true);
		this.handle_.setPositionScheme(PositionScheme.Absolute);
		this.handle_.mouseWentDown().addListener(this.mouseDownJS_);
		this.handle_.mouseMoved().addListener(this.mouseMovedJS_);
		this.handle_.mouseWentUp().addListener(this.mouseUpJS_);
		slider.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				PaintedSlider.this.onSliderClick(e1);
			}
		});
		this.sliderReleased_.addListener(this, new Signal1.Listener<Integer>() {
			public void trigger(Integer e1) {
				PaintedSlider.this.onSliderReleased(e1);
			}
		});
	}

	public void updateState() {
		String resourcesURL = WApplication.getResourcesUrl();
		Orientation o = this.slider_.getOrientation();
		this.handle_.setStyleClass("handle");
		if (o == Orientation.Horizontal) {
			this.handle_.resize(new WLength(HANDLE_WIDTH), new WLength(this
					.getH()));
			this.handle_.setOffsets(new WLength(0), EnumSet.of(Side.Top));
		} else {
			this.handle_.resize(new WLength(this.getW()), new WLength(
					HANDLE_WIDTH));
			this.handle_.setOffsets(new WLength(0), EnumSet.of(Side.Left));
		}
		double l = o == Orientation.Horizontal ? this.getW() : this.getH();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		String dir = o == Orientation.Horizontal ? "left" : "top";
		String u = o == Orientation.Horizontal ? "x" : "y";
		String U = o == Orientation.Horizontal ? "X" : "Y";
		String maxS = String.valueOf(l - HANDLE_WIDTH);
		String ppU = String.valueOf(pixelsPerUnit);
		String minimumS = String.valueOf(this.slider_.getMinimum());
		String maximumS = String.valueOf(this.slider_.getMaximum());
		this.mouseDownJS_
				.setJavaScript("function(obj, event) {obj.setAttribute('down', Wt3_1_8.widgetCoordinates(obj, event)."
						+ u + "); Wt3_1_8.cancelEvent(event);}");
		String computeD = "var objh = " + this.handle_.getJsRef() + ",objb = "
				+ this.getJsRef() + ",u = WT.pageCoordinates(event)." + u
				+ " - down,w = WT.widgetPageCoordinates(objb)." + u
				+ ",d = u-w;";
		this.mouseMovedJS_
				.setJavaScript("function(obj, event) {var down = obj.getAttribute('down');var WT = Wt3_1_8;if (down != null && down != '') {"
						+ computeD
						+ "d = Math.max(0, Math.min(d, "
						+ maxS
						+ "));var v = Math.round(d/"
						+ ppU
						+ ");var intd = v*"
						+ ppU
						+ ";if (Math.abs(WT.pxself(objh, '"
						+ dir
						+ "') - intd) > 1) {objh.style."
						+ dir
						+ " = intd + 'px';"
						+ this.slider_.sliderMoved().createCall(
								o == Orientation.Horizontal ? "v + " + minimumS
										: maximumS + " - v") + "}}}");
		this.mouseUpJS_
				.setJavaScript("function(obj, event) {var down = obj.getAttribute('down');var WT = Wt3_1_8;if (down != null && down != '') {"
						+ computeD
						+ "d += "
						+ String.valueOf(HANDLE_WIDTH / 2)
						+ ";"
						+ this.sliderReleased_.createCall("d")
						+ "obj.removeAttribute('down');}}");
		this.update();
		this.updateSliderPosition();
	}

	public void updateSliderPosition() {
		double l = this.slider_.getOrientation() == Orientation.Horizontal ? this
				.getW()
				: this.getH();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		double u = ((double) this.slider_.getValue() - this.slider_
				.getMinimum())
				* pixelsPerUnit;
		if (this.slider_.getOrientation() == Orientation.Horizontal) {
			this.handle_.setOffsets(new WLength(u), EnumSet.of(Side.Left));
		} else {
			this.handle_.setOffsets(
					new WLength(this.getH() - HANDLE_WIDTH - u), EnumSet
							.of(Side.Top));
		}
	}

	public void doUpdateDom(DomElement element, boolean all) {
		if (all) {
			WApplication app = WApplication.getInstance();
			element.addChild(this.createSDomElement(app));
			element
					.addChild(((WWebWidget) this.handle_)
							.createSDomElement(app));
			DomElement west = DomElement
					.createNew(DomElementType.DomElement_DIV);
			west.setProperty(Property.PropertyClass, "Wt-w");
			element.addChild(west);
			DomElement east = DomElement
					.createNew(DomElementType.DomElement_DIV);
			east.setProperty(Property.PropertyClass, "Wt-e");
			element.addChild(east);
		}
	}

	public void sliderResized(WLength width, WLength height) {
		if (this.slider_.getOrientation() == Orientation.Horizontal) {
			WLength w = width;
			if (!w.isAuto()) {
				w = new WLength(w.toPixels() - 10);
			}
			this.resize(w, height);
		} else {
			WLength h = height;
			if (!h.isAuto()) {
				h = new WLength(h.toPixels() - 10);
			}
			this.resize(width, h);
		}
		this.updateState();
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		if (!this.slider_.getTickPosition().isEmpty()) {
			WPainter painter = new WPainter(paintDevice);
			int w;
			int h;
			if (this.slider_.getOrientation() == Orientation.Horizontal) {
				w = (int) this.getWidth().toPixels();
				h = (int) this.getHeight().toPixels();
			} else {
				w = (int) this.getHeight().toPixels();
				h = (int) this.getWidth().toPixels();
				painter.translate(0, w);
				painter.rotate(-90);
			}
			int tickInterval = this.slider_.getTickInterval();
			int r = this.getRange();
			if (tickInterval == 0) {
				tickInterval = r / 2;
			}
			double tickStep = ((double) w - (HANDLE_WIDTH - 10))
					/ (r / tickInterval);
			WPen pen = new WPen();
			pen.setColor(new WColor(0xd7, 0xd7, 0xd7));
			pen.setCapStyle(PenCapStyle.FlatCap);
			pen.setWidth(new WLength(1));
			painter.setPen(pen);
			int y1 = h / 4;
			int y2 = h / 2 - 4;
			int y3 = h / 2 + 4;
			int y4 = h - h / 4;
			for (int i = 0;; ++i) {
				int x = (HANDLE_WIDTH - 10) / 2 + (int) (i * tickStep);
				if (x > w - (HANDLE_WIDTH - 10) / 2) {
					break;
				}
				if (!EnumUtils.mask(this.slider_.getTickPosition(),
						WSlider.TickPosition.TicksAbove).isEmpty()) {
					painter.drawLine(x + 0.5, y1, x + 0.5, y2);
				}
				if (!EnumUtils.mask(this.slider_.getTickPosition(),
						WSlider.TickPosition.TicksBelow).isEmpty()) {
					painter.drawLine(x + 0.5, y3, x + 0.5, y4);
				}
			}
		}
	}

	private WSlider slider_;
	private JSignal1<Integer> sliderReleased_;
	private JSlot mouseDownJS_;
	private JSlot mouseMovedJS_;
	private JSlot mouseUpJS_;
	private WContainerWidget handle_;

	private int getRange() {
		return this.slider_.getMaximum() - this.slider_.getMinimum();
	}

	private double getW() {
		return this.getWidth().toPixels()
				+ (this.slider_.getOrientation() == Orientation.Horizontal ? 10
						: 0);
	}

	private double getH() {
		return this.getHeight().toPixels()
				+ (this.slider_.getOrientation() == Orientation.Vertical ? 10
						: 0);
	}

	private void onSliderClick(WMouseEvent event) {
		this
				.onSliderReleased(this.slider_.getOrientation() == Orientation.Horizontal ? event
						.getWidget().x
						: event.getWidget().y);
	}

	private void onSliderReleased(int u) {
		if (this.slider_.getOrientation() == Orientation.Horizontal) {
			u -= HANDLE_WIDTH / 2;
		} else {
			u = (int) this.getH() - (u + HANDLE_WIDTH / 2);
		}
		double l = this.slider_.getOrientation() == Orientation.Horizontal ? this
				.getW()
				: this.getH();
		double pixelsPerUnit = (l - HANDLE_WIDTH) / this.getRange();
		double v = Math.max(this.slider_.getMinimum(), Math.min(this.slider_
				.getMaximum(), this.slider_.getMinimum()
				+ (int) ((double) u / pixelsPerUnit + 0.5)));
		this.slider_.sliderMoved().trigger((int) v);
		this.slider_.setValue((int) v);
		this.slider_.valueChanged().trigger(this.slider_.getValue());
		this.updateSliderPosition();
	}

	private static final int HANDLE_WIDTH = 20;
}
