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

class WidgetGallery extends BaseTemplate {
  private static Logger logger = LoggerFactory.getLogger(WidgetGallery.class);

  public WidgetGallery(WContainerWidget parentContainer) {
    super("tpl:widget-gallery", (WContainerWidget) null);
    this.openMenuButton_ = null;
    this.menuOpen_ = false;
    this.contentsStack_ = (WStackedWidget) this.bindWidget("contents", new WStackedWidget());
    WAnimation animation = new WAnimation(AnimationEffect.Fade, TimingFunction.Linear, 200);
    this.contentsStack_.setTransitionAnimation(animation, true);
    WMenu menu = (WMenu) this.bindWidget("menu", new WMenu(this.contentsStack_));
    menu.addStyleClass("flex-column");
    menu.setInternalPathEnabled();
    menu.setInternalBasePath("/");
    this.openMenuButton_ = (WPushButton) this.bindWidget("open-menu", new WPushButton());
    this.openMenuButton_.setTextFormat(TextFormat.UnsafeXHTML);
    this.openMenuButton_.setText(showMenuText);
    this.openMenuButton_
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              WidgetGallery.this.toggleMenu();
            });
    WContainerWidget contentsCover =
        (WContainerWidget) this.bindWidget("contents-cover", new WContainerWidget());
    contentsCover
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              WidgetGallery.this.closeMenu();
            });
    this.addToMenu(menu, "Layout", new Layout());
    this.addToMenu(menu, "Forms", new FormWidgets());
    this.addToMenu(menu, "Navigation", new Navigation());
    this.addToMenu(menu, "Trees & Tables", new TreesTables()).setPathComponent("trees-tables");
    this.addToMenu(menu, "Graphics & Charts", new GraphicsWidgets())
        .setPathComponent("graphics-charts");
    this.addToMenu(menu, "Media", new Media());
    if (menu.getCurrentIndex() < 0) {
      menu.select(0);
      menu.itemAt(0).getMenu().select(0);
    }
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public WidgetGallery() {
    this((WContainerWidget) null);
  }

  private WStackedWidget contentsStack_;
  private WPushButton openMenuButton_;
  private boolean menuOpen_;

  private WMenuItem addToMenu(WMenu menu, final CharSequence name, Topic topicPtr) {
    Topic topic = topicPtr;
    WContainerWidget result = new WContainerWidget();
    WMenu subMenuPtr = new WMenu(this.contentsStack_);
    WMenu subMenu = subMenuPtr;
    WMenuItem itemPtr = new WMenuItem(WString.toWString(name));
    itemPtr.setMenu(subMenuPtr);
    WMenuItem item = menu.addItem(itemPtr);
    subMenu.addStyleClass("nav-stacked submenu");
    subMenu
        .itemSelected()
        .addListener(
            this,
            (WMenuItem e1) -> {
              WidgetGallery.this.closeMenu();
            });
    subMenu.setInternalPathEnabled("/" + item.getPathComponent());
    topic.populateSubMenu(subMenu);
    return item;
  }

  private void toggleMenu() {
    if (this.menuOpen_) {
      this.closeMenu();
    } else {
      this.openMenu();
    }
  }

  private void openMenu() {
    if (this.menuOpen_) {
      return;
    }
    this.openMenuButton_.setText(closeMenuText);
    this.addStyleClass("menu-open");
    this.menuOpen_ = true;
  }

  private void closeMenu() {
    if (!this.menuOpen_) {
      return;
    }
    this.openMenuButton_.setText(showMenuText);
    this.removeStyleClass("menu-open");
    this.menuOpen_ = false;
  }

  private static final String showMenuText =
      "<i class='fa fa-bars' aria-hidden='true'></i> Show menu";
  private static final String closeMenuText =
      "<i class='fa fa-bars' aria-hidden='true'></i> Close menu";
}
