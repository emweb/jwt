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

class GraphicsWidgets extends TopicWidget {
	private static Logger logger = LoggerFactory
			.getLogger(GraphicsWidgets.class);

	public GraphicsWidgets() {
		super();
		addText(tr("graphics-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.setInternalBasePath("/graphics-charts");
		menu.addItem("2D painting", this.painting2d()).setPathComponent("");
		menu.addItem("Paintbrush",
				DeferredWidget.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.paintbrush();
					}
				}));
		menu.addItem("Category chart",
				DeferredWidget.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.categoryChart();
					}
				}));
		menu.addItem("Scatter plot",
				DeferredWidget.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.scatterPlot();
					}
				}));
		menu.addItem("Axis slider widget",
				DeferredWidget.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.axisSliderWidget();
					}
				}));
		menu.addItem("Pie chart",
				DeferredWidget.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.pieChart();
					}
				}));
		menu.addItem("Maps", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return GraphicsWidgets.this.googleMap();
			}
		}));
		menu.addItem("3D painting",
				DeferredWidget.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.painting3d();
					}
				}));
		menu.addItem("3D numerical chart",
				DeferredWidget.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.numCharts3d();
					}
				}));
		menu.addItem("3D category chart",
				DeferredWidget.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.catCharts3d();
					}
				}));
	}

	private WWidget painting2d() {
		WTemplate result = new TopicTemplate("graphics-Painting2D");
		result.bindWidget("PaintingEvent", PaintingEvent());
		result.bindWidget("PaintingShapes", PaintingShapes());
		result.bindWidget("PaintingTransformations", PaintingTransformations());
		result.bindWidget("PaintingClipping", PaintingClipping());
		result.bindWidget("PaintingStyle", PaintingStyle());
		result.bindWidget("PaintingImages", PaintingImages());
		result.bindWidget("PaintingInteractive", PaintingInteractive());
		return result;
	}

	private WWidget paintbrush() {
		WTemplate result = new TopicTemplate("graphics-Paintbrush");
		result.bindWidget("Paintbrush", Paintbrush());
		return result;
	}

	private WWidget categoryChart() {
		WTemplate result = new TopicTemplate("graphics-CategoryChart");
		result.bindWidget("CategoryChart", CategoryChart());
		return result;
	}

	private WWidget scatterPlot() {
		WTemplate result = new TopicTemplate("graphics-ScatterPlot");
		result.bindWidget("ScatterPlotData", ScatterPlotData());
		result.bindWidget("ScatterPlotCurve", ScatterPlotCurve());
		result.bindWidget("ScatterPlotInteractive", ScatterPlotInteractive());
		return result;
	}

	private WWidget axisSliderWidget() {
		WTemplate result = new TopicTemplate("graphics-AxisSliderWidget");
		result.bindWidget("AxisSliderWidget", AxisSliderWidget());
		result.bindWidget("AxisSliderWidgetDifferentDataSeries",
				AxisSliderWidgetDifferentDataSeries());
		return result;
	}

	private WWidget pieChart() {
		WTemplate result = new TopicTemplate("graphics-PieChart");
		result.bindWidget("PieChart", PieChart());
		return result;
	}

	private WWidget googleMap() {
		WTemplate result = new TopicTemplate("graphics-GoogleMap");
		result.bindWidget("GoogleMap", GoogleMap());
		result.bindString("GoogleMap-controls",
				reindent(tr("graphics-GoogleMap-controls")),
				TextFormat.PlainText);
		return result;
	}

	private WWidget painting3d() {
		WTemplate result = new TopicTemplate("graphics-Painting3D");
		result.bindWidget("Painting3D", Painting3D());
		return result;
	}

	private WWidget numCharts3d() {
		WTemplate result = new TopicTemplate("graphics-NumCharts3D");
		result.bindWidget("NumericalCharts3D", NumChart3d());
		return result;
	}

	private WWidget catCharts3d() {
		WTemplate result = new TopicTemplate("graphics-CatCharts3D");
		result.bindWidget("CategoryCharts3D", CatChart3d());
		return result;
	}

	// private WAbstractItemModel readCsvFile(final String fname,
	// WContainerWidget parent) ;
	WWidget PaintingEvent() {
		WContainerWidget container = new WContainerWidget();
		final MyPaintedWidget painting = new MyPaintedWidget(container);
		final WSpinBox sb = new WSpinBox(container);
		sb.setRange(10, 200);
		sb.setValue(100);
		sb.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				painting.setEnd(sb.getValue());
			}
		});
		return container;
	}

	WWidget PaintingShapes() {
		WContainerWidget container = new WContainerWidget();
		new ShapesWidget(container);
		return container;
	}

	WWidget PaintingTransformations() {
		WContainerWidget container = new WContainerWidget();
		new TransformationsWidget(container);
		return container;
	}

	WWidget PaintingClipping() {
		WContainerWidget container = new WContainerWidget();
		new ClippingWidget(container);
		return container;
	}

	WWidget PaintingStyle() {
		WContainerWidget container = new WContainerWidget();
		new StyleWidget(container);
		return container;
	}

	WWidget PaintingImages() {
		WContainerWidget container = new WContainerWidget();
		new PaintingImagesWidget(container);
		return container;
	}

	WWidget PaintingInteractive() {
		WContainerWidget container = new WContainerWidget();
		final PaintingInteractiveWidget widget = new PaintingInteractiveWidget(
				container);
		final WSpinBox sb = new WSpinBox(container);
		sb.setWidth(new WLength(300));
		sb.setRange(0, 360);
		sb.setValue(0);
		final WSlider slider = new WSlider(Orientation.Horizontal, container);
		slider.resize(new WLength(300), new WLength(50));
		slider.setRange(0, 360);
		slider.sliderMoved().addListener(widget.rotateSlot);
		sb.valueChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				slider.setValue(sb.getValue());
				widget.rotate(sb.getValue());
			}
		});
		sb.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				slider.setValue(sb.getValue());
				widget.rotate(sb.getValue());
			}
		});
		slider.valueChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				sb.setValue(slider.getValue());
				widget.rotate(slider.getValue());
			}
		});
		return container;
	}

	WPushButton createColorToggle(String className, final WColor color,
			final PaintBrush canvas) {
		WPushButton button = new WPushButton();
		button.setTextFormat(TextFormat.XHTMLText);
		button.setText("&nbsp;");
		button.setCheckable(true);
		button.addStyleClass(className);
		button.setWidth(new WLength(30));
		button.checked().addListener(this, new Signal.Listener() {
			public void trigger() {
				canvas.setColor(color);
			}
		});
		return button;
	}

	WWidget Paintbrush() {
		final WColor blue = new WColor(0, 110, 204);
		final WColor red = new WColor(218, 81, 76);
		final WColor green = new WColor(59, 195, 95);
		final WColor orange = new WColor(250, 168, 52);
		final WColor black = WColor.black;
		final WColor gray = new WColor(210, 210, 210);
		WContainerWidget result = new WContainerWidget();
		final PaintBrush canvas = new PaintBrush(710, 400);
		canvas.setColor(blue);
		canvas.getDecorationStyle().setBorder(
				new WBorder(WBorder.Style.Solid, WBorder.Width.Medium,
						WColor.black));
		List<WPushButton> colorButtons = new ArrayList<WPushButton>();
		colorButtons.add(createColorToggle("btn-primary", blue, canvas));
		colorButtons.add(createColorToggle("btn-danger", red, canvas));
		colorButtons.add(createColorToggle("btn-success", green, canvas));
		colorButtons.add(createColorToggle("btn-warning", orange, canvas));
		colorButtons.add(createColorToggle("btn-inverse", black, canvas));
		colorButtons.add(createColorToggle("", gray, canvas));
		WToolBar toolBar = new WToolBar();
		for (int i = 0; i < colorButtons.size(); ++i) {
			WPushButton button = colorButtons.get(i);
			button.setChecked(i == 0);
			toolBar.addButton(button);
			for (int j = 0; j < colorButtons.size(); ++j) {
				if (i != j) {
					final WPushButton other = colorButtons.get(j);
					button.checked().addListener(other, new Signal.Listener() {
						public void trigger() {
							other.setUnChecked();
						}
					});
				}
			}
		}
		WPushButton clearButton = new WPushButton("Clear");
		clearButton.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				canvas.clear();
			}
		});
		toolBar.addSeparator();
		toolBar.addButton(clearButton);
		result.addWidget(toolBar);
		result.addWidget(canvas);
		return result;
	}

	WWidget CategoryChart() {
		WContainerWidget container = new WContainerWidget();
		WStandardItemModel model = CsvUtil.csvToModel("" + "category.csv",
				container);
		if (!(model != null)) {
			return container;
		}
		for (int row = 0; row < model.getRowCount(); ++row) {
			for (int col = 0; col < model.getColumnCount(); ++col) {
				model.getItem(row, col).setFlags(
						EnumSet.of(ItemFlag.ItemIsEditable));
			}
		}
		WTableView table = new WTableView(container);
		table.setModel(model);
		table.setSortingEnabled(true);
		table.setColumnResizeEnabled(true);
		table.setAlternatingRowColors(true);
		table.setHeaderAlignment(0, EnumSet.of(AlignmentFlag.AlignCenter));
		table.setColumnAlignment(0, AlignmentFlag.AlignCenter);
		table.setRowHeight(new WLength(28));
		table.setHeaderHeight(new WLength(28));
		table.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
		table.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		table.setWidth(new WLength(4 * 120 + 80 + 5 * 7 + 2));
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			table.setEditTriggers(EnumSet
					.of(WAbstractItemView.EditTrigger.SingleClicked));
			table.setEditOptions(EnumUtils.or(table.getEditOptions(),
					WAbstractItemView.EditOption.SaveWhenClosed));
		} else {
			table.setEditTriggers(EnumSet
					.of(WAbstractItemView.EditTrigger.NoEditTrigger));
		}
		WItemDelegate delegate = new WItemDelegate(table);
		delegate.setTextFormat("%.f");
		table.setItemDelegate(delegate);
		table.setColumnWidth(0, new WLength(80));
		for (int i = 1; i < model.getColumnCount(); ++i) {
			table.setColumnWidth(i, new WLength(120));
		}
		WCartesianChart chart = new WCartesianChart(container);
		chart.setModel(model);
		chart.setXSeriesColumn(0);
		chart.setLegendEnabled(true);
		chart.setPlotAreaPadding(40,
				EnumSet.of(Side.Left, Side.Top, Side.Bottom));
		chart.setPlotAreaPadding(120, EnumSet.of(Side.Right));
		for (int column = 1; column < model.getColumnCount(); ++column) {
			WDataSeries series = new WDataSeries(column, SeriesType.BarSeries);
			series.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
			chart.addSeries(series);
		}
		chart.resize(new WLength(600), new WLength(400));
		chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		return container;
	}

	WWidget ScatterPlotData() {
		WContainerWidget container = new WContainerWidget();
		WStandardItemModel model = CsvUtil.csvToModel("" + "timeseries.csv",
				container);
		if (!(model != null)) {
			return container;
		}
		for (int row = 0; row < model.getRowCount(); ++row) {
			WString s = StringUtils.asString(model.getData(row, 0));
			WDate date = WDate.fromString(s.toString(), "dd/MM/yy");
			model.setData(row, 0, date);
		}
		WTableView table = new WTableView(container);
		table.setModel(model);
		table.setSortingEnabled(false);
		table.setColumnResizeEnabled(true);
		table.setAlternatingRowColors(true);
		table.setColumnAlignment(0, AlignmentFlag.AlignCenter);
		table.setHeaderAlignment(0, EnumSet.of(AlignmentFlag.AlignCenter));
		table.setRowHeight(new WLength(28));
		table.setHeaderHeight(new WLength(28));
		table.setColumnWidth(0, new WLength(80));
		for (int column = 1; column < model.getColumnCount(); ++column) {
			table.setColumnWidth(column, new WLength(90));
		}
		table.resize(new WLength(783), new WLength(200));
		WItemDelegate delegate = new WItemDelegate(table);
		delegate.setTextFormat("%.1f");
		table.setItemDelegate(delegate);
		table.setItemDelegateForColumn(0, new WItemDelegate(table));
		WCartesianChart chart = new WCartesianChart(container);
		chart.setBackground(new WBrush(new WColor(220, 220, 220)));
		chart.setModel(model);
		chart.setXSeriesColumn(0);
		chart.setLegendEnabled(true);
		chart.setType(ChartType.ScatterPlot);
		chart.getAxis(Axis.XAxis).setScale(AxisScale.DateScale);
		chart.setPlotAreaPadding(40,
				EnumSet.of(Side.Left, Side.Top, Side.Bottom));
		chart.setPlotAreaPadding(120, EnumSet.of(Side.Right));
		for (int i = 2; i < 4; ++i) {
			WDataSeries s = new WDataSeries(i, SeriesType.LineSeries);
			s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
			chart.addSeries(s);
		}
		chart.resize(new WLength(800), new WLength(400));
		chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		return container;
	}

	WWidget ScatterPlotCurve() {
		WContainerWidget container = new WContainerWidget();
		WStandardItemModel model = new WStandardItemModel(40, 2, container);
		model.setHeaderData(0, new WString("X"));
		model.setHeaderData(1, new WString("Y = sin(X)"));
		for (int i = 0; i < 40; ++i) {
			double x = ((double) i - 20) / 4;
			model.setData(i, 0, x);
			model.setData(i, 1, Math.sin(x));
		}
		WCartesianChart chart = new WCartesianChart(container);
		chart.setModel(model);
		chart.setXSeriesColumn(0);
		chart.setLegendEnabled(true);
		chart.setType(ChartType.ScatterPlot);
		chart.getAxis(Axis.XAxis).setLocation(AxisValue.ZeroValue);
		chart.getAxis(Axis.YAxis).setLocation(AxisValue.ZeroValue);
		chart.setPlotAreaPadding(120, EnumSet.of(Side.Right));
		chart.setPlotAreaPadding(40, EnumSet.of(Side.Top, Side.Bottom));
		WDataSeries s = new WDataSeries(1, SeriesType.CurveSeries);
		s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
		chart.addSeries(s);
		chart.resize(new WLength(800), new WLength(300));
		chart.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
		chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		return container;
	}

	WWidget ScatterPlotInteractive() {
		WContainerWidget container = new WContainerWidget();
		WStandardItemModel model = CsvUtil.csvToModel("" + "timeseries.csv",
				container);
		if (!(model != null)) {
			return container;
		}
		for (int row = 0; row < model.getRowCount(); ++row) {
			WString s = StringUtils.asString(model.getData(row, 0));
			WDate date = WDate.fromString(s.toString(), "dd/MM/yy");
			model.setData(row, 0, date);
		}
		WCartesianChart chart = new WCartesianChart(container);
		chart.setBackground(new WBrush(new WColor(220, 220, 220)));
		chart.setModel(model);
		chart.setXSeriesColumn(0);
		chart.setType(ChartType.ScatterPlot);
		chart.getAxis(Axis.XAxis).setScale(AxisScale.DateScale);
		double min = StringUtils.asNumber(model.getData(0, 0));
		double max = StringUtils.asNumber(model.getData(
				model.getRowCount() - 1, 0));
		chart.getAxis(Axis.XAxis).setMinimumZoomRange((max - min) / 16.0);
		{
			WDataSeries s = new WDataSeries(2, SeriesType.LineSeries);
			s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
			chart.addSeries(s);
		}
		{
			WDataSeries s = new WDataSeries(3, SeriesType.LineSeries);
			s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
			chart.addSeries(s);
		}
		chart.resize(new WLength(800), new WLength(400));
		chart.setPanEnabled(true);
		chart.setZoomEnabled(true);
		chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		return container;
	}

	WWidget AxisSliderWidget() {
		WContainerWidget container = new WContainerWidget();
		WStandardItemModel model = CsvUtil.csvToModel("" + "timeseries.csv",
				container);
		if (!(model != null)) {
			return container;
		}
		for (int row = 0; row < model.getRowCount(); ++row) {
			WString s = StringUtils.asString(model.getData(row, 0));
			WDate date = WDate.fromString(s.toString(), "dd/MM/yy");
			model.setData(row, 0, date);
		}
		WCartesianChart chart = new WCartesianChart(container);
		chart.setBackground(new WBrush(new WColor(220, 220, 220)));
		chart.setModel(model);
		chart.setXSeriesColumn(0);
		chart.setType(ChartType.ScatterPlot);
		chart.getAxis(Axis.XAxis).setScale(AxisScale.DateScale);
		double min = StringUtils.asNumber(model.getData(0, 0));
		double max = StringUtils.asNumber(model.getData(
				model.getRowCount() - 1, 0));
		chart.getAxis(Axis.XAxis).setMinimumZoomRange((max - min) / 16.0);
		WDataSeries s = new WDataSeries(2, SeriesType.LineSeries);
		s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
		chart.addSeries(s);
		chart.resize(new WLength(800), new WLength(400));
		chart.setPanEnabled(true);
		chart.setZoomEnabled(true);
		chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		WAxisSliderWidget sliderWidget = new WAxisSliderWidget(s, container);
		sliderWidget.resize(new WLength(800), new WLength(80));
		sliderWidget.setSelectionAreaPadding(40,
				EnumSet.of(Side.Left, Side.Right));
		sliderWidget.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		return container;
	}

	WWidget AxisSliderWidgetDifferentDataSeries() {
		WContainerWidget container = new WContainerWidget();
		final ChartState state = new ChartState(container);
		state.model = new SinModel(-3.14159265358979323846,
				3.14159265358979323846);
		final WCartesianChart chart = new WCartesianChart(container);
		chart.setBackground(new WBrush(new WColor(220, 220, 220)));
		chart.setType(ChartType.ScatterPlot);
		WAbstractChartModel roughModel = new SinModel(-3.14159265358979323846,
				3.14159265358979323846, container);
		WDataSeries roughSeries = new WDataSeries(1, SeriesType.LineSeries);
		roughSeries.setModel(roughModel);
		roughSeries.setXSeriesColumn(0);
		roughSeries.setHidden(true);
		chart.addSeries(roughSeries);
		final WDataSeries series = new WDataSeries(1, SeriesType.LineSeries);
		series.setModel(state.model);
		series.setXSeriesColumn(0);
		series.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
		chart.addSeries(series);
		chart.getAxis(Axis.XAxis).zoomRangeChanged()
				.addListener(this, new Signal.Listener() {
					public void trigger() {
						double minX = chart.getAxis(Axis.XAxis)
								.getZoomMinimum();
						double maxX = chart.getAxis(Axis.XAxis)
								.getZoomMaximum();
						double dX = maxX - minX;
						minX = minX - dX / 2.0;
						if (minX < -3.14159265358979323846) {
							minX = -3.14159265358979323846;
						}
						maxX = maxX + dX / 2.0;
						if (maxX > 3.14159265358979323846) {
							maxX = 3.14159265358979323846;
						}
						if (state.model.getMinimum() != minX
								|| state.model.getMaximum() != maxX) {
							;
							state.model = new SinModel(minX, maxX);
							series.setModel(state.model);
						}
					}
				});
		chart.getAxis(Axis.XAxis).setMinimumZoomRange(
				3.14159265358979323846 / 8.0);
		chart.getAxis(Axis.XAxis).setMinimum(-3.5);
		chart.getAxis(Axis.XAxis).setMaximum(3.5);
		chart.getAxis(Axis.YAxis).setMinimumZoomRange(0.1);
		chart.getAxis(Axis.YAxis).setMinimum(-1.5);
		chart.getAxis(Axis.YAxis).setMaximum(1.5);
		chart.resize(new WLength(800), new WLength(400));
		chart.setPanEnabled(true);
		chart.setZoomEnabled(true);
		chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		WAxisSliderWidget sliderWidget = new WAxisSliderWidget(roughSeries,
				container);
		sliderWidget.resize(new WLength(800), new WLength(80));
		sliderWidget.setSelectionAreaPadding(40,
				EnumSet.of(Side.Left, Side.Right));
		sliderWidget.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		return container;
	}

	WWidget PieChart() {
		WContainerWidget container = new WContainerWidget();
		WStandardItemModel model = new WStandardItemModel(container);
		model.setItemPrototype(new NumericItem());
		model.insertColumns(model.getColumnCount(), 2);
		model.setHeaderData(0, new WString("Item"));
		model.setHeaderData(1, new WString("Sales"));
		model.insertRows(model.getRowCount(), 6);
		int row = 0;
		model.setData(row, 0, new WString("Blueberry"));
		model.setData(row, 1, new WString("Blueberry"),
				ItemDataRole.ToolTipRole);
		model.setData(row, 1, 120);
		model.setData(++row, 0, new WString("Cherry"));
		model.setData(row, 1, 30);
		model.setData(++row, 0, new WString("Apple"));
		model.setData(row, 1, 260);
		model.setData(++row, 0, new WString("Boston Cream"));
		model.setData(row, 1, 160);
		model.setData(++row, 0, new WString("Other"));
		model.setData(row, 1, 40);
		model.setData(++row, 0, new WString("Vanilla Cream"));
		model.setData(row, 1, 120);
		for (row = 0; row < model.getRowCount(); ++row) {
			for (int col = 0; col < model.getColumnCount(); ++col) {
				model.getItem(row, col).setFlags(
						EnumSet.of(ItemFlag.ItemIsEditable));
			}
		}
		WTableView table = new WTableView(container);
		table.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
		table.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		table.setSortingEnabled(true);
		table.setModel(model);
		table.setColumnWidth(1, new WLength(100));
		table.setRowHeight(new WLength(28));
		table.setHeaderHeight(new WLength(28));
		table.setWidth(new WLength(150 + 100 + 14 + 2));
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			table.setEditTriggers(EnumSet
					.of(WAbstractItemView.EditTrigger.SingleClicked));
		} else {
			table.setEditTriggers(EnumSet
					.of(WAbstractItemView.EditTrigger.NoEditTrigger));
		}
		WPieChart chart = new WPieChart(container);
		chart.setModel(model);
		chart.setLabelsColumn(0);
		chart.setDataColumn(1);
		chart.setDisplayLabels(EnumUtils.or(
				EnumSet.of(LabelOption.Outside, LabelOption.TextLabel),
				LabelOption.TextPercentage));
		chart.setPerspectiveEnabled(true, 0.2);
		chart.setShadowEnabled(true);
		chart.setExplode(0, 0.3);
		chart.resize(new WLength(800), new WLength(300));
		chart.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
		chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		return container;
	}

	WWidget GoogleMap() {
		GoogleMapExample map = new GoogleMapExample();
		return map;
	}

	WWidget Painting3D() {
		WContainerWidget container = new WContainerWidget();
		return container;
	}

	WWidget NumChart3d() {
		WContainerWidget container = new WContainerWidget();
		WCartesian3DChart chart = new WCartesian3DChart(container);
		chart.setType(ChartType.ScatterPlot);
		chart.setRenderOptions(EnumSet.of(
				WGLWidget.RenderOption.ClientSideRendering,
				WGLWidget.RenderOption.AntiAliasing));
		final WCssDecorationStyle style = new WCssDecorationStyle();
		style.setBorder(new WBorder(WBorder.Style.Solid, WBorder.Width.Medium,
				WColor.black));
		chart.setDecorationStyle(style);
		chart.resize(new WLength(900), new WLength(700));
		chart.setGridEnabled(Plane.XY_Plane, Axis.XAxis_3D, true);
		chart.setGridEnabled(Plane.XY_Plane, Axis.YAxis_3D, true);
		chart.setGridEnabled(Plane.XZ_Plane, Axis.XAxis_3D, true);
		chart.setGridEnabled(Plane.XZ_Plane, Axis.ZAxis_3D, true);
		chart.setGridEnabled(Plane.YZ_Plane, Axis.YAxis_3D, true);
		chart.setGridEnabled(Plane.YZ_Plane, Axis.ZAxis_3D, true);
		chart.axis(Axis.XAxis_3D).setTitle("X");
		chart.axis(Axis.YAxis_3D).setTitle("Y");
		chart.axis(Axis.ZAxis_3D).setTitle("Z");
		chart.setIntersectionLinesEnabled(true);
		chart.setIntersectionLinesColor(new WColor(0, 255, 255));
		WStandardItemModel model1 = new SombreroData(40, 40, container);
		WGridData dataset1 = new WGridData(model1);
		dataset1.setType(Series3DType.SurfaceSeries3D);
		dataset1.setSurfaceMeshEnabled(true);
		WStandardColorMap colormap = new WStandardColorMap(
				dataset1.minimum(Axis.ZAxis_3D),
				dataset1.maximum(Axis.ZAxis_3D), true);
		dataset1.setColorMap(colormap);
		WStandardItemModel model2 = new PlaneData(40, 40, container);
		for (int i = 0; i < model2.getRowCount(); i++) {
			model2.setData(i, 0, 5, ItemDataRole.MarkerScaleFactorRole);
			model2.setData(i, model2.getColumnCount() - 1, 5,
					ItemDataRole.MarkerScaleFactorRole);
		}
		for (int i = 0; i < model2.getColumnCount(); i++) {
			model2.setData(0, i, 5, ItemDataRole.MarkerScaleFactorRole);
			model2.setData(model2.getRowCount() - 1, i, 5,
					ItemDataRole.MarkerScaleFactorRole);
		}
		for (int i = 0; i < model2.getRowCount(); i++) {
			model2.setData(i, 5, new WColor(0, 255, 0),
					ItemDataRole.MarkerBrushColorRole);
			model2.setData(i, 6, new WColor(0, 0, 255),
					ItemDataRole.MarkerBrushColorRole);
			model2.setData(i, 7, new WColor(0, 255, 0),
					ItemDataRole.MarkerBrushColorRole);
			model2.setData(i, 8, new WColor(0, 0, 255),
					ItemDataRole.MarkerBrushColorRole);
		}
		WEquidistantGridData dataset2 = new WEquidistantGridData(model2, -10,
				0.5f, -10, 0.5f);
		WStandardItemModel model3 = new SpiralData(100, container);
		WScatterData dataset3 = new WScatterData(model3);
		dataset3.setPointSize(5);
		WStandardItemModel model4 = new HorizontalPlaneData(20, 20, container);
		WEquidistantGridData dataset4 = new WEquidistantGridData(model4, -10,
				1.0f, -10, 1.0f);
		dataset4.setType(Series3DType.SurfaceSeries3D);
		dataset4.setSurfaceMeshEnabled(true);
		chart.addDataSeries(dataset1);
		chart.addDataSeries(dataset2);
		chart.addDataSeries(dataset3);
		chart.addDataSeries(dataset4);
		chart.setAlternativeContent(new WImage(new WLink(
				"pics/numericalChartScreenshot.png")));
		return container;
	}

	WWidget CatChart3d() {
		WContainerWidget container = new WContainerWidget();
		WCartesian3DChart chart = new WCartesian3DChart(container);
		chart.setType(ChartType.CategoryChart);
		chart.setRenderOptions(EnumSet.of(
				WGLWidget.RenderOption.ClientSideRendering,
				WGLWidget.RenderOption.AntiAliasing));
		final WCssDecorationStyle style = new WCssDecorationStyle();
		style.setBorder(new WBorder(WBorder.Style.Solid, WBorder.Width.Medium,
				WColor.black));
		chart.setDecorationStyle(style);
		chart.resize(new WLength(800), new WLength(600));
		chart.setTitle("Fish consumption in western Europe");
		chart.axis(Axis.ZAxis_3D).setTitle("Consumption (pcs/year)");
		chart.setLegendStyle(new WFont(), new WPen(), new WBrush(
				WColor.lightGray));
		chart.setLegendEnabled(true);
		chart.setGridEnabled(Plane.XZ_Plane, Axis.ZAxis_3D, true);
		chart.setGridEnabled(Plane.YZ_Plane, Axis.ZAxis_3D, true);
		WStandardItemModel model = CsvUtil.csvToModel(""
				+ "fish_consumption.csv", container, false);
		for (int i = 0; i < model.getRowCount(); i++) {
			for (int j = 0; j < model.getColumnCount(); j++) {
				if (StringUtils.asString(model.getData(0, j)).equals(
						new WString("codfish"))
						&& StringUtils.asString(model.getData(i, 0)).equals(
								new WString("Belgium"))) {
					model.setData(i, j, WColor.cyan,
							ItemDataRole.MarkerBrushColorRole);
				}
			}
		}
		WGridData isotopes = new WGridData(model);
		isotopes.setTitle("made-up data");
		isotopes.setType(Series3DType.BarSeries3D);
		chart.addDataSeries(isotopes);
		chart.setAlternativeContent(new WImage(new WLink(
				"pics/categoricalChartScreenshot.png")));
		return container;
	}
}
