/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.charts;

import java.util.ArrayList;
import java.util.List;

import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.StringUtils;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WCheckBox;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WDoubleValidator;
import eu.webtoolkit.jwt.WFormWidget;
import eu.webtoolkit.jwt.WIntValidator;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPanel;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WShadow;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WTable;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WValidator;
import eu.webtoolkit.jwt.chart.Axis;
import eu.webtoolkit.jwt.chart.AxisScale;
import eu.webtoolkit.jwt.chart.ChartType;
import eu.webtoolkit.jwt.chart.FillRangeType;
import eu.webtoolkit.jwt.chart.MarkerType;
import eu.webtoolkit.jwt.chart.SeriesType;
import eu.webtoolkit.jwt.chart.WAxis;
import eu.webtoolkit.jwt.chart.WCartesianChart;
import eu.webtoolkit.jwt.chart.WDataSeries;

/**
 * A class that allows configuration of a cartesian chart.
 * 
 * This widget provides forms for configuring chart, series, and axis properties
 * and manipulates the chart according to user settings.
 * 
 * This widget is part of the JWt charts example.
 */
public class ChartConfig extends WContainerWidget {
    /**
     * Constructor.
     */
    public ChartConfig(WCartesianChart chart, WContainerWidget parent) {
        super(parent);
        chart_ = chart;
        fill_ = FillRangeType.MinimumValueFill;

        PanelList list = new PanelList(this);

        WIntValidator sizeValidator = new WIntValidator(200, 2000, this);
        sizeValidator.setMandatory(true);

        WDoubleValidator anyNumberValidator = new WDoubleValidator(this);
        anyNumberValidator.setMandatory(true);

        WDoubleValidator angleValidator = new WDoubleValidator(-90, 90, this);
        angleValidator.setMandatory(true);

        // ---- Chart properties ----

        WStandardItemModel orientation = new WStandardItemModel(0, 1, this);
        addEntry(orientation, "Vertical");
        addEntry(orientation, "Horizontal");

        WTable chartConfig = new WTable();
        chartConfig.setMargin(WLength.Auto, Side.Left, Side.Right);

        int row = 0;
        chartConfig.getElementAt(row, 0).addWidget(new WText("Title:"));
        titleEdit_ = new WLineEdit(chartConfig.getElementAt(row, 1));
        connectSignals(titleEdit_);
        ++row;

        chartConfig.getElementAt(row, 0).addWidget(new WText("Width:"));
        chartWidthEdit_ = new WLineEdit(chartConfig.getElementAt(row, 1));
        chartWidthEdit_.setText(String.valueOf((int) chart_.getWidth()
                .getValue()));
        chartWidthEdit_.setValidator(sizeValidator);
        chartWidthEdit_.setMaxLength(4);
        connectSignals(chartWidthEdit_);
        ++row;

        chartConfig.getElementAt(row, 0).addWidget(new WText("Height:"));
        chartHeightEdit_ = new WLineEdit(chartConfig.getElementAt(row, 1));
        chartHeightEdit_.setText(String.valueOf((int) chart_.getHeight()
                .getValue()));
        chartHeightEdit_.setValidator(sizeValidator);
        chartHeightEdit_.setMaxLength(4);
        connectSignals(chartHeightEdit_);
        ++row;

        chartConfig.getElementAt(row, 0).addWidget(new WText("Orientation:"));
        chartOrientationEdit_ = new WComboBox(chartConfig.getElementAt(row, 1));
        chartOrientationEdit_.setModel(orientation);
        connectSignals(chartOrientationEdit_);
        ++row;

        for (int i = 0; i < chartConfig.getRowCount(); ++i) {
            chartConfig.getElementAt(i, 0).setStyleClass("tdhead");
            chartConfig.getElementAt(i, 1).setStyleClass("tddata");
        }

        WPanel p = list.addWidget("Chart properties", chartConfig);
        p.setMargin(WLength.Auto, Side.Left, Side.Right);
        p.resize(new WLength(880), WLength.Auto);
        p.setMargin(20, Side.Top, Side.Bottom);

        // ---- Series properties ----

        WStandardItemModel types = new WStandardItemModel(0, 1, this);
        addEntry(types, "Points");
        addEntry(types, "Line");
        addEntry(types, "Curve");
        addEntry(types, "Bar");
        addEntry(types, "Line Area");
        addEntry(types, "Curve Area");
        addEntry(types, "Stacked Bar");
        addEntry(types, "Stacked Line Area");
        addEntry(types, "Stacked Curve Area");

        WStandardItemModel markers = new WStandardItemModel(0, 1, this);
        addEntry(markers, "None");
        addEntry(markers, "Square");
        addEntry(markers, "Circle");
        addEntry(markers, "Cross");
        addEntry(markers, "X cross");
        addEntry(markers, "Triangle");

        WStandardItemModel axes = new WStandardItemModel(0, 1, this);
        addEntry(axes, "1st Y axis");
        addEntry(axes, "2nd Y axis");

        WStandardItemModel labels = new WStandardItemModel(0, 1, this);
        addEntry(labels, "None");
        addEntry(labels, "X");
        addEntry(labels, "Y");
        addEntry(labels, "X: Y");

        WTable seriesConfig = new WTable();
        seriesConfig.setMargin(WLength.Auto, Side.Left, Side.Right);

        addHeader(seriesConfig, "Name");
        addHeader(seriesConfig, "Enabled");
        addHeader(seriesConfig, "Type");
        addHeader(seriesConfig, "Marker");
        addHeader(seriesConfig, "Y axis");
        addHeader(seriesConfig, "Legend");
        addHeader(seriesConfig, "Shadow");
        addHeader(seriesConfig, "Value labels");

        seriesConfig.getRowAt(0).setStyleClass("trhead");

        for (int j = 1; j < chart.getModel().getColumnCount(); ++j) {
            SeriesControl sc = new SeriesControl();

            new WText(StringUtils.asString(chart.getModel().getHeaderData(j)),
                    seriesConfig.getElementAt(j, 0));

            sc.enabledEdit = new WCheckBox(seriesConfig.getElementAt(j, 1));
            connectSignals(sc.enabledEdit);

            sc.typeEdit = new WComboBox(seriesConfig.getElementAt(j, 2));
            sc.typeEdit.setModel(types);
            connectSignals(sc.typeEdit);

            sc.markerEdit = new WComboBox(seriesConfig.getElementAt(j, 3));
            sc.markerEdit.setModel(markers);
            connectSignals(sc.markerEdit);

            sc.axisEdit = new WComboBox(seriesConfig.getElementAt(j, 4));
            sc.axisEdit.setModel(axes);
            connectSignals(sc.axisEdit);

            sc.legendEdit = new WCheckBox(seriesConfig.getElementAt(j, 5));
            connectSignals(sc.legendEdit);

            sc.shadowEdit = new WCheckBox(seriesConfig.getElementAt(j, 6));
            connectSignals(sc.shadowEdit);

            sc.labelsEdit = new WComboBox(seriesConfig.getElementAt(j, 7));
            sc.labelsEdit.setModel(labels);
            connectSignals(sc.labelsEdit);

            int si = -1;
    		for (int i = 0; i < chart.getSeries().size(); ++i) {
    			if (chart.getSeries().get(i).getModelColumn() == j) {
    				si = i;
    				break;
    			}
    		}

            if (si != -1) {
                sc.enabledEdit.setChecked();
                WDataSeries s = chart_.getSeries(j);
                switch (s.getType()) {
                case PointSeries:
                    sc.typeEdit.setCurrentIndex(0);
                    break;
                case LineSeries:
                    sc.typeEdit
                            .setCurrentIndex(s.getFillRange() != FillRangeType.NoFill ? (s
                                    .isStacked() ? 7 : 4)
                                    : 1);
                    break;
                case CurveSeries:
                    sc.typeEdit
                            .setCurrentIndex(s.getFillRange() != FillRangeType.NoFill ? (s
                                    .isStacked() ? 8 : 5)
                                    : 2);
                    break;
                case BarSeries:
                    sc.typeEdit.setCurrentIndex(s.isStacked() ? 6 : 3);
                }

                sc.markerEdit.setCurrentIndex(s.getMarker().getValue());
                sc.legendEdit.setChecked(s.isLegendEnabled());
                sc.shadowEdit.setChecked(!s.getShadow().isNone());
            }

            seriesControls_.add(sc);

            seriesConfig.getRowAt(j).setStyleClass("trdata");
        }

        p = list.addWidget("Series properties", seriesConfig);
        p.expand();
        p.setMargin(WLength.Auto, Side.Left, Side.Right);
        p.resize(new WLength(880), WLength.Auto);
        p.setMargin(20, Side.Top, Side.Bottom);

        // ---- Axis properties ----

        WStandardItemModel yScales = new WStandardItemModel(0, 1, this);
        addEntry(yScales, "Linear scale");
        addEntry(yScales, "Log scale");

        WStandardItemModel xScales = new WStandardItemModel(0, 1, this);
        addEntry(xScales, "Categories");
        addEntry(xScales, "Linear scale");
        addEntry(xScales, "Log scale");
        addEntry(xScales, "Date scale");

        WTable axisConfig = new WTable();
        axisConfig.setMargin(WLength.Auto, Side.Left, Side.Right);

        addHeader(axisConfig, "Axis");
        addHeader(axisConfig, "Visible");
        addHeader(axisConfig, "Scale");
        addHeader(axisConfig, "Automatic");
        addHeader(axisConfig, "Minimum");
        addHeader(axisConfig, "Maximum");
        addHeader(axisConfig, "Gridlines");
        addHeader(axisConfig, "Label angle");

        axisConfig.getRowAt(0).setStyleClass("trhead");

        for (int i = 0; i < 3; ++i) {
            String axisName[] = { "X axis", "1st Y axis", "2nd Y axis" };
            int j = i + 1;

            Axis axisType = null;
            for (Axis at : Axis.values()) {
                if (at.getValue() == i) {
                    axisType = at;
                    break;
                }
            }

            WAxis axis = chart_.getAxis(axisType);

            final AxisControl sc = new AxisControl();

            new WText(axisName[i], axisConfig.getElementAt(j, 0));

            sc.visibleEdit = new WCheckBox(axisConfig.getElementAt(j, 1));
            sc.visibleEdit.setChecked(axis.isVisible());
            connectSignals(sc.visibleEdit);

            sc.scaleEdit = new WComboBox(axisConfig.getElementAt(j, 2));
            if (axis.getScale() == AxisScale.CategoryScale)
                sc.scaleEdit.addItem("Category scale");
            else {
                if (axis.getId() == Axis.XAxis) {
                    sc.scaleEdit.setModel(xScales);
                    sc.scaleEdit.setCurrentIndex(axis.getScale().getValue());
                } else {
                    sc.scaleEdit.setModel(yScales);
                    sc.scaleEdit
                            .setCurrentIndex(axis.getScale().getValue() - 1);
                }
            }
            connectSignals(sc.scaleEdit);

            boolean autoValues = axis.getMinimum() == WAxis.AUTO_MINIMUM
                    && axis.getMaximum() == WAxis.AUTO_MAXIMUM;

            sc.minimumEdit = new WLineEdit(axisConfig.getElementAt(j, 4));
            sc.minimumEdit.setText(gregDays(1986) + "");
            sc.minimumEdit.setValidator(anyNumberValidator);
            sc.minimumEdit.setEnabled(!autoValues);
            connectSignals(sc.minimumEdit);

            sc.maximumEdit = new WLineEdit(axisConfig.getElementAt(j, 5));
            sc.maximumEdit.setText(gregDays(1988) + "");
            sc.maximumEdit.setValidator(anyNumberValidator);
            sc.maximumEdit.setEnabled(!autoValues);
            connectSignals(sc.maximumEdit);

            sc.autoEdit = new WCheckBox(axisConfig.getElementAt(j, 3));
            sc.autoEdit.setChecked(autoValues);
            connectSignals(sc.autoEdit);
            sc.autoEdit.checked().addListener(this, new Signal.Listener() {
                public void trigger() {
                    sc.maximumEdit.disable();
                }
            });
            sc.autoEdit.unChecked().addListener(this, new Signal.Listener() {
                public void trigger() {
                    sc.maximumEdit.enable();
                }
            });
            sc.autoEdit.checked().addListener(this, new Signal.Listener() {
                public void trigger() {
                    sc.minimumEdit.disable();
                }
            });
            sc.autoEdit.unChecked().addListener(this, new Signal.Listener() {
                public void trigger() {
                    sc.minimumEdit.enable();
                }
            });

            sc.gridLinesEdit = new WCheckBox(axisConfig.getElementAt(j, 6));
            connectSignals(sc.gridLinesEdit);

            sc.labelAngleEdit = new WLineEdit(axisConfig.getElementAt(j, 7));
            sc.labelAngleEdit.setText("0");
            sc.labelAngleEdit.setValidator(angleValidator);
            connectSignals(sc.labelAngleEdit);

            axisConfig.getRowAt(j).setStyleClass("trdata");

            axisControls_.add(sc);
        }

        p = list.addWidget("Axis properties", axisConfig);
        p.setMargin(WLength.Auto, Side.Left, Side.Right);
        p.resize(new WLength(880), WLength.Auto);
        p.setMargin(20, Side.Top, Side.Bottom);

        /*
         * If we do not have JavaScript, then add a button to reflect changes to
         * the chart.
         */
        if (!WApplication.getInstance().getEnvironment().hasJavaScript()) {
            WPushButton b = new WPushButton(this);
            b.setText("Update chart");
            b.setInline(false); // so we can add margin to center horizontally
            b.setMargin(WLength.Auto, Side.Left, Side.Right);
            b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
                public void trigger(WMouseEvent a1) {
                    update();
                }
            });

        }
    }

    public void setValueFill(FillRangeType fill) {
        fill_ = fill;
    }

    private WCartesianChart chart_;
    private FillRangeType fill_;

    /**
     * Struct that holds the controls for one series
     */
    private class SeriesControl {
        WCheckBox enabledEdit;
        WComboBox typeEdit;
        WComboBox markerEdit;
        WComboBox axisEdit;
        WCheckBox legendEdit;
        WCheckBox shadowEdit;
        WComboBox labelsEdit;
    }

    /**
     * Controls for series
     */
    private List<SeriesControl> seriesControls_ = new ArrayList<SeriesControl>();

    /**
     * Struct that holds the controls for one axis
     */
    private class AxisControl {
        WCheckBox visibleEdit;
        WComboBox scaleEdit;
        WCheckBox autoEdit;
        WLineEdit minimumEdit;
        WLineEdit maximumEdit;
        WCheckBox gridLinesEdit;
        WLineEdit labelAngleEdit;
    }

    /**
     * Controls for axes
     */
    private List<AxisControl> axisControls_ = new ArrayList<AxisControl>();

    private WLineEdit titleEdit_;
    private WLineEdit chartWidthEdit_;
    private WLineEdit chartHeightEdit_;
    private WComboBox chartOrientationEdit_;

    private void connectSignals(WFormWidget w) {
        w.changed().addListener(this, new Signal.Listener() {
            public void trigger() {
                update();
            }
        });

        if (w instanceof WLineEdit) {
            w.enterPressed().addListener(this, new Signal.Listener() {
                public void trigger() {
                    update();
                }
            });
        }
    }

    private void update() {
        boolean haveLegend = false;
        List<WDataSeries> series = new ArrayList<WDataSeries>();

        for (int i = 1; i < chart_.getModel().getColumnCount(); ++i) {
            SeriesControl sc = seriesControls_.get(i - 1);

            if (sc.enabledEdit.isChecked()) {
                WDataSeries s = new WDataSeries(i);

                switch (sc.typeEdit.getCurrentIndex()) {
                case 0:
                    s.setType(SeriesType.PointSeries);
                    if (sc.markerEdit.getCurrentIndex() == 0)
                        sc.markerEdit.setCurrentIndex(1);
                    break;
                case 1:
                    s.setType(SeriesType.LineSeries);
                    break;
                case 2:
                    s.setType(SeriesType.CurveSeries);
                    break;
                case 3:
                    s.setType(SeriesType.BarSeries);
                    break;
                case 4:
                    s.setType(SeriesType.LineSeries);
                    s.setFillRange(fill_);
                    break;
                case 5:
                    s.setType(SeriesType.CurveSeries);
                    s.setFillRange(fill_);
                    break;
                case 6:
                    s.setType(SeriesType.BarSeries);
                    s.setStacked(true);
                    break;
                case 7:
                    s.setType(SeriesType.LineSeries);
                    s.setFillRange(fill_);
                    s.setStacked(true);
                    break;
                case 8:
                    s.setType(SeriesType.CurveSeries);
                    s.setFillRange(fill_);
                    s.setStacked(true);
                }

                for (MarkerType mt : MarkerType.values()) {
                    if (mt.getValue() == sc.markerEdit.getCurrentIndex()) {
                        s.setMarker(mt);
                    }
                }

                if (sc.axisEdit.getCurrentIndex() == 1) {
                    s.bindToAxis(Axis.Y2Axis);
                }

                if (sc.legendEdit.isChecked()) {
                    s.setLegendEnabled(true);
                    haveLegend = true;
                } else
                    s.setLegendEnabled(false);
                
                if (sc.shadowEdit.isChecked())
                    s.setShadow(new WShadow(3, 3, new WColor(0, 0, 0, 127), 3));
                else
                    s.setShadow(new WShadow());

                switch (sc.labelsEdit.getCurrentIndex()) {
                case 1:
                    s.setLabelsEnabled(Axis.XAxis);
                    break;
                case 2:
                    s.setLabelsEnabled(Axis.YAxis);
                    break;
                case 3:
                    s.setLabelsEnabled(Axis.XAxis);
                    s.setLabelsEnabled(Axis.YAxis);
                    break;
                }

                series.add(s);
            }
        }

        chart_.setSeries(series);

        for (int i = 0; i < 3; ++i) {
            AxisControl sc = axisControls_.get(i);

            Axis axisType = null;
            for (Axis at : Axis.values()) {
                if (at.getValue() == i)
                    axisType = at;
            }
            WAxis axis = chart_.getAxis(axisType);

            axis.setVisible(sc.visibleEdit.isChecked());

            if (sc.scaleEdit.getCount() != 1) {
                int k = sc.scaleEdit.getCurrentIndex();
                if (axis.getId() != Axis.XAxis)
                    k += 1;
                else {
                    if (k == 0)
                        chart_.setType(ChartType.CategoryChart);
                    else
                        chart_.setType(ChartType.ScatterPlot);
                }

                switch (k) {
                case 1:
                    axis.setScale(AxisScale.LinearScale);
                    break;
                case 2:
                    axis.setScale(AxisScale.LogScale);
                    break;
                case 3:
                    axis.setScale(AxisScale.DateScale);
                    break;
                }
            }

            if (sc.autoEdit.isChecked()){
            	if(axis.getScale() == AxisScale.DateScale){
            		axis.setRange(gregDays(1986), gregDays(1988));
            	} else{
            		axis.setRange(WAxis.AUTO_MINIMUM, WAxis.AUTO_MAXIMUM);
            	}
            }else {
            	if (validate(sc.minimumEdit) && validate(sc.maximumEdit)) {
            		double min = getDouble(sc.minimumEdit);
            		double max = getDouble(sc.maximumEdit);

            		if (axis.getScale() == AxisScale.LogScale)
            			if (min <= 0)
            				min = 0.0001;

            		max = Math.max(min, max);

            		if (axis.getScale() == AxisScale.DateScale){
            			double gregDaysMin = gregDays(1900);
            			double gregDaysMax = gregDays(3000);

            			boolean greg_year_validation =
            					(min > gregDaysMin &&
            							min < gregDaysMax &&
            							max > gregDaysMin &&
            							max < gregDaysMax);

            			if(!greg_year_validation){
            				min = gregDaysMin;
            				max = gregDaysMax;
            			}
            		}

            		axis.setRange(min, max);
            	}
            }

            if (validate(sc.labelAngleEdit)) {
                double angle = getDouble(sc.labelAngleEdit);
                axis.setLabelAngle(angle);
            }

            axis.setGridLinesEnabled(sc.gridLinesEdit.isChecked());
        }

        chart_.setTitle(titleEdit_.getText());

        if (validate(chartWidthEdit_) && validate(chartHeightEdit_)) {
            double width = getDouble(chartWidthEdit_);
            double height = getDouble(chartHeightEdit_);

            chart_.resize(new WLength(width), new WLength(height));
        }

        switch (chartOrientationEdit_.getCurrentIndex()) {
        case 0:
            chart_.setOrientation(Orientation.Vertical);
            break;
        case 1:
            chart_.setOrientation(Orientation.Horizontal);
            break;
        }

        chart_.setLegendEnabled(haveLegend);
    }

    private static boolean validate(WFormWidget w) {
        boolean valid = w.validate() == WValidator.State.Valid;

        if (!WApplication.getInstance().getEnvironment().hasJavaScript()) {
            w.setStyleClass(valid ? "" : "Wt-invalid");
            w.setToolTip(valid ? "" : "Invalid value");
        }

        return valid;
    }

    private void addHeader(WTable t, String value) {
        t.getElementAt(0, t.getColumnCount()).addWidget(new WText(value));
    }

    private void addEntry(WAbstractItemModel model, String value) {
        model.insertRows(model.getRowCount(), 1);
        model.setData(model.getRowCount() - 1, 0, value);
    }

    private Double getDouble(WLineEdit edit) {
        try {
            return Double.parseDouble(edit.getText());
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private double gregDays(int year){
    	//the number of julian days until year
    	WDate ans = new WDate(year,1,1);
    	return (double)ans.toJulianDay();
    }
}
