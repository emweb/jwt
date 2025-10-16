/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.hellowidgetset;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class HelloWidgetSetMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public HelloWidgetSetMain() {
      super();

      getConfiguration().setUseScriptNonce(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        return new HelloWidgetSetApplication(env, true);
    }
}
