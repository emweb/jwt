/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.javascript;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class JavaScriptMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public JavaScriptMain() {
        super();
    }

    public WApplication createApplication(WEnvironment env) {
        return new JavaScriptExample(env);
    }
}
