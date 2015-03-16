/*
 * Copyright (C) 2014 Emweb bvba, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.chart.WCartesian3DChart;

public class MouseHandlerDemoChart extends WCartesian3DChart {
	public MouseHandlerDemoChart() {
		this(null);
	}

	public MouseHandlerDemoChart(WContainerWidget parent) {
		super(parent);
		selectionMode = false;
		selectionHandler = false;
	}

	@Override
	public void updateGL() {
		super.updateGL();
		if (selectionMode) {
			if (selectionHandler) {
				setClientSideMouseHandler("CustomMouseHandlers.selectionHandler("
						+ getJsRef() + ")");
			} else {
				setClientSideMouseHandler("{}");
			}
		} else {
			setClientSideMouseHandler("CustomMouseHandlers.lookAtHandler("
					+ getJsRef() + "," + getJsMatrix().getJsRef()
					+ ", [0.5,0.5,0.5], " + "[0,1,0], 0.005, 0.005)");
		}
	}

	public boolean selectionMode;
	public boolean selectionHandler;
}
