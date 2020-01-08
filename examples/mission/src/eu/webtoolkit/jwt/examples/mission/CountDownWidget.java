/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.mission;

import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.Signal1;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WMouseEvent;
import eu.webtoolkit.jwt.WText;
import eu.webtoolkit.jwt.WTimer;

/**
 * A widget which displays a decrementing number.
 */
public class CountDownWidget extends WText {
    /**
     * Create a new CountDownWidget.
     * 
     * The widget will count down from start to stop, decrementing the number
     * every msec milliseconds.
     */
    public CountDownWidget(int start, int stop, int msec,
            WContainerWidget parent) {
        super(parent);

        done_ = new Signal();

        start_ = start;
        stop_ = stop;

        stop_ = Math.min(start_ - 1, stop_); // stop must be smaller than start
        current_ = start_;

        timer_ = new WTimer(this);
        timer_.setInterval(msec);
        timer_.timeout().addListener(this, new Signal1.Listener<WMouseEvent>() {

            public void trigger(WMouseEvent a1) {
                timerTick();
            }
        });
        timer_.start();

        setText(current_ + "");
    }

    /**
     * Signal emitted when the countdown reached stop.
     */
    public Signal done() {
        return done_;
    }

    /**
     * brief Cancel the count down.
     */
    public void cancel() {
        timer_.stop();
    }

    private Signal done_;
    private int start_;
    private int stop_;

    private int current_;

    private WTimer timer_;

    /**
     * Process one timer tick.
     */
    private void timerTick() {
        setText(--current_ + "");

        if (current_ <= stop_) {
            timer_.stop();
            done_.trigger();
        }

    }
};
