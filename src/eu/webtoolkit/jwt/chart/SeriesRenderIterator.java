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
package eu.webtoolkit.jwt.chart;

import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WRectF;

class SeriesRenderIterator extends SeriesIterator {
	public SeriesRenderIterator(WChart2DRenderer renderer) {
		super();
		this.renderer_ = renderer;
		this.series_ = null;
	}

	public void startSegment(int currentXSegment, int currentYSegment,
			WRectF currentSegmentArea) {
		super
				.startSegment(currentXSegment, currentYSegment,
						currentSegmentArea);
		WAxis yAxis = this.renderer_.getChart().getAxis(this.series_.getAxis());
		if (currentYSegment == 0) {
			this.maxY_ = Double.MAX_VALUE;
		} else {
			this.maxY_ = currentSegmentArea.getBottom();
		}
		if (currentYSegment == yAxis.getSegmentCount() - 1) {
			this.minY_ = -Double.MAX_VALUE;
		} else {
			this.minY_ = currentSegmentArea.getTop();
		}
	}

	public void endSegment() {
		super.endSegment();
		this.seriesRenderer_.paint();
	}

	public boolean startSeries(WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		this.seriesRenderer_ = null;
		switch (series.getType()) {
		case LineSeries:
		case CurveSeries:
			this.seriesRenderer_ = new LineSeriesRenderer(this.renderer_,
					series, this);
			break;
		case BarSeries:
			this.seriesRenderer_ = new BarSeriesRenderer(this.renderer_,
					series, this, groupWidth, numBarGroups, currentBarGroup);
		default:
			break;
		}
		this.series_ = series;
		this.renderer_.getPainter().save();
		return this.seriesRenderer_ != null;
	}

	public void endSeries() {
		this.seriesRenderer_.paint();
		this.renderer_.getPainter().restore();
		;
		this.series_ = null;
	}

	public void newValue(WDataSeries series, double x, double y, double stackY,
			WModelIndex xIndex, WModelIndex yIndex) {
		if (Double.isNaN(x) || Double.isNaN(y)) {
			this.seriesRenderer_.paint();
		} else {
			this.seriesRenderer_.addValue(x, y, stackY, xIndex, yIndex);
		}
	}

	public double breakY(double y) {
		if (y < this.minY_) {
			return this.minY_;
		} else {
			if (y > this.maxY_) {
				return this.maxY_;
			} else {
				return y;
			}
		}
	}

	private WChart2DRenderer renderer_;
	private WDataSeries series_;
	private SeriesRenderer seriesRenderer_;
	private double minY_;
	private double maxY_;
}
