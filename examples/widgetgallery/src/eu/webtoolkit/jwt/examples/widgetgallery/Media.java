/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Media extends TopicWidget {
  private static Logger logger = LoggerFactory.getLogger(Media.class);

  public Media() {
    super();
    addText(tr("specialpurposewidgets-intro"), this);
  }

  public void populateSubMenu(WMenu menu) {
    menu.addItem(
            "WMediaPlayer",
            DeferredWidget.deferCreate(
                new WidgetCreator() {
                  public WWidget create() {
                    return Media.this.mediaPlayer();
                  }
                }))
        .setPathComponent("");
    menu.addItem(
        "WSound",
        DeferredWidget.deferCreate(
            new WidgetCreator() {
              public WWidget create() {
                return Media.this.sound();
              }
            }));
    menu.addItem(
        "WAudio",
        DeferredWidget.deferCreate(
            new WidgetCreator() {
              public WWidget create() {
                return Media.this.audio();
              }
            }));
    menu.addItem(
        "WVideo",
        DeferredWidget.deferCreate(
            new WidgetCreator() {
              public WWidget create() {
                return Media.this.video();
              }
            }));
    menu.addItem(
        "WFlashObject",
        DeferredWidget.deferCreate(
            new WidgetCreator() {
              public WWidget create() {
                return Media.this.flashObject();
              }
            }));
    menu.addItem(
        "Resources",
        DeferredWidget.deferCreate(
            new WidgetCreator() {
              public WWidget create() {
                return Media.this.resources();
              }
            }));
    menu.addItem(
        "PDF output",
        DeferredWidget.deferCreate(
            new WidgetCreator() {
              public WWidget create() {
                return Media.this.pdf();
              }
            }));
  }

  private WWidget mediaPlayer() {
    WTemplate result = new TopicTemplate("media-MediaPlayer");
    result.bindWidget("MediaPlayerVideo", MediaPlayerVideo());
    result.bindWidget("MediaPlayerAudio", MediaPlayerAudio());
    return result;
  }

  private WWidget sound() {
    WTemplate result = new TopicTemplate("media-Sound");
    result.bindWidget("Sound", Sound());
    return result;
  }

  private WWidget audio() {
    WTemplate result = new TopicTemplate("media-Audio");
    result.bindWidget("Audio", Audio());
    return result;
  }

  private WWidget video() {
    WTemplate result = new TopicTemplate("media-Video");
    result.bindWidget("Video", Video());
    result.bindWidget("VideoFallback", VideoFallback());
    return result;
  }

  private WWidget flashObject() {
    WTemplate result = new TopicTemplate("media-FlashObject");
    result.bindWidget("Flash", Flash());
    return result;
  }

  private WWidget resources() {
    WTemplate result = new TopicTemplate("media-Resources");
    result.bindWidget("ResourceCustom", ResourceCustom());
    result.bindWidget("ResourceStatic", ResourceStatic());
    return result;
  }

  private WWidget pdf() {
    WTemplate result = new TopicTemplate("media-PDF");
    result.bindWidget("PdfImage", PdfImage());
    result.bindWidget("PdfRenderer", PdfRenderer());
    result.bindString("PdfImageWrite", reindent(tr("media-PdfImageWrite")), TextFormat.PlainText);
    return result;
  }

  WWidget MediaPlayerVideo() {
    String mp4Video = "https://www.webtoolkit.eu/videos/sintel_trailer.mp4";
    String ogvVideo = "https://www.webtoolkit.eu/videos/sintel_trailer.ogv";
    String poster = "pics/sintel_trailer.jpg";
    WContainerWidget container = new WContainerWidget();
    WMediaPlayer player = new WMediaPlayer(WMediaPlayer.MediaType.Video, container);
    player.addSource(WMediaPlayer.Encoding.M4V, new WLink(mp4Video));
    player.addSource(WMediaPlayer.Encoding.OGV, new WLink(ogvVideo));
    player.addSource(WMediaPlayer.Encoding.PosterImage, new WLink(poster));
    player.setTitle(
        "<a href=\"https://durian.blender.org/\"target=\"_blank\">Sintel</a>, (c) copyright Blender Foundation");
    final WText out = new WText(container);
    player
        .playbackStarted()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video playing</p>");
              }
            });
    player
        .playbackPaused()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video paused</p>");
              }
            });
    player
        .ended()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video ended</p>");
              }
            });
    player
        .volumeChanged()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Volume changed</p>");
              }
            });
    return container;
  }

  WWidget MediaPlayerAudio() {
    String mp3Audio = "https://www.webtoolkit.eu/audio/LaSera-NeverComeAround.mp3";
    String oggAudio = "https://www.webtoolkit.eu/audio/LaSera-NeverComeAround.ogg";
    WContainerWidget container = new WContainerWidget();
    WMediaPlayer player = new WMediaPlayer(WMediaPlayer.MediaType.Audio, container);
    player.addSource(WMediaPlayer.Encoding.MP3, new WLink(mp3Audio));
    player.addSource(WMediaPlayer.Encoding.OGA, new WLink(oggAudio));
    player.setTitle("La Sera - Never Come Around");
    final WText out = new WText(container);
    player
        .playbackStarted()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Song playing</p>");
              }
            });
    player
        .playbackPaused()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Song paused</p>");
              }
            });
    player
        .ended()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Song ended</p>");
              }
            });
    player
        .volumeChanged()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Volume changed</p>");
              }
            });
    return container;
  }

  WWidget Sound() {
    WContainerWidget container = new WContainerWidget();
    final WSound sound = new WSound("sounds/beep.mp3", container);
    sound.setLoops(3);
    WPushButton playButton = new WPushButton("Beep!", container);
    playButton.setMargin(new WLength(5));
    WPushButton stopButton = new WPushButton("Stop it!", container);
    stopButton.setMargin(new WLength(5));
    final WText out = new WText(container);
    playButton
        .clicked()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                sound.play();
                out.setText("<p>Beeping started!</p>");
              }
            });
    stopButton
        .clicked()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                sound.stop();
                out.setText("<p>Beeping stopped!</p>");
              }
            });
    return container;
  }

  WWidget Audio() {
    String mp3Audio = "https://www.webtoolkit.eu/audio/LaSera-NeverComeAround.mp3";
    String oggAudio = "https://www.webtoolkit.eu/audio/LaSera-NeverComeAround.ogg";
    WContainerWidget container = new WContainerWidget();
    WAudio audio = new WAudio(container);
    audio.addSource(new WLink(mp3Audio));
    audio.addSource(new WLink(oggAudio));
    audio.setOptions(EnumSet.of(WAbstractMedia.Options.Controls));
    audio.setAlternativeContent(new WText("You don't have HTML5 audio support!"));
    final WText out = new WText(container);
    audio
        .playbackStarted()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Audio playing</p>");
              }
            });
    audio
        .playbackPaused()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Audio paused</p>");
              }
            });
    audio
        .ended()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Audio ended</p>");
              }
            });
    audio
        .volumeChanged()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Volume changed</p>");
              }
            });
    return container;
  }

  WWidget Video() {
    String mp4Video = "https://www.webtoolkit.eu/videos/sintel_trailer.mp4";
    String ogvVideo = "https://www.webtoolkit.eu/videos/sintel_trailer.ogv";
    String poster = "pics/sintel_trailer.jpg";
    WContainerWidget container = new WContainerWidget();
    WVideo video = new WVideo(container);
    video.addSource(new WLink(mp4Video));
    video.addSource(new WLink(ogvVideo));
    video.setPoster(poster);
    video.setAlternativeContent(new WImage(poster));
    video.resize(new WLength(640), new WLength(360));
    final WText out = new WText(container);
    video
        .playbackStarted()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video playing</p>");
              }
            });
    video
        .playbackPaused()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video paused</p>");
              }
            });
    video
        .ended()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video ended</p>");
              }
            });
    video
        .volumeChanged()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Volume changed</p>");
              }
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
    flash.setAlternativeContent(new WImage(poster));
    flash.resize(new WLength(640), new WLength(360));
    WVideo video = new WVideo(container);
    video.addSource(new WLink(mp4Video));
    video.addSource(new WLink(ogvVideo));
    video.setAlternativeContent(flash);
    video.setPoster(poster);
    video.resize(new WLength(640), new WLength(360));
    final WText out = new WText(container);
    video
        .playbackStarted()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video playing</p>");
              }
            });
    video
        .playbackPaused()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video paused</p>");
              }
            });
    video
        .ended()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Video ended</p>");
              }
            });
    video
        .volumeChanged()
        .addListener(
            this,
            new Signal.Listener() {
              public void trigger() {
                out.setText("<p>Volume changed</p>");
              }
            });
    return container;
  }

  WWidget Flash() {
    String poster = "pics/sintel_trailer.jpg";
    WContainerWidget container = new WContainerWidget();
    WFlashObject flash = new WFlashObject("https://www.youtube.com/v/HOfdboHvshg", container);
    flash.setFlashParameter("allowFullScreen", "true");
    flash.setAlternativeContent(new WImage(poster));
    flash.resize(new WLength(640), new WLength(360));
    return container;
  }

  WWidget ResourceCustom() {
    WContainerWidget container = new WContainerWidget();
    WResource textResource = new MyResource(container);
    WAnchor anchor = new WAnchor(new WLink(textResource), "Download file", container);
    anchor.setTarget(AnchorTarget.TargetNewWindow);
    return container;
  }

  WWidget ResourceStatic() {
    WContainerWidget container = new WContainerWidget();
    return container;
  }

  WWidget PdfImage() {
    WContainerWidget container = new WContainerWidget();
    WResource pdf = new SamplePdfResource(container);
    WPushButton button = new WPushButton("Create pdf", container);
    button.setLink(new WLink(pdf));
    return container;
  }

  WWidget PdfRenderer() {
    WContainerWidget container = new WContainerWidget();
    WText text = new WText(WString.tr("report.example"), container);
    text.setStyleClass("reset");
    WPushButton button = new WPushButton("Create pdf", container);
    WResource pdf = new ReportResource(container);
    button.setLink(new WLink(pdf));
    return container;
  }
}
