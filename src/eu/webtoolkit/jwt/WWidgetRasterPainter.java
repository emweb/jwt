/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.List;

class WWidgetRasterPainter extends WWidgetPainter {
	public WWidgetRasterPainter(WPaintedWidget widget) {
		super(widget);
	}

	public WPaintDevice getCreatePaintDevice() {
		return new WRasterPaintDevice(WRasterPaintDevice.Format.PngFormat,
				new WLength(this.widget_.renderWidth_), new WLength(
						this.widget_.renderHeight_));
	}

	public void createContents(DomElement result, WPaintDevice device) {
		String wstr = String.valueOf(this.widget_.renderWidth_);
		String hstr = String.valueOf(this.widget_.renderHeight_);
		DomElement img = DomElement.createNew(DomElementType.DomElement_IMG);
		img.setId('i' + this.widget_.getId());
		img.setAttribute("width", wstr);
		img.setAttribute("height", hstr);
		img.setAttribute("class", "unselectable");
		img.setAttribute("unselectable", "on");
		img.setAttribute("onselectstart", "return false;");
		img.setAttribute("onmousedown", "return false;");
		WRasterPaintDevice rasterDevice = ((device) instanceof WRasterPaintDevice ? (WRasterPaintDevice) (device)
				: null);
		img.setAttribute("src", rasterDevice.generateUrl());
		result.addChild(img);
	}

	public void updateContents(List<DomElement> result, WPaintDevice device) {
		WRasterPaintDevice rasterDevice = ((device) instanceof WRasterPaintDevice ? (WRasterPaintDevice) (device)
				: null);
		DomElement img = DomElement.getForUpdate('i' + this.widget_.getId(),
				DomElementType.DomElement_IMG);
		if (this.widget_.sizeChanged_) {
			img
					.setAttribute("width", String
							.valueOf(this.widget_.renderWidth_));
			img.setAttribute("height", String
					.valueOf(this.widget_.renderHeight_));
			this.widget_.sizeChanged_ = false;
		}
		img.setAttribute("src", rasterDevice.generateUrl());
		result.add(img);
	}

	public WWidgetPainter.RenderType getRenderType() {
		return WWidgetPainter.RenderType.PngImage;
	}
}
