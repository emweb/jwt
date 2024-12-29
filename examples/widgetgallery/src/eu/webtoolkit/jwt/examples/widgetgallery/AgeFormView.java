/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AgeFormView extends WTemplateFormView {
  private static Logger logger = LoggerFactory.getLogger(AgeFormView.class);

  public AgeFormView(WContainerWidget parentContainer) {
    super();
    this.model_ = null;
    this.model_ = new AgeFormModel();
    this.setTemplateText(tr("validation-template"));
    this.setFormWidget(AgeFormModel.AgeField, new WLineEdit());
    WPushButton button = new WPushButton("Save");
    this.bindWidget("button", button);
    button
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              AgeFormView.this.process();
            });
    this.updateView(this.model_);
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public AgeFormView() {
    this((WContainerWidget) null);
  }

  private void process() {
    this.updateModel(this.model_);
    if (this.model_.validate()) {
      this.updateView(this.model_);
      this.bindString(
          "age-info",
          new WString("Age of {1} is saved!")
              .arg(StringUtils.asString(this.model_.getValue(AgeFormModel.AgeField))));
    } else {
      this.updateView(this.model_);
      WLineEdit viewField = (WLineEdit) this.resolveWidget(AgeFormModel.AgeField);
      viewField.setFocus(true);
    }
  }

  private AgeFormModel model_;
}
