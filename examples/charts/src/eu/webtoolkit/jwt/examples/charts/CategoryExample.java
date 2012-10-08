package eu.webtoolkit.jwt.examples.charts;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WAbstractItemView;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WShadow;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WPaintedWidget.Method;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.SeriesType;
import eu.webtoolkit.jwt.chart.WCartesianChart;
import eu.webtoolkit.jwt.chart.WDataSeries;
import eu.webtoolkit.jwt.examples.charts.csv.CsvUtil;

/**
 * A Widget that demonstrates a category chart
 */
public class CategoryExample extends WContainerWidget {
    /**
     * Creates the category chart example
     */
    public CategoryExample(WContainerWidget parent) {
        super(parent);
        new WText(WString.tr("category chart"), this);

        WAbstractItemModel model = CsvUtil.readCsvFile("category.csv", this);

        if (model == null)
            return;

        WContainerWidget w = new WContainerWidget(this);
        WTableView table = new WTableView(w);
        table.setMargin(new WLength(10), Side.Top, Side.Bottom);
        table.setMargin(WLength.Auto, Side.Left, Side.Right);
        table.setModel(model);
        table.setSortingEnabled(true);
        table.setColumnResizeEnabled(true);
        table.setSelectionMode(SelectionMode.NoSelection);
        table.setAlternatingRowColors(true);
        table.setColumnAlignment(0, AlignmentFlag.AlignCenter);
        table.setHeaderAlignment(0, AlignmentFlag.AlignCenter);
        table.setRowHeight(new WLength(22));

        // Editing does not really work without Ajax, it would require an
        // additional button somewhere to confirm the edited value.
        if (WApplication.getInstance().getEnvironment().hasAjax()) {
          table.resize(600, 20 + 5*22);
          table.setEditTriggers(WAbstractItemView.EditTrigger.SingleClicked);
        } else {
          table.resize(new WLength(600), WLength.Auto);
          table.setEditTriggers(WAbstractItemView.EditTrigger.NoEditTrigger);
        }

        table.setColumnWidth(0, new WLength(80));
        for (int i = 1; i < model.getColumnCount(); ++i)
          table.setColumnWidth(i, new WLength(120));

        /*
         * Create the category chart.
         */
        WCartesianChart chart = new WCartesianChart(this);
        chart.setModel(model); // set the model
        chart.setXSeriesColumn(0); // set the column that holds the categories
        chart.setLegendEnabled(true); // enable the legend
        chart.setPreferredMethod(Method.PngImage);

        // Provide space for the X and Y axis and title.
        chart.setPlotAreaPadding(100, Side.Left);
        chart.setPlotAreaPadding(50, Side.Top, Side.Bottom);
        chart.getAxis(Axis.YAxis).setLabelFormat("%.0f");

        /*
         * Add all (but first) column as bar series
         */
        for (int i = 1; i < model.getColumnCount(); ++i) {
            WDataSeries s = new WDataSeries(i, SeriesType.BarSeries);
            s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
            chart.addSeries(s);
        }

        chart.resize(800, 400); // WPaintedWidget must be given explicit size

        chart.setMargin(10, Side.Top, Side.Bottom); // add margin vertically
        chart.setMargin(WLength.Auto, Side.Left, Side.Right); // center
        // horizontally

        new ChartConfig(chart, this);
    }
}
