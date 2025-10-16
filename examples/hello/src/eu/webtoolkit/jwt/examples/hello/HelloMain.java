/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.hello;

import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WtServlet;

public class HelloMain extends WtServlet {
    private static final long serialVersionUID = 1L;

    public HelloMain() {
        super();
        // Enable websockets only if the servlet container has support for JSR-356 (Jetty 9, Tomcat 7, ...)
        //getConfiguration().setWebSocketsEnabled(true);

        getConfiguration().setUseScriptNonce(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        /*
         * You could read information from the environment to decide whether the
         * user has permission to start a new application
         */
        return new HelloApplication(env);
    }
}
