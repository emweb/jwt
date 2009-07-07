/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.form;

import java.util.Date;
import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Side;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WComboBox;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WCssDecorationStyle;
import eu.webtoolkit.jwt.WDate;
import eu.webtoolkit.jwt.WDatePicker;
import eu.webtoolkit.jwt.WDateValidator;
import eu.webtoolkit.jwt.WFont;
import eu.webtoolkit.jwt.WFormWidget;
import eu.webtoolkit.jwt.WIntValidator;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLength;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WSound;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WTable;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextArea;
import eu.webtoolkit.jwt.WValidator;

/**
 * A simple Form.
 *
 * Shows how a simple form can made, with an emphasis on how
 * to handle validation.
 * 
 * When submitting the form, not all fields are filled in in a valid way,
 * a beep sound will be played.
 */
public class Form extends WTable
{
  /**
   * Instantiate a new form.
   */
	public Form(WContainerWidget parent) {
		super(parent);
		createUI();
	}

 /**
  * The user selected a new country: adjust the cities combo box.
   */
	private void countryChanged() {
		  cityEdit_.clear();
		  cityEdit_.addItem("");
		  cityEdit_.setCurrentIndex(-1);

		  switch (countryEdit_.getCurrentIndex()) {
		  case 0:
		    break;
		  case 1:
		    cityEdit_.addItem("Antwerp");
		    cityEdit_.addItem("Brussels");
		    cityEdit_.addItem("Oekene");
		    break;
		  case 2:
		    cityEdit_.addItem("Amsterdam");
		    cityEdit_.addItem("Den Haag");
		    cityEdit_.addItem("Rotterdam");
		    break;
		  case 3:
		    cityEdit_.addItem("London");
		    cityEdit_.addItem("Bristolt");
		    cityEdit_.addItem("Oxford");
		    cityEdit_.addItem("Stonehenge");
		    break;
		  case 4:
		    cityEdit_.addItem("Boston");
		    cityEdit_.addItem("Chicago");
		    cityEdit_.addItem("Los Angelos");
		    cityEdit_.addItem("New York");
		    break;
		  }    
	}

  /**
   * Submit the form.
   */
	private void submit() {
		  if (validate()) {
			    // do something useful with the data...
			    String name
			      = firstNameEdit_.getText() + " " + nameEdit_.getText();

			    String remarks
			      = remarksEdit_.getText();

			    clear();

			    // WMessage with arguments is not yet implemented...
			    new WText("<p>Thank you, "
				      + name
				      + ", for all this precious data.</p>", getElementAt(0, 0));
			    
			    if (remarks.length() > 0)
			      new WText("<p>You had some remarks. Splendid !</p>", getElementAt(0, 0));

			    WApplication.getInstance().quit();
			  }
		  else {
			  //play a error mp3
			  WSound beep = new WSound("sounds/beep.mp3", this);
			  beep.play();
		  }
	}


