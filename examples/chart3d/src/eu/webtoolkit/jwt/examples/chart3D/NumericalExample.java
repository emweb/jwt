/*
 * Copyright (C) 2014 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point3d;

import eu.webtoolkit.jwt.AbstractSignal.Connection;
import eu.webtoolkit.jwt.JSignal3;
import eu.webtoolkit.jwt.PositionScheme;
import eu.webtoolkit.jwt.KeyboardModifier;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.Signal3;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.WButtonGroup;
import eu.webtoolkit.jwt.WCheckBox;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPen;
import eu.webtoolkit.jwt.WRadioButton;
import eu.webtoolkit.jwt.WSlider;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WTemplate;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WGLWidget.ClientSideRenderer;
import eu.webtoolkit.jwt.WGLWidget.RenderOption;
import eu.webtoolkit.jwt.WMouseEvent.Button;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.ChartType;
import eu.webtoolkit.jwt.chart.Plane;
import eu.webtoolkit.jwt.chart.Series3DType;
import eu.webtoolkit.jwt.chart.WAbstractGridData;
import eu.webtoolkit.jwt.chart.WCartesian3DChart;
import eu.webtoolkit.jwt.chart.WEquidistantGridData;
import eu.webtoolkit.jwt.chart.WGridData;
import eu.webtoolkit.jwt.chart.WPointSelection;
import eu.webtoolkit.jwt.chart.WScatterData;
import eu.webtoolkit.jwt.chart.WStandardColorMap;
import eu.webtoolkit.jwt.chart.WSurfaceSelection;
import eu.webtoolkit.jwt.chart.WCartesian3DChart.IntersectionPlane;

import eu.webtoolkit.jwt.examples.chart3D.MouseHandlerDemoChart;
import eu.webtoolkit.jwt.examples.chart3D.datasets.PlaneData;
import eu.webtoolkit.jwt.examples.chart3D.datasets.PointListData;
import eu.webtoolkit.jwt.examples.chart3D.datasets.SombreroData;
import eu.webtoolkit.jwt.examples.chart3D.datasets.SpiralData;

public class NumericalExample extends WTemplate {
	public NumericalExample(WContainerWidget parent) {
		super(tr("surface-chart"), parent);

		setPositionScheme(PositionScheme.Relative);

		chart_ = new MouseHandlerDemoChart();
		// Disabling server side rendering for JWt website.
		// Disabling anti-aliasing for better point sprites.
		chart_.setRenderOptions(RenderOption.ClientSideRendering);
		bindWidget("chart", chart_);
		chart_.setType(ChartType.ScatterPlot);

		chart_.resize(600, 600);
		chart_.setGridEnabled(Plane.XY_Plane, Axis.XAxis_3D, true);
		chart_.setGridEnabled(Plane.XY_Plane, Axis.YAxis_3D, true);
		chart_.setGridEnabled(Plane.XZ_Plane, Axis.XAxis_3D, true);
		chart_.setGridEnabled(Plane.XZ_Plane, Axis.ZAxis_3D, true);
		chart_.setGridEnabled(Plane.YZ_Plane, Axis.YAxis_3D, true);
		chart_.setGridEnabled(Plane.YZ_Plane, Axis.ZAxis_3D, true);

		chart_.axis(Axis.XAxis_3D).setTitle("X");
		chart_.axis(Axis.YAxis_3D).setTitle("Y");
		chart_.axis(Axis.ZAxis_3D).setTitle("Z");
		chart_.axis(Axis.XAxis_3D).setMinimum(-6);
		chart_.axis(Axis.XAxis_3D).setMaximum(6);
		chart_.axis(Axis.YAxis_3D).setMinimum(-6);
		chart_.axis(Axis.YAxis_3D).setMaximum(6);
		chart_.axis(Axis.ZAxis_3D).setMinimum(-5);
		chart_.axis(Axis.ZAxis_3D).setMaximum(5);

		chart_.setIntersectionLinesColor(new WColor(255, 0, 255));

		SombreroData model1 = new SombreroData(100, 100, -10, 10, -10, 10, this);
		sombreroData_ = new WGridData(model1);
		gridDataList_.add(sombreroData_);
		sombreroData_.setType(Series3DType.SurfaceSeries3D);
		sombreroData_.setSurfaceMeshEnabled(true);
		WStandardColorMap colorMap = new WStandardColorMap(
				sombreroData_.minimum(Axis.ZAxis_3D),
				sombreroData_.maximum(Axis.ZAxis_3D), true);
		sombreroData_.setColorMap(colorMap);
		sombreroData_.setPen(new WPen(new WColor(100,100,100)));

		final List<Double> isoLevels = new ArrayList<Double>();
		for (double isoLevel = -5.0; isoLevel <= 5.0; isoLevel += 0.5) {
			isoLevels.add(isoLevel);
		}
		final WCheckBox isoCheckbox = new WCheckBox("isolines");
		bindWidget("isolines", isoCheckbox);
		isoCheckbox.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				if (isoCheckbox.isChecked()) {
					sombreroData_.setIsoLevels(isoLevels);
				} else {
					sombreroData_.setIsoLevels(Collections.<Double>emptyList());
				}
			}
		});

		sombreroData_.setClippingLinesColor(new WColor(255, 255, 0));

		chart_.addDataSeries(sombreroData_);

		WStandardItemModel model3 = new SpiralData(100, this);
		spiralData_ = new WScatterData(model3);
		spiralData_.setPointSize(7);
		spiralData_.setPointSprite("fat_cross.png");
		spiralData_.setColorMap(new WStandardColorMap(-5.0, 5.0, Arrays.asList(
				new WStandardColorMap.Pair[] {
						new WStandardColorMap.Pair(-3.0, new WColor(0,100,0)),
						new WStandardColorMap.Pair(1.0, new WColor(0,255,0)),
						new WStandardColorMap.Pair(5.0, new WColor(0,255,255))
				}), true));
		chart_.addDataSeries(spiralData_);

		WStandardItemModel model4 = new PlaneData(80, 80, this);
		WEquidistantGridData planeData = new WEquidistantGridData(model4, -10,
				0.25f, -10, 0.25f);
		planeData.setColorMap(new WStandardColorMap(-5.0, 5.0, Arrays.asList(
				new WStandardColorMap.Pair[] {
						new WStandardColorMap.Pair(0.0, new WColor(62, 75, 106))
				}), false));
		planeData.setPen(new WPen(new WColor(62-25, 75-25, 106-25)));
		gridDataList_.add(planeData);
		planeData.setType(Series3DType.SurfaceSeries3D);
		planeData.setSurfaceMeshEnabled(true);
		chart_.addDataSeries(planeData);
		planeData.setClippingLinesColor(new WColor(255, 255, 0));

		WStandardItemModel model2 = new PointListData(new ArrayList<Point3d>(),
				this);
		points_ = new WScatterData(model2);
		points_.setColorMap(new WStandardColorMap(-5.0, 5.0, Arrays.asList(
				new WStandardColorMap.Pair[] {
						new WStandardColorMap.Pair(0.0, new WColor(255, 0, 0))
				}), false));
		points_.setPointSize(10);
		chart_.addDataSeries(points_);

		List<WCartesian3DChart.IntersectionPlane> intersectionPlanes = new ArrayList<WCartesian3DChart.IntersectionPlane>();
		intersectionPlanes.add(new WCartesian3DChart.IntersectionPlane(
				Axis.ZAxis_3D, 2.5, new WColor(0, 255, 255)));
		intersectionPlanes.add(new WCartesian3DChart.IntersectionPlane(
				Axis.XAxis_3D, 4.0, new WColor(123, 104, 238)));
		intersectionPlanes.add(new WCartesian3DChart.IntersectionPlane(
				Axis.XAxis_3D, 4.5, new WColor(123, 104, 238)));
		chart_.setIntersectionPlanes(intersectionPlanes);

		chart_.setAlternativeContent(new WText("Sorry, no charts for you."));
		
		final WCheckBox clippingLinesEnabled = new WCheckBox("clipping lines");
		bindWidget("clipping-lines", clippingLinesEnabled);
		clippingLinesEnabled.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				for (WAbstractGridData data : gridDataList_) {
					data.setClippingLinesEnabled(clippingLinesEnabled.isChecked());
				}
			}
		});

		xMinSlider_ = new WSlider();
		bindWidget("x-min-slider", xMinSlider_);
		xMinSlider_.setMinimum(-100);
		xMinSlider_.setMaximum(100);
		xMinSlider_.setValue(-100);
		xMinSlider_.resize(300, 12);
		xMaxSlider_ = new WSlider();
		bindWidget("x-max-slider", xMaxSlider_);
		xMaxSlider_.setMinimum(-100);
		xMaxSlider_.setMaximum(100);
		xMaxSlider_.setValue(100);
		xMaxSlider_.resize(300, 12);
		yMinSlider_ = new WSlider();
		bindWidget("y-min-slider", yMinSlider_);
		yMinSlider_.setMinimum(-100);
		yMinSlider_.setMaximum(100);
		yMinSlider_.setValue(-100);
		yMinSlider_.resize(300, 12);
		yMaxSlider_ = new WSlider();
		bindWidget("y-max-slider", yMaxSlider_);
		yMaxSlider_.setMinimum(-100);
		yMaxSlider_.setMaximum(100);
		yMaxSlider_.setValue(100);
		yMaxSlider_.resize(300, 12);
		zMinSlider_ = new WSlider();
		bindWidget("z-min-slider", zMinSlider_);
		zMinSlider_.setMinimum(-100);
		zMinSlider_.setMaximum(100);
		zMinSlider_.setValue(-100);
		zMinSlider_.resize(300, 12);
		zMaxSlider_ = new WSlider();
		bindWidget("z-max-slider", zMaxSlider_);
		zMaxSlider_.setMinimum(-100);
		zMaxSlider_.setMaximum(100);
		zMaxSlider_.setValue(100);
		zMaxSlider_.resize(300, 12);

		for (final WAbstractGridData data : gridDataList_) {
			xMinSlider_.sliderMoved().addListener(
					"function (o,e,pos) { "
							+ data.changeClippingMin(Axis.XAxis_3D).execJs("o",
									"e", "pos / 100.0 * 6") + " }");
			xMinSlider_.valueChanged().addListener(this,
					new Signal1.Listener<Integer>() {
						@Override
						public void trigger(Integer value) {
							data.setClippingMin(Axis.XAxis_3D, value / 100.0F * 6);
						}
					});
			xMaxSlider_.sliderMoved().addListener(
					"function (o,e,pos) { "
							+ data.changeClippingMax(Axis.XAxis_3D).execJs("o",
									"e", "pos / 100.0 * 6") + " }");
			xMaxSlider_.valueChanged().addListener(this,
					new Signal1.Listener<Integer>() {
						@Override
						public void trigger(Integer value) {
							data.setClippingMax(Axis.XAxis_3D, value / 100.0F * 6);
						}
					});
			yMinSlider_.sliderMoved().addListener(
					"function (o,e,pos) { "
							+ data.changeClippingMin(Axis.YAxis_3D).execJs("o",
									"e", "pos / 100.0 * 6") + " }");
			yMinSlider_.valueChanged().addListener(this,
					new Signal1.Listener<Integer>() {
						@Override
						public void trigger(Integer value) {
							data.setClippingMin(Axis.YAxis_3D, value / 100.0F * 6);
						}
					});
			yMaxSlider_.sliderMoved().addListener(
					"function (o,e,pos) { "
							+ data.changeClippingMax(Axis.YAxis_3D).execJs("o",
									"e", "pos / 100.0 * 6") + " }");
			yMaxSlider_.valueChanged().addListener(this,
					new Signal1.Listener<Integer>() {
						@Override
						public void trigger(Integer value) {
							data.setClippingMax(Axis.YAxis_3D, value / 100.0F * 6);
						}
					});
			zMinSlider_.sliderMoved().addListener(
					"function (o,e,pos) { "
							+ data.changeClippingMin(Axis.ZAxis_3D).execJs("o",
									"e", "pos / 100.0 * 6") + " }");
			zMinSlider_.valueChanged().addListener(this,
					new Signal1.Listener<Integer>() {
						@Override
						public void trigger(Integer value) {
							data.setClippingMin(Axis.ZAxis_3D, value / 100.0F * 6);
						}
					});
			zMaxSlider_.sliderMoved().addListener(
					"function (o,e,pos) { "
							+ data.changeClippingMax(Axis.ZAxis_3D).execJs("o",
									"e", "pos / 100.0 * 6") + " }");
			zMaxSlider_.valueChanged().addListener(this,
					new Signal1.Listener<Integer>() {
						@Override
						public void trigger(Integer value) {
							data.setClippingMax(Axis.ZAxis_3D, value / 100.0F * 6);
						}
					});
		}

		final WButtonGroup mouseHandler = new WButtonGroup(this);
		final WContainerWidget buttonGroupContainer = new WContainerWidget();
		bindWidget("mouse-handler", buttonGroupContainer);
		WRadioButton cameraRadio = new WRadioButton("camera", buttonGroupContainer);
		cameraRadio.setChecked(true);
		cameraRadio.setToolTip(tr("camera-tooltip"), TextFormat.XHTMLUnsafeText);
		WRadioButton selectSombreroRadio = new WRadioButton("select sombrero surface", buttonGroupContainer);
		selectSombreroRadio.setToolTip(tr("select-sombrero-tooltip"), TextFormat.XHTMLUnsafeText);
		WRadioButton selectSpiralRadio = new WRadioButton("select spiral", buttonGroupContainer);
		selectSpiralRadio.setToolTip(tr("select-spiral-tooltip"), TextFormat.XHTMLUnsafeText);
		mouseHandler.addButton(cameraRadio, 0);
		mouseHandler.addButton(selectSombreroRadio, 1);
		mouseHandler.addButton(selectSpiralRadio, 2);
		
		Signal.Listener mouseHandlerListener = new Signal.Listener() {
			@Override
			public void trigger() {
				switch (mouseHandler.getSelectedButtonIndex()) {
				case 0:
					if (selectModeConnection_ != null)
						chart_.clicked().disconnect(selectModeConnection_);
					chart_.selectionMode = false;
					chart_.selectionHandler = false;
					chart_.repaintGL(ClientSideRenderer.UPDATE_GL);
					break;
				case 1:
					selectionMode(false);
					break;
				case 2:
					selectionMode(true);
					break;
				}
			}
		};
		
		mouseHandler.checkedChanged().addListener(this, mouseHandlerListener);

		final WCheckBox intersectionLinesCheckBox = new WCheckBox("intersection lines");
		bindWidget("intersection-lines", intersectionLinesCheckBox);
		intersectionLinesCheckBox.changed().addListener(this,
				new Signal.Listener() {
					@Override
					public void trigger() {
						chart_.setIntersectionLinesEnabled(intersectionLinesCheckBox.isChecked());
					}
				});
		
		final WCheckBox intersectionPlanesCheckBox = new WCheckBox("intersection planes");
		intersectionPlanesCheckBox.setChecked(true);
		bindWidget("intersection-planes", intersectionPlanesCheckBox);
		intersectionPlanesCheckBox.changed().addListener(this,
				new Signal.Listener() {
					List<IntersectionPlane> prevPlanes;
					@Override
					public void trigger() {
						if (intersectionPlanesCheckBox.isChecked()) {
							chart_.setIntersectionPlanes(prevPlanes);
						} else {
							prevPlanes = new ArrayList<IntersectionPlane>(chart_.getIntersectionPlanes());
							chart_.setIntersectionPlanes(Collections.<IntersectionPlane>emptyList());
						}
					}
				});

		final WComboBox colorCombo = new WComboBox();
		bindWidget("intersection-lines-color", colorCombo);
		colorCombo.addItem("magenta");
		colorCombo.addItem("yellow");
		colorCombo.addItem("blue");
		colorCombo.addItem("cyan");
		colorCombo.addItem("black");
		colorCombo.changed().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				switch (colorCombo.getCurrentIndex()) {
				case 0:
					chart_.setIntersectionLinesColor(new WColor(255, 0, 255));
					break;
				case 1:
					chart_.setIntersectionLinesColor(new WColor(255, 255, 0));
					break;
				case 2:
					chart_.setIntersectionLinesColor(new WColor(0, 0, 255));
					break;
				case 3:
					chart_.setIntersectionLinesColor(new WColor(0, 255, 255));
					break;
				case 4:
					chart_.setIntersectionLinesColor(new WColor(0, 0, 0));
					break;
				}
			}
		});

		chart_.setBackground(new WColor(200, 200, 200));

		rangeSelectionSignal_ = new JSignal3<Integer, Integer, WMouseEvent>(
				chart_, "rangeSelect") {
		};

		rangeSelectionSignal_.addListener(this, new HandleRangeSelect());

		selectedRange_ = new WContainerWidget();
		bindWidget("selected-range", selectedRange_);
		selectedRange_.addStyleClass("selectedRange");

		chart_.setAttributeValue("oncontextmenu", "Wt.WT.cancelEvent(event);");

	}

	private class HandleSelect implements Signal1.Listener<WMouseEvent> {
		public void trigger(WMouseEvent event) {
			if (event.getButton() == Button.LeftButton) {
				if (selectSpiralData_) {
					selectSpiral(event);
				} else {
					selectSurface(event);
				}
			}
		}

		private void selectSurface(WMouseEvent event) {
			if (!event.getModifiers().contains(
					KeyboardModifier.ControlModifier)) {
				surfaceSelections_.clear();
			}
			long before = System.currentTimeMillis();
			List<WSurfaceSelection> selection = sombreroData_
					.pickSurface(event.getWidget().x,
							event.getWidget().y);
			surfaceSelections_.addAll(selection);
			long after = System.currentTimeMillis();
			System.out.println(String.format("Select took %d ms", after
					- before));
			if (!surfaceSelections_.isEmpty()) {
				surfaceSelected(surfaceSelections_);
			}
		}

		private void selectSpiral(WMouseEvent event) {
			if (!event.getModifiers().contains(
					KeyboardModifier.ControlModifier)) {
				pointSelections_.clear();
			}
			long before = System.currentTimeMillis();
			List<WPointSelection> selection = spiralData_.pickPoints(
					event.getWidget().x, event.getWidget().y, 5);
			pointSelections_.addAll(selection);
			long after = System.currentTimeMillis();
			System.out.println(String.format("Select took %d ms", after
					- before));
			if (!pointSelections_.isEmpty()) {
				pointsSelected(pointSelections_);
			}
		}
	}

	private class HandleRangeSelect implements
			Signal3.Listener<Integer, Integer, WMouseEvent> {
		@Override
		public void trigger(Integer x1, Integer y1, WMouseEvent e) {
			if (!e.getModifiers().contains(KeyboardModifier.ControlModifier)) {
				pointSelections_.clear();
			}
			long before = System.currentTimeMillis();
			List<WPointSelection> selection = spiralData_.pickPoints(x1, y1,
					e.getWidget().x, e.getWidget().y);
			pointSelections_.addAll(selection);
			long after = System.currentTimeMillis();
			System.out.println(String.format("Select took %d ms", after
					- before));
			if (!pointSelections_.isEmpty()) {
				pointsSelected(pointSelections_);
			}
		}
	}

	private void pointsSelected(ArrayList<WPointSelection> pointSelections) {
		ArrayList<Point3d> v = new ArrayList<Point3d>();
		for (int i = 0; i < pointSelections.size(); i++) {
			v.add(toModelData(pointSelections.get(i)));
		}
		WStandardItemModel model = new PointListData(v, this);
		chart_.removeDataSeries(points_);
		points_ = new WScatterData(model);
		points_.setColorMap(new WStandardColorMap(-5.0, 5.0, Arrays.asList(
				new WStandardColorMap.Pair[] {
						new WStandardColorMap.Pair(0.0, new WColor(255, 0, 0))
				}), false));
		points_.setPointSize(10);
		chart_.addDataSeries(points_);
	}

	private void surfaceSelected(ArrayList<WSurfaceSelection> surfaceSelections) {
		ArrayList<Point3d> v = new ArrayList<Point3d>();
		for (int i = 0; i < surfaceSelections.size(); i++) {
			v.add(toModelData(surfaceSelections.get(i)));
		}
		WStandardItemModel model = new PointListData(v, this);
		chart_.removeDataSeries(points_);
		points_ = new WScatterData(model);
		points_.setColorMap(new WStandardColorMap(-5.0, 5.0, Arrays.asList(
				new WStandardColorMap.Pair[] {
						new WStandardColorMap.Pair(0.0, new WColor(255, 0, 0))
				}), false));
		points_.setPointSize(10);
		chart_.addDataSeries(points_);
	}

	private Point3d toModelData(WPointSelection selection) {
		double x = (Double) spiralData_.getModel().getData(selection.rowNumber,
				0);
		double y = (Double) spiralData_.getModel().getData(selection.rowNumber,
				1);
		double z = (Double) spiralData_.getModel().getData(selection.rowNumber,
				2);
		return new Point3d(x, y, z);
	}

	private Point3d toModelData(WSurfaceSelection selection) {
		return new Point3d(selection.x, selection.y, selection.z);
	}

	private void selectionMode(boolean selectSpiralData) {
		if (chart_.selectionMode && selectSpiralData_ == selectSpiralData)
			return;
		if (!chart_.selectionMode)
			selectModeConnection_ = chart_.clicked().addListener(this,
					new HandleSelect());
		chart_.selectionMode = true;
		selectSpiralData_ = selectSpiralData;
		chart_.selectionHandler = selectSpiralData_;
		chart_.repaintGL(ClientSideRenderer.UPDATE_GL);
	}

	private JSignal3<Integer, Integer, WMouseEvent> rangeSelectionSignal_;
	private boolean selectSpiralData_ = false;
	private MouseHandlerDemoChart chart_;
	private WGridData sombreroData_;
	private WScatterData points_;
	private WScatterData spiralData_;
	private Connection selectModeConnection_;
	private ArrayList<WPointSelection> pointSelections_ = new ArrayList<WPointSelection>();
	private ArrayList<WSurfaceSelection> surfaceSelections_ = new ArrayList<WSurfaceSelection>();
	private WContainerWidget selectedRange_;
	private WSlider xMinSlider_;
	private WSlider xMaxSlider_;
	private WSlider yMinSlider_;
	private WSlider yMaxSlider_;
	private WSlider zMinSlider_;
	private WSlider zMaxSlider_;
	private List<WAbstractGridData> gridDataList_ = new ArrayList<WAbstractGridData>();
}
