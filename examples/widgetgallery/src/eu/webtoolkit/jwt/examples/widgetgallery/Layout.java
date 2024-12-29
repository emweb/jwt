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

class Layout extends Topic {
  private static Logger logger = LoggerFactory.getLogger(Layout.class);

  public Layout() {
    super();
  }

  public void populateSubMenu(WMenu menu) {
    menu.addItem(
            "Containers",
            DeferredWidget.deferCreate(
                () -> {
                  return Layout.this.containers();
                }))
        .setPathComponent("");
    menu.addItem(
        "HTML Templates",
        DeferredWidget.deferCreate(
            () -> {
              return Layout.this.templates();
            }));
    menu.addItem(
        "Text",
        DeferredWidget.deferCreate(
            () -> {
              return Layout.this.text();
            }));
    menu.addItem(
        "Grouping widgets",
        DeferredWidget.deferCreate(
            () -> {
              return Layout.this.grouping();
            }));
    menu.addItem(
        "Layout managers",
        DeferredWidget.deferCreate(
            () -> {
              return Layout.this.layoutManagers();
            }));
    menu.addItem(
        "Dialogs",
        DeferredWidget.deferCreate(
            () -> {
              return Layout.this.dialogs();
            }));
    menu.addItem(
        "Images",
        DeferredWidget.deferCreate(
            () -> {
              return Layout.this.images();
            }));
    menu.addItem(
        "CSS",
        DeferredWidget.deferCreate(
            () -> {
              return Layout.this.css();
            }));
    menu.addItem(
        "Themes",
        DeferredWidget.deferCreate(
            () -> {
              return Layout.this.themes();
            }));
  }

  private WWidget containers() {
    TopicTemplate result = new TopicTemplate("layout-Containers");
    result.bindWidget("Container", Container());
    return result;
  }

  private WWidget templates() {
    TopicTemplate result = new TopicTemplate("layout-Template");
    result.bindWidget("Template", Template());
    result.bindString("template-text", reindent(WString.tr("WTemplate-example")), TextFormat.Plain);
    return result;
  }

  private WWidget text() {
    TopicTemplate result = new TopicTemplate("layout-Text");
    result.bindWidget("TextPlain", TextPlain());
    result.bindWidget("TextXHTML", TextXHTML());
    result.bindWidget("TextXSS", TextXSS());
    result.bindWidget("TextEvents", TextEvents());
    result.bindWidget("TextToolTip", TextToolTip());
    result.bindWidget("TextDeferredToolTip", TextDeferredToolTip());
    return result;
  }

  private WWidget grouping() {
    TopicTemplate result = new TopicTemplate("layout-Grouping");
    result.bindWidget("GroupBox", GroupBox());
    result.bindWidget("PanelNoTitle", PanelNoTitle());
    result.bindWidget("Panel", Panel());
    result.bindWidget("PanelCollapsible", PanelCollapsible());
    return result;
  }

  private WWidget layoutManagers() {
    TopicTemplate result = new TopicTemplate("layout-Managers");
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
    TopicTemplate result = new TopicTemplate("layout-Dialogs");
    result.bindWidget("Dialog", Dialog());
    result.bindWidget("MessageBox", MessageBox());
    result.bindWidget("MessageBoxSync", MessageBoxSync());
    return result;
  }

  private WWidget images() {
    TopicTemplate result = new TopicTemplate("layout-Images");
    result.bindWidget("Image", Image());
    result.bindWidget("ImageArea", ImageArea());
    return result;
  }

  private WWidget css() {
    TopicTemplate result = new TopicTemplate("layout-CSS");
    result.bindWidget("CSS", CSS());
    result.bindString(
        "CSS-example-style", reindent(WString.tr("CSS-example-style")), TextFormat.Plain);
    return result;
  }

