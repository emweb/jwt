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

class LabelRenderIterator extends SeriesIterator {
	private static Logger logger = LoggerFactory
			.getLogger(LabelRenderIterator.class);

	public LabelRenderIterator(final WCartesianChart chart,
			final WPainter painter) {
		super();
		this.chart_ = chart;
		this.painter_ = painter;
	}

	public boolean startSeries(final WDataSeries series, double groupWidth,
			int numBarGroups, int currentBarGroup) {
		if (series.isLabelsEnabled(Axis.XAxis)
				|| series.isLabelsEnabled(Axis.YAxis)) {
			this.groupWidth_ = groupWidth;
			this.numGroups_ = numBarGroups;
			this.group_ = currentBarGroup;
			return true;
		} else {
			return false;
		}
	}

	public void newValue(final WDataSeries series, double x, double y,
			double stackY, final WModelIndex xIndex, final WModelIndex yIndex) {
		if (Double.isNaN(x) || Double.isNaN(y)) {
			return;
		}
		WString text = new WString();
		if (series.isLabelsEnabled(Axis.XAxis)) {
			text = this.chart_.getAxis(Axis.XAxis).getLabel(x);
		}
		if (series.isLabelsEnabled(Axis.YAxis)) {
			if (!(text.length() == 0)) {
				text.append(": ");
			}
			text.append(this.chart_.getAxis(series.getAxis()).getLabel(
					y - stackY));
		}
		if (!(text.length() == 0)) {
			WPointF p = this.chart_.map(x, y, series.getAxis(), this
					.getCurrentXSegment(), this.getCurrentYSegment());
			if (series.getType() == SeriesType.BarSeries) {
				double g = this.numGroups_ + (this.numGroups_ - 1)
						* this.chart_.getBarMargin();
				double width = this.groupWidth_ / g;
				double left = p.getX() - this.groupWidth_ / 2 + this.group_
						* width * (1 + this.chart_.getBarMargin());
				p = new WPointF(left + width / 2, p.getY());
			}
			EnumSet<AlignmentFlag> alignment = EnumSet
					.noneOf(AlignmentFlag.class);
			if (series.getType() == SeriesType.BarSeries) {
				if (y < 0) {
					alignment = EnumSet.copyOf(EnumSet.of(
							AlignmentFlag.AlignCenter,
							AlignmentFlag.AlignBottom));
				} else {
					alignment = EnumSet.copyOf(EnumSet.of(
							AlignmentFlag.AlignCenter, AlignmentFlag.AlignTop));
				}
			} else {
				alignment = EnumSet.copyOf(EnumSet.of(
						AlignmentFlag.AlignCenter, AlignmentFlag.AlignBottom));
				p.setY(p.getY() - 3);
			}
			final WCartesianChart chart = this.chart_;
			WPen oldPen = chart.textPen_.clone();
			chart.textPen_.setColor(series.getLabelColor());
			WTransform t = new WTransform(1, 0, 0, -1, chart.chartArea_
					.getLeft(), chart.chartArea_.getBottom()).multiply(
					chart.xTransform_.getValue()).multiply(
					chart.yTransform_.getValue()).multiply(
					new WTransform(1, 0, 0, -1, -chart.chartArea_.getLeft(),
							chart.chartArea_.getBottom()));
			chart.renderLabel(this.painter_, text, t.map(p), alignment, 0, 3);
			chart.textPen_ = oldPen;
		}
	}

	private final WCartesianChart chart_;
	private final WPainter painter_;
	private double groupWidth_;
	private int numGroups_;
	private int group_;
}
