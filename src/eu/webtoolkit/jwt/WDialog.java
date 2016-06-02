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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WDialog shows a dialog.
 * <p>
 * 
 * By default, the dialog is <i>modal</i>. A modal window blocks the user
 * interface, and does not allow the user to interact with any other part of the
 * user interface until the dialog is closed (this is enforced at the server
 * side, so you may rely on this behavior).
 * <p>
 * A modal dialog can be instantiated synchronously or asynchronously. A
 * non-modal dialog can only be instantiated asynchronously.
 * <p>
 * When using a dialog asynchronously, there is no API call that waits for the
 * dialog to be closed. Then, the usage is similar to instantiating any other
 * widget. The dialog may be closed by calling {@link WDialog#accept() accept()}, {@link WDialog#reject() reject()} or
 * {@link WDialog#done(WDialog.DialogCode result) done()} (or connecting a
 * signal to one of these methods). This will hide the dialog and emit the
 * {@link WDialog#finished() finished()} signal, which you then can listen for
 * to process the dialog result and delete the dialog. Unlike other widgets, a
 * dialog does not need to be added to a parent widget, but is hidden by
 * default. You must use the method {@link WWidget#show() WWidget#show()} or
 * {@link WDialog#setHidden(boolean hidden, WAnimation animation)
 * setHidden(false)} to show the dialog.
 * <p>
 * The synchronous use of a dialog involves a call to
 * {@link WDialog#exec(WAnimation animation) exec()} which will block (suspend
 * the thread) until the dialog window is closed, and return the dialog result.
 * Events within dialog are handled using a so-called recursive event loop.
 * Typically, an OK button will be connected to {@link WDialog#accept()
 * accept()}, and in some cases a Cancel button to {@link WDialog#reject()
 * reject()}. This solution has the drawback that it is not scalable to many
 * concurrent sessions, since for every session with a recursive event loop, a
 * thread is locked until {@link WDialog#exec(WAnimation animation) exec()}
 * returns. A thread that is locked by a recursive event loop cannot be used to
 * process requests from another sessions. When all threads in the threadpool
 * are locked in recursive event loops, the server will be unresponsive to
 * requests from any other session. In practical terms, this means you must not
 * use {@link WDialog#exec(WAnimation animation) exec()}, unless your
 * application will never be used by more concurrent users than the amount of
 * threads in your threadpool (like on some intranets or extranets). Using
 * {@link WDialog#exec(WAnimation animation) exec()} is not supported from
 * outside the regular event loop (i.e. when taking a lock on a session using
 * {@link WApplication#getUpdateLock() WApplication#getUpdateLock()} or by
 * posting an event using WServer::post()). This functionality is only available
 * on Servlet 3.0 compatible servlet containers.
 * <p>
 * Use {@link WDialog#setModal(boolean modal) setModal(false)} to create a
 * non-modal dialog. A non-modal dialog does not block the underlying user
 * interface: the user must not first deal with the dialog before interacting
 * with the rest of the user interface.
 * <p>
 * Contents for the dialog is defined by adding it to the
 * {@link WDialog#getContents() getContents()} widget.
 * <p>
 * This dialog looks like this (using the default css themes):
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
 * <p>
 * <i><b>Note: </b>For the dialog (or rather, the silkscreen covering the user
 * interface below) to render properly in IE, the &quot;html body&quot; margin
 * is set to 0 (if it wasn&apos;t already). </i>
 * </p>
 */
public class WDialog extends WPopupWidget {
	private static Logger logger = LoggerFactory.getLogger(WDialog.class);

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
	 * Constructs a new dialog.
	 * <p>
	 * Unlike other widgets, the dialog does not require a parent container
	 * since it is a top-level widget. You may however still provide a parent
	 * object to let the dialog be deleted together with its parent.
	 */
	public WDialog(WObject parent) {
		super(new WTemplate(tr("Wt.WDialog.template")), parent);
		this.moved_ = new JSignal2<Integer, Integer>(this, "moved") {
		};
		this.resized_ = new JSignal2<Integer, Integer>(this, "resized") {
		};
		this.finished_ = new Signal1<WDialog.DialogCode>(this);
		this.escapeConnection1_ = new AbstractSignal.Connection();
		this.escapeConnection2_ = new AbstractSignal.Connection();
		this.enterConnection1_ = new AbstractSignal.Connection();
		this.enterConnection2_ = new AbstractSignal.Connection();
		this.create();
	}

	/**
	 * Constructs a new dialog.
	 * <p>
	 * Calls {@link #WDialog(WObject parent) this((WObject)null)}
	 */
	public WDialog() {
		this((WObject) null);
	}

	/**
	 * Constructs a dialog with a given window title.
	 * <p>
	 * Unlike other widgets, the dialog does not require a parent container
	 * since it is a top-level widget. You may however still provide a parent
	 * object to let the dialog be deleted together with its parent.
	 */
	public WDialog(final CharSequence windowTitle, WObject parent) {
		super(new WTemplate(tr("Wt.WDialog.template")), parent);
		this.moved_ = new JSignal2<Integer, Integer>(this, "moved") {
		};
		this.resized_ = new JSignal2<Integer, Integer>(this, "resized") {
		};
		this.finished_ = new Signal1<WDialog.DialogCode>(this);
		this.escapeConnection1_ = new AbstractSignal.Connection();
		this.escapeConnection2_ = new AbstractSignal.Connection();
		this.enterConnection1_ = new AbstractSignal.Connection();
		this.enterConnection2_ = new AbstractSignal.Connection();
		this.create();
		this.setWindowTitle(windowTitle);
	}

	/**
	 * Constructs a dialog with a given window title.
	 * <p>
	 * Calls {@link #WDialog(CharSequence windowTitle, WObject parent)
	 * this(windowTitle, (WObject)null)}
	 */
	public WDialog(final CharSequence windowTitle) {
		this(windowTitle, (WObject) null);
	}

	/**
	 * Deletes a dialog.
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
	public void setWindowTitle(final CharSequence windowTitle) {
		this.caption_.setText(new WString("<h4>"
				+ Utils.htmlEncode(windowTitle.toString()) + "</h4>"));
	}

	/**
	 * Returns the dialog window title.
	 * <p>
	 * 
	 * @see WDialog#setWindowTitle(CharSequence windowTitle)
	 */
	public WString getWindowTitle() {
		String text = this.caption_.getText().toString();
		if (text.length() > 9) {
			return new WString(text.substring(4, 4 + text.length() - 9));
		} else {
			return WString.Empty;
		}
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
	 * Returns the dialog footer container.
	 * <p>
	 * This is an optional section which is typically used for buttons.
	 */
	public WContainerWidget getFooter() {
		if (!(this.footer_ != null)) {
			this.footer_ = new WContainerWidget();
			WApplication
					.getInstance()
					.getTheme()
					.apply(this, this.footer_, WidgetThemeRole.DialogFooterRole);
			WContainerWidget layoutContainer = (WContainerWidget) this.impl_
					.resolveWidget("layout");
			layoutContainer.getLayout().addWidget(this.footer_);
		}
		return this.footer_;
	}

	/**
	 * Executes the dialog in a recursive event loop.
	 * <p>
	 * Executes the dialog synchronously. This blocks the current thread of
	 * execution until one of {@link WDialog#done(WDialog.DialogCode result)
	 * done()}, {@link WDialog#accept() accept()} or {@link WDialog#reject()
	 * reject()} is called.
	 * <p>
	 * <i>Warning: using {@link WDialog#exec(WAnimation animation) exec()} does
	 * not scale to many concurrent sessions, since the thread is locked until
	 * exec returns, so the entire server will be unresponsive when the thread
	 * pool is exhausted.</i>
	 * <p>
	 * <i>This functionality is only available on Servlet 3.0 compatible servlet
	 * containers.</i>
	 * <p>
	 * 
	 * @see WDialog#done(WDialog.DialogCode result)
	 * @see WDialog#accept()
	 * @see WDialog#reject()
	 */
	public WDialog.DialogCode exec(final WAnimation animation) {
		if (this.recursiveEventLoop_) {
			throw new WException("WDialog::exec(): already being executed.");
		}
		this.animateShow(animation);
		if (!WtServlet.isAsyncSupported()) {
			throw new WException(
					"WDialog#exec() requires a Servlet 3.0 enabled servlet container and an application with async-supported enabled.");
		}
		WApplication app = WApplication.getInstance();
		this.recursiveEventLoop_ = true;
		if (app.getEnvironment().isTest()) {
			app.getEnvironment().dialogExecuted().trigger(this);
			if (this.recursiveEventLoop_) {
				throw new WException("Test case must close dialog");
			}
		} else {
			do {
				app.waitForEvent();
			} while (this.recursiveEventLoop_);
		}
		this.hide();
		return this.result_;
	}

	/**
	 * Executes the dialog in a recursive event loop.
	 * <p>
	 * Returns {@link #exec(WAnimation animation) exec(new WAnimation())}
	 */
	public final WDialog.DialogCode exec() {
		return exec(new WAnimation());
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
		if (this.isHidden()) {
			return;
		}
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
	public void rejectWhenEscapePressed(boolean enable) {
		this.escapeIsReject_ = enable;
	}

	/**
	 * Lets pressing the escape key reject the dialog.
	 * <p>
	 * Calls {@link #rejectWhenEscapePressed(boolean enable)
	 * rejectWhenEscapePressed(true)}
	 */
	public final void rejectWhenEscapePressed() {
		rejectWhenEscapePressed(true);
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
	 * A modal dialog will block the underlying user interface. A modal dialog
	 * can be shown synchronously or asynchronously. A non-modal dialog can only
	 * be shown asynchronously.
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

	/**
	 * Adds a resize handle to the dialog.
	 * <p>
	 * The resize handle is shown in the bottom right corner of the dialog, and
	 * allows the user to resize the dialog (but not smaller than the content
	 * allows).
	 * <p>
	 * This also sets the minimum width and height to {@link WLength#Auto} to
	 * use the initial width and height as minimum sizes. You may want to
	 * provide other values for minimum width and height to allow the dialog to
	 * be reduced in size.
	 * <p>
	 * 
	 * @see WDialog#setMinimumSize(WLength width, WLength height)
	 * @see WDialog#setMaximumSize(WLength width, WLength height)
	 */
	public void setResizable(boolean resizable) {
		if (resizable != this.resizable_) {
			this.resizable_ = resizable;
			this.toggleStyleClass("Wt-resizable", resizable);
			this.setSelectable(!resizable);
			if (resizable) {
				this.contents_.setSelectable(true);
			}
			if (this.resizable_) {
				Resizable.loadJavaScript(WApplication.getInstance());
				this.setJavaScriptMember(
						" Resizable",
						"(new Wt3_3_5.Resizable(Wt3_3_5,"
								+ this.getJsRef()
								+ ")).onresize(function(w, h, done) {var obj = $('#"
								+ this.getId()
								+ "').data('obj');if (obj) obj.onresize(w, h, done); });");
			}
		}
	}

	/**
	 * Returns whether the dialog has a resize handle.
	 * <p>
	 * 
	 * @see WDialog#setResizable(boolean resizable)
	 */
	public boolean isResizable() {
		return this.resizable_;
	}

	/**
	 * Adds a close button to the titlebar.
	 * <p>
	 * The close button is shown in the title bar. Clicking the close button
	 * will reject the dialog.
	 */
	public void setClosable(boolean closable) {
		if (closable) {
			if (!(this.closeIcon_ != null)) {
				this.closeIcon_ = new WText();
				this.titleBar_.insertWidget(0, this.closeIcon_);
				WApplication
						.getInstance()
						.getTheme()
						.apply(this, this.closeIcon_,
								WidgetThemeRole.DialogCloseIconRole);
				this.closeIcon_.clicked().addListener(this,
						new Signal1.Listener<WMouseEvent>() {
							public void trigger(WMouseEvent e1) {
								WDialog.this.reject();
							}
						});
			}
		} else {
			if (this.closeIcon_ != null)
				this.closeIcon_.remove();
			this.closeIcon_ = null;
		}
	}

	/**
	 * Returns whether the dialog can be closed.
	 */
	public boolean isClosable() {
		return this.closeIcon_ != null;
	}

	/**
	 * Set focus on the first widget in the dialog.
	 */
	public void setAutoFocus(boolean enable) {
		this.autoFocus_ = enable;
	}

	public void setHidden(boolean hidden, final WAnimation animation) {
		if (this.contents_ != null && this.isHidden() != hidden) {
			if (!hidden) {
				WApplication app = WApplication.getInstance();
				if (this.footer_ != null) {
					for (int i = 0; i < this.getFooter().getCount(); ++i) {
						WPushButton b = ((this.getFooter().getWidget(i)) instanceof WPushButton ? (WPushButton) (this
								.getFooter().getWidget(i)) : null);
						if (b != null && b.isDefault()) {
							this.enterConnection1_ = app.globalEnterPressed()
									.addListener(this, new Signal.Listener() {
										public void trigger() {
											WDialog.this.onDefaultPressed();
										}
									});
							this.enterConnection2_ = this.impl_.enterPressed()
									.addListener(this, new Signal.Listener() {
										public void trigger() {
											WDialog.this.onDefaultPressed();
										}
									});
							break;
						}
					}
				}
				if (this.escapeIsReject_) {
					this.escapeConnection1_ = this.escapePressed().addListener(
							this, new Signal.Listener() {
								public void trigger() {
									WDialog.this.onEscapePressed();
								}
							});
					this.escapeConnection2_ = this.impl_.escapePressed()
							.addListener(this, new Signal.Listener() {
								public void trigger() {
									WDialog.this.onEscapePressed();
								}
							});
				}
			} else {
				this.escapeConnection1_.disconnect();
				this.escapeConnection2_.disconnect();
				this.enterConnection1_.disconnect();
				this.enterConnection2_.disconnect();
			}
			DialogCover c = this.getCover();
			if (!hidden) {
				if (c != null) {
					c.pushDialog(this, animation);
				}
				if (this.modal_) {
					this.doJavaScript("try {var ae=document.activeElement;if (ae && ae.blur && ae.nodeName != 'BODY') {document.activeElement.blur();}} catch (e) { }");
				}
			} else {
				if (c != null) {
					c.popDialog(this, animation);
				}
			}
		}
		super.setHidden(hidden, animation);
	}

	public void positionAt(WWidget widget, Orientation orientation) {
		this.setPositionScheme(PositionScheme.Absolute);
		if (WApplication.getInstance().getEnvironment().hasJavaScript()) {
			this.setOffsets(new WLength(0), EnumSet.of(Side.Left, Side.Top));
		}
		super.positionAt(widget, orientation);
	}

	/**
	 * Set the position of the widget at the mouse position.
	 */
	public void positionAt(final WMouseEvent ev) {
		this.setPositionScheme(PositionScheme.Fixed);
		if (WApplication.getInstance().getEnvironment().hasJavaScript()) {
			this.setOffsets(new WLength(ev.getWindow().x),
					EnumSet.of(Side.Left));
			this.setOffsets(new WLength(ev.getWindow().y), EnumSet.of(Side.Top));
		}
	}

	/**
	 * Raises this dialog to be the top-most dialog.
	 */
	public void raiseToFront() {
		this.doJavaScript("jQuery.data(" + this.getJsRef()
				+ ", 'obj').bringToFront()");
		DialogCover c = this.getCover();
		c.bringToFront(this);
	}

	public void setMinimumSize(final WLength width, final WLength height) {
		super.setMinimumSize(width, height);
		this.impl_.resolveWidget("layout").setMinimumSize(width, height);
	}

	public void setMaximumSize(final WLength width, final WLength height) {
		super.setMaximumSize(width, height);
		WLength w = width.getUnit() != WLength.Unit.Percentage ? width
				: WLength.Auto;
		WLength h = height.getUnit() != WLength.Unit.Percentage ? height
				: WLength.Auto;
		this.impl_.resolveWidget("layout").setMaximumSize(w, h);
	}

	/**
	 * Signal emitted when the dialog is being resized by the user.
	 * <p>
	 * The information passed are the new width and height.
	 * <p>
	 * 
	 * @see WDialog#setResizable(boolean resizable)
	 */
	public JSignal2<Integer, Integer> resized() {
		return this.resized_;
	}

	/**
	 * Signal emitted when the dialog is being moved by the user.
	 * <p>
	 * The information passed are the new x and y position (relative to the
	 * wietdow).
	 */
	public JSignal2<Integer, Integer> moved() {
		return this.moved_;
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	public EventSignal1<WKeyEvent> keyWentDown() {
		return this.layoutContainer_.keyWentDown();
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	public EventSignal1<WKeyEvent> keyPressed() {
		return this.layoutContainer_.keyPressed();
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	public EventSignal1<WKeyEvent> keyWentUp() {
		return this.layoutContainer_.keyWentUp();
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	public EventSignal enterPressed() {
		return this.layoutContainer_.enterPressed();
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	public EventSignal escapePressed() {
		return this.layoutContainer_.escapePressed();
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			WApplication app = WApplication.getInstance();
			boolean centerX = this.getOffset(Side.Left).isAuto()
					&& this.getOffset(Side.Right).isAuto();
			boolean centerY = this.getOffset(Side.Top).isAuto()
					&& this.getOffset(Side.Bottom).isAuto();
			if (app.getEnvironment().hasAjax()) {
				if (this.getWidth().isAuto()) {
					if (this.getMaximumWidth().getUnit() == WLength.Unit.Percentage
							|| this.getMaximumWidth().toPixels() == 0) {
						this.impl_.resolveWidget("layout").setMaximumSize(
								new WLength(999999), this.getMaximumHeight());
					}
				}
			}
			this.doJavaScript("new Wt3_3_5.WDialog("
					+ app.getJavaScriptClass()
					+ ","
					+ this.getJsRef()
					+ ","
					+ this.titleBar_.getJsRef()
					+ ","
					+ (centerX ? "1" : "0")
					+ ","
					+ (centerY ? "1" : "0")
					+ ","
					+ (this.moved_.isConnected() ? '"' + this.moved_.getName() + '"'
							: "null")
					+ ","
					+ (this.resized_.isConnected() ? '"' + this.resized_
							.getName() + '"' : "null") + ");");
			if (!app.getEnvironment().agentIsIElt(9)) {
				String js = WString.tr("Wt.WDialog.CenterJS").toString();
				StringUtils.replace(js, "$el", "'" + this.getId() + "'");
				StringUtils.replace(js, "$centerX", centerX ? "1" : "0");
				StringUtils.replace(js, "$centerY", centerY ? "1" : "0");
				this.impl_.bindString("center-script",
						"<script>" + Utils.htmlEncode(js) + "</script>",
						TextFormat.XHTMLUnsafeText);
			} else {
				this.impl_.bindEmpty("center-script");
			}
		}
		if (!this.isModal()) {
			this.impl_.mouseWentDown().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WDialog.this.bringToFront(e1);
						}
					});
		}
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()
				&& this.autoFocus_) {
			this.impl_.isSetFirstFocus();
		}
		super.render(flags);
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private WTemplate impl_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private WText caption_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private WText closeIcon_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private WContainerWidget titleBar_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private WContainerWidget contents_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	WContainerWidget layoutContainer_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private WContainerWidget footer_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private boolean modal_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private boolean resizable_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private boolean escapeIsReject_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private boolean autoFocus_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private JSignal2<Integer, Integer> moved_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private JSignal2<Integer, Integer> resized_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private Signal1<WDialog.DialogCode> finished_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private WDialog.DialogCode result_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private boolean recursiveEventLoop_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private AbstractSignal.Connection escapeConnection1_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private AbstractSignal.Connection escapeConnection2_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private AbstractSignal.Connection enterConnection1_;
	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private AbstractSignal.Connection enterConnection2_;

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private void create() {
		this.closeIcon_ = null;
		this.footer_ = null;
		this.modal_ = true;
		this.resizable_ = false;
		this.recursiveEventLoop_ = false;
		this.escapeIsReject_ = false;
		this.autoFocus_ = true;
		this.impl_ = ((this.getImplementation()) instanceof WTemplate ? (WTemplate) (this
				.getImplementation()) : null);
		String CSS_RULES_NAME = "Wt::WDialog";
		WApplication app = WApplication.getInstance();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			if (app.getEnvironment().agentIsIElt(9)) {
				app.getStyleSheet().addRule("body", "height: 100%;");
			}
			String position = app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6 ? "absolute"
					: "fixed";
			app.getStyleSheet()
					.addRule(
							"div.Wt-dialog",
							""
									+ (app.getEnvironment().hasAjax() ? "visibility: hidden;"
											: "")
									+ (!app.getEnvironment().hasAjax() ? "left: 50%; top: 50%;margin-left: -100px; margin-top: -50px;"
											: "left: 0px; top: 0px;"),
							CSS_RULES_NAME);
			if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
				app.getStyleSheet()
						.addRule(
								"div.Wt-dialogcover",
								"position: absolute;left: expression((ignoreMe2 = document.documentElement.scrollLeft) + 'px' );top: expression((ignoreMe = document.documentElement.scrollTop) + 'px' );");
				if (!app.getEnvironment().hasAjax()) {
					app.getStyleSheet()
							.addRule(
									"div.Wt-dialog",
									"position: absolute;left: expression((ignoreMe2 = document.documentElement.scrollLeft + document.documentElement.clientWidth/2) + 'px' );top: expression((ignoreMe = document.documentElement.scrollTop + document.documentElement.clientHeight/2) + 'px' );");
				}
			}
		}
		app.loadJavaScript("js/WDialog.js", wtjs1());
		this.layoutContainer_ = new WContainerWidget();
		this.layoutContainer_.setGlobalUnfocused(true);
		WApplication
				.getInstance()
				.getTheme()
				.apply(this, this.layoutContainer_,
						WidgetThemeRole.DialogContent);
		this.layoutContainer_.addStyleClass("dialog-layout");
		WVBoxLayout layout = new WVBoxLayout(this.layoutContainer_);
		layout.setContentsMargins(0, 0, 0, 0);
		layout.setSpacing(0);
		this.impl_.bindWidget("layout", this.layoutContainer_);
		this.titleBar_ = new WContainerWidget();
		app.getTheme().apply(this, this.titleBar_,
				WidgetThemeRole.DialogTitleBarRole);
		this.caption_ = new WText(this.titleBar_);
		this.caption_.setInline(false);
		this.contents_ = new WContainerWidget();
		app.getTheme().apply(this, this.contents_,
				WidgetThemeRole.DialogBodyRole);
		layout.addWidget(this.titleBar_);
		layout.addWidget(this.contents_, 1);
		if (app.getEnvironment().hasAjax()) {
			this.setAttributeValue("style", "visibility: hidden");
			this.impl_.setMargin(new WLength(0), Side.All);
			if (!app.getEnvironment().agentIsIElt(9)) {
				this.setPositionScheme(PositionScheme.Fixed);
			}
		} else {
			this.setPositionScheme(app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6 ? PositionScheme.Absolute
					: PositionScheme.Fixed);
		}
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private void onEscapePressed() {
		DialogCover c = this.getCover();
		if (c != null && c.isTopDialogRendered(this)) {
			this.reject();
		}
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private void onDefaultPressed() {
		DialogCover c = this.getCover();
		if (this.footer_ != null && c != null && c.isTopDialogRendered(this)) {
			for (int i = 0; i < this.getFooter().getCount(); ++i) {
				WPushButton b = ((this.getFooter().getWidget(i)) instanceof WPushButton ? (WPushButton) (this
						.getFooter().getWidget(i)) : null);
				if (b != null && b.isDefault()) {
					if (b.isEnabled()) {
						b.clicked().trigger(new WMouseEvent());
					}
					break;
				}
			}
		}
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private void bringToFront(final WMouseEvent e) {
		if (e.getButton() == WMouseEvent.Button.LeftButton
				&& e.getModifiers().equals(KeyboardModifier.NoModifier)) {
			this.raiseToFront();
		}
	}

	/**
	 * Event signal emitted when a keyboard key is pushed down.
	 * <p>
	 * The event will be triggered if nothing in the {@link WDialog} has focus
	 * <p>
	 */
	private DialogCover getCover() {
		WApplication app = WApplication.getInstance();
		if (app.getDomRoot() != null) {
			WWidget w = app.findWidget("dialog-cover");
			if (w != null) {
				return ((w) instanceof DialogCover ? (DialogCover) (w) : null);
			} else {
				return new DialogCover();
			}
		} else {
			return null;
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WDialog",
				"function(i,a,g,m,n,w,x){function y(){if(w){var b=c.pxself(a,\"left\"),d=c.pxself(a,\"top\");if(b!=s||d!=t){s=b;t=d;i.emit(a,w,s,t)}}}function z(b,d){if(!A)if(b!=u||d!=v){u=b;v=d;x&&i.emit(a,x,u,v)}}function D(b){var d=b||window.event;b=c.pageCoordinates(d);d=c.windowCoordinates(d);var e=c.windowSize();if(d.x>0&&d.x<e.x&&d.y>0&&d.y<e.y){m=n=false;if(a.style.right===\"auto\"||a.style.right===\"\"){a.style.left=c.px(a,\"left\")+b.x-o+\"px\";a.style.right=\"\"}else{a.style.right= c.px(a,\"right\")+o-b.x+\"px\";a.style.left=\"auto\"}if(a.style.bottom===\"auto\"||a.style.bottom===\"\"){a.style.top=c.px(a,\"top\")+b.y-p+\"px\";a.style.bottom=\"\"}else{a.style.bottom=c.px(a,\"bottom\")+p-b.y+\"px\";a.style.top=\"auto\"}o=b.x;p=b.y}}function E(b,d,e){if(a.style.position==\"\")a.style.position=c.isIE6?\"absolute\":\"fixed\";a.style.visibility=\"visible\";c.windowSize();j=c.parsePct(a.style.height,j);k=c.parsePct(a.style.width,k);a.style.height=Math.max(0,e)+\"px\";a.style.width=Math.max(0,d)+\"px\";z(d,e);h.centerDialog(); b=k!=-1;var l=j!=-1;if(b&&l){q=B();r=C();h.onresize(q,r,true)}else if(b){q=B();h.onresize(q,e,true)}else if(l){r=C();h.onresize(d,r,true)}}function B(){return c.windowSize().x*k/100}function C(){return c.windowSize().y*j/100}function F(b,d,e){if(d>0)f.style.width=d+c.parsePx($(f).css(\"borderLeftWidth\"))+c.parsePx($(f).css(\"borderRightWidth\"))+\"px\";if(e>0)f.style.height=e+c.parsePx($(f).css(\"borderTopWidth\"))+c.parsePx($(f).css(\"borderBottomWidth\"))+\"px\";h.centerDialog();a.wtResize&&a.wtResize(a,d, e)}function G(){i.layouts2.adjust()}jQuery.data(a,\"obj\",this);var h=this,f=$(a).find(\".dialog-layout\").get(0),c=i.WT,o,p,s=-1,t=-1,u=-1,v=-1,A=false,k=-1,j=-1,q=-1,r=-1;if(g){g.onmousedown=function(b){b=b||window.event;c.capture(g);b=c.pageCoordinates(b);o=b.x;p=b.y;g.onmousemove=D};g.onmouseup=function(){g.onmousemove=null;y();c.capture(null)}}this.centerDialog=function(){var b=c.parsePct(c.css(a,\"max-width\"),0),d=c.parsePct(c.css(a,\"max-height\"),0);if(b!==0){var e=c.windowSize(),l=jQuery.data(f.firstChild, \"layout\");l&&l.setMaxSize(e.x*b/100,e.y*d/100)}if(a.parentNode==null)a=g=null;else if(a.style.display!=\"none\"&&a.style.visibility!=\"hidden\"){e=c.windowSize();b=a.offsetWidth;d=a.offsetHeight;if(k!=-1)m=true;if(j!=-1)n=true;if(m){a.style.left=Math.round((e.x-b)/2+(c.isIE6?document.documentElement.scrollLeft:0))+\"px\";a.style.marginLeft=\"0px\"}if(n){a.style.top=Math.round((e.y-d)/2+(c.isIE6?document.documentElement.scrollTop:0))+\"px\";a.style.marginTop=\"0px\"}if(a.style.position!=\"\")a.style.visibility= \"visible\";y()}};this.bringToFront=function(){var b=0;$(\".Wt-dialog, .modal, .modal-dialog\").each(function(d,e){b=Math.max(b,$(e).css(\"z-index\"))});if(b>a.style.zIndex)a.style.zIndex=b+1};this.onresize=function(b,d,e){m=n=false;A=!e;F(a,b,d);jQuery.data(f.firstChild,\"layout\").setMaxSize(0,0);i.layouts2.scheduleAdjust();e&&z(b,d)};f.wtResize=E;a.wtPosition=G;if(a.style.width!=\"\")f.style.width=c.parsePx(a.style.width)>0?a.style.width:a.offsetWidth+\"px\";if(a.style.height!=\"\")f.style.height=c.parsePx(a.style.height)> 0?a.style.height:a.offsetHeight+\"px\";h.centerDialog()}");
	}
}
