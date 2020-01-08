/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.javascript;

import eu.webtoolkit.jwt.JSignal;
import eu.webtoolkit.jwt.JSignal1;
import eu.webtoolkit.jwt.JSlot;
import eu.webtoolkit.jwt.WObject;

/**
 * A JavaScript based popup window, encapsulating the Javascript functions
 * alert(), confirm(), and prompt().
 * 
 * Use one of the create static methods to create a popup. This will not display
 * the popup, until either the show slot is triggered from an event handler, or
 * is executed using it's exec() method.
 * 
 * When the user closes the popup, either the okPressed or cancelPressed signal
 * is emitted. For a prompt dialog, the value is passed as a parameter to the
 * okPressed signal.
 */
public class Popup extends WObject {

    /**
     * Create a confirm dialog.
     */
    public static Popup createConfirm(CharSequence message, WObject parent) {
        return new Popup(Type.Confirm, message, "", parent);
    }

    /**
     * Create a prompt dialog with the given default value
     */
    public static Popup createPrompt(CharSequence message, String defaultValue,
            WObject parent) {
        return new Popup(Type.Prompt, message, defaultValue, parent);
    }

    /**
     * Create an alert dialog.
     */
    public static Popup createAlert(CharSequence message, WObject parent) {
        return new Popup(Type.Alert, message, "", parent);
    }

    /**
     * Change the message
     */
    public void setMessage(CharSequence message) {
        message_ = message;
        setJavaScript();
    }

    /**
     * Change the default value for a prompt dialog.
     */
    public void setDefaultValue(String defaultValue) {
        defaultValue_ = defaultValue;
        setJavaScript();
    }

    /**
     * Get the current message.
     */
    public CharSequence message() {
        return message_;
    }

    /**
     * Get the default value for a prompt dialog.
     */
    public String defaultValue() {
        return defaultValue_;
    }

    /**
     * Show the dialog.
     * 
     * Use show.exec() to show the dialog, or connect the slot to an EventSignal
     * to directly show the dialog without a server round trip.
     */
    public JSlot show = new JSlot();

    /**
     * Signal emitted when ok pressed.
     */
    public JSignal1<String> okPressed() {
        return okPressed_;
    }

    /**
     * Signal emitted when cancel is pressed.
     */
    public JSignal cancelPressed() {
        return cancelPressed_;
    }

    /**
     * Popup type.
     */
    private enum Type {
        Confirm, Alert, Prompt
    }

    /**
     * Popup constructor.
     */
    private Popup(Type t, CharSequence message, String defaultValue,
            WObject parent) {
        super();
        okPressed_ = new JSignal1<String>(this, "ok") {
        };
        cancelPressed_ = new JSignal(this, "cancel") {
        };
        t_ = t;
        message_ = message;
        defaultValue_ = defaultValue;
        setJavaScript();
    }

    private JSignal1<String> okPressed_;
    private JSignal cancelPressed_;

    private Type t_;
    private CharSequence message_;
    private String defaultValue_;

    /**
     * Update the javascript code.
     */
    private void setJavaScript() {
        /*
         * Sets the JavaScript code.
         * 
         * Notice how Wt.emit() is used to emit the okPressed or cancelPressed
         * signal, and how arguments may be passed to it, matching the number
         * and type of arguments in the JSignal definition.
         */
        switch (t_) {
        case Confirm:
            show.setJavaScript("function(){ if (confirm('"
                    + message_.toString() + "')) {"
                    + okPressed_.createCall("''") + "} else {"
                    + cancelPressed_.createCall() + "}}");
            break;
        case Alert:
            show.setJavaScript("function(){ alert('" + message_.toString()
                    + "');" + okPressed_.createCall("''") + "}");
            break;
        case Prompt:
            show.setJavaScript("function(){var n = prompt('"
                    + message_.toString() + "', '" + defaultValue_ + "');"
                    + "if (n != null) {" + okPressed_.createCall("n")
                    + "} else {" + cancelPressed_.createCall() + "}}");
        }
    }
}
