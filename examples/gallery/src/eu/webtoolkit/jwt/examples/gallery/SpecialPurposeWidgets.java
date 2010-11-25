/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.gallery;

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
		WTable layout = new WTable(result);
		final WGoogleMap map = new WGoogleMap(layout.getElementAt(0, 0));
		map.resize(new WLength(700), new WLength(500));
		map.setMapTypeControl(WGoogleMap.MapTypeControl.DefaultControl);
		map.enableScrollWheelZoom();
		layout.getElementAt(0, 1).setPadding(new WLength(3));
		WContainerWidget zoomContainer = new WContainerWidget(layout
				.getElementAt(0, 1));
		new WText("Zoom: ", zoomContainer);
		WPushButton zoomIn = new WPushButton("+", zoomContainer);
		zoomIn.clicked().addListener(map, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				map.zoomIn();
			}
		});
		WPushButton zoomOut = new WPushButton("-", zoomContainer);
		zoomOut.clicked().addListener(map, new Signal1.Listener<WMouseEvent>() {
			public void trigger(WMouseEvent e1) {
				map.zoomOut();
			}
		});
		List<WGoogleMap.Coordinate> road = new ArrayList<WGoogleMap.Coordinate>();
		this.roadDescription(road);
		map.addPolyline(road, new WColor(0, 191, 255));
		map.setCenter(road.get(road.size() - 1));
		map
				.openInfoWindow(road.get(0),
						"<img src=\"http://emweb.be/img/emweb_small.jpg\" /><br/><b>Emweb office</b>");
		map.clicked().addListener(this,
				new Signal1.Listener<WGoogleMap.Coordinate>() {
					public void trigger(WGoogleMap.Coordinate e1) {
						SpecialPurposeWidgets.this.googleMapClicked(e1);
					}
				});
		map.doubleClicked().addListener(this,
				new Signal1.Listener<WGoogleMap.Coordinate>() {
					public void trigger(WGoogleMap.Coordinate e1) {
						SpecialPurposeWidgets.this.googleMapDoubleClicked(e1);
					}
				});
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

	private void roadDescription(List<WGoogleMap.Coordinate> roadDescription) {
		roadDescription
				.add(new WGoogleMap.Coordinate(50.85342000000001, 4.7281));
		roadDescription.add(new WGoogleMap.Coordinate(50.85377, 4.72573));
		roadDescription.add(new WGoogleMap.Coordinate(50.85393, 4.72496));
		roadDescription.add(new WGoogleMap.Coordinate(50.85393, 4.72496));
		roadDescription.add(new WGoogleMap.Coordinate(50.85372, 4.72482));
		roadDescription.add(new WGoogleMap.Coordinate(50.85304, 4.72421));
		roadDescription.add(new WGoogleMap.Coordinate(50.8519, 4.72297));
		roadDescription.add(new WGoogleMap.Coordinate(50.85154, 4.72251));
		roadDescription.add(new WGoogleMap.Coordinate(50.85154, 4.72251));
		roadDescription.add(new WGoogleMap.Coordinate(50.85153, 4.72205));
		roadDescription.add(new WGoogleMap.Coordinate(50.85153, 4.72205));
		roadDescription.add(new WGoogleMap.Coordinate(50.85752, 4.7186));
		roadDescription.add(new WGoogleMap.Coordinate(50.85847, 4.71798));
		roadDescription.add(new WGoogleMap.Coordinate(50.859, 4.71753));
		roadDescription.add(new WGoogleMap.Coordinate(50.8593, 4.71709));
		roadDescription.add(new WGoogleMap.Coordinate(50.85986999999999,
				4.71589));
		roadDescription.add(new WGoogleMap.Coordinate(50.8606, 4.7147));
		roadDescription.add(new WGoogleMap.Coordinate(50.8611, 4.71327));
		roadDescription.add(new WGoogleMap.Coordinate(50.86125999999999,
				4.71293));
		roadDescription.add(new WGoogleMap.Coordinate(50.86184000000001,
				4.71217));
		roadDescription.add(new WGoogleMap.Coordinate(50.86219, 4.71202));
		roadDescription.add(new WGoogleMap.Coordinate(50.86346, 4.71178));
		roadDescription.add(new WGoogleMap.Coordinate(50.86406, 4.71146));
		roadDescription.add(new WGoogleMap.Coordinate(50.86478, 4.71126));
		roadDescription.add(new WGoogleMap.Coordinate(50.86623000000001,
				4.71111));
		roadDescription.add(new WGoogleMap.Coordinate(50.86659999999999,
				4.71101));
		roadDescription.add(new WGoogleMap.Coordinate(50.8668, 4.71072));
		roadDescription.add(new WGoogleMap.Coordinate(50.86709, 4.71018));
		roadDescription.add(new WGoogleMap.Coordinate(50.86739, 4.70941));
		roadDescription.add(new WGoogleMap.Coordinate(50.86751, 4.70921));
		roadDescription.add(new WGoogleMap.Coordinate(50.86869, 4.70843));
		roadDescription.add(new WGoogleMap.Coordinate(50.8691, 4.70798));
		roadDescription.add(new WGoogleMap.Coordinate(50.8691, 4.70798));
		roadDescription.add(new WGoogleMap.Coordinate(50.86936, 4.70763));
		roadDescription.add(new WGoogleMap.Coordinate(50.86936, 4.70763));
		roadDescription.add(new WGoogleMap.Coordinate(50.86874, 4.70469));
		roadDescription.add(new WGoogleMap.Coordinate(50.86858, 4.70365));
		roadDescription.add(new WGoogleMap.Coordinate(50.86845999999999,
				4.70269));
		roadDescription.add(new WGoogleMap.Coordinate(50.86839, 4.70152));
		roadDescription.add(new WGoogleMap.Coordinate(50.86843, 4.70043));
		roadDescription.add(new WGoogleMap.Coordinate(50.86851000000001,
				4.69987));
		roadDescription.add(new WGoogleMap.Coordinate(50.86881999999999,
				4.69869));
		roadDescription.add(new WGoogleMap.Coordinate(50.8689, 4.69827));
		roadDescription.add(new WGoogleMap.Coordinate(50.87006, 4.6941));
		roadDescription.add(new WGoogleMap.Coordinate(50.87006, 4.6941));
		roadDescription.add(new WGoogleMap.Coordinate(50.87045999999999,
				4.69348));
		roadDescription.add(new WGoogleMap.Coordinate(50.87172, 4.69233));
		roadDescription.add(new WGoogleMap.Coordinate(50.87229000000001,
				4.69167));
		roadDescription.add(new WGoogleMap.Coordinate(50.87229000000001,
				4.69167));
		roadDescription.add(new WGoogleMap.Coordinate(50.8725, 4.69123));
		roadDescription.add(new WGoogleMap.Coordinate(50.8725, 4.69123));
		roadDescription.add(new WGoogleMap.Coordinate(50.87408, 4.69142));
		roadDescription.add(new WGoogleMap.Coordinate(50.87423, 4.69125));
		roadDescription.add(new WGoogleMap.Coordinate(50.87464, 4.69116));
		roadDescription.add(new WGoogleMap.Coordinate(50.87579999999999,
				4.69061));
		roadDescription.add(new WGoogleMap.Coordinate(50.87595, 4.69061));
		roadDescription.add(new WGoogleMap.Coordinate(50.87733, 4.69073));
		roadDescription.add(new WGoogleMap.Coordinate(50.87742, 4.69078));
		roadDescription.add(new WGoogleMap.Coordinate(50.87784, 4.69131));
		roadDescription.add(new WGoogleMap.Coordinate(50.87784, 4.69131));
		roadDescription.add(new WGoogleMap.Coordinate(50.87759, 4.69267));
		roadDescription.add(new WGoogleMap.Coordinate(50.8775, 4.6935));
		roadDescription.add(new WGoogleMap.Coordinate(50.87751, 4.69395));
		roadDescription.add(new WGoogleMap.Coordinate(50.87768, 4.69545));
		roadDescription.add(new WGoogleMap.Coordinate(50.87769, 4.69666));
		roadDescription.add(new WGoogleMap.Coordinate(50.87759, 4.69742));
		roadDescription.add(new WGoogleMap.Coordinate(50.87734, 4.69823));
		roadDescription.add(new WGoogleMap.Coordinate(50.87734, 4.69823));
		roadDescription.add(new WGoogleMap.Coordinate(50.87790999999999,
				4.69861));
	}

	private void googleMapDoubleClicked(WGoogleMap.Coordinate c) {
		StringWriter strm = new StringWriter();
		strm.append("Double clicked at coordinate (").append(
				String.valueOf(c.getLatitude())).append(",").append(
				String.valueOf(c.getLongitude())).append(")");
		this.ed_.setStatus(strm.toString());
	}

	private void googleMapClicked(WGoogleMap.Coordinate c) {
		StringWriter strm = new StringWriter();
		strm.append("Clicked at coordinate (").append(
				String.valueOf(c.getLatitude())).append(",").append(
				String.valueOf(c.getLongitude())).append(")");
		this.ed_.setStatus(strm.toString());
	}
}
