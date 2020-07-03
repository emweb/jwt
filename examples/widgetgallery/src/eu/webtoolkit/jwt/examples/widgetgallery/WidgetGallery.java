/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
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

class WidgetGallery extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(WidgetGallery.class);

  public WidgetGallery(WContainerWidget parentContainer) {
    super();
    this.setOverflow(Overflow.Hidden);
    WNavigationBar navigation = new WNavigationBar();
    this.navigation_ = navigation;
    this.navigation_.addStyleClass("main-nav");
    this.navigation_.setTitle("Wt Widget Gallery", new WLink("https://www.webtoolkit.eu/widgets"));
    this.navigation_.setResponsive(true);
    WStackedWidget contentsStack = new WStackedWidget();
    this.contentsStack_ = contentsStack;
    WAnimation animation = new WAnimation(AnimationEffect.Fade, TimingFunction.Linear, 200);
    this.contentsStack_.setTransitionAnimation(animation, true);
    WMenu menu = new WMenu(this.contentsStack_);
    menu.setInternalPathEnabled();
    menu.setInternalBasePath("/");
    this.addToMenu(menu, "Layout", new Layout());
    this.addToMenu(menu, "Forms", new FormWidgets());
    this.addToMenu(menu, "Navigation", new Navigation());
    this.addToMenu(menu, "Trees & Tables", new TreesTables()).setPathComponent("trees-tables");
    this.addToMenu(menu, "Graphics & Charts", new GraphicsWidgets())
        .setPathComponent("graphics-charts");
    this.addToMenu(menu, "Media", new Media());
    this.navigation_.addMenu(menu);
    WVBoxLayout layout = new WVBoxLayout();
    this.setLayout(layout);
    layout.addWidget(navigation, 0);
    layout.addWidget(contentsStack, 1);
    layout.setContentsMargins(0, 0, 0, 0);
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public WidgetGallery() {
    this((WContainerWidget) null);
  }

  private WNavigationBar navigation_;
  private WStackedWidget contentsStack_;

  private WMenuItem addToMenu(WMenu menu, final CharSequence name, TopicWidget topic) {
    TopicWidget topic_ = topic;
    WContainerWidget result = new WContainerWidget();
    WContainerWidget pane = new WContainerWidget();
    WContainerWidget pane_ = pane;
    WVBoxLayout vLayout = new WVBoxLayout();
    result.setLayout(vLayout);
    vLayout.setContentsMargins(0, 0, 0, 0);
    vLayout.addWidget(topic);
    vLayout.addWidget(pane, 1);
    WHBoxLayout hLayout = new WHBoxLayout();
    pane_.setLayout(hLayout);
    WMenuItem item = new WMenuItem(WString.toWString(name), result);
    WMenuItem item_ = menu.addItem(item);
    WStackedWidget subStack = new WStackedWidget();
    subStack.addStyleClass("contents");
    WMenu subMenu = new WMenu(subStack);
    WMenu subMenu_ = subMenu;
    subMenu_.addStyleClass("nav-pills nav-stacked submenu");
    subMenu_.setWidth(new WLength(200));
    hLayout.addWidget(subMenu);
    hLayout.addWidget(subStack, 1);
    subMenu_.setInternalPathEnabled();
    subMenu_.setInternalBasePath("/" + item_.getPathComponent());
    topic_.populateSubMenu(subMenu_);
    return item_;
  }
}
