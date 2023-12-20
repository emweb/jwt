/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
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

class FormWidgets extends Topic {
  private static Logger logger = LoggerFactory.getLogger(FormWidgets.class);

  public FormWidgets() {
    super();
  }

  public void populateSubMenu(WMenu menu) {
    menu.addItem(
            "Introduction",
            DeferredWidget.deferCreate(
                () -> {
                  return FormWidgets.this.introduction();
                }))
        .setPathComponent("");
    menu.addItem(
        "Line/Text editor",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.textEditors();
            }));
    menu.addItem(
        "Check boxes",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.checkBox();
            }));
    menu.addItem(
        "Radio buttons",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.radioButton();
            }));
    menu.addItem(
        "Combo box",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.comboBox();
            }));
    menu.addItem(
        "Selection box",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.selectionBox();
            }));
    menu.addItem(
        "Autocomplete",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.autoComplete();
            }));
    menu.addItem(
        "Date & Time entry",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.dateEntry();
            }));
    menu.addItem(
        "In-place edit",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.inPlaceEdit();
            }));
    menu.addItem(
        "Slider",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.slider();
            }));
    menu.addItem(
        "Progress bar",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.progressBar();
            }));
    menu.addItem(
        "File upload",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.fileUpload();
            }));
    menu.addItem(
        "Push button",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.pushButton();
            }));
    menu.addItem(
        "Validation",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.validation();
            }));
    menu.addItem(
        "Integration example",
        DeferredWidget.deferCreate(
            () -> {
              return FormWidgets.this.example();
            }));
  }

  private WWidget introduction() {
    TopicTemplate result = new TopicTemplate("forms-introduction");
    result.bindWidget("SimpleForm", SimpleForm());
    result.bindWidget("FormModel", FormModel());
    result.bindString(
        "simpleForm-template", reindent(WString.tr("simpleForm-template")), TextFormat.Plain);
    result.bindString("form-field", reindent(WString.tr("form-field")), TextFormat.Plain);
    result.bindString(
        "userForm-template", reindent(WString.tr("userForm-template")), TextFormat.Plain);
    return result;
  }

  private WWidget textEditors() {
    TopicTemplate result = new TopicTemplate("forms-textEditors");
    result.bindWidget("LineEdit", LineEdit());
    result.bindWidget("LineEditEvent", LineEditEvent());
    result.bindWidget("TextArea", TextArea());
    result.bindWidget("TextEdit", TextEdit());
    result.bindWidget("SpinBox", SpinBox());
    result.bindWidget("TextSide", TextSide());
    result.bindWidget("InputMask", InputMask());
    result.bindWidget("EmailEdit", EmailEdit());
    result.bindString(
        "emailEdit-template", reindent(WString.tr("emailEdit-template")), TextFormat.Plain);
    result.bindString(
        "lineEdit-template", reindent(WString.tr("lineEdit-template")), TextFormat.Plain);
    result.bindString(
        "editSide-template", reindent(WString.tr("editSide-template")), TextFormat.Plain);
    return result;
  }

  private WWidget checkBox() {
    TopicTemplate result = new TopicTemplate("forms-checkBox");
    result.bindWidget("CheckBoxInline", CheckBoxInline());
    result.bindWidget("CheckBoxStack", CheckBoxStack());
    return result;
  }

  private WWidget radioButton() {
    TopicTemplate result = new TopicTemplate("forms-radioButton");
    result.bindWidget("RadioButtonsLoose", RadioButtonsLoose());
    result.bindWidget("RadioButtonGroup", RadioButtonGroup());
    result.bindWidget("RadioButtonStack", RadioButtonStack());
    result.bindWidget("RadioButtonsActivated", RadioButtonsActivated());
    return result;
  }

  private WWidget comboBox() {
    TopicTemplate result = new TopicTemplate("forms-comboBox");
    result.bindWidget("ComboBox", ComboBox());
    result.bindWidget("ComboBoxActivated", ComboBoxActivated());
    result.bindWidget("ComboBoxModel", ComboBoxModel());
    return result;
  }

  private WWidget selectionBox() {
    TopicTemplate result = new TopicTemplate("forms-selectionBox");
    result.bindWidget("SelectionBoxSimple", SelectionBoxSimple());
    result.bindWidget("SelectionBoxExtended", SelectionBoxExtended());
    return result;
  }

  private WWidget autoComplete() {
    TopicTemplate result = new TopicTemplate("forms-autoComplete");
    result.bindWidget("AutoComplete", AutoComplete());
    return result;
  }

  private WWidget dateEntry() {
    TopicTemplate result = new TopicTemplate("forms-dateEntry");
    result.bindWidget("CalendarSimple", CalendarSimple());
    result.bindWidget("CalendarExtended", CalendarExtended());
    result.bindWidget("DateEdit", DateEdit());
    result.bindWidget("TimeEdit", TimeEdit());
    return result;
  }

  private WWidget inPlaceEdit() {
    TopicTemplate result = new TopicTemplate("forms-inPlaceEdit");
    result.bindWidget("InPlaceEditButtons", InPlaceEditButtons());
    result.bindWidget("InPlaceEdit", InPlaceEdit());
    return result;
  }

  private WWidget slider() {
    TopicTemplate result = new TopicTemplate("forms-slider");
    result.bindWidget("Slider", Slider());
    result.bindWidget("SliderVertical", SliderVertical());
    result.bindWidget("SliderSteps", SliderSteps());
    return result;
  }

  private WWidget progressBar() {
    TopicTemplate result = new TopicTemplate("forms-progressBar");
    result.bindWidget("ProgressBar", ProgressBar());
    return result;
  }

  private WWidget fileUpload() {
    TopicTemplate result = new TopicTemplate("forms-fileUpload");
    result.bindWidget("FileUpload", FileUpload());
    result.bindWidget("FileDrop", FileDrop());
    return result;
  }

  private WWidget pushButton() {
    TopicTemplate result = new TopicTemplate("forms-pushButton");
    result.bindWidget("PushButton", PushButton());
    result.bindWidget("PushButtonOnce", PushButtonOnce());
    result.bindWidget("PushButtonLink", PushButtonLink());
    result.bindWidget("PushButtonDropdownAppended", PushButtonDropdownAppended());
    result.bindWidget("PushButtonColor", PushButtonColor());
    result.bindWidget("PushButtonSize", PushButtonSize());
    result.bindWidget("PushButtonPrimary", PushButtonPrimary());
    result.bindWidget("PushButtonAction", PushButtonAction());
    result.bindString(
        "appendedDropdownButton-template",
        reindent(WString.tr("appendedDropdownButton-template")),
        TextFormat.Plain);
    result.bindString(
        "pushButtonColor-template",
        reindent(WString.tr("pushButtonColor-template")),
        TextFormat.Plain);
    result.bindString(
        "pushButtonSize-template",
        reindent(WString.tr("pushButtonSize-template")),
        TextFormat.Plain);
    result.bindString(
        "pushButtonAction-template",
        reindent(WString.tr("pushButtonAction-template")),
        TextFormat.Plain);
    return result;
  }

  private WWidget validation() {
    TopicTemplate result = new TopicTemplate("forms-validation");
    result.bindWidget("Validation", Validation());
    result.bindWidget("ValidationDate", ValidationDate());
    result.bindWidget("ValidationModel", ValidationModel());
    result.bindString(
        "validation-template", reindent(WString.tr("validation-template")), TextFormat.Plain);
    return result;
  }

  private WWidget example() {
    TopicTemplate result = new TopicTemplate("forms-integration-example");
    result.bindWidget("FormModel", FormModel());
    result.bindString("form-field", reindent(WString.tr("form-field")), TextFormat.Plain);
    result.bindString(
        "userForm-template", reindent(WString.tr("userForm-template")), TextFormat.Plain);
    return result;
  }

  WWidget SimpleForm() {
    WTemplate result = new WTemplate(WString.tr("simpleForm-template"));
    final WLineEdit name = new WLineEdit();
    result.bindWidget("name", name);
    name.setPlaceholderText("first name");
    WPushButton button = new WPushButton("OK");
    result.bindWidget("button", button);
    final WText out = new WText();
    result.bindWidget("out", out);
    button
        .clicked()
        .addListener(
            this,
            () -> {
              out.setText(
                  "Hello, "
                      + name.getText()
                      + "! I just want to help you... You"
                      + " could complete this simple form by adding validation.");
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
    result.addFunction("id", WTemplate.Functions.id);
    WLineEdit edit = new WLineEdit();
    edit.setValidator(new WIntValidator(0, 130));
    result.bindString("label", "Age:");
    result.bindWidget("edit", edit);
    return result;
  }

  WWidget InputMask() {
    WTemplate result = new WTemplate(WString.tr("lineEdit-template"));
    result.addFunction("id", WTemplate.Functions.id);
    WLineEdit edit = new WLineEdit();
    edit.setTextSize(15);
    edit.setInputMask("009.009.009.009;_");
    result.bindString("label", "IP Address:");
    result.bindWidget("edit", edit);
    return result;
  }

  WWidget LineEditEvent() {
    WContainerWidget container = new WContainerWidget();
    WLineEdit edit = new WLineEdit((WContainerWidget) container);
    edit.setPlaceholderText("Edit me");
    final WText out = new WText("", (WContainerWidget) container);
    out.addStyleClass("help-block");
    edit.keyPressed()
        .addListener(
            this,
            (WKeyEvent e) -> {
              out.setText("You pressed the '" + e.getText() + "' key.");
            });
    return container;
  }

  WWidget TextArea() {
    WContainerWidget container = new WContainerWidget();
    WTextArea ta = new WTextArea((WContainerWidget) container);
    ta.setColumns(80);
    ta.setRows(5);
    ta.setText("Change this text... \nand click outside the text area to get a changed event.");
    final WText out = new WText("<p></p>", (WContainerWidget) container);
    out.addStyleClass("help-block");
    ta.changed()
        .addListener(
            this,
            () -> {
              out.setText(
                  "<p>Text area changed at " + WDate.getCurrentServerDate().toString() + ".</p>");
            });
    return container;
  }

  WWidget TextEdit() {
    WContainerWidget container = new WContainerWidget();
    final WTextEdit edit = new WTextEdit((WContainerWidget) container);
    edit.setHeight(new WLength(300));
    edit.setText(
        "<p><span style=\"font-family: 'courier new', courier; font-size: medium;\"><strong>WTextEdit</strong></span></p><p>Hey, I'm a <strong>WTextEdit</strong> and you can make me <span style=\"text-decoration: underline;\"><em>rich</em></span> by adding your <span style=\"color: #ff0000;\"><em>style</em></span>!</p><p>Other widgets like...</p><ul style=\"padding: 0px; margin: 0px 0px 10px 25px;\"><li>WLineEdit</li><li>WTextArea</li><li>WSpinBox</li></ul><p>don't have style.</p>");
    WPushButton button = new WPushButton("Get text", (WContainerWidget) container);
    button.setMargin(new WLength(10), EnumSet.of(Side.Top, Side.Bottom));
    final WText out = new WText((WContainerWidget) container);
    out.setStyleClass("xhtml-output");
    button
        .clicked()
        .addListener(
            this,
            () -> {
              out.setText("<pre>" + Utils.htmlEncode(edit.getText()) + "</pre>");
            });
    return container;
  }

  WWidget SpinBox() {
    WContainerWidget container = new WContainerWidget();
    container.addStyleClass("form-group");
    WLabel label = new WLabel("Enter a number (0 - 100):", (WContainerWidget) container);
    final WDoubleSpinBox sb = new WDoubleSpinBox((WContainerWidget) container);
    sb.setRange(0, 100);
    sb.setValue(50);
    sb.setDecimals(2);
    sb.setSingleStep(0.1);
    label.setBuddy(sb);
    final WText out = new WText("", (WContainerWidget) container);
    out.addStyleClass("help-block");
    sb.changed()
        .addListener(
            this,
            () -> {
              if (sb.validate() == ValidationState.Valid) {
                out.setText(new WString("Spin box value changed to {1}").arg(sb.getText()));
              } else {
                out.setText(new WString("Invalid spin box value!"));
              }
            });
    return container;
  }

  WWidget TextSide() {
    WTemplate result = new WTemplate(WString.tr("editSide-template"));
    WLineEdit edit = new WLineEdit("Username");
    edit.setStyleClass("span2");
    result.bindWidget("name", edit);
    edit = new WLineEdit();
    edit.setStyleClass("span2");
    result.bindWidget("amount1", edit);
    edit = new WLineEdit();
    edit.setStyleClass("span2");
    result.bindWidget("amount2", edit);
    return result;
  }

  WWidget EmailEdit() {
    WTemplate result = new WTemplate(WString.tr("emailEdit-template"));
    final WTemplate tpl = result;
    tpl.addFunction("id", WTemplate.Functions.id);
    tpl.bindString("label", "Email address: ", TextFormat.Plain);
    final WEmailEdit edit = (WEmailEdit) tpl.bindWidget("edit", new WEmailEdit());
    WPushButton submit = (WPushButton) tpl.bindWidget("submit", new WPushButton("Submit"));
    final WCheckBox multiple =
        (WCheckBox) tpl.bindWidget("multiple", new WCheckBox("Allow multiple"));
    multiple
        .changed()
        .addListener(
            this,
            () -> {
              edit.setMultiple(multiple.isChecked());
            });
    final WCheckBox pattern =
        (WCheckBox) tpl.bindWidget("pattern", new WCheckBox("Use pattern '.*@example[.]com'"));
    pattern
        .changed()
        .addListener(
            this,
            () -> {
              edit.setPattern(pattern.isChecked() ? ".*@example[.]com" : "");
            });
    tpl.bindEmpty("edit-info");
    edit.validated()
        .addListener(
            this,
            (WValidator.Result validationResult) -> {
              tpl.bindString("edit-info", validationResult.getMessage());
            });
    submit
        .clicked()
        .addListener(
            this,
            () -> {
              edit.setMultiple(multiple.isChecked());
              edit.setPattern(pattern.isChecked() ? ".*@example[.]com" : "");
              edit.validate();
              tpl.bindString(
                  "output",
                  new WString("Entered email address: {1}").arg(edit.getValueText()),
                  TextFormat.Plain);
            });
    tpl.bindEmpty("output");
    return result;
  }

  WWidget CheckBoxInline() {
    WContainerWidget result = new WContainerWidget();
    WCheckBox cb;
    cb = new WCheckBox("Check me!", (WContainerWidget) result);
    cb.setChecked(true);
    cb = new WCheckBox("Check me too!", (WContainerWidget) result);
    cb = new WCheckBox("Check me, I'm tristate!", (WContainerWidget) result);
    cb.setTristate();
    cb.setCheckState(CheckState.PartiallyChecked);
    return result;
  }

  WWidget CheckBoxStack() {
    WContainerWidget result = new WContainerWidget();
    WCheckBox cb;
    cb = new WCheckBox("Check me!", (WContainerWidget) result);
    cb.setInline(false);
    cb.setChecked(true);
    cb = new WCheckBox("Check me too!", (WContainerWidget) result);
    cb.setInline(false);
    cb = new WCheckBox("Check me, I'm tristate!", (WContainerWidget) result);
    cb.setInline(false);
    cb.setTristate();
    cb.setCheckState(CheckState.PartiallyChecked);
    return result;
  }

  WWidget RadioButtonsLoose() {
    WContainerWidget container = new WContainerWidget();
    new WRadioButton("Radio me!", (WContainerWidget) container);
    new WRadioButton("Radio me too!", (WContainerWidget) container);
    return container;
  }

  WWidget RadioButtonGroup() {
    WContainerWidget container = new WContainerWidget();
    WButtonGroup group = new WButtonGroup();
    WRadioButton button;
    button = new WRadioButton("Radio me!", (WContainerWidget) container);
    group.addButton(button);
    button = new WRadioButton("No, radio me!", (WContainerWidget) container);
    group.addButton(button);
    button = new WRadioButton("Nono, radio me!", (WContainerWidget) container);
    group.addButton(button);
    group.setSelectedButtonIndex(0);
    return container;
  }

  WWidget RadioButtonStack() {
    WContainerWidget container = new WContainerWidget();
    WButtonGroup group = new WButtonGroup();
    WRadioButton button;
    button = new WRadioButton("Radio me!", (WContainerWidget) container);
    button.setInline(false);
    group.addButton(button);
    button = new WRadioButton("No, radio me!", (WContainerWidget) container);
    button.setInline(false);
    group.addButton(button);
    button = new WRadioButton("Nono, radio me!", (WContainerWidget) container);
    button.setInline(false);
    group.addButton(button);
    group.setSelectedButtonIndex(0);
    return container;
  }

  WWidget RadioButtonsActivated() {
    WContainerWidget container = new WContainerWidget();
    WButtonGroup group = new WButtonGroup();
    WRadioButton rb;
    rb = new WRadioButton("sleeping", (WContainerWidget) container);
    rb.setInline(false);
    group.addButton(rb, 1);
    rb = new WRadioButton("eating", (WContainerWidget) container);
    rb.setInline(false);
    group.addButton(rb, 2);
    rb = new WRadioButton("driving", (WContainerWidget) container);
    rb.setInline(false);
    group.addButton(rb, 3);
    rb = new WRadioButton("learning Wt", (WContainerWidget) container);
    rb.setInline(false);
    group.addButton(rb, 4);
    group.setSelectedButtonIndex(0);
    final WText out = new WText((WContainerWidget) container);
    final WButtonGroup rawGroup = group;
    group
        .checkedChanged()
        .addListener(
            this,
            (WRadioButton selection) -> {
              WString text = new WString();
              switch (rawGroup.getId(selection)) {
                case 1:
                  text = new WString("You checked button {1}.").arg(rawGroup.getCheckedId());
                  break;
                case 2:
                  text = new WString("You selected button {1}.").arg(rawGroup.getCheckedId());
                  break;
                case 3:
                  text = new WString("You clicked button {1}.").arg(rawGroup.getCheckedId());
                  break;
              }
              text.append(new WString("... Are your really {1} now?").arg(selection.getText()));
              if (rawGroup.getId(selection) == 4) {
                text = new WString("That's what I expected!");
              }
              out.setText(new WString("<p>").append(text).append("</p>"));
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
    final WComboBox cb = new WComboBox((WContainerWidget) container);
    cb.addItem("Heavy");
    cb.addItem("Medium");
    cb.addItem("Light");
    cb.setCurrentIndex(1);
    cb.setMargin(new WLength(10), EnumSet.of(Side.Right));
    final WText out = new WText((WContainerWidget) container);
    out.addStyleClass("help-block");
    cb.changed()
        .addListener(
            this,
            () -> {
              out.setText(new WString("You selected {1}.").arg(cb.getCurrentText()));
            });
    return container;
  }

  WWidget ComboBoxModel() {
    WContainerWidget container = new WContainerWidget();
    final WComboBox cb = new WComboBox((WContainerWidget) container);
    cb.setMargin(new WLength(10), EnumSet.of(Side.Right));
    final WStringListModel model = new WStringListModel();
    model.addString("Belgium");
    model.setData(0, 0, "BE", ItemDataRole.User);
    model.addString("Netherlands");
    model.setData(1, 0, "NL", ItemDataRole.User);
    model.addString("United Kingdom");
    model.setData(2, 0, "UK", ItemDataRole.User);
    model.addString("United States");
    model.setData(3, 0, "US", ItemDataRole.User);
    model.setFlags(3, EnumSet.of(ItemFlag.Selectable));
    cb.setNoSelectionEnabled(true);
    cb.setModel(model);
    final WText out = new WText((WContainerWidget) container);
    out.addStyleClass("help-block");
    cb.changed()
        .addListener(
            this,
            () -> {
              WString countryName = cb.getCurrentText();
              int row = cb.getCurrentIndex();
              WString countryCode =
                  StringUtils.asString(model.getData(model.getIndex(row, 0), ItemDataRole.User));
              out.setText(
                  new WString("You selected {1} with key {2}.").arg(countryName).arg(countryCode));
            });
    return container;
  }

  WWidget SelectionBoxSimple() {
    WContainerWidget container = new WContainerWidget();
    final WSelectionBox sb1 = new WSelectionBox((WContainerWidget) container);
    sb1.addItem("Heavy");
    sb1.addItem("Medium");
    sb1.addItem("Light");
    sb1.setCurrentIndex(1);
    sb1.setMargin(new WLength(10), EnumSet.of(Side.Right));
    final WText out = new WText("", (WContainerWidget) container);
    sb1.activated()
        .addListener(
            this,
            () -> {
              out.setText(new WString("You selected {1}.").arg(sb1.getCurrentText()));
            });
    return container;
  }

  WWidget SelectionBoxExtended() {
    WContainerWidget container = new WContainerWidget();
    final WSelectionBox sb2 = new WSelectionBox((WContainerWidget) container);
    sb2.addItem("Bacon");
    sb2.addItem("Cheese");
    sb2.addItem("Mushrooms");
    sb2.addItem("Green peppers");
    sb2.addItem("Ham");
    sb2.addItem("Pepperoni");
    sb2.addItem("Red peppers");
    sb2.addItem("Turkey");
    sb2.setSelectionMode(SelectionMode.Extended);
    Set<Integer> selection = new HashSet<Integer>();
    selection.add(1);
    selection.add(4);
    sb2.setSelectedIndexes(selection);
    sb2.setMargin(new WLength(10), EnumSet.of(Side.Right));
    final WText out = new WText((WContainerWidget) container);
    out.addStyleClass("help-block");
    sb2.activated()
        .addListener(
            this,
            () -> {
              WString selected = new WString();
              Set<Integer> newSelection = sb2.getSelectedIndexes();
              for (Iterator<Integer> it_it = newSelection.iterator(); it_it.hasNext(); ) {
                int it = it_it.next();
                if (!(selected.length() == 0)) {
                  selected.append(", ");
                }
                selected.append(sb2.getItemText(it));
              }
              out.setText(new WString("You choose {1}.").arg(selected));
            });
    return container;
  }

  WWidget AutoComplete() {
    WContainerWidget container = new WContainerWidget();
    WSuggestionPopup.Options contactOptions = new WSuggestionPopup.Options();
    contactOptions.highlightBeginTag = "<span class=\"highlight\">";
    contactOptions.highlightEndTag = "</span>";
    contactOptions.listSeparator = ',';
    contactOptions.whitespace = " \n";
    contactOptions.wordSeparators = "-., \"@\n;";
    contactOptions.appendReplacedText = ", ";
    WSuggestionPopup sp =
        new WSuggestionPopup(
            WSuggestionPopup.generateMatcherJS(contactOptions),
            WSuggestionPopup.generateReplacerJS(contactOptions));
    WLineEdit le = new WLineEdit((WContainerWidget) container);
    le.setPlaceholderText("Enter a name starting with 'J'");
    sp.forEdit(le);
    sp.addSuggestion("John Tech <techie@mycompany.com>");
    sp.addSuggestion("Johnny Cash <cash@mycompany.com>");
    sp.addSuggestion("John Rambo <rambo@mycompany.com>");
    sp.addSuggestion("Johanna Tree <johanna@mycompany.com>");
    return container;
  }

  WWidget CalendarSimple() {
    WContainerWidget container = new WContainerWidget();
    final WCalendar c1 = new WCalendar((WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    out.addStyleClass("help-block");
    c1.selectionChanged()
        .addListener(
            this,
            () -> {
              Set<WDate> selection = c1.getSelection();
              if (selection.size() != 0) {
                WDate d = null;
                d = selection.iterator().next();
                WDate toDate = new WDate(d.getYear() + 1, 1, 1);
                int days = d.getDaysTo(toDate);
                out.setText(new WString("<p>That's {1} days until New Year's Day!</p>").arg(days));
              }
            });
    return container;
  }

  WWidget CalendarExtended() {
    WContainerWidget container = new WContainerWidget();
    final WCalendar c2 = new WCalendar((WContainerWidget) container);
    c2.setSelectionMode(SelectionMode.Extended);
    final WText out = new WText((WContainerWidget) container);
    out.addStyleClass("help-block");
    c2.selectionChanged()
        .addListener(
            this,
            () -> {
              WString selected = new WString();
              Set<WDate> selection = c2.getSelection();
              for (WDate date : c2.getSelection()) {
                if (!(selected.length() == 0)) {
                  selected.append(", ");
                }
                selected.append(date.toString("dd/MM/yyyy"));
              }
              out.setText(
                  new WString("<p>You selected the following dates: {1}</p>").arg(selected));
            });
    return container;
  }

  WWidget DateEdit() {
    WTemplate form = new WTemplate(WString.tr("dateEdit-template"));
    form.addFunction("id", WTemplate.Functions.id);
    final WDateEdit de1 = new WDateEdit();
    form.bindWidget("from", de1);
    de1.setDate(WDate.getCurrentServerDate().addDays(1));
    final WDateEdit de2 = new WDateEdit();
    form.bindWidget("to", de2);
    de2.setFormat("dd MM yyyy");
    de2.getCalendar().setHorizontalHeaderFormat(CalendarHeaderFormat.SingleLetterDayNames);
    de2.setBottom(de1.getDate());
    WPushButton button = new WPushButton("Save");
    form.bindWidget("save", button);
    final WText out = new WText();
    form.bindWidget("out", out);
    de1.changed()
        .addListener(
            this,
            () -> {
              if (de1.validate() == ValidationState.Valid) {
                de2.setBottom(de1.getDate());
                out.setText("Date picker 1 is changed.");
              }
            });
    de2.changed()
        .addListener(
            this,
            () -> {
              if (de1.validate() == ValidationState.Valid) {
                de1.setTop(de2.getDate());
                out.setText("Date picker 2 is changed.");
              }
            });
    button
        .clicked()
        .addListener(
            this,
            () -> {
              if (de1.getText().length() == 0 || de2.getText().length() == 0) {
                out.setText("You should enter two dates!");
              } else {
                int days = de1.getDate().getDaysTo(de2.getDate()) + 1;
                if (days == 1) {
                  out.setText("It's fine to take holiday just for one day!");
                } else {
                  if (days > 1) {
                    out.setText(
                        new WString("So, you want to take holiday for a period of {1} days?")
                            .arg(days));
                  } else {
                    out.setText("Invalid period!");
                  }
                }
              }
            });
    return form;
  }

  WWidget TimeEdit() {
    WTemplate form = new WTemplate(WString.tr("timeEdit-template"));
    form.addFunction("id", WTemplate.Functions.id);
    final WTimeEdit te1 = new WTimeEdit();
    form.bindWidget("from", te1);
    form.bindString("from-format", te1.getFormat());
    te1.setTime(WTime.getCurrentTime());
    final WTimeEdit te2 = new WTimeEdit();
    form.bindWidget("to", te2);
    te2.setFormat("h:mm:ss.SSS a");
    te2.setTime(WTime.getCurrentTime().addSecs(60 * 15));
    form.bindString("to-format", te2.getFormat());
    WPushButton button = new WPushButton("Save");
    form.bindWidget("save", button);
    final WText out = new WText();
    form.bindWidget("out", out);
    te1.changed()
        .addListener(
            this,
            () -> {
              if (te1.validate() == ValidationState.Valid) {
                out.setText("Time picker 1 is changed.");
              }
            });
    te2.changed()
        .addListener(
            this,
            () -> {
              if (te2.validate() == ValidationState.Valid) {
                out.setText("Time picker 2 is changed.");
              }
            });
    button
        .clicked()
        .addListener(
            this,
            () -> {
              if (te1.getText().length() == 0 || te2.getText().length() == 0) {
                out.setText("You should enter two times!");
              } else {
                long secs = te1.getTime().secsTo(te2.getTime()) + 1;
                if (secs <= 60 * 10) {
                  out.setText("This is a really small range of time");
                } else {
                  out.setText(
                      new WString("So, you want your package to be delivered between {1} and {2}?")
                          .arg(te1.getTime().toString())
                          .arg(te2.getTime().toString()));
                }
              }
            });
    return form;
  }

  WWidget InPlaceEditButtons() {
    WContainerWidget container = new WContainerWidget();
    WInPlaceEdit ipe = new WInPlaceEdit("This is editable text", (WContainerWidget) container);
    ipe.setPlaceholderText("Enter something");
    return container;
  }

  WWidget InPlaceEdit() {
    WContainerWidget container = new WContainerWidget();
    WInPlaceEdit ipe = new WInPlaceEdit("This is editable text", (WContainerWidget) container);
    ipe.setPlaceholderText("Enter something");
    ipe.setButtonsEnabled(false);
    return container;
  }

  WWidget Slider() {
    WContainerWidget container = new WContainerWidget();
    new WText("In which year were you born?", (WContainerWidget) container);
    new WBreak((WContainerWidget) container);
    final WSlider slider = new WSlider((WContainerWidget) container);
    slider.resize(new WLength(500), new WLength(50));
    slider.setTickPosition(EnumSet.of(WSlider.TickPosition.TicksAbove));
    slider.setTickInterval(10);
    slider.setMinimum(1910);
    slider.setMaximum(2010);
    slider.setValue(1960);
    new WBreak((WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    slider
        .valueChanged()
        .addListener(
            this,
            () -> {
              out.setText("I was born in the year " + slider.getValueText() + ".");
            });
    return container;
  }

  WWidget SliderVertical() {
    WContainerWidget container = new WContainerWidget();
    new WText("How much does Wt increase your efficiency?", (WContainerWidget) container);
    new WBreak((WContainerWidget) container);
    final WSlider verticalSlider = new WSlider(Orientation.Vertical, (WContainerWidget) container);
    verticalSlider.resize(new WLength(50), new WLength(150));
    verticalSlider.setTickPosition(WSlider.TicksBothSides);
    verticalSlider.setRange(5, 50);
    new WBreak((WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    out.setMargin(new WLength(10), EnumSet.of(Side.Left));
    verticalSlider
        .valueChanged()
        .addListener(
            this,
            () -> {
              out.setText(
                  "Currenly, my efficiency increased " + verticalSlider.getValueText() + "%!");
            });
    return container;
  }

  WWidget SliderSteps() {
    WContainerWidget container = new WContainerWidget();
    new WText("Try to select '7'. I bet you can't.", (WContainerWidget) container);
    new WBreak((WContainerWidget) container);
    final WSlider slider = new WSlider((WContainerWidget) container);
    slider.resize(new WLength(300), new WLength(50));
    slider.setTickPosition(WSlider.TicksBothSides);
    slider.setRange(0, 10);
    slider.setStep(3);
    new WBreak((WContainerWidget) container);
    final WText out = new WText((WContainerWidget) container);
    out.setMargin(new WLength(10), EnumSet.of(Side.Left));
    slider
        .valueChanged()
        .addListener(
            this,
            () -> {
              out.setText("That's a failure, you selected: '" + slider.getValueText() + "'!");
            });
    return container;
  }

  WWidget ProgressBar() {
    WContainerWidget container = new WContainerWidget();
    container.setStyleClass("inline-buttons");
    final WProgressBar bar = new WProgressBar((WContainerWidget) container);
    bar.setRange(0, 10);
    final WPushButton startButton = new WPushButton("Start", (WContainerWidget) container);
    final WPushButton stopButton = new WPushButton("Stop", (WContainerWidget) container);
    final WPushButton resetButton = new WPushButton("Reset", (WContainerWidget) container);
    stopButton.disable();
    resetButton.disable();
    final WTimer intervalTimer = new WTimer();
    intervalTimer.setInterval(Duration.ofMillis(1000));
    startButton
        .clicked()
        .addListener(
            this,
            () -> {
              if (bar.getValue() < 10) {
                intervalTimer.start();
                startButton.setText("Resume");
              }
              startButton.disable();
              stopButton.enable();
              resetButton.disable();
            });
    stopButton
        .clicked()
        .addListener(
            this,
            () -> {
              intervalTimer.stop();
              startButton.enable();
              stopButton.disable();
              resetButton.enable();
            });
    resetButton
        .clicked()
        .addListener(
            this,
            () -> {
              bar.setValue(0.0);
              startButton.setText("Start");
              startButton.enable();
              stopButton.disable();
              resetButton.disable();
            });
    intervalTimer
        .timeout()
        .addListener(
            this,
            () -> {
              bar.setValue(bar.getValue() + 1);
              if (bar.getValue() == 10) {
                stopButton.clicked().trigger(new WMouseEvent());
                startButton.disable();
              }
            });
    return container;
  }

  WWidget FileUpload() {
    WContainerWidget container = new WContainerWidget();
    final WFileUpload fu = new WFileUpload((WContainerWidget) container);
    fu.setProgressBar(new WProgressBar());
    fu.setMargin(new WLength(10), EnumSet.of(Side.Right));
    final WPushButton uploadButton = new WPushButton("Send", (WContainerWidget) container);
    uploadButton.setMargin(new WLength(10), EnumSet.of(Side.Left, Side.Right));
    final WText out = new WText((WContainerWidget) container);
    uploadButton
        .clicked()
        .addListener(
            this,
            () -> {
              fu.upload();
              uploadButton.disable();
            });
    fu.changed()
        .addListener(
            this,
            () -> {
              fu.upload();
              uploadButton.disable();
              out.setText("File upload is changed.");
            });
    fu.uploaded()
        .addListener(
            this,
            () -> {
              out.setText("File upload is finished.");
            });
    fu.fileTooLarge()
        .addListener(
            this,
            () -> {
              out.setText("File is too large.");
            });
    return container;
  }

  WWidget FileDrop() {
    WFileDropWidget dropWidgetPtr = new WFileDropWidget();
    final WFileDropWidget dropWidget = dropWidgetPtr;
    dropWidget
        .drop()
        .addListener(
            this,
            (List<WFileDropWidget.File> files) -> {
              final int maxFiles = 5;
              int prevNbFiles = dropWidget.getUploads().size() - files.size();
              for (int i = 0; i < files.size(); i++) {
                if (prevNbFiles + i >= maxFiles) {
                  dropWidget.cancelUpload(files.get(i));
                  continue;
                }
                WContainerWidget block = new WContainerWidget((WContainerWidget) dropWidget);
                block.setToolTip(files.get(i).getClientFileName());
                block.addStyleClass("upload-block spinner");
              }
              if (dropWidget.getUploads().size() >= maxFiles) {
                dropWidget.setAcceptDrops(false);
              }
            });
    dropWidget
        .uploaded()
        .addListener(
            this,
            (WFileDropWidget.File file) -> {
              List<WFileDropWidget.File> uploads = dropWidget.getUploads();
              int idx = 0;
              for (; idx != uploads.size(); ++idx) {
                if (uploads.get(idx) == file) {
                  break;
                }
              }
              dropWidget.getWidget(idx).removeStyleClass("spinner");
              dropWidget.getWidget(idx).addStyleClass("ready");
            });
    dropWidget
        .tooLarge()
        .addListener(
            this,
            (WFileDropWidget.File file, Long size) -> {
              List<WFileDropWidget.File> uploads = dropWidget.getUploads();
              int idx = 0;
              for (; idx != uploads.size(); ++idx) {
                if (uploads.get(idx) == file) {
                  break;
                }
              }
              dropWidget.getWidget(idx).removeStyleClass("spinner");
              dropWidget.getWidget(idx).addStyleClass("failed");
            });
    dropWidget
        .uploadFailed()
        .addListener(
            this,
            (WFileDropWidget.File file) -> {
              List<WFileDropWidget.File> uploads = dropWidget.getUploads();
              int idx = 0;
              for (; idx != uploads.size(); ++idx) {
                if (uploads.get(idx) == file) {
                  break;
                }
              }
              dropWidget.getWidget(idx).removeStyleClass("spinner");
              dropWidget.getWidget(idx).addStyleClass("failed");
            });
    return dropWidgetPtr;
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
    WPushButton okPtr = new WPushButton("Send");
    final WPushButton ok = okPtr;
    ok.clicked()
        .addListener(
            ok,
            (WMouseEvent e1) -> {
              ok.disable();
            });
    ok.clicked()
        .addListener(
            this,
            () -> {
              ok.setText("Thank you");
            });
    return okPtr;
  }

  WWidget PushButtonLink() {
    WPushButton button = new WPushButton("Navigate");
    button.setLink(new WLink(LinkType.InternalPath, "/navigation/anchor"));
    return button;
  }

  WWidget PushButtonDropdownAppended() {
    WTemplate result = new WTemplate(WString.tr("appendedDropdownButton-template"));
    WPopupMenu popup = new WPopupMenu();
    popup.addItem("Choose a button type");
    popup.addSeparator();
    popup.addItem("One-time hit button").setLink(new WLink("#one-time"));
    popup.addItem("Navigation button").setLink(new WLink("#navigation"));
    popup.addItem("Button style").setLink(new WLink("#style"));
    WLineEdit input = new WLineEdit();
    result.bindWidget("input", input);
    WPushButton button = new WPushButton("Action");
    result.bindWidget("appendedButton", button);
    button.setMenu(popup);
    return result;
  }

  WWidget PushButtonColor() {
    WTemplate result = new WTemplate(WString.tr("pushButtonColor-template"));
    WPushButton button = new WPushButton("Primary");
    button.setStyleClass("btn-primary");
    result.bindWidget("button-primary", button);
    button = new WPushButton("Secondary");
    result.bindWidget("button-secondary", button);
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
    button = new WPushButton("Light");
    button.setStyleClass("btn-light");
    result.bindWidget("button-light", button);
    button = new WPushButton("Dark");
    button.setStyleClass("btn-dark");
    result.bindWidget("button-dark", button);
    button = new WPushButton("Outline");
    button.setStyleClass("btn-outline-primary");
    result.bindWidget("button-outline", button);
    button = new WPushButton("Link");
    button.setStyleClass("btn-link");
    result.bindWidget("button-link", button);
    button = new WPushButton("");
    button.setStyleClass("btn-close");
    result.bindWidget("button-close", button);
    return result;
  }

  WWidget PushButtonSize() {
    WTemplate result = new WTemplate(WString.tr("pushButtonSize-template"));
    WPushButton button = new WPushButton("Large");
    button.setStyleClass("btn-lg");
    result.bindWidget("button-large", button);
    button = new WPushButton("Default");
    result.bindWidget("button-default", button);
    button = new WPushButton("Small");
    button.setStyleClass("btn-sm");
    result.bindWidget("button-small", button);
    return result;
  }

  WWidget PushButtonPrimary() {
    WContainerWidget container = new WContainerWidget();
    WPushButton button = new WPushButton("Save", (WContainerWidget) container);
    button.setStyleClass("btn-primary");
    button = new WPushButton("Cancel", (WContainerWidget) container);
    button.setMargin(new WLength(5), EnumSet.of(Side.Left));
    return container;
  }

  WWidget PushButtonAction() {
    WTemplate result = new WTemplate(WString.tr("pushButtonAction-template"));
    WPushButton button = new WPushButton("Save");
    result.bindWidget("button-save", button);
    button.setStyleClass("btn-primary");
    result.bindWidget("button-cancel", new WPushButton("Cancel"));
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
    final WPushButton button = new WPushButton("Save");
    t.bindWidget("button", button);
    final WText out = new WText();
    t.bindWidget("age-info", out);
    out.setInline(false);
    out.hide();
    button
        .clicked()
        .addListener(
            this,
            () -> {
              out.show();
              if (ageEdit.validate() == ValidationState.Valid) {
                out.setText("Age of " + ageEdit.getText() + " is saved!");
                out.setStyleClass("alert alert-success");
              } else {
                out.setText("The number must be in the range 0 to 150");
                out.setStyleClass("alert alert-danger");
              }
            });
    ageEdit
        .enterPressed()
        .addListener(
            this,
            () -> {
              button.clicked().trigger(new WMouseEvent());
            });
    return t;
  }

  WWidget ValidationDate() {
    WTemplate t = new WTemplate(WString.tr("date-template"));
    t.addFunction("id", WTemplate.Functions.id);
    final WDateEdit dateEdit = new WDateEdit();
    t.bindWidget("birth-date", dateEdit);
    final WDateValidator dv = new WDateValidator();
    dv.setBottom(new WDate(1900, 1, 1));
    dv.setTop(WDate.getCurrentDate());
    dv.setFormat("dd/MM/yyyy");
    dv.setMandatory(true);
    dv.setInvalidBlankText("A birthdate is mandatory!");
    dv.setInvalidNotADateText("You should enter a date in the format \"dd/MM/yyyy\"!");
    dv.setInvalidTooEarlyText(
        new WString("That's too early... The date must be {1} or later!")
            .arg(dv.getBottom().toString("dd/MM/yyyy")));
    dv.setInvalidTooLateText(
        new WString("That's too late... The date must be {1} or earlier!")
            .arg(dv.getTop().toString("dd/MM/yyyy")));
    dateEdit.setValidator(dv);
    final WPushButton button = new WPushButton("Ok");
    t.bindWidget("button", button);
    final WText out = new WText();
    t.bindWidget("info", out);
    out.setInline(false);
    out.hide();
    button
        .clicked()
        .addListener(
            this,
            () -> {
              out.show();
              WValidator.Result result = dv.validate(dateEdit.getText());
              if (result.getState() == ValidationState.Valid) {
                WDate d = WDate.getCurrentServerDate();
                int years = d.getYear() - dateEdit.getDate().getYear();
                int days = d.getDaysTo(dateEdit.getDate().addYears(years));
                if (days < 0) {
                  days = d.getDaysTo(dateEdit.getDate().addYears(years + 1));
                }
                out.setText(
                    "<p>In "
                        + String.valueOf(days)
                        + " days, we will be celebrating your next anniversary!</p>");
                out.setStyleClass("alert alert-success");
              } else {
                dateEdit.setFocus(true);
                out.setText(result.getMessage());
                out.setStyleClass("alert alert-danger");
              }
            });
    dateEdit
        .enterPressed()
        .addListener(
            this,
            () -> {
              button.clicked().trigger(new WMouseEvent());
            });
    return t;
  }

  WWidget ValidationModel() {
    AgeFormView view = new AgeFormView();
    return view;
  }
}
