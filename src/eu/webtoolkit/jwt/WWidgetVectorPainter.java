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

class WWidgetVectorPainter extends WWidgetPainter {
	private static Logger logger = LoggerFactory
			.getLogger(WWidgetVectorPainter.class);

	public WWidgetVectorPainter(WPaintedWidget widget,
			WWidgetPainter.RenderType renderType) {
		super(widget);
		this.renderType_ = renderType;
	}

	public WPaintDevice getPaintDevice(boolean paintUpdate) {
		if (this.renderType_ == WWidgetPainter.RenderType.InlineSvg) {
			return new WSvgImage(new WLength(this.widget_.renderWidth_),
					new WLength(this.widget_.renderHeight_), (WObject) null,
					paintUpdate);
		} else {
			return new WVmlImage(new WLength(this.widget_.renderWidth_),
					new WLength(this.widget_.renderHeight_), paintUpdate);
		}
	}

	public void createContents(DomElement canvas, WPaintDevice device) {
		WVectorImage vectorDevice = ((device) instanceof WVectorImage ? (WVectorImage) (device)
				: null);
		canvas.setProperty(Property.PropertyInnerHTML, vectorDevice
				.getRendered());
		;
	}

	public void updateContents(final List<DomElement> result,
			WPaintDevice device) {
		WVectorImage vectorDevice = ((device) instanceof WVectorImage ? (WVectorImage) (device)
				: null);
		if (!EnumUtils.mask(this.widget_.repaintFlags_, PaintFlag.PaintUpdate)
				.isEmpty()) {
			DomElement painter = DomElement.updateGiven("Wt3_3_1.getElement('p"
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
		;
	}

	public WWidgetPainter.RenderType getRenderType() {
		return this.renderType_;
	}

	private WWidgetPainter.RenderType renderType_;
}
