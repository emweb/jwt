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

	// private WAbstractItemModel readCsvFile(String fname, WContainerWidget
	// parent) ;
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
		return container;
	}

	WWidget ScatterPlotData() {
		WContainerWidget container = new WContainerWidget();
		return container;
	}

	WWidget ScatterPlotCurve() {
		WContainerWidget container = new WContainerWidget();
		return container;
	}

	WWidget PieChart() {
		WContainerWidget container = new WContainerWidget();
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
