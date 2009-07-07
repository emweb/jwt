/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

/**
 * A WDialog shows a dialog
 * <p>
 * 
 * By default, the dialog is <i>modal</i>. A modal window blocks the user
 * interface, and does not allow the user to interact with any other part of the
 * user interface until the dialog is closed (this is enforced at the server
 * side, so you may rely on this behavior).
 * <p>
 * There are two distinct ways for using a WDialog window.
 * <p>
 * A second way is by treating the WDialog as another widget. In this case, the
 * WDialog is created with the proper content, and for example an OK button is
 * connected to a method which deletes the dialog. Unlike other widgets, a
 * dialog is hidden by default. You must use the method {@link WWidget#show()}
 * or setHidden(true) to show the dialog.
 * <p>
 * Use setModal(false) to create a non-modal dialog. A non-modal dialog does not
 * block the underlying user interface: the user must not first deal with the
 * dialog before interacting with the rest of the user interface. Multiple
 * non-modal dialogs may be open at the same time.
 * <p>
 * Widgets may be added to the dialog, by adding to the
 * {@link WDialog#getContents()} ContainerWidget.
 * <p>
 * This dialog looks like this (using the standard look):
 * <p>
 * <div align="center"> <img src="doc-files//WDialog-1.png"
 * alt="A simple custom dialog">
 * <p>
 * <strong>A simple custom dialog</strong>
 * </p>
 * </div>
 * <p>
 * <p>
 * <i><b>Note:</b>Currently only one modal dialog can be shown at a time, but
 * you can show as many non-modal dialogs as you want.
 * <p>
 * For the dialog to render properly in IE, the &quot;body&quot; margin is set
 * to 0 (if it wasn&apos;t already). </i>
 * </p>
 */
