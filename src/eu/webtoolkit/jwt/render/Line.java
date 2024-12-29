/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

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

class Line {
  private static Logger logger = LoggerFactory.getLogger(Line.class);

  public static final double LEFT_MARGIN_X = -1;

  public Line(double x, double y, int page) {
    this.page_ = page;
    this.x_ = x;
    this.y_ = y;
    this.height_ = 0;
    this.baseline_ = 0;
    this.blocks_ = new ArrayList<Block>();
    this.lineBreak_ = false;
  }

  public double getX() {
    return this.x_;
  }

  public double getY() {
    return this.y_;
  }

  public int getPage() {
    return this.page_;
  }

  public double getHeight() {
    return this.height_;
  }

  public double getBottom() {
    return this.y_ + this.height_;
  }

  public void setX(double x) {
    this.x_ = x;
  }

  public void setLineBreak(boolean lineBreak) {
    this.lineBreak_ = lineBreak;
  }

  public void newLine(double x, double y, int page) {
    this.page_ = page;
    this.x_ = x;
    this.y_ = y;
    this.height_ = this.baseline_ = 0;
    this.lineBreak_ = false;
  }

  public void reflow(Block lineFloat) {
    if (!lineFloat.blockLayout.isEmpty()) {
      final BlockBox bb = lineFloat.blockLayout.get(0);
      if (bb.y == this.y_ && bb.page == this.page_ && bb.x <= this.x_) {
        this.x_ += bb.width;
      }
    }
  }

  public void moveToNextPage(
      final List<Block> floats, double minX, double maxX, final WTextRenderer renderer) {
    for (int i = 0; i < this.blocks_.size(); ++i) {
      Block b = this.blocks_.get(i);
      if (b.isFloat()) {
        floats.remove(b);
      }
    }
    PageState ps = new PageState();
    Utils.copyList(floats, ps.floats);
    ps.page = this.page_;
    Block.clearFloats(ps);
    this.page_ = ps.page;
    Utils.copyList(ps.floats, floats);
    double oldY = this.y_;
    this.y_ = 0;
    this.x_ = minX;
    ++this.page_;
    List<Block> blocks = new ArrayList<Block>(this.blocks_);
    this.blocks_.clear();
    Range rangeX = new Range(this.x_, maxX);
    Block.adjustAvailableWidth(this.y_, this.page_, floats, rangeX);
    this.x_ = rangeX.start;
    maxX = rangeX.end;
    for (int i = 0; i < blocks.size(); ++i) {
      Block b = blocks.get(i);
      if (b.isFloat()) {
        b.layoutFloat(
            this.y_, this.page_, floats, this.x_, this.height_, minX, maxX, false, renderer);
        this.reflow(b);
      } else {
        for (int j = 0; j < b.inlineLayout.size(); ++j) {
          final InlineBox ib = b.inlineLayout.get(j);
          if (ib.y == oldY && ib.page == this.page_ - 1) {
            if (ib.x != LEFT_MARGIN_X) {
              ib.x = this.x_;
              this.x_ += ib.width;
            }
            ib.page = this.page_;
            ib.y = this.y_;
          }
        }
      }
      this.blocks_.add(b);
    }
  }

  public void adjustHeight(double height, double baseline, double minLineHeight) {
    if (this.height_ == 0) {
      this.height_ = height;
      this.baseline_ = baseline;
    } else {
      double ascent = Math.max(this.baseline_, baseline);
      double descent = Math.max(this.height_ - this.baseline_, height - baseline);
      this.baseline_ = ascent;
      this.height_ = this.baseline_ + descent;
    }
    if (minLineHeight > this.height_) {
      this.height_ = minLineHeight;
      this.baseline_ += (minLineHeight - this.height_) / 2.0;
    }
  }

