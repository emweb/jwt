/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.dragdrop;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WtServlet;

public class DragMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public DragMain() {
        super();

        getConfiguration().setUseScriptNonce(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        WApplication app = new WApplication(env);
        app.setTitle("Drag & drop");
        
        app.getRoot().setStyleClass("root");

        new WText("<h1>Wt Drag &amp; drop example.</h1>", app.getRoot());

        new DragExample(app.getRoot());

        app.useStyleSheet(new WLink("style/dragdrop.css"));

        return app;
    }
}
