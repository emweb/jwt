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

class AgeFormView extends WTemplateFormView {
	private static Logger logger = LoggerFactory.getLogger(AgeFormView.class);

	public AgeFormView() {
		super();
		this.model_ = new AgeFormModel(this);
		this.setTemplateText(tr("validation-template"));
		this.setFormWidget(AgeFormModel.AgeField, new WLineEdit());
		WPushButton button = new WPushButton("Save");
		this.bindWidget("button", button);
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				AgeFormView.this.process();
			}
		});
		this.updateView(this.model_);
	}

	private void process() {
		this.updateModel(this.model_);
		if (this.model_.validate()) {
			this.updateView(this.model_);
			this.bindString("age-info", new WString("Age of {1} is saved!")
					.arg(StringUtils.asString(this.model_
							.getValue(AgeFormModel.AgeField))));
		} else {
			this.updateView(this.model_);
			WLineEdit viewField = (WLineEdit) this
					.resolveWidget(AgeFormModel.AgeField);
			viewField.setFocus();
		}
	}

	private AgeFormModel model_;
}
