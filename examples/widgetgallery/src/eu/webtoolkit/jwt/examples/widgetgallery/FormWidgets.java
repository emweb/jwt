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

class FormWidgets extends ControlsWidget {
	public FormWidgets(EventDisplayer ed) {
		super(ed, true);
		new WText(tr("formwidgets-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("WPushButton", this.wPushButton());
		menu.addItem("WCheckBox", this.wCheckBox());
		menu.addItem("WRadioButton", this.wRadioButton());
		menu.addItem("WComboBox", this.wComboBox());
		menu.addItem("WSelectionBox", this.wSelectionBox());
		menu.addItem("WLineEdit", this.wLineEdit());
		menu.addItem("WSpinBox", this.wSpinBox());
		menu.addItem("WTextArea", this.wTextArea());
		menu.addItem("WCalendar", this.wCalendar());
		menu.addItem("WDatePicker", this.wDatePicker());
		menu.addItem("WInPlaceEdit", this.wInPlaceEdit());
		menu.addItem("WSuggestionPopup", this.wSuggestionPopup());
		menu.addItem("WTextEdit", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return FormWidgets.this.wTextEdit();
					}
				}));
		menu.addItem("WFileUpload", this.wFileUpload());
	}

	private WWidget wPushButton() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WPushButton", result);
		new WText(tr("formwidgets-WPushButton"), result);
		WPushButton pb = new WPushButton("Click me!", result);
		this.ed_.showSignal(pb.clicked(), "WPushButton click");
		new WText(tr("formwidgets-WPushButton-more"), result);
		pb = new WPushButton("Try to click me...", result);
		pb.setEnabled(false);
		return result;
	}

	private WWidget wCheckBox() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WCheckBox", result);
		new WText(tr("formwidgets-WCheckBox"), result);
		WCheckBox cb = new WCheckBox("Check me!", result);
		cb.setChecked(true);
		this.ed_.showSignal(cb.checked(), "'Check me!' checked");
		new WBreak(result);
		cb = new WCheckBox("Check me too!", result);
		this.ed_.showSignal(cb.checked(), "'Check me too!' checked");
		new WBreak(result);
		cb = new WCheckBox("Check me, I'm tristate!", result);
		cb.setTristate();
		cb.setCheckState(CheckState.PartiallyChecked);
		this.ed_.showSignal(cb.checked(), "'Check me, I'm tristate!' checked");
		return result;
	}

	private WWidget wRadioButton() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WRadioButton", result);
		new WText(tr("formwidgets-WRadioButton"), result);
		WRadioButton rb = null;
		rb = new WRadioButton("Radio me!", result);
		this.ed_.showSignal(rb.checked(),
				"'Radio me!' checked (not in buttongroup)");
		new WBreak(result);
		rb = new WRadioButton("Radio me too!", result);
		this.ed_.showSignal(rb.checked(),
				"'Radio me too!' checked (not in buttongroup)");
		new WText(tr("formwidgets-WRadioButton-group"), result);
		WButtonGroup wgb = new WButtonGroup(result);
		rb = new WRadioButton("Radio me!", result);
		this.ed_.showSignal(rb.checked(), "'Radio me!' checked");
		wgb.addButton(rb);
		new WBreak(result);
		rb = new WRadioButton("No, radio me!", result);
		this.ed_.showSignal(rb.checked(), "'No, Radio me!' checked");
		wgb.addButton(rb);
		new WBreak(result);
		rb = new WRadioButton("Nono, radio me!", result);
		this.ed_.showSignal(rb.checked(), "'Nono, radio me!' checked");
		wgb.addButton(rb);
		wgb.setSelectedButtonIndex(0);
		return result;
	}

	private WWidget wComboBox() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WComboBox", result);
		new WText(tr("formwidgets-WComboBox"), result);
		WComboBox cb = new WComboBox(result);
		cb.addItem("Heavy");
		cb.addItem("Medium");
		cb.addItem("Light");
		cb.setCurrentIndex(1);
		this.ed_.showSignal(cb.sactivated(), "Combo-box 1 activated: ");
		new WText(tr("formwidgets-WComboBox-model"), result);
		new WText(tr("formwidgets-WComboBox-style"), result);
		WComboBox colorCb = new WComboBox(result);
		WStandardItemModel model = new WStandardItemModel(colorCb);
		model.insertColumns(0, 3);
		this.addColorElement(model, "Red", "combo-red");
		this.addColorElement(model, "Blue", "combo-blue");
		this.addColorElement(model, "Green", "combo-green");
		colorCb.setModel(model);
		colorCb.setCurrentIndex(0);
		this.ed_.showSignal(colorCb.sactivated(), "Combo-box 2 activated: ");
		return result;
	}

	private WWidget wSelectionBox() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WSelectionBox", result);
		new WText(tr("formwidgets-WSelectionBox"), result);
		WSelectionBox sb1 = new WSelectionBox(result);
		sb1.addItem("Heavy");
		sb1.addItem("Medium");
		sb1.addItem("Light");
		sb1.setCurrentIndex(1);
		this.ed_.showSignal(sb1.sactivated(), "SelectionBox activated: ");
		new WText(
				"<p>... or multiple options (use shift and/or ctrl-click to select your pizza toppings)</p>",
				result);
		WSelectionBox sb2 = new WSelectionBox(result);
		sb2.addItem("Bacon");
		sb2.addItem("Cheese");
		sb2.addItem("Mushrooms");
		sb2.addItem("Green peppers");
		sb2.addItem("Red peppers");
		sb2.addItem("Ham");
		sb2.addItem("Pepperoni");
		sb2.addItem("Turkey");
		sb2.setSelectionMode(SelectionMode.ExtendedSelection);
		Set<Integer> selection = new HashSet<Integer>();
		selection.add(1);
		selection.add(2);
		selection.add(5);
		sb2.setSelectedIndexes(selection);
		this.ed_.showSignal(sb2.changed(), "SelectionBox 2 changed");
		new WText(tr("formwidgets-WSelectionBox-model"), result);
		return result;
	}

	private WWidget wLineEdit() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WLineEdit", result);
		new WText(tr("formwidgets-WLineEdit"), result);
		WLineEdit le = new WLineEdit(result);
		le.setEmptyText("Edit me");
		this.ed_.showSignal(le.keyWentUp(), "Line edit key up event");
		new WText(
				"<p>The line edit on the following line reacts on the enter button:</p>",
				result);
		le = new WLineEdit(result);
		this.ed_.showSignal(le.enterPressed(), "Line edit enter pressed event");
		new WText(tr("formwidgets-WLineEdit-more"), result);
		return result;
	}

	private WWidget wSpinBox() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WSpinBox", result);
		new WText(tr("formwidgets-WSpinBox"), result);
		new WText("Enter a number between 0 and 100: ", result);
		WSpinBox le = new WSpinBox(result);
		this.ed_.showSignal(le.changed(), "Spin box value changed");
		le.setValue(30);
		return result;
	}

	private WWidget wTextArea() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTextArea", result);
		new WText(tr("formwidgets-WTextArea"), result);
		WTextArea ta = new WTextArea(result);
		ta.setColumns(80);
		ta.setRows(15);
		ta.setText(tr("formwidgets-WTextArea-contents").toString());
		this.ed_.showSignal(ta.changed(), "Text area changed");
		new WText(tr("formwidgets-WTextArea-related"), result);
		return result;
	}

	private WWidget wCalendar() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WCalendar", result);
		new WText(tr("formwidgets-WCalendar"), result);
		WCalendar c = new WCalendar(result);
		this.ed_.showSignal(c.selectionChanged(),
				"First calendar's selection changed");
		new WText(
				"<p>A flag indicates if multiple dates can be selected...</p>",
				result);
		WCalendar c2 = new WCalendar(result);
		c2.setSelectionMode(SelectionMode.ExtendedSelection);
		this.ed_.showSignal(c2.selectionChanged(),
				"Second calendar's selection changed");
		return result;
	}

	private WWidget wDatePicker() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WDatePicker", result);
		new WText(
				"<p>The <tt>WDatePicker</tt> allows the entry of a date.</p>",
				result);
		WDatePicker dp1 = new WDatePicker(result);
		this.ed_.showSignal(dp1.getLineEdit().changed(),
				"Date picker 1 changed");
		new WText("(format " + dp1.getFormat() + ")", result);
		new WBreak(result);
		WDatePicker dp2 = new WDatePicker(result);
		this.ed_.showSignal(dp2.getLineEdit().changed(),
				"Date picker 2 changed");
		dp2.setFormat("dd MM yyyy");
		new WText("(format " + dp2.getFormat() + ")", result);
		return result;
	}

	private WWidget wInPlaceEdit() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WInPlaceEdit", result);
		new WText(
				"<p>This widget allows you to edit a text in-place by clicking on it. You can enable the save/cancel buttons (like here below) or disable them (as used in the <tt>WCalendar</tt> widget to edit the year).</p>",
				result);
		new WText("Try it here: ", result);
		WInPlaceEdit ipe = new WInPlaceEdit("This is editable text", result);
		ipe.setStyleClass("in-place-edit");
		this.ed_.showSignal(ipe.valueChanged(), "In-place edit changed: ");
		return result;
	}

	private WWidget wSuggestionPopup() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WSuggestionPopup", result);
		new WText(tr("formwidgets-WSuggestionPopup"), result);
		WSuggestionPopup.Options contactOptions = new WSuggestionPopup.Options();
		contactOptions.highlightBeginTag = "<span class=\"highlight\">";
		contactOptions.highlightEndTag = "</span>";
		contactOptions.listSeparator = ',';
		contactOptions.whitespace = " \\n";
		contactOptions.wordSeparators = "-., \"@\\n;";
		contactOptions.appendReplacedText = ", ";
		WSuggestionPopup sp = new WSuggestionPopup(WSuggestionPopup
				.generateMatcherJS(contactOptions), WSuggestionPopup
				.generateReplacerJS(contactOptions), result);
		WLineEdit le = new WLineEdit(result);
		le.setTextSize(50);
		le.setInline(false);
		sp.forEdit(le);
		sp.addSuggestion("John Tech <techie@mycompany.com>",
				"John Tech <techie@mycompany.com>");
		sp.addSuggestion("Johnny Cash <cash@mycompany.com>",
				"Johnny Cash <cash@mycompany.com>");
		sp.addSuggestion("John Rambo <rambo@mycompany.com>",
				"John Rambo <rambo@mycompany.com>");
		sp.addSuggestion("Johanna Tree <johanna@mycompany.com>",
				"Johanna Tree <johanna@mycompany.com>");
		return result;
	}

	private WWidget wTextEdit() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WTextEdit", result);
		new WText(
				"<p>The <tt>WTextEdit</tt> is a full-featured editor for rich textediting. It is based on the TinyMCE editor, which must be downloaded separately from its author's website. The TinyMCE toolbar layout and plugins can be configured through Wt's interface. The default, shown below, covers only a small portion of TinyMCE's capabilities.</p>",
				result);
		WTextEdit te = new WTextEdit(result);
		this.ed_.showSignal(te.changed(), "Text edit changed");
		return result;
	}

	private WWidget wFileUpload() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WFileUpload", result);
		new WText(tr("formwidgets-WFileUpload"), result);
		final WFileUpload fu = new WFileUpload(result);
		fu.setProgressBar(new WProgressBar());
		fu.changed().addListener(fu, new Signal.Listener() {
			public void trigger() {
				fu.upload();
			}
		});
		this.ed_.showSignal(fu.changed(), "File upload changed");
		this.ed_.showSignal(fu.uploaded(), "File upload finished");
		new WText(tr("formwidgets-WFileUpload-more"), result);
		return result;
	}

	private void addColorElement(WStandardItemModel model, String name,
			String style) {
		WStandardItem item = new WStandardItem(name);
		item.setStyleClass(style);
		model.appendRow(item);
	}
}
