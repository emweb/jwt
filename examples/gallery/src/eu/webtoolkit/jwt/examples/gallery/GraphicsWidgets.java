/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.gallery;

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
import eu.webtoolkit.jwt.examples.painting.*;

class GraphicsWidgets extends ControlsWidget {
	public GraphicsWidgets(EventDisplayer ed) {
		super(ed, true);
		this.topic("WPaintedWidget", this);
		new WText(tr("graphics-intro"), this);
	}

	public void remove() {
		super.remove();
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("Emweb logo example", this.emwebLogo());
		menu.addItem("Paintbrush example", this.paintbrush());
	}

	private WWidget emwebLogo() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WPaintedWidget", result);
		new PaintExample(result, false);
		return result;
	}

	private WWidget paintbrush() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WPaintedWidget", result);
		new WText(tr("graphics-paintbrush"), result);
		WTable layout = new WTable(result);
		final PaintBrush canvas = new PaintBrush(710, 400, layout.getElementAt(
				0, 0));
		canvas.getDecorationStyle().setBorder(new WBorder(WBorder.Style.Solid));
		new WText("Color chooser:", layout.getElementAt(0, 1));
		WTable colorTable = new WTable(layout.getElementAt(0, 1));
		addColor(canvas, colorTable.getElementAt(0, 0), WColor.black);
		addColor(canvas, colorTable.getElementAt(0, 1), WColor.red);
		addColor(canvas, colorTable.getElementAt(1, 0), WColor.green);
		addColor(canvas, colorTable.getElementAt(1, 1), WColor.blue);
		new WBreak(layout.getElementAt(0, 1));
		WPushButton clearButton = new WPushButton("Clear", layout.getElementAt(
				0, 1));
		clearButton.clicked().addListener(canvas,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						canvas.clear();
					}
				});
		layout.getElementAt(0, 1).setPadding(new WLength(3));
		return result;
	}

	static void addColor(final PaintBrush canvas, WTableCell cell, WColor color) {
		cell.getDecorationStyle().setBackgroundColor(color);
		cell.resize(new WLength(15), new WLength(15));
		final WColor javaColor = color;
		cell.clicked().addListener(canvas, new Signal.Listener() {
			public void trigger() {
				canvas.setColor(javaColor);
			}
		});
	}
}
