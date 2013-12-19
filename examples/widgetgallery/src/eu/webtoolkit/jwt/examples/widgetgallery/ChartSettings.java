/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class ChartSettings extends WContainerWidget {
	private static Logger logger = LoggerFactory.getLogger(ChartSettings.class);

	public ChartSettings(final WCartesian3DChart chart, WContainerWidget parent) {
		super(parent);
		WTemplate template_ = new WTemplate(WString.tr("chartconfig-template"),
				this);
		chart.initLayout();
		final WCheckBox autoRangeX = new WCheckBox(this);
		template_.bindWidget("xAuto", autoRangeX);
		autoRangeX.setCheckState(CheckState.Checked);
		final WLineEdit xMin = new WLineEdit(StringUtils.asString(
				chart.axis(Axis.XAxis_3D).getMinimum()).toString(), this);
		template_.bindWidget("xAxisMin", xMin);
		xMin.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		xMin.setEnabled(false);
		final WLineEdit xMax = new WLineEdit(StringUtils.asString(
				chart.axis(Axis.XAxis_3D).getMaximum()).toString(), this);
		template_.bindWidget("xAxisMax", xMax);
		xMax.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		xMax.setEnabled(false);
		final WCheckBox autoRangeY = new WCheckBox(this);
		template_.bindWidget("yAuto", autoRangeY);
		autoRangeY.setCheckState(CheckState.Checked);
		final WLineEdit yMin = new WLineEdit(StringUtils.asString(
				chart.axis(Axis.YAxis_3D).getMinimum()).toString(), this);
		template_.bindWidget("yAxisMin", yMin);
		yMin.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		yMin.setEnabled(false);
		final WLineEdit yMax = new WLineEdit(StringUtils.asString(
				chart.axis(Axis.YAxis_3D).getMaximum()).toString(), this);
		template_.bindWidget("yAxisMax", yMax);
		yMax.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		yMax.setEnabled(false);
		final WCheckBox autoRangeZ = new WCheckBox(this);
		template_.bindWidget("zAuto", autoRangeZ);
		autoRangeZ.setCheckState(CheckState.Checked);
		final WLineEdit zMin = new WLineEdit(StringUtils.asString(
				chart.axis(Axis.ZAxis_3D).getMinimum()).toString(), this);
		template_.bindWidget("zAxisMin", zMin);
		zMin.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		zMin.setEnabled(false);
		final WLineEdit zMax = new WLineEdit(StringUtils.asString(
				chart.axis(Axis.ZAxis_3D).getMaximum()).toString(), this);
		template_.bindWidget("zAxisMax", zMax);
		zMax.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		zMax.setEnabled(false);
		autoRangeX.checked().addListener(this, new Signal.Listener() {
			public void trigger() {
				switch (autoRangeX.getCheckState()) {
				case Unchecked:
					xMin.setEnabled(true);
					xMax.setEnabled(true);
					break;
				case Checked:
					xMin.setText(StringUtils.asString(
							chart.axis(Axis.XAxis_3D).getMinimum()).toString());
					xMin.setEnabled(false);
					xMax.setText(StringUtils.asString(
							chart.axis(Axis.XAxis_3D).getMaximum()).toString());
					xMax.setEnabled(false);
					chart.axis(Axis.XAxis_3D).setAutoLimits(
							EnumSet.of(AxisValue.MinimumValue,
									AxisValue.MaximumValue));
					break;
				default:
					break;
				}
			}
		});
		autoRangeX.unChecked().addListener(this, new Signal.Listener() {
			public void trigger() {
				switch (autoRangeX.getCheckState()) {
				case Unchecked:
					xMin.setEnabled(true);
					xMax.setEnabled(true);
					break;
				case Checked:
					xMin.setText(StringUtils.asString(
							chart.axis(Axis.XAxis_3D).getMinimum()).toString());
					xMin.setEnabled(false);
					xMax.setText(StringUtils.asString(
							chart.axis(Axis.XAxis_3D).getMaximum()).toString());
					xMax.setEnabled(false);
					chart.axis(Axis.XAxis_3D).setAutoLimits(
							EnumSet.of(AxisValue.MinimumValue,
									AxisValue.MaximumValue));
					break;
				default:
					break;
				}
			}
		});
		autoRangeY.checked().addListener(this, new Signal.Listener() {
			public void trigger() {
				switch (autoRangeY.getCheckState()) {
				case Unchecked:
					yMin.setEnabled(true);
					yMax.setEnabled(true);
					break;
				case Checked:
					yMin.setText(StringUtils.asString(
							chart.axis(Axis.YAxis_3D).getMinimum()).toString());
					yMin.setEnabled(false);
					yMax.setText(StringUtils.asString(
							chart.axis(Axis.YAxis_3D).getMaximum()).toString());
					yMax.setEnabled(false);
					chart.axis(Axis.YAxis_3D).setAutoLimits(
							EnumSet.of(AxisValue.MinimumValue,
									AxisValue.MaximumValue));
					break;
				default:
					break;
				}
			}
		});
		autoRangeY.unChecked().addListener(this, new Signal.Listener() {
			public void trigger() {
				switch (autoRangeY.getCheckState()) {
				case Unchecked:
					yMin.setEnabled(true);
					yMax.setEnabled(true);
					break;
				case Checked:
					yMin.setText(StringUtils.asString(
							chart.axis(Axis.YAxis_3D).getMinimum()).toString());
					yMin.setEnabled(false);
					yMax.setText(StringUtils.asString(
							chart.axis(Axis.YAxis_3D).getMaximum()).toString());
					yMax.setEnabled(false);
					chart.axis(Axis.YAxis_3D).setAutoLimits(
							EnumSet.of(AxisValue.MinimumValue,
									AxisValue.MaximumValue));
					break;
				default:
					break;
				}
			}
		});
		autoRangeZ.checked().addListener(this, new Signal.Listener() {
			public void trigger() {
				switch (autoRangeZ.getCheckState()) {
				case Unchecked:
					zMin.setEnabled(true);
					zMax.setEnabled(true);
					break;
				case Checked:
					zMin.setText(StringUtils.asString(
							chart.axis(Axis.ZAxis_3D).getMinimum()).toString());
					zMin.setEnabled(false);
					zMax.setText(StringUtils.asString(
							chart.axis(Axis.ZAxis_3D).getMaximum()).toString());
					zMax.setEnabled(false);
					chart.axis(Axis.ZAxis_3D).setAutoLimits(
							EnumSet.of(AxisValue.MinimumValue,
									AxisValue.MaximumValue));
					break;
				default:
					break;
				}
			}
		});
		autoRangeZ.unChecked().addListener(this, new Signal.Listener() {
			public void trigger() {
				switch (autoRangeZ.getCheckState()) {
				case Unchecked:
					zMin.setEnabled(true);
					zMax.setEnabled(true);
					break;
				case Checked:
					zMin.setText(StringUtils.asString(
							chart.axis(Axis.ZAxis_3D).getMinimum()).toString());
					zMin.setEnabled(false);
					zMax.setText(StringUtils.asString(
							chart.axis(Axis.ZAxis_3D).getMaximum()).toString());
					zMax.setEnabled(false);
					chart.axis(Axis.ZAxis_3D).setAutoLimits(
							EnumSet.of(AxisValue.MinimumValue,
									AxisValue.MaximumValue));
					break;
				default:
					break;
				}
			}
		});
		xMin.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				chart.axis(Axis.XAxis_3D).setRange(
						StringUtils.asNumber(xMin.getText()),
						StringUtils.asNumber(xMax.getText()));
			}
		});
		xMax.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				chart.axis(Axis.XAxis_3D).setRange(
						StringUtils.asNumber(xMin.getText()),
						StringUtils.asNumber(xMax.getText()));
			}
		});
		yMin.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				chart.axis(Axis.YAxis_3D).setRange(
						StringUtils.asNumber(yMin.getText()),
						StringUtils.asNumber(yMax.getText()));
			}
		});
		yMax.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				chart.axis(Axis.YAxis_3D).setRange(
						StringUtils.asNumber(yMin.getText()),
						StringUtils.asNumber(yMax.getText()));
			}
		});
		zMin.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				chart.axis(Axis.ZAxis_3D).setRange(
						StringUtils.asNumber(zMin.getText()),
						StringUtils.asNumber(zMax.getText()));
			}
		});
		zMax.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				chart.axis(Axis.ZAxis_3D).setRange(
						StringUtils.asNumber(zMin.getText()),
						StringUtils.asNumber(zMax.getText()));
			}
		});
	}

	public ChartSettings(final WCartesian3DChart chart) {
		this(chart, (WContainerWidget) null);
	}
}
