/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import eu.webtoolkit.jwt.utils.EnumUtils;

class StdGridLayoutImpl extends StdLayoutImpl {
	public StdGridLayoutImpl(WLayout layout, Grid grid) {
		super(layout);
		this.grid_ = grid;
		this.useFixedLayout_ = false;
		String CSS_RULES_NAME = "StdGridLayoutImpl";
		WApplication app = WApplication.getInstance();
		final boolean jsHeights = useJavaScriptHeights(app);
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			app.getStyleSheet().addRule("table.Wt-hcenter",
					"margin: 0px auto;", CSS_RULES_NAME);
			app.doJavaScript(app.getJavaScriptClass() + ".layoutTableObjs=[];",
					false);
			if (jsHeights) {
				app
						.doJavaScript(
								"Wt2_99_2.layoutAdjust=function(w,c,mh){if (Wt2_99_2.isHidden(w))return;var WT=Wt2_99_2,t=w.firstChild;var r=WT.pxself(w.parentNode, 'height');if (r==0) {r=w.parentNode.clientHeight;r+= -WT.px(w.parentNode, 'paddingTop')-WT.px(w.parentNode, 'paddingBottom');}r+= -WT.px(w, 'marginTop')-WT.px(w, 'marginBottom');var ts=0,tmh=0,i, j, il, jl,row, tds, td;for (i=0, il=t.rows.length; i<il; i++) {tmh += mh[i];if (c[i] <= 0)r -= t.rows[i].offsetHeight;else ts += c[i];}r=r>tmh?r:tmh;if (ts!=0 && r>0) {var left=r, h;for (i=0, il=t.rows.length; i<il; i++) {row=t.rows[i];if (c[i] != 0) {if (c[i] != -1) {h=r*c[i]/ts;h=left>h?h:left;h=Math.round(mh[i]>h?mh[i]:h);left -= h;} else {h=row.offsetHeight;}if (row.style.height!=h+'px'){row.style.height=h+'px';tds=row.childNodes;for (j=0, jl=tds.length; j<jl; ++j){td=tds[j];var k=h-WT.pxself(td, 'paddingTop')-WT.pxself(td, 'paddingBottom');if (k <= 0) k=0;td.style.height= k+'px';if (td.style['verticalAlign']|| td.childNodes.length == 0) continue;var ch=td.childNodes[0];if (k <= 0) k=0;if (ch.className=='Wt-hcenter'){ch.style.height= k+'px';var itd=ch.firstChild.firstChild;if (!WT.hasTag(itd, 'TD'))itd=itd.firstChild;if (itd.style.height!=k+'px')itd.style.height=k+'px';ch=itd.firstChild;}if (td.childNodes.length==1)k += -WT.px(ch, 'marginTop')-WT.px(ch, 'marginBottom')-WT.px(ch, 'borderTopWidth')-WT.px(ch, 'borderBottomWidth')-WT.px(ch, 'paddingTop')-WT.px(ch, 'paddingBottom');if (k <= 0) k=0;if (WT.hasTag(ch, 'BUTTON')|| WT.hasTag(ch, 'INPUT')|| WT.hasTag(ch, 'SELECT')|| WT.hasTag(ch, 'TABLE'))continue;if (ch.style.height != k+'px') {if (ch.wtSetHeight) ch.wtSetHeight(ch, k);else ch.style.height = k+'px';}if (td.childNodes.length==1&& WT.hasTag(ch, 'TEXTAREA')) {"
										+ (app.getEnvironment().agentIsOpera() ? "if (k <= 6) k=6;ch.style.height = (k-6) + 'px';td.style.marginLeft = '-1px';"
												: "if (k <= 8) k=8;ch.style.height = (k-8) + 'px';td.style.marginLeft = '-1px';td.style.marginTop = '-1px';")
										+ "}}}}}}if (t.style.tableLayout != 'fixed')return;var jc=0, chn=t.childNodes;for (j=0, jl=chn.length; j<jl; j++) {var col=chn[j], chw, mw,c, ci, cil;if (WT.hasTag(col, 'COLGROUP')) {j=-1;chn=col.childNodes;jl=chn.length;}if (!WT.hasTag(col, 'COL'))continue;if (WT.pctself(col, 'width') == 0) {mw=0;for (i=0, il=t.rows.length; i<il; i++) {row=t.rows[i];tds=row.childNodes;c=0;for (ci=0, cil=tds.length; ci<cil; ci++) {td=tds[ci];if (td.colSpan==1 && c==jc && td.childNodes.length==1) {ch=td.firstChild;w=ch.offsetWidth+WT.px(ch, 'marginLeft')+WT.px(ch, 'marginRight')+WT.px(td, 'paddingLeft')+WT.px(td, 'paddingRight');mw=Math.max(mw, w);break;}c += td.colSpan;if (c>jc) break;}}if (mw>0 && WT.pxself(col, 'width') != mw)col.style.width=mw+'px';}++jc;}};",
								false);
				app
						.declareJavaScriptFunction(
								"layoutsAdjust",
								"function(){var a="
										+ app.getJavaScriptClass()
										+ ".layoutTableObjs;var i;for(i=0;i<a.length;++i){var id=a[i][0];var c=a[i][1];var mh=a[i][2];var w=Wt2_99_2.getElement(id);if(!w){Wt2_99_2.arrayRemove(a, i);--i;}else{Wt2_99_2.layoutAdjust(w,c,mh);}}}");
				app.addAutoJavaScript(app.getJavaScriptClass()
						+ ".layoutsAdjust();");
			}
		}
	}

	public void destroy() {
		WApplication app = WApplication.getInstance();
		if (this.getParentLayoutImpl() == null) {
			if (this.getContainer() == app.getRoot()) {
				app.setBodyClass("");
				app.setHtmlClass("");
			}
			if (app.getEnvironment().agentIsIE()) {
				this.getContainer().setOverflow(
						WContainerWidget.Overflow.OverflowVisible);
			}
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
		final int colCount = this.grid_.columns_.size();
		final int rowCount = this.grid_.rows_.size();
		int totalColStretch = 0;
		for (int col = 0; col < colCount; ++col) {
			totalColStretch += Math.max(0,
					this.grid_.columns_.get(col).stretch_);
		}
		int totalRowStretch = 0;
		for (int row = 0; row < rowCount; ++row) {
			totalRowStretch += Math.max(0, this.grid_.rows_.get(row).stretch_);
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
		String divStyle = "";
		if (fitHeight && !app.getEnvironment().agentIsIE()) {
			divStyle += "height: 100%;";
		}
		if (divStyle.length() != 0) {
			div.setAttribute("style", divStyle);
		}
		DomElement table = DomElement
				.createNew(DomElementType.DomElement_TABLE);
		final boolean jsHeights = useJavaScriptHeights(app);
		String style = "";
		if (fitWidth) {
			if (this.useFixedLayout_) {
				style = "table-layout: fixed;";
			}
			style += "width: 100%;";
		}
		if (!jsHeights && fitHeight) {
			style += "height: 100%;";
		}
		table.setAttribute("style", style);
		if (jsHeights && fitHeight) {
			StringWriter layoutAdd = new StringWriter();
			layoutAdd.append(app.getJavaScriptClass()).append(
					".layoutTableObjs.push(['").append(div.getId()).append(
					"',[");
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
			layoutAdd.append("],[");
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
			layoutAdd.append("]]);");
			app.doJavaScript(layoutAdd.toString());
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
					c.setAttribute("style", "width:" + String.valueOf(pct)
							+ "%;");
				}
				table.addChild(c);
			}
		}
		List<Boolean> overSpanned = new ArrayList<Boolean>();
		{
			int insertPos = 0;
			for (int ii = 0; ii < colCount * rowCount; ++ii)
				overSpanned.add(insertPos + ii, false);
		}
		;
		for (int row = 0; row < rowCount; ++row) {
			String heightPct = "";
			DomElement tr = DomElement.createNew(DomElementType.DomElement_TR);
			int stretch = Math.max(0, this.grid_.rows_.get(row).stretch_);
			if (stretch != 0 || !jsHeights && fitHeight && totalRowStretch == 0) {
				int pct = totalRowStretch == 0 ? 100 / rowCount : 100 * stretch
						/ totalRowStretch;
				StringWriter style2 = new StringWriter();
				style2.append("height: ").append(String.valueOf(pct)).append(
						"%;");
				heightPct = style2.toString();
				tr.setAttribute("style", heightPct);
			}
			for (int col = 0; col < colCount; ++col) {
				if (!overSpanned.get(row * colCount + col)) {
					Grid.Item item = this.grid_.items_.get(row).get(col);
					boolean itemFitWidth = item.colSpan_ == (int) colCount
							|| totalColStretch == 0;
					boolean itemFitHeight = item.rowSpan_ == (int) rowCount
							|| totalRowStretch == 0;
					for (int i = 0; i < item.rowSpan_; ++i) {
						if (this.grid_.rows_.get(row + i).stretch_ != 0) {
							itemFitHeight = true;
						}
						for (int j = 0; j < item.colSpan_; ++j) {
							if (this.grid_.columns_.get(col + j).stretch_ != 0) {
								itemFitWidth = true;
							}
							if (i + j > 0) {
								overSpanned.set((row + i) * colCount + col + j,
										true);
							}
						}
					}
					AlignmentFlag hAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignHorizontalMask));
					AlignmentFlag vAlign = EnumUtils.enumFromSet(EnumUtils
							.mask(item.alignment_,
									AlignmentFlag.AlignVerticalMask));
					if (hAlign != null) {
						itemFitWidth = false;
					}
					if (vAlign != null) {
						itemFitHeight = false;
					}
					int[] padding = { 0, 0, 0, 0 };
					if (row == 0) {
						padding[0] = margin[0];
					} else {
						padding[0] = (this.grid_.verticalSpacing_ + 1) / 2;
					}
					if (row + item.rowSpan_ == rowCount) {
						padding[2] = margin[2];
					} else {
						padding[2] = this.grid_.verticalSpacing_ / 2;
					}
					padding[1] = padding[3] = 0;
					if (col == 0) {
						padding[3] = margin[3];
					} else {
						padding[3] = (this.grid_.horizontalSpacing_ + 1) / 2;
					}
					if (col + item.colSpan_ == colCount) {
						padding[1] = margin[1];
					} else {
						padding[1] = this.grid_.horizontalSpacing_ / 2;
					}
					DomElement td = DomElement
							.createNew(DomElementType.DomElement_TD);
					if (!jsHeights) {
						td.setAttribute("class", "Wt-grtd");
					}
					int additionalVerticalPadding = 0;
					if (item.item_ != null) {
						DomElement c = getImpl(item.item_).createDomElement(
								itemFitWidth, itemFitHeight, app);
						additionalVerticalPadding = getImpl(item.item_)
								.getAdditionalVerticalPadding(itemFitWidth,
										itemFitHeight);
						if (hAlign == null) {
							hAlign = AlignmentFlag.AlignJustify;
						}
						switch (hAlign) {
						case AlignCenter: {
							DomElement itable = DomElement
									.createNew(DomElementType.DomElement_TABLE);
							itable.setAttribute("class", "Wt-hcenter");
							if (vAlign == null && !jsHeights) {
								itable.setAttribute("style", "height:100%;");
							}
							DomElement irow = DomElement
									.createNew(DomElementType.DomElement_TR);
							DomElement itd = DomElement
									.createNew(DomElementType.DomElement_TD);
							if (!jsHeights) {
								itd.setAttribute("class", "Wt-grtd");
							}
							if (vAlign == null) {
								itd.setAttribute("style", "height:100%;");
							}
							itd.addChild(c);
							irow.addChild(itd);
							itable.addChild(irow);
							c = itable;
							break;
						}
						case AlignRight:
							c.setProperty(Property.PropertyStyleFloat, "right");
							break;
						case AlignLeft:
							c.setProperty(Property.PropertyStyleFloat, "left");
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
						td.addChild(c);
					}
					String style2 = "";
					if (!jsHeights && vAlign == null) {
						style2 += heightPct;
					}
					style2 += "overflow:auto;";
					int padding2 = padding[2] + additionalVerticalPadding;
					if (padding[0] == padding[1] && padding[0] == padding2
							&& padding[0] == padding[3]) {
						if (padding[0] != 0) {
							style2 += "padding:" + String.valueOf(padding[0])
									+ "px;";
						}
					} else {
						style2 += "padding:" + String.valueOf(padding[0])
								+ "px " + String.valueOf(padding[1]) + "px "
								+ String.valueOf(padding[2]) + "px "
								+ String.valueOf(padding[3]) + "px;";
					}
					if (vAlign != null) {
						switch (vAlign) {
						case AlignTop:
							style2 += "vertical-align:top;";
							break;
						case AlignMiddle:
							style2 += "vertical-align:middle;";
							break;
						case AlignBottom:
							style2 += "vertical-align:bottom;";
						default:
							break;
						}
					}
					if (style2.length() != 0) {
						td.setAttribute("style", style2);
					}
					if (item.rowSpan_ != 1) {
						td.setAttribute("rowspan", String
								.valueOf(item.rowSpan_));
					}
					if (item.colSpan_ != 1) {
						td.setAttribute("colspan", String
								.valueOf(item.colSpan_));
					}
					tr.addChild(td);
				}
			}
			tbody.addChild(tr);
		}
		table.addChild(tbody);
		div.addChild(table);
		return div;
	}

	public int getAdditionalVerticalPadding(boolean fitWidth, boolean fitHeight) {
		return 0;
	}

	public static boolean useJavaScriptHeights(WApplication app) {
		return true;
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

	protected void containerAddWidgets(WContainerWidget container) {
		super.containerAddWidgets(container);
		WApplication app = WApplication.getInstance();
		if (this.getParentLayoutImpl() == null) {
			if (container == app.getRoot()) {
				app.setBodyClass("Wt-layout");
				app.setHtmlClass("Wt-layout");
			}
			if (app.getEnvironment().agentIsIE()) {
				container.setOverflow(WContainerWidget.Overflow.OverflowAuto);
			}
		}
	}

	private Grid grid_;
	private boolean useFixedLayout_;
}
