/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.charts;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WPanel;
import eu.webtoolkit.jwt.WWidget;

public class PanelList extends WContainerWidget {

    public PanelList(WContainerWidget parent) {
        super(parent);
    }

    public WPanel addWidget(String text, WWidget w) {
        WPanel p = new WPanel();
        p.setTitle(text);
        p.setCentralWidget(w);

        addPanel(p);

        return p;
    }

    public void addPanel(final WPanel panel) {
        panel.setCollapsible(true);
        panel.collapse();

        panel.expandedSS().addListener(this, new Signal1.Listener<Boolean>() {
            public void trigger(Boolean b) {
                onExpand(b, panel);
            }
        });

        super.addWidget(panel);
    }

    private void onExpand(boolean notUndo, WPanel sender) {
        WPanel panel = sender;

        if (notUndo) {
            wasExpanded_ = -1;

            for (int i = 0; i < getChildren().size(); ++i) {
                WPanel p = (WPanel) getChildren().get(i);
                if (p != panel) {
                    if (!p.isCollapsed())
                        wasExpanded_ = i;
                    p.collapse();
                }
            }
        } else {
            if (wasExpanded_ != -1) {
                WPanel p = (WPanel) getChildren().get(wasExpanded_);
                p.expand();
            }
        }
    }

    private int wasExpanded_;
}
