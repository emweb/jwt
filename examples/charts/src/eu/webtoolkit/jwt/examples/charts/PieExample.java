package eu.webtoolkit.jwt.examples.charts;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WAbstractItemView;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WTableView;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.chart.LabelOption;
import eu.webtoolkit.jwt.chart.WPieChart;
import eu.webtoolkit.jwt.examples.charts.csv.CsvUtil;

/**
 * A Widget that demonstrates a Pie chart
 */
public class PieExample extends WContainerWidget {

    /**
     * Creates the pie chart example
     */
    public PieExample(WContainerWidget parent) {
        super(parent);
        new WText(WString.tr("pie chart"), this);

        WAbstractItemModel model = CsvUtil.readCsvFile("pie.csv", this);

        if (model == null)
            return;

        WContainerWidget w = new WContainerWidget(this);

        WTableView table = new WTableView(w);
        table.setMargin(10, Side.Top, Side.Bottom);
        table.setMargin(WLength.Auto, Side.Left, Side.Right);
        table.setSortingEnabled(true);
        table.setModel(model);
        table.setColumnWidth(1, new WLength(100));
        table.setRowHeight(new WLength(22));

        table.resize(300, 175);

        if (WApplication.getInstance().getEnvironment().hasAjax()) {
            table.resize(150 + 100 + 14, 20 + 6 * 22);
            table.setEditTriggers(WAbstractItemView.EditTrigger.SingleClicked);
        } else {
            table.resize(new WLength(150 + 100 + 14), WLength.Auto);
            table.setEditTriggers(WAbstractItemView.EditTrigger.NoEditTrigger);    
        }

        /*
         * Create the pie chart.
         */
        WPieChart chart = new WPieChart(this);
        chart.setModel(model); // set the model
        chart.setLabelsColumn(0); // set the column that holds the labels
        chart.setDataColumn(1); // set the column that holds the data

        // configure location and type of labels
        chart.setDisplayLabels(LabelOption.Outside, LabelOption.TextLabel,
                LabelOption.TextPercentage);

        // enable a 3D effect
        chart.setPerspectiveEnabled(true, 0.2);
        chart.setShadowEnabled(true);

        // explode the first item
        chart.setExplode(0, 0.3);

        chart.resize(800, 300); // WPaintedWidget must be given explicit size

        chart.setMargin(10, Side.Top, Side.Bottom); // add margin vertically
        chart.setMargin(WLength.Auto, Side.Left, Side.Right); // center
        // horizontally
    }
}