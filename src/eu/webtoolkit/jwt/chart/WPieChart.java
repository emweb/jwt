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

/**
 * A pie chart.
 * <p>
 * 
 * A pie chart renders a single data series as segments of a circle, so that the
 * area of each segment is proportional to the value in the data series.
 * <p>
 * To use a pie chart, you need to set a model using
 * {@link WAbstractChart#setModel(WAbstractItemModel model)
 * WAbstractChart#setModel()}, and use
 * {@link WPieChart#setLabelsColumn(int modelColumn) setLabelsColumn()} and
 * {@link WPieChart#setDataColumn(int modelColumn) setDataColumn()} to specify
 * the model column that contains the category labels and data.
 * <p>
 * The pie chart may be customized visually by enabling a 3D effect (
 * {@link WPieChart#setPerspectiveEnabled(boolean enabled, double height)
 * setPerspectiveEnabled()}), or by specifying the angle of the first segment.
 * One or more segments may be exploded, which seperates the segment from the
 * rest of the pie chart, using
 * {@link WPieChart#setExplode(int modelRow, double factor) setExplode()}.
 * <p>
 * The segments may be labeled in various ways using
 * {@link WPieChart#setDisplayLabels(EnumSet options) setDisplayLabels()}.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * Styling through CSS is not applicable.
 * <p>
 * <div align="center"> <img src="doc-files//ChartWPieChart-1.png"
 * alt="Example of a pie chart">
 * <p>
 * <strong>Example of a pie chart</strong>
 * </p>
 * </div>
 * <p>
 * 
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
 */
public class WPieChart extends WAbstractChart {
	private static Logger logger = LoggerFactory.getLogger(WPieChart.class);

	/**
	 * Creates a new pie chart.
	 */
	public WPieChart(WContainerWidget parent) {
		super(parent);
		this.labelsColumn_ = -1;
		this.dataColumn_ = -1;
		this.height_ = 0.0;
		this.startAngle_ = 45;
		this.avoidLabelRendering_ = 0.0;
		this.labelOptions_ = EnumSet.noneOf(LabelOption.class);
		this.shadow_ = false;
		this.pie_ = new ArrayList<WPieChart.PieData>();
		this.setPalette(new WStandardPalette(WStandardPalette.Flavour.Neutral));
		this.setPreferredMethod(WPaintedWidget.Method.InlineSvgVml);
		this.setPlotAreaPadding(5);
	}

	/**
	 * Creates a new pie chart.
	 * <p>
	 * Calls {@link #WPieChart(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WPieChart() {
		this((WContainerWidget) null);
	}

	/**
	 * Sets the model column that holds the labels.
	 * <p>
	 * The labels are used only when
	 * {@link WPieChart#setDisplayLabels(EnumSet options) setDisplayLabels()} is
	 * called with the {@link LabelOption#TextLabel TextLabel} option.
	 * <p>
	 * The default value is -1 (not defined).
	 * <p>
	 * 
	 * @see WAbstractChart#setModel(WAbstractItemModel model)
	 * @see WPieChart#setDisplayLabels(EnumSet options)
	 * @see WPieChart#setDataColumn(int modelColumn)
	 */
	public void setLabelsColumn(int modelColumn) {
		if (this.labelsColumn_ != modelColumn) {
			this.labelsColumn_ = modelColumn;
			this.update();
		}
	}

	/**
	 * Returns the model column used for the labels.
	 * <p>
	 * 
	 * @see WPieChart#setLabelsColumn(int modelColumn)
	 */
	public int getLabelsColumn() {
		return this.labelsColumn_;
	}

	/**
	 * Sets the model column that holds the data.
	 * <p>
	 * The data column should contain data that can be converted to a number,
	 * but should not necessarily be of a number type, see also
	 * {@link StringUtils#asNumber(Object)}.
	 * <p>
	 * The default value is -1 (not defined).
	 * <p>
	 * 
	 * @see WAbstractChart#setModel(WAbstractItemModel model)
	 * @see WPieChart#setLabelsColumn(int modelColumn)
	 */
	public void setDataColumn(int modelColumn) {
		if (this.dataColumn_ != modelColumn) {
			this.dataColumn_ = modelColumn;
			this.update();
		}
	}

