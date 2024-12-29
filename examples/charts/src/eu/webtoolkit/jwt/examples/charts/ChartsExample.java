/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.charts;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;

/**
 * A widget that demonstrates various aspects of the charting lib.
 */
public class ChartsExample extends WContainerWidget {
    /**
     * Constructor.
     */
    public ChartsExample(WContainerWidget root) {
        super(root);
        new WText(WString.tr("introduction"), this);

        new CategoryExample(this);
        new TimeSeriesExample(this);
        new ScatterPlotExample(this);
        new PieExample(this);
    }
}