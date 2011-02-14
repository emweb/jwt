/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

/**
 * A WDialog shows a dialog.
 * <p>
 * 
 * By default, the dialog is <i>modal</i>. A modal window blocks the user
 * interface, and does not allow the user to interact with any other part of the
 * user interface until the dialog is closed (this is enforced at the server
 * side, so you may rely on this behavior).
 * <p>
 * There are two distinct ways for using a WDialog window.
 * <p>
 * A WDialog can be used as any other widget. In this case, the WDialog is
 * simply instantiated as another widget. The dialog may be closed by calling
 * {@link WDialog#accept() accept()}, {@link WDialog#reject() reject()} or
 * {@link WDialog#done(WDialog.DialogCode result) done()} (or connecting a
 * signal to one of these methods). This will hide the dialog and emit the
 * {@link WDialog#finished() finished()} signal, which you then can listen for
 * to process the dialog result and delete the dialog. Unlike other widgets, a
 * dialog does not need to be added to a parent widget, but is hidden by
 * default. You must use the method {@link WWidget#show() WWidget#show()} or
 * setHidden(true) to show the dialog.
 * <p>
 * The easiest way to display a modal dialog is using {@link WDialog#exec()
 * exec()}: after creating a WDialog window, a call to {@link WDialog#exec()
 * exec()} will block (suspend the thread) until the dialog window is closed,
 * and return the dialog result. Typically, an OK button will be connected to
 * {@link WDialog#accept() accept()}, and in some cases a Cancel button to
 * {@link WDialog#reject() reject()}. This solution has the drawback that it is
 * not scalable to many concurrent sessions, since for every session with a
 * recursive event loop (which is running durring the {@link WDialog#exec()
 * exec()} method), a thread is locked. In practical terms, this means it is
 * only suitable for software with restricted access or deployed on an intranet
 * or extranet.
 * <p>
 * This functionality is only available on Servlet 3.0 compatible servlet
 * containers.
 * <p>
 * Use setModal(false) to create a non-modal dialog. A non-modal dialog does not
 * block the underlying user interface: the user must not first deal with the
 * dialog before interacting with the rest of the user interface.
 * <p>
 * Contents for the dialog is defined by adding it to the
 * {@link WDialog#getContents() getContents()} widget.
 * <p>
 * This dialog looks like this (using the standard look):
 * <p>
 * <table border="0" align="center" cellspacing="3" cellpadding="3">
 * <tr>
 * <td><div align="center"> <img src="doc-files//WDialog-default-1.png"
 * alt="A simple custom dialog (default)">
 * <p>
 * <strong>A simple custom dialog (default)</strong>
 * </p>
 * </div></td>
 * <td><div align="center"> <img src="doc-files//WDialog-polished-1.png"
 * alt="A simple custom dialog (polished)">
 * <p>
 * <strong>A simple custom dialog (polished)</strong>
 * </p>
 * </div></td>
 * </tr>
 * </table>
 * <p>
 * <h3>CSS</h3>
 * <p>
 * A dialog has the <code>Wt-dialog</code> and <code>Wt-outset</code> style
 * classes. The look can be overridden using the following style class
 * selectors:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-dialog .titlebar : The title bar
 *  .Wt-dialog .body     : The body (requires vertical padding 4px).
 * </pre>
 * 
 * </div>
 * <p>
 * <p>
 * <i><b>Note: </b>For the dialog (or rather, the silkscreen covering the user
 * interface below) to render properly in IE, the &quot;html body&quot; margin
 * is set to 0 (if it wasn&apos;t already). </i>
 * </p>
 */
