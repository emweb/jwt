/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class which provides timer signals and single-shot timers.
 *
 * <p>To use a timer, create a WTimer instance, set the timer interval using {@link
 * WTimer#setInterval(Duration msec) setInterval()} and connect a slot to the timeout signal. Then,
 * start the timer using {@link WTimer#start() start()}. An active timer may be cancelled at any
 * time using {@link WTimer#stop() stop()}.
 *
 * <p>By default, a timer will continue to generate events until you {@link WTimer#stop() stop()}
 * it. To create a timer that will fire only once, use {@link WTimer#setSingleShot(boolean
 * singleShot) setSingleShot()}.
 *
 * <p>When connecting stateless slot implementations to the timeout signal, these stateless slot
 * implementations will be used as for any other signal (when Ajax is available).
 *
 * <p>In clients without (enabled) JavaScript support, the minimum resolution of the timer is one
 * second (1000 milli-seconds), and it is probably wise to use timers sparingly.
 *
 * <p>A {@link WTimer} is only usable inside of a JWt event loop. If you want to create a timer
 * outside the JWt event loop, take a look at {@link java.util.Timer}.
 */
public class WTimer extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WTimer.class);

  /** Construct a new timer with the given parent. */
  public WTimer() {
    super();
    this.timerWidget_ = null;
    this.uTimerWidget_ = new WTimerWidget(this);
    this.interval_ = Duration.ofMillis(0);
    this.singleShot_ = false;
    this.active_ = false;
    this.timeout_ = new Time();
    this.timerWidget_ = this.uTimerWidget_;
    this.timeout()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              WTimer.this.gotTimeout();
            });
  }
  /** Returns the interval. */
  public Duration getInterval() {
    return this.interval_;
  }
  /** Sets the interval. */
  public void setInterval(Duration msec) {
    this.interval_ = msec;
  }
  /** Returns if the timer is running. */
  public boolean isActive() {
    return this.active_;
  }
  /** Is this timer set to fire only once. */
  public boolean isSingleShot() {
    return this.singleShot_;
  }
  /**
   * Configures this timer to fire only once.
   *
   * <p>A Timer is by default not single shot, and will fire continuously, until it is stopped.
   */
  public void setSingleShot(boolean singleShot) {
    this.singleShot_ = singleShot;
  }
  /**
   * Starts the timer.
   *
   * <p>The timer will be {@link WTimer#isActive() isActive()}, until either the interval has
   * elapsed, after which the timeout signal is activated, or until {@link WTimer#stop() stop()} is
   * called.
   */
  public void start() {
    WApplication app = WApplication.getInstance();
    if (!this.active_) {
      if (app != null && app.getTimerRoot() != null) {
        app.getTimerRoot().addWidget(this.uTimerWidget_);
      }
    }
    this.active_ = true;
    this.timeout_ = new Time().add((int) this.interval_.toMillis());
    boolean jsRepeat =
        !this.singleShot_
            && (app != null && app.getEnvironment().hasAjax() || !this.timeout().isExposedSignal());
    this.timerWidget_.timerStart(jsRepeat);
  }
  /**
   * Stops the timer.
   *
   * <p>You may stop the timer during its {@link WTimer#timeout() timeout()}, or cancel a running
   * timer at any other time.
   *
   * <p>
   *
   * @see WTimer#start()
   */
  public void stop() {
    if (this.active_) {
      if (this.timerWidget_ != null && this.timerWidget_.getParent() != null) {
        this.uTimerWidget_ = WidgetUtils.remove(this.timerWidget_.getParent(), this.timerWidget_);
      }
      this.active_ = false;
    }
  }
  /**
   * Signal emitted when the timer timeouts.
   *
   * <p>The WMouseEvent does not provide any meaningful information but is an implementation
   * artefact.
   */
  public EventSignal1<WMouseEvent> timeout() {
    return this.timerWidget_.clicked();
  }

  WTimerWidget timerWidget_;
  private WTimerWidget uTimerWidget_;
  private Duration interval_;
  private boolean singleShot_;
  private boolean active_;
  private Time timeout_;

  private void gotTimeout() {
    if (this.active_) {
      if (!this.singleShot_) {
        this.timeout_ = new Time().add((int) this.interval_.toMillis());
        if (!this.timerWidget_.isJsRepeat()) {
          WApplication app = WApplication.getInstance();
          this.timerWidget_.timerStart(app.getEnvironment().hasAjax());
        }
      } else {
        this.stop();
      }
    }
  }

  int getRemainingInterval() {
    int remaining = this.timeout_.subtract(new Time());
    return Math.max(0, remaining);
  }
}