	/**
	 * Returns the model column used for the data.
	 * <p>
	 * 
	 * @see WPieChart#setDataColumn(int modelColumn)
	 */
	public int getDataColumn() {
		return this.dataColumn_;
	}

	/**
	 * Customizes the brush used for a pie segment.
	 * <p>
	 * By default, the brush is taken from the
	 * {@link WAbstractChart#getPalette() WAbstractChart#getPalette()}. You can
	 * use this method to override the palette&apos;s brush for a particular
	 * <i>modelRow</i>.
	 * <p>
	 * 
	 * @see WAbstractChart#setPalette(WChartPalette palette)
	 */
	public void setBrush(int modelRow, WBrush brush) {
		this.pie_.get(modelRow).customBrush = true;
		this.pie_.get(modelRow).brush = brush;
		this.update();
	}

	/**
	 * Returns the brush used for a pie segment.
	 * <p>
	 * 
	 * @see WPieChart#setBrush(int modelRow, WBrush brush)
	 */
	public WBrush getBrush(int modelRow) {
		if (this.pie_.get(modelRow).customBrush) {
			return this.pie_.get(modelRow).brush;
		} else {
			return this.getPalette().getBrush(modelRow);
		}
	}

	/**
	 * Sets the explosion factor for a pie segment.
	 * <p>
	 * Separates the segment corresponding to model row <i>modelRow</i> from the
	 * rest of the pie. The <i>factor</i> is a positive number that represents
	 * the distance from the center as a fraction of the pie radius. Thus, 0
	 * corresponds to no separation, and 0.1 to a 10% separation, and 1 to a
	 * separation where the segment tip is on the outer perimeter of the pie.
	 * <p>
	 * The default value is 0.
	 */
	public void setExplode(int modelRow, double factor) {
		this.pie_.get(modelRow).explode = factor;
		this.update();
	}

	/**
	 * Returns the explosion factor for a segment.
	 * <p>
	 * 
	 * @see WPieChart#setExplode(int modelRow, double factor)
	 */
	public double getExplode(int modelRow) {
		return this.pie_.get(modelRow).explode;
	}

	/**
	 * Enables a 3D perspective effect on the pie.
	 * <p>
	 * A 3D perspective effect is added, which may be customized by specifying
	 * the simulated <i>height</i> of the pie. The height is defined as a
	 * fraction of the pie radius.
	 * <p>
	 * The default value is false.
	 */
	public void setPerspectiveEnabled(boolean enabled, double height) {
		if (!enabled && this.height_ != 0.0 || this.height_ != height) {
			this.height_ = height;
			this.update();
		}
	}

	/**
	 * Enables a 3D perspective effect on the pie.
	 * <p>
	 * Calls {@link #setPerspectiveEnabled(boolean enabled, double height)
	 * setPerspectiveEnabled(enabled, 1.0)}
	 */
	public final void setPerspectiveEnabled(boolean enabled) {
		setPerspectiveEnabled(enabled, 1.0);
	}

	/**
	 * Returns whether a 3D effect is enabled.
	 * <p>
	 * 
	 * @see WPieChart#setPerspectiveEnabled(boolean enabled, double height)
	 */
	public boolean isPerspectiveEnabled() {
		return this.height_ > 0.0;
	}

	/**
	 * Enables a shadow effect.
	 * <p>
	 * A soft shadow effect is added.
	 * <p>
	 * The default value is false.
	 */
	public void setShadowEnabled(boolean enabled) {
		if (this.shadow_ != enabled) {
			this.shadow_ = enabled;
			this.update();
		}
	}

	/**
	 * Returns whether a shadow effect is enabled.
	 * <p>
	 * 
	 * @see WPieChart#setShadowEnabled(boolean enabled)
	 */
	public boolean isShadowEnabled() {
		return this.shadow_;
	}

	/**
	 * Sets the angle of the first segment.
	 * <p>
	 * The default value is 45 degrees.
	 */
	public void setStartAngle(double startAngle) {
		if (this.startAngle_ != startAngle) {
			this.startAngle_ = startAngle;
			this.update();
		}
	}

