/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPointF;

class MarkerRenderIterator extends SeriesIterator {
	public MarkerRenderIterator(WChart2DRenderer renderer) {
		super();
		this.renderer_ = renderer;
		this.marker_ = new WPainterPath();
	}

	public boolean startSeries(WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		if (series.getMarker() != MarkerType.NoMarker) {
			this.marker_.assign(new WPainterPath());
			this.renderer_.getChart().drawMarker(series, this.marker_);
			return true;
		} else {
			return false;
		}
	}

	public void newValue(WDataSeries series, double x, double y, double stackY) {
		if (!Double.isNaN(x) && !Double.isNaN(y) && !this.marker_.isEmpty()) {
			WPointF p = this.renderer_.map(x, y, series.getAxis(), this
					.getCurrentXSegment(), this.getCurrentYSegment());
			WPainter painter = this.renderer_.getPainter();
			painter.save();
			painter.translate(this.hv(p));
			painter.setPen(series.getMarkerPen());
			painter.setBrush(series.getMarkerBrush());
			painter.drawPath(this.marker_);
			painter.restore();
		}
	}

	public WPointF hv(WPointF p) {
		return this.renderer_.hv(p);
	}

	public WPointF hv(double x, double y) {
		return this.renderer_.hv(x, y);
	}

	private WChart2DRenderer renderer_;
	private WPainterPath marker_;
}
