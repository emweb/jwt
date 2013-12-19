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

class CategoryDataSettings extends WContainerWidget {
	private static Logger logger = LoggerFactory
			.getLogger(CategoryDataSettings.class);

	public CategoryDataSettings(final WAbstractGridData data,
			WContainerWidget parent) {
		super(parent);
		this.data_ = data;
		this.template_ = new WTemplate(WString.tr("categorydata-config"), this);
		this.barWidthX_ = new WLineEdit(this);
		this.template_.bindWidget("barWidthX", this.barWidthX_);
		this.barWidthX_.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		this.barWidthX_.setText(StringUtils.asString(this.data_.getBarWidthX())
				.toString());
		this.barWidthY_ = new WLineEdit(this);
		this.template_.bindWidget("barWidthY", this.barWidthY_);
		this.barWidthY_.setValidator(new WDoubleValidator(-Double.MAX_VALUE,
				Double.MAX_VALUE));
		this.barWidthY_.setText(StringUtils.asString(this.data_.getBarWidthY())
				.toString());
		this.hideData_ = new WCheckBox(this);
		this.template_.bindWidget("hide", this.hideData_);
		this.barWidthX_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				CategoryDataSettings.this.adjustBarWidth();
			}
		});
		this.barWidthY_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				CategoryDataSettings.this.adjustBarWidth();
			}
		});
		this.hideData_.checked().addListener(this, new Signal.Listener() {
			public void trigger() {
				data.setHidden(true);
			}
		});
		this.hideData_.unChecked().addListener(this, new Signal.Listener() {
			public void trigger() {
				data.setHidden(false);
			}
		});
	}

	public CategoryDataSettings(final WAbstractGridData data) {
		this(data, (WContainerWidget) null);
	}

	private void adjustBarWidth() {
		this.data_.setBarWidth((float) StringUtils.asNumber(this.barWidthX_
				.getText()), (float) StringUtils.asNumber(this.barWidthY_
				.getText()));
	}

	private WAbstractGridData data_;
	private WTemplate template_;
	private WLineEdit barWidthX_;
	private WLineEdit barWidthY_;
	private WCheckBox hideData_;
}
