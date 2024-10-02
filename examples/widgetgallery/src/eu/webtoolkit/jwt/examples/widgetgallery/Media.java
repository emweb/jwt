/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Media extends Topic {
  private static Logger logger = LoggerFactory.getLogger(Media.class);

  public Media() {
    super();
  }

  public void populateSubMenu(WMenu menu) {
    menu.addItem(
            "WMediaPlayer",
            DeferredWidget.deferCreate(
                () -> {
                  return Media.this.mediaPlayer();
                }))
        .setPathComponent("");
    menu.addItem(
        "WSound",
        DeferredWidget.deferCreate(
            () -> {
              return Media.this.sound();
            }));
    menu.addItem(
        "WAudio",
        DeferredWidget.deferCreate(
            () -> {
              return Media.this.audio();
            }));
    menu.addItem(
        "WVideo",
        DeferredWidget.deferCreate(
            () -> {
              return Media.this.video();
            }));
    menu.addItem(
        "WFlashObject",
        DeferredWidget.deferCreate(
            () -> {
              return Media.this.flashObject();
            }));
    menu.addItem(
        "Resources",
        DeferredWidget.deferCreate(
            () -> {
              return Media.this.resources();
            }));
    menu.addItem(
        "PDF output",
        DeferredWidget.deferCreate(
            () -> {
              return Media.this.pdf();
            }));
  }

  private WWidget mediaPlayer() {
    TopicTemplate result = new TopicTemplate("media-MediaPlayer");
    result.bindWidget("MediaPlayerVideo", MediaPlayerVideo());
    result.bindWidget("MediaPlayerAudio", MediaPlayerAudio());
    return result;
  }

  private WWidget sound() {
    TopicTemplate result = new TopicTemplate("media-Sound");
    result.bindWidget("Sound", Sound());
    return result;
  }

  private WWidget audio() {
    TopicTemplate result = new TopicTemplate("media-Audio");
    result.bindWidget("Audio", Audio());
    return result;
  }

  private WWidget video() {
    TopicTemplate result = new TopicTemplate("media-Video");
    result.bindWidget("Video", Video());
    result.bindWidget("VideoFallback", VideoFallback());
    return result;
  }

  private WWidget flashObject() {
    TopicTemplate result = new TopicTemplate("media-FlashObject");
    result.bindWidget("Flash", Flash());
    return result;
  }

  private WWidget resources() {
    TopicTemplate result = new TopicTemplate("media-Resources");
    result.bindWidget("ResourceCustom", ResourceCustom());
    result.bindWidget("ResourceStatic", ResourceStatic());
    return result;
  }

  private WWidget pdf() {
    TopicTemplate result = new TopicTemplate("media-PDF");
    result.bindWidget("PdfImage", PdfImage());
    result.bindWidget("PdfRenderer", PdfRenderer());
    result.bindString(
        "PdfImageWrite", reindent(WString.tr("media-PdfImageWrite")), TextFormat.Plain);
    return result;
  }

  WWidget MediaPlayerVideo() {
    String mp4Video = "https://www.webtoolkit.eu/videos/sintel_trailer.mp4";
    String ogvVideo = "https://www.webtoolkit.eu/videos/sintel_trailer.ogv";
    String poster = "pics/sintel_trailer.jpg";
    WContainerWidget container = new WContainerWidget();
    WMediaPlayer player = new WMediaPlayer(MediaType.Video, (WContainerWidget) container);
    player.addSource(MediaEncoding.M4V, new WLink(mp4Video));
    player.addSource(MediaEncoding.OGV, new WLink(ogvVideo));
    player.addSource(MediaEncoding.PosterImage, new WLink(poster));
    player.setTitle(
        "<a href=\"https://durian.blender.org/\"target=\"_blank\">Sintel</a>, (c) copyright Blender Foundation");
    final WText out = new WText((WContainerWidget) container);
    player
        .playbackStarted()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video playing</p>");
            });
    player
        .playbackPaused()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video paused</p>");
            });
    player
        .ended()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video ended</p>");
            });
    player
        .volumeChanged()
        .addListener(
            this,
            () -> {
              out.setText("<p>Volume changed</p>");
            });
    return container;
  }

  WWidget MediaPlayerAudio() {
    String mp3Audio = "https://www.webtoolkit.eu/audio/LaSera-NeverComeAround.mp3";
    String oggAudio = "https://www.webtoolkit.eu/audio/LaSera-NeverComeAround.ogg";
    WContainerWidget container = new WContainerWidget();
    WMediaPlayer player = new WMediaPlayer(MediaType.Audio, (WContainerWidget) container);
    player.addSource(MediaEncoding.MP3, new WLink(mp3Audio));
    player.addSource(MediaEncoding.OGA, new WLink(oggAudio));
    player.setTitle("La Sera - Never Come Around");
    final WText out = new WText((WContainerWidget) container);
    player
        .playbackStarted()
        .addListener(
            this,
            () -> {
              out.setText("<p>Song playing</p>");
            });
    player
        .playbackPaused()
        .addListener(
            this,
            () -> {
              out.setText("<p>Song paused</p>");
            });
    player
        .ended()
        .addListener(
            this,
            () -> {
              out.setText("<p>Song ended</p>");
            });
    player
        .volumeChanged()
        .addListener(
            this,
            () -> {
              out.setText("<p>Volume changed</p>");
            });
    return container;
  }

  WWidget Sound() {
    WContainerWidget container = new WContainerWidget();
    final WSound sound = new WSound("sounds/beep.mp3");
    sound.setLoops(3);
    WPushButton playButton = new WPushButton("Beep!", (WContainerWidget) container);
    playButton.setMargin(new WLength(5));
    WPushButton stopButton = new WPushButton("Stop it!", (WContainerWidget) container);
    stopButton.setMargin(new WLength(5));
    final WText out = new WText((WContainerWidget) container);
    playButton
        .clicked()
        .addListener(
            this,
            () -> {
              sound.play();
              out.setText("<p>Beeping started!</p>");
            });
    stopButton
        .clicked()
        .addListener(
            this,
            () -> {
              sound.stop();
              out.setText("<p>Beeping stopped!</p>");
            });
    return container;
  }

  WWidget Audio() {
    String mp3Audio = "https://www.webtoolkit.eu/audio/LaSera-NeverComeAround.mp3";
    String oggAudio = "https://www.webtoolkit.eu/audio/LaSera-NeverComeAround.ogg";
    WContainerWidget container = new WContainerWidget();
    WAudio audio = new WAudio((WContainerWidget) container);
    audio.addSource(new WLink(mp3Audio));
    audio.addSource(new WLink(oggAudio));
    audio.setOptions(EnumSet.of(PlayerOption.Controls));
    audio.setAlternativeContent(new WText("You don't have HTML5 audio support!"));
    final WText out = new WText((WContainerWidget) container);
    audio
        .playbackStarted()
        .addListener(
            this,
            () -> {
              out.setText("<p>Audio playing</p>");
            });
    audio
        .playbackPaused()
        .addListener(
            this,
            () -> {
              out.setText("<p>Audio paused</p>");
            });
    audio
        .ended()
        .addListener(
            this,
            () -> {
              out.setText("<p>Audio ended</p>");
            });
    audio
        .volumeChanged()
        .addListener(
            this,
            () -> {
              out.setText("<p>Volume changed</p>");
            });
    return container;
  }

  WWidget Video() {
    String mp4Video = "https://www.webtoolkit.eu/videos/sintel_trailer.mp4";
    String ogvVideo = "https://www.webtoolkit.eu/videos/sintel_trailer.ogv";
    String poster = "pics/sintel_trailer.jpg";
    WContainerWidget container = new WContainerWidget();
    WVideo video = new WVideo((WContainerWidget) container);
    video.addSource(new WLink(mp4Video));
    video.addSource(new WLink(ogvVideo));
    video.setPoster(poster);
    video.setAlternativeContent(new WImage(new WLink(poster)));
    video.resize(new WLength(640), new WLength(360));
    final WText out = new WText((WContainerWidget) container);
    video
        .playbackStarted()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video playing</p>");
            });
    video
        .playbackPaused()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video paused</p>");
            });
    video
        .ended()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video ended</p>");
            });
    video
        .volumeChanged()
        .addListener(
            this,
            () -> {
              out.setText("<p>Volume changed</p>");
            });
    return container;
  }

  WWidget VideoFallback() {
    String mp4Video = "https://www.webtoolkit.eu/videos/sintel_trailer.mp4";
    String ogvVideo = "https://www.webtoolkit.eu/videos/sintel_trailer.ogv";
    String poster = "pics/sintel_trailer.jpg";
    WContainerWidget container = new WContainerWidget();
    WFlashObject flash = new WFlashObject("https://www.webtoolkit.eu/videos/player_flv_maxi.swf");
    flash.setFlashVariable("startimage", "pics/sintel_trailer.jpg");
    flash.setFlashParameter("allowFullScreen", "true");
    flash.setFlashVariable("flv", mp4Video);
    flash.setFlashVariable("showvolume", "1");
    flash.setFlashVariable("showfullscreen", "1");
    flash.setAlternativeContent(new WImage(new WLink(poster)));
    flash.resize(new WLength(640), new WLength(360));
    WVideo video = new WVideo((WContainerWidget) container);
    video.addSource(new WLink(mp4Video));
    video.addSource(new WLink(ogvVideo));
    video.setAlternativeContent(flash);
    video.setPoster(poster);
    video.resize(new WLength(640), new WLength(360));
    final WText out = new WText((WContainerWidget) container);
    video
        .playbackStarted()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video playing</p>");
            });
    video
        .playbackPaused()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video paused</p>");
            });
    video
        .ended()
        .addListener(
            this,
            () -> {
              out.setText("<p>Video ended</p>");
            });
    video
        .volumeChanged()
        .addListener(
            this,
            () -> {
              out.setText("<p>Volume changed</p>");
            });
    return container;
  }

  WWidget Flash() {
    String poster = "pics/sintel_trailer.jpg";
    WContainerWidget container = new WContainerWidget();
    WFlashObject flash =
        new WFlashObject("https://www.youtube.com/v/HOfdboHvshg", (WContainerWidget) container);
    flash.setFlashParameter("allowFullScreen", "true");
    flash.setAlternativeContent(new WImage(new WLink(poster)));
    flash.resize(new WLength(640), new WLength(360));
    return container;
  }

  WWidget ResourceCustom() {
    WContainerWidget container = new WContainerWidget();
    MyResource textResource = new MyResource();
    WLink link = new WLink(textResource);
    link.setTarget(LinkTarget.NewWindow);
    new WAnchor(link, "Download file", (WContainerWidget) container);
    return container;
  }

  WWidget ResourceStatic() {
    WContainerWidget container = new WContainerWidget();
    return container;
  }

  WWidget PdfImage() {
    WContainerWidget container = new WContainerWidget();
    SamplePdfResource pdf = new SamplePdfResource();
    WPushButton button = new WPushButton("Create pdf", (WContainerWidget) container);
    button.setLink(new WLink(pdf));
    return container;
  }

  WWidget PdfRenderer() {
    WContainerWidget container = new WContainerWidget();
    WText text = new WText(WString.tr("report.example"), (WContainerWidget) container);
    text.setStyleClass("reset");
    WPushButton button = new WPushButton("Create pdf", (WContainerWidget) container);
    ReportResource pdf = new ReportResource();
    button.setLink(new WLink(pdf));
    return container;
  }
}
