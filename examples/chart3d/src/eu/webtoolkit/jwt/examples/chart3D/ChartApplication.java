/*
 * Copyright (C) 2014 Emweb bvba, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D;

import java.util.ArrayList;
import java.util.List;

import eu.webtoolkit.jwt.ItemDataRole;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBootstrapTheme;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WCheckBox;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.WGLWidget.RenderOption;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.ChartType;
import eu.webtoolkit.jwt.chart.Plane;
import eu.webtoolkit.jwt.chart.Series3DType;
import eu.webtoolkit.jwt.chart.WAxis;
import eu.webtoolkit.jwt.chart.WBarSelection;
import eu.webtoolkit.jwt.chart.WCartesian3DChart;
import eu.webtoolkit.jwt.chart.WGridData;

public class ChartApplication extends WApplication {
	private List<WGridData> isotopeses_ = new ArrayList<WGridData>();

	public ChartApplication(WEnvironment env) {
		super(env);
		
		WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
		resourceBundle.use("/eu/webtoolkit/jwt/examples/chart3D/template");
		setLocalizedStrings(resourceBundle);
		
		setTheme(new WBootstrapTheme(this));

		useStyleSheet(new WLink("style.css"));
		require("mouseHandlers.js");

		setTitle("Chart3D JWt example");

		new NumericalExample(getRoot());

        initBarChart();
		initFreeformChart();
	}

	private void initFreeformChart() {
		new WText("<h2>Free form data sets</h2>", getRoot());
		
		new WText("<p>This is a demonstration of how you could implement " +
				"your own <tt>WAbstractDataSeries</tt>. This example takes " +
				"a <tt>WAbstractItemModel</tt> to define its triangles, or " +
				"two <tt>WAbstractItemModels</tt> to define vertices and their " +
				"connection model separately.</p>", getRoot());

		final WCartesian3DChart chart2 = new WCartesian3DChart(getRoot());
		// Disabling server side rendering for JWt website.
		chart2.setRenderOptions(RenderOption.ClientSideRendering, RenderOption.AntiAliasing);
		
		chart2.setType(ChartType.ScatterPlot);

		chart2.resize(600, 600);

		chart2.axis(Axis.XAxis_3D).setTitle("X");
		chart2.axis(Axis.YAxis_3D).setTitle("Y");
		chart2.axis(Axis.ZAxis_3D).setTitle("Z");

		final WStandardItemModel myModel = new WStandardItemModel(6, 3,
				getRoot());
		myModel.setData(0, 0, 1f);
		myModel.setData(0, 1, 1f);
		myModel.setData(0, 2, 1f);
		myModel.setData(1, 0, 6f);
		myModel.setData(1, 1, 6f);
		myModel.setData(1, 2, 5f);
		myModel.setData(2, 0, 0f);
		myModel.setData(2, 1, 6f);
		myModel.setData(2, 2, 0f);
		myModel.setData(3, 0, -1f);
		myModel.setData(3, 1, -1f);
		myModel.setData(3, 2, -1f);
		myModel.setData(4, 0, -6f);
		myModel.setData(4, 1, -6f);
		myModel.setData(4, 2, -5f);
		myModel.setData(5, 0, 0f);
		myModel.setData(5, 1, -6f);
		myModel.setData(5, 2, 0f);
		final FreeFormDataSet ff = new FreeFormDataSet(myModel);
		ff.setSurfaceColor(new WColor(200, 200, 255));
		ff.setLineColor(new WColor(0, 0, 255));
		ff.setDrawLines(true);
		chart2.addDataSeries(ff);

		final WStandardItemModel myModel2 = new WStandardItemModel(6, 3,
				getRoot());
		myModel2.setData(0, 0, 0f);
		myModel2.setData(0, 1, 0f);
		myModel2.setData(0, 2, 0f);
		myModel2.setData(1, 0, 0f);
		myModel2.setData(1, 1, 0f);
		myModel2.setData(1, 2, 5f);
		myModel2.setData(2, 0, 5f);
		myModel2.setData(2, 1, 0f);
		myModel2.setData(2, 2, 0f);
		myModel2.setData(3, 0, 5f);
		myModel2.setData(3, 1, 0f);
		myModel2.setData(3, 2, 5f);
		myModel2.setData(4, 0, 0f);
		myModel2.setData(4, 1, -5f);
		myModel2.setData(4, 2, 0f);
		myModel2.setData(5, 0, 0f);
		myModel2.setData(5, 1, -5f);
		myModel2.setData(5, 2, 5f);
		final WStandardItemModel myModel3 = new WStandardItemModel(9, 1,
				getRoot());
		myModel3.setData(0, 0, 0);
		myModel3.setData(1, 0, 1);
		myModel3.setData(2, 0, 2);
		myModel3.setData(3, 0, 1);
		myModel3.setData(4, 0, 2);
		myModel3.setData(5, 0, 3);
		myModel3.setData(6, 0, 0);
		myModel3.setData(7, 0, 4);
		myModel3.setData(8, 0, 5);
		final FreeFormDataSet ff2 = new FreeFormDataSet(myModel2, myModel3);
		ff2.setSurfaceColor(new WColor(230, 200, 200));
		ff2.setLineColor(new WColor(255, 0, 0));
		ff2.setDrawLines(true);
		chart2.addDataSeries(ff2);

		final WCheckBox lines = new WCheckBox("lines", getRoot());
		lines.setChecked(true);
		lines.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				ff.setDrawLines(lines.isChecked());
				ff2.setDrawLines(lines.isChecked());
			}
		});
		
		new WBreak(getRoot());

		final WCheckBox extraTriangle = new WCheckBox("extra triangle, only updates connection model", getRoot());
		extraTriangle.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				int j = myModel3.getRowCount();
				if (extraTriangle.isChecked()) {
					myModel3.insertRow(j);
					myModel3.setData(j, 0, 0);
					myModel3.insertRow(j + 1);
					myModel3.setData(j + 1, 0, 1);
					myModel3.insertRow(j + 2);
					myModel3.setData(j + 2, 0, 5);
				} else {
					myModel3.removeRows(myModel3.getRowCount() - 3, 3);
				}
			}
		});

		new WBreak(getRoot());

		new WText("Color of surface based on connection model: ", getRoot());
		final WComboBox changeSquareColor = new WComboBox(getRoot());
		changeSquareColor.addItem("red");
		changeSquareColor.addItem("blue");
		changeSquareColor.addItem("green");
		changeSquareColor.addItem("orange");
		changeSquareColor.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				int newColor = changeSquareColor.getCurrentIndex();
				switch (newColor) {
				case 0:
					ff2.setSurfaceColor(new WColor(255, 200, 200));
					ff2.setLineColor(new WColor(255, 0, 0));
					break;
				case 1:
					ff2.setSurfaceColor(new WColor(200, 200, 255));
					ff2.setLineColor(new WColor(0, 0, 255));
					break;
				case 2:
					ff2.setSurfaceColor(new WColor(200, 255, 200));
					ff2.setLineColor(new WColor(0, 255, 0));
					break;
				case 3:
					ff2.setSurfaceColor(new WColor(255, 200, 100));
					ff2.setLineColor(new WColor(255, 127, 0));
					break;
				}
			}
		});
	}

	private void initBarChart() {
		new WText("<h2>Selectable bars on bar chart</h2>", getRoot());
		
		new WText("<p>Click on a bar segment, and it will change color.</p>", getRoot());

		WContainerWidget container = new WContainerWidget(getRoot());

		WCartesian3DChart barChart = new WCartesian3DChart(container);
		// Disabling server side rendering for JWt website.
		barChart.setRenderOptions(RenderOption.ClientSideRendering, RenderOption.AntiAliasing);
		barChart.setType(ChartType.CategoryChart);
		barChart.resize(800, 600);
		barChart.axis(Axis.ZAxis_3D).setTitle("Z");
		barChart.setGridEnabled(Plane.XZ_Plane, Axis.ZAxis_3D, true);
		barChart.setGridEnabled(Plane.YZ_Plane, Axis.ZAxis_3D, true);

		WStandardItemModel barModel = new WStandardItemModel(3, 3, container);
		barModel.setData(0, 0, 0);
		barModel.setData(0, 1, "A");
		barModel.setData(0, 2, "B");
		barModel.setData(1, 0, "0");
		barModel.setData(2, 0, "1");
		barModel.setData(1, 1, 12);
		barModel.setData(1, 2, 20);
		barModel.setData(2, 1, 19);
		barModel.setData(2, 2, 8);
		isotopeses_.add(new WGridData(barModel));
		isotopeses_.get(isotopeses_.size() - 1).setType(
				Series3DType.BarSeries3D);
		barChart.addDataSeries(isotopeses_.get(isotopeses_.size() - 1));

		WStandardItemModel barModel2 = new WStandardItemModel(3, 3, container);
		barModel2.setData(0, 0, 0);
		barModel2.setData(0, 1, "A");
		barModel2.setData(0, 2, "B");
		barModel2.setData(1, 0, "0");
		barModel2.setData(2, 0, "1");
		barModel2.setData(1, 1, 19);
		barModel2.setData(1, 2, 11);
		barModel2.setData(2, 1, 17);
		barModel2.setData(2, 2, 13);
		isotopeses_.add(new WGridData(barModel2));
		isotopeses_.get(isotopeses_.size() - 1).setType(
				Series3DType.BarSeries3D);
		barChart.addDataSeries(isotopeses_.get(isotopeses_.size() - 1));

		WStandardItemModel barModel3 = new WStandardItemModel(3, 3, container);
		barModel3.setData(0, 0, 0);
		barModel3.setData(0, 1, "A");
		barModel3.setData(0, 2, "B");
		barModel3.setData(1, 0, "0");
		barModel3.setData(2, 0, "1");
		barModel3.setData(1, 1, 3);
		barModel3.setData(1, 2, 4);
		barModel3.setData(2, 1, 6);
		barModel3.setData(2, 2, 2);
		isotopeses_.add(new WGridData(barModel3));
		isotopeses_.get(isotopeses_.size() - 1).setType(
				Series3DType.BarSeries3D);
		barChart.addDataSeries(isotopeses_.get(isotopeses_.size() - 1));

		barChart.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					@Override
					public void trigger(WMouseEvent e) {
						WGridData closestBar = null;
						WBarSelection closest = new WBarSelection();
						closest.distance = Double.POSITIVE_INFINITY;
						closest.index = null;
						for (WGridData isotopes : isotopeses_) {
							for (int x = 1; x <= isotopes.getNbXPoints(); x++) {
								for (int y = 1; y <= isotopes.getNbYPoints(); y++) {
									isotopes.getModel().setData(x, y, null,
											ItemDataRole.MarkerBrushColorRole);
								}
							}
							WBarSelection res = isotopes.pickBar(
									e.getWidget().x, e.getWidget().y);
							if (res.index != null
									&& res.distance < closest.distance) {
								closest = res;
								closestBar = isotopes;
							}
						}
						if (closest.index != null) {
							closestBar.getModel().setData(closest.index,
									WColor.darkCyan,
									ItemDataRole.MarkerBrushColorRole);
						}
					}
				});
		barChart.setAttributeValue("oncontextmenu",
				"Wt.WT.cancelEvent(event);");

		barChart.axis(Axis.ZAxis_3D).setMaximum(WAxis.AUTO_MAXIMUM);
	}
}