	/**
	 * Returns the angle of the first segment.
	 * <p>
	 * 
	 * @see WPieChart#setStartAngle(double startAngle)
	 */
	public double getStartAngle() {
		return this.startAngle_;
	}

	/**
	 * Sets the percentage value to avoid rendering of label texts.
	 * <p>
	 * The default value is 0 percent.
	 */
	public void setAvoidLabelRendering(double avoidLabelRendering) {
		if (this.avoidLabelRendering_ != avoidLabelRendering) {
			this.avoidLabelRendering_ = avoidLabelRendering;
			this.update();
		}
	}

	/**
	 * Returns the percentage to avoid label rendering.
	 * <p>
	 * 
	 * @see WPieChart#setAvoidLabelRendering(double avoidLabelRendering)
	 */
	public double getAvoidLabelRendering() {
		return this.avoidLabelRendering_;
	}

	/**
	 * Configures if and how labels should be displayed.
	 * <p>
	 * The <i>options</i> must be the logical OR of a placement option (
	 * {@link LabelOption#Inside Inside} or {@link LabelOption#Outside Outside})
	 * and {@link LabelOption#TextLabel TextLabel} and/or
	 * {@link LabelOption#TextPercentage TextPercentage}. If both TextLabel and
	 * TextPercentage are specified, then these are combined as
	 * &quot;&lt;label&gt;: &lt;percentage&gt;&quot;.
	 * <p>
	 * The default value is {@link LabelOption#NoLabels NoLabels}.
	 */
	public void setDisplayLabels(EnumSet<LabelOption> options) {
		this.labelOptions_ = EnumSet.copyOf(options);
		this.update();
	}

	/**
	 * Configures if and how labels should be displayed.
	 * <p>
	 * Calls {@link #setDisplayLabels(EnumSet options)
	 * setDisplayLabels(EnumSet.of(option, options))}
	 */
	public final void setDisplayLabels(LabelOption option,
			LabelOption... options) {
		setDisplayLabels(EnumSet.of(option, options));
	}

	/**
	 * Returns options set for displaying labels.
	 * <p>
	 * 
	 * @see WPieChart#setDisplayLabels(EnumSet options)
	 */
	public EnumSet<LabelOption> getDisplayLabels() {
		return this.labelOptions_;
	}

	/**
	 * Creates a widget which renders the a legend item.
	 * <p>
	 * Depending on the passed LabelOption flags, the legend item widget, will
	 * contain a text (with or without the percentage) and/or a span with the
	 * segment&apos;s color.
	 */
	public WWidget createLegendItemWidget(int index,
			EnumSet<LabelOption> options) {
		WContainerWidget legendItem = new WContainerWidget();
		legendItem.setPadding(new WLength(4));
		WText colorText = new WText();
		legendItem.addWidget(colorText);
		colorText
				.setPadding(new WLength(10), EnumSet.of(Side.Left, Side.Right));
		colorText.getDecorationStyle().setBackgroundColor(
				this.getBrush(index).getColor());
		if (WApplication.getInstance().getEnvironment().agentIsIE()) {
			colorText.setAttributeValue("style", "zoom: 1;");
		}
		double total = 0;
		if (this.dataColumn_ != -1) {
			for (int i = 0; i < this.getModel().getRowCount(); ++i) {
				double v = StringUtils.asNumber(this.getModel().getData(i,
						this.dataColumn_));
				if (!Double.isNaN(v)) {
					total += v;
				}
			}
		}
		double value = StringUtils.asNumber(this.getModel().getData(index,
				this.dataColumn_));
		if (!Double.isNaN(value)) {
			WString label = this.labelText(index, value, total, options);
			if (!(label.length() == 0)) {
				WText l = new WText(label);
				l.setPadding(new WLength(5), EnumSet.of(Side.Left));
				legendItem.addWidget(l);
			}
		}
		return legendItem;
	}

