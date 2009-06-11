package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A standard dialog for confirmation or to get simple user input
 * 
 * 
 * The message box shows a message in a dialog window, with a number of buttons.
 * These buttons may be standard buttons, or customized.
 * <p>
 * There are two distinct ways for using a WMessageBox, which reflect the two
 * ways of dealing with a {@link WDialog} box.
 * <p>
 * The more elaborate way is by creating a {@link WMessageBox}, and connecting
 * the buttonClicked signal to a method. This method then interpretes the result
 * and deletes the message box.
 * <p>
 * This will show a message box that looks like this:
 * <p>
 * <div align="center"> <img src="/WMessageBox-1.png"
 * alt="Example of a WMessageBox">
 * <p>
 * <strong>Example of a WMessageBox</strong>
 * </p>
 * </div>
 */
public class WMessageBox extends WDialog {
	/**
	 * Create an empty message box.
	 * 
	 * The button labels may be set fixed English (if i18n = false), or fetched
	 * from a resource bundle if i18n = true. In that case, the key for each
	 * button is exactly the same as the English text.
	 */
	public WMessageBox(boolean i18n) {
		super();
		this.buttons_ = EnumSet.noneOf(StandardButton.class);
		this.icon_ = Icon.NoIcon;
		this.i18n_ = i18n;
		this.result_ = StandardButton.NoButton;
		this.buttonClicked_ = new Signal1<StandardButton>(this);
		this.create();
	}

	public WMessageBox() {
		this(false);
	}

	/**
	 * Create a message box with given caption, text, icon, and buttons.
	 * 
	 * The button labels may be set fixed English (if i18n = false), or fetched
	 * from a resource bundle if i18n = true. In that case, the key for each
	 * button is exactly the same as the English text.
	 */
	public WMessageBox(CharSequence caption, CharSequence text, Icon icon,
			EnumSet<StandardButton> buttons, boolean i18n) {
		super(caption);
		this.buttons_ = EnumSet.noneOf(StandardButton.class);
		this.icon_ = Icon.NoIcon;
		this.i18n_ = i18n;
		this.buttonClicked_ = new Signal1<StandardButton>(this);
		this.create();
		this.setText(text);
		this.setIcon(icon);
		this.setButtons(buttons);
	}

	public WMessageBox(CharSequence caption, CharSequence text, Icon icon,
			EnumSet<StandardButton> buttons) {
		this(caption, text, icon, buttons, false);
	}

	/**
	 * Set the text for the message box.
	 */
	public void setText(CharSequence text) {
		this.text_.setText(text);
	}

	/**
	 * Get the message box text.
	 */
	public WString getText() {
		return this.text_.getText();
	}

	/**
	 * Get the text widget.
	 * 
	 * This may be useful to customize the style or layout of the displayed
	 * text.
	 */
	public WText getTextWidget() {
		return this.text_;
	}

	/**
	 * Set the icon.
	 */
	public void setIcon(Icon icon) {
		this.icon_ = icon;
		if (false && this.icon_ != Icon.NoIcon) {
			if (!(this.iconImage_ != null)) {
				this.iconImage_ = new WImage(iconURI[this.icon_.getValue() - 1]);
				this.getContents().insertBefore(this.iconImage_, this.text_);
			} else {
				this.iconImage_.setImageRef(iconURI[this.icon_.getValue() - 1]);
			}
		} else {
			if (this.iconImage_ != null) {
				if (this.iconImage_ != null)
					this.iconImage_.remove();
			}
			this.iconImage_ = null;
		}
	}

	/**
	 * Get the icon.
	 */
	public Icon getIcon() {
		return this.icon_;
	}

	public WImage getIconImage() {
		return this.iconImage_;
	}

	/**
	 * Add a custom button with given text.
	 * 
	 * When the button is clicked, the associated result will be returned.
	 */
	public WPushButton addButton(CharSequence text, StandardButton result) {
		WPushButton b = new WPushButton(text, this.buttonContainer_);
		this.buttonMapper_.mapConnect(b.clicked(), result);
		return b;
	}

	/**
	 * Set standard buttons for the message box.
	 */
	public void setButtons(EnumSet<StandardButton> buttons) {
		this.buttons_ = EnumSet.copyOf(buttons);
		this.buttonContainer_.clear();
		for (int i = 0; i < 9; ++i) {
			if (!EnumUtils.mask(this.buttons_, order_[i]).isEmpty()) {
				WPushButton b = new WPushButton(this.i18n_ ? tr(buttonText_[i])
						: new WString(buttonText_[i]), this.buttonContainer_);
				this.buttonMapper_.mapConnect(b.clicked(), order_[i]);
				if (order_[i] == StandardButton.Ok
						|| order_[i] == StandardButton.Yes) {
					b.setFocus();
				}
			}
		}
	}

	public final void setButtons(StandardButton button,
			StandardButton... buttons) {
		setButtons(EnumSet.of(button, buttons));
	}

	/**
	 * returns the standard buttons.
	 */
	public EnumSet<StandardButton> getButtons() {
		return this.buttons_;
	}

	/**
	 * Returns the button widget for the given standard button.
	 * 
	 * This may be useful to customize the style or layout of the button.
	 */
	public WPushButton getButton(StandardButton b) {
		int index = 0;
		for (int i = 1; i <= 9; ++i) {
			if (!EnumUtils.mask(this.buttons_, order_[i]).isEmpty()) {
				if (order_[i] == b) {
					return ((this.buttonContainer_.getChildren().get(index)) instanceof WPushButton ? (WPushButton) (this.buttonContainer_
							.getChildren().get(index))
							: null);
				}
				++index;
			}
		}
		return null;
	}

	/**
	 * Returns the result of this message box.
	 * 
	 * This value is only defined after a button has been clicked.
	 */
	public StandardButton getButtonResult() {
		return this.result_;
	}

	/**
	 * Signal emitted when a button is clicked.
	 */
	public Signal1<StandardButton> buttonClicked() {
		return this.buttonClicked_;
	}

	private EnumSet<StandardButton> buttons_;
	private Icon icon_;
	private boolean i18n_;
	private StandardButton result_;
	private Signal1<StandardButton> buttonClicked_;
	private WContainerWidget buttonContainer_;
	private WText text_;
	private WImage iconImage_;
	private WSignalMapper1<StandardButton> buttonMapper_;

	private void create() {
		this.iconImage_ = null;
		this.text_ = new WText(this.getContents());
		WContainerWidget buttons = new WContainerWidget(this.getContents());
		buttons.setMargin(new WLength(3), EnumSet.of(Side.Top));
		buttons.setPadding(new WLength(5), EnumSet.of(Side.Left, Side.Right));
		this.buttonContainer_ = new WContainerWidget(buttons);
		this.buttonMapper_ = new WSignalMapper1<StandardButton>(this);
		this.buttonMapper_.mapped().addListener(this,
				new Signal1.Listener<StandardButton>() {
					public void trigger(StandardButton e1) {
						WMessageBox.this.onButtonClick(e1);
					}
				});
		this.buttonContainer_.setStyleClass("Wt-msgbox-buttons");
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
	private static String[] buttonText_ = { "Ok", "Yes", "Yes to All", "Retry",
			"No", "No to All", "Abort", "Ignore", "Cancel" };
	private static String[] iconURI = { "icons/information.png",
			"icons/warning.png", "icons/critical.png", "icons/question.png" };
}
