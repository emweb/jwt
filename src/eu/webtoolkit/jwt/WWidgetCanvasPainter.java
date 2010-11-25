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

class WWidgetCanvasPainter extends WWidgetPainter {
	public WWidgetCanvasPainter(WPaintedWidget widget) {
		super(widget);
	}

	public WPaintDevice getPaintDevice() {
		return new WCanvasPaintDevice(new WLength(this.widget_.renderWidth_),
				new WLength(this.widget_.renderHeight_));
	}

	public void createContents(DomElement result, WPaintDevice device) {
		String wstr = String.valueOf(this.widget_.renderWidth_);
		String hstr = String.valueOf(this.widget_.renderHeight_);
		result.setProperty(Property.PropertyStylePosition, "relative");
		result.setProperty(Property.PropertyStyleOverflowX, "hidden");
		DomElement canvas = DomElement
				.createNew(DomElementType.DomElement_CANVAS);
		canvas.setId('c' + this.widget_.getId());
		canvas.setAttribute("width", wstr);
		canvas.setAttribute("height", hstr);
		result.addChild(canvas);
		WCanvasPaintDevice canvasDevice = ((device) instanceof WCanvasPaintDevice ? (WCanvasPaintDevice) (device)
				: null);
		DomElement text = null;
		if (canvasDevice.getTextMethod() == WCanvasPaintDevice.TextMethod.DomText) {
			text = DomElement.createNew(DomElementType.DomElement_DIV);
			text.setId('t' + this.widget_.getId());
			text.setProperty(Property.PropertyStylePosition, "absolute");
			text.setProperty(Property.PropertyStyleZIndex, "1");
			text.setProperty(Property.PropertyStyleTop, "0px");
			text.setProperty(Property.PropertyStyleLeft, "0px");
		}
		canvasDevice.render("c" + this.widget_.getId(), text != null ? text
				: result);
		if (text != null) {
			result.addChild(text);
		}
		;
	}

	public void updateContents(List<DomElement> result, WPaintDevice device) {
		WCanvasPaintDevice canvasDevice = ((device) instanceof WCanvasPaintDevice ? (WCanvasPaintDevice) (device)
				: null);
		if (this.widget_.sizeChanged_) {
			DomElement canvas = DomElement.getForUpdate('c' + this.widget_
					.getId(), DomElementType.DomElement_CANVAS);
			canvas.setAttribute("width", String
					.valueOf(this.widget_.renderWidth_));
			canvas.setAttribute("height", String
					.valueOf(this.widget_.renderHeight_));
			result.add(canvas);
			this.widget_.sizeChanged_ = false;
		}
		boolean domText = canvasDevice.getTextMethod() == WCanvasPaintDevice.TextMethod.DomText;
		DomElement el = DomElement.getForUpdate(domText ? 't' + this.widget_
				.getId() : this.widget_.getId(), DomElementType.DomElement_DIV);
		if (domText) {
			el.removeAllChildren();
		}
		canvasDevice.render('c' + this.widget_.getId(), el);
		result.add(el);
		;
	}

	public WWidgetPainter.RenderType getRenderType() {
		return WWidgetPainter.RenderType.HtmlCanvas;
	}
}
