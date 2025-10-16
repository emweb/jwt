/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WStdLocalizedStrings;
import eu.webtoolkit.jwt.WtServlet;

public class ComposerMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public ComposerMain() {
      super();

      getConfiguration().setUseScriptNonce(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        WApplication app = new WApplication(env);
        //Multiple resources can be used
        WStdLocalizedStrings wsls = new WStdLocalizedStrings();
        wsls.use("eu.webtoolkit.jwt.examples.composer.composer");
        wsls.use("eu.webtoolkit.jwt.examples.composer.composer-buttons");
        app.setLocalizedStrings(wsls);
        app.setTitle("Composer example");
        app.useStyleSheet(new WLink("style/composer.css"));

        app.getRoot().addWidget(new ComposeExample());

        return app;
    }
}
