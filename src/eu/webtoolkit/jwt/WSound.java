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
	 * Constructs a sound object.
	 * <p>
	 * 
	 * @see WSound#addSource(WMediaPlayer.Encoding encoding, WLink link)
	 */
	public WSound(WObject parent) {
		super(parent);
		this.media_ = new ArrayList<WSound.Source>();
		this.loops_ = 1;
	}

	/**
	 * Constructs a sound object.
	 * <p>
	 * Calls {@link #WSound(WObject parent) this((WObject)null)}
	 */
	public WSound() {
		this((WObject) null);
	}

	/**
	 * Constructs a sound object for an MP3 media source.
	 * <p>
	 * The <code>url</code> will be assumed to be an MP3 file.
	 * <p>
	 * 
	 * @see WSound#addSource(WMediaPlayer.Encoding encoding, WLink link)
	 */
	public WSound(final String url, WObject parent) {
		super(parent);
		this.media_ = new ArrayList<WSound.Source>();
		this.loops_ = 1;
		this.addSource(WMediaPlayer.Encoding.MP3, new WLink(url));
	}

	/**
	 * Constructs a sound object for an MP3 media source.
	 * <p>
	 * Calls {@link #WSound(String url, WObject parent) this(url,
	 * (WObject)null)}
	 */
	public WSound(final String url) {
		this(url, (WObject) null);
	}

	/**
	 * Constructs a sound object.
	 * <p>
	 * 
	 * @see WSound#addSource(WMediaPlayer.Encoding encoding, WLink link)
	 */
	public WSound(WMediaPlayer.Encoding encoding, final WLink link,
			WObject parent) {
		super(parent);
		this.media_ = new ArrayList<WSound.Source>();
		this.loops_ = 1;
		this.addSource(encoding, link);
	}

	/**
	 * Constructs a sound object.
	 * <p>
	 * Calls
	 * {@link #WSound(WMediaPlayer.Encoding encoding, WLink link, WObject parent)
	 * this(encoding, link, (WObject)null)}
	 */
	public WSound(WMediaPlayer.Encoding encoding, final WLink link) {
		this(encoding, link, (WObject) null);
	}

	/**
	 * Adds a media source.
	 * <p>
	 * You may add multiple media sources (with different encodings) to allow
	 * the file to be played in more browsers without needing Flash plugins.
	 */
	public void addSource(WMediaPlayer.Encoding encoding, final WLink link) {
		this.media_.add(new WSound.Source(encoding, link));
		WApplication.getInstance().getSoundManager().add(this);
	}

	/**
	 * Returns the media source (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated use getSource(WMediaPlayer::Encoding) instead.
	 */
	public String getUrl() {
		return this.getSource(WMediaPlayer.Encoding.MP3).getUrl();
	}

	/**
	 * Returns the media source.
	 * <p>
	 * This returns the link set for a specific encoding, or an empty link if no
	 * URL was set for that encoding.
	 */
	public WLink getSource(WMediaPlayer.Encoding encoding) {
		for (int i = 0; i < this.media_.size(); ++i) {
			if (this.media_.get(i).encoding == encoding) {
				return this.media_.get(i).link;
			}
		}
		return new WLink();
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
		WApplication.getInstance().getSoundManager().play(this, this.loops_);
	}

	/**
	 * Stops playback of the sound.
	 * <p>
	 * This method returns immediately. It causes the current playback (if any)
	 * of the sound to be stopped.
	 */
	public void stop() {
		WApplication.getInstance().getSoundManager().stop(this);
	}

	static class Source {
		private static Logger logger = LoggerFactory.getLogger(Source.class);

		public Source(WMediaPlayer.Encoding anEncoding, WLink aLink) {
			this.encoding = anEncoding;
			this.link = aLink;
		}

		public WMediaPlayer.Encoding encoding;
		public WLink link;
	}

	List<WSound.Source> media_;
	private int loops_;
}
