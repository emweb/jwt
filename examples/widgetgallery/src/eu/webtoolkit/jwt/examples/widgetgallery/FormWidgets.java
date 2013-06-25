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

class FormWidgets extends TopicWidget {
	private static Logger logger = LoggerFactory.getLogger(FormWidgets.class);

	public FormWidgets() {
		super();
		addText(tr("formwidgets-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("Introduction", this.introduction()).setPathComponent("");
		menu.addItem("Line/Text editor", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.textEditors();
					}
				}));
		menu.addItem("Check boxes", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.checkBox();
					}
				}));
		menu.addItem("Radio buttons", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.radioButton();
					}
				}));
		menu.addItem("Combo box", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.comboBox();
					}
				}));
		menu.addItem("Selection box", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.selectionBox();
					}
				}));
		menu.addItem("Autocomplete", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.autoComplete();
					}
				}));
		menu.addItem("Date entry", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.dateEntry();
					}
				}));
		menu.addItem("In-place edit", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.inPlaceEdit();
					}
				}));
		menu.addItem("Slider", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return FormWidgets.this.slider();
			}
		}));
		menu.addItem("Progress bar", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.progressBar();
					}
				}));
		menu.addItem("File upload", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.fileUpload();
					}
				}));
		menu.addItem("Push button", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.pushButton();
					}
				}));
		menu.addItem("Validation", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.validation();
					}
				}));
		menu.addItem("Integration example", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.example();
					}
				}));
	}

	private WWidget introduction() {
		WTemplate result = new TopicTemplate("forms-introduction");
		result.bindWidget("SimpleForm", SimpleForm());
		result.bindWidget("FormModel", FormModel());
		result.bindString("simpleForm-template",
				reindent(tr("simpleForm-template")), TextFormat.PlainText);
		result.bindString("form-field", reindent(tr("form-field")),
				TextFormat.PlainText);
		result.bindString("userForm-template",
				reindent(tr("userForm-template")), TextFormat.PlainText);
		return result;
	}

	private WWidget textEditors() {
		WTemplate result = new TopicTemplate("forms-textEditors");
		result.bindWidget("LineEdit", LineEdit());
		result.bindWidget("LineEditEvent", LineEditEvent());
		result.bindWidget("TextArea", TextArea());
		result.bindWidget("TextEdit", TextEdit());
		result.bindWidget("SpinBox", SpinBox());
		result.bindWidget("TextSide", TextSide());
		result.bindString("lineEdit-template",
				reindent(tr("lineEdit-template")), TextFormat.PlainText);
		result.bindString("editSide-template",
				reindent(tr("editSide-template")), TextFormat.PlainText);
		return result;
	}

	private WWidget checkBox() {
		WTemplate result = new TopicTemplate("forms-checkBox");
		result.bindWidget("CheckBoxInline", CheckBoxInline());
		result.bindWidget("CheckBoxStack", CheckBoxStack());
		return result;
	}

	private WWidget radioButton() {
		WTemplate result = new TopicTemplate("forms-radioButton");
		result.bindWidget("RadioButtonsLoose", RadioButtonsLoose());
		result.bindWidget("RadioButtonGroup", RadioButtonGroup());
		result.bindWidget("RadioButtonStack", RadioButtonStack());
		result.bindWidget("RadioButtonsActivated", RadioButtonsActivated());
		return result;
	}

	private WWidget comboBox() {
		WTemplate result = new TopicTemplate("forms-comboBox");
		result.bindWidget("ComboBox", ComboBox());
		result.bindWidget("ComboBoxActivated", ComboBoxActivated());
		result.bindWidget("ComboBoxModel", ComboBoxModel());
		return result;
	}

	private WWidget selectionBox() {
		WTemplate result = new TopicTemplate("forms-selectionBox");
		result.bindWidget("SelectionBoxSimple", SelectionBoxSimple());
		result.bindWidget("SelectionBoxExtended", SelectionBoxExtended());
		return result;
	}

	private WWidget autoComplete() {
		WTemplate result = new TopicTemplate("forms-autoComplete");
		result.bindWidget("AutoComplete", AutoComplete());
		return result;
	}

	private WWidget dateEntry() {
		WTemplate result = new TopicTemplate("forms-dateEntry");
		result.bindWidget("CalendarSimple", CalendarSimple());
		result.bindWidget("CalendarExtended", CalendarExtended());
		result.bindWidget("DateEdit", DateEdit());
		result.bindWidget("DatePicker", DatePicker());
		return result;
	}

	private WWidget inPlaceEdit() {
		WTemplate result = new TopicTemplate("forms-inPlaceEdit");
		result.bindWidget("InPlaceEditButtons", InPlaceEditButtons());
		result.bindWidget("InPlaceEdit", InPlaceEdit());
		return result;
	}

	private WWidget slider() {
		WTemplate result = new TopicTemplate("forms-slider");
		result.bindWidget("Slider", Slider());
		result.bindWidget("SliderVertical", SliderVertical());
		return result;
	}

	private WWidget progressBar() {
		WTemplate result = new TopicTemplate("forms-progressBar");
		result.bindWidget("ProgressBar", ProgressBar());
		return result;
	}

	private WWidget fileUpload() {
		WTemplate result = new TopicTemplate("forms-fileUpload");
		result.bindWidget("FileUpload", FileUpload());
		return result;
	}

	private WWidget pushButton() {
		WTemplate result = new TopicTemplate("forms-pushButton");
		result.bindWidget("PushButton", PushButton());
		result.bindWidget("PushButtonOnce", PushButtonOnce());
		result.bindWidget("PushButtonLink", PushButtonLink());
		result.bindWidget("PushButtonDropdownAppended",
				PushButtonDropdownAppended());
		result.bindWidget("PushButtonDropdownPrepended",
				PushButtonDropdownPrepended());
		result.bindWidget("PushButtonColor", PushButtonColor());
		result.bindWidget("PushButtonSize", PushButtonSize());
		result.bindWidget("PushButtonPrimary", PushButtonPrimary());
		result.bindWidget("PushButtonAction", PushButtonAction());
		result.bindString("appendedDropdownButton-template",
				reindent(tr("appendedDropdownButton-template")),
				TextFormat.PlainText);
		result.bindString("prependedDropdownButton-template",
				reindent(tr("prependedDropdownButton-template")),
				TextFormat.PlainText);
		result.bindString("pushButtonColor-template",
				reindent(tr("pushButtonColor-template")), TextFormat.PlainText);
		result.bindString("pushButtonSize-template",
				reindent(tr("pushButtonSize-template")), TextFormat.PlainText);
		result
				.bindString("pushButtonAction-template",
						reindent(tr("pushButtonAction-template")),
						TextFormat.PlainText);
		return result;
	}

	private WWidget validation() {
		WTemplate result = new TopicTemplate("forms-validation");
		result.bindWidget("Validation", Validation());
		result.bindWidget("ValidationDate", ValidationDate());
		result.bindWidget("ValidationModel", ValidationModel());
		result.bindString("validation-template",
				reindent(tr("validation-template")), TextFormat.PlainText);
		return result;
	}

	private WWidget example() {
		WTemplate result = new TopicTemplate("forms-integration-example");
		result.bindWidget("FormModel", FormModel());
		result.bindString("form-field", reindent(tr("form-field")),
				TextFormat.PlainText);
		result.bindString("userForm-template",
				reindent(tr("userForm-template")), TextFormat.PlainText);
		return result;
	}

	WWidget SimpleForm() {
		WTemplate result = new WTemplate(WString.tr("simpleForm-template"));
		result.addFunction("tr", WTemplate.Functions.tr);
		result.addFunction("id", WTemplate.Functions.id);
		final WLineEdit name = new WLineEdit();
		result.bindWidget("name", name);
		name.setEmptyText("first name");
		WPushButton button = new WPushButton("OK");
		result.bindWidget("button", button);
		final WText out = new WText("");
		result.bindWidget("out", out);
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out
						.setText("Hello, "
								+ name.getText()
								+ "! I just want to help you... You"
								+ " could complete this simple form by adding validation.");
			}
		});
		return result;
	}

	static Map<String, String> getCountryMap() {
		Map<String, String> retval = new HashMap<String, String>();
		retval.put("BE", "Belgium");
		retval.put("NL", "Netherlands");
		retval.put("UK", "United Kingdom");
		retval.put("US", "United States");
		return retval;
	}

	static Map<String, List<String>> getCityMap() {
		List<String> beCities = new ArrayList<String>();
		beCities.add("Antwerp");
		beCities.add("Bruges");
		beCities.add("Brussels");
		beCities.add("Ghent");
		List<String> nlCities = new ArrayList<String>();
		nlCities.add("Amsterdam");
		nlCities.add("Eindhoven");
		nlCities.add("Rotterdam");
		nlCities.add("The Hague");
		List<String> ukCities = new ArrayList<String>();
		ukCities.add("London");
		ukCities.add("Bristol");
		ukCities.add("Oxford");
		ukCities.add("Stonehenge");
		List<String> usCities = new ArrayList<String>();
		usCities.add("Boston");
		usCities.add("Chicago");
		usCities.add("Los Angeles");
		usCities.add("New York");
		Map<String, List<String>> retval = new HashMap<String, List<String>>();
		retval.put("BE", beCities);
		retval.put("NL", nlCities);
		retval.put("UK", ukCities);
		retval.put("US", usCities);
		return retval;
	}

	WWidget FormModel() {
		UserFormView view = new UserFormView();
		return view;
	}

	WWidget LineEdit() {
		WTemplate result = new WTemplate(WString.tr("lineEdit-template"));
		WLabel label = new WLabel("Age:");
		result.bindWidget("label", label);
		WLineEdit edit = new WLineEdit();
		result.bindWidget("edit", edit);
		edit.setValidator(new WIntValidator(0, 130));
		label.setBuddy(edit);
		return result;
	}

	WWidget LineEditEvent() {
		WContainerWidget container = new WContainerWidget();
		WLineEdit edit = new WLineEdit(container);
		edit.setEmptyText("Edit me");
		edit.setMargin(new WLength(10), EnumSet.of(Side.Right));
		final WText out = new WText("", container);
		edit.keyPressed().addListener(this, new Signal1.Listener<WKeyEvent>() {
			public void trigger(WKeyEvent e) {
				out.setText("Line edit: You pressed the '" + e.getText()
						+ "' key.");
			}
		});
		return container;
	}

	WWidget TextArea() {
		WContainerWidget container = new WContainerWidget();
		WTextArea ta = new WTextArea(container);
		ta.setColumns(80);
		ta.setRows(5);
		ta
				.setText("Change this text... \nand click outside the text area to get a changed event.");
		final WText out = new WText("<p></p>", container);
		ta.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("<p>Text area changed at "
						+ WDate.getCurrentDate().toString() + ".</p>");
			}
		});
		return container;
	}

	WWidget TextEdit() {
		WContainerWidget container = new WContainerWidget();
		final WTextEdit edit = new WTextEdit(container);
		edit.setHeight(new WLength(300));
		edit
				.setText("<p><span style=\"font-family: 'courier new', courier; font-size: medium;\"><strong>WTextEdit</strong></span></p><p>Hey, I'm a <strong>WTextEdit</strong> and you can make me <span style=\"text-decoration: underline;\"><em>rich</em></span> by adding your <span style=\"color: #ff0000;\"><em>style</em></span>!</p><p>Other widgets like...</p><ul style=\"padding: 0px; margin: 0px 0px 10px 25px;\"><li>WLineEdit</li><li>WTextArea</li><li>WSpinBox</li></ul><p>don't have style.</p>");
		WPushButton button = new WPushButton("Get text", container);
		button.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
		final WText out = new WText(container);
		out.setStyleClass("xhtml-output");
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("<pre>" + Utils.htmlEncode(edit.getText())
						+ "</pre>");
			}
		});
		return container;
	}

	WWidget SpinBox() {
		WContainerWidget container = new WContainerWidget();
		new WText("Enter a number between 0 and 100:", container);
		WDoubleSpinBox sb = new WDoubleSpinBox(container);
		sb.setRange(0, 100);
		sb.setValue(50);
		sb.setDecimals(2);
		sb.setSingleStep(0.1);
		sb.setMargin(new WLength(10), EnumSet.of(Side.Left, Side.Right));
		final WText out = new WText("", container);
		sb.valueChanged().addListener(this, new Signal1.Listener<Double>() {
			public void trigger(Double d) {
				out.setText(new WString("Spin box value changed to {1}.")
						.arg(d));
			}
		});
		return container;
	}

	WWidget TextSide() {
		WTemplate result = new WTemplate(WString.tr("editSide-template"));
		WLineEdit edit = new WLineEdit("Username");
		edit.setStyleClass("span2");
		edit.setId("prependedInput");
		result.bindWidget("name", edit);
		edit = new WLineEdit();
		edit.setStyleClass("span2");
		edit.setId("appendedInput");
		result.bindWidget("amount1", edit);
		edit = new WLineEdit();
		edit.setStyleClass("span2");
		edit.setId("appendedPrependedInput");
		result.bindWidget("amount2", edit);
		return result;
	}

	WWidget CheckBoxInline() {
		WContainerWidget result = new WContainerWidget();
		WCheckBox cb;
		cb = new WCheckBox("Check me!", result);
		cb.setChecked(true);
		cb = new WCheckBox("Check me too!", result);
		cb = new WCheckBox("Check me, I'm tristate!", result);
		cb.setTristate();
		cb.setCheckState(CheckState.PartiallyChecked);
		return result;
	}

	WWidget CheckBoxStack() {
		WContainerWidget result = new WContainerWidget();
		WCheckBox cb;
		cb = new WCheckBox("Check me!", result);
		cb.setInline(false);
		cb.setChecked(true);
		cb = new WCheckBox("Check me too!", result);
		cb.setInline(false);
		cb = new WCheckBox("Check me, I'm tristate!", result);
		cb.setInline(false);
		cb.setTristate();
		cb.setCheckState(CheckState.PartiallyChecked);
		return result;
	}

	WWidget RadioButtonsLoose() {
		WContainerWidget container = new WContainerWidget();
		new WRadioButton("Radio me!", container);
		new WRadioButton("Radio me too!", container);
		return container;
	}

	WWidget RadioButtonGroup() {
		WContainerWidget container = new WContainerWidget();
		WButtonGroup group = new WButtonGroup(container);
		WRadioButton button;
		button = new WRadioButton("Radio me!", container);
		group.addButton(button);
		button = new WRadioButton("No, radio me!", container);
		group.addButton(button);
		button = new WRadioButton("Nono, radio me!", container);
		group.addButton(button);
		group.setSelectedButtonIndex(0);
		return container;
	}

	WWidget RadioButtonStack() {
		WContainerWidget container = new WContainerWidget();
		WButtonGroup group = new WButtonGroup(container);
		WRadioButton button;
		button = new WRadioButton("Radio me!", container);
		button.setInline(false);
		group.addButton(button);
		button = new WRadioButton("No, radio me!", container);
		button.setInline(false);
		group.addButton(button);
		button = new WRadioButton("Nono, radio me!", container);
		button.setInline(false);
		group.addButton(button);
		group.setSelectedButtonIndex(0);
		return container;
	}

	WWidget RadioButtonsActivated() {
		WContainerWidget container = new WContainerWidget();
		final WButtonGroup group = new WButtonGroup(container);
		WRadioButton rb;
		rb = new WRadioButton("sleeping", container);
		rb.setInline(false);
		group.addButton(rb, 1);
		rb = new WRadioButton("eating", container);
		rb.setInline(false);
		group.addButton(rb, 2);
		rb = new WRadioButton("driving", container);
		rb.setInline(false);
		group.addButton(rb, 3);
		rb = new WRadioButton("learning Wt", container);
		rb.setInline(false);
		group.addButton(rb, 4);
		group.setSelectedButtonIndex(0);
		final WText out = new WText(container);
		group.checkedChanged().addListener(this,
				new Signal1.Listener<WRadioButton>() {
					public void trigger(WRadioButton selection) {
						WString text = new WString();
						switch (group.getId(selection)) {
						case 1:
							text = new WString("You checked button {1}.")
									.arg(group.getCheckedId());
							break;
						case 2:
							text = new WString("You selected button {1}.")
									.arg(group.getCheckedId());
							break;
						case 3:
							text = new WString("You clicked button {1}.")
									.arg(group.getCheckedId());
							break;
						}
						text.append(new WString("... Are your really {1} now?")
								.arg(selection.getText()));
						if (group.getId(selection) == 4) {
							text = new WString("That's what I expected!");
						}
						out.setText(new WString("<p>").append(text).append(
								"</p>"));
					}
				});
		return container;
	}

	WWidget ComboBox() {
		WComboBox cb = new WComboBox();
		cb.addItem("Heavy");
		cb.addItem("Medium");
		cb.addItem("Light");
		return cb;
	}

	WWidget ComboBoxActivated() {
		WContainerWidget container = new WContainerWidget();
		final WComboBox cb = new WComboBox(container);
		cb.addItem("Heavy");
		cb.addItem("Medium");
		cb.addItem("Light");
		cb.setCurrentIndex(1);
		cb.setMargin(new WLength(10), EnumSet.of(Side.Right));
		final WText out = new WText(container);
		cb.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText(new WString("You selected {1}.").arg(cb
						.getCurrentText()));
			}
		});
		return container;
	}

	WWidget ComboBoxModel() {
		WContainerWidget container = new WContainerWidget();
		final WComboBox cb = new WComboBox(container);
		cb.setMargin(new WLength(10), EnumSet.of(Side.Right));
		final WAbstractItemModel model = cb.getModel();
		model.insertRows(0, 4);
		model.setData(0, 0, "Belgium", ItemDataRole.DisplayRole);
		model.setData(0, 0, "BE", ItemDataRole.UserRole);
		model.setData(1, 0, "Netherlands", ItemDataRole.DisplayRole);
		model.setData(1, 0, "NL", ItemDataRole.UserRole);
		model.setData(2, 0, "United Kingdom", ItemDataRole.DisplayRole);
		model.setData(2, 0, "UK", ItemDataRole.UserRole);
		model.setData(3, 0, "United States", ItemDataRole.DisplayRole);
		model.setData(3, 0, "US", ItemDataRole.UserRole);
		final WText out = new WText(container);
		cb.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WString countryName = cb.getCurrentText();
				int row = cb.getCurrentIndex();
				String countryCode = (String) model.getData(model.getIndex(row,
						0), ItemDataRole.UserRole);
				out.setText(new WString("You selected {1} with key {2}.").arg(
						countryName).arg(countryCode));
			}
		});
		return container;
	}

	WWidget SelectionBoxSimple() {
		WContainerWidget container = new WContainerWidget();
		final WSelectionBox sb1 = new WSelectionBox(container);
		sb1.addItem("Heavy");
		sb1.addItem("Medium");
		sb1.addItem("Light");
		sb1.setCurrentIndex(1);
		sb1.setMargin(new WLength(10), EnumSet.of(Side.Right));
		final WText out = new WText("", container);
		sb1.activated().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText(new WString("You selected {1}.").arg(sb1
						.getCurrentText()));
			}
		});
		return container;
	}

	WWidget SelectionBoxExtended() {
		WContainerWidget container = new WContainerWidget();
		final WSelectionBox sb2 = new WSelectionBox(container);
		sb2.addItem("Bacon");
		sb2.addItem("Cheese");
		sb2.addItem("Mushrooms");
		sb2.addItem("Green peppers");
		sb2.addItem("Ham");
		sb2.addItem("Pepperoni");
		sb2.addItem("Red peppers");
		sb2.addItem("Turkey");
		sb2.setSelectionMode(SelectionMode.ExtendedSelection);
		Set<Integer> selection = new HashSet<Integer>();
		selection.add(1);
		selection.add(4);
		sb2.setSelectedIndexes(selection);
		sb2.setMargin(new WLength(10), EnumSet.of(Side.Right));
		final WText out = new WText(container);
		sb2.activated().addListener(this, new Signal.Listener() {
			public void trigger() {
				WString selected = new WString();
				Set<Integer> selection = sb2.getSelectedIndexes();
				for (Iterator<Integer> it_it = selection.iterator(); it_it
						.hasNext();) {
					int it = it_it.next();
					if (!(selected.length() == 0)) {
						selected.append(", ");
					}
					selected.append(sb2.getItemText(it));
				}
				out.setText(new WString("You choose {1}.").arg(selected));
			}
		});
		return container;
	}

	WWidget AutoComplete() {
		WContainerWidget container = new WContainerWidget();
		WSuggestionPopup.Options contactOptions = new WSuggestionPopup.Options();
		contactOptions.highlightBeginTag = "<span class=\"highlight\">";
		contactOptions.highlightEndTag = "</span>";
		contactOptions.listSeparator = ',';
		contactOptions.whitespace = " \\n";
		contactOptions.wordSeparators = "-., \"@\\n;";
		contactOptions.appendReplacedText = ", ";
		WSuggestionPopup sp = new WSuggestionPopup(WSuggestionPopup
				.generateMatcherJS(contactOptions), WSuggestionPopup
				.generateReplacerJS(contactOptions), container);
		WLineEdit le = new WLineEdit(container);
		le.setEmptyText("Enter a name starting with 'J'");
		sp.forEdit(le);
		sp.addSuggestion("John Tech <techie@mycompany.com>");
		sp.addSuggestion("Johnny Cash <cash@mycompany.com>");
		sp.addSuggestion("John Rambo <rambo@mycompany.com>");
		sp.addSuggestion("Johanna Tree <johanna@mycompany.com>");
		return container;
	}

	WWidget CalendarSimple() {
		WContainerWidget container = new WContainerWidget();
		final WCalendar c1 = new WCalendar(container);
		final WText out = new WText(container);
		c1.selectionChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				int days;
				WDate d = null;
				Set<WDate> selection = c1.getSelection();
				if (selection.size() != 0) {
					d = selection.iterator().next();
				}
				WDate toDate = new WDate(d.getYear() + 1, 1, 1);
				days = d.getDaysTo(toDate);
				out.setText("<p>That's " + String.valueOf(days)
						+ " days until New Year's Day!</p>");
			}
		});
		return container;
	}

	WWidget CalendarExtended() {
		WContainerWidget container = new WContainerWidget();
		final WCalendar c2 = new WCalendar(container);
		c2.setSelectionMode(SelectionMode.ExtendedSelection);
		final WText out = new WText(container);
		c2.selectionChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				WString selected = new WString();
				Set<WDate> selection = c2.getSelection();
				for (Iterator<WDate> it_it = selection.iterator(); it_it
						.hasNext();) {
					WDate it = it_it.next();
					if (!(selected.length() == 0)) {
						selected.append(", ");
					}
					WDate d = it;
					selected.append(d.toString("dd/MM/yyyy"));
				}
				out.setText(new WString(
						"<p>You selected the following dates: {1}</p>")
						.arg(selected));
			}
		});
		return container;
	}

	WWidget DateEdit() {
		WContainerWidget container = new WContainerWidget();
		new WText("<p>When do you want to take your holiday?</p>", container);
		WLabel de1Label = new WLabel("From", container);
		final WDateEdit de1 = new WDateEdit(container);
		de1.setDate(WDate.getCurrentServerDate().addDays(1));
		de1Label.setBuddy(de1);
		WLabel de2Label = new WLabel("To (format \"dd MM yyyy\")", container);
		final WDateEdit de2 = new WDateEdit(container);
		de2.setFormat("dd MM yyyy");
		de2.getCalendar().setHorizontalHeaderFormat(
				WCalendar.HorizontalHeaderFormat.SingleLetterDayNames);
		de2Label.setBuddy(de2);
		new WText("<p></p>", container);
		WPushButton button = new WPushButton("Save", container);
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Left));
		de1.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				de2.setBottom(de1.getDate());
				out.setText("<p>Date picker 1 is changed.</p>");
			}
		});
		de2.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				de1.setTop(de2.getDate());
				out.setText("<p>Date picker 2 is changed.</p>");
			}
		});
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (de1.getText().length() == 0 || de2.getText().length() == 0) {
					out.setText("<p>You should enter two dates!</p>");
				} else {
					int days = de1.getDate().getDaysTo(de2.getDate()) + 1;
					if ((de1.getDate() == de2.getDate() || (de1.getDate() != null && de1
							.getDate().equals(de2.getDate())))) {
						out
								.setText("<p>It's fine to take holiday just for one day!</p>");
					} else {
						if (de1.getDate().before(de2.getDate())) {
							out
									.setText("<p>So, you want to take holiday for a period of "
											+ String.valueOf(days)
											+ " days?...</p>");
						} else {
							out.setText("<p>Invalid period!</p>");
						}
					}
				}
			}
		});
		return container;
	}

	WWidget DatePicker() {
		WContainerWidget container = new WContainerWidget();
		new WText("<p>When do you want to take your holiday?</p>", container);
		WLabel dp1Label = new WLabel("From", container);
		final WDatePicker dp1 = new WDatePicker(container);
		dp1.setDate(WDate.getCurrentServerDate().addDays(1));
		dp1Label.setBuddy(dp1.getLineEdit());
		WLabel dp2Label = new WLabel("To (format \"dd MM yyyy\")", container);
		final WDatePicker dp2 = new WDatePicker(container);
		dp2.setFormat("dd MM yyyy");
		dp2.getCalendar().setHorizontalHeaderFormat(
				WCalendar.HorizontalHeaderFormat.SingleLetterDayNames);
		dp2Label.setBuddy(dp2.getLineEdit());
		new WText("<p></p>", container);
		WPushButton button = new WPushButton("Save", container);
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Left));
		dp1.getLineEdit().changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				dp2.setBottom(dp1.getDate());
				out.setText("<p>Date picker 1 is changed.</p>");
			}
		});
		dp2.getLineEdit().changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				dp1.setTop(dp2.getDate());
				out.setText("<p>Date picker 2 is changed.</p>");
			}
		});
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (dp1.getLineEdit().getText().length() == 0
						|| dp2.getLineEdit().getText().length() == 0) {
					out.setText("<p>You should enter two dates!</p>");
				} else {
					int days = dp1.getDate().getDaysTo(dp2.getDate()) + 1;
					if ((dp1.getDate() == dp2.getDate() || (dp1.getDate() != null && dp1
							.getDate().equals(dp2.getDate())))) {
						out
								.setText("<p>It's fine to take holiday just for one day!</p>");
					} else {
						if (dp1.getDate().before(dp2.getDate())) {
							out
									.setText("<p>So, you want to take holiday for a period of "
											+ String.valueOf(days)
											+ " days?...</p>");
						} else {
							out.setText("<p>Invalid period!</p>");
						}
					}
				}
			}
		});
		return container;
	}

	WWidget InPlaceEditButtons() {
		WContainerWidget container = new WContainerWidget();
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Right));
		WInPlaceEdit ipe = new WInPlaceEdit("This is editable text", container);
		ipe.setEmptyText("You deleted the text!");
		ipe.valueChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("In-place edit is set to... ");
			}
		});
		return container;
	}

	WWidget InPlaceEdit() {
		WContainerWidget container = new WContainerWidget();
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Right));
		WInPlaceEdit ipe = new WInPlaceEdit("This is editable text", container);
		ipe.setEmptyText("You deleted the text!");
		ipe.setButtonsEnabled(false);
		ipe.valueChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("In-place edit is set to... ");
			}
		});
		return container;
	}

	WWidget Slider() {
		WContainerWidget container = new WContainerWidget();
		new WText("In which year are you born?", container);
		new WBreak(container);
		final WSlider slider = new WSlider(container);
		slider.resize(new WLength(500), new WLength(50));
		slider.setTickPosition(EnumSet.of(WSlider.TickPosition.TicksAbove));
		slider.setTickInterval(10);
		slider.setMinimum(1910);
		slider.setMaximum(2010);
		slider.setValue(1960);
		new WBreak(container);
		final WText out = new WText(container);
		slider.valueChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("I'm born in the year " + slider.getValueText()
						+ ".");
			}
		});
		return container;
	}

	WWidget SliderVertical() {
		WContainerWidget container = new WContainerWidget();
		new WText("How much does Wt increase your efficiency?", container);
		new WBreak(container);
		final WSlider verticalSlider = new WSlider(Orientation.Vertical,
				container);
		verticalSlider.resize(new WLength(50), new WLength(150));
		verticalSlider.setTickPosition(WSlider.TicksBothSides);
		verticalSlider.setRange(5, 50);
		new WBreak(container);
		final WText out = new WText(container);
		out.setMargin(new WLength(10), EnumSet.of(Side.Left));
		verticalSlider.valueChanged().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("Currenly, my efficiency increased "
						+ verticalSlider.getValueText() + "%!");
			}
		});
		return container;
	}

	WWidget ProgressBar() {
		WContainerWidget container = new WContainerWidget();
		container.setStyleClass("inline-buttons");
		final WProgressBar bar = new WProgressBar(container);
		bar.setRange(0, 10);
		final WPushButton startButton = new WPushButton("Start", container);
		final WPushButton stopButton = new WPushButton("Stop", container);
		final WPushButton resetButton = new WPushButton("Reset", container);
		stopButton.disable();
		resetButton.disable();
		final WTimer intervalTimer = new WTimer(container);
		intervalTimer.setInterval(1000);
		startButton.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (bar.getValue() < 10) {
					intervalTimer.start();
					startButton.setText("Resume");
				}
				startButton.disable();
				stopButton.enable();
				resetButton.disable();
			}
		});
		stopButton.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				intervalTimer.stop();
				startButton.enable();
				stopButton.disable();
				resetButton.enable();
			}
		});
		resetButton.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				bar.setValue(0.0);
				startButton.setText("Start");
				startButton.enable();
				stopButton.disable();
				resetButton.disable();
			}
		});
		intervalTimer.timeout().addListener(this, new Signal.Listener() {
			public void trigger() {
				bar.setValue(bar.getValue() + 1);
				if (bar.getValue() == 10) {
					stopButton.clicked().trigger(new WMouseEvent());
					startButton.disable();
				}
			}
		});
		return container;
	}

	WWidget FileUpload() {
		WContainerWidget container = new WContainerWidget();
		final WFileUpload fu = new WFileUpload(container);
		fu.setFileTextSize(50);
		fu.setProgressBar(new WProgressBar());
		fu.setMargin(new WLength(10), EnumSet.of(Side.Right));
		final WPushButton uploadButton = new WPushButton("Send", container);
		uploadButton.setMargin(new WLength(10), EnumSet.of(Side.Left,
				Side.Right));
		final WText out = new WText(container);
		uploadButton.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				fu.upload();
				uploadButton.disable();
			}
		});
		fu.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				fu.upload();
				uploadButton.disable();
				out.setText("File upload is changed.");
			}
		});
		fu.uploaded().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("File upload is finished.");
			}
		});
		fu.fileTooLarge().addListener(this, new Signal.Listener() {
			public void trigger() {
				out.setText("File is too large.");
			}
		});
		return container;
	}

	WWidget PushButton() {
		WTemplate result = new WTemplate();
		result.setTemplateText("<div> ${pb1} ${pb2} </div>");
		WPushButton pb = new WPushButton("Click me!");
		result.bindWidget("pb1", pb);
		pb = new WPushButton("Try to click me...");
		result.bindWidget("pb2", pb);
		pb.setEnabled(false);
		return result;
	}

	WWidget PushButtonOnce() {
		final WPushButton ok = new WPushButton("Send");
		ok.clicked().addListener(ok, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				ok.disable();
			}
		});
		ok.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				ok.setText("Thank you");
			}
		});
		return ok;
	}

	WWidget PushButtonLink() {
		WPushButton button = new WPushButton("Navigate");
		button
				.setLink(new WLink(WLink.Type.InternalPath,
						"/navigation/anchor"));
		return button;
	}

	WWidget PushButtonDropdownAppended() {
		WTemplate result = new WTemplate(WString
				.tr("appendedDropdownButton-template"));
		WPopupMenu popup = new WPopupMenu();
		popup.addItem("Choose a button type");
		popup.addSeparator();
		popup.addItem("One-time hit button").setLink(new WLink("#one-time"));
		popup.addItem("Navigation button").setLink(new WLink("#navigation"));
		popup.addItem("Button style").setLink(new WLink("#style"));
		WLineEdit input = new WLineEdit();
		result.bindWidget("input", input);
		input.setStyleClass("span2");
		WPushButton appendedDropdownButton = new WPushButton("Action");
		result.bindWidget("appendedButton", appendedDropdownButton);
		appendedDropdownButton.setMenu(popup);
		return result;
	}

	WWidget PushButtonDropdownPrepended() {
		WTemplate result = new WTemplate(WString
				.tr("prependedDropdownButton-template"));
		WPopupMenu popup = new WPopupMenu();
		popup.addItem("Choose a button type");
		popup.addSeparator();
		popup.addSeparator();
		popup.addItem("One-time hit button").setLink(new WLink("#one-time"));
		popup.addItem("Navigation button").setLink(new WLink("#navigation"));
		popup.addItem("Button style").setLink(new WLink("#style"));
		WLineEdit input = new WLineEdit();
		result.bindWidget("input", input);
		input.setStyleClass("span2");
		WPushButton prependedDropdownButton = new WPushButton("Action");
		result.bindWidget("prependedButton", prependedDropdownButton);
		prependedDropdownButton.setMenu(popup);
		return result;
	}

	WWidget PushButtonColor() {
		WTemplate result = new WTemplate(WString.tr("pushButtonColor-template"));
		WPushButton button = new WPushButton("Default");
		result.bindWidget("button-default", button);
		button = new WPushButton("Primary");
		button.setStyleClass("btn-primary");
		result.bindWidget("button-primary", button);
		button = new WPushButton("Info");
		button.setStyleClass("btn-info");
		result.bindWidget("button-info", button);
		button = new WPushButton("Success");
		button.setStyleClass("btn-success");
		result.bindWidget("button-success", button);
		button = new WPushButton("Warning");
		button.setStyleClass("btn-warning");
		result.bindWidget("button-warning", button);
		button = new WPushButton("Danger");
		button.setStyleClass("btn-danger");
		result.bindWidget("button-danger", button);
		button = new WPushButton("Inverse");
		button.setStyleClass("btn-inverse");
		result.bindWidget("button-inverse", button);
		button = new WPushButton("Link");
		button.setStyleClass("btn-link");
		result.bindWidget("button-link", button);
		return result;
	}

	WWidget PushButtonSize() {
		WTemplate result = new WTemplate(WString.tr("pushButtonSize-template"));
		WPushButton button = new WPushButton("Large");
		button.setStyleClass("btn-large");
		result.bindWidget("button-large", button);
		button = new WPushButton("Default");
		result.bindWidget("button-default", button);
		button = new WPushButton("Small");
		button.setStyleClass("btn-small");
		result.bindWidget("button-small", button);
		button = new WPushButton("Mini");
		button.setStyleClass("btn-mini");
		result.bindWidget("button-mini", button);
		return result;
	}

	WWidget PushButtonPrimary() {
		WContainerWidget container = new WContainerWidget();
		WPushButton button = new WPushButton("Save", container);
		button.setStyleClass("btn-primary");
		button = new WPushButton("Cancel", container);
		button.setMargin(new WLength(5), EnumSet.of(Side.Left));
		return container;
	}

	WWidget PushButtonAction() {
		WTemplate result = new WTemplate(WString
				.tr("pushButtonAction-template"));
		WPushButton button = new WPushButton("Save");
		result.bindWidget("button-save", button);
		button.setStyleClass("btn-primary");
		button = new WPushButton("Cancel");
		result.bindWidget("button-cancel", button);
		return result;
	}

	WWidget Validation() {
		WTemplate t = new WTemplate(WString.tr("validation-template"));
		t.addFunction("id", WTemplate.Functions.id);
		final WLineEdit ageEdit = new WLineEdit();
		t.bindWidget("age", ageEdit);
		WIntValidator validator = new WIntValidator(0, 150);
		validator.setMandatory(true);
		ageEdit.setValidator(validator);
		WPushButton button = new WPushButton("Save");
		t.bindWidget("button", button);
		final WText out = new WText();
		t.bindWidget("age-info", out);
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				if (ageEdit.validate() == WValidator.State.Valid) {
					out.setText("Age of " + ageEdit.getText() + " is saved!");
				} else {
					out.setText("The number must be in the range 0 to 150");
				}
			}
		});
		return t;
	}

	WWidget ValidationDate() {
		WContainerWidget container = new WContainerWidget();
		WLabel label = new WLabel("When is your birthdate?", container);
		WLineEdit dateEdit = new WLineEdit(container);
		label.setBuddy(dateEdit);
		final WDatePicker birthDP = new WDatePicker(dateEdit, container);
		final WDateValidator dv = new WDateValidator();
		dateEdit.setValidator(dv);
		dv.setBottom(new WDate(1900, 1, 1));
		dv.setTop(WDate.getCurrentDate());
		dv.setFormat("dd/MM/yyyy");
		dv.setMandatory(true);
		dv.setInvalidBlankText("A birthdate is mandatory!");
		dv
				.setInvalidNotADateText("You should enter a date in the format \"dd/MM/yyyy\"!");
		dv.setInvalidTooEarlyText(new WString(
				"That's too early... The date must be {1} or later!").arg(dv
				.getBottom().toString("dd/MM/yyyy")));
		dv.setInvalidTooLateText(new WString(
				"That's too late... The date must be {1} or earlier!").arg(dv
				.getTop().toString("dd/MM/yyyy")));
		new WBreak(container);
		WPushButton button = new WPushButton("OK", container);
		final WText out = new WText(container);
		button.clicked().addListener(this, new Signal.Listener() {
			public void trigger() {
				WValidator.Result result = dv.validate(birthDP.getLineEdit()
						.getText());
				if (result.getState() == WValidator.State.Valid) {
					WDate d = WDate.getCurrentServerDate();
					int years = d.getYear() - birthDP.getDate().getYear();
					int days = d.getDaysTo(birthDP.getDate().addYears(years));
					if (days < 0) {
						days = d.getDaysTo(birthDP.getDate()
								.addYears(years + 1));
					}
					out
							.setText("<p>In "
									+ String.valueOf(days)
									+ " days, we will be celebrating your next anniversary!</p>");
				} else {
					birthDP.getLineEdit().setFocus();
					out.setText(result.getMessage());
				}
			}
		});
		return container;
	}

	WWidget ValidationModel() {
		AgeFormView view = new AgeFormView();
		return view;
	}
}
