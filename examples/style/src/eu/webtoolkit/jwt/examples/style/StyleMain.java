/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.style;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class StyleMain extends WtServlet {
    private static final long serialVersionUID = 8134425135017460475L;

    public StyleMain() {
        super();

        getConfiguration().setUseScriptNonce(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        WApplication app = new WApplication(env);
        app.setTitle("Style example");

        new StyleExample(app.getRoot());

        return app;
    }
}
