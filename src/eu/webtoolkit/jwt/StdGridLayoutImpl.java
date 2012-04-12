/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

class StdGridLayoutImpl extends StdLayoutImpl {
	private static Logger logger = LoggerFactory
			.getLogger(StdGridLayoutImpl.class);

	public StdGridLayoutImpl(WLayout layout, Grid grid) {
		super(layout);
		this.grid_ = grid;
		this.useFixedLayout_ = false;
		this.forceUpdate_ = false;
		String THIS_JS = "js/StdGridLayoutImpl.js";
		WApplication app = WApplication.getInstance();
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.getStyleSheet().addRule("table.Wt-hcenter",
					"margin: 0px auto;position: relative");
			app.loadJavaScript(THIS_JS, wtjs1());
			app.loadJavaScript(THIS_JS, appjs1());
			app.addAutoJavaScript(app.getJavaScriptClass()
					+ ".layouts.adjust();");
		}
	}

	public int getMinimumHeight() {
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		int total = 0;
		for (int i = 0; i < rowCount; ++i) {
			int minHeight = 0;
			for (int j = 0; j < colCount; ++j) {
				WLayoutItem item = this.grid_.items_.get(i).get(j).item_;
				if (item != null) {
					minHeight = Math.max(minHeight, getImpl(item)
							.getMinimumHeight());
				}
			}
			total += minHeight;
		}
		return total * (rowCount - 1) * this.grid_.verticalSpacing_;
	}

	public DomElement createDomElement(boolean fitWidth, boolean fitHeight,
			WApplication app) {
		this.forceUpdate_ = false;
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		boolean hasResizeHandles = false;
		for (int i = 0; i < colCount; ++i) {
			if (this.grid_.columns_.get(i).resizable_) {
				hasResizeHandles = true;
				break;
			}
		}
		if (!hasResizeHandles) {
			for (int i = 0; i < rowCount; ++i) {
				if (this.grid_.rows_.get(i).resizable_) {
					hasResizeHandles = true;
					break;
				}
			}
		}
		if (hasResizeHandles) {
			SizeHandle.loadJavaScript(app);
			app.loadJavaScript("js/StdGridLayoutImpl.js", wtjs2());
		}
		int totalColStretch = 0;
		if (fitWidth) {
			for (int col = 0; col < colCount; ++col) {
				totalColStretch += Math.max(0,
						this.grid_.columns_.get(col).stretch_);
			}
		}
		int totalRowStretch = 0;
		if (fitHeight) {
			for (int row = 0; row < rowCount; ++row) {
				totalRowStretch += Math.max(0,
						this.grid_.rows_.get(row).stretch_);
			}
		}
		int[] margin = { 0, 0, 0, 0 };
		if (this.getLayout().getParentLayout() == null) {
			margin[3] = this.getLayout().getContentsMargin(Side.Left);
			margin[0] = this.getLayout().getContentsMargin(Side.Top);
			margin[1] = this.getLayout().getContentsMargin(Side.Right);
			margin[2] = this.getLayout().getContentsMargin(Side.Bottom);
		}
		DomElement div = DomElement.createNew(DomElementType.DomElement_DIV);
		div.setId(this.getId());
		div.setProperty(Property.PropertyStylePosition, "relative");
		String divStyle = "";
		if (fitHeight && !app.getEnvironment().agentIsIElt(9)
				&& !app.getEnvironment().agentIsWebKit()) {
			divStyle += "height: 100%;";
		}
		if (app.getEnvironment().agentIsIElt(9)) {
			divStyle += "zoom: 1;";
		}
		if (divStyle.length() != 0) {
			div.setProperty(Property.PropertyStyle, divStyle);
		}
		DomElement table = DomElement
				.createNew(DomElementType.DomElement_TABLE);
		{
			String style = "";
			if (fitWidth) {
				if (this.useFixedLayout_) {
					style += "table-layout: fixed;";
				}
				style += "width: 100%;";
			}
			if (fitHeight) {
				if (!app.getEnvironment().hasAjax()) {
					style += "height: 100%;";
				}
			}
			table.setProperty(Property.PropertyStyle, style);
		}
		DomElement tbody = DomElement
				.createNew(DomElementType.DomElement_TBODY);
		if (fitWidth) {
			for (int col = 0; col < colCount; ++col) {
				DomElement c = DomElement
						.createNew(DomElementType.DomElement_COL);
				int stretch = Math
						.max(0, this.grid_.columns_.get(col).stretch_);
				if (stretch != 0 || fitWidth && totalColStretch == 0) {
					char[] buf = new char[30];
					double pct = totalColStretch == 0 ? 100.0 / colCount
							: 100.0 * stretch / totalColStretch;
					StringBuilder ss = new StringBuilder();
					ss.append("width:").append(MathUtils.round(pct, 2)).append(
							"%;");
					c.setProperty(Property.PropertyStyle, ss.toString());
				}
				table.addChild(c);
				boolean resizeHandleRight = col < colCount - 1
						&& this.grid_.columns_.get(col).resizable_;
				if (resizeHandleRight) {
					c = DomElement.createNew(DomElementType.DomElement_COL);
					c.setProperty(Property.PropertyStyleWidth, String
							.valueOf(this.grid_.horizontalSpacing_)
							+ "px");
					c.setProperty(Property.PropertyClass, "Wt-vrh");
					table.addChild(c);
				}
			}
		}
		List<Boolean> overSpanned = new ArrayList<Boolean>();
		{
			int insertPos = 0;
			for (int ii = 0; ii < colCount * rowCount; ++ii)
				overSpanned.add(insertPos + ii, false);
		}
		;
		boolean resizeHandleAbove = false;
		int prevRowWithItem = -1;
		for (int row = 0; row < rowCount; ++row) {
			boolean resizeHandleBelow = row < rowCount - 1
					&& this.grid_.rows_.get(row).resizable_;
			DomElement tr = DomElement.createNew(DomElementType.DomElement_TR);
			String heightPct = "";
			int rowStretch = Math.max(0, this.grid_.rows_.get(row).stretch_);
			if (rowStretch != 0 || fitHeight && totalRowStretch == 0) {
				int pct = totalRowStretch == 0 ? 100 / rowCount : 100
						* rowStretch / totalRowStretch;
				StringWriter style = new StringWriter();
				style.append("height: ").append(String.valueOf(pct)).append(
						"%;");
				heightPct = style.toString();
				tr.setProperty(Property.PropertyStyle, heightPct);
			}
			boolean resizeHandleLeft = false;
			int prevColumnWithItem = -1;
			boolean rowVisible = false;
			for (int col = 0; col < colCount; ++col) {
				boolean resizeHandleRight = col < colCount - 1
						&& this.grid_.columns_.get(col - 1
								+ this.grid_.items_.get(row).get(col).colSpan_).resizable_;
				if (!overSpanned.get(row * colCount + col)) {
					Grid.Item item = this.grid_.items_.get(row).get(col);
					boolean itemFitWidth = true;
					boolean itemFitHeight = fitHeight
							&& (item.rowSpan_ == (int) rowCount || totalRowStretch == 0);
					int colSpan = 0;
					boolean colStretch = false;
					for (int i = 0; i < item.rowSpan_; ++i) {
						if (this.grid_.rows_.get(row + i).stretch_ != 0) {
							itemFitHeight = fitHeight;
						} else {
							if (!(rowStretch != 0)
									&& !(item.item_ != null && item.item_
											.getLayout() != null)) {
								itemFitHeight = false;
							}
						}
						colSpan = item.colSpan_;
						for (int j = 0; j < item.colSpan_; ++j) {
							if (this.grid_.columns_.get(col + j).stretch_ != 0) {
								colStretch = fitWidth;
							}
							if (i + j > 0) {
								overSpanned.set((row + i) * colCount + col + j,
										true);
							}
							if (j + 1 < item.colSpan_
									&& this.grid_.columns_.get(col + j).resizable_) {
								++colSpan;
							}
						}
					}
					AlignmentFlag hAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignHorizontalMask));
					AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignVerticalMask));
					if (hAlign != null && hAlign != AlignmentFlag.AlignJustify) {
						itemFitWidth = false;
					}
					if (vAlign != null) {
						itemFitHeight = false;
					}
					int[] padding = { 0, 0, 0, 0 };
					boolean itemVisible = this.hasItem(row, col);
					rowVisible = rowVisible || itemVisible;
					if (itemVisible) {
						int nextRow = this.nextRowWithItem(row, col);
						int prevRow = prevRowWithItem;
						int nextCol = this.nextColumnWithItem(row, col);
						int prevCol = prevColumnWithItem;
						if (prevRow == -1) {
							padding[0] = margin[0];
						} else {
							if (!resizeHandleAbove) {
								padding[0] = (this.grid_.verticalSpacing_ + 1) / 2;
							}
						}
						if (nextRow == (int) rowCount) {
							padding[2] = margin[2];
						} else {
							if (!resizeHandleBelow) {
								padding[2] = this.grid_.verticalSpacing_ / 2;
							}
						}
						if (prevCol == -1) {
							padding[3] = margin[3];
						} else {
							if (!resizeHandleLeft) {
								padding[3] = (this.grid_.horizontalSpacing_ + 1) / 2;
							}
						}
						if (nextCol == (int) colCount) {
							padding[1] = margin[1];
						} else {
							if (!resizeHandleRight) {
								padding[1] = this.grid_.horizontalSpacing_ / 2;
							}
						}
					}
					DomElement td = DomElement
							.createNew(DomElementType.DomElement_TD);
					if (item.item_ != null) {
						if (app.getEnvironment().agentIsIElt(9)
								&& vAlign == null) {
							td.setProperty(Property.PropertyStylePosition,
									"relative");
						}
						DomElement c = getImpl(item.item_).createDomElement(
								itemFitWidth, itemFitHeight, app);
						if (!app.getEnvironment().agentIsIE()) {
							c.setProperty(Property.PropertyStyleBoxSizing,
									"border-box");
						}
						if (hAlign == null) {
							hAlign = AlignmentFlag.AlignJustify;
						}
						switch (hAlign) {
						case AlignCenter: {
							DomElement itable = DomElement
									.createNew(DomElementType.DomElement_TABLE);
							itable.setProperty(Property.PropertyClass,
									"Wt-hcenter");
							if (vAlign == null) {
								itable.setProperty(Property.PropertyStyle,
										"height:100%;");
							}
							DomElement irow = DomElement
									.createNew(DomElementType.DomElement_TR);
							DomElement itd = DomElement
									.createNew(DomElementType.DomElement_TD);
							if (vAlign == null) {
								itd.setProperty(Property.PropertyStyle,
										"height:100%;");
							}
							itd.addChild(c);
							if (app.getEnvironment().agentIsIElt(9)) {
								if (c.getProperty(
										Property.PropertyStyleMinWidth)
										.length() != 0) {
									DomElement spacer = DomElement
											.createNew(DomElementType.DomElement_DIV);
									spacer
											.setProperty(
													Property.PropertyStyleWidth,
													c
															.getProperty(Property.PropertyStyleMinWidth));
									spacer
											.setProperty(
													Property.PropertyStyleHeight,
													"1px");
									itd.addChild(spacer);
								}
							}
							irow.addChild(itd);
							itable.addChild(irow);
							c = itable;
							break;
						}
						case AlignRight:
							if (!c.isDefaultInline()) {
								c.setProperty(Property.PropertyStyleFloat,
										"right");
							} else {
								td.setProperty(Property.PropertyStyleTextAlign,
										"right");
							}
							break;
						case AlignLeft:
							if (!c.isDefaultInline()) {
								c.setProperty(Property.PropertyStyleFloat,
										"left");
							} else {
								td.setProperty(Property.PropertyStyleTextAlign,
										"left");
							}
							break;
						case AlignJustify:
							if (c.getProperty(Property.PropertyStyleWidth)
									.length() == 0
									&& this.useFixedLayout_
									&& !app.getEnvironment().agentIsWebKit()
									&& !app.getEnvironment().agentIsGecko()
									&& !c.isDefaultInline()) {
								c.setProperty(Property.PropertyStyleWidth,
										"100%");
							}
							break;
						default:
							break;
						}
						if (!app.getEnvironment().agentIsIE()) {
							c.setProperty(Property.PropertyStyleBoxSizing,
									"border-box");
						}
						if (c.getType() == DomElementType.DomElement_BUTTON) {
							c
									.setProperty(
											Property.PropertyStyleMarginLeft,
											"0");
							c.setProperty(Property.PropertyStyleMarginRight,
									"0");
						}
						if (colStretch && itemFitWidth && itemFitHeight) {
							td.setProperty(Property.PropertyClass, "Wt-chwrap");
							c.setProperty(Property.PropertyStylePosition,
									"absolute");
							if (!app.getEnvironment().agentIsIE()) {
								DomElement chwrap = DomElement
										.createNew(DomElementType.DomElement_DIV);
								chwrap.setProperty(Property.PropertyClass,
										"Wt-chwrap");
								chwrap.addChild(c);
								td.addChild(chwrap);
							} else {
								td.addChild(c);
							}
						} else {
							td.addChild(c);
						}
						if (app.getEnvironment().agentIsIElt(9)) {
							if (c.getProperty(Property.PropertyStyleMinWidth)
									.length() != 0) {
								DomElement spacer = DomElement
										.createNew(DomElementType.DomElement_DIV);
								spacer
										.setProperty(
												Property.PropertyStyleWidth,
												c
														.getProperty(Property.PropertyStyleMinWidth));
								spacer.setProperty(
										Property.PropertyStyleHeight, "1px");
								td.addChild(spacer);
							}
						}
					}
					{
						StringBuilder style = new StringBuilder();
						if (vAlign == null) {
							style.append(heightPct);
						}
						if (app.getLayoutDirection() == LayoutDirection.RightToLeft) {
							int tmp = padding[1];
							padding[1] = padding[3];
							padding[3] = tmp;
							;
						}
						if (padding[0] == padding[1]
								&& padding[0] == padding[2]
								&& padding[0] == padding[3]) {
							if (padding[0] != 0) {
								style.append("padding:").append(padding[0])
										.append("px;");
							}
						} else {
							style.append("padding:").append(padding[0]).append(
									"px ").append(padding[1]).append("px ")
									.append(padding[2]).append("px ").append(
											padding[3]).append("px;");
						}
						if (vAlign != null) {
							switch (vAlign) {
							case AlignTop:
								style.append("vertical-align:top;");
								break;
							case AlignMiddle:
								style.append("vertical-align:middle;");
								break;
							case AlignBottom:
								style.append("vertical-align:bottom;");
							default:
								break;
							}
						}
						td
								.setProperty(Property.PropertyStyle, style
										.toString());
					}
					if (item.rowSpan_ != 1) {
						td.setProperty(Property.PropertyRowSpan, String
								.valueOf(item.rowSpan_));
					}
					if (colSpan != 1) {
						td.setProperty(Property.PropertyColSpan, String
								.valueOf(colSpan));
					}
					tr.addChild(td);
					if (itemVisible && resizeHandleRight) {
						td = DomElement.createNew(DomElementType.DomElement_TD);
						td.setProperty(Property.PropertyClass, "Wt-vrh");
						StringBuilder style = new StringBuilder();
						style.append("padding:").append(padding[0]).append(
								"px 0px ").append(padding[2]).append("px;");
						td
								.setProperty(Property.PropertyStyle, style
										.toString());
						DomElement div2 = DomElement
								.createNew(DomElementType.DomElement_DIV);
						div2.setProperty(Property.PropertyStyleWidth, String
								.valueOf(this.grid_.horizontalSpacing_)
								+ "px");
						td.addChild(div2);
						tr.addChild(td);
					}
					if (itemVisible) {
						resizeHandleLeft = resizeHandleRight;
						prevColumnWithItem = col;
					}
				}
			}
			tbody.addChild(tr);
			if (rowVisible) {
				if (resizeHandleBelow) {
					tr = DomElement.createNew(DomElementType.DomElement_TR);
					tr.setProperty(Property.PropertyClass, "Wt-hrh");
					String height = String.valueOf(this.grid_.verticalSpacing_)
							+ "px";
					tr.setProperty(Property.PropertyStyleHeight, height);
					DomElement td = DomElement
							.createNew(DomElementType.DomElement_TD);
					td.setProperty(Property.PropertyColSpan, String
							.valueOf(colCount));
					StringBuilder style2 = new StringBuilder();
					style2.append("padding: 0px").append(margin[1]).append(
							"px 0px").append(margin[3]).append("px;");
					td.setProperty(Property.PropertyStyleHeight, style2
							.toString());
					DomElement div2 = DomElement
							.createNew(DomElementType.DomElement_DIV);
					div2.setProperty(Property.PropertyStyleHeight, height);
					td.addChild(div2);
					tr.addChild(td);
					tbody.addChild(tr);
				}
				prevRowWithItem = row;
				resizeHandleAbove = resizeHandleBelow;
			}
		}
		table.addChild(tbody);
		StringWriter layoutAdd = new StringWriter();
		layoutAdd.append(app.getJavaScriptClass()).append(
				".layouts.add(new Wt3_2_1.StdLayout( Wt3_2_1, '").append(
				div.getId()).append("', ").append(
				String.valueOf(fitHeight ? 1 : 0)).append(", { stretch: [");
		for (int i = 0; i < rowCount; ++i) {
			if (i != 0) {
				layoutAdd.append(",");
			}
			int stretch = 0;
			if (totalRowStretch == 0 && fitHeight) {
				stretch = 1;
			} else {
				stretch = this.grid_.rows_.get(i).stretch_;
			}
			layoutAdd.append(String.valueOf(stretch));
		}
		layoutAdd.append("], minheight: [");
		for (int i = 0; i < rowCount; ++i) {
			if (i != 0) {
				layoutAdd.append(",");
			}
			int minHeight = 0;
			for (int j = 0; j < colCount; ++j) {
				WLayoutItem item = this.grid_.items_.get(i).get(j).item_;
				if (item != null) {
					minHeight = Math.max(minHeight, getImpl(item)
							.getMinimumHeight());
				}
			}
			if (i == 0) {
				minHeight += margin[0];
			} else {
				minHeight += this.grid_.verticalSpacing_;
			}
			if (i == rowCount - 1) {
				minHeight += margin[2];
			}
			layoutAdd.append(String.valueOf(minHeight));
		}
		layoutAdd.append("]}));");
		table.callJavaScript(layoutAdd.toString());
		div.addChild(table);
		return div;
	}

	public void updateDom() {
		if (this.forceUpdate_) {
			this.forceUpdate_ = false;
			WApplication app = WApplication.getInstance();
			app.doJavaScript(app.getJavaScriptClass() + ".layouts.adjust('"
					+ this.getId() + "');");
		}
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		for (int i = 0; i < rowCount; ++i) {
			for (int j = 0; j < colCount; ++j) {
				WLayoutItem item = this.grid_.items_.get(i).get(j).item_;
				if (item != null) {
					WLayout nested = item.getLayout();
					if (nested != null) {
						(((nested.getImpl()) instanceof StdLayoutImpl ? (StdLayoutImpl) (nested
								.getImpl())
								: null)).updateDom();
					}
				}
			}
		}
	}

	public void setHint(String name, String value) {
		if (name.equals("table-layout")) {
			if (value.equals("fixed")) {
				this.useFixedLayout_ = true;
			} else {
				if (value.equals("auto")) {
					this.useFixedLayout_ = false;
				} else {
					logger.error(new StringWriter().append(
							"unrecognized hint value '").append(value).append(
							"' for '").append(name).append("'").toString());
				}
			}
		} else {
			logger.error(new StringWriter().append("unrecognized hint '")
					.append(name).append("'").toString());
		}
	}

	public static String getChildrenResizeJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs10());
		return "Wt3_2_1.ChildrenResize";
	}

	public boolean itemResized(WLayoutItem item) {
		WWidget ww = item.getWidget();
		if (ww != null && ww.getJavaScriptMember("wtResize").length() != 0) {
			this.forceUpdate_ = true;
			return true;
		}
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		for (int row = 0; row < rowCount; ++row) {
			if (this.grid_.rows_.get(row).stretch_ <= 0) {
				for (int col = 0; col < colCount; ++col) {
					if (this.grid_.items_.get(row).get(col).item_ == item) {
						this.forceUpdate_ = true;
						return true;
					}
				}
			}
		}
		return false;
	}

	void containerAddWidgets(WContainerWidget container) {
		super.containerAddWidgets(container);
		if (!(container != null)) {
			return;
		}
		WApplication app = WApplication.getInstance();
		if (this.getParentLayoutImpl() == null) {
			if (container == app.getRoot()) {
				app.setBodyClass(app.getBodyClass() + " Wt-layout");
				app.setHtmlClass(app.getHtmlClass() + " Wt-layout");
			}
		}
	}

	private Grid grid_;
	private boolean useFixedLayout_;
	private boolean forceUpdate_;

	private int nextRowWithItem(int row, int c) {
		for (row += this.grid_.items_.get(row).get(c).rowSpan_; row < (int) this.grid_.rows_
				.size(); ++row) {
			for (int col = 0; col < this.grid_.columns_.size(); col += this.grid_.items_
					.get(row).get(col).colSpan_) {
				if (this.hasItem(row, col)) {
					return row;
				}
			}
		}
		return this.grid_.rows_.size();
	}

	private int nextColumnWithItem(int row, int col) {
		for (;;) {
			col = col + this.grid_.items_.get(row).get(col).colSpan_;
			if (col < (int) this.grid_.columns_.size()) {
				for (int i = 0; i < this.grid_.rows_.size(); ++i) {
					if (this.hasItem(i, col)) {
						return col;
					}
				}
			} else {
				return this.grid_.columns_.size();
			}
		}
	}

	private boolean hasItem(int row, int col) {
		WLayoutItem item = this.grid_.items_.get(row).get(col).item_;
		if (item != null) {
			WWidget w = item.getWidget();
			return !(w != null) || !w.isHidden();
		} else {
			return false;
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"StdLayout",
				"function(b,r,j,n){var y=this;this.WT=b;this.getId=function(){return r};this.marginH=function(a){var c=a.parentNode,d=0;if(!b.boxSizing(a)){d=b.px(a,\"marginLeft\");d+=b.px(a,\"marginRight\");d+=b.px(a,\"borderLeftWidth\");d+=b.px(a,\"borderRightWidth\");d+=b.px(a,\"paddingLeft\");d+=b.px(a,\"paddingRight\")}d+=b.pxself(c,\"paddingLeft\");d+=b.pxself(c,\"paddingRight\");return d};this.marginV=function(a){var c=b.px(a,\"marginTop\");c+=b.px(a,\"marginBottom\");if(!b.boxSizing(a)&& !(b.isIE&&!b.isIElt9&&b.hasTag(a,\"BUTTON\"))){c+=b.px(a,\"borderTopWidth\");c+=b.px(a,\"borderBottomWidth\");c+=b.px(a,\"paddingTop\");c+=b.px(a,\"paddingBottom\")}return c};this.getColumn=function(a){var c,d,e,h=b.getElement(r).firstChild.childNodes;d=c=0;for(e=h.length;d<e;d++){var f=h[d];if(b.hasTag(f,\"COLGROUP\")){d=-1;h=f.childNodes;e=h.length}if(b.hasTag(f,\"COL\"))if(f.className!=\"Wt-vrh\")if(c==a)return f;else++c}return null};this.adjustCell=function(a,c){var d=c==0;c-=b.pxself(a,\"paddingTop\");c-=b.pxself(a, \"paddingBottom\");if(c<=0)c=0;a.style.height=c+\"px\";if(!(a.style.verticalAlign||a.childNodes.length==0)){a=a.childNodes[0];if(a.className==\"Wt-hcenter\"){a.style.height=c+\"px\";a=a.firstChild.firstChild;if(!b.hasTag(a,\"TD\"))a=a.firstChild;if(a.style.height!=c+\"px\")a.style.height=c+\"px\";a=a.firstChild}c-=this.marginV(a);if(c<=0)c=0;if(!b.hasTag(a,\"TABLE\")){var e,h;if(!d){e=a.parentNode;h=e.offsetWidth-y.marginH(a);if(e.className==\"Wt-chwrap\"&&h>0){if(!b.isIE){a.style.position=\"relative\";a=a.firstChild}a.style.width= h+\"px\";a.style.position=\"absolute\"}}if(!d&&a.wtResize)a.wtResize(a,h,c);else if(a.style.height!=c+\"px\"){a.style.height=c+\"px\";if(a.className==\"Wt-wrapdiv\")if(b.isIE&&b.hasTag(a.firstChild,\"TEXTAREA\"))a.firstChild.style.height=c-b.pxself(a,\"marginBottom\")+\"px\"}}}};this.adjustRow=function(a,c){var d=[];if(a.style.height!=c+\"px\")a.style.height=c+\"px\";a=a.childNodes;var e,h,f,o;e=0;o=-1;for(h=a.length;e<h;++e){f=a[e];f.className!=\"Wt-vrh\"&&++o;if(f.rowSpan!=1){this.adjustCell(f,0,o);d.push({td:f,col:o})}else this.adjustCell(f, c,o)}return d};this.adjust=function(){var a=b.getElement(r);if(!a)return false;y.initResize&&y.initResize(b,r,n);if(b.isHidden(a))return true;var c=a.firstChild,d=a.parentNode;if(c.style.height!==\"\")c.style.height=\"\";var e,h=false;if(j){e=b.pxself(d,\"height\");if(e===0){h=true;e=d.clientHeight}}else e=c.clientHeight;var f=d.clientWidth;if(!(a.dirty||c.w!==f||c.h!==e))return true;c.w=f;c.h=e;a.dirty=null;f=e;var o;if(j){if(h){f-=b.px(d,\"paddingTop\");f-=b.px(d,\"paddingBottom\")}else if(b.boxSizing(d)){f-= b.px(d,\"borderTopWidth\");f-=b.px(d,\"borderBottomWidth\");f-=b.px(d,\"paddingTop\");f-=b.px(d,\"paddingBottom\")}f-=b.px(a,\"marginTop\");f-=b.px(a,\"marginBottom\");if(d.children&&d.children.length!==1){h=0;for(o=d.children.length;h<o;++h){e=d.children[h];if(e!==a)f-=$(e).outerHeight()}}var q=0;a=0;var k;d=h=0;for(o=c.rows.length;h<o;h++){e=c.rows[h];if(e.className)f-=e.offsetHeight;else{a+=n.minheight[d];if(n.stretch[d]<=0)f-=e.offsetHeight;else q+=n.stretch[d];++d}}f=f>a?f:a}a=[];var s=f,t;for(d=h=0;h<o;h++){e= c.rows[h];if(!e.className){k=n.stretch[d];if(k==-1||j&&k>0){if(k!==-1){t=n.minheight[d];k=f*k/q;k=s>k?k:s;k=Math.round(t>k?t:k);s-=k}else k=e.offsetHeight;b.addAll(a,this.adjustRow(e,k))}++d}}h=0;for(o=a.length;h<o;++h){e=a[h].td;q=a[h].col;this.adjustCell(e,e.offsetHeight,q)}if(c.style.tableLayout!==\"fixed\")return true;d=0;s=c.childNodes;var A,x,u;f=0;for(a=s.length;f<a;f++){q=s[f];if(b.hasTag(q,\"COLGROUP\")){f=-1;s=q.childNodes;a=s.length}if(b.hasTag(q,\"COL\")){if(b.pctself(q,\"width\")===0){h=t=0; for(o=c.rows.length;h<o;h++){e=c.rows[h];k=e.childNodes;x=A=0;for(u=k.length;x<u;x++){e=k[x];if(e.colSpan===1&&A===d&&e.childNodes.length===1){e=e.firstChild;e=e.offsetWidth+y.marginH(e);t=Math.max(t,e);break}A+=e.colSpan;if(A>d)break}}if(t>0&&b.pxself(q,\"width\")!==t)q.style.width=t+\"px\"}++d}}return true};this.contains=function(a){var c=b.getElement(r);a=b.getElement(a.getId());return c&&a?b.contains(c,a):false}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"StdLayout.prototype.initResize",
				"function(b,r,j){function n(g,i){if(g.offsetWidth>0)return g.offsetWidth;else{g=q.firstChild.rows[0];var l,m,p,v;p=m=0;for(v=g.childNodes.length;m<v;++m){l=g.childNodes[m];if(l.className!=\"Wt-vrh\"){if(p==i)return l.offsetWidth;p+=l.colSpan}}return 0}}function y(g,i){var l=b.getElement(r).firstChild;o(g).style.width=i+\"px\";var m,p,v,z;p=m=0;for(v=l.rows.length;m<v;m++){z=l.rows[m];if(!z.className){var w,B,C,F;C=B=0;for(F=z.childNodes.length;B< F;++B){w=z.childNodes[B];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&C==g&&w.childNodes.length>0){z=w.firstChild;w=Math.max(1,i-f.marginH(z));z.style.width=w+\"px\";break}C+=w.colSpan}}++p}}}function a(g,i,l){var m=g.firstChild;new b.SizeHandle(b,\"v\",m.offsetHeight,m.offsetWidth,-g.parentNode.previousSibling.offsetHeight,g.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(p){d(g,i,p)},m,q,l,0,0)}function c(g,i,l){var m=-g.previousSibling.offsetWidth,p=g.nextSibling.offsetWidth,v=g.firstChild,z=b.pxself(k.rows[0].childNodes[0], \"paddingTop\"),w=b.pxself(k.rows[k.rows.length-1].childNodes[0],\"paddingBottom\");w=k.offsetHeight-z-w;if($(document.body).hasClass(\"Wt-rtl\")){var B=m;m=-p;p=-B}new b.SizeHandle(b,\"h\",v.offsetWidth,w,m,p,\"Wt-hsh\",function(C){h(g,i,C)},v,q,l,0,-g.offsetTop+z-b.pxself(g,\"paddingTop\"))}function d(g,i,l){var m=g.parentNode.previousSibling;g=g.parentNode.nextSibling;var p=m.offsetHeight,v=g.offsetHeight;if(j.stretch[i]>0&&j.stretch[i+1]>0)j.stretch[i]=-1;if(j.stretch[i+1]==0)j.stretch[i+1]=-1;j.stretch[i]<= 0&&f.adjustRow(m,p+l);j.stretch[i+1]<=0&&f.adjustRow(g,v-l);b.getElement(r).dirty=true;window.onresize()}function e(){var g,i=0;for(g=0;;++g){var l=o(g);if(l)i+=b.pctself(l,\"width\");else break}if(i!=0)for(g=0;;++g)if(l=o(g)){var m=b.pctself(l,\"width\");if(m)l.style.width=m*100/i+\"%\"}else break}function h(g,i,l){g=o(i);var m=n(g,i),p=o(i+1),v=n(p,i+1);if($(document.body).hasClass(\"Wt-rtl\"))l=-l;if(b.pctself(g,\"width\")>0&&b.pctself(p,\"width\")>0){g.style.width=\"\";e()}if(b.pctself(g,\"width\")==0)y(i,m+ l);else b.getElement(r).dirty=true;if(b.pctself(p,\"width\")==0)y(i+1,v-l);else b.getElement(r).dirty=true;window.onresize()}var f=this,o=f.getColumn,q=b.getElement(r);if(q)if(!f.resizeInitialized){var k=q.firstChild,s,t,A,x;t=s=0;for(A=k.rows.length;s<A;s++){x=k.rows[s];if(x.className){if(x.className===\"Wt-hrh\"){var u=x.firstChild;u.ri=t-1;u.onmousedown=u.ontouchstart=function(g){a(this,this.ri,g||window.event)}}}else{var D,E,G;E=D=0;for(G=x.childNodes.length;D<G;++D){u=x.childNodes[D];if(u.className== \"Wt-vrh\"){u.ci=E-1;u.onmousedown=u.ontouchstart=function(g){c(this,this.ci,g||window.event)}}else E+=u.colSpan}++t}}f.resizeInitialized=true}}");
	}

	static WJavaScriptPreamble appjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.ApplicationScope,
				JavaScriptObjectType.JavaScriptObject,
				"layouts",
				"new (function(){var b=[],r=false;this.add=function(j){var n,y;n=0;for(y=b.length;n<y;++n){var a=b[n];if(a.getId()==j.getId()){b[n]=j;return}else if(j.contains(a)){b.splice(n,0,j);return}}b.push(j)};this.adjust=function(j){if(j){if(j=$(\"#\"+j).get(0))j.dirty=true}else if(!r){r=true;for(var n=0;n<b.length;++n){j=b[n];if(!j.adjust()){j.WT.arrayRemove(b,n);--n}}r=false}}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(b,e,c){function k(f){var g=a.px(f,\"marginTop\");g+=a.px(f,\"marginBottom\");if(!a.boxSizing(f)){g+=a.px(f,\"borderTopWidth\");g+=a.px(f,\"borderBottomWidth\");g+=a.px(f,\"paddingTop\");g+=a.px(f,\"paddingBottom\")}return g}var i,j,d,a=this;b.style.height=c+\"px\";if(a.boxSizing(b)){c-=a.px(b,\"marginTop\");c-=a.px(b,\"marginBottom\");c-=a.px(b,\"borderTopWidth\");c-=a.px(b,\"borderBottomWidth\");c-=a.px(b,\"paddingTop\");c-=a.px(b,\"paddingBottom\");e-= a.px(b,\"marginLeft\");e-=a.px(b,\"marginRight\");e-=a.px(b,\"borderLeftWidth\");e-=a.px(b,\"borderRightWidth\");e-=a.px(b,\"paddingLeft\");e-=a.px(b,\"paddingRight\")}i=0;for(j=b.childNodes.length;i<j;++i){d=b.childNodes[i];if(d.nodeType==1){var h=c-k(d);if(h>0)if(d.wtResize)d.wtResize(d,e,h);else{h=h+\"px\";if(d.style.height!=h)d.style.height=h}}}}");
	}
}
