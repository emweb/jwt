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

class UserFormView extends WTemplateFormView {
	private static Logger logger = LoggerFactory.getLogger(UserFormView.class);

	public UserFormView() {
		super();
		this.model = new UserFormModel(this);
		this.setTemplateText(tr("userForm-template"));
		this.addFunction("id", WTemplate.Functions.id);
		this.addFunction("block", WTemplate.Functions.id);
		this.setFormWidget(UserFormModel.FirstNameField, new WLineEdit());
		this.setFormWidget(UserFormModel.LastNameField, new WLineEdit());
		final WComboBox countryCB = new WComboBox();
		countryCB.setModel(this.model.getCountryModel());
		countryCB.activated().addListener(this, new Signal.Listener() {
			public void trigger() {
				String code = UserFormView.this.model.countryCode(countryCB
						.getCurrentIndex());
				UserFormView.this.model.updateCityModel(code);
			}
		});
		this.setFormWidget(UserFormModel.CountryField, countryCB,
				new WTemplateFormView.FieldView() {
					public void updateViewValue() {
						String code = ((String) UserFormView.this.model
								.getValue(UserFormModel.CountryField));
						int row = UserFormView.this.model.countryModelRow(code);
						countryCB.setCurrentIndex(row);
					}

					public void updateModelValue() {
						String code = UserFormView.this.model
								.countryCode(countryCB.getCurrentIndex());
						UserFormView.this.model.setValue(
								UserFormModel.CountryField, code);
					}
				});
		WComboBox cityCB = new WComboBox();
		cityCB.setModel(this.model.getCityModel());
		this.setFormWidget(UserFormModel.CityField, cityCB);
		final WDateEdit dateEdit = new WDateEdit();
		this.setFormWidget(UserFormModel.BirthField, dateEdit,
				new WTemplateFormView.FieldView() {
					public void updateViewValue() {
						WDate date = ((WDate) UserFormView.this.model
								.getValue(UserFormModel.BirthField));
						dateEdit.setDate(date);
					}

					public void updateModelValue() {
						WDate date = dateEdit.getDate();
						UserFormView.this.model.setValue(
								UserFormModel.BirthField, date);
					}
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
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				UserFormView.this.process();
			}
		});
		this.updateView(this.model);
	}

	private void process() {
		this.updateModel(this.model);
		if (this.model.validate()) {
			this.bindString("submit-info", new WString("Saved user data for ")
					.append(this.model.getUserData()), TextFormat.PlainText);
			this.updateView(this.model);
			WLineEdit viewField = (WLineEdit) this
					.resolveWidget(UserFormModel.FirstNameField);
			viewField.setFocus(true);
		} else {
			this.bindEmpty("submit-info");
			this.updateView(this.model);
		}
	}

	private UserFormModel model;
}
