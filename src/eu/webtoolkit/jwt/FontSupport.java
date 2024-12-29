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

class FontSupport {
  private static Logger logger = LoggerFactory.getLogger(FontSupport.class);

  enum EnabledFontFormats {
    AnyFont,
    TrueTypeOnly;

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return ordinal();
    }
  }

  static class FontMatch {
    private static Logger logger = LoggerFactory.getLogger(FontMatch.class);

    public FontMatch() {
      this.file_ = "";
      this.quality_ = 0.0;
    }

    public FontMatch(final String fileName, double quality) {
      this.file_ = fileName;
      this.quality_ = quality;
    }

    public boolean isMatched() {
      return this.quality_ > 0;
    }

    public String getFileName() {
      return this.file_;
    }

    public void setFileName(final String file) {
      this.file_ = file;
    }

    public double getQuality() {
      return this.quality_;
    }

    public void setQuality(double quality) {
      this.quality_ = quality;
    }

    private String file_;
    private double quality_;
  }

  public FontSupport(WPaintDevice device, FontSupport.EnabledFontFormats anon2) {
    this.device_ = device;
    this.fontCollections_ = new ArrayList<FontSupport.FontCollection>();
    this.lruCache_ = new LinkedList<FontSupport.Matched>();
    this.font_ = null;
    for (int i = 0; i < 5; ++i) {
      this.lruCache_.addLast(new FontSupport.Matched());
    }
  }

  public FontSupport(WPaintDevice device) {
    this(device, FontSupport.EnabledFontFormats.AnyFont);
  }

  public void setDevice(WPaintDevice device) {
    assert false;
  }

  public FontMatch matchFont(final WFont font) {
    for (Iterator<FontSupport.Matched> i_it = this.lruCache_.iterator(); i_it.hasNext(); ) {
      FontSupport.Matched i = i_it.next();
      if (i.font.getGenericFamily() == font.getGenericFamily()
          && (i.font.getSpecificFamilies().toString().equals(font.getSpecificFamilies().toString()))
          && i.font.getWeight() == font.getWeight()
          && i.font.getStyle() == font.getStyle()) {
        CollectionUtils.splice(this.lruCache_, this.lruCache_.iterator(), this.lruCache_, i);
        return this.lruCache_.getFirst().match;
      }
    }
    FontMatch match = new FontMatch();
    for (int i = 0; i < this.fontCollections_.size(); ++i) {
      FontMatch m =
          this.matchFont(
              font, this.fontCollections_.get(i).directory, this.fontCollections_.get(i).recursive);
      if (m.getQuality() > match.getQuality()) {
        Utils.assignFontMatch(match, m);
      }
    }
    if (this.lruCache_.size() >= FONT_CACHE_MAX_SIZE) {
      this.lruCache_.removeLast();
    }
    this.lruCache_.addFirst(new FontSupport.Matched());
    this.lruCache_.getFirst().font = font;
    Utils.assignFontMatch(this.lruCache_.getFirst().match, match);
    return match;
  }

  public WFontMetrics fontMetrics(final WFont font) {
    this.font_ = font;
    WFontMetrics fm = this.device_.getFontMetrics();
    this.font_ = null;
    return fm;
  }

  public WTextItem measureText(
      final WFont font, final CharSequence text, double maxWidth, boolean wordWrap) {
    this.font_ = font;
    WTextItem ti = this.device_.measureText(text, maxWidth, wordWrap);
    this.font_ = null;
    return ti;
  }

  public void drawText(
      final WFont font, final WRectF rect, EnumSet<AlignmentFlag> flags, final CharSequence text) {
    this.font_ = font;
    this.device_.drawText(rect, flags, TextFlag.SingleLine, text, (WPointF) null);
    this.font_ = null;
  }

  public boolean isBusy() {
    return this.font_ != null;
  }

  public String getDrawingFontPath() {
    return this.matchFont(this.font_).getFileName();
  }

  public boolean isCanRender() {
    return false;
  }

  public void addFontCollection(final String directory, boolean recursive) {
    FontSupport.FontCollection c = new FontSupport.FontCollection();
    c.directory = directory;
    c.recursive = recursive;
    this.fontCollections_.add(c);
  }

  public final void addFontCollection(final String directory) {
    addFontCollection(directory, true);
  }

  private WPaintDevice device_;
  private static final int FONT_CACHE_MAX_SIZE = 10;

  static class FontCollection {
    private static Logger logger = LoggerFactory.getLogger(FontCollection.class);

    public String directory;
    public boolean recursive;
  }

  private List<FontSupport.FontCollection> fontCollections_;

  static class Matched {
    private static Logger logger = LoggerFactory.getLogger(Matched.class);

    public WFont font;
    public FontMatch match;

    public Matched() {
      this.font = new WFont();
      this.match = new FontMatch();
    }
  }

  private LinkedList<FontSupport.Matched> lruCache_;
  private WFont font_;

  private FontMatch matchFont(final WFont font, final String directory, boolean recursive) {
    if (!FileUtils.exists(directory) || !FileUtils.isDirectory(directory)) {
      logger.error(
          new StringWriter()
              .append("cannot read directory '")
              .append(directory)
              .append("'")
              .toString());
      return new FontMatch();
    }
    List<String> fontNames = new ArrayList<String>();
    String families = font.getSpecificFamilies().toString();
    StringUtils.split(fontNames, families, ",", false);
    for (int i = 0; i < fontNames.size(); ++i) {
      String s = fontNames.get(i).toLowerCase();
      s = Utils.strip(s, "\"'");
      s = StringUtils.replace(s, ' ', "");
      fontNames.set(i, s);
    }
    switch (font.getGenericFamily()) {
      case Serif:
        fontNames.add("times");
        fontNames.add("timesnewroman");
        break;
      case SansSerif:
        fontNames.add("helvetica");
        fontNames.add("arialunicode");
        fontNames.add("arial");
        fontNames.add("verdana");
        break;
      case Cursive:
        fontNames.add("zapfchancery");
        break;
      case Fantasy:
        fontNames.add("western");
        break;
      case Monospace:
        fontNames.add("courier");
        fontNames.add("mscouriernew");
        break;
      default:;
    }
    FontMatch match = new FontMatch();
    this.matchFont(font, fontNames, directory, recursive, match);
    return match;
  }

  private void matchFont(
      final WFont font,
      final List<String> fontNames,
      final String path,
      boolean recursive,
      final FontMatch match) {
    List<String> files = new ArrayList<String>();
    FileUtils.listFiles(path, files);
    for (int i = 0; i < files.size(); ++i) {
      String f = files.get(i);
      if (FileUtils.isDirectory(f)) {
        if (recursive) {
          this.matchFont(font, fontNames, f, recursive, match);
          if (match.getQuality() == 1.0) {
            return;
          }
        }
      } else {
        this.matchFont(font, fontNames, f, match);
        if (match.getQuality() == 1.0) {
          return;
        }
      }
    }
  }

  private void matchFont(
      final WFont font, final List<String> fontNames, final String path, final FontMatch match) {
    if (path.endsWith(".ttf") || path.endsWith(".ttc")) {
      String name = FileUtils.leaf(path).toLowerCase();
      name = name.substring(0, 0 + name.length() - 4);
      StringUtils.replace(name, ' ', "");
      List<String> weightVariants = new ArrayList<String>();
      List<String> styleVariants = new ArrayList<String>();
      if (font.getWeight() == FontWeight.Bold) {
        weightVariants.add("bold");
        weightVariants.add("bf");
      } else {
        weightVariants.add("");
      }
      switch (font.getStyle()) {
        case Normal:
          styleVariants.add("regular");
          styleVariants.add("");
          break;
        case Italic:
          styleVariants.add("italic");
          styleVariants.add("oblique");
          break;
        case Oblique:
          styleVariants.add("oblique");
          break;
      }
      for (int i = 0; i < fontNames.size(); ++i) {
        double q = 1.0 - 0.1 * i;
        if (q <= match.getQuality()) {
          return;
        }
        for (int w = 0; w < weightVariants.size(); ++w) {
          for (int s = 0; s < styleVariants.size(); ++s) {
            String fn = fontNames.get(i) + weightVariants.get(w) + styleVariants.get(s);
            if (fn.equals(name)) {
              Utils.assignFontMatch(match, new FontMatch(path, q));
              return;
            }
          }
        }
      }
    }
  }

  private static Map<String, String> fontRegistry_ = new HashMap<String, String>();
}
