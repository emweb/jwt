/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
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
		this.device_ = null;
	}

	public WPaintDevice getPaintDevice() {
		if (!(this.device_ != null)) {
			this.device_ = new WRasterPaintDevice("png", new WLength(
					this.widget_.renderWidth_), new WLength(
					this.widget_.renderHeight_));
		}
		return this.device_;
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
		WResource resource = ((device) instanceof WResource ? (WResource) (device)
				: null);
		img.setAttribute("src", resource.generateUrl());
		result.addChild(img);
	}

	public void updateContents(List<DomElement> result, WPaintDevice device) {
		WResource resource = ((device) instanceof WResource ? (WResource) (device)
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
		img.setAttribute("src", resource.generateUrl());
		result.add(img);
	}

	public WWidgetPainter.RenderType getRenderType() {
		return WWidgetPainter.RenderType.PngImage;
	}

	private WPaintDevice device_;
}
