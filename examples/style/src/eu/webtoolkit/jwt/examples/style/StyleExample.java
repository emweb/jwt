/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.style;

import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WBreak;
import eu.webtoolkit.jwt.WColor;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WIntValidator;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WValidator;

/**
 * A demonstration of the RoundedWidget.
 * 
 * This is the main class for the style example.
 */
public class StyleExample extends WContainerWidget {
    String loremipsum = "Lorem ipsum dolor sit amet, consectetur adipisicing "
            + "elit, sed do eiusmod tempor incididunt ut labore et "
            + "dolore magna aliqua. Ut enim ad minim veniam, quis "
            + "nostrud exercitation ullamco laboris nisi ut aliquip "
            + "ex ea commodo consequat. Duis aute irure dolor in "
            + "reprehenderit in voluptate velit esse cillum dolore eu "
            + "fugiat nulla pariatur. Excepteur sint occaecat cupidatat "
            + "non proident, sunt in culpa qui officia deserunt mollit "
            + "anim id est laborum.";

    /**
     * Create a StyleExample.
     */
    public StyleExample(WContainerWidget parent) {
        super(parent);
        w_ = new RoundedWidget(RoundedWidget.allCorners, this);

        new WText(loremipsum, w_.contents());
        new WBreak(this);

        new WText("Color (rgb): ", this);
        r_ = createValidateLineEdit(w_.backgroundColor().getRed(), 0, 255);
        g_ = createValidateLineEdit(w_.backgroundColor().getGreen(), 0, 255);
        b_ = createValidateLineEdit(w_.backgroundColor().getBlue(), 0, 255);

        new WBreak(this);

        new WText("Radius (px): ", this);
        radius_ = createValidateLineEdit(w_.cornerRadius(), 1, 500);

        new WBreak(this);

        WPushButton p = new WPushButton("Update!", this);
        p.clicked().addListener(this, new Signal1.Listener<WMouseEvent>() {
            public void trigger(WMouseEvent me) {
                updateStyle();
            }
        });

        new WBreak(this);

        error_ = new WText("", this);
    }

    private RoundedWidget w_;
    private WText error_;

    private WLineEdit radius_, r_, g_, b_;

    private WLineEdit createValidateLineEdit(int value, int min, int max) {
        WLineEdit le = new WLineEdit(value + "", this);
        le.setTextSize(3);
        le.setValidator(new WIntValidator(min, max));

        return le;
    }

    private void updateStyle() {
        if ((r_.validate() != WValidator.State.Valid)
                || (g_.validate() != WValidator.State.Valid)
                || (b_.validate() != WValidator.State.Valid))
            error_
                    .setText("Color components must be numbers between 0 and 255.");
        else if (radius_.validate() != WValidator.State.Valid)
            error_.setText("Radius must be between 1 and 500.");
        else {
            w_.setBackgroundColor(new WColor(Integer.parseInt(r_.getText()),
                    Integer.parseInt(g_.getText()), Integer.parseInt(b_
                            .getText())));
            w_.setCornerRadius(Integer.parseInt(radius_.getText()));
            error_.setText("");
        }
    }
}
