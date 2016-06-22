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
			double stackY, int xRow, int xColumn, int yRow, int yColumn) {
		if (!Double.isNaN(x) && !Double.isNaN(y)) {
			WPointF p = this.chart_.map(x, y, series.getAxis(),
					this.getCurrentXSegment(), this.getCurrentYSegment());
			if (!this.marker_.isEmpty()) {
				this.painter_.save();
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
				this.setMarkerSize(this.painter_, series, xRow, xColumn, yRow,
						yColumn, series.getMarkerSize());
				if (this.chart_.isSeriesSelectionEnabled()
						&& this.chart_.getSelectedSeries() != null
						&& this.chart_.getSelectedSeries() != series) {
					brush.setColor(WCartesianChart.lightenColor(brush
							.getColor()));
				}
				WTransform currentTransform = new WTransform().translate(
						this.chart_.getCombinedTransform().map(this.hv(p)))
						.multiply(this.scale_);
				this.painter_.setWorldTransform(currentTransform, false);
				this.painter_.setShadow(series.getShadow());
				if (series.getMarker() != MarkerType.CrossMarker
						&& series.getMarker() != MarkerType.XCrossMarker
						&& series.getMarker() != MarkerType.AsteriskMarker
						&& series.getMarker() != MarkerType.StarMarker) {
					this.painter_.fillPath(this.marker_, brush);
					this.painter_.setShadow(new WShadow());
				}
				this.painter_.strokePath(this.marker_, pen);
				this.painter_.restore();
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
						circleArea.setRadius(5);
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
	private WTransform scale_;

	private void setMarkerSize(final WPainter painter,
			final WDataSeries series, int xRow, int xColumn, int yRow,
			int yColumn, double markerSize) {
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
		this.scale_.assign(new WTransform(dScale, 0, 0, dScale, 0, 0));
	}
}
