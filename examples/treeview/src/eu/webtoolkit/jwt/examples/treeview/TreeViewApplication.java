/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.treeview;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WXmlLocalizedStrings;

public class TreeViewApplication extends WApplication {
    public TreeViewApplication(WEnvironment env) {
        super(env);

        WXmlLocalizedStrings resourceBundle = new WXmlLocalizedStrings();
        resourceBundle.use("/eu/webtoolkit/jwt/examples/treeview/drinks");
        setLocalizedStrings(resourceBundle);

        getStyleSheet().addRule("button", "margin: 2px");

        new TreeViewExample(true, getRoot());
        /*
         * Stub for the drink info
         */
        aboutDrink_ = new WText("", getRoot());

        internalPathChanged().addListener(this, new Signal1.Listener<String>() {
            public void trigger(String p) {
                handlePathChange(p);
            }
        });
    }

    private WText aboutDrink_;

    private void handlePathChange(String prefix) {
        if (prefix == "/drinks/") {
            String drink = getInternalPathNextPart(prefix);

            aboutDrink_.setText(tr("drink-" + drink));
        }
    }
}
