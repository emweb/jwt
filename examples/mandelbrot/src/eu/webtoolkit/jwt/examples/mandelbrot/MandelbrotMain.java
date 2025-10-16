/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.mandelbrot;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WtServlet;

public class MandelbrotMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public MandelbrotMain() {
        super();

        getConfiguration().setUseScriptNonce(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        WApplication app = new WApplication(env);
        app.setTitle(new WString("Wt Mandelbrot example"));
        app.getRoot().addWidget(new MandelbrotExample());
        return app;
    }
}
