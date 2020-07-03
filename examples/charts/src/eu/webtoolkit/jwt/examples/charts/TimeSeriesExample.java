package eu.webtoolkit.jwt.examples.charts;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.EditTrigger;
import eu.webtoolkit.jwt.SelectionMode;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.StringUtils;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WItemDelegate;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WShadow;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.AxisScale;
import eu.webtoolkit.jwt.chart.ChartType;
import eu.webtoolkit.jwt.chart.SeriesType;
import eu.webtoolkit.jwt.chart.WCartesianChart;
import eu.webtoolkit.jwt.chart.WDataSeries;
import eu.webtoolkit.jwt.examples.charts.csv.CsvUtil;

/**
 * A widget that demonstrates a times series chart
 */
public class TimeSeriesExample extends WContainerWidget {
    /**
     * Creates the time series scatter plot example
     */
    public TimeSeriesExample(WContainerWidget parent) {
        super(parent);

        new WText(WString.tr("scatter plot"), this);

        WAbstractItemModel model = CsvUtil.readCsvFile("timeseries.csv", this);

        if (model == null)
            return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
        /*
         * Parse the first column as dates
         */
        for (int i = 0; i < model.getRowCount(); ++i) {
            WString s = StringUtils.asString(model.getData(i, 0));
            WDate d = null;
            try {
                d = new WDate(sdf.parse(s.getValue()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            model.setData(i, 0, d);
        }

        WContainerWidget w = new WContainerWidget(this);
        WTableView table = new WTableView(w);

        table.setMargin(10, Side.Top, Side.Bottom);
        table.setMargin(WLength.Auto, Side.Left, Side.Right);

        table.setModel(model);
        table.setSortingEnabled(false); // Does not make much sense for time series
        table.setColumnResizeEnabled(true);
        table.setSelectionMode(SelectionMode.None);
        table.setAlternatingRowColors(true);
        table.setColumnAlignment(0, AlignmentFlag.Center);
        table.setHeaderAlignment(0, AlignmentFlag.Center);
        table.setRowHeight(new WLength(22));

        // Editing does not really work without Ajax, it would require an
        // additional button somewhere to confirm the edited value.
        if (WApplication.getInstance().getEnvironment().hasAjax()) {
          table.resize(800, 20 + 5*22);
          table.setEditTriggers(EditTrigger.SingleClicked);
        } else {
          table.resize(800, 20 + 5*22 + 25);
          table.setEditTriggers(EditTrigger.None);
        }

        table.setColumnWidth(0, new WLength(80));
        for (int i = 1; i < model.getColumnCount(); ++i)
          table.setColumnWidth(i, new WLength(90));
        
        WItemDelegate delegate = new WItemDelegate();
        delegate.setTextFormat("dd/MM/yy");
        table.setItemDelegateForColumn(0, delegate);

        /*
         * Create the scatter plot.
         */
        WCartesianChart chart = new WCartesianChart(this);
        chart.setModel(model); // set the model
        chart.setXSeriesColumn(0); // set the column that holds the X data
        chart.setLegendEnabled(true); // enable the legend

        chart.setType(ChartType.Scatter); // set type to ScatterPlot
        chart.getAxis(Axis.X).setScale(AxisScale.Date); // set scale of
        // X axis to
        // DateScale
        chart.getAxis(Axis.Y).setLabelFormat("%.0f");

        chart.setAutoLayoutEnabled();

        /*
         * Add first two columns as line series
         */
        for (int i = 1; i < 3; ++i) {
            WDataSeries s = new WDataSeries(i, SeriesType.Line);
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