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
		this.addChild(this.handle_ = this.slider_.getCreateHandle());
		this.addChild(this.fill_ = new WContainerWidget());
		this.fill_.setPositionScheme(PositionScheme.Absolute);
		this.fill_.setStyleClass("fill");
		this.handle_.setPopup(true);
		this.handle_.setPositionScheme(PositionScheme.Absolute);
		this.handle_.setStyleClass("handle");
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
		Orientation o = this.slider_.getOrientation();
		if (o == Orientation.Horizontal) {
			this.handle_.resize(new WLength(this.slider_.getHandleWidth()),
					new WLength(this.getH()));
			this.handle_.setOffsets(new WLength(0), EnumSet.of(Side.Top));
		} else {
			this.handle_.resize(new WLength(this.getW()), new WLength(
					this.slider_.getHandleWidth()));
			this.handle_.setOffsets(new WLength(0), EnumSet.of(Side.Left));
		}
		double l = o == Orientation.Horizontal ? this.getW() : this.getH();
		double pixelsPerUnit = (l - this.slider_.getHandleWidth())
				/ this.getRange();
		String dir = "";
		String size = "";
		if (o == Orientation.Horizontal) {
			dir = rtl ? "right" : "left";
			size = "width";
		} else {
			dir = "top";
			size = "height";
		}
		char u = o == Orientation.Horizontal ? 'x' : 'y';
		double max = l - this.slider_.getHandleWidth();
		boolean horizontal = o == Orientation.Horizontal;
		StringBuilder mouseDownJS = new StringBuilder();
		mouseDownJS.append("obj.setAttribute('down', Wt3_3_0").append(
				".widgetCoordinates(obj, event).").append(u).append(");")
				.append("Wt3_3_0.cancelEvent(event);");
		StringBuilder computeD = new StringBuilder();
		computeD.append("var objh = ").append(this.handle_.getJsRef()).append(
				",").append("objf = ").append(this.fill_.getJsRef())
				.append(",").append("objb = ").append(this.getJsRef()).append(
						",").append("page_u = WT.pageCoordinates(event).")
				.append(u).append(",").append(
						"widget_page_u = WT.widgetPageCoordinates(objb).")
				.append(u).append(",").append("pos = page_u - widget_page_u,")
				.append("rtl = ").append(rtl).append(",").append(
						"horizontal = ").append(horizontal).append(";").append(
						"if (rtl && horizontal)").append("pos = ").append(l)
				.append(" - pos;").append("var d = pos - down;");
		StringBuilder mouseMovedJS = new StringBuilder();
		mouseMovedJS.append("var down = obj.getAttribute('down');").append(
				"var WT = Wt3_3_0;")
				.append("if (down != null && down != '') {").append(
						computeD.toString()).append(
						"d = Math.max(0, Math.min(d, ").append(max).append(
						"));").append("var v = Math.round(d/").append(
						pixelsPerUnit).append(");").append("var intd = v*")
				.append(pixelsPerUnit).append(";").append(
						"if (Math.abs(WT.pxself(objh, '").append(dir).append(
						"') - intd) > 1) {").append("objf.style.").append(size)
				.append(" = ");
		if (o == Orientation.Vertical) {
			mouseMovedJS.append('(').append(max).append(" - intd)");
		} else {
			mouseMovedJS.append("intd");
		}
		mouseMovedJS.append(" + 'px';").append("objh.style.").append(dir)
				.append(" = intd + 'px';").append("var vs = ");
		if (o == Orientation.Horizontal) {
			mouseMovedJS.append("v + ").append(this.slider_.getMinimum());
		} else {
			mouseMovedJS.append(this.slider_.getMaximum()).append(" - v");
		}
		mouseMovedJS.append(";").append(
				"var f = objb.parentNode.onValueChange;").append(
				"if (f) f(vs);").append(
				this.slider_.sliderMoved().createCall("vs")).append("}")
				.append("}");
		StringBuilder mouseUpJS = new StringBuilder();
		mouseUpJS.append("var down = obj.getAttribute('down');").append(
				"var WT = Wt3_3_0;")
				.append("if (down != null && down != '') {").append(
						computeD.toString()).append("d += ").append(
						this.slider_.getHandleWidth() / 2).append(";").append(
						this.sliderReleased_.createCall("d")).append(
						"obj.removeAttribute('down');").append("}");
		boolean enabled = !this.slider_.isDisabled();
		this.mouseDownJS_.setJavaScript("function(obj, event) {"
				+ (enabled ? mouseDownJS.toString() : "") + "}");
		this.mouseMovedJS_.setJavaScript("function(obj, event) {"
				+ (enabled ? mouseMovedJS.toString() : "") + "}");
		this.mouseUpJS_.setJavaScript("function(obj, event) {"
				+ (enabled ? mouseUpJS.toString() : "") + "}");
		this.update();
		this.updateSliderPosition();
	}

	public void updateSliderPosition() {
		double l = this.slider_.getOrientation() == Orientation.Horizontal ? this
				.getW()
				: this.getH();
		double pixelsPerUnit = (l - this.slider_.getHandleWidth())
				/ this.getRange();
		double u = ((double) this.slider_.getValue() - this.slider_
				.getMinimum())
				* pixelsPerUnit;
		if (this.slider_.getOrientation() == Orientation.Horizontal) {
			this.handle_.setOffsets(new WLength(u), EnumSet.of(Side.Left));
			this.fill_.setWidth(new WLength(u));
		} else {
			this.handle_.setOffsets(new WLength(this.getH()
					- this.slider_.getHandleWidth() - u), EnumSet.of(Side.Top));
			this.fill_.setHeight(new WLength(u));
		}
	}

	public void doUpdateDom(DomElement element, boolean all) {
		if (all) {
			WApplication app = WApplication.getInstance();
			DomElement west = DomElement
					.createNew(DomElementType.DomElement_DIV);
			west.setProperty(Property.PropertyClass, "Wt-w");
			element.addChild(west);
			DomElement east = DomElement
					.createNew(DomElementType.DomElement_DIV);
			east.setProperty(Property.PropertyClass, "Wt-e");
			element.addChild(east);
			element.addChild(this.createSDomElement(app));
			element
					.addChild(((WWebWidget) this.handle_)
							.createSDomElement(app));
			element.addChild(((WWebWidget) this.fill_).createSDomElement(app));
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
		int tickInterval = this.slider_.getTickInterval();
		int r = this.getRange();
		if (tickInterval == 0) {
			tickInterval = r / 2;
		}
		int numTicks = r / tickInterval + 1;
		if (numTicks < 1) {
			return;
		}
		int w = 0;
		int h = 0;
		switch (this.slider_.getOrientation()) {
		case Horizontal:
			w = (int) paintDevice.getWidth().toPixels();
			h = (int) paintDevice.getHeight().toPixels();
			break;
		case Vertical:
			w = (int) paintDevice.getHeight().toPixels();
			h = (int) paintDevice.getWidth().toPixels();
		}
		double tickStep = ((double) w + 10 - this.slider_.getHandleWidth())
				/ (numTicks - 1);
		WPainter painter = new WPainter(paintDevice);
		for (int i = 0; i < numTicks; ++i) {
			int v = this.slider_.getMinimum() + i * tickInterval;
			int x = -5 + this.slider_.getHandleWidth() / 2
					+ (int) (i * tickStep);
			switch (this.slider_.getOrientation()) {
			case Horizontal:
				this.slider_.paintTick(painter, v, x, h / 2);
				break;
			case Vertical:
				this.slider_.paintTick(painter, v, h / 2, w - x);
			}
		}
	}

	private WSlider slider_;
	private JSignal1<Integer> sliderReleased_;
	private JSlot mouseDownJS_;
	private JSlot mouseMovedJS_;
	private JSlot mouseUpJS_;
	private WInteractWidget handle_;
	private WInteractWidget fill_;

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
			u -= this.slider_.getHandleWidth() / 2;
		} else {
			u = (int) this.getH() - (u + this.slider_.getHandleWidth() / 2);
		}
		double l = this.slider_.getOrientation() == Orientation.Horizontal ? this
				.getW()
				: this.getH();
		double pixelsPerUnit = (l - this.slider_.getHandleWidth())
				/ this.getRange();
		double v = Math.max(this.slider_.getMinimum(), Math.min(this.slider_
				.getMaximum(), this.slider_.getMinimum()
				+ (int) ((double) u / pixelsPerUnit + 0.5)));
		this.slider_.sliderMoved().trigger((int) v);
		this.slider_.setValue((int) v);
		this.slider_.valueChanged().trigger(this.slider_.getValue());
		this.updateSliderPosition();
	}
}
