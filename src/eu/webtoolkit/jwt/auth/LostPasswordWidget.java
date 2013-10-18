/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

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

/**
 * A widget which initiates a lost-password email.
 * <p>
 * 
 * The widget renders the
 * <code>&quot;Wt.Auth.template.lost-password&quot;</code> template. It prompts
 * for an email address and then invokes
 * {@link AuthService#lostPassword(String emailAddress, AbstractUserDatabase users)
 * AuthService#lostPassword()} with the given email address.
 * <p>
 * 
 * @see AuthWidget#getCreateLostPasswordView()
 */
public class LostPasswordWidget extends WTemplate {
	private static Logger logger = LoggerFactory
			.getLogger(LostPasswordWidget.class);

	/**
	 * Constructor.
	 */
	public LostPasswordWidget(AbstractUserDatabase users, AuthService auth,
			WContainerWidget parent) {
		super(tr("Wt.Auth.template.lost-password"), parent);
		this.users_ = users;
		this.baseAuth_ = auth;
		this.addFunction("id", Functions.id);
		this.addFunction("tr", Functions.tr);
		this.addFunction("block", Functions.block);
		WLineEdit email = new WLineEdit();
		email.setFocus();
		WPushButton okButton = new WPushButton(tr("Wt.Auth.send"));
		WPushButton cancelButton = new WPushButton(tr("Wt.WMessageBox.Cancel"));
		okButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						LostPasswordWidget.this.send();
					}
				});
		cancelButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						LostPasswordWidget.this.cancel();
					}
				});
		this.bindWidget("email", email);
		this.bindWidget("send-button", okButton);
		this.bindWidget("cancel-button", cancelButton);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls
	 * {@link #LostPasswordWidget(AbstractUserDatabase users, AuthService auth, WContainerWidget parent)
	 * this(users, auth, (WContainerWidget)null)}
	 */
	public LostPasswordWidget(AbstractUserDatabase users, AuthService auth) {
		this(users, auth, (WContainerWidget) null);
	}

	protected void send() {
		try {
			WFormWidget email = (WFormWidget) this.resolveWidget("email");
			this.baseAuth_.lostPassword(email.getValueText(), this.users_);
			this.cancel();
			final WMessageBox box = new WMessageBox(
					tr("Wt.Auth.lost-password"), tr("Wt.Auth.mail-sent"),
					Icon.NoIcon, EnumSet.of(StandardButton.Ok));
			box.buttonClicked().addListener(this, new Signal.Listener() {
				public void trigger() {
					LostPasswordWidget.this.deleteBox(box);
				}
			});
			box.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void cancel() {
		if (this != null)
			this.remove();
	}

	private AbstractUserDatabase users_;
	private AuthService baseAuth_;

	private void deleteBox(WMessageBox box) {
		if (box != null)
			box.remove();
	}
}
