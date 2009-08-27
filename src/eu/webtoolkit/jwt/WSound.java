/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Utility class to play a sound
 * <p>
 * 
 * This class provides a way to play a sound asynchonously (if the browser
 * supports this). It is intended as a simple way to play event sounds (not
 * quite for a media center).
 * <p>
 * The current implementation uses Adobe Flash to play sounds in the web
 * browser. Future releases may use the HTML5 tags to play audio in the browser.
 * The appropriate file formats depend on the Flash player or the browser
 * support, but MP3 or WAV are most widely supported.
 * <p>
 * This class uses <i>resourcesURL</i>&quot;WtSoundManager.swf&quot;, a flash
 * object, and <i>resourcesURL</i>&quot;swfobject.js&quot;, a companion
 * JavaScript library, which are both distributed with JWt in the resources
 * folder. <i>resourcesURL</i> is the configuration property that locates the
 * JWt resources/ folder inside your docroot.
 * <p>
 * Usage example: <code>
 WSound *s = new WSound(&quot;djing.mp3&quot;, parent); <br> 
 s-&gt;setLoops(3); <br> 
 s-&gt;play(); <br> 
 playButton-&gt;clicked().connect(SLOT(s, WSound::play)); <br> 
 stopButton-&gt;clicked().connect(SLOT(s, WSound::stop));
</code>
 * <p>
 * <p>
 * <i><b>Note:</b>The current implementation has occasional problems with
 * playing sound on Internet Explorer. </i>
 * </p>
 */
public class WSound extends WObject {
	/**
	 * Constructs a sound object that will play the given URL.
	 */
	public WSound(String url, WObject parent) {
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
	public WSound(String url) {
		this(url, (WObject) null);
	}

	/**
	 * Destructor.
	 * <p>
	 * The destructor calls {@link WSound#stop()} and unloads the sound object.
	 */
	public void destroy() {
		this.stop();
		this.sm_.remove(this);
	}

	/**
	 * Returns the url played by this class.
	 */
	public String getUrl() {
		return this.url_;
	}

	/**
	 * Returns the configured number of loops for this object.
	 * <p>
	 * When {@link WSound#play()} is called, the sound will be played for this
	 * amount of loops.
	 */
	public int getLoops() {
		return this.loops_;
	}

	/**
	 * Sets the amount of times the sound has to be played for every invocation
	 * of {@link WSound#play()}.
	 * <p>
	 * The behavior is undefined for negative loop numbers.
	 */
	public void setLoops(int number) {
		this.loops_ = number;
	}

	/**
	 * Start asynchronous playback of the sound.
	 * <p>
	 * This method returns immediately. It will cause the song to be played for
	 * the configured amount of loops.
	 * <p>
	 * The behavior of {@link WSound#play()} when a sound is already playing
	 * depends on the method to play songs in the browser (Flash/HTML5). It may
	 * be mixed with an already playing instance, or replace the previous
	 * instance. It is recommended to call {@link WSound#stop()} before
	 * {@link WSound#play()} if you want to avoid mixing multiple instances of a
	 * single {@link WSound} object.
	 */
	public void play() {
		this.sm_.play(this, this.loops_);
	}

	/**
	 * Stops playback of the sound.
	 * <p>
	 * This method returns immediately. It causes the playback of this
	 * {@link WSound} to be terminated.
	 */
	public void stop() {
		this.sm_.stop(this);
	}

	private String url_;
	private int loops_;
	private SoundManager sm_;
}
