/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.treeview;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class TreeViewMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public TreeViewMain() {
        super();

        getConfiguration().setUseScriptNonce(true);
        getConfiguration().setSendXHTMLMimeType(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        WApplication app = new TreeViewApplication(env);
        app.setTitle("WTreeView example");

        return app;
    }
}
