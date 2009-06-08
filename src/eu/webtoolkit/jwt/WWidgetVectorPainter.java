package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

class WWidgetVectorPainter extends WWidgetPainter {
	public WWidgetVectorPainter(WPaintedWidget widget, VectorFormat format) {
		super(widget);
		this.format_ = format;
	}

	public WPaintDevice getCreatePaintDevice() {
		if (this.format_ == VectorFormat.SvgFormat) {
			return new WSvgImage(this.widget_.getWidth(), this.widget_
					.getHeight());
		} else {
			return new WVmlImage(this.widget_.getWidth(), this.widget_
					.getHeight());
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
			DomElement painter = DomElement.updateGiven(
					"Wt2_99_2.getElement('p" + this.widget_.getFormName()
							+ "').firstChild", DomElementType.DomElement_DIV);
			painter.setProperty(Property.PropertyAddedInnerHTML, vectorDevice
					.getRendered());
			WApplication app = WApplication.instance();
			if (app.getEnvironment().agentIsOpera()) {
				painter.callMethod("forceRedraw();");
			}
			result.add(painter);
		} else {
			DomElement canvas = DomElement.getForUpdate('p' + this.widget_
					.getFormName(), DomElementType.DomElement_DIV);
			canvas.setProperty(Property.PropertyInnerHTML, vectorDevice
					.getRendered());
			result.add(canvas);
		}
		this.widget_.sizeChanged_ = false;
	}

	private VectorFormat format_;
}
