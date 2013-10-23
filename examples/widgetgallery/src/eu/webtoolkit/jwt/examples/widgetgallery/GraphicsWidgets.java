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
		menu.addItem("Paintbrush", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.paintbrush();
					}
				}));
		menu.addItem("Category chart", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.categoryChart();
					}
				}));
		menu.addItem("Scatter plot", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.scatterPlot();
					}
				}));
		menu.addItem("Pie chart", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.pieChart();
					}
				}));
		menu.addItem("Maps", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return GraphicsWidgets.this.googleMap();
			}
		}));
		menu.addItem("3D painting", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return GraphicsWidgets.this.painting3d();
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
		table.setWidth(new WLength(4 * 120 + 80 + 5 * 7));
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
		chart.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Top,
				Side.Bottom));
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
		table.resize(new WLength(800), new WLength(200));
		table.setColumnWidth(0, new WLength(80));
		for (int column = 1; column < model.getColumnCount(); ++column) {
			table.setColumnWidth(column, new WLength(90));
		}
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
		chart.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Top,
				Side.Bottom));
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
		chart.setPlotAreaPadding(80, EnumSet.of(Side.Left));
		chart.setPlotAreaPadding(40, EnumSet.of(Side.Top, Side.Bottom));
		WDataSeries s = new WDataSeries(1, SeriesType.CurveSeries);
		s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
		chart.addSeries(s);
		chart.resize(new WLength(800), new WLength(300));
		chart.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
		chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
		return container;
	}

	WWidget PieChart() {
		WContainerWidget container = new WContainerWidget();
		WStandardItemModel model = new WStandardItemModel(container);
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
		if (WApplication.getInstance().getEnvironment().hasAjax()) {
			table.resize(new WLength(150 + 100 + 14), new WLength(7 * 28));
			table.setEditTriggers(EnumSet
					.of(WAbstractItemView.EditTrigger.SingleClicked));
		} else {
			table.resize(new WLength(150 + 100 + 14), WLength.Auto);
			table.setEditTriggers(EnumSet
					.of(WAbstractItemView.EditTrigger.NoEditTrigger));
		}
		WPieChart chart = new WPieChart(container);
		chart.setModel(model);
		chart.setLabelsColumn(0);
		chart.setDataColumn(1);
		chart.setDisplayLabels(EnumUtils.or(EnumSet.of(LabelOption.Outside,
				LabelOption.TextLabel), LabelOption.TextPercentage));
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
}