	/**
	 * Creates a widget which renders the a legend item.
	 * <p>
	 * Returns {@link #createLegendItemWidget(int index, EnumSet options)
	 * createLegendItemWidget(index, EnumSet.of(option, options))}
	 */
	public final WWidget createLegendItemWidget(int index, LabelOption option,
			LabelOption... options) {
		return createLegendItemWidget(index, EnumSet.of(option, options));
	}

	/**
	 * Adds a data point area (used for displaying e.g. tooltips).
	 * <p>
	 * You may want to specialize this is if you wish to modify (or delete) the
	 * area.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Currently, an area is only created if the ToolTipRole
	 * data at the data point is not empty. </i>
	 * </p>
	 */
	public void addDataPointArea(WModelIndex index, WAbstractArea area) {
		(this).addArea(area);
	}

	public void paint(WPainter painter, WRectF rectangle) {
		double total = 0;
		if (this.dataColumn_ != -1) {
			for (int i = 0; i < this.getModel().getRowCount(); ++i) {
				double v = StringUtils.asNumber(this.getModel().getData(i,
						this.dataColumn_));
				if (!Double.isNaN(v)) {
					total += v;
				}
			}
		}
		if (!painter.isActive()) {
			throw new WException("WPieChart::paint(): painter is not active.");
		}
		WRectF rect = rectangle;
		if (rect.isEmpty()) {
			rect.assign(painter.getWindow());
		}
		rect.setX(rect.getX() + this.getPlotAreaPadding(Side.Left));
		rect.setY(rect.getY() + this.getPlotAreaPadding(Side.Top));
		rect.setWidth(rect.getWidth() - this.getPlotAreaPadding(Side.Left)
				- this.getPlotAreaPadding(Side.Right));
		rect.setHeight(rect.getHeight() - this.getPlotAreaPadding(Side.Top)
				- this.getPlotAreaPadding(Side.Bottom));
		double side = Math.min(rect.getWidth(), rect.getHeight());
		painter.save();
		painter.translate(rect.getLeft() + (rect.getWidth() - side) / 2, rect
				.getTop()
				+ (rect.getHeight() - side) / 2);
		if (!(this.getTitle().length() == 0)) {
			painter.translate(0, 15);
		}
		double cx = Math.floor(side / 2) + 0.5;
		double cy = cx;
		double r = (int) (side / 2 + 0.5);
		double h = this.height_ * r;
		painter.save();
		if (h > 0.0) {
			painter.translate(0, r / 2 - h / 4);
			painter.scale(1, 0.5);
		}
		this.drawPie(painter, cx, cy, r, h, total);
		painter.restore();
		painter.translate(0, -h / 4);
		if (!this.labelOptions_.isEmpty()) {
			if (total != 0) {
				double currentAngle = this.startAngle_;
				for (int i = 0; i < this.getModel().getRowCount(); ++i) {
					double v = StringUtils.asNumber(this.getModel().getData(i,
							this.dataColumn_));
					if (Double.isNaN(v)) {
						continue;
					}
					double spanAngle = -v / total * 360;
					double midAngle = currentAngle + spanAngle / 2.0;
					double endAngle = currentAngle + spanAngle;
					if (endAngle < 0) {
						endAngle += 360;
					}
					if (midAngle < 0) {
						midAngle += 360;
					}
					double width = 200;
					double height = 30;
					double left;
					double top;
					double f;
					if (!EnumUtils
							.mask(this.labelOptions_, LabelOption.Outside)
							.isEmpty()) {
						f = this.pie_.get(i).explode + 1.1;
					} else {
						f = this.pie_.get(i).explode + 0.7;
					}
					double px = cx
							+ f
							* r
							* Math
									.cos(-midAngle / 180.0 * 3.14159265358979323846);
					double py = cy
							+ f
							* r
							* Math
									.sin(-midAngle / 180.0 * 3.14159265358979323846)
							* (h > 0 ? 0.5 : 1);
					EnumSet<AlignmentFlag> alignment = EnumSet
							.noneOf(AlignmentFlag.class);
					WColor c = painter.getPen().getColor();
					if (!EnumUtils
							.mask(this.labelOptions_, LabelOption.Outside)
							.isEmpty()) {
						if (midAngle < 90) {
							left = px;
							top = py - height;
							alignment = EnumSet.copyOf(EnumSet.of(
									AlignmentFlag.AlignLeft,
									AlignmentFlag.AlignBottom));
						} else {
							if (midAngle < 180) {
								left = px - width;
								top = py - height;
								alignment = EnumSet.copyOf(EnumSet.of(
										AlignmentFlag.AlignRight,
										AlignmentFlag.AlignBottom));
							} else {
								if (midAngle < 270) {
									left = px - width;
									top = py + h / 2;
									alignment = EnumSet.copyOf(EnumSet.of(
											AlignmentFlag.AlignRight,
											AlignmentFlag.AlignTop));
								} else {
									left = px;
									top = py + h / 2;
									alignment = EnumSet.copyOf(EnumSet.of(
											AlignmentFlag.AlignLeft,
											AlignmentFlag.AlignTop));
								}
							}
						}
					} else {
						left = px - width / 2;
						top = py - height / 2;
						alignment = EnumSet.copyOf(EnumSet.of(
								AlignmentFlag.AlignCenter,
								AlignmentFlag.AlignMiddle));
						c = this.getPalette().getFontColor(i);
					}
					if (v / total * 100 >= this.avoidLabelRendering_) {
						painter.setPen(new WPen(c));
						painter.drawText(new WRectF(left, top, width, height),
								alignment, this.labelText(i, v, total,
										this.labelOptions_));
					}
					currentAngle = endAngle;
				}
			}
		}
		if (!(this.getTitle().length() == 0)) {
			WFont oldFont = painter.getFont();
			painter.setFont(this.getTitleFont());
			painter.drawText(cx - 50, cy - r, 100, 50, EnumSet.of(
					AlignmentFlag.AlignCenter, AlignmentFlag.AlignTop), this
					.getTitle());
			painter.setFont(oldFont);
		}
		painter.restore();
	}

