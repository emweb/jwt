/*
 * Copyright (C) 2014 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.chart3D;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class Chart3DMain extends WtServlet {
	private static final long serialVersionUID = 1L;

	public Chart3DMain() {
		super();

    getConfiguration().setUseScriptNonce(true);
	}

	@Override
	public WApplication createApplication(WEnvironment env) {
		return new ChartApplication(env);
	}
}
