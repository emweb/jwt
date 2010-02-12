/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.hellowidgetset;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;

public class HelloWidgetSetApplication extends WApplication {
    public HelloWidgetSetApplication(WEnvironment env, boolean embedded) {
        super(env);
        /*
         * By default, "dynamic script tags" are used to relay event information
         * in WidgetSet mode. This has the benefit of allowing an application to
         * be embedded from within a web page on another domain.
         * 
         * You can revert to plain AJAX requests using the following call. This
         * will only work if your application is hosted on the same domain as
         * the web page in which it is embedded.
         */
        //setAjaxMethod(AjaxMethod.XMLHttpRequest);

        WContainerWidget top;

        setTitle("Hello world");

        if (!embedded) {
            /*
             * In Application mode, we have the root() is a container
             * corresponding to the entire browser window
             */
            top = getRoot();

        } else {
            /*
             * In WidgetSet mode, we create and bind containers to existing divs
             * in the web page. In this example, we create a single div whose
             * DOM id was passed as a request argument.
             */
            top = new WContainerWidget();
            final String div = env.getParameter("div");
            if (div != null)
                bindWidget(top, div);
            else {
                System.err.println("Missing: parameter: 'div'");
                return;
            }
        }

        if (!embedded)
            new WText(
                    "<p><emph>Note: you can also run this application "
                            + "from within <a href=\"hello.html\">a web page</a>.</emph></p>",
                    getRoot());

        /*
         * Everything else is business as usual.
         */

        top.addWidget(new WText("Your name, please ? "));
        nameEdit_ = new WLineEdit(top);
        nameEdit_.setFocus();

        WPushButton b = new WPushButton("Greet me.", top);
        b.setMargin(5, Side.Left);

        top.addWidget(new WBreak());

        greeting_ = new WText(top);

        /*
         * Connect signals with slots
         */
        b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
            @Override
            public void trigger(WMouseEvent arg) {
                greet();
            }
        });
        nameEdit_.enterPressed().addListener(this, new Signal.Listener() {
            @Override
            public void trigger() {
                greet();
            }
        });
    }

    public void greet() {
        /*
         * Update the text, using text input into the nameEdit_ field.
         */
        greeting_.setText("Hello there, " + nameEdit_.getText());
    }

    private WLineEdit nameEdit_;
    private WText greeting_;
}
