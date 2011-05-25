package eu.webtoolkit.jwt.examples.simplechat;

import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.Icon;
import eu.webtoolkit.jwt.JSlot;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.Signal1.Listener;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.TextFormat;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDialog;
import eu.webtoolkit.jwt.WHBoxLayout;
import eu.webtoolkit.jwt.WLabel;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WSound;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTextArea;
import eu.webtoolkit.jwt.WVBoxLayout;
import eu.webtoolkit.jwt.WWebWidget;

/**
 * A self-contained chat widget.
 */
public class SimpleChatWidget extends WContainerWidget {
	/**
	 * Create a chat widget that will connect to the given server.
	 */
	public SimpleChatWidget(SimpleChatServer server, WContainerWidget parent) {
		super(parent);
		server_ = server;
		app_ = WApplication.getInstance();
		messageReceived_ = new WSound("sounds/message_received.mp3");
		user_ = server_.suggestGuest();
		letLogin();

		// this widget supports server-side updates its processChatEvent()
		// method is connected to a slot that is triggered from outside this
		// session's event loop (usually because another user enters text).
		app_.enableUpdates();
	}

	public void finalize() {
		logout();
	}

	/**
	 * Show a simple login screen.
	 */
	public void letLogin() {
		clear();

		WVBoxLayout vLayout = new WVBoxLayout();
		setLayout(vLayout, AlignmentFlag.AlignLeft, AlignmentFlag.AlignTop);

		WHBoxLayout hLayout = new WHBoxLayout();
		vLayout.addLayout(hLayout);

		hLayout.addWidget(new WLabel("User name:"), 0,
				AlignmentFlag.AlignMiddle);
		hLayout.addWidget(userNameEdit_ = new WLineEdit(user_), 0,
				AlignmentFlag.AlignMiddle);
		userNameEdit_.setFocus();

		WPushButton b = new WPushButton("Login");
		hLayout.addWidget(b, 0, AlignmentFlag.AlignMiddle);
		hLayout.addStretch(1);

		b.clicked().addListener(this, new Listener<WMouseEvent>() {
			public void trigger(WMouseEvent arg) {
				login();
			}
		});

		userNameEdit_.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				login();
			}
		});

		vLayout.addWidget(statusMsg_ = new WText());
		statusMsg_.setTextFormat(TextFormat.PlainText);
	}

	/**
	 * Start a chat for the given user.
	 * 
	 * Returns false if the user could not login.
	 */
	public boolean startChat(final String user) {
		if (server_.login(user)) {
			listener_ = new Signal1.Listener<ChatEvent>() {
				public void trigger(ChatEvent ce) {
					processChatEvent(ce);
				}
			};
			server_.chatEvent().addListener(this, listener_);

			user_ = user;

			clear();

			/*
			 * Create a vertical layout, which will hold 3 rows, organized like
			 * this:
			 * 
			 * WVBoxLayout -------------------------------------------- | nested
			 * WHBoxLayout (vertical stretch=1) | | | | | messages | userslist |
			 * | (horizontal stretch=1) | | | | |
			 * -------------------------------------------- | message edit area
			 * | -------------------------------------------- | WHBoxLayout | |
			 * send | logout | stretch = 1 |
			 * --------------------------------------------
			 */
			WVBoxLayout vLayout = new WVBoxLayout();

			// Create a horizontal layout for the messages | userslist.
			WHBoxLayout hLayout = new WHBoxLayout();

			// Add widget to horizontal layout with stretch = 1
			hLayout.addWidget(messages_ = new WContainerWidget(), 1);
			messages_.setStyleClass("chat-msgs");
			// Display scroll bars if contents overflows
			messages_.setOverflow(Overflow.OverflowAuto);

			// Add another widget to horizontal layout with stretch = 0
			hLayout.addWidget(userList_ = new WContainerWidget());
			userList_.setStyleClass("chat-users");
			userList_.setOverflow(Overflow.OverflowAuto);

			hLayout.setResizable(0, true);

			// Add nested layout to vertical layout with stretch = 1
			vLayout.addLayout(hLayout, 1);

			// Add widget to vertical layout with stretch = 0
			vLayout.addWidget(messageEdit_ = new WTextArea());
			messageEdit_.setStyleClass("chat-noedit");
			messageEdit_.setRows(2);
			messageEdit_.setFocus();

			// Create a horizontal layout for the buttons.
			hLayout = new WHBoxLayout();

			// Add button to horizontal layout with stretch = 0
			hLayout.addWidget(sendButton_ = new WPushButton("Send"));
			WPushButton b = new WPushButton("Logout");
			
			// Add button to horizontal layout with stretch = 0
			hLayout.addWidget(b);

			// Add stretching spacer to horizontal layout
			hLayout.addStretch(1);

			// Add nested layout to vertical layout with stretch = 0
			vLayout.addLayout(hLayout);

			setLayout(vLayout);

			/*
			 * Connect event handlers: - click on button - enter in text area
			 * 
			 * We will clear the input field using a small custom client-side
			 * JavaScript invocation.
			 */

			// Create a JavaScript 'slot' (JSlot). The JavaScript slot always
			// takes
			// 2 arguments: the originator of the event (in our case the
			// button or text area), and the JavaScript event object.
			clearInput_.setJavaScript("function(o, e) {"
					+ messageEdit_.getJsRef() + ".value='';}");

			// Bind the C++ and JavaScript event handlers.
			sendButton_.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent arg) {
							send();
						}
					});

			messageEdit_.enterPressed().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							send();
						}
					});

			sendButton_.clicked().addListener(clearInput_);
			messageEdit_.enterPressed().addListener(clearInput_);

			// Prevent the enter from generating a new line, which is its
			// default function
			messageEdit_.enterPressed().preventDefaultAction(true);

			b.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
				public void trigger(WMouseEvent arg) {
					logout();
				}
			});

			WText msg = new WText(
					"<div><span class='chat-info'>You are joining the conversation as "
							+ user_ + "</span></div>", messages_);
			msg.setStyleClass("chat-msg");

			updateUsers();

			return true;
		} else
			return false;
	}

	private SimpleChatServer server_;
	private WApplication app_;

	private JSlot clearInput_ = new JSlot();

	private String user_;

	private WLineEdit userNameEdit_;
	private WText statusMsg_;

	private WContainerWidget messages_;
	private WContainerWidget messageEditArea_;
	private WTextArea messageEdit_;
	private WPushButton sendButton_;
	private WContainerWidget userList_;

	private Signal1.Listener<ChatEvent> listener_;

	private WSound messageReceived_;

	private void login() {
		String name = WWebWidget.escapeText(userNameEdit_.getText());

		if (!startChat(name))
			statusMsg_.setText("Sorry, name '" + name + "' is already taken.");
	}

	private void logout() {
		StandardButton result = WMessageBox.show("Please Confirm", "Do you really want to logout?",
				EnumSet.of(StandardButton.Yes, StandardButton.No));
		
		if (result == StandardButton.No)
			return;
		
		if (listener_ != null) {
			server_.chatEvent().removeListener(listener_); // do not listen for more events
			listener_ = null;
			
			server_.logout(user_);

			letLogin();
		}
	}

	private void send() {
		if (!messageEdit_.getText().isEmpty()) {
			server_.sendMessage(user_, messageEdit_.getText());
			if (!WApplication.getInstance().getEnvironment().hasAjax())
				messageEdit_.setText("");
		}

		messageEdit_.setFocus();
	}

	private void updateUsers() {
		userList_.clear();

		String usersStr = "";

		for (String u : server_.users()) {
			if (u.equals(user_))
				usersStr += "<div><span class='chat-self'>" + u
						+ "</span></div>";
			else
				usersStr += "<div>" + u + "</div>";
		}

		userList_.addWidget(new WText(usersStr));
	}

	/* called from another session */
	private void processChatEvent(final ChatEvent event) {
		/*
		 * This is where the "server-push" happens. This method is called when a
		 * new event or message needs to be notified to the user. In general, it
		 * is called from another session.
		 */

		/*
		 * First, take the lock to safely manipulate the UI outside of the
		 * normal event loop, by having exclusive access to the session.
		 */
		WApplication.UpdateLock lock = app_.getUpdateLock();

		/*
		 * Format and append the line to the conversation.
		 * 
		 * This is also the step where the automatic XSS filtering will kick in:
		 * - if another user tried to pass on some JavaScript, it is filtered
		 * away. - if another user did not provide valid XHTML, the text is
		 * automatically interpreted as PlainText
		 */
		WText w = new WText(event.formattedHTML(user_), messages_);
		w.setInline(false);
		w.setStyleClass("chat-msg");

		/*
		 * Leave not more than 100 messages in the back-log
		 */
		if (messages_.getCount() > 100)
			messages_.getChildren().remove(0);

		/*
		 * If it is not a normal message, also update the user list.
		 */
		if (event.type() != ChatEvent.Type.Message)
			updateUsers();

		/*
		 * Little javascript trick to make sure we scroll along with new content
		 */
		app_.doJavaScript(messages_.getJsRef() + ".scrollTop += "
				+ messages_.getJsRef() + ".scrollHeight;");

		/* If this message belongs to another user, play a received sound */
		if (event.user() != user_)
			messageReceived_.play();

		app_.triggerUpdate();
		
		lock.release();
	}
}
