/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.charts;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLink;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;
import eu.webtoolkit.jwt.WtServlet;

public class ChartsMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public ChartsMain() {
        super();

        getConfiguration().setUseScriptNonce(true);
        getConfiguration().setSendXHTMLMimeType(false);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        WApplication app = new WApplication(env);
        app.setTitle("Charts example");

        //Multiple resources can be used
        WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
        resourceBundle.use("/eu/webtoolkit/jwt/examples/charts/charts");
        app.setLocalizedStrings(resourceBundle);

        app.getRoot().setPadding(new WLength(10));
        app.getRoot().resize(WLength.Auto, WLength.Auto);

        app.setCssTheme("polished");
        
        new ChartsExample(app.getRoot());

        app.useStyleSheet(new WLink("style/charts.css"));

        return app;
    }
}
