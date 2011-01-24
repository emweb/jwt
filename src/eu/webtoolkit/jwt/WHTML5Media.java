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
 * Abstract baseclass for HTML5 media elements.
 * <p>
 * 
 * This class is an abstract base class for HTML5 media elements (audio, video).
 */
public abstract class WHTML5Media extends WInteractWidget {
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
	 * {@link WHTML5Media#getReadyState() getReadyState()} &gt;
	 * HAVE_CURRENT_DATA
	 */
	public enum ReadyState {
		/**
		 * No information available.
		 */
		HAVE_NOTHING(0),
		/**
		 * Metadata loaded: duration, width, height.
		 */
		HAVE_METADATA(1),
		/**
		 * Data at playback position is available.
		 */
		HAVE_CURRENT_DATA(2),
		/**
		 * Have data to play for a while.
		 */
		HAVE_FUTURE_DATA(3),
		/**
		 * Enough to reach the end without stalling (est).
		 */
		HAVE_ENOUGH_DATA(4);

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
	 * Consctructor for a HTML5 media widget.
	 * <p>
	 * A freshly constructed HTML5Video widget has no options set, no media
	 * sources, and has preload mode set to PreloadAuto.
	 */
	public WHTML5Media(WContainerWidget parent) {
		super(parent);
		this.sources_ = new ArrayList<WHTML5Media.Source>();
		this.sourcesRendered_ = 0;
		this.mediaId_ = "";
		this.flags_ = EnumSet.noneOf(WHTML5Media.Options.class);
		this.preloadMode_ = WHTML5Media.PreloadMode.PreloadAuto;
		this.alternative_ = null;
		this.flagsChanged_ = false;
		this.preloadChanged_ = false;
		this.sourcesChanged_ = false;
		this.playing_ = false;
		this.volume_ = -1;
		this.current_ = -1;
		this.duration_ = -1;
		this.ended_ = false;
		this.readyState_ = WHTML5Media.ReadyState.HAVE_NOTHING;
		this.setInline(false);
		this.setFormObject(true);
		WApplication app = WApplication.getInstance();
		String THIS_JS = "js/WHTML5Media.js";
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
		this.doJavaScript("new Wt3_1_8.WHTML5Media(" + app.getJavaScriptClass()
				+ "," + this.getJsRef() + ");");
		this.setJavaScriptMember("WtPlay", "function() {jQuery.data("
				+ this.getJsRef() + ", 'obj').play();}");
		this.setJavaScriptMember("WtPause", "function() {jQuery.data("
				+ this.getJsRef() + ", 'obj').pause();}");
	}

	/**
	 * Consctructor for a HTML5 media widget.
	 * <p>
	 * Calls {@link #WHTML5Media(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WHTML5Media() {
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
	 * @see WHTML5Media.Options
	 */
	public void setOptions(EnumSet<WHTML5Media.Options> flags) {
		this.flags_ = EnumSet.copyOf(flags);
		this.flagsChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Set the media element options.
	 * <p>
	 * Calls {@link #setOptions(EnumSet flags) setOptions(EnumSet.of(flag,
	 * flags))}
	 */
	public final void setOptions(WHTML5Media.Options flag,
			WHTML5Media.Options... flags) {
		setOptions(EnumSet.of(flag, flags));
	}

	/**
	 * Retrieve the configured options.
	 */
	public EnumSet<WHTML5Media.Options> getOptions() {
		return this.flags_;
	}

	/**
	 * Set the preload mode.
	 */
	public void setPreloadMode(WHTML5Media.PreloadMode mode) {
		this.preloadMode_ = mode;
		this.preloadChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Retrieve the preload mode.
	 */
	public WHTML5Media.PreloadMode getPreloadMode() {
		return this.preloadMode_;
	}

	/**
	 * Removes all source elements.
	 * <p>
	 * This method can be used to remove all media sources. Afterward, you may
	 * add new media sources with calls to
	 * {@link WHTML5Media#addSource(String url, String type, String media)
	 * addSource()}.
	 * <p>
	 * Use this to reuse a {@link WHTML5Media} instantiation to play something
	 * else.
	 */
	public void clearSources() {
		for (int i = 0; i < this.sources_.size(); ++i) {
			;
		}
		this.sources_.clear();
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Add a media source.
	 * <p>
	 * This method specifies a media source. You may add as many media sources
	 * as you want. The browser will select the appropriate media stream to
	 * display to the user.
	 * <p>
	 * This method specifies a media source using the URL, the mime type, and
	 * the media attribute. HTML allows for empty type and media attributes.
	 */
	public void addSource(String url, String type, String media) {
		this.sources_.add(new WHTML5Media.Source(url, type, media));
		this.sourcesChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Add a media source.
	 * <p>
	 * Calls {@link #addSource(String url, String type, String media)
	 * addSource(url, "", "")}
	 */
	public final void addSource(String url) {
		addSource(url, "", "");
	}

	/**
	 * Add a media source.
	 * <p>
	 * Calls {@link #addSource(String url, String type, String media)
	 * addSource(url, type, "")}
	 */
	public final void addSource(String url, String type) {
		addSource(url, type, "");
	}

	/**
	 * Add a media source.
	 * <p>
	 * This method specifies a media source. You may add as many media sources
	 * as you want. The browser will select the appropriate media stream to
	 * display to the user.
	 * <p>
	 * This method specifies a media source using the URL, the mime type, and
	 * the media attribute. HTML allows for empty type and media attributes.
	 */
	public void addSource(WResource resource, String type, String media) {
		this.sources_.add(new WHTML5Media.Source(this, resource, type, media));
		this.sourcesChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Add a media source.
	 * <p>
	 * Calls {@link #addSource(WResource resource, String type, String media)
	 * addSource(resource, "", "")}
	 */
	public final void addSource(WResource resource) {
		addSource(resource, "", "");
	}

	/**
	 * Add a media source.
	 * <p>
	 * Calls {@link #addSource(WResource resource, String type, String media)
	 * addSource(resource, type, "")}
	 */
	public final void addSource(WResource resource, String type) {
		addSource(resource, type, "");
	}

	/**
	 * Content to be shown when media cannot be played.
	 * <p>
	 * As the HTML5 media tags are not supported by all browsers, it is a good
	 * idea to provide fallback options when the media cannot be displayed. If
	 * the media can be played by the browser, the alternative content will be
	 * suppressed.
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
	 * Invoke {@link WHTML5Media#play() play()} on the media element.
	 * <p>
	 * JavaScript must be available for this function to work.
	 */
	public void play() {
		this.doJavaScript(this.getJsRef() + ".WtPlay();");
	}

	/**
	 * Invoke {@link WHTML5Media#pause() pause()} on the media element.
	 * <p>
	 * JavaScript must be available for this function to work.
	 */
	public void pause() {
		this.doJavaScript(this.getJsRef() + ".WtPause();");
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
	public WHTML5Media.ReadyState getReadyState() {
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
	 * {@link WWidget#getJsRef() WWidget#getJsRef()} is not the HTML5 media
	 * element. {@link WHTML5Media#getJsMediaRef() getJsMediaRef()} is
	 * guaranteed to be an expression that evaluates to the media object. This
	 * expression may yield null, if the video object is not rendered at all
	 * (e.g. on older versions of Internet Explorer).
	 */
	public String getJsMediaRef() {
		if (this.mediaId_.length() == 0) {
			return "null";
		} else {
			return "Wt3_1_8.getElement('" + this.mediaId_ + "')";
		}
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		if (this.mediaId_.length() != 0) {
			DomElement media = DomElement.getForUpdate(this.mediaId_,
					DomElementType.DomElement_DIV);
			this.updateMediaDom(media, false);
			if (this.sourcesChanged_) {
				for (int i = 0; i < this.sourcesRendered_; ++i) {
					media.callJavaScript("Wt3_1_8.remove('" + this.mediaId_
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
								+ ";if(v){v.setAttribute('width', w);v.setAttribute('height', h);}");
			}
			if (this.alternative_ != null) {
				ss.append("a=" + this.alternative_.getJsRef() + ";if(a && a.")
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
							"if(event.target.error && event.target.error.code==event.target.error.MEDIA_ERR_SRC_NOT_SUPPORTED){while (this.hasChildNodes())if (Wt3_1_8.hasTag(this.firstChild,'SOURCE')){this.removeChild(this.firstChild);}else{this.parentNode.insertBefore(this.firstChild, this);}this.style.display= 'none';}");
		}
		if (all || this.flagsChanged_) {
			if (!all
					|| !EnumUtils.mask(this.flags_,
							WHTML5Media.Options.Controls).isEmpty()) {
				element.setAttribute("controls", !EnumUtils.mask(this.flags_,
						WHTML5Media.Options.Controls).isEmpty() ? "controls"
						: "");
			}
			if (!all
					|| !EnumUtils.mask(this.flags_,
							WHTML5Media.Options.Autoplay).isEmpty()) {
				element.setAttribute("autoplay", !EnumUtils.mask(this.flags_,
						WHTML5Media.Options.Autoplay).isEmpty() ? "autoplay"
						: "");
			}
			if (!all
					|| !EnumUtils.mask(this.flags_, WHTML5Media.Options.Loop)
							.isEmpty()) {
				element.setAttribute("loop", !EnumUtils.mask(this.flags_,
						WHTML5Media.Options.Loop).isEmpty() ? "loop" : "");
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
				double volume;
				double current;
				double duration;
				boolean paused;
				boolean ended;
				int readystate;
				boolean error = false;
				try {
					volume = Double.parseDouble(attributes.get(0));
				} catch (NumberFormatException e) {
					error = true;
				}
				try {
					current = Double.parseDouble(attributes.get(1));
				} catch (NumberFormatException e) {
					error = true;
				}
				try {
					duration = Double.parseDouble(attributes.get(2));
				} catch (NumberFormatException e) {
					error = true;
				}
				try {
					paused = attributes.get(3).equals("1");
					this.playing_ = !paused;
				} catch (NumberFormatException e) {
					error = true;
				}
				try {
					ended = attributes.get(4).equals("1");
				} catch (NumberFormatException e) {
					error = true;
				}
				try {
					readystate = Integer.parseInt(attributes.get(5));
					this.readyState_ = WHTML5Media.ReadyState.HAVE_NOTHING;
					if (readystate <= WHTML5Media.ReadyState.HAVE_ENOUGH_DATA
							.getValue()
							&& readystate >= WHTML5Media.ReadyState.HAVE_NOTHING
									.getValue()) {
						this.readyState_ = intToReadyState(readystate);
					}
				} catch (NumberFormatException e) {
					error = true;
				}
				if (error) {
					WApplication.getInstance().log("error").append(
							"WHTML5Media: could not parse form data: ").append(
							formData.values[0]);
				}
			}
		}
	}

	static class Source extends WObject {
		public Source(WHTML5Media parent, WResource resource, String type,
				String media) {
			super();
			this.parent = parent;
			this.connection = new AbstractSignal.Connection();
			this.type = type;
			this.url = resource.getUrl();
			this.media = media;
			this.resource = resource;
			this.connection = resource.dataChanged().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WHTML5Media.Source.this.resourceChanged();
						}
					});
		}

		public Source(String url, String type, String media) {
			super();
			this.connection = new AbstractSignal.Connection();
			this.type = type;
			this.url = url;
			this.media = media;
		}

		public void resourceChanged() {
			this.url = this.resource.getUrl();
			this.parent.sourcesChanged_ = true;
			this.parent.repaint(EnumSet
					.of(RepaintFlag.RepaintPropertyAttribute));
		}

		public WHTML5Media parent;
		public AbstractSignal.Connection connection;
		public String type;
		public String url;
		public String media;
		public WResource resource;
	}

	private void renderSource(DomElement element, WHTML5Media.Source source,
			boolean isLast) {
		element.setAttribute("src", fixRelativeUrl(source.url));
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
							"var media = this.parentNode;if(media){while (media && media.children.length)if (Wt3_1_8.hasTag(media.firstChild,'SOURCE')){media.removeChild(media.firstChild);}else{media.parentNode.insertBefore(media.firstChild, media);}media.style.display= 'none';}");
		} else {
			element.setAttribute("onerror", "");
		}
	}

	private List<WHTML5Media.Source> sources_;
	private int sourcesRendered_;
	private String mediaId_;
	EnumSet<WHTML5Media.Options> flags_;
	private WHTML5Media.PreloadMode preloadMode_;
	private WWidget alternative_;
	private boolean flagsChanged_;
	private boolean preloadChanged_;
	private boolean sourcesChanged_;
	private boolean playing_;
	private double volume_;
	private double current_;
	private double duration_;
	private boolean ended_;
	private WHTML5Media.ReadyState readyState_;

	static String wtjs1(WApplication app) {
		return "Wt3_1_8.WHTML5Media = function(c,b){jQuery.data(b,\"obj\",this);this.play=function(){if(b.mediaId){var a=$(\"#\"+b.mediaId).get(0);if(a){a.play();return}}if(b.alternativeId)(a=$(\"#\"+b.alternativeId).get(0))&&a.WtPlay&&a.WtPlay()};this.pause=function(){if(b.mediaId){var a=$(\"#\"+b.mediaId).get(0);if(a){a.pause();return}}if(b.alternativeId)(a=$(\"#\"+b.alternativeId).get(0))&&a.WtPlay&&a.WtPause()}};";
	}

	static WHTML5Media.ReadyState intToReadyState(int i) {
		switch (i) {
		case 0:
			return WHTML5Media.ReadyState.HAVE_NOTHING;
		case 1:
			return WHTML5Media.ReadyState.HAVE_METADATA;
		case 2:
			return WHTML5Media.ReadyState.HAVE_CURRENT_DATA;
		case 3:
			return WHTML5Media.ReadyState.HAVE_FUTURE_DATA;
		case 4:
			return WHTML5Media.ReadyState.HAVE_ENOUGH_DATA;
		default:
			assert false;
			throw new RuntimeException("unreachable code");
		}
	}

	private static String PLAYBACKSTARTED_SIGNAL = "play";
	private static String PLAYBACKPAUSED_SIGNAL = "pause";
	private static String ENDED_SIGNAL = "ended";
	private static String TIMEUPDATED_SIGNAL = "timeupdate";
	private static String VOLUMECHANGED_SIGNAL = "volumechange";
}
