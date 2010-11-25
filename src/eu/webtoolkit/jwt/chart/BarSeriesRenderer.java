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

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.PenStyle;
import eu.webtoolkit.jwt.StringUtils;
import eu.webtoolkit.jwt.WAbstractArea;
import eu.webtoolkit.jwt.WModelIndex;
import eu.webtoolkit.jwt.WPainterPath;
import eu.webtoolkit.jwt.WPen;
import eu.webtoolkit.jwt.WPointF;
import eu.webtoolkit.jwt.WPolygonArea;
import eu.webtoolkit.jwt.WRectArea;
import eu.webtoolkit.jwt.WShadow;
import eu.webtoolkit.jwt.WTransform;

class BarSeriesRenderer extends SeriesRenderer {
	public BarSeriesRenderer(WChart2DRenderer renderer, WDataSeries series,
			SeriesRenderIterator it, double groupWidth, int numGroups, int group) {
		super(renderer, series, it);
		this.groupWidth_ = groupWidth;
		this.numGroups_ = numGroups;
		this.group_ = group;
	}

	public void addValue(double x, double y, double stacky, WModelIndex xIndex,
			WModelIndex yIndex) {
		WPainterPath bar = new WPainterPath();
		WAxis yAxis = this.renderer_.getChart().getAxis(this.series_.getAxis());
		WPointF topMid = this.renderer_.map(x, y, yAxis.getId(), this.it_
				.getCurrentXSegment(), this.it_.getCurrentYSegment());
		WPointF bottomMid = this.renderer_.map(x, stacky, yAxis.getId(),
				this.it_.getCurrentXSegment(), this.it_.getCurrentYSegment());
		double g = this.numGroups_ + (this.numGroups_ - 1)
				* this.renderer_.getChart().getBarMargin();
		double width = this.groupWidth_ / g;
		double left = topMid.getX() - this.groupWidth_ / 2 + this.group_
				* width * (1 + this.renderer_.getChart().getBarMargin());
		bar.moveTo(this.hv(crisp(left), crisp(topMid.getY())));
		bar.lineTo(this.hv(crisp(left + width), crisp(topMid.getY())));
		bar.lineTo(this.hv(crisp(left + width), crisp(bottomMid.getY())));
		bar.lineTo(this.hv(crisp(left), crisp(bottomMid.getY())));
		bar.closeSubPath();
		this.renderer_.getPainter().setShadow(this.series_.getShadow());
		this.renderer_.getPainter().fillPath(bar, this.series_.getBrush());
		this.renderer_.getPainter().setShadow(new WShadow());
		this.renderer_.getPainter().strokePath(bar, this.series_.getPen());
		Object toolTip = yIndex.getData(ItemDataRole.ToolTipRole);
		if (!(toolTip == null)) {
			WTransform t = this.renderer_.getPainter().getWorldTransform();
			WPointF tl = t.map(segmentPoint(bar, 0));
			WPointF tr = t.map(segmentPoint(bar, 1));
			WPointF br = t.map(segmentPoint(bar, 2));
			WPointF bl = t.map(segmentPoint(bar, 3));
			double tlx = 0;
			double tly = 0;
			double brx = 0;
			double bry = 0;
			boolean useRect = false;
			if (fequal(tl.getY(), tr.getY())) {
				tlx = Math.min(tl.getX(), tr.getX());
				brx = Math.max(tl.getX(), tr.getX());
				tly = Math.min(tl.getY(), bl.getY());
				bry = Math.max(tl.getY(), br.getY());
				useRect = true;
			} else {
				if (fequal(tl.getX(), tr.getX())) {
					tlx = Math.min(tl.getX(), bl.getX());
					brx = Math.max(tl.getX(), bl.getX());
					tly = Math.min(tl.getY(), tr.getY());
					bry = Math.max(tl.getY(), tr.getY());
					useRect = true;
				}
			}
			WAbstractArea area;
			if (useRect) {
				area = new WRectArea(tlx, tly, brx - tlx, bry - tly);
			} else {
				WPolygonArea poly = new WPolygonArea();
				poly.addPoint(tl.getX(), tl.getY());
				poly.addPoint(tr.getX(), tr.getY());
				poly.addPoint(br.getX(), br.getY());
				poly.addPoint(bl.getX(), bl.getY());
				area = poly;
			}
			area.setToolTip(StringUtils.asString(toolTip));
			this.renderer_.getChart().addDataPointArea(this.series_, xIndex,
					area);
		}
		double bTopMidY = this.it_.breakY(topMid.getY());
		double bBottomMidY = this.it_.breakY(bottomMid.getY());
		if (bTopMidY > topMid.getY() && bBottomMidY <= bottomMid.getY()) {
			WPainterPath breakPath = new WPainterPath();
			breakPath.moveTo(this.hv(left - 10, bTopMidY + 10));
			breakPath.lineTo(this.hv(left + width + 10, bTopMidY + 1));
			breakPath.lineTo(this.hv(left + width + 10, bTopMidY - 1));
			breakPath.lineTo(this.hv(left - 10, bTopMidY - 1));
			this.renderer_.getPainter().setPen(new WPen(PenStyle.NoPen));
			this.renderer_.getPainter().setBrush(
					this.renderer_.getChart().getBackground());
			this.renderer_.getPainter().drawPath(breakPath);
			this.renderer_.getPainter().setPen(new WPen());
			this.renderer_.getPainter().drawLine(
					this.hv(left - 10, bTopMidY + 10),
					this.hv(left + width + 10, bTopMidY + 1));
		}
		if (bBottomMidY < bottomMid.getY() && bTopMidY >= topMid.getY()) {
			WPainterPath breakPath = new WPainterPath();
			breakPath.moveTo(this.hv(left + width + 10, bBottomMidY - 10));
			breakPath.lineTo(this.hv(left - 10, bBottomMidY - 1));
			breakPath.lineTo(this.hv(left - 10, bBottomMidY + 1));
			breakPath.lineTo(this.hv(left + width + 10, bBottomMidY + 1));
			this.renderer_.getPainter().setBrush(
					this.renderer_.getChart().getBackground());
			this.renderer_.getPainter().setPen(new WPen(PenStyle.NoPen));
			this.renderer_.getPainter().drawPath(breakPath);
			this.renderer_.getPainter().setPen(new WPen());
			this.renderer_.getPainter().drawLine(
					this.hv(left - 10, bBottomMidY - 1),
					this.hv(left + width + 10, bBottomMidY - 10));
		}
	}

	public void paint() {
	}

	private static WPointF segmentPoint(WPainterPath path, int segment) {
		WPainterPath.Segment s = path.getSegments().get(segment);
		return new WPointF(s.getX(), s.getY());
	}

	private static boolean fequal(double d1, double d2) {
		return Math.abs(d1 - d2) < 1E-5;
	}

	private double groupWidth_;
	private int numGroups_;
	private int group_;
}
