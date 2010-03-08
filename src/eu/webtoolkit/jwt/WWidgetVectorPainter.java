/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

class WWidgetVectorPainter extends WWidgetPainter {
	public WWidgetVectorPainter(WPaintedWidget widget, VectorFormat format) {
		super(widget);
		this.format_ = format;
	}

	public WPaintDevice getCreatePaintDevice() {
		if (this.format_ == VectorFormat.SvgFormat) {
			return new WSvgImage(new WLength(this.widget_.renderWidth_),
					new WLength(this.widget_.renderHeight_));
		} else {
			return new WVmlImage(new WLength(this.widget_.renderWidth_),
					new WLength(this.widget_.renderHeight_));
		}
	}

	public void createContents(DomElement canvas, WPaintDevice device) {
		WVectorImage vectorDevice = ((device) instanceof WVectorImage ? (WVectorImage) (device)
				: null);
		canvas.setProperty(Property.PropertyInnerHTML, vectorDevice
				.getRendered());
	}

	public void updateContents(List<DomElement> result, WPaintDevice device) {
		WVectorImage vectorDevice = ((device) instanceof WVectorImage ? (WVectorImage) (device)
				: null);
		if (!EnumUtils.mask(device.getPaintFlags(), PaintFlag.PaintUpdate)
				.isEmpty()) {
			DomElement painter = DomElement.updateGiven("Wt3_1_1.getElement('p"
					+ this.widget_.getId() + "').firstChild",
					DomElementType.DomElement_DIV);
			painter.setProperty(Property.PropertyAddedInnerHTML, vectorDevice
					.getRendered());
			WApplication app = WApplication.getInstance();
			if (app.getEnvironment().agentIsOpera()) {
				painter.callMethod("forceRedraw();");
			}
			result.add(painter);
		} else {
			DomElement canvas = DomElement.getForUpdate('p' + this.widget_
					.getId(), DomElementType.DomElement_DIV);
			canvas.setProperty(Property.PropertyInnerHTML, vectorDevice
					.getRendered());
			result.add(canvas);
		}
		this.widget_.sizeChanged_ = false;
	}

	private VectorFormat format_;
}
