/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GraphicsWidgets extends Topic {
  private static Logger logger = LoggerFactory.getLogger(GraphicsWidgets.class);

  public GraphicsWidgets() {
    super();
  }

  public void populateSubMenu(WMenu menu) {
    menu.setInternalBasePath("/graphics-charts");
    menu.addItem(
            "2D painting",
            DeferredWidget.deferCreate(
                () -> {
                  return GraphicsWidgets.this.painting2d();
                }))
        .setPathComponent("");
    menu.addItem(
        "Paintbrush",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.paintbrush();
            }));
    menu.addItem(
        "Category chart",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.categoryChart();
            }));
    menu.addItem(
        "Scatter plot",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.scatterPlot();
            }));
    menu.addItem(
        "Axis slider widget",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.axisSliderWidget();
            }));
    menu.addItem(
        "Pie chart",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.pieChart();
            }));
    menu.addItem(
        "Leaflet maps",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.leafletMap();
            }));
    menu.addItem(
        "Google maps",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.googleMap();
            }));
    menu.addItem(
        "3D painting",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.painting3d();
            }));
    menu.addItem(
        "3D numerical chart",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.numCharts3d();
            }));
    menu.addItem(
        "3D category chart",
        DeferredWidget.deferCreate(
            () -> {
              return GraphicsWidgets.this.catCharts3d();
            }));
  }

  private WWidget painting2d() {
    TopicTemplate result = new TopicTemplate("graphics-Painting2D");
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
    TopicTemplate result = new TopicTemplate("graphics-Paintbrush");
    result.bindWidget("Paintbrush", Paintbrush());
    return result;
  }

  private WWidget categoryChart() {
    TopicTemplate result = new TopicTemplate("graphics-CategoryChart");
    result.bindWidget("CategoryChart", CategoryChart());
    return result;
  }

  private WWidget scatterPlot() {
    TopicTemplate result = new TopicTemplate("graphics-ScatterPlot");
    result.bindWidget("ScatterPlotData", ScatterPlotData());
    result.bindWidget("ScatterPlotCurve", ScatterPlotCurve());
    result.bindWidget("ScatterPlotInteractive", ScatterPlotInteractive());
    return result;
  }

  private WWidget axisSliderWidget() {
    TopicTemplate result = new TopicTemplate("graphics-AxisSliderWidget");
    result.bindWidget("AxisSliderWidget", AxisSliderWidget());
    result.bindWidget("AxisSliderWidgetDifferentDataSeries", AxisSliderWidgetDifferentDataSeries());
    return result;
  }

  private WWidget pieChart() {
    TopicTemplate result = new TopicTemplate("graphics-PieChart");
    result.bindWidget("PieChart", PieChart());
    return result;
  }

  private WWidget leafletMap() {
    TopicTemplate result = new TopicTemplate("graphics-LeafletMap");
    result.bindWidget("LeafletMap", LeafletMap());
    return result;
  }

  private WWidget googleMap() {
    TopicTemplate result = new TopicTemplate("graphics-GoogleMap");
    result.bindWidget("GoogleMap", GoogleMap());
    result.bindString(
        "GoogleMap-controls",
        reindent(WString.tr("graphics-GoogleMap-controls")),
        TextFormat.Plain);
    return result;
  }

  private WWidget painting3d() {
    TopicTemplate result = new TopicTemplate("graphics-Painting3D");
    result.bindWidget("Painting3D", Painting3D());
    return result;
  }

  private WWidget numCharts3d() {
    TopicTemplate result = new TopicTemplate("graphics-NumCharts3D");
    result.bindWidget("NumericalCharts3D", NumChart3d());
    return result;
  }

  private WWidget catCharts3d() {
    TopicTemplate result = new TopicTemplate("graphics-CatCharts3D");
    result.bindWidget("CategoryCharts3D", CatChart3d());
    return result;
  }
  // private WAbstractItemModel  readCsvFile(final String fname, WContainerWidget  parent) ;
  WWidget PaintingEvent() {
    WContainerWidget container = new WContainerWidget();
    final MyPaintedWidget painting = new MyPaintedWidget((WContainerWidget) container);
    final WSpinBox sb = new WSpinBox((WContainerWidget) container);
    sb.setRange(10, 200);
    sb.setValue(100);
    sb.changed()
        .addListener(
            this,
            () -> {
              painting.setEnd(sb.getValue());
            });
    return container;
  }

  WWidget PaintingShapes() {
    WContainerWidget container = new WContainerWidget();
    new ShapesWidget((WContainerWidget) container);
    return container;
  }

  WWidget PaintingTransformations() {
    WContainerWidget container = new WContainerWidget();
    new TransformationsWidget((WContainerWidget) container);
    return container;
  }

  WWidget PaintingClipping() {
    WContainerWidget container = new WContainerWidget();
    new ClippingWidget((WContainerWidget) container);
    return container;
  }

  WWidget PaintingStyle() {
    WContainerWidget container = new WContainerWidget();
    new StyleWidget((WContainerWidget) container);
    return container;
  }

  WWidget PaintingImages() {
    WContainerWidget container = new WContainerWidget();
    new PaintingImagesWidget((WContainerWidget) container);
    return container;
  }

  WWidget PaintingInteractive() {
    WContainerWidget container = new WContainerWidget();
    final PaintingInteractiveWidget widget =
        new PaintingInteractiveWidget((WContainerWidget) container);
    final WSpinBox sb = new WSpinBox((WContainerWidget) container);
    sb.setWidth(new WLength(300));
    sb.setRange(0, 360);
    sb.setValue(0);
    final WSlider slider = new WSlider(Orientation.Horizontal, (WContainerWidget) container);
    slider.resize(new WLength(300), new WLength(50));
    slider.setRange(0, 360);
    slider.sliderMoved().addListener(widget.rotateSlot);
    sb.valueChanged()
        .addListener(
            this,
            () -> {
              slider.setValue(sb.getValue());
              widget.rotate(sb.getValue());
            });
    sb.enterPressed()
        .addListener(
            this,
            () -> {
              slider.setValue(sb.getValue());
              widget.rotate(sb.getValue());
            });
    slider
        .valueChanged()
        .addListener(
            this,
            () -> {
              sb.setValue(slider.getValue());
              widget.rotate(slider.getValue());
            });
    return container;
  }

  WPushButton createColorToggle(String className, final WColor color, final PaintBrush canvas) {
    WPushButton button = new WPushButton();
    button.setTextFormat(TextFormat.XHTML);
    button.setText("&nbsp;");
    button.setCheckable(true);
    button.addStyleClass(className);
    button.setWidth(new WLength(30));
    button
        .checked()
        .addListener(
            this,
            () -> {
              canvas.setColor(color);
            });
    return button;
  }

  WWidget Paintbrush() {
    final WColor blue = new WColor("#0d6efd");
    final WColor red = new WColor("#dc3545");
    final WColor green = new WColor("#198754");
    final WColor yellow = new WColor("#ffc107");
    final WColor black = new WColor(StandardColor.Black);
    final WColor gray = new WColor("#6c757d");
    WContainerWidget result = new WContainerWidget();
    PaintBrush canvas = new PaintBrush(710, 400);
    final PaintBrush canvas_ = canvas;
    canvas.setColor(blue);
    canvas
        .getDecorationStyle()
        .setBorder(new WBorder(BorderStyle.Solid, BorderWidth.Medium, black));
    List<WPushButton> colorButtons = new ArrayList<WPushButton>();
    colorButtons.add(createColorToggle("btn-blue", blue, canvas));
    colorButtons.add(createColorToggle("btn-danger", red, canvas));
    colorButtons.add(createColorToggle("btn-success", green, canvas));
    colorButtons.add(createColorToggle("btn-warning", yellow, canvas));
    colorButtons.add(createColorToggle("btn-black", black, canvas));
    colorButtons.add(createColorToggle("btn-secondary", gray, canvas));
    WToolBar toolBar = new WToolBar();
    for (int i = 0; i < colorButtons.size(); ++i) {
      WPushButton button = colorButtons.get(i);
      button.setChecked(i == 0);
      toolBar.addButton(button);
      for (int j = 0; j < colorButtons.size(); ++j) {
        if (i != j) {
          final WPushButton other = colorButtons.get(j);
          button
              .checked()
              .addListener(
                  other,
                  () -> {
                    other.setUnChecked();
                  });
        }
      }
    }
    WPushButton clearButton = new WPushButton("Clear");
    clearButton
        .clicked()
        .addListener(
            this,
            () -> {
              canvas_.clear();
            });
    toolBar.addSeparator();
    toolBar.addButton(clearButton);
    result.addWidget(toolBar);
    result.addWidget(canvas);
    return result;
  }

  WWidget CategoryChart() {
    WContainerWidget container = new WContainerWidget();
    WStandardItemModel model = CsvUtil.csvToModel("" + "category.csv");
    if (!(model != null)) {
      return container;
    }
    for (int row = 0; row < model.getRowCount(); ++row) {
      for (int col = 0; col < model.getColumnCount(); ++col) {
        model.getItem(row, col).setFlags(EnumSet.of(ItemFlag.Editable));
      }
    }
    WTableView table = new WTableView((WContainerWidget) container);
    table.setModel(model);
    table.setSortingEnabled(true);
    table.setColumnResizeEnabled(true);
    table.setAlternatingRowColors(true);
    table.setHeaderAlignment(0, EnumSet.of(AlignmentFlag.Center));
    table.setColumnAlignment(0, AlignmentFlag.Center);
    table.setRowHeight(new WLength(28));
    table.setHeaderHeight(new WLength(28));
    table.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
    table.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    table.setWidth(new WLength(4 * 120 + 80 + 5 * 7 + 2));
    if (WApplication.getInstance().getEnvironment().hasAjax()) {
      table.setEditTriggers(EnumSet.of(EditTrigger.SingleClicked));
      table.setEditOptions(EnumUtils.or(table.getEditOptions(), EditOption.SaveWhenClosed));
    } else {
      table.setEditTriggers(EnumSet.of(EditTrigger.None));
    }
    WItemDelegate delegate = new WItemDelegate();
    delegate.setTextFormat("%.f");
    table.setItemDelegate(delegate);
    table.setColumnWidth(0, new WLength(80));
    for (int i = 1; i < model.getColumnCount(); ++i) {
      table.setColumnWidth(i, new WLength(120));
    }
    WCartesianChart chart = new WCartesianChart((WContainerWidget) container);
    chart.setModel(model);
    chart.setXSeriesColumn(0);
    chart.setLegendEnabled(true);
    chart.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Top, Side.Bottom));
    chart.setPlotAreaPadding(120, EnumSet.of(Side.Right));
    for (int column = 1; column < model.getColumnCount(); ++column) {
      WDataSeries series = new WDataSeries(column, SeriesType.Bar);
      series.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
      chart.addSeries(series);
    }
    chart.resize(new WLength(600), new WLength(400));
    chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    return container;
  }

  WWidget ScatterPlotData() {
    WContainerWidget container = new WContainerWidget();
    WStandardItemModel model = CsvUtil.csvToModel("" + "timeseries.csv");
    if (!(model != null)) {
      return container;
    }
    for (int row = 0; row < model.getRowCount(); ++row) {
      WString s = StringUtils.asString(model.getData(row, 0));
      WDate date = WDate.fromString(s.toString(), "dd/MM/yy");
      model.setData(row, 0, date);
    }
    WTableView table = new WTableView((WContainerWidget) container);
    table.setModel(model);
    table.setSortingEnabled(false);
    table.setColumnResizeEnabled(true);
    table.setAlternatingRowColors(true);
    table.setColumnAlignment(0, AlignmentFlag.Center);
    table.setHeaderAlignment(0, EnumSet.of(AlignmentFlag.Center));
    table.setRowHeight(new WLength(28));
    table.setHeaderHeight(new WLength(28));
    table.setColumnWidth(0, new WLength(80));
    for (int column = 1; column < model.getColumnCount(); ++column) {
      table.setColumnWidth(column, new WLength(90));
    }
    table.resize(new WLength(783), new WLength(200));
    WItemDelegate delegate = new WItemDelegate();
    delegate.setTextFormat("%.1f");
    table.setItemDelegate(delegate);
    table.setItemDelegateForColumn(0, new WItemDelegate());
    WCartesianChart chart = new WCartesianChart((WContainerWidget) container);
    chart.setBackground(new WBrush(new WColor(220, 220, 220)));
    chart.setModel(model);
    chart.setXSeriesColumn(0);
    chart.setLegendEnabled(true);
    chart.setType(ChartType.Scatter);
    chart.getAxis(Axis.X).setScale(AxisScale.Date);
    chart.setPlotAreaPadding(40, EnumSet.of(Side.Left, Side.Top, Side.Bottom));
    chart.setPlotAreaPadding(120, EnumSet.of(Side.Right));
    for (int i = 2; i < 4; ++i) {
      WDataSeries s = new WDataSeries(i, SeriesType.Line);
      s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
      chart.addSeries(s);
    }
    chart.resize(new WLength(800), new WLength(400));
    chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    return container;
  }

  WWidget ScatterPlotCurve() {
    WContainerWidget container = new WContainerWidget();
    WStandardItemModel model = new WStandardItemModel(40, 2);
    model.setHeaderData(0, new WString("X"));
    model.setHeaderData(1, new WString("Y = sin(X)"));
    for (int i = 0; i < 40; ++i) {
      double x = ((double) i - 20) / 4;
      model.setData(i, 0, x);
      model.setData(i, 1, Math.sin(x));
    }
    WCartesianChart chart = new WCartesianChart((WContainerWidget) container);
    chart.setModel(model);
    chart.setXSeriesColumn(0);
    chart.setLegendEnabled(true);
    chart.setType(ChartType.Scatter);
    chart.getAxis(Axis.X).setLocation(AxisValue.Zero);
    chart.getAxis(Axis.Y).setLocation(AxisValue.Zero);
    chart.setPlotAreaPadding(120, EnumSet.of(Side.Right));
    chart.setPlotAreaPadding(40, EnumSet.of(Side.Top, Side.Bottom));
    WDataSeries s = new WDataSeries(1, SeriesType.Curve);
    s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
    chart.addSeries(s);
    chart.resize(new WLength(800), new WLength(300));
    chart.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
    chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    return container;
  }

  WWidget ScatterPlotInteractive() {
    WContainerWidget container = new WContainerWidget();
    WStandardItemModel model = CsvUtil.csvToModel("" + "timeseries.csv");
    if (!(model != null)) {
      return container;
    }
    for (int row = 0; row < model.getRowCount(); ++row) {
      WString s = StringUtils.asString(model.getData(row, 0));
      WDate date = WDate.fromString(s.toString(), "dd/MM/yy");
      model.setData(row, 0, date);
    }
    WCartesianChart chart = new WCartesianChart((WContainerWidget) container);
    chart.setBackground(new WBrush(new WColor(220, 220, 220)));
    chart.setModel(model);
    chart.setXSeriesColumn(0);
    chart.setType(ChartType.Scatter);
    chart.getAxis(Axis.X).setScale(AxisScale.Date);
    double min = StringUtils.asNumber(model.getData(0, 0));
    double max = StringUtils.asNumber(model.getData(model.getRowCount() - 1, 0));
    chart.getAxis(Axis.X).setMinimumZoomRange((max - min) / 16.0);
    {
      WDataSeries s = new WDataSeries(2, SeriesType.Line);
      s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
      chart.addSeries(s);
    }
    {
      WDataSeries s = new WDataSeries(3, SeriesType.Line);
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
    WStandardItemModel model = CsvUtil.csvToModel("" + "timeseries.csv");
    if (!(model != null)) {
      return container;
    }
    for (int row = 0; row < model.getRowCount(); ++row) {
      WString s = StringUtils.asString(model.getData(row, 0));
      WDate date = WDate.fromString(s.toString(), "dd/MM/yy");
      model.setData(row, 0, date);
    }
    WCartesianChart chart = new WCartesianChart((WContainerWidget) container);
    chart.setBackground(new WBrush(new WColor(220, 220, 220)));
    chart.setModel(model);
    chart.setXSeriesColumn(0);
    chart.setType(ChartType.Scatter);
    chart.getAxis(Axis.X).setScale(AxisScale.Date);
    double min = StringUtils.asNumber(model.getData(0, 0));
    double max = StringUtils.asNumber(model.getData(model.getRowCount() - 1, 0));
    chart.getAxis(Axis.X).setMinimumZoomRange((max - min) / 16.0);
    WDataSeries s = new WDataSeries(2, SeriesType.Line);
    WDataSeries s_ = s;
    s_.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
    chart.addSeries(s);
    chart.resize(new WLength(800), new WLength(400));
    chart.setPanEnabled(true);
    chart.setZoomEnabled(true);
    chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    WAxisSliderWidget sliderWidget = new WAxisSliderWidget(s_, (WContainerWidget) container);
    sliderWidget.resize(new WLength(800), new WLength(80));
    sliderWidget.setSelectionAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
    sliderWidget.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    return container;
  }

  WWidget AxisSliderWidgetDifferentDataSeries() {
    WContainerWidget container = new WContainerWidget();
    WCartesianChart chart = new WCartesianChart((WContainerWidget) container);
    chart.setBackground(new WBrush(new WColor(220, 220, 220)));
    chart.setType(ChartType.Scatter);
    SinModel roughModel = new SinModel(-3.14159265358979323846, 3.14159265358979323846, 100);
    WDataSeries roughSeries = new WDataSeries(1, SeriesType.Line);
    WDataSeries roughSeries_ = roughSeries;
    roughSeries_.setModel(roughModel);
    roughSeries_.setXSeriesColumn(0);
    roughSeries.setHidden(true);
    chart.addSeries(roughSeries);
    SinModel detailedModel = new SinModel(-3.14159265358979323846, 3.14159265358979323846, 10000);
    WDataSeries seriesPtr = new WDataSeries(1, SeriesType.Line);
    WDataSeries series = seriesPtr;
    series.setModel(detailedModel);
    series.setXSeriesColumn(0);
    series.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
    chart.addSeries(seriesPtr);
    chart.getAxis(Axis.X).setMaximumZoomRange(3.14159265358979323846);
    chart.getAxis(Axis.X).setMinimumZoomRange(3.14159265358979323846 / 16.0);
    chart.getAxis(Axis.X).setMinimum(-3.5);
    chart.getAxis(Axis.X).setMaximum(3.5);
    chart.getAxis(Axis.Y).setMinimumZoomRange(0.1);
    chart.getAxis(Axis.Y).setMinimum(-1.5);
    chart.getAxis(Axis.Y).setMaximum(1.5);
    chart.resize(new WLength(800), new WLength(400));
    chart.setPanEnabled(true);
    chart.setZoomEnabled(true);
    chart.setOnDemandLoadingEnabled(true);
    chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    WAxisSliderWidget sliderWidget =
        new WAxisSliderWidget(roughSeries_, (WContainerWidget) container);
    sliderWidget.resize(new WLength(800), new WLength(80));
    sliderWidget.setSelectionAreaPadding(40, EnumSet.of(Side.Left, Side.Right));
    sliderWidget.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    return container;
  }

  WWidget PieChart() {
    WContainerWidget container = new WContainerWidget();
    WStandardItemModel model = new WStandardItemModel();
    model.setItemPrototype(new NumericItem());
    model.insertColumns(model.getColumnCount(), 2);
    model.setHeaderData(0, new WString("Item"));
    model.setHeaderData(1, new WString("Sales"));
    model.insertRows(model.getRowCount(), 6);
    int row = 0;
    model.setData(row, 0, new WString("Blueberry"));
    model.setData(row, 1, new WString("Blueberry"), ItemDataRole.ToolTip);
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
        model.getItem(row, col).setFlags(EnumSet.of(ItemFlag.Editable));
      }
    }
    WTableView table = new WTableView((WContainerWidget) container);
    table.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
    table.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    table.setSortingEnabled(true);
    table.setModel(model);
    table.setColumnWidth(1, new WLength(100));
    table.setRowHeight(new WLength(28));
    table.setHeaderHeight(new WLength(28));
    table.setWidth(new WLength(150 + 100 + 14 + 2));
    if (WApplication.getInstance().getEnvironment().hasAjax()) {
      table.setEditTriggers(EnumSet.of(EditTrigger.SingleClicked));
    } else {
      table.setEditTriggers(EnumSet.of(EditTrigger.None));
    }
    WPieChart chart = new WPieChart((WContainerWidget) container);
    chart.setModel(model);
    chart.setLabelsColumn(0);
    chart.setDataColumn(1);
    chart.setDisplayLabels(
        EnumUtils.or(
            EnumSet.of(LabelOption.Outside, LabelOption.TextLabel), LabelOption.TextPercentage));
    chart.setPerspectiveEnabled(true, 0.2);
    chart.setShadowEnabled(true);
    chart.setExplode(0, 0.3);
    chart.resize(new WLength(800), new WLength(300));
    chart.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
    chart.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    return container;
  }

  WWidget LeafletMap() {
    LeafletMapExample map = new LeafletMapExample();
    return map;
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
    WCartesian3DChart chart = new WCartesian3DChart((WContainerWidget) container);
    chart.setType(ChartType.Scatter);
    chart.setRenderOptions(EnumSet.of(GLRenderOption.ClientSide, GLRenderOption.AntiAliasing));
    final WCssDecorationStyle style = new WCssDecorationStyle();
    style.setBorder(
        new WBorder(BorderStyle.Solid, BorderWidth.Medium, new WColor(StandardColor.Black)));
    chart.setDecorationStyle(style);
    chart.resize(new WLength(900), new WLength(700));
    chart.setGridEnabled(Plane.XY, Axis.X3D, true);
    chart.setGridEnabled(Plane.XY, Axis.Y3D, true);
    chart.setGridEnabled(Plane.XZ, Axis.X3D, true);
    chart.setGridEnabled(Plane.XZ, Axis.Z3D, true);
    chart.setGridEnabled(Plane.YZ, Axis.Y3D, true);
    chart.setGridEnabled(Plane.YZ, Axis.Z3D, true);
    chart.axis(Axis.X3D).setTitle("X");
    chart.axis(Axis.Y3D).setTitle("Y");
    chart.axis(Axis.Z3D).setTitle("Z");
    chart.setIntersectionLinesEnabled(true);
    chart.setIntersectionLinesColor(new WColor(0, 255, 255));
    SombreroData model1 = new SombreroData(40, 40);
    WGridData dataset1 = new WGridData(model1);
    dataset1.setType(Series3DType.Surface);
    dataset1.setSurfaceMeshEnabled(true);
    WStandardColorMap colormap =
        new WStandardColorMap(dataset1.minimum(Axis.Z3D), dataset1.maximum(Axis.Z3D), true);
    dataset1.setColorMap(colormap);
    PlaneData model2 = new PlaneData(40, 40);
    for (int i = 0; i < model2.getRowCount(); i++) {
      model2.setData(i, 0, 5, ItemDataRole.MarkerScaleFactor);
      model2.setData(i, model2.getColumnCount() - 1, 5, ItemDataRole.MarkerScaleFactor);
    }
    for (int i = 0; i < model2.getColumnCount(); i++) {
      model2.setData(0, i, 5, ItemDataRole.MarkerScaleFactor);
      model2.setData(model2.getRowCount() - 1, i, 5, ItemDataRole.MarkerScaleFactor);
    }
    for (int i = 0; i < model2.getRowCount(); i++) {
      model2.setData(i, 5, new WColor(0, 255, 0), ItemDataRole.MarkerBrushColor);
      model2.setData(i, 6, new WColor(0, 0, 255), ItemDataRole.MarkerBrushColor);
      model2.setData(i, 7, new WColor(0, 255, 0), ItemDataRole.MarkerBrushColor);
      model2.setData(i, 8, new WColor(0, 0, 255), ItemDataRole.MarkerBrushColor);
    }
    WEquidistantGridData dataset2 = new WEquidistantGridData(model2, -10, 0.5f, -10, 0.5f);
    SpiralData model3 = new SpiralData(100);
    WScatterData dataset3 = new WScatterData(model3);
    dataset3.setPointSize(5);
    HorizontalPlaneData model4 = new HorizontalPlaneData(20, 20);
    WEquidistantGridData dataset4 = new WEquidistantGridData(model4, -10, 1.0f, -10, 1.0f);
    dataset4.setType(Series3DType.Surface);
    dataset4.setSurfaceMeshEnabled(true);
    chart.addDataSeries(dataset1);
    chart.addDataSeries(dataset2);
    chart.addDataSeries(dataset3);
    chart.addDataSeries(dataset4);
    chart.setAlternativeContent(new WImage(new WLink("pics/numericalChartScreenshot.png")));
    return container;
  }

  WWidget CatChart3d() {
    WContainerWidget container = new WContainerWidget();
    WCartesian3DChart chart = new WCartesian3DChart((WContainerWidget) container);
    chart.setType(ChartType.Category);
    chart.setRenderOptions(EnumSet.of(GLRenderOption.ClientSide, GLRenderOption.AntiAliasing));
    final WCssDecorationStyle style = new WCssDecorationStyle();
    style.setBorder(
        new WBorder(BorderStyle.Solid, BorderWidth.Medium, new WColor(StandardColor.Black)));
    chart.setDecorationStyle(style);
    chart.resize(new WLength(800), new WLength(600));
    chart.setTitle("Fish consumption in western Europe");
    chart.axis(Axis.Z3D).setTitle("Consumption (pcs/year)");
    chart.setLegendStyle(new WFont(), new WPen(), new WBrush(new WColor(StandardColor.LightGray)));
    chart.setLegendEnabled(true);
    chart.setGridEnabled(Plane.XZ, Axis.Z3D, true);
    chart.setGridEnabled(Plane.YZ, Axis.Z3D, true);
    WStandardItemModel model = CsvUtil.csvToModel("" + "fish_consumption.csv", false);
    for (int i = 0; i < model.getRowCount(); i++) {
      for (int j = 0; j < model.getColumnCount(); j++) {
        if ((StringUtils.asString(model.getData(0, j))
                .toString()
                .equals(new WString("codfish").toString()))
            && (StringUtils.asString(model.getData(i, 0))
                .toString()
                .equals(new WString("Belgium").toString()))) {
          model.setData(i, j, new WColor(StandardColor.Cyan), ItemDataRole.MarkerBrushColor);
        }
      }
    }
    WGridData isotopes = new WGridData(model);
    isotopes.setTitle("made-up data");
    isotopes.setType(Series3DType.Bar);
    chart.addDataSeries(isotopes);
    chart.setAlternativeContent(new WImage(new WLink("pics/categoricalChartScreenshot.png")));
    return container;
  }
}
