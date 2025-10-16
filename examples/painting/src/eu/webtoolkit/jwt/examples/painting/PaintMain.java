/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.painting;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WtServlet;

public class PaintMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public PaintMain() {
        super();

        getConfiguration().setUseScriptNonce(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        WApplication app = new WApplication(env);
        app.setTitle(new WString("Paint example"));

        app.useStyleSheet(new WLink("style/painting.css"));

        new PaintExample(app.getRoot(), true);

        return app;
    }
}
