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

import eu.webtoolkit.jwt.StringUtils;
import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.WBrush;
import eu.webtoolkit.jwt.WCircleArea;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WPainter;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPen;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WTransform;

class MarkerRenderIterator extends SeriesIterator {
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
		}
		return true;
	}

	public void endSeries() {
		this.renderer_.getPainter().restore();
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
				Object penColor = yIndex
						.getData(ItemDataRole.MarkerPenColorRole);
				if ((penColor == null) && (xIndex != null)) {
					penColor = xIndex.getData(ItemDataRole.MarkerPenColorRole);
				}
				if (!(penColor == null)) {
					pen.setColor((WColor) penColor);
				}
				painter.setPen(pen);
				WBrush brush = series.getMarkerBrush().clone();
				Object brushColor = yIndex
						.getData(ItemDataRole.MarkerBrushColorRole);
				if ((brushColor == null) && (xIndex != null)) {
					brushColor = xIndex
							.getData(ItemDataRole.MarkerBrushColorRole);
				}
				if (!(brushColor == null)) {
					brush.setColor((WColor) brushColor);
				}
				painter.setBrush(brush);
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
}
