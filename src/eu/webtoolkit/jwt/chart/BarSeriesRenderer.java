package eu.webtoolkit.jwt.chart;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

class BarSeriesRenderer extends SeriesRenderer {
	public BarSeriesRenderer(WChart2DRenderer renderer, WDataSeries series,
			SeriesRenderIterator it, double groupWidth, int numGroups, int group) {
		super(renderer, series, it);
		this.groupWidth_ = groupWidth;
		this.numGroups_ = numGroups;
		this.group_ = group;
	}

	public void addValue(double x, double y, double stacky) {
		WPainterPath bar = new WPainterPath();
		WAxis yAxis = this.renderer_.getChart().axis(this.series_.getAxis());
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
		this.renderer_.getPainter().setPen(this.series_.getPen());
		this.renderer_.getPainter().setBrush(this.series_.getBrush());
		this.renderer_.getPainter().drawPath(bar);
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

	private double groupWidth_;
	private int numGroups_;
	private int group_;
}
