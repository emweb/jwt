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

class DialogWidgets extends ControlsWidget {
	public DialogWidgets(EventDisplayer ed) {
		super(ed, true);
		new WText(tr("dialogs-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("WDialog", this.wDialog());
		menu.addItem("WMessageBox", this.wMessageBox());
	}

	private WWidget wDialog() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WDialog", result);
		new WText(tr("dialogs-WDialog"), result);
		WPushButton button = new WPushButton("Modal dialog", result);
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				DialogWidgets.this.customModal();
			}
		});
		button = new WPushButton("Non-modal dialog", result);
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				DialogWidgets.this.customNonModal();
			}
		});
		return result;
	}

	private WWidget wMessageBox() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WMessageBox", result);
		new WText(tr("dialogs-WMessageBox"), result);
		WContainerWidget ex = new WContainerWidget(result);
		WVBoxLayout vLayout = new WVBoxLayout();
		ex.setLayout(vLayout, EnumSet.of(AlignmentFlag.AlignTop,
				AlignmentFlag.AlignLeft));
		vLayout.setContentsMargins(0, 0, 0, 0);
		vLayout.setSpacing(3);
		WPushButton button;
		vLayout.addWidget(button = new WPushButton("One liner"));
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				DialogWidgets.this.messageBox1();
			}
		});
		vLayout.addWidget(button = new WPushButton("Show some buttons"));
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				DialogWidgets.this.messageBox2();
			}
		});
		vLayout.addWidget(button = new WPushButton("Need confirmation"));
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				DialogWidgets.this.messageBox3();
			}
		});
		vLayout.addWidget(button = new WPushButton("Discard"));
		button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				DialogWidgets.this.messageBox4();
			}
		});
		return result;
	}

	// private WWidget eDialogs() ;
	private void messageBox1() {
		WMessageBox.show("Information",
				"One-liner dialogs have a simple constructor", EnumSet
						.of(StandardButton.Ok));
		this.ed_.setStatus("Ok'ed");
	}

	private void messageBox2() {
		this.messageBox_ = new WMessageBox(
				"Question",
				"This is a modal dialog that invokes a signal when a button is pushed",
				Icon.NoIcon, EnumSet.of(StandardButton.Yes, StandardButton.No,
						StandardButton.Cancel));
		this.messageBox_.buttonClicked().addListener(this,
				new Signal1.Listener<StandardButton>() {
					public void trigger(StandardButton e1) {
						DialogWidgets.this.messageBoxDone(e1);
					}
				});
		this.messageBox_.show();
	}

	private void messageBox3() {
		StandardButton result = WMessageBox
				.show(
						"Push it",
						"Yes/No questions can be tested by checking show()'s return value",
						EnumSet.of(StandardButton.Ok, StandardButton.Cancel));
		if (result == StandardButton.Ok) {
			this.ed_.setStatus("Accepted!");
		} else {
			this.ed_.setStatus("Cancelled!");
		}
	}

	private void messageBox4() {
		this.messageBox_ = new WMessageBox("Your work",
				"Provide your own button text.<br/>Your work is not saved",
				Icon.NoIcon, EnumSet.of(StandardButton.NoButton));
		this.messageBox_.addButton("Cancel modifications",
				StandardButton.Cancel);
		this.messageBox_
				.addButton("Continue modifying work", StandardButton.Ok);
		this.messageBox_.buttonClicked().addListener(this,
				new Signal1.Listener<StandardButton>() {
					public void trigger(StandardButton e1) {
						DialogWidgets.this.messageBoxDone(e1);
					}
				});
		this.messageBox_.show();
	}

	private void customModal() {
		final WDialog dialog = new WDialog("Personalia (modal)");
		new WText(
				"You can freely format the contents of a WDialog by adding any widget you want to it.<br/>Here, we added WText, WLineEdit and WPushButton to a dialog",
				dialog.getContents());
		new WBreak(dialog.getContents());
		new WText("Enter your name: ", dialog.getContents());
		final WLineEdit edit = new WLineEdit(dialog.getContents());
		new WBreak(dialog.getContents());
		final WPushButton ok = new WPushButton("Ok", dialog.getContents());
		edit.enterPressed().addListener(dialog, new Signal.Listener() {
			public void trigger() {
				dialog.accept();
			}
		});
		ok.clicked().addListener(dialog, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				dialog.accept();
			}
		});
		if (dialog.exec() == WDialog.DialogCode.Accepted) {
			this.ed_.setStatus("Welcome, " + edit.getText());
		}
	}

	private void customNonModal() {
		NonModalDialog dialog = new NonModalDialog("Personalia (non-modal)",
				this.ed_);
		dialog.show();
	}

	private void messageBoxDone(StandardButton result) {
		switch (result) {
		case Ok:
			this.ed_.setStatus("Ok'ed");
			break;
		case Cancel:
			this.ed_.setStatus("Cancelled!");
			break;
		case Yes:
			this.ed_.setStatus("Me too!");
			break;
		case No:
			this.ed_.setStatus("Me neither!");
			break;
		default:
			this.ed_.setStatus("Unkonwn result?");
		}
		if (this.messageBox_ != null)
			this.messageBox_.remove();
		this.messageBox_ = null;
	}

	// private void setStatus(CharSequence text) ;
	private WMessageBox messageBox_;
	private WText status_;
}
