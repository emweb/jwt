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

class StdGridLayoutImpl extends StdLayoutImpl {
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
					int pct = totalColStretch == 0 ? 100 / colCount : 100
							* stretch / totalColStretch;
					c.setProperty(Property.PropertyStyle, "width:"
							+ String.valueOf(pct) + "%;");
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
						String style = "";
						if (vAlign == null) {
							style += heightPct;
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
								style += "padding:"
										+ String.valueOf(padding[0]) + "px;";
							}
						} else {
							style += "padding:" + String.valueOf(padding[0])
									+ "px " + String.valueOf(padding[1])
									+ "px " + String.valueOf(padding[2])
									+ "px " + String.valueOf(padding[3])
									+ "px;";
						}
						if (vAlign != null) {
							switch (vAlign) {
							case AlignTop:
								style += "vertical-align:top;";
								break;
							case AlignMiddle:
								style += "vertical-align:middle;";
								break;
							case AlignBottom:
								style += "vertical-align:bottom;";
							default:
								break;
							}
						}
						if (style.length() != 0) {
							td.setProperty(Property.PropertyStyle, style);
						}
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
						String style = "padding:" + String.valueOf(padding[0])
								+ "px 0px" + String.valueOf(padding[2]) + "px;";
						td.setProperty(Property.PropertyStyle, style);
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
					String style2 = "padding: 0px" + String.valueOf(margin[1])
							+ "px 0px" + String.valueOf(margin[3]) + "px;";
					td.setProperty(Property.PropertyStyleHeight, style2);
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
		if (fitHeight) {
			StringWriter layoutAdd = new StringWriter();
			layoutAdd.append(app.getJavaScriptClass()).append(
					".layouts.add(new Wt3_1_11.StdLayout( Wt3_1_11, '").append(
					div.getId()).append("', { stretch: [");
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
		}
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
					WApplication.getInstance().log("error").append(
							"WGridLayout: unrecognized hint value '").append(
							value).append("' for '").append(name).append("'");
				}
			}
		} else {
			WApplication.getInstance().log("error").append(
					"WGridLayout: unrecognized hint '").append(name)
					.append("'");
		}
	}

	public static String getChildrenResizeJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WtResize.js", wtjs10());
		return "Wt3_1_11.ChildrenResize";
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
				"function(b,r,j){var o=this;this.getId=function(){return r};this.WT=b;this.marginH=function(a){var c=a.parentNode,d=0;if(!b.boxSizing(a)){d=b.px(a,\"marginLeft\");d+=b.px(a,\"marginRight\");d+=b.px(a,\"borderLeftWidth\");d+=b.px(a,\"borderRightWidth\");d+=b.px(a,\"paddingLeft\");d+=b.px(a,\"paddingRight\")}d+=b.pxself(c,\"paddingLeft\");d+=b.pxself(c,\"paddingRight\");return d};this.marginV=function(a){var c=b.px(a,\"marginTop\");c+=b.px(a,\"marginBottom\");if(!b.boxSizing(a)){c+= b.px(a,\"borderTopWidth\");c+=b.px(a,\"borderBottomWidth\");c+=b.px(a,\"paddingTop\");c+=b.px(a,\"paddingBottom\")}return c};this.getColumn=function(a){var c,d,i,h=b.getElement(r).firstChild.childNodes;d=c=0;for(i=h.length;d<i;d++){var e=h[d];if(b.hasTag(e,\"COLGROUP\")){d=-1;h=e.childNodes;i=h.length}if(b.hasTag(e,\"COL\"))if(e.className!=\"Wt-vrh\")if(c==a)return e;else++c}return null};this.adjustCell=function(a,c){var d=c==0;c-=b.pxself(a,\"paddingTop\");c-=b.pxself(a,\"paddingBottom\");if(c<=0)c=0;a.style.height= c+\"px\";if(!(a.style.verticalAlign||a.childNodes.length==0)){a=a.childNodes[0];if(c<=0)c=0;if(a.className==\"Wt-hcenter\"){a.style.height=c+\"px\";a=a.firstChild.firstChild;if(!b.hasTag(a,\"TD\"))a=a.firstChild;if(a.style.height!=c+\"px\")a.style.height=c+\"px\";a=a.firstChild}c-=this.marginV(a);if(c<=0)c=0;if(!b.hasTag(a,\"TABLE\")){var i,h;if(!d){i=a.parentNode;h=i.offsetWidth-o.marginH(a);if(i.className==\"Wt-chwrap\"&&h>0){if(!b.isIE){a.style.position=\"relative\";a=a.firstChild}a.style.width=h+\"px\";a.style.position= \"absolute\"}}if(!d&&a.wtResize)a.wtResize(a,h,c);else if(a.style.height!=c+\"px\"){a.style.height=c+\"px\";if(a.className==\"Wt-wrapdiv\")if(b.isIE&&b.hasTag(a.firstChild,\"TEXTAREA\"))a.firstChild.style.height=c-b.pxself(a,\"marginBottom\")+\"px\"}}}};this.adjustRow=function(a,c){var d=[];if(a.style.height!=c+\"px\")a.style.height=c+\"px\";a=a.childNodes;var i,h,e,g;i=0;g=-1;for(h=a.length;i<h;++i){e=a[i];e.className!=\"Wt-vrh\"&&++g;if(e.rowSpan!=1){this.adjustCell(e,0,g);d.push({td:e,col:g})}else this.adjustCell(e, c,g)}return d};this.adjust=function(){var a=b.getElement(r);if(!a)return false;o.initResize&&o.initResize(b,r,j);if(b.isHidden(a))return true;var c=a.firstChild,d=a.parentNode;if(c.style.height!==\"\")c.style.height=\"\";var i=b.pxself(d,\"height\"),h=false;if(i===0){h=true;i=d.clientHeight}var e=d.clientWidth;if(!(a.dirty||c.w!==e||c.h!==i))return true;c.w=e;c.h=i;a.dirty=null;var g=i;if(h){g-=b.px(d,\"paddingTop\");g-=b.px(d,\"paddingBottom\")}else if(b.boxSizing(d)){g-=b.px(d,\"borderTopWidth\");g-=b.px(d, \"borderBottomWidth\");g-=b.px(d,\"paddingTop\");g-=b.px(d,\"paddingBottom\")}g-=b.px(a,\"marginTop\");g-=b.px(a,\"marginBottom\");if(d.children&&d.children.length!==1){h=0;for(i=d.children.length;h<i;++h){e=d.children[h];if(e!==a)g-=$(e).outerHeight()}}a=d=0;var p,n;i=c.rows.length;for(p=h=0;h<i;h++){e=c.rows[h];if(e.className)g-=e.offsetHeight;else{a+=j.minheight[p];if(j.stretch[p]<=0)g-=e.offsetHeight;else d+=j.stretch[p];++p}}g=g>a?g:a;a=[];if(d!==0&&g>0){var s=g,t;for(p=h=0;h<i;h++){e=c.rows[h];if(!e.className){n= j.stretch[p];if(n!==0){if(n!==-1){t=j.minheight[p];n=g*n/d;n=s>n?n:s;n=Math.round(t>n?t:n);s-=n}else n=e.offsetHeight;b.addAll(a,this.adjustRow(e,n))}++p}}}h=0;for(i=a.length;h<i;++h){e=a[h].td;g=a[h].col;this.adjustCell(e,e.offsetHeight,g)}if(c.style.tableLayout!==\"fixed\")return true;p=0;s=c.childNodes;var z,A,x;a=0;for(d=s.length;a<d;a++){g=s[a];if(b.hasTag(g,\"COLGROUP\")){a=-1;s=g.childNodes;d=s.length}if(b.hasTag(g,\"COL\")){if(b.pctself(g,\"width\")===0){h=t=0;for(i=c.rows.length;h<i;h++){e=c.rows[h]; n=e.childNodes;A=z=0;for(x=n.length;A<x;A++){e=n[A];if(e.colSpan===1&&z===p&&e.childNodes.length===1){e=e.firstChild;e=e.offsetWidth+o.marginH(e);t=Math.max(t,e);break}z+=e.colSpan;if(z>p)break}}if(t>0&&b.pxself(g,\"width\")!==t)g.style.width=t+\"px\"}++p}}return true};this.contains=function(a){var c=b.getElement(r);a=b.getElement(a.getId());return c&&a?b.contains(c,a):false}}");
	}

	static WJavaScriptPreamble wtjs2() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptPrototype,
				"StdLayout.prototype.initResize",
				"function(b,r,j){function o(f,k){if(f.offsetWidth>0)return f.offsetWidth;else{f=n.firstChild.rows[0];var l,m,q,u;q=m=0;for(u=f.childNodes.length;m<u;++m){l=f.childNodes[m];if(l.className!=\"Wt-vrh\"){if(q==k)return l.offsetWidth;q+=l.colSpan}}return 0}}function a(f,k){var l=b.getElement(r).firstChild;p(f).style.width=k+\"px\";var m,q,u,w;q=m=0;for(u=l.rows.length;m<u;m++){w=l.rows[m];if(!w.className){var v,B,C,F;C=B=0;for(F=w.childNodes.length;B< F;++B){v=w.childNodes[B];if(v.className!=\"Wt-vrh\"){if(v.colSpan==1&&C==f&&v.childNodes.length>0){w=v.firstChild;v=Math.max(1,k-g.marginH(w));w.style.width=v+\"px\";break}C+=v.colSpan}}++q}}}function c(f,k,l){var m=f.firstChild;new b.SizeHandle(b,\"v\",m.offsetHeight,m.offsetWidth,-f.parentNode.previousSibling.offsetHeight,f.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(q){i(f,k,q)},m,n,l,0,0)}function d(f,k,l){var m=-f.previousSibling.offsetWidth,q=f.nextSibling.offsetWidth,u=f.firstChild,w=b.pxself(s.rows[0].childNodes[0], \"paddingTop\"),v=b.pxself(s.rows[s.rows.length-1].childNodes[0],\"paddingBottom\");v=s.offsetHeight-w-v;if($(document.body).hasClass(\"Wt-rtl\")){var B=m;m=-q;q=-B}new b.SizeHandle(b,\"h\",u.offsetWidth,v,m,q,\"Wt-hsh\",function(C){e(f,k,C)},u,n,l,0,-f.offsetTop+w-b.pxself(f,\"paddingTop\"))}function i(f,k,l){var m=f.parentNode.previousSibling;f=f.parentNode.nextSibling;var q=m.offsetHeight,u=f.offsetHeight;if(j.stretch[k]>0&&j.stretch[k+1]>0)j.stretch[k]=-1;if(j.stretch[k+1]==0)j.stretch[k+1]=-1;j.stretch[k]<= 0&&g.adjustRow(m,q+l);j.stretch[k+1]<=0&&g.adjustRow(f,u-l);b.getElement(r).dirty=true;window.onresize()}function h(){var f,k=0;for(f=0;;++f){var l=p(f);if(l)k+=b.pctself(l,\"width\");else break}if(k!=0)for(f=0;;++f)if(l=p(f)){var m=b.pctself(l,\"width\");if(m)l.style.width=m*100/k+\"%\"}else break}function e(f,k,l){f=p(k);var m=o(f,k),q=p(k+1),u=o(q,k+1);if($(document.body).hasClass(\"Wt-rtl\"))l=-l;if(b.pctself(f,\"width\")>0&&b.pctself(q,\"width\")>0){f.style.width=\"\";h()}if(b.pctself(f,\"width\")==0)a(k,m+ l);else b.getElement(r).dirty=true;if(b.pctself(q,\"width\")==0)a(k+1,u-l);else b.getElement(r).dirty=true;window.onresize()}var g=this,p=g.getColumn,n=b.getElement(r);if(n)if(!g.resizeInitialized){var s=n.firstChild,t,z,A,x;z=t=0;for(A=s.rows.length;t<A;t++){x=s.rows[t];if(x.className){if(x.className===\"Wt-hrh\"){var y=x.firstChild;y.ri=z-1;y.onmousedown=y.ontouchstart=function(f){c(this,this.ri,f||window.event)}}}else{var D,E,G;E=D=0;for(G=x.childNodes.length;D<G;++D){y=x.childNodes[D];if(y.className== \"Wt-vrh\"){y.ci=E-1;y.onmousedown=y.ontouchstart=function(f){d(this,this.ci,f||window.event)}}else E+=y.colSpan}++z}}g.resizeInitialized=true}}");
	}

	static WJavaScriptPreamble appjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.ApplicationScope,
				JavaScriptObjectType.JavaScriptObject,
				"layouts",
				"new (function(){var b=[],r=false;this.add=function(j){var o,a;o=0;for(a=b.length;o<a;++o){var c=b[o];if(c.getId()==j.getId()){b[o]=j;return}else if(j.contains(c)){b.splice(o,0,j);return}}b.push(j)};this.adjust=function(j){if(j){if(j=$(\"#\"+j).get(0))j.dirty=true}else if(!r){r=true;for(var o=0;o<b.length;++o){j=b[o];if(!j.adjust()){j.WT.arrayRemove(b,o);--o}}r=false}}})");
	}

	static WJavaScriptPreamble wtjs10() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptFunction,
				"ChildrenResize",
				"function(b,d,c){var a,f,e;a=this;b.style.height=c+\"px\";if(a.boxSizing(b)){c-=a.px(b,\"marginTop\");c-=a.px(b,\"marginBottom\");c-=a.px(b,\"borderTopWidth\");c-=a.px(b,\"borderBottomWidth\");c-=a.px(b,\"paddingTop\");c-=a.px(b,\"paddingBottom\");d-=a.px(b,\"marginLeft\");d-=a.px(b,\"marginRight\");d-=a.px(b,\"borderLeftWidth\");d-=a.px(b,\"borderRightWidth\");d-=a.px(b,\"paddingLeft\");d-=a.px(b,\"paddingRight\")}var g=c+\"px\";a=0;for(f=b.childNodes.length;a<f;++a){e= b.childNodes[a];if(e.nodeType==1)if(e.wtResize)e.wtResize(e,d,c);else if(e.style.height!=g)e.style.height=g}}");
	}
}
