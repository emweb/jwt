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

class SoundManager extends WMediaPlayer {
	private static Logger logger = LoggerFactory.getLogger(SoundManager.class);

	public SoundManager(WContainerWidget parent) {
		super(WMediaPlayer.MediaType.Audio, parent);
		this.getControlsWidget().hide();
		this.getDecorationStyle().setBorder(new WBorder());
		StringBuilder ss = new StringBuilder();
		ss
				.append("function() { var s = ")
				.append(this.getJsRef())
				.append(
						", l = s.getAttribute('loops');if (l && l != '0') {s.setAttribute('loops', l - 1);")
				.append(this.getJsPlayerRef()).append(".jPlayer('play');}}");
		this.ended().addListener(ss.toString());
		this.ended().setNotExposed();
	}

	public SoundManager() {
		this((WContainerWidget) null);
	}

	public void add(WSound sound) {
		if (!this.getSource(WMediaPlayer.Encoding.MP3).equals(sound.getUrl())) {
			this.clearSources();
			this
					.addSource(WMediaPlayer.Encoding.MP3, new WLink(sound
							.getUrl()));
		}
	}

	public void remove(WSound sound) {
	}

	public void play(WSound sound, int loops) {
		if (!this.getSource(WMediaPlayer.Encoding.MP3).equals(sound.getUrl())) {
			this.clearSources();
			this
					.addSource(WMediaPlayer.Encoding.MP3, new WLink(sound
							.getUrl()));
		}
		this.setAttributeValue("loops", "");
		this.setAttributeValue("loops", String.valueOf(loops - 1));
		super.play();
	}

	public void stop(WSound sound) {
		super.stop();
	}

	public boolean isFinished(WSound sound) {
		if (this.getSource(WMediaPlayer.Encoding.MP3).equals(sound.getUrl())) {
			return !this.isPlaying();
		} else {
			return true;
		}
	}
}
