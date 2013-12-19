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

class ScatterDataSettings extends WContainerWidget {
	private static Logger logger = LoggerFactory
			.getLogger(ScatterDataSettings.class);

	public ScatterDataSettings(final WScatterData data, WContainerWidget parent) {
		super(parent);
		this.template_ = new WTemplate(WString.tr("scatterdata-config"), this);
		final WLineEdit ptSize_ = new WLineEdit(this);
		ptSize_.setValidator(new WDoubleValidator(0.0, Double.MAX_VALUE));
		this.template_.bindWidget("pointSize", ptSize_);
		ptSize_.setText(StringUtils.asString(data.getPointSize()).toString());
		this.enableDroplines_ = new WCheckBox(this);
		this.template_.bindWidget("enableDroplines", this.enableDroplines_);
		this.hide_ = new WCheckBox(this);
		this.template_.bindWidget("hide", this.hide_);
		ptSize_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				data.setPointSize(StringUtils.asNumber(ptSize_.getText()));
			}
		});
		this.enableDroplines_.checked().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						data.setDroplinesEnabled(true);
					}
				});
		this.enableDroplines_.unChecked().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						data.setDroplinesEnabled(false);
					}
				});
		this.hide_.checked().addListener(this, new Signal.Listener() {
			public void trigger() {
				data.setHidden(true);
			}
		});
		this.hide_.unChecked().addListener(this, new Signal.Listener() {
			public void trigger() {
				data.setHidden(false);
			}
		});
	}

	public ScatterDataSettings(final WScatterData data) {
		this(data, (WContainerWidget) null);
	}

	private WTemplate template_;
	private WCheckBox enableDroplines_;
	private WCheckBox hide_;
}
