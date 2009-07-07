/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.dialog;

import java.util.EnumSet;

import eu.webtoolkit.jwt.Icon;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;

/**
 * An example illustrating usage of Dialogs
 */
public class DialogApplication extends WApplication
{
  /**
   * Create the example application.
   */
	public DialogApplication(WEnvironment env) {
		  super(env);
		    messageBox_ = null;

		    setTitle("Dialog example");

		  new WText("<h2>Wt dialogs example</h2>", getRoot());

		  WContainerWidget textdiv = new WContainerWidget(getRoot());
		  textdiv.setStyleClass("text");

		  new WText("You can use WMessageBox for simple modal dialog boxes. <br />",
			    textdiv);

		  WContainerWidget buttons = new WContainerWidget(getRoot());
		  buttons.setStyleClass("buttons");

		  WPushButton button;

		  button = new WPushButton("One liner", buttons);
		  button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
			public void trigger(WMouseEvent a1) {
				messageBox1();
			}
		  });

		  button = new WPushButton("Comfortable ?", buttons);
		  button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
				public void trigger(WMouseEvent a1) {
					messageBox2();
				}
			  });

		  button = new WPushButton("Havoc!", buttons);
		  button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
				public void trigger(WMouseEvent a1) {
					messageBox3();
				}
			  });

		  button = new WPushButton("Discard", buttons);
		  button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
				public void trigger(WMouseEvent a1) {
					messageBox4();
				}
			  });

		  button = new WPushButton("Familiar", buttons);
		  button.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
				public void trigger(WMouseEvent a1) {
					custom();
				}
			  });

		  textdiv = new WContainerWidget(getRoot());
		  textdiv.setStyleClass("text");

		  status_ = new WText("Go ahead...", textdiv);

		  getStyleSheet().addRule(".buttons",
				       "padding: 5px;");
		  getStyleSheet().addRule(".buttons BUTTON",
				       "padding-left: 4px; padding-right: 4px;"+
				       "margin-top: 4px; display: block");
		  getStyleSheet().addRule(".text", "padding: 4px 8px");

		  if (getEnvironment().agentIsIE())
		    getStyleSheet().addRule("body", "margin: 0px;"); // avoid scrollbar problems
	}

	private WMessageBox show(String caption, String text, StandardButton button)
	{
		return show(caption, text, EnumSet.of(button));
	}
	
	private WMessageBox show(String caption, String text, EnumSet<StandardButton> buttons)
	{
	  final WMessageBox box = new WMessageBox(caption, text, Icon.Information, buttons, false);
	  box.show();
	  
	  return box;
	}
	
	private void messageBox1() {
		final WMessageBox box = show("Information","Enjoy displaying messages with a one-liner.", StandardButton.Ok);
		box.buttonClicked().addListener(this, new Signal1.Listener<StandardButton>(){
			public void trigger(StandardButton sb) {
				box.remove();
				setStatus("Ok'ed");
			}
		});
		
	}
	private void messageBox2() {
		  messageBox_
		    = new WMessageBox("Question",
				      "Are you getting comfortable ?",
				      Icon.NoIcon, EnumSet.of(StandardButton.Yes ,StandardButton.No ,StandardButton.Cancel));

		  messageBox_.show();
		  
		  messageBox_.buttonClicked().addListener(this, new Signal1.Listener<StandardButton>(){
				public void trigger(StandardButton sb) {
					messageBox_.remove();
					messageBoxDone(sb);
				}
			});
	}
	private void messageBox3() {
		  final WMessageBox box = show("Confirm", "About to wreak havoc... Continue ?",
					       EnumSet.of(StandardButton.Ok, StandardButton.Cancel));
		  
		  box.buttonClicked().addListener(this, new Signal1.Listener<StandardButton>(){
				public void trigger(StandardButton sb) {
					box.remove();
					messageBoxDone(sb);
					
					if(sb == StandardButton.Ok) {
						setStatus("Wreaking havoc.");
					} else {
						setStatus("Cancelled!");
					}
				}
			});
	}
	private void messageBox4() {
		  messageBox_
		    = new WMessageBox("Your work",
				      "Your work is not saved",
				      Icon.NoIcon, EnumSet.of(StandardButton.NoButton));

		  messageBox_.addButton("Cancel modifications", StandardButton.Ok);
		  messageBox_.addButton("Continue modifying work", StandardButton.Cancel);

		  messageBox_.show();
		  
		  messageBox_.buttonClicked().addListener(this, new Signal1.Listener<StandardButton>(){
				public void trigger(StandardButton sb) {
					messageBox_.remove();
					messageBoxDone(sb);
				}
			});
	}
	
	private void custom() {
		  final WDialog dialog = new WDialog("Personalia");

		  new WText("Enter your name: ", dialog.getContents());
		  final WLineEdit edit = new WLineEdit(dialog.getContents());
		  new WBreak(dialog.getContents());
		  WPushButton ok = new WPushButton ("Ok", dialog.getContents());

		  edit.setFocus();

		  dialog.show();
		  
		  edit.enterPressed().addListener(this, new Signal.Listener(){
			public void trigger() {
				setStatus("Welcome, " + edit.getText());
				dialog.remove();
			}
		  });
		  
		  ok.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
				public void trigger(WMouseEvent me) {
					setStatus("Welcome, " + edit.getText());
					dialog.remove();
				}
			  });
	}

	private void messageBoxDone(StandardButton result) {
		  switch (result) {
		  case Ok:
		    setStatus("Ok'ed"); break;
		  case Cancel:
		    setStatus("Cancelled!"); break;
		  case Yes:
		    setStatus("Me too!"); break;
		  case No:
		    setStatus("Me neither!"); break;
		  default:
		    setStatus("Unkonwn result?");
		  }

		  messageBox_ = null;
	}

	private void setStatus(String text) {
		status_.setText(text);
	}

	private WMessageBox messageBox_;
	private WText status_;
}

