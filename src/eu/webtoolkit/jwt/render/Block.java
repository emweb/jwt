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

class Block {
  private static Logger logger = LoggerFactory.getLogger(Block.class);

  public Block(net.n3.nanoxml.XMLElement node, Block parent) {
    this.inlineLayout = new ArrayList<InlineBox>();
    this.blockLayout = new ArrayList<BlockBox>();
    this.node_ = node;
    this.parent_ = parent;
    this.offsetChildren_ = new ArrayList<Block>();
    this.offsetParent_ = null;
    this.type_ = DomElementType.UNKNOWN;
    this.classes_ = new ArrayList<String>();
    this.float_ = FloatSide.None;
    this.inline_ = false;
    this.children_ = new ArrayList<Block>();
    this.currentTheadBlock_ = null;
    this.currentWidth_ = 0;
    this.contentsHeight_ = 0;
    this.css_ = new HashMap<String, Block.PropertyValue>();
    this.font_ = new WFont();
    this.styleSheet_ = null;
    this.noPropertyCache_ = new HashSet<Property>();
    if (node != null) {
      if (RenderUtils.isXmlElement(node)) {
        this.type_ = DomElement.parseTagName(node.getName());
        if (this.type_ == DomElementType.UNKNOWN) {
          logger.error(
              new StringWriter().append("unsupported element: ").append(node.getName()).toString());
          this.type_ = DomElementType.DIV;
        }
        String s = this.attributeValue("class");
        StringUtils.split(this.classes_, s, " ", false);
      }
      RenderUtils.fetchBlockChildren(node, this, this.children_);
    }
  }

  public Block getParent() {
    return this.parent_;
  }

  public List<Block> getChildren() {
    return this.children_;
  }

  public void determineDisplay() {
    String fl = this.cssProperty(Property.StyleFloat);
    if (fl.length() != 0) {
      if (fl.equals("left")) {
        this.float_ = FloatSide.Left;
      } else {
        if (fl.equals("right")) {
          this.float_ = FloatSide.Right;
        } else {
          unsupportedCssValue(Property.StyleFloat, fl);
        }
      }
    } else {
      if (this.type_ == DomElementType.IMG || this.isTable()) {
        String align = this.attributeValue("align");
        if (align.length() != 0) {
          if (align.equals("left")) {
            this.float_ = FloatSide.Left;
          } else {
            if (align.equals("right")) {
              this.float_ = FloatSide.Right;
            } else {
              unsupportedAttributeValue("align", align);
            }
          }
        }
      }
    }
    boolean allChildrenInline = true;
    for (int i = 0; i < this.children_.size(); ++i) {
      Block b = this.children_.get(i);
      b.determineDisplay();
      if (!b.isFloat() && !b.isInline()) {
        allChildrenInline = false;
      }
    }
    if (!allChildrenInline) {
      int firstToGroup = -1;
      for (int i = 0; i <= this.children_.size(); ++i) {
        Block b = i < this.children_.size() ? this.children_.get(i) : null;
        if (!(b != null) || !b.isFloat()) {
          if (b != null && b.inline_ && firstToGroup == -1) {
            firstToGroup = i;
          }
          if ((!(b != null) || !b.inline_) && firstToGroup != -1 && (int) i > firstToGroup - 1) {
            Block anonymous = new Block((net.n3.nanoxml.XMLElement) null, this);
            this.children_.add(0 + i, anonymous);
            anonymous.inline_ = false;
            for (int j = firstToGroup; j < i; ++j) {
              anonymous.children_.add(this.children_.get(firstToGroup));
              this.children_.remove(0 + firstToGroup);
            }
            i -= i - firstToGroup;
            firstToGroup = -1;
          }
        }
      }
    }
    switch (this.type_) {
      case UNKNOWN:
        if (allChildrenInline) {
          this.inline_ = true;
        }
        break;
      default:
        if (!this.isFloat()) {
          String display = this.cssProperty(Property.StyleDisplay);
          if (display.length() != 0) {
            if (display.equals("inline")) {
              this.inline_ = true;
            } else {
              if (display.equals("block")) {
                this.inline_ = false;
              } else {
                logger.error(
                    new StringWriter()
                        .append("display '")
                        .append(display)
                        .append("' is not supported.")
                        .toString());
                this.inline_ = false;
              }
            }
          } else {
            this.inline_ = DomElement.isDefaultInline(this.type_);
          }
          if (this.inline_ && !allChildrenInline) {
            logger.error(
                new StringWriter()
                    .append("inline element ")
                    .append(DomElement.tagName(this.type_))
                    .append(" cannot contain block elements")
                    .toString());
          }
        } else {
          this.inline_ = false;
        }
    }
    if (this.type_ == DomElementType.TABLE) {
      List<Integer> rowSpan = new ArrayList<Integer>();
      int row = this.numberTableCells(0, rowSpan);
      int maxRowSpan = 0;
      for (int i = 0; i < rowSpan.size(); ++i) {
        maxRowSpan = Math.max(maxRowSpan, rowSpan.get(i));
      }
      this.tableRowCount_ = row + maxRowSpan;
      this.tableColCount_ = rowSpan.size();
    }
  }

  public boolean normalizeWhitespace(boolean haveWhitespace, final net.n3.nanoxml.XMLElement doc) {
    boolean whitespaceIn = haveWhitespace;
    if (!this.isInline()) {
      haveWhitespace = true;
    }
    if (this.type_ == DomElementType.UNKNOWN && this.isText()) {
      haveWhitespace = RenderUtils.normalizeWhitespace(this, this.node_, haveWhitespace, doc);
    } else {
      for (int i = 0; i < this.children_.size(); ++i) {
        Block b = this.children_.get(i);
        haveWhitespace = b.normalizeWhitespace(haveWhitespace, doc);
      }
    }
    if (!this.isInline()) {
      return whitespaceIn;
    } else {
      return haveWhitespace;
    }
  }

  public boolean isFloat() {
    return this.float_ != FloatSide.None;
  }

  public boolean isInline() {
    return this.inline_;
  }

  public DomElementType getType() {
    return this.type_;
  }

  public boolean isText() {
    return this.node_ != null && this.children_.isEmpty() && this.type_ == DomElementType.UNKNOWN
        || this.type_ == DomElementType.LI;
  }

  public String getText() {
    if (this.type_ == DomElementType.LI) {
      return this.getGenerateItem().toString();
    } else {
      return RenderUtils.nodeValueToString(this.node_);
    }
  }

  public boolean isInlineChildren() {
    if (this.inline_) {
      return true;
    }
    for (int i = 0; i < this.children_.size(); ++i) {
      if (!this.children_.get(i).isFloat() && this.children_.get(i).inline_) {
        return true;
      }
    }
    return false;
  }

  public AlignmentFlag getHorizontalAlignment() {
    String marginLeft = this.cssProperty(Property.StyleMarginLeft);
    String marginRight = this.cssProperty(Property.StyleMarginRight);
    if (marginLeft.equals("auto")) {
      if (marginRight.equals("auto")) {
        return AlignmentFlag.Center;
      } else {
        return AlignmentFlag.Right;
      }
    } else {
      if (marginRight.equals("auto")) {
        return AlignmentFlag.Left;
      } else {
        return AlignmentFlag.Justify;
      }
    }
  }

  public AlignmentFlag getVerticalAlignment() {
    String va = this.cssProperty(Property.StyleVerticalAlign);
    if (va.length() == 0) {
      va = this.attributeValue("valign");
    }
    if (va.length() == 0 || va.equals("middle")) {
      return AlignmentFlag.Middle;
    } else {
      if (va.equals("bottom")) {
        return AlignmentFlag.Bottom;
      } else {
        return AlignmentFlag.Top;
      }
    }
  }

  public FloatSide getFloatSide() {
    return this.float_;
  }

  public boolean isTableCell() {
    return this.type_ == DomElementType.TD || this.type_ == DomElementType.TH;
  }

  public boolean isTable() {
    return this.type_ == DomElementType.TABLE;
  }

  public Block getTable() {
    Block result = this.parent_;
    while (result != null && !result.isTable()) {
      result = result.parent_;
    }
    return result;
  }

  public boolean isTableCollapseBorders() {
    return this.cssProperty(Property.StyleBorderCollapse).equals("collapse");
  }