	private void createUI() {
		  WLabel label;
		  int row = 0;

		  // Title
		  getElementAt(row, 0).setColumnSpan(3);
		  getElementAt(row, 0).setContentAlignment(EnumSet.of(AlignmentFlag.AlignTop ,AlignmentFlag.AlignCenter));
		  getElementAt(row, 0).setPadding(new WLength(10));
		  WText title = new WText(tr("example.form"),
					   getElementAt(row, 0));
		  title.getDecorationStyle().getFont().setSize(WFont.Size.XLarge);

		  // error messages
		  ++row;
		  getElementAt(row, 0).setColumnSpan(3);
		  feedbackMessages_ = getElementAt(row, 0);
		  feedbackMessages_.setPadding(new WLength(5));

		  WCssDecorationStyle errorStyle = feedbackMessages_.getDecorationStyle();
		  errorStyle.setForegroundColor(WColor.red);
		  errorStyle.getFont().setSize(WFont.Size.Smaller);
		  errorStyle.getFont().setWeight(WFont.Weight.Bold);
		  errorStyle.getFont().setStyle(WFont.Style.Italic);

		  // Name
		  ++row;
		  nameEdit_ = new WLineEdit(getElementAt(row, 2));
		  label = new WLabel(tr("example.name"), getElementAt(row, 0));
		  label.setBuddy(nameEdit_);
		  nameEdit_.setValidator(new WValidator(true));
		  nameEdit_.enterPressed().addListener(this, new Signal.Listener(){
			public void trigger() {
				submit();
			}
		  });

		  // First name
		  ++row;
		  firstNameEdit_ = new WLineEdit(getElementAt(row, 2));
		  label = new WLabel(tr("example.firstname"), getElementAt(row,0));
		  label.setBuddy(firstNameEdit_);

		  // Country
		  ++row;
		  countryEdit_ = new WComboBox(getElementAt(row, 2));
		  countryEdit_.addItem("");
		  countryEdit_.addItem("Belgium");
		  countryEdit_.addItem("Netherlands");
		  countryEdit_.addItem("United Kingdom");
		  countryEdit_.addItem("United States");
		  label = new WLabel(tr("example.country"), getElementAt(row, 0));
		  label.setBuddy(countryEdit_);
		  countryEdit_.setValidator(new WValidator(true));
		  countryEdit_.changed().addListener(this, new Signal.Listener(){
				public void trigger() {
					countryChanged();
				}
			  });

		  // City
		  ++row;
		  cityEdit_ = new WComboBox(getElementAt(row, 2));
		  cityEdit_.addItem(tr("example.choosecountry"));
		  label = new WLabel(tr("example.city"), getElementAt(row, 0));
		  label.setBuddy(cityEdit_);

		  // Birth date
		  ++row;


		  birthDateEdit_ = new WLineEdit(getElementAt(row, 2));
		  label = new WLabel(tr("example.birthdate"), getElementAt(row, 0));
		  label.setBuddy(birthDateEdit_);
		  WDateValidator dv = new WDateValidator(new WDate(1900,1,1),new WDate(new Date()));
		  dv.setFormat("dd/MM/yyyy");
		  birthDateEdit_.setValidator(dv);
		  birthDateEdit_.getValidator().setMandatory(true);

		  WDatePicker picker = new WDatePicker(new WText("..."),
							birthDateEdit_, true,
							getElementAt(row, 2));

		  // Child count
		  ++row;
		  childCountEdit_ = new WLineEdit("0", getElementAt(row, 2));
		  label = new WLabel(tr("example.childcount"),
				     getElementAt(row, 0));
		  label.setBuddy(childCountEdit_);
		  childCountEdit_.setValidator(new WIntValidator(0,30));
		  childCountEdit_.getValidator().setMandatory(true);

		  ++row;
		  remarksEdit_ = new WTextArea(getElementAt(row, 2));
		  remarksEdit_.setColumns(40);
		  remarksEdit_.setRows(5);
		  label = new WLabel(tr("example.remarks"),
				     getElementAt(row, 0));
		  label.setBuddy(remarksEdit_);

		  // Submit
		  ++row;
		  WPushButton submit = new WPushButton(tr("submit"),
							getElementAt(row, 0));
		  submit.clicked().addListener(this, new Signal.Listener(){
				public void trigger() {
					submit();
				}
			  });
		  submit.setMargin(15, Side.Top);
		  getElementAt(row, 0).setColumnSpan(3);
		  getElementAt(row, 0).setContentAlignment(AlignmentFlag.AlignTop ,AlignmentFlag.AlignCenter);

		  // Set column widths for label and validation icon
		  getElementAt(2, 0).resize(new WLength(30, WLength.Unit.FontEx), WLength.Auto);
		  getElementAt(2, 1).resize(new WLength(20), WLength.Auto);
	}
 
	private WContainerWidget feedbackMessages_;

	private  WLineEdit nameEdit_;
	private WLineEdit firstNameEdit_;

	private WComboBox countryEdit_;
	private WComboBox cityEdit_;

	private WLineEdit birthDateEdit_;
	private WLineEdit childCountEdit_;

	private WTextArea remarksEdit_;

  /**
   * Validate the form, and return whether successful.
   */
  
   private boolean  validate() {
	   feedbackMessages_.clear();
	   boolean valid = true;

	   if (!checkValid(nameEdit_, tr("error.name")))
	     valid = false;
	   if (!checkValid(countryEdit_, tr("error.country")))
	     valid = false;
	   if (!checkValid(birthDateEdit_, tr("error.birthdate")))
	     valid = false;
	   if (!checkValid(childCountEdit_, tr("error.childcount")))
	     valid = false;

	   return valid;
   }

  /** Validate a single form field.
   
   Checks the given field, and appends the given text to the error
    messages on problems.
   */
   private boolean checkValid(WFormWidget edit, WString text) {
	   if (edit.validate() != WValidator.State.Valid) {
		    feedbackMessages_.addWidget(new WText(text));
		    feedbackMessages_.addWidget(new WBreak());
		    edit.getLabel().getDecorationStyle().setForegroundColor(WColor.red);
		    edit.setStyleClass("Wt-invalid");

		    return false;
		  } else {
		    edit.getLabel().getDecorationStyle().setForegroundColor(new WColor());    
		    edit.setStyleClass("");

		    return true;
		  }
   }
} 
