/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WPaintDevice;
import eu.webtoolkit.jwt.WPaintedWidget;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPointF;

class IconWidget extends WPaintedWidget {
	public IconWidget(WCartesianChart chart, int index, WContainerWidget parent) {
		super(parent);
		this.chart_ = chart;
		this.index_ = index;
		this.setInline(true);
		this.resize(new WLength(20), new WLength(20));
	}

	public IconWidget(WCartesianChart chart, int index) {
		this(chart, index, (WContainerWidget) null);
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		WPainter painter = new WPainter(paintDevice);
		this.chart_.renderLegendIcon(painter, new WPointF(2.5, 10.0),
				this.chart_.getSeries(this.index_));
	}

	private WCartesianChart chart_;
	private int index_;
}
