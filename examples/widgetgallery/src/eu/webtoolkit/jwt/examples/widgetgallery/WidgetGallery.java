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

class WidgetGallery extends WContainerWidget {
	public WidgetGallery() {
		super();
		this.contentsStack_ = new WStackedWidget();
		this.contentsStack_.setOverflow(WContainerWidget.Overflow.OverflowAuto);
		this.contentsStack_.setStyleClass("contents");
		EventDisplayer eventDisplayer = new EventDisplayer(
				(WContainerWidget) null);
		WMenu menu = new WMenu(this.contentsStack_, Orientation.Vertical,
				(WContainerWidget) null);
		menu.setRenderAsList(true);
		menu.setStyleClass("menu");
		menu.setInternalPathEnabled();
		menu.setInternalBasePath("/");
		this.addToMenu(menu, "Basics", new BasicControls(eventDisplayer));
		this.addToMenu(menu, "Form Widgets", new FormWidgets(eventDisplayer));
		this.addToMenu(menu, "Form Validators", new Validators(eventDisplayer));
		this.addToMenu(menu, "Vector Graphics", new GraphicsWidgets(
				eventDisplayer));
		this.addToMenu(menu, "Special Purpose", new SpecialPurposeWidgets(
				eventDisplayer));
		this.addToMenu(menu, "Dialogs", new DialogWidgets(eventDisplayer));
		this.addToMenu(menu, "Charts", new ChartWidgets(eventDisplayer));
		this.addToMenu(menu, "MVC Widgets", new MvcWidgets(eventDisplayer));
		this.addToMenu(menu, "Events", new EventsDemo(eventDisplayer));
		this.addToMenu(menu, "Style and Layout",
				new StyleLayout(eventDisplayer));
		WHBoxLayout horizLayout = new WHBoxLayout(this);
		WVBoxLayout vertLayout = new WVBoxLayout();
		horizLayout.addWidget(menu, 0);
		horizLayout.addLayout(vertLayout, 1);
		vertLayout.addWidget(this.contentsStack_, 1);
		vertLayout.addWidget(eventDisplayer);
		horizLayout.setResizable(0, true);
	}

	private WStackedWidget contentsStack_;

	private void addToMenu(WMenu menu, CharSequence name,
			ControlsWidget controls) {
		if (controls.hasSubMenu()) {
			WSubMenuItem smi = new WSubMenuItem(name, controls);
			WMenu subMenu = new WMenu(this.contentsStack_,
					Orientation.Vertical, (WContainerWidget) null);
			subMenu.setRenderAsList(true);
			smi.setSubMenu(subMenu);
			menu.addItem(smi);
			subMenu.setInternalPathEnabled();
			subMenu.setInternalBasePath("/" + smi.getPathComponent());
			subMenu.setStyleClass("menu submenu");
			controls.populateSubMenu(subMenu);
		} else {
			menu.addItem(name, controls);
		}
	}
}