  public double layoutBlock(
      final PageState ps,
      boolean canIncreaseWidth,
      final WTextRenderer renderer,
      double collapseMarginTop,
      double collapseMarginBottom,
      double cellHeight) {
    String pageBreakBefore = this.cssProperty(Property.StylePageBreakBefore);
    if (pageBreakBefore.equals("always")) {
      this.pageBreak(ps);
      collapseMarginTop = 0;
    }
    double origMinX = ps.minX;
    double origMaxX = ps.maxX;
    double inCollapseMarginTop = collapseMarginTop;
    double inY = ps.y;
    double spacerTop = 0;
    double spacerBottom = 0;
    if (cellHeight >= 0) {
      double ch = this.contentsHeight_;
      AlignmentFlag va = this.getVerticalAlignment();
      switch (va) {
        case Top:
          spacerBottom = cellHeight - ch;
          break;
        case Middle:
          spacerTop = spacerBottom = (cellHeight - ch) / 2;
          break;
        case Bottom:
          spacerTop = cellHeight - ch;
          break;
        default:
          break;
      }
    }
    this.blockLayout.clear();
    double startY;
    double marginTop = this.cssMargin(Side.Top, renderer.getFontScale());
    ps.y -= Math.min(marginTop, collapseMarginTop);
    collapseMarginTop = Math.max(marginTop, collapseMarginTop);
    collapseMarginBottom = 0;
    startY = ps.y;
    ps.y += marginTop;
    if (!this.isFloat()) {
      startY = ps.y;
    }
    int startPage = ps.page;
    ps.y += this.cssBorderWidth(Side.Top, renderer.getFontScale());
    ps.minX += this.cssMargin(Side.Left, renderer.getFontScale());
    ps.maxX -= this.cssMargin(Side.Right, renderer.getFontScale());
    double cssSetWidth = this.cssWidth(renderer.getFontScale());
    if (this.isTable()) {
      if (cssSetWidth > 0) {
        cssSetWidth -=
            this.cssBorderWidth(Side.Left, renderer.getFontScale())
                + this.cssBorderWidth(Side.Right, renderer.getFontScale());
        cssSetWidth = Math.max(0.0, cssSetWidth);
      }
      this.layoutTable(ps, canIncreaseWidth, renderer, cssSetWidth);
    } else {
      double width = cssSetWidth;
      boolean paddingBorderWithinWidth =
          this.isTableCell() && isPercentageLength(this.cssProperty(Property.StyleWidth));
      if (width >= 0) {
        if (!paddingBorderWithinWidth) {
          width +=
              this.cssPadding(Side.Left, renderer.getFontScale())
                  + this.cssBorderWidth(Side.Left, renderer.getFontScale())
                  + this.cssPadding(Side.Right, renderer.getFontScale())
                  + this.cssBorderWidth(Side.Right, renderer.getFontScale());
        }
        if (this.isTableCell()) {
          if (width < ps.maxX - ps.minX) {
            width = ps.maxX - ps.minX;
          }
          canIncreaseWidth = false;
        }
        if (width > ps.maxX - ps.minX) {
          ps.maxX = ps.minX + width;
        }
        AlignmentFlag hAlign = this.getHorizontalAlignment();
        switch (hAlign) {
          case Justify:
          case Left:
            ps.maxX = ps.minX + width;
            break;
          case Center:
            ps.minX = ps.minX + (ps.maxX - ps.minX - width) / 2;
            ps.maxX = ps.minX + width;
            break;
          case Right:
            ps.minX = ps.maxX - width;
            break;
          default:
            break;
        }
      }
      if (this.type_ == DomElementType.IMG) {
        double height = this.cssHeight(renderer.getFontScale());
        String src = this.attributeValue("src");
        if (width <= 0 || height <= 0) {
          WDataInfo imgInfo = new WDataInfo();
          if (DataUri.isDataUri(src)) {
            imgInfo.setDataUri(src);
          } else {
            imgInfo.setUrl(src);
            imgInfo.setFilePath(src);
          }
          WPainter.Image image = new WPainter.Image(imgInfo);
          if (height <= 0) {
            height = image.getHeight();
          }
          if (width <= 0) {
            width = image.getWidth();
          }
        }
        if (ps.y + height > renderer.textHeight(ps.page)) {
          this.pageBreak(ps);
          startPage = ps.page;
          startY = ps.y;
          ps.y += this.cssBorderWidth(Side.Top, renderer.getFontScale());
        }
        ps.y += height;
        ps.maxX = Math.max(ps.minX + width, ps.maxX);
      } else {
        double borderFactor = 1.0;
        if (this.isTableCell()) {
          Block t = this.getTable();
          if (t != null && t.isTableCollapseBorders()) {
            borderFactor = 0.5;
          }
        }
        double cMinX =
            ps.minX
                + this.cssPadding(Side.Left, renderer.getFontScale())
                + borderFactor * this.cssBorderWidth(Side.Left, renderer.getFontScale());
        double cMaxX =
            ps.maxX
                - this.cssPadding(Side.Right, renderer.getFontScale())
                - borderFactor * this.cssBorderWidth(Side.Right, renderer.getFontScale());
        cMaxX = Math.max(cMaxX, cMinX);
        this.currentWidth_ = cMaxX - cMinX;
        ps.y += this.cssPadding(Side.Top, renderer.getFontScale());
        advance(ps, spacerTop, renderer);
        if (this.isInlineChildren()) {
          Line line = new Line(cMinX, ps.y, ps.page);
          renderer.getPainter().setFont(this.cssFont(renderer.getFontScale()));
          cMaxX = this.layoutInline(line, ps.floats, cMinX, cMaxX, canIncreaseWidth, renderer);
          line.setLineBreak(true);
          line.finish(this.getCssTextAlign(), ps.floats, cMinX, cMaxX, renderer);
          ps.y = line.getBottom();
          ps.page = line.getPage();
        } else {
          double minY = ps.y;
          int minPage = ps.page;
          if (this.type_ == DomElementType.LI) {
            Line line = new Line(0, ps.y, ps.page);
            double x2 = 1000;
            x2 = this.layoutInline(line, ps.floats, cMinX, x2, false, renderer);
            line.setLineBreak(true);
            line.finish(AlignmentFlag.Left, ps.floats, cMinX, x2, renderer);
            this.inlineLayout.get(0).x -= this.inlineLayout.get(0).width;
            minY = line.getBottom();
            minPage = line.getPage();
            ps.y = line.getY();
            ps.page = line.getPage();
          }
          for (int i = 0; i < this.children_.size(); ++i) {
            Block c = this.children_.get(i);
            if (c.isFloat()) {
              cMaxX =
                  c.layoutFloat(
                      ps.y, ps.page, ps.floats, cMinX, 0, cMinX, cMaxX, canIncreaseWidth, renderer);
            } else {
              if (c.isPositionedAbsolutely()) {
                if (!(c.offsetParent_ != null)) {
                  c.setOffsetParent();
                }
                PageState absolutePs = new PageState();
                absolutePs.y = ps.y;
                absolutePs.page = ps.page;
                absolutePs.minX = ps.minX;
                absolutePs.maxX = ps.maxX;
                Utils.copyList(ps.floats, absolutePs.floats);
                c.layoutBlock(absolutePs, false, renderer, 0, 0);
              } else {
                double copyMinX = ps.minX;
                double copyMaxX = ps.maxX;
                ps.minX = cMinX;
                ps.maxX = cMaxX;
                collapseMarginBottom =
                    c.layoutBlock(
                        ps, canIncreaseWidth, renderer, collapseMarginTop, collapseMarginBottom);
                collapseMarginTop = collapseMarginBottom;
                cMaxX = ps.maxX;
                ps.minX = copyMinX;
                ps.maxX = copyMaxX;
              }
            }
          }
          if (ps.y < minY && ps.page == minPage) {
            ps.y = minY;
          }
        }
        ps.maxX =
            cMaxX
                + this.cssPadding(Side.Right, renderer.getFontScale())
                + borderFactor * this.cssBorderWidth(Side.Right, renderer.getFontScale());
        advance(ps, spacerBottom, renderer);
        ps.y += this.cssPadding(Side.Bottom, renderer.getFontScale());
      }
    }
    ps.y += this.cssBorderWidth(Side.Bottom, renderer.getFontScale());
    double marginBottom = this.cssMargin(Side.Bottom, renderer.getFontScale());
    ps.y -= collapseMarginBottom;
    double height = this.cssHeight(renderer.getFontScale());
    if (this.isTableCell()) {
      this.contentsHeight_ = Math.max(0.0, diff(ps.y, ps.page, startY, startPage, renderer));
    }
    if (height >= 0) {
      int prevPage = ps.page;
      double prevY = ps.y;
      ps.page = startPage;
      ps.y = startY;
      if (this.isFloat()) {
        ps.y += marginTop;
      }
      advance(ps, height, renderer);
      if (this.isTable() || this.isTableCell()) {
        if (prevPage > ps.page || prevPage == ps.page && prevY > ps.y) {
          ps.page = prevPage;
          ps.y = prevY;
        }
      }
    }
    collapseMarginBottom = Math.max(marginBottom, collapseMarginBottom);
    if (this.isFloat()) {
      ps.minX -= this.cssMargin(Side.Left, renderer.getFontScale());
      ps.maxX += this.cssMargin(Side.Right, renderer.getFontScale());
      ps.y += collapseMarginBottom;
      collapseMarginBottom = 0;
    }
    for (int i = startPage; i <= ps.page; ++i) {
      double boxY = i == startPage ? startY : 0;
      double boxH;
      if (i == ps.page) {
        boxH = ps.y - boxY;
      } else {
        boxH = Math.max(0.0, this.maxChildrenLayoutY(i) - boxY);
      }
      if (boxH > 0) {
        this.blockLayout.add(new BlockBox());
        final BlockBox box = this.blockLayout.get(this.blockLayout.size() - 1);
        box.page = i;
        box.x = ps.minX;
        box.width = ps.maxX - ps.minX;
        box.y = boxY;
        box.height = boxH;
      }
    }
    if (ps.y != 0) {
      ps.y += collapseMarginBottom;
    } else {
      collapseMarginBottom = 0;
    }
    if (this.blockLayout.isEmpty()) {
      ps.page = startPage;
      ps.y = inY;
      collapseMarginBottom = inCollapseMarginTop;
      this.blockLayout.add(new BlockBox());
      final BlockBox box = this.blockLayout.get(this.blockLayout.size() - 1);
      box.page = startPage;
      box.x = ps.minX;
      box.width = ps.maxX - ps.minX;
      box.y = inY;
      box.height = 0;
    }
    for (int i = 0; i < this.offsetChildren_.size(); ++i) {
      this.offsetChildren_.get(i).layoutAbsolute(renderer);
    }
    if (!this.isTableCell()
        && ps.maxX - ps.minX == cssSetWidth
        && isPercentageLength(this.cssProperty(Property.StyleWidth))) {
      ps.maxX = origMaxX;
    } else {
      if (ps.maxX < origMaxX) {
        ps.maxX = origMaxX;
      } else {
        if (!this.isFloat()) {
          ps.minX -= this.cssMargin(Side.Left, renderer.getFontScale());
          ps.maxX += this.cssMargin(Side.Right, renderer.getFontScale());
        }
      }
    }
    ps.minX = origMinX;
    String pageBreakAfter = this.cssProperty(Property.StylePageBreakAfter);
    if (pageBreakAfter.equals("always")) {
      this.pageBreak(ps);
      return 0;
    } else {
      return collapseMarginBottom;
    }
  }

  public final double layoutBlock(
      final PageState ps,
      boolean canIncreaseWidth,
      final WTextRenderer renderer,
      double collapseMarginTop,
      double collapseMarginBottom) {
    return layoutBlock(ps, canIncreaseWidth, renderer, collapseMarginTop, collapseMarginBottom, -1);
  }

  public void collectStyles(final StringBuilder ss) {
    for (int i = 0; i < this.children_.size(); ++i) {
      if (this.children_.get(i).type_ == DomElementType.STYLE) {
        ss.append(RenderUtils.nodeValueToString(this.children_.get(i).node_));

        this.children_.remove(0 + i);
        --i;
      } else {
        this.children_.get(i).collectStyles(ss);
      }
    }
  }

  public void setStyleSheet(StyleSheet styleSheet) {
    this.styleSheet_ = styleSheet;
    this.css_.clear();
    this.noPropertyCache_.clear();
    for (int i = 0; i < this.children_.size(); ++i) {
      this.children_.get(i).setStyleSheet(styleSheet);
    }
  }

  public void actualRender(
      final WTextRenderer renderer, final WPainter painter, final LayoutBox lb) {
    if (this.type_ == DomElementType.IMG) {
      LayoutBox bb = this.toBorderBox(lb, renderer.getFontScale());
      this.renderBorders(bb, renderer, painter, EnumSet.of(Side.Top, Side.Bottom));
      double left =
          renderer.getMargin(Side.Left)
              + bb.x
              + this.cssBorderWidth(Side.Left, renderer.getFontScale());
      double top =
          renderer.getMargin(Side.Top)
              + bb.y
              + this.cssBorderWidth(Side.Top, renderer.getFontScale());
      double width = bb.width;
      double height = bb.height;
      WRectF rect = new WRectF(left, top, width, height);
      String src = this.attributeValue("src");
      WDataInfo imgInfo = new WDataInfo();
      if (DataUri.isDataUri(src)) {
        imgInfo.setDataUri(src);
      } else {
        imgInfo.setUrl(src);
        imgInfo.setFilePath(src);
      }
      painter.drawImage(rect, new WPainter.Image(imgInfo, (int) width, (int) height));
    } else {
      LayoutBox bb = this.toBorderBox(lb, renderer.getFontScale());
      WRectF rect =
          new WRectF(
              bb.x + renderer.getMargin(Side.Left),
              bb.y + renderer.getMargin(Side.Top),
              bb.width,
              bb.height);
      String s = this.cssProperty(Property.StyleBackgroundColor);
      if (s.length() != 0) {
        WColor c = new WColor(new WString(s));
        painter.fillRect(rect, new WBrush(c));
      }
      EnumSet<Side> verticals = EnumSet.noneOf(Side.class);
      if (lb.page == this.getFirstLayoutPage()) {
        verticals.add(Side.Top);
      }
      if (lb.page == this.getLastLayoutPage()) {
        verticals.add(Side.Bottom);
      }
      this.renderBorders(bb, renderer, painter, verticals);
      if (this.type_ == DomElementType.THEAD) {
        if (this.currentTheadBlock_ == null && !this.blockLayout.isEmpty()) {
          this.currentTheadBlock_ = this.blockLayout.get(0);
        }
        for (int j = 0; j < this.children_.size(); ++j) {
          if (this.currentTheadBlock_ != lb) {
            this.children_.get(j).reLayout(this.currentTheadBlock_, lb);
          }
          this.children_.get(j).render(renderer, painter, lb.page);
        }
        this.currentTheadBlock_ = lb;
      }
    }
    if (this.type_ != DomElementType.THEAD) {
      for (int i = 0; i < this.children_.size(); ++i) {
        this.children_.get(i).render(renderer, painter, lb.page);
      }
    }
  }

  public void render(final WTextRenderer renderer, final WPainter painter, int page) {
    boolean painterTranslated = false;
    if (this.cssProperty(Property.StylePosition).equals("relative")) {
      painter.save();
      painterTranslated = true;
      LayoutBox box = this.getLayoutTotal();
      double left =
          this.cssDecodeLength(
              this.cssProperty(Property.StyleLeft),
              renderer.getFontScale(),
              0,
              Block.PercentageRule.PercentageOfParentSize,
              box.width);
      double top =
          this.cssDecodeLength(
              this.cssProperty(Property.StyleTop),
              renderer.getFontScale(),
              0,
              Block.PercentageRule.PercentageOfParentSize,
              box.height);
      painter.translate(left, top);
    }
    if (this.isText()) {
      this.renderText(this.getText(), renderer, painter, page);
      if (this.type_ != DomElementType.LI) {
        if (painterTranslated) {
          painter.restore();
        }
        return;
      }
    }
    int first = this.type_ == DomElementType.LI ? 1 : 0;
    for (int i = first; i < this.inlineLayout.size(); ++i) {
      final LayoutBox lb = this.inlineLayout.get(i);
      if (lb.page == page) {
        renderer.paintNode(painter, new WTextRenderer.Node(this, lb, renderer));
      }
    }
    for (int i = 0; i < this.blockLayout.size(); ++i) {
      final LayoutBox lb = this.blockLayout.get(i);
      if (lb.page == page) {
        renderer.paintNode(painter, new WTextRenderer.Node(this, lb, renderer));
      }
    }
    if (this.inlineLayout.isEmpty() && this.blockLayout.isEmpty()) {
      for (int i = 0; i < this.children_.size(); ++i) {
        this.children_.get(i).render(renderer, painter, page);
      }
    }
    if (painterTranslated) {
      painter.restore();
    }
  }

  public static void clearFloats(final PageState ps) {
    for (int i = 0; i < ps.floats.size(); ++i) {
      Block b = ps.floats.get(i);
      final BlockBox bb = b.blockLayout.get(b.blockLayout.size() - 1);
      if (bb.page <= ps.page) {
        ps.floats.remove(0 + i);
        --i;
      }
    }
  }

  public static void clearFloats(final PageState ps, double minWidth) {
    for (; !ps.floats.isEmpty(); ) {
      Block b = ps.floats.get(0);
      ps.y =
          b.blockLayout.get(b.blockLayout.size() - 1).y
              + b.blockLayout.get(b.blockLayout.size() - 1).height;
      ps.page = b.blockLayout.get(b.blockLayout.size() - 1).page;
      ps.floats.remove(0);
      Range rangeX = new Range(ps.minX, ps.maxX);
      adjustAvailableWidth(ps.y, ps.page, ps.floats, rangeX);
      if (!isEpsilonMore(minWidth, rangeX.end - rangeX.start)) {
        break;
      }
    }
  }

  public List<InlineBox> inlineLayout;
  public List<BlockBox> blockLayout;

  public static void adjustAvailableWidth(
      double y, int page, final List<Block> floats, final Range rangeX) {
    for (int i = 0; i < floats.size(); ++i) {
      Block b = floats.get(i);
      for (int j = 0; j < b.blockLayout.size(); ++j) {
        final BlockBox block = b.blockLayout.get(j);
        if (block.page == page) {
          if (block.y <= y && y < block.y + block.height) {
            if (floats.get(i).getFloatSide() == FloatSide.Left) {
              rangeX.start = Math.max(rangeX.start, block.x + block.width);
            } else {
              rangeX.end = Math.min(rangeX.end, block.x);
            }
            if (rangeX.end <= rangeX.start) {
              return;
            }
          }
        }
      }
    }
  }

  public static boolean isWhitespace(char c) {
    return c == ' ' || c == '\n' || c == '\r' || c == '\t';
  }

  public String getId() {
    return this.attributeValue("id");
  }

  public List<String> getClasses() {
    return this.classes_;
  }

