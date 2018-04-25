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
		this.pathFragment_ = new WPainterPath();
		this.currentPen_ = new WPen();
		this.currentBrush_ = new WBrush();
		this.currentScale_ = 0;
		this.series_ = null;
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
		this.finishPathFragment(this.series_);
		this.series_ = null;
		if (this.needRestore_) {
			this.painter_.restore();
		}
	}

	public void newValue(final WDataSeries series, double x, double y,
			double stackY, int xRow, int xColumn, int yRow, int yColumn) {
		if (!Double.isNaN(x) && !Double.isNaN(y)) {
			WPointF p = this.chart_.map(x, y, series.getAxis(),
					this.getCurrentXSegment(), this.getCurrentYSegment());
			if (!this.marker_.isEmpty()) {
				WPen pen = series.getMarkerPen().clone();
				SeriesIterator.setPenColor(pen, series, xRow, xColumn, yRow,
						yColumn, ItemDataRole.MarkerPenColorRole);
				if (this.chart_.isSeriesSelectionEnabled()
						&& this.chart_.getSelectedSeries() != null
						&& this.chart_.getSelectedSeries() != series) {
					pen.setColor(WCartesianChart.lightenColor(pen.getColor()));
				}
				WBrush brush = series.getMarkerBrush().clone();
				SeriesIterator.setBrushColor(brush, series, xRow, xColumn,
						yRow, yColumn, ItemDataRole.MarkerBrushColorRole);
				double scale = this.calculateMarkerScale(series, xRow, xColumn,
						yRow, yColumn, series.getMarkerSize());
				if (this.chart_.isSeriesSelectionEnabled()
						&& this.chart_.getSelectedSeries() != null
						&& this.chart_.getSelectedSeries() != series) {
					brush.setColor(WCartesianChart.lightenColor(brush
							.getColor()));
				}
				if (!(this.series_ != null)
						|| !brush.equals(this.currentBrush_)
						|| !pen.equals(this.currentPen_)
						|| scale != this.currentScale_) {
					if (this.series_ != null) {
						this.finishPathFragment(this.series_);
					}
					this.series_ = series;
					this.currentBrush_ = brush;
					this.currentPen_ = pen;
					this.currentScale_ = scale;
				}
				this.pathFragment_.moveTo(this.hv(p));
			}
			if (series.getType() != SeriesType.BarSeries) {
				WString toolTip = series.getModel().getToolTip(yRow, yColumn);
				if (!(toolTip.length() == 0)) {
					if (!(!EnumUtils.mask(
							series.getModel().flags(yRow, yColumn),
							ItemFlag.ItemHasDeferredTooltip).isEmpty() || !EnumUtils
							.mask(series.getModel().flags(yRow, yColumn),
									ItemFlag.ItemIsXHTMLText).isEmpty())) {
						WTransform t = this.painter_.getWorldTransform();
						p = t.map(this.hv(p));
						WCircleArea circleArea = new WCircleArea();
						circleArea.setCenter(new WPointF(p.getX(), p.getY()));
						Double scaleFactorP = series.getModel()
								.getMarkerScaleFactor(yRow, yColumn);
						double scaleFactor = scaleFactorP != null ? scaleFactorP
								: 1.0;
						if (scaleFactor < 1.0) {
							scaleFactor = 1.0;
						}
						circleArea.setRadius((int) (scaleFactor * 5.0));
						circleArea.setToolTip(toolTip);
						this.chart_.addDataPointArea(series, xRow, xColumn,
								circleArea);
					} else {
						this.chart_.hasDeferredToolTips_ = true;
					}
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
	private WPainterPath pathFragment_;
	private WPen currentPen_;
	private WBrush currentBrush_;
	private double currentScale_;
	private WDataSeries series_;

	private double calculateMarkerScale(final WDataSeries series, int xRow,
			int xColumn, int yRow, int yColumn, double markerSize) {
		Double scale = null;
		double dScale = 1;
		if (yRow >= 0 && yColumn >= 0) {
			scale = series.getModel().getMarkerScaleFactor(yRow, yColumn);
		}
		if (!(scale != null) && xRow >= 0 && xColumn >= 0) {
			scale = series.getModel().getMarkerScaleFactor(xRow, xColumn);
		}
		if (scale != null) {
			dScale = scale;
		}
		dScale = markerSize / 6 * dScale;
		return dScale;
	}

	private void finishPathFragment(final WDataSeries series) {
		if (this.pathFragment_.getSegments().isEmpty()) {
			return;
		}
		this.painter_.save();
		this.painter_.setWorldTransform(new WTransform(this.currentScale_, 0,
				0, this.currentScale_, 0, 0));
		WTransform currentTransform = new WTransform(1.0 / this.currentScale_,
				0, 0, 1.0 / this.currentScale_, 0, 0).multiply(this.chart_
				.zoomRangeTransform(series.getYAxis()));
		this.painter_.setPen(new WPen(PenStyle.NoPen));
		this.painter_.setBrush(new WBrush(BrushStyle.NoBrush));
		this.painter_.setShadow(series.getShadow());
		if (series.getMarker() != MarkerType.CrossMarker
				&& series.getMarker() != MarkerType.XCrossMarker
				&& series.getMarker() != MarkerType.AsteriskMarker
				&& series.getMarker() != MarkerType.StarMarker) {
			this.painter_.setBrush(this.currentBrush_);
			if (!series.getShadow().isNone()) {
				this.painter_.drawStencilAlongPath(this.marker_,
						currentTransform.map(this.pathFragment_), false);
			}
			this.painter_.setShadow(new WShadow());
		}
		this.painter_.setPen(this.currentPen_);
		if (!series.getShadow().isNone()) {
			this.painter_.setBrush(new WBrush(BrushStyle.NoBrush));
		}
		this.painter_.drawStencilAlongPath(this.marker_,
				currentTransform.map(this.pathFragment_), false);
		this.painter_.restore();
		this.pathFragment_.assign(new WPainterPath());
	}
}
