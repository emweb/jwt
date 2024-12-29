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

class UserFormView extends WTemplateFormView {
  private static Logger logger = LoggerFactory.getLogger(UserFormView.class);

  public UserFormView(WContainerWidget parentContainer) {
    super();
    this.model = null;
    this.model = new UserFormModel();
    this.setTemplateText(tr("userForm-template"));
    this.addFunction("id", WTemplate.Functions.id);
    this.addFunction("block", WTemplate.Functions.id);
    this.setFormWidget(UserFormModel.FirstNameField, new WLineEdit());
    this.setFormWidget(UserFormModel.LastNameField, new WLineEdit());
    WComboBox countryCB = new WComboBox();
    final WComboBox countryCB_ = countryCB;
    countryCB.setModel(this.model.getCountryModel());
    countryCB_
        .activated()
        .addListener(
            this,
            () -> {
              String code = UserFormView.this.model.countryCode(countryCB_.getCurrentIndex());
              UserFormView.this.model.updateCityModel(code);
            });
    this.setFormWidget(
        UserFormModel.CountryField,
        countryCB,
        () -> {
          String code =
              StringUtils.asString(UserFormView.this.model.getValue(UserFormModel.CountryField))
                  .toString();
          int row = UserFormView.this.model.countryModelRow(code);
          countryCB_.setCurrentIndex(row);
        },
        () -> {
          String code = UserFormView.this.model.countryCode(countryCB_.getCurrentIndex());
          UserFormView.this.model.setValue(UserFormModel.CountryField, code);
        });
    WComboBox cityCB = new WComboBox();
    cityCB.setModel(this.model.getCityModel());
    this.setFormWidget(UserFormModel.CityField, cityCB);
    WDateEdit dateEdit = new WDateEdit();
    final WDateEdit dateEdit_ = dateEdit;
    this.setFormWidget(
        UserFormModel.BirthField,
        dateEdit,
        () -> {
          WDate date = ((WDate) UserFormView.this.model.getValue(UserFormModel.BirthField));
          dateEdit_.setDate(date);
        },
        () -> {
          WDate date = dateEdit_.getDate();
          UserFormView.this.model.setValue(UserFormModel.BirthField, date);
        });
    this.setFormWidget(UserFormModel.ChildrenField, new WSpinBox());
    WTextArea remarksTA = new WTextArea();
    remarksTA.setColumns(40);
    remarksTA.setRows(5);
    this.setFormWidget(UserFormModel.RemarksField, remarksTA);
    WString title = new WString("Create new user");
    this.bindString("title", title);
    WPushButton button = new WPushButton("Save");
    this.bindWidget("submit-button", button);
    this.bindString("submit-info", new WString());
    button
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              UserFormView.this.process();
            });
    this.updateView(this.model);
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public UserFormView() {
    this((WContainerWidget) null);
  }

  private void process() {
    this.updateModel(this.model);
    if (this.model.validate()) {
      this.bindString(
          "submit-info",
          new WString("Saved user data for ").append(this.model.getUserData()),
          TextFormat.Plain);
      this.updateView(this.model);
      WLineEdit viewField = (WLineEdit) this.resolveWidget(UserFormModel.FirstNameField);
      viewField.setFocus(true);
    } else {
      this.bindEmpty("submit-info");
      this.updateView(this.model);
    }
  }

  private UserFormModel model;
}
