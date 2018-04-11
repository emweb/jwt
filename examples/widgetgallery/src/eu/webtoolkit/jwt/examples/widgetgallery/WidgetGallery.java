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

class WidgetGallery extends WContainerWidget {
	private static Logger logger = LoggerFactory.getLogger(WidgetGallery.class);

	public WidgetGallery() {
		super();
		this.setOverflow(WContainerWidget.Overflow.OverflowHidden);
		this.navigation_ = new WNavigationBar();
		this.navigation_.addStyleClass("main-nav");
		this.navigation_.setTitle("Wt Widget Gallery", new WLink(
				"https://www.webtoolkit.eu/widgets"));
		this.navigation_.setResponsive(true);
		this.contentsStack_ = new WStackedWidget();
		WAnimation animation = new WAnimation(WAnimation.AnimationEffect.Fade,
				WAnimation.TimingFunction.Linear, 200);
		this.contentsStack_.setTransitionAnimation(animation, true);
		WMenu menu = new WMenu(this.contentsStack_, (WContainerWidget) null);
		menu.setInternalPathEnabled();
		menu.setInternalBasePath("/");
		this.addToMenu(menu, "Layout", new Layout());
		this.addToMenu(menu, "Forms", new FormWidgets());
		this.addToMenu(menu, "Navigation", new Navigation());
		this.addToMenu(menu, "Trees & Tables", new TreesTables())
				.setPathComponent("trees-tables");
		this.addToMenu(menu, "Graphics & Charts", new GraphicsWidgets())
				.setPathComponent("graphics-charts");
		this.addToMenu(menu, "Media", new Media());
		this.navigation_.addMenu(menu);
		WVBoxLayout layout = new WVBoxLayout(this);
		layout.addWidget(this.navigation_);
		layout.addWidget(this.contentsStack_, 1);
		layout.setContentsMargins(0, 0, 0, 0);
	}

	private WNavigationBar navigation_;
	private WStackedWidget contentsStack_;

	private WMenuItem addToMenu(WMenu menu, final CharSequence name,
			TopicWidget topic) {
		WContainerWidget result = new WContainerWidget();
		WContainerWidget pane = new WContainerWidget();
		WVBoxLayout vLayout = new WVBoxLayout(result);
		vLayout.setContentsMargins(0, 0, 0, 0);
		vLayout.addWidget(topic);
		vLayout.addWidget(pane, 1);
		WHBoxLayout hLayout = new WHBoxLayout(pane);
		WMenuItem item = new WMenuItem(name, result);
		menu.addItem(item);
		WStackedWidget subStack = new WStackedWidget();
		subStack.addStyleClass("contents");
		subStack.setOverflow(WContainerWidget.Overflow.OverflowAuto);
		WMenu subMenu = new WMenu(subStack);
		subMenu.addStyleClass("nav-pills nav-stacked submenu");
		subMenu.setWidth(new WLength(200));
		hLayout.addWidget(subMenu);
		hLayout.addWidget(subStack, 1);
		subMenu.setInternalPathEnabled();
		subMenu.setInternalBasePath("/" + item.getPathComponent());
		topic.populateSubMenu(subMenu);
		return item;
	}
}