	protected void paintEvent(WPaintDevice paintDevice) {
		while (!this.getAreas().isEmpty()) {
			;
		}
		WPainter painter = new WPainter(paintDevice);
		painter.setRenderHint(WPainter.RenderHint.Antialiasing, true);
		this.paint(painter);
	}

	private int labelsColumn_;
	private int dataColumn_;
	private double height_;
	private double startAngle_;
	private double avoidLabelRendering_;
	private EnumSet<LabelOption> labelOptions_;
	private boolean shadow_;

	static class PieData {
		private static Logger logger = LoggerFactory.getLogger(PieData.class);

		public boolean customBrush;
		public WBrush brush;
		public double explode;

		public PieData() {
			this.customBrush = false;
			this.brush = new WBrush();
			this.explode = 0;
		}
	}

	private List<WPieChart.PieData> pie_;

	protected void modelChanged() {
		this.pie_.clear();
		{
			int insertPos = 0;
			for (int ii = 0; ii < this.getModel().getRowCount(); ++ii)
				this.pie_.add(insertPos + ii, new WPieChart.PieData());
		}
		;
		this.update();
	}

	protected void modelReset() {
		if (this.getModel().getRowCount() != (int) this.pie_.size()) {
			this.modelChanged();
		} else {
			this.update();
		}
	}

	protected void modelColumnsInserted(WModelIndex parent, int start, int end) {
		if (this.labelsColumn_ >= start) {
			this.labelsColumn_ += end - start + 1;
		}
		if (this.dataColumn_ >= start) {
			this.dataColumn_ += end - start + 1;
		}
	}

	protected void modelColumnsRemoved(WModelIndex parent, int start, int end) {
		boolean needUpdate = false;
		if (this.labelsColumn_ >= start) {
			if (this.labelsColumn_ <= end) {
				this.labelsColumn_ = -1;
				needUpdate = true;
			} else {
				this.labelsColumn_ -= end - start + 1;
			}
		}
		if (this.dataColumn_ >= start) {
			if (this.dataColumn_ <= end) {
				this.dataColumn_ = -1;
				needUpdate = true;
			} else {
				this.dataColumn_ -= end - start + 1;
			}
		}
		if (needUpdate) {
			this.update();
		}
	}