  private WWidget themes() {
    TopicTemplate result = new TopicTemplate("layout-Themes");
    result.bindString("Theme", reindent(WString.tr("theme")), TextFormat.Plain);
    return result;
  }
  // private WWidget  loadingIndicator() ;
  // private void loadingIndicatorSelected(CharSequence indicator) ;
  WWidget Container() {
    WContainerWidget container = new WContainerWidget();
    new WText("A first widget", (WContainerWidget) container);
    for (int i = 0; i < 3; ++i) {
      new WText(new WString("<p>Text {1}</p>").arg(i), (WContainerWidget) container);
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
    WText text =
        new WText(
            "This is an example of plain text. Any contained special XHTML characters, such as \"<\" and \">\", are automatically escaped.",
            TextFormat.Plain);
    return text;
  }

  WWidget TextXHTML() {
    WText text =
        new WText(
            "This is <b>XHTML</b> markup text. It supports a safe subset of XHTML tags and attributes, which have only decorative functions.");
    return text;
  }

  WWidget TextXSS() {
    WText text =
        new WText(
            "<p>This XHTML text contains JavaScript, wich is filtered by the XSS filter.</p><script>alert(\"XSS Attack!\");</script><p>A warning is printed in the logs.</p>");
    return text;
  }

  WWidget TextEvents() {
    WContainerWidget container = new WContainerWidget();
    WText text1 = new WText("This text reacts to <tt>clicked()</tt>", (WContainerWidget) container);
    text1.setStyleClass("reactive");
    WText text2 =
        new WText("This text reacts to <tt>doubleClicked()</tt>", (WContainerWidget) container);
    text2.setStyleClass("reactive");
    WText text3 =
        new WText("This text reacts to <tt>mouseWentOver()</tt>", (WContainerWidget) container);
    text3.setStyleClass("reactive");
    WText text4 =
        new WText("This text reacts to <tt>mouseWentOut()</tt>", (WContainerWidget) container);
    text4.setStyleClass("reactive");
    final WText out = new WText((WContainerWidget) container);
    text1
        .clicked()
        .addListener(
            this,
            () -> {
              out.setText("<p>Text was clicked.</p>");
            });
    text2
        .doubleClicked()
        .addListener(
            this,
            () -> {
              out.setText("<p>Text was double clicked.</p>");
            });
    text3
        .mouseWentOver()
        .addListener(
            this,
            () -> {
              out.setText("<p>Mouse went over text.</p>");
            });
    text4
        .mouseWentOut()
        .addListener(
            this,
            () -> {
              out.setText("<p>Mouse went out text.</p>");
            });
    return container;
  }

  WWidget TextToolTip() {
    WText text = new WText("Some text", TextFormat.Plain);
    text.setToolTip("ToolTip", TextFormat.XHTML);
    return text;
  }

  WWidget TextDeferredToolTip() {
    Text text = new Text();
    text.setText("Text");
    text.setDeferredToolTip(true);
    return text;
  }

  WWidget GroupBox() {
    WGroupBox groupBox = new WGroupBox("A group box");
    groupBox.addStyleClass("centered-example");
    new WText("<p>Some contents.</p>", (WContainerWidget) groupBox);
    new WText("<p>More contents.</p>", (WContainerWidget) groupBox);
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
    WAnimation animation =
        new WAnimation(AnimationEffect.SlideInFromTop, TimingFunction.EaseOut, 100);
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
        WString cell = new WString("Item ({1}, {2})").arg(row).arg(column);
        WText text = new WText(cell);
        if (row == 1 || column == 1 || column == 2) {
          text.setStyleClass("blue-box");
        } else {
          text.setStyleClass("green-box");
        }
        grid.addWidget(text, row, column);
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
    layout.addWidget(item, LayoutPosition.North);
    item = new WText(new WString(cell).arg("West"));
    item.setStyleClass("green-box");
    layout.addWidget(item, LayoutPosition.West);
    item = new WText(new WString(cell).arg("East"));
    item.setStyleClass("green-box");
    layout.addWidget(item, LayoutPosition.East);
    item = new WText(new WString(cell).arg("South"));
    item.setStyleClass("green-box");
    layout.addWidget(item, LayoutPosition.South);
    item = new WText(new WString(cell).arg("Center"));
    item.setStyleClass("green-box");
    layout.addWidget(item, LayoutPosition.Center);
    return container;
  }

  final void showDialog(WObject owner, final WText out) {
    final WDialog dialog = new WDialog("Go to cell");
    WLabel label = new WLabel("Cell location (A1..Z999)", (WContainerWidget) dialog.getContents());
    final WLineEdit edit = new WLineEdit((WContainerWidget) dialog.getContents());
    label.setBuddy(edit);
    dialog.getContents().addStyleClass("form-group");
    WRegExpValidator validator = new WRegExpValidator("[A-Za-z][1-9][0-9]{0,2}");
    validator.setMandatory(true);
    edit.setValidator(validator);
    final WPushButton ok = new WPushButton("OK", (WContainerWidget) dialog.getFooter());
    ok.setDefault(true);
    if (WApplication.getInstance().getEnvironment().hasAjax()) {
      ok.disable();
    }
    WPushButton cancel = new WPushButton("Cancel", (WContainerWidget) dialog.getFooter());
    dialog.rejectWhenEscapePressed();
    edit.keyWentUp()
        .addListener(
            this,
            () -> {
              ok.setDisabled(edit.validate() != ValidationState.Valid);
            });
    ok.clicked()
        .addListener(
            this,
            () -> {
              if (edit.validate() == ValidationState.Valid) {
                dialog.accept();
              }
            });
    cancel
        .clicked()
        .addListener(
            dialog,
            (WMouseEvent e1) -> {
              dialog.reject();
            });
    dialog
        .finished()
        .addListener(
            this,
            () -> {
              if (dialog.getResult() == DialogCode.Accepted) {
                out.setText("New location: " + edit.getText());
              } else {
                out.setText("No location selected.");
              }
              if (dialog != null) dialog.remove();
            });
    dialog.show();
  }

  WWidget Dialog() {
    WContainerWidget container = new WContainerWidget();
    WPushButton button = new WPushButton("Jump", (WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    out.setStyleClass("help-block");
    final WContainerWidget c = container;
    button
        .clicked()
        .addListener(
            this,
            () -> {
              showDialog(c, out);
            });
    return container;
  }

  WWidget MessageBox() {
    WContainerWidget container = new WContainerWidget();
    WPushButton button = new WPushButton("Status", (WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    out.setMargin(new WLength(10), EnumSet.of(Side.Left));
    WContainerWidget c = container;
    button
        .clicked()
        .addListener(
            this,
            () -> {
              out.setText("The status button is clicked.");
              final WMessageBox messageBox =
                  new WMessageBox(
                      "Status",
                      "<p>Ready to launch the rocket...</p><p>Launch the rocket immediately?</p>",
                      Icon.Information,
                      EnumUtils.or(EnumSet.of(StandardButton.Yes), StandardButton.No));
              messageBox.setModal(false);
              messageBox
                  .buttonClicked()
                  .addListener(
                      this,
                      () -> {
                        if (messageBox.getButtonResult() == StandardButton.Yes) {
                          out.setText("The rocket is launched!");
                        } else {
                          out.setText("The rocket is ready for launch...");
                        }
                        if (messageBox != null) messageBox.remove();
                      });
              messageBox.show();
            });
    return container;
  }

  WWidget MessageBoxSync() {
    WContainerWidget container = new WContainerWidget();
    WPushButton button = new WPushButton("Start", (WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    out.setMargin(new WLength(10), EnumSet.of(Side.Left));
    button
        .clicked()
        .addListener(
            this,
            () -> {
              StandardButton answer =
                  WMessageBox.show(
                      "Launch phase",
                      "<p>Launch the rocket?</p>",
                      EnumSet.of(StandardButton.Ok, StandardButton.Cancel));
              if (answer == StandardButton.Ok) {
                out.setText("The rocket is launched!");
              } else {
                out.setText("Waiting on your decision...");
              }
            });
    return container;
  }

  WWidget Image() {
    WContainerWidget container = new WContainerWidget();
    WImage image = new WImage(new WLink("icons/wt.png"), (WContainerWidget) container);
    image.setAlternateText("Wt logo");
    final WText out = new WText((WContainerWidget) container);
    out.setMargin(new WLength(10), EnumSet.of(Side.Left));
    image
        .clicked()
        .addListener(
            this,
            (WMouseEvent e) -> {
              out.setText(
                  "You clicked the Wt logo at ("
                      + String.valueOf(e.getWidget().x)
                      + ","
                      + String.valueOf(e.getWidget().y)
                      + ").");
            });
    return container;
  }

  WWidget ImageArea() {
    WContainerWidget container = new WContainerWidget();
    WImage image = new WImage(new WLink("pics/sintel_trailer.jpg"), (WContainerWidget) container);
    image.setAlternateText("Sintel trailer");
    new WBreak((WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    WCircleArea circlePtr = new WCircleArea(427, 149, 58);
    WCircleArea circle = circlePtr;
    circle.setToolTip("tree");
    circle.setCursor(Cursor.Cross);
    image.addArea(circlePtr);
    WRectArea rectPtr = new WRectArea(294, 226, 265, 41);
    WRectArea rect = rectPtr;
    rect.setToolTip("title");
    rect.setCursor(Cursor.Cross);
    image.addArea(rectPtr);
    WPolygonArea polygonPtr = new WPolygonArea();
    WPolygonArea polygon = polygonPtr;
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
    polygon.setCursor(Cursor.Cross);
    image.addArea(polygonPtr);
    circle
        .clicked()
        .addListener(
            this,
            () -> {
              out.setText("You clicked the tree.");
            });
    rect.clicked()
        .addListener(
            this,
            () -> {
              out.setText("You clicked the title.");
            });
    polygon
        .clicked()
        .addListener(
            this,
            () -> {
              out.setText("You clicked the person.");
            });
    image
        .mouseMoved()
        .addListener(
            this,
            (WMouseEvent e) -> {
              out.setText(
                  "You're pointing the background at ("
                      + String.valueOf(e.getWidget().x)
                      + ","
                      + String.valueOf(e.getWidget().y)
                      + ").");
            });
    return container;
  }

  WWidget CSS() {
    WApplication.getInstance().useStyleSheet(new WLink("style/CSSexample.css"));
    WContainerWidget container = new WContainerWidget();
    container.setStyleClass("CSS-example");
    WPushButton allB = new WPushButton("Set all classes", (WContainerWidget) container);
    final WPushButton removeB = new WPushButton("Remove info class", (WContainerWidget) container);
    removeB.setMargin(new WLength(10), EnumSet.of(Side.Left, Side.Right));
    removeB.disable();
    final WPushButton toggleB = new WPushButton("Toggle compact", (WContainerWidget) container);
    toggleB.disable();
    WText text = new WText((WContainerWidget) container);
    text.setText("<p>These are the most import API classes and methods for working with CSS:</p>");
    final WTable table = new WTable((WContainerWidget) container);
    table.setHeaderCount(1);
    new WText("Method", (WContainerWidget) table.getElementAt(0, 0));
    new WText("Description", (WContainerWidget) table.getElementAt(0, 1));
    new WText("WApplication::useStyleSheet()", (WContainerWidget) table.getElementAt(1, 0));
    new WText("Adds an external style sheet", (WContainerWidget) table.getElementAt(1, 1));
    new WText("WWidget::setStyleClass()", (WContainerWidget) table.getElementAt(2, 0));
    new WText("Sets (one or more) CSS style classes", (WContainerWidget) table.getElementAt(2, 1));
    new WText("WWidget::removeStyleClass()", (WContainerWidget) table.getElementAt(3, 0));
    new WText("Removes a CSS style class", (WContainerWidget) table.getElementAt(3, 1));
    new WText("WWidget::toggleStyleClass()", (WContainerWidget) table.getElementAt(4, 0));
    new WText("Toggles a CSS style class", (WContainerWidget) table.getElementAt(4, 1));
    allB.clicked()
        .addListener(
            this,
            () -> {
              table.setStyleClass("table table-bordered");
              table.getRowAt(1).setStyleClass("info");
              for (int i = 1; i < table.getRowCount(); i++) {
                table.getElementAt(i, 0).setStyleClass("code");
              }
              removeB.enable();
              toggleB.enable();
            });
    removeB
        .clicked()
        .addListener(
            this,
            () -> {
              table.getRowAt(1).removeStyleClass("info");
              removeB.disable();
            });
    toggleB
        .clicked()
        .addListener(
            this,
            () -> {
              if ((toggleB.getText().toString().equals("Toggle compact".toString()))) {
                table.toggleStyleClass("table-sm", true);
                toggleB.setText("Toggle expanded");
              } else {
                table.toggleStyleClass("table-sm", false);
                toggleB.setText("Toggle compact");
              }
            });
    return container;
  }
}
