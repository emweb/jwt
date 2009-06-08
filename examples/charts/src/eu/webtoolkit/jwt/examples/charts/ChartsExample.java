package eu.webtoolkit.jwt.examples.charts;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTreeView;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.AxisLocation;
import eu.webtoolkit.jwt.chart.AxisScale;
import eu.webtoolkit.jwt.chart.ChartType;
import eu.webtoolkit.jwt.chart.FillRangeType;
import eu.webtoolkit.jwt.chart.LabelOption;
import eu.webtoolkit.jwt.chart.SeriesType;
import eu.webtoolkit.jwt.chart.WCartesianChart;
import eu.webtoolkit.jwt.chart.WDataSeries;
import eu.webtoolkit.jwt.chart.WPieChart;
import eu.webtoolkit.jwt.examples.charts.csv.CsvUtil;
import eu.webtoolkit.jwt.utils.StringUtils;

/** A widget that demonstrates various aspects of the charting lib.
 */
public class ChartsExample extends WContainerWidget
{
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

	/** 
	 * A widget that demonstrates a times series chart
	 */
	class TimeSeriesExample extends WContainerWidget
	{
	  /** 
	   * Creates the time series scatter plot example
	   */
		public TimeSeriesExample(WContainerWidget parent) {
			  super(parent);
			  
			  new WText(WString.tr("scatter plot"), this);

			    WAbstractItemModel model = CsvUtil.readCsvFile("timeseries.csv", this);

			    if (model==null)
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

			    /*
			     * Create the scatter plot.
			     */
			    WCartesianChart chart = new WCartesianChart(this);
			    chart.setModel(model);        // set the model
			    chart.setXSeriesColumn(0);    // set the column that holds the X data
			    chart.setLegendEnabled(true); // enable the legend

			    chart.setType(ChartType.ScatterPlot);            // set type to ScatterPlot
			    chart.axis(Axis.XAxis).setScale(AxisScale.DateScale); // set scale of X axis to DateScale

			    // Provide space for the X and Y axis and title. 
			    chart.setPlotAreaPadding(100, Side.Left);
			    chart.setPlotAreaPadding(50, Side.Top ,Side.Bottom);

			    /*
			     * Add first two columns as line series
			     */
			    for (int i = 1; i < 3; ++i) {
			      WDataSeries s = new WDataSeries(i, SeriesType.LineSeries);
			      chart.addSeries(s);
			    }

			    chart.resize(800, 400); // WPaintedWidget must be given explicit size

			    chart.setMargin(10, Side.Top ,Side.Bottom);            // add margin vertically
			    chart.setMargin(WLength.Auto, Side.Left ,Side.Right); // center horizontally

			    new ChartConfig(chart, this);
		}
	}

	/** 
	 * A Widget that demonstrates a category chart
	 */
	class CategoryExample extends WContainerWidget
	{
	  /** 
	   * Creates the category chart example
	   */
		public CategoryExample(WContainerWidget parent) {
			super(parent);
			  new WText(WString.tr("category chart"), this);
			  
			  WAbstractItemModel model = CsvUtil.readCsvFile("category.csv", this);

			  if (model==null)
			    return;

			  /*
			   * If we have JavaScript, show an Ext table view that allows editing
			   * of the model.
			   */
			  if (WApplication.instance().getEnvironment().hasJavaScript()) {
			    WContainerWidget w = new WContainerWidget(this);
			    WTreeView table = new WTreeView(w);
			    table.setMargin(new WLength(10), Side.Top , Side.Bottom);
			    table.setMargin(WLength.Auto, Side.Left ,Side.Right);
			    table.resize(500, 175);
			    table.setModel(model);
			  }

			  /*
			   * Create the category chart.
			   */
			  WCartesianChart chart = new WCartesianChart(this);
			  chart.setModel(model);        // set the model
			  chart.setXSeriesColumn(0);    // set the column that holds the categories
			  chart.setLegendEnabled(true); // enable the legend

			  // Provide space for the X and Y axis and title. 
			  chart.setPlotAreaPadding(100, Side.Left);
			  chart.setPlotAreaPadding(50, Side.Top ,Side.Bottom);

			  //chart.axis(YAxis).setBreak(70, 110);

			  /*
			   * Add all (but first) column as bar series
			   */
			  for (int i = 1; i < model.getColumnCount(); ++i) {
			    WDataSeries s = new WDataSeries(i, SeriesType.BarSeries);
			    chart.addSeries(s);
			  }

			  chart.resize(800, 400); // WPaintedWidget must be given explicit size

			  chart.setMargin(10, Side.Top ,Side.Bottom);            // add margin vertically
			  chart.setMargin(WLength.Auto, Side.Left , Side.Right); // center horizontally

			  new ChartConfig(chart, this);
		}
	}

