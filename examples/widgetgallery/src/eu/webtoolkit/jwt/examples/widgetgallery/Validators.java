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

class Validators extends ControlsWidget {
	public Validators(EventDisplayer ed) {
		super(ed, false);
		this.fields_ = new ArrayList<Pair<WFormWidget, WText>>();
		this.topic("WValidator", this);
		new WText(tr("validators-intro"), this);
		new WText("<h2>Validator types</h2>", this);
		WTable table = new WTable(this);
		table.setStyleClass("validators");
		WLineEdit le;
		new WText(
				"<tt>WIntValidator</tt>: input is mandatory and in range [50 - 100]",
				table.getElementAt(0, 0));
		le = new WLineEdit(table.getElementAt(0, 1));
		WIntValidator iv = new WIntValidator(50, 100);
		iv.setMandatory(true);
		le.setValidator(iv);
		this.fields_.add(new Pair<WFormWidget, WText>(le, new WText("", table
				.getElementAt(0, 2))));
		new WText("<tt>WDoubleValidator</tt>: range [-5.0 to 15.0]", table
				.getElementAt(1, 0));
		le = new WLineEdit(table.getElementAt(1, 1));
		le.setValidator(new WDoubleValidator(-5, 15));
		this.fields_.add(new Pair<WFormWidget, WText>(le, new WText("", table
				.getElementAt(1, 2))));
		new WText("<tt>WDateValidator</tt>, default format \"yyyy-MM-dd\"",
				table.getElementAt(2, 0));
		le = new WLineEdit(table.getElementAt(2, 1));
		le.setValidator(new WDateValidator());
		this.fields_.add(new Pair<WFormWidget, WText>(le, new WText("", table
				.getElementAt(2, 2))));
		new WText("<tt>WDateValidator</tt>, format \"dd-MM-yy\"", table
				.getElementAt(3, 0));
		le = new WLineEdit(table.getElementAt(3, 1));
		le.setValidator(new WDateValidator("dd-MM-yy"));
		this.fields_.add(new Pair<WFormWidget, WText>(le, new WText("", table
				.getElementAt(3, 2))));
		new WText(
				"<tt>WDateValidator</tt>, format \"yy-MM-dd\", range 1 to 15 October 08",
				table.getElementAt(4, 0));
		le = new WLineEdit(table.getElementAt(4, 1));
		le.setValidator(new WDateValidator("yy-MM-dd", new WDate(2008, 10, 1),
				new WDate(2008, 10, 15)));
		this.fields_.add(new Pair<WFormWidget, WText>(le, new WText("", table
				.getElementAt(4, 2))));
		new WText("<tt>WLengthValidator</tt>, 6 to 11 characters", table
				.getElementAt(5, 0));
		le = new WLineEdit(table.getElementAt(5, 1));
		le.setValidator(new WLengthValidator(6, 11));
		this.fields_.add(new Pair<WFormWidget, WText>(le, new WText("", table
				.getElementAt(5, 2))));
		String ipRegexp = "((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
		new WText("<tt>WRegExpValidator</tt>, IP address", table.getElementAt(
				6, 0));
		le = new WLineEdit(table.getElementAt(6, 1));
		le.setValidator(new WRegExpValidator(ipRegexp));
		this.fields_.add(new Pair<WFormWidget, WText>(le, new WText("", table
				.getElementAt(6, 2))));
		new WText("<p>The IP address validator regexp is: <tt>" + ipRegexp
				+ "</tt></p>", this);
		new WText(
				"<p>All WFormWidgets can have validators, so also the WTextArea. Type up to 50 characters in the box below</p>",
				this);
		WTextArea ta = new WTextArea(this);
		ta.setMargin(new WLength(4), EnumSet.of(Side.Right));
		ta.setValidator(new WLengthValidator(0, 50));
		this.fields_.add(new Pair<WFormWidget, WText>(ta, new WText("", this)));
		new WText("<h2>Server-side validation</h2>", this);
		new WText(
				"<p>The button below causes the server to validate all input fields above server-side, and puts the state of the validation on the right of every widget: <dl> <dt><tt>Valid</tt></dt><dd>data is valid</dd> <dt><tt>Invalid</tt></dt><dd>data is invalid</dd> <dt><tt>InvalidEmpty</tt></dt><dd>field is empty, but was indicated to be mandatory</dd></dl></p>",
				this);
		WPushButton pb = new WPushButton("Validate server-side", this);
		pb.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				Validators.this.validateServerside();
			}
		});
		ed.showSignal(pb.clicked(),
				"WPushButton: request server-side validation");
	}

	private void validateServerside() {
		for (int i = 0; i < this.fields_.size(); ++i) {
			switch (this.fields_.get(i).first.validate()) {
			case Valid:
				this.fields_.get(i).second.setText("Valid");
				break;
			case InvalidEmpty:
				this.fields_.get(i).second.setText("InvalidEmpty");
				break;
			case Invalid:
				this.fields_.get(i).second.setText("Invalid");
				break;
			}
		}
	}

	private List<Pair<WFormWidget, WText>> fields_;
}