	protected void modelRowsInserted(WModelIndex parent, int start, int end) {
		for (int i = start; i <= end; ++i) {
			this.pie_.add(0 + i, new WPieChart.PieData());
		}
		this.update();
	}

	protected void modelRowsRemoved(WModelIndex parent, int start, int end) {
		for (int i = end; i >= start; --i) {
			this.pie_.remove(0 + i);
		}
		this.update();
	}

	protected void modelDataChanged(WModelIndex topLeft, WModelIndex bottomRight) {
		if (this.labelsColumn_ >= topLeft.getColumn()
				&& this.labelsColumn_ <= bottomRight.getColumn()
				|| this.dataColumn_ >= topLeft.getColumn()
				&& this.dataColumn_ <= bottomRight.getColumn()) {
			this.update();
		}
	}

	private void drawPie(WPainter painter, double cx, double cy, double r,
			double h, double total) {
		if (h > 0) {
			if (total == 0) {
				if (this.shadow_) {
					this.setShadow(painter);
				}
				this.drawOuter(painter, cx, cy, r, 0, -180, h);
				if (this.shadow_) {
					painter.setShadow(new WShadow());
				}
			} else {
				if (this.shadow_) {
					this.setShadow(painter);
					painter.setBrush(new WBrush(WColor.black));
					this.drawSlices(painter, cx, cy + h, r, total, true);
					painter.setShadow(new WShadow());
				}
				List<Double> startAngles = new ArrayList<Double>();
				List<Double> midAngles = new ArrayList<Double>();
				{
					int insertPos = startAngles.size();
					for (int ii = 0; ii < this.getModel().getRowCount(); ++ii)
						startAngles.add(insertPos + ii, 0.0);
				}
				;
				{
					int insertPos = midAngles.size();
					for (int ii = 0; ii < this.getModel().getRowCount(); ++ii)
						midAngles.add(insertPos + ii, 0.0);
				}
				;
				int index90 = 0;
				double currentAngle = this.startAngle_;
				for (int i = 0; i < this.getModel().getRowCount(); ++i) {
					startAngles.set(i, currentAngle);
					double v = StringUtils.asNumber(this.getModel().getData(i,
							this.dataColumn_));
					if (Double.isNaN(v)) {
						continue;
					}
					double spanAngle = -v / total * 360;
					midAngles.set(i, currentAngle + spanAngle / 2.0);
					double endAngle = currentAngle + spanAngle;
					double to90 = currentAngle - 90;
					if (to90 < 0) {
						to90 += 360;
					}
					if (spanAngle <= -to90) {
						index90 = i;
					}
					if (endAngle < 0) {
						endAngle += 360;
					}
					currentAngle = endAngle;
				}
				for (int j = 0; j < this.getModel().getRowCount(); ++j) {
					int i = (index90 + j) % this.getModel().getRowCount();
					double v = StringUtils.asNumber(this.getModel().getData(i,
							this.dataColumn_));
					if (Double.isNaN(v)) {
						continue;
					}
					double midAngle = midAngles.get(i);
					double endAngle = startAngles.get((i + 1)
							% this.getModel().getRowCount());
					int n = this.nextIndex(i);
					boolean visible = endAngle <= 90 || endAngle >= 270;
					boolean drawS2 = visible
							&& (this.pie_.get(i).explode > 0.0 || this.pie_
									.get(n).explode > 0.0);
					if (drawS2) {
						double pcx = cx
								+ r
								* this.pie_.get(i).explode
								* Math
										.cos(-midAngle / 180.0 * 3.14159265358979323846);
						double pcy = cy
								+ r
								* this.pie_.get(i).explode
								* Math
										.sin(-midAngle / 180.0 * 3.14159265358979323846);
						painter.setBrush(darken(this.getBrush(i)));
						this.drawSide(painter, pcx, pcy, r, endAngle, h);
					}
					if (!visible) {
						break;
					}
				}
				for (int j = this.getModel().getRowCount(); j > 0; --j) {
					int i = (index90 + j) % this.getModel().getRowCount();
					double v = StringUtils.asNumber(this.getModel().getData(i,
							this.dataColumn_));
					if (Double.isNaN(v)) {
						continue;
					}
					double startAngle = startAngles.get(i);
					double midAngle = midAngles.get(i);
					int p = this.prevIndex(i);
					boolean visible = startAngle >= 90 && startAngle <= 270;
					boolean drawS1 = visible
							&& (this.pie_.get(i).explode > 0.0 || this.pie_
									.get(p).explode > 0.0);
					if (drawS1) {
						double pcx = cx
								+ r
								* this.pie_.get(i).explode
								* Math
										.cos(-midAngle / 180.0 * 3.14159265358979323846);
						double pcy = cy
								+ r
								* this.pie_.get(i).explode
								* Math
										.sin(-midAngle / 180.0 * 3.14159265358979323846);
						painter.setBrush(darken(this.getBrush(i)));
						this.drawSide(painter, pcx, pcy, r, startAngle, h);
					}
					if (!visible) {
						break;
					}
				}
				for (int j = 0; j < this.getModel().getRowCount(); ++j) {
					int i = (index90 + j) % this.getModel().getRowCount();
					double v = StringUtils.asNumber(this.getModel().getData(i,
							this.dataColumn_));
					if (Double.isNaN(v)) {
						continue;
					}
					double startAngle = startAngles.get(i);
					double midAngle = midAngles.get(i);
					double endAngle = startAngles.get((i + 1)
							% this.getModel().getRowCount());
					double spanAngle = endAngle - startAngle;
					if (spanAngle > 0) {
						spanAngle -= 360;
					}
					boolean drawBorder = startAngle > 180 || endAngle > 180
							|| spanAngle < -180
							|| this.getModel().getRowCount() == 1;
					if (drawBorder) {
						painter.setBrush(darken(this.getBrush(i)));
						double pcx = cx
								+ r
								* this.pie_.get(i).explode
								* Math
										.cos(-midAngle / 180.0 * 3.14159265358979323846);
						double pcy = cy
								+ r
								* this.pie_.get(i).explode
								* Math
										.sin(-midAngle / 180.0 * 3.14159265358979323846);
						double a1 = startAngle < 180 ? 360 : startAngle;
						double a2 = endAngle < 180 ? 180 : endAngle;
						this.drawOuter(painter, pcx, pcy, r, a1, a2, h);
					}
				}
			}
		}
		if (total == 0) {
			painter.drawArc(cx - r, cy - r, r * 2, r * 2, 0, 16 * 360);
		} else {
			this.drawSlices(painter, cx, cy, r, total, false);
		}
	}

