/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.textedit;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextEdit;
import eu.webtoolkit.jwt.WtServlet;

public class TextEditExample extends WtServlet {
    private static final long serialVersionUID = 1L;

    public TextEditExample() {
      super();

      getConfiguration().setUseScriptNonce(true);
    }

    @Override
    public WApplication createApplication(WEnvironment env) {
        WApplication app = new WApplication(env);

        WContainerWidget cw = new WContainerWidget(app.getRoot());
        final WTextEdit edit = new WTextEdit(cw);
        WPushButton button = new WPushButton("Push the button!", cw);
        final WText text = new WText(cw);

        button.clicked().addListener(cw, new Signal1.Listener<WMouseEvent>() {
            public void trigger(WMouseEvent a1) {
                text.setText(edit.getText());
            }
        });

        return app;
    }
}
