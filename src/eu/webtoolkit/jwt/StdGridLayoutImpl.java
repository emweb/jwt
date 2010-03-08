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
			app.getStyleSheet()
					.addRule("table.Wt-hcenter", "margin: 0px auto;");
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
		String divStyle = "";
		if (fitHeight && !app.getEnvironment().agentIsIE()) {
			divStyle += "height: 100%;";
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
					".layouts.push(new Wt3_1_1.StdLayout( Wt3_1_1, '").append(
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
						int padt = 0;
						int padb = 0;
						if (row == 0) {
							padt = margin[0];
						}
						if (row + 1 == rowCount) {
							padb = margin[2];
						}
						String style = "padding:" + String.valueOf(padt)
								+ "px 0px" + String.valueOf(padb) + "px;";
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
			if (app.getEnvironment().agentIsIE()) {
				container.setOverflow(WContainerWidget.Overflow.OverflowHidden);
			}
		}
	}

	private Grid grid_;
	private boolean useFixedLayout_;

	static String wtjs1(WApplication app) {
		return "Wt3_1_1.StdLayout = function(a,z,o){var v=this;this.marginH=function(b){var n=b.parentNode;return a.px(b,\"marginLeft\")+a.px(b,\"marginRight\")+a.px(b,\"borderLeftWidth\")+a.px(b,\"borderRightWidth\")+a.px(n,\"paddingLeft\")+a.px(n,\"paddingRight\")};this.marginV=function(b){return a.px(b,\"marginTop\")+a.px(b,\"marginBottom\")+a.px(b,\"borderTopWidth\")+a.px(b,\"borderBottomWidth\")+a.px(b,\"paddingTop\")+a.px(b,\"paddingBottom\")};this.adjustRow=function(b,n){if(b.style.height!=n+\"px\")b.style.height=n+ \"px\";b=b.childNodes;var i,r,h;i=0;for(r=b.length;i<r;++i){h=b[i];var d=n-a.pxself(h,\"paddingTop\")-a.pxself(h,\"paddingBottom\");if(d<=0)d=0;h.style.height=d+\"px\";if(!(h.style.verticalAlign||h.childNodes.length==0)){var e=h.childNodes[0];if(d<=0)d=0;if(e.className==\"Wt-hcenter\"){e.style.height=d+\"px\";e=e.firstChild.firstChild;if(!a.hasTag(e,\"TD\"))e=e.firstChild;if(e.style.height!=d+\"px\")e.style.height=d+\"px\";e=e.firstChild}if(h.childNodes.length==1)d+=-this.marginV(e);if(d<=0)d=0;if(!a.hasTag(e,\"TABLE\"))if(e.wtResize){h= e.parentNode.offsetWidth-v.marginH(e);e.wtResize(e,h,d)}else if(e.style.height!=d+\"px\")e.style.height=d+\"px\"}}};this.adjust=function(){var b=a.getElement(z);if(!b)return false;v.initResize&&v.initResize(a,z,o);if(a.isHidden(b))return true;var n=b.firstChild;n.style.height=\"\";var i=a.pxself(b.parentNode,\"height\");if(i==0){i=b.parentNode.clientHeight;i+=-a.px(b.parentNode,\"paddingTop\")-a.px(b.parentNode,\"paddingBottom\")}i+=-a.px(b,\"marginTop\")-a.px(b,\"marginBottom\");var r=0,h=0,d,e,j,s;d=b=0;for(e= n.rows.length;b<e;b++){j=n.rows[b];if(j.className==\"Wt-hrh\")i-=j.offsetHeight;else{h+=o.minheight[d];if(o.stretch[d]<=0)i-=j.offsetHeight;else r+=o.stretch[d];++d}}i=i>h?i:h;if(r!=0&&i>0){h=i;var l;d=b=0;for(e=n.rows.length;b<e;b++)if(n.rows[b].className!=\"Wt-hrh\"){j=n.rows[b];if(o.stretch[d]!=0){if(o.stretch[d]!=-1){l=i*o.stretch[d]/r;l=h>l?l:h;l=Math.round(o.minheight[d]>l?o.minheight[d]:l);h-=l}else l=j.offsetHeight;this.adjustRow(j,l)}++d}}if(n.style.tableLayout!=\"fixed\")return true;d=0;h=n.childNodes; i=0;for(r=h.length;i<r;i++){l=h[i];var u,t,q,x;if(a.hasTag(l,\"COLGROUP\")){i=-1;h=l.childNodes;r=h.length}if(a.hasTag(l,\"COL\")){if(a.pctself(l,\"width\")==0){b=u=0;for(e=n.rows.length;b<e;b++){j=n.rows[b];j=j.childNodes;q=t=0;for(x=j.length;q<x;q++){s=j[q];if(s.colSpan==1&&t==d&&s.childNodes.length==1){j=s.firstChild;j=j.offsetWidth+v.marginH(j);u=Math.max(u,j);break}t+=s.colSpan;if(t>d)break}}if(u>0&&a.pxself(l,\"width\")!=u)l.style.width=u+\"px\"}++d}}return true}};";
	}

	static String wtjs2(WApplication app) {
		return "Wt3_1_1.StdLayout.prototype.initResize = function(a,z,o){function v(c){var f,k,g,m=a.getElement(z).firstChild.childNodes;k=f=0;for(g=m.length;k<g;k++){var p=m[k];if(a.hasTag(p,\"COLGROUP\")){k=-1;m=p.childNodes;g=m.length}if(a.hasTag(p,\"COL\"))if(p.className!=\"Wt-vrh\")if(f==c)return p;else++f}return null}function b(c,f){if(c.offsetWidth>0)return c.offsetWidth;else{c=c.parentNode.rows[0];var k,g,m,p;m=g=0;for(p=c.childNodes.length;g<p;++g){k=c.childNodes[g];if(k.className!=\"Wt-vrh\"){if(m== f)return k.offsetWidth;m+=k.colSpan}}return 0}}function n(c,f){var k=a.getElement(z).firstChild;v(c).style.width=f+\"px\";var g,m,p,y;m=g=0;for(p=k.rows.length;g<p;g++){y=k.rows[g];if(y.className!=\"Wt-hrh\"){var w,A,B,D;B=A=0;for(D=y.childNodes.length;A<D;++A){w=y.childNodes[A];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&B==c&&w.childNodes.length==1){y=w.firstChild;w=f-e.marginH(y);y.style.width=w+\"px\";break}B+=w.colSpan}}++m}}}function i(c,f,k){var g=c.firstChild;new a.SizeHandle(a,\"v\",g.offsetHeight, g.offsetWidth,-c.parentNode.previousSibling.offsetHeight,c.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(m){h(c,f,m)},g,k)}function r(c,f,k){var g=c.firstChild;new a.SizeHandle(a,\"h\",g.offsetWidth,g.offsetHeight,-c.previousSibling.offsetWidth,c.nextSibling.offsetWidth,\"Wt-hsh\",function(m){d(c,f,m)},g,k)}function h(c,f,k){var g=c.parentNode.previousSibling;c=c.parentNode.nextSibling;var m=g.offsetHeight,p=c.offsetHeight;if(o.stretch[f]>0&&o.stretch[f+1]>0)o.stretch[f]=-1;if(o.stretch[f+1]== 0)o.stretch[f+1]=-1;o.stretch[f]<=0&&e.adjustRow(g,m+k);o.stretch[f+1]<=0&&e.adjustRow(c,p-k);window.onresize()}function d(c,f,k){c=v(f);var g=b(c,f),m=v(f+1),p=b(m,f+1);if(a.pctself(c,\"width\")>0&&a.pctself(m,\"width\")>0)c.style.width=\"\";a.pctself(c,\"width\")==0&&n(f,g+k);a.pctself(m,\"width\")==0&&n(f+1,p-k);window.onresize()}var e=this,j=a.getElement(z);if(j)if(!e.resizeInitialized){j=j.firstChild;var s,l,u,t;l=s=0;for(u=j.rows.length;s<u;s++){t=j.rows[s];if(t.className==\"Wt-hrh\"){var q=t.firstChild; q.ri=l-1;q.onmousedown=function(c){i(this,this.ri,c||window.event)}}else{var x,C,E;C=x=0;for(E=t.childNodes.length;x<E;++x){q=t.childNodes[x];if(q.className==\"Wt-vrh\"){q.ci=C-1;q.onmousedown=function(c){r(this,this.ci,c||window.event)}}else C+=q.colSpan}++l}}e.resizeInitialized=true}};";
	}

	static String appjs1(WApplication app) {
		return app.getJavaScriptClass() + ".layouts = [];";
	}

	static String appjs2(WApplication app) {
		return app.getJavaScriptClass()
				+ ".layoutsAdjust = function(){var a;for(a=0;a<this.layouts.length;++a)if(!this.layouts[a].adjust()){this.WT.arrayRemove(this.layouts,a);--a}};";
	}
}
