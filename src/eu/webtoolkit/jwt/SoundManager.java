/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

class SoundManager extends WMediaPlayer {
  private static Logger logger = LoggerFactory.getLogger(SoundManager.class);

  public SoundManager(WContainerWidget parentContainer) {
    super(MediaType.Audio, (WContainerWidget) null);
    this.resize(new WLength(0), new WLength(0));
    this.setAttributeValue("style", "overflow: hidden");
    this.getControlsWidget().hide();
    this.getDecorationStyle().setBorder(new WBorder());
    StringBuilder ss = new StringBuilder();
    ss.append("function() { var s = ")
        .append(this.getJsRef())
        .append(", l = s.getAttribute('loops');if (l && l != '0') {s.setAttribute('loops', l - 1);")
        .append(this.getJsPlayerRef())
        .append(".jPlayer('play');}}");
    this.ended().addListener(ss.toString());
    this.ended().setNotExposed();
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public SoundManager() {
    this((WContainerWidget) null);
  }

  public void add(WSound sound) {
    this.setup(sound);
  }

  public void play(WSound sound, int loops) {
    this.setup(sound);
    this.setAttributeValue("loops", "");
    this.setAttributeValue("loops", String.valueOf(loops - 1));
    this.current_ = sound;
    super.play();
  }

  public void stop(WSound sound) {
    super.stop();
    this.current_ = null;
  }

  public boolean isFinished(WSound sound) {
    if (this.current_ == sound) {
      return !this.isPlaying();
    } else {
      return true;
    }
  }

  public void refresh() {}

  private WSound current_;

  private void setup(WSound sound) {
    for (int i = 0; i < sound.media_.size(); ++i) {
      final WSound.Source m = sound.media_.get(i);
      if (!this.getSource(m.encoding).equals(m.link)) {
        this.clearSources();
        for (int j = 0; j < sound.media_.size(); ++j) {
          final WSound.Source m2 = sound.media_.get(j);
          this.addSource(m2.encoding, m2.link);
        }
        break;
      }
    }
  }
}
