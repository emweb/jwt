package eu.webtoolkit.jwt.examples.planner;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.ValidationState;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WValidator;
import eu.webtoolkit.jwt.examples.planner.captcha.ShapesCaptchaWidget;

public class LoginWidget extends WContainerWidget {
	private WLineEdit userNameEdit;
	private ShapesCaptchaWidget captcha;
	private WPushButton loginButton;
	private Signal1<String> loggedIn = new Signal1<String>();
	
	 public LoginWidget(WContainerWidget parent) {
		 super(parent);
		 
		 //set the css style class
		 setStyleClass("login");
		 
		 //construct a WLabel
		 WLabel userNameL = new WLabel(tr("login.userName"), this);
		 
		 //construct a WLineEdit
		 userNameEdit = new WLineEdit(this);
		 //give the line edit focus
		 userNameEdit.setFocus();
		 //set the default WValidator as validator,
		 //this invalidates the introduction of an empty user name
		 userNameEdit.setValidator(new WValidator(true));
		 
		 //connect the label to the user name text field
		 userNameL.setBuddy(userNameEdit);
		
		 //login when enter is pressed and the text input is not empty
		 userNameEdit.enterPressed().addListener(this, new Signal.Listener(){
			public void trigger() {
				if (userNameEdit.validate() == ValidationState.Valid && !loginButton.isHidden())
					login();
			}
		 });
		 
		 //construct a WPushButton and hide it
		 loginButton = new WPushButton(tr("login.loginButton"), this);
		 loginButton.hide();
		 
		 //when clicked and the text input provided is not empty login
		 loginButton.clicked().addListener(this, new Signal1.Listener<WMouseEvent>(){
			public void trigger(WMouseEvent arg) {
				if (userNameEdit.validate() == ValidationState.Valid)
					login();
			}
		 });
		 
		 //construct the captcha widget
		 captcha = new ShapesCaptchaWidget(150, 70, this);
		 captcha.completed().addListener(this, new Signal.Listener(){
			public void trigger() {
				if (userNameEdit.validate() != ValidationState.Valid) {
					captcha.hide();
					loginButton.show();
					userNameEdit.setFocus();
				} else {
					login();
				}
			}
		 });
	 }
	 
	 public Signal1<String> loggedIn() {
		 return loggedIn;
	 }
	 
	 //logging in triggers the loggedIn signal
	 private void login() {
		 loggedIn.trigger(userNameEdit.getText().trim());
	 }
}
