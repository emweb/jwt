/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for MVC-based charts.
 * <p>
 * 
 * This is an abstract class and should not be used directly.
 * <p>
 * As an abstract base for MVC-based charts, this class manages the model
 * {@link WAbstractChart#setModel(WAbstractItemModel model) setModel()} and
 * provides virtual methods that listen to model changes. In addition, it gives
 * access to generic chart properties such as the title
 * {@link WAbstractChart#setTitle(CharSequence title) setTitle()} and title font
 * {@link WAbstractChart#setTitleFont(WFont titleFont) setTitleFont()}, the
 * chart palette {@link WAbstractChart#setPalette(WChartPalette palette)
 * setPalette()}, plot area padding
 * {@link WAbstractChart#setPlotAreaPadding(int padding, EnumSet sides)
 * setPlotAreaPadding()}, and the background fill color
 * {@link WAbstractChart#setBackground(WBrush background) setBackground()}.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * Styling through CSS is not applicable.
 * <p>
 * 
 * @see eu.webtoolkit.jwt.chart.WCartesianChart
 * @see WPieChart
 */
public abstract class WAbstractChart extends WPaintedWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WAbstractChart.class);

	/**
	 * Destructor.
	 */
	public void remove() {
		;
		super.remove();
	}

	/**
	 * Set the model.
	 * <p>
	 * The model is used by the chart to get its data. Ownership of the model is
	 * not transferred, and if a previous model was set it is not deleted.
	 * <p>
	 * The default model is a 0 model.
	 * <p>
	 * 
	 * @see WAbstractChart#getModel()
	 */
	public void setModel(WAbstractItemModel model) {
		if (this.model_ != null) {
			for (int i = 0; i < this.modelConnections_.size(); ++i) {
				this.modelConnections_.get(i).disconnect();
			}
			this.modelConnections_.clear();
		}
		this.model_ = model;
		this.modelConnections_.add(this.model_.columnsInserted().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WAbstractChart.this.modelColumnsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.columnsRemoved().addListener(
				this, new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WAbstractChart.this.modelColumnsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.rowsInserted().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WAbstractChart.this.modelRowsInserted(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.rowsRemoved().addListener(this,
				new Signal3.Listener<WModelIndex, Integer, Integer>() {
					public void trigger(WModelIndex e1, Integer e2, Integer e3) {
						WAbstractChart.this.modelRowsRemoved(e1, e2, e3);
					}
				}));
		this.modelConnections_.add(this.model_.dataChanged().addListener(this,
				new Signal2.Listener<WModelIndex, WModelIndex>() {
					public void trigger(WModelIndex e1, WModelIndex e2) {
						WAbstractChart.this.modelDataChanged(e1, e2);
					}
				}));
		this.modelConnections_.add(this.model_.layoutChanged().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						WAbstractChart.this.modelReset();
					}
				}));
		this.modelConnections_.add(this.model_.modelReset().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WAbstractChart.this.modelReset();
					}
				}));
		this.modelConnections_.add(this.model_.headerDataChanged().addListener(
				this, new Signal3.Listener<Orientation, Integer, Integer>() {
					public void trigger(Orientation e1, Integer e2, Integer e3) {
						WAbstractChart.this.modelHeaderDataChanged(e1, e2, e3);
					}
				}));
		this.modelChanged();
	}

	/**
	 * Returns the model.
	 * <p>
	 * 
	 * @see WAbstractChart#setModel(WAbstractItemModel model)
	 */
	public WAbstractItemModel getModel() {
		return this.model_;
	}

	/**
	 * Sets a background for the chart.
	 * <p>
	 * Set the background color for the main plot area.
	 * <p>
	 * The default is a completely transparent background.
	 * <p>
	 * 
	 * @see WAbstractChart#getBackground()
	 */
	public void setBackground(WBrush background) {
		if (!ChartUtils.equals(this.background_, background)) {
			this.background_ = background;
			update();
		}
		;
	}

	/**
	 * Returns the background of the chart.
	 * <p>
	 * 
	 * @see WAbstractChart#setBackground(WBrush background)
	 */
	public WBrush getBackground() {
		return this.background_;
	}

	/**
	 * Set a palette for the chart.
	 * <p>
	 * A palette is used to provide the style information to render the chart
	 * series. Ownership of the palette is transferred to the chart.
	 * <p>
	 * The default palette is dependent on the chart type.
	 * <p>
	 * 
	 * @see WAbstractChart#getPalette()
	 */
	public void setPalette(WChartPalette palette) {
		;
		this.palette_ = palette;
		this.update();
	}

	/**
	 * Returns the palette for the chart.
	 * <p>
	 * 
	 * @see WAbstractChart#setPalette(WChartPalette palette)
	 */
	public WChartPalette getPalette() {
		return this.palette_;
	}

	/**
	 * Set an internal margin for the main plot area.
	 * <p>
	 * This configures the area (in pixels) around the plot area that is
	 * available for axes, labels, and titles. You need to set this
	 * appropriately so that labels fit inside these margins.
	 * <p>
	 * The default is dependent on the chart type.
	 */
	public void setPlotAreaPadding(int padding, EnumSet<Side> sides) {
		if (!EnumUtils.mask(sides, Side.Top).isEmpty()) {
			this.padding_[0] = padding;
		}
		if (!EnumUtils.mask(sides, Side.Right).isEmpty()) {
			this.padding_[1] = padding;
		}
		if (!EnumUtils.mask(sides, Side.Bottom).isEmpty()) {
			this.padding_[2] = padding;
		}
		if (!EnumUtils.mask(sides, Side.Left).isEmpty()) {
			this.padding_[3] = padding;
		}
	}

	/**
	 * Set an internal margin for the main plot area.
	 * <p>
	 * Calls {@link #setPlotAreaPadding(int padding, EnumSet sides)
	 * setPlotAreaPadding(padding, EnumSet.of(side, sides))}
	 */
	public final void setPlotAreaPadding(int padding, Side side, Side... sides) {
		setPlotAreaPadding(padding, EnumSet.of(side, sides));
	}

	/**
	 * Set an internal margin for the main plot area.
	 * <p>
	 * Calls {@link #setPlotAreaPadding(int padding, EnumSet sides)
	 * setPlotAreaPadding(padding, Side.All)}
	 */
	public final void setPlotAreaPadding(int padding) {
		setPlotAreaPadding(padding, Side.All);
	}

	/**
	 * Returns the internal margin for the main plot area.
	 * <p>
	 * 
	 * @see WAbstractChart#setPlotAreaPadding(int padding, EnumSet sides)
	 */
	public int getPlotAreaPadding(Side side) {
		switch (side) {
		case Top:
			return this.padding_[0];
		case Right:
			return this.padding_[1];
		case Bottom:
			return this.padding_[2];
		case Left:
			return this.padding_[3];
		default:
			logger.error(new StringWriter().append(
					"plotAreaPadding(): improper side.").toString());
			return 0;
		}
	}

	/**
	 * Set a chart title.
	 * <p>
	 * The title is displayed on top of the chart, using the
	 * {@link WAbstractChart#getTitleFont() getTitleFont()}.
	 * <p>
	 * The default title is an empty title (&quot;&quot;).
	 * <p>
	 * 
	 * @see WAbstractChart#getTitle()
	 */
	public void setTitle(CharSequence title) {
		if (!ChartUtils.equals(this.title_, WString.toWString(title))) {
			this.title_ = WString.toWString(title);
			update();
		}
		;
	}

	/**
	 * Return the chart title.
	 * <p>
	 * 
	 * @see WAbstractChart#getTitle()
	 */
	public WString getTitle() {
		return this.title_;
	}

	/**
	 * Set the font for the chart title.
	 * <p>
	 * Changes the font for the chart title.
	 * <p>
	 * The default title font is a 15 point Sans Serif font.
	 * <p>
	 * 
	 * @see WAbstractChart#getTitleFont()
	 * @see WAbstractChart#setTitle(CharSequence title)
	 */
	public void setTitleFont(WFont titleFont) {
		if (!ChartUtils.equals(this.titleFont_, titleFont)) {
			this.titleFont_ = titleFont;
			update();
		}
		;
	}

	/**
	 * Returns the font for the chart title.
	 * <p>
	 * 
	 * @see WAbstractChart#setTitleFont(WFont titleFont)
	 */
	public WFont getTitleFont() {
		return this.titleFont_;
	}

	void setAxisTitleFont(WFont titleFont) {
		if (!ChartUtils.equals(this.axisTitleFont_, titleFont)) {
			this.axisTitleFont_ = titleFont;
			update();
		}
		;
	}

	WFont getAxisTitleFont() {
		return this.titleFont_;
	}

	/**
	 * Paint the chart in a rectangle of the given painter.
	 * <p>
	 * Paints the chart inside the <i>painter</i>, in the area indicated by
	 * <i>rectangle</i>. When <i>rectangle</i> is a null rectangle, the entire
	 * painter {@link WPainter#getWindow() window} is used.
	 */
	public abstract void paint(WPainter painter, WRectF rectangle);

	/**
	 * Paint the chart in a rectangle of the given painter.
	 * <p>
	 * Calls {@link #paint(WPainter painter, WRectF rectangle) paint(painter,
	 * new WRectF())}
	 */
	public final void paint(WPainter painter) {
		paint(painter, new WRectF());
	}

	WAbstractChart(WContainerWidget parent) {
		super(parent);
		this.model_ = null;
		this.background_ = new WBrush(WColor.white);
		this.palette_ = null;
		this.title_ = new WString();
		this.titleFont_ = new WFont();
		this.axisTitleFont_ = new WFont();
		this.modelConnections_ = new ArrayList<AbstractSignal.Connection>();
		this.titleFont_.setFamily(WFont.GenericFamily.SansSerif);
		this.titleFont_.setSize(WFont.Size.FixedSize, new WLength(15,
				WLength.Unit.Point));
		this.setPlotAreaPadding(5, EnumSet.of(Side.Left, Side.Right));
		this.setPlotAreaPadding(5, EnumSet.of(Side.Top, Side.Bottom));
	}

	private WAbstractItemModel model_;
	private WBrush background_;
	private WChartPalette palette_;
	private int[] padding_ = new int[4];
	private WString title_;
	private WFont titleFont_;
	private WFont axisTitleFont_;
	private List<AbstractSignal.Connection> modelConnections_;

	/**
	 * Method called whenever the entire model was changed.
	 * <p>
	 * 
	 * @see WAbstractChart#setModel(WAbstractItemModel model)
	 */
	protected void modelChanged() {
	}

	/**
	 * Method called whenever the entire model was reset.
	 * <p>
	 * Bound to the {@link WAbstractItemModel#modelReset()
	 * WAbstractItemModel#modelReset()} and
	 * {@link WAbstractItemModel#layoutChanged()
	 * WAbstractItemModel#layoutChanged()} signals.
	 */
	protected void modelReset() {
	}

	/**
	 * Method called when colums have been inserted in the model.
	 * <p>
	 * 
	 * @see WAbstractItemModel#columnsInserted()
	 */
	protected abstract void modelColumnsInserted(WModelIndex parent, int start,
			int end);

	/**
	 * Method called when colums have been removed from the model.
	 * <p>
	 * 
	 * @see WAbstractItemModel#columnsRemoved()
	 */
	protected abstract void modelColumnsRemoved(WModelIndex parent, int start,
			int end);

	/**
	 * Method called when rows have been inserted from the model.
	 * <p>
	 * 
	 * @see WAbstractItemModel#rowsInserted()
	 */
	protected abstract void modelRowsInserted(WModelIndex parent, int start,
			int end);

	/**
	 * Method called when rows have been removed from the model.
	 * <p>
	 * 
	 * @see WAbstractItemModel#rowsRemoved()
	 */
	protected abstract void modelRowsRemoved(WModelIndex parent, int start,
			int end);

	/**
	 * Method called when data has been changed in the model.
	 * <p>
	 * 
	 * @see WAbstractItemModel#dataChanged()
	 */
	protected abstract void modelDataChanged(WModelIndex topLeft,
			WModelIndex bottomRight);

	/**
	 * Method called when header data has been changed in the model.
	 * <p>
	 * 
	 * @see WAbstractItemModel#headerDataChanged()
	 */
	protected abstract void modelHeaderDataChanged(Orientation orientation,
			int start, int end);
	// private void (T m, T v) ;
}
