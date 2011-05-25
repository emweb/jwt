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
 * Class that defines a transition effect.
 * <p>
 * 
 * A class which defines an animation used as a transition to show or hide a
 * widget.
 * <p>
 * 
 * @see WWidget#animateShow(WAnimation animation)
 * @see WWidget#animateHide(WAnimation animation)
 * @see WWidget#setHidden(boolean hidden, WAnimation animation)
 */
public class WAnimation {
	/**
	 * An enumeration describing an anmiation effects.
	 */
	public enum AnimationEffect {
		/**
		 * Slides right to show, left to hide.
		 */
		SlideInFromLeft,
		/**
		 * Slides left to show, right to hide.
		 */
		SlideInFromRight,
		/**
		 * Slides up to show, down to hide.
		 */
		SlideInFromBottom,
		/**
		 * Slides down to show, up to hide.
		 */
		SlideInFromTop,
		/**
		 * Pops up to show, pops away to hide.
		 */
		Pop,
		/**
		 * Fade effect.
		 */
		Fade;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * A timing function.
	 */
	public enum TimingFunction {
		Ease, Linear, EaseIn, EaseOut, EaseInOut, CubicBezier;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Default constructor.
	 * <p>
	 * Creates an animation that actually represent <i>no</i> animation. (
	 * {@link WAnimation#getEffects() getEffects()} == 0).
	 */
	public WAnimation() {
		this.effects_ = EnumSet.noneOf(WAnimation.AnimationEffect.class);
		this.timing_ = WAnimation.TimingFunction.Linear;
		this.duration_ = 250;
	}

	/**
	 * Creates an animation.
	 */
	public WAnimation(EnumSet<WAnimation.AnimationEffect> effects,
			WAnimation.TimingFunction timing, int duration) {
		this.effects_ = effects;
		this.timing_ = timing;
		this.duration_ = duration;
	}

	/**
	 * Creates an animation.
	 * <p>
	 * Calls
	 * {@link #WAnimation(EnumSet effects, WAnimation.TimingFunction timing, int duration)
	 * this(effects, WAnimation.TimingFunction.Linear, 250)}
	 */
	public WAnimation(EnumSet<WAnimation.AnimationEffect> effects) {
		this(effects, WAnimation.TimingFunction.Linear, 250);
	}

	/**
	 * Creates an animation.
	 * <p>
	 * Calls
	 * {@link #WAnimation(EnumSet effects, WAnimation.TimingFunction timing, int duration)
	 * this(effects, timing, 250)}
	 */
	public WAnimation(EnumSet<WAnimation.AnimationEffect> effects,
			WAnimation.TimingFunction timing) {
		this(effects, timing, 250);
	}

	public WAnimation(WAnimation.AnimationEffect effect,
			WAnimation.TimingFunction timing, int duration) {
		this.effects_ = EnumSet.of(effect);
		this.timing_ = timing;
		this.duration_ = duration;
	}

	public WAnimation(WAnimation.AnimationEffect effect) {
		this(effect, WAnimation.TimingFunction.Linear, 250);
	}

	public WAnimation(WAnimation.AnimationEffect effect,
			WAnimation.TimingFunction timing) {
		this(effect, timing, 250);
	}

	public WAnimation(WAnimation.AnimationEffect effect1,
			WAnimation.AnimationEffect effect2,
			WAnimation.TimingFunction timing, int duration) {
		this.effects_ = EnumSet.of(effect1, effect2);
		this.timing_ = timing;
		this.duration_ = duration;
	}

	public WAnimation(WAnimation.AnimationEffect effect1,
			WAnimation.AnimationEffect effect2) {
		this(effect1, effect2, WAnimation.TimingFunction.Linear, 250);
	}

	public WAnimation(WAnimation.AnimationEffect effect1,
			WAnimation.AnimationEffect effect2, WAnimation.TimingFunction timing) {
		this(effect1, effect2, timing, 250);
	}

	/**
	 * Clone method.
	 * <p>
	 * Clones this transition object.
	 */
	public WAnimation clone() {
		WAnimation result = new WAnimation();
		result.effects_ = EnumSet.copyOf(this.effects_);
		result.duration_ = this.duration_;
		return result;
	}

	/**
	 * Sets the animation effects.
	 */
	public void setEffects(EnumSet<WAnimation.AnimationEffect> effects) {
		this.effects_ = EnumSet.copyOf(effects);
	}

	/**
	 * Sets the animation effects.
	 * <p>
	 * Calls {@link #setEffects(EnumSet effects) setEffects(EnumSet.of(effect,
	 * effects))}
	 */
	public final void setEffects(WAnimation.AnimationEffect effect,
			WAnimation.AnimationEffect... effects) {
		setEffects(EnumSet.of(effect, effects));
	}

	/**
	 * Returns animation effects.
	 */
	public EnumSet<WAnimation.AnimationEffect> getEffects() {
		return this.effects_;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * Returns <code>true</code> if the transitions are exactly the same.
	 */
	public boolean equals(WAnimation animation) {
		return animation.effects_.equals(this.effects_)
				&& animation.duration_ == this.duration_;
	}

	/**
	 * Sets the duration.
	 * <p>
	 * The default animation duration is 250 ms.
	 * <p>
	 * 
	 * @see WAnimation#getDuration()
	 */
	public void setDuration(int msecs) {
		this.duration_ = msecs;
	}

	/**
	 * Returns the duration.
	 * <p>
	 * 
	 * @see WAnimation#setDuration(int msecs)
	 */
	public int getDuration() {
		return this.duration_;
	}

	// public void setTimingFunction(WAnimation.TimingFunction function) ;
	public WAnimation.TimingFunction getTimingFunction() {
		return this.timing_;
	}

	/**
	 * Returns whether the animation is empty.
	 */
	public boolean isEmpty() {
		return this.duration_ == 0 || !!this.effects_.isEmpty();
	}

	private EnumSet<WAnimation.AnimationEffect> effects_;
	private WAnimation.TimingFunction timing_;
	private int duration_;
}
