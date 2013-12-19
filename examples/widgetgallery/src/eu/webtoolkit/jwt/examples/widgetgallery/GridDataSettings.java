/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class GridDataSettings extends WContainerWidget {
	private static Logger logger = LoggerFactory
			.getLogger(GridDataSettings.class);

	public GridDataSettings(WCartesian3DChart chart,
			final WAbstractGridData data, WContainerWidget parent) {
		super(parent);
		this.data_ = data;
		this.chart_ = chart;
		this.xStart_ = -10.0;
		this.xEnd_ = 10.0;
		this.yStart_ = -10.0;
		this.yEnd_ = 10.0;
		this.nbXPts_ = 20;
		this.nbYPts_ = 20;
		this.template_ = new WTemplate(WString.tr("dataconfig-template"), this);
		this.template_.addFunction("id", WTemplate.Functions.id);
		this.template_.addFunction("tr", WTemplate.Functions.tr);
		this.template_.addFunction("block", WTemplate.Functions.block);
		this.typeSelection_ = new WComboBox(this);
		this.typeSelection_.addItem("Points");
		this.typeSelection_.addItem("Surface");
		this.template_.bindWidget("typeSelection", this.typeSelection_);
		this.xMin_ = new WLineEdit(this);
		this.template_.bindWidget("x-axisMin", this.xMin_);
		this.xMin_.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		this.xMax_ = new WLineEdit(this);
		this.template_.bindWidget("x-axisMax", this.xMax_);
		this.xMax_.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		this.yMin_ = new WLineEdit(this);
		this.template_.bindWidget("y-axisMin", this.yMin_);
		this.yMin_.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		this.yMax_ = new WLineEdit(this);
		this.template_.bindWidget("y-axisMax", this.yMax_);
		this.yMax_.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		this.nbPtsX_ = new WLineEdit(this);
		this.nbPtsX_.setValidator(new WIntValidator(0, 50));
		this.template_.bindWidget("nbXPoints", this.nbPtsX_);
		this.nbPtsY_ = new WLineEdit(this);
		this.nbPtsY_.setValidator(new WIntValidator(0, 50));
		this.template_.bindWidget("nbYPoints", this.nbPtsY_);
		final WLineEdit ptSize_ = new WLineEdit(this);
		ptSize_.setValidator(new WDoubleValidator(0.0, Double.MAX_VALUE));
		this.template_.bindWidget("pointSize", ptSize_);
		ptSize_.setText(StringUtils.asString(this.data_.getPointSize())
				.toString());
		this.enableMesh_ = new WCheckBox(this);
		this.template_.bindWidget("meshEnabled", this.enableMesh_);
		this.hidden_ = new WCheckBox(this);
		this.hidden_.setCheckState(CheckState.Unchecked);
		this.template_.bindWidget("hiddenEnabled", this.hidden_);
		if (((data.getModel()) instanceof EquidistantGrid ? (EquidistantGrid) (data
				.getModel())
				: null) != null) {
			EquidistantGrid model = ((data.getModel()) instanceof EquidistantGrid ? (EquidistantGrid) (data
					.getModel())
					: null);
			this.xMin_
					.setText(StringUtils.asString(model.getXMin()).toString());
			this.xMax_
					.setText(StringUtils.asString(model.getXMax()).toString());
			this.yMin_
					.setText(StringUtils.asString(model.getYMin()).toString());
			this.yMax_
					.setText(StringUtils.asString(model.getYMax()).toString());
			this.nbPtsX_.setText(StringUtils.asString(model.getNbXPts())
					.toString());
			this.nbPtsY_.setText(StringUtils.asString(model.getNbYPts())
					.toString());
		}
		this.xMin_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.updateData();
			}
		});
		this.xMax_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.updateData();
			}
		});
		this.yMin_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.updateData();
			}
		});
		this.yMax_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.updateData();
			}
		});
		this.nbPtsX_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.updateData();
			}
		});
		this.nbPtsY_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.updateData();
			}
		});
		this.enableMesh_.checked().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.enableMesh();
			}
		});
		this.enableMesh_.unChecked().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.enableMesh();
			}
		});
		this.hidden_.checked().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.hideData();
			}
		});
		this.hidden_.unChecked().addListener(this, new Signal.Listener() {
			public void trigger() {
				GridDataSettings.this.hideData();
			}
		});
		this.typeSelection_.sactivated().addListener(this,
				new Signal1.Listener<WString>() {
					public void trigger(WString e1) {
						GridDataSettings.this.changeChartType(e1);
					}
				});
		ptSize_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				data.setPointSize(StringUtils.asNumber(ptSize_.getText()));
			}
		});
	}

	public GridDataSettings(WCartesian3DChart chart,
			final WAbstractGridData data) {
		this(chart, data, (WContainerWidget) null);
	}

	public void remove() {
		super.remove();
	}

	private void updateData() {
		if (this.nbPtsX_.validate() != WValidator.State.Valid
				|| this.nbPtsY_.validate() != WValidator.State.Valid) {
			return;
		}
		WAbstractItemModel model = this.data_.getModel();
		SombreroData sombreroModel = ((model) instanceof SombreroData ? (SombreroData) (model)
				: null);
		if (sombreroModel != null) {
			sombreroModel.update(StringUtils.asNumber(this.xMin_.getText()),
					StringUtils.asNumber(this.xMax_.getText()), StringUtils
							.asNumber(this.yMin_.getText()), StringUtils
							.asNumber(this.yMax_.getText()), (int) StringUtils
							.asNumber(this.nbPtsX_.getText()),
					(int) StringUtils.asNumber(this.nbPtsY_.getText()));
		} else {
			if (((model) instanceof PlaneData ? (PlaneData) (model) : null) != null) {
				PlaneData planeModel = ((model) instanceof PlaneData ? (PlaneData) (model)
						: null);
				double xDelta = (StringUtils.asNumber(this.xMax_.getText()) - StringUtils
						.asNumber(this.xMin_.getText()))
						/ StringUtils.asNumber(this.nbPtsX_.getText());
				double yDelta = (StringUtils.asNumber(this.yMax_.getText()) - StringUtils
						.asNumber(this.yMin_.getText()))
						/ StringUtils.asNumber(this.nbPtsY_.getText());
				planeModel.update(StringUtils.asNumber(this.xMin_.getText()),
						xDelta, StringUtils.asNumber(this.yMin_.getText()),
						yDelta, (int) StringUtils.asNumber(this.nbPtsX_
								.getText()), (int) StringUtils
								.asNumber(this.nbPtsY_.getText()));
				(((this.data_) instanceof WEquidistantGridData ? (WEquidistantGridData) (this.data_)
						: null)).setXAbscis(StringUtils.asNumber(this.xMin_
						.getText()), xDelta);
				(((this.data_) instanceof WEquidistantGridData ? (WEquidistantGridData) (this.data_)
						: null)).setYAbscis(StringUtils.asNumber(this.yMin_
						.getText()), yDelta);
			}
		}
	}

	private void enableMesh() {
		if (this.data_.getType() != Series3DType.SurfaceSeries3D) {
			this.enableMesh_.setCheckState(CheckState.Unchecked);
			return;
		}
		this.data_
				.setSurfaceMeshEnabled(this.enableMesh_.getCheckState() == CheckState.Checked);
	}

	private void hideData() {
		if (this.hidden_.getCheckState() == CheckState.Checked) {
			this.data_.setHidden(true);
		} else {
			if (this.hidden_.getCheckState() == CheckState.Unchecked) {
				this.data_.setHidden(false);
			}
		}
	}

	private void changeChartType(CharSequence selection) {
		if (selection.equals(new WString("Points"))) {
			this.data_.setType(Series3DType.PointSeries3D);
		} else {
			if (selection.equals(new WString("Surface"))) {
				this.data_.setType(Series3DType.SurfaceSeries3D);
			}
		}
	}

	private WAbstractGridData data_;
	private WCartesian3DChart chart_;
	private WTemplate template_;
	private WLabel title_;
	private WComboBox typeSelection_;
	private WLineEdit xMin_;
	private WLineEdit xMax_;
	private WLineEdit yMin_;
	private WLineEdit yMax_;
	private WLineEdit nbPtsX_;
	private WLineEdit nbPtsY_;
	private WCheckBox enableColorBands_;
	private WLabel enableMeshLabel_;
	private WCheckBox enableMesh_;
	private WCheckBox hidden_;
	private double xStart_;
	private double xEnd_;
	private double yStart_;
	private double yEnd_;
	private int nbXPts_;
	private int nbYPts_;
}