  String cssProperty(Property property) {
    if (!(this.node_ != null)) {
      return "";
    }
    if (this.noPropertyCache_.contains(property) != false) {
      return "";
    }
    if (this.css_.isEmpty()) {
      if (this.styleSheet_ != null) {
        for (int i = 0; i < this.styleSheet_.getRulesetSize(); ++i) {
          Specificity s = Match.isMatch(this, this.styleSheet_.rulesetAt(i).getSelector());
          if (s.isValid()) {
            this.fillinStyle(
                this.styleSheet_.rulesetAt(i).getDeclarationBlock().getDeclarationString(), s);
          }
        }
      }
      this.fillinStyle(this.attributeValue("style"), new Specificity(1, 0, 0, 0));
    }
    Block.PropertyValue i = this.css_.get(DomElement.cssName(property));
    if (i != null) {
      return i.value_;
    } else {
      this.noPropertyCache_.add(property);
      return "";
    }
  }

  public String attributeValue(String attribute) {
    if (!(this.node_ != null)) {
      return "";
    }
    net.n3.nanoxml.XMLAttribute attr = this.node_.findAttribute(attribute);
    if (attr != null) {
      return attr.getValue();
    } else {
      return "";
    }
  }

  static class CssLength {
    private static Logger logger = LoggerFactory.getLogger(CssLength.class);

    public double length;
    public boolean defined;
  }

