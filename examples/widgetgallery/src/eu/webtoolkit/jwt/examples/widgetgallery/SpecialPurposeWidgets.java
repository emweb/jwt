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

class SpecialPurposeWidgets extends ControlsWidget {
	public SpecialPurposeWidgets(EventDisplayer ed) {
		super(ed, true);
		new WText(tr("specialpurposewidgets-intro"), this);
	}

	public void populateSubMenu(WMenu menu) {
		menu.addItem("WGoogleMap", DeferredWidget
				.deferCreate(new WidgetCreator() {
					public WWidget create() {
						return SpecialPurposeWidgets.this.wGoogleMap();
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
		new WText(tr("specialpurposewidgets-WGoogleMap"), result);
		GoogleMapExample googleMapExample = new GoogleMapExample(result, this);
		return result;
	}

	private WWidget wSound() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WSound", result);
		new WText(tr("specialpurposewidgets-WSound"), result);
		new WText("The beep will be repeated 3 times.", result);
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

	private WWidget wVideo() {
		String ogvVideo = "http://www.webtoolkit.eu/videos/sintel_trailer.ogv";
		String mp4Video = "http://www.webtoolkit.eu/videos/sintel_trailer.mp4";
		String poster = "pics/sintel_trailer.jpg";
		WContainerWidget result = new WContainerWidget();
		this.topic("WHTML5Video", result);
		new WText(tr("specialpurposewidgets-WHTML5Video"), result);
		new WBreak(result);
		new WText(tr("specialpurposewidgets-WHTML5Video-1"), result);
		WHTML5Video v1 = new WHTML5Video(result);
		v1.addSource(mp4Video);
		v1.addSource(ogvVideo);
		v1.setPoster(poster);
		v1.setAlternativeContent(new WImage(poster));
		v1.resize(new WLength(640), new WLength(360));
		this.ed_
				.showEvent(v1.playbackStarted(), new WString("Video 1 playing"));
		this.ed_.showEvent(v1.playbackPaused(), new WString("Video 1 paused"));
		this.ed_.showEvent(v1.ended(), new WString("Video 1 ended"));
		this.ed_.showEvent(v1.volumeChanged(), new WString(
				"Video 1 volume changed"));
		new WText(tr("specialpurposewidgets-WHTML5Video-2"), result);
		WFlashObject flash2 = new WFlashObject(
				"http://www.webtoolkit.eu/videos/player_flv_maxi.swf");
		flash2.setFlashVariable("startimage", "pics/sintel_trailer.jpg");
		flash2.setFlashParameter("allowFullScreen", "true");
		flash2.setFlashVariable("flv", mp4Video);
		flash2.setFlashVariable("showvolume", "1");
		flash2.setFlashVariable("showfullscreen", "1");
		flash2.setAlternativeContent(new WImage(poster));
		flash2.resize(new WLength(640), new WLength(360));
		WHTML5Video v2 = new WHTML5Video(result);
		v2.addSource(mp4Video);
		v2.addSource(ogvVideo);
		v2.setAlternativeContent(flash2);
		v2.setPoster(poster);
		v2.resize(new WLength(640), new WLength(360));
		this.ed_
				.showEvent(v2.playbackStarted(), new WString("Video 2 playing"));
		this.ed_.showEvent(v2.playbackPaused(), new WString("Video 2 paused"));
		this.ed_.showEvent(v2.ended(), new WString("Video 2 ended"));
		this.ed_.showEvent(v2.volumeChanged(), new WString(
				"Video 2 volume changed"));
		new WText(tr("specialpurposewidgets-WHTML5Video-3"), result);
		WFlashObject flash3 = new WFlashObject(
				"http://www.youtube.com/v/HOfdboHvshg", result);
		flash3.setFlashParameter("allowFullScreen", "true");
		flash3.resize(new WLength(640), new WLength(360));
		return result;
	}

	private WWidget wFlashObject() {
		WContainerWidget result = new WContainerWidget();
		this.topic("WFlashObject", result);
		new WText(tr("specialpurposewidgets-WFlashObject"), result);
		WFlashObject f = new WFlashObject(
				"http://www.youtube.com/v/HOfdboHvshg", result);
		f.resize(new WLength(640), new WLength(385));
		return result;
	}
}
