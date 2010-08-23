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
		String THIS_JS = "js/StdGridLayoutImpl.js";
		WApplication app = WApplication.getInstance();
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.getStyleSheet().addRule("table.Wt-hcenter",
					"margin: 0px auto;position: relative");
			app.doJavaScript(wtjs1(app), false);
			app.doJavaScript(appjs1(app), false);
			app.doJavaScript(appjs2(app), false);
			app.setJavaScriptLoaded(THIS_JS);
			app.addAutoJavaScript(app.getJavaScriptClass()
					+ ".layoutsAdjust();");
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
		if (fitHeight && !app.getEnvironment().agentIsIE()) {
			divStyle += "height: 100%;";
		}
		if (app.getEnvironment().agentIsIE()) {
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
		if (fitHeight) {
			StringWriter layoutAdd = new StringWriter();
			layoutAdd.append(app.getJavaScriptClass()).append(
					".layouts.push(new Wt3_1_5.StdLayout( Wt3_1_5, '").append(
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
			int[] padding = { 0, 0, 0, 0 };
			if (row == 0) {
				padding[0] = margin[0];
			} else {
				if (!resizeHandleAbove) {
					padding[0] = (this.grid_.verticalSpacing_ + 1) / 2;
				}
			}
			if (row + 1 == rowCount) {
				padding[2] = margin[2];
			} else {
				if (!resizeHandleBelow) {
					padding[2] = this.grid_.verticalSpacing_ / 2;
				}
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
					padding[1] = padding[3] = 0;
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
					if (app.getEnvironment().agentIsIE()) {
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
		div.addChild(table);
		return div;
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
		return "Wt3_1_5.ChildrenResize";
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

	static String wtjs1(WApplication app) {
		return "Wt3_1_5.StdLayout = function(a,A,q){var s=this;this.marginH=function(c){var m=c.parentNode;return a.px(c,\"marginLeft\")+a.px(c,\"marginRight\")+a.px(c,\"borderLeftWidth\")+a.px(c,\"borderRightWidth\")+a.px(m,\"paddingLeft\")+a.px(m,\"paddingRight\")};this.marginV=function(c){return a.px(c,\"marginTop\")+a.px(c,\"marginBottom\")+a.px(c,\"borderTopWidth\")+a.px(c,\"borderBottomWidth\")+a.px(c,\"paddingTop\")+a.px(c,\"paddingBottom\")};this.adjustRow=function(c,m){if(c.style.height!=m+\"px\")c.style.height=m+ \"px\";c=c.childNodes;var i,k,n;i=0;for(k=c.length;i<k;++i){n=c[i];var e=m-a.pxself(n,\"paddingTop\")-a.pxself(n,\"paddingBottom\");if(e<=0)e=0;n.style.height=e+\"px\";if(!(n.style.verticalAlign||n.childNodes.length==0)){var d=n.childNodes[0];if(e<=0)e=0;if(d.className==\"Wt-hcenter\"){d.style.height=e+\"px\";d=d.firstChild.firstChild;if(!a.hasTag(d,\"TD\"))d=d.firstChild;if(d.style.height!=e+\"px\")d.style.height=e+\"px\";d=d.firstChild}if(n.childNodes.length==1)e+=-this.marginV(d);if(e<=0)e=0;if(!a.hasTag(d,\"TABLE\"))if(d.wtResize){n= d.parentNode.offsetWidth-s.marginH(d);d.wtResize(d,n,e)}else if(d.style.height!=e+\"px\"){d.style.height=e+\"px\";if(d.className==\"Wt-wrapdiv\")if(a.isIE&&a.hasTag(d.firstChild,\"TEXTAREA\"))d.firstChild.style.height=e-a.pxself(d,\"marginBottom\")+\"px\"}}}};this.adjust=function(){var c=a.getElement(A);if(!c)return false;s.initResize&&s.initResize(a,A,q);if(a.isHidden(c))return true;var m=c.firstChild;if(m.style.height!=\"\")m.style.height=\"\";var i=a.pxself(c.parentNode,\"height\");if(i==0){i=c.parentNode.clientHeight; i+=-a.px(c.parentNode,\"paddingTop\")-a.px(c.parentNode,\"paddingBottom\")}i+=-a.px(c,\"marginTop\")-a.px(c,\"marginBottom\");var k,n;if(c.parentNode.children){k=0;for(n=c.parentNode.children.length;k<n;++k){var e=c.parentNode.children[k];if(e!=c)i-=$(e).outerHeight()}}var d=c=0,l,t;l=k=0;for(n=m.rows.length;k<n;k++){e=m.rows[k];if(e.className==\"Wt-hrh\")i-=e.offsetHeight;else{d+=q.minheight[l];if(q.stretch[l]<=0)i-=e.offsetHeight;else c+=q.stretch[l];++l}}i=i>d?i:d;if(c!=0&&i>0){d=i;var j;l=k=0;for(n=m.rows.length;k< n;k++)if(m.rows[k].className!=\"Wt-hrh\"){e=m.rows[k];if(q.stretch[l]!=0){if(q.stretch[l]!=-1){j=i*q.stretch[l]/c;j=d>j?j:d;j=Math.round(q.minheight[l]>j?q.minheight[l]:j);d-=j}else j=e.offsetHeight;this.adjustRow(e,j)}++l}}if(m.style.tableLayout!=\"fixed\")return true;l=0;d=m.childNodes;i=0;for(c=d.length;i<c;i++){j=d[i];var r,w,y,x;if(a.hasTag(j,\"COLGROUP\")){i=-1;d=j.childNodes;c=d.length}if(a.hasTag(j,\"COL\")){if(a.pctself(j,\"width\")==0){k=r=0;for(n=m.rows.length;k<n;k++){e=m.rows[k];e=e.childNodes; y=w=0;for(x=e.length;y<x;y++){t=e[y];if(t.colSpan==1&&w==l&&t.childNodes.length==1){e=t.firstChild;e=e.offsetWidth+s.marginH(e);r=Math.max(r,e);break}w+=t.colSpan;if(w>l)break}}if(r>0&&a.pxself(j,\"width\")!=r)j.style.width=r+\"px\"}++l}}return true}};";
	}

	static String wtjs2(WApplication app) {
		return "Wt3_1_5.StdLayout.prototype.initResize = function(a,A,q){function s(b){var f,g,h,o=a.getElement(A).firstChild.childNodes;g=f=0;for(h=o.length;g<h;g++){var p=o[g];if(a.hasTag(p,\"COLGROUP\")){g=-1;o=p.childNodes;h=o.length}if(a.hasTag(p,\"COL\"))if(p.className!=\"Wt-vrh\")if(f==b)return p;else++f}return null}function c(b,f){if(b.offsetWidth>0)return b.offsetWidth;else{b=b.parentNode.rows[0];var g,h,o,p;o=h=0;for(p=b.childNodes.length;h<p;++h){g=b.childNodes[h];if(g.className!=\"Wt-vrh\"){if(o== f)return g.offsetWidth;o+=g.colSpan}}return 0}}function m(b,f){var g=a.getElement(A).firstChild;s(b).style.width=f+\"px\";var h,o,p,u;o=h=0;for(p=g.rows.length;h<p;h++){u=g.rows[h];if(u.className!=\"Wt-hrh\"){var v,B,D,F;D=B=0;for(F=u.childNodes.length;B<F;++B){v=u.childNodes[B];if(v.className!=\"Wt-vrh\"){if(v.colSpan==1&&D==b&&v.childNodes.length==1){u=v.firstChild;v=f-l.marginH(u);u.style.width=v+\"px\";break}D+=v.colSpan}}++o}}}function i(b,f,g){var h=b.firstChild;new a.SizeHandle(a,\"v\",h.offsetHeight, h.offsetWidth,-b.parentNode.previousSibling.offsetHeight,b.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(o){n(b,f,o)},h,t,g,0,0)}function k(b,f,g){var h=-b.previousSibling.offsetWidth,o=b.nextSibling.offsetWidth,p=b.firstChild,u=a.pxself(j.rows[0].childNodes[0],\"paddingTop\"),v=a.pxself(j.rows[j.rows.length-1].childNodes[0],\"paddingBottom\");new a.SizeHandle(a,\"h\",p.offsetWidth,j.offsetHeight-u-v,h,o,\"Wt-hsh\",function(B){d(b,f,B)},p,t,g,0,-b.offsetTop+u-a.pxself(b,\"paddingTop\"))}function n(b, f,g){var h=b.parentNode.previousSibling;b=b.parentNode.nextSibling;var o=h.offsetHeight,p=b.offsetHeight;if(q.stretch[f]>0&&q.stretch[f+1]>0)q.stretch[f]=-1;if(q.stretch[f+1]==0)q.stretch[f+1]=-1;q.stretch[f]<=0&&l.adjustRow(h,o+g);q.stretch[f+1]<=0&&l.adjustRow(b,p-g);window.onresize()}function e(){var b,f=0;for(b=0;;++b){var g=s(b);if(g)f+=a.pctself(g,\"width\");else break}if(f!=0)for(b=0;;++b)if(g=s(b)){var h=a.pctself(g,\"width\");if(h)g.style.width=h*100/f+\"%\"}else break}function d(b,f,g){b=s(f); var h=c(b,f),o=s(f+1),p=c(o,f+1);if(a.pctself(b,\"width\")>0&&a.pctself(o,\"width\")>0){b.style.width=\"\";e()}a.pctself(b,\"width\")==0&&m(f,h+g);a.pctself(o,\"width\")==0&&m(f+1,p-g);window.onresize()}var l=this,t=a.getElement(A);if(t)if(!l.resizeInitialized){var j=t.firstChild,r,w,y,x;w=r=0;for(y=j.rows.length;r<y;r++){x=j.rows[r];if(x.className==\"Wt-hrh\"){var z=x.firstChild;z.ri=w-1;z.onmousedown=function(b){i(this,this.ri,b||window.event)}}else{var C,E,G;E=C=0;for(G=x.childNodes.length;C<G;++C){z=x.childNodes[C]; if(z.className==\"Wt-vrh\"){z.ci=E-1;z.onmousedown=function(b){k(this,this.ci,b||window.event)}}else E+=z.colSpan}++w}}l.resizeInitialized=true}};";
	}

	static String appjs1(WApplication app) {
		return app.getJavaScriptClass() + ".layouts = [];";
	}

	static String appjs2(WApplication app) {
		return app.getJavaScriptClass()
				+ ".layoutsAdjust = function(){if(!this.adjusting){this.adjusting=true;var a;for(a=0;a<this.layouts.length;++a)if(!this.layouts[a].adjust()){this.WT.arrayRemove(this.layouts,a);--a}this.adjusting=false}};";
	}

	static String wtjs10(WApplication app) {
		return "Wt3_1_5.ChildrenResize = function(b,f,d){var c,e,a;b.style.height=d+\"px\";c=0;for(e=b.childNodes.length;c<e;++c){a=b.childNodes[c];if(a.nodeType==1)if(a.wtResize)a.wtResize(a,f,d);else if(a.style.height!=b.style.height)a.style.height=b.style.height}};";
	}
}
