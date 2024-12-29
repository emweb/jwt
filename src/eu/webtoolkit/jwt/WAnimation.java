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
 * A value class that defines a transition effect.
 *
 * <p>This class defines an animation used as a transition to show or hide a widget.
 *
 * <p>The animation can be defined as a motion effect (e.g. sliding in or out), optionally combined
 * with a fade effect. A timing function defines how the effects(s) are animated during the total
 * duration of the animation.
 *
 * <p>
 *
 * @see WWidget#animateShow(WAnimation animation)
 * @see WWidget#animateHide(WAnimation animation)
 * @see WWidget#setHidden(boolean hidden, WAnimation animation)
 */
public class WAnimation {
  private static Logger logger = LoggerFactory.getLogger(WAnimation.class);

  /**
   * Default constructor.
   *
   * <p>Creates an animation that actually represent <i>no</i> animation. ({@link
   * WAnimation#getEffects() getEffects()} == 0).
   */
  public WAnimation() {
    this.effects_ = EnumSet.noneOf(AnimationEffect.class);
    this.timing_ = TimingFunction.Linear;
    this.duration_ = 250;
  }
  /**
   * Creates an animation.
   *
   * <p>An animation is created with given effects, timing and duration.
   */
  public WAnimation(EnumSet<AnimationEffect> effects, TimingFunction timing, int duration) {
    this.effects_ = effects;
    this.timing_ = timing;
    this.duration_ = duration;
  }
  /**
   * Creates an animation.
   *
   * <p>Calls {@link #WAnimation(EnumSet effects, TimingFunction timing, int duration) this(effects,
   * TimingFunction.Linear, 250)}
   */
  public WAnimation(EnumSet<AnimationEffect> effects) {
    this(effects, TimingFunction.Linear, 250);
  }
  /**
   * Creates an animation.
   *
   * <p>Calls {@link #WAnimation(EnumSet effects, TimingFunction timing, int duration) this(effects,
   * timing, 250)}
   */
  public WAnimation(EnumSet<AnimationEffect> effects, TimingFunction timing) {
    this(effects, timing, 250);
  }
  /**
   * Creates an animation.
   *
   * <p>An animation is created with one effect, timing and duration.
   */
  public WAnimation(AnimationEffect effect, TimingFunction timing, int duration) {
    this.effects_ = EnumSet.of(effect);
    this.timing_ = timing;
    this.duration_ = duration;
  }
  /**
   * Creates an animation.
   *
   * <p>Calls {@link #WAnimation(AnimationEffect effect, TimingFunction timing, int duration)
   * this(effect, TimingFunction.Linear, 250)}
   */
  public WAnimation(AnimationEffect effect) {
    this(effect, TimingFunction.Linear, 250);
  }
  /**
   * Creates an animation.
   *
   * <p>Calls {@link #WAnimation(AnimationEffect effect, TimingFunction timing, int duration)
   * this(effect, timing, 250)}
   */
  public WAnimation(AnimationEffect effect, TimingFunction timing) {
    this(effect, timing, 250);
  }
  /**
   * Creates an animation.
   *
   * <p>An animation is created with two effects (a motion and Fade).
   */
  public WAnimation(
      AnimationEffect effect1, AnimationEffect effect2, TimingFunction timing, int duration) {
    this.effects_ = EnumSet.of(effect1, effect2);
    this.timing_ = timing;
    this.duration_ = duration;
  }
  /**
   * Creates an animation.
   *
   * <p>Calls {@link #WAnimation(AnimationEffect effect1, AnimationEffect effect2, TimingFunction
   * timing, int duration) this(effect1, effect2, TimingFunction.Linear, 250)}
   */
  public WAnimation(AnimationEffect effect1, AnimationEffect effect2) {
    this(effect1, effect2, TimingFunction.Linear, 250);
  }
  /**
   * Creates an animation.
   *
   * <p>Calls {@link #WAnimation(AnimationEffect effect1, AnimationEffect effect2, TimingFunction
   * timing, int duration) this(effect1, effect2, timing, 250)}
   */
  public WAnimation(AnimationEffect effect1, AnimationEffect effect2, TimingFunction timing) {
    this(effect1, effect2, timing, 250);
  }
  /**
   * Clone method.
   *
   * <p>Clones this animation object.
   */
  public WAnimation clone() {
    WAnimation result = new WAnimation();
    result.effects_ = EnumSet.copyOf(this.effects_);
    result.duration_ = this.duration_;
    return result;
  }
  /**
   * Sets the animation effects.
   *
   * <p>A motion effect ({@link AnimationEffect#SlideInFromLeft}, {@link
   * AnimationEffect#SlideInFromRight}, {@link AnimationEffect#SlideInFromBottom}, {@link
   * AnimationEffect#SlideInFromTop} or {@link AnimationEffect#Pop}) can be combined with a fade
   * effect ({@link AnimationEffect#Fade}).
   *
   * <p>When effects are 0, the animation does not actually specify an animation, but instead an
   * instant transition.
   */
  public void setEffects(EnumSet<AnimationEffect> effects) {
    this.effects_ = EnumSet.copyOf(effects);
  }
  /**
   * Sets the animation effects.
   *
   * <p>Calls {@link #setEffects(EnumSet effects) setEffects(EnumSet.of(effect, effects))}
   */
  public final void setEffects(AnimationEffect effect, AnimationEffect... effects) {
    setEffects(EnumSet.of(effect, effects));
  }
  /**
   * Returns animation effects.
   *
   * <p>
   *
   * @see WAnimation#setEffects(EnumSet effects)
   */
  public EnumSet<AnimationEffect> getEffects() {
    return this.effects_;
  }
  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * <p>Returns <code>true</code> if the transitions are exactly the same.
   */
  public boolean equals(final WAnimation animation) {
    return animation.effects_.equals(this.effects_) && animation.duration_ == this.duration_;
  }
  /**
   * Sets the duration.
   *
   * <p>The default animation duration is 250 ms.
   *
   * <p>
   *
   * @see WAnimation#getDuration()
   */
  public void setDuration(int msecs) {
    this.duration_ = msecs;
  }
  /**
   * Returns the duration.
   *
   * <p>
   *
   * @see WAnimation#setDuration(int msecs)
   */
  public int getDuration() {
    return this.duration_;
  }
  /**
   * Sets a timing function.
   *
   * <p>The default timinig function is {@link TimingFunction#Linear}.
   */
  public void setTimingFunction(TimingFunction tf) {
    this.timing_ = tf;
  }
  /**
   * Returns the timing function.
   *
   * <p>
   *
   * @see WAnimation#setTimingFunction(TimingFunction tf)
   */
  public TimingFunction getTimingFunction() {
    return this.timing_;
  }
  /**
   * Returns whether the animation is empty.
   *
   * <p>An animation is empty (meaning the transition is instant), if the duration is 0, or if no
   * effects are defined.
   */
  public boolean isEmpty() {
    return this.duration_ == 0 || !!this.effects_.isEmpty();
  }

  private EnumSet<AnimationEffect> effects_;
  private TimingFunction timing_;
  private int duration_;
}
