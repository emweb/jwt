/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

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

class Block {
	private static Logger logger = LoggerFactory.getLogger(Block.class);

	public Block(net.n3.nanoxml.XMLElement node, Block parent) {
		this.inlineLayout = new ArrayList<InlineBox>();
		this.blockLayout = new ArrayList<BlockBox>();
		this.node_ = node;
		this.parent_ = parent;
		this.offsetChildren_ = new ArrayList<Block>();
		this.type_ = DomElementType.DomElement_UNKNOWN;
		this.inline_ = false;
		this.children_ = new ArrayList<Block>();
		this.currentTheadBlock_ = null;
		this.currentWidth_ = 0;
		this.contentsHeight_ = 0;
		this.css_ = new HashMap<String, Block.PropertyValue>();
		this.styleSheet_ = null;
		if (node != null) {
			if (RenderUtils.isXmlElement(node)) {
				this.type_ = DomElement.parseTagName(node.getName());
				if (this.type_ == DomElementType.DomElement_UNKNOWN) {
					logger.error(new StringWriter().append(
							"unsupported element: ").append(node.getName())
							.toString());
					this.type_ = DomElementType.DomElement_DIV;
				}
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
		String fl = this.cssProperty(Property.PropertyStyleFloat);
		if (fl.length() != 0) {
			if (fl.equals("left")) {
				this.float_ = Side.Left;
			} else {
				if (fl.equals("right")) {
					this.float_ = Side.Right;
				} else {
					unsupportedCssValue(Property.PropertyStyleFloat, fl);
				}
			}
		} else {
			if (this.type_ == DomElementType.DomElement_IMG
					|| this.type_ == DomElementType.DomElement_TABLE) {
				String align = this.attributeValue("align");
				if (align.length() != 0) {
					if (align.equals("left")) {
						this.float_ = Side.Left;
					} else {
						if (align.equals("right")) {
							this.float_ = Side.Right;
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
				Block b = i < this.children_.size() ? this.children_.get(i)
						: null;
				if (!(b != null) || !b.isFloat()) {
					if (b != null && b.inline_ && firstToGroup == -1) {
						firstToGroup = i;
					}
					if ((!(b != null) || !b.inline_) && firstToGroup != -1
							&& (int) i > firstToGroup - 1) {
						Block anonymous = new Block(
								(net.n3.nanoxml.XMLElement) null, this);
						this.children_.add(0 + i, anonymous);
						anonymous.inline_ = false;
						for (int j = firstToGroup; j < i; ++j) {
							anonymous.children_.add(this.children_
									.get(firstToGroup));
							this.children_.remove(0 + firstToGroup);
						}
						i -= i - firstToGroup;
						firstToGroup = -1;
					}
				}
			}
		}
		switch (this.type_) {
		case DomElement_UNKNOWN:
			if (allChildrenInline) {
				this.inline_ = true;
			}
			break;
		default:
			if (!this.isFloat()) {
				String display = this
						.cssProperty(Property.PropertyStyleDisplay);
				if (display.length() != 0) {
					if (display.equals("inline")) {
						this.inline_ = true;
					} else {
						if (display.equals("block")) {
							this.inline_ = false;
						} else {
							logger.error(new StringWriter().append("display '")
									.append(display).append(
											"' is not supported.").toString());
							this.inline_ = false;
						}
					}
				} else {
					this.inline_ = DomElement.isDefaultInline(this.type_);
				}
				if (this.inline_ && !allChildrenInline) {
					logger.error(new StringWriter().append("inline element ")
							.append(DomElement.tagName(this.type_)).append(
									" cannot contain block elements")
							.toString());
				}
			} else {
				this.inline_ = false;
			}
		}
	}

	public boolean normalizeWhitespace(boolean haveWhitespace,
			net.n3.nanoxml.XMLElement doc) {
		boolean whitespaceIn = haveWhitespace;
		if (!this.isInline()) {
			haveWhitespace = true;
		}
		if (this.type_ == DomElementType.DomElement_UNKNOWN && this.isText()) {
			haveWhitespace = RenderUtils.normalizeWhitespace(this, this.node_,
					haveWhitespace, doc);
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
		return this.float_ != null;
	}

	public boolean isInline() {
		return this.inline_;
	}

	public DomElementType getType() {
		return this.type_;
	}

	public boolean isText() {
		return this.node_ != null && this.children_.isEmpty()
				&& this.type_ == DomElementType.DomElement_UNKNOWN
				|| this.type_ == DomElementType.DomElement_LI;
	}

	public String getText() {
		if (this.type_ == DomElementType.DomElement_LI) {
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
			if (!this.children_.get(i).isFloat()
					&& this.children_.get(i).inline_) {
				return true;
			}
		}
		return false;
	}

	public AlignmentFlag getHorizontalAlignment() {
		String marginLeft = this.cssProperty(Property.PropertyStyleMarginLeft);
		String marginRight = this
				.cssProperty(Property.PropertyStyleMarginRight);
		if (marginLeft.equals("auto")) {
			if (marginRight.equals("auto")) {
				return AlignmentFlag.AlignCenter;
			} else {
				return AlignmentFlag.AlignRight;
			}
		} else {
			if (marginRight.equals("auto")) {
				return AlignmentFlag.AlignLeft;
			} else {
				return AlignmentFlag.AlignJustify;
			}
		}
	}

	public AlignmentFlag getVerticalAlignment() {
		String va = this.cssProperty(Property.PropertyStyleVerticalAlign);
		if (va.length() == 0) {
			va = this.attributeValue("valign");
		}
		if (va.length() == 0 || va.equals("middle")) {
			return AlignmentFlag.AlignMiddle;
		} else {
			if (va.equals("bottom")) {
				return AlignmentFlag.AlignBottom;
			} else {
				return AlignmentFlag.AlignTop;
			}
		}
	}

	public Side getFloatSide() {
		return this.float_;
	}

	public double layoutBlock(PageState ps, boolean canIncreaseWidth,
			WTextRenderer renderer, double collapseMarginTop,
			double collapseMarginBottom, double cellHeight) {
		String pageBreakBefore = this
				.cssProperty(Property.PropertyStylePageBreakBefore);
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
			case AlignTop:
				spacerBottom = cellHeight - ch;
				break;
			case AlignMiddle:
				spacerTop = spacerBottom = (cellHeight - ch) / 2;
				break;
			case AlignBottom:
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
		if (this.type_ == DomElementType.DomElement_TABLE) {
			if (cssSetWidth > 0) {
				cssSetWidth -= this.cssBorderWidth(Side.Left, renderer
						.getFontScale())
						+ this.cssBorderWidth(Side.Right, renderer
								.getFontScale());
				cssSetWidth = Math.max(0.0, cssSetWidth);
			}
			this.layoutTable(ps, canIncreaseWidth, renderer, cssSetWidth);
		} else {
			double width = cssSetWidth;
			boolean paddingBorderWithinWidth = this.isTableCell()
					&& isPercentageLength(this
							.cssProperty(Property.PropertyStyleWidth));
			if (width >= 0) {
				if (!paddingBorderWithinWidth) {
					width += this
							.cssPadding(Side.Left, renderer.getFontScale())
							+ this.cssBorderWidth(Side.Left, renderer
									.getFontScale())
							+ this.cssPadding(Side.Right, renderer
									.getFontScale())
							+ this.cssBorderWidth(Side.Right, renderer
									.getFontScale());
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
				case AlignJustify:
				case AlignLeft:
					ps.maxX = ps.minX + width;
					break;
				case AlignCenter:
					ps.minX = ps.minX + (ps.maxX - ps.minX - width) / 2;
					ps.maxX = ps.minX + width;
					break;
				case AlignRight:
					ps.minX = ps.maxX - width;
					break;
				default:
					break;
				}
			}
			if (this.type_ == DomElementType.DomElement_IMG) {
				double height = this.cssHeight(renderer.getFontScale());
				String src = this.attributeValue("src");
				if (width <= 0 || height <= 0) {
					WPainter.Image image = new WPainter.Image(src, src);
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
					ps.y += this.cssBorderWidth(Side.Top, renderer
							.getFontScale());
				}
				ps.y += height;
				ps.maxX = Math.max(ps.minX + width, ps.maxX);
			} else {
				double cMinX = ps.minX
						+ this.cssPadding(Side.Left, renderer.getFontScale())
						+ this.cssBorderWidth(Side.Left, renderer
								.getFontScale());
				double cMaxX = ps.maxX
						- this.cssPadding(Side.Right, renderer.getFontScale())
						- this.cssBorderWidth(Side.Right, renderer
								.getFontScale());
				this.currentWidth_ = cMaxX - cMinX;
				ps.y += this.cssPadding(Side.Top, renderer.getFontScale());
				advance(ps, spacerTop, renderer);
				if (this.isInlineChildren()) {
					Line line = new Line(cMinX, ps.y, ps.page);
					renderer.getPainter().setFont(
							this.cssFont(renderer.getFontScale()));
					cMaxX = this.layoutInline(line, ps.floats, cMinX, cMaxX,
							canIncreaseWidth, renderer);
					line.setLineBreak(true);
					line.finish(this.getCssTextAlign(), ps.floats, cMinX,
							cMaxX, renderer);
					ps.y = line.getBottom();
					ps.page = line.getPage();
				} else {
					double minY = ps.y;
					int minPage = ps.page;
					if (this.type_ == DomElementType.DomElement_LI) {
						Line line = new Line(0, ps.y, ps.page);
						double x2 = 1000;
						x2 = this.layoutInline(line, ps.floats, cMinX, x2,
								false, renderer);
						line.setLineBreak(true);
						line.finish(AlignmentFlag.AlignLeft, ps.floats, cMinX,
								x2, renderer);
						this.inlineLayout.get(0).x -= this.inlineLayout.get(0).width;
						minY = line.getBottom();
						minPage = line.getPage();
						ps.y = line.getY();
						ps.page = line.getPage();
					}
					for (int i = 0; i < this.children_.size(); ++i) {
						Block c = this.children_.get(i);
						if (c.isFloat()) {
							cMaxX = c.layoutFloat(ps.y, ps.page, ps.floats,
									cMinX, 0, cMinX, cMaxX, canIncreaseWidth,
									renderer);
						} else {
							if (c.isPositionedAbsolutely()) {
								if (!(c.offsetParent_ != null)) {
									c.setOffsetParent();
								}
								PageState absolutePs = ps;
								c
										.layoutBlock(absolutePs, false,
												renderer, 0, 0);
							} else {
								double copyMinX = ps.minX;
								double copyMaxX = ps.maxX;
								ps.minX = cMinX;
								ps.maxX = cMaxX;
								collapseMarginBottom = c
										.layoutBlock(ps, canIncreaseWidth,
												renderer, collapseMarginTop,
												collapseMarginBottom);
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
				ps.maxX = cMaxX
						+ this.cssPadding(Side.Right, renderer.getFontScale())
						+ this.cssBorderWidth(Side.Right, renderer
								.getFontScale());
				advance(ps, spacerBottom, renderer);
				ps.y += this.cssPadding(Side.Bottom, renderer.getFontScale());
			}
		}
		ps.y += this.cssBorderWidth(Side.Bottom, renderer.getFontScale());
		double marginBottom = this.cssMargin(Side.Bottom, renderer
				.getFontScale());
		ps.y -= collapseMarginBottom;
		double height = this.cssHeight(renderer.getFontScale());
		if (this.isTableCell()) {
			this.contentsHeight_ = Math.max(0.0, diff(ps.y, ps.page, startY,
					startPage, renderer));
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
			if (this.type_ == DomElementType.DomElement_TABLE) {
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
				BlockBox box = this.blockLayout
						.get(this.blockLayout.size() - 1);
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
			BlockBox box = this.blockLayout.get(this.blockLayout.size() - 1);
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
				&& isPercentageLength(this
						.cssProperty(Property.PropertyStyleWidth))) {
			ps.maxX = origMaxX;
		} else {
			if (ps.maxX < origMaxX) {
				ps.maxX = origMaxX;
			} else {
				if (!this.isFloat()) {
					ps.minX -= this.cssMargin(Side.Left, renderer
							.getFontScale());
					ps.maxX += this.cssMargin(Side.Right, renderer
							.getFontScale());
				}
			}
		}
		ps.minX = origMinX;
		String pageBreakAfter = this
				.cssProperty(Property.PropertyStylePageBreakAfter);
		if (pageBreakAfter.equals("always")) {
			this.pageBreak(ps);
			return 0;
		} else {
			return collapseMarginBottom;
		}
	}

	public final double layoutBlock(PageState ps, boolean canIncreaseWidth,
			WTextRenderer renderer, double collapseMarginTop,
			double collapseMarginBottom) {
		return layoutBlock(ps, canIncreaseWidth, renderer, collapseMarginTop,
				collapseMarginBottom, -1);
	}

	public void collectStyles(StringBuilder ss) {
		for (int i = 0; i < this.children_.size(); ++i) {
			if (this.children_.get(i).type_ == DomElementType.DomElement_STYLE) {
				ss.append(RenderUtils
						.nodeValueToString(this.children_.get(i).node_));
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
		for (int i = 0; i < this.children_.size(); ++i) {
			this.children_.get(i).setStyleSheet(styleSheet);
		}
	}

	public void actualRender(WTextRenderer renderer, WPainter painter,
			LayoutBox lb) {
		if (this.type_ == DomElementType.DomElement_IMG) {
			LayoutBox bb = this.toBorderBox(lb, renderer.getFontScale());
			this.renderBorders(bb, renderer, painter, EnumSet.of(Side.Top,
					Side.Bottom));
			double left = renderer.getMargin(Side.Left) + bb.x
					+ this.cssBorderWidth(Side.Left, renderer.getFontScale());
			double top = renderer.getMargin(Side.Top) + bb.y
					+ this.cssBorderWidth(Side.Top, renderer.getFontScale());
			double width = bb.width;
			double height = bb.height;
			WRectF rect = new WRectF(left, top, width, height);
			painter.drawImage(rect, new WPainter.Image(this
					.attributeValue("src"), (int) width, (int) height));
		} else {
			LayoutBox bb = this.toBorderBox(lb, renderer.getFontScale());
			WRectF rect = new WRectF(bb.x + renderer.getMargin(Side.Left), bb.y
					+ renderer.getMargin(Side.Top), bb.width, bb.height);
			String s = this.cssProperty(Property.PropertyStyleBackgroundColor);
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
			if (this.type_ == DomElementType.DomElement_THEAD) {
				for (int j = 0; j < this.children_.size(); ++j) {
					if (this.currentTheadBlock_ != lb) {
						this.children_.get(j).reLayout(this.currentTheadBlock_,
								lb);
					}
					this.children_.get(j).render(renderer, painter, lb.page);
				}
				this.currentTheadBlock_ = lb;
			}
		}
		if (this.type_ != DomElementType.DomElement_THEAD) {
			for (int i = 0; i < this.children_.size(); ++i) {
				this.children_.get(i).render(renderer, painter, lb.page);
			}
		}
	}

	public void render(WTextRenderer renderer, WPainter painter, int page) {
		boolean painterTranslated = false;
		if (this.cssProperty(Property.PropertyStylePosition).equals("relative")) {
			painter.save();
			painterTranslated = true;
			LayoutBox box = this.getLayoutTotal();
			double left = this.cssDecodeLength(this
					.cssProperty(Property.PropertyStyleLeft), renderer
					.getFontScale(), 0,
					Block.PercentageRule.PercentageOfParentSize, box.width);
			double top = this.cssDecodeLength(this
					.cssProperty(Property.PropertyStyleTop), renderer
					.getFontScale(), 0,
					Block.PercentageRule.PercentageOfParentSize, box.height);
			painter.translate(left, top);
		}
		if (this.isText()) {
			this.renderText(this.getText(), renderer, painter, page);
			if (this.type_ != DomElementType.DomElement_LI) {
				if (painterTranslated) {
					painter.restore();
				}
				return;
			}
		}
		if (this.type_ == DomElementType.DomElement_THEAD) {
			this.currentTheadBlock_ = this.blockLayout.isEmpty() ? null
					: this.blockLayout.get(0);
		}
		int first = this.type_ == DomElementType.DomElement_LI ? 1 : 0;
		for (int i = first; i < this.inlineLayout.size(); ++i) {
			LayoutBox lb = this.inlineLayout.get(i);
			if (lb.page == page) {
				renderer.paintNode(painter, new WTextRenderer.Node(this, lb,
						renderer));
			}
		}
		for (int i = 0; i < this.blockLayout.size(); ++i) {
			LayoutBox lb = this.blockLayout.get(i);
			if (lb.page == page) {
				renderer.paintNode(painter, new WTextRenderer.Node(this, lb,
						renderer));
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

	public static void clearFloats(PageState ps) {
		for (int i = 0; i < ps.floats.size(); ++i) {
			Block b = ps.floats.get(i);
			BlockBox bb = b.blockLayout.get(b.blockLayout.size() - 1);
			if (bb.page <= ps.page) {
				ps.floats.remove(0 + i);
				--i;
			}
		}
	}

	public static void clearFloats(PageState ps, double minWidth) {
		for (; !ps.floats.isEmpty();) {
			Block b = ps.floats.get(0);
			ps.y = b.blockLayout.get(b.blockLayout.size() - 1).y
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

	public static void adjustAvailableWidth(double y, int page,
			List<Block> floats, Range rangeX) {
		for (int i = 0; i < floats.size(); ++i) {
			Block b = floats.get(i);
			for (int j = 0; j < b.blockLayout.size(); ++j) {
				BlockBox block = b.blockLayout.get(j);
				if (block.page == page) {
					if (block.y <= y && y < block.y + block.height) {
						if (floats.get(i).getFloatSide() == Side.Left) {
							rangeX.start = Math.max(rangeX.start, block.x
									+ block.width);
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
		String s = this.attributeValue("class");
		List<String> r = new ArrayList<String>();
		r = new ArrayList<String>(Arrays.asList(s.split(" ")));
		return r;
	}

	String cssProperty(Property property) {
		if (!(this.node_ != null)) {
			return "";
		}
		if (this.css_.isEmpty()) {
			if (this.styleSheet_ != null) {
				for (int i = 0; i < this.styleSheet_.getRulesetSize(); ++i) {
					Specificity s = Match.isMatch(this, this.styleSheet_
							.rulesetAt(i).getSelector());
					if (s.isValid()) {
						this.fillinStyle(this.styleSheet_.rulesetAt(i)
								.getDeclarationBlock().getDeclarationString(),
								s);
					}
				}
			}
			this.fillinStyle(this.attributeValue("style"), new Specificity(1,
					0, 0, 0));
		}
		Block.PropertyValue i = this.css_.get(DomElement.cssName(property));
		if (i != null) {
			return i.value_;
		} else {
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
		PercentageOfFontSize, PercentageOfParentSize, IgnorePercentage;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	static class PropertyValue {
		private static Logger logger = LoggerFactory
				.getLogger(PropertyValue.class);

		public PropertyValue() {
			this.value_ = "";
			this.s_ = new Specificity();
		}

		public PropertyValue(String value, Specificity s) {
			this.value_ = value;
			this.s_ = s;
		}

		public String value_;
		public Specificity s_;
	}

	private net.n3.nanoxml.XMLElement node_;
	private Block parent_;
	private List<Block> offsetChildren_;
	private Block offsetParent_;
	private DomElementType type_;
	private Side float_;
	private boolean inline_;
	private List<Block> children_;
	private LayoutBox currentTheadBlock_;
	private double currentWidth_;
	private double contentsHeight_;
	private Map<String, Block.PropertyValue> css_;
	private StyleSheet styleSheet_;

	private int attributeValue(String attribute, int defaultValue) {
		String valueStr = this.attributeValue(attribute);
		if (valueStr.length() != 0) {
			return Integer.parseInt(valueStr);
		} else {
			return defaultValue;
		}
	}

	private void updateAggregateProperty(String property, String aggregate,
			Specificity spec, String value) {
		if (this.css_.get(property + aggregate) == null
				|| this.css_.get(property + aggregate).s_
						.isSmallerOrEqualThen(spec)) {
			this.css_.put(property + aggregate, new Block.PropertyValue(value,
					spec));
		}
	}

	private void fillinStyle(String style, Specificity specificity) {
		if (style.length() == 0) {
			return;
		}
		List<String> values = new ArrayList<String>();
		values = new ArrayList<String>(Arrays.asList(style.split(";")));
		for (int i = 0; i < values.size(); ++i) {
			List<String> namevalue = new ArrayList<String>();
			namevalue = new ArrayList<String>(Arrays.asList(values.get(i)
					.split(":")));
			if (namevalue.size() == 2) {
				String n = namevalue.get(0);
				String v = namevalue.get(1);
				n = n.trim();
				v = v.trim();
				this.updateAggregateProperty(n, "", specificity, v);
				if (isAggregate(n)) {
					List<String> allvalues = new ArrayList<String>();
					allvalues = new ArrayList<String>(Arrays.asList(v
							.split(" ")));
					int count = 0;
					for (int j = 0; j < allvalues.size(); ++j) {
						String vj = allvalues.get(j);
						if (vj.charAt(0) < '0' || vj.charAt(0) > '9') {
							break;
						}
						++count;
					}
					if (count == 0) {
						logger.error(new StringWriter().append(
								"Strange aggregate CSS length property: '")
								.append(v).append("'").toString());
					} else {
						if (count == 1) {
							String v0 = allvalues.get(0);
							this.updateAggregateProperty(n, "-top",
									specificity, v0);
							this.updateAggregateProperty(n, "-right",
									specificity, v0);
							this.updateAggregateProperty(n, "-bottom",
									specificity, v0);
							this.updateAggregateProperty(n, "-left",
									specificity, v0);
						} else {
							if (count == 2) {
								String v1 = allvalues.get(0);
								this.updateAggregateProperty(n, "-top",
										specificity, v1);
								this.updateAggregateProperty(n, "-bottom",
										specificity, v1);
								String v2 = allvalues.get(1);
								this.updateAggregateProperty(n, "-right",
										specificity, v2);
								this.updateAggregateProperty(n, "-left",
										specificity, v2);
							} else {
								if (count == 3) {
									String v1 = allvalues.get(0);
									this.updateAggregateProperty(n, "-top",
											specificity, v1);
									String v2 = allvalues.get(1);
									this.updateAggregateProperty(n, "-right",
											specificity, v2);
									this.updateAggregateProperty(n, "-left",
											specificity, v2);
									String v3 = allvalues.get(2);
									this.updateAggregateProperty(n, "-bottom",
											specificity, v3);
								} else {
									String v1 = allvalues.get(0);
									this.updateAggregateProperty(n, "-top",
											specificity, v1);
									String v2 = allvalues.get(1);
									this.updateAggregateProperty(n, "-right",
											specificity, v2);
									String v3 = allvalues.get(2);
									this.updateAggregateProperty(n, "-bottom",
											specificity, v3);
									String v4 = allvalues.get(3);
									this.updateAggregateProperty(n, "-left",
											specificity, v4);
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean isPositionedAbsolutely() {
		String pos = this.cssProperty(Property.PropertyStylePosition);
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
			result = this.cssDecodeLength(this
					.cssProperty(Property.PropertyStyleWidth), fontScale,
					result, Block.PercentageRule.PercentageOfParentSize, this
							.getCurrentParentWidth());
			if (this.type_ == DomElementType.DomElement_IMG
					|| this.type_ == DomElementType.DomElement_TABLE
					|| this.type_ == DomElementType.DomElement_TD
					|| this.type_ == DomElementType.DomElement_TH) {
				result = this.cssDecodeLength(this.attributeValue("width"),
						fontScale, result,
						Block.PercentageRule.PercentageOfParentSize, this
								.getCurrentParentWidth());
			}
		}
		return result;
	}

	private double cssHeight(double fontScale) {
		double result = -1;
		if (this.node_ != null) {
			result = this.cssDecodeLength(this
					.cssProperty(Property.PropertyStyleHeight), fontScale,
					result, Block.PercentageRule.IgnorePercentage);
			if (this.type_ == DomElementType.DomElement_IMG) {
				result = this.cssDecodeLength(this.attributeValue("height"),
						fontScale, result,
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
		if (this.type_ == DomElementType.DomElement_TD) {
			return 0;
		}
		try {
			result = this.cssLength(Property.PropertyStyleMarginTop, side,
					fontScale);
		} catch (RuntimeException e) {
		}
		if (!result.defined) {
			if (side == Side.Top || side == Side.Bottom) {
				if (this.type_ == DomElementType.DomElement_H4
						|| this.type_ == DomElementType.DomElement_P
						|| this.type_ == DomElementType.DomElement_FIELDSET
						|| this.type_ == DomElementType.DomElement_FORM) {
					return 1.12 * this.cssFontSize(fontScale);
				} else {
					if (this.type_ == DomElementType.DomElement_UL
							|| this.type_ == DomElementType.DomElement_OL) {
						if (!(this.isInside(DomElementType.DomElement_UL) || this
								.isInside(DomElementType.DomElement_OL))) {
							return 1.12 * this.cssFontSize(fontScale);
						} else {
							return 0;
						}
					} else {
						if (this.type_ == DomElementType.DomElement_H1) {
							return 0.67 * this.cssFontSize(fontScale);
						} else {
							if (this.type_ == DomElementType.DomElement_H2) {
								return 0.75 * this.cssFontSize(fontScale);
							} else {
								if (this.type_ == DomElementType.DomElement_H3) {
									return 0.83 * this.cssFontSize(fontScale);
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
		Block.CssLength result = this.cssLength(
				Property.PropertyStylePaddingTop, side, fontScale);
		if (!result.defined) {
			if (this.isTableCell()) {
				return 4;
			} else {
				if ((this.type_ == DomElementType.DomElement_UL || this.type_ == DomElementType.DomElement_OL)
						&& side == Side.Left) {
					return 40;
				}
			}
		}
		return result.length;
	}

	private double cssBorderWidth(Side side, double fontScale) {
		if (!(this.node_ != null)) {
			return 0;
		}
		int index = sideToIndex(side);
		Property property = Property.values()[Property.PropertyStyleBorderTop
				.getValue()
				+ index];
		String borderStr = this.cssProperty(property);
		double result = 0;
		if (borderStr.length() != 0) {
			List<String> values = new ArrayList<String>();
			values = new ArrayList<String>(Arrays.asList(borderStr.split(" ")));
			WLength l = new WLength(values.get(0));
			result = l.toPixels(this.cssFontSize(fontScale));
		}
		if (result == 0) {
			if (this.type_ == DomElementType.DomElement_TABLE) {
				result = this.attributeValue("border", 0);
			} else {
				if (this.isTableCell()) {
					Block table = this.parent_;
					while (table != null
							&& table.type_ != DomElementType.DomElement_TABLE) {
						table = table.parent_;
					}
					if (table != null) {
						result = table.attributeValue("border", 0) != 0 ? 1 : 0;
					}
				}
			}
		}
		return result;
	}

	private WColor cssBorderColor(Side side) {
		int index = sideToIndex(side);
		Property property = Property.values()[Property.PropertyStyleBorderTop
				.getValue()
				+ index];
		String borderStr = this.cssProperty(property);
		if (borderStr.length() != 0) {
			List<String> values = new ArrayList<String>();
			values = new ArrayList<String>(Arrays.asList(borderStr.split(" ")));
			if (values.size() > 2) {
				return new WColor(new WString(values.get(2)));
			}
		}
		return WColor.black;
	}

	private WColor getCssColor() {
		String color = this.inheritedCssProperty(Property.PropertyStyleColor);
		if (color.length() != 0) {
			return new WColor(new WString(color));
		} else {
			return WColor.black;
		}
	}

	private AlignmentFlag getCssTextAlign() {
		if (this.node_ != null && !this.isInline()) {
			String s = this.cssProperty(Property.PropertyStyleTextAlign);
			if (s.length() == 0
					&& this.type_ != DomElementType.DomElement_TABLE) {
				s = this.attributeValue("align");
			}
			if (s.length() == 0 || s.equals("inherit")) {
				if (this.type_ == DomElementType.DomElement_TH) {
					return AlignmentFlag.AlignCenter;
				} else {
					if (this.parent_ != null) {
						return this.parent_.getCssTextAlign();
					} else {
						return AlignmentFlag.AlignLeft;
					}
				}
			} else {
				if (s.equals("left")) {
					return AlignmentFlag.AlignLeft;
				} else {
					if (s.equals("center")) {
						return AlignmentFlag.AlignCenter;
					} else {
						if (s.equals("right")) {
							return AlignmentFlag.AlignRight;
						} else {
							if (s.equals("justify")) {
								return AlignmentFlag.AlignJustify;
							} else {
								unsupportedCssValue(
										Property.PropertyStyleTextAlign, s);
								return AlignmentFlag.AlignLeft;
							}
						}
					}
				}
			}
		} else {
			if (this.parent_ != null) {
				return this.parent_.getCssTextAlign();
			} else {
				return AlignmentFlag.AlignLeft;
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
		String v = this.cssProperty(Property.PropertyStyleLineHeight);
		if (v.length() != 0) {
			if (v.equals("normal")) {
				return fontLineHeight;
			} else {
				try {
					return Double.parseDouble(v);
				} catch (NumberFormatException e) {
					WLength l = new WLength(v);
					if (l.getUnit() == WLength.Unit.Percentage) {
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
		String v = this.cssProperty(Property.PropertyStyleFontSize);
		double Medium = 16;
		double parentSize = this.parent_ != null ? this.parent_.cssFontSize()
				: Medium;
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
												if (l.getUnit() == WLength.Unit.Percentage) {
													result = parentSize
															* l.getValue()
															/ 100;
												} else {
													if (l.getUnit() == WLength.Unit.FontEm) {
														result = parentSize
																* l.getValue();
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
			if (this.type_ == DomElementType.DomElement_H1) {
				result = parentSize * 2;
			} else {
				if (this.type_ == DomElementType.DomElement_H2) {
					result = parentSize * 1.5;
				} else {
					if (this.type_ == DomElementType.DomElement_H3) {
						result = parentSize * 1.17;
					} else {
						if (this.type_ == DomElementType.DomElement_H5) {
							result = parentSize * 0.83;
						} else {
							if (this.type_ == DomElementType.DomElement_H6) {
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
	private WFont.Style getCssFontStyle() {
		if (!(this.node_ != null) && this.parent_ != null) {
			return this.parent_.getCssFontStyle();
		}
		String v = this.cssProperty(Property.PropertyStyleFontStyle);
		if (v.length() == 0 && this.type_ == DomElementType.DomElement_EM) {
			return WFont.Style.Italic;
		} else {
			if (v.equals("normal")) {
				return WFont.Style.NormalStyle;
			} else {
				if (v.equals("italic")) {
					return WFont.Style.Italic;
				} else {
					if (v.equals("oblique")) {
						return WFont.Style.Oblique;
					} else {
						if (this.parent_ != null) {
							return this.parent_.getCssFontStyle();
						} else {
							return WFont.Style.NormalStyle;
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
		String v = this.cssProperty(Property.PropertyStyleFontWeight);
		if (v.length() == 0
				&& (this.type_ == DomElementType.DomElement_B
						|| this.type_ == DomElementType.DomElement_STRONG
						|| this.type_ == DomElementType.DomElement_TH || this.type_
						.getValue() >= DomElementType.DomElement_H1.getValue()
						&& this.type_.getValue() <= DomElementType.DomElement_H6
								.getValue())) {
			v = "bolder";
		}
		if (v.length() != 0) {
			try {
				return Integer.parseInt(v);
			} catch (NumberFormatException blc) {
				if (v.equals("normal")) {
					return 400;
				} else {
					if (v.equals("bold")) {
						return 700;
					}
				}
			}
		}
		int parentWeight = this.parent_ != null ? this.parent_
				.getCssFontWeight() : 400;
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
		WFont.GenericFamily genericFamily = WFont.GenericFamily.SansSerif;
		WString specificFamilies = new WString();
		String family = this
				.inheritedCssProperty(Property.PropertyStyleFontFamily);
		if (family.length() != 0) {
			List<String> values = new ArrayList<String>();
			values = new ArrayList<String>(Arrays.asList(family.split(",")));
			for (int i = 0; i < values.size(); ++i) {
				String name = values.get(i);
				name = name.trim();
				name = Utils.strip(name, "'\"");
				name = name.toLowerCase();
				if (name.equals("sans-serif")) {
					genericFamily = WFont.GenericFamily.SansSerif;
				} else {
					if (name.equals("serif")) {
						genericFamily = WFont.GenericFamily.Serif;
					} else {
						if (name.equals("cursive")) {
							genericFamily = WFont.GenericFamily.Cursive;
						} else {
							if (name.equals("fantasy")) {
								genericFamily = WFont.GenericFamily.Fantasy;
							} else {
								if (name.equals("monospace")) {
									genericFamily = WFont.GenericFamily.Monospace;
								} else {
									if (name.equals("times")
											|| name.equals("palatino")) {
										genericFamily = WFont.GenericFamily.Serif;
									} else {
										if (name.equals("arial")
												|| name.equals("helvetica")) {
											genericFamily = WFont.GenericFamily.SansSerif;
										} else {
											if (name.equals("courier")) {
												genericFamily = WFont.GenericFamily.Monospace;
											} else {
												if (name.equals("symbol")) {
													genericFamily = WFont.GenericFamily.Fantasy;
												} else {
													if (name
															.equals("zapf dingbats")) {
														genericFamily = WFont.GenericFamily.Cursive;
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
		WFont result = new WFont();
		result.setFamily(genericFamily, specificFamilies);
		result.setSize(WFont.Size.FixedSize, new WLength(this
				.cssFontSize(fontScale), WLength.Unit.Pixel));
		result.setWeight(WFont.Weight.Value, this.getCssFontWeight());
		result.setStyle(this.getCssFontStyle());
		return result;
	}

	private String getCssTextDecoration() {
		String v = this.cssProperty(Property.PropertyStyleTextDecoration);
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

	private double cssDecodeLength(String length, double fontScale,
			double defaultValue, Block.PercentageRule percentage,
			double parentSize) {
		if (length.length() != 0) {
			WLength l = new WLength(length);
			if (l.getUnit() == WLength.Unit.Percentage) {
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

	private final double cssDecodeLength(String length, double fontScale,
			double defaultValue) {
		return cssDecodeLength(length, fontScale, defaultValue,
				Block.PercentageRule.PercentageOfFontSize, 0);
	}

	private final double cssDecodeLength(String length, double fontScale,
			double defaultValue, Block.PercentageRule percentage) {
		return cssDecodeLength(length, fontScale, defaultValue, percentage, 0);
	}

	private static boolean isPercentageLength(String length) {
		return length.length() != 0
				&& new WLength(length).getUnit() == WLength.Unit.Percentage;
	}

	private double getCurrentParentWidth() {
		if (this.parent_ != null) {
			switch (this.parent_.type_) {
			case DomElement_TR:
			case DomElement_TBODY:
			case DomElement_THEAD:
			case DomElement_TFOOT:
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

	private void pageBreak(PageState ps) {
		clearFloats(ps);
		++ps.page;
		ps.y = 0;
	}

	private void inlinePageBreak(String pageBreak, Line line,
			List<Block> floats, double minX, double maxX, WTextRenderer renderer) {
		if (pageBreak.equals("always")) {
			if (this.inlineLayout.isEmpty()) {
				this.inlineLayout.add(new InlineBox());
				InlineBox b = this.inlineLayout
						.get(this.inlineLayout.size() - 1);
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

	private double layoutInline(Line line, List<Block> floats, double minX,
			double maxX, boolean canIncreaseWidth, WTextRenderer renderer) {
		this.inlineLayout.clear();
		this.inlinePageBreak(this
				.cssProperty(Property.PropertyStylePageBreakBefore), line,
				floats, minX, maxX, renderer);
		if (this.isText() || this.type_ == DomElementType.DomElement_IMG
				|| this.type_ == DomElementType.DomElement_BR) {
			String s = "";
			int utf8Pos = 0;
			int utf8Count = 0;
			double whitespaceWidth = 0;
			renderer.getPainter()
					.setFont(this.cssFont(renderer.getFontScale()));
			WPaintDevice device = renderer.getPainter().getDevice();
			WFontMetrics metrics = device.getFontMetrics();
			double lineHeight = this.cssLineHeight(metrics.getHeight(),
					renderer.getFontScale());
			double fontHeight = metrics.getSize();
			double baseline = (lineHeight - fontHeight) / 2.0
					+ metrics.getAscent();
			if (this.isText()) {
				s = this.getText();
				whitespaceWidth = device.measureText(new WString(" "))
						.getWidth();
			}
			for (;;) {
				Range rangeX = new Range(minX, maxX);
				adjustAvailableWidth(line.getY(), line.getPage(), floats,
						rangeX);
				if (rangeX.start > line.getX()) {
					line.setX(rangeX.start);
				}
				double w = 0;
				double h = 0;
				boolean lineBreak = false;
				if (this.isText()) {
					if (utf8Pos < s.length() && line.getX() == rangeX.start
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
						WTextItem item = renderer.getPainter().getDevice()
								.measureText(text, maxWidth, true);
						utf8Count = item.getText().toString().length();
						w = item.getWidth();
						if (utf8Count > 0
								&& utf8Pos + utf8Count < s.length()
								&& isWhitespace(s.charAt(utf8Pos + utf8Count
										- 1))) {
							w += whitespaceWidth;
							rangeX.end += whitespaceWidth;
						}
						if (canIncreaseWidth
								&& isEpsilonMore(item.getWidth(), rangeX.end
										- line.getX())) {
							maxX += w - (rangeX.end - line.getX());
							rangeX.end += w - (rangeX.end - line.getX());
						}
						if (w == 0) {
							lineBreak = true;
							if (line.getX() == rangeX.start) {
								if (item.getNextWidth() < 0) {
									for (int i = utf8Pos; i <= s.length(); ++i) {
										if (i == s.length()
												|| isWhitespace(s.charAt(i))) {
											WString word = new WString(s
													.substring(utf8Pos, utf8Pos
															+ i - utf8Pos));
											double wordWidth = device
													.measureText(word)
													.getWidth();
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
								throw new WException(
										"Internal error: utf8Count <= 0!");
							}
							h = fontHeight;
						}
					} else {
						break;
					}
				} else {
					if (this.type_ == DomElementType.DomElement_BR) {
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
							WPainter.Image image = new WPainter.Image(src, src);
							if (w <= 0) {
								w = image.getWidth();
							}
							if (h <= 0) {
								h = image.getHeight();
							}
						}
						w += this.cssBoxMargin(Side.Left, renderer
								.getFontScale())
								+ this.cssBoxMargin(Side.Right, renderer
										.getFontScale());
						h += this.cssBoxMargin(Side.Top, renderer
								.getFontScale())
								+ this.cssBoxMargin(Side.Bottom, renderer
										.getFontScale());
						String va = this
								.cssProperty(Property.PropertyStyleVerticalAlign);
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
					line
							.setLineBreak(this.type_ == DomElementType.DomElement_BR);
					line.finish(this.getCssTextAlign(), floats, minX, maxX,
							renderer);
					if (w == 0 || line.getX() > rangeX.start) {
						if (w > 0 && canIncreaseWidth) {
							maxX += w - (maxX - line.getX());
							rangeX.end += w - (maxX - line.getX());
						}
						line.newLine(minX, line.getY() + line.getHeight(), line
								.getPage());
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
					InlineBox b = this.inlineLayout.get(this.inlineLayout
							.size() - 1);
					double marginLeft = 0;
					double marginRight = 0;
					double marginBottom = 0;
					double marginTop = 0;
					if (this.type_ == DomElementType.DomElement_IMG) {
						marginLeft = this.cssMargin(Side.Left, renderer
								.getFontScale());
						marginRight = this.cssMargin(Side.Right, renderer
								.getFontScale());
						marginBottom = this.cssMargin(Side.Bottom, renderer
								.getFontScale());
						marginTop = this.cssMargin(Side.Top, renderer
								.getFontScale());
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
					if (!this.isText() || this.isText()
							&& utf8Pos == s.length()) {
						break;
					}
				}
			}
		}
		if (this.isInlineChildren()) {
			if (this.type_ == DomElementType.DomElement_LI) {
				this.inlineLayout.get(0).x = MARGINX;
				line.setX(minX);
			}
			if (!this.children_.isEmpty()) {
				for (int i = 0; i < this.children_.size(); ++i) {
					Block c = this.children_.get(i);
					if (c.isFloat()) {
						maxX = c.layoutFloat(line.getY(), line.getPage(),
								floats, line.getX(), line.getHeight(), minX,
								maxX, canIncreaseWidth, renderer);
						line.reflow(c);
						line.addBlock(c);
					} else {
						if (c.isPositionedAbsolutely()) {
							if (!(c.offsetParent_ != null)) {
								c.setOffsetParent();
							}
							c.inlineLayout.clear();
							c.inlineLayout.add(new InlineBox());
							InlineBox box = c.inlineLayout.get(c.inlineLayout
									.size() - 1);
							box.page = line.getPage();
							box.x = line.getX();
							box.y = line.getY();
							box.width = 0;
							box.height = 0;
						} else {
							maxX = c.layoutInline(line, floats, minX, maxX,
									canIncreaseWidth, renderer);
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
		this.inlinePageBreak(this
				.cssProperty(Property.PropertyStylePageBreakAfter), line,
				floats, minX, maxX, renderer);
		return maxX;
	}

	private void layoutTable(PageState ps, boolean canIncreaseWidth,
			WTextRenderer renderer, double cssSetWidth) {
		this.currentWidth_ = Math.max(0.0, cssSetWidth);
		List<Double> minimumColumnWidths = new ArrayList<Double>();
		List<Double> maximumColumnWidths = new ArrayList<Double>();
		for (int i = 0; i < this.children_.size(); ++i) {
			Block c = this.children_.get(i);
			c.tableComputeColumnWidths(minimumColumnWidths,
					maximumColumnWidths, renderer, this);
		}
		this.currentWidth_ = 0;
		int colCount = minimumColumnWidths.size();
		int cellSpacing = this.attributeValue("cellspacing", 2);
		double totalSpacing = (colCount + 1) * cellSpacing;
		double totalMinWidth = sum(minimumColumnWidths) + totalSpacing;
		double totalMaxWidth = sum(maximumColumnWidths) + totalSpacing;
		double desiredMinWidth = Math.max(totalMinWidth, cssSetWidth);
		double desiredMaxWidth = totalMaxWidth;
		if (cssSetWidth > 0 && cssSetWidth < desiredMaxWidth) {
			desiredMaxWidth = Math.max(desiredMinWidth, cssSetWidth);
		}
		double availableWidth;
		for (;;) {
			Range rangeX = new Range(ps.minX, ps.maxX);
			adjustAvailableWidth(ps.y, ps.page, ps.floats, rangeX);
			ps.maxX = rangeX.end;
			double border = this.cssBorderWidth(Side.Left, renderer
					.getFontScale())
					+ this.cssBorderWidth(Side.Right, renderer.getFontScale());
			availableWidth = rangeX.end - rangeX.start - border;
			if (canIncreaseWidth
					&& isEpsilonLess(availableWidth, desiredMaxWidth)) {
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
			double factor = width / totalMaxWidth;
			for (int i = 0; i < widths.size(); ++i) {
				widths.set(i, widths.get(i) * factor);
			}
		} else {
			if (width > totalMinWidth) {
				double totalStretch = 0;
				for (int i = 0; i < widths.size(); ++i) {
					totalStretch += maximumColumnWidths.get(i)
							- minimumColumnWidths.get(i);
				}
				double room = width - totalMinWidth;
				double factor = room / totalStretch;
				for (int i = 0; i < widths.size(); ++i) {
					double stretch = maximumColumnWidths.get(i)
							- minimumColumnWidths.get(i);
					widths.set(i, widths.get(i) + factor * stretch);
				}
			}
		}
		AlignmentFlag hAlign = this.getHorizontalAlignment();
		switch (hAlign) {
		case AlignLeft:
		case AlignJustify:
			ps.maxX = ps.minX + width;
			break;
		case AlignCenter:
			ps.minX = ps.minX + (ps.maxX - ps.minX - width) / 2;
			ps.maxX = ps.minX + width;
			break;
		case AlignRight:
			ps.minX = ps.maxX - width;
			break;
		default:
			break;
		}
		Block repeatHead = null;
		for (int i = 0; i < this.children_.size(); ++i) {
			if (this.children_.get(i).type_ == DomElementType.DomElement_THEAD) {
				repeatHead = this.children_.get(i);
			} else {
				if (this.children_.get(i).type_ != DomElementType.DomElement_UNKNOWN) {
					break;
				}
			}
		}
		boolean protectRows = repeatHead != null;
		this.tableDoLayout(ps.minX, ps, cellSpacing, widths, protectRows,
				repeatHead, renderer);
		ps.minX -= this.cssBorderWidth(Side.Left, renderer.getFontScale());
		ps.maxX += this.cssBorderWidth(Side.Right, renderer.getFontScale());
		ps.y += cellSpacing;
	}

	double layoutFloat(double y, int page, List<Block> floats, double lineX,
			double lineHeight, double minX, double maxX,
			boolean canIncreaseWidth, WTextRenderer renderer) {
		if (floats.indexOf(this) != -1) {
			return maxX;
		}
		double blockCssWidth = this.cssWidth(renderer.getFontScale());
		double currentWidth = Math.max(0.0, blockCssWidth)
				+ this.cssBoxMargin(Side.Left, renderer.getFontScale())
				+ this.cssBoxMargin(Side.Right, renderer.getFontScale());
		PageState floatPs = new PageState();
		Utils.copyList(floats, floatPs.floats);
		for (;;) {
			floatPs.page = page;
			floatPs.y = y;
			floatPs.minX = minX;
			floatPs.maxX = maxX;
			double floatX = positionFloat(lineX, floatPs, lineHeight,
					currentWidth, canIncreaseWidth, renderer, this
							.getFloatSide());
			if (floatPs.maxX > maxX) {
				return floatPs.maxX;
			}
			List<Block> innerFloats = new ArrayList<Block>();
			boolean unknownWidth = blockCssWidth < 0
					&& isEpsilonLess(currentWidth, maxX - minX);
			double collapseMarginBottom = 0;
			floatPs.minX = floatX;
			floatPs.maxX = floatX + currentWidth;
			collapseMarginBottom = this.layoutBlock(floatPs, unknownWidth
					|| canIncreaseWidth, renderer, 0, collapseMarginBottom);
			double pw = floatPs.maxX - (floatPs.minX + currentWidth);
			if (pw > 0) {
				if (blockCssWidth < 0) {
					currentWidth = Math.min(maxX - minX, currentWidth + pw);
					continue;
				} else {
					if (!canIncreaseWidth) {
						throw new WException(
								"Internal error: !canIncreaseWidth");
					}
					return maxX + pw;
				}
			}
			floats.add(this);
			return maxX;
		}
	}

	private void layoutAbsolute(WTextRenderer renderer) {
		LayoutBox staticLayout = this.getLayoutTotal();
		LayoutBox containingLayout = this.offsetParent_.getLayoutTotal();
		boolean leftAuto = isOffsetAuto(this
				.cssProperty(Property.PropertyStyleLeft));
		boolean widthAuto = isOffsetAuto(this
				.cssProperty(Property.PropertyStyleWidth));
		boolean rightAuto = isOffsetAuto(this
				.cssProperty(Property.PropertyStyleRight));
		double staticLeft = staticLayout.x - containingLayout.x;
		PageState ps = new PageState();
		this.layoutBlock(ps, false, renderer, 0, 0);
		double preferredMinWidth = ps.maxX;
		ps = new PageState();
		this.layoutBlock(ps, true, renderer, 0, 0);
		double preferredWidth = ps.maxX;
		double availableWidth = containingLayout.width;
		double shrinkToFitWidth = Math.min(Math.max(preferredMinWidth,
				availableWidth), preferredWidth);
		double left = 0;
		double width = 0;
		double right = 0;
		if (!leftAuto) {
			left = this.cssDecodeLength(this
					.cssProperty(Property.PropertyStyleLeft), renderer
					.getFontScale(), 0,
					Block.PercentageRule.PercentageOfParentSize,
					containingLayout.width);
		}
		if (!rightAuto) {
			right = this.cssDecodeLength(this
					.cssProperty(Property.PropertyStyleRight), renderer
					.getFontScale(), 0,
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
									width = Math.max(0.0,
											containingLayout.width - left
													- right);
								} else {
									if (!leftAuto && !widthAuto && rightAuto) {
									}
								}
							}
						}
					}
				}
			}
		}
		double staticTop = staticLayout.y - containingLayout.y;
		staticTop += (staticLayout.page - containingLayout.page)
				* renderer.textHeight(containingLayout.page);
		boolean topAuto = isOffsetAuto(this
				.cssProperty(Property.PropertyStyleTop));
		boolean heightAuto = isOffsetAuto(this
				.cssProperty(Property.PropertyStyleHeight));
		boolean bottomAuto = isOffsetAuto(this
				.cssProperty(Property.PropertyStyleBottom));
		double top = 0;
		double height = 0;
		double bottom = 0;
		if (!topAuto) {
			top = this.cssDecodeLength(this
					.cssProperty(Property.PropertyStyleTop), renderer
					.getFontScale(), 0,
					Block.PercentageRule.PercentageOfParentSize,
					containingLayout.height);
		}
		if (!bottomAuto) {
			right = this.cssDecodeLength(this
					.cssProperty(Property.PropertyStyleBottom), renderer
					.getFontScale(), 0,
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
									height = containingLayout.height - top
											- bottom;
								} else {
									if (!topAuto && !heightAuto && bottomAuto) {
									}
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

	private void tableDoLayout(double x, PageState ps, int cellSpacing,
			List<Double> widths, boolean protectRows, Block repeatHead,
			WTextRenderer renderer) {
		if (this.type_ == DomElementType.DomElement_TABLE
				|| this.type_ == DomElementType.DomElement_TBODY
				|| this.type_ == DomElementType.DomElement_THEAD
				|| this.type_ == DomElementType.DomElement_TFOOT) {
			for (int i = 0; i < this.children_.size(); ++i) {
				Block c = this.children_.get(i);
				c
						.tableDoLayout(
								x,
								ps,
								cellSpacing,
								widths,
								protectRows,
								this.type_ != DomElementType.DomElement_THEAD ? repeatHead
										: null, renderer);
			}
			if (repeatHead != null
					&& this.type_ == DomElementType.DomElement_THEAD) {
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
			if (this.type_ == DomElementType.DomElement_TR) {
				double startY = ps.y;
				int startPage = ps.page;
				this.tableRowDoLayout(x, ps, cellSpacing, widths, renderer, -1);
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
					this.tableRowDoLayout(x, ps, cellSpacing, widths, renderer,
							-1);
				}
				double rowHeight = (ps.page - startPage)
						* renderer.textHeight(ps.page) + (ps.y - startY)
						- cellSpacing;
				ps.y = startY;
				ps.page = startPage;
				this.tableRowDoLayout(x, ps, cellSpacing, widths, renderer,
						rowHeight);
			}
		}
	}

	private void tableRowDoLayout(double x, PageState ps, int cellSpacing,
			List<Double> widths, WTextRenderer renderer, double rowHeight) {
		double endY = ps.y;
		int endPage = ps.page;
		int col = 0;
		x += cellSpacing;
		for (int i = 0; i < this.children_.size(); ++i) {
			Block c = this.children_.get(i);
			if (c.isTableCell()) {
				int colSpan = c.attributeValue("colspan", 1);
				double width = 0;
				for (int j = col; j < col + colSpan; ++j) {
					width += widths.get(j);
				}
				width += (colSpan - 1) * cellSpacing;
				PageState cellPs = new PageState();
				cellPs.y = ps.y + cellSpacing;
				cellPs.page = ps.page;
				cellPs.minX = x;
				cellPs.maxX = x + width;
				double collapseMarginBottom = 0;
				double collapseMarginTop = Double.MAX_VALUE;
				collapseMarginBottom = c.layoutBlock(cellPs, false, renderer,
						collapseMarginTop, collapseMarginBottom, rowHeight);
				if (collapseMarginBottom < collapseMarginTop) {
					cellPs.y -= collapseMarginBottom;
				}
				cellPs.minX = x;
				cellPs.maxX = x + width;
				Block.clearFloats(cellPs, width);
				if (cellPs.page > endPage || cellPs.page == endPage
						&& cellPs.y > endY) {
					endPage = cellPs.page;
					endY = cellPs.y;
				}
				col += colSpan;
				x += width + cellSpacing;
			}
		}
		ps.y = endY;
		ps.page = endPage;
	}

	private void tableComputeColumnWidths(List<Double> minima,
			List<Double> maxima, WTextRenderer renderer, Block table) {
		if (this.type_ == DomElementType.DomElement_TBODY
				|| this.type_ == DomElementType.DomElement_THEAD
				|| this.type_ == DomElementType.DomElement_TFOOT) {
			for (int i = 0; i < this.children_.size(); ++i) {
				Block c = this.children_.get(i);
				c.tableComputeColumnWidths(minima, maxima, renderer, table);
			}
		} else {
			if (this.type_ == DomElementType.DomElement_TR) {
				int col = 0;
				for (int i = 0; i < this.children_.size(); ++i) {
					Block c = this.children_.get(i);
					if (c.isTableCell()) {
						c.cellComputeColumnWidths(col, false, minima, renderer,
								table);
						col = c.cellComputeColumnWidths(col, true, maxima,
								renderer, table);
					}
				}
			}
		}
	}

	private int cellComputeColumnWidths(int col, boolean maximum,
			List<Double> values, WTextRenderer renderer, Block table) {
		double currentWidth = 0;
		int colSpan = this.attributeValue("colspan", 1);
		while (col + colSpan > (int) values.size()) {
			values.add(0.0);
		}
		for (int i = 0; i < colSpan; ++i) {
			currentWidth += values.get(col + i);
		}
		double width = currentWidth;
		PageState ps = new PageState();
		ps.y = 0;
		ps.page = 0;
		ps.minX = 0;
		ps.maxX = width;
		double origTableWidth = table.currentWidth_;
		if (!maximum) {
			table.currentWidth_ = 0;
		}
		this.layoutBlock(ps, maximum, renderer, 0, 0);
		table.currentWidth_ = origTableWidth;
		width = ps.maxX;
		if (width > currentWidth) {
			double extraPerColumn = (width - currentWidth) / colSpan;
			for (int i = 0; i < colSpan; ++i) {
				values.set(col + i, values.get(col + i) + extraPerColumn);
			}
		}
		return col + colSpan;
	}

	private void setOffsetParent() {
		this.offsetParent_ = this.getFindOffsetParent();
		this.offsetParent_.offsetChildren_.add(this);
	}

	private Block getFindOffsetParent() {
		if (this.parent_ != null) {
			String pos = this.parent_
					.cssProperty(Property.PropertyStylePosition);
			if (pos.equals("absolute") || pos.equals("fixed")
					|| pos.equals("relative")) {
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
			LayoutBox first = this.blockLayout.get(0);
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

	private LayoutBox toBorderBox(LayoutBox bb, double fontScale) {
		LayoutBox result = bb;
		if (this.isFloat()) {
			result.x += this.cssMargin(Side.Left, fontScale);
			result.y += this.cssMargin(Side.Top, fontScale);
			result.width -= this.cssMargin(Side.Left, fontScale)
					+ this.cssMargin(Side.Right, fontScale);
			result.height -= this.cssMargin(Side.Top, fontScale)
					+ this.cssMargin(Side.Bottom, fontScale);
		}
		return result;
	}

	private double maxLayoutY(int page) {
		double result = 0;
		for (int i = 0; i < this.inlineLayout.size(); ++i) {
			InlineBox ib = this.inlineLayout.get(i);
			if (page == -1 || ib.page == page) {
				result = Math.max(result, ib.y + ib.height);
			}
		}
		for (int i = 0; i < this.blockLayout.size(); ++i) {
			BlockBox lb = this.blockLayout.get(i);
			if (page == -1 || lb.page == page) {
				result = Math.max(result, lb.y + lb.height);
			}
		}
		if (this.inlineLayout.isEmpty() && this.blockLayout.isEmpty()) {
			for (int i = 0; i < this.children_.size(); ++i) {
				result = Math.max(result, this.children_.get(i)
						.maxLayoutY(page));
			}
		}
		return result;
	}

	private double minLayoutY(int page) {
		double result = 1E9;
		for (int i = 0; i < this.inlineLayout.size(); ++i) {
			InlineBox ib = this.inlineLayout.get(i);
			if (page == -1 || ib.page == page) {
				result = Math.min(result, ib.y);
			}
		}
		for (int i = 0; i < this.blockLayout.size(); ++i) {
			BlockBox lb = this.blockLayout.get(i);
			if (page == -1 || lb.page == page) {
				result = Math.min(result, lb.y);
			}
		}
		if (this.inlineLayout.isEmpty() && this.blockLayout.isEmpty()) {
			for (int i = 0; i < this.children_.size(); ++i) {
				result = Math.min(result, this.children_.get(i)
						.minLayoutY(page));
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

	private void reLayout(LayoutBox from, LayoutBox to) {
		for (int i = 0; i < this.inlineLayout.size(); ++i) {
			InlineBox ib = this.inlineLayout.get(i);
			ib.page = to.page;
			ib.x += to.x - from.x;
			ib.y += to.y - from.y;
		}
		for (int i = 0; i < this.blockLayout.size(); ++i) {
			BlockBox bb = this.blockLayout.get(i);
			bb.page = to.page;
			bb.x += to.x - from.x;
			bb.y += to.y - from.y;
		}
		for (int i = 0; i < this.children_.size(); ++i) {
			this.children_.get(i).reLayout(from, to);
		}
	}

	private void renderText(String text, WTextRenderer renderer,
			WPainter painter, int page) {
		WPaintDevice device = painter.getDevice();
		painter.setFont(this.cssFont(renderer.getFontScale()));
		WFontMetrics metrics = device.getFontMetrics();
		double lineHeight = this.cssLineHeight(metrics.getHeight(), renderer
				.getFontScale());
		double fontHeight = metrics.getSize();
		String decoration = this.getCssTextDecoration();
		for (int i = 0; i < this.inlineLayout.size(); ++i) {
			InlineBox ib = this.inlineLayout.get(i);
			if (ib.page == page) {
				double y = renderer.getMargin(Side.Top) + ib.y
						- metrics.getLeading() + (lineHeight - fontHeight)
						/ 2.0;
				WRectF rect = new WRectF(renderer.getMargin(Side.Left) + ib.x,
						y, ib.width, ib.height);
				painter.setPen(new WPen(this.getCssColor()));
				if (ib.whitespaceWidth == device.measureText(" ").getWidth()) {
					WString t = new WString(text.substring(ib.utf8Pos,
							ib.utf8Pos + ib.utf8Count));
					painter
							.drawText(new WRectF(rect.getX(), rect.getY(), rect
									.getWidth(), rect.getHeight()
									+ metrics.getLeading()), EnumSet.of(
									AlignmentFlag.AlignLeft,
									AlignmentFlag.AlignTop), t);
				} else {
					double x = rect.getLeft();
					int wordStart = 0;
					double wordTotal = 0;
					for (int j = 0; j <= ib.utf8Count; ++j) {
						if (j == ib.utf8Count
								|| isWhitespace(text.charAt(ib.utf8Pos + j))) {
							if (j > wordStart) {
								WString word = new WString(text.substring(
										ib.utf8Pos + wordStart, ib.utf8Pos
												+ wordStart + j - wordStart));
								double wordWidth = device.measureText(word)
										.getWidth();
								wordTotal += wordWidth;
								painter.drawText(new WRectF(x, rect.getTop(),
										wordWidth, rect.getHeight()), EnumSet
										.of(AlignmentFlag.AlignLeft,
												AlignmentFlag.AlignTop), word);
								x += wordWidth;
							}
							x += ib.whitespaceWidth;
							wordStart = j + 1;
						}
					}
				}
				if (decoration.equals("underline")) {
					double below = y + metrics.getLeading()
							+ metrics.getAscent() + 2;
					painter.drawLine(rect.getLeft(), below, rect.getRight(),
							below);
				} else {
					if (decoration.equals("overline")) {
						double over = renderer.getMargin(Side.Top) + ib.y + 2;
						painter.drawLine(rect.getLeft(), over, rect.getRight(),
								over);
					} else {
						if (decoration.equals("line-through")) {
							double through = y + metrics.getLeading()
									+ metrics.getAscent() - 3;
							painter.drawLine(rect.getLeft(), through, rect
									.getRight(), through);
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

	private void renderBorders(LayoutBox bb, WTextRenderer renderer,
			WPainter painter, EnumSet<Side> verticals) {
		if (!(this.node_ != null)) {
			return;
		}
		double left = renderer.getMargin(Side.Left) + bb.x;
		double top = renderer.getMargin(Side.Top) + bb.y;
		double right = left + bb.width;
		double bottom = top + bb.height;
		double[] borderWidth = new double[4];
		WColor[] borderColor = new WColor[4];
		Side[] sides = { Side.Top, Side.Right, Side.Bottom, Side.Left };
		for (int i = 0; i < 4; ++i) {
			borderWidth[i] = this.cssBorderWidth(sides[i], renderer
					.getFontScale());
			borderColor[i] = this.cssBorderColor(sides[i]);
		}
		WPen borderPen = new WPen();
		borderPen.setCapStyle(PenCapStyle.FlatCap);
		for (int i = 0; i < 4; ++i) {
			if (borderWidth[i] != 0) {
				borderPen.setWidth(new WLength(borderWidth[i]));
				borderPen.setColor(borderColor[i]);
				painter.setPen(borderPen);
				switch (sides[i]) {
				case Top:
					if (!EnumUtils.mask(verticals, Side.Top).isEmpty()) {
						painter.drawLine(left, top + borderWidth[0] / 2, right,
								top + borderWidth[0] / 2);
					}
					break;
				case Right:
					painter.drawLine(right - borderWidth[1] / 2, top, right
							- borderWidth[1] / 2, bottom);
					break;
				case Bottom:
					if (!EnumUtils.mask(verticals, Side.Bottom).isEmpty()) {
						painter.drawLine(left, bottom - borderWidth[2] / 2,
								right, bottom - borderWidth[2] / 2);
					}
					break;
				case Left:
					painter.drawLine(left + borderWidth[3] / 2, top, left
							+ borderWidth[3] / 2, bottom);
					break;
				default:
					break;
				}
			}
		}
	}

	private final void renderBorders(LayoutBox bb, WTextRenderer renderer,
			WPainter painter, Side vertical, Side... verticals) {
		renderBorders(bb, renderer, painter, EnumSet.of(vertical, verticals));
	}

	private WString getGenerateItem() {
		boolean numbered = this.parent_ != null
				&& this.parent_.type_ == DomElementType.DomElement_OL;
		if (numbered) {
			int counter = 0;
			for (int i = 0; i < this.parent_.children_.size(); ++i) {
				Block child = this.parent_.children_.get(i);
				if (child.type_ == DomElementType.DomElement_LI) {
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

	private static void advance(PageState ps, double height,
			WTextRenderer renderer) {
		while (ps.y + height > renderer.textHeight(ps.page)) {
			++ps.page;
			ps.y = 0;
			height -= renderer.textHeight(ps.page) - ps.y;
			if (height < 0) {
				height = 0;
			}
			if (renderer.textHeight(ps.page) - ps.y < 0 && height >= 0) {
				throw new WException("The margin is to large");
			}
		}
		ps.y += height;
	}

	private static double diff(double y, int page, double startY,
			int startPage, WTextRenderer renderer) {
		double result = y - startY;
		while (page > startPage) {
			result += renderer.textHeight(page);
			--page;
		}
		return result;
	}

	private static double positionFloat(double x, PageState ps,
			double lineHeight, double width, boolean canIncreaseWidth,
			WTextRenderer renderer, Side floatSide) {
		if (!ps.floats.isEmpty()) {
			double minY = ps.floats.get(ps.floats.size() - 1).blockLayout
					.get(0).y;
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
		for (;;) {
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
		if (floatSide == Side.Left) {
			x = rangeX.start;
		} else {
			x = rangeX.end - width;
		}
		return x;
	}

	private static void unsupportedAttributeValue(String attribute, String value) {
		logger.error(new StringWriter().append("unsupported value '").append(
				value).append("' for attribute ").append(attribute).toString());
	}

	private static void unsupportedCssValue(Property property, String value) {
		logger.error(new StringWriter().append("unsupported value '").append(
				value).append("'for CSS style property ").append(
				DomElement.cssName(property)).toString());
	}

	private static boolean isAggregate(String cssProperty) {
		return cssProperty.equals("margin") || cssProperty.equals("border")
				|| cssProperty.equals("padding");
	}

	private boolean isTableCell() {
		return this.type_ == DomElementType.DomElement_TD
				|| this.type_ == DomElementType.DomElement_TH;
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
			throw new WException("Unexpected side: " + String.valueOf(side));
		}
	}

	static double sum(List<Double> v) {
		double result = 0;
		for (int i = 0; i < v.size(); ++i) {
			result += v.get(i);
		}
		return result;
	}

	static boolean isOffsetAuto(String s) {
		return s.length() == 0 || s.equals("auto");
	}
}