  public void finish(
      AlignmentFlag textAlign,
      final List<Block> floats,
      double minX,
      double maxX,
      final WTextRenderer renderer) {
    for (int i = 0; i < this.blocks_.size(); ++i) {
      Block b = this.blocks_.get(this.blocks_.size() - 1 - i);
      if (!b.isFloat()) {
        if (b.getType() != DomElementType.LI && b.isText()) {
          boolean done = false;
          for (int j = 0; j < b.inlineLayout.size(); ++j) {
            final InlineBox ib = b.inlineLayout.get(b.inlineLayout.size() - 1 - j);
            if (ib.utf8Count > 0) {
              char lastChar = b.getText().charAt(ib.utf8Pos + ib.utf8Count - 1);
              if (Block.isWhitespace(lastChar)) {
                --ib.utf8Count;
                ib.width -= ib.whitespaceWidth;
              }
              done = true;
              break;
            }
          }
          if (done) {
            break;
          }
        } else {
          break;
        }
      }
    }
    Range rangeX = new Range(minX, maxX);
    Block.adjustAvailableWidth(this.y_, this.page_, floats, rangeX);
    double whitespace = 0;
    double content = 0;
    List<InlineBox> boxes = new ArrayList<InlineBox>();
    for (int i = 0; i < this.blocks_.size(); ++i) {
      Block b = this.blocks_.get(i);
      if (b.isFloat()) {
        b.layoutFloat(
            this.y_ + this.height_, this.page_, floats, minX, 0, minX, maxX, false, renderer);
      } else {
        for (int j = 0; j < b.inlineLayout.size(); ++j) {
          final InlineBox ib = b.inlineLayout.get(j);
          if (ib.y == this.y_ && ib.page == this.page_) {
            String va = b.cssProperty(Property.StyleVerticalAlign);
            if (va.equals("top")) {
              ib.y = this.y_;
            } else {
              if (va.equals("bottom")) {
                ib.y = this.y_ + this.height_ - ib.height;
              } else {
                ib.y += this.baseline_ - ib.baseline;
              }
            }
            if (ib.x != LEFT_MARGIN_X) {
              boxes.add(ib);
              content += ib.width;
              ib.whitespaceCount = 0;
              if (b.isText()) {
                for (int k = 0; k < ib.utf8Count; ++k) {
                  if (Block.isWhitespace(b.getText().charAt(ib.utf8Pos + k))) {
                    ++ib.whitespaceCount;
                  }
                }
                content -= ib.whitespaceWidth * ib.whitespaceCount;
                whitespace += ib.whitespaceWidth * ib.whitespaceCount;
              }
            } else {
              ib.x = rangeX.start - ib.width;
            }
          }
        }
      }
    }
    double spaceFactor = 1.0;
    switch (textAlign) {
      case Left:
        break;
      case Right:
        rangeX.start = rangeX.end - content - whitespace;
        break;
      case Center:
        rangeX.start += (rangeX.end - rangeX.start - content - whitespace) / 2;
        break;
      case Justify:
        if (!this.lineBreak_) {
          double remaining = rangeX.end - rangeX.start - content;
          if (whitespace > 0) {
            spaceFactor = remaining / whitespace;
          }
        }
        break;
      default:
        logger.error(
            new StringWriter()
                .append("unsupported text-align attribute: ")
                .append(String.valueOf((int) textAlign.getValue()))
                .toString());
    }
    double x = rangeX.start;
    for (int i = 0; i < boxes.size(); ++i) {
      final InlineBox ib = boxes.get(i);
      ib.x = x;
      double contentWidth = ib.width - ib.whitespaceWidth * ib.whitespaceCount;
      ib.whitespaceWidth *= spaceFactor;
      ib.width = contentWidth + ib.whitespaceWidth * ib.whitespaceCount;
      x += ib.width;
    }
    this.blocks_.clear();
  }

  public void addBlock(Block block) {
    if (this.blocks_.isEmpty() || this.blocks_.get(this.blocks_.size() - 1) != block) {
      this.blocks_.add(block);
    }
  }

  private int page_;
  private double x_;
  private double y_;
  private double height_;
  private double baseline_;
  private List<Block> blocks_;
  private boolean lineBreak_;
}
