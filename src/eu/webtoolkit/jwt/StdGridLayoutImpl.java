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
					".layouts.add(new Wt3_1_6.StdLayout( Wt3_1_6, '").append(
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
		return "Wt3_1_6.ChildrenResize";
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
		return "Wt3_1_6.StdLayout = function(a,t,g){var l=this;this.getId=function(){return t};this.WT=a;this.marginH=function(b){var j=b.parentNode;return a.px(b,\"marginLeft\")+a.px(b,\"marginRight\")+a.px(b,\"borderLeftWidth\")+a.px(b,\"borderRightWidth\")+a.px(j,\"paddingLeft\")+a.px(j,\"paddingRight\")};this.marginV=function(b){return a.px(b,\"marginTop\")+a.px(b,\"marginBottom\")+a.px(b,\"borderTopWidth\")+a.px(b,\"borderBottomWidth\")+a.px(b,\"paddingTop\")+a.px(b,\"paddingBottom\")};this.adjustRow=function(b,j){if(b.style.height!= j+\"px\")b.style.height=j+\"px\";b=b.childNodes;var k,m,o;k=0;for(m=b.length;k<m;++k){o=b[k];var d=j-a.pxself(o,\"paddingTop\")-a.pxself(o,\"paddingBottom\");if(d<=0)d=0;o.style.height=d+\"px\";if(!(o.style.verticalAlign||o.childNodes.length==0)){var e=o.childNodes[0];if(d<=0)d=0;if(e.className==\"Wt-hcenter\"){e.style.height=d+\"px\";e=e.firstChild.firstChild;if(!a.hasTag(e,\"TD\"))e=e.firstChild;if(e.style.height!=d+\"px\")e.style.height=d+\"px\";e=e.firstChild}if(o.childNodes.length==1)d+=-this.marginV(e);if(d<=0)d= 0;if(!a.hasTag(e,\"TABLE\"))if(e.wtResize){o=e.parentNode.offsetWidth-l.marginH(e);e.wtResize(e,o,d)}else if(e.style.height!=d+\"px\"){e.style.height=d+\"px\";if(e.className==\"Wt-wrapdiv\")if(a.isIE&&a.hasTag(e.firstChild,\"TEXTAREA\"))e.firstChild.style.height=d-a.pxself(e,\"marginBottom\")+\"px\"}}}};this.adjust=function(){var b=a.getElement(t);if(!b)return false;l.initResize&&l.initResize(a,t,g);if(a.isHidden(b))return true;var j=b.firstChild;if(j.style.height!=\"\")j.style.height=\"\";if(b.dirty||j.w!=b.clientWidth|| j.h!=b.clientHeight)b.dirty=null;else return true;var k=a.pxself(b.parentNode,\"height\");if(k==0){k=b.parentNode.clientHeight;k+=-a.px(b.parentNode,\"paddingTop\")-a.px(b.parentNode,\"paddingBottom\")}k+=-a.px(b,\"marginTop\")-a.px(b,\"marginBottom\");var m,o;if(b.parentNode.children){m=0;for(o=b.parentNode.children.length;m<o;++m){var d=b.parentNode.children[m];if(d!=b)k-=$(d).outerHeight()}}var e=0,r=0,n,u;n=m=0;for(o=j.rows.length;m<o;m++){d=j.rows[m];if(d.className==\"Wt-hrh\")k-=d.offsetHeight;else{r+= g.minheight[n];if(g.stretch[n]<=0)k-=d.offsetHeight;else e+=g.stretch[n];++n}}k=k>r?k:r;if(e!=0&&k>0){r=k;var p;n=m=0;for(o=j.rows.length;m<o;m++)if(j.rows[m].className!=\"Wt-hrh\"){d=j.rows[m];if(g.stretch[n]!=0){if(g.stretch[n]!=-1){p=k*g.stretch[n]/e;p=r>p?p:r;p=Math.round(g.minheight[n]>p?g.minheight[n]:p);r-=p}else p=d.offsetHeight;this.adjustRow(d,p)}++n}}j.w=b.clientWidth;j.h=b.clientHeight;if(j.style.tableLayout!=\"fixed\")return true;e=0;n=j.childNodes;b=0;for(k=n.length;b<k;b++){r=n[b];var x, z,y;if(a.hasTag(r,\"COLGROUP\")){b=-1;n=r.childNodes;k=n.length}if(a.hasTag(r,\"COL\")){if(a.pctself(r,\"width\")==0){m=p=0;for(o=j.rows.length;m<o;m++){d=j.rows[m];d=d.childNodes;z=x=0;for(y=d.length;z<y;z++){u=d[z];if(u.colSpan==1&&x==e&&u.childNodes.length==1){d=u.firstChild;d=d.offsetWidth+l.marginH(d);p=Math.max(p,d);break}x+=u.colSpan;if(x>e)break}}if(p>0&&a.pxself(r,\"width\")!=p)r.style.width=p+\"px\"}++e}}return true};this.adjust()};";
	}

	static String wtjs2(WApplication app) {
		return "Wt3_1_6.StdLayout.prototype.initResize = function(a,t,g){function l(c){var f,h,i,q=a.getElement(t).firstChild.childNodes;h=f=0;for(i=q.length;h<i;h++){var s=q[h];if(a.hasTag(s,\"COLGROUP\")){h=-1;q=s.childNodes;i=q.length}if(a.hasTag(s,\"COL\"))if(s.className!=\"Wt-vrh\")if(f==c)return s;else++f}return null}function b(c,f){if(c.offsetWidth>0)return c.offsetWidth;else{c=c.parentNode.rows[0];var h,i,q,s;q=i=0;for(s=c.childNodes.length;i<s;++i){h=c.childNodes[i];if(h.className!=\"Wt-vrh\"){if(q== f)return h.offsetWidth;q+=h.colSpan}}return 0}}function j(c,f){var h=a.getElement(t).firstChild;l(c).style.width=f+\"px\";var i,q,s,v;q=i=0;for(s=h.rows.length;i<s;i++){v=h.rows[i];if(v.className!=\"Wt-hrh\"){var w,B,D,F;D=B=0;for(F=v.childNodes.length;B<F;++B){w=v.childNodes[B];if(w.className!=\"Wt-vrh\"){if(w.colSpan==1&&D==c&&w.childNodes.length==1){v=w.firstChild;w=f-r.marginH(v);v.style.width=w+\"px\";break}D+=w.colSpan}}++q}}}function k(c,f,h){var i=c.firstChild;new a.SizeHandle(a,\"v\",i.offsetHeight, i.offsetWidth,-c.parentNode.previousSibling.offsetHeight,c.parentNode.nextSibling.offsetHeight,\"Wt-vsh\",function(q){o(c,f,q)},i,n,h,0,0)}function m(c,f,h){var i=-c.previousSibling.offsetWidth,q=c.nextSibling.offsetWidth,s=c.firstChild,v=a.pxself(u.rows[0].childNodes[0],\"paddingTop\"),w=a.pxself(u.rows[u.rows.length-1].childNodes[0],\"paddingBottom\");new a.SizeHandle(a,\"h\",s.offsetWidth,u.offsetHeight-v-w,i,q,\"Wt-hsh\",function(B){e(c,f,B)},s,n,h,0,-c.offsetTop+v-a.pxself(c,\"paddingTop\"))}function o(c, f,h){var i=c.parentNode.previousSibling;c=c.parentNode.nextSibling;var q=i.offsetHeight,s=c.offsetHeight;if(g.stretch[f]>0&&g.stretch[f+1]>0)g.stretch[f]=-1;if(g.stretch[f+1]==0)g.stretch[f+1]=-1;g.stretch[f]<=0&&r.adjustRow(i,q+h);g.stretch[f+1]<=0&&r.adjustRow(c,s-h);a.getElement(t).dirty=true;window.onresize()}function d(){var c,f=0;for(c=0;;++c){var h=l(c);if(h)f+=a.pctself(h,\"width\");else break}if(f!=0)for(c=0;;++c)if(h=l(c)){var i=a.pctself(h,\"width\");if(i)h.style.width=i*100/f+\"%\"}else break} function e(c,f,h){c=l(f);var i=b(c,f),q=l(f+1),s=b(q,f+1);if(a.pctself(c,\"width\")>0&&a.pctself(q,\"width\")>0){c.style.width=\"\";d()}a.pctself(c,\"width\")==0&&j(f,i+h);a.pctself(q,\"width\")==0&&j(f+1,s-h);window.onresize()}var r=this,n=a.getElement(t);if(n)if(!r.resizeInitialized){var u=n.firstChild,p,x,z,y;x=p=0;for(z=u.rows.length;p<z;p++){y=u.rows[p];if(y.className==\"Wt-hrh\"){var A=y.firstChild;A.ri=x-1;A.onmousedown=function(c){k(this,this.ri,c||window.event)}}else{var C,E,G;E=C=0;for(G=y.childNodes.length;C< G;++C){A=y.childNodes[C];if(A.className==\"Wt-vrh\"){A.ci=E-1;A.onmousedown=function(c){m(this,this.ci,c||window.event)}}else E+=A.colSpan}++x}}r.resizeInitialized=true}};";
	}

	static String appjs1(WApplication app) {
		return app.getJavaScriptClass()
				+ ".layouts = new (function(){var a=[],t=false;this.add=function(g){var l,b;l=0;for(b=a.length;l<b;++l)if(a[l].getId()==g.getId()){a[l]=g;return}a.push(g)};this.adjust=function(g){if(g){if(g=$(\"#\"+g).get(0))g.dirty=true}else if(!t){t=true;for(var l=0;l<a.length;++l){g=a[l];if(!g.adjust()){g.WT.arrayRemove(a,l);--l}}t=false}}});";
	}

	static String wtjs10(WApplication app) {
		return "Wt3_1_6.ChildrenResize = function(b,f,d){var c,e,a;b.style.height=d+\"px\";c=0;for(e=b.childNodes.length;c<e;++c){a=b.childNodes[c];if(a.nodeType==1)if(a.wtResize)a.wtResize(a,f,d);else if(a.style.height!=b.style.height)a.style.height=b.style.height}};";
	}
}