  enum PercentageRule {
    PercentageOfFontSize,
    PercentageOfParentSize,
    IgnorePercentage;

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return ordinal();
    }
  }

  static class PropertyValue {
    private static Logger logger = LoggerFactory.getLogger(PropertyValue.class);

    public PropertyValue() {
      this.value_ = "";
      this.s_ = new Specificity();
    }

    public PropertyValue(final String value, final Specificity s) {
      this.value_ = value;
      this.s_ = s;
    }

    public String value_;
    public Specificity s_;
  }

  enum Corner {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight;

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return ordinal();
    }
  }

  enum WidthType {
    AsSetWidth,
    MinimumWidth,
    MaximumWidth;

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return ordinal();
    }
  }

  static class BorderElement {
    private static Logger logger = LoggerFactory.getLogger(BorderElement.class);

    public Block block;
    public Side side;

    public BorderElement() {
      this.block = null;
      this.side = Side.Left;
    }

    public BorderElement(Block aBlock, Side aSide) {
      this.block = aBlock;
      this.side = aSide;
    }
  }

  private net.n3.nanoxml.XMLElement node_;
  private Block parent_;
  private List<Block> offsetChildren_;
  private Block offsetParent_;
  private DomElementType type_;
  private List<String> classes_;
  private FloatSide float_;
  private boolean inline_;
  private List<Block> children_;
  private LayoutBox currentTheadBlock_;
  private double currentWidth_;
  private double contentsHeight_;
  private Map<String, Block.PropertyValue> css_;
  private WFont font_;
  private StyleSheet styleSheet_;
  private Set<Property> noPropertyCache_;
  private int tableRowCount_;
  private int tableColCount_;
  private int cellRow_;
  private int cellCol_;

  private int attributeValue(String attribute, int defaultValue) {
    String valueStr = this.attributeValue(attribute);
    if (valueStr.length() != 0) {
      return Integer.parseInt(valueStr);
    } else {
      return defaultValue;
    }
  }

  private void updateAggregateProperty(
      final String property, final String aggregate, final Specificity spec, final String value) {
    if (this.css_.get(property + aggregate) == null
        || this.css_.get(property + aggregate).s_.isSmallerOrEqualThen(spec)) {
      this.css_.put(property + aggregate, new Block.PropertyValue(value, spec));
    }
  }

  private void fillinStyle(final String style, final Specificity specificity) {
    if (style.length() == 0) {
      return;
    }
    List<String> values = new ArrayList<String>();
    StringUtils.split(values, style, ";", false);
    for (int i = 0; i < values.size(); ++i) {
      List<String> namevalue = new ArrayList<String>();
      StringUtils.split(namevalue, values.get(i), ":", false);
      if (namevalue.size() == 2) {
        String n = namevalue.get(0);
        String v = namevalue.get(1);
        n = n.trim();
        v = v.trim();
        this.updateAggregateProperty(n, "", specificity, v);
        if (isAggregate(n)) {
          List<String> allvalues = new ArrayList<String>();
          StringUtils.split(allvalues, v, " ", false);
          int count = 0;
          for (int j = 0; j < allvalues.size(); ++j) {
            String vj = allvalues.get(j);
            if (vj.charAt(0) < '0' || vj.charAt(0) > '9') {
              break;
            }
            ++count;
          }
          if (count == 0) {
            count = allvalues.size();
          }
          if (count == 1) {
            this.updateAggregateProperty(n, "-top", specificity, v);
            this.updateAggregateProperty(n, "-right", specificity, v);
            this.updateAggregateProperty(n, "-bottom", specificity, v);
            this.updateAggregateProperty(n, "-left", specificity, v);
          } else {
            if (count == 2) {
              String v1 = allvalues.get(0);
              this.updateAggregateProperty(n, "-top", specificity, v1);
              this.updateAggregateProperty(n, "-bottom", specificity, v1);
              String v2 = allvalues.get(1);
              this.updateAggregateProperty(n, "-right", specificity, v2);
              this.updateAggregateProperty(n, "-left", specificity, v2);
            } else {
              if (count == 3) {
                String v1 = allvalues.get(0);
                this.updateAggregateProperty(n, "-top", specificity, v1);
                String v2 = allvalues.get(1);
                this.updateAggregateProperty(n, "-right", specificity, v2);
                this.updateAggregateProperty(n, "-left", specificity, v2);
                String v3 = allvalues.get(2);
                this.updateAggregateProperty(n, "-bottom", specificity, v3);
              } else {
                String v1 = allvalues.get(0);
                this.updateAggregateProperty(n, "-top", specificity, v1);
                String v2 = allvalues.get(1);
                this.updateAggregateProperty(n, "-right", specificity, v2);
                String v3 = allvalues.get(2);
                this.updateAggregateProperty(n, "-bottom", specificity, v3);
                String v4 = allvalues.get(3);
                this.updateAggregateProperty(n, "-left", specificity, v4);
              }
            }
          }
        }
      }
    }
  }

  private boolean isPositionedAbsolutely() {
    String pos = this.cssProperty(Property.StylePosition);
    return pos.equals("absolute") || pos.equals("fixed");
  }

  private String inheritedCssProperty(Property property) {
    if (this.node_ != null) {
      String s = this.cssProperty(property);
      if (s.length() != 0) {
        return s;
      }
    }
    if (this.parent_ != null) {
      return this.parent_.inheritedCssProperty(property);
    } else {
      return "";
    }
  }

  private double cssWidth(double fontScale) {
    double result = -1;
    if (this.node_ != null) {
      result =
          this.cssDecodeLength(
              this.cssProperty(Property.StyleWidth),
              fontScale,
              result,
              Block.PercentageRule.PercentageOfParentSize,
              this.getCurrentParentWidth());
      if (this.type_ == DomElementType.IMG
          || this.type_ == DomElementType.TABLE
          || this.type_ == DomElementType.TD
          || this.type_ == DomElementType.TH) {
        result =
            this.cssDecodeLength(
                this.attributeValue("width"),
                fontScale,
                result,
                Block.PercentageRule.PercentageOfParentSize,
                this.getCurrentParentWidth());
      }
    }
    return result;
  }

  private double cssHeight(double fontScale) {
    double result = -1;
    if (this.node_ != null) {
      result =
          this.cssDecodeLength(
              this.cssProperty(Property.StyleHeight),
              fontScale,
              result,
              Block.PercentageRule.IgnorePercentage);
      if (this.type_ == DomElementType.IMG) {
        result =
            this.cssDecodeLength(
                this.attributeValue("height"),
                fontScale,
                result,
                Block.PercentageRule.IgnorePercentage);
      }
    }
    return result;
  }

  private Block.CssLength cssLength(Property top, Side side, double fontScale) {
    Block.CssLength result = new Block.CssLength();
    if (!(this.node_ != null)) {
      result.defined = false;
      result.length = 0;
      return result;
    }
    int index = sideToIndex(side);
    Property property = Property.values()[top.getValue() + index];
    String value = this.cssProperty(property);
    if (value.length() != 0) {
      WLength l = new WLength(value);
      result.defined = true;
      result.length = l.toPixels(this.cssFontSize(fontScale));
      return result;
    } else {
      result.defined = false;
      result.length = 0;
      return result;
    }
  }

  private double cssMargin(Side side, double fontScale) {
    Block.CssLength result = new Block.CssLength();
    result.length = 0;
    if (this.type_ == DomElementType.TD) {
      return 0;
    }
    try {
      result = this.cssLength(Property.StyleMarginTop, side, fontScale);
    } catch (final RuntimeException e) {
    }
    if (!result.defined) {
      if (side == Side.Top || side == Side.Bottom) {
        if (this.type_ == DomElementType.H4
            || this.type_ == DomElementType.P
            || this.type_ == DomElementType.FIELDSET
            || this.type_ == DomElementType.FORM) {
          return 1.12 * this.cssFontSize(fontScale);
        } else {
          if (this.type_ == DomElementType.UL || this.type_ == DomElementType.OL) {
            if (!(this.isInside(DomElementType.UL) || this.isInside(DomElementType.OL))) {
              return 1.12 * this.cssFontSize(fontScale);
            } else {
              return 0;
            }
          } else {
            if (this.type_ == DomElementType.H1) {
              return 0.67 * this.cssFontSize(fontScale);
            } else {
              if (this.type_ == DomElementType.H2) {
                return 0.75 * this.cssFontSize(fontScale);
              } else {
                if (this.type_ == DomElementType.H3) {
                  return 0.83 * this.cssFontSize(fontScale);
                } else {
                  if (this.type_ == DomElementType.H5) {
                    return 1.5 * this.cssFontSize(fontScale);
                  } else {
                    if (this.type_ == DomElementType.H6) {
                      return 1.67 * this.cssFontSize(fontScale);
                    } else {
                      if (this.type_ == DomElementType.HR) {
                        return 0.5 * this.cssFontSize(fontScale);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return result.length;
  }

  private double cssPadding(Side side, double fontScale) {
    Block.CssLength result = this.cssLength(Property.StylePaddingTop, side, fontScale);
    if (!result.defined) {
      if (this.isTableCell()) {
        return 1;
      } else {
        if ((this.type_ == DomElementType.UL || this.type_ == DomElementType.OL)
            && side == Side.Left) {
          return 40;
        }
      }
    }
    return result.length;
  }

  private double cssBorderSpacing(double fontScale) {
    if (this.isTableCollapseBorders()) {
      return 0;
    }
    String spacingStr = this.cssProperty(Property.StyleBorderSpacing);
    if (spacingStr.length() != 0) {
      WLength l = new WLength(spacingStr);
      return l.toPixels(this.cssFontSize(fontScale));
    } else {
      return this.attributeValue("cellspacing", 2);
    }
  }

  private double cssBorderWidth(Side side, double fontScale) {
    if (this.isTableCell()) {
      Block t = this.getTable();
      if (t != null && t.isTableCollapseBorders()) {
        return this.collapsedBorderWidth(side, fontScale);
      } else {
        return this.rawCssBorderWidth(side, fontScale);
      }
    } else {
      if (this.isTable() && this.isTableCollapseBorders()) {
        return this.collapsedBorderWidth(side, fontScale);
      } else {
        return this.rawCssBorderWidth(side, fontScale);
      }
    }
  }

  private double collapsedBorderWidth(Side side, double fontScale) {
    assert this.isTable() || this.isTableCell();
    if (this.isTable()) {
      return 0;
    }
    Block.BorderElement be = this.collapseCellBorders(side);
    return be.block.rawCssBorderWidth(be.side, fontScale);
  }

  private double rawCssBorderWidth(Side side, double fontScale, boolean indicateHidden) {
    if (!(this.node_ != null)) {
      return 0;
    }
    int index = sideToIndex(side);
    Property property = Property.values()[Property.StyleBorderTop.getValue() + index];
    String borderStr = this.cssProperty(property);
    String borderWidthStr = "";
    if (borderStr.length() != 0) {
      List<String> values = new ArrayList<String>();
      StringUtils.split(values, borderStr, " ", false);
      if (values.size() > 1 && values.get(1).equals("hidden")) {
        if (indicateHidden) {
          return -1;
        } else {
          return 0;
        }
      }
      borderWidthStr = values.get(0);
    }
    if (borderWidthStr.length() == 0) {
      property = Property.values()[Property.StyleBorderWidthTop.getValue() + index];
      borderWidthStr = this.cssProperty(property);
    }
    double result = 0;
    if (borderWidthStr.length() != 0) {
      WLength l = new WLength(borderWidthStr);
      result = l.toPixels(this.cssFontSize(fontScale));
    }
    if (result == 0) {
      if (this.isTable()) {
        result = this.attributeValue("border", 0) != 0 ? 1 : 0;
      } else {
        if (this.isTableCell()) {
          Block t = this.getTable();
          if (t != null && !t.isTableCollapseBorders()) {
            result = t.attributeValue("border", 0) != 0 ? 1 : 0;
          }
        } else {
          if (this.type_ == DomElementType.HR) {
            result = 1;
          }
        }
      }
    }
    return result;
  }

  private final double rawCssBorderWidth(Side side, double fontScale) {
    return rawCssBorderWidth(side, fontScale, false);
  }

  private WColor cssBorderColor(Side side) {
    if (this.isTableCell()) {
      Block t = this.getTable();
      if (t != null && t.isTableCollapseBorders()) {
        return this.collapsedBorderColor(side);
      } else {
        return this.rawCssBorderColor(side);
      }
    } else {
      if (this.isTable() && this.isTableCollapseBorders()) {
        return this.collapsedBorderColor(side);
      } else {
        return this.rawCssBorderColor(side);
      }
    }
  }

  private WColor collapsedBorderColor(Side side) {
    assert this.isTable() || this.isTableCell();
    if (this.isTable()) {
      return new WColor();
    }
    Block.BorderElement be = this.collapseCellBorders(side);
    return be.block.rawCssBorderColor(be.side);
  }

  private WColor rawCssBorderColor(Side side) {
    int index = sideToIndex(side);
    Property property = Property.values()[Property.StyleBorderTop.getValue() + index];
    String borderStr = this.cssProperty(property);
    String borderColorStr = "";
    if (borderStr.length() != 0) {
      List<String> values = new ArrayList<String>();
      StringUtils.split(values, borderStr, " ", false);
      if (values.size() > 2) {
        borderColorStr = values.get(2);
      }
    }
    if (borderColorStr.length() == 0) {
      property = Property.values()[Property.StyleBorderColorTop.getValue() + index];
      borderColorStr = this.cssProperty(property);
    }
    if (borderColorStr.length() != 0) {
      return new WColor(new WString(borderColorStr));
    }
    return new WColor(StandardColor.Black);
  }

  private WColor getCssColor() {
    String color = this.inheritedCssProperty(Property.StyleColor);
    if (color.length() != 0) {
      return new WColor(new WString(color));
    } else {
      return new WColor(StandardColor.Black);
    }
  }

  private AlignmentFlag getCssTextAlign() {
    if (this.node_ != null && !this.isInline()) {
      String s = this.cssProperty(Property.StyleTextAlign);
      if (s.length() == 0 && !this.isTable()) {
        s = this.attributeValue("align");
      }
      if (s.length() == 0 || s.equals("inherit")) {
        if (this.type_ == DomElementType.TH) {
          return AlignmentFlag.Center;
        } else {
          if (this.parent_ != null) {
            return this.parent_.getCssTextAlign();
          } else {
            return AlignmentFlag.Left;
          }
        }
      } else {
        if (s.equals("left")) {
          return AlignmentFlag.Left;
        } else {
          if (s.equals("center")) {
            return AlignmentFlag.Center;
          } else {
            if (s.equals("right")) {
              return AlignmentFlag.Right;
            } else {
              if (s.equals("justify")) {
                return AlignmentFlag.Justify;
              } else {
                unsupportedCssValue(Property.StyleTextAlign, s);
                return AlignmentFlag.Left;
              }
            }
          }
        }
      }
    } else {
      if (this.parent_ != null) {
        return this.parent_.getCssTextAlign();
      } else {
        return AlignmentFlag.Left;
      }
    }
  }

  private double cssBoxMargin(Side side, double fontScale) {
    return this.cssPadding(side, fontScale)
        + this.cssMargin(side, fontScale)
        + this.cssBorderWidth(side, fontScale);
  }

  private double cssLineHeight(double fontLineHeight, double fontScale) {
    if (!(this.node_ != null) && this.parent_ != null) {
      return this.parent_.cssLineHeight(fontLineHeight, fontScale);
    }
    String v = this.cssProperty(Property.StyleLineHeight);
    if (v.length() != 0) {
      if (v.equals("normal")) {
        return fontLineHeight;
      } else {
        try {
          return Double.parseDouble(v);
        } catch (final RuntimeException e) {
          WLength l = new WLength(v);
          if (l.getUnit() == LengthUnit.Percentage) {
            return this.cssFontSize(fontScale) * l.getValue() / 100;
          } else {
            return l.toPixels(this.parent_.cssFontSize(fontScale));
          }
        }
      }
    } else {
      if (this.parent_ != null) {
        return this.parent_.cssLineHeight(fontLineHeight, fontScale);
      } else {
        return fontLineHeight;
      }
    }
  }

  private double cssFontSize(double fontScale) {
    if (!(this.node_ != null) && this.parent_ != null) {
      return fontScale * this.parent_.cssFontSize();
    }
    String v = this.cssProperty(Property.StyleFontSize);
    double Medium = 16;
    double parentSize = this.parent_ != null ? this.parent_.cssFontSize() : Medium;
    double result;
    if (v.length() != 0) {
      if (v.equals("xx-small")) {
        result = Medium / 1.2 / 1.2 / 1.2;
      } else {
        if (v.equals("x-small")) {
          result = Medium / 1.2 / 1.2;
        } else {
          if (v.equals("small")) {
            result = Medium / 1.2;
          } else {
            if (v.equals("medium")) {
              result = Medium;
            } else {
              if (v.equals("large")) {
                result = Medium * 1.2;
              } else {
                if (v.equals("x-large")) {
                  result = Medium * 1.2 * 1.2;
                } else {
                  if (v.equals("xx-large")) {
                    result = Medium * 1.2 * 1.2 * 1.2;
                  } else {
                    if (v.equals("larger")) {
                      result = parentSize * 1.2;
                    } else {
                      if (v.equals("smaller")) {
                        result = parentSize / 1.2;
                      } else {
                        WLength l = new WLength(v);
                        if (l.getUnit() == LengthUnit.Percentage) {
                          result = parentSize * l.getValue() / 100;
                        } else {
                          if (l.getUnit() == LengthUnit.FontEm) {
                            result = parentSize * l.getValue();
                          } else {
                            result = l.toPixels();
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    } else {
      if (this.type_ == DomElementType.H1) {
        result = parentSize * 2;
      } else {
        if (this.type_ == DomElementType.H2) {
          result = parentSize * 1.5;
        } else {
          if (this.type_ == DomElementType.H3) {
            result = parentSize * 1.17;
          } else {
            if (this.type_ == DomElementType.H5) {
              result = parentSize * 0.83;
            } else {
              if (this.type_ == DomElementType.H6) {
                result = parentSize * 0.75;
              } else {
                result = parentSize;
              }
            }
          }
        }
      }
    }
    return result * fontScale;
  }

  private final double cssFontSize() {
    return cssFontSize(1);
  }
  // private String getCssPosition() ;
  private FontStyle getCssFontStyle() {
    if (!(this.node_ != null) && this.parent_ != null) {
      return this.parent_.getCssFontStyle();
    }
    String v = this.cssProperty(Property.StyleFontStyle);
    if (v.length() == 0 && (this.type_ == DomElementType.EM || this.type_ == DomElementType.I)) {
      return FontStyle.Italic;
    } else {
      if (v.equals("normal")) {
        return FontStyle.Normal;
      } else {
        if (v.equals("italic")) {
          return FontStyle.Italic;
        } else {
          if (v.equals("oblique")) {
            return FontStyle.Oblique;
          } else {
            if (this.parent_ != null) {
              return this.parent_.getCssFontStyle();
            } else {
              return FontStyle.Normal;
            }
          }
        }
      }
    }
  }

  private int getCssFontWeight() {
    if (!(this.node_ != null) && this.parent_ != null) {
      return this.parent_.getCssFontWeight();
    }
    String v = this.cssProperty(Property.StyleFontWeight);
    if (v.length() == 0
        && (this.type_ == DomElementType.B
            || this.type_ == DomElementType.STRONG
            || this.type_ == DomElementType.TH
            || this.type_.getValue() >= DomElementType.H1.getValue()
                && this.type_.getValue() <= DomElementType.H6.getValue())) {
      v = "bolder";
    }
    if (v.length() != 0 && !v.equals("bolder") && !v.equals("lighter")) {
      if (v.equals("normal")) {
        return 400;
      } else {
        if (v.equals("bold")) {
          return 700;
        } else {
          try {
            return Integer.parseInt(v);
          } catch (final RuntimeException blc) {
          }
        }
      }
    }
    int parentWeight = this.parent_ != null ? this.parent_.getCssFontWeight() : 400;
    if (v.equals("bolder")) {
      if (parentWeight < 300) {
        return 400;
      } else {
        if (parentWeight < 600) {
          return 700;
        } else {
          return 900;
        }
      }
    } else {
      if (v.equals("lighter")) {
        if (parentWeight < 600) {
          return 100;
        } else {
          if (parentWeight < 800) {
            return 400;
          } else {
            return 700;
          }
        }
      } else {
        return parentWeight;
      }
    }
  }

  private WFont cssFont(double fontScale) {
    if (this.font_.getGenericFamily() != FontFamily.Default) {
      return this.font_;
    }
    FontFamily genericFamily = FontFamily.SansSerif;
    WString specificFamilies = new WString();
    String family = this.inheritedCssProperty(Property.StyleFontFamily);
    if (family.length() != 0) {
      List<String> values = new ArrayList<String>();
      StringUtils.split(values, family, ",", false);
      for (int i = 0; i < values.size(); ++i) {
        String name = values.get(i);
        name = name.trim();
        name = Utils.strip(name, "'\"");
        name = name.toLowerCase();
        if (name.equals("sans-serif")) {
          genericFamily = FontFamily.SansSerif;
        } else {
          if (name.equals("serif")) {
            genericFamily = FontFamily.Serif;
          } else {
            if (name.equals("cursive")) {
              genericFamily = FontFamily.Cursive;
            } else {
              if (name.equals("fantasy")) {
                genericFamily = FontFamily.Fantasy;
              } else {
                if (name.equals("monospace")) {
                  genericFamily = FontFamily.Monospace;
                } else {
                  if (name.equals("times") || name.equals("palatino")) {
                    genericFamily = FontFamily.Serif;
                  } else {
                    if (name.equals("arial") || name.equals("helvetica")) {
                      genericFamily = FontFamily.SansSerif;
                    } else {
                      if (name.equals("courier")) {
                        genericFamily = FontFamily.Monospace;
                      } else {
                        if (name.equals("symbol")) {
                          genericFamily = FontFamily.Fantasy;
                        } else {
                          if (name.equals("zapf dingbats")) {
                            genericFamily = FontFamily.Cursive;
                          }
                        }
                      }
                    }
                  }
                  if (!(specificFamilies.length() == 0)) {
                    specificFamilies.append(", ");
                  }
                  specificFamilies.append(name);
                }
              }
            }
          }
        }
      }
    }
    this.font_.setFamily(genericFamily, specificFamilies);
    this.font_.setSize(new WLength(this.cssFontSize(fontScale), LengthUnit.Pixel));
    this.font_.setWeight(FontWeight.Value, this.getCssFontWeight());
    this.font_.setStyle(this.getCssFontStyle());
    return this.font_;
  }

  private String getCssTextDecoration() {
    String v = this.cssProperty(Property.StyleTextDecoration);
    if (v.length() == 0 || v.equals("inherit")) {
      if (this.parent_ != null) {
        return this.parent_.getCssTextDecoration();
      } else {
        return "";
      }
    } else {
      return v;
    }
  }

  private double cssDecodeLength(
      final String length,
      double fontScale,
      double defaultValue,
      Block.PercentageRule percentage,
      double parentSize) {
    if (length.length() != 0) {
      WLength l = new WLength(length);
      if (l.getUnit() == LengthUnit.Percentage) {
        switch (percentage) {
          case PercentageOfFontSize:
            return l.toPixels(this.cssFontSize(fontScale));
          case PercentageOfParentSize:
            return l.getValue() / 100.0 * parentSize;
          case IgnorePercentage:
            return defaultValue;
        }
        return defaultValue;
      } else {
        return l.toPixels(this.cssFontSize(fontScale));
      }
    } else {
      return defaultValue;
    }
  }

  private final double cssDecodeLength(final String length, double fontScale, double defaultValue) {
    return cssDecodeLength(
        length, fontScale, defaultValue, Block.PercentageRule.PercentageOfFontSize, 0);
  }

  private final double cssDecodeLength(
      final String length, double fontScale, double defaultValue, Block.PercentageRule percentage) {
    return cssDecodeLength(length, fontScale, defaultValue, percentage, 0);
  }

  private static boolean isPercentageLength(final String length) {
    return length.length() != 0 && new WLength(length).getUnit() == LengthUnit.Percentage;
  }

  private double getCurrentParentWidth() {
    if (this.parent_ != null) {
      switch (this.parent_.type_) {
        case TR:
        case TBODY:
        case THEAD:
        case TFOOT:
          return this.parent_.getCurrentParentWidth();
        default:
          return this.parent_.currentWidth_;
      }
    } else {
      return 0;
    }
  }

  private boolean isInside(DomElementType type) {
    if (this.parent_ != null) {
      if (this.parent_.type_ == type) {
        return true;
      } else {
        return this.parent_.isInside(type);
      }
    } else {
      return false;
    }
  }

  private void pageBreak(final PageState ps) {
    clearFloats(ps);
    ++ps.page;
    ps.y = 0;
  }

  private void inlinePageBreak(
      final String pageBreak,
      final Line line,
      final List<Block> floats,
      double minX,
      double maxX,
      final WTextRenderer renderer) {
    if (pageBreak.equals("always")) {
      if (this.inlineLayout.isEmpty()) {
        this.inlineLayout.add(new InlineBox());
        final InlineBox b = this.inlineLayout.get(this.inlineLayout.size() - 1);
        b.page = line.getPage();
        b.x = line.getX();
        b.width = 1;
        b.y = line.getY();
        b.height = 1;
        b.baseline = 0;
        b.utf8Count = 0;
        b.utf8Pos = 0;
        b.whitespaceWidth = 0;
        line.adjustHeight(1, 0, 0);
        line.setX(line.getX() + b.width);
        line.addBlock(this);
      }
      line.newLine(minX, line.getY() + line.getHeight(), line.getPage());
      line.moveToNextPage(floats, minX, maxX, renderer);
    }
  }

  private double layoutInline(
      final Line line,
      final List<Block> floats,
      double minX,
      double maxX,
      boolean canIncreaseWidth,
      final WTextRenderer renderer) {
    this.inlineLayout.clear();
    this.inlinePageBreak(
        this.cssProperty(Property.StylePageBreakBefore), line, floats, minX, maxX, renderer);
    if (this.isText() || this.type_ == DomElementType.IMG || this.type_ == DomElementType.BR) {
      String s = "";
      int utf8Pos = 0;
      int utf8Count = 0;
      double whitespaceWidth = 0;
      renderer.getPainter().setFont(this.cssFont(renderer.getFontScale()));
      WPaintDevice device = renderer.getPainter().getDevice();
      WFontMetrics metrics = device.getFontMetrics();
      double lineHeight = this.cssLineHeight(metrics.getHeight(), renderer.getFontScale());
      double fontHeight = metrics.getSize();
      double baseline = (lineHeight - fontHeight) / 2.0 + metrics.getAscent();
      if (this.isText()) {
        s = this.getText();
        whitespaceWidth = device.measureText(new WString(" ")).getWidth();
      }
      for (; ; ) {
        Range rangeX = new Range(minX, maxX);
        adjustAvailableWidth(line.getY(), line.getPage(), floats, rangeX);
        if (rangeX.start > line.getX()) {
          line.setX(rangeX.start);
        }
        double w = 0;
        double h = 0;
        boolean lineBreak = false;
        if (this.isText()) {
          if (utf8Pos < s.length()
              && line.getX() == rangeX.start
              && isWhitespace(s.charAt(utf8Pos))) {
            ++utf8Pos;
          }
          if (utf8Pos < s.length()) {
            double maxWidth;
            if (canIncreaseWidth) {
              maxWidth = Double.MAX_VALUE;
            } else {
              maxWidth = rangeX.end - line.getX();
            }
            WString text = new WString(s.substring(utf8Pos));
            WTextItem item = renderer.getPainter().getDevice().measureText(text, maxWidth, true);
            utf8Count = item.getText().toString().length();
            w = item.getWidth();
            if (utf8Count > 0
                && utf8Pos + utf8Count < s.length()
                && isWhitespace(s.charAt(utf8Pos + utf8Count - 1))) {
              w += whitespaceWidth;
              rangeX.end += whitespaceWidth;
            }
            if (canIncreaseWidth && isEpsilonMore(item.getWidth(), rangeX.end - line.getX())) {
              maxX += w - (rangeX.end - line.getX());
              rangeX.end += w - (rangeX.end - line.getX());
            }
            if (w == 0) {
              lineBreak = true;
              if (line.getX() == rangeX.start) {
                if (item.getNextWidth() < 0) {
                  for (int i = utf8Pos; i <= s.length(); ++i) {
                    if (i == s.length() || isWhitespace(s.charAt(i))) {
                      WString word = new WString(s.substring(utf8Pos, utf8Pos + i - utf8Pos));
                      double wordWidth = device.measureText(word).getWidth();
                      w = wordWidth;
                      break;
                    }
                  }
                } else {
                  w = item.getNextWidth();
                }
              }
            } else {
              if (utf8Count <= 0) {
                throw new WException("Internal error: utf8Count <= 0!");
              }
              h = fontHeight;
            }
          } else {
            break;
          }
        } else {
          if (this.type_ == DomElementType.BR) {
            if (this.inlineLayout.isEmpty()) {
              this.inlineLayout.add(new InlineBox());
              lineBreak = true;
              line.adjustHeight(fontHeight, baseline, lineHeight);
            } else {
              this.inlineLayout.clear();
              break;
            }
          } else {
            w = this.cssWidth(renderer.getFontScale());
            h = this.cssHeight(renderer.getFontScale());
            String src = this.attributeValue("src");
            if (w <= 0 || h <= 0) {
              WDataInfo imgInfo = new WDataInfo();
              if (DataUri.isDataUri(src)) {
                imgInfo.setDataUri(src);
              } else {
                imgInfo.setUrl(src);
                imgInfo.setFilePath(src);
              }
              WPainter.Image image = new WPainter.Image(imgInfo);
              if (w <= 0) {
                w = image.getWidth();
              }
              if (h <= 0) {
                h = image.getHeight();
              }
            }
            w +=
                this.cssBoxMargin(Side.Left, renderer.getFontScale())
                    + this.cssBoxMargin(Side.Right, renderer.getFontScale());
            h +=
                this.cssBoxMargin(Side.Top, renderer.getFontScale())
                    + this.cssBoxMargin(Side.Bottom, renderer.getFontScale());
            String va = this.cssProperty(Property.StyleVerticalAlign);
            if (va.equals("middle")) {
              baseline = h / 2 + fontHeight / 2;
            } else {
              if (va.equals("text-top")) {
              } else {
                baseline = h;
              }
            }
          }
        }
        if (lineBreak || isEpsilonMore(w, rangeX.end - line.getX())) {
          line.setLineBreak(this.type_ == DomElementType.BR);
          line.finish(this.getCssTextAlign(), floats, minX, maxX, renderer);
          if (w == 0 || line.getX() > rangeX.start) {
            if (w > 0 && canIncreaseWidth) {
              maxX += w - (maxX - line.getX());
              rangeX.end += w - (maxX - line.getX());
            }
            line.newLine(minX, line.getY() + line.getHeight(), line.getPage());
            h = 0;
          } else {
            if (isEpsilonMore(w, maxX - minX)) {
              maxX += w - (maxX - minX);
              rangeX.end += w - (maxX - minX);
            } else {
              PageState linePs = new PageState();
              linePs.y = line.getY();
              linePs.page = line.getPage();
              linePs.minX = minX;
              linePs.maxX = maxX;
              Utils.copyList(floats, linePs.floats);
              clearFloats(linePs, w);
              line.newLine(linePs.minX, linePs.y, linePs.page);
            }
          }
          h = 0;
        }
        if (w > 0 && h > 0) {
          this.inlineLayout.add(new InlineBox());
          final InlineBox b = this.inlineLayout.get(this.inlineLayout.size() - 1);
          double marginLeft = 0;
          double marginRight = 0;
          double marginBottom = 0;
          double marginTop = 0;
          if (this.type_ == DomElementType.IMG) {
            marginLeft = this.cssMargin(Side.Left, renderer.getFontScale());
            marginRight = this.cssMargin(Side.Right, renderer.getFontScale());
            marginBottom = this.cssMargin(Side.Bottom, renderer.getFontScale());
            marginTop = this.cssMargin(Side.Top, renderer.getFontScale());
          }
          b.page = line.getPage();
          b.x = line.getX() + marginLeft;
          b.width = w - marginLeft - marginRight;
          b.y = line.getY() + marginTop;
          b.height = h - marginTop - marginBottom;
          b.baseline = baseline - marginTop - marginBottom;
          b.utf8Count = utf8Count;
          b.utf8Pos = utf8Pos;
          b.whitespaceWidth = whitespaceWidth;
          utf8Pos += utf8Count;
          line.adjustHeight(h, baseline, lineHeight);
          line.setX(line.getX() + w);
          line.addBlock(this);
          if (line.getBottom() >= renderer.textHeight(line.getPage())) {
            line.moveToNextPage(floats, minX, maxX, renderer);
          }
          if (!this.isText() || this.isText() && utf8Pos == s.length()) {
            break;
          }
        }
      }
    }
    if (this.isInlineChildren()) {
      if (this.type_ == DomElementType.LI) {
        this.inlineLayout.get(0).x = MARGINX;
        line.setX(minX);
      }
      if (!this.children_.isEmpty()) {
        for (int i = 0; i < this.children_.size(); ++i) {
          Block c = this.children_.get(i);
          if (c.isFloat()) {
            maxX =
                c.layoutFloat(
                    line.getY(),
                    line.getPage(),
                    floats,
                    line.getX(),
                    line.getHeight(),
                    minX,
                    maxX,
                    canIncreaseWidth,
                    renderer);
            line.reflow(c);
            line.addBlock(c);
          } else {
            if (c.isPositionedAbsolutely()) {
              if (!(c.offsetParent_ != null)) {
                c.setOffsetParent();
              }
              c.inlineLayout.clear();
              c.inlineLayout.add(new InlineBox());
              final InlineBox box = c.inlineLayout.get(c.inlineLayout.size() - 1);
              box.page = line.getPage();
              box.x = line.getX();
              box.y = line.getY();
              box.width = 0;
              box.height = 0;
            } else {
              maxX = c.layoutInline(line, floats, minX, maxX, canIncreaseWidth, renderer);
            }
          }
        }
      }
    }
    if (this.isInline()) {
      for (int i = 0; i < this.offsetChildren_.size(); ++i) {
        this.offsetChildren_.get(i).layoutAbsolute(renderer);
      }
    }
    this.inlinePageBreak(
        this.cssProperty(Property.StylePageBreakAfter), line, floats, minX, maxX, renderer);
    return maxX;
  }

  private void layoutTable(
      final PageState ps,
      boolean canIncreaseWidth,
      final WTextRenderer renderer,
      double cssSetWidth) {
    this.currentWidth_ = Math.max(0.0, cssSetWidth);
    List<Double> minimumColumnWidths = new ArrayList<Double>();
    List<Double> maximumColumnWidths = new ArrayList<Double>();
    List<Double> setColumnWidths = new ArrayList<Double>();
    for (int i = 0; i < this.children_.size(); ++i) {
      Block c = this.children_.get(i);
      c.tableComputeColumnWidths(
          minimumColumnWidths, maximumColumnWidths, setColumnWidths, renderer, this);
    }
    this.currentWidth_ = 0;
    int colCount = minimumColumnWidths.size();
    for (int i = 0; i < colCount; ++i) {
      if (setColumnWidths.get(i) >= 0) {
        setColumnWidths.set(i, Math.max(setColumnWidths.get(i), minimumColumnWidths.get(i)));
        maximumColumnWidths.set(i, minimumColumnWidths.set(i, setColumnWidths.get(i)));
      }
    }
    double cellSpacing = this.cssBorderSpacing(renderer.getFontScale());
    double totalSpacing = (colCount + 1) * cellSpacing;
    double totalMinWidth = sum(minimumColumnWidths) + totalSpacing;
    double totalMaxWidth = sum(maximumColumnWidths) + totalSpacing;
    double desiredMinWidth = Math.max(totalMinWidth, cssSetWidth);
    double desiredMaxWidth = totalMaxWidth;
    if (cssSetWidth > 0 && cssSetWidth < desiredMaxWidth) {
      desiredMaxWidth = Math.max(desiredMinWidth, cssSetWidth);
    }
    double availableWidth;
    for (; ; ) {
      Range rangeX = new Range(ps.minX, ps.maxX);
      adjustAvailableWidth(ps.y, ps.page, ps.floats, rangeX);
      ps.maxX = rangeX.end;
      double border =
          this.cssBorderWidth(Side.Left, renderer.getFontScale())
              + this.cssBorderWidth(Side.Right, renderer.getFontScale());
      availableWidth = rangeX.end - rangeX.start - border;
      if (canIncreaseWidth && isEpsilonLess(availableWidth, desiredMaxWidth)) {
        ps.maxX += desiredMaxWidth - availableWidth;
        availableWidth = desiredMaxWidth;
      }
      if (!isEpsilonLess(availableWidth, desiredMinWidth)) {
        break;
      } else {
        if (isEpsilonLess(desiredMinWidth, ps.maxX - ps.minX - border)) {
          clearFloats(ps, desiredMinWidth);
        } else {
          ps.maxX += desiredMinWidth - availableWidth;
          availableWidth = desiredMinWidth;
        }
      }
    }
    double width = desiredMinWidth;
    if (width <= availableWidth) {
      if (desiredMaxWidth > availableWidth) {
        width = availableWidth;
      } else {
        width = Math.max(desiredMaxWidth, width);
      }
    } else {
      Utils.copyList(minimumColumnWidths, maximumColumnWidths);
    }
    List<Double> widths = minimumColumnWidths;
    if (width > totalMaxWidth) {
      Utils.copyList(maximumColumnWidths, widths);
      double rWidth = width - totalSpacing;
      double rTotalMaxWidth = totalMaxWidth - totalSpacing;
      for (int i = 0; i < colCount; ++i) {
        if (setColumnWidths.get(i) >= 0) {
          rWidth -= widths.get(i);
          rTotalMaxWidth -= widths.get(i);
        }
      }
      if (rTotalMaxWidth <= 0) {
        rWidth = width - totalSpacing;
        rTotalMaxWidth = totalMaxWidth - totalSpacing;
        for (int i = 0; i < widths.size(); ++i) {
          setColumnWidths.set(i, -1.0);
        }
      }
      if (rTotalMaxWidth > 0) {
        double factor = rWidth / rTotalMaxWidth;
        for (int i = 0; i < widths.size(); ++i) {
          if (setColumnWidths.get(i) < 0) {
            widths.set(i, widths.get(i) * factor);
          }
        }
      } else {
        double widthPerColumn = rWidth / colCount;
        for (int i = 0; i < colCount; ++i) {
          widths.set(i, widthPerColumn);
        }
      }
    } else {
      if (width > totalMinWidth) {
        double totalStretch = 0;
        for (int i = 0; i < widths.size(); ++i) {
          totalStretch += maximumColumnWidths.get(i) - minimumColumnWidths.get(i);
        }
        double room = width - totalMinWidth;
        double factor = room / totalStretch;
        for (int i = 0; i < widths.size(); ++i) {
          double stretch = maximumColumnWidths.get(i) - minimumColumnWidths.get(i);
          widths.set(i, widths.get(i) + factor * stretch);
        }
      }
    }
    AlignmentFlag hAlign = this.getHorizontalAlignment();
    switch (hAlign) {
      case Left:
      case Justify:
        ps.maxX = ps.minX + width;
        break;
      case Center:
        ps.minX = ps.minX + (ps.maxX - ps.minX - width) / 2;
        ps.maxX = ps.minX + width;
        break;
      case Right:
        ps.minX = ps.maxX - width;
        break;
      default:
        break;
    }
    Block repeatHead = null;
    for (int i = 0; i < this.children_.size(); ++i) {
      if (this.children_.get(i).type_ == DomElementType.THEAD) {
        repeatHead = this.children_.get(i);
        break;
      } else {
        if (this.children_.get(i).type_ == DomElementType.TBODY
            || this.children_.get(i).type_ == DomElementType.TR) {
          break;
        }
      }
    }
    boolean protectRows = repeatHead != null;
    List<CellState> rowSpanBackLog = new ArrayList<CellState>();
    this.tableDoLayout(
        ps.minX, ps, cellSpacing, widths, rowSpanBackLog, protectRows, repeatHead, renderer);
    ps.minX -= this.cssBorderWidth(Side.Left, renderer.getFontScale());
    ps.maxX += this.cssBorderWidth(Side.Right, renderer.getFontScale());
    ps.y += cellSpacing;
  }

  double layoutFloat(
      double y,
      int page,
      final List<Block> floats,
      double lineX,
      double lineHeight,
      double minX,
      double maxX,
      boolean canIncreaseWidth,
      final WTextRenderer renderer) {
    if (floats.indexOf(this) != -1) {
      return maxX;
    }
    double blockCssWidth = this.cssWidth(renderer.getFontScale());
    double currentWidth =
        Math.max(0.0, blockCssWidth)
            + this.cssBoxMargin(Side.Left, renderer.getFontScale())
            + this.cssBoxMargin(Side.Right, renderer.getFontScale());
    PageState floatPs = new PageState();
    Utils.copyList(floats, floatPs.floats);
    for (; ; ) {
      floatPs.page = page;
      floatPs.y = y;
      floatPs.minX = minX;
      floatPs.maxX = maxX;
      double floatX =
          positionFloat(
              lineX,
              floatPs,
              lineHeight,
              currentWidth,
              canIncreaseWidth,
              renderer,
              this.getFloatSide());
      if (floatPs.maxX > maxX) {
        return floatPs.maxX;
      }
      List<Block> innerFloats = new ArrayList<Block>();
      boolean unknownWidth = blockCssWidth < 0 && isEpsilonLess(currentWidth, maxX - minX);
      double collapseMarginBottom = 0;
      floatPs.minX = floatX;
      floatPs.maxX = floatX + currentWidth;
      collapseMarginBottom =
          this.layoutBlock(
              floatPs, unknownWidth || canIncreaseWidth, renderer, 0, collapseMarginBottom);
      double pw = floatPs.maxX - (floatPs.minX + currentWidth);
      if (pw > 0) {
        if (blockCssWidth < 0) {
          currentWidth = Math.min(maxX - minX, currentWidth + pw);
          continue;
        } else {
          if (!canIncreaseWidth) {
            throw new WException("Internal error: !canIncreaseWidth");
          }
          return maxX + pw;
        }
      }
      floats.add(this);
      return maxX;
    }
  }

  private void layoutAbsolute(final WTextRenderer renderer) {
    LayoutBox staticLayout = this.getLayoutTotal();
    LayoutBox containingLayout = this.offsetParent_.getLayoutTotal();
    boolean leftAuto = isOffsetAuto(this.cssProperty(Property.StyleLeft));
    boolean widthAuto = isOffsetAuto(this.cssProperty(Property.StyleWidth));
    boolean rightAuto = isOffsetAuto(this.cssProperty(Property.StyleRight));
    double staticLeft = staticLayout.x - containingLayout.x;
    PageState ps = new PageState();
    this.layoutBlock(ps, false, renderer, 0, 0);
    double preferredMinWidth = ps.maxX;
    ps = new PageState();
    this.layoutBlock(ps, true, renderer, 0, 0);
    double preferredWidth = ps.maxX;
    double availableWidth = containingLayout.width;
    double shrinkToFitWidth = Math.min(Math.max(preferredMinWidth, availableWidth), preferredWidth);
    double left = 0;
    double width = 0;
    double right = 0;
    if (!leftAuto) {
      left =
          this.cssDecodeLength(
              this.cssProperty(Property.StyleLeft),
              renderer.getFontScale(),
              0,
              Block.PercentageRule.PercentageOfParentSize,
              containingLayout.width);
    }
    if (!rightAuto) {
      right =
          this.cssDecodeLength(
              this.cssProperty(Property.StyleRight),
              renderer.getFontScale(),
              0,
              Block.PercentageRule.PercentageOfParentSize,
              containingLayout.width);
    }
    if (!widthAuto) {
      width = this.cssWidth(renderer.getFontScale());
    }
    if (leftAuto && widthAuto && rightAuto) {
      left = staticLeft;
      width = shrinkToFitWidth;
    } else {
      if (!leftAuto && !widthAuto && !rightAuto) {
      } else {
        if (leftAuto && widthAuto && !rightAuto) {
          width = shrinkToFitWidth;
          left = containingLayout.width - right - width;
        } else {
          if (leftAuto && !widthAuto && rightAuto) {
          } else {
            if (!leftAuto && widthAuto && rightAuto) {
              width = shrinkToFitWidth;
            } else {
              if (leftAuto && !widthAuto && !rightAuto) {
                left = containingLayout.width - right - width;
              } else {
                if (!leftAuto && widthAuto && !rightAuto) {
                  width = Math.max(0.0, containingLayout.width - left - right);
                } else {
                  if (!leftAuto && !widthAuto && rightAuto) {}
                }
              }
            }
          }
        }
      }
    }
    double staticTop = staticLayout.y - containingLayout.y;
    staticTop +=
        (staticLayout.page - containingLayout.page) * renderer.textHeight(containingLayout.page);
    boolean topAuto = isOffsetAuto(this.cssProperty(Property.StyleTop));
    boolean heightAuto = isOffsetAuto(this.cssProperty(Property.StyleHeight));
    boolean bottomAuto = isOffsetAuto(this.cssProperty(Property.StyleBottom));
    double top = 0;
    double height = 0;
    double bottom = 0;
    if (!topAuto) {
      top =
          this.cssDecodeLength(
              this.cssProperty(Property.StyleTop),
              renderer.getFontScale(),
              0,
              Block.PercentageRule.PercentageOfParentSize,
              containingLayout.height);
    }
    if (!bottomAuto) {
      right =
          this.cssDecodeLength(
              this.cssProperty(Property.StyleBottom),
              renderer.getFontScale(),
              0,
              Block.PercentageRule.PercentageOfParentSize,
              containingLayout.height);
    }
    if (!heightAuto) {
      height = this.cssWidth(renderer.getFontScale());
    }
    ps = new PageState();
    ps.minX = containingLayout.x + left;
    ps.maxX = containingLayout.x + left + width;
    this.layoutBlock(ps, false, renderer, 0, 0);
    double contentHeight = this.getLayoutTotal().height;
    if (topAuto && heightAuto && bottomAuto) {
      top = staticTop;
      height = contentHeight;
    } else {
      if (!topAuto && !heightAuto && !bottomAuto) {
      } else {
        if (topAuto && heightAuto && !bottomAuto) {
          height = contentHeight;
          top = containingLayout.height - bottom - height;
        } else {
          if (topAuto && !heightAuto && bottomAuto) {
            top = staticTop;
          } else {
            if (!topAuto && heightAuto && bottomAuto) {
              height = contentHeight;
            } else {
              if (topAuto && !heightAuto && !bottomAuto) {
                top = containingLayout.height - bottom - height;
              } else {
                if (!topAuto && heightAuto && !bottomAuto) {
                  height = containingLayout.height - top - bottom;
                } else {
                  if (!topAuto && !heightAuto && bottomAuto) {}
                }
              }
            }
          }
        }
      }
    }
    ps = new PageState();
    ps.y = containingLayout.y + top;
    ps.page = containingLayout.page;
    while (ps.y > renderer.pageHeight(ps.page)) {
      ++ps.page;
      ps.y -= renderer.pageHeight(ps.page);
    }
    ps.minX = containingLayout.x + left;
    ps.maxX = containingLayout.x + left + width;
    this.layoutBlock(ps, false, renderer, 0, 0);
  }

  private void tableDoLayout(
      double x,
      final PageState ps,
      double cellSpacing,
      final List<Double> widths,
      final List<CellState> rowSpanBackLog,
      boolean protectRows,
      Block repeatHead,
      final WTextRenderer renderer) {
    if (this.type_ == DomElementType.TABLE
        || this.type_ == DomElementType.TBODY
        || this.type_ == DomElementType.THEAD
        || this.type_ == DomElementType.TFOOT) {
      for (int i = 0; i < this.children_.size(); ++i) {
        Block c = this.children_.get(i);
        c.tableDoLayout(
            x,
            ps,
            cellSpacing,
            widths,
            rowSpanBackLog,
            protectRows,
            this.type_ != DomElementType.THEAD ? repeatHead : null,
            renderer);
      }
      if (repeatHead != null && this.type_ == DomElementType.THEAD) {
        this.blockLayout.clear();
        BlockBox bb = new BlockBox();
        bb.page = ps.page;
        bb.y = this.minChildrenLayoutY(ps.page);
        bb.height = this.childrenLayoutHeight(ps.page);
        bb.x = x;
        bb.width = 0;
        this.blockLayout.add(bb);
      }
    } else {
      if (this.type_ == DomElementType.TR) {
        double startY = ps.y;
        int startPage = ps.page;
        this.tableRowDoLayout(x, ps, cellSpacing, widths, rowSpanBackLog, renderer, -1);
        if (protectRows && ps.page != startPage) {
          ps.y = startY;
          ps.page = startPage;
          this.pageBreak(ps);
          if (repeatHead != null) {
            BlockBox bb = new BlockBox();
            bb.page = ps.page;
            bb.y = ps.y;
            bb.height = repeatHead.blockLayout.get(0).height;
            bb.x = x;
            bb.width = 0;
            repeatHead.blockLayout.add(bb);
            ps.y += bb.height;
          }
          startY = ps.y;
          startPage = ps.page;
          this.tableRowDoLayout(x, ps, cellSpacing, widths, rowSpanBackLog, renderer, -1);
        }
        double rowHeight =
            (ps.page - startPage) * renderer.textHeight(ps.page) + (ps.y - startY) - cellSpacing;
        ps.y = startY;
        ps.page = startPage;
        this.tableRowDoLayout(x, ps, cellSpacing, widths, rowSpanBackLog, renderer, rowHeight);
      }
    }
  }

  private void tableRowDoLayout(
      double x,
      final PageState ps,
      double cellSpacing,
      final List<Double> widths,
      final List<CellState> rowSpanBackLog,
      final WTextRenderer renderer,
      double rowHeight) {
    PageState rowEnd = new PageState();
    rowEnd.y = ps.y;
    rowEnd.page = ps.page;
    if (rowHeight == -1) {
      double height = this.cssHeight(renderer.getFontScale());
      if (height > 0) {
        advance(rowEnd, height, renderer);
      }
    }
    x += cellSpacing;
    for (int i = 0; i < this.children_.size(); ++i) {
      Block c = this.children_.get(i);
      if (c.isTableCell()) {
        int rowSpan = c.attributeValue("rowspan", 1);
        if (rowSpan > 1) {
          if (rowHeight == -1) {
            CellState cs = new CellState();
            cs.lastRow = c.cellRow_ + rowSpan - 1;
            cs.y = ps.y;
            cs.page = ps.page;
            cs.cell = c;
            rowSpanBackLog.add(cs);
          }
        } else {
          c.tableCellDoLayout(x, ps, cellSpacing, rowEnd, widths, renderer, rowHeight);
        }
      }
    }
    for (int i = 0; i < rowSpanBackLog.size(); ++i) {
      if (rowSpanBackLog.get(i).lastRow == this.cellRow_) {
        final CellState cs = rowSpanBackLog.get(i);
        double rh = rowHeight;
        if (rh >= 0) {
          rh += (ps.page - cs.page) * renderer.textHeight(cs.page) + (ps.y - cs.y);
        }
        cs.cell.tableCellDoLayout(x, cs, cellSpacing, rowEnd, widths, renderer, rh);
      }
    }
    ps.y = rowEnd.y;
    ps.page = rowEnd.page;
  }

  private void tableCellDoLayout(
      double x,
      final PageState ps,
      double cellSpacing,
      final PageState rowEnd,
      final List<Double> widths,
      final WTextRenderer renderer,
      double rowHeight) {
    x += this.tableCellX(widths, cellSpacing);
    double width = this.tableCellWidth(widths, cellSpacing);
    PageState cellPs = new PageState();
    cellPs.y = ps.y + cellSpacing;
    cellPs.page = ps.page;
    cellPs.minX = x;
    cellPs.maxX = x + width;
    double collapseMarginBottom = 0;
    double collapseMarginTop = Double.MAX_VALUE;
    String s = this.cssProperty(Property.StyleBackgroundColor);
    collapseMarginBottom =
        this.layoutBlock(
            cellPs, false, renderer, collapseMarginTop, collapseMarginBottom, rowHeight);
    if (collapseMarginBottom < collapseMarginTop) {
      cellPs.y -= collapseMarginBottom;
    }
    cellPs.minX = x;
    cellPs.maxX = x + width;
    Block.clearFloats(cellPs, width);
    if (cellPs.page > rowEnd.page || cellPs.page == rowEnd.page && cellPs.y > rowEnd.y) {
      rowEnd.page = cellPs.page;
      rowEnd.y = cellPs.y;
    }
  }

  private double tableCellX(final List<Double> widths, double cellSpacing) {
    double result = 0;
    for (int j = 0; j < this.cellCol_; ++j) {
      result += widths.get(j) + cellSpacing;
    }
    return result;
  }

  private double tableCellWidth(final List<Double> widths, double cellSpacing) {
    int colSpan = this.attributeValue("colspan", 1);
    double width = 0;
    for (int j = this.cellCol_; j < this.cellCol_ + colSpan; ++j) {
      width += widths.get(j);
    }
    return width + (colSpan - 1) * cellSpacing;
  }

  private void tableComputeColumnWidths(
      final List<Double> minima,
      final List<Double> maxima,
      final List<Double> asSet,
      final WTextRenderer renderer,
      Block table) {
    if (this.type_ == DomElementType.TBODY
        || this.type_ == DomElementType.THEAD
        || this.type_ == DomElementType.TFOOT) {
      for (int i = 0; i < this.children_.size(); ++i) {
        Block c = this.children_.get(i);
        c.tableComputeColumnWidths(minima, maxima, asSet, renderer, table);
      }
    } else {
      if (this.type_ == DomElementType.TR) {
        for (int i = 0; i < this.children_.size(); ++i) {
          Block c = this.children_.get(i);
          if (c.isTableCell()) {
            c.cellComputeColumnWidths(Block.WidthType.AsSetWidth, asSet, renderer, table);
            c.cellComputeColumnWidths(Block.WidthType.MinimumWidth, minima, renderer, table);
            c.cellComputeColumnWidths(Block.WidthType.MaximumWidth, maxima, renderer, table);
          }
        }
      }
    }
  }

  private Block.BorderElement collapseCellBorders(Side side) {
    List<Block.BorderElement> elements = new ArrayList<Block.BorderElement>();
    ;

    Block s = this.siblingTableCell(side);
    Block t = this.getTable();
    switch (side) {
      case Left:
        if (s != null) {
          elements.add(new Block.BorderElement(s, Side.Right));
          elements.add(new Block.BorderElement(this, Side.Left));
        } else {
          elements.add(new Block.BorderElement(this, Side.Left));
          elements.add(new Block.BorderElement(t, Side.Left));
        }
        break;
      case Top:
        if (s != null) {
          elements.add(new Block.BorderElement(s, Side.Bottom));
          elements.add(new Block.BorderElement(this, Side.Top));
        } else {
          elements.add(new Block.BorderElement(this, Side.Top));
          elements.add(new Block.BorderElement(t, Side.Top));
        }
        break;
      case Right:
        elements.add(new Block.BorderElement(this, Side.Right));
        if (s != null) {
          elements.add(new Block.BorderElement(s, Side.Left));
        } else {
          elements.add(new Block.BorderElement(t, Side.Right));
        }
        break;
      case Bottom:
        elements.add(new Block.BorderElement(this, Side.Bottom));
        if (s != null) {
          elements.add(new Block.BorderElement(s, Side.Top));
        } else {
          elements.add(new Block.BorderElement(t, Side.Bottom));
        }
      default:
        break;
    }
    double borderWidth = 0;
    Block.BorderElement result = new Block.BorderElement();
    for (int i = 0; i < elements.size(); ++i) {
      double c = elements.get(i).block.rawCssBorderWidth(elements.get(i).side, 1, true);
      if (c == -1) {
        return elements.get(i);
      }
      if (c > borderWidth) {
        result = elements.get(i);
        borderWidth = Math.max(borderWidth, c);
      }
    }
    if (!(result.block != null)) {
      return elements.get(0);
    } else {
      return result;
    }
  }

  private int numberTableCells(int row, final List<Integer> rowSpan) {
    if (this.type_ == DomElementType.TABLE
        || this.type_ == DomElementType.TBODY
        || this.type_ == DomElementType.THEAD
        || this.type_ == DomElementType.TFOOT) {
      for (int i = 0; i < this.children_.size(); ++i) {
        Block c = this.children_.get(i);
        row = c.numberTableCells(row, rowSpan);
      }
    } else {
      if (this.type_ == DomElementType.TR) {
        int col = 0;
        this.cellRow_ = row;
        for (int i = 0; i < this.children_.size(); ++i) {
          Block c = this.children_.get(i);
          if (c.isTableCell()) {
            while (col < (int) rowSpan.size() && rowSpan.get(col) > 0) {
              ++col;
            }
            c.cellCol_ = col;
            c.cellRow_ = row;
            int rs = c.attributeValue("rowspan", 1);
            int cs = c.attributeValue("colspan", 1);
            while ((int) rowSpan.size() <= col + cs - 1) {
              rowSpan.add(1);
            }
            for (int k = 0; k < cs; ++k) {
              rowSpan.set(col + k, rs);
            }
            col += cs;
          }
        }
        for (int i = 0; i < rowSpan.size(); ++i) {
          if (rowSpan.get(i) > 0) {
            rowSpan.set(i, rowSpan.get(i) - 1);
          }
        }
        ++row;
      }
    }
    return row;
  }

  private Block findTableCell(int row, int col) {
    if (this.type_ == DomElementType.TABLE
        || this.type_ == DomElementType.TBODY
        || this.type_ == DomElementType.THEAD
        || this.type_ == DomElementType.TFOOT) {
      for (int i = 0; i < this.children_.size(); ++i) {
        Block c = this.children_.get(i);
        Block result = c.findTableCell(row, col);
        if (result != null) {
          return result;
        }
      }
      return null;
    } else {
      if (this.type_ == DomElementType.TR) {
        for (int i = 0; i < this.children_.size(); ++i) {
          Block c = this.children_.get(i);
          if (c.isTableCell()) {
            int rs = c.attributeValue("rowspan", 1);
            int cs = c.attributeValue("colspan", 1);
            if (row >= c.cellRow_
                && row < c.cellRow_ + rs
                && col >= c.cellCol_
                && col < c.cellCol_ + cs) {
              return c;
            }
          }
        }
        return null;
      }
    }
    return null;
  }

  private Block siblingTableCell(Side side) {
    Block t = this.getTable();
    switch (side) {
      case Left:
        if (this.cellCol_ == 0) {
          return null;
        } else {
          return t.findTableCell(this.cellRow_, this.cellCol_ - 1);
        }
      case Right:
        {
          int nextCol = this.cellCol_ + this.attributeValue("colspan", 1);
          if (nextCol >= t.tableColCount_) {
            return null;
          } else {
            return t.findTableCell(this.cellRow_, nextCol);
          }
        }
      case Top:
        if (this.cellRow_ == 0) {
          return null;
        } else {
          return t.findTableCell(this.cellRow_ - 1, this.cellCol_);
        }
      case Bottom:
        {
          int nextRow = this.cellRow_ + this.attributeValue("rowspan", 1);
          if (nextRow >= t.tableRowCount_) {
            return null;
          } else {
            return t.findTableCell(nextRow, this.cellCol_);
          }
        }
      default:
        break;
    }
    return null;
  }

  private void cellComputeColumnWidths(
      Block.WidthType type, final List<Double> values, final WTextRenderer renderer, Block table) {
    double currentWidth = 0;
    int col = this.cellCol_;
    int colSpan = this.attributeValue("colspan", 1);
    double defaultWidth = 0;
    if (type == Block.WidthType.AsSetWidth) {
      defaultWidth = -1;
    }
    while (col + colSpan > (int) values.size()) {
      values.add(defaultWidth);
    }
    for (int i = 0; i < colSpan; ++i) {
      if (values.get(col + i) > 0) {
        currentWidth += values.get(col + i);
      }
    }
    double width = currentWidth;
    switch (type) {
      case AsSetWidth:
        width = this.cssWidth(renderer.getFontScale());
        break;
      case MinimumWidth:
      case MaximumWidth:
        {
          PageState ps = new PageState();
          ps.y = 0;
          ps.page = 0;
          ps.minX = 0;
          ps.maxX = width;
          double origTableWidth = table.currentWidth_;
          if (type == Block.WidthType.MinimumWidth) {
            table.currentWidth_ = 0;
          }
          this.layoutBlock(ps, type == Block.WidthType.MaximumWidth, renderer, 0, 0);
          table.currentWidth_ = origTableWidth;
          width = ps.maxX;
        }
    }
    if (width > currentWidth) {
      double extraPerColumn = (width - currentWidth) / colSpan;
      for (int i = 0; i < colSpan; ++i) {
        values.set(col + i, values.get(col + i) + extraPerColumn);
      }
    }
  }

  private void setOffsetParent() {
    this.offsetParent_ = this.getFindOffsetParent();
    this.offsetParent_.offsetChildren_.add(this);
  }

  private Block getFindOffsetParent() {
    if (this.parent_ != null) {
      String pos = this.parent_.cssProperty(Property.StylePosition);
      if (pos.equals("absolute") || pos.equals("fixed") || pos.equals("relative")) {
        return this.parent_;
      } else {
        return this.parent_.getFindOffsetParent();
      }
    } else {
      return this;
    }
  }

  private LayoutBox getLayoutTotal() {
    if (this.isInline()) {
      return this.getFirstInlineLayoutBox();
    } else {
      LayoutBox result = new LayoutBox();
      final LayoutBox first = this.blockLayout.get(0);
      result.x = first.x;
      result.y = first.y;
      result.page = first.page;
      result.width = first.width;
      result.height = first.height;
      for (int i = 1; i < this.blockLayout.size(); ++i) {
        result.height += this.blockLayout.get(i).height;
      }
      return result;
    }
  }

  private LayoutBox getFirstInlineLayoutBox() {
    if (!this.inlineLayout.isEmpty()) {
      return this.inlineLayout.get(0);
    } else {
      for (int i = 0; i < this.children_.size(); ++i) {
        Block c = this.children_.get(i);
        LayoutBox b = c.getFirstInlineLayoutBox();
        if (!b.isNull()) {
          return b;
        }
      }
      return new LayoutBox();
    }
  }

  private LayoutBox toBorderBox(final LayoutBox bb, double fontScale) {
    LayoutBox result = bb;
    if (this.isFloat()) {
      result.x += this.cssMargin(Side.Left, fontScale);
      result.y += this.cssMargin(Side.Top, fontScale);
      result.width -= this.cssMargin(Side.Left, fontScale) + this.cssMargin(Side.Right, fontScale);
      result.height -= this.cssMargin(Side.Top, fontScale) + this.cssMargin(Side.Bottom, fontScale);
    }
    return result;
  }

  private double maxLayoutY(int page) {
    double result = 0;
    for (int i = 0; i < this.inlineLayout.size(); ++i) {
      final InlineBox ib = this.inlineLayout.get(i);
      if (page == -1 || ib.page == page) {
        result = Math.max(result, ib.y + ib.height);
      }
    }
    for (int i = 0; i < this.blockLayout.size(); ++i) {
      final BlockBox lb = this.blockLayout.get(i);
      if (page == -1 || lb.page == page) {
        result = Math.max(result, lb.y + lb.height);
      }
    }
    if (this.inlineLayout.isEmpty() && this.blockLayout.isEmpty()) {
      for (int i = 0; i < this.children_.size(); ++i) {
        result = Math.max(result, this.children_.get(i).maxLayoutY(page));
      }
    }
    return result;
  }

  private double minLayoutY(int page) {
    double result = 1E9;
    for (int i = 0; i < this.inlineLayout.size(); ++i) {
      final InlineBox ib = this.inlineLayout.get(i);
      if (page == -1 || ib.page == page) {
        result = Math.min(result, ib.y);
      }
    }
    for (int i = 0; i < this.blockLayout.size(); ++i) {
      final BlockBox lb = this.blockLayout.get(i);
      if (page == -1 || lb.page == page) {
        result = Math.min(result, lb.y);
      }
    }
    if (this.inlineLayout.isEmpty() && this.blockLayout.isEmpty()) {
      for (int i = 0; i < this.children_.size(); ++i) {
        result = Math.min(result, this.children_.get(i).minLayoutY(page));
      }
    }
    return result;
  }

  private double maxChildrenLayoutY(int page) {
    double result = 0;
    for (int i = 0; i < this.children_.size(); ++i) {
      result = Math.max(result, this.children_.get(i).maxLayoutY(page));
    }
    return result;
  }

  private double minChildrenLayoutY(int page) {
    double result = 1E9;
    for (int i = 0; i < this.children_.size(); ++i) {
      result = Math.min(result, this.children_.get(i).minLayoutY(page));
    }
    return result;
  }

  private double childrenLayoutHeight(int page) {
    return this.maxChildrenLayoutY(page) - this.minChildrenLayoutY(page);
  }

  private void reLayout(final LayoutBox from, final LayoutBox to) {
    for (int i = 0; i < this.inlineLayout.size(); ++i) {
      final InlineBox ib = this.inlineLayout.get(i);
      ib.page = to.page;
      ib.x += to.x - from.x;
      ib.y += to.y - from.y;
    }
    for (int i = 0; i < this.blockLayout.size(); ++i) {
      final BlockBox bb = this.blockLayout.get(i);
      bb.page = to.page;
      bb.x += to.x - from.x;
      bb.y += to.y - from.y;
    }
    for (int i = 0; i < this.children_.size(); ++i) {
      this.children_.get(i).reLayout(from, to);
    }
  }

  private void renderText(
      final String text, final WTextRenderer renderer, final WPainter painter, int page) {
    WPaintDevice device = painter.getDevice();
    painter.setFont(this.cssFont(renderer.getFontScale()));
    WFontMetrics metrics = device.getFontMetrics();
    double lineHeight = this.cssLineHeight(metrics.getHeight(), renderer.getFontScale());
    double fontHeight = metrics.getSize();
    String decoration = this.getCssTextDecoration();
    for (int i = 0; i < this.inlineLayout.size(); ++i) {
      final InlineBox ib = this.inlineLayout.get(i);
      if (ib.page == page) {
        double y =
            renderer.getMargin(Side.Top)
                + ib.y
                - metrics.getLeading()
                + (lineHeight - fontHeight) / 2.0;
        WRectF rect = new WRectF(renderer.getMargin(Side.Left) + ib.x, y, ib.width, ib.height);
        painter.setPen(new WPen(this.getCssColor()));
        if (ib.whitespaceWidth == device.measureText(" ").getWidth()) {
          WString t = new WString(text.substring(ib.utf8Pos, ib.utf8Pos + ib.utf8Count));
          painter.drawText(
              new WRectF(
                  rect.getX(),
                  rect.getY(),
                  rect.getWidth(),
                  rect.getHeight() + metrics.getLeading()),
              EnumSet.of(AlignmentFlag.Left, AlignmentFlag.Top),
              t);
        } else {
          double x = rect.getLeft();
          int wordStart = 0;
          for (int j = 0; j <= ib.utf8Count; ++j) {
            if (j == ib.utf8Count || isWhitespace(text.charAt(ib.utf8Pos + j))) {
              if (j > wordStart) {
                WString word =
                    new WString(
                        text.substring(
                            ib.utf8Pos + wordStart, ib.utf8Pos + wordStart + j - wordStart));
                double wordWidth = device.measureText(word).getWidth();
                painter.drawText(
                    new WRectF(x, rect.getTop(), wordWidth, rect.getHeight()),
                    EnumSet.of(AlignmentFlag.Left, AlignmentFlag.Top),
                    word);
                x += wordWidth;
              }
              x += ib.whitespaceWidth;
              wordStart = j + 1;
            }
          }
        }
        if (decoration.equals("underline")) {
          double below = y + metrics.getLeading() + metrics.getAscent() + 2;
          painter.drawLine(rect.getLeft(), below, rect.getRight(), below);
        } else {
          if (decoration.equals("overline")) {
            double over = renderer.getMargin(Side.Top) + ib.y + 2;
            painter.drawLine(rect.getLeft(), over, rect.getRight(), over);
          } else {
            if (decoration.equals("line-through")) {
              double through = y + metrics.getLeading() + metrics.getAscent() - 3;
              painter.drawLine(rect.getLeft(), through, rect.getRight(), through);
            }
          }
        }
      } else {
        if (ib.page > page) {
          break;
        }
      }
    }
  }

  private void renderBorders(
      final LayoutBox bb,
      final WTextRenderer renderer,
      final WPainter painter,
      EnumSet<Side> verticals) {
    if (!(this.node_ != null)) {
      return;
    }
    double left = renderer.getMargin(Side.Left) + bb.x;
    double top = renderer.getMargin(Side.Top) + bb.y;
    double right = left + bb.width;
    double bottom = top + bb.height;
    double[] borderWidth = new double[4];
    WColor[] borderColor = new WColor[4];
    Side[] sides = {Side.Top, Side.Right, Side.Bottom, Side.Left};
    for (int i = 0; i < 4; ++i) {
      borderWidth[i] = this.cssBorderWidth(sides[i], renderer.getFontScale());
      borderColor[i] = this.cssBorderColor(sides[i]);
    }
    double offsetFactor = 1;
    if (this.isTableCell()) {
      Block t = this.getTable();
      if (t != null && t.isTableCollapseBorders()) {
        offsetFactor = 0;
      }
    }
    double[] cornerMaxWidth = {0, 0, 0, 0};
    if (offsetFactor == 0) {
      Block[] siblings = new Block[4];
      for (int i = 0; i < 4; ++i) {
        siblings[i] = this.siblingTableCell(sides[i]);
      }
      cornerMaxWidth[Block.Corner.TopLeft.getValue()] =
          maxBorderWidth(
              siblings[3],
              Side.Top,
              this,
              Side.Top,
              siblings[0],
              Side.Left,
              this,
              Side.Left,
              renderer.getFontScale());
      cornerMaxWidth[Block.Corner.TopRight.getValue()] =
          maxBorderWidth(
              siblings[1],
              Side.Top,
              this,
              Side.Top,
              siblings[0],
              Side.Right,
              this,
              Side.Right,
              renderer.getFontScale());
      cornerMaxWidth[Block.Corner.BottomLeft.getValue()] =
          maxBorderWidth(
              siblings[3],
              Side.Bottom,
              this,
              Side.Bottom,
              siblings[2],
              Side.Left,
              this,
              Side.Left,
              renderer.getFontScale());
      cornerMaxWidth[Block.Corner.BottomRight.getValue()] =
          maxBorderWidth(
              siblings[1],
              Side.Bottom,
              this,
              Side.Bottom,
              siblings[2],
              Side.Right,
              this,
              Side.Right,
              renderer.getFontScale());
    }
    for (int i = 0; i < 4; ++i) {
      if (borderWidth[i] != 0) {
        WPen borderPen = new WPen();
        borderPen.setCapStyle(PenCapStyle.Flat);
        borderPen.setWidth(new WLength(borderWidth[i]));
        borderPen.setColor(borderColor[i]);
        painter.setPen(borderPen);
        switch (sides[i]) {
          case Top:
            if (verticals.contains(Side.Top)) {
              double leftOffset = 0;
              double rightOffset = 0;
              if (borderWidth[i] < cornerMaxWidth[Block.Corner.TopLeft.getValue()]) {
                leftOffset = cornerMaxWidth[Block.Corner.TopLeft.getValue()] / 2;
              } else {
                if (offsetFactor == 0) {
                  leftOffset = -borderWidth[i] / 2;
                }
              }
              if (borderWidth[i] < cornerMaxWidth[Block.Corner.TopRight.getValue()]) {
                rightOffset = cornerMaxWidth[Block.Corner.TopRight.getValue()] / 2;
              } else {
                if (offsetFactor == 0) {
                  rightOffset = -borderWidth[i] / 2;
                }
              }
              painter.drawLine(
                  left + leftOffset,
                  top + offsetFactor * borderWidth[i] / 2,
                  right - rightOffset,
                  top + offsetFactor * borderWidth[i] / 2);
            }
            break;
          case Right:
            {
              double topOffset = 0;
              double bottomOffset = 0;
              if (borderWidth[i] < cornerMaxWidth[Block.Corner.TopRight.getValue()]) {
                topOffset = cornerMaxWidth[Block.Corner.TopRight.getValue()] / 2;
              } else {
                if (offsetFactor == 0) {
                  topOffset = -borderWidth[i] / 2;
                }
              }
              if (borderWidth[i] < cornerMaxWidth[Block.Corner.BottomRight.getValue()]) {
                bottomOffset = cornerMaxWidth[Block.Corner.BottomRight.getValue()] / 2;
              } else {
                if (offsetFactor == 0) {
                  bottomOffset = -borderWidth[i] / 2;
                }
              }
              painter.drawLine(
                  right - offsetFactor * borderWidth[i] / 2,
                  top + topOffset,
                  right - offsetFactor * borderWidth[i] / 2,
                  bottom - bottomOffset);
            }
            break;
          case Bottom:
            if (verticals.contains(Side.Bottom)) {
              double leftOffset = 0;
              double rightOffset = 0;
              if (borderWidth[i] < cornerMaxWidth[Block.Corner.BottomLeft.getValue()]) {
                leftOffset = cornerMaxWidth[Block.Corner.BottomLeft.getValue()] / 2;
              } else {
                if (offsetFactor == 0) {
                  leftOffset = -borderWidth[i] / 2;
                }
              }
              if (borderWidth[i] < cornerMaxWidth[Block.Corner.TopRight.getValue()]) {
                rightOffset = cornerMaxWidth[Block.Corner.BottomRight.getValue()] / 2;
              } else {
                if (offsetFactor == 0) {
                  rightOffset = -borderWidth[i] / 2;
                }
              }
              painter.drawLine(
                  left + leftOffset,
                  bottom - offsetFactor * borderWidth[i] / 2,
                  right - rightOffset,
                  bottom - offsetFactor * borderWidth[i] / 2);
            }
            break;
          case Left:
            {
              double topOffset = 0;
              double bottomOffset = 0;
              if (borderWidth[i] < cornerMaxWidth[Block.Corner.TopLeft.getValue()]) {
                topOffset = cornerMaxWidth[Block.Corner.TopLeft.getValue()] / 2;
              } else {
                if (offsetFactor == 0) {
                  topOffset = -borderWidth[i] / 2;
                }
              }
              if (borderWidth[i] < cornerMaxWidth[Block.Corner.BottomLeft.getValue()]) {
                bottomOffset = cornerMaxWidth[Block.Corner.BottomLeft.getValue()] / 2;
              } else {
                if (offsetFactor == 0) {
                  bottomOffset = -borderWidth[i] / 2;
                }
              }
              painter.drawLine(
                  left + offsetFactor * borderWidth[i] / 2,
                  top + topOffset,
                  left + offsetFactor * borderWidth[i] / 2,
                  bottom - bottomOffset);
            }
            break;
          default:
            break;
        }
      }
    }
  }

  private final void renderBorders(
      final LayoutBox bb,
      final WTextRenderer renderer,
      final WPainter painter,
      Side vertical,
      Side... verticals) {
    renderBorders(bb, renderer, painter, EnumSet.of(vertical, verticals));
  }

  private WString getGenerateItem() {
    boolean numbered = this.parent_ != null && this.parent_.type_ == DomElementType.OL;
    if (numbered) {
      int counter = 0;
      for (int i = 0; i < this.parent_.children_.size(); ++i) {
        Block child = this.parent_.children_.get(i);
        if (child.type_ == DomElementType.LI) {
          ++counter;
        }
        if (child == this) {
          break;
        }
      }
      return new WString(String.valueOf(counter) + ". ");
    } else {
      return new WString("- ");
    }
  }

  private int getFirstLayoutPage() {
    if (!this.inlineLayout.isEmpty()) {
      return this.inlineLayout.get(0).page;
    }
    if (!this.blockLayout.isEmpty()) {
      return this.blockLayout.get(0).page;
    }
    return -1;
  }

  private int getLastLayoutPage() {
    if (!this.inlineLayout.isEmpty()) {
      return this.inlineLayout.get(this.inlineLayout.size() - 1).page;
    }
    if (!this.blockLayout.isEmpty()) {
      return this.blockLayout.get(this.blockLayout.size() - 1).page;
    }
    return -1;
  }

  private static void advance(final PageState ps, double height, final WTextRenderer renderer) {
    while (ps.y + height > renderer.textHeight(ps.page)) {
      ++ps.page;
      ps.y = 0;
      height -= renderer.textHeight(ps.page) - ps.y;
      if (height < 0) {
        height = 0;
      }
      if (renderer.textHeight(ps.page) - ps.y < 0 && height >= 0) {
        throw new WException("The margin is too large");
      }
    }
    ps.y += height;
  }

  private static double diff(
      double y, int page, double startY, int startPage, final WTextRenderer renderer) {
    double result = y - startY;
    while (page > startPage) {
      result += renderer.textHeight(page);
      --page;
    }
    return result;
  }

  private static double positionFloat(
      double x,
      final PageState ps,
      double lineHeight,
      double width,
      boolean canIncreaseWidth,
      final WTextRenderer renderer,
      FloatSide floatSide) {
    if (!ps.floats.isEmpty()) {
      double minY = ps.floats.get(ps.floats.size() - 1).blockLayout.get(0).y;
      if (minY > ps.y) {
        if (minY < ps.y + lineHeight) {
          lineHeight -= minY - ps.y;
        } else {
          x = ps.minX;
        }
        ps.y = minY;
      }
    }
    List<Block> floats = ps.floats;
    for (; ; ) {
      Range rangeX = new Range(ps.minX, ps.maxX);
      adjustAvailableWidth(ps.y, ps.page, ps.floats, rangeX);
      ps.maxX = rangeX.end;
      double availableWidth = rangeX.end - Math.max(x, rangeX.start);
      if (!isEpsilonLess(availableWidth, width)) {
        break;
      } else {
        if (canIncreaseWidth) {
          ps.maxX += width - availableWidth;
          break;
        } else {
          if (x > rangeX.start) {
            ps.y += lineHeight;
            x = ps.minX;
          } else {
            clearFloats(ps, width);
            break;
          }
        }
      }
    }
    Utils.copyList(floats, ps.floats);
    Range rangeX = new Range(ps.minX, ps.maxX);
    adjustAvailableWidth(ps.y, ps.page, ps.floats, rangeX);
    ps.maxX = rangeX.end;
    if (floatSide == FloatSide.Left) {
      x = rangeX.start;
    } else {
      x = rangeX.end - width;
    }
    return x;
  }

  private static void unsupportedAttributeValue(String attribute, final String value) {
    logger.error(
        new StringWriter()
            .append("unsupported value '")
            .append(value)
            .append("' for attribute ")
            .append(attribute)
            .toString());
  }

  private static void unsupportedCssValue(Property property, final String value) {
    logger.error(
        new StringWriter()
            .append("unsupported value '")
            .append(value)
            .append("'for CSS style property ")
            .append(DomElement.cssName(property))
            .toString());
  }

  private static boolean isAggregate(final String cssProperty) {
    return cssProperty.equals("margin")
        || cssProperty.equals("border")
        || cssProperty.equals("padding")
        || cssProperty.equals("border-color")
        || cssProperty.equals("border-width");
  }

  private static double maxBorderWidth(
      Block b1,
      Side s1,
      Block b2,
      Side s2,
      Block b3,
      Side s3,
      Block b4,
      Side s4,
      double fontScale) {
    double result = 0;
    if (b1 != null) {
      result = Math.max(result, b1.collapsedBorderWidth(s1, fontScale));
    }
    if (b2 != null) {
      result = Math.max(result, b2.collapsedBorderWidth(s2, fontScale));
    }
    if (b3 != null) {
      result = Math.max(result, b3.collapsedBorderWidth(s3, fontScale));
    }
    if (b4 != null) {
      result = Math.max(result, b4.collapsedBorderWidth(s4, fontScale));
    }
    return result;
  }

  private static final double MARGINX = -1;
  private static final double EPSILON = 1e-4;

  static boolean isEpsilonMore(double x, double limit) {
    return x - EPSILON > limit;
  }

  static boolean isEpsilonLess(double x, double limit) {
    return x + EPSILON < limit;
  }

  static int sideToIndex(Side side) {
    switch (side) {
      case Top:
        return 0;
      case Right:
        return 1;
      case Bottom:
        return 2;
      case Left:
        return 3;
      default:
        return -1;
    }
  }

  static double sum(final List<Double> v) {
    double result = 0;
    for (int i = 0; i < v.size(); ++i) {
      result += v.get(i);
    }
    return result;
  }

  static boolean isOffsetAuto(final String s) {
    return s.length() == 0 || s.equals("auto");
  }
}
