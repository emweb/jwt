package eu.webtoolkit.jwt.examples.charts;

import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WShadow;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.AxisValue;
import eu.webtoolkit.jwt.chart.ChartType;
import eu.webtoolkit.jwt.chart.FillRangeType;
import eu.webtoolkit.jwt.chart.SeriesType;
import eu.webtoolkit.jwt.chart.WCartesianChart;
import eu.webtoolkit.jwt.chart.WDataSeries;

/**
 * A Widget that demonstrates a scatter plot
 */
public class ScatterPlotExample extends WContainerWidget {
    /**
     * Creates the scatter plot example
     */
    public ScatterPlotExample(WContainerWidget parent) {
        super(parent);
        new WText(WString.tr("scatter plot 2"), this);

        WStandardItemModel model = new WStandardItemModel(40, 2, this);
        model.setHeaderData(0, Orientation.Horizontal, new WString("X"));
        model.setHeaderData(1, Orientation.Horizontal,
                new WString("Y = sin(X)"));

        for (int i = 0; i < 40; ++i) {
            double x = (i - 20) / 4.0;

            model.setData(i, 0, x);
            model.setData(i, 1, Math.sin(x));
        }

        /*
         * Create the scatter plot.
         */
        WCartesianChart chart = new WCartesianChart(this);
        chart.setModel(model); // set the model
        chart.setXSeriesColumn(0); // set the column that holds the X data
        chart.setLegendEnabled(true); // enable the legend

        chart.setType(ChartType.ScatterPlot); // set type to ScatterPlot

        // Typically, for mathematical functions, you want the axes to cross
        // at the 0 mark:
        chart.getAxis(Axis.XAxis).setLocation(AxisValue.ZeroValue);
        chart.getAxis(Axis.YAxis).setLocation(AxisValue.ZeroValue);
        chart.getAxis(Axis.XAxis).setLabelFormat("%.1f");
        chart.getAxis(Axis.YAxis).setLabelFormat("%.1f");

        // Provide space for the X and Y axis and title.
        chart.setPlotAreaPadding(100, Side.Left);
        chart.setPlotAreaPadding(50, Side.Top, Side.Bottom);

        WDataSeries s = new WDataSeries(1, SeriesType.CurveSeries);
        s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
        chart.addSeries(s);

        chart.resize(800, 300); // WPaintedWidget must be given explicit size

        chart.setMargin(10, Side.Top, Side.Bottom); // add margin vertically
        chart.setMargin(WLength.Auto, Side.Left, Side.Right); // center
        // horizontally

        ChartConfig config = new ChartConfig(chart, this);
        config.setValueFill(FillRangeType.ZeroValueFill);
    }
}