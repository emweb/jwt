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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PaintedSlider extends WPaintedWidget {
	private static Logger logger = LoggerFactory.getLogger(PaintedSlider.class);

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
				.addStyleClass("Wt-slider-"
						+ (this.slider_.getOrientation() == Orientation.Horizontal ? "h"
								: "v"));
		if (this.slider_.getPositionScheme() == PositionScheme.Static) {
			this.slider_.setPositionScheme(PositionScheme.Relative);
			this.slider_.setOffsets(new WLength(0), EnumSet.of(Side.Left,
					Side.Top));
		}
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
		boolean rtl = WApplication.getInstance().getLayoutDirection() == LayoutDirection.RightToLeft;
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
		String dir = "";
		if (o == Orientation.Horizontal) {
			dir = rtl ? "right" : "left";
		} else {
			dir = "top";
		}
		String u = o == Orientation.Horizontal ? "x" : "y";
		String U = o == Orientation.Horizontal ? "X" : "Y";
		String maxS = String.valueOf(l - HANDLE_WIDTH);
		String ppU = String.valueOf(pixelsPerUnit);
		String minimumS = String.valueOf(this.slider_.getMinimum());
		String maximumS = String.valueOf(this.slider_.getMaximum());
		String width = String.valueOf(this.getW());
		String horizontal = String.valueOf(o == Orientation.Horizontal);
		String mouseDownJS = "obj.setAttribute('down', Wt3_2_0.widgetCoordinates(obj, event)."
				+ u + "); Wt3_2_0.cancelEvent(event);";
		String computeD = "var objh = " + this.handle_.getJsRef() + ",objb = "
				+ this.getJsRef() + ",page_u = WT.pageCoordinates(event)." + u
				+ ",widget_page_u = WT.widgetPageCoordinates(objb)." + u
				+ ",pos = page_u - widget_page_u,rtl = " + String.valueOf(rtl)
				+ ",horizontal = " + horizontal
				+ ";if (rtl && horizontal)  pos = " + width
				+ " - pos;var d = pos - down;";
		String mouseMovedJS = "var down = obj.getAttribute('down');var WT = Wt3_2_0;if (down != null && down != '') {"
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
								: maximumS + " - v") + "}}";
		String mouseUpJS = "var down = obj.getAttribute('down');var WT = Wt3_2_0;if (down != null && down != '') {"
				+ computeD
				+ "d += "
				+ String.valueOf(HANDLE_WIDTH / 2)
				+ ";"
				+ this.sliderReleased_.createCall("d")
				+ "obj.removeAttribute('down');}";
		boolean enabled = !this.slider_.isDisabled();
		this.mouseDownJS_.setJavaScript("function(obj, event) {"
				+ (enabled ? mouseDownJS : "") + "}");
		this.mouseMovedJS_.setJavaScript("function(obj, event) {"
				+ (enabled ? mouseMovedJS : "") + "}");
		this.mouseUpJS_.setJavaScript("function(obj, event) {"
				+ (enabled ? mouseUpJS : "") + "}");
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

	public void propagateSetEnabled(boolean enabled) {
		if (enabled) {
			this.removeStyleClass("Wt-disabled");
			this.slider_.removeStyleClass("Wt-disabled");
		} else {
			this.addStyleClass("Wt-disabled");
			this.slider_.addStyleClass("Wt-disabled");
		}
		super.propagateSetEnabled(enabled);
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
		int x = event.getWidget().x;
		int y = event.getWidget().y;
		if (WApplication.getInstance().getLayoutDirection() == LayoutDirection.RightToLeft) {
			x = (int) (this.getW() - x);
		}
		this
				.onSliderReleased(this.slider_.getOrientation() == Orientation.Horizontal ? x
						: y);
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
