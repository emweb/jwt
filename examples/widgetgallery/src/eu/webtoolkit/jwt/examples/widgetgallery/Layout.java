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

class Layout extends TopicWidget {
	private static Logger logger = LoggerFactory.getLogger(Layout.class);

	public Layout() {
		super();
		addText(tr("layout-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("Containers", this.containers()).setPathComponent("");
		menu.addItem("HTML Templates", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return Layout.this.templates();
					}
				}));
		menu.addItem("Text", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return Layout.this.text();
			}
		}));
		menu.addItem("Grouping widgets", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return Layout.this.grouping();
					}
				}));
		menu.addItem("Layout managers", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return Layout.this.layoutManagers();
					}
				}));
		menu.addItem("Dialogs", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return Layout.this.dialogs();
			}
		}));
		menu.addItem("Images", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return Layout.this.images();
			}
		}));
		menu.addItem("CSS", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return Layout.this.css();
			}
		}));
		menu.addItem("Themes", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return Layout.this.themes();
			}
		}));
	}

	private WWidget containers() {
		WTemplate result = new TopicTemplate("layout-Containers");
		result.bindWidget("Container", Container());
		return result;
	}

	private WWidget templates() {
		WTemplate result = new TopicTemplate("layout-Template");
		result.bindWidget("Template", Template());
		result.bindString("template-text", reindent(tr("WTemplate-example")),
				TextFormat.PlainText);
		return result;
	}

	private WWidget text() {
		WTemplate result = new TopicTemplate("layout-Text");
		result.bindWidget("TextPlain", TextPlain());
		result.bindWidget("TextXHTML", TextXHTML());
		result.bindWidget("TextXSS", TextXSS());
		result.bindWidget("TextEvents", TextEvents());
		return result;
	}

	private WWidget grouping() {
		WTemplate result = new TopicTemplate("layout-Grouping");
		result.bindWidget("GroupBox", GroupBox());
		result.bindWidget("PanelNoTitle", PanelNoTitle());
		result.bindWidget("Panel", Panel());
		result.bindWidget("PanelCollapsible", PanelCollapsible());
		return result;
	}

	private WWidget layoutManagers() {
		WTemplate result = new TopicTemplate("layout-Managers");
		result.bindWidget("HBoxLayout", HBoxLayout());
		result.bindWidget("HBoxLayoutStretch", HBoxLayoutStretch());
		result.bindWidget("VBoxLayout", VBoxLayout());
		result.bindWidget("VBoxLayoutStretch", VBoxLayoutStretch());
		result.bindWidget("NestedLayout", NestedLayout());
		result.bindWidget("GridLayout", GridLayout());
		result.bindWidget("BorderLayout", BorderLayout());
		return result;
	}

	private WWidget dialogs() {
		WTemplate result = new TopicTemplate("layout-Dialogs");
		result.bindWidget("Dialog", Dialog());
		result.bindWidget("MessageBox", MessageBox());
		result.bindWidget("MessageBoxSync", MessageBoxSync());
		return result;
	}

	private WWidget images() {
		WTemplate result = new TopicTemplate("layout-Images");
		result.bindWidget("Image", Image());
		result.bindWidget("ImageArea", ImageArea());
		return result;
	}

	private WWidget css() {
		WTemplate result = new TopicTemplate("layout-CSS");
		result.bindWidget("CSS", CSS());
		result.bindWidget("SizingBlock", SizingBlock());
		result.bindWidget("SizingRelative", SizingRelative());
		result.bindWidget("SizingGrid", SizingGrid());
		result.bindString("CSS-example-style",
				reindent(tr("CSS-example-style")), TextFormat.PlainText);
		return result;
	}

	private WWidget themes() {
		WTemplate result = new TopicTemplate("layout-Themes");
		result.bindWidget("Theme", Theme());
		return result;
	}

	// private WWidget loadingIndicator() ;
	// private void loadingIndicatorSelected(CharSequence indicator) ;
	// private void load(WMouseEvent anon1) ;
	WWidget Container() {
		WContainerWidget container = new WContainerWidget();
		container.addWidget(new WText("A first widget"));
		for (int i = 0; i < 3; ++i) {
			new WText(new WString("<p>Text {1}</p>").arg(i), container);
		}
		return container;
	}

	WWidget Template() {
		WTemplate t = new WTemplate(WString.tr("WTemplate-example"));
		t.bindWidget("name-edit", new WLineEdit());
		t.bindWidget("save-button", new WPushButton("Save"));
		t.bindWidget("cancel-button", new WPushButton("Cancel"));
		return t;
	}

	WWidget TextPlain() {
		WText text = new WText(
				"This is an example of plain text. Any contained special XHTML characters, such as \"<\" and \">\", are automatically escaped.",
				TextFormat.PlainText);
		return text;
	}

	WWidget TextXHTML() {
		WText text = new WText(
				"This is <b>XHTML</b> markup text. It supports a safe subset of XHTML tags and attributes, which have only decorative functions.");
		return text;
	}

	WWidget TextXSS() {
		WText text = new WText(
				"<p>This XHTML text contains JavaScript, wich is filtered by the XSS filter.</p><script>alert(\"XSS Attack!\");</script><p>A warning is printed in the logs.</p>");
		return text;
	}

	WWidget TextEvents() {
		WContainerWidget container = new WContainerWidget();
		WText text1 = new WText("This text reacts to <tt>clicked()</tt>",
				container);
		text1.setStyleClass("reactive");
		WText text2 = new WText("This text reacts to <tt>doubleClicked()</tt>",
				container);
		text2.setStyleClass("reactive");
		WText text3 = new WText("This text reacts to <tt>mouseWentOver()</tt>",
				container);
		text3.setStyleClass("reactive");
		WText text4 = new WText("This text reacts to <tt>mouseWentOut()</tt>",
				container);
		text4.setStyleClass("reactive");
		final WText out = new WText(container);
		text1.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("<p>Text was clicked.</p>");
			}
		});
		text2.doubleClicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("<p>Text was double clicked.</p>");
			}
		});
		text3.mouseWentOver().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("<p>Mouse went over text.</p>");
			}
		});
		text4.mouseWentOut().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("<p>Mouse went out text.</p>");
			}
		});
		return container;
	}

	WWidget GroupBox() {
		WGroupBox groupBox = new WGroupBox("A group box");
		groupBox.addStyleClass("centered-example");
		groupBox.addWidget(new WText("<p>Some contents.</p>"));
		groupBox.addWidget(new WText("<p>More contents.</p>"));
		return groupBox;
	}

	WWidget PanelNoTitle() {
		WPanel panel = new WPanel();
		panel.addStyleClass("centered-example");
		panel.setCentralWidget(new WText("This is a default panel."));
		return panel;
	}

	WWidget Panel() {
		WPanel panel = new WPanel();
		panel.addStyleClass("centered-example");
		panel.setTitle("Terrific panel");
		panel.setCentralWidget(new WText("This is a panel with a title."));
		return panel;
	}

	WWidget PanelCollapsible() {
		WPanel panel = new WPanel();
		panel.setTitle("Collapsible panel");
		panel.addStyleClass("centered-example");
		panel.setCollapsible(true);
		WAnimation animation = new WAnimation(
				WAnimation.AnimationEffect.SlideInFromTop,
				WAnimation.TimingFunction.EaseOut, 100);
		panel.setAnimation(animation);
		panel.setCentralWidget(new WText("This panel can be collapsed."));
		return panel;
	}

	WWidget HBoxLayout() {
		WContainerWidget container = new WContainerWidget();
		container.setStyleClass("yellow-box");
		WHBoxLayout hbox = new WHBoxLayout();
		container.setLayout(hbox);
		WText item = new WText("Item 1");
		item.setStyleClass("green-box");
		hbox.addWidget(item);
		item = new WText("Item 2");
		item.setStyleClass("blue-box");
		hbox.addWidget(item);
		return container;
	}

	WWidget HBoxLayoutStretch() {
		WContainerWidget container = new WContainerWidget();
		container.setStyleClass("yellow-box");
		WHBoxLayout hbox = new WHBoxLayout();
		container.setLayout(hbox);
		WText item = new WText("Item 1");
		item.setStyleClass("green-box");
		hbox.addWidget(item, 1);
		item = new WText("Item 2");
		item.setStyleClass("blue-box");
		hbox.addWidget(item);
		return container;
	}

	WWidget VBoxLayout() {
		WContainerWidget container = new WContainerWidget();
		container.resize(new WLength(150), new WLength(150));
		container.setStyleClass("yellow-box centered");
		WVBoxLayout vbox = new WVBoxLayout();
		container.setLayout(vbox);
		WText item = new WText("Item 1");
		item.setStyleClass("green-box");
		vbox.addWidget(item);
		item = new WText("Item 2");
		item.setStyleClass("blue-box");
		vbox.addWidget(item);
		return container;
	}

	WWidget VBoxLayoutStretch() {
		WContainerWidget container = new WContainerWidget();
		container.resize(new WLength(150), new WLength(150));
		container.setStyleClass("yellow-box centered");
		WVBoxLayout vbox = new WVBoxLayout();
		container.setLayout(vbox);
		WText item = new WText("Item 1");
		item.setStyleClass("green-box");
		vbox.addWidget(item, 1);
		item = new WText("Item 2");
		item.setStyleClass("blue-box");
		vbox.addWidget(item);
		return container;
	}

	WWidget NestedLayout() {
		WContainerWidget container = new WContainerWidget();
		container.resize(new WLength(200), new WLength(200));
		container.setStyleClass("yellow-box centered");
		WVBoxLayout vbox = new WVBoxLayout();
		container.setLayout(vbox);
		WText item = new WText("Item 1");
		item.setStyleClass("green-box");
		vbox.addWidget(item, 1);
		WHBoxLayout hbox = new WHBoxLayout();
		vbox.addLayout(hbox);
		item = new WText("Item 2");
		item.setStyleClass("green-box");
		hbox.addWidget(item);
		item = new WText("Item 3");
		item.setStyleClass("blue-box");
		hbox.addWidget(item);
		return container;
	}

	WWidget GridLayout() {
		WContainerWidget container = new WContainerWidget();
		container.setHeight(new WLength(400));
		container.setStyleClass("yellow-box");
		WGridLayout grid = new WGridLayout();
		container.setLayout(grid);
		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 4; ++column) {
				WString cell = new WString("Item ({1}, {2})").arg(row).arg(
						column);
				WText t = new WText(cell);
				if (row == 1 || column == 1 || column == 2) {
					t.setStyleClass("blue-box");
				} else {
					t.setStyleClass("green-box");
				}
				grid.addWidget(t, row, column);
			}
		}
		grid.setRowStretch(1, 1);
		grid.setColumnStretch(1, 1);
		grid.setColumnStretch(2, 1);
		return container;
	}

	WWidget BorderLayout() {
		WContainerWidget container = new WContainerWidget();
		container.setHeight(new WLength(400));
		container.setStyleClass("yellow-box");
		WBorderLayout layout = new WBorderLayout();
		container.setLayout(layout);
		String cell = "{1} item";
		WText item = new WText(new WString(cell).arg("North"));
		item.setStyleClass("green-box");
		layout.addWidget(item, WBorderLayout.Position.North);
		item = new WText(new WString(cell).arg("West"));
		item.setStyleClass("green-box");
		layout.addWidget(item, WBorderLayout.Position.West);
		item = new WText(new WString(cell).arg("East"));
		item.setStyleClass("green-box");
		layout.addWidget(item, WBorderLayout.Position.East);
		item = new WText(new WString(cell).arg("South"));
		item.setStyleClass("green-box");
		layout.addWidget(item, WBorderLayout.Position.South);
		item = new WText(new WString(cell).arg("Center"));
		item.setStyleClass("green-box");
		layout.addWidget(item, WBorderLayout.Position.Center);
		return container;
	}

	final void showDialog(final WText out) {
		final WDialog dialog = new WDialog("Go to cell");
		WLabel label = new WLabel("Cell location (A1..Z999)", dialog
				.getContents());
		final WLineEdit edit = new WLineEdit(dialog.getContents());
		label.setBuddy(edit);
		WRegExpValidator validator = new WRegExpValidator(
				"[A-Za-z][1-9][0-9]{0,2}");
		edit.setValidator(validator);
		final WPushButton ok = new WPushButton("OK", dialog.getFooter());
		WPushButton cancel = new WPushButton("Cancel", dialog.getFooter());
		ok.disable();
		edit.keyWentUp().addListener(this, new Signal.Listener() {
			public void trigger() {
				ok.setDisabled(edit.validate() != WValidator.State.Valid);
			}
		});
		ok.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (edit.validate() != null) {
					dialog.accept();
				}
			}
		});
		edit.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (edit.validate() != null) {
					dialog.accept();
				}
			}
		});
		cancel.clicked().addListener(dialog,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						dialog.reject();
					}
				});
		dialog.finished().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (dialog.getResult() == WDialog.DialogCode.Accepted) {
					out.setText("New location: " + edit.getText());
				} else {
					out.setText("No location selected.");
				}
				if (dialog != null)
					dialog.remove();
			}
		});
		dialog.show();
	}

	WWidget Dialog() {
		WContainerWidget container = new WContainerWidget();
		WPushButton button = new WPushButton("Jump", container);
		final WText out = new WText(container);
		out.setStyleClass("help-inline");
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				showDialog(out);
			}
		});
		return container;
	}

	WWidget MessageBox() {
		WContainerWidget container = new WContainerWidget();
		WPushButton button = new WPushButton("Status", container);
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Left));
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("The status button is clicked.");
				final WMessageBox messageBox = new WMessageBox(
						"Status",
						"<p>Ready to launch the rocket...</p><p>Launch the rocket immediately?</p>",
						Icon.Information, EnumSet.of(StandardButton.Yes,
								StandardButton.No));
				messageBox.setModal(false);
				messageBox.buttonClicked().addListener(messageBox,
						new Signal.Listener() {
							public void trigger() {
								if (messageBox.getButtonResult() == StandardButton.Yes) {
									out.setText("The rocket is launched!");
								} else {
									out
											.setText("The rocket is ready for launch...");
								}
								if (messageBox != null)
									messageBox.remove();
							}
						});
				messageBox.show();
			}
		});
		return container;
	}

	WWidget MessageBoxSync() {
		WContainerWidget container = new WContainerWidget();
		WPushButton button = new WPushButton("Start", container);
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Left));
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				StandardButton answer = WMessageBox.show("Launch phase",
						"<p>Launch the rocket?</p>", EnumSet.of(
								StandardButton.Ok, StandardButton.Cancel));
				if (answer == StandardButton.Ok) {
					out.setText("The rocket is launched!");
				} else {
					out.setText("Waiting on your decision...");
				}
			}
		});
		return container;
	}

	WWidget Image() {
		WContainerWidget container = new WContainerWidget();
		WImage image = new WImage(new WLink("icons/wt_powered.jpg"), container);
		image.setAlternateText("Wt logo");
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Left));
		image.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e) {
				out.setText("You clicked the Wt logo at ("
						+ String.valueOf(e.getWidget().x) + ","
						+ String.valueOf(e.getWidget().y) + ").");
			}
		});
		return container;
	}

	WWidget ImageArea() {
		WContainerWidget container = new WContainerWidget();
		WImage image = new WImage(new WLink("pics/sintel_trailer.jpg"),
				container);
		image.setAlternateText("Sintel trailer");
		new WBreak(container);
		final WText out = new WText(container);
		WCircleArea circle = new WCircleArea(427, 149, 58);
		circle.setToolTip("tree");
		circle.setCursor(Cursor.CrossCursor);
		image.addArea(circle);
		WRectArea rect = new WRectArea(294, 226, 265, 41);
		rect.setToolTip("title");
		rect.setCursor(Cursor.CrossCursor);
		image.addArea(rect);
		WPolygonArea polygon = new WPolygonArea();
		List<WPoint> points = new ArrayList<WPoint>();
		points.add(new WPoint(92, 330));
		points.add(new WPoint(66, 261));
		points.add(new WPoint(122, 176));
		points.add(new WPoint(143, 33));
		points.add(new WPoint(164, 33));
		points.add(new WPoint(157, 88));
		points.add(new WPoint(210, 90));
		points.add(new WPoint(263, 264));
		points.add(new WPoint(228, 330));
		points.add(new WPoint(92, 330));
		polygon.setPoints(points);
		polygon.setToolTip("person");
		polygon.setCursor(Cursor.CrossCursor);
		image.addArea(polygon);
		circle.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("You clicked the tree.");
			}
		});
		rect.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("You clicked the title.");
			}
		});
		polygon.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("You clicked the person.");
			}
		});
		image.mouseMoved().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e) {
						out.setText("You're pointing the background at ("
								+ String.valueOf(e.getWidget().x) + ","
								+ String.valueOf(e.getWidget().y) + ").");
					}
				});
		return container;
	}

	WWidget SizingBlock() {
		WContainerWidget container = new WContainerWidget();
		WLineEdit edit = new WLineEdit(container);
		edit.setStyleClass("input-block-level");
		edit
				.setEmptyText("This is a line edit with style class .input-block-level applied to it.");
		WComboBox combo = new WComboBox(container);
		combo.setStyleClass("input-block-level");
		for (int i = 1; i < 5; ++i) {
			combo
					.addItem("Combo box with style class .input-block-level - item"
							+ String.valueOf(i));
		}
		WTextArea area = new WTextArea(container);
		area.setStyleClass("input-block-level");
		area
				.setEmptyText("This is a text area with style class .input-block-level applied to it.");
		return container;
	}

	WWidget SizingRelative() {
		WContainerWidget container = new WContainerWidget();
		WLineEdit edit = new WLineEdit(container);
		edit.setEmptyText(".input-mini");
		edit.setStyleClass("input-mini");
		new WBreak(container);
		edit = new WLineEdit(container);
		edit.setEmptyText(".input-small");
		edit.setStyleClass("input-small");
		new WBreak(container);
		edit = new WLineEdit(container);
		edit.setEmptyText(".input-medium");
		edit.setStyleClass("input-medium");
		new WBreak(container);
		edit = new WLineEdit(container);
		edit.setEmptyText(".input-large");
		edit.setStyleClass("input-large");
		new WBreak(container);
		edit = new WLineEdit(container);
		edit.setEmptyText(".input-xlarge");
		edit.setStyleClass("input-xlarge");
		new WBreak(container);
		edit = new WLineEdit(container);
		edit.setEmptyText(".input-xxlarge");
		edit.setStyleClass("input-xxlarge");
		return container;
	}

	WWidget SizingGrid() {
		WContainerWidget parentContainer = new WContainerWidget();
		WLineEdit edit = new WLineEdit(parentContainer);
		edit.setEmptyText(".span8");
		edit.setStyleClass("span8");
		WContainerWidget childContainer = new WContainerWidget();
		childContainer.setStyleClass("controls-row");
		edit = new WLineEdit(childContainer);
		edit.setEmptyText(".span1");
		edit.setStyleClass("span1");
		edit = new WLineEdit(childContainer);
		edit.setEmptyText(".span2");
		edit.setStyleClass("span2");
		edit = new WLineEdit(childContainer);
		edit.setEmptyText(".span3");
		edit.setStyleClass("span3");
		edit = new WLineEdit(childContainer);
		edit.setEmptyText(".span2");
		edit.setStyleClass("span2");
		parentContainer.addWidget(childContainer);
		return parentContainer;
	}

	WWidget CSS() {
		WApplication.getInstance().useStyleSheet(
				new WLink("style/CSSexample.css"));
		WContainerWidget container = new WContainerWidget();
		container.setStyleClass("CSS-example");
		WPushButton allB = new WPushButton("Set all classes", container);
		final WPushButton removeB = new WPushButton("Remove info class",
				container);
		removeB.setMargin(new WLength(10), EnumSet.of(Side.Left, Side.Right));
		removeB.disable();
		final WPushButton toggleB = new WPushButton("Toggle condensed",
				container);
		toggleB.disable();
		WText text = new WText(container);
		text
				.setText("<p>These are the most import API classes and methods for working with CSS:</p>");
		final WTable table = new WTable(container);
		table.setHeaderCount(1);
		table.getElementAt(0, 0).addWidget(new WText("Method"));
		table.getElementAt(0, 1).addWidget(new WText("Description"));
		table.getElementAt(1, 0).addWidget(
				new WText("WApplication::useStyleSheet()"));
		table.getElementAt(1, 1).addWidget(
				new WText("Adds an external style sheet"));
		table.getElementAt(2, 0).addWidget(
				new WText("WWidget::setStyleClass()"));
		table.getElementAt(2, 1).addWidget(
				new WText("Sets (one or more) CSS style classes"));
		table.getElementAt(3, 0).addWidget(
				new WText("WWidget::removeStyleClass()"));
		table.getElementAt(3, 1).addWidget(
				new WText("Removes a CSS style class"));
		table.getElementAt(4, 0).addWidget(
				new WText("WWidget::toggleStyleClass()"));
		table.getElementAt(4, 1).addWidget(
				new WText("Toggles a CSS style class"));
		allB.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				table.setStyleClass("table table-bordered");
				table.getRowAt(1).setStyleClass("info");
				for (int i = 1; i < table.getRowCount(); i++) {
					table.getElementAt(i, 0).setStyleClass("code");
				}
				removeB.enable();
				toggleB.enable();
			}
		});
		removeB.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				table.getRowAt(1).removeStyleClass("info");
				removeB.disable();
			}
		});
		toggleB.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (toggleB.getText().equals("Toggle condensed")) {
					table.toggleStyleClass("table-condensed", true);
					toggleB.setText("Toggle expanded");
				} else {
					table.toggleStyleClass("table-condensed", false);
					toggleB.setText("Toggle condensed");
				}
			}
		});
		return container;
	}

	WWidget Theme() {
		WContainerWidget container = new WContainerWidget();
		WText out = new WText(container);
		out.setText("some text");
		return container;
	}
}
