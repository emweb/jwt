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
 * A standard dialog for confirmation or to get simple user input.
 * <p>
 * 
 * The messagebox shows a message in a dialog window, with a number of buttons.
 * These buttons may be standard buttons, or customized.
 * <p>
 * A messagebox is (usually) modal, and can be instantiated synchronously or
 * asynchronously.
 * <p>
 * When using a messagebox asynchronously, there is no API call that waits for
 * the messagebox to be processed. Then, the usage is similar to instantiating a
 * {@link WDialog} (or any other widget). You need to connect to the
 * buttonClicked signal with a method that interpretes the result and deletes
 * the message box.
 * <p>
 * The synchronous use of a messagebox involves the use of the static
 * {@link WWidget#show() WWidget#show()} method, which blocks the current thread
 * until the user has processed the messabebox. Since this uses the
 * {@link WDialog#exec(WAnimation animation) WDialog#exec()}, it suffers from
 * the same scalability issues. See documentation of {@link WDialog} for more
 * details.
 * <p>
 * This will show a message box that looks like this:
 * <p>
 * <table border="0" align="center" cellspacing="3" cellpadding="3">
 * <tr>
 * <td><div align="center"> <img src="doc-files//WMessageBox-default-1.png"
 * alt="Example of a WMessageBox (default)">
 * <p>
 * <strong>Example of a WMessageBox (default)</strong>
 * </p>
 * </div></td>
 * <td><div align="center"> <img src="doc-files//WMessageBox-polished-1.png"
 * alt="Example of a WMessageBox (polished)">
 * <p>
 * <strong>Example of a WMessageBox (polished)</strong>
 * </p>
 * </div></td>
 * </tr>
 * </table>
 * <p>
 * <h3>i18n</h3>
 * <p>
 * The strings used in the {@link WMessageBox} buttons can be translated by
 * overriding the default values for the following localization keys:
 * <ul>
 * <li>Wt.WMessageBox.Abort: Abort</li>
 * <li>Wt.WMessageBox.Cancel: Cancel</li>
 * <li>Wt.WMessageBox.Ignore: Ignore</li>
 * <li>Wt.WMessageBox.No: No</li>
 * <li>Wt.WMessageBox.NoToAll: No to All</li>
 * <li>Wt.WMessageBox.Ok: Ok</li>
 * <li>Wt.WMessageBox.Retry: Retry</li>
 * <li>Wt.WMessageBox.Yes: Yes</li>
 * <li>Wt.WMessageBox.YesToAll: Yes to All</li>
 * </ul>
 * <p>
 * <h3>CSS</h3>
 * <p>
 * A {@link WMessageBox} can be styled using the <code>Wt-dialog</code> and
 * <code>Wt-outset</code> style classes from it&apos;s superclass
 * {@link WDialog}. The messagebox&apos; buttons can be styled using
 * <code>Wt-msgbox-buttons</code> style class.
 */
public class WMessageBox extends WDialog {
	private static Logger logger = LoggerFactory.getLogger(WMessageBox.class);

	/**
	 * Creates an empty message box.
	 */
	public WMessageBox(WObject parent) {
		super(parent);
		this.buttons_ = EnumSet.noneOf(StandardButton.class);
		this.icon_ = Icon.NoIcon;
		this.result_ = StandardButton.NoButton;
		this.buttonClicked_ = new Signal1<StandardButton>(this);
		this.create();
	}

	/**
	 * Creates an empty message box.
	 * <p>
	 * Calls {@link #WMessageBox(WObject parent) this((WObject)null)}
	 */
	public WMessageBox() {
		this((WObject) null);
	}

	/**
	 * Creates a message box with given caption, text, icon, and buttons.
	 */
	public WMessageBox(CharSequence caption, CharSequence text, Icon icon,
			EnumSet<StandardButton> buttons, WObject parent) {
		super(caption, parent);
		this.buttons_ = EnumSet.noneOf(StandardButton.class);
		this.icon_ = Icon.NoIcon;
		this.buttonClicked_ = new Signal1<StandardButton>(this);
		this.create();
		this.setText(text);
		this.setIcon(icon);
		this.setButtons(buttons);
	}

	/**
	 * Creates a message box with given caption, text, icon, and buttons.
	 * <p>
	 * Calls
	 * {@link #WMessageBox(CharSequence caption, CharSequence text, Icon icon, EnumSet buttons, WObject parent)
	 * this(caption, text, icon, buttons, (WObject)null)}
	 */
	public WMessageBox(CharSequence caption, CharSequence text, Icon icon,
			EnumSet<StandardButton> buttons) {
		this(caption, text, icon, buttons, (WObject) null);
	}

	/**
	 * Sets the text for the message box.
	 */
	public void setText(CharSequence text) {
		this.text_.setText(text);
	}

	/**
	 * Returns the message box text.
	 */
	public WString getText() {
		return this.text_.getText();
	}

	/**
	 * Returns the text widget.
	 * <p>
	 * This may be useful to customize the style or layout of the displayed
	 * text.
	 */
	public WText getTextWidget() {
		return this.text_;
	}

	/**
	 * Sets the icon.
	 */
	public void setIcon(Icon icon) {
		this.icon_ = icon;
		if (false && this.icon_ != Icon.NoIcon) {
			if (!(this.iconImage_ != null)) {
				this.iconImage_ = new WImage(new WLink(iconURI[this.icon_
						.getValue() - 1]));
				this.getContents().insertBefore(this.iconImage_, this.text_);
			} else {
				this.iconImage_.setImageLink(new WLink(iconURI[this.icon_
						.getValue() - 1]));
			}
		} else {
			if (this.iconImage_ != null)
				this.iconImage_.remove();
			this.iconImage_ = null;
		}
	}

	/**
	 * Returns the icon.
	 */
	public Icon getIcon() {
		return this.icon_;
	}

	WImage getIconImage() {
		return this.iconImage_;
	}

	/**
	 * Add a custom button with given text.
	 * <p>
	 * When the button is clicked, the associated result will be returned.
	 */
	public WPushButton addButton(CharSequence text, StandardButton result) {
		WPushButton b = new WPushButton(text, this.getFooter());
		this.buttonMapper_.mapConnect(b.clicked(), result);
		return b;
	}

	/**
	 * Sets standard buttons for the message box.
	 */
	public void setButtons(EnumSet<StandardButton> buttons) {
		this.buttons_ = EnumSet.copyOf(buttons);
		this.getFooter().clear();
		for (int i = 0; i < 9; ++i) {
			if (!EnumUtils.mask(this.buttons_, order_[i]).isEmpty()) {
				WPushButton b = new WPushButton(tr(buttonText_[i]), this
						.getFooter());
				this.buttonMapper_.mapConnect(b.clicked(), order_[i]);
				if (order_[i] == StandardButton.Ok
						|| order_[i] == StandardButton.Yes) {
					b.setFocus();
				}
			}
		}
	}

	/**
	 * Sets standard buttons for the message box.
	 * <p>
	 * Calls {@link #setButtons(EnumSet buttons) setButtons(EnumSet.of(button,
	 * buttons))}
	 */
	public final void setButtons(StandardButton button,
			StandardButton... buttons) {
		setButtons(EnumSet.of(button, buttons));
	}

	/**
	 * Returns the standard buttons.
	 */
	public EnumSet<StandardButton> getButtons() {
		return this.buttons_;
	}

	/**
	 * Returns the button widget for the given standard button.
	 * <p>
	 * This may be useful to customize the style or layout of the button.
	 */
	public WPushButton getButton(StandardButton b) {
		int index = 0;
		for (int i = 0; i <= 9; ++i) {
			if (!EnumUtils.mask(this.buttons_, order_[i]).isEmpty()) {
				if (order_[i] == b) {
					return ((this.getFooter().getChildren().get(index)) instanceof WPushButton ? (WPushButton) (this
							.getFooter().getChildren().get(index))
							: null);
				}
				++index;
			}
		}
		return null;
	}

	/**
	 * Returns the result of this message box.
	 * <p>
	 * This value is only defined after a button has been clicked.
	 */
	public StandardButton getButtonResult() {
		return this.result_;
	}

	/**
	 * Convenience method to show a message box, blocking the current thread.
	 * <p>
	 * Show a message box, blocking the current thread until the message box is
	 * closed, and return the result. The use of this method is not recommended
	 * since it uses {@link WDialog#exec(WAnimation animation) WDialog#exec()}.
	 * See documentation of {@link WDialog} for detailed information.
	 * <p>
	 * <i>This functionality is only available on Servlet 3.0 compatible servlet
	 * containers.</i>
	 */
	public static StandardButton show(CharSequence caption, CharSequence text,
			EnumSet<StandardButton> buttons, WAnimation animation) {
		final WMessageBox box = new WMessageBox(caption, text,
				Icon.Information, buttons);
		box.buttonClicked().addListener(box,
				new Signal1.Listener<StandardButton>() {
					public void trigger(StandardButton e1) {
						box.accept();
					}
				});
		box.exec(animation);
		return box.getButtonResult();
	}

	/**
	 * Convenience method to show a message box, blocking the current thread.
	 * <p>
	 * Returns
	 * {@link #show(CharSequence caption, CharSequence text, EnumSet buttons, WAnimation animation)
	 * show(caption, text, buttons, new WAnimation())}
	 */
	public static final StandardButton show(CharSequence caption,
			CharSequence text, EnumSet<StandardButton> buttons) {
		return show(caption, text, buttons, new WAnimation());
	}

	/**
	 * Signal emitted when a button is clicked.
	 */
	public Signal1<StandardButton> buttonClicked() {
		return this.buttonClicked_;
	}

	private EnumSet<StandardButton> buttons_;
	private Icon icon_;
	private StandardButton result_;
	private Signal1<StandardButton> buttonClicked_;
	private WText text_;
	private WImage iconImage_;
	private WSignalMapper1<StandardButton> buttonMapper_;

	private void create() {
		this.iconImage_ = null;
		this.text_ = new WText(this.getContents());
		WContainerWidget buttons = new WContainerWidget(this.getContents());
		buttons.setMargin(new WLength(3), EnumSet.of(Side.Top));
		buttons.setPadding(new WLength(5), EnumSet.of(Side.Left, Side.Right));
		this.buttonMapper_ = new WSignalMapper1<StandardButton>(this);
		this.buttonMapper_.mapped().addListener(this,
				new Signal1.Listener<StandardButton>() {
					public void trigger(StandardButton e1) {
						WMessageBox.this.onButtonClick(e1);
					}
				});
		this.rejectWhenEscapePressed();
	}

	private void onButtonClick(StandardButton b) {
		this.result_ = b;
		this.buttonClicked_.trigger(b);
	}

	// private void mappedButtonClick(StandardButton b) ;
	private static StandardButton[] order_ = { StandardButton.Ok,
			StandardButton.Yes, StandardButton.YesAll, StandardButton.Retry,
			StandardButton.No, StandardButton.NoAll, StandardButton.Abort,
			StandardButton.Ignore, StandardButton.Cancel };
	private static String[] buttonText_ = { "Wt.WMessageBox.Ok",
			"Wt.WMessageBox.Yes", "Wt.WMessageBox.YesToAll",
			"Wt.WMessageBox.Retry", "Wt.WMessageBox.No",
			"Wt.WMessageBox.NoToAll", "Wt.WMessageBox.Abort",
			"Wt.WMessageBox.Ignore", "Wt.WMessageBox.Cancel" };
	private static String[] iconURI = { "icons/information.png",
			"icons/warning.png", "icons/critical.png", "icons/question.png" };
}
