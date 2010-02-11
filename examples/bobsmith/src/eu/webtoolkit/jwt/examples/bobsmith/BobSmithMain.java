/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.bobsmith;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WInPlaceEdit;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WtServlet;

public class BobSmithMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public BobSmithMain() {
        super();
    }

    public WApplication createApplication(WEnvironment env) {
        WApplication app = new WApplication(env);

        new WText("Name: ", app.getRoot());
        WInPlaceEdit edit = new WInPlaceEdit("Bob Smith", app.getRoot());
        edit.setStyleClass("inplace");
        edit.setEmptyText("Empty, click the field to provide a name.");

        app.getStyleSheet().addRule("*.inplace span:hover",
                "background-color: gray");

        return app;
    }
}