public class WDialog extends WCompositeWidget {
	/**
	 * The result of a modal dialog execution.
	 */
	public enum DialogCode {
		/**
		 * Dialog closed with {@link WDialog#reject()}.
		 */
		Rejected,
		/**
		 * Dialog closed with {@link WDialog#accept()}.
		 */
		Accepted;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Construct a WDialog with a given window title.
	 * <p>
	 * Only a single Dialog may be constructed at any time. Unlike other
	 * widgets, a dialog does not need to be added to a container widget.
	 */
	public WDialog(CharSequence windowTitle) {
		super();
		this.modal_ = true;
		this.finished_ = new Signal1<WDialog.DialogCode>(this);
		this.recursiveEventLoop_ = false;
		this.mouseDownJS_ = new JSlot();
		this.mouseMovedJS_ = new JSlot();
		this.mouseUpJS_ = new JSlot();
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.impl_.setStyleClass("Wt-dialog");
		String CSS_RULES_NAME = "Wt::WDialog";
		WApplication app = WApplication.getInstance();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			if (app.getEnvironment().agentIsIE()) {
				app.getStyleSheet().addRule("body", "height: 100%;");
			}
			app
					.getStyleSheet()
					.addRule(
							"div.Wt-dialogcover",
							""
									+ "background: white;height: 100%; width: 100%;top: 0px; left: 0px;opacity: 0.5; position: fixed;"
									+ (app.getEnvironment().agentIsIE() ? "filter: alpha(opacity=50);"
											: "-moz-background-clip: -moz-initial;-moz-background-origin: -moz-initial;-moz-background-inline-policy: -moz-initial;-moz-opacity:0.5;-khtml-opacity: 0.5"),
							CSS_RULES_NAME);
			app
					.getStyleSheet()
					.addRule(
							"div.Wt-dialog",
							"visibility: visible;position: fixed; left: 50%; top: 50%;margin-left: -100px; margin-top: -50px;");
			if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
				app
						.getStyleSheet()
						.addRule(
								"div.Wt-dialogcover",
								"position: absolute;left: expression((ignoreMe2 = document.documentElement.scrollLeft) + 'px' );top: expression((ignoreMe = document.documentElement.scrollTop) + 'px' );");
				app
						.getStyleSheet()
						.addRule(
								"div.Wt-dialog",
								"position: absolute;left: expression((ignoreMe2 = document.documentElement.scrollLeft + document.documentElement.clientWidth/2) + 'px' );top: expression((ignoreMe = document.documentElement.scrollTop + document.documentElement.clientHeight/2) + 'px' );");
			}
			app
					.getStyleSheet()
					.addRule("div.Wt-dialog",
							"border: 1px solid #888888;background: #EEEEEE none repeat scroll 0%;");
			app
					.getStyleSheet()
					.addRule("div.Wt-dialog .titlebar",
							"background: #888888; color: #FFFFFF;cursor: move;padding: 2px 6px 3px;");
			app.getStyleSheet().addRule("div.Wt-dialog .body",
					"background: #EEEEEE;padding: 4px 6px 4px;");
			app.getStyleSheet().addRule("div.Wt-msgbox-buttons button",
					"padding: 1px 4px 1px;margin: 2px;");
		}
		WContainerWidget parent = app.getDomRoot();
		this.setPopup(true);
		app
				.addAutoJavaScript("{var d="
						+ this.getJsRef()
						+ ";if (d && d.style.display != 'none' && !d.getAttribute('moved')) {var ws=Wt2_99_2.windowSize();d.style.left=Math.round((ws.x - d.clientWidth)/2) + 'px';d.style.top=Math.round((ws.y - d.clientHeight)/2) + 'px';d.style.marginLeft='0px';d.style.marginTop='0px';}}");
		parent.addWidget(this);
		WVBoxLayout layout = new WVBoxLayout();
		layout.setSpacing(0);
		layout.setContentsMargins(0, 0, 0, 0);
		this.titleBar_ = new WContainerWidget();
		this.titleBar_.setStyleClass("titlebar");
		this.caption_ = new WText(windowTitle, this.titleBar_);
		layout.addWidget(this.titleBar_);
		this.contents_ = new WContainerWidget();
		this.contents_.setStyleClass("body");
		layout.addWidget(this.contents_, 1);
		this.impl_.setLayout(layout, EnumSet.of(AlignmentFlag.AlignLeft));
		if (app.getEnvironment().agentIsIE()) {
			this.impl_.setOverflow(WContainerWidget.Overflow.OverflowVisible);
		}
		this.mouseDownJS_
				.setJavaScript("function(obj, event) {  var pc = Wt2_99_2.pageCoordinates(event);  obj.setAttribute('dsx', pc.x);  obj.setAttribute('dsy', pc.y);}");
		this.mouseMovedJS_
				.setJavaScript("function(obj, event) {var WT= Wt2_99_2;var lastx = obj.getAttribute('dsx');var lasty = obj.getAttribute('dsy');if (lastx != null && lastx != '') {nowxy = WT.pageCoordinates(event);var d = "
						+ this.getJsRef()
						+ ";d.setAttribute('moved', true);d.style.left = (WT.pxself(d, 'left')+nowxy.x-lastx) + 'px';d.style.top = (WT.pxself(d, 'top')+nowxy.y-lasty) + 'px';obj.setAttribute('dsx', nowxy.x);obj.setAttribute('dsy', nowxy.y);}}");
		this.mouseUpJS_
				.setJavaScript("function(obj, event) {obj.removeAttribute('dsx');}");
		this.titleBar_.mouseWentDown().addListener(this.mouseDownJS_);
		this.titleBar_.mouseMoved().addListener(this.mouseMovedJS_);
		this.titleBar_.mouseWentUp().addListener(this.mouseUpJS_);
		this.hide();
	}

	/**
	 * Construct a WDialog with a given window title.
	 * <p>
	 * Calls {@link #WDialog(CharSequence windowTitle) this(new WString())}
	 */
	public WDialog() {
		this(new WString());
	}

	/**
	 * Destruct a WDialog.
	 */
	public void remove() {
		this.hide();
		super.remove();
	}

	/**
	 * Set the dialog window title.
	 * <p>
	 * The window title is displayed in the title bar.
	 * <p>
	 * 
	 * @see WDialog#setTitleBarEnabled(boolean enable)
	 */
	public void setWindowTitle(CharSequence windowTitle) {
		this.caption_.setText(windowTitle);
	}

	/**
	 * Get the dialog window title.
	 * <p>
	 * 
	 * @see WDialog#setWindowTitle(CharSequence windowTitle)
	 */
	public WString getWindowTitle() {
		return this.caption_.getText();
	}

	/**
	 * Enable or disable the title bar.
	 * <p>
	 * The titlebar is enabled by default.
	 */
	public void setTitleBarEnabled(boolean enable) {
		this.titleBar_.setHidden(!enable);
	}

	/**
	 * Returns whether the title bar is enabled.
	 * <p>
	 * 
	 * @see WDialog#setTitleBarEnabled(boolean enable)
	 */
	public boolean isTitleBarEnabled() {
		return !this.titleBar_.isHidden();
	}

	/**
	 * Get the dialog contents container.
	 * <p>
	 * Content to the dialog window may be added to this container widget.
	 */
	public WContainerWidget getContents() {
		return this.contents_;
	}

	// public WDialog.DialogCode getExec() ;
	/**
	 * Stop the dialog.
	 * <p>
	 * Sets the dialog result, and emits the {@link WDialog#finished()} signal.
	 * <p>
	 * 
	 * @see WDialog#finished()
	 * @see WDialog#getResult()
	 */
	public void done(WDialog.DialogCode result) {
		this.result_ = result;
		if (this.recursiveEventLoop_) {
			WebSession session = WApplication.getInstance().getSession();
			this.recursiveEventLoop_ = false;
			session.unlockRecursiveEventLoop();
		} else {
			this.hide();
		}
		this.finished_.trigger(result);
	}

	/**
	 * Close the dialog, with result is Accepted.
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 * @see WDialog#reject()
	 */
	public void accept() {
		this.done(WDialog.DialogCode.Accepted);
	}

	/**
	 * Close the dialog, with result is Rejected.
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 * @see WDialog#accept()
	 */
	public void reject() {
		this.done(WDialog.DialogCode.Rejected);
	}

	/**
	 * Signal emitted when the dialog is closed.
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 * @see WDialog#accept()
	 * @see WDialog#reject()
	 */
	public Signal1<WDialog.DialogCode> finished() {
		return this.finished_;
	}

	/**
	 * Get the result that was set for this dialog.
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 */
	public WDialog.DialogCode getResult() {
		return this.result_;
	}

	/**
	 * Set whether the dialog is modal.
	 * <p>
	 * A modal dialog will block the underlying user interface.
	 * <p>
	 * By default a dialog is modal.
	 */
	public void setModal(boolean modal) {
		this.modal_ = modal;
	}

	/**
	 * Returns whether the dialog is modal.
	 * <p>
	 * 
	 * @see WDialog#setModal(boolean modal)
	 */
	public boolean isModal() {
		return this.modal_;
	}

	public void setHidden(boolean hidden) {
		WApplication app = WApplication.getInstance();
		if (this.modal_) {
			WContainerWidget cover = app.getDialogCover(!hidden);
			if (cover != null) {
				cover.setHidden(hidden);
				if (!hidden) {
					cover.setAttributeValue("style", "z-index:"
							+ String.valueOf(this.impl_.getZIndex() - 1));
				}
			}
			app.exposeOnly(hidden ? null : this);
		}
		super.setHidden(hidden);
	}

	public void resize(WLength width, WLength height) {
		this.impl_.setLayout(this.impl_.getLayout());
		super.resize(width, height);
	}

	private WContainerWidget impl_;
	private WText caption_;
	private WContainerWidget titleBar_;
	private WContainerWidget contents_;
	private boolean modal_;
	private Signal1<WDialog.DialogCode> finished_;
	private WDialog.DialogCode result_;
	private boolean recursiveEventLoop_;
	private JSlot mouseDownJS_;
	private JSlot mouseMovedJS_;
	private JSlot mouseUpJS_;
}