public class WDialog extends WCompositeWidget {
	/**
	 * The result of a modal dialog execution.
	 */
	public enum DialogCode {
		/**
		 * Dialog closed with {@link WDialog#reject() reject()}.
		 */
		Rejected,
		/**
		 * Dialog closed with {@link WDialog#accept() accept()}.
		 */
		Accepted;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Constructs a WDialog with a given window title.
	 * <p>
	 * Only a single Dialog may be constructed at any time. Unlike other
	 * widgets, a dialog does not need to be added to a container widget.
	 */
	public WDialog(CharSequence windowTitle) {
		super();
		this.modal_ = true;
		this.finished_ = new Signal1<WDialog.DialogCode>(this);
		this.recursiveEventLoop_ = false;
		String TEMPLATE = "${shadow-x1-x2}${titlebar}${contents}";
		this
				.setImplementation(this.impl_ = new WTemplate(new WString(
						TEMPLATE)));
		String CSS_RULES_NAME = "Wt::WDialog";
		WApplication app = WApplication.getInstance();
		this
				.setPositionScheme(app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6 ? PositionScheme.Absolute
						: PositionScheme.Fixed);
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			if (app.getEnvironment().agentIsIElt(9)) {
				app.getStyleSheet().addRule("body", "height: 100%;");
			}
			app
					.getStyleSheet()
					.addRule(
							"div.Wt-dialogcover",
							""
									+ "height: 100%; width: 100%;top: 0px; left: 0px;opacity: 0.5; position: fixed;"
									+ (app.getEnvironment().agentIsIElt(9) ? "filter: alpha(opacity=50);"
											: "opacity: 0.5"), CSS_RULES_NAME);
			String position = app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6 ? "absolute"
					: "fixed";
			app
					.getStyleSheet()
					.addRule(
							"div.Wt-dialog",
							""
									+ (app.getEnvironment().hasAjax()
											&& !app.getEnvironment()
													.agentIsIElt(9) ? "visibility: hidden;"
											: "")
									+ "position: "
									+ position
									+ ';'
									+ (!app.getEnvironment().hasAjax() ? "left: 50%; top: 50%;margin-left: -100px; margin-top: -50px;"
											: "left: 0px; top: 0px;"));
			if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
				app
						.getStyleSheet()
						.addRule(
								"div.Wt-dialogcover",
								"position: absolute;left: expression((ignoreMe2 = document.documentElement.scrollLeft) + 'px' );top: expression((ignoreMe = document.documentElement.scrollTop) + 'px' );");
				if (!app.getEnvironment().hasAjax()) {
					app
							.getStyleSheet()
							.addRule(
									"div.Wt-dialog",
									"position: absolute;left: expression((ignoreMe2 = document.documentElement.scrollLeft + document.documentElement.clientWidth/2) + 'px' );top: expression((ignoreMe = document.documentElement.scrollTop + document.documentElement.clientHeight/2) + 'px' );");
				}
			}
		}
		this.impl_.setStyleClass("Wt-dialog Wt-outset");
		WContainerWidget parent = app.getDomRoot();
		this.setPopup(true);
		String THIS_JS = "js/WDialog.js";
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
		this.setJavaScriptMember("_a", "0;new Wt3_1_8.WDialog("
				+ app.getJavaScriptClass() + "," + this.getJsRef() + ")");
		app.addAutoJavaScript("{var obj = $('#" + this.getId()
				+ "').data('obj');if (obj) obj.centerDialog();}");
		parent.addWidget(this);
		this.titleBar_ = new WContainerWidget();
		this.titleBar_.setStyleClass("titlebar");
		this.caption_ = new WText(windowTitle, this.titleBar_);
		this.impl_.bindString("shadow-x1-x2", WTemplate.DropShadow_x1_x2);
		this.impl_.bindWidget("titlebar", this.titleBar_);
		this.contents_ = new WContainerWidget();
		this.contents_.setStyleClass("body");
		this.impl_.bindWidget("contents", this.contents_);
		this.saveCoverState(app, app.getDialogCover());
		this.setJavaScriptMember(WT_RESIZE_JS, "$('#" + this.getId()
				+ "').data('obj').wtResize");
		this.hide();
	}

	/**
	 * Constructs a WDialog with a given window title.
	 * <p>
	 * Calls {@link #WDialog(CharSequence windowTitle) this(new WString())}
	 */
	public WDialog() {
		this(new WString());
	}

	/**
	 * Destructs a WDialog.
	 */
	public void remove() {
		this.hide();
		super.remove();
	}

	/**
	 * Sets the dialog window title.
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
	 * Returns the dialog window title.
	 * <p>
	 * 
	 * @see WDialog#setWindowTitle(CharSequence windowTitle)
	 */
	public WString getWindowTitle() {
		return this.caption_.getText();
	}

	/**
	 * Enables or disables the title bar.
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
	 * Returns the dialog title bar container.
	 * <p>
	 * The title bar contains a single text that contains the caption. You may
	 * customize the title bar by for example adding other content.
	 */
	public WContainerWidget getTitleBar() {
		return this.titleBar_;
	}

	/**
	 * Returns the dialog contents container.
	 * <p>
	 * Content to the dialog window may be added to this container widget.
	 */
	public WContainerWidget getContents() {
		return this.contents_;
	}

	/**
	 * Executes the dialog in a recursive event loop.
	 * <p>
	 * Executes the dialog. This blocks the current thread of execution until
	 * one of {@link WDialog#done(WDialog.DialogCode result) done()},
	 * {@link WDialog#accept() accept()} or {@link WDialog#reject() reject()} is
	 * called.
	 * <p>
	 * <i>Warning: using {@link WDialog#exec() exec()} does not scale to many
	 * concurrent sessions, since the thread is locked.</i>
	 * <p>
	 * <i>This functionality is only available on Servlet 3.0 compatible servlet
	 * containers.</i>
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 * @see WDialog#accept()
	 * @see WDialog#reject()
	 */
	public WDialog.DialogCode exec() {
		if (this.recursiveEventLoop_) {
			throw new WtException(
					"WDialog::exec(): already in recursive event loop.");
		}
		this.show();
		if (!WtServlet.isAsyncSupported()) {
			throw new WtException(
					"Server push requires a Servlet 3.0 enabled servlet container and an application with async-supported enabled.");
		}
		this.recursiveEventLoop_ = true;
		do {
			WApplication.getInstance().getSession().doRecursiveEventLoop();
		} while (this.recursiveEventLoop_);
		this.hide();
		return this.result_;
	}

	/**
	 * Stops the dialog.
	 * <p>
	 * Sets the dialog result, and emits the {@link WDialog#finished()
	 * finished()} signal.
	 * <p>
	 * 
	 * @see WDialog#finished()
	 * @see WDialog#getResult()
	 */
	public void done(WDialog.DialogCode result) {
		this.result_ = result;
		if (this.recursiveEventLoop_) {
			this.recursiveEventLoop_ = false;
		} else {
			this.hide();
		}
		this.finished_.trigger(result);
	}

	/**
	 * Closes the dialog, with result is Accepted.
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 * @see WDialog#reject()
	 */
	public void accept() {
		this.done(WDialog.DialogCode.Accepted);
	}

	/**
	 * Closes the dialog, with result is Rejected.
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 * @see WDialog#accept()
	 */
	public void reject() {
		this.done(WDialog.DialogCode.Rejected);
	}

	/**
	 * Lets pressing the escape key reject the dialog.
	 * <p>
	 * Before JWt 3.1.5, pressing escape automatically rejected the dialog.
	 * Since 3.1.4 this behaviour is no longer the default since it may
	 * interfere with other functionality in the dialog. Use this method to
	 * enable this behaviour.
	 * <p>
	 * 
	 * @see WDialog#reject()
	 */
	public void rejectWhenEscapePressed() {
		WApplication.getInstance().globalEscapePressed().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WDialog.this.reject();
					}
				});
		this.impl_.escapePressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WDialog.this.reject();
			}
		});
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
	 * Returns the result that was set for this dialog.
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 */
	public WDialog.DialogCode getResult() {
		return this.result_;
	}

	/**
	 * Sets whether the dialog is modal.
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
		if (this.isHidden() != hidden) {
			if (this.modal_) {
				WApplication app = WApplication.getInstance();
				WContainerWidget cover = app.getDialogCover();
				if (!(cover != null)) {
					return;
				}
				if (!hidden) {
					this.saveCoverState(app, cover);
					cover.show();
					cover.setZIndex(this.impl_.getZIndex() - 1);
					app.constrainExposed(this);
					app
							.doJavaScript("try {if (document.activeElement && document.activeElement.blur)document.activeElement.blur();} catch (e) { }");
				} else {
					this.restoreCoverState(app, cover);
				}
			}
		}
		super.setHidden(hidden);
	}

	private WTemplate impl_;
	private WText caption_;
	private WContainerWidget titleBar_;
	private WContainerWidget contents_;
	private boolean modal_;
	private WWidget previousExposeConstraint_;
	private int coverPreviousZIndex_;
	private boolean coverWasHidden_;
	private Signal1<WDialog.DialogCode> finished_;
	private WDialog.DialogCode result_;
	private boolean recursiveEventLoop_;

	private void saveCoverState(WApplication app, WContainerWidget cover) {
		this.coverWasHidden_ = cover.isHidden();
		this.coverPreviousZIndex_ = cover.getZIndex();
		this.previousExposeConstraint_ = app.getExposeConstraint();
	}

	private void restoreCoverState(WApplication app, WContainerWidget cover) {
		cover.setHidden(this.coverWasHidden_);
		cover.setZIndex(this.coverPreviousZIndex_);
		app.constrainExposed(this.previousExposeConstraint_);
	}

	static String wtjs1(WApplication app) {
		String s = "function(g,b){function k(a){var c=a||window.event;a=d.pageCoordinates(c);c=d.windowCoordinates(c);var e=d.windowSize();if(c.x>0&&c.x<e.x&&c.y>0&&c.y<e.y){h=true;b.style.left=d.pxself(b,\"left\")+a.x-i+\"px\";b.style.top=d.pxself(b,\"top\")+a.y-j+\"px\";i=a.x;j=a.y}}jQuery.data(b,\"obj\",this);var l=this,f=$(b).find(\".titlebar\").first().get(0),d=g.WT,i,j,h=false;if(b.style.left!=\"\"||b.style.top!=\"\")h=true;if(f){f.onmousedown=function(a){a=a||window.event;d.capture(f);a= d.pageCoordinates(a);i=a.x;j=a.y;f.onmousemove=k};f.onmouseup=function(){f.onmousemove=null;d.capture(null)}}this.centerDialog=function(){if(b.parentNode==null){b=f=null;this.centerDialog=function(){}}else if(b.style.display!=\"none\"&&b.style.visibility!=\"hidden\"){if(!h){var a=d.windowSize(),c=b.offsetHeight;b.style.left=Math.round((a.x-b.offsetWidth)/2+(d.isIE6?document.documentElement.scrollLeft:0))+\"px\";b.style.top=Math.round((a.y-c)/2+(d.isIE6?document.documentElement.scrollTop:0))+\"px\";b.style.marginLeft= \"\";b.style.marginTop=\"\";b.style.height!=\"\"&&l.wtResize(b,-1,c)}b.style.visibility=\"visible\"}};this.wtResize=function(a,c,e){e-=2;c-=2;a.style.height=e+\"px\";if(c>0)a.style.width=c+\"px\";a=a.lastChild;e-=a.previousSibling.offsetHeight+8;if(e>0){a.style.height=e+\"px\";g.layouts&&g.layouts.adjust()}}}";
		if ("ctor.WDialog".indexOf(".prototype") != -1) {
			return "Wt3_1_8.ctor.WDialog = " + s + ";";
		} else {
			if ("ctor.WDialog".substring(0, 5).compareTo(
					"ctor.".substring(0, 5)) == 0) {
				return "Wt3_1_8." + "ctor.WDialog".substring(5) + " = " + s
						+ ";";
			} else {
				return "Wt3_1_8.ctor.WDialog = function() { (" + s
						+ ").apply(Wt3_1_8, arguments) };";
			}
		}
	}
}
