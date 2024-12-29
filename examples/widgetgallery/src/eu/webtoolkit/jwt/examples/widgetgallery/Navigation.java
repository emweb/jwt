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

class Navigation extends Topic {
  private static Logger logger = LoggerFactory.getLogger(Navigation.class);

  public Navigation() {
    super();
  }

  public void populateSubMenu(WMenu menu) {
    menu.addItem(
            "Internal paths",
            DeferredWidget.deferCreate(
                () -> {
                  return Navigation.this.internalPaths();
                }))
        .setPathComponent("");
    menu.addItem(
        "Anchor",
        DeferredWidget.deferCreate(
            () -> {
              return Navigation.this.anchor();
            }));
    menu.addItem(
        "Stacked widget",
        DeferredWidget.deferCreate(
            () -> {
              return Navigation.this.stackedWidget();
            }));
    menu.addItem(
        "Menu",
        DeferredWidget.deferCreate(
            () -> {
              return Navigation.this.menuWidget();
            }));
    menu.addItem(
        "Tab widget",
        DeferredWidget.deferCreate(
            () -> {
              return Navigation.this.tabWidget();
            }));
    menu.addItem(
        "Navigation bar",
        DeferredWidget.deferCreate(
            () -> {
              return Navigation.this.navigationBar();
            }));
    menu.addItem(
        "Popup menu",
        DeferredWidget.deferCreate(
            () -> {
              return Navigation.this.popupMenu();
            }));
    menu.addItem(
        "Split button",
        DeferredWidget.deferCreate(
            () -> {
              return Navigation.this.splitButton();
            }));
    menu.addItem(
        "Toolbar",
        DeferredWidget.deferCreate(
            () -> {
              return Navigation.this.toolBar();
            }));
  }

  private WWidget internalPaths() {
    TopicTemplate result = new TopicTemplate("navigation-internalPaths");
    result.bindWidget("Path", Path());
    result.bindWidget("PathChange", PathChange());
    return result;
  }

  private WWidget anchor() {
    TopicTemplate result = new TopicTemplate("navigation-anchor");
    result.bindWidget("Anchor", Anchor());
    result.bindWidget("AnchorImage", AnchorImage());
    return result;
  }

  private WWidget stackedWidget() {
    TopicTemplate result = new TopicTemplate("navigation-stackedWidget");
    result.bindWidget("Stack", Stack());
    return result;
  }

  private WWidget tabWidget() {
    TopicTemplate result = new TopicTemplate("navigation-tabWidget");
    result.bindWidget("Tab", Tab());
    return result;
  }

  private WWidget menuWidget() {
    TopicTemplate result = new TopicTemplate("navigation-menu");
    result.bindWidget("Menu", Menu());
    return result;
  }

  private WWidget navigationBar() {
    TopicTemplate result = new TopicTemplate("navigation-navigationBar");
    result.bindWidget("NavigationBar", NavigationBar());
    return result;
  }

  private WWidget popupMenu() {
    TopicTemplate result = new TopicTemplate("navigation-popupMenu");
    result.bindWidget("Popup", Popup());
    return result;
  }

  private WWidget splitButton() {
    TopicTemplate result = new TopicTemplate("navigation-splitButton");
    result.bindWidget("SplitButton", SplitButton());
    return result;
  }

  private WWidget toolBar() {
    TopicTemplate result = new TopicTemplate("navigation-toolBar");
    result.bindWidget("ToolBar", ToolBar());
    return result;
  }

  WWidget Path() {
    WPushButton button = new WPushButton("Next");
    button.setLink(new WLink(LinkType.InternalPath, "/navigation/anchor"));
    return button;
  }

  static final void handlePathChange(WText out) {
    WApplication app = WApplication.getInstance();
    if (app.getInternalPath().equals("/navigation/shop")) {
      out.setText("<p>Currently shopping.</p>");
    } else {
      if (app.getInternalPath().equals("/navigation/eat")) {
        out.setText("<p>Needed some food, eating now!</p>");
      } else {
        out.setText("<p><i>Idle.</i></p>");
      }
    }
  }

