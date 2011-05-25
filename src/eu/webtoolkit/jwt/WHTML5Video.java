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
 * A widget that renders video using the HTML5 video element.
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
 * {@link WHTML5Media#addSource(String url, String type, String media)
 * WHTML5Media#addSource()} and
 * {@link WHTML5Media#setAlternativeContent(WWidget alternative)
 * WHTML5Media#setAlternativeContent()} must not be called after the
 * {@link WHTML5Video} was rendered. This can easily be avoided by calling these
 * functions right after construction.
 * <p>
 * This is a technology-specific class. To let JWt choose a technology (and
 * fallback technologies) to display your videos, use the WVideo class.
 */
public class WHTML5Video extends WHTML5Media {
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
		this.posterUrl_ = "";
		this.sizeChanged_ = false;
		this.posterChanged_ = false;
		this.setInline(false);
		this.setOptions(EnumSet.of(WHTML5Media.Options.Controls));
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
	 * <p>
	 * On browsers that support it, the poster image is displayed before the
	 * video is playing. Some browsers display the first frame of the video
	 * stream once the video stream is loaded; it is therefore a good idea to
	 * include the poster image as first frame in the video feed too.
	 */
	public void setPoster(String url) {
		this.posterUrl_ = url;
		this.posterChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
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
		return this.getJsMediaRef();
	}

	public void resize(WLength width, WLength height) {
		this.sizeChanged_ = true;
		super.resize(width, height);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	DomElement createMediaDomElement() {
		return DomElement.createNew(DomElementType.DomElement_VIDEO);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_VIDEO;
	}

	void updateMediaDom(DomElement element, boolean all) {
		super.updateMediaDom(element, all);
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
				element.setAttribute("poster",
						resolveRelativeUrl(this.posterUrl_));
			}
		}
		this.sizeChanged_ = this.posterChanged_ = false;
	}

	private String posterUrl_;
	private WHTML5Media.PreloadMode preloadMode_;
	private boolean sizeChanged_;
	private boolean posterChanged_;
}
