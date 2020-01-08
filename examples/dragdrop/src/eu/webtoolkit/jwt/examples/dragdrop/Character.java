/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.dragdrop;

import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WDropEvent;
import eu.webtoolkit.jwt.WText;

/**
 * A Matrix character that takes red and/or blue pills.
 * 
 * The Character class demonstrates how to accept and react to drop events.
 */
public class Character extends WText {
    /**
     * Create a new character with the given name.
     */
    public Character(String name, WContainerWidget parent) {
        super(parent);
        name_ = name;
        redDrops_ = 0;
        blueDrops_ = 0;

        setText(name_ + " got no pills");

        setStyleClass("character");

        /*
         * Accept drops, and indicate this with a change in CSS style class.
         */
        acceptDrops("red-pill", "red-drop-site");
        acceptDrops("blue-pill", "blue-drop-site");

        setInline(false);
    }

    /**
     * React to a drop event.
     */
    public void dropEvent(WDropEvent event) {
        if (event.getMimeType().equals("red-pill"))
            ++redDrops_;
        if (event.getMimeType().equals("blue-pill"))
            ++blueDrops_;

        String text = name_ + " got ";

        if (redDrops_ != 0)
            text += redDrops_ + " red pill";
        if (redDrops_ > 1)
            text += "s";

        if (redDrops_ != 0 && blueDrops_ != 0)
            text += " and ";

        if (blueDrops_ != 0)
            text += blueDrops_ + " blue pill";
        if (blueDrops_ > 1)
            text += "s";

        setText(text);
    }

    /**
     * The name
     */
    private String name_;

    /**
     * The current number of red pills.
     */
    private int redDrops_;

    /**
     * The current number of blue pills.
     */
    private int blueDrops_;
}