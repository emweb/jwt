/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A widget that renders video using the HTML5 video element
 * <p>
 * 
 * This class renders HTML5 video. In a typical usage scenario, you instantiate
 * the class, set options, add one or multiple video sources. Since not every
 * browser supports HTML5 video, the class provides a mechanism to display
 * alternative content in browsers that cannot play the video.
 * <p>
 * There are two reasons why the a browser may use the alternative content:
 * either because the browser does not support the HTML5 video tag (alternative
 * content is displayed even when JavaScript is not available), or because none
 * of the specified sources contain a video format that is understood by the
 * browser (requires JavaScript to display the alternative content).
 * <p>
 * {@link WHTML5Video#addSource(String url) addSource()} and
 * {@link WHTML5Video#setAlternativeContent(WWidget alternative)
 * setAlternativeContent()} must not be called after the {@link WHTML5Video} was
 * rendered. This can easily be avoided by calling these functions right after
 * construction.
 * <p>
 * This is a technology-specific class. To let JWt choose a technology (and
 * fallback technologies) to display your videos, use the WVideo class.
 */
public class WHTML5Video extends WWebWidget {
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
	 * Creates a HTML5 video widget.
	 * <p>
	 * The constructor sets the &apos;controls&apos; option, which causes the
	 * browser to display a bar with play/pauze/volume/... controls.
	 * <p>
	 * A freshly constructed HTML5Video widget has no poster image, no media
	 * sources, has preload mode set to PreloadAuto, and only the Controls flag
	 * is set.
	 */
	public WHTML5Video(WContainerWidget parent) {
		super(parent);
		this.sources_ = new ArrayList<WHTML5Video.Source>();
		this.videoId_ = "";
		this.posterUrl_ = "";
		this.flags_ = EnumSet.of(WHTML5Video.Options.Controls);
		this.preloadMode_ = WHTML5Video.PreloadMode.PreloadAuto;
		this.alternative_ = null;
		this.sizeChanged_ = false;
		this.posterChanged_ = false;
		this.flagsChanged_ = false;
		this.preloadChanged_ = false;
		this.setInline(false);
	}

	/**
	 * Creates a HTML5 video widget.
	 * <p>
	 * Calls {@link #WHTML5Video(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WHTML5Video() {
		this((WContainerWidget) null);
	}

	/**
	 * Set the poster image.
	 */
	public void setPoster(String url) {
		this.posterUrl_ = url;
		this.posterChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Set the video tag options.
	 * <p>
	 * 
	 * @see WHTML5Video.Options
	 */
	public void setOptions(EnumSet<WHTML5Video.Options> flags) {
		this.flags_ = EnumSet.copyOf(flags);
		this.flagsChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Set the video tag options.
	 * <p>
	 * Calls {@link #setOptions(EnumSet flags) setOptions(EnumSet.of(flag,
	 * flags))}
	 */
	public final void setOptions(WHTML5Video.Options flag,
			WHTML5Video.Options... flags) {
		setOptions(EnumSet.of(flag, flags));
	}

	/**
	 * Retrieve the currently configured options.
	 */
	public EnumSet<WHTML5Video.Options> getOptions() {
		return this.flags_;
	}

	/**
	 * Set the preload mode for the video data.
	 */
	public void setPreloadMode(WHTML5Video.PreloadMode mode) {
		this.preloadMode_ = mode;
		this.preloadChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Get the preload mode for the video data.
	 */
	public WHTML5Video.PreloadMode getPreloadMode() {
		return this.preloadMode_;
	}

	/**
	 * Add a video source.
	 * <p>
	 * This method specifies a video source using only the URL. You may add as
	 * many video sources as you want. The browser will select the appropriate
	 * video stream to display to the user.
	 */
	public void addSource(String url) {
		this.sources_.add(new WHTML5Video.Source(url));
	}

	/**
	 * Add a video source.
	 * <p>
	 * This method specifies a video source using the URL and the mime type
	 * (e.g. video/ogg; codecs=&quot;theora, vorbis&quot;).
	 */
	public void addSource(String url, String type) {
		this.sources_.add(new WHTML5Video.Source(url, type));
	}

	/**
	 * Add a video source.
	 * <p>
	 * This method specifies a video source using the URL, the mime type, and
	 * the media attribute.
	 */
	public void addSource(String url, String type, String media) {
		this.sources_.add(new WHTML5Video.Source(url, type, media));
	}

	/**
	 * Content to be shown when video cannot be played.
	 * <p>
	 * As the video tag is not supported by all browsers, it is a good idea to
	 * provide fallback options when the video cannot be displayed. The two
	 * reasons to display the alternative content are (1) the video tag is not
	 * supported, or (2) the video tag is supported, but none of the video
	 * sources can be played by the browser. In the first case, fall-back is
	 * automatic and does not rely on JavaScript in the browser; in the latter
	 * case, JavaScript is required to make the fallback work.
	 * <p>
	 * The alternative content can be any widget: you can set it to an
	 * alternative video player (QuickTime, Flash, ...), show a Flash movie, an
	 * animated gif, a text, the poster image, ...
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
	 * Returns the JavaScript reference to the video object, or null.
	 * <p>
	 * It is possible, for compatibility reasons, that
	 * {@link WWidget#getJsRef() WWidget#getJsRef()} is not the HTML5 video
	 * element. {@link WHTML5Video#getJsVideoRef() getJsVideoRef()} is
	 * guaranteed to be an expression that evaluates to the video object. This
	 * expression may yield null, if the video object is not rendered at all
	 * (e.g. on older versions of Internet Explorer).
	 */
	public String getJsVideoRef() {
		if (this.videoId_.length() == 0) {
			return "null";
		} else {
			return "Wt3_1_3.getElement('" + this.videoId_ + "')";
		}
	}

	public void resize(WLength width, WLength height) {
		super.resize(width, height);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		if (this.videoId_.length() != 0) {
			DomElement video = DomElement.getForUpdate(this.videoId_,
					DomElementType.DomElement_VIDEO);
			this.updateVideoDom(video, false);
			result.add(video);
		}
		super.getDomChanges(result, app);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_VIDEO;
	}

	DomElement createDomElement(WApplication app) {
		DomElement result = null;
		if (this.isInLayout()) {
			this.setJavaScriptMember(WT_RESIZE_JS, "function() {}");
		}
		if (app.getEnvironment().agentIsIE()) {
			result = DomElement.createNew(DomElementType.DomElement_DIV);
			if (this.alternative_ != null) {
				result.addChild(this.alternative_.createSDomElement(app));
			}
		} else {
			DomElement video = DomElement
					.createNew(DomElementType.DomElement_VIDEO);
			DomElement wrap = null;
			if (this.isInLayout()) {
				video.setProperty(Property.PropertyStylePosition, "absolute");
				video.setProperty(Property.PropertyStyleLeft, "0");
				video.setProperty(Property.PropertyStyleRight, "0");
				wrap = DomElement.createNew(DomElementType.DomElement_DIV);
				wrap.setProperty(Property.PropertyStylePosition, "relative");
			}
			result = wrap != null ? wrap : video;
			if (wrap != null) {
				this.videoId_ = this.getId() + "_video";
				video.setId(this.videoId_);
			} else {
				this.videoId_ = this.getId();
			}
			this.updateVideoDom(video, true);
			if (wrap != null) {
				wrap.addChild(video);
			}
		}
		if (this.isInLayout()) {
			StringWriter ss = new StringWriter();
			ss.append("function(self, w, h) {");
			if (this.videoId_.length() != 0) {
				ss
						.append("v="
								+ this.getJsVideoRef()
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
		return result;
	}

	private void updateVideoDom(DomElement element, boolean all) {
		if (all && this.alternative_ != null) {
			element
					.setAttribute(
							"onerror",
							"if(event.target.error && event.target.error.code==event.target.error.MEDIA_ERR_SRC_NOT_SUPPORTED){while (this.hasChildNodes())if (Wt3_1_3.hasTag(this.firstChild,'SOURCE')){this.removeChild(this.firstChild);}else{this.parentNode.insertBefore(this.firstChild, this);}this.style.display= 'none';}");
		}
		if (all || this.sizeChanged_) {
			if (!all || !this.getWidth().isAuto()) {
				element.setAttribute("width", this.getWidth().isAuto() ? ""
						: String.valueOf((int) this.getWidth().toPixels()));
			}
			if (!all || !this.getHeight().isAuto()) {
				element.setAttribute("height", this.getHeight().isAuto() ? ""
						: String.valueOf((int) this.getHeight().toPixels()));
			}
		}
		if (all || this.posterChanged_) {
			if (!all || !this.posterUrl_.equals("")) {
				element.setAttribute("poster", this.posterUrl_);
			}
		}
		if (all || this.flagsChanged_) {
			if (!all
					|| !EnumUtils.mask(this.flags_,
							WHTML5Video.Options.Controls).isEmpty()) {
				element.setAttribute("controls", !EnumUtils.mask(this.flags_,
						WHTML5Video.Options.Controls).isEmpty() ? "controls"
						: "");
			}
			if (!all
					|| !EnumUtils.mask(this.flags_,
							WHTML5Video.Options.Autoplay).isEmpty()) {
				element.setAttribute("autoplay", !EnumUtils.mask(this.flags_,
						WHTML5Video.Options.Autoplay).isEmpty() ? "autoplay"
						: "");
			}
			if (!all
					|| !EnumUtils.mask(this.flags_, WHTML5Video.Options.Loop)
							.isEmpty()) {
				element.setAttribute("loop", !EnumUtils.mask(this.flags_,
						WHTML5Video.Options.Loop).isEmpty() ? "loop" : "");
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
		if (all) {
			for (int i = 0; i < this.sources_.size(); ++i) {
				DomElement src = DomElement
						.createNew(DomElementType.DomElement_SOURCE);
				src.setAttribute("src", this.sources_.get(i).url);
				if (this.sources_.get(i).hasType) {
					src.setAttribute("type", this.sources_.get(i).type);
				}
				if (this.sources_.get(i).hasMedia) {
					src.setAttribute("media", this.sources_.get(i).media);
				}
				if (i + 1 >= this.sources_.size() && this.alternative_ != null) {
					src
							.setAttribute(
									"onerror",
									"video = parentNode;if(video){while (video && video.hasChildNodes())if (Wt3_1_3.hasTag(video.firstChild,'SOURCE')){video.removeChild(video.firstChild);}else{video.parentNode.insertBefore(video.firstChild, video);}video.style.display= 'none';}");
				}
				element.addChild(src);
			}
		}
		if (all) {
			if (this.alternative_ != null) {
				element.addChild(this.alternative_
						.createSDomElement(WApplication.getInstance()));
			}
		}
		this.sizeChanged_ = this.posterChanged_ = this.flagsChanged_ = this.preloadChanged_ = false;
	}

	static class Source {
		public Source(String url, String type, String media) {
			this.type = type;
			this.url = url;
			this.media = media;
			this.hasMedia = true;
			this.hasType = true;
		}

		public Source(String url, String type) {
			this.type = type;
			this.url = url;
			this.media = "";
			this.hasMedia = false;
			this.hasType = true;
		}

		public Source(String url) {
			this.type = "";
			this.url = url;
			this.media = "";
			this.hasMedia = false;
			this.hasType = false;
		}

		public String type;
		public String url;
		public String media;
		public boolean hasMedia;
		public boolean hasType;
	}

	private List<WHTML5Video.Source> sources_;
	private String videoId_;
	private String posterUrl_;
	EnumSet<WHTML5Video.Options> flags_;
	private WHTML5Video.PreloadMode preloadMode_;
	private WWidget alternative_;
	private boolean sizeChanged_;
	private boolean posterChanged_;
	private boolean flagsChanged_;
	private boolean preloadChanged_;
}