	private void drawSlices(WPainter painter, double cx, double cy, double r,
			double total, boolean shadow) {
		double currentAngle = this.startAngle_;
		for (int i = 0; i < this.getModel().getRowCount(); ++i) {
			double v = StringUtils.asNumber(this.getModel().getData(i,
					this.dataColumn_));
			if (Double.isNaN(v)) {
				continue;
			}
			double spanAngle = -v / total * 360;
			double midAngle = currentAngle + spanAngle / 2.0;
			double pcx = cx + r * this.pie_.get(i).explode
					* Math.cos(-midAngle / 180.0 * 3.14159265358979323846);
			double pcy = cy + r * this.pie_.get(i).explode
					* Math.sin(-midAngle / 180.0 * 3.14159265358979323846);
			if (!shadow) {
				painter.setBrush(this.getBrush(i));
			}
			if (v / total != 1.0) {
				painter.drawPie(pcx - r, pcy - r, r * 2, r * 2,
						(int) (currentAngle * 16), (int) (spanAngle * 16));
			} else {
				painter.drawEllipse(pcx - r, pcy - r, r * 2, r * 2);
			}
			if (!shadow) {
				WModelIndex index = this.getModel().getIndex(i,
						this.dataColumn_);
				Object toolTip = index.getData(ItemDataRole.ToolTipRole);
				if (!(toolTip == null)) {
					final int SEGMENT_ANGLE = 20;
					WPolygonArea area = new WPolygonArea();
					WTransform t = painter.getWorldTransform();
					area.addPoint(t.map(new WPointF(pcx, pcy)));
					double sa = Math.abs(spanAngle);
					for (double d = 0; d < sa; d += SEGMENT_ANGLE) {
						double a;
						if (spanAngle < 0) {
							a = currentAngle - d;
						} else {
							a = currentAngle + d;
						}
						area
								.addPoint(t
										.map(new WPointF(
												pcx
														+ r
														* Math
																.cos(-a / 180.0 * 3.14159265358979323846),
												pcy
														+ r
														* Math
																.sin(-a / 180.0 * 3.14159265358979323846))));
					}
					double a = currentAngle + spanAngle;
					area
							.addPoint(t
									.map(new WPointF(
											pcx
													+ r
													* Math
															.cos(-a / 180.0 * 3.14159265358979323846),
											pcy
													+ r
													* Math
															.sin(-a / 180.0 * 3.14159265358979323846))));
					area.setToolTip(StringUtils.asString(toolTip));
					this.addDataPointArea(index, area);
				}
			}
			double endAngle = currentAngle + spanAngle;
			if (endAngle < 0) {
				endAngle += 360;
			}
			currentAngle = endAngle;
		}
	}

