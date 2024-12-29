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

/** An enumeration for a button function. */
public enum MediaPlayerButtonId {
  /** Play button which overlays the video (for Video only) */
  VideoPlay,
  /** Play button, is hidden while playing. */
  Play,
  /** Pause button, is hidden while paused. */
  Pause,
  /** Stop button. */
  Stop,
  /** Volume mute button. */
  VolumeMute,
  /** Volume unmute button. */
  VolumeUnmute,
  /** Volume max button. */
  VolumeMax,
  /** Toggle button for full screen, is hidden while full screen (for Video only) */
  FullScreen,
  /** Toggle button to restore the screen, is shown only in full screen (for Video only) */
  RestoreScreen,
  /** Toggle button to enable looping, is hidden while repeating is on */
  RepeatOn,
  /** Toggle button to disable looping, is hidden while repeat is off */
  RepeatOff;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
