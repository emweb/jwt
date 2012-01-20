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
				".layouts.add(new Wt3_2_0.StdLayout( Wt3_2_0, '").append(
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
		return "Wt3_2_0.ChildrenResize";
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
				"function(b,r,k,o){var y=this;this.WT=b;this.getId=function(){return r};this.marginH=function(a){var c=a.parentNode,d=0;if(!b.boxSizing(a)){d=b.px(a,\"marginLeft\");d+=b.px(a,\"marginRight\");d+=b.px(a,\"borderLeftWidth\");d+=b.px(a,\"borderRightWidth\");d+=b.px(a,\"paddingLeft\");d+=b.px(a,\"paddingRight\")}d+=b.pxself(c,\"paddingLeft\");d+=b.pxself(c,\"paddingRight\");return d};this.marginV=function(a){var c=b.px(a,\"marginTop\");c+=b.px(a,\"marginBottom\");if(!b.boxSizing(a)){c+= b.px(a,\"borderTopWidth\");c+=b.px(a,\"borderBottomWidth\");c+=b.px(a,\"paddingTop\");c+=b.px(a,\"paddingBottom\")}return c};this.getColumn=function(a){var c,d,i,g=b.getElement(r).firstChild.childNodes;d=c=0;for(i=g.length;d<i;d++){var e=g[d];if(b.hasTag(e,\"COLGROUP\")){d=-1;g=e.childNodes;i=g.length}if(b.hasTag(e,\"COL\"))if(e.className!=\"Wt-vrh\")if(c==a)return e;else++c}return null};this.adjustCell=function(a,c){var d=c==0;c-=b.pxself(a,\"paddingTop\");c-=b.pxself(a,\"paddingBottom\");if(c<=0)c=0;a.style.height= c+\"px\";if(!(a.style.verticalAlign||a.childNodes.length==0)){a=a.childNodes[0];if(c<=0)c=0;if(a.className==\"Wt-hcenter\"){a.style.height=c+\"px\";a=a.firstChild.firstChild;if(!b.hasTag(a,\"TD\"))a=a.firstChild;if(a.style.height!=c+\"px\")a.style.height=c+\"px\";a=a.firstChild}c-=this.marginV(a);if(c<=0)c=0;if(!b.hasTag(a,\"TABLE\")){var i,g;if(!d){i=a.parentNode;g=i.offsetWidth-y.marginH(a);if(i.className==\"Wt-chwrap\"&&g>0){if(!b.isIE){a.style.position=\"relative\";a=a.firstChild}a.style.width=g+\"px\";a.style.position= \"absolute\"}}if(!d&&a.wtResize)a.wtResize(a,g,c);else if(a.style.height!=c+\"px\"){a.style.height=c+\"px\";if(a.className==\"Wt-wrapdiv\")if(b.isIE&&b.hasTag(a.firstChild,\"TEXTAREA\"))a.firstChild.style.height=c-b.pxself(a,\"marginBottom\")+\"px\"}}}};this.adjustRow=function(a,c){var d=[];if(a.style.height!=c+\"px\")a.style.height=c+\"px\";a=a.childNodes;var i,g,e,h;i=0;h=-1;for(g=a.length;i<g;++i){e=a[i];e.className!=\"Wt-vrh\"&&++h;if(e.rowSpan!=1){this.adjustCell(e,0,h);d.push({td:e,col:h})}else this.adjustCell(e, c,h)}return d};this.adjust=function(){var a=b.getElement(r);if(!a)return false;y.initResize&&y.initResize(b,r,o);if(b.isHidden(a))return true;var c=a.firstChild,d=a.parentNode;if(c.style.height!==\"\")c.style.height=\"\";var i,g=false;if(k){i=b.pxself(d,\"height\");if(i===0){g=true;i=d.clientHeight}}else i=c.clientHeight;var e=d.clientWidth;if(!(a.dirty||c.w!==e||c.h!==i))return true;c.w=e;c.h=i;a.dirty=null;var h=i;i=c.rows.length;if(k){if(g){h-=b.px(d,\"paddingTop\");h-=b.px(d,\"paddingBottom\")}else if(b.boxSizing(d)){h-= b.px(d,\"borderTopWidth\");h-=b.px(d,\"borderBottomWidth\");h-=b.px(d,\"paddingTop\");h-=b.px(d,\"paddingBottom\")}h-=b.px(a,\"marginTop\");h-=b.px(a,\"marginBottom\");if(d.children&&d.children.length!==1){g=0;for(i=d.children.length;g<i;++g){e=d.children[g];if(e!==a)h-=$(e).outerHeight()}}var q=0;a=0;var l;for(d=g=0;g<i;g++){e=c.rows[g];if(e.className)h-=e.offsetHeight;else{a+=o.minheight[d];if(o.stretch[d]<=0)h-=e.offsetHeight;else q+=o.stretch[d];++d}}h=h>a?h:a}a=[];var s=h,t;for(d=g=0;g<i;g++){e=c.rows[g]; if(!e.className){l=o.stretch[d];if(l==-1||k&&l>0){if(l!==-1){t=o.minheight[d];l=h*l/q;l=s>l?l:s;l=Math.round(t>l?t:l);s-=l}else l=e.offsetHeight;b.addAll(a,this.adjustRow(e,l))}++d}}g=0;for(i=a.length;g<i;++g){e=a[g].td;q=a[g].col;this.adjustCell(e,e.offsetHeight,q)}if(c.style.tableLayout!==\"fixed\")return true;d=0;s=c.childNodes;var A,x,u;h=0;for(a=s.length;h<a;h++){q=s[h];if(b.hasTag(q,\"COLGROUP\")){h=-1;s=q.childNodes;a=s.length}if(b.hasTag(q,\"COL\")){if(b.pctself(q,\"width\")===0){g=t=0;for(i=c.rows.length;g< i;g++){e=c.rows[g];l=e.childNodes;x=A=0;for(u=l.length;x<u;x++){e=l[x];if(e.colSpan===1&&A===d&&e.childNodes.length===1){e=e.firstChild;e=e.offsetWidth+y.marginH(e);t=Math.max(t,e);break}A+=e.colSpan;if(A>d)break}}if(t>0&&b.pxself(q,\"width\")!==t)q.style.width=t+\"px\"}++d}}return true};this.contains=function(a){var c=b.getElement(r);a=b.getElement(a.getId());return c&&a?b.contains(c,a):false}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"StdLayout.prototype.initResize",
				"function(b,r,k){function o(f,j){if(f.offsetWidth>0)return f.offsetWidth;else{f=q.firstChild.rows[0];var m,n,p,v;p=n=0;for(v=f.childNodes.length;n<v;++n){m=f.childNodes[n];if(m.className!=\"Wt-vrh\"){if(p==j)return m.offsetWidth;p+=m.colSpan}}return 0}}function y(f,j){var m=b.getElement(r).firstChild;h(f).style.width=j+\"px\";var n,p,v,z;p=n=0;for(v=m.rows.length;n<v;n++){z=m.rows[n];if(!z.className){var w,B,C,F;C=B=0;for(F=z.childNodes.length;B< F;++B){w=z.childNodes[B];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&C==f&&w.childNodes.length>0){z=w.firstChild;w=Math.max(1,j-e.marginH(z));z.style.width=w+\"px\";break}C+=w.colSpan}}++p}}}function a(f,j,m){var n=f.firstChild;new b.SizeHandle(b,\"v\",n.offsetHeight,n.offsetWidth,-f.parentNode.previousSibling.offsetHeight,f.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(p){d(f,j,p)},n,q,m,0,0)}function c(f,j,m){var n=-f.previousSibling.offsetWidth,p=f.nextSibling.offsetWidth,v=f.firstChild,z=b.pxself(l.rows[0].childNodes[0], \"paddingTop\"),w=b.pxself(l.rows[l.rows.length-1].childNodes[0],\"paddingBottom\");w=l.offsetHeight-z-w;if($(document.body).hasClass(\"Wt-rtl\")){var B=n;n=-p;p=-B}new b.SizeHandle(b,\"h\",v.offsetWidth,w,n,p,\"Wt-hsh\",function(C){g(f,j,C)},v,q,m,0,-f.offsetTop+z-b.pxself(f,\"paddingTop\"))}function d(f,j,m){var n=f.parentNode.previousSibling;f=f.parentNode.nextSibling;var p=n.offsetHeight,v=f.offsetHeight;if(k.stretch[j]>0&&k.stretch[j+1]>0)k.stretch[j]=-1;if(k.stretch[j+1]==0)k.stretch[j+1]=-1;k.stretch[j]<= 0&&e.adjustRow(n,p+m);k.stretch[j+1]<=0&&e.adjustRow(f,v-m);b.getElement(r).dirty=true;window.onresize()}function i(){var f,j=0;for(f=0;;++f){var m=h(f);if(m)j+=b.pctself(m,\"width\");else break}if(j!=0)for(f=0;;++f)if(m=h(f)){var n=b.pctself(m,\"width\");if(n)m.style.width=n*100/j+\"%\"}else break}function g(f,j,m){f=h(j);var n=o(f,j),p=h(j+1),v=o(p,j+1);if($(document.body).hasClass(\"Wt-rtl\"))m=-m;if(b.pctself(f,\"width\")>0&&b.pctself(p,\"width\")>0){f.style.width=\"\";i()}if(b.pctself(f,\"width\")==0)y(j,n+ m);else b.getElement(r).dirty=true;if(b.pctself(p,\"width\")==0)y(j+1,v-m);else b.getElement(r).dirty=true;window.onresize()}var e=this,h=e.getColumn,q=b.getElement(r);if(q)if(!e.resizeInitialized){var l=q.firstChild,s,t,A,x;t=s=0;for(A=l.rows.length;s<A;s++){x=l.rows[s];if(x.className){if(x.className===\"Wt-hrh\"){var u=x.firstChild;u.ri=t-1;u.onmousedown=u.ontouchstart=function(f){a(this,this.ri,f||window.event)}}}else{var D,E,G;E=D=0;for(G=x.childNodes.length;D<G;++D){u=x.childNodes[D];if(u.className== \"Wt-vrh\"){u.ci=E-1;u.onmousedown=u.ontouchstart=function(f){c(this,this.ci,f||window.event)}}else E+=u.colSpan}++t}}e.resizeInitialized=true}}");
	}

	static WJavaScriptPreamble appjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.ApplicationScope,
				JavaScriptObjectType.JavaScriptObject,
				"layouts",
				"new (function(){var b=[],r=false;this.add=function(k){var o,y;o=0;for(y=b.length;o<y;++o){var a=b[o];if(a.getId()==k.getId()){b[o]=k;return}else if(k.contains(a)){b.splice(o,0,k);return}}b.push(k)};this.adjust=function(k){if(k){if(k=$(\"#\"+k).get(0))k.dirty=true}else if(!r){r=true;for(var o=0;o<b.length;++o){k=b[o];if(!k.adjust()){k.WT.arrayRemove(b,o);--o}}r=false}}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(b,e,c){function k(f){var g=a.px(f,\"marginTop\");g+=a.px(f,\"marginBottom\");if(!a.boxSizing(f)){g+=a.px(f,\"borderTopWidth\");g+=a.px(f,\"borderBottomWidth\");g+=a.px(f,\"paddingTop\");g+=a.px(f,\"paddingBottom\")}return g}var i,j,d,a=this;b.style.height=c+\"px\";if(a.boxSizing(b)){c-=a.px(b,\"marginTop\");c-=a.px(b,\"marginBottom\");c-=a.px(b,\"borderTopWidth\");c-=a.px(b,\"borderBottomWidth\");c-=a.px(b,\"paddingTop\");c-=a.px(b,\"paddingBottom\");e-= a.px(b,\"marginLeft\");e-=a.px(b,\"marginRight\");e-=a.px(b,\"borderLeftWidth\");e-=a.px(b,\"borderRightWidth\");e-=a.px(b,\"paddingLeft\");e-=a.px(b,\"paddingRight\")}i=0;for(j=b.childNodes.length;i<j;++i){d=b.childNodes[i];if(d.nodeType==1){var h=c-k(d);if(h>0)if(d.wtResize)d.wtResize(d,e,h);else{h=h+\"px\";if(d.style.height!=h)d.style.height=h}}}}");
	}
}
