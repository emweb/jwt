package eu.webtoolkit.jwt;


/**
 * Class that plays a sound
 * 
 * 
 * Simple interface to play a sound asynchonously. It is intended as a simple
 * way to play event sounds rather than to build a media center.
 * <p>
 * This class uses Flash to play sounds in the web browser. Future releases may
 * use the HTML5 tags to play audio in the browser. The appropriate file formats
 * depend on the Flash player or the browser support: mp3 and wav are probably
 * appropriate formats.
 * <p>
 * This class uses WtSoundManager.swf, which must be placed in the resources
 * directory. For laoding the flash object, swfobject.js is required in the
 * resources folder.
 * <p>
 * Usage example: <code>
 WSound *s = new WSound(&quot;djing.mp3&quot;, parent); <br> 
 s-&gt;setLoops(3); <br> 
 s-&gt;play(); <br> 
 playButton-&gt;clicked().connect(SLOT(s, WSound::play)); <br> 
 stopButton-&gt;clicked().connect(SLOT(s, WSound::stop));
</code>
 * <p>
 * Note: we occasionally encountered problems with playing sound using Flash on
 * Internet Explorer.
 */
public class WSound extends WObject {
	/**
	 * Construct a sound object that will play the given URL.
	 */
	public WSound(String url, WObject parent) {
		super(parent);
		this.url_ = url;
		this.loops_ = 1;
		this.sm_ = WApplication.getInstance().getSoundManager();
		this.sm_.add(this);
	}

	public WSound(String url) {
		this(url, (WObject) null);
	}

	/**
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
	 * 
	 * When {@link WSound#play()} is called, the sound will be played for this
	 * amount of loops.
	 */
	public int getLoops() {
		return this.loops_;
	}

	/**
	 * Sets the amount of times the sound has to be played for every invocation
	 * of {@link WSound#play()}.
	 * 
	 * The behavior is undefined for negative loop numbers.
	 */
	public void setLoops(int number) {
		this.loops_ = number;
	}

	/**
	 * Start asynchonous playback of the sound.
	 * 
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
	 * 
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
