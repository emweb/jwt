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
			app.doJavaScript(wtjs1(app), false);
			app.doJavaScript(appjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
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
		if (hasResizeHandles
				&& !app.isJavaScriptLoaded("js/StdGridLayoutImpl-resize.js")) {
			SizeHandle.loadJavaScript(app);
			app.doJavaScript(wtjs2(app), false);
			app.setJavaScriptLoaded("js/StdGridLayoutImpl-resize.js");
		}
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
		div.setProperty(Property.PropertyStylePosition, "relative");
		String divStyle = "";
		if (fitHeight && !app.getEnvironment().agentIsIElt(9)) {
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
					style = "table-layout: fixed;";
				}
				style += "width: 100%;";
			}
			if (fitHeight) {
				style += "height: 100%;";
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
		for (int row = 0; row < rowCount; ++row) {
			boolean resizeHandleBelow = row < rowCount - 1
					&& this.grid_.rows_.get(row).resizable_;
			DomElement tr = DomElement.createNew(DomElementType.DomElement_TR);
			String heightPct = "";
			int stretch = Math.max(0, this.grid_.rows_.get(row).stretch_);
			if (stretch != 0 || fitHeight && totalRowStretch == 0) {
				int pct = totalRowStretch == 0 ? 100 / rowCount : 100 * stretch
						/ totalRowStretch;
				StringWriter style = new StringWriter();
				style.append("height: ").append(String.valueOf(pct)).append(
						"%;");
				heightPct = style.toString();
				tr.setProperty(Property.PropertyStyle, heightPct);
			}
			boolean resizeHandleLeft = false;
			for (int col = 0; col < colCount; ++col) {
				boolean resizeHandleRight = col < colCount - 1
						&& this.grid_.columns_.get(col - 1
								+ this.grid_.items_.get(row).get(col).colSpan_).resizable_;
				if (!overSpanned.get(row * colCount + col)) {
					Grid.Item item = this.grid_.items_.get(row).get(col);
					boolean itemFitWidth = item.colSpan_ == (int) colCount
							|| totalColStretch == 0;
					boolean itemFitHeight = item.rowSpan_ == (int) rowCount
							|| totalRowStretch == 0;
					int colSpan = 0;
					for (int i = 0; i < item.rowSpan_; ++i) {
						if (this.grid_.rows_.get(row + i).stretch_ != 0) {
							itemFitHeight = true;
						} else {
							if (!(stretch != 0)) {
								itemFitHeight = false;
							}
						}
						colSpan = item.colSpan_;
						for (int j = 0; j < item.colSpan_; ++j) {
							if (this.grid_.columns_.get(col + j).stretch_ != 0) {
								itemFitWidth = true;
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
					if (row == 0) {
						padding[0] = margin[0];
					} else {
						if (!resizeHandleAbove) {
							padding[0] = (this.grid_.verticalSpacing_ + 1) / 2;
						}
					}
					if (row + item.rowSpan_ == rowCount) {
						padding[2] = margin[2];
					} else {
						if (!resizeHandleBelow) {
							padding[2] = this.grid_.verticalSpacing_ / 2;
						}
					}
					if (col == 0) {
						padding[3] = margin[3];
					} else {
						if (!resizeHandleLeft) {
							padding[3] = (this.grid_.horizontalSpacing_ + 1) / 2;
						}
					}
					if (col + item.colSpan_ == colCount) {
						padding[1] = margin[1];
					} else {
						if (!resizeHandleRight) {
							padding[1] = this.grid_.horizontalSpacing_ / 2;
						}
					}
					DomElement td = DomElement
							.createNew(DomElementType.DomElement_TD);
					if (app.getEnvironment().agentIsIElt(9)) {
						td.setProperty(Property.PropertyStylePosition,
								"relative");
					}
					if (item.item_ != null) {
						DomElement c = getImpl(item.item_).createDomElement(
								itemFitWidth, itemFitHeight, app);
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
						td.addChild(c);
					}
					{
						String style = "";
						if (vAlign == null) {
							style += heightPct;
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
					td.setProperty(Property.PropertyStyleOverflowX, "hidden");
					tr.addChild(td);
					if (resizeHandleRight) {
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
				}
				resizeHandleLeft = resizeHandleRight;
			}
			tbody.addChild(tr);
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
			resizeHandleAbove = resizeHandleBelow;
		}
		table.addChild(tbody);
		if (fitHeight) {
			StringWriter layoutAdd = new StringWriter();
			layoutAdd.append(app.getJavaScriptClass()).append(
					".layouts.add(new Wt3_1_8.StdLayout( Wt3_1_8, '").append(
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
		String THIS_JS = "js/WtResize.js";
		WApplication app = WApplication.getInstance();
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs10(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
		return "Wt3_1_8.ChildrenResize";
	}

	public boolean itemResized(WLayoutItem item) {
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

	static String wtjs1(WApplication app) {
		return "Wt3_1_8.StdLayout = function(a,s,i){var o=this;this.getId=function(){return s};this.WT=a;this.marginH=function(b){var c=b.parentNode,f=a.px(b,\"marginLeft\");f+=a.px(b,\"marginRight\");f+=a.px(b,\"borderLeftWidth\");f+=a.px(b,\"borderRightWidth\");f+=a.px(b,\"paddingLeft\");f+=a.px(b,\"paddingRight\");f+=a.pxself(c,\"paddingLeft\");f+=a.pxself(c,\"paddingRight\");return f};this.marginV=function(b){var c=a.px(b,\"marginTop\");c+=a.px(b,\"marginBottom\");c+=a.px(b,\"borderTopWidth\");c+=a.px(b,\"borderBottomWidth\"); c+=a.px(b,\"paddingTop\");c+=a.px(b,\"paddingBottom\");return c};this.getColumn=function(b){var c,f,g,d=a.getElement(s).firstChild.childNodes;f=c=0;for(g=d.length;f<g;f++){var k=d[f];if(a.hasTag(k,\"COLGROUP\")){f=-1;d=k.childNodes;g=d.length}if(a.hasTag(k,\"COL\"))if(k.className!=\"Wt-vrh\")if(c==b)return k;else++c}return null};this.adjustCell=function(b,c,f){var g=c==0;c-=a.pxself(b,\"paddingTop\");c-=a.pxself(b,\"paddingBottom\");if(c<=0)c=0;b.style.height=c+\"px\";if(!(b.style.verticalAlign||b.childNodes.length== 0)){var d=b.childNodes[0];if(c<=0)c=0;if(d.className==\"Wt-hcenter\"){d.style.height=c+\"px\";d=d.firstChild.firstChild;if(!a.hasTag(d,\"TD\"))d=d.firstChild;if(d.style.height!=c+\"px\")d.style.height=c+\"px\";d=d.firstChild}if(b.childNodes.length==1)c-=this.marginV(d);if(c<=0)c=0;if(!a.hasTag(d,\"TABLE\"))if(!g&&d.wtResize){b=d.parentNode.offsetWidth-o.marginH(d);if(o.getColumn(f).style.width!=\"\"){d.style.position=\"absolute\";d.style.width=b+\"px\"}d.wtResize(d,b,c)}else if(d.style.height!=c+\"px\"){d.style.height= c+\"px\";if(d.className==\"Wt-wrapdiv\")if(a.isIE&&a.hasTag(d.firstChild,\"TEXTAREA\"))d.firstChild.style.height=c-a.pxself(d,\"marginBottom\")+\"px\"}}};this.adjustRow=function(b,c){var f=[];if(b.style.height!=c+\"px\")b.style.height=c+\"px\";b=b.childNodes;var g,d,k,h;g=0;h=-1;for(d=b.length;g<d;++g){k=b[g];k.className!=\"Wt-vrh\"&&++h;if(k.rowSpan!=1){this.adjustCell(k,0,h);f.push({td:k,col:h})}else this.adjustCell(k,c,h)}return f};this.adjust=function(){var b=a.getElement(s);if(!b)return false;o.initResize&& o.initResize(a,s,i);if(a.isHidden(b))return true;var c=b.firstChild,f=b.parentNode;if(c.style.height!=\"\")c.style.height=\"\";if(!(b.dirty||c.w!=f.clientWidth||c.h!=f.clientHeight))return true;b.dirty=null;var g=a.pxself(f,\"height\");if(g==0){g=f.clientHeight;g-=a.px(f,\"paddingTop\");g-=a.px(f,\"paddingBottom\")}g-=a.px(b,\"marginTop\");g-=a.px(b,\"marginBottom\");var d,k;if(f.children){d=0;for(k=f.children.length;d<k;++d){var h=f.children[d];if(h!=b)g-=$(h).outerHeight()}}var t=0;b=0;var l,p;l=d=0;for(k=c.rows.length;d< k;d++){h=c.rows[d];if(h.className==\"Wt-hrh\")g-=h.offsetHeight;else{b+=i.minheight[l];if(i.stretch[l]<=0)g-=h.offsetHeight;else t+=i.stretch[l];++l}}g=g>b?g:b;b=[];if(t!=0&&g>0){var r=g;l=d=0;for(k=c.rows.length;d<k;d++)if(c.rows[d].className!=\"Wt-hrh\"){h=c.rows[d];if(i.stretch[l]!=0){if(i.stretch[l]!=-1){p=g*i.stretch[l]/t;p=r>p?p:r;p=Math.round(i.minheight[l]>p?i.minheight[l]:p);r-=p}else p=h.offsetHeight;a.addAll(b,this.adjustRow(h,p))}++l}}d=0;for(k=b.length;d<k;++d){h=b[d].td;g=b[d].col;this.adjustCell(h, h.offsetHeight,g)}c.w=f.clientWidth;c.h=f.clientHeight;if(c.style.tableLayout!=\"fixed\")return true;t=0;l=c.childNodes;f=0;for(b=l.length;f<b;f++){g=l[f];var y,A,z;if(a.hasTag(g,\"COLGROUP\")){f=-1;l=g.childNodes;b=l.length}if(a.hasTag(g,\"COL\")){if(a.pctself(g,\"width\")==0){d=r=0;for(k=c.rows.length;d<k;d++){h=c.rows[d];p=h.childNodes;A=y=0;for(z=p.length;A<z;A++){h=p[A];if(h.colSpan==1&&y==t&&h.childNodes.length==1){h=h.firstChild;h=h.offsetWidth+o.marginH(h);r=Math.max(r,h);break}y+=h.colSpan;if(y> t)break}}if(r>0&&a.pxself(g,\"width\")!=r)g.style.width=r+\"px\"}++t}}return true};this.contains=function(b){var c=a.getElement(s);b=a.getElement(b.getId());return c&&b?a.contains(c,b):false};this.adjust()};";
	}

	static String wtjs2(WApplication app) {
		return "Wt3_1_8.StdLayout.prototype.initResize = function(a,s,i){function o(e,j){if(e.offsetWidth>0)return e.offsetWidth;else{e=l.firstChild.rows[0];var m,n,q,u;q=n=0;for(u=e.childNodes.length;n<u;++n){m=e.childNodes[n];if(m.className!=\"Wt-vrh\"){if(q==j)return m.offsetWidth;q+=m.colSpan}}return 0}}function b(e,j){var m=a.getElement(s).firstChild;t(e).style.width=j+\"px\";var n,q,u,v;q=n=0;for(u=m.rows.length;n<u;n++){v=m.rows[n];if(v.className!=\"Wt-hrh\"){var w,B,D,F;D=B=0;for(F=v.childNodes.length;B< F;++B){w=v.childNodes[B];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&D==e&&w.childNodes.length==1){v=w.firstChild;w=j-h.marginH(v);v.style.width=w+\"px\";break}D+=w.colSpan}}++q}}}function c(e,j,m){var n=e.firstChild;new a.SizeHandle(a,\"v\",n.offsetHeight,n.offsetWidth,-e.parentNode.previousSibling.offsetHeight,e.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(q){g(e,j,q)},n,l,m,0,0)}function f(e,j,m){var n=-e.previousSibling.offsetWidth,q=e.nextSibling.offsetWidth,u=e.firstChild,v=a.pxself(p.rows[0].childNodes[0], \"paddingTop\"),w=a.pxself(p.rows[p.rows.length-1].childNodes[0],\"paddingBottom\");new a.SizeHandle(a,\"h\",u.offsetWidth,p.offsetHeight-v-w,n,q,\"Wt-hsh\",function(B){k(e,j,B)},u,l,m,0,-e.offsetTop+v-a.pxself(e,\"paddingTop\"))}function g(e,j,m){var n=e.parentNode.previousSibling;e=e.parentNode.nextSibling;var q=n.offsetHeight,u=e.offsetHeight;if(i.stretch[j]>0&&i.stretch[j+1]>0)i.stretch[j]=-1;if(i.stretch[j+1]==0)i.stretch[j+1]=-1;i.stretch[j]<=0&&h.adjustRow(n,q+m);i.stretch[j+1]<=0&&h.adjustRow(e,u-m); a.getElement(s).dirty=true;window.onresize()}function d(){var e,j=0;for(e=0;;++e){var m=t(e);if(m)j+=a.pctself(m,\"width\");else break}if(j!=0)for(e=0;;++e)if(m=t(e)){var n=a.pctself(m,\"width\");if(n)m.style.width=n*100/j+\"%\"}else break}function k(e,j,m){e=t(j);var n=o(e,j),q=t(j+1),u=o(q,j+1);if(a.pctself(e,\"width\")>0&&a.pctself(q,\"width\")>0){e.style.width=\"\";d()}a.pctself(e,\"width\")==0&&b(j,n+m);a.pctself(q,\"width\")==0&&b(j+1,u-m);window.onresize()}var h=this,t=h.getColumn,l=a.getElement(s);if(l)if(!h.resizeInitialized){var p= l.firstChild,r,y,A,z;y=r=0;for(A=p.rows.length;r<A;r++){z=p.rows[r];if(z.className==\"Wt-hrh\"){var x=z.firstChild;x.ri=y-1;x.onmousedown=x.ontouchstart=function(e){c(this,this.ri,e||window.event)}}else{var C,E,G;E=C=0;for(G=z.childNodes.length;C<G;++C){x=z.childNodes[C];if(x.className==\"Wt-vrh\"){x.ci=E-1;x.onmousedown=x.ontouchstart=function(e){f(this,this.ci,e||window.event)}}else E+=x.colSpan}++y}}h.resizeInitialized=true}};";
	}

	static String appjs1(WApplication app) {
		return app.getJavaScriptClass()
				+ ".layouts = new (function(){var a=[],s=false;this.add=function(i){var o,b;o=0;for(b=a.length;o<b;++o){var c=a[o];if(c.getId()==i.getId()){a[o]=i;return}else if(i.contains(c)){a.splice(o,0,i);return}}a.push(i)};this.adjust=function(i){if(i){if(i=$(\"#\"+i).get(0))i.dirty=true}else if(!s){s=true;for(var o=0;o<a.length;++o){i=a[o];if(!i.adjust()){i.WT.arrayRemove(a,o);--o}}s=false}}});";
	}

	static String wtjs10(WApplication app) {
		return "Wt3_1_8.ChildrenResize = function(b,f,d){var c,e,a;b.style.height=d+\"px\";c=0;for(e=b.childNodes.length;c<e;++c){a=b.childNodes[c];if(a.nodeType==1)if(a.wtResize)a.wtResize(a,f,d);else if(a.style.height!=b.style.height)a.style.height=b.style.height}};";
	}
}
