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

	public MarkerRenderIterator(final WCartesianChart chart,
			final WPainter painter) {
		super();
		this.chart_ = chart;
		this.painter_ = painter;
		this.marker_ = new WPainterPath();
		this.scale_ = new WTransform();
	}

	public boolean startSeries(final WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		this.marker_.assign(new WPainterPath());
		if (series.getMarker() != MarkerType.NoMarker) {
			this.chart_.drawMarker(series, this.marker_);
			this.painter_.save();
			this.needRestore_ = true;
		} else {
			this.needRestore_ = false;
		}
		return true;
	}

	public void endSeries() {
		if (this.needRestore_) {
			this.painter_.restore();
		}
	}

	public void newValue(final WDataSeries series, double x, double y,
			double stackY, final WModelIndex xIndex, final WModelIndex yIndex) {
		if (!Double.isNaN(x) && !Double.isNaN(y)) {
			WPointF p = this.chart_.map(x, y, series.getAxis(),
					this.getCurrentXSegment(), this.getCurrentYSegment());
			final WCartesianChart chart = this.chart_;
			if (!this.marker_.isEmpty()) {
				this.painter_.save();
				WPen pen = series.getMarkerPen().clone();
				setPenColor(pen, xIndex, yIndex,
						ItemDataRole.MarkerPenColorRole);
				WBrush brush = series.getMarkerBrush().clone();
				setBrushColor(brush, xIndex, yIndex,
						ItemDataRole.MarkerBrushColorRole);
				this.setMarkerSize(this.painter_, xIndex, yIndex,
						series.getMarkerSize());
				WTransform currentTransform = new WTransform().translate(
						chart.getCombinedTransform().map(this.hv(p))).multiply(
						this.scale_);
				this.painter_.setWorldTransform(currentTransform, false);
				this.painter_.setShadow(series.getShadow());
				if (series.getMarker() != MarkerType.CrossMarker
						&& series.getMarker() != MarkerType.XCrossMarker) {
					this.painter_.fillPath(this.marker_, brush);
					this.painter_.setShadow(new WShadow());
				}
				this.painter_.strokePath(this.marker_, pen);
				this.painter_.restore();
			}
			if (series.getType() != SeriesType.BarSeries) {
				Object toolTip = yIndex.getData(ItemDataRole.ToolTipRole);
				if (!(toolTip == null)) {
					WTransform t = this.painter_.getWorldTransform();
					p = t.map(this.hv(p));
					WCircleArea circleArea = new WCircleArea();
					circleArea.setCenter(new WPointF(p.getX(), p.getY()));
					circleArea.setRadius(5);
					circleArea.setToolTip(StringUtils.asString(toolTip));
					this.chart_.addDataPointArea(series, xIndex, circleArea);
				}
			}
		}
	}

	public WPointF hv(final WPointF p) {
		return this.chart_.hv(p);
	}

	public WPointF hv(double x, double y) {
		return this.chart_.hv(x, y);
	}

	private final WCartesianChart chart_;
	private final WPainter painter_;
	private WPainterPath marker_;
	private boolean needRestore_;
	private WTransform scale_;

	private void setMarkerSize(final WPainter painter,
			final WModelIndex xIndex, final WModelIndex yIndex,
			double markerSize) {
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
		this.scale_.assign(new WTransform(dScale, 0, 0, dScale, 0, 0));
	}
}
