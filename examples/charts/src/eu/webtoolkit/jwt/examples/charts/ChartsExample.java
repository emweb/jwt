/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.charts;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.StringUtils;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WAbstractItemView;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WItemDelegate;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WShadow;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTreeView;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.AxisValue;
import eu.webtoolkit.jwt.chart.AxisScale;
import eu.webtoolkit.jwt.chart.ChartType;
import eu.webtoolkit.jwt.chart.FillRangeType;
import eu.webtoolkit.jwt.chart.LabelOption;
import eu.webtoolkit.jwt.chart.SeriesType;
import eu.webtoolkit.jwt.chart.WCartesianChart;
import eu.webtoolkit.jwt.chart.WDataSeries;
import eu.webtoolkit.jwt.chart.WPieChart;
import eu.webtoolkit.jwt.examples.charts.csv.CsvUtil;

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