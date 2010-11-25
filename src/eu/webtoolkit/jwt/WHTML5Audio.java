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
 * A widget that plays audio using the HTML5 audio element.
 * <p>
 * 
 * This class plays HTML5 audio. In a typical usage scenario, you instantiate
 * the class, set options, and add one or multiple audio sources. The browser
 * will play the first audio stream that it is capable to play back.
 * <p>
 * Since not every browser supports HTML5 audio, the class provides a mechanism
 * to display alternative content in browsers that cannot play the audio. A
 * Flash based player, configured to play the same audio file, is an example of
 * sensible alternative content.
 * <p>
 * There are two reasons why the a browser may use the alternative content:
 * either because the browser does not support the HTML5 audio tag (alternative
 * content is displayed even when JavaScript is not available), or because none
 * of the specified sources contain an audio format that is understood by the
 * browser (requires JavaScript to display the alternative content).
 * <p>
 * {@link WHTML5Media#addSource(String url, String type, String media)
 * WHTML5Media#addSource()} and
 * {@link WHTML5Media#setAlternativeContent(WWidget alternative)
 * WHTML5Media#setAlternativeContent()} must not be called after the
 * {@link WHTML5Audio} was rendered. This can easily be avoided by calling these
 * functions right after construction.
 * <p>
 * This is a technology-specific class. To let JWt choose a technology (and
 * fallback technologies) to display your videos, use the {@link WSound} class.
 * <p>
 * 
 * @see WHTML5Media
 * @see WSound
 */
public class WHTML5Audio extends WHTML5Media {
	/**
	 * Creates a HTML5 audio widget.
	 * <p>
	 * A freshly constructed HTML5Audio widget has no media sources, no options,
	 * and has preload mode set to PreloadAuto.
	 */
	public WHTML5Audio(WContainerWidget parent) {
		super(parent);
	}

	/**
	 * Creates a HTML5 audio widget.
	 * <p>
	 * Calls {@link #WHTML5Audio(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WHTML5Audio() {
		this((WContainerWidget) null);
	}

	/**
	 * Returns the JavaScript reference to the audio object, or null.
	 * <p>
	 * It is possible, for browser compatibility reasons, that
	 * {@link WWidget#getJsRef() WWidget#getJsRef()} is not the HTML5 audio
	 * element. {@link WHTML5Audio#getJsAudioRef() getJsAudioRef()} is
	 * guaranteed to be an expression that evaluates to the media object. This
	 * expression may yield null, if the video object is not rendered at all
	 * (e.g. on older versions of Internet Explorer).
	 */
	public String getJsAudioRef() {
		return this.getJsMediaRef();
	}

	DomElement createMediaDomElement() {
		return DomElement.createNew(DomElementType.DomElement_AUDIO);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_AUDIO;
	}
}