	private void drawSide(WPainter painter, double pcx, double pcy, double r,
			double angle, double h) {
		WPainterPath path = new WPainterPath();
		path.arcMoveTo(pcx - r, pcy - r, 2 * r, 2 * r, angle);
		path.lineTo(path.getCurrentPosition().getX(), path.getCurrentPosition()
				.getY()
				+ h);
		path.lineTo(pcx, pcy + h);
		path.lineTo(pcx, pcy);
		path.closeSubPath();
		painter.drawPath(path);
	}

	private void drawOuter(WPainter painter, double pcx, double pcy, double r,
			double a1, double a2, double h) {
		WPainterPath path = new WPainterPath();
		path.arcMoveTo(pcx - r, pcy - r, 2 * r, 2 * r, a1);
		path.lineTo(path.getCurrentPosition().getX(), path.getCurrentPosition()
				.getY()
				+ h);
		path.arcTo(pcx, pcy + h, r, a1, a2 - a1);
		path.arcTo(pcx, pcy, r, a2, a1 - a2);
		path.closeSubPath();
		painter.drawPath(path);
	}

	private void setShadow(WPainter painter) {
		painter.setShadow(new WShadow(5, 15, new WColor(0, 0, 0, 20), 40));
	}

	private int prevIndex(int i) {
		int r = this.getModel().getRowCount();
		for (int p = i - 1; p != i; --p) {
			if (p < 0) {
				p += r;
			}
			double v = StringUtils.asNumber(this.getModel().getData(p,
					this.dataColumn_));
			if (!Double.isNaN(v)) {
				return p;
			}
		}
		return i;
	}

	private int nextIndex(int i) {
		int r = this.getModel().getRowCount();
		for (int n = (i + 1) % r; n != i; ++n) {
			double v = StringUtils.asNumber(this.getModel().getData(n,
					this.dataColumn_));
			if (!Double.isNaN(v)) {
				return n;
			}
		}
		return i;
	}

	private static WBrush darken(WBrush brush) {
		WBrush result = brush;
		WColor c = result.getColor();
		c.setRgb(c.getRed() * 3 / 4, c.getGreen() * 3 / 4, c.getBlue() * 3 / 4,
				c.getAlpha());
		result.setColor(c);
		return result;
	}

	private WString labelText(int index, double v, double total,
			EnumSet<LabelOption> options) {
		WString text = new WString();
		if (!EnumUtils.mask(options, LabelOption.TextLabel).isEmpty()) {
			if (this.labelsColumn_ != -1) {
				text = StringUtils.asString(this.getModel().getData(index,
						this.labelsColumn_));
			}
		}
		if (!EnumUtils.mask(options, LabelOption.TextPercentage).isEmpty()) {
			String buf = null;
			buf = String.format("%.3g%%", v / total * 100);
			if (!(text.length() == 0)) {
				text.append(": ");
			}
			text.append(buf);
		}
		return text;
	}

	private final WString labelText(int index, double v, double total,
			LabelOption option, LabelOption... options) {
		return labelText(index, v, total, EnumSet.of(option, options));
	}
}
