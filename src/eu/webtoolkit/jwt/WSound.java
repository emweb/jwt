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
 * A value class to play a sound effect.
 * <p>
 * 
 * This class provides a way to play an MP3 sound asynchonously (if the browser
 * supports this). It is intended as a simple way to play event sounds (not
 * quite for a media center).
 * <p>
 * This class uses a {@link WMediaPlayer} to play the sound (using HTML
 * &lt;audio&gt; or a flash player).
 */
public class WSound extends WObject {
	private static Logger logger = LoggerFactory.getLogger(WSound.class);

	/**
	 * Constructs a sound object that will play the given URL.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>As of JWt 3.1.10, the <code>url</code> must specify an
	 * MP3 file. </i>
	 * </p>
	 */
	public WSound(final String url, WObject parent) {
		super(parent);
		this.url_ = url;
		this.loops_ = 1;
		this.sm_ = WApplication.getInstance().getSoundManager();
		this.sm_.add(this);
	}

	/**
	 * Constructs a sound object that will play the given URL.
	 * <p>
	 * Calls {@link #WSound(String url, WObject parent) this(url,
	 * (WObject)null)}
	 */
	public WSound(final String url) {
		this(url, (WObject) null);
	}

	/**
	 * Returns the sound url.
	 * <p>
	 * 
	 * @see WSound#WSound(String url, WObject parent)
	 */
	public String getUrl() {
		return this.url_;
	}

	/**
	 * Sets the amount of times the sound has to be repeated.
	 * <p>
	 * A call to {@link WSound#play() play()} will play the sound
	 * <code>number</code> of times. The default value is 1 (no repeats).
	 */
	public void setLoops(int number) {
		this.loops_ = number;
	}

	/**
	 * Returns the configured number of repeats.
	 * <p>
	 * <i>{@link WSound#setLoops(int number) setLoops()}</i>
	 */
	public int getLoops() {
		return this.loops_;
	}

	/**
	 * Start asynchronous playback of the sound.
	 * <p>
	 * This method returns immediately. It will cause the sound to be played for
	 * the configured amount of {@link WSound#getLoops() getLoops()}.
	 * <p>
	 * The behavior of {@link WSound#play() play()} when a sound is already
	 * playing is undefind: it may be intermixed, sequentially queued, or a
	 * current playing sound may be stopped. It is recommended to call
	 * {@link WSound#stop() stop()} before {@link WSound#play() play()} if you
	 * want to avoid mixing multiple instances of a single {@link WSound}
	 * object.
	 */
	public void play() {
		this.sm_.play(this, this.loops_);
	}

	/**
	 * Stops playback of the sound.
	 * <p>
	 * This method returns immediately. It causes the current playback (if any)
	 * of the sound to be stopped.
	 */
	public void stop() {
		this.sm_.stop(this);
	}

	private String url_;
	private int loops_;
	private SoundManager sm_;
}
