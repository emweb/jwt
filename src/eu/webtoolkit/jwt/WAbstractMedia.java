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
 * Abstract baseclass for native media elements.
 * <p>
 * 
 * This class is an abstract base class for HTML5 media elements (&lt;audio&gt;,
 * &lt;video&gt;).
 */
public abstract class WAbstractMedia extends WInteractWidget {
	private static Logger logger = LoggerFactory
			.getLogger(WAbstractMedia.class);

	/**
	 * Enumeration for playback options.
	 */
	public enum Options {
		/**
		 * Start playing as soon as the video is loaded.
		 */
		Autoplay(1),
		/**
		 * Enable loop mode.
		 */
		Loop(2),
		/**
		 * Show video controls in the browser.
		 */
		Controls(4);

		private int value;

		Options(int value) {
			this.value = value;
		}

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return value;
		}
	}

	/**
	 * Enumeration for preload strategy.
	 */
	public enum PreloadMode {
		/**
		 * Hints that the user will probably not play the video.
		 */
		PreloadNone,
		/**
		 * Hints that it is ok to download the entire resource.
		 */
		PreloadAuto,
		/**
		 * Hints that retrieving metadata is a good option.
		 */
		PreloadMetadata;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * The HTML5 media ReadyState flag indicates how much of the media is
	 * loaded.
	 * <p>
	 * This is often used in conjunction with the &gt; operator, e.g.
	 * {@link WAbstractMedia#getReadyState() getReadyState()} &gt;
	 * HaveCurrentData
	 */
	public enum ReadyState {
		/**
		 * No information available.
		 */
		HaveNothing(0),
		/**
		 * Metadata loaded: duration, width, height.
		 */
		HaveMetaData(1),
		/**
		 * Data at playback position is available.
		 */
		HaveCurrentData(2),
		/**
		 * Have data to play for a while.
		 */
		HaveFutureData(3),
		/**
		 * Enough to reach the end without stalling.
		 */
		HaveEnoughData(4);

		private int value;

		ReadyState(int value) {
			this.value = value;
		}

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return value;
		}
	}

	/**
	 * Consctructor for a media widget.
	 * <p>
	 * A freshly constructed media widget has no options set, no media sources,
	 * and has preload mode set to PreloadAuto.
	 */
	public WAbstractMedia(WContainerWidget parent) {
		super(parent);
		this.sources_ = new ArrayList<WAbstractMedia.Source>();
		this.sourcesRendered_ = 0;
		this.mediaId_ = "";
		this.flags_ = EnumSet.noneOf(WAbstractMedia.Options.class);
		this.preloadMode_ = WAbstractMedia.PreloadMode.PreloadAuto;
		this.alternative_ = null;
		this.flagsChanged_ = false;
		this.preloadChanged_ = false;
		this.sourcesChanged_ = false;
		this.playing_ = false;
		this.volume_ = -1;
		this.current_ = -1;
		this.duration_ = -1;
		this.ended_ = false;
		this.readyState_ = WAbstractMedia.ReadyState.HaveNothing;
		this.setInline(false);
		this.setFormObject(true);
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WAbstractMedia.js", wtjs1());
		this.setJavaScriptMember(" WAbstractMedia",
				"new Wt3_3_0.WAbstractMedia(" + app.getJavaScriptClass() + ","
						+ this.getJsRef() + ");");
	}

	/**
	 * Consctructor for a media widget.
	 * <p>
	 * Calls {@link #WAbstractMedia(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WAbstractMedia() {
		this((WContainerWidget) null);
	}

	public void remove() {
		for (int i = 0; i < this.sources_.size(); ++i) {
			;
		}
		super.remove();
	}

	/**
	 * Set the media element options.
	 * <p>
	 * 
	 * @see WAbstractMedia.Options
	 */
	public void setOptions(EnumSet<WAbstractMedia.Options> flags) {
		this.flags_ = EnumSet.copyOf(flags);
		this.flagsChanged_ = true;
		this.repaint();
	}

	/**
	 * Set the media element options.
	 * <p>
	 * Calls {@link #setOptions(EnumSet flags) setOptions(EnumSet.of(flag,
	 * flags))}
	 */
	public final void setOptions(WAbstractMedia.Options flag,
			WAbstractMedia.Options... flags) {
		setOptions(EnumSet.of(flag, flags));
	}

	/**
	 * Retrieve the configured options.
	 */
	public EnumSet<WAbstractMedia.Options> getOptions() {
		return this.flags_;
	}

	/**
	 * Set the preload mode.
	 */
	public void setPreloadMode(WAbstractMedia.PreloadMode mode) {
		this.preloadMode_ = mode;
		this.preloadChanged_ = true;
		this.repaint();
	}

	/**
	 * Retrieve the preload mode.
	 */
	public WAbstractMedia.PreloadMode getPreloadMode() {
		return this.preloadMode_;
	}

	/**
	 * Removes all source elements.
	 * <p>
	 * This method can be used to remove all media sources. Afterward, you may
	 * add new media sources with calls to
	 * {@link WAbstractMedia#addSource(WLink link, String type, String media)
	 * addSource()}.
	 * <p>
	 * Use this to reuse a {@link WAbstractMedia} instantiation to play
	 * something else.
	 */
	public void clearSources() {
		for (int i = 0; i < this.sources_.size(); ++i) {
			;
		}
		this.sources_.clear();
		this.repaint();
	}

	/**
	 * Add a media source.
	 * <p>
	 * This method specifies a media source (which may be a URL or dynamic
	 * resource). You may add as many media sources as you want. The browser
	 * will select the appropriate media stream to display to the user.
	 * <p>
	 * This method specifies a media source using the URL, the mime type, and
	 * the media attribute. HTML allows for empty type and media attributes.
	 */
	public void addSource(WLink link, String type, String media) {
		this.sources_.add(new WAbstractMedia.Source(this, link, type, media));
		this.sourcesChanged_ = true;
		this.repaint();
	}

	/**
	 * Add a media source.
	 * <p>
	 * Calls {@link #addSource(WLink link, String type, String media)
	 * addSource(link, "", "")}
	 */
	public final void addSource(WLink link) {
		addSource(link, "", "");
	}

	/**
	 * Add a media source.
	 * <p>
	 * Calls {@link #addSource(WLink link, String type, String media)
	 * addSource(link, type, "")}
	 */
	public final void addSource(WLink link, String type) {
		addSource(link, type, "");
	}

	/**
	 * Content to be shown when media cannot be played.
	 * <p>
	 * As not all browsers are HTML5 compliant, it is a good idea to provide
	 * fallback options when the media cannot be displayed. If the media can be
	 * played by the browser, the alternative content will be suppressed.
	 * <p>
	 * The two reasons to display the alternative content are (1) the media tag
	 * is not supported, or (2) the media tag is supported, but none of the
	 * media sources are supported by the browser. In the first case, fall-back
	 * is automatic and does not rely on JavaScript in the browser; in the
	 * latter case, JavaScript is required to make the fallback work.
	 * <p>
	 * The alternative content can be any widget: you can set it to an
	 * alternative media player (QuickTime, Flash, ...), show a Flash movie, an
	 * animated gif, a text, a poster image, ...
	 */
	public void setAlternativeContent(WWidget alternative) {
		if (this.alternative_ != null) {
			if (this.alternative_ != null)
				this.alternative_.remove();
		}
		this.alternative_ = alternative;
		if (this.alternative_ != null) {
			this.addChild(this.alternative_);
		}
	}

	/**
	 * Invoke {@link WAbstractMedia#play() play()} on the media element.
	 * <p>
	 * JavaScript must be available for this function to work.
	 */
	public void play() {
		this
				.doJavaScript("jQuery.data(" + this.getJsRef()
						+ ", 'obj').play();");
	}

	/**
	 * Invoke {@link WAbstractMedia#pause() pause()} on the media element.
	 * <p>
	 * JavaScript must be available for this function to work.
	 */
	public void pause() {
		this.doJavaScript("jQuery.data(" + this.getJsRef()
				+ ", 'obj').pause();}");
	}

	/**
	 * Returns whether the media is playing.
	 */
	public boolean isPlaying() {
		return this.playing_;
	}

	/**
	 * Returns the media&apos;s readyState.
	 */
	public WAbstractMedia.ReadyState getReadyState() {
		return this.readyState_;
	}

	/**
	 * Event signal emitted when playback has begun.
	 * <p>
	 * This event fires when play was invoked, or when the media element starts
	 * playing because the Autoplay option was provided.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal playbackStarted() {
		return this.voidEventSignal(PLAYBACKSTARTED_SIGNAL, true);
	}

	/**
	 * Event signal emitted when the playback has paused.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal playbackPaused() {
		return this.voidEventSignal(PLAYBACKPAUSED_SIGNAL, true);
	}

	/**
	 * Event signal emitted when the playback stopped because the end of the
	 * media was reached.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal ended() {
		return this.voidEventSignal(ENDED_SIGNAL, true);
	}

	/**
	 * Event signal emitted when the current playback position has changed.
	 * <p>
	 * This event is fired when the playback position has changed, both when the
	 * media is in a normal playing mode, but also when it has changed
	 * discontinuously because of another reason.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal timeUpdated() {
		return this.voidEventSignal(TIMEUPDATED_SIGNAL, true);
	}

	/**
	 * Event signal emitted when the playback volume has changed.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>When JavaScript is disabled, the signal will never fire.
	 * </i>
	 * </p>
	 */
	public EventSignal volumeChanged() {
		return this.voidEventSignal(VOLUMECHANGED_SIGNAL, true);
	}

	/**
	 * Returns the JavaScript reference to the media object, or null.
	 * <p>
	 * It is possible, for browser compatibility reasons, that
	 * {@link WWidget#getJsRef() WWidget#getJsRef()} is not the media element.
	 * {@link WAbstractMedia#getJsMediaRef() getJsMediaRef()} is guaranteed to
	 * be an expression that evaluates to the media object. This expression may
	 * yield null, if the video object is not rendered at all (e.g. on older
	 * versions of Internet Explorer).
	 */
	public String getJsMediaRef() {
		if (this.mediaId_.length() == 0) {
			return "null";
		} else {
			return "Wt3_3_0.getElement('" + this.mediaId_ + "')";
		}
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		if (this.mediaId_.length() != 0) {
			DomElement media = DomElement.getForUpdate(this.mediaId_,
					DomElementType.DomElement_DIV);
			this.updateMediaDom(media, false);
			if (this.sourcesChanged_) {
				for (int i = 0; i < this.sourcesRendered_; ++i) {
					media.callJavaScript("Wt3_3_0.remove('" + this.mediaId_
							+ "s" + String.valueOf(i) + "');", true);
				}
				this.sourcesRendered_ = 0;
				for (int i = 0; i < this.sources_.size(); ++i) {
					DomElement src = DomElement
							.createNew(DomElementType.DomElement_SOURCE);
					src.setId(this.mediaId_ + "s" + String.valueOf(i));
					this.renderSource(src, this.sources_.get(i),
							i + 1 >= this.sources_.size());
					media.addChild(src);
				}
				this.sourcesRendered_ = this.sources_.size();
				this.sourcesChanged_ = false;
				media.callJavaScript(this.getJsMediaRef() + ".load();");
			}
			result.add(media);
		}
		super.getDomChanges(result, app);
	}

	DomElement createDomElement(WApplication app) {
		DomElement result = null;
		if (this.isInLayout()) {
			this.setJavaScriptMember(WT_RESIZE_JS, "function() {}");
		}
		if (app.getEnvironment().agentIsIElt(9)
				|| app.getEnvironment().getAgent() == WEnvironment.UserAgent.MobileWebKitAndroid) {
			result = DomElement.createNew(DomElementType.DomElement_DIV);
			if (this.alternative_ != null) {
				result.addChild(this.alternative_.createSDomElement(app));
			}
		} else {
			DomElement media = this.createMediaDomElement();
			DomElement wrap = null;
			if (this.isInLayout()) {
				media.setProperty(Property.PropertyStylePosition, "absolute");
				media.setProperty(Property.PropertyStyleLeft, "0");
				media.setProperty(Property.PropertyStyleRight, "0");
				wrap = DomElement.createNew(DomElementType.DomElement_DIV);
				wrap.setProperty(Property.PropertyStylePosition, "relative");
			}
			result = wrap != null ? wrap : media;
			if (wrap != null) {
				this.mediaId_ = this.getId() + "_media";
				media.setId(this.mediaId_);
			} else {
				this.mediaId_ = this.getId();
			}
			this.updateMediaDom(media, true);
			for (int i = 0; i < this.sources_.size(); ++i) {
				DomElement src = DomElement
						.createNew(DomElementType.DomElement_SOURCE);
				src.setId(this.mediaId_ + "s" + String.valueOf(i));
				this.renderSource(src, this.sources_.get(i),
						i + 1 >= this.sources_.size());
				media.addChild(src);
			}
			this.sourcesRendered_ = this.sources_.size();
			this.sourcesChanged_ = false;
			if (wrap != null) {
				wrap.addChild(media);
			}
		}
		if (this.isInLayout()) {
			StringWriter ss = new StringWriter();
			ss.append("function(self, w, h) {");
			if (this.mediaId_.length() != 0) {
				ss
						.append("v="
								+ this.getJsMediaRef()
								+ ";if (v) {if (w >= 0) v.setAttribute('width', w);if (h >= 0) v.setAttribute('height', h);}");
			}
			if (this.alternative_ != null) {
				ss.append("a=" + this.alternative_.getJsRef() + ";if (a && a.")
						.append(WT_RESIZE_JS).append(")a.")
						.append(WT_RESIZE_JS).append("(a, w, h);");
			}
			ss.append("}");
			this.setJavaScriptMember(WT_RESIZE_JS, ss.toString());
		}
		this.setId(result, app);
		this.updateDom(result, true);
		if (this.isInLayout()) {
			result.setEvent(PLAYBACKSTARTED_SIGNAL, "");
			result.setEvent(PLAYBACKPAUSED_SIGNAL, "");
			result.setEvent(ENDED_SIGNAL, "");
			result.setEvent(TIMEUPDATED_SIGNAL, "");
			result.setEvent(VOLUMECHANGED_SIGNAL, "");
		}
		this.setJavaScriptMember("mediaId", "'" + this.mediaId_ + "'");
		return result;
	}

	void updateMediaDom(DomElement element, boolean all) {
		if (all && this.alternative_ != null) {
			element
					.setAttribute(
							"onerror",
							"if(event.target.error && event.target.error.code==event.target.error.MEDIA_ERR_SRC_NOT_SUPPORTED){while (this.hasChildNodes())if (Wt3_3_0.hasTag(this.firstChild,'SOURCE')){this.removeChild(this.firstChild);}else{this.parentNode.insertBefore(this.firstChild, this);}this.style.display= 'none';}");
		}
		if (all || this.flagsChanged_) {
			if (!all
					|| !EnumUtils.mask(this.flags_,
							WAbstractMedia.Options.Controls).isEmpty()) {
				element.setAttribute("controls", !EnumUtils.mask(this.flags_,
						WAbstractMedia.Options.Controls).isEmpty() ? "controls"
						: "");
			}
			if (!all
					|| !EnumUtils.mask(this.flags_,
							WAbstractMedia.Options.Autoplay).isEmpty()) {
				element.setAttribute("autoplay", !EnumUtils.mask(this.flags_,
						WAbstractMedia.Options.Autoplay).isEmpty() ? "autoplay"
						: "");
			}
			if (!all
					|| !EnumUtils
							.mask(this.flags_, WAbstractMedia.Options.Loop)
							.isEmpty()) {
				element.setAttribute("loop", !EnumUtils.mask(this.flags_,
						WAbstractMedia.Options.Loop).isEmpty() ? "loop" : "");
			}
		}
		if (all || this.preloadChanged_) {
			switch (this.preloadMode_) {
			case PreloadNone:
				element.setAttribute("preload", "none");
				break;
			default:
			case PreloadAuto:
				element.setAttribute("preload", "auto");
				break;
			case PreloadMetadata:
				element.setAttribute("preload", "metadata");
				break;
			}
		}
		this.updateEventSignals(element, all);
		if (all) {
			if (this.alternative_ != null) {
				element.addChild(this.alternative_
						.createSDomElement(WApplication.getInstance()));
			}
		}
		this.flagsChanged_ = this.preloadChanged_ = false;
	}

	abstract DomElement createMediaDomElement();

	void setFormData(WObject.FormData formData) {
		if (!(formData.values.length == 0)) {
			List<String> attributes = new ArrayList<String>();
			attributes = new ArrayList<String>(Arrays.asList(formData.values[0]
					.split(";")));
			if (attributes.size() == 6) {
				try {
					this.volume_ = Double.parseDouble(attributes.get(0));
				} catch (RuntimeException e) {
					this.volume_ = -1;
				}
				try {
					this.current_ = Double.parseDouble(attributes.get(1));
				} catch (RuntimeException e) {
					this.current_ = -1;
				}
				try {
					this.duration_ = Double.parseDouble(attributes.get(2));
				} catch (RuntimeException e) {
					this.duration_ = -1;
				}
				this.playing_ = attributes.get(3).equals("0");
				this.ended_ = attributes.get(4).equals("1");
				try {
					this.readyState_ = intToReadyState(Integer
							.parseInt(attributes.get(5)));
				} catch (RuntimeException e) {
					throw new WException("WAbstractMedia: error parsing: "
							+ formData.values[0] + ": " + e.toString());
				}
			} else {
				throw new WException("WAbstractMedia: error parsing: "
						+ formData.values[0]);
			}
		}
	}

	static class Source extends WObject {
		private static Logger logger = LoggerFactory.getLogger(Source.class);

		public Source(WAbstractMedia parent, WLink link, String type,
				String media) {
			super();
			this.parent = parent;
			this.connection = new AbstractSignal.Connection();
			this.type = type;
			this.media = media;
			this.link = link;
			if (link.getType() == WLink.Type.Resource) {
				this.connection = link.getResource().dataChanged().addListener(
						this, new Signal.Listener() {
							public void trigger() {
								WAbstractMedia.Source.this.resourceChanged();
							}
						});
			}
		}

		public void resourceChanged() {
			this.parent.sourcesChanged_ = true;
			this.parent.repaint();
		}

		public WAbstractMedia parent;
		public AbstractSignal.Connection connection;
		public String type;
		public String media;
		public WLink link;
	}

	private void renderSource(DomElement element, WAbstractMedia.Source source,
			boolean isLast) {
		element.setAttribute("src", resolveRelativeUrl(source.link.getUrl()));
		if (!source.type.equals("")) {
			element.setAttribute("type", source.type);
		}
		if (!source.media.equals("")) {
			element.setAttribute("media", source.media);
		}
		if (isLast && this.alternative_ != null) {
			element
					.setAttribute(
							"onerror",
							"var media = this.parentNode;if(media){while (media && media.children.length)if (Wt3_3_0.hasTag(media.firstChild,'SOURCE')){media.removeChild(media.firstChild);}else{media.parentNode.insertBefore(media.firstChild, media);}media.style.display= 'none';}");
		} else {
			element.setAttribute("onerror", "");
		}
	}

	private List<WAbstractMedia.Source> sources_;
	private int sourcesRendered_;
	private String mediaId_;
	EnumSet<WAbstractMedia.Options> flags_;
	private WAbstractMedia.PreloadMode preloadMode_;
	private WWidget alternative_;
	private boolean flagsChanged_;
	private boolean preloadChanged_;
	private boolean sourcesChanged_;
	private boolean playing_;
	private double volume_;
	private double current_;
	private double duration_;
	private boolean ended_;
	private WAbstractMedia.ReadyState readyState_;
	private static String PLAYBACKSTARTED_SIGNAL = "play";
	private static String PLAYBACKPAUSED_SIGNAL = "pause";
	private static String ENDED_SIGNAL = "ended";
	private static String TIMEUPDATED_SIGNAL = "timeupdate";
	private static String VOLUMECHANGED_SIGNAL = "volumechange";

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WAbstractMedia",
				"function(d,b){function c(){if(b.mediaId){var a=$(\"#\"+b.mediaId).get(0);if(a)return\"\"+a.volume+\";\"+a.currentTime+\";\"+(a.readyState>=1?a.duration:0)+\";\"+(a.paused?\"1\":\"0\")+\";\"+(a.ended?\" 1\":\"0\")+\";\"+a.readyState}return null}jQuery.data(b,\"obj\",this);this.play=function(){if(b.mediaId){var a=$(\"#\"+b.mediaId).get(0);if(a){a.play();return}}if(b.alternativeId)(a=$(\"#\"+b.alternativeId).get(0))&&a.WtPlay&&a.WtPlay()};this.pause=function(){if(b.mediaId){var a= $(\"#\"+b.mediaId).get(0);if(a){a.pause();return}}if(b.alternativeId)(a=$(\"#\"+b.alternativeId).get(0))&&a.WtPlay&&a.WtPause()};b.wtEncodeValue=c}");
	}

	static WAbstractMedia.ReadyState intToReadyState(int i) {
		switch (i) {
		case 0:
			return WAbstractMedia.ReadyState.HaveNothing;
		case 1:
			return WAbstractMedia.ReadyState.HaveMetaData;
		case 2:
			return WAbstractMedia.ReadyState.HaveCurrentData;
		case 3:
			return WAbstractMedia.ReadyState.HaveFutureData;
		case 4:
			return WAbstractMedia.ReadyState.HaveEnoughData;
		default:
			throw new WException("Invalid readystate");
		}
	}
}
