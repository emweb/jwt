/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.List;

class WWidgetCanvasPainter extends WWidgetPainter {
	public WWidgetCanvasPainter(WPaintedWidget widget) {
		super(widget);
	}

	public WPaintDevice getCreatePaintDevice() {
		return new WCanvasPaintDevice(this.widget_.getWidth(), this.widget_
				.getHeight());
	}

	public void createContents(DomElement result, WPaintDevice device) {
		String wstr = String.valueOf(this.widget_.getWidth().getValue());
		String hstr = String.valueOf(this.widget_.getHeight().getValue());
		result.setProperty(Property.PropertyStylePosition, "relative");
		DomElement canvas = DomElement
				.createNew(DomElementType.DomElement_CANVAS);
		canvas.setId('c' + this.widget_.getFormName());
		canvas.setAttribute("width", wstr);
		canvas.setAttribute("height", hstr);
		result.addChild(canvas);
		DomElement text = DomElement.createNew(DomElementType.DomElement_DIV);
		text.setId('t' + this.widget_.getFormName());
		text.setProperty(Property.PropertyStylePosition, "absolute");
		text.setProperty(Property.PropertyStyleZIndex, "1");
		text.setProperty(Property.PropertyStyleTop, "0px");
		text.setProperty(Property.PropertyStyleLeft, "0px");
		WCanvasPaintDevice canvasDevice = ((device) instanceof WCanvasPaintDevice ? (WCanvasPaintDevice) (device)
				: null);
		canvasDevice.render("c" + this.widget_.getFormName(), text);
		result.addChild(text);
	}

	public void updateContents(List<DomElement> result, WPaintDevice device) {
		WCanvasPaintDevice canvasDevice = ((device) instanceof WCanvasPaintDevice ? (WCanvasPaintDevice) (device)
				: null);
		if (this.widget_.sizeChanged_) {
			DomElement canvas = DomElement.getForUpdate('c' + this.widget_
					.getFormName(), DomElementType.DomElement_CANVAS);
			canvas.setAttribute("width", String.valueOf(this.widget_.getWidth()
					.getValue()));
			canvas.setAttribute("height", String.valueOf(this.widget_
					.getHeight().getValue()));
			result.add(canvas);
			this.widget_.sizeChanged_ = false;
		}
		DomElement text = DomElement.getForUpdate('t' + this.widget_
				.getFormName(), DomElementType.DomElement_DIV);
		text.removeAllChildren();
		canvasDevice.render('c' + this.widget_.getFormName(), text);
		result.add(text);
	}
}