	/** A Widget that demonstrates a scatter plot
	 */
	class ScatterPlotExample extends WContainerWidget
	{
	  /**
	   *  Creates the scatter plot example
	   */
		public ScatterPlotExample(WContainerWidget parent) {
			  super(parent);
			    new WText(WString.tr("scatter plot 2"), this);

			    WStandardItemModel model = new WStandardItemModel(100, 2, this);
			    model.setHeaderData(0, new WString("X"));
			    model.setHeaderData(1, new WString("Y = sin(X)"));

			    for (int i = 0; i < 40; ++i) {
			      double x = (i - 20) / 4.0;

			      model.setData(i, 0, x);
			      model.setData(i, 1, Math.sin(x));
			    }
			   
			    /*
			     * Create the scatter plot.
			     */
			    WCartesianChart chart = new WCartesianChart(this);
			    chart.setModel(model);        // set the model
			    chart.setXSeriesColumn(0);    // set the column that holds the X data
			    chart.setLegendEnabled(true); // enable the legend

			    chart.setType(ChartType.ScatterPlot);   // set type to ScatterPlot

			    // Typically, for mathematical functions, you want the axes to cross
			    // at the 0 mark:
			    chart.axis(Axis.XAxis).setLocation(AxisLocation.ZeroValue);
			    chart.axis(Axis.YAxis).setLocation(AxisLocation.ZeroValue);

			    // Provide space for the X and Y axis and title. 
			    chart.setPlotAreaPadding(100, Side.Left);
			    chart.setPlotAreaPadding(50, Side.Top ,Side.Bottom);

			    // Add the two curves
			    chart.addSeries(new WDataSeries(1, SeriesType.CurveSeries));

			    chart.resize(800, 300); // WPaintedWidget must be given explicit size

			    chart.setMargin(10, Side.Top , Side.Bottom);            // add margin vertically
			    chart.setMargin(WLength.Auto, Side.Left ,Side.Right); // center horizontally

			    ChartConfig config = new ChartConfig(chart, this);
			    config.setValueFill(FillRangeType.ZeroValueFill);
		}
	}

	/** A Widget that demonstrates a Pie chart
	 */
	class PieExample extends WContainerWidget
	{
	
	  /** 
	   * Creates the pie chart example
	   */
		public PieExample(WContainerWidget parent) {
			  super(parent);
			    new WText(WString.tr("pie chart"), this);

			    WAbstractItemModel model = CsvUtil.readCsvFile("pie.csv", this);

			    if (model==null)
			      return;

			    /*
			     * If we have JavaScript, show an Ext table view that allows editing
			     * of the model.
			     */
			    if (WApplication.instance().getEnvironment().hasJavaScript()) {
			      WContainerWidget w = new WContainerWidget(this);
			      WTreeView table = new WTreeView(w);
			      table.setMargin(10, Side.Top ,Side.Bottom);
			      table.setMargin(WLength.Auto, Side.Left ,Side.Right);
			      table.resize(300, 175);
			      table.setModel(model);
			    }

			    /*
			     * Create the pie chart.
			     */
			    WPieChart chart = new WPieChart(this);
			    chart.setModel(model);       // set the model
			    chart.setLabelsColumn(0);    // set the column that holds the labels
			    chart.setDataColumn(1);      // set the column that holds the data

			    // configure location and type of labels
			    chart.setDisplayLabels(LabelOption.Outside ,LabelOption.TextLabel ,LabelOption.TextPercentage);

			    // enable a 3D effect
			    chart.setPerspectiveEnabled(true, 0.2);

			    // explode the first item
			    chart.setExplode(0, 0.3);

			    chart.resize(800, 300); // WPaintedWidget must be given explicit size

			    chart.setMargin(10, Side.Top , Side.Bottom);            // add margin vertically
			    chart.setMargin(WLength.Auto, Side.Left ,Side.Right); // center horizontally
		}
	}


