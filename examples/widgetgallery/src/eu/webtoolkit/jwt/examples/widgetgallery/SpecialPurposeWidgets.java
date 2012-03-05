/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

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

class SpecialPurposeWidgets extends ControlsWidget {
	private static Logger logger = LoggerFactory
			.getLogger(SpecialPurposeWidgets.class);

	public SpecialPurposeWidgets(EventDisplayer ed) {
		super(ed, true);
		addText(tr("specialpurposewidgets-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("WGoogleMap", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return SpecialPurposeWidgets.this.wGoogleMap();
					}
				}));
		menu.addItem("WMediaPlayer", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return SpecialPurposeWidgets.this.wMediaPlayer();
					}
				}));
		menu.addItem("WSound", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return SpecialPurposeWidgets.this.wSound();
			}
		}));
		menu.addItem("WVideo", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return SpecialPurposeWidgets.this.wVideo();
			}
		}));
		menu.addItem("WAudio", DeferredWidget.deferCreate(new WidgetCreator() {
			public WWidget create() {
				return SpecialPurposeWidgets.this.wAudio();
			}
		}));
		menu.addItem("WFlashObject", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return SpecialPurposeWidgets.this.wFlashObject();
					}
				}));
	}

	private WWidget wGoogleMap() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WGoogleMap", result);
		addText(tr("specialpurposewidgets-WGoogleMap"), result);
		new GoogleMapExample(result, this);
		return result;
	}

	private WWidget wMediaPlayer() {
		WContainerWidget result = new WContainerWidget();
		result.setStyleClass("wmediaplayer");
		this.topic("WMediaPlayer", result);
		addText(tr("specialpurposewidgets-WMediaPlayer"), result);
		addText(tr("specialpurposewidgets-WMediaPlayer-video"), result);
		WMediaPlayer player = new WMediaPlayer(WMediaPlayer.MediaType.Video,
				result);
		player.addSource(WMediaPlayer.Encoding.M4V, new WLink(mp4Video));
		player.addSource(WMediaPlayer.Encoding.OGV, new WLink(ogvVideo));
		player.addSource(WMediaPlayer.Encoding.PosterImage, new WLink(poster));
		player
				.setTitle("<a href=\"http://durian.blender.org/\"target=\"_blank\">Sintel</a>, (c) copyright Blender Foundation");
		this.ed_.showEvent(player.playbackStarted(), new WString(
				"Video playing"));
		this.ed_
				.showEvent(player.playbackPaused(), new WString("Video paused"));
		this.ed_.showEvent(player.ended(), new WString("Video ended"));
		this.ed_.showEvent(player.volumeChanged(),
				new WString("Volume changed"));
		addText(tr("specialpurposewidgets-WMediaPlayer-audio"), result);
		player = new WMediaPlayer(WMediaPlayer.MediaType.Audio, result);
		player.addSource(WMediaPlayer.Encoding.MP3, new WLink(mp3Audio));
		player.addSource(WMediaPlayer.Encoding.OGA, new WLink(oggAudio));
		player.setTitle("La Sera - Never Come Around");
		this.ed_.showEvent(player.playbackStarted(),
				new WString("Song playing"));
		this.ed_.showEvent(player.playbackPaused(), new WString("Song paused"));
		this.ed_.showEvent(player.ended(), new WString("Song ended"));
		this.ed_.showEvent(player.volumeChanged(),
				new WString("Volume changed"));
		return result;
	}

	private WWidget wSound() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WSound", result);
		addText(tr("specialpurposewidgets-WSound"), result);
		addText("The beep will be repeated 3 times.", result);
		new WBreak(result);
		final WSound sound = new WSound("sounds/beep.mp3", result);
		sound.setLoops(3);
		WPushButton playButton = new WPushButton("Beep!", result);
		playButton.setMargin(new WLength(5));
		WPushButton stopButton = new WPushButton("Make it stop!!!", result);
		stopButton.setMargin(new WLength(5));
		playButton.clicked().addListener(sound,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						sound.play();
					}
				});
		stopButton.clicked().addListener(sound,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						sound.stop();
					}
				});
		this.ed_.showSignal(playButton.clicked(), "Beeping started!");
		this.ed_.showSignal(stopButton.clicked(), "Beeping stopped!");
		return result;
	}

	private WWidget wAudio() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WAudio", result);
		addText(tr("specialpurposewidgets-WAudio"), result);
		WAudio a1 = new WAudio(result);
		a1.addSource(new WLink(mp3Audio));
		a1.addSource(new WLink(oggAudio));
		a1.setOptions(EnumSet.of(WAbstractMedia.Options.Controls));
		this.ed_.showEvent(a1.playbackStarted(), new WString("Audio playing"));
		this.ed_.showEvent(a1.playbackPaused(), new WString("Audio paused"));
		this.ed_.showEvent(a1.ended(), new WString("Audio ended"));
		this.ed_.showEvent(a1.volumeChanged(), new WString(
				"Audio volume changed"));
		return result;
	}

	private WWidget wVideo() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WVideo", result);
		addText(tr("specialpurposewidgets-WVideo"), result);
		new WBreak(result);
		addText(tr("specialpurposewidgets-WVideo-1"), result);
		WVideo v1 = new WVideo(result);
		v1.addSource(new WLink(mp4Video));
		v1.addSource(new WLink(ogvVideo));
		v1.setPoster(poster);
		v1.setAlternativeContent(new WImage(poster));
		v1.resize(new WLength(640), new WLength(360));
		this.ed_
				.showEvent(v1.playbackStarted(), new WString("Video 1 playing"));
		this.ed_.showEvent(v1.playbackPaused(), new WString("Video 1 paused"));
		this.ed_.showEvent(v1.ended(), new WString("Video 1 ended"));
		this.ed_.showEvent(v1.volumeChanged(), new WString(
				"Video 1 volume changed"));
		addText(tr("specialpurposewidgets-WVideo-2"), result);
		WFlashObject flash2 = new WFlashObject(
				"http://www.webtoolkit.eu/videos/player_flv_maxi.swf");
		flash2.setFlashVariable("startimage", "pics/sintel_trailer.jpg");
		flash2.setFlashParameter("allowFullScreen", "true");
		flash2.setFlashVariable("flv", mp4Video);
		flash2.setFlashVariable("showvolume", "1");
		flash2.setFlashVariable("showfullscreen", "1");
		flash2.setAlternativeContent(new WImage(poster));
		flash2.resize(new WLength(640), new WLength(360));
		WVideo v2 = new WVideo(result);
		v2.addSource(new WLink(mp4Video));
		v2.addSource(new WLink(ogvVideo));
		v2.setAlternativeContent(flash2);
		v2.setPoster(poster);
		v2.resize(new WLength(640), new WLength(360));
		this.ed_
				.showEvent(v2.playbackStarted(), new WString("Video 2 playing"));
		this.ed_.showEvent(v2.playbackPaused(), new WString("Video 2 paused"));
		this.ed_.showEvent(v2.ended(), new WString("Video 2 ended"));
		this.ed_.showEvent(v2.volumeChanged(), new WString(
				"Video 2 volume changed"));
		addText(tr("specialpurposewidgets-WVideo-3"), result);
		WFlashObject flash3 = new WFlashObject(
				"http://www.youtube.com/v/HOfdboHvshg", result);
		flash3.setFlashParameter("allowFullScreen", "true");
		flash3.resize(new WLength(640), new WLength(360));
		return result;
	}

	private WWidget wFlashObject() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WFlashObject", result);
		addText(tr("specialpurposewidgets-WFlashObject"), result);
		WFlashObject f = new WFlashObject(
				"http://www.youtube.com/v/HOfdboHvshg", result);
		f.resize(new WLength(640), new WLength(385));
		return result;
	}

	static String ogvVideo = "http://www.webtoolkit.eu/videos/sintel_trailer.ogv";
	static String mp4Video = "http://www.webtoolkit.eu/videos/sintel_trailer.mp4";
	static String mp3Audio = "http://www.webtoolkit.eu/audio/LaSera-NeverComeAround.mp3";
	static String oggAudio = "http://www.webtoolkit.eu/audio/LaSera-NeverComeAround.ogg";
	static String poster = "pics/sintel_trailer.jpg";
}
