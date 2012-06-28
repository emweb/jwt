/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

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

class MarkerRenderIterator extends SeriesIterator {
	private static Logger logger = LoggerFactory
			.getLogger(MarkerRenderIterator.class);

	public MarkerRenderIterator(WChart2DRenderer renderer) {
		super();
		this.renderer_ = renderer;
		this.marker_ = new WPainterPath();
	}

	public boolean startSeries(WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		this.marker_.assign(new WPainterPath());
		if (series.getMarker() != MarkerType.NoMarker) {
			this.renderer_.getChart().drawMarker(series, this.marker_);
			this.renderer_.getPainter().save();
			this.renderer_.getPainter().setShadow(series.getShadow());
			this.needRestore_ = true;
		} else {
			this.needRestore_ = false;
		}
		return true;
	}

	public void endSeries() {
		if (this.needRestore_) {
			this.renderer_.getPainter().restore();
		}
	}

	public void newValue(WDataSeries series, double x, double y, double stackY,
			WModelIndex xIndex, WModelIndex yIndex) {
		if (!Double.isNaN(x) && !Double.isNaN(y)) {
			WPointF p = this.renderer_.map(x, y, series.getAxis(), this
					.getCurrentXSegment(), this.getCurrentYSegment());
			if (!this.marker_.isEmpty()) {
				WPainter painter = this.renderer_.getPainter();
				painter.save();
				painter.translate(this.hv(p));
				WPen pen = series.getMarkerPen().clone();
				setPenColor(pen, xIndex, yIndex,
						ItemDataRole.MarkerPenColorRole);
				painter.setPen(pen);
				WBrush brush = series.getMarkerBrush().clone();
				setBrushColor(brush, xIndex, yIndex,
						ItemDataRole.MarkerBrushColorRole);
				painter.setBrush(brush);
				this.setMarkerSize(painter, xIndex, yIndex, series
						.getMarkerSize());
				painter.drawPath(this.marker_);
				painter.restore();
			}
			if (series.getType() != SeriesType.BarSeries) {
				Object toolTip = yIndex.getData(ItemDataRole.ToolTipRole);
				if (!(toolTip == null)) {
					WTransform t = this.renderer_.getPainter()
							.getWorldTransform();
					p = t.map(this.hv(p));
					WCircleArea circleArea = new WCircleArea((int) p.getX(),
							(int) p.getY(), 5);
					circleArea.setToolTip(StringUtils.asString(toolTip));
					this.renderer_.getChart().addDataPointArea(series, xIndex,
							circleArea);
				}
			}
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
	private boolean needRestore_;

	private void setMarkerSize(WPainter painter, WModelIndex xIndex,
			WModelIndex yIndex, double markerSize) {
		Object scale = new Object();
		double dScale = 1;
		if ((yIndex != null)) {
			scale = yIndex.getData(ItemDataRole.MarkerScaleFactorRole);
		}
		if ((scale == null) && (xIndex != null)) {
			scale = xIndex.getData(ItemDataRole.MarkerScaleFactorRole);
		}
		if (!(scale == null)) {
			dScale = StringUtils.asNumber(scale);
		}
		dScale = markerSize / 6 * dScale;
		painter.scale(dScale, dScale);
	}
}