  WWidget PathChange() {
    WContainerWidget container = new WContainerWidget();
    new WAnchor(
        new WLink(LinkType.InternalPath, "/navigation/shop"), "Shop", (WContainerWidget) container);
    new WText(" ", (WContainerWidget) container);
    new WAnchor(
        new WLink(LinkType.InternalPath, "/navigation/eat"), "Eat", (WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    out.setInline(false);
    WApplication app = WApplication.getInstance();
    app.internalPathChanged()
        .addListener(
            this,
            () -> {
              handlePathChange(out);
            });
    handlePathChange(out);
    return container;
  }

  WWidget Anchor() {
    WLink link = new WLink("https://www.webtoolkit.eu/");
    link.setTarget(LinkTarget.NewWindow);
    WAnchor anchor = new WAnchor(link, "Wt homepage (in a new window)");
    return anchor;
  }

  WWidget AnchorImage() {
    WLink link = new WLink("https://www.emweb.be/");
    link.setTarget(LinkTarget.NewWindow);
    WAnchor anchor = new WAnchor(link);
    new WImage(new WLink("https://www.emweb.be/css/emweb_small.png"), (WContainerWidget) anchor);
    return anchor;
  }

  WWidget Stack() {
    WContainerWidget container = new WContainerWidget();
    final WSpinBox sb = new WSpinBox((WContainerWidget) container);
    sb.setRange(0, 2);
    final WStackedWidget stack = new WStackedWidget((WContainerWidget) container);
    new WText("<strong>Stacked widget-index 0</strong><p>Hello</p>", (WContainerWidget) stack);
    new WText("<strong>Stacked widget-index 1</strong><p>This is Wt</p>", (WContainerWidget) stack);
    new WText(
        "<strong>Stacked widget-index 2</strong><p>Do you like it?</p>", (WContainerWidget) stack);
    sb.changed()
        .addListener(
            this,
            () -> {
              if (sb.validate() == ValidationState.Valid) {
                stack.setCurrentIndex(sb.getValue());
              }
            });
    return container;
  }

  WWidget Menu() {
    WContainerWidget container = new WContainerWidget();
    WStackedWidget contents = new WStackedWidget();
    WMenu menu = new WMenu(contents, (WContainerWidget) container);
    menu.setStyleClass("nav nav-pills flex-column");
    menu.setWidth(new WLength(150));
    menu.addItem("Internal paths", new WTextArea("Internal paths contents"));
    menu.addItem("Anchor", new WTextArea("Anchor contents"));
    menu.addItem("Stacked widget", new WTextArea("Stacked widget contents"));
    menu.addItem("Tab widget", new WTextArea("Tab widget contents"));
    menu.addItem("Menu", new WTextArea("Menu contents"));
    container.addWidget(contents);
    return container;
  }

  WWidget Tab() {
    WContainerWidget container = new WContainerWidget();
    WTabWidget tabW = new WTabWidget((WContainerWidget) container);
    tabW.addTab(
        new WTextArea("This is the contents of the first tab."), "First", ContentLoading.Eager);
    tabW.addTab(
        new WTextArea(
            "The contents of the tabs are pre-loaded in the browser to ensure swift switching."),
        "Preload",
        ContentLoading.Eager);
    tabW.addTab(
            new WTextArea(
                "You could change any other style attribute of the tab widget by modifying the style class. The style class 'trhead' is applied to this tab."),
            "Style",
            ContentLoading.Eager)
        .setStyleClass("trhead");
    WMenuItem tab =
        tabW.addTab(
            new WTextArea("You can close this tab by clicking on the close icon."), "Close");
    tab.setCloseable(true);
    tabW.setStyleClass("tabwidget");
    return container;
  }

  WWidget NavigationBar() {
    WContainerWidget container = new WContainerWidget();
    WNavigationBar navigation = new WNavigationBar((WContainerWidget) container);
    navigation.setResponsive(true);
    navigation.addStyleClass("navbar-light bg-light");
    navigation.setTitle("Corpy Inc.", new WLink("https://www.google.com/search?q=corpy+inc"));
    WStackedWidget contentsStack = new WStackedWidget((WContainerWidget) container);
    contentsStack.addStyleClass("contents");
    WMenu leftMenu = new WMenu(contentsStack);
    final WMenu leftMenu_ = navigation.addMenu(leftMenu);
    WText searchResult = new WText("Buy or Sell... Bye!");
    final WText searchResult_ = searchResult;
    leftMenu_.addItem("Home", new WText("There is no better place!"));
    leftMenu_
        .addItem("Layout", new WText("Layout contents"))
        .setLink(new WLink(LinkType.InternalPath, "/layout"));
    leftMenu_.addItem("Sales", searchResult);
    leftMenu_.addStyleClass("me-auto");
    WLineEdit editPtr = new WLineEdit();
    final WLineEdit edit = editPtr;
    edit.setPlaceholderText("Enter a search item");
    edit.enterPressed()
        .addListener(
            this,
            () -> {
              leftMenu_.select(2);
              searchResult_.setText(new WString("Nothing found for {1}.").arg(edit.getText()));
            });
    navigation.addSearch(editPtr);
    WMenu rightMenu = new WMenu();
    WMenu rightMenu_ = navigation.addMenu(rightMenu);
    WPopupMenu popupPtr = new WPopupMenu();
    WPopupMenu popup = popupPtr;
    popup.addItem("Contents");
    popup.addItem("Index");
    popup.addSeparator();
    popup.addItem("About");
    popup
        .itemSelected()
        .addListener(
            this,
            (WMenuItem item) -> {
              final WMessageBox messageBox =
                  new WMessageBox(
                      "Help",
                      new WString("<p>Showing Help: {1}</p>").arg(item.getText()),
                      Icon.Information,
                      EnumSet.of(StandardButton.Ok));
              messageBox
                  .buttonClicked()
                  .addListener(
                      this,
                      () -> {
                        if (messageBox != null) messageBox.remove();
                      });
              messageBox.show();
            });
    WMenuItem item = new WMenuItem("Help");
    item.setMenu(popupPtr);
    rightMenu_.addItem(item);
    return container;
  }

  WWidget Popup() {
    WContainerWidget container = new WContainerWidget();
    WPopupMenu popupPtr = new WPopupMenu();
    WPopupMenu popup = popupPtr;
    final WText status = new WText();
    status.setMargin(new WLength(10), EnumSet.of(Side.Left, Side.Right));
    final WText out = new WText();
    popup
        .addItem("Connect")
        .triggered()
        .addListener(
            this,
            () -> {
              out.setText("<p>Connecting...</p>");
            });
    popup
        .addItem("Disconnect")
        .triggered()
        .addListener(
            this,
            () -> {
              out.setText("<p>You are disconnected now.</p>");
            });
    popup.addSeparator();
    popup
        .addItem("icons/house.png", "I'm home")
        .triggered()
        .addListener(
            this,
            () -> {
              out.setText("");
            });
    final WMenuItem item = popup.addItem("Don't disturb");
    item.setCheckable(true);
    item.triggered()
        .addListener(
            this,
            () -> {
              out.setText(
                  new WString("<p>{1} item is {2}.</p>")
                      .arg(item.getText())
                      .arg(item.isChecked() ? "checked" : "unchecked"));
            });
    popup.addSeparator();
    WPopupMenu subMenuPtr = new WPopupMenu();
    WPopupMenu subMenu = subMenuPtr;
    subMenu
        .addItem("Contents")
        .triggered()
        .addListener(
            this,
            () -> {
              out.setText("<p>This could be a link to /contents.html.</p>");
            });
    subMenu
        .addItem("Index")
        .triggered()
        .addListener(
            this,
            () -> {
              out.setText("<p>This could be a link to /index.html.</p>");
            });
    subMenu.addSeparator();
    subMenu
        .addItem("About")
        .triggered()
        .addListener(
            this,
            () -> {
              final WMessageBox messageBox =
                  new WMessageBox(
                      "About",
                      "<p>This is a program to make connections.</p>",
                      Icon.Information,
                      EnumSet.of(StandardButton.Ok));
              messageBox.show();
              messageBox
                  .buttonClicked()
                  .addListener(
                      this,
                      () -> {
                        if (messageBox != null) messageBox.remove();
                      });
            });
    popup.addMenu("Help", subMenuPtr);
    WPushButton button = new WPushButton((WContainerWidget) container);
    button.setMenu(popupPtr);
    popup
        .itemSelected()
        .addListener(
            this,
            (WMenuItem selectedItem) -> {
              status.setText(new WString("Selected menu item: {1}.").arg(selectedItem.getText()));
            });
    container.addWidget(status);
    container.addWidget(out);
    return container;
  }

  WWidget SplitButton() {
    WContainerWidget container = new WContainerWidget();
    WSplitButton sb = new WSplitButton("Save", (WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    out.setMargin(new WLength(10), EnumSet.of(Side.Left));
    WPopupMenu popup = new WPopupMenu();
    WPopupMenu popup_ = popup;
    popup_.addItem("Save As ...");
    popup_.addItem("Save Template");
    sb.getDropDownButton().setMenu(popup);
    sb.getActionButton()
        .clicked()
        .addListener(
            this,
            () -> {
              out.setText("Saved!");
            });
    popup_
        .itemSelected()
        .addListener(
            this,
            (WMenuItem item) -> {
              out.setText(item.getText());
            });
    return container;
  }

  WPushButton createColorButton(String className, final CharSequence text) {
    WPushButton button = new WPushButton();
    button.setTextFormat(TextFormat.XHTML);
    button.setText(text);
    button.addStyleClass(className);
    return button;
  }

  WWidget ToolBar() {
    WContainerWidget container = new WContainerWidget();
    WToolBar toolBar = new WToolBar((WContainerWidget) container);
    toolBar.addButton(createColorButton("btn-primary", "Primary"));
    toolBar.addButton(createColorButton("btn-danger", "Danger"));
    toolBar.addButton(createColorButton("btn-success", "Success"));
    toolBar.addButton(createColorButton("btn-warning", "Warning"));
    toolBar.addButton(createColorButton("btn-inverse", "Inverse"));
    toolBar.addButton(createColorButton("", "Default"));
    WPushButton resetButton = new WPushButton("Reset");
    toolBar.addSeparator();
    toolBar.addButton(resetButton);
    return container;
  }
}
